# VLOG Exporter Tool Plan

## Overview

Add VLOG file support to the existing VSAV exporter tool, creating a unified CLI that handles both `.vsav` and `.vlog` files with automatic format detection.

## VLOG vs VSAV Format Comparison

| Aspect | VSAV | VLOG |
|--------|------|------|
| Structure | ZIP with savedGame, savedata, moduledata | Identical ZIP structure |
| Obfuscation | XOR obfuscation | Same obfuscation |
| Content | Complete game state as commands | Initial state + LOG-wrapped command sequence |
| Command Prefix | None | `LOG\t` for commands, `UNDO\t` for undo markers |

## Implementation Plan

### Phase 1: Model Extension

**File: `vsav/model/LogEntry.java` (NEW)**
```java
public class LogEntry {
    public enum EntryType { LOG, UNDO }
    private EntryType entryType;
    private CommandData command;      // For LOG entries (reuse existing)
    private boolean undoInProgress;   // For UNDO entries
    private String rawEntry;          // Original string for round-trip
}
```

**File: `vsav/model/ExportData.java` (MODIFY)**
- Add `fileType` field: `"vsav"` or `"vlog"`
- Add `logEntries` field: `List<LogEntry>` for VLOG files
- When `fileType == "vlog"`, `logEntries` contains the logged command sequence

### Phase 2: Command Type Extension

**File: `vsav/model/CommandData.java` (MODIFY)**
- Add `BEGIN_LOG` command type (for `begin_log` marker)
- Add `END_LOG` command type (for `end_log` marker)

**File: `vsav/CommandParser.java` (MODIFY)**
- Add case for `"begin_log"` → `BEGIN_LOG`
- Add case for `"end_log"` → `END_LOG`

**File: `vsav/CommandEncoder.java` (MODIFY)**
- Add encoding for `BEGIN_LOG` → `"begin_log"`
- Add encoding for `END_LOG` → `"end_log"`

### Phase 3: VLOG Reader/Writer

**File: `vsav/VlogReader.java` (NEW ~100 lines)**
- Read .vlog ZIP structure (same as VsavReader)
- Parse `LOG\t` and `UNDO\t` prefixes from command stream
- Unwrap inner commands and delegate to existing `CommandParser`
- Return `ExportData` with `fileType="vlog"` and populated `logEntries`

**File: `vsav/VlogWriter.java` (NEW ~100 lines)**
- Write .vlog ZIP structure (same as VsavWriter)
- Wrap commands with `LOG\t` prefix
- Encode `UNDO\t` markers
- Delegate inner command encoding to existing `CommandEncoder`

### Phase 4: Export/Import Updates

**File: `vsav/export/JsonExporter.java` (MODIFY)**
- Handle `ExportData.fileType` in output
- Include `logEntries` array when `fileType == "vlog"`

**File: `vsav/export/TextExporter.java` (MODIFY)**
- Add VLOG-specific text output showing log sequence
- Show initial state summary, then step-by-step entries

**File: `vsav/import_/JsonImporter.java` (MODIFY)**
- Detect `fileType` from JSON
- Parse `logEntries` array when present

### Phase 5: Unified CLI

**File: `VsavExporter.java` (MODIFY)**
- Auto-detect file format by extension (`.vsav` vs `.vlog`)
- Route to `VsavReader`/`VlogReader` based on format
- Route to `VsavWriter`/`VlogWriter` for import
- Update help text to mention both formats

### Phase 6: Build Updates

**File: `vassal-tools/pom.xml` (MODIFY)**
- Update artifact description to mention VLOG support
- No new assembly needed (unified tool)

### Phase 7: Documentation

**File: `VSAV_TOOL.md` (MODIFY)**
- Add VLOG file format documentation
- Add VLOG-specific JSON structure examples
- Update CLI usage examples for .vlog files

## File Summary

### New Files (3)
| File | Lines | Description |
|------|-------|-------------|
| `vsav/model/LogEntry.java` | ~50 | VLOG entry wrapper model |
| `vsav/VlogReader.java` | ~100 | Read .vlog files |
| `vsav/VlogWriter.java` | ~100 | Write .vlog files |

### Modified Files (7)
| File | Changes |
|------|---------|
| `vsav/model/ExportData.java` | Add fileType, logEntries fields |
| `vsav/model/CommandData.java` | Add BEGIN_LOG, END_LOG types |
| `vsav/CommandParser.java` | Handle begin_log, end_log |
| `vsav/CommandEncoder.java` | Encode BEGIN_LOG, END_LOG |
| `vsav/export/JsonExporter.java` | Handle VLOG structure |
| `vsav/export/TextExporter.java` | VLOG text output |
| `vsav/import_/JsonImporter.java` | Parse VLOG JSON |
| `VsavExporter.java` | Unified CLI with format detection |

## JSON Output Format

### VSAV (existing)
```json
{
  "formatVersion": "1.0",
  "fileType": "vsav",
  "saveMetadata": {...},
  "moduleMetadata": {...},
  "commands": [...]
}
```

### VLOG (new)
```json
{
  "formatVersion": "1.0",
  "fileType": "vlog",
  "saveMetadata": {...},
  "moduleMetadata": {...},
  "initialState": [...],
  "logEntries": [
    {"entryType": "LOG", "command": {...}},
    {"entryType": "UNDO", "undoInProgress": true}
  ]
}
```

## CLI Usage (Unified)

```bash
# Export VSAV (auto-detected)
java -jar vsav-exporter.jar export -o game.json game.vsav

# Export VLOG (auto-detected)
java -jar vsav-exporter.jar export -o log.json game.vlog

# Import back to VLOG
java -jar vsav-exporter.jar import -o new.vlog log.json

# Info command works for both
java -jar vsav-exporter.jar info game.vlog
```

## Critical Files to Read Before Implementation

1. `vassal-tools/src/main/java/org/vassalengine/tools/vsav/VsavReader.java` - Pattern for VlogReader
2. `vassal-tools/src/main/java/org/vassalengine/tools/vsav/VsavWriter.java` - Pattern for VlogWriter
3. `vassal-tools/src/main/java/org/vassalengine/tools/vsav/CommandParser.java` - Reuse for inner commands
4. `vassal-tools/src/main/java/org/vassalengine/tools/VsavExporter.java` - CLI to modify
5. `vassal-app/src/main/java/VASSAL/build/module/BasicLogger.java` - LOG/UNDO prefix constants (lines 81-84)
6. `vassal-tools/src/main/java/org/vassalengine/tools/vsav/model/ExportData.java` - Model to extend

## Implementation Order

1. Add `LogEntry` model class
2. Extend `ExportData` with fileType and logEntries
3. Add BEGIN_LOG/END_LOG to CommandData, CommandParser, CommandEncoder
4. Create `VlogReader` (allows testing export)
5. Update `JsonExporter` and `TextExporter` for VLOG
6. Update `VsavExporter` CLI for format detection
7. Create `VlogWriter`
8. Update `JsonImporter` for VLOG
9. Test round-trip: .vlog → JSON → .vlog
10. Update documentation
