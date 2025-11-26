# Vassal Code Structure Reference

Quick reference for understanding and working with Vassal's codebase.

## File Formats

### Module Files (.vmod)
- **Format**: Standard ZIP archive
- **Can open with**: Any ZIP tool or Java's ZipFile
- **Structure**:
  ```
  module.vmod (ZIP)
  ├── buildFile.xml        # Main module definition (XML)
  ├── moduledata           # Module metadata (XML)
  ├── images/              # Game piece images, maps
  ├── sounds/              # Audio files
  └── icons/               # UI icons
  ```

### Other File Types
- **.vsav** - Saved games (ZIP with savedata + game state)
- **.vext** - Module extensions (same structure as .vmod)
- **.vlog** - Game replay logs

## Core Classes for File Manipulation

### Archive I/O (Low to High Level)

**ZipArchive** - `vassal-app/src/main/java/VASSAL/tools/io/ZipArchive.java`
- Direct ZIP file manipulation
- Methods: `add()`, `remove()`, `getInputStream()`, `getOutputStream()`, `contains()`, `flush()`, `close()`
- Always use try-with-resources

**DataArchive** - `vassal-app/src/main/java/VASSAL/tools/DataArchive.java`
- Module-aware archive reading
- Methods: `getInputStream()`, `getImageNames()`, `contains()`
- Read-only wrapper around ZipArchive
- Constants: `IMAGE_DIR = "images/"`, `SOUND_DIR = "sounds/"`, `ICON_DIR = "icons/"`

**ArchiveWriter** - `vassal-app/src/main/java/VASSAL/tools/ArchiveWriter.java`
- Extends DataArchive with write capabilities
- Methods: `addImage()`, `addFile()`, `removeFile()`, `save()`, `saveAs()`
- Use for modifying modules

### Module Building

**GameModule** - `vassal-app/src/main/java/VASSAL/build/GameModule.java`
- Main module class
- Constants:
  - `BUILDFILE = "buildFile.xml"` (line 204)
  - `BUILDFILE_OLD = "buildFile"` (line 205)
- Key methods:
  - `build()` - Loads module from archive (lines 696-731)
  - `save()` - Saves module (lines 2181-2203)
  - `buildString()` - Serializes to XML

**Builder** - `vassal-app/src/main/java/VASSAL/build/Builder.java`
- XML serialization utilities
- Static methods:
  - `createDocument(InputStream)` - Parse XML (lines 169-181)
  - `createNewDocument()` - Create new XML doc (lines 187-197)
  - `toString(Document)` - Convert to string (lines 244-255)
  - `build(Element, Buildable)` - Build component hierarchy (lines 73-96)

**AbstractBuildable** - `vassal-app/src/main/java/VASSAL/build/AbstractBuildable.java`
- Base class for all module components
- Pattern: Components define attributes → serialize to XML → deserialize back
- Methods:
  - `build(Element)` - Deserialize from XML
  - `buildString()` - Serialize to XML string
  - `getBuildElement(Document)` - Create XML element
  - `setAttribute()/getAttributeValueString()` - Attribute access

### Metadata

**ModuleMetaData** - `vassal-app/src/main/java/VASSAL/build/module/metadata/ModuleMetaData.java`
- ZIP entry name: `"moduledata"` (line 51)
- Contains: name, version, description, vassalVersion

**SaveMetaData** - `vassal-app/src/main/java/VASSAL/build/module/metadata/SaveMetaData.java`
- ZIP entry name: `"savedata"` (line 66)
- For saved games (.vsav)

## Common Operations

### Reading a Module

```java
// Quick metadata read
try (ZipFile zip = new ZipFile("module.vmod")) {
    ModuleMetaData meta = new ModuleMetaData(zip);
    System.out.println(meta.getName() + " v" + meta.getVersion());
}

// Read with DataArchive
try (DataArchive archive = new DataArchive("module.vmod")) {
    String[] images = archive.getImageNames();
    // Read buildFile
    try (InputStream in = archive.getInputStream("buildFile.xml")) {
        Document doc = Builder.createDocument(in);
        // Parse XML...
    }
}
```

### Modifying a Module

```java
// Open for writing
try (ArchiveWriter writer = new ArchiveWriter("module.vmod", ".vmod")) {
    // Add new image
    writer.addImage("/path/to/image.png", "piece.png");

    // Remove old file
    writer.removeFile("images/old.png");

    // Add arbitrary file
    byte[] data = "content".getBytes(StandardCharsets.UTF_8);
    writer.addFile("myfile.txt", data);

    // Save changes (automatic on close, but explicit is clearer)
    writer.save();
}
```

### Direct ZIP Manipulation

```java
// Lowest level - when you need full control
try (ZipArchive zip = new ZipArchive("module.vmod")) {
    // Check existence
    if (zip.contains("images/piece.png")) {
        // Read
        try (InputStream in = zip.getInputStream("images/piece.png")) {
            // Process...
        }
    }

    // Add/modify
    zip.add("newfile.txt", "content".getBytes(StandardCharsets.UTF_8));

    // Remove
    zip.remove("oldfile.txt");

    // List all files
    List<String> files = zip.getFiles();

    // Changes written on close()
}
```

