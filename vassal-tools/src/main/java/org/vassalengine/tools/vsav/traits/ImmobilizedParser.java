package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for Immobilized trait (ID: "immob") - the "Does Not Stack" trait.
 *
 * Type format: immob;[selectionOptions];[movementOption];[stackingOption];[description]
 *
 * Selection options are character flags:
 *   'i' = Shift to select
 *   't' = Ctrl to select
 *   'c' = Alt to select
 *   'n' = Never select
 *   'g' = Ignore grid
 *   'A' = Alt to band select
 *   'B' = Alt+Shift to band select
 *   'Z' = Never band select
 *
 * Movement option (single char):
 *   'I' = Move if selected
 *   'N' = Move normally
 *   'V' = Never move
 *
 * Stacking option (single char):
 *   'L' = Stack normally
 *   'R' = Never stack
 *
 * State format: (empty)
 *
 * This trait controls whether a piece can be selected, moved, or stacked.
 */
public class ImmobilizedParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "immob";

    // Selection option constants
    private static final char SHIFT_SELECT = 'i';
    private static final char CTRL_SELECT = 't';
    private static final char ALT_SELECT = 'c';
    private static final char NEVER_SELECT = 'n';
    private static final char IGNORE_GRID = 'g';
    private static final char ALT_BAND_SELECT = 'A';
    private static final char ALT_SHIFT_BAND_SELECT = 'B';
    private static final char NEVER_BAND_SELECT = 'Z';

    // Movement option constants
    private static final char MOVE_SELECTED = 'I';
    private static final char MOVE_NORMAL = 'N';
    private static final char NEVER_MOVE = 'V';

    // Stacking option constants
    private static final char STACK_NORMAL = 'L';
    private static final char NEVER_STACK = 'R';

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        // Parse type: immob;[selectionOptions];[movementOption];[stackingOption];[description]
        String[] typeParts = split(typeSegment, ';');
        // typeParts[0] is "immob"

        String selectionOptions = getPart(typeParts, 1);
        String movementOptions = getPart(typeParts, 2);
        String stackingOptions = getPart(typeParts, 3, String.valueOf(NEVER_STACK));
        String description = getPart(typeParts, 4);

        // Parse selection options
        trait.setProperty("shiftToSelect", selectionOptions.indexOf(SHIFT_SELECT) >= 0);
        trait.setProperty("ctrlToSelect", selectionOptions.indexOf(CTRL_SELECT) >= 0);
        trait.setProperty("altToSelect", selectionOptions.indexOf(ALT_SELECT) >= 0);
        trait.setProperty("neverSelect", selectionOptions.indexOf(NEVER_SELECT) >= 0);
        trait.setProperty("ignoreGrid", selectionOptions.indexOf(IGNORE_GRID) >= 0);
        trait.setProperty("altToBandSelect", selectionOptions.indexOf(ALT_BAND_SELECT) >= 0);
        trait.setProperty("altShiftToBandSelect", selectionOptions.indexOf(ALT_SHIFT_BAND_SELECT) >= 0);
        trait.setProperty("neverBandSelect", selectionOptions.indexOf(NEVER_BAND_SELECT) >= 0);

        // Parse movement options
        if (!movementOptions.isEmpty()) {
            char moveOpt = movementOptions.charAt(0);
            trait.setProperty("neverMove", moveOpt == NEVER_MOVE);
            trait.setProperty("moveIfSelected", moveOpt == MOVE_SELECTED);
        } else {
            trait.setProperty("neverMove", false);
            trait.setProperty("moveIfSelected", false);
        }

        // Parse stacking options
        if (!stackingOptions.isEmpty()) {
            char stackOpt = stackingOptions.charAt(0);
            trait.setProperty("canStack", stackOpt == STACK_NORMAL);
        } else {
            trait.setProperty("canStack", false);
        }

        trait.setProperty("description", description);

        // State is empty
        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        // Encode type
        StringBuilder type = new StringBuilder(TRAIT_ID);

        // Selection options
        StringBuilder selectionOptions = new StringBuilder();
        if (Boolean.TRUE.equals(trait.getProperty("neverSelect"))) {
            selectionOptions.append(NEVER_SELECT);
        } else if (Boolean.TRUE.equals(trait.getProperty("shiftToSelect"))) {
            selectionOptions.append(SHIFT_SELECT);
        } else if (Boolean.TRUE.equals(trait.getProperty("ctrlToSelect"))) {
            selectionOptions.append(CTRL_SELECT);
        } else if (Boolean.TRUE.equals(trait.getProperty("altToSelect"))) {
            selectionOptions.append(ALT_SELECT);
        }
        if (Boolean.TRUE.equals(trait.getProperty("ignoreGrid"))) {
            selectionOptions.append(IGNORE_GRID);
        }
        if (Boolean.TRUE.equals(trait.getProperty("neverBandSelect"))) {
            selectionOptions.append(NEVER_BAND_SELECT);
        } else if (Boolean.TRUE.equals(trait.getProperty("altToBandSelect"))) {
            selectionOptions.append(ALT_BAND_SELECT);
        } else if (Boolean.TRUE.equals(trait.getProperty("altShiftToBandSelect"))) {
            selectionOptions.append(ALT_SHIFT_BAND_SELECT);
        }
        type.append(';').append(selectionOptions);

        // Movement option
        char moveOpt;
        if (Boolean.TRUE.equals(trait.getProperty("neverMove"))) {
            moveOpt = NEVER_MOVE;
        } else if (Boolean.TRUE.equals(trait.getProperty("moveIfSelected"))) {
            moveOpt = MOVE_SELECTED;
        } else {
            moveOpt = MOVE_NORMAL;
        }
        type.append(';').append(moveOpt);

        // Stacking option
        char stackOpt;
        if (Boolean.TRUE.equals(trait.getProperty("canStack"))) {
            stackOpt = STACK_NORMAL;
        } else {
            stackOpt = NEVER_STACK;
        }
        type.append(';').append(stackOpt);

        // Description
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));

        // State is empty
        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}
