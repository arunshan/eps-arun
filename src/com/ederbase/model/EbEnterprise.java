package com.ederbase.model;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EbEnterprise
{
  public String[] gastEmails = { "RobertEder@myinfo.com|steinwand|mail.myinfo.com|RobertEder@myinfo.com" };

  public int giEmailIx = 0;

  public EbDynamic ebDyn = null;
  public EbUserData ebUd = null;
  public EbSecurity ebSec = null;
  public EbAdmin ebAdmin = null;
  public EbMenuBar ebMb = null;
  public EbNormalized ebNorm = null;
  private EbCipher cCrypt = null;

  public EbDatabase dbDyn = null;
  public EbDatabase dbEb = null;
  public EbDatabase dbEnterprise = null;
  public EbDatabase dbCommon = null;

  public int iDebugLevel = 0;
  private String stError = "";
  private String stUser = "";
  private String stPassword = "";
  private String stHost = "";
  public String stUserParams = "";
  private int iDbType = 0;
  private int nmMainApp = 0;
  private int nmDynId = 0;
  private int nmSkinId = 0;
  private int nmLanguage = 0;

  private ResultSet rsEbCompany = null;
  private ResultSet rsApp = null;
  public ResultSet rsA = null;

  public EbEnterprise(int nmSkinId, int nmLanguage, String stEbName, String stEbEnterpriseName, String stEbCommon)
  {
    String stSql = "";
    this.nmSkinId = nmSkinId;
    this.nmLanguage = nmLanguage;
    try
    {
      ///////////////////EBLoadConfig();

      if (this.dbEb == null)
      {
    	  /*this.dbEb = new EbDatabase(0, "localhost", "root", "27225643", stEbName, "");
          this.dbEnterprise = new EbDatabase(0, "localhost", "root", "27225643", stEbEnterpriseName, "");
          this.dbCommon = new EbDatabase(0, "localhost", "root", "27225643", stEbCommon, "");*/
        this.dbEb = new EbDatabase(0, "localhost", "root", "", stEbName, "");
        this.dbEnterprise = new EbDatabase(0, "localhost", "root", "", stEbEnterpriseName, "");
        this.dbCommon = new EbDatabase(0, "localhost", "root", "", stEbCommon, "");
        this.ebUd = new EbUserData(this);
        this.ebSec = new EbSecurity(this);
        this.ebNorm = new EbNormalized(this);
        try
        {
          stSql = "SELECT * FROM X25Company c left join X25Website w on c.RecId=w.nmCompanyId where (nmCompanyFlags & 0x02) != 0";
          this.rsEbCompany = this.dbEnterprise.ExecuteSql(stSql);
          this.rsEbCompany.absolute(1);
          this.nmMainApp = this.rsEbCompany.getInt("nmMainApp");
          stSql = "select * from t_application s where nmAppId='" + this.nmMainApp + "' ";
          this.rsApp = this.dbEb.ExecuteSql(stSql);
          this.rsApp.absolute(1);
          this.nmDynId = this.rsApp.getInt("nmDynId");
        }
        catch (Exception e) {
          this.stError = (this.stError + "<br>ERROR ebDyn:  " + stSql + ": " + e);
        }
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>ERROR ebDyn:  " + stSql + ": " + e);
    }
    this.ebAdmin = new EbAdmin(this);
  }

  public int getMainCompanyId()
  {
    int iReturn = 0;
    try
    {
      iReturn = this.rsEbCompany.getInt("RecId");
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>getMainCompanyId: " + e);
    }
    return iReturn;
  }

  public String createMyHome(ResultSet rsA)
  {
    String stReturn = "<br>TODO: MYHOME";

    return stReturn;
  }

  public String processApplication(HttpServletRequest request, HttpServletResponse response)
  {
    String stHTML = "";
    int nmApp = 0;
    int iSel = 0;
    int myHome = 0;
    try
    {
      String stUnsubscribe = request.getParameter("v");

      String stSql = "SELECT * FROM t_application a left join t_security sec on a.nmSecurity=sec.nmSecurityId where nmAppMain=" + this.rsEbCompany.getString("nmMainApp") + " order by nmAppType,nmAppParent,nmOrder";
      this.rsA = this.dbEb.ExecuteSql(stSql);
      if (this.rsA != null)
      {
        this.rsA.last();
        int iMaxA = this.rsA.getRow();
        if (iMaxA >= 1)
        {
          this.ebMb = new EbMenuBar(this.rsEbCompany.getString("stCompanyName"), this.rsEbCompany.getString("stLogo"), this.rsEbCompany.getString("stCompanyName"), this.ebUd.getLoginName(), this.rsA);
          if ((stUnsubscribe != null) && (stUnsubscribe.length() > 5))
          {
            stHTML = unSubscribe(EBDecrypt(stUnsubscribe));
          }
          else {
            for (int iA = 1; iA <= iMaxA; iA++)
            {
              this.rsA.absolute(iA);

              if (iA == 1)
              {
                myHome = this.rsA.getInt("nmAppType2");
                if (this.ebUd.getAppId() <= 0)
                {
                  this.ebUd.setAppId(myHome);
                }
              }
              if (this.rsA.getInt("nmAppId") == this.ebUd.getAppId())
              {
                nmApp = iA;
                if (this.ebSec.checkSecurity(this.rsA) > 0)
                {
                  Calendar cal1 = Calendar.getInstance();
                  long lTime1 = cal1.getTimeInMillis();
                  EbCrm ebCrm = new EbCrm();
                  EbMail ebMmail = new EbMail(this);
                  EbCampaignManager ebCamp = new EbCampaignManager(this, request);
                  EbTwitter ebTwitter = new EbTwitter(this);
                  switch (this.rsA.getInt("nmProcessId"))
                  {
                  case 1:
                    stHTML = this.rsA.getString("stContent");
                    break;
                  case 1000:
                    if (this.ebUd.getLoginId() > 0) break;
                    stHTML = getLoginPage(); break;
                  case 100:
                    stHTML = createMyHome(this.rsA);
                    break;
                  case 1001:
                    stHTML = getTOC(this.rsA.getInt("nmMainDb"));
                    break;
                  case 1002:
                    stHTML = "<br>TODO Dynamic Data getHTML ";
                    break;
                  case 2000:
                    stHTML = "~~" + this.rsA.getString("stUrl");
                    return stHTML;
                  case 1900:
                    stHTML = createMyHome(this.rsA);
                    break;
                  case 2001:
                    EbMarketingSpider ebSpider = new EbMarketingSpider(this, request);
                    stHTML = stHTML + ebSpider.doSpider(1);
                    this.stError += ebSpider.getError();
                    break;
                  case 2003:
                    stHTML = stHTML + ebCamp.campainManager(1);
                    this.stError += ebCamp.getError();
                    break;
                  case 2004:
                    stHTML = stHTML + ebCamp.campainManager(20);
                    this.stError += ebCamp.getError();
                    break;
                  case 2005:
                    stHTML = stHTML + ebMmail.manageEmail();
                    stHTML = stHTML + ebMmail.getError();
                    break;
                  case 2011:
                    if (getState("b", request) > 1)
                      break;
                    stHTML = "~~" + this.rsA.getString("stUrl");
                    return stHTML;
                  case 2010:
                    stHTML = stHTML + ebCrm.doSearch(this, request);
                    break;
                  case 2012:
                    stHTML = stHTML + ebCrm.emailBulkReply(this, request);
                    break;
                  case 2013:
                    stHTML = stHTML + ebCrm.emailJunk(this, request);
                    break;
                  case 2018:
                    stHTML = stHTML + ebTwitter.processMarketing(0);
                    this.stError += ebTwitter.getError();
                    break;
                  case 2019:
                    EbCraigsList ebCl = new EbCraigsList(this);
                    stHTML = stHTML + ebCl.processCraigsList(1);
                    this.stError += ebCl.getError();
                    break;
                  case 2021:
                    stHTML = stHTML + "<BR>Reports Disabled";

                    break;
                  case 1901:
                  case 1902:
                  case 1903:
                    stHTML = this.ebAdmin.getAdmin(this.rsA, request);
                    this.stError += this.ebAdmin.getError();
                    break;
                  }
                  stHTML = "<BR>WARNING: no application found for: " + this.rsA.getInt("nmProcessId");

                  Calendar cal2 = Calendar.getInstance();
                  long lTime2 = cal2.getTimeInMillis();
                  stHTML = stHTML + "<br>Duration (ms): " + (lTime2 - lTime1);
                }
                else if (this.ebUd.getDebugScript() > 0)
                {
                  this.stError += "<br>ERROR APP: security failure";
                }
              }

              if (this.rsA.getInt("nmProcessId") == 1000)
              {
                if (this.ebUd.getLoginId() <= 0)
                {
                  stHTML = getLoginPage();
                }
              }

              if (this.ebSec.checkSecurity(this.rsA) > 0)
              {
                switch (this.rsA.getInt("nmAppType"))
                {
                case 1:
                default:
                  break;
                case 2:
                  this.ebMb.addLink(this.rsA);
                  break;
                case 80:
                case 90:
                  if (nmApp == iA)
                  {
                    iSel = 1;
                  }
                  else {
                    iSel = 0;
                  }
                  this.ebMb.addMenuBar(this.rsA, iSel);
                  break;
                }
              }
              else {
                if (this.ebUd.getDebugScript() <= 0)
                  continue;
                this.stError += "<br>ERROR APP: security failure for AppType";
              }
            }
          }
        }
        else
        {
          this.stError += "<BR>ERROR: no application records";
        }
      }
      else {
        this.stError += "<BR>ERROR: no application record data";
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<br>ERROR getHTML: " + e);
    }
    if (stHTML.equals(""))
    {
      if (this.ebUd.getLoginId() <= 0)
      {
        stHTML = getLoginPage();
      }
      else {
        stHTML = "<br>WARNING: No Application data was genereted.<br>";
      }
    }
    return stHTML;
  }

  public int getState(String stId, HttpServletRequest request)
  {
    int iReturn = -1;
    String stValue = request.getParameter("b");
    try
    {
      if ((stValue == null) || (stValue.equals("")))
      {
        stValue = "1";
      }
      iReturn = Integer.parseInt(stValue);
    }
    catch (Exception e) {
      iReturn = 0;
    }
    return iReturn;
  }

  public String getLoginPage()
  {
    String stEMail = this.ebUd.getLoginEmail();

    String stReturn = "";
    try
    {
      stReturn = stReturn + "<hr><form method=post><table border=0><tr><th colspan=2>Login</th></trh>";
      stReturn = stReturn + "<tr><td align=right>Enter your email: </td><td align=left><input type=text name=un size=30 value=\"" + stEMail + "\"></td></tr>";
      stReturn = stReturn + "<tr><td align=right>Enter your password: </td><td align=left><input type=password name=p size=10></td></tr>";
      stReturn = stReturn + "<tr><td align=center colspan=2><input type=submit name=Login value='Login'></td></tr>";
      stReturn = stReturn + "</table></form>";
      stReturn = stReturn + "<br>" + this.dbEb.ExecuteSql1(new StringBuilder().append("SELECT stValue FROM t_setting where nmCid=").append(this.rsEbCompany.getString("RecId")).append(" and nmSeq=1 and stField='defmsg'").toString());
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>ERROR ebDyn: : " + e);
    }
    return stReturn;
  }

  public String createContent(ResultSet rsD)
  {
    String stReturn = "\n<!--TODO createContent() -->\n";
    return stReturn;
  }

  private void EBLoadConfig()
  {
    if ((this.stUser == null) || (this.stUser.equals("")))
    {
      String stLine = "";
      this.stUser = "ils";
      this.cCrypt = new EbCipher("yu#$%1xx6");
      this.stPassword = this.cCrypt.decrypt("oE1UCg1pzBLQDuTqH83JCw==");
      this.iDbType = 0;
      this.stHost = "myinfo.com";
      try
      {
        FileInputStream fsIn = new FileInputStream("ederbase.conf");
        DataInputStream fpIn = new DataInputStream(fsIn);
        while (fpIn.available() != 0)
        {
          stLine = fpIn.readLine();
          String[] v = stLine.split("\t");
          if (v[0].equals("stEB"))
          {
            this.cCrypt = new EbCipher(v[1]);
          }
          else if (v[0].equals("stPassword"))
          {
            this.stPassword = this.cCrypt.decrypt(v[1]);
          } else if (v[0].equals("iDbType"))
          {
            this.iDbType = Integer.parseInt(v[1]);
          } else if (v[0].equals("stHost"))
          {
            this.stHost = v[1];
          } else if (v[0].equals("stUser"))
          {
            this.stUser = v[1];
          } else if (v[0].equals("stUserParams"))
          {
            this.stUserParams = v[1];
          }
        }
        fpIn.close();
      }
      catch (Exception e) {
        System.err.println("File input error: /etc/ederbase/ederbase.conf ERR: " + e);
      }
    }
  }

  public String fmtHTML(String stHTML, String stErr)
  {
    String stReturn = "";
    String stTemp = "";
    int nmContentType = 0;
    try
    {
      this.stError = "";
      String stSql = "select * from t_dynamic y where y.nmDynId=" + this.nmDynId + " and y.nmSkinId=" + this.nmSkinId + " and y.nmLanguage=" + this.nmLanguage + " order by y.nmOrder";
      ResultSet rsD = this.dbEb.ExecuteSql(stSql);
      if (rsD != null)
      {
        rsD.last();
        int iMaxD = rsD.getRow();
        if (iMaxD >= 1)
        {
          for (int iD = 1; iD <= iMaxD; iD++)
          {
            rsD.absolute(iD);
            nmContentType = rsD.getInt("nmContentType");
            switch (nmContentType)
            {
            case 1:
              stReturn = stReturn + rsD.getString("stContent");
              break;
            case 2:
              stTemp = rsD.getString("stContent");
              if (stTemp.equals(""))
              {
                stTemp = createContent(rsD);
              }
              stReturn = stReturn + stTemp;
              break;
            case 3:
              stReturn = stReturn + stHTML;
              break;
            case 4:
              stReturn = stReturn + stErr;
              break;
            case 30:
              stReturn = stReturn + this.ebMb.makeMenuBar(rsD.getString("stContent"));
              break;
            case 40:
            case 50:
              break;
            default:
              stReturn = stReturn + "<hr>TODO:nmContentType " + nmContentType + "<hr>";
            }
          }
        }
        else
        {
          this.stError = (this.stError + "<br>ERROR fmtHTML: NO DYN " + stSql);
        }
      }
      else {
        this.stError = (this.stError + "<br>ERROR fmtHTML: " + stSql + " eb: " + this.dbEb.getError());
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>ERROR fmtHTML: " + e);
    }
    if (!this.stError.equals(""))
    {
      stReturn = stReturn + "<hr>FATAL ERROR1: " + this.stError + " err: " + stErr + "<hr>" + stHTML;
    }
    if (stReturn.equals(""))
    {
      stReturn = stReturn + "<hr>FATAL ERROR2: " + stErr + "<hr>" + stHTML;
    }
    return stReturn;
  }

  public String EBEncrypt(String stE)
  {
    return this.cCrypt.encrypt(stE);
  }

  public String EBDecrypt(String stE)
  {
    return this.cCrypt.decrypt(stE);
  }

  public String getAllErrors()
  {
    String stReturn = this.stError;
    if (this.ebAdmin != null)
    {
      this.stError += this.ebAdmin.getError();
    }

    if (this.dbEb != null)
    {
      stReturn = stReturn + this.dbEb.getError();
    }
    if (this.dbEnterprise != null)
    {
      stReturn = stReturn + this.dbEnterprise.getError();
    }
    if (this.dbDyn != null)
    {
      stReturn = stReturn + this.dbDyn.getError();
    }
    if (this.dbCommon != null)
    {
      stReturn = stReturn + this.dbCommon.getError();
    }
    if (this.ebSec != null)
    {
      stReturn = stReturn + this.ebSec.getError();
    }
    if (this.ebUd != null)
    {
      stReturn = stReturn + this.ebUd.getError();
    }
    if (this.cCrypt != null)
    {
      stReturn = stReturn + this.cCrypt.getError();
    }
    if (this.ebMb != null)
    {
      stReturn = stReturn + this.ebMb.getError();
    }
    return stReturn;
  }

  public void ebClose()
  {
    if (this.dbEb != null)
    {
      this.dbEb.ebClose();
    }
    if (this.dbEnterprise != null)
    {
      this.dbEnterprise.ebClose();
    }
    if (this.dbDyn != null)
    {
      this.dbDyn.ebClose();
    }
    if (this.dbCommon != null)
    {
      this.dbCommon.ebClose();
    }
  }

  public void logError(String stErr)
  {
    System.out.println(stErr);
  }

  public int getDbType()
  {
    return this.iDbType;
  }

  public String getHost()
  {
    return this.stHost;
  }

  public String getUser()
  {
    return this.stUser;
  }

  public String getPassword()
  {
    return this.stPassword;
  }

  String unSubscribe(String stEMail)
  {
    String stReturn = "<html><body><br>&nbsp;<br>&nbsp;<p>Sorry to see you leave: <b>" + stEMail;
    stEMail = this.dbEnterprise.fmtDbString(stEMail);
    if (this.dbEnterprise.ExecuteSql1n("select count(*) from X25DoNotCall where stValue=" + stEMail) <= 0)
    {
      String stSql = "insert into X25DoNotCall (stValue, nmFlags, dtEntered ) values(" + stEMail + ",2,now())";
      this.dbEnterprise.ExecuteUpdate(stSql);
    }
    this.dbEnterprise.ExecuteUpdate("update X25User set nmPriviledge=0 where stEMail=" + stEMail);
    stReturn = stReturn + "</b><br>You have been removed.<br></body></html>";
    return stReturn;
  }

  String getTOC(int nmMainDb)
  {
    if (this.ebDyn == null)
    {
      this.ebDyn = new EbDynamic(this);
    }
    return this.ebDyn.getTOC(nmMainDb);
  }
}
