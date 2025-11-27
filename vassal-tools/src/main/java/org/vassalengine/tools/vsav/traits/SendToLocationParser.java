package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for SendToLocation trait.
 *
 * Type format: sendto;commandName;key;mapId;boardName;x;y;backCommandName;backKey;xIndex;yIndex;xOffset;yOffset;description;destination;zone;region;propertyFilter;gridLocation
 * State format: mapId;x;y (for back location tracking)
 */
public class SendToLocationParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "sendto";

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
        trait.setProperty("mapId", getPart(typeParts, 3));
        trait.setProperty("boardName", getPart(typeParts, 4));
        trait.setProperty("x", getPart(typeParts, 5));
        trait.setProperty("y", getPart(typeParts, 6));
        trait.setProperty("backCommandName", getPart(typeParts, 7));
        trait.setProperty("backKey", getPart(typeParts, 8));
        trait.setProperty("xIndex", getPart(typeParts, 9));
        trait.setProperty("yIndex", getPart(typeParts, 10));
        trait.setProperty("xOffset", getPart(typeParts, 11));
        trait.setProperty("yOffset", getPart(typeParts, 12));
        trait.setProperty("description", getPart(typeParts, 13));
        trait.setProperty("destination", getPart(typeParts, 14));
        trait.setProperty("zone", getPart(typeParts, 15));
        trait.setProperty("region", getPart(typeParts, 16));
        trait.setProperty("propertyFilter", getPart(typeParts, 17));
        trait.setProperty("gridLocation", getPart(typeParts, 18));

        // State tracks back location: mapId;x;y
        trait.setProperty("backState", stateSegment != null ? stateSegment : "");

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("commandName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("key")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("mapId")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("boardName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("x")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("y")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("backCommandName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("backKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("xIndex")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("yIndex")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("xOffset")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("yOffset")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("destination")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("zone")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("region")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("propertyFilter")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("gridLocation")));

        String state = nullToEmpty(trait.getStringProperty("backState"));

        return new String[] { type.toString(), state };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
