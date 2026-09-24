package com.streamhub.auth.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {
    private  String from;
    private String fromName;
    private String applicationName;
    private Integer otpValidityMinutes;
}
