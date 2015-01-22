/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.utils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author andrea
 */
public class MessageUtility {
    
    /**
     * This method add to FacesMessage a general warning message
     * @param message 
     */
    public static void addWarning(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
    }
    
    /**
     * This method add to FacesMessage a general info message
     * @param message 
     */
    public static void addInfo(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }
    
    /**
     * This method add to FacesMessage a general error message
     * @param message 
     */
    public static void addError(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }
    
    /**
     * This method add to FacesMessage a warning message for a specified id
     * @param id
     * @param message 
     */
    public static void addWarning(String id, String message) {
        FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
    }
    
    /**
     * This method add to FacesMessage a info message for a specified id
     * @param id
     * @param message 
     */
    public static void addInfo(String id, String message) {
        FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }
    
    /**
     * This method add to FacesMessage a error message for a specified id
     * @param id
     * @param message 
     */
    public static void addError(String id, String message) {
        FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }
    
}
