package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.configuration.AuditionClientProperties;
import com.audition.model.AuditionPost;
import com.audition.model.AuditionPostComment;
import com.audition.model.AuditionPostWithComments;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class AuditionIntegrationClient {


    private final transient RestTemplate restTemplate;
    private final transient AuditionClientProperties auditionClientProperties;

    public AuditionIntegrationClient(RestTemplate restTemplate, AuditionClientProperties auditionClientProperties) {
        this.restTemplate = restTemplate;
        this.auditionClientProperties = auditionClientProperties;
    }

    public List<AuditionPost> getPosts() {
        try {
            final String postsUrl = String.format("%s%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getPostsPath());
            final ResponseEntity<List<AuditionPost>> response = restTemplate.exchange(
                postsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AuditionPost>>() {
                }
            );

            return response.getBody();
        } catch (RestClientException ex) {
            throw new SystemException(ex.getMessage(), ex);
        }
    }

    public AuditionPost getPostById(final String id) {
        try {
            final String postUrl = String.format("%s%s/%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getPostsPath(), id);
            final ResponseEntity<AuditionPost> response = restTemplate.exchange(
                postUrl,
                HttpMethod.GET,
                null,
                AuditionPost.class
            );
            return response.getBody();
        } catch (final HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find a Post with id " + id, "Resource Not Found",
                    HttpStatus.NOT_FOUND.value(), e);
            } else {
                throw new SystemException("Error fetching post with id " + id + ": " + e.getMessage(),
                    "Error Fetching Post",
                    e);
            }
        } catch (RestClientException e) {
            throw new SystemException("Unexpected error: " + e.getMessage(), "Error Fetching Post", e);
        }
    }

    public AuditionPostWithComments getPostWithComments(final String postId) {
        try {
            final AuditionPost post = getPostById(postId); // reuse your existing method
            final String url = String.format("%s%s/%s%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getPostsPath(), postId, auditionClientProperties.getCommentsPath()
            );
            final ResponseEntity<List<AuditionPostComment>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AuditionPostComment>>() {
                }
            );
            final List<AuditionPostComment> comments = response.getBody();
            return new AuditionPostWithComments(post, comments);
        } catch (final RestClientException e) {
            throw new SystemException(
                "Failed to fetch post with comments for postId " + postId + ": " + e.getMessage(),
                "Error Fetching Post With Comments",
                e
            );
        }
    }

    public List<AuditionPostComment> getCommentsForPost(final String postId) {
        try {
            final String url = String.format("%s%s?postId=%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getCommentsPath(), postId
            );
            final ResponseEntity<List<AuditionPostComment>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AuditionPostComment>>() {
                }
            );
            return response.getBody();
        } catch (RestClientException e) {
            throw new SystemException(
                "Failed to fetch comments for postId " + postId + ": " + e.getMessage(),
                "Error Fetching Comments",
                e
            );
        }
    }


}
