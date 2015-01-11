/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.NotificationController;
import it.polimi.meteocal.entity.Notification;
import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.NotificationManager;
import it.polimi.meteocal.entityManager.UserManager;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

/**
 *
 * @author Andrea
 */
@Named
@RequestScoped
public class UserBean {

    @EJB
    UserManager um;
    
    @EJB
    NotificationManager notificationManager;
    
    @Inject
    NotificationController notificationController;
    
    public UserBean() {
    }
    
    public User getLoggedUser(){
        return um.getLoggedUser();
    }
    
    @Deprecated
    public String getName() {
        return um.getLoggedUser().getName();
    }
    
    @Deprecated
    public String getSurname() {
        return um.getLoggedUser().getSurname();
    }
    
    @Deprecated
    public String getEmail() {
        return um.getLoggedUser().getEmail();
    }
    
    public int getNumOfNotReadNotifications() {
        return notificationManager.countNotReadNotifications(um.getLoggedUser().getEmail());
    }
    
    public List<Notification> getNotifications() {
        return notificationManager.findAllNotificationsByUser(um.getLoggedUser().getEmail());
    }
    
    public List<Event> getEventsInCalendar() {
        return new ArrayList<>(um.getLoggedUser().getEvents());
    }

    public List<Event> getEventsInvitedTo() {
        return new ArrayList<>(um.getLoggedUser().getInvitations());
    }
    
    public void hasReadNotification(){
        notificationController.setAsRead(getLoggedUser());
    }
    
    public String getNotificationClass(Notification notification){
        String style;
        if(!notification.getReadByUser())
            style = "notReadNotification";
        else
            style = "readNotification";
        
        if(notification.getType()=='A')
            return style + " inviteNotification";
        if(notification.getType()=='B')
            return style + " updateNotification";
        return style + " weatherNotification";
    }
    
    public String getNotificationIcon(Notification notification){
        if(notification.getType()=='A')
            return "inviteNotification.png";
        if(notification.getType()=='B')
            return "updateNotification.png";
        return "weatherNotification.png";
    }
}
