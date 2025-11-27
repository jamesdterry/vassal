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

## Phase 1: Core Traits ✅ COMPLETE
> Goal: Parse the most essential traits - pieces can be identified and positioned

**Status:** Completed and tested 2024-11-26

### Traits
| ID | Class | Description |
|----|-------|-------------|
| `piece` | BasicPiece | Base piece with image and name |
| `prototype` | UsePrototype | Reference to prototype definition |
| `mark` | Marker | Simple property marker |
| `label` | Labeler | Text label on piece |

### Checklist
- [x] **1.1** Create `vsav/traits/TraitParserRegistry.java` - Map trait IDs to parsers
- [x] **1.2** Create `vsav/traits/AbstractTraitParser.java` - Base parser class
- [x] **1.3** Create `vsav/TraitParser.java` - Parse full decorator chain
  - Split type string by tab
  - Split state string by tab
  - Match each segment to registered parser
- [x] **1.4** Create `vsav/traits/UnknownTraitParser.java` - Fallback for unknown traits
- [x] **1.5** Implement `BasicPieceParser.java`
  - Type: `piece;[cloneKey];[deleteKey];[imageName];[commonName]`
  - State: `[mapName];[x];[y];[gpId];[persistentPropCount];[props...]`
- [x] **1.6** Implement `UsePrototypeParser.java`
  - Type: `prototype;[prototypeName];[properties]`
  - State: (empty)
- [x] **1.7** Implement `MarkerParser.java`
  - Type: `mark;[key1],[key2],...`
  - State: `[value1],[value2],...`
- [x] **1.8** Implement `LabelerParser.java`
  - Type: `label;[keyStroke];[menuCommand];[fontSize];...`
  - State: `[labelText]`
- [x] **1.9** Update `PieceData.java` to include parsed traits
- [x] **1.10** Update `JsonExporter` to output parsed trait data
- [x] **1.11** Create trait encoding in parsers (encode method)
- [x] **1.12** Implement encoders for Core traits
- [x] **1.13** Test: Export → Edit JSON → Import round-trip with Core traits

### Test Results
- Tested with `onemove.vsav` (from `1812_Test.vmod`)
- Round-trip: Original 14,226 bytes → Re-imported 14,131 bytes
- JSON exports are identical after round-trip (0 diffs)
- Trait modifications (e.g., changing piece name) persist through round-trip
- Verified: Re-imported save loads correctly in VASSAL

### Files Created/Modified
```
vassal-tools/src/main/java/org/vassalengine/tools/vsav/
├── TraitParser.java (NEW)
├── CommandParser.java (modified - calls parseTraits)
├── CommandEncoder.java (modified - encodes from traits)
├── model/
│   ├── PieceData.java (modified - includes traits list)
│   └── TraitData.java (NEW)
└── traits/ (NEW directory)
    ├── TraitParserRegistry.java
    ├── AbstractTraitParser.java
    ├── UnknownTraitParser.java
    ├── BasicPieceParser.java
    ├── UsePrototypeParser.java
    ├── MarkerParser.java
    └── LabelerParser.java
```

**Deliverable**: Tool parses/encodes Core traits; unknown traits preserved as raw strings. ✅

### Implementation Notes for Future Phases

#### Architecture Overview
```
Export Flow:
  VsavReader → CommandParser.parseCommands() → PieceData.parseTraits() → JsonExporter

Import Flow:
  JsonImporter → CommandEncoder.encodeCommand() → PieceData.encodeTraits() → VsavWriter
```

#### Adding a New Trait Parser
1. Create `FooParser.java` in `vsav/traits/`:
   ```java
   public class FooParser extends AbstractTraitParser {
       public static final String TRAIT_ID = "foo";  // Match VASSAL's ID constant

       @Override
       public String getTraitId() { return TRAIT_ID; }

       @Override
       public TraitData parse(String typeSegment, String stateSegment) {
           TraitData trait = new TraitData(TRAIT_ID, typeSegment, stateSegment);
           // Parse type: foo;param1;param2;...
           String[] typeParts = split(typeSegment, ';');
           trait.setProperty("param1", getPart(typeParts, 1));
           // Parse state similarly
           return trait;
       }

       @Override
       public String[] encode(TraitData trait) {
           StringBuilder type = new StringBuilder(TRAIT_ID);
           type.append(';').append(nullToEmpty(trait.getStringProperty("param1")));
           String state = nullToEmpty(trait.getStringProperty("stateField"));
           return new String[] { type.toString(), state };
       }
   }
   ```

