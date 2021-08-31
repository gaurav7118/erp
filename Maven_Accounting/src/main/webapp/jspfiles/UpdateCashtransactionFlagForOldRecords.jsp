<%-- 
    Document   : UpdateCashtransactionFlagForOldRecords
    Created on : Apr 7, 2014, 10:32:43 AM
    Author     : krawler
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>

<%
    Connection conn = null;
    try {
        String serverip = "192.168.0.209";
        String port = "3306";
        String dbName = "newstaging";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "krawlersqladmin";
        String password = "krawler";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);

        String companyId = "";
        String cashAccount = "";
        String querycash = "select id,cashAccount from compaccpreferences";
        PreparedStatement stmtcash = conn.prepareStatement(querycash);
        ResultSet rscash = stmtcash.executeQuery();
        while (rscash.next()) {
            companyId = rscash.getString("id");
            cashAccount = rscash.getString("cashAccount");
            String querycashNet = "update invoice inner join jedetail on jedetail.id = invoice.centry   set invoice.cashtransaction=1   where invoice.company = '" + companyId + "' and jedetail.account = '" + cashAccount + "' ";
            PreparedStatement stmtcashNet = conn.prepareStatement(querycashNet);
            stmtcashNet.executeUpdate();
            querycashNet = "update goodsreceipt inner join jedetail on jedetail.id = goodsreceipt.centry   set goodsreceipt.cashtransaction=1   where goodsreceipt.company = '" + companyId + "' and jedetail.account = '" + cashAccount + "'";
            stmtcashNet = conn.prepareStatement(querycashNet);
            stmtcashNet.executeUpdate();
        }
        out.println("Records updated successfully.");
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    }
%>
