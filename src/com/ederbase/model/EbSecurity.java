package com.ederbase.model;

import java.sql.ResultSet;

public class EbSecurity
{
  private String stError = "";
  private ResultSet rsUser = null;
  private EbEnterprise ebEnt;

  public EbSecurity(EbEnterprise ebEnt)
  {
    this.ebEnt = ebEnt;
    this.stError = "";
  }

  public int checkSecurity(ResultSet rsA)
  {
    int iReturn = 0;
    int nmSecurityId = -1;
    String stRule = "";
    try
    {
      if (rsA != null)
      {
        nmSecurityId = rsA.getInt("nmSecurity");
        if (nmSecurityId == 0)
        {
          iReturn = 1;
        }
        else {
          stRule = rsA.getString("stRule");
          String[] aRule = stRule.split(",");
          iReturn = 1;
          for (int i = 0; (i < aRule.length) && (iReturn == 1); i++)
          {
            iReturn = 0;

            switch (aRule[i].trim().charAt(0))
            {
            case 'A':
              if (this.rsUser == null)
                continue;
              if ((this.rsUser.getInt("nmPriviledge") & 0xC) == 0) continue;
              iReturn = 1; break;
            case 'S':
              if (this.rsUser == null)
                continue;
              if ((this.rsUser.getInt("nmPriviledge") & 0x4000) == 0) continue;
              iReturn = 1; break;
            case 'L':
              if (this.ebEnt.ebUd.getLoginId() <= 0)
                continue;
              iReturn = 1;
              if (this.rsUser != null)
                continue;
              this.rsUser = this.ebEnt.dbEnterprise.ExecuteSql("select u.*,p.* from X25User u left join X25RefUser ru on ru.nmUserId=" + this.ebEnt.ebUd.getLoginId() + " and ru.nmRefType=1 left join X25Person p on ru.nmPersonId=p.RecId where u.RecId = " + this.ebEnt.ebUd.getLoginId());
            }
          }
        }

      }

    }
    catch (Exception e)
    {
      iReturn = 0;
    }
    return iReturn;
  }

  public String getError()
  {
    return this.stError;
  }
}