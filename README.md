# Nebula Vanguard

An original side-scrolling arcade shooter extended directly from the supplied
GDD Space Invaders starter.

## Features

- Two horizontally scrolling five-minute stages
- Two animated enemy classes plus a final-stage boss
- Pure-drawn animated player, enemies, projectiles, power-ups and effects
- Two speed upgrades and four multi-shot levels
- Score, health, stage timer, speed, and shot status dashboard

## Controls

- Arrow keys: move in four directions
- Space: fire
- Enter: return to title after a mission ends

## Team

- Khine Khant
- Hein Oke Soe

## Run

Compile from the project root:

```sh
javac -d out $(find src -name '*.java')
java -cp out gdd.Main
```

## Starter reference

The supplied starter identifies
[Java Space Invaders](https://github.com/janbodnar/Java-Space-Invaders)
as its original reference. This project retains and extends the starter's
Java/Swing scene and sprite approach.
