/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
var giTabId = -1;
var giFieldMax = -1;
var NS4 = (navigator.appName == "Netscape" && parseInt(navigator.appVersion) < 5);
var gstD22 = "";
var dateFormat = 0; // US: dd/mm/yyyy
var dependencyCount = 0;

window.onbeforeunload = confirmExit;
function confirmExit()
{
  //   return "WARNING: giNrValidation2" + giNrValidation2 + " giSubmitId= " + giSubmitId + " giUser = " + giUser;
  if (giNrValidation2 > 0 && giSubmitId == 0 && giUser > 0 )
  {
    return "WARNING: You are currently editing and have not clicked on 'Save' or 'Cancel' button. If you leave this page, all edits in this session will be lost.";
  }
  else
    return;
}

function EpsLoad( )
{
  if ( stPopupMessage != null && stPopupMessage != "" )
  {
    var aV = stPopupMessage.split("|");
    if ( aV[0] == "alert" )
      alert( aV[1].replace(/~/gi,"\n") );
  }
  var oPage = getFieldValue2("id3");
  if ( oPage != null && oPage.length > 0  && parseInt(oPage) > 4  )
  {
    window.location.hash="page"+oPage;
  }
  if ( giNrValidation2 > 0 )
  {
    for( iField=0; iField < giNrValidation2 ; iField++ )
    {
      if ( gaValidation[iField] != null )
      {
        if ( gaValidation[iField][7] != null && gaValidation[iField][7] == "autoclick"  )
        {
          var oValue = getObject("f"+gaValidation[iField][0]);
          if ( oValue != null )
          {
            oValue.click();
          }
        }else
        if ( gaValidation[iField][3] == 40 ) // Special Days
        {
          specialDays(iField);
        }
      }
    }
  }
  var stValue = getFieldValue2( "id3" ); // Set focus to User Name on Login Page
  if ( stValue != null && parseInt(stValue) == 3 )
  {
    var o = getObject( "f5" );
    if ( o != null )
      o.focus();
  }else
  {
    var o2 = null;
    for( var i=0 ; i < giNrValidation2 ; i++ )
    {
      o2 = getObject("f" + gaValidation[i][0] );
      if ( o2 != null && ( gaValidation[i][4] & 1 ) == 1 ) // Cannot be hidden
      {
        o2.focus();
        break;
      }
    }
  }
}

var dialogDisplayField;
var dialogIdField;
var dialogMulti;

var dialogF0;
var dialogF1;
var dialogF2;
var dialogF3;
var dialogF4;
var dialogF5;
var dialogF6;

function getPopupValue (DisplayField,IdField,Multi, stList)
{
  dialogDisplayField = DisplayField;
  dialogIdField = IdField;
  dialogMulti = Multi;
  
  open ("./?stAction=admin&t=9&do=users&h=n&list="+stList, 'popup', 'width=1100,height=600,scrollbars=1');
  return false;
}

function getPopupValue3 (DisplayField,IdField,Multi, stList, stLc)
{
  dialogDisplayField = DisplayField;
  dialogIdField = IdField;
  dialogMulti = Multi;
  var stTemp = getSelectedValue(stLc);
  open ("./?stAction=admin&t=9&do=users&h=n&list="+stList+"&lc="+stTemp, 'popup', 'width=1100,height=600,scrollbars=1');
  return false;
}

function getPopupValue2 (nmFieldId,id,fAll,f0,f1,f2,f3,f4,f5,f6,stPk)
{
  dialogMulti = nmFieldId;
  if( nmFieldId == 492 )
  {
    dialogDisplayField = fAll;
    dialogIdField=id;
    dialogF0 = f0;
    dialogF1 = f1;
    dialogF2 = f2;
    dialogF3 = f3;
    dialogF4 = f4;
    dialogF5 = f5;
    dialogF6 = f6;
    /*for( iField=0; iField < giNrValidation2 ; iField++ )
    {
      if ( gaValidation[iField] != null )
      {
        if ( gaValidation[iField][3] == 40 ) // Special Days
        {
          specialDays(iField);
        }
      }
    }*/
    open ('./?stAction=admin&t=9&do=specialday&h=n&pk='+stPk, 'popup', 'width=600,height=550,scrollbars=1');
  }
  return false;
}

/* Start of Issue AS -- 12Oct2011 -- Issue #41  */

function getPopupValue3 (nmFieldId,id,fAll,f0,f1,f2,f3,f4,f5,f6,stPk)
{
  dialogMulti = nmFieldId;
  if( nmFieldId == 492 )
  {
    dialogDisplayField = fAll;
    dialogIdField=id;
    dialogF0 = f0;
    dialogF1 = f1;
    dialogF2 = f2;
    dialogF3 = f3;
    dialogF4 = f4;
    dialogF5 = f5;
    dialogF6 = f6;
    /*for( iField=0; iField < giNrValidation2 ; iField++ )
    {
      if ( gaValidation[iField] != null )
      {
        if ( gaValidation[iField][3] == 40 ) // Special Days
        {
          specialDays(iField);
        }
      }
    }*/
    open ('./?stAction=admin&t=9&do=specialdayfd&h=n&pk='+stPk, 'popup', 'width=600,height=550,scrollbars=1');
  }
  return false;
}

/* End of Issue AS -- 12Oct2011 -- Issue #41  */

//<input type=submit name=userslect3813 value=Select style='font-size:7pt;'
//onClick='sendBack("Christian** Abary", 3813 );'>
function specialDay( stDay )
{
  var stValue =  window.opener.dialogIdField + "~"+ getTextValue( "stType" );
  stValue += "~" + getTextValue( "stComment" )+"~"+stDay+ "~Pending";
  //alert( " specialDay= " + stValue );
  if ( window.opener.dialogMulti == 492 )
  {
    window.opener.dialogF1.value = getTextValue( "stType" );
    window.opener.dialogF2.value = getTextValue( "stComment" );
    window.opener.dialogF3.value = stDay;
    window.opener.dialogF4.value = "Pending";
    if ( window.opener.dialogDisplayField.value.toString().length > 0 )
      window.opener.dialogDisplayField.value += "\n|";
    window.opener.dialogDisplayField.value += stValue;
  }
  window.close();
}

/* Start of Issue AS -- 12Oct2011 -- Issue # 41 */
function getDateObject(dateString,dateSeperator)
{
//This function return a date object after accepting
//a date string ans dateseparator as arguments
var curValue=dateString;
var sepChar=dateSeperator;
var curPos=0;
var cDate,cMonth,cYear;

//extract day portion
curPos=dateString.indexOf(sepChar);
cDate=dateString.substring(0,curPos);

//extract month portion
endPos=dateString.indexOf(sepChar,curPos+1); cMonth=dateString.substring(curPos+1,endPos);

//extract year portion
curPos=endPos;
endPos=curPos+5;
cYear=curValue.substring(curPos+1,endPos);

//Create Date Object
dtObject=new Date(cYear,cDate,cMonth);
return dtObject;
}
/* End of Issue AS -- 12Oct2011 -- Issue # 41 */

/* Start of Issue AS -- 12Oct2011 -- Issue # 41 */
function specialDay1( stDay )
{
  var stValue =  window.opener.dialogIdField + "~"+ getTextValue( "stType" );
  stValue += "~" + getTextValue( "stComment" )+"~"+stDay+ "~Pending";
  //alert( " specialDay= " + stValue );
  if ( window.opener.dialogMulti == 492 )
  {
	  var startdate = getDateObject(window.opener.dialogF3.value,"/");
	  var finishdate = getDateObject(stDay,"/");
	  if(window.opener.dialogF3.value == null || window.opener.dialogF3.value == "")
	  {
		alert("Please select a start date");
		return;
	  }
	  if(startdate > finishdate)
	  {
		alert ("Please select a date atleast as great as your start date");
		return;
      }
    window.opener.dialogF4.value = stDay;
    //window.opener.dialogF5.value = "Pending";
    if ( window.opener.dialogDisplayField.value.toString().length > 0 )
      window.opener.dialogDisplayField.value += "\n|";
    window.opener.dialogDisplayField.value += stValue;
  }
  window.close();
}
/* End of Issue AS -- 12Oct2011 -- Issue # 41 */

function getTextValue( stField )
{
  var oValue = getObject(stField); // document.getElementById(stField);
  if ( oValue != null )
  {
    if( oValue.value != null )
    {
      return oValue.value;
    }
  }
  return "";
}

function sendBack( stValue, stId )
{
  if ( window.opener.dialogMulti > 0  )
  {
    if ( gstMultiSel != null )
    {
      stValue = "";
      stId = "";
      elems = gstMultiSel.split("~");
      for( iElem=0 ; elems != null && iElem < elems.length ; iElem++ )
      {
        var o = getObject( elems[iElem]);
        if ( o != null )
        {
          var selLength = o.length;
          var i;
          for(i=0; i < selLength ; i++)
          {
            o.options[i].selected = true;
            if ( stValue.length > 0 )
            {
              stValue += "\n";
              stId += ",";
            }
            stValue += o.options[i].text;
            stId += o.options[i].value;
          }
        }
      }
    }
    window.opener.dialogDisplayField.value = "";
    window.opener.dialogDisplayField.value = stValue;
  }
  else
  {
    window.opener.dialogDisplayField.value = stValue;
  }
  window.opener.dialogIdField.value = stId;
  window.close();
}

