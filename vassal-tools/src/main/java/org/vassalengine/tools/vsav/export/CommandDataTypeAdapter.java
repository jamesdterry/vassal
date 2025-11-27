package org.vassalengine.tools.vsav.export;

import com.google.gson.*;
import org.vassalengine.tools.vsav.model.*;

import java.lang.reflect.Type;

/**
 * GSON type adapter for CommandData polymorphism.
 * Handles serialization/deserialization of different command types.
 */
public class CommandDataTypeAdapter implements JsonSerializer<CommandData>, JsonDeserializer<CommandData> {

    @Override
    public JsonElement serialize(CommandData src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();

        // Add the type discriminator
        result.addProperty("commandType", src.getType().name());

        // Serialize based on actual type
        if (src instanceof AddPieceCommand) {
            AddPieceCommand cmd = (AddPieceCommand) src;
            result.add("piece", context.serialize(cmd.getPiece()));
        } else if (src instanceof RemovePieceCommand) {
            RemovePieceCommand cmd = (RemovePieceCommand) src;
            result.addProperty("pieceId", cmd.getPieceId());
        } else if (src instanceof ChangePieceCommand) {
            ChangePieceCommand cmd = (ChangePieceCommand) src;
            result.addProperty("pieceId", cmd.getPieceId());
            result.addProperty("newState", cmd.getNewState());
            if (cmd.getOldState() != null) {
                result.addProperty("oldState", cmd.getOldState());
            }
        } else if (src instanceof MovePieceCommand) {
            MovePieceCommand cmd = (MovePieceCommand) src;
            result.addProperty("pieceId", cmd.getPieceId());
            result.addProperty("newMapId", cmd.getNewMapId());
            result.addProperty("newX", cmd.getNewX());
            result.addProperty("newY", cmd.getNewY());
            result.addProperty("newUnderId", cmd.getNewUnderId());
            result.addProperty("oldMapId", cmd.getOldMapId());
            result.addProperty("oldX", cmd.getOldX());
            result.addProperty("oldY", cmd.getOldY());
            result.addProperty("oldUnderId", cmd.getOldUnderId());
            result.addProperty("playerId", cmd.getPlayerId());
        }

        // Always include raw command for round-trip fidelity
        if (src.getRawCommand() != null) {
            result.addProperty("rawCommand", src.getRawCommand());
        }

        return result;
    }

    @Override
    public CommandData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();
        String typeStr = obj.get("commandType").getAsString();
        CommandData.CommandType type = CommandData.CommandType.valueOf(typeStr);

        String rawCommand = obj.has("rawCommand") ? obj.get("rawCommand").getAsString() : null;

        switch (type) {
            case ADD_PIECE:
                AddPieceCommand addCmd = new AddPieceCommand();
                addCmd.setRawCommand(rawCommand);
                if (obj.has("piece")) {
                    addCmd.setPiece(context.deserialize(obj.get("piece"), PieceData.class));
                }
                return addCmd;

            case REMOVE_PIECE:
                RemovePieceCommand removeCmd = new RemovePieceCommand();
                removeCmd.setRawCommand(rawCommand);
                if (obj.has("pieceId")) {
                    removeCmd.setPieceId(obj.get("pieceId").getAsString());
                }
                return removeCmd;

            case CHANGE_PIECE:
                ChangePieceCommand changeCmd = new ChangePieceCommand();
                changeCmd.setRawCommand(rawCommand);
                if (obj.has("pieceId")) {
                    changeCmd.setPieceId(obj.get("pieceId").getAsString());
                }
                if (obj.has("newState")) {
                    changeCmd.setNewState(obj.get("newState").getAsString());
                }
                if (obj.has("oldState") && !obj.get("oldState").isJsonNull()) {
                    changeCmd.setOldState(obj.get("oldState").getAsString());
                }
                return changeCmd;

            case MOVE_PIECE:
                MovePieceCommand moveCmd = new MovePieceCommand(rawCommand);
                if (obj.has("pieceId") && !obj.get("pieceId").isJsonNull()) {
                    moveCmd.setPieceId(obj.get("pieceId").getAsString());
                }
                if (obj.has("newMapId") && !obj.get("newMapId").isJsonNull()) {
                    moveCmd.setNewMapId(obj.get("newMapId").getAsString());
                }
                if (obj.has("newX")) {
                    moveCmd.setNewX(obj.get("newX").getAsInt());
                }
                if (obj.has("newY")) {
                    moveCmd.setNewY(obj.get("newY").getAsInt());
                }
                if (obj.has("newUnderId") && !obj.get("newUnderId").isJsonNull()) {
                    moveCmd.setNewUnderId(obj.get("newUnderId").getAsString());
                }
                if (obj.has("oldMapId") && !obj.get("oldMapId").isJsonNull()) {
                    moveCmd.setOldMapId(obj.get("oldMapId").getAsString());
                }
                if (obj.has("oldX")) {
                    moveCmd.setOldX(obj.get("oldX").getAsInt());
                }
                if (obj.has("oldY")) {
                    moveCmd.setOldY(obj.get("oldY").getAsInt());
                }
                if (obj.has("oldUnderId") && !obj.get("oldUnderId").isJsonNull()) {
                    moveCmd.setOldUnderId(obj.get("oldUnderId").getAsString());
                }
                if (obj.has("playerId") && !obj.get("playerId").isJsonNull()) {
                    moveCmd.setPlayerId(obj.get("playerId").getAsString());
                }
                return moveCmd;

            case BEGIN_SAVE:
            case END_SAVE:
            case PLAY_AUDIO:
            case UNKNOWN:
            default:
                CommandData cmd = new CommandData(type, rawCommand);
                return cmd;
        }
    }
}
