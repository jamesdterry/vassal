package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for SubMenu trait.
 *
 * Type format: submenu;menuName;commands;description
 * State format: (empty)
 */
public class SubMenuParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "submenu";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("menuName", getPart(typeParts, 1));
        trait.setProperty("commands", getPart(typeParts, 2));
        trait.setProperty("description", getPart(typeParts, 3));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("menuName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("commands")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
