package com.ederbase.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.StringCharacterIterator;

public class EbDatabase
{
  private String stHost;
  private String stUser;
  private String stPassword;
  private String stDbName;
  private String stConnectString;
  private EbConnect ebConnect;
  private String stError = "";
  private int iDbType;
  private long lExecuteTime = 0L;
  private int iSqlCount = 0;
  private int iDebugLevel = 0;
  private String stDebugTrace = "";
  private DecimalFormat decFmt = new DecimalFormat("###.########");
  private Statement[] aStmt = null;
  private ResultSet[] aRs = null;
  private int iMaxStatement = 0;

  public EbDatabase(String stDbName, EbEnterprise ebEnt)
  {
    try
    {
      this.iDbType = ebEnt.getDbType();
      this.stHost = ebEnt.getHost();
      this.stUser = ebEnt.getUser();
      this.stPassword = ebEnt.getPassword();
      this.iDebugLevel = ebEnt.iDebugLevel;
      this.stConnectString = "";
      this.stDbName = stDbName;
      //this.ebConnect = new EbConnect(this.iDbType, this.stHost, this.stUser, this.stPassword, this.stDbName, this.stConnectString);
      this.ebConnect = new EbConnect(0, "localhost", "eps", "eps", this.stDbName, this.stConnectString);
      if (this.ebConnect != null)
        this.stError += this.ebConnect.getError();
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>EbDatabase: " + this.stHost + " " + this.stUser + " type: " + this.iDbType + " Exception: " + e);
    }
  }

  public String getDbName()
  {
    return this.stDbName;
  }

  public void setDebugLevel(int iLevel)
  {
    this.iDebugLevel = iLevel;
  }

  public EbConnect getConnect()
  {
    return this.ebConnect;
  }

  public EbDatabase(int iDbType, String stHost, String stUser, String stPassword, String stDbName, String stConnectString)
  {
    try
    {
      this.iDbType = iDbType;
      this.stHost = stHost;
      this.stUser = stUser;
      this.stPassword = stPassword;
      this.stDbName = stDbName;
      this.stConnectString = stConnectString;
      this.ebConnect = new EbConnect(this.iDbType, this.stHost, this.stUser, this.stPassword, this.stDbName, this.stConnectString);
      if (this.ebConnect != null)
        this.stError += this.ebConnect.getError();
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>EbDatabase: " + stHost + " " + stUser + " type: " + iDbType + " Exception: " + e);
    }
  }

  public void resetError()
  {
    this.stError = "";
  }

  public void ebClose()
  {
    if (this.ebConnect != null)
      this.ebConnect.ebClose();
  }

  public int getiDbType()
  {
    return this.iDbType;
  }

  public int ExecuteUpdate(String stSql)
  {
    long startTime = System.nanoTime();

    int iReturn = -1;
    try
    {
      Connection dbConn = this.ebConnect.getEbConn();
      Statement stmt = dbConn.createStatement(1004, 1007);
      iReturn = stmt.executeUpdate(stSql);
      stmt.close();
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>ExecuteSql: " + stSql + " Exception: " + e);
    }
    fmtTrace(startTime, stSql);
    return iReturn;
  }

  public void fmtTrace(long startTime, String stSql)
  {
    long endTime = System.nanoTime();
    double val = (endTime - startTime) / 1000000000.0D;
    this.lExecuteTime += endTime - startTime;
    this.iSqlCount += 1;
    if (this.iDebugLevel > 0)
    {
      try
      {
        if (val >= 0.1D)
          this.stDebugTrace = (this.stDebugTrace + "<tr><td align=right>" + this.iSqlCount + "</td><td align=left bgcolor=pink>" + this.decFmt.format(val) + "</td><td style='font-size:9px;'>" + stSql + "</td></tr>");
        else
          this.stDebugTrace = (this.stDebugTrace + "<tr><td align=right>" + this.iSqlCount + "</td><td align=left>" + this.decFmt.format(val) + "</td><td style='font-size:9px;'>" + stSql + "</td></tr>");
      }
      catch (Exception e)
      {
      }
    }
  }

