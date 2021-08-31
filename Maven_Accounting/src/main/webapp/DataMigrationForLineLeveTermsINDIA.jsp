
<%@page import="org.apache.commons.lang.StringUtils"%>
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
<%@page import="org.apache.poi.hssf.usermodel.HSSFCell"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFRow"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFSheet"%>
<%@page import="org.apache.poi.hssf.usermodel.HSSFWorkbook"%>
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
    StringBuilder UpdateCompany = new StringBuilder();
    StringBuilder NoUpdateCompany = new StringBuilder();
    try {

        ResultSet rscompany = null;
        PreparedStatement pstmt = null;
        pstmt = conn.prepareStatement("SELECT * FROM company WHERE country =105;");
        rscompany = pstmt.executeQuery();
        while (rscompany.next()) {
            String country = rscompany.getString("country");
            String companyid = rscompany.getString("companyid");

            PreparedStatement pstmtCheckMaster = null;
            pstmtCheckMaster = conn.prepareStatement("SELECT count(*) as count FROM masteritem WHERE company=? and masterGroup=?;");
            pstmtCheckMaster.setString(1, companyid);
            pstmtCheckMaster.setString(2, "37");
            ResultSet masterCheckRes = pstmtCheckMaster.executeQuery();
            int count = 0;
            if (masterCheckRes.next()) {
                count = masterCheckRes.getInt("count");
            }
            if (count < 7) {
                PreparedStatement pstmtMaster = null;
                pstmtMaster = conn.prepareStatement("SELECT * FROM defaultmasteritem WHERE country =? and masterGroup=?;");
                pstmtMaster.setString(1, country);
                pstmtMaster.setString(2, "37");
                ResultSet masterRes = pstmtMaster.executeQuery();
                while (masterRes.next()) {
                    String value = masterRes.getString("value");
                    String masterGroup = masterRes.getString("masterGroup");
                    String code = masterRes.getString("code");
                    String id = masterRes.getString("id");

                    PreparedStatement pstmtMasterUpdate = null;
                    pstmtMasterUpdate = conn.prepareStatement("INSERT INTO masteritem (id, value, masterGroup,code,company,defaultmasteritem) VALUE (?,?,?,?,?,?);");
                    String uid = UUID.randomUUID().toString();
                    pstmtMasterUpdate.setString(1, uid);
                    pstmtMasterUpdate.setString(2, value);
                    pstmtMasterUpdate.setString(3, masterGroup);
                    pstmtMasterUpdate.setString(4, code);
                    pstmtMasterUpdate.setString(5, companyid);
                    pstmtMasterUpdate.setString(6, id);
                    pstmtMasterUpdate.executeUpdate();

                    PreparedStatement pstmtLineUpdate = null;
                    pstmtLineUpdate = conn.prepareStatement("UPDATE linelevelterms  SET masteritem =? WHERE  termtype= ? AND company=? and masteritem is NULL;");
                    pstmtLineUpdate.setString(1, uid);
                    pstmtLineUpdate.setString(2, code);
                    pstmtLineUpdate.setString(3, companyid);
                    pstmtLineUpdate.executeUpdate();
                }
                UpdateCompany.append("<br>Company id : " + companyid);
            } else {
                NoUpdateCompany.append("<br>Company id : " + companyid);
            }
        }

    } catch (Exception ex) {
        ex.printStackTrace();
    } finally {
        conn.close();
        out.print("<b>Script executed successfully</b> <br><br>Updated Company ids : <br> "+UpdateCompany + "<br> No update company ids :<br>" +NoUpdateCompany);
    }
%>