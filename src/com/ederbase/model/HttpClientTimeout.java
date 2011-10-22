package com.ederbase.model;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import sun.net.www.http.HttpClient;
import sun.net.www.http.KeepAliveCache;

public class HttpClientTimeout extends HttpClient
{
  public HttpClientTimeout(URL url, String proxy, int proxyPort)
    throws IOException
  {
    super(url, proxy, proxyPort);
  }

  public HttpClientTimeout(URL url) throws IOException
  {
    super(url, (String)null, -1);
  }

  public void SetTimeout(int i) throws SocketException
  {
    this.serverSocket.setSoTimeout(i);
  }

  public static HttpClientTimeout GetNew(URL url)
    throws IOException
  {
    HttpClientTimeout ret = (HttpClientTimeout)kac.get(url);
    if (ret == null)
    {
      ret = new HttpClientTimeout(url);
    }
    else {
      ret.url = url;
    }

    return ret;
  }

  public void Close() throws IOException
  {
    this.serverSocket.close();
  }

  public Socket GetSocket()
  {
    return this.serverSocket;
  }
}