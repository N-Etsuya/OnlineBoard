package onlineboard.service;

import onlineboard.model.Post;
import onlineboard.model.User;
import onlineboard.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public synchronized Post createPost(String threadId, User user, String content) {
        logger.info("Creating post in thread {} for user {}", threadId, user.getEmail());

        String postId = UUID.randomUUID().toString();
        LocalDateTime timestamp = LocalDateTime.now();
        Post newPost = new Post(postId, threadId, user.getEmail(), user.getNickname(), content, timestamp);

        postRepository.createPost(newPost);
        logger.info("Post created successfully: {}", newPost);

        return newPost;
    }

    public synchronized List<Post> getPosts(String threadId) {
        logger.info("Fetching posts for thread {}", threadId);
        List<Post> posts = postRepository.getPostsByThreadId(threadId);
        logger.info("Fetched {} posts for thread {}", posts.size(), threadId);
        return posts;
    }

    public synchronized Post getPostById(String postId) {
        logger.info("Fetching post with id: {}", postId);
        Post post = postRepository.getPostById(postId);
        if (post == null) {
            logger.warn("Post not found with id: {}", postId);
            throw new RuntimeException("Post not found");
        }
        return post;
    }

    public synchronized void deletePost(String postId) {
        logger.info("Deleting post {}", postId);
        postRepository.deletePost(postId);
        logger.info("Post {} deleted", postId);
    }
}