2. Register in `TraitParserRegistry` constructor:
   ```java
   register(new FooParser());
   ```

#### Critical: VASSAL Escape Sequence Handling
VASSAL's `SequenceEncoder` has asymmetric escape handling:
- **Decoder**: `\X` where X is delimiter → literal X; `\\` → literal `\`
- **Encoder**: Only escapes the delimiter, NOT backslashes

**In our code**:
- `split(s, delim)` - Only treats `\` + delimiter as escape. Backslashes are literal.
- `escapeValue(s, delim)` - Only escapes the delimiter character, not backslashes.

This was a major source of bugs. Don't add backslash escaping!

#### Trait String Format
- Type and state strings are TAB-separated (char 0x09)
- Each segment is one trait, from outermost decorator to innermost (BasicPiece)
- Within each segment, fields are typically semicolon-separated
- Use `split(s, ';')` for type parsing, check trait source for state delimiter

#### Finding Trait Formats
Look at each trait class in `vassal-app/src/main/java/VASSAL/counters/`:
- `public static final String ID` - The trait prefix (e.g., "emb2;")
- `mySetType(String type)` - How type string is parsed
- `myGetType()` - How type string is encoded
- `mySetState(String state)` - How state string is parsed
- `myGetState()` - How state string is encoded

#### Key Classes Reference
| VASSAL Class | Trait ID | Notes |
|--------------|----------|-------|
| `BasicPiece` | `piece;` | Base piece |
| `UsePrototype` | `prototype;` | Prototype reference |
| `Marker` | `mark;` | Property marker |
| `Labeler` | `label;` | Text label |
| `Embellishment` | `emb2;` | Layers (modern format) |
| `Embellishment0` | `emb2;` | Layers (legacy, same ID!) |
| `Obscurable` | `obs;` | Masked/fog of war |
| `Hideable` | `hide;` | Invisible pieces |
| `PropertySheet` | `propertysheet;` | Custom properties |
| `Immobilized` | `immob;` | Cannot move |
| `FreeRotator` | `rotate;` | Rotation |
| `DynamicProperty` | `PROP;` | Note uppercase! |
| `TriggerAction` | `macro;` | Not "trigger"! |
| `ActionButton` | `button;` | Clickable button |
| `RestrictCommands` | `hideCmd;` | Not "restrict2"! |
| `CounterGlobalKeyCommand` | `globalkey;` | GKC from piece |
| `NonRectangular` | `nonRect2;` | Has old ID nonRect; too |
| `AreaOfEffect` | `AreaOfEffect;` | Note case-sensitive |

#### Testing Pattern
```bash
# Build
./mvnw package -pl vassal-tools -am -DskipTests

# Round-trip test (should show 0 diffs)
java -jar vassal-tools/target/vsav-exporter.jar export -o /tmp/a.json game.vsav
java -jar vassal-tools/target/vsav-exporter.jar import -o /tmp/b.vsav /tmp/a.json
java -jar vassal-tools/target/vsav-exporter.jar export -o /tmp/c.json /tmp/b.vsav
diff /tmp/a.json /tmp/c.json

