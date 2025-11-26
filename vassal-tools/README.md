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
- `vassal-inspector.jar` - Standalone executable with all dependencies

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
│   │       └── ModuleInspector.java
│   ├── target/
│   │   └── vassal-inspector.jar
│   ├── pom.xml
│   ├── vassal-inspector.sh
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

### 1.0.0 (Initial Release)
- Module Inspector tool with summary, detailed, JSON, and filtered output modes
- Support for analyzing module metadata, resources, and component hierarchy
- Comprehensive documentation and examples
