package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parser for BasicPiece trait (ID: "piece").
 *
 * Type format: piece;[cloneKey];[deleteKey];[imageName];[commonName]
 * State format: [mapName];[x];[y];[gpId];[persistentPropCount];[propKey1];[propVal1];...
 */
public class BasicPieceParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "piece";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        // Parse type: piece;[cloneKey];[deleteKey];[imageName];[commonName]
        String[] typeParts = split(typeSegment, ';');
        // typeParts[0] is "piece"
        trait.setProperty("cloneKey", getPart(typeParts, 1));
        trait.setProperty("deleteKey", getPart(typeParts, 2));
        trait.setProperty("imageName", getPart(typeParts, 3));
        trait.setProperty("basicName", getPart(typeParts, 4));

        // Parse state: [mapName];[x];[y];[gpId];[persistentPropCount];[propKey1];[propVal1];...
        String[] stateParts = split(stateSegment, ';');
        String mapName = getPart(stateParts, 0);
        trait.setProperty("mapName", "null".equals(mapName) ? null : mapName);
        trait.setProperty("x", parseInt(getPart(stateParts, 1), 0));
        trait.setProperty("y", parseInt(getPart(stateParts, 2), 0));
        trait.setProperty("gpId", getPart(stateParts, 3));

        // Parse persistent properties if present
        int propCount = parseInt(getPart(stateParts, 4), 0);
        if (propCount > 0) {
            Map<String, String> persistentProps = new LinkedHashMap<>();
            int idx = 5;
            for (int i = 0; i < propCount && idx + 1 < stateParts.length; i++) {
                String key = stateParts[idx++];
                String value = idx < stateParts.length ? stateParts[idx++] : "";
                if (key != null && !key.isEmpty()) {
                    persistentProps.put(key, value);
                }
            }
            if (!persistentProps.isEmpty()) {
                trait.setProperty("persistentProperties", persistentProps);
            }
        }

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        // Encode type
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("cloneKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("deleteKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("imageName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("basicName")));

        // Encode state
        StringBuilder state = new StringBuilder();
        String mapName = trait.getStringProperty("mapName");
        state.append(mapName != null ? mapName : "null");
        state.append(';').append(getIntOrDefault(trait, "x", 0));
        state.append(';').append(getIntOrDefault(trait, "y", 0));
        state.append(';').append(nullToEmpty(trait.getStringProperty("gpId")));

        // Encode persistent properties
        @SuppressWarnings("unchecked")
        Map<String, String> props = (Map<String, String>) trait.getProperty("persistentProperties");
        if (props != null && !props.isEmpty()) {
            state.append(';').append(props.size());
            for (Map.Entry<String, String> entry : props.entrySet()) {
                state.append(';').append(escapeValue(entry.getKey(), ';'));
                state.append(';').append(escapeValue(entry.getValue(), ';'));
            }
        } else {
            state.append(';').append(0);
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
