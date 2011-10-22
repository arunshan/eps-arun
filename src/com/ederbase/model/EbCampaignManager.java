package com.ederbase.model;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

public class EbCampaignManager
{
  private EbEnterprise ebEnt = null;
  private String stError = "";
  private String stCampTitle = "";
  private HttpServletRequest request = null;

  public EbCampaignManager(EbEnterprise ebEnt, HttpServletRequest request)
  {
    this.ebEnt = ebEnt;
    this.request = request;
  }

  public String campainManager(int iType)
  {
    String stReturn = "";
    String stA = "";
    try
    {
      stA = this.request.getParameter("a");
      this.stCampTitle = this.request.getParameter("stTitle");
      if ((this.stCampTitle != null) && (this.stCampTitle.length() > 0))
      {
        iType = Integer.parseInt(this.request.getParameter("nmCampaignType"));
      }
      switch (iType)
      {
      case 20:
        stReturn = stReturn + manageCampaign();
        break;
      case 1:
        stReturn = stReturn + "<br><form method=post><table><tr><th colspan=2><h1>Create a new campaign</th></tr><tr><td>Campaign Type: </td><td><select name=nmCampaignType><option value=2>Email Campaign (1 email per company)</option><option value=3>Email Campaign (all emails)</option><option value=4>AUTO: CraigsList: GIGS/Computer</option><option value=5>Phone Campaign</option><option value=6>Mailing Campaign</option></td></tr><tr><td>Campaign Title: </td><td><input type=text name=stTitle value='' size=80></td></tr></td></tr><tr><td>Profession ID List (0=everyone, 2=Real Estate) </td><td><input type=text name=stProfessionList value='2' size=80></td></tr></td></tr><tr><td>Production Code </td><td><input type=text name=stProdCode value='z7qp01FGH' size=80></td></tr></td></tr><tr><td>Filter </td><td><input type=text name=stFilter value=\" and ( p.nmFlags & 0x1E7F0001 ) = 1 \" size=80></td></tr></td></tr><tr><td>From Name </td><td><input type=text name=stFromName value='Rob Eder' size=80></td></tr></td></tr><tr><td>From email </td><td><input type=text name=stFromEmail value='RobertEder@myinfo.com' size=80></td></tr><tr><td valign=top>Description: </td><td><textarea valign=top name=stDescription rows=5 cols=80></textarea></td></tr><tr><td>Select Boiler Text: </td><td>";

        stReturn = stReturn + this.ebEnt.ebNorm.makeDropDown("select RecId, stSubject from X25Boiler where RecId=nmBoilerMaster order by stSubject,dtLastUpdate", "nmBoilerId", "");
        stReturn = stReturn + "</td></tr><tr><td>Start Date</td><td><input type=text name=dtStart value=''> (today if left empty)</td></tr><tr><td>End Date</td><td><input type=text name=dtEnd value=''> (today if left empty)</td></tr><tr><td valign=top>Status List</td><td><textarea name=stStatusList rows=5 cols=80>002: Initial Email Sent\n003: Positive Initial Reply\n004: Negative Initial Reply\n005: Manual Followup sent.\n006: Automatic Followup sent</textarea></td></tr><tr><td colspan=2 align=center><input type=hidden name=a value='" + stA + "'><input type=submit name=submit value='Start'></form></td></tr></table>";

        break;
      case 2:
      case 3:
      case 4:
        stReturn = stReturn + addCampaign(iType);
        break;
      default:
        stReturn = stReturn + "<br>ERROR: doBlast Type: " + iType + " not yet implemented ";
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<br>ERROR EbMarketingEmail doBlast " + e);
    }
    return stReturn;
  }

  public String manageCampaign()
  {
    String stReturn = "";
    int iSubType = 1;
    String stTemp = "";
    try
    {
      stTemp = this.request.getParameter("b");
      if ((stTemp != null) && (stTemp.length() > 0))
      {
        iSubType = Integer.parseInt(stTemp);
      }
      switch (iSubType)
      {
      case 1:
        stReturn = stReturn + showCampaigns();
        break;
      case 2:
        stReturn = stReturn + manageEmailQueue();
        break;
      default:
        this.stError = (this.stError + "<BR>ERROR manageCampaign: iSubType not supportd " + iSubType);
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<BR>ERROR manageCampaign:  " + e);
    }
    return stReturn;
  }

  public String manageEmailQueue()
  {
    String stReturn = "";
    int iSubType = 1;
    int nmCampaignId = 0;
    int iQueueMax = 0;
    String stTemp = "";
    try
    {
      stTemp = this.request.getParameter("cc");
      if ((stTemp != null) && (stTemp.length() > 0))
      {
        iSubType = Integer.parseInt(stTemp);
      }
      else {
        stTemp = this.request.getParameter("c");
        if ((stTemp != null) && (stTemp.length() > 0))
        {
          iSubType = Integer.parseInt(stTemp);
        }
      }
      stTemp = this.request.getParameter("d");
      if ((stTemp != null) && (stTemp.length() > 0))
      {
        nmCampaignId = Integer.parseInt(stTemp);
      }
      ResultSet rsCamp = this.ebEnt.dbEnterprise.ExecuteSql("select * from X25Campaign c, X25Boiler b where c.nmBoilerId=b.RecId and c.nmCampaignId=" + nmCampaignId);

      if (rsCamp != null)
      {
        rsCamp.last();
        int iMax = rsCamp.getRow();
        if (iMax == 1)
        {
          rsCamp.absolute(1);
          switch (iSubType)
          {
          case 2:
            if (rsCamp.getInt("nmCampaignType") == 4)
            {
              stReturn = stReturn + emailCampaignBlast(nmCampaignId, 100, rsCamp.getString("stProdCode"), 100, 120000);
            }
            else {
              int iMaxSend = Integer.parseInt(this.request.getParameter("s"));
              String stProdCode = this.request.getParameter("p");
              int iSleep = Integer.parseInt(this.request.getParameter("l"));
              int iMaxExe = Integer.parseInt(this.request.getParameter("x"));
              stReturn = stReturn + emailCampaignBlast(nmCampaignId, iMaxSend, stProdCode, iSleep, iMaxExe);
            }
            break;
          case 1:
            stReturn = stReturn + "</form><form method=post><table><tr><th colspan=2><h1>Campaign: \"" + rsCamp.getString("stTitle") + "\" - Email Queue Manager</h1></th></tr>";
            iQueueMax += this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from X25CampaignDetails where nmFlags = 0 and nmCampaignId=" + nmCampaignId);
            stReturn = stReturn + "<tr><td>Production Code or test email: </td><td><input type=text name=p value='RobertEder@myinfo.com'></td></tr>";
            stReturn = stReturn + "<tr><td>Select max # emails to be sent: </td><td><input type=text name=s value='" + iQueueMax + "'></td></tr>";
            stReturn = stReturn + "<tr><td>Sleep time (ms): </td><td><input type=text name=l value='300'></td></tr>";
            stReturn = stReturn + "<tr><td>Max Execution time (ms): </td><td><input type=text name=x value='600000'></td></tr>";
            stReturn = stReturn + "<tr><td colspan=2 align=center><input type=hidden name=a value='" + this.request.getParameter("a") + "'>" + "<input type=hidden name=b value='" + this.request.getParameter("b") + "'>" + "<input type=hidden name=cc value='2'>" + "<input type=submit name=submit9 value='Send'></td></tr>";

            stReturn = stReturn + "</table></form>";
            break;
          case 10:
          case 11:
          case 12:
            stReturn = stReturn + showCampList(iSubType, rsCamp);
            break;
          case 3:
          case 4:
          case 5:
          case 6:
          case 7:
          case 8:
          case 9:
          default:
            this.stError = (this.stError + "<BR>ERROR manageEmailQueue: iSubType not supportd " + iSubType);
          }
        }
      }

    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<BR>ERROR manageEmailQueue:  " + e);
    }
    return stReturn;
  }

  public String showCampList(int iSubType, ResultSet rsCamp)
  {
    String stReturn = "";
    String stTemp = "";
    String stSql = "";
    try
    {
      switch (iSubType)
      {
      case 10:
        stSql = "select * from X25Communications com, X25CampaignDetails cd , X25Person p where com.nmPersonid=p.RecId and com.nmRefType=42 and cd.nmRefType=42 and com.nmRefId=cd.nmRefId and (com.nmFlags & 0x00000600) != 0 and com.nmRefId != 5518 and cd.nmCampaignId=" + rsCamp.getString("nmCampaignId") + " and com.dtDate >= " + this.ebEnt.dbEnterprise.fmtDbString(rsCamp.getString("dtStart")) + " order by com.RecId desc, p.stLastName, p.stFirstName  ";

        break;
      case 11:
        stSql = "select * from X25Communications com, X25CampaignDetails cd , X25Person p where com.nmPersonid=p.RecId and com.nmRefType=42 and cd.nmRefType=42 and com.nmRefId=cd.nmRefId and (p.nmFlags & 0x00100000 ) != 0 and cd.nmCampaignId=" + rsCamp.getString("nmCampaignId") + " order by  com.RecId desc, p.stLastName, p.stFirstName ";

        break;
      case 12:
        stSql = "select * from X25Communications com, X25CampaignDetails cd , X25Person p where com.nmPersonid=p.RecId and com.nmRefType=42 and cd.nmRefType=42 and com.nmRefId=cd.nmRefId and (p.nmFlags & 0x00080000 ) != 0 and cd.nmCampaignId=" + rsCamp.getString("nmCampaignId") + " order by com.RecId desc, p.stLastName, p.stFirstName";

        stReturn = stReturn + "</a></td><tr>";
      }

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMaxP = rs.getRow();
        stReturn = stReturn + "<table>";
        for (int iP = 1; iP <= iMaxP; iP++)
        {
          rs.absolute(iP);
          stReturn = stReturn + "<tr>";
          stReturn = stReturn + "<td valign=top><a href='./?a=41&b=2&p=" + rs.getString("nmPersonId") + "'>" + rs.getString("stFirstName") + "</td>";

          stReturn = stReturn + "<td valign=top>" + rs.getString("stLastName") + "</td>";
          stReturn = stReturn + "<td valign=top>" + rs.getString("dtDate") + "</td>";
          stReturn = stReturn + "<td valign=top>" + rs.getString("stTitle") + "</td>";
          stTemp = rs.getString("stContent");
          if ((stTemp != null) && (stTemp.length() > 0))
          {
            stTemp = stTemp.trim().replace("<", "&lt;");
            int iLen = stTemp.length();
            if (iLen > 500)
            {
              stTemp = stTemp.substring(0, 500);
            }
          }
          else {
            stTemp = "&nbsp;";
          }
          stReturn = stReturn + "<td valign=top>" + stTemp + "</td>";
          stReturn = stReturn + "</tr>";
        }
        stReturn = stReturn + "</table>";
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR showCampList " + e);
    }
    return stReturn;
  }

  public String showCampaigns()
  {
    String stReturn = "";
    try
    {
      ResultSet rsCamp = this.ebEnt.dbEnterprise.ExecuteSql("select * from X25Campaign order by dtStart DESC");
      if (rsCamp != null)
      {
        rsCamp.last();
        int iMax = rsCamp.getRow();
        if (iMax > 0)
        {
          String stBg = "";
          stReturn = stReturn + "<br>&nbsp;<br><table bgcolor=blue cellpadding=1 cellspacing=2><tr><th bgcolor=white>ID</td><th bgcolor=white>Start</th><th bgcolor=white>Title</th><th bgcolor=white>Type</th><th bgcolor=white>Boiler</th><th bgcolor=white>Queue</th><th bgcolor=white>Sent</th><th bgcolor=white>Err</th><th bgcolor=white>Persons</th><th bgcolor=white>Replied</th><th bgcolor=white>DNC</th><th bgcolor=white>Prospect</th><th bgcolor=white>Customer</th></tr>";

          for (int iC = 1; iC <= iMax; iC++)
          {
            rsCamp.absolute(iC);
            if (iC % 2 != 0)
            {
              stBg = " bgcolor='#DDDDDD' ";
            }
            else {
              stBg = " bgcolor='white' ";
            }

            stReturn = stReturn + "<tr>";
            stReturn = stReturn + "<td " + stBg + " align=right><a href='./?a=28&tn=540&tid=" + rsCamp.getString("nmCampaignId") + "'>" + rsCamp.getString("nmCampaignId") + "</a></td>";
            stReturn = stReturn + "<td " + stBg + " nowrap>" + rsCamp.getString("dtStart") + "</td>";
            stReturn = stReturn + "<td " + stBg + " nowrap><b>" + rsCamp.getString("stTitle") + "</b></td>";
            stReturn = stReturn + "<td " + stBg + " align=right>" + rsCamp.getString("nmCampaignType") + "</td>";
            stReturn = stReturn + "<td " + stBg + " align=right><a href='./?a=28&tn=22&tid=" + rsCamp.getString("nmBoilerId") + "'>" + rsCamp.getString("nmBoilerId") + "</a></td>";
            stReturn = stReturn + "<td " + stBg + " align=right><a href='./?a=" + this.request.getParameter("a") + "&b=2&c=1&d=" + rsCamp.getString("nmCampaignId") + "'>";
            stReturn = stReturn + this.ebEnt.dbEnterprise.ExecuteSql1(new StringBuilder().append("select count(*) from X25CampaignDetails where nmFlags = 0 and nmCampaignId=").append(rsCamp.getString("nmCampaignId")).toString());
            stReturn = stReturn + "</a></td><td " + stBg + " align=right>";
            stReturn = stReturn + this.ebEnt.dbEnterprise.ExecuteSql1(new StringBuilder().append("select count(*) from X25CampaignDetails where nmFlags != 0 and nmCampaignId=").append(rsCamp.getString("nmCampaignId")).toString());
            stReturn = stReturn + "</td><td " + stBg + " align=right>";
            stReturn = stReturn + this.ebEnt.dbEnterprise.ExecuteSql1(new StringBuilder().append("select count(*) from X25CampaignDetails where nmFlags = 4 and nmCampaignId=").append(rsCamp.getString("nmCampaignId")).toString());
            stReturn = stReturn + "</td><td " + stBg + " align=right>";

            stReturn = stReturn + this.ebEnt.dbEnterprise.ExecuteSql1(new StringBuilder().append("select count(distinct p.RecId) from X25Person p, X25RefUser ru, X25CampaignDetails cd where p.RecId = ru.nmPersonId and ru.nmRefType=1 and ru.nmUserId=cd.nmRefId and cd.nmRefType=42 and cd.nmFlags = 2 and cd.nmCampaignId=").append(rsCamp.getString("nmCampaignId")).toString());

            stReturn = stReturn + "</td><td " + stBg + " align=right><a href='./?a=" + this.request.getParameter("a") + "&b=2&c=10&d=" + rsCamp.getString("nmCampaignId") + "'>";
            stReturn = stReturn + this.ebEnt.dbEnterprise.ExecuteSql1(new StringBuilder().append("select count(distinct com.nmRefId) from X25Communications com, X25CampaignDetails cd where com.nmRefType=42 and cd.nmRefType=42 and com.nmRefId=cd.nmRefId and com.dtDate >= ").append(this.ebEnt.dbEnterprise.fmtDbString(rsCamp.getString("dtStart"))).append("and (com.nmFlags & 0x00000600) != 0 and cd.nmCampaignId=").append(rsCamp.getString("nmCampaignId")).toString());

            stReturn = stReturn + "</a></td><td " + stBg + " align=right>";
            stReturn = stReturn + this.ebEnt.dbEnterprise.ExecuteSql1(new StringBuilder().append("SELECT count(distinct dnc.stValue) FROM X25CampaignDetails cd, X25DoNotCall dnc, X25User u where cd.nmRefType=42 and cd.nmRefId=u.RecId and u.stEMail = dnc.stValue and cd.nmCampaignId=").append(rsCamp.getString("nmCampaignId")).toString());

            stReturn = stReturn + "</a></td><td " + stBg + " align=right><a href='./?a=" + this.request.getParameter("a") + "&b=2&c=11&d=" + rsCamp.getString("nmCampaignId") + "'>";
            stReturn = stReturn + this.ebEnt.dbEnterprise.ExecuteSql1(new StringBuilder().append("select count(distinct p.RecId) from X25Person p, X25RefUser ru, X25CampaignDetails cd where p.RecId = ru.nmPersonId and ru.nmRefType=1 and ru.nmUserId=cd.nmRefId and cd.nmRefType=42 and (p.nmFlags & 0x00100000) != 0 and cd.nmCampaignId=").append(rsCamp.getString("nmCampaignId")).toString());

            stReturn = stReturn + "</a></td><td " + stBg + " align=right><a href='./?a=" + this.request.getParameter("a") + "&b=2&c=12&d=" + rsCamp.getString("nmCampaignId") + "'>";
            stReturn = stReturn + this.ebEnt.dbEnterprise.ExecuteSql1(new StringBuilder().append("select count(distinct p.RecId) from X25Person p, X25RefUser ru, X25CampaignDetails cd where p.RecId = ru.nmPersonId and ru.nmRefType=1 and ru.nmUserId=cd.nmRefId and cd.nmRefType=42 and (p.nmFlags & 0x00080000) != 0 and cd.nmCampaignId=").append(rsCamp.getString("nmCampaignId")).toString());

            stReturn = stReturn + "</a></td><tr>";
          }
          stReturn = stReturn + "</table>";
        }
        else {
          stReturn = stReturn + "<BR>ERROR: no campaigns (count)";
        }
      }
      else {
        stReturn = stReturn + "<BR>ERROR: no campaigns (null)";
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR manageCampaign:  " + e);
    }
    return stReturn;
  }

  public String addCampaign(int iType)
  {
    int iOrder = 0;
    int iCompanyCount = 0;
    String stSql = "";

    int nmCampaignId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(nmCampaignId) from X25Campaign");
    nmCampaignId++;

    String stReturn = "<center><h1>Added campaign ID: " + nmCampaignId + "</h1>";
    stReturn = stReturn + "<h1>" + this.request.getParameter("stSubject") + "</h1><table border=1>";
    String stStart = "";
    String stEnd = "";

    if ((this.request.getParameter("dtStart") != null) && (this.request.getParameter("dtStart").length() > 9))
    {
      stStart = this.ebEnt.dbEnterprise.fmtDbString(this.request.getParameter("dtStart"));
    }
    else {
      stStart = "now()";
    }
    if ((this.request.getParameter("dtEnd") != null) && (this.request.getParameter("dtEnd").length() > 9))
    {
      stEnd = this.ebEnt.dbEnterprise.fmtDbString(this.request.getParameter("dtStart"));
    }
    else {
      stEnd = "now()";
    }
    this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25Campaign (nmCampaignId,dtStart,dtEnd,stTitle,nmBoilerId,nmCampaignType,stDescription,stStatusList,stProfessionList,stProdCode,stFromName,stFromEmail,stBlastLog) values(" + nmCampaignId + "," + stStart + "," + stEnd + "," + this.ebEnt.dbEnterprise.fmtDbString(this.request.getParameter("stTitle")) + "," + this.request.getParameter("nmBoilerId") + "," + this.request.getParameter("nmCampaignType") + "," + this.ebEnt.dbEnterprise.fmtDbString(this.request.getParameter("stDescription")) + "," + this.ebEnt.dbEnterprise.fmtDbString(this.request.getParameter("stStatusList")) + "," + this.ebEnt.dbEnterprise.fmtDbString(this.request.getParameter("stProfessionList")) + "," + this.ebEnt.dbEnterprise.fmtDbString(this.request.getParameter("stProdCode")) + "," + this.ebEnt.dbEnterprise.fmtDbString(this.request.getParameter("stFromName")) + "," + this.ebEnt.dbEnterprise.fmtDbString(this.request.getParameter("stFromEmail")) + ",'' )");

    if (iType != 4)
    {
      String stTemp = this.request.getParameter("stProfessionList");
      if (stTemp == null)
      {
        stTemp = "";
      }
      if (stTemp.equals("0"))
      {
        stSql = "FROM X25RefUser ru, X25Person p where ru.nmPersonId=p.RecId and ru.nmRefType=1 " + this.request.getParameter("stFilter");
      }
      else
      {
        stSql = "FROM X25RefUser ru, X25Person p left join X25Education ed on ed.nmPersonId=p.RecId and ed.nmProfession in (" + stTemp + " ) " + "where ru.nmPersonId=p.RecId and ru.nmRefType=1 and ed.RecId is not null " + this.request.getParameter("stFilter");
      }

      this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25CampaignDetails (nmCampaignId,nmRefType,nmRefId,dtEntered,dtLastUpdated) select distinct " + nmCampaignId + ",42,ru.nmUserId,now(),now() " + stSql);

      this.ebEnt.dbEnterprise.ExecuteUpdate("delete from X25CampaignDetails where nmRefType=42 and nmCampaignId=" + nmCampaignId + " and nmRefId in " + "(select distinct u.RecId from X25User u, X25DoNotCall dnc where u.stEMail=dnc.stValue)");

      stReturn = stReturn + "<tr><td>Total emails for campaign: </td><td align=right>" + this.ebEnt.dbEnterprise.ExecuteSql1(new StringBuilder().append("select count(*) from X25CampaignDetails where nmCampaignId=").append(nmCampaignId).append(" and nmRefType=42").toString());
      try
      {
        ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql("SELECT rc.nmCompanyId,ru.nmUserId FROM X25CampaignDetails cd, X25RefUser ru, X25RefCompany rc where cd.nmCampaignId=" + nmCampaignId + " and cd.nmRefType=42 and cd.nmCompanyId=0 and cd.nmRefId=ru.nmUserId and ru.nmRefType=1 and ru.nmPersonId = rc.nmRefId and rc.nmRefType=1 order by nmCompanyId, nmUserId desc");
        if (rs != null)
        {
          rs.last();
          int iMax = rs.getRow();
          String stLast = "";
          for (int iCd = 1; iCd <= iMax; iCd++)
          {
            rs.absolute(iCd);
            if (!stLast.equals(rs.getString("nmCompanyId")))
            {
              iOrder = 0;
              iCompanyCount++;
            }
            iOrder++;
            this.ebEnt.dbEnterprise.ExecuteUpdate("update X25CampaignDetails set nmCompanyId=" + rs.getString("nmCompanyId") + ",nmOrder=" + iOrder + " where nmCampaignId=" + nmCampaignId + " and nmRefType=42 and nmRefId=" + rs.getString("nmUserId"));
            stLast = rs.getString("nmCompanyId");
          }
        }
        rs.close();
        rs = this.ebEnt.dbEnterprise.ExecuteSql("SELECT ru.nmPersonId as nmCompanyId,ru.nmUserId FROM X25CampaignDetails cd, X25RefUser ru where cd.nmCampaignId=" + nmCampaignId + " and cd.nmRefType=42 and cd.nmCompanyId=0 and cd.nmRefId=ru.nmUserId and ru.nmRefType=2 order by ru.nmPersonId, ru.nmUserId desc");
        if (rs != null)
        {
          rs.last();
          int iMax = rs.getRow();
          String stLast = "";
          iOrder = 10000;
          for (int iCd = 1; iCd <= iMax; iCd++)
          {
            rs.absolute(iCd);
            if (!stLast.equals(rs.getString("nmCompanyId")))
            {
              iOrder = 10000;
              iCompanyCount++;
            }
            iOrder++;
            this.ebEnt.dbEnterprise.ExecuteUpdate("update X25CampaignDetails set nmCompanyId=" + rs.getString("nmCompanyId") + ",nmOrder=" + iOrder + " where nmCampaignId=" + nmCampaignId + " and nmRefType=42 and nmRefId=" + rs.getString("nmUserId"));
            stLast = rs.getString("nmCompanyId");
          }
        }
      }
      catch (Exception e) {
        this.stError = (this.stError + "<BR>ERROR addCampaign: " + e);
      }
      stReturn = stReturn + "</td></tr><tr><td>Total Companies: </td><td align=right>" + iCompanyCount + " </td></tr>";
      stReturn = stReturn + "</td></tr></table></center>";
    }
    return stReturn;
  }

  public String emailCampaignBlast(int nmCampaignId, int iMaxSend, String stProdCode, int iSleep, int iMaxExe)
  {
    int iEmailCount = 0;
    String stReturn = "<h1>Email blast</h1>";
    String stSql = "";
    long lTime1 = 0L;
    long lTime2 = 0L;
    int nmCampaignType = 0;
    try
    {
      Calendar cal1 = Calendar.getInstance();
      lTime1 = cal1.getTimeInMillis();
      stSql = "select * from X25Campaign c, X25Boiler b where c.nmCampaignId=" + nmCampaignId + " and c.nmBoilerId=b.RecId limit 100";
      ResultSet rsCamp = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rsCamp != null)
      {
        rsCamp.last();
        if (rsCamp.getRow() == 1)
        {
          rsCamp.absolute(1);
        }
        else
          this.stError = (this.stError + "<BR>ERROR emailCampaignBlast " + stSql);
      }
      else
      {
        this.stError = (this.stError + "<BR>ERROR emailCampaignBlast (null) " + stSql);
      }

      if (rsCamp.getInt("nmCampaignType") != 4)
      {
        stSql = "select u.stEMail,u.nmPriviledge,cd.nmCompanyId,cd.nmRefId,com.RecId as comId,p.stFirstName, p.stLastName, p.stMiddleName from X25User u left join X25RefUser ru on ru.nmUserId=u.RecId and ru.nmRefType=1 left join X25Person p on ru.nmPersonId=p.RecId, X25CampaignDetails cd left join X25Communications com on com.RecId = cd.nm1stCommId where cd.nmCampaignId=" + nmCampaignId + " and cd.nmFlags=0 and cd.nmRefType=42 " + "and cd.nmCompanyid > 0 and cd.nmRefId=u.RecId ";

        if (nmCampaignType == 2)
        {
          stSql = stSql + " order by nmCompanyId desc";
        }
        else {
          stSql = stSql + " order by u.RecId desc";
        }
      }
      else
      {
        stSql = "select stURL, stWebContent, ws.stEmails as stEMail,nmCompanyId, stDescription, nmSpiderId, nmSpiderId as nmRefId from X25WebSpider ws where stError='cl' and nmAnalyzeCount > 0 and nmStatus = 1 and nmSpiderId >= 14434 order by nmSpiderId desc limit " + iMaxSend;

        stSql = "select stURL, stWebContent, ws.stEmails as stEMail,nmCompanyId, stDescription, nmSpiderId, nmSpiderId as nmRefId from X25WebSpider ws where stError='cl' and nmAnalyzeCount > 0 and nmStatus = 1 and stEMails not like '%@craigslist.org%' order by nmSpiderId desc limit " + iMaxSend;

        stSql = "select stURL, stWebContent, ws.stEmails as stEMail,nmCompanyId, stDescription, nmSpiderId, nmSpiderId as nmRefId from X25WebSpider ws where nmSpiderId=14441 order by nmSpiderId desc limit " + iMaxSend;

        stSql = "select stURL, stWebContent, ws.stEmails as stEMail,nmCompanyId, stDescription, nmSpiderId, nmSpiderId as nmRefId from X25WebSpider ws where nmSpiderId=14441 order by nmSpiderId desc limit " + iMaxSend;

        stSql = "select stURL, stWebContent, ws.stEmails as stEMail,nmCompanyId, stDescription, nmSpiderId, nmSpiderId as nmRefId from X25WebSpider ws where stError='cl' and nmAnalyzeCount > 0 and nmStatus = 1 and stEMails not like '%@craigslist.org%' and stEMails like '%@%.%' order by nmSpiderId desc limit " + iMaxSend;
      }

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        stReturn = stReturn + "<br><h2>" + rsCamp.getString("stTitle") + " [" + iMax + "] </h2>";
        nmCampaignType = rsCamp.getInt("nmCampaignType");
        String stLastSent = "";
        for (int iE = 1; (iE <= iMax) && (iEmailCount < iMaxSend); iE++)
        {
          rs.absolute(iE);

          if (nmCampaignType == 2)
          {
            String stRecId = rs.getString("comId");
            if ((stRecId == null) || (stRecId.length() <= 0))
            {
              if (!stLastSent.equals(rs.getString("nmCompanyId")))
              {
                iEmailCount++;
                sendQueuedEmail(rs, rsCamp);
                stLastSent = rs.getString("nmCompanyId");
              }
            }
          } else if ((nmCampaignType == 3) || (nmCampaignType == 4))
          {
            iEmailCount++;
            stReturn = stReturn + sendQueuedEmail(rs, rsCamp);
          }
          else {
            this.stError = (this.stError + "<BR>ERROR emailCampaignBlast: Invalid nmCampaignType=" + nmCampaignType);
            break;
          }
          Thread.sleep(iSleep);
          Calendar cal2b = Calendar.getInstance();
          lTime2 = cal2b.getTimeInMillis();
          if (lTime2 - lTime1 <= iMaxExe)
            continue;
          stReturn = stReturn + "<BR>MAX EXEC TIMEOUT at pos: " + iE + " or max: " + iMaxSend;
          break;
        }
      }
      else
      {
        this.stError = (this.stError + "<BR>ERROR emailCampaignBlast (null) " + stSql);
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>ERROR emailCampaignBlast: " + e);
    }

    Calendar cal2 = Calendar.getInstance();
    lTime2 = cal2.getTimeInMillis();

    stSql = "update X25Campaign set stBlastLog = concat(stBlastLog, " + this.ebEnt.dbEnterprise.fmtDbString(new StringBuilder().append("\n<BR>").append(cal2.getTime().toLocaleString()).append(" \tmax: ").append(iMaxSend).append(" \tcode: ").append(stProdCode).append(" \tsleep: ").append(iSleep).append(" t/o: ").append(iMaxExe).append(" \tsent: +").append(iEmailCount).append(" \ttime: ").append(lTime2 - lTime1).toString()) + ") where nmCampaignId=" + nmCampaignId;

    this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
    stReturn = stReturn + "<BR>EmailCount " + iEmailCount;
    return stReturn;
  }

  public String sendQueuedEmail(ResultSet rsUser, ResultSet rsCamp)
  {
    String stReturn = "";
    String stTemp = "";
    int nmFlags = 0;
    int nmRefType = 0;
    try
    {
      String stSubject = rsCamp.getString("stSubject").trim();
      String stBody = rsCamp.getString("stBody").trim();

      EbMail ebM = new EbMail(this.ebEnt);
      int iStatus = -1;
      if (rsCamp.getInt("nmCampaignType") == 4)
      {
        ebM.setProduction(1);
        stTemp = rsUser.getString("stDescription");
        if ((stTemp != null) && (stTemp.length() > 0))
        {
          stSubject = stTemp;
        }
        stBody = stBody.replace("~~CLTITLE", stSubject);
        stTemp = rsUser.getString("stWebContent");
        if ((stTemp != null) && (stTemp.length() > 10))
        {
          int iPos1 = stTemp.indexOf("<div id=\"userbody\">");
          int iPos2 = stTemp.indexOf("<!-- START CLTAGS -->", iPos1);
          if ((iPos1 > 0) && (iPos2 > iPos1))
          {
            stBody = stBody.replace("~~CLAD", stTemp.substring(iPos1, iPos2));
          }
        }

        stBody = stBody.replace("~~CLURL", rsUser.getString("stURL"));

        iStatus = ebM.sendMailCL(rsUser.getString("stEMail"), "", stSubject, stBody, rsCamp.getInt("nmCampaignId"));
        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25WebSpider set nmStatus=2 where nmSpiderId=" + rsUser.getString("nmSpiderId"));
        stReturn = stReturn + "<br>" + iStatus + " \t" + rsUser.getString("stEMail").trim() + "\t spider: \t" + rsUser.getString("nmSpiderId");
        nmRefType = 76;
        stBody = ebM.getBody();
      }
      else {
        stTemp = this.request.getParameter("p");
        ebM.setProduction(-1);
        if ((stTemp != null) && (stTemp.length() > 0))
        {
          if (stTemp.equals(rsCamp.getString("stProdCode")))
          {
            ebM.setProduction(1);
          }
          else {
            ebM.setTestEmail(stTemp);
          }
        }
        iStatus = ebM.sendMail(rsUser, rsCamp.getString("nmCampaignId"), stSubject, stBody);
        stBody = ebM.getBody();
        nmRefType = 42;
      }
      this.stError += ebM.getError();

      if (iStatus >= 0)
      {
        nmFlags = 2;
      }
      else {
        nmFlags = 4;
      }
      int iRecId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from X25Communications");
      iRecId++;

      String stSql = "insert into X25Communications (RecId,nmRefType,nmRefId,nmFlags,dtDate,stTitle,stContent,nmBoilerId) values(" + iRecId + "," + nmRefType + "," + rsUser.getString("nmRefId") + "," + nmFlags + ",now()," + this.ebEnt.dbEnterprise.fmtDbString(stSubject) + "," + this.ebEnt.dbEnterprise.fmtDbString(stBody) + "," + rsCamp.getString("nmBoilerId") + ")";

      this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
      stTemp = rsUser.getString("nmRefId");
      this.ebEnt.dbEnterprise.ExecuteUpdate("update X25CampaignDetails set nm1stCommId=" + iRecId + ",nmFlags=" + nmFlags + ",dtLastUpdated=now() where nmCampaignId=" + rsCamp.getString("nmCampaignId") + " and nmRefType=42 and nmRefId=" + stTemp);
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<br>ERROR sendQueuedEmail: " + e);
    }
    return stReturn;
  }

  public String getError()
  {
    return this.stError;
  }
}