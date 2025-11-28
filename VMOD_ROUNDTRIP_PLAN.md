# VMOD Round-Trip Implementation Plan

## Summary

Extend `ModuleInspector` with `export` and `import` commands to enable full VMOD round-tripping. The JSON format will include both structured component data (for programmatic editing) and raw XML (for perfect fidelity fallback).

## Design Decisions

1. **Extend ModuleInspector** - Add export/import commands rather than creating a new tool
2. **Dual representation** - JSON includes both parsed `ComponentData` tree AND raw `buildFileXml` string
3. **Import precedence** - On import, use structured components if present; fall back to raw XML if components are null/removed
4. **External resources** - Binary files (images/sounds/icons) exported to directory alongside JSON

## JSON Schema

```json
{
  "formatVersion": "1.0",
  "fileType": "vmod",
  "metadata": {
    "name": "Module Name",
    "version": "1.0",
    "description": "Description",
    "vassalVersion": "3.7.0"
  },
  "buildFileName": "buildFile.xml",
  "buildFileXml": "<?xml version=\"1.0\"?>...",
  "rootComponent": {
    "type": "VASSAL.build.GameModule",
    "name": "Module Name",
    "attributes": { "name": "Module Name", "version": "1.0" },
    "children": [
      {
        "type": "VASSAL.build.module.Map",
        "name": "Main Map",
        "attributes": { "name": "Main Map" },
        "children": []
      }
    ]
  },
  "resources": [
    { "path": "images/piece.png", "size": 12345 }
  ]
}
```

## Export Directory Structure

```
module_export/
  module.json           # Metadata + components + raw XML + resource manifest
  images/               # Binary resources preserved
  sounds/
  icons/
  [other files]
```

## CLI Commands

```bash
# Existing (unchanged)
vassal-inspector module.vmod              # Summary
vassal-inspector --json module.vmod       # JSON output (inspection only)

# New commands
vassal-inspector export -o ./output module.vmod    # Export to directory
vassal-inspector import -o new.vmod ./input        # Import from directory
```

## Files to Modify

| File | Changes |
|------|---------|
| `vassal-tools/.../ModuleInspector.java` | Add export/import commands, new data classes |

## Implementation Steps

### Step 1: Add Data Classes (inner classes in ModuleInspector.java)

```java
// Root export container
static class VmodExportData {
    String formatVersion = "1.0";
    String fileType = "vmod";
    Map<String, String> metadata;      // name, version, description, vassalVersion
    String buildFileName;              // "buildFile.xml" or "buildFile"
    String buildFileXml;               // Raw XML for perfect round-trip
    ComponentData rootComponent;       // Parsed tree for programmatic editing
    List<ResourceEntry> resources;
}

// Structured component (recursive tree)
static class ComponentData {
    String type;                       // Java class name (XML tag)
    String name;                       // name attribute (convenience)
    Map<String, String> attributes;    // All attributes (LinkedHashMap for order)
    List<ComponentData> children;      // Child components
}

// Resource reference
static class ResourceEntry {
    String path;                       // Archive path (e.g., "images/piece.png")
    long size;
}
```

### Step 2: Add Command Routing

Modify `run()` method to detect subcommands:

```java
public int run(String[] args) {
    if (args.length > 0) {
        String cmd = args[0].toLowerCase();
        if ("export".equals(cmd)) {
            return runExport(Arrays.copyOfRange(args, 1, args.length));
        } else if ("import".equals(cmd)) {
            return runImport(Arrays.copyOfRange(args, 1, args.length));
        }
    }
    // ... existing inspection logic
}
```

### Step 3: Implement Export Command

```java
private int runExport(String[] args) {
    // Parse options: -o/--output <dir>
    // 1. Read VMOD with ZipFile
    // 2. Read moduledata -> metadata map
    // 3. Read buildFile.xml -> both raw string AND parsed ComponentData tree
    // 4. Catalog resources -> ResourceEntry list
    // 5. Extract resources to output directory
    // 6. Write module.json with Gson
    return 0;
}
```

Key implementation details:
- Use `Builder.createDocument()` to parse XML
- Recursively parse XML elements into ComponentData tree
- Preserve raw XML string separately for fallback
- Use `LinkedHashMap` to preserve attribute order

### Step 4: Implement Import Command

```java
private int runImport(String[] args) {
    // Parse options: -o/--output <file.vmod>
    // 1. Read module.json with Gson
    // 2. Determine XML source:
    //    - If rootComponent != null: encode ComponentData -> XML
    //    - Else: use buildFileXml directly
    // 3. Create VMOD with ZipArchive
    // 4. Write moduledata XML
    // 5. Write buildFile.xml
    // 6. Copy resources from input directory
    return 0;
}
```

Key implementation details:
- ComponentData -> XML encoding uses `Builder.createNewDocument()` and `Builder.toString()`
- Use `ZipArchive` for ZIP creation (already imported in ModuleInspector)
- Preserve original `buildFileName` ("buildFile.xml" vs "buildFile")

### Step 5: Add Component Parser/Encoder Methods

```java
// Parse XML element to ComponentData (recursive)
private ComponentData parseComponent(Element element) {
    ComponentData comp = new ComponentData();
    comp.type = element.getTagName();
    comp.attributes = new LinkedHashMap<>();

    NamedNodeMap attrs = element.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
        Attr attr = (Attr) attrs.item(i);
        comp.attributes.put(attr.getName(), attr.getValue());
    }
    comp.name = comp.attributes.get("name");

    comp.children = new ArrayList<>();
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
        if (children.item(i) instanceof Element) {
            comp.children.add(parseComponent((Element) children.item(i)));
        }
    }
    return comp;
}

// Encode ComponentData to XML string
private String encodeComponents(ComponentData root) {
    Document doc = Builder.createNewDocument();
    Element element = encodeElement(doc, root);
    doc.appendChild(element);
    return Builder.toString(doc);
}

private Element encodeElement(Document doc, ComponentData comp) {
    Element element = doc.createElement(comp.type);
    for (Map.Entry<String, String> attr : comp.attributes.entrySet()) {
        if (attr.getValue() != null) {
            element.setAttribute(attr.getKey(), attr.getValue());
        }
    }
    for (ComponentData child : comp.children) {
        element.appendChild(encodeElement(doc, child));
    }
    return element;
}
```

### Step 6: Update Help Text

Add help for new commands in `printUsage()` and add `printExportHelp()`, `printImportHelp()` methods.

## Testing

1. Export a test VMOD to directory
2. Verify JSON contains both `buildFileXml` and `rootComponent`
3. Import back without modifications - verify byte-for-byte buildFile.xml match
4. Modify `rootComponent` in JSON, remove `buildFileXml` - verify import uses structured data
5. Test with modules using old "buildFile" name (no .xml)

## Critical Files Reference

- `vassal-tools/src/main/java/org/vassalengine/tools/ModuleInspector.java` - Main file to modify
- `vassal-tools/src/main/java/org/vassalengine/tools/VsavExporter.java` - CLI pattern reference
- `vassal-app/src/main/java/VASSAL/build/Builder.java` - XML utilities (`createDocument`, `createNewDocument`, `toString`)
- `vassal-app/src/main/java/VASSAL/tools/io/ZipArchive.java` - ZIP creation API
