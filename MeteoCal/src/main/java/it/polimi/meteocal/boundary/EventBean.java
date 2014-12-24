/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.NavigationBean;
import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.EventManager;
import it.polimi.meteocal.entityManager.UserManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;

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
    
    public String editEvent() {
        this.eventManager.update(event);
        return NavigationBean.redirectToHome();
    }
 
    public Date getToday() {
        return new Date();
    }
    
    public void setEventByParam() {
	String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        this.setEvent(eventManager.findById(Integer.parseInt(id)));
    }
    
    public String eventIndoor() {
        if(this.event.getIndoor()) {
            return "Indoor";
        }
        return "Outdoor";
    }
    
    public String eventVisibility() {
         if(this.event.getPublicEvent()) {
            return "Public";
        }
        return "Private";       
    }
        
    public String eventBegin() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm",Locale.ENGLISH);
        return sdf.format(event.getBeginDate());
    }
    
    public String eventEnd() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm",Locale.ENGLISH);
        return sdf.format(event.getEndDate());
    }
}