# Modification test
jq '(.commands[] | select(.piece.id == "XXX") | .piece.traits[] | select(.traitId == "piece") | .properties.basicName) = "NewName"' /tmp/a.json > /tmp/mod.json
java -jar vassal-tools/target/vsav-exporter.jar import -o /tmp/mod.vsav /tmp/mod.json
```

#### Common Gotchas
1. **Trait IDs**: Some differ from class names (e.g., `TriggerAction` uses `macro;`)
2. **State delimiters**: Each trait may use different delimiters (check source)
3. **Empty values**: Use `getPart(parts, idx, "default")` for optional fields
4. **Integer properties**: GSON deserializes as Double; use `trait.getIntProperty()`
5. **Trailing content**: BasicPiece state has persistent properties after gpId

---

## Phase 2: High Priority Traits ✅ COMPLETE
> Goal: Handle visibility, layers, and common game mechanics

**Status:** Completed and tested 2024-11-26

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
- [x] **2.1** Implement `EmbellishmentParser.java` (emb2)
  - Type: `emb2;[activateCommand];[activateModifiers];[activateKey];...`
  - State: `[value];[activationStatus]`
  - Note: Handles both modern Embellishment and legacy Embellishment0 (both use emb2 ID)
- [x] **2.2** Implement `Embellishment0Parser.java` (layer)
  - Note: Merged with EmbellishmentParser - both modern and legacy use `emb2` ID
- [x] **2.3** Implement `ObscurableParser.java` (obs)
  - Type: `obs;[keyCommand];[imageName];[hideCommand];[displayStyle];[maskName];...`
  - State: `[obscuredBy];[obscuredOptions]`
- [x] **2.4** Implement `HideableParser.java` (hide)
  - Type: `hide;[hideKey];[command];[bgColor];[access];[transparency];...`
  - State: `[hiddenBy]`
- [x] **2.5** Implement `PropertySheetParser.java` (propertysheet)
  - Type: `propertysheet;[definition];[menuName];...`
  - State: `[values~separated~by~tilde]`
- [x] **2.6** Implement `ImmobilizedParser.java` (immob)
  - Type: `immob;[selectionOptions];[movementOption];[stackingOption];[description]`
  - State: (empty)
- [x] **2.7** Implement encoders for High priority traits
- [x] **2.8** Test: Build compiles successfully with all parsers registered
- [x] **2.9** Fix: Command parsing with SequenceEncoder for escaped `/` characters

### Test Results
- Tested with `Campaign.vsav` (complex wargame save with ~500 commands)
- Export: 513 commands
- Round-trip JSON: **0 differences** (perfect fidelity)
- All rawCommand content identical (390,169 bytes)
- Binary VSAV: 2-byte difference in savedGame (metadata variation, game state preserved)
- Verified: Commands encode/decode correctly through multiple round-trips

### Files Created
```
vassal-tools/src/main/java/org/vassalengine/tools/vsav/traits/
├── EmbellishmentParser.java   # emb2 - Layer (modern and legacy formats)
├── ObscurableParser.java      # obs - Mask trait
├── HideableParser.java        # hide - Invisible trait
├── PropertySheetParser.java   # propertysheet - Custom properties
└── ImmobilizedParser.java     # immob - Does Not Stack
```

### Implementation Notes
- Both `Embellishment` and `Embellishment0` use the same trait ID (`emb2;`)
- The `EmbellishmentParser` handles both versions by checking for version field and activationStatus
- The `PropertySheetParser` also preserves the raw definition string for round-trip compatibility

### Critical Bug Fix: Command Parsing

**Problem**: AddPiece commands were being incorrectly parsed when piece names contained `/` characters (e.g., `1_P1/`).

**Root Cause**: The original `CommandParser` used naive string splitting on `/`, but VASSAL's `BasicCommandEncoder` uses `SequenceEncoder` which escapes `/` characters within the id, type, and state fields.

**Solution**: Changed `CommandParser.parseAddPiece()` and `CommandEncoder.encodeAddPiece()` to use VASSAL's `SequenceEncoder.Decoder` and `SequenceEncoder` respectively:

```java
// Parsing - properly handles escaped / characters
SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(content, '/');
String id = st.nextToken();
String type = st.nextToken();
String state = st.nextToken();

