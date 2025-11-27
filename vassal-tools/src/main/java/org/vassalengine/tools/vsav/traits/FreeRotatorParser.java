package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for FreeRotator trait (Can Rotate).
 *
 * Type format varies based on whether it's free rotation (1 facing) or fixed facings:
 *
 * Free rotation (validAngles.length == 1):
 *   rotate;1;[setAngleKey];[setAngleText];[rndKey];[rndText];[name];[description];[directKey];[directText];[directExpr];[directTypeFacing]
 *
 * Fixed facings (validAngles.length > 1):
 *   rotate;[numFacings];[cwKey];[ccwKey];[cwText];[ccwText];[rndKey];[rndText];[name];[description];[directKey];[directText];[directExpr];[directTypeFacing]
 *
 * State format:
 *   Free rotation: [angle] (double)
 *   Fixed facings: [angleIndex] (integer)
 */
public class FreeRotatorParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "rotate";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        // Parse type: rotate;numFacings;...
        String[] typeParts = split(typeSegment, ';');

        int numFacings = parseInt(getPart(typeParts, 1), 1);
        trait.setProperty("numFacings", numFacings);
        boolean isFreeRotation = (numFacings == 1);
        trait.setProperty("isFreeRotation", isFreeRotation);

        if (isFreeRotation) {
            // Free rotation: rotate;1;setAngleKey;setAngleText;rndKey;rndText;name;description;directKey;directText;directExpr;directTypeFacing
            trait.setProperty("setAngleKey", getPart(typeParts, 2));
            trait.setProperty("setAngleText", getPart(typeParts, 3));
            trait.setProperty("rndKey", getPart(typeParts, 4));
            trait.setProperty("rndText", getPart(typeParts, 5));
            trait.setProperty("name", getPart(typeParts, 6));
            trait.setProperty("description", getPart(typeParts, 7));
            trait.setProperty("directKey", getPart(typeParts, 8));
            trait.setProperty("directText", getPart(typeParts, 9));
            trait.setProperty("directExpr", getPart(typeParts, 10));
            trait.setProperty("directTypeFacing", getPart(typeParts, 11));
        } else {
            // Fixed facings: rotate;numFacings;cwKey;ccwKey;cwText;ccwText;rndKey;rndText;name;description;directKey;directText;directExpr;directTypeFacing
            trait.setProperty("cwKey", getPart(typeParts, 2));
            trait.setProperty("ccwKey", getPart(typeParts, 3));
            trait.setProperty("cwText", getPart(typeParts, 4));
            trait.setProperty("ccwText", getPart(typeParts, 5));
            trait.setProperty("rndKey", getPart(typeParts, 6));
            trait.setProperty("rndText", getPart(typeParts, 7));
            trait.setProperty("name", getPart(typeParts, 8));
            trait.setProperty("description", getPart(typeParts, 9));
            trait.setProperty("directKey", getPart(typeParts, 10));
            trait.setProperty("directText", getPart(typeParts, 11));
            trait.setProperty("directExpr", getPart(typeParts, 12));
            trait.setProperty("directTypeFacing", getPart(typeParts, 13));
        }

        // Parse state
        if (stateSegment != null && !stateSegment.isEmpty()) {
            if (isFreeRotation) {
                // State is the angle as a double
                try {
                    trait.setProperty("angle", Double.parseDouble(stateSegment));
                } catch (NumberFormatException e) {
                    trait.setProperty("angle", 0.0);
                }
            } else {
                // State is the angleIndex as an integer
                trait.setProperty("angleIndex", parseInt(stateSegment, 0));
            }
        } else {
            if (isFreeRotation) {
                trait.setProperty("angle", 0.0);
            } else {
                trait.setProperty("angleIndex", 0);
            }
        }

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);

        int numFacings = trait.getIntProperty("numFacings", 1);
        boolean isFreeRotation = (numFacings == 1);

        type.append(';').append(numFacings);

        if (isFreeRotation) {
            // Free rotation format
            type.append(';').append(nullToEmpty(trait.getStringProperty("setAngleKey")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("setAngleText")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("rndKey")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("rndText")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("name")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("directKey")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("directText")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("directExpr")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("directTypeFacing")));
        } else {
            // Fixed facings format
            type.append(';').append(nullToEmpty(trait.getStringProperty("cwKey")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("ccwKey")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("cwText")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("ccwText")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("rndKey")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("rndText")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("name")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("directKey")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("directText")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("directExpr")));
            type.append(';').append(nullToEmpty(trait.getStringProperty("directTypeFacing")));
        }

        // Encode state
        String state;
        if (isFreeRotation) {
            double angle = trait.getDoubleProperty("angle", 0.0);
            state = String.valueOf(angle);
        } else {
            int angleIndex = trait.getIntProperty("angleIndex", 0);
            state = String.valueOf(angleIndex);
        }

        return new String[] { type.toString(), state };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
