package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for AreaOfEffect trait.
 *
 * Type format: AreaOfEffect;transparencyColor;transparencyLevel;radius;alwaysActive;activateCommand;
 *              activateKey;mapShaderName;fixedRadius;radiusMarker;description;onMenuText;onKey;
 *              offMenuText;offKey;globallyVisible
 * State format: active (boolean)
 */
public class AreaOfEffectParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "AreaOfEffect";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("transparencyColor", getPart(typeParts, 1));
        trait.setProperty("transparencyLevel", getPart(typeParts, 2));
        trait.setProperty("radius", getPart(typeParts, 3));
        trait.setProperty("alwaysActive", getPart(typeParts, 4));
        trait.setProperty("activateCommand", getPart(typeParts, 5));
        trait.setProperty("activateKey", getPart(typeParts, 6));
        trait.setProperty("mapShaderName", getPart(typeParts, 7));
        trait.setProperty("fixedRadius", getPart(typeParts, 8));
        trait.setProperty("radiusMarker", getPart(typeParts, 9));
        trait.setProperty("description", getPart(typeParts, 10));
        trait.setProperty("onMenuText", getPart(typeParts, 11));
        trait.setProperty("onKey", getPart(typeParts, 12));
        trait.setProperty("offMenuText", getPart(typeParts, 13));
        trait.setProperty("offKey", getPart(typeParts, 14));
        trait.setProperty("globallyVisible", getPart(typeParts, 15));

        // State: active
        if (stateSegment != null && !stateSegment.isEmpty()) {
            trait.setProperty("state.active", stateSegment);
        }

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("transparencyColor")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("transparencyLevel")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("radius")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("alwaysActive")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("activateCommand")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("activateKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("mapShaderName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("fixedRadius")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("radiusMarker")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("onMenuText")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("onKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("offMenuText")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("offKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("globallyVisible")));

        String state = nullToEmpty(trait.getStringProperty("state.active"));

        return new String[] { type.toString(), state };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
