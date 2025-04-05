package com.audition.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.AuditionPostComment;
import com.audition.model.AuditionPostWithComments;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
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

    private static final String TYPECODE_URL = "https://jsonplaceholder.typicode.com";
    private static final String POSTS_URL = TYPECODE_URL + "/posts";
    private static final String COMMENTS_URL = TYPECODE_URL + "/comments";

    @Mock
    private RestTemplate restTemplate;

    @Spy
    @InjectMocks
    private AuditionIntegrationClient client;

    @Test
    void givenPostsExist_whenGetPosts_thenReturnThem() {
        // Given
        AuditionPost post1 = new AuditionPost(1, 101, "Post Title 1", "Post Body 1");
        AuditionPost post2 = new AuditionPost(2, 102, "Post Title 2", "Post Body 2");
        List<AuditionPost> mockPosts = List.of(post1, post2);
        ResponseEntity<List<AuditionPost>> responseEntity = new ResponseEntity<>(mockPosts, HttpStatus.OK);
        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.eq(POSTS_URL),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.isNull(),
            ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPost>>>any()
        )).thenReturn(responseEntity);

        // When
        List<AuditionPost> result = client.getPosts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Post Title 1", result.get(0).getTitle());
    }

    @Test
    void givenApiFailure_whenGetPosts_thenThrowSystemException() {
        // Given
        String errorMessage = "Service unavailable";
        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.eq(POSTS_URL),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.isNull(),
            ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPost>>>any()
        )).thenThrow(new RestClientException(errorMessage));

        // When
        SystemException exception = assertThrows(SystemException.class, () -> {
            client.getPosts();
        });
        //Then
        assertTrue(exception.getMessage().contains(errorMessage));
    }

    @Test
    void givenTimeout_whenGetPosts_thenThrowSystemException() {
        //Given
        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.eq("https://jsonplaceholder.typicode.com/posts"),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.isNull(),
            ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPost>>>any()
        )).thenThrow(new ResourceAccessException("Connection timed out"));
        //When
        SystemException exception = assertThrows(SystemException.class, () -> client.getPosts());
        //Then
        assertTrue(exception.getMessage().contains("Connection timed out"));
    }

    @Test
    void givenValidPostId_whenGetPostById_thenReturnAuditionPost() {
        // Given
        String postId = "101";
        AuditionPost expectedPost = new AuditionPost(1, 101, "Title", "Body");
        ResponseEntity<AuditionPost> responseEntity = new ResponseEntity<>(expectedPost, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.eq("https://jsonplaceholder.typicode.com/posts/" + postId),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.isNull(),
            ArgumentMatchers.eq(AuditionPost.class)
        )).thenReturn(responseEntity);

        // When
        AuditionPost result = client.getPostById(postId);

        // Then
        assertNotNull(result);
        assertEquals("Title", result.getTitle());
    }

    @Test
    void givenInvalidPostId_whenGetPostById_thenThrowSystemExceptionWith404() {
        // Given
        String postId = "999";
        HttpClientErrorException notFoundException = HttpClientErrorException.create(
            HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null
        );

        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.eq("https://jsonplaceholder.typicode.com/posts/" + postId),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.isNull(),
            ArgumentMatchers.eq(AuditionPost.class)
        )).thenThrow(notFoundException);

        // When & Then
        SystemException ex = assertThrows(SystemException.class, () -> client.getPostById(postId));
        assertEquals("Cannot find a Post with id " + postId, ex.getMessage());
        assertEquals(404, ex.getStatusCode());
    }

    @Test
    void givenHttpClientErrorOtherThan404_whenGetPostById_thenThrowSystemException() {
        // Given
        String postId = "101";
        HttpClientErrorException badRequestException = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            HttpHeaders.EMPTY,
            null,
            null
        );

        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.eq("https://jsonplaceholder.typicode.com/posts/" + postId),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.isNull(),
            ArgumentMatchers.eq(AuditionPost.class)
        )).thenThrow(badRequestException);

        // When & Then
        SystemException ex = assertThrows(SystemException.class, () -> client.getPostById(postId));

        assertTrue(ex.getMessage().contains("Error fetching post with id " + postId));
        assertEquals("Error Fetching Post", ex.getTitle());
    }

    @Test
    void givenApiFailure_whenGetPostById_thenThrowGenericSystemException() {
        // Given
        String postId = "101";
        RestClientException exception = new RestClientException("Something went wrong");

        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.eq("https://jsonplaceholder.typicode.com/posts/" + postId),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.isNull(),
            ArgumentMatchers.eq(AuditionPost.class)
        )).thenThrow(exception);

        // When & Then
        SystemException ex = assertThrows(SystemException.class, () -> client.getPostById(postId));
        assertTrue(ex.getMessage().contains("Something went wrong"));
    }

    @Test
    void givenValidPostId_whenGetPostWithComments_thenReturnPostWithComments() {
        // Given
        String postId = "1";
        AuditionPost mockPost = new AuditionPost(1, 1, "Test Title", "Test Body");
        List<AuditionPostComment> mockComments = List.of(
            new AuditionPostComment(1, 1, "Commenter", "email@example.com", "Great post!")
        );

        // Stub getPostById() call
        Mockito.doReturn(mockPost).when(client).getPostById(postId);

        // Stub RestTemplate call for comments
        String commentsUrl = "https://jsonplaceholder.typicode.com/posts/" + postId + "/comments";
        ResponseEntity<List<AuditionPostComment>> responseEntity = new ResponseEntity<>(mockComments, HttpStatus.OK);
        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.eq(commentsUrl),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.isNull(),
            ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPostComment>>>any()
        )).thenReturn(responseEntity);

        // When
        AuditionPostWithComments result = client.getPostWithComments(postId);

        // Then
        assertNotNull(result);
        assertEquals(mockPost, result.getPost());
        assertEquals(1, result.getComments().size());
        assertEquals("Great post!", result.getComments().get(0).getBody());
    }

    @Test
    void givenValidPostId_whenCommentsApiFails_thenThrowSystemException() {
        // Given
        String postId = "1";
        AuditionPost mockPost = new AuditionPost(1, 1, "Title", "Body");

        Mockito.doReturn(mockPost).when(client).getPostById(postId);

        String commentsUrl = "https://jsonplaceholder.typicode.com/posts/" + postId + "/comments";

        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.eq(commentsUrl),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.isNull(),
            ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPostComment>>>any()
        )).thenThrow(new RestClientException("Comments API failed"));

        // When & Then
        SystemException ex = assertThrows(SystemException.class, () -> client.getPostWithComments(postId));
        assertTrue(ex.getMessage().contains("Failed to fetch post with comments for postId " + postId));
        assertEquals("Error Fetching Post With Comments", ex.getTitle());
    }

    @Test
    void givenInvalidPostId_whenGetPostWithComments_thenThrowSystemExceptionFromGetPostById() {
        // Given
        String postId = "999";

        Mockito.doThrow(new SystemException("Cannot find a Post with id " + postId, "Resource Not Found", 404))
            .when(client).getPostById(postId);

        // When & Then
        SystemException ex = assertThrows(SystemException.class, () -> client.getPostWithComments(postId));
        assertEquals("Cannot find a Post with id " + postId, ex.getMessage());
        assertEquals(404, ex.getStatusCode());
    }

    @Test
    void givenValidPostId_whenGetCommentsForPost_thenReturnComments() {
        // Given
        String postId = "1";
        String url = "https://jsonplaceholder.typicode.com/comments?postId=" + postId;

        List<AuditionPostComment> mockComments = List.of(
            new AuditionPostComment(1, 1, "Jane Doe", "jane@example.com", "Nice post!")
        );

        ResponseEntity<List<AuditionPostComment>> response = new ResponseEntity<>(mockComments, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.eq(url),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.isNull(),
            ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPostComment>>>any()
        )).thenReturn(response);

        // When
        List<AuditionPostComment> result = client.getCommentsForPost(postId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Nice post!", result.get(0).getBody());
    }

    @Test
    void givenApiFailure_whenGetCommentsForPost_thenThrowSystemException() {
        // Given
        String postId = "1";
        String url = "https://jsonplaceholder.typicode.com/comments?postId=" + postId;

        Mockito.when(restTemplate.exchange(
            ArgumentMatchers.eq(url),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.isNull(),
            ArgumentMatchers.<ParameterizedTypeReference<List<AuditionPostComment>>>any()
        )).thenThrow(new RestClientException("Comment service unavailable"));

        // When & Then
        SystemException ex = assertThrows(SystemException.class, () -> client.getCommentsForPost(postId));

        assertTrue(ex.getMessage().contains("Failed to fetch comments for postId " + postId));
        assertEquals("Error Fetching Comments", ex.getTitle());
    }

}

