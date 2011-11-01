package com.eps.model;

import com.ederbase.model.EbEnterprise;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author Robert Eder
 */
public class EpsEditField
{

  private EbEnterprise ebEnt = null;
  public int giNrValidation = 0;
  public String stValidation = "";
  public String stValidationMultiSel = "";
  private String stError = "";
  private EpsClient epsClient = null;

  public EpsEditField()
  {
    //this.epsUd = epsUd;
  }

  public void setEbEnt(EbEnterprise ebEnt, EpsClient epsClient)
  {
    this.ebEnt = ebEnt;
    this.epsClient = epsClient;
  }

  public void addValidation(int nmFieldId)
  {
    try
    {
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("select * from teb_fields f where f.nmForeignId=" + nmFieldId);
      rs.absolute(1);
      if (giNrValidation > 0)
        this.stValidation += ",";
      this.giNrValidation++;
      this.stValidation += "\nnew Array(" + nmFieldId + ",\"" + rs.getString("stDbFieldName") + "\",\"" + rs.getString("stLabel") + "\"," + rs.getString("nmDatatype") + "," + rs.getString("nmFlags") + "," + rs.getString("nmMinBytes") + "," + rs.getString("nmMaxBytes") + ",\"" + rs.getString("stValidation") + "\",\"\" )";
      rs.close();
    } catch (Exception e)
    {
      this.stError += "<br>ERROR addValidation: " + e;
    }
  }

