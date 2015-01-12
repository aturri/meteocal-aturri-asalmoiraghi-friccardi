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

    @PostConstruct
    public void init() {
        notifications = notificationManager.findAllNotificationsByUser(um.getLoggedUser().getEmail());
    }

    public int getNumOfNotReadNotifications() {
        return notificationManager.countNotReadNotifications(um.getLoggedUser().getEmail());
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void hasReadNotification() {
        notificationController.setAsRead(um.getLoggedUser());
    }

    public String getNotificationClass(Notification notification) {
        return notificationController.getNotificationClass(notification);
    }

    public String getNotificationIcon(Notification notification) {
        return notificationController.getNotificationIcon(notification);
    }

    public void checkNewNotifications() {
        //Load notifications not read yet
        List<Notification> newNotifications = notificationManager.findNewNotificationsByUser(um.getLoggedUser().getEmail());
        //Remove notification that were already shown
        newNotifications.removeAll(notifications);
        //The remaining notification are shown with message
        for (Notification n : newNotifications) {
            MessageBean.addInfo("notifyMessage", n.getText());
        }
        notifications.addAll(newNotifications);
    }
}
