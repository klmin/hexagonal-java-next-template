package com.example.template.item.domain;

import com.example.template.common.error.DomainValidationException;
import java.time.Instant;
import java.util.Objects;

public final class Item {

    public static final int NAME_MAX_LENGTH = 100;
    public static final int DESCRIPTION_MAX_LENGTH = 500;

    private final Long id;
    private final String name;
    private final String description;
    private final Instant createdAt;

    private Item(Long id, String name, String description, Instant createdAt) {
        this.id = id;
        this.name = validateName(name);
        this.description = validateDescription(description);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public static Item create(String name, String description, Instant createdAt) {
        return new Item(null, name, description, createdAt);
    }

    public static Item restore(Long id, String name, String description, Instant createdAt) {
        return new Item(Objects.requireNonNull(id, "id"), name, description, createdAt);
    }

    /** 이름, 설명을 바꾼 새 Item을 돌려준다. 저장된(id가 있는) Item만 바꿀 수 있다. */
    public Item update(String name, String description) {
        if (id == null) {
            throw new IllegalStateException("저장되지 않은 Item은 수정할 수 없다");
        }
        return new Item(id, name, description, createdAt);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainValidationException("name must not be blank");
        }
        String trimmed = name.strip();
        if (trimmed.length() > NAME_MAX_LENGTH) {
            throw new DomainValidationException("name must be at most " + NAME_MAX_LENGTH + " characters");
        }
        return trimmed;
    }

    private static String validateDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String trimmed = description.strip();
        if (trimmed.length() > DESCRIPTION_MAX_LENGTH) {
            throw new DomainValidationException(
                    "description must be at most " + DESCRIPTION_MAX_LENGTH + " characters");
        }
        return trimmed;
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
