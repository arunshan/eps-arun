/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eps.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.Vector;

import com.ederbase.model.EbEnterprise;

/**
 *
 * @author guest1

 */
public class EpsReport
{

  private String stError = "";
  private String stPageTitle = "";
  private int nmCustomReportId = 0;
  private EpsUserData epsUd = null;

  public void EpsReport()
  {
    this.stError = "";
  }

  public void setUd(EpsUserData epsUd, int nmCustomReportId)
  {
    this.epsUd = epsUd;
    this.nmCustomReportId = nmCustomReportId;
  }

  public String doReport(ResultSet rsTable, EpsUserData epsUd)
  {
    this.epsUd = epsUd;
    String stReturn = "";
    try
    {
      String stSubmit = epsUd.ebEnt.ebUd.request.getParameter("submit2");
      String stValue = epsUd.ebEnt.ebUd.request.getParameter("customreport");

      String stReportId = epsUd.ebEnt.ebUd.request.getParameter("reportid");
      if (stReportId != null && stReportId.length() > 0)
      {
        return epsFormatSavedReports(stReportId, this.epsUd.ebEnt);
      }
      if (stValue != null)
        nmCustomReportId = Integer.parseInt(stValue);

      if (stSubmit != null && stSubmit.length() > 0)
      {
        if (stSubmit.startsWith("Custom"))
        {
          return customReportDesigner(rsTable);
        } else if (stSubmit.startsWith("Run"))
        {
          return runReport(rsTable);
        } else if (stSubmit.startsWith("View"))
        {
          return viewReport(rsTable);
        } else
        {
          epsUd.ebEnt.ebUd.setRedirect("./");
          return ""; //----------------------------->
        }
      }
      stReturn += "<center><form method=post><h1>" + rsTable.getString("stTableName") + "</h1><table>";
      ResultSet rsR = epsUd.ebEnt.dbDyn.ExecuteSql("SELECT * FROM teb_customreport where stReportType = '" + rsTable.getString("nmTableId") + "' order by stReportName ");
      rsR.last();
      int iMaxR = rsR.getRow();
      if (iMaxR <= 0)
      {
        // create default Report
        int i = epsUd.ebEnt.dbDyn.ExecuteSql1n("select max(RecId) from teb_customreport");
        i++;
        epsUd.ebEnt.dbDyn.ExecuteUpdate("replace into teb_customreport (RecId,stReportType,stReportName,nmReportFlags)"
          + " values( " + i + ",'" + rsTable.getString("nmTableId") + "',\"" + rsTable.getString("stTableName") + "\",1)");
        rsR = epsUd.ebEnt.dbDyn.ExecuteSql("SELECT * FROM teb_customreport where stReportType = '" + rsTable.getString("nmTableId") + "' order by stReportName ");
        rsR.last();
        iMaxR = rsR.getRow();
      }
      if (iMaxR <= 0)
      {
        this.stError += "<BR>ERROR2 doReport iMax = 0 ";
      } else
      {
        stReturn += "<tr><td>Select:</td><td><select name=customreport>";
        for (int iR = 1; iR <= iMaxR; iR++)
        {
          rsR.absolute(iR);
          stReturn += epsUd.ebEnt.ebUd.addOption2(rsR.getString("stReportName"), rsR.getString("RecId"), "");
        }
        stReturn += epsUd.ebEnt.ebUd.addOption2("--Create New Custom Report--", "-1", "");
        stReturn += "</select></td></tr>";
        stReturn += "<tr>";
        stReturn += "<td colspan=2 align=center><input type=submit name=submit2 value='View Saved Reports'>&nbsp;&nbsp;&nbsp;";
        stReturn += "<input type=submit name=submit2 value='Run/Execute Report'>&nbsp;&nbsp;&nbsp;";
        stReturn += "<input type=submit name=submit2 value='Custom Report Designer'>&nbsp;&nbsp;&nbsp;";
        stReturn += "<input type=submit name=submit2 value='Cancel'></form>";
        stReturn += "</td></tr>";
      }
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR doReport " + e;
    }
    stReturn += "</table>";
    return stReturn;
  }

  public String getError()
  {
    return this.stError;
  }

