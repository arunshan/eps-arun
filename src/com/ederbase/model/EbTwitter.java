/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * http://yusuke.homeip.net/twitter4j/en/code-examples.html
 */
package com.ederbase.model;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;
import twitter4j.DirectMessage;
import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.User;

public class EbTwitter
{

  EbEnterprise ebEnt = null;
  String stError = "";
  int iTot = 0;
  int nmLoginId = 0;
  int iSearch = 0;
  int iFollow = 0;
  int iExit = 0;
  String stMyTwitterId = "";

  public EbTwitter(EbEnterprise ebEnt)
  {
    this.ebEnt = ebEnt;
  }

  public String getError()
  {
    return this.stError;
  }

  private String getPassword(String stId)
  {
    this.stMyTwitterId = stId;
    this.nmLoginId = this.ebEnt.dbEnterprise.ExecuteSql1n("select nmMyTwitterId from MyTwitterUser where stTwitterId=\"" + stId + "\"");
    return this.ebEnt.dbEnterprise.ExecuteSql1("select stPassword from MyTwitterUser where stTwitterId=\"" + stId + "\"");
  }

  public String processSearchFollowers(String stFollowingUser, int iMaxSearchUsers, int iMaxFolling)
  {
    String stReturn = "<center><h1>Twitter Process Following Followers</h1><table>";
    Twitter twitter = null;

    try
    {
      String stSql = "SELECT * FROM MyTwitterUser where nmTwitterId=0 and stUserType='S' " +
          " and dtLimit is null limit " + iMaxSearchUsers;
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        stReturn += "<tr><th>#</th><th>Search User</th><th>MyId</th><th>Twitter</th><th>User</th><th>Name</th><th>Desc</th><th>Loc</td><th>Desc</td></tr>";
        twitter = new Twitter(stFollowingUser, getPassword(stFollowingUser));
        int iCount = 0;
        for (int iU = 1; iU <= iMax && this.iExit == 0; iU++)
        {
          rs.absolute(iU);
          try
          {
            List<User> aUsers = twitter.getFollowers(rs.getString("stTwitterId"));
            this.ebEnt.dbEnterprise.ExecuteUpdate("delete from MyTwitterFollowers where nmLoginId=" + this.nmLoginId);
            for (User u : aUsers)
            {
              iCount++;
              stReturn += "<tr>";
              stReturn += "<td align=right>" + iCount + "</td>";
              stReturn += "<td align=left>" + rs.getString("stTwitterId") + "</td>";
              stReturn += "<td align=left>" + rs.getString("nmMyTwitterId") + "</td>";
              stReturn += "<td align=left>" + u.getId() + "</td>";
              stReturn += "<td align=left>" + u.getScreenName() + "</td>";
              stReturn += "<td align=left>" + u.getName() + "</td>";
              stReturn += "<td align=left>" + u.getDescription() + "</td>";
              stReturn += "<td align=left>" + u.getLocation() + "</td>";
              stReturn += "<td align=left>" + u.getTimeZone() + "</td>";
              int iId = addTwUser(u);
              int i = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from MyTwitterFollowing " +
                  "where nmLoginId=" + this.nmLoginId + " and nmFollowingId=" + iId);

              if (i <= 0 && iId != nmLoginId && iFollow < iMaxFolling && this.iExit == 0)
              {
                try
                {
                  twitter.createFriendship(u.getScreenName(), true);
                  iFollow++;
                  this.ebEnt.dbEnterprise.ExecuteUpdate("insert into MyTwitterFollowing" +
                      "(nmLoginId,nmFollowingId,dtEntered)" +
                      "value(" + nmLoginId + "," + iId + ",now())");
                  stReturn += "<td align=left>ok " + iFollow + "</td>";
                } catch (twitter4j.TwitterException e)
                {
                  this.stError += "<br>ERRPR TwitterException " + rs.getString("stTwitterId") + ": " + e;
                  if (e.getMessage().indexOf("already") <= 0)
                  {
                    this.ebEnt.dbEnterprise.ExecuteUpdate("update MyTwitterUser set dtLimit=now()" +
                        " where nmTwitterId=" + this.nmLoginId);
                    this.iExit++;
                  } else
                  {
                    this.ebEnt.dbEnterprise.ExecuteUpdate("insert into MyTwitterFollowing" +
                        "(nmLoginId,nmFollowingId,dtEntered)" +
                        "value(" + nmLoginId + "," + iId + ",now())");
                  }
                  stReturn += "<td align=left>" + e.getMessage() + "</td>";
                }
              } else
              {
                if (i > 0)
                  stReturn += "<td align=left>already</td>";
                else
                  stReturn += "<td align=left>skp</td>";

              }
              stReturn += "</tr>";
            }
          } catch (twitter4j.TwitterException e)
          {
            this.stError += "<br>ERRPR TwitterException: " + e;
            if (e.getMessage().indexOf("exceeded") >= 0)
            {
              this.ebEnt.dbEnterprise.ExecuteUpdate("update MyTwitterUser set=dtLimit=now()" +
                  " where nmTwitterId=" + this.nmLoginId);
            }
          }
        }
      }
    } catch (Exception e)
    {
      this.stError += "<br>ERRPR Exception: " + e;
    }
    stReturn += "</table></center>";;
    return stReturn;
  }

  public int addTwUser(User user)
  {
    int i = 0;
    int nmMyTwitterId = this.ebEnt.dbEnterprise.ExecuteSql1n("select nmMyTwitterId from MyTwitterUser " +
        "where stTwitterId=\"" + user.getScreenName() + "\" ");
    if (nmMyTwitterId <= 0)
    {
      nmMyTwitterId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(nmMyTwitterId) from MyTwitterUser ");
      nmMyTwitterId++;
      this.ebEnt.dbEnterprise.ExecuteUpdate("insert into MyTwitterUser " +
          "(stTwitterId,nmTwitterId,dtEntered,nmMyTwitterId,stUserType,stName,stDescription,stLocation,stTimeZone) values" +
          "( \"" + user.getScreenName() + "\"," + user.getId() + ",now()," + nmMyTwitterId + ",'L'," +
          this.ebEnt.dbEnterprise.fmtDbString(user.getName()) + "," +
          this.ebEnt.dbEnterprise.fmtDbString(user.getScreenName()) + "," +
          this.ebEnt.dbEnterprise.fmtDbString(user.getLocation()) + "," +
          this.ebEnt.dbEnterprise.fmtDbString(user.getTimeZone()) + ")");

    } else
    {
      i = this.ebEnt.dbEnterprise.ExecuteSql1n("select nmTwitterId from MyTwitterUser " +
          "where stTwitterId=\"" + user.getScreenName() + "\" ");
      this.ebEnt.dbEnterprise.ExecuteUpdate("update MyTwitterUser " +
          "set nmTwitterId=" + user.getId() + ", stUserType='L'" +
          ", stName=" + this.ebEnt.dbEnterprise.fmtDbString(user.getName()) +
          ", stDescription=" + this.ebEnt.dbEnterprise.fmtDbString(user.getScreenName()) +
          ", stLocation=" + this.ebEnt.dbEnterprise.fmtDbString(user.getLocation()) +
          ", stTimeZone=" + this.ebEnt.dbEnterprise.fmtDbString(user.getTimeZone()) +
          " where nmMyTwitterId=" + nmMyTwitterId);
    }
    return nmMyTwitterId;
  }

  String syncFollowers()
  {
    String stReturn = "<table>";
    Twitter twitter = null;
    int iCount = 0;
    try
    {
      String stSql = "SELECT f.* FROM MyTwitterFollowers f left join MyTwitterUser u " +
          "on u.nmTwitterId=f.nmFollowerTwitterId where u.nmTwitterId is null limit 2 ";
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        stReturn += "<tr><th>#</th><th>User</th><th>MyId</th><th>Twitter</th><th>User</th><th>Name</th><th>Desc</th><th>Loc</td><th>Desc</td></tr>";
        for (int iU = 1; iU <= iMax && this.iExit == 0; iU++)
        {
          rs.absolute(iU);
          twitter = new Twitter("roberteder2", getPassword("roberteder2"));
          User user = twitter.showUser(rs.getString("nmFollowerTwitterId"));
          this.addTwUser(user);
          List<User> aUsers = twitter.getFollowers(rs.getString("nmFollowerTwitterId"));
          this.ebEnt.dbEnterprise.ExecuteUpdate("delete from MyTwitterFollowers where nmLoginId=" + this.nmLoginId);
          for (User u : aUsers)
          {
            this.addTwUser(u);
          }
        }
      }
    } catch (twitter4j.TwitterException e)
    {
      this.stError += "<br>ERRPR TwitterException: " + e;
    } catch (Exception e)
    {
      this.stError += "<br>ERRPR Exception: " + e;
    }
    stReturn += "</table>";
    return stReturn;
  }

  String getTwitterDetail(
      String stTwitterId)
  {
    String stReturn = "<table>";

    try
    {
      Twitter twitter = new Twitter(stTwitterId, getPassword(stTwitterId));
      User user = twitter.getUserDetail(stTwitterId);
      stReturn +=
          "</tr><tr><td>user.getScreenName(): </td><td><b>" + user.getScreenName() + "</b></td>";
      stReturn +=
          "</tr><tr><td>user.getName(): </td><td>" + user.getName() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getLocation(): </td><td>" + user.getLocation() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getCreatedAt(): </td><td>" + user.getCreatedAt() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getId(): </td><td>" + user.getId() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getUtcOffset(): </td><td>" + user.getUtcOffset() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getProfileBackgroundColor(): </td><td>" + user.getProfileBackgroundColor() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getProfileBackgroundImageUrl(): </td><td>" + user.getProfileBackgroundImageUrl() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getProfileImageURL(): </td><td>" + user.getProfileImageURL() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getStatusInReplyToScreenName(): </td><td>" + user.getStatusInReplyToScreenName() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getStatusInReplyToUserId(): </td><td>" + user.getStatusInReplyToUserId() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getURL(): </td><td>" + user.getURL() + "</td>";

      stReturn +=
          "</tr><tr><td>user.getFavouritesCount(): </td><td>" + user.getFavouritesCount() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getFollowersCount(): </td><td>" + user.getFollowersCount() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getFriendsCount(): </td><td>" + user.getFriendsCount() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getRateLimitLimit(): </td><td>" + user.getRateLimitLimit() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getRateLimitRemaining(): </td><td>" + user.getRateLimitRemaining() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getRateLimitReset(): </td><td>" + user.getRateLimitReset() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getStatusesCount(): </td><td>" + user.getStatusesCount() + "</td>";
      stReturn +=
          "</tr><tr><td>user.getTimeZone(): </td><td>" + user.getTimeZone() + "</td>";
      stReturn +=
          "</tr><tr><td colspan=2><h1>getFriendsTimeline pg1</h1></td>";

      int iCount = 0;
      Paging paging1 = new Paging(1, 300); // only shows 200 !!!
      List<Status> statuses = twitter.getFriendsTimeline(paging1);
      for (Status status : statuses)
      {
        iCount++;
        stReturn +=
            "</tr><tr><td>(" + iCount + ") " + status.getUser().getScreenName() + " " +
            status.getCreatedAt() + "</td><td>" + status.getText() + "</td>";
      }

      stReturn += "</tr><tr><td colspan=2><h1>getFriendsTimeline pg2</h1></td>";
      /*
      Paging paging2 = new Paging(2, 200);
      statuses = twitter.getFriendsTimeline(paging2);
      for (Status status : statuses)
      {
      iCount++;
      stReturn += "</tr><tr><td>("+iCount+") " + status.getUser().getScreenName() + " "+
      status.getCreatedAt() +"</td><td>" + status.getText() + "</td>";
      }
       */
      stReturn +=
          "</tr><tr><td colspan=2><h1>getDirectMessages</h1></td>";
      List<DirectMessage> messages = twitter.getDirectMessages(paging1);
      iCount =
          0;
      for (DirectMessage message : messages)
      {
        iCount++;
        stReturn +=
            "</tr><tr><td>(" + iCount + ") " + message.getSenderScreenName() + "</td><td>" + message.getText() + "</td>";
      }

    } catch (twitter4j.TwitterException e)
    {
      this.stError += "<br>ERRPR TwitterException: " + e;
    }

    stReturn += "</table>";

    return stReturn;
  }

  String processSendTwitter()
  {
    String stMessage = "";
    String stReturn = "</form><form method=post><table>";
    String stValue = "";
    try
    {
      String stSql = "SELECT " +
          "u.nmMyTwitterId,u.stTwitterId,u.stPassword, u.nmFollowers, u.nmFollowing, " +
          "TIMESTAMPDIFF(SECOND,dtLimit,now()) as nmLimit " +
          "FROM MyTwitterUser u where stUserType='M' " +
          "order by u.nmMyTwitterId";
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        stValue =
            this.ebEnt.ebUd.request.getParameter("submit9");
        if (stValue != null && stValue.length() > 0)
        {
          stReturn += "<tr><th colspan=2><h1>Processing Send Twitter</h1></th></tr>";
          for (int iT = 1; iT <=
              iMax; iT++)
          {
            rs.absolute(iT);
            stMessage =
                this.ebEnt.ebUd.request.getParameter("t_" + rs.getString("stTwitterId"));
            if (stMessage != null && stMessage.length() > 0)
            {
              stReturn += "<tr>";
              stReturn +=
                  "<td>" + rs.getString("stTwitterId") + "</td>";
              stReturn +=
                  "<td colspan=2><b>" + stMessage + "</b> ";
              stReturn +=
                  sendTweet(rs.getString("stTwitterId"), stMessage);
              stReturn +=
                  "</td>";
              stReturn +=
                  "</tr>";
            }

          }
        }
        {
          stReturn += "<tr><th colspan=2><h1>Send Twitter</h1></th></tr>";
          for (int iT = 1; iT <=
              iMax; iT++)
          {
            rs.absolute(iT);
            stReturn +=
                "<tr>";
            stReturn +=
                "<td>" + rs.getString("nmFollowers") + "</td>";
            stReturn +=
                "<td>" + rs.getString("stTwitterId") + "</td>";
            if (rs.getInt("nmLimit") <= 0)
              stReturn += "<td><input type=text name='t_" + rs.getString("stTwitterId") + "' value='' size=141 MAXLENGTH =140></td>";
            else
              stReturn += "<td>Blocked ID</td>";
            stReturn +=
                "</tr>";
          }

          stReturn += "<tr><th colspan=2><input type=submit name=submit9 value='Send Twitter'></th></tr>";
        }

      } else
        stReturn += "tr><th colspan=2>ERROR: no twitter accounts</th></tr>";
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR processSendTwitter: " + e;
    }

    stReturn += "</table></form>";
    return stReturn;
  }

  public String processMarketing( int iType)
  {
    String stReturn = "";
    String stPid = this.ebEnt.ebUd.request.getParameter("pid");
    if (stPid != null && stPid.length() > 0)
    {
      int iPid = Integer.parseInt(stPid);
      switch (iPid)
      {
        case 1:
          stReturn += processSendTwitter();
          break;

        case 2:
          stReturn += this.syncFollowers();
          break;

        case 3:
          stReturn += this.getTwitterDetail(this.ebEnt.ebUd.request.getParameter("tw"));
          break;

        default:
          stReturn += "<br>TODO PID: " + stPid;
          break;

      }

    } else
    {
      stReturn = "<center><h1>Twitter Marketing Home Page</h1><table border=1>" +
          "<tr><td valign=top>";

      stReturn +=
          processResult(iType);
      stReturn +=
          "</td><td valign=top><h1>Available Processes</h1><ul><br>";
      stReturn +=
          "<li> <a href='./?" + this.ebEnt.ebUd.request.getQueryString() + "&pid=1'>Send Tweets</a>";
      stReturn +=
          "<li> <a href='./?" + this.ebEnt.ebUd.request.getQueryString() + "&pid=2'>Synchronize Followers for <b>M</b> Users (MyInfo core)</a>";
      stReturn +=
          "<li> <a href=''>Send Tweets</a>";
      stReturn +=
          "<li> <a href=''>Send Tweets</a>";

      stReturn +=
          "</ul></td></tr></table>";
    }

    return stReturn;
  }

  public String processResult(
      int iType)
  {
    String stReturn = "<center><h1>Twitter Summary</h1><table>";
    Twitter twitter = null;
    int iProc = 0;
    int iFollowers = 0;
    int iTotFollowers = 0;
    int iTotFollowing = 0;
    float fPct = 0;
    String stBg = "";
    String stLimit = "";
    int nmLimit = 0;

    try
    {
      String stSql = "SELECT count(*) as cnt,sum(nmSearches) as srch,sum(nmTotProc) as proc ,sum(l.nmFoLlowing) as follow," +
          "u.nmMyTwitterId,u.stTwitterId,u.stPassword, u.nmFollowers, u.nmFollowing, " +
          "TIMESTAMPDIFF(SECOND,dtLimit,now()) as nmLimit " +
          "FROM MyTwitterLog l, MyTwitterUser u where l.stLogin=u.stTwitterId " +
          "group by u.nmMyTwitterId,u.stTwitterId,u.stPassword,u.nmFollowers, u.nmFollowing " +
          "order by u.nmMyTwitterId";
      ResultSet rs = this.ebEnt.dbEnterprise.ExecuteSql(stSql);
      if (rs != null)
      {
        rs.last();
        int iMax = rs.getRow();
        stReturn +=
            "<tr><th>#</th><th>User</th><th>ID</th><th>Batches</th><th>Searches</th><th>Proc</th><th>Fing</th><th>Fers</td><th>%</td><th>Sec.</td></tr>";
        for (int iU = 1; iU <=
            iMax; iU++)
        {
          rs.absolute(iU);
          nmLoginId =
              rs.getInt("nmMyTwitterId");
          nmLimit =
              rs.getInt("nmLimit");
          iFollowers =
              rs.getInt("nmFollowers");
          stBg =
              "";
          if (nmLimit == 0) // || nmLimit > 3600)// myearlywarning is suspended. 1/6
          {
            if (iType > 0)
            {
              try
              {
                twitter = new Twitter(rs.getString("stTwitterId"), rs.getString("stPassword"));
                IDs ids = twitter.getFollowersIDs();
                int aId[] = ids.getIDs();
                iFollowers =
                    aId.length;
                stLimit =
                    "";
                stBg =
                    " bgcolor=skyblue ";
                this.ebEnt.dbEnterprise.ExecuteUpdate("delete from MyTwitterFollowers where nmLoginId=" + rs.getString("nmMyTwitterId"));
                for (int iF = 0; iF <
                    aId.length; iF++)
                {
                  this.ebEnt.dbEnterprise.ExecuteUpdate("insert into MyTwitterFollowers values(" + rs.getString("nmMyTwitterId") + "," + aId[iF] + ",now())");
                }

              } catch (twitter4j.TwitterException e)
              {
                iFollowers = rs.getInt("nmFollowers");
                stBg =
                    " bgcolor=pink ";
                this.stError += "<br>ERRPR TwitterException: " + e;
                if (e.getMessage().indexOf("exceeded") >= 0)
                {
                  stLimit = ",dtLimit=now()";
                }
              }
            }
          } else
          {
            stBg = " bgcolor='#DDDDDD' ";
          }

          iTotFollowers += iFollowers;
          iTotFollowing +=
              rs.getInt("follow");
          iProc +=
              rs.getInt("proc");
          if (rs.getInt("follow") > 0)
            fPct = (float) iFollowers / (float) rs.getInt("follow") * 100;
          else
            fPct = 0;
          stReturn +=
              "<tr>";
          stReturn +=
              "<td align=right>" + iU + "</td>";
          stReturn +=
              "<td align=right><a href='./?" + this.ebEnt.ebUd.request.getQueryString() + "&pid=3&tw=" + rs.getString("stTwitterId") + "'>" + rs.getString("stTwitterId") + "</a></td>";
          stReturn +=
              "<td align=right>" + rs.getString("nmMyTwitterId") + "</td>";
          stReturn +=
              "<td align=right>" + rs.getString("cnt") + "</td>";
          stReturn +=
              "<td align=right>" + rs.getString("srch") + "</td>";
          stReturn +=
              "<td align=right>" + rs.getString("proc") + "</td>";
          stReturn +=
              "<td align=right>" + rs.getString("follow") + "</td>";
          stReturn +=
              "<td align=right " + stBg + ">" + iFollowers + "</td>";
          stReturn +=
              "<td align=right>" + fPct + "%</td>";
          stReturn +=
              "<td align=right class=smallfont><i>" + nmLimit + "</i></td>";
          stReturn +=
              "</tr>";
          this.ebEnt.dbEnterprise.ExecuteUpdate("update MyTwitterUser " +
              "set nmFollowers=" + iFollowers + ", nmFollowing=" + rs.getString("follow") + stLimit +
              " where stTwitterId=\"" + rs.getString("stTwitterId") + "\"");
        }

        if (iTotFollowing > 0)
          fPct = (float) iTotFollowers / (float) iTotFollowing * 100;
        else
          fPct = 0;
        stReturn +=
            "<tr>";
        stReturn +=
            "<td align=right colspan=5>Totals:</td>";

        stReturn +=
            "<td align=right>" + iProc + "</td>";
        stReturn +=
            "<td align=right>" + iTotFollowing + "</td>";
        stReturn +=
            "<td align=right>" + iTotFollowers + "</td>";
        stReturn +=
            "<td align=right>" + fPct + "%</td>";
        stReturn +=
            "</tr>";
      }

    } catch (Exception e)
    {
      this.stError += "<br>ERRPR Exception: " + e;
    }

    stReturn += "</table></center>";
    return stReturn;
  }

  public String searchAndFollow(
      String stValue)
  {
    String stReturn = "";
    int iRpp = 0;
    int iLoop = 0;
    String[] astCommands = null;
    try
    {
      //"L0: user  L1 # rpp L2: # of searches/LOOP L3: search"
      astCommands = stValue.trim().split("\n"); // 0= count, 1=question
      Twitter twitter = new Twitter(astCommands[0].trim(), getPassword(astCommands[0].trim())); // login to service
      Query query = new Query("source:twitter4j " + astCommands[3].trim());

      iRpp =
          Integer.parseInt(astCommands[1].trim());
      iLoop =
          Integer.parseInt(astCommands[2].trim());
      stReturn +=
          "<table border=1><tr><th colspan=6><h1>Following User: " + astCommands[0].trim() +
          " rpp: " + iRpp + " loop: " + iLoop + " Search: " + astCommands[3].trim() +
          "</th></tr><tr><th>#</th><th>User</th><th>Date</th><th>Tweet</th><th>MyTwitterId</th><th>Comment</th></tr>";
      Twitter twitterSearch = new Twitter();
      for (int iL = 1; iL <=
          iLoop && iExit <= 0; iL++)
      {
        query.setRpp(iRpp);
        query.setQuery(astCommands[3].trim());
        twitterSearch.setSearchBaseURL("http://search.twitter.com/");
        query.setPage(iL);
        QueryResult result = twitterSearch.search(query);
        iSearch++;

        for (Tweet tweet : result.getTweets())
        {
          iTot++;
          stReturn +=
              addTwUserAndFollow(tweet.getFromUser(), "S", tweet.getCreatedAt().toString(),
              tweet.getText(), twitter);
          if (iExit > 0)
            break;
        }

      }
    } catch (Exception e)
    {
      this.stError += "<br>ERRPR Exception: " + e;
    }

    stReturn += "</table><br>END: Searches: " + iSearch + " Total returns: " + iTot + " Following: " + iFollow + "<br>";
    String stSql = "insert into MyTwitterLog (dtAction,stLogin,nmActionType,stSearch,nmRpp,nmLoop," +
        "nmSearches,nmTotProc,nmFollowing,stError,stReport,stIp)" +
        "values(now(),\"" + astCommands[0].trim() + "\",1,\"" + astCommands[3].trim() + "\"," + iRpp + "," + iLoop +
        "," + iSearch + "," + iTot + "," + iFollow + "," + this.ebEnt.dbEnterprise.fmtDbString(this.stError) + "," +
        this.ebEnt.dbEnterprise.fmtDbString(stReturn) + ",\"" + this.ebEnt.ebUd.request.getRemoteAddr() + "\")";
    this.ebEnt.dbEnterprise.ExecuteUpdate(stSql);
    return stReturn;
  }

  public String addTwUserAndFollow(
      String stTwUser, String stType, String stDate, String stText, Twitter twitter)
  {
    String stReturn = "";

    stReturn +=
        "<tr><td align=right>" + this.iTot + "</td>";
    stReturn +=
        "<td align=left>" + stTwUser + "</td>";
    stReturn +=
        "<td align=left>" + stDate + "</td>";
    stReturn +=
        "<td align=left>" + stText + "</td>";

    int nmMyTwitterId = this.ebEnt.dbEnterprise.ExecuteSql1n("select nmMyTwitterId from MyTwitterUser " +
        "where stTwitterId=\"" + stTwUser + "\"");
    if (nmMyTwitterId <= 0)
    {
      nmMyTwitterId = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(nmMyTwitterId) from MyTwitterUser");
      nmMyTwitterId++;

      String stTemp1 = "";
      String stTemp2 = "";
      if (stType.equals("S"))
      {
        stTemp1 = ",stUserType";
        stTemp2 =
            ",'S'";
      } else if (stType.equals("L"))
      {
        stTemp1 = ",stUserType=";
        stTemp2 =
            ",'L'";
      }

      this.ebEnt.dbEnterprise.ExecuteUpdate("insert into " +
          "MyTwitterUser (nmMyTwitterId,stTwitterId,dtEntered" + stTemp1 + ")" +
          "value(" + nmMyTwitterId + ",\"" + stTwUser + "\",now()" + stTemp2 + ")");
      stReturn +=
          "<td align=right><b>" + nmMyTwitterId + "</b></td>";
    } else
    {
      stReturn += "<td align=right><i>" + nmMyTwitterId + "</i></td>";
    }

    int nmCount = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from MyTwitterFollowing " +
        "where nmLoginId=" + nmLoginId + " and nmFollowingId=" + nmMyTwitterId);
    if (nmCount <= 0)
    {
      this.ebEnt.dbEnterprise.ExecuteUpdate("insert into MyTwitterFollowing" +
          "(nmLoginId,nmFollowingId,dtEntered)" +
          "value(" + nmLoginId + "," + nmMyTwitterId + ",now())");
      try
      {
        twitter.createFriendship(stTwUser, true);
        iFollow++;

        stReturn +=
            "<td align=right>ok " + iFollow + "</td>";
      } catch (twitter4j.TwitterException e)
      {
        stReturn += "<td align=right>" + e.getMessage() + "</td>";
        this.stError += "<br>ERRPR TwitterException: " + e.getMessage();

        /*ERRPR TwitterException: 403:The request is understood, but it has been refused.
        An accompanying error message will explain why. /friendships/create/Princess_aFlor.xml
        Could not follow user: You are unable to follow more people at this time.
        Learn more <a href="http://help.twitter.com/forums/10713/entries/66885">here</a>. */
        if (e.getMessage().indexOf("unable to follow more") >= 0)
        {
          this.ebEnt.dbEnterprise.ExecuteUpdate("update MyTwitterUser set dtFollowLimit=now() where stTwitterId=\"" + this.stMyTwitterId + "\"");
          iExit++;

        } else if (e.getMessage().indexOf("already") <= 0)
        {
          iExit++;
        }

      } catch (Exception e)
      {
        this.stError += "<br>ERRPR Exception: " + e;
        stReturn +=
            "<td align=right>" + e.getMessage() + "</td>";
        iExit++;

      }


    } else
    {
      stReturn += "<td align=right><i>already</i></td>";
    }

    stReturn += "</tr>";
    return stReturn;
  }

  public String sendTweet(
      String stId, String stMessage)
  {
    String stReturn = "";
    try
    {
      Twitter twitter = new Twitter(stId, getPassword(stId)); // login to service
      twitter.updateStatus(stMessage); // update your status
      stReturn +=
          "ok";
    } catch (twitter4j.TwitterException e)
    {
      this.stError += "<br>ERRPR sendStatus: " + e;
      stReturn +=
          "err";
    }

    return stReturn;
  }
  /* OK
  //OK twitter.sendDirectMessage("roberteder2", "working on the final release for monday");
  List<Status> statuses = twitter.getFriendsTimeline();
  System.out.println("Showing friends timeline.");
  for (Status status : statuses)
  {
  System.out.println(status.getUser().getName() + ":" +
  status.getText());
  }
   */
}
