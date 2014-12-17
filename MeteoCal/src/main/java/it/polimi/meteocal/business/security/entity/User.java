/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.business.security.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 *
 * @author Andrea
 */
@Entity
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Pattern(regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
            message = "invalid email")
    @NotNull(message = "May not be empty")
    private String email;
    
    @NotNull(message = "May not be empty")
    private String password;
    
    @NotNull(message = "May not be empty")
    private String groupName;
    
    @NotNull(message = "May not be empty")
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date birthDate;
    
    @NotNull(message = "May not be empty")
    private String name;
    
    @NotNull(message = "May not be empty")
    private String surname;
    
    @NotNull(message = "May not be empty")
    private char gender;
    
    @NotNull(message = "May not be empty")
    private String address;
    
    @NotNull(message = "May not be empty")
    private String city;
    
    @NotNull(message = "May not be empty")
    private Boolean privateCalendar = true;
    
    @ManyToMany(mappedBy = "users")
    private Set<Event> events;
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            BigInteger bigInt = new BigInteger(1, hash);
            this.password = bigInt.toString(16);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
    
    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }    
    
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }      
        
    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }    
    
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }    
    
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }    
     
    public Boolean getPrivateCalendar() {
        return privateCalendar;
    }

    public void setPrivateCalendar(Boolean privateCalendar) {
        this.privateCalendar = privateCalendar;
    }    

    public Set<Event> getEvents() {
        return events;
    }

    public void setUserEvents(Set<Event> events) {
        this.events = events;
    }
}
