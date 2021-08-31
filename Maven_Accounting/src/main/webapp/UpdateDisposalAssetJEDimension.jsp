<%-- 
    Document   : UpdateDisposalAssetJEDimension
    Created on : 29 Aug, 2016, 6:39:47 PM
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
        String currentJeDetailId = "";
        String assetDetailId = "";
        String companyid = "";
        String dimensionValue = "";
        String dimensionData = "";
        String jeDimensionData = "";
        String query1 = "";
        String query2 = "";
        String query3 = "";
        String insertQuery = "";
        ResultSet rs=null,rs1=null,rs2=null;
        if (!StringUtil.isNullOrEmpty(subDomain)) {
                query = "select jedetail.id as jedetailid,assetdetail.id as assetdetailid ,assetdetail.company as companyid from assetdetail "
                        + "inner join jedetail on assetdetail.disposalje=jedetail.journalEntry "
                        + "where jedetail.debit='T' and srno=3 and  assetdetail.isdisposed='1' and jedetail.company=(select companyid from company where subdomain=?)";
                stmtquery = conn.prepareStatement(query);
                stmtquery.setString(1, subDomain);
                rs = stmtquery.executeQuery();
                while (rs.next()) {
                        currentJeDetailId = rs.getString("jedetailid");
                        assetDetailId = rs.getString("assetdetailid");
                        companyid = rs.getString("companyid");

                        String queryFieldParams = "select colnum from fieldparams where companyid = ? and moduleid = ? and fieldlabel='Entity'";
                        stmtquery = conn.prepareStatement(queryFieldParams);
                        stmtquery.setString(1, companyid);
                        stmtquery.setInt(2, 121);
                        ResultSet rsFieldParams = stmtquery.executeQuery();
                        String column = "col";
                        if(rsFieldParams.next()){
                            column += rsFieldParams.getString("colnum");
                        }
                        
                        /*Get Asset ID dimension value from asset detail id*/
                        query1 = "select "+column+" as dimensionValue from assetdetailcustomdata where assetDetailsId='" + assetDetailId + "' and company=(select companyid from company where subdomain=?)";
                        stmtquery = conn.prepareStatement(query1);
                        stmtquery.setString(1, subDomain);
                        rs1 = stmtquery.executeQuery();
                        while (rs1.next()) {
                            dimensionValue = rs1.getString("dimensionValue");
                        }

                        /*Get the Value of dimension from field combo data*/
                        query2 = "select value as data from fieldcombodata where id='" + dimensionValue+"'";
                        stmtquery = conn.prepareStatement(query2);
                        rs1 = stmtquery.executeQuery();

                        while (rs1.next()) {
                            dimensionData = rs1.getString("data");
                        }
                        
                        /*Finally Get the JE details dimension value from dimensionData*/
                        query3 = "select id as jedetailDimVal from fieldcombodata where fieldid in (select id from fieldparams where companyid=(select companyid from company where subdomain=?) and fieldlabel='Entity' and moduleid=24) and value='" + dimensionData+"'";
                        stmtquery = conn.prepareStatement(query3);
                        stmtquery.setString(1, subDomain);
                        rs2 = stmtquery.executeQuery();
                        
                         while (rs2.next()) {
                            jeDimensionData = rs2.getString("jedetailDimVal");
                        }
                         
                        out.println("<br> Record:= "+count+ "JE detail ID= "+currentJeDetailId);
                         
                         
                         /*Insert  Query*/
                             insertQuery = "INSERT INTO  accjedetailcustomdata (jedetailId ,col4, company,recdetailId,moduleId) VALUES (?,?,?,?,?)";
                             stmtquery = conn.prepareStatement(insertQuery);
                             stmtquery.setString(1, currentJeDetailId);
                             stmtquery.setString(2, jeDimensionData);
                             stmtquery.setString(3, companyid);
                             stmtquery.setString(4, currentJeDetailId);
                             stmtquery.setInt(5, 24);
                             stmtquery.executeUpdate();
                             count++;

                    }
               
               
            }
        
        out.println("<br>Total Record updated :="+count);
        

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
