package src;

import java.awt.Graphics;
import java.awt.Color;

/**
 * Abstract Projectile with position, velocity and damage.
 */
public abstract class Projectile {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private int damage;
    private int size = 12;

    public Projectile(double x, double y, double vx, double vy, int damage) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
    }

    public abstract void move();

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillOval((int)x, (int)y, size, size);
    }

    public boolean collidesWith(Character c) {
        int cx = c.getX();
        int cy = c.getY();
        int cw = c.getWidth();
        int ch = c.getHeight();
        return (x + size > cx && x < cx + cw && y + size > cy && y < cy + ch);
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getVx() { return vx; }
    public void setVx(double vx) { this.vx = vx; }
    public double getVy() { return vy; }
    public void setVy(double vy) { this.vy = vy; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
