<%-- 
    Document   : updatePriceBandMappingWithVolDisc
    Created on : 14 Oct, 2016, 11:48:04 AM
    Author     : krawler
--%>


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%
    String message = "";
    Connection conn = null;
    try {
        
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        int count=0;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery;
        String query = "";
        String pricebandID = "";
        String volumeDiscountid = "";
        String companyid = "";
        String insertQuery = "";
        ResultSet rs=null,rs1=null;
        if (!StringUtil.isNullOrEmpty(subDomain)) {
            query = "SELECT companyid,subdomain FROM company where subdomain= ? ORDER BY companyid ASC ";
            stmtquery = conn.prepareStatement(query);
            stmtquery.setString(1, subDomain);
            rs = stmtquery.executeQuery();
        } else {
            query = "SELECT companyid,subdomain FROM company ORDER BY companyid ASC ";
            stmtquery = conn.prepareStatement(query);
            rs = stmtquery.executeQuery();
        }
        while (rs.next()) {
            companyid = rs.getString("companyid");
            subDomain = rs.getString("subdomain");
        if (!StringUtil.isNullOrEmpty(companyid)) {
                query = "select id from pricingbandmaster where  id not in (select pricebandid from pricingbandmappingwithvolumedisc) and volumediscount='F' and company= ?";
                stmtquery = conn.prepareStatement(query);
                stmtquery.setString(1, companyid);
                rs1 = stmtquery.executeQuery();
                while (rs1.next()) {
                        pricebandID = rs1.getString("id");
                        
                        String queryVolumeDiscountIDs = "SELECT id FROM pricingbandmaster WHERE volumediscount='T'and company=?";
                        stmtquery = conn.prepareStatement(queryVolumeDiscountIDs);
                        stmtquery.setString(1, companyid);
                        ResultSet VolumeDiscountIDs = stmtquery.executeQuery();
                         while (VolumeDiscountIDs.next()) {
                             volumeDiscountid = VolumeDiscountIDs.getString("id");
                         
                         /*Insert  Query*/
                             insertQuery = "INSERT INTO  pricingbandmappingwithvolumedisc (id,pricebandid ,volumediscountid) VALUES (UUID(),?,?)";
                             stmtquery = conn.prepareStatement(insertQuery);
                             stmtquery.setString(1, pricebandID);
                             stmtquery.setString(2, volumeDiscountid);
                             stmtquery.executeUpdate();
                             count++;
                         }

                }                                      
               
            }
                out.println(count + " Record updated for:= " + subDomain +"<br>");
        }
        

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
        out.print(message);
    }
%>