// Encoding - properly escapes / characters
SequenceEncoder se = new SequenceEncoder('/');
se.append(id).append(type).append(state);
return ADD_PREFIX + se.getValue();
```

### Trait Encoding: Recursive Structure

VASSAL's trait type/state strings use **recursive nested encoding**:
- Each outer trait wraps inner traits' data using `SequenceEncoder` with TAB separator
- When decoding, use `nextToken()` to get properly decoded inner data
- The `TraitParser` was updated to use recursive parsing/encoding with `SequenceEncoder.Decoder`

**Deliverable**: Tool handles layers, visibility, and property sheets. ✅

---

## Phase 3: Medium Priority Traits ✅ COMPLETE
> Goal: Handle movement, rotation, and piece manipulation

**Status:** Completed and tested 2024-11-27

### Traits
| ID | Class | Description |
|----|-------|-------------|
| `report` | ReportState | Report actions to chat |
| `markmoved` | MovementMarkable | Mark piece as moved |
| `rotate` | FreeRotator | Rotation angles |
| `pivot` | Pivot | Pivot rotation |
| `restrict` | Restricted | Restrict commands |
| `delete` | Delete | Delete piece |
| `replace` | Replace | Replace with another piece |
| `clone` | Clone | Clone piece |
| `mat` | Mat | Mat for cargo pieces |
| `matPiece` | MatCargo | Cargo on a mat |
| `placemark` | PlaceMarker | Place new marker |
| `calcProp` | CalculatedProperty | Calculated properties |
| `setprop` | SetGlobalProperty | Set global property |
| `sendto` | SendToLocation | Send to location |
| `return` | ReturnToDeck | Return to deck |

### Checklist
- [x] **3.1** Implement `ReportStateParser.java`
- [x] **3.2** Implement `MovementMarkableParser.java`
- [x] **3.3** Implement `FreeRotatorParser.java`
- [x] **3.4** Implement `PivotParser.java`
- [x] **3.5** Implement `RestrictedParser.java`
- [x] **3.6** Implement `DeleteParser.java`
- [x] **3.7** Implement `ReplaceParser.java`
- [x] **3.8** Implement `CloneParser.java`
- [x] **3.9** Implement `MatParser.java`
- [x] **3.10** Implement `MatCargoParser.java`
- [x] **3.11** Implement `PlaceMarkerParser.java`
- [x] **3.12** Implement `CalculatedPropertyParser.java`
- [x] **3.13** Implement `SetGlobalPropertyParser.java`
- [x] **3.14** Implement `SendToLocationParser.java`
- [x] **3.15** Implement `ReturnToDeckParser.java`
- [x] **3.16** Implement encoders for Medium priority traits
- [x] **3.17** Test: Export → Edit JSON → Import with Medium priority traits
- [x] **3.18** Add helper methods to TraitData (getIntProperty, getDoubleProperty, getBooleanProperty with defaults)

### Test Results
- Tested with `Campaign7_roundtrip.vsav` (complex wargame save)
- Export: 513 commands
- Round-trip JSON: **0 differences** (perfect fidelity)
- Verified: New trait parsers correctly parse/encode traits like rotate, markmoved, report, sendto

### Files Created
```
vassal-tools/src/main/java/org/vassalengine/tools/vsav/traits/
├── ReportStateParser.java          # report - Report Action
├── MovementMarkableParser.java     # markmoved - Mark When Moved
├── FreeRotatorParser.java          # rotate - Can Rotate
├── PivotParser.java                # pivot - Can Pivot
├── RestrictedParser.java           # restrict - Restricted Access
├── DeleteParser.java               # delete - Delete
├── CloneParser.java                # clone - Clone
├── MatParser.java                  # mat - Mat
├── MatCargoParser.java             # matPiece - Mat Cargo
├── PlaceMarkerParser.java          # placemark - Place Marker
├── ReplaceParser.java              # replace - Replace With Other
├── CalculatedPropertyParser.java   # calcProp - Calculated Property
├── SetGlobalPropertyParser.java    # setprop - Set Global Property
├── SendToLocationParser.java       # sendto - Send to Location
└── ReturnToDeckParser.java         # return - Return to Deck

