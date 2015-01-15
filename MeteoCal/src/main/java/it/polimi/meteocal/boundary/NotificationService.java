/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.NotificationController;
import it.polimi.meteocal.entity.Notification;
import it.polimi.meteocal.entityManager.NotificationManager;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

/**
 *
 * @author Andrea
 */
@Named
@ViewScoped
public class NotificationService implements Serializable {

    @EJB
    UserManager um;

    @EJB
    NotificationManager notificationManager;

    @Inject
    NotificationController notificationController;

    private List<Notification> notifications;
    private int notReadNotifications;

    @PostConstruct
    public void init() {
        notifications = loadNotifications();
        notReadNotifications = loadNumOfNotReadNotifications();
    }

    public int getNumOfNotReadNotifications() {
        return notReadNotifications;
    }
    
    private int loadNumOfNotReadNotifications(){
        return notificationManager.countNotReadNotifications(um.getLoggedUser().getEmail());
    }
    
    private List<Notification> loadNotifications(){
        return notificationManager.findAllNotificationsByUser(um.getLoggedUser().getEmail());
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void hasReadNotification() {
        notificationController.setAsRead(um.getLoggedUser());
        notReadNotifications = loadNumOfNotReadNotifications();
    }

    public String getNotificationClass(Notification notification) {
        return notificationController.getNotificationClass(notification);
    }

    public String getNotificationIcon(Notification notification) {
        return notificationController.getNotificationIcon(notification);
    }

    public void checkNewNotifications() {
        //check if there are new not read notifications
        int newNotReadNotifications = loadNumOfNotReadNotifications();
        if(newNotReadNotifications>notReadNotifications){
            //Shown message for only the new notifications not loaded before
            List<Notification> newNotifications = loadNotifications();
            List<Notification> tmpNotifications = new ArrayList<>(newNotifications);
            //Remove notification that were already loaded
            newNotifications.removeAll(notifications);
            //The remaining notification are shown with message
            for (Notification n : newNotifications) {
                MessageBean.addInfo("notifyMessage", n.getText());
            }
            //Add the new notifications to the list
            notifications = tmpNotifications;
            notReadNotifications = newNotReadNotifications;
        }
    }
}
