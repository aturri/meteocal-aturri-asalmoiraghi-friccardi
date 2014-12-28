/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.MailController;
import it.polimi.meteocal.control.MailController;
import it.polimi.meteocal.control.NavigationBean;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.Session;

/**
 *
 * @author Fabiuz
 */
@Named
@RequestScoped
public class RecoverPasswordBean {
    
    @Inject
    private MailController mailControl;
    
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
            return "";
        }
        else{
            User user=userManager.findByEmail(email);
            try {
                this.mailControl.sendMail(email, user.getName()+" "+user.getSurname(),"Recover your Meteocal's password", 
                        "Dear "+ user.getName()+" "+user.getSurname()+",<br />"
                                + "We have see your request to change your password because you have forgotten it. <br />"
                                + "Now you just click on the follow link to set a new password and come back to MeteoCal<br /><br />"
                                + "<a href=\""+NavigationBean.getLinkForResetEmail(user)+"\" >"+NavigationBean.getLinkForResetEmail(user)+"</a>"
                                + "<br /><br />"
                                + "If you haven't registed to MeteoCal or if you haven't required to change your password, ignore this eMail.<br />"
                                + "Best regards,<br />"
                                + "        MeteoCal's Team"        
                );            } catch (MessagingException | UnsupportedEncodingException ex) {
                Logger.getLogger(RecoverPasswordBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            MessageBean.addInfo("Recover link sent to the specified email");
        }
        return NavigationBean.redirectToIndex();
    }
    
    public String setNewPassword(){
        Map<String, String> params =FacesContext.getCurrentInstance().
                   getExternalContext().getRequestParameterMap();
        String codeFromEmail = params.get("code");
        String emailFromEmail=params.get("email");
        System.out.println(""+ params.keySet());

        
        if(!userManager.existsUser(emailFromEmail)){
            MessageBean.addError("User not found");
            return "";
        }
        //verifica che il codice sia corretto
        User user=userManager.findByEmail(emailFromEmail);
        String aux1=UserManager.getCodeFromUser(user);
        if(!aux1.equals(codeFromEmail)){
            MessageBean.addError("The code is not valid");
            return "";
        }
        //verifica che la password siano corrette
        if(!this.password1.equals(this.password2)){
            MessageBean.addError("password don't match");
            //returns null implies that the page is the same
            return "";
        }
        
        //Set the new password
        user.setPassword(password1);
        userManager.update(user);
        
        return NavigationBean.redirectToIndex();
    }

    /**
     * @return the email
     */
    public String getEmail() {
        if(this.email==null){
            Map<String, String> params =FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            String emailFromEmail = params.get("email");
            System.out.println(params.keySet().toString() +"-->"+emailFromEmail+"<--");
            if(emailFromEmail!=null){
                this.email=emailFromEmail;
            }
        }
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

    public String getCode() {
        if(this.code==null){
            String codeFromEmail = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("code");
            if(codeFromEmail==null){
            } else {
                this.code=codeFromEmail;
            }
        }
        return this.code;
    }
    
    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    
}
