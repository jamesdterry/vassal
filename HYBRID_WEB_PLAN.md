# Vassal Web Port - Hybrid Server Architecture

## Overview

Port Vassal to the web using a **Hybrid Server** architecture: Java game engine on server, thin TypeScript/Canvas web client. Cloud-hosted deployment targeting popular modules first.

**Estimated Timeline**: 15-20 months
**Architecture**: Server-authoritative with optimistic client updates

```
┌─────────────────┐       WebSocket        ┌──────────────────┐
│   Web Client    │◄─────────────────────►│  Vassal Server   │
│                 │                        │                  │
│ - TypeScript    │  Commands (JSON) ────► │ - Java Engine    │
│ - Canvas 2D     │                        │ - GameModule     │
│ - React UI      │ ◄──── State Updates    │ - GameState      │
│                 │                        │ - Image Server   │
└─────────────────┘                        └──────────────────┘
```

## Why Hybrid Server?

1. **Maximum Compatibility** - Reuses existing Java game logic unchanged
2. **Fastest Path** - Minimal Java modifications needed
3. **Command Pattern** - Already serializes all game actions for network transmission
4. **vassal-tools Leverage** - 35+ trait parsers already convert to JSON
5. **Cloud-Friendly** - Server-based architecture fits cloud deployment model

---

## Phase 1: Server Foundation (3-4 months)

### Goal
Create headless Java server that loads modules, manages game sessions, exposes REST/WebSocket API.

### 1.1 New Maven Module: `vassal-server`
```xml
<module>vassal-server</module>
```

### 1.2 Core Server Classes

**Location**: `vassal-server/src/main/java/org/vassalengine/server/`

| Class | Purpose |
|-------|---------|
| `HeadlessGameModule` | GameModule without Swing UI |
| `GameSession` | Single game instance wrapper |
| `SessionManager` | Manages concurrent games |
| `WebSocketHandler` | Client connection handling |
| `JsonCommandEncoder` | Command ↔ JSON translation |
| `GameStateSerializer` | Full state to JSON |

### 1.3 API Endpoints

```
POST   /api/modules/upload        Upload and register module
POST   /api/games/create          Create game session
GET    /api/games/{id}/state      Get full game state (JSON)
POST   /api/games/{id}/command    Execute command
GET    /api/games/{id}/assets/*   Serve images/sounds from module
WS     /api/games/{id}/stream     Real-time command stream
```

### 1.4 Key Files to Modify

| File | Changes |
|------|---------|
| `GameModule.java` | Extract non-UI initialization to reusable methods |
| `GameState.java` | Add `toJson()` method using existing `getRestoreCommand()` |
| `BasicCommandEncoder.java` | Add JSON encoding alongside text encoding |

### 1.5 Leverage Existing Code

The `vsav-exporter` tool already demonstrates JSON export:
- `CommandParser.java` - Parse command strings
- `TraitParser.java` - Parse 35+ trait types
- `ExportData.java` - JSON data model

Extend this pattern for the server API.

---

## Phase 2: Web Client Prototype (4-5 months)

### Goal
View-only web client that displays game state from server.

### 2.1 Project Structure

```
vassal-web-client/
├── src/
│   ├── api/
│   │   ├── WebSocketClient.ts
│   │   └── RestClient.ts
│   ├── model/
│   │   ├── GameState.ts
│   │   ├── PieceState.ts
│   │   └── TraitData.ts
│   ├── render/
│   │   ├── MapRenderer.ts
│   │   ├── PieceRenderer.ts
│   │   └── traits/           # Per-trait renderers
│   └── components/
│       ├── GameBoard.tsx
│       ├── Toolbar.tsx
│       └── ChatWindow.tsx
```

### 2.2 Core Data Model

```typescript
interface GameState {
  pieces: Map<string, PieceState>;
  maps: MapState[];
  globalProperties: Record<string, string>;
  players: Player[];
}

interface PieceState {
  id: string;
  type: string;      // From GamePiece.getType()
  state: string;     // From GamePiece.getState()
  traits: TraitData[];
  position: { x: number; y: number };
  mapId: string;
}
```

### 2.3 Rendering Strategy