function deleteFields(DisplayField,IdField)
{
  DisplayField.value = "";
  IdField.value = "";
  return false;
}

function copyToClipboard(text)
{   
  if(window.clipboardData)
  {
    window.clipboardData.setData('text',text);
  }
  else
  {
    // too complicated for now.
    var clipboarddiv=document.getElementById('divclipboardswf');
    if(clipboarddiv==null)
    {
      clipboarddiv=document.createElement('div');
      clipboarddiv.setAttribute("name", "divclipboardswf");
      clipboarddiv.setAttribute("id", "divclipboardswf");
      document.body.appendChild(clipboarddiv);
    }
    clipboarddiv.innerHTML='<embed src="clipboard.swf" FlashVars="clipboard='+
    encodeURIComponent(text)+'" width="0" height="0" type="application/x-shockwave-flash"></embed>';
  }
  return false;
}

function ebCopy( stName )
{
  var item = getObject( stName );
  item.select();
  copyToClipboard( item.value );
}
function highlightmetasearch()
{
  document.post.message.select();
  document.FORMNAME.TEXTAREANAME.focus();
}
function copymetasearch()
{
  highlightmetasearch();

  textRange = document.post.message.createTextRange();
  textRange.execCommand("RemoveFormat");
  textRange.execCommand("Copy");
  alert("This post has been copied to your clipboard.");
}
// -->
var giMonth=1;
var giDay=4;
var giMax=31;
var giYear=2010;
var gaValues = null;
var giOffset = 0;

function DrawEpsCalender()
{
  var stValue;

  stValue = getFieldValue2( "f193");
  var gaValues2 = stValue.split("~");
  gaValues = new Array(gaValues2.length);
  for ( var i=0 ; i < gaValues2.length ; i++ )
  {
    gaValues[i] = parseInt(gaValues2[i]);
  }
  return DrawCalendarYear( 2010 );
}
function DrawCalendarYear2( year2  )
{
  var iIndex = year2.selectedIndex;
  var stValue = year2[iIndex].value;
  var iYear = parseInt( stValue );
  SetTextValue( "zz193", DrawCalendarYear( iYear ) );
  return true;
}
function DrawCalendarYear( iYearSelect )
{
  var stYears = "";
  for ( var i=2 ; i < gaValues.length ; i += 14 )
  {
    iYear = ( gaValues[i] & 0x0000FFFF) ;
    iDay = ( ( gaValues[i] >> 27) & 0x0000001F);
    if ( iYear == iYearSelect )
    {
      giOffset = i;
      giYear = iYear;
      giDay = ( iDay+1);
      if ( giDay > 6 )
        giDay=0;
      stSelected = " selected ";
    }else
      stSelected = "";
    stYears += "\n<option value="+iYear+" "+stSelected+">"+iYear+"</option>";
  }
  var stReturn = "<table border=10 class=table1><tr><th colspan=3 align=center>Year: " + giYear + "</th><th><select name='year2' onChange=\"DrawCalendarYear2(this.form.year2);\">"+stYears+"</select></th></tr><tr>";
  stReturn += "<td valign=top>" +DrawCalendar( giYear, 1 ) + "</td>";
  stReturn += "<td valign=top>" +DrawCalendar( giYear, 2 ) + "</td>";
  stReturn += "<td valign=top>" +DrawCalendar( giYear, 3 ) + "</td>";
  stReturn += "<td valign=top>" +DrawCalendar( giYear, 4 ) + "</td>";
  stReturn += "</tr><tr>";
  stReturn += "<td valign=top>" +DrawCalendar( giYear, 5 ) + "</td>";
  stReturn += "<td valign=top>" +DrawCalendar( giYear, 6 ) + "</td>";
  stReturn += "<td valign=top>" +DrawCalendar( giYear, 7 ) + "</td>";
  stReturn += "<td valign=top>" +DrawCalendar( giYear, 8 ) + "</td>";
  stReturn += "</tr><tr>";
  stReturn += "<td valign=top>" +DrawCalendar( giYear, 9 ) + "</td>";
  stReturn += "<td valign=top>" +DrawCalendar( giYear, 10 ) + "</td>";
  stReturn += "<td valign=top>" +DrawCalendar( giYear, 11 ) + "</td>";
  stReturn += "<td valign=top>" +DrawCalendar( giYear, 12 ) + "</td>";
  stReturn += "</tr></table>";
  return stReturn;
}
function flipDay(iYear, iMonth, iDay )
{
  var oObj = getObject("td_"+iYear+"_"+iMonth+"_"+iDay );
  var iMask = gaValues[ giOffset + 13 - iMonth ];
  if ( oObj != null )
  {
    if ( iMask < 0 )
      iMask = 0xFFFFFFFF + iMask + 1;
    if ( ( (iMask >> (32 - iDay)) & 1) == 0 )
    {
      stClass = "workday";
      iMask |= 1 << (32 - iDay);
    }else
    {
      stClass = "noworkday";
      iMask &= ~(1 << (32 - iDay));
    }
    gaValues[ giOffset + 13 - iMonth ] = iMask; // Save it;
    if ( oObj.className != null )
      oObj.className  = stClass;
    oObj.setAttribute("class", stClass);
  }else
  {
    alert("JS ERROR: cannot set Mask");
  }
  return true;
}
function DrawCalendar( iYear, iMonth )
{
  // Joel: SMTWTFS
  //       6012345
  // Rob:  0123456
  switch( iMonth )
  {
    case 1:
      stMonth = "January";
      giMax=31;
      break;
    case 2:
      stMonth = "February";
      giMax=28;
      if ( (iYear%4) == 0 )
        giMax++;
      break;
    case 3:
      stMonth = "March";
      giMax=31;
      break;
    case 4:
      stMonth = "April";
      giMax=30;
      break;
    case 5:
      stMonth = "May";
      giMax=31;
      break;
    case 6:
      stMonth = "June";
      giMax=30;
      break;
    case 7:
      stMonth = "July";
      giMax=31;
      break;
    case 8:
      stMonth = "August";
      giMax=31;
      break;
    case 9:
      stMonth = "September";
      giMax=30;
      break;
    case 10:
      stMonth = "October";
      giMax=31;
      break;
    case 11:
      stMonth = "November";
      giMax=30;
      break;
    case 12:
      stMonth = "December";
      giMax=31;
      break;
  }
  var iMask = gaValues[ giOffset + 13 - iMonth ];
  if ( iMask < 0 )
    iMask = 0xFFFFFFFF + iMask + 1;
  var stReturn = "<table border=1 class=table1><tr><th colspan=7>"+stMonth+"</th></tr><tr><th>Su</th><th>Mo</th><th>Tu</th><th>We</th><th>Th</th><th>Fr</th><th>Sa</th>";
  for( var i=0 ; i < giDay ; i++ )
  {
    if ( (i%7) == 0 )
      stReturn += "</tr><tr>";
    stReturn += "<td align=center>&nbsp;</td>";
  }
  for( iDay=1 ; iDay <= giMax ; i++, iDay++ )
  {
    if ( (i%7) == 0 )
      stReturn += "</tr><tr>";
    if ( ( (iMask >> (31-(iDay-1))) & 1) == 0 )
      stClass = " class=noworkday ";
    else
      stClass = " class=workday  ";

    stReturn += "<td align=center id='td_"+iYear+"_"+iMonth+"_"+iDay+"' "+stClass+"><a href='#' onClick='flipDay("+iYear+","+iMonth+","+iDay+");'>" + iDay + "</a></td>";
  }
  while ( (i%7) != 0 )
  {
    if ( (i%7) == 0 )
    {
      stReturn += "</tr>";
      break;
    }else
    {
      stReturn += "<td align=center>&nbsp;</td>";
    }
    i++;
  }
  stReturn += "</table>";
  giDay = (giDay + giMax) % 7;

  return stReturn;
}

