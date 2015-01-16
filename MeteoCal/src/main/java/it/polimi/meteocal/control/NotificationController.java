/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.Notification;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.NotificationManager;
import it.polimi.meteocal.entityManager.UserManager;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 *
 * @author Andrea
 */
@Stateless
public class NotificationController {

    @Inject
    private MailController mailControl;
    
    @EJB
    UserManager userManager;
    
    @EJB
    NotificationManager notificationManager;
    
    private final Map<KindOfNotification,String> messages=new EnumMap<>(KindOfNotification.class);

    /**
     * Initialize the Map with default subjects and messages
     */
    @PostConstruct
    private void init(){
        messages.put(KindOfNotification.INVITEDTOEVENT, "You are invited to partecipate to %s.");
        messages.put(KindOfNotification.EVENTCANCELLED, null);
        messages.put(KindOfNotification.EVENTUPDATED, "%s has been updated.");         
        messages.put(KindOfNotification.EVENTUPDATED1, "%s: you have to accept invitation again, changed dates!");         
        messages.put(KindOfNotification.ALERTWEATHER1, "%s: bad weather!");         
        messages.put(KindOfNotification.ALERTWEATHER3, "%s: change date with a sunny day!");         
        messages.put(KindOfNotification.WEATHERCHANGED, "%s: changed weather conditions!");         
    }  
    
    /**
     * This method sends a notificatin to a list of users
     * @param users list of users
     * @param kindOfNotif type of notification
     * @param event referring to
     */
    public void sendNotificationToGroup(List<User> users, KindOfNotification kindOfNotif, Event event) {
        for(User u: users) {
            this.sendNotification(u.getEmail(), kindOfNotif, event);
        }
    }
    
    /**
     * This methods sends a notification
     * @param destination email of the user
     * @param kindOfNotif type of notification
     * @param event referring to
     */
    public void sendNotification(String destination, KindOfNotification kindOfNotif, Event event) {
        User user = userManager.findByEmail(destination);
        String message = "";
        char type = 'x';
        switch(kindOfNotif){
            case INVITEDTOEVENT:
                message = String.format(this.messages.get(KindOfNotification.INVITEDTOEVENT), event.getTitle());
                type = 'A';
                mailControl.sendMail(destination, KindOfEmail.INVITEDTOEVENT,event);
                createNotification(user, event, message, type);
                break;
            case EVENTCANCELLED:
                mailControl.sendMail(destination, KindOfEmail.EVENTCANCELLED,event);
                break;
            case EVENTUPDATED:
                message = String.format(this.messages.get(KindOfNotification.EVENTUPDATED), event.getTitle());
                type = 'B';
                mailControl.sendMail(destination, KindOfEmail.EVENTUPDATED,event);
                createNotification(user, event, message, type);
                break;
            case EVENTUPDATED1:
                message = String.format(this.messages.get(KindOfNotification.EVENTUPDATED1), event.getTitle());
                type = 'B';
                mailControl.sendMail(destination, KindOfEmail.EVENTUPDATED1,event);
                createNotification(user, event, message, type);
                break;
            case ALERTWEATHER1:
                message = String.format(this.messages.get(KindOfNotification.ALERTWEATHER1), event.getTitle());
                type = 'C';
                mailControl.sendMail(destination, KindOfEmail.ALERTWEATHER1,event);
                createNotification(user, event, message, type);
                break;
            case ALERTWEATHER3:
                message = String.format(this.messages.get(KindOfNotification.ALERTWEATHER3), event.getTitle());
                type = 'D';
                mailControl.sendMail(destination, KindOfEmail.ALERTWEATHER3,event);
                createNotification(user, event, message, type);
                break;
            case WEATHERCHANGED:
                message = String.format(this.messages.get(KindOfNotification.WEATHERCHANGED), event.getTitle());
                type = 'E';
                mailControl.sendMail(destination, KindOfEmail.WEATHERCHANGED,event);
                createNotification(user, event, message, type);
                break;
        }
    }
    
    /**
     * This method creates the notification
     * @param user recipient
     * @param event referring to
     * @param message to be shown
     * @param type internal type
     */
    private void createNotification(User user, Event event, String message, char type) {
        Notification notif = new Notification();
        notif.setEvent(event);
        notif.setReadByUser(false);
        notif.setText(message);
        notif.setType(type);
        notif.setUser(user);
        notificationManager.save(notif);
    }
    
    /**
     * This method destroys all notifications associated to the specified event
     * @param event to find notifications associated
     */
    public void destroyNotifications(Event event) {
        List<Notification> toBeDeleted = notificationManager.findAllNotificationsByEvent(event.getId());
        for(Notification n: toBeDeleted) {
            notificationManager.remove(n);
        }
    }
    
    /**
     * This method updates all user's notifications, marking them as read
     * @param user whose notifications are marked as read
     */
    public void setAsRead(User user) {
        List<Notification> toBeMarkedAsRead = notificationManager.findAllNotificationsByUser(user.getEmail());
        for(Notification n: toBeMarkedAsRead) {
            n.setReadByUser(true);
            notificationManager.update(n);
        }
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
