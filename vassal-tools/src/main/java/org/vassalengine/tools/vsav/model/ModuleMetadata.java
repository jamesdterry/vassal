package org.vassalengine.tools.vsav.model;

/**
 * Metadata from the moduledata XML entry in a .vsav file.
 */
public class ModuleMetadata {
    private String name;
    private String version;
    private String description;
    private String vassalVersion;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getVassalVersion() {
        return vassalVersion;
    }

    public void setVassalVersion(String vassalVersion) {
        this.vassalVersion = vassalVersion;
    }
}
