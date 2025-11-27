package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for Embellishment trait (ID: "emb2") - the modern Layer format.
 *
 * Type format: emb2;[activateCommand];[activateModifiers];[activateKey];[upCommand];
 *              [upModifiers];[upKey];[downCommand];[downModifiers];[downKey];[resetCommand];
 *              [resetKey];[resetLevel];[drawUnderneathWhenSelected];[xOff];[yOff];
 *              [imageName[]];[commonName[]];[loopLevels];[name];[rndKey];[rndText];
 *              [followProperty];[propertyName];[firstLevelValue];[version];[alwaysActive];
 *              [activateKeyStroke];[increaseKeyStroke];[decreaseKeyStroke];[description];
 *              [scale];[onlyPropertyName];[onlyPropertyState]
 *
 * State format: [value] (a single integer)
 *
 * The Layer trait displays multiple images that can be cycled through.
 */
public class EmbellishmentParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "emb2";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        // Parse type: emb2;[many parameters...]
        String[] typeParts = split(typeSegment, ';');
        // typeParts[0] is "emb2"

        int idx = 1;
        trait.setProperty("activateCommand", getPart(typeParts, idx++));
        trait.setProperty("activateModifiers", parseInt(getPart(typeParts, idx++), 0));
        trait.setProperty("activateKey", getPart(typeParts, idx++));
        trait.setProperty("upCommand", getPart(typeParts, idx++));
        trait.setProperty("upModifiers", parseInt(getPart(typeParts, idx++), 0));
        trait.setProperty("upKey", getPart(typeParts, idx++));
        trait.setProperty("downCommand", getPart(typeParts, idx++));
        trait.setProperty("downModifiers", parseInt(getPart(typeParts, idx++), 0));
        trait.setProperty("downKey", getPart(typeParts, idx++));
        trait.setProperty("resetCommand", getPart(typeParts, idx++));
        trait.setProperty("resetKey", getPart(typeParts, idx++));
        trait.setProperty("resetLevel", getPart(typeParts, idx++, "1"));
        trait.setProperty("drawUnderneathWhenSelected", "true".equals(getPart(typeParts, idx++)));
        trait.setProperty("xOff", parseInt(getPart(typeParts, idx++), 0));
        trait.setProperty("yOff", parseInt(getPart(typeParts, idx++), 0));

        // Parse image name array
        String imageNamesStr = getPart(typeParts, idx++);
        trait.setProperty("imageNames", parseStringArray(imageNamesStr));

        // Parse common name array
        String commonNamesStr = getPart(typeParts, idx++);
        trait.setProperty("commonNames", parseStringArray(commonNamesStr));

        trait.setProperty("loopLevels", "true".equals(getPart(typeParts, idx++)));
        trait.setProperty("name", getPart(typeParts, idx++));
        trait.setProperty("rndKey", getPart(typeParts, idx++));
        trait.setProperty("rndText", getPart(typeParts, idx++));
        trait.setProperty("followProperty", "true".equals(getPart(typeParts, idx++)));
        trait.setProperty("propertyName", getPart(typeParts, idx++));
        trait.setProperty("firstLevelValue", parseInt(getPart(typeParts, idx++), 1));
        trait.setProperty("version", parseInt(getPart(typeParts, idx++), 0));
        trait.setProperty("alwaysActive", "true".equals(getPart(typeParts, idx++)));
        trait.setProperty("activateKeyStroke", getPart(typeParts, idx++));
        trait.setProperty("increaseKeyStroke", getPart(typeParts, idx++));
        trait.setProperty("decreaseKeyStroke", getPart(typeParts, idx++));
        trait.setProperty("description", getPart(typeParts, idx++));
        trait.setProperty("scale", parseDouble(getPart(typeParts, idx++), 1.0));
        trait.setProperty("onlyPropertyName", getPart(typeParts, idx++));
        trait.setProperty("onlyPropertyState", getPart(typeParts, idx++, "true"));

        // Parse state: [value] (can have additional fields for legacy Embellishment0)
        String[] stateParts = split(stateSegment != null ? stateSegment : "", ';');
        trait.setProperty("value", parseInt(getPart(stateParts, 0), -1));
        // Embellishment0 has an activationStatus field
        if (stateParts.length > 1) {
            trait.setProperty("activationStatus", getPart(stateParts, 1));
        }

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        // Encode type
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("activateCommand")));
        type.append(';').append(getIntOrDefault(trait, "activateModifiers", 0));
        type.append(';').append(nullToEmpty(trait.getStringProperty("activateKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("upCommand")));
        type.append(';').append(getIntOrDefault(trait, "upModifiers", 0));
        type.append(';').append(nullToEmpty(trait.getStringProperty("upKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("downCommand")));
        type.append(';').append(getIntOrDefault(trait, "downModifiers", 0));
        type.append(';').append(nullToEmpty(trait.getStringProperty("downKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("resetCommand")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("resetKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("resetLevel")));
        type.append(';').append(Boolean.TRUE.equals(trait.getProperty("drawUnderneathWhenSelected")));
        type.append(';').append(getIntOrDefault(trait, "xOff", 0));
        type.append(';').append(getIntOrDefault(trait, "yOff", 0));

        // Encode image name array
        type.append(';').append(encodeStringArray(trait, "imageNames"));

        // Encode common name array
        type.append(';').append(encodeStringArray(trait, "commonNames"));

        type.append(';').append(Boolean.TRUE.equals(trait.getProperty("loopLevels")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("name")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("rndKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("rndText")));
        type.append(';').append(Boolean.TRUE.equals(trait.getProperty("followProperty")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("propertyName")));
        type.append(';').append(getIntOrDefault(trait, "firstLevelValue", 1));
        type.append(';').append(getIntOrDefault(trait, "version", 0));
        type.append(';').append(Boolean.TRUE.equals(trait.getProperty("alwaysActive")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("activateKeyStroke")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("increaseKeyStroke")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("decreaseKeyStroke")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(getDoubleOrDefault(trait, "scale", 1.0));
        type.append(';').append(nullToEmpty(trait.getStringProperty("onlyPropertyName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("onlyPropertyState")));

        // Encode state
        StringBuilder state = new StringBuilder();
        state.append(getIntOrDefault(trait, "value", -1));
        // Include activationStatus if present (for Embellishment0 compatibility)
        String activationStatus = trait.getStringProperty("activationStatus");
        if (activationStatus != null) {
            state.append(';').append(activationStatus);
        }

        return new String[] { type.toString(), state.toString() };
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private int getIntOrDefault(TraitData trait, String key, int defaultValue) {
        Integer val = trait.getIntProperty(key);
        return val != null ? val : defaultValue;
    }

    private double getDoubleOrDefault(TraitData trait, String key, double defaultValue) {
        Object val = trait.getProperty(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            try {
                return Double.parseDouble((String) val);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private double parseDouble(String s, double defaultValue) {
        if (s == null || s.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parse VASSAL's encoded string array format.
     * Arrays are simple comma-separated: value1,value2,...
     * Empty string means empty array.
     */
    private List<String> parseStringArray(String encoded) {
        List<String> result = new ArrayList<>();
        if (encoded == null || encoded.isEmpty()) {
            return result;
        }
        String[] parts = split(encoded, ',');
        for (String part : parts) {
            result.add(part);
        }
        return result;
    }

    /**
     * Encode a string list to VASSAL's array format.
     * Simple comma-separated: value1,value2,...
     */
    @SuppressWarnings("unchecked")
    private String encodeStringArray(TraitData trait, String key) {
        Object val = trait.getProperty(key);
        if (val == null) {
            return "";
        }
        List<String> list;
        if (val instanceof List) {
            list = (List<String>) val;
        } else {
            return "";
        }
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            String s = list.get(i);
            if (s != null) {
                // Escape commas
                for (int j = 0; j < s.length(); j++) {
                    char c = s.charAt(j);
                    if (c == ',') {
                        sb.append('\\');
                    }
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
