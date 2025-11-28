# Next-Generation Vassal Architecture Plan

## Executive Summary

A 25-year architecture for Vassal built on **TypeScript** with a **platform-agnostic core**, enabling deployment to web, mobile, and desktop from a single codebase. The existing `vassal-tools` trait parsers serve as the specification for the new engine.

---

## Core Architectural Principles

### 1. Four-Layer Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                     PLATFORM ADAPTERS                         │
│   Web (Canvas/React)  |  Mobile (React Native)  |  Desktop   │
└─────────────────────────────────┬────────────────────────────┘
                                  │ Render Commands / User Events
┌─────────────────────────────────▼────────────────────────────┐
│                     PRESENTATION LAYER                        │
│   - Viewport management                                       │
│   - Input normalization (touch/mouse/keyboard → events)      │
│   - Render tree generation (what to draw, not how)           │
└─────────────────────────────────┬────────────────────────────┘
                                  │ State Queries / Action Dispatch
┌─────────────────────────────────▼────────────────────────────┐
│                      GAME ENGINE CORE                         │
│   - Piece/Trait system (zero platform dependencies)           │
│   - Command pattern with undo/redo                            │
│   - Game state management                                     │
│   - Grid/Zone calculations                                    │
└─────────────────────────────────┬────────────────────────────┘
                                  │ Read/Write
┌─────────────────────────────────▼────────────────────────────┐
│                        DATA LAYER                             │
│   - Module loading (.vmod / .vmod2)                           │
│   - Save/Load (.vsav / .vsav2)                                │
│   - Multiplayer sync protocol                                 │
└──────────────────────────────────────────────────────────────┘
```

### 2. Technology Stack

| Layer | Technology | Rationale |
|-------|------------|-----------|
| Language | TypeScript | Largest contributor pool, runs everywhere |
| Core | Zero dependencies | Portability, 25-year stability |
| Web | Canvas 2D + React | Standard web platform |
| Mobile | React Native / Capacitor | Code sharing with web |
| Desktop | Electron / Tauri | Native desktop experience |
| Multiplayer | WebSocket + JSON | Universal, debuggable |
| Build | Turborepo monorepo | Fast builds, clear boundaries |

---

## Project Structure

```
vassal-next/
├── packages/
│   ├── core/               # @vassal/core - Zero dependencies
│   │   ├── src/
│   │   │   ├── model/      # GameModule, GamePiece, Trait interfaces
│   │   │   ├── traits/     # All 40+ trait implementations
│   │   │   ├── commands/   # Command pattern implementations
│   │   │   ├── grids/      # Hex, square, region grids
│   │   │   └── io/         # Module/save file parsing
│   │   └── README.md       # "How to add a new trait"
│   │
│   ├── renderer/           # @vassal/renderer - Render tree → Canvas
│   ├── ui/                 # @vassal/ui - React components
│   ├── multiplayer/        # @vassal/multiplayer - WebSocket client
│   └── server/             # @vassal/server - Node.js game server
│
├── apps/
│   ├── web/                # Main web application
│   ├── desktop/            # Electron wrapper
│   └── mobile/             # React Native app
│
├── tools/
│   ├── module-converter/   # Convert legacy .vmod to .vmod2
│   └── compat-checker/     # Test module compatibility
│
└── docs/
    ├── architecture/
    └── guides/
        └── adding-a-trait.md
```

---

## Core Engine Design

### Trait System (Platform-Agnostic)

```typescript
// @vassal/core - The heart of the engine

interface Trait {
  readonly traitId: string;
  getType(): string;       // Immutable definition
  getState(): string;      // Mutable runtime state
  setState(state: string): void;
  getProperty(key: string): unknown;
}

interface GamePiece {
  readonly id: string;
  traits: Trait[];
  position: Point;
  mapId: string | null;
  getProperty(key: string): unknown;
}

