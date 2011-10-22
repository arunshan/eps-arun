package com.ederbase.model;

import java.sql.ResultSet;

public class EbNormalized
{
  private EbEnterprise ebEnt = null;
  private String stError = "";

  public EbNormalized(EbEnterprise ebEnt)
  {
    this.ebEnt = ebEnt;
  }

  public int getPhoneId(String stPhone, String stCountry)
  {
    int nmPhoneId = 0;
    stPhone = EbStatic.stripPhone(stPhone);
    if ((stPhone.length() > 2) && (!stPhone.equals("7140000000")))
    {
      if ((stCountry == null) || (stCountry.length() != 2))
        stCountry = "US";
      String stSql = "select max(RecId) from X25Phone where stPhone = " + this.ebEnt.dbEnterprise.fmtDbString(stPhone) + " and stCountry=" + this.ebEnt.dbEnterprise.fmtDbString(stCountry) + " ";
      nmPhoneId = this.ebEnt.dbEnterprise.ExecuteSql1n(stSql);
      if (nmPhoneId <= 0)
      {
        nmPhoneId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from X25Phone");
        nmPhoneId++;
        this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25Phone (RecId,stPhone,stCountry) values(" + nmPhoneId + "," + this.ebEnt.dbEnterprise.fmtDbString(stPhone) + "," + this.ebEnt.dbEnterprise.fmtDbString(stCountry) + ")");
      }
    }
    return nmPhoneId;
  }

  public int getEmailId(String stEmail, String stFullName)
  {
    int nmEmailId = getEmailId(stEmail);
    if ((nmEmailId > 0) && (stFullName != null) && (stFullName.length() > 1))
    {
      int i = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from X25RefUser where nmRefType=1 and nmUserId=" + nmEmailId);

      if (i <= 0)
      {
        String[] aName = stFullName.split(" ");
        String stFirstName = "";
        String stMiddleName = "";
        String stLastName = "";

        if (aName.length <= 1)
        {
          stFirstName = aName[0].trim();
        } else if (aName.length <= 2)
        {
          stFirstName = aName[0].trim();
          stLastName = aName[1].trim();
        }
        else {
          stFirstName = aName[0].trim();
          stMiddleName = aName[1].trim();
          stLastName = aName[2].trim();
        }
        i = getPersonId(stFirstName, stMiddleName, stLastName, 0, nmEmailId);
      }
    }
    return nmEmailId;
  }

  public int getEmailId(String stEmail)
  {
    int nmEmailId = 0;
    if (stEmail != null)
    {
      stEmail = stEmail.replace("\"", "");
      if (EbStatic.isEmail(stEmail) == true)
      {
        stEmail = this.ebEnt.dbEnterprise.fmtDbString(stEmail);
        String stSql = "select max(RecId) from X25User where stEMail = " + stEmail + " ";
        nmEmailId = this.ebEnt.dbEnterprise.ExecuteSql1n(stSql);
        if (nmEmailId <= 0)
        {
          nmEmailId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from X25User");
          nmEmailId++;
          this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25User (RecId,stEmail) values(" + nmEmailId + "," + stEmail + ")");
        }
      }
    }
    return nmEmailId;
  }

  public int getWebId(String stWeb)
  {
    int nmWebId = 0;
    if (EbStatic.isMainWeb(stWeb) == true)
    {
      stWeb = this.ebEnt.dbEnterprise.fmtDbString(stWeb);
      String stSql = "select max(nmWebId) from X25Website where stUrl = " + stWeb + " ";
      nmWebId = this.ebEnt.dbEnterprise.ExecuteSql1n(stSql);
      if (nmWebId <= 0)
      {
        nmWebId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(nmWebId) from X25Website");
        nmWebId++;
        this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25Website (nmWebId,stUrl,dtLastUpdate) values(" + nmWebId + "," + stWeb + ",now())");
      }
    }
    return nmWebId;
  }

