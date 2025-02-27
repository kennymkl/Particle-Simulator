import java.awt.Graphics;
import java.util.UUID;

public class Ball {
    private String id;
    private double x, y, vx, vy;
    private static final int SIZE = 10; // Ball size

    public Ball(String id, int x, int y, double vx, double vy) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    public Ball(int x, int y, double vx, double vy) {
        this(UUID.randomUUID().toString(), x, y, vx, vy);
    }

    public void update(int canvasWidth, int canvasHeight) {
        x += vx;
        y += vy;

        // Bounce off the edges of the canvas
        if (x <= SIZE / 2 || x >= canvasWidth - SIZE / 2) {
            vx *= -1;
        }
        if (y <= SIZE / 2 || y >= canvasHeight - SIZE / 2) {
            vy *= -1;
        }

        // Ensure the ball stays within the bounds
        x = Math.max(SIZE / 2, Math.min(canvasWidth - SIZE / 2, x));
        y = Math.max(SIZE / 2, Math.min(canvasHeight - SIZE / 2, y));
    }

    public void draw(Graphics g, int canvasHeight, double alpha) {
        int drawX = (int) (x - SIZE / 2);
        int drawY = (int) (y - SIZE / 2);
        g.fillOval(drawX, drawY, SIZE, SIZE);
    }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }
}

