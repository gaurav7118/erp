<%-- 
    Document   : updateAmountDueAndDiscount
    Created on : Nov 6, 2015, 8:15:21 PM
    Author     : krawler
--%>

<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
<%@page import="com.krawler.spring.authHandler.authHandler"%>
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
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(subDomain)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement ps = null;
        ResultSet rs = null;
        int receiptcount = 0;
        int paymentcount = 0;
        Set receiptIdSet = new HashSet();
        Set paymentIdSet = new HashSet();
        String queryCompany = "";
        String selectequery = "";
        queryCompany = "select companyid, currency,subdomain from company where subdomain=?";
        ps = conn.prepareStatement(queryCompany);
        ps.setString(1, subDomain);
        rs = ps.executeQuery();
        
        String companyid, subdomain, currencyid;
        if (rs != null) {
            while (rs.next()) {
                companyid = rs.getString("companyid");
                subdomain = rs.getString("subdomain");
                currencyid = rs.getString("currency");

                try {
                    /**
                     * for Advanced Receipt Amount due.
                     */
                    selectequery = "select id,amountdue from receiptadvancedetail where advancedetailid is null and company = ? and amountdue < 0.000001 and amountdue != 0.0";
                    ps = conn.prepareStatement(selectequery);
                    ps.setString(1, companyid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        String receiptid = rs.getString("id");
                        double amountdue = rs.getDouble("amountdue");
                        amountdue = authHandler.round(amountdue, companyid);
                        String updatequery = "update  receiptadvancedetail  set amountdue = " + amountdue + " where company = '" + companyid + "' and id = '" + receiptid + "'";
                        PreparedStatement updatestmt = conn.prepareStatement(updatequery);
                        updatestmt.execute();
                        receiptcount++;
                        receiptIdSet.add(receiptid);
                    }
                    
                    /**
                     * for Advanced Payment Amount due.
                     */
                    selectequery = "select id,amountdue from advancedetail where receiptadvancedetail is null and company = ? and amountdue < 0.000001 and amountdue != 0.0";
                    ps = conn.prepareStatement(selectequery);
                    ps.setString(1, companyid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        String paymentid = rs.getString("id");
                        double amountdue = rs.getDouble("amountdue");
                        amountdue = authHandler.round(amountdue, companyid);
                        String updatequery = "update  advancedetail  set amountdue = " + amountdue + " where company = '" + companyid + "' and id = '" + paymentid + "'";
                        PreparedStatement updatestmt = conn.prepareStatement(updatequery);
                        updatestmt.execute();
                        paymentcount++;
                        paymentIdSet.add(paymentid);
                    }
                    
                } catch (Exception iex) {
                    iex.printStackTrace();
                } 
            }
        }
        out.println("<center><br/>"+ receiptcount +" Data Updated for subdomain : <b>" + subDomain + "</b> <br/>");
        out.println("<br/>Advanced Receipt Amount Due Updated for : <br/><br/><br/>");
        out.println(receiptIdSet.toString() + "</center>");
        out.println("<br/><br/><br/><br/>");
        out.println("<center><br/>"+ paymentcount +" Data Updated for subdomain : <b>" + subDomain + "</b> <br/>");
        out.println("<br/>Advanced Payment Amount Due Updated for : <br/><br/><br/>");
        out.println(paymentIdSet.toString() + "</center>");
    } catch (Exception ex) {
        out.println("Exception occuring while executing script - " + ex.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>
