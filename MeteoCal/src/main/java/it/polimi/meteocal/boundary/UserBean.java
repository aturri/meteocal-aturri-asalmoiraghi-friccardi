/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.utils.Utility;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
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
        
    public User getLoggedUser(){
        return userManager.getLoggedUser();
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
        return Utility.getPictureFromUser(userManager.getLoggedUser());
    }
    
    public List<User> getContacts(){
        List<User> contacts = new ArrayList<>(userManager.getLoggedUser().getContacts());
        contacts.sort(new UserComparator());
        return contacts;
    }

    static class UserComparator  implements Comparator<User>{
        
        private UserComparator() {
        }

        @Override
        public int compare(User o1, User o2) {
            String name1 = o1.getName() + o1.getSurname();
            String name2 = o2.getName() + o2.getSurname();
            return name1.compareToIgnoreCase(name2);
        }
    }
}
