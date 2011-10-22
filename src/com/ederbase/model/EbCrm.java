package com.ederbase.model;

import java.io.StringReader;
import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;

public class EbCrm
{
  String stError = "";
  EbEnterprise ebEnt = null;
  String stEmail = "";

  public EbCrm()
  {
    this.stError = "";
    this.ebEnt = null;
  }

  public String emailJunk(EbEnterprise ebEnt, HttpServletRequest request)
  {
    String stReturn = "";
    String stSql = "";
    String stTemp = "";

    this.ebEnt = ebEnt;
    try
    {
      String[] astJunk = request.getParameterValues("junk");
      if ((astJunk != null) && (astJunk.length > 0))
      {
        for (int iJ = 0; iJ < astJunk.length; iJ++)
        {
          ebEnt.dbEnterprise.ExecuteUpdate("update X25Person set nmFlags = ( nmFlags | 0x00010000 ) where RecId=" + astJunk[iJ]);
        }
      }
      astJunk = request.getParameterValues("lowprio");
      if ((astJunk != null) && (astJunk.length > 0))
      {
        for (int iJ = 0; iJ < astJunk.length; iJ++)
        {
          ebEnt.dbEnterprise.ExecuteUpdate("update X25Person set nmFlags = ( nmFlags | 0x00008000 ) where RecId=" + astJunk[iJ]);
        }
      }
      stReturn = stReturn + "<h1>Processing emailJunk</h1>";
      stSql = "SELECT * FROM X25User u, X25Communications com left join X25Person p on p.RecId=com.nmPersonId where u.RecId=com.nmRefId and com.nmRefType=42 and ( com.nmFlags & 0x7 ) = 0 and ( p.RecId is null or ( p.nmFlags & 0x0FFF8001 ) = 1 ) order by com.RecId desc limit 100;";
      ResultSet rs = ebEnt.dbEnterprise.ExecuteSql(stSql);
      stReturn = stReturn + "</form><form method=post><table border =1>";
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        for (int iC = 1; iC <= iMax; iC++)
        {
          rs.absolute(iC);
          stReturn = stReturn + "<tr>";
          stReturn = stReturn + "<td>" + rs.getString("stEMail");
          stReturn = stReturn + "<br>&nbsp;&nbsp;" + rs.getString("stFirstName") + " ";
          stTemp = rs.getString("stLastName");
          if (stTemp != null)
          {
            if (stTemp.length() > 20)
              stTemp = stTemp.substring(0, 20);
          }
          if ((stTemp == null) || (stTemp.equals("")))
            stTemp = "&nbsp;";
          stReturn = stReturn + stTemp + "</td>";
          stReturn = stReturn + "<td>" + rs.getString("dtDate") + "</td>";
          stReturn = stReturn + "<td>" + rs.getString("stTitle") + "</td>";
          stReturn = stReturn + "<td><input type=checkbox name=lowprio value=" + rs.getString("nmPersonId") + "></td>";
          stReturn = stReturn + "<td><input type=checkbox name=junk value=" + rs.getString("nmPersonId") + "></td>";
          stTemp = rs.getString("stContent");
          if (stTemp != null)
          {
            try
            {
              StringReader in = new StringReader(stTemp);
              Html2Text parser = new Html2Text();
              parser.parse(in);
              in.close();
              stTemp = parser.getText();
            }
            catch (Exception e) {
              stTemp = stTemp.replace("<", "&lt;");
            }
            if (stTemp.length() > 300)
              stTemp = stTemp.substring(0, 300);
          } else {
            stTemp = "&nbsp;";
          }stReturn = stReturn + "<td>" + stTemp + "</td>";
          stReturn = stReturn + "</tr>";
        }
        stReturn = stReturn + "<tr><td colspan=5 align=center><input type=submit name=submit9 value='Mark Junk'></tr>";
      }
      else {
        stReturn = stReturn + "<tr><td>ERROR: no results: " + stSql + "</td></tr>";
      }
      stReturn = stReturn + "</table>";
    }
    catch (Exception e) {
      stReturn = stReturn + "<BR>ERROR emailJunk: " + e;
      this.stError = (this.stError + "<BR>ERROR emailJunk: " + e);
    }
    return stReturn;
  }

  public String emailBulkReply(EbEnterprise ebEnt, HttpServletRequest request)
  {
    String stReturn = "";
    String stTemp = "";
    int nmCommId = -1;
    int nmPersonId = 0;

    this.ebEnt = ebEnt;

    int iState = this.ebEnt.getState("b", request);
    try
    {
      stReturn = stReturn + "<BR>TODO emailBulkReply";
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR emailBulkReply: " + e);
    }
    return stReturn;
  }

  public String doSearch(EbEnterprise ebEnt, HttpServletRequest request)
  {
    String stReturn = "";
    String stTemp = "";
    int nmCommId = -1;
    int nmPersonId = 0;

    this.ebEnt = ebEnt;

    int iState = this.ebEnt.getState("b", request);
    try
    {
      stTemp = EbStatic.myString(request.getParameter("submit9"));
      if (stTemp.equals("Save"))
      {
        iState = 1;
        nmPersonId = EbStatic.myInteger(request, "p");
        if (nmPersonId > 0)
        {
          int nmFlags = EbStatic.myInteger(request, "nmFlags");
          String stTo = EbStatic.myString(request, "tomail");
          String stSubject = EbStatic.myString(request, "subject");
          String stBody = EbStatic.myString(request, "ebody");
          String stComment = EbStatic.myString(request, "comment");
          this.ebEnt.dbEnterprise.ExecuteUpdate("update X25Person set nmFlags=" + nmFlags + " where RecId=" + nmPersonId);
          if ((stTo.length() > 4) && (stSubject.length() > 0) && (stBody.length() > 0))
          {
            int nmUserId = this.ebEnt.ebNorm.getEmailId(stTo);
            if (nmUserId > 0)
            {
              this.ebEnt.ebNorm.addUserRef(nmUserId, 1, nmPersonId);
              ResultSet rsUser = this.ebEnt.dbEnterprise.ExecuteSql("select * from X25User u, X25RefUser ru, X25Person p where p.RecId=ru.nmPersonId and ru.nmRefType=1 and ru.nmUserId=u.RecId and u.RecId=" + nmUserId);

              rsUser.absolute(1);
              EbMail ebM = new EbMail(this.ebEnt);
              ebM.setProduction(1);
              nmCommId = ebM.sendMail(rsUser, "0", stSubject, stBody);
              if (nmCommId < 0)
                stComment = stComment + " ERROR SENDING EMAIL TO: " + stTo;
            }
            else {
              stComment = stComment + " ERROR SENDING EMAIL TO (2): " + stTo;
            }
          }
          if (nmCommId > 0)
          {
            stComment = stComment + " Sent email to: " + stTo;
            this.ebEnt.ebNorm.addHistory(1, nmPersonId, "", stComment, 11, nmCommId);
          } else if (stComment.length() > 0)
          {
            this.ebEnt.ebNorm.addHistory(1, nmPersonId, "", stComment, 0, 0);
          }
        }
      } else if (stTemp.equals("Cancel"))
      {
        iState = 1;
      }

      switch (iState)
      {
      case 1:
        stReturn = stReturn + "<p align=left><form method=post id=\"form2\" name=\"form2\"><table border=0 width='100%'><tr><td valign=top>";

        stReturn = stReturn + searchMenuResult(1);
        stReturn = stReturn + "</td><td valign=top>";
        stReturn = stReturn + callBack(1);
        stReturn = stReturn + recentHistory(1);
        stReturn = stReturn + "</td></tr></table>";
        break;
      case 2:
        stReturn = stReturn + "<p align=left><form method=post id=\"form2\" name=\"form2\"><table border=0 width='100%'><tr><td valign=top>";

        stReturn = stReturn + doEditPerson();
        stReturn = stReturn + "</td></tr></table>";
        break;
      case 10:
        stReturn = stReturn + showHistoryDetail(Integer.parseInt(request.getParameter("h")), 1);
        break;
      case 11:
        stReturn = stReturn + showHistoryDetail(Integer.parseInt(request.getParameter("h")), 2);
        break;
      default:
        stReturn = stReturn + "<BR>ERROR doSearch: invalid state/process: " + this.ebEnt.getState("b", request);
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<br>ERROR doSearch" + e);
    }
    return stReturn;
  }

  public String showHistoryDetail(int iRecId, int iType)
  {
    String stReturn = "";
    String stTemp = "";

    if (iType == 1)
      stReturn = "<table border=0 width='100%'>";
    try
    {
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql("select * from X25HistoryLog h left join X25Communications com on com.RecId = h.nmRefId2 and h.nmRefType2=11 where h.RecId=" + iRecId);

      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        if (iMax == 1)
        {
          if (iType == 1)
          {
            stReturn = stReturn + "<tr><td>History: <a href='./?a=28&tn=27&tid=" + iRecId + "'>" + rs.getString("RecId") + "</a></td>";
            if (rs.getInt("nmRefType2") == 11)
              stReturn = stReturn + "<td>Communications: <a href='./?a=28&tn=23&tid=" + rs.getString("nmRefId2") + "'>" + rs.getString("nmRefId2") + "</a></td>";
            stReturn = stReturn + "</tr>";
          }
          else {
            stTemp = rs.getString("stHtml");
            if ((stTemp == null) || (stTemp.length() <= 0))
            {
              stTemp = rs.getString("stContent");
              if ((stTemp == null) || (stTemp.length() < 10) || (stTemp.indexOf("<html>") < 0))
                stTemp = "";
            }
            if ((stTemp != null) && (stTemp.trim().length() > 4))
            {
              stReturn = stReturn + "<br><b>" + EbStatic.myString(rs.getString("dtEventStartTime")) + " &nbsp;&nbsp;&nbsp;&nbsp;" + EbStatic.myString(rs.getString("stTitle")) + "</b><hr><br>&nbsp;";

              stReturn = stReturn + stTemp;
            }
            else {
              stReturn = stReturn + "<tr><td>";
              stReturn = stReturn + "<br><b>" + EbStatic.myString(rs.getString("dtEventStartTime")) + " &nbsp;&nbsp;&nbsp;&nbsp;" + EbStatic.myString(rs.getString("stTitle")) + "</b><hr><br>&nbsp;<pre>";

              stTemp = rs.getString("stContent");
              if ((stTemp != null) && (stTemp.length() > 0))
                stReturn = stReturn + stTemp.trim().replace("<", "&lt;");
              stReturn = stReturn + "</pre></td>></tr>";
            }
          }
        }
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR doEditPerson: " + e);
    }
    if (iType == 1) {
      stReturn = stReturn + "</table>";
    }
    return stReturn;
  }

  public String doEditPerson()
  {
    int nmPersonId = 0;
    String stReturn = "<table border=0 width='100%'>";
    try
    {
      nmPersonId = Integer.parseInt(this.ebEnt.ebUd.getRequest("p"));
      if (nmPersonId > 0)
      {
        stReturn = stReturn + "<tr><td valign=top width='50%'>";
        stReturn = stReturn + showPerson(nmPersonId);
        stReturn = stReturn + "</td>";
        stReturn = stReturn + "<td valign=top width='50%'>";
        stReturn = stReturn + showCompany(nmPersonId);
        stReturn = stReturn + "</td>";
        stReturn = stReturn + "</tr>";
        stReturn = stReturn + "<tr><td>EM:<input type=text name=tomail value=\"" + this.stEmail + "\"> Subj:<input type=text name=subject value=\"\" size=60><br>" + "<textarea name=ebody rows=5 cols=90></textarea></td>" + "<td valign=top>" + editPersonStatus(nmPersonId) + "<br>" + "<textarea name=comment rows=2 cols=90></textarea><br>" + "<input type=submit name=submit9 value='Save'> <input type=submit name=submit9 value='Cancel'> </td>" + "</tr>";

        stReturn = stReturn + "<tr><td colspan=2>" + showHistory(nmPersonId) + "</td></tr>";
      }
      else {
        this.stError += "<BR>ERROR doEditPerson: invalid person id";
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR doEditPerson: " + e);
    }
    stReturn = stReturn + "</table>";

    return stReturn;
  }

  public String editPersonStatus(int nmPersonId)
  {
    String stReturn = "";
    String stChecked = "";
    try
    {
      int iValue = 1;
      int nmFlags = this.ebEnt.dbEnterprise.ExecuteSql1n("select nmFlags from X25Person where RecId=" + nmPersonId);
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> Active | ";
      iValue = 65536;
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> Junk | ";
      iValue = 131072;
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> Hide | ";
      iValue = 262144;
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> Friend | ";
      iValue = 524288;
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> Customer | ";
      iValue = 1048576;
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> Prospect | ";
      iValue = 2097152;
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> Private ";
      iValue = 4194304;
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<br><input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> QW | ";
      iValue = 8388608;
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> Cgn | ";
      iValue = 16777216;
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> Cgn+ | ";
      iValue = 33554432;
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> Cgn- | ";
      iValue = 67108864;
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> DNCgn | ";
      iValue = 134217728;
      if ((iValue & nmFlags) != 0)
        stChecked = " checked ";
      else
        stChecked = "";
      stReturn = stReturn + "<input name=nmFlags type=checkbox value='" + iValue + "' " + stChecked + "> Aff | ";
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR editPersonStatus: " + e);
    }

    return stReturn;
  }

  public String showCompany(int nmPersonId)
  {
    String stSql = "";
    String stReturn = "<table border=0 width='100%'>";
    int iMax = 0;
    String stTemp = "";
    try
    {
      stSql = "select * from X25Company c, X25RefCompany rc  where rc.nmCompanyId=c.RecId and rc.nmRefType = 1 and rc.nmRefId=" + nmPersonId;

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        iMax = rs.getRow();
        if (iMax == 1)
        {
          stReturn = stReturn + "<tr><th colspan=2 bgcolor=skyblue><h2>";
          stReturn = stReturn + rs.getString("stCompanyName") + "</h2></td>";
          stReturn = stReturn + "</tr>";
          stTemp = showPhone(2, rs.getInt("RecId"));
          if (stTemp.length() > 0)
            stReturn = stReturn + "<tr><td align=right valign=top>Phones: </td><td>" + stTemp + "</td></tr>";
          stTemp = showEmail(2, rs.getInt("RecId"));
          if (stTemp.length() > 0) {
            stReturn = stReturn + "<tr><td align=right valign=top>Emails: </td><td>" + stTemp + "</td></tr>";
          }
          stTemp = showAddress(2, rs.getInt("RecId"));
          if (stTemp.length() > 0)
            stReturn = stReturn + "<tr><td align=left colspan=2>" + stTemp + "</td></tr>";
          stTemp = showWeb(2, rs.getInt("RecId"));
          if (stTemp.length() > 0)
            stReturn = stReturn + "<tr><td align=left colspan=2>" + stTemp + "</td></tr>";
        }
        else {
          this.stError = (this.stError + "<BR>ERROR showCompany: invalid records " + iMax);
        }
      } else {
        this.stError = (this.stError + "<BR>ERROR showCompany: " + nmPersonId);
      }
    } catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR showCompany: " + e);
    }
    if (iMax <= 0)
      stReturn = stReturn + "<tr><td bgcolor=skyblue>&nbsp;</td></tr>";
    stReturn = stReturn + "</table>";

    return stReturn;
  }

  public String showHistory(int nmPersonId)
  {
    String stSql = "";
    String stReturn = "<table border=0 width='100%'>";
    int iMax = 0;
    String stComment = "";
    try
    {
      stSql = "select h.* ,com.* from X25HistoryLog h left join X25Communications com on h.nmRefType2=11 and h.nmRefId2=com.RecId where h.nmRefType = 1 and h.nmRefId = " + nmPersonId + " " + "order by h.dtEventStartTime desc";

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(this.ebEnt.dbEnterprise.setSqlLimit(stSql, "200"));
      if (rs != null)
      {
        rs.last();
        iMax = rs.getRow();
        if (iMax > 0)
        {
          for (int iH = 1; iH <= iMax; iH++)
          {
            rs.absolute(iH);
            stReturn = stReturn + "<tr><td nowrap><a href='./?a=" + this.ebEnt.ebUd.request.getParameter("a") + "&b=11&h=" + rs.getString("RecId") + "'>" + rs.getString("dtEventStartTime").substring(2, 16);

            stReturn = stReturn + "</a></td><td><a href='./?a=" + this.ebEnt.ebUd.request.getParameter("a") + "&b=10&h=" + rs.getString("RecId") + "'>" + rs.getString("nmEventFlags") + "</a>";

            if (rs.getInt("nmRefType2") == 11)
            {
              stComment = EbStatic.myString(rs.getString("stTitle"));
              String stTemp = EbStatic.myString(rs.getString("stContent"));
              if (stTemp.length() > 0)
              {
                if (stTemp.length() > 2000)
                  stComment = stComment + " || " + stTemp.substring(0, 2000);
                else
                  stComment = stComment + " || " + stTemp;
              }
            }
            else {
              stComment = EbStatic.myString(rs.getString("stComment"));
            }
            stComment = stComment.replace("<", "&lt;");
            stComment = stComment.replace("\\", "");

            stReturn = stReturn + "</td><td>" + stComment;
            stReturn = stReturn + "</td></tr>";
          }
        }
      } else {
        this.stError = (this.stError + "<BR>ERROR showHistory: " + nmPersonId);
      }
    } catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR showHistory: " + e);
    }
    if (iMax <= 0)
      stReturn = stReturn + "<tr><td bgcolor=skyblue>&nbsp;</td></tr>";
    stReturn = stReturn + "</table>";

    return stReturn;
  }

  public String showPerson(int nmPersonId)
  {
    String stSql = "";
    String stReturn = "<table border=0 width='100%'>";
    String stTemp = "";
    try
    {
      stSql = "select * from X25Person p left join X25PersonStatus ps on p.RecId=ps.nmRefPersonId and ps.nmLoginPersonId=" + this.ebEnt.ebUd.getLoginPersonId() + " where p.RecId=" + nmPersonId;

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        if (iMax == 1)
        {
          stReturn = stReturn + "<tr><th colspan=2 align=center bgcolor=skyblue><a href='./?a=28&tn=29&tid=" + nmPersonId + "'><b>";
          stReturn = stReturn + rs.getString("stFirstName") + " ";
          stReturn = stReturn + rs.getString("stMiddleName") + " ";
          stReturn = stReturn + rs.getString("stLastName") + "</b></a></td>";
          stReturn = stReturn + "</tr>";
          stTemp = showPhone(1, nmPersonId);
          if (stTemp.length() > 0)
            stReturn = stReturn + "<tr><td align=right valign=top>Phones: </td><td>" + stTemp + "</td></tr>";
          stTemp = showEmail(1, nmPersonId);
          if (stTemp.length() > 0)
            stReturn = stReturn + "<tr><td align=right valign=top>Emails: </td><td>" + stTemp + "</td></tr>";
        }
        else {
          this.stError = (this.stError + "<BR>ERROR showPerson: invalid records " + iMax);
        }
      } else {
        this.stError = (this.stError + "<BR>ERROR showPerson: " + nmPersonId);
      }
    } catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR showPerson: " + e);
    }
    stReturn = stReturn + "</table>";

    return stReturn;
  }

  public String showPhone(int nmRefType, int nmRefId)
  {
    String stReturn = "";
    String stSql = "";
    try
    {
      stSql = "select * from X25Phone p, X25RefPhone rp where p.RecId=rp.nmPhoneId and rp.nmRefType=" + nmRefType + " and nmRefId=" + nmRefId;

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        if (iMax > 0)
        {
          stReturn = "<table border=0>";
          if (iMax > 4)
            stReturn = stReturn + "<tr><td>";
          for (int iP = 1; iP <= iMax; iP++)
          {
            rs.absolute(iP);
            if (iMax > 4)
            {
              stReturn = stReturn + " <a href=\"skype:+1" + rs.getString("stPhone") + "?call\" onclick=\"return skypeCheck();\">" + EbStatic.makePhone(rs.getString("stPhone")) + "</a>" + EbStatic.myString(rs.getString("stCountry")) + " " + EbStatic.myString(rs.getString("stExtension")) + " " + EbStatic.myString(rs.getString("stPhoneType")) + " " + EbStatic.myString(rs.getString("stComment"));

              if ((iP < 4) || (iMax - iP <= 0))
                continue;
              stReturn = stReturn + " [" + (iMax - iP) + " ...] ";
              break;
            }

            stReturn = stReturn + "<tr><td><input type=text name=xx" + iP + " id=xx" + iP + " value=\"" + rs.getString("stPhone") + "\" onmouseover=\"this.focus()\" onfocus=\"ebCopy('xx" + iP + "')\" >" + EbStatic.makePhone(rs.getString("stPhone")) + "</a></td>" + "<td>" + EbStatic.myString(rs.getString("stCountry")) + "</td>" + "<td>" + EbStatic.myString(rs.getString("stExtension")) + "</td>" + "<td>" + EbStatic.myString(rs.getString("stPhoneType")) + "</td>" + "<td>" + EbStatic.myString(rs.getString("stComment")) + "</td></tr>";
          }

          if (iMax > 4)
            stReturn = stReturn + "</td></tr>";
          stReturn = stReturn + "</table>";
        }
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR showPhone: " + e);
    }

    return stReturn;
  }

  public String showEmail(int nmRefType, int nmRefId)
  {
    String stSql = "";
    String stReturn = "";
    try
    {
      stSql = "select * from X25User u, X25RefUser ru where u.RecId=ru.nmUserId and ru.nmRefType=" + nmRefType + " and nmPersonId=" + nmRefId;

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        if (iMax > 0)
        {
          stReturn = "<table border=0>";
          for (int iP = 1; iP <= iMax; iP++)
          {
            rs.absolute(iP);
            if (this.stEmail.length() <= 0) {
              this.stEmail = rs.getString("stEmail");
            }
            stReturn = stReturn + "<tr><td><a href='./?a=28&tn=44&tid=" + rs.getString("nmUserId") + "'>" + rs.getString("stEmail") + "</a></td>" + "<td>" + EbStatic.myString(rs.getString("stType")) + "</td></tr>";
          }

          stReturn = stReturn + "</table>";
        }
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR showEmail: " + e);
    }

    return stReturn;
  }

  public String showAddress(int nmRefType, int nmRefId)
  {
    String stSql = "";
    String stReturn = "";
    try
    {
      stSql = "select * from X25Address a, X25RefAddress ra  where a.RecId=ra.nmAddressId and ra.nmRefType=" + nmRefType + " and ra.nmRefId=" + nmRefId;

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        if (iMax > 0)
        {
          stReturn = "<table border=0>";
          for (int iP = 1; iP <= iMax; iP++)
          {
            rs.absolute(iP);
            stReturn = stReturn + "<tr><td>" + EbStatic.myString(rs.getString("stAddress1")) + ", </td>" + "<td>" + EbStatic.myString(rs.getString("stZipCity")) + "</td></tr>";
          }

          stReturn = stReturn + "</table>";
        }
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR showEmail: " + e);
    }

    return stReturn;
  }

  public String showWeb(int nmRefType, int nmRefId)
  {
    String stSql = "";
    String stReturn = "";
    try
    {
      stSql = "select * from X25Website w, X25RefWeb rw  where w.nmWebId=rw.nmWebId and rw.nmRefType=" + nmRefType + " and w.stUrl != '' and rw.nmRefId=" + nmRefId;

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        if (iMax > 0)
        {
          stReturn = "<table border=0>";
          for (int iP = 1; iP <= iMax; iP++)
          {
            rs.absolute(iP);
            stReturn = stReturn + "<tr><td><a target=_blank href='" + EbStatic.myString(rs.getString("stUrl")) + "'>" + EbStatic.myString(rs.getString("stUrl")) + "</a></td></tr>";
          }

          stReturn = stReturn + "</table>";
        }
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR showEmail: " + e);
    }

    return stReturn;
  }

  public String searchMenuResult(int iType)
  {
    String stReturn = "";
    String stSql = "";
    try
    {
      String stSearchType = this.ebEnt.ebUd.getRequestCookieValue("stSearchType");
      String stSearchField = this.ebEnt.ebUd.getRequestCookieValue("stSearchField");
      String stWhere = this.ebEnt.ebUd.getRequestCookieValue("stWhere");
      String stTextSearch = this.ebEnt.ebUd.getRequestCookieValue("stTextSearch");
      String stOut = this.ebEnt.ebUd.getRequestCookieValue("stOut");
      switch (iType)
      {
      case 1:
        stReturn = stReturn + "<table border=0 width='100%'><tr><td align=left>Type of Search:</td>";
        stReturn = stReturn + "<td align=left><select name=stSearchType class=FormField onChange=\"form2.submit();\">";
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("search", "Quick Search", stSearchType);
        stReturn = stReturn + "</select></td></tr>";

        stReturn = stReturn + "<tr><td align=left>Search Field:</td>";
        stReturn = stReturn + "<td align=left><select name=stSearchField class=FormField onChange=\"form2.submit();\">";
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("fu", "Full Name (First/Last)", stSearchField);
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("ln", "Last Name", stSearchField);
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("fn", "First Name", stSearchField);
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("ph", "Phone", stSearchField);
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("em", "Email", stSearchField);
        stReturn = stReturn + "</select></td></tr>";

        stReturn = stReturn + "<tr><td valign=top>Search how:</td><td><select name=stWhere class=FormField>";
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("beg", "Beginning of field", stWhere);
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("any", "Anywhere in field", stWhere);
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("end", "End of Field", stWhere);
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("excact", "Exact Match", stWhere);
        stReturn = stReturn + "</select>";
        stReturn = stReturn + "</td></tr>";

        stReturn = stReturn + "<tr><td align=left>Search String:</td>";
        stReturn = stReturn + "<td align=left><input type=text name=stTextSearch class=FormField value=\"" + stTextSearch + "\"  size=50></td></tr>";
        stReturn = stReturn + "<tr>";

        stReturn = stReturn + "<tr><td valign=top>Return:</td><td><select name=stOut class=FormField>";
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("100", "Top 100 records", stOut);
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("1000", "Top 1000 records", stOut);
        stReturn = stReturn + this.ebEnt.ebNorm.AddOptionList("all", "All records", stOut);
        stReturn = stReturn + "</select>";
        stReturn = stReturn + "</td></tr>";

        stReturn = stReturn + "<tr><td colspan=2 align=center><input type=submit name=submit1 class=FormField value='Search'></td></tr>";
        stReturn = stReturn + "</table>";
      }

      if (stTextSearch.length() > 0)
      {
        switch (iType)
        {
        case 1:
          stReturn = stReturn + "<br><table>";
          stReturn = stReturn + "<table width='100%'>";
          String stWildCard1 = "%";
          String stWildCard2 = "%";
          if (stWhere.equals("excact"))
            stWildCard1 = stWildCard2 = "";
          else if (stWhere.equals("end"))
            stWildCard2 = "";
          else if (stWhere.equals("beg")) {
            stWildCard1 = "";
          }
          stSql = "select * from X25Person p left join X25PersonStatus ps on p.RecId=ps.nmRefPersonId and ps.nmLoginPersonId=" + this.ebEnt.ebUd.getLoginPersonId() + " where ";

          if (stSearchField.equals("fu"))
          {
            String[] aName = stTextSearch.split(" ");

            if (aName.length > 2)
            {
              stSql = stSql + " stFirstName like " + this.ebEnt.dbEnterprise.fmtDbString(new StringBuilder().append(stWildCard1).append(aName[0]).append(stWildCard2).toString()) + " and sMiddleName like " + this.ebEnt.dbEnterprise.fmtDbString(new StringBuilder().append(stWildCard1).append(aName[1]).append(stWildCard2).toString()) + " and stLastName like " + this.ebEnt.dbEnterprise.fmtDbString(new StringBuilder().append(stWildCard1).append(aName[2]).append(stWildCard2).toString());
            }
            else if (aName.length == 2)
            {
              stSql = stSql + " stFirstName like " + this.ebEnt.dbEnterprise.fmtDbString(new StringBuilder().append(stWildCard1).append(aName[0]).append(stWildCard2).toString()) + " and stLastName like " + this.ebEnt.dbEnterprise.fmtDbString(new StringBuilder().append(stWildCard1).append(aName[1]).append(stWildCard2).toString());
            }
            else
            {
              stSql = stSql + " stFirstName like " + this.ebEnt.dbEnterprise.fmtDbString(new StringBuilder().append(stWildCard1).append(aName[0]).append(stWildCard2).toString());
            }
          } else if (stSearchField.equals("fn"))
          {
            stSql = stSql + " stFirstName like " + this.ebEnt.dbEnterprise.fmtDbString(new StringBuilder().append(stWildCard1).append(stTextSearch).append(stWildCard2).toString());
          } else if (stSearchField.equals("ln"))
          {
            stSql = stSql + " stLastName like " + this.ebEnt.dbEnterprise.fmtDbString(new StringBuilder().append(stWildCard1).append(stTextSearch).append(stWildCard2).toString());
          }

          stSql = stSql + " order by stLastName, stFirstName ";
          if ((stOut.length() > 0) && (!stOut.equals("all"))) {
            stSql = this.ebEnt.dbEnterprise.setSqlLimit(stSql, stOut);
          }
          ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
          if (rs != null)
          {
            rs.last();
            int iMax = rs.getRow();
            String stBg = "";
            for (int iS = 1; iS <= iMax; iS++)
            {
              rs.absolute(iS);
              stReturn = stReturn + "<tr>";
              if (iS % 2 != 0)
                stBg = " bgcolor='#EEEEEE' ";
              else
                stBg = " bgcolor='#FFFFFF' ";
              stReturn = stReturn + "<td valign=top " + stBg + "><a href='" + this.ebEnt.rsA.getString("stUrl") + "&b=2&p=" + rs.getInt("RecId") + "'>" + rs.getString("stFirstName");
              stReturn = stReturn + " " + rs.getString("stMiddleName");
              stReturn = stReturn + " " + rs.getString("stLastName") + "</a></td>";
              stReturn = stReturn + "<tr>";
            }
          }
          stReturn = stReturn + "</table>";
        }
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<BR>ERROR: " + e);
    }
    return stReturn;
  }

  public String callBack(int iType)
  {
    String stReturn = "";
    String stSql = "SELECT * FROM X25Task where nmTaskType = 2 and dtStart > now() order by dtStart DESC limit 200";
    try
    {
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        stReturn = stReturn + "<table width='100%'><tr><td colspan=2 bgcolor=skyblue align=center width='100%'><h2>Call Back List</h2></td></tr>";
        String stBg = "";
        for (int iH = 1; iH <= iMax; iH++)
        {
          rs.absolute(iH);
          if (iH % 2 == 0)
            stBg = " bgcolor='#EEEEEE' ";
          else {
            stBg = " bgcolor='#FFFFFF' ";
          }
          stReturn = stReturn + "<tr>";
          stReturn = stReturn + "<td " + stBg + ">" + rs.getString("MaxDate") + "</td>";
          stReturn = stReturn + "<td " + stBg + "><a href='" + this.ebEnt.rsA.getString("stUrl") + "&b=2&p=" + rs.getInt("RecId") + "'>" + rs.getString("stFirstName") + " ";
          stReturn = stReturn + rs.getString("stMiddleName") + " ";
          stReturn = stReturn + rs.getString("stLastName") + "</td>";
          stReturn = stReturn + "</tr>";
        }
        stReturn = stReturn + "</table>";
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR callBack: " + e);
    }
    return stReturn;
  }

  public String recentHistory(int iType)
  {
    String stReturn = "";
    String stSql = "select * from X25Person p INNER JOIN (SELECT h.nmRefId, Max(h.dtEventStartTime) as MaxDate   FROM X25HistoryLog h where h.nmRefType=1 and h.nmEventFlags != 2 GROUP BY h.nmRefId) as TMax ON p.RecId = TMax.nmRefId where ( p.nmFlags & 0x30000 ) = 0 order by MaxDate DESC limit 200";
    try
    {
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        stReturn = stReturn + "<table><tr><td colspan=2 bgcolor=skyblue align=center><h2>Recent History</h2></td></tr>";
        String stBg = "";
        for (int iH = 1; iH <= iMax; iH++)
        {
          rs.absolute(iH);
          if (iH % 2 == 0)
            stBg = " bgcolor='#EEEEEE' ";
          else {
            stBg = " bgcolor='#FFFFFF' ";
          }
          stReturn = stReturn + "<tr>";
          stReturn = stReturn + "<td " + stBg + ">" + rs.getString("MaxDate") + "</td>";
          stReturn = stReturn + "<td " + stBg + "><a href='" + this.ebEnt.rsA.getString("stUrl") + "&b=2&p=" + rs.getInt("RecId") + "'>" + rs.getString("stFirstName") + " ";
          stReturn = stReturn + rs.getString("stMiddleName") + " ";
          stReturn = stReturn + rs.getString("stLastName") + "</td>";
          stReturn = stReturn + "</tr>";
        }
        stReturn = stReturn + "</table>";
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR recentHistory: " + e);
    }
    return stReturn;
  }
}