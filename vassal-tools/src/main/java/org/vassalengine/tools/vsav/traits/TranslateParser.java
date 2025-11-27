package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for Translate trait (Move Fixed Distance).
 *
 * Type format: translate;commandName;keyCommand;xDist;yDist;moveStack;xIndex;yIndex;xOffset;yOffset;description
 * State format: (empty)
 */
public class TranslateParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "translate";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("commandName", getPart(typeParts, 1));
        trait.setProperty("keyCommand", getPart(typeParts, 2));
        trait.setProperty("xDist", getPart(typeParts, 3));
        trait.setProperty("yDist", getPart(typeParts, 4));
        trait.setProperty("moveStack", getPart(typeParts, 5));
        trait.setProperty("xIndex", getPart(typeParts, 6));
        trait.setProperty("yIndex", getPart(typeParts, 7));
        trait.setProperty("xOffset", getPart(typeParts, 8));
        trait.setProperty("yOffset", getPart(typeParts, 9));
        trait.setProperty("description", getPart(typeParts, 10));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("commandName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("keyCommand")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("xDist")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("yDist")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("moveStack")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("xIndex")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("yIndex")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("xOffset")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("yOffset")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
