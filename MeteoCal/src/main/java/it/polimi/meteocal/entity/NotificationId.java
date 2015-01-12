/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Andrea
 */
@Embeddable
public class NotificationId implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();
    private String userId;
    private Integer eventId;

    public Date getGenDate() {
        return created;
    }

    public void setGenDate(Date created) {
        this.created = created;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NotificationId other = (NotificationId) obj;
        if (!Objects.equals(this.created, other.created)) {
            return false;
        }
        if (!Objects.equals(this.eventId, other.eventId)) {
            return false;
        }
        if (!Objects.equals(this.userId, other.userId)) {
            return false;
        }
        return true;
    }
}
