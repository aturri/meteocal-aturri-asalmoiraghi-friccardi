package it.polimi.meteocal.control;

import java.io.UnsupportedEncodingException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author Fabiuz
 */
public class MailControl {
    
    private final Session mailSession;
    
    public MailControl(Session mailSession){
        this.mailSession=mailSession;
    }
    
    /**
     * This method send the eMail in text/plain format
     * @param destination recipient's email address
     * @param nameOfDestination recipient's name
     * @param subject Subject of eMail
     * @param message Message of eMail
     * @throws MessagingException
     * @throws UnsupportedEncodingException 
     */
    public void sendMail(String destination, String nameOfDestination, String subject, String message) throws MessagingException, UnsupportedEncodingException{
        Message msg = new MimeMessage(mailSession);
        msg.setSubject(subject);
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(destination, nameOfDestination));
        msg.setFrom(new InternetAddress("afa.meteocal@gmail.com", "MeteoCal's Team"));
        //either
        //msg.setContent(message, "text/plain");
        //either
        msg.setContent(message, "text/html; charset=utf-8");
        Transport.send(msg);
        System.out.println("I've sent an email to "+ destination);
    }
}