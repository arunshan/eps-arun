package com.ederbase.model;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import sun.net.www.protocol.http.Handler;

public class HttpTimeoutHandler extends Handler
{
  int fiTimeoutVal;
  HttpURLConnectionTimeout fHUCT;

  public HttpTimeoutHandler(int iT)
  {
    this.fiTimeoutVal = iT;
  }

  protected URLConnection openConnection(URL u)
    throws IOException
  {
    return this.fHUCT = new HttpURLConnectionTimeout(u, this, this.fiTimeoutVal);
  }

  String GetProxy()
  {
    return this.proxy;
  }

  int GetProxyPort()
  {
    return this.proxyPort;
  }

  public void Close() throws Exception
  {
    this.fHUCT.Close();
  }

  public Socket GetSocket()
  {
    return this.fHUCT.GetSocket();
  }
}