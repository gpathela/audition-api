package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.AuditionPostComment;
import com.audition.model.AuditionPostWithComments;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditionService {

    @Autowired
    private AuditionIntegrationClient auditionIntegrationClient;


    public List<AuditionPost> getPosts() {
        return auditionIntegrationClient.getPosts();
    }

    public List<AuditionPost> getFilteredPosts(Integer minId, Integer maxId, String titleContains) {
        List<AuditionPost> posts = getPosts();
        return posts.stream()
            .filter(post -> minId == null || post.getId() >= minId)
            .filter(post -> maxId == null || post.getId() <= maxId)
            .filter(
                post -> titleContains == null || post.getTitle().toLowerCase().contains(titleContains.toLowerCase()))
            .collect(Collectors.toList());
    }

    public AuditionPost getPostById(final String postId) {
        return auditionIntegrationClient.getPostById(postId);
    }

    public AuditionPostWithComments getPostWithComments(String postId) {
        return auditionIntegrationClient.getPostWithComments(postId);
    }

    public List<AuditionPostComment> getCommentsForPost(String postId) {
        return auditionIntegrationClient.getCommentsForPost(postId);
    }

}
