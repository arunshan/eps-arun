package com.ederbase.model;

public class EbCompany
{
  private EbEnterprise ebEnt = null;
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

  public EbCompany(EbEnterprise ebEnt)
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

  public void setAddress(String stAddress, String stAddress2)
  {
    this.stAddress = stAddress;
    this.stAddress2 = stAddress2;
  }

  public void setAddress(String stAddress) {
    String[] aAddress = null;
    if ((stAddress != null) && (stAddress.length() > 3))
    {
      aAddress = stAddress.split(",");
      this.stAddress = aAddress[0];
      this.stAddress2 = "";
      this.stCity = aAddress[1];
      if (aAddress[2].length() > 2)
      {
        String[] aZip = aAddress[2].trim().split(" ");
        this.stState = aZip[0];
        if (aZip.length > 1)
          this.stZip = aZip[1];
      }
    }
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

  public int save()
  {
    this.nmCompanyId = 0;
    int nmPhoneId = this.ebEnt.ebNorm.getPhoneId(this.stPhone, this.stCountry);
    int nmEmailId = this.ebEnt.ebNorm.getEmailId(this.stEmail);
    int nmWebId = this.ebEnt.ebNorm.getWebId(this.stWeb);

    int nmAddressId = this.ebEnt.ebNorm.getAddressId(this.stAddress, this.stAddress2, this.stZip, this.stCity, this.stState, this.stCountry);
    if ((nmPhoneId > 0) || (nmEmailId > 0) || (nmWebId > 0) || (nmAddressId > 0))
    {
      this.nmCompanyId = this.ebEnt.ebNorm.getCompanyId(this.stCompany, nmAddressId, nmPhoneId, nmEmailId, nmWebId);
    }
    return this.nmCompanyId;
  }
}