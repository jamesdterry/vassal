package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Base class for trait parsers. Each trait parser knows how to parse
 * a specific trait ID (e.g., "piece", "label", "mark") from its
 * type and state string segments.
 */
public abstract class AbstractTraitParser {

    /**
     * @return The trait ID this parser handles (e.g., "piece", "label", "mark")
     */
    public abstract String getTraitId();

    /**
     * Parse the type and state string segments into a TraitData object.
     *
     * @param typeSegment  The type string segment (e.g., "piece;myCloneKey;myDeleteKey;image.png;My Piece")
     * @param stateSegment The state string segment (e.g., "Main Map;100;200;1234")
     * @return Parsed TraitData with properties extracted
     */
    public abstract TraitData parse(String typeSegment, String stateSegment);

    /**
     * Encode a TraitData object back into type and state strings.
     *
     * @param trait The trait data to encode
     * @return A two-element array: [typeSegment, stateSegment]
     */
    public abstract String[] encode(TraitData trait);

    /**
     * Split a string by a delimiter, handling VASSAL's escape sequences.
     * In VASSAL's SequenceEncoder, backslash only escapes the delimiter character,
     * NOT backslashes themselves. So \; means literal ; but \\ means literal \\.
     *
     * @param s     The string to split
     * @param delim The delimiter character
     * @return Array of parts
     */
    protected String[] split(String s, char delim) {
        if (s == null || s.isEmpty()) {
            return new String[0];
        }

        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '\\' && i + 1 < s.length() && s.charAt(i + 1) == delim) {
                // Escaped delimiter - include the delimiter as literal
                current.append(delim);
                i++; // Skip the delimiter
            } else if (c == delim) {
                parts.add(current.toString());
                current = new StringBuilder();
            } else {
                // All other characters including backslashes are literal
                current.append(c);
            }
        }
        parts.add(current.toString());

        return parts.toArray(new String[0]);
    }

    /**
     * Join parts with a delimiter, escaping the delimiter in values.
     * Note: VASSAL's SequenceEncoder only escapes the delimiter, not backslashes.
     *
     * @param parts Array of parts
     * @param delim The delimiter character
     * @return Joined string
     */
    protected String join(String[] parts, char delim) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(delim);
            }
            String part = parts[i];
            if (part != null) {
                // Escape only delimiter characters (not backslashes)
                for (int j = 0; j < part.length(); j++) {
                    char c = part.charAt(j);
                    if (c == delim) {
                        sb.append('\\');
                    }
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Get a part from an array safely, returning empty string if out of bounds.
     */
    protected String getPart(String[] parts, int index) {
        return index < parts.length ? parts[index] : "";
    }

    /**
     * Get a part from an array safely, returning default if out of bounds or empty.
     */
    protected String getPart(String[] parts, int index, String defaultValue) {
        if (index >= parts.length) {
            return defaultValue;
        }
        String val = parts[index];
        return (val == null || val.isEmpty()) ? defaultValue : val;
    }

    /**
     * Parse an integer from a string, returning default on error.
     */
    protected int parseInt(String s, int defaultValue) {
        if (s == null || s.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
