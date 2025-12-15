package src;

import java.util.List;

/**
 * Level3Boss: spiral pattern with periodic bursts.
 */
public class Level3Boss extends Boss {
    private long lastShot = System.currentTimeMillis();
    private int fireIntervalMs = 550;
    private double spiralAngle = 0;

    public Level3Boss(int x, int y) {
        super(x, y, 200, 3);
        setWeakDurations(1000, 2000);
    }

    @Override
    public void attackPattern(List<Projectile> projectiles, Character player) {
        long now = System.currentTimeMillis();
        if (now - lastShot >= fireIntervalMs) {
            lastShot = now;
            double cx = getX() + getSize() / 2.0;
            double cy = getY() + getSize() / 2.0;
            
            // Spawn 2 straight projectiles aimed at player
            double px = player.getX() + player.getWidth() / 2.0;
            double py = player.getY() + player.getHeight() / 2.0;
            
            // Center shot directly at player
            double dx = px - cx;
            double dy = py - cy;
            double len = Math.max(1, Math.hypot(dx, dy));
            double vx = (dx / len) * getProjectileSpeed();
            double vy = (dy / len) * getProjectileSpeed();
            projectiles.add(new StraightProjectile(cx, cy, vx, vy, 10));
            
            // One angled shot (20 degrees offset)
            double angle = Math.atan2(dy, dx);
            double spread = Math.toRadians(20);
            
            // Offset shot
            double offsetAngle = angle + spread;
            double offsetVx = Math.cos(offsetAngle) * getProjectileSpeed();
            double offsetVy = Math.sin(offsetAngle) * getProjectileSpeed();
            projectiles.add(new StraightProjectile(cx, cy, offsetVx, offsetVy, 10));
            
            spiralAngle += 0.3; // advance base angle for next wave
        }
    }
}
