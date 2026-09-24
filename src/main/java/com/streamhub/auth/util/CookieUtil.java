package com.streamhub.auth.util;

import com.streamhub.auth.config.properties.CookieProperties;
import com.streamhub.auth.config.properties.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Utility responsible for creating and deleting authentication cookies.
 * <p>
 * Responsibilities:
 * - Create ACCESS_TOKEN cookie
 * - Create REFRESH_TOKEN cookie
 * - Delete ACCESS_TOKEN cookie
 * - Delete REFRESH_TOKEN cookie
 * <p>
 * This class never generates JWTs or refresh tokens.
 * It only converts token values into secure HTTP cookies.
 */
@Component
@RequiredArgsConstructor
public class CookieUtil {
    private static final String ACCESS_TOKEN_COOKIE ="ACCESS_TOKEN";
    private static final String REFRESH_TOKEN_COOKIE="REFRESH_TOKEN";
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

    /**
     * Creates HttpOnly cookie for Access Token.
     */
    public ResponseCookie createAccessTokenCookie(String accessToken){
        return buildCookie(ACCESS_TOKEN_COOKIE,accessToken,jwtProperties.getAccessTokenExpiry()/1000);
    }
    /**
     * Creates HttpOnly cookie for Refresh Token.
     */
    public ResponseCookie createRefreshTokenCookie(String refreshToken){
        return buildCookie(REFRESH_TOKEN_COOKIE,refreshToken,jwtProperties.getRefreshTokenExpiry()/1000);
    }
    /**
     * Deletes Access Token cookie.
     */
    public ResponseCookie deleteAccessTokenCookie(){
        return buildCookie(ACCESS_TOKEN_COOKIE,"",0);
    }
    /**
     * Deletes Refresh Token cookie.
     */
    public ResponseCookie deleteRefreshTokenCookie(){
        return buildCookie(REFRESH_TOKEN_COOKIE,"",0);
    }

    /**
     * Returns the value of the requested cookie.
     * <p>
     * Used by:
     * - Refresh Token API
     * - Logout API
     * - JwtAuthenticationFilter (optional)
     * <p>
     * Returns null if the cookie does not exist.
     */
    public String getCookieValue(
            HttpServletRequest request,
            String cookieName
    ) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }

        }

        return null;
    }

    /**
     * Common cookie builder used by all cookie operations.
     * <p>
     * Why?
     * <p>
     * Without this method,
     * every cookie creation would repeat the same configuration.
     * <p>
     * Following the DRY principle,
     * all common cookie attributes are centralized here.
     */
    private ResponseCookie buildCookie(String name,String value,long maxAge){
        return ResponseCookie.from(name,value)
                .httpOnly(cookieProperties.isHttpOnly())
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .domain(cookieProperties.getDomain())
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}


/*
====================================================================================
WHY IS CookieUtil A SPRING BEAN (@Component)?
====================================================================================

General Rule:

If a class depends only on Java classes
(String, UUID, SecureRandom, Base64, MessageDigest, Collections, etc.)

→ Keep it as a plain Utility Class.
→ No @Component.
→ No object creation by Spring.
→ Usually contains static methods.

Example:

public final class RefreshTokenGenerator {

    public static String generate() {
        ...
    }

}

Reason:
This class does not require any Spring-managed objects.
Everything it needs comes from the Java Standard Library.

------------------------------------------------------------------------------------

If a class depends on Spring-managed objects (Beans)

Examples:
- @ConfigurationProperties
- Repository
- @Service
- RedisTemplate
- JavaMailSender
- ObjectMapper (Spring Bean)
- Environment
- RestTemplate
- WebClient
- JwtProperties
- CookieProperties

→ It SHOULD become a Spring Bean.

Example:

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final CookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

}

Reason:
CookieUtil needs configuration values loaded from application.yml.

Spring creates:

CookieProperties Bean
JwtProperties Bean

and injects them into CookieUtil using Constructor Injection.

------------------------------------------------------------------------------------

What happens if CookieUtil is NOT a Spring Bean?

Suppose we write:

public final class CookieUtil {

    private final CookieProperties cookieProperties;

}

Now we try:

CookieUtil cookieUtil = new CookieUtil();

Compiler Error

Why?

Constructor requires:

CookieProperties
JwtProperties

Where will these objects come from?

Only Spring knows how to create and configure them.

------------------------------------------------------------------------------------

Can we create them manually?

CookieProperties properties = new CookieProperties();

JwtProperties jwt = new JwtProperties();

CookieUtil util = new CookieUtil(properties, jwt);

Technically:
YES

Practically:
NO

Reason:

These objects are NOT populated from application.yml.

Example:

application.yml

cookie:
  secure: true

jwt:
  issuer: streamhub-auth

When Spring creates these beans:

application.yml
        │
        ▼
@ConfigurationProperties
        │
        ▼
CookieProperties Bean

All fields are automatically populated.

If we create them using "new",

cookieProperties.getSecure()

returns

false (default)

issuer

returns null

because Spring never configured them.

------------------------------------------------------------------------------------

Easy Rule to Remember

Does this class require dependency injection?

YES
↓

Spring Bean

@Component
@Service
@Repository

NO
↓

Plain Java Utility Class

static methods

No @Component

------------------------------------------------------------------------------------

Examples From This Project

Spring Beans
------------
CookieUtil
JwtServiceImpl
OtpServiceImpl
AuthenticationService
EmailServiceImpl

Utility Classes
---------------
OtpGenerator
RedisKeyUtil
RefreshTokenGenerator
RefreshTokenHashUtil

------------------------------------------------------------------------------------

Rule Followed Throughout This Project

✓ Business Services           → Spring Beans
✓ Repository Layer            → Spring Beans
✓ Configuration Classes       → Spring Beans
✓ Helper / Utility Classes    → Plain Java Classes
✓ Classes requiring Spring DI → Spring Beans

This keeps the project clean, testable, maintainable, and follows
Spring Boot production best practices.
====================================================================================

If you want Spring to inject dependencies into a class
(constructor injection, @Autowired, etc.),
then that class itself must be managed by Spring.

@Component tells Spring to register this class as a bean in the IoC Container.

Once the class becomes a Spring Bean:

✓ Spring creates the object.
✓ Spring manages the object lifecycle.
✓ Spring performs constructor injection (@RequiredArgsConstructor).
✓ Spring can inject other Spring Beans into this class.
✓ Other Spring Beans can also inject this class.

If you want Spring to perform dependency injection
(constructor injection, @Autowired, etc.),
the target class must itself be managed by the Spring IoC Container.
*/
