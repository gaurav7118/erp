<%-- 
    Document   : CustomFieldDimensionPermissionsConfigureScript.jsp
    Created on : May 25, 2017, 2:08:15 PM
    Author     : krawler
--%>
<%@page import="java.sql.*"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="java.util.logging.Level"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Master Configuration Permissions Reconfigure Script</title>
    </head>
</html>
<%
    Connection conn = null;
    try {
        //SCRIPT URL : http://<app-url>/CustomFieldDimensionPermissionsConfigureScript.jsp?serverip=?&dbname=?&username=?&password=?

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly.");
        }
        
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver).newInstance();
        
        //Execution Started :
        out.println("<br><br>Execution Started @ "+new java.util.Date()+"<br><br>");
        out.println("<br><br>Please wait.....<br><br>");
        
        conn = DriverManager.getConnection(connectString, username, password);
        
        String masterConfigFeatureId = "ff80808122f9dba90122fa4888cf0060";
        String customFieldDimensionFeatureId = "d577b5b0412c11e7897014dda9792823";
        
        int[] roleRecords = updateRolePermissions(conn, masterConfigFeatureId, customFieldDimensionFeatureId);
        out.println("Total Role Permissions Records: " + roleRecords[0] + "<br>");
        out.println("Total Role Permissions Updated Records: " + roleRecords[1] + "<br>");
        out.println("<br>");
        
        int[] userRecords = updateUserPermissions(conn, masterConfigFeatureId, customFieldDimensionFeatureId);                
        out.println("Total User Permissions Records: " + userRecords[0] + "<br>");
        out.println("Total User Permissions Updated Records: " + userRecords[1] + "<br>");
        out.println("<br>");
    }
    catch (Exception e) {
        Logger.getLogger("MasterConfigurationPermissionsReconfigureScript").log(Level.SEVERE, e.getMessage(), e);
    }
    finally {
        if (conn != null) {
            try {
                conn.close();
                out.println("Connection Closed....<br/>");
                out.println("<br><br>Execution Ended @ " + new java.util.Date() + "<br><br>");
            } catch (Exception e) {
                Logger.getLogger("MasterConfigurationPermissionsReconfigureScript").log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
%>

<%!
    public static int[] updateRolePermissions(Connection conn, String masterConfigFeatureId, String customFieldDimensionFeatureId) {
        int totalNumberOfRecord = 0;
        int numberOfRecordsAffected = 0;
        String getRolePermissionsForMasterConfigQuery = "SELECT role, company, permissioncode FROM rolepermission WHERE feature = ? AND role !=1 AND permissioncode != 0";
        String updateRolePermissionsForCustomFieldDimention = "INSERT INTO rolepermission (feature, role, company, permissioncode) VALUES (?, ?, ?, ?)";
        String deleteRolePermissionsForCustomFieldDimention = "DELETE FROM rolepermission WHERE feature = ? and role != 1";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(deleteRolePermissionsForCustomFieldDimention);
            pstmt.setString(1, customFieldDimensionFeatureId);
            pstmt.executeUpdate();
            
            pstmt = conn.prepareStatement(getRolePermissionsForMasterConfigQuery);
            pstmt.setString(1, masterConfigFeatureId);
            ResultSet rs = pstmt.executeQuery();
            
            while(rs.next()) {
                ++totalNumberOfRecord;
                String role = rs.getString("role");
                String companyid = rs.getString("company");
                int permissioncode = rs.getInt("permissioncode");
                
                StringBuilder binary = new StringBuilder(Integer.toBinaryString(permissioncode)).reverse();
                char[] bit = binary.toString().toCharArray();
                
                if (bit.length > 1 && bit[0] == '1' && bit[1] == '1') {
                    try {
                        pstmt = conn.prepareStatement(updateRolePermissionsForCustomFieldDimention);
                        pstmt.setString(1, customFieldDimensionFeatureId);
                        pstmt.setString(2, role);
                        pstmt.setString(3, companyid);
                        pstmt.setInt(4, 3);
                        int rowAffected = pstmt.executeUpdate();
                        numberOfRecordsAffected += rowAffected;
                        if (rowAffected != 1) {
                            throw new Exception("Can't able to change the role permission for company " + companyid + ", role " + role);
                        }
                    } catch (Exception e) {
                        Logger.getLogger("MasterConfigurationPermissionsReconfigureScript").log(Level.SEVERE, e.getMessage(), e);
                        continue;
                    }
                }
            }            
        } catch (Exception e) {
            Logger.getLogger("MasterConfigurationPermissionsReconfigureScript").log(Level.SEVERE, e.getMessage(), e);
        }
        return new int[]{totalNumberOfRecord, numberOfRecordsAffected};
    }
%>

<%!
    public static int[] updateUserPermissions(Connection conn, String masterConfigFeatureId, String customFieldDimensionFeatureId) {
        int totalNumberOfRecord = 0;
        int numberOfRecordsAffected = 0;
        String getUserPermissionsForMasterConfigQuery = "SELECT role, permissioncode, roleUserMapping FROM userpermission WHERE feature = ? AND role !=1 AND permissioncode != 0";
        String updateUserPermissionsForCustomFieldDimention = "INSERT INTO userpermission (feature, role, permissioncode, roleUserMapping) VALUES (?, ?, ?, ?)";
        String deleteUserPermissionsForCustomFieldDimention = "DELETE FROM userpermission WHERE feature = ? and role != 1";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(deleteUserPermissionsForCustomFieldDimention);
            pstmt.setString(1, customFieldDimensionFeatureId);
            pstmt.executeUpdate();
            
            pstmt = conn.prepareStatement(getUserPermissionsForMasterConfigQuery);
            pstmt.setString(1, masterConfigFeatureId);
            ResultSet rs = pstmt.executeQuery();
            
            while(rs.next()) {
                ++totalNumberOfRecord;
                String role = rs.getString("role");
                int permissioncode = rs.getInt("permissioncode");
                String roleUserMapping = rs.getString("roleUserMapping");
                
                StringBuilder binary = new StringBuilder(Integer.toBinaryString(permissioncode)).reverse();
                char[] bit = binary.toString().toCharArray();
                
                if (bit.length > 1 && bit[0] == '1' && bit[1] == '1') {
                    try {
                        pstmt = conn.prepareStatement(updateUserPermissionsForCustomFieldDimention);
                        pstmt.setString(1, customFieldDimensionFeatureId);
                        pstmt.setString(2, role);
                        pstmt.setInt(3, 3);
                        pstmt.setString(4, roleUserMapping);
                        int rowAffected = pstmt.executeUpdate();
                        numberOfRecordsAffected += rowAffected;
                        if (rowAffected != 1) {
                            throw new Exception("Can't able to change the user permission for role " + role);
                        }
                    } catch (Exception e) {
                        Logger.getLogger("MasterConfigurationPermissionsReconfigureScript").log(Level.SEVERE, e.getMessage(), e);
                        continue;
                    }
                }
            }            
        } catch (Exception e) {
            Logger.getLogger("MasterConfigurationPermissionsReconfigureScript").log(Level.SEVERE, e.getMessage(), e);
        }
        return new int[]{totalNumberOfRecord, numberOfRecordsAffected};
    }
%>