/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.EventManager;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.UnsupportedEncodingException;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
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
@Stateless
public class MailController {

    @Resource(name = "mail/mailSession")
    private Session mailSession;
    
    @EJB
    UserManager userManager;
    
    private final String meteocalsEmail="afa.meteocal@gmail.com";
    private final String meteocalsName="MeteoCal's Team";
    private final Map<KindOfEmail,String> subjects=new EnumMap<>(KindOfEmail.class);
    private final Map<KindOfEmail,String> messages=new EnumMap<>(KindOfEmail.class);
    
    public void sendMail(String destination, String nameOfDestination, String subject, String message) throws MessagingException, UnsupportedEncodingException{
        User user=userManager.findByEmail(destination);
        System.out.println(user.getEmail());
        
        Message msg = new MimeMessage(mailSession);
        msg.setSubject(subject);
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(destination, nameOfDestination));
        msg.setFrom(new InternetAddress(this.meteocalsEmail, this.meteocalsName));
        //either
        //msg.setContent(message, "text/plain");
        //either
        msg.setContent(message, "text/html; charset=utf-8");
        Transport.send(msg);
        System.out.println("I've sent an email to "+ destination);
    }
}
