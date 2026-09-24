package com.streamhub.auth.controller.test;

import com.streamhub.auth.service.EmailService;
import com.streamhub.auth.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    private final EmailService emailService;
    private final OtpService otpService;

    @GetMapping("/send-otp")
    public String sendOtp(@RequestParam String email) {

        otpService.sendOtp(email);

        return "OTP Sent Successfully";
    }

    @GetMapping("/mail")
    public String sendMail(){
        emailService.sendOtpEmail("harshavardhanch07@gmail.com","Harshavardhan","458921");
        return "Mail Sent";
    }
}
