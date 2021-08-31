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
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) ) {
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
        PreparedStatement dupPstn=null;
        ResultSet dupRes=null;
        
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
           
           String query1 = "select * from  defaultmasteritem where masterGroup=62";
                PreparedStatement stmt2 = conn.prepareStatement(query1);
                ResultSet rs2 = stmt2.executeQuery();
                while (rs2.next()) {
                    String uuid = UUID.randomUUID().toString();
                        String oldid = rs2.getString("id");
                        String value = rs2.getString("value");
                        /*
                        previously, repeated execution of this Script was leading to duplicate entries in database so I have put this check to avoid duplication.
                        */
                        String dupQuery="select * from masteritem where value='"+value+"' and masterGroup=62 and company='"+companyId+"'";
                        dupPstn=conn.prepareStatement(dupQuery);
                        dupRes=dupPstn.executeQuery();
                        if(dupRes.next()){
                            continue;
                        }
                        String query41 = "INSERT INTO masteritem (id,value,masterGroup,defaultmasteritem,company) VALUES (?,?,?,?,?) ";
                        PreparedStatement stmt4 = conn.prepareStatement(query41);
                        stmt4.setString(1, uuid);
                        stmt4.setString(2, value);
                        stmt4.setString(3, "62");
                        stmt4.setString(4, oldid);
                        stmt4.setString(5, companyId);
                        stmt4.execute();
               
               
                 }
                
                
                 String query2 = "select * from  defaultmasteritem where masterGroup=63";
                PreparedStatement stmt3 = conn.prepareStatement(query2);
                ResultSet rs3 = stmt3.executeQuery();
                while (rs3.next()) {
                    String uuid = UUID.randomUUID().toString();
                        String oldid = rs3.getString("id");
                        String value = rs3.getString("value");
                        /*
                        previously, repeated execution of this Script was leading to duplicate entries in database so I have put this check to avoid duplication.
                        */
                        String dupQuery="select * from masteritem where value='"+value+"' and masterGroup=63 and company='"+companyId+"'";
                        dupPstn=conn.prepareStatement(dupQuery);
                        dupRes=dupPstn.executeQuery();
                        if(dupRes.next()){
                            continue;
                        }
                        String query41 = "INSERT INTO masteritem (id,value,masterGroup,defaultmasteritem,company) VALUES (?,?,?,?,?) ";
                        PreparedStatement stmt4 = conn.prepareStatement(query41);
                        stmt4.setString(1, uuid);
                        stmt4.setString(2, value);
                        stmt4.setString(3, "63");
                        stmt4.setString(4, oldid);
                        stmt4.setString(5, companyId);
                        stmt4.execute();
               
                 }
                out.println("\nCompleted for companyid "+companyName);   
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