**Client-side rendering with server state** (not server-rendered images):
- Server sends game state as JSON
- Client renders using Canvas 2D
- Images served from `/api/games/{id}/assets/`
- Enables smooth drag-and-drop with zero latency

### 2.4 Priority Traits for Initial Implementation

Cover ~80% of popular modules:

| Trait ID | Name | Complexity |
|----------|------|------------|
| `piece` | BasicPiece | Base layer |
| `emb2` | Embellishment | Layers |
| `obs` | Obscurable | Mask/flip |
| `label` | Labeler | Text |
| `rotate` | FreeRotator | Rotation |
| `mark` | Marker | Properties |
| `PROP` | DynamicProperty | Variables |
| `prototype` | UsePrototype | Reuse |
| `delete` | Delete | Remove |
| `stack` | Stack | Grouping |

---

## Phase 3: Interaction Layer (3-4 months)

### Goal
Full piece interaction: drag-drop, keyboard commands, context menus.

### 3.1 Optimistic Updates Pattern

```typescript
class InteractionManager {
  onDragStart(piece: PieceState) {
    this.localState.startDrag(piece);  // Immediate visual
  }

  onDrop(position: Point) {
    // Optimistic local apply
    this.localState.movePiece(piece.id, position);

    // Send to server
    this.ws.send({
      type: "MOVE_PIECE",
      pieceId: piece.id,
      destination: position
    });
  }

  onServerResponse(command: Command) {
    this.reconcile(command);  // Fix any discrepancies
  }
}
```

### 3.2 Command Mapping

| User Action | Server Command |
|-------------|----------------|
| Drag piece | `MovePiece` |
| Right-click menu | Query `KeyCommand[]`, execute selected |
| Keyboard shortcut | `keyEvent(KeyStroke)` |
| Double-click stack | Local view state (expand/collapse) |

### 3.3 Key Features
- Piece selection (single, multi-select with Ctrl/Shift)
- Drag-and-drop with grid snapping
- Context menus populated from server
- Keyboard command routing
- Undo/redo via server command history

---

## Phase 4: Multiplayer (2-3 months)

### Goal
Real-time collaborative play with multiple web clients.

### 4.1 Architecture

```
              ┌────────────────┐
              │ WebSocket Hub  │
              │ (Session Mgr)  │
              └───────┬────────┘
                      │
         ┌────────────┼────────────┐
         │            │            │
    ┌────▼───┐   ┌────▼───┐   ┌────▼───┐
    │Client 1│   │Client 2│   │Client 3│
    └────────┘   └────────┘   └────────┘
```

### 4.2 Server Components

```java
// vassal-server/multiplayer/
GameRoom.java         // Game + connected clients
RoomManager.java      // All active rooms
BroadcastService.java // Send to all clients
PlayerManager.java    // Roles, permissions
```

### 4.3 Features
- Game rooms with join codes
- Player roster and roles
- Chat integration
- Observer mode
- Reconnection handling

---

## Phase 5: Cloud Deployment (2-3 months)

### Goal
Production cloud infrastructure.

### 5.1 Architecture

```
┌─────────────────────────────────────────────┐
│              Cloud Provider                  │
│  ┌─────────┐    ┌──────────┐   ┌─────────┐  │
│  │   CDN   │    │   API    │   │  Game   │  │
│  │ (Static │    │ Gateway  │   │ Servers │  │
│  │ Assets) │    │          │   │ (Java)  │  │
│  └────┬────┘    └────┬─────┘   └────┬────┘  │
│       │              │              │        │
│  ┌────▼──────────────▼──────────────▼────┐  │
│  │              Load Balancer            │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  Redis   │  │ Postgres │  │  Object  │  │
│  │ (State)  │  │ (Users)  │  │ Storage  │  │
│  └──────────┘  └──────────┘  └──────────┘  │
└─────────────────────────────────────────────┘
```

### 5.2 Components
- **API Gateway**: Route requests, auth
- **Game Servers**: Java containers (Kubernetes)
- **Redis**: Session state, pub/sub for multi-instance
- **PostgreSQL**: User accounts, game history
- **Object Storage**: Module files (S3/GCS)
- **CDN**: Static assets, images

### 5.3 Scaling Strategy
- Horizontal scaling of game servers
- Redis pub/sub for cross-instance communication
- Sticky sessions for WebSocket connections

