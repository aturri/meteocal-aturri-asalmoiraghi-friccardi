/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entity.Weather;
import it.polimi.meteocal.entityManager.EventManager;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.exception.EventOverlapException;
import it.polimi.meteocal.exception.IllegalEventDateException;
import it.polimi.meteocal.exception.IllegalInvitedUserException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * This is the business logic class for Events
 * @author Andrea
 */
@Stateless
public class EventController {
    
    @Inject
    private NotificationController notificationControl;
    
    @Inject
    private WeatherController weatherControl;

    @EJB
    EventManager eventManager;
    
    @EJB
    UserManager userManager;
    
    private Event event;
    private String invitedUsers;
    
    /**
     * This method returns true if the dates selected for the event are coherent, i.e. the end date is after the begin date
     * @return is end date legal?
     */
    private Boolean isEndDateLegal() {
        return !event.getBeginDate().after(event.getEndDate());
    }
    
    /**
     * This method returns true if there are overlaps with other events
     * @return is there some overlap?
     */
    public Boolean areThereOverlaps() {
        Set<Event> userEvents = userManager.getLoggedUser().getEvents();
        userEvents.remove(event); //the current event overlaps to the current event if already prensent
        Date a = event.getBeginDate();
        Date b = event.getEndDate();
        for(Event e: userEvents) {
            Date c = e.getBeginDate();
            Date d = e.getEndDate();
            if((c.after(a) && d.before(b)) || (c.after(a) && b.after(c)) || (c.before(a) && d.after(a))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * This method returns true if all invited users in the list are legal for create
     * @return are all the invited users legal?
     */
    private Boolean areInvitedUserLegalForCreate() {
        if(invitedUsers==null || invitedUsers.equals("")) {
            return true;
        }
        int error = 0;
        String foo = invitedUsers;
        String[] split = foo.split(",");
        List<String> tmpUsersList = new ArrayList<>();
        for (String splitted : split) {
            if(splitted.equals(userManager.getLoggedUser().getEmail())
                    || !userManager.existsUser(splitted)
                    || tmpUsersList.contains(splitted)) {
                error++;
            } else {
                tmpUsersList.add(splitted);
            }
        }
        return error==0;
    }
    
    /**
     * This method returns true if all invited users in the list are legal for edit
     * @return are all the invited users legal?
     */
    private Boolean areInvitedUserLegalForEdit() {
        if(invitedUsers==null || invitedUsers.equals("")) {
            return true;
        }
        if(!this.areInvitedUserLegalForCreate()) {
            return false;
        }
        int error = 0;
        String foo = invitedUsers;
        String[] split = foo.split(",");
        for (String splitted : split) {
            if(event.getInvitedUsers().contains(userManager.findByEmail(splitted)) || 
                        event.getUsers().contains(userManager.findByEmail(splitted))) {
                error++;
            }
        }
        return error==0;
    }
    
    /**
     * This method generates the weather for the event
     */
    private void attachWeather() {
        if(event.getCity()!=null && event.getBeginDate()!=null) {
            Weather weather = weatherControl.createWeather(event.getCity(), event.getBeginDate(), true);
            if(weather!=null) {
                event.setWeather(weather);
            }
        } else {
            event.setWeather(null);
        }
    }
    
    /**
     * This method creates the event
     * @param event to be created
     * @param invitedUsers list of users to be invited (comma separated)
     * @throws EventOverlapException
     * @throws IllegalInvitedUserException
     * @throws IllegalEventDateException 
     */
    public void createEvent(Event event, String invitedUsers) throws EventOverlapException, IllegalInvitedUserException, IllegalEventDateException {
        this.event = event;
        this.invitedUsers = invitedUsers;
        //controls
        if(this.areThereOverlaps()) {
            throw new EventOverlapException();
        }
        if(!this.areInvitedUserLegalForCreate()) {
            throw new IllegalInvitedUserException();
        }
        if(!this.isEndDateLegal()) {
            throw new IllegalEventDateException();
        }
        this.attachWeather();
        //setup creator
        User user = userManager.getLoggedUser(); 
        this.event.setCreator(user);
        this.event.getUsers().add(user);
        this.eventManager.save(this.event);
        //add to creator's calendar
        user.getEvents().add(this.event);
        userManager.update(user);
        //setup invitations
        if(this.invitedUsers!=null && !"".equals(this.invitedUsers)) {
            String[] split = this.invitedUsers.split(",");
            for (String email : split) {
                User invitedUser = userManager.findByEmail(email);
                this.event.getInvitedUsers().add(invitedUser);
                invitedUser.getInvitations().add(this.event);
                userManager.update(invitedUser);
                notificationControl.sendNotification(email, KindOfNotification.INVITEDTOEVENT, this.event);
            }
            eventManager.update(this.event);   
        }        
    }
    
    /**
     * This method edits the event
     * @param event to be updated
     * @param invitedUsers list of users to be invited (comma separated)
     * @throws EventOverlapException
     * @throws IllegalInvitedUserException
     * @throws IllegalEventDateException 
     */
    public void editEvent(Event event, String invitedUsers) throws EventOverlapException, IllegalInvitedUserException, IllegalEventDateException {
        this.event = event;
        this.invitedUsers = invitedUsers;
        if(this.areThereOverlaps()) {
            throw new EventOverlapException();
        }
        if(!this.areInvitedUserLegalForEdit()) {
            throw new IllegalInvitedUserException();
        }
        if(!this.isEndDateLegal()) {
            throw new IllegalEventDateException();
        }
        //weather & update
        Integer widx = -1;
        if(this.event.getWeather()!=null) widx = this.event.getWeather().getId();
        this.attachWeather();
        this.eventManager.update(this.event);
        this.weatherControl.destroyWeather(widx);
        //notify users
        Set<User> participants = this.event.getUsers();
        participants.remove(this.event.getCreator());
        notificationControl.sendNotificationToGroup(participants, KindOfNotification.EVENTUPDATED, this.event);
        notificationControl.sendNotificationToGroup(this.event.getInvitedUsers(), KindOfNotification.EVENTUPDATED, this.event);
        //setup invitations
        if(this.invitedUsers!=null && !"".equals(this.invitedUsers)) {
            String[] split = this.invitedUsers.split(",");
            for (String email : split) {
                User invitedUser = userManager.findByEmail(email);
                this.event.getInvitedUsers().add(invitedUser);
                invitedUser.getInvitations().add(this.event);
                userManager.update(invitedUser);
                notificationControl.sendNotification(email, KindOfNotification.INVITEDTOEVENT, this.event);
                //mailControl.sendMail(email, KindOfEmail.INVITEDTOEVENT,this.event);
            }
            this.eventManager.update(this.event);   
        }
        User user = userManager.getLoggedUser(); 
        user.getEvents().add(this.event);
        userManager.update(user);
    }
    
    /**
     * This method removes the event from all calendars
     * @param event to be removed
     */
    public void deleteEvent(Event event) {
        this.event = event;
        List<User> participatingUsers = this.getParticipatingUsers();
        for(User u: participatingUsers) {
            u.getEvents().remove(this.event);
            this.userManager.update(u);
        }
        List<User> listInvitedUsers = this.getListInvitedUsers();
        for(User u: listInvitedUsers) {
            u.getInvitations().remove(this.event);
            this.userManager.update(u);
        }    
        Integer widx = -1;
        if(this.event.getWeather()!=null) widx = this.event.getWeather().getId();
        //notify users
        Set<User> participants = this.event.getUsers();
        participants.remove(this.event.getCreator());
        notificationControl.sendNotificationToGroup(participants, KindOfNotification.EVENTCANCELLED, this.event);
        notificationControl.sendNotificationToGroup(this.event.getInvitedUsers(), KindOfNotification.EVENTCANCELLED, this.event);
        //delete
        notificationControl.destroyNotifications(this.event);
        this.eventManager.delete(this.event);
        this.weatherControl.destroyWeather(widx);
    }
    
    /**
     * This method returns the list of invited users for current event
     * @return list of invited users
     */
    private List<User> getListInvitedUsers() {
        return new ArrayList<>(event.getInvitedUsers());
    }
    
    /**
     * This method returns the list of participating users for current event
     * @return list of participating users
     */
    private List<User> getParticipatingUsers() {
        return new ArrayList<>(event.getUsers());
    }
    
    /**
     * This method removes the invitation for the current user and event
     */
    private void removeInvitation() {
        User currentUser = userManager.getLoggedUser();      
        event.getInvitedUsers().remove(currentUser);
        eventManager.update(event);   
        currentUser.getInvitations().remove(event);
        userManager.update(currentUser);
    }
    
    /**
     * This method accepts the invitation for current user
     * @param event accepted
     * @throws EventOverlapException 
     */
    public void acceptInvitation(Event event) throws EventOverlapException {
        this.event = event;
        if(this.areThereOverlaps()) {
            throw new EventOverlapException();
        }
        this.removeInvitation();
        User user = userManager.getLoggedUser();        
        this.event.getUsers().add(user);
        eventManager.update(this.event);
        user.getEvents().add(this.event);
        userManager.update(user);
    }
    
    /**
     * This method refuses the invitation for current user
     * @param event refused
     */
    public void refuseInvitation(Event event) {
        this.event = event;
        this.removeInvitation();
    }
    
    /**
     * This method removes the event from one user's calendar
     * @param event to be removed
     */
    public void removeFromCalendar(Event event) {
        this.event = event;
        User currentUser = userManager.getLoggedUser();        
        this.event.getUsers().remove(currentUser);
        eventManager.update(this.event);   
        currentUser.getEvents().remove(this.event);
        userManager.update(currentUser);
    }
    
    /**
     * This method removes an invited user
     * @param event from which to remove the user
     * @param email of the user to be removed
     */
    public void removeInvitedUser(Event event, String email) {
        this.event = event;
        User user = userManager.findByEmail(email);
        this.event.getInvitedUsers().remove(user);
        eventManager.update(this.event);   
        user.getInvitations().remove(this.event);
        userManager.update(user);
    }

    /**
     * This method removes a participating user
     * @param event from which to remove the user
     * @param email of the user to be removed
     */
    public void removeParticipant(Event event, String email) {
        this.event = event;
        User user = userManager.findByEmail(email);
        this.event.getUsers().remove(user);
        eventManager.update(this.event);   
        user.getEvents().remove(this.event);
        userManager.update(user);
    }
}
