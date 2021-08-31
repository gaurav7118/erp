
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


<%
      Connection conn = null;
       try {
        String serverip = "192.168.0.116";
        String port = "3306";
        String dbName = "hcis_12112014";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "krawlersqladmin";
        String password = "krawler";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
        int cnt = 0;
        int count=0;
        String record = "";
        String companyId="";
        String query = "SELECT companyid FROM company";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                companyId = rs.getString("companyid");
                String selecttermquery = "SELECT termid FROM creditterm where company='" + companyId + "' and termdays=-1";
                PreparedStatement selecttermstmt = conn.prepareStatement(selecttermquery);
                ResultSet selecttermrs = selecttermstmt.executeQuery();
                if(!selecttermrs.next()){
                    count ++ ;
                    String uuid = UUID.randomUUID().toString().replace("-", "");
                    System.out.println(uuid);
                    String sql = "insert into creditterm (termid,termname,termdays,company) values('" + uuid + "', 'NET -1', -1, '" + companyId + "')";
                    PreparedStatement stmt1 = conn.prepareStatement(sql);
                    cnt = stmt1.executeUpdate();
                 }
            }
            out.print("Executed for"+count +" companies : ");
        } catch (Exception e) {
        e.printStackTrace();
    }
    
%>
