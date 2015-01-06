/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entityManager;

import it.polimi.meteocal.entity.Notification;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * This class manages the CRUD operation on Notification
 * @author Andrea
 */
@Stateless
public class NotificationManager {

    private static final int MAX_NOTIF = 20;

    @PersistenceContext
    private EntityManager em;
    
    /**
     * This method saves the notification into the persistence context
     * @param notification to be saved
     */    
    public void save(Notification notification) {
        em.persist(notification);
    }
 
    /**
     * This method updates the notification into the persistence context
     * @param notification to be updated
     */
    public void update(Notification notification) {
        em.merge(notification);
    }
 
    /**
     * This method removes the notification from the persistence context
     * @param notification to be removed
     */    
    public void remove(Notification notification) {
        Notification toBeDeleted = em.merge(notification);
        em.remove(toBeDeleted);
    }
    
    /**
     * This method returns all notifications (until limit) for the specified user's email
     * @param email of the user
     * @return list of notifications
     */
    public List<Notification> findAllNotificationsByUser(String email) {
        TypedQuery<Notification> query = em.createQuery("SELECT n FROM Notification n WHERE n.user.email = :param_email", Notification.class).setParameter("param_email", email).setMaxResults(MAX_NOTIF);
        return query.getResultList();
    }
    
    /**
     * This method returns all notifications associated to event's id
     * @param id
     * @return list of all notifications
     */
    public List<Notification> findAllNotificationsByEvent(Integer id) {
        TypedQuery<Notification> query = em.createQuery("SELECT n FROM Notification n WHERE n.event.id = :param_id", Notification.class).setParameter("param_id", id);
        return query.getResultList();       
    }
    
    /**
     * This method returns the number of notifications to be read for the specified user's email
     * @param email of the user
     * @return number of not read notifications
     */
    public int countNotReadNotifications(String email) {
        Query query = em.createQuery("SELECT COUNT(n) FROM Notification n WHERE n.user.email = :param_email AND n.readByUser = false").setParameter("param_email", email);
        return ((Number)query.getSingleResult()).intValue();
    }
}
