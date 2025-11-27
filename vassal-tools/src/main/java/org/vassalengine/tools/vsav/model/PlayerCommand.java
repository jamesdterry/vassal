package org.vassalengine.tools.vsav.model;

/**
 * Command to add a player to the roster.
 * Format: PLAYER\t{playerId}\t{playerName}\t{side}
 */
public class PlayerCommand extends CommandData {
    private String playerId;
    private String playerName;
    private String side;

    public PlayerCommand() {
        setType(CommandType.PLAYER);
    }

    public PlayerCommand(String rawCommand) {
        super(CommandType.PLAYER, rawCommand);
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }
}
