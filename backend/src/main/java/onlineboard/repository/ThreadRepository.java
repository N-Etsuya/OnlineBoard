package onlineboard.repository;

import onlineboard.model.Thread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ThreadRepository {
    private static final Logger logger = LoggerFactory.getLogger(ThreadRepository.class);

    private SQLiteConnection connection;

    public ThreadRepository(SQLiteConnection connection) {
        this.connection = connection;
        createTable();
    }

    public synchronized void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS threads ("
                + "threadId VARSHAR(255) PRIMARY KEY,"
                + "title VARCHAR(255) NOT NULL,"
                + "createdAt VARSHAR(255) NOT NULL DEFAULT NOW"
                + ")";
        try {
            connection.connect();
            connection.executeUpdate(query);
        } catch (SQLException e) {
            logger.error("Error creating threads table: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating threads table", e);
        } finally {
            connection.disconnect();
        }
    }

    //スレッドの作成
    public synchronized void createThread(Thread thread) {
        logger.info("Creating thread: {}", thread);

        String query = "INSERT INTO threads (threadId, title, createdAt) VALUES (?, ?, ?)";

        try {
            connection.connect();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String createdAtString = thread.getCreatedAt().format(formatter);
            connection.executeUpdate(query, thread.getThreadId(), thread.getTitle(), createdAtString);
            logger.info("Thread created successfully: {}", thread);
        } catch (SQLException e) {
            logger.error("Error creating thread: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating thread", e);
        } finally {
            connection.disconnect();
        }
    }

    //スレッドの一覧を取得
    public synchronized List<Thread> getAllThreads() {
        logger.info("Fetching all threads");

        String query = "SELECT * FROM threads";
        List<Thread> threads = new ArrayList<>();

        try {
            connection.connect();
            ResultSet resultSet = connection.executeQuery(query);

            while (resultSet.next()) {
                String threadId = resultSet.getString("threadId");
                String title = resultSet.getString("title");
                String createdAtString = resultSet.getString("createdAt");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime createdAt = LocalDateTime.parse(createdAtString, formatter);
                Thread thread = new Thread(threadId, title, createdAt);
                threads.add(thread);
            }
            logger.info("Fetched {} threads", threads.size());

            resultSet.close();
        } catch (SQLException e) {
            logger.error("Error fetching all threads: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching all threads", e);
        } finally {
            connection.disconnect();
        }

        return threads;
    }

    //指定したスレッドを取得
    public synchronized Thread getThreadById(String threadId) {
        logger.info("Fetching thread with id: {}", threadId);

        String query = "SELECT * FROM threads WHERE threadId = ?";

        try {
            connection.connect();
            ResultSet resultSet = connection.executeQuery(query, threadId);

            if (resultSet.next()) {
                String title = resultSet.getString("title");
                String createdAtString = resultSet.getString("createdAt");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime createdAt = LocalDateTime.parse(createdAtString, formatter);
                return new Thread(threadId, title, createdAt);
            }

            resultSet.close();
        } catch (SQLException e) {
            logger.error("Error fetching thread with id {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Error fetching thread with id " + threadId, e);
        } finally {
            connection.disconnect();
        }

        logger.warn("Thread not found with id: {}", threadId);
        return null;
    }

    //スレッドを削除
    public synchronized void deleteThread(String threadId) {
        logger.info("Deleting thread {}", threadId);

        String query = "DELETE FROM threads WHERE threadId = ?";

        try {
            connection.connect();
            connection.executeUpdate(query, threadId);
            logger.info("Thread {} deleted successfully", threadId);
        } catch (SQLException e) {
            logger.error("Error deleting thread {}: {}", threadId, e.getMessage(), e);
            throw new RuntimeException("Error deleting thread " + threadId, e);
        } finally {
            connection.disconnect();
        }
    }
}