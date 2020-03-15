package com.hollandjake.dogbot.controller;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.text.MessageFormat;

@Slf4j
@Controller
public class MailController {
    private final JavaMailSender emailSender;
    private WebController webController;
    @Value("${spring.mail.target}")
    private String to;

    @Value("${spring.mail.from}")
    private String from;

    @Autowired
    public MailController(WebController webController, JavaMailSender emailSender) {
        this.webController = webController;
        this.emailSender = emailSender;
    }

    public void send(Throwable e) {
        try {
            MimeMessage message = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject("Chatbot error");
            helper.setText(MessageFormat.format("<h1>Failure</h1><h2>{0}</h2><p>{1}</p>",
                    e.getMessage(),
                    ExceptionUtils.getStackTrace(e)
            ), true);

            DataSource source = new FileDataSource(webController.screenshot());
            helper.addAttachment("Screenshot of error", source);

            emailSender.send(message);
            log.info("Message sent successfully");

        } catch (MessagingException ex) {
            log.error("Failed to send message", ex);
        }
    }
}
