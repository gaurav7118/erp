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
        String subdomain = request.getParameter("subdomain");

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
            
            // Get rules
            query = "select id,level,rule,companyid,hasapprover from prflow where companyid=? "; //
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                String ruleid = "";
                ruleid = rs1.getString("id");
                if (!StringUtil.isNullOrEmpty(ruleid)) { 
                    query = "insert into multilevelapprovalrule (id,level,rule,companyid,hasapprover,moduleid) values (?,?,?,?,?,?)";
                    PreparedStatement stmt2 = conn.prepareStatement(query);
                    stmt2.setString(1, ruleid);
                    stmt2.setInt(2, rs1.getInt("level"));
                    stmt2.setString(3, rs1.getString("rule"));
                    stmt2.setString(4, rs1.getString("companyid"));
                    stmt2.setInt(5, rs1.getInt("hasapprover"));
                    stmt2.setInt(6, 32);
                    stmt2.execute();
                    prRuleAddCounter++;
                    
                    // Get rules to user mapping
                    query = "select id,flowid,userid from prflowtargets where flowid=? "; //
                    PreparedStatement stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, ruleid);
                    ResultSet rs2 = stmt3.executeQuery();
                    while (rs2.next()) {
                        String ruleUserMapId = "";
                        ruleUserMapId = rs2.getString("id");
                        if (!StringUtil.isNullOrEmpty(ruleUserMapId)) { 
                            query = "insert into multilevelapprovalruletargetusers (id,ruleid,userid) values (?,?,?)";
                            PreparedStatement stmt4 = conn.prepareStatement(query);
                            stmt4.setString(1, ruleUserMapId);
                            stmt4.setString(2, rs2.getString("flowid"));
                            stmt4.setString(3, rs2.getString("userid"));
                            stmt4.execute();
                        }
                    }
                }
            }
            out.println("<br><br> Requisition Rules Record successfully <b>Added</b> for " + companyname + " : " + prRuleAddCounter );
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