package it.polimi.meteocal.boundary;

import it.polimi.meteocal.utils.MessageUtility;
import it.polimi.meteocal.control.EventController;
import it.polimi.meteocal.boundary.service.NavigationService;
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
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

/**
 * This bean is used to show the calendar of an user.
 * If the user is the logged user, he can fast create/edit events also.
 * @author andrea
 */
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
    
    /**
     * Get the eventModel to load the calendar
     * @return the eventModel
     */
    public ScheduleModel getEventModel() {
        return eventModel;
    }
    
    /**
     * Get the current scheduled event
     * @return the current scheduled event
     */
    public ScheduleEvent getScheduleEvent() {
        return scheduleEvent;
    }
 
    /**
     * Set the current scheduled event
     * @param event 
     */
    public void setScheduleEvent(ScheduleEvent event) {
        this.scheduleEvent = event;
    }
    
    /**
     * Get the current event
     * @return the current event
     */
    public Event getEvent() {
        return event;
    }
 
    /**
     * Set the current event
     * @param event 
     */
    public void setEvent(Event event) {
        this.event = event;
    }
    
    /**
     * Check if the calendar is public
     * @return true if the calendar is public
     */
    public boolean isPublicCalendar(){
        return !user.getPrivateCalendar();
    }
    
    /**
     * Check if the calendar shown is the loggedUser's calendar
     * @return true if is the loggedUser's calendar
     */
    public boolean isLoggedUserCalendar(){
        return user.equals(um.getLoggedUser());
    }
    
    /**
     * Check if the calendar can be shown.
     * @return true if is the loggedUser's calendar or the calendar is public
     */
    public boolean canShowCalendar(){
        return isPublicCalendar() || isLoggedUserCalendar();
    }
    
    /**
     * This method save (if the current scheduleEvent is null) or update an event.
     * @param actionEvent
     * @return the link to event details
     */
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
        
        return NavigationService.redirectToEventDetailsPage(id);
    }
     
    /**
     * Set the current scheduled event and the current event to the selected event from the calendar
     * @param selectEvent 
     */
    public void onEventSelect(SelectEvent selectEvent) {
        scheduleEvent = (ScheduleEvent) selectEvent.getObject();
        event = (Event) scheduleEvent.getData();
    }
     
    /**
     * Create a new event and a scheduled event on the date selected from the calendar.
     * Check if the date selected from the calendar is allowed (it's a future date).
     * If ok, create a new event on that date.
     * If not ok, set a callbackParam "pastDate" to true.
     * @param selectEvent 
     */
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
    
    /**
     * Check if the parameter exists
     * @param param
     * @return true if the parameter exist
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

    /**
     * Load the events of the user.
     * Create a scheduled event for each event loaded.
     * Set the colors of the scheduled events.
     * Set if a scheduled event is editable by the user or not.
     * Add the scheduled event to the event model 
     */
    private void loadUserEvents() {
        Set<Event> events = this.user.getEvents();
        
        for(Event e: events){
            DefaultScheduleEvent loadedEvent = new DefaultScheduleEvent(getTitle(e), e.getBeginDate(), e.getEndDate(), e);
            loadedEvent.setStyleClass(getEventClassStyle(e));
           loadedEvent.setEditable(isEditable(e));
            eventModel.addEvent(loadedEvent);
            }
    }
    
    /**
     * Get the title of the event.
     * Check if the user is participating at the event or the event is public.
     * If is true, return the original title.
     * If not, return the string "Occupied"
     * @param event
     * @return the title of the event
     */
    private String getTitle(Event event){
        if(!event.getUsers().contains(um.getLoggedUser()) && !event.getPublicEvent())
            return "Occupied";
        return event.getTitle();
    }
    
    /**
     * Check if the event is editable.
     * Check if the user is the owner of the event and the beginDate of the event is not passed.
     * @param event
     * @return true if editable
     */
    private Boolean isEditable(Event event){
        return event.getCreator().equals(um.getLoggedUser()) && event.getBeginDate().after(DateUtils.getToday());
    }
    
    /**
     * Return the styleClass for the event.
     * Check if is a own event, a participating event, a invited event, a public or private event.
     * Check also if is a past event, current event or future event.
     * @param event
     * @return 
     */
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
    
    /**
     * Update the current event and notify the result to user.
     */
    private void updateEvent(){
        Boolean addSuccess = false;            
        String message;
        try {
            ec.editEvent(event, null);
            addSuccess = true;
            eventModel.deleteEvent(scheduleEvent);
            eventModel.addEvent(new DefaultScheduleEvent(event.getTitle(), event.getBeginDate(), event.getEndDate(), event));
            MessageUtility.addInfo("calendarMessage","Event succesfully updated.");
        } catch (EventOverlapException ex) {
            message = "This event overlaps with an existing one!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("calendarMessage",message);
        } catch (IllegalInvitedUserException ex) {
            message = "Check the invitation list!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("calendarMessage",message);
        } catch (IllegalEventDateException ex) {
            message = "End date must be after begin date!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("calendarMessage",message);
        }
        RequestContext.getCurrentInstance().addCallbackParam("addSuccess", addSuccess);
    }
    
    /**
     * Save the new current event and notify the result to user.
     */
    private void saveEvent() {
        Boolean addSuccess = false;            
        String message;
        try {
            ec.createEvent(event, null);
            addSuccess = true;
            eventModel.addEvent(new DefaultScheduleEvent(event.getTitle(), event.getBeginDate(), event.getEndDate(), event));
            MessageUtility.addInfo("calendarMessage", "New event succesfully created.");
        } catch (EventOverlapException ex) {
            message = "This event overlaps with an existing one!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("calendarMessage",message);
        } catch (IllegalInvitedUserException ex) {
            message = "Check the invitation list!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("calendarMessage",message);
        } catch (IllegalEventDateException ex) {
            message = "End date must be after begin date!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("calendarMessage",message);
        } catch (IllegalArgumentException ex) {
            message = "Unexpected error during creation! Operation cancelled!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("calendarMessage",message);
        }    
        RequestContext.getCurrentInstance().addCallbackParam("addSuccess", addSuccess);
    }
    
    /**
     * Check if it is a new event or an existing event.
     * If is an existing event return true to show the link to its details page.
     * @return true if can show the link to event details page
     */
    public Boolean showDetailsLink(){
        return this.scheduleEvent != null && !"".equals(this.scheduleEvent.getTitle());
    }
    
    /**
     * Check if the current user can see the event details of the current event.
     * Return true if is a public event or the user is participating at the event
     * @return true if can see the details of the event
     */
    public Boolean canSeeEventDetails(){
        if(this.event != null)
            return this.event.getPublicEvent() || this.event.getUsers().contains(um.getLoggedUser());
        return false;
    }
}