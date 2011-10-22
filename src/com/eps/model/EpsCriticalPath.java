/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eps.model;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author guest1
 */
public class EpsCriticalPath
{

  public double dEffortHours = 0;
  public int iFLags = 0; // 1=Start Point, 2= End Point
  public int[] aDependency = null;
  public String[] aDependencyType = null;
  public double[] aDependencyLag = null;
  public int[] aSuccessor = null;
  public int[] aSuccessorProcess = null;
  public int iSlack = 0;
  public String[] aSuccessorType = null;
  public double[] aSuccessorLag = null;
  public ResultSet rsProject = null;
  public EpsUserData epsUd = null;
  private String stError = "";
  //public String stCpList = "";
  public String stTitle = "";
  public String stLc = "";
  public Calendar dtEnd = null;
  public Calendar dtStart = null;
  private String stHolidays = null;
  public int iHoliday = 0;
  public int iWeekend = 0;
  private int[] aWeekend = null;
  private int iMaxRecId = 0;
  double dLag = 0;
  public double dHoursStart = 0;
  public double dHoursEnd = 0;

  public void EpsXlsProject()
  {
    this.epsUd = null;
  }

  public void setEpsCriticalPath(int iMaxRec, EpsUserData epsUd, ResultSet rsProject, ResultSet rsSchedule, String stHolidays, int[] aWeekend)
  {
    this.epsUd = epsUd;
    this.rsProject = rsProject;
    this.iMaxRecId = iMaxRec;
    aDependency = new int[iMaxRecId];
    aDependencyType = new String[iMaxRecId];
    aDependencyLag = new double[iMaxRecId];
    aSuccessor = new int[iMaxRecId];
    aSuccessorProcess = new int[iMaxRecId];
    aSuccessorType = new String[iMaxRecId];
    aSuccessorLag = new double[iMaxRecId];
    for (int i = 0; i < aDependency.length; i++)
    {
      aDependency[i] = 0;
      aDependencyType[i] = "";
      aDependencyLag[i] = 0;
      aSuccessor[i] = 0;
      aSuccessorType[i] = "";
      aSuccessorProcess[i] = 0;
      aSuccessorLag[i] = 0;
    }
    try
    {
      dEffortHours = rsSchedule.getDouble("SchEstimatedEffort");
      stTitle = rsSchedule.getString("SchTitle");
      this.aWeekend = aWeekend;
      this.stHolidays = stHolidays;
      this.stLc = rsSchedule.getString("SchLaborCategories");
    } catch (Exception e)
    {
      this.stError += "<BR>EROR setEpsCriticalPath " + e;
    }
  }

  public String getStart()
  {
    String stReturn = "";
    try
    {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      stReturn = formatter.format(dtStart.getTime());
    } catch (Exception e)
    {
      this.stError += "<BR> ERRROR getStart " + e;
    }
    return stReturn;
  }

  public String getEnd()
  {
    String stReturn = "";
    try
    {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      stReturn = formatter.format(dtEnd.getTime());
    } catch (Exception e)
    {
      this.stError += "<BR> ERRROR getEnd " + e;
    }
    return stReturn;
  }

  public void setStartCalculateEnd(Calendar dtStartIn, double dPreviousHours)
  {
    dtStart = Calendar.getInstance();

    try
    {
      if (dtStartIn != null)
      {
        dtStart.setTime(dtStartIn.getTime());
        //dtStart.add(Calendar.DAY_OF_YEAR, +1); // Add 1 Day
      } else
      {
        dtStart.add(Calendar.DAY_OF_YEAR, +1); // Tomorrow
      }
      calculateEnd(dPreviousHours);
    } catch (Exception e)
    {
      this.stError += "<BR> ERRROR setStart " + e;
    }
  }

  public String getStartEnd()
  {
    String stReturn = "";
    try
    {
      SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
      stReturn += "{" + this.dEffortHours + "/" + this.dHoursStart + "/" + this.dHoursEnd + "} ";
      stReturn += formatter.format(dtStart.getTime()) + " - ";
      stReturn += formatter.format(dtEnd.getTime()) + " (" + EpsStatic.daysBetween(dtStart, dtEnd) + ")";
    } catch (Exception e)
    {
      this.stError += "<BR> ERRROR getStartEnd " + e;
    }
    return stReturn;
  }

