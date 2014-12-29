/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entityManager;

import it.polimi.meteocal.control.JsonReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

/**
 *
 * @author Andrea
 */
@Stateless
public class WeatherController {
    private static final String API = "https://query.yahooapis.com/v1/public/yql?q=";
    private static final String QUERY_BEGIN = "select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22";
    private static final String QUERY_END = "%22)%20and%20u%3D'c'&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
    private static final DateFormat format = new SimpleDateFormat("EEE, d MMM yyyy h:mm a z", Locale.ENGLISH);
    private static final DateFormat formatForecast = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
    
    private Date date;
    private String city;
    private String weather;
    private float minTemp;
    private float maxTemp;  
    
    private Boolean searchForecast(String search, Date when) {
        JSONObject json;
        String whenStr = formatForecast.format(when);
        try {
            json = JsonReader.readJsonFromUrl(API+QUERY_BEGIN+URLEncoder.encode(search,"UTF-8")+QUERY_END);
            JSONObject result = json.getJSONObject("query").getJSONObject("results").getJSONObject("channel");
            this.city = result.getJSONObject("location").getString("city") + ", " + result.getJSONObject("location").getString("country");
            JSONObject weather = result.getJSONObject("item");
            this.date = format.parse(weather.getString("pubDate"));
            JSONArray forecasts = weather.getJSONArray("forecast");
            for(int i=0;i<forecasts.length();i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                String forecastDate = forecast.getString("date");
                if(forecastDate.equals(whenStr)) {
                    this.weather = forecast.getString("text");
                    this.maxTemp = Float.parseFloat(forecast.getString("high"));
                    this.minTemp = Float.parseFloat(forecast.getString("low"));
                    return true;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(WeatherController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(WeatherController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(WeatherController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public String test() {
        Date today = new Date();
        Date tomorrow = new Date(today.getTime() + (1000 * 60 * 60 * 24));
        if(this.searchForecast("Milano", tomorrow)) {        
            return "(Last update: " + date.toString() + ") Forecast for " + city + " on " + tomorrow.toString() + " " + 
                    weather + " " + Float.toString(minTemp) + " °C - " + Float.toString(maxTemp) + " °C";
        }
        return "Not available";
    }
    
    /*
    L'utente inserisce città e data inizio/fine evento, con ajax viene creata l'entità Weather
    in cui sono aggiunti i dettagli della previsione se disponibili (negli eventi che durano
    più di un giorno si guarda il giorno con le previsioni peggiori).
    Quando fa submit l'utente, il weather è già creato e viene associato all'evento.
    Quando cancella i campi città, date durante la creazione, viene cancellata l'entità weather.
    
    Quando l'utente aggiorna l'evento, weather esiste già se ci sono città e date, quando modifica,
    sempre con ajax viene distrutta e creata l'entità.
    
    La creazione e distruzione entità saranno due metodi in questa classe che usano il metodo di ricerca
    previsioni e si interfacciano al database. La creazione deve restituire il meteo se disponibile, altrimenti
    null, ci penserà EventBean a mostrare qualcosa all'utente.
    
    Qui verrà implementato anche un metodo per aggiornare tutti i Weather presenti nel database che verrà
    chiamato periodicamente (ogni 12 ore) in automatico.
    
    Verrà implemetato anche un metodo per aggiornare il Weather quando l'utente vede i dettagli dell'evento.
    
    */

}
