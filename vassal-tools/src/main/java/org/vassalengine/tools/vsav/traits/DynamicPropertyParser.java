package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for DynamicProperty trait.
 *
 * Type format: PROP;key;constraints;keyCommands;description
 *   where constraints = numeric,minValue,maxValue,wrap
 * State format: value (the current property value)
 */
public class DynamicPropertyParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "PROP";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("key", getPart(typeParts, 1));
        trait.setProperty("constraints", getPart(typeParts, 2));
        trait.setProperty("keyCommands", getPart(typeParts, 3));
        trait.setProperty("description", getPart(typeParts, 4));

        // Parse constraints: numeric,minValue,maxValue,wrap
        String constraints = getPart(typeParts, 2);
        if (constraints != null && !constraints.isEmpty()) {
            String[] constraintParts = constraints.split(",");
            if (constraintParts.length >= 1) {
                trait.setProperty("numeric", constraintParts[0]);
            }
            if (constraintParts.length >= 2) {
                trait.setProperty("minValue", constraintParts[1]);
            }
            if (constraintParts.length >= 3) {
                trait.setProperty("maxValue", constraintParts[2]);
            }
            if (constraintParts.length >= 4) {
                trait.setProperty("wrap", constraintParts[3]);
            }
        }

        // State: current property value
        if (stateSegment != null) {
            trait.setProperty("value", stateSegment);
        }

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("key")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("constraints")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("keyCommands")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));

        String state = nullToEmpty(trait.getStringProperty("value"));

        return new String[] { type.toString(), state };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
