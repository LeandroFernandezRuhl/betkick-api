package com.leandroruhl.betkickapi.config.security;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;

/**
 * Configuration class for setting up and customizing Cross-Origin Resource Sharing (CORS).
 * The class defines a bean method {@code corsFilter()} that creates and configures a CORS filter for handling cross-origin requests.
 */
@Configuration
@EnableWebMvc
public class WebConfiguration {

    /**
     * Maximum age (in seconds) for which the CORS preflight response should be cached.
     */
    private static final Long MAX_AGE = 3600L;

    /**
     * Order of the CORS filter in the filter chain. Should be set to -100 to ensure it runs before Spring Security filter.
     */
    private static final int CORS_FILTER_ORDER = -102;

    /**
     * Creates and configures a CORS filter to handle cross-origin requests.
     *
     * @return A {@code FilterRegistrationBean} containing the configured CORS filter.
     */
    @Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("https://betkick.vercel.app");
        config.addAllowedOrigin("https://betkick.leandroruhl.com");
        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT));
        config.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name()));
        config.setMaxAge(MAX_AGE);
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));

        bean.setOrder(CORS_FILTER_ORDER);
        return bean;
    }
}