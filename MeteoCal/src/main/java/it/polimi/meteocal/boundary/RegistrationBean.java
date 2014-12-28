/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.MailController;
import it.polimi.meteocal.control.NavigationBean;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.entity.User;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.mail.MessagingException;
import javax.mail.Session;

/**
 *
 * @author Andrea
 */
@Named(value = "registrationBean")
@RequestScoped
public class RegistrationBean {
    
    @Resource(name = "mail/mailSession")
    private Session mailSession;
    private MailController mailControl;
    
    @EJB
    private UserManager um;

    private User user=new User();

    public RegistrationBean() {
    }

    public User getUser() {
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
            mailControl=new MailController(mailSession);
            mailControl.sendMail(user.getEmail(),user.getName()+" "+user.getSurname(),"Confirm registration",
                    "Congraturation "+user.getName()+" "+user.getSurname()+",<br />Your account is registred succefully!<br />Best regards,<br />       MeteoCal's Team");
            return NavigationBean.redirectToLogin();
        }catch(MessagingException ex) {
            Logger.getLogger(RegistrationBean.class.getName()).log(Level.SEVERE, null, ex);
            return NavigationBean.redirectToLogin();
        }catch (UnsupportedEncodingException ex) {
            Logger.getLogger(RegistrationBean.class.getName()).log(Level.SEVERE, null, ex);
        }catch (EJBException e){
            MessageBean.addWarning("Email already registered");
            Logger.getLogger(RegistrationBean.class.getName()).log(Level.SEVERE, null, e);
        }
        return "";
    }
    
    public Date getToday() {
        return new Date();
    }
}
