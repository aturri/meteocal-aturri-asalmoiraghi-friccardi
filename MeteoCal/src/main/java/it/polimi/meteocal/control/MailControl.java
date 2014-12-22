package it.polimi.meteocal.control;

import java.io.UnsupportedEncodingException;
import javax.annotation.Resource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author Fabiuz
 */
public class MailControl {
    
    @Resource(name = "mail/mailSession")
    private Session mailSession;
    
    public void sendMail(String destination, String nameOfDestination, String subject, String message) throws MessagingException, UnsupportedEncodingException{
        Message msg = new MimeMessage(mailSession);
        msg.setSubject(subject);

        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(destination, nameOfDestination));

        msg.setFrom(new InternetAddress("afa.meteocal@gmail.com", "MeteoCal's Team"));
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(message);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        msg.setContent(multipart);

        Transport.send(msg);
    }
}
