package org.vassalengine.tools.vsav.model;

/**
 * Command to show a flare on the map.
 * Format: FLARE\t{flareId}\t{x}\t{y}
 */
public class FlareCommand extends CommandData {
    private String flareId;
    private int x;
    private int y;

    public FlareCommand() {
        setType(CommandType.FLARE);
    }

    public FlareCommand(String rawCommand) {
        super(CommandType.FLARE, rawCommand);
    }

    public String getFlareId() {
        return flareId;
    }

    public void setFlareId(String flareId) {
        this.flareId = flareId;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
