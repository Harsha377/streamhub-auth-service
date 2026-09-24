package com.streamhub.auth.util;

import java.security.SecureRandom;

public final class OtpGenerator {
    private static final SecureRandom RANDOM=new SecureRandom();
    private OtpGenerator(){

    }
    public static String generateOtp(int length){
      int bound=(int) Math.pow(10,length);
      int min=bound/10;
      int otp=RANDOM.nextInt(bound-min)+min;
      return String.valueOf(otp);
    }
}
