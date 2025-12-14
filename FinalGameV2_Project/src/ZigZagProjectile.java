package src;

/**
 * ZigZag projectile oscillates horizontally while moving vertically.
 */
public class ZigZagProjectile extends Projectile {
    private int t = 0;
    public ZigZagProjectile(double x, double y, double vx, double vy, int damage) {
        super(x, y, vx, vy, damage);
    }

    @Override
    public void move() {
        t++;
        double zig = Math.sin(t * 0.2) * 3.0; // small oscillation
        setX(getX() + getVx() + zig);
        setY(getY() + getVy());
    }
}
