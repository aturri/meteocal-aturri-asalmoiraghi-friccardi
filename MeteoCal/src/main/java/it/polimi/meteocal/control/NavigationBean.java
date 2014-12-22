/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

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
}