vassal-tools/src/main/java/org/vassalengine/tools/vsav/model/
└── TraitData.java                  # Added getIntProperty(key, default), getDoubleProperty, getBooleanProperty methods
```

### Implementation Notes
- **FreeRotator** has two formats: free rotation (numFacings=1) vs fixed facings (numFacings>1)
- **MovementMarkable** state is a boolean string ("true"/"false")
- **Mat/MatCargo** work together - Mat tracks cargo piece IDs, MatCargo tracks its mat's ID
- **SendToLocation** has complex type format with many optional fields, state tracks back location
- **TraitData** was enhanced with helper methods for getting typed properties with defaults

**Deliverable**: Tool handles movement, rotation, and piece manipulation traits. ✅

---

## Phase 4: Low Priority Traits ✅ COMPLETE
> Goal: Complete coverage of all standard traits

**Status:** Completed and tested 2024-11-27

### Traits (Actual IDs from VASSAL source)
| ID | Class | Description |
|----|-------|-------------|
| `macro` | TriggerAction | Trigger other actions |
| `button` | ActionButton | Clickable button on piece |
| `menuSeparator` | MenuSeparator | Menu separator |
| `globalhotkey` | GlobalHotKey | Fire global hotkey |
| `submenu` | SubMenu | Submenu grouping |
| `deselect` | Deselect | Deselect after action |
| `nonRect2` | NonRectangular | Non-rectangular shape |
| `footprint` | Footprint | Movement trail |
| `AreaOfEffect` | AreaOfEffect | Area highlight |
| `hideCmd` | RestrictCommands | Restrict commands |
| `PROP` | DynamicProperty | Dynamic property (note: uppercase) |
| `playSound` | PlaySound | Play sound effect |
| `attach` | Attachment | Attach to other pieces |
| `translate` | Translate | Move by offset |
| `globalkey` | CounterGlobalKeyCommand | GKC from piece |

### Checklist
- [x] **4.1** Implement `TriggerActionParser.java` (macro)
- [x] **4.2** Implement `ActionButtonParser.java` (button)
- [x] **4.3** Implement `MenuSeparatorParser.java` (menuSeparator)
- [x] **4.4** Implement `GlobalHotKeyParser.java` (globalhotkey)
- [x] **4.5** Implement `SubMenuParser.java` (submenu)
- [x] **4.6** Implement `DeselectParser.java` (deselect)
- [x] **4.7** Implement `NonRectangularParser.java` (nonRect2)
- [x] **4.8** Implement `FootprintParser.java` (footprint)
- [x] **4.9** Implement `AreaOfEffectParser.java` (AreaOfEffect)
- [x] **4.10** Implement `RestrictCommandsParser.java` (hideCmd)
- [x] **4.11** Implement `DynamicPropertyParser.java` (PROP)
- [x] **4.12** Implement `PlaySoundParser.java` (playSound)
- [x] **4.13** Implement `AttachmentParser.java` (attach)
- [x] **4.14** Implement `TranslateParser.java` (translate)
- [x] **4.15** Implement `CounterGlobalKeyCommandParser.java` (globalkey)
- [x] **4.16** Register all Phase 4 parsers in TraitParserRegistry
- [x] **4.17** Test: Build compiles and exports work correctly

### Test Results
- Tested with `Campaign.vsav` (complex wargame)
- All Phase 4 traits correctly parsed (e.g., footprint shows menuCommand, circleRadius, fillColor, etc.)
- Build compiles successfully with all 15 new parsers registered

### Files Created
```
vassal-tools/src/main/java/org/vassalengine/tools/vsav/traits/
├── TriggerActionParser.java            # macro - Trigger Action
├── ActionButtonParser.java             # button - Action Button
├── MenuSeparatorParser.java            # menuSeparator - Menu Separator
├── GlobalHotKeyParser.java             # globalhotkey - Global Hotkey
├── SubMenuParser.java                  # submenu - Sub-Menu
├── DeselectParser.java                 # deselect - Deselect
├── NonRectangularParser.java           # nonRect2 - Non-Rectangular
├── FootprintParser.java                # footprint - Movement Trail
├── AreaOfEffectParser.java             # AreaOfEffect - Area of Effect
├── RestrictCommandsParser.java         # hideCmd - Restrict Commands
├── DynamicPropertyParser.java          # PROP - Dynamic Property
├── PlaySoundParser.java                # playSound - Play Sound
├── AttachmentParser.java               # attach - Attachment
├── TranslateParser.java                # translate - Move Fixed Distance
└── CounterGlobalKeyCommandParser.java  # globalkey - Global Key Command
```

### Implementation Notes
- **Trait IDs differ from class names**: Always check `public static final String ID` in VASSAL source
- **Footprint** has complex state with point trail data
- **AreaOfEffect** has state for active status
- **Attachment** has state with count and attached piece IDs
- **DynamicProperty (PROP)** stores current value in state

**Deliverable**: Complete trait coverage for standard Vassal piece traits. ✅

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
