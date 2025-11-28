# Vassal Web Port - TypeScript Rewrite Architecture

## Overview

Full rewrite of the Vassal game engine in TypeScript for the web, reusing existing file formats (.vmod, .vsav) and leveraging vassal-tools JSON export as a bridge.

**Estimated Timeline**: 18 months
**Architecture**: Pure client-side with optional multiplayer server

```
┌─────────────────────────────────────────────────────────┐
│                     Web Browser                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ Module Loader│  │ Game Engine  │  │  Renderer    │   │
│  │ (JSZip)      │  │ (TypeScript) │  │ (Canvas 2D)  │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│                           │                              │
│                    ┌──────▼──────┐                       │
│                    │   React UI  │                       │
│                    └─────────────┘                       │
└─────────────────────────────────────────────────────────┘
                            │
                     WebSocket (optional)
                            │
                    ┌───────▼───────┐
                    │ Multiplayer   │
                    │ Server (Node) │
                    └───────────────┘
```

## Why TypeScript Rewrite?

1. **Modern Web-Native** - No transpilation quirks, full browser API access
2. **Clean Architecture** - Design from scratch without legacy Swing coupling
3. **Single Language** - Frontend and backend both TypeScript
4. **Better Tooling** - npm ecosystem, modern bundlers, hot reload
5. **No Server Required** - Can run entirely in browser for single-player

## Strategic Approach

### Leverage Existing Infrastructure

1. **vassal-tools as Bridge** - JSON export provides clean data format reference
2. **Module Format Unchanged** - Parse .vmod ZIP files directly in browser
3. **Save Format Compatible** - Read/write .vsav files for interoperability
4. **Gradual Feature Parity** - Start with core, add features incrementally

---

## Phase 1: Foundation (Months 1-3)

### Goal
Core infrastructure, module loading, basic piece representation.

### 1.1 Project Setup

```
vassal-web/
├── packages/
│   ├── core/              # @vassal-web/core - Game engine
│   │   ├── src/
│   │   │   ├── model/     # Data structures
│   │   │   ├── piece/     # Piece & trait system
│   │   │   ├── command/   # Command pattern
│   │   │   ├── module/    # Module loading
│   │   │   └── state/     # Game state management
│   │   └── package.json
│   │
│   ├── renderer/          # @vassal-web/renderer - Canvas rendering
│   │   ├── src/
│   │   │   ├── map/       # Map rendering
│   │   │   ├── piece/     # Piece rendering
│   │   │   └── traits/    # Per-trait renderers
│   │   └── package.json
│   │
│   ├── client/            # @vassal-web/client - React app
│   │   ├── src/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   └── pages/
│   │   └── package.json
│   │
│   └── server/            # @vassal-web/server - Multiplayer (Node)
│       ├── src/
│       └── package.json
│
├── tools/                 # Build tools, module compiler
├── package.json           # Workspace root
└── tsconfig.json
```

### 1.2 Core Data Model

```typescript
// @vassal-web/core/src/model/

export interface GameModule {
  name: string;
  version: string;
  description: string;
  maps: MapDefinition[];
  pieceWindows: PieceWindow[];
  prototypes: Map<string, PrototypeDefinition>;
  globalProperties: Map<string, PropertyDefinition>;
  diceButtons: DiceButton[];
}

export interface GameState {
  pieces: Map<string, GamePiece>;
  stacks: Map<string, Stack>;
  globalProperties: Map<string, string>;
  turnState: TurnState;
  players: Player[];
}

export interface GamePiece {
  id: string;
  traits: Trait[];
  position: Point;
  mapId: string | null;
  parentStackId: string | null;
}

export interface Trait {
  traitId: string;
  properties: Record<string, unknown>;

  // Methods
  getProperty(key: string): unknown;
  setProperty(key: string, value: unknown): void;
  getState(): string;
  setState(state: string): void;
}
```

### 1.3 Module Loader

