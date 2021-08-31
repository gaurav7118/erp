<%-- 
    Document   : UpdateCNAmountInBase
    Created on : Nov 2, 2015, 12:01:45 PM
    Author     : krawler
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
//        String serverip = "192.168.0.94";
//        String port = "3306";
//        String dbName = "accounting_senwan291015";
//        String userName = "krawler";
//        String password = "krawler";
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
        String uri = request.getRequestURL().toString();
        String servletBase = request.getServletPath();
        String requestedURL = uri.substring(0, uri.indexOf(servletBase)) + "/CommonFunctions/updateAmountInBase.do";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement ps = null;
        ResultSet rs = null;
        String queryCompany = "";
        if (!StringUtil.isNullOrEmpty(subDomain)) {
            queryCompany = "select companyid, currency,subdomain from company where subdomain=?";
            ps = conn.prepareStatement(queryCompany);
            ps.setString(1, subDomain);
            rs = ps.executeQuery();
        } else {
            queryCompany = "select companyid,currency, subdomain from company";
            ps = conn.prepareStatement(queryCompany);
            rs = ps.executeQuery();
        }
        String companyid, subdomain, currencyid;
        if (rs != null) {
            while (rs.next()) {
                companyid = rs.getString("companyid");
                subdomain = rs.getString("subdomain");
                currencyid = rs.getString("currency");
                InputStream iStream = null;
                try {
                    URL u = new URL(requestedURL);
                    URLConnection uc = u.openConnection();
                    uc.setDoOutput(true);
                    uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
                    pw.print("companyid=" + companyid + "&currencyid=" + currencyid);
                    pw.close();
                    iStream = uc.getInputStream();
                    java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(iStream));
                    String res = URLDecoder.decode(in.readLine());
                    out.println("<br> Completed for Company - " + subdomain + " " + res + "</br>");
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
        }
    } catch (Exception ex) {
        out.println("Exception occuring while executing script - " + ex.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>