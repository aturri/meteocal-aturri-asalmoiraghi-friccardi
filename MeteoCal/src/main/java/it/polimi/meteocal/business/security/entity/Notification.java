/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.business.security.entity;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Temporal;

/**
 *
 * @author Andrea
 */
@Entity
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    private NotificationId id;
 
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "USER_EMAIL")
    private User user;
    
    @ManyToOne
    @MapsId("eventId")
    @JoinColumn(name = "EVENT_ID")
    private Event event;
        
    private String text;
    private char type;
    private Boolean readByUser = false;

    public NotificationId getId() {
        return id;
    }

    public void setId(NotificationId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    public Boolean getReadByUser() {
        return readByUser;
    }

    public void setReadByUser(Boolean readByUser) {
        this.readByUser = readByUser;
    }
    

}
