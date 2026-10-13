package com.rsl.leavemanagement.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Central Spring configuration class.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Enables {@link AppProperties} binding via {@link EnableConfigurationProperties}.</li>
 *   <li>Registers a request-logging filter so that every incoming HTTP request is logged
 *       at DEBUG level (useful in the {@code dev} profile).</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {

    /**
     * Logs incoming HTTP requests (method, URI, query string, payload) for debugging.
     * Activated only when the logging level for this filter is set to DEBUG in the profile.
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(1000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }
}
