/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

/**
 * This class contains the business logic for the scheduled tasks
 * @author Andrea
 */
@Singleton
public class CronService {

    @Inject
    EventController eventControl;
    
    @Schedule(hour="*/1", persistent=false)
    public void updateWeather() {
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,"SCHEDULED TASK #1 BEGIN");
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,"Repeated every hour");
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,new Date().toString());
        eventControl.checkWeatherFutureEvents();
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,"SCHEDULED TASK #1 END");
    }
    
    @Schedule(hour="10", dayOfWeek="*", persistent=false)
    public void sendBadWeatherAlerts() {
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,"SCHEDULED TASK #3 BEGIN");
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,"Repeated every day at 10:00 am");
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,new Date().toString());
        eventControl.checkBadWeatherTomorrow();
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,"SCHEDULED TASK #3 END");
    }
    
    @Schedule(hour="10", dayOfWeek="*", persistent=false)
    public void sendClosestSunnyDay() {
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,"SCHEDULED TASK #3 BEGIN");
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,"Repeated every day at 10:00 am");
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,new Date().toString());
        eventControl.checkBadWeatherAndSearch();
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,"SCHEDULED TASK #3 END");
    }
    
    //@Schedule(minute="*/1", hour="*", persistent=false)
    public void test() {
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,"SCHEDULED TASK TEST BEGIN");
        eventControl.checkWeatherFutureEvents();
        eventControl.checkBadWeatherTomorrow();
        eventControl.checkBadWeatherAndSearch();
        Logger.getLogger(CronService.class.getName()).log(Level.INFO,"SCHEDULED TASK TEST END");
    }
}