### Working with buildFile.xml

```java
// Parse buildFile
try (DataArchive archive = new DataArchive("module.vmod")) {
    try (InputStream in = archive.getInputStream(GameModule.BUILDFILE)) {
        Document doc = Builder.createDocument(in);
        Element root = doc.getDocumentElement();

        // Traverse components
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element child = (Element) children.item(i);
                String type = child.getTagName();
                String name = child.getAttribute("name");
                // Process component...
            }
        }
    }
}

// Modify buildFile
ArchiveWriter writer = new ArchiveWriter("module.vmod", ".vmod");
GameModule module = new GameModule(writer);
module.build(); // Load existing
// Modify module components...
String xml = module.buildString();
writer.addFile(GameModule.BUILDFILE, xml.getBytes(StandardCharsets.UTF_8));
writer.save();
```

## Architecture Patterns

### XML Serialization Pattern

1. Every component extends `AbstractBuildable`
2. Components define attributes via `getAttributeNames()`
3. Serialization: `buildString()` → XML
4. Deserialization: `build(Element)` → Object
5. Attributes stored as key-value pairs

### Resource Management

- **Images**: Stored in `images/` with relative paths
- **Caching**: DataArchive caches image/sound resources
- **Paths**: Always use forward slashes `/`, even on Windows
- **Cleanup**: Always use try-with-resources for archives

### Module Loading Flow

1. Open archive (ZipArchive/DataArchive)
2. Read `moduledata` → ModuleMetaData
3. Read `buildFile.xml` → DOM Document
4. Parse DOM → Build component hierarchy (Builder.build())
5. Each component's `build(Element)` called recursively
6. Components register themselves with parent

### Module Saving Flow

1. Each component's `getBuildElement()` creates XML element
2. Hierarchy traversed to build complete DOM
3. DOM serialized to string via `Builder.toString()`
4. String written to `buildFile.xml` in archive
5. Metadata updated and written to `moduledata`
6. Archive closed/flushed

## Key Source Locations

### Archive Operations
- `vassal-app/src/main/java/VASSAL/tools/io/ZipArchive.java` - ZIP implementation
- `vassal-app/src/main/java/VASSAL/tools/io/FileArchive.java` - Archive interface
- `vassal-app/src/main/java/VASSAL/tools/DataArchive.java` - Read operations
- `vassal-app/src/main/java/VASSAL/tools/ArchiveWriter.java` - Write operations

### Module Core
- `vassal-app/src/main/java/VASSAL/build/GameModule.java:696-731` - Module loading
- `vassal-app/src/main/java/VASSAL/build/GameModule.java:2181-2203` - Module saving
- `vassal-app/src/main/java/VASSAL/build/Builder.java` - XML utilities
- `vassal-app/src/main/java/VASSAL/build/AbstractBuildable.java` - Component base class

### Metadata
- `vassal-app/src/main/java/VASSAL/build/module/metadata/ModuleMetaData.java`
- `vassal-app/src/main/java/VASSAL/build/module/metadata/SaveMetaData.java`

### Game State
- `vassal-app/src/main/java/VASSAL/build/module/GameState.java` - Saved games

### Tests (Usage Examples)
- `vassal-app/src/test/java/VASSAL/tools/io/ZipArchiveTest.java` - Archive usage patterns

## Important Constants

```java
// GameModule
GameModule.BUILDFILE = "buildFile.xml"
GameModule.BUILDFILE_OLD = "buildFile"

// DataArchive
DataArchive.IMAGE_DIR = "images/"
DataArchive.SOUND_DIR = "sounds/"
DataArchive.ICON_DIR = "icons/"

// Metadata
ModuleMetaData.ZIP_ENTRY_NAME = "moduledata"
SaveMetaData.ZIP_ENTRY_NAME = "savedata"
```

## Common Pitfalls

1. **Forgetting to close archives** - Always use try-with-resources
2. **Path separators** - Always use `/`, never `\`
3. **Not flushing changes** - Call `flush()` or `close()` on ArchiveWriter
4. **Assuming buildFile.xml** - Check for old "buildFile" too
5. **Encoding** - Always use `StandardCharsets.UTF_8` for text
6. **Temporary files** - ArchiveWriter creates temp files for new archives

## Building Tools

The `vassal-tools` module shows how to:
- Create standalone CLI tools using Vassal's APIs
- Package with all dependencies using maven-assembly-plugin
- Parse command-line arguments
- Output multiple formats (text, JSON)

See `vassal-tools/src/main/java/org/vassalengine/tools/ModuleInspector.java` for a complete working example.

## Quick Start for New Utilities

```java
// 1. Add dependency in pom.xml
<dependency>
    <groupId>org.vassalengine</groupId>
    <artifactId>vassal-app</artifactId>
    <version>${project.version}</version>
</dependency>

// 2. Use the APIs
import VASSAL.tools.io.ZipArchive;
import VASSAL.tools.DataArchive;
import VASSAL.tools.ArchiveWriter;
import VASSAL.build.GameModule;
import VASSAL.build.Builder;

// 3. Remember: .vmod files are just ZIP + XML!
```
