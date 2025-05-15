package com.project.teama_be.domain.member.controller;

import com.project.teama_be.domain.member.dto.request.MemberReqDTO;
import com.project.teama_be.domain.member.dto.response.MemberResDTO;
import com.project.teama_be.domain.member.service.command.MemberCommandService;
import com.project.teama_be.domain.member.service.query.MemberQueryService;
import com.project.teama_be.global.apiPayload.CustomResponse;
import com.project.teama_be.global.security.annotation.CurrentUser;
import com.project.teama_be.global.security.userdetails.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "사용자 관련 API", description = "사용자 관련 API입니다.")
public class MemberController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    @PostMapping("/blocks")
    @Operation(summary = "사용자 추천 안함 API by 김지명", description = "사용자 추천 안함 API")
    public CustomResponse<MemberResDTO.blockMember> blockMember(@CurrentUser AuthUser authUser,
                                                                @RequestBody MemberReqDTO.blockMember reqDTO) {
        MemberResDTO.blockMember resDTO = memberCommandService.blockMember(authUser.getLoginId(), reqDTO);
        return CustomResponse.onSuccess(resDTO);
    }

    @GetMapping("")
    @Operation(summary = "회원 정보 조회 API by 김지명", description = "현재 로그인한 회원의 정보를 조회합니다.")
    public CustomResponse<MemberResDTO.memberInfo> getMemberInfo(@CurrentUser AuthUser authUser) {
        MemberResDTO.memberInfo resDTO = memberQueryService.getMemberInfo(authUser.getLoginId());
        return CustomResponse.onSuccess(resDTO);
    }

    @GetMapping("/check-id")
    @Operation(summary = "아이디 중복 확인 API by 김지명 (개발중)", description = "loginId가 이미 사용 중인지 확인합니다.")
    public CustomResponse<String> checkDuplicateId(@RequestParam("id") String loginId) {

        return CustomResponse.onSuccess("사용가능한 ID 입니다.");
    }

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인 API by 김지명 (개발중)", description = "nickname이 이미 사용 중인지 확인합니다.")
    public CustomResponse<String> checkDuplicateNickname(@RequestParam("nickname") String nickname) {

        return CustomResponse.onSuccess("사용가능한 닉네임 입니다.");
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀번호 변경 API by 김지명 (개발중)", description = "기존 비밀번호를 확인하고 새 비밀번호로 변경합니다.")
    public CustomResponse<String> changePassword(@CurrentUser AuthUser authUser,
                                                 @RequestBody @Valid MemberReqDTO.changePassword reqDTO) {

        return CustomResponse.onSuccess("비밀번호가 변경되었습니다.");
    }

    @PatchMapping("/nickname")
    @Operation(summary = "닉네임 변경 API by 김지명 (개발중)", description = "사용자의 닉네임을 새로운 닉네임으로 변경합니다.")
    public CustomResponse<MemberResDTO.changeNickname> changeNickname(
            @CurrentUser AuthUser authUser,
            @RequestBody @Valid MemberReqDTO.changeNickname reqDTO) {

        return CustomResponse.onSuccess(null);
    }

    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 변경 API by 김지명 (개발중)", description = "회원의 프로필 이미지를 업로드하고 URL을 반환합니다.")
    public CustomResponse<MemberResDTO.changeProfileImg> updateProfileImage(@CurrentUser AuthUser authUser,
                                                                            @RequestPart("file") MultipartFile file) {

        return CustomResponse.onSuccess(null);
    }

    @DeleteMapping("")
    @Operation(summary = "회원 탈퇴 API by 김지명 (개발중)", description = "회원 탈퇴(soft delete) 후 삭제 시간을 반환합니다.")
    public CustomResponse<MemberResDTO.deleteMember> deleteMember(@CurrentUser AuthUser authUser) {

        return CustomResponse.onSuccess(null);
    }

}
