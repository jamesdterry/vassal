package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for Labeler trait (ID: "label").
 *
 * Type format: label;[keyStroke];[menuCommand];[fontSize];[bgColor];[fgColor];
 *              [vPos];[vOffset];[hPos];[hOffset];[vJust];[hJust];[nameFormat];
 *              [fontFamily];[fontStyle];[rotateDegrees];[propertyName];[description];[alwaysUseFormat]
 *
 * State format: [labelText]
 *
 * The label trait displays a text label on a piece that can be changed during play.
 */
public class LabelerParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "label";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        // Parse type: label;[many parameters...]
        String[] typeParts = split(typeSegment, ';');
        // typeParts[0] is "label"

        trait.setProperty("keyStroke", getPart(typeParts, 1));
        trait.setProperty("menuCommand", getPart(typeParts, 2));
        trait.setProperty("fontSize", parseInt(getPart(typeParts, 3), 10));
        trait.setProperty("bgColor", getPart(typeParts, 4));
        trait.setProperty("fgColor", getPart(typeParts, 5));
        trait.setProperty("verticalPos", getPart(typeParts, 6, "t"));
        trait.setProperty("verticalOffset", parseInt(getPart(typeParts, 7), 0));
        trait.setProperty("horizontalPos", getPart(typeParts, 8, "c"));
        trait.setProperty("horizontalOffset", parseInt(getPart(typeParts, 9), 0));
        trait.setProperty("verticalJust", getPart(typeParts, 10, "b"));
        trait.setProperty("horizontalJust", getPart(typeParts, 11, "c"));
        trait.setProperty("nameFormat", getPart(typeParts, 12));
        trait.setProperty("fontFamily", getPart(typeParts, 13, "Dialog"));
        trait.setProperty("fontStyle", parseInt(getPart(typeParts, 14), 0));
        trait.setProperty("rotateDegrees", parseInt(getPart(typeParts, 15), 0));
        trait.setProperty("propertyName", getPart(typeParts, 16, "TextLabel"));
        trait.setProperty("description", getPart(typeParts, 17));
        trait.setProperty("alwaysUseFormat", "true".equals(getPart(typeParts, 18)));

        // Parse state: just the label text
        String labelText = stateSegment != null ? stateSegment.trim() : "";
        trait.setProperty("labelText", labelText);

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        // Encode type
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("keyStroke")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("menuCommand")));
        type.append(';').append(getIntOrDefault(trait, "fontSize", 10));
        type.append(';').append(nullToEmpty(trait.getStringProperty("bgColor")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("fgColor")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("verticalPos")));
        type.append(';').append(getIntOrDefault(trait, "verticalOffset", 0));
        type.append(';').append(nullToEmpty(trait.getStringProperty("horizontalPos")));
        type.append(';').append(getIntOrDefault(trait, "horizontalOffset", 0));
        type.append(';').append(nullToEmpty(trait.getStringProperty("verticalJust")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("horizontalJust")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("nameFormat")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("fontFamily")));
        type.append(';').append(getIntOrDefault(trait, "fontStyle", 0));
        type.append(';').append(getIntOrDefault(trait, "rotateDegrees", 0));
        type.append(';').append(nullToEmpty(trait.getStringProperty("propertyName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(Boolean.TRUE.equals(trait.getProperty("alwaysUseFormat")));

        // Encode state: just the label text
        String state = nullToEmpty(trait.getStringProperty("labelText"));

        return new String[] { type.toString(), state };
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private int getIntOrDefault(TraitData trait, String key, int defaultValue) {
        Integer val = trait.getIntProperty(key);
        return val != null ? val : defaultValue;
    }
}
