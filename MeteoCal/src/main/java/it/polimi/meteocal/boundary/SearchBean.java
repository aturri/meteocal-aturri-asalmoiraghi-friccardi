/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

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
public class SearchBean implements Serializable{
    
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
    
    public String getKeyword() {
        return this.keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public List<Event> getEvents (){
        return this.events;
    }
    
    public List<User> getUsers (){
        return this.users;
    }
    
    public void searchKeyword(){
        if(keyword.length()>2){
            this.events = sc.getEvents(keyword);
            this.users = sc.getUsers(keyword);
            MessageBean.addInfo("resultsMessage","Found " + Integer.toString(users.size()) + " users");
            MessageBean.addInfo("resultsMessage","Found " + Integer.toString(events.size()) + " events");
        }
        else{
            this.events = new ArrayList<>();
            this.users = new ArrayList<>();
        }
    }
}
