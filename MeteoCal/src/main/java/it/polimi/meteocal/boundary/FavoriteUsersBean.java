/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.utils.MessageUtility;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author Fabiuz
 */
@Named
@RequestScoped
public class FavoriteUsersBean {

    @EJB
    UserManager um;
    
    private String[] selectedUsers; 
    private final List<String> favoriteUsers=new ArrayList<>();
    
    @PostConstruct
    public void init() {
        for(User utente:this.um.getLoggedUser().getContacts()){
            this.favoriteUsers.add(utente.getEmail());
        }
    }
    
    
    /**
     * Delete favorite user(s)
     * NB: when we recall um.getLoggedUser, everytime it return a DIFFERENT instance of loggedUser
     * NB2: um.getLoggedUser doesn't read from the DB everytime we call it.
     * @return destination page, in this case is the same page of the call
     */
    public String deleteFavoriteUsers(){
        User loggedUser=um.getLoggedUser();
        for(String mailToDelete:this.selectedUsers){
            User y=um.findByEmail(mailToDelete);
            loggedUser.getContacts().remove(y);
        }
        um.update(loggedUser);
        MessageUtility.addInfo("contactsMessage","Users removed from favorites");
        return "";
    }

    /**
     * Get the selected user
     * @return the selectedUsers
     */
    public String[] getSelectedUsers() {
        return selectedUsers;
    }

    /**
     * Set the selected users by the email
     * @param selectedUsers the selectedUsers to set
     */
    public void setSelectedUsers(String[] selectedUsers) {
        this.selectedUsers = selectedUsers;
    }
}