```typescript
// @vassal-web/core/src/module/ModuleLoader.ts

import JSZip from 'jszip';

export class ModuleLoader {
  async load(file: File | ArrayBuffer): Promise<GameModule> {
    const zip = await JSZip.loadAsync(file);

    // Parse metadata
    const moduleData = await this.parseMetadata(zip);

    // Parse buildFile.xml
    const buildFile = await zip.file('buildFile.xml')?.async('string');
    const module = this.parseBuildFile(buildFile);

    // Create image provider
    module.imageProvider = new ZipImageProvider(zip);

    return module;
  }

  private parseBuildFile(xml: string): GameModule {
    const parser = new DOMParser();
    const doc = parser.parseFromString(xml, 'text/xml');
    return this.parseComponent(doc.documentElement);
  }

  private parseComponent(element: Element): ComponentDefinition {
    // Recursive XML parsing matching Java's Builder.build()
  }
}
```

### 1.4 Trait System Architecture

Port the Decorator pattern from Java:

```typescript
// @vassal-web/core/src/piece/Trait.ts

export abstract class Trait {
  protected inner: Trait | null;

  constructor(inner: Trait | null) {
    this.inner = inner;
  }

  abstract get traitId(): string;

  getProperty(key: string): unknown {
    const local = this.getLocalProperty(key);
    if (local !== undefined) return local;
    return this.inner?.getProperty(key);
  }

  protected abstract getLocalProperty(key: string): unknown;
  abstract getType(): string;
  abstract getState(): string;
  abstract setState(state: string): void;
}

// @vassal-web/core/src/piece/BasicPiece.ts

export class BasicPiece extends Trait {
  readonly traitId = 'piece';

  private imageName: string;
  private position: Point;
  private pieceId: string;

  constructor(type: string, state: string) {
    super(null);
    this.parseType(type);
    this.setState(state);
  }

  private parseType(type: string): void {
    // Parse: piece;image.png;name
    const parts = type.split(';');
    this.imageName = parts[1] || '';
  }
}
```

### 1.5 Trait Registry

```typescript
// @vassal-web/core/src/piece/TraitRegistry.ts

type TraitFactory = (type: string, state: string, inner: Trait | null) => Trait;

export class TraitRegistry {
  private factories = new Map<string, TraitFactory>();

  constructor() {
    this.register('piece', BasicPiece.create);
    this.register('emb2', Embellishment.create);
    this.register('obs', Obscurable.create);
    this.register('label', Labeler.create);
    this.register('rotate', FreeRotator.create);
    this.register('mark', Marker.create);
    this.register('PROP', DynamicProperty.create);
    this.register('prototype', UsePrototype.create);
    // ... 30+ more traits
  }

  register(traitId: string, factory: TraitFactory): void {
    this.factories.set(traitId, factory);
  }

  create(traitId: string, type: string, state: string, inner: Trait | null): Trait {
    const factory = this.factories.get(traitId);
    if (!factory) {
      return new UnknownTrait(traitId, type, state, inner);
    }
    return factory(type, state, inner);
  }
}
```

### 1.6 Reference: vassal-tools Trait Parsers

Use existing parsers as specification:

| Java File | TypeScript Equivalent |
|-----------|----------------------|
| `BasicPieceParser.java` | `BasicPiece.ts` |
| `EmbellishmentParser.java` | `Embellishment.ts` |
| `ObscurableParser.java` | `Obscurable.ts` |
| `FreeRotatorParser.java` | `FreeRotator.ts` |
| `DynamicPropertyParser.java` | `DynamicProperty.ts` |
| `UsePrototypeParser.java` | `UsePrototype.ts` |

**Key Reference Files:**
- `vassal-tools/src/main/java/org/vassalengine/tools/vsav/traits/`
- `vassal-tools/src/main/java/org/vassalengine/tools/vsav/model/TraitData.java`

---

## Phase 2: Rendering Engine (Months 4-6)

### Goal
Visual representation of game state on HTML5 Canvas.

### 2.1 Map Renderer