function EbLoad( )
{
  if ( giFieldMax > 2 )
  {
    for( iField=1; iField < giFieldMax ; iField++ )
    {
      if ( gaData[iField] != null && gaF[iField] != null  )
      {
        if ( gaF[iField][2] != 9 )
        {
          SetTextValue( "f" + iField , gaData[iField] );
        }else
        {
          var v;
          v = gaF[iField][3].split("|");
          Select_Value_Set( iField , gaData[iField] , 0, v[2] );
        }
      }
    }
    tabHandler(1);
  }
  return;
}
function refresh()
{
  window.location.reload( false );
}
function ValidateNum( iField, stFieldName )
{
  var thisCtrl = getObject( "f" + iField );
  try
  {
    var inpVal = parseInt(thisCtrl.value, 10);
    if (isNaN(inpVal))
    {
      thisCtrl.value = "0";
      alert( " Please enter numbers only for field: ");
      alert( " Please enter numbers only for field: " + stFieldName);
      thisCtrl.focus();
      thisCtrl.select();
      return false;
    }
  } catch (e)
{
    alert(e.message);
    thisCtrl.focus();
    thisCtrl.select();
  }
  return true;
}
function myInput( iField )
{
  alert( gaF[iField][1]  + " myInput " + iField);
  if ((gaF[iField][1] & 0x20) != 0)
  {
    Select_Value_Set( iField , "", 0,0 );
  }

  if ( giTabId >= 0 && ( gaF[iField][1] & (0x40 | 0x200)) != 0)
  {
    tabHandler( giTabId );
  }
}
function myInput2( iField, iC, iMax )
{
  alert( gaF[iField][1]  + " myInput " + iField);
  //if ((gaF[iField][1] & 0x20) != 0)
  {
    Select_Value_Set( iField , "", iC, iMax );
  }

  if ( giTabId >= 0 && ( gaF[iField][1] & (0x40 | 0x200)) != 0)
  {
    tabHandler( giTabId );
  }
}
function getFieldValue( iField )
{
  var stValue = "";
  var oObj = getObject( "f" + iField );
  if ( oObj != null && oObj.value != null )
    stValue = oObj.value;
  return stValue;
}
function getFieldValue2( stField )
{
  var stValue = "";
  var oObj = getObject(stField );
  if ( oObj != null && oObj.value != null )
    stValue = oObj.value;
  return stValue;
}
function tabHandler( iTabId )
{
  giTabId = iTabId;
  for( i=0 ; i <= giTabMax ; i++ )
  {
    oObj = getObject( "tab" + i );
    if ( oObj != null )
      oObj.className = "notselected";
  }
  oObj = getObject( "tab" + iTabId );
  if ( oObj != null )
    oObj.className = "selected";

  for( iField=1; iField < giFieldMax ; iField++ )
  {
    if ( gaF[iField] != null && gaF[iField][4] != null )
    {
      if ( ( gaF[iField][4] == iTabId || iTabId == 0 ) && CheckRule( iField ) == true )
      {
        showObject("row" + iField);
        showObject("rowb" + iField);
        showObject("rowc" + iField);
      }else
      {
        hideObject("row" + iField);
        hideObject("rowb" + iField);
        hideObject("rowc" + iField);
      }
    }
  }
  return false;
}
function CheckRule( iField )
{
  var bShow = true;
  var stValue = "";
  var iRule=0;
  //if ( ( gaF[iField][1] & 33554432 ) != 0 ) ... need this on onChange ... to redo tabHandler() !! TODO
  if ( gaF[iField][6] > 0 )
  {
    iRule = gaF[iField][6];
    //alert( iField + " iField zz CONDITION INDEX " + iRule  + " " + gaF[iField][5] );
    stValue = "|" + getFieldValue( gaG[iRule][2] ) + "|";
    //if (  gaG[iRule][2] == 3 )
    //  alert( " MATCH COND FIELD: "+gaG[iRule][2]+ " VALUE=" + stValue + " RULE="+gaG[iRule][1]+" CONDITION=" + gaG[iRule][3]);

    switch( gaG[iRule][1] )
    {
      case 0: // "SHOW ALWAYS";
        bShow = true;
        break;

      case 1: // "HIDE ALWAYS";
        bShow = false;
        break;

      case 2: // "SHOW if &gt; ";
        if ( gaG[iRule][3] > stValue )
        {
          bShow = true;
        }
        else

        {
          bShow = false;
        }
        break;

      case 3: // "SHOW if &gt= ";
        if ( gaG[iRule][3] >= stValue )
        {
          bShow = true;
        }
        else

        {
          bShow = false;
        }
        break;

      case 4: // "SHOW if = ";
        if ( gaG[iRule][3] == stValue )
        {
          bShow = true;
        }else
        {
          bShow = false;
        }
        break;

      case 16: // "SHOW IF IN a|b|c";
        if ( gaG[iRule][3].indexOf( stValue ) >= 0 )
        {
          bShow = true;
        }else
        {
          bShow = false;
        }
        break;

      case 17: // "HIDE IF IN a|b|c";
        if ( gaG[iRule][3].indexOf( stValue ) >= 0 )
        {
          bShow = false;
        }else
        {
          bShow = true;
        }
        break;

      default:
        alert( "CONDITION NOT IMPLEMENTED: " + gaG[iRule][1] );
        break;
    }
  }
  return bShow;
}
function Select_Value_Set(iField, Value, iC, iMax)
{
  var SelectName = "f" + iField;

  if( (gaF[iField][1] & 0x20 ) == 0 )
  {
    if ( Value > 0 )
    {
      oValue = getObject(SelectName);
      if ( oValue != null )
      {
        for(index = 0; index < oValue.length; index++)
        {
          if(oValue[index].value == Value)
          {
            oValue.selectedIndex = index;
            break;
          }
        }
      }
    }
  }
  else

  {
    var v;
    if ( Value.indexOf("|") >= 0 )
    {
      v = Value.split("|");
    }else
    {
      v = new Array(1);
      v[0] = Value;
    }
    for( var i=0; i < v.length ; i++ )
    {
      for( var ii=1 ; ii <= iMax ; ii++ )
      {
        var stName = SelectName + "_" + (ii) + "_" + iMax;
        if ( parseInt(v[i]) > 0 )
        {
          oValue = getObject( stName );
          if ( oValue != null )
          {
            if ( "" + v[i] == "" + oValue.value  )
            {
              oValue.checked = true;
            }
          }
        }
      }
    }
  }
  return;
}

function getObject (id)
{
  if (document.getElementById)
  {
    return document.getElementById(id);
  }
  else if (document.all)
  {
    return window.document.all[id];
  }
  else if (document.layers)
  {
    return window.document.layers[id];
  }else
    return null;
}

function SetTextValue( stField, value )
{
  var oValue = getObject(stField); // document.getElementById(stField);
  if ( oValue != null )
  {
    if( oValue.value != null )
    {
      oValue.value = value;
    }
    if( oValue.innerText != null )
    {
      oValue.innerText = value;
    }
    // RE: 5/14/2011 was commented out. dont remember why. But we need for Special Days.
    if( oValue.innerHTML != null )
    {
      oValue.innerHTML = value;
    }
  }
}
function SetTextValue2( stField, value )
{
  var oValue = getObject(stField); // document.getElementById(stField);
  if ( oValue != null )
  {
    if( oValue.value != null )
    {
      oValue.value = value;
    }
  }
}
function getDivValue( stField )
{
  var oValue = document.getElementById(stField);
  if ( oValue == null )
    oValue = document.all[stField];
  if ( oValue != null )
    stValue = oValue.innerText;
  else
    stValue = "??error-3??";
  return stValue;
}

function hideObject(objID)
{
  var obj = getObject(objID);
  var rtn = false;

  if (obj != null)
  {
    obj.style.display = "none";
    rtn = true;
  }
  return rtn;
}
function showObject(objID)
{
  var obj = getObject(objID);
  var rtn = false;
  if (obj != null)
  {
    obj.style.display   = "";
    rtn = true;
  }
  return rtn;
}

function AreYouSure(stLabel)
{
  var result = confirm("Are you sure \n" + stLabel + "?");

  if ( result )
  {
    oValue = document.getElementById("stUpdatedFields");
    if ( oValue == null )
      oValue = document.all["stUpdatedFields"];
    if ( oValue != null )
      oValue.value = "deleteme";

    document.form3.submit();
  }
  return result;
}

