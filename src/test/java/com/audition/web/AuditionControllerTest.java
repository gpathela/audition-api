package com.audition.web;

import static org.hamcrest.Matchers.containsString;
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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuditionController.class)
class AuditionControllerTest {

    private static final AuditionPost POST1 = new AuditionPost(1, 1, "First title", "Body 1");
    private static final AuditionPost POST2 = new AuditionPost(2, 2, "Second title", "Body 2");
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuditionService auditionService;
    @MockBean
    private AuditionLogger auditionLogger;

    @Test
    void givenNoParams_whenGetPosts_thenReturnAllPosts() throws Exception {
        List<AuditionPost> mockPosts = List.of(POST1, POST2);
        Mockito.when(auditionService.getFilteredPosts(null, null, null)).thenReturn(mockPosts);

        mockMvc.perform(get("/posts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].title").value("First title"));
    }

    @Test
    void givenMinId_whenGetPosts_thenReturnFilteredPosts() throws Exception {
        Mockito.when(auditionService.getFilteredPosts(eq(2), isNull(), isNull()))
            .thenReturn(List.of(POST2));

        mockMvc.perform(get("/posts").param("minId", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void givenMaxId_whenGetPosts_thenReturnFilteredPosts() throws Exception {
        Mockito.when(auditionService.getFilteredPosts(isNull(), eq(1), isNull()))
            .thenReturn(List.of(POST1));

        mockMvc.perform(get("/posts").param("maxId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void givenTitleContains_whenGetPosts_thenReturnFilteredPosts() throws Exception {
        Mockito.when(auditionService.getFilteredPosts(isNull(), isNull(), eq("First")))
            .thenReturn(List.of(POST1));

        mockMvc.perform(get("/posts").param("titleContains", "First"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("First title"));
    }

    @Test
    void givenAllParams_whenGetPosts_thenReturnFilteredPosts() throws Exception {
        Mockito.when(auditionService.getFilteredPosts(eq(1), eq(2), eq("Second")))
            .thenReturn(List.of(POST2));

        mockMvc.perform(get("/posts")
                .param("minId", "1")
                .param("maxId", "2")
                .param("titleContains", "Second"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void givenEmptyTitleContains_whenGetPosts_thenReturnAll() throws Exception {
        Mockito.when(auditionService.getFilteredPosts(isNull(), isNull(), eq("")))
            .thenReturn(List.of(POST1, POST2));

        mockMvc.perform(get("/posts").param("titleContains", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void givenValidPostId_whenGetPostById_thenReturnPost() throws Exception {
        AuditionPost post = new AuditionPost(1, 1, "Test Title", "Test Body");

        Mockito.when(auditionService.getPostById("1")).thenReturn(post);

        mockMvc.perform(get("/posts/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    void givenNonNumericPostId_whenGetPostById_thenReturn400() throws Exception {
        mockMvc.perform(get("/posts/abc"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidPostId_whenPostNotFound_thenReturn404() throws Exception {
        String postId = "999";
        Mockito.when(auditionService.getPostById(postId))
            .thenThrow(new SystemException("Cannot find a Post with id " + postId, "Resource Not Found", 404));

        mockMvc.perform(get("/posts/" + postId))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Cannot find a Post")));
    }

    @Test
    void givenValidPostId_whenGetPostWithComments_thenReturnPostWithComments() throws Exception {
        AuditionPost post = new AuditionPost(1, 1, "Title", "Body");
        List<AuditionPostComment> comments = List.of(
            new AuditionPostComment(1, 1, "User", "user@example.com", "Nice post")
        );
        AuditionPostWithComments combined = new AuditionPostWithComments(post, comments);

        Mockito.when(auditionService.getPostWithComments("1")).thenReturn(combined);

        mockMvc.perform(get("/posts/1/with-comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.post.id").value(1))
            .andExpect(jsonPath("$.comments.length()").value(1))
            .andExpect(jsonPath("$.comments[0].body").value("Nice post"));
    }

    @Test
    void givenInvalidPostId_whenGetPostWithComments_thenReturn400() throws Exception {
        mockMvc.perform(get("/posts/abc/with-comments"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void givenNonExistingPostId_whenGetPostWithComments_thenReturn404() throws Exception {
        String postId = "999";
        Mockito.when(auditionService.getPostWithComments(postId))
            .thenThrow(new SystemException("Cannot find a Post with id " + postId, "Resource Not Found", 404));

        mockMvc.perform(get("/posts/" + postId + "/with-comments"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Cannot find a Post with id")));
    }

    @Test
    void givenValidPostId_whenGetCommentsForPost_thenReturnComments() throws Exception {
        List<AuditionPostComment> comments = List.of(
            new AuditionPostComment(1, 1, "Alice", "alice@example.com", "Nice one!")
        );

        Mockito.when(auditionService.getCommentsForPost("1")).thenReturn(comments);

        mockMvc.perform(get("/posts/1/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Alice"))
            .andExpect(jsonPath("$[0].body").value("Nice one!"));
    }

    @Test
    void givenInvalidPostId_whenGetCommentsForPost_thenReturn400() throws Exception {
        mockMvc.perform(get("/posts/abc/comments"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidPostId_whenNoCommentsExist_thenReturnEmptyList() throws Exception {
        Mockito.when(auditionService.getCommentsForPost("42")).thenReturn(List.of());

        mockMvc.perform(get("/posts/42/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }
}
