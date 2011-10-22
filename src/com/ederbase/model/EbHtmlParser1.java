package com.ederbase.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.*;
import javax.swing.text.html.HTMLEditorKit.Parser;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

public class EbHtmlParser1
{
  private OutString cbOut = null;
  private String stUrl = "";

  private void parseUrl(URL stUrl, String stEncoding)
    throws IOException
  {
    ParserGetter kit = new ParserGetter();
    HTMLEditorKit.Parser parser = kit.getParser();
    InputStream in = stUrl.openStream();
    InputStreamReader r = new InputStreamReader(in, stEncoding);
    HTMLEditorKit.ParserCallback callback = new Outliner(this.cbOut);
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
      stSql = "insert into X25WebSpider (nmSpiderId,nmStatus,stURL,nmLength,dtFirst,dtLast,nmCompanyId,stHrefList,stError,stWebContent) values(" + nmSpiderId + "," + nmStatus + ",\"" + this.stUrl + "\"," + iLen + ",now(),now()," + nmCompanyId + ", " + dbEnterprise.fmtDbString(this.cbOut.getHrefList()) + ", " + dbEnterprise.fmtDbString(stError) + "," + dbEnterprise.fmtDbString(stContent) + ") ";
    }
    else
    {
      stSql = "update X25WebSpider set dtLast=now(),nmStatus=" + nmStatus + ",nmLength= " + this.cbOut.getLength() + " ,nmCompanyId=" + nmCompanyId + ",stHrefList= " + dbEnterprise.fmtDbString(this.cbOut.getHrefList()) + " where nmSpiderId= " + nmSpiderId;
    }
    dbEnterprise.ExecuteUpdate(stSql);
    return stReturn;
  }

  public String[] doParse(EbDatabase dbEntertprise, int nmCompanyId, String stUrl) throws Exception
  {
    this.stUrl = stUrl;
    this.cbOut = new OutString();
    String[] aReturn = new String[2];
    aReturn[0] = ("<br>EbHtmlParser1: <b>" + stUrl + "</b><br>");
    aReturn[1] = "";
    try
    {
      int iCount = dbEntertprise.ExecuteSql1n("select count(*) as cnt from X25WebSpider where stUrl = \"" + stUrl + "\" ");
      if (iCount == 0)
      {
        ParserGetter kit = new ParserGetter();
        HTMLEditorKit.Parser parser = kit.getParser();

        String encoding = "ISO-8859-1";

        HttpTimeoutHandler xHTH = new HttpTimeoutHandler(15000);
        URL url = new URL((URL)null, stUrl, xHTH);

        URLConnection theURLconn = url.openConnection();
        theURLconn.connect();

        InputStream in = url.openStream();

        InputStreamReader r = new InputStreamReader(in, encoding);

        HTMLEditorKit.ParserCallback doNothing = new HTMLEditorKit.ParserCallback();

        parser.parse(r, doNothing, true);
        parseUrl(url, encoding);
        aReturn[1] = this.cbOut.getHrefList();
        int tmp219_218 = 0;
        String[] tmp219_216 = aReturn; tmp219_216[tmp219_218] = (tmp219_216[tmp219_218] + "<hr>DONE parsing ");
        int tmp243_242 = 0;
        String[] tmp243_240 = aReturn; tmp243_240[tmp243_242] = (tmp243_240[tmp243_242] + "<hr><font size=1>" + this.cbOut.getString());

        logWebSpider(dbEntertprise, nmCompanyId, 1, "ok", theURLconn.getContent().toString(), theURLconn.getContentLength());
      }
      else
      {
        int tmp303_302 = 0;
        String[] tmp303_300 = aReturn; tmp303_300[tmp303_302] = (tmp303_300[tmp303_302] + "<hr>Already parsed<hr>");
      }
    }
    catch (InterruptedIOException e) {
      System.out.println("<br>ERROR: doParse: timeout " + e);
      int tmp358_357 = 0;
      String[] tmp358_355 = aReturn; tmp358_355[tmp358_357] = (tmp358_355[tmp358_357] + "<br>ERROR: doParse: timeout " + e);
      logWebSpider(dbEntertprise, nmCompanyId, 2, e.toString(), "", 0);
    }
    catch (Exception e) {
      System.out.println("<br>ERROR: doParse: " + e);
      int tmp434_433 = 0;
      String[] tmp434_431 = aReturn; tmp434_431[tmp434_433] = (tmp434_431[tmp434_433] + "<br>ERROR: doParse: " + e);
      aReturn[1] = this.cbOut.getHrefList();
      logWebSpider(dbEntertprise, nmCompanyId, 2, e.toString(), "", 0);
    }
    return aReturn;
  }

  public String getCompanies()
  {
    return this.cbOut.getCompanies();
  }
}

