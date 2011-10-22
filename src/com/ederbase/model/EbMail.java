package com.ederbase.model;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EbMail
{
  private String stError = "";
  private int inProduction = 0;
  private String stTestEmail = "roberteder@myinfo.com";
  private String stHost = "myinfo.com";
  private String stAuth = "true";
  private String stFromName = "Robert Eder";
  private String stFromEmail = "roberteder@myinfo.com";
  private String stEmailLoginUser = "email@xxx.comm";
  private String stEMailPassword = "pwd";
  private EbEnterprise ebEnt = null;
  private String stBody = "";
  private String[] astUser = null;

  public EbMail(EbEnterprise ebEnt)
  {
    this.ebEnt = ebEnt;
    this.inProduction = 0;
  }

  public void setEbEmail(String stHost, String stAuth, String stFromName, String stFromEmail, String stEmailLoginUser, String stEMailPassword)
  {
    this.stHost = stHost;
    this.stAuth = stAuth;
    this.stFromName = stFromName;
    this.stFromEmail = stFromEmail;
    this.stEmailLoginUser = stEmailLoginUser;
    this.stEMailPassword = stEMailPassword;
  }

  public void setTestEmail(String stEmail)
  {
    this.stTestEmail = stEmail;
  }

  public void setProduction(int inProduction)
  {
    this.inProduction = inProduction;
  }

  public String getBody()
  {
    return this.stBody;
  }

  public int sendMail(String stTo, String stToName, String stSubject, String stBodyIn, int nmCampaignId)
  {
    int iPos = 0;
    int iDnc = 1;
    this.stBody = stBodyIn;
    try
    {
      if (stTo.trim().toLowerCase().startsWith("joeledfleiss"))
      {
        stTo = "jf@eppora.com";
      }
      iPos = stTo.indexOf("@");
      if (iPos > 0)
      {
        iDnc = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from X25DoNotCall where (stValue= " + this.ebEnt.dbEnterprise.fmtDbString(stTo) + " or stValue = " + this.ebEnt.dbEnterprise.fmtDbString(stTo.substring(iPos)) + ")");
      }
      if ((iPos > 0) && (iDnc == 0))
      {
        if (this.inProduction <= 0)
        {
          stTo = this.stTestEmail;
        }

        int nmUserId = this.ebEnt.ebNorm.getEmailId(stTo);
        Properties props = System.getProperties();

        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.host", this.stHost);

        props.put("mail.smtp.auth", this.stAuth);
        Authenticator auth = new SMTPAuthenticator(this.stEmailLoginUser, this.stEMailPassword);
        Session session = Session.getInstance(props, auth);

        Message msg = new MyMessage(session);

        msg.setFrom(new InternetAddress(this.stFromEmail, this.stFromName));
        Address addrTo = new InternetAddress(stTo.trim(), stToName.trim());
        msg.setRecipient(Message.RecipientType.TO, addrTo);
        msg.setSubject(stSubject);

        if (this.stBody.toLowerCase().indexOf("<html>") < 0)
        {
          msg.setText(this.stBody);
        }
        else
        {
          Multipart mp = new MimeMultipart("alternative");
          try
          {
            StringReader in = new StringReader(this.stBody);
            Html2Text parser = new Html2Text();
            parser.parse(in);
            in.close();
            BodyPart bp1 = new MimeBodyPart();
            bp1.setContent(parser.getText(), "text/plain; charset=UTF-8");
            mp.addBodyPart(bp1);
          }
          catch (Exception e) {
          }
          BodyPart bp2 = new MimeBodyPart();
          bp2.setContent(this.stBody.toString(), "text/html; charset=UTF-8");
          mp.addBodyPart(bp2);
          msg.setContent(mp);
        }
        msg.setHeader("X-Mailer", "MyInfo/V1");
        msg.setSentDate(new Date());
        Transport.send(msg);
        int iMaxComm = 0;
        if (nmCampaignId <= 0)
        {
          iMaxComm = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from X25Communications");
          iMaxComm++;
          this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25Communications (RecId,nmRefType,nmRefId,nmFlags,dtDate,nmOrigSize,stTitle,stContent) values(" + iMaxComm + ",42," + nmUserId + ",1," + this.ebEnt.dbEnterprise.getNowString() + "," + this.stBody.length() + "," + this.ebEnt.dbEnterprise.fmtDbString(stSubject) + "," + this.ebEnt.dbEnterprise.fmtDbString(this.stBody) + ")");
        }
        else
        {
          iMaxComm = 1;
        }
        return iMaxComm;
      }

      return -2;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.out.println("Exception " + ex);
      this.stError = (this.stError + "<BR>ERROR sendMail " + ex);
    }return -1;
  }

  public int sendMailCL(String stTo, String stToName, String stSubject, String stBodyIn, int nmCampaignId)
  {
    int iPos = 0;
    int iDnc = 1;
    int iCL = 0;
    this.stBody = stBodyIn;
    String stTemp = "";
    try
    {
      stTo = stTo.replace(" ", "");
      stTo = stTo.replace("\t", "");
      stTo = stTo.replace("\n", "");
      iPos = stTo.indexOf("@");
      if (iPos > 0)
      {
        iDnc = this.ebEnt.dbEnterprise.ExecuteSql1n("select count(*) from X25DoNotCall where (stValue= " + this.ebEnt.dbEnterprise.fmtDbString(stTo) + " or stValue = " + this.ebEnt.dbEnterprise.fmtDbString(stTo.substring(iPos)) + ")");
      }
      if (this.inProduction <= 0)
      {
        stTo = this.stTestEmail;
      }
      iCL = this.ebEnt.dbEnterprise.ExecuteSql1n("select nmApproveUserId from X25User where stEMail = \"" + stTo.trim() + "\" ");

      if ((iPos > 0) && (iDnc == 0) && (iCL < 4))
      {
        this.astUser = this.ebEnt.gastEmails[(this.ebEnt.giEmailIx++)].split("\\|");
        if (this.ebEnt.giEmailIx >= this.ebEnt.gastEmails.length)
        {
          this.ebEnt.giEmailIx = 0;
        }

        int nmUserId = this.ebEnt.ebNorm.getEmailId(stTo);
        Properties props = System.getProperties();

        props.put("mail.transport.protocol", "smtp");
        if (this.astUser[0].indexOf("@gmail.com") >= 0)
        {
          props.put("mail.smtp.starttls.enable", "true");
        }
        else {
          props.put("mail.smtp.starttls.enable", "false");
        }
        props.put("mail.smtp.host", this.astUser[2].trim());
        props.put("mail.smtp.auth", "true");
        if ((this.astUser.length > 3) && (this.astUser[3].length() > 0))
        {
          stTemp = this.astUser[3].trim();
        }
        else {
          stTemp = this.astUser[0].trim();
        }
        Authenticator auth = new SMTPAuthenticator(stTemp, this.astUser[1].trim());

        Session session = Session.getInstance(props, auth);

        Message msg = new MyMessage(session);

        msg.setFrom(new InternetAddress(this.astUser[0].trim(), "Robert Eder"));
        Address addrTo = new InternetAddress(stTo.trim(), stToName.trim());
        msg.setRecipient(Message.RecipientType.TO, addrTo);
        msg.setSubject(stSubject.trim());
        if (this.stBody.toLowerCase().indexOf("<html>") < 0)
        {
          msg.setText(this.stBody);
        }
        else
        {
          Multipart mp = new MimeMultipart("alternative");

          StringReader in = new StringReader(this.stBody.trim());
          Html2Text parser = new Html2Text();
          parser.parse(in);
          in.close();
          BodyPart bp1 = new MimeBodyPart();
          bp1.setContent(parser.getText().trim(), "text/plain; charset=UTF-8");
          mp.addBodyPart(bp1);

          BodyPart bp2 = new MimeBodyPart();
          bp2.setContent(this.stBody.toString().trim(), "text/html; charset=UTF-8");
          mp.addBodyPart(bp2);
          msg.setContent(mp);
        }

        msg.setSentDate(new Date());
        Transport.send(msg);
        this.ebEnt.dbEnterprise.ExecuteUpdate("update X25User set nmApproveUserId = (nmApproveUserId+1) where stEMail = \"" + stTo.trim() + "\" ");
        int iMaxComm = 0;
        if (nmCampaignId <= 0)
        {
          iMaxComm = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from X25Communications");
          iMaxComm++;
          this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25Communications (RecId,nmRefType,nmRefId,nmFlags,dtDate,nmOrigSize,stTitle,stContent) values(" + iMaxComm + ",42," + nmUserId + ",1," + this.ebEnt.dbEnterprise.getNowString() + "," + this.stBody.length() + "," + this.ebEnt.dbEnterprise.fmtDbString(stSubject) + "," + this.ebEnt.dbEnterprise.fmtDbString(this.stBody) + ")");
        }
        else
        {
          iMaxComm = 1;
        }
        return iMaxComm;
      }

      if (iDnc == 0)
      {
        return -3;
      }

      return -2;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      this.stError = (this.stError + "<BR>ERROR sendMailCL: " + ex);
    }return -1;
  }

  public int readEmail()
    throws Exception
  {
    int iReturn = 0;
    int nmUserId = 0;

    Properties props = new Properties();

    String host = "myinfo.com";
    String username = "robe@myinfo.com";
    String password = "test123";
    String provider = "pop3";

    Session session = Session.getDefaultInstance(props, null);
    Store store = session.getStore(provider);
    store.connect(host, username, password);

    Folder fDefault = store.getDefaultFolder();

    Folder[] aFolder = fDefault.list();
    for (int iF = 0; iF < aFolder.length; iF++)
    {
      Folder inbox = aFolder[iF];
      inbox.open(2);
      int iMaxComm = this.ebEnt.dbEnterprise.ExecuteSql1n("select max(RecId) from X25Communications");
      String[] aMessage = null;
      String stForeignRefId = "";
      String stTo = "";
      Message[] messages = inbox.getMessages();
      Message message = null;
      for (int iM = 0; iM < messages.length; iM++)
      {
        try
        {
          message = messages[iM];
          Address[] aaFrom = message.getFrom();
          for (int iA = 0; iA < aaFrom.length; iA++)
          {
            String[] astFrom = EbStatic.parseEmail(aaFrom[iA].toString());
            aMessage = getMessage(message);
            String stHeader = "";
            stForeignRefId = "";
            for (Enumeration e = message.getAllHeaders(); e.hasMoreElements(); )
            {
              Object o = e.nextElement();
              if ((o instanceof Header))
              {
                Header h = (Header)o;
                stHeader = stHeader + h.getName() + ":\t " + h.getValue() + "\n";
                if (h.getName().trim().toLowerCase().equals("message-id"))
                {
                  stForeignRefId = h.getValue();
                } else if (h.getName().trim().toLowerCase().equals("to"))
                {
                  stTo = h.getValue();
                }
              }
            }
            int nmFlags = 512;
            if (astFrom[0].toLowerCase().equals("rob@myinfo.com"))
            {
              if (stTo.length() > 3)
              {
                String[] astTo = EbStatic.parseEmail(stTo);
                astFrom[0] = astTo[0];
                astFrom[1] = astTo[1];
                nmFlags |= 1024;
              }
            }
            nmUserId = this.ebEnt.ebNorm.getEmailId(astFrom[0], astFrom[1]);
            if (nmUserId > 0)
            {
              iMaxComm++;
              String stSubject = message.getSubject();
              if (stSubject == null)
              {
                stSubject = "";
              }
              this.ebEnt.dbEnterprise.ExecuteUpdate("insert into X25Communications (RecId,nmRefType,nmRefId,nmFlags,dtDate,nmOrigSize,stTitle,stContent,stHeader,stForeignRefId,stHtml) values(" + iMaxComm + ",42," + nmUserId + "," + nmFlags + ",now()," + message.getSize() + "," + this.ebEnt.dbEnterprise.fmtDbString(stSubject) + "," + this.ebEnt.dbEnterprise.fmtDbString(aMessage[0]) + "," + this.ebEnt.dbEnterprise.fmtDbString(stHeader) + "," + this.ebEnt.dbEnterprise.fmtDbString(stForeignRefId) + "," + this.ebEnt.dbEnterprise.fmtDbString(aMessage[1]) + ")");
            }
            else
            {
              this.stError = (this.stError + "<br>ERROR readEmail: cannot add " + astFrom[0]);
            }
          }
          iReturn++;
        }
        catch (Exception e) {
          this.stError = (this.stError + "<BR>ERROR : readEmail [" + iM + "] " + e);
        }
        messages[iM].setFlag(Flags.Flag.DELETED, true);
      }
      inbox.close(true);
    }
    store.close();
    return iReturn;
  }

  public String getError()
  {
    return this.stError;
  }

  public String manageEmail()
  {
    String stReturn = "<h1>Email Manager</h1>";
    try
    {
      int iStatus = readEmail();
      stReturn = stReturn + "<br>readEmail: " + iStatus;
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR manageEmail: " + e);
      e.printStackTrace();
    }
    return stReturn;
  }

  public String[] getMessage(Message pMessage)
  {
    String[] aReturn = new String[2];
    aReturn[0] = "";
    aReturn[1] = "";
    try
    {
      Object content = pMessage.getContent();

      if ((content instanceof String))
      {
        aReturn[0] = ((String)content);
      } else if ((content instanceof Multipart))
      {
        aReturn = parseMultipart((Multipart)content, aReturn);
      }
    }
    catch (MessagingException e) {
      this.stError = (this.stError + "<BR>ERROR getMessage: " + e);
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    aReturn[0] = aReturn[0].trim();
    aReturn[1] = aReturn[1].trim();
    return aReturn;
  }

  public String[] parseMultipart(Multipart mPart, String[] aReturn)
  {
    try
    {
      for (int iB = 0; iB < mPart.getCount(); iB++)
      {
        BodyPart bp = mPart.getBodyPart(iB);
        if (!(bp instanceof MimeBodyPart))
          continue;
        MimeBodyPart mbp = (MimeBodyPart)bp;
        if ((mbp.getContent() instanceof Multipart))
        {
          aReturn = parseMultipart((Multipart)mbp.getContent(), aReturn);
        }
        else if (mbp.isMimeType("text/plain"))
        {
          int tmp79_78 = 0;
          String[] tmp79_77 = aReturn; tmp79_77[tmp79_78] = (tmp79_77[tmp79_78] + (String)mbp.getContent()); } else {
          if (!mbp.isMimeType("text/html"))
            continue;
          int tmp121_120 = 1;
          String[] tmp121_119 = aReturn; tmp121_119[tmp121_120] = (tmp121_119[tmp121_120] + (String)mbp.getContent());
        }
      }

    }
    catch (MessagingException me)
    {
      this.stError = (this.stError + "<br>ERROR parseMultipart MessagingException " + me);
      me.printStackTrace();
    }
    catch (IOException io) {
      this.stError = (this.stError + "<br>ERROR parseMultipart IOException " + io);
      io.printStackTrace();
    }
    return aReturn;
  }

  private void listSubFolders(Folder folder, boolean recurse) throws MessagingException
  {
    System.out.println("Folder " + folder.getFullName() + " type is " + folder.getType());
    if ((folder.getType() & 0x2) == 0)
    {
      System.out.println("Folder " + folder.getFullName() + " cannot contain subfolders.");
      return;
    }
    System.out.println("Listing sub folders of " + folder.getFullName());

    listFolders(folder.list(), recurse);
  }

  private void listFolders(Folder[] array, boolean recurse) throws MessagingException
  {
    for (int i = 0; i < array.length; i++)
    {
      Folder folder = array[i];
      System.out.println("Found folder " + folder.getFullName());
      if (!recurse)
        continue;
      listSubFolders(folder, recurse);
    }
  }

  public int sendMail(ResultSet rsUser, String stCampaignId, String stSubject, String stBodyIn)
  {
    int iReturn = -1;

    String stTo = "";
    String stToName = "";
    String stFirstName = "";
    String stMiddleName = "";
    String stLastName = "";

    this.stBody = stBodyIn;
    try
    {
      stTo = rsUser.getString("stEMail");
      if (stTo != null)
      {
        if (!stCampaignId.equals("-200"))
        {
          stFirstName = rsUser.getString("stFirstName");
          if (stFirstName == null)
          {
            stFirstName = "";
          }

          stMiddleName = rsUser.getString("stMiddleName");
          if (stMiddleName == null)
          {
            stMiddleName = "";
          }

          stLastName = rsUser.getString("stLastName");
          if (stLastName == null)
          {
            stLastName = "";
          }
        }
        if (stMiddleName.equals(""))
        {
          stToName = stFirstName + " " + stLastName;
        }
        else {
          stToName = stFirstName + " " + stMiddleName + " " + stLastName;
        }
        if (stToName.trim().equals(""))
        {
          int iPos = stTo.indexOf('@');
          if (iPos > 0)
          {
            stFirstName = stTo.substring(0, iPos);
          }
          else {
            stFirstName = stTo;
          }
          stToName = stFirstName;
        }
        this.stBody = this.stBody.replace("~~stFirstName", stFirstName.trim());
        this.stBody = this.stBody.replace("~~stMiddleName", stMiddleName.trim());
        this.stBody = this.stBody.replace("~~stLastName", stLastName.trim());
        this.stBody = this.stBody.replace("~~stEMailEncode", URLEncoder.encode(this.ebEnt.EBEncrypt(stTo)));
        this.stBody = this.stBody.replace("~~stEMail", stTo);
        this.stBody = this.stBody.replace("~~stFullName", stToName);
        this.stBody = this.stBody.replace("~~stCampaignId", stCampaignId);
        if (rsUser.getInt("nmPriviledge") > 0)
        {
          iReturn = sendMail(stTo, stToName, stSubject, "<html><body>" + this.stBody + "</body></html>", Integer.parseInt(stCampaignId));
        }
        else
          this.stError = (this.stError + "<BR>ERROR sendMail: NOT AN ACTIVE USER " + stTo);
      }
      else
      {
        this.stError += "<BR>ERROR sendMail: NO EMAIL ";
      }
    }
    catch (Exception e) {
      this.stError = (this.stError + "<BR>ERROR sendMail: " + e);
    }
    return iReturn;
  }

  private class MyMessage extends MimeMessage
  {
    protected MyMessage(Session session)
    {
      super(session);
    }

    protected void updateMessageID()
      throws MessagingException
    {
      Calendar cal = Calendar.getInstance();
      long now = cal.getTimeInMillis();
      setHeader("Message-ID", "001d0f9d0$6386ed70$@" + now);
    }
  }

  private class SMTPAuthenticator extends Authenticator
  {
    private String stU = "";
    private String stP = "";

    public SMTPAuthenticator(String stU, String stP)
    {
      this.stU = stU;
      this.stP = stP;
    }

    public PasswordAuthentication getPasswordAuthentication()
    {
      return new PasswordAuthentication(this.stU, this.stP);
    }
  }
}