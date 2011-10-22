package com.ederbase.model;

import java.sql.ResultSet;

public class EbMenuBar
{
  private String stCompany;
  private String stLogo;
  private String stTitle;
  private String stLoginName;
  private int iL1Set;
  private int iL2Set;
  private int iLinkMax = 0;
  private int iL1Max = 0;
  private int iL2Max = 0;
  private String stError = "";
  private String stL = "";
  private String stM1 = "";
  private String[] astM2 = new String[300];

  public EbMenuBar(String stCompany, String stLogo, String stTitle, String stLoginName, ResultSet rsA)
  {
    try
    {
      this.stCompany = stCompany;
      this.stLogo = stLogo;
      this.stTitle = stTitle;
      this.stLoginName = stLoginName;
      this.iL1Set = 0;
      this.iL2Set = 0;
      this.stM1 = "";
      for (int i = 0; i < this.astM2.length; i++)
      {
        this.astM2[i] = "";
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR EbMenuBar: " + stCompany + " stLogo " + stLogo + " stTitle: " + stTitle + " stLoginName: " + stLoginName + " Exception: " + e);
    }
  }

  public String makeMenuBar(String stMb)
  {
    String stReturn = "";
    stReturn = stReturn + "\n<tr><td valign='top' align='left' rowspan='2'><img src=\"" + this.stLogo + "\" title=\"" + this.stCompany + "\" alt=\"" + this.stCompany + "\" border='0'></td><td valign='top' align='left' width='100%'><table width='100%'><tr><td rowspan='2' align='center' valign='middle'><h1>" + this.stCompany + "</h1></td><td align='right' valign='middle'>";
    stReturn = stReturn + this.stL;
    stReturn = stReturn + "</td></tr>\n<tr><td align='right' valign='middle'>";
    if (!this.stLoginName.trim().equals(""))
      stReturn = stReturn + "<font size='1'>Welcome <b>" + this.stLoginName + "</b> &nbsp;</font>";
    else
      stReturn = stReturn + "&nbsp;";
    stReturn = stReturn + "</td></tr></table></td></tr>";
    stReturn = stReturn + "<tr><td valign='bottom' align='left' width='100%'><table id='menu' cellspacing='0'><tr><td>&nbsp;&nbsp;</td><td><div id='menu1Wrap'>";
    stReturn = stReturn + this.stM1;
    stReturn = stReturn + "</div></td></tr></table></td></tr>\n<tr><td colspan='2 'valign='top' align=left width='100%'><table id='menu2' cellspacing='0'><tr><td><div id='menu2Wrap'>";
    stReturn = stReturn + this.astM2[this.iL1Set];
    stReturn = stReturn + "</div></td></tr></table></td></tr>\n<tr><td colspan='2' width='100%' valign='top' align='left' id='tdbody'>";

    return stReturn;
  }

  public String addMenuBar(ResultSet rsA, int iSel)
  {
    String stReturn = "";
    try
    {
      String stUrl = rsA.getString("stUrl");
      if ((stUrl == null) || (stUrl.equals("")))
        stUrl = "./?a=" + rsA.getString("nmAppId");
      switch (rsA.getInt("nmAppType"))
      {
      case 90:
        this.iL1Max += 1;
        if ((iSel > 0) || (this.iL1Set == rsA.getInt("nmAppId")))
        {
          this.iL1Set = rsA.getInt("nmAppId");

          stReturn = stReturn + "<a href='" + stUrl + "' title=\"" + rsA.getString("stTitle") + "\" class='selectedTab'>" + rsA.getString("stTitle") + "</a>";
        }
        else {
          stReturn = stReturn + "<a href='" + stUrl + "' title=\"" + rsA.getString("stTitle") + "\">" + rsA.getString("stTitle") + "</a>";
        }
        this.stM1 += stReturn;
        break;
      case 80:
        this.iL2Max += 1;
        if (iSel > 0)
        {
          this.iL2Set = rsA.getInt("nmAppId");
          this.iL1Set = rsA.getInt("nmAppParent");
          stReturn = stReturn + "<a href='" + stUrl + "' title=\"" + rsA.getString("stTitle") + "\" class='selectedTab' >" + rsA.getString("stTitle") + "</a>";
        }
        else {
          stReturn = stReturn + "<a href='" + stUrl + "' title=\"" + rsA.getString("stTitle") + "\">" + rsA.getString("stTitle") + "</a>";
        }
        int tmp472_467 = rsA.getInt("nmAppParent");
        String[] tmp472_461 = this.astM2; tmp472_461[tmp472_467] = (tmp472_461[tmp472_467] + stReturn);
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<br>ERROR: addMenuBar: " + e);
    }
    return stReturn;
  }

  public String addLink(ResultSet rsA)
  {
    String stReturn = "";
    String stExtra = "";
    try
    {
      String stUrl = rsA.getString("stUrl");
      if ((stUrl == null) || (stUrl.equals("")))
        stUrl = "./?a=" + rsA.getString("nmAppId");
      this.iLinkMax += 1;
      if (this.iLinkMax > 1)
        stReturn = stReturn + " &nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp; ";
      if (rsA.getInt("nmAppType2") == 1)
        stExtra = stExtra + " target=_blank ";
      stReturn = stReturn + "<a " + stExtra + " href=\"" + stUrl + "\" title=\"" + rsA.getString("stTitle") + "\">" + rsA.getString("stTitle") + "</a>";
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>ERROR: addLink: " + e);
    }
    this.stL += stReturn;
    return stReturn;
  }

  public String getError()
  {
    return this.stError;
  }
}