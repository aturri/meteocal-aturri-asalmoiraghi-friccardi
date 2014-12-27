/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entityManager.SearchController;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.ManagedBean;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
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
    
    public SearchBean(){
        this.events = new ArrayList<>();
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
    
    public void searchKeyword(){
        this.events = sc.getEvents(keyword);
        MessageBean.addInfo("resultsMessage",Integer.toString(events.size()));
    }
}
