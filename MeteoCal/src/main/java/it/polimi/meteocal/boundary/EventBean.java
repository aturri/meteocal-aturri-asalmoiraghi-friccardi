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
    
    //È necessario per il form dove si modifica l'evento, f:metadata>f:viewParam vogliono un setter su questo attributo
    private Event event;

    public EventBean() {
    }

    public Event getEvent() {
        if(this.event==null&&this.isExistsIdParam()){
            this.setEventByParam();
        }else if(this.event==null){
            this.event=new Event();
        }
        return this.event;
    }

    public void setEvent(Event event) {
        //guardiamo se this.id oppure il paramentro id è impostato, se si facciamo la ricerca nel DB
        /*if(this.event==null&&this.isExistsIdParam()){ 
           this.setEventByParam();
        }*/
        this.event = event;
    }
    
    public String createEvent(){
        User user = userManager.getLoggedUser(); 
        this.event.setCreator(user);
        this.event.getUsers().add(user);
        this.eventManager.save(event);
        
        //devo aggiornare la lista di eventi nell'utente
        user.getEvents().add(event);
        userManager.update(user);
        return NavigationBean.redirectToHome();
    }
    
    public String editEvent() {
        System.out.println(eventManager.toString());
        System.out.println("Sto facendo l'update");
        this.eventManager.update(event);
        return NavigationBean.redirectToHome();
    }
 
    /**
     * Dice se esiste un paramentro "id"
     * @return 
     */
    public boolean isExistsIdParam(){
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().containsKey("id");
    }
    
    /**
     * Prende dal GET il parametro id e trova l'evento corrispondente
     */
    public void setEventByParam() throws RuntimeException {
        if(this.isExistsIdParam()){
            String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
            this.event=eventManager.findById(Integer.parseInt(idParam));
        }else{
            throw new RuntimeException();
        }
    }
   
    
    
    
    
   
    
    
    
    public Date getToday() {
        return new Date();
    }
   
    //METEDI UTILI PER LA VISUALIZZAZIONE TESTUALE
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
