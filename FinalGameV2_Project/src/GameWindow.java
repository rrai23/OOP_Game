package src;

import javax.swing.JFrame;

/**
 * GameWindow creates the main application frame using JFrame.
 * It initializes and shows the GamePanel where the game runs.
 */
public class GameWindow extends JFrame {
    public GameWindow() {
        setTitle("Dodge Adventure");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel panel = new GamePanel();
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        panel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        new GameWindow();
    }
}