// Registry pattern for extensibility
class TraitRegistry {
  register(traitId: string, factory: TraitFactory): void;
  create(traitId: string, type: string, state: string): Trait;
}
```

### Render Tree Pattern (Not Platform-Specific Drawing)

```typescript
// Generate drawing instructions, not pixels
type RenderNode = ImageNode | TextNode | ShapeNode | GroupNode;

interface ImageNode {
  type: 'image';
  imagePath: string;  // Logical path, not URL
  x: number; y: number;
  width: number; height: number;
  rotation: number;
  opacity: number;
}

// Platform adapters interpret render nodes
interface RenderAdapter {
  render(node: RenderNode): void;
}

// Web adapter draws to Canvas
class CanvasAdapter implements RenderAdapter { ... }

// Mobile adapter creates React Native elements
class ReactNativeAdapter implements RenderAdapter { ... }
```

---

## New Module Format (.vmod2)

Replace Java class names in XML with declarative JSON:

### Current (Problem)
```xml
<VASSAL.build.GameModule name="My Game">
  <VASSAL.build.module.Map mapName="Main"/>
</VASSAL.build.GameModule>
```

### Proposed (.vmod2)
```json
{
  "$schema": "https://vassalengine.org/schemas/vmod2/1.0.json",
  "formatVersion": "1.0",
  "module": {
    "name": "My Game",
    "version": "1.0"
  },
  "components": [
    {
      "type": "map",
      "id": "main-map",
      "name": "Main Map"
    }
  ]
}
```

### Piece Definition (Declarative Traits)
```json
{
  "piece": {
    "name": "Infantry",
    "baseImage": "infantry.png",
    "traits": [
      {
        "type": "layer",
        "activateOn": "S",
        "images": ["str-4.png", "str-3.png"],
        "property": "Strength"
      },
      {
        "type": "rotate",
        "facings": 8
      },
      {
        "type": "mask",
        "image": "hidden.png",
        "owner": "$PlayerSide$"
      }
    ]
  }
}
```

---

## Migration Strategy

### Phase 0: Foundation (Months 1-3)
- Create `vassal-next/` monorepo alongside existing Java
- Port 5 core traits: BasicPiece, Embellishment, Obscurable, Label, Marker
- Use `vassal-tools` JSON export as test oracle
- Create module loader that reads existing .vmod files

### Phase 1: Core Engine (Months 4-9)
- Port remaining 35+ traits (parallelizable - good for contributors)
- Implement command pattern with undo/redo
- Implement save/load (.vsav files)
- Add grid systems (hex, square)

### Phase 2: Interactive Web Client (Months 10-15)
- Canvas rendering with pan/zoom
- Drag-drop interaction
- Context menus, keyboard shortcuts
- Dice rolling, chat window

### Phase 3: Multiplayer & Polish (Months 16-20)
- WebSocket-based real-time sync
- Progressive Web App (offline support)
- Module compatibility testing (top 50 modules)
- Performance optimization

### Phase 4: Mobile & Desktop (Months 21-24)
- React Native iOS/Android apps
- Electron desktop app
- Unified component library

---

## Contributor Experience Design

### Trait Porting as "Good First Issues"

Each of the 40+ traits is an independent contribution opportunity:

1. Find Java source: `vassal-app/.../VASSAL/counters/YourTrait.java`
2. Reference parser: `vassal-tools/.../traits/YourTraitParser.java`
3. Create TypeScript: `packages/core/src/traits/YourTrait.ts`
4. Add tests using existing .vsav files as test vectors
5. Register in TraitRegistry

### Self-Documenting Code

```typescript
/**
 * Embellishment (Layer) Trait
 *
 * Displays different images based on a property value.
 *
 * VASSAL ID: emb2
 * @see https://vassalengine.org/wiki/Embellishment
 * @see vassal-app/.../counters/Embellishment.java
 */
