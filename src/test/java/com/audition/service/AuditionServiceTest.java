package com.audition.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.audition.common.exception.SystemException;
import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.AuditionPostComment;
import com.audition.model.AuditionPostWithComments;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditionServiceTest {

    private final AuditionPost POST1 = new AuditionPost(1, 101, "First Post", "Body A");
    private final AuditionPost POST2 = new AuditionPost(2, 102, "Second Post", "Body B");
    private final AuditionPost POST3 = new AuditionPost(3, 103, "Another Post", "Body C");
    private final AuditionPostComment comment = new AuditionPostComment(101, 1, "User", "email@example.com",
        "Nice one");
    @Mock
    private AuditionIntegrationClient auditionIntegrationClient;
    @InjectMocks
    private AuditionService auditionService;

    @Test
    void whenGetPosts_thenReturnAllFromClient() {
        List<AuditionPost> mockPosts = List.of(POST1, POST2);
        Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(mockPosts);

        List<AuditionPost> result = auditionService.getPosts();

        assertEquals(2, result.size());
        assertEquals("First Post", result.get(0).getTitle());
    }


    @Test
    void givenNoFilters_whenGetFilteredPosts_thenReturnAll() {
        Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(List.of(POST1, POST2, POST3));

        List<AuditionPost> result = auditionService.getFilteredPosts(null, null, null);

        assertEquals(3, result.size());
    }

    @Test
    void givenMinId_whenGetFilteredPosts_thenReturnFilteredByMinId() {
        Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(List.of(POST1, POST2, POST3));

        List<AuditionPost> result = auditionService.getFilteredPosts(102, null, null);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(post -> post.getId() >= 2));
    }

    @Test
    void givenMaxId_whenGetFilteredPosts_thenReturnFilteredByMaxId() {
        Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(List.of(POST1, POST2, POST3));

        List<AuditionPost> result = auditionService.getFilteredPosts(null, 102, null);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(post -> post.getId() <= 102));
    }

    @Test
    void givenTitleContains_whenGetFilteredPosts_thenReturnMatchingTitles() {
        Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(List.of(POST1, POST2, POST3));

        List<AuditionPost> result = auditionService.getFilteredPosts(null, null, "another");

        assertEquals(1, result.size());
        assertEquals("Another Post", result.get(0).getTitle());
    }

    @Test
    void givenAllFilters_whenGetFilteredPosts_thenReturnCombinedFilterResult() {
        Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(List.of(POST1, POST2, POST3));

        List<AuditionPost> result = auditionService.getFilteredPosts(102, 103, "post");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(post ->
            post.getId() >= 102 && post.getId() <= 1033 && post.getTitle().toLowerCase().contains("post")));
    }

    @Test
    void givenValidPostId_whenGetPostById_thenReturnPost() {
        Mockito.when(auditionIntegrationClient.getPostById("101")).thenReturn(POST1);

        AuditionPost result = auditionService.getPostById("101");

        assertNotNull(result);
        assertEquals("First Post", result.getTitle());
    }

    @Test
    void givenInvalidPostId_whenGetPostById_thenThrowSystemException() {
        Mockito.when(auditionIntegrationClient.getPostById("999"))
            .thenThrow(new SystemException("Post not found", "Resource Not Found", 404));

        SystemException ex = assertThrows(SystemException.class, () -> auditionService.getPostById("999"));
        assertEquals("Post not found", ex.getMessage());
    }

    @Test
    void givenValidPostId_whenGetPostWithComments_thenReturnCombinedObject() {
        List<AuditionPostComment> comments = List.of(comment);
        AuditionPostWithComments postWithComments = new AuditionPostWithComments(POST1, comments);

        Mockito.when(auditionIntegrationClient.getPostWithComments("101")).thenReturn(postWithComments);

        AuditionPostWithComments result = auditionService.getPostWithComments("101");

        assertNotNull(result);
        assertEquals("First Post", result.getPost().getTitle());
        assertEquals(1, result.getComments().size());
    }

    @Test
    void givenInvalidPostId_whenGetPostWithComments_thenThrowSystemException() {
        Mockito.when(auditionIntegrationClient.getPostWithComments("999"))
            .thenThrow(new SystemException("Post not found", "Resource Not Found", 404));

        assertThrows(SystemException.class, () -> auditionService.getPostWithComments("999"));
    }

    @Test
    void givenValidPostId_whenGetCommentsForPost_thenReturnComments() {
        List<AuditionPostComment> comments = List.of(comment);

        Mockito.when(auditionIntegrationClient.getCommentsForPost("101")).thenReturn(comments);

        List<AuditionPostComment> result = auditionService.getCommentsForPost("101");

        assertEquals(1, result.size());
        assertEquals("Nice one", result.get(0).getBody());
    }

    @Test
    void givenInvalidPostId_whenGetCommentsForPost_thenThrowSystemException() {
        Mockito.when(auditionIntegrationClient.getCommentsForPost("999"))
            .thenThrow(new SystemException("Post not found", "Resource Not Found", 404));

        assertThrows(SystemException.class, () -> auditionService.getCommentsForPost("999"));
    }
}