  public String fixAddressZip()
  {
    String stReturn = "<br>fixAddressZip: ";
    try
    {
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql("select distinct nmZipId from X25Address");
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        stReturn = stReturn + iMax;
        for (int iA = 1; iA <= iMax; iA++)
        {
          rs.absolute(iA);
          ResultSet rsZ = this.ebEnt.dbCommon.ExecuteSql("select * from X25Zip where RecId=" + rs.getString("nmZipId"));
          String stZipCity = "";
          if (rsZ != null)
          {
            rsZ.last();
            int iMaxZ = rsZ.getRow();
            if (iMaxZ == 1)
            {
              rsZ.absolute(1);
              stZipCity = rsZ.getString("stCity") + ", " + rsZ.getString("stStateShort") + ", " + rsZ.getString("stZipCode");
            }
            else {
              this.stError = (this.stError + "<BR>ERROR: no zip found zipid: " + rs.getString("nmZipId"));
            }
          }
          else {
            this.stError = (this.stError + "<BR>ERROR: db no zip found zipid: " + rs.getString("nmZipId"));
          }
          this.ebEnt.dbEnterprise.ExecuteUpdate("update X25Address set stZipCity=" + this.ebEnt.dbEnterprise.fmtDbString(stZipCity) + " where nmZipId=" + rs.getString("nmZipId"));
        }
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<BR>ERROR: " + e);
    }
    return stReturn;
  }

  public int getAddressId(String stAddress, String stAddress2, String stZip, String stCity, String stState, String stCountry)
  {
    int nmAddressId = 0;
    int nmZipId = 0;
    if ((stZip != null) && (stZip.length() > 1) && (stCountry != null) && (stCountry.length() == 2))
    {
      if ((stCity != null) && (stCity.length() > 2) && (stState != null) && (stState.length() == 2))
        nmZipId = this.ebEnt.dbCommon.ExecuteSql1n("SELECT max(RecId) FROM X25Zip where stZipCode=" + this.ebEnt.dbEnterprise.fmtDbString(stZip) + " and stCountry = " + this.ebEnt.dbEnterprise.fmtDbString(stCountry) + " and stCity = " + this.ebEnt.dbEnterprise.fmtDbString(stCity) + " and stStateShort = " + this.ebEnt.dbEnterprise.fmtDbString(stState) + " ");
      else
        nmZipId = this.ebEnt.dbCommon.ExecuteSql1n("SELECT max(RecId) FROM X25Zip where stZipCode=" + this.ebEnt.dbEnterprise.fmtDbString(stZip) + " and stCountry = " + this.ebEnt.dbEnterprise.fmtDbString(stCountry) + "");
      if (nmZipId <= 0)
      {
        nmZipId = this.ebEnt.dbCommon.ExecuteSql1n("SELECT max(RecId) FROM X25Zip where stZipCode=" + this.ebEnt.dbEnterprise.fmtDbString(stZip) + " and stCountry = " + this.ebEnt.dbEnterprise.fmtDbString(stCountry) + "");
        if (nmZipId > 0)
        {
          this.ebEnt.dbCommon.ExecuteUpdate("update X25Zip set stCity = " + this.ebEnt.dbEnterprise.fmtDbString(stCity) + ", stStateShort=" + this.ebEnt.dbEnterprise.fmtDbString(stState) + " where stZipCode=" + this.ebEnt.dbEnterprise.fmtDbString(stZip) + " and stCountry = " + this.ebEnt.dbEnterprise.fmtDbString(stCountry) + "");
        }
        else {
          nmZipId = this.ebEnt.dbCommon.ExecuteSql1n("SELECT max(RecId) FROM X25Zip");
          nmZipId++;
          if ((stCity == null) || (stCity.length() <= 0))
            stCity = "??City: " + stZip;
          if ((stState == null) || (stState.length() != 2))
            stState = "??";
          this.ebEnt.dbCommon.ExecuteUpdate("insert into X25Zip (RecId,stZipCode,stCity,stStateShort,stCountry) values(" + nmZipId + "," + this.ebEnt.dbEnterprise.fmtDbString(stZip) + "," + this.ebEnt.dbEnterprise.fmtDbString(stCity) + "," + this.ebEnt.dbEnterprise.fmtDbString(stState) + "," + this.ebEnt.dbEnterprise.fmtDbString(stCountry) + " ) ");
        }
        if (nmZipId <= 0)
          this.stError = (this.stError + "<br>ERROR Zip: no found: ZipCode=" + this.ebEnt.dbEnterprise.fmtDbString(stZip) + " and stCountry = " + stCountry);
      }
    }
    if ((stAddress != null) && (stAddress.length() > 2) && (nmZipId > 0))
    {
      String stSql = "select max(RecId) from X25Address where stAddress1 = " + this.ebEnt.dbEnterprise.fmtDbString(stAddress) + " and stAddress2 = " + this.ebEnt.dbEnterprise.fmtDbString(stAddress2) + " and nmZipId = " + nmZipId + " ";
      nmAddressId = this.ebEnt.dbEnterprise.ExecuteSql1n(stSql);
      if (nmAddressId <= 0)
      {
        nmAddressId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from X25Address");
        nmAddressId++;
        this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25Address (RecId,stAddress1,stAddress2,nmZipId) values(" + nmAddressId + "," + this.ebEnt.dbEnterprise.fmtDbString(stAddress) + "," + this.ebEnt.dbEnterprise.fmtDbString(stAddress2) + "," + nmZipId + ")");
      }
    }
    return nmAddressId;
  }

