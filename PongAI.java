package PONGGAME;

import java.util.Random;

public class PongAI {
    private Random random = new Random();
    private String difficulty;

    public PongAI(String difficulty) {
        this.difficulty = difficulty;
    }

    // Returns true if AI should hit the ball correctly based on win rate
    public boolean shouldHit() {
        double winRate;
        switch (difficulty.toLowerCase()) {
            case "easy": winRate = 0.85; break;
            case "medium": winRate = 0.90; break;
            case "hard": winRate = 0.95; break;
            default: winRate = 0.90; break;
        }
        return random.nextDouble() < winRate;
    }
}