package org.vassalengine.tools.vsav.model;

/**
 * Command to update the chess clock control state.
 * Format: CLOCKCONTROL\t{showing}\t{online}
 */
public class ClockControlCommand extends CommandData {
    private boolean showing;
    private boolean online;

    public ClockControlCommand() {
        setType(CommandType.CLOCK_CONTROL);
    }

    public ClockControlCommand(String rawCommand) {
        super(CommandType.CLOCK_CONTROL, rawCommand);
    }

    public boolean isShowing() {
        return showing;
    }

    public void setShowing(boolean showing) {
        this.showing = showing;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