  public int checkHoliday(Calendar dDate)
  {
    int iReturn = 0;

    int dayofweek = dDate.get(Calendar.DAY_OF_WEEK);
    for (int iWe = 0; iWe < aWeekend.length; iWe++)
    {
      if (dayofweek == aWeekend[iWe])
      {
        iReturn++;
        this.iWeekend++;
      }
    }
    if (iReturn == 0)
    {
      String stDate = "";
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      stDate = formatter.format(dDate.getTime());
      if (this.stHolidays.contains(stDate))
      {
        iReturn++;
        this.iHoliday++;
      }
    }
    return iReturn;
  }

  public void calculateEnd(double dPreviousHours)
  {
    int iLoop = 0;
    double dEnd = 0;
    double dEffort = dEffortHours;
    try
    {

      if (dPreviousHours >= this.epsUd.rsMyDiv.getDouble("nmHoursPerDay"))
      {
        dPreviousHours = 0;
        dtStart.add(Calendar.DAY_OF_YEAR, +1);
        do
        {
          iLoop = checkHoliday(dtStart);
          if (iLoop > 0)
            dtStart.add(Calendar.DAY_OF_YEAR, iLoop);
        } while (iLoop != 0);
      }
      dtEnd = (Calendar) dtStart.clone();
      if (dPreviousHours < this.epsUd.rsMyDiv.getDouble("nmHoursPerDay"))
      {
        this.dHoursStart = dPreviousHours;
        double dTodayMax = this.epsUd.rsMyDiv.getDouble("nmHoursPerDay") - dPreviousHours;
        if (dTodayMax > dEffort)
        {
          dTodayMax = dEffort;
        }
        dEnd = this.dHoursStart + dTodayMax;
        dEffort -= dTodayMax;
      }
      // Start Date and Hour is now known.
      dEffort = getMaxEffort(dEffort, stLc);
      if (dEffort > 0)
      { // Do all WHOLE Days
        double dDays = dEffort / this.epsUd.rsMyDiv.getDouble("nmHoursPerDay");
        for (int i = 0; i < (int) dDays; i++)
        {
          dEffort -= this.epsUd.rsMyDiv.getDouble("nmHoursPerDay");
          dEnd = this.epsUd.rsMyDiv.getDouble("nmHoursPerDay");
          dtEnd.add(Calendar.DAY_OF_YEAR, +1);
          do
          {
            iLoop = checkHoliday(dtEnd);
            if (iLoop > 0)
              dtEnd.add(Calendar.DAY_OF_YEAR, iLoop);
          } while (iLoop != 0);
        }
        if (dEffort < 0)
        {
          this.stError += "<BR>ERROR calculateEnd - Effort is negative";
          dEffort = 0;
        } else if (dEffort > 0)
        {
          if (dEnd >= this.epsUd.rsMyDiv.getDouble("nmHoursPerDay"))
          {
            dtEnd.add(Calendar.DAY_OF_YEAR, +1);
            do
            {
              iLoop = checkHoliday(dtEnd);
              if (iLoop > 0)
                dtEnd.add(Calendar.DAY_OF_YEAR, iLoop);
            } while (iLoop != 0);
            dEnd = dEffort;
          }
        }
      }
      if (dEnd > 0 && dEnd <= this.epsUd.rsMyDiv.getDouble("nmHoursPerDay"))
        this.dHoursEnd = dEnd;
    } catch (Exception e)
    {
      this.stError += "<BR> ERRROR calculateEnd " + e;
    }
  }

  public void calculateStart()
  {
    int iLoop = 0;
    try
    {
      dtStart = (Calendar) dtEnd.clone();
      double dDays = dEffortHours / this.epsUd.rsMyDiv.getDouble("nmHoursPerDay");
      for (int i = 0; i < dDays; i++)
      {
        dtStart.add(Calendar.DAY_OF_YEAR, -1);
        do
        {
          iLoop = checkHoliday(dtStart);
          if (iLoop > 0)
            dtStart.add(Calendar.DAY_OF_YEAR, -iLoop);
        } while (iLoop != 0);
      }
    } catch (Exception e)
    {
      this.stError += "<BR> ERRROR calculateStart " + e;
    }
  }

