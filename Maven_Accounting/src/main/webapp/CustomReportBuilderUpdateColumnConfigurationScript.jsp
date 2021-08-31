<%--
    Document   : CustomReportBuilderUpdateColumnConfigurationScript.jsp
    Created on : Aug 4, 2017, 10:22:55 AM
    Author     : krawler
--%>

<%@page import = "com.krawler.common.util.StringUtil"%>
<%@page import = "com.krawler.utils.json.base.JSONArray"%>
<%@page import = "com.krawler.utils.json.base.JSONException"%>
<%@page import = "com.krawler.utils.json.base.JSONObject"%>

<%@page import = "java.sql.*"%>
<%@page import = "java.util.logging.Logger"%>
<%@page import = "java.util.logging.Level"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Custom Report Builder Update Column Configuration Script</title>
    </head>
</html>

<%
    //SCRIPT URL : http://<app-url>/CustomReportBuilderUpdateColumnConfigurationScript.jsp?serverip=?&dbname=?&username=?&password=?
    
    Connection conn = null;
    
    String serverip = null;
    String dbname = null;
    
    try {
        String succeessMsg = "";
        int totalRecord = 0;
        int totalUpdated = 0;
        
        serverip = request.getParameter("serverip");
        dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        String columnid = "d4258c68-8d04-4983-aa8d-82ebaf7861c8"; //Sales Order Status Column id
        String configName = "dbcolumnname"; //Config name which need to be update
        String configValue = "IF(salesorder.issoclosed='T','Closed',IF(getSumOfSOProductBalanceQty(salesorder.id)=0,'Closed',IF(salesorder.isopen='T','Open',IF(producttype.name='Service','Closed','Open'))))"; //Config new value
        /*
            defaultmappingJSONObj arrays for SO status column;
        */
        
        String defaultmappingJSONArray = "["
                + "{"
                + "'id':'84ee1d60-c419-4aa6-8e3d-3e982e88b1c1',"
                + "'isSelectDataIndex':false,'reftablefk':'id',"
                + "'defaultheaderid':'11f89ee2-d6bd-4afb-b56c-1edbedc47bf5',"
                + "'dbtabletame':'sodetails',"
                + "'defaultextrainnerjoin':' left join sodetails on salesorder.id = sodetails.salesorder ',"
                + "'reftabledatacolumn':'id',"
                + "'measurefieldid':'d4258c68-8d04-4983-aa8d-82ebaf7861c8',"
                + "'dbcolumnname':'salesorder',"
                + "'defaultHeader':'salesorder.id',"
                + "'reftablename':'salesorder'"
                + "},"
                + "{"
                + "'id':'9ec3f5ac-7c3b-11e7-bb31-be2e44b06b34',"
                + "'isSelectDataIndex':false,'reftablefk':'id',"
                + "'defaultheaderid':'544aa36c-7c2d-11e7-bb31-be2e44b06b34',"
                + "'dbtabletame':'sodetails',"
                + "'defaultextrainnerjoin':' left join product on product.id = sodetails.product ',"
                + "'reftabledatacolumn':'id',"
                + "'measurefieldid':'d4258c68-8d04-4983-aa8d-82ebaf7861c8',"
                + "'dbcolumnname':'product',"
                + "'defaultHeader':'product.id',"
                + "'reftablename':'product'"
                + "},"
                + "{"
                + "'id':'9ec3fa34-7c3b-11e7-bb31-be2e44b06b34',"
                + "'isSelectDataIndex':false,"
                + "'reftablefk':'id',"
                + "'defaultheaderid':'544aa470-7c2d-11e7-bb31-be2e44b06b34',"
                + "'dbtabletame':'product',"
                + "'defaultextrainnerjoin':' left join producttype on producttype.id = product.producttype ',"
                + "'reftabledatacolumn':'id',"
                + "'measurefieldid':'d4258c68-8d04-4983-aa8d-82ebaf7861c8',"
                + "'dbcolumnname':'producttype',"
                + "'defaultHeader':'producttype.id',"
                + "'reftablename':'producttype'"
                + "}"
                + "]";

        JSONArray defaultmappingJSONObj = new JSONArray(defaultmappingJSONArray);
        
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            String msg = "You do not have provided all the parameters correctly in url."
                       + " Parameters are : serverip, dbname, username, password"
                       + "<br>If you don't have set password for your username then pass empty string for passwords parameter";
            throw new Exception(msg);
        }
        
        if (StringUtil.isNullOrEmpty(configName) || StringUtil.isNullOrEmpty(configValue)) {
            String msg = "Please provide configname and configvalue";
            throw new Exception(msg);
        }
        
        try {
            String driver = "com.mysql.jdbc.Driver";
            Class.forName(driver).newInstance();
            
            String connectionString = "jdbc:mysql://" + serverip + ":3306/" + dbname;

            conn = DriverManager.getConnection(connectionString, username, password);
            out.println("The connection has been established with Database - " + dbname);
        } catch (SQLException e) {
            throw new Exception("Not able to make connection with Database. Please check your credentials");
        }
