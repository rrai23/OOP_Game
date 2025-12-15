package src;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * Represents a floating damage number that appears when damage is dealt.
 */
public class DamageNumber {
    private final int damage;
    private double x;
    private double y;
    private final long spawnTime;
    private final long duration = 800; // milliseconds
    
    public DamageNumber(int damage, double x, double y) {
        this.damage = damage;
        this.x = x;
        this.y = y;
        this.spawnTime = System.currentTimeMillis();
    }
    
    public void update() {
        // Float upward
        y -= 1.5;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > duration;
    }
    
    public void draw(Graphics g) {
        long elapsed = System.currentTimeMillis() - spawnTime;
        double progress = elapsed / (double) duration;
        
        // Fade out over time
        int alpha = (int) ((1.0 - progress) * 255);
        alpha = Math.max(0, Math.min(255, alpha));
        
        // Scale up slightly at start
        double scale = Math.min(1.0, progress * 3);
        int fontSize = (int) (20 * scale);
        
        g.setFont(new Font("Arial", Font.BOLD, fontSize));
        g.setColor(new Color(255, 255, 100, alpha));
        
        String text = String.valueOf(damage);
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (int) x - textWidth / 2, (int) y);
    }
}
