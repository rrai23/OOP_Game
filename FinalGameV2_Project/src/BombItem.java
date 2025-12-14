package src;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Bomb item damages the player when picked up - a trap item!
 */
public class BombItem extends Item {
    public BombItem(int x, int y) {
        super(x, y);
    }

    @Override
    public void applyEffect(Character player, java.util.List<Projectile> projectiles) {
        // Damage the player by 15 HP
        player.setHealth(player.getHealth() - 15);
    }

    @Override
    public void draw(Graphics g) {
        // Draw a bomb with fuse
        int s = getSize();
        int x = getX();
        int y = getY();
        
        // Main bomb body (dark gray/black sphere)
        g.setColor(Color.BLACK);
        g.fillOval(x, y + s/4, s, s*3/4);
        
        // Highlight to show it's round
        g.setColor(new Color(80, 80, 80));
        g.fillOval(x + s/4, y + s/3, s/3, s/3);
        
        // Fuse (yellow-orange line)
        g.setColor(new Color(255, 150, 0));
        g.fillRect(x + s/2 - 1, y, 3, s/4);
        
        // Spark at top of fuse (red)
        g.setColor(Color.RED);
        g.fillOval(x + s/2 - 2, y - 2, 5, 5);
        
        // Warning indicator (pulsing red glow)
        int pulse = (int)(Math.sin(System.currentTimeMillis() * 0.01) * 30 + 30);
        g.setColor(new Color(255, 0, 0, pulse));
        g.fillOval(x - 3, y + s/4 - 3, s + 6, s*3/4 + 6);
    }
}
