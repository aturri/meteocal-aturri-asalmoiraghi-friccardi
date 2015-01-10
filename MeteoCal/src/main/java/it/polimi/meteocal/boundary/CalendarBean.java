package it.polimi.meteocal.boundary;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
 
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
 
@Named
@ViewScoped
public class CalendarBean implements Serializable {
    
    @Inject
    private EventBean eb;
    @EJB
    private UserManager um;
    
    private ScheduleModel eventModel;
     
    private ScheduleEvent scheduleEvent;
    
    private Event event;
    
    private User user;
 
    @PostConstruct
    public void init() {        
        eventModel = new DefaultScheduleModel();
    
        if(this.user == null && existsParam("email")){
            setUserByParam();
        }
        else{
            user = um.getLoggedUser();
        }
        loadUserEvents();
    }
     
    public ScheduleModel getEventModel() {
        return eventModel;
    }
     
    public ScheduleEvent getScheduleEvent() {
        return scheduleEvent;
    }
 
    public void setScheduleEvent(ScheduleEvent event) {
        this.scheduleEvent = event;
    }
    
    public Event getEvent() {
        return event;
    }
 
    public void setEvent(Event event) {
        this.event = event;
    }
    
    public User getUser() {
        return user;
    }
    
    public boolean isPublicCalendar(){
        return !user.getPrivateCalendar();
    }
     
    public void addEvent(ActionEvent actionEvent) {
        if(scheduleEvent.getId() == null){
            saveEvent();
        }
        else{
            updateEvent();
        }
         
        scheduleEvent = new DefaultScheduleEvent();
        event = new Event();
    }
     
    public void onEventSelect(SelectEvent selectEvent) {
        scheduleEvent = (ScheduleEvent) selectEvent.getObject();
        event = (Event) scheduleEvent.getData();
    }
     
    public void onDateSelect(SelectEvent selectEvent) {
        event = new Event();
        event.setBeginDate((Date) selectEvent.getObject());
        event.setEndDate((Date) selectEvent.getObject());
        event.setIndoor(Boolean.TRUE);
        scheduleEvent = new DefaultScheduleEvent("", event.getBeginDate(), event.getEndDate(), event);
    }
     
    public void onEventMove(ScheduleEntryMoveEvent movedEvent) {
        /*FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event moved", "Day delta:" + movedEvent.getDayDelta() + ", Minute delta:" + movedEvent.getMinuteDelta());
        addMessage(message);
        
        scheduleEvent = movedEvent.getScheduleEvent();
        MessageBean.addWarning("Schedule data: "+scheduleEvent.getStartDate().toString());
        event = (Event) scheduleEvent.getData();
        MessageBean.addWarning("Event data: " + event.getBeginDate().toString());
        
        Date myDate = new Date(event.getBeginDate().getTime());
        MessageBean.addWarning("MyData: " + myDate.toString());
        event.setBeginDate(myDate);        
        event.setTitle(myDate.toString());
        em.delete(event);
        em.save(event);
        //add to creator's calendar
        User myUser = um.getLoggedUser();
        myUser.getEvents().add(event);
        um.update(myUser);
        
//        updateEvent();
        
//        myEvent.setTitle("Papeete2");
//        Date start = scheduleEvent.getStartDate();
//        MessageBean.addInfo(start.toString());
//        myEvent.setBeginDate(new Date(start.getTime()));
//        myEvent.setEndDate(scheduleEvent.getEndDate());
//        
//        MessageBean.addWarning(Integer.toString(myEvent.getId()));
//        
//        MessageBean.addWarning(myEvent.getBeginDate().toString());
//        em.update(myEvent);*/
    }
     
    public void onEventResize(ScheduleEntryResizeEvent resizedEvent) {
        /*FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event resized", "Day delta:" + resizedEvent.getDayDelta() + ", Minute delta:" + resizedEvent.getMinuteDelta());
         
        addMessage(message);
        
        scheduleEvent = resizedEvent.getScheduleEvent();
        MessageBean.addWarning("Schedule data: "+scheduleEvent.getEndDate().toString());
        event = (Event) scheduleEvent.getData();
        MessageBean.addWarning("Event data: " + event.getEndDate().toString());
        
        updateEvent();*/
    }
     
    /*private void addMessage(FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(null, message);
    }*/
    
    /**
     * Check if the parameter exists
     * @param param
     * @return 
     */
    public boolean existsParam(String param){
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().containsKey(param);
    }
    
    /**
     * Load the user from the email
     */
    public void setUserByParam() throws RuntimeException {
        if(this.existsParam("email")){
            String email = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("email");
            this.user = um.findByEmail(email);
        }else{
            throw new RuntimeException();
        }
    }

    private void loadUserEvents() {
        Set<Event> events = this.user.getEvents();
        
        for(Event e: events){
            String title;
            if(!e.getUsers().contains(um.getLoggedUser()) && !e.getPublicEvent())
                title = "Occupied";
            else
                title = e.getTitle();
            eventModel.addEvent(new DefaultScheduleEvent(title, e.getBeginDate(), e.getEndDate(), e));
        }
    }
    
    private void updateEvent(){
        eb.setEvent(event);
        eb.editEvent();
        eventModel.deleteEvent(scheduleEvent);
        eventModel.addEvent(new DefaultScheduleEvent(event.getTitle(), event.getBeginDate(), event.getEndDate(), event));
        MessageBean.addInfo("Event succesfully updated.");
    }
    
    private void saveEvent() {
        eb.setEvent(event);
        eb.createEvent();       
        eventModel.addEvent(new DefaultScheduleEvent(event.getTitle(), event.getBeginDate(), event.getEndDate(), event));
        MessageBean.addInfo("New event succesfully created.");
    }
    
    public Boolean showDetailsLink(){
        return this.scheduleEvent != null && !"".equals(this.scheduleEvent.getTitle());
    }

}