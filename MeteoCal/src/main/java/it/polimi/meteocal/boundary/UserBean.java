/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.entity.Notification;
import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entityManager.UserManager;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author Andrea
 */
@Named
@RequestScoped
public class UserBean {

    @EJB
    UserManager um;
    
    public UserBean() {
    }
    
    public String getName() {
        return um.getLoggedUser().getName();
    }
    
    public String getSurname() {
        return um.getLoggedUser().getSurname();
    }
    
    public String getEmail() {
        return um.getLoggedUser().getEmail();
    }
    
    public int getNumOfNotReadNotifications() {
        return um.countNotReadNotifications();
    }
    
    public List<Notification> getNotifications() {
        return um.findAllNotifications();
    }
    
    public List<Event> getEventsInCalendar() {
        return new ArrayList<>(um.getLoggedUser().getEvents());
    }
}
