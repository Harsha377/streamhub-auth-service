package com.streamhub.auth.config.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.otp")
public class OtpProperties {

    private int length;
    private int expiryMinutes;
}
