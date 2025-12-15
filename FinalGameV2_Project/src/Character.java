package src;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Abstract Character encapsulates common player fields and behaviors.
 */
public abstract class Character {
    private int x;
    private int y;
    private int width;
    private int height;
    private int speed;
    private int health;
    private int maxHealth;
    private int attackPower;
    // Attack timing for animations/cooldowns
    private long lastAttackMs = 0;
    private long swingStartMs = -1;
    // Dash timing and state
    private long lastDashMs = 0;
    private long dashStartMs = -1;
    private double dashDirX = 0;
    private double dashDirY = 0;

    public Character(int x, int y, int width, int height, int speed, int health, int attackPower) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.health = health;
        this.maxHealth = health;
        this.attackPower = attackPower;
    }

    // Movement with arena bounds
    public void move(int dx, int dy, int arenaW, int arenaH) {
        x += dx * speed;
        y += dy * speed;
        // Constrain to arena inside margins (40)
        int minX = 40;
        int minY = 40;
        int maxX = arenaW - 40 - width;
        int maxY = arenaH - 40 - height;
        if (x < minX) x = minX;
        if (y < minY) y = minY;
        if (x > maxX) x = maxX;
        if (y > maxY) y = maxY;
    }

    // Polymorphic attack behavior
    public abstract void attack(Boss boss);

    // Draw player rectangle with color per subclass
    public void draw(Graphics g) {
        g.setColor(getColor());
        g.fillRect(x, y, width, height);
    }

    protected abstract Color getColor();

    // Weapon visuals and hitbox
    public void drawWeapon(Graphics g, Boss boss, boolean attacking) {
        if (boss == null) return;
        // Aim toward boss center
        int cx = x + width / 2;
        int cy = y + height / 2;
        int bx = boss.getX() + boss.getSize() / 2;
        int by = boss.getY() + boss.getSize() / 2;
        double dx = bx - cx;
        double dy = by - cy;
        double len = Math.max(1, Math.hypot(dx, dy));
        double ux = dx / len;
        double uy = dy / len;

        int reach = getDynamicReach();
        int thickness = getDynamicThickness(attacking);
        int sx = cx;
        int sy = cy;
        int ex = (int)(cx + ux * reach);
        int ey = (int)(cy + uy * reach);

        g.setColor(getWeaponColor());
        // Draw as rectangle approximating a thick line
        // Compute perpendicular
        double px = -uy;
        double py = ux;
        int hx = (int)(px * thickness / 2);
        int hy = (int)(py * thickness / 2);
        int[] xs = new int[] { sx + hx, sx - hx, ex - hx, ex + hx };
        int[] ys = new int[] { sy + hy, sy - hy, ey - hy, ey + hy };
        g.fillPolygon(xs, ys, 4);
    }

    // Hitbox as rectangle polygon points toward boss
    public int[][] getWeaponHitbox(Boss boss) {
        int cx = x + width / 2;
        int cy = y + height / 2;
        int bx = boss.getX() + boss.getSize() / 2;
        int by = boss.getY() + boss.getSize() / 2;
        double dx = bx - cx;
        double dy = by - cy;
        double len = Math.max(1, Math.hypot(dx, dy));
        double ux = dx / len;
        double uy = dy / len;
        int reach = getWeaponReach();
        int thickness = 10;
        int sx = cx;
        int sy = cy;
        int ex = (int)(cx + ux * reach);
        int ey = (int)(cy + uy * reach);
        double px = -uy;
        double py = ux;
        int hx = (int)(px * thickness / 2);
        int hy = (int)(py * thickness / 2);
        int[] xs = new int[] { sx + hx, sx - hx, ex - hx, ex + hx };
        int[] ys = new int[] { sy + hy, sy - hy, ey - hy, ey + hy };
        return new int[][] { xs, ys };
    }

    protected abstract int getWeaponReach();
    protected abstract Color getWeaponColor();

    // Encapsulation via getters/setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = Math.max(0, health); }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    public int getAttackPower() { return attackPower; }
    public void setAttackPower(int attackPower) { this.attackPower = attackPower; }

    // Cooldown and swing animation support
    protected boolean canAttack() {
        long now = System.currentTimeMillis();
        return (now - lastAttackMs) >= getAttackCooldownMs();
    }

    protected void markAttack() {
        long now = System.currentTimeMillis();
        lastAttackMs = now;
        swingStartMs = now;
    }

    private int getDynamicThickness(boolean attacking) {
        if (!attacking || swingStartMs < 0) return 6;
        long now = System.currentTimeMillis();
        long elapsed = now - swingStartMs;
        long dur = getSwingDurationMs();
        double t = Math.min(1.0, elapsed / (double) dur);
        return 6 + (int) Math.round(6 * Math.sin(t * Math.PI)); // pulse
    }

    private int getDynamicReach() {
        if (swingStartMs < 0) return getWeaponReach();
        long now = System.currentTimeMillis();
        long elapsed = now - swingStartMs;
        long dur = getSwingDurationMs();
        double t = Math.min(1.0, elapsed / (double) dur);
        // Ease-out reach for visual swing
        return (int) (getWeaponReach() * (0.6 + 0.4 * t));
    }

    protected abstract long getAttackCooldownMs();
    protected abstract long getSwingDurationMs();
    protected abstract double getSwingArcRadians();
    
    // Public accessors for cooldown indicator
    public long getLastAttackTime() {
        return lastAttackMs;
    }
    
    public long getAttackCooldown() {
        return getAttackCooldownMs();
    }
    
    public long getLastDashTime() {
        return lastDashMs;
    }
    
    // Dash system - only for warrior and rogue
    public boolean canDash() {
        return false; // Override in subclasses that can dash
    }
    
    public void startDash(double dirX, double dirY) {
        long now = System.currentTimeMillis();
        lastDashMs = now;
        dashStartMs = now;
        dashDirX = dirX;
        dashDirY = dirY;
    }
    
    public boolean isDashing() {
        if (dashStartMs < 0) return false;
        long now = System.currentTimeMillis();
        return (now - dashStartMs) < getDashDurationMs();
    }
    
    public void updateDash(int arenaW, int arenaH) {
        if (isDashing()) {
            // Move in dash direction at high speed
            int dashSpeed = getDashSpeed();
            x += (int)(dashDirX * dashSpeed);
            y += (int)(dashDirY * dashSpeed);
            // Constrain to arena
            int minX = 40;
            int minY = 40;
            int maxX = arenaW - 40 - width;
            int maxY = arenaH - 40 - height;
            if (x < minX) x = minX;
            if (y < minY) y = minY;
            if (x > maxX) x = maxX;
            if (y > maxY) y = maxY;
        }
    }
    
    protected long getDashCooldownMs() {
        return 1000; // 1 second default
    }
    
    protected long getDashDurationMs() {
        return 150; // 150ms dash
    }
    
    protected int getDashSpeed() {
        return 15; // Fast dash speed
    }
}
