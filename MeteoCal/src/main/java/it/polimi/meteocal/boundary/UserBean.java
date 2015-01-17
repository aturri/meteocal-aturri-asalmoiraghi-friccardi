/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Andrea
 */
@Named
@RequestScoped
public class UserBean {

    @EJB
    UserManager userManager;

    @Inject
    PictureBean pictureBean;
        
    public User getLoggedUser(){
        return userManager.getLoggedUser();
    }
    
    @Deprecated
    public String getName() {
        return userManager.getLoggedUser().getName();
    }
    
    @Deprecated
    public String getSurname() {
        return userManager.getLoggedUser().getSurname();
    }
    
    @Deprecated
    public String getEmail() {
        return userManager.getLoggedUser().getEmail();
    }
    
    public List<Event> getEventsInCalendar() {
        return new ArrayList<>(userManager.getLoggedUser().getEvents());
    }

    public List<Event> getEventsInvitedTo() {
        return new ArrayList<>(userManager.getLoggedUser().getInvitations());
    }
    
    public boolean hasInFavorite(){
        Map<String, String> params =FacesContext.getCurrentInstance().
                   getExternalContext().getRequestParameterMap();
        String email=params.get("email");
        return isThisUserInList(userManager.getLoggedUser().getContacts(),email);
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

    /**
     * @return the picture
     */
    public StreamedContent getPicture() {
        return pictureBean.getPictureFromUser(userManager.getLoggedUser());
    }

}
