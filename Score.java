package PONGGAME;

public class Score {
    private int score1 = 0, score2 = 0;

    public void incrementScore1() {
        score1++;
    }

    public void incrementScore2() {
        score2++;
    }

    public int getScore1() {
        return score1;
    }

    public int getScore2() {
        return score2;
    }

    public void reset() {
        score1 = 0;
        score2 = 0;
    }
}