package org.vassalengine.tools.vsav;

import org.vassalengine.tools.vsav.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses command strings from saved game data into CommandData objects.
 */
public class CommandParser {

    // Command separator is ESC character (0x1B)
    private static final char COMMAND_SEPARATOR = 0x1B;

    // Parameter separator within commands
    private static final char PARAM_SEPARATOR = '/';

    // Command prefixes
    private static final String ADD_PREFIX = "+/";
    private static final String REMOVE_PREFIX = "-/";
    private static final String CHANGE_PREFIX = "D/";
    private static final String MOVE_PREFIX = "M/";

    // Special commands
    private static final String BEGIN_SAVE = "begin_save";
    private static final String END_SAVE = "end_save";

    /**
     * Parse a full saved game data string into a list of commands.
     */
    public static List<CommandData> parseCommands(String gameData) {
        List<CommandData> commands = new ArrayList<>();

        StringBuilder current = new StringBuilder();
        for (char c : gameData.toCharArray()) {
            if (c == COMMAND_SEPARATOR) {
                if (current.length() > 0) {
                    CommandData cmd = parseCommand(current.toString());
                    if (cmd != null) {
                        commands.add(cmd);
                    }
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }

        // Handle final command
        if (current.length() > 0) {
            CommandData cmd = parseCommand(current.toString());
            if (cmd != null) {
                commands.add(cmd);
            }
        }

        return commands;
    }

    /**
     * Parse a single command string.
     */
    public static CommandData parseCommand(String command) {
        if (command == null || command.isEmpty()) {
            return null;
        }

        // Special commands
        if (BEGIN_SAVE.equals(command)) {
            return new CommandData(CommandData.CommandType.BEGIN_SAVE, command);
        }
        if (END_SAVE.equals(command)) {
            return new CommandData(CommandData.CommandType.END_SAVE, command);
        }

        // AddPiece: +/{id}/{type}/{state}
        if (command.startsWith(ADD_PREFIX)) {
            return parseAddPiece(command);
        }

        // RemovePiece: -/{id}
        if (command.startsWith(REMOVE_PREFIX)) {
            return parseRemovePiece(command);
        }

        // ChangePiece: D/{id}/{newState}[/{oldState}]
        if (command.startsWith(CHANGE_PREFIX)) {
            return parseChangePiece(command);
        }

        // MovePiece: M/{id}/{mapId}/{x}/{y}/...
        if (command.startsWith(MOVE_PREFIX)) {
            return parseMovePiece(command);
        }

        // PlayAudioClip: !{clip}
        if (command.startsWith("!")) {
            CommandData cmd = new CommandData(CommandData.CommandType.PLAY_AUDIO, command);
            return cmd;
        }

        // Unknown command - preserve raw for round-trip
        return new CommandData(CommandData.CommandType.UNKNOWN, command);
    }

    /**
     * Parse AddPiece command: +/{id}/{type}/{state}
     *
     * Uses SequenceEncoder.Decoder to properly handle escaped / characters.
     * VASSAL's format escapes / characters within id, type, and state using backslash.
     */
    private static AddPieceCommand parseAddPiece(String command) {
        String content = command.substring(ADD_PREFIX.length());

        // Use SequenceEncoder.Decoder with / as separator - this properly handles escaped / characters
        VASSAL.tools.SequenceEncoder.Decoder st = new VASSAL.tools.SequenceEncoder.Decoder(content, PARAM_SEPARATOR);

        String id = st.hasMoreTokens() ? st.nextToken() : null;
        String type = st.hasMoreTokens() ? st.nextToken() : "";
        String state = st.hasMoreTokens() ? st.nextToken() : "";

        PieceData piece = new PieceData(
            unwrapNull(id),
            type,
            state
        );

        // Parse traits for structured access
        piece.parseTraits();

        return new AddPieceCommand(command, piece);
    }

    /**
     * Parse RemovePiece command: -/{id}
     */
    private static RemovePieceCommand parseRemovePiece(String command) {
        String id = command.substring(REMOVE_PREFIX.length());
        return new RemovePieceCommand(command, id);
    }

    /**
     * Parse ChangePiece command: D/{id}/{newState}[/{oldState}]
     */
    private static ChangePieceCommand parseChangePiece(String command) {
        String content = command.substring(CHANGE_PREFIX.length());
        String[] parts = splitParams(content, 3);

        String id = parts.length > 0 ? parts[0] : "";
        String newState = parts.length > 1 ? parts[1] : "";
        String oldState = parts.length > 2 ? parts[2] : null;

        return new ChangePieceCommand(command, id, newState, oldState);
    }

    /**
     * Parse MovePiece command: M/{id}/{mapId}/{x}/{y}/{underId}/{oldMapId}/{oldX}/{oldY}/{oldUnderId}/{playerId}
     */
    private static MovePieceCommand parseMovePiece(String command) {
        String content = command.substring(MOVE_PREFIX.length());
        String[] parts = splitParams(content, 10);

        MovePieceCommand cmd = new MovePieceCommand(command);

        if (parts.length > 0) cmd.setPieceId(unwrapNull(parts[0]));
        if (parts.length > 1) cmd.setNewMapId(unwrapNull(parts[1]));
        if (parts.length > 2) cmd.setNewX(parseInt(parts[2]));
        if (parts.length > 3) cmd.setNewY(parseInt(parts[3]));
        if (parts.length > 4) cmd.setNewUnderId(unwrapNull(parts[4]));
        if (parts.length > 5) cmd.setOldMapId(unwrapNull(parts[5]));
        if (parts.length > 6) cmd.setOldX(parseInt(parts[6]));
        if (parts.length > 7) cmd.setOldY(parseInt(parts[7]));
        if (parts.length > 8) cmd.setOldUnderId(unwrapNull(parts[8]));
        if (parts.length > 9) cmd.setPlayerId(parts[9]);

        return cmd;
    }

    /**
     * Split command content by parameter separator, up to maxParts.
     * The last part will contain the remainder of the string.
     */
    private static String[] splitParams(String content, int maxParts) {
        List<String> parts = new ArrayList<>();
        int start = 0;

        for (int i = 0; i < content.length() && parts.size() < maxParts - 1; i++) {
            if (content.charAt(i) == PARAM_SEPARATOR) {
                parts.add(content.substring(start, i));
                start = i + 1;
            }
        }

        // Add the remainder
        if (start <= content.length()) {
            parts.add(content.substring(start));
        }

        return parts.toArray(new String[0]);
    }

    /**
     * Unwrap "null" string to actual null.
     */
    private static String unwrapNull(String s) {
        return "null".equals(s) ? null : s;
    }

    /**
     * Parse integer, defaulting to 0 on error.
     */
    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