class OutString
{
  private String stCompanies = "";
  private String stOut = "";
  int iLength = 0;
  private String stHrefList = "";

  public String getHrefList()
  {
    return this.stHrefList;
  }

  public void addHrefList(String stAdd)
  {
    this.stHrefList += stAdd;
  }

  public void Outstring()
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
    if (this.stCompanies == null) {
      this.stCompanies = "";
    }
    this.stCompanies += stAll;
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

class Outliner extends HTMLEditorKit.ParserCallback
{
  private OutString cbOut = null;
  private String stOut = "";
  private int iStart = 0;
  private String stLastClass = "";
  private String stAll = "";
  private String stLastHref = "";
  private static String lineSeparator = System.getProperty("line.separator", "\r\n");
  private int iInHref = 0;
  private int inPhone = 0;
  private int inOffice = 0;

  public Outliner(OutString cOut)
  {
    this.cbOut = cOut;
    this.stOut = "";
  }

  public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position)
  {
    parseAttributes(tag, attributes);
  }

  private void parseAttributes(HTML.Tag tag, MutableAttributeSet attributes)
  {
    Enumeration e = attributes.getAttributeNames();
    while (e.hasMoreElements())
    {
      Object name = e.nextElement();
      String value = (String)attributes.getAttribute(name);
      if (name == HTML.Attribute.CLASS)
      {
        this.stLastClass = value;
        if (value.equals("fn org"))
          this.iStart = 1;
        else if ((value.equals("options")) || (value.equals("rating")))
          this.iStart = 0;
      } else if (name == HTML.Attribute.HREF)
      {
        this.stLastHref = value;
        this.cbOut.addHrefList("\n\t" + value + "\t");
        this.iInHref = 1;
      } else if (name == HTML.Attribute.ID)
      {
        if (value.indexOf("_Phone") >= 0) {
          this.inPhone = 1;
        }
      }
      if (tag == HTML.Tag.FRAME)
      {
        if (name == HTML.Attribute.SRC)
        {
          this.stLastHref = value;
          this.cbOut.addHrefList("\n\t" + value + "\t");
        }
      }
    }
  }

  public void handleEndTag(HTML.Tag tag, int position)
  {
    this.iInHref = 0;
    this.inPhone = 0;
    if (tag == HTML.Tag.A)
      this.inOffice = 0;
    if (tag == HTML.Tag.HTML)
    {
      flush();
      this.cbOut.setLength(position);
    }
  }

