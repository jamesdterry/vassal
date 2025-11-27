package org.vassalengine.tools.vsav.model;

/**
 * Base class for all command types in a save file.
 * Commands represent actions like adding pieces, moving pieces, etc.
 */
public class CommandData {

    public enum CommandType {
        ADD_PIECE,          // +/{id}/{type}/{state}
        REMOVE_PIECE,       // -/{id}
        CHANGE_PIECE,       // D/{id}/{newState}[/{oldState}]
        MOVE_PIECE,         // M/{id}/{mapId}/{x}/{y}/...
        BEGIN_SAVE,         // begin_save
        END_SAVE,           // end_save
        PLAY_AUDIO,         // !{clip}
        // New command types for comprehensive VSAV support
        MUTABLE_PROPERTY,   // MutableProperty\t{key}\t{oldVal}\t{newVal}\t{containerId}
        GLOBAL_PROPERTY,    // GlobalProperty\t{propId};{newVal};{containerId}
        TURN,               // TURN{id}\t{newState}
        PLAYER,             // PLAYER\t{id}\t{name}\t{side}
        PLAYER_REMOVE,      // PYREMOVE\t{id}
        FLARE,              // FLARE\t{id}\t{x}\t{y}
        CLOCK,              // CLOCK\t{who}\t{name}\t{elapsed}\t{verified}\t{ticking}\t{restore}
        CLOCK_CONTROL,      // CLOCKCONTROL\t{showing}\t{online}
        SETUP_STACK,        // SETUP_STACK\t marker
        UNKNOWN             // Unrecognized command
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
