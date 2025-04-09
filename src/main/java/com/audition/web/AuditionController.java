package com.audition.web;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.AuditionPostComment;
import com.audition.model.AuditionPostWithComments;
import com.audition.model.PaginationParams;
import com.audition.service.AuditionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class AuditionController {

    private static final String VALIDATION_ERROR = "Validation Error";
    private static final String INVALID_POST_ID = "Invalid post ID: %s. Must be a numeric value";

    @Autowired
    private transient AuditionService auditionService;

    @RequestMapping(value = "/posts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditionPost> getPosts(
        @RequestParam(required = false) final Integer minId,
        @RequestParam(required = false) final Integer maxId,
        @RequestParam(required = false) final String titleContains,
        @Valid @ModelAttribute final PaginationParams paginationParams) {
        final List<AuditionPost> filtered = auditionService.getFilteredPosts(minId, maxId, titleContains);
        return auditionService.paginate(filtered, paginationParams.getPage(), paginationParams.getSize());
    }

    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditionPost getPostById(@PathVariable("id") final String postId) {
        return handleWithValidatedPostId(postId, auditionService::getPostById);
    }

    @RequestMapping(value = "/posts/{id}/with-comments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditionPostWithComments getPostWithComments(@PathVariable("id") final String postId) {
        return handleWithValidatedPostId(postId, auditionService::getPostWithComments);
    }

    @RequestMapping(value = "/posts/{id}/comments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditionPostComment> getCommentsForPost(@PathVariable("id") final String postId) {
        return handleWithValidatedPostId(postId, auditionService::getCommentsForPost);
    }

    private <T> T handleWithValidatedPostId(final String postId, final Function<String, T> serviceCall) {
        if (!postId.matches("\\d+")) {
            throw new SystemException(String.format(INVALID_POST_ID, postId), VALIDATION_ERROR,
                HttpStatus.BAD_REQUEST.value());
        }
        return serviceCall.apply(postId);
    }
}
