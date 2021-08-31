<%-- 
    Document   : updateCustomerFields
    Created on : July 17, 2017, 11:27:11 AM
    Author     : krawler
    Purpose: Update cheque name of existing cheque sequence format as they were saved with startnumber + accountid + numberofdigits .
--%>

<%--<%@page import="java.lang.System.out"%>--%>
<%@page import="org.krysalis.barcode4j.tools.Length"%>
<%@page import="java.util.UUID"%>
<%@page import="java.math.BigInteger"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


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

        if (StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            out.println("Parameter missing from parameters=> [serverip,dbname,username,password] ");
        } else {
            String connectDB = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectDB, userName, password);

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
            while (comprs.next()) {
                String companyid = comprs.getString("companyid");
                String companyname = comprs.getString("companyname");
                String compSubDomain = comprs.getString("subdomain");
                try {
                    if (!StringUtil.isNullOrEmpty(companyid)) {
//                        
                            String selectExistingSeqFormatQuery = "select id,numberofdigits from chequesequenceformat where company=? ";
                            PreparedStatement existingChequeStmt = conn.prepareStatement(selectExistingSeqFormatQuery);
                            existingChequeStmt.setString(1, companyid);
                            ResultSet existingChequeRS = existingChequeStmt.executeQuery();
                            while(existingChequeRS.next()) {
                                String id = existingChequeRS.getString("id");
                                int numberOfSDigit = existingChequeRS.getInt("numberofdigits");
                                String name="";
                                for (int i = 0; i < numberOfSDigit; i++) {
                                    name +="0";
                                }
                                
                                String addChequeSequenceFormatQuery = "update chequesequenceformat set name=? where id=? and company=?";
                                PreparedStatement insertstmt = conn.prepareStatement(addChequeSequenceFormatQuery);
                                insertstmt.setString(1, name);
                                insertstmt.setString(2, id);
                                insertstmt.setString(3, companyid);
                                insertstmt.execute();
                            }
                        }
                        out.println("</br>Data Successfully updated for <b>Company:-</b> " + companyname + ", <b>Subdomain:-</b>" + compSubDomain);
//                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    out.println("</br>Failed to update data for <b>Company:-</b> " + companyname + ", <b>Subdomain:-</b>" + compSubDomain);
                }
            }
        }
        if (conn != null) {
            out.println("</br>Script Execution Completed Sucessfully.Thanks!!!");
            conn.close();//finally release connection   
        }
    } catch (Exception e) {
        e.printStackTrace();
//        out.println(e.getMessage());

    } finally {
        conn.close();
    }

%>
