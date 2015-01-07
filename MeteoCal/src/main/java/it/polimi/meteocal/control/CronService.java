/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

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
    
    /*
    Schedule 1: ogni ora deve scorrere tutti gli eventi futuri per aggiungere/aggiornare il meteo
                    la logica che verifica l'effettivo avvenimento di una aggiunta o di un aggiornamento 
                    in cui le condizioni meteo cambiano -> deve essere in EventController e manda le notifiche
    
    Schedule 2: ogni giorno ad un'ora fissa controlla gli eventi di domani -> EventController farà il controllo
                    e se il tempo fa schifo deve mandare al creatore la notifica speciale, agli altri utenti
                    la notifica normale
    
    Schedule 3: ogni giorno ad un'ora fissa controlla gli eventi tra 3 giorni e manda in caso di brutto tempo 
                    la notifica con la proposta di cambio data
    */
}
