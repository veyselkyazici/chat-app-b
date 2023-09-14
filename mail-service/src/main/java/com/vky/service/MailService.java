package com.vky.service;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
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

import static com.vky.utils.EmailUtils.getVerificationUrl;

@Service
@RequiredArgsConstructor
public class MailService {
    public static final String NEW_USER_ACCOUNT_VERIFICATION = "New User Account Verification";
    public static final String FORGOT_PASSWORD = "Forgot Password";
    public static final String UTF_8_ENCODING = "UTF-8";
    public static final String EMAIL_TEMPLATE = "emailtemplate";
    public static final String FORGOT_PASSWORD_TEMPLATE = "forgotpasswordtemplate";
    public static final String TEXT_HTML_ENCONDING = "text/html";

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;
    @Value("${spring.mail.verify.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String fromEmail;

//    @Async
//    public void sendHtmlEmailWithEmbeddedFiles(String name, String to, String token) {
//        try {
//            MimeMessage message = getMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
//            helper.setPriority(1);
//            helper.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
//            helper.setFrom(fromEmail);
//            helper.setTo(to);
//            //helper.setText(text, true);
//            Context context = new Context();
//            context.setVariables(Map.of("name", name, "url", getVerificationUrl(host, token)));
//            String text = templateEngine.process(EMAIL_TEMPLATE, context);
//
//            // Add HTML email body
//            MimeMultipart mimeMultipart = new MimeMultipart("related");
//            BodyPart messageBodyPart = new MimeBodyPart();
//            messageBodyPart.setContent(text, TEXT_HTML_ENCONDING);
//            mimeMultipart.addBodyPart(messageBodyPart);
//
//            // Add images to the email body
//            BodyPart imageBodyPart = new MimeBodyPart();
//            DataSource dataSource = new FileDataSource(System.getProperty("user.home") + "/Downloads/picture.jpg");
//            imageBodyPart.setDataHandler(new DataHandler(dataSource));
//            imageBodyPart.setHeader("Content-ID", "image");
//            mimeMultipart.addBodyPart(imageBodyPart);
//            message.setContent(mimeMultipart);
//            mailSender.send(message);
//        } catch (Exception exception) {
//            System.out.println(exception.getMessage());
//            throw new RuntimeException(exception.getMessage());
//        }
//
//    }
    @Async
    public void sendHtmlEmailWithEmbeddedFiles(String email, String token) {
        try {
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
            helper.setPriority(1);
            helper.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
            helper.setFrom(fromEmail);
            helper.setTo(email);
            //helper.setText(text, true);
            Context context = new Context();
            context.setVariables(Map.of("name", email, "url", getVerificationUrl(host, token)));
            String text = templateEngine.process(EMAIL_TEMPLATE, context);


            MimeMultipart mimeMultipart = new MimeMultipart("related");
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(text, TEXT_HTML_ENCONDING);
            mimeMultipart.addBodyPart(messageBodyPart);


            BodyPart imageBodyPart = new MimeBodyPart();
            DataSource dataSource = new FileDataSource(System.getProperty("user.home") + "/Downloads/picture.jpg");
            imageBodyPart.setDataHandler(new DataHandler(dataSource));
            imageBodyPart.setHeader("Content-ID", "image");
            mimeMultipart.addBodyPart(imageBodyPart);
            message.setContent(mimeMultipart);
            mailSender.send(message);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }

    }

    @Async
    public void sendHtmlEmailWithEmbeddedFilesForgotPassword(String email, String oneTimePassword) {
        try {
            MimeMessage message = getMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8_ENCODING);
            helper.setPriority(1);
            helper.setSubject(FORGOT_PASSWORD);
            helper.setFrom(fromEmail);
            helper.setTo(email);
            //helper.setText(text, true);
            Context context = new Context();
            context.setVariables(Map.of("name", email, "oneTimePassword", oneTimePassword));
            String text = templateEngine.process(FORGOT_PASSWORD_TEMPLATE, context);

            // Add HTML email body
            MimeMultipart mimeMultipart = new MimeMultipart("related");
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(text, TEXT_HTML_ENCONDING);
            mimeMultipart.addBodyPart(messageBodyPart);

            // Add images to the email body
            BodyPart imageBodyPart = new MimeBodyPart();
            DataSource dataSource = new FileDataSource(System.getProperty("user.home") + "/Downloads/picture.jpg");
            imageBodyPart.setDataHandler(new DataHandler(dataSource));
            imageBodyPart.setHeader("Content-ID", "image");
            mimeMultipart.addBodyPart(imageBodyPart);
            message.setContent(mimeMultipart);
            mailSender.send(message);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }

    }

    private MimeMessage getMimeMessage() {
        return mailSender.createMimeMessage();
    }

    private String getContentId(String filename) {
        return "<" + filename + ">";
    }
}
