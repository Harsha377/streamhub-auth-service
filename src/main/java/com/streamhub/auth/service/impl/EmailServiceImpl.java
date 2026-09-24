package com.streamhub.auth.service.impl;

import com.streamhub.auth.config.properties.MailProperties;
import com.streamhub.auth.service.EmailService;
import com.streamhub.auth.service.EmailTemplateService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final MailProperties mailProperties;
    private final JavaMailSender javaMailSender;
    private final EmailTemplateService emailTemplateService;
    @Override
    public void sendOtpEmail(String toEmail, String name, String otp) {
        try{
            Map<String,Object> variables=new HashMap<>();
            variables.put("applicationName",mailProperties.getApplicationName());
            variables.put("name",name);
            variables.put("otp",otp);
            variables.put("expiry",mailProperties.getOtpValidityMinutes());
            String html = emailTemplateService.processTemplate("email/otp-email", variables);
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setTo(toEmail);
            helper.setSubject("OTP Verification");
            helper.setFrom(
                    mailProperties.getFrom(),
                    mailProperties.getFromName()
            );
            helper.setText(html,true);
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }

    }
}
