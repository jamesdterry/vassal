package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for MenuSeparator trait.
 *
 * Type format: menuSeparator;desc;key
 * State format: (empty)
 */
public class MenuSeparatorParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "menuSeparator";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("desc", getPart(typeParts, 1));
        trait.setProperty("key", getPart(typeParts, 2));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("desc")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("key")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