  public EbResultSet ebSql(String stSql)
  {
    long startTime = System.nanoTime();
    EbResultSet ebRs = new EbResultSet();
    try
    {
      Connection dbConn = this.ebConnect.getEbConn();
      ebRs.stmt = dbConn.createStatement(1004, 1007);
      ebRs.rs = ebRs.stmt.executeQuery(stSql);
      ebRs.rs.last();
      ebRs.iRows = ebRs.rs.getRow();
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>ExecuteSql: " + stSql + " Exception: " + e);
    }
    fmtTrace(startTime, stSql);
    return ebRs;
  }

  public ResultSet ExecuteSql(String stSql)
  {
    long startTime = System.nanoTime();
    try
    {
      if (this.aStmt == null)
      {
        this.aStmt = new Statement[20];
        this.aRs = new ResultSet[20];
      }
      Connection dbConn = this.ebConnect.getEbConn();
      this.iMaxStatement = ((this.iMaxStatement + 1) % 20);
      this.aStmt[this.iMaxStatement] = dbConn.createStatement(1004, 1007);
      this.aRs[this.iMaxStatement] = this.aStmt[this.iMaxStatement].executeQuery(stSql);
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>ExecuteSql: " + stSql + " iMaxStatement = " + this.iMaxStatement + " Exception: " + e);
    }
    fmtTrace(startTime, stSql);
    return this.aRs[this.iMaxStatement];
  }

