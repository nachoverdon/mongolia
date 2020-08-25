package com.nachoverdon.mongolia.utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
  /**
   * Gets a Date hours ago from now.
   *
   * @param calendar A Calendar instance
   * @param hours How many hours ago.
   * @return Date
   */
  public static Date hoursAgo(Calendar calendar, int hours) {
    calendar.add(Calendar.HOUR_OF_DAY, -hours);

    return calendar.getTime();
  }

  /**
   * Gets a Date hours ago from now.
   *
   * @param hours How many hours ago.
   * @return Date
   */
  public static Date hoursAgo(int hours) {
    return hoursAgo(Calendar.getInstance(), hours);
  }

  /**
   * Gets a date hours ago from now in seconds.
   *
   * @param hours How many hours ago.
   * @return Date
   */
  public static int hoursAgoInSeconds(int hours) {
    return dateToSeconds(hoursAgo(Calendar.getInstance(), hours));
  }

  /**
   * Gets a Date days ago from now.
   *
   * @param calendar A Calendar instance
   * @param days How many days ago.
   * @return Date
   */
  public static Date daysAgo(Calendar calendar, int days) {
    calendar.add(Calendar.DAY_OF_MONTH, -days);

    return calendar.getTime();
  }

  /**
   * Gets a Date days ago from now.
   *
   * @param days How many days ago.
   * @return Date
   */
  public static Date daysAgo(int days) {
    return daysAgo(Calendar.getInstance(), days);
  }

  /**
   * Gets a date days ago from now in seconds.
   *
   * @param days How many days ago.
   * @return Date
   */
  public static int daysAgoInSeconds(int days) {
    return dateToSeconds(daysAgo(Calendar.getInstance(), days));
  }

  /**
   *  Converts milliseconds to seconds.
   *
   * @param time The time in milliseconds.
   * @return The time in seconds
   */
  public static int msToSeconds(long time) {
    return (int) time / 1000;
  }

  /**
   *  Converts date to seconds.
   *
   * @param date The Date object.
   * @return The time in seconds
   */
  public static int dateToSeconds(Date date) {
    // @d0nt
    return msToSeconds(date.getTime());
  }
}
