package onlineboard.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import onlineboard.service.*;
import onlineboard.model.*;
import onlineboard.util.LocalDateTimeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UserService userService;
    private ThreadService threadService;
    private PostService postService;
    private User currentUser;
    private BoardServer server;

    public ClientHandler(Socket socket, UserService userService, ThreadService threadService,
            PostService postService, BoardServer server) {
        this.socket = socket;
        this.userService = userService;
        this.threadService = threadService;
        this.postService = postService;
        this.server = server;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            logger.error("Error creating client handler: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating client handler", e);
        }
    }

    @Override
    public void run() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                handleRequest(inputLine);
            }
        } catch (IOException e) {
            logger.warn("Client disconnected unexpectedly: {}", e.getMessage(), e);
        } finally {
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Error closing client socket: {}", e.getMessage(), e);
            }
        }
    }

    private void handleRequest(String request) {
        logger.info("Received request: {}", request);
        try {
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> requestMap = gson.fromJson(request, type);
            String command = requestMap.get("command");
            String data = requestMap.get("data");

            switch (command) {
                case "REGISTER":
                    handleRegister(data);
                    break;
                case "LOGIN":
                    handleLogin(data);
                    break;
                case "LOGOUT":
                    handleLogout();
                    break;
                case "CREATE_THREAD":
                    handleCreateThread(data);
                    break;
                case "GET_THREADS":
                    handleGetThreads();
                    break;
                case "GET_POSTS":
                    handleGetPosts(data);
                    break;
                case "CREATE_POST":
                    handleCreatePost(data);
                    break;
                case "DELETE_THREAD":
                    handleDeleteThread(data);
                    break;
                case "DELETE_POST":
                    handleDeletePost(data);
                    break;
                default:
                    sendMessage(
                            gson.toJson(Map.of(
                                    "status", "ERROR",
                                    "message", "Unknown command: " + command)));
            }
        } catch (Exception e) {
            logger.error("Error handling request: {}", e.getMessage(), e);
            sendMessage(
                    gson.toJson(Map.of(
                            "status", "ERROR",
                            "message", "Invalid request format: " + e.getMessage())));
        }
    }

    public void sendMessage(String message) {
        logger.info("Sending response: {}", message);
        out.println(message);
    }

    private void handleRegister(String data) {
        try {
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> userData = gson.fromJson(data, type);
            String email = userData.get("email");
            String password = userData.get("password");
            String nickname = userData.get("nickname");

            User user = userService.register(email, password, nickname);
            sendMessage(gson.toJson(Map.of("status", "OK", "user", user)));
        } catch (Exception e) {
            sendMessage(gson.toJson(Map.of("status", "ERROR", "message", e.getMessage())));
        }
    }

    private void handleLogin(String data) {
        try {
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> credentials = gson.fromJson(data, type);
            String email = credentials.get("email");
            String password = credentials.get("password");

            User user = userService.login(email, password);
            currentUser = user;
            sendMessage(gson.toJson(Map.of("status", "OK", "user", user)));
        } catch (Exception e) {
            sendMessage(gson.toJson(Map.of("status", "ERROR", "message", e.getMessage())));
        }
    }

    private void handleLogout() {
        userService.logout(currentUser);
        server.removeClient(this);
    }

    private void handleCreateThread(String data) {
        try {
            if (currentUser == null) {
                throw new RuntimeException("Not logged in");
            }

            String title = gson.fromJson(data, String.class);
            onlineboard.model.Thread newThread = threadService.createThread(currentUser, title);

            sendMessage(gson.toJson(Map.of("status", "OK", "thread", newThread)));

            server.notifyClients(gson.toJson(Map.of("event", "NEW_THREAD", "thread", newThread)));
        } catch (Exception e) {
            sendMessage(gson.toJson(Map.of("status", "ERROR", "message", e.getMessage())));
        }
    }

    private void handleGetThreads() {
        try {
            List<onlineboard.model.Thread> threads = threadService.getThreads();
            sendMessage(gson.toJson(Map.of("status", "OK", "threads", threads)));
        } catch (Exception e) {
            sendMessage(gson.toJson(Map.of("status", "ERROR", "message", e.getMessage())));
        }
    }

    private void handleGetPosts(String data) {
        try {
            String threadId = gson.fromJson(data, String.class);
            List<Post> posts = postService.getPosts(threadId);
            sendMessage(gson.toJson(Map.of("status", "OK", "posts", posts)));
        } catch (Exception e) {
            sendMessage(gson.toJson(Map.of("status", "ERROR", "message", e.getMessage())));
        }
    }

    private void handleCreatePost(String data) {
        try {
            if (currentUser == null) {
                throw new RuntimeException("Not logged in");
            }

            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> postData = gson.fromJson(data, type);
            String threadId = postData.get("threadId");
            String content = postData.get("content");

            Post newPost = postService.createPost(threadId, currentUser, content);
            onlineboard.model.Thread thread = threadService.getThreadById(threadId);

            sendMessage(gson.toJson(Map.of("status", "OK", "post", newPost)));

            server.notifyClients(gson.toJson(Map.of(
                    "event", "NEW_POST",
                    "threadId", threadId,
                    "threadTitle", thread.getTitle(),
                    "post", newPost)));
        } catch (Exception e) {
            sendMessage(gson.toJson(Map.of("status", "ERROR", "message", e.getMessage())));
        }
    }

    private void handleDeleteThread(String data) {
        try {
            if (currentUser == null) {
                throw new RuntimeException("Not logged in");
            }

            String threadId = gson.fromJson(data, String.class);
            onlineboard.model.Thread thread = threadService.getThreadById(threadId);
            threadService.deleteThread(threadId);

            sendMessage(gson.toJson(Map.of("status", "OK")));

            server.notifyClients(gson.toJson(Map.of("event", "DELETE_THREAD", "threadId", threadId, "threadTitle", thread.getTitle())));
        } catch (Exception e) {
            sendMessage(gson.toJson(Map.of("status", "ERROR", "message", e.getMessage())));
        }
    }

    private void handleDeletePost(String data) {
        try {
            if (currentUser == null) {
                throw new RuntimeException("Not logged in");
            }

            String postId = gson.fromJson(data, String.class);
            Post post = postService.getPostById(postId);
            onlineboard.model.Thread thread = threadService.getThreadById(post.getThreadId());
            postService.deletePost(postId);

            sendMessage(gson.toJson(Map.of("status", "OK")));

            server.notifyClients(gson.toJson(Map.of("event", "DELETE_POST", "postId", postId, "threadId", post.getThreadId(), "threadTitle", thread.getTitle())));
        } catch (Exception e) {
            sendMessage(gson.toJson(Map.of("status", "ERROR", "message", e.getMessage())));
        }
    }
}