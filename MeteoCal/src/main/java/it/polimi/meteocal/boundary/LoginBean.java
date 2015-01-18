/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.NavigationBean;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Andrea
 */
@Named
@ViewScoped
public class LoginBean implements Serializable{

    @EJB
    UserManager um;
    
    private String username;
    private String password;
    
    private String forwardUrl;
    
    @PostConstruct
    void init(){
        String requestedURI = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(RequestDispatcher.FORWARD_SERVLET_PATH);
        String applicationPath = (String) FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath();
        String requestedQuery = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get(RequestDispatcher.FORWARD_QUERY_STRING);
        
        if(requestedURI==null)
            requestedURI = NavigationBean.toHome()+".xhtml";
        forwardUrl = applicationPath + requestedURI;
        if(requestedQuery!=null)
           forwardUrl += "?" + requestedQuery;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String login() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        try {
            request.login(this.username, this.password);
            User user = um.getLoggedUser();
            user.setLastAccess(new Date());
            um.update(user);
        } catch (ServletException e) {
            MessageBean.addError("Login failed");
            return NavigationBean.toLogin();
        }
        //System.out.println("Is User = "+request.isUserInRole("USER"));
        
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(forwardUrl);
        return null;
    }


    
    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        request.getSession().invalidate();
        return NavigationBean.redirectToIndex();
    }
    
    public boolean isLogged(){
        return um.getLoggedUser()!=null;
    }
}

