package com.project.teama_be.global.apiPayload.exception.handler;

import com.project.teama_be.domain.chat.exception.ChatErrorCode;
import com.project.teama_be.domain.chat.exception.ChatException;
import com.project.teama_be.global.apiPayload.CustomResponse;
import com.project.teama_be.global.apiPayload.code.BaseErrorCode;
import com.project.teama_be.global.apiPayload.code.GeneralErrorCode;
import com.project.teama_be.global.apiPayload.exception.CustomException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 컨트롤러 메서드에서 @Valid 어노테이션을 사용하여 DTO의 유효성 검사를 수행
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<CustomResponse<Map<String, String>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        // 검사에 실패한 필드와 그에 대한 메시지를 저장하는 Map
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        BaseErrorCode validationErrorCode = GeneralErrorCode.VALIDATION_FAILED; // BaseErrorCode로 통일
        CustomResponse<Map<String, String>> errorResponse = CustomResponse.onFailure(
                validationErrorCode.getCode(),
                validationErrorCode.getMessage(),
                errors
        );
        // 에러 코드, 메시지와 함께 errors를 반환
        return ResponseEntity.status(validationErrorCode.getHttpStatus()).body(errorResponse);
    }

    // 쿼리 파라미터 검증
    @ExceptionHandler(HandlerMethodValidationException.class)
    protected ResponseEntity<CustomResponse<Map<String, String>>> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getParameterValidationResults().forEach(result ->
                errors.put(result.getMethodParameter().getParameterName(), result.getResolvableErrors().get(0).getDefaultMessage()));
        BaseErrorCode validationErrorCode = GeneralErrorCode.VALIDATION_FAILED; // BaseErrorCode로 통일
        CustomResponse<Map<String, String>> errorResponse = CustomResponse.onFailure(
                validationErrorCode.getCode(),
                validationErrorCode.getMessage(),
                errors
        );
        // 에러 코드, 메시지와 함께 errors를 반환
        return ResponseEntity.status(validationErrorCode.getHttpStatus()).body(errorResponse);

    }

    // 요청 파라미터가 없을 때 발생하는 예외 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<CustomResponse<String>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex
    ) {

        log.warn("[ MissingRequestParameterException ]: 필요한 파라미터가 요청에 없습니다.");
        BaseErrorCode validationErrorCode = GeneralErrorCode.VALIDATION_FAILED; // BaseErrorCode로 통일
        CustomResponse<String> errorResponse = CustomResponse.onFailure(
                validationErrorCode.getCode(),
                validationErrorCode.getMessage(),
                ex.getParameterName()+" 파라미터가 없습니다."
        );
        // 에러 코드, 메시지와 함께 errors를 반환
        return ResponseEntity.status(validationErrorCode.getHttpStatus()).body(errorResponse);
    }

    // ConstraintViolationException 핸들러
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<CustomResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        // 제약 조건 위반 정보를 저장할 Map
        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            // 마지막 필드명만 추출 (예: user.name -> name)
            String fieldName = propertyPath.contains(".") ?
                    propertyPath.substring(propertyPath.lastIndexOf(".") + 1) : propertyPath;

            errors.put(fieldName, violation.getMessage());
        });

        BaseErrorCode constraintErrorCode = GeneralErrorCode.VALIDATION_FAILED;
        CustomResponse<Map<String, String>> errorResponse = CustomResponse.onFailure(
                constraintErrorCode.getCode(),
                constraintErrorCode.getMessage(),
                errors
        );

        log.warn("[ ConstraintViolationException ]: Constraint violations detected");

        return ResponseEntity.status(constraintErrorCode.getHttpStatus()).body(errorResponse);
    }

    //애플리케이션에서 발생하는 커스텀 예외를 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomResponse<Void>> handleCustomException(CustomException ex) {
        //예외가 발생하면 로그 기록
        log.warn("[ CustomException ]: {}", ex.getCode().getMessage());
        //커스텀 예외에 정의된 에러 코드와 메시지를 포함한 응답 제공
        return ResponseEntity.status(ex.getCode().getHttpStatus())
                .body(ex.getCode().getErrorResponse());
    }

    // ChatException 핸들러
    @ExceptionHandler(ChatException.class)
    public ResponseEntity<CustomResponse<Object>> handleChatException(ChatException exception) {
        ChatErrorCode errorCode = exception.getErrorCode();
        log.error("채팅 오류 발생: {}", errorCode.getMessage());

        // 서비스 상태 코드 유지하면서 클라이언트에게 적절한 에러 메시지 전달
        return ResponseEntity.status(HttpStatus.OK)
                .body(CustomResponse.onFailure(errorCode.getCode(), errorCode.getMessage()));
    }

    // 그 외의 정의되지 않은 모든 예외 처리
    @ExceptionHandler({Exception.class})
    public ResponseEntity<CustomResponse<String>> handleAllException(Exception ex) {
        log.error("[WARNING] Internal Server Error : {} ", ex.getMessage());
        BaseErrorCode errorCode = GeneralErrorCode.INTERNAL_SERVER_ERROR_500;
        CustomResponse<String> errorResponse = CustomResponse.onFailure(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }
}
