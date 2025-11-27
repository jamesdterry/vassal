# VsavExporter Tool - Incremental Implementation Plan

This document serves as a checklist for implementing the VsavExporter tool in phases. Each phase delivers a working export AND import capability, progressively adding trait support.

---

## Phase 0: Infrastructure ✅ COMPLETE
> Goal: Build the foundation - CLI, file I/O, command parsing (no trait parsing yet)

**Status:** Completed and tested 2024-11-26

### Checklist
- [x] **0.1** Create `VsavExporter.java` - CLI entry point with Apache Commons CLI
  - Commands: `export`, `import`, `info`
  - Options: `-m/--module`, `-o/--output`, `-t/--text`, `-j/--json`, `-h/--help`, `-v/--version`
- [x] **0.2** Create `vsav/VsavReader.java` - Read .vsav ZIP structure
  - Extract `savedata` → parse XML metadata
  - Extract `moduledata` → parse XML metadata
  - Extract `savedGame` → deobfuscate via DeobfuscatingInputStream
  - Split commands by COMMAND_SEPARATOR (0x1B)
- [x] **0.3** Create `vsav/VsavWriter.java` - Write .vsav ZIP structure
  - Write `savedata` XML
  - Write `moduledata` XML
  - Write `savedGame` via ObfuscatingOutputStream
  - Join commands with COMMAND_SEPARATOR
- [x] **0.4** Create `vsav/ModuleDefinitionReader.java` - Read .vmod buildFile.xml
  - Extract prototype definitions
  - Extract map names
- [x] **0.5** Create `vsav/CommandParser.java` - Parse command strings
  - Parse `+/{id}/{type}/{state}` → AddPieceCommand
  - Parse `-/{id}` → RemovePieceCommand
  - Parse `D/{id}/{newState}[/{oldState}]` → ChangePieceCommand
  - Parse `M/{id}/{mapId}/{x}/{y}/...` → MovePieceCommand
  - Parse `begin_save` / `end_save` → SetupCommand
- [x] **0.6** Create `vsav/CommandEncoder.java` - Encode commands back to strings
- [x] **0.7** Create data models in `vsav/model/`
  - `ExportData.java` - Root model
  - `SaveMetadata.java` - Save file metadata
  - `ModuleMetadata.java` - Module metadata
  - `CommandData.java` - Base command class
  - `PieceData.java` - Piece with raw type/state
  - `AddPieceCommand.java` - Add piece command
  - `RemovePieceCommand.java` - Remove piece command
  - `ChangePieceCommand.java` - Change piece command
  - `MovePieceCommand.java` - Move piece command
- [x] **0.8** Create `vsav/export/JsonExporter.java` - JSON output with GSON
- [x] **0.9** Create `vsav/export/TextExporter.java` - Human-readable output
- [x] **0.10** Create `vsav/import_/JsonImporter.java` - Read JSON back
- [x] **0.11** Add assembly configuration to `vassal-tools/pom.xml`
- [x] **0.12** Test: Export → Import round-trip with raw type/state strings (no parsing)

### Test Results
- Tested with `onemove.vsav` (from `1812_Test.vmod`)
- Export: 467 commands (451 ADD_PIECE, 16 other)
- Round-trip: Original 14,226 bytes → Re-imported 14,212 bytes
- Verified: Re-imported save loads correctly in VASSAL

### Files Created
```
vassal-tools/src/main/java/org/vassalengine/tools/
├── VsavExporter.java
└── vsav/
    ├── VsavReader.java
    ├── VsavWriter.java
    ├── ModuleDefinitionReader.java
    ├── CommandParser.java
    ├── CommandEncoder.java
    ├── model/
    │   ├── ExportData.java
    │   ├── SaveMetadata.java
    │   ├── ModuleMetadata.java
    │   ├── CommandData.java
    │   ├── PieceData.java
    │   ├── AddPieceCommand.java
    │   ├── RemovePieceCommand.java
    │   ├── ChangePieceCommand.java
    │   └── MovePieceCommand.java
    ├── export/
    │   ├── JsonExporter.java
    │   ├── TextExporter.java
    │   └── CommandDataTypeAdapter.java
    └── import_/
        └── JsonImporter.java
```

