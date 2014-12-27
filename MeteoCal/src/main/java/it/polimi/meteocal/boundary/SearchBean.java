/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entityManager.SearchController;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 *
 * @author andrea
 */
@Named
@RequestScoped
public class SearchBean {
    
    @EJB
    SearchController sc;
    
    String keyword;
    
    public SearchBean(){
    }
    
    public String getKeyword() {
        return this.keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public List<Event> getResultsEvents (){
        return sc.getEvents(keyword);
    }
}
