
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<%
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String moduleName = request.getParameter("module");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":3306/";

        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String servletBase = request.getServletPath();
        String path = "/CommonFunctions/updateQuotationAmount.do";              // Update Customer Quotation
            if (moduleName != null && moduleName.equals("vendorquotation")) {
                path = "/CommonFunctions/updateVendorQuotationAmount.do";           // Update Vendor Quotation
            } else if(moduleName != null && moduleName.equals("salesandpurchaseorder")){
                path = "/CommonFunctions/updateAmountInPOandSO.do";  
            }
        String requestedURL = uri.substring(0, uri.indexOf(servletBase)) + path;
        String driver = "com.mysql.jdbc.Driver";
        String companyId = "";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String querycash = "select companyid, subdomain from company";
        PreparedStatement stmtcash = conn.prepareStatement(querycash);
        ResultSet rscash = stmtcash.executeQuery();
        while (rscash.next()) {
            companyId = rscash.getString("companyid");
            InputStream iStream = null;
            try {
                URL u = new URL(requestedURL);
                URLConnection uc = u.openConnection();
                uc.setDoOutput(true);
                uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
                pw.print("companyid=" + companyId );
                pw.close();
                iStream = uc.getInputStream();
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(iStream));
                String res = URLDecoder.decode(in.readLine());
                out.println("<br> Completed for Company - " + rscash.getString("subdomain") + " " + res + "</br>");
                in.close();
                iStream.close();
            } catch (IOException iex) {
                iex.printStackTrace();
            } finally {
                if (iStream != null) {
                    try {
                        iStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        stmtcash.close();
        rscash.close();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>
