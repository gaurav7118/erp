<%-- 
    Document   : addinvoicetermforcorruptedinvoices
    Created on : Mar 3, 2018, 12:54:58 PM
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
        while (rs.next()) {
            String companyId = rs.getString("companyid");
            String query1 = "select Distinct (gr.grnumber),gr.id ,grol.linkeddocid,grol.docid,createdby from goodsreceipt gr "
                    + " inner join grdetails grd on grd.goodsreceipt=gr.id "
                    + " inner join company comp on comp.companyid=gr.company "
                    + " inner join goodsreceiptorderlinking grol on grol.linkeddocid=gr.id "
                    + " inner join goodsreceiptordertermmap grot on grol.docid=grot.goodsreceiptorder "
                    + " where round(gr.invoiceamount,2) != round(gr.excludinggstamount,2) and grd.rowtermamount=0 and gr.gstincluded ='F' and gr.taxamount=0  and gr.isopeningbalenceinvoice='F' and gr.company = ? and gr.id not in (select goodsreceipt from receipttermsmap ) ";
            PreparedStatement stmt1 = conn.prepareStatement(query1);
            stmt1.setString(1, companyId);
            
            ResultSet rs1 = stmt1.executeQuery();
            
            while (rs1.next()) {

                String grid = rs1.getString("id");
                String groid = rs1.getString("docid");
                String creator = rs1.getString("createdby");

                query = "select term,termamount from goodsreceiptordertermmap where goodsreceiptorder = ? ";
                PreparedStatement stmt2 = conn.prepareStatement(query);
                stmt2.setString(1, groid);
                ResultSet rs2 = stmt2.executeQuery();
                
                    while (rs2.next()) {
                        String term = rs2.getString("term");
                        String termamount = rs2.getString("termamount");

                        try {
                            String insertquery = "insert receipttermsmap (id,term,goodsreceipt,termamount,creator) values(?,?,?,?,?)";
                            PreparedStatement insertstmt = conn.prepareStatement(insertquery);
                            String id = "";
                            id = UUID.randomUUID().toString();
                            insertstmt.setString(1, id);
                            insertstmt.setString(2, term);
                            insertstmt.setString(3, grid);
                            insertstmt.setString(4, termamount);
                            insertstmt.setString(5, creator);
                            insertstmt.executeUpdate();
                            insertstmt.close();

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            out.println("Exception occurred " + ex.toString() + " ");
                        }

                    }
                
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
