package onlineboard.server;

import onlineboard.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class BoardServer {
    private static final Logger logger = LoggerFactory.getLogger(BoardServer.class);

    private int port;
    private UserService userService;
    private ThreadService threadService;
    private PostService postService;
    private final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    public BoardServer(int port, UserService userService, ThreadService threadService, PostService postService) {
        this.port = port;
        this.userService = userService;
        this.threadService = threadService;
        this.postService = postService;
    }

    public void notifyClients(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started on port {}", port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("New client connected: {}", clientSocket);
                ClientHandler clientHandler = new ClientHandler(clientSocket, userService, threadService, postService, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        }
    }
}