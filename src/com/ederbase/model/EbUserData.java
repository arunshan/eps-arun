package com.ederbase.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Enumeration;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EbUserData
{
  static int ciDatabaseTypeMysql = 0;
  static int ciDatabaseTypeMssql = 1;
  static int ciDatabaseTypeODBC = 2;
  private String[] aGetClassName = null;
  private String[] aGetClassLabel = null;
  private int iDebugScript = 0;
  private int iAppId = -1;
  private int iLoginId = -1;
  private int iLoginPersonId = -1;
  private int iLoginPersonFlags = 0;
  private EbEnterprise ebEnt = null;
  private String stError = "";
  private String stLoginName = "";
  private HttpServletResponse response = null;
  private String stRedirect = "";
  public Cookie[] aCookies = null;
  public String[] aLogin = null;
  public HttpServletRequest request = null;
  public int iLic = 0;
  private String stPopupMessage = "";
  private String stChild = "";

  public EbUserData(EbEnterprise ebEnt)
  {
    this.iAppId = 0;
    this.iLoginId = 0;
    this.iLoginPersonId = 0;
    this.iLoginPersonFlags = 0;
    this.aCookies = null;
    this.aLogin = null;
    this.stError = "";
    this.stLoginName = "";
    this.ebEnt = ebEnt;
    this.request = null;
    this.response = null;
  }

  public void setRequest(HttpServletRequest request, HttpServletResponse response)
  {
    this.request = request;
    this.response = response;
  }

  public boolean isSelected(ResultSet rsField, ResultSet rsChoice, String stValueCurrent)
  {
    boolean bReturn = false;
    int iBitValue = 0;
    int iCurrentValue = 0;
    int iBitPos = 0;
    try
    {
      if (rsField.getString("stHandler").contains("bit"))
      {
        try
        {
          if (stValueCurrent.startsWith("0x"))
          {
            iCurrentValue = Integer.decode(stValueCurrent).intValue();
          }
          else {
            iCurrentValue = Integer.parseInt(stValueCurrent);
          }

          if (rsChoice.getString("UniqIdChoice").startsWith("0x"))
          {
            iBitValue = Integer.decode(rsChoice.getString("UniqIdChoice")).intValue();
          }
          else {
            iBitPos = Integer.parseInt(rsChoice.getString("UniqIdChoice"));
            iBitValue = (int)(iBitValue | 1 << 31 - iBitPos);
          }

          if ((iBitValue & iCurrentValue) == iBitValue)
          {
            bReturn = true;
          }
        }
        catch (Exception e) {
        }
      }
      else if ((stValueCurrent != null) && (stValueCurrent.equals(rsChoice.getString("UniqIdChoice"))))
      {
        bReturn = true;
      }
    }
    catch (Exception e) {
    }
    return bReturn;
  }

  public String addOption(String stLabel, String stId, String stCurrent)
  {
    if ((stCurrent.length() > 0) && (stId.endsWith(stCurrent)))
    {
      return "<option value=\"" + stId + "\" selected>" + stLabel + "</option>";
    }

    return "<option value=\"" + stId + "\">" + stLabel + "</option>";
  }

  public String addOption2(String stLabel, String stId, String stCurrent)
  {
    if (stId.trim().equals(stCurrent.trim()))
    {
      return "<option value=\"" + stId + "\" selected>" + stLabel + "</option>";
    }

    return "<option value=\"" + stId + "\">" + stLabel + "</option>";
  }

  public String addOption4(String stLabel, String stId, String stCurrent, int iAllowed)
  {
    if (stId.trim().equals(stCurrent.trim()))
    {
      return "<option value=\"" + stId + "\" selected>" + stLabel + "</option>";
    }

    if (iAllowed > 0) {
      return "<option value=\"" + stId + "\">" + stLabel + "</option>";
    }
    return "<option DISABLED value=\"" + stId + "\">" + stLabel + "</option>";
  }

  public String addOption3(String stLabel, String stId, String stCurrent)
  {
    if (stCurrent.trim().contains(stId.trim()))
    {
      return "<option value=\"" + stId + "\" selected>" + stLabel + "</option>";
    }

    return "<option value=\"" + stId + "\">" + stLabel + "</option>";
  }

  public String getClass(int iC, String stValue)
  {
    String stReturn = "<select  name=\"class_" + iC + "\">";

    stReturn = stReturn + addOption2("-- select or enter manual --", "", stValue);
    for (int ii = 0; (this.aGetClassName != null) && (ii < this.aGetClassName.length) && (this.aGetClassName[ii] != null); ii++)
    {
      stReturn = stReturn + addOption2(this.aGetClassLabel[ii], this.aGetClassName[ii], stValue);
    }
    stReturn = stReturn + "</select>";
    return stReturn;
  }

  public String getMyDb(String stParam, ResultSet rs2, String stName)
  {
    String stValue = "";
    stValue = this.ebEnt.ebUd.request.getParameter(stParam);
    if (stValue == null)
    {
      try
      {
        if (rs2 != null)
          stValue = rs2.getString(stName);
      }
      catch (Exception e) {
      }
    }
    if (stValue == null)
      stValue = "";
    return stValue.trim();
  }

  public String UrlReader(String stUrl)
  {
    String stReturn = "";
    try
    {
      String stRequest = this.ebEnt.ebUd.request.getRequestURL().toString();

      URL oUrl = new URL(stRequest + stUrl);
      URLConnection yc = oUrl.openConnection();
      BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
      String inputLine;
      while ((inputLine = in.readLine()) != null)
      {
        stReturn = stReturn + inputLine + "\n";
      }
      in.close();
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR: UrlReader " + e);
    }
    return stReturn;
  }

  public String getCountry()
  {
    return "US";
  }

  public int getDebugScript()
  {
    return this.iDebugScript;
  }

  public int checkLogin()
  {
    int iReturn = 0;
    String stIp = "";
    String stReferer = "";
    try
    {
      stIp = this.request.getLocalAddr().toString();
      stReferer = this.request.getHeader("referer");
      if ((this.aLogin != null) && (this.aLogin.length > 4) && (this.aLogin[0] != null))
      {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();

        if ((!this.aLogin[0].equals("")) && (!this.aLogin[1].equals("")) && (!this.aLogin[4].equals("")))
        {
          iReturn = this.ebEnt.dbEb.ExecuteSql1n("select nmUserId from t_session where nmSessionId=" + this.aLogin[4] + " and nmLastTime = " + this.aLogin[1]);
          if ((iReturn > 0) && (iReturn == Integer.parseInt(this.aLogin[0])))
          {
            this.ebEnt.dbEb.ExecuteUpdate("update t_session set dtLast=now(), nmHitCount=(nmHitCount+1) where nmSessionId=" + this.aLogin[4]);
          }
          else iReturn = -2; 
        }
        else if ((!this.aLogin[2].equals("")) && (!this.aLogin[3].equals("")))
        {
          iReturn = this.ebEnt.dbEnterprise.ExecuteSql1n("select RecId from X25User where stEMail='" + this.aLogin[2] + "' and ( stPassword=password('" + this.aLogin[3] + "') or stPassword=old_password('" + this.aLogin[3] + "') )");
          if (iReturn > 0)
          {
            this.ebEnt.dbEnterprise.ExecuteUpdate("update X25User set SuccessLoginTime = " + now + ", nmLastLoginTime=" + now + ", nmLoginCount=(nmLoginCount+1) where RecId=" + iReturn);
            this.ebEnt.dbEb.ExecuteUpdate("insert into t_session (dtStart,dtLast,nmUserId,stIp,nmLastTime,stReferer) values( now(), now(), " + iReturn + ",'" + stIp + "'," + now + ",\"" + stReferer + "\")");
            this.aLogin[4] = this.ebEnt.dbEb.ExecuteSql1("select max(nmSessionId) from t_session where nmUserId=" + iReturn);
            this.aLogin[1] = ("" + now);
            this.aLogin[0] = ("" + iReturn);
          }
          else {
            this.ebEnt.dbEnterprise.ExecuteUpdate("update X25User set nmLastLoginTime=" + now + ", nmErrorCount=(nmErrorCount+1) where stEMail='" + this.aLogin[2] + "' ");
          }
        }
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "ERROR checkLogin: " + e);
      iReturn = 0;
    }
    this.iLoginId = iReturn;
    if (this.iLoginId > 0)
    {
      this.iLoginPersonFlags = this.ebEnt.dbEnterprise.ExecuteSql1n("select nmPriviledge from X25User where RecId=" + this.iLoginId);
    }
    return iReturn;
  }

  public String getCookieValue(String cookieName)
  {
    String stReturn = "";
    if (this.aCookies == null)
    {
      this.aCookies = this.request.getCookies();
    }

    for (int i = 0; (this.aCookies != null) && (i < this.aCookies.length); i++)
    {
      if (!cookieName.equals(this.aCookies[i].getName()))
        continue;
      stReturn = this.aCookies[i].getValue();
      break;
    }

    if (stReturn == null)
    {
      stReturn = "";
    }
    return stReturn;
  }

  public String getRequestCookieValue(String cookieName)
  {
    String stReturn = "";
    stReturn = getRequest(cookieName);
    if (stReturn.length() <= 0)
    {
      for (int i = 0; i < this.aCookies.length; i++)
      {
        if (!cookieName.equals(this.aCookies[i].getName()))
          continue;
        stReturn = this.aCookies[i].getValue();
        break;
      }
    }
    else
    {
      setCookie(cookieName, stReturn);
    }
    if (stReturn == null)
    {
      stReturn = "";
    }

    return stReturn;
  }

  public String getRequest(String stParam)
  {
    String stReturn = "";
    if (this.request != null)
    {
      stReturn = this.request.getParameter(stParam);
      if (stReturn == null)
      {
        stReturn = "";
      }
    }
    return stReturn;
  }

  public void setCookie(String stLabel, String stValue)
  {
    if (this.response != null)
    {
      Cookie userCookie1 = new Cookie(stLabel, stValue);
      userCookie1.setMaxAge(-1);
      this.response.addCookie(userCookie1);
    }
  }

  public void setCookie(String stLabel, String stValue, int iMaxDays)
  {
    if (this.response != null)
    {
      Cookie userCookie1 = new Cookie(stLabel, stValue);
      userCookie1.setMaxAge(86400 * iMaxDays);
      this.response.addCookie(userCookie1);
    }
  }

  public String dumpRequest()
  {
    return dumpRequest(this.request);
  }

  private String dumpRequest(HttpServletRequest request)
  {
    Enumeration paramNames = request.getParameterNames();
    String stHTML = "";
    String title = "Reading All Request Parameters";
    stHTML = stHTML + "<BODY BGCOLOR=\"#FDF5E6\">\n<H1 ALIGN=CENTER>" + title + "</H1>\n" + "<TABLE BORDER=1 ALIGN=CENTER>\n" + "<TR BGCOLOR=\"#FFAD00\">\n" + "<TH>Parameter Name<TH>Parameter Value(s)";

    while (paramNames.hasMoreElements())
    {
      String paramName = (String)paramNames.nextElement();
      stHTML = stHTML + "<TR><TD>" + paramName + "\n<TD>";
      String[] paramValues = request.getParameterValues(paramName);
      if (paramValues.length == 1)
      {
        String paramValue = paramValues[0];
        if (paramValue.length() == 0)
        {
          stHTML = stHTML + "<I>No Value</I>";
        }
        else
          stHTML = stHTML + paramValue;
      }
      else
      {
        stHTML = stHTML + "<UL>";
        for (int i = 0; i < paramValues.length; i++)
        {
          stHTML = stHTML + "<LI>" + paramValues[i];
        }
        stHTML = stHTML + "</UL>";
      }
    }
    stHTML = stHTML + "</TABLE>\n</BODY></HTML>";
    return stHTML;
  }

  public int getLoginId()
  {
    return this.iLoginId;
  }

  public int getLoginPersonId()
  {
    if ((this.iLoginPersonId <= 0) && (this.iLoginId > 0))
    {
      this.iLoginPersonId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(nmPersonId) from X25RefUser where nmRefType=1 and nmUserId=" + this.iLoginId);
      this.iLoginPersonFlags = this.ebEnt.dbEnterprise.ExecuteSql1n("select nmFlags from X25Person where RecId=" + this.iLoginPersonId);
    }
    return this.iLoginPersonId;
  }

  public int getLoginPersonFlags()
  {
    return this.iLoginPersonFlags;
  }

  public int setAppId(int iAppId)
  {
    this.iAppId = iAppId;
    return this.iAppId;
  }

  public int getAppId()
  {
    return this.iAppId;
  }

  public String getLoginName()
  {
    if (getLoginId() > 0)
    {
      if (this.stLoginName.trim().equals("") == true)
      {
        try
        {
          ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql("select stFirstName,stLastName from X25Person p, X25RefUser ru where ru.nmUserId = " + getLoginId() + " and ru.nmRefType=1 and ru.nmPersonId=p.RecId");
          if (rs != null)
          {
            rs.last();
            if (rs.getRow() >= 1)
            {
              rs.absolute(1);
              this.stLoginName = rs.getString("stFirstName");
              this.stLoginName = (this.stLoginName + " " + rs.getString("stLastName"));
            }
          }
        }
        catch (Exception e) {
          this.stError = (this.stError + "<BR>ERROR getLoginName: Email not found: " + this.aLogin[2]);
        }
      }
      if (this.stLoginName.trim().equals(""))
      {
        this.stLoginName = this.aLogin[2];
      }
    }
    else {
      this.stLoginName = "";
    }
    return this.stLoginName;
  }

  public String getLoginEmail()
  {
    return this.aLogin[2];
  }

  public String getError()
  {
    return this.stError;
  }

  public String getFormValue(int nmEpsPriv, ResultSet rs)
  {
    String stValue = null;
    try
    {
      if (rs.getString("stValidationFlags").contains("time"))
      {
        String stHour = this.ebEnt.ebUd.request.getParameter("f" + rs.getString("nmForeignId") + "_hr");
        String stMin = this.ebEnt.ebUd.request.getParameter("f" + rs.getString("nmForeignId") + "_mn");
        if ((stHour != null) && (stHour.length() > 0) && (stMin != null))
        {
          return stHour + ":" + stMin + ":00";
        }

        return "00:00:00";
      }

      String[] astValues = this.ebEnt.ebUd.request.getParameterValues("f" + rs.getString("nmForeignId"));
      stValue = "";
      long iBitValue = 0L;
      if (astValues != null)
      {
        for (int i = 0; i < astValues.length; i++)
        {
          if (rs.getString("stHandler").contains("bit"))
          {
            try
            {
              int iBitPos = Integer.decode(astValues[i]).intValue();
              iBitValue |= 1 << 31 - iBitPos;
            }
            catch (Exception e) {
              this.stError = (this.stError + "<BR>Error getFormValue - can't create bitvalue " + e);
            }
          }
          else {
            if (i > 0)
            {
              stValue = stValue + "~";
            }
            stValue = stValue + astValues[i];
          }
        }
        if (rs.getString("stHandler").contains("bit"))
        {
          stValue = "" + iBitValue;
        }
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR getFormValue: " + e);
    }
    return stValue;
  }

  public String getRedirect()
  {
    return this.stRedirect;
  }

  public void setRedirect(String stR)
  {
    this.stRedirect = stR;
  }

  public String fmtDateToDb(String stIn)
  {
    String stDate = stIn;
    if ((stIn != null) && (stIn.trim().length() > 0))
    {
      String[] aV = stIn.trim().split("/");
      if ((aV != null) && (aV.length >= 3))
        stDate = aV[2] + "-" + aV[0] + "-" + aV[1];
    } else {
      stDate = "1900-01-01";
    }return stDate;
  }

  public String fmtDateFromDb(String stIn)
  {
    String stDate = "";
    if ((stIn != null) && (stIn.trim().length() > 0))
    {
      if (!stIn.equals("1900-01-01"))
      {
        String[] aV1 = stIn.trim().split(" ");
        String[] aV = aV1[0].trim().split("-");
        if ((aV != null) && (aV.length >= 3))
          stDate = aV[1] + "/" + aV[2] + "/" + aV[0];
      }
    }
    return stDate;
  }

  public String fmtDateTimeFromDb(String stIn)
  {
    String stDate = "";
    if ((stIn != null) && (stIn.trim().length() > 0))
    {
      if (!stIn.equals("1900-01-01"))
      {
        String[] aV1 = stIn.trim().split(" ");
        String[] aV = aV1[0].trim().split("-");
        if ((aV != null) && (aV.length >= 3))
          stDate = aV[1] + "/" + aV[2] + "/" + aV[0];
        if (aV1.length > 1)
        {
          String[] aV2 = aV1[1].trim().split(":");
          stDate = stDate + " " + aV2[0] + ":" + aV2[1];
        }
      }
    }
    return stDate;
  }

  public String datePicker(String stForm, String stField)
  {
    String stReturn = "\n<script language='JavaScript'>\nnew tcal ({\n\t'formname': '" + stForm + "'," + "\n 'controlname': '" + stField + "'});\n</script>";

    return stReturn;
  }

  public void setLoginId(int iUserId)
  {
    this.iLoginId = iUserId;
  }

  public void setLoginPersonFlags(int nmPrivUser)
  {
    this.iLoginPersonFlags = nmPrivUser;
  }

  public String getYear(int nmTableId, int iYear, String stName, int iF)
  {
    String stReturn = "<select onChange=\" selectDate('f" + iF + "_selected'); document.form" + nmTableId + ".submit();\" name=\"" + stName + "\">";
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2010", "2010", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2011", "2011", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2012", "2012", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2013", "2013", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2014", "2014", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2015", "2015", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2016", "2016", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2017", "2017", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2018", "2018", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2019", "2019", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2020", "2020", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2021", "2021", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2022", "2022", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2023", "2023", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2024", "2024", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2025", "2025", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2026", "2026", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2027", "2027", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2028", "2028", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption("2029", "2029", new StringBuilder().append("").append(iYear).toString());
    stReturn = stReturn + "</select>";
    return stReturn;
  }

  public String getMonth(int nmTableId, int iMonth, String stName, int iF)
  {
    String stReturn = "<select onChange=\"selectDate('f" + iF + "_selected'); document.form" + nmTableId + ".submit();\" name=\"" + stName + "\">";
    stReturn = stReturn + this.ebEnt.ebUd.addOption2("January", "1", new StringBuilder().append("").append(iMonth).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption2("February", "2", new StringBuilder().append("").append(iMonth).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption2("March", "3", new StringBuilder().append("").append(iMonth).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption2("April", "4", new StringBuilder().append("").append(iMonth).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption2("May", "5", new StringBuilder().append("").append(iMonth).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption2("June", "6", new StringBuilder().append("").append(iMonth).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption2("July", "7", new StringBuilder().append("").append(iMonth).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption2("August", "8", new StringBuilder().append("").append(iMonth).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption2("September", "9", new StringBuilder().append("").append(iMonth).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption2("October", "10", new StringBuilder().append("").append(iMonth).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption2("November", "11", new StringBuilder().append("").append(iMonth).toString());
    stReturn = stReturn + this.ebEnt.ebUd.addOption2("December", "12", new StringBuilder().append("").append(iMonth).toString());
    stReturn = stReturn + "</select>";
    return stReturn;
  }

  public String getPopupMessage()
  {
    return this.stPopupMessage;
  }

  public void clearPopupMessage()
  {
    this.stPopupMessage = "";
  }

  public void setPopupMessage(String stMessage)
  {
    if (this.stPopupMessage.length() > 0)
      this.stPopupMessage += "~~";
    this.stPopupMessage += stMessage.replace("\n", "~");
  }

  public String convertApostrophe(String stIn)
  {
    String stReturn = "";
    byte[] aOut = new byte[stIn.length() + 2];

    if (null != stIn)
    {
      int iOut = 0;
      int iLines = 1;
      for (int isb = 0; isb < stIn.length(); isb++)
      {
        int origCharAsInt = stIn.charAt(isb);
        aOut[iOut] = 0;
        try
        {
          switch (origCharAsInt)
          {
          case 226:
            isb++;
            origCharAsInt = stIn.charAt(isb);
            isb++;
            origCharAsInt = stIn.charAt(isb);
            switch (origCharAsInt)
            {
            case 152:
            case 153:
              aOut[(iOut++)] = 39;
              break;
            case 156:
            case 157:
              aOut[(iOut++)] = 34;
              break;
            case 154:
            case 155:
            default:
              aOut[(iOut++)] = 63;
              this.stError = (this.stError + "<BR>ERROR: convertApostrophe at " + isb + " line " + iLines + " 0x" + Long.toHexString(origCharAsInt) + " " + origCharAsInt);
            }break;
          case 10:
            iLines++;
            aOut[(iOut++)] = (byte)origCharAsInt;
            break;
          default:
            aOut[(iOut++)] = (byte)origCharAsInt;
          }

          aOut[iOut] = 0;
        }
        catch (Exception e) {
          this.stError = (this.stError + "<br>ERROR: convertApastrophe isb " + isb + " " + e);
        }
      }
      stReturn = new String(aOut);
    }
    return stReturn;
  }

  public void setXlsProcess(String stChild)
  {
    this.stChild = stChild;
  }

  public String getXlsProcess()
  {
    return this.stChild;
  }
}