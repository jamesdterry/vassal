package org.vassalengine.tools.vsav.traits;

import org.vassalengine.tools.vsav.model.TraitData;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for PropertySheet trait (ID: "propertysheet").
 *
 * Type format: propertysheet;[definition];[menuName];[empty];[commitStyle];[red];[green];[blue];[launchKeyStroke];[description]
 *
 * The definition field contains property definitions encoded as ~-separated values,
 * where each value is a type digit followed by the property name.
 * Type digits:
 *   0 = Text Field
 *   1 = Multi-line text
 *   2 = Label Only
 *   3 = Tick Marks
 *   4 = Tick Marks with Max Field
 *   5 = Tick Marks with Value Field
 *   6 = Tick Marks with Value &amp; Max
 *   7 = Spinner
 *
 * State format: values separated by ~ (tilde)
 *
 * The Property Sheet trait provides a dialog for editing custom properties on a piece.
 */
public class PropertySheetParser extends AbstractTraitParser {

    public static final String TRAIT_ID = "propertysheet";

    @Override
    public String getTraitId() {
        return TRAIT_ID;
    }

    @Override
    public TraitData parse(String typeSegment, String stateSegment) {
        TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);

        // Parse type: propertysheet;[parameters...]
        String[] typeParts = split(typeSegment, ';');
        // typeParts[0] is "propertysheet"

        int idx = 1;
        String definition = getPart(typeParts, idx++);
        trait.setProperty("definition", definition);
        trait.setProperty("menuName", getPart(typeParts, idx++));
        idx++; // Skip empty field (legacy keystroke position)
        trait.setProperty("commitStyle", parseInt(getPart(typeParts, idx++), 0));

        // RGB components for background color (optional)
        String red = getPart(typeParts, idx++);
        String green = getPart(typeParts, idx++);
        String blue = getPart(typeParts, idx++);

        if (!red.isEmpty() && !green.isEmpty() && !blue.isEmpty()) {
            trait.setProperty("bgColorRed", parseInt(red, 0));
            trait.setProperty("bgColorGreen", parseInt(green, 0));
            trait.setProperty("bgColorBlue", parseInt(blue, 0));
        }

        trait.setProperty("launchKeyStroke", getPart(typeParts, idx++));
        trait.setProperty("description", getPart(typeParts, idx++));

        // Parse definition to extract property definitions
        List<Object> properties = parseDefinition(definition);
        trait.setProperty("properties", properties);

        // Parse state: values separated by ~
        String[] stateValues = split(stateSegment != null ? stateSegment : "", '~');
        List<String> values = new ArrayList<>();
        for (String val : stateValues) {
            values.add(val);
        }
        trait.setProperty("values", values);

        return trait;
    }

    @Override
    public String[] encode(TraitData trait) {
        // Encode type
        StringBuilder type = new StringBuilder(TRAIT_ID);

        // Reconstruct definition from properties if needed
        String definition = trait.getStringProperty("definition");
        if (definition == null) {
            definition = encodeDefinition(trait);
        }
        type.append(';').append(nullToEmpty(definition));
        type.append(';').append(nullToEmpty(trait.getStringProperty("menuName")));
        type.append(';'); // Empty field for legacy
        type.append(';').append(getIntOrDefault(trait, "commitStyle", 0));

        // RGB components
        Integer red = trait.getIntProperty("bgColorRed");
        Integer green = trait.getIntProperty("bgColorGreen");
        Integer blue = trait.getIntProperty("bgColorBlue");
        if (red != null && green != null && blue != null) {
            type.append(';').append(red);
            type.append(';').append(green);
            type.append(';').append(blue);
        } else {
            type.append(';').append("");
            type.append(';').append("");
            type.append(';').append("");
        }

        type.append(';').append(nullToEmpty(trait.getStringProperty("launchKeyStroke")));
        type.append(';').append(nullToEmpty(trait.getStringProperty("description")));

        // Encode state: values joined by ~
        StringBuilder state = new StringBuilder();
        Object valuesObj = trait.getProperty("values");
        if (valuesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> values = (List<String>) valuesObj;
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    state.append('~');
                }
                state.append(nullToEmpty(values.get(i)));
            }
        }

        return new String[] { type.toString(), state.toString() };
    }

    /**
     * Parse the definition string to extract property names and types.
     * Definition format: type1name1~type2name2~...
     * where type is a single digit (0-7) and name is the property name.
     */
    private List<Object> parseDefinition(String definition) {
        List<Object> properties = new ArrayList<>();
        if (definition == null || definition.isEmpty()) {
            return properties;
        }

        String[] parts = split(definition, '~');
        for (String part : parts) {
            if (part.length() > 0) {
                PropertyDef prop = new PropertyDef();
                prop.type = parseInt(String.valueOf(part.charAt(0)), 0);
                prop.name = part.length() > 1 ? part.substring(1) : "";
                properties.add(prop);
            }
        }
        return properties;
    }

    /**
     * Reconstruct definition string from properties list.
     */
    private String encodeDefinition(TraitData trait) {
        Object propsObj = trait.getProperty("properties");
        if (!(propsObj instanceof List)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        @SuppressWarnings("unchecked")
        List<Object> props = (List<Object>) propsObj;
        for (int i = 0; i < props.size(); i++) {
            if (i > 0) {
                sb.append('~');
            }
            Object prop = props.get(i);
            if (prop instanceof PropertyDef) {
                PropertyDef pd = (PropertyDef) prop;
                sb.append(pd.type);
                sb.append(nullToEmpty(pd.name));
            }
        }
        return sb.toString();
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private int getIntOrDefault(TraitData trait, String key, int defaultValue) {
        Integer val = trait.getIntProperty(key);
        return val != null ? val : defaultValue;
    }

    /**
     * Helper class to represent a property definition.
     */
    public static class PropertyDef {
        public int type;
        public String name;
    }
}
