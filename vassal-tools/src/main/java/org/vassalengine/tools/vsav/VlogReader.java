package org.vassalengine.tools.vsav;

import org.vassalengine.tools.vsav.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import VASSAL.build.Builder;
import VASSAL.tools.io.DeobfuscatingInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads .vlog (game log) files and extracts their contents.
 *
 * VLOG files have the same ZIP structure as VSAV files, but the command stream
 * contains LOG/UNDO prefixed entries:
 * - Initial state commands (before begin_log)
 * - LOG\t prefixed logged commands
 * - UNDO\t prefixed undo markers
 */
public class VlogReader {

    private static final String SAVEDATA_ENTRY = "savedata";
    private static final String MODULEDATA_ENTRY = "moduledata";
    private static final String SAVEDGAME_ENTRY = "savedGame";

    // Command separator is ESC character (0x1B)
    private static final char COMMAND_SEPARATOR = 0x1B;

    // VLOG prefixes (from BasicLogger)
    private static final String LOG_PREFIX = "LOG\t";
    private static final String UNDO_PREFIX = "UNDO\t";

    /**
     * Read a .vlog file and return its contents as ExportData.
     *
     * @param vlogPath Path to the .vlog file
     * @return ExportData containing all log file contents
     * @throws IOException if file cannot be read
     */
    public ExportData read(String vlogPath) throws IOException {
        ExportData data = new ExportData();
        data.setFileType("vlog");

        try (ZipFile zip = new ZipFile(vlogPath)) {
            // Read save metadata
            data.setSaveMetadata(readSaveMetadata(zip));

            // Read module metadata
            data.setModuleMetadata(readModuleMetadata(zip));

            // Read and parse saved game data
            String gameData = readSavedGame(zip);
            if (gameData != null) {
                parseVlogContent(gameData, data);
            }
        }

        return data;
    }

    /**
     * Read the savedata XML entry.
     */
    private SaveMetadata readSaveMetadata(ZipFile zip) throws IOException {
        ZipEntry entry = zip.getEntry(SAVEDATA_ENTRY);
        if (entry == null) {
            return null;
        }

        SaveMetadata metadata = new SaveMetadata();
        try (InputStream in = zip.getInputStream(entry)) {
            Document doc = Builder.createDocument(in);
            Element root = doc.getDocumentElement();

            metadata.setVersion(getElementText(root, "version"));
            metadata.setDescription(getElementText(root, "description"));
        } catch (Exception e) {
            throw new IOException("Failed to parse savedata: " + e.getMessage(), e);
        }

        return metadata;
    }

    /**
     * Read the moduledata XML entry.
     */
    private ModuleMetadata readModuleMetadata(ZipFile zip) throws IOException {
        ZipEntry entry = zip.getEntry(MODULEDATA_ENTRY);
        if (entry == null) {
            return null;
        }

        ModuleMetadata metadata = new ModuleMetadata();
        try (InputStream in = zip.getInputStream(entry)) {
            Document doc = Builder.createDocument(in);
            Element root = doc.getDocumentElement();

            metadata.setName(getElementText(root, "name"));
            metadata.setVersion(getElementText(root, "version"));
            metadata.setDescription(getElementText(root, "description"));
            metadata.setVassalVersion(getElementText(root, "VassalVersion"));
        } catch (Exception e) {
            throw new IOException("Failed to parse moduledata: " + e.getMessage(), e);
        }

        return metadata;
    }

