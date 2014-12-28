/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.EventManager;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.UnsupportedEncodingException;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.PostConstruct;
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
    
    @PostConstruct
    private void init(){
        subjects.put(KindOfEmail.REGISTRATION, "Confirm registration");
        messages.put(KindOfEmail.REGISTRATION, ""
                + "Congraturation %s %s,<br />"
                + "Your account is registred succefully!<br />"
                + "Best regards,<br />"
                + "       MeteoCal's Team");
        subjects.put(KindOfEmail.INVITEDTOEVENT, "Invite to partecipate to %s");
        messages.put(KindOfEmail.INVITEDTOEVENT, ""
                + "Dear %s %s,<br />"
                + "You are invited from %s %s to partecipate to %s. Click on the follow link to see more details:<br />"
                + "<br />"
                + "<a href=\"http://localhost:8080/MeteoCal/event/detail.xhtml?id=%d\">%s</a><br />"
                + "<br />"
                + "Enjoy it ;)<br />"
                + "      MeteoCal's Team");
        subjects.put(KindOfEmail.FORGOTTENPASSWORD, "Recover your Meteocal's password");
        messages.put(KindOfEmail.FORGOTTENPASSWORD, ""
                + "Dear %s %s,<br />"
                + "We have see your request to change your password because you have forgotten it. <br />"
                + "Now you just click on the follow link to set a new password and come back to MeteoCal<br />"
                + "<br />"
                + "<a href=\"%s\">%s</a>"
                + "<br /><br />"
                + "If you haven't registed to MeteoCal or if you haven't required to change your password, ignore this eMail.<br />"
                + "Best regards,<br />"
                + "        MeteoCal's Team");
    }
    
    public void sendMail(String destination, KindOfEmail kindOfEmail, Event event) throws MessagingException, UnsupportedEncodingException{
        User user=userManager.findByEmail(destination);
        System.out.println(user.getEmail());
        
        
        String subject=null;
        String message=null;
        switch(kindOfEmail){
            case REGISTRATION:
                subject=this.subjects.get(KindOfEmail.REGISTRATION);
                message=String.format(this.messages.get(KindOfEmail.REGISTRATION), user.getName(),user.getSurname());
                break;
            case INVITEDTOEVENT:
                subject=String.format(this.subjects.get(KindOfEmail.INVITEDTOEVENT), event.getTitle());
                message=String.format(this.messages.get(KindOfEmail.INVITEDTOEVENT), user.getName(),user.getSurname(),event.getCreator().getName(),event.getCreator().getSurname(),event.getTitle(),event.getId(),event.getTitle());
                break;
            case FORGOTTENPASSWORD:
                subject=this.subjects.get(KindOfEmail.FORGOTTENPASSWORD);
                message=String.format(this.messages.get(KindOfEmail.FORGOTTENPASSWORD), user.getName(),user.getSurname(),NavigationBean.getLinkForResetEmail(user),NavigationBean.getLinkForResetEmail(user));
                break;
        }
        
        Message msg = new MimeMessage(mailSession);
        msg.setSubject(subject);
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(destination, user.getName()+" "+user.getSurname()));
        msg.setFrom(new InternetAddress(this.meteocalsEmail, this.meteocalsName));
        //either
        //msg.setContent(message, "text/plain");
        //either
        msg.setContent(message, "text/html; charset=utf-8");
        Transport.send(msg);
        System.out.println("I've sent an email to "+ destination);
    }
}
