package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for Restricted trait (Restricted Access).
 *
 * Type format: restrict;[sides array];restrictByPlayer;restrictMovement;description
 * State format: owningPlayer (string, can be empty)
 */
public class RestrictedParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "restrict";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("sides", getPart(typeParts, 1));
        trait.setProperty("restrictByPlayer", getPart(typeParts, 2));
        trait.setProperty("restrictMovement", getPart(typeParts, 3));
        trait.setProperty("description", getPart(typeParts, 4));

        // State is the owning player
        trait.setProperty("owningPlayer", stateSegment != null ? stateSegment : "");

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("sides")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("restrictByPlayer")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("restrictMovement")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));

        String state = nullToEmpty(trait.getStringProperty("owningPlayer"));

        return new String[] { type.toString(), state };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
