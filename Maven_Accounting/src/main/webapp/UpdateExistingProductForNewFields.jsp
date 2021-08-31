<%-- 
    Document   : CreateStoreLocationForExistingCompanies
    Created on : Jan 27, 2015, 6:04:14 PM
    Author     : krawler
--%>

<%@page import="org.w3c.util.UUID"%>
<%@page import="java.sql.*"%>
<%@page import="java.io.*"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    Connection conn = null;
    try {
        String serverip = "localhost";
        String port = "3306";
        String dbName = "accountingdiamondaviation";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "krawlersqladmin";
        String password = "krawler";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);

        String companyId = "";
        String productId = "";
        String uom = null;
        String query1 = " SELECT id, company, unitOfMeasure AS uom FROM product ";
        PreparedStatement stmt1 = conn.prepareStatement(query1);
        ResultSet rs1 = stmt1.executeQuery();
        while (rs1.next()) {
            productId = rs1.getString("id");
            companyId = rs1.getString("company");
            uom = rs1.getString("uom");
            String pkgid = UUID.getUUID().toString();
            if (uom != null) {
                String query2 = " INSERT INTO in_packaging (id, company,  stockuom, stockuom_value) VALUES(?,?,?,?); ";
                PreparedStatement stmt2 = conn.prepareStatement(query2);

                stmt2.setString(1, pkgid);
                stmt2.setString(2, companyId);
                stmt2.setString(3, uom);
                stmt2.setDouble(4, 1);
                stmt2.executeUpdate();
            }
            if (uom != null) {
                String query3 = " Update product SET packaging = ?, transferuom = ?, orderinguom = ?, salesuom = ?, purchaseuom = ? WHERE company = ? AND id = ?";
                PreparedStatement stmt3 = conn.prepareStatement(query3);
                stmt3.setString(1, pkgid);
                stmt3.setString(2, uom);
                stmt3.setString(3, uom);
                stmt3.setString(4, uom);
                stmt3.setString(5, uom);
                stmt3.setString(6, companyId);
                stmt3.setString(7, productId);
                stmt3.executeUpdate();
            }
        }

        out.println(" Records updated successfully.");
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    }
%>
