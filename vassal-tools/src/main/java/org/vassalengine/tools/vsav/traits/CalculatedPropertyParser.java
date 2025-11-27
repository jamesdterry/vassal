package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for CalculatedProperty trait.
 *
 * Type format: calcProp;name;expression;description
 * State format: (empty)
 */
public class CalculatedPropertyParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "calcProp";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("name", getPart(typeParts, 1));
        trait.setProperty("expression", getPart(typeParts, 2));
        trait.setProperty("description", getPart(typeParts, 3));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("name")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("expression")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
