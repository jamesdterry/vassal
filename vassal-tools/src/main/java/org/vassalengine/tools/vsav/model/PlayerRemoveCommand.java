package org.vassalengine.tools.vsav.model;

/**
 * Command to remove a player from the roster.
 * Format: PYREMOVE\t{playerId}
 */
public class PlayerRemoveCommand extends CommandData {
    private String playerId;

    public PlayerRemoveCommand() {
        setType(CommandType.PLAYER_REMOVE);
    }

    public PlayerRemoveCommand(String rawCommand) {
        super(CommandType.PLAYER_REMOVE, rawCommand);
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
