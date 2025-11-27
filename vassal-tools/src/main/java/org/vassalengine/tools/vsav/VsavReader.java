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
 * Reads .vsav (saved game) files and extracts their contents.
 */
public class VsavReader {

    private static final String SAVEDATA_ENTRY = "savedata";
    private static final String MODULEDATA_ENTRY = "moduledata";
    private static final String SAVEDGAME_ENTRY = "savedGame";

    // Command separator is ESC character (0x1B)
    private static final char COMMAND_SEPARATOR = 0x1B;

    /**
     * Read a .vsav file and return its contents as ExportData.
     *
     * @param vsavPath Path to the .vsav file
     * @return ExportData containing all save file contents
     * @throws IOException if file cannot be read
     */
    public ExportData read(String vsavPath) throws IOException {
        ExportData data = new ExportData();

        try (ZipFile zip = new ZipFile(vsavPath)) {
            // Read save metadata
            data.setSaveMetadata(readSaveMetadata(zip));

            // Read module metadata
            data.setModuleMetadata(readModuleMetadata(zip));

            // Read and parse saved game commands
            String gameData = readSavedGame(zip);
            if (gameData != null) {
                List<CommandData> commands = CommandParser.parseCommands(gameData);
                data.setCommands(commands);
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
     * Get raw commands from a .vsav file without parsing.
     * Useful for debugging.
     */
    public List<String> getRawCommands(String vsavPath) throws IOException {
        List<String> commands = new ArrayList<>();

        try (ZipFile zip = new ZipFile(vsavPath)) {
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
