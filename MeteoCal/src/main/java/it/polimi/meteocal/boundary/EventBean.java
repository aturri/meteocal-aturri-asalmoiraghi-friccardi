/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.EventManager;
import it.polimi.meteocal.entityManager.UserManager;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author Andrea
 */
@Named
@RequestScoped
public class EventBean {

    @EJB
    EventManager eventManager;
    
    @EJB
    UserManager userManager;
    
    private Event event = new Event();

    public EventBean() {
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
    
    public String createEvent(){
        User user=userManager.getLoggedUser(); 
        this.event.setCreator(user);
        this.event.getUsers().add(user);
        this.eventManager.save(event);
        user.getEvents().add(event);
        userManager.update(user);
        return NavigationBean.redirectToHome();
    }
    
    
    
    
    
    
    
    
    
}
