package org.vassalengine.tools.vsav;

import org.vassalengine.tools.vsav.model.TraitData;
import org.vassalengine.tools.vsav.traits.AbstractTraitParser;
import org.vassalengine.tools.vsav.traits.TraitParserRegistry;
import VASSAL.tools.SequenceEncoder;

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
     * Uses recursive parsing to handle VASSAL's nested SequenceEncoder format.
     *
     * VASSAL encodes pieces recursively: each trait's getType()/getState() wraps
     * the inner piece's encoded data, escaping any embedded tabs. So we must
     * decode recursively to get each trait's individual type/state.
     *
     * @param type  The type string (recursively tab-encoded trait types)
     * @param state The state string (recursively tab-encoded trait states)
     * @return List of parsed TraitData objects, from outer to inner
     */
    public List<TraitData> parseTraits(String type, String state) {
        List<TraitData> traits = new ArrayList<>();
        parseTraitsRecursive(type, state, traits);
        return traits;
    }

    /**
     * Recursively parse traits from type and state strings.
     *
     * IMPORTANT: VASSAL's encoding is recursive - each trait's type/state is:
     *   myType/myState TAB innerPiece.type/state
     *
     * The SequenceEncoder escapes any tabs in the inner data, so when we decode:
     * - First token = this trait's own type/state
     * - Second token = inner piece's ALREADY-DECODED type/state (with embedded tabs)
     *
     * We must use nextToken() to get properly decoded data, not getRemaining()
     * which returns raw escaped data.
     */
    private void parseTraitsRecursive(String type, String state, List<TraitData> traits) {
        if (type == null || type.isEmpty()) {
            return;
        }

        // Use SequenceEncoder.Decoder to get the outer trait and inner encoded data
        SequenceEncoder.Decoder typeDecoder = new SequenceEncoder.Decoder(type, TAB);
        SequenceEncoder.Decoder stateDecoder = new SequenceEncoder.Decoder(state != null ? state : "", TAB);

        // First token is this trait's type/state
        String thisType = typeDecoder.hasMoreTokens() ? typeDecoder.nextToken() : "";
        String thisState = stateDecoder.hasMoreTokens() ? stateDecoder.nextToken() : "";

        // Parse this trait
        TraitData trait = parseTrait(thisType, thisState);
        if (trait != null) {
            traits.add(trait);
        }

        // Second token is the inner piece's encoded type/state (already decoded by nextToken)
        // This contains all inner traits with their proper escaping preserved
        String innerType = typeDecoder.hasMoreTokens() ? typeDecoder.nextToken() : null;
        String innerState = stateDecoder.hasMoreTokens() ? stateDecoder.nextToken() : null;

        // Recursively parse inner traits
        if (innerType != null && !innerType.isEmpty()) {
            parseTraitsRecursive(innerType, innerState, traits);
        }
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
     * Uses recursive encoding to match VASSAL's nested SequenceEncoder format.
     *
     * @param traits List of TraitData objects (from outer to inner)
     * @return Two-element array: [type, state]
     */
    public String[] encodeTraits(List<TraitData> traits) {
        if (traits == null || traits.isEmpty()) {
            return new String[] { "", "" };
        }

        return encodeTraitsRecursive(traits, 0);
    }

    /**
     * Recursively encode traits starting from the given index.
     * Each trait wraps the inner traits' encoded data using SequenceEncoder.
     */
    private String[] encodeTraitsRecursive(List<TraitData> traits, int index) {
        if (index >= traits.size()) {
            return new String[] { "", "" };
        }

        TraitData trait = traits.get(index);

        // Get the parser for this trait
        String traitId = trait.getTraitId();
        AbstractTraitParser parser = registry.getParser(traitId);

        // Encode this trait's type and state
        String[] thisEncoded = parser.encode(trait);

        // If this is the innermost trait, just return its encoding
        if (index == traits.size() - 1) {
            return thisEncoded;
        }

        // Otherwise, recursively encode the inner traits and wrap them
        String[] innerEncoded = encodeTraitsRecursive(traits, index + 1);

        // Use SequenceEncoder to join this trait's data with inner traits' data
        SequenceEncoder typeEncoder = new SequenceEncoder(thisEncoded[0], TAB);
        typeEncoder.append(innerEncoded[0]);

        SequenceEncoder stateEncoder = new SequenceEncoder(thisEncoded[1], TAB);
        stateEncoder.append(innerEncoded[1]);

        return new String[] { typeEncoder.getValue(), stateEncoder.getValue() };
    }

}
