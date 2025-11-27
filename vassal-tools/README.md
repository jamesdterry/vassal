# Vassal Tools

Command-line utilities for manipulating and analyzing Vassal module files (.vmod).

## Overview

This project provides standalone tools for working with Vassal gameboxes programmatically, eliminating the need to use the built-in GUI editor for automation tasks.

## Tools

### vassal-inspector

A command-line tool for inspecting and analyzing Vassal module (.vmod) files.

#### Features

- **Module Metadata**: Extract module name, version, description, and Vassal version
- **Resource Analysis**: Count and list images, sounds, and icon files
- **Component Analysis**: Parse and display the component hierarchy from buildFile.xml
- **Multiple Output Formats**: Summary, detailed, JSON, or filtered lists
- **File Inspection**: List all files within the module archive with sizes

#### Usage

```bash
# Summary report (default)
./vassal-inspector.sh <module.vmod>

# Detailed report with full file listing and component tree
./vassal-inspector.sh --detailed <module.vmod>

# JSON output for scripting/automation
./vassal-inspector.sh --json <module.vmod>

# List only images
./vassal-inspector.sh --images <module.vmod>

# List only components
./vassal-inspector.sh --components <module.vmod>

# List all files in the archive
./vassal-inspector.sh --files <module.vmod>

# Save output to a file
./vassal-inspector.sh --output report.txt <module.vmod>
```

#### Command-Line Options

- `-h, --help` - Show help message
- `-v, --version` - Show version information
- `-s, --summary` - Show summary (default)
- `-d, --detailed` - Show detailed report with file listings and component tree
- `-j, --json` - Output in JSON format for scripting
- `-i, --images` - List only image files
- `-c, --components` - List only components from buildFile.xml
- `-f, --files` - List all archive files
- `-o, --output <file>` - Save report to specified file

#### Examples

**Basic module inspection:**
```bash
$ ./vassal-inspector.sh OnHellsHighway.vmod

=== Vassal Module Inspector ===

File: OnHellsHighway.vmod
Path: /path/to/OnHellsHighway.vmod
Size: 102.4 MB

--- Module Information ---
Name: On Hell's Highway
Version: 0.5.0
Description: (c) 2025 John Butterfield/New England Simulations
Vassal Version: 3.7.18

--- Resource Summary ---
Total Files: 718
Images: 712
Sounds: 0
Icons: 0

--- Components ---
  VASSAL.build.widget.PieceSlot: 391
  VASSAL.build.module.map.boardPicker.board.mapgrid.Zone: 175
  VASSAL.build.module.PrototypeDefinition: 35
  ...
```

**Extract list of all images:**
```bash
$ ./vassal-inspector.sh --images MyModule.vmod
images/piece1.png
images/piece2.png
images/map_board.png
...
```

**Generate JSON for scripting:**
```bash
$ ./vassal-inspector.sh --json MyModule.vmod > module-info.json
```

**Create a detailed analysis report:**
```bash
$ ./vassal-inspector.sh --detailed --output analysis.txt MyModule.vmod
```

---

### vsav-exporter

A command-line tool for exporting and importing VASSAL saved games (.vsav files) to/from JSON format.

#### Features

- **Export to JSON**: Convert .vsav files to structured, editable JSON
- **Import from JSON**: Create .vsav files from modified JSON
- **Round-trip Fidelity**: Lossless export/import preserves all game state
- **Trait Parsing**: 35+ trait types parsed into structured properties
- **Command Parsing**: 16 command types with full field extraction
- **Text Export**: Human-readable output for quick inspection

#### Commands

| Command | Description |
|---------|-------------|
| `export` | Export .vsav to JSON or text format |
| `import` | Import JSON back to .vsav format |
| `info` | Display information about a .vsav file |

#### Usage

```bash
# Export to JSON (stdout)
java -jar vassal-tools/target/vsav-exporter.jar export game.vsav

# Export to JSON file
java -jar vassal-tools/target/vsav-exporter.jar export -o game.json game.vsav

# Export as human-readable text
java -jar vassal-tools/target/vsav-exporter.jar export -t game.vsav

# Import JSON back to .vsav
java -jar vassal-tools/target/vsav-exporter.jar import -o new_game.vsav game.json

# Show save file info
java -jar vassal-tools/target/vsav-exporter.jar info game.vsav

# Show info as JSON
java -jar vassal-tools/target/vsav-exporter.jar info --json game.vsav
```

#### Command-Line Options

**export command:**
- `-h, --help` - Show help message
- `-j, --json` - Export as JSON (default)
- `-t, --text` - Export as human-readable text
- `-o, --output <file>` - Output file (default: stdout)
- `-m, --module <file>` - Module file (.vmod) for prototype expansion
- `--raw` - Include raw command strings in output

**import command:**
- `-h, --help` - Show help message
- `-o, --output <file>` - Output .vsav file (required)

**info command:**
- `-h, --help` - Show help message
- `-j, --json` - Output as JSON
- `--commands` - Show command summary

