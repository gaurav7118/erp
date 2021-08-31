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
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"newstaging";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = "gccm";

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
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
        int prRuleAddCounter = 0;
        
        while (rs.next()) {
            String companyId = rs.getString("companyid");
            String companyname = rs.getString("companyname");
            
            // Get Tax
            query = "select id,name,taxcode from tax where company=? "; //
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                String taxid = "", taxName="", taxCode="";
                taxid = rs1.getString("id");
                taxName = rs1.getString("name");
                taxCode = rs1.getString("taxcode");
                if (!StringUtil.isNullOrEmpty(taxid)) { 
                    // Get rules to user mapping
                    query = "select id, percent from taxlist where tax=? and company=?"; //
                    PreparedStatement stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, taxid);
                    stmt3.setString(2, companyId);
                    ResultSet rs2 = stmt3.executeQuery();
                    while (rs2.next()) {
                        String taxPer = "";
                        taxPer = rs2.getString("percent");
                        if (!StringUtil.isNullOrEmpty(taxPer)) { 
                            taxName=taxName+"@"+taxPer+"%";
                            taxCode=taxCode+"@"+taxPer+"%";
                            query = "UPDATE tax SET name=?, taxcode=? WHERE id=?";
                            PreparedStatement stmt4 = conn.prepareStatement(query);
                            stmt4.setString(1, taxName);
                            stmt4.setString(2, taxCode);
                            stmt4.setString(3, taxid);
                            stmt4.execute();
                        }
                    }
                }
                prRuleAddCounter++;
            }
            out.println("<br><br> Tax Record successfully <b>Updated</b> for " + companyname + " : " + prRuleAddCounter );
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
