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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;

/**
 *
 * @author Andrea
 */
@Entity
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "NOTIF_ID")
    private Integer id;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date genDate;
    private Time genTime;
    private String text;
    private char type;
    private Boolean readByUser = false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
