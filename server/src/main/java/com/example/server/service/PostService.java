package com.example.server.service;

import com.example.server.model.Post;
import com.example.server.model.User;
import com.example.server.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;

    @Autowired
    public PostService(PostRepository postRepository,
                       AuditLogService auditLogService,
                       CurrentUserService currentUserService) {
        this.postRepository = postRepository;
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Optional<Post> findById(Integer id) {
        return postRepository.findById(id);
    }

    @Transactional
    public Post createPost(String namePost) {
        Post post = new Post(namePost);
        Post savedPost = postRepository.save(post);

        // Логирование
        try {
            Map<String, Object> postData = Map.of(
                    "id", savedPost.getId(),
                    "name", savedPost.getNamePost()
            );

            auditLogService.createAuditLog(
                    currentUserService.getActorForLogging(),
                    "INSERT",
                    "posts",
                    savedPost.getId(),
                    null,
                    postData
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedPost;
    }

    @Transactional
    public Post save(Post post) {
        boolean isNew = post.getId() == null;
        Map<String, Object> beforeData = null;

        if (!isNew) {
            Optional<Post> existing = findById(post.getId());
            if (existing.isPresent()) {
                beforeData = Map.of(
                    "id", existing.get().getId(),
                    "name", existing.get().getNamePost()
                );
            }
        }

        Post savedPost = postRepository.save(post);

        // Логирование
        try {
            Map<String, Object> afterData = Map.of(
                    "id", savedPost.getId(),
                    "name", savedPost.getNamePost()
            );

            auditLogService.createAuditLog(
                    currentUserService.getActorForLogging(),
                    isNew ? "INSERT" : "UPDATE",
                    "posts",
                    savedPost.getId(),
                    beforeData,
                    afterData
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedPost;
    }

    @Transactional
    public void deleteById(Integer id) {
        Optional<Post> postOpt = findById(id);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();

            // Логирование до удаления
            try {
                Map<String, Object> beforeData = Map.of(
                        "id", post.getId(),
                        "name", post.getNamePost()
                );

                auditLogService.createAuditLog(
                        currentUserService.getActorForLogging(),
                        "DELETE",
                        "posts",
                        id,
                        beforeData,
                        null
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            postRepository.deleteById(id);
        }
    }

    @Transactional
    public Post update(Integer id, Post post) {
        post.setId(id);
        return save(post);
    }
}