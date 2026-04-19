package com.microservices.jury_service.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoleJury {
    PRESIDENT("président"),
    RAPPORTEUR("rapporteur"),
    EXAMINATEUR("examinateur");

    private final String value;

    RoleJury(String value) {
        this.value = value;
    }

    @JsonValue // Utilisé pour l'affichage dans Swagger et l'enregistrement dans MongoDB
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator // Utilisé pour transformer le JSON entrant (ex: "président") en Enum
    public static RoleJury fromValue(String value) {
        for (RoleJury role : RoleJury.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rôle inconnu: " + value);
    }
}