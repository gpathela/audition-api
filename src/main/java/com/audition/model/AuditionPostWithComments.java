package com.audition.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditionPostWithComments {

    AuditionPost post;
    List<AuditionPostComment> comments;

    public AuditionPostWithComments(final AuditionPost post, final List<AuditionPostComment> comments) {
        this.post = post;
        this.comments = comments;
    }

}
