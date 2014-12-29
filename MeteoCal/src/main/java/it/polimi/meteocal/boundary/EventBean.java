/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.KindOfEmail;
import it.polimi.meteocal.control.MailController;
import it.polimi.meteocal.control.NavigationBean;
import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.EventManager;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.entityManager.WeatherController;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.mail.MessagingException;

/**
 *
 * @author Andrea
 */
@Named
@RequestScoped
public class EventBean {
    
    @Inject
    private MailController mailControl;
    
    @EJB
    EventManager eventManager;
    
    @EJB
    UserManager userManager;
    
    @EJB
    WeatherController wc;
    
    private String invitedUsers;
    
    //È necessario per il form dove si modifica l'evento, f:metadata>f:viewParam vogliono un setter su questo attributo
    private Event event;

    public EventBean() {
    }

    public Event getEvent() {
        if(this.event==null&&this.isExistsIdParam()){
            this.setEventByParam();
        }else if(this.event==null){
            this.event=new Event();
            this.event.setBeginDate(this.getToday());
        }
        return this.event;
    }
    
    public String getWeather() {
        return this.wc.test();
    }

    public void setEvent(Event event) {
        //guardiamo se this.id oppure il paramentro id è impostato, se si facciamo la ricerca nel DB
        /*if(this.event==null&&this.isExistsIdParam()){ 
           this.setEventByParam();
        }*/
        this.event = event;
    }
    
    public Boolean isEndDateLegal() {
        if(event.getBeginDate().after(event.getEndDate())) {
            MessageBean.addError("errorMsg","End date must be after begin date!");
            return false;
        }
        return true;
    }
    
    public String createEvent(){    
        if(!this.areInvitedUserLegalForCreate() || this.areThereOverlaps() || !this.isEndDateLegal()) {
            return "";
        }
        
        //setup creator
        User user = userManager.getLoggedUser(); 
        this.event.setCreator(user);
        this.event.getUsers().add(user);
        this.eventManager.save(event);
        //add to creator's calendar
        user.getEvents().add(event);
        userManager.update(user);
        //setup invitations
        if(this.invitedUsers!=null && !"".equals(this.invitedUsers)) {
            String[] split = this.invitedUsers.split(",");
            for (String email : split) {
                User invitedUser=userManager.findByEmail(email);
                this.event.getInvitedUsers().add(invitedUser);
                invitedUser.getInvitations().add(event);
                userManager.update(invitedUser);
                mailControl.sendMail(email, KindOfEmail.INVITEDTOEVENT,this.event);
            }
            this.eventManager.update(event);   
        }
        return NavigationBean.redirectToHome();
    }
    
    public String editEvent() {
        if(!this.areInvitedUserLegalForEdit() || this.areThereOverlaps() || !this.isEndDateLegal()) {
            return "";
        }
        this.eventManager.update(event);
        //setup invitations
        if(this.invitedUsers!=null && !"".equals(this.invitedUsers)) {
            String[] split = this.invitedUsers.split(",");
            for (String email : split) {
                User invitedUser=userManager.findByEmail(email);
                this.event.getInvitedUsers().add(invitedUser);
                invitedUser.getInvitations().add(event);
                userManager.update(invitedUser);
                mailControl.sendMail(email, KindOfEmail.INVITEDTOEVENT,this.event);
            }
            this.eventManager.update(event);   
        }
        return NavigationBean.redirectToEventDetailsPage(event.getId());
    }
    
    public String deleteEvent() {
        List<User> participatingUsers = this.getParticipatingUsers();
        for(User u: participatingUsers) {
            u.getEvents().remove(event);
            this.userManager.update(u);
        }
        List<User> listInvitedUsers = this.getListInvitedUsers();
        for(User u: listInvitedUsers) {
            u.getInvitations().remove(event);
            this.userManager.update(u);
        }    
        this.eventManager.delete(event);
        return NavigationBean.redirectToHome();
    }
    