//-----------------------------------------Execution Started---------------------------------------------------
        out.println("<br><br>Execution Started @ " + new java.util.Date() + "<br>");
        
        String reportQuery = "SELECT id, name, reportjson FROM reportmaster";
        
        ResultSet rs = conn.prepareStatement(reportQuery).executeQuery();
        
        boolean update = false;
        
        while (rs.next()) {
            totalRecord++;
            update = false;
            String reportid = rs.getString("id");
            String reportName = rs.getString("name");
            String reportJsonStr = rs.getString("reportjson");
            
            JSONObject reportJson = null;
            if(!StringUtil.isNullOrEmpty(reportJsonStr)) {
                reportJson = new JSONObject(reportJsonStr);
            }
            
            if(!StringUtil.isNullObject(reportJson) && !StringUtil.isNullOrEmpty(reportid)) {
                update = updateColumnConfiguration(reportJson, columnid, configName, configValue, defaultmappingJSONObj);
            }
            
            if(update) {
                totalUpdated++;
                succeessMsg = updateCustomReportWithChanges(conn, reportJson, reportid, reportName);
                out.println(succeessMsg);
            }
        }
        
        if(totalRecord == 0) {
            throw new Exception("<br>There is no matching record to update in given database.");
        }
        
        out.println("<br><br>Total Record : " + totalRecord);
        out.println("<br>Total Record Updated : " + totalUpdated);
//-----------------------------------------Execution Ended---------------------------------------------------
    } catch (Exception e) {
        out.println(e.getMessage());
        Logger.getLogger("CustomReportBuilderUpdateColumnConfigurationScript").log(Level.SEVERE, e.getMessage(), e);
    }
    finally {
        if (conn != null) {
            try {
                out.println("<br><br>Execution Ended @ " + new java.util.Date());
                conn.close();
                out.println("<br><br>The connection has been closed with Database - " + dbname);
            } catch (Exception e) {
                Logger.getLogger("CustomReportBuilderUpdateColumnConfigurationScript").log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
    
%>

<%!
    public static boolean updateColumnConfiguration(JSONObject reportJson, String columnid, String configName, String configValue, JSONArray defaultmappingJSONObj) throws Exception {
        boolean update = false;
        try {
            JSONArray jArr = reportJson.optJSONArray("columnConfig");
            
            if(!StringUtil.isNullObject(jArr)) {
                for(int i = 0; i < jArr.length(); i++) {
                    JSONObject columnConfigJson = jArr.optJSONObject(i);
                    if(StringUtil.equalIgnoreCase(columnConfigJson.optString("id"), columnid)) {
                        if(columnConfigJson.has(configName)) {
                            columnConfigJson.put("defaultmappingJSONObj", defaultmappingJSONObj);
                            columnConfigJson.put(configName, configValue);
                            update = true;
                            break;
                        } else {
                            throw new Exception("<br><font color='red'>Supplied config name (" + configName + ") is incorrect. Please provide correct config name</font>");
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return update;
    }
%>

<%!
    public static String updateCustomReportWithChanges(Connection conn, JSONObject reportJson, String reportid, String reportName) {
        String succeessMsg = null;
        try {
            PreparedStatement pstmt = null;
            String customReportUpdateQuery = "UPDATE reportmaster SET reportjson = ? WHERE id = ?";
            
            pstmt = conn.prepareStatement(customReportUpdateQuery);
            pstmt.setString(1, reportJson.toString());
            pstmt.setString(2, reportid);
            
            int result = pstmt.executeUpdate();
            
            if(result > 0) {
                succeessMsg = "<br><font color='green'>Custom Report <b>'" + reportName + "'</b> updated successfully.</font>";
            } else {
                succeessMsg = "<br><font color='red'>Custom Report <b>'" + reportName + "'</b> not updated.</font>";
            }
            
        } catch (Exception e) {
            succeessMsg = "<br><font color='red'>Exception occurred while updating Custom Report <b>'" + reportName + "'</b></font>";
        }
        return succeessMsg;
    }
%>