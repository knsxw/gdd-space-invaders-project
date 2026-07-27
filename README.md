# Nebula Vanguard

Nebula Vanguard is a two-stage, side-scrolling arcade shooter built with Java
Swing. The player pilots a starfighter through increasingly difficult alien
formations, collects persistent upgrades, and confronts the Void Sovereign
flagship in a multi-phase final battle.

The project extends the supplied GDD Space Invaders starter while retaining its
scene-and-sprite structure. It uses only the Java standard library and ships
with its image and WAV audio assets.

## Gameplay

Each stage begins with a short briefing and a 30-second flight sequence.

- **Stage 1 — The Outer Reach:** fight through pairs of weaving Alien 1 ships
  and tougher bouncing Alien 2 ships.
- **Stage 2 — The Void Citadel:** survive denser, faster formations before the
  final boss arrives.
- **Final boss — The Void Sovereign:** defeat a 90-HP flagship whose projectile
  volleys become wider and more frequent across three health-based phases.
  The boss also telegraphs a horizontal targeting beam before firing it.

The player begins with 5 HP, level-0 speed, and a single forward shot. Damage
briefly makes the player invulnerable. Health, upgrades, and score carry from
Stage 1 into Stage 2.

### Power-ups

Power-ups drift in from the right side of the screen:

| Marker | Upgrade    | Effect                                                |
| ------ | ---------- | ----------------------------------------------------- |
| `S`    | Speed      | Raises movement speed, up to level 2                  |
| `M`    | Multi-shot | Expands the firing spread, up to 4 simultaneous shots |
| `H`    | Health     | Restores 1 HP, up to the 5-HP maximum                 |

During the boss fight, the game periodically chooses drops based on the
player's current health and missing upgrades.

### Scoring

| Target         | Points |
| -------------- | -----: |
| Alien 1        |    100 |
| Alien 2        |    175 |
| Void Sovereign |  5,000 |

The mission-end screen assigns a rank from C to SS based on the final score.

## Controls

| Key        | Action                                             |
| ---------- | -------------------------------------------------- |
| Arrow keys | Move in four directions                            |
| Space      | Start the game / fire                              |
| Enter      | Return to the title screen after victory or defeat |

The HUD shows the current stage, score, HP, speed level, shot level, and the
remaining flight time (or `BOSS` during the final encounter).

## Requirements

- JDK 11 or newer
- A desktop environment capable of displaying Java Swing windows
- Audio output supported by Java Sound (the game remains playable if audio
  initialization fails)

No external libraries, package manager, or build tool are required.

## Build and run

Run these commands from the project root. Keeping the project root as the
working directory is important because image and audio assets are loaded from
relative `src/...` paths.

```sh
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out gdd.Main
```

To remove compiled output, delete the generated `out` directory.

## Project structure

```text
.
├── MANIFEST.MF
├── README.md
└── src
    ├── audio/              WAV music and sound effects
    ├── images/             Title art and starter image assets
    └── gdd/
        ├── Main.java       Application entry point
        ├── Game.java       Window setup and scene transitions
        ├── Global.java     Board dimensions and shared constants
        ├── AudioPlayer.java
        ├── scene/          Title screen and both gameplay stages
        ├── sprite/         Player, enemies, boss, and projectiles
        └── powerup/        Speed, multi-shot, and health upgrades
```

## How it works

- `Main` creates the Swing application on the Event Dispatch Thread.
- `Game` owns the fixed 716×700 window and switches between `TitleScene`,
  `Scene1`, and `Scene2`.
- `Scene1` contains the shared fixed-step game loop, spawning, collision
  detection, combat, HUD, procedural drawing, audio triggers, and end states.
- `Scene2` reuses that engine with the Stage 2 difficulty and boss behavior.
- Sprite classes hold entity state and movement rules; power-up classes apply
  upgrades to the player.
- Gameplay updates target 60 frames per second using a fixed timestep, while
  Swing repainting is driven by a coalescing timer.
- Most gameplay visuals are drawn with Java2D. The title screen uses the
  bundled title image, and WAV clips provide music and sound effects.

## Team

- Khine Khant - 6611718
- Hein Oke Soe - 6611717

## Starter reference

The supplied starter identifies
[Java Space Invaders](https://github.com/janbodnar/Java-Space-Invaders) as its
original reference. Nebula Vanguard retains and expands the starter's
Java/Swing scene and sprite approach.