    private void removeInvitation() {
        User currentUser = userManager.getLoggedUser();      
        event.getInvitedUsers().remove(currentUser);
        eventManager.update(event);   
        currentUser.getInvitations().remove(event);
        userManager.update(currentUser);
    }
    
    public String acceptInvitation() {
        if(this.areThereOverlaps()) {
            return "";
        }
        this.removeInvitation();
        User user = userManager.getLoggedUser();        
        event.getUsers().add(user);
        eventManager.update(event);
        user.getEvents().add(event);
        userManager.update(user);
        return NavigationBean.redirectToHome();
    }
    
    public String refuseInvitation() {
        this.removeInvitation();
        return NavigationBean.redirectToHome();
    }
    
    public String removeFromMyCalendar() {
        User currentUser = userManager.getLoggedUser();        
        event.getUsers().remove(currentUser);
        eventManager.update(event);   
        currentUser.getEvents().remove(event);
        userManager.update(currentUser);
        return NavigationBean.redirectToHome();
    }
    
    public String removeParticipant(String email) {
        User user = userManager.findByEmail(email);
        event.getUsers().remove(user);
        eventManager.update(event);   
        user.getEvents().remove(event);
        userManager.update(user);   
        return NavigationBean.redirectToEventDetailsPage(event.getId());
    }
    
    public Boolean canUserBeRemovedFromParticipants(String email) {
        return !(!isCurrentUserCreator() || email.equals(userManager.getLoggedUser().getEmail()));
    }
    
    public String removeInvitation(String email) {
        User user = userManager.findByEmail(email);
        event.getInvitedUsers().remove(user);
        eventManager.update(event);   
        user.getInvitations().remove(event);
        userManager.update(user);        
        return NavigationBean.redirectToEventDetailsPage(event.getId());
    }
 
    /**
     * Dice se esiste un paramentro "id"
     * @return 
     */
    public boolean isExistsIdParam(){
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().containsKey("id");
    }
    
    /**
     * Prende dal GET il parametro id e trova l'evento corrispondente
     */
    public void setEventByParam() throws RuntimeException {
        if(this.isExistsIdParam()){
            String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
            this.event=eventManager.findById(Integer.parseInt(idParam));
        }else{
            throw new RuntimeException();
        }
    }
    
    public Date getToday() {
        return new Date();
    }
   
    //METEDI UTILI PER LA VISUALIZZAZIONE TESTUALE
    public String eventIndoor() {
        if(this.event.getIndoor()) {
            return "Indoor";
        }
        return "Outdoor";
    }
    
    public String eventVisibility() {
         if(this.event.getPublicEvent()) {
            return "Public";
        }
        return "Private";       
    }
        
