package onlineboard.models;

import java.time.LocalDateTime;

public class Thread {
    private String threadId;
    private String title;
    private LocalDateTime createdAt;

    public Thread(String threadId, String title, LocalDateTime createdAt) {
        this.threadId = threadId;
        this.title = title;
        this.createdAt = createdAt;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}