package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for PlaySound trait.
 *
 * Type format: playSound;format;menuText;stroke;sendToOthers;description;noSuppress
 * State format: (empty)
 */
public class PlaySoundParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "playSound";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("format", getPart(typeParts, 1));
        trait.setProperty("menuText", getPart(typeParts, 2));
        trait.setProperty("stroke", getPart(typeParts, 3));
        trait.setProperty("sendToOthers", getPart(typeParts, 4));
        trait.setProperty("description", getPart(typeParts, 5));
        trait.setProperty("noSuppress", getPart(typeParts, 6));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("format")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("menuText")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("stroke")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("sendToOthers")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("noSuppress")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
