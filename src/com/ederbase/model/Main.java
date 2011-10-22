package com.ederbase.model;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Calendar;

public class Main
{
  static int indentLevel = -1;
  static EbEnterprise ebEnt = null;

  static void listPath(File path)
  {
    indentLevel += 1;
    int nmFileId = 0;
    String stSql = "";

    File[] files = path.listFiles();

    Arrays.sort(files);
    int i = 0; for (int n = files.length; i < n; i++)
    {
      if (files[i].isDirectory())
      {
        listPath(files[i]);
      }
      else {
        stSql = "select nmFileId from MyFiles where stPath=" + ebEnt.dbEnterprise.fmtDbString(files[i].getParent()) + " and stFile=" + ebEnt.dbEnterprise.fmtDbString(files[i].getName());

        nmFileId = ebEnt.dbEnterprise.ExecuteSql1n(stSql);
        if (nmFileId > 0)
          continue;
        nmFileId = ebEnt.dbEnterprise.ExecuteSql1n("select max(nmFileId) from MyFiles ");
        nmFileId++;
        stSql = "insert into MyFiles (nmFileId,stPath,stFile,nmLastModified,nmSize) values(" + nmFileId + "," + ebEnt.dbEnterprise.fmtDbString(files[i].getParent()) + "," + ebEnt.dbEnterprise.fmtDbString(files[i].getName()) + "," + files[i].lastModified() + "," + files[i].length() + ")";

        ebEnt.dbEnterprise.ExecuteUpdate(stSql);
      }
    }

    indentLevel -= 1;
  }

  public static void main(String[] args)
  {
    String stHTML = "";
    Calendar cal1 = Calendar.getInstance();
    long lTime1 = cal1.getTimeInMillis();
    try
    {
      System.out.println("EderBaseModel Application starting: ");
      ebEnt = new EbEnterprise(1, 26, "ederbase", "ebenterprise", "common");
      EbCraigsList ebCl = new EbCraigsList(ebEnt);
      for (int iA = 0; iA < args.length; iA++)
      {
        stHTML = stHTML + "\nargs[" + iA + "]: " + args[iA];
        if (args[iA].equals("a"))
        {
          EbMail ebMmail = new EbMail(ebEnt);
          stHTML = stHTML + "\n" + ebMmail.manageEmail();
          stHTML = stHTML + ebMmail.getError();
        } else if (args[iA].equals("b"))
        {
          stHTML = stHTML + "\n" + ebEnt.ebAdmin.fixCommHistory();
        } else if (args[iA].equals("c"))
        {
          stHTML = stHTML + "\n" + ebEnt.ebAdmin.processHourly(4);
        } else if (args[iA].equals("d"))
        {
          stHTML = stHTML + "\n" + ebCl.readCL(1);
          stHTML = stHTML + ebCl.getError();
        } else if (args[iA].equals("e"))
        {
          stHTML = stHTML + "\n" + ebCl.setEmail();
          stHTML = stHTML + ebCl.getError();
        } else if (args[iA].equals("f"))
        {
          EbCampaignManager ebCamp = new EbCampaignManager(ebEnt, null);
          stHTML = stHTML + ebCamp.emailCampaignBlast(5, 2000, "zzzz", 100, 120000);
        } else if (args[iA].equals("g"))
        {
          iA++;
          listPath(new File(args[iA]));
        } else if (args[iA].equals("u"))
        {
          UDPServer udpServer = new UDPServer();
          iA++;
          int iPort = Integer.parseInt(args[iA]);
          udpServer.runServer(iPort);
        } else if (args[iA].equals("s"))
        {
          UDPClient udpClient = new UDPClient();
          iA++;
          int iPort = Integer.parseInt(args[iA]);
          iA++;
          udpClient.sendUdp(args[iA], iPort);
        }
        else {
          stHTML = stHTML + "\n\n NOT YET PROCESSING: " + args[iA];
        }
      }
    }
    catch (Exception e) {
      System.out.println("Main - exception: " + e);
    }
    System.out.println(stHTML.replace("<br>", "\n"));
    Calendar cal2 = Calendar.getInstance();
    long lTime2 = cal2.getTimeInMillis();
    System.out.println("EderBaseModel - EXITING -  Duration (ms): " + (lTime2 - lTime1));
  }
}