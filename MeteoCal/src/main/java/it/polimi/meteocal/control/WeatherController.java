/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import it.polimi.meteocal.entity.Weather;
import it.polimi.meteocal.entityManager.WeatherManager;
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
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.primefaces.json.JSONArray;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

/**
 * This is the business logic class for Weather
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
    
    @EJB
    WeatherManager weatherManager;
    
    /**
     * This method searches for weather in the specified city (search) and date (when).
     * When finds the forecasts, it sets all the parameters in the current object.
     * @param search the name of the city where to search forecast
     * @param when the date when to search forecast
     * @return true if found forecast, false if not available
     */
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
    
    /**
     * This method searches the first good day forecast in the specified city (search), close to the date (when)
     * When finds the forecasts, it sets all the parameters in the current object.
     * @param search the name of the city where to search forecast
     * @param when the target date on which is based the search
     * @return true if found forecast which is good, false if not available
     */
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
            int closer = -1;
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
    
    /**
     * This method returns true if the current weather is bad
     * @return true if bad weather
     */
    private Boolean isBad() {
        return this.isBadTxt(this.weatherText);
    }
    
    /**
     * Thsi method returns if the specified text contains bad weather
     * @param text to be analyzed
     * @return true if bad weather
     */
    private Boolean isBadTxt(String text) {
        for(String s: BAD_WEATHER) {
            if(text.toLowerCase().contains(s))
                return true;
        }   
        return false;
    }
    
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
    
    /**
     * This method creates a Weather object for the first good weather day found around the specified date, in the specified city.
     * It returns null if the forecast is not available.
     * @param city: String, the city where you want to obtain the forecast
     * @param when: Date, the date when you want to obtain the forecast
     * @return weather if available, null if not
     */
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
    
    /**
     * This method destroys weather with specified id
     * IMPORTANT: first set null event.weather
     * @param id of the weather to be destroyed
     */
    public void destroyWeather(Integer id) {
        Weather weather = weatherManager.find(id);
        if(weather!=null) {
            delete(weather);
        }
    }
    
    /**
     * This method checks for updates on the specified weather id
     * @param id of the weather to be updated
     */
    public void checkWeatherUpdate(Integer id) {
        Weather weather = weatherManager.find(id);
        this.checkWeatherUpdate(weather);
    }
    
    /**
     * This method checks if there is an update for the specified weather
     * @param weather to be updated
     */
    private void checkWeatherUpdate(Weather weather) {
        if(weather==null) return;
        if(this.searchForecast(weather.getCity(), weather.getForecastDate())) {
            weather.setWeather(this.weatherText);
            weather.setMaxTemp(this.maxTemp);
            weather.setMinTemp(this.minTemp);
            weather.setLastUpdate(this.date);
            this.update(weather);
        } else {
            weather.setWeather("An error occurred while updating weather."
                    + "Please try to edit and save the event.");
            weather.setMaxTemp(0);
            weather.setMinTemp(0);
            weather.setLastUpdate(new Date());
            this.update(weather);
        }  
    }
    
    /**
     * This method checks updates for all future forecasts
     */
    public void checkAllWeather() {
        List<Weather> listWeather = weatherManager.findAllFuture();
        for(Weather w: listWeather) {
            Logger.getLogger(WeatherController.class.getName()).log(Level.INFO,
                    "{0} / {1}", new Object[]{w.getForecastDate().toString(), w.getCity()});
            this.checkWeatherUpdate(w);
        }
    }
    
    /**
     * This method forwards the save request to the entityManager
     * @param weather to be saved
     */
    private void save(Weather weather) {
        weatherManager.save(weather);
    }
 
    /**
     * This method forwards the update request to the entityManager
     * @param weather to be updated
     */
    private void update(Weather weather) {
        weatherManager.update(weather);
    }
 
    /**
     * This method forwards the delete request to the entityManager
     * @param weather to be deleted
     */
    private void delete(Weather weather) {
        weatherManager.delete(weather);
    }
}