#### JSON Schema

The exported JSON follows this structure:

```
ExportData (root)
├── formatVersion: string           # Schema version ("1.0")
├── saveMetadata                    # From savedata XML entry
│   ├── version: string
│   └── description: string
├── moduleMetadata                  # From moduledata XML entry
│   ├── name: string
│   ├── version: string
│   ├── description: string
│   └── vassalVersion: string
└── commands: array                 # Game state commands
```

**Command Types:**

| Type | Fields |
|------|--------|
| `ADD_PIECE` | piece: PieceData |
| `REMOVE_PIECE` | pieceId |
| `CHANGE_PIECE` | pieceId, newState, oldState |
| `MOVE_PIECE` | pieceId, newMapId, newX, newY, newUnderId, oldMapId, oldX, oldY, oldUnderId, playerId |
| `BEGIN_SAVE` | (marker only) |
| `END_SAVE` | (marker only) |
| `PLAY_AUDIO` | (audio clip) |
| `MUTABLE_PROPERTY` | key, oldValue, newValue, containerId |
| `GLOBAL_PROPERTY` | propertyId, newValue, containerId |
| `TURN` | trackerId, newState |
| `PLAYER` | playerId, playerName, side |
| `PLAYER_REMOVE` | playerId |
| `FLARE` | flareId, x, y |
| `CLOCK` | who, name, elapsed, verified, ticking, restore |
| `CLOCK_CONTROL` | showing, online |
| `SETUP_STACK` | content |

**PieceData:**

```
PieceData
├── id: string          # Unique piece identifier
├── type: string        # Raw type definition (tab-separated traits)
├── state: string       # Raw state string (tab-separated trait states)
└── traits: array       # Parsed trait data
    └── TraitData
        ├── traitId: string           # e.g., "piece", "label", "emb2"
        ├── rawType: string           # Original type segment
        ├── rawState: string          # Original state segment
        └── properties: object        # Parsed key-value properties
```

**Supported Traits (35+):**

| Trait ID | VASSAL Class | Description |
|----------|--------------|-------------|
| `piece` | BasicPiece | Base piece with image and name |
| `prototype` | UsePrototype | Reference to prototype definition |
| `mark` | Marker | Property marker |
| `label` | Labeler | Text label |
| `emb2` | Embellishment | Layer images |
| `obs` | Obscurable | Masked/hidden state |
| `hide` | Hideable | Invisible to players |
| `propertysheet` | PropertySheet | Custom properties |
| `immob` | Immobilized | Cannot move |
| `rotate` | FreeRotator | Rotation |
| `markmoved` | MovementMarkable | Mark when moved |
| `report` | ReportState | Report actions |
| `PROP` | DynamicProperty | Dynamic property |
| `macro` | TriggerAction | Trigger actions |
| `footprint` | Footprint | Movement trail |
| ... | ... | (and 20+ more) |

#### Example JSON Output

```json
{
  "formatVersion": "1.0",
  "saveMetadata": {
    "version": "1"
  },
  "moduleMetadata": {
    "name": "My Game",
    "version": "1.0",
    "vassalVersion": "3.7.18"
  },
  "commands": [
    {
      "commandType": "PLAYER",
      "playerId": "abc-123",
      "playerName": "Player 1",
      "side": "Allied"
    },
    {
      "commandType": "ADD_PIECE",
      "piece": {
        "id": "piece_001",
        "traits": [
          {
            "traitId": "piece",
            "properties": {
              "imageName": "infantry.png",
              "basicName": "1st Infantry"
            }
          },
          {
            "traitId": "prototype",
            "properties": {
              "prototypeName": "Infantry Unit"
            }
          }
        ]
      }
    }
  ]
}
```

#### Round-Trip Testing

```bash
# Export original save
java -jar vassal-tools/target/vsav-exporter.jar export -o /tmp/a.json game.vsav

# Import back to new save
java -jar vassal-tools/target/vsav-exporter.jar import -o /tmp/b.vsav /tmp/a.json

# Export the re-imported save
java -jar vassal-tools/target/vsav-exporter.jar export -o /tmp/c.json /tmp/b.vsav

# Compare - should show 0 differences
diff /tmp/a.json /tmp/c.json
```

#### Modifying Save Files

```bash
# Export to JSON
java -jar vassal-tools/target/vsav-exporter.jar export -o game.json game.vsav

# Edit with jq (example: change a piece name)
jq '(.commands[] | select(.commandType == "ADD_PIECE") | .piece.traits[] | select(.traitId == "piece") | .properties.basicName) = "New Name"' game.json > modified.json

# Import modified JSON
java -jar vassal-tools/target/vsav-exporter.jar import -o modified.vsav modified.json
```

---

## Building

### Prerequisites

- Java 11 or higher
- Maven 3.5 or higher

### Build Instructions

From the `vassal` directory:

```bash
# Build all modules including vassal-tools
./mvnw clean package -DskipTests

# Or build only vassal-tools and its dependencies
./mvnw clean package -DskipTests -pl vassal-tools -am
```

