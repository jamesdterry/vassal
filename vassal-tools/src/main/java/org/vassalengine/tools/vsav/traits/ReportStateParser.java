package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for ReportState trait (Report Action).
 *
 * Type format: report;[keys];[reportFormat];[cycleDownKeys];[cycleReportFormat];[description];[noSuppress]
 * State format: [cycleIndex] (integer, -1 if not cycling)
 */
public class ReportStateParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "report";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        // Parse type: report;keys;reportFormat;cycleDownKeys;cycleReportFormat;description;noSuppress
        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("keys", getPart(typeParts, 1));
        trait.setProperty("reportFormat", getPart(typeParts, 2));
        trait.setProperty("cycleDownKeys", getPart(typeParts, 3));
        trait.setProperty("cycleReportFormat", getPart(typeParts, 4));
        trait.setProperty("description", getPart(typeParts, 5));
        trait.setProperty("noSuppress", getPart(typeParts, 6));

        // Parse state: cycleIndex
        if (stateSegment != null && !stateSegment.isEmpty()) {
            trait.setProperty("cycleIndex", parseInt(stateSegment, -1));
        } else {
            trait.setProperty("cycleIndex", -1);
        }

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        // Encode type
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("keys")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("reportFormat")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("cycleDownKeys")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("cycleReportFormat")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("noSuppress")));

        // Encode state
        int cycleIndex = trait.getIntProperty("cycleIndex", -1);
        String state = String.valueOf(cycleIndex);

        return new String[] { type.toString(), state };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
