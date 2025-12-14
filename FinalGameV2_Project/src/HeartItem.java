package src;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Heart item heals the player by 10 HP when picked up.
 */
public class HeartItem extends Item {
    public HeartItem(int x, int y) {
        super(x, y);
    }

    @Override
    public void applyEffect(Character player, java.util.List<Projectile> projectiles) {
        int newHealth = Math.min(player.getMaxHealth(), player.getHealth() + 10);
        player.setHealth(newHealth);
    }

    @Override
    public void draw(Graphics g) {
        // Draw a simple heart shape using two circles and a triangle
        g.setColor(Color.RED);
        int s = getSize();
        int x = getX();
        int y = getY();
        
        // Simple heart approximation: two circles at top and filled polygon
        g.fillOval(x, y, s/2, s/2);
        g.fillOval(x + s/2, y, s/2, s/2);
        int[] xs = {x, x + s, x + s/2};
        int[] ys = {y + s/3, y + s/3, y + s};
        g.fillPolygon(xs, ys, 3);
    }
}
