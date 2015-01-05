/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entityManager;

import it.polimi.meteocal.entity.Weather;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 * This class manages CRUD operations for Weather
 * @author Andrea
 */
@Stateless
public class WeatherManager {
    
    @PersistenceContext
    private EntityManager em;

    /**
     * This method saves the weather to the persistence context
     * @param weather to be saved
     */
    public void save(Weather weather) {
        em.persist(weather);
    }
    
    /**
     * This method updates the weather in the persistence context
     * @param weather to be saved
     */
    public void update(Weather weather) {
        em.merge(weather);
    }
    
    /**
     * This method removes the weather from the persistence context
     * @param weather to be removed
     */
    public void delete(Weather weather) {
        Weather toBeDeleted = em.merge(weather);
        em.remove(toBeDeleted);
    }
    
    /**
     * This method finds a weather object by id
     * @param id of the weather to be found
     * @return weather associated to the id
     */
    public Weather find(Integer id) {
        return em.find(Weather.class, id);
    }
    
    /**
     * This method finds all weather objects with future date
     * @return list of weather ojbects with date >= current date
     */
    public List<Weather> findAllFuture() {
        TypedQuery<Weather> query = em.createQuery("SELECT w FROM Weather w WHERE w.forecastDate >= CURRENT_TIMESTAMP", Weather.class);
        return query.getResultList();
    }
}
