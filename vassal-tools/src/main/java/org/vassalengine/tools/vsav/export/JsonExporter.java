package org.vassalengine.tools.vsav.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.vassalengine.tools.vsav.model.*;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Exports ExportData to JSON format.
 */
public class JsonExporter {

    private final Gson gson;

    public JsonExporter() {
        this(true);
    }

    public JsonExporter(boolean prettyPrint) {
        GsonBuilder builder = new GsonBuilder();
        if (prettyPrint) {
            builder.setPrettyPrinting();
        }
        // Register type adapters for polymorphic CommandData
        builder.registerTypeAdapter(CommandData.class, new CommandDataTypeAdapter());
        this.gson = builder.create();
    }

    /**
     * Export to a file.
     */
    public void export(ExportData data, String outputPath) throws IOException {
        try (Writer writer = Files.newBufferedWriter(Path.of(outputPath), StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        }
    }

    /**
     * Export to a string.
     */
    public String exportToString(ExportData data) {
        return gson.toJson(data);
    }

    /**
     * Export to a Writer.
     */
    public void export(ExportData data, Writer writer) {
        gson.toJson(data, writer);
    }
}
