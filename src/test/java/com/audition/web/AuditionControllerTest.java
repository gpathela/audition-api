package com.audition.web;


import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionPost;
import com.audition.model.AuditionPostComment;
import com.audition.model.AuditionPostWithComments;
import com.audition.service.AuditionService;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.UnusedPrivateField"})
//Need to avoid as mockMvc.perform throws generic Exception
@WebMvcTest(AuditionController.class)
class AuditionControllerTest {

    public static final String LENGTH = "$.length()";
    public static final String LENGTH_OF_LIST_RETURNED = "$[0].id";
    private static final AuditionPost POST1 = new AuditionPost(1, 1, "First title", "Body 1");
    private static final AuditionPost POST2 = new AuditionPost(2, 2, "Second title", "Body 2");
    private static final String POSTS_URL = "/posts";
    private static final String MOCK_FAILED = "MockMvc call failed: %s";
    @Autowired
    private transient MockMvc mockMvc;
    @MockBean
    private transient AuditionService auditionService;
    @MockBean
    private transient AuditionLogger auditionLogger;

    @Nested
    class GetPostComments {

        @Test
        void givenValidPostIdWhenGetCommentsForPostThenReturnComments() {
            final List<AuditionPostComment> comments = List.of(
                new AuditionPostComment(1, 1, "Alice", "alice@example.com", "Nice one!")
            );

            Mockito.when(auditionService.getCommentsForPost("1")).thenReturn(comments);
            try {
                mockMvc.perform(get(POSTS_URL + "/1/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(LENGTH).value(1))
                    .andExpect(jsonPath("$[0].name").value("Alice"))
                    .andExpect(jsonPath("$[0].body").value("Nice one!"));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }

        }

        @Test
        void givenInvalidPostIdWhenGetCommentsForPostThenReturn400() {
            try {
                mockMvc.perform(get(POSTS_URL + "/abc/comments"))
                    .andExpect(status().isBadRequest());
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }

        @Test
        void givenValidPostIdWhenNoCommentsExistThenReturnEmptyList() {
            Mockito.when(auditionService.getCommentsForPost("42")).thenReturn(List.of());
            try {
                mockMvc.perform(get(POSTS_URL + "/42/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(LENGTH).value(0));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }
    }

    @Nested
    class GetPostWithComments {

        @Test
        void givenValidPostIdWhenGetPostWithCommentsThenReturnPostWithComments() {
            final AuditionPost post = new AuditionPost(1, 1, "Title", "Body");
            final List<AuditionPostComment> comments = List.of(
                new AuditionPostComment(1, 1, "User", "user@example.com", "Nice post")
            );
            final AuditionPostWithComments combined = new AuditionPostWithComments(post, comments);

            Mockito.when(auditionService.getPostWithComments("1")).thenReturn(combined);
            try {
                mockMvc.perform(get(POSTS_URL + "/1/with-comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.post.id").value(1))
                    .andExpect(jsonPath("$.comments.length()").value(1))
                    .andExpect(jsonPath("$.comments[0].body").value("Nice post"));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }

        @Test
        void givenInvalidPostIdWhenGetPostWithCommentsThenReturn400() {
            try {
                mockMvc.perform(get(POSTS_URL + "/abc/with-comments"))
                    .andExpect(status().isBadRequest());
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }

        @Test
        void givenNonExistingPostIdWhenGetPostWithCommentsThenReturn404() {
            final String postId = "999";
            Mockito.when(auditionService.getPostWithComments(postId))
                .thenThrow(new SystemException("Cannot find a Post with id " + postId, "Resource Not Found", 404));
            try {
                mockMvc.perform(get(POSTS_URL + "/" + postId + "/with-comments"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Cannot find a Post with id")));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }
    }

    @Nested
    class GetPostByIdTest {

        @Test
        void givenValidPostIdWhenGetPostByIdThenReturnPost() {
            final AuditionPost post = new AuditionPost(1, 1, "Test Title", "Test Body");

            Mockito.when(auditionService.getPostById("1")).thenReturn(post);
            try {
                mockMvc.perform(get(POSTS_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Test Title"));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }

        @Test
        void givenNonNumericPostIdWhenGetPostByIdThenReturn400() {
            try {
                mockMvc.perform(get(POSTS_URL + "/abc"))
                    .andExpect(status().isBadRequest());
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }

        @Test
        void givenValidPostIdWhenPostNotFoundThenReturn404() {
            final String postId = "999";
            Mockito.when(auditionService.getPostById(postId))
                .thenThrow(new SystemException("Cannot find a Post with id " + postId, "Resource Not Found", 404));
            try {
                mockMvc.perform(get(POSTS_URL + "/" + postId))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Cannot find a Post")));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }
    }

    @Nested
    class FilterPostsTest {

        @Test
        void givenNoParamsWhenGetPostsThenReturnAllPosts() {
            final List<AuditionPost> mockPosts = List.of(POST1, POST2);
            Mockito.when(auditionService.getFilteredPosts(null, null, null)).thenReturn(mockPosts);
            Mockito.when(auditionService.paginate(mockPosts, 0, 10)).thenReturn(mockPosts);
            try {
                mockMvc.perform(get(POSTS_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(LENGTH).value(2))
                    .andExpect(jsonPath("$[0].title").value("First title"));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }


        @Test
        void givenMinIdWhenGetPostsThenReturnFilteredPosts() {
            Mockito.when(auditionService.getFilteredPosts(eq(2), isNull(), isNull()))
                .thenReturn(List.of(POST2));
            Mockito.when(auditionService.paginate(List.of(POST2), 0, 10)).thenReturn(List.of(POST2));
            try {
                mockMvc.perform(get(POSTS_URL).param("minId", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(LENGTH).value(1))
                    .andExpect(jsonPath(LENGTH_OF_LIST_RETURNED).value(2));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }


        @Test
        void givenMaxIdWhenGetPostsThenReturnFilteredPosts() {
            Mockito.when(auditionService.getFilteredPosts(isNull(), eq(1), isNull()))
                .thenReturn(List.of(POST1));
            Mockito.when(auditionService.paginate(List.of(POST1), 0, 10)).thenReturn(List.of(POST1));
            try {
                mockMvc.perform(get(POSTS_URL).param("maxId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(LENGTH).value(1))
                    .andExpect(jsonPath(LENGTH_OF_LIST_RETURNED).value(1));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }


        @Test
        void givenTitleContainsWhenGetPostsThenReturnFilteredPosts() {
            Mockito.when(auditionService.getFilteredPosts(isNull(), isNull(), eq("First")))
                .thenReturn(List.of(POST1));
            Mockito.when(auditionService.paginate(List.of(POST1), 0, 10)).thenReturn(List.of(POST1));
            try {
                mockMvc.perform(get(POSTS_URL).param("titleContains", "First"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(LENGTH).value(1))
                    .andExpect(jsonPath("$[0].title").value("First title"));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }


        @Test
        void givenAllParamsWhenGetPostsThenReturnFilteredPosts() {
            Mockito.when(auditionService.getFilteredPosts(eq(1), eq(2), eq("Second")))
                .thenReturn(List.of(POST2));
            Mockito.when(auditionService.paginate(List.of(POST2), 0, 10)).thenReturn(List.of(POST2));
            try {
                mockMvc.perform(get(POSTS_URL)
                        .param("minId", "1")
                        .param("maxId", "2")
                        .param("titleContains", "Second"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(LENGTH).value(1))
                    .andExpect(jsonPath(LENGTH_OF_LIST_RETURNED).value(2));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }


        @Test
        void givenEmptyTitleContainsWhenGetPostsThenReturnAll() {
            Mockito.when(auditionService.getFilteredPosts(isNull(), isNull(), eq("")))
                .thenReturn(List.of(POST1, POST2));
            Mockito.when(auditionService.paginate(List.of(POST1, POST2), 0, 10))
                .thenReturn(List.of(POST1, POST2));
            try {
                mockMvc.perform(get(POSTS_URL).param("titleContains", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(LENGTH).value(2));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }
    }

    @Nested
    class PaginationTests {

        @Test
        void givenPaginationParamsWhenGetPostsThenReturnsPaginatedResult() {
            Mockito.when(auditionService.getFilteredPosts(null, null, null))
                .thenReturn(List.of(POST1, POST2));

            Mockito.when(auditionService.paginate(List.of(POST1, POST2), 1, 1))
                .thenReturn(List.of(POST2));
            try {
                mockMvc.perform(get(POSTS_URL)
                        .param("page", "1")
                        .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath(LENGTH_OF_LIST_RETURNED).value(POST2.getId()));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }

        @Test
        void givenNoPaginationParamsWhenGetPostsThenReturnsDefaultPage() {
            Mockito.when(auditionService.getFilteredPosts(null, null, null))
                .thenReturn(List.of(POST1, POST2));

            Mockito.when(auditionService.paginate(List.of(POST1, POST2), 0, 10))
                .thenReturn(List.of(POST1, POST2));
            try {
                mockMvc.perform(get(POSTS_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
            } catch (Exception e) {
                fail(String.format(MOCK_FAILED, e.getMessage()));
            }
        }

    }
}
