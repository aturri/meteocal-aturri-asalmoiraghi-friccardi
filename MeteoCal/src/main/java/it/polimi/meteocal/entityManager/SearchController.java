/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entityManager;

import it.polimi.meteocal.boundary.MessageBean;
import it.polimi.meteocal.entity.Event;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author andrea
 */
@Stateless
public class SearchController {

    @EJB
    UserManager um;
    @EJB
    EventManager evm;
    @PersistenceContext
    private EntityManager em;

    public List<Event> getEvents(String keyword) {
        TypedQuery<Event> query = em.createQuery(
                "SELECT e FROM Event e WHERE e.publicEvent = 1 AND e.title LIKE :param_keyword",
                Event.class).setParameter("param_keyword", "%" + keyword + "%");
        List<Event> results = query.getResultList();
        MessageBean.addInfo("resultsMessage", results.size() + " events founded. " + keyword + ".");
        return results;
    }

}