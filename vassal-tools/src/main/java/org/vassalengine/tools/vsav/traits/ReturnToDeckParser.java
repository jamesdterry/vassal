package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for ReturnToDeck trait.
 *
 * Type format: return;returnCommand;returnKey;deckId;selectDeckPrompt;description;version;deckSelect;deckExpression
 * State format: (empty)
 */
public class ReturnToDeckParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "return";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("returnCommand", getPart(typeParts, 1));
        trait.setProperty("returnKey", getPart(typeParts, 2));
        trait.setProperty("deckId", getPart(typeParts, 3));
        trait.setProperty("selectDeckPrompt", getPart(typeParts, 4));
        trait.setProperty("description", getPart(typeParts, 5));
        trait.setProperty("version", getPart(typeParts, 6));
        trait.setProperty("deckSelect", getPart(typeParts, 7));
        trait.setProperty("deckExpression", getPart(typeParts, 8));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("returnCommand")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("returnKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("deckId")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("selectDeckPrompt")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("version")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("deckSelect")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("deckExpression")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
