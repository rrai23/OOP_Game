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
            
            // Mix of spiral projectiles (5) and straight chasing projectiles (3)
            double base = spiralAngle;
            
            // Spawn 5 spiral projectiles that circle and pulse around boss
            int spiralCount = 5;
            double spiralStep = (Math.PI * 2) / spiralCount;
            for (int i = 0; i < spiralCount; i++) {
                double ang = base + i * spiralStep;
                projectiles.add(new SpiralProjectile(cx, cy, ang, getProjectileSpeed(), 10));
            }
            
            // Spawn 3 straight projectiles aimed at player
            double px = player.getX() + player.getWidth() / 2.0;
            double py = player.getY() + player.getHeight() / 2.0;
            
            // Center shot directly at player
            double dx = px - cx;
            double dy = py - cy;
            double len = Math.max(1, Math.hypot(dx, dy));
            double vx = (dx / len) * getProjectileSpeed();
            double vy = (dy / len) * getProjectileSpeed();
            projectiles.add(new StraightProjectile(cx, cy, vx, vy, 10));
            
            // Two angled shots (15 degrees left and right)
            double angle = Math.atan2(dy, dx);
            double spread = Math.toRadians(15);
            
            // Left angled shot
            double leftAngle = angle - spread;
            double leftVx = Math.cos(leftAngle) * getProjectileSpeed();
            double leftVy = Math.sin(leftAngle) * getProjectileSpeed();
            projectiles.add(new StraightProjectile(cx, cy, leftVx, leftVy, 10));
            
            // Right angled shot
            double rightAngle = angle + spread;
            double rightVx = Math.cos(rightAngle) * getProjectileSpeed();
            double rightVy = Math.sin(rightAngle) * getProjectileSpeed();
            projectiles.add(new StraightProjectile(cx, cy, rightVx, rightVy, 10));
            
            spiralAngle += 0.3; // advance base angle for next wave
        }
    }
}
