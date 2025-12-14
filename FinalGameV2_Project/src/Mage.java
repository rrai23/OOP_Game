package src;

import java.awt.Color;

/**
 * Mage: ranged magic attack (longer reach simulated via extra damage when weak point open).
 */
public class Mage extends Character {
    public Mage(int x, int y) {
        super(x, y, 34, 34, 5, 80, 12);
    }

    @Override
    public void attack(Boss boss) {
        // Mage melee swing animation only; ranged handled via PlayerProjectile
        if (!canAttack()) return;
        markAttack();
    }

    @Override
    protected Color getColor() {
        return Color.MAGENTA;
    }

    @Override
    protected int getWeaponReach() {
        return 80; // staff longer reach visually
    }

    @Override
    protected Color getWeaponColor() {
        return Color.YELLOW; // staff/golden
    }

    @Override
    protected long getAttackCooldownMs() { return 400; } // medium

    @Override
    protected long getSwingDurationMs() { return 350; }

    @Override
    protected double getSwingArcRadians() { return Math.toRadians(90); }
}
