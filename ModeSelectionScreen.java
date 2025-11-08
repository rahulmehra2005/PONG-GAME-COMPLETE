package PONGGAME;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ModeSelectionScreen extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private boolean isSinglePlayer;

    private Runnable modeChangeListener;
    private BufferedImage backgroundImage;

    public ModeSelectionScreen(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;

        // Load background image from local system
        try {
            backgroundImage = ImageIO.read(new File("C:\\Users\\rahul\\Desktop\\College\\NOTES\\modePONG.jpg"));  // Update this path to your local image file
        } catch (IOException | IllegalArgumentException e) {
            backgroundImage = null;  // Fallback if image fails to load
        }

        setLayout(new BorderLayout());
        setOpaque(false);  // Allow background image to show

        // New: Arrow button on top left to go back to welcome
        JButton backButton = new JButton("← BACK");  // Arrow button
        backButton.setFont(new Font("Arial", Font.BOLD, 15));
        backButton.setBackground(Color.black);
        backButton.setForeground(Color.white);
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(100, 20));  // Updated: Reduced size to 50x30
        backButton.addActionListener(e -> {
            // Removed: stopBackgroundAudio();  // Do not stop theme here to allow continuity
            cardLayout.show(mainPanel, "Welcome");
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(backButton, BorderLayout.WEST);  // Top left

        // Title label at the top center
        JLabel titleLabel = new JLabel("SELECT GAME MODE", SwingConstants.CENTER);  // Updated: Uppercase
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 72));  // Updated: Increased font size
        titleLabel.setForeground(Color.YELLOW);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Bottom panel for centering buttons in the bottom half
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        // Button panel centered in the bottom half
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        buttonPanel.setOpaque(false);

        JButton singlePlayerButton = new JButton("SINGLE PLAYER");  // Updated: Uppercase and white background with black text
        JButton multiPlayerButton = new JButton("MULTIPLAYER");  // Updated: Uppercase and white background with black text

        styleButton(singlePlayerButton);
        styleButton(multiPlayerButton);

        singlePlayerButton.addActionListener(e -> {
            System.out.println("SINGLE PLAYER BUTTON CLICKED");  // Debug: Check console
            isSinglePlayer = true;
            notifyModeChange();
            // Removed: stopBackgroundAudio();  // Do not stop theme here to allow continuity
            cardLayout.show(mainPanel, "PlayerName");
        });

        multiPlayerButton.addActionListener(e -> {
            System.out.println("MULTIPLAYER BUTTON CLICKED");  // Debug: Check console
            isSinglePlayer = false;
            notifyModeChange();
            // Removed: stopBackgroundAudio();  // Do not stop theme here to allow continuity
            cardLayout.show(mainPanel, "PlayerName");
        });

        buttonPanel.add(singlePlayerButton);
        buttonPanel.add(multiPlayerButton);

        bottomPanel.add(buttonPanel, BorderLayout.CENTER);  // Center buttons in bottom panel
        add(bottomPanel, BorderLayout.SOUTH);

        // New: Start theme audio when screen is shown (using shared clip)
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                playThemeAudio();
            }
        });
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Times New Roman", Font.PLAIN, 56));
        button.setBackground(Color.black);  // Updated: White background
        button.setForeground(Color.yellow);  // Updated: Black text
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
    }

    public boolean isSinglePlayer() {
        return isSinglePlayer;
    }

    public void setModeChangeListener(Runnable listener) {
        this.modeChangeListener = listener;
    }

    private void notifyModeChange() {
        if (modeChangeListener != null) {
            modeChangeListener.run();
        }
    }

    // New: Play theme audio using shared clip (only if not already running)
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