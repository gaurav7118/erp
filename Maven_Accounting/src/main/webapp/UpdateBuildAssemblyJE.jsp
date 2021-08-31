<%-- 
    Document   : 
    Created on : 5 Jan, 2016, 1:45:50 PM
    Author     : swapnil.khandre
--%>

<%@page import="java.io.IOException"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.Connection"%>
<%
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = StringUtil.isNullOrEmpty(request.getParameter("subdomain")) ? "" : request.getParameter("subdomain");
        String isBuild = request.getParameter("isBuild");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":3306" + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
        String uri = request.getRequestURL().toString();
        String servletBase = request.getServletPath();
        String requestedURL = uri.substring(0, uri.indexOf(servletBase)) + "/CommonFunctions/updateBuildAssemblyJE.do";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);

        InputStream iStream = null;
        try {
            URL u = new URL(requestedURL);
            URLConnection uc = u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
            pw.print("subdomain=" + subDomain + "&isBuild=" + isBuild);
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