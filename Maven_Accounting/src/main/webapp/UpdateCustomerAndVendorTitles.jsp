<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<%
    Connection conn = null;
    try {

        String serverip = request.getParameter("serverip");
        String port = request.getParameter("port");
        String dbName = request.getParameter("dbname");
         String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
       
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String companyId = "";
        String companyName = "";
        int recordsno = 0;
        String query = "";
        ResultSet rs;
        
        if (!StringUtil.isNullOrEmpty(subDomain)) {
            query = "SELECT companyid,subdomain FROM company where subdomain= ? ";
            PreparedStatement stmtquery = conn.prepareStatement(query);
            stmtquery.setString(1, subDomain);
            rs = stmtquery.executeQuery();
        } else {
            query = "SELECT companyid,subdomain FROM company ";
            PreparedStatement stmtquery = conn.prepareStatement(query);
            rs = stmtquery.executeQuery();
        }
        while (rs.next()) {
            companyId = rs.getString("companyid");
            companyName = rs.getString("subdomain");
            String queryForMasterItem = "SELECT * FROM masteritem WHERE masterGroup=6 AND company= ? ";
            PreparedStatement stmt1 = conn.prepareStatement(queryForMasterItem);
            stmt1.setString(1, companyId);
            ResultSet resultset = stmt1.executeQuery();
            recordsno = 0;
            while (resultset.next()) {
                String titleId = resultset.getString("id");
                String title = resultset.getString("value");

                String queryForVendor = " UPDATE vendor SET title=? WHERE title=? AND company= ? ";
                PreparedStatement stmt2 = conn.prepareStatement(queryForVendor);
                stmt2.setString(1, titleId);
                stmt2.setString(2, title);
                stmt2.setString(3, companyId);
                stmt2.executeUpdate();

                String queryForCustomer = " UPDATE customer SET title=? WHERE title=? AND company= ? ";
                PreparedStatement stmt3 = conn.prepareStatement(queryForCustomer);
                stmt3.setString(1, titleId);
                stmt3.setString(2, title);
                stmt3.setString(3, companyId);
                stmt3.executeUpdate();
                recordsno ++;
            }
            out.println(String.valueOf(recordsno)+" Records updated for "+companyName );
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