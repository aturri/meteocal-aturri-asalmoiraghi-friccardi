package it.polimi.meteocal.boundary;

import it.polimi.meteocal.utils.MessageUtility;
import it.polimi.meteocal.control.EventController;
import it.polimi.meteocal.boundary.service.NavigationService;
import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.Weather;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.EventManager;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.control.WeatherController;
import it.polimi.meteocal.exception.EventOverlapException;
import it.polimi.meteocal.exception.IllegalEventDateException;
import it.polimi.meteocal.exception.IllegalInvitedUserException;
import it.polimi.meteocal.utils.DateUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author Andrea
 */
@Named
@ViewScoped
public class EventBean implements Serializable{
    
    @Inject
    private WeatherController weatherControl;
  
    @Inject
    private EventController eventControl;
        
    @EJB
    EventManager eventManager;
    
    @EJB
    UserManager userManager;
    
    private List<String> invitedUsersList;
    
    private Event event;
    
    @PostConstruct
    public void init(){
        if(this.event==null&&this.isExistsIdParam()){
            this.setEventByParam();
        }else if(this.event==null){
            this.event=new Event();
            this.event.setBeginDate(DateUtils.getToday());
        }
        
        invitedUsersList = new ArrayList<>();
    }
    
    /**
     * This method return the current event
     * @return current event
     */
    public Event getEvent() {
        return this.event;
    }
    
    /**
     * This method sets the current event
     * @param event 
     */
    public void setEvent(Event event) {
        this.event = event;
    }
    
    /**
     * This method forwards to the controller the request to create the event
     * @return redirect
     */
    public String createEvent(){  
        String message;
        try {
            eventControl.createEvent(event, invitedUsersList);
        } catch (EventOverlapException ex) {
            message = "This event overlaps with an existing one!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("errorMsg",message);
            return "";
        } catch (IllegalInvitedUserException ex) {
            message = "Check the invitation list!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("errorMsg",message);
            return "";
        } catch (IllegalEventDateException ex) {
            message = "End date must be after begin date!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("errorMsg",message);
            return "";
        } catch (IllegalArgumentException ex) {
            message = "Unexpected error during creation! Operation cancelled!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("calendarMessage",message);
            return "";
        }
        return NavigationService.redirectToEventDetailsPage(event.getId().toString());
    }
    
    /**
     * This method forwards to the controller the request to edit the event
     * @return redirect
     */
    public String editEvent() {
        String message;
        try {
            eventControl.editEvent(event, invitedUsersList);
        } catch (EventOverlapException ex) {
            message = "This event overlaps with an existing one!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("errorMsg",message);
            return "";
        } catch (IllegalInvitedUserException ex) {
            message = "Check the invitation list!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("errorMsg",message);
            return "";
        } catch (IllegalEventDateException ex) {
            message = "End date must be after begin date!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("errorMsg",message);
            return "";
        }
        return NavigationService.redirectToEventDetailsPage(event.getId().toString());
    }
    
    /**
     * This method forwards to the controller the request to delete the event
     * @return redirect
     */
    public String deleteEvent() {
        eventControl.deleteEvent(event);
        return NavigationService.redirectToHome();
    }
    
    /**
     * This method forwards to the controller the request to accept invitation
     * @return redirect
     */
    public String acceptInvitation() {
        String message;
        try {
            eventControl.acceptInvitation(event);
        } catch (EventOverlapException ex) {
            message = "This event overlaps with an existing one!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageUtility.addError("errorMsg",message);
            return "";
        }
        return NavigationService.redirectToEventDetailsPage(Integer.toString(event.getId()));
    }
    
    /**
     * This method forwards to the controller the request to refuse invitation
     * @return redirect
     */
    public String refuseInvitation() {
        eventControl.refuseInvitation(event);
        return NavigationService.redirectToHome();
    }
 
    /**
     * This method forwards to controller the request to remove the event from current user's cal
     * @return redirect
     */
    public String removeFromMyCalendar() {
        eventControl.removeFromCalendar(event);
        return NavigationService.redirectToHome();
    }
    
    /**
     * This method forwards to controller the request to remove participant
     * @param email of the user to be removed
     * @return redirect
     */
    public String removeParticipant(String email) {
        eventControl.removeParticipant(event, email);
        return NavigationService.redirectToEventDetailsPage(event.getId().toString());
    }    
    
