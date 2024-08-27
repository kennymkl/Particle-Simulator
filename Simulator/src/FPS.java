public class FPS {
    private long lastTime;
    private int frames;
    private double fps;

    public FPS() {
        lastTime = System.nanoTime();
        frames = 0;
        fps = 0.0;
    }

    public void update() {
        long currentTime = System.nanoTime();
        frames++;
        if (currentTime - lastTime >= 1_000_000_000) {
            fps = frames;
            frames = 0;
            lastTime += 1_000_000_000;
        }
    }

    public double getFPS() {
        return fps;
    }
}
