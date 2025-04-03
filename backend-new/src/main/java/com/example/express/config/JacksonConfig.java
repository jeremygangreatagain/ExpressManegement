package com.example.express.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary // Indicate this ObjectMapper should be preferred
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Register the module for Java 8 Date/Time types
        mapper.registerModule(new JavaTimeModule());
        // Optional: Disable writing dates as timestamps for better readability
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Optional: Configure date format if needed (e.g., "yyyy-MM-dd HH:mm:ss")
        // mapper.setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        return mapper;
    }
}
