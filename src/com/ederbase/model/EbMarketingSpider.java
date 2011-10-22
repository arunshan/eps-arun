package com.ederbase.model;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;

public class EbMarketingSpider
{
  private EbEnterprise ebEnt = null;
  private String stCountry = "US";
  private String stZip = "";
  private String stError = "";
  private int iPageId = 0;
  private HttpServletRequest request = null;

  public EbMarketingSpider(EbEnterprise ebEnt, HttpServletRequest request)
  {
    this.ebEnt = ebEnt;
    this.request = request;
  }

  public String doSpider(int iType)
  {
    String stReturn = "<br>EbMarketingSpider ";
    String stUrl = "";
    String stStart = "";
    String stA = "";
    long lTime1 = 0L;
    long lTime2 = 0L;
    try
    {
      Calendar cal = Calendar.getInstance();
      lTime1 = cal.getTimeInMillis();
      EbHtmlParser1 eb = new EbHtmlParser1();
      stA = this.request.getParameter("a");
      this.stZip = this.request.getParameter("zip");
      if ((this.stZip != null) && (this.stZip.length() >= 5))
      {
        iType = Integer.parseInt(this.request.getParameter("iType"));
        stStart = this.request.getParameter("startpage");
        if ((stStart == null) || (stStart.equals("")))
          stStart = "1";
        this.iPageId = Integer.parseInt(stStart);
      }
      switch (iType)
      {
      case 1:
        stReturn = stReturn + "<br><form method=post><table><tr><td>Type: </td><td><select name=iType><option value=3>Agents</option><option value=2>Yellowpages</option></td></tr><tr><td>Enter Zip: </td><td><textarea name=zip rows=10 cols=80>91306</textarea></td></tr><tr><td>Start Page: </td><td><input type=text name=startpage value='1'></td></tr><tr><td colspan=2 align=center><input type=hidden name=a value='" + stA + "'><input type=submit name=submit value='Start'></form></td></tr></table>";

        break;
      case 2:
      case 3:
        break;
      default:
        stReturn = stReturn + "<br>ERROR: doSpider Type: " + iType + " not yet implemented ";
      }

      int iLoop = 1;
      if (this.stZip.length() >= 5)
      {
        String[] aZip = this.stZip.split("\n");
        for (int iZ1 = 0; iZ1 < aZip.length; iZ1++)
        {
          stUrl = "";
          if (aZip[iZ1].length() >= 5)
          {
            switch (iType)
            {
            case 3:
              stUrl = "http://www.socalmls.com/Public/AgentSearch/Results.aspx?SearchType=agent&FirstName=&LastName=&OfficeName=&Address=&City=&State=ca&Zip=" + aZip[iZ1].trim() + "&Languages=&Titles=&Specialties=&Accreditations=&rpp=50&page=" + stStart + "&SortOrder=";
              break;
            case 2:
              stUrl = "http://www.yellowpages.com/" + aZip[iZ1].trim() + "/Real-Estate-Agents?page=" + stStart + "&search_terms=real-estate";
            }

          }

          while ((!stUrl.equals("")) && (iLoop++ < 100))
          {
            Calendar calb = Calendar.getInstance();
            long lTime1b = calb.getTimeInMillis();
            int iCount = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from X25WebSpider where stUrl = \"" + stUrl + "\" ");
            stReturn = stReturn + "<hr>[" + iZ1 + "] Starting " + stUrl + " count: " + iCount + " Time: " + (lTime1b - lTime1);
            if (iCount == 0)
            {
              String[] aReturn = eb.doParse(this.ebEnt.dbEnterprise, this.ebEnt.getMainCompanyId(), stUrl);
              stUrl = "";
              switch (iType)
              {
              case 2:
                stReturn = stReturn + aReturn[0];
                stUrl = saveCompanies(eb.getCompanies());
                stReturn = stReturn + runSpiderWeb();
                if (stUrl.length() < 0)
                  break;
                stUrl = "http://www.yellowpages.com" + stUrl;
                stReturn = stReturn + "<br>&nbsp;<br><a href=\"" + stUrl + "\">" + stUrl + "</a><br>&nbsp;"; break;
              case 3:
                stReturn = stReturn + aReturn[0];
                stUrl = saveAgents(eb.getCompanies());
                if (stUrl.length() > 0)
                {
                  this.iPageId += 1;
                  stUrl = "http://www.socalmls.com/Public/AgentSearch/Results.aspx?SearchType=agent&FirstName=&LastName=&OfficeName=&Address=&City=&State=ca&Zip=" + aZip[iZ1].trim() + "&Languages=&Titles=&Specialties=&Accreditations=&rpp=50&page=" + this.iPageId + "&SortOrder=";
                  stReturn = stReturn + "<br>&nbsp;<br><a href=\"" + stUrl + "\">" + stUrl + "</a><br>&nbsp;";
                }
                else {
                  stUrl = "";
                }
              }
            }
            else
            {
              stReturn = stReturn + "<br>Already done: " + stUrl;
              stUrl = "";
            }
          }
        }
      }
      Calendar cal2 = Calendar.getInstance();
      lTime2 = cal2.getTimeInMillis();
      stReturn = stReturn + "<br>Duration: " + (lTime2 - lTime1) + " ms ";
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>ERROR: " + e);
      System.out.println("<br>ERROR: " + e);
    }
    return stReturn;
  }

  public String runSpiderWeb()
  {
    String stReturn = "";
    String stUrl = "";
    EbHtmlParser1 eb = new EbHtmlParser1();
    try
    {
      String stSql = "SELECT w.* FROM X25Website w left join X25WebSpider ws on w.nmWebId=ws.nmReferWeb where w.nmStatusFlag=0 and ws.nmSpiderId is null";
      ResultSet rsWs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rsWs != null)
      {
        rsWs.last();
        int iMax = rsWs.getRow();
        for (int iW = 1; iW <= iMax; iW++)
        {
          rsWs.absolute(iW);
          stUrl = rsWs.getString("stUrl");
          String[] aReturn = eb.doParse(this.ebEnt.dbEnterprise, rsWs.getInt("nmCompanyId"), stUrl);
          stReturn = stReturn + aReturn[0];
          String[] aL1 = aReturn[1].split("\n");
          String stDomain = getDomain(stUrl);
          for (int iL1 = 0; iL1 < aL1.length; iL1++)
          {
            parsePage(1, stDomain, stUrl, rsWs.getInt("nmCompanyId"), aL1[iL1]);
          }
        }
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>ERROR: spiderWebs: " + e);
    }
    return stReturn;
  }

  public String getDomain(String stUrl)
  {
    String stReturn = "";
    if ((stUrl != null) && (stUrl.length() > 8))
    {
      if (stUrl.substring(0, 7).equals("http://"))
        stUrl = stUrl.substring(7);
      else if (stUrl.substring(0, 8).equals("https://"))
        stUrl = stUrl.substring(8);
      int iPos = stUrl.indexOf('/');
      if (iPos > 0)
        stUrl = stUrl.substring(0, iPos);
      int iLastDot = stUrl.lastIndexOf('.');
      int iLastDot2 = 0;
      if (iLastDot > 0)
        iLastDot2 = stUrl.lastIndexOf('.', iLastDot - 1);
      if (iLastDot2 > 0) {
        stUrl = stUrl.substring(iLastDot2 + 1);
      }
      stReturn = stUrl;
    }
    return stReturn;
  }

  private void parsePage(int iLevel, String stDomain, String stUrl, int nmCompanyId, String stLine)
  {
    EbHtmlParser1 eb = new EbHtmlParser1();
    try
    {
      if ((stUrl != null) && (stUrl.length() > 10) && (stLine != null) && (stLine.length() > 0))
      {
        if (stLine.toLowerCase().indexOf("javascript") <= 0)
        {
          if (stLine.toLowerCase().indexOf("mailto:") > 0)
          {
            addCompanyEmail(nmCompanyId, stLine);
          }
          else {
            String stUrl1 = makeUrl1(stDomain, stUrl, stLine);
            if (!stUrl1.equals(""))
            {
              String[] aR = eb.doParse(this.ebEnt.dbEnterprise, nmCompanyId, stUrl1);
              if (iLevel < 3)
              {
                String[] aL = aR[1].split("\n");
                for (int iL = 0; iL < aL.length; iL++)
                {
                  parsePage(iLevel + 1, stDomain, stUrl, nmCompanyId, aL[iL]);
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR: parsePage " + e);
    }
  }

  private String addCompanyEmail(int nmCompanyId, String stLine)
  {
    String stReturn = "";
    String stEmail = "";
    String[] aFields = stLine.split("\t");
    if (aFields.length > 0)
    {
      try
      {
        stEmail = aFields[1].trim().substring(7);
        EbNormalized ebNorm = new EbNormalized(this.ebEnt);
        int nmEmailId = ebNorm.getEmailId(stEmail);
        if (nmEmailId > 0)
          ebNorm.addUserRef(nmEmailId, 2, nmCompanyId);
      }
      catch (Exception e) {
        this.stError = (this.stError + "<BR>ERROR addCompanyEmail: " + stEmail + " : " + e);
      }
    }
    return stReturn;
  }

  private String makeUrl1(String stDomain, String stUrl, String stLine)
  {
    String stReturn = "";
    String stTemp = "";
    String[] aTemp = stLine.split("\t");
    if ((aTemp != null) && (aTemp.length > 0) && (!aTemp[1].trim().equals("")))
    {
      stTemp = aTemp[1].trim();
      if ((stTemp.length() > 7) && ((stTemp.substring(0, 7).equals("http://")) || (stTemp.substring(0, 8).equals("https://"))))
      {
        stReturn = stTemp;
      }
      if (stReturn.equals(""))
      {
        if ((!stUrl.equals("")) && (!aTemp[1].trim().equals("")))
        {
          stTemp = aTemp[1].trim();
          if ((stUrl.lastIndexOf('/') == stUrl.length()) || (stTemp.substring(0, 1).equals("/")))
            stReturn = stUrl + stTemp;
          else
            stReturn = stUrl + "/" + stTemp;
        }
      }
    }
    if ((!stReturn.equals("")) && ((stReturn.lastIndexOf('?') > 0) || (stReturn.lastIndexOf('#') > 0)))
    {
      stReturn = "";
    }
    if (stReturn.length() > stDomain.length())
    {
      if (stReturn.indexOf(stDomain) <= 0)
        stReturn = "";
    }
    else {
      stReturn = "";
    }
    return stReturn;
  }

  public String getError()
  {
    return this.stError;
  }

  public String saveCompanies(String stCompanies)
  {
    String stNext = "";
    EbCompany ebComp = null;

    if ((stCompanies != null) && (!stCompanies.equals("")))
    {
      String[] astAll = stCompanies.split("\\n");
      for (int iC = 0; iC < astAll.length; iC++)
      {
        String[] astFields = astAll[iC].split("\t");
        if (astFields[0].equals("COMPANY"))
        {
          if (ebComp != null)
            ebComp.save();
          ebComp = new EbCompany(this.ebEnt);
          ebComp.setCompany(astFields[1].trim());
        } else if (astFields[0].equals("NEXT"))
        {
          stNext = astFields[1].trim();
        } else if (astFields[0].equals("postal-code"))
        {
          if (astFields[1].trim().length() == 5)
            ebComp.setZip(astFields[1].trim());
        } else if (astFields[0].equals("locality"))
        {
          if (astFields[1].trim().length() > 3)
            ebComp.setCity(astFields[1].trim());
        } else if (astFields[0].equals("region"))
        {
          if (astFields[1].trim().length() >= 2)
            ebComp.setState(astFields[1].trim());
        } else if (astFields[0].equals("number"))
        {
          if (astFields[1].length() >= 10)
            ebComp.setPhone(astFields[1].trim());
        } else if (astFields[0].equals("EMAIL"))
        {
          if ((astFields[1].length() >= 10) && (astFields[1].trim().substring(0, 7).equals("mailto:")))
            ebComp.setEmail(astFields[1].trim().substring(7));
        } else if (astFields[0].equals("WEB"))
        {
          if (astFields[1].length() >= 4)
            ebComp.setWeb(astFields[1].trim()); 
        } else {
          if (!astFields[0].equals("street-address"))
            continue;
          if (astFields[1].length() >= 4)
            ebComp.setAddress(astFields[1].trim(), "");
        }
      }
      if (ebComp != null)
        ebComp.save();
    }
    return stNext;
  }

  public String saveAgents(String stCompanies)
  {
    String stNext = "";
    EbPerson ebPerson = null;
    EbCompany ebComp = null;

    if ((stCompanies != null) && (!stCompanies.equals("")))
    {
      String[] astAll = stCompanies.split("\\n");
      for (int iC = 0; iC < astAll.length; iC++)
      {
        String[] astFields = astAll[iC].split("\t");
        if (astFields[0].equals("AGENT"))
        {
          if (ebPerson != null)
            ebPerson.save(ebComp);
          ebComp = new EbCompany(this.ebEnt);
          ebPerson = new EbPerson(this.ebEnt);
          ebPerson.setName(astFields[2].trim());
        } else if (astFields[0].equals("OFFICE"))
        {
          ebComp.setCompany(astFields[1].trim());
          if ((astFields.length > 2) && (astFields[2].trim().startsWith("http://")))
            ebComp.setWeb(astFields[2].trim());
        } else if (astFields[0].equals("NEXT"))
        {
          stNext = astFields[1].trim();
        } else if (astFields[0].equals("ADDRESS"))
        {
          ebComp.setAddress(astFields[1].trim());
        } else if (astFields[0].equals("EMAIL"))
        {
          if ((astFields[1].length() >= 10) && (astFields[1].trim().substring(0, 7).equals("mailto:")))
            ebPerson.setEmail(astFields[1].trim().substring(7));
          else
            ebPerson.setEmail(astFields[1].trim());
        } else {
          if (!astFields[0].equals("PHONE"))
            continue;
          if (astFields[1].length() < 10)
            continue;
          ebComp.setPhone(astFields[1].trim());
          ebPerson.setPhone(astFields[1].trim());
        }
      }

      if (ebPerson != null)
        ebPerson.save(ebComp);
    }
    return stNext;
  }
}