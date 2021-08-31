<%-- 
    Document   : Addbatchdetailstocorruptedproduct
    Created on : April 5, 2018, 12:54:58 PM
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

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
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
        int totalCompanyUpdationCnt = 0;
         int batch_details_update_count=0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");
            String query1 = "select product,quantity from  inventory inv where inv.carryin='T' and inv.isopening='T' and inv.newinv='T' and quantity>0 and company = ?;";
            PreparedStatement stmt1 = conn.prepareStatement(query1);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            
            while (rs1.next()) {
                String productid = rs1.getString("product");
                String quantity = rs1.getString("quantity");
                String query2 = "select warehouse,location from product where id = ? and company =? ";
                String query4="select documentid from locationbatchdocumentmapping where documentid= ? ";
                PreparedStatement stmt4 = conn.prepareStatement(query4);
                stmt4.setString(1, productid);
                ResultSet rs4 = stmt4.executeQuery();
                PreparedStatement stmt2 = conn.prepareStatement(query2);
                stmt2.setString(1, productid);
                stmt2.setString(2, companyId);
                ResultSet rs2 = stmt2.executeQuery();
                int count =0;
                    while (rs4.next()){
                        count++;
                    }
                    if (count == 0) { //
                            while (rs2.next()) {
                                String warehouse = rs2.getString("warehouse");
                                String location = rs2.getString("location");
                                String query3 = "select id from newproductbatch where warehouse = ? and location= ? and product = ? and company = ? group by product,warehouse,location;";
                                PreparedStatement stmt3 = conn.prepareStatement(query3);
                                stmt3.setString(1, warehouse);
                                stmt3.setString(2, location);
                                stmt3.setString(3, productid);
                                stmt3.setString(4, companyId);
                                ResultSet rs3 = stmt3.executeQuery();
                                while (rs3.next()){
                                String batchmapid = rs3.getString("id");
                                try {
                                    String insertquery = "insert locationbatchdocumentmapping (id,transactiontype,documentid,quantity,batchmapid) values(?,?,?,?,?)";
                                    PreparedStatement insertstmt = conn.prepareStatement(insertquery);
                                    String id = "";
                                    id = UUID.randomUUID().toString();
                                    insertstmt.setString(1, id);
                                    insertstmt.setString(2, "28");
                                    insertstmt.setString(3, productid);
                                    insertstmt.setString(4, quantity);
                                    insertstmt.setString(5, batchmapid);
                                    insertstmt.executeUpdate();
                                    insertstmt.close();
                                    batch_details_update_count++;

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    out.println("Exception occurred " + ex.toString() + " ");
                                }
                                }
                            }
                    }
                        
                       
                
            }
            totalCompanyUpdationCnt++;
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
        out.println("<br><br> No of product's updated with batch details   " + batch_details_update_count);
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>