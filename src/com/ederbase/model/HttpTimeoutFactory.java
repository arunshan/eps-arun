package com.ederbase.model;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class HttpTimeoutFactory
  implements URLStreamHandlerFactory
{
  int fiTimeoutVal;

  public HttpTimeoutFactory(int iT)
  {
    this.fiTimeoutVal = iT;
  }

  public URLStreamHandler createURLStreamHandler(String str)
  {
    return new HttpTimeoutHandler(this.fiTimeoutVal);
  }
}