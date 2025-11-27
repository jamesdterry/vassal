package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for Obscurable trait (ID: "obs") - the Mask trait.
 *
 * Type format: obs;[keyCommand];[imageName];[hideCommand];[displayStyle...];[maskName];
 *              [access];[peekCommand];[description];[autoPeekRollover];[dealKey];[dealExpression]
 *
 * The displayStyle field can be:
 * - 'I' for INSET
 * - 'B' for BACKGROUND
 * - '2' for INSET2
 * - 'P' or 'P'+peekKey for PEEK
 * - 'G'+imageName for IMAGE
 *
 * State format: [obscuredBy];[obscuredOptions]
 *
 * The Mask trait hides piece information from some players.
 */
public class ObscurableParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "obs";

    // Display style constants
    private static final char INSET = 'I';
    private static final char BACKGROUND = 'B';
    private static final char PEEK = 'P';
    private static final char IMAGE = 'G';
    private static final char INSET2 = '2';

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        // Parse type: obs;[many parameters...]
        String[] typeParts = split(typeSegment, ';');
        // typeParts[0] is "obs"

        int idx = 1;
        trait.setProperty("keyCommand", getPart(typeParts, idx++));
        trait.setProperty("imageName", getPart(typeParts, idx++));
        trait.setProperty("hideCommand", getPart(typeParts, idx++));

        // Parse display style (can be complex)
        String displayStyleStr = getPart(typeParts, idx++);
        if (!displayStyleStr.isEmpty()) {
            char displayStyle = displayStyleStr.charAt(0);
            trait.setProperty("displayStyle", String.valueOf(displayStyle));

            switch (displayStyle) {
                case PEEK:
                    // P or P+peekKey
                    if (displayStyleStr.length() > 1) {
                        trait.setProperty("peekKey", displayStyleStr.substring(1));
                    }
                    break;
                case IMAGE:
                    // G+imageName
                    if (displayStyleStr.length() > 1) {
                        trait.setProperty("obscuredToOthersImage", displayStyleStr.substring(1));
                    }
                    break;
                default:
                    // INSET, BACKGROUND, INSET2 - no extra data
                    break;
            }
        } else {
            trait.setProperty("displayStyle", String.valueOf(INSET));
        }

        trait.setProperty("maskName", getPart(typeParts, idx++, "?"));
        trait.setProperty("access", getPart(typeParts, idx++));
        trait.setProperty("peekCommand", getPart(typeParts, idx++));
        trait.setProperty("description", getPart(typeParts, idx++));
        trait.setProperty("autoPeekRollover", "true".equals(getPart(typeParts, idx++)));
        trait.setProperty("dealKey", getPart(typeParts, idx++));
        trait.setProperty("dealExpression", getPart(typeParts, idx++));

        // Parse state: [obscuredBy];[obscuredOptions]
        String[] stateParts = split(stateSegment != null ? stateSegment : "", ';');
        String obscuredBy = getPart(stateParts, 0, "null");
        trait.setProperty("obscuredBy", "null".equals(obscuredBy) ? null : obscuredBy);
        trait.setProperty("obscuredOptions", getPart(stateParts, 1));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        // Encode type
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("keyCommand")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("imageName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("hideCommand")));

        // Encode display style
        String displayStyle = trait.getStringProperty("displayStyle");
        if (displayStyle == null || displayStyle.isEmpty()) {
            displayStyle = String.valueOf(INSET);
        }
        char style = displayStyle.charAt(0);
        StringBuilder displayStyleStr = new StringBuilder();
        displayStyleStr.append(style);

        switch (style) {
            case PEEK:
                String peekKey = trait.getStringProperty("peekKey");
                if (peekKey != null && !peekKey.isEmpty()) {
                    displayStyleStr.append(peekKey);
                }
                break;
            case IMAGE:
                String obsImage = trait.getStringProperty("obscuredToOthersImage");
                if (obsImage != null) {
                    displayStyleStr.append(obsImage);
                }
                break;
            default:
                // No extra data
                break;
        }
        type.append(';').append(displayStyleStr);

        type.append(';').append(nullToEmpty(trait.getStringProperty("maskName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("access")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("peekCommand")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(Boolean.TRUE.equals(trait.getProperty("autoPeekRollover")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("dealKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("dealExpression")));

        // Encode state
        StringBuilder state = new StringBuilder();
        String obscuredBy = trait.getStringProperty("obscuredBy");
        state.append(obscuredBy == null ? "null" : obscuredBy);
        state.append(';').append(nullToEmpty(trait.getStringProperty("obscuredOptions")));

        return new String[] { type.toString(), state.toString() };
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}
