<%-- 
    Document   : missingaccgroup
    Created on : Mar 26, 2014, 4:24:17 PM
    Author     : krawler
--%>

<%@page import="java.util.jar.Attributes.Name"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<%
      Connection conn = null;
       try {
        String serverip = "192.168.0.209";
        String port = "3306";
        String dbName = "newstaging";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "krawlersqladmin";
        String password = "krawler";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
       
        int cnt = 0,cnt2=0,cnt3=0;
        int displayorder=0;
        String record = "";
        String companyId="";
        String name="";
        String nature="";
        String affectgp="";
        String deleteflag="";
        String parent="";
        String isMasterGroup="";
        String oldid="";
      
       
              
                String query = "SELECT companyid FROM company";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                
               String sql5 = "select max(displayOrder) as MaxOrder from accgroup";
                PreparedStatement stmt5 = conn.prepareStatement(sql5);
                ResultSet rs1 = stmt5.executeQuery();
                if(rs1.next())
                {
                        displayorder = Integer.parseInt(rs1.getString("MaxOrder"));
                    }

                while (rs.next()) {
                companyId = rs.getString("companyid");
                String query1 = "select * from accgroup where deleteflag=false and parent is null  and company is null";
                PreparedStatement stmt2 = conn.prepareStatement(query1);
                ResultSet rs2 = stmt2.executeQuery();
                while (rs2.next()) {
                    String uuid =UUID.randomUUID().toString();
                    oldid = rs2.getString("id");
                    name = rs2.getString("name");
                    nature = rs2.getString("nature");
                    affectgp = rs2.getString("affectgp");
                    deleteflag = rs2.getString("deleteflag");
                    parent = rs2.getString("parent");
                    isMasterGroup = rs2.getString("isMasterGroup");
                     displayorder++;
                                   
                   
                    String sql = "insert into accgroup (id,name,nature,affectgp,displayorder,isMasterGroup,grpOldId,company,deleteflag) values(?,?,?,?,?,?,?,?,?)";
                    PreparedStatement stmt1 = conn.prepareStatement(sql);
                    stmt1.setObject(1, uuid);
                    stmt1.setObject(2, name);
                    stmt1.setObject(3, nature);
                    stmt1.setObject(4, affectgp);
                    stmt1.setObject(5, displayorder);
                    //stmt1.setObject(6, false);
                   // stmt1.setObject(7, companyId);
                    stmt1.setObject(6, isMasterGroup);
                    stmt1.setObject(7, oldid);
                    stmt1.setObject(8, companyId);
                    stmt1.setObject(9, false);
                    cnt = stmt1.executeUpdate();

                    String sql3 = "update accgroup set parent =? where parent =? and company=?";
                    PreparedStatement stmt3 = conn.prepareStatement(sql3);
                    stmt3.setObject(1, uuid);
                    stmt3.setObject(2, oldid);
                    stmt3.setObject(3, companyId);
                    cnt2 = stmt3.executeUpdate();
                        
                    String sql4 = "update account set groupname=? where company=? and groupname=?";
                    PreparedStatement stmt4 = conn.prepareStatement(sql4);
                    stmt4.setObject(1, uuid);
                    stmt4.setObject(2, companyId);
                    stmt4.setObject(3,oldid );
                    cnt3 = stmt4.executeUpdate();
                
                   }
                  out.println("Completed for companyid "+companyId);    
            }
        
        
        
        
        
    }
    catch(Exception e)
    {
        e.printStackTrace();
    }
    
%>
