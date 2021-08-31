
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
        String serverip = request.getParameter("serverip");                            
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
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
        while (rs.next()) {
            String companyId = rs.getString("companyid");
            String companyname = rs.getString("companyname");
            int cnAddCounter = 0;

            query = "select id from sequenceformat where moduleid=120 and deleted='F' and isdefaultformat='T' and isactivate='T' and company=? "; //Build Assembly moduleid is 120
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            String sequnceformatid = "";
            while (rs1.next()) {
                sequnceformatid = rs1.getString("id");
                break;
            }
            if (StringUtil.isNullOrEmpty(sequnceformatid)) { //This check avoid duplicate entry when script executed more than one time by mistake 
                String uuid = UUID.randomUUID().toString().replace("-", "");
                query = "insert into sequenceformat (id,name,prefix,suffix,numberofdigit,startfrom,deleted,showleadingzero,modulename,moduleid,company,isdefaultformat,isactivate) values (?,?,?,?,?,?,?,?,?,?,?,?,?) ";
                PreparedStatement stmt4 = conn.prepareStatement(query);
                stmt4.setString(1, uuid);
                stmt4.setString(2, "BA000000");
                stmt4.setString(3, "BA");
                stmt4.setString(4, "");
                stmt4.setInt(5, 6);
                stmt4.setInt(6, 1);
                stmt4.setString(7, "F");
                stmt4.setString(8, "T");
                stmt4.setString(9, "autobuildassembly");
                stmt4.setInt(10, 120);
                stmt4.setString(11, companyId);
                stmt4.setString(12, "T");
                stmt4.setString(13, "T");
                stmt4.execute();
                cnAddCounter++;
                out.println("<br><br> Default Build Assembly Sequence Format added successfully <b>Added</b> for " + companyname + " : " + cnAddCounter);
                totalCompanyUpdationCnt++;
            }
        }
        if (totalCompanyUpdationCnt == 0) {
            out.println("<br><br> Script Already Executed for Database " + dbName);
        } else {
            out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
            out.println("<br><br> Script Executed Sccessfully ");
        }

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>
 


