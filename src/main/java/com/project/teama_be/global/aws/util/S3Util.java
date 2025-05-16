package com.project.teama_be.global.aws.util;

import com.project.teama_be.global.aws.exception.S3Exception;
import com.project.teama_be.global.aws.exception.code.S3ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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

    private static final String PREFIX = "https://s3.ap-northeast-2.amazonaws.com/api-smp.shop";

    /** 사진 S3 업로드
     *
     * @param image 업로드할 파일
     * @param folderName 폴더명 ex) test/
     * @return 업로드 성공시 파일 경로 반환
     */
    public List<String> uploadFile(List<MultipartFile> image, String folderName) {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : image) {
            if (file.getSize() > Long.parseLong(maxFileSize)) {
                log.warn("[ 사진 업로드 ] 파일 크기가 제한 크기를 초과하였습니다.");
                throw new S3Exception(S3ErrorCode.FILE_SIZE_OVER);
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

        log.info("[ 사진 업로드 ] 단일 사진 업로드 시작 : {}.{}", fileName, fileExtension);

        try {
            // 파일 업로드 요청
            PutObjectRequest uploadRequest = PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(folderName + fileName + "." + fileExtension)
                            .contentType("image/" + fileExtension)
                            .contentLength(image.getSize())
                            .contentDisposition("inline")
                            .build();

            s3Client.putObject(
                    uploadRequest,
                    RequestBody.fromInputStream(image.getInputStream(), image.getSize())
            );
            log.info("[ 사진 업로드 ] 단일 사진 업로드 성공");
            return folderName + fileName + "." + fileExtension;
        } catch (IOException e) {
            log.error("[ 사진 업로드 ] 단일 사진 업로드 중 IOException 발생: {}", e.getMessage());
            throw new S3Exception(S3ErrorCode.IO_EXCEPTION);
        } catch (S3Exception e) {
            log.error("[ 사진 업로드 ] 단일 사진 업로드 중 S3Exception 발생: {}", e.getMessage());
            throw new S3Exception(S3ErrorCode.S3_EXCEPTION);
        }
    }

    /** 사진 URL 조회
     *
     * @param key 조회할 사진 경로명 ex)../test/123456789.jpg
     * @return 파일 URL
     */
    public String getImageUrl(String key) {

        try {
            // URL 요청
            GetUrlRequest urlRequest = GetUrlRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            String url = s3Client.utilities().getUrl(urlRequest).toString();
            log.info("[ 사진 조회 ] url:{}", url);
            return url;
        } catch (S3Exception e) {
            log.error("[ 사진 조회 ] 단일 사진 조회 중 S3Exception 발생: {}", e.getMessage());
            throw new S3Exception(S3ErrorCode.S3_EXCEPTION);
        }
    }

    /** 파일 삭제 로직 :
     * S3에 저장된 파일을 삭제합니다.
     * @param key DB에 저장되어 있는 URL ex) https://s3.~~/post/~~.png
     * @return 삭제한 URL ex) https://s3.~~/post/~~.png
     */
    public String deleteFile(String key) {

        try {
            // 접두사 제거
            key = key.replace(PREFIX, "");

            log.info("[ 사진 삭제 ] key:{}", key);
            // URL 요청
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
            log.info("[ 사진 삭제 ] OriginUrl:{}", PREFIX+key);
            return PREFIX+key;
        } catch (S3Exception e) {
            log.error("[ 사진 삭제 ] 단일 사진 삭제 중 S3Exception 발생: {}", e.getMessage());
            throw new S3Exception(S3ErrorCode.S3_EXCEPTION);
        }
    }

    // 파일 확장자 추출
    private String getExtension(MultipartFile file) {

        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            log.warn("[ 사진 정보 추출 ] 파일명이 존재하지 않습니다.");
            throw new S3Exception(S3ErrorCode.NOT_FOUND_FILE);
        }

        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (fileExtension.equals("jpg") || fileExtension.equals("jpeg") || fileExtension.equals("png")) {
            return fileExtension;
        }

        log.warn("[ 사진 정보 추출 ] 사진이 아닙니다.");
        throw new S3Exception(S3ErrorCode.NOT_IMAGE_FILE);
    }
}
