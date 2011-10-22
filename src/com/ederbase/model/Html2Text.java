package com.ederbase.model;

import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.*;
import javax.swing.text.html.HTML.*;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

public class Html2Text extends HTMLEditorKit.ParserCallback
{
  StringBuffer s;

  public void parse(Reader in)
    throws IOException
  {
    this.s = new StringBuffer();
    ParserDelegator delegator = new ParserDelegator();
    delegator.parse(in, this, Boolean.TRUE.booleanValue());
  }

  public void handleText(char[] text, int pos)
  {
    this.s.append(text);
  }

  public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position)
  {
    if ((tag == HTML.Tag.P) || (tag == HTML.Tag.H1) || (tag == HTML.Tag.H2))
    {
      this.s.append("\r\n\r\n");
    } else if (tag == HTML.Tag.BR)
    {
      this.s.append("\r\n");
    } else if ((tag == HTML.Tag.P) || (tag == HTML.Tag.LI) || (tag == HTML.Tag.H1) || (tag == HTML.Tag.H2) || (tag == HTML.Tag.TD) || (tag == HTML.Tag.A))
    {
      this.s.append("\r\n");
    }
    parseAttributes(tag, attributes);
  }

  public void handleEndTag(HTML.Tag tag, int position)
  {
    if (tag == HTML.Tag.BR)
    {
      this.s.append("\r\n");
    }
  }

  public void handleEmptyTag(HTML.Tag tag, int position)
  {
    if (tag == HTML.Tag.BR)
    {
      this.s.append("\r\n");
    }
  }

  private void parseAttributes(HTML.Tag tag, MutableAttributeSet attributes)
  {
    Enumeration e = attributes.getAttributeNames();
    while (e.hasMoreElements())
    {
      Object name = e.nextElement();
      String value = (String)attributes.getAttribute(name);
      if (name == HTML.Attribute.HREF)
      {
        this.s.append("\r\n" + value + "\r\n");
      }
    }
  }

  public String getText()
  {
    return this.s.toString() + "\r\n\r\n";
  }
}