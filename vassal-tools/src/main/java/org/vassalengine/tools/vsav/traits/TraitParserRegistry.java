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

        // Register High Priority traits (Phase 2)
        register(new EmbellishmentParser());  // emb2 - Layer (modern format)
        register(new ObscurableParser());     // obs - Mask
        register(new HideableParser());       // hide - Invisible
        register(new PropertySheetParser());  // propertysheet
        register(new ImmobilizedParser());    // immob - Does Not Stack

        // Register Medium Priority traits (Phase 3)
        register(new ReportStateParser());          // report - Report Action
        register(new MovementMarkableParser());     // markmoved - Mark When Moved
        register(new FreeRotatorParser());          // rotate - Can Rotate
        register(new PivotParser());                // pivot - Can Pivot
        register(new RestrictedParser());           // restrict - Restricted Access
        register(new DeleteParser());               // delete - Delete
        register(new CloneParser());                // clone - Clone
        register(new MatParser());                  // mat - Mat
        register(new MatCargoParser());             // matPiece - Mat Cargo
        register(new PlaceMarkerParser());          // placemark - Place Marker
        register(new ReplaceParser());              // replace - Replace With Other
        register(new CalculatedPropertyParser());   // calcProp - Calculated Property
        register(new SetGlobalPropertyParser());    // setprop - Set Global Property
        register(new SendToLocationParser());       // sendto - Send to Location
        register(new ReturnToDeckParser());         // return - Return to Deck

        // Register Low Priority traits (Phase 4)
        register(new TriggerActionParser());            // macro - Trigger Action
        register(new ActionButtonParser());             // button - Action Button
        register(new MenuSeparatorParser());            // menuSeparator - Menu Separator
        register(new GlobalHotKeyParser());             // globalhotkey - Global Hotkey
        register(new SubMenuParser());                  // submenu - Sub-Menu
        register(new DeselectParser());                 // deselect - Deselect
        register(new NonRectangularParser());           // nonRect2 - Non-Rectangular
        register(new FootprintParser());                // footprint - Movement Trail
        register(new AreaOfEffectParser());             // AreaOfEffect - Area of Effect
        register(new RestrictCommandsParser());         // hideCmd - Restrict Commands
        register(new DynamicPropertyParser());          // PROP - Dynamic Property
        register(new PlaySoundParser());                // playSound - Play Sound
        register(new AttachmentParser());               // attach - Attachment
        register(new TranslateParser());                // translate - Move Fixed Distance
        register(new CounterGlobalKeyCommandParser());  // globalkey - Global Key Command
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
