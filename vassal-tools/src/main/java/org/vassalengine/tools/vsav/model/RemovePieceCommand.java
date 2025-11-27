package org.vassalengine.tools.vsav.model;

/**
 * Command to remove a piece from the game.
 * Format: -/{id}
 */
public class RemovePieceCommand extends CommandData {
    private String pieceId;

    public RemovePieceCommand() {
        setType(CommandType.REMOVE_PIECE);
    }

    public RemovePieceCommand(String rawCommand, String pieceId) {
        super(CommandType.REMOVE_PIECE, rawCommand);
        this.pieceId = pieceId;
    }

    public String getPieceId() {
        return pieceId;
    }

    public void setPieceId(String pieceId) {
        this.pieceId = pieceId;
    }
}
