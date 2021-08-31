<%-- 
    Document   : LineLevelToGlobalLevelCustomDataMigration
    Created on : Oct 12, 2016, 1:10:29 PM
    Author     : krawler
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.mysql.jdbc.exceptions.MySQLSyntaxErrorException"%>
<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>



<%

    Connection conn = null;
    try {
        //http://<app-url>/LineLevelToGlobalLevelCustomFieldMigration.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?&fieldlabel=?
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        String fieldLabel =request.getParameter("fieldlabel");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery;
        PreparedStatement pstmt;
        String customQuery = "";
        ResultSet customrs = null;
        ResultSet recdetailrs = null;
        String columnData = "";
        String linetableData = "";
        String globaltableData = "";

        customQuery = "SELECT colnum,moduleid,companyid FROM fieldparams WHERE fieldlabel=? AND companyid IN (SELECT companyid FROM company WHERE subdomain=?) ";
        stmtquery = conn.prepareStatement(customQuery);
        stmtquery.setString(1, fieldLabel);
        stmtquery.setString(2, subDomain);
        customrs = stmtquery.executeQuery();
        while (customrs.next()) {
            String message = "";
            String globalTable = "";
            String DetailTable = "";
            String DetailTableid = "";
            String customgolbaltable = "";
            String linetableid = "";
            String linetable = "";
            String refkey = "";
            String reflinekey = "";
            String reftomaintable = "";
            String maintable = "";
            String customgolbaltableid = "";
            String customdetailTable = "";
            String refid = "";
            int count = 0;
            int module = Integer.parseInt(customrs.getString("moduleid"));
            String columnNumber = customrs.getString("colnum");
            int colnum = Integer.parseInt(columnNumber);
            String company = customrs.getString("companyid");
            switch (module) {

                case 41:
                case 51:
                case 67:
                case 27:
                    linetable = "dodetails";
                    linetableid = "id";
                    reflinekey = "accdodetailscustomdataref";
                    refkey = "accdeliveryordercustomdataref";
                    reftomaintable = "deliveryorder";
                    maintable = "deliveryorder";
                    customgolbaltable = "deliveryordercustomdata";
                    customgolbaltableid = "deliveryOrderId";
                    customdetailTable = "dodetailscustomdata";
                    DetailTableid = "dodetailsid";
                    break;
                case 28:
                case 40:
                case 57:
                    linetable = "grodetails";
                    linetableid = "id";
                    reflinekey = "accgrodetailscustomdataref";
                    refkey = "accgrordercustomdataref";
                    reftomaintable = "grorder";
                    maintable = "grorder";
                    customgolbaltable = "grordercustomdata";
                    customgolbaltableid = "goodsreceiptorderid";
                    customdetailTable = "grodetailscustomdata";
                    DetailTableid = "grodetailsid";
                    break;

                case 63:
                case 90:
                case 18:
                    linetable = "podetails";
                    linetableid = "id";
                    reflinekey = "purchaseorderdetailcustomdataref";
                    refkey = "purchaseordercustomdataref";
                    reftomaintable = "purchaseorder";
                    maintable = "purchaseorder";
                    customgolbaltable = "purchaseordercustomdata";
                    customgolbaltableid = "poID";
                    customdetailTable = "purchaseorderdetailcustomdata";
                    DetailTableid = "poDetailID";
                    break;
                case 36:            //LO
                case 50:
                case 20://consignment
                    linetable = "sodetails";
                    linetableid = "id";
                    reflinekey = "salesorderdetailcustomdataref";
                    refkey = "salesordercustomdataref";
                    reftomaintable = "salesorder";
                    maintable = "salesorder";
                    customgolbaltable = "salesordercustomdata";
                    customgolbaltableid = "soID";
                    customdetailTable = "salesorderdetailcustomdata";
                    DetailTableid = "soDetailID";
                    break;
                case 23:        //VQ
                case 89:
                    linetable = "vendorquotationdetails";
                    linetableid = "id";
                    reflinekey = "accvendorquotationdetailscustomdataref";
                    refkey = "accvendorquotationcustomdataref";
                    reftomaintable = "vendorquotation";
                    maintable = "vendorquotation";
                    customgolbaltable = "vendorquotationcustomdata";
                    customgolbaltableid = "vendorquotationid";
                    customdetailTable = "vendorquotationdetailscustomdata";
                    DetailTableid = "vendorquotationdetailsid";
                    break;

                case 22:        //Cq
                case 65:
                    linetable = "quotationdetails";
                    linetableid = "id";
                    reflinekey = "accquotationdetailscustomdataref";
                    refkey = "accquotationcustomdataref";
                    reftomaintable = "quotation";
                    maintable = "quotation";
                    customgolbaltable = "quotationcustomdata";
                    customgolbaltableid = "quotationid";
                    customdetailTable = "quotationdetailscustomdata";
                    DetailTableid = "quotationdetailsid";

                    break;
                case 29:        //SR
                case 53:
                case 68:
                case 98:
                    linetable = "srdetails";
                    linetableid = "id";
                    reflinekey = "accsrdetailsscustomdataref";
                    refkey = "accsalesreturncustomdataref";
                    reftomaintable = "salesreturn";
                    maintable = "salesreturn";
                    customgolbaltable = "salesreturncustomdata";
                    customgolbaltableid = "salesreturnid";
                    customdetailTable = "srdetailscustomdata";
                    DetailTableid = "srdetailsid";
                    break;
                case 31:        //PR
                case 59:
                case 96:
                    linetable = "prdetails";
                    linetableid = "id";
                    reflinekey = "accprdetailscustomdataref";
                    refkey = "accpurchasereturncustomdataref";
                    reftomaintable = "purchasereturn";
                    maintable = "purchasereturn";
                    customgolbaltable = "purchasereturncustomdata";
                    customgolbaltableid = "purchasereturnid";
                    customdetailTable = "prdetailscustomdata";
                    DetailTableid = "prdetailsid";
                    break;
                case 32:        //PRqui
                case 87:
                    linetable = "purchaserequisitiondetail";
                    linetableid = "id";
                    reflinekey = "accpurchaserequisitiondetailcustomdataref";
                    refkey = "accpurchaserequisitioncustomdataref";
                    reftomaintable = "purchaserequisition";
                    maintable = "purchaserequisition";
                    customgolbaltable = "purchaserequisitioncustomdata";
                    customgolbaltableid = "purchaserequisitionid";
                    customdetailTable = "purchaserequisitiondetailcustomdata";
                    DetailTableid = "purchaserequisitiondetailid";
                    break;
                case 33:
                case 88:        //RFQ
                    linetable = "requestforquotationdetail";
                    linetableid = "id";
                    reflinekey = "accrequestforquotationdetailcustomdataref";
                    refkey = "accrfqcustomdataref";
                    reftomaintable = "requestforquotation";
                    maintable = "requestforquotation";
                    customgolbaltable = "rfqcustomdata";
                    customgolbaltableid = "rfqid";
                    customdetailTable = "requestforquotationdetailcustomdata";
                    DetailTableid = "requestforquotationdetailid";
                    break;
                case 2:     //CI
                case 6:     //VI
                case 24:
                case 38:
                case 39:   //FA -PI 
                case 52:
                case 58:
                case 93:
                case 10:          //DN
                case 12:         //CN
                case 14:        //MP
                case 16:       //RP
                    linetable = "jedetail";
                    linetableid = "id";
                    refkey = "accjecustomdataref";
                    reflinekey = "accjedetailcustomdataref";
                    reftomaintable = "journalEntry";
                    maintable = "journalentry";
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;

            }
            if (!customgolbaltable.isEmpty()) {             // Select all those transactions for which custom field is used.
                columnData = "SELECT " + DetailTableid + ",col" + columnNumber + " from " + customdetailTable + " WHERE col" + columnNumber + " is not null AND moduleId=? AND company=?";
                stmtquery = conn.prepareStatement(columnData);
                stmtquery.setInt(1, module);
                stmtquery.setString(2, company);
                ResultSet linetablers = stmtquery.executeQuery();
                while (linetablers.next()) {
                    String detailtableid = linetablers.getString(DetailTableid);
                    String value = linetablers.getString("col" + columnNumber);
                    String globaltableIdquery = "select " + reftomaintable + " from " + linetable + " where id=? limit 1";
                    stmtquery = conn.prepareStatement(globaltableIdquery);
                    stmtquery.setString(1, detailtableid);
                    ResultSet globaltabledata = stmtquery.executeQuery();
                    while (globaltabledata.next()) {
                        String globaltableid = globaltabledata.getString(reftomaintable);
                        // check data in global custom table 
                        String q = "select col" + columnNumber + " from " + customgolbaltable + " where " + customgolbaltableid + " =?";
                        stmtquery = conn.prepareStatement(q);
                        stmtquery.setString(1, globaltableid);
                        ResultSet globalcustomtabledata = stmtquery.executeQuery();
                        if (!globalcustomtabledata.next()) {
                            // insert data into global custom table

                            String query5 = "insert into " + customgolbaltable + " (" + customgolbaltableid + ",company,moduleId,col" + columnNumber + ") values(?,?,?,?)";
                            stmtquery = conn.prepareStatement(query5);
                            stmtquery.setString(1, globaltableid);
                            stmtquery.setString(2, company);
                            stmtquery.setInt(3, module);
                            stmtquery.setString(4, value);
                            stmtquery.executeUpdate();
                             count++;
                        } else {
                            String val = globalcustomtabledata.getString("col" + columnNumber);
                            if (StringUtil.isNullOrEmpty(val)) {
                                String q2 = "update " + customgolbaltable + " set col" + columnNumber + " =? where " + customgolbaltableid + "=?";
                                stmtquery = conn.prepareStatement(q2);
                                stmtquery.setString(1, value);
                                stmtquery.setString(2, globaltableid);
                                stmtquery.executeUpdate();
                                 count++;
                            }
                        }
                        // update global table
                        String q2 = "update " + maintable + " set " + refkey + " =? where " + linetableid + "=?";
                        stmtquery = conn.prepareStatement(q2);
                        stmtquery.setString(1, globaltableid);
                        stmtquery.setString(2, globaltableid);
                        stmtquery.executeUpdate();
                    }

                    // update line custom table
                    String q2 = "update " + customdetailTable + " set col" + columnNumber + " =NULL where " + DetailTableid + "=?";
                    stmtquery = conn.prepareStatement(q2);
                    stmtquery.setString(1, detailtableid);
                    stmtquery.executeUpdate();

                }
            }

            message = "<br>Module: " + module + "&nbsp;&nbsp;&nbsp; Records moved from Line level to Global level : " + count + "<br>";
            out.print(message);
        }

        // Make custom field line level
        String makeLinelevel = "UPDATE fieldparams SET customcolumn=0,isformultientity=1,isessential=1 WHERE fieldlabel=? AND companyid IN (SELECT companyid FROM company WHERE subdomain=?)";
        pstmt = conn.prepareStatement(makeLinelevel);
        pstmt.setString(1, fieldLabel);
        pstmt.setString(2, subDomain);
        int num = pstmt.executeUpdate();

    } catch (MySQLSyntaxErrorException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
                conn.close();
                out.println("<center>Connection Closed....</center><br/>");
                out.println("<br><br><center>Execution Ended @ " + new java.util.Date() + "</center><br><br>");
            }
    }
%>