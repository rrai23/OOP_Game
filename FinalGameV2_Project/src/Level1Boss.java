package src;

import java.util.List;

/**
 * Level1Boss: slow single shots.
 */
public class Level1Boss extends Boss {
    private long lastShot = System.currentTimeMillis();
    private int fireIntervalMs = 900;

    public Level1Boss(int x, int y) {
        super(x, y, 120, 3);
        setWeakDurations(1500, 2500);
    }

    @Override
    public void attackPattern(List<Projectile> projectiles, Character player) {
        long now = System.currentTimeMillis();
        if (now - lastShot >= fireIntervalMs) {
            lastShot = now;
            double px = getX() + getSize() / 2.0;
            double py = getY() + getSize() / 2.0;
            // Aim roughly towards player
            double dx = (player.getX() + player.getWidth() / 2.0) - px;
            double dy = (player.getY() + player.getHeight() / 2.0) - py;
            double len = Math.max(1, Math.hypot(dx, dy));
            double vx = (dx / len) * getProjectileSpeed();
            double vy = (dy / len) * getProjectileSpeed();
            projectiles.add(new StraightProjectile(px, py, vx, vy, 8));
        }
    }
}
