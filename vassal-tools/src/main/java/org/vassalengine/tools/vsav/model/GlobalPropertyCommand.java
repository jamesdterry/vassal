package org.vassalengine.tools.vsav.model;

/**
 * Command to change a global property.
 * Format: GlobalProperty\t{propertyId};{newValue};{containerId}
 */
public class GlobalPropertyCommand extends CommandData {
    private String propertyId;
    private String newValue;
    private String containerId;

    public GlobalPropertyCommand() {
        setType(CommandType.GLOBAL_PROPERTY);
    }

    public GlobalPropertyCommand(String rawCommand) {
        super(CommandType.GLOBAL_PROPERTY, rawCommand);
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
}
