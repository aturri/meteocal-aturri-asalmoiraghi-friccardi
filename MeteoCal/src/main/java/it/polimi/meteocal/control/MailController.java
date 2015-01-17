package it.polimi.meteocal.control;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.exception.InvalidArgumentException;
import java.io.UnsupportedEncodingException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * IMPROVEMENT TODO:
 * - create sendGenericEmail()
 * - create sendEmail() and sendDefaultEmail()
 * @author Fabiuz
 */
@Stateless
public class MailController {

    @Resource(name = "mail/mailSession")
    private Session mailSession;
    
    @EJB
    UserManager userManager;
    
    private final String meteocalURL="http://localhost:8080/MeteoCal/";
    private final String meteocalsEmail="afa.meteocal@gmail.com";
    private final String meteocalsName="MeteoCal's Team";
    private final String meteocalsSignature="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;MeteoCal's Team";
    private final String meteocalsHello="Dear %s %s,<br />";
    private final Map<KindOfEmail,String> subjects=new EnumMap<>(KindOfEmail.class);
    private final Map<KindOfEmail,String> messages=new EnumMap<>(KindOfEmail.class);
    
    /**
     * Initialize the Map with default subjects and messages
     */
    @PostConstruct
    private void init(){
        subjects.put(KindOfEmail.REGISTRATION, "Confirm registration");
        messages.put(KindOfEmail.REGISTRATION, ""
                + "Congraturation %s %s,<br />"
                + "Your account is registred succefully!<br />"
                + "Best regards,<br />"
                + this.meteocalsSignature);
        subjects.put(KindOfEmail.INVITEDTOEVENT, "Invite to partecipate to %s");
        messages.put(KindOfEmail.INVITEDTOEVENT, ""
                + this.meteocalsHello
                + "You are invited from %s %s to partecipate to %s. Click on the follow link to see more details:<br />"
                + "<br />"
                + "<a href=\""+meteocalURL+"event/detail.xhtml?id=%d\">%s</a><br />"
                + "<br />"
                + "Enjoy it ;)<br />"
                + this.meteocalsSignature);
        subjects.put(KindOfEmail.FORGOTTENPASSWORD, "Recover your Meteocal's password");
        messages.put(KindOfEmail.FORGOTTENPASSWORD, ""
                + this.meteocalsHello
                + "We have see your request to change your password because you have forgotten it. <br />"
                + "Now you just click on the follow link to set a new password and come back to MeteoCal<br />"
                + "<br />"
                + "<a href=\"%s\">%s</a>"
                + "<br /><br />"
                + "If you haven't registed to MeteoCal or if you haven't required to change your password, ignore this eMail.<br />"
                + "Best regards,<br />"
                + this.meteocalsSignature);
        subjects.put(KindOfEmail.EVENTCANCELLED,"An event that you l'll partecipate is cancelled");
        messages.put(KindOfEmail.EVENTCANCELLED, ""
                + this.meteocalsHello
                + "The following event is cancelled from the organizer %s %s (%s):<br />"
                + "<br />"
                + "Title: %s<br />"
                + "Description: %s<br />"
                + "<br />"
                + "All the partecipating users and invited users are be notified. Sorry for the drawback.<br />"
                + this.meteocalsSignature);
        subjects.put(KindOfEmail.EVENTUPDATED, "An event that you l'll partecipate is updated");
        messages.put(KindOfEmail.EVENTUPDATED, ""
                + this.meteocalsHello
                + "The following event is updated from the organizer %s %s (%s):<br />"
                + "<br />"
                + "<a href=\""+meteocalURL+"event/detail.xhtml?id=%d\">%s</a><br />"
                + "<br />"
                + "Click on the link to see more details.<br />"
                + "<br />"
                + "All the partecipating users and invited users are be notified. Sorry for the drawback.<br />"
                + this.meteocalsSignature);
        subjects.put(KindOfEmail.EVENTUPDATED1, "An event that you l'll partecipate is updated with new dates");
        messages.put(KindOfEmail.EVENTUPDATED1, ""
                + this.meteocalsHello
                + "The following event is updated from the organizer %s %s (%s) with new dates:<br />"
                + "<br />"
                + "<a href=\""+meteocalURL+"event/detail.xhtml?id=%d\">%s</a><br />"
                + "<br />"
                + "YOU HAVE TO ACCEPT INVITATION AGAIN!<br /><br />Click on the link to see more details.<br />"
                + "<br />"
                + "All the partecipating users and invited users are be notified. Sorry for the drawback.<br />"
                + this.meteocalsSignature);
        subjects.put(KindOfEmail.ALERTWEATHER1, "Bad weather alert");
        messages.put(KindOfEmail.ALERTWEATHER1, ""
                + this.meteocalsHello
                + "For the following event we detected bad weather:<br />"
                + "<br />"
                + "<a href=\""+meteocalURL+"event/detail.xhtml?id=%d\">%s</a><br />"
                + "<br />"
                + "Click on the link to see more details.<br />"
                + "<br />"
                + "All the partecipating users and invited users are be notified. Sorry for the drawback.<br />"
                + this.meteocalsSignature);
        subjects.put(KindOfEmail.ALERTWEATHER3, "Update the event with a sunny day");
        messages.put(KindOfEmail.ALERTWEATHER3, ""
                + this.meteocalsHello
                + "For the following event we detected bad weather:<br />"
                + "<br />"
                + "<a href=\""+meteocalURL+"event/edit.xhtml?id=%d\">%s</a><br />"
                + "<br />"
                + "but there may be good weather on a close day!"
                + "Click on the link to see it and to update the event.<br />"
                + this.meteocalsSignature);
        subjects.put(KindOfEmail.WEATHERCHANGED, "An event that you l'll partecipate has changed forecasts");
        messages.put(KindOfEmail.WEATHERCHANGED, ""
                + this.meteocalsHello
                + "The following event has weather forecasts changed to %s:<br />"
                + "<br />"
                + "<a href=\""+meteocalURL+"event/detail.xhtml?id=%d\">%s</a><br />"
                + "<br />"
                + "Click on the link to see more details.<br />"
                + "<br />"
                + "All the partecipating users and invited users are be notified. Sorry for the drawback.<br />"
                + this.meteocalsSignature);
    }
    
