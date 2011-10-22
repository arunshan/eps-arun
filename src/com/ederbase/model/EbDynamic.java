package com.ederbase.model;

import java.sql.ResultSet;

public class EbDynamic
{
  private int nmDynId;
  private int nmSkinId;
  private int iDynDbId = 0;
  private String stDynDbName;
  private ResultSet rsDynFields;
  private int iDynFieldsMax = 0;
  private ResultSet[] arsChoices = null;
  private String stError = "";
  private EbEnterprise ebEnt;
  private int iRecId;
  private int iSeqNr;
  private String[] asChoicesList = null;
  private String stPopupMessage = "";

  public void setPopupMessage(String stType, String stMsg)
  {
    if ((stMsg != null) && (stMsg.length() > 0))
    {
      this.stPopupMessage = (this.stPopupMessage + stType + "|" + stMsg + "|");
    }
  }

  public String getValitation(int giNrValidation2, String stValidation2, String stMultiSel)
  {
    String stReturn = "";
    stReturn = stReturn + "\n<SCRIPT LANGUAGE='JavaScript'>\n var gaValidation = new Array(\n";
    stReturn = stReturn + stValidation2;
    stReturn = stReturn + "\n);\n var giNrValidation2=" + giNrValidation2 + ";\n" + " var giSubmitId=0;\n var stPopupMessage=\"" + this.stPopupMessage + "\";\n " + " var gstMultiSel=\"" + stMultiSel + "\"; \n" + " var giUser=" + this.ebEnt.ebUd.getLoginId() + "; \n</SCRIPT>\n";

    return stReturn;
  }

  public EbDynamic(EbEnterprise ebEnt)
  {
    this.stDynDbName = "";
    this.ebEnt = ebEnt;
    this.iRecId = 0;
    this.iSeqNr = 0;
  }

  private String getDynData(int iRecId, int iSeqNr)
  {
    String stReturn = "";
    String stSql = "";
    ResultSet rsFields = null;
    ResultSet rsTabs = null;
    ResultSet rsData = null;
    int iF = 0;
    int iT = 0;
    int iFieldCount = 0;
    int iColumn = 0;
    try
    {
      stSql = "select distinct stTabName from teb_fields order by stTabName";
      rsTabs = this.ebEnt.dbDyn.ExecuteSql(stSql);

      rsTabs.last();
      int iTables = rsTabs.getRow();
      stReturn = stReturn + "\n<SCRIPT language='javascript'>\nvar gaData = new Array( giFieldMax );\n";
      for (iT = 1; iT <= iTables; iT++)
      {
        rsTabs.absolute(iT);

        stSql = "select * from " + rsTabs.getString(1) + " where RecId=" + iRecId + " and nmSeqNr=" + iSeqNr;
        rsData = this.ebEnt.dbDyn.ExecuteSql(stSql);
        rsData.absolute(1);

        stSql = "select * from teb_fields where stTabName = '" + rsTabs.getString(1) + "' ";
        rsFields = this.ebEnt.dbDyn.ExecuteSql(stSql);
        rsFields.last();
        iFieldCount = rsFields.getRow();

        for (iF = 1; iF <= iFieldCount; iF++)
        {
          rsFields.absolute(iF);
          String stData = "";
          if ((rsFields.getInt("nmDataType") == 9) && ((rsFields.getInt("nmFlags") & 0x80) != 0))
          {
            continue;
          }

          iColumn = rsData.findColumn(rsFields.getString("stDbFieldName"));
          stData = rsData.getString(iColumn);
          if (stData == null)
          {
            stData = "";
          }

          switch (rsFields.getInt("nmDataType"))
          {
          case 8:
            stData = this.ebEnt.dbDyn.fmtDateTime(1, 2, stData);
            break;
          case 20:
            stData = this.ebEnt.dbDyn.fmtDateTime(1, 1, stData);
            break;
          }

          stReturn = stReturn + "\n gaData[" + rsFields.getInt("nmOrder2") + "] = \"" + EbDatabase.addSlashes(stData) + "\";";
        }

      }

      stSql = "select mc.nmFieldId,mc.nmChoiceId,f.nmOrder2 from teb_multichoices mc, teb_fields f where f.nmForeignId=mc.nmFieldId and nmRecId=" + iRecId + " and nmSeqNr=" + iSeqNr + " order by nmFieldId";
      ResultSet rsMC = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rsMC.last();
      int iMax = rsMC.getRow();
      int iLastFieldId = 0;
      String stData = "";
      for (int iMC = 1; iMC <= iMax; iMC++)
      {
        rsMC.absolute(iMC);
        if (iLastFieldId != rsMC.getInt("nmFieldId"))
        {
          if (iLastFieldId > 0)
          {
            stReturn = stReturn + "\n gaData[" + rsMC.getInt("nmOrder2") + "] = \"" + EbDatabase.addSlashes(stData) + "\";";
          }

          stData = "";
        }

        if (iMC > 1)
        {
          stData = stData + "|";
        }

        stData = stData + rsMC.getInt("nmChoiceId");
        iLastFieldId = rsMC.getInt("nmFieldId");
      }

    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<br>ERROR ebDyn: : getDynData RecId=" + iRecId + " and nmSeqNr=" + iSeqNr + ": " + e);
    }

    stReturn = stReturn + "\n</script>";
    return stReturn;
  }

