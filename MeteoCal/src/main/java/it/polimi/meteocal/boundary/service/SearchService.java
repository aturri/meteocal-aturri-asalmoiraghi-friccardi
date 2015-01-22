/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary.service;

import it.polimi.meteocal.utils.MessageUtility;
import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.control.SearchController;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author andrea
 */
@Named
@ViewScoped
public class SearchService implements Serializable{
    
    @EJB
    SearchController sc;
    
    private String keyword;
    private List<Event> events;
    private List<User> users;
    
    @PostConstruct
    public void init(){
        this.events = new ArrayList<>();
        this.users = new ArrayList<>();
    }
    
    /**
     * Get the search keywords
     * @return keyword
     */
    public String getKeyword() {
        return this.keyword;
    }
    
    /**
     * Set the search keywords
     * @param keyword 
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    /**
     * Get the list of events that match the searched keywords
     * @return list of events
     */
    public List<Event> getEvents (){
        return this.events;
    }
    
    /**
     * Get the list of users that match the searched keywords
     * @return the list of users
     */
    public List<User> getUsers (){
        return this.users;
    }
    
    /**
     * Do the search of events/users with the keywords.
     * Notify the user of the number of events/users found.
     * The search start when the keyword is at least length 3 characters.
     */
    public void searchKeyword(){
        if(keyword.length()>2){
            this.events = sc.getEvents(keyword);
            this.users = sc.getUsers(keyword);
            MessageUtility.addInfo("resultsMessage","Found " + Integer.toString(users.size()) + " users");
            MessageUtility.addInfo("resultsMessage","Found " + Integer.toString(events.size()) + " events");
        }
        else{
            this.events = new ArrayList<>();
            this.users = new ArrayList<>();
        }
    }
}
