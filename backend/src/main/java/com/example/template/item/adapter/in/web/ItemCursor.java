package com.example.template.item.adapter.in.web;

import com.example.template.common.error.DomainValidationException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// 클라이언트에는 커서 내용을 숨긴다(불투명 문자열). 나중에 정렬 기준이 바뀌어도 API 형태는 그대로 둘 수 있다
final class ItemCursor {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private ItemCursor() {}

    static String encode(Long id) {
        return id == null ? null : ENCODER.encodeToString(Long.toString(id).getBytes(StandardCharsets.UTF_8));
    }

    static Long decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            long id = Long.parseLong(new String(DECODER.decode(cursor), StandardCharsets.UTF_8));
            if (id <= 0) {
                throw new DomainValidationException("cursor is invalid");
            }
            return id;
        } catch (IllegalArgumentException e) {
            throw new DomainValidationException("cursor is invalid");
        }
    }
}
