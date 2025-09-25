package com.example.librarysystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper(); // Skapar en ny ObjectMapper instans
        mapper.registerModule(new JavaTimeModule()); // Registrerar modulen för att stödja Java 8 datum och tid
        return mapper;
    }
}

