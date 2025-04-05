package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.AuditionPostComment;
import com.audition.model.AuditionPostWithComments;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String TYPECODE_URL = "https://jsonplaceholder.typicode.com";
    private static final String POSTS_URL = TYPECODE_URL + "/posts";
    private static final String COMMENTS_URL = TYPECODE_URL + "/comments";


    @Autowired
    private RestTemplate restTemplate;

    public List<AuditionPost> getPosts() {
        try {
            ResponseEntity<List<AuditionPost>> response = restTemplate.exchange(
                POSTS_URL,
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
            String url = POSTS_URL + "/" + id;
            ResponseEntity<AuditionPost> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                AuditionPost.class
            );
            return response.getBody();
        } catch (final HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find a Post with id " + id, "Resource Not Found",
                    HttpStatus.NOT_FOUND.value());
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
            AuditionPost post = getPostById(postId); // reuse your existing method
            String url = POSTS_URL + "/" + postId + "/comments";
            ResponseEntity<List<AuditionPostComment>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AuditionPostComment>>() {
                }
            );
            List<AuditionPostComment> comments = response.getBody();
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
            String url = TYPECODE_URL + "/comments?postId=" + postId;
            ResponseEntity<List<AuditionPostComment>> response = restTemplate.exchange(
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
