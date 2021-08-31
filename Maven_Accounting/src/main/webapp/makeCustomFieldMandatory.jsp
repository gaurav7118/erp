<%@page import="java.text.SimpleDateFormat"%>
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
    /* Make Custom Field/Dimension Mandatory by setting default value to existing records.*/
    /* Note: Please provide Date default value in this format =>  yyyy-MM-DD */
    /* Send comma (,) separated default value for Multiselect Drop down. e.g abc,xyz */
    /* This script is written only to make Global level fields Mandatory. Need to handle to make line level field mandatory. */
    
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
        String defalutValue = request.getParameter("default"); // Send comma (,) separated default value for Multiselect Drop down. e.g abc,xyz

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
            ResultSet nullRefDataSet;
            ResultSet noNullRefDataSet;
            String defaultToBeUpdated = defalutValue;
//             get fieldid and column no for respective dimention
            customquery = "select id,colnum,companyid,moduleid,customcolumn,relatedmoduleid,fieldtype from fieldparams where fieldlabel=? and companyid in (select companyid from company where subdomain=?)";
            stmtquery = conn.prepareStatement(customquery);
            stmtquery.setString(1, fieldlabel);
            stmtquery.setString(2, subDomain);
            custrs = stmtquery.executeQuery();

            while (custrs.next()) {
                String fieldId = "";
                long column = 1;
                int globalcount = 0;
                int insertcount = 0;
                String globalTable = "";
                String DetailTable = "";
                String DetailTableid = "";
                String customgolbaltable = "";
                String customgolbaltableid = "";
                String customdetailTable = "";
                String refkey = "";
                String productMasterTable = "";
                String linetableid = "";
                String linetable = "";
                String reflinekey = "";
                String reftomaintable = "";
                String maintable = "";
                String refid = "";

                fieldId = custrs.getString("id");
                column = custrs.getLong("colnum");
                company = custrs.getString("companyid");
                int module = custrs.getInt("moduleid");
                int customColumn = custrs.getInt("customcolumn");
                String relatedModuleIds = custrs.getString("relatedmoduleid");
                int type = custrs.getInt("fieldtype");
                String moduleSpecificDefaultValue = "";

                // Get Default Value Logic here
                switch (type) {
                    case 4:
                        String defaultValueQuery = "SELECT id FROM fieldcombodata WHERE value=? AND fieldid IN (SELECT id FROM fieldparams WHERE fieldlabel=? AND moduleid=? AND companyid=?)";
                        stmtquery = conn.prepareStatement(defaultValueQuery);
                        stmtquery.setString(1, defalutValue);
                        stmtquery.setString(2, fieldlabel);
                        stmtquery.setInt(3, module);
                        stmtquery.setString(4, company);
                        ResultSet defRs = stmtquery.executeQuery();
                        while (defRs.next()) {
                            moduleSpecificDefaultValue = defRs.getString("id");
                        }

                        break; 
                    case 3:
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        java.util.Date date = sdf.parse(defalutValue);
                        moduleSpecificDefaultValue = date.getTime() + "";
                        defaultToBeUpdated = moduleSpecificDefaultValue;
                        break;
                    case 7:
                        String[] valueArray = defalutValue.split(",");
                        String value = "";
                        for (int val = 0; val < valueArray.length; val++) {
                            value = value + ",'" + valueArray[val] + "'";
                        }
                        if (value.length() > 0) {
                            value = value.substring(1, value.length());
                        }

                        defaultValueQuery = "SELECT id FROM fieldcombodata WHERE value IN (" + value + ") AND fieldid IN (SELECT id FROM fieldparams WHERE fieldlabel=? AND moduleid=? AND companyid=?)";
                        stmtquery = conn.prepareStatement(defaultValueQuery);
                        stmtquery.setString(1, fieldlabel);
                        stmtquery.setInt(2, module);
                        stmtquery.setString(3, company);
                        defRs = stmtquery.executeQuery();
                        String ValueId = "";
                        while (defRs.next()) {
                            ValueId = ValueId + defRs.getString("id") + ",";
                        }
                        if (ValueId.length() > 0) {
                            moduleSpecificDefaultValue = ValueId.substring(0, ValueId.length() - 1);
                        }

                        break;
                    default:
                        moduleSpecificDefaultValue = defalutValue;
                        break;
                }

                switch (module) {
                    case 27:
                    case 41:
                    case 51:
                    case 67:
                        globalTable = "deliveryorder";
                        DetailTable = "dodetails";
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
                        globalTable = "grorder";
                        DetailTable = "grodetails";
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
                    case 18:        //PO
                    case 63:
                    case 90:
                        globalTable = "purchaseorder";
                        DetailTable = "podetails";
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
                    case 20:            //SO
                    case 36:            //LO
                    case 50:
                        globalTable = "salesorder";
                        DetailTable = "sodetails";
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
                        globalTable = "vendorquotation";
                        DetailTable = "vendorquotationdetails";
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
                        globalTable = "quotation";
                        DetailTable = "quotationdetails";
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
                        globalTable = "purchasereturn";
                        DetailTable = "prdetails";
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
                    case 6:     //VI
                    case 39:     //VI
                    case 58:
                    case 2:     //CI
                    case 38:     //CI
                    case 52:
                    case 14:
                    case 16:
                    case 10:        //DN
                    case 12:        //CN
                    case 93:
                    case 24:
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
                    case 30:        //product
                    case 42:        //group
                        customgolbaltable = "accproductcustomdata";
                        customgolbaltableid = "productId";
                        linetableid = "id";
                        maintable = "product";
                        refkey = "accproductcustomdataref";
                        break;
                    case 34:        //account
                        customgolbaltable = "accountcustomdata";
                        customgolbaltableid = "accountId";
                        linetableid = "id";
                        maintable = "account";
                        refkey = "accaccountcustomdataref";
                        break;
                    case 35:        //contract
                        customgolbaltable = "contractcustomdata";
                        customgolbaltableid = "contractid";
                        linetableid = "id";
                        maintable = "contract";
                        refkey = "contractcustomdataref";
                        break;
                    case 25:        //Customer
                        customgolbaltable = "customercustomdata";
                        customgolbaltableid = "customerId";
                        linetableid = "id";
                        maintable = "customer";
                        refkey = "acccustomercustomdataref";
                        break;
                    case 26:        //Vendor
                        customgolbaltable = "vendorcustomdata";
                        customgolbaltableid = "vendorId";
                        linetableid = "id";
                        maintable = "vendor";
                        refkey = "accvendorcustomdataref";
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

                if (!StringUtil.isNullOrEmpty(moduleSpecificDefaultValue)) {
                    if (customColumn == 0) {  //If global field

                        // Null Reference 
                        if (module == 6 || module == 39 || module == 58 || module == 2 || module == 38 || module == 52 || module == 14 || module == 16 || module == 10 || module == 12 || module == 93 || module == 24) {
                            String getTransQuery = "select " + linetableid + " from " + maintable + " where " + refkey + " is null and transactionModuleid=? and company=?";
                            stmtquery = conn.prepareStatement(getTransQuery);
                                if (module == 24) {
                                    stmtquery.setInt(1, 0);
                                } else {
                            stmtquery.setInt(1, module);
                                }

                            stmtquery.setString(2, company);
                            nullRefDataSet = stmtquery.executeQuery();
                        } else {
                            String getTransQuery = "select " + linetableid + " from " + maintable + " where " + refkey + " is null and company=?";
                            stmtquery = conn.prepareStatement(getTransQuery);
                            stmtquery.setString(1, company);
                            nullRefDataSet = stmtquery.executeQuery();
                        }

//                        if (nullRefDataSet.next()) {
                            // Insert
                            while (nullRefDataSet.next()) {
                                String globaltableid = nullRefDataSet.getString(linetableid);
                                String query5 = "insert into " + customgolbaltable + " (" + customgolbaltableid + ",company,moduleId,col" + column + ") values(?,?,?,?)";

                                stmtquery = conn.prepareStatement(query5);
                                stmtquery.setString(1, globaltableid);
                                stmtquery.setString(2, company);
                                stmtquery.setInt(3, module);
                                stmtquery.setString(4, moduleSpecificDefaultValue);
                                stmtquery.executeUpdate();
                                udpateCustomRefId(conn, maintable, refkey, linetableid, globaltableid);        // Update Ref Id in Details table
                                insertcount++;
                            }
//                        }

                        String getDataQuery = "select " + customgolbaltableid + " from " + customgolbaltable + " where moduleId=? and (col" + column + " is null OR col" + column + "= '') and company=?";
                        stmtquery = conn.prepareStatement(getDataQuery);
                        stmtquery.setInt(1, module);
                        stmtquery.setString(2, company);
                        ResultSet dataSet = stmtquery.executeQuery();
                        while (dataSet.next()) {
                            String id = dataSet.getString(customgolbaltableid);
                            String updateQuery = "UPDATE " + customgolbaltable + " set col" + column + " =? where " + customgolbaltableid + "=? AND company=?";
                            stmtquery = conn.prepareStatement(updateQuery);
                            stmtquery.setString(1, moduleSpecificDefaultValue);
                            stmtquery.setString(2, id);
                            stmtquery.setString(3, company);
                            int count = stmtquery.executeUpdate();
                            globalcount++;
                        }

                    } else if (customColumn == 1 && !StringUtil.isNullOrEmpty(customdetailTable)) { //Line Field
                      // Not handled for line level field.
                        }
                    }

                message1 += "<br><b>Module: </b>" + module + "&nbsp;&nbsp;&nbsp;&nbsp;<b>Update Count: " + globalcount +"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp <b> Insert Count: "+ insertcount +"</b>";
            }
            
            String updateToMandatory = "UPDATE fieldparams SET isessential=1, defaultvalue=? ,isautopopulatedefaultvalue=1 WHERE fieldlabel=? and companyid IN (SELECT companyid FROM company WHERE subdomain=?) ";
            stmtquery = conn.prepareStatement(updateToMandatory);
            stmtquery.setString(1, defaultToBeUpdated);
            stmtquery.setString(2, fieldlabel);
            stmtquery.setString(3, subDomain);
            int mandatoarycount = stmtquery.executeUpdate();
            
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