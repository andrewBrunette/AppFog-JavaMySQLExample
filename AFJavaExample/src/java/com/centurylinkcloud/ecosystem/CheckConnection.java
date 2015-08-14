/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.centurylinkcloud.ecosystem;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.TreeMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONArray;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;


/**
 *
 * @author Andrew Brunette
 */
public class CheckConnection extends HttpServlet {

    /**
     * This is a simple example of acquiring the service variables for the Mysql service, 
     * and setting up and validating a connection 
     * 
     */
    private JSONObject getEnvData(){
        /* return new JSONObject(System.getenv("VCAP_SERVICES"));*/
        String envVars = "{\"ctl_mysql\": [ {  \"credentials\": {\"password\": \"y1Z5XmGrctqLgK1S\"," +
"    \"certificate\": \"-----BEGIN CERTIFICATE---------END CERTIFICATE-----\",\n" +
"    \"url\": \"mysql://66.151.15.159:49162\"," +
"    \"username\": \"admin\"" +
"  },\n" +
"  \"name\": \"my_instance\"," +
"  \"label\": \"ctl_mysql\"," +
"  \"plan\": \"free\"," +
"  \"tags\": []" +
"}]}";
        JSONObject creds =  new JSONObject(envVars);
        return creds;
    }
    
    private String tryConnection(JSONObject parmList){
        JSONArray serviceData = parmList.getJSONArray("ctl_mysql");// fetch the credentials for the my_sql service
        JSONObject payload = serviceData.getJSONObject(0);
        JSONObject credentials = payload.getJSONObject("credentials");
        String connURL = new String(credentials.getString("url")); // will be of form: mysql://66.99.99.159:88888. Note the port
        String dbUser = new String(credentials.getString("username"));
        String dbPassword = new String(credentials.getString("password"));
        String database = new String("example1"); // this is set up by an outside script, which needs to be executed before this will work
        Connection conn = null;
        
        try {
            
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:" + connURL + "/" + database + "?" +
                                   "user=" +dbUser + "&password=" + dbPassword); 
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
        JSONObject credentials = getEnvData();
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

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
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
