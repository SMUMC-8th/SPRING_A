package com.project.teama_be.domain.post.converter;

import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.entity.PostTag;
import com.project.teama_be.domain.post.entity.Tag;

public class PostTagConverter {

    // PostTag 생성
    public static PostTag toPostTag(
            Tag tag,
            Post post
    ){
        return PostTag.builder()
                .tag(tag)
                .post(post)
                .build();
    }
}
