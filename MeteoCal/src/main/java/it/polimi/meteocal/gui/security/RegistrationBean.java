/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.gui.security;

import it.polimi.meteocal.business.security.boundary.UserManager;
import it.polimi.meteocal.business.security.entity.User;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
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
 * @author Andrea
 */
@Named(value = "registrationBean")
@RequestScoped
public class RegistrationBean {
    @Resource(name = "mail/mailSession")
    private Session mailSession;
    
    @EJB
    private UserManager um;

    private User user;

    public RegistrationBean() {
    }

    public User getUser() {
        if (user == null) {
            user = new User();
        }
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
   
    public void handleExistsUser() {
        if(um.existsUser(user.getEmail())) {
            MessageBean.addWarning("Email already registered");
        }
    }

    public String register() {
        try {
            um.save(user);
            
            Message msg = new MimeMessage(mailSession);
            msg.setSubject("Confirm registration");
            try {
                msg.setRecipient(RecipientType.TO, new InternetAddress(user.getEmail(), user.getName()+" "+user.getSurname()));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(RegistrationBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                msg.setFrom(new InternetAddress("afa.meteocal@gmail.com", "MeteoCal's Team"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(RegistrationBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Congraturation "+user.getName()+" "+user.getSurname()+",\\nYour account is registred succefully!\\nBest regards,\\n       MeteoCal's Team");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            msg.setContent(multipart);

            Transport.send(msg);
            
            return NavigationBean.redirectToLogin();
            
        }catch(MessagingException ex) {
            Logger.getLogger(RegistrationBean.class.getName()).log(Level.SEVERE, null, ex);
            return NavigationBean.redirectToLogin();
       }
        catch (EJBException e){
            MessageBean.addWarning("Email already registered");
            Logger.getLogger(RegistrationBean.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }
}
