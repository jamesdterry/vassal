package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for Footprint trait (Movement Trail).
 *
 * Grammar from VASSAL 3.8.0 counters/Footprint.java (myGetType / myGetState):
 *
 * Type format: footprint;trailKey;menuCommand;initiallyVisible;globallyVisible;circleRadius;
 *              fillColor;lineColor;selectedTransparency;unSelectedTransparency;edgePointBuffer;
 *              edgeDisplayBuffer;lineWidth;trailKeyOn;trailKeyOff;trailKeyClear;description
 *              [;ignoreSameLocation]                                       (3.8.0+)
 * State format: globalVisibility;startMapId;pointCount;x,y;...;everInitialized
 *               [;lastPointListSize]                                       (3.8.0+)
 *
 * The 3.8.0 fields are only written back when the source had them, so 3.7 saves
 * round-trip unchanged.
 */
public class FootprintParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "footprint";

    private static final String[] TYPE_FIELDS = {
        "trailKey", "menuCommand", "initiallyVisible", "globallyVisible", "circleRadius",
        "fillColor", "lineColor", "selectedTransparency", "unSelectedTransparency",
        "edgePointBuffer", "edgeDisplayBuffer", "lineWidth",
        "trailKeyOn", "trailKeyOff", "trailKeyClear", "description"
    };

    /** Type field added in 3.8.0, after description */
    private static final String IGNORE_SAME_LOCATION = "ignoreSameLocation";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');
        for (int i = 0; i < TYPE_FIELDS.length; i++) {
            trait.setProperty(TYPE_FIELDS[i], getPart(typeParts, i + 1));
        }
        if (typeParts.length > TYPE_FIELDS.length + 1) {
            trait.setProperty(IGNORE_SAME_LOCATION, getPart(typeParts, TYPE_FIELDS.length + 1));
        }

        if (stateSegment != null && !stateSegment.isEmpty()) {
            String[] stateParts = split(stateSegment, ';');
            int count = parseInt(getPart(stateParts, 2), -1);

            if (count < 0 || stateParts.length < 3 + count) {
                // Not the documented layout: keep it verbatim so it round-trips
                trait.setProperty("state.raw", stateSegment);
                return trait;
            }

            trait.setProperty("state.globalVisibility", getPart(stateParts, 0));
            trait.setProperty("state.startMapId", getPart(stateParts, 1));

            StringBuilder points = new StringBuilder();
            for (int i = 0; i < count; i++) {
                if (i > 0) points.append(';');
                points.append(stateParts[3 + i]);
            }
            trait.setProperty("state.points", points.toString());

            int tail = 3 + count;
            if (stateParts.length > tail) {
                trait.setProperty("state.everInitialized", stateParts[tail]);
            }
            if (stateParts.length > tail + 1) {
                trait.setProperty("state.lastPointListSize", stateParts[tail + 1]);
            }
        }

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        for (String field : TYPE_FIELDS) {
            type.append(';').append(nullToEmpty(trait.getStringProperty(field)));
        }
        String ignoreSameLocation = trait.getStringProperty(IGNORE_SAME_LOCATION);
        if (ignoreSameLocation != null) {
            type.append(';').append(ignoreSameLocation);
        }

        String raw = trait.getStringProperty("state.raw");
        if (raw != null) {
            return new String[] { type.toString(), raw };
        }

        StringBuilder state = new StringBuilder();
        if (trait.getStringProperty("state.globalVisibility") != null) {
            String points = nullToEmpty(trait.getStringProperty("state.points"));
            String[] pointList = points.isEmpty() ? new String[0] : points.split(";");

            state.append(nullToEmpty(trait.getStringProperty("state.globalVisibility")));
            state.append(';').append(nullToEmpty(trait.getStringProperty("state.startMapId")));
            state.append(';').append(pointList.length);
            for (String point : pointList) {
                state.append(';').append(point);
            }

            String everInitialized = trait.getStringProperty("state.everInitialized");
            if (everInitialized != null) {
                state.append(';').append(everInitialized);
                String lastPointListSize = trait.getStringProperty("state.lastPointListSize");
                if (lastPointListSize != null) {
                    state.append(';').append(lastPointListSize);
                }
            }
        }

        return new String[] { type.toString(), state.toString() };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