### Usage
```bash
# Build
./mvnw package -pl vassal-tools -am -DskipTests

# Export to JSON
java -jar vassal-tools/target/vsav-exporter.jar export -o game.json game.vsav

# Export to text
java -jar vassal-tools/target/vsav-exporter.jar export -t game.vsav

# Import from JSON
java -jar vassal-tools/target/vsav-exporter.jar import -o new.vsav game.json

# Show info
java -jar vassal-tools/target/vsav-exporter.jar info game.vsav
```

**Deliverable**: Working tool that exports/imports saves using raw (unparsed) type/state strings. ✅

---

## Phase 1: Core Traits
> Goal: Parse the most essential traits - pieces can be identified and positioned

### Traits
| ID | Class | Description |
|----|-------|-------------|
| `piece` | BasicPiece | Base piece with image and name |
| `proto` | UsePrototype | Reference to prototype definition |
| `mark` | Marker | Simple property marker |
| `label` | Labeler | Text label on piece |

### Checklist
- [ ] **1.1** Create `vsav/traits/TraitParserRegistry.java` - Map trait IDs to parsers
- [ ] **1.2** Create `vsav/traits/AbstractTraitParser.java` - Base parser class
- [ ] **1.3** Create `vsav/TraitParser.java` - Parse full decorator chain
  - Split type string by tab
  - Split state string by tab
  - Match each segment to registered parser
- [ ] **1.4** Create `vsav/traits/UnknownTraitParser.java` - Fallback for unknown traits
- [ ] **1.5** Implement `BasicPieceParser.java`
  - Type: `piece;[cloneKey];[deleteKey];[imageName];[commonName]`
  - State: `[x];[y];[mapId];[gpId]`
- [ ] **1.6** Implement `UsePrototypeParser.java`
  - Type: `proto;[prototypeName]`
  - State: (empty or matches type params)
- [ ] **1.7** Implement `MarkerParser.java`
  - Type: `mark;[propertyName];[value]`
  - State: (none)
- [ ] **1.8** Implement `LabelerParser.java`
  - Type: `label;[labelFormat];[fontSize];...`
  - State: `[currentLabel]`
- [ ] **1.9** Update `PieceData.java` to include parsed traits
- [ ] **1.10** Update `JsonExporter` to output parsed trait data
- [ ] **1.11** Create `vsav/traits/TraitEncoder.java` - Encode traits back to strings
- [ ] **1.12** Implement encoders for Core traits
- [ ] **1.13** Test: Export → Edit JSON → Import round-trip with Core traits

**Deliverable**: Tool parses/encodes Core traits; unknown traits preserved as raw strings.

---

## Phase 2: High Priority Traits
> Goal: Handle visibility, layers, and common game mechanics

### Traits
| ID | Class | Description |
|----|-------|-------------|
| `emb2` | Embellishment | Layer images (modern format) |
| `layer` | Embellishment0 | Layer images (legacy format) |
| `obs` | Obscurable | Masked/hidden state |
| `hide` | Hideable | Invisible to other players |
| `prop` | PropertySheet | Custom properties |
| `immob` | Immobilized | Cannot be moved |

### Checklist
- [ ] **2.1** Implement `EmbellishmentParser.java` (emb2)
  - Type: `emb2;[activateKey];[upKey];[downKey];[resetKey];...`
  - State: `[currentLevel];[active]`
- [ ] **2.2** Implement `Embellishment0Parser.java` (layer)
  - Type: `layer;[activateCommand];[activateKey];...`
  - State: `[value]`
- [ ] **2.3** Implement `ObscurableParser.java` (obs)
  - Type: `obs;[keyCommand];[imageName];[displayStyle];...`
  - State: `[obscuredToOthers];[obscuredBy]`
- [ ] **2.4** Implement `HideableParser.java` (hide)
  - Type: `hide;[keyCommand];[hiddenImage];...`
  - State: `[hidden]`
- [ ] **2.5** Implement `PropertySheetParser.java` (prop)
  - Type: `prop;[menuText];[properties]`
  - State: `[propertyValues]`
- [ ] **2.6** Implement `ImmobilizedParser.java` (immob)
  - Type: `immob;[option];[keyCommand];...`
  - State: (none or boolean)
- [ ] **2.7** Implement encoders for High priority traits
- [ ] **2.8** Test: Export → Edit JSON → Import with High priority traits

**Deliverable**: Tool handles layers, visibility, and property sheets.

---

## Phase 3: Medium Priority Traits
> Goal: Handle movement, rotation, and piece manipulation

