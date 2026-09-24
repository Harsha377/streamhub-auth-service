package com.streamhub.auth.util;

public final class RedisKeyUtil {
    private static final String OTP_PREFIX="otp:user:";
    private static  final String SESSION_PREFIX="session:";
    private static final String USER_PREFIX="user:";
    private static final String LOGIN_ATTEMPT_PREFIX="login:attempt:";
    private static final String VERIFIED_PREFIX = "verify:user:";

    private RedisKeyUtil(){

    }

    public static  String otpKey(String email){
        return OTP_PREFIX+email;
    }
    public static  String sessionKey(String token){
        return SESSION_PREFIX+token;
    }
    public static String userCacheKey(Long userId) {
        return USER_PREFIX + userId;
    }

    public static String loginAttemptKey(String email) {
        return LOGIN_ATTEMPT_PREFIX + email;
    }
    public static String verifiedUserKey(String email) {
        return VERIFIED_PREFIX + email;
    }
}
