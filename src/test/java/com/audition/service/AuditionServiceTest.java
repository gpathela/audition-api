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
import java.util.Locale;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditionServiceTest {

    public static final String FIRST_POST_ID = "101";
    public static final String UNAVAILABLE_POST_ID = "999";
    public static final String POST_NOT_FOUND = "Post not found";
    private static final String FIRST_POST = "First Post";
    private static final AuditionPost POST_1 = new AuditionPost(1, 101, FIRST_POST, "Body A");
    private static final AuditionPost POST_2 = new AuditionPost(2, 102, "Second Post", "Body B");
    private static final AuditionPost POST_3 = new AuditionPost(3, 103, "Another Post", "Body C");
    private static final AuditionPostComment COMMENT = new AuditionPostComment(101, 1, "User", "email@example.com",
        "Nice one");
    @Mock
    private transient AuditionIntegrationClient auditionIntegrationClient;
    @InjectMocks
    private transient AuditionService auditionService;

    @Nested
    class GetPostComments {

        @Test
        void givenValidPostIdWhenGetCommentsForPostThenReturnComments() {
            final List<AuditionPostComment> comments = List.of(COMMENT);

            Mockito.when(auditionIntegrationClient.getCommentsForPost(FIRST_POST_ID)).thenReturn(comments);

            final List<AuditionPostComment> result = auditionService.getCommentsForPost(FIRST_POST_ID);

            assertEquals(1, result.size());
            assertEquals("Nice one", result.get(0).getBody());
        }

        @Test
        void givenInvalidPostIdWhenGetCommentsForPostThenThrowSystemException() {
            Mockito.when(auditionIntegrationClient.getCommentsForPost(UNAVAILABLE_POST_ID))
                .thenThrow(new SystemException(POST_NOT_FOUND, "Resource Not Found", 404));

            assertThrows(SystemException.class, () -> auditionService.getCommentsForPost(UNAVAILABLE_POST_ID));
        }
    }

    @Nested
    class GetPostWithComments {

        @Test
        void givenValidPostIdWhenGetPostWithCommentsThenReturnCombinedObject() {
            final List<AuditionPostComment> comments = List.of(COMMENT);
            final AuditionPostWithComments postWithComments = new AuditionPostWithComments(POST_1, comments);

            Mockito.when(auditionIntegrationClient.getPostWithComments(FIRST_POST_ID)).thenReturn(postWithComments);

            final AuditionPostWithComments result = auditionService.getPostWithComments(FIRST_POST_ID);

            assertNotNull(result);
            assertEquals(FIRST_POST, result.getPost().getTitle());
            assertEquals(1, result.getComments().size());
        }

        @Test
        void givenInvalidPostIdWhenGetPostWithCommentsThenThrowSystemException() {
            Mockito.when(auditionIntegrationClient.getPostWithComments(UNAVAILABLE_POST_ID))
                .thenThrow(new SystemException(POST_NOT_FOUND, "Resource Not Found", 404));

            assertThrows(SystemException.class, () -> auditionService.getPostWithComments(UNAVAILABLE_POST_ID));
        }
    }

    @Nested
    class GetPostsById {

        @Test
        void givenValidPostIdWhenGetPostByIdThenReturnPost() {
            Mockito.when(auditionIntegrationClient.getPostById(FIRST_POST_ID)).thenReturn(POST_1);

            final AuditionPost result = auditionService.getPostById(FIRST_POST_ID);

            assertNotNull(result);
            assertEquals(FIRST_POST, result.getTitle());
        }

        @Test
        void givenInvalidPostIdWhenGetPostByIdThenThrowSystemException() {
            Mockito.when(auditionIntegrationClient.getPostById(UNAVAILABLE_POST_ID))
                .thenThrow(new SystemException(POST_NOT_FOUND, "Resource Not Found", 404));

            final SystemException ex = assertThrows(SystemException.class, () -> auditionService.getPostById(
                UNAVAILABLE_POST_ID));
            assertEquals(POST_NOT_FOUND, ex.getMessage());
        }
    }

    @Nested
    class GetPostsTest {

        @Test
        void whenGetPostsThenReturnAllFromClient() {
            final List<AuditionPost> mockPosts = List.of(POST_1, POST_2);
            Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(mockPosts);

            final List<AuditionPost> result = auditionService.getPosts();

            assertEquals(2, result.size());
            assertEquals(FIRST_POST, result.get(0).getTitle());
        }


        @Test
        void givenNoFiltersWhenGetFilteredPostsThenReturnAll() {
            Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(List.of(POST_1, POST_2, POST_3));

            final List<AuditionPost> result = auditionService.getFilteredPosts(null, null, null);

            assertEquals(3, result.size());
        }

        @Test
        void givenMinIdWhenGetFilteredPostsThenReturnFilteredByMinId() {
            Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(List.of(POST_1, POST_2, POST_3));

            final List<AuditionPost> result = auditionService.getFilteredPosts(102, null, null);

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(post -> post.getId() >= 2));
        }

        @Test
        void givenMaxIdWhenGetFilteredPostsThenReturnFilteredByMaxId() {
            Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(List.of(POST_1, POST_2, POST_3));

            final List<AuditionPost> result = auditionService.getFilteredPosts(null, 102, null);

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(post -> post.getId() <= 102));
        }

        @Test
        void givenTitleContainsWhenGetFilteredPostsThenReturnMatchingTitles() {
            Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(List.of(POST_1, POST_2, POST_3));

            final List<AuditionPost> result = auditionService.getFilteredPosts(null, null, "another");

            assertEquals(1, result.size());
            assertEquals("Another Post", result.get(0).getTitle());
        }

        @Test
        void givenAllFiltersWhenGetFilteredPostsThenReturnCombinedFilterResult() {
            Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(List.of(POST_1, POST_2, POST_3));

            final List<AuditionPost> result = auditionService.getFilteredPosts(102, 103, "post");

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(post ->
                post.getId() >= 102 && post.getId() <= 1033 && post.getTitle().toLowerCase(Locale.getDefault())
                    .contains("post")));
        }
    }
}

