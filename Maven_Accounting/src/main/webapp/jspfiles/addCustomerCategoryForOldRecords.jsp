<%-- 
    Document   : addCustomerCategoryForOldRecords
    Created on : Apr 9, 2014, 4:59:56 PM
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

        int cnt = 0;
        String companyId = "";
        String query1 = "select companyid from company";
        PreparedStatement stmt1 = conn.prepareStatement(query1);
        ResultSet rs1 = stmt1.executeQuery();
        while (rs1.next()) {
            companyId = rs1.getString("companyid");

            String query2 = "select c.id,ac.category from customer c inner join account ac on ac.id = c.account where c.company=? ";
            PreparedStatement stmt2 = conn.prepareStatement(query2);
            stmt2.setObject(1, companyId);
            ResultSet rs2 = stmt2.executeQuery();
            while (rs2.next()) {
                String customerID = rs2.getString("id");
                String categoryID = rs2.getString("category");

                if (customerID != null && categoryID != null) {
                    String query3 = "select id from customercategorymapping where customerid =? and customercategory=? ";
                    PreparedStatement stmt3 = conn.prepareStatement(query3);
                    stmt3.setString(1, customerID);
                    stmt3.setString(2, categoryID);
                    ResultSet rs3 = stmt3.executeQuery();
                    if (!rs3.next()) {
                        String uuid1 = java.util.UUID.randomUUID().toString();
                        String query4 = "insert into customercategorymapping (id,customerid,customercategory) values (?,?,?) ";
                        PreparedStatement stmt4 = conn.prepareStatement(query4);
                        stmt4.setString(1, uuid1);
                        stmt4.setString(2, customerID);
                        stmt4.setString(3, categoryID);
                        stmt4.execute();
                        cnt++;
                    }
                }
            }
        }
        out.println(cnt + " Records added successfully.");
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    }
%>