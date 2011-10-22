package com.eps.model;
/*
 * 4/4/2011 -- RE-WRITE WITHOUT OILP.exe
 *
 */

import com.ederbase.model.*;
import java.sql.ResultSet;

/**
 *
 * @author Rob Eder
 */
public class EpsClient
{
  public String stVersion = "Version: 8/18/11 10am JF LAPTOP";
  private int iUserId = -1;
  private int nmPrivUser = 0;
  private String stAction = "";
  private String stTrace = "";
  private EbEnterprise ebEnt = null;
  private String stError = "";
  public EpsUserData epsUd = null;

  public EpsClient(EbEnterprise ebEnt, String stDbDyn)
  {
    this.ebEnt = ebEnt;
    if (this.ebEnt.ebDyn == null)
    {
      this.ebEnt.ebDyn = new EbDynamic(this.ebEnt);
    }
    this.ebEnt.ebDyn.assertDb(stDbDyn);
    if (this.ebEnt.ebUd != null && this.ebEnt.ebUd.request != null)
    {
      String stTemp = this.ebEnt.ebUd.request.getParameter("commtrace");
      if (stTemp != null && stTemp.length() > 0 && stTemp.equals("d"))
      {
        this.ebEnt.iDebugLevel = 1;
        this.ebEnt.dbCommon.setDebugLevel(1);
        this.ebEnt.dbDyn.setDebugLevel(1);
        this.ebEnt.dbEb.setDebugLevel(1);
        this.ebEnt.dbEnterprise.setDebugLevel(1);
      }
    }
    this.epsUd = new EpsUserData();
    this.epsUd.setEbEnt(ebEnt, this.stVersion);
    this.epsUd.epsEf.setEbEnt(ebEnt, this);
    this.epsUd.setUser(iUserId, 0);
  }

