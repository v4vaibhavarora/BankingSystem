package com.banking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${bank.mail.from:noreply@securebank.com}")
    private String fromEmail;

    @Value("${bank.name:SecureBank}")
    private String bankName;

    @Async
    public void sendApprovalEmail(String toEmail, String customerName, String accountNumber, String tempPassword) {
        if (mailSender == null) {
            System.out.println("=== EMAIL (mail not configured) ===");
            System.out.println("TO: " + toEmail);
            System.out.println("Account: " + accountNumber + " | Password: " + tempPassword);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
            h.setFrom(fromEmail);
            h.setTo(toEmail);
            h.setSubject(bankName + " – Account Approved! Your Login Details");
            h.setText(buildApprovalHtml(customerName, accountNumber, tempPassword), true);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Email send failed: " + e.getMessage());
        }
    }

    @Async
    public void sendFreezeEmail(String toEmail, String name, boolean frozen) {
        if (mailSender == null) return;
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
            h.setFrom(fromEmail);
            h.setTo(toEmail);
            h.setSubject(bankName + " – Account " + (frozen ? "Frozen" : "Unfrozen"));
            String body = "<div style='font-family:Arial;padding:20px'><h2 style='color:" + (frozen ? "#e74c3c" : "#27ae60") + "'>"
                + (frozen ? "❄️ Account Frozen" : "✅ Account Activated") + "</h2>"
                + "<p>Dear <b>" + name + "</b>, your account has been <b>" + (frozen ? "frozen" : "unfrozen") + "</b> by the bank.</p>"
                + (frozen ? "<p>You cannot transact until unfrozen. Contact support for help.</p>" : "<p>Your account is now active.</p>")
                + "</div>";
            h.setText(body, true);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Email send failed: " + e.getMessage());
        }
    }

    private String buildApprovalHtml(String name, String acct, String pwd) {
        return "<!DOCTYPE html><html><body style='margin:0;font-family:Arial,sans-serif;background:#f0f4f8'>"
            + "<div style='max-width:600px;margin:40px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1)'>"
            + "<div style='background:linear-gradient(135deg,#0f2b5c,#1565c0);padding:40px;text-align:center'>"
            + "<h1 style='color:white;margin:0;font-size:28px'>🏦 " + bankName + "</h1>"
            + "<p style='color:#90caf9;margin:8px 0 0'>Secure Banking, Simplified</p></div>"
            + "<div style='padding:40px'>"
            + "<h2 style='color:#0f2b5c'>Congratulations, " + name + "! 🎉</h2>"
            + "<p style='color:#555;line-height:1.6'>Your KYC has been verified and your bank account is now <strong>active</strong>.</p>"
            + "<div style='background:#f8faff;border:2px solid #1565c0;border-radius:8px;padding:24px;margin:24px 0'>"
            + "<h3 style='color:#0f2b5c;margin-top:0'>🔐 Your Login Credentials</h3>"
            + "<table style='width:100%'>"
            + "<tr><td style='color:#888;padding:8px 0;width:160px'>Account Number:</td>"
            + "<td style='font-weight:bold;font-size:22px;color:#0f2b5c;letter-spacing:3px'>" + acct + "</td></tr>"
            + "<tr><td style='color:#888;padding:8px 0'>Temp Password:</td>"
            + "<td style='font-family:monospace;font-weight:bold;font-size:18px;color:#c62828;background:#fff3e0;padding:4px 10px;border-radius:4px'>" + pwd + "</td></tr>"
            + "</table></div>"
            + "<div style='background:#fff8e1;border-left:4px solid #f9a825;padding:16px;border-radius:4px'>"
            + "<strong>⚠️ Security Tips</strong><ul style='margin:8px 0 0;padding-left:20px;color:#555'>"
            + "<li>Login and change your password immediately</li>"
            + "<li>Never share credentials with anyone</li></ul></div>"
            + "<p style='margin-top:24px'>👉 Login at: <a href='http://localhost:8080/customer/login' style='color:#1565c0'>http://localhost:8080/customer/login</a></p>"
            + "</div>"
            + "<div style='background:#0f2b5c;padding:16px;text-align:center'>"
            + "<p style='color:#90caf9;margin:0;font-size:12px'>© 2024 " + bankName + " | All rights reserved</p></div>"
            + "</div></body></html>";
    }
}