  public int getCompanyId(String stCompany, int nmAddressId, int nmPhoneId, int nmEmailId, int nmWebId)
  {
    int nmCompanyId = 0;
    if ((stCompany != null) && (stCompany.length() > 1) && ((nmPhoneId > 0) || (nmEmailId > 0) || (nmWebId > 0) || (nmAddressId > 0)))
    {
      if (nmAddressId > 0)
      {
        nmCompanyId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(c.RecId) from X25Company c, X25RefAddress ra  where ra.nmRefType=2 and ra.nmAddressId=" + nmAddressId + " and ra.nmRefId=c.RecId and c.stCompanyName=" + this.ebEnt.dbEnterprise.fmtDbString(stCompany) + "");
      }
      if ((nmCompanyId <= 0) && (nmPhoneId > 0))
      {
        nmCompanyId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(c.RecId) from X25Company c, X25RefPhone rp  where rp.nmRefType=2 and rp.nmPhoneId=" + nmPhoneId + " and rp.nmRefId=c.RecId and c.stCompanyName=" + this.ebEnt.dbEnterprise.fmtDbString(stCompany) + "");
      }
      if ((nmCompanyId <= 0) && (nmEmailId > 0))
      {
        nmCompanyId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(c.RecId) from X25Company c, X25RefUser ru  where ru.nmRefType=2 and ru.nmUserId=" + nmEmailId + " and ru.nmPersonId=c.RecId and c.stCompanyName=" + this.ebEnt.dbEnterprise.fmtDbString(stCompany) + "");
      }
      if ((nmCompanyId <= 0) && (nmWebId > 0))
      {
        nmCompanyId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(c.RecId) from X25Company c, X25RefWeb rw  where rw.nmRefType=2 and rw.nmWebId=" + nmWebId + " and rw.nmRefId=c.RecId and c.stCompanyName=" + this.ebEnt.dbEnterprise.fmtDbString(stCompany) + "");
      }
      if (nmCompanyId <= 0)
      {
        nmCompanyId = getCompanyId(stCompany);
      }
      if (nmCompanyId > 0)
      {
        addAddressRef(nmAddressId, 2, nmCompanyId);
        addPhoneRef(nmPhoneId, 2, nmCompanyId);
        addUserRef(nmEmailId, 2, nmCompanyId);
        addWebRef(nmWebId, 2, nmCompanyId);
      }
    }
    return nmCompanyId;
  }

