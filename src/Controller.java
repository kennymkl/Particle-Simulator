import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Controller {
    private List<Ball> particles = new CopyOnWriteArrayList<>();

    public void addParticle(int x, int y, double angle, double velocity) {
        particles.add(new Ball(x, y, Math.cos(angle) * velocity, Math.sin(angle) * velocity));
    }

    public void addParticle(Ball particle) {
        particles.add(particle);
    }

    public void updateParticles(int canvasWidth, int canvasHeight) {
        for (Ball particle : particles) {
            particle.update(canvasWidth, canvasHeight);
        }
    }

    public List<Ball> getParticles(){
        return particles;
    }

    public void drawParticles(Graphics g, int canvasHeight) {
        for (Ball particle : particles) {
            particle.draw(g, canvasHeight);
        }
    }
    public void clearParticles() {
        particles.clear();
    }
}