    /**
     * This function send a default email
     * @param destination the address of the recipient
     * @param kindOfEmail kind of email (it defines the layout of message and subject in the email)
     * @param event the event which the email refer. If the email doesn't use event, it may set to null
     */
    public void sendMail(String destination, KindOfEmail kindOfEmail, Event event){
        User user=userManager.findByEmail(destination);

        String subject=null;
        String message=null;
        switch(kindOfEmail){
            case REGISTRATION:
                subject=this.subjects.get(KindOfEmail.REGISTRATION);
                message=String.format(this.messages.get(KindOfEmail.REGISTRATION), user.getName(),user.getSurname());
                break;
            case FORGOTTENPASSWORD:
                subject=this.subjects.get(KindOfEmail.FORGOTTENPASSWORD);
                message=String.format(this.messages.get(KindOfEmail.FORGOTTENPASSWORD), user.getName(),user.getSurname(),NavigationBean.getLinkForResetEmail(user),NavigationBean.getLinkForResetEmail(user));
                break;
            case INVITEDTOEVENT:
                subject=String.format(this.subjects.get(KindOfEmail.INVITEDTOEVENT), event.getTitle());
                message=String.format(this.messages.get(KindOfEmail.INVITEDTOEVENT), user.getName(),user.getSurname(),event.getCreator().getName(),event.getCreator().getSurname(),event.getTitle(),event.getId(),event.getTitle());
                break;
            case EVENTCANCELLED:
                subject=this.subjects.get(KindOfEmail.EVENTCANCELLED);
                message=String.format(this.messages.get(KindOfEmail.EVENTCANCELLED), user.getName(),user.getSurname(),event.getCreator().getName(),event.getCreator().getSurname(),event.getCreator().getEmail(),event.getTitle(),event.getDescription());
                break;
            case EVENTUPDATED:
                subject=this.subjects.get(KindOfEmail.EVENTUPDATED);
                message=String.format(this.messages.get(KindOfEmail.EVENTUPDATED), user.getName(),user.getSurname(),event.getCreator().getName(),event.getCreator().getSurname(),event.getCreator().getEmail(),event.getId(),event.getTitle());
                break;
            case EVENTUPDATED1:
                subject=this.subjects.get(KindOfEmail.EVENTUPDATED1);
                message=String.format(this.messages.get(KindOfEmail.EVENTUPDATED1), user.getName(),user.getSurname(),event.getCreator().getName(),event.getCreator().getSurname(),event.getCreator().getEmail(),event.getId(),event.getTitle());
                break;
            case ALERTWEATHER1:
                subject=this.subjects.get(KindOfEmail.ALERTWEATHER1);
                message=String.format(this.messages.get(KindOfEmail.ALERTWEATHER1), user.getName(),user.getSurname(),event.getId(),event.getTitle());
                break;
            case ALERTWEATHER3:
                subject=this.subjects.get(KindOfEmail.ALERTWEATHER3);
                message=String.format(this.messages.get(KindOfEmail.ALERTWEATHER3), user.getName(),user.getSurname(),event.getId(),event.getTitle());
                break;
            case WEATHERCHANGED:
                subject=this.subjects.get(KindOfEmail.WEATHERCHANGED);
                String weatherStr = "not available";
                if(event.getWeather()!=null) weatherStr = event.getWeather().getWeather();
                message=String.format(this.messages.get(KindOfEmail.WEATHERCHANGED),user.getName(),user.getSurname(),weatherStr,event.getId(),event.getTitle());
                break;
            case GENERIC:
                throw new InvalidArgumentException("You must use sendGenericEmail() and set manually subject and message email");
        }
        try {    
            Message msg = new MimeMessage(mailSession);
            msg.setSubject(subject);
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(destination, user.getName()+" "+user.getSurname()));
            msg.setFrom(new InternetAddress(this.meteocalsEmail, this.meteocalsName));
            //either
            //msg.setContent(message, "text/plain");
            //either
            msg.setContent(message, "text/html; charset=utf-8");
            SendEmailThread sec=new SendEmailThread();
            sec.setMessage(msg);
            Thread t1=new Thread(sec);
            t1.start();
        } catch (MessagingException | UnsupportedEncodingException ex) {
            Logger.getLogger(MailController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
