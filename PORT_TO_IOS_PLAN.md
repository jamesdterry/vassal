# Vassal iOS Port - Investigation of Approaches

## Recommendation: Gluon Mobile (JavaFX)

Based on your requirements:
- **Timeline**: 12-18 months ✓
- **Offline play**: Required ✓
- **Platform**: iOS/iPad only ✓
- **Team**: Can hire ✓

**Primary Recommendation**: **Gluon Mobile (JavaFX → Native iOS)**

**Why Gluon Mobile?**
1. Fits 12-18 month timeline
2. Game logic stays in Java (minimal changes)
3. Only UI layer needs rewriting (Swing → JavaFX)
4. Compiles to native iOS binary via GraalVM
5. Full offline support
6. Commercial support available from GluonHQ
7. Actively maintained with regular releases

**Alternative to Consider**: Swift/SwiftUI native rewrite would give the absolute best iOS experience but likely exceeds 18 months.

---

## Gluon Mobile Implementation Roadmap

### Phase 1: Foundation & Proof of Concept (Months 1-3)

**Goal**: Get a basic Vassal module displaying on iOS simulator

1. **Setup**
   - Install Gluon tools, GraalVM, Xcode
   - Create new `vassal-ios` Maven module
   - Configure Gluon Maven plugin

2. **Core Module Loading**
   - Port `DataArchive` for module reading (no Swing dependencies)
   - Port `Builder` XML parsing
   - Create JavaFX-based image loading

3. **Minimal Rendering**
   - JavaFX Canvas for map display
   - Render board images
   - Display static pieces (BasicPiece only)

4. **iOS Build**
   - Configure GraalVM native-image
   - Test on iOS simulator
   - First build on physical iPad

### Phase 2: Game Logic Layer (Months 4-6)

**Goal**: Core game mechanics working

1. **Trait System**
   - Port trait decorator pattern (no UI code)
   - Start with: BasicPiece, Embellishment, Obscurable, FreeRotator
   - Leverage vassal-tools trait parsers as reference

2. **Command Pattern**
   - Port Command hierarchy
   - Implement undo/redo
   - Test game state consistency

3. **File I/O**
   - .vsav reading (use existing obfuscation code)
   - .vsav writing
   - Test round-trip save/load

### Phase 3: UI Implementation (Months 7-10)

**Goal**: Playable UI

1. **Map Interaction**
   - Pan/zoom (touch gestures)
   - Piece selection
   - Drag-and-drop

2. **JavaFX Components**
   - Piece palette window
   - Turn tracker
   - Chat window (for notes)
   - Dice roller

3. **Context Menus**
   - Port KeyCommand system
   - Right-click → long-press conversion
   - Keyboard shortcuts → toolbar buttons

4. **Additional Traits**
   - Label, Marker, DynamicProperty
   - Stack, Delete, PlaceMarker
   - Global Key Commands

### Phase 4: Advanced Features (Months 11-14)

**Goal**: Module compatibility

1. **Complex Traits**
   - Decks (card games)
   - Grids (hex, square)
   - Footprint (movement trails)
   - Report formatting

2. **Performance**
   - Image caching
   - Render optimization
   - Memory management

3. **Touch Optimization**
   - iPad-specific layouts
   - Multi-touch gestures
   - Apple Pencil support (optional)

### Phase 5: Polish & Release (Months 15-18)

**Goal**: App Store ready

1. **Module Browser**
   - Download .vmod files
   - Local storage management

2. **Testing**
   - Top 20 popular modules
   - Performance profiling
   - Crash reporting (Crashlytics)

3. **App Store**
   - Apple Developer account
   - App signing
   - TestFlight beta
   - App Store submission

---

### Key Vassal Files to Preserve (Game Logic)

These stay mostly unchanged:
- `VASSAL/build/module/GameState.java` - state management
- `VASSAL/command/Command.java` hierarchy - all commands
- `VASSAL/counters/*.java` - trait implementations (strip Swing dependencies)
- `VASSAL/tools/io/ZipArchive.java` - file handling

### Key Vassal Files to Replace (UI Layer)

These need JavaFX equivalents:
- `VASSAL/build/module/map/MapWindow.java` → JavaFX Canvas
- `VASSAL/build/widget/PieceSlot.java` → JavaFX drag source
- `VASSAL/build/module/PieceWindow.java` → JavaFX palette
- All Swing JPanel/JFrame classes

### Estimated Code Impact

| Category | Files | Effort |
|----------|-------|--------|
| Keep as-is | 40% | Low |
| Strip Swing deps | 25% | Medium |
| Rewrite to JavaFX | 35% | High |

---

## Executive Summary

There is **no direct Swing-to-iOS port** available. Apple does not allow JVMs on iOS, so any Java-to-iOS solution must compile/transpile to native code. The research identified 8 viable approaches with varying tradeoffs.