```typescript
// @vassal-web/renderer/src/map/MapRenderer.ts

export class MapRenderer {
  private canvas: HTMLCanvasElement;
  private ctx: CanvasRenderingContext2D;
  private viewport: Viewport;
  private imageCache: ImageCache;

  constructor(canvas: HTMLCanvasElement, imageCache: ImageCache) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d')!;
    this.viewport = new Viewport();
    this.imageCache = imageCache;
  }

  render(map: GameMap, pieces: GamePiece[]): void {
    this.ctx.save();

    // Apply viewport transform
    this.ctx.translate(-this.viewport.x, -this.viewport.y);
    this.ctx.scale(this.viewport.zoom, this.viewport.zoom);

    // Render layers
    this.renderBoards(map.boards);
    this.renderGrid(map.grid);
    this.renderPieces(pieces);
    this.renderSelections();
    this.renderDragPreview();

    this.ctx.restore();
  }

  private renderPieces(pieces: GamePiece[]): void {
    // Sort by draw order (stacking)
    const sorted = this.sortByDrawOrder(pieces);

    for (const piece of sorted) {
      this.pieceRenderer.render(this.ctx, piece, this.viewport.zoom);
    }
  }
}
```

### 2.2 Piece Renderer with Trait Chain

```typescript
// @vassal-web/renderer/src/piece/PieceRenderer.ts

export class PieceRenderer {
  private traitRenderers = new Map<string, TraitRenderer>();

  constructor() {
    this.register('piece', new BasicPieceRenderer());
    this.register('emb2', new EmbellishmentRenderer());
    this.register('obs', new ObscurableRenderer());
    this.register('label', new LabelerRenderer());
    this.register('rotate', new FreeRotatorRenderer());
    // ...
  }

  render(ctx: CanvasRenderingContext2D, piece: GamePiece, zoom: number): void {
    const renderCtx = new PieceRenderContext(ctx, piece, zoom);

    // Walk trait chain from innermost to outermost
    for (const trait of piece.traits) {
      const renderer = this.traitRenderers.get(trait.traitId);
      renderer?.render(renderCtx, trait);
    }
  }
}

// Example trait renderer
export class EmbellishmentRenderer implements TraitRenderer {
  render(ctx: PieceRenderContext, trait: Embellishment): void {
    const activeLayer = trait.getActiveLayer();
    if (activeLayer && activeLayer.imageName) {
      const image = ctx.getImage(activeLayer.imageName);
      if (image) {
        ctx.drawImage(image, activeLayer.xOffset, activeLayer.yOffset);
      }
    }
  }
}
```

### 2.3 Image Management

```typescript
// @vassal-web/renderer/src/ImageCache.ts

export class ImageCache {
  private cache = new Map<string, HTMLImageElement>();
  private loading = new Map<string, Promise<HTMLImageElement>>();
  private imageProvider: ImageProvider;

  async preload(imageNames: string[]): Promise<void> {
    await Promise.all(imageNames.map(name => this.get(name)));
  }

  async get(imageName: string): Promise<HTMLImageElement | null> {
    // Check cache
    if (this.cache.has(imageName)) {
      return this.cache.get(imageName)!;
    }

    // Check if already loading
    if (this.loading.has(imageName)) {
      return this.loading.get(imageName)!;
    }

    // Start loading
    const promise = this.loadImage(imageName);
    this.loading.set(imageName, promise);

    const image = await promise;
    this.cache.set(imageName, image);
    this.loading.delete(imageName);

    return image;
  }

  private async loadImage(name: string): Promise<HTMLImageElement> {
    const blob = await this.imageProvider.getImage(name);
    const url = URL.createObjectURL(blob);

    return new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => resolve(img);
      img.onerror = reject;
      img.src = url;
    });
  }
}
```

### 2.4 Viewport Controls