The compiled JAR files will be created in `vassal-tools/target/`:
- `vassal-inspector.jar` - Module inspector with all dependencies
- `vsav-exporter.jar` - Save file exporter/importer with all dependencies

### Running Directly with Java

If you prefer not to use the wrapper script:

```bash
java -jar vassal-tools/target/vassal-inspector.jar <options> <module.vmod>
```

## Understanding Vassal Module Structure

Vassal `.vmod` files are standard ZIP archives containing:

### Core Files

- **buildFile.xml** (or `buildFile` for older modules) - Main module definition in XML format
- **moduledata** - Module metadata (name, version, description, etc.) in XML format

### Resource Directories

- **images/** - Game piece images, map boards, and other graphics
- **sounds/** - Audio files for game sounds and effects
- **icons/** - UI icons and buttons

### File Format

Since .vmod files are ZIP archives, you can also inspect them with standard ZIP tools:

```bash
# List contents
unzip -l module.vmod

# Extract all files
unzip module.vmod -d extracted/

# View specific file
unzip -p module.vmod buildFile.xml
```

## Programmatic Access

The `vassal-tools` module provides programmatic access to Vassal's file I/O APIs. Key classes:

### Core Archive Classes

- **`ZipArchive`** - Low-level ZIP file manipulation
- **`DataArchive`** - Module-aware archive reading with resource caching
- **`ArchiveWriter`** - Writable archive with helper methods for adding/removing files
- **`GameModule`** - Complete module loading, parsing, and saving

### Example: Reading a Module Programmatically

```java
import VASSAL.tools.DataArchive;
import VASSAL.build.module.metadata.ModuleMetaData;
import java.util.zip.ZipFile;

// Open and read module metadata
try (ZipFile zip = new ZipFile("module.vmod")) {
    ModuleMetaData metadata = new ModuleMetaData(zip);
    System.out.println("Module: " + metadata.getName());
    System.out.println("Version: " + metadata.getVersion());
}

// List images in module
try (DataArchive archive = new DataArchive("module.vmod")) {
    String[] images = archive.getImageNames();
    for (String image : images) {
        System.out.println("Image: " + image);
    }
}
```

### Example: Modifying a Module

```java
import VASSAL.tools.ArchiveWriter;

// Open module for writing
try (ArchiveWriter writer = new ArchiveWriter("module.vmod", ".vmod")) {
    // Add a new image
    writer.addImage("/path/to/new-piece.png", "new-piece.png");

    // Remove an old image
    writer.removeFile("images/old-piece.png");

    // Save changes
    writer.save();
}
```

## Future Tools

Additional tools planned for this project:

- **vassal-extract** - Extract images and other resources from modules
- **vassal-replace** - Batch replace images in modules
- **vassal-validate** - Validate module structure and check for common issues
- **vassal-merge** - Merge components from multiple modules
- **vassal-convert** - Convert modules between Vassal versions

## Architecture

The tools are built as a Maven module within the Vassal project structure:

```
vassal/
├── vassal-app/          # Main Vassal application
├── vassal-tools/        # CLI tools (this module)
│   ├── src/
│   │   └── main/java/org/vassalengine/tools/
│   │       ├── ModuleInspector.java
│   │       ├── VsavExporter.java
│   │       └── vsav/           # Save file processing
│   │           ├── VsavReader.java
│   │           ├── VsavWriter.java
│   │           ├── CommandParser.java
│   │           ├── CommandEncoder.java
│   │           ├── TraitParser.java
│   │           ├── model/      # Data models
│   │           ├── traits/     # 35+ trait parsers
│   │           ├── export/     # JSON/text exporters
│   │           └── import_/    # JSON importer
│   ├── target/
│   │   ├── vassal-inspector.jar
│   │   └── vsav-exporter.jar
│   ├── pom.xml
│   └── README.md
└── pom.xml
```

All tools depend on `vassal-app` for core functionality, ensuring 100% compatibility with the Vassal engine.

## Contributing

When adding new tools:

1. Create a new class in `org.vassalengine.tools` package
2. Follow the existing patterns for command-line parsing (Apache Commons CLI)
3. Add appropriate Maven Assembly plugin configuration to create standalone JARs
4. Create a wrapper shell script for convenience
5. Update this README with documentation

## License

These tools are part of the Vassal Engine project and are licensed under the GNU Lesser General Public License, version 2.1.

## Support

For issues or questions:
- Vassal Engine: https://vassalengine.org
- GitHub: https://github.com/vassalengine/vassal

## Version History

### 1.1.0
- Added vsav-exporter tool for .vsav file manipulation
- Export/import saved games to/from JSON format
- 35+ trait parsers for structured piece data
- 16 command types with full field extraction
- Round-trip fidelity for lossless save modification

### 1.0.0 (Initial Release)
- Module Inspector tool with summary, detailed, JSON, and filtered output modes
- Support for analyzing module metadata, resources, and component hierarchy
- Comprehensive documentation and examples
