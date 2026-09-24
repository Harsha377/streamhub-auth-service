package com.streamhub.auth.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "cookie")
public class CookieProperties {

    @NotBlank
    private String domain;
    @NotBlank
    private String sameSite;
    private boolean secure;
    private boolean httpOnly=true;
}
