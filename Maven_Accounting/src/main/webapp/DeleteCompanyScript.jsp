
<%--
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
--%>
<%@page import="javax.mail.Session"%>
<%@page import="org.hibernate.Query"%>
<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page language="java" %>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.DriverManager"%>
<%@ page import="com.krawler.common.util.URLUtil" %>
<%@ page import="com.krawler.esp.utils.ConfigReader" %>
<%@page import="com.krawler.esp.handlers.SendMailHandler"%>
<%@page import="javax.mail.MessagingException"%>
<%@page import="com.krawler.esp.web.resource.Links"%>
<%@page import="com.krawler.spring.sessionHandler.sessionHandlerImpl" %>
<%@page import="com.krawler.common.session.SessionExpiredException" %>
<%@page import="com.krawler.common.util.StringUtil"%>
<%
    Connection conn = null;
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        java.sql.CallableStatement callStmt = null;
        int deletedCount = 0, notDeletedCount = 0;

    //---------------------------------------Mail id's------------------------------------------------------------
        String[] emailids = {"kuldeep.singh@krawlernetworks.com", "sagar.mahamuni@krawlernetworks.com", "swapnil.pandhare@krawler.com"};

    //-------------------------------------------------------------------------------------------------------------------

//    DbResults rs1 = DbUtil.executeQuery(conn, "select subdomain,companyid from company where subdomain COLLATE utf8_general_ci not in (select subdomain from ondemandprod.company);");

            ResultSet rs1 = null;

            PreparedStatement pstmt = null;

            //DbResults rs1 = DbUtil.executeQuery(conn, "select subdomain,companyid from company where companyid='008d4818-008d-4d6f-8ca4-434291cbc6b4';");
//            String fetchRecords = "select subdomain,companyid from company where companyid='59267cec-8661-4538-a8cb-dfa7e0199c66';";
            String fetchRecords = "select subdomain,companyid from company where subdomain COLLATE utf8_general_ci not in (select subdomain from ondemandprod.company)";
            pstmt = conn.prepareStatement(fetchRecords);
            rs1 = pstmt.executeQuery();
    
    
    try {
        while (rs1.next() && !rs1.wasNull()) {

            try {
                callStmt = conn.prepareCall("{call deletecompanydata(?,?)}");
                callStmt.setString(1, "staginginvacc"); // staging database name
                callStmt.setString(2, rs1.getString("subdomain"));
                callStmt.executeUpdate();
                
                
                out.println("\n \u001B[32m" + rs1.getString("subdomain") + " is deleted " + "\u001B[0m");
                deletedCount++;

                SendMailHandler.postMail(emailids, "Deleted " + rs1.getString("subdomain") + " company.", "", "", "admin@deskera.com", rs1.getString("companyid"));

            } catch (Exception e) {
                out.println("\n \u001B[31m" + rs1.getString("subdomain") + " is Not deleted " + "\u001B[0m");
                notDeletedCount++;
                SendMailHandler.postMail(emailids, "Deleted " + rs1.getString("subdomain") + " company Failed.", "", "", "admin@deskera.com", rs1.getString("companyid"));
            }
        }

        out.println("\n \u001B[34m" + "Deleted subdomains: " + deletedCount + "\u001B[0m");
        out.println("\n \u001B[34m" + "Not Deleted subdomains: " + notDeletedCount + "\u001B[0m");
    } catch (Exception ex) {
        ex.printStackTrace();
    } finally {
        conn.commit();
        conn.close();
        callStmt.close();
    }
%>