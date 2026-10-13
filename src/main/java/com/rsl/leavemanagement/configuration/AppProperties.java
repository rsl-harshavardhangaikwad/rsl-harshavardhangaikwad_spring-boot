package com.rsl.leavemanagement.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code app.*} properties from {@code application.yml} into a
 * strongly-typed, immutable record.
 *
 * <p>Example usage in any Spring bean:</p>
 * <pre>
 *   &#64;Autowired
 *   AppProperties props;
 *   String name = props.name();
 * </pre>
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String name,
        String version,
        String description,
        int maxLeaveDaysPerRequest
) {
}
