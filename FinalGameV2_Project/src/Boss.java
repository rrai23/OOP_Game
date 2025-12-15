package src;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

/**
 * Abstract Boss encapsulates boss statistics and behaviors.
 */
public abstract class Boss {
    private int x;
    private int y;
    private int size = 80;
    private int health;
    private int maxHealth;
    private int projectileSpeed;
    private boolean weakPointActive;

    // Timers for weak point activation
    private long lastWeakToggleMs = System.currentTimeMillis();
    private int weakOpenMs = 1500;  // duration open
    private int weakClosedMs = 2500; // duration closed

    public Boss(int x, int y, int health, int projectileSpeed) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.maxHealth = health;
        this.projectileSpeed = projectileSpeed;
    }

    public void updateWeakPoint() {
        long now = System.currentTimeMillis();
        if (weakPointActive) {
            if (now - lastWeakToggleMs > weakOpenMs) {
                weakPointActive = false;
                lastWeakToggleMs = now;
            }
        } else {
            if (now - lastWeakToggleMs > weakClosedMs) {
                weakPointActive = true;
                lastWeakToggleMs = now;
            }
        }
    }

    public void draw(Graphics g) {
        g.setColor(weakPointActive ? Color.RED : Color.ORANGE);
        g.fillOval(x, y, size, size);
    }

    public abstract void attackPattern(List<Projectile> projectiles, Character player);

    // Configuration helpers
    protected void setWeakDurations(int openMs, int closedMs) {
        this.weakOpenMs = openMs;
        this.weakClosedMs = closedMs;
    }

    // Encapsulation
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = Math.max(0, health); }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    public int getProjectileSpeed() { return projectileSpeed; }
    public void setProjectileSpeed(int projectileSpeed) { this.projectileSpeed = projectileSpeed; }
    public boolean isWeakPointActive() { return weakPointActive; }
    public void activateWeakPoint() { this.weakPointActive = true; lastWeakToggleMs = System.currentTimeMillis(); }
}
