package com.ederbase.model;

import javax.servlet.http.HttpServletRequest;

public class EbStatic
{
  public static final int ciMYSQL = 0;
  public static final int ciMSSQL = 1;
  public static final int ciODBC = 2;
  public static final String csCONFIG = "/etc/ederbase/ederbase.conf";
  public static final int ciTAB = 2000;
  public static final int ciADMIN = 2001;

  public static String stripPhone(String stPhone)
  {
    String stReturn = "";
    for (int iP = 0; iP < stPhone.length(); iP++)
    {
      char c = stPhone.charAt(iP);
      if ((c >= '0') && (c <= '9'))
        stReturn = stReturn + c;
    }
    if ((stReturn.length() > 0) && (stReturn.substring(0, 1).equals("1")))
      stReturn = stReturn.substring(1);
    return stReturn;
  }

  public static String makePhone(String stPhone)
  {
    String stReturn = "";
    stPhone = stripPhone(stPhone);
    stReturn = "(" + stPhone.substring(0, 3) + ")" + stPhone.substring(3, 6) + "-" + stPhone.substring(6);
    return stReturn;
  }

  public static boolean isEmail(String stEmail)
  {
    boolean bReturn = false;

    if ((stEmail != null) && (stEmail.length() > 4))
    {
      int iAt = stEmail.indexOf('@');
      int iAt2 = stEmail.lastIndexOf('@');
      int iLastDot = stEmail.lastIndexOf('.');
      if ((iAt > 0) && (iLastDot > 0) && (iAt < iLastDot) && (iAt == iAt2))
        bReturn = true;
    }
    return bReturn;
  }

  public static boolean isMainWeb(String stWeb)
  {
    boolean bReturn = false;

    if ((stWeb != null) && (stWeb.length() > 10) && (stWeb.substring(0, 7).equals("http://")))
    {
      int iParam = stWeb.indexOf('?');
      if (iParam <= 0)
      {
        bReturn = true;
      }
    }
    return bReturn;
  }

  public static String[] parseEmail(String stEmail)
  {
    String[] aReturn = new String[2];
    int iPos1 = -1;
    int iPos2 = -1;

    for (int i = 0; i < aReturn.length; i++) {
      aReturn[i] = "";
    }
    iPos1 = stEmail.indexOf("<");
    iPos2 = 0;
    if (iPos1 >= 0)
      iPos2 = stEmail.indexOf(">");
    if ((iPos1 >= 0) && (iPos2 > iPos1))
    {
      aReturn[0] = stEmail.substring(iPos1 + 1, iPos2).trim();
      aReturn[1] = stEmail.substring(0, iPos1).trim();
    }
    else {
      aReturn[0] = stEmail.trim();
    }
    return aReturn;
  }

  public static String myString(String stValue)
  {
    if (stValue == null)
      stValue = "";
    stValue = stValue.trim();
    if (stValue.equals(""))
      stValue = "&nbsp;";
    return stValue;
  }

  public static int myInteger(HttpServletRequest request, String stLabel)
  {
    int iReturn = 0;
    try
    {
      String[] aV = request.getParameterValues(stLabel);
      for (int i = 0; i < aV.length; i++)
      {
        iReturn |= Integer.parseInt(aV[i]);
      }
    }
    catch (Exception e)
    {
    }
    return iReturn;
  }

  public static String myString(HttpServletRequest request, String stLabel)
  {
    String stReturn = "";
    try
    {
      stReturn = request.getParameter(stLabel);
      if (stReturn == null)
        stReturn = "";
    }
    catch (Exception e)
    {
    }
    return stReturn;
  }
}