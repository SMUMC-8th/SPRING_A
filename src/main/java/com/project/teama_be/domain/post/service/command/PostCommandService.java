package com.project.teama_be.domain.post.service.command;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.project.teama_be.domain.location.converter.LocationConverter;
import com.project.teama_be.domain.location.entity.Location;
import com.project.teama_be.domain.location.exception.LocationException;
import com.project.teama_be.domain.location.exception.code.LocationErrorCode;
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
import com.project.teama_be.domain.post.converter.PostTagConverter;
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
import java.util.Optional;
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
            // 의미있는 값인지 확인: 지번, 도로명, 장소명은 공백이면 안됨
            if (postUpload.addressName().isBlank() || postUpload.placeName().isBlank()
                    || postUpload.roadAddressName().isBlank()) {
                throw new LocationException(LocationErrorCode.NOT_VALID);
            }
            location = LocationConverter.toLocation(postUpload);

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

    // 게시글 수정
    @Transactional
    public Optional<PostResDTO.PostUpdate> PostUpdate(
            Long postId,
            AuthUser user,
            PostReqDTO.PostUpdate dto
    ) {

        Post post = getPost(postId);
        if (!post.getMember().getId().equals(user.getUserId())) {
            throw new PostException(PostErrorCode.USER_NOT_MATCH);
        }

        // 플래그
        boolean isChange = false;

        // 내용 변경
        if (!dto.content().isBlank()) {

            isChange = true;
            post.updateContent(dto.content());
        }

        // 태그 변경: 태그 이름이 공백이면 제거
        dto.tags().removeIf(String::isBlank);
        if (!dto.tags().isEmpty()){

            isChange = true;
            // 기존 태그 관계 삭제
            post.getPostTags().clear();

            // 태그 생성: 없는 태그 저장하기
            List<Tag> tagList = new ArrayList<>();
            for (String tagName : dto.tags()) {
                Tag tag = tagRepository.findByTagName(tagName).orElseGet(
                        () -> tagRepository.save(TagConverter.toTag(tagName))
                );
                tagList.add(tag);
            }

            // 새로운 태그 관계 생성
            List<PostTag> postTagList = tagList.stream()
                            .map(tag -> PostTagConverter.toPostTag(tag, post))
                            .toList();
            postTagRepository.saveAll(postTagList);
        }

        // 지역 변경
        if (dto.placeId() > 0){

            isChange = true;
            // 지역 조회: 없으면 에러
            Location location = locationRepository.findById(dto.placeId()).orElseThrow(()->
                    new LocationException(LocationErrorCode.NOT_FOUND));
            post.updateLocation(location);
        }

        if (!isChange) {
            return Optional.empty();
        }
        return Optional.of(PostConverter.toPostUpdate(post));
    }

    // 게시글 삭제
    @Transactional
    public PostResDTO.PostDelete deletePost(
            Long postId,
            AuthUser user
    ) {

        Post post = getPost(postId);
        if (!post.getMember().getId().equals(user.getUserId())) {
            throw new PostException(PostErrorCode.USER_NOT_MATCH);
        }

        log.info("[ 게시글 삭제 ] postID:{}", postId);
        postRepository.deleteById(postId);
        LocalDateTime now = LocalDateTime.now();
        return PostConverter.toPostDelete(post, now);
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
