/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary.service;

import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 * This bean manage the navigation links of the site
 * @author andrea
 */

@Named
@RequestScoped
public class NavigationService implements Serializable {
 
    private static final long serialVersionUID = 1520318172495977648L;
    
    /**
     * 
     * @return The main folder where the project is placed in the server.
     */
    public static String getAbsolute() {
        return "/MeteoCal/";
    }
    
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

    /**
     * Redirect to recover password page.
     * @return recover password page name.
     */
    public static String redirectToSetNewPassword(String email,String code) {
        return "/setNewPassword?faces-redirect=true&email="+email+"&code="+code;
    }
    
    /**
     * Go to user page.
     * @param email of the user
     * @return user page name.
     */
    public static String toShowUser(String email){
        return "/user/showUser?email="+email;
    }
    
    /**
     * Go to event create page.
     * @return event create page name.
     */
    public static String toEventCreate(){
        return "/event/create";
    } 
    
    /**
     * Go to event detail page.
     * Pick the id of the event from faces parameter.
     * @return event detail page name.
     */
    public static String toEventDetailsPage(){
        String id = null;
        if(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().containsKey("id"))
            id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        return "/event/detail?id="+id;
    }
    
    /**
     * Go to event detail page.
     * @param id of the event
     * @return event detail page name.
     */
    public static String toEventDetailsPage(String id){
        return "/event/detail?id="+id;
    } 
      
    /**
     * Redirect to event detail page.
     * @param id of the event
     * @return event detail page name.
     */
    public static String redirectToEventDetailsPage(String id){
        if(id==null)
            id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        return "/event/detail.xhtml?faces-redirect=true&id="+id;
    }
    
    /**
     * Go to event edit page.
     * @param id of the event
     * @return event detail page name.
     */
    public static String toEventEditPage(String id){
        return "/event/edit?id="+id;
    }    
        
    public static String redirectToEventEditPage(String id){
        if(id==null)
            id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        return "/event/edit.xhtml?faces-redirect=true&id="+id;
    }
    
    /**
     * Return the link to go to the page to set the new password with correct parameter
     * @param user
     * @return the absolute path to go to the set new password page with correct parameters
     */
    public static String getLinkForResetEmail(User user) {
        try {
            return "http://localhost:8080/MeteoCal/setNewPassword.xhtml?faces-redirect=true&code="
                    + UserManager.getCodeFromUser(user) + "&email="+URLEncoder.encode(user.getEmail(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(NavigationService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    /**
     * Go to contacts editing page
     * @return path to contacts editing page
     */
    public static String toContacts() {
        return "/user/favoriteUsers";
    }
    
    /**
     * Go to user settings page
     * @return path to settings page
     */
    public static String toSettings() {
        return "/user/settings";
    }
}
