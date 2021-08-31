<%-- 
    Document   : AddWithoutBOMJsonColumn
    Created on : 2 May, 2017, 7:42:18 PM
    Author     : krawler
--%><%@page import="com.krawler.utils.json.JSON"%>
<%@page import="com.krawler.utils.json.JSONObject"%>
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
        String serverip = request.getParameter("serverip");  //"192.168.0.147";   
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"staging_02032017";
        String userName = request.getParameter("username");//"root";
        String password = request.getParameter("password");//"";

//        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName)) { //when password is empty
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String withoutBOM = null;
        String id = null;
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select id,withoutBOM from extracompanypreferences";
        

        PreparedStatement stmt = conn.prepareStatement(query);
        
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            withoutBOM = rs.getString("withoutBOM");
            id = rs.getString("id");
            if (withoutBOM.equals("F")) {
                withoutBOM = "false";

            } else {
                withoutBOM = "true";

            }

            String jsonValue = "{" + "\"" + "withoutBOM" + "\"" + ":" + withoutBOM + "}";

            String insertQuery = "update extracompanypreferences SET columnpref= '" + jsonValue;
            insertQuery += "' where id='" + id + "'";

            PreparedStatement stmt1 = conn.prepareStatement(insertQuery);

            stmt1.executeUpdate(insertQuery);

        }
        String deleteQuery = "ALTER TABLE extracompanypreferences DROP COLUMN withoutBOM";
        PreparedStatement stmt2 = conn.prepareStatement(query);
        stmt2.executeUpdate(deleteQuery);

        out.println("Record Updated in columnpref column and Deleted from withoutBOM column");

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }


%>