package com.ecomerce.authservice.service.impl;

import com.ecomerce.authservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    @Value("${mail.from}")
    private String fromEmail;
    
    @Override
    public void sendOtpEmail(String toEmail, String otp, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã OTP Khôi Phục Mật khẩu");

            String htmlContent = buildOtpEmailTemplate(otp, userName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }

    }

    @Override
    public String buildOtpEmailTemplate(String otp, String userName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; color: #333333; }
                    .otp-box { background-color: #f0f0f0; padding: 20px; text-align: center; border-radius: 5px; margin: 20px 0; }
                    .otp-code { font-size: 32px; font-weight: bold; color: #4CAF50; letter-spacing: 5px; }
                    .info { color: #666666; line-height: 1.6; }
                    .warning { color: #ff6b6b; font-weight: bold; margin-top: 20px; }
                    .footer { text-align: center; color: #999999; font-size: 12px; margin-top: 30px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2 class="header">Khôi phục mật khẩu</h2>
                    <p class="info">Xin chào <strong>%s</strong>,</p>
                    <p class="info">Bạn đã yêu cầu khôi phục mật khẩu, đây là mã otp của bạn:</p>
                    
                    <div class="otp-box">
                        <div class="otp-code">%s</div>
                    </div>
                    
                    <p class="info">Mã OTP này có hiệu lực trong <strong>5 phút</strong>.</p>
                 
                    <div class="footer">
                        <p>Email này gửi từ hệ thống, vui lòng không trả lời.</p>
                        <p>&copy; 2024 Ecomerce.TranAn. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, otp);
    }
}
