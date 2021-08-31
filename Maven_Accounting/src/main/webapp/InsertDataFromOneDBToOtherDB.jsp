<%-- 
    Document   : InsertDataFromOneDBToOTherDB
    Created on : June 28, 2017 
    Author     : krawler
--%>

<%@page import="com.krawler.spring.accounting.invoice.accDeliveryPlannerController"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.krawler.common.util.Constants"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.krawler.spring.authHandler.authHandler"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.HashSet"%>
<%@page import="com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO"%>
<%@page import="com.krawler.spring.accounting.invoice.accInvoiceDAO"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.*"%>
<%
    Connection conn = null;
    try {
        String port = "3306";
        String driver = "com.mysql.jdbc.Driver";

        String serverip = request.getParameter("serverip") != null ? request.getParameter("serverip") : "";
        String dbName = request.getParameter("dbname") != null ? request.getParameter("dbname") : "";
        String userName = request.getParameter("username") != null ? request.getParameter("username") : "";
        String password = request.getParameter("password") != null ? request.getParameter("password") : "";
        String subdomain = request.getParameter("subdomain") != null ? request.getParameter("subdomain") : "";
         //|| StringUtil.isNullOrEmpty(password)
        if (StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) ) {
            out.println("Parameter missing from parameters=> [serverip,dbname,username,password] ");
        } else {
            String connectDB = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectDB, userName, password);


            ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(application);
            accDeliveryPlannerController accDeliveryPlanner = (accDeliveryPlannerController) context.getBean("accDeliveryPlannerController");
            
            String query = "";
            PreparedStatement compstmt = null;
            if (StringUtil.isNullOrEmpty(subdomain)) {
                query = "select companyname,subdomain,companyid from company";
                compstmt = conn.prepareStatement(query);
            } else {
                query = "select companyname,subdomain,companyid from company where subdomain=?";
                compstmt = conn.prepareStatement(query);
                compstmt.setString(1, subdomain);
            }
            ResultSet comprs = compstmt.executeQuery();
            DateFormat sdf = new SimpleDateFormat(Constants.yyyyMMdd);
            while (comprs.next()) {
                String companyid = comprs.getString("companyid");
                String companyname = comprs.getString("companyname");
                String compSubDomain = comprs.getString("subdomain");
                if (!StringUtil.isNullOrEmpty(companyid)) {
                    request.setAttribute("subdomain", compSubDomain);
                     accDeliveryPlanner.getInsertDataFromOneDatabaseToOtherDatabase(request, response);
                }
            }
            out.println("</br></br>Script executed successfully.Thanks!!! ");
        }

    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>
