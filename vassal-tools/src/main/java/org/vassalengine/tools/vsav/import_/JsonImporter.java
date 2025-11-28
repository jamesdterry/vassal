package org.vassalengine.tools.vsav.import_;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.vassalengine.tools.vsav.export.CommandDataTypeAdapter;
import org.vassalengine.tools.vsav.export.LogEntryTypeAdapter;
import org.vassalengine.tools.vsav.model.*;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Imports ExportData from JSON format.
 */
public class JsonImporter {

    private final Gson gson;

    public JsonImporter() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CommandData.class, new CommandDataTypeAdapter());
        builder.registerTypeAdapter(LogEntry.class, new LogEntryTypeAdapter());
        this.gson = builder.create();
    }

    /**
     * Import from a file.
     */
    public ExportData importFromFile(String inputPath) throws IOException {
        try (Reader reader = Files.newBufferedReader(Path.of(inputPath), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, ExportData.class);
        }
    }

    /**
     * Import from a string.
     */
    public ExportData importFromString(String json) {
        return gson.fromJson(json, ExportData.class);
    }

    /**
     * Import from a Reader.
     */
    public ExportData importFromReader(Reader reader) {
        return gson.fromJson(reader, ExportData.class);
    }
}
