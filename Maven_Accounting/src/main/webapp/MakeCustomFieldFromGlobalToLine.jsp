

<%@page import="com.mysql.jdbc.exceptions.MySQLSyntaxErrorException"%>
<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%!
    int udpateCustomRefId(Connection conn, String linetable, String refkey, String linetableid, String id) {
        int count = 0;
        try {
            String updateRef = "UPDATE " + linetable + " SET " + refkey + "=?  WHERE " + linetableid + "=?";
            PreparedStatement stmtquery = conn.prepareStatement(updateRef);
            stmtquery.setString(1, id);
            stmtquery.setString(2, id);
            count = stmtquery.executeUpdate();
        } catch (Exception ex) {

        }
        return count;
    }
%>

<%

    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        String fieldLabel = request.getParameter("fieldlabel");
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
        ResultSet expdetailrs = null;
        String columnData = "";
        String linetableData = "";

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
            String reftomaintable = "";
            String customgolbaltableid = "";
            String customdetailTable = "";
            String refid = "";
            int count = 0;
            int module = Integer.parseInt(customrs.getString("moduleid"));
            String columnNumber = customrs.getString("colnum");
            int colnum = Integer.parseInt(columnNumber);
            String company = customrs.getString("companyid");

            switch (module) {

                case 27:
                    linetable = "dodetails";
                    linetableid = "id";
                    refkey = "accdodetailscustomdataref";
                    reftomaintable = "deliveryorder";
                    customgolbaltable = "deliveryordercustomdata";
                    customgolbaltableid = "deliveryOrderId";
                    customdetailTable = "dodetailscustomdata";
                    DetailTableid = "dodetailsid";
                    globalTable = "dodetailproductcustomdata";
                    refid = "doDetailID";
                    break;
                case 41:
                case 51:
                case 67:
                    linetable = "dodetails";
                    linetableid = "id";
                    refkey = "accdodetailscustomdataref";
                    reftomaintable = "deliveryorder";
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
                    refkey = "accgrodetailscustomdataref";
                    reftomaintable = "grorder";
                    customgolbaltable = "grordercustomdata";
                    customgolbaltableid = "goodsreceiptorderid";
                    customdetailTable = "grodetailscustomdata";
                    DetailTableid = "grodetailsid";
                    break;
                case 18:        //PO
                    linetable = "podetails";
                    linetableid = "id";
                    refkey = "purchaseorderdetailcustomdataref";
                    reftomaintable = "purchaseorder";
                    customgolbaltable = "purchaseordercustomdata";
                    customgolbaltableid = "poID";
                    customdetailTable = "purchaseorderdetailcustomdata";
                    DetailTableid = "poDetailID";
                    globalTable = "podetailproductcustomdata";
                    refid = "poDetailID";
                    break;
                case 63:
                case 90:
                    linetable = "podetails";
                    linetableid = "id";
                    refkey = "purchaseorderdetailcustomdataref";
                    reftomaintable = "purchaseorder";
                    customgolbaltable = "purchaseordercustomdata";
                    customgolbaltableid = "poID";
                    customdetailTable = "purchaseorderdetailcustomdata";
                    DetailTableid = "poDetailID";
                    break;
                case 20:            //SO
                    linetable = "sodetails";
                    linetableid = "id";
                    refkey = "salesorderdetailcustomdataref";
                    reftomaintable = "salesorder";
                    customgolbaltable = "salesordercustomdata";
                    customgolbaltableid = "soID";
                    customdetailTable = "salesorderdetailcustomdata";
                    DetailTableid = "soDetailID";
                    globalTable = "sodetailproductcustomdata";
                    refid = "soDetailID";
                    break;
                case 36:            //LO
                case 50:                //consignment
                    linetable = "sodetails";
                    linetableid = "id";
                    refkey = "salesorderdetailcustomdataref";
                    reftomaintable = "salesorder";
                    customgolbaltable = "salesordercustomdata";
                    customgolbaltableid = "soID";
                    customdetailTable = "salesorderdetailcustomdata";
                    DetailTableid = "soDetailID";
                    break;
                case 23:        //VQ
                case 89:
                    linetable = "vendorquotationdetails";
                    linetableid = "id";
                    refkey = "accvendorquotationdetailscustomdataref";
                    reftomaintable = "vendorquotation";
                    customgolbaltable = "vendorquotationcustomdata";
                    customgolbaltableid = "vendorquotationid";
                    customdetailTable = "vendorquotationdetailscustomdata";
                    DetailTableid = "vendorquotationdetailsid";
                    break;
                case 24:        //CI
                    linetable = "jedetail";
                    linetableid = "id";
                    refkey = "accjedetailcustomdataref";
                    reftomaintable = "journalEntry";
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;
                case 22:        //Cq
                case 65:
                    linetable = "quotationdetails";
                    linetableid = "id";
                    refkey = "accquotationdetailscustomdataref";
                    reftomaintable = "quotation";
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
                    refkey = "accsrdetailsscustomdataref";
                    reftomaintable = "salesreturn";
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
                    refkey = "accprdetailscustomdataref";
                    reftomaintable = "purchasereturn";
                    customgolbaltable = "purchasereturncustomdata";
                    customgolbaltableid = "purchasereturnid";
                    customdetailTable = "prdetailscustomdata";
                    DetailTableid = "prdetailsid";
                    break;
                case 32:        //PRqui
                case 87:
                    linetable = "purchaserequisitiondetail";
                    linetableid = "id";
                    refkey = "accpurchaserequisitiondetailcustomdataref";
                    reftomaintable = "purchaserequisition";
                    customgolbaltable = "purchaserequisitioncustomdata";
                    customgolbaltableid = "purchaserequisitionid";
                    customdetailTable = "purchaserequisitiondetailcustomdata";
                    DetailTableid = "purchaserequisitiondetailid";
                    break;
                case 33:
                case 88:        //RFQ
                    linetable = "requestforquotationdetail";
                    linetableid = "id";
                    refkey = "accrequestforquotationdetailcustomdataref";
                    reftomaintable = "requestforquotation";
                    customgolbaltable = "rfqcustomdata";
                    customgolbaltableid = "rfqid";
                    customdetailTable = "requestforquotationdetailcustomdata";
                    DetailTableid = "requestforquotationdetailid";
                    break;
                case 34:
                    customgolbaltable = "accountcustomdata";
                    customgolbaltableid = "accountId";
                    break;
                case 2:     //CI
                case 6:     //VI
                    linetable = "jedetail";
                    linetableid = "id";
                    refkey = "accjedetailcustomdataref";
                    reftomaintable = "journalEntry";
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    globalTable = "accjedetailproductcustomdata";
                    refid = "jedetailId";
                    break;
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
                    refkey = "accjedetailcustomdataref";
                    reftomaintable = "journalEntry";
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;
                case 25:        //Customer
                    customgolbaltable = "customercustomdata";
                    customgolbaltableid = "customerId";
                    break;
                case 26:        //Vendor
                    customgolbaltable = "vendorcustomdata";
                    customgolbaltableid = "vendorId";
                    break;
                case 30:        //product
                case 42:        //group
                    customgolbaltable = "accproductcustomdata";
                    customgolbaltableid = "productId";
                    break;
                case 35:        //contract
                case 64:
                    customgolbaltable = "contractcustomdata";
                    customgolbaltableid = "contractid";
                    break;
                case 79:        //Serial Window
                    customgolbaltable = "serialcustomdata";
                    customgolbaltableid = "serialdocumentmappingid";
                    break;
                case 92:        //Stock Issue / Stock Request
                case 1001:
                    customgolbaltable = "stockcustomdata";
                    customgolbaltableid = "stockId";
                    break;
                case 95:        //Stock Adjustment
                    customgolbaltable = "in_stockadjustment_customdata";
                    customgolbaltableid = "stockadjustmentid";
                    break;
                case 1002:        // Inter Store Stock Transfer
                    customgolbaltable = "in_interstoretransfer_customdata";
                    customgolbaltableid = "istid";
                    break;
            }

            if (!customdetailTable.isEmpty()) {             // Select all those transactions for which custom field is used.
                columnData = "SELECT " + customgolbaltableid + ",col" + columnNumber + " from " + customgolbaltable + " WHERE col" + columnNumber + " is not null AND moduleId=? AND company=?";
                stmtquery = conn.prepareStatement(columnData);
                stmtquery.setInt(1, module);
                stmtquery.setString(2, company);
                ResultSet globaltablers = stmtquery.executeQuery();

                while (globaltablers.next()) {
                    String columnId = globaltablers.getString(customgolbaltableid);
                    String value = globaltablers.getString("col" + columnNumber);

                    if (!StringUtil.isNullOrEmpty(value)) {
                        linetableData = "SELECT " + linetableid + " FROM " + linetable + " WHERE " + reftomaintable + "=?";
                        stmtquery = conn.prepareStatement(linetableData);
                        stmtquery.setString(1, columnId);
                        ResultSet linetablers = stmtquery.executeQuery();
                        
                        // Set NULL value in global table
                        String query2 = "update " + customgolbaltable + " set col" + columnNumber + "=null where " + customgolbaltableid + "=? AND company=?";
                        stmtquery = conn.prepareStatement(query2);
                        stmtquery.setString(1, columnId);
                        stmtquery.setString(2, company);
                        stmtquery.executeUpdate();

                        while (linetablers.next()) {
                            String id = linetablers.getString(linetableid);

                            String query3 = "SELECT 1 FROM " + customdetailTable + " WHERE " + DetailTableid + "=?";
                            stmtquery = conn.prepareStatement(query3);
                            stmtquery.setString(1, id);
                            ResultSet rs = stmtquery.executeQuery();
                            if (rs.next()) {
                                //Update existing details

                                String query4 = "update " + customdetailTable + " set col" + columnNumber + " = ? where " + DetailTableid + "=? AND company=?";
                                stmtquery = conn.prepareStatement(query4);
                                stmtquery.setString(1, value);
                                stmtquery.setString(2, id);
                                stmtquery.setString(3, company);
                                stmtquery.executeUpdate();
                                count++;
                            } else {
                                // Insert 
                                if (module == 2) {
                                    String query4 = "SELECT id FROM invoicedetails WHERE salesjedid=? AND company=?";
                                    stmtquery = conn.prepareStatement(query4);
                                    stmtquery.setString(1, id);
                                    stmtquery.setString(2, company);
                                    recdetailrs = stmtquery.executeQuery();
                                    if (recdetailrs.next()) {
                                        String query5 = "insert into " + customdetailTable + " (jedetailId,company,moduleId,recdetailId,col" + columnNumber + ") values(?,?,?,?,?)";
                                        stmtquery = conn.prepareStatement(query5);
                                        stmtquery.setString(1, id);
                                        stmtquery.setString(2, company);
                                        stmtquery.setInt(3, module);
                                        stmtquery.setString(4, recdetailrs.getString("id"));
                                        stmtquery.setString(5, value);
                                        stmtquery.executeUpdate();

                                        udpateCustomRefId(conn, linetable, refkey, linetableid, id);        // Update Ref Id in Details table
                                        count++;
                                    }

                                } else if (module == 6) {
                                    String query4 = "SELECT id FROM grdetails WHERE purchasejedid=? AND company=?";
                                    stmtquery = conn.prepareStatement(query4);
                                    stmtquery.setString(1, id);
                                    stmtquery.setString(2, company);
                                    recdetailrs = stmtquery.executeQuery();
                                    if (recdetailrs.next()) {
                                        String query5 = "insert into " + customdetailTable + " (jedetailId,company,moduleId,recdetailId,col" + columnNumber + ") values(?,?,?,?,?)";
                                        stmtquery = conn.prepareStatement(query5);
                                        stmtquery.setString(1, id);
                                        stmtquery.setString(2, company);
                                        stmtquery.setInt(3, module);
                                        stmtquery.setString(4, recdetailrs.getString("id"));
                                        stmtquery.setString(5, value);
                                        stmtquery.executeUpdate();

                                        udpateCustomRefId(conn, linetable, refkey, linetableid, id);                // Update Ref Id in Details table
                                        count++;
                                    } else {
                                        String query8 = "SELECT id FROM expenseggrdetails WHERE purchasejedid=? AND company=?";
                                        stmtquery = conn.prepareStatement(query8);
                                        stmtquery.setString(1, id);
                                        stmtquery.setString(2, company);
                                        expdetailrs = stmtquery.executeQuery();
                                        if (expdetailrs.next()) {
                                            String query5 = "insert into " + customdetailTable + " (jedetailId,company,moduleId,recdetailId,col" + columnNumber + ") values(?,?,?,?,?)";
                                            stmtquery = conn.prepareStatement(query5);
                                            stmtquery.setString(1, id);
                                            stmtquery.setString(2, company);
                                            stmtquery.setInt(3, module);
                                            stmtquery.setString(4, expdetailrs.getString("id"));
                                            stmtquery.setString(5, value);
                                            stmtquery.executeUpdate();

                                            udpateCustomRefId(conn, linetable, refkey, linetableid, id);                // Update Ref Id in Details table
                                            count++;
                                    }
                                    }

                                } else if (module == 10) {                          // Debit Note
                                    String query4 = "SELECT id FROM dntaxentry WHERE totaljedid=? AND company=?";
                                    stmtquery = conn.prepareStatement(query4);
                                    stmtquery.setString(1, id);
                                    stmtquery.setString(2, company);
                                    recdetailrs = stmtquery.executeQuery();
                                    if (recdetailrs.next()) {
                                        String query5 = "insert into " + customdetailTable + " (jedetailId,company,moduleId,recdetailId,col" + columnNumber + ") values(?,?,?,?,?)";
                                        stmtquery = conn.prepareStatement(query5);
                                        stmtquery.setString(1, id);
                                        stmtquery.setString(2, company);
                                        stmtquery.setInt(3, module);
                                        stmtquery.setString(4, recdetailrs.getString("id"));
                                        stmtquery.setString(5, value);
                                        stmtquery.executeUpdate();

                                        udpateCustomRefId(conn, linetable, refkey, linetableid, id);                // Update Ref Id in Details table
                                        count++;
                                    }

                                } else if (module == 12) {                          // Credit Note
                                    String query4 = "SELECT id FROM cntaxentry WHERE totaljedid=? AND company=?";
                                    stmtquery = conn.prepareStatement(query4);
                                    stmtquery.setString(1, id);
                                    stmtquery.setString(2, company);
                                    recdetailrs = stmtquery.executeQuery();
                                    if (recdetailrs.next()) {
                                        String query5 = "insert into " + customdetailTable + " (jedetailId,company,moduleId,recdetailId,col" + columnNumber + ") values(?,?,?,?,?)";
                                        stmtquery = conn.prepareStatement(query5);
                                        stmtquery.setString(1, id);
                                        stmtquery.setString(2, company);
                                        stmtquery.setInt(3, module);
                                        stmtquery.setString(4, recdetailrs.getString("id"));
                                        stmtquery.setString(5, value);
                                        stmtquery.executeUpdate();

                                        udpateCustomRefId(conn, linetable, refkey, linetableid, id);            // Update Ref Id in Details table
                                        count++;
                                    }
                                } else if (module == 14) {                          // Make Payment

                                    String query4 = "SELECT id FROM paymentdetail WHERE totaljedid=? AND company=?";
                                    stmtquery = conn.prepareStatement(query4);
                                    stmtquery.setString(1, id);
                                    stmtquery.setString(2, company);
                                    recdetailrs = stmtquery.executeQuery();
                                    if (recdetailrs.next()) {
                                        String query5 = "insert into " + customdetailTable + " (jedetailId,company,moduleId,recdetailId,col" + columnNumber + ") values(?,?,?,?,?)";
                                        stmtquery = conn.prepareStatement(query5);
                                        stmtquery.setString(1, id);
                                        stmtquery.setString(2, company);
                                        stmtquery.setInt(3, module);
                                        stmtquery.setString(4, recdetailrs.getString("id"));
                                        stmtquery.setString(5, value);
                                        stmtquery.executeUpdate();

                                        udpateCustomRefId(conn, linetable, refkey, linetableid, id);                // Update Ref Id in Details table
                                        count++;
                                    } else {
                                        query4 = "SELECT id FROM paymentdetailotherwise WHERE totaljedid=? ";
                                        stmtquery = conn.prepareStatement(query4);
                                        stmtquery.setString(1, id);
                                        ResultSet rs1 = stmtquery.executeQuery();
                                        if (rs1.next()) {
                                            String query5 = "insert into " + customdetailTable + " (jedetailId,company,moduleId,recdetailId,col" + columnNumber + ") values(?,?,?,?,?)";
                                            stmtquery = conn.prepareStatement(query5);
                                            stmtquery.setString(1, id);
                                            stmtquery.setString(2, company);
                                            stmtquery.setInt(3, module);
                                            stmtquery.setString(4, rs1.getString("id"));
                                            stmtquery.setString(5, value);
                                            stmtquery.executeUpdate();

                                            udpateCustomRefId(conn, linetable, refkey, linetableid, id);                // Update Ref Id in Details table
                                            count++;
                                        } else {
                                            query4 = "SELECT id FROM advancedetail WHERE totaljedid=? AND company=?";
                                            stmtquery = conn.prepareStatement(query4);
                                            stmtquery.setString(1, id);
                                            stmtquery.setString(2, company);
                                            ResultSet rs2 = stmtquery.executeQuery();
                                            if (rs2.next()) {
                                                String query5 = "insert into " + customdetailTable + " (jedetailId,company,moduleId,recdetailId,col" + columnNumber + ") values(?,?,?,?,?)";
                                                stmtquery = conn.prepareStatement(query5);
                                                stmtquery.setString(1, id);
                                                stmtquery.setString(2, company);
                                                stmtquery.setInt(3, module);
                                                stmtquery.setString(4, rs2.getString("id"));
                                                stmtquery.setString(5, value);
                                                stmtquery.executeUpdate();

                                                udpateCustomRefId(conn, linetable, refkey, linetableid, id);                    // Update Ref Id in Details table
                                                count++;
                                            }

                                        }
                                    }

                                } else if (module == 16) {                              //Receive Payment
                                    String query4 = "SELECT id FROM receiptdetails WHERE totaljedid=? AND company=?";
                                    stmtquery = conn.prepareStatement(query4);
                                    stmtquery.setString(1, id);
                                    stmtquery.setString(2, company);
                                    recdetailrs = stmtquery.executeQuery();
                                    if (recdetailrs.next()) {
                                        String query5 = "insert into " + customdetailTable + " (jedetailId,company,moduleId,recdetailId,col" + columnNumber + ") values(?,?,?,?,?)";
                                        stmtquery = conn.prepareStatement(query5);
                                        stmtquery.setString(1, id);
                                        stmtquery.setString(2, company);
                                        stmtquery.setInt(3, module);
                                        stmtquery.setString(4, recdetailrs.getString("id"));
                                        stmtquery.setString(5, value);
                                        stmtquery.executeUpdate();

                                        udpateCustomRefId(conn, linetable, refkey, linetableid, id);                // Update Ref Id in Details table
                                        count++;
                                        
                                    } else {
                                        query4 = "SELECT id FROM receiptdetailotherwise WHERE totaljedid=? ";
                                        stmtquery = conn.prepareStatement(query4);
                                        stmtquery.setString(1, id);
                                        ResultSet rs1 = stmtquery.executeQuery();
                                        if (rs1.next()) {
                                            String query5 = "insert into " + customdetailTable + " (jedetailId,company,moduleId,recdetailId,col" + columnNumber + ") values(?,?,?,?,?)";
                                            stmtquery = conn.prepareStatement(query5);
                                            stmtquery.setString(1, id);
                                            stmtquery.setString(2, company);
                                            stmtquery.setInt(3, module);
                                            stmtquery.setString(4, rs1.getString("id"));
                                            stmtquery.setString(5, value);
                                            stmtquery.executeUpdate();

                                            udpateCustomRefId(conn, linetable, refkey, linetableid, id);                    // Update Ref Id in Details table
                                            count++;
                                        }
                                    }
                                } else if (module == 24) {
                                    String query5 = "insert into " + customdetailTable + " (jedetailId,company,moduleId,recdetailId,col" + columnNumber + ") values(?,?,?,?,?)";
                                    stmtquery = conn.prepareStatement(query5);
                                    stmtquery.setString(1, id);
                                    stmtquery.setString(2, company);
                                    stmtquery.setInt(3, module);
                                    stmtquery.setString(4, id);
                                    stmtquery.setString(5, value);
                                    stmtquery.executeUpdate();

                                    udpateCustomRefId(conn, linetable, refkey, linetableid, id);                    // Update Ref Id in Details table
                                    count++;
                                } else {
                                    String query5 = "insert into " + customdetailTable + " (" + DetailTableid + ",company,moduleId,col" + columnNumber + ") values(?,?,?,?)";
                                    stmtquery = conn.prepareStatement(query5);
                                    stmtquery.setString(1, id);
                                    stmtquery.setString(2, company);
                                    stmtquery.setInt(3, module);
                                    stmtquery.setString(4, value);
                                    stmtquery.executeUpdate();

                                    udpateCustomRefId(conn, linetable, refkey, linetableid, id);                    // Update Ref Id in Details table
                                    count++;
                                }
                                
                            }
                        }

                    }
                }
            }

            message = "<br>Module: " + module + "&nbsp;&nbsp;&nbsp; Records moved from Global level to Line level: " + count + "<br>";
            out.print(message);
        }
        
        // Make custom field line level
        String makeLinelevel = "UPDATE fieldparams SET customcolumn=1 WHERE fieldlabel=? AND companyid IN (SELECT companyid FROM company WHERE subdomain=?)";
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
        }
    }
%>