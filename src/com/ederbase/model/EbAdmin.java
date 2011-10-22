package com.ederbase.model;

import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;

public class EbAdmin
{
  private EbEnterprise ebEnt = null;
  private String stError;

  public EbAdmin(EbEnterprise ebEnt)
  {
    this.ebEnt = ebEnt;
    this.stError = "";
  }

  public String getDbManagement(ResultSet rsA, HttpServletRequest request)
  {
    String stReturn = "";
    try
    {
      String stB = request.getParameter("b");
      if ((stB == null) || (stB.equals("")))
      {
        stB = "1";
      }
      int iState = Integer.parseInt(stB);
      EbTwitter ebTwitter = new EbTwitter(this.ebEnt);
      switch (iState)
      {
      case 1:
        stReturn = rsA.getString("stContent");
        String stA = request.getParameter("a");
        stReturn = stReturn + "<table border=0><tr><td><a href='./?a=" + stA + "&b=2019'>Read Craigslist</a></td>" + "<tr><td><a href='./?a=" + stA + "&b=1002'>EPS Demo</a></td>" + "<tr><td><a href='./?a=" + stA + "&b=19'>Send Twitter</a></td>" + "<td>Send a Tweet</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=20'>Search Follow </a></td>" + "<td>Follow from search result</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=18'>Twitter Result</a></td>" + "<td>Count followers etc.</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=8'>Read Email</a></td>" + "<td>Read Email into X25Communications (removes from mailbox)</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=5'>Fix History</a></td>" + "<td>Fixes: a) mulitple emails from gmail (same foreign refd) b) set person id in COMM c) add NEW COMM to HISTORY  </td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=15'>Search/Follow Twitter</a></td>" + "<td>Twiter: Line 1 # of returns, Line2: # of searches, Line 3: search, Line4: user</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=16'>Create Full Hourly Stats</a></td>" + "<td>Rebuild MyHourlyTable</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=17'>Last 2 Hourly Stats</a></td>" + "<td>Only last 2 hours</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=10'>Fix MailQ</a></td>" + "<tr><td><a href='./?a=" + stA + "&b=13'>Fix Insert</a></td>" + "<td>Fixes: mailq >x shows all email addreses that cant be sent.</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=11'>Fix Message Undeliverable</a></td>" + "<td>Fixes: Undeliverable messages need to be flagged</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=12'>Process 'confirm me' </a></td>" + "<td>Fixes: Exercises and flags those emails that need to be confirmed</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=9'>Fix Address/Zip</a></td>  " + "<td>Set stZipCity in Address, for display. X25Zip resides in common</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=6'>Fix Cross Links / Zombies</a></td>" + "<td>Phone/Email if set in both: Company and Person add link to X25RefCompany, delete zombies</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=2'>Resync EB</a></td>" + "<td>Use this tool each time any DB changes are made: Add/Modify fields and types. It is safe to run this tool multiple times, however it runs a few minutes. Be Patient.</td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=3'>Fix Email</a></td>" + "<td>Fixes emails after a Marketing YP/... has added new leads. Some emails are broken.  </td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=7'>Import Enterprise Companies (step #2)</a></td>" + "<td>Check all Phone/Email/Web and pull over companies if not here yet. </td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=4'>Import EB Enterprise (step #1)</a></td>" + "<td>You must make a backup first. This allows you to import full or partial data from another Enterprise DB.   </td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=4'>Import EB Enterprise (step #1)</a></td>" + "<td>You must make a backup first. This allows you to import full or partial data from another Enterprise DB.   </td></tr>" + "<tr><td><a href='./?a=" + stA + "&b=1008'>Process EPS 8</a></td>" + "</table><br>&nbsp;<br>&nbsp;";

        break;
      case 2:
        stReturn = stReturn + getResetEb();
        break;
      case 3:
        stReturn = stReturn + getFixEmail();
        break;
      case 4:
        stReturn = stReturn + getImportEnterprise(rsA, request);
        break;
      case 5:
        stReturn = stReturn + fixCommHistory();
        break;
      case 6:
        stReturn = stReturn + fixCrossLinksZombies(rsA, request);
        break;
      case 7:
        stReturn = stReturn + getImportCompanies(rsA, request);
        break;
      case 8:
        EbMail ebMmail = new EbMail(this.ebEnt);
        stReturn = stReturn + ebMmail.manageEmail();
        stReturn = stReturn + ebMmail.getError();
        break;
      case 9:
        stReturn = stReturn + this.ebEnt.ebNorm.fixAddressZip();
        break;
      case 10:
        stReturn = stReturn + fixMailQ();
        break;
      case 13:
        stReturn = stReturn + fixInsert();
        break;
      case 11:
        stReturn = stReturn + fixUndeliverable();
        break;
      case 12:
        stReturn = stReturn + processConfirm();
        break;
      case 15:
        stReturn = stReturn + processTwitter(request);
        break;
      case 16:
        stReturn = stReturn + processHourly(-1);
        break;
      case 17:
        stReturn = stReturn + processHourly(4);
        break;
      case 18:
        stReturn = stReturn + ebTwitter.processResult(1);
        this.stError += ebTwitter.getError();
        break;
      case 19:
        stReturn = stReturn + ebTwitter.processSendTwitter();
        this.stError += ebTwitter.getError();
        break;
      case 20:
        stReturn = stReturn + processSearchFollowers();
        break;
      case 21:
        stReturn = stReturn + processFiles();
        break;
      case 14:
      default:
        stReturn = stReturn + "<BR>ERROR getDbManagement: invalid state/process: " + iState;
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<br>ERROR getDbManagement" + e);
    }
    return stReturn;
  }

  public String getAdmin(ResultSet rsA, HttpServletRequest request)
  {
    String stReturn = "";
    try
    {
      this.ebEnt.dbEb.resetError();

      switch (rsA.getInt("nmProcessId"))
      {
      case 6:
        stReturn = stReturn + getFieldNames(request);
        break;
      case 1901:
        stReturn = stReturn + getTableEdit(request);
        break;
      case 1902:
        stReturn = stReturn + getDbManagement(rsA, request);
        break;
      default:
        stReturn = stReturn + "<br>ERROR: invalid admin " + rsA.getInt("nmProcessId");
      }

    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<br>ERROR getAdmin: " + e);
    }
    return stReturn;
  }

  public String getFieldNames(HttpServletRequest request)
  {
    String stReturn = "";
    stReturn = stReturn + "<br><h1>Set FIELD NAMES</h1>";
    EbDatabase eb = null;
    try
    {
      String stSql = "select * from t_server s, t_databases d, t_tables t where s.nmServerId=d.nmServerId and d.nmDbId=t.nmDbId and t.stTableName='teb_fields'";
      ResultSet rsF = this.ebEnt.dbEb.ExecuteSql(stSql);
      if (rsF != null)
      {
        rsF.last();
        int iMaxF = rsF.getRow();
        for (int iF = 1; iF <= iMaxF; iF++)
        {
          rsF.absolute(iF);
          stSql = "select * from teb_fields";
          eb = new EbDatabase(rsF.getInt("nmDbType"), rsF.getString("stServerIp"), rsF.getString("stUser"), rsF.getString("stPassword"), rsF.getString("stDbName"), "");
          ResultSet rsFields = eb.ExecuteSql(stSql);
          if (rsFields == null)
            continue;
          rsFields.last();
          int iFieldMax = rsFields.getRow();
          for (int iFld = 1; iFld <= iFieldMax; iFld++)
          {
            rsFields.absolute(iFld);
            int nmFieldId = this.ebEnt.dbEb.ExecuteSql1n("select f.nmFieldId from t_tables t, t_fields f where t.nmTableId = f.nmTableId and t.nmDbId = " + rsF.getInt("nmDbId") + " and f.stDbFieldName = '" + rsFields.getString("stDbFieldName") + "'");
            ResultSet rsF2 = this.ebEnt.dbEb.ExecuteSql("select * from t_fields2 where nmFieldId=" + nmFieldId);
            int iCount = 0;
            if (rsF2 != null)
            {
              rsF2.last();
              iCount = rsF2.getRow();
            }
            if (iCount > 0)
              continue;
            stSql = "insert into t_fields2 (nmFieldId,nmDataType,nmFlags,nmOrder2,nmGrouping,nmGroupingOrder,stDefaultValue,nmTabId,stTabName,stLabel,nmOrder,nmHeaderOrder,nmMinBytes,nmMaxBytes,nmRows,nmCols,nmSecurityFlags,nmReadFlags,nmWriteFlags,nmRecordFlags,nmFieldFlags,nmLookup,nmShowLookup,stReadUG,nmReportFlags,stAllowUG,stMask,stValidation,stValidParam,nmComparison,nmFieldCalc) values(" + nmFieldId + "," + rsFields.getString("nmDataType") + "," + rsFields.getString("nmFlags") + "," + rsFields.getString("nmOrder2") + "," + rsFields.getString("nmGrouping") + "," + rsFields.getString("nmGroupingOrder") + "," + this.ebEnt.dbEb.fmtDbString(rsFields.getString("stDefaultValue")) + "," + rsFields.getString("nmTabId") + "," + this.ebEnt.dbEb.fmtDbString(rsFields.getString("stTabName")) + "," + this.ebEnt.dbEb.fmtDbString(rsFields.getString("stLabel")) + "," + rsFields.getString("nmOrder") + "," + rsFields.getString("nmHeaderOrder") + "," + rsFields.getString("nmMinBytes") + "," + rsFields.getString("nmMaxBytes") + "," + rsFields.getString("nmRows") + "," + rsFields.getString("nmCols") + "," + rsFields.getString("nmSecurityFlags") + "," + rsFields.getString("nmReadFlags") + "," + rsFields.getString("nmWriteFlags") + "," + rsFields.getString("nmRecordFlags") + "," + rsFields.getString("nmFieldFlags") + "," + rsFields.getString("nmLookup") + "," + rsFields.getString("nmShowLookup") + "," + this.ebEnt.dbEb.fmtDbString(rsFields.getString("stReadUG")) + "," + rsFields.getString("nmReportFlags") + "," + this.ebEnt.dbEb.fmtDbString(rsFields.getString("stAllowUG")) + "," + this.ebEnt.dbEb.fmtDbString(rsFields.getString("stMask")) + "," + this.ebEnt.dbEb.fmtDbString(rsFields.getString("stValidation")) + "," + this.ebEnt.dbEb.fmtDbString(rsFields.getString("stValidParam")) + "," + rsFields.getString("nmComparison") + "," + rsFields.getString("nmFieldCalc") + ")";

            this.ebEnt.dbEb.ExecuteUpdate(stSql);
          }
        }

      }

      if (eb != null)
      {
        this.stError += eb.getError();
        eb.ebClose();
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<br>getFieldNames ERROR: " + e);
      if (eb != null)
      {
        this.stError += eb.getError();
        eb.ebClose();
      }
    }
    return stReturn;
  }

  public String getFixEmail()
  {
    String stReturn = "<br>getFixEmail";
    String stSql = "";
    ResultSet rs = null;
    int iMaxRow = 0;
    int iPos = 0;
    int iFound = 0;
    try
    {
      stSql = "select * from X25User order by RecId ";
      rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        iMaxRow = rs.getRow();
        for (int iU = 1; iU <= iMaxRow; iU++)
        {
          rs.absolute(iU);
          String stEmail = rs.getString("stEMail").trim();
          iFound = 0;
          if ((iPos = stEmail.indexOf("?")) > 1)
          {
            stEmail = stEmail.substring(0, iPos);
            iFound++;
          }
          if ((iPos = stEmail.indexOf("mailto:")) > 1)
          {
            stEmail = stEmail.substring(iPos + 7);
            iFound++;
          }
          if ((iPos = stEmail.indexOf("%20")) >= 0)
          {
            stEmail = stEmail.replace("%20", " ").trim();
            iFound++;
          }

          if (iFound <= 0)
            continue;
          this.ebEnt.dbEnterprise.ExecuteUpdate("update X25User set stEMail=" + this.ebEnt.dbEnterprise.fmtDbString(stEmail) + " where RecId=" + rs.getString("RecId"));
        }
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "ERROR getFixEmai: " + e);
    }
    return stReturn;
  }

  public String getResetEb()
  {
    String stReturn = "<br>getResetEb";
    String stSql = "";
    ResultSet rs = null;
    int iMaxRow = 0;
    int iLastTable = 0;
    String stPk = "";
    String stPkFids = "";
    String stDbName = "";
    int iSkip = 0;
    int iMaxServer = 0;
    int iMaxDatabases = 0;
    int iMaxTables = 0;
    EbDatabase eb = null;
    try
    {
      ResultSet rsS = null;
      ResultSet rsD = null;
      ResultSet rsT = null;
      ResultSet rsF = null;

      this.ebEnt.dbEb.ExecuteUpdate("update t_databases set nmDbFlag = ( nmDbFlag | 0x40000000)");
      this.ebEnt.dbEb.ExecuteUpdate("update t_tables set nmTableFlag = ( nmTableFlag |  0x40000000)");
      this.ebEnt.dbEb.ExecuteUpdate("update t_fields set nmFieldFlag = ( nmFieldFlag & ~0x00F00000)");
      this.ebEnt.dbEb.ExecuteUpdate("update t_fields set nmFieldFlag = ( nmFieldFlag |  0x40000000)");

      rsS = this.ebEnt.dbEb.ExecuteSql("select * from t_server");
      rsS.last();
      iMaxServer = rsS.getRow();
      for (int iRS = 1; iRS <= iMaxServer; iRS++)
      {
        rsS.absolute(iRS);
        eb = new EbDatabase(rsS.getInt("nmDbType"), rsS.getString("stServerIp"), rsS.getString("stUser"), rsS.getString("stPassword"), "", "");
        switch (rsS.getInt("nmDbType"))
        {
        case 0:
          rsD = eb.ExecuteSql("show databases");
          break;
        case 1:
          rsD = eb.ExecuteSql("exec sp_databases");
        }

        rsD.last();
        iMaxDatabases = rsD.getRow();
        for (int iRD = 1; iRD <= iMaxDatabases; iRD++)
        {
          rsD.absolute(iRD);
          iSkip = 0;
          switch (rsS.getInt("nmDbType"))
          {
          case 0:
            stDbName = rsD.getString("Database");
            if ((!stDbName.equals("mysql")) && (!stDbName.equals("information_schema")))
              break;
            iSkip = 1; break;
          case 1:
            stDbName = rsD.getString("DATABASE_NAME");
            if ((!stDbName.equals("model")) && (!stDbName.equals("master")) && (!stDbName.equals("msdb")) && (!stDbName.equals("pubs")) && (!stDbName.equals("tempdb")))
              break;
            iSkip = 1;
          }

          if (iSkip != 0)
          {
            continue;
          }
          int iDbId = this.ebEnt.dbEb.ExecuteSql1n("select nmDbId from t_databases where nmServerId=" + rsS.getInt("nmServerId") + " and stDbName='" + stDbName + "'");
          if (iDbId <= 0)
          {
            iDbId = this.ebEnt.dbEb.ExecuteSql1n("select max(nmDbId) from t_databases ");
            iDbId++;
            this.ebEnt.dbEb.ExecuteUpdate("insert into t_databases (nmDbId,nmServerId,stDbName) values(" + iDbId + ", " + rsS.getInt("nmServerId") + ",'" + stDbName + "') ");
          }
          this.ebEnt.dbEb.ExecuteUpdate("update t_databases set nmDbFlag = ( nmDbFlag & ~0x40000000) where nmDbId=" + iDbId);

          EbDatabase ebD = new EbDatabase(rsS.getInt("nmDbType"), rsS.getString("stServerIp"), rsS.getString("stUser"), rsS.getString("stPassword"), stDbName, "");
          switch (rsS.getInt("nmDbType"))
          {
          case 0:
            rsT = ebD.ExecuteSql("show table status");
            break;
          case 1:
            rsT = ebD.ExecuteSql("SELECT * FROM INFORMATION_SCHEMA.TABLES AS data WHERE TABLE_TYPE = 'BASE TABLE'");
          }

          rsT.last();
          iMaxTables = rsT.getRow();
          for (int iRT = 1; iRT <= iMaxTables; iRT++)
          {
            rsT.absolute(iRT);
            String stTableName = "";
            String stDbEngine = "";
            switch (rsS.getInt("nmDbType"))
            {
            case 0:
              stTableName = rsT.getString("Name");
              stDbEngine = rsT.getString("Engine");
              break;
            case 1:
              stTableName = rsT.getString("TABLE_NAME");
              stDbEngine = "MSSQL";
            }

            int iTableId = this.ebEnt.dbEb.ExecuteSql1n("select nmTableId from t_tables where nmDbId=" + iDbId + " and stTableName='" + stTableName + "'");
            if (iTableId <= 0)
            {
              iTableId = this.ebEnt.dbEb.ExecuteSql1n("select max(nmTableId) from t_tables");
              iTableId++;
              this.ebEnt.dbEb.ExecuteUpdate("insert into t_tables (nmTableId,nmDbId,stTableName,stDbEngine,dtLastCheck) values(" + iTableId + "," + iDbId + ",'" + stTableName + "','" + stDbEngine + "',now()) ");
            }
            this.ebEnt.dbEb.ExecuteUpdate("update t_tables set dtLastCheck=now(), nmTableFlag = ( nmTableFlag & ~0x40000000)  where nmTableId=" + iTableId);

            switch (rsS.getInt("nmDbType"))
            {
            case 0:
              rsF = ebD.ExecuteSql("explain " + stTableName);
              break;
            case 1:
              rsF = ebD.ExecuteSql("SELECT sysobjects.name AS table_name, syscolumns.name AS column_name, systypes.name AS datatype, syscolumns.LENGTH AS LENGTH FROM sysobjects  INNER JOIN syscolumns ON sysobjects.id = syscolumns.id  INNER JOIN systypes ON syscolumns.xtype = systypes.xtype  WHERE (sysobjects.xtype = 'U') and sysobjects.name = '" + stTableName + "' ");
            }

            rsF.last();
            int iMaxFields = rsF.getRow();
            for (int iRF = 1; iRF <= iMaxFields; iRF++)
            {
              rsF.absolute(iRF);
              String stDbFieldName = "";
              String stFieldType = "";
              String stDefault = "";
              String stExtra = "";
              int nmFieldFlag = 0;
              int[] aF = new int[3];
              switch (rsS.getInt("nmDbType"))
              {
              case 0:
                stDbFieldName = rsF.getString("Field");
                stFieldType = rsF.getString("Type");
                stDefault = rsF.getString("Default");
                stExtra = rsF.getString("Extra");
                if (rsF.getString("Key").equals("PRI"))
                {
                  nmFieldFlag |= 8388608;
                }
                if (rsF.getString("Key").equals("MUL"))
                {
                  nmFieldFlag |= 4194304;
                }
                if (rsF.getString("Extra").equals("auto_increment"))
                {
                  nmFieldFlag |= 2097152;
                }
                aF = parseFieldType(stFieldType);
                break;
              case 1:
                stDbFieldName = rsF.getString("column_name");
                stFieldType = rsF.getString("datatype");
                aF = parseFieldType(stFieldType);
                if ((!stFieldType.equals("char")) && (!stFieldType.equals("varchar")))
                  break;
                aF[1] = rsF.getInt("LENGTH");
              }

              int iFieldId = this.ebEnt.dbEb.ExecuteSql1n("select nmFieldId from t_fields where nmTableId=" + iTableId + " and stDbFieldName='" + stDbFieldName + "'");
              if (iFieldId <= 0)
              {
                iFieldId = this.ebEnt.dbEb.ExecuteSql1n("select max(nmFieldId) from t_fields ");
                iFieldId++;
                this.ebEnt.dbEb.ExecuteUpdate("insert into t_fields(nmFieldId,nmTableId,stDbFieldName) values(" + iFieldId + "," + iTableId + ",'" + stDbFieldName + "'  )");
              }
              this.ebEnt.dbEb.ExecuteUpdate("update t_fields set stFieldType='" + stFieldType + "',stDefault='" + stDefault + "',stExtra='" + stExtra + "',nmFieldType=" + aF[0] + ",nmFieldSize=" + aF[1] + ",nmTableOrder=" + iRF + ",nmFieldFlag = ( (nmFieldFlag |" + nmFieldFlag + " )& ~0x40000000) where nmFieldId=" + iFieldId);
            }
          }
          stReturn = stReturn + "<br>DB " + iRD + " of: " + iMaxDatabases;
        }

      }

      this.ebEnt.dbEb.ExecuteUpdate("delete from t_databases where ( nmDbFlag & 0x40000000) != 0 ");
      this.ebEnt.dbEb.ExecuteUpdate("delete from t_tables where ( nmTableFlag & 0x40000000) != 0 ");
      this.ebEnt.dbEb.ExecuteUpdate("delete from t_fields where ( nmFieldFlag & 0x40000000) != 0 ");

      stSql = "select * from t_server where nmDbType=1";
      ResultSet rsS1 = this.ebEnt.dbEb.ExecuteSql(stSql);
      if (rsS1 != null)
      {
        rsS1.last();
        int iMaxS1 = rsS1.getRow();
        for (int iS = 1; iS <= iMaxS1; iS++)
        {
          rsS1.absolute(iS);
          stDbName = this.ebEnt.dbEb.ExecuteSql1("SELECT max(stDbName) FROM t_databases where nmServerId=" + rsS1.getInt("nmServerId"));
          if ((stDbName == null) || (stDbName.equals("")))
            continue;
          EbDatabase ebTemp = new EbDatabase(rsS1.getInt("nmDbType"), rsS1.getString("stServerIp"), rsS1.getString("stUser"), rsS1.getString("stPassword"), stDbName, "");
          stSql = "SELECT KU.TABLE_CATALOG, KU.TABLE_NAME,KU.COLUMN_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS TC INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS KU ON TC.CONSTRAINT_TYPE = 'PRIMARY KEY' AND TC.CONSTRAINT_NAME = KU.CONSTRAINT_NAME ORDER BY KU.TABLE_CATALOG, KU.TABLE_NAME, KU.COLUMN_NAME";
          ResultSet rsPk = ebTemp.ExecuteSql(stSql);
          if (rsPk == null)
            continue;
          rsPk.last();
          int iMax = rsPk.getRow();
          for (int iR = 1; iR <= iMax; iR++)
          {
            rsPk.absolute(iR);
            int nmFieldId = this.ebEnt.dbEb.ExecuteSql1n("SELECT f.nmFieldId FROM t_fields f, t_tables t, t_databases d, t_server s where s.nmDbType=1 and s.nmServerId=d.nmServerId and d.nmDbId=t.nmDbId and t.nmTableId=f.nmTableId and d.stDbName='" + rsPk.getString("TABLE_CATALOG") + "' and t.stTableName='" + rsPk.getString("TABLE_NAME") + "' and f.stDbFieldName='" + rsPk.getString("COLUMN_NAME") + "'");
            this.ebEnt.dbEb.ExecuteUpdate("update t_fields set nmFieldFlag = ( nmFieldFlag | 0x00800000) where nmFieldId=" + nmFieldId);
          }

        }

      }

      stSql = "SELECT nmTableId,nmFieldId, stDbFieldName FROM t_fields where ( nmFieldFlag & 0x00800000) != 0 order by nmTableId, stDbFieldName";
      rs = this.ebEnt.dbEb.ExecuteSql(stSql);
      rs.last();
      iMaxRow = rs.getRow();
      for (int iR = 1; iR <= iMaxRow; iR++)
      {
        rs.absolute(iR);
        int iTableId = rs.getInt("nmTableId");

        if ((iTableId != iLastTable) && (iLastTable > 0))
        {
          this.ebEnt.dbEb.ExecuteUpdate("update t_tables set stPk='" + stPk + "', stPkFids='" + stPkFids + "' where nmTableId=" + iLastTable);
          stPk = stPkFids = "";
        }
        if (!stPk.equals(""))
        {
          stPk = stPk + "|";
          stPkFids = stPkFids + "|";
        }
        stPk = stPk + rs.getString("stDbFieldName");
        stPkFids = stPkFids + rs.getString("nmFieldId");

        iLastTable = iTableId;
      }
      if (iLastTable > 0)
      {
        this.ebEnt.dbEb.ExecuteUpdate("update t_tables set stPk='" + stPk + "', stPkFids='" + stPkFids + "' where nmTableId=" + iLastTable);
      }
      if (eb != null)
      {
        this.stError += eb.getError();
        eb.ebClose();
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<hr>getResetEb ERROR: " + e);
      if (eb != null)
      {
        this.stError += eb.getError();
        eb.ebClose();
      }
    }
    return stReturn;
  }

  public String addCompanyLinks(String stSql)
  {
    String stReturn = "";
    int iMax = 0;
    try
    {
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        iMax = rs.getRow();
      }
      for (int iC = 1; iC <= iMax; iC++)
      {
        rs.absolute(iC);
        this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25RefCompany (nmCompanyId,nmRefType,nmRefId) values(" + rs.getString("nmCompanyId") + ",1," + rs.getString("nmPersonId") + ")");
      }

    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<BR>ERROR addCompanyLinks: " + e);
    }
    return stReturn;
  }

  public String fixCrossLinksZombies(ResultSet rsA, HttpServletRequest request)
  {
    String stReturn = "";
    try
    {
      stReturn = stReturn + addCompanyLinks("SELECT DISTINCT ru1.nmPersonId as nmPersonId, ru2.nmPersonId as nmCompanyId  FROM X25RefUser ru1 left join X25RefCompany rc on rc.nmRefType=1 and rc.nmRefId=ru1.nmPersonId, X25RefUser ru2 where ru1.nmRefType=1 and ru1.nmUserId=ru2.nmUserId and ru2.nmRefType=2 and rc.nmCompanyId is null");

      stReturn = stReturn + addCompanyLinks("SELECT DISTINCT rp1.nmRefId as nmPersonId, rp2.nmRefId as nmCompanyId FROM X25RefPhone rp1 left join X25RefCompany rc on rc.nmRefType=1 and rc.nmRefId=rp1.nmRefId,X25RefPhone rp2 where rp1.nmRefType=1 and rp1.nmPhoneId=rp2.nmPhoneId and rp2.nmRefType=2 and rc.nmCompanyId is null");

      stReturn = stReturn + this.ebEnt.ebNorm.deleteZombies();
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<BR>ERROR fixCrossLinksZombies: " + e);
    }
    return stReturn;
  }

  public String fixCommHistory()
  {
    String stReturn = "";
    String stSql = "";
    int iMax = 0;
    int iMax2 = 0;
    int iMax3 = 0;
    int iMax4 = 0;
    int iMax5 = 0;
    try
    {
      stSql = "SELECT count(*),stForeignRefId FROM X25Communications where stForeignRefId != '' group by stForeignRefId having count(*) > 1";
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        iMax = rs.getRow();
      }
      for (int iC = 1; iC <= iMax; iC++)
      {
        rs.absolute(iC);

        ResultSet rs2 = this.ebEnt.dbEnterprise.ExecuteSql("select RecId from X25Communications where stForeignRefId = " + this.ebEnt.dbEnterprise.fmtDbString(rs.getString("stForeignRefId")) + " order by RecId ");

        if (rs2 != null)
        {
          rs2.last();
          iMax2 = rs2.getRow();
        }
        else {
          iMax2 = 0;
        }
        for (int iDel = 2; iDel <= iMax2; iDel++)
        {
          rs2.absolute(iDel);
          this.ebEnt.dbEnterprise.ExecuteUpdate("delete from X25Communications where RecId=" + rs2.getString("RecId"));
        }
      }
      stReturn = stReturn + "<br> " + iMax + " a) mulitple emails from gmail ";

      stSql = "SELECT u.RecId as nmUserId,u.stEMail,p.RecId as nmPersonId,c.RecId as Comp, c.stCompanyName,p.stFirstName,p.stLastName,com.* FROM X25Communications com left join X25User u on u.RecId=com.nmRefId left join X25RefUser ru on ru.nmRefType=1 and ru.nmUserId=u.RecId left join X25User uu on uu.RecId=com.nmRefId left join X25RefUser ruu on ruu.nmRefType=2 and ruu.nmUserId=u.RecId left join X25Company c on ruu.nmPersonId=c.RecId left join X25Person p on ru.nmPersonId=p.RecId where com.nmPersonId=0 and com.nmRefType=42 and p.RecId is null order by stEMail";

      ResultSet rs2 = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs2 != null)
      {
        rs2.last();
        iMax2 = rs2.getRow();
      }
      for (int iC2 = 1; iC2 <= iMax2; iC2++)
      {
        rs2.absolute(iC2);
        String[] aName = rs2.getString("stEMail").split("@");
        int i = this.ebEnt.ebNorm.getPersonId(aName[0], "", "", 0, rs2.getInt("nmUserId"));
        stReturn = stReturn + " added i " + i;
      }
      stReturn = stReturn + "<br> " + iMax2 + " b) add preson to new email addresses ";

      stSql = "SELECT p.RecId as nmPersonId,com.RecId FROM X25Communications com left join X25User u on u.RecId=com.nmRefId left join X25RefUser ru on ru.nmRefType=1 and ru.nmUserId=u.RecId left join X25Person p on ru.nmPersonId=p.RecId where com.nmPersonId=0 and com.nmRefType=42";

      ResultSet rs3 = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs3 != null)
      {
        rs3.last();
        iMax3 = rs3.getRow();
      }
      for (int iC3 = 1; iC3 <= iMax3; iC3++)
      {
        rs3.absolute(iC3);
        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25Communications set nmPersonId=" + rs3.getString("nmPersonId") + " where RecId=" + rs3.getString("RecId"));
      }
      stReturn = stReturn + "<br> " + iMax3 + " b) set person id in COMM PART 2";

