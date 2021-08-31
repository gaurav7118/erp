
<%-- 
    Document   : DeleteDuplicateAddresses
    Created on : Apr 2, 2018, 4:42:43 PM
    Author     : krawler
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");  //"192.168.0.104";   
        String port = "3306";
        String dbName = request.getParameter("dbname");//"stagingaccounting_04042016";
        String userName = request.getParameter("username");//"krawler";
        String password = request.getParameter("password");//"krawler";
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(subdomain)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,dbname,username,password,subdomain) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);

        String query = "select companyid,companyname from company";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query += " where subdomain = ?";
        }
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalAffectedRecords = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");
            String query1 = "select id,acccode from customer where company= ?";
            PreparedStatement stmt1 = conn.prepareStatement(query1);
            stmt1.setString(1, companyId);

            ResultSet rs1 = stmt1.executeQuery();

            while (rs1.next()) {
                int cnt = 0;
                String custid = rs1.getString("id");
                String acccode = rs1.getString("acccode");
                    //For deleting duplicate customer billing addresses
                    query = "select id,aliasname from customeraddressdetails where customerid=? and isbillingaddress='T'";
                    PreparedStatement stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, custid);
                    ResultSet rs3 = stmt3.executeQuery();
                    List<String> addressdetails = new ArrayList<String>();
                    while (rs3.next()) {
                        String aliasname = rs3.getString("aliasname");
                        String id = rs3.getString("id");
                        if (addressdetails.contains(aliasname)) {
                            query = "delete from customeraddressdetails where id=?";
                            PreparedStatement stmt4 = conn.prepareStatement(query);
                            stmt4.setString(1, id);
                            int deletedcnt = 0;
                            deletedcnt = stmt4.executeUpdate();
                            if (deletedcnt >= 1) {
                                out.print("<br>duplicate billing addresses deleted for customer" + acccode + "<br>");
                                totalAffectedRecords += 1;
                            }
                        } else {
                            addressdetails.add(aliasname);
                        }
                    }
                    
                    //For deleting duplicate customer shipping addresses
                    query = "select id,aliasname from customeraddressdetails where customerid=? and isbillingaddress='F'";
                    stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, custid);
                    rs3 = stmt3.executeQuery();
                    addressdetails = new ArrayList<String>();
                    while (rs3.next()) {
                        String aliasname = rs3.getString("aliasname");
                        String id = rs3.getString("id");
                        if (addressdetails.contains(aliasname)) {
                            query = "delete from customeraddressdetails where id=?";
                            PreparedStatement stmt4 = conn.prepareStatement(query);
                            stmt4.setString(1, id);
                            int deletedcnt = 0;
                            deletedcnt = stmt4.executeUpdate();
                            if (deletedcnt >= 1) {
                                out.print("<br>duplicate shipping addresses deleted for customer" + acccode + "<br>");
                                totalAffectedRecords += 1;
                            }
                        } else {
                            addressdetails.add(aliasname);
                        }
                    }
            }
            
            query1 = "select id,acccode from vendor where company= ?";
            stmt1 = conn.prepareStatement(query1);
            stmt1.setString(1, companyId);

            rs1 = stmt1.executeQuery();

            while (rs1.next()) {
                int cnt = 0;
                String custid = rs1.getString("id");
                String acccode = rs1.getString("acccode");
                    //For deleting duplicate Vendor billing addresses
                    query = "select id,aliasname from vendoraddressdetails where vendorid=? and isbillingaddress='T'";
                    PreparedStatement stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, custid);
                    ResultSet rs3 = stmt3.executeQuery();
                    List<String> addressdetails = new ArrayList<String>();
                    while (rs3.next()) {
                        String aliasname = rs3.getString("aliasname");
                        String id = rs3.getString("id");
                        if (addressdetails.contains(aliasname)) {
                            query = "delete from vendoraddressdetails where id=?";
                            PreparedStatement stmt4 = conn.prepareStatement(query);
                            stmt4.setString(1, id);
                            int deletedcnt = 0;
                            deletedcnt = stmt4.executeUpdate();
                            if (deletedcnt >= 1) {
                                out.print("<br>duplicate billing addresses deleted for vendor" + acccode + "<br>");
                                totalAffectedRecords += 1;
                            }
                        } else {
                            addressdetails.add(aliasname);
                        }
                    }
                    
                    //For deleting duplicate Vendor shipping addresses
                    query = "select id,aliasname from vendoraddressdetails where vendorid=? and isbillingaddress='F'";
                    stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, custid);
                    rs3 = stmt3.executeQuery();
                    addressdetails = new ArrayList<String>();
                    while (rs3.next()) {
                        String aliasname = rs3.getString("aliasname");
                        String id = rs3.getString("id");
                        if (addressdetails.contains(aliasname)) {
                            query = "delete from vendoraddressdetails where id=?";
                            PreparedStatement stmt4 = conn.prepareStatement(query);
                            stmt4.setString(1, id);
                            int deletedcnt = 0;
                            deletedcnt = stmt4.executeUpdate();
                            if (deletedcnt >= 1) {
                                out.print("<br>duplicate shipping addresses deleted for vendor" + acccode + "<br>");
                                totalAffectedRecords += 1;
                            }
                        } else {
                            addressdetails.add(aliasname);
                        }
                    }
            }
        }
        out.print("Total affected records: "+totalAffectedRecords);
        }catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    }finally {
        if (conn != null) {
            conn.close();
        }
    }

%>
