package org.vassalengine.tools.vsav.model;

/**
 * Command to change a piece's state.
 * Format: D/{id}/{newState}[/{oldState}]
 */
public class ChangePieceCommand extends CommandData {
    private String pieceId;
    private String newState;
    private String oldState;  // May be null

    public ChangePieceCommand() {
        setType(CommandType.CHANGE_PIECE);
    }

    public ChangePieceCommand(String rawCommand, String pieceId, String newState, String oldState) {
        super(CommandType.CHANGE_PIECE, rawCommand);
        this.pieceId = pieceId;
        this.newState = newState;
        this.oldState = oldState;
    }

    public String getPieceId() {
        return pieceId;
    }

    public void setPieceId(String pieceId) {
        this.pieceId = pieceId;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getOldState() {
        return oldState;
    }

    public void setOldState(String oldState) {
        this.oldState = oldState;
    }
}
