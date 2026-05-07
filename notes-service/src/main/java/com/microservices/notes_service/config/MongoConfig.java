package com.microservices.notes_service.config;

import com.microservices.notes_service.enums.RoleJury;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new StringToRoleJuryConverter());
        return new MongoCustomConversions(converters);
    }

    public static class StringToRoleJuryConverter implements Converter<String, RoleJury> {
        @Override
        public RoleJury convert(String source) {
            if (source == null || source.isEmpty()) {
                return null;
            }
            try {
                return RoleJury.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null; // Or handle as needed
            }
        }
    }
}
