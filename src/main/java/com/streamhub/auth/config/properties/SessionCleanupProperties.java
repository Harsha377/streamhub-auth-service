package com.streamhub.auth.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "session.cleanup")
public class SessionCleanupProperties {
    /**
     * Cron expression controlling how often expired
     * user sessions are removed.
     */
    private String cron;

}
