package src;

import java.awt.Color;

/**
 * Warrior: high health, high attack.
 */
public class Warrior extends Character {
    public Warrior(int x, int y) {
        super(x, y, 40, 40, 4, 120, 20);
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
        return Color.GREEN;
    }

    @Override
    protected int getWeaponReach() {
        return 70; // sword reach
    }

    @Override
    protected Color getWeaponColor() {
        return Color.LIGHT_GRAY; // sword color
    }

    @Override
    protected long getAttackCooldownMs() { return 600; } // slower swing

    @Override
    protected long getSwingDurationMs() { return 500; }

    @Override
    protected double getSwingArcRadians() { return Math.toRadians(120); } // wide sword arc
}
