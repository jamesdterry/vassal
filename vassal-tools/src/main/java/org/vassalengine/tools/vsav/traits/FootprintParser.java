package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for Footprint trait (Movement Trail).
 *
 * Type format: footprint;trailKey;menuCommand;initiallyVisible;globallyVisible;circleRadius;
 *              fillColor;lineColor;selectedTransparency;unSelectedTransparency;edgePointBuffer;
 *              edgeDisplayBuffer;lineWidth;offCommandName;offKey;description;rotateWithPiece
 * State format: globalVisibility;localVisibility;everInitialized;points...
 */
public class FootprintParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "footprint";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("trailKey", getPart(typeParts, 1));
        trait.setProperty("menuCommand", getPart(typeParts, 2));
        trait.setProperty("initiallyVisible", getPart(typeParts, 3));
        trait.setProperty("globallyVisible", getPart(typeParts, 4));
        trait.setProperty("circleRadius", getPart(typeParts, 5));
        trait.setProperty("fillColor", getPart(typeParts, 6));
        trait.setProperty("lineColor", getPart(typeParts, 7));
        trait.setProperty("selectedTransparency", getPart(typeParts, 8));
        trait.setProperty("unSelectedTransparency", getPart(typeParts, 9));
        trait.setProperty("edgePointBuffer", getPart(typeParts, 10));
        trait.setProperty("edgeDisplayBuffer", getPart(typeParts, 11));
        trait.setProperty("lineWidth", getPart(typeParts, 12));
        trait.setProperty("offCommandName", getPart(typeParts, 13));
        trait.setProperty("offKey", getPart(typeParts, 14));
        trait.setProperty("description", getPart(typeParts, 15));
        trait.setProperty("rotateWithPiece", getPart(typeParts, 16));

        // State: globalVisibility;localVisibility;everInitialized;points...
        if (stateSegment != null && !stateSegment.isEmpty()) {
            String[] stateParts = split(stateSegment, ';');
            trait.setProperty("state.globalVisibility", getPart(stateParts, 0));
            trait.setProperty("state.localVisibility", getPart(stateParts, 1));
            trait.setProperty("state.everInitialized", getPart(stateParts, 2));
            // Store remaining state as raw point data
            if (stateParts.length > 3) {
                StringBuilder pointData = new StringBuilder();
                for (int i = 3; i < stateParts.length; i++) {
                    if (i > 3) pointData.append(';');
                    pointData.append(stateParts[i]);
                }
                trait.setProperty("state.pointData", pointData.toString());
            }
        }

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("trailKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("menuCommand")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("initiallyVisible")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("globallyVisible")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("circleRadius")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("fillColor")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("lineColor")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("selectedTransparency")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("unSelectedTransparency")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("edgePointBuffer")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("edgeDisplayBuffer")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("lineWidth")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("offCommandName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("offKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("rotateWithPiece")));

        // Encode state
        StringBuilder state = new StringBuilder();
        state.append(nullToEmpty(trait.getStringProperty("state.globalVisibility")));
        state.append(';').append(nullToEmpty(trait.getStringProperty("state.localVisibility")));
        state.append(';').append(nullToEmpty(trait.getStringProperty("state.everInitialized")));
        String pointData = trait.getStringProperty("state.pointData");
        if (pointData != null && !pointData.isEmpty()) {
            state.append(';').append(pointData);
        }

        return new String[] { type.toString(), state.toString() };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
