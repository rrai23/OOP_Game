package src;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

/**
 * Orb item clears all enemy projectiles when picked up.
 */
public class OrbItem extends Item {
    public OrbItem(int x, int y) {
        super(x, y);
    }

    @Override
    public void applyEffect(Character player, java.util.List<Projectile> projectiles) {
        // Remove all non-player projectiles
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            if (!(p instanceof PlayerProjectile)) {
                it.remove();
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        // Draw a glowing orb effect
        int s = getSize();
        int x = getX();
        int y = getY();
        
        // Outer glow
        g.setColor(new Color(100, 200, 255, 100));
        g.fillOval(x - 2, y - 2, s + 4, s + 4);
        
        // Main orb
        g.setColor(new Color(150, 220, 255));
        g.fillOval(x, y, s, s);
        
        // Inner highlight
        g.setColor(Color.WHITE);
        g.fillOval(x + s/3, y + s/4, s/3, s/3);
    }
}
