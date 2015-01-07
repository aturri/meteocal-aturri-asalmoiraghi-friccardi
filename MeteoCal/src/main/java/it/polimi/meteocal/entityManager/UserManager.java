/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entityManager;

import it.polimi.meteocal.control.Utility;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entity.Group;
import java.security.Principal;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * This class manages the CRUD operation on User
 * @author Andrea
 */
@Stateless
public class UserManager {
 
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    Principal principal;

    /**
     * This method saves the user into the persistence context
     * @param user to be saved
     */
    public void save(User user) {
        user.setGroupName(Group.USER.toString());
        em.persist(user);
    }
    
    /**
     * This method updates the user
     * @param user to be updated
     */
    public void update(User user) {
        em.merge(user);
    }

    /**
     * This method removes the user
     */
    public void unregister() {
        em.remove(getLoggedUser());
    }

    /**
     * This method returns the current logged user
     * @return user
     */
    public User getLoggedUser() {
        return em.find(User.class, principal.getName());
    }
    
    /**
     * This method checks if a user exists
     * @param email of the user
     * @return true if exists
     */
    public Boolean existsUser(String email) {
        return em.find(User.class, email) != null;
    }

    /**
     * This method finds the user by email
     * @param email
     * @return user associated
     */
    public User findByEmail(String email) {
        return em.find(User.class, email);
    }
    
    /**
     * This method returns a code for the user
     * @param user
     * @return the string that represents the code
     */
    public static String getCodeFromUser(User user){
        String string=user.getEmail()+user.getCity()+user.getLastAccess().toString()+user.getPassword();
        return Utility.getHashSHA256(string);
    }
}
