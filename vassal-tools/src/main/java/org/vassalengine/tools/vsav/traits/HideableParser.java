package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for Hideable trait (ID: "hide") - the Invisible trait.
 *
 * Type format: hide;[hideKey];[command];[bgColor];[access];[transparency];[description];[disableAutoReportMove]
 *
 * State format: [hiddenBy] (player ID or "null")
 *
 * The Invisible trait makes a piece invisible to other players.
 */
public class HideableParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "hide";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        // Parse type: hide;[parameters...]
        String[] typeParts = split(typeSegment, ';');
        // typeParts[0] is "hide"

        int idx = 1;
        trait.setProperty("hideKey", getPart(typeParts, idx++));
        trait.setProperty("command", getPart(typeParts, idx++));
        trait.setProperty("bgColor", getPart(typeParts, idx++));
        trait.setProperty("access", getPart(typeParts, idx++));
        trait.setProperty("transparency", parseDouble(getPart(typeParts, idx++), 0.3));
        trait.setProperty("description", getPart(typeParts, idx++));
        trait.setProperty("disableAutoReportMove", "true".equals(getPart(typeParts, idx++)));

        // Parse state: [hiddenBy]
        String hiddenBy = stateSegment != null ? stateSegment.trim() : "null";
        trait.setProperty("hiddenBy", "null".equals(hiddenBy) ? null : hiddenBy);

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        // Encode type
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("hideKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("command")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("bgColor")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("access")));
        type.append(';').append(getDoubleOrDefault(trait, "transparency", 0.3));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(Boolean.TRUE.equals(trait.getProperty("disableAutoReportMove")));

        // Encode state
        String hiddenBy = trait.getStringProperty("hiddenBy");
        String state = hiddenBy == null ? "null" : hiddenBy;

        return new String[] { type.toString(), state };
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
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
}
