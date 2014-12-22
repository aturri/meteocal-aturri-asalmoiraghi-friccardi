/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entity;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;

/**
 *
 * @author Andrea
 */
@Embeddable
public class NotificationId implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date genDate;
    private Time genTime;
    private String userId;
    private Integer eventId;

    public Date getGenDate() {
        return genDate;
    }

    public void setGenDate(Date genDate) {
        this.genDate = genDate;
    }

    public Time getGenTime() {
        return genTime;
    }

    public void setGenTime(Time genTime) {
        this.genTime = genTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    
}
