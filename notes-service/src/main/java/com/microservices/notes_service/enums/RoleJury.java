package com.microservices.notes_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum RoleJury {
    PRESIDENT,
    RAPPORTEUR,
    EXAMINATEUR;

    @JsonCreator
    public static RoleJury decode(final String code) {
        return Stream.of(RoleJury.values())
                .filter(targetEnum -> targetEnum.name().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    @Override
    public String toString() {
        return name();
    }
}
