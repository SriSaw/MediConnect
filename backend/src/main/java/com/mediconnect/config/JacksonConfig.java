package com.mediconnect.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * Jackson configuration ensuring robust date and time serialization/deserialization.
 * Supports both HH:mm and HH:mm:ss input formats across all REST APIs,
 * preventing serialization failures caused by variable client time input representations.
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter LOCAL_TIME_PARSER = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm")
            .optionalStart()
            .appendPattern(":ss")
            .optionalEnd()
            .toFormatter();

    private static final DateTimeFormatter LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.deserializerByType(LocalTime.class, new JsonDeserializer<LocalTime>() {
                @Override
                public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String value = p.getText();
                    if (value == null || value.trim().isEmpty()) {
                        return null;
                    }
                    return LocalTime.parse(value.trim(), LOCAL_TIME_PARSER);
                }
            });
            builder.serializerByType(LocalTime.class, new LocalTimeSerializer(LOCAL_TIME_FORMATTER));
        };
    }
}
