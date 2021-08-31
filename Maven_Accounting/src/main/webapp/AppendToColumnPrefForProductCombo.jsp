<%-- 
    Document   : AppendToColumnPrefForProductCombo
    Created on : 4 Jul, 2017, 12:03:40 PM
    Author     : krawler
--%>




<%@page import="com.krawler.common.util.Constants"%>
<%@page import="org.mortbay.util.ajax.JSON"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<%
    /* This Script is for product combo paging funcationality. 
     * This script put allowProductPagingEditing check in columnpref JSON and 
     * if if already exist then it set this parameter as true.
     */       
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = request.getParameter("port");
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }

        String id = "";
        String columnPref = "";
        String jsonValue = "";
        String appendQuery = "";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        int totalrec = 0;
        int updatedrec = 0;

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select id,columnpref from extracompanypreferences where productoptimizedflag=0";

        PreparedStatement stmt = conn.prepareStatement(query);

        ResultSet rs = stmt.executeQuery();

        JSONObject obj = null;

        while (rs.next()) {
            obj = new JSONObject();
            columnPref = rs.getString("columnpref");
            id = rs.getString("id");
            if (columnPref != null) {
                try {
                    obj = new JSONObject(columnPref);
                } catch (Exception e) {

                    out.println("Exception occured because JSON IS NOT IN FORMAT for Company_id " + id);
                    out.println(e.toString());
                    e.printStackTrace();
                    continue;
                }

            }

            totalrec++;
            if (obj.has("allowProductPagingEditing") && obj.getBoolean("allowProductPagingEditing") == true) {
                continue;
            } else {

                obj.put("allowProductPagingEditing", true);

                jsonValue = JSON.toString(obj);

                appendQuery = "update extracompanypreferences SET columnpref= " + jsonValue;
                appendQuery += " where id='" + id + "'";

                PreparedStatement stmt1 = conn.prepareStatement(appendQuery);

                stmt1.executeUpdate(appendQuery);
                stmt1.close();
                updatedrec++;
            }

        }

        out.println("<center>Total Record Count = " + totalrec + "</center><br>");
        out.println("<center>Updated Record Count = " + updatedrec + "</center><br><br>");
        out.println("<center> ** Script Excuted Succecfully ** </center><br><br>");

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>