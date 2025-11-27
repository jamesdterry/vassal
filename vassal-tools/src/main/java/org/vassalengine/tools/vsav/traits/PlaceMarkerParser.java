package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

/**
 * Parser for PlaceMarker trait.
 *
 * Type format: placemark;commandName;key;markerSpec;markerText;xOffset;yOffset;matchRotation;afterBurnerKey;description;gpId;placement;above;copyDPsByName;parameterList;version
 * State format: (empty)
 */
public class PlaceMarkerParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "placemark";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        String[] typeParts = split(typeSegment, ';');

        trait.setProperty("commandName", getPart(typeParts, 1));
        trait.setProperty("key", getPart(typeParts, 2));
        trait.setProperty("markerSpec", getPart(typeParts, 3));
        trait.setProperty("markerText", getPart(typeParts, 4));
        trait.setProperty("xOffset", getPart(typeParts, 5));
        trait.setProperty("yOffset", getPart(typeParts, 6));
        trait.setProperty("matchRotation", getPart(typeParts, 7));
        trait.setProperty("afterBurnerKey", getPart(typeParts, 8));
        trait.setProperty("description", getPart(typeParts, 9));
        trait.setProperty("gpId", getPart(typeParts, 10));
        trait.setProperty("placement", getPart(typeParts, 11));
        trait.setProperty("above", getPart(typeParts, 12));
        trait.setProperty("copyDPsByName", getPart(typeParts, 13));
        trait.setProperty("parameterList", getPart(typeParts, 14));
        trait.setProperty("version", getPart(typeParts, 15));

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        StringBuilder type = new StringBuilder(TRAIT_ID);
        type.append(';').append(nullToEmpty(trait.getStringProperty("commandName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("key")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("markerSpec")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("markerText")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("xOffset")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("yOffset")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("matchRotation")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("afterBurnerKey")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("gpId")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("placement")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("above")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("copyDPsByName")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("parameterList")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("version")));

        return new String[] { type.toString(), "" };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
