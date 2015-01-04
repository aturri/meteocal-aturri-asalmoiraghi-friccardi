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
import it.polimi.meteocal.entityManager.WeatherController;
import it.polimi.meteocal.exception.EventOverlapException;
import it.polimi.meteocal.exception.IllegalEventDateException;
import it.polimi.meteocal.exception.IllegalInvitedUserException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    //È necessario per il form dove si modifica l'evento, f:metadata>f:viewParam vogliono un setter su questo attributo
    private Event event;

    public EventBean() {
    }

    public Event getEvent() {
        if(this.event==null&&this.isExistsIdParam()){
            this.setEventByParam();
        }else if(this.event==null){
            this.event=new Event();
            this.event.setBeginDate(this.getToday());
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
    
    public String deleteEvent() {
        eventControl.deleteEvent(event);
        return NavigationBean.redirectToHome();
    }
    
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
    
    public String refuseInvitation() {
        eventControl.refuseInvitation(event);
        return NavigationBean.redirectToHome();
    }
    
    public String removeFromMyCalendar() {
        eventControl.removeFromCalendar(event);
        return NavigationBean.redirectToHome();
    }
    
    public String removeParticipant(String email) {
        eventControl.removeParticipant(event, email);
        return NavigationBean.redirectToEventDetailsPage(event.getId());
    }    
    
    public String removeInvitation(String email) {
        eventControl.removeInvitedUser(event, email);
        return NavigationBean.redirectToEventDetailsPage(event.getId());
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
        return EXT_DATE_FORMAT.format(event.getBeginDate());
    }
    
    public String eventEnd() {
        return EXT_DATE_FORMAT.format(event.getEndDate());
    }
    
    public String forecastDate() {
        return DATE_FORMAT.format(event.getWeather().getForecastDate());
    }
    
    public String forecastUpdatedAt() {
        return EXT_DATE_FORMAT.format(event.getWeather().getLastUpdate());
    }
    
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
    
    public Weather searchFirstSunnyDay() {
        if(this.event.getCity()!=null && this.event.getBeginDate()!=null) {
            Weather weather = weatherControl.searchFirstGoodDay(this.event.getCity(), this.event.getBeginDate());
            if(weather!=null) {
                return weather; 
            }
        }
        return null;
    }

    public String getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(String invitedUsers) {
        this.invitedUsers = invitedUsers;
    }

    public List<User> getListInvitedUsers() {
        return new ArrayList<>(event.getInvitedUsers());
    }
    
    public List<User> getParticipatingUsers() {
        return new ArrayList<>(event.getUsers());
    }
  
    public Boolean canUserBeRemovedFromParticipants(String email) {
        return !(!isCurrentUserCreator() || email.equals(userManager.getLoggedUser().getEmail()));
    }
    
    
    public Boolean isUserParticipant(User user) {
       List<User> participants = this.getParticipatingUsers();
        for(User u: participants) {
            if(u.getEmail().equals(user.getEmail())) {
                return true;
            }
        }
        return false;
    }
    
    public Boolean isCurrentUserCreator() {
        return event.getCreator().getEmail().equals(userManager.getLoggedUser().getEmail());
    }
    
    public Boolean isUserInvited(User user) {
        List<User> invited = this.getListInvitedUsers();
        for(User u: invited) {
            if(u.getEmail().equals(user.getEmail())) {
                return true;
            }
        }
        return false;      
    }
    
    public Boolean isCurrentUserInvitedTo(Integer id) {
        this.event = eventManager.findById(id);
        User user = userManager.getLoggedUser();
        return this.isUserInvited(user);
    }
    
    public Boolean isCurrentUserParticipatingTo(Integer id) {
        this.event = eventManager.findById(id);
        User user = userManager.getLoggedUser();
        return this.isUserParticipant(user);
    }
    
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
    
    public Boolean hasWeather(Integer id) {
        if(this.event == null) {
            this.event = eventManager.findById(id);
        }
        return event.getWeather()!=null;
    }
    
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
            } else {
                return "na";
            }
        }
        return "na";
    }
}
