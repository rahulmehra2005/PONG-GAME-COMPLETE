package PONGGAME;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.Clip;

public class GameFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private WelcomeScreen welcomeScreen;
    private ModeSelectionScreen modeScreen;
    private PlayerNameScreen nameScreen;
    private GamePanel gamePanel;
    private GameOverScreen gameOverScreen;

    // New: Shared static Clip for theme song across screens
    public static Clip themeClip;

    public GameFrame() {
        setTitle("PONG GAME");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        welcomeScreen = new WelcomeScreen(cardLayout, mainPanel);
        modeScreen = new ModeSelectionScreen(cardLayout, mainPanel);
        gamePanel = new GamePanel(cardLayout, mainPanel);
        gameOverScreen = new GameOverScreen(cardLayout, mainPanel);
        nameScreen = new PlayerNameScreen(cardLayout, mainPanel, modeScreen, gamePanel);

        gamePanel.setGameOverScreen(gameOverScreen);
        modeScreen.setModeChangeListener(() -> nameScreen.adjustInputs());

        mainPanel.add(welcomeScreen, "Welcome");
        mainPanel.add(modeScreen, "ModeSelection");
        mainPanel.add(nameScreen, "PlayerName");
        mainPanel.add(gamePanel, "Game");
        mainPanel.add(gameOverScreen, "GameOver");

        // Removed: Menu bar from frame

        setContentPane(mainPanel);

        pack();
        setVisible(true);

        mainPanel.requestFocusInWindow();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
}