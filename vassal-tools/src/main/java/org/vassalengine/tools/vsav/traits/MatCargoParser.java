package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for MatCargo trait (Cargo on a Mat).
 *
 * Type format: matPiece;desc;maintainRelativeFacing;detectionDistanceX;detectionDistanceY;matFindKey;matDetachKey
 * State format: [matId] or "noMat"
 */
public class MatCargoParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "matPiece";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("description", getPart(typeParts, 1));
        trait.setProperty("maintainRelativeFacing", getPart(typeParts, 2));
        trait.setProperty("detectionDistanceX", parseInt(getPart(typeParts, 3), 0));
        trait.setProperty("detectionDistanceY", parseInt(getPart(typeParts, 4), 0));
        trait.setProperty("matFindKey", getPart(typeParts, 5));
        trait.setProperty("matDetachKey", getPart(typeParts, 6));

        // State is mat ID or "noMat"
        trait.setProperty("matId", stateSegment != null ? stateSegment : "noMat");

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("maintainRelativeFacing")));
        type.append(';').append(trait.getIntProperty("detectionDistanceX", 0));
        type.append(';').append(trait.getIntProperty("detectionDistanceY", 0));
        type.append(';').append(nullToEmpty(trait.getStringProperty("matFindKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("matDetachKey")));

        String state = nullToEmpty(trait.getStringProperty("matId"));
        if (state.isEmpty()) {
            state = "noMat";
        }

        return new String[] { type.toString(), state };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
