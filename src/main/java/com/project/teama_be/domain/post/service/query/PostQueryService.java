package com.project.teama_be.domain.post.service.query;

import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.project.teama_be.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    // 게시글 조회
    public PostResDTO.HomePost getPost(
            List<String> query
    ) {

        // 페이지네이션 설정
        return postRepository.getPostByPlaceName(query);

    }
}
