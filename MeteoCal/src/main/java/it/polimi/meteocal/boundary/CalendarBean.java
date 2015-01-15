package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.EventController;
import it.polimi.meteocal.control.NavigationBean;
import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.exception.EventOverlapException;
import it.polimi.meteocal.exception.IllegalEventDateException;
import it.polimi.meteocal.exception.IllegalInvitedUserException;
import it.polimi.meteocal.utils.DateUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
 
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
    private EventController ec;
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
     
    public String addEvent(ActionEvent actionEvent) {
        if(scheduleEvent.getId() == null){
            saveEvent();
        }
        else{
            updateEvent();
        }
        
        String id = Integer.toString(event.getId());

        scheduleEvent = new DefaultScheduleEvent();
        event = new Event();
        
        return NavigationBean.redirectToEventDetailsPage(id);
    }
    public String addEventAndGo() {
        return addEvent(null);
    }
     
    public void onEventSelect(SelectEvent selectEvent) {
        scheduleEvent = (ScheduleEvent) selectEvent.getObject();
        event = (Event) scheduleEvent.getData();
    }
     
    public void onDateSelect(SelectEvent selectEvent) {
        Date dateSelected = (Date) selectEvent.getObject();
        if(DateUtils.isToday(dateSelected))
            dateSelected = DateUtils.getToday();
        if(dateSelected.before(DateUtils.setTimeToMidnight(new Date())))
            RequestContext.getCurrentInstance().addCallbackParam("pastDate", true);
        else{
            event = new Event();
            event.setBeginDate(dateSelected);
            event.setIndoor(Boolean.TRUE);
            scheduleEvent = new DefaultScheduleEvent("", event.getBeginDate(), event.getEndDate(), event);
        }
    }
     
    public void onEventMove(ScheduleEntryMoveEvent movedEvent) {}
     
    public void onEventResize(ScheduleEntryResizeEvent resizedEvent) {}
    
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
            DefaultScheduleEvent loadedEvent = new DefaultScheduleEvent(getTitle(e), e.getBeginDate(), e.getEndDate(), e);
            loadedEvent.setStyleClass(getEventClassStyle(e));
           loadedEvent.setEditable(isEditable(e));
            eventModel.addEvent(loadedEvent);
            }
    }
    
    private String getTitle(Event event){
        if(!event.getUsers().contains(um.getLoggedUser()) && !event.getPublicEvent())
            return "Occupied";
        return event.getTitle();
    }
    
    private Boolean isEditable(Event event){
        return event.getCreator().equals(um.getLoggedUser()) && event.getBeginDate().after(DateUtils.getToday());
    }
    
    private String getEventClassStyle(Event event){
        User loggedUser = um.getLoggedUser();
        String style;
        if(event.getBeginDate().before(new Date())){
            if(event.getEndDate().before(new Date()))
                style = "pastEvent";
            else
                style = "currentEvent";
        }
        else
            style = "futureEvent";
        
        if(event.getCreator().equals(loggedUser))
            return style + " ownEvent";
        if(event.getUsers().contains(loggedUser))
            return style + " participatingEvent";
        if(event.getInvitedUsers().contains(loggedUser))
            return style + " invitedEvent";
        if(event.getPublicEvent())
            return style + " publicOtherEvent";
        return style + " privateOtherEvent";
        
    }
    
    private void updateEvent(){
        Boolean addSuccess = false;            
        String message;
        try {
            ec.editEvent(event, null);
            addSuccess = true;
            eventModel.deleteEvent(scheduleEvent);
            eventModel.addEvent(new DefaultScheduleEvent(event.getTitle(), event.getBeginDate(), event.getEndDate(), event));
            MessageBean.addInfo("calendarMessage","Event succesfully updated.");
        } catch (EventOverlapException ex) {
            message = "This event overlaps with an existing one!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("calendarMessage",message);
        } catch (IllegalInvitedUserException ex) {
            message = "Check the invitation list!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("calendarMessage",message);
        } catch (IllegalEventDateException ex) {
            message = "End date must be after begin date!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("calendarMessage",message);
        }
        RequestContext.getCurrentInstance().addCallbackParam("addSuccess", addSuccess);
    }
    
    private void saveEvent() {
        Boolean addSuccess = false;            
        String message;
        try {
            ec.createEvent(event, null);
            addSuccess = true;
            eventModel.addEvent(new DefaultScheduleEvent(event.getTitle(), event.getBeginDate(), event.getEndDate(), event));
            MessageBean.addInfo("calendarMessage", "New event succesfully created.");
        } catch (EventOverlapException ex) {
            message = "This event overlaps with an existing one!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("calendarMessage",message);
        } catch (IllegalInvitedUserException ex) {
            message = "Check the invitation list!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("calendarMessage",message);
        } catch (IllegalEventDateException ex) {
            message = "End date must be after begin date!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("calendarMessage",message);
        } catch (IllegalArgumentException ex) {
            message = "Unexpected error during creation! Operation cancelled!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("calendarMessage",message);
        }    
        RequestContext.getCurrentInstance().addCallbackParam("addSuccess", addSuccess);
    }
    
    public Boolean showDetailsLink(){
        return this.scheduleEvent != null && !"".equals(this.scheduleEvent.getTitle());
    }
    
    public Boolean canSeeEventDetails(){
        if(this.event != null)
            return this.event.getPublicEvent() || this.event.getUsers().contains(um.getLoggedUser());
        return false;
    }
}