  public String getTOC(int iDbId)
  {
    ResultSet rs2 = null;

    String stReturn = "";
    int iAssert = 0;

    String stSql = "";
    try
    {
      if ((iAssert = assertDb(iDbId)) > 0)
      {
        stSql = "SELECT * FROM teb_fields where nmHeaderOrder > 0 order by nmHeaderOrder";
        ResultSet rsFields2 = this.ebEnt.dbDyn.ExecuteSql(stSql);
        String[] stSearch = makeTebSql(rsFields2);

        stSql = "SELECT * FROM t_dynamic where nmOrder <= 7 and nmDynId=" + this.nmDynId + " and nmSkinId=" + this.nmSkinId + " order by nmOrder";
        ResultSet rs = this.ebEnt.dbEb.ExecuteSql(stSql);
        rs.last();
        int iRowMax = rs.getRow();
        for (int iF = 1; iF <= iRowMax; iF++)
        {
          rs.absolute(iF);
          if ((iF != 3) && (iF != 5) && (iF != 6))
          {
            stReturn = stReturn + "\n" + rs.getString("stContent"); } else {
            if (iF != 6)
              continue;
            if (this.ebEnt.dbDyn.getiDbType() == 1)
            {
              stSql = "select top 25 " + stSearch[0];
            }
            else {
              stSql = "select " + stSearch[0] + " limit 25 ";
            }

            rs2 = this.ebEnt.dbDyn.ExecuteSql(stSql);
            String[] v = stSearch[1].split("\\|");

            rs2.last();
            int iRowMax2 = rs2.getRow();

            stReturn = stReturn + "<SCRIPT language='javascript'>\nvar giFieldMax=0;\nvar gaF = new Array(1);\n \n</script><table bgcolor=blue cellspacing=1 cellpadding=3>";

            stReturn = stReturn + "<tr>";

            for (int i = 1; i <= v.length; i++)
            {
              stReturn = stReturn + "<td bgcolor=skyblue align=left><b>" + v[(i - 1)] + "</b></td>";
            }

            stReturn = stReturn + "</tr>";
            String stBg = "";
            for (int iR = 1; iR <= iRowMax2; iR++)
            {
              if (iR % 2 == 0)
              {
                stBg = " bgcolor=white ";
              }
              else {
                stBg = " bgcolor=#CCCCCC ";
              }

              rs2.absolute(iR);
              stReturn = stReturn + "<tr>";

              for (int i = 1; i <= v.length; i++)
              {
                stReturn = stReturn + "<td " + stBg + "><a href='./index.jsp?a=20&iRecId=" + rs2.getInt("RecId") + "&iSeqNr=" + rs2.getInt("nmSeqNr") + "&stHTML=uu'>" + rs2.getString(i) + "</td>";
              }

              stReturn = stReturn + "</tr>";
            }

            stReturn = stReturn + "</table><br>&nbsp;<br>";
          }
        }
      }
      else {
        this.stError = (this.stError + "<BR>ERROR getTOC: db " + iDbId + " not found: " + iAssert);
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR getTOC: " + e);
    }
    return stReturn;
  }

  public static String helpFmt(String text)
  {
    text = text.replace('"', '`');
    text = text.replace("\\n\\r", "\r\n");
    text = text.replace("\\r\\n", "\r\n");
    text = text.replace("<br>", "");
    text = text.replace("\n\r", "\r\n");
    String stReturn = text;
    return stReturn;
  }

  public String[] makeTebSql(ResultSet rsFields)
  {
    String stSelect = "";
    String stMain = "";
    String stFrom = "";
    String stJoin = "";
    String stHeader = "";
    int iChoice = 1;

    String[] stReturn = new String[3];
    try
    {
      rsFields.last();
      int iRowMax = rsFields.getRow();
      for (int iF = 1; iF <= iRowMax; iF++)
      {
        rsFields.absolute(iF);
        if (iF > 1)
        {
          stSelect = stSelect + ", ";
          stHeader = stHeader + "|";
        }

        stHeader = stHeader + rsFields.getString("stDbFieldName").replace("_", " ");

        if (rsFields.getInt("nmDataType") == 9)
        {
          stSelect = stSelect + " c" + iChoice + ".stChoiceValueShort as " + rsFields.getString("stDbFieldName");
          if (!stFrom.equals(""))
          {
            stFrom = stFrom + ",";
            stJoin = stJoin + " and ";
          }

          stFrom = stFrom + " teb_choices c" + iChoice;
          stJoin = stJoin + rsFields.getString("stDbFieldName") + " = c" + iChoice + ".RecId ";
          iChoice++;
        }
        else
        {
          stSelect = stSelect + rsFields.getString("stDbFieldName");
        }

        if (stMain.equals(""))
        {
          stMain = rsFields.getString("stTabName"); } else {
          if (stMain.equals(rsFields.getString("stTabName")))
            continue;
          if (stFrom.indexOf(rsFields.getString("stTabName")) >= 0)
            continue;
          if (!stFrom.equals(""))
          {
            stFrom = stFrom + ",";
            stJoin = stJoin + " and ";
          }

          stFrom = stFrom + rsFields.getString("stTabName");
          stJoin = stJoin + stMain + ".RecId=" + rsFields.getString("stTabName") + ".RecId and ";
          stJoin = stJoin + stMain + ".nmSeqNr=" + rsFields.getString("stTabName") + ".nmSeqNr ";
        }
      }

    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<br>ERROR ebDyn: : " + e);
    }

    stReturn[0] = (stSelect + "," + stMain + ".RecId," + stMain + ".nmSeqNr  from " + stMain + "," + stFrom + " where " + stJoin);
    stReturn[1] = stHeader;
    return stReturn;
  }

  public int getRecId()
  {
    return this.iRecId;
  }

  public int getSeqNr()
  {
    return this.iSeqNr;
  }

  public int assertDb(String stDbName)
  {
    int iDbId = -1;
    try
    {
      String[] astF = stDbName.split("\\|");
      String stTemp = this.ebEnt.dbEb.ExecuteSql1("select nmDbId from t_databases where nmServerId = " + astF[0] + " and stDbName = \"" + astF[1] + "\" ");

      iDbId = Integer.parseInt(stTemp);
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR: resetEbDyn " + stDbName + ": " + e);
    }
    return assertDb(iDbId);
  }

  public int assertDb(int iDbId)
  {
    int iReturn = -1;
    String stSql = "";
    try
    {
      if ((this.ebEnt.dbDyn == null) || (this.iDynDbId != iDbId))
      {
        if (this.ebEnt.dbDyn != null)
        {
          this.ebEnt.dbDyn.ebClose();
        }

        stSql = "SELECT * FROM t_databases d, t_server s where d.nmServerId=s.nmServerId and d.nmDbId=" + iDbId;
        ResultSet rs = this.ebEnt.dbEb.ExecuteSql(stSql);
        if (rs != null)
        {
          iReturn = 0;
          rs.last();
          if (rs.getRow() >= 1)
          {
            rs.absolute(1);
            this.stDynDbName = rs.getString("stDbName");
            this.iDynDbId = iDbId;
            this.ebEnt.dbDyn = new EbDatabase(rs.getInt("nmDbType"), rs.getString("stServerIp"), rs.getString("stUser"), rs.getString("stPassword"), this.stDynDbName, rs.getString("stConnectString"));
            iReturn = 1;
          }
        }
      }
      else {
        iReturn = 2;
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR assertDb: " + e);
      iReturn = -2;
    }
    return iReturn;
  }
}