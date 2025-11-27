package org.vassalengine.tools.vsav.model;

import org.vassalengine.tools.vsav.TraitParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game piece with its type definition and current state.
 * Contains both raw type/state strings and parsed trait data.
 *
 * The raw strings are preserved for lossless round-trip, while the
 * parsed traits provide structured access to trait properties.
 */
public class PieceData {
    private String id;
    private String type;   // Raw type definition string (tab-separated traits)
    private String state;  // Raw state string (tab-separated trait states)
    private List<TraitData> traits;  // Parsed trait data

    private static final TraitParser traitParser = new TraitParser();

    public PieceData() {
        this.traits = new ArrayList<>();
    }

    public PieceData(String id, String type, String state) {
        this();
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

    public List<TraitData> getTraits() {
        return traits;
    }

    public void setTraits(List<TraitData> traits) {
        this.traits = traits;
    }

    /**
     * Parse the type/state strings into trait data.
     * Call this after construction to populate the traits list.
     */
    public void parseTraits() {
        if (type != null) {
            this.traits = traitParser.parseTraits(type, state);
        }
    }

    /**
     * Encode traits back to type/state strings.
     * Call this before serialization to update raw strings from modified traits.
     */
    public void encodeTraits() {
        if (traits != null && !traits.isEmpty()) {
            String[] encoded = traitParser.encodeTraits(traits);
            this.type = encoded[0];
            this.state = encoded[1];
        }
    }

    /**
     * Find a trait by its ID.
     * @param traitId The trait ID (e.g., "piece", "label", "mark")
     * @return The first matching trait, or null if not found
     */
    public TraitData findTrait(String traitId) {
        if (traits == null) return null;
        for (TraitData trait : traits) {
            if (traitId.equals(trait.getTraitId())) {
                return trait;
            }
        }
        return null;
    }

    /**
     * Find all traits with a given ID.
     * @param traitId The trait ID
     * @return List of matching traits (may be empty)
     */
    public List<TraitData> findAllTraits(String traitId) {
        List<TraitData> result = new ArrayList<>();
        if (traits == null) return result;
        for (TraitData trait : traits) {
            if (traitId.equals(trait.getTraitId())) {
                result.add(trait);
            }
        }
        return result;
    }

    /**
     * Get the BasicPiece trait (always the innermost trait).
     * @return The BasicPiece trait, or null if not present
     */
    public TraitData getBasicPiece() {
        return findTrait("piece");
    }

    /**
     * Get the piece's basic name from the BasicPiece trait.
     * @return The basic name, or null if not available
     */
    public String getBasicName() {
        TraitData basicPiece = getBasicPiece();
        if (basicPiece != null) {
            return basicPiece.getStringProperty("basicName");
        }
        return null;
    }
}
