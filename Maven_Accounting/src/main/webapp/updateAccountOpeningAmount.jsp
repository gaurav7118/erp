<%-- 
    Document   : AdjustRoundingDifference
    Created on : 5 Jan, 2016, 1:45:50 PM
    Author     : krawler
--%>

<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
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
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        String endDate = request.getParameter("enddate");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(endDate)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password,enddate) in url. so please provide all these parameter correctly. ");
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date enddate = sdf.parse(endDate);
        } catch (Exception ex) {
            String msg = "</br>Invalid Value given for enddate:  " + endDate + "."
                    + "</br>Please give value in yyyy-MM-dd format only.";
            throw new Exception(msg);
        }

        String uri = request.getRequestURL().toString();
        String servletBase = request.getServletPath();
        String requestedURL = uri.substring(0, uri.indexOf(servletBase)) + "/CommonFunctions/updateAccountOpeningBalance.do";
        InputStream iStream = null;
        try {
            URL u = new URL(requestedURL);
            URLConnection uc = u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
            if (!StringUtil.isNullOrEmpty(subDomain)) {
                pw.print("subdomain=" + subDomain + "&enddate=" + endDate + "&dbname=" + dbName);
            } else {
                pw.print("enddate=" + endDate + "&dbname=" + dbName);
            }
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