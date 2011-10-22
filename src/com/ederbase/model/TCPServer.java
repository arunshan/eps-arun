/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ederbase.model;

import java.io.*;
import java.net.*;

/**
 *
 * @author Rob
 */
public class TCPServer
{
  ServerSocket srvr = null;
  Socket skt = null;
  PrintWriter out = null;

  public TCPServer(int iPort)
  {
    try
    {
      srvr = new ServerSocket(iPort);
      skt = srvr.accept();
      System.out.print("Server has connected on port: " + iPort + " !\n");
      out = new PrintWriter(skt.getOutputStream(), true);
    } catch (Exception e)
    {
      System.out.print("Whoops! It didn't workon port: " + iPort + " !" + e);
    }
  }

  public int sendData(String stData)
  {
    int iReturn = -1;
    try
    {
      System.out.print("Sending string: '" + stData + "'\n");
      out.print(stData);
      out.close();
      skt.close();
      srvr.close();
    } catch (Exception e)
    {
      System.out.print("Whoops! It didn't work!\n" + e);
    }
    return iReturn;
  }

  public int close()
  {
    int iReturn = -1;
    try
    {
      if (out != null)
      {
        out.close();
        iReturn++;
      }
      if (skt != null)
      {
        skt.close();
        iReturn++;
      }
      if (srvr != null)
      {
        srvr.close();
        iReturn++;
      }
    } catch (Exception e)
    {
      System.out.print("Whoops! It didn't work!\n" + e);
    }
    return iReturn;
  }
}