      stSql = "SELECT com.RecId,com.nmPersonId,com.dtDate,com.nmFlags FROM X25Communications com left join X25HistoryLog h on com.RecId=h.nmRefId2 and h.nmRefType2=11 where h.RecId is null";

      ResultSet rs4 = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs4 != null)
      {
        rs4.last();
        iMax4 = rs4.getRow();
      }
      for (int iC4 = 1; iC4 <= iMax4; iC4++)
      {
        rs4.absolute(iC4);
        this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25HistoryLog (nmUserId,nmRefType,nmRefId,dtEventStartTime,dtEventEndTime,nmRefType2,nmRefId2,stComment,nmEventFlags) values(" + this.ebEnt.ebUd.getLoginId() + ",1," + rs4.getString("nmPersonId") + ",'" + rs4.getString("dtDate") + "','" + rs4.getString("dtDate") + "',11," + rs4.getString("RecId") + ",''," + rs4.getString("nmFlags") + ") ");
      }

      stReturn = stReturn + "<br> " + iMax4 + " c) add NEW COMM to HISTORY ";

      stSql = "SELECT count(*),stLastName,stFirstName,stMiddleName FROM X25Person where stLastName != '' group by stLastName,stMiddleName,stFirstName having count(*) > 1 order by stLastName,stMiddleName,stFirstName";
      ResultSet rs5 = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs5 != null)
      {
        rs5.last();
        iMax5 = rs5.getRow();
      }
      for (int iC5 = 1; iC5 <= iMax5; iC5++)
      {
        rs5.absolute(iC5);
        this.ebEnt.ebNorm.mergePerson(rs5.getString("stFirstName"), rs5.getString("stMiddleName"), rs5.getString("stLastName"));
      }
      stReturn = stReturn + "<br> " + iMax5 + " d) temporary ... fix multi entries created from import ";
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<BR>Error fixCommHistory: " + e);
      e.printStackTrace();
    }
    return stReturn;
  }

  public String getImportCompanies(ResultSet rsA, HttpServletRequest request)
  {
    String stReturn = "";
    EbDatabase db = null;
    String stDbName = null;
    String stSql = "";
    try
    {
      String stC = request.getParameter("c");
      if ((stC == null) || (stC.equals("")))
      {
        stC = "1";
      }
      int iSubState = Integer.parseInt(stC);
      if (iSubState > 1)
      {
        stDbName = request.getParameter("dbname");
        if ((stDbName != null) && (stDbName.length() > 0))
        {
          db = new EbDatabase(stDbName, this.ebEnt);
        }
        else {
          stReturn = stReturn + "<br><font color=red>ERROR: cannot leave dbname blank</font><br>";
        }
        if (db == null)
        {
          iSubState = 1;
        }
      }

      switch (iSubState)
      {
      case 1:
        stReturn = stReturn + "</form><form method=post><input type=hidden name=a value='" + request.getParameter("a") + "'>" + "<input type=hidden name=b value='" + request.getParameter("b") + "'>" + "<input type=hidden name=c value='2'>" + "<table>" + "<tr><td>DB Name(must be on same server as 'ederbase'):</td><td>" + "<input type=text name=dbname value=''></td></tr>" + "<tr><th colspan=2><input type=submit name=submit value='Import Now'</td></tr>" + "</table></form><br><font color=red>NOTE: it will store each person with phone/email in new row !!! need to fix it.</font>";

        break;
      case 2:
        stReturn = stReturn + "<table><tr><th colspan=2>Importing Companies from " + stDbName + "</th></tr>";
        ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql("SELECT DISTINCT ru.nmPersonId,u.stEMail FROM X25RefUser ru left join X25RefCompany rc on rc.nmRefType=1 and rc.nmRefId=ru.nmPersonId, X25User u where ru.nmRefType=1 and u.RecId=ru.nmUserId and rc.nmRefId is null");

        if (rs != null)
        {
          rs.last();
          int iMax = rs.getRow();
          stReturn = stReturn + "<tr><td>By Email reference: </td><td>" + iMax + "</td>";
          for (int iT = 1; iT <= iMax; iT++)
          {
            rs.absolute(iT);
            stSql = "select distinct mu.stEMail,mc.*,ma.*,mz.*,mw.* from X25Company mc left join X25WebSite mw on mw.nmCompanyId=mc.RecId, X25RefAddress mra, X25Address ma, X25Zip mz,X25RefCompany mrcc, X25RefUser mru, X25User mu where mc.RecId=mra.nmRefId and mra.nmRefType=2 and ma.RecId=mra.nmAddressId and mz.RecId=ma.nmZipId and mrcc.nmRefType=1 and mrcc.nmCompanyId=mc.RecId and mrcc.nmRefId = mru.nmPersonId and mru.nmRefType=1 and mru.nmUserId=mu.RecId and mu.stEMail = " + db.fmtDbString(rs.getString("stEMail"));

            ResultSet rsImport = db.ExecuteSql(stSql);
            if (rsImport == null)
              continue;
            rsImport.last();
            int iMaxI = rsImport.getRow();
            for (int iI = 1; iI <= iMaxI; iI++)
            {
              rsImport.absolute(iI);
              int nmAddressId = this.ebEnt.ebNorm.getAddressId(rsImport.getString("stAddress1"), rsImport.getString("stAddress2"), rsImport.getString("stZipCode"), rsImport.getString("stCity"), rsImport.getString("stStateShort"), rsImport.getString("stCountry"));

              int nmEmailId = 0;
              int nmPhoneId = 0;
              int nmWebId = this.ebEnt.ebNorm.getWebId(rsImport.getString("stUrl"));
              int nmCompanyId = this.ebEnt.ebNorm.getCompanyId(rsImport.getString("stCompanyName"), nmAddressId, nmPhoneId, nmEmailId, nmWebId);

              this.ebEnt.ebNorm.addCompanyRef(nmCompanyId, 1, rs.getInt("nmPersonId"));
            }
            rsImport.close();
          }

          rs.close();
        }

        rs = this.ebEnt.dbEnterprise.ExecuteSql("SELECT DISTINCT rp.nmRefId as nmPersonId,p.stPhone FROM X25RefPhone rp left join X25RefCompany rc on rc.nmRefType=1 and rc.nmRefId=rp.nmRefId, X25Phone p where rp.nmRefType=1 and p.RecId=rp.nmPhoneId and rc.nmRefId is null");

        if (rs != null)
        {
          rs.last();
          int iMax = rs.getRow();
          stReturn = stReturn + "<tr><td>By Phone reference: </td><td>" + iMax + "</td>";
          for (int iT = 1; iT <= iMax; iT++)
          {
            rs.absolute(iT);
            stSql = "select distinct mp.stPhone,mc.*,ma.*,mz.*,mw.* from X25Company mc left join X25WebSite mw on mw.nmCompanyId=mc.RecId, X25RefAddress mra, X25Address ma, X25Zip mz,X25RefCompany mrcc, X25RefPhone mrp, X25Phone mp where mc.RecId=mra.nmRefId and mra.nmRefType=2 and ma.RecId=mra.nmAddressId and mz.RecId=ma.nmZipId and mrcc.nmRefType=1 and mrcc.nmCompanyId=mc.RecId and mrcc.nmRefId = mrp.nmRefId and mrp.nmRefType=1 and mrp.nmPhoneId=mp.RecId and mp.stPhone = " + db.fmtDbString(rs.getString("stPhone"));

            ResultSet rsImport = db.ExecuteSql(stSql);
            if (rs.getString("stPhone").equals("7189379923"))
            {
              stSql = "xx";
            }
            if (rsImport == null)
              continue;
            rsImport.last();
            int iMaxI = rsImport.getRow();
            for (int iI = 1; iI <= iMaxI; iI++)
            {
              rsImport.absolute(iI);
              int nmAddressId = this.ebEnt.ebNorm.getAddressId(rsImport.getString("stAddress1"), rsImport.getString("stAddress2"), rsImport.getString("stZipCode"), rsImport.getString("stCity"), rsImport.getString("stStateShort"), rsImport.getString("stCountry"));

              int nmEmailId = 0;
              int nmPhoneId = 0;
              int nmWebId = this.ebEnt.ebNorm.getWebId(rsImport.getString("stUrl"));
              int nmCompanyId = this.ebEnt.ebNorm.getCompanyId(rsImport.getString("stCompanyName"), nmAddressId, nmPhoneId, nmEmailId, nmWebId);

              this.ebEnt.ebNorm.addCompanyRef(nmCompanyId, 1, rs.getInt("nmPersonId"));
            }
            rsImport.close();
          }

          rs.close();
        }
        if (db != null)
        {
          this.stError += db.getError();
        }

        stReturn = stReturn + "</table>";
        break;
      default:
        stReturn = stReturn + "<BR>ERROR getImportCompanies: invalid sub-state/sub=process: " + iSubState;
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<br>ERROR getImportCompanies: " + e);
      e.printStackTrace();
    }
    return stReturn;
  }

  public String getImportEnterprise(ResultSet rsA, HttpServletRequest request)
  {
    String stReturn = "";
    EbDatabase db = null;
    String stDbName = null;
    String stSql = "";
    try
    {
      String stC = request.getParameter("c");
      if ((stC == null) || (stC.equals("")))
      {
        stC = "1";
      }
      int iSubState = Integer.parseInt(stC);
      if (iSubState > 1)
      {
        stDbName = request.getParameter("dbname");
        if ((stDbName != null) && (stDbName.length() > 0))
        {
          db = new EbDatabase(stDbName, this.ebEnt);
        }
        else {
          stReturn = stReturn + "<br><font color=red>ERROR: cannot leave dbname blank</font><br>";
        }
        if (db == null)
        {
          iSubState = 1;
        }
      }

      switch (iSubState)
      {
      case 1:
        stReturn = stReturn + "</form><form method=post><input type=hidden name=a value='" + request.getParameter("a") + "'>" + "<input type=hidden name=b value='" + request.getParameter("b") + "'>" + "<input type=hidden name=c value='2'>" + "<table>" + "<tr><td>DB Name(must be on same server as 'ederbase'):</td><td>" + "<input type=text name=dbname value=''></td></tr>" + "<tr><td>Query:</td><td><textarea name=sql rows=10 cols=100>" + "select p.stLastName, p.stFirstName, p.stMiddleName,ph.stPhone,u.stEMail, h.*" + "\n from X25HistoryLog h,X25Person p\n left join X25RefPhone rp on p.RecId=rp.nmRefId and rp.nmRefType=1" + "\n left join X25Phone ph on rp.nmPhoneId=ph.RecId" + "\n left join X25RefUser ru on p.RecId=ru.nmPersonId and ru.nmRefType=1" + "\n left join X25User u on ru.nmUserId=u.RecId" + "\n where p.RecId=h.nmRefId and h.nmRefType=1 and h.stComment != '' and h.nmEventFlags = 19" + "\n order by p.stLastName, p.stFirstName, h.RecId</textarea></td></tr>" + "<tr><th colspan=2><input type=submit name=submit value='Import Now'</td></tr>" + "</table></form><br><font color=red>NOTE: it will store each person with phone/email in new row !!! need to fix it.</font>";

        break;
      case 2:
        stSql = request.getParameter("sql");
        ResultSet rsImport = db.ExecuteSql(stSql);
        stReturn = stReturn + "<table><tr><th colspan=2>Importing from " + stDbName + "</th></tr>";
        if (rsImport != null)
        {
          rsImport.last();
          int iMax = rsImport.getRow();
          String stLast = "";
          String stBg = "";
          int nmPersonId = 0;
          for (int iI = 1; iI <= iMax; iI++)
          {
            rsImport.absolute(iI);
            String stFirstName = rsImport.getString("stFirstName");
            if (stFirstName == null)
            {
              stFirstName = "";
            }
            String stLastName = rsImport.getString("stLastName");
            if (stLastName == null)
            {
              stLastName = "";
            }
            String stMiddleName = rsImport.getString("stMiddleName");
            if (stMiddleName == null)
            {
              stMiddleName = "";
            }
            String stPhone = rsImport.getString("stPhone");
            if (stPhone == null)
            {
              stPhone = "";
            }
            String stEMail = rsImport.getString("stEMail");
            if (stEMail == null)
            {
              stEMail = "";
            }
            nmPersonId = this.ebEnt.ebNorm.getPersonId(stFirstName, stMiddleName, stLastName, stPhone, "US", stEMail);

            if (stLast.equals(rsImport.getString("RecId")))
            {
              stBg = " bgcolor=pink ";
            }
            else {
              this.ebEnt.ebNorm.addHistory(1, nmPersonId, rsImport.getString("dtEventStartTime"), rsImport.getString("stComment"), 0, 0);
              stBg = " bgcolor=lightgreen ";
            }

            stReturn = stReturn + "<tr><td align=right>" + nmPersonId + "</td>" + "<td>" + stFirstName + "</td>" + "<td>" + stLastName + "</td>" + "<td>" + stPhone + "</td>" + "<td>" + stEMail + "</td>" + "<td " + stBg + ">" + rsImport.getString("RecId") + "</td>" + "<td>" + rsImport.getString("dtEventStartTime") + "</td>" + "<td>" + rsImport.getString("stComment") + "</td>" + "</tr>";

            stLast = rsImport.getString("RecId");
          }
        }
        stReturn = stReturn + "</table>";
        break;
      default:
        stReturn = stReturn + "<BR>ERROR getImportEnterprise: invalid sub-state/sub=process: " + iSubState;
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<br>ERROR getImportEnterprise: " + e);
      e.printStackTrace();
    }
    return stReturn;
  }

  public String getTableId(HttpServletRequest request)
  {
    String stTb = request.getParameter("tb");
    String stTn = "";
    try
    {
      if (stTb != null)
      {
        String stDb = "";
        String[] aV = stTb.split("\\.");
        if (aV[1].equals("d"))
          stDb = this.ebEnt.dbDyn.getDbName();
        else if (aV[1].equals("e"))
          stDb = this.ebEnt.dbEnterprise.getDbName();
        else
          stDb = aV[1];
        stTn = this.ebEnt.dbEb.ExecuteSql1("select t.nmTableId from t_tables t, t_databases d where t.nmDbId=d.nmDbId and d.nmServerId=" + aV[0] + " and d.stDbName=\"" + stDb + "\" and t.stTableName = \"" + aV[2] + "\"");
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR getTableId " + e);
    }
    return stTn;
  }

  public String getTableEdit(HttpServletRequest request)
  {
    String stReturn = "";
    String stReturn1 = "";
    String stSql = "";
    ResultSet rs = null;
    int iRowMax = 0;
    String stValue = null;
    int iF = 0;
    int iD = 0;
    EbDatabase dbTemp = null;
    int iLookup = 0;
    String stTn = request.getParameter("tn");
    if (stTn == null)
    {
      stTn = getTableId(request);
      iLookup = 1;
    }
    String stTid = request.getParameter("tid");
    try
    {
      this.ebEnt.dbEb.resetError();

      if ((stTn == null) || (stTn.equals("")))
      {
        stSql = "SELECT * FROM t_tables t, t_databases d, t_server s where s.nmServerId = d.nmServerId and d.nmDbId = t.nmDbId ";
        String s1 = request.getParameter("setup");
        if ((s1 != null) && (s1.equals("y")))
        {
          stSql = stSql + " and d.stDbName in ('ederbase','ebenterprise') ";
        }
        stSql = stSql + " order by t.stTableName, d.stDbName, s.stServerName";
        rs = this.ebEnt.dbEb.ExecuteSql(stSql);
        rs.last();
        iRowMax = rs.getRow();
        stReturn = stReturn + "<form method=post><table border=1><tr><th colspan=5>Table Editor</th></tr>";
        for (iF = 1; iF <= iRowMax; iF++)
        {
          rs.absolute(iF);
          stReturn = stReturn + "<tr>";
          stReturn = stReturn + "<td align=right>" + rs.getString("nmRows") + "</td>";
          stReturn = stReturn + "<td><a href='./?a=28&tn=" + rs.getString("nmTableId") + "'><b>" + rs.getString("stTableName") + "</b></a></td>";
          stReturn = stReturn + "<td>" + rs.getString("stDbName") + "</td>";
          stReturn = stReturn + "<td>" + rs.getString("stServerName") + "</td>";
          stReturn = stReturn + "<td>" + rs.getString("nmServerId") + "</td>";
          stReturn = stReturn + "</tr>";
        }

        stReturn = stReturn + "</table>";
      }
      else {
        stSql = "SELECT s.*,t.stTableName,d.stDbName,t.stPk,t.stPkFids FROM t_tables t, t_databases d, t_server s where s.nmServerId = d.nmServerId and d.nmDbId = t.nmDbId and t.nmTableId=" + stTn;

        ResultSet rsDb = this.ebEnt.dbEb.ExecuteSql(stSql);
        rsDb.absolute(1);
        dbTemp = new EbDatabase(rsDb.getInt("nmDbType"), rsDb.getString("stServerIp"), rsDb.getString("stUser"), rsDb.getString("stPassword"), rsDb.getString("stDbName"), rsDb.getString("stConnectString"));
        String stSqlList = "";
        if (rsDb.getInt("nmDbType") == 1)
        {
          stSqlList = "select top 1000 * from " + rsDb.getString("stTableName");
        }
        else {
          stSqlList = "select * from " + rsDb.getString("stTableName") + " limit 1000 ";
        }
        ResultSet rsF = this.ebEnt.dbEb.ExecuteSql("select * from t_fields where nmTableId=" + stTn + " order by stDbFieldName");
        rsF.last();
        int iFieldMax = rsF.getRow();
        String[] aFields = new String[iFieldMax + 1];
        stReturn1 = stReturn1 + "<th>Action</th>";
        iF = 1;
        for (; iF <= iFieldMax; iF++)
        {
          rsF.absolute(iF);
          stReturn1 = stReturn1 + "<th>" + rsF.getString("stDbFieldName") + "</th>";
          aFields[iF] = rsF.getString("stDbFieldName");
        }

        rs = dbTemp.ExecuteSql(stSqlList);
        String stPk = rsDb.getString("stPk");
        String[] aPk = stPk.split("\\|");
        stReturn1 = stReturn1 + "</tr>";

        if ((stTid != null) && (!stTid.equals("")))
        {
          String[] aPkValue = stTid.split("\\|");
          stPk = "";

          int i = 0;
          for (; i < aPkValue.length; i++)
          {
            if (i > 0)
            {
              stPk = stPk + " and ";
            }
            stPk = stPk + aPk[i];

            stPk = stPk + "=";

            int isNumber = 0;
            try
            {
              int i1 = Integer.parseInt(aPkValue[i]);
              isNumber = 1;
            }
            catch (Exception e)
            {
              isNumber = 0;
            }

            if (isNumber == 0)
            {
              stPk = stPk + "\"";
            }
            stPk = stPk + aPkValue[i];

            if (isNumber != 0)
              continue;
            stPk = stPk + "\"";
          }

          String stTemp = request.getParameter("del");
          if ((stTemp != null) && (stTemp.equals("y")))
          {
            stSql = "delete from " + rsDb.getString("stTableName") + " where " + stPk;
            dbTemp.ExecuteUpdate(stSql);

            stTid = null;
            rs = dbTemp.ExecuteSql(stSqlList);
          }
          else {
            stTemp = request.getParameter("Save");
            String stOrig = "";
            String stUpdate = "";
            if ((stTemp != null) && ((stTemp.equals("Save data")) || (stTemp.equals("Insert as new"))))
            {
              if (((aPkValue[0].length() >= 2) && (aPkValue[0].substring(0, 2).equals("-2"))) || (stTemp.equals("Insert as new")))
              {
                String s1 = "";
                String s2 = "";

                iF = 1;
                for (; iF <= iFieldMax; iF++)
                {
                  rsF.absolute(iF);
                  stTemp = request.getParameter("g" + rsF.getInt("nmFieldId") + "|" + stTid);

                  if (stTemp == null)
                  {
                    stTemp = "";
                  }
                  if (iF > 1)
                  {
                    s1 = s1 + ", ";
                    s2 = s2 + ", ";
                  }

                  s1 = s1 + rsF.getString("stDbFieldName");
                  s2 = s2 + dbTemp.fmtDbString(stTemp);
                }

                stSql = "insert into " + rsDb.getString("stTableName") + " (" + s1 + ") values(" + s2 + ")";
                dbTemp.ExecuteUpdate(stSql);
              }
              else {
                stSql = "select * from " + rsDb.getString("stTableName") + " where " + stPk;
                ResultSet rsOrig = dbTemp.ExecuteSql(stSql);
                rsOrig.absolute(1);
                for (iF = 1; iF <= iFieldMax; iF++)
                {
                  rsF.absolute(iF);
                  stTemp = request.getParameter("g" + rsF.getInt("nmFieldId") + "|" + stTid);
                  if (stTemp == null)
                  {
                    stTemp = "";
                  }
                  stOrig = rsOrig.getString(rsF.getString("stDbFieldName"));
                  if (stOrig == null)
                  {
                    stOrig = "";
                  }
                  if (stTemp.equals(stOrig))
                    continue;
                  if (!stUpdate.equals(""))
                  {
                    stUpdate = stUpdate + ", ";
                  }
                  stUpdate = stUpdate + rsF.getString("stDbFieldName");
                  stUpdate = stUpdate + "=";
                  stUpdate = stUpdate + dbTemp.fmtDbString(stTemp);
                }

                if (!stUpdate.equals(""))
                {
                  dbTemp.ExecuteUpdate("update " + rsDb.getString("stTableName") + " set " + stUpdate + " where " + stPk);
                }
              }

              stTid = null;
              rs = dbTemp.ExecuteSql(stSqlList);
              if (iLookup > 0)
                return stReturn;
            }
          }
        }
        if ((stTid == null) || (stTid.trim().equals("")))
        {
          stReturn = stReturn + "<form method=post><table border=1><tr><th colspan=" + (iFieldMax + 1) + ">Table: " + rsDb.getString("nmServerId") + "." + rsDb.getString("stDbName") + "." + rsDb.getString("stTableName") + " [" + iRowMax + " rows] <a href='./?a=28&tn=" + stTn + "&tid=-2' title='Insert'><img border=0 src='./common/b_insrow.png'> Insert</a></th></tr><tr>";
          stReturn = stReturn + stReturn1;
          rs.last();
          iRowMax = rs.getRow();
          for (iD = 1; iD <= iRowMax; iD++)
          {
            stReturn1 = "";
            rs.absolute(iD);
            String stPkValue = "";
            for (iF = 1; iF <= iFieldMax; iF++)
            {
              stValue = rs.getString(aFields[iF]);
              if (stValue == null)
              {
                stValue = "&nbsp;";
              }
              else {
                stValue = stValue.replace("<", "&lt;");
              }

              if (rsDb.getString("stPk").contains(aFields[iF]))
              {
                if (!stPkValue.equals(""))
                {
                  stPkValue = stPkValue + "|";
                }
                stPkValue = stPkValue + stValue;
              }

              if ((stValue == null) || (stValue.equals("")))
              {
                stValue = "&nbsp;";
              }
              stReturn1 = stReturn1 + "<td valign=top>" + stValue + "</td>";
            }

            stReturn = stReturn + "<tr>";
            if (!stPkValue.equals(""))
            {
              stReturn = stReturn + "<td valign=top><a title='Edit' href='./?a=28&tn=" + stTn + "&tid=" + stPkValue + "'><img border=0 src='./common/b_edit.png'></a> <a title='Delete' href='./?a=28&tn=" + stTn + "&tid=" + stPkValue + "&del=y' onclick=\"return confirm('Are you sure you want to delete?')\"><img border=0 src='./common/b_drop.png'></a></td>";
            }
            else {
              stReturn = stReturn + "<td>&nbsp;??</td>";
            }
            stReturn = stReturn + stReturn1;
            stReturn1 = "";
          }

          stReturn = stReturn + "</table>";
        }
        else {
          String[] aPkValue = stTid.split("\\|");
          stPk = "";
          for (int i = 0; i < aPkValue.length; i++)
          {
            if (i > 0)
            {
              stPk = stPk + " and ";
            }
            stPk = stPk + aPk[i];
            stPk = stPk + "=";
            int isNumber = 0;
            try
            {
              int i1 = Integer.parseInt(aPkValue[i]);
              isNumber = 1;
            }
            catch (Exception e) {
              isNumber = 0;
            }

            if (isNumber == 0)
            {
              stPk = stPk + "\"";
            }
            stPk = stPk + aPkValue[i];
            if (isNumber != 0)
              continue;
            stPk = stPk + "\"";
          }

          stSql = "select * from " + rsDb.getString("stTableName") + " where " + stPk;
          rs = dbTemp.ExecuteSql(stSql);
          rs.last();
          iRowMax = 1;

          stReturn = stReturn + "\n<form method=post><table border=0><tr><th colspan=2 bgcolor='skyblue'>Edit Table: " + rsDb.getString("nmServerId") + "." + rsDb.getString("stDbName") + "." + rsDb.getString("stTableName") + "</th></tr><tr>";

          String stBg = "";
          for (iD = 1; iD <= iRowMax; iD++)
          {
            iRowMax = rs.getRow();
            if (iRowMax >= iD)
            {
              rs.absolute(iD);
            }
            else {
              rs = null;
            }
            if (iD >= 1)
            {
              stReturn = stReturn + "<tr><td colspan=2><hr></td></tr>";
            }
            for (iF = 1; iF <= iFieldMax; iF++)
            {
              rsF.absolute(iF);
              stReturn = stReturn + "\n<tr>";

              if ((rsF.getInt("nmFieldFlag") & 0x800000) != 0)
              {
                stBg = " bgcolor='yellow' ";
              }
              else {
                stBg = "";
              }
              stReturn = stReturn + "<td align=right valign=top " + stBg + ">" + rsF.getString("stDbFieldName") + "</th>";

              if (rs != null)
              {
                stValue = rs.getString(aFields[iF]);
              }
              else {
                stValue = "";
              }
              if (stValue != null)
                stValue = stValue.replace("&", "&amp;");
              stReturn = stReturn + dbTemp.fmtInput(1, rsF, stValue, stTid);
              stReturn = stReturn + "<tr>";
            }
          }
          stReturn = stReturn + "\n<tr><td align=center colspan=2><input type=submit name=Save value='Save data'> &nbsp;&nbsp;&nbsp;&nbsp;<input type=submit name=Save value='Insert as new'></td></tr></table></form>";
        }
      }
      if (dbTemp != null)
      {
        this.stError += dbTemp.getError();
        dbTemp.ebClose();
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "ERROR getTableEdit( " + stTn + " ,  " + stTid + ") iD " + iD + " iF " + iF + " stValue [" + stValue + "]: " + e);
      if (dbTemp != null)
      {
        this.stError += dbTemp.getError();
        dbTemp.ebClose();
      }
    }
    return stReturn;
  }

  int[] parseFieldType(String stFieldType)
  {
    int[] aF = new int[2];
    String stTemp = "";
    int i1 = 0;
    int i2 = 0;
    i1 = stFieldType.indexOf('(');

    i2 = stFieldType.indexOf(')');

    aF[1] = 1;
    if ((i1 > 0) && (i2 > i1))
    {
      stTemp = stFieldType.substring(i1 + 1, i2);
      i2 = stTemp.indexOf(',');

      if (i2 > 0)
      {
        stTemp = stTemp.substring(0, i2);
      }
      aF[1] = Integer.parseInt(stTemp);
    }

    if (i1 > 0)
    {
      stTemp = stFieldType.substring(0, i1).toLowerCase();
    }
    else {
      stTemp = stFieldType.toLowerCase();
    }

    if (stTemp.equals("bigint"))
    {
      aF[0] = 35;
    } else if ((stTemp.equals("year")) || (stTemp.equals("bit")) || (stTemp.equals("byte")) || (stTemp.equals("tinyint")) || (stTemp.equals("counter")) || (stTemp.equals("enum")) || (stTemp.equals("int")) || (stTemp.equals("integer")) || (stTemp.equals("mediumint")) || (stTemp.equals("smallint")) || (stTemp.equals("timestamp")))
    {
      aF[0] = 10;
      if (stTemp.equals("year"))
      {
        aF[1] = 4;
      }
      if (stTemp.equals("bit"))
      {
        aF[1] = 1;
      }
      if ((stTemp.equals("byte")) || (stTemp.equals("tinyint")))
      {
        aF[1] = 3;
      }
      if (stTemp.equals("smallint"))
      {
        aF[1] = 5;
      }
      if (stTemp.equals("mediumint"))
      {
        aF[1] = 7;
      }
      if (stTemp.equals("bigint"))
      {
        aF[1] = 20;
      }
      if (stTemp.indexOf("unsigned") <= 0)
      {
        aF[1] += 1;
      }
    } else if ((stTemp.equals("char")) || (stTemp.equals("sysname")) || (stTemp.equals("varchar")))
    {
      aF[0] = 3;
    } else if ((stTemp.equals("set")) || (stTemp.equals("image")) || (stTemp.equals("blob")) || (stTemp.equals("longblob")) || (stTemp.equals("longtext")) || (stTemp.equals("longchar")) || (stTemp.equals("mediumblob")) || (stTemp.equals("mediumtext")) || (stTemp.equals("nchar")) || (stTemp.equals("ntext")) || (stTemp.equals("text")) || (stTemp.equals("tinyblob")))
    {
      aF[0] = 4;
    } else if ((stTemp.equals("float")) || (stTemp.equals("decimal")) || (stTemp.equals("double")) || (stTemp.equals("money")) || (stTemp.equals("real")))
    {
      aF[0] = 31;
    } else if (stTemp.equals("date"))
    {
      aF[0] = 20;
    } else if (stTemp.equals("datetime"))
    {
      aF[0] = 8;
    }
    else {
      aF[0] = -1;
    }

    return aF;
  }

  public String getError()
  {
    return this.stError;
  }

  public String fixMailQ()
  {
    String stReturn = "";
    String stValue = this.ebEnt.ebUd.request.getParameter("stMailQ");
    String stLine = "";
    if ((stValue != null) && (stValue.length() > 0))
    {
      stReturn = stReturn + "<br>Procesing MailQ " + stValue.length();
      String[] aLines = stValue.trim().split("\n");
      int iL = 0; for (int iFound = 0; iL < aLines.length; iL++)
      {
        stLine = aLines[iL].trim();
        if (stLine.length() <= 0)
          continue;
        if (iFound > 0)
        {
          if (EbStatic.isEmail(stLine) != true)
            continue;
          int nmErrorCount = incEmailError(stLine);
          stReturn = stReturn + "<br>Removing: " + stLine + " nmErrorCount: " + nmErrorCount;
          iFound = 0;
        }
        else if (stLine.indexOf("said: 451") >= 0)
        {
          iFound = 1;
        } else if (stLine.indexOf("Connection timed out") >= 0)
        {
          iFound = 1;
        } else if (stLine.indexOf("not found") >= 0)
        {
          iFound = 1;
        } else if (stLine.indexOf("Unable to select desired mailbox") >= 0)
        {
          iFound = 1; } else {
          if (stLine.indexOf("said: 451") < 0)
            continue;
          iFound = 1;
        }
      }

    }
    else
    {
      stReturn = stReturn + "</form><form method=post><br>Copy mailq output here<br><textarea name='stMailQ' cols=150 rows=20></textarea></br><input type=submit name=sumbit8 value='Process mailq'></form>";
    }

    return stReturn;
  }

  public String processHourly(int iInterval)
  {
    String stReturn = "";
    String stSql = "";
    try
    {
      if (iInterval > 0)
      {
        stSql = "SELECT count(*) as cnt, year(dtEntered)as y,month(dtEntered) as m, day(dtEntered) as d, hour(dtEntered) as h FROM X25Counter where dtEntered >= DATE_ADD(now(),INTERVAL -" + iInterval + " HOUR) " + "group by year(dtEntered), month(dtEntered) ,day(dtEntered) ,hour(dtEntered) " + "order by year(dtEntered) desc,month(dtEntered) desc,day(dtEntered) desc,hour(dtEntered) desc";
      }
      else
      {
        stSql = "SELECT count(*) as cnt, year(dtEntered)as y,month(dtEntered) as m, day(dtEntered) as d, hour(dtEntered) as h FROM X25Counter group by year(dtEntered), month(dtEntered) ,day(dtEntered) ,hour(dtEntered) order by year(dtEntered) desc,month(dtEntered) desc,day(dtEntered) desc,hour(dtEntered) desc";

        this.ebEnt.dbEnterprise.ExecuteUpdate("truncate table MyHourlyTable");
      }
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        if ((iInterval >= 2) && (iMax > iInterval))
        {
          iMax = 2;
        }
        for (int iH = 1; iH <= iMax; iH++)
        {
          rs.absolute(iH);
          stSql = "replace into MyHourlyTable (nmClicks,nmYear,nmMonth,nmDay,nmHour) values(" + rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3) + "," + rs.getString(4) + "," + rs.getString(5) + ")";

          this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
        }
        stReturn = stReturn + "<BR>Total Stats: " + iMax;
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR processHourly: " + e);
    }
    stReturn = stReturn + processHourlyPets(iInterval);
    stReturn = stReturn + processHourlyMood(iInterval);
    return stReturn;
  }

  public String processHourlyMood(int iInterval)
  {
    String stReturn = "";
    String stSql = "";
    try
    {
      if (iInterval > 0)
      {
        stSql = "SELECT count(*) as cnt, year(dtReported)as y,month(dtReported) as m, day(dtReported) as d, hour(dtReported) as h,sum(nmMood) as mood  FROM MyMoodReport where dtReported >= DATE_ADD(now(),INTERVAL -" + iInterval + " HOUR) " + "group by year(dtReported), month(dtReported) ,day(dtReported) ,hour(dtReported) " + "order by year(dtReported) desc,month(dtReported) desc,day(dtReported) desc,hour(dtReported) desc";
      }
      else
      {
        stSql = "SELECT count(*) as cnt, year(dtReported)as y,month(dtReported) as m, day(dtReported) as d, hour(dtReported) as h,sum(nmMood) as mood  FROM MyMoodReport group by year(dtReported), month(dtReported) ,day(dtReported) ,hour(dtReported) order by year(dtReported) desc,month(dtReported) desc,day(dtReported) desc,hour(dtReported) desc";
      }

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        if ((iInterval >= 2) && (iMax > iInterval))
        {
          iMax = 2;
        }
        for (int iH = 1; iH <= iMax; iH++)
        {
          rs.absolute(iH);
          int i = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from MyHourlyTable where nmYear=" + rs.getString(2) + " and nmMonth=" + rs.getString(3) + " and " + "nmDay=" + rs.getString(4) + " and nmHour=" + rs.getString(5));

          if (i <= 0)
          {
            stSql = "insert into MyHourlyTable (nmMoodCount,nmYear,nmMonth,nmDay,nmHour,nmMoodSum) values(" + rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3) + "," + rs.getString(4) + "," + rs.getString(5) + "," + rs.getString(6) + ")";
          }
          else
          {
            stSql = "update MyHourlyTable set nmMoodCount=" + rs.getString(1) + ",nmMoodSum=" + rs.getString(6) + " where nmYear=" + rs.getString(2) + " and nmMonth=" + rs.getString(3) + " and " + "nmDay=" + rs.getString(4) + " and nmHour=" + rs.getString(5);
          }

          this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
        }
        stReturn = stReturn + "<BR>Total Mood: " + iMax;
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR processHourly: " + e);
    }
    return stReturn;
  }

  public String processHourlyPets(int iInterval)
  {
    String stReturn = "";
    String stSql = "";
    try
    {
      if (iInterval > 0)
      {
        stSql = "SELECT count(*) as cnt, year(dtReported)as y,month(dtReported) as m, day(dtReported) as d, hour(dtReported) as h,sum(nmPetMood) as mood  FROM MyPetReport where dtReported >= DATE_ADD(now(),INTERVAL -" + iInterval + " HOUR) " + "group by year(dtReported), month(dtReported) ,day(dtReported) ,hour(dtReported) " + "order by year(dtReported) desc,month(dtReported) desc,day(dtReported) desc,hour(dtReported) desc";
      }
      else
      {
        stSql = "SELECT count(*) as cnt, year(dtReported)as y,month(dtReported) as m, day(dtReported) as d, hour(dtReported) as h,sum(nmPetMood) as mood  FROM MyPetReport group by year(dtReported), month(dtReported) ,day(dtReported) ,hour(dtReported) order by year(dtReported) desc,month(dtReported) desc,day(dtReported) desc,hour(dtReported) desc";
      }

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        if ((iInterval >= 2) && (iMax > iInterval))
        {
          iMax = 2;
        }
        for (int iH = 1; iH <= iMax; iH++)
        {
          rs.absolute(iH);
          String stDetail = "";
          String stDate = rs.getString(2) + "-" + rs.getString(3) + "-" + rs.getString(4) + " " + rs.getString(5);
          stSql = "SELECT count(*) as cnt, year(dtReported)as y,month(dtReported) as m, day(dtReported) as d, hour(dtReported) as h,sum(nmPetMood)/ count(*) as avg, stCountry,stState,stCity,stIp FROM MyPetReport where dtReported >='" + stDate + ":00:00' and dtReported <='" + stDate + ":59:59' " + "group by stIp, year(dtReported), month(dtReported) ,day(dtReported) ,hour(dtReported),stCountry,stState,stCity  " + "order by stIP, year(dtReported) desc,month(dtReported) desc,day(dtReported) desc,hour(dtReported) desc,stCountry,stState,stCity";

          ResultSet rs1 = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
          if (rs1 != null)
          {
            rs1.last();
            int iMx = rs1.getRow();
            stDetail = "<table width='100%'>";
            for (int iM = 1; iM <= iMx; iM++)
            {
              rs1.absolute(iM);
              stDetail = stDetail + "<tr>";
              stDetail = stDetail + "<td class='FormField'>" + rs1.getString("stCountry") + "</td>";
              stDetail = stDetail + "<td class='FormField'>" + rs1.getString("stState") + "</td>";
              stDetail = stDetail + "<td class='FormField'>" + rs1.getString("stCity") + "</td>";
              stDetail = stDetail + "<td class='FormField'>" + rs1.getString("cnt") + "</td>";
              stDetail = stDetail + "<td class='FormField'><b>" + rs1.getString("avg") + "</b></td>";
              stDetail = stDetail + "<td class='FormField'>" + rs1.getString("stIp") + "</td>";
              stDetail = stDetail + "</tr>";
            }
            stDetail = stDetail + "</table>";
            rs1.close();
          }
          int i = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from MyHourlyTable where nmYear=" + rs.getString(2) + " and nmMonth=" + rs.getString(3) + " and " + "nmDay=" + rs.getString(4) + " and nmHour=" + rs.getString(5));

          if (i <= 0)
          {
            stSql = "insert into MyHourlyTable (nmPetCount,nmYear,nmMonth,nmDay,nmHour,nmPetSum,stPetDetail) values(" + rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3) + "," + rs.getString(4) + "," + rs.getString(5) + "," + rs.getString(6) + "," + this.ebEnt.dbEnterprise.fmtDbString(stDetail) + ")";
          }
          else
          {
            stSql = "update MyHourlyTable set nmPetCount=" + rs.getString(1) + ",nmPetSum=" + rs.getString(6) + "," + "stPetDetail = " + this.ebEnt.dbEnterprise.fmtDbString(stDetail) + " where nmYear=" + rs.getString(2) + " and nmMonth=" + rs.getString(3) + " and " + "nmDay=" + rs.getString(4) + " and nmHour=" + rs.getString(5);
          }

          this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
          stReturn = stReturn + "<BR>" + iH + ": " + rs.getString(5) + " Pet Count: " + rs.getString(1);
        }
        stReturn = stReturn + "<BR>Total Pet: " + iMax;
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR processHourly: " + e);
    }
    return stReturn;
  }

  public String processTwitter(HttpServletRequest request)
  {
    String stReturn = "";
    String stValue = this.ebEnt.ebUd.request.getParameter("stInsert");

    if ((stValue != null) && (stValue.length() > 0))
    {
      stReturn = stReturn + "<h1>Processing Twitter: search and follow</h1>";
      EbTwitter ebTwitter = new EbTwitter(this.ebEnt);
      stReturn = stReturn + ebTwitter.searchAndFollow(stValue.trim());
      this.stError += ebTwitter.getError();
    }
    else {
      stReturn = stReturn + "</form><form method=post><br>Twitter commands<br>L0: user  L1 # rpp L2: # of searches L3: search<br><textarea name='stInsert' cols=150 rows=20>mypetalert\n1\n1\npet\n</textarea><br><input type=submit name=sumbit8 value='Twitter'></form>";
    }

    return stReturn;
  }

  public String processSearchFollowers()
  {
    String stReturn = "";
    String stValue = this.ebEnt.ebUd.request.getParameter("stInsert");

    if ((stValue != null) && (stValue.length() > 0))
    {
      stReturn = stReturn + "<h1>Processing Twitter: search and follow</h1>";
      EbTwitter ebTwitter = new EbTwitter(this.ebEnt);
      String[] aCmd = stValue.trim().split("\n");
      stReturn = stReturn + ebTwitter.processSearchFollowers(aCmd[0].trim(), Integer.parseInt(aCmd[1].trim()), Integer.parseInt(aCmd[2].trim()));

      this.stError += ebTwitter.getError();
    }
    else {
      stReturn = stReturn + "</form><form method=post><br>processSearchFollowers<br>L0: user  L1 # iMaxSearchUsers L2: # of iMaxFolling <br><textarea name='stInsert' cols=150 rows=20>mypetalert\n1\n1\n</textarea><br><input type=submit name=sumbit8 value='Twitter'></form>";
    }

    return stReturn;
  }

  public String fixInsert()
  {
    String stReturn = "";
    String stValue = this.ebEnt.ebUd.request.getParameter("stInsert");
    String stSql = "";
    int iPos1 = -1;
    int iPos2 = -1;
    int iPos3 = 0;
    int iMax = -1;
    if (stValue != null && stValue.length() > 0)
    {
      /*ExecuteSql: insert into X25Communications (RecId,nmRefType,nmRefId,nmFlags,dtDate,nmOrigSize,stTitle,stContent,stHeader,stForeignRefId,stHtml)
      values(41575,42,5518,512,now(),9118,"Undelivered Mail Returned to Sender","This is the mail system at host s15344509.onlinehome-server.com.\r\n\r\nI\'m sorry to have to inform you that your message could not\r\nbe delivered to one or more recipients. It\'s attached below.\r\n\r\nFor further assistance, please send mail to \r\n\r\nIf you do so, please include this problem report. You can\r\ndelete your own text from the attached returned message.\r\n\r\n The mail system\r\n\r\n: host mx1.2by2.vsi.net[64.22.133.19] said: 550\r\n - mailbox is full (in reply to RCPT TO command)","From MAILER-DAEMON Thu Dec 24 10: 39:03 2009\nReturn-Path: \nX-Original-To: robe@myinfo.com\nDelivered-To: robe@myinfo.com\nReceived: from s15344509.onlinehome-server.com (unknown [127.0.0.1])\r\n by s15344509.onlinehome-server.com (Postfix) with ESMTP id E849A2B9F000E\r\n for ; Thu, 24 Dec 2009 16:39:03 +0000 (UTC)\nReceived: by s15344509.onlinehome-server.com (Postfix, from userid 110)\r\n id DC0542B9F0002; Thu, 24 Dec 2009 16:39:03 +0000 (UTC)\nX-Original-To: Rob@myinfo.com\nDelivered-To: rob@myinfo.com\nReceived: by s15344509.onlinehome-server.com (Postfix)\r\n id D35A52B9F000E; Thu, 24 Dec 2009 10:39:03 -0600 (CST)\nDate: Thu, 24 Dec 2009 10:39:03 -0600 (CST)\nFrom: MAILER-DAEMON@s15344509.onlinehome-server.com (Mail Delivery System)\nSubject: Undelivered Mail Returned to Sender\nTo: Rob@myinfo.com\nAuto-Submitted: auto-replied\nMIME-Version: 1.0\nContent-Type: multipart/report; report-type=delivery-status;\r\n boundary=\"3895B2B9F0002.1261672743/s15344509.onlinehome-server.com\"\nMessage-Id: <20091224163903.D35A52B9F000E@s15344509.onlinehome-server.com>\n","<20091224163903.D35A52B9F000E@s15344509.onlinehome-server.com>","")
      Exception: com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException: Duplicate entry '41575' for key 1
       */
      iMax = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from X25Communications");
      stReturn += "<br>Procesing Insert " + stValue.length();
      while ((iPos1 = stValue.indexOf("ExecuteSql: insert into X25Communications (RecId,", iPos3)) >= 0)
      {
        iPos2 = stValue.indexOf("values(", iPos1);
        iPos3 = stValue.indexOf("Exception: com.mysql.jdbc.", iPos1);
        if (iPos2 > iPos1 && iPos3 > iPos2)
        {
          stSql = stValue.substring(iPos1 + 12, iPos2 + 7);
          iMax++;
          stSql += iMax;
          int iPos4 = stValue.indexOf(",", iPos2 + 7);
          if (iPos4 > iPos2)
          {
            stSql += stValue.substring(iPos4, iPos3);
            this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
          } else
          {
            this.stError += "<BR>ERROR fixInsert: no comma found ";
            break;
          }

        }

      }
    } else
    {
      stReturn += "</form><form method=post><br>Copy Insert failures here<br>"
              + "<textarea name='stInsert' cols=150 rows=20></textarea>"
              + "</br><input type=submit name=sumbit8 value='Process mailq'></form>";
    }
    return stReturn;
  }

  public String processConfirm()
  {
    String stReturn = "<center><h1>Process Confirm replies</h1></center></form><form method=post><table border=1>";
    String[] astResult = null;
    try
    {
      astResult = this.ebEnt.ebUd.request.getParameterValues("usrs");
      if ((astResult != null) && (astResult.length > 0))
      {
        for (int iR = 0; iR < astResult.length; iR++)
        {
          this.ebEnt.dbEnterprise.ExecuteUpdate("update X25Communications com set nmFlags = 0x1000 where com.nmRefType=42 and ( stContent like '%http://www.spamarrest.com/%' or stContent like '%https://webmail.pas.earthlink.net/wam/addme?%' )and (nmFlags & 0x00000200) != 0 and com.nmRefId=" + astResult[iR]);
        }

      }
      else
      {
        stReturn = stReturn + listConfirm("http://www.spamarrest.com/");
        stReturn = stReturn + listConfirm("https://webmail.pas.earthlink.net/wam/addme?");

        stReturn = stReturn + "<tr><td colspan=4 align=center><input type=submit name=submit9 value='Save Marked Users'></td></tr>";
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<BR>ERROR processConfirm: " + e);
    }
    stReturn = stReturn + "</table></form>";
    return stReturn;
  }

  public String listConfirm(String stSearch)
  {
    String stEmail = "";
    String stUrl = "";
    String stTemp = "";
    String stSql = "";
    int iPos1 = -1;
    int iPos2 = -1;
    String stReturn = "";
    try
    {
      stSql = "SELECT u.stEMail, com.* FROM X25Communications com, X25User u where com.nmRefType=42 and com.nmRefId=u.RecId and stContent like '%" + stSearch + "%'" + "and (nmFlags & 0x00000200) != 0 order by stEMail, com.RecId desc";

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        String stLast = "";
        for (int iC = 1; iC <= iMax; iC++)
        {
          rs.absolute(iC);
          stEmail = rs.getString("stEMail");
          if (!stLast.equals(stEmail))
          {
            stReturn = stReturn + "<tr>";
            stReturn = stReturn + "<td>" + stEmail + "</td>";
            stReturn = stReturn + "<td><input type=checkbox name=usrs value='" + rs.getString("nmRefId") + "'></td>";
            stUrl = "";
            iPos1 = rs.getString("stContent").indexOf(stSearch);
            if (iPos1 >= 0)
            {
              iPos2 = rs.getString("stContent").indexOf("\n", iPos1);
              if (iPos2 > iPos1)
              {
                stUrl = rs.getString("stContent").substring(iPos1, iPos2);
              }
              else {
                stUrl = rs.getString("stContent").substring(iPos1);
              }
            }
            stReturn = stReturn + "<td><a href=\"" + stUrl + "\" target=_blank>confirm</a></td>";
            stTemp = rs.getString("stContent").replace("<", "&lt;");
            if (stTemp.length() > 300)
            {
              stTemp = stTemp.substring(0, 300);
            }
            stReturn = stReturn + "<td>" + stTemp + "</td>";
            stReturn = stReturn + "</tr>";
          }
          stLast = stEmail;
        }
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR listConfirm: " + e);
    }

    return stReturn;
  }

  public String fixUndeliverable()
  {
    String stReturn = "<br><h2>fixUndeliverable</h2>";
    String stSql = "";

    ResultSet rs = null;
    try
    {
      stSql = "SELECT u.stEMail, com.* FROM X25Communications com, X25User u where com.nmRefType=42 and com.nmRefId=u.RecId and stTitle in( 'Returned mail: see transcript for details', 'failure notice','Mail System Error - Returned Mail','Delivery Status Notification (Failure)','Undelivered Mail Returned to Sender','Undelivered mail: User unknown') and (nmFlags & 0x00000200) != 0 ";

      rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);

      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        for (int iR = 1; iR <= iMax; iR++)
        {
          rs.absolute(iR);
          stReturn = stReturn + markEmail(rs.getString("stContent"), rs.getInt("RecId"));
        }
        rs.close();
      }
      stSql = "SELECT u.stEMail, com.* FROM X25Communications com, X25User u where com.nmRefType=42 and com.nmRefId=u.RecId and stTitle like 'Undeliverable:%' and (nmFlags & 0x00000200) != 0 ";

      rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);

      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        for (int iR = 1; iR <= iMax; iR++)
        {
          rs.absolute(iR);
          stReturn = stReturn + markEmail(rs.getString("stContent"), rs.getInt("RecId"));
        }
        rs.close();
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR fixUndeliverable: [" + stSql + "] " + e);
    }
    return stReturn;
  }

  public String markEmail(String stContent, int iRecId)
  {
    String stReturn = "";
    String stLine = "";
    String stEmail = "";
    int iCount = 0;
    int iPos1 = -1;
    int iPos2 = -1;
    int iPos3 = -1;

    if (stContent != null)
    {
      String[] aLines = stContent.trim().split("\n");
      for (int iL = 0; iL < aLines.length; iL++)
      {
        stLine = aLines[iL].trim();
        iPos1 = stLine.indexOf("<");
        if (iPos1 != 0)
          continue;
        iPos2 = stLine.indexOf("@");
        iPos3 = stLine.indexOf(">");
        if ((iPos2 <= iPos1) || (iPos3 <= iPos2) || (iPos3 - iPos1 >= 255))
          continue;
        stEmail = stLine.substring(iPos1 + 1, iPos3);
        if (!EbStatic.isEmail(stEmail))
          continue;
        iCount++;
        int i = incEmailError(stEmail);
        stReturn = stReturn + "<br>Marking: " + stEmail + " count: " + i;
        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25Communications set nmFlags=0x100 where RecId=" + iRecId);
      }

      if (iCount == 0)
      {
        iPos1 = stContent.indexOf("to <");
        if (iPos1 <= 0)
        {
          iPos1 = stContent.indexOf("for <");
        }
        if (iPos1 >= 0)
        {
          iPos2 = stContent.indexOf("@", iPos1);
          iPos3 = stContent.indexOf(">", iPos1);
          if ((iPos2 > iPos1) && (iPos3 > iPos2) && (iPos3 - iPos1 < 255))
          {
            stEmail = stContent.substring(iPos1 + 1, iPos3);
            if (EbStatic.isEmail(stEmail))
            {
              iCount++;
              int i = incEmailError(stEmail);
              stReturn = stReturn + "<br>Marking: " + stEmail + " count: " + i;
              this.ebEnt.dbEnterprise.ExecuteUpdate("update X25Communications set nmFlags=0x100 where RecId=" + iRecId);
            }
          }
        }
        if (iCount == 0)
        {
          if (stContent == null)
          {
            stContent = "";
          }
          stReturn = stReturn + "<br>ERROR: Cannot find email for com.RecId=" + iRecId + " content: " + stContent.replace("<", "&lt;");
          this.ebEnt.dbEnterprise.ExecuteUpdate("update X25Communications set nmFlags=0x100 where RecId=" + iRecId);
        }
      }
    }
    return stReturn;
  }

  public int incEmailError(String stEmail)
  {
    int nmErrorCount = -1;

    if (EbStatic.isEmail(stEmail) == true)
    {
      int nmUserId = this.ebEnt.dbEnterprise.ExecuteSql1n("select RecId from X25User where stEMail=" + this.ebEnt.dbEnterprise.fmtDbString(stEmail));

      if (nmUserId > 0)
      {
        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25User set nmErrorCount=(nmErrorCount+1) where RecId=" + nmUserId);
        nmErrorCount = this.ebEnt.dbEnterprise.ExecuteSql1n("select nmErrorCount from X25User where RecId=" + nmUserId);

        if (nmErrorCount > 3)
        {
          this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25DoNotCall (stValue,nmFlags,dtEntered) values(" + this.ebEnt.dbEnterprise.fmtDbString(stEmail) + ",2,now())");
        }
      }

    }

    return nmErrorCount;
  }

  public String processFiles()
  {
    String stMessage = "";
    String stReturn = "</form><form method=post><table>";
    String stValue = "";
    try
    {
      String stSql = "SELECT u.nmMyTwitterId,u.stTwitterId,u.stPassword, u.nmFollowers, u.nmFollowing, TIMESTAMPDIFF(SECOND,dtLimit,now()) as nmLimit FROM MyTwitterUser u where stUserType='M' order by u.nmMyTwitterId";

      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        stValue = this.ebEnt.ebUd.request.getParameter("submit9");

        if ((stValue != null) && (stValue.length() > 0))
        {
          stReturn = stReturn + "<tr><th colspan=2><h1>Processing Send Twitter</h1></th></tr>";
          int iT = 1;
          for (; iT <= iMax; iT++)
          {
            rs.absolute(iT);
            stMessage = this.ebEnt.ebUd.request.getParameter("t_" + rs.getString("stTwitterId"));

            if ((stMessage == null) || (stMessage.length() <= 0))
              continue;
            stReturn = stReturn + "<tr>";
            stReturn = stReturn + "<td>" + rs.getString("stTwitterId") + "</td>";

            stReturn = stReturn + "<td colspan=2><b>" + stMessage + "</b> ";

            stReturn = stReturn + "aaaaa TODO";

            stReturn = stReturn + "</td>";

            stReturn = stReturn + "</tr>";
          }

        }

        stReturn = stReturn + "<tr><th colspan=2><h1>Send Twitter</h1></th></tr>";
        int iT = 1;
        for (; iT <= iMax; iT++)
        {
          rs.absolute(iT);
          stReturn = stReturn + "<tr>";

          stReturn = stReturn + "<td>" + rs.getString("nmFollowers") + "</td>";

          stReturn = stReturn + "<td>" + rs.getString("stTwitterId") + "</td>";

          if (rs.getInt("nmLimit") <= 0)
          {
            stReturn = stReturn + "<td><input type=text name='t_" + rs.getString("stTwitterId") + "' value='' size=141 MAXLENGTH =140></td>";
          }
          else {
            stReturn = stReturn + "<td>Blocked ID</td>";
          }
          stReturn = stReturn + "</tr>";
        }

        stReturn = stReturn + "<tr><th colspan=2><input type=submit name=submit9 value='Send Twitter'></th></tr>";
      }
      else
      {
        stReturn = stReturn + "tr><th colspan=2>ERROR: no twitter accounts</th></tr>";
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR processSendTwitter: " + e);
    }

    stReturn = stReturn + "</table></form>";
    return stReturn;
  }

  public String processEps7()
  {
    String stFix = "Cancel_20_36,Delete_20_36,Insert_20_36,Project_Manager_20_3,Project_Name_20_3,Update_20_36";
    String stReturn = "";
    return stReturn;
  }

  public String processEps8()
  {
    String stReturn = "";
    try
    {
      String stSql = "SELECT * FROM teb_fields order by nmGrouping, nmFieldFlags";
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      int iMax = rs.getRow();
      int nmGrouping = -1;
      int nmFieldFlags = -1;
      for (int iR = 1; iR <= iMax; iR++)
      {
        rs.absolute(iR);
        if (nmGrouping != rs.getInt("nmGrouping"))
        {
          nmGrouping = rs.getInt("nmGrouping");
          nmFieldFlags = -1;
        }
        nmFieldFlags++;
        if (nmFieldFlags == rs.getInt("nmFieldFlags"))
          continue;
        stSql = "update teb_fields set nmFieldFlags=" + nmFieldFlags + " where stDbFieldName=\"" + rs.getString("stDbFieldName") + "\";";
        this.ebEnt.dbDyn.ExecuteUpdate(stSql);
        stReturn = stReturn + "<BR>" + stSql;
      }
    }
    catch (Exception e)
    {
    }
    return stReturn;
  }

  public String processEps8b()
  {
    String stReturn = "";
    try
    {
      String stSql = "SELECT stDbFieldName,stResponseStructure FROM teb_epsfields where stResponseStructure != '' and stResponseStructure is not null and stResponseStructure != 'null' and stResponseStructure not like '%bitMask%' and stResponseStructure not like '%privReports%' ";

      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      int iMax = rs.getRow();
      for (int iR = 1; iR <= iMax; iR++)
      {
        rs.absolute(iR);
        String[] aV = rs.getString("stResponseStructure").split("\\|");
        String stNew = aV[0] + "|" + aV[1];
        for (int iV = 2; iV < aV.length; iV += 2)
        {
          stNew = stNew + "|" + aV[(iV + 0)] + "|";
          try
          {
            ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql("select * from dbeps2.teb_fields where stDbFieldName=\"" + aV[(iV + 1)] + "\" ");

            rs1.absolute(1);
            String[] v = rs1.getString("stDbFieldName").split("_");
            String stNewField = "";
            for (int ii = 0; ii < v.length; ii++)
            {
              if (ii + 1 < v.length)
              {
                if (ii > 0)
                {
                  stNewField = stNewField + "_";
                }
                stNewField = stNewField + v[ii];
              }
              else
              {
                try {
                  int iNr = Integer.parseInt(v[ii]);
                  if (iNr <= 0)
                  {
                    if (ii > 0)
                    {
                      stNewField = stNewField + "_";
                    }
                    stNewField = stNewField + v[ii];
                  }
                }
                catch (Exception e) {
                  if (ii > 0)
                  {
                    stNewField = stNewField + "_";
                  }
                  stNewField = stNewField + v[ii];
                }
              }
            }
            stNewField = stNewField + "_" + rs1.getString("nmGrouping") + "_" + rs1.getString("nmDataType");
            if (aV[(iV + 1)].equals("Locations_484"))
            {
              stNewField = "Selected_Locations_40_3";
            }
            if (aV[(iV + 1)].equals("Labor_Categories_97"))
            {
              stNewField = "Labor_Categories_14_3";
            }

            int nmForeignId = this.ebEnt.dbDyn.ExecuteSql1n("select nmForeignId from teb_fields where stDbFieldName=\"" + stNewField + "\"");

            stNew = stNew + stNewField;
            if (nmForeignId <= 0)
            {
              this.stError = (this.stError + "<BR>NOT FOUND stResponseStructure: [" + aV[(iV + 1)] + "] " + stNewField);
            }
          }
          catch (Exception e) {
          }
        }
        this.ebEnt.dbDyn.ExecuteUpdate("update teb_epsfields set stResponseStructure=\"" + stNew + "\" " + "where stDbFieldName=\"" + rs.getString("stDbFieldName") + "\" ");
      }

      stSql = "SELECT stDbFieldName,stRequestStructure FROM teb_epsfields where stRequestStructure like 'event|%'";
      rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      iMax = rs.getRow();
      for (int iR = 1; iR <= iMax; iR++)
      {
        rs.absolute(iR);
        String[] aV = rs.getString("stRequestStructure").split("\\|");
        String stNew = aV[0] + "|" + aV[1];
        for (int iV = 2; iV < aV.length; iV += 2)
        {
          stNew = stNew + "|" + aV[(iV + 0)] + "|";
          try
          {
            ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql("select * from dbeps2.teb_fields where stDbFieldName=\"" + aV[(iV + 1)] + "\" ");

            rs1.absolute(1);
            String[] v = rs1.getString("stDbFieldName").split("_");
            String stNewField = "";
            for (int ii = 0; ii < v.length; ii++)
            {
              if (ii + 1 < v.length)
              {
                if (ii > 0)
                {
                  stNewField = stNewField + "_";
                }
                stNewField = stNewField + v[ii];
              }
              else
              {
                try {
                  int iNr = Integer.parseInt(v[ii]);
                  if (iNr <= 0)
                  {
                    if (ii > 0)
                    {
                      stNewField = stNewField + "_";
                    }
                    stNewField = stNewField + v[ii];
                  }
                }
                catch (Exception e) {
                  if (ii > 0)
                  {
                    stNewField = stNewField + "_";
                  }
                  stNewField = stNewField + v[ii];
                }
              }
            }
            stNewField = stNewField + "_" + rs1.getString("nmGrouping") + "_" + rs1.getString("nmDataType");
            if (aV[(iV + 1)].equals("Locations_484"))
            {
              stNewField = "Selected_Locations_40_3";
            }
            if (aV[(iV + 1)].equals("Labor_Categories_97"))
            {
              stNewField = "Labor_Categories_14_3";
            }

            int nmForeignId = this.ebEnt.dbDyn.ExecuteSql1n("select nmForeignId from teb_fields where stDbFieldName=\"" + stNewField + "\"");

            stNew = stNew + stNewField;
            if (nmForeignId <= 0)
            {
              this.stError = (this.stError + "<BR>NOT FOUND: [" + aV[(iV + 1)] + "] " + stNewField);
            }
          }
          catch (Exception e) {
          }
        }
        this.ebEnt.dbDyn.ExecuteUpdate("update teb_epsfields set stRequestStructure=\"" + stNew + "\" " + "where stDbFieldName=\"" + rs.getString("stDbFieldName") + "\" ");
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<BR>ERROR: processEps8 " + e);
    }
    return stReturn;
  }

  public static int[] getEpsDataType(String stDt)
  {
    int[] aiReturn = new int[3];

    aiReturn[0] = 0;
    aiReturn[1] = 0;
    aiReturn[2] = 128;

    if ((stDt.equals("Drop-Down List")) || (stDt.equals("Drop Down List")) || (stDt.equals("Check Boxes")))
    {
      aiReturn[0] = 9;
    } else if (stDt.equals("Menu Command"))
    {
      aiReturn[0] = 38;
    } else if (stDt.equals("Button"))
    {
      aiReturn[0] = 36;
    } else if (stDt.equals("Text Field"))
    {
      aiReturn[0] = 3;
    } else if (stDt.equals("Password"))
    {
      aiReturn[0] = 3;
      aiReturn[1] = 536870912;
    } else if (stDt.equals("Check Box"))
    {
      aiReturn[0] = 9;
      aiReturn[1] = 32;
    } else if (stDt.equals("Multi Choice"))
    {
      aiReturn[0] = 9;
      aiReturn[1] = 128;
    } else if (stDt.equals("Money Field"))
    {
      aiReturn[0] = 5;
    } else if (stDt.equals("Fraction"))
    {
      aiReturn[0] = 3;
      aiReturn[1] = 1073741824;
      aiReturn[2] = 10;
    } else if (stDt.equals("Number"))
    {
      aiReturn[0] = 31;
    } else if ((stDt.equals("Radio Button")) || (stDt.equals("Radio Buttons")))
    {
      aiReturn[0] = 37;
    } else if (stDt.equals("Indicator"))
    {
      aiReturn[0] = 3;
    }

    return aiReturn;
  }
}