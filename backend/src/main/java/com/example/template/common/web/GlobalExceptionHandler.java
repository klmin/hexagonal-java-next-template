package com.example.template.common.web;

import com.example.template.common.error.DomainValidationException;
import com.example.template.common.error.NotFoundException;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// 오류 응답은 RFC 9457 Problem Details 형식으로 통일
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String INVALID_INPUT = "입력값을 확인해 주세요.";

    @ExceptionHandler(DomainValidationException.class)
    ProblemDetail handleDomainValidation(DomainValidationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail handleNotFound(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 처리하지 못한 예외도 Problem Details 로 응답한다. 내부 메시지는 응답에 넣지 않고 로그로만 남긴다
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("unhandled exception", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    }

    // @Valid @RequestBody 검증 실패
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
                .toList();
        ProblemDetail body = ex.getBody();
        body.setDetail(INVALID_INPUT);
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    // @RequestParam, @PathVariable 등에 붙인 제약(@Min, @Max ...) 검증 실패.
    // 같은 메서드에 이런 제약이 있으면 @RequestBody 검증 실패도 이쪽으로 온다
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldError> errors = ex.getParameterValidationResults().stream()
                .flatMap(GlobalExceptionHandler::toFieldErrors)
                .toList();
        ProblemDetail body = ex.getBody();
        body.setDetail(INVALID_INPUT);
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    private static Stream<FieldError> toFieldErrors(ParameterValidationResult result) {
        if (result instanceof ParameterErrors errors) {
            return errors.getFieldErrors().stream().map(e -> new FieldError(e.getField(), e.getDefaultMessage()));
        }
        String name = result.getMethodParameter().getParameterName();
        return result.getResolvableErrors().stream().map(e -> new FieldError(name, e.getDefaultMessage()));
    }

    record FieldError(String field, String message) {}
}
