/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.entityManager;

import it.polimi.meteocal.entity.Weather;
import it.polimi.meteocal.control.JsonReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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
    private static final List<String> BAD_WEATHER = Arrays.asList("tornado","storm","hurricane",
            "thunderstorm","rain","snow","sleet","drizzle","shower","hail","dust","fog","haze","smoky","blustery");
    
    private Date date;
    private Date forecastDate;
    private String city;
    private String weatherText;
    private float minTemp;
    private float maxTemp; 
    
    @PersistenceContext
    private EntityManager em;
    
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
                String forecastDateStr = forecast.getString("date");
                if(forecastDateStr.equals(whenStr)) {
                    this.weatherText = forecast.getString("text");
                    this.forecastDate = formatForecast.parse(forecastDateStr);
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
    
    private Boolean searchFirstGoodForecast(String search, Date when) {
        JSONObject json;
        String whenStr = formatForecast.format(when);
        try {
            json = JsonReader.readJsonFromUrl(API+QUERY_BEGIN+URLEncoder.encode(search,"UTF-8")+QUERY_END);
            JSONObject result = json.getJSONObject("query").getJSONObject("results").getJSONObject("channel");
            this.city = result.getJSONObject("location").getString("city") + ", " + result.getJSONObject("location").getString("country");
            JSONObject weather = result.getJSONObject("item");
            this.date = format.parse(weather.getString("pubDate"));
            JSONArray forecasts = weather.getJSONArray("forecast");
            //index of the searched day
            int whenIndex = 0;
            //list containing indexes of good forecast dates
            List<Integer> goodWeatherIndexes = new ArrayList<>();
            for(int i=0;i<forecasts.length();i++) {
                JSONObject forecast = forecasts.getJSONObject(i);
                String forecastDateStr = forecast.getString("date");
                if(!forecastDateStr.equals(whenStr)) {
                    if(!isBadTxt(forecast.getString("text").toLowerCase())) {
                        goodWeatherIndexes.add(i);
                    }
                } else {
                   whenIndex = i;
                }
            }
            if(goodWeatherIndexes.isEmpty()) return false;
            //now we search in the list the closest index to the one of the searched day
            int closer = 0;
            for(Integer j: goodWeatherIndexes) {
                if(Math.abs(j-whenIndex)<=Math.abs(closer-whenIndex)) {
                   closer = j;
                }
            }
            //now i contains the index of the first day of good weather
            JSONObject forecast = forecasts.getJSONObject(closer);
            this.weatherText = forecast.getString("text");
            this.forecastDate = formatForecast.parse(forecast.getString("date"));
            this.maxTemp = Float.parseFloat(forecast.getString("high"));
            this.minTemp = Float.parseFloat(forecast.getString("low"));
            return true;
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
                    weatherText + " " + Float.toString(minTemp) + " °C - " + Float.toString(maxTemp) + " °C";
        }
        return "Not available";
    }
    
    private Boolean isBad() {
        return this.isBadTxt(this.weatherText);
    }
    
    private Boolean isBadTxt(String text) {
        for(String s: BAD_WEATHER) {
            if(text.toLowerCase().contains(s))
                return true;
        }   
        return false;
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
    
    /**
     * This method creates a Weather object for the specified city and in the specified date.
     * It returns null if the forecast is not available.
     * @param city: String, the city where you want to obtain the forecast
     * @param when: Date, the date when you want to obtain the forecast
     * @param save: Boolean, if true saves the entity into the database
     * @return weather if available, null if not
     */
    public Weather createWeather(String city, Date when, Boolean save) {
        if(this.searchForecast(city, when)) {
            Weather weather = new Weather();
            weather.setCity(city);
            weather.setWeather(this.weatherText);
            weather.setMaxTemp(this.maxTemp);
            weather.setMinTemp(this.minTemp);
            weather.setForecastDate(this.forecastDate);
            weather.setLastUpdate(this.date);
            if(this.isBad())
                weather.setBad(true);
            if(save) this.save(weather);
            return weather;
        }
        return null;
    }
    
    public Weather searchFirstGoodDay(String city, Date when) {
         if(this.searchFirstGoodForecast(city, when)) {
            Weather weather = new Weather();
            weather.setCity(city);
            weather.setWeather(this.weatherText);
            weather.setMaxTemp(this.maxTemp);
            weather.setMinTemp(this.minTemp);
            weather.setForecastDate(this.forecastDate);
            weather.setLastUpdate(this.date);
            return weather;
        }
        return null;       
    }
    
    public void destroyWeather(Integer id) {
        this.delete(this.find(id));
    }
    
    public void checkWeatherUpdate(Integer id) {
        Weather weather = this.find(id);
        this.checkWeatherUpdate(weather);
    }
    
    private void checkWeatherUpdate(Weather weather) {
        if(weather==null) return;
        if(this.searchForecast(weather.getCity(), weather.getForecastDate())) {
            weather.setWeather(this.weatherText);
            weather.setMaxTemp(this.maxTemp);
            weather.setMinTemp(this.minTemp);
            weather.setLastUpdate(this.date);
            this.update(weather);
        } else {
            this.delete(weather);
        }  
    }
    
    private void checkAllWeather() {
        Logger.getLogger(WeatherController.class.getName()).log(Level.INFO, "Updating weather for all events...");
        List<Weather> listWeather = this.findAll();
        for(Weather w: listWeather) {
            this.checkWeatherUpdate(w);
        }
    }
    
    private void save(Weather weather) {
        em.persist(weather);
    }
    
    private void update(Weather weather) {
        em.merge(weather);
    }
    
    private void delete(Weather weather) {
        Weather toBeDeleted = em.merge(weather);
        em.remove(toBeDeleted);
    }
    
    private Weather find(Integer id) {
        return em.find(Weather.class, id);
    }
    
    private List<Weather> findAll() {
        TypedQuery<Weather> query = em.createQuery("SELECT w FROM Weather w WHERE w.forecastDate < CURRENT_TIMESTAMP", Weather.class);
        return query.getResultList();
    }
}
