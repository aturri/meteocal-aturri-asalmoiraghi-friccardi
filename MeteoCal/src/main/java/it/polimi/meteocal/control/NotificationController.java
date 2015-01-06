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
        messages.put(KindOfNotification.INVITEDTOEVENT, "You are invited to partecipate to %s. Click for further details.");
        messages.put(KindOfNotification.EVENTCANCELLED, "\"%s\" has been cancelled!");
        messages.put(KindOfNotification.EVENTUPDATED, "\"%s\" has been updated. Click for further details.");         
        messages.put(KindOfNotification.SEVEREWEATHER, "\"%s\": bad weather!");         
        messages.put(KindOfNotification.SEVEREWEATHER_MOD, "\"%s\": bad weather! Click for further details.");         
        messages.put(KindOfNotification.WEATHERCHANGED, "\"%s\": changed weather conditions!");         
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
                break;
            case EVENTCANCELLED:
                message = String.format(this.messages.get(KindOfNotification.EVENTCANCELLED), event.getTitle());
                type = 'B';
                mailControl.sendMail(destination, KindOfEmail.EVENTCANCELLED,event);
                break;
            case EVENTUPDATED:
                message = String.format(this.messages.get(KindOfNotification.EVENTUPDATED), event.getTitle());
                type = 'C';
                mailControl.sendMail(destination, KindOfEmail.EVENTUPDATED,event);
                break;
            case SEVEREWEATHER:
                message = String.format(this.messages.get(KindOfNotification.SEVEREWEATHER), event.getTitle());
                type = 'D';
                mailControl.sendMail(destination, KindOfEmail.SEVEREWEATHER,event);
                break;
            case SEVEREWEATHER_MOD:
                message = String.format(this.messages.get(KindOfNotification.SEVEREWEATHER_MOD), event.getTitle());
                type = 'E';
                mailControl.sendMail(destination, KindOfEmail.SEVEREWEATHER_MOD,event);
                break;
            case WEATHERCHANGED:
                message = String.format(this.messages.get(KindOfNotification.WEATHERCHANGED), event.getTitle());
                type = 'F';
                mailControl.sendMail(destination, KindOfEmail.WEATHERCHANGED,event);
                break;
        }
        createNotification(user, event, message, type);
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
     * This method destroys all notifications associated to the specified user and event
     * @param user to find notifications associated
     * @param event to find notifications associated
     */
    public void destroyNotifications(User user, Event event) {
        List<Notification> toBeDeleted = notificationManager.findAllNotificationsByUserAndEvent(user.getEmail(), event.getId());
        for(Notification n: toBeDeleted) {
            notificationManager.remove(n);
        }
    }
}
