import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.*;

public class Main extends JFrame {
    private static final int PORT = 12345;
    private static ConcurrentLinkedQueue<ClientHandler> clients = new ConcurrentLinkedQueue<>();
    private MainScreenUI simulationPanel;
    private Map<String, String> lastExplorerState = new HashMap<>();
    private Map<String, String> lastParticleState = new HashMap<>();

    public Main() {
        simulationPanel = new MainScreenUI();
        setupUI();
        startServer();
        startBroadcaster();
    }

    private void setupUI() {
        setTitle("Particle Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(simulationPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

        InputSelection inputSelection = new InputSelection(simulationPanel.getDynamicThreadManager());
        inputSelection.setVisible(true);
    }

    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server started on port " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket, clients, simulationPanel);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();

                    // Send the state of all existing explorers to the new client
                    for (ClientHandler client : clients) {
                        Explorer explorer = client.getExplorer();
                        clientHandler.sendMessage("EXPLORER: " + explorer.getId() + ", " + explorer.getX() + ", " + explorer.getY());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startBroadcaster() {
        new Thread(() -> {
            while (true) {
                List<String> particleMessages = getDeltaParticleData();
                List<String> explorerMessages = getDeltaExplorerData();

                for (String particleMessage : particleMessages) {
                    broadcast(particleMessage);
                }

                for (String explorerMessage : explorerMessages) {
                    broadcast(explorerMessage);
                }

                try {
                    Thread.sleep(10); // Reduce to 10ms or an appropriate value
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private List<String> getDeltaExplorerData() {
        List<String> deltaMessages = new ArrayList<>();
        for (ClientHandler client : clients) {
            Explorer explorer = client.getExplorer();
            String newState = "EXPLORER: " + explorer.getId() + ", " + explorer.getX() + ", " + explorer.getY();
            if (!newState.equals(lastExplorerState.get(explorer.getId()))) {
                deltaMessages.add(newState);
                lastExplorerState.put(explorer.getId(), newState);
            }
        }
        return deltaMessages;
    }

    private List<String> getDeltaParticleData() {
        List<String> deltaMessages = new ArrayList<>();
        for (Ball particle : simulationPanel.getDynamicThreadManager().getParticles()) {
            String newState = "PARTICLE: " + particle.getId() + ", " + particle.getX() + ", " + particle.getY() + ", " + particle.getVx() + ", " + particle.getVy();
            if (!newState.equals(lastParticleState.get(particle.getId()))) {
                deltaMessages.add(newState);
                lastParticleState.put(particle.getId(), newState);
            }
        }
        return deltaMessages;
    }

    private void broadcast(String message) {
        System.out.println("Broadcasting message: " + message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(Main::new);
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private ConcurrentLinkedQueue<ClientHandler> clients;
    private PrintWriter out;
    private BufferedReader in;
    private Explorer explorer;
    private MainScreenUI simulationPanel;

    public ClientHandler(Socket socket, ConcurrentLinkedQueue<ClientHandler> clients, MainScreenUI simulationPanel) {
        this.socket = socket;
        this.clients = clients;
        this.simulationPanel = simulationPanel;
        String id = UUID.randomUUID().toString();
        this.explorer = new Explorer(id, 640, 360);
        simulationPanel.addExplorer(explorer);
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            sendControlledExplorerId();

            String message;
            while ((message = in.readLine()) != null) {
                final String msg = message; // Make message final for lambda
                new Thread(() -> handleClientMessage(msg)).start(); // Process message asynchronously
                broadcast("EXPLORER: " + explorer.getId() + ", " + explorer.getX() + ", " + explorer.getY());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clients.remove(this);
        }
    }

    private void handleClientMessage(String message) {
        switch (message) {
            case "MOVE_UP":
                explorer.setVelocity(0, -5);
                break;
            case "MOVE_DOWN":
                explorer.setVelocity(0, 5);
                break;
            case "MOVE_LEFT":
                explorer.setVelocity(-5, 0);
                break;
            case "MOVE_RIGHT":
                explorer.setVelocity(5, 0);
                break;
            case "STOP_MOVE":
                explorer.setVelocity(0, 0);
                break;
        }
    }

    private void sendControlledExplorerId() {
        out.println("CONTROLLED_EXPLORER: " + explorer.getId());
    }

    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public Explorer getExplorer() {
        return explorer;
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