  public String ExecuteSql1(String stSql)
  {
    String stReturn = "";
    try
    {
      EbResultSet rs = ebSql(stSql);
      if (rs != null)
      {
        if (rs.iRows > 0)
        {
          rs.rs.absolute(1);
          stReturn = rs.rs.getString(1);
        }
        this.stError += rs.ebClose();
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>ExecuteSql1 ex: [" + stSql + "] " + e);
    }
    return stReturn;
  }

  public long getExecuteTime()
  {
    return this.lExecuteTime;
  }

  public int getSqlCount()
  {
    return this.iSqlCount;
  }

  public String getDebugTrace()
  {
    return this.stDebugTrace;
  }

  public int ExecuteSql1n(String stSql)
  {
    int iReturn = 0;
    try
    {
      EbResultSet rs = ebSql(stSql);
      if (rs != null)
      {
        if (rs.iRows > 0)
        {
          rs.rs.absolute(1);
          iReturn = rs.rs.getInt(1);
        }
        this.stError += rs.ebClose();
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>ExecuteSql1n ex: [" + stSql + "] " + e);
    }
    return iReturn;
  }

  public String fmtInput(int iType, ResultSet rsF, String stValue, String stPkValue)
  {
    String stReturn = "";
    if (iType == 1)
      stReturn = stReturn + "<td align=left valign=top>";
    try
    {
      if (stValue == null) {
        stValue = "";
      }

      int iMax = 0;
      int iSize = 0;

      switch (rsF.getInt("nmFieldType"))
      {
      case 1:
      default:
        iMax = rsF.getInt("nmFieldSize");
        iSize = 1;
        if (iMax <= 0)
          iMax = 1;
        if (iMax > 100)
          iSize = 100;
        else
          iSize = iMax;
        iSize++;
        stReturn = stReturn + "<input type=text name='g" + rsF.getInt("nmFieldId") + "|" + stPkValue + "' value=\"" + stValue + "\" MAXLENGTH=" + iMax + " SIZE=" + iSize + " >";
        break;
      case 31:
        iMax = iSize = 22;
        iSize++;
        stReturn = stReturn + "<input type=text name='g" + rsF.getInt("nmFieldId") + "|" + stPkValue + "' value=\"" + stValue + "\" MAXLENGTH=" + iMax + " SIZE=" + iSize + " >";
        break;
      case 8:
        iMax = iSize = 22;
        iSize++;
        stReturn = stReturn + "<input type=text name='g" + rsF.getInt("nmFieldId") + "|" + stPkValue + "' value=\"" + stValue + "\" MAXLENGTH=" + iMax + " SIZE=" + iSize + " >";
        break;
      case 20:
        iMax = iSize = 10;
        iSize++;
        stReturn = stReturn + "<input type=text name='g" + rsF.getInt("nmFieldId") + "|" + stPkValue + "' value=\"" + stValue + "\" MAXLENGTH=" + iMax + " SIZE=" + iSize + " >";
        break;
      case 21:
        iMax = iSize = 12;
        iSize++;
        stReturn = stReturn + "<input type=text name='g" + rsF.getInt("nmFieldId") + "|" + stPkValue + "' value=\"" + stValue + "\" MAXLENGTH=" + iMax + " SIZE=" + iSize + " >";
        break;
      case 4:
        stReturn = stReturn + "<textarea rows=20 cols=150 name='g" + rsF.getInt("nmFieldId") + "|" + stPkValue + "'>" + stValue + "</textarea>";
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<br>ERROR fmtInput: " + e);
    }
    if (iType == 1)
      stReturn = stReturn + "</td>";
    return stReturn;
  }

  public String getError()
  {
    if (this.ebConnect != null)
      this.stError += this.ebConnect.getError();
    return this.stError;
  }

  public String setSqlLimit(String stSql, String stLimit)
  {
    String stReturn = stSql;
    if ((stLimit != null) && (stLimit.length() > 0))
    {
      try
      {
        int iLimit = Integer.parseInt(stLimit);
        if (iLimit > 0)
        {
          if (getiDbType() == 1)
          {
            if (stSql.trim().toLowerCase().startsWith("select "))
              stReturn = "select top " + iLimit + " " + stSql.trim().substring(7);
          }
          else {
            stReturn = stSql + " limit " + iLimit;
          }
        }
      }
      catch (Exception e)
      {
      }
    }
    return stReturn;
  }

  public String fmtDbString(String stValue)
  {
    String stReturn = "";

    if (getiDbType() == 1)
    {
      stReturn = "'" + stValue.replace("'", "''") + "'";
    }
    else {
      stReturn = '"' + addSlashes(stValue) + '"';
    }
    return stReturn;
  }

  public String fmtDateTime(int iUserType, int iFmtType, String stData)
  {
    String stReturn = "";

    stReturn = stData.substring(5, 7) + "/" + stData.substring(8, 10) + "/" + stData.substring(0, 4);
    if (iFmtType == 2)
    {
      int iH = Integer.parseInt(stData.substring(11, 13));
      String stAPM = "";
      if (iH > 12)
      {
        iH -= 12;
        stAPM = "PM";
      }
      else {
        stAPM = "AM";
      }

      String stHr = "";
      if (iH < 10)
        stHr = "0" + iH;
      else
        stHr = "" + iH;
      stReturn = stReturn + " " + stHr + ":" + stData.substring(14, 16) + " " + stAPM;
    }

    return stReturn;
  }

  public static String addSlashes(String text)
  {
    StringBuffer sb = new StringBuffer(text.length() * 2);
    StringCharacterIterator iterator = new StringCharacterIterator(text);

    char character = iterator.current();

    while (character != 65535)
    {
      if (character == '"')
      {
        sb.append("\\\"");
      } else if (character == '\'')
      {
        sb.append("\\'");
      } else if (character == '\\')
      {
        sb.append("\\\\");
      } else if (character == '\r')
      {
        sb.append("\\r");
      } else if (character == '\n')
      {
        sb.append("\\n");
      } else if (character == '{')
      {
        sb.append("\\{");
      } else if (character == '}')
      {
        sb.append("\\}");
      }
      else {
        sb.append(character);
      }

      character = iterator.next();
    }

    return sb.toString();
  }

  public String getNowString()
  {
    String stReturn = "??";
    if (getiDbType() == 1)
      stReturn = "GETDATE()";
    else
      stReturn = "now()";
    return stReturn;
  }

  public String makeDecimal(String stIn)
  {
    String stOut = "";
    for (int i = 0; (stIn != null) && (i < stIn.length()); i++)
    {
      char ch = stIn.charAt(i);
      if (((ch >= '0') && (ch <= '9')) || (ch == '.'))
        stOut = stOut + ch;
    }
    if (stOut.length() <= 0)
      stOut = "0";
    return stOut;
  }
}


class EbResultSet
{
  public ResultSet rs = null;
  public Statement stmt = null;
  public int iRows = 0;

  public String ebClose()
  {
    String stError = "";
    try
    {
      this.rs.close();
      this.stmt.close();
    }
    catch (Exception e) {
      stError = stError + "<br> EbResultSet.ebClose " + e;
    }
    return stError;
  }
}