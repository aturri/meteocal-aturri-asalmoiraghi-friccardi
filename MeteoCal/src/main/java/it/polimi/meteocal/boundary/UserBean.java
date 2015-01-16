/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author Andrea
 */
@Named
@RequestScoped
public class UserBean {

    @EJB
    UserManager um;
    
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
    
    public List<Event> getEventsInCalendar() {
        return new ArrayList<>(um.getLoggedUser().getEvents());
    }

    public List<Event> getEventsInvitedTo() {
        return new ArrayList<>(um.getLoggedUser().getInvitations());
    }
    
    public boolean hasInFavorite(){
        Map<String, String> params =FacesContext.getCurrentInstance().
                   getExternalContext().getRequestParameterMap();
        String email=params.get("email");
        return isThisUserInList(um.getLoggedUser().getContacts(),email);
    }
    
    /**
     * Return true if the user identified by email is in the list
     * @param list
     * @param email
     * @return 
     */
    public boolean isThisUserInList(Set<User> list,String email){
        for(User user:list){
            if(email!=null&&user.getEmail().equals(email)){
                return true;
            }
        }
        return false;
    }
}
