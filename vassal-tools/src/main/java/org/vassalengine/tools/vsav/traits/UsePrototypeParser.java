package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for UsePrototype trait (ID: "prototype").
 *
 * Type format: prototype;[prototypeName];[key1=val1,key2=val2,...]
 * State format: (empty)
 *
 * Note: The trait ID in VASSAL source is "prototype;" but in saved games
 * it appears as just "prototype" followed by the prototype name.
 */
public class UsePrototypeParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "prototype";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        // Parse type: prototype;[prototypeName];[properties]
        String[] typeParts = split(typeSegment, ';');
        // typeParts[0] is "prototype"
        String prototypeName = getPart(typeParts, 1);
        trait.setProperty("prototypeName", prototypeName);

        // Optional properties (key=val,key=val,...)
        if (typeParts.length > 2) {
            String propsStr = getPart(typeParts, 2);
            if (!propsStr.isEmpty()) {
                trait.setProperty("prototypeProperties", propsStr);
            }
        }

        // State is typically empty for UsePrototype
        // but preserve it if present
        if (stateSegment != null && !stateSegment.isEmpty()) {
            trait.setProperty("state", stateSegment);
        }

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        // Encode type
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("prototypeName")));

        String props = trait.getStringProperty("prototypeProperties");
        if (props != null && !props.isEmpty()) {
            type.append(';').append(props);
        }

        // Encode state (typically empty)
        String state = trait.getStringProperty("state");
        if (state == null) {
            state = "";
        }

        return new String[] { type.toString(), state };
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}
