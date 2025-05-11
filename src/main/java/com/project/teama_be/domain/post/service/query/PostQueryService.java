package com.project.teama_be.domain.post.service.query;

import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.project.teama_be.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    // 홈화면용 게시글 조회
    public PostResDTO.HomePost getPost(
            List<String> query
    ) {

        log.info("[ 게시글 조회 ] 홈화면용 게시글을 조회합니다.]");
        // 페이지네이션 설정
        return postRepository.getPostByPlaceName(query);

    }
}
