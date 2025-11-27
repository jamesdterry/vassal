package org.vassalengine.tools.vsav.model;

/**
 * Command to add a new piece to the game.
 * Format: +/{id}/{type}/{state}
 */
public class AddPieceCommand extends CommandData {
    private PieceData piece;

    public AddPieceCommand() {
        setType(CommandType.ADD_PIECE);
    }

    public AddPieceCommand(String rawCommand, PieceData piece) {
        super(CommandType.ADD_PIECE, rawCommand);
        this.piece = piece;
    }

    public PieceData getPiece() {
        return piece;
    }

    public void setPiece(PieceData piece) {
        this.piece = piece;
    }
}
