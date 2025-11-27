package org.vassalengine.tools.vsav.model;

/**
 * Represents a game piece with its type definition and current state.
 * In Phase 0, type and state are stored as raw strings.
 * In later phases, these will be parsed into trait data.
 */
public class PieceData {
    private String id;
    private String type;   // Raw type definition string (tab-separated traits)
    private String state;  // Raw state string (tab-separated trait states)

    public PieceData() {
    }

    public PieceData(String id, String type, String state) {
        this.id = id;
        this.type = type;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
