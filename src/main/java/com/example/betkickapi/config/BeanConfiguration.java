package com.example.betkickapi.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for creating and configuring beans used in the application.
 */
@Configuration
public class BeanConfiguration {

    /**
     * Creates a new instance of {@link RestTemplate}.
     *
     * @return A new {@link RestTemplate} instance.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Creates a new instance of {@link ModelMapper}.
     *
     * @return A new {@link ModelMapper} instance.
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
