package org.vassalengine.tools.vsav;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import VASSAL.build.Builder;
import VASSAL.build.GameModule;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads module definition (.vmod) files to extract prototype definitions
 * and other metadata useful for interpreting saved games.
 *
 * This is used in later phases to expand prototype references.
 */
public class ModuleDefinitionReader {

    private final Map<String, String> prototypes = new HashMap<>();
    private final Map<String, String> mapNames = new HashMap<>();

    /**
     * Read a module definition file.
     *
     * @param vmodPath Path to the .vmod file
     * @throws IOException if file cannot be read
     */
    public void read(String vmodPath) throws IOException {
        try (ZipFile zip = new ZipFile(vmodPath)) {
            // Try buildFile.xml first, then buildFile
            ZipEntry entry = zip.getEntry(GameModule.BUILDFILE);
            if (entry == null) {
                entry = zip.getEntry(GameModule.BUILDFILE_OLD);
            }

            if (entry == null) {
                throw new IOException("Module has no buildFile.xml");
            }

            try (InputStream in = zip.getInputStream(entry)) {
                Document doc = Builder.createDocument(in);
                Element root = doc.getDocumentElement();
                parseElement(root);
            } catch (Exception e) {
                throw new IOException("Failed to parse buildFile: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Recursively parse the build file looking for prototypes and maps.
     */
    private void parseElement(Element element) {
        String tagName = element.getTagName();

        // Look for VASSAL.build.module.PrototypeDefinition
        if ("VASSAL.build.module.PrototypeDefinition".equals(tagName) ||
            "VASSAL.build.widget.PieceSlot".equals(tagName)) {
            String name = element.getAttribute("name");
            // The type definition is in an entryName attribute or as text content
            String definition = element.getAttribute("entryName");
            if (definition.isEmpty()) {
                definition = element.getTextContent();
            }
            if (name != null && !name.isEmpty()) {
                prototypes.put(name, definition);
            }
        }

        // Look for Map components
        if (tagName.contains("Map")) {
            String name = element.getAttribute("mapName");
            if (name == null || name.isEmpty()) {
                name = element.getAttribute("name");
            }
            String id = element.getAttribute("id");
            if (id == null || id.isEmpty()) {
                // Generate an ID from the map name
                id = name;
            }
            if (name != null && !name.isEmpty()) {
                mapNames.put(id, name);
            }
        }

        // Recurse into children
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                parseElement((Element) child);
            }
        }
    }

    /**
     * Get all prototype definitions.
     *
     * @return Map of prototype name to definition string
     */
    public Map<String, String> getPrototypes() {
        return prototypes;
    }

    /**
     * Get a specific prototype definition.
     *
     * @param name Prototype name
     * @return Definition string, or null if not found
     */
    public String getPrototype(String name) {
        return prototypes.get(name);
    }

    /**
     * Get all map names.
     *
     * @return Map of map ID to display name
     */
    public Map<String, String> getMapNames() {
        return mapNames;
    }

    /**
     * Get a specific map name.
     *
     * @param id Map ID
     * @return Display name, or the ID if not found
     */
    public String getMapName(String id) {
        return mapNames.getOrDefault(id, id);
    }
}
