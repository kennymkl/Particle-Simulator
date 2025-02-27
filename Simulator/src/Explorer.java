import java.awt.*;

public class Explorer {
    private String id;
    private double x, y;
    private double vx, vy;
    private static final int SIZE = 10;

    public Explorer(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
    }

    public void update(int canvasWidth, int canvasHeight) {
        x += vx;
        y += vy;

        if (x < 0) x = 0;
        if (x > canvasWidth - SIZE) x = canvasWidth - SIZE;
        if (y < 0) y = 0;
        if (y > canvasHeight - SIZE) y = canvasHeight - SIZE;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillOval((int) x, (int) y, SIZE, SIZE);
    }

    public void setVelocity(int vx, int vy) {
        this.vx = vx;
        this.vy = vy;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
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
}
