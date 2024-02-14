package com.example.betkickapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom filter for handling CSRF tokens as cookies in HTTP responses.
 * <br>
 * <br>
 * This filter extends the Spring Security's {@link OncePerRequestFilter} and is responsible for
 * extracting the CSRF token from the request attributes and setting it as a cookie in the HTTP response headers.
 */
public class CookieCsrfFilter extends OncePerRequestFilter {

    /**
     * Performs the filtering of the request and response.
     * This method extracts the CSRF token from the request attributes and sets it as a cookie
     * in the HTTP response headers. The request is then passed through the filter chain for further processing.
     *
     * @param request     The {@link HttpServletRequest} instance representing the incoming HTTP request.
     * @param response    The {@link HttpServletResponse} instance representing the outgoing HTTP response.
     * @param filterChain The {@link FilterChain} for processing the request and response.
     * @throws ServletException If an exception occurs during the filter process.
     * @throws IOException      If an I/O exception occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        // Set CSRF token as a cookie in the response headers
        response.setHeader(csrfToken.getHeaderName(), csrfToken.getToken());

        // Continue the request through the filter chain
        filterChain.doFilter(request, response);
    }
}
