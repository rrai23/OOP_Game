package src;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * GamePanel is the main game surface. It manages the game loop,
 * keyboard input, updates, rendering, collision, and level progression.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {
    // Arena dimensions
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    // Game loop timer (Swing Timer)
    private final Timer timer;
    
    // Audio manager for sound effects
    private final AudioManager audioManager;

    // Game state
    private Character player;
    private Boss boss;
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final List<DamageNumber> damageNumbers = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    private int level = 1;
    private boolean running = true;
    private boolean selectingCharacter = true;
    private boolean selectingMode = false;
    private boolean selectingDifficulty = false;
    private boolean paused = false;
    
    // Screen shake effect
    private int shakeOffsetX = 0;
    private int shakeOffsetY = 0;
    private long shakeUntilMs = 0;
    
    // Game modes
    private boolean endlessMode = false;
    private String difficulty = "MEDIUM"; // EASY, MEDIUM, NIGHTMARE
    
    // Scoring system
    private int score = 0;
    private double scoreMultiplier = 1.0;
    // Hit indicator timing
    private long playerHitFlashUntilMs = 0;
    // Shield immunity timing
    private long shieldUntilMs = 0;
    // Explosion effect timing
    private long explosionUntilMs = 0;
    private int explosionX = 0;
    private int explosionY = 0;
    // Deflection effect timing
    private long deflectionUntilMs = 0;
    private int deflectionX = 0;
    private int deflectionY = 0;
    // Item spawn timing
    private long lastItemSpawnMs = System.currentTimeMillis();
    private long nextItemSpawnMs = System.currentTimeMillis() + getRandomSpawnDelay();

    // Input state
    private boolean up, down, left, right, attacking, dashing;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // Start with mode selection, don't spawn boss yet
        selectingMode = true;
        selectingCharacter = false;
        
        // Initialize audio manager
        audioManager = new AudioManager();

        // 60 FPS equivalent ~16ms
        timer = new Timer(16, this);
        timer.start();
    }

    private void spawnBossForLevel(int lvl) {
        int cx = WIDTH / 2 - 40;
        int cy = HEIGHT / 2 - 40;
        switch (lvl) {
            case 1:
                boss = new Level1Boss(cx, cy);
                break;
            case 2:
                boss = new Level2Boss(cx, cy);
                break;
            case 3:
                boss = new Level3Boss(cx, cy);
                break;
            default:
                boss = new Level4Boss(cx, cy);
                break;
        }
    }

    private void spawnDefaultPlayer() {
        // Default spawn point near bottom center
        int px = WIDTH / 2 - 20;
        int py = HEIGHT - 100;
        // If none selected yet, choose Warrior
        if (player == null) {
            player = new Warrior(px, py);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!running) return;
        if (paused) { repaint(); return; }

        if (selectingCharacter) {
            // No update until character selected
            repaint();
            return;
        }

        updateGame();
        repaint();
    }

    private void updateGame() {
        // Move player based on input
        if (player != null) {
            // Handle dash input
            if (dashing && player.canDash()) {
                int dx = 0, dy = 0;
                if (up) dy -= 1;
                if (down) dy += 1;
                if (left) dx -= 1;
                if (right) dx += 1;
                
                // Normalize direction if moving diagonally
                if (dx != 0 || dy != 0) {
                    double len = Math.sqrt(dx * dx + dy * dy);
                    player.startDash(dx / len, dy / len);
                    audioManager.playSound("dash"); // Dash sound effect
                }
                dashing = false; // Reset dash input
            }
            
            // Update dash movement with collision check
            if (player.isDashing()) {
                int prevX = player.getX();
                int prevY = player.getY();
                player.updateDash(WIDTH, HEIGHT);
                // Prevent dashing into boss
                if (boss != null && collidesPlayerBoss(player, boss)) {
                    player.setX(prevX);
                    player.setY(prevY);
                }
            }
            
            // Regular movement only if not dashing
            if (!player.isDashing()) {
                int prevX = player.getX();
                int prevY = player.getY();
                int dx = 0, dy = 0;
                if (up) dy -= 1;
                if (down) dy += 1;
                if (left) dx -= 1;
                if (right) dx += 1;
                player.move(dx, dy, WIDTH, HEIGHT);
                // Prevent overlapping with boss
                if (boss != null && collidesPlayerBoss(player, boss)) {
                    player.setX(prevX);
                    player.setY(prevY);
                }
            }
        }

        // Boss attacks periodically and adds projectiles
        if (boss != null) {
            boss.updateWeakPoint();
            boss.attackPattern(projectiles, player);
        }

        // Mage ranged attack: allow firing anytime while attacking
        if (attacking && player instanceof Mage && boss != null) {
            Mage m = (Mage) player;
            if (m.canAttack()) {
                audioManager.playSound("mage");
                double sx = m.getX() + m.getWidth() / 2.0;
                double sy = m.getY() + m.getHeight() / 2.0;
                double bx = boss.getX() + boss.getSize() / 2.0;
                double by = boss.getY() + boss.getSize() / 2.0;
                double dx = bx - sx;
                double dy = by - sy;
                double len = Math.max(1, Math.hypot(dx, dy));
                double speed = 6.0;
                double vx = dx / len * speed;
                double vy = dy / len * speed;
                projectiles.add(new PlayerProjectile(sx, sy, vx, vy, m.getAttackPower()));
                m.markAttack();
            }
        }

        // Update projectiles
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.move();
            // Remove if out of bounds
            if (p.getX() < -50 || p.getX() > WIDTH + 50 || p.getY() < -50 || p.getY() > HEIGHT + 50) {
                it.remove();
                continue;
            }
            // Collision with player (ignore player projectiles and shield immunity)
            if (!(p instanceof PlayerProjectile) && player != null && p.collidesWith(player)) {
                long currentTime = System.currentTimeMillis();
                // Invincible during dash or shield
                if (currentTime >= shieldUntilMs && !player.isDashing()) {
                    // Only take damage if shield is not active and not dashing
                    player.setHealth(player.getHealth() - p.getDamage());
                    audioManager.playSound("damage");
                    // Trigger hit flash indicator for a short duration
                    playerHitFlashUntilMs = currentTime + 200; // 200ms flash
                }
                it.remove();
            }

            // Player projectile hits boss (mage only damages when weak point is open)
            if (p instanceof PlayerProjectile && boss != null) {
                PlayerProjectile pp = (PlayerProjectile) p;
                if (pp.collidesWithBoss(boss)) {
                    if (boss.isWeakPointActive()) {
                        int damage = pp.getDamage();
                        boss.setHealth(boss.getHealth() - damage);
                        audioManager.playSound("boss_hit");
                        // Add damage number
                        damageNumbers.add(new DamageNumber(damage, 
                            boss.getX() + boss.getSize() / 2.0, 
                            boss.getY() + boss.getSize() / 2.0));
                        // Screen shake
                        shakeUntilMs = System.currentTimeMillis() + 100;
                        // Award score for hitting boss
                        score += (int)(10 * scoreMultiplier);
                    }
                    it.remove();
                }
            }
        }

        // Player attack via weapon hit detection
        updatePlayerAttack();

        // Check win/loss
        if (player != null && player.getHealth() <= 0) {
            audioManager.playSound("lose");
            running = false; // Game over
        }
        if (boss != null && boss.getHealth() <= 0) {
            // Award bonus score for defeating boss
            score += (int)(level * 100 * scoreMultiplier);
            
            level++;
            if (!endlessMode && level > 4) {
                audioManager.playSound("won");
                running = false; // Game win (levels mode only)
            } else {
                audioManager.playSound("level_next");
                projectiles.clear();
                items.clear(); // Clear items on level transition
                
                // In endless mode, cycle through boss types
                if (endlessMode) {
                    int bossType = ((level - 1) % 4) + 1;
                    spawnBossForLevel(bossType);
                } else {
                    spawnBossForLevel(level);
                }
                
                // Slight heal/reposition player
                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 20));
                player.setX(WIDTH / 2 - player.getWidth() / 2);
                player.setY(HEIGHT - 100);
                // Reset item spawn timer
                lastItemSpawnMs = System.currentTimeMillis();
                nextItemSpawnMs = System.currentTimeMillis() + getRandomSpawnDelay();
            }
        }

        // Item spawning at random intervals (no max limit)
        long now = System.currentTimeMillis();
        if (now >= nextItemSpawnMs) {
            spawnRandomItem();
            lastItemSpawnMs = now;
            nextItemSpawnMs = now + getRandomSpawnDelay();
        }

        // Item collision and pickup
        Iterator<Item> itemIt = items.iterator();
        while (itemIt.hasNext()) {
            Item item = itemIt.next();
            if (player != null && item.collidesWith(player)) {
                if (item instanceof BombItem) {
                    audioManager.playSound("boom");
                    // Trigger hit flash when bomb damages player
                    playerHitFlashUntilMs = System.currentTimeMillis() + 300;
                    // Trigger explosion animation and screen shake
                    explosionUntilMs = System.currentTimeMillis() + 500;
                    shakeUntilMs = System.currentTimeMillis() + 300;
                    explosionX = item.getX();
                    explosionY = item.getY();
                } else {
                    audioManager.playSound("pick_uped");
                    if (item instanceof ShieldItem) {
                        // Activate shield for 5 seconds
                        shieldUntilMs = System.currentTimeMillis() + 5000;
                    }
                }
                item.applyEffect(player, projectiles);
                itemIt.remove();
            }
        }
        
        // Update damage numbers
        Iterator<DamageNumber> dnIt = damageNumbers.iterator();
        while (dnIt.hasNext()) {
            DamageNumber dn = dnIt.next();
            dn.update();
            if (dn.isExpired()) {
                dnIt.remove();
            }
        }
        
        // Update particles
        Iterator<Particle> partIt = particles.iterator();
        while (partIt.hasNext()) {
            Particle p = partIt.next();
            p.update();
            if (p.isExpired()) {
                partIt.remove();
            }
        }
        
        // Spawn particle trails for projectiles
        if (random.nextInt(3) == 0) { // Not every frame to avoid too many particles
            for (Projectile p : projectiles) {
                Color trailColor = (p instanceof PlayerProjectile) ? 
                    new Color(255, 200, 100) : new Color(200, 50, 50);
                particles.add(Particle.createTrailParticle(
                    p.getX() + p.getSize() / 2.0, 
                    p.getY() + p.getSize() / 2.0, 
                    trailColor
                ));
            }
        }
        
        // Update screen shake
        if (System.currentTimeMillis() < shakeUntilMs) {
            shakeOffsetX = random.nextInt(11) - 5; // -5 to +5
            shakeOffsetY = random.nextInt(11) - 5;
        } else {
            shakeOffsetX = 0;
            shakeOffsetY = 0;
        }
    }

    // Rectangle (player) vs circle (boss) collision check
    private boolean collidesPlayerBoss(Character c, Boss b) {
        int cx = c.getX();
        int cy = c.getY();
        int cw = c.getWidth();
        int ch = c.getHeight();
        int bx = b.getX() + b.getSize() / 2;
        int by = b.getY() + b.getSize() / 2;
        int radius = b.getSize() / 2;
        // Closest point on rect to circle center
        int closestX = Math.max(cx, Math.min(bx, cx + cw));
        int closestY = Math.max(cy, Math.min(by, cy + ch));
        int dx = bx - closestX;
        int dy = by - closestY;
        return (dx * dx + dy * dy) < (radius * radius);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Apply screen shake offset
        g.translate(shakeOffsetX, shakeOffsetY);

        // Draw arena boundary
        g.setColor(Color.DARK_GRAY);
        g.fillRect(40, 40, WIDTH - 80, HEIGHT - 80);

        // Character selection overlay
        if (selectingCharacter) {
            drawCharacterSelection(g);
            return;
        }
        
        // Mode selection overlay
        if (selectingMode) {
            drawModeSelection(g);
            return;
        }
        
        // Difficulty selection overlay (for endless mode)
        if (selectingDifficulty) {
            drawDifficultySelection(g);
            return;
        }

        // Draw player
        if (player != null) {
            // If recently hit, draw a flashing overlay
            boolean flashing = System.currentTimeMillis() < playerHitFlashUntilMs;
            boolean shielded = System.currentTimeMillis() < shieldUntilMs;
            boolean isDashing = player.isDashing();
            
            // Draw dash trail effect
            if (isDashing) {
                g.setColor(new Color(255, 255, 255, 100));
                int trailSize = 8;
                g.fillRect(player.getX() - trailSize/2, player.getY() - trailSize/2, 
                          player.getWidth() + trailSize, player.getHeight() + trailSize);
            }
            
            player.draw(g);
            
            if (flashing) {
                g.setColor(new Color(255, 0, 0, 120));
                g.fillRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());
            }
            if (shielded) {
                // Draw pulsing shield aura around player
                g.setColor(new Color(100, 200, 255, 80));
                int pulseSize = (int)(Math.sin(System.currentTimeMillis() * 0.01) * 3 + 5);
                g.fillRect(player.getX() - pulseSize, player.getY() - pulseSize, 
                          player.getWidth() + pulseSize * 2, player.getHeight() + pulseSize * 2);
            }
            // Draw weapon aimed at boss; highlight when attacking
            player.drawWeapon(g, boss, attacking);
        }

        // Draw boss
        if (boss != null) {
            boss.draw(g);
            
            // Draw glowing effect around boss when weak point is active
            if (boss.isWeakPointActive()) {
                // Pulsing glow
                double pulse = Math.sin(System.currentTimeMillis() * 0.01) * 0.5 + 0.5;
                int glowAlpha = (int)(150 * pulse);
                g.setColor(new Color(255, 255, 0, glowAlpha));
                int glowSize = (int)(boss.getSize() + 20 + pulse * 10);
                int bossCenterX = boss.getX() + boss.getSize() / 2;
                int bossCenterY = boss.getY() + boss.getSize() / 2;
                g.fillOval(bossCenterX - glowSize / 2, bossCenterY - glowSize / 2, glowSize, glowSize);
            }
        }

        // Draw projectiles
        for (Projectile p : projectiles) {
            p.draw(g);
        }

        // Draw items
        for (Item item : items) {
            item.draw(g);
        }

        // Draw explosion effect if active
        long currentTime = System.currentTimeMillis();
        if (currentTime < explosionUntilMs) {
            long elapsed = currentTime - (explosionUntilMs - 500);
            double progress = elapsed / 500.0;
            int explosionSize = (int)(progress * 80);
            int alpha = (int)((1.0 - progress) * 200);
            
            // Outer explosion (orange)
            g.setColor(new Color(255, 100, 0, Math.max(0, alpha)));
            g.fillOval(explosionX - explosionSize/2, explosionY - explosionSize/2, explosionSize, explosionSize);
            
            // Inner explosion (yellow)
            g.setColor(new Color(255, 255, 0, Math.max(0, alpha + 55)));
            int innerSize = explosionSize * 2 / 3;
            g.fillOval(explosionX - innerSize/2, explosionY - innerSize/2, innerSize, innerSize);
            
            // Core (white)
            g.setColor(new Color(255, 255, 255, Math.max(0, alpha + 55)));
            int coreSize = explosionSize / 3;
            g.fillOval(explosionX - coreSize/2, explosionY - coreSize/2, coreSize, coreSize);
        }
        
        // Draw deflection spark effect if active
        if (currentTime < deflectionUntilMs) {
            long elapsed = currentTime - (deflectionUntilMs - 200);
            double progress = elapsed / 200.0;
            int sparkSize = (int)((1.0 - progress) * 30);
            int alpha = (int)((1.0 - progress) * 255);
            
            // Draw spark burst (cyan/white for deflection)
            g.setColor(new Color(100, 255, 255, Math.max(0, alpha)));
            g.fillOval(deflectionX - sparkSize/2, deflectionY - sparkSize/2, sparkSize, sparkSize);
            
            // Draw cross pattern for impact effect
            g.setColor(new Color(255, 255, 255, Math.max(0, alpha)));
            int lineLen = sparkSize / 2;
            g.drawLine(deflectionX - lineLen, deflectionY, deflectionX + lineLen, deflectionY);
            g.drawLine(deflectionX, deflectionY - lineLen, deflectionX, deflectionY + lineLen);
        }
        
        // Draw particles
        for (Particle particle : particles) {
            particle.draw(g);
        }
        
        // Draw damage numbers
        for (DamageNumber dn : damageNumbers) {
            dn.draw(g);
        }

        // Draw shield timer above player if active
        if (player != null && currentTime < shieldUntilMs) {
            long timeLeft = (shieldUntilMs - currentTime) / 1000 + 1; // Round up
            g.setColor(new Color(100, 200, 255));
            g.setFont(new Font("Arial", Font.BOLD, 16));
            String timerText = "Shield: " + timeLeft + "s";
            int textWidth = g.getFontMetrics().stringWidth(timerText);
            int textX = player.getX() + player.getWidth() / 2 - textWidth / 2;
            int textY = player.getY() - 8;
            
            // Background for readability
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(textX - 3, textY - 14, textWidth + 6, 18);
            
            // Timer text
            g.setColor(new Color(150, 220, 255));
            g.drawString(timerText, textX, textY);
        }

        // HUD
        drawHUD(g);

        // End messages
        if (!running) {
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(WIDTH / 2 - 250, HEIGHT / 2 - 80, 500, 160);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            String msg = (endlessMode || level <= 4) ? "Game Over!" : "You Win!";
            int msgWidth = g.getFontMetrics().stringWidth(msg);
            g.drawString(msg, WIDTH / 2 - msgWidth / 2, HEIGHT / 2 - 30);
            
            // Show final score
            g.setColor(new Color(255, 220, 100));
            g.setFont(new Font("Arial", Font.BOLD, 24));
            String scoreMsg = "Final Score: " + score;
            int scoreWidth = g.getFontMetrics().stringWidth(scoreMsg);
            g.drawString(scoreMsg, WIDTH / 2 - scoreWidth / 2, HEIGHT / 2 + 10);
            
            // Restart instruction
            g.setColor(new Color(200, 200, 200));
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            String restartMsg = "Press R to Restart";
            int restartWidth = g.getFontMetrics().stringWidth(restartMsg);
            g.drawString(restartMsg, WIDTH / 2 - restartWidth / 2, HEIGHT / 2 + 50);
        }

        // Pause overlay
        if (paused && running && !selectingCharacter) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(40, 40, WIDTH - 80, HEIGHT - 80);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Paused", WIDTH / 2 - 60, HEIGHT / 2 - 20);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Press P to Resume | Press R to Restart", WIDTH / 2 - 190, HEIGHT / 2 + 20);
        }
    }

    private void drawCharacterSelection(Graphics g) {
        // Dark overlay background
        g.setColor(new Color(20, 20, 40, 230));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Title with shadow
        g.setColor(new Color(0, 0, 0, 100));
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("DODGE ADVENTURE", WIDTH / 2 - 197, 72);
        g.setColor(new Color(255, 200, 0));
        g.drawString("DODGE ADVENTURE", WIDTH / 2 - 200, 70);
        
        // Subtitle
        g.setColor(new Color(200, 200, 200));
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Select Your Character", WIDTH / 2 - 90, 100);
        
        // Character selection boxes
        int boxY = 130;
        int boxHeight = 60;
        int spacing = 10;
        
        // Warrior box
        g.setColor(new Color(0, 150, 0, 180));
        g.fillRoundRect(WIDTH / 2 - 300, boxY, 180, boxHeight, 15, 15);
        g.setColor(Color.GREEN);
        g.drawRoundRect(WIDTH / 2 - 300, boxY, 180, boxHeight, 15, 15);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("1. WARRIOR", WIDTH / 2 - 280, boxY + 28);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(200, 255, 200));
        g.drawString("High HP & Attack", WIDTH / 2 - 280, boxY + 48);
        
        // Rogue box
        g.setColor(new Color(0, 150, 150, 180));
        g.fillRoundRect(WIDTH / 2 - 90, boxY, 180, boxHeight, 15, 15);
        g.setColor(Color.CYAN);
        g.drawRoundRect(WIDTH / 2 - 90, boxY, 180, boxHeight, 15, 15);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("2. ROGUE", WIDTH / 2 - 70, boxY + 28);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(200, 255, 255));
        g.drawString("High Speed", WIDTH / 2 - 70, boxY + 48);
        
        // Mage box
        g.setColor(new Color(150, 0, 150, 180));
        g.fillRoundRect(WIDTH / 2 + 120, boxY, 180, boxHeight, 15, 15);
        g.setColor(Color.MAGENTA);
        g.drawRoundRect(WIDTH / 2 + 120, boxY, 180, boxHeight, 15, 15);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("3. MAGE", WIDTH / 2 + 140, boxY + 28);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(255, 200, 255));
        g.drawString("Ranged Magic", WIDTH / 2 + 140, boxY + 48);
        
        // Divider line
        g.setColor(new Color(100, 100, 150));
        g.fillRect(WIDTH / 2 - 300, 250, 600, 2);
        
        // Items section header
        g.setColor(new Color(255, 220, 100));
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("COLLECTIBLE ITEMS", WIDTH / 2 - 125, 285);
        
        int itemX = WIDTH / 2 - 280;
        int itemY = 310;
        int itemSpacing = 35;
        
        // Draw actual item visuals
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // Heart item
        drawMenuHeart(g, itemX, itemY);
        g.setColor(Color.WHITE);
        g.drawString("HEART", itemX + 35, itemY + 16);
        g.setColor(new Color(180, 180, 180));
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Restores 10 HP", itemX + 120, itemY + 16);
        
        // Orb item
        itemY += itemSpacing;
        drawMenuOrb(g, itemX, itemY);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("ORB", itemX + 35, itemY + 16);
        g.setColor(new Color(180, 180, 180));
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Clears all enemy projectiles", itemX + 120, itemY + 16);
        
        // Shield item
        itemY += itemSpacing;
        drawMenuShield(g, itemX, itemY);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("SHIELD", itemX + 35, itemY + 16);
        g.setColor(new Color(180, 180, 180));
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("5 seconds of immunity", itemX + 120, itemY + 16);
        
        // Bomb item (warning)
        itemY += itemSpacing;
        drawMenuBomb(g, itemX, itemY);
        g.setColor(new Color(255, 100, 100));
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("BOMB", itemX + 35, itemY + 16);
        g.setColor(new Color(255, 150, 150));
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("Deals 15 damage - AVOID!", itemX + 120, itemY + 16);
        
        // Bottom info
        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("Arial", Font.ITALIC, 14));
        g.drawString("Items spawn randomly throughout the game", WIDTH / 2 - 160, 490);
        g.setColor(new Color(200, 200, 255));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Press 1, 2, or 3 to begin!", WIDTH / 2 - 100, 520);
    }
    
    private void drawMenuHeart(Graphics g, int x, int y) {
        g.setColor(Color.RED);
        g.fillOval(x, y, 12, 12);
        g.fillOval(x + 10, y, 12, 12);
        int[] xs = {x, x + 22, x + 11};
        int[] ys = {y + 8, y + 8, y + 22};
        g.fillPolygon(xs, ys, 3);
    }
    
    private void drawMenuOrb(Graphics g, int x, int y) {
        g.setColor(new Color(100, 200, 255, 100));
        g.fillOval(x - 2, y - 2, 26, 26);
        g.setColor(new Color(150, 220, 255));
        g.fillOval(x, y, 22, 22);
        g.setColor(Color.WHITE);
        g.fillOval(x + 7, y + 5, 8, 8);
    }
    
    private void drawMenuShield(Graphics g, int x, int y) {
        g.setColor(new Color(255, 215, 0));
        int[] xs = {x + 11, x, x, x + 11, x + 22, x + 22};
        int[] ys = {y, y + 7, y + 15, y + 22, y + 15, y + 7};
        g.fillPolygon(xs, ys, 6);
        g.setColor(new Color(100, 150, 255));
        int[] xs2 = {x + 11, x + 3, x + 3, x + 11, x + 19, x + 19};
        int[] ys2 = {y + 3, y + 8, y + 14, y + 19, y + 14, y + 8};
        g.fillPolygon(xs2, ys2, 6);
    }
    
    private void drawMenuBomb(Graphics g, int x, int y) {
        g.setColor(Color.BLACK);
        g.fillOval(x, y + 5, 22, 17);
        g.setColor(new Color(255, 150, 0));
        g.fillRect(x + 10, y, 3, 6);
        g.setColor(Color.RED);
        g.fillOval(x + 9, y - 2, 5, 5);
    }
    
    private void drawModeSelection(Graphics g) {
        // Dark overlay background
        g.setColor(new Color(20, 20, 40, 230));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Title with shadow
        g.setColor(new Color(0, 0, 0, 100));
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("DODGE ADVENTURE", WIDTH / 2 - 197, 92);
        g.setColor(new Color(255, 200, 0));
        g.drawString("DODGE ADVENTURE", WIDTH / 2 - 200, 90);
        
        // Subtitle
        g.setColor(new Color(200, 200, 200));
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Choose Game Mode", WIDTH / 2 - 85, 130);
        
        // Mode selection boxes
        int boxY = 200;
        int boxHeight = 120;
        int boxWidth = 280;
        int spacing = 40;
        
        // Levels mode box
        g.setColor(new Color(0, 100, 200, 180));
        g.fillRoundRect(WIDTH / 2 - boxWidth - spacing/2, boxY, boxWidth, boxHeight, 20, 20);
        g.setColor(new Color(100, 180, 255));
        g.drawRoundRect(WIDTH / 2 - boxWidth - spacing/2, boxY, boxWidth, boxHeight, 20, 20);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("1. LEVELS", WIDTH / 2 - boxWidth - spacing/2 + 50, boxY + 40);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(new Color(200, 220, 255));
        g.drawString("Complete 4 levels", WIDTH / 2 - boxWidth - spacing/2 + 60, boxY + 70);
        g.drawString("Progressive difficulty", WIDTH / 2 - boxWidth - spacing/2 + 50, boxY + 95);
        
        // Endless mode box
        g.setColor(new Color(150, 0, 150, 180));
        g.fillRoundRect(WIDTH / 2 + spacing/2, boxY, boxWidth, boxHeight, 20, 20);
        g.setColor(new Color(255, 100, 255));
        g.drawRoundRect(WIDTH / 2 + spacing/2, boxY, boxWidth, boxHeight, 20, 20);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("2. ENDLESS", WIDTH / 2 + spacing/2 + 50, boxY + 40);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(new Color(255, 200, 255));
        g.drawString("Survive as long as you can", WIDTH / 2 + spacing/2 + 25, boxY + 70);
        g.drawString("Choose your difficulty", WIDTH / 2 + spacing/2 + 40, boxY + 95);
        
        // Instructions
        g.setColor(new Color(200, 200, 255));
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Press 1 or 2 to select mode", WIDTH / 2 - 130, 480);
    }
    
    private void drawDifficultySelection(Graphics g) {
        // Dark overlay background
        g.setColor(new Color(20, 20, 40, 230));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Title with shadow
        g.setColor(new Color(0, 0, 0, 100));
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("ENDLESS MODE", WIDTH / 2 - 167, 72);
        g.setColor(new Color(255, 100, 255));
        g.drawString("ENDLESS MODE", WIDTH / 2 - 170, 70);
        
        // Subtitle
        g.setColor(new Color(200, 200, 200));
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Select Difficulty", WIDTH / 2 - 75, 110);
        
        int boxY = 150;
        int boxHeight = 100;
        int boxWidth = 220;
        int spacing = 20;
        
        // Easy difficulty
        g.setColor(new Color(0, 150, 0, 180));
        g.fillRoundRect(WIDTH / 2 - boxWidth*3/2 - spacing, boxY, boxWidth, boxHeight, 15, 15);
        g.setColor(Color.GREEN);
        g.drawRoundRect(WIDTH / 2 - boxWidth*3/2 - spacing, boxY, boxWidth, boxHeight, 15, 15);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("1. EASY", WIDTH / 2 - boxWidth*3/2 - spacing + 45, boxY + 35);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(200, 255, 200));
        g.drawString("More hearts & shields", WIDTH / 2 - boxWidth*3/2 - spacing + 20, boxY + 60);
        g.drawString("Less bombs", WIDTH / 2 - boxWidth*3/2 - spacing + 50, boxY + 78);
        g.setColor(new Color(255, 220, 100));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Score: x1.0", WIDTH / 2 - boxWidth*3/2 - spacing + 55, boxY + 95);
        
        // Medium difficulty
        g.setColor(new Color(200, 150, 0, 180));
        g.fillRoundRect(WIDTH / 2 - boxWidth/2, boxY, boxWidth, boxHeight, 15, 15);
        g.setColor(Color.YELLOW);
        g.drawRoundRect(WIDTH / 2 - boxWidth/2, boxY, boxWidth, boxHeight, 15, 15);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("2. MEDIUM", WIDTH / 2 - boxWidth/2 + 30, boxY + 35);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(255, 255, 200));
        g.drawString("Balanced spawn rates", WIDTH / 2 - boxWidth/2 + 30, boxY + 60);
        g.drawString("Normal difficulty", WIDTH / 2 - boxWidth/2 + 40, boxY + 78);
        g.setColor(new Color(255, 220, 100));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Score: x1.5", WIDTH / 2 - boxWidth/2 + 55, boxY + 95);
        
        // Nightmare difficulty
        g.setColor(new Color(150, 0, 0, 180));
        g.fillRoundRect(WIDTH / 2 + boxWidth/2 + spacing, boxY, boxWidth, boxHeight, 15, 15);
        g.setColor(new Color(255, 50, 50));
        g.drawRoundRect(WIDTH / 2 + boxWidth/2 + spacing, boxY, boxWidth, boxHeight, 15, 15);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("3. NIGHTMARE", WIDTH / 2 + boxWidth/2 + spacing + 15, boxY + 35);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(255, 200, 200));
        g.drawString("More bombs!", WIDTH / 2 + boxWidth/2 + spacing + 50, boxY + 60);
        g.drawString("High challenge", WIDTH / 2 + boxWidth/2 + spacing + 45, boxY + 78);
        g.setColor(new Color(255, 220, 100));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Score: x2.5", WIDTH / 2 + boxWidth/2 + spacing + 55, boxY + 95);
        
        // Instructions
        g.setColor(new Color(200, 200, 255));
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Press 1, 2, or 3 to select difficulty", WIDTH / 2 - 155, 480);
    }

    // Player attack: weapon must reach boss; damage gated inside Character.attack
    private void updatePlayerAttack() {
        if (attacking && player != null && boss != null) {
            // Check if this is Warrior or Rogue for special abilities
            boolean isWarriorOrRogue = (player instanceof Warrior || player instanceof Rogue);
            boolean justAttacked = player.canAttack(); // Check if we're off cooldown (about to attack)
            
            // Warrior and Rogue can destroy projectiles with their weapon
            if (isWarriorOrRogue && justAttacked) {
                int[][] weaponBox = player.getWeaponHitbox(boss);
                int[] xs = weaponBox[0];
                int[] ys = weaponBox[1];
                int minX = xs[0], maxX = xs[0], minY = ys[0], maxY = ys[0];
                for (int i = 1; i < xs.length; i++) {
                    if (xs[i] < minX) minX = xs[i];
                    if (xs[i] > maxX) maxX = xs[i];
                    if (ys[i] < minY) minY = ys[i];
                    if (ys[i] > maxY) maxY = ys[i];
                }
                
                // Check for projectile collisions with weapon
                Iterator<Projectile> projIt = projectiles.iterator();
                boolean deflectedAny = false;
                while (projIt.hasNext()) {
                    Projectile p = projIt.next();
                    // Only destroy enemy projectiles, not player projectiles
                    if (!(p instanceof PlayerProjectile)) {
                        int px = (int)p.getX();
                        int py = (int)p.getY();
                        int pSize = p.getSize();
                        
                        // Simple AABB collision check
                        if (!(px + pSize < minX || px > maxX || py + pSize < minY || py > maxY)) {
                            // Trigger deflection animation
                            deflectionUntilMs = System.currentTimeMillis() + 200;
                            deflectionX = px;
                            deflectionY = py;
                            deflectedAny = true;
                            projIt.remove();
                            score += (int)(5 * scoreMultiplier); // Small bonus for deflecting
                        }
                    }
                }
                
                // Play deflection sound if any projectiles were destroyed
                if (deflectedAny) {
                    audioManager.playSound("boss_hit");
                }
            }
            
            if (weaponHitsBoss(player, boss)) {
                int healthBefore = boss.getHealth();
                player.attack(boss);
                int healthAfter = boss.getHealth();
                
                // Play slash sound when Warrior or Rogue swing (regardless of damage)
                if (isWarriorOrRogue && justAttacked) {
                    audioManager.playSound("slash");
                }
                
                // Award score if damage was dealt
                if (healthAfter < healthBefore) {
                    int damage = healthBefore - healthAfter;
                    audioManager.playSound("boss_hit");
                    // Add damage number
                    damageNumbers.add(new DamageNumber(damage, 
                        boss.getX() + boss.getSize() / 2.0, 
                        boss.getY() + boss.getSize() / 2.0));
                    // Screen shake
                    shakeUntilMs = System.currentTimeMillis() + 100;
                    score += (int)(10 * scoreMultiplier);
                }
            }
        }
    }

    private boolean weaponHitsBoss(Character c, Boss b) {
        int[][] poly = c.getWeaponHitbox(b);
        int[] xs = poly[0];
        int[] ys = poly[1];
        int minX = xs[0], maxX = xs[0], minY = ys[0], maxY = ys[0];
        for (int i = 1; i < xs.length; i++) {
            if (xs[i] < minX) minX = xs[i];
            if (xs[i] > maxX) maxX = xs[i];
            if (ys[i] < minY) minY = ys[i];
            if (ys[i] > maxY) maxY = ys[i];
        }
        int bx = b.getX();
        int by = b.getY();
        int bs = b.getSize();
        int bMinX = bx;
        int bMinY = by;
        int bMaxX = bx + bs;
        int bMaxY = by + bs;
        return !(maxX < bMinX || minX > bMaxX || maxY < bMinY || minY > bMaxY);
    }

    private void drawHUD(Graphics g) {
        // Top-left: Level + Player HP
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        int topY = 28;
        
        String modeText = endlessMode ? "Endless" : "Level: " + level;
        g.drawString(modeText, 50, topY);
        
        if (player != null) {
            g.drawString("HP: " + player.getHealth() + "/" + player.getMaxHealth(), 140, topY);
        }
        
        // Score display - moved to top right to avoid overlap
        g.setColor(new Color(255, 220, 100));
        g.setFont(new Font("Arial", Font.BOLD, 16));
        String scoreText = "Score: " + score;
        if (endlessMode) {
            scoreText += " (x" + String.format("%.1f", scoreMultiplier) + ")";
        }
        int scoreWidth = g.getFontMetrics().stringWidth(scoreText);
        g.drawString(scoreText, WIDTH - scoreWidth - 50, topY);

        // Boss health bar at top center
        if (boss != null) {
            int barWidth = 300;
            int barHeight = 25;
            int barX = WIDTH / 2 - barWidth / 2;
            int barY = 50;
            
            // Background
            g.setColor(new Color(50, 50, 50));
            g.fillRect(barX, barY, barWidth, barHeight);
            
            // Health fill
            double healthPercent = (double) boss.getHealth() / boss.getMaxHealth();
            int fillWidth = (int) (barWidth * healthPercent);
            
            // Color based on health
            Color healthColor;
            if (healthPercent > 0.6) {
                healthColor = new Color(0, 200, 0);
            } else if (healthPercent > 0.3) {
                healthColor = new Color(255, 200, 0);
            } else {
                healthColor = new Color(255, 50, 50);
            }
            g.setColor(healthColor);
            g.fillRect(barX, barY, fillWidth, barHeight);
            
            // Border
            g.setColor(Color.WHITE);
            g.drawRect(barX, barY, barWidth, barHeight);
            
            // Boss HP text
            g.setFont(new Font("Arial", Font.BOLD, 14));
            String bossText = "BOSS: " + boss.getHealth() + " / " + boss.getMaxHealth();
            int textWidth = g.getFontMetrics().stringWidth(bossText);
            g.drawString(bossText, WIDTH / 2 - textWidth / 2, barY + 18);
            
            // Weak point indicator
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            String wpText = boss.isWeakPointActive() ? "WEAK POINT OPEN!" : "Weak Point Closed";
            Color wpColor = boss.isWeakPointActive() ? new Color(255, 255, 0) : new Color(150, 150, 150);
            g.setColor(wpColor);
            int wpWidth = g.getFontMetrics().stringWidth(wpText);
            g.drawString(wpText, WIDTH / 2 - wpWidth / 2, barY + barHeight + 15);
        }
        
        // Attack cooldown indicator
        if (player != null && attacking) {
            long now = System.currentTimeMillis();
            long timeSinceAttack = now - player.getLastAttackTime();
            long cooldown = player.getAttackCooldown();
            
            if (timeSinceAttack < cooldown) {
                double cooldownPercent = (double) timeSinceAttack / cooldown;
                
                int cdSize = 40;
                int cdX = player.getX() + player.getWidth() / 2 - cdSize / 2;
                int cdY = player.getY() - cdSize - 5;
                
                // Background circle
                g.setColor(new Color(50, 50, 50, 150));
                g.fillOval(cdX, cdY, cdSize, cdSize);
                
                // Cooldown arc (fills as cooldown progresses)
                g.setColor(new Color(100, 200, 255, 200));
                int arcAngle = (int) (360 * cooldownPercent);
                g.fillArc(cdX, cdY, cdSize, cdSize, 90, -arcAngle);
                
                // Border
                g.setColor(Color.WHITE);
                g.drawOval(cdX, cdY, cdSize, cdSize);
            }
        }

        // Bottom-center: Controls to avoid overlapping top info
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        String controls = "Move: Arrow Keys    Attack: SPACE    Dash: SHIFT (Warrior/Rogue)    Pause: P    Restart: R";
        int controlsY = HEIGHT - 18;
        int controlsX = WIDTH / 2 - g.getFontMetrics().stringWidth(controls) / 2;
        g.drawString(controls, controlsX, controlsY);
    }

    // KeyListener
    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        // Mode selection
        if (selectingMode) {
            if (code == KeyEvent.VK_1) {
                // Levels mode
                audioManager.playSound("click");
                endlessMode = false;
                selectingMode = false;
                selectingCharacter = true;
                scoreMultiplier = 1.0;
            } else if (code == KeyEvent.VK_2) {
                // Endless mode - go to difficulty selection
                audioManager.playSound("click");
                endlessMode = true;
                selectingMode = false;
                selectingDifficulty = true;
            }
            return;
        }
        
        // Difficulty selection (for endless mode)
        if (selectingDifficulty) {
            if (code == KeyEvent.VK_1) {
                // Easy
                audioManager.playSound("click");
                difficulty = "EASY";
                scoreMultiplier = 1.0;
                selectingDifficulty = false;
                selectingCharacter = true;
            } else if (code == KeyEvent.VK_2) {
                // Medium
                audioManager.playSound("click");
                difficulty = "MEDIUM";
                scoreMultiplier = 1.5;
                selectingDifficulty = false;
                selectingCharacter = true;
            } else if (code == KeyEvent.VK_3) {
                // Nightmare
                audioManager.playSound("click");
                difficulty = "NIGHTMARE";
                scoreMultiplier = 2.5;
                selectingDifficulty = false;
                selectingCharacter = true;
            }
            return;
        }
        
        // Character selection
        if (selectingCharacter) {
            if (code == KeyEvent.VK_1) {
                audioManager.playSound("click");
                player = new Warrior(WIDTH / 2 - 20, HEIGHT - 100);
                selectingCharacter = false;
                spawnBossForLevel(level);
                spawnDefaultPlayer();
            } else if (code == KeyEvent.VK_2) {
                audioManager.playSound("click");
                player = new Rogue(WIDTH / 2 - 20, HEIGHT - 100);
                selectingCharacter = false;
                spawnBossForLevel(level);
                spawnDefaultPlayer();
            } else if (code == KeyEvent.VK_3) {
                audioManager.playSound("click");
                player = new Mage(WIDTH / 2 - 20, HEIGHT - 100);
                selectingCharacter = false;
                spawnBossForLevel(level);
                spawnDefaultPlayer();
            }
            return;
        }
        
        switch (code) {
            case KeyEvent.VK_UP:    up = true; break;
            case KeyEvent.VK_DOWN:  down = true; break;
            case KeyEvent.VK_LEFT:  left = true; break;
            case KeyEvent.VK_RIGHT: right = true; break;
            case KeyEvent.VK_SPACE: attacking = true; break;
            case KeyEvent.VK_SHIFT: dashing = true; break;
            case KeyEvent.VK_P:
                paused = !paused;
                break;
            case KeyEvent.VK_R:
                if (!running || paused) restartGame();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_UP:    up = false; break;
            case KeyEvent.VK_DOWN:  down = false; break;
            case KeyEvent.VK_LEFT:  left = false; break;
            case KeyEvent.VK_RIGHT: right = false; break;
            case KeyEvent.VK_SPACE: attacking = false; break;
            case KeyEvent.VK_SHIFT: dashing = false; break;
            case KeyEvent.VK_P: /* no-op on release */ break;
        }
    }

    private void restartGame() {
        level = 1;
        running = true;
        score = 0;
        projectiles.clear();
        items.clear();
        boss = null; // Clear boss until character is selected
        selectingMode = true;
        selectingCharacter = false;
        selectingDifficulty = false;
        player = null;
        lastItemSpawnMs = System.currentTimeMillis();
        nextItemSpawnMs = System.currentTimeMillis() + getRandomSpawnDelay();
        shieldUntilMs = 0;
        playerHitFlashUntilMs = 0;
        explosionUntilMs = 0;
    }

    private int getRandomSpawnDelay() {
        // Spawn rate increases per level
        int baseMin = 10000; // 10 seconds
        int baseMax = 18000; // 18 seconds
        
        // Level 1: 0%, Level 2: 60%, Level 3: 80%, Level 4: 90%
        double speedIncrease = 0;
        if (level == 2) speedIncrease = 0.60;
        else if (level == 3) speedIncrease = 0.80;
        else if (level >= 4) speedIncrease = 0.90;
        
        double multiplier = 1.0 - speedIncrease;
        
        int minInterval = (int)(baseMin * multiplier);
        int maxInterval = (int)(baseMax * multiplier);
        
        return minInterval + random.nextInt(Math.max(1, maxInterval - minInterval));
    }

    private void spawnRandomItem() {
        // Random position within arena bounds (with margins), avoiding boss
        int margin = 60;
        int x, y;
        int attempts = 0;
        
        do {
            x = margin + random.nextInt(WIDTH - margin * 2 - 20);
            y = margin + random.nextInt(HEIGHT - margin * 2 - 20);
            attempts++;
        } while (attempts < 50 && boss != null && isNearBoss(x, y, boss, 100)); // Keep 100 pixel distance from boss
        
        // Difficulty-based item spawn rates
        int choice;
        if (endlessMode) {
            if (difficulty.equals("EASY")) {
                // Easy: More hearts (40%) and shields (30%), fewer bombs (10%)
                int roll = random.nextInt(100);
                if (roll < 40) {
                    choice = 0; // Heart
                } else if (roll < 60) {
                    choice = 1; // Orb
                } else if (roll < 90) {
                    choice = 2; // Shield
                } else {
                    choice = 3; // Bomb
                }
            } else if (difficulty.equals("NIGHTMARE")) {
                // Nightmare: More bombs (40%), fewer hearts (15%) and shields (15%)
                int roll = random.nextInt(100);
                if (roll < 15) {
                    choice = 0; // Heart
                } else if (roll < 30) {
                    choice = 1; // Orb
                } else if (roll < 45) {
                    choice = 2; // Shield
                } else {
                    choice = 3; // Bomb
                }
            } else {
                // Medium: Balanced (25% each)
                choice = random.nextInt(4);
            }
        } else {
            // Levels mode: Normal spawn (25% each)
            choice = random.nextInt(4);
        }
        
        // Spawn the selected item
        if (choice == 0) {
            items.add(new HeartItem(x, y));
        } else if (choice == 1) {
            items.add(new OrbItem(x, y));
        } else if (choice == 2) {
            items.add(new ShieldItem(x, y));
        } else {
            items.add(new BombItem(x, y));
        }
    }
    
    private boolean isNearBoss(int x, int y, Boss b, int minDistance) {
        int bx = b.getX() + b.getSize() / 2;
        int by = b.getY() + b.getSize() / 2;
        double dist = Math.hypot(x - bx, y - by);
        return dist < minDistance;
    }
}