export class Embellishment implements Trait {
  static readonly TRAIT_ID = 'emb2';
  // ...
}
```

---

## Critical Reference Files

| File | Purpose |
|------|---------|
| `vassal-tools/.../TraitParserRegistry.java` | Complete list of all trait IDs |
| `vassal-tools/.../AbstractTraitParser.java` | Parsing pattern to replicate |
| `vassal-tools/.../model/TraitData.java` | Platform-agnostic trait model |
| `vassal-app/.../counters/Decorator.java` | Trait chain pattern (lines 68-78) |
| `vassal-app/.../BasicCommandEncoder.java` | Trait factory registrations |
| `vassal-app/.../command/Command.java` | Command pattern base class |

---

## Key Design Decisions

### Why TypeScript?
- Largest developer community (volunteer sustainability)
- Runs on every platform (web, Node, mobile, desktop)
- 25-year platform stability (JavaScript is the web's assembly language)
- Modern tooling with excellent DX
- Existing `vassal-tools` JSON model proves the data layer works

### Why Render Tree (Not Direct Drawing)?
- Core engine has zero platform dependencies
- Same game logic works on Canvas, React Native, WebGL
- Enables future platforms without core changes
- Simplifies testing (render tree is just data)

### Why New Module Format?
- Current format requires Java class loading
- JSON is language-agnostic and 25+ years stable
- Schema validation catches errors early
- Human-readable and editable
- Backward compatibility via conversion tool

### Why Monorepo?
- Clear package boundaries aid contributor focus
- Shared tooling and configuration
- Atomic cross-package changes
- Single version control history

---

## Key User Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Compatibility target | ~90% acceptable | Focus on popular modules; document limitations |
| Extension mechanism | Sandboxed JavaScript | Allows complex logic with security review |
| First platform | Web browser | Fastest deployment, widest reach, easiest contributor testing |

---

## Extension Mechanism: Sandboxed JavaScript

For complex module behaviors that can't be expressed declaratively:

```json
{
  "scripts": {
    "combat.js": {
      "language": "javascript",
      "sandbox": true,
      "exports": ["resolveCombat", "calculateRange"]
    }
  },
  "traits": [
    {
      "type": "trigger",
      "name": "Attack",
      "key": "A",
      "script": "combat.js",
      "function": "resolveCombat"
    }
  ]
}
```

**Sandbox constraints:**
- No network access
- No file system access
- Limited execution time (timeout)
- Read-only access to game state (mutations via commands only)
- Whitelisted APIs only (Math, Array, Object, String)

---

## Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| Module compatibility gaps | Use vassal-tools JSON as test oracle; focus on top 50 modules |
| Contributor burnout | Trait porting is parallelizable; clear documentation |
| TypeScript ecosystem churn | Pin dependencies; use only stable APIs |
| Performance in browser | Canvas 2D for simple modules; optional WebGL path |
| Complex expressions (BeanShell) | Implement subset; document limitations |
| Script security | Sandbox isolation; code review for module marketplace |

---

## Success Metrics

| Milestone | Criteria |
|-----------|----------|
| Phase 0 Complete | 5 traits pass round-trip tests; module loads in browser |
| Phase 1 Complete | All traits ported; save/load works; grids functional |
| Phase 2 Complete | Full game playable solo in browser |
| Phase 3 Complete | 2 players can play real-time; top 20 modules tested |
| Phase 4 Complete | iOS/Android apps in stores; desktop app available |

---

## Comparison: New Architecture vs Alternatives

| Aspect | This Plan (TypeScript) | Hybrid Server | Keep Java |
|--------|----------------------|---------------|-----------|
| Platform reach | Web + Mobile + Desktop | Web only | Desktop only |
| Offline play | Full | Limited | Full |
| Server costs | Minimal | Ongoing | None |
| Module compatibility | ~90% | ~99% | 100% |
| Contributor pool | Largest | Split (Java+TS) | Shrinking |
| 25-year viability | Excellent | Good | Poor |
