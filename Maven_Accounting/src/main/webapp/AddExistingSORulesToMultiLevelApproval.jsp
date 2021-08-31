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
        String typeid = request.getParameter("typeid");
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
        if (StringUtil.isNullOrEmpty(typeid)) {
            typeid = "2";
        }
        if (StringUtil.isNullOrEmpty(moduleid)) {
            moduleid = "20";
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
            
            query = "select id,typeid,rulename,company,approvallevel,value from approvalrules where company=? AND fieldtype='1' AND typeid=?"; // fieldtype means applied on field in this case it is Total Amount, typeid means for which module in this case it is Sales Order
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            stmt1.setString(2, typeid);
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
            String ruleid = "";
                ruleid = rs1.getString("id");
                if (!StringUtil.isNullOrEmpty(ruleid)) { 
                    int approvallevel=rs1.getInt("approvallevel");
                        for(int i=1;i<=approvallevel;i++){
                          ruleid=UUID.randomUUID().toString();  
                    query = "insert into multilevelapprovalrule (id,level,rule,companyid,hasapprover,moduleid,appliedupon) values (?,?,?,?,?,?,?)";
                    PreparedStatement stmt2 = conn.prepareStatement(query);
                    stmt2.setString(1, ruleid);
                    stmt2.setInt(2, i);
                    stmt2.setString(3, "$$>"+rs1.getString("value"));
                    stmt2.setString(4, rs1.getString("company"));
                    stmt2.setInt(5, 0);
                    stmt2.setInt(6, Integer.parseInt(moduleid));
                    stmt2.setInt(7, 1);
                    stmt2.execute();
                    prRuleAddCounter++;
                    }
                }
            }
            out.println("<br><br> Approval Rule Records are successfully <b>Added</b> for " + companyname + " : " + prRuleAddCounter );
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