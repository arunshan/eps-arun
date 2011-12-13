package com.eps.model;
//Additional checks
//SELECT * FROM Schedule where SchLaborCategories != '' and (SchFlags & 0x10) = 0;
//SELECT * FROM Schedule where SchLaborCategories = '' and (SchFlags & 0x10) != 0;
//SELECT * FROM Schedule where SchDependencies = '' and (SchFlags & 0x10) != 0; Starting poing
//SELECT * FROM Calendar where dtDay >= curdate() and dtDay < ADDDATE(curdate(), INTERVAL 10 year) and (nmDivision=1 or nmUser=123) and (nmFlags&1) != 0;

/**
 *
 * @author Robert Eder
 */
import com.ederbase.model.EbEnterprise;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

class EpsXlsProject //extends EpsUserData
{

  protected ResultSet rsProject = null;
  protected int nmBaseline = 0;
  protected EbEnterprise ebEnt = null;
  protected String stError = "";
  private EpsUserData epsUd = null;
  private int iSubmit = 0;
  private ResultSet rsFields = null;
  private int iMaxFields = 0;
  public String stPk = "";
  private String stGoBack = "";
  private int iAnalyzeStatus = 0;
  private long startTime = 0;
  private int iLastSqlCount = 0;
  private EpsCriticalPath[] aCp = null;
  private int[][] aaPath = null;
  int iMaxPath = 0;
  int iPathPosition = 0;
  private String stCommTrace = null;
  private String stHolidays = "";
  private int[] aWeekend = null;
  private Calendar dtProjectStart = null;
  private int iCpIndexByDate = -1;
  private int iMaxRecId = 0;

  public void EpsXlsProject()
  {
    this.ebEnt = null;
  }

  public void setEpsXlsProject(EbEnterprise ebEnt, EpsUserData epsUd)
  {
    this.ebEnt = ebEnt;
    this.epsUd = epsUd;
    this.startTime = System.nanoTime();
  }

  public String getError()
  {
    return this.stError;
  }

