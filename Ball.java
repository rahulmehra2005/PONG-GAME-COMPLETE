package PONGGAME;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Ball {
    private int x, y, diameter = 20;
    private double velocityX = 6, velocityY = 6;
    private double initialVelocityX = 6, initialVelocityY = 6;

    // New: Invisible effect
    private boolean invisible = false;

    // New: Ball trail for particle effects
    private List<Point> trail = new ArrayList<>();

    public Ball(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        // Update trail for particle effects
        trail.add(new Point(x, y));
        if (trail.size() > 15) trail.remove(0);

        x += velocityX;
        y += velocityY;
    }

    public void draw(Graphics2D g2d) {
        // Draw trail particles only if not invisible
        if (!invisible) {
            for (int i = 0; i < trail.size(); i++) {
                Point p = trail.get(i);
                float alpha = 1.0f - (i / (float)trail.size());
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(Color.CYAN);
                g2d.fillOval(p.x, p.y, diameter, diameter);
            }
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));  // Reset
        }

        // Draw ball with invisible effect
        if (invisible) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.0f));  // Fully invisible
        }
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x, y, diameter, diameter);
        if (invisible) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));  // Reset
        }
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, diameter, diameter);
    }

    public void reverseX() {
        velocityX = -velocityX;
    }

    public void reverseY() {
        velocityY = -velocityY;
    }

    public void increaseSpeed() {
        velocityX *= 1.1;
        velocityY *= 1.1;
    }

    public void reset(int x, int y) {
        this.x = x;
        this.y = y;
        velocityX = initialVelocityX * (Math.random() > 0.5 ? 1 : -1);
        velocityY = initialVelocityY * (Math.random() > 0.5 ? 1 : -1);
        trail.clear();  // Clear trail on reset
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public int getDiameter() {
        return diameter;
    }

    public List<Point> getTrail() {
        return trail;
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }
}