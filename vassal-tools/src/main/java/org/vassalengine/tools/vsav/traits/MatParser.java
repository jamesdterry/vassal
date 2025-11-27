package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for Mat trait.
 *
 * Type format: mat;matName;desc
 * State format: [count];[id1];[id2];... (list of cargo piece IDs)
 */
public class MatParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "mat";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("matName", getPart(typeParts, 1));
        trait.setProperty("description", getPart(typeParts, 2));

        // State is semicolon-separated: count;id1;id2;...
        // We preserve it as rawState for round-trip fidelity
        trait.setProperty("cargoState", stateSegment != null ? stateSegment : "");

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("matName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));

        String state = nullToEmpty(trait.getStringProperty("cargoState"));

        return new String[] { type.toString(), state };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
