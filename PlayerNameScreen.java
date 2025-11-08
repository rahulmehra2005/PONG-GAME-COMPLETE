package PONGGAME;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PlayerNameScreen extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ModeSelectionScreen modeScreen;
    private GamePanel gamePanel;

    private JTextField player1Field;
    private JTextField player2Field;
    private JComboBox<String> difficultyCombo;
    private JButton startButton;
    private JPanel inputPanel;
    private BufferedImage backgroundImage;

    public PlayerNameScreen(CardLayout cardLayout, JPanel mainPanel, ModeSelectionScreen modeScreen, GamePanel gamePanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        this.modeScreen = modeScreen;
        this.gamePanel = gamePanel;
        

        setLayout(new BorderLayout());

        try {
            backgroundImage = ImageIO.read(new File("C:\\Users\\rahul\\Desktop\\College\\NOTES\\vspong.jpg"));
        } catch (IOException | IllegalArgumentException e) {
            backgroundImage = null;
        }

        // New: Back button on top left
        JButton backButton = new JButton("← BACK");
        backButton.setFont(new Font("Arial", Font.BOLD, 15));
        backButton.setBackground(Color.black);
        backButton.setForeground(Color.white);
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(100, 20));
        backButton.addActionListener(e -> {
            // Removed: stopBackgroundAudio();  // Do not stop theme here to allow continuity
            cardLayout.show(mainPanel, "ModeSelection");
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(backButton, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Title label centered in the middle of the screen
        JLabel titleLabel = new JLabel("ENTER PLAYER INFO", SwingConstants.CENTER);  // Updated: Uppercase
        titleLabel.setFont(new Font("Serif", Font.BOLD, 72));
        titleLabel.setForeground(Color.YELLOW);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        // Form panel without border, centered in lower middle
        inputPanel = new JPanel();
        inputPanel.setOpaque(false);
        inputPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;

        inputPanel.add(new JSeparator(), gbc);

        gbc.gridy++;
        JLabel lblP1 = new JLabel("PLAYER 1 NAME:");  // Updated: Uppercase
        lblP1.setFont(new Font("Times New Roman", Font.BOLD, 35));
        lblP1.setForeground(Color.orange);
        inputPanel.add(lblP1, gbc);

        gbc.gridx = 1;
        player1Field = new JTextField(15);
        player1Field.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        ((AbstractDocument) player1Field.getDocument()).setDocumentFilter(new AlphabetFilter());  // Updated: Alphabet-only filter
        inputPanel.add(player1Field, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblP2 = new JLabel("PLAYER 2 NAME:");  // Updated: Uppercase
        lblP2.setFont(new Font("Times New Roman", Font.BOLD, 35));
        lblP2.setForeground(Color.orange);
        inputPanel.add(lblP2, gbc);

        gbc.gridx = 1;
        player2Field = new JTextField(15);
        player2Field.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        ((AbstractDocument) player2Field.getDocument()).setDocumentFilter(new AlphabetFilter());  // Updated: Alphabet-only filter
        inputPanel.add(player2Field, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblDiff = new JLabel("DIFFICULTY:");  // Updated: Uppercase
        lblDiff.setFont(new Font("Times New Roman", Font.BOLD, 35));
        lblDiff.setForeground(Color.orange);
        inputPanel.add(lblDiff, gbc);

        gbc.gridx = 1;
        String[] difficulties = {"EASY", "MEDIUM", "HARD"};  // Updated: Uppercase
        difficultyCombo = new JComboBox<>(difficulties);
        difficultyCombo.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        inputPanel.add(difficultyCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        startButton = new JButton("START GAME");  // Updated: Uppercase
        startButton.setFont(new Font("Times New Roman", Font.BOLD, 43));
        startButton.setBackground(Color.black);
        startButton.setForeground(Color.YELLOW);
        startButton.setFocusPainted(false);
        startButton.setPreferredSize(new Dimension(400, 60));
        startButton.addActionListener(ae -> startGame());

        inputPanel.add(startButton, gbc);

        // Position the inputPanel in the lower middle
        add(inputPanel, BorderLayout.SOUTH);

        // New: Start theme audio when screen is shown (using shared clip)
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                playThemeAudio();
            }
        });

        adjustInputs();
    }

    // New: DocumentFilter to allow only alphabets
    private static class AlphabetFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string != null && string.matches("[a-zA-Z ]*")) {  // Allow letters and spaces
                super.insertString(fb, offset, string.toUpperCase(), attr);  // Updated: Convert to uppercase
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text != null && text.matches("[a-zA-Z ]*")) {  // Allow letters and spaces
                super.replace(fb, offset, length, text.toUpperCase(), attrs);  // Updated: Convert to uppercase
            }
        }
    }

    public void adjustInputs() {
        boolean single = modeScreen.isSinglePlayer();

        player2Field.setText("");  // Updated: Always empty, no "AI"
        player2Field.setEditable(!single);
        player2Field.setEnabled(!single);
        player2Field.setVisible(!single);  // Updated: Hide in single player
        difficultyCombo.setVisible(single);

        Component[] comps = inputPanel.getComponents();
        for (Component c : comps) {
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                if (lbl.getText().equals("PLAYER 2 NAME:")) lbl.setVisible(!single);  // Updated: Uppercase
                if (lbl.getText().equals("DIFFICULTY:")) lbl.setVisible(single);  // Updated: Uppercase
            }
        }

        revalidate();
        repaint();
    }

    private void startGame() {
        String p1 = player1Field.getText().trim().toUpperCase();  // Updated: Uppercase
        String p2 = player2Field.getText().trim().toUpperCase();  // Updated: Uppercase
        String difficulty = (String) difficultyCombo.getSelectedItem();

        if (p1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "PLEASE ENTER PLAYER 1 NAME.");  // Updated: Uppercase
            return;
        }
        if (!modeScreen.isSinglePlayer() && p2.isEmpty()) {
            JOptionPane.showMessageDialog(this, "PLEASE ENTER PLAYER 2 NAME.");  // Updated: Uppercase
            return;
        }
        if (modeScreen.isSinglePlayer()) p2 = "COMPUTER";

        // Updated: Check for uniqueness and alphabet-only (already enforced by filter)
        if (p1.equals(p2) && !modeScreen.isSinglePlayer()) {
            JOptionPane.showMessageDialog(this, "PLAYER NAMES MUST BE UNIQUE.");  // Updated: Uppercase
            return;
        }

        // New: Stop the theme song when starting the game
        if (GameFrame.themeClip != null && GameFrame.themeClip.isRunning()) {
            GameFrame.themeClip.stop();
            GameFrame.themeClip.close();
        }

        gamePanel.setPlayers(p1, p2, modeScreen.isSinglePlayer(), difficulty);
        cardLayout.show(mainPanel, "Game");
        gamePanel.requestFocusInWindow();
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
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);  // Draw background image full screen
        } else {
            // Fallback: solid color if image fails
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}