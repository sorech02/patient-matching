package org.openimmunizationsoftware.pm.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.openimmunizationsoftware.pm.model.Patient;
import org.openimmunizationsoftware.pm.model.User;

/**
 * Allows for download of patient data in HL7 format for the CDC project.
 * 
 * @author Nathan Bunker
 * 
 */
public class DownloadHL7Servlet extends HomeServlet
{

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("text/html");
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    HttpSession session = req.getSession(true);
    User user = (User) session.getAttribute(TestSetServlet.ATTRIBUTE_USER);
    try {
      out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"> ");
      out.println("<html>");
      out.println("  <head>");
      out.println("    <title>Download HL7 Servlet</title>");
      out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"index.css\" />");
      out.println("    <script>");
      out.println("      function toggleLayer(whichLayer) ");
      out.println("      {");
      out.println("        var elem, vis;");
      out.println("        if (document.getElementById) ");
      out.println("          elem = document.getElementById(whichLayer);");
      out.println("        else if (document.all) ");
      out.println("          elem = document.all[whichLayer] ");
      out.println("        else if (document.layers) ");
      out.println("          elem = document.layers[whichLayer]");
      out.println("        vis = elem.style;");
      out.println("        if (vis.display == '' && elem.offsetWidth != undefined && elem.offsetHeight != undefined) ");
      out.println("          vis.display = (elem.offsetWidth != 0 && elem.offsetHeight != 0) ? 'block' : 'none';");
      out.println("        vis.display = (vis.display == '' || vis.display == 'block') ? 'none' : 'block';");
      out.println("      }");
      out.println("    </script>");
      out.println("  </head>");
      out.println("  <body>");
      makeMenu(out, user);
      out.println("    <h1>Download HL7 Servlet</h1>");

      SessionFactory factory = getSessionFactory();
      Session dataSession = factory.openSession();

      Query query = dataSession.createQuery("from Patient");
      List<Patient> patientList = query.list();

      out.println("    <pre>");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
      String today = sdf.format(new Date());
      int count = 0;
      for (Patient patient : patientList) {
        out.print("MSH|^~\\&|||||" + today + "||VXU^V04^VXU_V04|" + today + "-" + count + "|P|2.5.1|\r");
        out.print("PID|1||" + patient.getPatientId() + "^^^PM^MR||" + patient.getNameLast() + "^"
            + patient.getNameFirst() + "^" + patient.getNameMiddle() + "^" + patient.getNameSuffix() + "^^^L" + "|"
            + patient.getMotherMaidenName() + "|" + patient.getBirthDate() + "|" + patient.getGender() + "|\r");
        if (!patient.getMotherNameFirst().equals("") || !patient.getMotherNameLast().equals("")) {
          out.print("NK1|1|" + patient.getMotherNameLast() + "^" + patient.getMotherNameFirst() + "^"
              + patient.getMotherNameMiddle() + "|MTH^Mother^HL70063|\r");
        }
        if (!patient.getVacCode().equals("")) {
          out.print("RXA|0|1|" + patient.getVacDate() + "|" + patient.getVacDate() + "|" + patient.getVacCode()
              + "^^CVX|999|||01^Historical^NIP0001||||||||" + patient.getVacMfr() + "^^MVX||||A|\r");
        }

      }

      dataSession.close();

      out.println("    </pre>");
      out.println("  </body>");
      out.println("</fhtml>");
    } catch (Exception e) {
      out.print("<pre>");
      e.printStackTrace(out);
      out.print("</pre>");
    } finally {
      out.close();
    }
  }

  private static SessionFactory factory;

  public static SessionFactory getSessionFactory() {
    if (factory == null) {

      factory = new AnnotationConfiguration().configure().buildSessionFactory();

    }
    return factory;
  }

  // dataStoreDir
}
