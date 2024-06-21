import java.awt.Graphics;

public class Ball {
    double x, y, vx, vy;

    public Ball(int x, int y, double vx, double vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    void update(int canvasWidth, int canvasHeight) {
        x += vx;
        y += vy;

        if (x <= 5) {
            x = 5;
            vx *= -1;
        } else if (x >= canvasWidth - 5) {
            x = canvasWidth - 5;
            vx *= -1;
        }

        if (y <= 5) {
            y = 5;
            vy *= -1;
        } else if (y >= canvasHeight - 5) {
            y = canvasHeight - 5;
            vy *= -1;
        }
    }

    void draw(Graphics g, int canvasHeight) {
        int invertedY = canvasHeight - (int)y - 5;
        g.fillOval((int)x - 5, invertedY, 10, 10);
    }
}
