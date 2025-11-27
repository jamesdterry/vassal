package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for GlobalHotKey trait.
 *
 * Type format: globalhotkey;commandName;commandKey;globalHotKey;description
 * State format: (empty)
 */
public class GlobalHotKeyParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "globalhotkey";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("commandName", getPart(typeParts, 1));
        trait.setProperty("commandKey", getPart(typeParts, 2));
        trait.setProperty("globalHotKey", getPart(typeParts, 3));
        trait.setProperty("description", getPart(typeParts, 4));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("commandName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("commandKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("globalHotKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
