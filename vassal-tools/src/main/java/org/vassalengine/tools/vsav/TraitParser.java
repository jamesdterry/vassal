package org.vassalengine.tools.vsav;

import org.vassalengine.tools.vsav.model.TraitData;
import org.vassalengine.tools.vsav.traits.AbstractTraitParser;
import org.vassalengine.tools.vsav.traits.TraitParserRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses piece type and state strings into a list of TraitData objects.
 *
 * In VASSAL, pieces are built as a decorator chain where each trait wraps
 * the next. The type and state strings are tab-separated, with each segment
 * corresponding to one trait in the chain.
 *
 * The order is from outermost to innermost trait, with BasicPiece always
 * being the innermost (last) trait.
 */
public class TraitParser {

    private static final char TAB = '\t';
    private final TraitParserRegistry registry;

    public TraitParser() {
        this.registry = TraitParserRegistry.getInstance();
    }

    /**
     * Parse piece type and state strings into a list of traits.
     *
     * @param type  The type string (tab-separated trait type segments)
     * @param state The state string (tab-separated trait state segments)
     * @return List of parsed TraitData objects, from outer to inner
     */
    public List<TraitData> parseTraits(String type, String state) {
        List<TraitData> traits = new ArrayList<>();

        if (type == null || type.isEmpty()) {
            return traits;
        }

        // Split by tabs
        String[] typeSegments = splitByTab(type);
        String[] stateSegments = splitByTab(state != null ? state : "");

        // Parse each segment
        for (int i = 0; i < typeSegments.length; i++) {
            String typeSegment = typeSegments[i];
            String stateSegment = i < stateSegments.length ? stateSegments[i] : "";

            TraitData trait = parseTrait(typeSegment, stateSegment);
            if (trait != null) {
                traits.add(trait);
            }
        }

        return traits;
    }

    /**
     * Parse a single trait from its type and state segments.
     */
    public TraitData parseTrait(String typeSegment, String stateSegment) {
        if (typeSegment == null || typeSegment.isEmpty()) {
            return null;
        }

        // Extract the trait ID
        String traitId = registry.extractTraitId(typeSegment);

        // Get the appropriate parser
        AbstractTraitParser parser = registry.getParser(traitId);

        // Parse and return
        return parser.parse(typeSegment, stateSegment);
    }

    /**
     * Encode a list of traits back into type and state strings.
     *
     * @param traits List of TraitData objects
     * @return Two-element array: [type, state]
     */
    public String[] encodeTraits(List<TraitData> traits) {
        if (traits == null || traits.isEmpty()) {
            return new String[] { "", "" };
        }

        StringBuilder type = new StringBuilder();
        StringBuilder state = new StringBuilder();

        for (int i = 0; i < traits.size(); i++) {
            TraitData trait = traits.get(i);

            // Get the parser for this trait
            String traitId = trait.getTraitId();
            AbstractTraitParser parser = registry.getParser(traitId);

            // Encode
            String[] encoded = parser.encode(trait);

            if (i > 0) {
                type.append(TAB);
                state.append(TAB);
            }
            type.append(encoded[0]);
            state.append(encoded[1]);
        }

        return new String[] { type.toString(), state.toString() };
    }

    /**
     * Split a string by tab character.
     * Does NOT handle escape sequences - tabs in VASSAL type/state strings
     * are literal separators.
     */
    private String[] splitByTab(String s) {
        if (s == null || s.isEmpty()) {
            return new String[0];
        }

        List<String> parts = new ArrayList<>();
        int start = 0;

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == TAB) {
                parts.add(s.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(s.substring(start));

        return parts.toArray(new String[0]);
    }
}
