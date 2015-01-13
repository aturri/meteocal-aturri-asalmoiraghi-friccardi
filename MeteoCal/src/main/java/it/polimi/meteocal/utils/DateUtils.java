/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.utils;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author andrea
 */
public class DateUtils {
    /**
     * This method returns the current date, rounded to the next :00/:15/:30/:45 minute
     * @return current date rounded
     */
    public static Date getToday() {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 15;
        calendar.add(Calendar.MINUTE, 15-mod);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    /**
     * This method set the date time to midnight
     * @param date
     * @return date with time at midnight
     */
    public static Date setTimeToMidnight(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( date );
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    /**
     * This method check if the date is today
     * @param date
     * @return true if is today
     */
    public static Boolean isToday(Date date) {
        return date.equals(setTimeToMidnight(new Date()));
    }
}
