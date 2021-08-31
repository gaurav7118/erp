<%@page import="com.krawler.common.util.StringUtil"%>
<!--%@page contentType="text/html" pageEncoding="UTF-8"%>-->
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");//"192.168.0.208";                            
        String port = "3306";
        String dbName = request.getParameter("dbname");//"newstaging";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname FROM company ";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query += " where subdomain= ?";
        }
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        int contractUpdateCounter = 0;
        
        while (rs.next()) {
            String companyId = rs.getString("companyid");
            String companyname = rs.getString("companyname");
            // Get asset maintenance scheduler
            query = "select id, contractid from assetmaintenanceschedulerobject where company=? "; //
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                String assetmaintenanceschedulerobjectid = "", contractid="";
                assetmaintenanceschedulerobjectid = rs1.getString("id");
                contractid = rs1.getString("contractid");
                if (!StringUtil.isNullOrEmpty(assetmaintenanceschedulerobjectid) && !StringUtil.isNullOrEmpty(contractid)) { 
                    // Get all aggreed services count
                    query = "SELECT count(id) as agreedservices FROM assetmaintenancescheduler WHERE assetmaintenanceschedulerobject=?"; 
                    PreparedStatement stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, assetmaintenanceschedulerobjectid);
                    ResultSet rs2 = stmt3.executeQuery();
                    while (rs2.next()) {
                        int agreedServices = rs2.getInt("agreedservices");
                        if (agreedServices!=0) { 
                            query = "UPDATE contract SET agreedservices=? WHERE id=?"; // Update contract agreedservices field
                            PreparedStatement stmt4 = conn.prepareStatement(query);
                            stmt4.setInt(1, agreedServices);
                            stmt4.setString(2, contractid);
                            stmt4.execute();
                            contractUpdateCounter++;
                        }
                    }
                }
            }
            out.println("<br><br> Contract Record successfully <b>Updated</b> for " + companyname + " : " + contractUpdateCounter );
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
