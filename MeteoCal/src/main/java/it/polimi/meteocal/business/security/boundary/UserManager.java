/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.business.security.boundary;

import it.polimi.meteocal.business.security.entity.Event;
import it.polimi.meteocal.business.security.entity.Group;
import it.polimi.meteocal.business.security.entity.Notification;
import it.polimi.meteocal.business.security.entity.User;
import java.security.Principal;
import java.util.List;
import java.util.Set;
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

    @PersistenceContext
    private EntityManager em;
    
    @Inject
    Principal principal;

    public void save(User user) {
        user.setGroupName(Group.USER);
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
        TypedQuery<Notification> query = em.createQuery("SELECT n FROM Notification n WHERE n.user.email = :param_email", Notification.class).setParameter("param_email", principal.getName());
        return query.getResultList();
    }
    
    public int countNotReadNotifications() {
        Query query = em.createQuery("SELECT COUNT(n) FROM Notification n WHERE n.user.email = :param_email AND n.readByUser = false").setParameter("param_email", principal.getName());
        return ((Number)query.getSingleResult()).intValue();
    }
}
