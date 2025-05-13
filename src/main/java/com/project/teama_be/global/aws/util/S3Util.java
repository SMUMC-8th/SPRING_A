package com.project.teama_be.global.aws.util;

import io.awspring.cloud.s3.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Util {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    /** 사진 S3 업로드
     *
     * @param image 업로드할 파일
     * @param folderName 폴더명 ex)../test/
     * @return 업로드 성공시 파일 경로 반환
     * @exception IllegalArgumentException
     */
    public List<String> uploadFile(List<MultipartFile> image, String folderName) {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : image) {
            if (file.getSize() > Long.parseLong(maxFileSize)) {
                log.warn("[ 사진 업로드 ] 파일 크기가 제한 크기를 초과하였습니다.");
                throw new IllegalArgumentException("파일 크기가 제한 크기를 초과하였습니다.");
            }
            String fileName = uploadFile(file, folderName);
            imageUrls.add(getImageUrl(fileName));
        }
        return imageUrls;

    }

    // 사진 S3 업로드 로직
    public String uploadFile(MultipartFile image, String folderName) {

        // 랜덤 파일명 생성
        String fileName = UUID.randomUUID().toString();
        // 파일 확장자 추출
        String fileExtension = getExtension(image);
        // 메타데이터 생성
        ObjectMetadata metadata = ObjectMetadata.builder()
                .contentType(image.getContentType())
                .contentLength(image.getSize())
                .build();

        log.info("[ 사진 업로드 ] 단일 사진 업로드 시작 : {}.{}", fileName, fileExtension);

        try {
            // 파일 업로드 요청
            PutObjectRequest uploadRequest = PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(folderName + fileName + "." + fileExtension)
                            .metadata(metadata.getMetadata())
                            .build();

            s3Client.putObject(
                    uploadRequest,
                    RequestBody.fromInputStream(image.getInputStream(), image.getSize())
            );
            log.info("[ 사진 업로드 ] 단일 사진 업로드 성공");
            return folderName + fileName + "." + fileExtension;
        } catch (IOException e) {
            log.error("[ 사진 업로드 ] 단일 사진 업로드 중 IOException 발생: {}", e.getMessage());
            throw new IllegalArgumentException("파일 업로드 중 오류가 발생하였습니다.");
        } catch (S3Exception e) {
            log.error("[ 사진 업로드 ] 단일 사진 업로드 중 S3Exception 발생: {}", e.getMessage());
            throw new IllegalArgumentException("S3에 파일 업로드 실패하였습니다.");
        }
    }

    /** 사진 URL 조회
     *
     * @param key 조회할 사진 경로명 ex)../test/123456789.jpg
     * @return 파일 URL
     * @exception IllegalArgumentException 파일이 존재하지 않은 경우
     */
    public String getImageUrl(String key) {

        log.info("[ 사진 조회 ] 단일 사진 조회 시작");
        try {
            // URL 요청
            GetUrlRequest urlRequest = GetUrlRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            String url = s3Client.utilities().getUrl(urlRequest).toString();
            log.info("[ 사진 조회 ] 단일 사진 조회 성공");
            return url;
        } catch (S3Exception e) {
            log.error("[ 사진 조회 ] 단일 사진 조회 중 S3Exception 발생: {}", e.getMessage());
            throw new IllegalArgumentException("파일 URL을 가져오는데 실패하였습니다.");
        }
    }

    // 파일 확장자 추출
    private String getExtension(MultipartFile file) {

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            log.warn("[ 사진 정보 추출 ] 파일명이 존재하지 않습니다.]");
            throw new IllegalArgumentException("이미지 파일이 존재하지 않습니다.");
        }

        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (fileExtension.equals("jpg") || fileExtension.equals("jpeg") || fileExtension.equals("png")) {
            return fileExtension;
        }
        log.warn("[ 사진 정보 추출 ] 사진이 아닙니다.");
        throw new IllegalArgumentException("이미지 파일은 jpg, jpeg, png 파일만 가능합니다.");
    }
}
