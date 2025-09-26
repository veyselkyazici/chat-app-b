package com.vky.service;

import com.vky.dto.request.SendInvitationDTO;
import com.vky.exception.InvitationException;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static com.vky.utils.MailUtils.getVerificationUrl;

@Service
@RequiredArgsConstructor
public class MailService {
    public static final String NEW_USER_ACCOUNT_VERIFICATION = "New User Account Verification";
    public static final String FORGOT_PASSWORD = "Forgot Password";
    public static final String UTF_8_ENCODING = "UTF-8";
    public static final String EMAIL_TEMPLATE = "emailtemplate";
    public static final String FORGOT_PASSWORD_TEMPLATE = "forgotpasswordtemplate";
    public static final String INVITATION_TEMPLATE = "invitationtemplate";
    public static final String TEXT_HTML_ENCONDING = "text/html";
    public static final String INVITATION = "APP Invitation";

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;
    @Value("${spring.mail.verify.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendHtmlEmailWithEmbeddedFiles(String email, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);

            helper.setPriority(3);
            helper.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
            helper.setFrom(fromEmail);
            helper.setTo(email);

            Context context = new Context();
            context.setVariables(Map.of(
                    "name", email,
                    "url", getVerificationUrl(host, token)
            ));
            String htmlContent = templateEngine.process(EMAIL_TEMPLATE, context);

            String plainText = "Hello " + email + ",\n"
                    + "Please verify your account by clicking this link: "
                    + getVerificationUrl(host, token);

            MimeMultipart mimeMultipart = new MimeMultipart("alternative");

            BodyPart textPart = new MimeBodyPart();
            textPart.setText(plainText);
            mimeMultipart.addBodyPart(textPart);

            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, TEXT_HTML_ENCONDING + "; charset=" + UTF_8_ENCODING);
            mimeMultipart.addBodyPart(htmlPart);

            message.setContent(mimeMultipart);
            mailSender.send(message);

        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Async
    public void sendHtmlEmailWithEmbeddedFilesForgotPassword(String email, String oneTimePassword) {
        try {
            MimeMessage message = getMimeMessage();

            MimeMultipart mimeMultipart = new MimeMultipart("alternative");

            String plainText = "Hello " + email + ",\n"
                    + "Your one-time password is: " + oneTimePassword;
            BodyPart textPart = new MimeBodyPart();
            textPart.setText(plainText);
            mimeMultipart.addBodyPart(textPart);

            Context context = new Context();
            context.setVariables(Map.of("name", email, "oneTimePassword", oneTimePassword));
            String htmlContent = templateEngine.process(FORGOT_PASSWORD_TEMPLATE, context);

            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, TEXT_HTML_ENCONDING + "; charset=" + UTF_8_ENCODING);
            mimeMultipart.addBodyPart(htmlPart);

            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
            helper.setPriority(3);
            helper.setSubject(FORGOT_PASSWORD);
            helper.setFrom(fromEmail);
            helper.setTo(email);

            message.setContent(mimeMultipart);

            mailSender.send(message);

        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Async
    public void sendInvitationEmail(SendInvitationDTO sendInvitationDTO) {
        try {
            MimeMessage message = getMimeMessage();

            MimeMultipart mimeMultipart = new MimeMultipart("alternative");

            String plainText = "Hello " + sendInvitationDTO.getInviteeEmail() + ",\n"
                    + sendInvitationDTO.getInviterEmail() + " has invited you to join vkychatapp.\n"
                    + "Please visit: " + host;
            BodyPart textPart = new MimeBodyPart();
            textPart.setText(plainText);
            mimeMultipart.addBodyPart(textPart);

            Context context = new Context();
            context.setVariables(Map.of(
                    "name", sendInvitationDTO.getInviteeEmail(),
                    "inviterEmail", sendInvitationDTO.getInviterEmail(),
                    "url", host
            ));
            String htmlContent = templateEngine.process(INVITATION_TEMPLATE, context);

            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, TEXT_HTML_ENCONDING + "; charset=" + UTF_8_ENCODING);
            mimeMultipart.addBodyPart(htmlPart);

            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
            helper.setPriority(3);
            helper.setSubject(INVITATION);
            helper.setFrom(fromEmail);
            helper.setTo(sendInvitationDTO.getInviteeEmail());

            message.setContent(mimeMultipart);

            mailSender.send(message);

        } catch (Exception exception) {
            throw new InvitationException("An error occurred while sending the invitation.");
        }
    }

    private MimeMessage getMimeMessage() {
        return mailSender.createMimeMessage();
    }
}
