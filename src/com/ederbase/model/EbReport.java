/*
http://netbeansboy.com/2008/04/17/netbeans-61-jasper-reports-a-marriage-made-in-heaven-or-somewhere-else-nice/
 */
package com.ederbase.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.Map;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;

/**
 *
 * @author Administrator
 */
public class EbReport
{

  String stError = "";
  EbEnterprise ebEnt;

  public EbReport(EbEnterprise ebEnt)
  {
    this.stError = "";
    this.ebEnt = ebEnt;
  }

  public String getError()
  {
    return this.stError;
  }

  public String getReport(HttpServletRequest request, HttpServletResponse response)
  {
    String stReturn = "";
    String stName = "report1.jrxml";
    try
    {
      ResultSet rs = ebEnt.dbEnterprise.ExecuteSql("select * from X25Person limit 10");
      Map fillParams = new HashMap();
      jasperReport(stName, "text/html", rs, fillParams, request, response);
    } catch (Exception e)
    {
      this.stError += "<br>ERROR getReport " + stName + ": " + e;
    }
    return stReturn;
  }

  /**
   * <p>Generate the specified report, in the specified output
   * format, based on the specified data.</p>
   *
   * @param name Report name to be rendered
   * @param type Content type of the requested report ("application/pdf"
   *  or "text/html")
   * @param data <code>ResultSet</code> containing the data to report
   * @param params <code>Map</code> of additional
   *   report parameters
   *
   * @exception IllegalArgumentException if the specified
   *  content type is not recognized
   * @exception IllegalArgumentException if no compiled report definition
   * for the specified name can be found
   */
  public void jasperReport(String name, String type, ResultSet data, Map params,
      HttpServletRequest request, HttpServletResponse response)
  {
    // Validate that we recognize the report type
    // before potentially wasting time filling the
    // report with data
    String[] VALID_TYPES =
    {
      "application/pdf", "text/html", "application/rtf"
    };
    boolean found = false;
    for (int i = 0; i < VALID_TYPES.length; i++)
    {
      if (VALID_TYPES[i].equals(type))
      {
        found = true;
        break;
      }
    }
    if (!found)
    {
      stError += "<BR>ERROR: NET ";
    }

    // Look up the compiled report design resource
    String PREFIX = "C:\\dev\\ebview\\web\\";
    String SUFFIX = "";
    InputStream stream = null;
    try
    {
      stream = new FileInputStream(PREFIX + name + SUFFIX);
    } catch (FileNotFoundException e)
    {
      this.stError += "<BR>ERROR: " + e;
    }
    try
    {
      data.beforeFirst();
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR: " + e;
    }

    // Fill the requested report with the specified data
    JRResultSetDataSource ds = new JRResultSetDataSource(data);
    JasperPrint jasperPrint = null;
    try
    {
      jasperPrint = JasperFillManager.fillReport(stream, params, ds);
    } catch (RuntimeException e)
    {
      this.stError += "<BR>ERROR: " + e;
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR: " + e;
    } finally
    {
      try
      {
        stream.close();
      } catch (IOException e)
      {
        this.stError += "<BR>ERROR: " + e;
      }
    }

    // Configure the exporter to be used, along with the custom
    // parameters specific to the exporter type
    JRExporter exporter = null;

    try
    {
      response.setContentType(type);
      if ("application/pdf".equals(type))
      {
        exporter = new JRPdfExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT,
            jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM,
            response.getOutputStream());
      } else if ("text/html".equals(type))
      {
        exporter = new JRHtmlExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT,
            jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_WRITER,
            response.getWriter());
        // Make images available for the HTML output
        request.getSession().setAttribute(
            ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE,
            jasperPrint);
        exporter.setParameter(
            JRHtmlExporterParameter.IMAGES_MAP, new HashMap());
        // The following statement requires mapping /image
        // to the imageServlet in the web.xml.
        //
        // This servlet serves up images including the px
        // images for spacing.
        //
        // Serve up the images directly so we
        // don't incur the extra overhead associated with
        // with a JSF request for a non-JSF entity.
        exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI,
            request.getContextPath() + "/image?image=");
      }
    } catch (RuntimeException e)
    {
      this.stError += "<BR>ERROR: " + e;
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR: " + e;
    }

    // Enough with the preliminaries ...
    // export the report already
    try
    {
      exporter.exportReport();
    } catch (RuntimeException e)
    {
      this.stError += "<BR>ERROR: " + e;
    } catch (Exception e)
    {
      this.stError += "<BR>ERROR: " + e;
    }

  }
}