    /**
     * This method forwards to controller the request to remove invitation
     * @param email of the user to be removed
     * @return redirect
     */
    public String removeInvitation(String email) {
        eventControl.removeInvitedUser(event, email);
        return NavigationService.redirectToEventDetailsPage(event.getId().toString());
    }
 
    /**
     * This method says if there exists id param in GET
     * @return true if GET[id] exists
     */
    public boolean isExistsIdParam(){
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().containsKey("id");
    }
    
    /**
     * This method takes the GET id parameter and sets the current event
     * @throws RuntimeException 
     */
    public void setEventByParam() throws RuntimeException {
        if(this.isExistsIdParam()){
            String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
            this.event=eventManager.findById(Integer.parseInt(idParam));
        }else{
            throw new RuntimeException();
        }
    }
   
    /**
     * This method formats the string for event type (indoor/outdoor)
     * @return event type formatted
     */  
    public String eventIndoor() {
        if(this.event.getIndoor()) {
            return "Indoor";
        }
        return "Outdoor";
    }

    /**
     * This method formats the string for event visibility
     * @return event visibility formatted
     */      
    public String eventVisibility() {
         if(this.event.getPublicEvent()) {
            return "Public";
        }
        return "Private";       
    }

    /**
     * This method formats the date for the last update date of the event begin
     * @return event begin date formatted
     */      
    public String eventBegin() {
        return event.eventBeginString();
    }
 
    /**
     * This method formats the date for the last update date of the event end
     * @return event end date formatted
     */    
    public String eventEnd() {
        return event.eventEndString();
    }
    
    /**
     * This method formats the date for the weather date
     * @return weather date formatted
     */
    public String forecastDate() {
        return DateUtils.formatDate(event.getWeather().getForecastDate());
    }
    
    /**
     * This method formats the date for the last update date of the weather
     * @return last update weather date formatted
     */
    public String forecastUpdatedAt() {
        return DateUtils.formatExtDate(event.getWeather().getLastUpdate());
    }
    
    /**
     * This method returns the string representing the duration of the event
     * @return duration of the event, es 1.5 h
     */
    public String eventDuration() {
        Float hours = (float) (this.event.getEndDate().getTime() - this.event.getBeginDate().getTime()) / (60*60*1000);
        return "~ "+String.format("%.01f", hours)+" h";
    }
    
    /**
     * This method returns the event address composed
     * @return string representing location
     */
    public String eventLocation() {
        if(event.getCity()!=null && event.getAddress()!=null) {
            return event.getAddress()+", "+event.getCity();
        } else if(event.getCity()!=null) {
            return event.getCity();
        } else if(event.getAddress()!=null) {
            return event.getAddress()+", not specified city";
        }
        return "";
    }
    
    /**
     * This method returns the event address for map
     * @return string representing location for the map
     */
    public String eventAddressForMap() {
        return event.getAddress().replaceAll(" ","+").replaceAll(",","")
                +",+"+
                event.getCity().replaceAll(" ","+").replaceAll(" ","");
    }
    

    /**
     * This method handles requests to search actual weather forecasts for the event's begin date/time and city
     * @return the string containing the weather
     */
    public String handleWeather() {
        if(this.event.getCity()!=null && this.event.getBeginDate()!=null) {
            Weather weather = weatherControl.createWeather(this.event.getCity(), this.event.getBeginDate(), false);
            if(weather!=null) {
                //this.event.setWeather(weather);
                String formattedForecast = "Forecast for "+weather.getCity()+
                        " on "+DateUtils.formatDate(weather.getForecastDate())+
                        ": "+weather.getWeather()+
                        ", with high of "+Float.toString(weather.getMaxTemp())+
                        "°C and low of "+Float.toString(weather.getMinTemp())+"°C";
                return formattedForecast;
            } else {
                return "Not available";
            }
        }
        return "Please insert start date/time and city.";
    }
    
    /**
     * This method handles requests to search the closest good weather forecasts for the event's begin date/time and city
     */
    public void handleBadWeather() {
        if(this.event.getCity()!=null && this.event.getBeginDate()!=null) {
            Weather weather = weatherControl.createWeather(this.event.getCity(), this.event.getBeginDate(), false);
            if(weather!=null && weather.getBad()) {
                    Weather firstGood = this.searchFirstSunnyDay();
                    if(firstGood!=null) {
                        MessageUtility.addWarning("growlMsg","There is bad weather for the time and location you chose!\n"
                                + "But MeteoCal found good weather on "+
                                DateUtils.formatDate(firstGood.getForecastDate())+": "+
                                firstGood.getWeather()+
                                ", with high of "+Float.toString(firstGood.getMaxTemp())+
                                "°C and low of "+Float.toString(firstGood.getMinTemp())+"°C");
                    } else {
                        MessageUtility.addError("growlMsg","There is bad weather for the time and location you chose!\n"
                                + "Unfortunately MeteoCal did't find a close good day :(");
                    }
            }
        }
    }
    
