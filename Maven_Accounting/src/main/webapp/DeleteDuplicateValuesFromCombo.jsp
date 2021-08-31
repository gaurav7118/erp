<%@page import="javax.print.attribute.HashAttributeSet"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<!--%@page contentType="text/html" pageEncoding="UTF-8"%>-->
<%@page import="java.sql.*"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="java.util.Map" %>



<%
    String message = "";
    String message1 = "";
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        String fieldlabel = request.getParameter("fieldlabel");
        int module = -1;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(fieldlabel)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password,fieldlabel) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery;
        String query = "";
        ResultSet rs;
        String company = "";
        String customquery = "";
        ResultSet custrs;
        String fieldId = "";
        String fieldcombodataid = "";
        String invno = "";
        int count = 0;
        long column = 1;

        String globalTable = "";
        String DetailTable = "";
        String DetailTableid = "";
        String Entryno = "";
        String customgolbaltable = "";
        String customgolbaltableid = "";
        String customdetailTable = "";
//         String customdetailTableid="";
        String refid = "";
        
        String moduleQuery = "select moduleid,companyid from fieldparams where fieldlabel = ? and companyid in (select companyid from company where subdomain = ?)";
        stmtquery = conn.prepareStatement(moduleQuery);
        stmtquery.setString(1, fieldlabel);
        stmtquery.setString(2, subDomain);
        rs = stmtquery.executeQuery();
        
        while(rs.next()){
            module = rs.getInt("moduleid");
            company = rs.getString("companyid");
            switch (module) {
                case 27:
                case 41:
                case 51://Consignment DO
                case 67://Lease DO
                    globalTable = "deliveryorder";
                    DetailTable = "dodetails";
                    DetailTableid = "dodetailsid";          //id in customdetail table
                    Entryno = "donumber";
                    customgolbaltable = "deliveryordercustomdata";
                    customgolbaltableid = "deliveryOrderId";
                    customdetailTable = "dodetailscustomdata";
    //                    customdetailTableid="dodetailsid";
                    refid = "accdodetailscustomdataref";
                    break;
                case 28:
                case 40:
                case 57://Consignment GRO
                    globalTable = "grorder";
                    DetailTable = "grodetails";
                    DetailTableid = "grodetailsid";
                    Entryno = "gronumber";
                    customgolbaltable = "grordercustomdata";
                    customgolbaltableid = "goodsreceiptorderid";
                    customdetailTable = "grodetailscustomdata";
    //                    customdetailTableid="grodetailsid";
                    refid = "accgrodetailscustomdataref";
                    break;
                case 18:        //PO
                case 63:
                case 90:
                    globalTable = "purchaseorder";
                    DetailTable = "podetails";
                    DetailTableid = "poDetailID";
                    Entryno = "ponumber";
                    customgolbaltable = "purchaseordercustomdata";
                    customgolbaltableid = "poID";
                    customdetailTable = "purchaseorderdetailcustomdata";
    //                    customdetailTableid="grodetailsid";
                    refid = "purchaseorderdetailcustomdataref";
                    break;
                case 20:            //SO
                case 36:            //LO
                case 50://Consignment Request
                case 1114://Vendor job workorder
                    globalTable = "salesorder";
                    DetailTable = "sodetails";
                    DetailTableid = "soDetailID";
                    Entryno = "sonumber";
                    customgolbaltable = "salesordercustomdata";
                    customgolbaltableid = "soID";
                    customdetailTable = "salesorderdetailcustomdata";
    //                    customdetailTableid="grodetailsid";
                    refid = "salesorderdetailcustomdataref";
                    break;
                case 23:        //VQ
                case 89:
                    globalTable = "vendorquotation";
                    DetailTable = "vendorquotationdetails";
                    DetailTableid = "vendorquotationdetailsid";
                    Entryno = "quotationnumber";
                    customgolbaltable = "vendorquotationcustomdata";
                    customgolbaltableid = "vendorquotationid";
                    customdetailTable = "vendorquotationdetailscustomdata";
    //                    customdetailTableid="grodetailsid";
                    refid = "accvendorquotationdetailscustomdataref";
                    break;
                case 22:        //Cq
                case 65://Lease Quotation
                    globalTable = "quotation";
                    DetailTable = "quotationdetails";
                    DetailTableid = "quotationdetailsid";
                    Entryno = "quotationnumber";
                    customgolbaltable = "quotationcustomdata";
                    customgolbaltableid = "quotationid";
                    customdetailTable = "quotationdetailscustomdata";
    //                    customdetailTableid="grodetailsid";
                    refid = "accquotationdetailscustomdataref";
                    break;
                case 29:        //SR
                case 53:
                case 68:
                case 98:
                    globalTable = "salesreturn";
                    DetailTable = "srdetails";
                    DetailTableid = "srdetailsid";
                    Entryno = "srnumber";
                    customgolbaltable = "salesreturncustomdata";
                    customgolbaltableid = "salesreturnid";
                    customdetailTable = "srdetailscustomdata";
    //                    customdetailTableid="grodetailsid";
                    refid = "accsrdetailsscustomdataref";
                    break;
                case 31:        //PR
                case 59:
                case 96:
                    globalTable = "purchasereturn";
                    DetailTable = "prdetails";
                    DetailTableid = "prdetailsid";
                    Entryno = "prnumber";
                    customgolbaltable = "purchasereturncustomdata";
                    customgolbaltableid = "purchasereturnid";
                    customdetailTable = "prdetailscustomdata";
    //                    customdetailTableid="grodetailsid";
                    refid = "accprdetailscustomdataref";
                    break;
                case 32:        //PRqui
                case 87:
                    globalTable = "purchaserequisition";
                    DetailTable = "purchaserequisitiondetail";
                    DetailTableid = "purchaserequisitiondetailid";
                    Entryno = "prnumber";
                    customgolbaltable = "purchaserequisitioncustomdata";
                    customgolbaltableid = "purchaserequisitionid";
                    customdetailTable = "purchaserequisitiondetailcustomdata";
    //                    customdetailTableid="grodetailsid";
                    refid = "accpurchaserequisitiondetailcustomdataref";
                    break;
                case 33:
                case 88:        //RFQ
                    customgolbaltable = "rfqcustomdata";
                    customgolbaltableid = "rfqid";
                    customdetailTable = "requestforquotationdetailcustomdata";
                    DetailTableid = "requestforquotationdetailid";
                    break;

                case 6:     //VI
                case 39:     //VI
                case 58://Consignment GR
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;
                case 2:     //CI
                case 38:     //CI
                case 52: //Consignment Invoice
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;
                case 14:    //MP
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;
                case 16:        //RP
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;
                case 10:        //DN
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;
                case 12:        //CN
                case 93://Lease Invoice
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;
                case 30:        //product
                case 42:        //group
                    customgolbaltable = "accproductcustomdata";
                    customgolbaltableid = "productId";

                    break;
                case 34:        //account
                    customgolbaltable = "accountcustomdata";
                    customgolbaltableid = "accountId";

                    break;
                case 24:        //JE
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";

                    break;
                case 25:        //Customer
                    customgolbaltable = "customercustomdata";
                    customgolbaltableid = "customerId";
                    break;

                case 35:        //contract
                case 64:        //Lease Contract
                    customgolbaltable = "contractcustomdata";
                    customgolbaltableid = "contractid";
                    break;
                case 26:        //Vendor
                    customgolbaltable = "vendorcustomdata";
                    customgolbaltableid = "vendorId";
                    break;
                case 79:        //Serial Window
                    customgolbaltable = "serialcustomdata";
                    customgolbaltableid = "serialdocumentmappingid";
                    break;
                case 121:
                    customgolbaltable = "assetdetailcustomdata";
                    customgolbaltableid = "assetDetailsId";
                    break;

                // ------------newly added cases---
                case 95:        //Stock Adjustment
                    customgolbaltable = "in_stockadjustment_customdata";
                    customgolbaltableid = "stockadjustmentid";
                    customdetailTable = "in_stockadjustment_customdata";
                    DetailTableid = "stockadjustmentid";
                    break;
                case 1004:      //CycleCount
                    customdetailTable = "cyclecountcustomdata";
                    DetailTableid = "ccid";
                    break;
                case 1001:      //Stock Request
                case 92:        //Inventory_ModuleId
                    customgolbaltable = "stockcustomdata";
                    customgolbaltableid = "stockId";
                    customdetailTable = "stockcustomdata";
                    DetailTableid = "stockId";
                    break;
                case 1002:      //Inter Store Stock Transfer
                case 1003:      //Inter Location Stock Transfer
                    customgolbaltable = "in_interstoretransfer_customdata";
                    customgolbaltableid = "istid";
                    customdetailTable = "in_interstoretransfer_customdata";
                    DetailTableid = "istid";
                    break;

                case 1101:      //Labour Master
                    customgolbaltable = "labourcustomdata";
                    customgolbaltableid = "labourId";
                    customdetailTable = "labourcustomdata";
                    DetailTableid = "labourId";
                    break;
                case 1102:      //Work Centre Master
                    customgolbaltable = "workcentrecustomdata";
                    customgolbaltableid = "workCentreId";
                    customdetailTable = "workcentrecustomdata";
                    DetailTableid = "workCentreId";
                    break;
                case 1103:      //Machine Master
                    customgolbaltable = "machinecustomdata";
                    customgolbaltableid = "machineId";
                    customdetailTable = "machinecustomdata";
                    DetailTableid = "machineId";
                    break;
                case 1105:      //Work Order Master
                    customgolbaltable = "workordercustomdata";
                    customgolbaltableid = "workOrderId";
                    customdetailTable = "workordercustomdata";
                    DetailTableid = "workOrderId";
                    break;
                case 1106:      //MRP Contract
                    customgolbaltable = "mrpcontractcustomdata";
                    customgolbaltableid = "contractId";
                    customdetailTable = "mrpcontractdetailscustomdata";
                    DetailTableid = "contractDetailsId";
                    break;
                case 1107:      //Routing Template
                    customgolbaltable = "routingtemplatecustomdata";
                    customgolbaltableid = "routingTemplateId";
                    customdetailTable = "routingtemplatecustomdata";
                    DetailTableid = "routingTemplateId";
                    break;
                case 1104:      //Job Work
                    customgolbaltable = "jobworkcustomdata";
                    customgolbaltableid = "jobworkId";
                    customdetailTable = "jobworkcustomdata";
                    DetailTableid = "jobworkId";
                case 1116:      //Security Gate
                    customgolbaltable = "securitygateentrycustomdata";
                    customgolbaltableid = "sgeid";
                    customdetailTable = "securitygateentrydetailcustomdata";
                    DetailTableid = "sgeDetailID";

            }

            if (!StringUtil.isNullOrEmpty("" + module)) {
    //             get fieldid and column no for respective dimention
                customquery = "select id,colnum from fieldparams where fieldlabel=? and companyid=? and moduleid=?";
                stmtquery = conn.prepareStatement(customquery);
                stmtquery.setString(1, fieldlabel);
                stmtquery.setString(2, company);
                stmtquery.setInt(3, module);
                custrs = stmtquery.executeQuery();

                while (custrs.next()) {
                    fieldId = custrs.getString("id");
                    column = custrs.getLong("colnum");
                    String vaulueid1 = "";
                    if (!StringUtil.isNullOrEmpty(fieldId)) {
                        customquery = "select id,value from fieldcombodata where fieldid=?";    //get all record for module
                        stmtquery = conn.prepareStatement(customquery);
                        stmtquery.setString(1, fieldId);
                        custrs = stmtquery.executeQuery();
                        String tempValue = "";
                        while (custrs.next()) {
                            String valueid = custrs.getString("id");
                            String value = custrs.getString("value");

                            String q1 = "select id from fieldcombodata where fieldid=? and value=?";        // get records for same value and field i.e. duplicate
                            stmtquery = conn.prepareStatement(q1);
                            stmtquery.setString(1, fieldId);
                            stmtquery.setString(2, value);
                            ResultSet r1 = stmtquery.executeQuery();
                            Map map = new HashMap();
                            int counter = 1;
                            while (r1.next()) {
                                vaulueid1 = r1.getString("id");
                                map.put("" + counter, vaulueid1);
                                counter++;

                            }
                            if (map.size() > 1) {           //if duplication is present i.e. duplicate combo values 
                                // find col data
                                String tablevalue = "";
                                String recvalue = "";
                                String colquery = "select " + customgolbaltableid + ",col" + column + " from " + customgolbaltable + " where company=?";
                                stmtquery = conn.prepareStatement(colquery);
                                stmtquery.setString(1, company);
                                ResultSet resultSet2 = stmtquery.executeQuery();
                                while (resultSet2.next()) {
                                    tablevalue = resultSet2.getString("col" + column + "");
                                    recvalue = resultSet2.getString(customgolbaltableid);
                                    if (map.size() > 1 && !StringUtil.isNullOrEmpty(tablevalue)) {
            //                            message += value;
                                        if (map.containsValue(tablevalue)) {
            //                        if (tablevalue.equalsIgnoreCase(map.get("" + 2).toString()) || tablevalue.equalsIgnoreCase(map.get("" + 3).toString()) || tablevalue.equalsIgnoreCase(map.get("" + 4).toString())) {
                                            String insertQuery = "update " + customgolbaltable + " set col" + column + "=? where " + customgolbaltableid + "=? and company=? ";
                                            PreparedStatement preparedStatement = conn.prepareStatement(insertQuery);
                                            preparedStatement.setString(1, map.get("" + 1).toString());                // value fro global level data
                                            preparedStatement.setString(2, recvalue);
                                            preparedStatement.setString(3, company);
                                            preparedStatement.executeUpdate();

                                        }
                                    }
                                }

                            }

                            //delete fieldCombodata which consist duplication
                            if (map.size() > 1) {
                                message1 += value + ",";
                                count++;
                                String deletequery = "delete from fieldcombodata where fieldid=? and id=?";
                                PreparedStatement statement = conn.prepareStatement(deletequery);
                                statement.setString(1, fieldId);
                                statement.setString(2, map.get("" + 2).toString());
                                statement.executeUpdate();

                                for (int i = 2; i <= map.size(); i++) {
                                    statement.setString(2, map.get("" + i).toString());
                                    statement.executeUpdate();
                                }

                            }
                        }
                    }
                }
            }
        }
        out.println("<b>Data updated for Dimension/Custom Field Values and Duplicate Values Deleted successfully.<b>");
        message += "\nSubdomain: " + subDomain + ",count=" + count + ", Fields=" + message1;

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
        out.print(message);
    }

%>