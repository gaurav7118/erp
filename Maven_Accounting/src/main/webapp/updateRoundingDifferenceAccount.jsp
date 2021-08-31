<%-- 
    Document   : updateRoundingDifferenceAccount
    Created on : 4 Mar, 2016, 1:57:16 PM
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
        String requestedURL = uri.substring(0, uri.indexOf(servletBase)) + "/CommonFunctions/adjustRoundingDifference.do";
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
        String companyid, subdomain;
        if (rs != null) {
            while (rs.next()) {
                companyid = rs.getString("companyid");
                subdomain = rs.getString("subdomain");
                try {
                    String query3 = "select id from account where company =? and name='Rounding Difference'";
                    PreparedStatement stmt3 = conn.prepareStatement(query3);
                    stmt3.setString(1, companyid);
                    ResultSet rs3 = stmt3.executeQuery();
                    if (rs3.next()) {
                            String roundingDiffAccount = rs3.getString("id");
                            String queryForVendor = " UPDATE compaccpreferences SET roundingDifferenceAccount=? WHERE  id= ? ";
                            PreparedStatement stmt2 = conn.prepareStatement(queryForVendor);
                            stmt2.setString(1, roundingDiffAccount);
                            stmt2.setString(2, companyid);
                            stmt2.executeUpdate();
                            out.println("<br> Completed for Company - " + subdomain + "</br>");
                        }
                } catch (Exception iex) {
                    iex.printStackTrace();
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