---

## Phase 6: Polish (3-4 months)

### Features
- Module browser/uploader
- User accounts and profiles
- Game save/load to cloud
- Sound playback (Web Audio API)
- Mobile-responsive design
- Touch support for tablets
- Performance optimization
- Popular module compatibility testing

---

## Technology Stack

### Backend
- Java 11 (existing Vassal requirement)
- Spring Boot (REST/WebSocket)
- Redis (session state)
- PostgreSQL (persistence)

### Frontend
- TypeScript
- React
- Canvas 2D (rendering)
- Zustand (state management)

### Infrastructure
- Docker/Kubernetes
- AWS/GCP/Azure
- CloudFlare (CDN)

---

## Critical Files for Implementation

### Server Side
| File | Purpose |
|------|---------|
| `vassal-app/.../GameState.java` | `getRestoreCommand()` provides complete state serialization |
| `vassal-app/.../Command.java` | Base class, understand for JSON encoding |
| `vassal-app/.../BasicCommandEncoder.java` | Trait factories, command parsing |
| `vassal-app/.../GamePiece.java` | `getType()`, `getState()` contract |
| `vassal-tools/.../CommandParser.java` | Existing JSON parsing pattern |
| `vassal-tools/.../TraitParserRegistry.java` | 35+ trait parsers to reuse |

### Reference Implementation
The `vsav-exporter` tool in `vassal-tools` demonstrates:
- Command parsing to JSON
- Trait parsing to structured data
- Round-trip serialization

This is the primary reference for the server API design.

---

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Complex traits | Some modules won't render perfectly | Start with popular modules, add traits incrementally |
| Latency | Drag-drop may feel sluggish | Optimistic updates, tune reconciliation |
| Server costs | Cloud hosting expensive | Start small, add caching, optimize |
| Module compatibility | Edge cases in old modules | Focus on top 50 modules, document limitations |

---

## Success Metrics

### Phase 1 Complete
- [ ] Headless server loads modules
- [ ] REST API returns game state as JSON
- [ ] WebSocket streams commands

### Phase 2 Complete
- [ ] Web client displays static game
- [ ] Images render correctly
- [ ] 10 core traits working

### Phase 3 Complete
- [ ] Drag-and-drop works
- [ ] Context menus functional
- [ ] Keyboard commands work

### Phase 4 Complete
- [ ] Two players can play together
- [ ] Chat works
- [ ] State synchronizes

### Phase 5 Complete
- [ ] Deployed to cloud
- [ ] Handles 100+ concurrent games
- [ ] 99.9% uptime

### Phase 6 Complete
- [ ] Top 20 modules tested and working
- [ ] Mobile-friendly
- [ ] Production launch

---

## Next Steps

1. Create `vassal-server` Maven module
2. Implement `HeadlessGameModule`
3. Build `GameStateSerializer` using vsav-exporter patterns
4. Create basic REST API
5. Build minimal web client prototype

---

## Appendix: TeaVM Feasibility Analysis

### Why Not Direct TeaVM/WASM Port?

TeaVM was initially considered but has significant blockers:

| Feature | TeaVM Support | Vassal Usage |
|---------|---------------|--------------|
| **Swing/AWT** | Not supported | ~35-40% of codebase |
| **Reflection** | Heavily restricted | 21+ Class.forName calls |
| **Threading** | Green threads only | 92+ Thread/SwingWorker usages |
| **File I/O** | Not supported (browser) | Core module loading |
| **WASM backend** | "Experimental" | N/A |

The TeaVM author stated: "Wasm has proven to be extremely inappropriate to run Java."

### Alternative Approaches Considered

1. **Refactor & Transpile** - Extract game logic, transpile to JS via TeaVM
   - Pro: Single Java codebase
   - Con: Requires 50-60% code refactoring, TeaVM limitations remain

2. **TypeScript Rewrite** - Full rewrite in TypeScript
   - Pro: Modern web-native architecture
   - Con: Complete rewrite, 18+ months, compatibility risk

3. **Hybrid Server** (Chosen) - Java server + web client
   - Pro: Maximum compatibility, fastest path, leverages existing code
   - Con: Server hosting costs, latency considerations
