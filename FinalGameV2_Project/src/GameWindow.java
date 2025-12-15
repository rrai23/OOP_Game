package src;

import javax.swing.JFrame;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;

/**
 * GameWindow creates the main application frame using JFrame.
 * It initializes and shows the GamePanel where the game runs.
 */
public class GameWindow extends JFrame {
    public GameWindow() {
        setTitle("Dodge Adventure");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Set application icon
        try {
            File iconFile = new File("ICON.png");
            if (iconFile.exists()) {
                ImageIcon icon = new ImageIcon(iconFile.getAbsolutePath());
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            System.err.println("Could not load icon: " + e.getMessage());
        }

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