```typescript
// @vassal-web/renderer/src/Viewport.ts

export class Viewport {
  x = 0;
  y = 0;
  zoom = 1.0;

  private minZoom = 0.1;
  private maxZoom = 4.0;

  pan(dx: number, dy: number): void {
    this.x += dx / this.zoom;
    this.y += dy / this.zoom;
  }

  zoomAt(factor: number, screenX: number, screenY: number): void {
    const newZoom = Math.max(this.minZoom, Math.min(this.maxZoom, this.zoom * factor));

    // Zoom toward mouse position
    const worldX = this.x + screenX / this.zoom;
    const worldY = this.y + screenY / this.zoom;

    this.zoom = newZoom;

    this.x = worldX - screenX / this.zoom;
    this.y = worldY - screenY / this.zoom;
  }

  screenToWorld(screenX: number, screenY: number): Point {
    return {
      x: this.x + screenX / this.zoom,
      y: this.y + screenY / this.zoom
    };
  }
}
```

---

## Phase 3: Game State & Commands (Months 7-9)

### Goal
Full game state management with undo/redo, save/load.

### 3.1 Command Pattern

```typescript
// @vassal-web/core/src/command/Command.ts

export abstract class Command {
  abstract execute(state: GameState): void;
  abstract undo(state: GameState): void;
  abstract encode(): string;

  static decode(encoded: string): Command {
    const type = encoded.charAt(0);
    const data = encoded.substring(1);

    switch (type) {
      case '+': return AddPieceCommand.decode(data);
      case '-': return RemovePieceCommand.decode(data);
      case '/': return ChangePieceCommand.decode(data);
      case 'M': return MovePieceCommand.decode(data);
      // ...
    }
  }
}

// @vassal-web/core/src/command/AddPieceCommand.ts

export class AddPieceCommand extends Command {
  constructor(
    private pieceId: string,
    private type: string,
    private state: string,
    private mapId: string,
    private position: Point
  ) {
    super();
  }

  execute(state: GameState): void {
    const piece = PieceFactory.create(this.pieceId, this.type, this.state);
    piece.position = this.position;
    piece.mapId = this.mapId;
    state.pieces.set(this.pieceId, piece);
  }

  undo(state: GameState): void {
    state.pieces.delete(this.pieceId);
  }

  encode(): string {
    // Match Java format: +/id/type/state/mapId/x;y
    return `+/${this.pieceId}/${this.type}/${this.state}/${this.mapId}/${this.position.x};${this.position.y}`;
  }
}
```

### 3.2 Game State Manager

```typescript
// @vassal-web/core/src/state/GameStateManager.ts

export class GameStateManager {
  private state: GameState;
  private history: Command[] = [];
  private redoStack: Command[] = [];
  private listeners: StateListener[] = [];

  execute(command: Command): void {
    command.execute(this.state);
    this.history.push(command);
    this.redoStack = [];
    this.notifyListeners();
  }

  undo(): boolean {
    const command = this.history.pop();
    if (!command) return false;

    command.undo(this.state);
    this.redoStack.push(command);
    this.notifyListeners();
    return true;
  }

  redo(): boolean {
    const command = this.redoStack.pop();
    if (!command) return false;

    command.execute(this.state);
    this.history.push(command);
    this.notifyListeners();
    return true;
  }

  getRestoreCommands(): Command[] {
    // Generate commands to recreate current state
    return [
      ...this.generatePieceCommands(),
      ...this.generatePropertyCommands(),
      ...this.generateTurnCommands()
    ];
  }
}
```

### 3.3 Save/Load (.vsav Files)

