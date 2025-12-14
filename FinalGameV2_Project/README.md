# Arena Dodge & Attack (Java Swing)

A real-time 2D arena game built with Java Swing that demonstrates core OOP principles: encapsulation, inheritance, polymorphism, and abstraction. All graphics are simple shapes (rectangles and circles) rendered via `paintComponent(Graphics g)`.

## Overview
- Pick a character (Warrior, Rogue, Mage)
- Enter a small arena with a boss in the center
- Dodge continuously fired projectiles
- Attack only when the boss opens its weak point
- Defeat the boss to advance through 4 levels of increasing difficulty

## Technology
- Java Swing GUI
- `JFrame` (`GameWindow`) + `JPanel` (`GamePanel`)
- Input via `KeyListener` (arrows, space, R)
- Game loop via Swing `Timer`
- Rendering with `paintComponent(Graphics g)`

## OOP Structure
- `Character` (abstract)
  - Fields: position, size, speed, health, attackPower
  - Methods: `move(...)`, `attack(Boss)`, `draw(Graphics)`
  - Subclasses: `Warrior`, `Rogue`, `Mage`
- `Boss` (abstract)
  - Fields: position, health, projectileSpeed, weakPointActive
  - Methods: `attackPattern(List<Projectile>, Character)`, `activateWeakPoint()`, `draw(Graphics)`
  - Subclasses: `Level1Boss`, `Level2Boss`, `Level3Boss`, `Level4Boss`
- `Projectile` (abstract)
  - Fields: position (x,y), velocity (vx,vy), damage
  - Methods: `move()`, `draw(Graphics)`
  - Subclasses: `StraightProjectile`, `ZigZagProjectile`, `SpiralProjectile`

All object fields are private with getters/setters. Abstract classes define common contracts that subclasses override (polymorphism).

## Gameplay
- Movement: Arrow keys
- Attack: `SPACE` (only works when Weak Point is OPEN)
- Restart after win/lose: `R`
- Arena bounds enforced; player cannot leave the arena.

### Levels
- Each level increases difficulty:
  - Boss HP
  - Fire rate
  - Projectile speed/pattern complexity
  - Weak point duration decreases

Patterns:
- Level 1: slow straight aimed shots
- Level 2: multi-shot zigzag spread
- Level 3: spiral projectiles
- Level 4: rapid mixed patterns (straight, zigzag, spirals)

## Files
- `src/GameWindow.java` — Main `JFrame` window that launches the game
- `src/GamePanel.java` — Game loop, input, updates, rendering, collisions, level progression
- `src/Character.java`, `src/Warrior.java`, `src/Rogue.java`, `src/Mage.java`
- `src/Boss.java`, `src/Level1Boss.java`, `src/Level2Boss.java`, `src/Level3Boss.java`, `src/Level4Boss.java`
- `src/Projectile.java`, `src/StraightProjectile.java`, `src/ZigZagProjectile.java`, `src/SpiralProjectile.java`

## Build & Run (Windows PowerShell)
```powershell
Push-Location "c:\Users\Rai\OneDrive\Documents\VsCodes\OOP\FinalGameV2_Project"
javac -d . src\*.java
java src.GameWindow
Pop-Location
```

If `javac`/`java` are not recognized, install a JDK and ensure your PATH includes the JDK `bin` directory.

## Notes
- Graphics are shape-based only; no image assets.
- Collision is circle (projectile) vs rectangle (player) AABB-style for simplicity.
- Values for speeds and damage are tuned for playability and can be adjusted in constructors.