  public String getEpsPage()
  {
    String stReturn = "";

    String stSql = "SELECT * FROM X25RefContent where nmRefPageId=1 order by nmUserId";
    try
    {

      iUserId = this.ebEnt.ebUd.getLoginId();
      nmPrivUser = this.ebEnt.ebUd.getLoginPersonFlags();

      stAction = this.ebEnt.ebUd.request.getParameter("stAction");
      if (stAction == null || stAction.length() <= 0)
        stAction = "home";

      String stPopupMessage = this.ebEnt.ebUd.request.getParameter("popupmessage");
      if ( stPopupMessage != null && stPopupMessage.length() > 0 )
        ebEnt.ebUd.setPopupMessage(stPopupMessage);

      String stLookup = this.ebEnt.ebUd.request.getParameter("h");
      if (stLookup != null && stLookup.equals("n"))
      { // Load Framework.Masterpage for popup menus, without TOP and NAVIGATION
        stSql = "SELECT * FROM X25RefContent where nmContentId in (1,5,6)  order by nmUserId;";
      }
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        for (int iC = 1; iC <= iMax; iC++)
        {
          rs.absolute(iC);
          if ((rs.getInt("nmFlag") & 0x01) != 0) // Active
          {
            stReturn += processRules(rs.getInt("nmFlag"), rs.getString("stContent"));
          }
        }
      }
      this.ebEnt.ebDyn.setPopupMessage("alert", this.ebEnt.ebUd.getPopupMessage());
      stReturn += this.epsUd.getValitation();
      if (stTrace.length() > 0)
      {
        stReturn += "<hr>Trace:<br>" + stTrace + "</td></tr></table><hr>";
      }

      // Replace global tags with real values
      stReturn = stReturn.replace("~~stPageTitle~", this.epsUd.getPageTitle());
     String stA = this.ebEnt.ebUd.request.getParameter("a");
      if (stA == null || ! stA.equals("28"))
        stReturn = stReturn.replace("PageWidthPx",  this.epsUd.rsMyDiv.getString("PageWidthPx")+"px");
      if (iUserId > 0)
        stReturn = stReturn.replace("~~stWelcome~", "<div id='gen3'>"
          + "<form method=post id=loginout name=loginout>"
          + "<center><font class=medium>Welcome: </font><font class=mediumbold>" + this.ebEnt.dbDyn.ExecuteSql1("select concat(FirstName,' ',LastName) from Users where nmUserId=" + this.iUserId) + "</b></font> &nbsp;&nbsp;<input type=submit name=Logout value=Logout onClick=\"setSubmitId(9998);\"></center></form></div>");
      else
        stReturn = stReturn.replace("~~stWelcome~", "");
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR getEpsPage: " + e;
    }
    return stReturn;
  }

  public String processRules(int nmFlag, String stContent)
  {
    String stReturn = "";
    if ((nmFlag & 0x04) != 0)
    {
      int iPos1 = 0;
      int iPos2 = 0;
      int iPos3 = 0; // last iPos2
      while ((iPos1 = stContent.indexOf("~~", iPos2)) >= 0)
      {
        iPos3 = iPos2;
        iPos2 = stContent.indexOf("~", iPos1 + 2);
        if (iPos2 > iPos1)
        {
          iPos2++; // advance past this character.
          stReturn += stContent.substring(iPos3, iPos1);
          String stCommand = stContent.substring(iPos1, iPos2);
          String[] asFields = stCommand.split("\\|");
          if (asFields.length >= 2)
          {
            if (asFields[0].equals("~~stPageTitle"))
            {
              stReturn += "~~stPageTitle~";
            } else if (asFields[0].equals("~~stWelcome"))
            {
              stReturn += "~~stWelcome~";
            } else if (asFields[0].equals("~~Version"))
            {
              stReturn += this.stVersion;
            } else if (asFields[0].equals("~~MenuBar"))
            {
              // Create Menu Bar.
              if (iUserId > 0)
              {
                stReturn += this.makeMenuBar();
              } else
              {
                stReturn += "&nbsp;";
              }
            } else if (asFields[0].equals("~~MainBody"))
            {
              if (iUserId <= 0)
              {
                this.epsUd.setPageTitle("Login Page");
                stReturn += this.epsUd.getLoginPage();
              } else
              {
                String stA = this.ebEnt.ebUd.request.getParameter("a");
                if (stA != null && stA.equals("28"))
                {
                  this.epsUd.setPageTitle("Super User - Table Edit");
                  stReturn += ebEnt.ebAdmin.getTableEdit(this.ebEnt.ebUd.request);
                } else
                {
                  String stReturn1 = this.epsUd.getActionPage(stAction);
                  String stChild = this.ebEnt.ebUd.getXlsProcess();
                  if (stChild != null && stChild.length() > 0)
                  {
                    EpsXlsProject epsProject = new EpsXlsProject();
                    epsProject.setEpsXlsProject(this.ebEnt, this.epsUd);
                    if (stChild.equals("19"))
                      stReturn1 = epsProject.xlsRequirementsEdit(stChild);
                    else if (stChild.equals("21"))
                      stReturn1 = epsProject.xlsSchedulesEdit(stChild);
                    else if (stChild.equals("34"))
                      stReturn1 = epsProject.xlsTestEdit(stChild);
                    else if (stChild.equals("46"))
                      stReturn1 = epsProject.xlsAnalyze();
                    else if (stChild.equals("26"))
                      stReturn1 = epsProject.xlsBaseline(stChild);
                    else
                      stError += "<BR>ERROR child not implemented " + stChild;
                    this.stError += epsProject.getError();
                  }
                  stReturn += stReturn1;
                }
              }
            }
          } else
          {
            stError += "<BR>ERROR: process command not implemented: " + stCommand;
          }
        } else
        {
          break;
        }
      } // while
      if (iPos2 < stContent.length())
      {
        stReturn += stContent.substring(iPos2); // copy remainder.
      }
    } else
    {
      stReturn = stContent;
    }
    return stReturn;
  }

  public String makeMenuBar()
  {
    String stReturn = "";
    stReturn += "<li><a class='topnav' href='./?stAction=home'>Home</a><ul>";
    //stReturn += "<li><a href='./?stAction=tasks'>Tasks</a></li>";
    //stReturn += "<li><a href='./?stAction=wf'>Workflow</a></li>
    stReturn += "</ul></li>";
    try
    {
      String stSql = "SELECT * FROM teb_table where nmTableType > 0 order by stTableName";
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      int iMax = rs.getRow();
      String stReport = "";
      String stAdmin = "";
      String stProject = "";
      for (int iR = 1; iR <= iMax; iR++)
      {
        rs.absolute(iR);
        if ((rs.getInt("nmProjectPriv") & nmPrivUser) != 0)
        {
          stProject += "<li><a href='./?stAction=projects&c=allocate'>Allocate</a></li>";
          //stProject += "<li><a href='./?stAction=projects&c=analprj'>Analyze Project</a></li>";
          //stProject += "<li><a href='./?stAction=projects&c=analreq'>Analyze Requirements</a></li>";
          //stProject += "<li><a href='./?stAction=projects&c=analsch'>Analyze Schedule</a></li>";
          //stProject += "<li><a href='./?stAction=projects&c=apprprj'>Approve Project</a></li>";
          //stProject += "<li><a href='./?stAction=projects&c=apprreq'>Approve Requirement</a></li>";
          //stProject += "<li><a href='./?stAction=projects&c=apprsch'>Approve Schedule</a></li>";
          stProject += "<li><a href='./?stAction=projects&c=critscor'>Criterion Scoring</a></li>";
          if (this.epsUd.stPrj != null && this.epsUd.stPrj.length() > 0)
            stProject += "<li><a href='./?stAction=projects&t=12&pk=" + this.epsUd.stPrj + "&do=edit'>Current Project</a></li>";
          stProject += "<li><a href='./?stAction=projects&t=" + rs.getInt("nmTableId") + "'>" + rs.getString("stTableName") + "</a></li>";
        }
        if ((rs.getInt("nmReportPriv") & nmPrivUser) != 0)
        {
          stReport += "<li><a href='./?stAction=reports&t=" + rs.getInt("nmTableId") + "'>" + rs.getString("stTableName") + "</a></li>";
        }
        if ((rs.getInt("nmAccessPriv") & nmPrivUser) != 0)
        {
          if (stAdmin.length() <= 0 && ( nmPrivUser & 0x220) != 0) // Ex and PPM
            stAdmin += "<li><a href='./?stAction=admin&c=appr'>Approval</a></li>";

          if (rs.getInt("nmTableId") == 15) // Options
            stAdmin += "<li><a href='./?stAction=admin&t=" + rs.getInt("nmTableId") + "&pk=1&do=edit'>" + rs.getString("stTableName") + "</a></li>";
          else if (rs.getInt("nmTableId") == 9) // Users
          {
            stAdmin += "<li><a href='./?stAction=admin&t=" + rs.getInt("nmTableId") + "&do=users'>" + rs.getString("stTableName") + "</a></li>";
          } else
            stAdmin += "<li><a href='./?stAction=admin&t=" + rs.getInt("nmTableId") + "'>" + rs.getString("stTableName") + "</a></li>";
        }
      }
      if (stReport.length() > 0)
        stReturn += "<li><a class='topnav' href='#'>Reports</a><ul>" + stReport + "</ul></li>";

      if (stProject.length() > 0)
        stReturn += "<li><a class='topnav' href='#'>Project</a><ul>" + stProject + "</ul></li>";

      if (stAdmin.length() > 0)
        stReturn += "<li><a class='topnav' href='#'>Administration</a><ul>" + stAdmin + "</ul></li>";

      stReturn += "<li><a class='topnav' href='#'>Help</a><ul>";
      stReturn += "<li><a href='./?stAction=help&i=about'>About</a></li>";
      /* AS -- 28Sept2011 -- Issue #66 */
      //stReturn += "<li><a href='./?stAction=help&i=content'>Help Contents</a></li></ul></li>";
      stReturn += "<li><a href='./?stAction=help&i=content'>Contents</a></li></ul></li>";

      if ((this.ebEnt.ebUd.getLoginPersonFlags() & 0x800) != 0)
      { // Super User only
        stReturn += "<li><a class='topnav' href='#'>System Admin</a><ul>";
        //stReturn += "<li><a href='./?a=28&tb=1.d.teb_division'>Division Setup</a></li>";
        stReturn += "<li><a href='./?stAction=tablefield&rebuild=tblcol'>Table/Field Manager</a></li>";
        //stReturn += "<li><a href='./?stAction=mspmload'>MS Project Load</a></li>";
        stReturn += "<li><a href='./?stAction=tcimport&id2=di'>Data Import</a></li>";
        //stReturn += "<li><a href='./?stAction=specialdays'>Load Site Special Days</a></li>";
        //stReturn += "<li><a target=\"_blank\" href='./?b=2002'>QA Admin</a></li>";
        stReturn += "<li><a target=\"_blank\" href='./?b=2001'>QA Manager</a></li>";
        stReturn += "<li><a href='./?a=28&tb=1.e.X25RefContent'>Edit Content</a></li>";
        stReturn += "<li><a href='./?stAction=rescyneb'>Resync EB</a></li>";
        stReturn += "<li><a href='./?stAction=d50d53&commtrace=i'>Full D50/D53</a></li>";
        stReturn += "<li><a href='./?stAction=runeob&commtrace=i'>Run EOB now</a></li>";
        stReturn += "</ul></li>";
      }
    } catch (Exception e)
    {
      this.stError += "<BR> makeMenuBar " + e;
    }
    return stReturn;
  }

  public String getError()
  {
    if (this.epsUd != null)
      this.stError += this.epsUd.getError();

    return this.stError;
  }
}