  /* AS -- 2Oct2011 -- Issue#9*/
  public String epsShowSavedReports(ResultSet rsTable)
  {
    String stReturn = "";
    try
    {
    	// todo
    	/* AS -- 29Sept2011 -- Issue #9*/
      //String stSql = "select r.*,cr.stReportType,cr.stReportName from teb_reports r, teb_customreport cr where r.nmCustomReportId=cr.RecId order by r.RecId Desc";
    	String stSql = "select r.*,cr.stReportType,cr.stReportName from teb_reports r, teb_customreport cr where r.nmCustomReportId=cr.RecId and cr.stReportType="+ rsTable.getInt("nmTableId") +" order by r.RecId Desc";
      ResultSet rs = epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        String stClass = "style='background-color:yellow;font-size:12px;padding:0px 10px 0px 10px;'";
        //<th " + stClass + ">Report Type</th>
        stReturn += "<table border=0 bgcolor=white cellspacing=1 cellpadding=2>"
          + "<tr>"
          + "<th " + stClass + ">Report Title</th><th " + stClass + ">Run Time</th>"
          + "<th " + stClass + " colspan=2>View Report Format</th><th " + stClass + ">&nbsp;</th></tr>";
        //<th " + stClass + " colspan=2>Download Report</th>
        stClass = "style='background-color:white;font-size:10px;padding:0px 10px 0px 10px;'";
        for (int iR = 1; iR <= iMax; iR++)
        {
          rs.absolute(iR);
          stReturn += "<tr>";
          //stReturn += "<td " + stClass + ">" + rs.getString("stReportType") + "</td>";
          stReturn += "<td " + stClass + ">" + rs.getString("stReportName") + "</td>";
          stReturn += "<td " + stClass + ">" + rs.getString("dtRun") + "</td>";
          stReturn += "<td " + stClass + "><a target=_blank href='./?stAction=reports&reportid=" + rs.getString("RecId") + "&format=html'>HTML</a></td>";
          stReturn += "<td " + stClass + "><a href='./?stAction=reports&reportid=" + rs.getString("RecId") + "&format=excel'>Excel</a></td>";
          stReturn += "<td " + stClass + "><a href='./?stAction=reports&reportid=" + rs.getString("RecId") + "&format=delete'  onclick=\"return confirm('Are you sure you want to delete?')\">Delete</a></td>";
          stReturn += "</tr>";
        }
        stReturn += "</table>";
      }
    } catch (Exception e)
    {
      stError += "<br>ERROR: epsShowSavedReports " + e;
    }
    return stReturn;
  }

  public String epsFormatSavedReports(String stReportId, EbEnterprise ebEnt)
  {
    String stReturn = "";
    String stOut = "";
    try
    {
      String stTemp = ebEnt.ebUd.request.getParameter("format");

      if (stTemp != null && stTemp.equals("delete"))
      {
        ebEnt.dbDyn.ExecuteUpdate("delete from teb_reports where RecId=" + stReportId);
        ebEnt.ebUd.setRedirect("./?stAction=reports&t=71&submit2=View");
        return ""; //----------------------------->
      }

      String stSql = "select * from teb_reports r, teb_customreport cr where r.nmCustomReportId=cr.RecId and r.RecId= " + stReportId;
      ResultSet rs = ebEnt.dbDyn.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.absolute(1);
        String[] aLines = rs.getString("stReportRaw").split("\\|", -1);
        
        //project name
        //get filtered projects
        String pIDs = "";
        String prjName = "";
        String[] prjArr = null;
    	prjArr = rs.getString("prjFilter").split(",");
    	if(prjArr != null && prjArr.length > 0){
    		for(int i=0; i<prjArr.length; i++){
    			if(!prjArr[i].equals(""))
    				pIDs += "RecId = " + prjArr[i] + " or ";
        	}
        	if(!pIDs.equals("")){
        		pIDs = " where " + pIDs.substring(0, pIDs.length()-4);
        	}
    	}
        ResultSet rsPID = ebEnt.dbDyn.ExecuteSql("select ProjectName from projects" + pIDs);
        while(rsPID.next()){
        	prjName += rsPID.getString("ProjectName") + ", ";
        }
        if(!prjName.equals("")){
    		prjName = prjName.substring(0, prjName.length()-2);
    	}
        
        //stPageTitle = rs.getString("stReportType") + " Report &nbsp;&nbsp;&nbsp; Title: <b>" + rs.getString("stReportName") + "</b> Run: " + rs.getString("dtRun");
        stPageTitle = "<b>" + rs.getString("stReportName") + "</b> &nbsp;&nbsp;<i>Run: " + rs.getString("dtRun") + "</i>";
        stReturn += "<center><table border=1>";
        String[] aTdClass = null;
        ResultSet rs2 = ebEnt.dbDyn.ExecuteSql("select * from teb_reportcolumns rc,teb_fields f, teb_epsfields ef"
          + " where rc.nmFieldId=f.nmForeignId and f.nmForeignId=ef.nmForeignId"
          + " and stShow='Y' and nmCustomReportId=" + rs.getString("nmCustomReportId") + " order by rc.nmOrder");
        rs2.last();
        int i2 = rs2.getRow();
        for (int iL = 0; iL < aLines.length; iL++)
        {
          String[] aFields = aLines[iL].trim().split("~", -1);
          if (iL == 0)
          {
            aTdClass = new String[aFields.length + 1];
            aTdClass[0] = "";
            for (int iF = 1; iF < aFields.length && iF <= i2; iF++)
            {
              aTdClass[iF] = "";
              rs2.absolute(iF);
              if (rs2.getString("stClass").length() > 0 && !rs2.getString("stClass").equals("null"))
                aTdClass[iF] += " class=\"" + rs2.getString("stClass") + "\" ";
              if (rs2.getString("stCustom").length() > 0 && !rs2.getString("stCustom").equals("null"))
                aTdClass[iF] += " style=\"" + rs2.getString("stCustom") + "\" ";
              if (aTdClass[iF].length() <= 0)
              {
                /* AS -- 19Oct2011 -- Issue # 55 */
          	  /*if (rs2.getInt("nmDataType") == 1 || rs2.getInt("nmDataType") == 5 || rs2.getInt("nmDataType") == 31)
                {
                  if (rs2.getString("stHandler").length() <= 0)
                    aTdClass[iF] = " align=right ";
                }*/
                if (rs2.getInt("nmDataType") == 1  || rs2.getInt("nmDataType") == 5 || rs2.getInt("nmDataType") == 31)
                {
                  if (rs2.getString("stHandler").length() <= 0)
                    aTdClass[iF] = " align=right ";
                }
              }
            }
            
            /* AS -- 19Oct2011 -- Issue # 55 */
            //stReturn += "<tr><th colspan=" + (aFields.length - 1) + " align=center>" + stPageTitle + "</th></tr>";
            stReturn += "<tr><th colspan=" + (aFields.length - 1) + " align=center STYLE='font-size: 18px; background-color: yellow;'>" + stPageTitle + "</th></tr>";
            
            //display project filters if any projects are filtered
            if(rs.getString("prjFilter") != ""){
            	stReturn += "<tr><th colspan=" + (aFields.length - 1) + " align='left'>Project " + rs.getString("stReportName") + " Report for project " + prjName + "</th></tr>";
            }
          }
          stReturn += "<tr>";
          //~Project Name^123~Synergy With Organization^1088~Synergy With Other Projects^1089~Total Ranking Score^998|~EPS Office Building~27~24~3370|~EPPORA Training Sessions~21~21~3179|~EPPORA SQL Server Conversion Suite~18~18~2950|~EPPORA Source Analysis~15~15~2749|~EPPORA Resource Modeling~12~12~2546|~EPPORA Project Templates~9~9~2365|~EPPORA Project Manager Scheduling Enhancer~24~27~2192|~EPPORA Primavera Conversion Suite~6~6~2022|~EPPORA Performance Enhancer~3~3~1871|~EPPORA Oracle Conversion Suite~6~6~1833|~EPPORA Online Training~9~9~1761|~EPPORA Niku Workbench Conversion Suite~12~12~1655|~EPPORA MySQL Conversion Suite~15~15~1511|~EPPORA Microsoft Project Conversion Suite~30~0~1337|~EPPORA Implementation~27~27~1148|~EPPORA Estimation Analyzer~18~18~852|~EPPORA DB2 Conversion Suite~21~21~650|~EPPORA Comprehensive Project Test Tool~24~24~418|~EPPORA Automated Test Suite~18~21~156|
          for (int iF = 1; iF < aFields.length && iF <= i2; iF++)
          {
            if (iL == 0)
            {
              String[] aV = aFields[iF].trim().split("\\^", -1);
              /* AS -- 19Oct2011 -- Issue # 55 */
              //stReturn += "<th>" + aV[0] + "</td>";
              stReturn += "<th STYLE='font-size: 12px; background-color:yellow;'>" + aV[0] + "</td>";
            } else
              stReturn += "<td " + aTdClass[iF] + ">" + aFields[iF].replace("`", "<br>") + "</td>";
          }
          stReturn += "</tr>";
        }
        stOut = "<html><head><title>" + stPageTitle + "</title><style type='text/css'>";
        stOut += rs.getString("stReportCss");
        stOut += "</style></head><body id='ebbody'>";
        stReturn += "</table></center></body></html>";
      }
    } catch (Exception e)
    {
      stError += "<br>ERROR: epsFormatSavedReports " + e;
    }
    return stOut + stReturn;
  }

  public String getMyParameter(String stName)
  {
    String stValue = epsUd.ebEnt.ebUd.request.getParameter(stName);
    if (stValue == null)
      stValue = "";
    stValue = stValue.trim();
    return stValue;
  }

  public String customReportDesigner(ResultSet rsTable)
  {
    String stReturn = "";
    String stPopupError = "";
    String stSql = "";
    int iF = 0;
    int giSubmitId = 0;
    try
    {
      String stSubmit = epsUd.ebEnt.ebUd.request.getParameter("savedata");
      String stTemp = epsUd.ebEnt.ebUd.request.getParameter("giSubmitId");
      if (stTemp != null && stTemp.length() > 0)
        giSubmitId = Integer.parseInt(stTemp);
      if (nmCustomReportId <= 0)
      {
        nmCustomReportId = epsUd.ebEnt.dbDyn.ExecuteSql1n("select max(RecId) from teb_customreport");
        nmCustomReportId++;
        epsUd.ebEnt.dbDyn.ExecuteUpdate("replace into teb_customreport (RecId,stReportName,stReportType, prjFilter) values "
          + "(" + nmCustomReportId + ",\"New " + rsTable.getString("stTableName") + " - " + nmCustomReportId + "\",\"" + rsTable.getString("nmTableId") + "\", \"\") ");
      }
      ResultSet rsCr = epsUd.ebEnt.dbDyn.ExecuteSql("SELECT * FROM teb_customreport where RecId=" + nmCustomReportId);
      rsCr.absolute(1);

      // Housekeeping, check if field has been stored.
      switch (rsTable.getInt("nmTableId"))
      {

        case 72: // Projects
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " f.nmForeignId in (116,1029,120,1025,1027,1028,112,1029,1030,114,115,127,1031,1023,47,805,1024,765,766)"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 0 ) order by f.stLabel";
          break;
        case 51: // Budget Report
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (51) and (f.nmFlags & 0x400) = 0) or f.nmForeignId in (116,765,766) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 0 ) order by f.stLabel";
          break;
        case 53: // Cost Effectiveness  133,82.494
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (53) and (f.nmFlags & 0x400) = 0) or f.nmForeignId in (133,82,494,95) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 0 ) order by f.stLabel";
          break;
        case 55: // Criteria
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and ((f.nmTabId in (27) and (nmFlags & 0x400) = 0)"
            + " or f.nmForeignID in ( 880 )) and rc.nmFieldId is null order by f.stLabel";
          break;
        case 56: // Division
          /* AS -- 19Oct2011 -- Issue # 61 */
      	/*stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
          + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
          + " where f.nmForeignId=ef.nmForeignId and f.nmTabId in (10) and (nmFlags & 0x400) = 0"
          + " and rc.nmFieldId is null and f.nmForeignID not in ( 0 ) order by f.stLabel";*/
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and f.nmTabId in (10) and ((nmFlags & 0x400) = 0 or nmFlags = 1024) "
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 0 ) order by f.stLabel";
          break;
        case 57: // Inventory
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and f.nmTabId in (18,880,570) and (nmFlags & 0x400) = 0"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 0 ) order by f.stLabel";
          break;
        case 61: // Labor Category
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (14,610) and (f.nmFlags & 0x400) = 0)  )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 0 ) order by f.stLabel";
          break;
        case 65: // Missing Inventory
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId "
            + " and rc.nmFieldId is null and f.nmForeignID in ( 28,208,116,32,829 ) order by f.stLabel";
          break;
        case 67: // Missing Labor Category
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (67) and (f.nmFlags & 0x400) = 0) or f.nmForeignId in (133,116) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 41) order by f.stLabel";
          break;
        case 76: // Project Requirement
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (19,76) and (f.nmFlags & 0x400) = 0) or f.nmForeignId in (116) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 0) order by f.stLabel";
          break;
        case 81: // Project Schedule
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (21) and (f.nmFlags & 0x400) = 0) or f.nmForeignId in (116) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 807,808) order by f.stLabel";
          break;
        case 74: // Project Team Assignment
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (12,74) and (f.nmFlags & 0x400) = 0) or f.nmForeignId in (0) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 0) order by f.stLabel";
          break;
        case 78: // Project Requirements Analysis
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (19) and (f.nmFlags & 0x400) = 0) or f.nmForeignId in (116,43,1008) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 231) order by f.stLabel";
          break;
        case 83: // Project Schedule Analysis
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " f.nmForeignId in (271,829,258,814,116,45,1008)"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 0) order by f.stLabel";
          break;
        case 69: // Productivity
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( f.nmForeignId in (82,494,40,903,904,906,133) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 0) order by f.stLabel";
          break;
        case 80: // Project Resource Allocation
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (12,80) and (f.nmFlags & 0x400) = 0) or f.nmForeignId in (0) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 0) order by f.stLabel";
          break;
        case 59: // Issues
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (16,870) and (f.nmFlags & 0x400) = 0) or f.nmForeignId in (82,494) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 11,171,172,169 ) order by f.stLabel";
          break;
        case 63: // Log/Audit
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (63) and (f.nmFlags & 0x400) = 0) or f.nmForeignId in (82,494) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 0) order by f.stLabel";
          break;

        case 87: // Triggers
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (16,870) and (f.nmFlags & 0x400) = 0) or f.nmForeignId in (82,494) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 11,171,172,169 ) order by f.stLabel";

          break;
        case 89: // Users
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and f.nmTabId in (9,49,380) and (nmFlags & 0x400) = 0"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 84,79 ) order by f.stLabel";
          stSql = "select * from teb_fields f, teb_epsfields ef left join teb_reportcolumns rc"
            + " on ef.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmForeignId=ef.nmForeignId and"
            + " ( (f.nmTabId in (9,49,380) and (f.nmFlags & 0x400) = 0) or f.nmForeignId in (0) )"
            + " and rc.nmFieldId is null and f.nmForeignID not in ( 84,79,78,76 ) order by f.stLabel";
          break;

        case 71: // Project Ranking
          stSql = "select * from teb_fields f left join teb_reportcolumns rc"
            + " on f.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where  ( f.nmTabId=" + rsTable.getString("nmTableId") + " or "
            + " (f.nmFlags & 0x10000000 ) != 0 ) and rc.nmFieldId is null "
            + " order by f.stLabel";
          break;
        default:
          stSql = "select * from teb_fields f left join teb_reportcolumns rc"
            + " on f.nmForeignId=rc.nmFieldId and rc.nmCustomReportId = " + nmCustomReportId
            + " where f.nmTabId=" + rsTable.getString("nmTableId") + " and rc.nmFieldId is null "
            + " and ( f.nmFlags & 0x400 ) = 0 order by f.stLabel";
      }
      ResultSet rsF = epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
      rsF.last();
      int iMaxF = rsF.getRow();
      for (iF = 1; iF <= iMaxF; iF++)
      {
        rsF.absolute(iF);
        epsUd.ebEnt.dbDyn.ExecuteUpdate("replace into teb_reportcolumns (nmCustomReportId,nmFieldId,nmWidth,nmOrder) "
          + "values(" + nmCustomReportId + ",\"" + rsF.getString("nmForeignId") + "\"," + rsF.getString("nmCols") + "," + iF + ") ");
      }
      if (giSubmitId > 0 && giSubmitId < 2000)
      {
        // Need to reorder.
        int iOrig = epsUd.ebEnt.dbDyn.ExecuteSql1n("select nmOrder from teb_reportcolumns "
          + "where nmCustomReportId=" + nmCustomReportId + " and nmFieldId=" + giSubmitId);
        stTemp = epsUd.ebEnt.ebUd.request.getParameter("order_" + giSubmitId);
        int iNew = Integer.parseInt(stTemp);
        if (iNew < iOrig)
        { // 1 2 3 4 5 -> 3 1 2 4 5
          epsUd.ebEnt.dbDyn.ExecuteUpdate("update teb_reportcolumns set nmOrder = ( nmOrder + 1 )"
            + " where nmOrder >= " + iNew + " and nmOrder <= " + iOrig + " and nmCustomReportId=" + nmCustomReportId);
        } else
        {
          epsUd.ebEnt.dbDyn.ExecuteUpdate("update teb_reportcolumns set nmOrder = ( nmOrder - 1 )"
            + " where nmOrder >= " + iOrig + " and nmOrder <= " + iNew + " and nmCustomReportId=" + nmCustomReportId);
        }
        epsUd.ebEnt.dbDyn.ExecuteUpdate("update teb_reportcolumns set nmOrder = " + iNew
          + " where nmFieldId = " + giSubmitId + " and nmCustomReportId=" + nmCustomReportId);
      }
      
      switch (rsTable.getInt("nmTableId")){
  		case 76:	//project requirements omit project name
  			 rsF = epsUd.ebEnt.dbDyn.ExecuteSql("select rc.*,f.stLabel from teb_reportcolumns rc, teb_fields f "
		        + " where rc.nmFieldId=f.nmForeignId"
		        + " and rc.nmCustomReportId=" + nmCustomReportId + " and rc.nmFieldId <> 116 order by rc.stShow desc, rc.nmOrder asc ");
  			break;
  		case 78:	//project requirements analysis omit project name
 			 rsF = epsUd.ebEnt.dbDyn.ExecuteSql("select rc.*,f.stLabel from teb_reportcolumns rc, teb_fields f "
		        + " where rc.nmFieldId=f.nmForeignId"
		        + " and rc.nmCustomReportId=" + nmCustomReportId + " and rc.nmFieldId <> 116 order by rc.stShow desc, rc.nmOrder asc ");
 			break;
  		case 81:	//project schedule omit project name
  			rsF = epsUd.ebEnt.dbDyn.ExecuteSql("select rc.*,f.stLabel from teb_reportcolumns rc, teb_fields f "
		        + " where rc.nmFieldId=f.nmForeignId"
		        + " and rc.nmCustomReportId=" + nmCustomReportId + " and rc.nmFieldId <> 116 order by rc.stShow desc, rc.nmOrder asc ");
  			break;
  		default:
  			 rsF = epsUd.ebEnt.dbDyn.ExecuteSql("select rc.*,f.stLabel from teb_reportcolumns rc, teb_fields f "
		        + " where rc.nmFieldId=f.nmForeignId"
		        + " and rc.nmCustomReportId=" + nmCustomReportId + " order by rc.stShow desc, rc.nmOrder asc ");
      }
     
      rsF.last();
      iMaxF = rsF.getRow();

      if ((stSubmit != null && stSubmit.length() > 0) || (giSubmitId > 0 && giSubmitId < 2000))
      {
        for (iF = 1; iF <= iMaxF; iF++)
        {
          rsF.absolute(iF);
          int nmFieldId = rsF.getInt("nmFieldId");
          String stShow = epsUd.ebEnt.ebUd.request.getParameter("show_" + nmFieldId);
          if (stShow == null)
            stShow = "N";
          else
            stShow = "Y";
          
          stSql = "update teb_reportcolumns set stShow = \"" + stShow + "\""
            + ",stClass = \"" + epsUd.ebEnt.ebUd.request.getParameter("class_" + nmFieldId) + "\""
            + ",stCustom = \"" + epsUd.ebEnt.ebUd.request.getParameter("custom_" + nmFieldId) + "\""
            + ",stShort = \"" + epsUd.ebEnt.ebUd.request.getParameter("short_" + nmFieldId) + "\"";
          if (epsUd.ebEnt.ebUd.request.getParameter("width_" + nmFieldId) != null
            && epsUd.ebEnt.ebUd.request.getParameter("width_" + nmFieldId).length() > 0)
            stSql += ",nmWidth = " + epsUd.ebEnt.ebUd.request.getParameter("width_" + nmFieldId);
          stSql += " where nmFieldId = " + nmFieldId + " and nmCustomReportId=" + nmCustomReportId;
          epsUd.ebEnt.dbDyn.ExecuteUpdate(stSql);
        }
        if (stSubmit != null)
        {
          if (stSubmit.equals("Save"))
          {
            if (nmCustomReportId > 3)
            {
              stPopupError += epsUd.validateD22("stReportName", rsCr, "stReportName", "teb_customreport", "RecId");
              if (stPopupError.length() <= 0)
              {
                String stFilter = this.epsUd.userSearch(rsTable, "Save");
                
                //update project filters
                String[] aV = epsUd.ebEnt.ebUd.request.getParameterValues("fprojects_selected");
                String prjFilter = "";
                if(aV != null && aV.length > 0){
	                for(int i=0; i<aV.length; i++){
	                	prjFilter += aV[i]+",";
	                }
	                prjFilter = prjFilter.substring(0, prjFilter.length() - 1);
                }
                epsUd.ebEnt.dbDyn.ExecuteUpdate("update teb_customreport set "
                  + " stReportName = \"" + epsUd.ebEnt.ebUd.request.getParameter("stReportName") + "\""
                  + ",stFilter = " + epsUd.ebEnt.dbDyn.fmtDbString(stFilter)
                  + ",stReportCss = " + epsUd.ebEnt.dbDyn.fmtDbString(epsUd.ebEnt.ebUd.request.getParameter("stReportCss"))
                  + ",prjFilter = " + epsUd.ebEnt.dbDyn.fmtDbString(prjFilter)
                  + " where RecId = " + nmCustomReportId);
                epsUd.ebEnt.ebUd.setRedirect("./?stAction=reports&t=" + rsTable.getString("nmTableId"));
                return ""; //----------------------------->
              }
            } else
              return ""; //----------------------------->
          } else if (stSubmit.equals("Cancel"))
          {
            epsUd.ebEnt.ebUd.setRedirect("./?stAction=reports&t=" + rsTable.getString("nmTableId"));
            return "";
          }
        }
        
        switch (rsTable.getInt("nmTableId")){
        	case 76:	//project requirements omit project name
        		rsF = epsUd.ebEnt.dbDyn.ExecuteSql("select rc.*,f.stLabel from teb_reportcolumns rc, teb_fields f "
  		          + " where rc.nmFieldId=f.nmForeignId"
  		          + " and rc.nmCustomReportId=" + nmCustomReportId + " and rc.nmFieldId != 116 order by rc.stShow desc, rc.nmOrder asc ");
        		break;
        	case 78:	//project requirements analysis omit project name
    			 rsF = epsUd.ebEnt.dbDyn.ExecuteSql("select rc.*,f.stLabel from teb_reportcolumns rc, teb_fields f "
   		        + " where rc.nmFieldId=f.nmForeignId"
   		        + " and rc.nmCustomReportId=" + nmCustomReportId + " and rc.nmFieldId <> 116 order by rc.stShow desc, rc.nmOrder asc ");
    			break;
        	case 81:	//project schedule omit project name
      			rsF = epsUd.ebEnt.dbDyn.ExecuteSql("select rc.*,f.stLabel from teb_reportcolumns rc, teb_fields f "
    		        + " where rc.nmFieldId=f.nmForeignId"
    		        + " and rc.nmCustomReportId=" + nmCustomReportId + " and rc.nmFieldId <> 116 order by rc.stShow desc, rc.nmOrder asc ");
      			break;
        	default: 
        		rsF = epsUd.ebEnt.dbDyn.ExecuteSql("select rc.*,f.stLabel from teb_reportcolumns rc, teb_fields f "
		          + " where rc.nmFieldId=f.nmForeignId"
		          + " and rc.nmCustomReportId=" + nmCustomReportId + " order by rc.stShow desc, rc.nmOrder asc ");
        }
      }

      stReturn = "\n</form><form method=post name='form" + rsTable.getInt("nmTableId") + "' id='form" + rsTable.getInt("nmTableId") + "'>"
        + "<input type=hidden name=giSubmitId id=giSubmitId value=''>"
        + "<input type=hidden name=submit2 id=submit2 value='" + epsUd.ebEnt.ebUd.request.getParameter("submit2") + "'>"
        + "<input type=hidden name=customreport id=customreport value='" + nmCustomReportId + "'>";
      if (nmCustomReportId > 3)
      {
        stReturn += "<h2 style=\"font-size: 14pt;\">Report Content</h2>"
          + "Report Title: <input type=text name=stReportName "
          + "value=\"" + epsUd.ebEnt.ebUd.getMyDb("stReportName", rsCr, "stReportName") + "\" size=64>";
      }
      stReturn += "<br/>&nbsp;"
        + "<font color=red>" + stPopupError + "</font><br/><table border=1 style=\"background-color:white\">";
      /* AS -- 19Oct2011 -- Issue # 64 */
      stReturn += "<tr><th>Order</th><th align=left>Field</th><th>Show <input type=checkbox onclick='checkAllShows(this);' /></th>";
      if (nmCustomReportId > 3)
        stReturn += "<th>CSS Format</td>";
      else
        stReturn += "<th>Width %</th>";
      stReturn += "<th>Short Label</th></tr>";

      String stChecked = "";
      /* AS -- 19Oct2011 -- Issue # 64 */
      Vector<Integer> vecshow = new Vector<Integer>();
      for (iF = 1; iF <= iMaxF; iF++)
      {
        rsF.absolute(iF);
        int nmFieldId = rsF.getInt("nmFieldId");
    
        stSql = "update teb_reportcolumns set nmOrder= " + iF
          + " where nmCustomReportId=" + nmCustomReportId + " and nmFieldId=\"" + nmFieldId + "\"";
        epsUd.ebEnt.dbDyn.ExecuteUpdate(stSql); // Just for safety sakes.  Should be ok though.

        stReturn += "<tr>";

        stReturn += "\n<td align=center>" + this.orderSelect("order_" + nmFieldId, iF, iMaxF, rsTable.getInt("nmTableId"), nmFieldId) + "</td>";
        stReturn += "<td align=left>" + rsF.getString("stLabel") + "</td>";
        String stShow = rsF.getString("stShow");
        if (stShow.equals("Y"))
        {
          stChecked = " checked ";
        } else
        {
          stChecked = "";
        }
        stReturn += "<td align=center><input type=checkbox id=show_"+ nmFieldId +" name=show_" + nmFieldId + " value=" + nmFieldId + " " + stChecked + "></td>";
        /* AS -- 19Oct2011 -- Issue # 64 */
        vecshow.add(nmFieldId);
        if (nmCustomReportId <= 3)
          stReturn += "<td align=left><input type=text name=width_" + nmFieldId + " value=\"" + epsUd.ebEnt.ebUd.getMyDb("width_" + nmFieldId, rsF, "nmWidth") + "\" size=3 style=\"text-align:right\" ></td>";
        //stReturn += "<td align=left>" + epsUd.ebEnt.ebUd.getClass(nmFieldId, epsUd.ebEnt.ebUd.getMyDb("class_" + nmFieldId, rsF, "stClass")) + "</td>";
        else
          stReturn += "<td align=left><input type=text name=custom_" + nmFieldId + " value=\"" + epsUd.ebEnt.ebUd.getMyDb("custom_" + nmFieldId, rsF, "stCustom") + "\" size=40 ></td>";
        stReturn += "<td align=left><input type=text name=short_" + nmFieldId + " value=\"" + epsUd.ebEnt.ebUd.getMyDb("short_" + nmFieldId, rsF, "stShort") + "\" size=15 ></td>";
        stReturn += "</tr>";
      }
      // Special Report Handler for FILTERS
      stReturn += "</td></tr></table>";
      
      /* Start of Change AS -- 19Oct2011 -- Issue # 64 */
      String strjsbuilder = "";
      strjsbuilder += "<script language='javascript'>";
      strjsbuilder += "function checkAllShows(elem){";
      strjsbuilder += "if(elem.checked == true){";
      for(int checkcount=0;checkcount<vecshow.size();checkcount++){
    	  strjsbuilder += "document.getElementById('show_"+ vecshow.get(checkcount) +"').checked = true;";
      }
      strjsbuilder += "} else{";
      for(int checkcount=0;checkcount<vecshow.size();checkcount++){
    	  strjsbuilder += "document.getElementById('show_"+ vecshow.get(checkcount) +"').checked = false;";
      }
      strjsbuilder += "}}";
      strjsbuilder += "</script>";
      
      
      /* End of Change AS -- 19Oct2011 -- Issue # 64 */
      
      String stFilter = epsUd.ebEnt.dbDyn.ExecuteSql1("select stFilter from teb_customreport where RecId=" + nmCustomReportId);
      if (stFilter == null || stFilter.trim().length() <= 0)
        stFilter = "";

      switch (rsTable.getInt("nmTableId"))
      {
        case 53: // Cost Effectiveness
        /* Start of Change AS -- 12Oct2011 -- Issue#50*/
        case 55:
          stReturn += "<br>&nbsp;<br>" + this.epsUd.divisionFilter(rsTable, stFilter);
          break;
        /* End of Change AS -- 12Oct2011 -- Issue#50*/
          
        /* Start of Change AS -- 4Oct2011 -- Issue#19*/
        case 56:
          stReturn += "<br>&nbsp;<br>" + this.epsUd.divisionFilter(rsTable, stFilter);
          break;
        /* End of Change AS -- 4Oct2011 -- Issue#19*/
        /* Start of Change AS -- 12Oct2011 -- Issue#43*/
        case 69: // Prooductivity Report
          stReturn += "<br>&nbsp;<br>" + this.epsUd.userSearch(rsTable, stFilter);
          break;
        /* End of Change AS -- 12Oct2011 -- Issue#43*/
        case 89: // Users
          stReturn += "<br>&nbsp;<br>" + this.epsUd.userSearch(rsTable, stFilter);
          break;
        case 81: // Project Schedule users
          stReturn += this.epsUd.epsEf.selectProjects(rsTable, nmCustomReportId);
          stReturn += "<br>&nbsp;<br>" + this.epsUd.userSearch(rsTable, stFilter);
          break;
        case 76: //Project Requirements project filter
          stReturn += this.epsUd.epsEf.selectProjects(rsTable, nmCustomReportId);
          break;
        case 78: //Project Requirements Analysis project filter
            stReturn += this.epsUd.epsEf.selectProjects(rsTable, nmCustomReportId);
            break;
        case 72: //Projects Report project filter
          stReturn += this.epsUd.epsEf.selectProjects(rsTable, nmCustomReportId);
          break;
        case 83: //Project schedule analysis filter
          stReturn += this.epsUd.epsEf.selectProjects(rsTable, nmCustomReportId);
          break;
      }
      String stCss = epsUd.ebEnt.dbDyn.ExecuteSql1("select stReportCss from teb_customreport where RecId=" + nmCustomReportId);
      if (stCss == null || stCss.trim().length() <= 0 && !stCss.equals("null"))
        stCss = "*{font-family:verdana,arial, helvetica, sans-serif;font-size: 8.5px;}";

      stReturn += "<table>";
      if (nmCustomReportId > 3)
        stReturn += "<tr><th valign=top>Cascading<BR>Style<br>Sheet<br>(CSS)</td><td valign=top><textarea name=stReportCss rows=10 cols=100>"
          + stCss + "</textarea></td></tr>";
      
      if(rsTable.getInt("nmTableId") == 76){		//project requirement submit
    	  stReturn += "<tr><td align=center colspan=2>"
	        + "<input type=submit name=savedata value='Save' onclick='selectAllOptions(document.getElementById(\"fprojects_selected\"))'>&nbsp;&nbsp;&nbsp;"
	        + "<input type=submit name=savedata value='Cancel'>"
	        + "</td></tr></table>";
      }else if(rsTable.getInt("nmTableId") == 81){		//project schedule submit
    	  stReturn += "<tr><td align=center colspan=2>"
    		+ "<input type=submit name=savedata value='Save' onclick='selectAllOptions(document.getElementById(\"fprojects_selected\"))'>&nbsp;&nbsp;&nbsp;"
	        + "<input type=submit name=savedata value='Cancel'>"
	        + "</td></tr></table>";
	  }else if(rsTable.getInt("nmTableId") == 72){		//projects report submit
    	  stReturn += "<tr><td align=center colspan=2>"
      		+ "<input type=submit name=savedata value='Save' onclick='selectAllOptions(document.getElementById(\"fprojects_selected\"))'>&nbsp;&nbsp;&nbsp;"
  	        + "<input type=submit name=savedata value='Cancel'>"
  	        + "</td></tr></table>";
  	  }else if(rsTable.getInt("nmTableId") == 78){		//project requirement analysis report submit
    	  stReturn += "<tr><td align=center colspan=2>"
    		+ "<input type=submit name=savedata value='Save' onclick='selectAllOptions(document.getElementById(\"fprojects_selected\"))'>&nbsp;&nbsp;&nbsp;"
	        + "<input type=submit name=savedata value='Cancel'>"
	        + "</td></tr></table>";
	  }else if(rsTable.getInt("nmTableId") == 83){		//projects schedule analysis submit
  		  stReturn += "<tr><td align=center colspan=2>"
    		+ "<input type=submit name=savedata value='Save' onclick='selectAllOptions(document.getElementById(\"fprojects_selected\"))'>&nbsp;&nbsp;&nbsp;"
	        + "<input type=submit name=savedata value='Cancel'>"
	        + "</td></tr></table>";
	  }else{
		  stReturn += "<tr><td align=center colspan=2>"
	        + "<input type=submit name=savedata value='Save'>&nbsp;&nbsp;&nbsp;"
	        + "<input type=submit name=savedata value='Cancel'>"
	        + "</td></tr></table>";  
      }
      
      stReturn += strjsbuilder;
      
    } catch (Exception e)
    {
      stError += "<BR>ERROR customReportDesigner " + e;
    }
    return stReturn;
  }

  public String orderSelect(String stName, int iC, int iLength, int nmTableId, int iField)
  {
    String stReturn = "<select name=\"" + stName + "\" id=\"" + stName + "\" onChange=\"setSubmitId(" + iField + ");document.form" + nmTableId + ".submit();\">";
    String stChecked = "";
    for (int i = 1; i <= iLength; i++)
    {
      if (i == iC)
      {
        stChecked = " selected ";
      } else
      {
        stChecked = "";
      }
      stReturn += "<option value=\"" + i + "\" " + stChecked + " size=3 style=\"text-align:right\" >" + i + "</option>";
    }
    stReturn += "</select>";
    return stReturn;
  }

  private String runReport(ResultSet rsTable)
  {
    String stReturn = " running ";
    String stSql = "";
    String stReport = "";
    if (nmCustomReportId > 0)
    {
      try
      {
        ResultSet rsR = null;
        int iMaxR = 0;
        ResultSet rsF = epsUd.ebEnt.dbDyn.ExecuteSql("select rc.*,f.*,ef.stHandler"
          + " from teb_reportcolumns rc, teb_fields f, teb_epsfields ef "
          + " where f.nmForeignId=ef.nmForeignId and rc.nmFieldId=f.nmForeignId and rc.stShow='Y' "
          + " and rc.nmCustomReportId=" + nmCustomReportId + " order by rc.nmOrder asc ");
        rsF.last();
        int iMaxF = rsF.getRow();
        if (iMaxF <= 0)
          return customReportDesigner(rsTable); //--------------> Must slect fields
        String stFilter = epsUd.ebEnt.dbDyn.ExecuteSql1("select stFilter from teb_customreport where RecId=" + nmCustomReportId);
        String prjFilter = epsUd.ebEnt.dbDyn.ExecuteSql1("select prjFilter from teb_customreport where RecId=" + nmCustomReportId);
        String[] prjArr = null;
        String pIDs = "";
        switch (rsTable.getInt("nmTableId"))
        {
          case 76: // Project Requirement
        	//get filtered projects
        	prjArr = prjFilter.split(",");
        	if(prjArr != null && prjArr.length > 0){
        		for(int i=0; i<prjArr.length; i++){
        			if(!prjArr[i].equals(""))
        				pIDs += "p.RecId = " + prjArr[i] + " or ";
            	}
            	if(!pIDs.equals("")){
            		pIDs = " and (" + pIDs.substring(0, pIDs.length()-4) + ")";
            	}
        	}

            stSql = "select p.ProjectName,r.*, count(*) cnt, sum(l.nmPercent) nmPercent"
              + " from Projects p, Requirements r left join teb_link l"
              + " on l.nmLinkFlags=1 and r.nmProjectId=l.nmProjectId and r.nmBaseline=l.nmBaseline and r.RecId=l.nmFromId"
              + " where r.nmProjectId=p.RecId and r.nmBaseline=p.CurrentBaseline" + pIDs
              + "  group by p.ProjectName,r.RecId order by p.ProjectName,r.ReqId";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 81: // Project Schedule
            /*stSql = "select p.ProjectName,s.*, count(*) cnt, sum(l.nmPercent) nmPercent"
            + " from Projects p, Schedule s left join teb_link l"
            + " on l.nmLinkFlags=1 and s.nmProjectId=l.nmProjectId and s.nmBaseline=l.nmBaseline and s.RecId=l.nmFromId"
            + " where s.nmProjectId=p.RecId and s.nmBaseline=p.CurrentBaseline"
            + " group by p.ProjectName,s.RecId order by p.ProjectName,s.SchId";*/
        	  
        	/*
        	stSql = "select p.ProjectName,s.* from Projects p, Schedule s"
              + " where s.nmProjectId=p.RecId and s.nmBaseline=p.CurrentBaseline"
              + " group by p.ProjectName,s.RecId order by p.ProjectName,s.SchId";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
        	*/
        	
        	//get filtered projects
        	if(prjFilter != null && prjFilter.length() > 0){
	          	prjArr = prjFilter.split(",");
	          	if(prjArr != null && prjArr.length > 0){
	          		for(int i=0; i<prjArr.length; i++){
	          			if(!prjArr[i].equals(""))
	          				pIDs += "p.RecId = " + prjArr[i] + " or ";
	              	}
	              	if(!pIDs.equals("")){
	              		pIDs = " and (" + pIDs.substring(0, pIDs.length()-4) + ")";
	              	}
	          	}
        	}
            
        	//get users from filter
            if (stFilter != null && stFilter.length() > 7)
            { //128^,3,,4,,6,^,3,,1,^full^^beg
              String[] aV = stFilter.split("\\^", -1);
              int nmType = Integer.parseInt(aV[0]);
              stSql = this.epsUd.makeUserSql(nmType, aV[1], aV[2], aV[3], aV[4], aV[5]);
            } else
            {
              stSql = this.epsUd.makeUserSql(0xFFFF, ",0", ",0,", "full", "", "beg"); // Do all
            }
            stSql += " order by LastName,FirstName,stEMail";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            int[] userArr = new int[iMaxR];
            String[] aFields = null;
            String[] lbusrs = null;
            String stlb = "";
            
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              userArr[iR-1] = rsR.getInt("nmUserId"); 

            }
            
            //get labor category field from all tasks so we can parse users
        	stSql = "select p.ProjectName,s.* from Projects p, Schedule s"
              + " where s.nmProjectId=p.RecId and s.nmBaseline=p.CurrentBaseline" + pIDs
              + " group by p.ProjectName,s.RecId order by p.ProjectName,s.SchId";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stlb = rsR.getString("SchLaborCategories");
              if(!stlb.equals("")){
            	  aFields = stlb.split("~", -1);
            	  if(!aFields[3].equals("")){
	            	  lbusrs = aFields[3].split(",");
	            	  for(int j=0; j<lbusrs.length; j++){
	            		  //check if this user is filtered
            	          for (int k = 0; k < userArr.length; k++)
            	          {
            	        	 if(Integer.parseInt(lbusrs[j]) == userArr[k]){
            	        		 stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            	        	 }
            	          }
	            	  }
            	  }
              }
            }
            break;
          case 74: // Project Team Assignment
            stSql += "SELECT p.ProjectName,u.FirstName,u.LastName,lc.LaborCategory,ap.*,s.*"
              + " FROM teb_allocateprj ap, Users u, Projects p, Schedule s, LaborCategory lc"
              + " where ap.nmPrjId=p.RecId and ap.nmUserId=u.nmUserId and ap.nmTaskId=s.RecId"
              + " and s.nmProjectId=ap.nmPrjId and s.nmBaseline=p.CurrentBaseline and ap.nmLc=lc.nmLcId"
              + " order by p.ProjectName, u.LastName, u.FirstName, ap.dtDatePrj";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 78: // Project Requirements Analysis
        	//get filtered projects
          	prjArr = prjFilter.split(",");
          	if(prjArr != null && prjArr.length > 0){
          		for(int i=0; i<prjArr.length; i++){
          			if(!prjArr[i].equals(""))
          				pIDs += "p.RecId = " + prjArr[i] + " or ";
              	}
              	if(!pIDs.equals("")){
              		pIDs = " and (" + pIDs.substring(0, pIDs.length()-4) + ")";
              	}
          	}
        	  
            stSql += "select p.ProjectName,r.*, '' as SuggestedMitigationStrategy from Requirements r, Projects p"
              + " where r.nmProjectId=p.RecId and r.nmBaseline=p.CurrentBaseline" + pIDs
              + " and r.nmD50Flags != 0 and (ReqFlags & 0x10) != 0"
              + " order by p.ProjectName, r.ReqId;";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 83: // Project Schedule Analysis
        	//get filtered projects
        	prjArr = prjFilter.split(",");
        	if(prjArr != null && prjArr.length > 0){
        		for(int i=0; i<prjArr.length; i++){
        			if(!prjArr[i].equals(""))
        				pIDs += "p.RecId = " + prjArr[i] + " or ";
            	}
            	if(!pIDs.equals("")){
            		pIDs = " and (" + pIDs.substring(0, pIDs.length()-4) + ")";
            	}
        	}
        	  
            stSql += "select p.ProjectName,s.*, '' as SuggestedMitigationStrategy from Schedule s, Projects p"
              + " where s.nmProjectId=p.RecId and s.nmBaseline=p.CurrentBaseline" + pIDs
              + " and s.nmD53Flags != 0 and (SchFlags & 0x10) != 0"
              + " order by p.ProjectName, s.SchId";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 69: // Productivity
            //u.nmUserId in (" + stSqlUser + ") and
        	  /* Start of Issue AS -- 12Oct2011 -- Issue # 43 */
        	  String stSqlUser1 = "";
              if (stFilter != null && stFilter.length() > 7)
              { //128^,3,,4,,6,^,3,,1,^full^^beg
                String[] aV = stFilter.split("\\^", -1);
                int nmType = Integer.parseInt(aV[0]);
                stSqlUser1 = this.epsUd.makeUserSql(nmType, aV[1], aV[2], aV[3], aV[4], aV[5]);
              } else
              {
                stSqlUser1 = this.epsUd.makeUserSql(0xFFFF, ",0", ",0,", "full", "", "beg"); // Do all
              }
              stSqlUser1 = stSqlUser1.replace("u.*,xu.stEMail,xu.nmPriviledge,xu.RecId", "u.nmUserId");
  
        	/* End of Issue AS -- 12Oct2011 -- Issue # 43 */ 
            stSql += "SELECT u.FirstName,u.LastName,rlc.*,lc.LaborCategory FROM Users u, LaborCategory lc, teb_reflaborcategory rlc"
              + " where u.nmUserId=rlc.nmRefId and rlc.nmRefType=42 and rlc.nmLaborCategoryId=lc.nmLcId"
              + " order by u.LastName,u.FirstName, lc.LaborCategory";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 80: // Project Resource Allocation
            stSql += "SELECT * FROM Budget_Report br, Projects p where p.RecId=br.RecId order by p.ProjectName;";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 63: // Log/Audit
            stSql += "select distinct a.nmTableId,a.RecId as LogId,"
              + " concat('Accessing: ', t.stTableName) as LogEventType,a.nmUserId, concat('') as LogNote,"
              + " dtEventStartTime as LogDateTime,nmPk,nmProject,u.FirstName,u.LastName"
              + " from ebeps01.X25AuditTrail a, Users u, teb_table t"
              + " where t.nmTableId=a.nmTableId and a.nmUserId=u.nmUserId order by dtEventStartTime desc limit 2000";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;

          //------------
          case 72: // Projets
        	//get filtered projects
        	prjArr = prjFilter.split(",");
        	if(prjArr != null && prjArr.length > 0){
        		for(int i=0; i<prjArr.length; i++){
        			if(!prjArr[i].equals(""))
        				pIDs += "p.RecId = " + prjArr[i] + " or ";
            	}
            	if(!pIDs.equals("")){
            		pIDs = " where " + pIDs.substring(0, pIDs.length()-4);
            	}
        	}
        	  
            getBudgetReport();
            stSql += "SELECT * FROM Projects p" + pIDs + " order by p.ProjectName;";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;

          case 51: // Budget Report
            getBudgetReport();
            stSql += "SELECT * FROM Budget_Report br, Projects p where p.RecId=br.RecId order by p.ProjectName";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;

          case 55: // Criteria
        	
    	  /* Start of Issue AS -- 12Oct2011 -- Issue # 50 */
          String stDivList1 = "";
          if (stFilter != null && stFilter.length() > 7)
          { //128^,3,,4,,6,^,3,,1,^full^^beg
            String[] aV = stFilter.split("\\^", -1);
            int nmType = Integer.parseInt(aV[0]);
            //stSqlUser = this.epsUd.makeUserSql(nmType, aV[1], aV[2], aV[3], aV[4], aV[5]);
            stDivList1 = aV[2];
            if (!stDivList1.contains(",0,"))
            {
              stDivList1 = stDivList1.replace(",,", ",");
              stDivList1 = stDivList1.substring(1, stDivList1.length() - 1);
              //stSql += " where nmDivision in (" + stDivList + ") ";
            }
            
          }
          /* End of Issue AS -- 12Oct2011 -- Issue # 50 */
            this.epsUd.epsEf.processUsersInDivision();
            stSql += "SELECT * FROM Criteria c, teb_division d where c.nmDivision = d.nmDivision order by stDivisionName,c.CriteriaName";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 56: // Division
            this.epsUd.epsEf.processUsersInDivision();
            stSql += "SELECT * FROM teb_division order by stDivisionName;";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 57: // Inventory
            stSql = "select i.*,sum(ri.nmQty) as nmUsed from Inventory i"
              + " left join teb_refinventory ri on i.RecId=ri.nmInventoryId"
              + " group by i.RecId order by InventoryName;";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 61: // Labor Category
            this.epsUd.epsEf.processUsersInLaborCategory();
            stSql += "SELECT * FROM LaborCategory order by LaborCategory";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 65: // Missing Inventory
            stSql = "select * from Inventory i,teb_refinventory ri, Projects p, Schedule s"
              + " where i.RecId=ri.nmInventoryId and ri.nmMissing > 0 and ri.nmProjectId=p.RecId"
              + " and p.RecId=s.nmProjectId and p.CurrentBaseline=s.nmBaseline and s.RecId=ri.nmTaskId"
              + " order by InventoryName, p.ProjectName,s.SchTitle";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 67: // Missing Labor Category
            this.epsUd.epsEf.getMissingLc(1);
            stSql = "SELECT * FROM Missing_Labor_Report mlr, LaborCategory lc, Projects p"
              + " where p.RecId=mlr.nmPrjIdMLC and mlr.nmTaskIdMLC=lc.nmLcId"
              + " order by lc.LaborCategory, mlr.TaskFinishDate";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 59: // Issues
            stSql = "SELECT * FROM Triggers tr, ebeps01.X25Task t left join Users u on u.nmUserId=t.nmUserCreated"
              + " where tr.nmTaskType=2 and t.nmMasterTaskId=tr.RecId order by dtStart desc";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 87: // Triggers
            stSql = "SELECT * FROM Triggers tr, ebeps01.X25Task t left join Users u on u.nmUserId=t.nmUserCreated"
              + " where tr.nmTaskType != 2 and  t.nmMasterTaskId=tr.RecId order by dtStart desc";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 53: // Cost Effectiveness
            getCostEffectiveness();
            String stSqlUser = "";
            if (stFilter != null && stFilter.length() > 7)
            { //128^,3,,4,,6,^,3,,1,^full^^beg
              String[] aV = stFilter.split("\\^", -1);
              int nmType = Integer.parseInt(aV[0]);
              stSqlUser = this.epsUd.makeUserSql(nmType, aV[1], aV[2], aV[3], aV[4], aV[5]);
            } else
            {
              stSqlUser = this.epsUd.makeUserSql(0xFFFF, ",0", ",0,", "full", "", "beg"); // Do all
            }
            stSqlUser = stSqlUser.replace("u.*,xu.stEMail,xu.nmPriviledge,xu.RecId", "u.nmUserId");

            stSql = "SELECT * FROM Users u, LaborCategory lc, teb_reflaborcategory rlc"
              + " where u.nmUserId in (" + stSqlUser + ") "
              + " and u.nmUserId=rlc.nmRefId and rlc.nmRefType=42 and rlc.nmLaborCategoryId=lc.nmLcId"
              + " order by u.LastName,u.FirstName; ";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              String val = getReportFields(rsR, rsF, iMaxF);
              String[] fieldArr = val.split("~");
              val = "";
              //if money value, calculate exchange rate
              for (int i=1; i<fieldArr.length; i++){
	              if(fieldArr[i].contains("$")){
	            	  //get exchange rate with id and division #
	            	  int divid = this.epsUd.ebEnt.dbDyn.ExecuteSql1n("select nmDivision from teb_refdivision where nmRefType=42 and nmRefId=" + rsR.getString("nmUserId"));
	                  ResultSet rs2 = this.epsUd.ebEnt.dbDyn.ExecuteSql("select stCurrency, stMoneySymbol from teb_division where nmDivision=" + divid);
	                  rs2.last();
	                
	                  //calculate exchange rate if this is not in usd
	                  if(!rs2.getString("stCurrency").equals("USD")){
	                	  String rate = getExchangeRate("http://finance.yahoo.com/d/quotes.csv?e=.csv&f=sl1d1t1&s=USD"+rs2.getString("stCurrency")+"=X");
	                	  String[] resp = rate.split(",");
  
  						  DecimalFormat df = new DecimalFormat(" #,###,###,##0.00");
	                	  Double vald = Double.parseDouble(fieldArr[i].substring(2)) * Double.parseDouble(resp[1]);
	                	  fieldArr[i] = rs2.getString("stMoneySymbol") + df.format(vald);
	                  System.out.println(rate);
	                  }
	              }
            	  val += "~" + fieldArr[i];
            	  System.out.println(fieldArr[i]);
              }
              stReport += val + "|";
            }
            break;
          case 89: // Users
            getCostEffectiveness();
            if (stFilter != null && stFilter.length() > 7)
            { //128^,3,,4,,6,^,3,,1,^full^^beg
              String[] aV = stFilter.split("\\^", -1);
              int nmType = Integer.parseInt(aV[0]);
              stSql = this.epsUd.makeUserSql(nmType, aV[1], aV[2], aV[3], aV[4], aV[5]);
            } else
            {
              stSql = this.epsUd.makeUserSql(0xFFFF, ",0", ",0,", "full", "", "beg"); // Do all
            }
            stSql += " order by LastName,FirstName,stEMail";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getReportFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          case 71: //  PROJECT Ranking
            doProjectRanking(this.epsUd);
            stSql = "SELECT * FROM Projects order by TotalRankingScore DESC,ProjectName";
            rsR = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            rsR.last();
            iMaxR = rsR.getRow();
            for (int iR = 1; iR <= iMaxR; iR++)
            {
              rsR.absolute(iR);
              stReport += getProjectFields(rsR, rsF, iMaxF) + "|";
            }
            break;
          default:
            stReturn += "<BR>Internal ERROR during report run for " + rsTable.getString("stTableName");
            break;
        }
        // Save Report DATA
        int iId = this.epsUd.ebEnt.dbDyn.ExecuteSql1n("select max(RecId) from teb_reports");
        iId++;
        this.epsUd.ebEnt.dbDyn.ExecuteUpdate("replace into teb_reports (RecId,nmCustomReportId,dtRun,stReportRaw)"
          + "values( " + iId + "," + nmCustomReportId + ",now(),"
          + this.epsUd.ebEnt.dbDyn.fmtDbString(getFields(rsF, iMaxF) + "|\n" + stReport) + ")");
        return viewReport(rsTable);
      } catch (Exception e)
      {
        this.stError += "<BR> ERROR runReport " + e;
      }
      return stReturn;
    } else
      return customReportDesigner(rsTable); // Not a valid id, go design it first.
  }

  private String getProjectFields(ResultSet rsP, ResultSet rsF, int iMaxF)
  {
    String stReturn = "";
    String stTemp = "";
    try
    {
      for (int iF = 1; iF <= iMaxF; iF++)
      {
        rsF.absolute(iF);
        switch (rsF.getInt("nmForeignId"))
        {
          case 123: // Project Name
            stReturn += "~" + rsP.getString("ProjectName");
            break;
          case 998: // Total_Ranking_Score 
            stReturn += "~" + rsP.getString("TotalRankingScore");
            break;

          case 640: // Report_Title_71_3 IGNORE
            break;

          default:
            stReturn += "~";
            if ((rsF.getInt("nmFlags") & 0x10000000) != 0)
            {
              stTemp = this.epsUd.ebEnt.dbDyn.ExecuteSql1("select (iValue * WeightImportance) as val"
                + " from teb_project p, Criteria c where c.nmDivision=" + rsP.getString("nmDivision")
                + " and c.CriteriaName = \"" + rsF.getString("stLabel") + "\""
                + " and nmProjectId=" + rsP.getString("RecId") + " and nmBaseline=1 and"
                + " nmFieldId=" + rsF.getString("nmForeignId"));
              if (stTemp == null)
                stTemp = "";
              stReturn += fmtReportData(stTemp.trim(), rsF);
            } else
            {
              if (this.epsUd.ebEnt.ebUd.getLoginId() == 1)
                this.stError += "<br>Field: " + rsF.getInt("nmForeignId") + " " + rsF.getString("stDbFieldName") + " not yet mapped";
            }
            break;
        }
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR: getProjectFields " + e;
    }
    return stReturn;
  }

  private String getReportFields(ResultSet rsR, ResultSet rsF, int iMaxF)
  {
    StringBuilder abReturn = new StringBuilder(10000);
    String stValue = "";
    String stSql = "";
    ResultSet rs = null;
    int iF = 0;
    int iCount = 0;
    int stlvl = 0;
    try
    {
      for (iF = 1; iF <= iMaxF; iF++)
      {
        rsF.absolute(iF);
        if(rsF.getInt("nmFieldId") != 765 && rsF.getInt("nmFieldId") != 766){	//these are not in db. calculate these later
        	stValue = rsR.getString(rsF.getString("stDbFieldName"));
        }
        
        switch (rsF.getInt("nmDataType"))
        {
        case 5: //cost effectiveness
            abReturn.append("~");
            abReturn.append(fmtReportData(stValue, rsF));
            break;
          case 41: // LaborCateogries
            abReturn.append("~");
            abReturn.append(this.epsUd.epsEf.makeLaborCategories(rsF, stValue, 2));
            break;
          case 42: // Dependencies
            abReturn.append("~");
            abReturn.append(this.epsUd.epsEf.makeDependencies(rsF, 2, rsR.getString("nmProjectId"), rsR.getString("RecId")));
            break;
          case 45: // Successors
            abReturn.append("~");
            abReturn.append(this.epsUd.epsEf.makeSuccessors(rsF, 2, rsR.getString("nmProjectId"), rsR.getString("RecId")));
            break;
          case 43: // Inventory
            abReturn.append("~");
            abReturn.append(this.epsUd.epsEf.makeInventory(rsF, stValue, 2));
            break;
          case 44: // Other Resources
            abReturn.append("~");
            abReturn.append(this.epsUd.epsEf.makeOtherResources(rsF, stValue, 2));
            break;
          case 47: // Indicators
            abReturn.append("~");
            abReturn.append(this.epsUd.epsEf.makeIndicators(rsF, stValue, 2));
            break;

          case 54: //Req mapping task ids
          case 55: // 	MappingProjectPercent
          case 56: // mapping task ids
            abReturn.append("~");
            abReturn.append(this.epsUd.epsEf.makeProjectReqMap(rsF, stValue, rsR));
            break;

          case 40: // Special Days
            abReturn.append("~");
            abReturn.append("");
            break;

          case 49:
            abReturn.append("~");
            abReturn.append(this.epsUd.epsEf.makeCostEffectiveness(rsF, stValue, rsR));
            break;
          case 50:
            abReturn.append("~");
            abReturn.append(this.epsUd.epsEf.makeProductivity(rsF, stValue, rsR));
            break;
          case 51:
            abReturn.append("~");
            abReturn.append(this.epsUd.epsEf.makeEstimatedHours(rsF, stValue, rsR));
            break;
          case 52:
            abReturn.append("~");
            abReturn.append(this.epsUd.epsEf.makeActualHours(rsF, stValue, rsR));
            break;
          case 53:
            abReturn.append("~");
            abReturn.append(this.epsUd.epsEf.makeDivision(rsF, stValue, rsR));
            break;
          default:
            switch (rsF.getInt("nmForeignId"))
            {
              case 43:
                abReturn.append("~");
                int iFlags = rsR.getInt("ReqFlags");
                if ((iFlags & 0x10) != 0)
                {
                  iCount = 1;
                  int iD50Flags = rsR.getInt("nmD50Flags");
                  if ((iD50Flags & 0x1) != 0)
                  {
                    if (iCount++ > 1)
                      abReturn.append("<br>");
                    abReturn.append("Contains more than 1 Sentences.");
                  }
                  if ((iD50Flags & 0x2) != 0)
                  {
                    if (iCount++ > 1)
                      abReturn.append("<br>");
                    abReturn.append("Contains lists or clauses");
                  }
                  if ((iD50Flags & 0x4) != 0)
                  {
                    if (iCount++ > 1)
                      abReturn.append("<br>");
                    abReturn.append("Contains clauses.");
                  }
                  if ((iD50Flags & 8) != 0)
                  {
                    if (iCount++ > 1)
                      abReturn.append("<br>");
                    abReturn.append("Min words required");
                  }
                  if ((iD50Flags & 0x10) != 0)
                  {
                    if (iCount++ > 1)
                      abReturn.append("<br>");
                    abReturn.append("Max words exceeded");
                  }
                  if ((iD50Flags & 0x20) != 0)
                  {
                    if (iCount++ > 1)
                      abReturn.append("<br>");
                    abReturn.append("adjectives/adverbs");
                  }
                  if ((iD50Flags & 0x40) != 0)
                  {
                    if (iCount++ > 1)
                      abReturn.append("<br>");
                    abReturn.append("Contains conjunctions");
                  }
                }
                break;
              case 45:
                abReturn.append("~");
                iFlags = rsR.getInt("SchFlags");
                if ((iFlags & 0x10) != 0)
                {
                  int iD53Flags = Integer.parseInt(stValue);
                  if ((iD53Flags & 0x1) != 0)
                  {
                    abReturn.append("Does not begin with a verb");
                  }
                }
                break;
              case 29:
                abReturn.append("~");
                abReturn.append(this.epsUd.ebEnt.dbDyn.ExecuteSql1(
                  "select  ProjectName from Projects where RecId=" + rsR.getInt(rsF.getString("stDbFieldName"))));
                break;
              case 16: // Lc - for user;
                abReturn.append("~");
                ResultSet rsLc = this.epsUd.ebEnt.dbDyn.ExecuteSql("select distinct LaborCategory from LaborCategory l,"
                  + " teb_reflaborcategory rl where rl.nmRefType=42 and rl.nmRefId=" + rsR.getString("RecId")
                  + " and rl.nmLaborCategoryId=nmLcId order by LaborCategory;");
                rsLc.last();
                int iMaxL = rsLc.getRow();
                if (iMaxL > 0)
                {
                  for (int iL = 1; iL <= iMaxL; iL++)
                  {
                    rsLc.absolute(iL);
                    if (iL > 1)
                      abReturn.append("`");
                    abReturn.append(rsLc.getString("LaborCategory"));
                  }
                }
                break;
              case 104: //Weekly Work Hours
              case 91: //Activity Hours
                int iHours = 0;
                try
                {
                  String[] aV = stValue.trim().split("~", -1);
                  for (int i = 0; i < aV.length; i++)
                  {
                    if (aV[i] != null && aV[i].length() > 0)
                      iHours += Integer.parseInt(aV[i]);
                  }
                } catch (Exception e)
                {
                }
                abReturn.append("~");
                abReturn.append(iHours);
                break;
              case 15: // nmPriviledge
                abReturn.append("~");
                abReturn.append(this.epsUd.epsEf.getPriviledgeTypes(rsR.getInt(rsF.getString("stDbFieldName"))));
                break;
              case 238:	//Requirement Title
            	  //calculate level and indent
            	  stlvl = rsR.getInt("ReqLevel");
            	  for(int i=0; i<stlvl; i++)
            		  stValue = "&nbsp;&nbsp;&nbsp;&nbsp;" + stValue;
            	  abReturn.append("~");
                  abReturn.append(fmtReportData(stValue, rsF));
                  break;
              case 258:	//Schedule Title
            	  //calculate level and indent
            	  stlvl = rsR.getInt("SchLevel");
            	  for(int i=0; i<stlvl; i++)
            		  stValue = "&nbsp;&nbsp;&nbsp;&nbsp;" + stValue;
            	  abReturn.append("~");
                  abReturn.append(fmtReportData(stValue, rsF));
                  break;
              case 765:  //baseline cost difference
            	  Double currCost = 0.00;
            	  Double appCost = 0.00;
            	  //lookup current baseline
            	  stSql = "select sum(SchCost) as costSum FROM Schedule where (SchFlags & 0x10 ) != 0 and nmProjectId=" + rsR.getString("RecId") + " and nmBaseline=" + rsR.getString("CurrentBaseline");
            	  rs = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            	  if(rs.first()){
            		  rs.absolute(1);
            		  
            		  currCost = Double.parseDouble(rs.getString("costSum"));
            	  }

            	  //look up saved baseline
            	  stSql = "select nmCost from teb_baseline where nmProjectId="+rsR.getString("RecId")+" and stType='Approve' ORDER BY nmBaseline DESC";
            	  rs = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            	  if(rs.first()){
            		  rs.absolute(1);
            		  appCost = Double.parseDouble(rs.getString("nmCost"));
            	  }
            	  abReturn.append("~");
                  
            	  //if baseline was not approved, no difference
            	  if(appCost > 0.00){
            		  DecimalFormat df = new DecimalFormat("$ #,###,###,##0.00");
            		  abReturn.append(df.format(currCost-appCost));
            	  }
            	  break;
              case 766:  //baseline effort difference
            	  Double currBL = 0.00;
            	  Double appBL = 0.00;
            	  //lookup current baseline
            	  stSql = "SELECT sum(SchEstimatedEffort) as effSum FROM Schedule where (SchFlags & 0x10 ) != 0 and nmProjectId=" + rsR.getString("RecId") + " and nmBaseline=" + rsR.getString("CurrentBaseline");
            	  
            	  rs = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            	  if(rs.first()){
            		  rs.absolute(1);
            		  
            		  currBL = Double.parseDouble(rs.getString("effSum"));
            	  }

            	  //look up saved baseline
            	  stSql = "select nmEffort from teb_baseline where nmProjectId="+rsR.getString("RecId")+" and stType='Approve' ORDER BY nmBaseline DESC";
            	  rs = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
            	  if(rs.first()){
            		  rs.absolute(1);
            		  appBL = Double.parseDouble(rs.getString("nmEffort"));
            	  }
            	  abReturn.append("~");
            	  //if baseline was not approved, no difference
            	  if(appBL > 0.00)
            		  abReturn.append(Math.ceil(currBL-appBL));
            	  break;
              default:
                abReturn.append("~");
                abReturn.append(stValue);
                break;
            }
            break;
        }
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR: getReportFields (" + iF + ") " + e;
    }
    return abReturn.toString();
  }

  private String fmtReportData(String stValue, ResultSet rsF)
  {
    StringBuilder abReturn = new StringBuilder(255);
    DecimalFormat df = null;
    double dValue = 0;
    int iValue = 0;
    try
    {
      if (stValue != null && stValue.trim().length() > 0)
      {
        if (rsF.getString("stHandler").contains("selectuser-29"))
        {
          ResultSet rs2 = this.epsUd.ebEnt.dbDyn.ExecuteSql("select concat(FirstName,' ',LastName) as nm from Users where nmUserId in ( " + stValue + " ) order by LastName,FirstName");
          rs2.last();
          int iMax2 = rs2.getRow();
          for (int i = 1; i <= iMax2; i++)
          {
            rs2.absolute(i);
            if (i > 1)
            {
              abReturn.append("<br>");
            }
            abReturn.append(rs2.getString("nm"));
          }
        } else
        {
          switch (rsF.getInt("nmDataType"))
          {
            //case 48: // LC:
            //  break;
            case 5:
              if (rsF.getString("stMask").length() > 0)
                df = new DecimalFormat(rsF.getString("stMask"));
              else
                df = new DecimalFormat("$ #,###,###,##0.00");
              dValue = Double.parseDouble(stValue);
              abReturn.append(df.format(dValue));
              break;
            case 31:
              if (rsF.getString("stMask").length() > 0)
                df = new DecimalFormat(rsF.getString("stMask"));
              else{
            	  /* AS -- 27Sept2011 -- Issue #53 */
                  //df = new DecimalFormat("#,###,###,##0.0");
                  df = new DecimalFormat("#,###,###,##");
              }
              dValue = Double.parseDouble(stValue);
              abReturn.append(df.format(dValue));
              break;
            case 1:
              if (rsF.getString("stMask").length() > 0)
                df = new DecimalFormat(rsF.getString("stMask"));
              else
                df = new DecimalFormat("#,###,###,##0");
              iValue = Integer.parseInt(stValue);
              abReturn.append(df.format(iValue));
              break;
            case 4: // Long Text;
              stValue = stValue.replace("\n", "`");
              abReturn.append(stValue.replace("~", "`"));
              break;

            default:
              abReturn.append(stValue.replace("~", "`"));
              break;
          }
        }
      }
    } catch (Exception e)
    {
      stError += "<BR>ERROR fmtReportData: " + e;
    }
    return abReturn.toString();
  }

  private String getFields(ResultSet rsF, int iMaxF)
  {
    String stReturn = "";
    try
    {
      for (int iF = 1; iF <= iMaxF; iF++)
      {
        rsF.absolute(iF);
        String stShort = rsF.getString("stShort");
        String stLabel = rsF.getString("stLabel");
        if (stShort != null && stShort.length() > 0)
        {
          stLabel = stShort;
        }
        stReturn += "~" + stLabel.trim() + "^" + rsF.getString("nmForeignId");
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR: getFields " + e;
    }
    return stReturn;
  }

  private String viewReport(ResultSet rsTable)
  {
    String stReturn = "";

    /* AS -- 2Oct2011 -- Issue#9*/
    //stReturn += epsShowSavedReports();
    stReturn += epsShowSavedReports(rsTable);
    
    return stReturn;
  }

  public void doProjectRanking(EpsUserData epsUd)
  {
    try
    {
      this.epsUd = epsUd;
      this.epsUd.ebEnt.dbDyn.ExecuteUpdate("update Projects set TotalRankingScore=0");

      ResultSet rs = this.epsUd.ebEnt.dbDyn.ExecuteSql("select * from teb_project p, teb_fields f, Projects pr, Criteria c"
        + " where p.nmFieldId=f.nmForeignId and c.nmDivision = pr.nmDivision and c.CriteriaName=f.stLabel "
        + " and pr.RecId=p.nmProjectId"
        + " and (f.nmFlags & 0x10000000 ) != 0 and p.nmBaseline=pr.CurrentBaseline "
        + " order by nmProjectId");
      rs.last();
      int iMax = rs.getRow();
      int iLast = -1;
      double dTotal = 0;
      for (int iR = 1; iR <= iMax; iR++)
      {
        rs.absolute(iR);
        if (rs.getInt("nmProjectId") == 22)
          stError += "";

        if (iLast > 0 && iLast != rs.getInt("nmProjectId"))
        {
          this.epsUd.ebEnt.dbDyn.ExecuteUpdate("update Projects set"
            + " TotalRankingScore=" + dTotal + " where RecId=" + iLast);
          dTotal = 0;
        }
        iLast = rs.getInt("nmProjectId");
        if (rs.getString("WeightImportance") != null && rs.getString("iValue") != null && rs.getInt("iValue") > 0)
        {
          try
          {
            double dValue = rs.getInt("WeightImportance") * rs.getInt("iValue");
            dTotal += dValue;
          } catch (Exception e)
          {
          }
        }
      }
      if (iLast > 0)
        this.epsUd.ebEnt.dbDyn.ExecuteUpdate("update Projects set"
          + " TotalRankingScore=" + dTotal + " where RecId=" + iLast);

    } catch (Exception e)
    {
      this.stError += "<BR> ERROR doProjectRanking " + e;
    }
  }

  public void getBudgetReport()
  {
    String stSql = "";
    double DifferenceInitCurrCost = 0;
    double CurrentEstimatedCost = 0;
    double DollarsExpendedtoDate = 0;
    double InitialEstimatedCost = 0;
    double CPI = 0;
    double SPI = 0;
    double EarnedValue = 0;
    try
    {
      this.epsUd.ebEnt.dbDyn.ExecuteUpdate("truncate table Budget_Report");
      ResultSet rsP = this.epsUd.ebEnt.dbDyn.ExecuteSql("select * from Projects");
      rsP.last();
      int iMaxP = rsP.getRow();
      for (int iP = 1; iP <= iMaxP; iP++)
      {
        rsP.absolute(iP);
        EarnedValue = this.epsUd.ebEnt.dbDyn.ExecuteSql1n("SELECT sum(SchCost)"
          + " FROM Projects p, Schedule s where s.nmProjectId=p.RecId"
          + " and p.CurrentBaseline=s.nmBaseline and p.RecId=" + rsP.getString("RecId")
          + " and (s.SchFlags & 0x1000) != 0");
        DifferenceInitCurrCost = 0;
        CurrentEstimatedCost = 0;
        DollarsExpendedtoDate = 0;
        InitialEstimatedCost = 0;
        CPI = 0;
        SPI = 0;
        EarnedValue = 0;
        int iFirstApprove = this.epsUd.ebEnt.dbDyn.ExecuteSql1n("SELECT min(nmBaseline)"
          + " FROM teb_baseline where nmProjectId=" + rsP.getString("RecId") + " and stType='Approve'");
        int iIniticalBaseline = this.epsUd.ebEnt.dbDyn.ExecuteSql1n("SELECT max(nmBaseline)"
          + " FROM teb_baseline where nmProjectId=" + rsP.getString("RecId") + " and nmBaseline < " + iFirstApprove);
        if (iIniticalBaseline < iFirstApprove)
        {
          // Got a real approval and initial bl
          InitialEstimatedCost = getScheduleCost(rsP.getInt("RecId"), iIniticalBaseline);
          CurrentEstimatedCost = getScheduleCost(rsP.getInt("RecId"), rsP.getInt("CurrentBaseline"));
          DifferenceInitCurrCost = CurrentEstimatedCost - InitialEstimatedCost;
        } else
        {
          InitialEstimatedCost = getScheduleCost(rsP.getInt("RecId"), rsP.getInt("CurrentBaseline"));
        }
        stSql = "INSERT INTO Budget_Report "
          + "(RecId,DifferenceInitCurrCost,CurrentEstimatedCost,DollarsExpendedtoDate,InitialEstimatedCost"
          + ",CPI,SPI,EarnedValue) values(" + rsP.getString("RecId") + "," + DifferenceInitCurrCost
          + "," + CurrentEstimatedCost + "," + DollarsExpendedtoDate + "," + InitialEstimatedCost
          + "," + CPI + "," + SPI + "," + EarnedValue + ")";
        this.epsUd.ebEnt.dbDyn.ExecuteUpdate(stSql);
      }
    } catch (Exception e)
    {
      this.stError += "<BR> ERROR getBudgetReport " + e;
    }
  }

  public double getScheduleCost(int nmProjectId, int nmBaseline)
  {
    double dReturn = 0;
    try
    {
      dReturn = this.epsUd.ebEnt.dbDyn.ExecuteSql1n("select sum(SchCost) from Schedule"
        + " where nmProjectId=" + nmProjectId + " and nmBaseline=" + nmBaseline + " and SchLevel=0");
    } catch (Exception e)
    {
      this.stError += "<BR> ERROR getScheduleCost " + e;
    }
    return dReturn;
  }

  public void getCostEffectiveness()
  {
    try
    {
      // Need to sum up by User/Lc only, not tasks.
      String stSql = "SELECT count(*) cnt ,sum(nmAllocated) allocated,sum(nmActualApproved) approved,"
        + " ap.nmLc,ap.nmUserId,ap.nmPrjId,ap.nmTaskId,u.HourlyRate,lc.AverageHourlySalary"
        + " FROM teb_allocateprj ap, Projects p, Schedule s, Users u, LaborCategory lc"
        + " where ap.nmPrjId=p.RecId and ap.nmPrjId=s.nmProjectId and p.CurrentBaseline=s.nmBaseline "
        + " and (s.SchFlags & 0x1000) != 0 "
        + " and ap.nmUserId=u.nmUserId and lc.nmLcId=nmLc"
        + " group by nmLc,ap.nmLc,ap.nmUserId,ap.nmPrjId,ap.nmTaskId";
      //		20	160	24	39	50	5	4	74	74
      ResultSet rsC = this.epsUd.ebEnt.dbDyn.ExecuteSql(stSql);
      rsC.last();
      int iMaxC = rsC.getRow();
      if (iMaxC > 0)
      {
        for (int iC = 1; iC <= iMaxC; iC++)
        {
          rsC.absolute(iC);
          /*Cost Effective formula is:
          (Hourly Salary * Productivity Factor) / (Average Labor Category Salary)*/
          double nmCostEffectiveness = 0;
          double nmProductiviyFactor = 0.5;
          if (rsC.getDouble("approved") > 0)
            nmProductiviyFactor = rsC.getDouble("allocated") / rsC.getDouble("approved");
          if (rsC.getDouble("AverageHourlySalary") > 0)
            nmCostEffectiveness = rsC.getDouble("HourlyRate") * nmProductiviyFactor / rsC.getDouble("AverageHourlySalary");
          stSql = "update teb_reflaborcategory set"
            + " nmCostEffectiveness = " + nmCostEffectiveness
            + ",nmProductiviyFactor = " + nmProductiviyFactor
            //+ ",nmTasksCompleted = " + nmTasksCompleted 
            + ",nmActualHours = " + rsC.getString("approved")
            + ",nmEstimatedHours = " + rsC.getString("allocated")
            + " where nmLaborCategoryId = " + rsC.getString("nmLc")
            + " and nmRefType=42 and nmRefId = " + rsC.getString("nmUserId");
          this.epsUd.ebEnt.dbDyn.ExecuteUpdate(stSql);
        }
      }

    } catch (Exception e)
    {
      this.stError += "<BR> ERROR getCostEffectiveness " + e;
    }
  }
  
  public String getExchangeRate(String url){
	HttpURLConnection connection = null;
	OutputStreamWriter wr = null;
	BufferedReader rd = null;
	StringBuilder sb = null;
	String line = null;
	
	URL serverAddress = null;

	try {
		serverAddress = new URL(url);
		//set up out communications stuff
		connection = null;
		
		//Set up the initial connection
		connection = (HttpURLConnection)serverAddress.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);
		connection.setReadTimeout(10000);
		
		connection.connect();
		
		//get the output stream writer and write the output to the server
		//not needed in this example
		//wr = new OutputStreamWriter(connection.getOutputStream());
		//wr.write("");
		//wr.flush();
		
		//read the result from the server
		rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		sb = new StringBuilder();
	
		while ((line = rd.readLine()) != null)
		{
			sb.append(line + '\n');
		}

	} catch (MalformedURLException e) {
		e.printStackTrace();
	} catch (ProtocolException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
	return sb.toString();
  }
}