    /**
     * This method asks the controller to search for the closest good weather day in the specified begin time and location
     * @return the weather if available, null if not
     */
    public Weather searchFirstSunnyDay() {
        if(this.event.getCity()!=null && this.event.getBeginDate()!=null) {
            Weather weather = weatherControl.searchFirstGoodDay(this.event.getCity(), this.event.getBeginDate());
            if(weather!=null) {
                return weather; 
            }
        }
        return null;
    }
    
    /**
     * This method returns the invitedUsers list
     * @return invitedUsers
     */
    public List<String> getInvitedUsersList() {
        return invitedUsersList;
    }

    /**
     * This method sets the invitedUsers list
     * @param invitedUsers 
     */
    public void setInvitedUsersList(List<String> invitedUsers) {
        this.invitedUsersList = invitedUsers;
    }
    /**
     * This method returns suggestions list for user invitation list
     * @param query for filtering
     * @return users can invite
     */
    public List<String> completeUsersCanInvite(String query) {
        List<User> allUser = userManager.getOtherUsers();
        List<String> filteredUser = new ArrayList<>();
        
        for(User u: allUser){
            if(u.getEmail().toLowerCase().contains(query.toLowerCase()))
                filteredUser.add(u.getEmail());
        }
        
        //From the filtered users i remove also:
        //partecipating users
        for(User u: event.getUsers())
            filteredUser.remove(u.getEmail());
        //already invited users
        for(User u: event.getInvitedUsers())
            filteredUser.remove(u.getEmail());
        //already selected users
        if(this.invitedUsersList != null)
            filteredUser.removeAll(this.invitedUsersList);

        return filteredUser;
    }
    
    public void handleUserSelected(SelectEvent event) {
        this.invitedUsersList.add(event.getObject().toString());
    }
    public void handleUserUnselected(SelectEvent event) {
        this.invitedUsersList.remove(event.getObject().toString());
    }

    /**
     * This method returns the list of invited users to current event
     * @return list of users invited to current event
     */
    public List<User> getListInvitedUsers() {
        return new ArrayList<>(event.getInvitedUsers());
    }
    
    /**
     * This method returns the list of participants to current event
     * @return list of users participating to current event
     */
    public List<User> getParticipatingUsers() {
        return new ArrayList<>(event.getUsers());
    }
  
    /**
     * This method says if the user whose email is specified can be removed from participants
     * @param email of the user to be checked as removable
     * @return true if the user is removable from participants
     */
    public Boolean canUserBeRemovedFromParticipants(String email) {
        return !(!isCurrentUserCreator() || email.equals(userManager.getLoggedUser().getEmail()));
    }
    
