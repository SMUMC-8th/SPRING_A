package com.project.teama_be.domain.post.service.command;

import com.project.teama_be.domain.location.entity.Location;
import com.project.teama_be.domain.location.repository.LocationRepository;
import com.project.teama_be.domain.member.entity.Member;
import com.project.teama_be.domain.member.repository.MemberRepository;
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
    private final S3Util s3Util;
    private final LocationRepository locationRepository;

    // 게시글 업로드
    @Transactional
    public PostResDTO.PostUpload PostUpload(
            AuthUser user,
            List<MultipartFile> image,
            PostReqDTO.PostUpload postUpload
    ) {

        // 유저 정보 생성
        Member member = getMember(user);

        // 위치 정보 생성 : 존재하는지 확인, 없으면 생성, 있으면 그걸로 (구현X)
        log.info("[ 위치 정보 생성 ] 위치 정보를 생성합니다.");
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
            log.info("[ 위치 정보 저장 ] 위치 정보를 저장합니다.");
            locationRepository.save(location);
        }

        // 태그 생성 : 기존 태그 불러오기 + 없는 태그 저장하기
        log.info("[ 태그 정보 생성 ] 태그 정보를 생성합니다.");
        List<Tag> foundTags = tagRepository.findByTagNameIn(postUpload.tags());
        Map<String, Tag> tagMap = foundTags.stream()
                .collect(Collectors.toMap(Tag::getTagName, Function.identity()));

        List<Tag> tags = new ArrayList<>();
        for (String tagName : postUpload.tags()) {
            Tag tag = tagMap.get(tagName);
            if (tag == null) {
                tag = tagRepository.save(TagConverter.of(tagName));
            }
            tags.add(tag);
        }

        // 게시글 생성
        log.info("[ 게시글 정보 생성 ] 게시글 정보를 생성합니다.");
        Post post = PostConverter.of(location, member, postUpload);
        log.info("[ 게시글 정보 저장 ] 게시글 정보를 저장합니다.");
        postRepository.save(post);

        // 태그 <-> 게시글 연동
        log.info("[ 태그 <-> 게시글 연동 ] 태그 <-> 게시글 연동을 합니다.");
        for (Tag tag : tags) {
            PostTag postTag = PostTag.builder()
                    .post(post)
                    .tag(tag)
                    .build();
            log.info("[ 태그 <-> 게시글 연동 저장 ] 태그 <-> 게시글 연동을 저장합니다.");
            postTagRepository.save(postTag);
        }

        // 사진 업로드, 게시글 <-> 이미지 연동
        log.info("[ 사진 업로드 ] 사진을 업로드합니다.");
        List<String> url = s3Util.uploadFile(image, "/post/");
        log.info("[ 사진 <-> 게시글 연동 저장 ] 사진 <-> 게시글 연동을 저장합니다.");
        for (String s : url) {
            postImageRepository.save(PostConverter.of(post, s));
        }

        log.info("[ 게시글 업로드 완료 ] 게시글 업로드 완료했습니다.");
        return PostConverter.of(post);
    }

    // 게시글 좋아요
    @Transactional
    public PostResDTO.PostLike PostLike(
            AuthUser user,
            Long postId
    ) {

        // 유저 정보 생성
        Member member = getMember(user);

        // 게시글 존재 여부 확인
        Post post = postRepository.findById(postId).orElseThrow(()->
                new PostException(PostErrorCode.NOT_FOUND));

        // 현재 반응 조회
        log.info("[ 게시글 좋아요 반영 ] 게시글 좋아요를 반영합니다.");
        PostReaction reaction = postReactionRepository.findByMemberIdAndPostId(member.getId(), postId);
        // 좋아요 누른 적이 없으면 좋아요 반영
        if (reaction == null) {
            PostReaction result = postReactionRepository.save(PostConverter.of(member, post, ReactionType.LIKE));
            return PostConverter.of(result);
        }
        String reactionType = reaction.getReactionType().name();
        // 현재 좋아요 상태면 취소, 아니면 좋아요 반영
        if (reactionType.equals(ReactionType.LIKE.name())) {
            reaction.updateReactionType(ReactionType.UNLIKE);
        } else {
            reaction.updateReactionType(ReactionType.LIKE);
        }
        return PostConverter.of(reaction);
    }

    // 유저 정보 생성 : 임시로 예외처리
    private Member getMember(AuthUser user) {
        log.info("[ 유저 정보 생성 ] 유저 정보를 생성합니다.");
//        String uid = user.getUid();
        String uid = "test";
        return memberRepository.findByUid(uid).orElseThrow(()->
                new PostException(PostErrorCode.USER_NOT_FOUND));
    }
}
