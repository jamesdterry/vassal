package org.vassalengine.tools.vsav;

import org.vassalengine.tools.vsav.model.*;

import java.util.List;

/**
 * Encodes CommandData objects back into command strings.
 */
public class CommandEncoder {

    // Command separator is ESC character (0x1B)
    private static final char COMMAND_SEPARATOR = 0x1B;

    // Parameter separator within commands
    private static final char PARAM_SEPARATOR = '/';

    // Command prefixes
    private static final String ADD_PREFIX = "+/";
    private static final String REMOVE_PREFIX = "-/";
    private static final String CHANGE_PREFIX = "D/";
    private static final String MOVE_PREFIX = "M/";

    /**
     * Encode a list of commands into a saved game data string.
     */
    public static String encodeCommands(List<CommandData> commands) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < commands.size(); i++) {
            if (i > 0) {
                sb.append(COMMAND_SEPARATOR);
            }
            sb.append(encodeCommand(commands.get(i)));
        }

        return sb.toString();
    }

    /**
     * Encode a single command back to its string form.
     * For ADD_PIECE, we always encode from piece data so trait modifications take effect.
     * For other commands, we use rawCommand if available for round-trip fidelity.
     */
    public static String encodeCommand(CommandData command) {
        switch (command.getType()) {
            case ADD_PIECE:
                // Always encode from piece data so trait modifications take effect
                return encodeAddPiece((AddPieceCommand) command);
            case REMOVE_PIECE:
                if (command.getRawCommand() != null && !command.getRawCommand().isEmpty()) {
                    return command.getRawCommand();
                }
                return encodeRemovePiece((RemovePieceCommand) command);
            case CHANGE_PIECE:
                if (command.getRawCommand() != null && !command.getRawCommand().isEmpty()) {
                    return command.getRawCommand();
                }
                return encodeChangePiece((ChangePieceCommand) command);
            case MOVE_PIECE:
                if (command.getRawCommand() != null && !command.getRawCommand().isEmpty()) {
                    return command.getRawCommand();
                }
                return encodeMovePiece((MovePieceCommand) command);
            case BEGIN_SAVE:
                return "begin_save";
            case END_SAVE:
                return "end_save";
            case PLAY_AUDIO:
            case UNKNOWN:
            default:
                // For unknown commands, rawCommand should be present
                return command.getRawCommand() != null ? command.getRawCommand() : "";
        }
    }

    /**
     * Encode AddPiece command: +/{id}/{type}/{state}
     *
     * Uses SequenceEncoder to properly escape / characters in id, type, and state.
     */
    private static String encodeAddPiece(AddPieceCommand command) {
        PieceData piece = command.getPiece();
        if (piece == null) {
            return command.getRawCommand();
        }

        // If traits have been modified, encode them back to type/state strings
        if (piece.getTraits() != null && !piece.getTraits().isEmpty()) {
            piece.encodeTraits();
        }

        // Use SequenceEncoder with / as separator - this properly escapes / characters
        VASSAL.tools.SequenceEncoder se = new VASSAL.tools.SequenceEncoder(PARAM_SEPARATOR);
        se.append(wrapNull(piece.getId()));
        se.append(piece.getType() != null ? piece.getType() : "");
        se.append(piece.getState() != null ? piece.getState() : "");

        return ADD_PREFIX + se.getValue();
    }

    /**
     * Encode RemovePiece command: -/{id}
     */
    private static String encodeRemovePiece(RemovePieceCommand command) {
        return REMOVE_PREFIX + command.getPieceId();
    }

    /**
     * Encode ChangePiece command: D/{id}/{newState}[/{oldState}]
     */
    private static String encodeChangePiece(ChangePieceCommand command) {
        StringBuilder sb = new StringBuilder();
        sb.append(CHANGE_PREFIX);
        sb.append(command.getPieceId());
        sb.append(PARAM_SEPARATOR);
        sb.append(command.getNewState());

        if (command.getOldState() != null) {
            sb.append(PARAM_SEPARATOR);
            sb.append(command.getOldState());
        }

        return sb.toString();
    }

    /**
     * Encode MovePiece command: M/{id}/{mapId}/{x}/{y}/{underId}/{oldMapId}/{oldX}/{oldY}/{oldUnderId}/{playerId}
     */
    private static String encodeMovePiece(MovePieceCommand command) {
        StringBuilder sb = new StringBuilder();
        sb.append(MOVE_PREFIX);
        sb.append(wrapNull(command.getPieceId()));
        sb.append(PARAM_SEPARATOR);
        sb.append(wrapNull(command.getNewMapId()));
        sb.append(PARAM_SEPARATOR);
        sb.append(command.getNewX());
        sb.append(PARAM_SEPARATOR);
        sb.append(command.getNewY());
        sb.append(PARAM_SEPARATOR);
        sb.append(wrapNull(command.getNewUnderId()));
        sb.append(PARAM_SEPARATOR);
        sb.append(wrapNull(command.getOldMapId()));
        sb.append(PARAM_SEPARATOR);
        sb.append(command.getOldX());
        sb.append(PARAM_SEPARATOR);
        sb.append(command.getOldY());
        sb.append(PARAM_SEPARATOR);
        sb.append(wrapNull(command.getOldUnderId()));
        sb.append(PARAM_SEPARATOR);
        sb.append(command.getPlayerId() != null ? command.getPlayerId() : "");

        return sb.toString();
    }

    /**
     * Wrap null value as "null" string.
     */
    private static String wrapNull(String s) {
        return s == null ? "null" : s;
    }
}
