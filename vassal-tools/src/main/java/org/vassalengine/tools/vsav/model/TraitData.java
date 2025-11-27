package org.vassalengine.tools.vsav.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a parsed trait from a piece's type/state definition.
 * Each trait has a type ID (e.g., "piece", "label", "mark") and
 * named properties extracted from the type and state strings.
 */
public class TraitData {
    private String traitId;           // e.g., "piece", "label", "proto", "mark"
    private String rawType;           // Original type string segment
    private String rawState;          // Original state string segment
    private Map<String, Object> properties;  // Parsed properties from type and state

    public TraitData() {
        this.properties = new LinkedHashMap<>();
    }

    public TraitData(String traitId) {
        this();
        this.traitId = traitId;
    }

    public TraitData(String traitId, String rawType, String rawState) {
        this();
        this.traitId = traitId;
        this.rawType = rawType;
        this.rawState = rawState;
    }

    public String getTraitId() {
        return traitId;
    }

    public void setTraitId(String traitId) {
        this.traitId = traitId;
    }

    public String getRawType() {
        return rawType;
    }

    public void setRawType(String rawType) {
        this.rawType = rawType;
    }

    public String getRawState() {
        return rawState;
    }

    public void setRawState(String rawState) {
        this.rawState = rawState;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public String getStringProperty(String key) {
        Object val = properties.get(key);
        return val != null ? val.toString() : null;
    }

    public Integer getIntProperty(String key) {
        Object val = properties.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val instanceof String) {
            try {
                return Integer.parseInt((String) val);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Get an integer property with a default value.
     */
    public int getIntProperty(String key, int defaultValue) {
        Integer val = getIntProperty(key);
        return val != null ? val : defaultValue;
    }

    /**
     * Get a double property.
     */
    public Double getDoubleProperty(String key) {
        Object val = properties.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        if (val instanceof String) {
            try {
                return Double.parseDouble((String) val);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Get a double property with a default value.
     */
    public double getDoubleProperty(String key, double defaultValue) {
        Double val = getDoubleProperty(key);
        return val != null ? val : defaultValue;
    }

    /**
     * Get a boolean property.
     */
    public Boolean getBooleanProperty(String key) {
        Object val = properties.get(key);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        if (val instanceof String) {
            return Boolean.parseBoolean((String) val);
        }
        return null;
    }

    /**
     * Get a boolean property with a default value.
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        Boolean val = getBooleanProperty(key);
        return val != null ? val : defaultValue;
    }
}
