package src;

import java.util.List;

/**
 * Level2Boss: multi-shot pattern.
 */
public class Level2Boss extends Boss {
    private long lastShot = System.currentTimeMillis();
    private int fireIntervalMs = 750;

    public Level2Boss(int x, int y) {
        super(x, y, 160, 3);
        setWeakDurations(1200, 2200);
    }

    @Override
    public void attackPattern(List<Projectile> projectiles, Character player) {
        long now = System.currentTimeMillis();
        if (now - lastShot >= fireIntervalMs) {
            lastShot = now;
            double cx = getX() + getSize() / 2.0;
            double cy = getY() + getSize() / 2.0;
            // 3-way spread using zig-zag projectiles
            for (int i = -1; i <= 1; i++) {
                double angle = Math.atan2(
                        (player.getY() + player.getHeight() / 2.0) - cy,
                        (player.getX() + player.getWidth() / 2.0) - cx
                ) + i * 0.2;
                double vx = Math.cos(angle) * getProjectileSpeed();
                double vy = Math.sin(angle) * getProjectileSpeed();
                projectiles.add(new ZigZagProjectile(cx, cy, vx, vy, 9));
            }
        }
    }
}
