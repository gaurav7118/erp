<%-- 
    Document   : PostInventoryJEForNonSaleItem
    Created on : Aug 22, 2017, 8:15:20 AM
    Author     : swapnil.khandre
--%>

<%@page import="java.net.*"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
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
        String subDomain = StringUtil.isNullOrEmpty(request.getParameter("subdomain")) ? "" : request.getParameter("subdomain");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":3306" + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
        String uri = request.getRequestURL().toString();
        String servletBase = request.getServletPath();
        String requestedURL = uri.substring(0, uri.indexOf(servletBase)) + "/CommonFunctions/postInventoryJEForNonSaleItem.do";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        InputStream iStream = null;
        try {
            URL u = new URL(requestedURL);
            URLConnection uc = u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
            pw.print("subdomain=" + subDomain);
            pw.close();
            iStream = uc.getInputStream();
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(iStream));
            String res = in.readLine();
            out.println("<br>" + res + "</br>");
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
    } catch (Exception ex) {
        out.println("Exception occuring while executing script - " + ex.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>