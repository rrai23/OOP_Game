package src;

import java.util.List;
import java.util.Random;

/**
 * Level4Boss: rapid fire mixed patterns.
 */
public class Level4Boss extends Boss {
    private long lastShot = System.currentTimeMillis();
    private int fireIntervalMs = 380;
    private final Random rng = new Random();

    public Level4Boss(int x, int y) {
        super(x, y, 260, 4);
        setWeakDurations(800, 1800);
    }

    @Override
    public void attackPattern(List<Projectile> projectiles, Character player) {
        long now = System.currentTimeMillis();
        if (now - lastShot >= fireIntervalMs) {
            lastShot = now;
            double cx = getX() + getSize() / 2.0;
            double cy = getY() + getSize() / 2.0;

            int pattern = rng.nextInt(3);
            switch (pattern) {
                case 0: // rapid straight aimed
                    double dx = (player.getX() + player.getWidth() / 2.0) - cx;
                    double dy = (player.getY() + player.getHeight() / 2.0) - cy;
                    double len = Math.max(1, Math.hypot(dx, dy));
                    double vx = (dx / len) * getProjectileSpeed();
                    double vy = (dy / len) * getProjectileSpeed();
                    projectiles.add(new StraightProjectile(cx, cy, vx, vy, 11));
                    break;
                case 1: // 5-way zigzag spread
                    for (int i = -2; i <= 2; i++) {
                        double angle = Math.atan2(
                                (player.getY() + player.getHeight() / 2.0) - cy,
                                (player.getX() + player.getWidth() / 2.0) - cx
                        ) + i * 0.15;
                        double vx2 = Math.cos(angle) * getProjectileSpeed();
                        double vy2 = Math.sin(angle) * getProjectileSpeed();
                        projectiles.add(new ZigZagProjectile(cx, cy, vx2, vy2, 12));
                    }
                    break;
                default: // multiple spirals
                    for (int i = 0; i < 3; i++) {
                        double ang = rng.nextDouble() * Math.PI * 2;
                        projectiles.add(new SpiralProjectile(cx, cy, ang, getProjectileSpeed(), 12));
                    }
                    break;
            }
        }
    }
}
