package com.microservices.planning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();

        // 1. Convertisseur de String (MongoDB) vers LocalTime (Java)
        converters.add(new Converter<String, LocalTime>() {
            @Override
            public LocalTime convert(String source) {
                return LocalTime.parse(source); // Transforme "09:00:00" en LocalTime
            }
        });

        // 2. Convertisseur de LocalTime (Java) vers String (MongoDB)
        converters.add(new Converter<LocalTime, String>() {
            @Override
            public String convert(LocalTime source) {
                return source.toString(); // Transforme LocalTime en "09:00:00"
            }
        });

        return new MongoCustomConversions(converters);
    }
}