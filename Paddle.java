package PONGGAME;

import java.awt.*;

public class Paddle {
    private int x, y, width, height;
    private Color color;
    private boolean movingUp, movingDown;
    private int speed = 8;

    private double swingAngle = 0;
    private double swingSpeed = 0.5;  // Increased for more visible swing
    private boolean isSwinging = false;
    private int swingDirection = -1;
    private int initialDirection = -1;
    private boolean goingOut = true;
    private double maxAngle = Math.PI / 2;  // 90 degrees

    public Paddle(int x, int y, Color color, int width, int height) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.width = width;
        this.height = height;
    }

    public void update(int panelHeight) {
        if (movingUp && y > 0) y -= speed;
        if (movingDown && y < panelHeight - height) y += speed;

        if (isSwinging) {
            swingAngle += swingSpeed * swingDirection;
            if (goingOut && Math.abs(swingAngle) >= maxAngle) {
                goingOut = false;
                swingDirection = -swingDirection;
            } else if (!goingOut && swingAngle * swingDirection <= 0) {
                swingAngle = 0;
                isSwinging = false;
                goingOut = true;
                swingDirection = initialDirection;
            }
        }
    }

    public void draw(Graphics2D g2d) {
        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        if (isSwinging) {
            g2dCopy.rotate(swingAngle, x + width / 2, y + height / 2);
        }

        g2dCopy.setColor(color);
        
        g2dCopy.fillOval(x, y, width, height - 30);
        
        g2dCopy.fillRoundRect(x + width / 2 - 3, y + height - 30, 6, 25, 3, 3);
        
        g2dCopy.setColor(Color.BLACK);
        for (int i = 0; i < 5; i++) {
            g2dCopy.drawLine(x + width / 2 - 2, y + height - 25 + i * 4, x + width / 2 + 2, y + height - 25 + i * 4);
        }
        
        g2dCopy.setColor(color);
        g2dCopy.dispose();
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }

    public void reset(int x, int y) {
        this.x = x;
        this.y = y;
        swingAngle = 0;
        isSwinging = false;
        goingOut = true;
        swingDirection = initialDirection;
    }

    public void startSwing(int direction) {
        if (!isSwinging) {
            isSwinging = true;
            swingAngle = 0;
            swingDirection = direction;
            initialDirection = direction;
            goingOut = true;
        }
    }

    public void enlarge() {
        width = (int) (width * 1.5);  // Increase width by 50%
    }
    public void resetSize() {
        width = 75;  // Reset to original width (adjust if your default differs)
    }


    public int getX() { return x; }
    public int getY() { return y; }
    public int getHeight() { return height; }
    public int getWidth() { return width; }
    public void setMovingUp(boolean movingUp) { this.movingUp = movingUp; }
    public void setMovingDown(boolean movingDown) { this.movingDown = movingDown; }
}