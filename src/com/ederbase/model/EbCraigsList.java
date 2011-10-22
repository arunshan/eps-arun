/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ederbase.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Enumeration;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author Administrator
 */
public class EbCraigsList
{

  private OutString2 cbOut = null;
  private String stUrl = "";
  Calendar cal1 = Calendar.getInstance();
  long lTime1 = cal1.getTimeInMillis();
  long lTime2 = 0;
  EbEnterprise ebEnt = null;
  String stError = "";
  int iTot = 0;
  int nmLoginId = 0;
  int iSearch = 0;
  int iFollow = 0;
  int iExit = 0;
  String stMyTwitterId = "";

  public EbCraigsList(EbEnterprise ebEnt)
  {
    this.ebEnt = ebEnt;
  }

  public String getError()
  {
    return this.stError;
  }

  public String setEmail()
  {
    String stReturn = "";
    try
    {
      String stSql = "SELECT * FROM X25WebSpider where stError='cl' and nmAnalyzeCount=0 order by nmSpiderId ASC limit 10000";
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        stReturn += "<table border=1>";
        for (int iR = 1; iR <= iMax; iR++)
        {
          rs.absolute(iR);
          stReturn += "<tr>";
          stReturn += "<td>" + iR + "</td>";
          //stReturn += "<td>" + rs.getString("stUrl") + "</td>";
          stReturn += "<td bgcolor=yellow valign=top>";

          String stTemp = rs.getString("stWebContent");
          int iAt = 0;
          int iDot = 0;
          int iBeg = 0;
          int iEnd = 0;
          iBeg = stTemp.indexOf("<title>");
          iEnd = stTemp.indexOf("</title>");
          String stTitle = "";
          if ( (iBeg + 7) < iEnd )
            stTitle = stTemp.substring(iBeg + 7, iEnd);
          stReturn += "<BR><b>" + stTitle + "</b><br>";
          String[] astEMail = new String[100];
          int iECount = 0;
          while (iAt < stTemp.length() && (iAt = stTemp.indexOf("@", iAt)) > 30)
          {
            iDot = stTemp.indexOf(".", iAt);
            if (iDot > iAt && (iDot - iAt) < 25)
            {
              iBeg = 0;
              for (int i = 0; i < 30 && i < iAt && iBeg == 0; i++)
              {
                char ch = stTemp.charAt(iAt - i);
                switch (ch)
                {
                  case '\n':
                  case '\r':
                  case '\t':
                  case ' ':
                  case ',':
                  case ';':
                  case '{':
                  case '}':
                  case '(':
                  case ')':
                  case '[':
                  case ']':
                  case ':':
                  case '&':
                  case '=':
                  case '+':
                  case '-':
                  case '!':
                  case '?':
                  case '/':
                  case '\\':
                  case '<':
                  case '>':
                    iBeg = iAt - i + 1;
                    break;
                }
              }
              iEnd = 0;
              for (int i = 0; i < 10 && (iDot + i) < stTemp.length() && iEnd == 0; i++)
              {
                char ch = stTemp.charAt(iDot + i);
                switch (ch)
                {
                  case '\n':
                  case '\r':
                  case '\t':
                  case ' ':
                  case ',':
                  case ';':
                  case '{':
                  case '}':
                  case '(':
                  case ')':
                  case '[':
                  case ']':
                  case ':':
                  case '&':
                  case '=':
                  case '+':
                  case '-':
                  case '!':
                  case '?':
                  case '/':
                  case '\\':
                  case '<':
                  case '>':
                    iEnd = iDot + i;
                    break;
                }
              }
              if (iBeg > 0 && iEnd > iBeg)
              {
                astEMail[iECount] = stTemp.substring(iBeg, iEnd);
                if (astEMail[iECount].charAt(astEMail[iECount].length() - 1) == '.' ||
                    astEMail[iECount].charAt(astEMail[iECount].length() - 1) == ',')
                {
                  astEMail[iECount] = astEMail[iECount].substring(0, astEMail[iECount].length() - 1);
                }
                stReturn += "<BR>" + astEMail[iECount];
                iECount++;
              }
            }
            iAt += 10; // need a min distance
          }

          stReturn += "</td><td>" + rs.getString("nmLength") + "</td>";
          //stReturn += "<td>" + rs.getString("stWebContent") + "</td>";

          // Sending email
          String stEMail = "";
          for (int i = 0; i < iECount; i++)
          {
            if (!astEMail[i].endsWith("@craigslist.org"))
            {
              stEMail = astEMail[i];
            }
          }
          if (stEMail.equals("") && iECount > 0)
            stEMail = astEMail[0];
          stReturn += "<td bgcolor=skyblue valign=top>" + stEMail + "</td>";
          this.ebEnt.dbEnterprise.ExecuteUpdate("update X25WebSpider set nmAnalyzeCount=nmAnalyzeCount+1, " +
              "stEmails=" + this.ebEnt.dbEnterprise.fmtDbString(stEMail) +
              ", stDescription = " + this.ebEnt.dbEnterprise.fmtDbString(stTitle) +
              " where nmSpiderId=" + rs.getString("nmSpiderId"));
          stReturn += "</tr>";
        }
        stReturn += "</table>";
      }
    } catch (Exception e)
    {
      e.printStackTrace();
      this.stError += "<BR>ERROR EbCraigsList.sendEmail: " + e;
    }
    return stReturn;
  }

  public String processCraigsList(int iType)
  {
    String stReturn = "";
    String stPid = this.ebEnt.ebUd.request.getParameter("pid");
    if (stPid != null && stPid.length() > 0)
    {
      int iPid = Integer.parseInt(stPid);
      switch (iPid)
      {
        case 1:
          stReturn += readCL(1);
          break;

        case 2:
          //stReturn += this.syncFollowers();
          break;

        case 3:
          //stReturn += this.getTwitterDetail(this.ebEnt.ebUd.request.getParameter("tw"));
          break;

        default:
          stReturn += "<br>TODO PID: " + stPid;
          break;

      }
    } else
    {
      stReturn = "<center><h1>CraigsList Marketing Home Page</h1><table border=1>" +
          "<tr><td valign=top>TODO";

      stReturn += "</td><td valign=top><h1>Available Processes</h1><ul><br>";
      stReturn += "<li> <a href='./?" + this.ebEnt.ebUd.request.getQueryString() + "&pid=1'>Read all Gigs</a>";
      stReturn += "</ul></td></tr></table>";
    }
    return stReturn;
  }

  public String readCL(int iType)
  {
    String stReturn = "";
    String[] astGigs =
    {
      //"/ccc/", "/hhh/", "/sss/", "/bbb/", "/jjj/", "/ggg/"
      "/jjj/", "/ggg/"
    };

    try
    {
      if (iType == 1) // Read ALL
      {
        String[] astValues = doParse("http://losangeles.craigslist.org/", 0); // From the ROOT - get US CITIES
        stReturn += astValues[0];
        String[] astUs = astValues[1].split("\n");
        stReturn += "<table><tr><th>#</th><th>City</th><th>Gigs</th><th>Jobs</th></tr>";
        for (int iC = 0; iC < astUs.length; iC++)
        {
          String[] astUrl = astUs[iC].split("\t");
          if (astUrl != null && astUrl.length > 1)
          {
            stReturn += "<tr>";
            stReturn += "<td valign=top align=right>" + iC + "</td>";
            stReturn += "<td valign=top align=left>" + astUrl[2] + "</td><td valign=top align=left>";

            astValues = doParse(astUrl[1], 0);
            // here is where we should loop for all other cities
            String[] astOther = astValues[2].split("\n");
            for (int iO = 0; iO < astOther.length; iO++)
            {
              String[] astUrlOther = astOther[iO].split("\t");
              if (astUrlOther != null && astUrlOther.length > 1)
              {
                stReturn += "<tr>";
                stReturn += "<td valign=top align=right>" + iC + "</td>";
                stReturn += "<td valign=top align=right>" + iO + "</td>";
                stReturn += "<td valign=top align=left>" + astUrlOther[2] + "</td><td valign=top align=left>";

                for (int iG = 0; astGigs != null && iG < astGigs.length; iG++)
                {
                  stReturn += "<br>" + astUrlOther[2] + astGigs[iG];
                  String[] astValues2 = doParse(astUrlOther[1].trim() + astGigs[iG], 0);
                  String[] astList1 = astValues2[5].split("\n");
                  for (int iL = 0; iL < astList1.length; iL++)
                  {
                    String[] astU1 = astList1[iL].split("\t");
                    if (astU1 != null && astU1.length > 1)
                    {
                      String[] astValues3 = null;
                      if ( astU1[1].trim().substring(0,7).equals("http://"))
                        astValues3 = doParse( astU1[1].trim(), 1);
                      else
                      {
                        if ( astU1[1].trim().substring(0,1).equals("/"))
                          astValues3 = doParse(astUrlOther[1].trim() + astU1[1].trim(), 1);
                        else
                          astValues3 = doParse(astUrlOther[1].trim() + "/" + astU1[1].trim(), 1);
                      }
                      if (astValues3[6].indexOf("already") < 0)
                      {
                        stReturn += "<br>&nbsp;&nbsp;(" + iL + ") " + astUrl[1].trim() + astU1[1].trim();
                        stReturn += " &nbsp;: " + astValues3[6];
                      } else
                      {
                        iL = astList1.length + 10; // indicate end. wont allow break; ????
                        break; // first already found, let's stop
                      }
                    }
                  }
                }
                stReturn += "</td></tr>";
              }
            }
          }
        }
        stReturn += "</table>";
      }
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR readCL: " + e;
    }
    return stReturn;
  }

  public String[] doParse(String stUrl, int iType) throws Exception
  {
    int nmCompanyId = -1; // TODO
    this.stUrl = stUrl;
    this.cbOut = new OutString2();
    String[] aReturn = new String[7];
    aReturn[0] = "<br>HtmlParser: <b>" + stUrl + "</b><br>";
    aReturn[1] = "";
    int iCount = 0;
    try
    {
      if (iType == 1)
        iCount = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) as cnt from X25WebSpider where stUrl = \"" + stUrl + "\" ");
      if (iCount == 0)
      {
        ParserGetter kit = new ParserGetter();
        HTMLEditorKit.Parser parser = kit.getParser();

        String encoding = "ISO-8859-1";
        //URL url = new URL(stUrl);
        //
        HttpTimeoutHandler xHTH = new HttpTimeoutHandler(15000);	// timeout value in milliseconds
        URL url = new URL((URL) null, stUrl, xHTH); // timeout value in milliseconds

        // the next step is optional
        ////CRASHES url.setURLStreamHandlerFactory(new HttpTimeoutFactory(150));
        URLConnection theURLconn = url.openConnection();
        theURLconn.connect();

        InputStream in = url.openStream();

        InputStreamReader r = new InputStreamReader(in, encoding);
        // parse once just to detect the encoding
        HTMLEditorKit.ParserCallback doNothing = new HTMLEditorKit.ParserCallback();

        parser.parse(r, doNothing, true);
        parseUrl(url, encoding);
        aReturn[1] = cbOut.getUsHrefList();
        aReturn[2] = cbOut.getOtherHrefList();
        aReturn[3] = cbOut.getJobsHrefList();
        aReturn[4] = cbOut.getGigsHrefList();
        aReturn[5] = cbOut.getHrefList();
        aReturn[6] = "?";

        aReturn[0] += "<hr>DONE parsing ";
        aReturn[0] += "<hr><font size=1>" + cbOut.getString();
        if (iType == 1)
        {
          BufferedReader in1 = new BufferedReader(new InputStreamReader(theURLconn.getInputStream()));
          String stContent = "";
          String stLine = "";
          while ((stLine = in1.readLine()) != null)
          {
            stContent += stLine + "\n";
          }
          in1.close();
          int nmStatus = 0;

          if (stUrl.indexOf("cpg/") >= 0 ||
              stUrl.indexOf("eng/") >= 0 ||
              //stUrl.indexOf("mar/") >= 0 ||  // removed 2/10/2010 .. too many bad calls
              stUrl.indexOf("web/") >= 0)
            nmStatus = 1;
          else
            nmStatus = 4;
          logWebSpider(this.ebEnt.dbEnterprise, nmCompanyId, nmStatus, "cl", stContent, stContent.length());
          aReturn[6] = "" + stContent.length();
        }
      } else
      {
        aReturn[0] += "<hr>Already parsed<hr>";
        aReturn[6] = "already";
      }
    } catch (InterruptedIOException e)
    {
      System.out.println("<br>ERROR: doParse: timeout " + e);
      aReturn[0] += "<br>ERROR: doParse: timeout " + e;
      logWebSpider(this.ebEnt.dbEnterprise, nmCompanyId, 2, e.toString(), "", 0);
    } catch (IOException e)
    {
      System.out.println("<br>ERROR: doParse: " + e);
      aReturn[0] += "<br>ERROR: doParse: " + e;
      aReturn[5] = cbOut.getHrefList();
      logWebSpider(this.ebEnt.dbEnterprise, nmCompanyId, 2, e.toString(), "", 0);
    } catch (Exception e)
    {
      System.out.println("<br>ERROR: doParse: " + e);
      aReturn[0] += "<br>ERROR: doParse: " + e;
      aReturn[5] = cbOut.getHrefList();
      logWebSpider(this.ebEnt.dbEnterprise, nmCompanyId, 2, e.toString(), "", 0);
    }
    return aReturn;
  }
  //private String stWebContent = ""; don't know how to read it ... not important to save anyways.

  private void parseUrl(URL stUrl, String stEncoding) throws IOException
  {
    ParserGetter kit = new ParserGetter();
    HTMLEditorKit.Parser parser = kit.getParser();
    InputStream in = stUrl.openStream();
    InputStreamReader r = new InputStreamReader(in, stEncoding);
    HTMLEditorKit.ParserCallback callback = new Outliner2(this.cbOut);
    parser.parse(r, callback, true);
  }

  public String logWebSpider(EbDatabase dbEnterprise, int nmCompanyId, int nmStatus, String stError, String stContent, int iLen)
  {
    String stReturn = "";
    String stSql = "";

    int nmSpiderId = dbEnterprise.ExecuteSql1n("select max(nmSpiderId) from X25WebSpider where stUrl=\"" + this.stUrl + "\" ");
    if (nmSpiderId <= 0)
    {
      nmSpiderId = dbEnterprise.ExecuteSql1n("select max(nmSpiderId) from X25WebSpider ");
      nmSpiderId++;
      stSql = "insert into X25WebSpider (nmSpiderId,nmStatus,stURL,nmLength,dtFirst,dtLast,nmCompanyId,stHrefList,stError,stWebContent) values" +
          "(" + nmSpiderId + "," + nmStatus + ",\"" + stUrl + "\"," + iLen + ",now(),now()," + nmCompanyId + ", " + dbEnterprise.fmtDbString(this.cbOut.getHrefList()) + ", " + dbEnterprise.fmtDbString(stError) + "," + dbEnterprise.fmtDbString(stContent) + ") ";
    } else
    {
      stSql = "update X25WebSpider set dtLast=now(),nmStatus=" + nmStatus + ",nmLength= " + cbOut.getLength() + " ,nmCompanyId=" + nmCompanyId + ",stHrefList= " + dbEnterprise.fmtDbString(this.cbOut.getHrefList()) + " where nmSpiderId= " + nmSpiderId;
    }
    dbEnterprise.ExecuteUpdate(stSql);
    return stReturn;
  }

  public String getCompanies()
  {
    return cbOut.getCompanies();
  }
}