  public void addAddressRef(int nmAddressId, int nmRefType, int nmRefId)
  {
    if ((nmAddressId > 0) && (nmRefType > 0) && (nmRefId > 0))
    {
      int i = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from X25RefAddress where nmAddressId=" + nmAddressId + " and nmRefType=" + nmRefType + " and nmRefId=" + nmRefId + " ");
      if (i <= 0)
      {
        this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25RefAddress (nmAddressId, nmRefType, nmRefId) values(" + nmAddressId + " ," + nmRefType + " ," + nmRefId + " ) ");
      }
    }
  }

  public void addCompanyRef(int nmCompanyId, int nmRefType, int nmRefId)
  {
    if ((nmCompanyId > 0) && (nmRefType > 0) && (nmRefId > 0))
    {
      int i = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from X25RefCompany where nmCompanyId=" + nmCompanyId + " and nmRefType=" + nmRefType + " and nmRefId=" + nmRefId + " ");
      if (i <= 0)
      {
        this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25RefCompany (nmCompanyId, nmRefType, nmRefId) values(" + nmCompanyId + " ," + nmRefType + " ," + nmRefId + " ) ");
      }
    }
  }

  public void addUserRef(int nmUserId, int nmRefType, int nmRefId)
  {
    if ((nmUserId > 0) && (nmRefType > 0) && (nmRefId > 0))
    {
      int i = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from X25RefUser where nmUserId=" + nmUserId + " and nmRefType=" + nmRefType + " and nmPersonId=" + nmRefId + " ");
      if (i <= 0)
      {
        this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25RefUser (nmUserId, nmRefType, nmPersonId) values(" + nmUserId + " ," + nmRefType + " ," + nmRefId + " ) ");
      }
    }
  }

  public void addPhoneRef(int nmPhoneId, int nmRefType, int nmRefId)
  {
    if ((nmPhoneId > 0) && (nmRefType > 0) && (nmRefId > 0))
    {
      int i = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from X25RefPhone where nmPhoneId=" + nmPhoneId + " and nmRefType=" + nmRefType + " and nmRefId=" + nmRefId + " ");
      if (i <= 0)
      {
        this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25RefPhone (nmPhoneId, nmRefType, nmRefId) values(" + nmPhoneId + " ," + nmRefType + " ," + nmRefId + " ) ");
      }
    }
  }

  public void addWebRef(int nmWebId, int nmRefType, int nmRefId)
  {
    if ((nmWebId > 0) && (nmRefType > 0) && (nmRefId > 0))
    {
      int i = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from X25RefWeb where nmWebId=" + nmWebId + " and nmRefType=" + nmRefType + " and nmRefId=" + nmRefId + " ");
      if (i <= 0)
      {
        this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25RefWeb (nmWebId, nmRefType, nmRefId) values(" + nmWebId + " ," + nmRefType + " ," + nmRefId + " ) ");
        if (nmRefType == 2)
        {
          this.ebEnt.dbEnterprise.ExecuteUpdate("update X25Website set nmCompanyId=" + nmRefId + " where nmWebId=" + nmWebId);
        }
      }
    }
  }

  public int getCompanyId(String stCompany)
  {
    int nmCompanyId = 0;
    if ((stCompany != null) && (stCompany.length() > 1))
    {
      String stSql = "select max(RecId) from X25Company where stCompanyName = " + this.ebEnt.dbEnterprise.fmtDbString(stCompany) + " ";
      nmCompanyId = this.ebEnt.dbEnterprise.ExecuteSql1n(stSql);
      if (nmCompanyId <= 0)
      {
        nmCompanyId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from X25Company");
        nmCompanyId++;
        this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25Company (RecId,stCompanyName,dtCreated,dtLastModified) values(" + nmCompanyId + "," + this.ebEnt.dbEnterprise.fmtDbString(stCompany) + ",now(),now())");
      }
    }
    return nmCompanyId;
  }

