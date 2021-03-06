/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entity;

import it.polimi.meteocal.utils.DateUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Andrea
 */
@Entity
public class Event implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @NotNull
    @ManyToOne
    //Unidirectional relationship, many events have one creator
    private User creator;
    
    @ManyToOne
    private Weather weather;
    
    @NotNull(message = "Please insert a title")
    private String title;
    
    private String description;
    
    private String city;
    
    private String address;
    
    private String locationInfo;
    
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "Please choose a date/time")
    private Date beginDate;
    
    @NotNull(message = "Please choose a date/time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdEvent = new Date();

    @NotNull(message = "Please specify event's visibility")
    private Boolean publicEvent;
    
    @NotNull(message = "Please choose an option")
    private Boolean indoor;
    
    @ManyToMany
    @JoinTable(name="EVENT_IN_CALENDAR")
    private Set<User> users = new HashSet<>();
  
    @ManyToMany
    @JoinTable(name="INVITATION")
    private Set<User> invitedUsers = new HashSet<>();
      
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(String locationInfo) {
        this.locationInfo = locationInfo;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    /**
     * Check if the beginDate is before today.
     * if true, set the beginDate to today.
     * Check also if the endDate is after the beginDate.
     * If not, set the endDate, 30minutes after the beginDate.
     * @param beginDate 
     */
    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
        if(this.beginDate.before(DateUtils.getToday())){
            this.beginDate = DateUtils.getToday();
        }
        //For new event or event with beginDate after endDate, set endDate after 30 minutes when beginDate change
        if(this.endDate == null || (this.endDate != null && !this.endDate.after(beginDate)))
            this.endDate = new Date(beginDate.getTime() + TimeUnit.MINUTES.toMillis(30));
    }
    
    public void simpleSetBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    
    public Date getMinEndDate(){
        return new Date(this.beginDate.getTime() + TimeUnit.MINUTES.toMillis(15));
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    /**
     * This method formats the date for the last update date of the event begin
     * @return event begin date formatted
     */      
    public String eventBeginString() {
        return DateUtils.formatExtDate(this.beginDate);
    }
 
    /**
     * This method formats the date for the last update date of the event end
     * @return event end date formatted
     */    
    public String eventEndString() {
        return DateUtils.formatExtDate(this.endDate);
    }

    public Date getCreatedEvent() {
        return createdEvent;
    }

    public void setCreatedEvent(Date createdEvent) {
        this.createdEvent = createdEvent;
    }

    public Boolean getPublicEvent() {
        return publicEvent;
    }

    public void setPublicEvent(Boolean publicEvent) {
        this.publicEvent = publicEvent;
    }

    public Boolean getIndoor() {
        return indoor;
    }

    public void setIndoor(Boolean indoor) {
        this.indoor = indoor;
    }
    
    public String getIndoorString(){
        if(getIndoor())
            return "Indoor";
        else
            return "Outdoor";
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<User> getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(Set<User> invitedUsers) {
        this.invitedUsers = invitedUsers;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.id);
        return hash;
    }

    /**
     * Two event are equals if they have the same id
     * @param obj
     * @return true if the events are equals
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Event other = (Event) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}
