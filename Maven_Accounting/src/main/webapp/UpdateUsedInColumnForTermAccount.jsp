<%-- 
    Document   : AddDefaultBOMDetail
    Created on : Apr 23, 2016, 10:21:55 AM
    Author     : krawler
--%>
<%@page import="com.krawler.common.util.Constants"%>
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
        String serverip = request.getParameter("serverip");  //"192.168.0.104";   
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"stagingaccounting_04042016";
        String userName = request.getParameter("username");//"krawler";
        String password = request.getParameter("password");//"krawler";
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname from company where country = 105";// For India Country only 
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query += " and subdomain= ?";
        }
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");
            query = "select IFNULL(account,'') as account, IFNULL(payableaccount,'') as payableaccount from linelevelterms where company=? ";
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                String account = rs1.getString("account");
                String payableaccount = rs1.getString("payableaccount");
                /**
                 * update usedin column for account and payableaccount of linelevelterm.
                 */
                if (!StringUtil.isNullOrEmpty(account)) {
                    try {
                        String usedInQuery = "select usedin from account where id=? ";
                        PreparedStatement defaultStmt = conn.prepareStatement(usedInQuery);
                        defaultStmt.setString(1, account);
                        ResultSet Rs = defaultStmt.executeQuery();
                        String usedIn = "";
                        while (Rs.next()) {
                            usedIn = Rs.getString("usedin");
                        }
                        if (!StringUtil.isNullOrEmpty(usedIn)) {
                            usedIn = StringUtil.getUsedInValue(usedIn, Constants.Term_Account);
                            String productAssemblyQuery = "update account set controlaccounts='T', usedin=? where id=?";
                            PreparedStatement stmt2 = conn.prepareStatement(productAssemblyQuery);
                            stmt2.setString(1, usedIn);
                            stmt2.setString(2, account);
                            stmt2.executeUpdate();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        out.println("Exception occurred " + ex.toString() + " for account id " + account);
                    }
                }
                if (!StringUtil.isNullOrEmpty(payableaccount)) {
                    try {
                        String usedInQuery = "select usedin from account where id=? ";
                        PreparedStatement defaultStmt = conn.prepareStatement(usedInQuery);
                        defaultStmt.setString(1, payableaccount);
                        ResultSet Rs = defaultStmt.executeQuery();
                        String usedIn = "";
                        while (Rs.next()) { 
                            usedIn = Rs.getString("usedin");
                        }
                        if (!StringUtil.isNullOrEmpty(usedIn)) {
                            usedIn = StringUtil.getUsedInValue(usedIn, Constants.Term_Account);
                            String productAssemblyQuery = "update account set controlaccounts='T', usedin=? where id=?";
                            PreparedStatement stmt2 = conn.prepareStatement(productAssemblyQuery);
                            stmt2.setString(1, usedIn);
                            stmt2.setString(2, payableaccount);
                            stmt2.executeUpdate();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        out.println("Exception occurred " + ex.toString() + " for account id " + payableaccount);
                    }
                }
            }
            totalCompanyUpdationCnt++;
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>
