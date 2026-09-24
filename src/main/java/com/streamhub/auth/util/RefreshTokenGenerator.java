package com.streamhub.auth.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class RefreshTokenGenerator {
    private static final SecureRandom SECURE_RANDOM=new SecureRandom();
    private static final int TOKEN_LENGTH=32;
    private RefreshTokenGenerator(){

    }
    public static String generate(){
        byte[] bytes=new byte[TOKEN_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
