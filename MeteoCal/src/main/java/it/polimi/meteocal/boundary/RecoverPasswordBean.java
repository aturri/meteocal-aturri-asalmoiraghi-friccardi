/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.utils.MessageUtility;
import it.polimi.meteocal.control.kind.KindOfEmail;
import it.polimi.meteocal.control.MailController;
import it.polimi.meteocal.boundary.service.NavigationService;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.utils.Utility;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

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
    
    /**
     * Function that control the data insert into the form and send email to reset the password
     * @return the destination page
     */
    public String recoverPassword(){
        if(!userManager.existsUser(email)){
            MessageUtility.addError("User not found");
            return "";
        }
        else{
            this.mailControl.sendMail(email, KindOfEmail.FORGOTTENPASSWORD,null);
            MessageUtility.addInfo("Recover link sent to the specified email");
        }
        return NavigationService.redirectToIndex();
    }
    
    /**
     * This function controls the data inserted into the form and if correct, change the password
     * @return the destination page
     */
    public String setNewPassword(){
        String codeFromEmail = Utility.getValueFromURL("code");
        String emailFromEmail=Utility.getValueFromURL("email");

        //verify that the user exists
        if(!userManager.existsUser(emailFromEmail)){
            MessageUtility.addError("User not found");
            return "";
        }
        //verifica che il codice sia corretto
        User user=userManager.findByEmail(emailFromEmail);
        String aux1=UserManager.getCodeFromUser(user);
        if(!aux1.equals(codeFromEmail)){
            MessageUtility.addError("The code is not valid");
            return "";
        }
        //verifica che la password siano corrette
        if(!this.password1.equals(this.password2)){
            MessageUtility.addError("password don't match");
            //returns "" implies that the page is the same
            return "";
        }
        
        //Set the new password
        user.setPassword(password1);
        userManager.update(user);
        return NavigationService.redirectToIndex();
    }

    /**
     * Get email From "GET" parameter (if class field are not null) or from the class field
     * @return the email
     */
    public String getEmail() {
        if(this.email==null){
            String emailFromEmail = Utility.getValueFromURL("email");
            if(emailFromEmail!=null){
                this.email=emailFromEmail;
            }
        }
        return email;
    }

    /**
     * 
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
     * 
     * @return the code for changing email
     */
    public String getCode() {
        if(this.code==null){
            String codeFromEmail = Utility.getValueFromURL("code");
            if(codeFromEmail==null){
            } else {
                this.code=codeFromEmail;
            }
        }
        return this.code;
    }
    
    /**
     * 
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }
}
