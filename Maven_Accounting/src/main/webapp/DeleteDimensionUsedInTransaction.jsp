<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>


<%!
    int udpateCustomTable(Connection conn, String customgolbaltableid, String customgolbaltable, long column, int module, String company) {
        String recvalue = "";
        int count = 0;
        try {
            String colquery = "select " + customgolbaltableid + " from " + customgolbaltable + " where col" + column + " is not NULL and moduleId=? and company=?";
            PreparedStatement stmtquery = conn.prepareStatement(colquery);
            stmtquery.setInt(1, module);
            stmtquery.setString(2, company);
            ResultSet resultSet2 = stmtquery.executeQuery();
            while (resultSet2.next()) {
                recvalue = resultSet2.getString(customgolbaltableid);
                String insertQuery = "update " + customgolbaltable + " set col" + column + "=NULL where " + customgolbaltableid + "=? and company=? ";
                PreparedStatement preparedStatement = conn.prepareStatement(insertQuery);
                preparedStatement.setString(1, recvalue);
                preparedStatement.setString(2, company);
                preparedStatement.executeUpdate();
                count++;
            }
        } catch (Exception ex) {

        }
        return count;
    }
%>

<%
    /* Please Note: Script is used only to set NULL in transactions where this field has been used. After Execution delete field safely from UI.*/
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
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);

        if (!StringUtil.isNullOrEmpty(subDomain) && !StringUtil.isNullOrEmpty(fieldlabel)) {
            PreparedStatement stmtquery;
            String query = "";
            ResultSet rs;
            String company = "";
            String customquery = "";
            ResultSet custrs;
//             get fieldid and column no for respective dimention
            customquery = "select id,colnum,companyid,moduleid,customcolumn,relatedmoduleid from fieldparams where fieldlabel=? and companyid in (select companyid from company where subdomain=?)";
            stmtquery = conn.prepareStatement(customquery);
            stmtquery.setString(1, fieldlabel);
            stmtquery.setString(2, subDomain);
            custrs = stmtquery.executeQuery();

            while (custrs.next()) {
                String fieldId = "";
                long column = 1;
                int globalcount = 0;
                String globalTable = "";
                String DetailTable = "";
                String DetailTableid = "";
                String customgolbaltable = "";
                String customgolbaltableid = "";
                String customdetailTable = "";
                String refkey = "";
                String productMasterTable = "";

                fieldId = custrs.getString("id");
                column = custrs.getLong("colnum");
                company = custrs.getString("companyid");
                int module = custrs.getInt("moduleid");
                int customColumn = custrs.getInt("customcolumn");
                String relatedModuleIds = custrs.getString("relatedmoduleid");
                switch (module) {
                    case 27:
                    case 41:
                    case 51:
                    case 67:
                        globalTable = "deliveryorder";
                        DetailTable = "dodetails";
                        DetailTableid = "dodetailsid";          //id in customdetail table
                        customgolbaltable = "deliveryordercustomdata";
                        customgolbaltableid = "deliveryOrderId";
                        customdetailTable = "dodetailscustomdata";
                        //                    customdetailTableid="dodetailsid";
//                        refid = "accdodetailscustomdataref";
                        break;
                    case 28:
                    case 40:
                    case 57:
                        globalTable = "grorder";
                        DetailTable = "grodetails";
                        DetailTableid = "grodetailsid";
                        customgolbaltable = "grordercustomdata";
                        customgolbaltableid = "goodsreceiptorderid";
                        customdetailTable = "grodetailscustomdata";
                        //                    customdetailTableid="grodetailsid";
//                        refid = "accgrodetailscustomdataref";
                        break;
                    case 18:        //PO
                    case 63:
                    case 90:
                        globalTable = "purchaseorder";
                        DetailTable = "podetails";
                        DetailTableid = "poDetailID";
                        customgolbaltable = "purchaseordercustomdata";
                        customgolbaltableid = "poID";
                        customdetailTable = "purchaseorderdetailcustomdata";
                        //                    customdetailTableid="grodetailsid";
//                        refid = "purchaseorderdetailcustomdataref";
                        break;
                    case 20:            //SO
                    case 36:            //LO
                    case 50:
                        globalTable = "salesorder";
                        DetailTable = "sodetails";
                        DetailTableid = "soDetailID";
                        customgolbaltable = "salesordercustomdata";
                        customgolbaltableid = "soID";
                        customdetailTable = "salesorderdetailcustomdata";
                        //                    customdetailTableid="grodetailsid";
//                        refid = "salesorderdetailcustomdataref";
                        break;
                    case 23:        //VQ
                    case 89:
                        globalTable = "vendorquotation";
                        DetailTable = "vendorquotationdetails";
                        DetailTableid = "vendorquotationdetailsid";
                        customgolbaltable = "vendorquotationcustomdata";
                        customgolbaltableid = "vendorquotationid";
                        customdetailTable = "vendorquotationdetailscustomdata";
                        //                    customdetailTableid="grodetailsid";
//                        refid = "accvendorquotationdetailscustomdataref";
                        break;
                    case 22:        //Cq
                    case 65:
                        globalTable = "quotation";
                        DetailTable = "quotationdetails";
                        DetailTableid = "quotationdetailsid";
                        customgolbaltable = "quotationcustomdata";
                        customgolbaltableid = "quotationid";
                        customdetailTable = "quotationdetailscustomdata";
                        //                    customdetailTableid="grodetailsid";
//                        refid = "accquotationdetailscustomdataref";
                        break;
                    case 29:        //SR
                    case 53:
                    case 68:
                    case 98:
                        globalTable = "salesreturn";
                        DetailTable = "srdetails";
                        DetailTableid = "srdetailsid";
                        customgolbaltable = "salesreturncustomdata";
                        customgolbaltableid = "salesreturnid";
                        customdetailTable = "srdetailscustomdata";
                        //                    customdetailTableid="grodetailsid";
//                        refid = "accsrdetailsscustomdataref";
                        break;
                    case 31:        //PR
                    case 59:
                    case 96:
                        globalTable = "purchasereturn";
                        DetailTable = "prdetails";
                        DetailTableid = "prdetailsid";
                        customgolbaltable = "purchasereturncustomdata";
                        customgolbaltableid = "purchasereturnid";
                        customdetailTable = "prdetailscustomdata";
                        //                    customdetailTableid="grodetailsid";
//                        refid = "accprdetailscustomdataref";
                        break;
                    case 32:        //PRqui
                    case 87:
                        globalTable = "purchaserequisition";
                        DetailTable = "purchaserequisitiondetail";
                        DetailTableid = "purchaserequisitiondetailid";
                        customgolbaltable = "purchaserequisitioncustomdata";
                        customgolbaltableid = "purchaserequisitionid";
                        customdetailTable = "purchaserequisitiondetailcustomdata";
                        //                    customdetailTableid="grodetailsid";
//                        refid = "accpurchaserequisitiondetailcustomdataref";
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
                    case 58:
                        customgolbaltable = "accjecustomdata";
                        customgolbaltableid = "journalentryId";
                        customdetailTable = "accjedetailcustomdata";
                        DetailTableid = "jedetailId";
                        break;
                    case 2:     //CI
                    case 38:     //CI
                    case 52:
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
                    case 93:
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
                        customdetailTable = "accjedetailcustomdata";
                        DetailTableid = "jedetailId";
                        break;
                    case 35:        //contract
                        customgolbaltable = "contractcustomdata";
                        customgolbaltableid = "contractid";
                        break;
                    case 25:        //Customer
                        customgolbaltable = "customercustomdata";
                        customgolbaltableid = "customerId";
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
                }

                if (customColumn == 0) {  //If global field
                    if (module != 30) {
                        //delete from custom global table
                        int count = udpateCustomTable(conn, customgolbaltableid, customgolbaltable, column, module, company);
                        globalcount = globalcount + count;

                    } else {
                        if (!StringUtil.isNullOrEmpty(relatedModuleIds)) {
                            String[] moduleIdStr = relatedModuleIds.split(",");
                            for (int cnt = 0; cnt < moduleIdStr.length; cnt++) {
                                int relatedmodule = Integer.parseInt(moduleIdStr[cnt]);
                                switch (relatedmodule) {

                                    case 27:
                                        productMasterTable = "dodetailproductcustomdata";
                                        refkey = "doDetailID";
                                        break;
                                    case 18:        //PO
                                        productMasterTable = "podetailproductcustomdata";
                                        refkey = "poDetailID";
                                        break;
                                    case 20:            //SO
                                        productMasterTable = "sodetailproductcustomdata";
                                        refkey = "soDetailID";
                                        break;
                                    case 2:     //CI
                                    case 6:     //VI
                                        productMasterTable = "accjedetailproductcustomdata";
                                        refkey = "jedetailId";
                                        break;
                                }

                                String columnData = "SELECT " + refkey + " from " + productMasterTable + " WHERE col" + column + " is not null AND moduleId=? AND company=?";
                                stmtquery = conn.prepareStatement(columnData);
                                stmtquery.setInt(1, relatedmodule);
                                stmtquery.setString(2, company);
                                ResultSet globaltablers = stmtquery.executeQuery();

                                while (globaltablers.next()) {
                                    String id = globaltablers.getString(refkey);
                                    String query2 = "UPDATE " + productMasterTable + " SET col" + column + "=null where " + refkey + "=? AND company=?";
                                    stmtquery = conn.prepareStatement(query2);
                                    stmtquery.setString(1, id);
                                    stmtquery.setString(2, company);
                                    stmtquery.executeUpdate();
                                    globalcount++;
                                }

                            }
                        }
                        int count = udpateCustomTable(conn, customgolbaltableid, customgolbaltable, column, module, company);
                        globalcount = globalcount + count;
                    }

                } else if (customColumn == 1 && StringUtil.isNullOrEmpty(customdetailTable)) {
                    //delete from custom global table
                    int count = udpateCustomTable(conn, customgolbaltableid, customgolbaltable, column, module, company);
                    globalcount = globalcount + count;

                } else {
                    //delete from custom detail table.
                    int count = udpateCustomTable(conn, DetailTableid, customdetailTable, column, module, company);
                    globalcount = globalcount + count;
                }

                message1 += "<br><b>Module: </b>" + module + "&nbsp;&nbsp;&nbsp;&nbsp;<b>Count: </b>" + globalcount;
            }
        }

        out.println("<b>Data updated for Dimension/Custom Field Values.<b>");
        message += "<br><b>Subdomain:</b>" + subDomain + "<br><b>Count:</b>" + message1;

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