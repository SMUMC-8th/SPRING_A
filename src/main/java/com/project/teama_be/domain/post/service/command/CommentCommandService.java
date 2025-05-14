package com.project.teama_be.domain.post.service.command;


import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.domain.post.converter.CommentConverter;
import com.project.teama_be.domain.post.dto.response.CommentResDTO;
import com.project.teama_be.domain.post.entity.Comment;
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.exception.CommentException;
import com.project.teama_be.domain.post.exception.PostException;
import com.project.teama_be.domain.post.exception.code.CommentErrorCode;
import com.project.teama_be.domain.post.exception.code.PostErrorCode;
import com.project.teama_be.domain.post.repository.CommentRepository;
import com.project.teama_be.domain.post.repository.PostRepository;
import com.project.teama_be.global.security.userdetails.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    // 댓글 작성
    public CommentResDTO.CommentUpload createComment(
            Long postId,
            AuthUser user,
            String content
    ) {

        // 게시글 정보 조회
        Post post = postRepository.findById(postId).orElseThrow(()->
                new PostException(PostErrorCode.NOT_FOUND));

        // 유저 정보
        Member member = getMember();

        // 댓글 저장
        log.info("[ 댓글 작성 ] 댓글 작성을 요청합니다.");
        Comment comment = commentRepository.save(
                CommentConverter.toComment(post, member, content)
        );

        return CommentConverter.toCommentUpload(comment);
    }

    // 대댓글 작성
    public CommentResDTO.Reply createReply(
            Long commentId,
            AuthUser user,
            String content
    ) {
        // 유저 정보
        Member member = getMember();

        // 게시글 정보
//        Long postId = commentRepository.findById(commentId).orElseThrow(()->
//                new CommentException(CommentErrorCode.NOT_FOUND));
//        Post post = postRepository.findById()

        return null;
    }

    // 유저 정보 : 임시
    private Member getMember() {
        return memberRepository.findByLoginId("test").orElseThrow(()->
                new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
