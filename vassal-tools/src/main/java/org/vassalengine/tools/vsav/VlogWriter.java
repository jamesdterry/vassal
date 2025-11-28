package org.vassalengine.tools.vsav;

import org.vassalengine.tools.vsav.model.*;
import VASSAL.tools.io.ObfuscatingOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Writes ExportData back to a .vlog (game log) file.
 *
 * VLOG files have the same structure as VSAV files, but the command stream
 * includes LOG/UNDO prefixed entries after the initial state.
 */
public class VlogWriter {

    private static final String SAVEDATA_ENTRY = "savedata";
    private static final String MODULEDATA_ENTRY = "moduledata";
    private static final String SAVEDGAME_ENTRY = "savedGame";

    // Command separator is ESC character (0x1B)
    private static final char COMMAND_SEPARATOR = 0x1B;

    // VLOG prefixes (from BasicLogger)
    private static final String LOG_PREFIX = "LOG\t";
    private static final String UNDO_PREFIX = "UNDO\t";

    /**
     * Write ExportData to a .vlog file.
     *
     * @param data The export data to write
     * @param outputPath Path to the output .vlog file
     * @throws IOException if file cannot be written
     */
    public void write(ExportData data, String outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // Write savedata XML
            if (data.getSaveMetadata() != null) {
                writeSaveMetadata(zos, data.getSaveMetadata());
            }

            // Write moduledata XML
            if (data.getModuleMetadata() != null) {
                writeModuleMetadata(zos, data.getModuleMetadata());
            }

            // Write savedGame (obfuscated) - initial state + log entries
            writeSavedGame(zos, data);
        }
    }

    /**
     * Write savedata XML entry.
     */
    private void writeSaveMetadata(ZipOutputStream zos, SaveMetadata metadata) throws IOException {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<data version=\"1\">\n");
        xml.append("  <version>").append(escapeXml(metadata.getVersion())).append("</version>\n");
        if (metadata.getDescription() != null && !metadata.getDescription().isEmpty()) {
            xml.append("  <description>").append(escapeXml(metadata.getDescription())).append("</description>\n");
        }
        xml.append("</data>\n");

        ZipEntry entry = new ZipEntry(SAVEDATA_ENTRY);
        zos.putNextEntry(entry);
        zos.write(xml.toString().getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    /**
     * Write moduledata XML entry.
     */
    private void writeModuleMetadata(ZipOutputStream zos, ModuleMetadata metadata) throws IOException {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<data version=\"1\">\n");
        xml.append("  <name>").append(escapeXml(metadata.getName())).append("</name>\n");
        xml.append("  <version>").append(escapeXml(metadata.getVersion())).append("</version>\n");
        if (metadata.getDescription() != null && !metadata.getDescription().isEmpty()) {
            xml.append("  <description>").append(escapeXml(metadata.getDescription())).append("</description>\n");
        }
        if (metadata.getVassalVersion() != null) {
            xml.append("  <VassalVersion>").append(escapeXml(metadata.getVassalVersion())).append("</VassalVersion>\n");
        }
        xml.append("</data>\n");

        ZipEntry entry = new ZipEntry(MODULEDATA_ENTRY);
        zos.putNextEntry(entry);
        zos.write(xml.toString().getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    /**
     * Write savedGame entry (obfuscated) with VLOG format.
     * Format: initial_state_commands + begin_log + LOG/UNDO entries
     */
    private void writeSavedGame(ZipOutputStream zos, ExportData data) throws IOException {
        StringBuilder gameData = new StringBuilder();

        // Write initial state commands
        List<CommandData> commands = data.getCommands();
        boolean first = true;
        for (CommandData cmd : commands) {
            if (!first) {
                gameData.append(COMMAND_SEPARATOR);
            }
            gameData.append(CommandEncoder.encodeCommand(cmd));
            first = false;
        }

        // Write log entries if present
        List<LogEntry> logEntries = data.getLogEntries();
        if (logEntries != null && !logEntries.isEmpty()) {
            for (LogEntry entry : logEntries) {
                if (!first) {
                    gameData.append(COMMAND_SEPARATOR);
                }
                gameData.append(encodeLogEntry(entry));
                first = false;
            }
        }

        // Create zip entry
        ZipEntry entry = new ZipEntry(SAVEDGAME_ENTRY);
        zos.putNextEntry(entry);

        // Write obfuscated data
        try (ObfuscatingOutputStream oos = new ObfuscatingOutputStream(new NonClosingOutputStream(zos))) {
            oos.write(gameData.toString().getBytes(StandardCharsets.UTF_8));
        }

        zos.closeEntry();
    }

    /**
     * Encode a log entry to its string representation.
     */
    private String encodeLogEntry(LogEntry entry) {
        if (entry.getEntryType() == LogEntry.EntryType.UNDO) {
            return UNDO_PREFIX + entry.isUndoInProgress();
        } else if (entry.getEntryType() == LogEntry.EntryType.LOG) {
            CommandData cmd = entry.getCommand();
            if (cmd != null) {
                // Check if it's an END_LOG command
                if (cmd.getType() == CommandData.CommandType.END_LOG) {
                    return "end_log";
                }
                return LOG_PREFIX + CommandEncoder.encodeCommand(cmd);
            }
            // Fallback to raw entry if available
            if (entry.getRawEntry() != null) {
                return entry.getRawEntry();
            }
            return "";
        }
        return "";
    }

    /**
     * Escape special XML characters.
     */
    private String escapeXml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * Wrapper to prevent closing the underlying stream when ObfuscatingOutputStream closes.
     */
    private static class NonClosingOutputStream extends FilterOutputStream {
        public NonClosingOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void close() throws IOException {
            // Don't close the underlying stream
            flush();
        }
    }
}