    /**
     * Read and deobfuscate the savedGame entry.
     */
    private String readSavedGame(ZipFile zip) throws IOException {
        ZipEntry entry = zip.getEntry(SAVEDGAME_ENTRY);
        if (entry == null) {
            return null;
        }

        try (InputStream zipIn = zip.getInputStream(entry);
             InputStream deobfuscated = new DeobfuscatingInputStream(zipIn);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            deobfuscated.transferTo(baos);
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    /**
     * Parse VLOG content into initial state commands and log entries.
     *
     * VLOG format:
     * - Commands before begin_log: initial state
     * - begin_log marker
     * - LOG\t{command} entries: logged commands
     * - UNDO\t{flag} entries: undo markers
     * - end_log marker (optional at end)
     */
    private void parseVlogContent(String gameData, ExportData data) {
        List<CommandData> initialState = new ArrayList<>();
        List<LogEntry> logEntries = new ArrayList<>();

        boolean inLogSection = false;

        StringBuilder current = new StringBuilder();
        for (char c : gameData.toCharArray()) {
            if (c == COMMAND_SEPARATOR) {
                if (current.length() > 0) {
                    String cmdStr = current.toString();

                    if (!inLogSection) {
                        // Check for begin_log marker
                        if ("begin_log".equals(cmdStr)) {
                            CommandData beginLogCmd = new CommandData(
                                CommandData.CommandType.BEGIN_LOG, cmdStr);
                            initialState.add(beginLogCmd);
                            inLogSection = true;
                        } else {
                            // Initial state command
                            CommandData cmd = CommandParser.parseCommand(cmdStr);
                            if (cmd != null) {
                                initialState.add(cmd);
                            }
                        }
                    } else {
                        // In log section - parse LOG/UNDO entries
                        LogEntry entry = parseLogEntry(cmdStr);
                        if (entry != null) {
                            logEntries.add(entry);
                        }
                    }

                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }

        // Handle final command
        if (current.length() > 0) {
            String cmdStr = current.toString();
            if (!inLogSection) {
                CommandData cmd = CommandParser.parseCommand(cmdStr);
                if (cmd != null) {
                    initialState.add(cmd);
                }
            } else {
                LogEntry entry = parseLogEntry(cmdStr);
                if (entry != null) {
                    logEntries.add(entry);
                }
            }
        }

        data.setCommands(initialState);
        data.setLogEntries(logEntries);
    }

    /**
     * Parse a single log entry (LOG\t or UNDO\t prefixed).
     */
    private LogEntry parseLogEntry(String entryStr) {
        if (entryStr == null || entryStr.isEmpty()) {
            return null;
        }

        // Check for end_log marker
        if ("end_log".equals(entryStr)) {
            LogEntry entry = new LogEntry(LogEntry.EntryType.LOG);
            entry.setCommand(new CommandData(CommandData.CommandType.END_LOG, entryStr));
            entry.setRawEntry(entryStr);
            return entry;
        }

        // Parse LOG\t entries
        if (entryStr.startsWith(LOG_PREFIX)) {
            String innerCommand = entryStr.substring(LOG_PREFIX.length());
            CommandData cmd = CommandParser.parseCommand(innerCommand);

            LogEntry entry = new LogEntry(LogEntry.EntryType.LOG, cmd);
            entry.setRawEntry(entryStr);
            return entry;
        }

        // Parse UNDO\t entries
        if (entryStr.startsWith(UNDO_PREFIX)) {
            String undoFlag = entryStr.substring(UNDO_PREFIX.length());

            LogEntry entry = new LogEntry(LogEntry.EntryType.UNDO);
            entry.setUndoInProgress("true".equalsIgnoreCase(undoFlag));
            entry.setRawEntry(entryStr);
            return entry;
        }

        // Unknown entry - treat as LOG with raw command
        LogEntry entry = new LogEntry(LogEntry.EntryType.LOG);
        CommandData cmd = new CommandData(CommandData.CommandType.UNKNOWN, entryStr);
        entry.setCommand(cmd);
        entry.setRawEntry(entryStr);
        return entry;
    }

    /**
     * Get text content of a child element.
     */
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return "";
    }

    /**
     * Get raw commands from a .vlog file without parsing.
     * Useful for debugging.
     */
    public List<String> getRawCommands(String vlogPath) throws IOException {
        List<String> commands = new ArrayList<>();

        try (ZipFile zip = new ZipFile(vlogPath)) {
            String gameData = readSavedGame(zip);
            if (gameData != null) {
                StringBuilder current = new StringBuilder();
                for (char c : gameData.toCharArray()) {
                    if (c == COMMAND_SEPARATOR) {
                        if (current.length() > 0) {
                            commands.add(current.toString());
                            current = new StringBuilder();
                        }
                    } else {
                        current.append(c);
                    }
                }
                // Add final command if any
                if (current.length() > 0) {
                    commands.add(current.toString());
                }
            }
        }

        return commands;
    }
}
