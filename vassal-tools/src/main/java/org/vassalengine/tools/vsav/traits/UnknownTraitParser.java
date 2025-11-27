package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Fallback parser for unknown/unrecognized traits.
 * Preserves the raw type and state strings without parsing,
 * allowing for lossless round-trip export/import.
 */
public class UnknownTraitParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "unknown";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData();

        // Extract the actual trait ID from the type segment
        String actualId = typeSegment;
        int idx = typeSegment.indexOf(';');
        if (idx > 0) {
            actualId = typeSegment.substring(0, idx);
        }

        trait.setTraitId(actualId);
        trait.setRawType(typeSegment);
        trait.setRawState(stateSegment);

        // Store the raw strings as properties for visibility in JSON
        trait.setProperty("_unparsed", true);

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        // Simply return the raw strings that were stored
        return new String[] {
            trait.getRawType(),
            trait.getRawState()
        };
    }
}
