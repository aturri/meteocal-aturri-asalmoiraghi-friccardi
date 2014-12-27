package it.polimi.meteocal.control;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.UnsupportedEncodingException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Class that manage the email: for each email required to instanciated a new object MailController, 
 * otherwise will happen for concurrent error
 * @author Fabiuz
 */
public class MailControler implements Runnable{
    
    private UserManager userManager;
    
    private final Session mailSession;
    private final String meteocalsEmail="afa.meteocal@gmail.com";
    private final String meteocalsName="MeteoCal's Team";
    private final Map<KindOfEmail,String> subjects=new EnumMap<>(KindOfEmail.class);
    private final Map<KindOfEmail,String> messages=new EnumMap<>(KindOfEmail.class);
    private Message msg;
    
    public MailControler(Session mailSession){
        this.mailSession=mailSession;
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
                + "<a href=\"http://localhost:8080/MeteoCal/event/detail.xhtml?id=%d\">http://localhost:8080/MeteoCal</a><br />"
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
    
    
    
    /**
     * This method send the eMail in text/plain format
     * @param destination recipient's email address
     * @param kindOfEmail kind Of Email
     * @param event usefull for partecipating kind of email, otherwise it can be null
     * @throws MessagingException
     * @throws UnsupportedEncodingException 
     */
    public void sendMail(String destination, KindOfEmail kindOfEmail, Event event) throws MessagingException, UnsupportedEncodingException{
        User user=userManager.findByEmail(destination);
        String message=null,subject=null;
        switch(kindOfEmail){
            case REGISTRATION:
                subject=this.subjects.get(KindOfEmail.REGISTRATION);
                message=String.format(this.messages.get(KindOfEmail.REGISTRATION), user.getName(),user.getSurname());
                break;
            case INVITEDTOEVENT:
                subject=String.format(this.subjects.get(KindOfEmail.INVITEDTOEVENT), event.getTitle());
                message=String.format(this.messages.get(KindOfEmail.INVITEDTOEVENT), user.getName(),user.getSurname(),event.getCreator().getName(),event.getCreator().getSurname(),event.getTitle(),event.getId());
                break;
            case FORGOTTENPASSWORD:
                subject=this.subjects.get(KindOfEmail.FORGOTTENPASSWORD);
                message=String.format(this.messages.get(KindOfEmail.FORGOTTENPASSWORD), user.getName(),user.getSurname(),this.getLinkForResetEmail(user),this.getLinkForResetEmail(user));
                break;
        }
        msg = new MimeMessage(mailSession);
        msg.setSubject(subject);
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(destination, user.getName()+" "+user.getSurname()));
        msg.setFrom(new InternetAddress(this.meteocalsEmail, this.meteocalsName));
        //either
        //msg.setContent(message, "text/plain");
        //either
        msg.setContent(message, "text/html; charset=utf-8");
        
        Thread t1=new Thread(this);
        t1.start();
        System.out.println("Thread partito!");
    }

    /**
     * 
     * @param user
     * @return the absolute path to go to the set new password page with correct parameters
     */
    private String getLinkForResetEmail(User user) {
        return "http://localhost:8080/MeteoCal/setNewPassword.xhtml?faces-redirect=true&code="
                + userManager.getCodeFromUser(user) + "&email="+user.getEmail();
    }
    
    @Override
    public void run() {
        try {
            Transport.send(msg);
            System.out.println("I've sent an email to "+ msg.getAllRecipients());
        } catch (MessagingException ex) {
            Logger.getLogger(MailControler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}