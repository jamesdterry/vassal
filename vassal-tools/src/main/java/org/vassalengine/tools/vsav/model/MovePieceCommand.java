package org.vassalengine.tools.vsav.model;

/**
 * Command to move a piece on the map.
 * Format: M/{id}/{newMapId}/{newX}/{newY}/{newUnderId}/{oldMapId}/{oldX}/{oldY}/{oldUnderId}/{playerId}
 */
public class MovePieceCommand extends CommandData {
    private String pieceId;
    private String newMapId;
    private int newX;
    private int newY;
    private String newUnderId;
    private String oldMapId;
    private int oldX;
    private int oldY;
    private String oldUnderId;
    private String playerId;

    public MovePieceCommand() {
        setType(CommandType.MOVE_PIECE);
    }

    public MovePieceCommand(String rawCommand) {
        super(CommandType.MOVE_PIECE, rawCommand);
    }

    public String getPieceId() {
        return pieceId;
    }

    public void setPieceId(String pieceId) {
        this.pieceId = pieceId;
    }

    public String getNewMapId() {
        return newMapId;
    }

    public void setNewMapId(String newMapId) {
        this.newMapId = newMapId;
    }

    public int getNewX() {
        return newX;
    }

    public void setNewX(int newX) {
        this.newX = newX;
    }

    public int getNewY() {
        return newY;
    }

    public void setNewY(int newY) {
        this.newY = newY;
    }

    public String getNewUnderId() {
        return newUnderId;
    }

    public void setNewUnderId(String newUnderId) {
        this.newUnderId = newUnderId;
    }

    public String getOldMapId() {
        return oldMapId;
    }

    public void setOldMapId(String oldMapId) {
        this.oldMapId = oldMapId;
    }

    public int getOldX() {
        return oldX;
    }

    public void setOldX(int oldX) {
        this.oldX = oldX;
    }

    public int getOldY() {
        return oldY;
    }

    public void setOldY(int oldY) {
        this.oldY = oldY;
    }

    public String getOldUnderId() {
        return oldUnderId;
    }

    public void setOldUnderId(String oldUnderId) {
        this.oldUnderId = oldUnderId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
