package src;

/**
 * Moves in a straight line based on initial velocity.
 */
public class StraightProjectile extends Projectile {
    public StraightProjectile(double x, double y, double vx, double vy, int damage) {
        super(x, y, vx, vy, damage);
    }

    @Override
    public void move() {
        setX(getX() + getVx());
        setY(getY() + getVy());
    }
}
