/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entityManager;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author andrea
 */
@Stateless
public class SearchController {

    @EJB
    UserManager um;
    @EJB
    EventManager evm;
    @PersistenceContext
    private EntityManager em;
    
    private List<String> keywords;

    public List<Event> getEvents(String keyword) {
        keywords = java.util.Arrays.asList(keyword.split(" "));
        String queryText = "SELECT * FROM Event e WHERE e.publicEvent = 1 AND ";
        
        String preText = "";
        for(String word: keywords){
            queryText += preText + "concat_ws(' ',e.title,e.city) LIKE '%"+word+"%'";
            preText = " OR ";
        }
        
        Query query = em.createNativeQuery(queryText,Event.class);
        List<Event> results = new ArrayList<>(query.getResultList());
        results.sort(new SearchEventsComparator(keyword));
        return results;
    }
    
    public List<User> getUsers(String keyword) {
        keywords = java.util.Arrays.asList(keyword.split(" "));
        String queryText = "SELECT * FROM User u WHERE privatecalendar = 0 AND ";
        String preText = "";
        for(String word: keywords){
            queryText += preText + "concat_ws(' ',u.email,u.name,u.surname) LIKE '%"+word+"%'";
            preText = " OR ";
        }
        
        Query query = em.createNativeQuery(queryText,User.class);
        List<User> results = new ArrayList<>(query.getResultList());
        results.sort(new SearchUsersComparator(keyword));
        return results;
    }
    
    
    static class SearchEventsComparator  implements Comparator<Event>{

        List<String> keywords;
        
        private SearchEventsComparator(String keyword) {
            keywords = java.util.Arrays.asList(keyword.split(" "));
        }

        @Override
        public int compare(Event o1, Event o2) {
            int score1 = 0;
            int score2 = 0;
            
            for(String key : keywords){
                if(o1.getTitle().toLowerCase().contains(key.toLowerCase())) score1 += 2;
                if(o2.getTitle().toLowerCase().contains(key.toLowerCase())) score2 += 2;
                if(o1.getCity() != null && o1.getCity().toLowerCase().contains(key.toLowerCase())) score1 += 1;
                if(o2.getCity() != null && o2.getCity().toLowerCase().contains(key.toLowerCase())) score2 += 1;
            }
            return score2-score1;
        }
    }
    
    static class SearchUsersComparator  implements Comparator<User>{

        List<String> keywords;
        
        private SearchUsersComparator(String keyword) {
            keywords = java.util.Arrays.asList(keyword.split(" "));
        }

        @Override
        public int compare(User o1, User o2) {
            int score1 = 0;
            int score2 = 0;
            
            for(String key : keywords){
                if(o1.getEmail().toLowerCase().contains(key.toLowerCase())) score1 += 3;
                if(o2.getEmail().toLowerCase().contains(key.toLowerCase())) score2 += 3;
                if(o1.getName().toLowerCase().contains(key.toLowerCase())) score1 += 2;
                if(o2.getName().toLowerCase().contains(key.toLowerCase())) score2 += 2;
                if(o1.getSurname().toLowerCase().contains(key.toLowerCase())) score1 += 4;
                if(o2.getSurname().toLowerCase().contains(key.toLowerCase())) score2 += 4;
            }
            return score2-score1;
        }
    }

}