package net.dm73.plainpress.util;

import android.util.Log;

import net.dm73.plainpress.model.Location;

import java.sql.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public abstract class EventUtil {

    public static String getDifferenceTime(long timestamp){

        long diff = (System.currentTimeMillis() - timestamp)/1000;

        long diffMinutes = diff/60;
        long diffHoures = diff/3600;
        long diffDays = diff/86400;
        long diffMonth = diff/2592000;

        if(diffMonth>0){
            return String.format((int) Math.floor(diffMonth) + ((int) Math.floor(diffMonth) == 1 ? " month" : " month"));
        }else if(diffDays>0){
            return String.format(((int) Math.floor(diffDays)) + (((int) Math.floor(diffDays)) == 1 ? " day" : " days"));
        }else if(diffHoures>0){
            return String.format((int) Math.floor(diffHoures)+ ((int) Math.floor(diffHoures) == 1 ? " hour" : " hours"));
        }else if(diffMinutes>0){
            return String.format((int) Math.floor(diffMinutes) + " min");
        }else{
            return String.format("now");
        }
    }

    public static String getLocationName(Location location) {

        if (location.getCity() != null && !location.getCity().isEmpty()) {
            return location.getCity();
        }

        if (location.getCountry() != null && !location.getCountry().isEmpty()) {
            return location.getCountry();
        }

        return "Unknoun";
    }

    public static String customViewNumber(Long viewNumbers) {

        if (viewNumbers < 1000)
            return viewNumbers + "";
        else if (viewNumbers < 1000000)
            return new DecimalFormat("###.# K").format((viewNumbers / 1000f));
        else
            return new DecimalFormat("###.# M").format((viewNumbers / 1000000f));

    }

    public static String convertDateToLocalTimeZine(long timestamp){
        Date date = new Date(timestamp);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy  hh:mm aa");
        format.setTimeZone(TimeZone.getDefault());
        Log.e("tiemstamp", timestamp+"");
        return format.format(date);
    }

    public static String convertDateToRedableDate(long timestamp){
        Date date = new Date(timestamp);
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm aa");
        format.setTimeZone(TimeZone.getDefault());
        Log.e("tiemstamp", timestamp+"");
        return format.format(date);
    }

    public static long convertDateToTimestamp(String date){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy  HH:mm");
        format.setTimeZone(TimeZone.getDefault());

        try {
            return format.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0l;
        }
    }

    public static String convertDateToTimestampAMPM(String date){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy  HH:mm");
        SimpleDateFormat formatAm = new SimpleDateFormat("dd/MM/yyyy  hh:mm aa");
        try {
            java.util.Date d = format.parse(date);
            return formatAm.format(d);

        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }


}
