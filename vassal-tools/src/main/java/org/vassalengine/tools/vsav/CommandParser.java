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

        // MutableProperty: MutableProperty\t{key}\t{oldVal}\t{newVal}\t{containerId}
        if (command.startsWith(MUTABLE_PROPERTY_PREFIX)) {
            return parseMutableProperty(command);
        }

        // GlobalProperty: GlobalProperty\t{propId};{newVal};{containerId}
        if (command.startsWith(GLOBAL_PROPERTY_PREFIX)) {
            return parseGlobalProperty(command);
        }

        // Turn: TURN{id}\t{newState}
        if (command.startsWith(TURN_PREFIX)) {
            return parseTurn(command);
        }

        // Player: PLAYER\t{id}\t{name}\t{side}
        if (command.startsWith(PLAYER_PREFIX)) {
            return parsePlayer(command);
        }

        // PlayerRemove: PYREMOVE\t{id}
        if (command.startsWith(PLAYER_REMOVE_PREFIX)) {
            return parsePlayerRemove(command);
        }

        // Flare: FLARE\t{id}\t{x}\t{y}
        if (command.startsWith(FLARE_PREFIX)) {
            return parseFlare(command);
        }

        // ClockControl: CLOCKCONTROL\t{showing}\t{online}
        // Must check before CLOCK since CLOCK is a prefix of CLOCKCONTROL
        if (command.startsWith(CLOCK_CONTROL_PREFIX)) {
            return parseClockControl(command);
        }

        // Clock: CLOCK\t{who}\t{name}\t{elapsed}\t{verified}\t{ticking}\t{restore}
        if (command.startsWith(CLOCK_PREFIX)) {
            return parseClock(command);
        }

        // SetupStack: SETUP_STACK\t{content}
        if (command.startsWith(SETUP_STACK_PREFIX)) {
            return parseSetupStack(command);
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

    /**
     * Parse long, defaulting to 0 on error.
     */
    private static long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Parse boolean from string.
     */
    private static boolean parseBoolean(String s) {
        return "true".equalsIgnoreCase(s);
    }

    /**
     * Split by tab character.
     */
    private static String[] splitByTab(String s) {
        return s.split("\t", -1);
    }

    /**
     * Parse MutableProperty command: MutableProperty\t{key}\t{oldVal}\t{newVal}\t{containerId}
     */
    private static MutablePropertyCommand parseMutableProperty(String command) {
        String content = command.substring(MUTABLE_PROPERTY_PREFIX.length());
        String[] parts = splitByTab(content);

        MutablePropertyCommand cmd = new MutablePropertyCommand(command);
        if (parts.length > 0) cmd.setKey(parts[0]);
        if (parts.length > 1) cmd.setOldValue(parts[1]);
        if (parts.length > 2) cmd.setNewValue(parts[2]);
        if (parts.length > 3) cmd.setContainerId(parts[3]);

        return cmd;
    }

    /**
     * Parse GlobalProperty command: GlobalProperty\t{propId};{newVal};{containerId}
     * Note: Uses semicolons within the tab-separated content.
     */
    private static GlobalPropertyCommand parseGlobalProperty(String command) {
        String content = command.substring(GLOBAL_PROPERTY_PREFIX.length());
        String[] parts = content.split(";", -1);

        GlobalPropertyCommand cmd = new GlobalPropertyCommand(command);
        if (parts.length > 0) cmd.setPropertyId(parts[0]);
        if (parts.length > 1) cmd.setNewValue(parts[1]);
        if (parts.length > 2) cmd.setContainerId(parts[2]);

        return cmd;
    }

    /**
     * Parse Turn command: TURN{trackerId}\t{newState}
     */
    private static TurnCommand parseTurn(String command) {
        String content = command.substring(TURN_PREFIX.length());
        String[] parts = splitByTab(content);

        TurnCommand cmd = new TurnCommand(command);
        if (parts.length > 0) cmd.setTrackerId(parts[0]);
        if (parts.length > 1) cmd.setNewState(parts[1]);

        return cmd;
    }

    /**
     * Parse Player command: PLAYER\t{playerId}\t{playerName}\t{side}
     */
    private static PlayerCommand parsePlayer(String command) {
        String content = command.substring(PLAYER_PREFIX.length());
        String[] parts = splitByTab(content);

        PlayerCommand cmd = new PlayerCommand(command);
        if (parts.length > 0) cmd.setPlayerId(parts[0]);
        if (parts.length > 1) cmd.setPlayerName(parts[1]);
        if (parts.length > 2) cmd.setSide(parts[2]);

        return cmd;
    }

    /**
     * Parse PlayerRemove command: PYREMOVE\t{playerId}
     */
    private static PlayerRemoveCommand parsePlayerRemove(String command) {
        String content = command.substring(PLAYER_REMOVE_PREFIX.length());
        String[] parts = splitByTab(content);

        PlayerRemoveCommand cmd = new PlayerRemoveCommand(command);
        if (parts.length > 0) cmd.setPlayerId(parts[0]);

        return cmd;
    }

    /**
     * Parse Flare command: FLARE\t{flareId}\t{x}\t{y}
     */
    private static FlareCommand parseFlare(String command) {
        String content = command.substring(FLARE_PREFIX.length());
        String[] parts = splitByTab(content);

        FlareCommand cmd = new FlareCommand(command);
        if (parts.length > 0) cmd.setFlareId(parts[0]);
        if (parts.length > 1) cmd.setX(parseInt(parts[1]));
        if (parts.length > 2) cmd.setY(parseInt(parts[2]));

        return cmd;
    }

    /**
     * Parse Clock command: CLOCK\t{who}\t{name}\t{elapsed}\t{verified}\t{ticking}\t{restore}
     */
    private static ClockCommand parseClock(String command) {
        String content = command.substring(CLOCK_PREFIX.length());
        String[] parts = splitByTab(content);

        ClockCommand cmd = new ClockCommand(command);
        if (parts.length > 0) cmd.setWho(parts[0]);
        if (parts.length > 1) cmd.setName(parts[1]);
        if (parts.length > 2) cmd.setElapsed(parseLong(parts[2]));
        if (parts.length > 3) cmd.setVerified(parseLong(parts[3]));
        if (parts.length > 4) cmd.setTicking(parseBoolean(parts[4]));
        if (parts.length > 5) cmd.setRestore(parseBoolean(parts[5]));

        return cmd;
    }

    /**
     * Parse ClockControl command: CLOCKCONTROL\t{showing}\t{online}
     */
    private static ClockControlCommand parseClockControl(String command) {
        String content = command.substring(CLOCK_CONTROL_PREFIX.length());
        String[] parts = splitByTab(content);

        ClockControlCommand cmd = new ClockControlCommand(command);
        if (parts.length > 0) cmd.setShowing(parseBoolean(parts[0]));
        if (parts.length > 1) cmd.setOnline(parseBoolean(parts[1]));

        return cmd;
    }

    /**
     * Parse SetupStack command: SETUP_STACK\t{content}
     */
    private static SetupStackCommand parseSetupStack(String command) {
        String content = command.substring(SETUP_STACK_PREFIX.length());

        SetupStackCommand cmd = new SetupStackCommand(command);
        cmd.setContent(content);

        return cmd;
    }
}
