import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class ExplorerClient extends JFrame {
    private ExplorerPanel explorerPanel;
    private PrintWriter out;
    private final ConcurrentHashMap<String, Explorer> explorers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Ball> particles = new ConcurrentHashMap<>();
    private String controlledExplorerId;

    public ExplorerClient() {
        setupUI();
        connectToServer();
    }

    private void setupUI() {
        setTitle("Explorer Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        explorerPanel = new ExplorerPanel(explorers, particles, this);
        add(explorerPanel);
        setPreferredSize(new Dimension(1280, 720));
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 12345);        // <-------------- PUT IPV4 address here to connect to host instead of localhost
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                explorerPanel.setOut(out);

                String message;
                while ((message = in.readLine()) != null) {
                    handleServerMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleServerMessage(String message) {
        System.out.println("Received message: " + message);

        String[] parts = message.split(": ");
        if (parts.length == 2) {
            if (parts[0].equals("CONTROLLED_EXPLORER")) {
                controlledExplorerId = parts[1];
            } else {
                String[] data = parts[1].split(", ");
                if (data.length == 3 && parts[0].equals("EXPLORER")) {
                    String id = data[0];
                    double x = Double.parseDouble(data[1]);
                    double y = Double.parseDouble(data[2]);
                    explorers.put(id, new Explorer(id, x, y));
                } else if (data.length == 5) {
                    String id = data[0];
                    double x = Double.parseDouble(data[1]);
                    double y = Double.parseDouble(data[2]);
                    double vx = Double.parseDouble(data[3]);
                    double vy = Double.parseDouble(data[4]);
                    particles.put(id, new Ball(id, (int) x, (int) y, vx, vy));
                }
            }
        }
        explorerPanel.repaint();
    }

    public Explorer getControlledExplorer() {
        return explorers.get(controlledExplorerId);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            ExplorerClient clientFrame = new ExplorerClient();
            clientFrame.setVisible(true);
        });
    }
}


class ExplorerPanel extends JPanel {
    private final ConcurrentHashMap<String, Explorer> explorers;
    private final ConcurrentHashMap<String, Ball> particles;
    private BufferedImage bufferImage;
    private PrintWriter out;
    private final ExplorerClient client;
    private FPS fpsTracker;
    private long lastUpdateTime = System.currentTimeMillis();
    private ScheduledExecutorService executor;

    public ExplorerPanel(ConcurrentHashMap<String, Explorer> explorers, ConcurrentHashMap<String, Ball> particles, ExplorerClient client) {
        this.explorers = explorers;
        this.particles = particles;
        this.client = client;
        this.fpsTracker = new FPS();
        setFocusable(true);
        setPreferredSize(new Dimension(1280, 720));
        startRenderLoop();
    }

    private void startRenderLoop() {
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this::updateAndRepaint, 0, 16, TimeUnit.MILLISECONDS);
    }

    private void updateAndRepaint() {
        updateLogic();
        repaint();
    }

    private void updateLogic() {
        long currentTime = System.currentTimeMillis();
        lastUpdateTime = currentTime;

        for (Explorer explorer : explorers.values()) {
            explorer.update(getWidth(), getHeight());
        }
        for (Ball particle : particles.values()) {
            particle.update(getWidth(), getHeight());
        }

        fpsTracker.update();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bufferImage == null || bufferImage.getWidth() != getWidth() || bufferImage.getHeight() != getHeight()) {
            bufferImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }
        Graphics2D g2d = bufferImage.createGraphics();

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight()); // Clear the screen

        renderExplorersAndParticles(g2d);
        drawHUD(g2d);

        g.drawImage(bufferImage, 0, 0, this);
        g2d.dispose();
    }

    private void renderExplorersAndParticles(Graphics2D g2d) {
        Explorer mainExplorer = client.getControlledExplorer();

        if (mainExplorer != null) {
            setupViewTransform(g2d, mainExplorer);
            drawParticlesInView(g2d);
            drawExplorersInView(g2d);
            resetViewTransform(g2d);
        }
    }

    private void setupViewTransform(Graphics2D g2d, Explorer mainExplorer) {
        int explorerX = (int) mainExplorer.getX();
        int explorerY = (int) mainExplorer.getY();
        double scale = 2.0;
        int translateX = (int) ((getWidth() / scale) / 2 - explorerX);
        int translateY = (int) ((getHeight() / scale) / 2 - explorerY);

        g2d.scale(scale, scale);
        g2d.translate(translateX, translateY);
    }

    private void resetViewTransform(Graphics2D g2d) {
        g2d.setTransform(new AffineTransform());
    }

    private void drawParticlesInView(Graphics2D g2d) {
        Color saiyanBlue = new Color(173, 216, 230, 255);
        g2d.setColor(saiyanBlue);
        for (Ball particle : particles.values()) {
            particle.draw(g2d, getHeight(), 1.0);
        }
    }

    private void drawExplorersInView(Graphics2D g2d) {
        for (Explorer explorer : explorers.values()) {
            explorer.draw(g2d);
        }
    }

    private void drawHUD(Graphics2D g2d) {
        Color saiyanBlue = new Color(173, 216, 230, 255);
        g2d.setColor(saiyanBlue);
        g2d.setFont(new Font("Tahoma", Font.BOLD, 14));
        
        // Mode display
        String modeText = "Mode: Explorer";
        g2d.drawString(modeText, 10, 20); // Positioned at top left
    
    
    
        // Movement keys info
        g2d.drawString("Move with Arrow Keys", getWidth() / 2 - 110, getHeight() - 20); // Bottom center
    
        int yOffset = getHeight() - 30;
    
        // FPS display
        if (fpsTracker.getFPS() >= 60) {
            g2d.setColor(Color.GREEN);
        } else if (fpsTracker.getFPS() >= 50) {
            g2d.setColor(Color.ORANGE);
        } else {
            g2d.setColor(Color.RED);
        }
        String fpsText = String.format("FPS: %.2f", (float) fpsTracker.getFPS()); // Casting to float ensures correct format
        g2d.drawString(fpsText, 10, yOffset);
    
        // Particles display
        String particlesText = String.format("Particles: %d", particles.size());
        g2d.setColor(Color.BLACK);
        g2d.drawString(particlesText, 151, yOffset + 1);
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString(particlesText, 150, yOffset);
    }
    

    public void setOut(PrintWriter out) {
        this.out = out;
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        });
    }

    private void handleKeyPress(KeyEvent e) {
        if (out != null) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    out.println("MOVE_UP");
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    out.println("MOVE_DOWN");
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    out.println("MOVE_LEFT");
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    out.println("MOVE_RIGHT");
                    break;
            }
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        if (out != null) {
            out.println("STOP_MOVE");
        }
    }
}