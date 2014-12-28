/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * This bean manage the navigation links of the site
 * @author andrea
 */

@Named
@RequestScoped
public class NavigationBean implements Serializable {
 
    private static final long serialVersionUID = 1520318172495977648L;
    
    /**
     * Redirect to login page.
     * @return Login page name.
     */
    public static String redirectToIndex() {
        return "/index?faces-redirect=true";
    }
     
    /**
     * Go to login page.
     * @return Login page name.
     */
    public static String toIndex() {
        return "/index";
    }
    
    /**
     * Redirect to login page.
     * @return Login page name.
     */
    public static String redirectToLogin() {
        return "/login?faces-redirect=true";
    }
     
    /**
     * Go to login page.
     * @return Login page name.
     */
    public static String toLogin() {
        return "/login";
    }
    
    /**
     * Redirect to login page.
     * @return Login page name.
     */
    public static String redirectToRegister() {
        return "/register?faces-redirect=true";
    }
     
    /**
     * Go to login page.
     * @return Login page name.
     */
    public static String toRegister() {
        return "/register";
    }
    
    /**
     * Redirect to login page.
     * @return Login page name.
     */
    public static String redirectToHome() {
        return "/user/home?faces-redirect=true";
    }
     
    /**
     * Go to login page.
     * @return Login page name.
     */
    public static String toHome() {
        return "/user/home";
    }
    
    /**
     * Redirect to recover password page.
     * @return recover password page name.
     */
    public static String redirectToRecoverPassword() {
        return "/recoverPassword?faces-redirect=true";
    }
    
    /**
     * Go to recover password page.
     * @return recover password page name.
     */
    public static String toRecoverPassword(){
        return "/recoverPassword";
    }

    
    public static String redirectToSetNewPassword(String email,String code) {
        return "/setNewPassword?faces-redirect=true&email="+email+"&code="+code;
    }
    
    /**
     * Go to search results page.
     * @return search results page name.
     */
    public static String toSearchResultsPage(){
        return "/search/results";
    }
    
    public static String redirectToEventDetailsPage(Integer id){
        return "/event/detail.xhtml?faces-redirect=true&id="+id;
    }
    
    /**
     * Return the link to go to the page to set the new password with correct parameter
     * @param user
     * @return the absolute path to go to the set new password page with correct parameters
     */
    public static String getLinkForResetEmail(User user) {
        return "http://localhost:8080/MeteoCal/setNewPassword.xhtml?faces-redirect=true&code="
                + UserManager.getCodeFromUser(user) + "&email="+user.getEmail();
    }
}