### Traits
| ID | Class | Description |
|----|-------|-------------|
| `report` | ReportState | Report actions to chat |
| `move` | MovementMarkable | Mark piece as moved |
| `rotate` | FreeRotator | Rotation angles |
| `pivot` | Pivot | Pivot rotation |
| `restrict` | Restricted | Restrict commands |
| `delete` | Delete | Delete piece |
| `replace` | Replace | Replace with another piece |
| `clone` | Clone | Clone piece |
| `mat` | Mat | Mat for cargo pieces |
| `cargo` | MatCargo | Cargo on a mat |
| `place` | PlaceMarker | Place new marker |
| `stack` | CalculatedProperty | Calculated properties |
| `gprop` | GlobalProperty | Global property access |
| `setgprop` | SetGlobalProperty | Set global property |
| `sendto` | SendToLocation | Send to location |
| `return` | ReturnToDeck | Return to deck |

### Checklist
- [ ] **3.1** Implement `ReportStateParser.java`
- [ ] **3.2** Implement `MovementMarkableParser.java`
- [ ] **3.3** Implement `FreeRotatorParser.java`
- [ ] **3.4** Implement `PivotParser.java`
- [ ] **3.5** Implement `RestrictedParser.java`
- [ ] **3.6** Implement `DeleteParser.java`
- [ ] **3.7** Implement `ReplaceParser.java`
- [ ] **3.8** Implement `CloneParser.java`
- [ ] **3.9** Implement `MatParser.java`
- [ ] **3.10** Implement `MatCargoParser.java`
- [ ] **3.11** Implement `PlaceMarkerParser.java`
- [ ] **3.12** Implement `CalculatedPropertyParser.java`
- [ ] **3.13** Implement `GlobalPropertyParser.java`
- [ ] **3.14** Implement `SetGlobalPropertyParser.java`
- [ ] **3.15** Implement `SendToLocationParser.java`
- [ ] **3.16** Implement `ReturnToDeckParser.java`
- [ ] **3.17** Implement encoders for Medium priority traits
- [ ] **3.18** Test: Export → Edit JSON → Import with Medium priority traits

**Deliverable**: Tool handles movement, rotation, and piece manipulation traits.

---

## Phase 4: Low Priority Traits
> Goal: Complete coverage of all standard traits

### Traits
| ID | Class | Description |
|----|-------|-------------|
| `trigger` | TriggerAction | Trigger other actions |
| `macro` | ActionButton | Toolbar button action |
| `menuSep` | MenuSeparator | Menu separator |
| `globalHotkey` | GlobalHotKey | Fire global hotkey |
| `subMenu` | SubMenu | Submenu grouping |
| `deselect` | Deselect | Deselect after action |
| `nonRect` | NonRectangular | Non-rectangular selection |
| `footprint` | Footprint | Movement trail |
| `area` | AreaOfEffect | Area highlight |
| `restrict2` | RestrictCommands | Restrict commands v2 |
| `prototype` | Prototype (definition) | Prototype component |
| `deck` | Deck | Deck container |
| `dynamicProp` | DynamicProperty | Dynamic property |
| `playSound` | PlaySound | Play sound effect |
| `setAttach` | Attachment | Attach to other pieces |
| `translate` | Translate | Move by offset |
| `countup` | CounterGlobalKeyCommand | Counter GKC |
| `globalkey` | GlobalKeyCommand | Global key command |
| `masskey` | MassKeyCommand | Mass key command |

### Checklist
- [ ] **4.1** Implement `TriggerActionParser.java`
- [ ] **4.2** Implement `ActionButtonParser.java`
- [ ] **4.3** Implement `MenuSeparatorParser.java`
- [ ] **4.4** Implement `GlobalHotKeyParser.java`
- [ ] **4.5** Implement `SubMenuParser.java`
- [ ] **4.6** Implement `DeselectParser.java`
- [ ] **4.7** Implement `NonRectangularParser.java`
- [ ] **4.8** Implement `FootprintParser.java`
- [ ] **4.9** Implement `AreaOfEffectParser.java`
- [ ] **4.10** Implement `RestrictCommandsParser.java`
- [ ] **4.11** Implement `DeckParser.java`
- [ ] **4.12** Implement `DynamicPropertyParser.java`
- [ ] **4.13** Implement `PlaySoundParser.java`
- [ ] **4.14** Implement `AttachmentParser.java`
- [ ] **4.15** Implement `TranslateParser.java`
- [ ] **4.16** Implement `CounterGlobalKeyCommandParser.java`
- [ ] **4.17** Implement `GlobalKeyCommandParser.java`
- [ ] **4.18** Implement `MassKeyCommandParser.java`
- [ ] **4.19** Implement encoders for Low priority traits
- [ ] **4.20** Test: Full export → Edit JSON → Import round-trip

