/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entityManager;

import it.polimi.meteocal.entity.Event;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
     * Thsi method returns the event with the specified id
     * @param id of the event
     * @return the event object associated to the id
     */
    public Event findById(Integer id) {
        return em.find(Event.class, id);
    }
           
}