  public int addDpendency(int iRecId, String stType, double dLag)
  {
    int iReturn = 0;
    for (int i = 0; i < aDependency.length; i++)
    {
      if (aDependency[i] == iRecId)
      {
        iReturn = iRecId;
        this.aDependencyLag[i] = dLag;
        this.aDependencyType[i] = stType;
        break;
      } else if (aDependency[i] == 0)
      {
        aDependency[i] = iRecId; // tell caller we added
        this.aDependencyLag[i] = dLag;
        this.aDependencyType[i] = stType;
        break;
      }
    }
    return iReturn;
  }

  public String getError()
  {
    return this.stError;
  }

  public int addSuccessor(int iRecId)
  {
    int iReturn = 0;
    for (int i = 0; i < aSuccessor.length; i++)
    {
      if (aSuccessor[i] == iRecId)
      {
        iReturn = i;
        break;
      } else if (aSuccessor[i] == 0)
      {
        aSuccessor[i] = iRecId; // tell caller we added
        iReturn = i;
        break;
      }
    }
    return iReturn;
  }

  public int addSuccessor(ResultSet rsSchedule)
  {
    int iReturn = 0;
    try
    {
      String stTemp = rsSchedule.getString("nmToId");
      if (stTemp != null && stTemp.length() > 0 && !stTemp.equals("0"))
      {
        iReturn = addSuccessor(rsSchedule.getInt("nmToId"));
        aSuccessorLag[iReturn] = rsSchedule.getDouble("nmPercent");
        aSuccessorType[iReturn] = rsSchedule.getString("stComment");
      }
    } catch (Exception e)
    {
      this.stError += "<BR>EROR addCp " + e;
    }
    return iReturn;
  }

  public int workDaysBetween(Calendar startDate, Calendar endDate)
  {
    Calendar date = null;
    Calendar dtStart2 = null;
    Calendar dtEnd2 = null;

    if (startDate != null)
      dtStart2 = startDate;
    else
      dtStart2 = Calendar.getInstance();

    if (endDate != null)
      dtEnd2 = endDate;
    else
      dtEnd2 = Calendar.getInstance();

    date = (Calendar) dtStart2.clone();
    int daysBetween = 0;
    if (date.after(dtEnd2))
    {
      while (date.after(dtEnd2))
      {
        date.add(Calendar.DAY_OF_MONTH, -1);
        if (this.checkHoliday(date) <= 0)
          daysBetween--;
      }
    } else if (date.before(dtEnd2))
    {
      while (date.before(dtEnd2))
      {
        date.add(Calendar.DAY_OF_MONTH, 1);
        if (this.checkHoliday(date) <= 0)
          daysBetween++;
      }
    }
    return daysBetween;
  }

  public double getMaxEffort(double dEffort, String stValue)
  {
    double dEffortReturn = dEffort;
    double dMax = 0;
    String[] aRecords = null;
    String[] aFields = null;
    int iRecMax;
    try
    {
      if (stValue.length() > 0)
      {
        aRecords = stValue.split("\\|", -1);
        iRecMax = aRecords.length;
      } else
      {
        iRecMax = 0;
      }
      for (int iR = 0; iR < iRecMax; iR++)
      {
        aFields = aRecords[iR].split("~", -1); // LcId, MaxEmployess, Effort,
        try
        {
          int iMaxEmp = Integer.parseInt(aFields[1]);
          double dMaxEffort = Double.parseDouble(aFields[2]);
          if ((dMaxEffort / iMaxEmp) > dMax)
            dMax = (dMaxEffort / iMaxEmp);
        } catch (Exception e)
        {
        }
      }
      dEffortReturn = dMax;
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR getMaxEffort " + e;
    }
    return dEffortReturn;
  }
}
