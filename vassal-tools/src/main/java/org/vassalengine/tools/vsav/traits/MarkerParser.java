package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for Marker trait (ID: "mark").
 *
 * Type format: mark;[key1],[key2],[key3],...
 * State format: [value1],[value2],[value3],...
 *
 * The Marker trait stores property key-value pairs where keys are defined
 * in the type and values are stored in the state.
 */
public class MarkerParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "mark";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        // Parse type: mark;[key1],[key2],...
        String keysStr = "";
        if (typeSegment.startsWith(TRAIT_ID + ";")) {
            keysStr = typeSegment.substring(TRAIT_ID.length() + 1);
        }

        String[] keys = split(keysStr, ',');

        // Parse state: [value1],[value2],...
        String[] values = (stateSegment != null && !stateSegment.isEmpty())
            ? split(stateSegment, ',')
            : new String[0];

        // Store as ordered map of key-value pairs
        Map<String, String> markers = new LinkedHashMap<>();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String value = i < values.length ? values[i] : "";
            if (key != null && !key.isEmpty()) {
                markers.put(key, value);
            }
        }

        trait.setProperty("markers", markers);

        // Also store as separate lists for visibility
        List<String> keyList = new ArrayList<>();
        for (String key : keys) {
            if (key != null && !key.isEmpty()) {
                keyList.add(key);
            }
        }
        trait.setProperty("keys", keyList);

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        @SuppressWarnings("unchecked")
        Map<String, String> markers = (Map<String, String>) trait.getProperty("markers");

        if (markers == null || markers.isEmpty()) {
            // Fall back to raw if no parsed data
            return new String[] {
                trait.getRawType() != null ? trait.getRawType() : TRAIT_ID + ";",
                trait.getRawState() != null ? trait.getRawState() : ""
            };
        }

        // Encode type: mark;key1,key2,...
        StringBuilder type = new StringBuilder(TRAIT_ID).append(';');
        StringBuilder state = new StringBuilder();

        boolean first = true;
        for (Map.Entry<String, String> entry : markers.entrySet()) {
            if (!first) {
                type.append(',');
                state.append(',');
            }
            type.append(escapeValue(entry.getKey(), ','));
            state.append(escapeValue(entry.getValue(), ','));
            first = false;
        }

        return new String[] { type.toString(), state.toString() };
    }

    private String escapeValue(String s, char delim) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == delim) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
