package src;

import java.awt.Color;

/**
 * Rogue: high speed.
 */
public class Rogue extends Character {
    public Rogue(int x, int y) {
        super(x, y, 36, 36, 7, 90, 14);
    }

    @Override
    public void attack(Boss boss) {
        if (boss == null) return;
        if (!canAttack()) return;
        // Always mark swing to show animation
        markAttack();
        // Only deal damage if weak point is open
        if (boss.isWeakPointActive()) {
            boss.setHealth(boss.getHealth() - getAttackPower());
        }
    }

    @Override
    protected Color getColor() {
        return Color.CYAN;
    }

    @Override
    protected int getWeaponReach() {
        return 50; // dagger shorter reach
    }

    @Override
    protected Color getWeaponColor() {
        return Color.WHITE; // dagger color
    }

    @Override
    protected long getAttackCooldownMs() { return 250; } // fast dagger

    @Override
    protected long getSwingDurationMs() { return 200; }

    @Override
    protected double getSwingArcRadians() { return Math.toRadians(70); } // tighter, faster arc
}
