package com.adrapps.mytasks.domain;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by adria on 03/06/2017.
 */

public class Reminder implements Serializable {

   private Calendar date;
   private int repeatMode;
   private int year;
   private int month;
   private int dayOfMonth;
   private int hour; //24 hour format
   private int minute;
   private int second;
   private int millisecond;
   private long id;

   public Reminder(Calendar date, int repeatMode) {
      this.date = date;
      this.repeatMode = repeatMode;
      this.id = System.currentTimeMillis();
   }

   public Reminder(Calendar date) {
      this.date = date;
      this.repeatMode = 0;
      this.id = System.currentTimeMillis();
   }

   public static Reminder getInstance(){
      return new Reminder(Calendar.getInstance());
   }
   public Calendar getDate() {
      return date;
   }

   public long getTimeInMillis(){
      if (date != null){
         return date.getTimeInMillis()
      } else {
         return -1;
      }
   }

   public int getRepeatMode() {
      return repeatMode;
   }

   public int getYear() {
      if (date != null)
         return date.get(Calendar.YEAR);
      else return -1;
   }

   public int getMonth() {
      if (date != null)
         return date.get(Calendar.MONTH);
      else return -1;   }

   public int getDayOfMonth() {
      if (date != null)
         return date.get(Calendar.DAY_OF_MONTH);
      else return -1;   }

   public int getHour() {
      if (date != null)
         return date.get(Calendar.HOUR_OF_DAY);
      else return -1;
   }

   public int getMinute() {
      if (date != null)
         return date.get(Calendar.MINUTE);
      else return -1;   }

   public int getSecond() {
      if (date != null)
         return date.get(Calendar.SECOND);
      else return -1;   }

   public int getMillisecond() {
      if (date != null)
         return date.get(Calendar.MILLISECOND);
      else return -1;   }

   public void setDate(Calendar date) {
      this.date = date;
   }

   public void setRepeatMode(int repeatMode) {
      if (repeatMode < 0 || repeatMode > 6) {
         throw new IllegalArgumentException("Repeat Mode not in range [0 to 6]: " + repeatMode);
      }
      this.repeatMode = repeatMode;
   }

   public void setId() {
      this.id = System.currentTimeMillis();
   }

   public long getId() {
      return id;
   }
}
