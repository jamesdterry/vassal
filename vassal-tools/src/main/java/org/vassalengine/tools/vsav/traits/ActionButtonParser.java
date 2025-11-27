package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for ActionButton trait.
 *
 * Type format: button;stroke;x;y;width;height;description;launchPopupMenu;useWholeShape;version;npoints;x1;y1;x2;y2;...
 * State format: (empty)
 */
public class ActionButtonParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "button";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("stroke", getPart(typeParts, 1));
        trait.setProperty("x", getPart(typeParts, 2));
        trait.setProperty("y", getPart(typeParts, 3));
        trait.setProperty("width", getPart(typeParts, 4));
        trait.setProperty("height", getPart(typeParts, 5));
        trait.setProperty("description", getPart(typeParts, 6));
        trait.setProperty("launchPopupMenu", getPart(typeParts, 7));
        trait.setProperty("useWholeShape", getPart(typeParts, 8));
        trait.setProperty("version", getPart(typeParts, 9));

        // If version >= 2, there may be polygon points
        if (typeParts.length > 10) {
            trait.setProperty("npoints", getPart(typeParts, 10));
            // Store remaining parts as polygon points
            StringBuilder points = new StringBuilder();
            for (int i = 11; i < typeParts.length; i++) {
                if (i > 11) points.append(';');
                points.append(typeParts[i]);
            }
            trait.setProperty("polygonPoints", points.toString());
        }

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("stroke")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("x")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("y")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("width")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("height")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("launchPopupMenu")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("useWholeShape")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("version")));

        String npoints = trait.getStringProperty("npoints");
        if (npoints != null && !npoints.isEmpty()) {
            type.append(';').append(npoints);
            String polygonPoints = trait.getStringProperty("polygonPoints");
            if (polygonPoints != null && !polygonPoints.isEmpty()) {
                type.append(';').append(polygonPoints);
            }
        }

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
