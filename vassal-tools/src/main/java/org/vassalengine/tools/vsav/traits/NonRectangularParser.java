package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for NonRectangular trait.
 *
 * Type format: nonRect2;scale;shapeSpec
 * State format: (empty)
 */
public class NonRectangularParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "nonRect2";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("scale", getPart(typeParts, 1, "1.0"));
        // Shape spec is the remaining part after scale
        if (typeParts.length > 2) {
            StringBuilder shapeSpec = new StringBuilder();
            for (int i = 2; i < typeParts.length; i++) {
                if (i > 2) shapeSpec.append(';');
                shapeSpec.append(typeParts[i]);
            }
            trait.setProperty("shapeSpec", shapeSpec.toString());
        }

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("scale"), "1.0"));

        String shapeSpec = trait.getStringProperty("shapeSpec");
        if (shapeSpec != null && !shapeSpec.isEmpty()) {
            type.append(';').append(shapeSpec);
        }

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s, String defaultVal) {
        return (s == null || s.isEmpty()) ? defaultVal : s;
    }
}
