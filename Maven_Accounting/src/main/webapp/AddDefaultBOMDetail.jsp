<%-- 
    Document   : AddDefaultBOMDetail
    Created on : Apr 23, 2016, 10:21:55 AM
    Author     : krawler
--%>
<%@page import="com.krawler.common.util.StringUtil"%>

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
        String serverip = request.getParameter("serverip");  //"192.168.0.104";   
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"stagingaccounting_04042016";
        String userName = request.getParameter("username");//"krawler";
        String password = request.getParameter("password");//"krawler";
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname from company";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query += " where subdomain= ?";
        }
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");
            String companyname = rs.getString("companyname");
            query = "select id from product where producttype = 'e4611696-515c-102d-8de6-001cc0794cfa' and company=?"; // Assembly Type Product
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                do {
                    String productid = rs1.getString("id");
                    if (!StringUtil.isNullOrEmpty(productid)) {
                        try {
                            String checkIfDefaultBOMAdded = "select * from bomdetail where product= ? and isdefaultbom=? ";
                            PreparedStatement defaultStmt=conn.prepareStatement(checkIfDefaultBOMAdded);
                            defaultStmt.setString(1,productid);
                            defaultStmt.setString(2, "T");
                            ResultSet defaultRs=defaultStmt.executeQuery();
                            if(defaultRs.next()){ // do not add the bomdetail if default BOM is already added.
                                continue;
                            }
                            boolean isDefaultBOMAdded = false;
                            String bomid = "";
                            // Add Default BOM for Assembly Type Product
                            String productAssemblyQuery = "select id from productassembly where product=?";
                            PreparedStatement stmt2 = conn.prepareStatement(productAssemblyQuery);
                            stmt2.setString(1, productid);
                            ResultSet rs2 = stmt2.executeQuery();
                            if (rs2.next()) {
                                do {
                                    if (!isDefaultBOMAdded) { // check if default BOM for product is added
                                        String insertquery = "insert into bomdetail(id,bomcode,bomname,product,isdefaultbom) values(?,?,?,?,?)";
                                        PreparedStatement insertstmt = conn.prepareStatement(insertquery);
                                        bomid = UUID.randomUUID().toString();
                                        insertstmt.setString(1, bomid);
                                        insertstmt.setString(2, "Default BOM");
                                        insertstmt.setString(3, "Default BOM");
                                        insertstmt.setString(4, productid);
                                        insertstmt.setString(5, "T");
                                        insertstmt.executeUpdate();
                                        insertstmt.close();
                                        isDefaultBOMAdded = true;
                                    }
                                    String productassemblyid = rs2.getString("id");
                                    String updateQuery = "UPDATE productassembly SET bomdetail=? where id=?";
                                    PreparedStatement statement1 = conn.prepareStatement(updateQuery);
                                    statement1.setString(1, bomid);
                                    statement1.setString(2, productassemblyid);
                                    statement1.executeUpdate();
                                    statement1.close();
                                } while (rs2.next());
                            }

                            // add subbom detail in product assembly
                            if (!StringUtil.isNullOrEmpty(bomid)) {
                                String subProductQuery = "select id from productassembly where subproducts=?";
                                PreparedStatement stmt3 = conn.prepareStatement(subProductQuery);
                                stmt3.setString(1, productid);
                                ResultSet rs3 = stmt3.executeQuery();
                                if (rs3.next()) {
                                    do {
                                        String productassemblyid = rs3.getString("id");
                                        String updateQuery = "UPDATE productassembly SET subbom=? where id=?";
                                        PreparedStatement statement1 = conn.prepareStatement(updateQuery);
                                        statement1.setString(1, bomid);
                                        statement1.setString(2, productassemblyid);
                                        statement1.executeUpdate();
                                        statement1.close();
                                    } while (rs3.next());
                                }
                            }

                            // update default BOM in productbuild 
                            if (!StringUtil.isNullOrEmpty(bomid)) {
                                String subProductQuery = "select id from productbuild where product=?";
                                PreparedStatement stmt3 = conn.prepareStatement(subProductQuery);
                                stmt3.setString(1, productid);
                                ResultSet rs3 = stmt3.executeQuery();
                                if (rs3.next()) {
                                    do {
                                        String productbuildid = rs3.getString("id");
                                        String updateQuery = "UPDATE productbuild SET bomdetail=? where id=?";
                                        PreparedStatement statement1 = conn.prepareStatement(updateQuery);
                                        statement1.setString(1, bomid);
                                        statement1.setString(2, productbuildid);
                                        statement1.executeUpdate();
                                        statement1.close();
                                    } while (rs3.next());
                                }
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            out.println("Exception occurred " + ex.toString() + " for product id " + productid);
                        }
                    }
                } while (rs1.next());
            }
            totalCompanyUpdationCnt++;
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>
