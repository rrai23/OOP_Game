package src;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Abstract Item class for pickups that players can collect.
 */
public abstract class Item {
    private int x;
    private int y;
    private int size = 20;

    public Item(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void applyEffect(Character player, java.util.List<Projectile> projectiles);
    
    public abstract void draw(Graphics g);
    
    public boolean collidesWith(Character c) {
        int cx = c.getX();
        int cy = c.getY();
        int cw = c.getWidth();
        int ch = c.getHeight();
        return (x + size > cx && x < cx + cw && y + size > cy && y < cy + ch);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getSize() { return size; }
    protected void setSize(int size) { this.size = size; }
}
