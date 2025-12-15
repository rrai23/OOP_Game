package src;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

/**
 * Represents a single particle for visual effects.
 */
public class Particle {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private final Color color;
    private final long spawnTime;
    private final long lifetime;
    private final int size;
    
    public Particle(double x, double y, double vx, double vy, Color color, long lifetime, int size) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
        this.spawnTime = System.currentTimeMillis();
        this.lifetime = lifetime;
        this.size = size;
    }
    
    public void update() {
        x += vx;
        y += vy;
        // Slow down over time
        vx *= 0.95;
        vy *= 0.95;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - spawnTime > lifetime;
    }
    
    public void draw(Graphics g) {
        long elapsed = System.currentTimeMillis() - spawnTime;
        double progress = elapsed / (double) lifetime;
        
        // Fade out
        int alpha = (int) ((1.0 - progress) * 255);
        alpha = Math.max(0, Math.min(255, alpha));
        
        Color fadedColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        g.setColor(fadedColor);
        g.fillOval((int) x, (int) y, size, size);
    }
    
    /**
     * Create particles for a projectile trail effect
     */
    public static Particle createTrailParticle(double x, double y, Color baseColor) {
        Random rand = new Random();
        double vx = (rand.nextDouble() - 0.5) * 0.5;
        double vy = (rand.nextDouble() - 0.5) * 0.5;
        return new Particle(x, y, vx, vy, baseColor, 300, 3);
    }
}
