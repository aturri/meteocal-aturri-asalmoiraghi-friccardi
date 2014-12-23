/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import it.polimi.meteocal.control.MailControl;
import it.polimi.meteocal.control.NavigationBean;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.mail.MessagingException;
import javax.mail.Session;

/**
 *
 * @author Fabiuz
 */
@Named
@RequestScoped
public class RecoverPasswordBean {

    @Resource(name = "mail/mailSession")
    private Session mailSession;
    private MailControl mailControl;
    
    @EJB
    private UserManager userManager;
    
    private String email;
    private String password1;
    private String password2;
    private String code;
    /**
     * Creates a new instance of RecoverPasswordBean
     */
    public RecoverPasswordBean() {
    }
    
    public String recoverPassword(){
        if(!userManager.existsUser(email)){
            MessageBean.addError("User not found");
        }
        else{
            User user=userManager.findByEmail(email);
            mailControl=new MailControl(mailSession);
            try {
                this.mailControl.sendMail(email, user.getName()+" "+user.getSurname(),"Recover your Meteocal's password", 
                        "Dear "+user.getName()+" "+user.getSurname()+",\n"
                                + "We have see your request to change your password because you have forgotten it. \n"
                                + "Now you just click on the follow link to set a new password and come back to MeteoCal\n"
                                + this.getLinkForResetEmail(user)
                                + "\n\n"
                                + "If you haven't registed to MeteoCal or if you haven't required to change your password, ignore this eMail.\n"
                                + "Best regards,\n"
                                + "        MeteoCal's Team"        
                );            } catch (MessagingException | UnsupportedEncodingException ex) {
                Logger.getLogger(RecoverPasswordBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            MessageBean.addInfo("Recover link sent to the specified email");
        }
        return NavigationBean.redirectToIndex();
    }
    
    public String setNewPassword(){
        if(!userManager.existsUser(email)){
            MessageBean.addError("User not found");
        }
        //verifica che il codice sia corretto
        
        //verifica che la password siano corrette
        if(!this.password1.equals(this.password2)){
            MessageBean.addError("eMails don't match");
            return null;
        }
        return null;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the password1
     */
    public String getPassword1() {
        return password1;
    }

    /**
     * @param password1 the password1 to set
     */
    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    /**
     * @return the password2
     */
    public String getPassword2() {
        return password2;
    }

    /**
     * @param password2 the password2 to set
     */
    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     * 
     * @param user
     * @return 
     */
    
    private String getLinkForResetEmail(User user) {
        String string=user.getEmail()+user.getPassword()+user.getLastAccess().toString();
        String hashed = Hashing.sha256().hashString(string,Charsets.UTF_8 ).toString();
        return "http://localhost:8080/MeteoCal/insertNewPassoword?faces-redirect=true&code="
                + hashed + "&email="+user.getEmail();
    }
    
    
}


















