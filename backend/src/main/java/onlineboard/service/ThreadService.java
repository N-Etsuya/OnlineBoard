package onlineboard.service;

import onlineboard.model.Thread;
import onlineboard.model.User;
import onlineboard.repository.ThreadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ThreadService {
    private static final Logger logger = LoggerFactory.getLogger(ThreadService.class);
    private ThreadRepository threadRepository;

    public ThreadService(ThreadRepository threadRepository) {
        this.threadRepository = threadRepository;
    }

    public synchronized Thread createThread(User user, String title) {
        logger.info("Creating thread with title '{}' for user {}", title, user.getEmail());
        String id = UUID.randomUUID().toString();
        LocalDateTime createdAt = LocalDateTime.now();
        Thread thread = new Thread(id, title, createdAt);
        threadRepository.createThread(thread);

        logger.info("Thread created successfully: {}", thread);
        return thread;
    }

    public synchronized List<Thread> getThreads() {
        logger.info("Fetching all threads");
        List<Thread> threads = threadRepository.getAllThreads();
        logger.info("Fetched {} threads", threads.size());
        return threads;
    }

    public synchronized Thread getThreadById(String threadId) {
        logger.info("Fetching thread with id: {}", threadId);
        Thread thread = threadRepository.getThreadById(threadId);
        if (thread == null) {
            logger.warn("Thread not found with id: {}", threadId);
            throw new RuntimeException("Thread not found");
        }
        return thread;
    }

    public synchronized void deleteThread(String threadId) {
        logger.info("Deleting thread {}", threadId);
        threadRepository.deleteThread(threadId);
        logger.info("Thread {} deleted", threadId);
    }
}