```typescript
// @vassal-web/core/src/io/SaveGameWriter.ts

export class SaveGameWriter {
  async save(state: GameState, module: GameModule): Promise<Blob> {
    const zip = new JSZip();

    // Write savedata (metadata)
    zip.file('savedata', this.createSaveMetadata(module));

    // Write moduledata
    zip.file('moduledata', this.createModuleMetadata(module));

    // Write savedGame (obfuscated command stream)
    const commands = state.getRestoreCommands();
    const encoded = this.encodeCommands(commands);
    const obfuscated = this.obfuscate(encoded);
    zip.file('savedGame', obfuscated);

    return zip.generateAsync({ type: 'blob' });
  }

  private obfuscate(data: string): Uint8Array {
    // XOR obfuscation matching Java implementation
    const bytes = new TextEncoder().encode(data);
    const key = 'a]?#LszP'.split('').map(c => c.charCodeAt(0));

    for (let i = 0; i < bytes.length; i++) {
      bytes[i] ^= key[i % key.length];
    }

    return bytes;
  }
}

// @vassal-web/core/src/io/SaveGameReader.ts

export class SaveGameReader {
  async load(file: File | Blob): Promise<GameState> {
    const zip = await JSZip.loadAsync(file);

    // Read and deobfuscate savedGame
    const obfuscated = await zip.file('savedGame')?.async('uint8array');
    const decoded = this.deobfuscate(obfuscated);

    // Parse command stream
    const commands = this.parseCommands(decoded);

    // Execute commands to build state
    const state = new GameState();
    for (const command of commands) {
      command.execute(state);
    }

    return state;
  }
}
```

---

## Phase 4: Advanced Features (Months 10-12)

### Goal
Feature parity for common module patterns.

### 4.1 Additional Traits

**Priority order based on usage frequency:**

| Trait | Description | Complexity |
|-------|-------------|------------|
| `deck` | Card decks | High |
| `hide` | Invisible pieces | Medium |
| `stack` | Piece stacking | Medium |
| `footprint` | Movement trails | Medium |
| `placeMarker` | Spawn pieces | Medium |
| `sendToLocation` | Move to location | Low |
| `returnToDeck` | Return cards | Low |
| `globalKeyCommand` | Batch operations | High |
| `reportFormat` | Chat messages | Low |
| `restrictCommands` | Permission | Medium |

### 4.2 Grid System

```typescript
// @vassal-web/core/src/map/grid/Grid.ts

export interface Grid {
  type: 'hex' | 'square' | 'irregular' | 'region';
  snapTo(point: Point): Point;
  getLocationName(point: Point): string;
  getRange(p1: Point, p2: Point): number;
  getNeighbors(point: Point): Point[];
}

// @vassal-web/core/src/map/grid/HexGrid.ts

export class HexGrid implements Grid {
  type: 'hex' = 'hex';

  constructor(
    private dx: number,      // Hex width
    private dy: number,      // Hex height
    private xOffset: number,
    private yOffset: number,
    private sideways: boolean  // Flat-top vs pointy-top
  ) {}

  snapTo(point: Point): Point {
    // Convert to hex coordinates, round, convert back
    const hex = this.pixelToHex(point);
    const rounded = this.roundHex(hex);
    return this.hexToPixel(rounded);
  }

  getRange(p1: Point, p2: Point): number {
    const h1 = this.pixelToHex(p1);
    const h2 = this.pixelToHex(p2);
    return this.hexDistance(h1, h2);
  }

  // Hex math implementation...
}
```

### 4.3 Dice System

```typescript
// @vassal-web/core/src/dice/DiceRoller.ts

export class DiceRoller {
  roll(expression: string): DiceResult {
    // Parse: "2d6+3", "1d20", "3d8-2"
    const parsed = this.parseExpression(expression);
    const rolls = this.generateRolls(parsed);
    const total = this.calculateTotal(rolls, parsed.modifier);

    return {
      expression,
      rolls,
      modifier: parsed.modifier,
      total
    };
  }

  private generateRolls(parsed: DiceExpression): number[] {
    const rolls: number[] = [];
    for (let i = 0; i < parsed.count; i++) {
      rolls.push(this.random(1, parsed.sides));
    }
    return rolls;
  }

  private random(min: number, max: number): number {
    // Cryptographically secure random
    const range = max - min + 1;
    const array = new Uint32Array(1);
    crypto.getRandomValues(array);
    return min + (array[0] % range);
  }
}
```

### 4.4 Expression Evaluator

