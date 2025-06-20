package com.project.teama_be.domain.post.service.command;


import com.google.firebase.messaging.FirebaseMessagingException;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.domain.member.repository.NotRecommendedRepository;
import com.project.teama_be.domain.notification.enums.NotiType;
import com.project.teama_be.domain.notification.exception.NotiException;
import com.project.teama_be.domain.notification.exception.code.NotiErrorCode;
import com.project.teama_be.domain.notification.service.NotiService;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final NotRecommendedRepository notRecommendedRepository;
    private final NotiService notiService;

    // 댓글 작성 ✅
    @Transactional
    public CommentResDTO.CommentUpload createComment(
            Long postId,
            AuthUser user,
            String content
    ) {

        // 게시글 정보 조회
        Post post = postRepository.findById(postId).orElseThrow(()->
                new PostException(PostErrorCode.NOT_FOUND));

        // 유저 정보
        Member member = getMember(user);

        // 댓글 저장
        if (content.isBlank()) {
            throw new CommentException(CommentErrorCode.NOT_BLANK);
        }

        // 차단 여부 확인
        isBlocking(user, post.getMember().getId());

        log.info("[ 댓글 작성 ] postID:{}, member:{}, content:{}", post.getId(), member.getLoginId(), content);
        Comment comment = commentRepository.save(
                CommentConverter.toComment(post, member, content)
        );

        // 알람 기능: 주석처리
//        try {   //member:로그인된 사용자, post에서 member:알림을 받는 사람
//            notiService.sendMessage(member, post.getMember(), post, NotiType.COMMENT);
//        } catch (FirebaseMessagingException e) {
//            throw new NotiException(NotiErrorCode.FCM_SEND_FAIL);
//        }

        return CommentConverter.toCommentUpload(comment);
    }

    // 대댓글 작성 ✅
    @Transactional
    public CommentResDTO.CommentUpload createReply(
            Long commentId,
            AuthUser user,
            String content
    ) {
        // 유저 정보
        Member member = getMember(user);

        // 댓글 정보
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentErrorCode.NOT_FOUND));

        // 게시글 정보
        Post post = comment.getPost();

        // 대댓글 저장
        if (content.isBlank()) {
            throw new CommentException(CommentErrorCode.NOT_BLANK);
        }

        // 차단 여부 확인
        isBlocking(user, comment.getMember().getId());

        log.info("[ 대댓글 작성 ] postID:{}, member:{}, content:{}, commentID:{}",
                post.getId(), member.getLoginId(), content, commentId);
        Comment result = commentRepository.save(
                CommentConverter.toReply(post, member, content, commentId)
        );

        // 알람 기능: 주석처리
//        try {   //member:로그인된 사용자, post에서 member:알림을 받는 사람
//            notiService.sendMessage(member, post.getMember(), post, NotiType.COMMENT_COMMENT);
//        } catch (FirebaseMessagingException e) {
//            throw new NotiException(NotiErrorCode.FCM_SEND_FAIL);
//        }

        return CommentConverter.toCommentUpload(result);
    }

    // 댓글 좋아요 ✅
    @Transactional
    public CommentResDTO.CommentLike likeComment(
            Long commentId,
            AuthUser user
    ) {
        // 유저 정보
        Member member = getMember(user);

        // 댓글 좋아요
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentErrorCode.NOT_FOUND));

        // 차단 여부 확인
        isBlocking(user, comment.getMember().getId());

        // 좋아요 여부 조회
        CommentReaction commentReaction= commentReactionRepository
                .findByCommentId(commentId).orElse(null);

        // 좋아요 누른 적이 없으면 좋아요 반영
        if (commentReaction == null) {

            log.info("[ 댓글 좋아요 ] commentID:{}, member:{}", comment.getId(), member.getLoginId());
            // 댓글 좋아요 생성
            commentReaction = commentReactionRepository.save(
                    CommentConverter.toCommentReaction(
                            comment, member, ReactionType.LIKE
                    )
            );
        } else if (commentReaction.getReactionType().equals(ReactionType.LIKE)) {

            commentReaction.updateReactionType(ReactionType.UNLIKE);
            comment.updateLikeCount(comment.getLikeCount() - 1);
        } else {

            commentReaction.updateReactionType(ReactionType.LIKE);
        }

        // 댓글 좋아요 알림: member: 로그인된 사용자, comment.getMember: 알림을 받는 사람
        if (commentReaction.getReactionType().equals(ReactionType.LIKE)) {

            // 댓글 좋아요 수 ++
            comment.updateLikeCount(comment.getLikeCount() + 1);
            // 알람 보낼 post
            Post post = comment.getPost();

            // 알람 기능: 주석처리
//            try {
//                notiService.sendMessage(member, comment.getMember(), post, NotiType.COMMENT_LIKE);
//            } catch (FirebaseMessagingException e) {
//                throw new NotiException(NotiErrorCode.FCM_SEND_FAIL);
//            }
        }

        return CommentConverter.toCommentLike(commentReaction);
    }

    // 댓글 수정
    @Transactional
    public CommentResDTO.CommentUpdate updateComment(
            Long commentId,
            AuthUser user,
            String content
    ){
        // 댓글 작성자인지 확인
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentErrorCode.NOT_FOUND));
        if (!comment.getMember().getId().equals(user.getUserId())){
            throw new CommentException(CommentErrorCode.ACCESS_DENIED);
        }

        comment.updateContent(content);
        return CommentConverter.toCommentUpdate(comment);
    }

    // 댓글 삭제
    @Transactional
    public CommentResDTO.CommentDelete deleteComment(
            Long commentId,
            AuthUser user
    ){
        // 댓글 작성자인지 확인
        Comment comment = commentRepository.findById(commentId).orElseThrow(()->
                new CommentException(CommentErrorCode.NOT_FOUND));
        if (!comment.getMember().getId().equals(user.getUserId())){
            throw new CommentException(CommentErrorCode.ACCESS_DENIED);
        }

        commentRepository.deleteById(commentId);
        LocalDateTime now = LocalDateTime.now();
        return CommentConverter.toCommentDelete(comment, now);
    }

    // 멤버 조회 ✅
    private Member getMember(AuthUser user) {

        return memberRepository.findByLoginId(user.getLoginId()).orElseThrow(() ->
                new UsernameNotFoundException("로그인된 유저를 찾을 수 없습니다."));
    }

    // 차단당한 유저인지 확인
    private void isBlocking(AuthUser targetUser, Long userId) {
        List<Long> result = notRecommendedRepository.findBlockingUserList(userId);
        if (result.contains(targetUser.getUserId())) {
            throw new CommentException(CommentErrorCode.BLOCKING);
        }
    }
}