function removeMSWordChars(str)
{
  var myReplacements = new Array();
  var myCode, intReplacement;
  myReplacements[8216] = 39;
  myReplacements[8217] = 39;
  myReplacements[8220] = 34;
  myReplacements[8221] = 34;
  myReplacements[8212] = 45;

  myReplacements[8230] = 36;
  myReplacements[8211] = 45;
  myReplacements[8227] = 45;

  myReplacements[0x96] = 39;
  myReplacements[0x97] = 39;
  myReplacements[0x9B] = 39;
  myReplacements[61672] = 35; // weird -> OS
  myReplacements[61684] = 42; // *

  for(c=0; c<str.length; c++)
  {
    var myCode = str.charCodeAt(c);
    //if ( c < 5 )
    //  alert(" c: " + c + " value: " + myCode );
    if(myReplacements[myCode] != undefined)
    {
      intReplacement = myReplacements[myCode];
      str = str.substr(0,c) + String.fromCharCode(intReplacement) + str.substr(c+1);
    }
  }
  return str;
}
function myValidation(form1)
{
  var bReturn = true;
  var iElem;
  var elems = null;

  if ( gstMultiSel != null )
  {
    elems = gstMultiSel.split("~");
    for( iElem=0 ; elems != null && iElem < elems.length ; iElem++ )
    {
      var o = getObject( elems[iElem]);
      if ( o != null )
      {
        var selLength = o.length;
        var i;
        for(i=selLength-1; i>=0; i--)
        {
          o.options[i].selected = true;
        }
      }
    }
  }
  if ( giSubmitId == 8888 ) // Cancel
    return true; //----------------------------------------------------------------------->

  if ( giSubmitId == 9990 || giSubmitId == 9970 )
  {
    // Labor Category
    var iMax = getTextValue("imax");
    var iC=0;
    var stTemp = "";
    if( iMax > 0 )
    {
      for( iC=0 ; iC < iMax ; iC++ )
      {
        stTemp =  getSelectedValue("lc_" + iC );
        if ( stTemp > 0 )
        {
          //thisCtrl = eval("form1.est_" + iC);
          stTemp =  getTextValue("est_" + iC );
          if ( stTemp == null || stTemp.length <= 0 )
          {
            alert( "\nEstimated Effort cannot be empty" );
            return false;
          }
          if ( ! validNum(stTemp) )
          {
            alert( "\nEstimated Effort must be number." );
            return false;
          }
          if ( stTemp <= 0 )
          {
            alert( "\nEstimated Effort must be greater than 0 " );
            return false;
          }
        }
      }
    }
  }
  
  
  //task level x can only be in range of 0-x+1
  if (giSubmitId == 9970 && (form1.f271.value > (parseInt(form1.stLvl.value))+1 || form1.f271.value < 0)){
	  alert( "\nIllegal Level" );
	  form1.f271.value = form1.stLvl.value;
      return false;
  }
  
  // packcalendar
  // new Array(201,"Update_17_36","Update",36,1,0,64,"packcalendar","" ),
  for( iField=0; iField < giNrValidation2 ; iField++ )
  {
    if ( gaValidation[iField] != null && gaValidation[iField][0] == giSubmitId )
    {
      if (  gaValidation[iField][7] != null && gaValidation[iField][7] == "packcalendar"  )
      {
        var stValue = "";
        for ( i=0 ; i < gaValues.length ; i++ )
        {
          if ( i > 0 )
            stValue += "~";
          stValue += gaValues[i];
        }
        SetTextValue( "f193", stValue );
      }
      break;
    }
  }

  
  var stAlert = "";
  var thisCtrl;
  for (i = 0; i < giNrValidation2 ; i++)
  {
    /* gaValidation[i][]:
     * [0] = FID
     * [1] = stFieldName
     * [2] = stFieldLabel
     * [3] = nmDataType
     * [4] = nmFlags
     * [5] = nmMin
     * [6] = nmMax
     * [7] = validation method
     * [8] = stJaw
 new Array(5,"User_Name_3_3","E-Mail",3,1,0,25,"*|min","" )*/
    //if ( i > 14 )
    //alert( " i = " + i + " field: " + gaValidation[i][2]  + " dt: "+gaValidation[i][3]+" flags: "+gaValidation[i][4]+"  " );
    if ( gaValidation[i][7] == "epsweekly" )
    {
      var iTemp = 0;
      thisCtrl = eval("form1.f" + gaValidation[i][0]+"_mon");
      iTemp = parseInt(thisCtrl.value);
      if ( iTemp < 0 || iTemp > 24 )
      {
        stAlert += "\n" +gaValidation[i][2] + ": Monday must beween 0 and 24";
        bReturn = false;
      }
      thisCtrl = eval("form1.f" + gaValidation[i][0]+"_tue");
      iTemp = parseInt(thisCtrl.value);
      if ( iTemp < 0 || iTemp > 24 )
      {
        stAlert += "\n" +gaValidation[i][2] + ": Tuesday must beween 0 and 24";
        bReturn = false;
      }
      thisCtrl = eval("form1.f" + gaValidation[i][0]+"_wed");
      iTemp = parseInt(thisCtrl.value);
      if ( iTemp < 0 || iTemp > 24 )
      {
        stAlert += "\n" +gaValidation[i][2] + ": Wednesday must beween 0 and 24";
        bReturn = false;
      }
      thisCtrl = eval("form1.f" + gaValidation[i][0]+"_thu");
      iTemp = parseInt(thisCtrl.value);
      if ( iTemp < 0 || iTemp > 24 )
      {
        stAlert += "\n" +gaValidation[i][2] + ": Thursday must beween 0 and 24";
        bReturn = false;
      }
      thisCtrl = eval("form1.f" + gaValidation[i][0]+"_fri");
      iTemp = parseInt(thisCtrl.value);
      if ( iTemp < 0 || iTemp > 24 )
      {
        stAlert += "\n" +gaValidation[i][2] + ": Fri must beween 0 and 24";
        bReturn = false;
      }
      thisCtrl = eval("form1.f" + gaValidation[i][0]+"_sat");
      iTemp = parseInt(thisCtrl.value);
      if ( iTemp < 0 || iTemp > 24 )
      {
        stAlert += "\n" +gaValidation[i][2] + ": Saturday must beween 0 and 24";
        bReturn = false;
      }
      thisCtrl = eval("form1.f" + gaValidation[i][0]+"_sun");
      iTemp = parseInt(thisCtrl.value);
      if ( iTemp < 0 || iTemp > 24 )
      {
        stAlert += "\n" +gaValidation[i][2] + ": Sunday must beween 0 and 24";
        bReturn = false;
      }
    }else
    {
      thisCtrl = eval("form1.f" + gaValidation[i][0]);
      //alert( " i = " + i + " field: " + gaValidation[i][2]  + " dt: "+gaValidation[i][3]+" flags: "+gaValidation[i][4]+" len " + thisCtrl.value.length );

      switch( gaValidation[i][3] )
      {
        case 40:
        case 39:
          break;
        
        case 37:
          /* having trouble getting the value
        alert( " 37 value: " + thisCtrl.value );
        var o2 = getObject( "f"+gaValidation[i][0] );
        alert( " o2: " + getCheckedValue(o2) );

        alert( " button value: " + getTextValue( "f"+gaValidation[i][0] ));
         */
          break;
        
        case 5: // money
        case 31: // number
        case 1:  // int
        case 2:  // Y/N
          if ( thisCtrl == null || thisCtrl.value == null )
          {
            if ( gaValidation[i][3] == 2 && thisCtrl != null)
            {
              for (ii=0; ii< thisCtrl.length && ii < 1000; ii++)
              {
                if (thisCtrl[ii].checked==true)
                {
                  if ( ! validNum(thisCtrl[ii].value) )
                  {
                    stAlert += "\n" +gaValidation[ii][2] + " must be a number";
                    bReturn = false;
                  }
                  break;
                }
              }
            }
          }else
          {
            if  (  thisCtrl.value == null || thisCtrl.value.length <= 0 )
            {
              if ( (gaValidation[i][4] & 0x2 ) != 0 )
              {
                stAlert += "\n" +gaValidation[i][2] + " cannot be blank.";
                bReturn = false;
              }
            }else
            {
              if ( thisCtrl.value.substring( 0, 1 ) == "." )
                thisCtrl.value = "0" + thisCtrl.value;
              if ( ! validNum(thisCtrl.value.replace(",","")) )
              {
                stAlert += "\n" +gaValidation[i][2] + " must be a number";
                bReturn = false;
              }
            }
            if ( bReturn == true && gaValidation[i][7] == "rid" )
            {
              if ( thisCtrl.value <= 0 )
              {
                bReturn = false;
                stAlert += "\n" +gaValidation[i][2] + " must be a valid ID";
              }
            }
            if ( bReturn == true && gaValidation[i][7] == "gt0" )
            {
              if ( thisCtrl.value <= 0 )
              {
                bReturn = false;
                stAlert += "\n" +gaValidation[i][2] + " must be greater than 0";
              }
            }
            if ( bReturn == true && gaValidation[i][7] == "range" )
            {
              var elems1 = gaValidation[i][8].split(",");
              if ( thisCtrl.value < elems1[0] )
              {
                bReturn = false;
                stAlert += "\n" +gaValidation[i][2] + " must be greater than or equals " + elems1[0];
              }           
              if ( thisCtrl.value > elems1[1] )
              {
                bReturn = false;
                stAlert += "\n" +gaValidation[i][2] + " must be less than or equals " + elems1[1];
              }
            }
          }
          break;

        case 3:   // Short Text
        case 4:   // Long Text
        case 32:  // Long Text
          if  ( (gaValidation[i][4] & 0x2 ) != 0 && thisCtrl.value.length <= 0 ) // Mandatory
          {
            stAlert += "\n" +gaValidation[i][2] + " cannot be blank.";
            bReturn = false;
          }else if ( gaValidation[i][5] > 0 ) // have MIN
          {
            if ( thisCtrl.value.length < gaValidation[i][5] )
            {
              stAlert += "\n" +gaValidation[i][2] + " must have at least "+gaValidation[i][5]+" characters.";
              bReturn = false;
            }
          }
          if ( gaValidation[i][6] > 0 && gaValidation[i][3] == 3 ) // have Max
          {
            if ( thisCtrl.value.length > gaValidation[i][6] )
            {
              stAlert += "\n" +gaValidation[i][2] + " max. "+gaValidation[i][6]+" characters.  You entered: " + thisCtrl.value.length;
              bReturn = false;
            }
          }
          if ( bReturn == true && gaValidation[i][7] == "d22" )
          {
            bReturn = validateD22( thisCtrl.value )
            if ( bReturn == false )
            {
              stAlert += "\n" +gaValidation[i][2] + " " + gstD22;
            }
          }
          break;

        case 8:   // Date
          if  ( (gaValidation[i][4] & 0x2 ) != 0 && thisCtrl.value.length <= 0 ) // Mandatory
          {
            stAlert += "\n" +gaValidation[i][2] + " cannot be blank.";
            bReturn = false;
          }else if ( ! validDateTime(thisCtrl.value) )
          {
            switch(dateFormat)
            {
              case 0:
              default:
                stAlert += "\nValidation check for DATE FIELD " +gaValidation[i][2] + " (mm/dd/yyyy) failed";
                break;

              case 1:
                stAlert += "\nValidation check for DATE FIELD " +gaValidation[i][2] + " (mm/dd/yyyy) failed";
                break;

              case 2:
                stAlert += "\nValidation check for DATE FIELD " +gaValidation[i][2] + " (yyyy/mmm/dd) failed";
                break;

              case 3:
                stAlert += "\nValidation check for DATE FIELD " +gaValidation[i][2] + " (yyyy mmm dd) failed";
                break;
            }
            bReturn = false;
          }
          break;

        case 20:   // Date Only
          if  ( (gaValidation[i][4] & 0x2 ) != 0 && thisCtrl.value.length <= 0 ) // Mandatory
          {
            stAlert += "\n" +gaValidation[i][2] + " cannot be blank.";
            bReturn = false;
          } else if ( ! validDate(thisCtrl.value) )
{
            switch(dateFormat)
            {
              case 0:
              default:
                stAlert += "\nValidation check for DATE FIELD (mm/dd/yyyy) failed on field:\r\n\r\n"+gaValidation[i][2];
                break;

              case 1:
                stAlert += "\nValidation check for DATE FIELD (mm/dd/yyyy) failed on field:\r\n\r\n"+gaValidation[i][2];
                break;

              case 2:
                stAlert += "\nValidation check for DATE FIELD (yyyy/mmm/dd) failed on field:\r\n\r\n"+gaValidation[i][2];
                break;

              case 3:
                stAlert += "\nValidation check for DATE FIELD (yyyy mmm dd) failed on field:\r\n\r\n"+gaValidation[i][2];
                break;
            }
            bReturn = false;
          }
          break;

        case 21:   // Time Only
          if ( thisCtrl != null && thisCtrl.value != null )
          {
            var elems = thisCtrl.value.split(" ");
            result1 = 0;
            if (elems.length > 0 )
            {
              if (elems.length > 1 )
                result1 = validTime( elems[0], elems[1] );
              else
                result1 = validTime( elems[0], "" );
            }
            if ( ! result1 )
            {
              stAlert += "\nValidation check for TIME FIELD (hh:mm) failed on field:\r\n\r\n"+gaValidation[i][2];
              bReturn = false;
            }
          }
          break;

        case 22:   // DOB
          if ( ! validDate(thisCtrl.value) )
          {
            switch(dateFormat)
            {
              default:
                stAlert += "\nValidation check for DOB FIELD (mm/dd/yyyy) failed\r\n\r\n";
                break;

              case 1:
                stAlert += "\nValidation check for DOB FIELD (mm/dd/yyyy) failed\r\n\r\n";
                break;
              case 2:
                stAlert += "\nValidation check for DOB FIELD (yyyy/mmm/dd) failed\r\n\r\n";
                break;

              case 3:
                stAlert += "\nValidation check for DOB FIELD (yyyy mmm dd) failed\r\n\r\n";
                break;
            }
            bReturn = false;
          }
          break;

        case 41:
        case 42:
        case 43:
        case 44:
        case 45:
        case 46:
        case 47:
        case 36:
        case 24:  // AutoInc
        case 0:
        case 9: // TODO drop down check .
          break;

        default:
          stAlert += "\nValidation check for data type [" + gaValidation[i][3] + "] failed on field:\r\n\r\n"+gaValidation[i][2];
          bReturn = false;
          break;
      } // switch
    }
  }
  if ( stAlert.length > 0 )
    alert( stAlert );

  return bReturn;
}

