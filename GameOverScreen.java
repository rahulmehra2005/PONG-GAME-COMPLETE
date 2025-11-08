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

public class GameOverScreen extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JLabel winnerLabel;

    // New: Background image
    private BufferedImage backgroundImage;

    public GameOverScreen(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;

        // Load background image from local system
        try {
            backgroundImage = ImageIO.read(new File("C:\\Users\\rahul\\Downloads\\overrpong.png"));  // Update this path to your local image file
        } catch (IOException | IllegalArgumentException e) {
            backgroundImage = null;  // Fallback if image fails to load
        }

        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        winnerLabel = new JLabel("", SwingConstants.CENTER);
        winnerLabel.setFont(new Font("Times New Roman", Font.BOLD, 72));
        winnerLabel.setForeground(Color.orange);
        add(winnerLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setLayout(new GridLayout(1, 2, 20, 0));

        JButton playAgainButton = new JButton("PLAY AGAIN");  // Updated: Uppercase and white background with black text
        playAgainButton.setFont(new Font("Times New Roman", Font.PLAIN, 56));
        playAgainButton.setBackground(Color.black);  // Updated: White background
        playAgainButton.setForeground(Color.yellow);  // Updated: Black text
        playAgainButton.setBorder(BorderFactory.createRaisedBevelBorder());
        playAgainButton.setFocusPainted(false);
        playAgainButton.addActionListener(e -> cardLayout.show(mainPanel, "ModeSelection"));

        JButton exitButton = new JButton("EXIT");  // Updated: Uppercase and white background with black text
        exitButton.setFont(new Font("Times New Roman", Font.PLAIN, 56));
        exitButton.setBackground(Color.black);  // Updated: White background
        exitButton.setForeground(Color.yellow);  // Updated: Black text
        exitButton.setBorder(BorderFactory.createRaisedBevelBorder());
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(playAgainButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // New: Stop theme song and play victory sound when screen is shown
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // Stop the theme song
                if (GameFrame.themeClip != null && GameFrame.themeClip.isRunning()) {
                    GameFrame.themeClip.stop();
                    GameFrame.themeClip.close();
                }
                // Play victory sound once
                playGameOverSound();
            }
        });
    }

    public void setWinner(String winner) {
        // Updated: Handle single player and multiplayer differently
        // Assuming winner is player1Name or player2Name
        // For single player, if winner is player1Name, "YOU WIN!", else "COMPUTER WIN"
        // For multiplayer, winner + " WINS!"
        // But since we don't have isSinglePlayer here, we need to pass it or check if winner == "COMPUTER"
        // For simplicity, if winner == "COMPUTER", it's "COMPUTER WIN", else winner + " WINS!"
        if (winner.equals("COMPUTER")) {
            winnerLabel.setText("COMPUTER WINS!");
        } else {
            winnerLabel.setText(winner.toUpperCase() + " WINS!");
        }
    }

    public void playGameOverSound() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("C:\\Users\\rahul\\Downloads\\poppop.ai - victory sound.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();  // Play once
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