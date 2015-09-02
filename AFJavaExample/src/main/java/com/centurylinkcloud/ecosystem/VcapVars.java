/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.centurylinkcloud.ecosystem;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;


/**
 *
 * @author Andrew Brunette
 */
@WebServlet(urlPatterns = {"/VcapVars"})
public class VcapVars extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            
        String json = new String();
        JSONObject jObj = null;
        try {
            int spacesToIndentEachLevel = 2;
            json = new JSONObject(System.getenv("VCAP_SERVICES")).toString(spacesToIndentEachLevel);       
            
        }
        catch (Exception e) {
            /* for local testing */
            json = new String ("The environment variable 'VCAP_SERVICES' is not defined in your environment.  <br>You have not yet added a service to be connected to.");
        }
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"j_example.css\"");
            out.println("</head>");
            out.println("<body>");
            out.println("<div align=\"center\">");
            out.println("<h1>Your VCAP_Services environment variables are...</h1>");
            out.println("</div>");
            out.println("<div align=\"left\">");
            out.println("<pre>");
            out.println(json);
            out.println("</pre>");
            out.println("<br>");               
            out.println("</div>");
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
