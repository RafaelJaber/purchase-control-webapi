package br.psi.giganet.api.purchase.common.emails.services;

import br.psi.giganet.api.purchase.config.project_property.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Arrays;

@Service
public class EmailService {

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleMessage(String subject, String content, String from, String... to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        if (properties.getEnableMail()) {
            mailSender.send(message);
        } else {
            System.out.println("=========== MAIL: Mail sender request. \n" +
                    "From: "+ from + "\n" +
                    "To: "+ Arrays.toString(to) + "\n" +
                    "Subject: " + subject + "\n" +
                    "Content: " + content + "\n");
        }

    }

    public void sendEmail(String subject, String content, File file, String from, String... to) throws MessagingException {
        sendEmail(subject, content, file, true, from, to);
    }

    public void sendEmailAsHtml(String subject, String content, String from, String... to) throws MessagingException {
        sendEmail(subject, content, null, true, from, to);
    }

    public void sendEmail(String subject, String content, File file, boolean html, String from, String... to) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, html);

        if (file != null) {
            helper.addAttachment(file.getName(), file);
        }

        if (properties.getEnableMail()) {
            mailSender.send(mimeMessage);
        } else {
            System.out.println("=========== MAIL: Mail sender request. \n" +
                    "From: "+ from + "\n" +
                    "To: "+ Arrays.toString(to) + "\n" +
                    "Subject: " + subject + "\n" +
                    "Content: " + content + "\n");
        }

    }

}
