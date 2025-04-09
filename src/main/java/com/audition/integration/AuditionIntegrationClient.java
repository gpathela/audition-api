package com.audition.integration;

import com.audition.common.constants.ErrorMessages;
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

    public AuditionIntegrationClient(final RestTemplate restTemplate,
        final AuditionClientProperties auditionClientProperties) {
        this.restTemplate = restTemplate;
        this.auditionClientProperties = auditionClientProperties;
        if (auditionClientProperties.getBaseUrl() == null || auditionClientProperties.getBaseUrl().isBlank()) {
            throw new IllegalStateException(ErrorMessages.MISSING_BASE_URL);
        }
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

            return response.getBody() != null ? response.getBody() : List.of();
        } catch (RestClientException ex) {
            throw new SystemException(
                String.format(String.format(ErrorMessages.ERROR_FETCHING_POSTS_MESSAGE, ex.getMessage())),
                ErrorMessages.ERROR_FETCHING_POSTS, ex);
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
                throw new SystemException(String.format(ErrorMessages.POST_NOT_FOUND, id),
                    ErrorMessages.RESOURCE_NOT_FOUND,
                    HttpStatus.NOT_FOUND.value(), e);
            } else {
                throw new SystemException(String.format(ErrorMessages.ERROR_FETCHING_POST_MESSAGE, id, e.getMessage()),
                    ErrorMessages.ERROR_FETCHING_POST,
                    e);
            }
        } catch (RestClientException e) {
            throw new SystemException(String.format(ErrorMessages.UNEXPECTED_ERROR, e.getMessage()),
                ErrorMessages.ERROR_FETCHING_POST, e);
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
            final List<AuditionPostComment> comments = response.getBody() != null ? response.getBody() : List.of();
            return new AuditionPostWithComments(post, comments);
        } catch (final RestClientException e) {
            throw new SystemException(
                String.format(ErrorMessages.ERROR_FETCHING_POST_WITH_COMMENTS_MESSAGE, postId, e.getMessage()),
                ErrorMessages.ERROR_FETCHING_POST_WITH_COMMENTS,
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
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (RestClientException e) {
            throw new SystemException(
                String.format(ErrorMessages.ERROR_FETCHING_POST_COMMENTS_MESSAGE, postId, e.getMessage()),
                ErrorMessages.ERROR_FETCHING_POST_COMMENTS,
                e
            );
        }
    }


}
