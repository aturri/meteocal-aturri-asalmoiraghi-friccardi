/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.EventManager;
import it.polimi.meteocal.entityManager.UserManager;
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

    /**
     * This method split the keywords received and search all events that match at least one keyword.
     * So, reorder the events found respect the number of match with the keywords.
     * With more matching, the event is in the first results of the list.
     * 
     * NB: The keywords are search in the title and in the city of the event.
     * A matching in the title is more important for the sort. 
     * @param keyword
     * @return sorted list of events
     */
    public List<Event> getEvents(String keyword) {
        keywords = java.util.Arrays.asList(keyword.split(" "));
        String queryText = "SELECT * FROM Event e WHERE e.publicEvent = 1 AND (";        
        String preText = "";
        for(String word: keywords){
            if(word.length()>2){
                queryText += preText + "concat_ws(' ',e.title,e.city) LIKE '%"+word+"%'";
                preText = " OR ";
            }
        }
        queryText += ")";
        
        Query query = em.createNativeQuery(queryText,Event.class);
        List<Event> results = new ArrayList<>(query.getResultList());
        results.sort(new SearchEventsComparator(keyword));
        return results;
    }
    
    /**
     * This method split the keywords received and search all users that match at least one keyword.
     * So, reorder the users found respect first results of the list.
     * 
     * NB: The keywords are search in the name, surname and email of the city.
     * A matching in the surname is more important for the sort. 
     * @param keyword
     * @return sorted list of users
     */ 
    public List<User> getUsers(String keyword) {
        keywords = java.util.Arrays.asList(keyword.split(" "));
        String queryText = "SELECT * FROM User u WHERE u.email NOT LIKE '"+um.getLoggedUser().getEmail()+"' AND (";
        String preText = "";
        for(String word: keywords){
            if(word.length()>2){
                queryText += preText + "concat_ws(' ',u.email,u.name,u.surname) LIKE '%"+word+"%'";
                preText = " OR ";
            }
        }
        queryText += ")";
        
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