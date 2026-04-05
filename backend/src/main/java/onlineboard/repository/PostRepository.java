package onlineboard.repository;

import onlineboard.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class PostRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostRepository.class);
    private SQLiteConnection connection;

    public PostRepository(SQLiteConnection connection) {
        this.connection = connection;
        createTable();
    }

    public synchronized void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS posts ("
                + "postId VARCHAR(255) PRIMARY KEY," 
                + "threadId VARCHAR(255) NOT NULL," 
                + "email VARCHAR(255) NOT NULL,"
                + "nickname VARCHAR(255) NOT NULL,"
                + "content VARCHAR(255) NOT NULL,"
                + "createdAt VARCHAR(255) NOT NULL DEFAULT NOW,"
                + "FOREIGN KEY (threadId) REFERENCES threads(threadId),"
                + "FOREIGN KEY (email) REFERENCES users(email)" 
                + ")";
        try {
            connection.connect();
            connection.executeUpdate(query);
        } catch (SQLException e) {
            logger.error("Error creating posts table: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating posts table", e);
        } finally {
            connection.disconnect();
        }
    }

    //ポストを作成
    public synchronized void createPost(Post post) {
        logger.info("Creating post: {}", post);

        String query = "INSERT INTO posts (postId, threadId, email, nickname, content, createdAt) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            connection.connect();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String createdAtString = post.getCreatedAt().format(formatter);
            connection.executeUpdate(query, post.getPostId(), post.getThreadId(), post.getEmail(), post.getNickname(), post.getContent(),
                    createdAtString);
            logger.info("Post created successfully: {}", post);
        } catch (SQLException e) {
            logger.error("Error creating post: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating post", e);
        } finally {
            connection.disconnect();
        }
    }

    //ポストの一覧を取得
    public synchronized List<Post> getPostsByThreadId(String threadId) {
        logger.info("Fetching posts for thread {}", threadId);

        String query = "SELECT * FROM posts WHERE threadId = ?";
        List<Post> posts = new ArrayList<>();

        try {
            connection.connect();
            ResultSet resultSet = connection.executeQuery(query, threadId);

            while (resultSet.next()) {
                String postId = resultSet.getString("postId");
                String email = resultSet.getString("email");
                String nickname = resultSet.getString("nickname");
                String content = resultSet.getString("content");
                String createdAtString = resultSet.getString("createdAt");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime createdAt = LocalDateTime.parse(createdAtString, formatter);
                Post post = new Post(postId, threadId, email, nickname, content, createdAt);
                posts.add(post);
            }

            resultSet.close();
            logger.info("Fetched {} posts for thread {}", posts.size(), threadId);
        } catch (SQLException e) {
            logger.error("Error fetching posts for thread {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Error fetching posts for thread " + threadId, e);
        } finally {
            connection.disconnect();
        }

        return posts;
    }

    //指定したポストの取得
    public synchronized Post getPostById(String postId) {
        logger.info("Fetching post with id: {}", postId);

        String query = "SELECT * FROM posts WHERE postId = ?";

        try {
            connection.connect();
            ResultSet resultSet = connection.executeQuery(query, postId);

            if (resultSet.next()) {
                String threadId = resultSet.getString("threadId");
                String email = resultSet.getString("email");
                String nickname = resultSet.getString("nickname");
                String content = resultSet.getString("content");
                String createdAtString = resultSet.getString("createdAt");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime createdAt = LocalDateTime.parse(createdAtString, formatter);
                return new Post(postId, threadId, email, nickname, content, createdAt);
            }

            resultSet.close();
        } catch (SQLException e) {
            logger.error("Error fetching post with id {}: {}", postId, e.getMessage(), e);
            throw new RuntimeException("Error fetching post with id " + postId, e);
        } finally {
            connection.disconnect();
        }

        logger.warn("Post not found with id: {}", postId);
        return null;
    }

    //ポストの削除
    public synchronized void deletePost(String postId) {
        logger.info("Deleting post {}", postId);

        String query = "DELETE FROM posts WHERE postId = ?";

        try {
            connection.connect();
            connection.executeUpdate(query, postId);
            logger.info("Post {} deleted successfully", postId);
        } catch (SQLException e) {
            logger.error("Error deleting post {}: {}", postId, e.getMessage(), e);
            throw new RuntimeException("Error deleting post " + postId, e);
        } finally {
            connection.disconnect();
        }
    }
}