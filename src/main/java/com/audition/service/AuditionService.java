package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.AuditionPostComment;
import com.audition.model.AuditionPostWithComments;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditionService {

    @Autowired
    private transient AuditionIntegrationClient auditionIntegrationClient;


    public List<AuditionPost> getPosts() {
        return auditionIntegrationClient.getPosts();
    }

    public List<AuditionPost> getFilteredPosts(final Integer minId, final Integer maxId, final String titleContains) {
        final List<AuditionPost> posts = getPosts();
        return posts.stream()
            .filter(post -> minId == null || post.getId() >= minId)
            .filter(post -> maxId == null || post.getId() <= maxId)
            .filter(
                post -> titleContains == null || post.getTitle().toLowerCase(Locale.getDefault())
                    .contains(titleContains.toLowerCase(Locale.getDefault())))
            .collect(Collectors.toList());
    }

    public AuditionPost getPostById(final String postId) {
        return auditionIntegrationClient.getPostById(postId);
    }

    public AuditionPostWithComments getPostWithComments(final String postId) {
        return auditionIntegrationClient.getPostWithComments(postId);
    }

    public List<AuditionPostComment> getCommentsForPost(final String postId) {
        return auditionIntegrationClient.getCommentsForPost(postId);
    }

}
