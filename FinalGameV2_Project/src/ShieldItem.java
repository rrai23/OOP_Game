package src;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Shield item grants the player 5 seconds of immunity from damage.
 */
public class ShieldItem extends Item {
    public ShieldItem(int x, int y) {
        super(x, y);
    }

    @Override
    public void applyEffect(Character player, java.util.List<Projectile> projectiles) {
        // Shield effect is handled in GamePanel by setting shieldUntilMs
        // This method serves as a marker that shield was picked up
    }

    @Override
    public void draw(Graphics g) {
        // Draw a shield shape
        int s = getSize();
        int x = getX();
        int y = getY();
        
        // Outer shield border (gold)
        g.setColor(new Color(255, 215, 0));
        int[] xs = {x + s/2, x, x, x + s/2, x + s, x + s};
        int[] ys = {y, y + s/3, y + s*2/3, y + s, y + s*2/3, y + s/3};
        g.fillPolygon(xs, ys, 6);
        
        // Inner shield (lighter blue)
        g.setColor(new Color(100, 150, 255));
        int margin = 3;
        int[] xs2 = {x + s/2, x + margin, x + margin, x + s/2, x + s - margin, x + s - margin};
        int[] ys2 = {y + margin, y + s/3 + margin, y + s*2/3 - margin, y + s - margin, y + s*2/3 - margin, y + s/3 + margin};
        g.fillPolygon(xs2, ys2, 6);
        
        // Shine effect
        g.setColor(Color.WHITE);
        g.fillOval(x + s/2 - 2, y + s/3, 4, 4);
    }
}
