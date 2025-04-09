package com.audition.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.audition.common.exception.SystemException;
import com.audition.configuration.AuditionClientProperties;
import com.audition.model.AuditionPost;
import com.audition.model.AuditionPostComment;
import com.audition.model.AuditionPostWithComments;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class AuditionIntegrationClientTest {


    @Mock
    private transient RestTemplate restTemplate;

    private transient AuditionClientProperties auditionClientProperties;


    private transient AuditionIntegrationClient client;

    @BeforeEach
    void setUp() {
        auditionClientProperties = new AuditionClientProperties();
        auditionClientProperties.setBaseUrl("https://jsonplaceholder.typicode.com");
        auditionClientProperties.setPostsPath("/posts");
        auditionClientProperties.setCommentsPath("/comments");
        client = new AuditionIntegrationClient(restTemplate, auditionClientProperties);
        client = Mockito.spy(new AuditionIntegrationClient(restTemplate, auditionClientProperties));
    }

    
    @Nested
    class GetPostCommentsTest {

        @Test
        void givenValidPostIdWhenGetCommentsForPostThenReturnComments() {
            // Given
            final String postId = "1";
            final String url = String.format("%s%s?postId=%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getCommentsPath(), postId
            );

            final List<AuditionPostComment> mockComments = List.of(
                new AuditionPostComment(1, 1, "Jane Doe", "jane@example.com", "Nice post!")
            );

            final ResponseEntity<List<AuditionPostComment>> response = new ResponseEntity<>(mockComments,
                HttpStatus.OK);

            Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(url),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPostComment>>>any()
            )).thenReturn(response);

            // When
            final List<AuditionPostComment> result = client.getCommentsForPost(postId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Nice post!", result.get(0).getBody());
        }

        @Test
        void givenApiFailureWhenGetCommentsForPostThenThrowSystemException() {
            // Given
            final String postId = "1";
            final String url = String.format("%s%s?postId=%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getCommentsPath(), postId
            );

            Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(url),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPostComment>>>any()
            )).thenThrow(new RestClientException("Comment service unavailable"));

            // When & Then
            final SystemException ex = assertThrows(SystemException.class, () -> client.getCommentsForPost(postId));

            assertTrue(ex.getMessage().contains("Failed to fetch comments for postId " + postId));
            assertEquals("Error Fetching Comments", ex.getTitle());
        }
    }

    @Nested
    class GetPostWithComments {

        @Test
        void givenValidPostIdWhenGetPostWithCommentsThenReturnPostWithComments() {
            // Given
            final String postId = "1";
            final AuditionPost mockPost = new AuditionPost(1, 1, "Test Title", "Test Body");
            final List<AuditionPostComment> mockComments = List.of(
                new AuditionPostComment(1, 1, "Commenter", "email@example.com", "Great post!")
            );

            // Stub getPostById() call
            Mockito.doReturn(mockPost).when(client).getPostById(postId);

            // Stub RestTemplate call for comments
            final String commentsUrl = String.format("%s%s/%s%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getPostsPath(), postId, auditionClientProperties.getCommentsPath()
            );
            final ResponseEntity<List<AuditionPostComment>> responseEntity = new ResponseEntity<>(mockComments,
                HttpStatus.OK);
            Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(commentsUrl),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPostComment>>>any()
            )).thenReturn(responseEntity);

            // When
            final AuditionPostWithComments result = client.getPostWithComments(postId);

            // Then
            assertNotNull(result);
            assertEquals(mockPost, result.getPost());
            assertEquals(1, result.getComments().size());
            assertEquals("Great post!", result.getComments().get(0).getBody());
        }

        @Test
        void givenValidPostIdWhenCommentsApiFailsThenThrowSystemException() {
            // Given
            final String postId = "1";
            final AuditionPost mockPost = new AuditionPost(1, 1, "Title", "Body");

            Mockito.doReturn(mockPost).when(client).getPostById(postId);

            final String commentsUrl = String.format("%s%s/%s%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getPostsPath(), postId, auditionClientProperties.getCommentsPath()
            );

            Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(commentsUrl),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPostComment>>>any()
            )).thenThrow(new RestClientException("Comments API failed"));

            // When & Then
            final SystemException ex = assertThrows(SystemException.class, () -> client.getPostWithComments(postId));
            assertTrue(ex.getMessage().contains("Failed to fetch post with comments for postId " + postId));
            assertEquals("Error Fetching Post With Comments", ex.getTitle());
        }

        @Test
        void givenInvalidPostIdWhenGetPostWithCommentsThenThrowSystemExceptionFromGetPostById() {
            // Given
            final String postId = "999";

            Mockito.doThrow(new SystemException("Cannot find a Post with id " + postId, "Resource Not Found", 404))
                .when(client).getPostById(postId);

            // When & Then
            final SystemException ex = assertThrows(SystemException.class, () -> client.getPostWithComments(postId));
            assertEquals("Cannot find a Post with id " + postId, ex.getMessage());
            assertEquals(404, ex.getStatusCode());
        }
    }

    @Nested
    class GetPostByIdClass {

        @Test
        void givenValidPostIdWhenGetPostByIdThenReturnAuditionPost() {
            // Given
            final String postId = "101";
            final AuditionPost expectedPost = new AuditionPost(1, 101, "Title", "Body");
            final ResponseEntity<AuditionPost> responseEntity = new ResponseEntity<>(expectedPost, HttpStatus.OK);
            final String url = String.format("%s%s/%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getPostsPath(), postId);
            Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(url),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.eq(AuditionPost.class)
            )).thenReturn(responseEntity);

            // When
            final AuditionPost result = client.getPostById(postId);

            // Then
            assertNotNull(result);
            assertEquals("Title", result.getTitle());
        }

        @Test
        void givenInvalidPostIdWhenGetPostByIdThenThrowSystemExceptionWith404() {
            // Given
            final String postId = "999";
            final HttpClientErrorException notFoundException = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null
            );
            final String url = String.format("%s%s/%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getPostsPath(), postId);
            Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(url),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.eq(AuditionPost.class)
            )).thenThrow(notFoundException);

            // When & Then
            final SystemException ex = assertThrows(SystemException.class, () -> client.getPostById(postId));
            assertEquals("Cannot find a Post with id " + postId, ex.getMessage());
            assertEquals(404, ex.getStatusCode());
        }

        @Test
        void givenHttpClientErrorOtherThan404WhenGetPostByIdThenThrowSystemException() {
            // Given
            final String postId = "101";
            final HttpClientErrorException badRequestException = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                null,
                null
            );
            final String url = String.format("%s%s/%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getPostsPath(), postId);
            Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(url),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.eq(AuditionPost.class)
            )).thenThrow(badRequestException);

            // When & Then
            final SystemException ex = assertThrows(SystemException.class, () -> client.getPostById(postId));

            assertTrue(ex.getMessage().contains("Error fetching post with id " + postId));
            assertEquals("Error Fetching Post", ex.getTitle());
        }

        @Test
        void givenApiFailureWhenGetPostByIdThenThrowGenericSystemException() {
            // Given
            final String postId = "101";
            final RestClientException exception = new RestClientException("Something went wrong");
            final String url = String.format("%s%s/%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getPostsPath(), postId);
            Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(url),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.eq(AuditionPost.class)
            )).thenThrow(exception);

            // When & Then
            final SystemException ex = assertThrows(SystemException.class, () -> client.getPostById(postId));
            assertTrue(ex.getMessage().contains("Something went wrong"));
        }
    }

    @Nested
    class GetPostTest {

        @Test
        void givenPostsExistWhenGetPostsThenReturnThem() {
            // Given
            final AuditionPost post1 = new AuditionPost(1, 101, "Post Title 1", "Post Body 1");
            final AuditionPost post2 = new AuditionPost(2, 102, "Post Title 2", "Post Body 2");
            final List<AuditionPost> mockPosts = List.of(post1, post2);
            final String url = String.format("%s%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getPostsPath());
            final ResponseEntity<List<AuditionPost>> responseEntity = new ResponseEntity<>(mockPosts, HttpStatus.OK);
            Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(url),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPost>>>any()
            )).thenReturn(responseEntity);

            // When
            final List<AuditionPost> result = client.getPosts();

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Post Title 1", result.get(0).getTitle());
        }

        @Test
        void givenApiFailureWhenGetPostsThenThrowSystemException() {
            // Given
            final String errorMessage = "Service unavailable";
            final String url = String.format("%s%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getPostsPath());
            Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(url),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPost>>>any()
            )).thenThrow(new RestClientException(errorMessage));

            // When
            final SystemException exception = assertThrows(SystemException.class, () -> {
                client.getPosts();
            });
            //Then
            assertTrue(exception.getMessage().contains(errorMessage));
        }

        @Test
        void givenTimeoutWhenGetPostsThenThrowSystemException() {

            //Given
            final String url = String.format("%s%s", auditionClientProperties.getBaseUrl(),
                auditionClientProperties.getPostsPath());
            Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(url),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPost>>>any()
            )).thenThrow(new ResourceAccessException("Connection timed out"));
            //When
            final SystemException exception = assertThrows(SystemException.class, () -> client.getPosts());
            //Then
            assertTrue(exception.getMessage().contains("Connection timed out"));
        }
    }

}

