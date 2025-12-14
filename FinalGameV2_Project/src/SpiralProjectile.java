package src;

/**
 * Spiral projectile rotates its velocity vector to create a spiral.
 * Can pulse (expand/contract) for Level 3 pattern.
 */
public class SpiralProjectile extends Projectile {
    private double angle;
    private double speed;
    private double radius; // distance from spawn point
    private double radiusSpeed = 2.0; // how fast radius changes
    private boolean expanding = true; // pulse direction
    private double minRadius = 30;
    private double maxRadius = 150;
    private double centerX;
    private double centerY;
    private int tickCount = 0;

    public SpiralProjectile(double x, double y, double initialAngle, double speed, int damage) {
        super(x, y, 0, 0, damage);
        this.angle = initialAngle;
        this.speed = speed;
        this.centerX = x;
        this.centerY = y;
        this.radius = minRadius;
    }

    @Override
    public void move() {
        tickCount++;
        angle += 0.08; // rotate around boss
        
        // Pulse radius: expand then contract
        if (expanding) {
            radius += radiusSpeed;
            if (radius >= maxRadius) {
                expanding = false;
            }
        } else {
            radius -= radiusSpeed;
            if (radius <= minRadius) {
                expanding = true;
            }
        }
        
        // Position on circular path with current radius
        setX(centerX + Math.cos(angle) * radius);
        setY(centerY + Math.sin(angle) * radius);
    }
}
