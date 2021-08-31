<%-- 
    Document   : CreditNoteAddressUpdateScript
    Created on : 2 Apr, 2016, 9:43:33 AM
    Author     : krawler
--%>

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
                List creditNumberList = new ArrayList();
                List debitNumberList = new ArrayList();
                String crNotequery = "select id,cnnumber,customer,vendor from creditnote where deleteflag='F' and company=? and (billingshippingaddresses is null OR billingshippingaddresses='')";
                PreparedStatement crStmt = conn.prepareStatement(crNotequery);
                crStmt.setString(1, companyId);
                ResultSet creditNoteRs = crStmt.executeQuery();
                int crCount = 0;
                while (creditNoteRs.next()) {
                    crCount++;
                    String ceditNoteId = StringUtil.isNullOrEmpty(creditNoteRs.getString("id")) ? "" : creditNoteRs.getString("id");
                    String creditNumber = StringUtil.isNullOrEmpty(creditNoteRs.getString("cnnumber")) ? "" : creditNoteRs.getString("cnnumber");
                    String customerId = StringUtil.isNullOrEmpty(creditNoteRs.getString("customer")) ? "" : creditNoteRs.getString("customer");
                    String vendorId = StringUtil.isNullOrEmpty(creditNoteRs.getString("vendor")) ? "" : creditNoteRs.getString("vendor");
                    String shpAliasName = "", shpAddr = "", shpCity = "", shpState = "", shpCountry = "", shppoCode = "", shpPhone = "", shpMobile = "", shpFax = "", shpEmail = "", shpCP = "", shpCPN = "", shpRN = "", shippingRoute = "";
                    String billAliasName = "", billAddr = "", billCity = "", billState = "", billCountry = "", billpoCode = "", billPhone = "", billMobile = "", billFax = "", billEmail = "", billCP = "", billCPN = "", billRN = "";
                    String selectquery = "";
                    PreparedStatement addressDetailsStmt = null;
                    if (!StringUtil.isNullOrEmpty(customerId)) {
                        selectquery = "select * from customeraddressdetails where customerid=? and isdefaultaddress='T'";
                        addressDetailsStmt = conn.prepareStatement(selectquery);
                        addressDetailsStmt.setString(1, customerId);
                    } else if (!StringUtil.isNullOrEmpty(vendorId)) {
                        selectquery = "select * from vendoraddressdetails where vendorid=? and isdefaultaddress='T'";
                        addressDetailsStmt = conn.prepareStatement(selectquery);
                        addressDetailsStmt.setString(1, vendorId);
                    }

                        ResultSet addressDetailsResult = addressDetailsStmt.executeQuery();
                        while (addressDetailsResult.next()) {
                            boolean isBilling = addressDetailsResult.getBoolean("isbillingaddress");
                            if (isBilling) {
                                billAliasName = StringUtil.isNullOrEmpty(addressDetailsResult.getString("aliasname")) ? "" : addressDetailsResult.getString("aliasname");
                                billAddr = StringUtil.isNullOrEmpty(addressDetailsResult.getString("address")) ? "" : addressDetailsResult.getString("address");
                                billCity = StringUtil.isNullOrEmpty(addressDetailsResult.getString("city")) ? "" : addressDetailsResult.getString("city");
                                billState = StringUtil.isNullOrEmpty(addressDetailsResult.getString("state")) ? "" : addressDetailsResult.getString("state");
                                billCountry = StringUtil.isNullOrEmpty(addressDetailsResult.getString("country")) ? "" : addressDetailsResult.getString("country");
                                billpoCode = StringUtil.isNullOrEmpty(addressDetailsResult.getString("postalcode")) ? "" : addressDetailsResult.getString("postalcode");
                                billPhone = StringUtil.isNullOrEmpty(addressDetailsResult.getString("phone")) ? "" : addressDetailsResult.getString("phone");
                                billMobile = StringUtil.isNullOrEmpty(addressDetailsResult.getString("mobilenumber")) ? "" : addressDetailsResult.getString("mobilenumber");
                                billFax = StringUtil.isNullOrEmpty(addressDetailsResult.getString("fax")) ? "" : addressDetailsResult.getString("fax");
                                billEmail = StringUtil.isNullOrEmpty(addressDetailsResult.getString("emailid")) ? "" : addressDetailsResult.getString("emailid");
                                billRN = StringUtil.isNullOrEmpty(addressDetailsResult.getString("recipientname")) ? "" : addressDetailsResult.getString("recipientname");
                                billCP = StringUtil.isNullOrEmpty(addressDetailsResult.getString("contactperson")) ? "" : addressDetailsResult.getString("contactperson");
                                billCPN = StringUtil.isNullOrEmpty(addressDetailsResult.getString("contactpersonnumber")) ? "" : addressDetailsResult.getString("contactpersonnumber");
                            } else {
                                shpAliasName = StringUtil.isNullOrEmpty(addressDetailsResult.getString("aliasname")) ? "" : addressDetailsResult.getString("aliasname");
                                shpAddr = StringUtil.isNullOrEmpty(addressDetailsResult.getString("address")) ? "" : addressDetailsResult.getString("address");
                                shpCity = StringUtil.isNullOrEmpty(addressDetailsResult.getString("city")) ? "" : addressDetailsResult.getString("city");
                                shpState = StringUtil.isNullOrEmpty(addressDetailsResult.getString("state")) ? "" : addressDetailsResult.getString("state");
                                shpCountry = StringUtil.isNullOrEmpty(addressDetailsResult.getString("country")) ? "" : addressDetailsResult.getString("country");
                                shppoCode = StringUtil.isNullOrEmpty(addressDetailsResult.getString("postalcode")) ? "" : addressDetailsResult.getString("postalcode");
                                shpPhone = StringUtil.isNullOrEmpty(addressDetailsResult.getString("phone")) ? "" : addressDetailsResult.getString("phone");
                                shpMobile = StringUtil.isNullOrEmpty(addressDetailsResult.getString("mobilenumber")) ? "" : addressDetailsResult.getString("mobilenumber");
                                shpFax = StringUtil.isNullOrEmpty(addressDetailsResult.getString("fax")) ? "" : addressDetailsResult.getString("fax");
                                shpEmail = StringUtil.isNullOrEmpty(addressDetailsResult.getString("emailid")) ? "" : addressDetailsResult.getString("emailid");
                                shpRN = StringUtil.isNullOrEmpty(addressDetailsResult.getString("recipientname")) ? "" : addressDetailsResult.getString("recipientname");
                                shpCP = StringUtil.isNullOrEmpty(addressDetailsResult.getString("contactperson")) ? "" : addressDetailsResult.getString("contactperson");
                                shpCPN = StringUtil.isNullOrEmpty(addressDetailsResult.getString("contactpersonnumber")) ? "" : addressDetailsResult.getString("contactpersonnumber");
                            }
                        }
                        String uuid = UUID.randomUUID().toString().replace("-", "");
                        String insertquery = "INSERT INTO billingshippingaddresses(id,billingaddress,billingcountry,billingstate,billingcity,"
                                + "billingpostal,billingemail,billingfax,billingmobile,billingphone,billingrecipientname,billingcontactperson,"
                                + "billingcontactpersonnumber,billingaddresstype,"
                                + "shippingaddress,shippingCountry,shippingstate,shippingcity,shippingpostal,shippingemail,shippingfax,"
                                + "shippingmobile,shippingphone,shippingcontactpersonnumber,shippingcontactperson,shippingrecipientname,"
                                + "shippingaddresstype,company) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        PreparedStatement insertstmt = conn.prepareStatement(insertquery);
                        insertstmt.setString(1, uuid);
                        insertstmt.setString(2, billAddr);
                        insertstmt.setString(3, billCountry);
                        insertstmt.setString(4, billState);
                        insertstmt.setString(5, billCity);
                        insertstmt.setString(6, billpoCode);
                        insertstmt.setString(7, billEmail);
                        insertstmt.setString(8, billFax);
                        insertstmt.setString(9, billMobile);
                        insertstmt.setString(10, billPhone);
                        insertstmt.setString(11, billRN);
                        insertstmt.setString(12, billCP);
                        insertstmt.setString(13, billCPN);
                        insertstmt.setString(14, billAliasName);
                        insertstmt.setString(15, shpAddr);
                        insertstmt.setString(16, shpCountry);
                        insertstmt.setString(17, shpState);
                        insertstmt.setString(18, shpCity);
                        insertstmt.setString(19, shppoCode);
                        insertstmt.setString(20, shpEmail);
                        insertstmt.setString(21, shpFax);
                        insertstmt.setString(22, shpMobile);
                        insertstmt.setString(23, shpPhone);
                        insertstmt.setString(24, shpCPN);
                        insertstmt.setString(25, shpCP);
                        insertstmt.setString(26, shpRN);
                        insertstmt.setString(27, shpAliasName);
                        insertstmt.setString(28, companyId);
                        insertstmt.execute();

                        String updatequery = "UPDATE creditnote SET billingshippingaddresses=? where id=? and company=?";
                        PreparedStatement updatestmt = conn.prepareStatement(updatequery);
                        updatestmt.setString(1, uuid);
                        updatestmt.setString(2, ceditNoteId);
                        updatestmt.setString(3, companyId);
                        updatestmt.execute();
                        creditNumberList.add(creditNumber);
                    }
                    out.println("Company Name : <b>" + companyname + "</b>");
                    if (creditNumberList.isEmpty()) {
                        out.println("<br/>Number of Credit Note's=" + crCount + ". All Credit Note are already updated or cannot update (In case customer does not have address).");
                    } else {
                        out.println("<br/>Number of CreditNote's=" + crCount + " and Number of updated Records are : " + creditNumberList.size());
                        out.println("<br/>Updated Records are : " + creditNumberList.toString());
                    }


                //debit Note
                String drNotequery = "select id,dnnumber,customer,vendor from debitnote where deleteflag='F' and company=? and (billingshippingaddresses is null OR billingshippingaddresses='')";
                PreparedStatement dnStmt = conn.prepareStatement(drNotequery);
                dnStmt.setString(1, companyId);
                ResultSet debitNoteRs = dnStmt.executeQuery();
                int dnCount = 0;
                while (debitNoteRs.next()) {
                    dnCount++;
                    String debitNoteId = StringUtil.isNullOrEmpty(debitNoteRs.getString("id")) ? "" : debitNoteRs.getString("id");
                    String debitNumber = StringUtil.isNullOrEmpty(debitNoteRs.getString("dnnumber")) ? "" : debitNoteRs.getString("dnnumber");
                    String customerId = StringUtil.isNullOrEmpty(debitNoteRs.getString("customer")) ? "" : debitNoteRs.getString("customer");
                    String vendorId = StringUtil.isNullOrEmpty(debitNoteRs.getString("vendor")) ? "" : debitNoteRs.getString("vendor");
                    String shpAliasName = "", shpAddr = "", shpCity = "", shpState = "", shpCountry = "", shppoCode = "", shpPhone = "", shpMobile = "", shpFax = "", shpEmail = "", shpCP = "", shpCPN = "", shpRN = "", shippingRoute = "";
                    String billAliasName = "", billAddr = "", billCity = "", billState = "", billCountry = "", billpoCode = "", billPhone = "", billMobile = "", billFax = "", billEmail = "", billCP = "", billCPN = "", billRN = "";
                    String selectquery = "";
                    PreparedStatement addressDetailsStmt = null;
                    if (!StringUtil.isNullOrEmpty(customerId)) {
                        selectquery = "select * from customeraddressdetails where customerid=? and isdefaultaddress='T'";
                        addressDetailsStmt = conn.prepareStatement(selectquery);
                        addressDetailsStmt.setString(1, customerId);
                    } else if (!StringUtil.isNullOrEmpty(vendorId)) {
                        selectquery = "select * from vendoraddressdetails where vendorid=? and isdefaultaddress='T'";
                        addressDetailsStmt = conn.prepareStatement(selectquery);
                        addressDetailsStmt.setString(1, vendorId);
                    }
            
                        ResultSet addressDetailsResult = addressDetailsStmt.executeQuery();
                        while (addressDetailsResult.next()) {
                            boolean isBilling = addressDetailsResult.getBoolean("isbillingaddress");
                            if (isBilling) {
                                billAliasName = StringUtil.isNullOrEmpty(addressDetailsResult.getString("aliasname")) ? "" : addressDetailsResult.getString("aliasname");
                                billAddr = StringUtil.isNullOrEmpty(addressDetailsResult.getString("address")) ? "" : addressDetailsResult.getString("address");
                                billCity = StringUtil.isNullOrEmpty(addressDetailsResult.getString("city")) ? "" : addressDetailsResult.getString("city");
                                billState = StringUtil.isNullOrEmpty(addressDetailsResult.getString("state")) ? "" : addressDetailsResult.getString("state");
                                billCountry = StringUtil.isNullOrEmpty(addressDetailsResult.getString("country")) ? "" : addressDetailsResult.getString("country");
                                billpoCode = StringUtil.isNullOrEmpty(addressDetailsResult.getString("postalcode")) ? "" : addressDetailsResult.getString("postalcode");
                                billPhone = StringUtil.isNullOrEmpty(addressDetailsResult.getString("phone")) ? "" : addressDetailsResult.getString("phone");
                                billMobile = StringUtil.isNullOrEmpty(addressDetailsResult.getString("mobilenumber")) ? "" : addressDetailsResult.getString("mobilenumber");
                                billFax = StringUtil.isNullOrEmpty(addressDetailsResult.getString("fax")) ? "" : addressDetailsResult.getString("fax");
                                billEmail = StringUtil.isNullOrEmpty(addressDetailsResult.getString("emailid")) ? "" : addressDetailsResult.getString("emailid");
                                billRN = StringUtil.isNullOrEmpty(addressDetailsResult.getString("recipientname")) ? "" : addressDetailsResult.getString("recipientname");
                                billCP = StringUtil.isNullOrEmpty(addressDetailsResult.getString("contactperson")) ? "" : addressDetailsResult.getString("contactperson");
                                billCPN = StringUtil.isNullOrEmpty(addressDetailsResult.getString("contactpersonnumber")) ? "" : addressDetailsResult.getString("contactpersonnumber");
                            } else {
                                shpAliasName = StringUtil.isNullOrEmpty(addressDetailsResult.getString("aliasname")) ? "" : addressDetailsResult.getString("aliasname");
                                shpAddr = StringUtil.isNullOrEmpty(addressDetailsResult.getString("address")) ? "" : addressDetailsResult.getString("address");
                                shpCity = StringUtil.isNullOrEmpty(addressDetailsResult.getString("city")) ? "" : addressDetailsResult.getString("city");
                                shpState = StringUtil.isNullOrEmpty(addressDetailsResult.getString("state")) ? "" : addressDetailsResult.getString("state");
                                shpCountry = StringUtil.isNullOrEmpty(addressDetailsResult.getString("country")) ? "" : addressDetailsResult.getString("country");
                                shppoCode = StringUtil.isNullOrEmpty(addressDetailsResult.getString("postalcode")) ? "" : addressDetailsResult.getString("postalcode");
                                shpPhone = StringUtil.isNullOrEmpty(addressDetailsResult.getString("phone")) ? "" : addressDetailsResult.getString("phone");
                                shpMobile = StringUtil.isNullOrEmpty(addressDetailsResult.getString("mobilenumber")) ? "" : addressDetailsResult.getString("mobilenumber");
                                shpFax = StringUtil.isNullOrEmpty(addressDetailsResult.getString("fax")) ? "" : addressDetailsResult.getString("fax");
                                shpEmail = StringUtil.isNullOrEmpty(addressDetailsResult.getString("emailid")) ? "" : addressDetailsResult.getString("emailid");
                                shpRN = StringUtil.isNullOrEmpty(addressDetailsResult.getString("recipientname")) ? "" : addressDetailsResult.getString("recipientname");
                                shpCP = StringUtil.isNullOrEmpty(addressDetailsResult.getString("contactperson")) ? "" : addressDetailsResult.getString("contactperson");
                                shpCPN = StringUtil.isNullOrEmpty(addressDetailsResult.getString("contactpersonnumber")) ? "" : addressDetailsResult.getString("contactpersonnumber");
                            }
                        }
                        String uuid = UUID.randomUUID().toString().replace("-", "");
                        String insertquery = "INSERT INTO billingshippingaddresses(id,billingaddress,billingcountry,billingstate,billingcity,"
                                + "billingpostal,billingemail,billingfax,billingmobile,billingphone,billingrecipientname,billingcontactperson,"
                                + "billingcontactpersonnumber,billingaddresstype,"
                                + "shippingaddress,shippingCountry,shippingstate,shippingcity,shippingpostal,shippingemail,shippingfax,"
                                + "shippingmobile,shippingphone,shippingcontactpersonnumber,shippingcontactperson,shippingrecipientname,"
                                + "shippingaddresstype,company) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        PreparedStatement insertstmt = conn.prepareStatement(insertquery);
                        insertstmt.setString(1, uuid);
                        insertstmt.setString(2, billAddr);
                        insertstmt.setString(3, billCountry);
                        insertstmt.setString(4, billState);
                        insertstmt.setString(5, billCity);
                        insertstmt.setString(6, billpoCode);
                        insertstmt.setString(7, billEmail);
                        insertstmt.setString(8, billFax);
                        insertstmt.setString(9, billMobile);
                        insertstmt.setString(10, billPhone);
                        insertstmt.setString(11, billRN);
                        insertstmt.setString(12, billCP);
                        insertstmt.setString(13, billCPN);
                        insertstmt.setString(14, billAliasName);
                        insertstmt.setString(15, shpAddr);
                        insertstmt.setString(16, shpCountry);
                        insertstmt.setString(17, shpState);
                        insertstmt.setString(18, shpCity);
                        insertstmt.setString(19, shppoCode);
                        insertstmt.setString(20, shpEmail);
                        insertstmt.setString(21, shpFax);
                        insertstmt.setString(22, shpMobile);
                        insertstmt.setString(23, shpPhone);
                        insertstmt.setString(24, shpCPN);
                        insertstmt.setString(25, shpCP);
                        insertstmt.setString(26, shpRN);
                        insertstmt.setString(27, shpAliasName);
                        insertstmt.setString(28, companyId);
                        insertstmt.execute();

                        String updatequery = "UPDATE debitnote SET billingshippingaddresses=? where id=? and company=?";
                        PreparedStatement updatestmt = conn.prepareStatement(updatequery);
                        updatestmt.setString(1, uuid);
                        updatestmt.setString(2, debitNoteId);
                        updatestmt.setString(3, companyId);
                        updatestmt.execute();
                        debitNumberList.add(debitNumber);
                    }

                    out.println("<br/>Company Name : <b>" + companyname + "</b>");
                    if (debitNumberList.isEmpty()) {
                        out.println("<br/>Number of Debit Note's=" + dnCount + ". All Debit Note are already updated or cannot update (In case customer/vendor does not have address).");
                    } else {
                        out.println("<br/>Number of Debit Note's=" + dnCount + " and Number of updated Records are : " + debitNumberList.size());
                        out.println("<br/>Updated Records are : " + debitNumberList.toString());
                    }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>
