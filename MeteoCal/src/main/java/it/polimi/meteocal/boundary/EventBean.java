/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.MailControl;
import it.polimi.meteocal.control.NavigationBean;
import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.EventManager;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.Session;

/**
 *
 * @author Andrea
 */
@Named
@RequestScoped
public class EventBean {

    @Resource(name = "mail/mailSession")
    private Session mailSession;
    private MailControl mailControl;
    
    @EJB
    EventManager eventManager;
    
    @EJB
    UserManager userManager;
    
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
        }
        return this.event;
    }

    public void setEvent(Event event) {
        //guardiamo se this.id oppure il paramentro id è impostato, se si facciamo la ricerca nel DB
        /*if(this.event==null&&this.isExistsIdParam()){ 
           this.setEventByParam();
        }*/
        this.event = event;
    }
    
    public String createEvent(){       
        //setup creator
        User user = userManager.getLoggedUser(); 
        this.event.setCreator(user);
        this.event.getUsers().add(user);
        this.eventManager.save(event);
        //add to creator's calendar
        user.getEvents().add(event);
        userManager.update(user);
        //setup invitations
        if(this.invitedUsers!=null && this.invitedUsers!="") {
            String[] split = this.invitedUsers.split(",");
            for (String email : split) {
                User invitedUser=userManager.findByEmail(email);
                this.event.getInvitedUsers().add(invitedUser);
                mailControl=new MailControl(mailSession);
                try {
                    mailControl.sendMail(email, invitedUser.getName()+" "+invitedUser.getSurname(), "Invite to partecipate to "+event.getTitle(), "Dear "+invitedUser.getName()+" "+invitedUser.getSurname()+",<br />"
                            + "You are invited from "+event.getCreator().getName()+" "+event.getCreator().getSurname()+ " to partecipate to "+event.getTitle()+". Click on the follow link to see more details:<br /><br />"
                            + "<a href=\"http://localhost:8080/MeteoCal\">http://localhost:8080/MeteoCal</a>"
                            + "<br />"
                            + "<br />"
                            + "Enjoy it ;)<br />"
                            + "      MeteoCal's Team");
                } catch (MessagingException | UnsupportedEncodingException ex) {
                    Logger.getLogger(EventBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            this.eventManager.update(event);   
        }
        return NavigationBean.redirectToHome();
    }
    
    public String editEvent() {
        this.eventManager.update(event);
        return NavigationBean.redirectToHome();
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
    
    public void handleInvitedUsers() {
        String foo = this.invitedUsers;
        String[] split = foo.split(",");
        for (String splitted : split) {
            if(splitted.equals(userManager.getLoggedUser().getEmail())) {
                MessageBean.addError("You can't invite yourself");
            } else if(!userManager.existsUser(splitted)) {
                MessageBean.addWarning(splitted+" is not registered to MeteoCal");
            }

        }
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
    
    public Boolean isUserInvited(User user) {
        List<User> invited = this.getListInvitedUsers();
        for(User u: invited) {
            if(u.getEmail().equals(user.getEmail())) {
                return true;
            }
        }
        return false;
    }
    
    public Boolean isCurrentUserCreator() {
        return event.getCreator().getEmail().equals(userManager.getLoggedUser().getEmail());
    }
    
    public Boolean isVisibleForCurrentUser() {
        User currentUser = userManager.getLoggedUser();
        if(this.isCurrentUserCreator() || 
                this.event.getPublicEvent() ||
                this.isUserInvited(currentUser) ||
                this.isUserParticipant(currentUser)) {
            return true;
        }
        return false;
    }
}
