package com.project.teama_be.domain.post.service.command;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.project.teama_be.domain.location.entity.Location;
import com.project.teama_be.domain.location.repository.LocationRepository;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.entity.RecentlyViewed;
import com.project.teama_be.domain.member.repository.MemberRepository;
import com.project.teama_be.domain.member.repository.RecentlyViewedRepository;
import com.project.teama_be.domain.notification.enums.NotiType;
import com.project.teama_be.domain.notification.exception.NotiException;
import com.project.teama_be.domain.notification.exception.code.NotiErrorCode;
import com.project.teama_be.domain.notification.service.NotiService;
import com.project.teama_be.domain.post.converter.PostConverter;
import com.project.teama_be.domain.post.converter.TagConverter;
import com.project.teama_be.domain.post.dto.request.PostReqDTO;
import com.project.teama_be.domain.post.dto.response.PostResDTO;
import com.project.teama_be.domain.post.entity.Post;
import com.project.teama_be.domain.post.entity.PostReaction;
import com.project.teama_be.domain.post.entity.PostTag;
import com.project.teama_be.domain.post.entity.Tag;
import com.project.teama_be.domain.post.enums.ReactionType;
import com.project.teama_be.domain.post.exception.PostException;
import com.project.teama_be.domain.post.exception.code.PostErrorCode;
import com.project.teama_be.domain.post.repository.*;
import com.project.teama_be.global.aws.util.S3Util;
import com.project.teama_be.global.security.userdetails.AuthUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final PostReactionRepository postReactionRepository;
    private final PostImageRepository postImageRepository;
    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;
    private final RecentlyViewedRepository recentlyViewedRepository;
    private final S3Util s3Util;
    private final NotiService notiService;

    // 게시글 업로드 ✅
    @Transactional
    public PostResDTO.PostUpload PostUpload(
            AuthUser user,
            List<MultipartFile> image,
            PostReqDTO.PostUpload postUpload
    ) {

        // 유저 정보 생성
        Member member = getMember(user);

        // 위치 정보 생성 : 존재하는지 확인, 없으면 생성, 있으면 그걸로 (구현X)
        Location location;
        if (locationRepository.existsByPlaceName(postUpload.placeName())) {
            location = locationRepository.findByPlaceName(postUpload.placeName());
        } else {
            // 추후에 LocationConverter 로 변환
            location = Location.builder()
                    .latitude(postUpload.latitude())
                    .longitude(postUpload.longitude())
                    .placeName(postUpload.placeName())
                    .build();

            log.info("[ 위치 정보 생성 ] locationID:{}", location.getId());
            locationRepository.save(location);
        }
        log.info("[ 위치 정보 생성 ] locationID:{}",location.getId());

        // 태그 생성 : 기존 태그 불러오기 + 없는 태그 저장하기
        List<Tag> foundTags = tagRepository.findByTagNameIn(postUpload.tags());
        Map<String, Tag> tagMap = foundTags.stream()
                .collect(Collectors.toMap(Tag::getTagName, Function.identity()));

        List<Tag> tags = new ArrayList<>();
        for (String tagName : postUpload.tags()) {
            Tag tag = tagMap.get(tagName);
            if (tag == null) {
                tag = tagRepository.save(TagConverter.toTag(tagName));
            }
            tags.add(tag);
        }
        log.info("[ 태그 생성 ] tagCnt:{}", tags.size());

        // 게시글 생성
        Post post = PostConverter.toPost(location, member, postUpload);
        log.info("[ 게시글 생성 ] postID:{}", post.getId());
        postRepository.save(post);

        // 태그 <-> 게시글 연동
        for (Tag tag : tags) {
            PostTag postTag = PostTag.builder()
                    .post(post)
                    .tag(tag)
                    .build();
            postTag = postTagRepository.save(postTag);
            log.info("[ 태그 <-> 게시글 연동 저장 ] postTagID:{}", postTag.getId());
        }

        // 사진 업로드, 게시글 <-> 이미지 연동
        List<String> url = s3Util.uploadFile(image, "post/");
        for (String s : url) {
            log.info("[ 사진 업로드 ] s3Url:{}", s);
            postImageRepository.save(PostConverter.toPostImage(post, s));
        }

        log.info("[ 게시글 업로드 ] postID:{}", post.getId());
        return PostConverter.toPostUpload(post);
    }

    // 게시글 좋아요 ✅
    @Transactional
    public PostResDTO.PostLike PostLike(
            AuthUser user,
            Long postId
    ) {

        // 유저 정보 생성
        Member member = getMember(user);

        // 게시글 존재 여부 확인
        Post post = getPost(postId);

        // 현재 반응 조회
        PostReaction reaction = postReactionRepository.findByMemberIdAndPostId(member.getId(), postId);

        log.info("[ 게시글 좋아요 ] postID:{}, member:{}, reaction:{}", post.getId(), member.getLoginId(), reaction);
        // 좋아요 누른 적이 없으면 생성
        if (reaction == null) {

            reaction = postReactionRepository.save(
                    PostConverter.toPostReaction(member, post, ReactionType.LIKE));
        } else if (reaction.getReactionType().equals(ReactionType.LIKE)) {

            reaction.updateReactionType(ReactionType.UNLIKE);
            post.updateLikeCount(post.getLikeCount() - 1);
        } else {

            reaction.updateReactionType(ReactionType.LIKE);
        }

        // 좋아요만 알람: member: 로그인된 사용자, post.getMember(): 알림을 받는 사람
        if (reaction.getReactionType().equals(ReactionType.LIKE)) {

            post.updateLikeCount(post.getLikeCount() + 1);
            try {
                notiService.sendMessage(member, post.getMember(), post, NotiType.LIKE);
            } catch (FirebaseMessagingException e) {
                throw new NotiException(NotiErrorCode.FCM_SEND_FAIL);
            }
        }

        log.info("[ 게시글 좋아요 ] reactionID:{}", reaction.getId());

        return PostConverter.toPostLike(reaction);
    }

    // 최근 본 게시글 추가
    @Transactional
    public void addRecentPost(
            Long postId,
            AuthUser user
    ) {
        Member member = getMember(user);

        // 게시글 정보 생성
        Post post = getPost(postId);

        // 이미 본적이 있는 경우: 시청 시각 업데이트
        if (recentlyViewedRepository.existsByMemberIdAndPostId(member.getId(), postId)) {
            RecentlyViewed recentlyViewed = recentlyViewedRepository.findByMemberIdAndPostId(member.getId(), postId);
            recentlyViewed.updateViewedAt(LocalDateTime.now());
            log.info("[ 최근 본 게시글 업데이트 ] recentlyViewedID:{}", recentlyViewed.getId());
            return;
        }

        log.info("[ 최근 본 게시글 추가 ] memberID:{}, postID:{}", member.getId(), post.getId());
        // 최근 본 게시글 저장
        RecentlyViewed recentlyViewed = PostConverter.toRecentlyViewed(post, member);
        recentlyViewedRepository.save(recentlyViewed);
    }

    // 유저 정보 생성 ✅
    private Member getMember(AuthUser user) {

        Member member = memberRepository.findByLoginId(user.getLoginId()).orElseThrow(()->
                new PostException(PostErrorCode.USER_NOT_FOUND));
        log.info("[ 유저 정보 생성 ] member:{}", member.getLoginId());
        return member;
    }

    // 게시글 정보 생성
    private Post getPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() ->
                new PostException(PostErrorCode.NOT_FOUND));
    }
}
