package org.vassalengine.tools.vsav.model;

/**
 * Command to update a chess clock timer.
 * Format: CLOCK\t{who}\t{name}\t{elapsed}\t{verified}\t{ticking}\t{restore}
 */
public class ClockCommand extends CommandData {
    private String who;
    private String name;
    private long elapsed;
    private long verified;
    private boolean ticking;
    private boolean restore;

    public ClockCommand() {
        setType(CommandType.CLOCK);
    }

    public ClockCommand(String rawCommand) {
        super(CommandType.CLOCK, rawCommand);
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    public long getVerified() {
        return verified;
    }

    public void setVerified(long verified) {
        this.verified = verified;
    }

    public boolean isTicking() {
        return ticking;
    }

    public void setTicking(boolean ticking) {
        this.ticking = ticking;
    }

    public boolean isRestore() {
        return restore;
    }

    public void setRestore(boolean restore) {
        this.restore = restore;
    }
}
