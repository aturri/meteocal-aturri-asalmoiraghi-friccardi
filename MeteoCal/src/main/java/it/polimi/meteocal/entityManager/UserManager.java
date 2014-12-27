/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entityManager;

import it.polimi.meteocal.boundary.RecoverPasswordBean;
import it.polimi.meteocal.control.Utility;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entity.Notification;
import it.polimi.meteocal.entity.Group;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author Andrea
 */
@Stateless
public class UserManager {
    
    private static final int MAX_NOTIF = 20;

    @PersistenceContext
    private EntityManager em;
    
    @Inject
    Principal principal;

    public void save(User user) {
        user.setGroupName(Group.USER.toString());
        em.persist(user);
    }
    
    //New method
    public void update(User user) {
        em.merge(user);
    }

    public void unregister() {
        em.remove(getLoggedUser());
    }

    public User getLoggedUser() {
        return em.find(User.class, principal.getName());
    }
    
    public Boolean existsUser(String email) {
        return em.find(User.class, email) != null;
    }
    
    public List<Notification> findAllNotifications() {
        TypedQuery<Notification> query = em.createQuery("SELECT n FROM Notification n WHERE n.user.email = :param_email", Notification.class).setParameter("param_email", principal.getName()).setMaxResults(MAX_NOTIF);
        return query.getResultList();
    }
    
    public int countNotReadNotifications() {
        Query query = em.createQuery("SELECT COUNT(n) FROM Notification n WHERE n.user.email = :param_email AND n.readByUser = false").setParameter("param_email", principal.getName());
        return ((Number)query.getSingleResult()).intValue();
    }

    /**
     * Find into the database the user with the selected email
     * @param email
     * @return the user selected
     */
    public User findByEmail(String email) {
        return em.find(User.class, email);
    }
    
    /**
     * Privide a code that expired if the user logs in into the system or changes the password
     * @param user
     * @return the string that represents the code
     */
    public String getCodeFromUser(User user){
        String string=user.getEmail()+user.getCity()+user.getLastAccess().toString()+user.getPassword();
        return Utility.getHashSHA256(string);
    }
}
