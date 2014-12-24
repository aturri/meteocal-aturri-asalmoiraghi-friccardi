/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.Utility;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author Fabiuz
 */
@Named
@RequestScoped
public class SettingsBean {

    @EJB
    UserManager userManager;
    
    private User user;
    
    private String oldPassword;
    private String newPassword;
//    private String newPassword2;
    
    public SettingsBean() {
    }

    public String changePassword(){
//        if(!this.newPassword1.equals(this.newPassword2)){
//            MessageBean.addError("Passwords don't match");
//        }else{
            //verify the old password is correct
            String old=Utility.getHashSHA256(this.oldPassword);
            User currentUser=userManager.getLoggedUser();
            String saved=currentUser.getPassword();
            if(!saved.equals(old)){
                MessageBean.addError("PasswordMessages", "Insert the right password before change it");
            }else{
                //password update
                //NB: we don't need to hash the new password. This is computed from glassfish automatically
                currentUser.setPassword(this.newPassword);
                userManager.update(currentUser);
                MessageBean.addInfo("PasswordMessages","The password is correctly changed");
            }
//        }
        return "";
    }
    
    public String changeUsersData(){
        userManager.update(this.user);
        MessageBean.addInfo("UserDataMessages","Data correctly changed");
        return "";
    }
    
    public String changeCalendarVisibilityData(){
        userManager.update(this.user);
        MessageBean.addInfo("VisibilityMessages","Your calendar's visibility is correctly changed");
        return "";
    }
    /**
     * @return the oldPassword
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * @param oldPassword the oldPassword to set
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * @return the newPassword1
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * @param newPassword1 the newPassword1 to set
     */
    public void setNewPassword(String newPassword1) {
        this.newPassword = newPassword1;
    }

    /**
     * @return the newPassword2
     */
//    public String getNewPassword2() {
//        return newPassword2;
//    }

    /**
     * @param newPassword2 the newPassword2 to set
     */
//    public void setNewPassword2(String newPassword2) {
//        this.newPassword2 = newPassword2;
//    }

    /**
     * @return the user
     */
    public User getUser() {
        if(this.user==null){
            this.user=userManager.getLoggedUser();
        }
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    
    
}
