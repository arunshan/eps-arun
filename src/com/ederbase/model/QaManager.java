package com.ederbase.model;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;
import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
import org.jawin.DispatchPtr;

public class QaManager
{
  EbEnterprise ebEnt = null;
  public String stError = "";
  private int nmTestSeq = 0;
  private int indentLevel = 0;

  public QaManager(EbEnterprise ebEnt)
  {
    this.ebEnt = ebEnt;
  }

  public String getQaAdmin(EbEnterprise ebEnt)
  {
    this.ebEnt = ebEnt;

    String stReturn = getTop();
    stReturn = stReturn + "<table width='100%'>";
    stReturn = stReturn + "<tr><th colspan=3 align=center>";
    String stSql = "";
    int nmSubId = 0;
    int iId = 0;
    String stTemp = "";
    try
    {
      nmSubId = Integer.parseInt(this.ebEnt.ebUd.request.getParameter("c"));
    }
    catch (Exception e) {
      nmSubId = 1;
    }
    try
    {
      switch (nmSubId)
      {
      case 1:
        String[] aProc = ebEnt.stUserParams.split("\\|");
        File path = new File(aProc[1]);
        syncMacros(path);
        stSql = "SELECT * FROM myqaproc order by nmOrder, RecId desc";
        stReturn = stReturn + "</form><form method=post><table border=1><tr><th>Test ID</th><th>Type</th><th>Status</th><th>Title</th><th>Procedure</th><th>EPPORA/EPS Component</th><th>Order</th><th>Expected Result</th></tr>";

        break;
      case 2:
        stSql = "SELECT * FROM myqatestgroup order by nmTestGroupId desc";
        stReturn = stReturn + "</form><form method=post><table border=1><tr><th>Group ID</th><th>Type</th><th>Status</th><th>Title</th><th>List of tests (top down)</th></tr>";

        break;
      case 3:
        iId = Integer.parseInt(this.ebEnt.ebUd.request.getParameter("id"));
        stTemp = this.ebEnt.ebUd.request.getParameter("Save");
        if ((stTemp != null) && (stTemp.length() > 0))
        {
          if (iId == -1)
          {
            iId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from myqaproc");
          }
          stSql = "update myqaproc set nmType=" + this.ebEnt.ebUd.request.getParameter("nmType") + ",nmFlags=" + this.ebEnt.ebUd.request.getParameter("nmFlags") + ",nmOrder=" + this.ebEnt.ebUd.request.getParameter("nmOrder") + ",stTitle=" + this.ebEnt.dbEnterprise.fmtDbString(this.ebEnt.ebUd.request.getParameter("stTitle")) + ",stAutoProc=" + this.ebEnt.dbEnterprise.fmtDbString(this.ebEnt.ebUd.request.getParameter("stAutoProc")) + ",stMainTestComponent=" + this.ebEnt.dbEnterprise.fmtDbString(this.ebEnt.ebUd.request.getParameter("stMainTestComponent")) + ",stDescription=" + this.ebEnt.dbEnterprise.fmtDbString(this.ebEnt.ebUd.request.getParameter("stDescription")) + ",stExpectedResult=" + this.ebEnt.dbEnterprise.fmtDbString(this.ebEnt.ebUd.request.getParameter("stExpectedResult")) + "where RecId=" + iId;

          this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
          nmSubId = 1;
        }
        else {
          stTemp = this.ebEnt.ebUd.request.getParameter("Delete");
          if ((stTemp != null) && (stTemp.length() > 0))
          {
            nmSubId = 1;
            this.ebEnt.dbEnterprise.ExecuteUpdate("delete FROM myqaproc where RecId=" + iId);
          }
        }
        if (nmSubId == 1)
        {
          stSql = "SELECT * FROM myqaproc order by nmOrder, RecId desc";
          stReturn = stReturn + "</form><form method=post><table border=1><tr><th>Test ID</th><th>Type</th><th>Status</th><th>Title</th><th>Procedure</th><th>Test Component</th><th>Order</th><th>Expected Result</th></tr>";
        }
        else
        {
          if (iId == -1)
          {
            iId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from myqaproc");
            iId++;
            this.ebEnt.dbEnterprise.ExecuteUpdate("insert into myqaproc (RecId) values(" + iId + ") ");
          }
          stSql = "SELECT * FROM myqaproc where RecId=" + iId;
        }
        break;
      case 4:
        iId = Integer.parseInt(this.ebEnt.ebUd.request.getParameter("id"));
        stTemp = this.ebEnt.ebUd.request.getParameter("Save");
        if ((stTemp != null) && (stTemp.length() > 0))
        {
          if (iId == -1)
          {
            iId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(nmTestGroupId) from myqatestgroup");
          }
          stSql = "update myqatestgroup set nmType=" + this.ebEnt.ebUd.request.getParameter("nmType") + ",nmFlags=" + this.ebEnt.ebUd.request.getParameter("nmFlags") + ",stTitle=" + this.ebEnt.dbEnterprise.fmtDbString(this.ebEnt.ebUd.request.getParameter("stTitle")) + ",stTests=" + this.ebEnt.dbEnterprise.fmtDbString(this.ebEnt.ebUd.request.getParameter("stTests")) + "where nmTestGroupId=" + iId;

          this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
          nmSubId = 2;
        }
        else {
          stTemp = this.ebEnt.ebUd.request.getParameter("Delete");
          if ((stTemp != null) && (stTemp.length() > 0))
          {
            nmSubId = 2;
            this.ebEnt.dbEnterprise.ExecuteUpdate("delete FROM myqatestgroup where nmTestGroupId=" + iId);
          }
        }
        if (nmSubId == 2)
        {
          stSql = "SELECT * FROM myqatestgroup order by nmTestGroupId desc";
          stReturn = stReturn + "</form><form method=post><table border=1><tr><th>Group ID</th><th>Type</th><th>Status</th><th>Title</th><th>List of tests (top down)</th></tr>";
        }
        else
        {
          if (iId == -1)
          {
            iId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(nmTestGroupId) from myqatestgroup");
            iId++;
            this.ebEnt.dbEnterprise.ExecuteUpdate("insert into myqatestgroup (nmTestGroupId) values(" + iId + ") ");
          }
          stSql = "SELECT * FROM myqatestgroup where nmTestGroupId=" + iId;
        }
      }

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        for (int iR = 1; iR <= iMax; iR++)
        {
          rs.absolute(iR);
          switch (nmSubId)
          {
          case 1:
            stReturn = stReturn + "<tr>";
            stReturn = stReturn + "<td><a href='./?b=2002&c=3&id=" + rs.getString("RecId") + "'>" + rs.getString("RecId") + "</a></td>";
            stReturn = stReturn + "<td>" + getOptions(0, "nmType", rs.getString("nmType")) + "</td>";
            stReturn = stReturn + "<td>" + getOptions(0, "nmFlags", rs.getString("nmFlags")) + "</td>";
            stReturn = stReturn + "<td>" + rs.getString("stTitle") + "</td>";
            stReturn = stReturn + "<td>" + rs.getString("stAutoProc") + "</td>";
            stReturn = stReturn + "<td>" + rs.getString("stMainTestComponent") + "</td>";
            stReturn = stReturn + "<td>" + rs.getString("nmOrder") + "</td>";
            stReturn = stReturn + "<td>" + rs.getString("stExpectedResult") + "</td>";
            stReturn = stReturn + "</tr>";
            break;
          case 2:
            stReturn = stReturn + "<tr>";
            stReturn = stReturn + "<td valign=top align=right><a href='./?b=2002&c=4&id=" + rs.getString("nmTestGroupId") + "'>" + rs.getString("nmTestGroupId") + "</a></td>";
            stReturn = stReturn + "<td valign=top>" + getOptions(0, "nmType", rs.getString("nmType")) + "</td>";
            stReturn = stReturn + "<td valign=top>" + getOptions(0, "nmFlags", rs.getString("nmFlags")) + "</td>";
            stReturn = stReturn + "<td valign=top>" + rs.getString("stTitle") + "</td>";
            stReturn = stReturn + "<td class=tiny valign=top>" + rs.getString("stTests").replace("\n", "<br>") + "</td>";
            stReturn = stReturn + "</tr>";
            break;
          case 3:
            stReturn = stReturn + "</form><form method=post><tr><th align=right>Test ID: </th><td align=left>" + rs.getString("RecId") + "</td></tr>";
            stReturn = stReturn + simpleEditField(1, 5, 5, 0, "nmOrder", rs, "Sequence");
            stReturn = stReturn + simpleEditField(9, 0, 0, 0, "nmType", rs, "Type");
            stReturn = stReturn + simpleEditField(9, 0, 0, 0, "nmFlags", rs, "Status");
            stReturn = stReturn + simpleEditField(3, 80, 255, 0, "stTitle", rs, "Title");
            stReturn = stReturn + simpleEditField(3, 20, 255, 0, "stAutoProc", rs, "Procedure");
            stReturn = stReturn + simpleEditField(9, 0, 0, 0, "stMainTestComponent", rs, "Main Test Component");
            stReturn = stReturn + simpleEditField(4, 80, 0, 10, "stDescription", rs, "Description");
            stReturn = stReturn + simpleEditField(4, 80, 0, 10, "stExpectedResult", rs, "Expected Result");
            stReturn = stReturn + "<tr><td colspan=2 align=center><input type=submit name=Save value=Save> <input type=submit name=Delete value=Delete></form></td></tr>";
            break;
          case 4:
            stReturn = stReturn + "</form><form method=post><tr><th align=right>GROUP ID: </th><td align=left>" + rs.getString("nmTestGroupId") + "</td></tr>";
            stReturn = stReturn + simpleEditField(9, 0, 0, 0, "nmType", rs, "Type");
            stReturn = stReturn + simpleEditField(9, 0, 0, 0, "nmFlags", rs, "Status");
            stReturn = stReturn + simpleEditField(3, 80, 255, 0, "stTitle", rs, "Title");
            stReturn = stReturn + simpleEditField(4, 120, 0, 10, "stTests", rs, "List of Tests");
            stReturn = stReturn + "<tr><td colspan=2 align=center><input type=submit name=Save value=Save> <input type=submit name=Delete value=Delete></form></td></tr>";
          }
        }
      }

      stReturn = stReturn + "</table>";
      if (nmSubId == 1)
      {
        stReturn = stReturn + "<p align=center><a href='./?b=2002&c=3&id=-1'>ADD NEW TEST PROCEDURE</a></p>";
      }
      if (nmSubId == 2)
      {
        stReturn = stReturn + "<p align=center><a href='./?b=2002&c=4&id=-1'>ADD NEW TEST GROUP</a></p>";
      }
      if (nmSubId == 4)
      {
        stSql = "SELECT * FROM myqaproc where nmType in (21) and nmFlags = 1 order by nmOrder,RecId";
        rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
        rs.last();
        int iMax = rs.getRow();
        stReturn = stReturn + "<table border=0 width='100%' align=center><tr><h2>Logins</h2><td valign=top nowrap><textarea name=login cols=120 rows=" + iMax + ">";

        for (int iR = 1; iR <= iMax; iR++)
        {
          rs.absolute(iR);
          stReturn = stReturn + "test~" + rs.getString("RecId") + "~" + rs.getString("stAutoProc") + "~" + rs.getString("stTitle") + "\n";
        }
        stSql = "SELECT * FROM myqaproc where nmType in (20) and nmFlags = 1 order by nmOrder,RecId";
        rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
        rs.last();
        iMax = rs.getRow();
        stReturn = stReturn + "</textarea></td></tr><tr><td valign=top nowrap><h2>Test Procedures</h2><textarea name=proc cols=120 rows=" + iMax + ">";
        for (int iR = 1; iR <= iMax; iR++)
        {
          rs.absolute(iR);
          stReturn = stReturn + "test~" + rs.getString("RecId") + "~" + rs.getString("stAutoProc") + "~" + rs.getString("stTitle") + "\n";
        }
        stSql = "SELECT * FROM myqatestgroup where nmFlags = 1 order by nmTestGroupId";
        rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
        rs.last();
        iMax = rs.getRow();
        stReturn = stReturn + "</textarea></td></tr><tr><td valign=top nowrap><h2>Test GROUPS</h2><textarea name=group cols=120 rows=" + iMax + ">";
        for (int iR = 1; iR <= iMax; iR++)
        {
          rs.absolute(iR);
          stReturn = stReturn + "group~" + rs.getString("nmTestGroupId") + "~n/a~" + rs.getString("stTitle") + "\n";
        }
        stReturn = stReturn + "</textarea></td></tr></table>";
      }
    }
    catch (Exception e)
    {
      stReturn = stReturn + "<BR>ERROR: getQaAdmin " + e;
    }
    return stReturn;
  }

  public String simpleEditField(int nmDataType, int nmCols, int nmMaxBytes, int nmRows, String stFieldName, ResultSet rsValue, String stLabel)
  {
    String stEdit = "<tr><th align=right valign=top>" + stLabel + ": </th><td align=left valign=top>";
    try
    {
      String stValue = rsValue.getString(stFieldName);
      if (stValue == null)
      {
        stValue = "";
      }

      switch (nmDataType)
      {
      case 9:
        stEdit = stEdit + getOptions(1, stFieldName, stValue);
        break;
      case 4:
        stEdit = stEdit + "<textarea name=" + stFieldName + " id=" + stFieldName + " rows=" + nmRows + " cols=" + nmCols + ">" + stValue + "</textarea>";
        break;
      default:
        stEdit = stEdit + "<input type=text name=" + stFieldName + " id=" + stFieldName + " value=\"" + stValue + "\" size=" + nmCols + " maxlength=" + nmMaxBytes + ">";
      }
    }
    catch (Exception e)
    {
      stEdit = stEdit + "<BR>ERROR: simpleEditField " + e;
    }
    stEdit = stEdit + "</td></tr>";
    return stEdit;
  }

  public String getOptions(int iEdit, String stFieldName, String stValue)
  {
    String stEdit = "<select name=" + stFieldName + " id=" + stFieldName;
    if (iEdit == 0)
    {
      stEdit = stEdit + " disabled ";
    }
    stEdit = stEdit + ">";

    if (stFieldName.equals("nmType"))
    {
      stEdit = stEdit + addOption("iMacro Procedure", "20", stValue);
      stEdit = stEdit + addOption("iMacro LOGIN", "21", stValue);
      stEdit = stEdit + addOption("Other", "1", stValue);
    } else if (stFieldName.equals("nmFlags"))
    {
      stEdit = stEdit + addOption("Enabled", "1", stValue);
      stEdit = stEdit + addOption("Disabled", "0", stValue);
    } else if (stFieldName.equals("stMainTestComponent"))
    {
      try
      {
        stEdit = stEdit + addOption("Other", "Other", stValue);
        stEdit = stEdit + addOption("Login", "Login", stValue);
        stEdit = stEdit + addOption("Home", "Home", stValue);
        stEdit = stEdit + addOption("Reports", "Reports", stValue);
        stEdit = stEdit + addOption("Project", "Project", stValue);
        stEdit = stEdit + addOption("Administration", "Administration", stValue);
        stEdit = stEdit + addOption("Help", "Help", stValue);
      }
      catch (Exception e) {
      }
    }
    else {
      stEdit = stEdit + "TODO";
    }
    stEdit = stEdit + "</select>";
    return stEdit;
  }

  public String addOption(String stLabel, String stId, String stCurrent)
  {
    if (stId.equals(stCurrent))
    {
      return "<option value=\"" + stId + "\" selected>" + stLabel + "</option>";
    }

    return "<option value=\"" + stId + "\">" + stLabel + "</option>";
  }

  public String getQaManager(EbEnterprise ebEnt)
  {
    this.ebEnt = ebEnt;

    String stReturn = getTop();
    stReturn = stReturn + "<table width='100%'>";
    stReturn = stReturn + "<tr><th colspan=3 align=center>";
    String stSql = "SELECT count(*) as cnt, nmTestId, dtRunTime,sum(nmPassFail) as pass FROM myqatestrun where nmQaProcId > 0 group by nmTestId order by nmTestId desc";
    int nmTestId = 0;
    try
    {
      nmTestId = Integer.parseInt(this.ebEnt.ebUd.request.getParameter("c"));
    }
    catch (Exception e)
    {
    }
    try {
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        String stReturn2 = "<table border=1><tr><th>Test RUN</th><th>Run Time</th><th># of TESTS</th><th>PASS/FAIL</th><th># Pass</th><th># Fail</th></tr>";
        String stReturn1 = "";
        String stBg = "";
        String stPassFail = "";
        for (int iR = 1; iR <= iMax; iR++)
        {
          rs.absolute(iR);
          if (nmTestId <= 0)
          {
            nmTestId = rs.getInt("nmTestId");
          }
          if (nmTestId == rs.getInt("nmTestId"))
          {
            stReturn1 = stReturn1 + "<h2>Test Details for TEST RUN: " + nmTestId;

            stReturn1 = stReturn1 + "<table border=1><tr><th colspan=2>Test ID</th><th>Run Time</th><th>Title</th><th>Procedure</th><th>Pass/Fail</th><th>Result</th><th>Variables / Error</th></tr>";
            stSql = "select * from myqatestrun r left join myqaproc p on r.nmQaProcId=p.RecId where r.nmTestId=" + nmTestId + " order by nmTestSeq";
            ResultSet rs1 = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
            if (rs1 != null)
            {
              rs1.last();
              int iMax1 = rs1.getRow();
              int iR1 = 1;
              rs1.absolute(iR1);
              if (rs1.getInt("nmQaProcId") == 0)
              {
                stReturn1 = stReturn1 + " [" + rs1.getInt("nmResult") + "] " + this.ebEnt.dbEnterprise.ExecuteSql1(new StringBuilder().append("select stTitle from myqatestgroup where nmTestGroupId=").append(rs1.getInt("nmResult")).toString());
                iR1++;
              }

              stReturn1 = stReturn1 + "</h2>";
              for (; iR1 <= iMax1; iR1++)
              {
                rs1.absolute(iR1);
                stReturn1 = stReturn1 + "<tr>";
                stReturn1 = stReturn1 + "<td align=right>" + rs1.getString("nmTestSeq") + "</a></td>";
                stReturn1 = stReturn1 + "<td align=right>" + rs1.getString("nmQaProcId") + "</a></td>";
                stReturn1 = stReturn1 + "<td align=left>" + rs1.getString("dtRunTime") + "</td>";
                stReturn1 = stReturn1 + "<td align=left>" + rs1.getString("stTitle") + "</td>";
                stReturn1 = stReturn1 + "<td align=left>" + rs1.getString("stAutoProc") + "</td>";
                if (rs1.getInt("nmResult") <= 0)
                {
                  stBg = "red";
                  stPassFail = "FAIL";
                }
                else {
                  stBg = "lightgreen";
                  stPassFail = "PASS";
                }
                stReturn1 = stReturn1 + "<td align=left bgcolor=" + stBg + ">" + stPassFail + "</td>";
                stReturn1 = stReturn1 + "<td align=right bgcolor=" + stBg + ">" + rs1.getString("nmResult") + "</a></td>";
                stReturn1 = stReturn1 + "<td align=left bgcolor=" + stBg + ">" + rs1.getString("stError") + "</a></td>";
                stReturn1 = stReturn1 + "</tr>";
              }
            }
          }
          stReturn2 = stReturn2 + "<tr>";
          stReturn2 = stReturn2 + "<td align=right><a href='./?b=2001&c=" + rs.getString("nmTestId") + "'>" + rs.getString("nmTestId") + "</a></td>";
          stReturn2 = stReturn2 + "<td align=left>" + rs.getString("dtRunTime") + "</td>";
          stReturn2 = stReturn2 + "<td align=right>" + rs.getString("cnt") + "</td>";
          int iFail = rs.getInt("cnt") - rs.getInt("pass");
          if (iFail > 0)
          {
            stBg = "red";
            stPassFail = "FAIL";
          }
          else {
            stBg = "lightgreen";
            stPassFail = "PASS";
          }
          stReturn2 = stReturn2 + "<td align=left bgcolor=" + stBg + ">" + stPassFail + "</td>";
          stReturn2 = stReturn2 + "<td align=right bgcolor=" + stBg + ">" + rs.getString("pass") + "</td>";
          stReturn2 = stReturn2 + "<td align=right bgcolor=" + stBg + ">" + iFail + "</td>";
          stReturn2 = stReturn2 + "</tr>";
        }
        stReturn1 = stReturn1 + "</table><h2>Test History</h2>";
        stReturn2 = stReturn2 + "</table>";

        stReturn = stReturn + stReturn1 + stReturn2 + "</th></tr></table>";
      }
    }
    catch (Exception e) {
    }
    stReturn = stReturn + "</table>";
    return stReturn;
  }

  public String getTop()
  {
    String stHTML = "<html><head><style type='text/css'>*{font-size: 10pt;font-family: Verdana, sans-serif;}h1{font-size: 18pt;FONT-WEIGHT: bold;}h2{font-size: 15pt;FONT-WEIGHT: bold;}.tiny {font-size: 7pt;} </style></head><body>";

    stHTML = stHTML + "\n<table width='100%' border=0 cellpadding=3 cellspacing=2 bgcolor=yellow><tr><th colspan=3 align=center><h1>EPPORA/EPS - Qualtiy Assurance</h1></th></tr><tr><td><a href='./?b=2002&c=1'>Administer Test Procedures</a></td>";

    stHTML = stHTML + "<td><a href='./?b=2002&c=2'>Administer Test GROUPS</a></td>";
    stHTML = stHTML + "<td><a href='./?b=2001'>View Test Results</a></td>";
    stHTML = stHTML + "</tr></table><br>";
    return stHTML;
  }

  public String qaProcess(String stId, DispatchPtr app32, ActiveXComponent app64, int nmQaProcId)
  {
    String stHTML = "<br>qaProcess " + stId;
    String stSql = "";
    long lTime1b = 0L;
    long lTime1c = 0L;
    int iReturn = 0;
    String stResult = "";
    String stErr = "";
    Object oReturn = null;
    Calendar cal1 = null;
    String[] aProc = this.ebEnt.stUserParams.split("\\|");
    stSql = "SELECT * FROM myqatestgroup where nmTestGroupId=" + stId;
    try
    {
      ResultSet rs2 = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      rs2.absolute(1);

      String[] stTestRun = rs2.getString("stTests").trim().split("\n");

      for (int iR = 0; iR < stTestRun.length; iR++)
      {
        this.stError = "";
        cal1 = Calendar.getInstance();
        lTime1b = cal1.getTimeInMillis();
        String[] aV = stTestRun[iR].split("~");

        if (aV[0].toLowerCase().equals("wait"))
        {
          long lSleep = Long.parseLong(aV[1].trim());
          Thread.sleep(lSleep);
        } else if (aV[0].toLowerCase().equals("group"))
        {
          stHTML = stHTML + qaProcess(aV[1], app32, app64, nmQaProcId); } else {
          if ((!aV[0].toLowerCase().equals("test")) && (!aV[0].toLowerCase().equals("csvloop")))
            continue;
          ResultSet rs1 = this.ebEnt.dbEnterprise.ExecuteSql("SELECT * FROM myqaproc where RecId=" + aV[1]);
          rs1.absolute(1);

          int nmPassFail = 0;
          int iMax = 1;
          ResultSet rsLoop = null;
          int numberOfColumns = 0;
          ResultSetMetaData rsMetaData = null;
          if (aV[0].toLowerCase().equals("csvloop"))
          {
            rsLoop = this.ebEnt.dbEnterprise.ExecuteSql(aV[3]);
            rsLoop.last();
            iMax = rsLoop.getRow();
            rsMetaData = rsLoop.getMetaData();
            numberOfColumns = rsMetaData.getColumnCount();
          }
          for (int iLoop = 1; iLoop <= iMax; iLoop++)
          {
            stErr = "";
            if (rsLoop != null)
            {
              rsLoop.absolute(iLoop);
              for (int iC = 1; iC <= numberOfColumns; iC++)
              {
                if (aProc[0].equals("32bit"))
                {
                  oReturn = app32.invoke("iimSet", rsMetaData.getColumnName(iC), rsLoop.getString(iC));
                  iReturn = ((Integer)oReturn).intValue();
                }
                else {
                  Variant v1 = new Variant(rsMetaData.getColumnName(iC));
                  Variant v2 = new Variant(rsLoop.getString(iC));
                  if (iC < 3)
                  {
                    stErr = stErr + rsLoop.getString(iC) + ", ";
                  }
                  oReturn = app64.invoke("iimSet", new Variant[] { v1, v2 });
                  iReturn = Integer.parseInt(oReturn.toString());
                }
              }
            }
            if (aProc[0].equals("32bit"))
            {
              oReturn = app32.invoke("iimPlay", aProc[1] + rs1.getString("stAutoProc"));
              iReturn = ((Integer)oReturn).intValue();
            }
            else {
              oReturn = app64.invoke("iimPlay", aProc[1] + rs1.getString("stAutoProc"));
              iReturn = Integer.parseInt(oReturn.toString());
            }
            if (iReturn < 0)
            {
              if (aProc[0].equals("32bit"))
              {
                stErr = app32.invoke("iimGetLastError").toString();
              }
              else {
                stErr = app64.invoke("iimGetLastError").toString();
              }
            }
            if (aProc[0].equals("32bit"))
            {
              stResult = app32.invoke("iimGetLastExtract").toString();
            }
            else
              stResult = app64.invoke("iimGetLastExtract").toString();
            int i;
            if (stResult == null)
            {
              stResult = "";
            }
            else {
              stResult = stResult.replace("[EXTRACT]", "~").trim();
              i = stResult.length();
            }
            cal1 = Calendar.getInstance();
            lTime1c = cal1.getTimeInMillis();
            nmPassFail = 0;
            if (iReturn > 0)
            {
              nmPassFail = 1;

              String stExpectedResult = rs1.getString("stExpectedResult");
              if ((stExpectedResult == null) || (stExpectedResult.length() == 0))
              {
                stExpectedResult = "~";
              }
              if (!stResult.trim().equals(stExpectedResult.trim()))
              {
                nmPassFail = 0;
                iReturn = -123456;
                this.stError += "RESULT(s) DID NOT MATCH ";
              }
            }
            this.nmTestSeq += 1;
            stSql = "insert into myqatestrun values(" + nmQaProcId + "," + this.nmTestSeq + "," + rs1.getInt("RecId") + ",now()," + (lTime1c - lTime1b) + "," + iReturn + "," + this.ebEnt.dbEnterprise.fmtDbString(stResult) + "," + this.ebEnt.dbEnterprise.fmtDbString(new StringBuilder().append(stErr).append(this.stError.trim()).toString()) + "," + nmPassFail + ")";
            this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
          }
        }
      }
    }
    catch (Exception e) {
      stHTML = stHTML + "<hr>ERROR qaProcess: " + e;
    }
    return stHTML;
  }

  private void syncMacros(File path)
  {
    this.indentLevel += 1;
    int iRecId = 0;
    String stSql = "";
    int iType = 0;
    File[] files = path.listFiles();

    Arrays.sort(files);
    int i = 0; for (int n = files.length; i < n; i++)
    {
      if (files[i].isDirectory())
      {
        syncMacros(files[i]);
      }
      else {
        if ((files[i].getName().equals("#Current.iim")) || (!files[i].getName().endsWith(".iim")))
          continue;
        stSql = "select RecId from myqaproc where  stAutoProc=" + this.ebEnt.dbEnterprise.fmtDbString(files[i].getName());

        iRecId = this.ebEnt.dbEnterprise.ExecuteSql1n(stSql);
        if (iRecId > 0)
          continue;
        iRecId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from myqaproc ");
        iRecId++;
        if (files[i].getName().toLowerCase().contains("login"))
          iType = 21;
        else
          iType = 20;
        stSql = "insert into myqaproc (RecId,nmType,nmFlags,stTitle,stAutoProc) values(" + iRecId + "," + iType + ",1" + "," + this.ebEnt.dbEnterprise.fmtDbString(files[i].getName()) + "," + this.ebEnt.dbEnterprise.fmtDbString(files[i].getName()) + ")";

        this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
      }

    }

    this.indentLevel -= 1;
  }
}