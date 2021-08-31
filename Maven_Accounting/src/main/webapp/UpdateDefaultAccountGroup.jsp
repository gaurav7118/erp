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
        String serverip = request.getParameter("serverip");//"192.168.0.21";                            
        String port = "3306";//"3306";
        String dbName = request.getParameter("dbname");//"stagingaccounting";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname,subdomain FROM company ";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query += " where subdomain= ?";
        }
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        while (rs.next()) {
            int accGrpUpdateCounter = 0;
            String companyId = rs.getString("companyid");
            String companyname = rs.getString("companyname");
            subdomain = rs.getString("subdomain");
%>
            <div align="center">
                <table border="1" cellpadding="5">
                    <caption><h2>List of Updated Records</h2></caption>
                    <tr><th>Subdomain</th><th>AccountCode</th><th>AccountName</th><th>OldGroupId</th><th>OldGroupName</th><th>NewGroupId</th><th>NewGroupName</th></tr>
<%
            // Get Accounts with default account
            query = "select ac.id, ac.acccode, ac.name, gr.id as groupid, gr.name as groupname from account ac inner join accgroup gr on ac.groupname=gr.id where ac.company=? and ac.groupname in (select id from accgroup where company is null or company='')"; //
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                String accid = "", accName="", accCode="", accGroup="", accGroupName="";
                accid = rs1.getString("id");
                accName = rs1.getString("name");
                accCode = rs1.getString("acccode");
                accGroup = rs1.getString("groupid");
                accGroupName = rs1.getString("groupname");
                if (!StringUtil.isNullOrEmpty(accGroup)) { 
                    // Get rules to user mapping
                    query = "select id, name from accgroup where company=? and grpOldId=?"; 
                    PreparedStatement stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, companyId);
                    stmt3.setString(2, accGroup);
                    ResultSet rs2 = stmt3.executeQuery();
                    while (rs2.next()) {
                        String newGrpId = "", oldGrpId = "", newGrpName="";
                        newGrpId = rs2.getString("id");
                        newGrpName = rs2.getString("name");
                        oldGrpId = accGroup;
                        if (!StringUtil.isNullOrEmpty(newGrpId)) { 
                            query = "UPDATE account SET groupname=? WHERE id=?";
                            PreparedStatement stmt4 = conn.prepareStatement(query);
                            stmt4.setString(1, newGrpId);
                            stmt4.setString(2, accid);
                            stmt4.execute();
                            accGrpUpdateCounter++;
%>
                            <tr>
                                <td><% out.println(subdomain);%></td>
                                <td><% out.println(accCode);%></td>
                                <td><% out.println(accName);%></td>
                                <td><% out.println(oldGrpId);%></td>
                                <td><% out.println(accGroupName);%></td>
                                <td><% out.println(newGrpId);%></td>
                                <td><% out.println(newGrpName);%></td>
                            </tr>
<%
                        }
                    }
                }
            }
            out.println("<br><br> Total Default Account Group <b>Updated</b> for " + companyname + " : " + accGrpUpdateCounter );
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


