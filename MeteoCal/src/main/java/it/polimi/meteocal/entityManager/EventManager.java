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
    
    public void save(Event event) {
        em.persist(event);
    }
    
    public void update(Event event){
        em.merge(event);
    }
    
    public Boolean existsEvent(Integer id) {
        return em.find(Event.class, id) != null;
    }
    
    public Event findById(Integer id) {
        return em.find(Event.class, id);
    }
           
}