function setSubmitId( iId )
{
  giSubmitId = iId;
  var oValue = getObject("giSubmitId");
  if (   oValue != null )
    oValue.value = "" + iId;
  return true;
}
function setSubmitId2( iId, iVar )
{
  giSubmitId = iId;
  var oValue = getObject("giSubmitId");
  if (   oValue != null )
    oValue.value = "" + iId;
  
  oValue = getObject("giVar");
  if (   oValue != null )
    oValue.value = "" + iVar;
}
function setSubmitId3( iId, iForm )
{
  giSubmitId = iId;
  var oValue = getObject("giSubmitId");
  if (   oValue != null )
    oValue.value = "" + iId;
  eval("document.form"+iForm+".submit();");
}

/*
 * Bring up progress while user waits
 */
function setSubmitId4( iId )
{
  //reveal loading screen
  document.getElementById("loadingDiv").style.display = "block";
  document.getElementById("loadingDiv").innerHTML = "Analysis in Process";
  document.getElementById("loadingDiv").style.position = "absolute";
  document.getElementById("loadingDiv").style.zIndex = "10";
  document.getElementById("loadingDiv").style.width = "100%";
  //hide fields
  document.getElementById("fieldtb").style.visibility = 'hidden';
  //proceed
  giSubmitId = iId;
  var oValue = getObject("giSubmitId");
  if (   oValue != null )
    oValue.value = "" + iId;
  return true;
}

function setSubmitIdConfirm( iId, stConfirm )
{
  //alert(" aaa " + iId );
  giSubmitId = iId;
  var oValue = getObject("giSubmitId");
  if (   oValue != null )
    oValue.value = "" + iId;
  return confirm(stConfirm);
}
function makePrimary( theSelTo, iId )
{
  var selLength = theSelTo.length;

  var i;
  // Find the selected Options in reverse order
  // and delete them from the 'from' Select.
  for(i=selLength-1; i>=0; i--)
  {
    if(theSelTo.options[i].selected)
    {
      theSelTo.options[i].className = "option2";
      theSelTo.options[i].selected = false;
      SetTextValue( "f" + iId + "_dcvalue", theSelTo.options[i].value );
    }
    else

    {
      theSelTo.options[i].className = "";
    }
  }
  return false;
}

function moveOptions(theSelFrom, theSelTo)
{
  var selLength = theSelFrom.length;
  var selectedText = new Array();
  var selectedValues = new Array();
  var selectedCount = 0;

  var i;
  // Find the selected Options in reverse order
  // and delete them from the 'from' Select.
  for(i=selLength-1; i>=0; i--)
  {
    if(theSelFrom.options[i].selected)
    {
      selectedText[selectedCount] = theSelFrom.options[i].text;
      selectedValues[selectedCount] = theSelFrom.options[i].value;
      deleteOption(theSelFrom, i);
      selectedCount++;
    }
  }

  // Add the selected text/values in reverse order.
  // This will add the Options to the 'to' Select
  // in the same order as they were in the 'from' Select.
  if ( theSelTo != null )
  {
    for(i=selectedCount-1; i>=0; i--)
    {
      addOption(theSelTo, selectedText[i], selectedValues[i]);
    }
  }
  if(NS4) history.go(0);
}

function addOption(theSel, theText, theValue)
{
  var newOpt = new Option(theText, theValue);
  var selLength = theSel.length;
  theSel.options[selLength] = newOpt;
}

function deleteOption(theSel, theIndex)
{
  var selLength = theSel.length;
  if(selLength>0)
  {
    theSel.options[theIndex] = null;
  }
}

function selectAllOptions(theSel){
  var idx;
  for(idx=0; idx<theSel.length; idx++){
	  theSel.options[idx].selected = true;
  }
}

function moveDate( stDate, theSelTo)
{
  var newOpt = new Option(stDate, stDate);
  var item = getObject( theSelTo );
  var selLength = item.length;
  item.options[selLength] = newOpt;
  selLength = item.length;
  var i;
  for(i=selLength-1; i>=0; i--)
  {
    item.options[i].selected = true;
  }
  return false;
}
function selectDate(theSelTo)
{
  var item = getObject( theSelTo );
  var selLength = item.length;
  var i;
  for(i=selLength-1; i>=0; i--)
  {
    item.options[i].selected = true;
  }
  return false;
}
function closeW()
{
  if ( navigator.appName == "Netscape" && parseInt(navigator.appVersion) >= 5 )
  {
    alert( "Mozilla Firefox does not allow exiting from this button. \n\nPlease exit your browser directly. ");
  }
  window.opener = self;
  window.close();
}
//-----------


function validDateTime(value)
{
  var result = true;
  var elems = value.split(" ");
  if (elems.length > 1 )
  {

    if (elems.length > 2 )
      result = validTime( elems[1], elems[2] );
    else
      result = validTime( elems[1], "" );
  }
  if (result)
  {
    if (elems.length > 1 )
      result = validDate( elems[0] );
    else
      result = validDate( value );
  }
  return result;
}
function validDate( value, DobFlag )
{
  var result = true;
  var stmonth = 0;
  var stday = 0;
  var styear = 0;

  var re = new RegExp('[ /.-]');
  var elems = value.split( re );

  if (value == "")
    return true;
  result = (elems.length == 3); 
  if (result)
  {
    switch(dateFormat)
    {
      case 0:
      default:  //Standard mm/dd/yyyy
        stmonth = parseInt(elems[0],10);
        stday = parseInt(elems[1],10);
        styear = parseInt(elems[2],10);
        break;

      case 1:  //Standard mm/dd/yyyy
        stmonth = parseInt(elems[0],10);
        stday = parseInt(elems[1],10);
        styear = parseInt(elems[2],10);
        break;

      case 2: // yyyy/mmm/dd
        styear = parseInt(elems[0],10);
        stmonth = stMonth2Int(elems[1]);
        stday = parseInt(elems[2],10);
        break;

      case 3: // yyyy mmm dd
        styear = parseInt(elems[0],10);
        stmonth = stMonth2Int(elems[1]);
        stday = parseInt(elems[2],10);
        break;
    }
  }
  return validDateHelper( stmonth, stday, styear );
}

