package org.vassalengine.tools.vsav.traits;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry mapping trait IDs to their parsers.
 * Parsers are registered by trait ID prefix (e.g., "piece;", "label;", "mark;").
 */
public class TraitParserRegistry {

    private static final TraitParserRegistry INSTANCE = new TraitParserRegistry();

    private final Map<String, AbstractTraitParser> parsers = new HashMap<>();
    private final UnknownTraitParser unknownParser = new UnknownTraitParser();

    private TraitParserRegistry() {
        // Register Core traits (Phase 1)
        register(new BasicPieceParser());
        register(new UsePrototypeParser());
        register(new MarkerParser());
        register(new LabelerParser());
    }

    public static TraitParserRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register a trait parser.
     */
    public void register(AbstractTraitParser parser) {
        parsers.put(parser.getTraitId(), parser);
    }

    /**
     * Get the parser for a given trait ID.
     * Returns UnknownTraitParser if no specific parser is registered.
     *
     * @param traitId The trait ID (e.g., "piece", "label")
     * @return The appropriate parser
     */
    public AbstractTraitParser getParser(String traitId) {
        AbstractTraitParser parser = parsers.get(traitId);
        return parser != null ? parser : unknownParser;
    }

    /**
     * Extract the trait ID from a type segment.
     * The trait ID is the prefix before the first semicolon or the entire segment.
     *
     * @param typeSegment The type string segment (e.g., "piece;cloneKey;...")
     * @return The trait ID (e.g., "piece")
     */
    public String extractTraitId(String typeSegment) {
        if (typeSegment == null || typeSegment.isEmpty()) {
            return "";
        }
        int idx = typeSegment.indexOf(';');
        if (idx > 0) {
            return typeSegment.substring(0, idx);
        }
        return typeSegment;
    }

    /**
     * Check if a parser exists for the given trait ID.
     */
    public boolean hasParser(String traitId) {
        return parsers.containsKey(traitId);
    }

    /**
     * @return The fallback parser for unknown traits
     */
    public UnknownTraitParser getUnknownParser() {
        return unknownParser;
    }
}
