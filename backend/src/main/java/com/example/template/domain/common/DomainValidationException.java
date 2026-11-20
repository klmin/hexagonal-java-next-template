package com.example.template.domain.common;

/**
 * 도메인 규칙 위반을 나타낸다. 웹 어댑터에서 400 응답으로 변환된다.
 */
public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }
}
