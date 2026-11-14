package org.vassalengine.tools.vsav;

import org.vassalengine.tools.vsav.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Writes ExportData back to a .vsav (saved game) file.
 */
public class VsavWriter {

    /** Encoding of the savedGame entry; defaults to the VASSAL 3.8 (XZ) format */
    private SavedGameFormat format = SavedGameFormat.XZ;

    /** Choose the savedGame encoding (LEGACY for files VASSAL 3.7 can open) */
    public void setFormat(SavedGameFormat format) {
        this.format = format;
    }

    private static final String SAVEDATA_ENTRY = "savedata";
    private static final String MODULEDATA_ENTRY = "moduledata";
    private static final String SAVEDGAME_ENTRY = "savedGame";

    /**
     * Write ExportData to a .vsav file.
     *
     * @param data The export data to write
     * @param outputPath Path to the output .vsav file
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

            // Write savedGame (XZ or legacy hex)
            writeSavedGame(zos, data.getCommands());
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
     * Write savedGame entry (XZ or legacy hex).
     */
    private void writeSavedGame(ZipOutputStream zos, java.util.List<CommandData> commands) throws IOException {
        // Encode commands to string
        String gameData = CommandEncoder.encodeCommands(commands);

        // Create zip entry
        ZipEntry entry = new ZipEntry(SAVEDGAME_ENTRY);
        zos.putNextEntry(entry);

        // Write the command log in the chosen format (XZ or legacy hex)
        try (OutputStream oos = format.wrap(zos)) {
            oos.write(gameData.getBytes(StandardCharsets.UTF_8));
        }

        zos.closeEntry();
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

}
