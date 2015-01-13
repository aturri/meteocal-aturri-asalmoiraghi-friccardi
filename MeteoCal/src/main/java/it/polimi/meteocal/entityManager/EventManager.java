/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entityManager;

import it.polimi.meteocal.entity.Event;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

/**
 * This class manages the CRUD operation on Event
 * @author Andrea
 */
@Stateless
public class EventManager {

    @PersistenceContext
    private EntityManager em;
    
    /**
     * This method saves the event into the persistence context
     * @param event to be saved
     */
    public void save(Event event) {
        em.persist(event);
    }
    
    /**
     * This method updates the event into the persistence context
     * @param event to be updated
     */
    public void update(Event event) {
        em.merge(event);
    }
    
    /**
     * This method deletes the event from the persistence context
     * @param event to be deleted
     */
    public void delete(Event event) {
        Event toBeDeleted = em.merge(event);
        em.remove(toBeDeleted);
    }
    
    /**
     * This method tells if exists an event with the specified id
     * @param id of the event for which you want to know existence
     * @return true if the event exists
     */
    public Boolean existsEvent(Integer id) {
        return em.find(Event.class, id) != null;
    }
    
    /**
     * This method returns the event with the specified id
     * @param id of the event
     * @return the event object associated to the id
     */
    public Event findById(Integer id) {
        return em.find(Event.class, id);
    }
    
    /**
     * This method finds all event objects with future begin date
     * @return list of event objects with begin date >= current date
     */
    public List<Event> findAllFuture() {
        TypedQuery<Event> query = em.createQuery("SELECT e FROM Event e WHERE e.beginDate >= CURRENT_TIMESTAMP", Event.class);
        return query.getResultList();
    }

    /**
     * This method finds all event objects that start on a specified date
     * @param date where to search events
     * @return list of event objects with begin date = specified date
     */
    public List<Event> findTomorrowEvents(Date date) {
        List<Event> events = new ArrayList<>();
        try {
            String dateString = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH).format(date);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd HH:mm", Locale.ENGLISH);
            Date date1 = format.parse(dateString+" 00:00");
            Date date2 = format.parse(dateString+" 23:59");
            TypedQuery<Event> query = em.createQuery("SELECT e FROM Event e WHERE e.beginDate >= :bdate and e.endDate <= :edate", Event.class).
                    setParameter("bdate", date1, TemporalType.TIMESTAMP).setParameter("edate", date2, TemporalType.TIMESTAMP);
            events = query.getResultList();
        } catch (ParseException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return events;
    }    
}
