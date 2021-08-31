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

        String query = "SELECT company FROM in_storemaster WHERE isdefault = 1 GROUP BY company having COUNT(id) > 1";
        PreparedStatement stmtquery = conn.prepareStatement(query);
        ResultSet rs = stmtquery.executeQuery();
        while (rs.next()) {
            String company = rs.getString("company");
            String query1 = "SELECT id FROM in_storemaster WHERE isdefault = 1 AND company = ? ";
            PreparedStatement stmtquery1 = conn.prepareStatement(query1);
            stmtquery1.setString(1, company);
            ResultSet rs1 = stmtquery1.executeQuery();
            if(rs1.next()){
                String id = rs1.getString("id");
                String query2 = "UPDATE in_storemaster SET isdefault = 0 WHERE id = ? ";
                PreparedStatement stmtquery2 = conn.prepareStatement(query2);
                stmtquery2.setString(1, id);
                stmtquery2.executeUpdate();
                stmtquery2.close();
            }
            stmtquery1.close();
        }
        stmtquery.close();
        
        query = "SELECT company FROM in_location WHERE isdefault = 1 GROUP BY company having COUNT(id) > 1";
        stmtquery = conn.prepareStatement(query);
        rs = stmtquery.executeQuery();
        while (rs.next()) {
            String company = rs.getString("company");
            String query1 = "SELECT id FROM in_location WHERE isdefault = 1 AND company = ? ";
            PreparedStatement stmtquery1 = conn.prepareStatement(query1);
            stmtquery1.setString(1, company);
            ResultSet rs1 = stmtquery1.executeQuery();
            if(rs1.next()){
                String id = rs1.getString("id");
                String query2 = "UPDATE in_location SET isdefault = 0 WHERE id = ? ";
                PreparedStatement stmtquery2 = conn.prepareStatement(query2);
                stmtquery2.setString(1, id);
                stmtquery2.executeUpdate();
                stmtquery2.close();
            }
            stmtquery1.close();
        }
        stmtquery.close();
        
        query = "INSERT INTO inventorywarehouse (id, name, company, isdefault) "
                +" SELECT UUID(), 'MWH', companyid, 1 FROM company WHERE companyid NOT IN ( SELECT DISTINCT company FROM inventorywarehouse)";
        stmtquery = conn.prepareStatement(query);
        stmtquery.executeUpdate();
        stmtquery.close();
        
        query = "INSERT INTO inventorylocation (id, name, company, isdefault) "
                +" SELECT UUID(), 'Default Location', companyid, 1 FROM company WHERE companyid NOT IN ( SELECT DISTINCT company FROM inventorylocation)";
        stmtquery = conn.prepareStatement(query);
        stmtquery.executeUpdate();
        stmtquery.close();
        
        query = "SELECT id FROM inventorywarehouse WHERE  company NOT IN (SELECT DISTINCT company FROM inventorywarehouse WHERE isdefault = 1 ) GROUP BY company ";
        stmtquery = conn.prepareStatement(query);
        rs = stmtquery.executeQuery();
        while (rs.next()) {
            String warehouseid = rs.getString("id");
            String query1 = "UPDATE inventorywarehouse SET isdefault = 1 WHERE id = ? ";
            PreparedStatement stmtquery1 = conn.prepareStatement(query1);
            stmtquery1.setString(1, warehouseid);
            stmtquery1.executeUpdate();
            stmtquery1.close();
        }
        stmtquery.close();
        
        query = "SELECT id FROM inventorylocation WHERE  company NOT IN (SELECT DISTINCT company FROM inventorylocation WHERE isdefault = 1 ) GROUP BY company ";
        stmtquery = conn.prepareStatement(query);
        rs = stmtquery.executeQuery();
        while (rs.next()) {
            String locationid = rs.getString("id");
            String query1 = "UPDATE inventorylocation SET isdefault = 1 WHERE id = ? ";
            PreparedStatement stmtquery1 = conn.prepareStatement(query1);
            stmtquery1.setString(1, locationid);
            stmtquery1.executeUpdate();
            
            stmtquery1.close();
        }
        stmtquery.close();
        
        
        query = "INSERT INTO in_storemaster (id, abbrev, description, type, isdefault, isactive, company ,createdby, modifiedby, createdon, modifiedon) "
                +" SELECT invw.id, invw.name, invw.name,  0, invw.isdefault, 1, invw.company, c.creator, c.creator, NOW(), NOW() "
                +" FROM inventorywarehouse invw INNER JOIN company c ON invw.company = c.companyid WHERE c.creator IS NOT NULL AND invw.id NOT IN (SELECT id FROM in_storemaster)";
        stmtquery = conn.prepareStatement(query);
        int storeCount = stmtquery.executeUpdate();
        stmtquery.close();
        
        query = "INSERT INTO in_location (id, name, isactive, isdefault, company, createdby, modifiedby, createdon, modifiedon) "
                +" SELECT invl.id, invl.name, 1,invl.isdefault, invl.company, c.creator, c.creator, NOW(), NOW() "
                +" FROM inventorylocation invl INNER JOIN company c ON invl.company = c.companyid WHERE c.creator IS NOT NULL AND invl.id NOT IN (SELECT id FROM in_location)";
        stmtquery = conn.prepareStatement(query);
        int locationCount = stmtquery.executeUpdate();
        stmtquery.close();
        
        query = "INSERT INTO in_store_location (storeid, locationid) "
                +" SELECT s.id, l.id FROM in_storemaster s INNER JOIN in_location l ON s.company = l.company "
                +" WHERE l.isdefault = 1 AND s.id NOT IN(SELECT storeid FROM in_store_location) ";
        stmtquery = conn.prepareStatement(query);
        int mappingCount = stmtquery.executeUpdate();
        stmtquery.close();
        
        out.println( storeCount +" Store,  "+locationCount+" Location Records has been copied with "+mappingCount+" mappings");
        out.println("==============================================================================================================");
        out.println();
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
        if (conn != null) {
            conn.rollback();
        }
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>
