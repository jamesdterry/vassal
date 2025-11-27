package org.vassalengine.tools.vsav.model;

/**
 * Command to set turn tracker state.
 * Format: TURN{trackerId}\t{newState}
 */
public class TurnCommand extends CommandData {
    private String trackerId;
    private String newState;

    public TurnCommand() {
        setType(CommandType.TURN);
    }

    public TurnCommand(String rawCommand) {
        super(CommandType.TURN, rawCommand);
    }

    public String getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(String trackerId) {
        this.trackerId = trackerId;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }
}
