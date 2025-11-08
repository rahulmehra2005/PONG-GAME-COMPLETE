package PONGGAME;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements Runnable {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private Paddle paddle1;
    private Paddle paddle2;
    private Ball ball;
    private Score score;

    private boolean isSinglePlayer;
    private String difficulty = "MEDIUM";
    private String player1Name;
    private String player2Name;

    private Thread gameThread;
    private boolean running;
    private boolean paused = false;

    private List<Point> ballTrail;
    private int aiDelay = 0;
    private GameOverScreen gameOverScreen;
    private PongAI pongAI;

    // New: Hit flags for manual hitting
    private boolean player1Hit = false;
    private boolean player2Hit = false;

    // New: Background image - Use fully qualified name to avoid conflict
    private java.awt.image.BufferedImage backgroundImage;

    // New: Timer variables
    private int countdown = 3;
    private boolean showingCountdown = false;
    private long countdownStartTime;

    // New: Menu button and popup
    private JButton menuButton;
    private JPopupMenu popupMenu;

    // New: Power-up related fields
    private List<PowerUp> powerUps;
    private Random random = new Random();
    private long lastPowerUpSpawn = 0;
    private final long POWER_UP_SPAWN_INTERVAL = 15000 + random.nextInt(10000);  // 15-25 seconds

    // New: Effect flags and timers
    private boolean paddleEnlarged = false;
    private Timer enlargeTimer;
    private boolean ballInvisible = false;
    private Timer invisibleTimer;
    private boolean shieldActive = false;
    private Timer shieldTimer;
    private boolean shieldForPlayer1;

    // New: Dynamic obstacles
    private List<Obstacle> obstacles = new ArrayList<>();
    private long lastObstacleSpawn = 0;
    private final long OBSTACLE_SPAWN_INTERVAL = 20000;  // 20 seconds

    public GamePanel(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;

        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        setFocusable(true);

        // New: Top panel for menu button on top left
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        menuButton = new JButton("MENU");
        menuButton.addActionListener(e -> {
            paused = true;  // Auto-pause when menu is clicked
            popupMenu.show(menuButton, 0, menuButton.getHeight());
        });
        menuButton.setBackground(Color.black);
        menuButton.setForeground(Color.white);
        popupMenu = new JPopupMenu();
        // popupMenu.add(pauseItem);  // Commented out as in your code
        JMenuItem resumeItem = new JMenuItem("RESUME");
        resumeItem.addActionListener(e -> {
            paused = false;
            requestFocusInWindow();  // Restore focus
        });
        JMenuItem newGameItem = new JMenuItem("NEW GAME");
        newGameItem.addActionListener(e -> {
            cardLayout.show(mainPanel, "ModeSelection");
            requestFocusInWindow();  // Fix: Restore focus after menu action
        });
        JMenuItem homeItem = new JMenuItem("HOME");
        homeItem.addActionListener(e -> {
            // Fix: Stop theme before navigating to prevent overlapping sounds
            if (GameFrame.themeClip != null && GameFrame.themeClip.isRunning()) {
                GameFrame.themeClip.stop();
                GameFrame.themeClip.close();
            }
            returnToMenu();
            cardLayout.show(mainPanel, "Welcome");
            requestFocusInWindow();  // Fix: Restore focus after menu action
        });
        popupMenu.add(resumeItem);
        popupMenu.add(newGameItem);
        popupMenu.add(homeItem);
        topPanel.add(menuButton, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Load background image from local system
        try {
            backgroundImage = javax.imageio.ImageIO.read(new java.io.File("C:\\Users\\rahul\\Desktop\\College\\NOTES\\matchpong.jpg"));
        } catch (java.io.IOException | IllegalArgumentException e) {
            backgroundImage = null;
        }

        // Fix: Stop theme when game screen is shown
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                if (GameFrame.themeClip != null && GameFrame.themeClip.isRunning()) {
                    GameFrame.themeClip.stop();
                    GameFrame.themeClip.close();
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!running) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        resetGame();
                    }
                } else {
                    // Updated: Controls for rackets - up/down for movement, space/enter for hit
                    if (e.getKeyCode() == KeyEvent.VK_W) paddle1.setMovingUp(true);
                    else if (e.getKeyCode() == KeyEvent.VK_S) paddle1.setMovingDown(true);
                    else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        swingLeftPaddle();
                    }
                    if (!isSinglePlayer) {
                        if (e.getKeyCode() == KeyEvent.VK_UP) paddle2.setMovingUp(true);
                        else if (e.getKeyCode() == KeyEvent.VK_DOWN) paddle2.setMovingDown(true);
                        else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            swingRightPaddle();
                        }
                    }
                    if (e.getKeyCode() == KeyEvent.VK_P) paused = !paused;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_W) paddle1.setMovingUp(false);
                else if (e.getKeyCode() == KeyEvent.VK_S) paddle1.setMovingDown(false);
                if (!isSinglePlayer) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) paddle2.setMovingUp(false);
                    else if (e.getKeyCode() == KeyEvent.VK_DOWN) paddle2.setMovingDown(false);
                    }
                }
            }
        );

        paddle1 = new Paddle(0, 0, Color.BLUE, 75, 130);
        paddle2 = new Paddle(0, 0, Color.RED, 75, 130);

        ball = new Ball(0, 0);
        score = new Score();
        ballTrail = new ArrayList<>();
        powerUps = new ArrayList<>();
    }

    public void setGameOverScreen(GameOverScreen gameOverScreen) {
        this.gameOverScreen = gameOverScreen;
    }

    public void setPlayers(String player1Name, String player2Name, boolean isSinglePlayer, String difficulty) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.isSinglePlayer = isSinglePlayer;
        this.difficulty = difficulty != null ? difficulty.toUpperCase() : "MEDIUM";

        pongAI = new PongAI(difficulty);  // New: Initialize AI

        score.reset();
        resetGame();
        startGame();
        requestFocusInWindow();
    }

    private void startGame() {
        running = true;
        paused = false;
        showingCountdown = true;
        countdown = 3;
        countdownStartTime = System.currentTimeMillis();
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void resetGame() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int paddleHeight = paddle1.getHeight();
        int paddleWidth = paddle1.getWidth();

        paddle1.reset(20, (panelHeight - paddleHeight) / 2);
        paddle2.reset(panelWidth - 20 - paddleWidth, (panelHeight - paddleHeight) / 2);
        ball.reset(panelWidth / 2 - ball.getDiameter() / 2, panelHeight / 2 - ball.getDiameter() / 2);

        ballTrail.clear();
        running = true;
        paused = false;
        aiDelay = 0;
        player1Hit = false;
        player2Hit = false;
        requestFocusInWindow();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1000000000D / 60D;
        double delta = 0;
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;
            while (delta >= 1) {
                if (!paused) update();
                delta--;
            }
            repaint();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        paddle1.update(getHeight());
        paddle2.update(getHeight());  // Always update paddle2
        if (isSinglePlayer) updateAI();

        if (!showingCountdown) {  // Ball only updates if not showing countdown
            ball.update();
        }
        checkCollisions();
        checkScore();

        ballTrail.add(new Point(ball.getX(), ball.getY()));
        if (ballTrail.size() > 10) ballTrail.remove(0);

        // New: Spawn power-ups periodically
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPowerUpSpawn > POWER_UP_SPAWN_INTERVAL) {
            spawnPowerUp();
            lastPowerUpSpawn = currentTime;
        }

        // New: Update power-ups (remove expired ones)
        powerUps.removeIf(powerUp -> currentTime - powerUp.spawnTime > 10000);  // Expire after 10 seconds

        // New: Spawn obstacles periodically
        if (currentTime - lastObstacleSpawn > OBSTACLE_SPAWN_INTERVAL) {
            spawnObstacle();
            lastObstacleSpawn = currentTime;
        }

        // Handle countdown
        if (showingCountdown) {
            long elapsed = System.currentTimeMillis() - countdownStartTime;
            if (elapsed >= 3000) {
                showingCountdown = false;
            }
        }
    }

    private void updateAI() {
        if (aiDelay > 0) {
            aiDelay--;
            return;
        }

        int ballY = ball.getY();
        int paddleY = paddle2.getY();
        int paddleHeight = paddle2.getHeight();
        int panelHeight = getHeight();

        double velocityX = ball.getVelocityX();
        int predictedY = ballY;
        if (velocityX != 0) {
            predictedY = ballY + (int)(ball.getVelocityY() * (paddle2.getX() - ball.getX()) / velocityX);
        }
        predictedY = Math.max(0, Math.min(panelHeight - paddleHeight, predictedY));

        int diff = predictedY - paddleY;
        if (Math.abs(diff) > 5) {
            if (diff > 0) {
                paddle2.setMovingDown(true);
                paddle2.setMovingUp(false);
            } else {
                paddle2.setMovingUp(true);
                paddle2.setMovingDown(false);
            }
        } else {
            paddle2.setMovingUp(false);
            paddle2.setMovingDown(false);
        }

        // AI hits based on win rate
        if (ball.getRect().intersects(paddle2.getRect())) {
            if (pongAI.shouldHit()) {
                swingRightPaddle();
            }
        }

        int baseDelay;
        switch (difficulty.toLowerCase()) {
            case "easy": baseDelay = 25; break;
            case "hard": baseDelay = 5; break;
            default: baseDelay = 15; break;
        }
        int adjustment = (score.getScore1() > score.getScore2()) ? -5 : 5;
        aiDelay = Math.max(2, baseDelay + adjustment);
    }

    private void checkCollisions() {
        if (ball.getRect().intersects(paddle1.getRect()) && player1Hit) {
            ball.reverseX();
            ball.increaseSpeed();
            player1Hit = false;
            playHitSound();  // New: Play custom hit sound
        } else if (ball.getRect().intersects(paddle2.getRect()) && player2Hit) {
            ball.reverseX();
            ball.increaseSpeed();
            player2Hit = false;
            playHitSound();  // New: Play custom hit sound
        }
        if (ball.getY() <= 0 || ball.getY() >= getHeight() - ball.getDiameter()) {
            ball.reverseY();
        }

        // New: Check ball-power-up collisions
        for (PowerUp powerUp : new ArrayList<>(powerUps)) {
            if (ball.getRect().intersects(powerUp.getRect())) {
                activatePowerUp(powerUp);
                powerUps.remove(powerUp);
                break;  // Only one per hit
            }
        }

        // New: Check ball-obstacle collisions
        for (Obstacle obs : new ArrayList<>(obstacles)) {
            if (ball.getRect().intersects(obs.getRect())) {
                ball.reverseY();  // Bounce off
            }
            if (paddle1.getRect().intersects(obs.getRect()) || paddle2.getRect().intersects(obs.getRect())) {
                obstacles.remove(obs);  // Destroy on paddle hit
            }
        }
    }

    // New: Method to spawn a random power-up
    private void spawnPowerUp() {
        int x = random.nextInt(getWidth() - 75) + 25;  // Adjusted for new width
        int y = random.nextInt(getHeight() - 100) + 50;  // Avoid top/bottom edges
        PowerUp.Type type = PowerUp.Type.values()[random.nextInt(PowerUp.Type.values().length)];
        powerUps.add(new PowerUp(x, y, type));
    }

    // New: Method to spawn obstacles
    private void spawnObstacle() {
        int x = random.nextInt(getWidth() - 100) + 50;
        int y = random.nextInt(getHeight() - 200) + 100;
        obstacles.add(new Obstacle(x, y));
    }

    // New: Method to activate power-up based on type
    private void activatePowerUp(PowerUp powerUp) {
        boolean forPlayer1 = ball.getX() < getWidth() / 2;  // Left side for player 1, right for player 2/AI
        switch (powerUp.type) {
            case ENLARGE_PADDLE:
                if (forPlayer1) {
                    paddle1.enlarge();
                    startEnlargeTimer(paddle1);
                } else {
                    paddle2.enlarge();
                    startEnlargeTimer(paddle2);
                }
                break;
            case INVISIBLE_BALL:
                ball.setInvisible(true);
                startInvisibleTimer();
                repaint();  // Fix: Force immediate repaint for visibility
                break;
            case SHIELD:
                if (forPlayer1) {
                    activateShield(true);
                } else {
                    activateShield(false);
                }
                break;
        }
    }

    // New: Timer methods for effects
    // New: Timer methods for effects
    private void startEnlargeTimer(Paddle paddle) {
        if (enlargeTimer != null) enlargeTimer.stop();
        enlargeTimer = new Timer(8000, e -> paddle.resetSize());  // 8 seconds
        enlargeTimer.setRepeats(false);
        enlargeTimer.start();
    }

    private void startInvisibleTimer() {
        if (invisibleTimer != null) invisibleTimer.stop();
        invisibleTimer = new Timer(2000, e -> {
            ball.setInvisible(false);
            repaint();  // Fix: Ensure repaint after effect ends
        });  // 2 seconds
        invisibleTimer.setRepeats(false);
        invisibleTimer.start();
    }

    private void activateShield(boolean forPlayer1) {
        shieldActive = true;
        this.shieldForPlayer1 = forPlayer1;
        if (shieldTimer != null) shieldTimer.stop();
        shieldTimer = new Timer(5000, e -> shieldActive = false);  // 5 seconds
        shieldTimer.setRepeats(false);
        shieldTimer.start();
    }

    private void checkScore() {
        if (ball.getX() < 0) {
            if (shieldActive && !shieldForPlayer1) {
                ball.reverseX();  // Bounce back instead of scoring
            } else {
                score.incrementScore2();
                startCountdown();
            }
        } else if (ball.getX() > getWidth()) {
            if (shieldActive && shieldForPlayer1) {
                ball.reverseX();  // Bounce back instead of scoring
            } else {
                score.incrementScore1();
                startCountdown();
            }
        }
        if (score.getScore1() >= 11 || score.getScore2() >= 11) {
            running = false;
            if (gameOverScreen != null && player1Name != null && player2Name != null) {
                gameOverScreen.setWinner(score.getScore1() >= 11 ? player1Name : player2Name);
            }
            cardLayout.show(mainPanel, "GameOver");
        }
    }

    private void startCountdown() {
        showingCountdown = true;
        countdown = 3;
        countdownStartTime = System.currentTimeMillis();
        resetPositions();
    }

    private void resetPositions() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int paddleHeight = paddle1.getHeight();
        int paddleWidth = paddle1.getWidth();

        paddle1.reset(20, (panelHeight - paddleHeight) / 2);
        paddle2.reset(panelWidth - 20 - paddleWidth, (panelHeight - paddleHeight) / 2);
        ball.reset(panelWidth / 2 - ball.getDiameter() / 2, panelHeight / 2 - ball.getDiameter() / 2);
        ballTrail.clear();
        player1Hit = false;
        player2Hit = false;
    }

    // New: Separate function for left paddle swing
    private void swingLeftPaddle() {
        player1Hit = true;
        paddle1.startSwing(1);  // Counterclockwise (right swing)
    }

    // New: Separate function for right paddle swing
    private void swingRightPaddle() {
        player2Hit = true;
        paddle2.startSwing(-1);  // Clockwise (left swing)
    }

    // New: Method to play hit sound
    private void playHitSound() {
        try {
            javax.sound.sampled.AudioInputStream audioInputStream = javax.sound.sampled.AudioSystem.getAudioInputStream(new java.io.File("C:\\Users\\rahul\\Downloads\\point-smooth-beep-230573.wav"));  // Update path to your sound file
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();  // Play once
        } catch (javax.sound.sampled.UnsupportedAudioFileException | java.io.IOException | javax.sound.sampled.LineUnavailableException e) {
            e.printStackTrace();  // Fallback: If file fails, do nothing
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        paddle1.draw(g2d);
        paddle2.draw(g2d);
        ball.draw(g2d);

        g2d.setFont(new Font("Times New Roman", Font.BOLD, 24));
        g2d.setColor(Color.black);
        g2d.fillRect(0, 827, 250, 50);
        g2d.setColor(Color.white);
        g2d.drawString(player1Name + ": " + score.getScore1(), 10, 860);

        g2d.setColor(Color.BLACK);
        g2d.fillRect(getWidth() - 210, getHeight() - 60, 250, 50);
        g2d.setColor(Color.WHITE);
        g2d.drawString(player2Name + ": " + score.getScore2(), getWidth() - 200, getHeight() - 30);

        g2d.setColor(Color.black);
        g2d.fillRect(getWidth() / 2 - 100, 20, 200, 50);
        g2d.setColor(Color.YELLOW);
        g2d.drawString("TARGET- 11", getWidth() / 2 - 70, 50);

        // New: Draw power-ups
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g2d);
        }

        // New: Draw obstacles
        for (Obstacle obs : obstacles) {
            obs.draw(g2d);
        }

        if (showingCountdown) {
            countdown = 3 - (int)((System.currentTimeMillis() - countdownStartTime) / 1000);
            g2d.setFont(new Font("Times New Roman", Font.BOLD, 170));
            g2d.setColor(Color.ORANGE);
            String count = String.valueOf(Math.max(1, countdown));
            int sw = g2d.getFontMetrics().stringWidth(count);
            g2d.drawString(count, (getWidth() - sw) / 2, getHeight() / 2);
        }

        if (paused) {
            g2d.setFont(new Font("Times New Roman", Font.BOLD, 72));
            g2d.setColor(Color.YELLOW);
            String msg = "PAUSED (PRESS P TO RESUME)";
            int sw = g2d.getFontMetrics().stringWidth(msg);
            g2d.drawString(msg, (getWidth() - sw) / 2, getHeight() / 2);
        }
    }

    public void togglePause() {
        paused = !paused;
    }

    public void returnToMenu() {
        running = false;
        paused = false;
        cardLayout.show(mainPanel, "ModeSelection");
    }
}

// New: PowerUp class
class PowerUp {
    enum Type { ENLARGE_PADDLE, INVISIBLE_BALL, SHIELD }
    int x, y, width = 75, height = 45;  // Updated size: 1.5x original (50x30 -> 75x45)
    Type type;
    long spawnTime;

    PowerUp(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.spawnTime = System.currentTimeMillis();
    }

    Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }

    void draw(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(x, y, width, height);  // Always circle for all power-ups
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Times New Roman", Font.BOLD, 12));
        String label = type == Type.ENLARGE_PADDLE ? "ENLARGE" : type == Type.INVISIBLE_BALL ? "INVISIBLE" : "SHIELD";
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(label)) / 2;
        int textY = y + (height + fm.getAscent()) / 2;
        g2d.drawString(label, textX, textY);
    }
}

// New: Obstacle class
class Obstacle {
    int x, y, width = 50, height = 20;

    Obstacle(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }

    void draw(Graphics2D g2d) {
        g2d.setColor(Color.orange);
        g2d.fillRect(x, y, width, height);
    }
}
