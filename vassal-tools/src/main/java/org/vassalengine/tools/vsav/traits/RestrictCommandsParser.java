package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for RestrictCommands trait.
 *
 * Type format: hideCmd;name;action;propertyMatch;watchKeys
 * State format: (empty)
 */
public class RestrictCommandsParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "hideCmd";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("name", getPart(typeParts, 1));
        trait.setProperty("action", getPart(typeParts, 2));
        trait.setProperty("propertyMatch", getPart(typeParts, 3));
        trait.setProperty("watchKeys", getPart(typeParts, 4));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("name")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("action")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("propertyMatch")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("watchKeys")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
