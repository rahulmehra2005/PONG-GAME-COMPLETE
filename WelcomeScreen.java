package PONGGAME;

import javax.imageio.ImageIO;
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.Clip;
//import javax.sound.sampled.LineUnavailableException;
//import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.event.ComponentAdapter;
//import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WelcomeScreen extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private BufferedImage backgroundImage;

    public WelcomeScreen(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;

        // Load background image from local system
        try {
            backgroundImage = ImageIO.read(new File("C:\\Users\\rahul\\Desktop\\College\\NOTES\\welcomepong.jpg"));  // Update this path to your local image file
        } catch (IOException | IllegalArgumentException e) {
            backgroundImage = null;  // Fallback if image fails to load
        }

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  // Vertical layout for stacking
        setOpaque(false);  // Allow background image to show

        // Welcome message label
        JLabel welcomeLabel = new JLabel("Welcome to the Pong Game", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 48));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);  // Center horizontally
        add(Box.createVerticalGlue());  // Push to center vertically

        // Start Game button
        JButton startButton = new JButton("START GAME");
        startButton.setFont(new Font("Times New Roman", Font.BOLD, 36));
        startButton.setBackground(Color.black);
        startButton.setForeground(Color.YELLOW);
        startButton.setFocusPainted(false);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);  // Center horizontally
        add(Box.createRigidArea(new Dimension(0, 20)));  // Small gap below message
        add(startButton);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Removed: stopBackgroundAudio();  // Do not stop theme here to allow continuity
                cardLayout.show(mainPanel, "ModeSelection");  // Navigate to mode selection
            }
        });

        // New: Exit button below Start Game with no space
        JButton exitButton = new JButton("  EXIT GAME  ");
        exitButton.setFont(new Font("Times New Roman", Font.BOLD, 36));
        exitButton.setBackground(Color.black);
        exitButton.setForeground(Color.RED);
        exitButton.setFocusPainted(false);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);  // Center horizontally
        add(Box.createRigidArea(new Dimension(0, 0)));  // No space between buttons
        add(exitButton);

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);  // Exit the application
            }
        });

        add(Box.createVerticalGlue());  // Push to center vertically
    }
        // New: Start theme audio when screen is shown (using shared clip)
        /*addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                playThemeAudio();
            }
        });
    }

    // New: Play theme audio using shared clip
    private void playThemeAudio() {
        try {
            if (GameFrame.themeClip == null || !GameFrame.themeClip.isRunning()) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("C:\\Users\\rahul\\Downloads\\retro-chiptune-adventure-8-bit-video-game-music-318059.wav"));
                GameFrame.themeClip = AudioSystem.getClip();
                GameFrame.themeClip.open(audioInputStream);
                GameFrame.themeClip.loop(Clip.LOOP_CONTINUOUSLY);  // Loop continuously
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
        */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);  // Draw background image
        } else {
            // Fallback: solid color if image fails
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}