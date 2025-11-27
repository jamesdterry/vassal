package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for SetGlobalProperty trait.
 *
 * Type format: setprop;key;constraints;keyCommandList;description;propertyLevel;searchName
 * State format: (empty)
 */
public class SetGlobalPropertyParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "setprop";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("key", getPart(typeParts, 1));
        trait.setProperty("constraints", getPart(typeParts, 2));
        trait.setProperty("keyCommandList", getPart(typeParts, 3));
        trait.setProperty("description", getPart(typeParts, 4));
        trait.setProperty("propertyLevel", getPart(typeParts, 5));
        trait.setProperty("searchName", getPart(typeParts, 6));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("key")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("constraints")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("keyCommandList")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("propertyLevel")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("searchName")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
