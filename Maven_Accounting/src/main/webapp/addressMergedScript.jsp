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
               String serverip = "192.168.0.164";
               String port = "3306";
               String dbName = "accountingsats_31052014";
               String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
               String url = "jdbc:mysql@" + serverip + ":" + port + "/";
               String driver = "com.mysql.jdbc.Driver";
               String userName = "krawlersqladmin";
               String password = "krawler";
               Class.forName(driver).newInstance();
               conn = DriverManager.getConnection(connectString, userName, password);
               
               String subdomain=request.getParameter("subdomain");
               String query = "SELECT companyid FROM company where subdomain=?";
               PreparedStatement stmt = conn.prepareStatement(query);
               stmt.setString(1, subdomain);
               ResultSet rs = stmt.executeQuery();
               while (rs.next()) {
                   String companyId =rs.getString("companyid");
                   
                   //Invoice Part
                   String invquery = " select invoice.id,customer.billingaddress,customer.shippingaddress,customer.email from invoice "
                           +" inner join customer on customer.id=invoice.customer "
                           +" where invoice.deleteflag='F' and invoice.billingshippingaddresses is null and invoice.billto='' and customer.customeraddresses is null and invoice.company=?";                 
                   PreparedStatement invstmt = conn.prepareStatement(invquery);
                   invstmt.setString(1, companyId);    
                   ResultSet invrs = invstmt.executeQuery();
                   while (invrs.next()) {
                       String invid = invrs.getString("id");
                       String billingAddress = StringUtil.isNullOrEmpty(invrs.getString("billingaddress")) ? "" : invrs.getString("billingaddress");
                       String shippingAddress = StringUtil.isNullOrEmpty(invrs.getString("shippingaddress")) ? "" : invrs.getString("shippingaddress");
                       String email = StringUtil.isNullOrEmpty(invrs.getString("email")) ? "" : invrs.getString("email");
                       String baddresstype="Billing Address 1";
                       String saddresstype="Shipping Address 1";
                       
                       String uuid = UUID.randomUUID().toString().replace("-", "");
                       String insertquery = "INSERT INTO billingshippingaddresses(id,billingaddress,billingemail,billingaddresstype,shippingaddress,shippingemail,shippingaddresstype,company) VALUES(?,?,?,?,?,?,?,?) ";
                       PreparedStatement insertstmt = conn.prepareStatement(insertquery);
                       insertstmt.setString(1, uuid);
                       insertstmt.setString(2, billingAddress);
                       insertstmt.setString(3, email);
                       insertstmt.setString(4, baddresstype);
                       insertstmt.setString(5, shippingAddress);
                       insertstmt.setString(6, email);
                       insertstmt.setString(7, saddresstype);
                       insertstmt.setString(8, companyId);
                       insertstmt.execute();

                       String updatequery = "UPDATE invoice SET billingshippingaddresses=? where id=? and company=?";
                       PreparedStatement updatestmt = conn.prepareStatement(updatequery);
                       updatestmt.setString(1, uuid);
                       updatestmt.setString(2, invid);
                       updatestmt.setString(3, companyId);
                       updatestmt.execute();
                   }


                   //Vendor Invoice Part;
                   
                   String veninvquery = "select goodsreceipt.id,vendor.address,vendor.email from goodsreceipt "
                           + "inner join vendor on vendor.id=goodsreceipt.vendor "
                           + "where goodsreceipt.deleteflag='F' and goodsreceipt.billingshippingaddresses is null and goodsreceipt.billto='' and vendor.vendoraddresses is null and goodsreceipt.company=?";
                   PreparedStatement veninvstmt = conn.prepareStatement(veninvquery);
                   veninvstmt.setString(1, companyId);
                   ResultSet veninvrs = veninvstmt.executeQuery();
                   while (veninvrs.next()) {
                       String veninvid = veninvrs.getString("id");
                       String address = StringUtil.isNullOrEmpty(veninvrs.getString("address")) ? "" : veninvrs.getString("address");
                       String email = StringUtil.isNullOrEmpty(veninvrs.getString("email")) ? "" : veninvrs.getString("email"); 
                       String baddresstype="Billing Address 1";
                       String saddresstype="Shipping Address 1";
                       
                       String uuid = UUID.randomUUID().toString().replace("-", "");
                       String insertquery = "INSERT INTO billingshippingaddresses(id,billingaddress,billingemail,billingaddresstype,shippingaddress,shippingemail,shippingaddresstype,company) VALUES(?,?,?,?,?,?,?,?) ";
                       PreparedStatement insertstmt = conn.prepareStatement(insertquery);
                       insertstmt.setString(1, uuid);
                       insertstmt.setString(2, address);
                       insertstmt.setString(3, email);
                       insertstmt.setString(4, baddresstype);
                       insertstmt.setString(5, address);
                       insertstmt.setString(6, email);
                       insertstmt.setString(7, saddresstype);
                       insertstmt.setString(8, companyId);
                       insertstmt.execute();

                       String updatequery = "UPDATE goodsreceipt SET billingshippingaddresses=? where id=? and company=?";
                       PreparedStatement updatestmt = conn.prepareStatement(updatequery);
                       updatestmt.setString(1, uuid);
                       updatestmt.setString(2, veninvid);
                       updatestmt.setString(3, companyId);
                       updatestmt.execute();
                   }
                      
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
    
%>