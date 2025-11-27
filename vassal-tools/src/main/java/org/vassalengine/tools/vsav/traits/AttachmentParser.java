package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for Attachment trait.
 *
 * Type format: attach;attachName;desc;attachCommandName;attachKey;clearAllCommandName;clearAllKey;
 *              propertiesFilter;restrictRange;range;fixedRange;rangeProperty;selectFromDeck;target;
 *              clearMatchingCommandName;clearMatchingKey;clearMatchingFilter;onAttach;onDetach;
 *              beforeAttach;allowSelfAttach;autoAttach
 * State format: count;id1;id2;... (list of attached piece IDs)
 */
public class AttachmentParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "attach";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("attachName", getPart(typeParts, 1));
        trait.setProperty("desc", getPart(typeParts, 2));
        trait.setProperty("attachCommandName", getPart(typeParts, 3));
        trait.setProperty("attachKey", getPart(typeParts, 4));
        trait.setProperty("clearAllCommandName", getPart(typeParts, 5));
        trait.setProperty("clearAllKey", getPart(typeParts, 6));
        trait.setProperty("propertiesFilter", getPart(typeParts, 7));
        trait.setProperty("restrictRange", getPart(typeParts, 8));
        trait.setProperty("range", getPart(typeParts, 9));
        trait.setProperty("fixedRange", getPart(typeParts, 10));
        trait.setProperty("rangeProperty", getPart(typeParts, 11));
        trait.setProperty("selectFromDeck", getPart(typeParts, 12));
        trait.setProperty("target", getPart(typeParts, 13));
        trait.setProperty("clearMatchingCommandName", getPart(typeParts, 14));
        trait.setProperty("clearMatchingKey", getPart(typeParts, 15));
        trait.setProperty("clearMatchingFilter", getPart(typeParts, 16));
        trait.setProperty("onAttach", getPart(typeParts, 17));
        trait.setProperty("onDetach", getPart(typeParts, 18));
        trait.setProperty("beforeAttach", getPart(typeParts, 19));
        trait.setProperty("allowSelfAttach", getPart(typeParts, 20));
        trait.setProperty("autoAttach", getPart(typeParts, 21));

        // State: count;id1;id2;...
        if (stateSegment != null && !stateSegment.isEmpty()) {
            String[] stateParts = split(stateSegment, ';');
            trait.setProperty("state.count", getPart(stateParts, 0));
            // Store attached piece IDs
            if (stateParts.length > 1) {
                StringBuilder ids = new StringBuilder();
                for (int i = 1; i < stateParts.length; i++) {
                    if (i > 1) ids.append(';');
                    ids.append(stateParts[i]);
                }
                trait.setProperty("state.pieceIds", ids.toString());
            }
        }

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("attachName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("desc")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("attachCommandName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("attachKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("clearAllCommandName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("clearAllKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("propertiesFilter")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("restrictRange")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("range")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("fixedRange")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("rangeProperty")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("selectFromDeck")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("target")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("clearMatchingCommandName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("clearMatchingKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("clearMatchingFilter")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("onAttach")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("onDetach")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("beforeAttach")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("allowSelfAttach")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("autoAttach")));

        // Encode state
        StringBuilder state = new StringBuilder();
        String count = trait.getStringProperty("state.count");
        if (count != null && !count.isEmpty()) {
            state.append(count);
            String pieceIds = trait.getStringProperty("state.pieceIds");
            if (pieceIds != null && !pieceIds.isEmpty()) {
                state.append(';').append(pieceIds);
            }
        }

        return new String[] { type.toString(), state.toString() };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
