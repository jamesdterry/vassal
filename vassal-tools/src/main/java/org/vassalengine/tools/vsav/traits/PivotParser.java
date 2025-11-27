package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for Pivot trait (Can Pivot).
 *
 * Type format: pivot;command;key;pivotX;pivotY;fixedAngle;angle;description;command2;key2;angle2
 * State format: (empty)
 */
public class PivotParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "pivot";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("command", getPart(typeParts, 1));
        trait.setProperty("key", getPart(typeParts, 2));
        trait.setProperty("pivotX", parseInt(getPart(typeParts, 3), 0));
        trait.setProperty("pivotY", parseInt(getPart(typeParts, 4), 0));
        trait.setProperty("fixedAngle", getPart(typeParts, 5));
        trait.setProperty("angle", getPart(typeParts, 6));
        trait.setProperty("description", getPart(typeParts, 7));
        trait.setProperty("command2", getPart(typeParts, 8));
        trait.setProperty("key2", getPart(typeParts, 9));
        trait.setProperty("angle2", getPart(typeParts, 10));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("command")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("key")));
        type.append(';').append(trait.getIntProperty("pivotX", 0));
        type.append(';').append(trait.getIntProperty("pivotY", 0));
        type.append(';').append(nullToEmpty(trait.getStringProperty("fixedAngle")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("angle")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("command2")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("key2")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("angle2")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
