package org.vassalengine.tools.vsav.export;

import com.google.gson.*;
import org.vassalengine.tools.vsav.model.*;

import java.lang.reflect.Type;

/**
 * GSON type adapter for LogEntry serialization/deserialization.
 * Handles the nested CommandData within LOG entries.
 */
public class LogEntryTypeAdapter implements JsonSerializer<LogEntry>, JsonDeserializer<LogEntry> {

    @Override
    public JsonElement serialize(LogEntry src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();

        result.addProperty("entryType", src.getEntryType().name());

        if (src.getEntryType() == LogEntry.EntryType.LOG) {
            if (src.getCommand() != null) {
                result.add("command", context.serialize(src.getCommand(), CommandData.class));
            }
        } else if (src.getEntryType() == LogEntry.EntryType.UNDO) {
            result.addProperty("undoInProgress", src.isUndoInProgress());
        }

        if (src.getRawEntry() != null) {
            result.addProperty("rawEntry", src.getRawEntry());
        }

        return result;
    }

    @Override
    public LogEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();

        String entryTypeStr = obj.get("entryType").getAsString();
        LogEntry.EntryType entryType = LogEntry.EntryType.valueOf(entryTypeStr);

        LogEntry entry = new LogEntry(entryType);

        if (entryType == LogEntry.EntryType.LOG) {
            if (obj.has("command") && !obj.get("command").isJsonNull()) {
                CommandData cmd = context.deserialize(obj.get("command"), CommandData.class);
                entry.setCommand(cmd);
            }
        } else if (entryType == LogEntry.EntryType.UNDO) {
            if (obj.has("undoInProgress")) {
                entry.setUndoInProgress(obj.get("undoInProgress").getAsBoolean());
            }
        }

        if (obj.has("rawEntry") && !obj.get("rawEntry").isJsonNull()) {
            entry.setRawEntry(obj.get("rawEntry").getAsString());
        }

        return entry;
    }
}
