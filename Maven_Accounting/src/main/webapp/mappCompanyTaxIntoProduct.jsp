<%-- 
    Document   : AddDefaultMasterItemEntryForWastage
    Created on : Dec 29, 2015
    Author     : krawler
--%>
<!--%@page contentType="text/html" pageEncoding="UTF-8"%>-->
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="com.krawler.common.util.Constants"%>


<%
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname FROM company ";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query += " where subdomain= ?";
        }
        PreparedStatement stmt1 = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt1.setString(1, subdomain);
        }
        ResultSet rs1 = stmt1.executeQuery();
        int totalCompanyUpdationCnt = 0;
        while (rs1.next()) {
            String companyID = rs1.getString("companyid");
            String companyName = rs1.getString("companyname");
            int cnAddCounter = 0;

            query = "SELECT id,productid,name FROM product WHERE company = ? AND deleteflag='F'"; // productp id for Stock Adjustment is 31
            PreparedStatement stmt2 = conn.prepareStatement(query);
            stmt2.setString(1, companyID);
            ResultSet rs2 = stmt2.executeQuery();
            String masterID = "";
            while (rs2.next()) {
                    String productuuid = rs2.getString(1);
                    String productname = rs2.getString(2);
                    String productid = rs2.getString(3);
                    
                    query = "SELECT id,product FROM producttermsmap WHERE product=?"; // productp id for Stock Adjustment is 31
                    PreparedStatement stmtCheck = conn.prepareStatement(query);
                    stmtCheck.setString(1, productuuid);
                    ResultSet rscheck = stmtCheck.executeQuery();
                    if (rscheck.next()) {
                        out.println("<br><br> Product : " + productname + " Tax is Already  <b>Added</b> for " + companyName + " : " + cnAddCounter);
                        continue;
                    }


                    if (!StringUtil.isNullOrEmpty(productuuid)) {
                        query = " INSERT INTO producttermsmap ( id,percentage,termamount,product,account,purchasevalueorsalevalue,deductionorabatementpercent,isdefault,taxtype,term,creator ) ( SELECT UUID(),llt.percentage,llt.termamount,?,llt.account,llt.purchasevalueorsalevalue,llt.deductionorabatementpercent,llt.isdefault,llt.taxtype,llt.id,llt.creator FROM linelevelterms llt where llt.company=? )";
                        PreparedStatement stmt3 = conn.prepareStatement(query);
                        stmt3.setString(1, productuuid);
                        stmt3.setString(2, companyID);
                        stmt3.execute();
                        cnAddCounter++;
                        out.println("<br><br> Product : " + productname + " Tax is added successfully <b>Added</b> for " + companyName + " : " + cnAddCounter);
                        totalCompanyUpdationCnt++;
                    }
                }
        }
        if (totalCompanyUpdationCnt == 0) {
            out.println("<br><br> Script Already Executed for Database " + dbName);
        } else {
            out.println("<br><br> Total Product updated are " + totalCompanyUpdationCnt);
            out.println("<br><br> Script Executed Sccessfully ");
        }
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>