**Deliverable**: Complete trait coverage for all standard Vassal traits.

---

## Phase 5: Polish & Documentation
> Goal: Production-ready tool

### Checklist
- [ ] **5.1** Add `--validate` command to check JSON against schema
- [ ] **5.2** Add `--diff` command to compare two exports
- [ ] **5.3** Add progress reporting for large saves
- [ ] **5.4** Add `--filter-map` option to export specific maps
- [ ] **5.5** Add `--expand-prototypes` option to inline prototype definitions
- [ ] **5.6** Create comprehensive error messages with context
- [ ] **5.7** Write unit tests for all trait parsers
- [ ] **5.8** Write integration tests with real .vsav files
- [ ] **5.9** Document JSON schema in README
- [ ] **5.10** Document CLI usage with examples
- [ ] **5.11** Update CLAUDE.md with VsavExporter documentation

**Deliverable**: Production-ready tool with full documentation.

---

## File Structure

```
vassal-tools/src/main/java/org/vassalengine/tools/
├── VsavExporter.java
└── vsav/
    ├── VsavReader.java
    ├── VsavWriter.java
    ├── ModuleDefinitionReader.java
    ├── CommandParser.java
    ├── CommandEncoder.java
    ├── TraitParser.java
    ├── model/
    │   ├── ExportData.java
    │   ├── SaveMetadata.java
    │   ├── ModuleMetadata.java
    │   ├── CommandData.java
    │   ├── PieceData.java
    │   ├── TraitData.java
    │   └── PrototypeData.java
    ├── traits/
    │   ├── TraitParserRegistry.java
    │   ├── AbstractTraitParser.java
    │   ├── TraitEncoder.java
    │   ├── UnknownTraitParser.java
    │   ├── BasicPieceParser.java
    │   ├── UsePrototypeParser.java
    │   ├── MarkerParser.java
    │   ├── LabelerParser.java
    │   ├── EmbellishmentParser.java
    │   ├── ... (remaining trait parsers)
    │   └── encoders/
    │       └── ... (trait encoders)
    ├── export/
    │   ├── JsonExporter.java
    │   └── TextExporter.java
    └── import_/
        └── JsonImporter.java
```

---

## Critical Files to Reference

| File | Purpose |
|------|---------|
| `vassal-app/.../BasicCommandEncoder.java:93-200` | decoratorFactories map - trait ID list |
| `vassal-app/.../BasicCommandEncoder.java:337-394` | Command parsing patterns |
| `vassal-app/.../SequenceEncoder.java` | Parsing utility |
| `vassal-app/.../DeobfuscatingInputStream.java` | Deobfuscation |
| `vassal-app/.../ObfuscatingOutputStream.java` | Obfuscation for import |
| `vassal-app/.../counters/*.java` | Each trait's myGetType/myGetState |
| `vassal-tools/.../ModuleInspector.java` | CLI patterns |

---

## Testing Strategy

Each phase includes round-trip testing:
1. Export a .vsav to JSON
2. Manually edit the JSON (change a value)
3. Import the JSON back to a new .vsav
4. Load both in Vassal to verify the change took effect

Test files needed:
- Simple save with Core traits only
- Save with layers and visibility (High priority)
- Save with movement/rotation (Medium priority)
- Complex save with all traits (Low priority)

---

## Notes

### Observed Command Types (from onemove.vsav)
Commands seen in testing that need handling:
- `PLAYER` - Player info commands
- `BoardPicker` - Map board selection
- `SETUP_STACK` - Stack setup command
- `stack` - Stack piece type (not a trait, a basic piece type)

### Trait Encoding Notes
- Traits are tab-separated in type/state strings
- Escape sequences use backslash: `\\` for literal backslash, `\t` for tab in nested contexts
- BasicPiece is always the innermost trait
- Decorator chain goes from outer to inner (first trait in string wraps the rest)
