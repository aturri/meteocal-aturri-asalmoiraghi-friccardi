/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary.service;

import it.polimi.meteocal.utils.MessageUtility;
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

    /**
     * Load all the notifications and the number of not read notifications
     */
    @PostConstruct
    public void init() {
        notifications = loadNotifications();
        notReadNotifications = loadNumOfNotReadNotifications();
    }

    /**
     * Get the number of notifications not read yet
     * @return number of notifications not read
     */
    public int getNumOfNotReadNotifications() {
        return notReadNotifications;
    }
    
    /**
     * Load the current number of notifications not read yet
     * @return number of notifications not read
     */
    private int loadNumOfNotReadNotifications(){
        return notificationManager.countNotReadNotifications(um.getLoggedUser().getEmail());
    }
    
    /**
     * Load all user's notifications
     * @return list of notifications
     */
    private List<Notification> loadNotifications(){
        return notificationManager.findAllNotificationsByUser(um.getLoggedUser().getEmail());
    }

    /**
     * Get the list of user's notifications
     * @return ist of notifications
     */
    public List<Notification> getNotifications() {
        return notifications;
    }

    /**
     * Set the last not read notifications as read.
     * Reload the number of not read notifications.
     */
    public void hasReadNotification() {
        notificationController.setAsRead(um.getLoggedUser());
        notReadNotifications = loadNumOfNotReadNotifications();
    }

    /**
     * Get the styleClass of a notification
     * @param notification
     * @return the styleClass of the notification
     */
    public String getNotificationClass(Notification notification) {
        return notificationController.getNotificationClass(notification);
    }

    /**
     * Get the icon of a notification
     * @param notification
     * @return the icon of the notification
     */
    public String getNotificationIcon(Notification notification) {
        return notificationController.getNotificationIcon(notification);
    }

    /**
     * Check if there are new notifications not read.
     * If true, load the new notifications and notify the user with a message for each new notification
     */
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
                MessageUtility.addInfo("notifyMessage", n.getText());
            }
            //Add the new notifications to the list
            notifications = tmpNotifications;
            notReadNotifications = newNotReadNotifications;
        }
    }
}
