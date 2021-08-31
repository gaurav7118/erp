<%-- 
    Document   : DataMigrationForInventoryStoreLocation
    Created on : Feb 3, 2015, 10:12:49 AM
    Author     : krawler
--%>

<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.Connection"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<%

    Connection conn = null;
    try {

        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        int updateCount = 0;
        String query = "TRUNCATE TABLE in_stock ";
        PreparedStatement stmtquery = conn.prepareStatement(query);
        stmtquery.executeUpdate();
        stmtquery.close();

        query = "SELECT b.product, b.warehouse, b.location, b.batchname, GROUP_CONCAT(s.serialname) AS serialnames , IF(s.id IS NULL , b.quantitydue, COUNT(s.id)) AS qty , b.company "
                + " FROM newproductbatch  b LEFT JOIN newbatchserial s ON  b.id = s.batch AND s.quantitydue = 1 AND b.quantitydue > 0 "
                + " WHERE b.warehouse IS NOT NULL AND b.location IS NOT NULL  AND b.product  IN(SELECT id from product) "
                + " GROUP BY b.product, b.warehouse, b.location, b.batchname, b.company HAVING ( qty>=1) ";
        stmtquery = conn.prepareStatement(query);
        ResultSet rs = stmtquery.executeQuery();
        while (rs.next()) {
            String product = rs.getString("product");
            String warehouse = rs.getString("warehouse");
            String location = rs.getString("location");
            String batchname = rs.getString("batchname");
            String company = rs.getString("company");
            String serialnames = rs.getString("serialnames");
            double qty = rs.getObject("qty") == null ? 0 : rs.getDouble("qty");


            String query2 = "INSERT INTO in_stock(id, product, store, location, batchname, serialnames, company, quantity, createdon, modifiedon) "
                    + " VALUES (UUID(), ?,?,?,?,?,?,?, NOW(), NOW()) ";
            PreparedStatement stmtquery2 = conn.prepareStatement(query2);
            stmtquery2.setString(1, product);
            stmtquery2.setString(2, warehouse);
            stmtquery2.setString(3, location);
            stmtquery2.setString(4, batchname);
            stmtquery2.setString(5, serialnames);
            stmtquery2.setString(6, company);
            stmtquery2.setDouble(7, qty);
            updateCount += stmtquery2.executeUpdate();
            stmtquery2.close();

        }
        stmtquery.close();

        /*
         * query = "INSERT INTO in_stock(id, product, store, location,
         * batchname, company, quantity, createdon, modifiedon) " + " SELECT
         * UUID(), tb.product, s.id, l.id, '', tb.company, tb.qty, NOW(), NOW()
         * " + " FROM ( SELECT i.product, p.company, sum( IF( i.carryin='T' ,
         * i.baseuomquantity , -1 * i.baseuomquantity)) AS qty " + " FROM
         * inventory i INNER JOIN product p ON i.product = p.id where
         * i.deleteflag='F' and p.producttype <>
         * '4efb0286-5627-102d-8de6-001cc0794cfa'" + " and i.product NOT IN (
         * SELECT product FROM in_stock) group by i.product having sum( IF(
         * i.carryin='T' , i.baseuomquantity , -1 * i.baseuomquantity)) > 0) tb
         * " + " LEFT JOIN in_storemaster s ON tb.company = s.company LEFT JOIN
         * in_location l ON tb.company = l.company WHERE s.isdefault = 1 AND
         * l.isdefault = 1"; stmtquery = conn.prepareStatement(query);
         * insertCount += stmtquery.executeUpdate(); stmtquery.close();
         */
        query = " UPDATE in_stock set quantity =0 where quantity IS NULL";
        stmtquery = conn.prepareStatement(query);
        stmtquery.executeUpdate();
        stmtquery.close();


        out.println(updateCount + " Records has been updated for inventory stock");
        out.println("==============================================================================================================");
        out.println();
    } catch (Exception e) {
        if (conn != null) {
            conn.rollback();
        }
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>