  public String editField(ResultSet rsMyDiv, ResultSet rsTable, ResultSet rsFields, ResultSet rsD, String stValue, int iEnable, String stLabel)
  {
    String stEdit = "";
    String stDisabled = "";
    /* gaValidation[i][]:
     * [0] = FID
     * [1] = stDbFieldName
     * [2] = stFieldLabel
     * [3] = nmDataType
     * [4] = nmFlags
     * [5] = nmMinBytes
     * [6] = nmMaxBytes
     * [7] = validation method
     * [8] = Jaw
     */
    int iF = 0;
    try
    {
      int iDt = rsFields.getInt("nmDataType");
      iF = rsFields.getInt("nmForeignId");
      int iFlags = rsFields.getInt("nmFlags");
      if (rsFields.getString("stValidation").contains("="))
      { // e=0x20   MEANS: edit is only allowed for PPM 0x20/32 user type !!!
        String[] aV = rsFields.getString("stValidation").split("=");
        if (aV[0].equals("e"))
        {
          int iMask = 0;
          if (aV[1].length() > 2 && aV[1].toLowerCase().startsWith("0x"))
            iMask = Integer.valueOf(aV[1].substring(2), 16).intValue();
          else
            iMask = Integer.parseInt(aV[1]);
          if ((iMask & this.ebEnt.ebUd.getLoginPersonFlags()) == 0)
            iFlags &= ~1; // Clear the EDIT bit if user not allowed.
        }
      }
      int nmMaxBytes = rsFields.getInt("nmMaxBytes");
      int nmRows = 5;
      String stValueCurrent = this.ebEnt.ebUd.getFormValue(-1, rsFields);

      // Field Specific OVERRIDES.
      switch (iF)
      {
        case 352: // Criteria Name
          if ((rsTable.getInt("nmTableFlags") & 0x80) != 0)
          { //Criteria, check nmFlags=1 NO DEL...
            if (rsD != null && (rsD.getInt("nmFlags") & 1) != 0)
              iFlags = 0; // Cannot change DEFAULT CRITERIA
            if (stValue == null && rsD != null)
              stValue = rsD.getString(rsFields.getString("stDbFieldName"));
          }
          break;
        default:
          if (stValue == null && rsD != null)
            stValue = rsD.getString(rsFields.getString("stDbFieldName"));
          break;
      }

      String stDisabled2 = " ";

      // Special HANDLERS
      String[] astHandler = rsFields.getString("stHandler").split("\\|");
      if (rsFields.getString("stHandler").contains("openurl"))
      {
        //openurl|./common/help/index.html
        stValue = stValueCurrent = this.ebEnt.ebUd.UrlReader(astHandler[1]);
      }
      if (stValue == null || stValue.length() <= 0)
      {
        stValue = "";
      }
      if (stValueCurrent == null || stValueCurrent.length() <= 0)
      {
        stValueCurrent = stValue;
      }
      String stDefaultValue = rsFields.getString("stDefaultValue");
      if (stValue.length() <= 0 && stDefaultValue != null && stDefaultValue.length() > 0)
        stValue = stDefaultValue;

      if (nmMaxBytes <= 0)
      {
        nmMaxBytes = 32;
      }
      stValue = stValue.replace("&", "&amp;");
      stValueCurrent = stValueCurrent.replace("&", "&amp;");

      if (astHandler.length > 1)
      {
        if (astHandler[1].equals("onchangesubmit"))
        {
          stDisabled += " onChange=\"document.form" + rsTable.getInt("nmTableId") + ".submit();\" ";
          if (astHandler.length > 2 && astHandler[2].equals("setcookie"))
          {
            if (stValue != null && stValue.length() > 0)
            {
              this.ebEnt.ebUd.setCookie(rsFields.getString("stDbFieldName"), stValue);
            } else
            {
              stValue = this.ebEnt.ebUd.getCookieValue(rsFields.getString("stDbFieldName"));
            }
          }
        } else if (astHandler[1].equals("multichoice"))
        {
          //|multichoice|5|event=341
          //|multichoice|10||addremove|Primary
          iDt = 9;
          iFlags |= 0x8000;
          iFlags &= ~0x20; // not checkbox;
          try
          {
            nmRows = Integer.parseInt(astHandler[2]);
          } catch (Exception e)
          {
            nmRows = 3;
          }
        } else if (astHandler[1].equals("checkbox"))
        {
          iDt = 9;
          iFlags |= 0x20; // checkbox;
        }
      }
      String stProcess = "";
      int nmCols = nmMaxBytes + 1;
      //if ( (iFlags & 2) != 0 || rsFields.getString("stValidationFlags").length() > 0 )
      {
        this.giNrValidation++;
        if (this.giNrValidation > 1)
        {
          this.stValidation += ",";
        }
        this.stValidation += "\n new Array(" + iF + ",\"" + rsFields.getString("stDbFieldName") + "\","
          + "\"" + stLabel + "\"," + iDt + "," + iFlags + ","
          + rsFields.getInt("nmMinBytes") + "," + nmMaxBytes + ","
          + "\"" + rsFields.getString("stValidation") + "\","
          + "\"" + rsFields.getString("stValidParam") + "\" )";
      }
      stEdit += "\n"; // nicer for reading HTML code

      String stExtraFieldName = "";
      if (rsFields.getString("stHandler").contains("selectuser"))
      {
        stExtraFieldName = "_value";
        stDisabled2 = stDisabled = " DISABLED ";
      }
      String stChecked = "";
      switch (iDt)
      {
        case 41: // LaborCateogries
          stEdit += makeLaborCategories(rsFields, stValue, 0);
          break;
        case 42: // Dependencies
          stEdit += makeDependencies(rsFields, 0, null, null);
          break;
        case 45: // Successors
          stEdit += makeSuccessors(rsFields, 0, null, null);
          break;
        case 43: // Inventory
          stEdit += makeInventory(rsFields, stValue, 0);
          break;
        case 44: // Other Resources
          stEdit += makeOtherResources(rsFields, stValue, 0);
          break;
        case 47: // Indicators
          stEdit += makeIndicators(rsFields, stValue, 1);
          break;
        case 49:
          stEdit += makeCostEffectiveness(rsFields, stValue, rsD);
          break;
        case 50:
          stEdit += makeProductivity(rsFields, stValue, rsD);
          break;
        case 51:
          stEdit += makeEstimatedHours(rsFields, stValue, rsD);
          break;
        case 52:
          stEdit += makeActualHours(rsFields, stValue, rsD);
          break;
        case 53:
          stEdit += makeDivision(rsFields, stValue, rsD);
          break;

        case 40: // Special Days
          stEdit += makePopup("select c.RecId, c.stType as stValue1, c.stEvent as stValue2, "
            + "DATE_FORMAT(c.dtDay, '%m/%d/%Y') as stValue3, ch.stChoiceValue as stValue4 "
            + "from Calendar c left join teb_choices ch on ch.nmFieldId=" + iF + " and ch.UniqIdChoice=c.nmFlags "
            + "where dtDay >= now() and  c.nmDivision=1 and c.nmUser =" + this.ebEnt.ebUd.request.getParameter("pk") + " "
            + " order by dtDay limit 100", 4, rsFields,
            "~Type^55px~Comment/Request^110px~Date^80px~Status^60px");
          break;

        case 39:
          //Tilde List (Tucker~Lakers~Blue)
          String[] aU = this.epsClient.epsUd.rsMyDiv.getString("UserQuestions").replace("~", "\n").split("\\\n", -1);
          stValue = stValue.replace(",", "~");
          String[] aA = stValue.replace("~", "\n").split("\\\n", -1);
          if (aU != null && aU.length > 0)
          {
            stEdit += "<table>";
            for (int i = 0; i < aU.length; i++)
            {
              stEdit += "<tr>";
              stEdit += "<td align=left>" + aU[i] + "</td>";
              if (aA != null && aA.length > i)
                stValue = aA[i].trim();
              else
                stValue = "";
              stEdit += "<td align=left><input type=text name=f" + iF + " id=f" + iF + " value=\"" + stValue + "\"></td>";
              stEdit += "</tr>";
            }
            stEdit += "</table>";

          }
          break;

        case 37: // radio button
          if ((iFlags & 1) == 0 || iEnable <= 0) // Editable
          {
            stDisabled2 = " DISABLED ";
          }
          if (rsFields.getString("stChoiceValues").length() > 0)
          {
            String[] aV = rsFields.getString("stChoiceValues").split("\n");
            for (int i = 0; i < aV.length; i++)
            {
              String[] aV2 = aV[i].split("=");
              if (stValue.equals(aV2[0]))
              {
                stChecked = " checked ";
              } else
              {
                stChecked = " ";
              }
              stEdit += "<input type=radio " + stDisabled2 + " " + stChecked + " name=f" + iF + " id=f" + iF + " value='" + aV2[0] + "'>&nbsp;" + aV2[1] + "&nbsp;&nbsp;&nbsp;";
            }
          } else
          {
            if (stValue.equals(stLabel))
            {
              stChecked = " checked ";
            } else
            {
              stChecked = " ";
            }
            if (astHandler.length > 2)
            {
              stEdit += "<input type=radio " + stDisabled2 + " " + stChecked + " name=f" + astHandler[2] + " id=f" + astHandler[2] + " value='" + stLabel + "'>&nbsp;" + stLabel + "&nbsp;&nbsp;&nbsp;";
            }
          }
          break;
        case 38: // menu command
        case 36: // button
          stEdit += "<input type=submit " + stDisabled + " name=f" + iF + " id=f" + iF + " value='" + stLabel;
          if (rsFields.getString("stHandler").equals("#OS"))
          {
            //stEdit += "' onclick=\"window.close();\"";
            stEdit += "' onClick=\"closeW();\"";
            //stEdit += "' onClick=\"alert('hey u');\"";
          } else
          {
            stEdit += "' onClick=\"return setSubmitId(" + rsFields.getString("nmForeignId") + ");\"";
          }
          if (iEnable <= 0)
          {
            stEdit += " DISABLED ";
          }
          stEdit += ">";
          break;

        case 27: // No data label only (27)
          stEdit += "&nbsp;";
          break;

        case 9: // int | Choice (9)
        //if ((iFlags & 1) != 0) // Editable
        {
          if ((iFlags & (0x40 | 0x200)) != 0) // Process
          {
            stProcess = " onChange=myInput(" + iF + ") ";
          } else
          {
            stProcess = " ";
          }
          if (iEnable <= 0 || (iFlags & 1) == 0)
          {
            stProcess += " DISABLED ";
          }

          //arsChoices[iF].last();
          int iMax = 0;
          ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("select * from teb_choices where nmFieldId=" + iF + " order by nmOrder ");
          if (rs != null)
          {
            rs.last();
            iMax = rs.getRow();
          }
          if ((iFlags & 0x20) == 0) // Dropdown vs. checkbox
          {
            String stMultiChoice = " ";
            if ((iFlags & 0x8000) != 0) // multi choice
            {
              stMultiChoice = "MULTIPLE SIZE=" + nmRows;
            }

            stEdit += "\n<select " + stMultiChoice + " name=f" + iF + " id=f" + iF + " " + stProcess + " " + stDisabled + ">";

            if (iMax > 0)
            {
              for (int iC = 1; iC <= iMax; iC++)
              {
                rs.absolute(iC);
                stChecked = " ";
                if ((stValueCurrent == null || stValueCurrent.length() <= 0) && (iC == 1))
                {
                  if (!rsFields.getString("stHandler").contains("nodefault"))
                  {
                    stChecked = " SELECTED ";
                  }
                } else if (stValueCurrent != null && stValueCurrent.equals(rs.getString("UniqIdChoice")))
                {
                  stChecked = " SELECTED ";
                }
                stEdit += "\n<option value=\"" + rs.getString("UniqIdChoice") + "\" " + stChecked + ">" + rs.getString("stChoiceValue") + "</option>";
              }
            } else
            {
              String[] aV = stValue.split("~", -1);
              int iN = 0;
              for (iN = 0; iN < aV.length; iN++)
              {
                stEdit += "<option value=\"" + aV[iN] + "\">" + aV[iN] + "</option>";
              }
            }
            stEdit += "</select>";

          } else
          {
            if (iMax > 0)
            {
              for (int iC = 1; iC <= iMax; iC++)
              {
                rs.absolute(iC);
                stChecked = " ";
                if (stValueCurrent != null && this.ebEnt.ebUd.isSelected(rsFields, rs, stValueCurrent))
                {
                  stChecked = " CHECKED ";
                } else if ((stValueCurrent == null || stValueCurrent.length() <= 0) && iC == 1)
                {
                  if (!rsFields.getString("stHandler").contains("nodefault"))
                  {
                    stChecked = " CHECKED ";
                  }
                }
                stEdit += "&nbsp;&nbsp;<input type=checkbox " + stChecked + " name=f" + iF + " id=f" + iF + " value=\"" + rs.getString("UniqIdChoice") + "\" " + stProcess + ">&nbsp;" + rs.getString("stChoiceValue");
              }
            }
          }
        }
        break;

        case 4: // blob | Long Text (4)
        case 7: // blob | Binary Blob (small) ()
        case 32: // blob | Long Text / Append only (32)
          nmRows = rsFields.getInt("nmRows");
          nmCols = rsFields.getInt("nmCols");
          if ((iFlags & 1) != 0) // Editable
          {
            if (rsFields.getString("stHandler").contains("rows="))
            {
              String[] aRows = rsFields.getString("stHandler").split("=");
              try
              {
                nmRows = Integer.parseInt(aRows[1]);
              } catch (Exception e)
              {
              }
            }
            if (nmRows < 1)
            {
              nmRows = 1;
            }
            if (nmCols < 20)
            {
              nmCols = 80;
            }

            if (rsFields.getString("stHandler").contains("selectuser"))
            {
              stEdit += "<table><tr><td valign=top>";
              nmRows = 4;
              String stId = "";
              stExtraFieldName = "_showonly";
              stId = stValue;
              int iCount = 0;
              if (stId.length() > 0)
              {
                stValue = "";
                stId = stId.replace("~", ",");
                String[] aV = stId.split(",", -1);
                for (int i = 0; i < aV.length; i++)
                {
                  if (aV[i].length() > 0)
                  {
                    iCount++;
                    stValue += this.ebEnt.dbDyn.ExecuteSql1("select concat(FirstName,' ',LastName) from Users where nmUserId=" + aV[i]);
                    stValue += "\n";
                  }
                }
              }
              stEdit += "<textarea DISABLED name=f" + iF + stExtraFieldName + " id=f" + iF + stExtraFieldName + " rows=" + nmRows + " cols=" + nmCols + ">" + stValue.trim().replace("~", "\n") + "</textarea>"
                + "<input type=hidden name=f" + iF + " id=f" + iF + " value=\"" + stId + "\">";
              stEdit += "</td><td valign=top>";
              if ((iFlags & 1) != 0)
              {
                stEdit += "<input type=image border=0 src='./images/stickman2.png' alt='Select Users' class=imageStyle"
                  + " onClick='return getPopupValue(this.form.f" + iF + stExtraFieldName + ",this.form.f" + iF + ",1,\"" + stId + "\");'>"
                  + "<input type=image border=0 src='./images/stickman2.png' alt='Select Users' class=imageStyle"
                  + " onClick='return getPopupValue(this.form.f" + iF + stExtraFieldName + ",this.form.f" + iF + ",1,\"" + stId + "\");'>";
              }
              stEdit += "</td></tr></table>";
            } else
            {
              if ((iFlags & 0x8) != 0)
                stEdit += stLabel + ":<BR>";
              stEdit += "<textarea name=f" + iF + stExtraFieldName + " id=f" + iF + stExtraFieldName + " rows=" + nmRows + " cols=" + nmCols + ">" + stValue.trim().replace("~", "\n") + "</textarea>";
            }
          } else
          {
            stEdit += "<div id=f" + iF + ">" + stValue.replace("\n", "<BR>") + "</div>";
          }

          break;
        case 2: // int | Yes/No (2)

          if (stValue.endsWith("1"))
          {
            stChecked = " checked ";
          } else
          {
            stChecked = " ";
          }
          stEdit += "<input type=radio name=f" + iF + " id=f" + iF + " " + stChecked + " value=1> YES &nbsp;&nbsp;";
          if (stValue.endsWith("0"))
          {
            stChecked = " checked ";
          } else
          {
            stChecked = " ";
          }
          stEdit += "<input type=radio name=f" + iF + " id=f" + iF + " " + stChecked + " value=0> NO ";
          break;
        case 21: // datetime | Time (only) (21)
          if (rsFields.getString("stValidationFlags").contains("time"))
          {
            NumberFormat formatter = new DecimalFormat("00");
            String[] aV = null;
            aV = stValue.trim().split(":");
            if (aV == null || aV.length < 2)
            {
              aV = new String[2];
              aV[0] = "00";
              aV[1] = "00";
            }
            stEdit += "<select name=f" + iF + "_hr id=f" + iF + "_hr>";
            for (int i = 0; i < 24; i++)
            {
              stEdit += this.ebEnt.ebUd.addOption(formatter.format(i), formatter.format(i), aV[0]);
            }
            stEdit += "</select>:<select name=f" + iF + "_mn id=f" + iF + "_mn>";
            for (int i = 0; i < 60; i++)
            {
              stEdit += this.ebEnt.ebUd.addOption(formatter.format(i), formatter.format(i), aV[1]);
            }
          } else
          {
            stEdit += "<input type=text name=f" + iF + " id=f" + iF + " value=\"" + stValue + "\" size=" + nmCols + " maxlength=" + nmMaxBytes + " " + stDisabled2 + stProcess + ">";
          }
          break;

        case 1: // int | Integer (1)
        case 5: // decimal | Money (5)
        case 28: // decimal | Money - LAYER total for reference field (28)
        case 29: // decimal | Money - DATABASE total for reference field ()
        case 30: // decimal | Money - CALCULATION FORMULA (30)
        case 31: // decimal | NUMBER (31)
        case 35: // bigint | Big Integer ()
          stDisabled2 += " style=\"text-align:right\" ";
        case 3: // varchar | Short Text (3)
        case 6: // longblob | Binary Blob (6)
        case 8: // datetime | Date & Time (8)
        case 12: // varchar | People Involved ()
        case 13: // varchar | Upload (13)
        case 14: // varchar | Template (14)
        case 15: // varchar | EMail ()
        case 16: // varchar | Fields to complete (16)
        case 17: // varchar | Mail Merge (17)
        case 18: // varchar | Send E-Mail (18)
        case 26: // varchar | Reference List (from multi layers) ()
        case 33: // varchar | Reporting Formula Field (33)
        case 34: // decimal | LAYER total for ref field ()
        case 19: // varchar | IVR (19)
        case 20: // date | Date (only) (20)
        case 22: // varchar | DOB & AGE (calculation) (22)
        case 24: // varchar | Auto Counter (24)
        case 25: // int | Week Day ()
        default:
          nmCols = rsFields.getInt("nmCols");
          nmMaxBytes = rsFields.getInt("nmMaxBytes");
          if (nmCols <= 0)
            nmCols = nmMaxBytes;

          if (rsFields.getInt("nmDataType") == 20)
          {
            stValue = this.ebEnt.ebUd.fmtDateFromDb(stValue);
          }
          if (rsFields.getInt("nmDataType") == 8)
          {
            stValue = this.ebEnt.ebUd.fmtDateFromDb(stValue);
          }
          /*if (rsFields.getString("stValidationFlags").contains("int"))
          {
          stProcess = " onChange=\"ValidateNum(" + iF + ", '"+ rsFields.getString("stLabel")+"' );\" ";
          }*/

          if ((iFlags & 1) == 0 && iEnable > 0) // Editable
          {
            stDisabled2 += " DISABLED ";
          }
          if (nmCols < 1)
          {
            nmCols = 20;
          }

          if ((nmMaxBytes + 1) < nmCols)
          {
            nmMaxBytes = nmCols;
          }
          if (stLabel.toLowerCase().contains("passw") && iDt == 3)
          {
            iFlags |= 0x20000000;
          }
          if (iEnable <= 0)
          {
            stDisabled2 = " DISABLED ";
          }
          if (rsFields.getString("stHandler").contains("division"))
          {
            stEdit += this.epsClient.epsUd.selectDivision("f" + iF);
          } else if (rsFields.getString("stHandler").contains("epscalendar"))
          {
            stEdit += "<input type=hidden name=f" + iF + " id=f" + iF + " value=\"" + stValue + "\"><div id=zz" + iF + " name=zz" + iF + " class=table1>Calendar ... loading</div>";
          } else if (rsFields.getString("stHandler").contains("epsweekly"))
          {
            String[] aV = stValue.split("~", -1);
            if (aV == null || aV.length < 8)
            {
              stValue = "~0~0~0~0~0~0~0";
              aV = stValue.split("~", -1);
            }
            stEdit += "Mon<input style='width:18px;text-align:right;' type=text name=f" + iF + "_mon id=f" + iF + "_mon value=\"" + aV[1] + "\" maxlength=3 " + stDisabled2 + ">&nbsp;";
            stEdit += "Tue<input style='width:18px;text-align:right;' type=text name=f" + iF + "_tue id=f" + iF + "_tue value=\"" + aV[2] + "\" maxlength=3 " + stDisabled2 + ">&nbsp;";
            stEdit += "Wed<input style='width:18px;text-align:right;' type=text name=f" + iF + "_wed id=f" + iF + "_wed value=\"" + aV[3] + "\" maxlength=3 " + stDisabled2 + ">&nbsp;";
            stEdit += "Thu<input style='width:18px;text-align:right;' type=text name=f" + iF + "_thu id=f" + iF + "_thu value=\"" + aV[4] + "\" maxlength=3 " + stDisabled2 + ">&nbsp;";
            stEdit += "Fri<input style='width:18px;text-align:right;' type=text name=f" + iF + "_fri id=f" + iF + "_fri value=\"" + aV[5] + "\" maxlength=3 " + stDisabled2 + ">&nbsp;";
            stEdit += "Sat<input style='width:18px;text-align:right;' type=text name=f" + iF + "_sat id=f" + iF + "_sa value=\"" + aV[6] + "\" maxlength=3 " + stDisabled2 + ">&nbsp;";
            stEdit += "Sun<input style='width:18px;text-align:right;' type=text name=f" + iF + "_sun id=f" + iF + "_su value=\"" + aV[7] + "\" maxlength=3 " + stDisabled2 + ">&nbsp;";
          } else if ((iFlags & 0x20000000) == 0) // text vs pwd
          {
            if (iDt == 5)
            {
              stEdit += "\n" + rsMyDiv.getString("stMoneySymbol") + " ";
            } else
            {
              stEdit += "\n";
            }
            String stId = "";
            if (rsFields.getString("stHandler").contains("selectuser"))
            {
              stExtraFieldName = "_showonly";
              stId = stValue;
              if (stId.length() > 0)
                stValue = this.ebEnt.dbDyn.ExecuteSql1("select concat(FirstName,' ',LastName) as nm from Users where nmUserId in (" + stId + ")");
            }
            if ((iDt == 5 || iDt == 31) && ((iFlags & 1) == 0 || iEnable <= 0) && stValue.length() > 0)
            {
              DecimalFormat df = new DecimalFormat("#,###,###,##0.00");
              stValue = df.format(Double.parseDouble(stValue));
            } else if ((iDt == 5 || iDt == 31) && ((iFlags & 1) != 0 || iEnable > 0) && stValue.length() > 0)
            {
              DecimalFormat df = new DecimalFormat("#0.00");
              stValue = df.format(Double.parseDouble(stValue));
            }
            /* AS -- 19Oct2011 -- Issue # 59 */
            //Check for Burden Factor
            if(iF != 877 && iF != 889)
              stEdit += "<input type=text name='f" + iF + stExtraFieldName + "' id='f" + iF + stExtraFieldName + "' value=\"" + stValue + "\" size=" + nmCols + " maxlength=" + nmMaxBytes + " " + stDisabled2 + stProcess + ">";
            else if(iF == 877)
            {
              	
              //stEdit += "<input type=hidden name='f" + iF + stExtraFieldName + "' id='f" + iF + stExtraFieldName + "' value=\"" + stValue + "\" size=" + nmCols + " maxlength=" + nmMaxBytes + " " + stDisabled2 + stProcess + ">";
              stEdit += burdenFactorHTML(iF, stExtraFieldName, stValue, nmCols, nmMaxBytes, stDisabled2, stProcess);
            }
            else if(iF == 889)
            {
              stEdit += workDaysHTML(iF, stExtraFieldName, stValue, nmCols, nmMaxBytes, stDisabled2, stProcess);	
            }
            if ((iDt == 20 || iDt == 8) && (iFlags & 1) != 0) // DATE ONLY or DATETIME - only if editable
            {
              stEdit += "\n<script language='JavaScript'>"
                + "\nnew tcal ({"
                + "\n	'formname': 'form" + rsFields.getInt("nmTabId") + "',"
                + "\n 'controlname': 'f" + iF + "'});\n</script>";
            }
            if (rsFields.getString("stHandler").contains("selectuser"))
            {
              stEdit += "<input type=hidden name='f" + iF + "' id='f" + iF + "' value=\"" + stId + "\">";
              if ((iFlags & 1) != 0)
              {
                stEdit += " <input type=image border=0 src='./images/stickman2.png' alt='Select User' class=imageStyle"
                  + " onClick='return getPopupValue(this.form.f" + iF + stExtraFieldName + ",this.form.f" + iF + ",0,0);'\">";
              }
            }
            if (iDt == 5)
            {
              stEdit += "\n<font class=small>" + rsMyDiv.getString("stCurrency") + " ";
            }
          } else
          {
            stEdit += "<input type=password name=f" + iF + " id=f" + iF + " value=\"" + stValue + "\" size=" + nmCols + " maxlength=" + nmMaxBytes + " " + stDisabled2 + ">";
          }
          break;
      }
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR editField: iF " + iF + " " + e;
    }
    return stEdit;
  }

  
  /* AS -- 19Oct2011 -- Issue # 59 */
  public String burdenFactorHTML(int iF,  String stExtraFieldName, String stValue, int nmCols, int nmMaxBytes, String stDisabled2, String stProcess)
  {
	  String sthtml = "<select id='bf"+ iF +"' size='1' onchange='jsfunc1(this)'>"+
			   "<option value='1' selected='selected'>1</option>"+
			   "<option value='2'>2</option>"+
			  "<option value='3'>3</option>"+
		      "<option value='4'>4</option>"+
		      "<option value='5'>5</option>"+
		      "<option value='6'>6</option>"+
		      "<option value='7'>7</option>"+
		      "<option value='8'>8</option>"+
		      "<option value='9'>9</option>"+
		      "<option value='10'>10</option>"+
		      "</select>";
	   sthtml += "&nbsp;&nbsp; + &nbsp;&nbsp;";
	   sthtml += "<select id='bfd"+ iF +"' size='1' onchange='jsfunc1(this)'>"+
			   "<option value='0.0' selected='selected'>0.0</option>"+
			   "<option value='0.1' >0.1</option>"+
			  "<option value='0.2'>0.2</option>"+
			  "<option value='0.3'>0.3</option>"+
		      "<option value='0.4'>0.4</option>"+
		      "<option value='0.5'>0.5</option>"+
		      "<option value='0.6'>0.6</option>"+
		      "<option value='0.7'>0.7</option>"+
		      "<option value='0.8'>0.8</option>"+
		      "<option value='0.9'>0.9</option>"+
		      
		      "</select> &nbsp;&nbsp = &nbsp;&nbsp;";
	   sthtml += "<input type=text name='f" + iF + stExtraFieldName + "' id='f" + iF + stExtraFieldName + "' value=\"" + stValue + "\" size=" + nmCols + " maxlength=" + nmMaxBytes + " " + stDisabled2 + stProcess + ">";
	   sthtml += "<script type='text/javascript'>" +
	             "function jsfunc1(elem){" +
			     "document.getElementById('f"+ iF + stExtraFieldName +"').value = parseFloat(document.getElementById('bf"+ iF +"').value) + parseFloat(document.getElementById('bfd"+ iF +"').value); " +
			     "}"+
			     
	             "</script>";
	 return sthtml;
  }
  