class Outliner2 extends HTMLEditorKit.ParserCallback
{

  private OutString2 cbOut = null;
  private String stOut = "";
  private int iStart = 0;
  private String stLastClass = "";
  private String stAll = "";
  private String stLastHref = "";
  private static String lineSeparator = System.getProperty("line.separator", "\r\n");
  private int iInHref = 0;
  private int inH5 = 0;
  private int inOtherCities = 0;
  private int inUsCities = 0;
  private int inJobs = 0;
  private int inGigs = 0;

  public Outliner2(OutString2 cOut)
  {
    this.cbOut = cOut;
    this.stOut = "";
  }

  @Override
  public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position)
  {
    if (tag == HTML.Tag.H5 || tag == HTML.Tag.H4)
      inH5 = 1;

    this.parseAttributes(tag, attributes);
  }

  private void parseAttributes(HTML.Tag tag, MutableAttributeSet attributes)
  {
    Enumeration e = attributes.getAttributeNames();
    while (e.hasMoreElements())
    {
      Object name = e.nextElement();
      String value = "";
      if (name == HTML.Attribute.HREF)
      {
        value = (String) attributes.getAttribute(name);
        stLastHref = value;
        if (value.trim().indexOf(".html") >= 0)
          cbOut.addHrefList("\n\t" + value.trim() + "\t");
        if (inUsCities > 0)
          cbOut.addUsHrefList("\n\t" + value + "\t");
        else if (inOtherCities > 0)
          cbOut.addOtherHrefList("\n\t" + value + "\t");
        //else if (inJobs > 0)
        //  cbOut.addGigsHrefList("\n\t" + value + "\t");
        //cbOut.addJobsHrefList("\n\t" + value + "\t");
        //else if (inGigs > 0)
        //cbOut.addGigsHrefList("\n\t" + value + "\t");

        iInHref = 1;
      } else if (tag == HTML.Tag.FRAME)
      {
        if (name == HTML.Attribute.SRC)
        {
          value = (String) attributes.getAttribute(name);
          stLastHref = value;
          cbOut.addHrefList("\n\t" + value + "\t");
        }
      }
    }
  }

  @Override
  public void handleEndTag(HTML.Tag tag, int position)
  {
    iInHref = 0;
    inH5 = 0;
    if (tag == HTML.Tag.HTML)
    {
      this.flush();
      this.cbOut.setLength(position);
    }
  }

  @Override
  public void handleText(char[] text, int position)
  {
    int iPos = 0;
    String stText = String.valueOf(text).trim();
    if (inH5 > 0)
    {
      inOtherCities = 0;
      inUsCities = 0;
      inJobs = 0;
      inGigs = 0;
      if (stText.equals("us cities"))
        inUsCities = 1;
      else if (stText.equals("other cities"))
        inOtherCities = 1;
      else if (stText.equals("jobs"))
        inJobs = 1;
      else if (stText.equals("gigs"))
        inGigs = 1;

    } else if (iInHref > 0)
    {
      if (inUsCities > 0)
        cbOut.addUsHrefList(stText);
      else if (inOtherCities > 0)
        cbOut.addOtherHrefList(stText);
    }
  }

  @Override
  public void flush()
  {
    if (stAll != null && !stAll.equals(""))
      cbOut.addCompanies(stAll);
    cbOut.addString(stOut);
  }
}

