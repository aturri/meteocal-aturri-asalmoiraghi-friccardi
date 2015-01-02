/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import it.polimi.meteocal.entityManager.WeatherController;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

/**
 *
 * @author Andrea
 */
@Singleton
public class CronService {

    @EJB
    WeatherController weatherControl;
    
    //@Schedule(minute="*/1", hour="*", persistent=false)
    @Schedule(hour="*/4", persistent=false)
    public void updateWeather() {
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,
                "Updating weather...");
        weatherControl.checkAllWeather();
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,
                "Weather updated");
    }
}
