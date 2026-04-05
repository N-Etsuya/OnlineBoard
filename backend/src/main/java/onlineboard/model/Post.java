package onlineboard.model;

import java.time.LocalDateTime;

public class Post {
    private String postId;
    private String threadId;
    private String email;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;

    public Post(String postId, String threadId, String email, String nickname, String content, LocalDateTime createdAt) {
        this.postId = postId;
        this.threadId = threadId;
        this.email = email;
        this.nickname = nickname;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getPostId() {
        return postId;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postId='" + postId + '\'' +
                ", threadId='" + threadId + '\'' +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}