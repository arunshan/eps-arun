/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eps.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Robert Eder
 */
public class EpsStatic
{

  public static final int ciDatabaseTypeMysql = 0;
  public static final int ciDatabaseTypeMssql = 1;
  public static final int ciDatabaseTypeODBC = 2;

  public String myDump(byte[] pMsg, int iLen)
  {
    String stReturn = "<table border=1>";
    int i = 0;
    String stText = "";

    for (i = 0; i < iLen; i++)
    {
      if ((i % 16) == 0)
      {
        stReturn += "<td>" + stText + "</td></tr><tr><td align=right>" + i;
        stText = "";
      }
      stReturn += "<td>" + Integer.toHexString((pMsg[i] & 0xFF)) + "</td>";
      if (pMsg[i] > ' ' && pMsg[i] <= 'z')
      {
        byte[] byteArray = new byte[1];
        byteArray[0] = pMsg[i];
        stText += new String(byteArray);
      } else
      {
        stText += '.';
      }
    }
    for (; (i % 16) != 0; i++)
    {
      stReturn += "<td>&nbsp;</td>";
    }
    stReturn += "<td>" + stText + "</td></tr></table>";
    return stReturn;
  }

  public static int getDay(String stDay)
  {
    int iDay = 0;
    stDay = stDay.trim().toLowerCase();
    if (stDay.startsWith("mo"))
      iDay = 1;
    else if (stDay.startsWith("tu"))
      iDay = 2;
    else if (stDay.startsWith("we"))
      iDay = 3;
    else if (stDay.startsWith("th"))
      iDay = 4;
    else if (stDay.startsWith("fr"))
      iDay = 5;
    else if (stDay.startsWith("sa"))
      iDay = 6;
    else //if(stDay.startsWith("su"))
      iDay = 0;

    return iDay;
  }

  public static int getMonth(String stMonth)
  {
    int iMonth = 0;
    stMonth = stMonth.trim().toLowerCase();
    if (stMonth.equals("january"))
      iMonth = 1;
    else if (stMonth.equals("february"))
      iMonth = 2;
    else if (stMonth.equals("march"))
      iMonth = 3;
    else if (stMonth.equals("april"))
      iMonth = 4;
    else if (stMonth.equals("may"))
      iMonth = 5;
    else if (stMonth.equals("june"))
      iMonth = 6;
    else if (stMonth.equals("july"))
      iMonth = 7;
    else if (stMonth.equals("august"))
      iMonth = 8;
    else if (stMonth.equals("september"))
      iMonth = 9;
    else if (stMonth.equals("october"))
      iMonth = 10;
    else if (stMonth.equals("november"))
      iMonth = 11;
    else if (stMonth.equals("december"))
      iMonth = 12;

    return iMonth;
  }

  public static String getMonth2(int iMonth)
  {
    String stMonth = "";
    switch (iMonth)
    {
      case 1:
        stMonth = "January";
        break;
      case 2:
        stMonth = "February";
        break;
      case 3:
        stMonth = "March";
        break;
      case 4:
        stMonth = "April";
        break;
      case 5:
        stMonth = "May";
        break;
      case 6:
        stMonth = "June";
        break;
      case 7:
        stMonth = "July";
        break;
      case 8:
        stMonth = "August";
        break;
      case 9:
        stMonth = "September";
        break;
      case 10:
        stMonth = "October";
        break;
      case 11:
        stMonth = "November";
        break;
      case 12:
        stMonth = "December";
        break;
    }
    return stMonth;
  }

  public static long daysBetween(Calendar startDate, Calendar endDate)
  {
    Calendar date = null;
    Calendar dtStart = null;
    Calendar dtEnd = null;

    if (startDate != null)
      dtStart = startDate;
    else
      dtStart = Calendar.getInstance();

    if (endDate != null)
      dtEnd = endDate;
    else
      dtEnd = Calendar.getInstance();

    date = (Calendar) dtStart.clone();
    long daysBetween = 0;
    if (date.after(dtEnd))
    {
      while (date.after(dtEnd))
      {
        date.add(Calendar.DAY_OF_MONTH, -1);
        daysBetween--;
      }
    } else if (date.before(dtEnd))
    {
      while (date.before(dtEnd))
      {
        date.add(Calendar.DAY_OF_MONTH, 1);
        daysBetween++;
      }
    }
    return daysBetween;
  }

  public static Calendar getCalendar(String stDate)
  {
    Calendar dtReturn = Calendar.getInstance();
    try
    {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      if (stDate != null && stDate.length() > 8)
      {
        Date date;
        date = (Date) formatter.parse(stDate);
        dtReturn.setTime(date);
      }
    } catch (Exception e)
    {
    }
    return dtReturn;
  }

  public static String getDate(Calendar dtDate)
  {
    String stReturn = "";
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
    if (dtDate != null)
      stReturn = formatter.format(dtDate.getTime());
    else
      stReturn = "";
    return stReturn;
  }

  public static int countIndexOf(String content, String search)
  {
    int ctr = -1;
    int total = 0;
    while (true)
    {
      if (ctr == -1)
        ctr = content.indexOf(search);
      else
        ctr = content.indexOf(search, ctr);
      if (ctr == -1)
      {
        break;
      } else
      {
        total++;
        ctr += search.length();
      }
    }
    return total;
  }
}
