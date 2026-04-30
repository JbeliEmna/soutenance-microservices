package com.microservices.jury_service.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.text.Normalizer;
import java.util.Locale;

public enum RoleJury {
    PRESIDENT("president"),
    RAPPORTEUR("rapporteur"),
    EXAMINATEUR("examinateur");

    private final String value;

    RoleJury(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static RoleJury fromValue(String value) {
        String normalized = normalize(value);
        for (RoleJury role : RoleJury.values()) {
            if (normalize(role.value).equals(normalized) || role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Role jury inconnu: " + value);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
