package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for TriggerAction trait (Trigger Action / Macro).
 *
 * Type format: macro;name;command;key;propertyMatch;watchKeys;actionKeys;loop;preLoopKey;postLoopKey;
 *              loopType;whileExpression;untilExpression;loopCount;index;indexProperty;indexStart;indexStep
 * State format: (empty)
 */
public class TriggerActionParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "macro";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("name", getPart(typeParts, 1));
        trait.setProperty("command", getPart(typeParts, 2));
        trait.setProperty("key", getPart(typeParts, 3));
        trait.setProperty("propertyMatch", getPart(typeParts, 4));
        trait.setProperty("watchKeys", getPart(typeParts, 5));
        trait.setProperty("actionKeys", getPart(typeParts, 6));
        trait.setProperty("loop", getPart(typeParts, 7));
        trait.setProperty("preLoopKey", getPart(typeParts, 8));
        trait.setProperty("postLoopKey", getPart(typeParts, 9));
        trait.setProperty("loopType", getPart(typeParts, 10));
        trait.setProperty("whileExpression", getPart(typeParts, 11));
        trait.setProperty("untilExpression", getPart(typeParts, 12));
        trait.setProperty("loopCount", getPart(typeParts, 13));
        trait.setProperty("index", getPart(typeParts, 14));
        trait.setProperty("indexProperty", getPart(typeParts, 15));
        trait.setProperty("indexStart", getPart(typeParts, 16));
        trait.setProperty("indexStep", getPart(typeParts, 17));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("name")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("command")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("key")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("propertyMatch")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("watchKeys")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("actionKeys")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("loop")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("preLoopKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("postLoopKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("loopType")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("whileExpression")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("untilExpression")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("loopCount")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("index")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("indexProperty")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("indexStart")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("indexStep")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
