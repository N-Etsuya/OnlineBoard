package onlineboard;

import onlineboard.server.BoardServer;
import onlineboard.repository.*;
import onlineboard.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SQLiteConnection connection = new SQLiteConnection("database.db");

        UserRepository userRepository = new UserRepository(connection); 
        ThreadRepository threadRepository = new ThreadRepository(connection);
        PostRepository postRepository = new PostRepository(connection);   
        UserService userService = new UserService(userRepository);
        ThreadService threadService = new ThreadService(threadRepository);
        PostService postService = new PostService(postRepository);
        BoardServer server = new BoardServer(8000, userService, threadService, postService);

        try {
            server.start();
        } catch (Exception e) {
            logger.error("Server error: {}", e.getMessage(), e);
        }
    }
}