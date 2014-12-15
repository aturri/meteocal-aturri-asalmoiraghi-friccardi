/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.business.security.entity;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
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
    private Integer eventId;
    
    //@NotNull(message = "May not be empty")
    //private User creator;
    //private Waether weather;
    
    @NotNull(message = "May not be empty")
    private String title;
    
    private String description;

    
    private String city;
    private String address;
    private String locationInfo;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    @NotNull(message = "May not be empty")
    private Date beginDate;
    @NotNull(message = "May not be empty")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date endDate;
    @NotNull(message = "May not be empty")
    private Time beginTime;
    @NotNull(message = "May not be empty")
    private Time endTime;

    @NotNull(message = "May not be empty")
    private Boolean publicEvent;
    @NotNull(message = "May not be empty")
    private Boolean indoor;
       
    public Integer getId() {
        return eventId;
    }

    public void setId(Integer id) {
        this.eventId = id;
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

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Time getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Time beginTime) {
        this.beginTime = beginTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
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

  
}
