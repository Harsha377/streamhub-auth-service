package com.streamhub.auth.service;

public interface EmailService {
    void sendOtpEmail(String toEmail,String name,String otp);
}