    /**
     * This method says if the specified user is participating to the currnet event
     * @param user to be checked as participant to current event
     * @return true if the user is participant
     */
    public Boolean isUserParticipant(User user) {
       List<User> participants = this.getParticipatingUsers();
        for(User u: participants) {
            if(u.getEmail().equals(user.getEmail())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * This method says if the current user is the creator of the event
     * @return true if the current user is creator
     */
    public Boolean isCurrentUserCreator() {
        return event.getCreator().getEmail().equals(userManager.getLoggedUser().getEmail());
    }
    
    /**
     * This method says if the current user is the creator of the specified event
     * @return true if the current user is creator
     */
    public Boolean isCurrentUserCreatorOf(Integer id) {
        return eventManager.findById(id).getCreator().getEmail().equals(userManager.getLoggedUser().getEmail());
    }
    
    /**
     * This method says if the specified user is invited to the current event
     * @param user to be checked as invited to current event
     * @return true if the user is invited
     */
    public Boolean isUserInvited(User user) {
        List<User> invited = this.getListInvitedUsers();
        for(User u: invited) {
            if(u.getEmail().equals(user.getEmail())) {
                return true;
            }
        }
        return false;      
    }
    
    /**
     * This method says if current user is invited to the event
     * @param id of the event
     * @return true if the user is invited
     */
    public Boolean isCurrentUserInvitedTo(Integer id) {
        this.event = eventManager.findById(id);
        User user = userManager.getLoggedUser();
        return this.isUserInvited(user);
    }
    
    /**
     * This method says if the event is in the future
     * @param id of the event
     * @return true if the event is in the future
     */
    public Boolean isEventAfterNow(Integer id) {
        return eventManager.findById(id).getBeginDate().after(new Date());
    }
    
    /**
     * This method says if the current user is participating to the event
     * @param id of the event
     * @return true if the user is participant
     */
    public Boolean isCurrentUserParticipatingTo(Integer id) {
        this.event = eventManager.findById(id);
        User user = userManager.getLoggedUser();
        return this.isUserParticipant(user);
    }
    
    /**
     * This method says if the event is visible for current user
     * @param id of the event
     * @return true if the current user can see the event
     */
    public Boolean isVisibleForCurrentUser(Integer id) {
        if(this.event == null) {
            this.event = eventManager.findById(id);
        }
        User currentUser = userManager.getLoggedUser();
        return this.isCurrentUserCreator() || 
                this.event.getPublicEvent() ||
                this.isUserInvited(currentUser) ||
                this.isUserParticipant(currentUser);
    }
    
    /**
     * This method says if the event has associated weather
     * @param id of the event
     * @return true if there is weather
     */
    public Boolean hasWeather(Integer id) {
        if(this.event == null) {
            this.event = eventManager.findById(id);
        }
        return event.getWeather()!=null;
    }
    
    /**
     * This method says if the event is eligible for having a map
     * @param id of the event
     * @return true if address and city are set
     */
    public Boolean hasMap(Integer id) {
        if(this.event == null) {
            this.event = eventManager.findById(id);
        }
        return event.getCity()!=null && event.getAddress()!=null;
    }
    
    /**
     * This method returns the image name for the weather
     * @return img name
     */
    public String getWeatherImg() {
        if(hasWeather(event.getId())) {
            String weatherTxt = event.getWeather().getWeather();
            if(weatherTxt.toLowerCase().contains("mostly") && weatherTxt.toLowerCase().contains("cloudy")) {
                return "mostly_cloudy";
            } else if(weatherTxt.toLowerCase().contains("mostly") && weatherTxt.toLowerCase().contains("sunny")) {
                return "mostly_sunny";
            } else if(weatherTxt.toLowerCase().contains("sun") && weatherTxt.toLowerCase().contains("rain")) {
                return "sun_rain";
            } else if(weatherTxt.toLowerCase().contains("sun") && weatherTxt.toLowerCase().contains("snow")) {
                return "sun_snow";
            } else if(weatherTxt.toLowerCase().contains("sun") && weatherTxt.toLowerCase().contains("thunder")) {
                return "sun_thunder";
            } else if(weatherTxt.toLowerCase().contains("sunny") || weatherTxt.toLowerCase().contains("clear")) {
                return "sunny";
            } else if(weatherTxt.toLowerCase().contains("hot")) {
                return "hot";
            } else if(weatherTxt.toLowerCase().contains("heavy") && weatherTxt.toLowerCase().contains("rain")) {
                return "rain_hard";
            } else if(weatherTxt.toLowerCase().contains("rain") || weatherTxt.toLowerCase().contains("shower")) {
                return "rain";
            } else if(weatherTxt.toLowerCase().contains("wind") && weatherTxt.toLowerCase().contains("snow")) {
                return "wind_snow";
            } else if(weatherTxt.toLowerCase().contains("wind")) {
                return "windy";
            } else if(weatherTxt.toLowerCase().contains("thunder") || weatherTxt.toLowerCase().contains("hurricane") || weatherTxt.toLowerCase().contains("storm")) {
                return "thunder";
            } else if(weatherTxt.toLowerCase().contains("snow")) {
                return "snow";
            } else if(weatherTxt.toLowerCase().contains("hail")) {
                return "hail";
            } else if(weatherTxt.toLowerCase().contains("low") && weatherTxt.toLowerCase().contains("clouds")) {
                return "low_clouds";
            } else if(weatherTxt.toLowerCase().contains("fog")) {
                return "fog";
            } else if(weatherTxt.toLowerCase().contains("cloud")) {
                return "cloud";
            }
        }
        return "na";
    }
    
    /**
     * Get the current date
     * @return the current date
     */
    public Date getToday(){
        return DateUtils.getToday();
    }
}
