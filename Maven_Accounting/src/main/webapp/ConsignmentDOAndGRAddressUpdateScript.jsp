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
                List doNumberList = new ArrayList();
                List grNumberList = new ArrayList();

                //CDO Part
                String doquery = "select deliveryorder.id as doid,deliveryorder.donumber,deliveryorder.customer,billingshippingaddresses.id as addressid,billingshippingaddresses.billingaddress from deliveryorder "
                        + " left join billingshippingaddresses on deliveryorder.billingshippingaddresses=billingshippingaddresses.id"
                        + " where deliveryorder.deleteflag='F' and  deliveryorder.isconsignment='T' and deliveryorder.company=?";
                PreparedStatement dostmt = conn.prepareStatement(doquery);
                dostmt.setString(1, companyId);
                ResultSet dors = dostmt.executeQuery();
                int docount=0;
                while (dors.next()) {
                    docount++;
                    String doid = StringUtil.isNullOrEmpty(dors.getString("doid")) ? "" : dors.getString("doid");
                    String donumber = StringUtil.isNullOrEmpty(dors.getString("donumber")) ? "" : dors.getString("donumber");
                    String customerid = StringUtil.isNullOrEmpty(dors.getString("customer")) ? "" : dors.getString("customer");
                    String addressid = StringUtil.isNullOrEmpty(dors.getString("addressid")) ? "" : dors.getString("addressid");
                    String billingaddress = StringUtil.isNullOrEmpty(dors.getString("billingaddress")) ? "" : dors.getString("billingaddress");
                    String shpAliasName = "", shpAddr = "", shpCity = "", shpState = "", shpCountry = "", shppoCode = "", shpPhone = "", shpMobile = "", shpFax = "", shpEmail = "", shpCP = "", shpCPN = "", shpRN = "", shippingRoute = "";
                    String billAliasName = "", billAddr = "", billCity = "", billState = "", billCountry = "", billpoCode = "", billPhone = "", billMobile = "", billFax = "", billEmail = "", billCP = "", billCPN = "", billRN = "";
                    boolean isCustomerHasAddress = false;

                    String selectquery = "select * from customeraddressdetails where customerid=? and isdefaultaddress='T'";
                    PreparedStatement addressDetailsStmt = conn.prepareStatement(selectquery);
                    addressDetailsStmt.setString(1, customerid);
                    ResultSet addressDetailsResult = addressDetailsStmt.executeQuery();
                    while (addressDetailsResult.next()) {
                        isCustomerHasAddress = true;
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
                            shippingRoute = StringUtil.isNullOrEmpty(addressDetailsResult.getString("shippingroute")) ? "" : addressDetailsResult.getString("shippingroute");
                        }
                    }
                    if (isCustomerHasAddress) { //If customer have address then we can update or add address in DO otherwise we cannot. so here is check 
                        if (!StringUtil.isNullOrEmpty(addressid)) { // if address entry present 
                            if (StringUtil.isNullOrEmpty(billingaddress)) { //This check is for avoiding update when script already executed
                                String updateQuery = "UPDATE billingshippingaddresses SET billingaddress=?,billingcountry=?,billingstate=?,billingcity=?,"
                                        + "billingpostal=?,billingemail=?,billingfax=?,billingmobile=?,billingphone=?,billingrecipientname=?,billingcontactperson=?,"
                                        + "billingcontactpersonnumber=?,billingaddresstype=?,"
                                        + "shippingaddress=?,shippingCountry=?,shippingstate=?,shippingcity=?,shippingpostal=?,shippingemail=?,shippingfax=?,"
                                        + "shippingmobile=?,shippingphone=?,shippingcontactpersonnumber=?,shippingcontactperson=?,shippingrecipientname=?,"
                                        + "shippingroute=?,shippingaddresstype=?,company=?"
                                        + " WHERE id=?";
                                PreparedStatement updatestmt = conn.prepareStatement(updateQuery);
                                updatestmt.setString(1, billAddr);
                                updatestmt.setString(2, billCountry);
                                updatestmt.setString(3, billState);
                                updatestmt.setString(4, billCity);
                                updatestmt.setString(5, billpoCode);
                                updatestmt.setString(6, billEmail);
                                updatestmt.setString(7, billFax);
                                updatestmt.setString(8, billMobile);
                                updatestmt.setString(9, billPhone);
                                updatestmt.setString(10, billRN);
                                updatestmt.setString(11, billCP);
                                updatestmt.setString(12, billCPN);
                                updatestmt.setString(13, billAliasName);
                                updatestmt.setString(14, shpAddr);
                                updatestmt.setString(15, shpCountry);
                                updatestmt.setString(16, shpState);
                                updatestmt.setString(17, shpCity);
                                updatestmt.setString(18, shppoCode);
                                updatestmt.setString(19, shpEmail);
                                updatestmt.setString(20, shpFax);
                                updatestmt.setString(21, shpMobile);
                                updatestmt.setString(22, shpPhone);
                                updatestmt.setString(23, shpCPN);
                                updatestmt.setString(24, shpCP);
                                updatestmt.setString(25, shpRN);
                                updatestmt.setString(26, shippingRoute);
                                updatestmt.setString(27, shpAliasName);
                                updatestmt.setString(28, companyId);
                                updatestmt.setString(29, addressid);
                                updatestmt.execute();
                                doNumberList.add(donumber);
                            }
                        } else { //If no address entry 
                            String uuid = UUID.randomUUID().toString().replace("-", "");
                            String insertquery = "INSERT INTO billingshippingaddresses(id,billingaddress,billingcountry,billingstate,billingcity,"
                                    + "billingpostal,billingemail,billingfax,billingmobile,billingphone,billingrecipientname,billingcontactperson,"
                                    + "billingcontactpersonnumber,billingaddresstype,"
                                    + "shippingaddress,shippingCountry,shippingstate,shippingcity,shippingpostal,shippingemail,shippingfax,"
                                    + "shippingmobile,shippingphone,shippingcontactpersonnumber,shippingcontactperson,shippingrecipientname,"
                                    + "shippingroute,shippingaddresstype,company) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
                            insertstmt.setString(27, shippingRoute);
                            insertstmt.setString(28, shpAliasName);
                            insertstmt.setString(29, companyId);
                            insertstmt.execute();

                            String updatequery = "UPDATE deliveryorder SET billingshippingaddresses=? where id=? and company=?";
                            PreparedStatement updatestmt = conn.prepareStatement(updatequery);
                            updatestmt.setString(1, uuid);
                            updatestmt.setString(2, doid);
                            updatestmt.setString(3, companyId);
                            updatestmt.execute();
                            doNumberList.add(donumber);
                        }
                    }
                }
                out.println("Company Name : <b>" + companyname + "</b>");
                if (doNumberList.isEmpty()) {
                    out.println("<br/>Number of Consignment DO's="+docount+". All Consignment DO are already updated or cannot update (In case customer does not have address).");
                } else {
                    out.println("<br/>Number of Consignment DO's="+docount+" and Number of updated Records are : " + doNumberList.size());
                    out.println("<br/>Updated Records are : " + doNumberList.toString());
                }
                //CGR 
                String grquery = "select grorder.id as grid,grorder.gronumber,grorder.vendor,billingshippingaddresses.id as addressid,billingshippingaddresses.billingaddress from grorder "
                        + " left join billingshippingaddresses on grorder.billingshippingaddresses=billingshippingaddresses.id"
                        + " where grorder.deleteflag='F' and  grorder.isconsignment='T' and grorder.company=?";
                PreparedStatement grstmt = conn.prepareStatement(grquery);
                grstmt.setString(1, companyId);
                ResultSet grrs = grstmt.executeQuery();
                int grocount=0;
                while (grrs.next()) {
                    grocount++;
                    String grid = StringUtil.isNullOrEmpty(grrs.getString("grid")) ? "" : grrs.getString("grid");
                    String gronumber = StringUtil.isNullOrEmpty(grrs.getString("gronumber")) ? "" : grrs.getString("gronumber");
                    String vendorid = StringUtil.isNullOrEmpty(grrs.getString("vendor")) ? "" : grrs.getString("vendor");
                    String addressid = StringUtil.isNullOrEmpty(grrs.getString("addressid")) ? "" : grrs.getString("addressid");
                    String billingaddress = StringUtil.isNullOrEmpty(grrs.getString("billingaddress")) ? "" : grrs.getString("billingaddress");

                    String shpAliasName = "", shpAddr = "", shpCity = "", shpState = "", shpCountry = "", shppoCode = "", shpPhone = "", shpMobile = "", shpFax = "", shpEmail = "", shpCP = "", shpCPN = "", shpRN = "";
                    String billAliasName = "", billAddr = "", billCity = "", billState = "", billCountry = "", billpoCode = "", billPhone = "", billMobile = "", billFax = "", billEmail = "", billCP = "", billCPN = "", billRN = "";
                    boolean isVendorHasAddress = false;
                    String selectquery = "select * from vendoraddressdetails where vendorid=? and isdefaultaddress='T'";
                    PreparedStatement addressDetailsStmt = conn.prepareStatement(selectquery);
                    addressDetailsStmt.setString(1, vendorid);
                    ResultSet addressDetailsResult = addressDetailsStmt.executeQuery();
                    while (addressDetailsResult.next()) {
                        isVendorHasAddress = true;
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
                    if (isVendorHasAddress) {
                        if (!StringUtil.isNullOrEmpty(addressid)) { // if address entry present 
                            if (StringUtil.isNullOrEmpty(billingaddress)) { //This check is for avoiding update when script already executed
                                String updateQuery = "UPDATE billingshippingaddresses SET billingaddress=?,billingcountry=?,billingstate=?,billingcity=?,"
                                        + "billingpostal=?,billingemail=?,billingfax=?,billingmobile=?,billingphone=?,billingrecipientname=?,billingcontactperson=?,"
                                        + "billingcontactpersonnumber=?,billingaddresstype=?,"
                                        + "shippingaddress=?,shippingCountry=?,shippingstate=?,shippingcity=?,shippingpostal=?,shippingemail=?,shippingfax=?,"
                                        + "shippingmobile=?,shippingphone=?,shippingcontactpersonnumber=?,shippingcontactperson=?,shippingrecipientname=?,"
                                        + "shippingaddresstype=?,company=?"
                                        + "WHERE id=?";
                                PreparedStatement updatestmt = conn.prepareStatement(updateQuery);
                                updatestmt.setString(1, billAddr);
                                updatestmt.setString(2, billCountry);
                                updatestmt.setString(3, billState);
                                updatestmt.setString(4, billCity);
                                updatestmt.setString(5, billpoCode);
                                updatestmt.setString(6, billEmail);
                                updatestmt.setString(7, billFax);
                                updatestmt.setString(8, billMobile);
                                updatestmt.setString(9, billPhone);
                                updatestmt.setString(10, billRN);
                                updatestmt.setString(11, billCP);
                                updatestmt.setString(12, billCPN);
                                updatestmt.setString(13, billAliasName);
                                updatestmt.setString(14, shpAddr);
                                updatestmt.setString(15, shpCountry);
                                updatestmt.setString(16, shpState);
                                updatestmt.setString(17, shpCity);
                                updatestmt.setString(18, shppoCode);
                                updatestmt.setString(19, shpEmail);
                                updatestmt.setString(20, shpFax);
                                updatestmt.setString(21, shpMobile);
                                updatestmt.setString(22, shpPhone);
                                updatestmt.setString(23, shpCPN);
                                updatestmt.setString(24, shpCP);
                                updatestmt.setString(25, shpRN);
                                updatestmt.setString(26, shpAliasName);
                                updatestmt.setString(27, companyId);
                                updatestmt.setString(28, addressid);
                                updatestmt.execute();
                                grNumberList.add(gronumber);
                            }
                        } else { //If no address entry 
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

                            String updatequery = "UPDATE grorder SET billingshippingaddresses=? where id=? and company=?";
                            PreparedStatement updatestmt = conn.prepareStatement(updatequery);
                            updatestmt.setString(1, uuid);
                            updatestmt.setString(2, grid);
                            updatestmt.setString(3, companyId);
                            updatestmt.execute();
                            grNumberList.add(gronumber);
                        }
                    }
                }
                if (grNumberList.isEmpty()) {
                    out.println("<br/>Number of Consignment GR="+grocount+". All Consignment GR are already updated or cannot update (In case vendor does not have address).");
                } else {
                    out.println("</br>Number of Consignment GR="+grocount+" and number of updated Records are : " + grNumberList.size());
                    out.println("</br>Updated Records are : " + grNumberList.toString());
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

%>