## Swing on iOS: The Bad News

**No one has successfully ported Java Swing to iOS.** Key findings:
- [J2ObjC (Google)](https://groups.google.com/g/j2objc-discuss/c/MUPYCN1m41Y) explicitly states it doesn't support AWT or Swing
- [Codename One](https://groups.google.com/g/codenameone-discussions/c/SsDHNk10sbk) - Swing-inspired API but NOT Swing itself
- [QMole](https://chriskohlhepp.wordpress.com/java-on-iphone-and-ipad/) - Full Swing via Cydia (requires jailbreak - not viable)

The only "zero-code" options are web-based (Webswing, AjaxSwing) which run Swing on a server and display in iPad Safari.

---

## Approach Comparison Matrix

| Approach | Timeline | Offline? | Compatibility | UI Quality | Effort |
|----------|----------|----------|---------------|------------|--------|
| 1. Webswing (server) | 1-2 months | No | 99% | Fair | Very Low |
| 2. Hybrid Server + iOS Client | 18-24 months | Limited | 99% | Good | High |
| 3. TypeScript + Capacitor | 20-24 months | Yes | 90% | Good | High |
| 4. Gluon Mobile (JavaFX) | 12-18 months | Yes | 85% | Good | Medium-High |
| 5. Codename One | 12-15 months | Yes | 80% | Fair | Medium |
| 6. Swift Native Rewrite | 24-30 months | Yes | 95%+ | Excellent | Very High |
| 7. Kotlin Multiplatform | 18-24 months | Yes | 90% | Good-Excellent | High |
| 8. libGDX + RoboVM | 15-20 months | Yes | 85% | Good | Medium-High |

---

## Detailed Analysis of Each Approach

### Approach 1: Webswing (Zero-Code Web Access)

**How it works**: Run Vassal on a Linux server with [Webswing](https://www.webswing.org/en), access via iPad Safari.

**Architecture**:
```
iPad Safari ◄───── HTTPS ─────► Webswing Server ◄──► Vassal (Java Swing)
             (HTML5 Canvas)           (Linux)
```

**Pros**:
- Immediate - works with current Vassal codebase unchanged
- 99%+ compatibility
- No App Store approval needed

**Cons**:
- Requires constant network connection
- Latency affects drag-and-drop feel
- Server hosting costs
- Not a "real" iOS app

**Effort**: Deploy Webswing server, configure HTTPS, optimize for touch input.

**Best for**: Quick proof of concept, internal use, testing.

---

### Approach 2: Hybrid Server + Native iOS Client

**How it works**: Extend existing HYBRID_WEB_PLAN.md with a native iOS client instead of (or in addition to) the web client.

**Architecture**:
```
┌─────────────────┐       WebSocket        ┌──────────────────┐
│  iOS App        │◄─────────────────────►│  Vassal Server   │
│  (Swift/SwiftUI)│                        │  (Java Engine)   │
│                 │   Commands (JSON) ───► │                  │
│  - Canvas/Metal │                        │  - HeadlessModule│
│  - Native UI    │ ◄──── State Updates    │  - GameState     │
└─────────────────┘                        └──────────────────┘
```

**Pros**:
- Reuses Java game engine (99% compatibility)
- Native iOS look and feel
- App Store distribution
- Leverages existing HYBRID_WEB_PLAN infrastructure

**Cons**:
- Requires network for gameplay
- Two codebases to maintain (server + iOS)
- Server hosting costs

**Implementation**:
1. Complete Phase 1-2 of HYBRID_WEB_PLAN (server foundation)
2. Create `vassal-ios` Swift project
3. Implement Canvas/Metal rendering for pieces
4. WebSocket client for real-time sync
5. SwiftUI for menus, piece palettes, chat

**Effort**: ~18-24 months total (includes server work from HYBRID_WEB_PLAN).

---

### Approach 3: TypeScript Rewrite + Capacitor/React Native

**How it works**: Complete the TYPESCRIPT_REWRITE_PLAN.md, then wrap for iOS using Capacitor or React Native.

**Architecture**:
```
┌──────────────────────────────────────────────┐
│              iOS App (Capacitor)              │
│  ┌────────────────────────────────────────┐  │
│  │         WKWebView Container             │  │
│  │  ┌──────────────────────────────────┐  │  │
│  │  │    TypeScript Engine + Canvas     │  │  │
│  │  │    React UI                        │  │  │
│  │  └──────────────────────────────────┘  │  │
│  └────────────────────────────────────────┘  │
│           Native Plugins (files, etc.)       │
└──────────────────────────────────────────────┘
```

**Pros**:
- Single codebase for Web + iOS + Android
- Full offline support
- No server required for single player
- Modern tooling (npm, TypeScript)

**Cons**:
- Largest rewrite effort
- ~90% module compatibility (some complex traits won't port)
- WebView performance slightly below native

**Tools**:
- [Capacitor](https://capacitorjs.com/) - Wrap web app in native shell
- Or React Native Web - Shared React codebase

**Effort**: ~20-24 months (TypeScript rewrite + Capacitor integration).

---

### Approach 4: Gluon Mobile (JavaFX → Native iOS)

**How it works**: Port Vassal's UI from Swing to JavaFX, use [Gluon](https://gluonhq.com/products/mobile/) to compile to native iOS.

**How Gluon works**:
- Uses GraalVM Native Image to compile Java to native code
- JavaFX provides the UI toolkit
- Generates actual Xcode project and native binary

**Architecture**:
```
┌───────────────────────────────────────────┐
│           iOS Native Binary                │
│  ┌───────────────────────────────────┐    │
│  │         JavaFX UI Layer            │    │
│  │  (Compiled to native via GraalVM)  │    │
│  └───────────────────────────────────┘    │
│  ┌───────────────────────────────────┐    │
│  │      Vassal Core (Java)            │    │
│  │  (Game logic, unchanged)           │    │
│  └───────────────────────────────────┘    │
└───────────────────────────────────────────┘
```

**Pros**:
- Stays in Java ecosystem
- Game logic can remain largely unchanged
- Native iOS performance
- [Actively maintained](https://gluonhq.com/) with commercial support

**Cons**:
- Must rewrite all Swing UI to JavaFX (~35-40% of codebase)
- JavaFX mobile has some rough edges
- Build requires macOS + Xcode
- Some reflection limitations with GraalVM

**Key References**:
- [Getting Started with Gluon](https://docs.gluonhq.com/getting-started/)
- [Gluon Mobile](https://gluonhq.com/products/mobile/)
- [Java on iOS, for real](https://gluonhq.com/java-on-ios-for-real/)

**Effort**: 12-18 months (UI rewrite + mobile optimization).

---

### Approach 5: Codename One

**How it works**: Port to [Codename One](https://www.codenameone.com/) API, which transpiles Java to Objective-C.

**How it works**:
- Java bytecode → C source code (ParparVM)
- Generates real Xcode project
- UI uses Codename One's cross-platform components (not Swing)

**Pros**:
- Stays in Java
- Write once, run on iOS + Android
- [GPL + Commercial license](https://www.codenameone.com/)
- Good documentation and community

**Cons**:
- Must rewrite UI to Codename One API (similar effort to JavaFX)
- UI won't be 100% native iOS look
- Some limitations on complex UI patterns
- Cloud builds (or complex local setup)

**Key References**:
- [Codename One GitHub](https://github.com/codenameone/CodenameOne)
- [Developer Guide](https://www.codenameone.com/developer-guide.html)

**Effort**: 12-15 months.

---

### Approach 6: Swift Native Rewrite

**How it works**: Complete rewrite in Swift with SwiftUI for iOS-first experience.

**Architecture**:
```
┌──────────────────────────────────────────────┐
│              iOS App (Swift)                  │
│  ┌──────────────────────────────────────────┐│
│  │         SwiftUI / UIKit UI                ││
│  └──────────────────────────────────────────┘│
│  ┌──────────────────────────────────────────┐│
│  │      Game Engine (Swift)                  ││
│  │  - Trait system                           ││
│  │  - Command pattern                        ││
│  │  - Module loader                          ││
│  └──────────────────────────────────────────┘│
│  ┌──────────────────────────────────────────┐│
│  │      File Format Compatibility            ││
│  │  - .vmod parsing (ZIP + XML)              ││
│  │  - .vsav reading/writing                  ││
│  └──────────────────────────────────────────┘│
└──────────────────────────────────────────────┘
```

**Pros**:
- Best possible iOS experience
- Apple's preferred approach (latest APIs, SwiftUI)
- Metal for graphics (excellent performance)
- No cross-platform compromises
- Could target macOS too (SwiftUI)

**Cons**:
- Complete rewrite - longest timeline
- Vassal-only (doesn't help web or Android)
- Requires iOS/Swift expertise

**Reference**: [How to Develop an iPad Board Game App](https://www.kodeco.com/2774-how-to-develop-an-ipad-board-game-app-part-1-2) from Kodeco.

**Effort**: 24-30 months.

---

### Approach 7: Kotlin Multiplatform

**How it works**: Rewrite core engine in Kotlin, share with iOS via Kotlin/Native. Native SwiftUI for iOS UI.

**Architecture**:
```
                  ┌─────────────────────────┐
                  │   Shared Kotlin Core     │
                  │  - Game logic            │
                  │  - Trait system          │
                  │  - Command pattern       │
                  │  - File I/O              │
                  └─────────────────────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
    ┌─────▼─────┐    ┌─────▼─────┐    ┌─────▼─────┐
    │  iOS App   │    │  Android  │    │   JVM     │
    │  (Swift UI)│    │  (Compose)│    │  (Desktop)│
    └───────────┘    └───────────┘    └───────────┘
```

**Pros**:
- Modern language with excellent tooling
- JetBrains actively maintains ([Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/))
- Share 60-80% of code across platforms
- Native UI on each platform
- Growing ecosystem of libraries

**Cons**:
- Kotlin learning curve (though similar to Java)
- Rewrite effort similar to TypeScript
- iOS interop has some rough edges
- Build complexity

**Key References**:
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [JetBrains KMP Guide](https://www.jetbrains.com/kotlin-multiplatform/)

**Effort**: 18-24 months.

---

### Approach 8: libGDX + RoboVM

**How it works**: Port rendering to [libGDX](https://libgdx.com/) game framework, compile to iOS via RoboVM fork.

**Architecture**:
```
┌──────────────────────────────────────────────┐
│              iOS Native Binary                │
│  ┌──────────────────────────────────────────┐│
│  │         libGDX (OpenGL ES)                ││
│  │  - Scene2D for UI                         ││
│  │  - SpriteBatch for rendering              ││
│  └──────────────────────────────────────────┘│
│  ┌──────────────────────────────────────────┐│
│  │      Vassal Core (Java)                   ││
│  │  (Game logic, largely unchanged)          ││
│  └──────────────────────────────────────────┘│
│  ┌──────────────────────────────────────────┐│
│  │      RoboVM (MobiDevelop fork)            ││
│  │  (Java → Native iOS)                      ││
│  └──────────────────────────────────────────┘│
└──────────────────────────────────────────────┘
```

**Pros**:
- Stays in Java
- libGDX designed for games (good fit for board games)
- Cross-platform (iOS, Android, Desktop, Web)
- [RoboVM fork actively maintained](https://github.com/libgdx/libgdx)
- Good performance

**Cons**:
- UI rewrite required (Swing → Scene2D)
- RoboVM is community-maintained (some risk)
- OpenGL ES learning curve
- libGDX UI (Scene2D) not as polished as native

**Key References**:
- [libGDX](https://libgdx.com/)
- [Deploying to iOS](https://libgdx.com/wiki/deployment/deploying-your-application)
- [MobiDevelop RoboVM](https://github.com/libgdx/libgdx/blob/master/backends/gdx-backend-robovm/)

**Effort**: 15-20 months.

---

## Recommendation Matrix by Priority

### If Priority is "Fastest to iPad"
**Approach 1 (Webswing)** → 1-2 months

### If Priority is "Best iOS Experience"
**Approach 6 (Swift Native)** or **Approach 7 (Kotlin Multiplatform)** → 18-30 months

### If Priority is "Maximum Module Compatibility"
**Approach 2 (Hybrid Server + iOS Client)** → 18-24 months

### If Priority is "Leverage Existing Web Plan"
**Approach 3 (TypeScript + Capacitor)** → 20-24 months (builds on TYPESCRIPT_REWRITE_PLAN)

### If Priority is "Stay in Java Ecosystem"
**Approach 4 (Gluon Mobile)** → 12-18 months

### If Priority is "Cross-Platform (iOS + Android + Web)"
**Approach 7 (Kotlin Multiplatform)** or **Approach 3 (TypeScript)** → 18-24 months

---

## Questions for Decision Making

1. **What's the acceptable timeline?** Quick (1-2 months) vs proper native (18+ months)?

2. **Is offline play required?** Server-based solutions need network connectivity.

3. **How important is App Store distribution?** Webswing works in browser, no app needed.

4. **What about Android?** Several approaches (KMP, TypeScript, libGDX, Codename One) cover both.

5. **Module compatibility tolerance?** Some approaches achieve 99% (server-based), others 85-90%.

6. **Team expertise?** Java (Gluon, libGDX), TypeScript (Capacitor), Swift (native), Kotlin (KMP)?

---

## Sources

- [Webswing](https://www.webswing.org/en) - Java Swing to browser
- [Gluon Mobile](https://gluonhq.com/products/mobile/) - JavaFX to iOS
- [Codename One](https://www.codenameone.com/) - Java to native mobile
- [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/) - Shared Kotlin code
- [libGDX](https://libgdx.com/) - Java game framework
- [Capacitor](https://capacitorjs.com/) - Web to native wrapper
- [Multi-OS Engine](https://multi-os-engine.org/) - Java to iOS (Intel)
- [Kodeco Board Game Tutorial](https://www.kodeco.com/2774-how-to-develop-an-ipad-board-game-app-part-1-2) - Native iOS game dev