  public void addHistory(int nmRefType, int nmRefId, String stDate, String stComment, int nmRefType2, int nmRefId2)
  {
    if ((stDate != null) && (stDate.length() > 5))
      stDate = this.ebEnt.dbEnterprise.fmtDbString(stDate);
    else
      stDate = this.ebEnt.dbEnterprise.getNowString();
    this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25HistoryLog (nmUserId,nmRefType,nmRefId,dtEventStartTime,dtEventEndTIme,stComment,nmRefType2,nmRefId2) values(" + this.ebEnt.ebUd.getLoginId() + "," + nmRefType + "," + nmRefId + "," + stDate + "," + stDate + "," + this.ebEnt.dbEnterprise.fmtDbString(stComment) + "," + nmRefType2 + "," + nmRefId2 + ")");
  }

  public int getPersonId(String stFirstName, String stMiddleName, String stLastName, String stPhone, String stCountry, String stEMail)
  {
    int nmPhoneId = getPhoneId(stPhone, stCountry);
    int nmEmailId = getEmailId(stEMail);
    return getPersonId(stFirstName, stMiddleName, stLastName, nmPhoneId, nmEmailId);
  }

  public int getPersonId(String stFirstName, String stMiddleName, String stLastName, int nmPhoneId, int nmEmailId)
  {
    int nmPersonId = 0;

    if ((stFirstName != null) && (stFirstName.trim().length() > 0))
    {
      stFirstName = this.ebEnt.dbEnterprise.fmtDbString(stFirstName.trim().replace("\"", ""));
      if (stLastName == null)
        stLastName = "";
      if (stMiddleName == null)
        stMiddleName = "";
      stMiddleName = this.ebEnt.dbEnterprise.fmtDbString(stMiddleName.trim().replace("\"", ""));
      stLastName = this.ebEnt.dbEnterprise.fmtDbString(stLastName.trim().replace("\"", ""));

      if ((nmPersonId <= 0) && (nmPhoneId > 0))
      {
        nmPersonId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(c.RecId) from X25Person c, X25RefPhone rp  where rp.nmRefType=1 and rp.nmPhoneId=" + nmPhoneId + " and rp.nmRefId=c.RecId and c.stFirstName=" + stFirstName + " and c.stMiddleName=" + stMiddleName + " and c.stLastName=" + stLastName + " ");
      }
      if ((nmPersonId <= 0) && (nmEmailId > 0))
      {
        nmPersonId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(c.RecId) from X25Person c, X25RefUser ru  where ru.nmRefType=1 and ru.nmUserId=" + nmEmailId + " and ru.nmPersonId=c.RecId and c.stFirstName=" + stFirstName + " and c.stMiddleName=" + stMiddleName + " and c.stLastName=" + stLastName + " ");
      }
      if (nmPersonId <= 0)
      {
        nmPersonId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from X25Person");
        nmPersonId++;

        this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25Person (RecId,stFirstName,stMiddleName,stLastName) values(" + nmPersonId + "," + stFirstName + "," + stMiddleName + "," + stLastName + ")");
      }
      else
      {
        stFirstName = "xx";
      }
      if (nmPersonId > 0)
      {
        addPhoneRef(nmPhoneId, 1, nmPersonId);
        addUserRef(nmEmailId, 1, nmPersonId);
      }
    }
    return nmPersonId;
  }