  public String workDaysHTML(int iF,  String stExtraFieldName, String stValue, int nmCols, int nmMaxBytes, String stDisabled2, String stProcess)
  {
	  String sthtml = "";
	  sthtml += "<input type=checkbox value='Sun' onclick='updatedays(this);'/> Sunday &nbsp;&nbsp;";
	  sthtml += "<input type=checkbox value='Mon' onclick='updatedays(this);'/> Monday &nbsp;&nbsp;";
	  sthtml += "<input type=checkbox value='Tue' onclick='updatedays(this);'/> Tuesday &nbsp;&nbsp;";
	  sthtml += "<input type=checkbox value='Wed' onclick='updatedays(this);'/> Wednesday &nbsp;&nbsp;";
	  sthtml += "<input type=checkbox value='Thu' onclick='updatedays(this);'/> Thursday &nbsp;&nbsp;";
	  sthtml += "<input type=checkbox value='Fri' onclick='updatedays(this);'/> Friday &nbsp;&nbsp;";
	  sthtml += "<input type=checkbox value='Sat' onclick='updatedays(this);'/> Saturday &nbsp;&nbsp;";
	  
	  //sthtml += "<input type=text name='f" + iF + stExtraFieldName + "' id='f" + iF + stExtraFieldName + "' value=\"" + stValue + "\" size=" + nmCols + " maxlength=" + nmMaxBytes + " " + stDisabled2 + stProcess + ">";
	  sthtml += "<input type=text name='f" + iF + stExtraFieldName + "' id='f" + iF + stExtraFieldName + "' size=" + nmCols + " maxlength=" + nmMaxBytes + " " + stDisabled2 + stProcess + ">";
	  sthtml += "<script type='text/javascript'>" +
	             "function updatedays(elem){" +
			     "if(elem.checked){" +
	             "if(document.getElementById('f"+ iF + stExtraFieldName +"').value.indexOf(elem.value) != -1)" +
			     " return; " +
			     "document.getElementById('f"+ iF + stExtraFieldName +"').value += elem.value+',';"+
			     "}else{"+
			     "var str = document.getElementById('f"+ iF + stExtraFieldName +"').value;" +
			     "document.getElementById('f"+ iF + stExtraFieldName +"').value = str.replace(elem.value+',' , '');" +
			     "}}"+
			     
	             "</script>";
	  return sthtml;
  }
  
  public String getError()
  {
    return this.stError;
  }

  public String makePopup(String stSql, int nmFields, ResultSet rsFields, String stHeader)
  {
    String stReturn = "";
    String stValue = stHeader;
    try
    {
      stReturn = "<div name='div" + rsFields.getString("nmForeignId") + "' id='div" + rsFields.getString("nmForeignId") + "'>"
        + rsFields.getString("stLabel") + "</div>";
      ResultSet rsD = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rsD.last();
      int iMaxD = rsD.getRow();
      for (int iD = 1; iD <= iMaxD; iD++)
      {
        rsD.absolute(iD);
        if (stValue.length() > 0)
          stValue += "\n|";
        stValue += rsD.getString("RecId");
        for (int i = 1; i <= nmFields; i++)
          stValue += "~" + rsD.getString("stValue" + i);
      }
      stReturn += "<input type=hidden name=f" + rsFields.getString("nmForeignId") + "_del id=f" + rsFields.getString("nmForeignId") + "_del value=''>"
        + "<input type=hidden name=f" + rsFields.getString("nmForeignId") + " id=f" + rsFields.getString("nmForeignId") + " value=\"" + stValue + "\">";
    } catch (Exception e)
    {
      stError += "<br>ERROR makePopup " + e;
    }

    return stReturn;
  }