  public String xlsRequirementsEdit(String stChild)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    String stSql = "";
    int iEditId = 0;
    String stTemp = "";
    try
    {
      stPk = this.ebEnt.ebUd.request.getParameter("pk");
      stGoBack = "?stAction=projects&t=12";
      String stLink = "?stAction=projects&t=" + this.ebEnt.ebUd.request.getParameter("t") + "&do=xls&pk=" + stPk + "&parent=" + stTemp + "&child=" + stChild;
      int iFrom = 0;
      int iBlock = this.epsUd.rsMyDiv.getInt("ReqSchRows");
      this.rsProject = this.ebEnt.dbDyn.ExecuteSql("select * from Projects where RecId=" + stPk);
      this.rsProject.absolute(1);
      nmBaseline = this.rsProject.getInt("CurrentBaseline");
      stTemp = this.ebEnt.ebUd.request.getParameter("from");
      if (stTemp != null && stTemp.length() > 0)
      {
        iFrom = Integer.parseInt(stTemp);
      }
      stLink += "&from=" + iFrom;
      stTemp = this.ebEnt.ebUd.request.getParameter("parent");
      if (stTemp == null || stTemp.length() <= 0)
        stTemp = this.ebEnt.ebUd.request.getParameter("t");
      String stSave = this.ebEnt.ebUd.request.getParameter("savedataxls");
      if (stSave != null && stSave.length() > 0)
      {
        if (!stSave.equals("Cancel"))
        {
          saveInline(stChild, stPk);
        }
        this.ebEnt.ebUd.setRedirect(stLink + "#row" + this.ebEnt.ebUd.request.getParameter("edit"));
        return ""; // redirect, to remove EDIT tag ----------------------------->
      }
      String stAction = this.ebEnt.ebUd.request.getParameter("a");
      String stR = this.ebEnt.ebUd.request.getParameter("r");
      if (stAction != null && stAction.length() > 0 && stR != null && stR.length() > 0)
      { // PROCESS ACTIONS
        int iRecIdDo = Integer.parseInt(stR);
        ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql("select * from Requirements where RecId=" + iRecIdDo + " and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
        rs1.absolute(1);
        if (stAction.equals("collapse"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqFlags = ( ReqFlags | 2) where RecId= " + iRecIdDo + " and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
          this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqFlags = ( ReqFlags | 1) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
            + "and ReqId > " + rs1.getString("ReqId") + " and ReqId < " + getEnd(stChild, stPk, rs1));
        } else if (stAction.equals("expand"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqFlags = (ReqFlags & ~0x3) where RecId= " + iRecIdDo + " and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
          this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqFlags = (ReqFlags & ~0x3) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
            + "and ReqId > " + rs1.getString("ReqId") + " and ReqId < " + getEnd(stChild, stPk, rs1));
        } else if (stAction.equals("insert"))
        {
          int iRecMax = this.ebEnt.dbDyn.ExecuteSql1n("select max(RecId) from Requirements");
          iRecMax++;
          String stWhat = this.ebEnt.ebUd.request.getParameter("what");
          try
          {
            iEditId = rs1.getInt("ReqId");
            if (stWhat.equals("below"))
            {
              iEditId++;
              this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqId=(ReqId+1) where ReqId > " + rs1.getString("ReqId") + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " ");
            } else
            {
              // Above
              this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqId=(ReqId+1) where ReqId >= " + rs1.getString("ReqId") + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " ");
            }
            this.ebEnt.dbDyn.ExecuteUpdate("insert into Requirements (RecId,nmProjectId,ReqId,nmBaseline,ReqLevel) "
              + "values(" + iRecMax + "," + stPk + "," + iEditId + "," + nmBaseline + "," + rs1.getInt("ReqLevel") + ")");
          } catch (Exception e)
          {
            iEditId = 1;
            this.ebEnt.dbDyn.ExecuteUpdate("insert into Requirements (RecId,nmProjectId,ReqId,nmBaseline,ReqLevel) "
              + "values(" + iRecMax + "," + stPk + "," + iEditId + "," + nmBaseline + ",0)");
          }
          this.epsUd.makeTask(6, this.epsUd.epsEf.getRequirementName("" + iRecIdDo, stPk, nmBaseline)); // Inserted Requirement
          this.ebEnt.ebUd.setRedirect(stLink + "&edit=" + iRecMax + "&new=y#row" + iEditId);
          return ""; //----------------------------->
        } else if (stAction.equals("delete"))
        {
          this.epsUd.makeTask(4, this.epsUd.epsEf.getRequirementName("" + iRecIdDo, stPk, nmBaseline)); // 4	1	Deleted Requirement	Enabled	Yes
          this.ebEnt.dbDyn.ExecuteUpdate("delete from Requirements where RecId=" + iRecIdDo + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
          String stWhat = this.ebEnt.ebUd.request.getParameter("what");
          int iDel = 1;
          if (stWhat.equals("children"))
          {
            int iReqIdEnd = getEnd(stChild, stPk, rs1);
            this.ebEnt.dbDyn.ExecuteUpdate("delete from Requirements where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
              + "and ReqId > " + rs1.getString("ReqId") + " and ReqId < " + iReqIdEnd);
            iDel += (iReqIdEnd - rs1.getInt("ReqId") - 1);
          } else
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqLevel=(ReqLevel-1) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
              + "and ReqId > " + rs1.getString("ReqId") + " and ReqId < " + getEnd(stChild, stPk, rs1));
          }
          this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqId=(ReqId-" + iDel + ") where ReqId > " + rs1.getString("ReqId") + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
            + "and ReqId > " + rs1.getString("ReqId"));
        } else if (stAction.equals("demote"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqLevel=(ReqLevel-1) where RecId=" + iRecIdDo + " and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
          String stWhat = this.ebEnt.ebUd.request.getParameter("what");
          if (stWhat.equals("children"))
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqLevel=(ReqLevel-1) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
              + "and ReqId > " + rs1.getString("ReqId") + " and ReqId < " + getEnd(stChild, stPk, rs1));
          }
        } else if (stAction.equals("promote"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqLevel=(ReqLevel+1) where RecId=" + iRecIdDo + " and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
          String stWhat = this.ebEnt.ebUd.request.getParameter("what");
          if (stWhat.equals("children"))
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqLevel=(ReqLevel+1) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
              + "and ReqId > " + rs1.getString("ReqId") + " and ReqId < " + getEnd(stChild, stPk, rs1));
          }
        } else if (stAction.equals("customize"))
        {
          stTemp = this.ebEnt.ebUd.request.getParameter("savedata");
          String stC = getCustomize(stChild, stLink, stPk);
          if (stTemp == null || stTemp.length() <= 0)
            return stC;
        } else if (stAction.equals("custom"))
        {
          String stB = this.ebEnt.ebUd.request.getParameter("b");
          if (stB != null)
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqFlags = (ReqFlags & ~0x3) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + "");
            try
            {
              int iLevel = Integer.parseInt(stB);
              this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqFlags = ( ReqFlags | 1) "
                + "where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and ReqLevel > " + iLevel);
            } catch (Exception e)
            {
            }
            setFlags(stChild);
          }
        } else if (stAction.equals("editfull"))
        {
          stTemp = editFull(stChild, stR);
          if (stTemp != null && stTemp.length() > 0)
            return stTemp;
        } else if (stAction.equals("map"))
        {
          stTemp = doMap(stChild, stR);
          if (stTemp != null && stTemp.length() > 0)
            return stTemp;
        }
      }
      String stHeader = xlsHeaders(stChild);
      String s1 = "</form><form method=post name='form" + stChild + "' id='form" + stChild + "' onsubmit='return myValidation(this)'>"
        + "<input type=hidden name=from value='" + iFrom + "'><table class=l1table><tr><td class=l1td colspan=" + iMaxFields + " align=center><h2>"
        + this.rsProject.getString("ProjectName") + "</h2></td></tr>";
      sbReturn.append(s1);
      String stEdit = this.ebEnt.ebUd.request.getParameter("edit");
      if (stEdit != null && stEdit.length() > 0)
        iEditId = Integer.parseInt(stEdit);
      else
        iEditId = -2;
      
      int iMaxRecords = this.ebEnt.dbDyn.ExecuteSql1n("select count(*) from Requirements where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and (ReqFlags & 0x1) = 0");
      stSql = "select * from Requirements where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and (ReqFlags & 0x1) =0 order by ReqId,RecId desc limit " + iFrom + "," + iBlock;
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      int iMax = rs.getRow();
      if (iMax <= 0) // Fix query, if it does not return any rows ... otherwise we auto insert
      {
        iFrom = 0; // reset to 0
        rs.close();
        stSql = "select * from Requirements where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and (ReqFlags & 0x1) != 1 order by ReqId,RecId desc limit " + iFrom + "," + iBlock;
        rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
        rs.last();
        iMax = rs.getRow();
        if (iMax <= 0)
        { // Clear all collapsed flags -- do EXPAND ALL
          this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqFlags=(ReqFlags & ~0x3) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + "");
          rs.close();
          stSql = "select * from Requirements where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and (ReqFlags & 0x1) != 1 order by ReqId,RecId desc limit " + iFrom + "," + iBlock;
          rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
          rs.last();
          iMax = rs.getRow();
        }
      }
      if (iMax > 0)
      {
        sbReturn.append(stHeader);
        String stClass = "";
        int iPrevLevel = -1;
        String stId = "";
        String stNm = "";
        for (int iR = 1; iR <= iMax; iR++)
        {
          int iNextLevel = -1;
          int iNextShow = 0;
          if (iR < iMax)
          {
            rs.absolute(iR + 1);
            iNextLevel = rs.getInt("ReqLevel");
            if ((rs.getInt("ReqFlags") & 0x1) == 0)
              iNextShow = 1; // rs.getInt("nmShow");
            else
              iNextShow = 0;
          } else
          {
            iNextShow = 1; // At the end ... must enable insert below
          }
          rs.absolute(iR);
          if ((rs.getInt("ReqFlags") & 0x1) == 1)
            continue;
          //  + " onmouseout='timer(2)'>";
          if ((iR & 1) != 0)
            stClass = " class=l1td ";
          else
            stClass = " class=l1td2 ";
          stId = " id=r" + rs.getInt("RecId") + "_";
          stNm = " name=r" + rs.getInt("RecId") + "_";
          String stDescription = rs.getString("ReqDescription");
          if (stDescription == null)
            stDescription = "";
          else
            stDescription = stDescription.trim();
          if (iEditId <= 0 && (this.ebEnt.ebUd.getLoginPersonFlags() & 0xA0) != 0) // Only BA and PPM
          {
            //onmouseover="
            // + "\"setActions(1," + rs.getInt("RecId") + "," + rs.getInt("ReqId") + "," + rs.getInt("ReqLevel") + "," + iNextLevel + "," + iPrevLevel + "," + iNextShow + ",'" + stLink + "' )\""
            s1 = "<tr  "
              + " onClick="
              + "\"setActionsClick(1," + rs.getInt("RecId") + "," + rs.getInt("ReqId") + "," + rs.getInt("ReqFlags") + "," + rs.getInt("ReqLevel") + "," + iNextLevel + "," + iPrevLevel + "," + iNextShow + ",'" + stLink + "'," + iMaxFields + " )\""
              + " >";
            sbReturn.append(s1);
          } else
          {
            sbReturn.append("<tr>");
          }
          for (int iF = 1; iF <= this.iMaxFields; iF++)
          {
            this.rsFields.absolute(iF);
            sbReturn.append("<td ");
            sbReturn.append(stClass);
            sbReturn.append(stId);
            sbReturn.append(iF);
            sbReturn.append(stNm);
            sbReturn.append(iF);
            sbReturn.append(" ");
            sbReturn.append(getStyle(0));
            if(rsFields.getInt("nmDataType") == 3 || rsFields.getInt("nmDataType") == 4)
	       		 sbReturn.append(" align='left' ");	//align text left
            sbReturn.append(">");
            
            if (iF == 1)
            {
              sbReturn.append("<a name='row");
              sbReturn.append(rs.getString("ReqId"));
              sbReturn.append("'></a>");
            }
            String stValue = rs.getString(rsFields.getString("stDbFieldName"));
            if (stValue == null)
              stValue = "";
            if (this.rsFields.getInt("nmDataType") == 5 && stValue.length() > 0)
              sbReturn.append(this.epsUd.rsMyDiv.getString("stMoneySymbol") + " ");

            if (iEditId == rs.getInt("RecId") && (rsFields.getInt("nmFlags") & 0x1) != 0)
            {
            	if (rsFields.getString("stDbFieldName").equals("ReqLevel")){
            		sbReturn.append(editXlsField(stValue));
            	}else{
            		sbReturn.append(editXlsField(stValue));
            	}
            } else
            {
              if (rsFields.getString("stDbFieldName").equals("ReqTitle"))
                for (int i = 0; i < rs.getInt("ReqLevel"); i++)
                  sbReturn.append("&nbsp;&nbsp;");
              else if(rsFields.getString("stDbFieldName").equals("ReqCost")){
            	  DecimalFormat df = new DecimalFormat("#,###,###,##0.00");
            	  stValue = df.format(Double.parseDouble(stValue));
              }
              sbReturn.append(stValue.replace("\n", "<BR>"));
            }
          }
          sbReturn.append("</tr>");
          iPrevLevel = rs.getInt("ReqLevel");
        }
        sbReturn.append(doNext(stChild, iMaxFields, iMaxRecords, iFrom, iBlock, stGoBack, stPk));
      } else
      {
        sbReturn.append(doNext(stChild, iMaxFields, 0, 0, iBlock, stGoBack, stPk));
      }
      sbReturn.append("</table></form><br/>");
    } catch (Exception e)
    {
      this.stError += "<br>ERROR xlsRequirementsEdit: " + e;
    }
    return sbReturn.toString();
  }

  public String xlsTestEdit(String stChild)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    String stSql = "";
    int iEditId = 0;
    String stTemp = "";
    try
    {
      stPk = this.ebEnt.ebUd.request.getParameter("pk");
      stGoBack = "?stAction=projects&t=12";
      String stLink = "?stAction=projects&t=" + this.ebEnt.ebUd.request.getParameter("t") + "&do=xls&pk=" + stPk + "&parent=" + stTemp + "&child=" + stChild;
      int iFrom = 0;
      int iBlock = this.epsUd.rsMyDiv.getInt("ReqSchRows");
      this.rsProject = this.ebEnt.dbDyn.ExecuteSql("select * from Projects where RecId=" + stPk);
      this.rsProject.absolute(1);
      nmBaseline = this.rsProject.getInt("CurrentBaseline");
      stTemp = this.ebEnt.ebUd.request.getParameter("from");
      if (stTemp != null && stTemp.length() > 0)
      {
        iFrom = Integer.parseInt(stTemp);
      }
      stLink += "&from=" + iFrom;
      stTemp = this.ebEnt.ebUd.request.getParameter("parent");
      if (stTemp == null || stTemp.length() <= 0)
        stTemp = this.ebEnt.ebUd.request.getParameter("t");
      String stSave = this.ebEnt.ebUd.request.getParameter("savedataxls");
      if (stSave != null && stSave.length() > 0)
      {
        if (!stSave.equals("Cancel"))
        {
          saveInline(stChild, stPk);
        }
        this.ebEnt.ebUd.setRedirect(stLink + "#row" + this.ebEnt.ebUd.request.getParameter("edit"));
        return ""; // redirect, to remove EDIT tag ----------------------------->
      }
      String stAction = this.ebEnt.ebUd.request.getParameter("a");
      String stR = this.ebEnt.ebUd.request.getParameter("r");
      if (stAction != null && stAction.length() > 0 && stR != null && stR.length() > 0)
      { // PROCESS ACTIONS
        int iRecIdDo = Integer.parseInt(stR);
        ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql("select * from Test where RecId=" + iRecIdDo + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
        rs1.absolute(1);
        if (stAction.equals("collapse"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstFlags=2 where RecId= " + iRecIdDo + " and nmBaseline=" + this.nmBaseline);
          this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstFlags=1 where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
            + "and TstId > " + rs1.getString("TstId") + " and TstId < " + getEnd(stChild, stPk, rs1));
        } else if (stAction.equals("expand"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstFlags=0 where RecId= " + iRecIdDo + " and nmBaseline=" + this.nmBaseline);
          this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstFlags=0 where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
            + "and TstId > " + rs1.getString("TstId") + " and TstId < " + getEnd(stChild, stPk, rs1));
        } else if (stAction.equals("insert"))
        {
          int iRecMax = this.ebEnt.dbDyn.ExecuteSql1n("select max(RecId) from Test");
          iRecMax++;
          String stWhat = this.ebEnt.ebUd.request.getParameter("what");
          try
          {
            iEditId = rs1.getInt("TstId");
            if (stWhat.equals("below"))
            {
              iEditId++;
              this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstId=(TstId+1) where TstId > " + rs1.getString("TstId") + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " ");
            } else
            {
              // Above
              this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstId=(TstId+1) where TstId >= " + rs1.getString("TstId") + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " ");
            }
            this.ebEnt.dbDyn.ExecuteUpdate("insert into Test (RecId,nmProjectId,TstId,nmBaseline,TstLevel) "
              + "values(" + iRecMax + "," + stPk + "," + iEditId + "," + rs1.getInt("nmBaseline") + "," + rs1.getInt("TstLevel") + ")");
          } catch (Exception e)
          {
            iEditId = 1;
            this.ebEnt.dbDyn.ExecuteUpdate("insert into Test (RecId,nmProjectId,TstId,nmBaseline,TstLevel) "
              + "values(" + iRecMax + "," + stPk + ",1," + this.nmBaseline + ",0)");
          }
          //this.ebEnt.ebUd.setRedirect(stLink + "&edit=" + iRecMax + "&new=y#row" + iEditId);
          //?stAction=projects&t=12&do=xls&pk=5&parent=&child=21&from=0&a=editfull&r=8#row8
          this.ebEnt.ebUd.setRedirect(stLink + "&a=editfull&r=" + iRecMax + "#row" + iEditId);
          return ""; //----------------------------->
        } else if (stAction.equals("delete"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("delete from Test where RecId=" + iRecIdDo + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
          String stWhat = this.ebEnt.ebUd.request.getParameter("what");
          int iDel = 1;
          if (stWhat.equals("children"))
          {
            int iTstIdEnd = getEnd(stChild, stPk, rs1);
            this.ebEnt.dbDyn.ExecuteUpdate("delete from Test where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
              + "and TstId > " + rs1.getString("TstId") + " and TstId < " + iTstIdEnd);
            iDel += (iTstIdEnd - rs1.getInt("TstId") - 1);
          } else
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstLevel=(TstLevel-1) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
              + "and TstId > " + rs1.getString("TstId") + " and TstId < " + getEnd(stChild, stPk, rs1));
          }
          this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstId=(TstId-" + iDel + ") where TstId > " + rs1.getString("TstId") + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
            + "and TstId > " + rs1.getString("TstId"));
        } else if (stAction.equals("demote"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstLevel=(TstLevel-1) where RecId=" + iRecIdDo);
          String stWhat = this.ebEnt.ebUd.request.getParameter("what");
          if (stWhat.equals("children"))
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstLevel=(TstLevel-1) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
              + "and TstId > " + rs1.getString("TstId") + " and TstId < " + getEnd(stChild, stPk, rs1));
          }
        } else if (stAction.equals("promote"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstLevel=(TstLevel+1) where RecId=" + iRecIdDo);
          String stWhat = this.ebEnt.ebUd.request.getParameter("what");
          if (stWhat.equals("children"))
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstLevel=(TstLevel+1) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
              + "and TstId > " + rs1.getString("TstId") + " and TstId < " + getEnd(stChild, stPk, rs1));
          }
        } else if (stAction.equals("customize"))
        {
          stTemp = this.ebEnt.ebUd.request.getParameter("savedata");
          String stC = getCustomize(stChild, stLink, stPk);
          if (stTemp == null || stTemp.length() <= 0)
            return stC;
        } else if (stAction.equals("custom"))
        {
          String stB = this.ebEnt.ebUd.request.getParameter("b");
          if (stB != null)
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstFlags=0 where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + "");
            try
            {
              int iLevel = Integer.parseInt(stB);
              this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstFlags=1 "
                + "where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and TstLevel > " + iLevel);
            } catch (Exception e)
            {
            }
            setFlags(stChild);
          }
        } else if (stAction.equals("editfull"))
        {
          stTemp = editFull(stChild, stR);
          if (stTemp != null && stTemp.length() > 0)
            return stTemp;
        } else if (stAction.equals("map"))
        {
          stTemp = doMap(stChild, stR);
          if (stTemp != null && stTemp.length() > 0)
            return stTemp;
        }
      }
      String stHeader = xlsHeaders(stChild);
      String s1 = "</form><form method=post name='form" + stChild + "' id='form" + stChild + "' onsubmit='return myValidation(this)'>"
        + "<input type=hidden name=from value='" + iFrom + "'><table class=l1table><tr><td class=l1td colspan=" + iMaxFields + " align=center><h2>"
        + this.rsProject.getString("ProjectName") + "</h2></td></tr>";
      sbReturn.append(s1);
      String stEdit = this.ebEnt.ebUd.request.getParameter("edit");
      if (stEdit != null && stEdit.length() > 0)
        iEditId = Integer.parseInt(stEdit);
      else
        iEditId = -2;
      int iMaxRecords = this.ebEnt.dbDyn.ExecuteSql1n("select count(*) from Test where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and TstFlags != 1");
      stSql = "select * from Test where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and TstFlags != 1 order by TstId,RecId desc limit " + iFrom + "," + iBlock;
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      int iMax = rs.getRow();
      if (iMax <= 0) // Fix query, if it does not return any rows ... otherwise we auto insert
      {
        iFrom = 0; // reset to 0
        rs.close();
        stSql = "select * from Test where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and TstFlags != 1 order by TstId,RecId desc limit " + iFrom + "," + iBlock;
        rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
        rs.last();
        iMax = rs.getRow();
        if (iMax <= 0)
        { // Clear all collapsed flags -- do EXPAND ALL
          this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstFlags=0 where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + "");
          rs.close();
          stSql = "select * from Test where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and TstFlags != 1 order by TstId,RecId desc limit " + iFrom + "," + iBlock;
          rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
          rs.last();
          iMax = rs.getRow();
        }
      }
      if (iMax > 0)
      {
        sbReturn.append(stHeader);
        String stClass = "";
        int iPrevLevel = -1;
        String stId = "";
        String stNm = "";
        for (int iR = 1; iR <= iMax; iR++)
        {
          int iNextLevel = -1;
          int iNextShow = 0;
          if (iR < iMax)
          {
            rs.absolute(iR + 1);
            iNextLevel = rs.getInt("TstLevel");
            if (rs.getInt("TstFlags") == 0)
              iNextShow = 1; // rs.getInt("nmShow");
            else
              iNextShow = 0;
          } else
            iNextShow = 1; // insert below on last line
          rs.absolute(iR);
          if (rs.getInt("TstFlags") == 1)
            continue;
          //  + " onmouseout='timer(2)'>";
          if ((iR & 1) != 0)
            stClass = " class=l1td ";
          else
            stClass = " class=l1td2 ";
          stId = " id=r" + rs.getInt("RecId") + "_";
          stNm = " name=r" + rs.getInt("RecId") + "_";
          String stDescription = rs.getString("TstDescription");
          if (stDescription == null)
            stDescription = "";
          else
            stDescription = stDescription.trim();
          if (iEditId <= 0)
          {
            //onmouseover="
            // + "\"setActions(1," + rs.getInt("RecId") + "," + rs.getInt("TstId") + "," + rs.getInt("TstLevel") + "," + iNextLevel + "," + iPrevLevel + "," + iNextShow + ",'" + stLink + "' )\""
            s1 = "<tr  "
              + " onClick="
              + "\"setActionsClick(3," + rs.getInt("RecId") + "," + rs.getInt("TstId") + "," + rs.getInt("TstFlags") + "," + rs.getInt("TstLevel") + "," + iNextLevel + "," + iPrevLevel + "," + iNextShow + ",'" + stLink + "'," + iMaxFields + " )\""
              + " >";
            sbReturn.append(s1);
          } else
          {
            sbReturn.append("<tr>)");
          }
          for (int iF = 1; iF <= this.iMaxFields; iF++)
          {
            this.rsFields.absolute(iF);
            sbReturn.append("<td " + stClass + stId + iF + stNm + iF + " " + getStyle(0) + ">");
            if (iF == 1)
              sbReturn.append("<a name='row" + rs.getString("TstId") + "'></a>");

            String stValue = rs.getString(rsFields.getString("stDbFieldName"));
            if (stValue == null)
              stValue = "";
            if (this.rsFields.getInt("nmDataType") == 5 && stValue.length() > 0)
              sbReturn.append(this.epsUd.rsMyDiv.getString("stMoneySymbol") + " ");

            if (iEditId == rs.getInt("RecId") && (rsFields.getInt("nmFlags") & 0x1) != 0)
            {
              sbReturn.append(editXlsField(stValue));
            } else
            {
              if (rsFields.getString("stDbFieldName").equals("TstTitle"))
                for (int i = 0; i < rs.getInt("TstLevel"); i++)
                  sbReturn.append("&nbsp;&nbsp;");
              sbReturn.append(stValue.replace("\n", "<BR>"));
            }
          }
          sbReturn.append("</tr>");
          iPrevLevel = rs.getInt("TstLevel");
        }
        sbReturn.append(doNext(stChild, iMaxFields, iMaxRecords, iFrom, iBlock, stGoBack, stPk));
      } else
      {
        sbReturn.append(doNext(stChild, iMaxFields, 0, 0, iBlock, stGoBack, stPk));
      }
      sbReturn.append("</table></form><br/>");
    } catch (Exception e)
    {
      this.stError += "<br>ERROR xlsTestEdit: " + e;
    }
    return sbReturn.toString();
  }

  public String xlsSchedulesEdit(String stChild)
  {
    StringBuilder sbReturn = new StringBuilder(100000);
    String stSql = "";
    int iEditId = 0;
    String stTemp = "";
    try
    {
      stPk = this.ebEnt.ebUd.request.getParameter("pk");
      stGoBack = "?stAction=projects&t=12";
      String stLink = "?stAction=projects&t=" + this.ebEnt.ebUd.request.getParameter("t") + "&do=xls&pk=" + stPk + "&parent=" + stTemp + "&child=" + stChild;
      int iFrom = 0;
      int iBlock = this.epsUd.rsMyDiv.getInt("ReqSchRows");
      this.rsProject = this.ebEnt.dbDyn.ExecuteSql("select * from Projects where RecId=" + stPk);
      this.rsProject.absolute(1);
      nmBaseline = this.rsProject.getInt("CurrentBaseline");
      stTemp = this.ebEnt.ebUd.request.getParameter("from");
      if (stTemp != null && stTemp.length() > 0)
      {
        iFrom = Integer.parseInt(stTemp);
      }
      stLink += "&from=" + iFrom;
      stTemp = this.ebEnt.ebUd.request.getParameter("parent");
      if (stTemp == null || stTemp.length() <= 0)
        stTemp = this.ebEnt.ebUd.request.getParameter("t");
      String stSave = this.ebEnt.ebUd.request.getParameter("savedataxls");
      if (stSave != null && stSave.length() > 0)
      {
        if (!stSave.equals("Cancel"))
        {
        	saveInline(stChild, stPk);
        }
        this.ebEnt.ebUd.setRedirect(stLink + "#row" + this.ebEnt.ebUd.request.getParameter("edit"));
        return ""; // redirect, to remove EDIT tag ----------------------------->
      }
      String stAction = this.ebEnt.ebUd.request.getParameter("a");
      String stR = this.ebEnt.ebUd.request.getParameter("r");
      if (stAction != null && stAction.length() > 0 && stR != null && stR.length() > 0)
      { // PROCESS ACTIONS
        int iRecIdDo = Integer.parseInt(stR);
        ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql("select * from Schedule where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and RecId=" + iRecIdDo);
        rs1.absolute(1);
        if (stAction.equals("collapse"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags = (SchFlags | 2) where RecId= " + iRecIdDo + " and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
          this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags = (SchFlags | 1) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
            + "and SchId > " + rs1.getString("SchId") + " and SchId < " + getEnd(stChild, stPk, rs1));
        } else if (stAction.equals("expand"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags = (SchFlags & ~0x3) where RecId= " + iRecIdDo + " and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
          this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags = (SchFlags & ~0x3) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
            + "and SchId > " + rs1.getString("SchId") + " and SchId < " + getEnd(stChild, stPk, rs1));
        } else if (stAction.equals("insert"))
        {
          int iRecMax = this.ebEnt.dbDyn.ExecuteSql1n("select max(RecId) from Schedule");
          iRecMax++;
          String stWhat = this.ebEnt.ebUd.request.getParameter("what");
          try
          {
            iEditId = rs1.getInt("SchId");
            if (stWhat.equals("below"))
            {
              iEditId++;
              this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchId=(SchId+1) where SchId > " + rs1.getString("SchId") + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " ");
            } else
            {
              // Above
              this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchId=(SchId+1) where SchId >= " + rs1.getString("SchId") + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " ");
            }
            this.ebEnt.dbDyn.ExecuteUpdate("insert into Schedule (RecId,nmProjectId,SchId,nmBaseline,SchLevel) "
              + "values(" + iRecMax + "," + stPk + "," + iEditId + "," + rs1.getInt("nmBaseline") + "," + rs1.getInt("SchLevel") + ")");
          } catch (Exception e)
          {
            iEditId = 1;
            this.ebEnt.dbDyn.ExecuteUpdate("insert into Schedule (RecId,nmProjectId,SchId,nmBaseline,SchLevel) "
              + "values(" + iRecMax + "," + stPk + "," + iEditId + "," + nmBaseline + ",0)");
          }
          this.epsUd.makeTask(16, this.epsUd.epsEf.getScheduleName("" + iRecIdDo, stPk, nmBaseline)); // New Project Schedule
          this.ebEnt.ebUd.setRedirect(stLink + "&edit=" + iRecMax + "&new=y#row" + iEditId);
          return ""; //----------------------------->
        } else if (stAction.equals("send"))
        {
        	ResultSet rset = this.ebEnt.dbDyn.ExecuteSql("select SchEstimatedEffort, SchEfforttoDate from schedule where RecId=" + iRecIdDo + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
        	rset.absolute(1);
        	if(rset.getInt("SchEfforttoDate") > rset.getInt("SchEstimatedEffort")){
        		this.ebEnt.ebUd.setPopupMessage("Expended hours may not be more than estimated hours. Please adjust estimated hours first.");
        	}else{
        		this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchMessage='Y', dtSchLastUpdate=now() where RecId=" + iRecIdDo + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
                int iDel = 1;
                
                this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchLevel=(SchLevel-1) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
                  + "and SchId > " + rs1.getString("SchId") + " and SchId < " + getEnd(stChild, stPk, rs1));
                this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchId=(SchId-" + iDel + ") where SchId > " + rs1.getString("SchId") + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
                  + "and SchId > " + rs1.getString("SchId"));
        	}
        } else if (stAction.equals("delete"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("delete from Schedule where RecId=" + iRecIdDo + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
          String stWhat = this.ebEnt.ebUd.request.getParameter("what");
          int iDel = 1;
          if (stWhat.equals("children"))
          {
            int iSchIdEnd = getEnd(stChild, stPk, rs1);
            this.ebEnt.dbDyn.ExecuteUpdate("delete from Schedule where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
              + "and SchId > " + rs1.getString("SchId") + " and SchId < " + iSchIdEnd);
            iDel += (iSchIdEnd - rs1.getInt("SchId") - 1);
          } else
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchLevel=(SchLevel-1) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
              + "and SchId > " + rs1.getString("SchId") + " and SchId < " + getEnd(stChild, stPk, rs1));
          }
          this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchId=(SchId-" + iDel + ") where SchId > " + rs1.getString("SchId") + " and  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
            + "and SchId > " + rs1.getString("SchId"));
        } else if (stAction.equals("demote"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchLevel=(SchLevel-1) where RecId=" + iRecIdDo + " and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
          String stWhat = this.ebEnt.ebUd.request.getParameter("what");
          if (stWhat.equals("children"))
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchLevel=(SchLevel-1) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
              + "and SchId > " + rs1.getString("SchId") + " and SchId < " + getEnd(stChild, stPk, rs1));
          }
        } else if (stAction.equals("promote"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchLevel=(SchLevel+1) where RecId=" + iRecIdDo + " and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
          String stWhat = this.ebEnt.ebUd.request.getParameter("what");
          if (stWhat.equals("children"))
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchLevel=(SchLevel+1) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " "
              + "and SchId > " + rs1.getString("SchId") + " and SchId < " + getEnd(stChild, stPk, rs1));
          }
        } else if (stAction.equals("customize"))
        {
          stTemp = this.ebEnt.ebUd.request.getParameter("savedata");
          String stC = getCustomize(stChild, stLink, stPk);
          if (stTemp == null || stTemp.length() <= 0)
            return stC;
        } else if (stAction.equals("custom"))
        {
          String stB = this.ebEnt.ebUd.request.getParameter("b");
          if (stB != null)
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags = (SchFlags & ~0x3) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + "");
            try
            {
              int iLevel = Integer.parseInt(stB);
              this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags = (SchFlags | 1) "
                + "where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and SchLevel > " + iLevel);
            } catch (Exception e)
            {
            }
            setFlags(stChild);
          }
        } else if (stAction.equals("editfull"))
        {
          stTemp = editFull(stChild, stR);
          if (stTemp != null && stTemp.length() > 0)
            return stTemp;
        } else if (stAction.equals("editmake"))
        {
          stTemp = editMake(stChild, stR);
          if (stTemp != null && stTemp.length() > 0)
            return stTemp;
        } else if (stAction.equals("map"))
        {
          stTemp = doMap(stChild, stR);
          if (stTemp != null && stTemp.length() > 0)
            return stTemp;
        }
      }
      String stHeader = xlsHeaders(stChild);
      stTemp = "</form><form method=post name='form" + stChild + "' id='form" + stChild + "' onsubmit='return myValidation(this)'>"
        + "<input type=hidden name=from value='" + iFrom + "'><table class=l1table><tr><td class=l1td colspan=" + iMaxFields + " align=center><h2>"
        + this.rsProject.getString("ProjectName") + "</h2></td></tr>";
      sbReturn.append(stTemp);
      String stEdit = this.ebEnt.ebUd.request.getParameter("edit");
      if (stEdit != null && stEdit.length() > 0)
        iEditId = Integer.parseInt(stEdit);
      else
        iEditId = -2;
      int iMaxRecords = this.ebEnt.dbDyn.ExecuteSql1n("select count(*) from Schedule where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and (SchFlags & 0x1) != 1");

      //do not go back to first page if click next on last page
      if(iFrom > iMaxRecords){
    	  iFrom -= iBlock;
      }
      
      stSql = "select * from Schedule where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and (SchFlags & 0x1) != 1 order by SchId,RecId desc limit " + iFrom + "," + iBlock;
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      int iMax = rs.getRow();
      if (iMax <= 0) // Fix query, if it does not return any rows ... otherwise we auto insert
      {
        iFrom = 0; // reset to 0
        rs.close();
        stSql = "select * from Schedule where SchMessage IS NULL and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and (SchFlags & 0x1) != 1 order by SchId,RecId desc limit " + iFrom + "," + iBlock;
        rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
        rs.last();
        iMax = rs.getRow();
        if (iMax <= 0)
        { // Clear all collapsed flags -- do EXPAND ALL
          this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags = (SchFlags & ~0x3) where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + "");
          rs.close();
          stSql = "select * from Schedule where SchMessage IS NULL and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and (SchFlags & 0x1) != 1 order by SchId,RecId desc limit " + iFrom + "," + iBlock;
          rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
          rs.last();
          iMax = rs.getRow();
        }
      }
      if (iMax > 0)
      {
        sbReturn.append(stHeader);
        String stClass = "";
        int iPrevLevel = -1;
        String stId = "";
        String stNm = "";
        for (int iR = 1; iR <= iMax; iR++)
        {
          int iNextLevel = -1;
          int iNextShow = 0;
          if (iR < iMax)
          {
            rs.absolute(iR + 1);
            iNextLevel = rs.getInt("SchLevel");
            if ((rs.getInt("SchFlags") & 0x1) == 0)
              iNextShow = 1; // rs.getInt("nmShow");
            else
              iNextShow = 0;
          } else
            iNextShow = 1; // insert below on last line
          rs.absolute(iR);
          if ((rs.getInt("SchFlags") & 0x1) == 1)
            continue;
          //  + " onmouseout='timer(2)'>";
          if ((iR & 1) != 0)
            stClass = " class=l1td ";		//white row
          else
            stClass = " class=l1td2 ";		//grey row
          stId = " id=r" + rs.getInt("RecId") + "_";
          stNm = " name=r" + rs.getInt("RecId") + "_";
          String stDescription = rs.getString("SchDescription");
          if (stDescription == null)
            stDescription = "";
          else
            stDescription = stDescription.trim();
          if (iEditId <= 0 && (this.ebEnt.ebUd.getLoginPersonFlags() & 0x60) != 0) // Only PM and PPM
          {
            //onmouseover="
            // + "\"setActions(1," + rs.getInt("RecId") + "," + rs.getInt("SchId") + "," + rs.getInt("SchLevel") + "," + iNextLevel + "," + iPrevLevel + "," + iNextShow + ",'" + stLink + "' )\""
            sbReturn.append("<tr  "
              + " onClick="
              + "\"setActionsClick(1," + rs.getInt("RecId") + "," + rs.getInt("SchId") + "," + rs.getInt("SchFlags") + "," + rs.getInt("SchLevel") + "," + iNextLevel + "," + iPrevLevel + "," + iNextShow + ",'" + stLink + "'," + iMaxFields + " )\""
              + " >");
          } else
          {
            sbReturn.append("<tr>");
          }
          iSubmit = 0;
          String laborcat = "";
          String tbstyle = "";
          //insert each field as column
          for (int iF = 1; iF <= this.iMaxFields; iF++)
          {
            this.rsFields.absolute(iF);
            String stValue = rs.getString(rsFields.getString("stDbFieldName"));
            
            if (rsFields.getInt("nmForeignId") == 825 && this.ebEnt.ebUd.request.getParameter("edit") != null){
            	//don't append labor categories if inline edit but store labor categories and process below all fields
            	laborcat = stValue;
            } else {
            	 if(rsFields.getInt("nmDataType") == 3 || rsFields.getInt("nmDataType") == 4 || rsFields.getInt("nmDataType") == 41)
            		 sbReturn.append("<td " + stClass + stId + iF + stNm + iF + " " + getStyle(0) + " align='left'>");	//align text left
            	 else
            		 sbReturn.append("<td " + stClass + stId + iF + stNm + iF + " " + getStyle(0) + ">");
            	 tbstyle = stClass + stId + iF + stNm + iF + " " + getStyle(0);
                 if (iF == 1)
                   sbReturn.append("<a name='row" + rs.getString("SchId") + "'></a>");
                 if (stValue == null)
                   stValue = "";
                 if (this.rsFields.getInt("nmDataType") == 5 && stValue.length() > 0){
                   sbReturn.append(this.epsUd.rsMyDiv.getString("stMoneySymbol") + " ");		//inline fields
                 }
                 if (rsFields.getInt("nmForeignId") == 813 && iEditId == rs.getInt("RecId") && (rsFields.getInt("nmFlags") & 0x1) != 0){
                	  stValue = makeDependenciesTB(rs, stChild);
                  	  sbReturn.append(stValue.replace("\n", "<BR>"));
                 }
                 else if (iEditId == rs.getInt("RecId") && (rsFields.getInt("nmFlags") & 0x1) != 0)
                 {
                   sbReturn.append(editXlsField(stValue));		//passes value into a editable field
                 }
                 else
                 {
                   if (rsFields.getString("stDbFieldName").equals("SchTitle"))
                     for (int i = 0; i < rs.getInt("SchLevel"); i++)
                       sbReturn.append("&nbsp;&nbsp;");	//indentations for levels
                   
                   
                   
                   //labor categories column
                   if(!stValue.equals("") && rsFields.getString("stDbFieldName").equals("SchLaborCategories")){
                	  String stReturn = "";
         	    	  String stRowspan = "";
         	    	  String[] aFields = null;
         	    	  String[] aRecords = stValue.split("\\|", -1);
                      int iRecMax = aRecords.length;

                      for (int iRa = 0; iRa < iRecMax; iRa++)
                      {
                         if (iRa < iRecMax)
                         {
                           aFields = aRecords[iRa].split("~", -1); // LcId, MaxEmployess, Effort,
                           ResultSet rsLcAll = this.ebEnt.dbDyn.ExecuteSql("SELECT * FROM LaborCategory WHERE nmLcId="+aFields[0]);
                           rsLcAll.last();
                           stValue = makeListValue(rsFields, rsLcAll.getString("LaborCategory"));
                           sbReturn.append(stValue.replace("\n", "<BR>")+"<BR>");
                         } else
                         {
                           aFields = new String[7];
                           for (int i = 0; i < aFields.length; i++)
                           {
                             if (i == 1)
                               aFields[i] = "1";
                             else
                               aFields[i] = "";
                           }
                         }
                      }
                   }
                   else if(stValue.equals("") && rsFields.getString("stDbFieldName").equals("SchLaborCategories")){
                	   stValue = makeListValue(rsFields, "");
                	   sbReturn.append(stValue.replace("\n", "<BR>"));
                   }
                   else if(rsFields.getString("stDbFieldName").equals("SchDependencies")){
                	   //stValue = makeListValue(rsFields, stValue+"");
                	   String stdSql = "select * from teb_link where nmProjectId=" + this.stPk + " and nmBaseline=" + this.nmBaseline + " and nmLinkFlags=2 and nmToId=" + rs.getInt("RecId") + " and nmToProject=nmProjectId order by nmFromId";
	           		   ResultSet res = this.ebEnt.dbDyn.ExecuteSql(stdSql);
	           		   res.last();
	           		   int rws = res.getRow();
	           		   res.absolute(1);
	           		   stValue = "";
                	   for(int l=0; l<rws; l++){
                		   stValue += res.getString("nmFromId");
                		   if(l<rws-1)
                			   stValue += ",";
                		   res.next();
                	   }
	           		   sbReturn.append(stValue.replace("\n", "<BR>"));
                   }
                   else if(rsFields.getString("stDbFieldName").equals("SchDone")){
                	   if(stValue.equals("Y")){
                		   stValue = "<input type='checkbox' disabled='disabled' checked='checked' value='Y'>";
                	   }else{
                		   stValue = "<input type='checkbox' disabled='disabled' value='N'>";
                	   }
                	   sbReturn.append(stValue);
                	   
                   }
                   else{
                	   stValue = makeListValue(rsFields, stValue);
                	   sbReturn.append(stValue.replace("\n", "<BR>"));
                   }
                 }
            }
          }
          sbReturn.append("</tr>");
          
          //append labor category row
          if (iEditId == rs.getInt("RecId") && (rsFields.getInt("nmFlags") & 0x1) != 0){
        	  sbReturn.append("<tr><td " + tbstyle + "><input type=submit name=savedataxls value='Save' onClick=\"return setSubmitId(9970);\"></td>" +
        	  		"<td " + tbstyle + "><input type=submit name=savedataxls value='Cancel' onClick=\"return setSubmitId(8888);\"></td>" +
        	  		"<td " + tbstyle + " colspan='"+this.iMaxFields+"'>");
        	  
        	  //parse labor cat rows
        	  String stReturn = "";
	    	  String stRowspan = "";
	    	  String[] aFields = null;
	    	  String[] aRecords = null;
	    	  int iRecMax = 0;
        	  
	    	  if (laborcat.length() > 0)
              {
                aRecords = laborcat.split("\\|", -1);
                iRecMax = aRecords.length;
              } else
              {
                aRecords = new String[1];
                iRecMax = 0;
              }
	    	  
	    	  sbReturn.append("<table class='l1table'><tr class=d1>"+
	        		    "<td colspan=2>Labor Category / Max Users</td><td>Effort</td>"+
	        		    "<td>Must Assign</td><td>Most Desireable</td><td>Least Desirable</td><td>Do Not Assign</td></tr>");
	    	  stRowspan = "";
	    	  int iRa = 0;
	    	  if(iRecMax > 0){		//add existing labor category rows
	              for (iRa = 0; iRa < iRecMax; iRa++)
	              {
	                stReturn += "<tr class=d0>";
	                if (iRa < iRecMax)
	                {
	                  aFields = aRecords[iRa].split("~", -1); // LcId, MaxEmployess, Effort,
	                } else
	                {
	                  aFields = new String[7];
	                  for (int i = 0; i < aFields.length; i++)
	                  {
	                    if (i == 1)
	                      aFields[i] = "1";
	                    else
	                      aFields[i] = "";
	                  }
	                }
	
	                stReturn += "<td " + stRowspan + "><select name=lc_" + iRa + " id=lc_" + iRa + ">";
	                ResultSet rsLcAll = this.ebEnt.dbDyn.ExecuteSql("SELECT * FROM LaborCategory order by LaborCategory");
	                rsLcAll.last();
	                int iMaxLc = rsLcAll.getRow();
	                //populate dropdown
	                stReturn += this.ebEnt.ebUd.addOption2("-- Select Labor Category --", "0", aFields[0]);
	                for (int iLc = 1; iLc <= iMaxLc; iLc++)
	                {
	                  rsLcAll.absolute(iLc);
	                  stReturn += this.ebEnt.ebUd.addOption2(rsLcAll.getString("LaborCategory"), rsLcAll.getString("nmLcId"), aFields[0]);
	                }
	                stReturn += "</select></td>";
	                stReturn += "<td " + stRowspan + "><select name=nr_" + iRa + " id=nr_" + iRa + ">";
	                for (int iLc = 1; iLc <= 20; iLc++)
	                {
	                  stReturn += this.ebEnt.ebUd.addOption2("" + iLc, "" + iLc, aFields[1]);
	                }
	                stReturn += "</select></td>";
	                stReturn += "<td " + stRowspan + "><input type=text size=5 style='text-align:right' name=est_" + iRa + " id=est_" + iRa + " value=\"" + aFields[2] + "\"></td>";
	                stReturn += "<td " + stRowspan + ">" + this.epsUd.epsEf.getMulitUsers(stChild, 9001, "_must_" + iRa, aFields[3], "lc_" + iRa) + "</td>";
	                stReturn += "<td " + stRowspan + ">" + this.epsUd.epsEf.getMulitUsers(stChild, 9002, "_most_" + iRa, aFields[4], "lc_" + iRa) + "</td>";
	                stReturn += "<td " + stRowspan + ">" + this.epsUd.epsEf.getMulitUsers(stChild, 9003, "_least_" + iRa, aFields[5], "lc_" + iRa) + "</td>";
	                stReturn += "<td " + stRowspan + ">" + this.epsUd.epsEf.getMulitUsers(stChild, 9004, "_not_" + iRa, aFields[6], "lc_" + iRa) + "</td>";
	                stReturn += "</tr>";
	              }
	    	  }
	    	  	//add new labor category row
	    	  	stReturn += "<tr class=d0>";
				stReturn += "<td " + stRowspan + " align='left'>Add New:<br><select name=lc_" + iRa + " id=lc_" + iRa + ">";
				ResultSet rsLcAll = this.ebEnt.dbDyn.ExecuteSql("SELECT * FROM LaborCategory order by LaborCategory");
				rsLcAll.last();
				int iMaxLc = rsLcAll.getRow();
				//populate dropdown
				stReturn += this.ebEnt.ebUd.addOption2("-- Select Labor Category --", "0", "");
				for (int iLc = 1; iLc <= iMaxLc; iLc++)
				{
				  rsLcAll.absolute(iLc);
				  stReturn += this.ebEnt.ebUd.addOption2(rsLcAll.getString("LaborCategory"), rsLcAll.getString("nmLcId"), "");
				}
				stReturn += "</select></td>";
				stReturn += "<td " + stRowspan + "><select name=nr_" + iRa + " id=nr_" + iRa + ">";
				for (int iLc = 1; iLc <= 20; iLc++)
				{
				  stReturn += this.ebEnt.ebUd.addOption2("" + iLc, "" + iLc, "");
				}
				stReturn += "</select></td>";
				stReturn += "<td " + stRowspan + "><input type=text size=5 style='text-align:right' name=est_" + iRa + " id=est_" + iRa + " value=\"\"></td>";
				stReturn += "<td " + stRowspan + ">" + this.epsUd.epsEf.getMulitUsers(stChild, 9001, "_must_" + iRa, "", "lc_" + iRa) + "</td>";
				stReturn += "<td " + stRowspan + ">" + this.epsUd.epsEf.getMulitUsers(stChild, 9002, "_most_" + iRa, "", "lc_" + iRa) + "</td>";
				stReturn += "<td " + stRowspan + ">" + this.epsUd.epsEf.getMulitUsers(stChild, 9003, "_least_" + iRa, "", "lc_" + iRa) + "</td>";
				stReturn += "<td " + stRowspan + ">" + this.epsUd.epsEf.getMulitUsers(stChild, 9004, "_not_" + iRa, "", "lc_" + iRa) + "</td>";
				stReturn += "</tr>";
			
			  //close the labor category table
	    	  stReturn += "</table><input type=hidden name=imax id=imax value='" + (iRecMax+1) + "'>";
              sbReturn.append(stReturn);
          }
          
          iPrevLevel = rs.getInt("SchLevel");
        }
        sbReturn.append(doNext(stChild, iMaxFields, iMaxRecords, iFrom, iBlock, stGoBack, stPk));
      } else
      {
        sbReturn.append(doNext(stChild, iMaxFields, 0, 0, iBlock, stGoBack, stPk));
      }
      sbReturn.append("</table></form><br/>");
    } catch (Exception e)
    {
      this.stError += "<br>ERROR xlsSchedulesEdit: " + e;
    }
    return sbReturn.toString();
  }

  public void setFlags(String stChild)
  {
    String stSql = "";
    String stOrder = "";
    int iFlags2above = 0;
    int iNewFlags = 0;
    try
    {
      if (stChild.equals("19")) // Requirements
      {
        stSql = "select RecId,ReqId,ReqFlags,ReqLevel from Requirements where nmProjectId=" + this.rsProject.getString("RecId") + " and nmBaseline=" + this.nmBaseline + " and (ReqFlags & 0x1) = 1";
        stOrder = " order by ReqId";
      } else if (stChild.equals("21")) // Requirements
      {
        stSql = "select RecId,SchId,SchFlags,SchLevel from Schedule where nmProjectId=" + this.rsProject.getString("RecId") + " and nmBaseline=" + this.nmBaseline + " and (SchFlags & 0x1) = 1 ";
        stOrder = " order by SchId";
      } else
      {
        stSql = "select RecId,TstId,TstFlags,TstLevel from Test where nmProjectId=" + this.rsProject.getString("RecId") + " and nmBaseline=" + this.nmBaseline + " and (TstFlags &0x1) = 1";
        stOrder = " order by TstId";
      }
      ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql(stSql + stOrder);
      int iLastId = -2;
      rs1.last();
      int iMax = rs1.getRow();
      for (int iR = 1; iR <= iMax; iR++)
      {
        rs1.absolute(iR);
        if ((iLastId + 1) != rs1.getInt(2))
        { // Out of sequence ... set parent to COLAPSED
          iFlags2above = 0;
          if (stChild.equals("19")) // Requirements
          {
            if (rs1.getInt(2) > 2)
              iFlags2above = this.ebEnt.dbDyn.ExecuteSql1n("select ReqFlags from Requirements where nmProjectId=" + this.rsProject.getString("RecId") + " and nmBaseline=" + this.nmBaseline + " and ReqId=" + (rs1.getInt(2) - 2));
            if (iFlags2above != 0)
              iNewFlags = 4; // Record ABOVE is hidden
            else
              iNewFlags = 2;
            this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqFlags =( ReqFlags | " + iNewFlags + ") where nmProjectId=" + this.rsProject.getString("RecId") + " and nmBaseline=" + this.nmBaseline + " and ReqId=" + (rs1.getInt(2) - 1));
          } else if (stChild.equals("21")) // Schedule
          {
            if (rs1.getInt(2) > 2)
              iFlags2above = this.ebEnt.dbDyn.ExecuteSql1n("select SchFlags from Schedule where nmProjectId=" + this.rsProject.getString("RecId") + " and nmBaseline=" + this.nmBaseline + " and SchId=" + (rs1.getInt(2) - 2));
            if (iFlags2above != 0)
              iNewFlags = 4; // Record ABOVE is hidden
            else
              iNewFlags = 2;
            this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags= (SchFlags | " + iNewFlags + ") where nmProjectId=" + this.rsProject.getString("RecId") + " and nmBaseline=" + this.nmBaseline + " and SchId=" + (rs1.getInt(2) - 1));
          } else   // Test
          {
            if (rs1.getInt(2) > 2)
              iFlags2above = this.ebEnt.dbDyn.ExecuteSql1n("select TstFlags from Test where nmProjectId=" + this.rsProject.getString("RecId") + " and nmBaseline=" + this.nmBaseline + " and TstId=" + (rs1.getInt(2) - 2));
            if (iFlags2above != 0)
              iNewFlags = 4; // Record ABOVE is hidden
            else
              iNewFlags = 2;
            this.ebEnt.dbDyn.ExecuteUpdate("update Test set TstFlags=" + iNewFlags + " where nmProjectId=" + this.rsProject.getString("RecId") + " and nmBaseline=" + this.nmBaseline + " and ReqId=" + (rs1.getInt(2) - 1));
          }
        }
        iLastId = rs1.getInt(2);
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR setFlags " + stSql + " " + e;
    }
  }

  public String getCustomize(String stChild, String stLink, String stPk)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    sbReturn.append("<h1>Customize</h1>");
    try
    {
      sbReturn.append("</form><form name='form4' id='form4' action=\"./");
      sbReturn.append(stLink);
      sbReturn.append("&r=1&a=custom\" onsubmit='return myValidation(this)' method='post'><table border=1>");
      sbReturn.append("<tr><td align=right>Show levels:</td><td align=left><select name=b id=b  onChange=\"document.form4.submit();\" >");
      int iLevelMax = 0;
      if (stChild.equals("19"))
        iLevelMax = this.ebEnt.dbDyn.ExecuteSql1n("select max(ReqLevel) from Requirements where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " ");
      else
        iLevelMax = this.ebEnt.dbDyn.ExecuteSql1n("select max(SchLevel) from Schedule where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " ");
      sbReturn.append(this.ebEnt.ebUd.addOption("-- Level --", "", ""));
      sbReturn.append(this.ebEnt.ebUd.addOption("All", "" + (iLevelMax + 1), ""));
      for (int i = 0; i <= iLevelMax; i++)
        sbReturn.append(this.ebEnt.ebUd.addOption("" + i, "" + i, ""));
      sbReturn.append("</select>");
      sbReturn.append("</td></tr>");

      sbReturn.append("</table></form>");

      ResultSet rsTable = this.ebEnt.dbDyn.ExecuteSql("SELECT * FROM teb_table where nmTableId=" + stChild);
      rsTable.absolute(1);
      EpsReport epsReport = new EpsReport();
      if (stChild.equals("19"))
        epsReport.setUd(epsUd, 1);
      else if (stChild.equals("21"))
        epsReport.setUd(epsUd, 2);
      else
        epsReport.setUd(epsUd, 3);
      sbReturn.append(epsReport.customReportDesigner(rsTable));
    } catch (Exception e)
    {
      stError += "<br>ERROR getCustomize " + e;
    }
    sbReturn.append("<br>&nbsp;<br>");
    return sbReturn.toString();
  }

  public String xlsHeaders(String stChild)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    String stSql = "";
    try
    {
      String stLabel = "";
      if (stChild.equals("19"))
        stSql = "SELECT * FROM teb_reportcolumns rc, teb_fields f where rc.nmFieldId=f.nmForeignId and rc.nmCustomReportId=1 and rc.stShow='y' order by rc.nmOrder;";
      else if (stChild.equals("21"))
        stSql = "SELECT * FROM teb_reportcolumns rc, teb_fields f where rc.nmFieldId=f.nmForeignId and rc.nmCustomReportId=2 and rc.stShow='y' order by rc.nmOrder;";
      else if (stChild.equals("34"))
        stSql = "SELECT * FROM teb_reportcolumns rc, teb_fields f where rc.nmFieldId=f.nmForeignId and rc.nmCustomReportId=3 and rc.stShow='y' order by rc.nmOrder;";
      this.rsFields = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rsFields.last();
      this.iMaxFields = rsFields.getRow();

      sbReturn.append("<tr>");

      for (int iF = 1; iF <= iMaxFields; iF++)
      {
        rsFields.absolute(iF);
        
        if (rsFields.getInt("nmForeignId") == 825 && this.ebEnt.ebUd.request.getParameter("edit") != null){
        	//skip labor category column if inline edit
        }else{
	        stLabel = rsFields.getString("stShort");
	        if (stLabel == null || stLabel.length() <= 0)
	          stLabel = rsFields.getString("stLabelShort");
	        if (stLabel == null || stLabel.length() <= 0)
	          stLabel = rsFields.getString("stLabel");
	        sbReturn.append("<th class=l1th style='width:");
	        sbReturn.append(rsFields.getString("nmWidth"));
	        sbReturn.append("%;'>");
	        sbReturn.append(stLabel);
	        sbReturn.append("</th>");
        }
      }
      sbReturn.append("</tr>");
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR xlsHeaders " + e;
    }
    sbReturn.append("</tr>");
    return sbReturn.toString();
  }

  private String doNext(String stChild, int iCol, int iMaxRecords, int iFrom, int iBlock, String stGoBack, String stPk)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    sbReturn.append("<tr><td colspan=");
    sbReturn.append(iCol);
    sbReturn.append(" width='100%' ><table border=0 bgcolor=skyblue width='100%'><tr><td width='33%' align=left>");
    //http://localhost:8084/eps/?stAction=projects&t=0&do=xls&pk=5&parent=12&child=46#next
    sbReturn.append("<input type=button onClick=\"parent.location='").append(stGoBack);
    sbReturn.append("&pk=");
    sbReturn.append(stPk);
    sbReturn.append("&do=edit'\" value='Go Back to Projects'>");
    sbReturn.append("</td><td width='33%' align=center>");
    //sbReturn.append( "<input type=button onClick=\"parent.location='./?stAction=projects&t=12&do=xls&pk=" + stPk + "&parent=12&child=46&child2=" + stChild + "'\" value='Analyze'>";
    if (iMaxRecords > 0)
    {
      sbReturn.append("<td width='33%' align=right ><input type=button onClick=\"parent.location='./?stAction=projects&t=12&do=xls&pk=");
      sbReturn.append(stPk);
      sbReturn.append("&parent=12&child=");
      sbReturn.append(stChild);
      sbReturn.append("&from=0'\" value='&lt;&lt First'>&nbsp;&nbsp;&nbsp;&nbsp;");
      int i = iFrom - iBlock;
      if (i < 0)
        i = 0;
      sbReturn.append("<input type=button onClick=\"parent.location='./?stAction=projects&t=12&do=xls&pk=");
      sbReturn.append(stPk);
      sbReturn.append("&parent=12&child=");
      sbReturn.append(stChild);
      sbReturn.append("&from=");
      sbReturn.append(i);
      sbReturn.append("'\" value='&lt; Previous'>&nbsp;&nbsp;&nbsp;&nbsp;");
      i = iFrom + iBlock;
      sbReturn.append("<input type=button onClick=\"parent.location='./?stAction=projects&t=12&do=xls&pk=");
      sbReturn.append(stPk);
      sbReturn.append("&parent=12&child=");
      sbReturn.append(stChild);
      sbReturn.append("&from=");
      sbReturn.append(i);
      sbReturn.append("'\" value='&gt; Next'>&nbsp;&nbsp;&nbsp;&nbsp;");
      int i2 = iMaxRecords / iBlock;
      i = i2 * iBlock;
      sbReturn.append("<input type=button onClick=\"parent.location='./?stAction=projects&t=12&do=xls&pk=");
      sbReturn.append(stPk);
      sbReturn.append("&parent=12&child=");
      sbReturn.append(stChild);
      sbReturn.append("&from=");
      sbReturn.append(i);
      sbReturn.append("'\" value='&gt;&gt; Last' >&nbsp;&nbsp;&nbsp;&nbsp;");
    } else
    {
      sbReturn.append("<td align=right>No records found: </td><td align=right>");
      String s1 = "";
      if ((stChild.equals("21") && (this.ebEnt.ebUd.getLoginPersonFlags() & 0x60) != 0) || // Only PM and PPM
        (stChild.equals("19") && (this.ebEnt.ebUd.getLoginPersonFlags() & 0xA0) != 0))  // Only PM and PPM
      {
        s1 = "<input type=button onClick=\"parent.location='./?stAction=projects&t=12&do=xls&pk=" + stPk + "&parent=12&child=" + stChild + "&from=0&a=insert&what=below&r=-1#row1'\" value='Insert New'>";
        sbReturn.append(s1);
      } else if (stChild.equals("34") && (this.ebEnt.ebUd.getLoginPersonFlags() & 0xF0) != 0)  // Test, everyone for now
      {
        s1 = "<input type=button onClick=\"parent.location='./?stAction=projects&t=12&do=xls&pk=" + stPk + "&parent=12&child=" + stChild + "&from=0&a=insert&what=below&r=-1#row1'\" value='Insert New'>";
        sbReturn.append(s1);
      }
      sbReturn.append("</td></tr>");
    }
    sbReturn.append("</td></tr></table></td></tr>");
    return sbReturn.toString();
  }

  private int getEnd(String stChild, String stPk, ResultSet rs1)
  {
    int iReturn = 0;
    try
    {
      if (stChild.equals("19"))
      {
        iReturn = this.ebEnt.dbDyn.ExecuteSql1n("select min(ReqId) from Requirements where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and ReqId > " + rs1.getString("ReqId") + " and ReqLevel <= " + rs1.getString("ReqLevel"));
        if (iReturn <= 0)
          iReturn = this.ebEnt.dbDyn.ExecuteSql1n("select (max(ReqId)+1) from Requirements where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
      } else if (stChild.equals("21"))
      {
        iReturn = this.ebEnt.dbDyn.ExecuteSql1n("select min(SchId) from Schedule where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and SchId > " + rs1.getString("SchId") + " and SchLevel <= " + rs1.getString("SchLevel"));
        if (iReturn <= 0)
          iReturn = this.ebEnt.dbDyn.ExecuteSql1n("select (max(SchId)+1) from Schedule where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
      } else
      {
        iReturn = this.ebEnt.dbDyn.ExecuteSql1n("select min(TstId) from Test where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and TstId > " + rs1.getString("TstId") + " and TstLevel <= " + rs1.getString("TstLevel"));
        if (iReturn <= 0)
          iReturn = this.ebEnt.dbDyn.ExecuteSql1n("select (max(TstId)+1) from Test where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
      }
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR getEnd " + e;
    }
    return iReturn;
  }

  private String editXlsField(String stValue)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    try
    {
      //this.rsFields
      String stStyle = getStyle(1.7);
      String s1 = "";
      switch (rsFields.getInt("nmDataType"))		//create editable fields for inline schedule and requirements
      {
        case 4:
          s1 = "<textarea name=f" + rsFields.getString("nmForeignId") + " id=f" + rsFields.getString("nmForeignId") + " rows=4 " + stStyle + ">" + stValue + "</textarea>";
          sbReturn.append(s1);
          break;
        default:
    	  if(rsFields.getString("nmForeignId").equals("231") && Integer.parseInt(this.ebEnt.ebUd.request.getParameter("child")) == 19){
    		  //drop down for req level 0-x+1
    		  s1 = "<select '" + stStyle + "' type=text name=f" + rsFields.getString("nmForeignId") + " id=f" + rsFields.getString("nmForeignId") + ">";
    		  for(int i=0; i<Integer.parseInt(stValue); i++){
    			  s1 += "<option value=\"" + i + "\">" + i + "</option>";
    		  }
    		  s1 += "<option value=\"" + stValue + "\" selected='selected'>" + stValue + "</option>" +
    		  	"<option value=\"" + (Integer.parseInt(stValue)+1) + "\">" + (Integer.parseInt(stValue)+1) + "</option>";
    		  s1 += "</select>";
              sbReturn.append(s1);
          }else if(rsFields.getString("stDbFieldName").equals("SchDone")){
        	  if(stValue.equals("Y"))
        		  s1 = "<input type='checkbox' checked='checked' name=f" + rsFields.getString("nmForeignId") + " id=f" + rsFields.getString("nmForeignId") + ">";
        	  else
        		  s1 = "<input type='checkbox' name=f" + rsFields.getString("nmForeignId") + " id=f" + rsFields.getString("nmForeignId") + ">";
        	  sbReturn.append(s1);
          }else{
            s1 = "<input '" + stStyle + "' maxsize=" + rsFields.getString("nmMaxBytes") + " type=text name=f" + rsFields.getString("nmForeignId") + " id=f" + rsFields.getString("nmForeignId") + " value=\"" + stValue + "\">";
            sbReturn.append(s1);
          }
          
          if(rsFields.getString("nmForeignId").equals("271")){
        	  //add hidden value for current level
        	  s1 = "<input type='hidden' name='stLvl' value=\"" + stValue + "\">";
        	  sbReturn.append(s1);
          }
          
          if (iSubmit == 0 && Integer.parseInt(this.ebEnt.ebUd.request.getParameter("child")) != 21)		//submit and cancel buttons for non-schedule
          {
            iSubmit++; // only do it once
            s1 = "<center><br><input type=submit name=savedataxls value='Save' onClick=\"return setSubmitId(9970);\">";
            sbReturn.append(s1);
            String stTemp = this.ebEnt.ebUd.request.getParameter("new");
            if (stTemp == null || stTemp.length() <= 0)
            {
              sbReturn.append("&nbsp;");
              sbReturn.append("<input type=submit name=savedataxls value='Cancel' onClick=\"return setSubmitId(8888);\">");
            }
            sbReturn.append("</center>");
          }

          break;
      }
      this.epsUd.epsEf.addValidation(rsFields.getInt("nmForeignId"));
    } catch (Exception e)
    {
      this.stError += "<br>ERROR editXlsField " + e;
    }
    return sbReturn.toString();
  }

  private String getStyle(double dReduce)
  {
    String stStyle = "";
    try
    {
      stStyle = rsFields.getString("stCustom");
      if (stStyle == null || stStyle.length() <= 0 || stStyle.equals("null"))
      {
        double d1 = this.epsUd.rsMyDiv.getDouble("PageWidthPx");
        double d2 = rsFields.getDouble("nmWidth") - dReduce; // Take 1 pct off for borders etc
        int iWidth = (int) ((d1 * d2) / 100);

        stStyle = " style='width:" + iWidth + "px;";
        String stAlign = rsFields.getString("stCustom");
        if (stAlign != null && stAlign.length() > 0)
          stStyle += "text-align:" + stAlign + ";";
        if (rsFields.getInt("nmDataType") == 1 || rsFields.getInt("nmDataType") == 31 || rsFields.getInt("nmDataType") == 5)
        {
          if (stAlign.length() <= 0 || stAlign.equals("null"))
            stStyle += "text-align:right;";
        }
        if (dReduce > 0)
          stStyle += "margin:1px;padding:1px;";
        stStyle += "'";
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR getStyle " + e;
    }
    return stStyle;
  }

  private void saveInline(String stChild, String stPk)
  {
    String stSql = "";
    String stSqlOld = "";
    String stValue = "";
    int rLvl = -1;
    try
    {
      String doneID = this.ebEnt.dbDyn.ExecuteSql1("SELECT nmForeignId FROM teb_fields WHERE stDbFieldName='SchDone'"); //id for schedule done checkbox
      ResultSet rsTable = this.ebEnt.dbDyn.ExecuteSql("SELECT * FROM teb_table where nmTableId=" + stChild);
      rsTable.absolute(1);
      stSqlOld = "select * from " + rsTable.getString("stDbTableName") + " ";
      xlsHeaders(stChild);
      stSql = "update " + rsTable.getString("stDbTableName") + " set ";
      int iCount = 0;
      for (int iF = 1; iF <= this.iMaxFields; iF++)
      {
        this.rsFields.absolute(iF);
        stValue = this.ebEnt.ebUd.request.getParameter("f" + this.rsFields.getString("nmForeignId"));
        if (stValue != null && !this.rsFields.getString("nmForeignId").equals(doneID))	//append post value to update query if not empty and not done checkbox
        {
          String stTemp = this.rsFields.getString("stValidation");
          if (stTemp != null && stTemp.length() > 0 && stTemp.toLowerCase().equals("d22"))
          {
            stValue = this.epsUd.validateD22(rsTable, this.rsFields, this.ebEnt.ebUd.request.getParameter("edit"), stValue, stPk, nmBaseline);
          } else if (stTemp != null && stTemp.length() > 0 && stTemp.toLowerCase().equals("d50"))
          {
            stValue = this.epsUd.validateD50(rsTable, this.rsFields, this.ebEnt.ebUd.request.getParameter("edit"), stValue, stPk, nmBaseline);
          } else if (stTemp != null && stTemp.length() > 0 && stTemp.toLowerCase().equals("d53"))
          {
            stValue = this.epsUd.validateD53(rsTable, this.rsFields, this.ebEnt.ebUd.request.getParameter("edit"), stValue, stPk, nmBaseline);
          } else if(this.rsFields.getString("stDbFieldName").equals("SchEstimatedEffort") && stChild.equals("21")){
        	  stValue = this.epsUd.validateEffort(this.rsFields, this.ebEnt.ebUd.request.getParameter("edit"), stValue, stPk, nmBaseline);
          }
          if (iCount > 0)
            stSql += ",";
          stSql += this.rsFields.getString("stDbFieldName") + "=" + this.ebEnt.dbDyn.fmtDbString(stValue);
          iCount++;
        
          //flag if low level task exceeds 40 #21
	      if(this.rsFields.getString("stDbFieldName").equals("SchEstimatedEffort") && stChild.equals("21")){
	    	  String sql = "SELECT * FROM schedule where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " order by RecId desc";
	          ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql(sql);
	          rs1.last();
	          int iSch = rs1.getRow();
	          int iLastLevel = -1;

	          for (int i = 1; i <= iSch; i++)
	          {
	            rs1.absolute(i);
	            if (iLastLevel == rs1.getInt("SchLevel") && this.ebEnt.ebUd.request.getParameter("edit").equals(rs1.getString("RecId"))){
	          	  // same level as before, therefore we are LAST
	              if(Integer.parseInt(stValue) > 40)
	            	  stSql += ",nmEffort40Flag=1";	//flag hours
	              else
	            	  stSql += ",nmEffort40Flag=0";	//unflag
	          	  break;
	            }
	            iLastLevel = rs1.getInt("SchLevel");
	          }
	      }
        
          if(this.rsFields.getString("stDbFieldName").equals("ReqLevel") && stChild.equals("19")){
        	  rLvl = Integer.parseInt(stValue);		//requirement level
          }
        }
      }
      
      //set done checkbox for tasks
      if(stChild.equals("21") && this.ebEnt.ebUd.request.getParameter("f" + doneID) != null){
    	  if (iCount > 0)
              stSql += ",";
          stSql += this.rsFields.getString("stDbFieldName") + "='Y'";
          iCount++;
      }else if(stChild.equals("21") && this.ebEnt.ebUd.request.getParameter("f" + doneID) == null){
    	  if (iCount > 0)
              stSql += ",";
          stSql += this.rsFields.getString("stDbFieldName") + "='N'";
          iCount++;
      }
      
      //build labor category string
  	  if(this.ebEnt.ebUd.request.getParameter("lc_0") != null && Integer.parseInt(this.ebEnt.ebUd.request.getParameter("child")) == 21 && Integer.parseInt(this.ebEnt.ebUd.request.getParameter("imax")) > 0){
  		  String lcfields = "";
	      for (int iR = 0; iR < Integer.parseInt(this.ebEnt.ebUd.request.getParameter("imax")); iR++){
	    	  if(iR == 0)
	    		  stSql += ", SchLaborCategories=";
	    	  else if(!this.ebEnt.ebUd.request.getParameter("lc_"+iR).equals("0"))		//don't add | to new row fields
	    		  lcfields += "|";
	    	  if(!this.ebEnt.ebUd.request.getParameter("lc_"+iR).equals("0"))	//skip the add new labor category row
	    		  lcfields += this.ebEnt.ebUd.request.getParameter("lc_"+iR) + "~" + this.ebEnt.ebUd.request.getParameter("nr_"+iR) + "~" + this.ebEnt.ebUd.request.getParameter("est_"+iR) + "~" + this.ebEnt.ebUd.request.getParameter("f9001_must_"+iR) + "~" + this.ebEnt.ebUd.request.getParameter("f9002_most_"+iR) + "~" + this.ebEnt.ebUd.request.getParameter("f9003_least_"+iR) + "~" + this.ebEnt.ebUd.request.getParameter("f9004_not_"+iR) + "~";
	      }
	      stSql += this.ebEnt.dbDyn.fmtDbString(lcfields);
      }
  	  
  	  
  	  //build dependencies string
  	  if(Integer.parseInt(this.ebEnt.ebUd.request.getParameter("child")) == 21 && !this.ebEnt.ebUd.request.getParameter("did_1").equals("") && Integer.parseInt(this.ebEnt.ebUd.request.getParameter("dmax")) > 0){
  		  String dfields = "";  		  
	      for (int d = 1; d <= Integer.parseInt(this.ebEnt.ebUd.request.getParameter("dmax")); d++){
	    	  if(d == 1){
	    		  stSql += ", SchDependencies=";
	    	  }else if(!this.ebEnt.ebUd.request.getParameter("did_"+d).equals("")){
	    		  dfields += ",";
	    	  }
	    	  
	    	  if(this.ebEnt.ebUd.request.getParameter("did_"+d) != null && !this.ebEnt.ebUd.request.getParameter("did_"+d).equals("") && !this.ebEnt.ebUd.request.getParameter("lag_"+d).equals("0") && !this.ebEnt.ebUd.request.getParameter("lag_"+d).equals("")){
	    		  dfields += this.ebEnt.ebUd.request.getParameter("did_"+d) + this.ebEnt.ebUd.request.getParameter("type_"+d) + "+" + this.ebEnt.ebUd.request.getParameter("lag_"+d) + "days";
	    		  linkMap(2, stPk, nmBaseline, stPk, Integer.parseInt(this.ebEnt.ebUd.request.getParameter("did_"+d)), stPk, Integer.parseInt(this.ebEnt.ebUd.request.getParameter("edit")), Double.parseDouble(this.ebEnt.ebUd.request.getParameter("lag_"+d)), this.ebEnt.ebUd.request.getParameter("type_"+d), 0, 0);
	    		  
	    		  
	    	  } else if(this.ebEnt.ebUd.request.getParameter("did_"+d) != null && !this.ebEnt.ebUd.request.getParameter("did_"+d).equals("")){
	    		  dfields += this.ebEnt.ebUd.request.getParameter("did_"+d) + this.ebEnt.ebUd.request.getParameter("type_"+d);
	    		  linkMap(2, stPk, nmBaseline, stPk, Integer.parseInt(this.ebEnt.ebUd.request.getParameter("did_"+d)), stPk, Integer.parseInt(this.ebEnt.ebUd.request.getParameter("edit")), 0, this.ebEnt.ebUd.request.getParameter("type_"+d), 0, 0);
	    	  }
	      }
	      stSql += this.ebEnt.dbDyn.fmtDbString(dfields);
      }
  	  
      //calc 1 level below if this level is not 0. order by desc and find first req level that matches
  	  if(Integer.parseInt(this.ebEnt.ebUd.request.getParameter("child")) == 19){
  		  //calculate parent level and add update parent id if not high level
  		  if(rLvl > 0){
	  		  //edit parent according to level
  			  int rId = this.ebEnt.dbDyn.ExecuteSql1n("SELECT ReqId FROM requirements WHERE RecId = " + this.ebEnt.ebUd.request.getParameter("edit"));
  			  stSql += ", ReqParentRecId=" + this.ebEnt.dbDyn.fmtDbString(this.ebEnt.dbDyn.ExecuteSql1n("SELECT RecId FROM requirements WHERE ReqId < " + rId + " AND ReqLevel=" + (rLvl-1) + " AND nmProjectId=" + stPk + " ORDER BY ReqId DESC")+"");
  		  }
  	  }
  
  	  stSql += " where RecId = " + this.ebEnt.ebUd.request.getParameter("edit") + " and nmBaseline=" + this.nmBaseline + " and nmProjectId=" + stPk;
      stSqlOld += " where RecId = " + this.ebEnt.ebUd.request.getParameter("edit") + " and nmBaseline=" + this.nmBaseline + " and nmProjectId=" + stPk;

      ResultSet rsOld = this.ebEnt.dbDyn.ExecuteSql(stSqlOld);
      this.ebEnt.dbDyn.ExecuteUpdate(stSql);
      
      ResultSet rsNew = this.ebEnt.dbDyn.ExecuteSql(stSqlOld);
      processScheduleRequirementCost(Integer.parseInt(this.ebEnt.ebUd.request.getParameter("edit")));		//calculate linked requirement costs
      this.epsUd.epsEf.addAuditTrail(rsTable, rsOld, rsNew, this.ebEnt.ebUd.request.getParameter("edit"), stPk, nmBaseline);
    } catch (Exception e)
    {
      this.stError += "<br>ERROR saveInline " + e;
    }
  }

  private String editFull(String stChild, String stPk)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    try
    {
      ResultSet rsTable = this.ebEnt.dbDyn.ExecuteSql("SELECT * FROM teb_table  where nmTableId=" + stChild);
      rsTable.absolute(1);
      String stSave = this.ebEnt.ebUd.request.getParameter("savedata");
      String stCancel = this.ebEnt.ebUd.request.getParameter("cancel");
      String stTemp = this.ebEnt.ebUd.request.getParameter("giSubmitId");
      String stMake = "";
      int giSubmitId = 0;
      if (stTemp != null && stTemp.length() > 0)
        giSubmitId = Integer.parseInt(stTemp);
      stTemp = this.ebEnt.ebUd.request.getParameter("giVar");
      int iDelete = -1;
      if (stTemp != null && stTemp.length() > 0 && !stTemp.equals("-1"))
      {
        iDelete = Integer.parseInt(stTemp);
        stSave = "and";
        //giSubmitId = 9990;
      }
      if (stSave != null || stCancel != null || iDelete >= 0)
      { // DO SAVEDATA()
        String stFrom = this.ebEnt.ebUd.request.getParameter("from");
        if (stFrom == null)
          stFrom = "0";
        String stParent = this.ebEnt.ebUd.request.getParameter("parent");
        if (stParent == null)
          stParent = "";
        String stLink = "?stAction=projects&t=" + this.ebEnt.ebUd.request.getParameter("t") + "&do=xls&pk=" + this.ebEnt.ebUd.request.getParameter("pk") + "&parent=" + stParent + "&child=" + stChild;
        if (stSave != null && stSave.length() > 0)
        {
          if (!stSave.toLowerCase().contains("and"))
          {
            ResultSet rsF = this.ebEnt.dbDyn.ExecuteSql("select * from teb_fields f, teb_epsfields ef where f.nmForeignId=ef.nmForeignId and f.nmTabId in (" + rsTable.getString("stTabList") + ") order by f.nmHeaderOrder, f.nmForeignId ");
            rsF.last();
            int iMaxF = rsF.getRow();
            this.epsUd.saveTable(rsTable, rsF, iMaxF, stPk, 0, 0);
          } else
          {
            String stPrj = "";
            int iBaseline = 0;
            String stR = "";

            switch (giSubmitId)
            {
              case 9974:
              case 9994: // Other Resources
                stPrj = this.ebEnt.ebUd.request.getParameter("pk");
                iBaseline = this.ebEnt.dbDyn.ExecuteSql1n("select CurrentBaseline from Projects where RecId=" + stPrj);
                stTemp = this.ebEnt.ebUd.request.getParameter("imax");
                if (stTemp != null && stTemp.length() > 0)
                {
                  int iMax = Integer.parseInt(stTemp);
                  String SchOtherResources = "";
                  for (int i = 0; i < iMax; i++)
                  {
                    if (i == iDelete)
                      continue;
                    String stCst = this.ebEnt.ebUd.request.getParameter("cst_" + i);
                    if (stCst != null && stCst.length() > 0 && !stCst.equals("0"))
                    {
                      if (SchOtherResources.length() > 0)
                        SchOtherResources += "|";
                      SchOtherResources += this.ebEnt.ebUd.request.getParameter("oth_" + i) + "~";
                      SchOtherResources += this.ebEnt.ebUd.request.getParameter("cst_" + i) + "~";
                    }
                  }
                  this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchOtherResources = "
                    + this.ebEnt.dbDyn.fmtDbString(SchOtherResources)
                    + " where nmProjectId=" + stPrj
                    + " and nmBaseline=" + iBaseline + " and RecId=" + this.ebEnt.ebUd.request.getParameter("r"));
                }
                stMake = "oth";
                break;
              case 9973:
              case 9993: // Inventory
                stPrj = this.ebEnt.ebUd.request.getParameter("pk");
                iBaseline = this.ebEnt.dbDyn.ExecuteSql1n("select CurrentBaseline from Projects where RecId=" + stPrj);
                stTemp = this.ebEnt.ebUd.request.getParameter("imax");
                if (stTemp != null && stTemp.length() > 0)
                {
                  int iMax = Integer.parseInt(stTemp);
                  String SchInventory = "";
                  for (int i = 0; i < iMax; i++)
                  {
                    String stQty = this.ebEnt.ebUd.request.getParameter("qty_" + i);
                    if (i == iDelete)
                    { // Add QTY
                      if (stQty != null && stQty.length() > 0 && !stQty.equals("0"))
                        this.ebEnt.dbDyn.ExecuteUpdate("update Inventory set"
                          + " DateLastUsed = now(), ProjectLastUsing = " + stPrj
                          + " where RecId =" + this.ebEnt.ebUd.request.getParameter("inv_" + i));
                      this.ebEnt.dbDyn.ExecuteUpdate("delete from teb_refinventory where nmProjectId=" + stPrj
                        + " and nmInventoryId=" + this.ebEnt.ebUd.request.getParameter("inv_" + i)
                        + " and nmTaskId=" + stPk);
                      continue;
                    }
                    if (stQty != null && stQty.length() > 0 && !stQty.equals("0"))
                    {
                      int iQty = 0;
                      try
                      {
                        int iMissing=0;
                        iQty = Integer.parseInt(this.ebEnt.ebUd.request.getParameter("qty_" + i));
                        if (iQty > 0)
                        {
                          ResultSet rsAvail = this.ebEnt.dbDyn.ExecuteSql("select Quantity,sum(ri.nmQty) as used"
                            + " from Inventory i left join teb_refinventory ri on i.RecId=ri.nmInventoryId"
                            + " and ! ( ri.nmProjectId=" + stPrj + " and ri.nmTaskId=" + stPk + " )"
                            + " where i.RecId=" + this.ebEnt.ebUd.request.getParameter("inv_" + i));
                          rsAvail.absolute(1);
                          int iAvail = rsAvail.getInt("Quantity") - rsAvail.getInt("used");
                          if (iAvail < iQty)
                          {
                            this.ebEnt.ebUd.setPopupMessage("Not enough (" + iAvail + ") resources available. "
                              + "You requested (" + iQty + ")");
                            iMissing = iQty - iAvail;
                          }
                          this.ebEnt.dbDyn.ExecuteUpdate("replace into teb_refinventory"
                            + " (nmProjectId,nmInventoryId,nmTaskId,nmQty,dtLastChanged,nmMissing)values"
                            + " (" + stPrj + "," + this.ebEnt.ebUd.request.getParameter("inv_" + i) + "," + stPk
                            + "," + iQty + ",now(),"+iMissing+")");
                          this.ebEnt.dbDyn.ExecuteUpdate("update Inventory set"
                            + " DateLastUsed = now(), ProjectLastUsing = " + stPrj
                            + " where RecId =" + this.ebEnt.ebUd.request.getParameter("inv_" + i));
                        }
                      } catch (Exception e)
                      {
                      }
                      if (SchInventory.length() > 0)
                        SchInventory += "|";
                      SchInventory += this.ebEnt.ebUd.request.getParameter("inv_" + i) + "~";
                      SchInventory += iQty + "~";
                    }
                  }
                  this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchInventory = "
                    + this.ebEnt.dbDyn.fmtDbString(SchInventory)
                    + " where nmProjectId=" + stPrj
                    + " and nmBaseline=" + iBaseline + " and RecId=" + this.ebEnt.ebUd.request.getParameter("r"));
                }
                stMake = "inv";
                break;
              case 9972:
              case 9992: // Successors
                stTemp = this.ebEnt.ebUd.request.getParameter("imax");
                stPrj = this.ebEnt.ebUd.request.getParameter("pk");
                iBaseline = this.ebEnt.dbDyn.ExecuteSql1n("select CurrentBaseline from Projects where RecId=" + stPrj);
                stR = this.ebEnt.ebUd.request.getParameter("r");

                String stSql = "delete from teb_link where nmProjectId=" + stPrj + " and nmBaseline=" + iBaseline + " and nmLinkFlags=2 "
                  + "and nmFromId=" + stR + " and nmFromProject=nmProjectId";
                this.ebEnt.dbDyn.ExecuteUpdate(stSql);
                String stSuc = "";
                int iForeign = 0;
                if (stTemp != null && stTemp.length() > 0)
                {
                  int iMax = Integer.parseInt(stTemp);
                  int iFrom = Integer.parseInt(stR);
                  for (int i = 1; i <= iMax; i++)
                  {
                    if (i != iDelete)
                    {
                      String stToProject = this.ebEnt.ebUd.request.getParameter("prj_" + i);
                      String stToId = this.ebEnt.ebUd.request.getParameter("id_" + i);
                      if (stToProject != null && stToProject.length() > 0 && stToId != null && stToId.length() > 0)
                      {
                        ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql("select * from Projects p left join Schedule s"
                          + " on s.nmProjectId=p.RecId and s.nmBaseline=p.CurrentBaseline and s.RecId=" + stToId
                          + " where  p.RecId=" + stToProject);
                        rs1.absolute(1);
                        if (rs1.getString("SchFlags") == null || (rs1.getInt("SchFlags") & 0x10) == 0)
                        {
                          if (rs1.getString("SchFlags") == null)
                            this.ebEnt.ebUd.setPopupMessage("Project: " + rs1.getString("ProjectName")
                              + " Task ID: " + stToId + " not found");
                          else
                            this.ebEnt.ebUd.setPopupMessage("Project: " + rs1.getString("ProjectName")
                              + " Task ID: " + stToId + " is not a low level schedule");
                        } else
                        {
                          int iTo1 = 0;
                          if (stToId != null && stToId.length() > 0)
                            iTo1 = Integer.parseInt(stToId);
                          String stLag = this.ebEnt.ebUd.request.getParameter("lag_" + i);
                          double dLag = 0;
                          if (stLag != null && stLag.length() > 0)
                            dLag = Double.parseDouble(stLag);
                          String stType = this.ebEnt.ebUd.request.getParameter("type_" + i);
                          if (stType.equals("sf")) // we reverse FINISH START to to FS
                          {
                            stType = "fs";
                            linkMap(2, stPrj, iBaseline, stToProject, iTo1, stPrj, iFrom, dLag, stType, 0, 0);
                          } else
                          {
                            if (stSuc.length() > 0)
                              stSuc += ", ";
                            if (!stToProject.equals(stPrj))
                            {
                              iForeign++;
                              stSuc += stToProject + ".";
                            }
                            stSuc += iTo1;
                            linkMap(2, stPrj, iBaseline, stPrj, iFrom, stToProject, iTo1, dLag, stType, 0, 0);
                          }
                        }
                      } else
                      {
                        if (i != iMax) // Only last one may have missing ID
                          this.ebEnt.ebUd.setPopupMessage("Invalid Project and/or ID " + i);
                      }
                    }
                  }
                  if (iForeign > 0)
                    stTemp = ", SchFlags = (SchFlags | 0x) ";
                  else
                    stTemp = "";

                  this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchSuccessors = "
                    + this.ebEnt.dbDyn.fmtDbString(stSuc) + stTemp
                    + " where nmProjectId=" + this.ebEnt.ebUd.request.getParameter("pk")
                    + " and nmBaseline=" + nmBaseline + " and RecId=" + this.ebEnt.ebUd.request.getParameter("r"));
                }
                stMake = "suc";
                break;
              case 9971:
              case 9991: // Dependents
                stTemp = this.ebEnt.ebUd.request.getParameter("imax");
                stPrj = this.ebEnt.ebUd.request.getParameter("pk");
                iBaseline = this.ebEnt.dbDyn.ExecuteSql1n("select CurrentBaseline from Projects where RecId=" + stPrj);
                stR = this.ebEnt.ebUd.request.getParameter("r");
                String stDep = "";
                stSql = "delete from teb_link where nmProjectId=" + stPrj + " and nmBaseline=" + iBaseline + " and nmLinkFlags=2 "
                  + "and nmToId=" + stR + " and nmToProject=nmProjectId";
                this.ebEnt.dbDyn.ExecuteUpdate(stSql);
                if (stTemp != null && stTemp.length() > 0)
                {
                  int iMax = Integer.parseInt(stTemp);
                  int iTo = Integer.parseInt(stR);
                  for (int i = 1; i <= iMax; i++)
                  {
                    if (i != iDelete)
                    {
                      String stFromProject = this.ebEnt.ebUd.request.getParameter("prj_" + i);
                      String stFromId = this.ebEnt.ebUd.request.getParameter("id_" + i);
                      if (stFromProject != null && stFromProject.length() > 0 && stFromId != null && stFromId.length() > 0)
                      {
                        ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql("select * from Projects p left join Schedule s"
                          + " on s.nmProjectId=p.RecId and s.nmBaseline=p.CurrentBaseline and s.RecId=" + stFromId
                          + " where  p.RecId=" + stFromProject);
                        rs1.absolute(1);
                        if (rs1.getString("SchFlags") == null || (rs1.getInt("SchFlags") & 0x10) == 0)
                        {
                          if (rs1.getString("SchFlags") == null)
                            this.ebEnt.ebUd.setPopupMessage("Project: " + rs1.getString("ProjectName")
                              + " Task ID: " + stFromId + " not found");
                          else
                            this.ebEnt.ebUd.setPopupMessage("Project: " + rs1.getString("ProjectName")
                              + " Task ID: " + stFromId + " is not a low level schedule");
                        } else
                        {
                          int iFrom = 0;
                          if (stFromId != null && stFromId.length() > 0)
                            iFrom = Integer.parseInt(stFromId);
                          String stLag = this.ebEnt.ebUd.request.getParameter("lag_" + i);
                          double dLag = 0;
                          if (stLag != null && stLag.length() > 0)
                            dLag = Double.parseDouble(stLag);
                          String stType = this.ebEnt.ebUd.request.getParameter("type_" + i);
                          if (stType.equals("sf")) // we reverse START TO FINISH to FS
                          {
                            stType = "fs";
                            linkMap(2, stPrj, iBaseline, stPrj, iTo, stFromProject, iFrom, dLag, stType, 0, 0);
                          } else
                          {
                            if (stDep.length() > 0)
                              stDep += ", ";
                            if (!stFromProject.equals(stPrj))
                              stDep += stFromProject + ".";
                            stDep += iFrom;
                            linkMap(2, stPrj, iBaseline, stFromProject, iFrom, stPrj, iTo, dLag, stType, 0, 0);
                          }
                        }
                      } else
                      {
                        if (i != iMax) // Only last one may have missing ID
                          this.ebEnt.ebUd.setPopupMessage("Invalid Project and/or ID " + i);
                      }
                    }
                  }
                  //update task dependency field and remove any fixed dates
                  this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFixedFinishDate = null, SchFixedStartDate = null, SchDependencies = "
                    + this.ebEnt.dbDyn.fmtDbString(stDep)
                    + " where nmProjectId=" + this.ebEnt.ebUd.request.getParameter("pk")
                    + " and nmBaseline=" + nmBaseline + " and RecId=" + this.ebEnt.ebUd.request.getParameter("r"));
                }
                stMake = "dep";
                break;
              case 9970:
              case 9990:
                stTemp = this.ebEnt.ebUd.request.getParameter("imax");
                if (stTemp != null && stTemp.length() > 0)
                {
                  int iMax = Integer.parseInt(stTemp);
                  String SchLaborCategories = "";
                  double dEffort = 0;
                  for (int i = 0; i < iMax; i++)
                  {
                    if (i == iDelete)
                      continue;
                    String stLc = this.ebEnt.ebUd.request.getParameter("lc_" + i);
                    if (stLc != null && !stLc.equals("0"))
                    {
                      if (SchLaborCategories.length() > 0)
                        SchLaborCategories += "|";
                      SchLaborCategories += this.ebEnt.ebUd.request.getParameter("lc_" + i) + "~";
                      SchLaborCategories += this.ebEnt.ebUd.request.getParameter("nr_" + i) + "~";
                      stTemp = this.ebEnt.ebUd.request.getParameter("est_" + i);
                      double d = Double.parseDouble(stTemp);
                      double dMax = this.epsUd.rsMyDiv.getDouble("MaximumTaskHours");
                      int iNr = Integer.parseInt(this.ebEnt.ebUd.request.getParameter("nr_" + i));
                      NumberFormat df = new DecimalFormat("#0.0");
                      double d2 = d;
                      if (iNr > 0)
                        d2 = d / iNr;
                      if (d2 > dMax)
                      {
                        this.ebEnt.ebUd.setPopupMessage("Estimate (" + df.format(d2) + ") exceeds maximum task hours (" + df.format(this.epsUd.rsMyDiv.getDouble("MaximumTaskHours")) + ")");
                      }
                      dEffort += d;
                      SchLaborCategories += stTemp + "~";
                      SchLaborCategories += this.ebEnt.ebUd.request.getParameter("f9001_must_" + i) + "~";
                      SchLaborCategories += this.ebEnt.ebUd.request.getParameter("f9002_most_" + i) + "~";
                      SchLaborCategories += this.ebEnt.ebUd.request.getParameter("f9003_least_" + i) + "~";
                      SchLaborCategories += this.ebEnt.ebUd.request.getParameter("f9004_not_" + i) + "~";
                    }
                  }
                  this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchLaborCategories = "
                    + this.ebEnt.dbDyn.fmtDbString(SchLaborCategories) + ",SchEstimatedEffort=" + dEffort
                    + " where nmProjectId=" + this.ebEnt.ebUd.request.getParameter("pk")
                    + " and nmBaseline=" + nmBaseline + " and RecId=" + this.ebEnt.ebUd.request.getParameter("r"));
                }
                stMake = "lc";
                break;
            }

            if (giSubmitId >= 9980 && giSubmitId <= 9990)
            {
              this.ebEnt.ebUd.setRedirect(stLink + "&from=" + stFrom + "&a=editfull&r=" + this.ebEnt.ebUd.request.getParameter("r") + "&make=" + stMake);
              return "."; // on return need data to exit loop, main redirect has higher priority, (to remove EDIT tags)---------->
            }
          }
          if (giSubmitId >= 9980 && giSubmitId < 9999)
            return this.editMake(stChild, stPk);
          
          processScheduleRequirementCost(Integer.parseInt(this.ebEnt.ebUd.request.getParameter("r")));		//calculate requirement costs linked to this task
        }
        if (giSubmitId == 0 || giSubmitId == 9999 || stCancel != null)
        {
          this.ebEnt.ebUd.setRedirect(stLink + "&from=" + stFrom + "#row" + this.ebEnt.ebUd.request.getParameter("r"));
          return "."; // on return need data to exit loop, main redirect has higher priority, (to remove EDIT tags)---------->
        }
      }
      if (giSubmitId != 8888) // 9970-9979 is Save and Exit; 
      {
        stTemp = this.ebEnt.ebUd.request.getParameter("make");
        if ((giSubmitId <= 0 && stTemp != null && stTemp.length() > 0) || (giSubmitId >= 9980 && giSubmitId <= 9990))
        {
          return this.editMake(stChild, stPk);
        }
      }
      
      String s1 = "<center><table><tr><td valign=middle><h2>" + this.rsProject.getString("ProjectName") + "</h2>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>";
      sbReturn.append(s1);
      if (stChild.equals("21"))
      {
        sbReturn.append("<td align=left><b>");
        sbReturn.append(this.epsUd.epsEf.fullSchTitle(stPk, this.stPk, this.nmBaseline));
        sbReturn.append("</b><br>&nbsp;</td>");
      }else if (stChild.equals("19"))
      {
        sbReturn.append("<td align=left><b>");
        sbReturn.append(this.epsUd.epsEf.fullReqTitle(stPk, this.stPk, this.nmBaseline));
        sbReturn.append("</b><br>&nbsp;</td>");
      }
      sbReturn.append("</tr></table>");
      sbReturn.append(this.epsUd.editTable(rsTable, stPk));

    } catch (Exception e)
    {
      stError += "<BR>ERROR editFull child: " + stChild + " " + e;
    }
    return sbReturn.toString();
  }

  public String xlsAnalyze()
  {
    String stChild = this.ebEnt.ebUd.request.getParameter("child2");
    if (stChild == null)
      stChild = "";
    StringBuilder sbReturn = new StringBuilder(5000);
    if (this.ebEnt.ebUd.request.getParameter("pk") != null)
    {
      sbReturn.append("<center><h1>Project Analyzer</h1><table border=1>");
      sbReturn.append("<tr><th>Prj</th><th>Module</th><th>Verify</th><th colspan=2>Comment/Output</th><th>Rslt</th><th>#db</th><th>Time</th></tr>");
    }
    try
    {
      this.iMaxPath = 0;
      if (stChild.length() <= 0 || stChild.equals("19"))
        sbReturn.append(analyzeRequirements("19"));
      if (stChild.length() <= 0 || stChild.equals("21"))
        sbReturn.append(analyzeSchedules("21"));
      if (stChild.length() <= 0 || stChild.equals("34"))
        sbReturn.append(analyzeTest("34"));
      if (stChild.length() <= 0)
      {
        sbReturn.append(analyzeLink());
        /*sbReturn.append(processCriticalPath());*/
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR xlsAnalyze " + e;
    }
    this.ebEnt.dbDyn.ExecuteUpdate("update teb_baseline set nmAnalyzeStatus=" + this.iAnalyzeStatus + ",dtLastAnalyze=now(),"
      + "stAnalyzeReport=" + this.ebEnt.dbDyn.fmtDbString(sbReturn.toString()) + " where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline);
    if (this.ebEnt.ebUd.request.getParameter("pk") != null)
    {
      String s1 = "";
      sbReturn.append("</table><br>&nbsp;<br><center>");
      if (stChild.length() <= 0 && this.iAnalyzeStatus == 0)
      {
        s1 = "<input type=button onClick=\"parent.location='" + stGoBack + "&pk=" + stPk + "&do=approve'\" value='Approve'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        sbReturn.append(s1);
      }
      s1 = "<input type=button onClick=\"parent.location='" + stGoBack + "&pk=" + stPk + "&do=edit'\" value='Go Back to Projects'>";
      sbReturn.append(s1);
      if (stChild.length() <= 0 && this.iAnalyzeStatus == 0)
      {
        s1 = "<br>&nbsp;<br><b>Note:</b> Selecting the \"Approve\" button will \"freeze\" the existing baseline and will be used for all reports accessing this baseline's attributes.  Without \"approval\" the new baseline will be in \"EDIT\" mode and will not be used for allocation until \"Approved.\"  Clicking on the hyperlink will open a sequential list of requirements or schedule tasks with issues.<br>";
        sbReturn.append(s1);
      }
    }
    return sbReturn.toString();
  }

  private String analyzeTest(String stChild)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    try
    {
      //sbReturn.append( "<h1>TODO analyzeTest</h1>";
    } catch (Exception e)
    {
      this.stError += "<br>ERROR analyzeTest " + e;
    }

    return sbReturn.toString();
  }

  private String analyzeRequirements(String stChild)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    String stSql = "";
    int iCount = 0;
    try
    {
      String stTemp = this.ebEnt.ebUd.request.getParameter("pk");
      if (stTemp != null && stTemp.length() > 0)
        stPk = stTemp;
      stGoBack = "?stAction=projects&t=12";
      this.rsProject = this.ebEnt.dbDyn.ExecuteSql("select * from Projects where RecId=" + stPk);
      this.rsProject.absolute(1);
      nmBaseline = this.rsProject.getInt("CurrentBaseline");

      sbReturn.append("<tr>");
      sbReturn.append("<td align=right>" + stPk + "</td>");
      sbReturn.append("<td>Requirements</td>");
      sbReturn.append("<td>ID's and Levels</td>");
      //ReqFlags  0x1 = Hidden, 0x2=Show next item hidden, 0x4= isParent, 0x8 is first child, 0x10 is Lowest
      // 1) reset all flags
      this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqFlags=0 where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline);

      // 2) set parent/child flags
      stSql = "SELECT * FROM Requirements where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " order by ReqId;";
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      String stRecIdList = "";
      String stRecIdList4 = "";
      int[] aRecId = new int[100];
      int iReq = rs.getRow();
      if (iReq >= 1)
      {
        sbReturn.append("<td>Found</td><td valign=top align=right>" + iReq + "</td>");
        int iLastLevel = 0;
        int ReqFlags = 0;
        iCount = 0;
        for (int iR = 1; iR <= iReq; iR++)
        {
          rs.absolute(iR);
          ReqFlags = 0;
          aRecId[rs.getInt("Reqlevel")] = rs.getInt("RecId"); // Save this RecId on this level
          if (iLastLevel < rs.getInt("Reqlevel"))
          {
            if (stRecIdList.length() > 0)
              stRecIdList += ",";
            stRecIdList += rs.getInt("RecId");

            if (stRecIdList4.length() > 0)
              stRecIdList4 += ",";
            stRecIdList4 += iLastLevel;
          } /* else if (iLastLevel > rs.getInt("Reqlevel"))
          {
          if (stRecIdList4.length() > 0)
          stRecIdList4 += ",";
          stRecIdList4 += rs.getInt("RecId");
          }*/
          if ((iAnalyzeStatus & 0x2) != 0 || iR != rs.getInt("ReqId"))
          {
            iCount++;
            iAnalyzeStatus |= 0x2; // out of seq
            this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqId=" + iR + " where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId=" + rs.getInt("RecId"));
          }
          if (rs.getInt("Reqlevel") > 0)
          {
            if (aRecId[rs.getInt("Reqlevel") - 1] != rs.getInt("ReqParentRecId"))
            {
              this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set "
                + "ReqParentRecId=" + aRecId[rs.getInt("Reqlevel") - 1] + " "
                + "where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId=" + rs.getInt("RecId"));
            }
          }
          iLastLevel = rs.getInt("Reqlevel");
        }
        //ReqFlags |= 0x8; // This is FRIST child
        if (stRecIdList.length() > 0)
          this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqFlags=(ReqFlags | 0x8 ) where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId in (" + stRecIdList + ")");
        //ReqFlags |= 0x4; // This is a parent
        if (stRecIdList4.length() > 0)
          this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqFlags=(ReqFlags | 0x4) where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId in (" + stRecIdList4 + ")");
        rs.close();
        // 3) Set LOW LEVEL inidcator
        stSql = "SELECT * FROM Requirements where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " order by ReqId desc";
        ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql(stSql);
        rs1.last();
        iReq = rs1.getRow();
        ReqFlags = 0x10;
        iLastLevel = -1;
        stRecIdList = "";
        for (int iR = 1; iR <= iReq; iR++)
        {
          rs1.absolute(iR);

          if (iLastLevel == rs1.getInt("Reqlevel")) // same level as before, therefore we are LAST
            ReqFlags = 0x10;
          else if (iLastLevel > rs1.getInt("Reqlevel"))
            ReqFlags = 0;
          else if (iLastLevel < rs1.getInt("Reqlevel"))
            ReqFlags = 0x10;

          if (iR == 1 || (ReqFlags == 0x10))
          {
            ReqFlags = 0x10;
            if (stRecIdList.length() > 0)
              stRecIdList += ",";
            stRecIdList += rs1.getString("RecId");
          }
          iLastLevel = rs1.getInt("Reqlevel");
        }
        this.ebEnt.dbDyn.ExecuteUpdate("update Requirements set ReqFlags = ( ReqFlags | 0x10 ) where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId in (" + stRecIdList + ")");
        if ((iAnalyzeStatus & 0x2) != 0 || iCount > 0)
        {
          iAnalyzeStatus |= 0x2;
          sbReturn.append("<td style='color:blue;'>fixed ");
          sbReturn.append(iCount);
          sbReturn.append("</td>");
        } else
          sbReturn.append("<td style='color:green;'>ok</td>");
        this.ebEnt.dbDyn.ExecuteUpdate("update Projects set nmReq=" + iReq + " where RecId=" + stPk);
      } else
      {
        sbReturn.append("<td colspan=2 style='color:red;'>No requirements found</td><td style='color:red;'>fail</td>");
        iAnalyzeStatus |= 0x1;
      }
      // 4) Process Low-leves, Rollups, check LC
      // in Req -> must get from Schecule
      sbReturn.append(getElapsed());
    } catch (Exception e)
    {
      this.stError += "<br>ERROR analyzeRequirements " + e;
    }
    return sbReturn.toString();
  }

  private String analyzeSchedules(String stChild)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    String stSql = "";
    int iCount = 0;
    try
    {
      String stTemp = this.ebEnt.ebUd.request.getParameter("pk");
      if (stTemp != null && stTemp.length() > 0)
        stPk = stTemp;
      stGoBack = "?stAction=projects&t=12";
      this.rsProject = this.ebEnt.dbDyn.ExecuteSql("select * from Projects where RecId=" + stPk);
      this.rsProject.absolute(1);
      nmBaseline = this.rsProject.getInt("CurrentBaseline");

      sbReturn.append("<tr>");
      sbReturn.append("<td align=right>" + stPk + "</td>");
      sbReturn.append("<td>Schedules</td>");
      sbReturn.append("<td>ID's and Levels</td>");
      /* 1) reset all flags except permanent
      0x1	Hide This item
      0x2	CP=Critical Path
      0x4	is parent
      0x8	is first child
      0x10	is lowest level / leaf leve
      0x20	F=Fixed Date
      0x40	L=Late Task from Original Schedule
      0x80	PLA=Permanent Labor Assignment
      0x100	FD = Foreign Dependency
      0x1000	Completed
      0x2000	D=Deliverable
      0x4000	M=Milestone
      0x8000	Reserved Permanent Flag*/
      this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags= (SchFlags & ~0x0FFF) where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline);

      // 2) set parent/child flags
      stSql = "SELECT * FROM Schedule where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " order by SchId;";
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      String stRecIdList = "";
      String stRecIdList4 = "";
      int iSch = rs.getRow();
      if (iSch >= 1)
      {
        sbReturn.append("<td>Found</td><td valign=top align=right>" + iSch + "</td>");
        int iLastLevel = 0;
        int iLastId = 0;
        int SchFlags = 0;
        iCount = 0;
        int[] aRecId = new int[100];

        for (int iR = 1; iR <= iSch; iR++)
        {
          rs.absolute(iR);
          SchFlags = 0;
          aRecId[rs.getInt("Schlevel")] = rs.getInt("RecId"); // Save this RecId on this level
          if (iLastLevel < rs.getInt("Schlevel"))
          {
            if (stRecIdList.length() > 0)
              stRecIdList += ",";
            stRecIdList += rs.getInt("RecId");
            if (stRecIdList4.length() > 0)
              stRecIdList4 += ",";
            stRecIdList4 += iLastId;
          }/* else if (iLastLevel > rs.getInt("Schlevel"))
          {
          if (stRecIdList4.length() > 0)
          stRecIdList4 += ",";
          stRecIdList4 += rs.getInt("RecId");
          }*/
          if ((iAnalyzeStatus & 0x8) != 0 || iR != rs.getInt("SchId"))
          {
            iCount++;
            iAnalyzeStatus |= 0x8; // out of seq
            this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchId=" + iR + " where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId=" + rs.getInt("RecId"));
          }
          if (rs.getInt("Schlevel") > 0)
          {
            if (aRecId[rs.getInt("Schlevel") - 1] != rs.getInt("SchParentRecId"))
            {
              this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set "
                + "SchParentRecId=" + aRecId[rs.getInt("Schlevel") - 1] + " "
                + "where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId=" + rs.getInt("RecId"));
            }
          }
          iLastLevel = rs.getInt("Schlevel");
          iLastId = rs.getInt("RecId");
        }
        //SchFlags |= 0x8; // This is a child
        if (stRecIdList.length() > 0)
          this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags=(SchFlags | 0x8) where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId in (" + stRecIdList + ")");
        //SchFlags |= 0x4; // This is a parent
        if (stRecIdList4.length() > 0)
          this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags=(SchFlags | 0x4) where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId in (" + stRecIdList4 + ")");
        rs.close();
        // 3) Set LOW LEVEL inidcator
        stSql = "SELECT * FROM Schedule where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " order by SchId desc";
        ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql(stSql);
        rs1.last();
        iSch = rs1.getRow();
        SchFlags = 0x10;
        iLastLevel = -1;
        stRecIdList = "";
        int[] aHours = new int[100];
        double[] aCost = new double[100];
        double dCost = 0;

        for (int i = 0; i < aHours.length; i++)
        {
          aHours[i] = 0;
          aCost[i] = 0;
        }
        for (int iR = 1; iR <= iSch; iR++)
        {
          rs1.absolute(iR);
          if (iLastLevel == rs1.getInt("Schlevel")){ // same level as before, therefore we are LAST
            SchFlags = 0x10;
          } else if (iLastLevel > rs1.getInt("Schlevel"))
            SchFlags = 0;
          else if (iLastLevel < rs1.getInt("Schlevel"))
            SchFlags = 0x10;
          if (iR == 1 || (SchFlags == 0x10))
          {
            dCost = this.epsUd.calculateScheduleCost(rs1);
            for (int i = 0; i <= rs1.getInt("Schlevel"); i++)
              aHours[i] += rs1.getInt("SchEstimatedEffort");
            for (int i = 0; i <= rs1.getInt("Schlevel"); i++)
              aCost[i] += dCost;
            SchFlags = 0x10;
            if (stRecIdList.length() > 0)
              stRecIdList += ",";
            stRecIdList += rs1.getString("RecId");
            this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchCost=" + dCost + ", SchFlags = ( SchFlags | 0x10 )"
              + " where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId =" + rs1.getInt("RecId"));
          } else
          {
            // Rollup Hours
            //if (aHours[rs1.getInt("Schlevel")] != rs1.getInt("SchEstimatedEffort"))
            this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchEstimatedEffort=" + aHours[rs1.getInt("Schlevel")]
              + ",SchCost=" + aCost[rs1.getInt("Schlevel")]
              + " where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId =" + rs1.getInt("RecId"));
            dCost = aCost[rs1.getInt("Schlevel")];
            for (int i = rs1.getInt("Schlevel"); i < aHours.length; i++)
            {
              aHours[i] = 0;
              aCost[i] = 0;
            }
          }
          iLastLevel = rs1.getInt("Schlevel");
        }
        //this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set  SchFlags = ( SchFlags | 0x10 ) where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId in (" + stRecIdList + ")");
        if ((iAnalyzeStatus & 0x8) != 0 || iCount > 0)
        {
          this.iAnalyzeStatus |= 0x8;
          sbReturn.append("<td style='color:blue;'>fixed " + iCount + "</td>");
        } else
          sbReturn.append("<td style='color:green;'>ok</td>");
      } else
      {
        sbReturn.append("<td colspan=2 style='color:red;'>No Schedules found</td><td style='color:red;'>fail</td>");
        iAnalyzeStatus |= 0x4;
      }
      // 4) Process Low-leves, Rollups, check LC
      // in Sch -> must get from Schecule
      this.ebEnt.dbDyn.ExecuteUpdate("update Projects set nmSch=" + iSch + " where RecId=" + stPk);
      sbReturn.append(this.getElapsed());
      
      int effortFlag = 0;
      //analyze low level hours
      stSql = "SELECT * FROM Schedule where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " order by SchId desc";
      ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs1.last();
      iSch = rs1.getRow();
      int iLastLevel = -1;
      stRecIdList = "";

      for (int iR = 1; iR <= iSch; iR++)
      {
        rs1.absolute(iR);
        if (iLastLevel == rs1.getInt("Schlevel") && rs1.getInt("SchEstimatedEffort") > 40){ // same level as before, therefore we are LAST
          effortFlag++;
          this.ebEnt.dbDyn.ExecuteUpdate("UPDATE Schedule SET nmEffort40Flag=1 WHERE nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecID="+rs1.getInt("RecId"));
        }else{
        	this.ebEnt.dbDyn.ExecuteUpdate("UPDATE Schedule SET nmEffort40Flag=0 WHERE nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecID="+rs1.getInt("RecId"));
        }
        iLastLevel = rs1.getInt("Schlevel");
      }
	  sbReturn.append("<tr>");
      sbReturn.append("<td align=right>" + stPk + "</td>");
      sbReturn.append("<td>Schedules</td>");
      sbReturn.append("<td>Effort</td>");

      if(effortFlag > 0){
    	  sbReturn.append("<td style='color:red;'>Low Level Tasks Exceeding 40 Hours</td><td valign=top align=right>" + effortFlag + "</td><td style='color:red;'>fail</td>");
      }else{
    	  sbReturn.append("<td>Complete</td><td>"+iSch+"</td><td style='color:green;'>ok</td>");
      }
      sbReturn.append(this.getElapsed());
    } catch (Exception e)
    {
      this.stError += "<br>ERROR analyzeSchedule " + e;
    }
    return sbReturn.toString();
  }

  public String epsLoadImport()
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    String stAction = "";
    String stSql = "";
    int iHoliday = 0;
    int iSchedule = 0;
    int iRecId = 0;
    int iAssignLaborCategoy = 0;
    try
    {
      long startTime = System.nanoTime();
      stAction = this.ebEnt.ebUd.request.getParameter("submit");
      String ProjectStartDate1 = "";
      String ProjectProjectStartDate = "";
      if (stAction != null && stAction.equals("LOAD"))
      {
        sbReturn.append("<br>DATA FOR: ");
        String[] aLines;
        String stTa = this.ebEnt.ebUd.request.getParameter("ta");
        if (stTa != null)
        {
          stTa = this.ebEnt.ebUd.convertApostrophe(stTa.trim());
          aLines = stTa.split("\n");
          int iTable = 0;
          for (int iL = 0; iL
            < aLines.length; iL++)
          {
            if (aLines[iL].trim().startsWith("::"))
            {
              iTable = 0;
              if (aLines[iL].toLowerCase().trim().startsWith("::div"))
              {
                sbReturn.append("DIVISION ");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table teb_division");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table Criteria");
                iL++; // go past field headers
                iTable = 1;
              } else if (aLines[iL].toLowerCase().trim().startsWith("::lab"))
              {
                sbReturn.append("LaborCategory ");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table teb_reflaborcategory");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table LaborCategory");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table Cost_Effectiveness_Report");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table Missing_Labor_Report");

                iL++; // go past field headers
                iTable = 2;
              } else if (aLines[iL].toLowerCase().trim().startsWith("::use"))
              {
                sbReturn.append("USERS ");
                iL++; // go past field headers
                iTable = 3;
                this.ebEnt.dbDyn.ExecuteUpdate("delete from Users where nmUserId > 10 ");
                this.ebEnt.dbEnterprise.ExecuteUpdate("delete from X25User where RecId > 10 ");
                this.ebEnt.dbEnterprise.ExecuteUpdate("truncate table X25RefTask");
                this.ebEnt.dbEnterprise.ExecuteUpdate("truncate table X25Task");

              } else if (aLines[iL].toLowerCase().trim().startsWith("::pro"))
              {
                sbReturn.append("PROJECTS ");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table Projects");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table teb_project");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table teb_baseline");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table teb_link");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table Budget_Report");

                iL++; // go past field headers
                iTable = 4;
              } else if (aLines[iL].toLowerCase().trim().startsWith("::req"))
              {
                iRecId = 0;
                sbReturn.append("REQUIREMENTS ");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table Requirements");
                this.ebEnt.dbDyn.ExecuteUpdate("delete from teb_link where nmLinkFlags=1");
                iL++; // go past field headers
                iTable = 5;
              } else if (aLines[iL].toLowerCase().trim().startsWith("::sch"))
              {
                sbReturn.append("SCHEDULES ");
                iRecId = 0;
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table Schedule");
                this.ebEnt.dbDyn.ExecuteUpdate("delete from teb_link where nmLinkFlags=2");
                iL++; // go past field headers
                iTable = 6;
              } else if (aLines[iL].toLowerCase().trim().startsWith("::map"))
              {
                sbReturn.append("MAPS ");
                iL++; // go past field headers
                iTable = 7;
              } else if (aLines[iL].toLowerCase().trim().startsWith("::tes"))
              {
                sbReturn.append("TEST ");
                iL++; // go past field headers
                iTable = 8;
              } else if (aLines[iL].toLowerCase().trim().startsWith("::inv"))
              {
                sbReturn.append("INVENTORY ");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table Inventory");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table teb_refinventory");
                iL++; // go past field headers
                iTable = 10;
              } else if (aLines[iL].toLowerCase().trim().startsWith("::hol"))
              {
                sbReturn.append("HOLIDAYS ");
                iL++; // go past field headers
                iHoliday = iTable = 9;
                this.ebEnt.dbDyn.ExecuteUpdate("update teb_division set stHolidays='' ");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table teb_allocate");
                this.ebEnt.dbDyn.ExecuteUpdate("truncate table teb_allocateprj");
              } else if (aLines[iL].toLowerCase().trim().startsWith("::alc"))
              {
                sbReturn.append("AssignLaborCategory ");
                iL++; // go past field headers
                iAssignLaborCategoy = 1;
              }
            } else if (aLines[iL].trim().length() > 0)
            {
              stSql = "";
              String[] aFields = aLines[iL].trim().split("\t", -1);
              for (int i = 0; i < aFields.length; i++)
              {
                if (aFields[i] == null)
                  aFields[i] = "";
                else
                  aFields[i] = aFields[i].trim();
              }
              switch (iTable)
              {
                case 1:
                  stSql = "replace into teb_division"
                    + " (nmDivision,stDivisionName, stCountry,stCurrency,stMoneySymbol,"
                    + "nmBurdenFactor,nmExchangeRate,dtExchangeRate,stWorkDays)"
                    + "values(" + aFields[0] + " ,\"" + aFields[1] + "\","
                    + "\"" + aFields[2] + "\",\"" + aFields[3] + "\",\"" + aFields[4] + "\","
                    + "1.0,'1.0',now(),'Mon,Tue,Wed,Thu,Fri') ";
                  String stTemp = "Alignment Organization Goals	3\nAlignment Project Goals	4\nAvailable Expertise	2\nCash Flow	-3\nCompetitive Advantage	3\nConformance	2\nCost	-5\nEnabler	2\nExternal Contract	5\nFinancial Risk	-2\nGovernment Regulations	2\nManagement Risk	-1\nNegative Consequences	2\nProject Enabler	2\nROI Year 1	5\nROI Year 2	4\nROI Year 3	3\nSchedule Risk	-3\nSecurity	4\nSuccess Factor	3\nSynergy With Organization	3\nSynergy With Other Projects	3\nTechnical Risk	-4\n";
                  String[] aV = stTemp.split("\\n");
                  String stResponsibilities = "2,1";
                  for (int iV = 0; iV < aV.length; iV++)
                  {
                    String[] v = aV[iV].trim().split("\t");
                    this.ebEnt.dbDyn.ExecuteUpdate("replace into Criteria (nmDivision,nmFlags,CriteriaName,WeightImportance,Responsibilities) values(\"" + aFields[0] + "\",1,\"" + v[0] + "\"," + v[1] + ", \"" + stResponsibilities + "\") ");
                  }
                  break;
                case 9: // Holidays
                  stSql = "update teb_division set"
                    + " stHolidays = concat( stHolidays,\"" + aFields[1] + "\",\"\n\" )"
                    + " where nmDivision=" + aFields[0];
                  break;
                case 2:
                  stSql = "insert into LaborCategory (LaborCategory) values(\"" + aFields[0] + "\") ";
                  break;
                case 10:
                  stSql = "replace into Inventory "
                    + "(InventoryName,Quantity,UnitOfMeasure,CostPerUnit) "
                    + "values(\"" + aFields[0] + "\""
                    + "," + aFields[1]
                    + ",\"" + aFields[2] + "\""
                    + ",\"" + this.epsUd.makeDecimal(aFields[3]) + "\""
                    + ") ";
                  break;
                case 3:
                  stSql = "replace into X25User (RecId,stEMail,stPassword,nmPriviledge) values("
                    + aFields[2] + "," + this.ebEnt.dbEnterprise.fmtDbString(aFields[13])
                    + ",old_password(" + this.ebEnt.dbEnterprise.fmtDbString(aFields[23])
                    + ")," + this.epsUd.getPriviledge(aFields[4])
                    + ")";
                  this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
                  aV = aFields[5].trim().split(",");
                  for (int iV = 0; iV < aV.length; iV++)
                  {
                    int nmLaborCategoryId = this.ebEnt.dbDyn.ExecuteSql1n("select nmLcId from LaborCategory where "
                      + "LaborCategory = " + this.ebEnt.dbDyn.fmtDbString(aV[iV].trim()));
                    if (nmLaborCategoryId > 0)
                    {
                      stSql = "replace INTO teb_reflaborcategory "
                        + "(nmLaborCategoryId,nmRefType,nmRefId,nmProductiviyFactor,nmTasksCompleted,"
                        + "nmActualHours,nmEstimatedHours ) VALUES"
                        + "(" + nmLaborCategoryId + ",42," + aFields[2]
                        + ","+aFields[7].replace("(", "-").replace(")", "")
                        + ","+aFields[16]
                        + ","+aFields[17]
                        + ","+aFields[18]
                        + ")";
                      this.ebEnt.dbDyn.ExecuteUpdate(stSql);
                    } else
                      stError += "<br>USERS ERROR: ID: " + aFields[2] + " Labor Category not found: " + aV[iV].trim();
                  }

                  aV = aFields[8].trim().split(",");
                  int iPrimary = 1;
                  for (int iV = 0; iV < aV.length; iV++)
                  {
                    int nmDivision = this.ebEnt.dbDyn.ExecuteSql1n("select nmDivision from teb_division where "
                      + "stDivisionName = " + this.ebEnt.dbDyn.fmtDbString(aV[iV].trim()));
                    if (nmDivision > 0)
                    {
                      stSql = "replace INTO teb_refdivision "
                        + "(nmDivision,nmRefType,nmRefId,nmFlags) VALUES("
                        + nmDivision + ",42," + aFields[2] + "," + iPrimary + ")";
                      this.ebEnt.dbDyn.ExecuteUpdate(stSql);
                      iPrimary = 0;
                    } else
                      stError += "<br>USERS ERROR: ID: " + aFields[2] + " Division not found: " + aV[iV].trim();
                  }
                  String ActivityHours = "";
                  String WeeklyWorkHours = "";
                  if (aFields.length >= 38)
                  {
                    //~8~9~0~0~0~0~0
                    for (int i = 0; i < 7; i++)
                      ActivityHours += "~" + aFields[32 + i];
                    for (int i = 0; i < 7; i++)
                      WeeklyWorkHours += "~" + aFields[25 + i];
                  }
                  String[] aS = aFields[9].split(" ");
                  int iSupervisor = 0;
                  if (aS != null && aS.length > 1)
                    iSupervisor = this.ebEnt.dbDyn.ExecuteSql1n("SELECT max(nmUserId) FROM Users where FirstName = \"" + aS[0].trim() + "\" and LastName=\"" + aS[1].trim() + "\" ");
                  stSql = "INSERT INTO Users"
                    + " (nmUserId,CellPhone,Facsimile,FirstName,Answers,Telephone,HourlyRate,"
                    + "JobTitle,StartDate,LastName,ActivityHours,WeeklyWorkHours,ProductivityFactor,"
                    + "NrTasksCompleted,AccumPrjHours,AccumEstPrjHours,NonPrjHours,SickDaysToDate,"
                    + "VacDaysToDate,Supervisor) VALUES("
                    + aFields[2]
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[15])
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[14])
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[1])
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[24])
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[12])
                    + "," + this.ebEnt.dbDyn.makeDecimal(aFields[6])
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[3])
                    + "," + this.ebEnt.dbDyn.fmtDbString(this.ebEnt.ebUd.fmtDateToDb(aFields[10]))
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[0])
                    + "," + this.ebEnt.dbDyn.fmtDbString(ActivityHours)
                    + "," + this.ebEnt.dbDyn.fmtDbString(WeeklyWorkHours)
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[7].replace("(", "-").replace(")", ""))
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[16])
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[17])
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[18])
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[19])
                    //+ "," + this.ebEnt.dbDyn.fmtDbString(aFields[20]) 
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[21])
                    + "," + this.ebEnt.dbDyn.fmtDbString(aFields[22])
                    + "," + iSupervisor
                    + ")";
                  break;
                case 4: // ::projects
                  aV = aFields[3].trim().split(" ");
                  if (aV.length > 1)
                    stSql = "select max(nmUserId) from Users where FirstName=\"" + aV[0].trim() + "\" and LastName=\"" + aV[1].trim() + "\" ";
                  else
                    stSql = "select max(nmUserId) from Users where FirstName='' and LastName=\"" + aV[0].trim() + "\" ";
                  int iPM = this.ebEnt.dbDyn.ExecuteSql1n(stSql);
                  aV = aFields[4].trim().split(" ");
                  if (aV.length > 1)
                    stSql = "select max(nmUserId) from Users where FirstName=\"" + aV[0].trim() + "\" and LastName=\"" + aV[1].trim() + "\" ";
                  else
                    stSql = "select max(nmUserId) from Users where FirstName='' and LastName=\"" + aV[0].trim() + "\" ";
                  int iBA = this.ebEnt.dbDyn.ExecuteSql1n(stSql);
                  aV = aFields[5].trim().split(" ");
                  if (aV.length > 1)
                    stSql = "select max(nmUserId) from Users where FirstName=\"" + aV[0].trim() + "\" and LastName=\"" + aV[1].trim() + "\" ";
                  else
                    stSql = "select max(nmUserId) from Users where FirstName='' and LastName=\"" + aV[0].trim() + "\" ";
                  int iPPM = this.ebEnt.dbDyn.ExecuteSql1n(stSql);
                  aV = aFields[6].trim().split(" ");
                  if (aV.length > 1)
                    stSql = "select max(nmUserId) from Users where FirstName=\"" + aV[0].trim() + "\" and LastName=\"" + aV[1].trim() + "\" ";
                  else
                    stSql = "select max(nmUserId) from Users where FirstName='' and LastName=\"" + aV[0].trim() + "\" ";
                  int iSponsor = this.ebEnt.dbDyn.ExecuteSql1n(stSql);
                  int[] aiFields =
                  {
                    1026, 932, 1023, 1028, 923, 1031, 1027, 895, 811, 1068, 1069, 1070, 1071, 1072, 1073, 1074,
                    1075, 1076, 1077, 1078, 1079, 1080, 1081, 1082, 1083, 1084, 1085, 1086, 1087, 1088, 1089, 1090
                  };
                  //CPI	Tasks	Task Done	Est. Hours	Hours Exp.	Prj Start	Est. Finish	Est. Cost	Cost to Date	AOG
                  int iValue = 0;
                  double dValue = 0;
                  String stValue = "";
                  for (int i = 0; i < aiFields.length; i++)
                  {
                    stValue = aFields[9 + i].trim();
                    if (aiFields[i] == 1031) // ProjectStartDate
                      if (stValue.length() > 4)
                      {
                        ProjectStartDate1 = ",ProjectStartDate";
                        ProjectProjectStartDate = "," + this.ebEnt.dbDyn.fmtDbString(this.ebEnt.ebUd.fmtDateToDb(stValue));
                      }
                    try
                    {
                      dValue = Double.parseDouble(this.ebEnt.dbDyn.makeDecimal(stValue));
                      iValue = Integer.parseInt(this.ebEnt.dbDyn.makeDecimal(stValue));
                    } catch (Exception e)
                    {
                      iValue = 0;
                      dValue = 0;
                    }
                    stSql = "INSERT INTO teb_project "
                      + "(nmProjectId,nmBaseline,nmFieldId,iValue,nmValue,stValue,nmUserId,dtLastChanged) VALUES("
                      + aFields[0] + ",1," + aiFields[i] + ","
                      + iValue + "," + dValue + ","
                      + this.ebEnt.dbDyn.fmtDbString(stValue) + "," + this.ebEnt.ebUd.getLoginId() + ",now())";
                    this.ebEnt.dbDyn.ExecuteUpdate(stSql);
                  }

                  stSql = "insert into teb_project (nmProjectId,nmBaseline,nmFieldId,nmType) "
                    + "select " + aFields[0] + " as nmProjectId, 1 as nmBaseline, RecId, 2 as nmType from Criteria where nmDivision=" + aFields[1];
                  this.ebEnt.dbDyn.ExecuteUpdate(stSql);

                  this.ebEnt.dbDyn.ExecuteUpdate("INSERT INTO teb_baseline (nmProjectId,nmBaseline,dtEntered,nmUserEntered) "
                    + "values(" + aFields[0] + ",1,now()," + this.ebEnt.ebUd.getLoginId() + ") ");

                  stSql = "INSERT INTO Projects (RecId,nmDivision,ProjectManagerAssignment,"
                    + "ProjectPortfolioManagerAssignment,ProjectName,Sponsor,"
                    + "BusinessAnalystAssignment,NumberBaselines,CurrentBaseline" + ProjectStartDate1 + ") VALUES ("
                    + aFields[0] + ","
                    + aFields[1] + ","
                    + iPM + ","
                    + iPPM + ","
                    + this.ebEnt.dbDyn.fmtDbString(aFields[2]) + ","
                    + iSponsor + ","
                    + iBA + ","
                    + aFields[7] + ","
                    + "1" + ProjectProjectStartDate
                    + ")";
                  break;
                case 5:
                  /*if (iCurrId != iLastId)
                  {
                  nmBaseline = this.ebEnt.dbDyn.ExecuteSql1n("SELECT CurrentBaseline FROM Projects where RecId=" + aFields[0]);
                  }*/
                  nmBaseline = 1;
                  iRecId++;
                  if (aFields.length < 8)
                    stError += "<br>REQ ERROR on Line: " + iRecId;
                  if (aFields[5].length() > 0 && aFields[6].length() > 0)
                  {
                    String[] aMap = aFields[5].trim().split(",");
                    String[] aPercent = aFields[6].trim().split(",");
                    int iPrjId = Integer.parseInt(aFields[0]);
                    int iReqId = Integer.parseInt(aFields[1]);
                    int iSchId = 0;
                    double dPercent = 0;
                    String stComment = "";
                    int iRemainder = 0;
                    int iAmortize = 0;
                    for (int iM = 0; iM < aMap.length; iM++)
                    {
                      try
                      {
                        iSchId = Integer.parseInt(aMap[iM].trim());
                        stComment = "";
                        iAmortize = iRemainder = 0;
                        if (aPercent[iM].toLowerCase().trim().equals("a"))
                        {
                          iAmortize = 1;
                          dPercent = 0; // Amortize
                        } else if (aPercent[iM].toLowerCase().trim().equals("r"))
                        {
                          iRemainder = 1;
                          dPercent = 0; // Remainder
                        } else
                        {
                          dPercent = Double.parseDouble(aPercent[iM].trim());
                        }
                        linkMap(1, aFields[0], nmBaseline, aFields[0], iReqId, aFields[0], iSchId, dPercent, stComment, iAmortize, iRemainder);
                      } catch (Exception e)
                      {
                        stError += "<br>Error in mapping: ReqId " + aFields[1] + " [" + aFields[5] + "] [" + aFields[6] + "]";
                      }
                    }
                  }
                  stSql = "INSERT INTO Requirements "
                    + "(RecId,nmProjectId,nmBaseline,ReqId,ReqTitle,ReqLevel,ReqDescription)VALUES("
                    + aFields[1] + ","
                    + aFields[0] + "," + nmBaseline + ","
                    + aFields[1] + ","
                    + this.ebEnt.dbDyn.fmtDbString(aFields[2]) + ","
                    + aFields[3] + ","
                    + this.ebEnt.dbDyn.fmtDbString(aFields[4])
                    + ")";
                  break;
                case 6:
                  /*if (iCurrId != iLastId)
                  {
                  nmBaseline = this.ebEnt.dbDyn.ExecuteSql1n("SELECT CurrentBaseline FROM Projects where RecId=" + aFields[0]);
                  }*/
                  nmBaseline = 1;
                  iRecId++;
                  stTemp = aFields[8].trim();
                  if (stTemp.length() > 0)
                  {
                    try
                    {
                      String[] aDep = stTemp.split(",");
                      for (int i = 0; i < aDep.length; i++)
                      {
                        // 9FS+260 days  or 1,2,3
                        int iDep = 0;
                        double dLag = 0;
                        String stComment = "fs";
                        if (aDep[i].trim().toLowerCase().contains("fs"))
                        {
                          stComment = "fs";
                          aDep[i] = aDep[i].trim().toLowerCase().replace("fs", "");
                        } else if (aDep[i].trim().toLowerCase().contains("ss"))
                        {
                          stComment = "ss";
                          aDep[i] = aDep[i].trim().toLowerCase().replace("ss", "");
                        } else if (aDep[i].trim().toLowerCase().contains("sf"))
                        {
                          stComment = "sf";
                          aDep[i] = aDep[i].trim().toLowerCase().replace("sf", "");
                        } else if (aDep[i].trim().toLowerCase().contains("ff"))
                        {
                          stComment = "ff";
                          aDep[i] = aDep[i].trim().toLowerCase().replace("ff", "");
                        }

                        if (aDep[i].trim().contains("+"))
                        {
                          String[] aV2 = aDep[i].trim().split("\\+");
                          iDep = Integer.parseInt(aV2[0].trim());
                          dLag = Double.parseDouble(aV2[1].trim().replace(" ", "").replace("days", ""));
                        } else
                        {
                          iDep = Integer.parseInt(aDep[i].trim());
                        }
                        if (stComment.equals("sf")) // we reverse START TO FINISH to FS
                          linkMap(2, aFields[0], nmBaseline, aFields[0], Integer.parseInt(aFields[1]), aFields[0], iDep, dLag, stComment, 0, 0);
                        else
                          linkMap(2, aFields[0], nmBaseline, aFields[0], iDep, aFields[0], Integer.parseInt(aFields[1]), dLag, stComment, 0, 0);
                      }
                    } catch (Exception e)
                    {
                      stError += "<BR> Error with Dependencies Schedule: " + aFields[1] + " Dep: " + aFields[8].trim();
                    }
                  }
                  String stLc = "";
                  if (aFields[10].trim().length() > 0)
                  {
                    aV = aFields[10].trim().split(",");
                    for (int iV = 0; iV < aV.length; iV++)
                    {
                      int nmLaborCategoryId = this.ebEnt.dbDyn.ExecuteSql1n("select nmLcId from LaborCategory where "
                        + "LaborCategory = " + this.ebEnt.dbDyn.fmtDbString(aV[iV].trim()));
                      if (nmLaborCategoryId > 0)
                      {
                        if (iV > 0)
                          stLc += "|";
                        double nmHours = 0;
                        try
                        {
                          nmHours = Double.parseDouble(this.epsUd.makeHours(aFields[5])) / aV.length;
                        } catch (Exception e)
                        {
                          stError += "<br>ProjectId " + aFields[0] + " Task ID: " + aFields[1] + " Labor Category invalid EFFORT: " + aV[iV].trim();
                        }
                        stLc += nmLaborCategoryId + "~1~" + nmHours + "~~~~";
                      } else
                        stError += "<br>ProjectId " + aFields[0] + " Task ID: " + aFields[1] + " Labor Category not found: " + aV[iV].trim();
                    }
                  }
                  stSql = "INSERT INTO Schedule (RecId,nmProjectId,nmBaseline,SchId,SchDescription,SchTitle,SchLevel,"
                    + "SchEstimatedEffort,SchDependencies,SchPriority,SchLaborCategories)VALUES("
                    + aFields[1] + "," + aFields[0] + "," + nmBaseline + ","
                    + aFields[1] + ","
                    + this.ebEnt.dbDyn.fmtDbString(aFields[2]) + ","
                    + this.ebEnt.dbDyn.fmtDbString(aFields[3]) + ","
                    + aFields[4] + ","
                    + this.epsUd.makeHours(aFields[5]) + ","
                    + this.ebEnt.dbDyn.fmtDbString(aFields[8]) + ","
                    + this.epsUd.makeYesNo(aFields[9]) + ","
                    + this.ebEnt.dbDyn.fmtDbString(stLc)
                    //+ this.ebEnt.dbDyn.fmtDbString(aFields[11]) -- SchIndicators
                    + ")";
                  iSchedule++;
                  break;
                default:
                  stError += "<BR>ERROR LOADING: Table: " + iTable;
                  break;
              }
              if (stSql.length() > 0)
                this.ebEnt.dbDyn.ExecuteUpdate(stSql);
            }
          }
          if (iHoliday > 0)
            this.epsUd.epsLoadSpecialDays();
          this.epsUd.epsEf.setEbEnt(ebEnt, null);
          this.epsUd.epsEf.processUsersInLaborCategory();
          this.epsUd.epsEf.processUsersInDivision();
          if (iAssignLaborCategoy > 0)
            this.epsUd.epsEf.processAssignLaborCategory();

          this.ebEnt.dbDyn.ExecuteUpdate("update Triggers set ContactList='1,2,3', TriggerEvent='Enabled', Communication='No' ");
          EpsReport epsReport = new EpsReport();
          epsReport.doProjectRanking(this.epsUd);
          this.epsUd.runEOB();
          stError += epsReport.getError();
          sbReturn.append(" Rows: " + aLines.length);
          if (this.stError.length() > 0)
            sbReturn.append(" <font color=red>WITH ERRORS<hr>" + this.stError + "<br></font>");
          else
            sbReturn.append(" no errors");
          this.stError = "";
          long endTime = System.nanoTime();
          long elapsedTime = endTime - startTime;
          double seconds = elapsedTime / 1.0E09;
          sbReturn.append("<br>Duration ");
          DecimalFormat df = new DecimalFormat("#,###,###,##0.000");
          sbReturn.append(df.format(seconds));
          sbReturn.append(" seconds <br> ");
        }
      } else
      {
        sbReturn.append("</form><form method=post><br>Load  Values (copy/paste from Excel)<br><textarea name=ta rows=10 cols=100>");
        sbReturn.append("</textarea><br /><input type=submit name=submit value=LOAD></form>");
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR epsLoadImport " + e;
    }
    return sbReturn.toString();
  }

  public void linkMap(int iFlags, String stPrjId, int iBaseline, String stReqPrj, int iReqRecId, String stSchPrj, int iSchRecId,
    double dPercent, String stComment, int iAmortize, int iRemainder)
  {
    try
    {
      String stValue = this.ebEnt.dbDyn.ExecuteSql1("select nmPercent from teb_link where nmProjectId=" + stPrjId
        + " and nmBaseline=" + iBaseline + " and nmLinkFlags=" + iFlags
        + " and nmFromProject=" + stReqPrj + " and nmFromId=" + iReqRecId + " and nmToProject=" + stSchPrj + " and nmToId=" + iSchRecId);
      if (stValue != null && stValue.length() > 0)
      {
        this.ebEnt.dbDyn.ExecuteUpdate("update teb_link set nmPercent=" + dPercent + ",nmAmortize=" + iAmortize
          + ",nmRemainder=" + iRemainder + ",stComment=\"" + stComment + "\" where nmProjectId=" + stPrjId
          + " and nmBaseline=" + iBaseline + " and nmLinkFlags=" + iFlags
          + " and nmFromProject=" + stReqPrj + " and nmFromId=" + iReqRecId + " and nmToProject=" + stSchPrj + " and nmToId=" + iSchRecId);
      } else
      {
        this.ebEnt.dbDyn.ExecuteUpdate("insert into teb_link "
          + "(nmProjectId,nmBaseline,nmLinkFlags,nmFromProject,nmFromId,nmToProject,nmToId,nmPercent,stComment,nmAmortize,nmRemainder) values"
          + "(" + stPrjId + "," + iBaseline + "," + iFlags + "," + stReqPrj + "," + iReqRecId + "," + stSchPrj + "," + iSchRecId + ","
          + dPercent + ",\"" + stComment + "\"," + iAmortize + "," + iRemainder + ")");
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR linkMap iReqRecId " + iReqRecId + " iSchRecId " + iSchRecId + " :" + e;
    }
    return;
  }

  // nmLinkFlags = 1 ... Req-Sch MAP: Current Edit Projects only
  // nmLinkFlags 0x100 ... OLD/FROZEN entries, do not recalculate
  // nmLinkFlags 0x200 ... ACTIVE entries, do no change
  public String analyzeLink()
  {
    String stHead = "<tr><td align=right>" + this.stPk + "</td><td>Map/Link</td>";
    StringBuilder sbReturn = new StringBuilder(5000);

    try
    {
      // Remainder calculation
      this.ebEnt.dbDyn.ExecuteUpdate("update teb_link set nmPercent=0 where nmProjectId=" + this.stPk + " and nmBaseline=" + this.nmBaseline + " and nmLinkFlags=1 and "
        + "( nmAmortize = 1 or nmRemainder = 1 ) ");
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("select "
        + "count(*) cnt,nmToId,format(sum(nmPercent),4) nmPercent,sum(nmAmortize) nmAmortize,sum(nmRemainder) nmRemainder "
        + "from teb_link where nmProjectId=" + this.stPk + " and nmBaseline=" + this.nmBaseline
        + " and nmLinkFlags=1 and nmAmortize=0 group by nmToId");
      rs.last();
      int iMax = rs.getRow();
      for (int iL = 1; iL <= iMax; iL++)
      {
        rs.absolute(iL);
        if (rs.getDouble("nmPercent") < 100 && rs.getInt("nmRemainder") > 0)
        {
          double nmRemainder = (100.0 - rs.getDouble("nmPercent")) / rs.getInt("nmRemainder");
          this.ebEnt.dbDyn.ExecuteUpdate("update teb_link set nmPercent=" + nmRemainder + " "
            + "where nmProjectId=" + this.stPk + " and nmBaseline=" + this.nmBaseline + " and nmLinkFlags=1 "
            + "and nmToId=" + rs.getString("nmToId") + " and nmRemainder != 0");
        }

        if (rs.getDouble("nmPercent") < 100 && this.ebEnt.dbDyn.ExecuteSql1n("select count(*) from Schedule where"
          + " nmProjectId=" + this.stPk + " and nmBaseline=" + this.nmBaseline
          + " and RecId=" + rs.getString("nmToId") + " and (SchFlags & 0x8000) != 0 ") > 0)
        {
          // If < 100 % and SchFlags == Amortize ... Distribute to all
          this.ebEnt.dbDyn.ExecuteUpdate("update Requirements r left join teb_link l"
            + " on l.nmProjectId=r.nmProjectId and l.nmBaseline=r.nmBaseline and l.nmLinkFlags=1 and l.nmFromId=r.RecId"
            + " and l.nmToId=" + rs.getInt("nmToId") + " set l.nmLinkFlags=5"
            + " where (r.ReqFlags & 0x10) != 0 and (l.nmAmortize=" + rs.getInt("nmToId") + " or l.nmAmortize is null )");

          ResultSet rs2 = this.ebEnt.dbDyn.ExecuteSql("select r.RecId from Requirements r left join teb_link l"
            + " on l.nmProjectId=r.nmProjectId and l.nmBaseline=r.nmBaseline and l.nmLinkFlags=1 and l.nmFromId=r.RecId"
            + " and l.nmToId=" + rs.getInt("nmToId") + " where (r.ReqFlags & 0x10) != 0"
            + " and (l.nmAmortize=" + rs.getInt("nmToId") + " or l.nmAmortize is null )");
          rs2.last();
          int iMax2 = rs2.getRow();
          if (iMax2 > 0)
          {
            double nmRemainder = (100.0 - rs.getDouble("nmPercent")) / iMax2;
            for (int i2 = 1; i2 <= iMax2; i2++)
            {
              rs2.absolute(i2);
              linkMap(1, stPk, nmBaseline, stPk, rs2.getInt("RecId"), stPk, rs.getInt("nmToId"),
                nmRemainder, "", rs.getInt("nmToId"), 0);
            }
          }
          this.ebEnt.dbDyn.ExecuteUpdate("delete from teb_link where nmLinkFlags=5");
        }
      }
      ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql("select"
        + " count(*) cnt,nmToId,format(sum(nmPercent),4) nmPercent,sum(nmAmortize) nmAmortize,sum(nmRemainder) nmRemainder"
        + " from teb_link where nmProjectId=" + this.stPk + " and nmBaseline=" + this.nmBaseline
        + " and nmLinkFlags=1 group by nmToId having format(sum(nmPercent),4) = 100");
      rs1.last();
      iMax = rs1.getRow();
      sbReturn.append(stHead + "<td>Requirements to Schedules</td><td>Schedules Mapped 100% </td><td align=right>" + iMax + "</td>");
      if (iMax > 0)
        sbReturn.append("<td style='color:green;'>ok</td>");
      else
      {
        this.iAnalyzeStatus |= 0x10; // No schedule mapped 100%
        sbReturn.append("<td style='color:red;'>fail</td>");
      }
      sbReturn.append(this.getElapsed());
      rs1.close();

      rs1 = this.ebEnt.dbDyn.ExecuteSql("select"
        + " count(*) cnt,nmToId,format(sum(nmPercent),4) nmPercent,sum(nmAmortize) nmAmortize,sum(nmRemainder) nmRemainder"
        + " from teb_link where nmProjectId=" + this.stPk + " and nmBaseline=" + this.nmBaseline
        + " and nmLinkFlags=1 group by nmToId having format(sum(nmPercent),4) < 100");
      rs1.last();
      iMax = rs1.getRow();
      if (iMax > 0)
      {
        sbReturn.append(stHead + "<td>Requirements to Schedules</td><td>Schedules Mapped &lt; 100% <br>");
        for (int i = 1; i <= iMax; i++)
        {
          rs1.absolute(i);
          sbReturn.append("<a href='" + makeScheduleLink(rs1.getInt("nmToId")) + "'>" + rs1.getString("nmToId") + "</a>&nbsp;");
        }
        sbReturn.append("</td><td valign=top align=right>" + iMax + "</td>");
        sbReturn.append("</td><td valign=top style='color:red;'>fail</td>");
        this.iAnalyzeStatus |= 0x20;
        sbReturn.append(this.getElapsed());
      }
      rs1.close();

      rs1 = this.ebEnt.dbDyn.ExecuteSql("select"
        + " count(*) cnt,nmToId,format(sum(nmPercent),4) nmPercent,sum(nmAmortize) nmAmortize,sum(nmRemainder) nmRemainder"
        + " from teb_link where nmProjectId=" + this.stPk + " and nmBaseline=" + this.nmBaseline
        + " and nmLinkFlags=1 group by nmToId having format(sum(nmPercent),4) > 100");
      rs1.last();
      iMax = rs1.getRow();
      if (iMax > 0)
      {
        sbReturn.append(stHead + "<td>Requirements to Schedules</td><td>Schedules Mapped &gt; 100% <br>");
        for (int i = 1; i <= iMax; i++)
        {
          rs1.absolute(i);
          sbReturn.append("<a href='" + makeScheduleLink(rs1.getInt("nmToId")) + "'>" + rs1.getString("nmToId") + "</a>&nbsp;");
        }
        sbReturn.append("</td><td valign=top align=right>" + iMax + "</td>");
        sbReturn.append("</td><td valign=top style='color:red;'>fail</td>");
        this.iAnalyzeStatus |= 0x40;
        sbReturn.append(this.getElapsed());
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR analyzeLink " + e;
    }
    return sbReturn.toString();
  }

  public String getElapsed()
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    sbReturn.append("<td valign=top>" + (ebEnt.dbDyn.getSqlCount() - iLastSqlCount) + "</td>");
    long endTime = System.nanoTime();
    long elapsedTime = endTime - startTime;
    double seconds = elapsedTime / 1.0E09;
    sbReturn.append("<td valign=top>" + seconds + "</td>");
    sbReturn.append("</tr>");
    startTime = endTime;
    iLastSqlCount = ebEnt.dbDyn.getSqlCount();
    return sbReturn.toString();
  }

  private String makeScheduleLink(int SchRecId)
  {
    int iFrom = 0;
    int SchId = 0;
    try
    {
      SchId = this.ebEnt.dbDyn.ExecuteSql1n("select SchId from Schedule where nmProjectId=" + stPk
        + " and nmBaseline=" + this.nmBaseline + " and RecId=" + SchRecId);
      iFrom = (SchId / this.epsUd.rsMyDiv.getInt("MaxRecords")) * this.epsUd.rsMyDiv.getInt("MaxRecords");
    } catch (Exception e)
    {
    }
    return "./?stAction=projects&t=12&do=xls&pk=" + this.stPk + "&parent=&child=21&from=" + iFrom + "#row" + SchId;
  }

  public String doMap(String stChild, String stR)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    sbReturn.append("<center><h1>Requirements / Schedule Mapping</h1>");
    String stTemp = this.ebEnt.ebUd.request.getParameter("pk");
    if (stTemp != null && stTemp.length() > 0)
      stPk = stTemp;
    stGoBack = "?stAction=projects&t=12";
    String stDo2 = this.ebEnt.ebUd.request.getParameter("do2");
    stTemp = "";
    //?stAction=projects&t=12&do=xls&pk=5&parent=&child=21&from=0&a=map&r=12#row12
    String stLink = "?stAction=projects&t=" + this.ebEnt.ebUd.request.getParameter("t") + "&do=xls&pk=" + stPk + "&parent=" + stTemp + "&child=" + stChild + "&a=map&r=" + stR;
    int iFrom = 0;
    double nmTotalPercent = 0.00;
    DecimalFormat df = new DecimalFormat("#,###,##0.0");
    //df.setMaximumFractionDigits(1);
    int iAmortize;
    int iCount = 0;
    try
    {
      int iBlock = this.epsUd.rsMyDiv.getInt("ReqSchRows");
      this.rsProject = this.ebEnt.dbDyn.ExecuteSql("select * from Projects where RecId=" + stPk);
      this.rsProject.absolute(1);
      nmBaseline = this.rsProject.getInt("CurrentBaseline");

      iMaxFields = 5;
      String stSql = "";
      String stFrom = "";
      String stTo = "";
      if (stChild.equals("21"))
      {
        stFrom = this.ebEnt.ebUd.request.getParameter("rid");
        stTo = stR;
      } else
      {
        stTo = this.ebEnt.ebUd.request.getParameter("rid");
        stFrom = stR;
      }
      if (stDo2 != null && stDo2.equals("del"))
      {
        this.ebEnt.dbDyn.ExecuteUpdate("delete l.* from teb_link l, Projects p"
          + " where l.nmProjectId=p.RecId and l.nmBaseline=p.CurrentBaseline and l.nmLinkFlags=1"
          + " and l.nmProjectId=" + stPk + " and l.nmFromId=" + stFrom + " and l.nmToId=" + stTo);
        analyzeLink();
        //calculate the requirement cost
        this.processRequirementCost(Integer.parseInt(stFrom));
      }
      String s1 = "</form><form method=post name='form" + stChild + "' id='form" + stChild + "' onsubmit='return myValidation(this)'>"
        + "<input type=hidden name=from value='" + iFrom + "'><table><tr><td align=center><h2>"
        + this.rsProject.getString("ProjectName") + "</h2></td></tr>";
      sbReturn.append(s1);
      String stFromOrig = "";
      String stToOrig = "";
      String nmPercent = "";
      String nmRemainder = "";
      int iFromId = 0;
      int iToId = 0;
      double dPercent = 0;
      if (stDo2 != null && stDo2.equals("edit"))
      {
        stTemp = this.ebEnt.ebUd.request.getParameter("Save");
        if (stTemp != null && stTemp.equals("Save"))
        {
          nmRemainder = this.ebEnt.ebUd.request.getParameter("nmRemainder");
          nmPercent = this.ebEnt.ebUd.request.getParameter("nmPercent");
          stFromOrig = this.ebEnt.ebUd.request.getParameter("stFromOrig");
          stToOrig = this.ebEnt.ebUd.request.getParameter("stToOrig");
          stFrom = this.ebEnt.ebUd.request.getParameter("stFrom");
          stTo = this.ebEnt.ebUd.request.getParameter("stTo");
          if (nmPercent == null || nmPercent.length() <= 0)
            nmPercent = "0";
          if (stFrom == null || stFrom.length() <= 0)
          {
            this.ebEnt.ebUd.setPopupMessage("Requirement ID cannot be blank");
            stFrom = "";
          } else
          {
            try
            {
              iFromId = Integer.parseInt(stFrom);
              iCount = this.ebEnt.dbDyn.ExecuteSql1n("SELECT count(*) FROM Requirements r, Projects p"
                + " where r.nmProjectId=p.RecId and r.nmBaseline=p.CurrentBaseline"
                + " and (ReqFlags & 0x10 ) != 0 and r.nmProjectId=" + stPk + " and r.RecId=" + stFrom);
              if (iCount <= 0)
                this.ebEnt.ebUd.setPopupMessage("Invalid low level Requirement ID: " + stFrom);
            } catch (Exception e)
            {
              this.ebEnt.ebUd.setPopupMessage("Invalid low level Requirement ID: " + stFrom);
              stFrom = "";
            }
          }
          if (stTo == null || stTo.length() <= 0)
          {
            this.ebEnt.ebUd.setPopupMessage("Task Id cannot be blank");
            stTo = "";
          } else
          {
            try
            {
              iToId = Integer.parseInt(stTo);
              iCount = this.ebEnt.dbDyn.ExecuteSql1n("SELECT count(*) FROM Schedule s, Projects p"
                + " where s.nmProjectId=p.RecId and s.nmBaseline=p.CurrentBaseline"
                + " and (SchFlags & 0x10 ) != 0 and s.nmProjectId=" + stPk + " and s.RecId=" + stTo);
              if (iCount <= 0)
                this.ebEnt.ebUd.setPopupMessage("Invalid low level Task Id: " + stTo);
            } catch (Exception e)
            {
              this.ebEnt.ebUd.setPopupMessage("Invalid low level Task Id: " + stTo);
              stTo = "";
            }
          }
          try
          {
            dPercent = Double.parseDouble(nmPercent);
          } catch (Exception e)
          {
            this.ebEnt.ebUd.setPopupMessage("Invalid percent: " + nmPercent);
            nmPercent = "";
          }

          stTemp = this.ebEnt.ebUd.getPopupMessage();
          if (stTemp == null || stTemp.length() <= 0)
          {
            if (stChild.equals("19") && stToOrig.equals("-1"))
            {
              // Insert NEW
              int iRemainder = Integer.parseInt(nmRemainder);
              linkMap(1, stPk, nmBaseline, stPk, iFromId, stPk, iToId, dPercent, "", 0, iRemainder);
  
              //calculate the requirement cost
              this.processRequirementCost(iFromId);
            } else if (stChild.equals("21") && stFromOrig.equals("-1"))
            {
              // Insert NEW
              int iRemainder = Integer.parseInt(nmRemainder);
              linkMap(1, stPk, nmBaseline, stPk, iFromId, stPk, iToId, dPercent, "", 0, iRemainder);
            } else
            {
              if (stChild.equals("21"))
              {
                stSql = "update teb_link l, Projects p"
                  + " set l.nmFromId=" + stFrom + ",l.nmToId=" + stTo + ",l.nmPercent=" + nmPercent
                  + ",l.nmRemainder=" + nmRemainder
                  + " where l.nmProjectId=p.RecId and l.nmBaseline=p.CurrentBaseline and l.nmLinkFlags=1"
                  + " and l.nmProjectId=" + stPk + " and l.nmFromId=" + stFromOrig + " and l.nmToId=" + stToOrig;
              } else
              {
                stSql = "update teb_link l, Projects p"
                  + " set l.nmFromId=" + stFrom + ",l.nmToId=" + stTo + ",l.nmPercent=" + nmPercent
                  + ",l.nmRemainder=" + nmRemainder
                  + " where l.nmProjectId=p.RecId and l.nmBaseline=p.CurrentBaseline and l.nmLinkFlags=1"
                  + " and l.nmProjectId=" + stPk + " and l.nmFromId=" + stFromOrig + " and l.nmToId=" + stToOrig;
                
                //calculate the requirement cost
                this.processRequirementCost(Integer.parseInt(stFrom));
              }
              this.ebEnt.dbDyn.ExecuteUpdate(stSql);
            }
            analyzeLink();
            stDo2 = "";
          }
        } else if (stTemp != null && stTemp.equals("Cancel"))
        {
          stDo2 = "";
        }
      }
      String nmAmortize = this.ebEnt.ebUd.request.getParameter("nmAmortize");
      if (nmAmortize != null && nmAmortize.length() > 0)
        stDo2 = "";

      if (stDo2 != null && stDo2.equals("edit"))
      {
        if (stFrom.equals("-1"))
        {
          stFrom = "";
          stTo = stR;
          stFromOrig = "-1";
          stToOrig = "-1";
          nmPercent = "0";
        } else if (stFromOrig.length() <= 0)
        {
          stFromOrig = stFrom;
          stToOrig = stTo;
          ResultSet rsValue = this.ebEnt.dbDyn.ExecuteSql("select * from teb_link l, Projects p"
            + " where l.nmProjectId=p.RecId and l.nmBaseline=p.CurrentBaseline and l.nmLinkFlags=1"
            + " and l.nmProjectId=" + stPk + " and l.nmFromId=" + stFrom + " and l.nmToId=" + stTo);
          rsValue.last();
          if (rsValue.getRow() > 0)
          {
            rsValue.absolute(1);
            nmRemainder = rsValue.getString("nmRemainder");
            nmPercent = rsValue.getString("nmPercent");
          } else
          {
            nmRemainder = "0";
            nmPercent = "0";
          }
        }
        sbReturn.append("<tr><td>Requirement ID:</td><td><input type=text name=stFrom style='text-align:right;width:50px;' value=\"");
        sbReturn.append(stFrom);
       sbReturn.append("\"></td></tr><tr><td>Task ID:<td>");
        
        if (stTo==null || "".equals(stTo) || "-1".equals(stTo)) {
        	sbReturn.append("<select name='stTo'>");
            sbReturn.append(this.ebEnt.ebUd.addOption2("Select Task id", "-1", stTo));
            ResultSet taskIds = this.ebEnt.dbDyn.ExecuteSql("SELECT * FROM Schedule s where (SchFlags & 0x10 ) != 0 and s.nmProjectId="+stPk);
            taskIds.last();
            int maxIds = taskIds.getRow();
            for (int i=1; i<=maxIds; i++) {
            	taskIds.absolute(i);
            	sbReturn.append(this.ebEnt.ebUd.addOption2(Integer.toString(taskIds.getInt("SchId")) + ": " + taskIds.getString("SchTitle"), Integer.toString(taskIds.getInt("SchId")), stTo));
            }
            sbReturn.append("</select>");
        } else {
            sbReturn.append("<input type=text style='text-align:right;width:50px;' name=stTo value=\"");
        	sbReturn.append(stTo);
        	sbReturn.append("\">");
        }
        
        
        sbReturn.append("</td></tr><tr><td>Map:<td><input type=test style='text-align:right;width:50px;' name=nmPercent value=\"");
        sbReturn.append(nmPercent);
        sbReturn.append("\">% <select name=nmRemainder>");
        sbReturn.append(this.ebEnt.ebUd.addOption2("Map by Percent", "0", nmRemainder));
        sbReturn.append(this.ebEnt.ebUd.addOption2("Remainder", "1", nmRemainder));
        sbReturn.append("</select></td></tr>");
        sbReturn.append("<tr><td colspan=2 align=center><input type=submit name=Save value='Save'> &nbsp;");
        sbReturn.append("<input type=hidden name=stFromOrig value=\"");
        sbReturn.append(stFromOrig);
        sbReturn.append("\">");
        sbReturn.append("<input type=hidden name=stToOrig value=\"");
        sbReturn.append(stToOrig);
        sbReturn.append("\">");

        sbReturn.append("<input type=submit name=Save value='Cancel'></td></tr>");
      } else if (stChild.equals("19"))
      {
        // Requirements Map
        stSql = "select * from Schedule s, teb_link l, Requirements r where l.nmLinkFlags=1 and "
          + " s.nmProjectId=l.nmProjectId and s.nmBaseline=l.nmBaseline and s.SchId=l.nmToId and"
          + " r.nmProjectId=s.nmProjectId and r.nmBaseline=s.nmBaseline and r.ReqId=l.nmFromId and"
          + " r.RecId=" + stR + " and s.nmProjectId=" + stPk + " and s.nmBaseline=" + this.nmBaseline
          + " order by r.ReqId";
        ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
        rs.last();
        int iMax = rs.getRow();
        if (iMax > 0)
        {
          rs.absolute(1);
          sbReturn.append("<tr><td align=right valign=bottom><b>Requirement</b> ID: ");
          sbReturn.append(rs.getString("ReqId"));
          sbReturn.append(" Level:");
          sbReturn.append(rs.getString("ReqLevel"));
          sbReturn.append(" Title: </td><td align=left><b>");
          sbReturn.append(this.epsUd.epsEf.fullReqTitle(rs, stPk, nmBaseline));
          sbReturn.append("</b></td></tr></table></td></tr><tr><td>");
          sbReturn.append("<table bgcolor=blue cellpadding=1 cellspacing=1>");
          sbReturn.append("<tr><th class=l1th>Action</th><th class=l1th>ID</th><th class=l1th>Level</th><th class=l1th>Task Title</th><th class=l1th colspan=2>Map</th></tr>");
          int iLastSchId = -1;
          for (int iR = 1; iR <= iMax; iR++)
          {
            rs.absolute(iR);
            sbReturn.append("<tr>");
            sbReturn.append("<td bgcolor=white align=center valign=bottom>");
            sbReturn.append("<a title='Edit' href='" + stLink + "&do2=edit&rid=" + rs.getString("SchId") + "'><img src='./common/b_edit.png'></a>");
            sbReturn.append("<a title='Delete' href='" + stLink + "&do2=del&rid=" + rs.getString("SchId") + "'><img src='./common/b_drop.png'"
              + " onClick=\"return myConfirm('Are you sure you want to delete this item?', " + iR + "," + iMax + " )\"></a></td>");

            sbReturn.append("<td bgcolor=white align=right valign=bottom>" + rs.getString("SchId") + "</td>");
            sbReturn.append("<td bgcolor=white align=right valign=bottom>" + rs.getString("SchLevel") + "</td>");
            sbReturn.append("<td bgcolor=white align=left valign=bottom>");
            if ((iLastSchId + 1) != rs.getInt("SchId")) //Out of sync
              sbReturn.append(this.epsUd.epsEf.fullSchTitle(rs, stPk, nmBaseline));
            else
            {
              for (int i = 0; i < (rs.getInt("SchLevel") * 2); i++)
                sbReturn.append("&nbsp;");
              sbReturn.append(rs.getString("SchTitle"));
            }
            sbReturn.append("</td>");
            sbReturn.append("<td bgcolor=white align=right valign=bottom>" + df.format(rs.getDouble("nmPercent")) + "</td>");

            nmTotalPercent += rs.getDouble("nmPercent");
            iCount++;
            sbReturn.append("<td bgcolor=white valign=bottom>%");
            if (rs.getInt("nmAmortize") > 0)
              sbReturn.append("&nbsp;Amortize(" + rs.getInt("nmAmortize") + ")");
            if (rs.getInt("nmRemainder") > 0)
              sbReturn.append("&nbsp;Remainder");
            sbReturn.append("</td>");
            sbReturn.append("</tr>");
            iLastSchId = rs.getInt("SchId");
          }
          if (stChild.equals("21"))
          {
            sbReturn.append("<tr>");
            sbReturn.append("<td bgcolor=white align=right colspan=4>Total (" + iCount + "):</td>");
            sbReturn.append("<td bgcolor=white align=right><b>" + df.format(nmTotalPercent) + "</b></td>");
            nmTotalPercent += rs.getDouble("nmPercent");
            iCount++;
            sbReturn.append("<td bgcolor=white align=left>%</td>");
            sbReturn.append("</tr>");
          }
        }
        sbReturn.append("<tr><td bgcolor=white align=center colspan=6><input type=button onClick =\"parent.location='");
        sbReturn.append(stGoBack);
        sbReturn.append("&pk=");
        sbReturn.append(stPk);
        sbReturn.append("&do=edit'\" value='Go Back to Projects'>");
        sbReturn.append("&nbsp;&nbsp;<input type=button onClick=\"parent.location='");
        sbReturn.append(stLink);
        sbReturn.append("&do2=edit&rid=-1'\" value='Insert New Map'></td></tr>");
      } else if (stChild.equals("21"))
      {
        if (nmAmortize != null && nmAmortize.length() > 0)
        {
          /*this.ebEnt.dbDyn.ExecuteUpdate("update teb_link l, Requirements r"
          + " set l.nmAmortize=" + nmAmortize + " where  r.ReqId=l.nmFromId"
          + " and r.RecId=" + stR + " and r.nmProjectId=" + stPk + " and r.nmBaseline=" + this.nmBaseline);*/
          if (nmAmortize.equals("1")) // YES
          {
            this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags = ( SchFlags | 0x8000 )"
              + " where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and RecId=" + stR);
            //this.ebEnt.dbDyn.ExecuteUpdate("update teb_link set nmLinkFlags=5 where (nmLinkFlags&0x1) != 0"
            //  + " and nmProjectId="+stPk+" and nmBaseline=" + this.nmBaseline+" and nmToId="+ stR+" and nmAmortize=" + stR);
          } else
          { // No
            this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags = ( SchFlags & ~0x8000 )"
              + " where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and RecId=" + stR);
            //this.ebEnt.dbDyn.ExecuteUpdate("delete from teb_link where (nmLinkFlags&0x1) != 0"
            //  + " and nmProjectId="+stPk+" and nmBaseline=" + this.nmBaseline+" and nmToId="+ stR+" and nmAmortize=" + stR);
          }
          analyzeLink();
        }
        // Schedule Map
        stSql = "select * from Schedule s, teb_link l, Requirements r where l.nmLinkFlags in (1,5) and"
          + " s.nmProjectId=l.nmProjectId and s.nmBaseline=l.nmBaseline and s.SchId=l.nmToId and"
          + " r.nmProjectId=s.nmProjectId and r.nmBaseline=s.nmBaseline and r.ReqId=l.nmFromId and"
          + " s.RecId=" + stR + " and s.nmProjectId=" + stPk + " and s.nmBaseline=" + this.nmBaseline
          + " order by r.ReqId";
        ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
        rs.last();
        int iMax = rs.getRow();
        if (iMax > 0)
        {
          rs.absolute(1);
          sbReturn.append("<tr><td align=right valign=bottom><b>Schedule</b> ID: " + rs.getString("SchId"));
          sbReturn.append(" Level:" + rs.getString("SchLevel") + "");
          sbReturn.append(" Title: </td><td align=left><b>" + this.epsUd.epsEf.fullSchTitle(rs, stPk, nmBaseline) + "</b></td></tr></table></td></tr><tr><td>"
            + "<table bgcolor=blue cellpadding=1 cellspacing=1>"
            + "<tr><th class=l1th>Action</th><th class=l1th>ID</th><th class=l1th>Level</th><th class=l1th>Requirement Title</th><th class=l1th colspan=2>Map</th></tr>");
          int iLastReqId = -1;
          int iRemainder = 0;
          for (int iR = 1; iR <= iMax; iR++)
          {
            rs.absolute(iR);
            if ((rs.getInt("SchFlags") & 0x8000) != 0 && rs.getInt("nmToId") == rs.getInt("nmAmortize"))
              continue;
            sbReturn.append("<tr>");
            sbReturn.append("<td bgcolor=white align=center valign=bottom>");
            sbReturn.append("<a title='Edit' href='" + stLink + "&do2=edit&rid=" + rs.getString("ReqId") + "'><img src='./common/b_edit.png'></a>");
            sbReturn.append("<a title='Delete' href='" + stLink + "&do2=del&rid=" + rs.getString("ReqId") + "'><img src='./common/b_drop.png'"
              + " onClick=\"return myConfirm('Are you sure you want to delete this item?', " + iR + "," + iMax + " )\"></a></td>");

            sbReturn.append("<td bgcolor=white align=right valign=bottom>" + rs.getString("ReqId") + "</td>");
            sbReturn.append("<td bgcolor=white align=right valign=bottom>" + rs.getString("ReqLevel") + "</td>");
            sbReturn.append("<td bgcolor=white align=left valign=bottom>");
            if ((iLastReqId + 1) != rs.getInt("ReqId")) //Out of sync
              sbReturn.append(this.epsUd.epsEf.fullReqTitle(rs, stPk, nmBaseline));
            else
            {
              for (int i = 0; i < (rs.getInt("ReqLevel") * 2); i++)
                sbReturn.append("&nbsp;");
              sbReturn.append(rs.getString("ReqTitle"));
            }
            sbReturn.append("</td>");
            sbReturn.append("<td bgcolor=white align=right valign=bottom>" + df.format(rs.getDouble("nmPercent")) + "</td>");

            nmTotalPercent += rs.getDouble("nmPercent");
            iCount++;
            sbReturn.append("<td bgcolor=white valign=bottom>%");
            //if (rs.getInt("nmAmortize") > 0)
            //  sbReturn.append("&nbsp;Amortize");
            if (rs.getInt("nmRemainder") > 0)
            {
              sbReturn.append("&nbsp;Remainder");
              iRemainder++;
            }
            sbReturn.append("</td>");
            sbReturn.append("</tr>");
            iLastReqId = rs.getInt("ReqId");
          }
          sbReturn.append("<tr><td bgcolor=white align=right colspan=4>Total (" + iCount + "):</td>");
          sbReturn.append("<td bgcolor=white align=right><b>" + df.format(nmTotalPercent) + "</b></td>");
          sbReturn.append("<td bgcolor=white align=left>%</td>");
          sbReturn.append("</tr>");

          sbReturn.append("<tr><td bgcolor=white valign=bottom colspan=4 align=right><b>Amortize remainder of task over rest (");
          sbReturn.append(df.format(this.ebEnt.dbDyn.ExecuteSql1n("select count(*) from Requirements r where r.nmProjectId=" + stPk + " and r.nmBaseline=" + nmBaseline + " and (r.ReqFlags & 0x10) != 0;") - iCount));
          sbReturn.append(") of Requirements:</b></td><td align=right bgcolor=yellow>");
          sbReturn.append(df.format(100.0 - nmTotalPercent));
          sbReturn.append(" &nbsp;%</td><td bgcolor=white>");
          if (iRemainder > 0)
          {
            sbReturn.append("Cannot Amortize this task due to 'Remainder'");
          } else if (nmTotalPercent >= 100)
          {
            sbReturn.append("Cannot Amortize this task. It is fully mapped.");
          } else
          {
            sbReturn.append("<select name=nmAmortize id=nmAmortize onChange=\"document.form");
            sbReturn.append(stChild);
            sbReturn.append(".submit();\" >");
            int iFlags = rs.getInt("SchFlags");
            int iValue = 0;
            if ((iFlags & 0x8000) != 0)
              iValue = 1;
            sbReturn.append(this.ebEnt.ebUd.addOption2("No", "0", "" + iValue));
            sbReturn.append(this.ebEnt.ebUd.addOption2("Yes", "1", "" + iValue));
            sbReturn.append("</select>");
          }
          sbReturn.append("</td><tr>");
        }
        sbReturn.append("<tr><td bgcolor=white align=center colspan=6><input type=button onClick =\"parent.location='");
        sbReturn.append(stGoBack);
        sbReturn.append("&pk=");
        sbReturn.append(stPk);
        sbReturn.append("&do=edit'\" value='Go Back to Projects'>");
        sbReturn.append("&nbsp;&nbsp;<input type=button onClick=\"parent.location='");
        sbReturn.append(stLink);
        sbReturn.append("&do2=edit&rid=-1'\" value='Insert New Map'></td></tr>");
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR doMap " + e;
    }
    sbReturn.append("</table></td></tr></table>");
    return sbReturn.toString();
  }

  public String processCriticalPath()
  {
    //http://hspm.sph.sc.edu/courses/j716/cpm/cpm.html
    int iMaxSchedule = 0;
    String stHead = "<tr><td align=right valign=top>" + this.stPk + "</td><th valign=top>Critical Path</th>";
    StringBuilder sbReturn = new StringBuilder(5000);
    try
    {
      // Reset CP flags
      this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags = ( SchFlags & ~0xE00) "
        + "where  nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);

      String stWeekend = this.epsUd.rsMyDiv.getString("stWorkDays").toLowerCase();
      String[] stWork = stWeekend.split(",");
      aWeekend = new int[7 - stWork.length];
      int iW = 0;
      if (!stWeekend.contains("sa"))
        aWeekend[iW++] = Calendar.SATURDAY;
      if (!stWeekend.contains("su"))
        aWeekend[iW++] = Calendar.SUNDAY;
      if (!stWeekend.contains("mo"))
        aWeekend[iW++] = Calendar.MONDAY;
      if (!stWeekend.contains("tu"))
        aWeekend[iW++] = Calendar.TUESDAY;
      if (!stWeekend.contains("we"))
        aWeekend[iW++] = Calendar.WEDNESDAY;
      if (!stWeekend.contains("th"))
        aWeekend[iW++] = Calendar.THURSDAY;
      if (!stWeekend.contains("fr"))
        aWeekend[iW++] = Calendar.FRIDAY;

      ResultSet rsH = this.ebEnt.dbDyn.ExecuteSql("SELECT * FROM Calendar where nmDivision=1 and nmFlags=1 and dtDay >= curdate() limit 500");
      rsH.last();
      int iHMax = rsH.getRow();
      for (int i = 1; i < iHMax; i++)
      {
        rsH.absolute(i);
        if (i > 1)
          stHolidays += ",";
        stHolidays += rsH.getString("dtDay");
      }

      /// sbReturn.append( stHead + "<td valign=top>by Estimated Effort</td><td colspan=3 valign=top>";
      iMaxRecId = this.ebEnt.dbDyn.ExecuteSql1n("SELECT max(RecId) FROM Schedule where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
      iMaxRecId++;
      aaPath = new int[iMaxRecId][];
      aCp = new EpsCriticalPath[iMaxRecId];
      for (int i = 0; i < aCp.length; i++)
        aCp[i] = null;

      String stSql = "select * from Schedule s left join teb_link l on s.nmProjectId=l.nmProjectId and "
        + "s.nmBaseline=l.nmBaseline and l.nmLinkFlags=2 and l.nmFromId=s.RecId "
        + "where s.nmProjectId=" + stPk + " and s.nmBaseline=" + this.nmBaseline + " and (s.SchFlags & 0x10 ) != 0 "
        + "order by RecId, nmToId";

      ResultSet rsAll = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rsAll.last();
      iMaxSchedule = rsAll.getRow();
      if (iMaxSchedule > 0)
      {
        // Build the Memory Array
        //for (int iR = 1; iR <= iMaxSchedule && this.stError.length() <= 0; iR++)
        for (int iR = 1; iR <= iMaxSchedule; iR++)
        {
          rsAll.absolute(iR);
          if (aCp[rsAll.getInt("RecId")] == null)
          {
            aCp[rsAll.getInt("RecId")] = new EpsCriticalPath();
            aCp[rsAll.getInt("RecId")].setEpsCriticalPath(iMaxRecId, epsUd, rsProject, rsAll, stHolidays, aWeekend);
            aCp[rsAll.getInt("RecId")].addSuccessor(rsAll);
          } else
          {
            aCp[rsAll.getInt("RecId")].addSuccessor(rsAll);
          }
          this.stError += aCp[rsAll.getInt("RecId")].getError();
        }
        for (int iR = 0; iR < aCp.length; iR++)
          setDependendies(iR);

        stSql = "select * from Schedule s left join teb_link l on s.nmProjectId=l.nmProjectId and "
          + "s.nmBaseline=l.nmBaseline and l.nmLinkFlags=2 and l.nmToId=s.RecId "
          + "where s.nmProjectId=" + stPk + " and s.nmBaseline=" + this.nmBaseline + " and (s.SchFlags & 0x10 ) != 0 "
          + "and l.nmFromId is null";
        ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
        rs.last();
        int iMax = rs.getRow();
        if (iMax > 0)
        {
          for (int iR = 1; iR <= iMax; iR++)
          {
            rs.absolute(iR);
            if (aCp[rs.getInt("RecId")] != null)
            {
              aCp[rs.getInt("RecId")].iFLags = 1; // Starting point.
            } else
            {
              this.stError += "<BR>ERROR Schedule should NOT be in queue " + rs.getInt("RecId");
            }
            this.stError += aCp[rs.getInt("RecId")].getError();
          }
        } else
        {
          this.iAnalyzeStatus |= 0x80; // No Starting point for Critical path
        }

        findPaths(0);

        stCommTrace = this.ebEnt.ebUd.request.getParameter("commtrace");
        if (stCommTrace != null && stCommTrace.length() > 0 && stCommTrace.equals("t")) // Trace
        {
          for (int i = 0; i < aCp.length; i++)
          {
            if (aCp[i] != null)
            {
              sbReturn.append("<br>" + i + "  S: ");
              for (int i2 = 0; i2 < aCp[i].aSuccessor.length && aCp[i].aSuccessor[i2] > 0; i2++)
              {
                sbReturn.append(aCp[i].aSuccessor[i2] + ", ");
              }
              sbReturn.append("  D: ");
              for (int i2 = 0; i2 < aCp[i].aDependency.length && aCp[i].aDependency[i2] > 0; i2++)
              {
                sbReturn.append(aCp[i].aDependency[i2] + ", ");
              }
            }
          }
        }

        String[] aEndDate = new String[iMaxPath];

        sbReturn.append(stHead);
        sbReturn.append("<td valign=top>by End Date</td><td valign=top colspan=3>");
        sbReturn.append("<table border=5>");

        // First, process Critical path to end, but need to stop at each Task with dependenciies
        String stStartDate = this.rsProject.getString("FixedProjectStartDate");
        this.dtProjectStart = null;
        // First Task in CP
        if (stStartDate != null && stStartDate.length() > 8)
        {
          dtProjectStart = EpsStatic.getCalendar(stStartDate);
          if (EpsStatic.daysBetween(null, dtProjectStart) < 0)
          {
            stError += "<br>ERROR: Invalid start date for project: " + this.rsProject.getString("ProjectName") + " " + stStartDate;
            dtProjectStart = null; // cant be in future
          }
        } else
          dtProjectStart = null;

        Calendar dtEnd = dtProjectStart;
        Calendar dtCpLongest = null;

        // Do all PATHS - Calculate CRITICAL PATH by latest end date
        for (int iI = 0; iI < iMaxPath; iI++)
        {
          int iPrev = 0;
          for (int iS = 0; iS < iMaxRecId; iS++)
          {
            if (this.aaPath[iI][iS] > 0)
            {
              dtEnd = calcStartEnd(this.aaPath[iI][iS], dtProjectStart, iPrev);
              iPrev = this.aaPath[iI][iS];
            } else
              break;
          }
          aEndDate[iI] = EpsStatic.getDate(dtEnd);
        }
        // Verify all done
        for (int iP = 0; iP < aCp.length; iP++)
        {
          if (aCp[iP] != null)
          {
            if ((aCp[iP].iFLags & 0x8000) == 0)
              stError += "<BR>ERROR: Task not processed in CP by END DATE " + iP;
          }
        }

        // Show Results
        String stBg = "";
        int iSlackLowest = 1000000;
        for (int iC = 0; iC < this.iMaxPath; iC++)
        {
          sbReturn.append("<tr>");
          stBg = "";
          if (iC == iCpIndexByDate)
            stBg = " bgcolor=yellow ";

          sbReturn.append("<td " + stBg + ">");
          Calendar dtEnd2 = null;
          int iTotalSlack = 0;
          for (int iS = 0; iS < iMaxRecId; iS++)
          {
            if (this.aaPath[iC][iS] > 0)
            {
              int iWorkDays = 0;
              aCp[this.aaPath[iC][iS]].iSlack = 0;
              if (dtEnd2 != null)
                iWorkDays = aCp[this.aaPath[iC][iS]].workDaysBetween(dtEnd2, aCp[this.aaPath[iC][iS]].dtStart);
              if (iWorkDays != 1 && iWorkDays != 0)
              {
                if (iWorkDays > 0)
                {
                  if (iS > 0 && aCp[this.aaPath[iC][iS - 1]] != null)
                    aCp[this.aaPath[iC][iS - 1]].iSlack = iWorkDays;
                  iTotalSlack += iWorkDays;
                }
                sbReturn.append("<span STYLE='background-color: #aaaaaa'>");
              } else
                sbReturn.append("<span>");
              sbReturn.append(this.aaPath[iC][iS] + " ");
              sbReturn.append(aCp[this.aaPath[iC][iS]].stTitle + " ");
              sbReturn.append(aCp[this.aaPath[iC][iS]].getStartEnd() + "/<font color=blue>" + aCp[this.aaPath[iC][iS]].iWeekend
                + "</font>/<font color=red>" + aCp[this.aaPath[iC][iS]].iHoliday + "/" + aCp[this.aaPath[iC][iS]].dLag + "</font>");
              if (iWorkDays != 1 && iWorkDays != 0)
                sbReturn.append(" [" + iWorkDays + "]");
              sbReturn.append("</span><br>");
              dtEnd2 = aCp[this.aaPath[iC][iS]].dtEnd;
            } else
              break;
          }
          sbReturn.append(" Total Slack: " + iTotalSlack + " Idx: " + iC);
          if (dtCpLongest == null || dtEnd2.after(dtCpLongest))
          {
            dtCpLongest = (Calendar) dtEnd2.clone();
            iCpIndexByDate = iC;
            iSlackLowest = 10000000;
          } else if (dtEnd2.equals(dtCpLongest))
          {
            if (iTotalSlack < iSlackLowest)
            {
              iSlackLowest = iTotalSlack;
              iCpIndexByDate = iC;
            }
          }
          sbReturn.append("</td>");

          sbReturn.append("<td " + stBg + " align=right valign=top>" + aEndDate[iC] + "</td>");
          if (stBg.length() > 0)
            sbReturn.append("<td " + stBg + " align=right valign=top>CRITICAL</td>");
          else
            sbReturn.append("<td " + stBg + " align=right valign=top>&nbsp;</td>");
          sbReturn.append("</tr>");
        }
        sbReturn.append("</table>");
        sbReturn.append("<br>CP: " + iCpIndexByDate);
        sbReturn.append("<table><tr><td class=small><u><b>Legend:</b> (fields from left to right)</u>");
        sbReturn.append("<br>Task Id");
        sbReturn.append("<br>Task Title");
        sbReturn.append("<br>{ Effort in Hours / Start Date: strating Hour / End Date: Ending Hour }");
        sbReturn.append("<br><b>Start - End Date</b>");
        sbReturn.append("<br>(# calendar days between start/end dates)");
        sbReturn.append("<br><font color=blue>/BLUE: # Weekend Days</font>");
        sbReturn.append("<br><font color=red>/RED: # Holidays");
        sbReturn.append("<br>/RED: # Lag</font>");
        sbReturn.append("<br><span STYLE='background-color: #aaaaaa'>[Difference in days from Line above <b>if not 1 day</b>. Shows in gray background]</span>");
        sbReturn.append("</font></td></tr></table></td>");
        sbReturn.append(this.getElapsed());

        // Save everything to DB
        //1) Mark CP
        String stCp = "";
        for (int iS = 0; iS < iMaxRecId; iS++)
        {
          if (this.aaPath[iCpIndexByDate][iS] > 0)
          {
            if (stCp.length() > 0)
              stCp += ",";
            stCp += this.aaPath[iCpIndexByDate][iS];
          }
        }
        this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set SchFlags = (SchFlags | 0x200) "
          + "where RecId in (" + stCp + ") and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline);
        for (int iC = 0; iC < aCp.length; iC++)
        {
          if (aCp[iC] != null)
          {
            stSql = "update Schedule set SchSlack= " + aCp[iC].iSlack
              + ",SchStartDate=\"" + aCp[iC].getStart() + "\""
              + ",SchFinishDate=\"" + aCp[iC].getEnd() + "\""
              + "  where RecId = " + iC + " and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline;
            this.ebEnt.dbDyn.ExecuteUpdate(stSql);
          }
        }
      } else
      {
        this.iAnalyzeStatus |= 0x100; // No Schedules for Critical path
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR processCriticalPath ProjectId=" + stPk + " " + e;
    }
    return sbReturn.toString();
  }

  private Calendar getLatestStart(int iI, Calendar dtStart)
  {
    Calendar dtReturn = null;
    if (dtStart != null)
      dtReturn = (Calendar) dtStart.clone();
    else
    {
      dtReturn = Calendar.getInstance();
      dtReturn.add(Calendar.DAY_OF_YEAR, +1);
    }
    Calendar dtEnd2 = null;

    if (aCp[iI] != null)
    {
      for (int iD = 0; iD < iMaxRecId; iD++)
      {
        if (aCp[iI].aDependency[iD] > 0)
        {
          if (iI > 0 || (aCp[aCp[iI].aDependency[iD]].iFLags & 0x8000) != 0)
          {
            dtEnd2 = (Calendar) aCp[aCp[iI].aDependency[iD]].dtEnd.clone();
            double dDays = 0; // aCp[aCp[iI].aDependency[iD]].dLag;
            dDays = aCp[iI].aDependencyLag[iD];
            if (dDays > 0)
            {
              if (dDays > aCp[iI].dLag)
                aCp[iI].dLag = dDays;
              for (double d = 0; d < dDays; d++)
              {
                dtEnd2.add(Calendar.DAY_OF_YEAR, +1);
                int iLoop = 0;
                do
                {
                  iLoop = aCp[aCp[iI].aDependency[iD]].checkHoliday(dtEnd2);
                  if (iLoop > 0)
                    dtEnd2.add(Calendar.DAY_OF_YEAR, iLoop);
                } while (iLoop != 0);
              }
            } else if (dDays < 0) // Negative LAG, start earlier
            {
              if (dDays < aCp[iI].dLag)
                aCp[iI].dLag = dDays;
              for (double d = 0; d > dDays; d--)
              {
                dtEnd2.add(Calendar.DAY_OF_YEAR, -1);
                int iLoop = 0;
                int iTemp = 0;
                do
                {
                  iLoop = aCp[aCp[iI].aDependency[iD]].checkHoliday(dtEnd2);
                  if (iLoop > 0)
                    dtEnd2.add(Calendar.DAY_OF_YEAR, -iLoop);
                } while (iLoop != 0 && iTemp++ < 1000);
              }
            }
          } else
          {
            stError += "<BR>ERROR getLatestStart: not all children are set " + aCp[iI].aDependency[iD];
            dtEnd2 = null; // dont know when it is
          }
          if (dtEnd2 != null && dtEnd2.after(dtReturn))
          {
            dtReturn = (Calendar) dtEnd2.clone();
          }
        }
      }
    }
    return dtReturn;
  }

  Calendar calcStartEnd(int iI, Calendar dtStart, int iPrev)
  {
    Calendar dtReturn = null;
    Calendar dtEnd2 = null;
    int iLoop = 0;
    int[] aChain = new int[iMaxRecId];
    int iChain = 0;
    double dPreviousHours = 0;
    try
    {
      if (aCp[iI] != null && (aCp[iI].iFLags & 0x8000) != 0)
        return aCp[iI].dtEnd; //------------------------------------>

      if (aCp[iI] == null)
      {
        stError += "<BR>ERROR calcStartEnd aCp is null for i = " + iI;
        return dtStart; //------------------------------------------->
      }

      // Walk through all dependends
      for (int iD = 0; iD < iMaxRecId; iD++)
      {
        if (aCp[iI].aDependency[iD] > 0)
        {
          // Check if Dependent already processed or not
          // Need to handle Type of dependency
          iChain = 0;
          if ((aCp[aCp[iI].aDependency[iD]].iFLags & 0x8000) == 0)
          {
            // Need to back Track to first Finished task and forward track to here
            int iCp = aCp[iI].aDependency[iD];
            //Get Front of Path for start Date
            aChain[iChain] = iCp;
            iChain++;
            while (iCp > 0 && aCp[iCp] != null && (aCp[iCp].iFLags & 0x8000) == 0)
            {
              aChain[iChain] = aCp[iCp].aDependency[0];
              iCp = aChain[iChain];
              iChain++;
              if (iLoop++ > iMaxRecId)
              {
                this.stError += "<BR>ERROR Loop in BackTrack " + iCp;
                break;
              }
            }
            if (iChain > 1)
            {
              iChain--;
              for (; iChain > 0; iChain--)
              {
                if (aChain[iChain] == 0
                  || (aCp[aChain[iChain]] != null && (aCp[aChain[iChain]].iFLags & 0x100) != 0))
                  dtEnd2 = this.dtProjectStart;
                else
                {
                  dtEnd2 = aCp[aChain[iChain]].dtEnd;
                }
                if (aCp[aChain[iChain]] != null)
                  dPreviousHours = aCp[aChain[iChain]].dHoursEnd;
                else
                  dPreviousHours = 0;
                if (aCp[aChain[iChain - 1]] != null)
                {
                  if (aCp[iI].aDependency[iD] == aChain[iChain - 1])
                  {
                    if (aCp[iI].aDependencyType[iD].equals("ss"))
                    { // Start to Start
                      calcStartEnd(aChain[iChain - 1], dtStart, aChain[iChain]); // Set dependent to same start as us
                    } else if (aCp[iI].aDependencyType[iD].equals("ff"))
                    { // Finish to Finish
                      dtStart = getLatestStart(iI, dtStart);
                      aCp[iI].dtStart = dtStart;
                      aCp[iI].calculateEnd(dPreviousHours);
                      aCp[aChain[iChain]].dtEnd = (Calendar) aCp[iI].dtEnd.clone(); // Set My end to depend end
                      aCp[aChain[iChain]].calculateStart(); // calculate depend start
                    } else if (aCp[iI].aDependencyType[iD].equals("sf"))
                    { // Start to Finish (is reverse of Finish to Start
                      //dtEnd2 = calcStartEnd(aChain[iChain - 1], dtStart); // Start dependent on my START
                      //dtStart = (Calendar) dtEnd2.clone(); // set my Start to prev. end
                      dtEnd2 = calcStartEnd(aChain[iChain - 1], dtEnd2, aChain[iChain]); // we treat SF like FS since To/from reversed
                    } else
                    { // Normal Finish to Start
                      dtEnd2 = calcStartEnd(aChain[iChain - 1], dtEnd2, aChain[iChain]);
                    }
                  } else
                  {
                    dtEnd2 = calcStartEnd(aChain[iChain - 1], dtEnd2, aChain[iChain]); // Not our dependent
                  }
                }
                if (iLoop++ > iMaxRecId)
                {
                  this.stError += "<BR>ERROR Loop2 in BackTrack " + iCp;
                  break;
                }
              }
            }
          }
        } else
          break;
      }
      if ((aCp[iI].iFLags & 0x8000) == 0)
      {
        int iD = 0; // Don't know how to handle multple deps' here
        if (aCp[iI].aDependency[iD] > 0 && (aCp[aCp[iI].aDependency[iD]].iFLags & 0x8000) != 0) // Special case of dep.
        {
          if (aCp[iI].aDependencyType[iD].equals("ss"))
          { // Start to Start
            dtStart = (Calendar) aCp[aCp[iI].aDependency[iD]].dtStart.clone();
          } else if (aCp[iI].aDependencyType[iD].equals("ff"))
          { // Finish to Finish
            aCp[iI].dtEnd = (Calendar) aCp[aCp[iI].aDependency[iD]].dtEnd.clone();
            aCp[iI].calculateStart(); // calculate depend start
            dtStart = aCp[iI].dtStart;
          } else if (aCp[iI].aDependencyType[iD].equals("sf"))
          {
            dtStart = getLatestStart(iI, dtStart); // we treat SF like FS since To/From reversed
          } else // Normal Finish to Start
          {
            dtStart = getLatestStart(iI, dtStart);
          }
        } else
        {
          dtStart = getLatestStart(iI, dtStart);
        }
        if (aCp[iPrev] != null)
          dPreviousHours = aCp[iPrev].dHoursEnd;
        else
          dPreviousHours = 0;
        aCp[iI].setStartCalculateEnd(dtStart, dPreviousHours);
        aCp[iI].iFLags |= 0x8000;
      }
      dtReturn = aCp[iI].dtEnd;
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR calcStartEnd i: " + iI + " " + e;
    }
    return dtReturn;
  }

  public String findPaths(int iFrom)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    try
    {
      for (int iP = iFrom; iP < aCp.length; iP++)
      {
        if (aCp[iP] != null && aCp[iP].iFLags == 1) // Root and NOT followed yet
        {
          this.aaPath[iMaxPath] = new int[iMaxRecId];
          for (int i2 = 0; i2 < iMaxRecId; i2++)
            aaPath[iMaxPath][i2] = 0;
          this.iPathPosition = 0;
          followPath(1, iP);
          iMaxPath++;
        }
      }
      // finish the rest.
      for (int iP = 0; iP < aCp.length; iP++)
      {
        if (aCp[iP] != null && (aCp[iP].iFLags & 0x100) == 0) // Non processed ID's
        {
          this.aaPath[iMaxPath] = new int[iMaxRecId];
          for (int i2 = 0; i2 < iMaxRecId; i2++)
            aaPath[iMaxPath][i2] = 0;
          this.iPathPosition = 0;
          followHead(iP);
          followPath(2, iP);
          iMaxPath++;
        }
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR findPaths " + e;
    }
    return sbReturn.toString();
  }

  public String followHead(int iP)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    try
    {
      int[] aList = new int[iMaxRecId];
      int iMax = 0;
      for (int i = 0; i < aList.length; i++)
        aList[i] = 0;

      while (iP > 0 && aCp[iP] != null && aCp[iP].aDependency[0] != 0)
      {
        aList[iMax++] = aCp[iP].aDependency[0];
        iP = aCp[iP].aDependency[0];
      }
      for (int i = iMax - 1; i >= 0; i--)
      {
        iP = aList[i];
        aaPath[iMaxPath][this.iPathPosition] = iP;
        iPathPosition++;
        aCp[iP].iFLags |= 0x100;
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR followHead " + e;
    }
    return sbReturn.toString();
  }

  public String followPath(int iType, int iP)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    int iFollow = 0;
    try
    {
      if (aCp[iP] != null)
      {
        aaPath[iMaxPath][this.iPathPosition] = iP;
        iPathPosition++;
        aCp[iP].iFLags |= 0x100;

        for (int iC = 0; iC < 20; iC++)
        {
          if (aCp[iP].aSuccessor[iC] != 0)
          {
            if (aCp[iP].aSuccessorProcess[iC] == 0)
            {
              followPath(iType, aCp[iP].aSuccessor[iC]);
              aCp[iP].aSuccessorProcess[iC]++; // Mark processed
              iFollow++;
              break;
            }
          } else
            break;
        }
        if (iFollow == 0 && iType == 2)
        {
          for (int iC = 0; iC < 20; iC++)
          {
            if (aCp[iP].aSuccessor[iC] != 0)
            {
              followPath(iType, aCp[iP].aSuccessor[iC]);
              aCp[iP].aSuccessorProcess[iC]++; // Mark processed
              iFollow++;
              break;
            } else
              break;
          }
        }

      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR followPath " + e;
    }
    return sbReturn.toString();
  }

  public void setDependendies(int iR)
  {
    if (aCp[iR] != null)
    {
      for (int i = 0; i < aCp[iR].aSuccessor.length; i++)
      {
        if (aCp[iR].aSuccessor[i] > 0)
        {
          aCp[aCp[iR].aSuccessor[i]].addDpendency(iR, aCp[iR].aSuccessorType[i], aCp[iR].aSuccessorLag[i]);
        } else
          break;
      }
    }
  }

  public void calcUpdateBaseline()
  {
    try
    {
      int nmReqt = this.ebEnt.dbDyn.ExecuteSql1n("SELECT count(*) FROM Requirements where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline);
      int nmSch = this.ebEnt.dbDyn.ExecuteSql1n("SELECT count(*) FROM Schedule where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline);
      double nmEffort = this.ebEnt.dbDyn.ExecuteSql1n("SELECT sum(SchEstimatedEffort) FROM Schedule where (SchFlags & 0x10 ) != 0 and nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline);
      double nmCost = this.ebEnt.dbDyn.ExecuteSql1n("SELECT sum(SchCost) FROM Schedule where (SchFlags & 0x10 ) != 0 and nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline);
      this.ebEnt.dbDyn.ExecuteUpdate("update teb_baseline set "
        + "nmReqt=" + nmReqt + ",nmSch=" + nmSch + ",nmEffort=" + nmEffort + ",nmCost=" + nmCost + " "
        + "where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline);
    } catch (Exception e)
    {
      this.stError += "<br>ERROR followPath " + e;
    }
  }

  public String xlsBaseline(String stChild)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    String stSql = "";
    try
    {
      stPk = this.ebEnt.ebUd.request.getParameter("pk");
      stGoBack = "?stAction=projects&t=12";
      String stTemp = "";
      String stLink = "?stAction=projects&t=" + this.ebEnt.ebUd.request.getParameter("t") + "&do=xls&pk=" + stPk + "&parent=" + stTemp + "&child=" + stChild;
      this.rsProject = this.ebEnt.dbDyn.ExecuteSql("select * from Projects where RecId=" + stPk);
      this.rsProject.absolute(1);
      nmBaseline = this.rsProject.getInt("CurrentBaseline");
      calcUpdateBaseline();

      String stAction = this.ebEnt.ebUd.request.getParameter("submit");
      String stFrom = this.ebEnt.ebUd.request.getParameter("nmFrom");
      iMaxFields = 12;
      if (stAction != null && stAction.length() > 0 && stFrom != null && stFrom.length() > 0)
      { // PROCESS ACTIONS
        if (stAction.equals("delete"))
        {
          this.ebEnt.dbDyn.ExecuteUpdate("delete from teb_baseline where nmProjectId=" + stPk + " and nmBaseline=" + stFrom);
          this.ebEnt.dbDyn.ExecuteUpdate("delete from Schedule where nmProjectId=" + stPk + " and nmBaseline=" + stFrom);
          this.ebEnt.dbDyn.ExecuteUpdate("delete from Requirements where nmProjectId=" + stPk + " and nmBaseline=" + stFrom);
          this.ebEnt.dbDyn.ExecuteUpdate("delete from Test where nmProjectId=" + stPk + " and nmBaseline=" + stFrom);
          this.ebEnt.dbDyn.ExecuteUpdate("delete from teb_link where nmProjectId=" + stPk + " and nmBaseline=" + stFrom);
          this.ebEnt.dbDyn.ExecuteUpdate("delete from teb_project where nmProjectId=" + stPk + " and nmBaseline=" + stFrom);
          stAction = null;
        } else
        {
          int nmMaxBaseline = this.ebEnt.dbDyn.ExecuteSql1n("SELECT max(nmBaseline) FROM teb_baseline where nmProjectId =" + stPk);
          nmMaxBaseline++;
          String stType = this.ebEnt.ebUd.request.getParameter("stType");
          String stAdditional = "";
          String stType2 = "";
          if (stType.toLowerCase().equals("approve"))
          {
            stAdditional += ", ProjectStatus=1 ";
            stType2 = "1";
          } else if (stType.toLowerCase().equals("completed"))
          {
            stAdditional += ", ProjectStatus=2 ";
            stType2 = "2";
          } else if (stType.toLowerCase().equals("suspended"))
          {
            stAdditional += ", ProjectStatus=3 ";
            stType2 = "3";
          }
          this.epsUd.epsEf.addAuditTrail(119, stPk, stType2);
          this.epsUd.epsEf.addAuditTrail(1025, stPk, "" + nmMaxBaseline);

          this.ebEnt.dbDyn.ExecuteUpdate("update Projects set CurrentBaseline=" + nmMaxBaseline + stAdditional
            + " where RecId=" + stPk + " and CurrentBaseline=" + nmBaseline);
          copyRows("teb_baseline", "nmProjectId", stPk, "nmBaseline", stFrom, "" + nmMaxBaseline);
          this.ebEnt.dbDyn.ExecuteUpdate("update teb_baseline set stType=" + this.ebEnt.dbDyn.fmtDbString(stType)
            + ",nmUserEntered=" + this.ebEnt.ebUd.getLoginId()
            + " where nmProjectId=" + stPk + " and nmBaseline=" + nmMaxBaseline);
          copyRows("Schedule", "nmProjectId", stPk, "nmBaseline", stFrom, "" + nmMaxBaseline);
          copyRows("Requirements", "nmProjectId", stPk, "nmBaseline", stFrom, "" + nmMaxBaseline);
          copyRows("Test", "nmProjectId", stPk, "nmBaseline", stFrom, "" + nmMaxBaseline);
          copyRows("teb_link", "nmProjectId", stPk, "nmBaseline", stFrom, "" + nmMaxBaseline);
          copyRows("teb_project", "nmProjectId", stPk, "nmBaseline", stFrom, "" + nmMaxBaseline);
          nmBaseline = nmMaxBaseline;
        }
      }
      sbReturn.append("</form><form method=post name='form" + stChild + "' id='form" + stChild + "' onsubmit='return myValidation(this)'>"
        + "<table class=l1tablenarrow><tr><td class=l1td colspan=" + iMaxFields + " align=center>"
        + "<h2>" + this.rsProject.getString("ProjectName") + "</h2></td></tr>");
      xlsAnalyze(); // must analyze it
      stSql = "SELECT * FROM teb_baseline where nmProjectId =" + stPk + " order by nmBaseline desc";
      ResultSet rsB = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rsB.last();
      int iMaxB = rsB.getRow();
      String stSelect = "<select name=nmFrom>";
      String stReturn1 = "<tr>";
      stReturn1 += "<th class=l1th colspan=2>Baseline</th><th class=l1th>Type</th><th class=l1th align=right>From</th>"
        + "<th class=l1th colspan=2>Created by</th><th class=l1th colspan=2>Last Analysis</th><th class=l1th>Requirements</th>"
        + "<th class=l1th>Schedules</th><th class=l1th>Effort</th><th class=l1th>Cost</th></tr>";
      stReturn1 += "</tr>";
      for (int iB = 1; iB <= iMaxB; iB++)
      {
        rsB.absolute(iB);
        stReturn1 += "<tr>";
        stReturn1 += "<td class=l1td align=right>";
        if (rsB.getInt("nmBaseline") != nmBaseline)
          stReturn1 += "<a title='Delete' href="
            + "'" + stLink + "&submit=delete&nmFrom=" + rsB.getString("nmBaseline") + "'>"
            + "<img src='./common/b_drop.png' "
            + "onClick=\"return myConfirm('Are you sure you want to delete this item?', " + rsB.getString("nmBaseline") + "," + iMaxB + " )\"></a></td>";
        stReturn1 += "<td class=l1td align=right>";
        stReturn1 += rsB.getString("nmBaseline") + "</td>";
        stReturn1 += "<td class=l1td align=right>" + rsB.getString("stType") + "</td>";
        stReturn1 += "<td class=l1td align=right>" + rsB.getInt("nmFromBaseline") + "</td>";
        stReturn1 += "<td class=l1td>" + this.epsUd.getUserName(rsB.getInt("nmUserEntered")) + "</td>";
        stReturn1 += "<td class=l1td>" + this.ebEnt.ebUd.fmtDateFromDb(rsB.getString("dtEntered")) + "</td>";
        stReturn1 += "<td class=l1td>";
        if (rsB.getInt("nmAnalyzeStatus") != 0)
          stReturn1 += "<font color=red>Errors</font>";
        else
          stReturn1 += "<font color=green>ok</font>";
        stReturn1 += "</td>";
        stReturn1 += "<td class=l1td>" + this.ebEnt.ebUd.fmtDateTimeFromDb(rsB.getString("dtLastAnalyze")) + "</td>";
        stReturn1 += "<td class=l1td align=right>" + rsB.getString("nmReqt") + "</td>";
        stReturn1 += "<td class=l1td align=right>" + rsB.getString("nmSch") + "</td>";
        stReturn1 += "<td class=l1td align=right>" + rsB.getString("nmEffort") + "</td>";
        stReturn1 += "<td class=l1td align=right>" + rsB.getString("nmCost") + "</td>";
        stReturn1 += "</tr>";
        stSelect += this.ebEnt.ebUd.addOption(rsB.getString("nmBaseline"), rsB.getString("nmBaseline"), this.rsProject.getString("CurrentBaseline"));
      }
      stSelect += "</select>";
      if (stAction == null || stFrom == null)
      {
        String stType = "<select name=stType>";
        stType += this.ebEnt.ebUd.addOption4("Interim", "Interim", "", 1);
        int iAllowed = 0;
        if ((this.ebEnt.ebUd.getLoginPersonFlags() & 0x20) != 0) // PPM only
          iAllowed = 1;
        stType += this.ebEnt.ebUd.addOption4("Approve", "Approve", "", iAllowed);
        stType += this.ebEnt.ebUd.addOption4("Completed", "Completed", "", iAllowed);
        stType += this.ebEnt.ebUd.addOption4("Suspended", "Suspended", "", iAllowed);
        stType += "</select>";
        sbReturn.append("<tr><td class=l1td colspan=" + iMaxFields + " align=center><br><table bgcolor=yellow><tr>"
          + "<td>Create new baseline based on </td><td>" + stSelect + "</td><td>" + stType + "</td><td> "
          + "<input type=submit name=submit value='Create Baseline'></td></tr></table>"
          + "<br>&nbsp;<h2>Baseline History</h2></td></tr>");
      }
      sbReturn.append(stReturn1);
    } catch (Exception e)
    {
      this.stError += "<br>ERROR xlsBaseline " + e;
    }
    sbReturn.append("</table>");
    sbReturn.append("<input type=button onClick=\"parent.location='" + stGoBack + "&pk=" + stPk + "&do=edit'\" value='Go Back to Projects'>");
    return sbReturn.toString();
  }

  public void copyRows(String stTable, String stPk, String stPkValue, String stPk2, String stPk2Value, String stPk2New)
  {
    try
    {
      ResultSet rsF = this.ebEnt.dbDyn.ExecuteSql("explain " + stTable);
      String stFields = "";
      String stSelectFrom = "";
      String stSql = "insert into " + stTable;
      rsF.last();
      int iMaxF = rsF.getRow();
      for (int iF = 1; iF <= iMaxF; iF++)
      {
        rsF.absolute(iF);
        if (iF > 1)
        {
          stFields += ",";
          stSelectFrom += ",";
        }
        if (rsF.getString(1).equals(stPk2))
        {
          stSelectFrom += stPk2New;
        } else if (rsF.getString(1).contains("nmFromBaseline"))
        {
          stSelectFrom += stPk2Value;
        } else if (rsF.getString(1).contains("dtEnter"))
        {
          stSelectFrom += "now()";
        } else
        {
          stSelectFrom += "t2." + rsF.getString(1);
        }

        stFields += rsF.getString(1);
      }
      stSql += " (" + stFields + ") select " + stSelectFrom + " from " + stTable + " t2 where t2."
        + stPk + " = " + stPkValue + " and t2." + stPk2 + " = " + stPk2Value;
      this.ebEnt.dbDyn.ExecuteUpdate(stSql);
    } catch (Exception e)
    {
      this.stError += "<br>ERROR copyBaseline " + stTable + " " + e;
    }

  }

  private String editMake(String stChild, String stR)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    int giSubmitId = 0;
    ResultSet rsField = null;
    String stValue = "";
    try
    {
      String stSave = epsUd.ebEnt.ebUd.request.getParameter("giSubmitId");
      if (stSave != null && stSave.length() > 0)
        giSubmitId = Integer.parseInt(stSave);
      if (stSave != null && stSave.length() > 0 && stSave.equals("9999"))
      {
        ResultSet rsTable = this.ebEnt.dbDyn.ExecuteSql("select * from teb_table where nmTableId=" + stChild);
        ResultSet rsF = this.ebEnt.dbDyn.ExecuteSql("select * from teb_fields f, teb_epsfields ef where f.nmForeignId=ef.nmForeignId and f.nmTabId in (" + rsTable.getString("stTabList") + ") order by f.nmHeaderOrder, f.nmForeignId ");
        rsF.last();
        int iMaxF = rsF.getRow();
        int iCount = this.epsUd.saveTable(rsTable, rsF, iMaxF, stPk, 0, 0);
      }

      String s1 = "</form><form method=post name='form" + stChild + "' id='form" + stChild + "' onsubmit='return myValidation(this)'>"
        + "<input type=hidden name=giSubmitId id=giSubmitId value=0"
        + "<center><table><tr><td valign=middle><h2>" + this.rsProject.getString("ProjectName") + "</h2>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>";
      sbReturn.append(s1);
      sbReturn.append("<td align=left><b>");
      sbReturn.append(this.epsUd.epsEf.fullSchTitle(stR, stPk, nmBaseline));
      sbReturn.append("</b><br>&nbsp;</td>");
      sbReturn.append("</tr></table>");
      if (giSubmitId <= 0)
      {
        String stMake = epsUd.ebEnt.ebUd.request.getParameter("make");
        if (stMake != null)
        {
          if (stMake.equals("lc"))
            giSubmitId = 9990;
          if (stMake.equals("dep"))
            giSubmitId = 9991;
          if (stMake.equals("suc"))
            giSubmitId = 9992;
          if (stMake.equals("inv"))
            giSubmitId = 9993;
          if (stMake.equals("oth"))
            giSubmitId = 9994;
        }
      }
      switch (giSubmitId)
      {
        case 9994:
        case 9974:
          rsField = this.ebEnt.dbDyn.ExecuteSql("select * from teb_fields where nmDataType=44");
          rsField.absolute(1);
          stValue = this.ebEnt.dbDyn.ExecuteSql1("select SchOtherResources from Schedule where nmProjectId=" + stPk + " "
            + "and nmBaseline=" + nmBaseline + " and SchId=" + stR);
          sbReturn.append(this.epsUd.epsEf.makeOtherResources(rsField, stValue, 1));
          break;
        case 9993:
        case 9973:
          rsField = this.ebEnt.dbDyn.ExecuteSql("select * from teb_fields where nmDataType=43");
          rsField.absolute(1);
          stValue = this.ebEnt.dbDyn.ExecuteSql1("select SchInventory from Schedule where nmProjectId=" + stPk + " "
            + "and nmBaseline=" + nmBaseline + " and SchId=" + stR);
          sbReturn.append(this.epsUd.epsEf.makeInventory(rsField, stValue, 1));
          break;
        case 9992:
        case 9972:
          rsField = this.ebEnt.dbDyn.ExecuteSql("select * from teb_fields where nmDataType=45");
          rsField.absolute(1);
          stValue = "";
          sbReturn.append(this.epsUd.epsEf.makeSuccessors(rsField, 1, null, null));
          break;
        case 9991:
        case 9971:
          rsField = this.ebEnt.dbDyn.ExecuteSql("select * from teb_fields where nmDataType=42");
          rsField.absolute(1);
          stValue = "";
          sbReturn.append(this.epsUd.epsEf.makeDependencies(rsField, 1, null, null));
          break;
        case 9990:
        case 9970:
          rsField = this.ebEnt.dbDyn.ExecuteSql("select * from teb_fields where nmDataType=41");
          rsField.absolute(1);
          stValue = this.ebEnt.dbDyn.ExecuteSql1("select SchLaborCategories from Schedule where nmProjectId=" + stPk + " "
            + "and nmBaseline=" + nmBaseline + " and RecId=" + stR);
          sbReturn.append(this.epsUd.epsEf.makeLaborCategories(rsField, stValue, 1));
          break;
        default:
          stError += "<BR> invalid giSubmitId " + giSubmitId;
          break;
      }
    } catch (Exception e)
    {
      stError += "ERROR editMake: " + e;
    }
    return sbReturn.toString();
  }

  public String makeListValue(ResultSet rsFields, String stValue)
  {
    StringBuilder sbReturn = new StringBuilder(5000);
    try
    {
      switch (rsFields.getInt("nmDataType"))
      {
        //case 1:
        case 1:
          try
          {
            int iValue = Integer.parseInt(stValue);
            DecimalFormat df = new DecimalFormat("#,###,###,##0");
            sbReturn.append(df.format(iValue));
          } catch (Exception e)
          {
          }
          break;
        case 31:
        case 5:
          try
          {
            double dValue = Double.parseDouble(stValue);
            DecimalFormat df = new DecimalFormat("#,###,###,##0");
            sbReturn.append(df.format(dValue));
          } catch (Exception e)
          {
          }
          break;
        case 8:
        case 20:
          sbReturn.append(this.ebEnt.ebUd.fmtDateFromDb(stValue));
          break;
        case 47: // Indicators
          sbReturn.append(this.epsUd.epsEf.makeIndicators(rsFields, stValue, 0));
          break;
        default:
          sbReturn.append(stValue);
          break;
      }
    } catch (Exception e)
    {
      stError += "ERROR makeListValue: " + e;
    }
    return sbReturn.toString();
  }
  
  /*
   * Calculate specific requirement's cost for the current requirement
   */
  private void processRequirementCost(int reqID) throws SQLException{
	  //get requirements we need to calculate
      Double rCost = 0.00;
      DecimalFormat df = new DecimalFormat("#########0.00");
      
      //calculate sum of tasks for each requirement cost
      ResultSet stResult = this.ebEnt.dbDyn.ExecuteSql("select l.nmPercent, l.nmRemainder, l.nmToId, r.ReqCost from teb_link l left join requirements r on l.nmFromId=r.RecId and l.nmProjectId=r.nmProjectId and l.nmBaseline=r.nmBaseline where l.nmLinkFlags=1 and l.nmProjectId=" + stPk + " and l.nmBaseline=" + this.nmBaseline + " and nmFromId=" + reqID);
      
      while(stResult.next()){
    	  if(stResult.getString("l.nmRemainder").equals("1"))
    		  rCost += Double.parseDouble(this.ebEnt.dbDyn.ExecuteSql1("select SchCost from schedule where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and RecId=" + stResult.getString("l.nmToId")));
    	  else
    		  rCost += Double.parseDouble(this.ebEnt.dbDyn.ExecuteSql1("select SchCost from schedule where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and RecId=" + stResult.getString("l.nmToId"))) * (Double.parseDouble(stResult.getString("l.nmPercent"))*0.01);
      }
	  this.ebEnt.dbDyn.ExecuteUpdate("update requirements set ReqCost='" + df.format(rCost) + "' where nmProjectId=" + stPk + " and RecId=" + reqID + " and nmBaseline=" + this.nmBaseline);
  }
  
  /*
   * Calculate requirement cost for all requirements linked to this schedule
   */
  private void processScheduleRequirementCost(int scheduleID) throws SQLException{
	  //get requirements we need to calculate
      Double rCost = 0.00;
      DecimalFormat df = new DecimalFormat("#########0.00");
      String[] reqIDs = null;
      int iCount = 0;
      
      //get all requirements that are linked to this schedule
      ResultSet stResult = this.ebEnt.dbDyn.ExecuteSql("select nmFromId from teb_link where nmLinkFlags=1 and nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and nmToId=" + scheduleID);
      stResult.last();
      if(stResult.getRow() > 0)
    	  reqIDs = new String[stResult.getRow()];
      stResult.beforeFirst();
      
      //build requirements string
      while(stResult.next()){
    	  reqIDs[iCount] = stResult.getString("nmFromId");
    	  iCount++;
      }
      
      //calculate requirement costs
      if(iCount > 0){
    	  for(int i=0; i<iCount; i++){
    		  rCost = 0.00;
	    	  stResult = this.ebEnt.dbDyn.ExecuteSql("select l.nmPercent, l.nmRemainder, l.nmToId, r.ReqCost from teb_link l left join requirements r on l.nmFromId=r.RecId and l.nmProjectId=r.nmProjectId and l.nmBaseline=r.nmBaseline where l.nmLinkFlags=1 and l.nmProjectId=" + stPk + " and l.nmBaseline=" + this.nmBaseline + " and nmFromId=" + reqIDs[i]);
	          
	          while(stResult.next()){
	        	  if(stResult.getString("l.nmRemainder").equals("1"))
	        		  rCost += Double.parseDouble(this.ebEnt.dbDyn.ExecuteSql1("select SchCost from schedule where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and RecId=" + stResult.getString("l.nmToId")));
	        	  else
	        		  rCost += Double.parseDouble(this.ebEnt.dbDyn.ExecuteSql1("select SchCost from schedule where nmProjectId=" + stPk + " and nmBaseline=" + this.nmBaseline + " and RecId=" + stResult.getString("l.nmToId"))) * (Double.parseDouble(stResult.getString("l.nmPercent"))*0.01);
	          }
	    	  this.ebEnt.dbDyn.ExecuteUpdate("update requirements set ReqCost='" + df.format(rCost) + "' where nmProjectId=" + stPk + " and RecId=" + reqIDs[i] + " and nmBaseline=" + this.nmBaseline);
    	  }
      }
  }
  
  /*
   * Calculate requirement costs for the current project
   */
  public void processAllRequirementCost(){
	  try{
		  ResultSet stResult = this.ebEnt.dbDyn.ExecuteSql("select nmProjectId, nmBaseline, RecId from requirements");
		  while(stResult.next()){
			  String reqID = stResult.getString("RecId");
			  String projID = stResult.getString("nmProjectId");
			  String bsline = stResult.getString("nmBaseline");
		      Double rCost = 0.00;
		      DecimalFormat df = new DecimalFormat("#########0.00");
		      
		      //calculate sum of tasks for each requirement cost
		      ResultSet rResult = this.ebEnt.dbDyn.ExecuteSql("select l.nmPercent, l.nmRemainder, l.nmToId, r.ReqCost from teb_link l left join requirements r on l.nmFromId=r.RecId and l.nmProjectId=r.nmProjectId and l.nmBaseline=r.nmBaseline where l.nmLinkFlags=1 and l.nmProjectId=" + projID + " and l.nmBaseline=" + bsline + " and nmFromId=" + reqID);
		      
		      while(rResult.next()){
		    	  if(rResult.getString("l.nmRemainder").equals("1"))
		    		  rCost += Double.parseDouble(this.ebEnt.dbDyn.ExecuteSql1("select SchCost from schedule where nmProjectId=" + projID + " and nmBaseline=" + bsline + " and RecId=" + rResult.getString("l.nmToId")));
		    	  else
		    		  rCost += Double.parseDouble(this.ebEnt.dbDyn.ExecuteSql1("select SchCost from schedule where nmProjectId=" + projID + " and nmBaseline=" + bsline + " and RecId=" + rResult.getString("l.nmToId"))) * (Double.parseDouble(rResult.getString("l.nmPercent"))*0.01);
		      }
			  this.ebEnt.dbDyn.ExecuteUpdate("update requirements set ReqCost='" + df.format(rCost) + "' where nmProjectId=" + projID + " and RecId=" + reqID + " and nmBaseline=" + bsline);  
		  }
	  } catch (Exception e)
	  {
	      stError += "ERROR makeListValue: " + e;
	  }
  }
  
  private String makeDependenciesTB(ResultSet rs, String stChild){
	  //display dependencies table
	  String stFromProject = "";
	  String stFromId = "";
	  String stLag = "";
	  String stType = "";
	  String stFromBaseline = "";
	  int iRecMax = 0;
	  String stPrj = "";
	  String stTsk = "";
	  String newtxt = "";
	  String stReturn = "";
	  
	  try{
		  if (this.stPk != null && this.stPk.length() > 0 && stChild != null && stChild.length() > 0)
		  {
		    stPrj = this.stPk;
		    stChild = "21";
		    stTsk = rs.getInt("RecId")+"";
		  } else
		  {
		    stPrj = this.ebEnt.ebUd.request.getParameter("pk");
		    stChild = this.ebEnt.ebUd.request.getParameter("child");
		    stTsk = rs.getInt("RecId")+"";
		  }
		  String stBaseline = this.ebEnt.dbDyn.ExecuteSql1("select CurrentBaseline from Projects where RecId=" + stPrj);
		  String stSql = "select * from teb_link where nmProjectId=" + stPrj + " and nmBaseline=" + stBaseline + " and nmLinkFlags=2 "
		    + "and nmToId=" + stTsk + " and nmToProject=nmProjectId order by nmFromId";
		  ResultSet res = this.ebEnt.dbDyn.ExecuteSql(stSql);
		  res.last();
		  iRecMax = res.getRow();
		
		 
		  stReturn += "<br><table bgcolor='blue' cellpadding='1'><tr class=d1>";
	      //stReturn += "<td>Action</td><td>Project</td><td>Task ID</td><td>Dependency</td><td>Lag [optional]</td></tr>";
	      stReturn += "<td>Task ID</td><td>Dependency</td><td>Lag [optional]</td></tr>";
	      //ResultSet rsPrj = this.ebEnt.dbDyn.ExecuteSql("select * from Projects order by ProjectName ");
	      //rsPrj.last();
	      //int iMaxPrj = rsPrj.getRow();
	
	      for (int k = 1; k <= (iRecMax + 1); k++)
	      {
	        stReturn += "<tr class=d0>";
	        if (k < (iRecMax + 1))
	        {
	          res.absolute(k);
	          stFromProject = res.getString("nmFromProject");
	          stFromId = res.getString("nmFromId");
	          stLag = res.getString("nmPercent");
	          stType = res.getString("stComment");
	          //stReturn += "<td valign=top align=center>";
	          //stReturn += "<input type=image name=del id=del value=" + k + " onClick=\"return setSubmitId2(9991," + k + ");\" src='./common/b_drop.png'></td>";
	        } else
	        {
	          stFromProject = stPrj;
	          stFromId = "";
	          stLag = "";
	          stType = "fs";
	          //stReturn += "<td>Add new:</td>";
	          newtxt = "Add:<br>";
	        }
	        
	        //stReturn += "<td><select name=prj_" + k + " id=prj_" + k + ">";
	        
	
	        /*for (int iLc = 1; iLc <= iMaxPrj; iLc++)
	        {
	          rsPrj.absolute(iLc);
	          stReturn += this.ebEnt.ebUd.addOption2(rsPrj.getString("ProjectName"), rsPrj.getString("RecId"), stFromProject);
	        }*/
	        stReturn += "<td align=right>"+newtxt+"<input type=text name=did_" + k + " id=did_" + k + " value=\"" + stFromId + "\" size=5 style='text-align:right'></td>";
	        stReturn += "<td>" + this.epsUd.epsEf.ConstraintList("type_" + k, stType, "") + "</td>";
	        stReturn += "<td align=right><input type=text name=lag_" + k + " id=lag_" + k + " value=\"" + stLag + "\" size=5 style='text-align:right'> (days)</td>";
	        stReturn += "</tr>";
	      }
	
	      stReturn += "</table><br>"
	        + "<input type=hidden name=dmax id=dmax value='" + (iRecMax + 1) + "'>";
	        //+ "<input type=hidden name=giVar id=giVar value='-1'>";
	  } catch (Exception e)
      {
        stError += "ERROR makeDependencies: " + e;
      }
      return stReturn;
  }
}
