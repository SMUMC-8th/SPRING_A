package com.project.teama_be.domain.post.service.command;


import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.domain.post.converter.CommentConverter;
import com.project.teama_be.domain.post.dto.response.CommentResDTO;
import com.project.teama_be.domain.post.entity.Comment;
import com.project.teama_be.domain.post.entity.CommentReaction;
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.enums.ReactionType;
import com.project.teama_be.domain.post.exception.CommentException;
import com.project.teama_be.domain.post.exception.PostException;
import com.project.teama_be.domain.post.exception.code.CommentErrorCode;
import com.project.teama_be.domain.post.exception.code.PostErrorCode;
import com.project.teama_be.domain.post.repository.CommentReactionRepository;
import com.project.teama_be.domain.post.repository.CommentRepository;
import com.project.teama_be.domain.post.repository.PostRepository;
import com.project.teama_be.global.security.userdetails.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final CommentReactionRepository commentReactionRepository;
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
    public CommentResDTO.CommentUpload createReply(
            Long commentId,
            AuthUser user,
            String content
    ) {
        // 유저 정보
        Member member = getMember();

        // 게시글 정보
        Post post = commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentErrorCode.NOT_FOUND))
                .getPost();

        // 대댓글 저장
        log.info("[ 대댓글 작성 ] 대댓글 작성을 시작합니다.");

        Comment comment = commentRepository.save(
                CommentConverter.toReply(post, member, content, commentId)
        );

        return CommentConverter.toCommentUpload(comment);
    }

    // 댓글 좋아요
    @Transactional
    public CommentResDTO.CommentLike likeComment(
            Long commentId,
            AuthUser user
    ) {
        // 유저 정보
        Member member = getMember();

        // 댓글 좋아요
        log.info("[ 댓글 좋아요 ] 댓글 좋아요를 반영합니다.");
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentErrorCode.NOT_FOUND));

        // 좋아요 여부 조회
        CommentReaction commentReaction= commentReactionRepository
                .findByCommentId(commentId).orElse(null);

        // 좋아요 누른 적이 없으면 좋아요 반영
        if (commentReaction == null) {
            CommentReaction result = commentReactionRepository.save(
                    CommentConverter.toCommentReaction(
                            comment, member, ReactionType.LIKE
                    )
            );
            // 댓글 좋아요 수 ++
            comment.updateLikeCount(comment.getLikeCount() + 1);
            return CommentConverter.toCommentLike(result);
        }
        if (commentReaction.getReactionType().equals(ReactionType.LIKE)) {
            commentReaction.updateReactionType(ReactionType.UNLIKE);
            // 댓글 좋아요 수 --
            comment.updateLikeCount(comment.getLikeCount() - 1);
        } else {
            commentReaction.updateReactionType(ReactionType.LIKE);
            // 댓글 좋아요 수 ++
            comment.updateLikeCount(comment.getLikeCount() + 1);
        }
        return CommentConverter.toCommentLike(commentReaction);
    }


    // 유저 정보 : 임시
    private Member getMember() {
        return memberRepository.findByLoginId("test").orElseThrow(()->
                new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
