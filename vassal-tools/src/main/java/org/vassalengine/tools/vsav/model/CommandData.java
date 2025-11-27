package org.vassalengine.tools.vsav.model;

/**
 * Base class for all command types in a save file.
 * Commands represent actions like adding pieces, moving pieces, etc.
 */
public class CommandData {

    public enum CommandType {
        ADD_PIECE,      // +/{id}/{type}/{state}
        REMOVE_PIECE,   // -/{id}
        CHANGE_PIECE,   // D/{id}/{newState}[/{oldState}]
        MOVE_PIECE,     // M/{id}/{mapId}/{x}/{y}/...
        BEGIN_SAVE,     // begin_save
        END_SAVE,       // end_save
        PLAY_AUDIO,     // !{clip}
        UNKNOWN         // Unrecognized command
    }

    private CommandType type;
    private String rawCommand;  // Original command string for round-trip fidelity

    public CommandData() {
    }

    public CommandData(CommandType type, String rawCommand) {
        this.type = type;
        this.rawCommand = rawCommand;
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    public String getRawCommand() {
        return rawCommand;
    }

    public void setRawCommand(String rawCommand) {
        this.rawCommand = rawCommand;
    }
}
