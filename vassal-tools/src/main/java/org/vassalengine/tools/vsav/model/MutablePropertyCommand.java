package org.vassalengine.tools.vsav.model;

/**
 * Command to change a mutable property.
 * Format: MutableProperty\t{key}\t{oldValue}\t{newValue}\t{containerId}
 */
public class MutablePropertyCommand extends CommandData {
    private String key;
    private String oldValue;
    private String newValue;
    private String containerId;

    public MutablePropertyCommand() {
        setType(CommandType.MUTABLE_PROPERTY);
    }

    public MutablePropertyCommand(String rawCommand) {
        super(CommandType.MUTABLE_PROPERTY, rawCommand);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
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
