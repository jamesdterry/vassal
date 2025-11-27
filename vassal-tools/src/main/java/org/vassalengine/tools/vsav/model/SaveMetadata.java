package org.vassalengine.tools.vsav.model;

/**
 * Metadata from the savedata XML entry in a .vsav file.
 */
public class SaveMetadata {
    private String version;
    private String description;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