Replace BeanShell with custom evaluator:

```typescript
// @vassal-web/core/src/expression/ExpressionEvaluator.ts

export class ExpressionEvaluator {
  evaluate(expression: string, context: PropertyContext): unknown {
    const ast = this.parse(expression);
    return this.evaluateNode(ast, context);
  }

  // Supported syntax:
  // - Property access: PropertyName, $PropertyName$
  // - Arithmetic: +, -, *, /, %
  // - Comparison: ==, !=, <, >, <=, >=
  // - Boolean: &&, ||, !
  // - Ternary: condition ? true : false
  // - String concat: "text" + value
  // - Functions: Math.max(), Math.min(), etc.
}
```

---

## Phase 5: Multiplayer (Months 13-15)

### Goal
Real-time collaborative play via WebSocket.

### 5.1 Protocol Design

```typescript
// Shared types
interface GameMessage {
  type: 'command' | 'chat' | 'sync' | 'join' | 'leave';
  sequence: number;
  timestamp: number;
  playerId: string;
}

interface CommandMessage extends GameMessage {
  type: 'command';
  command: string;  // Encoded command
}

interface SyncMessage extends GameMessage {
  type: 'sync';
  commands: string[];  // Full state as commands
}
```

### 5.2 Client

```typescript
// @vassal-web/core/src/multiplayer/MultiplayerClient.ts

export class MultiplayerClient {
  private socket: WebSocket;
  private pendingCommands = new Map<number, Command>();
  private sequence = 0;

  connect(roomId: string, playerId: string): Promise<void> {
    return new Promise((resolve, reject) => {
      this.socket = new WebSocket(`wss://server/games/${roomId}`);
      this.socket.onopen = () => resolve();
      this.socket.onerror = reject;
      this.socket.onmessage = (e) => this.handleMessage(JSON.parse(e.data));
    });
  }

  sendCommand(command: Command): void {
    const seq = this.sequence++;
    this.pendingCommands.set(seq, command);

    // Optimistic local execution
    this.gameState.execute(command);

    // Send to server
    this.socket.send(JSON.stringify({
      type: 'command',
      sequence: seq,
      command: command.encode()
    }));
  }

  private handleMessage(message: GameMessage): void {
    switch (message.type) {
      case 'command':
        this.handleRemoteCommand(message as CommandMessage);
        break;
      case 'sync':
        this.handleSync(message as SyncMessage);
        break;
    }
  }
}
```

### 5.3 Server (Node.js)

```typescript
// @vassal-web/server/src/GameRoom.ts

export class GameRoom {
  private clients = new Map<string, WebSocket>();
  private gameState: GameState;
  private commandLog: Command[] = [];

  handleCommand(playerId: string, message: CommandMessage): void {
    const command = Command.decode(message.command);

    // Validate command
    if (!this.validateCommand(playerId, command)) {
      this.sendError(playerId, 'Invalid command');
      return;
    }

    // Execute
    command.execute(this.gameState);
    this.commandLog.push(command);

    // Broadcast to all clients
    this.broadcast({
      type: 'command',
      sequence: this.commandLog.length,
      playerId,
      command: message.command
    });
  }

  private broadcast(message: GameMessage): void {
    const data = JSON.stringify(message);
    for (const socket of this.clients.values()) {
      socket.send(data);
    }
  }
}
```

---

## Phase 6: Polish (Months 16-18)

### Features
- Progressive Web App (offline support)
- Module browser and search
- User accounts and profiles
- Game history and replay
- Sound effects (Web Audio API)
- Mobile touch optimization
- Performance profiling and optimization
- Compatibility testing with popular modules

### PWA Configuration

```typescript
// service-worker.ts
const CACHE_NAME = 'vassal-web-v1';

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll([
        '/',
        '/index.html',
        '/main.js',
        '/main.css'
      ]);
    })
  );
});

