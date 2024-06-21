import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

public class MainScreenUI extends JPanel {
    private final DrawPanel drawPanel;
    private final ThreadController threadManager; 
    private Thread gameThread;
    private volatile boolean running = false;
    private FPS trackFPS = new FPS(); 

        public MainScreenUI() {
        drawPanel = new DrawPanel();

        setLayout(new BorderLayout());
        add(drawPanel, BorderLayout.CENTER);
        threadManager = new ThreadController();

        drawPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                drawPanel.removeComponentListener(this);
                startGameLoop();
            }
        });
    }

    public void startGameLoop() {
        threadManager.setCanvasSize(drawPanel.getWidth(), drawPanel.getHeight());
        running = true;
        gameThread = new Thread(this::gameLoop);
        gameThread.start();
    }

    private void gameLoop() {
        final long targetDelay = 1000 / 60; 
        long lastFpsDisplayTime = System.currentTimeMillis(); 

        while (running) {
            long now = System.currentTimeMillis();
            
            trackFPS.update(); 
            if (now - lastFpsDisplayTime >= 500 && trackFPS.getFPS() != 0) {
                drawPanel.setFps(trackFPS.getFPS()); 
                threadManager.checkAndAdjustThread(); 
                lastFpsDisplayTime = now;
            }

            updateAndRepaint(); 
            threadManager.updateProcessingTimes();

            long sleepTime = targetDelay - (System.currentTimeMillis() - now);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    running = false;
                }
            }
        }
    }

    private void updateAndRepaint() {
        threadManager.updateParticles(); 
        SwingUtilities.invokeLater(drawPanel::repaint);
    }

    public void stopGameLoop() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public ThreadController getDynamicThreadManager() {
        return threadManager;
    }

    private class DrawPanel extends JPanel {
        private double fpsToDisplay = 0;

        public void setFps(double fps) {
            this.fpsToDisplay = fps;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1280, 720);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public DrawPanel() {
            super();
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.BLACK);
            
            Color saiyanBlue = new Color(173,216,230,255);

            g.setColor(saiyanBlue);
            threadManager.drawParticles(g, getHeight());
            Font counterFont = new Font("Tahoma", Font.BOLD, 14);
            g.setFont(counterFont);
            
            int yOffset = getHeight() - 30;
            if (fpsToDisplay >= 60){
                g.setColor(Color.GREEN);
            } else if (fpsToDisplay >= 50){
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.RED);
            }
            g.drawString(String.format("FPS: %.2f", fpsToDisplay), 10, yOffset);
        
           
           String particlesText = String.format("Particles: %d", threadManager.getParticleSize());
            g.setColor(Color.BLACK);
            g.drawString(particlesText, 151, yOffset + 1);
            g.setColor(new Color(255, 215, 0)); 
            g.drawString(particlesText, 150, yOffset);
        }
    }
}