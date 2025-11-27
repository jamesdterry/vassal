package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for MovementMarkable trait (Mark When Moved).
 *
 * Type format: markmoved;[iconName];[xOffset];[yOffset];[command];[key];[description];[ignoreSameLocation];[commandTrue];[keyTrue];[commandFalse];[keyFalse]
 * State format: true|false (hasMoved boolean)
 */
public class MovementMarkableParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "markmoved";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        // Parse type: markmoved;iconName;xOffset;yOffset;command;key;description;ignoreSameLocation;commandTrue;keyTrue;commandFalse;keyFalse
        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("iconName", getPart(typeParts, 1));
        trait.setProperty("xOffset", parseInt(getPart(typeParts, 2), 0));
        trait.setProperty("yOffset", parseInt(getPart(typeParts, 3), 0));
        trait.setProperty("command", getPart(typeParts, 4));
        trait.setProperty("key", getPart(typeParts, 5));
        trait.setProperty("description", getPart(typeParts, 6));
        trait.setProperty("ignoreSameLocation", getPart(typeParts, 7));
        trait.setProperty("commandTrue", getPart(typeParts, 8));
        trait.setProperty("keyTrue", getPart(typeParts, 9));
        trait.setProperty("commandFalse", getPart(typeParts, 10));
        trait.setProperty("keyFalse", getPart(typeParts, 11));

        // Parse state: true|false
        trait.setProperty("hasMoved", "true".equals(stateSegment));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        // Encode type
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("iconName")));
        type.append(';').append(trait.getIntProperty("xOffset", 0));
        type.append(';').append(trait.getIntProperty("yOffset", 0));
        type.append(';').append(nullToEmpty(trait.getStringProperty("command")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("key")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("ignoreSameLocation")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("commandTrue")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("keyTrue")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("commandFalse")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("keyFalse")));

        // Encode state
        boolean hasMoved = trait.getBooleanProperty("hasMoved", false);
        String state = String.valueOf(hasMoved);

        return new String[] { type.toString(), state };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