    public String eventBegin() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm",Locale.ENGLISH);
        return sdf.format(event.getBeginDate());
    }
    
    public String eventEnd() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm",Locale.ENGLISH);
        return sdf.format(event.getEndDate());
    }
    
    public void handleInvitedUsersForCreate() {
        if(this.invitedUsers!=null && !"".equals(this.invitedUsers)) {
            String foo = this.invitedUsers;
            String[] split = foo.split(",");
            List<String> tmpUsersList = new ArrayList<>();
            for (String splitted : split) {
                if(splitted.equals(userManager.getLoggedUser().getEmail())) {
                    MessageBean.addWarning("errorMsg","You can't invite yourself");
                } else if(!userManager.existsUser(splitted)) {
                    MessageBean.addWarning("errorMsg",splitted+" is not registered to MeteoCal");
                } else if(tmpUsersList.contains(splitted)) {
                    MessageBean.addWarning("errorMsg",splitted+" has been invited more than once");
                } else {
                    tmpUsersList.add(splitted);
                }
            }
        }
    }
    
    public void handleInvitedUsersForEdit() {
        if(this.invitedUsers!=null && !"".equals(this.invitedUsers)) {
            this.handleInvitedUsersForCreate();
            String foo = this.invitedUsers;
            String[] split = foo.split(",");
            for (String splitted : split) {
                if(event.getInvitedUsers().contains(userManager.findByEmail(splitted)) || 
                            event.getUsers().contains(userManager.findByEmail(splitted))) {
                    MessageBean.addWarning("errorMsg",splitted+" has already been invited");
                }
            }
        }
    }
    
    public Boolean areThereOverlaps() {
        Set<Event> userEvents = userManager.getLoggedUser().getEvents();
        userEvents.remove(this.event); //the current event overlaps to the current event if already prensent
        Date a = this.event.getBeginDate();
        Date b = this.event.getEndDate();
        for(Event e: userEvents) {
            Date c = e.getBeginDate();
            Date d = e.getEndDate();
            if((c.after(a) && d.before(b)) || (c.after(a) && b.after(c)) || (c.before(a) && d.after(a))) {
                MessageBean.addError("errorMsg","This event overlaps with an existing one!");
                return true;
            }
        }
        return false;
    }
    
    public Boolean areInvitedUserLegalForCreate() {
        if(this.invitedUsers==null || this.invitedUsers.equals("")) {
            return true;
        }
        int error = 0;
        String foo = this.invitedUsers;
        String[] split = foo.split(",");
        List<String> tmpUsersList = new ArrayList<>();
        for (String splitted : split) {
            if(splitted.equals(userManager.getLoggedUser().getEmail())) {
                error++;
                MessageBean.addError("errorMsg","You can't invite yourself");
            } else if(!userManager.existsUser(splitted)) {
                error++;
                MessageBean.addError("errorMsg",splitted+" is not registered to MeteoCal");
            } else if(tmpUsersList.contains(splitted)) {
                error++;
                MessageBean.addError("errorMsg",splitted+" has been invited more than once");
            } else {
                tmpUsersList.add(splitted);
            }
        }
        return error==0;
    }
    
    public Boolean areInvitedUserLegalForEdit() {
        if(this.invitedUsers==null || this.invitedUsers.equals("")) {
            return true;
        }
        if(!this.areInvitedUserLegalForCreate()) {
            return false;
        }
        int error = 0;
        String foo = this.invitedUsers;
        String[] split = foo.split(",");
        for (String splitted : split) {
            if(event.getInvitedUsers().contains(userManager.findByEmail(splitted)) || 
                        event.getUsers().contains(userManager.findByEmail(splitted))) {
                error++;
                MessageBean.addError("errorMsg",splitted+" has already been invited");
            }
        }
        return error==0;
    }

    public String getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(String invitedUsers) {
        this.invitedUsers = invitedUsers;
    }

    public List<User> getListInvitedUsers() {
        return new ArrayList<>(event.getInvitedUsers());
    }
    
    public List<User> getParticipatingUsers() {
        return new ArrayList<>(event.getUsers());
    }
    
    public Boolean isUserParticipant(User user) {
       List<User> participants = this.getParticipatingUsers();
        for(User u: participants) {
            if(u.getEmail().equals(user.getEmail())) {
                return true;
            }
        }
        return false;
    }
    
    public Boolean isCurrentUserCreator() {
        return event.getCreator().getEmail().equals(userManager.getLoggedUser().getEmail());
    }
    
    public Boolean isUserInvited(User user) {
        List<User> invited = this.getListInvitedUsers();
        for(User u: invited) {
            if(u.getEmail().equals(user.getEmail())) {
                return true;
            }
        }
        return false;      
    }
    
    public Boolean isCurrentUserInvitedTo(Integer id) {
        this.event = eventManager.findById(id);
        User user = userManager.getLoggedUser();
        return this.isUserInvited(user);
    }
    
    public Boolean isCurrentUserParticipatingTo(Integer id) {
        this.event = eventManager.findById(id);
        User user = userManager.getLoggedUser();
        return this.isUserParticipant(user);
    }
    
    public Boolean isVisibleForCurrentUser(Integer id) {
        if(this.event == null) {
            this.event = eventManager.findById(id);
        }
        User currentUser = userManager.getLoggedUser();
        return this.isCurrentUserCreator() || 
                this.event.getPublicEvent() ||
                this.isUserInvited(currentUser) ||
                this.isUserParticipant(currentUser);
    }
}
