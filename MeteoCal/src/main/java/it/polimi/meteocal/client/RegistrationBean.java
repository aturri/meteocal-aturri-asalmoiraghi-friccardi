/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.client;

import it.polimi.meteocal.business.entity.User;
import it.polimi.meteocal.business.logic.UserManager;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author Andrea
 */
@Named
@RequestScoped
public class RegistrationBean {

    @EJB
    private UserManager um;

    private User user;

    public RegistrationBean() {
    }

    public User getUser() {
        if (user == null) {
            user = new User();
        }
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String register() {
        um.save(user);
        return "home/index?faces-redirect=true";
    }

    
}