class OutString2
{

  private String stCompanies = "";
  private String stOut = "";
  int iLength = 0;
  private String stHrefList = "";
  private String stUsHrefList = "";
  private String stOtherHrefList = "";
  private String stJobsHrefList = "";
  private String stGigsHrefList = "";

  public String getHrefList()
  {
    return stHrefList + "\n";
  }

  public String getUsHrefList()
  {
    return stUsHrefList + "\n";
  }

  public String getOtherHrefList()
  {
    return stOtherHrefList + "\n";
  }

  public String getJobsHrefList()
  {
    return stJobsHrefList + "\n";
  }

  public String getGigsHrefList()
  {
    return stGigsHrefList + "\n";
  }

  public void addHrefList(String stAdd)
  {
      stHrefList += stAdd.substring(iLength);
  }

  public void addUsHrefList(String stAdd)
  {
    stUsHrefList += stAdd;
  }

  public void addOtherHrefList(String stAdd)
  {
    stOtherHrefList += stAdd;
  }

  public void addJobsHrefList(String stAdd)
  {
    if (stAdd.indexOf("gov/") < 0)
      stJobsHrefList += stAdd;
  }

  public void addGigsHrefList(String stAdd)
  {
    /*
    if (stAdd.indexOf("cpg/") >= 0 ||
    stAdd.indexOf("eng/") >= 0 ||
    stAdd.indexOf("mar/") >= 0 ||
    stAdd.indexOf("web/") >= 0 )
     */
    if (stAdd.indexOf("ccc/") >= 0 ||
        stAdd.indexOf("hhh/") >= 0 ||
        stAdd.indexOf("sss/") >= 0 ||
        stAdd.indexOf("bbb/") >= 0 ||
        stAdd.indexOf("jjj/") >= 0 ||
        stAdd.indexOf("ggg/") >= 0)
      stGigsHrefList += stAdd;
  }

  public void Outstring2()
  {
    this.stOut = "";
    this.stCompanies = "";
  }

  public int getLength()
  {
    return this.iLength;
  }

  public void setLength(int iLen)
  {
    this.iLength = iLen;
  }

  public void addCompanies(String stAll)
  {
    if (stCompanies == null)
      stCompanies = "";

    stCompanies += stAll;
  }

  public String getCompanies()
  {
    return this.stCompanies;
  }

  public void addString(String stAdd)
  {
    this.stOut += stAdd;
  }

  public String getString()
  {
    return this.stOut;
  }
}

