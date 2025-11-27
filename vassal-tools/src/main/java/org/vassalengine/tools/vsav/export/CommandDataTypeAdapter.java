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
        } else if (src instanceof MutablePropertyCommand) {
            MutablePropertyCommand cmd = (MutablePropertyCommand) src;
            result.addProperty("key", cmd.getKey());
            result.addProperty("oldValue", cmd.getOldValue());
            result.addProperty("newValue", cmd.getNewValue());
            result.addProperty("containerId", cmd.getContainerId());
        } else if (src instanceof GlobalPropertyCommand) {
            GlobalPropertyCommand cmd = (GlobalPropertyCommand) src;
            result.addProperty("propertyId", cmd.getPropertyId());
            result.addProperty("newValue", cmd.getNewValue());
            result.addProperty("containerId", cmd.getContainerId());
        } else if (src instanceof TurnCommand) {
            TurnCommand cmd = (TurnCommand) src;
            result.addProperty("trackerId", cmd.getTrackerId());
            result.addProperty("newState", cmd.getNewState());
        } else if (src instanceof PlayerCommand) {
            PlayerCommand cmd = (PlayerCommand) src;
            result.addProperty("playerId", cmd.getPlayerId());
            result.addProperty("playerName", cmd.getPlayerName());
            result.addProperty("side", cmd.getSide());
        } else if (src instanceof PlayerRemoveCommand) {
            PlayerRemoveCommand cmd = (PlayerRemoveCommand) src;
            result.addProperty("playerId", cmd.getPlayerId());
        } else if (src instanceof FlareCommand) {
            FlareCommand cmd = (FlareCommand) src;
            result.addProperty("flareId", cmd.getFlareId());
            result.addProperty("x", cmd.getX());
            result.addProperty("y", cmd.getY());
        } else if (src instanceof ClockCommand) {
            ClockCommand cmd = (ClockCommand) src;
            result.addProperty("who", cmd.getWho());
            result.addProperty("name", cmd.getName());
            result.addProperty("elapsed", cmd.getElapsed());
            result.addProperty("verified", cmd.getVerified());
            result.addProperty("ticking", cmd.isTicking());
            result.addProperty("restore", cmd.isRestore());
        } else if (src instanceof ClockControlCommand) {
            ClockControlCommand cmd = (ClockControlCommand) src;
            result.addProperty("showing", cmd.isShowing());
            result.addProperty("online", cmd.isOnline());
        } else if (src instanceof SetupStackCommand) {
            SetupStackCommand cmd = (SetupStackCommand) src;
            result.addProperty("content", cmd.getContent());
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

            case MUTABLE_PROPERTY:
                MutablePropertyCommand mutableCmd = new MutablePropertyCommand(rawCommand);
                if (obj.has("key") && !obj.get("key").isJsonNull()) {
                    mutableCmd.setKey(obj.get("key").getAsString());
                }
                if (obj.has("oldValue") && !obj.get("oldValue").isJsonNull()) {
                    mutableCmd.setOldValue(obj.get("oldValue").getAsString());
                }
                if (obj.has("newValue") && !obj.get("newValue").isJsonNull()) {
                    mutableCmd.setNewValue(obj.get("newValue").getAsString());
                }
                if (obj.has("containerId") && !obj.get("containerId").isJsonNull()) {
                    mutableCmd.setContainerId(obj.get("containerId").getAsString());
                }
                return mutableCmd;

            case GLOBAL_PROPERTY:
                GlobalPropertyCommand globalCmd = new GlobalPropertyCommand(rawCommand);
                if (obj.has("propertyId") && !obj.get("propertyId").isJsonNull()) {
                    globalCmd.setPropertyId(obj.get("propertyId").getAsString());
                }
                if (obj.has("newValue") && !obj.get("newValue").isJsonNull()) {
                    globalCmd.setNewValue(obj.get("newValue").getAsString());
                }
                if (obj.has("containerId") && !obj.get("containerId").isJsonNull()) {
                    globalCmd.setContainerId(obj.get("containerId").getAsString());
                }
                return globalCmd;

            case TURN:
                TurnCommand turnCmd = new TurnCommand(rawCommand);
                if (obj.has("trackerId") && !obj.get("trackerId").isJsonNull()) {
                    turnCmd.setTrackerId(obj.get("trackerId").getAsString());
                }
                if (obj.has("newState") && !obj.get("newState").isJsonNull()) {
                    turnCmd.setNewState(obj.get("newState").getAsString());
                }
                return turnCmd;

            case PLAYER:
                PlayerCommand playerCmd = new PlayerCommand(rawCommand);
                if (obj.has("playerId") && !obj.get("playerId").isJsonNull()) {
                    playerCmd.setPlayerId(obj.get("playerId").getAsString());
                }
                if (obj.has("playerName") && !obj.get("playerName").isJsonNull()) {
                    playerCmd.setPlayerName(obj.get("playerName").getAsString());
                }
                if (obj.has("side") && !obj.get("side").isJsonNull()) {
                    playerCmd.setSide(obj.get("side").getAsString());
                }
                return playerCmd;

            case PLAYER_REMOVE:
                PlayerRemoveCommand playerRemoveCmd = new PlayerRemoveCommand(rawCommand);
                if (obj.has("playerId") && !obj.get("playerId").isJsonNull()) {
                    playerRemoveCmd.setPlayerId(obj.get("playerId").getAsString());
                }
                return playerRemoveCmd;

            case FLARE:
                FlareCommand flareCmd = new FlareCommand(rawCommand);
                if (obj.has("flareId") && !obj.get("flareId").isJsonNull()) {
                    flareCmd.setFlareId(obj.get("flareId").getAsString());
                }
                if (obj.has("x")) {
                    flareCmd.setX(obj.get("x").getAsInt());
                }
                if (obj.has("y")) {
                    flareCmd.setY(obj.get("y").getAsInt());
                }
                return flareCmd;

            case CLOCK:
                ClockCommand clockCmd = new ClockCommand(rawCommand);
                if (obj.has("who") && !obj.get("who").isJsonNull()) {
                    clockCmd.setWho(obj.get("who").getAsString());
                }
                if (obj.has("name") && !obj.get("name").isJsonNull()) {
                    clockCmd.setName(obj.get("name").getAsString());
                }
                if (obj.has("elapsed")) {
                    clockCmd.setElapsed(obj.get("elapsed").getAsLong());
                }
                if (obj.has("verified")) {
                    clockCmd.setVerified(obj.get("verified").getAsLong());
                }
                if (obj.has("ticking")) {
                    clockCmd.setTicking(obj.get("ticking").getAsBoolean());
                }
                if (obj.has("restore")) {
                    clockCmd.setRestore(obj.get("restore").getAsBoolean());
                }
                return clockCmd;

            case CLOCK_CONTROL:
                ClockControlCommand clockControlCmd = new ClockControlCommand(rawCommand);
                if (obj.has("showing")) {
                    clockControlCmd.setShowing(obj.get("showing").getAsBoolean());
                }
                if (obj.has("online")) {
                    clockControlCmd.setOnline(obj.get("online").getAsBoolean());
                }
                return clockControlCmd;

            case SETUP_STACK:
                SetupStackCommand setupStackCmd = new SetupStackCommand(rawCommand);
                if (obj.has("content") && !obj.get("content").isJsonNull()) {
                    setupStackCmd.setContent(obj.get("content").getAsString());
                }
                return setupStackCmd;

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
