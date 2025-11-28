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

    // Command prefixes (slash-separated)
    private static final String ADD_PREFIX = "+/";
    private static final String REMOVE_PREFIX = "-/";
    private static final String CHANGE_PREFIX = "D/";
    private static final String MOVE_PREFIX = "M/";

    // Command prefixes (tab-separated)
    private static final String MUTABLE_PROPERTY_PREFIX = "MutableProperty\t";
    private static final String GLOBAL_PROPERTY_PREFIX = "GlobalProperty\t";
    private static final String TURN_PREFIX = "TURN";
    private static final String PLAYER_PREFIX = "PLAYER\t";
    private static final String PLAYER_REMOVE_PREFIX = "PYREMOVE\t";
    private static final String FLARE_PREFIX = "FLARE\t";
    private static final String CLOCK_PREFIX = "CLOCK\t";
    private static final String CLOCK_CONTROL_PREFIX = "CLOCKCONTROL\t";
    private static final String SETUP_STACK_PREFIX = "SETUP_STACK\t";

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
            case BEGIN_LOG:
                return "begin_log";
            case END_LOG:
                return "end_log";
            case MUTABLE_PROPERTY:
                if (command.getRawCommand() != null && !command.getRawCommand().isEmpty()) {
                    return command.getRawCommand();
                }
                return encodeMutableProperty((MutablePropertyCommand) command);
            case GLOBAL_PROPERTY:
                if (command.getRawCommand() != null && !command.getRawCommand().isEmpty()) {
                    return command.getRawCommand();
                }
                return encodeGlobalProperty((GlobalPropertyCommand) command);
            case TURN:
                if (command.getRawCommand() != null && !command.getRawCommand().isEmpty()) {
                    return command.getRawCommand();
                }
                return encodeTurn((TurnCommand) command);
            case PLAYER:
                if (command.getRawCommand() != null && !command.getRawCommand().isEmpty()) {
                    return command.getRawCommand();
                }
                return encodePlayer((PlayerCommand) command);
            case PLAYER_REMOVE:
                if (command.getRawCommand() != null && !command.getRawCommand().isEmpty()) {
                    return command.getRawCommand();
                }
                return encodePlayerRemove((PlayerRemoveCommand) command);
            case FLARE:
                if (command.getRawCommand() != null && !command.getRawCommand().isEmpty()) {
                    return command.getRawCommand();
                }
                return encodeFlare((FlareCommand) command);
            case CLOCK:
                if (command.getRawCommand() != null && !command.getRawCommand().isEmpty()) {
                    return command.getRawCommand();
                }
                return encodeClock((ClockCommand) command);
            case CLOCK_CONTROL:
                if (command.getRawCommand() != null && !command.getRawCommand().isEmpty()) {
                    return command.getRawCommand();
                }
                return encodeClockControl((ClockControlCommand) command);
            case SETUP_STACK:
                if (command.getRawCommand() != null && !command.getRawCommand().isEmpty()) {
                    return command.getRawCommand();
                }
                return encodeSetupStack((SetupStackCommand) command);
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

    /**
     * Null-safe string - returns empty string if null.
     */
    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * Encode MutableProperty command: MutableProperty\t{key}\t{oldVal}\t{newVal}\t{containerId}
     */
    private static String encodeMutableProperty(MutablePropertyCommand cmd) {
        StringBuilder sb = new StringBuilder();
        sb.append(MUTABLE_PROPERTY_PREFIX);
        sb.append(nullToEmpty(cmd.getKey()));
        sb.append('\t');
        sb.append(nullToEmpty(cmd.getOldValue()));
        sb.append('\t');
        sb.append(nullToEmpty(cmd.getNewValue()));
        sb.append('\t');
        sb.append(nullToEmpty(cmd.getContainerId()));
        return sb.toString();
    }

    /**
     * Encode GlobalProperty command: GlobalProperty\t{propId};{newVal};{containerId}
     */
    private static String encodeGlobalProperty(GlobalPropertyCommand cmd) {
        StringBuilder sb = new StringBuilder();
        sb.append(GLOBAL_PROPERTY_PREFIX);
        sb.append(nullToEmpty(cmd.getPropertyId()));
        sb.append(';');
        sb.append(nullToEmpty(cmd.getNewValue()));
        sb.append(';');
        sb.append(nullToEmpty(cmd.getContainerId()));
        return sb.toString();
    }

    /**
     * Encode Turn command: TURN{trackerId}\t{newState}
     */
    private static String encodeTurn(TurnCommand cmd) {
        StringBuilder sb = new StringBuilder();
        sb.append(TURN_PREFIX);
        sb.append(nullToEmpty(cmd.getTrackerId()));
        sb.append('\t');
        sb.append(nullToEmpty(cmd.getNewState()));
        return sb.toString();
    }

    /**
     * Encode Player command: PLAYER\t{playerId}\t{playerName}\t{side}
     */
    private static String encodePlayer(PlayerCommand cmd) {
        StringBuilder sb = new StringBuilder();
        sb.append(PLAYER_PREFIX);
        sb.append(nullToEmpty(cmd.getPlayerId()));
        sb.append('\t');
        sb.append(nullToEmpty(cmd.getPlayerName()));
        sb.append('\t');
        sb.append(nullToEmpty(cmd.getSide()));
        return sb.toString();
    }

    /**
     * Encode PlayerRemove command: PYREMOVE\t{playerId}
     */
    private static String encodePlayerRemove(PlayerRemoveCommand cmd) {
        StringBuilder sb = new StringBuilder();
        sb.append(PLAYER_REMOVE_PREFIX);
        sb.append(nullToEmpty(cmd.getPlayerId()));
        return sb.toString();
    }

    /**
     * Encode Flare command: FLARE\t{flareId}\t{x}\t{y}
     */
    private static String encodeFlare(FlareCommand cmd) {
        StringBuilder sb = new StringBuilder();
        sb.append(FLARE_PREFIX);
        sb.append(nullToEmpty(cmd.getFlareId()));
        sb.append('\t');
        sb.append(cmd.getX());
        sb.append('\t');
        sb.append(cmd.getY());
        return sb.toString();
    }

    /**
     * Encode Clock command: CLOCK\t{who}\t{name}\t{elapsed}\t{verified}\t{ticking}\t{restore}
     */
    private static String encodeClock(ClockCommand cmd) {
        StringBuilder sb = new StringBuilder();
        sb.append(CLOCK_PREFIX);
        sb.append(nullToEmpty(cmd.getWho()));
        sb.append('\t');
        sb.append(nullToEmpty(cmd.getName()));
        sb.append('\t');
        sb.append(cmd.getElapsed());
        sb.append('\t');
        sb.append(cmd.getVerified());
        sb.append('\t');
        sb.append(cmd.isTicking());
        sb.append('\t');
        sb.append(cmd.isRestore());
        return sb.toString();
    }

    /**
     * Encode ClockControl command: CLOCKCONTROL\t{showing}\t{online}
     */
    private static String encodeClockControl(ClockControlCommand cmd) {
        StringBuilder sb = new StringBuilder();
        sb.append(CLOCK_CONTROL_PREFIX);
        sb.append(cmd.isShowing());
        sb.append('\t');
        sb.append(cmd.isOnline());
        return sb.toString();
    }

    /**
     * Encode SetupStack command: SETUP_STACK\t{content}
     */
    private static String encodeSetupStack(SetupStackCommand cmd) {
        StringBuilder sb = new StringBuilder();
        sb.append(SETUP_STACK_PREFIX);
        sb.append(nullToEmpty(cmd.getContent()));
        return sb.toString();
    }
}