self.addEventListener('fetch', (event) => {
  // Cache-first for static assets, network-first for API
});
```

---

## Technology Stack

### Frontend
- **TypeScript** - Full type safety
- **React 18** - UI components
- **Zustand** - State management
- **Canvas 2D** - Primary rendering
- **PixiJS** (optional) - WebGL for complex modules
- **JSZip** - Module/save file handling
- **Vite** - Build tooling

### Backend (Multiplayer)
- **Node.js** - Runtime
- **TypeScript** - Same language as frontend
- **ws** - WebSocket library
- **Redis** - Session state, pub/sub
- **PostgreSQL** - Persistence

### Infrastructure
- **Vercel/Netlify** - Frontend hosting
- **Railway/Fly.io** - Backend hosting
- **CloudFlare** - CDN, edge caching

---

## Module Compatibility Estimate

Based on trait implementation priority:

| Phase | Coverage | Modules Supported |
|-------|----------|-------------------|
| Phase 1-2 | 40% | Simple counter games |
| Phase 3 | 60% | Basic wargames |
| Phase 4 | 80% | Most card games, hex games |
| Phase 6 | 90%+ | Popular modules fully tested |

### Known Limitations

1. **Custom Java Classes** - Modules with custom Java code won't work
2. **Complex Expressions** - Some BeanShell scripts need manual porting
3. **Obscure Traits** - ~10 rarely-used traits deferred
4. **Extensions** - .vext files need separate handling

---

## Critical Files for Reference

### vassal-tools (Primary Reference)
| File | Purpose |
|------|---------|
| `TraitParserRegistry.java` | All trait IDs and parser registration |
| `AbstractTraitParser.java` | Base parsing pattern |
| `CommandParser.java` | Command encoding/decoding |
| `VsavReader.java` | Save file reading |
| `VsavWriter.java` | Save file writing |
| `ExportData.java` | JSON data model |

### vassal-app (Understanding Originals)
| File | Purpose |
|------|---------|
| `GamePiece.java` | Piece interface contract |
| `Decorator.java` | Trait chain pattern |
| `BasicCommandEncoder.java` | Command encoding |
| `HexGrid.java` | Hex math reference |
| `SquareGrid.java` | Square grid reference |

---

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Feature creep | Timeline slip | Strict phase gates, MVP focus |
| Compatibility issues | User frustration | Start with known working modules |
| Performance | Poor UX | Profile early, optimize hot paths |
| Spec drift | Implementation bugs | Use vassal-tools as ground truth |

---

## Success Metrics

### Phase 2 Complete
- [ ] Load .vmod in browser
- [ ] Display static board with pieces
- [ ] Pan/zoom working

### Phase 3 Complete
- [ ] Full game playable solo
- [ ] Save/load .vsav files
- [ ] Undo/redo working

### Phase 4 Complete
- [ ] Decks working (draw, shuffle)
- [ ] Grids snapping correctly
- [ ] Dice rolling

### Phase 5 Complete
- [ ] Two players can connect
- [ ] Real-time sync working
- [ ] Chat functional

### Phase 6 Complete
- [ ] Top 20 modules tested
- [ ] PWA installable
- [ ] Production launch

---

## Comparison: TypeScript Rewrite vs Hybrid Server

| Aspect | TypeScript Rewrite | Hybrid Server |
|--------|-------------------|---------------|
| **Time to MVP** | 9 months | 6 months |
| **Total timeline** | 18 months | 15-20 months |
| **Server costs** | Minimal (static hosting) | Higher (Java servers) |
| **Offline support** | Full | Limited |
| **Module compatibility** | ~90% achievable | ~99% |
| **Maintenance** | Single codebase (TS) | Two codebases (Java+TS) |
| **Performance** | Excellent (native JS) | Good (network latency) |
| **Development team** | TS/React developers | Java + TS developers |

### When to Choose TypeScript Rewrite
- Want single-language codebase
- Prioritize offline/PWA support
- Have strong TypeScript team
- Willing to accept some compatibility trade-offs
- Want minimal ongoing server costs
