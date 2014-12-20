/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.gui.security;

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
        return "index.xhtml?faces-redirect=true";
    }
     
    /**
     * Go to login page.
     * @return Login page name.
     */
    public static String toIndex() {
        return "index.xhtml";
    }
    
    /**
     * Redirect to login page.
     * @return Login page name.
     */
    public static String redirectToLogin() {
        return "login.xhtml?faces-redirect=true";
    }
     
    /**
     * Go to login page.
     * @return Login page name.
     */
    public static String toLogin() {
        return "login.xhtml";
    }
    
    /**
     * Redirect to login page.
     * @return Login page name.
     */
    public static String redirectToRegister() {
        return "register.xhtml?faces-redirect=true";
    }
     
    /**
     * Go to login page.
     * @return Login page name.
     */
    public static String toRegister() {
        return "register.xhtml";
    }
    
    /**
     * Redirect to login page.
     * @return Login page name.
     */
    public static String redirectToHome() {
        return "user/home.xhtml?faces-redirect=true";
    }
     
    /**
     * Go to login page.
     * @return Login page name.
     */
    public static String toHome() {
        return "user/home.xhtml";
    }
}