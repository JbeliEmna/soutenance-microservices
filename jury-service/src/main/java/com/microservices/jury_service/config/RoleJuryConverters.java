package com.microservices.jury_service.config;

import com.microservices.jury_service.entity.RoleJury;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

public class RoleJuryConverters {

    @ReadingConverter
    public static class StringToRoleJuryConverter implements Converter<String, RoleJury> {
        @Override
        public RoleJury convert(String source) {
            return RoleJury.fromValue(source); // utilise ton fromValue existant
        }
    }

    @WritingConverter
    public static class RoleJuryToStringConverter implements Converter<RoleJury, String> {
        @Override
        public String convert(RoleJury source) {
            return source.toString(); // retourne "président", "rapporteur", etc.
        }
    }
}