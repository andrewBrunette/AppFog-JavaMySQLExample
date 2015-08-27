/**
 * (c) 2015 CenturyLink Cloud
 *
 * @author Andrew Brunette
 * <p>
 * This class is example code for accessing the CenturyLink Mysql service from AppFog.  For the database connection to work, a
 * database "example" needs to exist in the instance.  You can use the "Get VcapVars" button on the index.html page
 * to get parameter values to use in a sql client to set up the database.
 * <p>
 * The code gets the environment variable "VCAP_SERVICES" where the connection parameters lie. There is commented out code
 * tht will let you test from a local IDE. If this code does not work locally, remove the values for the certificate, which contains
 * newlines, which seem to not work in some windows environments.
 * <p>
 * To properly work, it needs to run in your AppFog instance.  See accompanying documentation for assistance on the full life cycle.
 */


package com.centurylinkcloud.ecosystem;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*import org.apache.commons.io.IOUtils;*/
import org.json.JSONObject;
import org.json.JSONArray;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckConnection extends HttpServlet {
    private static final String PASS = "k3YSt0rEP@$s!";

    /**
     * This is a simple example of acquiring the service variables for the Mysql service,
     * and setting up and validating a connection
     */
    private JSONObject getEnvData() throws Exception {
        try {
            return new JSONObject(System.getenv("VCAP_SERVICES"));
        } catch (Exception e) {
            throw new Exception("Your VCAP_SERVICES environment variable is not set, indicating no service available");
        }
    }

    private String tryConnection(JSONObject parmList) {
        System.setProperty("javax.net.ssl.keyStore", getKeyStorePath());
        System.setProperty("javax.net.ssl.keyStorePassword", PASS);
        System.setProperty("javax.net.ssl.trustStore", getKeyStorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", PASS);

        JSONArray serviceData = parmList.getJSONArray("ctl_mysql");// fetch the credentials for the my_sql service
        JSONObject payload = serviceData.getJSONObject(0);
        JSONObject credentials = payload.getJSONObject("credentials");
        String host = credentials.getString("host");
        Integer port = credentials.getInt("port");
        String dbUser = credentials.getString("username");
        String dbPassword = credentials.getString("password");
        String database = credentials.getString("dbname");
        String certificate = credentials.getString("certificate");

        try {
            addCertToKeystore(host, certificate.getBytes());

            Class.forName("com.mysql.jdbc.Driver");
            String url = new StringBuilder("jdbc:mysql://").append(host).append(":").append(port)
                    .append("/").append(database).append("?")
                    .append("user=").append(dbUser)
                    .append("&password=").append(dbPassword)
                    .append("&useSSL=true").toString();
            Connection conn = DriverManager.getConnection(url);
            // At this point, have a connection, do something with it.  We'll do something simple...
            
            PreparedStatement preparedStatement = conn.prepareStatement("show status like 'Ssl_cipher%'");
            ResultSet rs = preparedStatement.executeQuery();
            StringBuilder output = new StringBuilder();
            output.append("Your connection has succeeded.  The ssl status is:");
            while (rs.next()) {
                output.append(rs.getString(1) + ":" + rs.getString(2) + "</br>");
            }
            rs.close();
            conn.close();
            return output.toString();
        } 
        catch (Exception e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }
        
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        JSONObject credentials = null;
        try {
            credentials = getEnvData();
        } catch (Exception e) {
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    private void addCertToKeystore(String host, byte[] certificate) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = getKeystore();

        String alias = host + "_client_cert";

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream certstream = new ByteArrayInputStream(certificate);
        Certificate certs = cf.generateCertificate(certstream);

        keyStore.setCertificateEntry(alias, certs);

        persistKeyStore(keyStore);
    }

    private KeyStore getKeystore () throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore;

        File keystoreFile = new File (getKeyStorePath());
        if (!keystoreFile.exists()) {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, PASS.toCharArray());

            persistKeyStore(keyStore);
        } else {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(keystoreFile), PASS.toCharArray());
        }

        return keyStore;
    }

    private String getKeyStorePath () {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        return baseDir + "/dbaas_keystore";
    }

    private void persistKeyStore(KeyStore keyStore) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        OutputStream outputStream = new FileOutputStream(new File(getKeyStorePath()));
        keyStore.store(outputStream, PASS.toCharArray());
    }
}