function validDateHelper( stmonth, stday, styear )
{
  result = !isNaN(stmonth) && (stmonth > 0) && (stmonth < 13) &&
  !isNaN(stday) && (stday > 0) && (stday < 32) &&
  !isNaN(styear) && ( styear >= 1800 ) && ( styear <= 2500 );
  return result;
}

function validTime(value,ampm)
{
  var result = true;
  var sec;
  var elems = value.split(":");

  result = (elems.length > 1 ); // should be three components
  if ( result == 0 )
  {
    if ( value.length == 3)
    {
      elems[0] = value.substring( 0, 1 );
      elems[1] = value.substring( 1, 3 );
    }else
    if ( value.length == 4)
    {
      elems[0] = value.substring( 0, 2 );
      elems[1] = value.substring( 2, 4 );
    }else
    {
      elems[0] = 99; // Make it fail
      elems[1] = 99;
    }
    result = 1;
  }
  if (result)
  {
    var hour = parseInt(elems[0],10);
    var min = parseInt(elems[1],10);
    if ( elems.length == 3 )
      sec = parseInt(elems[2],10);
    else
      sec = 0;
    if ( ampm != "" && ( ampm == "am" || ampm == "pm" || ampm == "AM" || ampm == "PM" ) )
    {
      if ( (hour >= 0) && (hour <= 12) )
        result=1;
      else
        result=0; 
    }
    else
    if ( ampm != "" )
      result = 0;
    if( result )
      result = !isNaN(hour) && (hour >= 0) && (hour < 24) &&
      !isNaN(min) && (min >= 0) && (min < 60) &&
      !isNaN(sec) && (sec >= 0) && (sec < 60);
  }
  return result;
}
function validNum(value)
{ 
  var result = true;
  var validFormatRegExp = /^[-+]?[0-9]+(\.[0-9]+)?$/;
  if ( value != "" && !validFormatRegExp.test(value) )
  {
    result=false;
  }
  return result;
}
function getCheckedValue(radioObj) {
  if(!radioObj)
    return "";
  var radioLength = radioObj.length;
  if(radioLength == undefined)
    if(radioObj.checked)
      return radioObj.value;
    else
      return "";
  for(var i = 0; i < radioLength; i++) {
    if(radioObj[i].checked) {
      return radioObj[i].value;
    }
  }
  return "";
}
function myConfirm(stText, iR, iMax)
{
  var i = 1;
  var stClass = "d1";
  
  for( i = 1 ; i <= iMax ; i++ )
  {
    var oObj = getObject("tr"+i );
    if ( oObj != null )
    {
      if ( i == iR )
        stClass = "d1";
      else
        stClass = "d0";

      if ( oObj.className != null )
        oObj.className  = stClass;
      oObj.setAttribute("class", stClass);
    }
  }
  return confirm( stText );
}
/*22         Name Processor (D22)
Req’t ID	Title	Level	Description
1	Size	0
2	  Minimum	1	The name shall be at least 2 characters.
3	  Maximum	1	The name shall be less than 256 characters.
4	Content	0
5	  Characters	1
6	    Alphabetic	2	The name shall be able to contain alphabetic characters.
7	    Digits	2	The name shall be able to contain digits.
8	    Apostrophes	2	The name shall be able to contain apostrophes.
9	    Hyphens	2	The name shall be able to contain hyphens.
10	    Blank	2	The name shall be able to contain a single blank separating non-blanks
11	  Initial Character	1	The initial character shall be an alphabetic.
12	  Consecutive Multiple Blanks	1	Multiple consecutive blanks shall signify the end of the inventory name.
13	  Case	1	The name shall be case sensitive.
14	Conflict	0	The name shall be unique within its type.
Table 30—Deliverable 22—Name Processor
 */
function validateD22( stValue )
{
  var bReturn = true;
  var myChar = "";
  var iPos=0;
  var iSpaceCount=0;
  gstD22 = "";
  if ( stValue == null || stValue == undefined )
  {
    gstD22 += "\nInvalid field";
    bReturn = false;
  }else if ( stValue.length < 2 )
  {
    gstD22 += "\nToo short. Must be at least 2 characters ";
    bReturn = false;
  }else if ( stValue.length > 256 )
  {
    gstD22 += "\nToo Long. Must be less then 256 characters ";
    bReturn = false;
  }
  else
    for( iPos=0 ; bReturn == true && iPos < stValue.length ; iPos++ )
    {
      myChar = stValue.substr(iPos,1);
      if ( ( myChar >= "a" && myChar <= "z" ) || ( myChar >= "A" && myChar <= "Z" ) )
      {
        iSpaceCount=0;
        continue;
      }else
      {
        if ( iPos == 0 )
        {
          gstD22 += "\nFirst character must be ALPHA ";
          bReturn = false; // First Char must be alpha
          break;
        }else if ( myChar >= "0" && myChar <= "9" )
        {
          iSpaceCount=0;
          continue;
        }else if ( myChar == "'" || myChar == "-" )
        {
          iSpaceCount=0;
          continue;
        }else  if ( myChar == " " )
        {
          iSpaceCount++;
          if ( iSpaceCount > 1 )
          {
            gstD22 += "\nToo many spaces ";
            bReturn = false;
          }else
            continue;
        }else
        {
          gstD22 += "\nInvalid special character. Only SPACE, HYPHEN and APOSTORPHES are permitted";
          bReturn = false; // Invalid Char
          break;
        }
      }
    }
  return bReturn;
}
var gaFields;
function specialDays(  iField )
{
  var stEdit = "";
  var stValues = getTextValue("f"+gaValidation[iField][0] );
  var i=0;
  var iR=0;
  stEdit += "<table border=0 bgcolor='blue' cellpadding=1 cellspacing=1>";
  // Header
  var stNew = "";
  stEdit += "<tr class=d1>";

  var aRecords = stValues.split("|");
  gaFields = aRecords[0].split("~");
 
  for ( i= 0 ; i < gaFields.length ; i++ )
  {
    var stTemp =  trim(gaFields[i]);
    if ( stTemp.length > 0 )
    {
      var aV = stTemp.split("^");
      stEdit  += "<td>"+aV[0]+"</td>";
      if ( stNew.length > 0 )
        stNew += "~";
      stNew += trim(getTextValue( aV[0] ));
    }else
      stNew += "~";
  }
  if ( stNew.length > gaFields.length )
  {
    alert( stNew.length + " NEW " + stNew );
    stValues += "\n|" + stNew;
    aRecords = stValues.split("|");
  }
  stEdit += "<td>&nbsp;</th></tr>";
  var iDo = 1;
  for ( iR= 1 ; iR < aRecords.length ; iR++ )
  {
    var aValues = aRecords[iR].split("~");

    var stDel = getTextValue("f492_del");
    iDo = 1;
    if ( stDel != null && stDel.length > 1 )
    {
      var aDel = stDel.split("~");
      for( ii=0 ; ii < aDel.length ; ii++ )
      {
        if ( aDel[ii] == aValues[0] )
        {
          iDo = 0;
          break;
        }
      }
    }
    if ( iDo > 0 )
    {
      stEdit += "<tr class=d0>";
      for ( i= 0 ; i < gaFields.length ; i++ )
      {
        stTemp = trim(gaFields[i]);
        if ( stTemp.length > 0 )
        {
          aV = stTemp.split("^");
          stEdit  += "<td><input DISABLED type=text name=\"field"+i+"_"+iR+"\" id=\"field"+i+"_"+iR+"\" value=\""+aValues[i]+"\" style='width:"+aV[1]+";'></td>";
        }
      }   
      stEdit += "<td><input type=image border=0 src='./common/b_drop.png'"
      + " onClick=\"return deleteSpecialDay("+iR+","+aValues[0]+");\">"
      + "</a></td></tr>";
    }
  }
  var iMax2 = iR + 5;
  for (  ; iR < iMax2 ; iR++ )
  {
    stEdit += "<tr class=d0>";
    for ( i= 0 ; i < gaFields.length ; i++ )
    {
      stTemp = trim(gaFields[i]);
     
      if ( stTemp.length > 0 )
      {
        aV = stTemp.split("^");
        stEdit  += "<td><input DISABLED type=text name=\"field"+i+"_"+iR+"\" id=\"field"+i+"_"+iR+"\" value='' style='width:"+aV[1]+";'>";
        if(i==3)
	  	{
	  	  stEdit += "<input type=image border=0 src='./common/img/cal.gif' alt='Special Day' class=imageStyle"
	  	  + " onClick='return getPopupValue2("+gaValidation[iField][0]+","+iR+",this.form.f"+gaValidation[iField][0]+",this.form.field0_"+iR+",this.form.field1_"+iR+",this.form.field2_"+iR+",this.form.field3_"+iR+",this.form.field4_"+iR+",this.form.field5_"+iR+",this.form.field6_"+iR+","+giUser+");'>";
	  	}
	    if(i==4)
		{
		  stEdit += "<input type=image border=0 src='./common/img/cal.gif' alt='Special Day' class=imageStyle"
		  + " onClick='return getPopupValue3("+gaValidation[iField][0]+","+iR+",this.form.f"+gaValidation[iField][0]+",this.form.field0_"+iR+",this.form.field1_"+iR+",this.form.field2_"+iR+",this.form.field3_"+iR+",this.form.field4_"+iR+",this.form.field5_"+iR+",this.form.field6_"+iR+","+giUser+");'>";
		}
        stEdit += "</td>";
      }
    	
     
    }
    stEdit += "</tr>";
  }
  stEdit += "</table>";
  SetTextValue( "div"+gaValidation[iField][0], stEdit );

}
/*function specialDays(  iField )
{
  var stEdit = "";
  var stValues = getTextValue("f"+gaValidation[iField][0] );
  var i=0;
  var iR=0;
  stEdit += "<table border=0 bgcolor='blue' cellpadding=1 cellspacing=1>";
  // Header
  var stNew = "";
  stEdit += "<tr class=d1>";

  var aRecords = stValues.split("|");
  gaFields = aRecords[0].split("~");
 
  for ( i= 0 ; i < gaFields.length ; i++ )
  {
    var stTemp =  trim(gaFields[i]);
    if ( stTemp.length > 0 )
    {
      var aV = stTemp.split("^");
      stEdit  += "<td>"+aV[0]+"</td>";
      if ( stNew.length > 0 )
        stNew += "~";
      stNew += trim(getTextValue( aV[0] ));
    }else
      stNew += "~";
  }
  if ( stNew.length > gaFields.length )
  {
    alert( stNew.length + " NEW " + stNew );
    stValues += "\n|" + stNew;
    aRecords = stValues.split("|");
  }
  stEdit += "<td>&nbsp;</th></tr>";
  var iDo = 1;
  for ( iR= 1 ; iR < aRecords.length ; iR++ )
  {
    var aValues = aRecords[iR].split("~");

    var stDel = getTextValue("f492_del");
    iDo = 1;
    if ( stDel != null && stDel.length > 1 )
    {
      var aDel = stDel.split("~");
      for( ii=0 ; ii < aDel.length ; ii++ )
      {
        if ( aDel[ii] == aValues[0] )
        {
          iDo = 0;
          break;
        }
      }
    }
    if ( iDo > 0 )
    {
      stEdit += "<tr class=d0>";
      for ( i= 0 ; i < gaFields.length ; i++ )
      {
        stTemp = trim(gaFields[i]);
        if ( stTemp.length > 0 )
        {
          aV = stTemp.split("^");
          stEdit  += "<td><input DISABLED type=text name=\"field"+i+"_"+iR+"\" id=\"field"+i+"_"+iR+"\" value=\""+aValues[i]+"\" style='width:"+aV[1]+";'></td>";
        }
      }   
      stEdit += "<td><input type=image border=0 src='./common/b_drop.png'"
      + " onClick=\"return deleteSpecialDay("+iR+","+aValues[0]+");\">"
      + "</a></td></tr>";
    }
  }
  var iMax2 = iR + 5;
  for (  ; iR < iMax2 ; iR++ )
  {
    stEdit += "<tr class=d0>";
    for ( i= 0 ; i < gaFields.length ; i++ )
    {
      stTemp = trim(gaFields[i]);
      if ( stTemp.length > 0 )
      {
        aV = stTemp.split("^");
        stEdit  += "<td><input DISABLED type=text name=\"field"+i+"_"+iR+"\" id=\"field"+i+"_"+iR+"\" value='' style='width:"+aV[1]+";'></td>";
      }
    }
  
    stEdit += "<td>";
    stEdit += "<input type=image border=0 src='./common/img/cal.gif' alt='Special Day' class=imageStyle"
    + " onClick='return getPopupValue2("+gaValidation[iField][0]+","+iR+",this.form.f"+gaValidation[iField][0]+",this.form.field0_"+iR+",this.form.field1_"+iR+",this.form.field2_"+iR+",this.form.field3_"+iR+",this.form.field4_"+iR+",this.form.field5_"+iR+",this.form.field6_"+iR+","+giUser+");'>";
    stEdit += "</td></tr>";
  }
  stEdit += "</table>";
  SetTextValue( "div"+gaValidation[iField][0], stEdit );

}*/