  public String makeDropDown(String stSql, String stName, String stSelected)
  {
    String stReturn = "<select name=\"" + stName + "\">";
    try
    {
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        for (int iO = 1; iO <= iMax; iO++)
        {
          rs.absolute(iO);
          if (rs.getString(1).trim().equals(stSelected))
            stReturn = stReturn + "\n<option SELECTED value=\"" + rs.getString(1) + "\">" + rs.getString(2) + "</option>";
          else
            stReturn = stReturn + "\n<option value=\"" + rs.getString(1) + "\">" + rs.getString(2) + "</option>";
        }
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "ERROR: makeDropDown " + stSql + " :" + e);
    }
    stReturn = stReturn + "</select>";
    return stReturn;
  }

  public String AddOptionList(String stId, String stLabel, String stSel)
  {
    String stReturn = "";
    if (stId.equals(stSel))
    {
      stReturn = "\n<option value=\"" + stId + "\" selected>";
    }
    else {
      stReturn = "<option value=\"" + stId + "\">";
    }
    stReturn = stReturn + stLabel + "</option>";
    return stReturn;
  }

  public String mergePerson(String stFirstName, String stMiddleName, String stLastName)
  {
    String stReturn = "";
    try
    {
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql("select RecId from X25Person where stFirstName=" + this.ebEnt.dbEnterprise.fmtDbString(stFirstName) + " and stMiddleName=" + this.ebEnt.dbEnterprise.fmtDbString(stMiddleName) + " and stLastName=" + this.ebEnt.dbEnterprise.fmtDbString(stLastName) + " order by RecId DESC");

      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        int[] iaP = new int[iMax];
        for (int iP = 1; iP <= iMax; iP++)
        {
          rs.absolute(iP);
          iaP[(iP - 1)] = rs.getInt("RecId");
        }
        stReturn = stReturn + mergePerson(iaP);
      }
      deleteZombies();
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR mergePerson: " + e);
    }
    return stReturn;
  }

  public String mergePerson(int[] iaPerson)
  {
    String stReturn = "";
    if ((iaPerson != null) && (iaPerson.length > 1))
    {
      for (int iP = 1; iP < iaPerson.length; iP++)
      {
        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25RefUser set nmPersonId=" + iaPerson[0] + " where nmRefType=1 and nmPersonId=" + iaPerson[iP]);

        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25RefPhone set nmRefId=" + iaPerson[0] + " where nmRefType=1 and nmRefId=" + iaPerson[iP]);

        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25RefCompany set nmRefId=" + iaPerson[0] + " where nmRefType=1 and nmRefId=" + iaPerson[iP]);

        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25RefTask set nmRefId=" + iaPerson[0] + " where nmRefType=1 and nmRefId=" + iaPerson[iP]);

        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25RefWeb set nmRefId=" + iaPerson[0] + " where nmRefType=1 and nmRefId=" + iaPerson[iP]);

        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25HistoryLog set nmRefId=" + iaPerson[0] + " where nmRefType=1 and nmRefId=" + iaPerson[iP]);

        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25Account set nmRefId=" + iaPerson[0] + " where nmRefType=1 and nmRefId=" + iaPerson[iP]);

        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25AuditTrail set nmRefId=" + iaPerson[0] + " where nmRefType=1 and nmRefId=" + iaPerson[iP]);

        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25CampaignDetails set nmRefId=" + iaPerson[0] + " where nmRefType=1 and nmRefId=" + iaPerson[iP]);

        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25Communications set nmPersonId=" + iaPerson[0] + " where nmPersonId=" + iaPerson[iP]);

        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25Education set nmPersonId=" + iaPerson[0] + " where nmPersonId=" + iaPerson[iP]);

        this.ebEnt.dbEnterprise.ExecuteUpdate("delete from X25Person where RecId=" + iaPerson[iP]);
      }
    }
    return stReturn;
  }

  public String deleteZombies(String stTable, int nmRefType)
  {
    String stReturn = "";
    ResultSet rs = null;
    int iMax = 0;
    try
    {
      switch (nmRefType)
      {
      case 1:
        rs = this.ebEnt.dbEnterprise.ExecuteSql("select * from " + stTable + " r " + " left join X25Person p on p.RecId=r.nmPersonId" + " where p.RecId is null ");

        if (rs == null)
          break;
        rs.last();
        iMax = rs.getRow();
        for (int iD = 1; iD <= iMax; iD++)
        {
          rs.absolute(iD);
          this.ebEnt.dbEnterprise.ExecuteUpdate("delete from " + stTable + " where nmPersonId=" + rs.getString("nmPersonId"));
        }
        break;
      default:
        stReturn = stReturn + "<br>ERROR: deleteZombies2: nmRefType not implemented " + stTable + ": " + nmRefType;
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<BR>ERROR deleteZombies: " + e);
    }
    return stReturn;
  }

  public String deleteZombies(String stTable, int nmRefType, String stRefId)
  {
    String stReturn = "";
    ResultSet rs = null;
    int iMax = 0;
    try
    {
      switch (nmRefType)
      {
      case 1:
        rs = this.ebEnt.dbEnterprise.ExecuteSql("select * from " + stTable + " r " + " left join X25Person p on r.nmRefType=1 and p.RecId=r." + stRefId + " where r.nmRefType=1 and p.RecId is null ");

        if (rs == null)
          break;
        rs.last();
        iMax = rs.getRow();
        for (int iD = 1; iD <= iMax; iD++)
        {
          rs.absolute(iD);
          this.ebEnt.dbEnterprise.ExecuteUpdate("delete from " + stTable + " where nmRefType=1 and " + stRefId + "=" + rs.getString(stRefId));
        }
        break;
      case 2:
        rs = this.ebEnt.dbEnterprise.ExecuteSql("select * from " + stTable + " r " + " left join X25Company c on r.nmRefType=2 and c.RecId=r." + stRefId + " where r.nmRefType=2 and c.RecId is null ");

        if (rs == null)
          break;
        rs.last();
        iMax = rs.getRow();
        for (int iD = 1; iD <= iMax; iD++)
        {
          rs.absolute(iD);
          this.ebEnt.dbEnterprise.ExecuteUpdate("delete from " + stTable + " where nmRefType=2 and " + stRefId + "=" + rs.getString(stRefId));
        }
        break;
      default:
        stReturn = stReturn + "<br>ERROR: deleteZombies: nmRefType not implemented: " + nmRefType;
      }
    }
    catch (Exception e)
    {
      this.stError = (this.stError + "<BR>ERROR deleteZombies: " + e);
    }
    return stReturn;
  }

  public String deleteZombies()
  {
    String stReturn = "<br>deleteZombies<br>";
    stReturn = stReturn + deleteZombies("X25RefUser", 1, "nmPersonId");
    stReturn = stReturn + deleteZombies("X25RefUser", 2, "nmPersonId");
    stReturn = stReturn + deleteZombies("X25RefPhone", 1, "nmRefId");
    stReturn = stReturn + deleteZombies("X25RefPhone", 2, "nmRefId");

    stReturn = stReturn + deleteZombies("X25RefCompany", 1, "nmRefId");
    stReturn = stReturn + deleteZombies("X25RefCompany", 2, "nmRefId");
    stReturn = stReturn + deleteZombies("X25RefTask", 1, "nmRefId");
    stReturn = stReturn + deleteZombies("X25RefTask", 2, "nmRefId");
    stReturn = stReturn + deleteZombies("X25RefWeb", 1, "nmRefId");
    stReturn = stReturn + deleteZombies("X25RefWeb", 2, "nmRefId");
    stReturn = stReturn + deleteZombies("X25HistoryLog", 1, "nmRefId");
    stReturn = stReturn + deleteZombies("X25HistoryLog", 2, "nmRefId");
    stReturn = stReturn + deleteZombies("X25AuditTrail", 1, "nmRefId");
    stReturn = stReturn + deleteZombies("X25AuditTrail", 2, "nmRefId");
    stReturn = stReturn + deleteZombies("X25CampaignDetails", 1, "nmRefId");
    stReturn = stReturn + deleteZombies("X25CampaignDetails", 2, "nmRefId");

    stReturn = stReturn + deleteZombies("X25Education", 1);

    return stReturn;
  }
}