package com.nachoverdon.mongolia.utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
  /**
   * Gets a Date hours ago from now.
   *
   * @param calendar A Calendar instance
   * @param hours How many hours ago.
   * @return Date.
   */
  public static Date hoursAgo(Calendar calendar, int hours) {
    calendar.add(Calendar.HOUR_OF_DAY, -hours);

    return calendar.getTime();
  }

  /**
   * Gets a Date hours ago from now.
   *
   * @param hours How many hours ago.
   * @return Date.
   */
  public static Date hoursAgo(int hours) {
    return hoursAgo(now(), hours);
  }

  /**
   * Gets a date hours ago from now in milliseconds.
   *
   * @param hours How many hours ago.
   * @return The time in milliseconds.
   */
  public static long hoursAgoInMs(int hours) {
    return hoursAgo(now(), hours).getTime();
  }

  /**
   * Gets a date hours ago from now in seconds.
   *
   * @param hours How many hours ago.
   * @return The time in seconds.
   */
  public static long hoursAgoInSeconds(int hours) {
    return dateToSeconds(hoursAgo(now(), hours));
  }

  /**
   * Gets a Date days ago from now.
   *
   * @param calendar A Calendar instance
   * @param days How many days ago.
   * @return Date.
   */
  public static Date daysAgo(Calendar calendar, int days) {
    calendar.add(Calendar.DAY_OF_MONTH, -days);

    return calendar.getTime();
  }

  /**
   * Gets a Date days ago from now.
   *
   * @param days How many days ago.
   * @return Date.
   */
  public static Date daysAgo(int days) {
    return daysAgo(now(), days);
  }

  /**
   * Gets a date days ago from now in milliseconds.
   *
   * @param days How many days ago.
   * @return The time in milliseconds.
   */
  public static long daysAgoInMs(int days) {
    return daysAgo(now(), days).getTime();
  }

  /**
   * Gets a date days ago from now in seconds.
   *
   * @param days How many days ago.
   * @return The time in seconds.
   */
  public static long daysAgoInSeconds(int days) {
    return dateToSeconds(daysAgo(now(), days));
  }

  /**
   *  Converts milliseconds to seconds.
   *
   * @param time The time in milliseconds.
   * @return The time in seconds.
   */
  public static long msToSeconds(long time) {
    return time / 1000;
  }

  /**
   *  Converts date to seconds.
   *
   * @param date The Date object.
   * @return The time in seconds.
   */
  public static long dateToSeconds(Date date) {
    return msToSeconds(date.getTime());
  }

  /**
   *  Gets the Calendar instance.
   *
   * @return Calendar.
   */
  public static Calendar now() {
    return Calendar.getInstance();
  }

  /**
   *  Gets the actual time as a Date object.
   *
   * @return Date.
   */
  public static Date nowAsDate() {
    return now().getTime();
  }

  /**
   *  Gets the actual time in milliseconds.
   *
   * @return The time in milliseconds.
   */
  public static long nowInMs() {
    return nowAsDate().getTime();
  }

  /**
   *  Gets the actual time in seconds.
   *
   * @return The time in seconds.
   */
  public static long nowInSeconds() {
    return msToSeconds(nowInMs());
  }
}
