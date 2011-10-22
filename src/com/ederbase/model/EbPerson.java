package com.ederbase.model;

public class EbPerson
{
  private EbEnterprise ebEnt = null;
  private int nmPersonId = 0;
  private int nmCompanyId = 0;
  private String stCountry = "";
  private String stCompany = "";
  private String stPhone = "";
  private String stAddress = "";
  private String stAddress2 = "";
  private String stCity = "";
  private String stState = "";
  private String stZip = "";
  private String stWeb = "";
  private String stEmail = "";
  private String stFirstName = "";
  private String stMiddleName = "";
  private String stLastName = "";

  public EbPerson(EbEnterprise ebEnt)
  {
    this.ebEnt = ebEnt;
    this.nmCompanyId = 0;
    this.stCountry = ebEnt.ebUd.getCountry();
    this.stCompany = "";
    this.stPhone = "";
    this.stAddress = "";
    this.stAddress2 = "";
    this.stCity = "";
    this.stState = "";
    this.stZip = "";
    this.stWeb = "";
    this.stEmail = "";
    this.stFirstName = "";
    this.stLastName = "";
  }

  public void setCity(String stCity)
  {
    this.stCity = stCity;
  }

  public void setState(String stState)
  {
    this.stState = stState;
  }

  public void setCountry(String stCountry)
  {
    this.stCountry = stCountry;
  }

  public void setCompany(String stCompany)
  {
    this.stCompany = stCompany;
  }

  public void setName(String stName)
  {
    String[] aName = null;
    if ((stName != null) && (stName.length() > 1))
    {
      aName = stName.split(" ");
      this.stFirstName = aName[0];
      if (aName.length == 2)
      {
        this.stLastName = aName[1];
      }
      else if (aName.length >= 3)
      {
        this.stMiddleName = aName[1];
        this.stLastName = aName[2];
      }
    }
  }

  public void setAddress(String stAddress, String stAddress2)
  {
    this.stAddress = stAddress;
    this.stAddress2 = stAddress2;
  }

  public void setZip(String stZip)
  {
    this.stZip = stZip;
  }

  public void setWeb(String stWeb)
  {
    this.stWeb = stWeb;
  }

  public void setEmail(String stEmail)
  {
    this.stEmail = stEmail;
  }

  public void setPhone(String stPhone)
  {
    this.stPhone = stPhone;
  }

  public int save(EbCompany ebComp)
  {
    this.nmPersonId = 0;
    if (ebComp != null) {
      this.nmCompanyId = ebComp.save();
    }
    int nmPhoneId = this.ebEnt.ebNorm.getPhoneId(this.stPhone, this.stCountry);
    int nmEmailId = this.ebEnt.ebNorm.getEmailId(this.stEmail);

    if ((nmPhoneId > 0) || (nmEmailId > 0))
    {
      this.nmPersonId = this.ebEnt.ebNorm.getPersonId(this.stFirstName, this.stMiddleName, this.stLastName, nmPhoneId, nmEmailId);
    }
    if ((this.nmPersonId > 0) && (this.nmCompanyId > 0))
    {
      this.ebEnt.ebNorm.addCompanyRef(this.nmCompanyId, 1, this.nmPersonId);
    }
    return this.nmPersonId;
  }
}