  public void handleText(char[] text, int position)
  {
    int iPos = 0;
    if (this.iInHref > 0)
    {
      this.cbOut.addHrefList(String.valueOf(text));

      if (this.stLastHref.indexOf("PersonID=") >= 0)
      {
        this.stAll = (this.stAll + "AGENT\t" + this.stLastHref + "\t" + String.valueOf(text).trim() + "\n");
        this.stOut = (this.stOut + "<br>AGENT\t" + this.stLastHref + "\t<b>" + String.valueOf(text).trim() + "</b>\n");
      } else if ((iPos = this.stLastHref.indexOf("AgentEmailAddress=")) >= 0)
      {
        int iPos2 = this.stLastHref.indexOf("&", iPos);
        if (iPos2 > iPos)
        {
          this.stAll = (this.stAll + "EMAIL\t" + this.stLastHref.substring(iPos + 18, iPos2) + "\t\n");
          this.stOut = (this.stOut + "<br>EMAIL\t" + this.stLastHref.substring(iPos + 18, iPos2) + "\t\n");
        }
      } else if ((iPos = this.stLastHref.indexOf("OpenDrivingDirections")) >= 0)
      {
        iPos = this.stLastHref.indexOf("end=", iPos);
        int iPos2 = this.stLastHref.indexOf("&", iPos);
        if (iPos2 > iPos)
        {
          this.stAll = (this.stAll + "ADDRESS\t" + this.stLastHref.substring(iPos + 4, iPos2) + "\t\n");
          this.stOut = (this.stOut + "<br>ADDRESS\t" + this.stLastHref.substring(iPos + 4, iPos2) + "\t\n");
        }
      }
      if (this.stLastClass.equals("ao_office_name_container"))
      {
        if ((iPos = this.stLastHref.indexOf("OfficeID=")) > 0)
        {
          this.stAll = (this.stAll + "OFFICE\t" + String.valueOf(text).trim() + "\t\n");
          this.stOut = (this.stOut + "<br>OFFICE\t" + String.valueOf(text).trim() + "\t\n");
        }
        else {
          this.stAll = (this.stAll + "OFFICE\t" + String.valueOf(text).trim() + "\t" + this.stLastHref + "\n");
          this.stOut = (this.stOut + "<br>OFFICE\t" + String.valueOf(text).trim() + "\t" + this.stLastHref + "\n");
        }
      }
    }

    if (this.inPhone > 0)
    {
      String stPhone = String.valueOf(text).trim();
      if ((stPhone != null) && (stPhone.length() >= 10))
      {
        this.stAll = (this.stAll + "PHONE\t" + stPhone + "\t\n");
        this.stOut = (this.stOut + "<br>PHONE\t" + stPhone + "\t\n");
      }
    }
    if (this.stLastClass.equals("ao_page_controls_text"))
    {
      if (String.valueOf(text).trim().equals(">"))
      {
        this.stAll = (this.stAll + "NEXT\t" + this.stLastHref + "\t\n");
        this.stOut = (this.stOut + "<br>NEXT: yes <b>" + this.stLastHref + "</b>");
      }

    }

    if (this.iStart > 0)
    {
      if ((this.stLastClass.equals("fn org")) || (this.iStart == 1))
      {
        if ((this.stAll != null) && (!this.stAll.equals("")))
          this.cbOut.addCompanies(this.stAll);
        this.stAll = ("COMPANY\t" + String.valueOf(text) + "\t\n");
        this.stOut = (this.stOut + "<hR><b>COMPANY: " + String.valueOf(text) + "</b>");
        this.iStart += 1;
      } else if (this.stLastClass.equals("email"))
      {
        this.stAll = (this.stAll + "EMAIL\t" + this.stLastHref + "\t\n");
        this.stOut = (this.stOut + "<BR>EMAIL: " + this.stLastHref);
      } else if ((this.stLastClass.equals("web")) || (this.stLastClass.equals("main_web_site")))
      {
        this.stAll = (this.stAll + "WEB\t" + this.stLastHref + "\t\n");
        this.stOut = (this.stOut + "<BR>Web: " + this.stLastHref);
      }
      else {
        this.stAll = (this.stAll + this.stLastClass + "\t" + String.valueOf(text) + "\t\n");
        this.stOut = (this.stOut + "<br>[" + this.stLastClass + " " + this.iStart + "] " + String.valueOf(text));
      }
    }
    if (this.stLastClass.equals("next"))
    {
      this.stAll = (this.stAll + "NEXT\t" + this.stLastHref + "\t\n");
      this.stOut = (this.stOut + "<br>NEXT: " + String.valueOf(text) + " <b>" + this.stLastHref + "</b>");
    }
  }

  public void flush()
  {
    if ((this.stAll != null) && (!this.stAll.equals("")))
      this.cbOut.addCompanies(this.stAll);
    this.cbOut.addString(this.stOut);
  }
}

class ParserGetter extends HTMLEditorKit
{
  public HTMLEditorKit.Parser getParser()
  {
    return super.getParser();
  }
}