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
        String dbName = request.getParameter("dbname");//"newstaging";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = request.getParameter("subdomain");
        String moduleid = request.getParameter("moduleid");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname FROM company ";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query += " where subdomain= ?";
        }
        if (StringUtil.isNullOrEmpty(moduleid)) {
            moduleid = "20";
        }
        String tableName = "salesorder";
        if(!StringUtil.isNullOrEmpty(moduleid) && moduleid.equals("18")){
            tableName = "purchaseorder";
        }
        if(!StringUtil.isNullOrEmpty(moduleid) && moduleid.equals("2")){
            tableName = "invoice";
        }
        if(!StringUtil.isNullOrEmpty(moduleid) && moduleid.equals("6")){
            tableName = "goodsreceipt";
        }
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        int soCounter = 0;
        
        while (rs.next()) {
            soCounter = 0;
            String companyId = rs.getString("companyid");
            String companyname = rs.getString("companyname");
            
            // Get Sales Order
            
            query = "select id,pendingapproval from "+tableName+" where company=?"; //Get All Records
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                String soid = "";
                soid = rs1.getString("id");
                if (!StringUtil.isNullOrEmpty(soid)) {
                    int approvalLevel = 0;
                    approvalLevel = rs1.getInt("pendingapproval");
                    query = "UPDATE "+tableName+" SET approvestatuslevel=? WHERE id=?";// Update approvalstatus according to approvallevel
                    PreparedStatement stmt2 = conn.prepareStatement(query);
                    if(approvalLevel == 0){
                        stmt2.setInt(1, 11);
                    }else{
                        stmt2.setInt(1, approvalLevel);
                    }
                    stmt2.setString(2, soid);
                    stmt2.execute();
                    soCounter++;
                }
            }
            out.println("<br><br> Records successfully <b>Updated</b> for " + companyname + " : " + soCounter );
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