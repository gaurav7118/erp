<%@page import="com.krawler.common.util.StringUtil"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
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
                    String companyId = comprs.getString("companyid");
                    String companyname = comprs.getString("companyname");
                    String compSubdomain = comprs.getString("subdomain");
                    out.println("</br>Company Name : <b>" + companyname + "</b> and Subdomain: <b>"+compSubdomain+"</b>"); 
                    query = "select * from companyaddressdetails where company=?";
                    PreparedStatement compAddrStmt = conn.prepareStatement(query);
                    compAddrStmt.setString(1, companyId);
                    ResultSet compAddrRS = compAddrStmt.executeQuery();
                    if (!compAddrRS.next()) {//This check is avoid multiple execution of script
                        String addrSelectQuery = "select billaddress,shipaddress from compaccpreferences where id=?";
                        PreparedStatement addrStmt = conn.prepareStatement(addrSelectQuery);
                        addrStmt.setString(1, companyId);
                        ResultSet addrRS = addrStmt.executeQuery();
                        if (addrRS.next()) {
                            String billAddress = addrRS.getString("billaddress");
                            String shipAddress = addrRS.getString("shipaddress");
                            if (!StringUtil.isNullOrEmpty(billAddress) && !billAddress.equalsIgnoreCase("Enter a Billing Address")) {
                                String uuid = UUID.randomUUID().toString().replace("-", "");
                                String insertQuery="INSERT INTO companyaddressdetails (id,aliasname,address,isbillingaddress,isdefaultaddress,company) VALUES (?,?,?,?,?,?)";
                                PreparedStatement insertStat=conn.prepareStatement(insertQuery);
                                insertStat.setString(1,uuid);
                                insertStat.setString(2,"Billing Address1");
                                insertStat.setString(3,billAddress);
                                insertStat.setString(4,"T");
                                insertStat.setString(5,"T");
                                insertStat.setString(6,companyId);
                                insertStat.execute();
                                out.println("</br>Billing Address updated.");
                            }
                            if (!StringUtil.isNullOrEmpty(shipAddress) && !shipAddress.equalsIgnoreCase("Enter a Shipping Address")) {
                                String uuid = UUID.randomUUID().toString().replace("-", "");
                                String insertQuery="INSERT INTO companyaddressdetails (id,aliasname,address,isbillingaddress,isdefaultaddress,company) VALUES (?,?,?,?,?,?)";
                                PreparedStatement insertStat=conn.prepareStatement(insertQuery);
                                insertStat.setString(1,uuid);
                                insertStat.setString(2,"Shipping Address1");
                                insertStat.setString(3,shipAddress);
                                insertStat.setString(4,"F");
                                insertStat.setString(5,"T");
                                insertStat.setString(6,companyId);
                                insertStat.execute();
                                out.println("</br>Shipping Address updated.");
                            }
                        }
                    } else{
                        out.println("</br>Script is already executed or user has manually entered company address.");  
                    }
                }
            out.println("</br><b>Script execution is completed. Thanks!");  
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally{
        if(conn!=null){
            conn.close();        
        }
    }

%>