function deleteSpecialDay( f1, f2 )
{
  var stValue = getTextValue("f492_del");
  stValue += "~"+f2;
  SetTextValue2("f492_del",stValue);
  
  for( iField=0; iField < giNrValidation2 ; iField++ )
  {
    if ( gaValidation[iField] != null )
    {
      if ( gaValidation[iField][3] == 40 ) // Special Days
      {
        specialDays(iField);
      }
    }
  } 
  return false;
}

// Removes leading whitespaces
function LTrim( value )
{

  var re = /\s*((\S+\s*)*)/;
  return value.replace(re, "$1");

}

// Removes ending whitespaces
function RTrim( value )
{

  var re = /((\s*\S+)*)\s*/;
  return value.replace(re, "$1");

}

// Removes leading and ending whitespaces
function trim( value )
{

  return LTrim(RTrim(value));

}
// --------------------------------------------------- Requirements/Schedule Editor
iens6=document.all||document.getElementById;
ns4=document.layers;

var thename;
var theobj;
var thetext;
var winHeight;
var winWidth;
var boxPosition;
var headerColor;
var tableColor;
var timerID;
var seconds=0;
var x=0;
var y=0;
var offsetx = 5;
var offsety = 5;
var oldRecId = 0;

if(ns4)
{
  document.captureEvents(Event.MOUSEMOVE);
}
document.onmousemove=getXY;

