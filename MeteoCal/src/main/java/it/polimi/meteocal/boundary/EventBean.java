/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.EventController;
import it.polimi.meteocal.control.NavigationBean;
import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.Weather;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.EventManager;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.control.WeatherController;
import it.polimi.meteocal.exception.EventOverlapException;
import it.polimi.meteocal.exception.IllegalEventDateException;
import it.polimi.meteocal.exception.IllegalInvitedUserException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author Andrea
 */
@Named
@RequestScoped
public class EventBean {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
    private static final SimpleDateFormat EXT_DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.ENGLISH);
    
    @Inject
    private WeatherController weatherControl;
  
    @Inject
    private EventController eventControl;
        
    @EJB
    EventManager eventManager;
    
    @EJB
    UserManager userManager;
    
    private String invitedUsers;

    private Event event;

    /**
     * This method return the current event
     * @return current event
     */
    public Event getEvent() {
        if(this.event==null&&this.isExistsIdParam()){
            this.setEventByParam();
        }else if(this.event==null){
            this.event=new Event();
            this.event.setBeginDate(this.getToday());
        }
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
            eventControl.createEvent(event, invitedUsers);
        } catch (EventOverlapException ex) {
            message = "This event overlaps with an existing one!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("errorMsg",message);
            return "";
        } catch (IllegalInvitedUserException ex) {
            message = "Check the invitation list!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("errorMsg",message);
            return "";
        } catch (IllegalEventDateException ex) {
            message = "End date must be after begin date!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("errorMsg",message);
            return "";
        }
        return NavigationBean.redirectToHome();
    }
    
    /**
     * This method forwards to the controller the request to edit the event
     * @return redirect
     */
    public String editEvent() {
        String message;
        try {
            eventControl.editEvent(event, invitedUsers);
        } catch (EventOverlapException ex) {
            message = "This event overlaps with an existing one!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("errorMsg",message);
            return "";
        } catch (IllegalInvitedUserException ex) {
            message = "Check the invitation list!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("errorMsg",message);
            return "";
        } catch (IllegalEventDateException ex) {
            message = "End date must be after begin date!";
            Logger.getLogger(EventBean.class.getName()).log(Level.FINE, message);
            MessageBean.addError("errorMsg",message);
            return "";
        }
        return NavigationBean.redirectToEventDetailsPage(event.getId());
    }
    
    /**
     * This method forwards to the controller the request to delete the event
     * @return redirect
     */
    public String deleteEvent() {
        eventControl.deleteEvent(event);
        return NavigationBean.redirectToHome();
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
            MessageBean.addError("errorMsg",message);
            return "";
        }
        return NavigationBean.redirectToHome();
    }
    
    /**
     * This method forwards to the controller the request to refuse invitation
     * @return redirect
     */
    public String refuseInvitation() {
        eventControl.refuseInvitation(event);
        return NavigationBean.redirectToHome();
    }
 
    /**
     * This method forwards to controller the request to remove the event from current user's cal
     * @return redirect
     */
    public String removeFromMyCalendar() {
        eventControl.removeFromCalendar(event);
        return NavigationBean.redirectToHome();
    }
    
    /**
     * This method forwards to controller the request to remove participant
     * @param email of the user to be removed
     * @return redirect
     */
    public String removeParticipant(String email) {
        eventControl.removeParticipant(event, email);
        return NavigationBean.redirectToEventDetailsPage(event.getId());
    }    
    
    /**
     * This method forwards to controller the request to remove invitation
     * @param email of the user to be removed
     * @return redirect
     */
    public String removeInvitation(String email) {
        eventControl.removeInvitedUser(event, email);
        return NavigationBean.redirectToEventDetailsPage(event.getId());
    }
 
    /**
     * This method says if there exists id param in GET
     * @return true if GET[id] exists
     */
    public boolean isExistsIdParam(){
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().containsKey("id");
    }
    
    /**
     * This method takes the GET ip parameter and sets the current event
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
     * This method returns the current date, rounded to the next :00/:15/:30/:45 minute
     * @return current date rounded
     */
    public Date getToday() {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 15;
        calendar.add(Calendar.MINUTE, 15-mod);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
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
        return EXT_DATE_FORMAT.format(event.getBeginDate());
    }
 
    /**
     * This method formats the date for the last update date of the event end
     * @return event end date formatted
     */    
    public String eventEnd() {
        return EXT_DATE_FORMAT.format(event.getEndDate());
    }
    
    /**
     * This method formats the date for the weather date
     * @return weather date formatted
     */
    public String forecastDate() {
        return DATE_FORMAT.format(event.getWeather().getForecastDate());
    }
    
    /**
     * This method formats the date for the last update date of the weather
     * @return last update weather date formatted
     */
    public String forecastUpdatedAt() {
        return EXT_DATE_FORMAT.format(event.getWeather().getLastUpdate());
    }
    
    /**
     * This method returns the string representing the duration of the event
     * @return duratin of the event, es 1.5 h
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
     * This method handles requests to check list of invited users for create page
     */ 
    public void handleInvitedUsersForCreate() {
        if(this.invitedUsers!=null && !"".equals(this.invitedUsers)) {
            String foo = this.invitedUsers;
            String[] split = foo.split(",");
            List<String> tmpUsersList = new ArrayList<>();
            for (String splitted : split) {
                if(splitted.equals(userManager.getLoggedUser().getEmail())) {
                    MessageBean.addWarning("errorMsg","You can't invite yourself");
                } else if(!userManager.existsUser(splitted)) {
                    MessageBean.addWarning("errorMsg",splitted+" is not registered to MeteoCal");
                } else if(tmpUsersList.contains(splitted)) {
                    MessageBean.addWarning("errorMsg",splitted+" has been invited more than once");
                } else {
                    tmpUsersList.add(splitted);
                }
            }
        }
    }
    
    /**
     * This method handles requests to check list of invited users for edit page
     */
    public void handleInvitedUsersForEdit() {
        if(this.invitedUsers!=null && !"".equals(this.invitedUsers)) {
            this.handleInvitedUsersForCreate();
            String foo = this.invitedUsers;
            String[] split = foo.split(",");
            for (String splitted : split) {
                if(event.getInvitedUsers().contains(userManager.findByEmail(splitted)) || 
                            event.getUsers().contains(userManager.findByEmail(splitted))) {
                    MessageBean.addWarning("errorMsg",splitted+" has already been invited");
                }
            }
        }
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
                        " on "+DATE_FORMAT.format(weather.getForecastDate())+
                        ": "+weather.getWeather()+
                        ", with high of "+Float.toString(weather.getMaxTemp())+
                        "°C and low of "+Float.toString(weather.getMinTemp())+"°C";
                if(weather.getBad()) {
                    Weather firstGood = this.searchFirstSunnyDay();
                    if(firstGood!=null) {
                        MessageBean.addInfo("growlMsg","There is bad weather for the time and location you chose!\n"
                                + "But MeteoCal found good weather on "+
                                DATE_FORMAT.format(firstGood.getForecastDate())+": "+
                                firstGood.getWeather()+
                                ", with high of "+Float.toString(firstGood.getMaxTemp())+
                                "°C and low of "+Float.toString(firstGood.getMinTemp())+"°C");
                    } else {
                        MessageBean.addInfo("growlMsg","There is bad weather for the time and location you chose!\n"
                                + "Unfortunately MeteoCal did't find a close good day :(");
                    }
                }
                return formattedForecast;
            } else {
                return "Not available";
            }
        }
        return "Please insert start date/time and city.";
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
     * This method returns the invitedUsers string
     * @return invitedUsers
     */
    public String getInvitedUsers() {
        return invitedUsers;
    }

    /**
     * This method sets the invitedUsers string
     * @param invitedUsers 
     */
    public void setInvitedUsers(String invitedUsers) {
        this.invitedUsers = invitedUsers;
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
     * This method says if the specified user is invited to the currnet event
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
            } else if(weatherTxt.toLowerCase().contains("sunny")) {
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
}
