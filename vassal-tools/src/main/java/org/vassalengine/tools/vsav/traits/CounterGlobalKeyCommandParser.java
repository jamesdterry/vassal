package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for CounterGlobalKeyCommand trait (Global Key Command from a piece).
 *
 * Type format: globalkey;commandName;key;globalKey;propertiesFilter;restrictRange;range;
 *              reportSingle;fixedRange;rangeProperty;description;selectFromDeck;target;
 *              suppressSounds;parameters
 * State format: (empty)
 */
public class CounterGlobalKeyCommandParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "globalkey";

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
        trait.setProperty("globalKey", getPart(typeParts, 3));
        trait.setProperty("propertiesFilter", getPart(typeParts, 4));
        trait.setProperty("restrictRange", getPart(typeParts, 5));
        trait.setProperty("range", getPart(typeParts, 6));
        trait.setProperty("reportSingle", getPart(typeParts, 7));
        trait.setProperty("fixedRange", getPart(typeParts, 8));
        trait.setProperty("rangeProperty", getPart(typeParts, 9));
        trait.setProperty("description", getPart(typeParts, 10));
        trait.setProperty("selectFromDeck", getPart(typeParts, 11));
        trait.setProperty("target", getPart(typeParts, 12));
        trait.setProperty("suppressSounds", getPart(typeParts, 13));
        trait.setProperty("parameters", getPart(typeParts, 14));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("commandName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("key")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("globalKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("propertiesFilter")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("restrictRange")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("range")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("reportSingle")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("fixedRange")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("rangeProperty")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("selectFromDeck")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("target")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("suppressSounds")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("parameters")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
