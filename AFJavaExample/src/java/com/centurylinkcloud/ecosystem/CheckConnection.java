/**
 * (c) 2015 CenturyLink Cloud
 * @author Andrew Brunette
 * 
 * This class is example code for accessing the CenturyLink Mysql service from AppFog.  For the database connection to work, a 
 * database "example" needs to exist in the instance.  You can use the "Get VcapVars" button on the index.html page
 * to get parameter values to use in a sql client to set up the database.  
 * 
 * The code gets the environment variable "VCAP_SERVICES" where the connection parameters lie. There is commented out code 
 * tht will let you test from a local IDE. If this code does not work locally, remove the values for the certificate, which contains
 * newlines, which seem to not work in some windows environments.  
 * 
 * To properly work, it needs to run in your AppFog instance.  See accompanying documentation for assistance on the full life cycle. 
 */


package com.centurylinkcloud.ecosystem;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.json.JSONArray;
import java.sql.Connection;
import java.sql.DriverManager;

public class CheckConnection extends HttpServlet {

    /**
     * This is a simple example of acquiring the service variables for the Mysql service, 
     * and setting up and validating a connection 
     * 
     */
    private JSONObject getEnvData()throws Exception {
        try {
            return new JSONObject(System.getenv("VCAP_SERVICES"));
        }
        catch (Exception e){
            throw new Exception("Your VCAP_SERVICES environment variable is not set, indicating no service available");
        }   
    }
    
    private String tryConnection(JSONObject parmList){
        JSONArray serviceData = parmList.getJSONArray("ctl_mysql");// fetch the credentials for the my_sql service
        JSONObject payload = serviceData.getJSONObject(0);
        JSONObject credentials = payload.getJSONObject("credentials");
        String host = new String(credentials.getString("host")); 
        String port = new String (new Integer(credentials.getInt("port")).toString());
        String dbUser = new String(credentials.getString("username"));
        String dbPassword = new String(credentials.getString("password"));
        String database = new String("example"); // this is set up by an outside script, which needs to be executed before this will work
        Connection conn = null;
        
        try {
            
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + host +":" + port.toString() + "/" + database + "?" +
                                   "user=" +dbUser + "&password=" + dbPassword + "&useSSL=true"); 
            conn.close();
            return "Success";
        }
        catch (Exception e){
            return e.getMessage();
        }
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        JSONObject credentials = null;
        try {
            credentials = getEnvData();
        }
        catch (Exception e){
           try (PrintWriter out = response.getWriter()) {
                /* TODO output your page here. You may use following sample code. */
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"j_example.css\">");
                out.println("<head>");
                out.println("<title>CenturyLink Cloud Mysql Database Connection Check</title>");            
                out.println("</head>");
                out.println("<body>");
                out.println("Your VCAP_SERVICES environment variable is not defined, indicating that you do not have a database service setup yet");
                out.println("</body>");
                out.println("</html>");
            }
        }   
            
        String result = tryConnection(credentials);

        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"j_example.css\">");
            out.println("<head>");
            out.println("<title>CenturyLink Cloud Mysql Database Connection Check</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("Your result is " + result);
            out.println("</body>");
            out.println("</html>");
        }    
    }
        
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