function buildText(iReqSched, iRecId, iId, iFlags, iLevel,iNextLevel, iPrevLevel, iNextShow, stLink)
{
  tcolor='black';
  bcolor='red';

  // CHANGE EACH ARRAY ELEMENT BELOW TO YOUR OWN CONTENT. MAKE SURE TO USE SINGLE QUOTES INSIDE DOUBLE QUOTES.
  //height="+winHeight+"
  text="<table border='5' width="+winWidth+" bgcolor='"+tableColor+"' cellspacing='0' cellpadding=1>";
  text+="<tr><td align='center' valign='top' bgcolor='"+headerColor+"'>";
  text+="<font face='arial,helvetica' color='"+tcolor+"' SIZE='-1'>ACTIONS (ID="+iId+")</font>";
  text+="</td></tr>";
  text+="<tr><td align='left' valign='top' height='90%'>";
  text+="<font face='verdana,helvetica' color='"+bcolor+"' SIZE='-1'>";
  if(  iNextLevel > iLevel && iNextShow > 0 )
    text+="<a href='./"+stLink+"&a=collapse&r="+iRecId+"#row"+iId+"'>Collapse</a><br>";

  if( iNextShow > 0 )
  {
    if ( iNextLevel > iLevel )
      text+="<a href='./"+stLink+"&a=delete&what=this&r="+iRecId+"#row"+iId+"' onClick=\"return confirm('Warning – the children of this requirement will be promoted one level after the selected requirement`s deletion.  Do you still wish to delete this requirement and promote its children?')\">Delete this item only</a><br>";
    else
      text+="<a href='./"+stLink+"&a=delete&what=this&r="+iRecId+"#row"+iId+"' onClick=\"return confirm('Warning – the selected requirement will be deleted.  Do you still wish to delete this requirement?')\">Delete this item only</a><br>";
  }
  if ( iReqSched < 3 )
  {
    text+="<a href='./"+stLink+"&a=delete&what=children&r="+iRecId+"#row"+iId+"' onClick=\"return confirm('Warning – the children of this requirement will be deleted along with the selected requirement.  Do you still wish to delete this requirement and delete its children requirements?')\">Delete with children</a><br>";
    if (  iNextShow > 0 && iLevel > 0 )
    {
      text+="<a href='./"+stLink+"&a=demote&what=children&r="+iRecId+"#row"+iId+"' onClick=\"return confirm('Warning – The Demote action will automatically demote the selected requirement one level and the entire selected requirement’s children one level.  Do you wish to demote this requirement and its children?')\">Demote</a><br>";
    }
  }
  text+="<a href='./"+stLink+"&a=editfull&r="+iRecId+"#row"+iId+"'>Edit full record</a><br>";
  if ( iReqSched < 3 )
    text+="<a href='./"+stLink+"&edit="+iRecId+"#row"+iId+"'>Edit inline</a><br>";
  //if ( iNextShow <= 0 )
  if( ! ( iNextLevel > iLevel && iNextShow > 0 ) )
    text+="<a href='./"+stLink+"&a=expand&r="+iRecId+"#row"+iId+"'>Expand</a><br>";
  text+="<a href='./"+stLink+"&a=insert&what=above&r="+iRecId+"#row"+iId+"'>Insert above</a><br>";
  if ( iNextShow > 0  )
    text+="<a href='./"+stLink+"&a=insert&what=below&r="+iRecId+"#row"+iId+"'>Insert below</a><br>";
  if ( iReqSched < 3 )
  {
    //if( (iFlags & 0x10) != 0 )
      text+="<a href='./"+stLink+"&a=map&r="+iRecId+"#row"+iId+"'>Map</a><br>"; // Only Low level
    //if( iFlags == 0 && iNextLevel <=  iLevel )
    //if ( iReqSched == 2 )
    //  text+="<a href='./"+stLink+"&a=test&r="+iRecId+"#row"+iId+"'>Test</a><br>";
    if ( iPrevLevel >= iLevel )
    {
      if( iNextLevel <=  iLevel )
        text+="<a href='./"+stLink+"&a=promote&what=this&r="+iRecId+"#row"+iId+"' onClick=\"return confirm('Warning – the selected requirement will become a child of the preceding requirement.  Do you wish to promote this requirement?')\">Promote this item only</a><br>";
      else
        text+="<a href='./"+stLink+"&a=promote&what=this&r="+iRecId+"#row"+iId+"' onClick=\"return confirm('Warning – This will make this requirement a child (sub-requirement) of the preceding requirement and all this requirements children will be promoted one level.  Do you wish to promote this requirement and its children?')\">Promote this item only</a><br>";
      text+="<a href='./"+stLink+"&a=promote&what=children&r="+iRecId+"#row"+iId+"' onClick=\"return confirm('Warning – the children of this requirement will be promoted one level.  Do you wish to promote this requirement and its children?')\">Promote with children</a><br>";
    }
  }
  var regexS = "[\\?&]child=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec(stLink);
  var childVal = results[1];
  if(childVal == 21){
	  text+="<a href='./"+stLink+"&a=send&what=this&r="+iRecId+"#row"+iId+"' onClick=\"return confirm('Warning – this task will be removed from this list. The children of this task will be promoted one level after the selected task`s deletion.  Do you still wish to delete this task and send a message to users?')\">Remove and Send Message</a><br>";
  }
  text+="<hr style=\"color: #aaa;background-color: #aaa;height: 5px;\"><a href='./"+stLink+"&a=customize&r="+iRecId+"#row"+iId+"'>Customize</a><br>";

  text+="</font></td></tr></table>";

  return text;
}
function setActionsClick(iReqSched, iRecId, iId, iFlags, iLevel, iNextLevel, iPrevLevel, iNextShow, stLink,iMaxCol)
{
  clearTimeout(timerID);
  boxPosition="bottomR";
  tableColor='#e6f1f2';
  headerColor='#d5e9ea';
  winWidth=210;
  winHeight=260;

  thetext=buildText(iReqSched, iRecId, iId, iFlags, iLevel, iNextLevel, iPrevLevel, iNextShow, stLink);
  x=x-50;
  y=y-10;
  if(iens6)
  {
    thename = "viewer"
    theobj = getObject( thename );
    theobj.style.width=winWidth;
    theobj.style.height=winHeight;
    theobj.style.left=(x+5)+"px";
    theobj.style.top=(y+5)+"px";
    theobj.style.display = "";
    theobj.innerHTML = "";
    theobj.innerHTML=thetext;
  }
  if(ns4)
  {
    thename = "nsviewer";
    theobj = eval("document."+thename);
    theobj.left=x;
    theobj.top=y;
    theobj.width=winWidth;
    theobj.clip.width=winWidth;
    theobj.height=winHeight;
    theobj.clip.height=winHeight;
    theobj.document.write("<table cellspacing=0 width="+winWidth+" height="+winHeight+" border=0><tr><td width=100% valign=top><font type='times' size='2' style='color:black;font-weight:normal'>"+thetext+"</font></td></tr></table>");
    theobj.document.close();
  }
  viewIt();
  if ( oldRecId > 0 )
  {
    for( i=1 ; i <= iMaxCol ; i++ )
    {
      oObj = getObject( "r" + oldRecId + "_" + i );
      if ( oObj.className != null )
        oObj.className  = "l1td2";
      oObj.setAttribute("class", "l1td2");
    }
  }
  for( i=1 ; i <= iMaxCol ; i++ )
  {
    oObj = getObject( "r" + iRecId + "_" + i );
    if ( oObj.className != null )
      oObj.className  = "l1td3";
    oObj.setAttribute("class", "l1td3");
  }
  oldRecId = iRecId;
}


function setActions(iReqSched, iRecId, iId, iFlags, iLevel, iNextLevel, iPrevLevel, iNextShow, stLink,iMaxCol)
{
  clearTimeout(timerID);
  boxPosition="bottomR";
  tableColor='#e6f1f2';
  headerColor='#d5e9ea';
  winWidth=210;
  winHeight=260;
  if ( x < 10 )
    return; // dont paint if no x is set
  
  thetext=buildText(iReqSched, iRecId, iId, iFlags, iLevel, iNextLevel, iPrevLevel, iNextShow, stLink);
  x=x+offsetx;
  y=y+offsety;

  if(iens6)
  {
    thename = "viewer"
    theobj = getObject( thename );
    theobj.style.width=winWidth;
    theobj.style.height=winHeight;
    theobj.style.left=(x+5)+"px";
    theobj.style.top=(y+5)+"px";
    theobj.style.display = "";
    theobj.innerHTML = "";
    theobj.innerHTML=thetext;
  }
  if(ns4)
  {
    thename = "nsviewer";
    theobj = eval("document."+thename);
    theobj.left=x;
    theobj.top=y;
    theobj.width=winWidth;
    theobj.clip.width=winWidth;
    theobj.height=winHeight;
    theobj.clip.height=winHeight;
    theobj.document.write("<table cellspacing=0 width="+winWidth+" height="+winHeight+" border=0><tr><td width=100% valign=top><font type='times' size='2' style='color:black;font-weight:normal'>"+thetext+"</font></td></tr></table>");
    theobj.document.close();
  }
  viewIt();
}
function viewIt()
{
  if(iens6)
  {
    theobj.style.visibility="visible";
  }
  if(ns4)
  {
    theobj.visibility = "visible";
  }
}
function stopIt()
{
  if(iens6)
  {
    theobj.innerHTML = "";
    theobj.style.visibility="hidden";
  }
  if(ns4)
  {
    theobj.document.write("");
    theobj.document.close();
    theobj.visibility="hidden";
  }
}
function timer(sec)
{
  seconds=parseInt(sec);
  if(seconds>0)
  {
    seconds--;
    timerID=setTimeout("timer(seconds)",1000);
  }else
  {
    stopIt();
  }
}
function getXY(e)
{
  if (ns4)
  {
    x=0;
    y=0;
    x=e.pageX;
    y=e.pageY;
  }
  if (iens6&&document.all)
  {
    x=0;
    y=0;
    x=event.x;
    y=event.y;
  }
  if (iens6&&!document.all)
  {
    x=0;
    y=0;
    x=e.pageX;
    y=e.pageY;
  }
}

function getSelectedValue( stField )
{
  var stValue = "";
  var oField = getObject( stField );
  if ( oField != null && oField.options != null )
  {
    for (var i=0; i< oField.options.length; i++)
    {
      if (oField.options[i].selected==true)
      {
        stValue =  oField.options[i].value;
        break;
      }
    }
  }
  return stValue;
}

/*
 * Project: full edit - Set estimated date based on the fixed start date and throws diagnostic for dependencies
 */
function setEstFinishDate(estDays, startInput, endInput){
	var startDate = document.getElementById(startInput).value;
	var ans = true;
	if(startDate != ""){
		
		if(dependencyCount > 0){
			ans = confirm("Do you really wish to specify a fixed date.  If so, the dependencies will be removed.");
		}
		
		if(ans){
			var d = new Date(startDate);
		    d.setDate(d.getDate()+parseInt(estDays));
		    if(d.getYear()<1900)
		    	document.getElementById(endInput).value = (d.getMonth()+1)+"/"+d.getDate()+"/"+(d.getYear()+1900);
		    else
		    	document.getElementById(endInput).value = (d.getMonth()+1)+"/"+d.getDate()+"/"+(d.getYear());
		}else{
			document.getElementById(startInput).value = "";
			document.getElementById(endInput).value = "";
		}
	}
}

/*
 * Checks if current date is before given date
 */
function isBeforeDate(theDate){
	var today = new Date();
	var compDate = new Date(theDate);
	
	if(compDate > today)	//before given date
		return true;
	else
		return false;		//after or on given date
}

function setDependencyCount(num){
	dependencyCount = num;
}

/* Start of Issue AS -- 2Oct2011  -- Issue #18*/
function checkAll(nmType, elem)
{
	
	if(elem.value == 1 || elem.value == 32 || elem.value == 64 || elem.value == 128 || elem.value == 512 || elem.value == 1024 )
    {
		nmType[0].checked = false; 
    }
    else
	{
	    for (var i = 1; i < nmType.length; i++)
	    	nmType[i].checked = false;
	}
}
/* End of Issue AS -- 2Oct2011  -- Issue #18*/