  public void rebuildChoices()
  {
    try
    {
      this.ebEnt.dbDyn.ExecuteUpdate("truncate table teb_choices");
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("select * from teb_epsfields where stChoiceValues != ''");
      rs.last();
      int iMax = rs.getRow();
      for (int iR = 1; iR <= iMax; iR++)
      {
        rs.absolute(iR);
        String[] aChoices = rs.getString("stChoiceValues").trim().split("\n");
        int nmOrder = 0;
        for (int iC = 0; iC < aChoices.length; iC++)
        {
          nmOrder += 10;
          String[] aV = aChoices[iC].split("=");
          this.ebEnt.dbDyn.ExecuteUpdate("insert into teb_choices (UniqIdChoice,stChoiceValue,nmOrder,nmFieldId) "
            + "values(\"" + aV[0] + "\",\"" + aV[1] + "\"," + nmOrder + "," + rs.getString("nmForeignId") + ") ");
        }
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR rebuildChoices " + e;
    }
  }

  public String processAllocate(EpsUserData epsUd, int iMode)
  {
    String stReturn = "<h1>Allocation Processor</h1><table border=1>";
    String stReturn1 = "";
    int iMax = 0;
    try
    {
      EpsXlsProject epsProject = new EpsXlsProject();
      epsProject.setEpsXlsProject(this.ebEnt, epsUd);

      // Must analyze ALL projects
      if (iMode == 1) // from runEOB only
      {
        ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql("select * from Projects where ProjectStatus != 1 ");
        rs1.last();
        iMax = rs1.getRow();

        for (int iR = 1; iR <= iMax; iR++)
        {
          rs1.absolute(iR);
          epsProject.stPk = rs1.getString("RecId");
          stReturn1 += epsProject.xlsAnalyze();
        }
        rs1.close();
      }
      this.ebEnt.dbDyn.ExecuteUpdate("delete from teb_allocateprj where nmActual=0 or nmActualApproved=0");
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("select * from Projects where ProjectStatus=1 order by TotalRankingScore desc");
      rs.last();
      iMax = rs.getRow();

      for (int iR = 1; iR <= iMax; iR++)
      {
        rs.absolute(iR);
        epsProject.stPk = rs.getString("RecId");
        stReturn1 += epsProject.xlsAnalyze();
        stReturn1 += doAllocate(rs);
      }
      this.stError += epsProject.getError();
    } catch (Exception e)
    {
      this.stError += "<br>ERROR processAllocate " + e;
    }
    stReturn += stReturn1 + "</table>";
    return stReturn;
  }

  public String doAllocate(ResultSet rsP)
  {
    String stReturn = "<table border=1>";
    String stSql = "";
    String stUsers = "";

    String[] aRecords = null;
    String[] aFields = null;
    String stValue = "";
    double dMax = 0;
    int iMaxEmp = 0;
    int iRecMax = 0;
    double dMaxEffort = 0;
    double dEffortUser = 0;
    String[] aUsers = null;
    String[] aStart = null;
    String[] aEnd = null;
    String stStart = "";
    String stEnd = "";

    try
    {
      stReturn = "<tr><td>" + rsP.getString("ProjectName") + "</td>";
      int iAllocationPeriod = this.epsClient.epsUd.rsMyDiv.getInt("AllocationPeriod");
      ResultSet rsTask = this.ebEnt.dbDyn.ExecuteSql("select * from Schedule where nmProjectId=" + rsP.getString("RecId")
        + " and nmBaseline=" + rsP.getString("CurrentBaseline") + " and ( SchFlags & 0x1010 ) = 0x10 "
        + " and SchStartDate <= DATE_ADD(curdate(),INTERVAL " + iAllocationPeriod + " DAY) order by SchStartDate,SchId");
      rsTask.last();
      int iMaxTask = rsTask.getRow();

      for (int iT = 1; iT <= iMaxTask; iT++)
      {
        rsTask.absolute(iT);
        stValue = rsTask.getString("SchLaborCategories");
        if (stValue.length() > 0)
        {
          aRecords = stValue.split("\\|", -1);
          iRecMax = aRecords.length;
        } else
        {
          iRecMax = 0;
        }
        for (int iR = 0; iR < iRecMax; iR++)
        {
          aFields = aRecords[iR].split("~", -1); // LcId, MaxEmployees, Effort, 58~1~32.0~~~~
          try
          {
            iMaxEmp = Integer.parseInt(aFields[1]);
            dMaxEffort = Double.parseDouble(aFields[2]);
            if ((dMaxEffort / iMaxEmp) > dMax)
              dMax = (dMaxEffort / iMaxEmp);
            dEffortUser = (dMaxEffort / iMaxEmp);
            // Now let's find the schmack(2), who can do the job the best.
            this.ebEnt.dbDyn.ExecuteUpdate("update Users set nmTempOrder=100 where nmUserId in "
              + "( SELECT nmRefId FROM teb_reflaborcategory rlc where nmRefType=42 and nmLaborCategoryId=" + aFields[0] + ")");

            if (aFields[4].length() > 0)
            {
              // Most Desireable
              this.ebEnt.dbDyn.ExecuteUpdate("update Users set nmTempOrder=200 where nmUserId in "
                + "( " + aFields[4] + ")");
            }
            if (aFields[5].length() > 0)
            {
              // Least
              this.ebEnt.dbDyn.ExecuteUpdate("update Users set nmTempOrder=0 where nmUserId in "
                + "( " + aFields[5] + ")");
            }
            if (aFields[3].length() > 0) // Must Assign
            {
              stUsers = aFields[3];
            } else
            {
              // Normal
              // SELECT * FROM teb_reflaborcategory rlc where nmRefType=42 and nmLaborCategoryId=58;
              String stNot = "";
              if (aFields[6].length() > 0) // Must NOT Assign
                stNot = " and nmUserId not in (" + aFields[6] + ") ";
              ResultSet rsU = this.ebEnt.dbDyn.ExecuteSql("select * from Users where nmUserId in "
                + "( SELECT nmRefId FROM teb_reflaborcategory rlc where nmRefType=42"
                + " and nmLaborCategoryId=" + aFields[0] + ") " + stNot
                + " order by nmTempOrder, ProductivityFactor");
              rsU.last();
              int iUMax = rsU.getRow();
              stUsers = "";
              for (int iU = 1; iU <= iUMax; iU++)
              {
                rsU.absolute(iU);
                if (iU > 1)
                  stUsers += ",";
                stUsers += rsU.getString("nmUserId");
              }
            }
            stReturn += "<br>Users " + stUsers;
            if (stUsers.length() > 0)
            {
              aUsers = stUsers.split(",");
              aStart = new String[aUsers.length];
              aEnd = new String[aUsers.length];
              for (int iU = 0; iU < aUsers.length; iU++)
              {
                aStart[iU] = "";
                aEnd[iU] = "";
              }
              if (aUsers.length < iMaxEmp)
                this.stError += "<BR>ERROR in allocation: found " + aUsers.length + " resources, but " + iMaxEmp + " are needed ";
              for (int iU = 0; iU < aUsers.length && iU < iMaxEmp; iU++)
              {
                // Now allocate this users.
                double dUser = dEffortUser;
                ResultSet rsAvail = this.ebEnt.dbDyn.ExecuteSql("SELECT * FROM teb_allocate ta"
                  + " left join Calendar c on c.dtDay=ta.dtAllocate "
                  + " and (c.nmDivision=" + rsP.getString("nmDivision") + " or c.nmUser=" + aUsers[iU] + ")"
                  + " where ta.nmUserId=" + aUsers[iU] + " and ta.dtAllocate >= curdate() order by dtAllocate limit " + (iAllocationPeriod * 4));
                rsAvail.last();
                int iMaxAvail = rsAvail.getRow();
                for (int iA = 1; dUser > 0 && iA <= iMaxAvail; iA++)
                {
                  rsAvail.absolute(iA);
                  String stDay = rsAvail.getString("dtDay");

                  double nmAvail = rsAvail.getDouble("nmAvailable");
                  double nmAllocate = 0;
                  if (nmAvail > 0 && stDay != null && stDay.length() > 0)
                  { // Holiday or Vaca ... clear out
                    this.ebEnt.dbDyn.ExecuteUpdate("update teb_allocate set nmAvailable=0"
                      + " where dtAllocate='" + rsAvail.getString("dtAllocate") + "' and nmUserId=" + aUsers[iU]);
                    nmAvail = 0;
                  }
                  if (nmAvail > 0)
                  {
                    double dAllocated = this.ebEnt.dbDyn.ExecuteSql1n("select sum(nmAllocated) from teb_allocateprj"
                      + " where dtDatePrj='" + rsAvail.getString("dtAllocate") + "' and nmUserId=" + aUsers[iU]
                      + " and nmPrjId=" + rsP.getString("RecId") + " and nmTaskId=" + rsTask.getString("RecId") + " ");
                    dUser -= dAllocated; // Already done. for this day and prj and task

                    // Now check all allocation for that day and user
                    dAllocated = this.ebEnt.dbDyn.ExecuteSql1n("select sum(nmAllocated) from teb_allocateprj"
                      + " where dtDatePrj='" + rsAvail.getString("dtAllocate") + "' and nmUserId=" + aUsers[iU]);
                    // If more hours are available, add more
                    if ((nmAvail - dAllocated) > 0)
                    {
                      if (dUser > nmAvail)
                        nmAllocate = nmAvail;
                      else
                        nmAllocate = dUser; // Rest of it.
                      dUser -= nmAllocate;

                      if (aStart[iU].length() <= 0)
                        aStart[iU] = rsAvail.getString("dtAllocate");
                      aEnd[iU] = rsAvail.getString("dtAllocate");

                      // check avail. check holiday/vacation
                      this.ebEnt.dbDyn.ExecuteUpdate("replace into teb_allocatePrj "
                        + "(dtDatePrj,nmUserId,nmLc,nmPrjId,nmTaskId,nmAllocated) values("
                        + "'" + rsAvail.getString("dtAllocate") + "'," + aUsers[iU] + "," + aFields[0] + "," + rsP.getString("RecId")
                        + "," + rsTask.getString("RecId") + "," + nmAllocate + ")");
                    }
                  }
                }
              }
              stStart = aStart[0];
              stEnd = "";
              for (int iU = 0; iU < aUsers.length; iU++)
              {
                if (aStart[iU].compareTo(stStart) < 0)
                  stStart = aStart[iU];
                if (aEnd[iU].compareTo(stEnd) > 0)
                  stEnd = aEnd[iU];
              }
              // Start doesnt matter, but END will have to push everything out.
              if (stEnd.compareTo(rsTask.getString("SchFinishDate")) > 0)
              {
                // TODO, push end dates out.
                stError += "<br>Must push end date from: " + rsTask.getString("SchFinishDate") + " to: " + stEnd;
              }
            } else
              this.stError += "<BR>ERROR in allocation: no resources found, but " + iMaxEmp + " are needed ";
          } catch (Exception e)
          {
            this.stError += "<br>ERROR doAllocate iR=" + iR + " " + e;
          }
        }
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR doAllocate " + e;
    }
    stReturn += "</table>";
    return stReturn;
  }

  public String processUsersInDivision()
  {
    String stReturn = "";
    try
    {
      this.ebEnt.dbDyn.ExecuteUpdate("update teb_division set nmUsersInDivision=0");
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("SELECT  count(*) as cnt, nmDivision from teb_refdivision where nmRefType=42 group by nmDivision");
      rs.last();
      int iMax = rs.getRow();
      for (int iL = 1; iL <= iMax; iL++)
      {
        rs.absolute(iL);
        this.ebEnt.dbDyn.ExecuteUpdate("update teb_division set nmUsersInDivision=" + rs.getString("cnt") + " where nmDivision=" + rs.getString("nmDivision"));
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR processUsersInDivision " + e;
    }
    stReturn += "</table>";
    return stReturn;
  }

  public String processAssignLaborCategory()
  {
    String stReturn = "";
    try
    {
      //this.ebEnt.dbDyn.ExecuteUpdate("update LaborCategory set NumberUsers=0");
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("select count(*) as cnt, nmLaborCategoryId from teb_reflaborcategory where nmRefType = 42  group by nmLaborCategoryId");
      rs.last();
      int iMax = rs.getRow();
      for (int iL = 1; iL <= iMax; iL++)
      {
        rs.absolute(iL);
        //this.ebEnt.dbDyn.ExecuteUpdate("update LaborCategory set NumberUsers=" + rs.getString("cnt") + " where nmLcId=" + rs.getString("nmLaborCategoryId"));
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR processAssignLaborCategory " + e;
    }
    stReturn += "</table>";
    return stReturn;
  }

  public String processUsersInLaborCategory()
  {
    String stReturn = "";
    try
    {
      this.ebEnt.dbDyn.ExecuteUpdate("update LaborCategory set NumberUsers=0,HighestHourlySalary=0,"
        + "LowestHourlySalary=0,AverageHourlySalary=0,HoursExpendedtoDate=0,HoursRemainingThisYear=0");

      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("select count(*) as cnt, rlc.nmLaborCategoryId, sum(u.HourlyRate) as sum"
        + ",max(u.HourlyRate) as max,min(u.HourlyRate) as min"
        + " from teb_reflaborcategory rlc, Users u where u.nmUserId=rlc.nmRefId and rlc.nmRefType = 42"
        + " group by rlc.nmLaborCategoryId;");
      rs.last();
      int iMax = rs.getRow();
      for (int iL = 1; iL <= iMax; iL++)
      {
        rs.absolute(iL);
        double dAvg = 0;
        if (rs.getInt("cnt") > 0)
          dAvg = rs.getDouble("sum") / rs.getInt("cnt");
        this.ebEnt.dbDyn.ExecuteUpdate("update LaborCategory set NumberUsers=" + rs.getString("cnt")
          + ",HighestHourlySalary=" + rs.getString("max")
          + ",LowestHourlySalary=" + rs.getString("min")
          + ",AverageHourlySalary=" + dAvg
          + " where nmLcId=" + rs.getString("nmLaborCategoryId"));
      }
      rs.close();
      ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql("select count(*) cnt,nmLc,sum(nmAllocated) as allocated,"
        + "sum(nmActualApproved) as approved from teb_allocateprj group by nmLc");
      rs1.last();
      iMax = rs1.getRow();
      for (int iL = 1; iL <= iMax; iL++)
      {
        rs1.absolute(iL);
        this.ebEnt.dbDyn.ExecuteUpdate("update LaborCategory set NumberUsers=" + rs1.getString("cnt")
          + ",HoursExpendedtoDate=" + rs1.getDouble("approved")
          + ",HoursRemainingThisYear=" + (rs1.getDouble("allocated") - rs1.getDouble("approved"))
          + " where nmLcId=" + rs1.getString("nmLc"));
      }
      rs1.close();
    } catch (Exception e)
    {
      this.stError += "<br>ERROR processUsersInLaborCategory " + e;
    }
    stReturn += "</table>";
    return stReturn;
  }

  public String selectUsers(ResultSet rsTable, String stResult, int iMax, int iTo, String stSql)
  {
    String stEdit = "<table border=0 cellpadding=2 bgcolor='#CCCCCC'>";
    try
    {
      String stList = this.ebEnt.ebUd.request.getParameter("list");
      if (stList != null && stList.length() > 0 && stList.subSequence(0, 1).equals("0"))
        stList = stList.substring(1);
      int iF = 7777;
      String stValue = "";
      stEdit += "<tr>";
      String[] aSel = this.ebEnt.ebUd.request.getParameterValues("f7777_selected");
      if (aSel != null && aSel.length > 0)
      {
        String stTemp = "";
        for (int i = 0; i < aSel.length; i++)
        {
          if (i > 0)
            stTemp += ",";
          stTemp += aSel[i];
        }
        stList = stTemp;
      } else
      {
        if (this.ebEnt.ebUd.request.getParameterValues("submit") != null)
          stList = "0"; // Special case, all users deleted, browser won't send me the list
      }
      String[] aV = stList.split(",");

      /* AS -- 29Sept2011 -- Issue #76*/
      stEdit += "<td align=center valign=top><b>AVAILABLE</b><br><select MULTIPLE SIZE=" + this.epsClient.epsUd.rsMyDiv.getInt("MaxRecords") + " name='f" + iF + "_list' id='f" + iF + "_list' style='width:300px' "
        + "onDblClick=\"moveOptions(document.form" + rsTable.getInt("nmTableId") + ".f" + iF + "_list, document.form" + rsTable.getInt("nmTableId") + ".f" + iF + "_selected);\">";
      
      stEdit += stResult;
      String stNext = "";
      if (iMax < this.epsClient.epsUd.rsMyDiv.getInt("MaxRecords"))
        stNext += "NOTE: please enter your search criteria and <br>click on <b>Search</b> button above";
      else
      {
        stNext += "<a name='next'></a><input type=hidden name='stFrom' value='" + iTo + "'>";
        stNext += "<input type=hidden name='stSql' value='" + stSql + "'>";
        stNext += "<input type=submit name='dosubmit' value='Next'  onClick=\"return setSubmitId(7777);\">";
      }
      stEdit += "</select><br>" + stNext;
      stEdit += "</td><td valign=middle align=center>";
      stEdit += "\n<input type=button onclick=\"moveOptions(document.form" + rsTable.getInt("nmTableId") + ".f" + iF + "_list, document.form" + rsTable.getInt("nmTableId") + ".f" + iF + "_selected);\"  name=f" + iF + "_add  id=n" + iF + "_add  value='&gt;&gt; ADD'><br>&nbsp;<br>"
        + "<input type=button onclick=\"moveOptions(document.form" + rsTable.getInt("nmTableId") + ".f" + iF + "_selected, document.form" + rsTable.getInt("nmTableId") + ".f" + iF + "_list);\"  name=f" + iF + "_remove id=n" + iF + "_remove  value='REMOVE &lt;&lt;'>";
      
      /* AS -- 29Sept2011 -- Issue #76*/
      //stEdit += "</td><td valign=top align=center><b>SELECTED USERS</b><br>";
      stEdit += "</td><td valign=top align=center><b>SELECTED</b><br>";
      stEdit += "<select MULTIPLE SIZE=" + this.epsClient.epsUd.rsMyDiv.getInt("MaxRecords") + " name='f" + iF + "_selected' id='f" + iF + "_selected' style='width:300px'>";
      
      String stNames = "";
      for (int i = 0; i < aV.length; i++)
      {
        if (aV[i].length() > 0)
        {
          stValue = this.ebEnt.dbDyn.ExecuteSql1("select concat(FirstName,' ',LastName) from Users where nmUserId=" + aV[i]);
          if (stNames.length() > 0)
            stNames += "~";
          stNames += stValue;
          stEdit += "\n<option value=\"" + aV[i] + "\" >" + stValue + "</option>";
        }
      }

      stEdit += "</select><br>";
      
      /* AS -- 29Sept2011 -- Issue #5*/
      
      //stEdit += "<input type=submit name=userslect0  value='Save selected users'"
      //  + " onClick='sendBack(\"" + stNames + "\", \"" + stList + "\" );'>";
      stEdit += "<input type=submit name=userslect0  value='Save Selected Users'"
    	        + " onClick='sendBack(\"" + stNames + "\", \"" + stList + "\" );'>";

      stEdit += "</td></tr></table>";
      if (this.stValidationMultiSel.length() > 0)
        this.stValidationMultiSel += "~";
      this.stValidationMultiSel += "f" + iF + "_selected";
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR selectUsers " + e;
    }
    return stEdit;
  }
  
  public String selectProjects(ResultSet rsTable, int reportID)
  {
    String stEdit = "<table border=0 cellpadding=2 bgcolor='#CCCCCC'>";
    try
    {
      String iF = "projects";
      String prjFilter = this.ebEnt.dbDyn.ExecuteSql1("select prjFilter from teb_customreport where RecId = " + reportID);
      String[] prjArr = prjFilter.split(",");
      String pIDs = "";
      
      stEdit += "<tr><td align=center valign=top><b>AVAILABLE PROJECTS</b><br><select MULTIPLE SIZE=" + this.epsClient.epsUd.rsMyDiv.getInt("MaxRecords") + " name='f" + iF + "_list' id='f" + iF + "_list' style='width:300px' "
        + "onDblClick=\"moveOptions(document.form" + rsTable.getInt("nmTableId") + ".f" + iF + "_list, document.form" + rsTable.getInt("nmTableId") + ".f" + iF + "_selected);\">";
      //get projects that are not added yet
      if(prjArr.length > 0){
  	  	for(int i=0; i<prjArr.length; i++){
  	  		if(!prjArr[i].equals(""))
  	  			pIDs += "RecId <> " + prjArr[i] + " and ";
  	   	}
  	   	if(!pIDs.equals("")){
  	   		pIDs = " where " + pIDs.substring(0, pIDs.length()-5);
  	   	}
  	  }
      ResultSet stResult = this.ebEnt.dbDyn.ExecuteSql("select RecId, ProjectName from projects" + pIDs);
      while(stResult.next()){
    	  stEdit += "\n<option value=\"" + stResult.getString("RecId") + "\" >" + stResult.getString("ProjectName") + "</option>";
      }
      
      stEdit += "</select>";
      stEdit += "</td><td valign=middle align=center>";
      stEdit += "\n<input type=button onclick=\"moveOptions(document.form" + rsTable.getInt("nmTableId") + ".f" + iF + "_list, document.form" + rsTable.getInt("nmTableId") + ".f" + iF + "_selected);\"  name=f" + iF + "_add  id=n" + iF + "_add  value='&gt;&gt; ADD'><br>&nbsp;<br>"
        + "<input type=button onclick=\"moveOptions(document.form" + rsTable.getInt("nmTableId") + ".f" + iF + "_selected, document.form" + rsTable.getInt("nmTableId") + ".f" + iF + "_list);\"  name=f" + iF + "_remove id=n" + iF + "_remove  value='REMOVE &lt;&lt;'>";
      stEdit += "</td><td valign=top align=center><b>SELECTED PROJECTS</b><br>";
      stEdit += "<select MULTIPLE SIZE=" + this.epsClient.epsUd.rsMyDiv.getInt("MaxRecords") + " name='f" + iF + "_selected' id='f" + iF + "_selected' style='width:300px'>";
      
      //get previously added projects
	  if(prjArr.length > 0){
		pIDs = "";
	  	for(int j=0; j<prjArr.length; j++){
	  		if(!prjArr[j].equals(""))
	  			pIDs += "RecId = " + prjArr[j] + " or ";
	   	}
	   	if(!pIDs.equals("")){
	   		pIDs = " where " + pIDs.substring(0, pIDs.length()-4);
	   		stResult = this.ebEnt.dbDyn.ExecuteSql("select RecId, ProjectName from projects" + pIDs);
		    while(stResult.next()){
		    	stEdit += "<option value=\"" + stResult.getString("RecId") + "\" >" + stResult.getString("ProjectName") + "</option>";
		    }
	   	}
	  }

      stEdit += "</select>";

      stEdit += "</td></tr></table>";
      if (this.stValidationMultiSel.length() > 0)
        this.stValidationMultiSel += "~";
      this.stValidationMultiSel += "f" + iF + "_selected";
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR selectProjects " + e;
    }
    return stEdit;
  }

  public String makeDependencies(ResultSet rsField, int iMode, String stPrjId, String stChild2)
  {
    String stReturn = "";
    String stFromProject = "";
    String stFromId = "";
    String stFromTitle = "";
    String stLag = "";
    String stType = "";
    String stFromBaseline = "";
    int iRecMax = 0;
    try
    {
      String stPrj = "";
      String stChild = "";
      String stR = "";
      if (stPrjId != null && stPrjId.length() > 0 && stChild2 != null && stChild2.length() > 0)
      {
        stPrj = stPrjId;
        stChild = "21";
        stR = stChild2;
      } else
      {
        stPrj = this.ebEnt.ebUd.request.getParameter("pk");
        stChild = this.ebEnt.ebUd.request.getParameter("child");
        stR = this.ebEnt.ebUd.request.getParameter("r");
      }
      String stBaseline = this.ebEnt.dbDyn.ExecuteSql1("select CurrentBaseline from Projects where RecId=" + stPrj);
      String stSql = "select * from teb_link where nmProjectId=" + stPrj + " and nmBaseline=" + stBaseline + " and nmLinkFlags=2 "
        + "and nmToId=" + stR + " and nmToProject=nmProjectId order by nmFromId";
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      iRecMax = rs.getRow();

      if (iRecMax <= 0 && iMode == 2)
      {
        return ""; //---------------------------->
      }
      if (iRecMax <= 0 && iMode == 0)
      {
        stReturn += "<tr><td style='font-size:150%' >" + rsField.getString("stLabel") + ":</td><td align=left>";
        stReturn += "&nbsp;<input type=image name=savedata value=9991 onClick=\"return setSubmitId(9991);\" src='./common/b_edit.png'></td></tr>";
      } else
      {
        if (iMode == 0)
        {
          stReturn += "<h4>Task ID " + stR + " " + rsField.getString("stLabel") + "</h4>";
          stReturn += "<table border=0><tr><td valign=top>";
          stReturn += "<table border=0 bgcolor='blue' cellpadding=3 cellspacing=1><tr class=d1>";
        }
        if (iMode == 2)
        {
          stReturn += "<table border=0><tr><td valign=top>";
          stReturn += "<table border=0 cellpadding=3 cellspacing=1><tr class=d1>";
        }
        if (iMode == 1)
        {
          stReturn += "<h4>Task ID " + stR + " " + rsField.getString("stLabel") + "</h4>";
          stReturn += "<table border=0 bgcolor='blue' cellpadding=3 cellspacing=1><tr class=d1>";
        }
        if (iMode == 0 || iMode == 2)
        {
          stReturn += "<td colspan=2 align=right>ID</td><td>Dependent Title</td><td>Dependency</td><td>Lag (days)</td></tr>";
          for (int iR = 1; iR < (iRecMax + 1); iR++)
          {
            rs.absolute(iR);
            stFromProject = rs.getString("nmFromProject");
            stFromId = rs.getString("nmFromId");
            stLag = rs.getString("nmPercent");
            stType = rs.getString("stComment");

            stReturn += "<tr class=d0><td>";
            if (!stPrj.equals(stFromProject))
            {
              stReturn += this.ebEnt.dbDyn.ExecuteSql1("select ProjectName from Projects where RecId=" + stFromProject) + "</td>";
              stFromBaseline = this.ebEnt.dbDyn.ExecuteSql1("select CurrentBaseline from Projects where RecId=" + stFromProject);
            } else
            {
              stReturn += "&nbsp;</td>";
              stFromBaseline = stBaseline;
            }
            stFromTitle = this.ebEnt.dbDyn.ExecuteSql1("select SchTitle from Schedule"
              + " where nmProjectId=" + stFromProject + " and nmBaseline=" + stFromBaseline + " and RecId=" + stFromId) + "</td>";

            stReturn += "<td align=right>" + stFromId + "</td>";
            stReturn += "<td>" + stFromTitle + "</td>";
            stReturn += "<td>" + ConstraintList("type_" + iR, stType, " DISABLED ") + "</td>";

            double dLag = 0;
            if (stLag != null && stLag.length() > 0)
            {
              dLag = Double.parseDouble(stLag);
            }
            if (dLag != 0)
              stReturn += "<td>" + dLag + " days</td>";
            else
              stReturn += "<td>&nbsp;</td>";
            stReturn += "</tr>";
          }
          if (iMode == 0)
            stReturn += "</table></td><td valign=top>&nbsp;<input type=image name=savedata value=9991 onClick=\"return setSubmitId(9991);\" src='./common/b_edit.png'></td></tr></table>";
          else
            stReturn += "</table></td><td valign=top>&nbsp;</td></tr></table>";
        } else if (iMode == 1)
        {
          stReturn += "<td>Action</td><td>Project</td><td>Task ID</td><td>Dependency</td><td>Lag [optional]</td></tr>";
          ResultSet rsPrj = this.ebEnt.dbDyn.ExecuteSql("select * from Projects order by ProjectName ");
          rsPrj.last();
          int iMaxPrj = rsPrj.getRow();

          for (int iR = 1; iR <= (iRecMax + 1); iR++)
          {
            stReturn += "<tr class=d0>";
            if (iR < (iRecMax + 1))
            {
              rs.absolute(iR);
              stFromProject = rs.getString("nmFromProject");
              stFromId = rs.getString("nmFromId");
              stLag = rs.getString("nmPercent");
              stType = rs.getString("stComment");
              stReturn += "<td valign=top align=center>";
              stReturn += "<input type=image name=del id=del value=" + iR + " onClick=\"return setSubmitId2(9991," + iR + ");\" src='./common/b_drop.png'></td>";
            } else
            {
              stFromProject = stPrj;
              stFromId = "";
              stLag = "";
              stType = "fs";
              stReturn += "<td>Add new:</td>";
            }

            stReturn += "<td><select name=prj_" + iR + " id=prj_" + iR + ">";

            for (int iLc = 1; iLc <= iMaxPrj; iLc++)
            {
              rsPrj.absolute(iLc);
              stReturn += this.ebEnt.ebUd.addOption2(rsPrj.getString("ProjectName"), rsPrj.getString("RecId"), stFromProject);
            }
            stReturn += "<td align=right><input type=text name=id_" + iR + " id=id_" + iR + " value=\"" + stFromId + "\" size=5 style='text-align:right'></td>";
            stReturn += "<td>" + ConstraintList("type_" + iR, stType, "") + "</td>";
            stReturn += "<td align=right><input type=text name=lag_" + iR + " id=lag_" + iR + " value=\"" + stLag + "\" size=5 style='text-align:right'> (days)</td>";
            stReturn += "</tr>";
          }

          stReturn += "</table><br>"
            + "<input type=hidden name=imax id=imax value='" + (iRecMax + 1) + "'>"
            + "<input type=hidden name=giVar id=giVar value='-1'>"
            + "<input type=submit name=savedata value='Save and Return'  onClick=\"return setSubmitId(9971);\">"
            + "<input type=submit name=savedata value='Save and Insert New'  onClick=\"return setSubmitId(9991);\">"
            + "<input type=submit name=cancel2 value='Cancel'  onClick=\"setSubmitId(8888);\">"
            + "<br>&nbsp;";
        }
      }
    } catch (Exception e)
    {
      stError += "ERROR makeDependencies: " + e;
    }
    return stReturn;
  }

  public String makeSuccessors(ResultSet rsField, int iMode, String stPrjId, String stChild2)
  {
    String stReturn = "";
    String stToProject = "";
    String stToId = "";
    String stToTitle = "";
    String stLag = "";
    String stType = "";
    String stToBaseline = "";
    int iRecMax = 0;
    try
    {
      String stPrj = "";
      String stChild = "";
      String stR = "";
      if (stPrjId != null && stPrjId.length() > 0 && stChild2 != null && stChild2.length() > 0)
      {
        stPrj = stPrjId;
        stChild = "21";
        stR = stChild2;
      } else
      {
        stPrj = this.ebEnt.ebUd.request.getParameter("pk");
        stChild = this.ebEnt.ebUd.request.getParameter("child");
        stR = this.ebEnt.ebUd.request.getParameter("r");
      }
      String stBaseline = this.ebEnt.dbDyn.ExecuteSql1("select CurrentBaseline from Projects where RecId=" + stPrj);
      String stSql = "select * from teb_link where nmProjectId=" + stPrj + " and nmBaseline=" + stBaseline + " and nmLinkFlags=2 "
        + "and nmFromId=" + stR + " and nmFromProject=nmProjectId order by nmToId";
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      iRecMax = rs.getRow();

      if (iRecMax <= 0 && iMode == 2)
      {
        return ""; //----------------------------->
      }
      if (iRecMax <= 0 && iMode == 0)
      {
        stReturn += "<tr><td>" + rsField.getString("stLabel") + ":</td><td align=left>";
        stReturn += "&nbsp;<input type=image name=savedata value=9991 onClick=\"return setSubmitId(9992);\" src='./common/b_edit.png'></td></tr>";
      } else
      {
        if (iMode == 2)
        {
          stReturn += "<table border=0><tr><td valign=top>";
          stReturn += "<table border=0  cellpadding=3 cellspacing=1><tr class=d1>";
        }
        if (iMode == 0)
        {
          stReturn += "<h4>Task ID " + stR + " " + rsField.getString("stLabel") + "</h4>";
          stReturn += "<table border=0><tr><td valign=top>";
          stReturn += "<table border=0 bgcolor='blue' cellpadding=3 cellspacing=1><tr class=d1>";
        }
        if (iMode == 1)
        {
          stReturn += "<h4>Task ID " + stR + " " + rsField.getString("stLabel") + "</h4>";
          stReturn += "<table border=0 bgcolor='blue' cellpadding=3 cellspacing=1><tr class=d1>";
        }
        if (iMode == 0 || iMode == 2)
        {
          stReturn += "<td colspan=2 align=right>ID</td><td>Dependent Title</td><td>Dependency</td><td>Lag (days)</td></tr>";
          for (int iR = 1; iR < (iRecMax + 1); iR++)
          {
            rs.absolute(iR);
            stToProject = rs.getString("nmToProject");
            stToId = rs.getString("nmToId");
            stLag = rs.getString("nmPercent");
            stType = rs.getString("stComment");

            stReturn += "<tr class=d0><td>";
            if (!stPrj.equals(stToProject))
            {
              stReturn += this.ebEnt.dbDyn.ExecuteSql1("select ProjectName from Projects where RecId=" + stToProject) + "</td>";
              stToBaseline = this.ebEnt.dbDyn.ExecuteSql1("select CurrentBaseline from Projects where RecId=" + stToProject);
            } else
            {
              stReturn += "&nbsp;</td>";
              stToBaseline = stBaseline;
            }
            stToTitle = this.ebEnt.dbDyn.ExecuteSql1("select SchTitle from Schedule"
              + " where nmProjectId=" + stToProject + " and nmBaseline=" + stToBaseline + " and RecId=" + stToId) + "</td>";

            stReturn += "<td align=right>" + stToId + "</td>";
            stReturn += "<td>" + stToTitle + "</td>";
            stReturn += "<td>" + ConstraintList("type_" + iR, stType, " DISABLED ") + "</td>";

            double dLag = 0;
            if (stLag != null && stLag.length() > 0)
            {
              dLag = Double.parseDouble(stLag);
            }
            if (dLag != 0)
              stReturn += "<td>" + dLag + " days</td>";
            else
              stReturn += "<td>&nbsp;</td>";
            stReturn += "</tr>";
          }
          if (iMode == 0)
            stReturn += "</table></td><td valign=top>&nbsp;<input type=image name=savedata value=9991 onClick=\"return setSubmitId(9992);\" src='./common/b_edit.png'></td></tr></table>";
          else
            stReturn += "</table></td><td valign=top>&nbsp;</td></tr></table>";
        } else if (iMode == 1)
        {
          stReturn += "<td>Action</td><td>Project</td><td>Task ID</td><td>Dependency</td><td>Lag [optional]</td></tr>";
          ResultSet rsPrj = this.ebEnt.dbDyn.ExecuteSql("select * from Projects order by ProjectName ");
          rsPrj.last();
          int iMaxPrj = rsPrj.getRow();

          for (int iR = 1; iR <= (iRecMax + 1); iR++)
          {
            stReturn += "<tr class=d0>";
            if (iR < (iRecMax + 1))
            {
              rs.absolute(iR);
              stToProject = rs.getString("nmToProject");
              stToId = rs.getString("nmToId");
              stLag = rs.getString("nmPercent");
              stType = rs.getString("stComment");
              stReturn += "<td valign=top align=center>";
              stReturn += "<input type=image name=del id=del value=" + iR + " onClick=\"return setSubmitId2(9992," + iR + ");\" src='./common/b_drop.png'></td>";
            } else
            {
              stToProject = stPrj;
              stToId = "";
              stLag = "";
              stType = "fs";
              stReturn += "<td>Add new:</td>";
            }

            stReturn += "<td><select name=prj_" + iR + " id=prj_" + iR + ">";

            for (int iLc = 1; iLc <= iMaxPrj; iLc++)
            {
              rsPrj.absolute(iLc);
              stReturn += this.ebEnt.ebUd.addOption2(rsPrj.getString("ProjectName"), rsPrj.getString("RecId"), stToProject);
            }
            stReturn += "<td align=right><input type=text name=id_" + iR + " id=id_" + iR + " value=\"" + stToId + "\" size=5 style='text-align:right'></td>";
            stReturn += "<td>" + ConstraintList("type_" + iR, stType, "") + "</td>";
            stReturn += "<td align=right><input type=text name=lag_" + iR + " id=lag_" + iR + " value=\"" + stLag + "\" size=5 style='text-align:right'> (days)</td>";
            stReturn += "</tr>";
          }

          stReturn += "</table><br>"
            + "<input type=hidden name=imax id=imax value='" + (iRecMax + 1) + "'>"
            + "<input type=hidden name=giVar id=giVar value='-1'>"
            + "<input type=submit name=savedata value='Save and Return'  onClick=\"return setSubmitId(9972);\">"
            + "<input type=submit name=savedata value='Save and Insert New'  onClick=\"return setSubmitId(9992);\">"
            + "<input type=submit name=cancel2 value='Cancel'  onClick=\"setSubmitId(8888);\">"
            + "<br>&nbsp;";
        }
      }
    } catch (Exception e)
    {
      stError += "ERROR makeSuccessors: " + e;
    }
    return stReturn;
  }

  public String makeInventory(ResultSet rsField, String stValue, int iMode)
  {
    String stReturn = "";
    String stInventoryId = "";
    String stInventoryQty = "";
    int iInvMax = 0;
    String[] aRecords = null;
    String[] aFields = null;
    String stSql = null;
    ResultSet rs = null;

    try
    {
      if ((stValue == null || stValue.length() <= 0) && iMode == 2)
        return ""; //---------------------------------------------->

      if (stValue.length() <= 0 && iMode == 0)
      {
        stReturn += "<tr><td>" + rsField.getString("stLabel") + ":</td><td align=left>";
        stReturn += "&nbsp;<input type=image name=savedata value=9993 onClick=\"return setSubmitId(9993);\" src='./common/b_edit.png'></td></tr>";
      } else
      {
        if (stValue.length() <= 0)
          iInvMax = 0;
        else
        {
          aRecords = stValue.split("\\|");
          iInvMax = aRecords.length;
        }
        if (iMode == 2)
        {
          stReturn += "<table border=0><tr><td valign=top>";
          stReturn += "<table border=0 cellpadding=3 cellspacing=1><tr class=d1>";
        }
        if (iMode == 0)
        {
          stReturn += "<h4>" + rsField.getString("stLabel") + "</h4>";
          stReturn += "<table border=0><tr><td valign=top>";
          stReturn += "<table border=0 bgcolor='blue' cellpadding=3 cellspacing=1><tr class=d1>";
        }
        if (iMode == 1)
        {
          stReturn += "<h4>" + rsField.getString("stLabel") + "</h4>";
          stReturn += "<table border=0 bgcolor='blue' cellpadding=3 cellspacing=1><tr class=d1>";
        }
        if (iMode == 0 || iMode == 2)
        {
          stReturn += "<td>Inventory</td><td>Quantity</td><td>Unit Price</td><td>Cost</td></tr>";
          for (int iR = 0; iR < iInvMax; iR++)
          {
            try
            {
              NumberFormat df = new DecimalFormat("$ #,###,###,##0.0");
              aFields = aRecords[iR].split("~");
              stSql = "SELECT * FROM Inventory where RecId=" + aFields[0];
              rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
              rs.absolute(1);

              stReturn += "<tr class=d0>";
              stReturn += "<td>" + rs.getString("InventoryName") + "</td>";
              stReturn += "<td align=right>" + aFields[1] + "</td>";
              int iQty = Integer.parseInt(aFields[1]);
              double dCost = iQty * rs.getDouble("CostPerUnit");
              stReturn += "<td align=right>" + df.format(rs.getDouble("CostPerUnit")) + "</td>";
              stReturn += "<td align=right>" + df.format(dCost) + "</td>";
              stReturn += "</tr>";
            } catch (Exception e)
            {
            }
          }
          if (iMode == 0)
            stReturn += "</table></td><td valign=top>&nbsp;<input type=image name=savedata value=9993 onClick=\"return setSubmitId(9993);\" src='./common/b_edit.png'></td></tr></table>";
          else
            stReturn += "</table></td><td valign=top>&nbsp;</td></tr></table>";
        } else if (iMode == 1)
        {
          stReturn += "<td>Action</td><td>Inventory</td><td>Quantity</td></tr>";
          stSql = "SELECT * FROM Inventory where Quantity > 0 order by InventoryName";
          ResultSet rsAll = this.ebEnt.dbDyn.ExecuteSql(stSql);
          rsAll.last();
          int iMaxAll = rsAll.getRow();
          for (int iR = 0; iR <= iInvMax; iR++)
          {
            stReturn += "<tr class=d0>";
            if (iR < iInvMax)
            {
              aFields = aRecords[iR].split("~", -1);
              stInventoryId = aFields[0];
              stInventoryQty = aFields[1];
              stReturn += "<td valign=top align=center>";
              stReturn += "<input type=image name=del id=del value=" + iR + " onClick=\"return setSubmitId2(9993," + iR + ");\" src='./common/b_drop.png'></td>";
            } else
            {
              stInventoryId = "";
              stInventoryQty = "";
              stReturn += "<td>Add new:</td>";
            }
            stReturn += "<td><select name=inv_" + iR + " id=inv_" + iR + ">";

            stReturn += this.ebEnt.ebUd.addOption2("-- Select Inventory --", "0", stInventoryId);
            for (int iLc = 1; iLc <= iMaxAll; iLc++)
            {
              rsAll.absolute(iLc);
              stReturn += this.ebEnt.ebUd.addOption2(rsAll.getString("InventoryName"), rsAll.getString("RecId"), stInventoryId);
            }
            stReturn += "<td align=right><input type=text name=qty_" + iR + " id=qty_" + iR + " value=\"" + stInventoryQty + "\" size=5 style='text-align:right'></td>";
            stReturn += "</tr>";
          }
          stReturn += "</table><br>"
            + "<input type=hidden name=imax id=imax value='" + (iInvMax + 1) + "'>"
            + "<input type=hidden name=giVar id=giVar value='-1'>"
            + "<input type=submit name=savedata value='Save and Return'  onClick=\"return setSubmitId(9973);\">"
            + "<input type=submit name=savedata value='Save and Insert New'  onClick=\"return setSubmitId(9993);\">"
            + "<input type=submit name=cancel2 value='Cancel'  onClick=\"setSubmitId(8888);\">"
            + "<br>&nbsp;";
        }
      }
    } catch (Exception e)
    {
      stError += "ERROR makeInventory: " + e;
    }
    return stReturn;
    // end inventory
  }

  public String makeOtherResources(ResultSet rsField, String stValue, int iMode)
  {
    String stReturn = "";
    String stInventoryId = "";
    String stInventoryQty = "";
    int iInvMax = 0;
    String[] aRecords = null;
    String[] aFields = null;
    String stSql = null;
    ResultSet rs = null;

    try
    {
      if (stValue.length() <= 0 && iMode == 2)
      {
        return ""; //----------------------------->
      }
      if (stValue.length() <= 0 && iMode == 0)
      {
        stReturn += "<tr><td>" + rsField.getString("stLabel") + ":</td><td align=left>";
        stReturn += "&nbsp;<input type=image name=savedata value=9994 onClick=\"return setSubmitId(9994);\" src='./common/b_edit.png'></td></tr>";
      } else
      {
        if (stValue.length() <= 0)
          iInvMax = 0;
        else
        {
          aRecords = stValue.split("\\|");
          iInvMax = aRecords.length;
        }
        if (iMode == 2)
        {
          stReturn += "<table border=0><tr><td valign=top>";
          stReturn += "<table border=0 cellpadding=3 cellspacing=1><tr class=d1>";
        }
        if (iMode == 0)
        {
          stReturn += "<h4>" + rsField.getString("stLabel") + "</h4>";
          stReturn += "<table border=0><tr><td valign=top>";
          stReturn += "<table border=0 bgcolor='blue' cellpadding=3 cellspacing=1><tr class=d1>";
        }
        if (iMode == 1)
        {
          stReturn += "<h4>" + rsField.getString("stLabel") + "</h4>";
          stReturn += "<table border=0 bgcolor='blue' cellpadding=3 cellspacing=1><tr class=d1>";
        }
        if (iMode == 0 || iMode == 2)
        {
          stReturn += "<td>Other Resource</td><td>Cost</td></tr>";
          for (int iR = 0; iR < iInvMax; iR++)
          {
            try
            {
              aFields = aRecords[iR].split("~");

              stReturn += "<tr class=d0>";
              stReturn += "<td>" + aFields[0] + "</td>";
              stReturn += "<td>" + aFields[1] + "</td>";
              stReturn += "</tr>";
            } catch (Exception e)
            {
            }
          }
          if (iMode == 0)
            stReturn += "</table></td><td valign=top>&nbsp;<input type=image name=savedata value=9994 onClick=\"return setSubmitId(9994);\" src='./common/b_edit.png'></td></tr></table>";
          else
            stReturn += "</table></td><td valign=top>&nbsp;</td></tr></table>";
        } else if (iMode == 1)
        {
          stReturn += "<td>Action</td><td>Other Resource</td><td>Cost</td></tr>";
          for (int iR = 0; iR <= iInvMax; iR++)
          {
            stReturn += "<tr class=d0>";
            if (iR < iInvMax)
            {
              aFields = aRecords[iR].split("~", -1);
              stInventoryId = aFields[0];
              stInventoryQty = aFields[1];
              stReturn += "<td valign=top align=center>";
              stReturn += "<input type=image name=del id=del value=" + iR + " onClick=\"return setSubmitId2(9994," + iR + ");\" src='./common/b_drop.png'></td>";
            } else
            {
              stInventoryId = "";
              stInventoryQty = "";
              stReturn += "<td>Add new:</td>";
            }
            stReturn += "<td align=right><input type=text name=oth_" + iR + " id=oth_" + iR + " value=\"" + stInventoryId + "\" size=60 style='text-align:left'></td>";
            stReturn += "<td align=right><input type=text name=cst_" + iR + " id=cst_" + iR + " value=\"" + stInventoryQty + "\" size=5 style='text-align:right'></td>";
            stReturn += "</tr>";
          }
          stReturn += "</table><br>"
            + "<input type=hidden name=imax id=imax value='" + (iInvMax + 1) + "'>"
            + "<input type=hidden name=giVar id=giVar value='-1'>"
            + "<input type=submit name=savedata value='Save and Return'  onClick=\"return setSubmitId(9974);\">"
            + "<input type=submit name=savedata value='Save and Insert New'  onClick=\"return setSubmitId(9994);\">"
            + "<input type=submit name=cancel2 value='Cancel'  onClick=\"setSubmitId(8888);\">"
            + "<br>&nbsp;";
        }
      }
    } catch (Exception e)
    {
      stError += "ERROR makeOtherResources: " + e;
    }
    return stReturn;
    // end inventory
  }

  private String makeGeneric(ResultSet rsField, String stValue, String stLabel)
  {
    String stReturn = "";
    try
    {
      String stLink2 = "";
      if (stValue.length() <= 0)
      {
        stReturn += "<tr><td>" + stLabel + ":</td><td align=left>"
          + "&nbsp;<a title='Edit' href='" + stLink2 + "&do=edit'><img src='./common/b_edit.png'></a></td></tr>";
      } else
      {
        stReturn += "<table border=1><tr><th>Labor Category</th><th>#</th><th>Effort</th><th>To Date</th><th>Allocated</th>"
          + "<th>Consideration</th><th>&nbsp;<a title='Edit' href='" + stLink2 + "&do=edit'><img src='./common/b_edit.png'></a></th></tr>";
        stReturn += "</table>" + stValue;
      }
    } catch (Exception e)
    {
      stError += "ERROR makeGeneric: " + e;
    }
    return stReturn;
  }

  public String fullSchTitle(String stR, String stPk, int nmBaseline)
  {
    String stReturn = "";
    try
    {
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("select * from Schedule where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId=" + stR);
      rs.last();
      rs.absolute(1);
      stReturn += fullSchTitle(rs, stPk, nmBaseline);
    } catch (Exception e)
    {
      this.stError += "<br>ERROR fullSchTitle [" + stR + "] " + e;
    }
    return stReturn;
  }

  public String fullSchTitle(ResultSet rs, String stPk, int nmBaseline)
  {
    String stReturn = "";
    try
    {
      String[] aTitle = new String[rs.getInt("SchLevel") + 1];
      aTitle[rs.getInt("SchLevel")] = rs.getString("SchTitle");
      if (rs.getInt("SchLevel") > 0)
      {
        int iParent = rs.getInt("SchParentRecId");
        if (iParent > 0)
        {
          for (int iL = rs.getInt("SchLevel") - 1; iL >= 0; iL--)
          {
            ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql("select * from Schedule where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId= " + iParent);
            rs1.absolute(1);
            aTitle[iL] = rs1.getString("SchTitle");
            iParent = rs1.getInt("SchParentRecId");
            rs1.close();
          }
        }
      }
      for (int iL = 0; iL <= rs.getInt("SchLevel"); iL++)
      {
        if (iL > 0)
        {
          stReturn += "<br>";
          for (int i = 0; i < (iL * 2); i++)
            stReturn += "&nbsp;";
        }
        stReturn += aTitle[iL];
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR fullSchTitle " + e;
    }

    return stReturn;
  }

  public String fullReqTitle(String stR, String stPk, int nmBaseline)
  {
    String stReturn = "";
    try
    {
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("select * from Requirements where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId=" + stR);
      rs.last();
      rs.absolute(1);
      stReturn += fullReqTitle(rs, stPk, nmBaseline);
    } catch (Exception e)
    {
      this.stError += "<br>ERROR fullReqTitle [" + stR + "] " + e;
    }
    return stReturn;
  }
  
  public String fullReqTitle(ResultSet rs, String stPk, int nmBaseline)
  {
    String stReturn = "";
    try
    {
      String[] aTitle = new String[rs.getInt("ReqLevel") + 1];
      aTitle[rs.getInt("ReqLevel")] = rs.getString("ReqTitle");
      if (rs.getInt("ReqLevel") > 0)
      {
        int iParent = rs.getInt("ReqParentRecId");
        for (int iL = rs.getInt("ReqLevel") - 1; iL >= 0; iL--)
        {
          ResultSet rs1 = this.ebEnt.dbDyn.ExecuteSql("select * from Requirements where nmProjectId=" + stPk + " and nmBaseline=" + nmBaseline + " and RecId= " + iParent);
          rs1.absolute(1);
          aTitle[iL] = rs1.getString("ReqTitle");
          iParent = rs1.getInt("ReqParentRecId");
          rs1.close();
        }
      }
      for (int iL = 0; iL <= rs.getInt("ReqLevel"); iL++)
      {
        if (iL > 0)
        {
          stReturn += "<br>";
          for (int i = 0; i < (iL * 2); i++)
            stReturn += "&nbsp;";
        }
        stReturn += aTitle[iL];
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERROR fullReqTitle " + e;
    }

    return stReturn;
  }

  public String makeLaborCategories(ResultSet rsField, String stValue, int iMode)
  {
    String stReturn = "";
    String stRowspan = "";
    String[] aFields = null;
    String[] aRecords = null;
    int iRecMax = 0;
    try
    {
      NumberFormat formatter = new DecimalFormat("#0.0");
      String stChild = this.ebEnt.ebUd.request.getParameter("child");
      String stPrj = this.ebEnt.ebUd.request.getParameter("pk");
      String stTask = this.ebEnt.ebUd.request.getParameter("r");
      if ((stValue == null || stValue.length() <= 0) && iMode == 2)
        return ""; //------------------------------------------------------->

      if (stValue.length() <= 0 && iMode == 0)
      {
        stReturn += "<tr><td>" + rsField.getString("stLabel") + ":</td><td align=left>";
        stReturn += "&nbsp;<input type=image name=savedata value=9990 onClick=\"return setSubmitId(9990);\" src='./common/b_edit.png'></td></tr>";
      } else
      {
        if (iMode == 0 || iMode == 2)
        {
          stReturn += "<table border=0><tr><td valign=top>";
          if (iMode == 2)
            stReturn += "<table border=0 cellpadding=3 cellspacing=1><tr class=d1>";
          else
            stReturn += "<table border=0 bgcolor='blue' cellpadding=3 cellspacing=1><tr class=d1>";
          stReturn += "<td colspan=2>Labor Category / Max Users</td><td>Effort</td>"
            + "<td>Resource Considerations</td><td colspan=2>Allocated to / Hrs</td>"
            + "<td>Actual</td><td>% Done</td>"
            + "</tr>";
        }
        if (iMode == 1)
        {
          stReturn += "<table border=0 bgcolor='blue' cellpadding=3 cellspacing=1><tr class=d1>";
          stReturn += "<td>Action</td>";
          stReturn += "<td colspan=2>Labor Category / Max Users</td><td>Effort</td>"
            + "<td>Must Assign</td><td>Most Desireable</td><td>Least Desirable</td><td>Do Not Assign</td></tr>";
        }
        if (stValue.length() > 0)
        {
          aRecords = stValue.split("\\|", -1);
          iRecMax = aRecords.length;
        } else
        {
          aRecords = new String[1];
          iRecMax = 0;
        }
        if (iMode == 0 || iMode == 2)
        {
          for (int iR = 0; iR < iRecMax; iR++)
          {
            aFields = aRecords[iR].split("~", -1); // LcId, MaxEmployess, Effort,
            stReturn += "<tr class=d0>";

            ResultSet rsLc = this.ebEnt.dbDyn.ExecuteSql("select * from LaborCategory where nmLcId=" + aFields[0]);
            rsLc.absolute(1);
            int iMaxUsers = Integer.parseInt(aFields[1]);

            ResultSet rsAllocate = this.ebEnt.dbDyn.ExecuteSql("select count(*) cnt,ap.nmUserId,"
              + " u.FirstName,u.LastName,sum(nmAllocated) nmAllocated,sum(nmActual) nmActual"
              + " from teb_allocateprj ap, Users u where ap.nmUserId=u.nmUserId"
              + " and nmLc=" + aFields[0] + " and nmPrjId=" + stPrj + " and nmTaskId=" + stTask + " group by ap.nmUserId");
            rsAllocate.last();
            int iMaxAllocate = rsAllocate.getRow();
            if (iMaxAllocate > 0)
            {
              stRowspan = " rowspan=" + iMaxAllocate + " ";
            } else
            {
              stRowspan = " rowspan=1 ";
            }
            if (rsLc.getInt("NumberUsers") < iMaxUsers)
              stReturn += "<td " + stRowspan + " style='background-color:#FF0033; color: white;'>" + rsLc.getString("LaborCategory") + "</td>";
            else
              stReturn += "<td " + stRowspan + ">" + rsLc.getString("LaborCategory") + "</td>";
            stReturn += "<td " + stRowspan + " align=right>" + aFields[1] + "</td>";

            stReturn += "<td " + stRowspan + " align=right>" + formatter.format(Double.parseDouble(aFields[2])) + "</td>";
            stReturn += "<td " + stRowspan + ">";
            stReturn += makeUsers("Must allocate:", aFields[3]);
            stReturn += makeUsers("Most desirable:", aFields[4]);
            stReturn += makeUsers("Least desireable:", aFields[5]);
            stReturn += makeUsers("Do not allocate:", aFields[6]);
            stReturn += "</td>";

            if (iMaxAllocate > 0)
            {
              for (int iA = 1; iA <= iMaxAllocate; iA++)
              {
                rsAllocate.absolute(iA);
                if (iA > 1)
                  stReturn += "</tr><tr class=d0>";
                stReturn += "<td>" + rsAllocate.getString("FirstName") + " " + rsAllocate.getString("LastName") + "</td>";
                stReturn += "<td align=right>" + formatter.format(rsAllocate.getDouble("nmAllocated")) + "</td>";
                stReturn += "<td align=right>" + formatter.format(rsAllocate.getDouble("nmActual")) + "</td>";
                double dDone = rsAllocate.getDouble("nmActual") / rsAllocate.getDouble("nmAllocated") * 100;
                stReturn += "<td align=right>" + formatter.format(dDone) + " %</td>";
                //if (iMaxAllocate > 1 && iA < iMaxAllocate)
                //  stReturn += "</tr>";
              }
            } else
            {
              stReturn += "<td colspan=4>Not allocated</td>";
            }

            stReturn += "</tr>";
          }
          if (iMode == 2)
            stReturn += "</table></td><td valign=top>&nbsp;</td></tr></table>";
          else
            stReturn += "</table></td><td valign=top>&nbsp;<input type=image name=savedata value=9990 onClick=\"return setSubmitId(9990);\" src='./common/b_edit.png'></td></tr></table>";
        } else if (iMode == 1)
        {
          stRowspan = "";
          for (int iR = 0; iR <= iRecMax; iR++)
          {
            stReturn += "<tr class=d0>";
            if (iR < iRecMax)
            {
              aFields = aRecords[iR].split("~", -1); // LcId, MaxEmployess, Effort,
              stReturn += "<td valign=top align=center>";
              stReturn += "<input type=image name=del id=del value=" + iR + " onClick=\"return setSubmitId2(9990," + iR + ");\" src='./common/b_drop.png'></td>";
            } else
            {
              stReturn += "<td>Add new:</td>";
              aFields = new String[7];
              for (int i = 0; i < aFields.length; i++)
              {
                if (i == 1)
                  aFields[i] = "1";
                else
                  aFields[i] = "";
              }
            }

            stReturn += "<td " + stRowspan + "><select name=lc_" + iR + " id=lc_" + iR + ">";
            ResultSet rsLcAll = this.ebEnt.dbDyn.ExecuteSql("SELECT * FROM LaborCategory order by LaborCategory");
            rsLcAll.last();
            int iMaxLc = rsLcAll.getRow();
            stReturn += this.ebEnt.ebUd.addOption2("-- Select Labor Category --", "0", aFields[0]);
            for (int iLc = 1; iLc <= iMaxLc; iLc++)
            {
              rsLcAll.absolute(iLc);
              stReturn += this.ebEnt.ebUd.addOption2(rsLcAll.getString("LaborCategory"), rsLcAll.getString("nmLcId"), aFields[0]);
            }
            stReturn += "</select></td>";
            stReturn += "<td " + stRowspan + "><select name=nr_" + iR + " id=nr_" + iR + ">";
            for (int iLc = 1; iLc <= 20; iLc++)
            {
              stReturn += this.ebEnt.ebUd.addOption2("" + iLc, "" + iLc, aFields[1]);
            }
            stReturn += "</select></td>";
            stReturn += "<td " + stRowspan + "><input type=text size=5 style='text-align:right' name=est_" + iR + " id=est_" + iR + " value=\"" + aFields[2] + "\"></td>";
            stReturn += "<td " + stRowspan + ">" + getMulitUsers(stChild, 9001, "_must_" + iR, aFields[3], "lc_" + iR) + "</td>";
            stReturn += "<td " + stRowspan + ">" + getMulitUsers(stChild, 9002, "_most_" + iR, aFields[4], "lc_" + iR) + "</td>";
            stReturn += "<td " + stRowspan + ">" + getMulitUsers(stChild, 9003, "_least_" + iR, aFields[5], "lc_" + iR) + "</td>";
            stReturn += "<td " + stRowspan + ">" + getMulitUsers(stChild, 9004, "_not_" + iR, aFields[6], "lc_" + iR) + "</td>";
            stReturn += "</tr>";
          }
          stReturn += "</table><br>"
            + "<input type=hidden name=imax id=imax value='" + (iRecMax + 1) + "'>"
            + "<input type=hidden name=giVar id=giVar value='-1'>"
            + "<input type=submit name=savedata value='Save and Return'  onClick=\"return setSubmitId(9970);\">"
            + "<input type=submit name=savedata value='Save and Insert New'  onClick=\"return setSubmitId(9990);\">"
            + "<input type=submit name=cancel2 value='Cancel'  onClick=\"setSubmitId(8888);\">"
            + "<br>&nbsp;";
        }
      }
    } catch (Exception e)
    {
      stError += "ERROR makeLaborCategories: " + e;
    }
    return stReturn;
  }

  public String getMulitUsers(String stChild, int iF, String stExtraFieldName, String stValue, String stLc)
  {
    String stEdit = "";
    int nmRows = 0;
    int nmCols = 15;
    String stId = "f" + iF + stExtraFieldName;
    stExtraFieldName = "_ta";
    String stNames = stValue;
    int iCount = 0;
    if (stNames.length() > 0)
    {
      stNames = stNames.replace("~", ",");
      String[] aV = stNames.split(",", -1);
      stNames = "";
      for (int i = 0; i < aV.length; i++)
      {
        if (aV[i].length() > 0)
        {
          nmRows++;
          stNames += this.ebEnt.dbDyn.ExecuteSql1("select concat(FirstName,' ',LastName) from Users where nmUserId=" + aV[i]);
          stNames += "\n";
        }
      }
    }
    if (nmRows <= 0)
      nmRows = 1;
    stEdit += "<table><tr><td><textarea DISABLED name=" + stId + stExtraFieldName + " id=" + stId + stExtraFieldName + " rows=" + nmRows + " cols=" + nmCols + ">" + stNames + "</textarea>"
      + "<input type=hidden name=" + stId + " id=" + stId + " value=\"" + stValue + "\">";
    stEdit += "</td><td valign=top>";
    stEdit += "<input type=image border=0 src='./images/stickman2.png' alt='Select Users' class=imageStyle"
      + " onClick='return getPopupValue3(this.form." + stId + stExtraFieldName + ",this.form." + stId + ",1,\"" + stValue + "\",\"" + stLc + "\");'>"
      + "<input type=image border=0 src='./images/stickman2.png' alt='Select Users' class=imageStyle"
      + " onClick='return getPopupValue3(this.form." + stId + stExtraFieldName + ",this.form." + stId + ",1,\"" + stValue + "\",\"" + stLc + "\");'>";
    stEdit += "</td></tr></table>";
    return stEdit;
  }

  private String makeUsers(String stType, String stList)
  {
    String stReturn = "";
    try
    {
      if (stList != null && stList.length() > 0)
      {
        stReturn += "<font class=small><u>" + stType + "</u><br><b>";
        String[] aV = stList.split(",", -1);
        for (int i = 0; i < aV.length; i++)
        {
          if (aV[i].length() > 0)
          {
            stReturn += "&nbsp;&nbsp;" + this.ebEnt.dbDyn.ExecuteSql1("select concat(FirstName,' ',LastName) from Users where nmUserId=" + aV[i]);
            stReturn += "<br>";
          }
        }
        stReturn += "</b></font>";
      }
    } catch (Exception e)
    {
    }
    return stReturn;
  }

  public String ConstraintList(String stF, String stValue, String stDisabled)
  {
    String stEdit = "<select name=" + stF + " id=" + stF + " " + stDisabled + ">";
    stEdit += this.ebEnt.ebUd.addOption("Finish to Finish", "ff", stValue);
    stEdit += this.ebEnt.ebUd.addOption("Finish to Start", "fs", stValue);
    stEdit += this.ebEnt.ebUd.addOption("Start to Start", "ss", stValue);
    stEdit += this.ebEnt.ebUd.addOption("Start to Finish", "sf", stValue);
    stEdit += "</select>";
    return stEdit;
  }

  public String makeIndicators(ResultSet rsField, String stValue, int iMode)
  {
    String stReturn = "";

    try
    {
      int iFlags = 0;
      if (stValue != null && stValue.length() > 0)
        iFlags = Integer.parseInt(stValue);
      if (iMode == 1)
        stReturn += "<table bgcolor=blue cellpadding=1 cellspacing=1><tr class=d1><td><b>Indicators: </b></td>";
      stReturn += addIndicator(iMode, 1, 0x1000, iFlags, "Done"); // Completed
      stReturn += addIndicator(iMode, 0, 0x200, iFlags, "CP"); // CP=Critical Path
      stReturn += addIndicator(iMode, 1, 0x2000, iFlags, "D"); // Deliverable
      stReturn += addIndicator(iMode, 0, 0x20, iFlags, "F"); // F=Fixed Date
      stReturn += addIndicator(iMode, 0, 0x100, iFlags, "FD"); // FD = Foreign Dependency
      stReturn += addIndicator(iMode, 0, 0x40, iFlags, "L"); // L=Late Task from Original Schedule
      stReturn += addIndicator(iMode, 0, 0x10, iFlags, "LL"); // LL=Low-Level Task
      stReturn += addIndicator(iMode, 1, 0x4000, iFlags, "M"); // Milestone
      stReturn += addIndicator(iMode, 0, 0x80, iFlags, "D"); // Deliverable
      stReturn += addIndicator(iMode, 0, 0x4, iFlags, "P"); // P=Parent Task
      stReturn += addIndicator(iMode, 0, 0x80, iFlags, "PLA"); // PLA=Permanent Labor Assignment
      if (iMode == 1)
        stReturn += "</tr></table>";
    } catch (Exception e)
    {
      stError += "ERROR makeIndicators: " + e;
    }
    return stReturn;
    // end inventory
  }

  public String addIndicator(int iMode, int iEdit, int iMask, int iFlags, String stLabel)
  {
    String stReturn = "";

    try
    {
      String stChecked = "";
      if (iMode == 1)
      {
        if (iEdit == 0)
          stChecked += " DISABLED ";
        if ((iFlags & iMask) != 0)
          stChecked += " checked ";
        stReturn += "<td valign=middle><input type=checkbox name=indicator_" + iMask + " value='" + iMask + "' " + stChecked + "> " + stLabel + "&nbsp;</td>";
      } else
      {
        if ((iFlags & iMask) != 0)
          stReturn = stLabel + " ";
      }
    } catch (Exception e)
    {
      stError += "ERROR addIndicator: " + e;
    }
    return stReturn;
    // end inventory
  }

  public int addAuditTrail(int nmFieldId, String stPk, String stNew)
  {
    int iReturn = 0;
    try
    {
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("select t.*,f.stDbFieldName from "
        + "teb_table t, teb_fields f where f.nmTabId=t.nmTableId and f.nmForeignId=" + nmFieldId);
      rs.absolute(1);
      String stOld = "";
      if (nmFieldId != 5)
        this.ebEnt.dbDyn.ExecuteSql1("select " + rs.getString("stDbFieldName") + " from "
          + rs.getString("stDbTableName") + " where " + rs.getString("stPk") + " = " + stPk);
      String stSql = "insert into X25AuditTrail "
        + "(nmUserId,dtEventStartTime,nmTableId,nmPk"
        + ",nmProject,nmBaseline,nmFieldId,stOldValue,stNewValue) values"
        + "(" + this.ebEnt.ebUd.getLoginId() + ",now()," + rs.getString("nmTableId") + "," + stPk + ""
        + ",0,0," + nmFieldId + ","
        + this.ebEnt.dbEnterprise.fmtDbString(stOld) + ","
        + this.ebEnt.dbEnterprise.fmtDbString(stNew) + ")";
      this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);

      if (!stNew.equals(stOld))
        switch (nmFieldId)
        {
          case 119:
            this.epsClient.epsUd.makeTask(3, this.ebEnt.dbDyn.ExecuteSql1("select ProjectName from Projects where RecId=" + stPk)); //3	1	Completed Project	Enabled	Yes
            break;
        }
    } catch (Exception e)
    {
      stError += "<BR>ERROR addAuditTrail nmFieldId " + nmFieldId + ": " + e;
    }
    return iReturn;
  }

  public int addAuditTrail(ResultSet rsTable, ResultSet rsOld, ResultSet rsNew, String stPk, String stProject, int nmBaseline)
  {
    int iChanged = 0;
    int iUsr = 0;
    int iSch = 0;
    int iReq = 0;
    int nmForeignId = 0;
    try
    {
      if (stProject.length() <= 0)
        stProject = "0";

      rsOld.absolute(1);
      rsNew.absolute(1);
      ResultSetMetaData rsMetaData = rsOld.getMetaData();

      int iMaxFields = rsMetaData.getColumnCount();
      for (int iF = 1; iF <= iMaxFields; iF++)
      {
        String stOld = rsOld.getString(iF);
        String stNew = rsNew.getString(iF);
        String stField = "";
        int iDiff = 0;
        if ((stOld == null && stNew != null) || (stOld != null && stNew == null))
        {
          if (stOld == null)
            stOld = "null";
          if (stNew == null)
            stNew = "null";
          iDiff = 1;
        }
        if (iDiff == 0 && stOld != null && stNew != null)
        {
          if (stOld.length() != stNew.length())
            iDiff = 1;
          else if (!stOld.equals(stNew))
            iDiff = 1;
        }
        if (iDiff == 1)
        {
          stField = rsMetaData.getColumnName(iF);
          nmForeignId = this.ebEnt.dbDyn.ExecuteSql1n(
            "select nmForeignId from teb_fields where stDbFieldName=\"" + stField + "\" ");
          String stSql = "insert into X25AuditTrail "
            + "(nmUserId,dtEventStartTime,nmTableId,nmPk"
            + ",nmProject,nmBaseline,nmFieldId,stOldValue,stNewValue) values"
            + "(" + this.ebEnt.ebUd.getLoginId() + ",now()," + rsTable.getString("nmTableId") + "," + stPk + ""
            + "," + stProject + "," + nmBaseline + "," + nmForeignId + ","
            + this.ebEnt.dbEnterprise.fmtDbString(stOld) + ","
            + this.ebEnt.dbEnterprise.fmtDbString(stNew) + ")";
          this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
          switch (nmForeignId)
          {
            // Some field changes have special meaning.
            /*
            6	1	Inserted Requirement	Enabled	Yes
            
            17	1	New User	Enabled	Yes
            18	1	No Initial Verb Task	Enabled	Yes
            19	1	Special Days Approval	Enabled	Yes
            20	1	Timely Updated Schedule	Enabled	Yes
             */
            case 875: // Holidays
              this.epsClient.epsUd.epsLoadSpecialDays();
              break;

            case 119: // ProjectStatus
              if (stNew.equals("Completed"))
                this.epsClient.epsUd.makeTask(3, getProjectName(stPk)); //3	1	Completed Project	Enabled	Yes
              break;
            case 819: // SchFlags
              try
              {
                if (stOld.length() <= 0)
                  stOld = "0";
                if (stNew.length() <= 0)
                  stNew = "0";
                int iOldFlag = Integer.parseInt(stOld);
                int iNewFlag = Integer.parseInt(stNew);
                if ((iOldFlag & 0x1000) == 0 && (iNewFlag & 0x1000) != 0)
                {
                  if ((iNewFlag & 0x2000) != 0)
                    this.epsClient.epsUd.makeTask(1, getScheduleName(stPk, stProject, nmBaseline)); //1	1	Completed Deliverable	Enabled	Yes
                  if ((iNewFlag & 0x4000) != 0)
                    this.epsClient.epsUd.makeTask(2, getScheduleName(stPk, stProject, nmBaseline)); //2	1	Completed Milestone	Enabled	Yes
                }
              } catch (Exception e)
              {
                stError += "<BR>addAuditTrail 819 " + e;
              }
              break;
            default:
              break;
          }
          switch (rsTable.getInt("nmTableId"))
          {
            case 9:
              iUsr++;
              break;
            case 19:
              iReq++;
              break;
            case 21:
              iSch++;
              break;
          }
        }
      }
      if (iUsr > 0)
        this.epsClient.epsUd.makeTask(22, getUserName(stPk)); //22	1	Updated User	Enabled	Yes 4096
      if (iReq > 0)
      {
        this.epsClient.epsUd.makeTask(21, getRequirementName(stPk, stProject, nmBaseline)); //21	1	Updated Requirements	Enabled	Yes
      }
      if (iSch > 0)
      {
        this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set dtSchLastUpdate=now()"
          + " where nmProjectId=" + stProject + " and RecId=" + stPk + " and nmBaseline=" + nmBaseline);
        recalcSchedule(stPk, stProject, nmBaseline);
        this.epsClient.epsUd.makeTask(15, getScheduleName(stPk, stProject, nmBaseline)); //15	1	Modified Project Schedule	Enabled	Yes
      }
    } catch (Exception e)
    {
      stError += "<br>ERROR addAuditTrail [" + nmForeignId + "] " + e;
    }
    return iChanged;
  }

  public void recalcSchedule(String stPk, String stProject, int nmBaseline)
  {
    try
    {
      double dInventory = 0;
      double dOther = 0;
      double dApproved = 0;
      double nmExpenditureToDate = 0;
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("select * from Schedule"
        + " where nmProjectId=" + stProject + " and RecId=" + stPk + " and nmBaseline=" + nmBaseline);
      rs.absolute(1);
      String[] aRecords = null;
      String[] aFields = null;
      String stValue = rs.getString("SchInventory");
      if (stValue != null && stValue.length() > 0)
      {
        aRecords = stValue.split("\\|");
        for (int iR = 0; iR < aRecords.length; iR++)
        { //6~2~|3~1~
          aFields = aRecords[iR].split("~");
          int iQty = Integer.parseInt(aFields[1]);
          double dPrice = this.ebEnt.dbDyn.ExecuteSql1n("select CostPerUnit from Inventory where RecId=" + aFields[0]);
          dInventory += (iQty * dPrice);
        }
      }
      stValue = rs.getString("SchOtherResources");
      if (stValue != null && stValue.length() > 0)
      {
        aRecords = stValue.split("\\|");
        for (int iR = 0; iR < aRecords.length; iR++)
        { //travel1~123.45~|t2~99.99~
          aFields = aRecords[iR].split("~");
          double dPrice = Double.parseDouble(aFields[1]);
          dOther += dPrice;
        }
      }
      this.ebEnt.dbDyn.ExecuteUpdate("update Schedule set nmExpenditureToDate="
        + (dInventory + dOther + nmExpenditureToDate)
        + " where nmProjectId=" + stProject + " and RecId=" + stPk + " and nmBaseline=" + nmBaseline);
    } catch (Exception e)
    {
      stError += "<br>ERROR recalcSchedule [" + stPk + "] " + e;
    }
  }

  public String getProjectName(String stPk)
  {
    String stReturn = "";
    try
    {
      stReturn = this.ebEnt.dbDyn.ExecuteSql1("select ProjectName from Projects where RecId=" + stPk);
    } catch (Exception e)
    {
      stError += "<br>ERROR getProjectName " + e;
    }
    return stReturn;
  }

  public String getUserName(String stPk)
  {
    String stReturn = "";
    try
    {
      stReturn = this.ebEnt.dbDyn.ExecuteSql1("select concat(FirstName,' ',LastName) from Users where nmUserId=" + stPk);
    } catch (Exception e)
    {
      stError += "<br>ERROR getUserName " + e;
    }
    return stReturn;
  }

  public String getScheduleName(String stPk, String stProject, int nmBaseline)
  {
    String stReturn = "";
    try
    {
      ResultSet rs = this.ebEnt.dbDyn.ExecuteSql("select * from Projects p,Schedule s where"
        + " s.RecId=" + stPk + " and p.RecId=" + stProject + " and s.nmBaseline=" + nmBaseline);
      rs.absolute(1);
      stReturn = rs.getString("ProjectName") + " - Task ID: " + rs.getString("SchId") + " Title: "
        + rs.getString("SchTitle");
      rs.close();
    } catch (Exception e)
    {
      stError += "<br>ERROR getScheduleName " + e;
    }
    return stReturn;
  }

  public String getRequirementName(String stPk, String stProject, int nmBaseline)
  {
    String stReturn = "";
    try
    {
      ResultSet rs = null;
      if (stPk != null && stPk.length() > 0 && !stPk.equals("-1"))
      {
        rs = this.ebEnt.dbDyn.ExecuteSql("select * from Projects p,Requirements r where"
          + " r.RecId=" + stPk + " and p.RecId=" + stProject + " and r.nmBaseline=" + nmBaseline);
        rs.absolute(1);
        stReturn = rs.getString("ProjectName") + " - Requirement ID: " + rs.getString("ReqId") + " Title: "
          + rs.getString("ReqTitle");
      } else
      {
        rs = this.ebEnt.dbDyn.ExecuteSql("select * from Projects p where p.RecId=" + stProject);
        rs.absolute(1);
        stReturn = rs.getString("ProjectName");
      }
    } catch (Exception e)
    {
      stError += "<br>ERROR getRequirementName " + e;
    }
    return stReturn;
  }

  public String yearEndProcess()
  {
    String stReturn = "";
    try
    {
      /*Leftover Hours
      Drop-Down List
      PPM
      How many hours should be used for a subsequent period when calculating average hourly salary for a labor category?
       */
      // Calculate performance index
      // Reset to 100 actual, calc estimated to perf index and set on user's table
      //select count(*), nmUserId, sum(nmAllocated), sum(nmActualApproved) from teb_allocateprj where year(dtDatePrj) = year(curdate()) group by nmUserId;
    } catch (Exception e)
    {
      stError += "<br>ERROR getRequirementName " + e;
    }
    return stReturn;
  }

  public String getPriviledgeTypes(int nmPriviledge)
  {
    StringBuilder abReturn = new StringBuilder(255);

    if ((nmPriviledge & 0x400) != 0)
      abReturn.append(",Ad");
    if ((nmPriviledge & 0x80) != 0)
      abReturn.append(",Ba");
    if ((nmPriviledge & 0x200) != 0)
      abReturn.append(",Ex");
    if ((nmPriviledge & 0x800) != 0)
      abReturn.append(",Su");
    if ((nmPriviledge & 0x40) != 0)
      abReturn.append(",Pm");
    if ((nmPriviledge & 0x20) != 0)
      abReturn.append(",Ppm");
    if ((nmPriviledge & 0x1) != 0)
      abReturn.append(",Ptm");
    String stReturn = abReturn.toString();
    if (stReturn != null && stReturn.length() > 0)
      stReturn = stReturn.substring(1);
    return stReturn;
  }

  public String getMissingLc(int iType)
  {
    String stByPrjSch = "";
    String stSql = "";
    ResultSet rs = null;
    int iMax = 0;
    String[] aItems = null;
    String[] aFields = null;
    int iLc = 0;
    int iLcNumUsers = 0;

    try
    {
      //Missing Labor Resource
      if (iType == 1)
      {
        this.ebEnt.dbDyn.ExecuteUpdate("truncate table Missing_Labor_Report");
      }

      int iMaxLC = this.ebEnt.dbDyn.ExecuteSql1n("select max(nmLcId) from LaborCategory");
      iMaxLC++;
      int[] aLc = new int[iMaxLC];
      double[] aLcMissingHours = new double[iMaxLC];
      int[] aLcMissingUsers = new int[iMaxLC];
      int[] aLcAvailableUsers = new int[iMaxLC];
      String[] aLcSchId = new String[iMaxLC];
      String[] aLcName = new String[iMaxLC];
      for (int i = 0; i < aLc.length; i++)
      {
        aLc[i] = 0;
        aLcMissingUsers[i] = 0;
        aLcAvailableUsers[i] = 0;
        aLcMissingHours[i] = 0;
        aLcSchId[i] = "";
        aLcName[i] = "";
      }
      stSql = "select * from LaborCategory ";

      rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      iMax = rs.getRow();
      for (int iR = 1; iR <= iMax; iR++)
      {
        rs.absolute(iR);
        aLcAvailableUsers[rs.getInt("nmLcId")] = rs.getInt("NumberUsers");
        aLcName[rs.getInt("nmLcId")] = rs.getString("LaborCategory");
      }
      rs.close();

      stSql = "select p.ProjectName,s.* from Projects p, Schedule s where p.RecId=s.nmProjectId"
        + " and p.CurrentBaseline=s.nmBaseline and ( s.SchFlags & 0x10 ) != 0 and SchLaborCategories != ''";

      rs = this.ebEnt.dbDyn.ExecuteSql(stSql);
      rs.last();
      iMax = rs.getRow();
      for (int iR = 1; iR <= iMax; iR++)
      {
        rs.absolute(iR);
        aItems = rs.getString("SchLaborCategories").trim().split("\\|");
        for (int iI = 0; iI < aItems.length; iI++)
        {
          aFields = aItems[iI].trim().split("~");
          // 39~1~120.0~~~~
          iLc = Integer.parseInt(aFields[0]);
          iLcNumUsers = Integer.parseInt(aFields[1]);
          aLcMissingHours[iLc] = Double.parseDouble(aFields[2].replace(",", ""));
          if (aLcAvailableUsers[iLc] < iLcNumUsers)
          {
            //aLcSchId[iLc] += ","+rs.getString("nmProjectId")+"."+rs.getString("nmBaseline")+"."+rs.getString("RecId");
            aLcSchId[iLc] += "|" + rs.getString("ProjectName") + "." + rs.getString("SchId") + "." + aLcName[iLc] + "." + (iLcNumUsers - aLcAvailableUsers[iLc]);
            stByPrjSch += "<tr><td>" + rs.getString("ProjectName") + "</td><td align=right>" + rs.getString("SchId") + "</td><td>" + aLcName[iLc] + "</td><td align=right>" + (iLcNumUsers - aLcAvailableUsers[iLc]) + "</td></tr>";
            if (iType == 1)
            {
              this.ebEnt.dbDyn.ExecuteUpdate("insert into Missing_Labor_Report"
                + " (nmPrjIdMLC,nmTaskIdMLC,DateReported,TaskFinishDate,HoursNeeded) values"
                + "(" + rs.getString("nmProjectId") + "," + rs.getString("RecId")
                + ",now(),'" + rs.getString("SchFinishDate") + "'," + aLcMissingHours[iLc] + ")");
            }
            if ((iLcNumUsers - aLcAvailableUsers[iLc]) < aLcMissingUsers[iLc])
            {
              aLcMissingUsers[iLc] = (iLcNumUsers - aLcAvailableUsers[iLc]); // Set max missing
            }
          }
        }
      }
    } catch (Exception e)
    {
      stError += "<BR>ERROR getMissingLc: " + e;
    }
    return stByPrjSch;
  }

  public String makeCostEffectiveness(ResultSet rsFields, String stValue, ResultSet rsD)
  {
    String stReturn = "";
    try
    {
      ResultSet rsC = this.ebEnt.dbDyn.ExecuteSql("select * from teb_reflaborcategory rl, LaborCategory lc"
        + " where nmRefType=42 and nmRefId=" + rsD.getString("nmUserId")
        + " and rl.nmLaborCategoryId=lc.nmLcId order by LaborCategory");
      rsC.last();
      int iMaxC = rsC.getRow();

      for (int iC = 1; iC <= iMaxC; iC++)
      {
        rsC.absolute(iC);
        if (iC > 1)
          stReturn += "<br>";
        stReturn += rsC.getString("nmCostEffectiveness");
      }
    } catch (Exception e)
    {
      stReturn += "<BR>ERROR makeCostEffectiveness: " + e;
    }
    return stReturn;
  }

  public String makeProductivity(ResultSet rsFields, String stValue, ResultSet rsD)
  {
    String stReturn = "";
    try
    {
      ResultSet rsC = this.ebEnt.dbDyn.ExecuteSql("select * from teb_reflaborcategory rl, LaborCategory lc"
        + " where nmRefType=42 and nmRefId=" + rsD.getString("nmUserId")
        + " and rl.nmLaborCategoryId=lc.nmLcId order by LaborCategory");
      rsC.last();
      int iMaxC = rsC.getRow();

      for (int iC = 1; iC <= iMaxC; iC++)
      {
        rsC.absolute(iC);
        if (iC > 1)
          stReturn += "<br>";
        stReturn += rsC.getString("nmProductiviyFactor");
      }
    } catch (Exception e)
    {
      stReturn += "<BR>ERROR makeProductivity: " + e;
    }
    return stReturn;
  }

  public String makeEstimatedHours(ResultSet rsFields, String stValue, ResultSet rsD)
  {
    String stReturn = "";
    try
    {
      ResultSet rsC = this.ebEnt.dbDyn.ExecuteSql("select * from teb_reflaborcategory rl, LaborCategory lc"
        + " where nmRefType=42 and nmRefId=" + rsD.getString("nmUserId")
        + " and rl.nmLaborCategoryId=lc.nmLcId order by LaborCategory");
      rsC.last();
      int iMaxC = rsC.getRow();

      for (int iC = 1; iC <= iMaxC; iC++)
      {
        rsC.absolute(iC);
        if (iC > 1)
          stReturn += "<br>";
        stReturn += rsC.getString("nmEstimatedHours");
      }
    } catch (Exception e)
    {
      stReturn += "<BR>ERROR makeEstimatedHours: " + e;
    }
    return stReturn;
  }

  public String makeActualHours(ResultSet rsFields, String stValue, ResultSet rsD)
  {
    String stReturn = "";
    try
    {
      ResultSet rsC = this.ebEnt.dbDyn.ExecuteSql("select * from teb_reflaborcategory rl, LaborCategory lc"
        + " where nmRefType=42 and nmRefId=" + rsD.getString("nmUserId")
        + " and rl.nmLaborCategoryId=lc.nmLcId order by LaborCategory");
      rsC.last();
      int iMaxC = rsC.getRow();

      for (int iC = 1; iC <= iMaxC; iC++)
      {
        rsC.absolute(iC);
        if (iC > 1)
          stReturn += "<br>";
        stReturn += rsC.getString("nmActualHours");
      }
    } catch (Exception e)
    {
      stReturn += "<BR>ERROR makeActualHours: " + e;
    }
    return stReturn;
  }

  public String makeDivision(ResultSet rsFields, String stValue, ResultSet rsD)
  {
    String stReturn = "";
    try
    {
      ResultSet rsC = this.ebEnt.dbDyn.ExecuteSql("SELECT stDivisionName FROM teb_refdivision rd, teb_division d"
        + " where rd.nmRefType=42 and rd.nmDivision = d.nmDivision"
        + " and rd.nmRefId=" + rsD.getString("nmUserId") + " order by stDivisionName");
      rsC.last();
      int iMaxC = rsC.getRow();

      for (int iC = 1; iC <= iMaxC; iC++)
      {
        rsC.absolute(iC);
        if (iC > 1)
          stReturn += "<br>";
        stReturn += rsC.getString("stDivisionName");
      }
    } catch (Exception e)
    {
      stReturn += "<BR>ERROR makeDivision: " + e;
    }
    return stReturn;
  }

  public String makeProjectReqMap(ResultSet rsF, String stValue, ResultSet rsD)
  {
    /*54	varchar	Req mapping task ids
    55	varchar	MappingProjectPercent
    56	varchar	MappingProjectExternalPrj */

    String stReturn = "";
    try
    {
      String stTemp = rsD.getString("nmPercent");
      if (stTemp != null && stTemp.length() > 0)
      {
        ResultSet rsC = this.ebEnt.dbDyn.ExecuteSql("select p.ProjectName,pTo.ProjectName as ExternalProjectName,l.*,s.*"
          + " from Schedule s, teb_link l, Projects p, Projects pTo where l.nmLinkFlags=1 and pTo.RecId=l.nmToProject"
          + " and s.nmProjectId=l.nmProjectId and s.nmBaseline=l.nmBaseline and s.SchId=l.nmToId"
          + " and p.RecId=s.nmProjectId and p.CurrentBaseline=s.nmBaseline and l.nmFromId =" + rsD.getString("ReqId")
          + " order by p.ProjectName,s.SchId");
        rsC.last();
        int iMaxC = rsC.getRow();

        for (int iC = 1; iC <= iMaxC; iC++)
        {
          rsC.absolute(iC);
          if (iC > 1)
            stReturn += "<br>";
          switch (rsF.getInt("nmDataType"))
          {
            case 54:
              stReturn += rsC.getString("SchId");
              break;
            case 55:
              DecimalFormat df = new DecimalFormat("#,###,###,##0.0");
              stReturn += df.format(rsC.getDouble("nmPercent")) + " %";
              break;
            case 56:
              if (!rsC.getString("ExternalProjectName").equals(rsC.getString("ProjectName")))
                stReturn += rsC.getString("ExternalProjectName");
              break;
            default:
              stReturn += "ERROR makeProjectReqMap dt: " + rsF.getInt("nmDataType");
          }
        }
      }
    } catch (Exception e)
    {
      stReturn += "<BR>ERROR makeProjectReqMap: " + e;
    }
    return stReturn;
  }
}
