package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for Deselect trait.
 *
 * Type format: deselect;commandName;key;description;unstack;deselectType
 * State format: (empty)
 */
public class DeselectParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "deselect";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("commandName", getPart(typeParts, 1));
        trait.setProperty("key", getPart(typeParts, 2));
        trait.setProperty("description", getPart(typeParts, 3));
        trait.setProperty("unstack", getPart(typeParts, 4));
        trait.setProperty("deselectType", getPart(typeParts, 5));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("commandName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("key")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("unstack")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("deselectType")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
