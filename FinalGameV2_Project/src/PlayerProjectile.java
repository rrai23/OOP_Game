package src;

import java.awt.Color;
import java.awt.Graphics;

/**
 * PlayerProjectile: fired by Mage's staff toward the boss.
 * Damages boss on hit; does not hurt the player.
 */
public class PlayerProjectile extends Projectile {
    public PlayerProjectile(double x, double y, double vx, double vy, int damage) {
        super(x, y, vx, vy, damage);
        setSize(10);
    }

    @Override
    public void move() {
        setX(getX() + getVx());
        setY(getY() + getVy());
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval((int)getX(), (int)getY(), getSize(), getSize());
    }

    public boolean collidesWithBoss(Boss b) {
        int bx = b.getX();
        int by = b.getY();
        int bs = b.getSize();
        int px = (int) getX();
        int py = (int) getY();
        int ps = getSize();
        return (px + ps > bx && px < bx + bs && py + ps > by && py < by + bs);
    }
}
