<%-- 
    Document   : updateCustomerFields
    Created on : Nov 7, 2014, 11:27:11 AM
    Author     : krawler    
--%>

<%@page import="java.util.UUID"%>
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
                if (!StringUtil.isNullOrEmpty(companyid)) {
                    out.println("</br></br><b>Updated company Name: " + companyname + "</b><br><b> Subdomain: " + compSubDomain + "</b>");
                    //Customer Addresses merged
                    query = "select * from customer where company='" + companyid + "'";
                    PreparedStatement custstmt = conn.prepareStatement(query);
                    ResultSet custrs = custstmt.executeQuery();
                    int updatedNumberOfCustomers=0;
                    while (custrs.next()) {
                        String customerid = custrs.getString("id");
                        String customeAddrID = custrs.getString("customeraddresses");
                        query = "select * from customeraddressdetails where customerid='" + customerid + "'";
                        PreparedStatement custAddrDetailsStmt = conn.prepareStatement(query);
                        if (!custAddrDetailsStmt.executeQuery().next()) {//This chek used to avoid multiple time data migration in which script runs more than one by mistake
                            updatedNumberOfCustomers++;
                            if (!StringUtil.isNullOrEmpty(customeAddrID)) {//when addresses given in customeraddresses table
                                query = "select * from customeraddresses where id='" + customeAddrID + "'";
                                PreparedStatement custAddrstmt = conn.prepareStatement(query);
                                ResultSet custAddrRS = custAddrstmt.executeQuery();
                                while (custAddrRS.next()) {
                                    String shpAddr1 = custAddrRS.getString("shippingAddress1");
                                    String shpAddr2 = custAddrRS.getString("shippingAddress2");
                                    String shpAddr3 = custAddrRS.getString("shippingAddress3");
                                    String billAddr1 = custAddrRS.getString("billingAddress1");
                                    String billAddr2 = custAddrRS.getString("billingAddress2");
                                    String billAddr3 = custAddrRS.getString("billingAddress3");

                                    if (!StringUtil.isNullOrEmpty(shpAddr1)) {//if it is null it means there is no shipping address for customer in customeraddress table
                                        String shpCity1 = custAddrRS.getString("shippingCity1");
                                        String shpState1 = custAddrRS.getString("shippingState1");
                                        String shpCountry1 = custAddrRS.getString("shippingCountry1");
                                        String shppoCode1 = custAddrRS.getString("shippingPostal1");
                                        String shpPhone1 = custAddrRS.getString("shippingPhone1");
                                        String shpMobile1 = custAddrRS.getString("shippingMobile1");
                                        String shpFax1 = custAddrRS.getString("shippingFax1");
                                        String shpEmail1 = custAddrRS.getString("shippingEmail1");
                                        String shpCP1 = custAddrRS.getString("shippingContactPerson1");
                                        String shpCPN1 = custAddrRS.getString("shippingContactNumber1");
                                        String shippingRoute1 = custAddrRS.getString("shippingRoute1");
                                        String uuid = UUID.randomUUID().toString().replace("-", "");
                                        query = "insert into customeraddressdetails (id,aliasname,address,city,state,country,postalcode,phone,mobilenumber,fax,emailid,contactperson,contactpersonnumber,shippingroute,isbillingaddress,isdefaultaddress,customerid,company) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
                                        PreparedStatement insertstmt = conn.prepareStatement(query);
                                        insertstmt.setString(1, uuid);
                                        insertstmt.setString(2, "Shipping Address1");
                                        insertstmt.setString(3, shpAddr1);
                                        insertstmt.setString(4, shpCity1);
                                        insertstmt.setString(5, shpState1);
                                        insertstmt.setString(6, shpCountry1);
                                        insertstmt.setString(7, shppoCode1);
                                        insertstmt.setString(8, shpPhone1);
                                        insertstmt.setString(9, shpMobile1);
                                        insertstmt.setString(10, shpFax1);
                                        insertstmt.setString(11, shpEmail1);
                                        insertstmt.setString(12, shpCP1);
                                        insertstmt.setString(13, shpCPN1);
                                        insertstmt.setString(14, shippingRoute1);
                                        insertstmt.setString(15, "F");
                                        insertstmt.setString(16, "T");
                                        insertstmt.setString(17, customerid);
                                        insertstmt.setString(18, companyid);
                                        insertstmt.execute();

                                        if (!StringUtil.isNullOrEmpty(shpAddr2)) {
                                            String shpCity2 = custAddrRS.getString("shippingCity2");
                                            String shpState2 = custAddrRS.getString("shippingState2");
                                            String shpCountry2 = custAddrRS.getString("shippingCountry2");
                                            String shppoCode2 = custAddrRS.getString("shippingPostal2");
                                            String shpPhone2 = custAddrRS.getString("shippingPhone2");
                                            String shpMobile2 = custAddrRS.getString("shippingMobile2");
                                            String shpFax2 = custAddrRS.getString("shippingFax2");
                                            String shpEmail2 = custAddrRS.getString("shippingEmail2");
                                            String shpCP2 = custAddrRS.getString("shippingContactPerson2");
                                            String shpCPN2 = custAddrRS.getString("shippingContactNumber2");
                                            String shippingRoute2 = custAddrRS.getString("shippingRoute2");
                                            uuid = UUID.randomUUID().toString().replace("-", "");
                                            insertstmt.setString(1, uuid);
                                            insertstmt.setString(2, "Shipping Address2");
                                            insertstmt.setString(3, shpAddr2);
                                            insertstmt.setString(4, shpCity2);
                                            insertstmt.setString(5, shpState2);
                                            insertstmt.setString(6, shpCountry2);
                                            insertstmt.setString(7, shppoCode2);
                                            insertstmt.setString(8, shpPhone2);
                                            insertstmt.setString(9, shpMobile2);
                                            insertstmt.setString(10, shpFax2);
                                            insertstmt.setString(11, shpEmail2);
                                            insertstmt.setString(12, shpCP2);
                                            insertstmt.setString(13, shpCPN2);
                                            insertstmt.setString(14, shippingRoute2);
                                            insertstmt.setString(15, "F");
                                            insertstmt.setString(16, "F");
                                            insertstmt.setString(17, customerid);
                                            insertstmt.setString(18, companyid);
                                            insertstmt.execute();
                                        }

                                        if (!StringUtil.isNullOrEmpty(shpAddr3)) {
                                            String shpCity3 = custAddrRS.getString("shippingCity3");
                                            String shpState3 = custAddrRS.getString("shippingState3");
                                            String shpCountry3 = custAddrRS.getString("shippingCountry3");
                                            String shppoCode3 = custAddrRS.getString("shippingPostal3");
                                            String shpPhone3 = custAddrRS.getString("shippingPhone3");
                                            String shpMobile3 = custAddrRS.getString("shippingMobile3");
                                            String shpFax3 = custAddrRS.getString("shippingFax3");
                                            String shpEmail3 = custAddrRS.getString("shippingEmail3");
                                            String shpCP3 = custAddrRS.getString("shippingContactPerson3");
                                            String shpCPN3 = custAddrRS.getString("shippingContactNumber3");
                                            String shippingRoute3 = custAddrRS.getString("shippingRoute3");
                                            uuid = UUID.randomUUID().toString().replace("-", "");
                                            insertstmt.setString(1, uuid);
                                            insertstmt.setString(2, "Shipping Address3");
                                            insertstmt.setString(3, shpAddr3);
                                            insertstmt.setString(4, shpCity3);
                                            insertstmt.setString(5, shpState3);
                                            insertstmt.setString(6, shpCountry3);
                                            insertstmt.setString(7, shppoCode3);
                                            insertstmt.setString(8, shpPhone3);
                                            insertstmt.setString(9, shpMobile3);
                                            insertstmt.setString(10, shpFax3);
                                            insertstmt.setString(11, shpEmail3);
                                            insertstmt.setString(12, shpCP3);
                                            insertstmt.setString(13, shpCPN3);
                                            insertstmt.setString(14, shippingRoute3);
                                            insertstmt.setString(15, "F");
                                            insertstmt.setString(16, "F");
                                            insertstmt.setString(17, customerid);
                                            insertstmt.setString(18, companyid);
                                            insertstmt.execute();
                                        }

                                    }
                                    if (!StringUtil.isNullOrEmpty(billAddr1)) { //if it is null it means there is no shipping address for customer in customeraddress table
                                        String billCity1 = custAddrRS.getString("billingCity1");
                                        String billState1 = custAddrRS.getString("billingState1");
                                        String billCountry1 = custAddrRS.getString("billingCountry1");
                                        String billpoCode1 = custAddrRS.getString("billingPostal1");
                                        String billPhone1 = custAddrRS.getString("billingPhone1");
                                        String billMobile1 = custAddrRS.getString("billingMobile1");
                                        String billFax1 = custAddrRS.getString("billingFax1");
                                        String billEmail1 = custAddrRS.getString("billingEmail1");
                                        String billCP1 = custAddrRS.getString("billingContactPerson1");
                                        String billCPN1 = custAddrRS.getString("billingContactNumber1");
                                        String uuid = UUID.randomUUID().toString().replace("-", "");
                                        query = "insert into customeraddressdetails (id,aliasname,address,city,state,country,postalcode,phone,mobilenumber,fax,emailid,contactperson,contactpersonnumber,isbillingaddress,isdefaultaddress,customerid,company) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
                                        PreparedStatement insertstmt = conn.prepareStatement(query);
                                        insertstmt.setString(1, uuid);
                                        insertstmt.setString(2, "Billing Address1");
                                        insertstmt.setString(3, billAddr1);
                                        insertstmt.setString(4, billCity1);
                                        insertstmt.setString(5, billState1);
                                        insertstmt.setString(6, billCountry1);
                                        insertstmt.setString(7, billpoCode1);
                                        insertstmt.setString(8, billPhone1);
                                        insertstmt.setString(9, billMobile1);
                                        insertstmt.setString(10, billFax1);
                                        insertstmt.setString(11, billEmail1);
                                        insertstmt.setString(12, billCP1);
                                        insertstmt.setString(13, billCPN1);
                                        insertstmt.setString(14, "T");
                                        insertstmt.setString(15, "T");
                                        insertstmt.setString(16, customerid);
                                        insertstmt.setString(17, companyid);
                                        insertstmt.execute();

                                        if (!StringUtil.isNullOrEmpty(billAddr2)) {
                                            String billCity2 = custAddrRS.getString("billingCity2");
                                            String billState2 = custAddrRS.getString("billingState2");
                                            String billCountry2 = custAddrRS.getString("billingCountry2");
                                            String billpoCode2 = custAddrRS.getString("billingPostal2");
                                            String billPhone2 = custAddrRS.getString("billingPhone2");
                                            String billMobile2 = custAddrRS.getString("billingMobile2");
                                            String billFax2 = custAddrRS.getString("billingFax2");
                                            String billEmail2 = custAddrRS.getString("billingEmail2");
                                            String billCP2 = custAddrRS.getString("billingContactPerson2");
                                            String billCPN2 = custAddrRS.getString("billingContactNumber2");
                                            uuid = UUID.randomUUID().toString().replace("-", "");
                                            insertstmt.setString(1, uuid);
                                            insertstmt.setString(2, "Billing Address2");
                                            insertstmt.setString(3, billAddr2);
                                            insertstmt.setString(4, billCity2);
                                            insertstmt.setString(5, billState2);
                                            insertstmt.setString(6, billCountry2);
                                            insertstmt.setString(7, billpoCode2);
                                            insertstmt.setString(8, billPhone2);
                                            insertstmt.setString(9, billMobile2);
                                            insertstmt.setString(10, billFax2);
                                            insertstmt.setString(11, billEmail2);
                                            insertstmt.setString(12, billCP2);
                                            insertstmt.setString(13, billCPN2);
                                            insertstmt.setString(14, "T");
                                            insertstmt.setString(15, "F");
                                            insertstmt.setString(16, customerid);
                                            insertstmt.setString(17, companyid);
                                            insertstmt.execute();
                                        }

                                        if (!StringUtil.isNullOrEmpty(billAddr3)) {
                                            String billCity3 = custAddrRS.getString("billingCity3");
                                            String billState3 = custAddrRS.getString("billingState3");
                                            String billCountry3 = custAddrRS.getString("billingCountry3");
                                            String billpoCode3 = custAddrRS.getString("billingPostal3");
                                            String billPhone3 = custAddrRS.getString("billingPhone3");
                                            String billMobile3 = custAddrRS.getString("billingMobile3");
                                            String billFax3 = custAddrRS.getString("billingFax3");
                                            String billEmail3 = custAddrRS.getString("billingEmail3");
                                            String billCP3 = custAddrRS.getString("billingContactPerson3");
                                            String billCPN3 = custAddrRS.getString("billingContactNumber2");
                                            uuid = UUID.randomUUID().toString().replace("-", "");
                                            insertstmt.setString(1, uuid);
                                            insertstmt.setString(2, "Billing Address3");
                                            insertstmt.setString(3, billAddr3);
                                            insertstmt.setString(4, billCity3);
                                            insertstmt.setString(5, billState3);
                                            insertstmt.setString(6, billCountry3);
                                            insertstmt.setString(7, billpoCode3);
                                            insertstmt.setString(8, billPhone3);
                                            insertstmt.setString(9, billMobile3);
                                            insertstmt.setString(10, billFax3);
                                            insertstmt.setString(11, billEmail3);
                                            insertstmt.setString(12, billCP3);
                                            insertstmt.setString(13, billCPN3);
                                            insertstmt.setString(14, "T");
                                            insertstmt.setString(15, "F");
                                            insertstmt.setString(16, customerid);
                                            insertstmt.setString(17, companyid);
                                            insertstmt.execute();
                                        }
                                    }
                                }
                            } else {//when addresses not saved in customeraddresses table. 
                                String custBA1 = custrs.getString("billingaddress");
                                String custBA2 = custrs.getString("billingaddress2");
                                String custBA3 = custrs.getString("billingaddress3");
                                String custSA1 = custrs.getString("shippingaddress");
                                String custSA2 = custrs.getString("shippingaddress2");
                                String custSA3 = custrs.getString("shippingaddress3");
                                String custEmail = custrs.getString("email");
                                String custPhoneNo = custrs.getString("contactno");
                                String custFax = custrs.getString("fax");
                                if (!StringUtil.isNullOrEmpty(custBA1)) {//If it is null that means billing address is not given for Customer
                                    String uuid = UUID.randomUUID().toString().replace("-", "");
                                    query = "insert into customeraddressdetails (id,aliasname,address,phone,fax,emailid,isbillingaddress,isdefaultaddress,customerid,company) values (?,?,?,?,?,?,?,?,?,?) ";
                                    PreparedStatement insertstmt = conn.prepareStatement(query);
                                    insertstmt.setString(1, uuid);
                                    insertstmt.setString(2, "Billing Address1");
                                    insertstmt.setString(3, custBA1);
                                    insertstmt.setString(4, custPhoneNo);
                                    insertstmt.setString(5, custFax);
                                    insertstmt.setString(6, custEmail);
                                    insertstmt.setString(7, "T");
                                    insertstmt.setString(8, "T");
                                    insertstmt.setString(9, customerid);
                                    insertstmt.setString(10, companyid);
                                    insertstmt.execute();
                                    if (!StringUtil.isNullOrEmpty(custBA2)) {
                                        uuid = UUID.randomUUID().toString().replace("-", "");
                                        insertstmt.setString(1, uuid);
                                        insertstmt.setString(2, "Billing Address2");
                                        insertstmt.setString(3, custBA2);
                                        insertstmt.setString(4, custPhoneNo);
                                        insertstmt.setString(5, custFax);
                                        insertstmt.setString(6, custEmail);
                                        insertstmt.setString(7, "T");
                                        insertstmt.setString(8, "F");
                                        insertstmt.setString(9, customerid);
                                        insertstmt.setString(10, companyid);
                                        insertstmt.execute();
                                    }
                                    if (!StringUtil.isNullOrEmpty(custBA3)) {
                                        uuid = UUID.randomUUID().toString().replace("-", "");
                                        insertstmt.setString(1, uuid);
                                        insertstmt.setString(2, "Billing Address3");
                                        insertstmt.setString(3, custBA3);
                                        insertstmt.setString(4, custPhoneNo);
                                        insertstmt.setString(5, custFax);
                                        insertstmt.setString(6, custEmail);
                                        insertstmt.setString(7, "T");
                                        insertstmt.setString(8, "F");
                                        insertstmt.setString(9, customerid);
                                        insertstmt.setString(10, companyid);
                                        insertstmt.execute();
                                    }
                                }
                                if (!StringUtil.isNullOrEmpty(custSA1)) {//If it is null that means shipping address is not given for Customer
                                    String uuid = UUID.randomUUID().toString().replace("-", "");
                                    query = "insert into customeraddressdetails (id,aliasname,address,phone,fax,emailid,isbillingaddress,isdefaultaddress,customerid,company) values (?,?,?,?,?,?,?,?,?,?) ";
                                    PreparedStatement insertstmt = conn.prepareStatement(query);
                                    insertstmt.setString(1, uuid);
                                    insertstmt.setString(2, "Shipping Address1");
                                    insertstmt.setString(3, custSA1);
                                    insertstmt.setString(4, custPhoneNo);
                                    insertstmt.setString(5, custFax);
                                    insertstmt.setString(6, custEmail);
                                    insertstmt.setString(7, "F");
                                    insertstmt.setString(8, "T");
                                    insertstmt.setString(9, customerid);
                                    insertstmt.setString(10, companyid);
                                    insertstmt.execute();
                                    if (!StringUtil.isNullOrEmpty(custSA2)) {
                                        uuid = UUID.randomUUID().toString().replace("-", "");
                                        insertstmt.setString(1, uuid);
                                        insertstmt.setString(2, "Shipping Address2");
                                        insertstmt.setString(3, custSA2);
                                        insertstmt.setString(4, custPhoneNo);
                                        insertstmt.setString(5, custFax);
                                        insertstmt.setString(6, custEmail);
                                        insertstmt.setString(7, "F");
                                        insertstmt.setString(8, "F");
                                        insertstmt.setString(9, customerid);
                                        insertstmt.setString(10, companyid);
                                        insertstmt.execute();
                                    }
                                    if (!StringUtil.isNullOrEmpty(custSA3)) {
                                        uuid = UUID.randomUUID().toString().replace("-", "");
                                        insertstmt.setString(1, uuid);
                                        insertstmt.setString(2, "Shipping Address3");
                                        insertstmt.setString(3, custSA3);
                                        insertstmt.setString(4, custPhoneNo);
                                        insertstmt.setString(5, custFax);
                                        insertstmt.setString(6, custEmail);
                                        insertstmt.setString(7, "F");
                                        insertstmt.setString(8, "F");
                                        insertstmt.setString(9, customerid);
                                        insertstmt.setString(10, companyid);
                                        insertstmt.execute();
                                    }
                                }
                            }
                        }
                    }//customer loop ended here
                    out.println("</br>Addresses Succesfully Miggrated From Old table To New For "+updatedNumberOfCustomers+" Customers");


                    //Vendor Addresses merged Part
                    query = "select * from vendor where company='" + companyid + "'";
                    PreparedStatement vendstmt = conn.prepareStatement(query);
                    ResultSet vendrs = vendstmt.executeQuery();
                    int updatedNumberOfVendors=0;
                    while (vendrs.next()) {
                        String vendorid = vendrs.getString("id");
                        String vendorAddrID = vendrs.getString("vendoraddresses");

                        query = "select * from vendoraddressdetails where vendorid='" + vendorid + "'";
                        PreparedStatement vendAddrDetailsStmt = conn.prepareStatement(query);
                        if (!vendAddrDetailsStmt.executeQuery().next()) { //This chek used to avoid multiple time data migration in which script runs more than one by mistake
                            updatedNumberOfVendors++;
                            if (!StringUtil.isNullOrEmpty(vendorAddrID)) {//when addresses given in vendoraddresses table
                                query = "select * from vendoraddresses where id='" + vendorAddrID + "'";
                                PreparedStatement vendAddrstmt = conn.prepareStatement(query);
                                ResultSet vendAddrRS = vendAddrstmt.executeQuery();
                                while (vendAddrRS.next()) {
                                    String shpAddr1 = vendAddrRS.getString("shippingAddress1");
                                    String shpAddr2 = vendAddrRS.getString("shippingAddress2");
                                    String shpAddr3 = vendAddrRS.getString("shippingAddress3");
                                    String billAddr1 = vendAddrRS.getString("billingAddress1");
                                    String billAddr2 = vendAddrRS.getString("billingAddress2");
                                    String billAddr3 = vendAddrRS.getString("billingAddress3");

                                    if (!StringUtil.isNullOrEmpty(shpAddr1)) {//if it is null it means there is no shipping address for vendor in vendoraddresses table
                                        String shpCity1 = vendAddrRS.getString("shippingCity1");
                                        String shpState1 = vendAddrRS.getString("shippingState1");
                                        String shpCountry1 = vendAddrRS.getString("shippingCountry1");
                                        String shppoCode1 = vendAddrRS.getString("shippingPostal1");
                                        String shpPhone1 = vendAddrRS.getString("shippingPhone1");
                                        String shpMobile1 = vendAddrRS.getString("shippingMobile1");
                                        String shpFax1 = vendAddrRS.getString("shippingFax1");
                                        String shpEmail1 = vendAddrRS.getString("shippingEmail1");
                                        String shpCP1 = vendAddrRS.getString("shippingContactPerson1");
                                        String shpCPN1 = vendAddrRS.getString("shippingContactNumber1");
                                        query = "insert into vendoraddressdetails (id,aliasname,address,city,state,country,postalcode,phone,mobilenumber,fax,emailid,contactperson,contactpersonnumber,isbillingaddress,isdefaultaddress,vendorid,company) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
                                        PreparedStatement insertstmt = conn.prepareStatement(query);
                                        String uuid = UUID.randomUUID().toString().replace("-", "");
                                        insertstmt.setString(1, uuid);
                                        insertstmt.setString(2, "Shipping Address1");
                                        insertstmt.setString(3, shpAddr1);
                                        insertstmt.setString(4, shpCity1);
                                        insertstmt.setString(5, shpState1);
                                        insertstmt.setString(6, shpCountry1);
                                        insertstmt.setString(7, shppoCode1);
                                        insertstmt.setString(8, shpPhone1);
                                        insertstmt.setString(9, shpMobile1);
                                        insertstmt.setString(10, shpFax1);
                                        insertstmt.setString(11, shpEmail1);
                                        insertstmt.setString(12, shpCP1);
                                        insertstmt.setString(13, shpCPN1);
                                        insertstmt.setString(14, "F");
                                        insertstmt.setString(15, "T");
                                        insertstmt.setString(16, vendorid);
                                        insertstmt.setString(17, companyid);
                                        insertstmt.execute();

                                        if (!StringUtil.isNullOrEmpty(shpAddr2)) {
                                            String shpCity2 = vendAddrRS.getString("shippingCity2");
                                            String shpState2 = vendAddrRS.getString("shippingState2");
                                            String shpCountry2 = vendAddrRS.getString("shippingCountry2");
                                            String shppoCode2 = vendAddrRS.getString("shippingPostal2");
                                            String shpPhone2 = vendAddrRS.getString("shippingPhone2");
                                            String shpMobile2 = vendAddrRS.getString("shippingMobile2");
                                            String shpFax2 = vendAddrRS.getString("shippingFax2");
                                            String shpEmail2 = vendAddrRS.getString("shippingEmail2");
                                            String shpCP2 = vendAddrRS.getString("shippingContactPerson2");
                                            String shpCPN2 = vendAddrRS.getString("shippingContactNumber2");
                                            uuid = UUID.randomUUID().toString().replace("-", "");
                                            insertstmt.setString(1, uuid);
                                            insertstmt.setString(2, "Shipping Address2");
                                            insertstmt.setString(3, shpAddr2);
                                            insertstmt.setString(4, shpCity2);
                                            insertstmt.setString(5, shpState2);
                                            insertstmt.setString(6, shpCountry2);
                                            insertstmt.setString(7, shppoCode2);
                                            insertstmt.setString(8, shpPhone2);
                                            insertstmt.setString(9, shpMobile2);
                                            insertstmt.setString(10, shpFax2);
                                            insertstmt.setString(11, shpEmail2);
                                            insertstmt.setString(12, shpCP2);
                                            insertstmt.setString(13, shpCPN2);
                                            insertstmt.setString(14, "F");
                                            insertstmt.setString(15, "F");
                                            insertstmt.setString(16, vendorid);
                                            insertstmt.setString(17, companyid);
                                            insertstmt.execute();
                                        }

                                        if (!StringUtil.isNullOrEmpty(shpAddr3)) {
                                            String shpCity3 = vendAddrRS.getString("shippingCity3");
                                            String shpState3 = vendAddrRS.getString("shippingState3");
                                            String shpCountry3 = vendAddrRS.getString("shippingCountry3");
                                            String shppoCode3 = vendAddrRS.getString("shippingPostal3");
                                            String shpPhone3 = vendAddrRS.getString("shippingPhone3");
                                            String shpMobile3 = vendAddrRS.getString("shippingMobile3");
                                            String shpFax3 = vendAddrRS.getString("shippingFax3");
                                            String shpEmail3 = vendAddrRS.getString("shippingEmail3");
                                            String shpCP3 = vendAddrRS.getString("shippingContactPerson3");
                                            String shpCPN3 = vendAddrRS.getString("shippingContactNumber3");
                                            uuid = UUID.randomUUID().toString().replace("-", "");
                                            insertstmt.setString(1, uuid);
                                            insertstmt.setString(2, "Shipping Address3");
                                            insertstmt.setString(3, shpAddr3);
                                            insertstmt.setString(4, shpCity3);
                                            insertstmt.setString(5, shpState3);
                                            insertstmt.setString(6, shpCountry3);
                                            insertstmt.setString(7, shppoCode3);
                                            insertstmt.setString(8, shpPhone3);
                                            insertstmt.setString(9, shpMobile3);
                                            insertstmt.setString(10, shpFax3);
                                            insertstmt.setString(11, shpEmail3);
                                            insertstmt.setString(12, shpCP3);
                                            insertstmt.setString(13, shpCPN3);
                                            insertstmt.setString(14, "F");
                                            insertstmt.setString(15, "F");
                                            insertstmt.setString(16, vendorid);
                                            insertstmt.setString(17, companyid);
                                            insertstmt.execute();
                                        }

                                    }
                                    if (!StringUtil.isNullOrEmpty(billAddr1)) { //if it is null it means there is no billing address for vendor in vendoraddresses table
                                        String billCity1 = vendAddrRS.getString("billingCity1");
                                        String billState1 = vendAddrRS.getString("billingState1");
                                        String billCountry1 = vendAddrRS.getString("billingCountry1");
                                        String billpoCode1 = vendAddrRS.getString("billingPostal1");
                                        String billPhone1 = vendAddrRS.getString("billingPhone1");
                                        String billMobile1 = vendAddrRS.getString("billingMobile1");
                                        String billFax1 = vendAddrRS.getString("billingFax1");
                                        String billEmail1 = vendAddrRS.getString("billingEmail1");
                                        String billCP1 = vendAddrRS.getString("billingContactPerson1");
                                        String billCPN1 = vendAddrRS.getString("billingContactNumber1");
                                        query = "insert into vendoraddressdetails (id,aliasname,address,city,state,country,postalcode,phone,mobilenumber,fax,emailid,contactperson,contactpersonnumber,isbillingaddress,isdefaultaddress,vendorid,company) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
                                        PreparedStatement insertstmt = conn.prepareStatement(query);
                                        String uuid = UUID.randomUUID().toString().replace("-", "");
                                        insertstmt.setString(1, uuid);
                                        insertstmt.setString(2, "Billing Address1");
                                        insertstmt.setString(3, billAddr1);
                                        insertstmt.setString(4, billCity1);
                                        insertstmt.setString(5, billState1);
                                        insertstmt.setString(6, billCountry1);
                                        insertstmt.setString(7, billpoCode1);
                                        insertstmt.setString(8, billPhone1);
                                        insertstmt.setString(9, billMobile1);
                                        insertstmt.setString(10, billFax1);
                                        insertstmt.setString(11, billEmail1);
                                        insertstmt.setString(12, billCP1);
                                        insertstmt.setString(13, billCPN1);
                                        insertstmt.setString(14, "T");
                                        insertstmt.setString(15, "T");
                                        insertstmt.setString(16, vendorid);
                                        insertstmt.setString(17, companyid);
                                        insertstmt.execute();

                                        if (!StringUtil.isNullOrEmpty(billAddr2)) {
                                            String billCity2 = vendAddrRS.getString("billingCity2");
                                            String billState2 = vendAddrRS.getString("billingState2");
                                            String billCountry2 = vendAddrRS.getString("billingCountry2");
                                            String billpoCode2 = vendAddrRS.getString("billingPostal2");
                                            String billPhone2 = vendAddrRS.getString("billingPhone2");
                                            String billMobile2 = vendAddrRS.getString("billingMobile2");
                                            String billFax2 = vendAddrRS.getString("billingFax2");
                                            String billEmail2 = vendAddrRS.getString("billingEmail2");
                                            String billCP2 = vendAddrRS.getString("billingContactPerson2");
                                            String billCPN2 = vendAddrRS.getString("billingContactNumber2");
                                            uuid = UUID.randomUUID().toString().replace("-", "");
                                            insertstmt.setString(1, uuid);
                                            insertstmt.setString(2, "Billing Address2");
                                            insertstmt.setString(3, billAddr2);
                                            insertstmt.setString(4, billCity2);
                                            insertstmt.setString(5, billState2);
                                            insertstmt.setString(6, billCountry2);
                                            insertstmt.setString(7, billpoCode2);
                                            insertstmt.setString(8, billPhone2);
                                            insertstmt.setString(9, billMobile2);
                                            insertstmt.setString(10, billFax2);
                                            insertstmt.setString(11, billEmail2);
                                            insertstmt.setString(12, billCP2);
                                            insertstmt.setString(13, billCPN2);
                                            insertstmt.setString(14, "T");
                                            insertstmt.setString(15, "F");
                                            insertstmt.setString(16, vendorid);
                                            insertstmt.setString(17, companyid);
                                            insertstmt.execute();
                                        }

                                        if (!StringUtil.isNullOrEmpty(billAddr3)) {
                                            String billCity3 = vendAddrRS.getString("billingCity3");
                                            String billState3 = vendAddrRS.getString("billingState3");
                                            String billCountry3 = vendAddrRS.getString("billingCountry3");
                                            String billpoCode3 = vendAddrRS.getString("billingPostal3");
                                            String billPhone3 = vendAddrRS.getString("billingPhone3");
                                            String billMobile3 = vendAddrRS.getString("billingMobile3");
                                            String billFax3 = vendAddrRS.getString("billingFax3");
                                            String billEmail3 = vendAddrRS.getString("billingEmail3");
                                            String billCP3 = vendAddrRS.getString("billingContactPerson3");
                                            String billCPN3 = vendAddrRS.getString("billingContactNumber2");
                                            uuid = UUID.randomUUID().toString().replace("-", "");
                                            insertstmt.setString(1, uuid);
                                            insertstmt.setString(2, "Billing Address3");
                                            insertstmt.setString(3, billAddr3);
                                            insertstmt.setString(4, billCity3);
                                            insertstmt.setString(5, billState3);
                                            insertstmt.setString(6, billCountry3);
                                            insertstmt.setString(7, billpoCode3);
                                            insertstmt.setString(8, billPhone3);
                                            insertstmt.setString(9, billMobile3);
                                            insertstmt.setString(10, billFax3);
                                            insertstmt.setString(11, billEmail3);
                                            insertstmt.setString(12, billCP3);
                                            insertstmt.setString(13, billCPN3);
                                            insertstmt.setString(14, "T");
                                            insertstmt.setString(15, "F");
                                            insertstmt.setString(16, vendorid);
                                            insertstmt.setString(17, companyid);
                                            insertstmt.execute();
                                        }
                                    }
                                }
                            } else {//when addresses not saved in vendoraddresses table. 
                                String vendAddr1 = vendrs.getString("address");
                                String vendAddr2 = vendrs.getString("address2");
                                String vendAddr3 = vendrs.getString("address3");
                                String vendEmail = vendrs.getString("email");
                                String vendPhoneNo = vendrs.getString("contactno");
                                String vendFax = vendrs.getString("fax");
                                if (!StringUtil.isNullOrEmpty(vendAddr1)) {//If it is null that means address is not given for Customer  in customer table                              
                                    query = "insert into vendoraddressdetails (id,aliasname,address,phone,fax,emailid,isbillingaddress,isdefaultaddress,vendorid,company) values (?,?,?,?,?,?,?,?,?,?) ";
                                    PreparedStatement insertstmt = conn.prepareStatement(query);
                                    String uuid = UUID.randomUUID().toString().replace("-", "");
                                    insertstmt.setString(1, uuid);
                                    insertstmt.setString(2, "Billing Address1");
                                    insertstmt.setString(3, vendAddr1);
                                    insertstmt.setString(4, vendPhoneNo);
                                    insertstmt.setString(5, vendFax);
                                    insertstmt.setString(6, vendEmail);
                                    insertstmt.setString(7, "T");
                                    insertstmt.setString(8, "T");
                                    insertstmt.setString(9, vendorid);
                                    insertstmt.setString(10, companyid);
                                    insertstmt.execute();

                                    uuid = UUID.randomUUID().toString().replace("-", "");
                                    insertstmt.setString(1, uuid);
                                    insertstmt.setString(2, "Shipping Address1");
                                    insertstmt.setString(3, vendAddr1);
                                    insertstmt.setString(4, vendPhoneNo);
                                    insertstmt.setString(5, vendFax);
                                    insertstmt.setString(6, vendEmail);
                                    insertstmt.setString(7, "F");
                                    insertstmt.setString(8, "T");
                                    insertstmt.setString(9, vendorid);
                                    insertstmt.setString(10, companyid);
                                    insertstmt.execute();


                                    if (!StringUtil.isNullOrEmpty(vendAddr2)) {
                                        uuid = UUID.randomUUID().toString().replace("-", "");
                                        insertstmt.setString(1, uuid);
                                        insertstmt.setString(2, "Billing Address2");
                                        insertstmt.setString(3, vendAddr2);
                                        insertstmt.setString(4, vendPhoneNo);
                                        insertstmt.setString(5, vendFax);
                                        insertstmt.setString(6, vendEmail);
                                        insertstmt.setString(7, "T");
                                        insertstmt.setString(8, "F");
                                        insertstmt.setString(9, vendorid);
                                        insertstmt.setString(10, companyid);
                                        insertstmt.execute();

                                        uuid = UUID.randomUUID().toString().replace("-", "");
                                        insertstmt.setString(1, uuid);
                                        insertstmt.setString(2, "Shipping Address2");
                                        insertstmt.setString(3, vendAddr2);
                                        insertstmt.setString(4, vendPhoneNo);
                                        insertstmt.setString(5, vendFax);
                                        insertstmt.setString(6, vendEmail);
                                        insertstmt.setString(7, "F");
                                        insertstmt.setString(8, "F");
                                        insertstmt.setString(9, vendorid);
                                        insertstmt.setString(10, companyid);
                                        insertstmt.execute();
                                    }
                                    if (!StringUtil.isNullOrEmpty(vendAddr3)) {
                                        uuid = UUID.randomUUID().toString().replace("-", "");
                                        insertstmt.setString(1, uuid);
                                        insertstmt.setString(2, "Billing Address3");
                                        insertstmt.setString(3, vendAddr3);
                                        insertstmt.setString(4, vendPhoneNo);
                                        insertstmt.setString(5, vendFax);
                                        insertstmt.setString(6, vendEmail);
                                        insertstmt.setString(7, "T");
                                        insertstmt.setString(8, "F");
                                        insertstmt.setString(9, vendorid);
                                        insertstmt.setString(10, companyid);
                                        insertstmt.execute();

                                        uuid = UUID.randomUUID().toString().replace("-", "");
                                        insertstmt.setString(1, uuid);
                                        insertstmt.setString(2, "Shipping Address3");
                                        insertstmt.setString(3, vendAddr3);
                                        insertstmt.setString(4, vendPhoneNo);
                                        insertstmt.setString(5, vendFax);
                                        insertstmt.setString(6, vendEmail);
                                        insertstmt.setString(7, "F");
                                        insertstmt.setString(8, "F");
                                        insertstmt.setString(9, vendorid);
                                        insertstmt.setString(10, companyid);
                                        insertstmt.execute();
                                    }
                                }
                            }
                        }
                    }//vendor while loop ended here
                    out.println("</br>Addresses Succesfully Miggrated From Old table To New Table For "+updatedNumberOfVendors+" Vendors");
                }
            }
        }
       
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
      if (conn != null) {
        conn.close();//finally release connection   
      }
    }

%>
