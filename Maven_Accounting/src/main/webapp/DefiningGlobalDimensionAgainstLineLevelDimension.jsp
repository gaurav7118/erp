
<%@page import="com.krawler.spring.accounting.account.accAccountDAOImpl"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.mysql.jdbc.exceptions.MySQLSyntaxErrorException"%>
<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="com.krawler.spring.accounting.account.accAccountDAO"%>"
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>


<%

    Connection conn = null;
    try {
        //http://<app-url>/LineLevelToGlobalLevelCustomFieldMigration.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?&fieldlabel=?
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
//        String subDomain = request.getParameter("subdomain");
        String fieldLabel =request.getParameter("fieldlabel");
        String newFieldLabel=request.getParameter("newfieldlabel");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName)) {
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
        String mainTableQuery="";
        String columnData = "";
        String linetableData = "";
        String globaltableData = "";
        Set<String> DimensionValues = new HashSet<String>();
        boolean isSameLineAndGlobalTableUsed=false;
      
        
        
        customQuery = "SELECT colnum,moduleid,companyid FROM fieldparams WHERE fieldlabel=? AND companyid IN (SELECT companyid FROM company WHERE subdomain='chkl') ";
        stmtquery = conn.prepareStatement(customQuery);
        stmtquery.setString(1, fieldLabel);
//        stmtquery.setString(2, subDomain);
        customrs = stmtquery.executeQuery();
        int ModuleID=0;
        while (customrs.next()) {   
            try {
                    String newuniqueFieldCombo="";
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
                    ModuleID = module;
                    String columnNumber = customrs.getString("colnum");
                    int colnum = Integer.parseInt(columnNumber);
                    String company = customrs.getString("companyid");
//                    if (module != 12) {
//                        continue;
//                        
//                    }
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
                        case 35:        //contract  
                            linetable = "contractdetails";
                            linetableid = "id";
                            refkey = "contractcustomdataref";
                            reflinekey = "contractdetailcustomdataref";
                            reftomaintable = "contract";
                            maintable = "contract";
                            customgolbaltable = "contractcustomdata";
                            customgolbaltableid = "contractid";
                            customdetailTable = "contractdetailcustomdata";
                            DetailTableid = "scDetailID";
                            break;
                        case 25:        //Customer                    
                            maintable = "customer";
                            customgolbaltable = "customercustomdata"; //module with same table.
                            customgolbaltableid = "customerId";//customerId
                            isSameLineAndGlobalTableUsed = true;
                            break;
                        case 26:        //Vendor
                            maintable = "vendor";
                            customgolbaltable = "vendorcustomdata";
                            customgolbaltableid = "vendorId";        //module with same table.        
                            isSameLineAndGlobalTableUsed = true;
                            break;
                        case 34:        //account
                            maintable = "account";
                            customgolbaltable = "accountcustomdata"; //maintable=account
                            customgolbaltableid = "accountId";
                            isSameLineAndGlobalTableUsed = true;
                            break;
                        case 121:
                            maintable = "assetdetail";
                            customgolbaltable = "assetdetailcustomdata";  //maintable=assetdetail
                            customgolbaltableid = "assetDetailsId";       //module with same table.
                            isSameLineAndGlobalTableUsed = true;
                            break;
                        case 92:
                            maintable = "in_goodsrequest";
                            customgolbaltable = "stockcustomdata"; //mainTable=in_goodsrequest
                            customgolbaltableid = "stockId";
                            isSameLineAndGlobalTableUsed = true;
                            break;
                        case 79:        //Serial Window 
                            maintable="serialdocumentmapping";
                            customgolbaltable = "serialcustomdata"; //maintable="serialdocumentmapping"
                            customgolbaltableid = "serialdocumentmappingid";
                            isSameLineAndGlobalTableUsed = true;
                            break;    

                        /*
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
                         */
                    }
                    

                        if (isSameLineAndGlobalTableUsed) {
                            /*
                             if Same table used for Global and lineLevel Fields
                             */
                            mainTableQuery = "select cgt." + customgolbaltableid + " from " + customgolbaltable + " cgt where cgt.company='" + company + "' and ((cgt.col" + columnNumber + " <> '') and (  cgt.col" + columnNumber + " is not null))  ";

                        } else {

                            mainTableQuery = "select mt.id,cdt.col" + columnNumber + " from " + maintable + " mt inner join " + linetable + " dt on mt.id=dt." + reftomaintable + ""
                                    + " inner join " + customdetailTable + " cdt on cdt." + DetailTableid + "=dt.id    "
                                    + "where mt.company='" + company + "' and ((cdt.col" + columnNumber + " <> '') and ( cdt.col" + columnNumber + " is not null)) and cdt.moduleId="+module+" group by mt.id,cdt.col"+columnNumber;
                        }
                        stmtquery = conn.prepareStatement(mainTableQuery);
                        ResultSet mainTableRst = stmtquery.executeQuery();

                        while (mainTableRst.next()) {
                            String query = "";
                            String maintableid = isSameLineAndGlobalTableUsed ? mainTableRst.getString(customgolbaltableid) : mainTableRst.getString(linetableid);
                            if (isSameLineAndGlobalTableUsed) {
                                /*
                                 if Same table used for Global and lineLevel Fields
                                 */
                                query = "select cgt.col" + columnNumber + " from " + customgolbaltable + " cgt where " + customgolbaltableid + "='" + maintableid + "' ";

                            } else {

                                query = "select cdt.col" + columnNumber + " from " + customdetailTable + " cdt inner join " + linetable + " dt on cdt." + DetailTableid + "=dt." + linetableid
                                        + " inner join " + maintable + " mt on mt." + linetableid + "=dt." + reftomaintable
                                        + " where mt." + linetableid + "='" + maintableid + "' and ((cdt.col" + columnNumber + " <> '') and (  cdt.col" + columnNumber + " is not null))"
                                        + " group by cdt.col" + columnNumber + " ";
                            }
//                            isSameLineAndGlobalTableUsed=false;
                            stmtquery = conn.prepareStatement(query);
                            ResultSet allRecords = stmtquery.executeQuery();
                            int value = 0;
                            String uniqueFieldCombo = "";
                            while (allRecords.next()) {
//                        value=allRecords.getString("count");                       
                                value++;
                                uniqueFieldCombo = allRecords.getString("col" + columnNumber);
                            }
                            if (value == 1) {

                                String newColNumber = "";
                                String newFieldParamId = "";
                                String fieldParamsQuery = "select id,colnum from fieldparams where fieldlabel='" + newFieldLabel + "' and companyid='" + company + "' and moduleId=" + module + " and customcolumn=0";
                                stmtquery = conn.prepareStatement(fieldParamsQuery);
                                ResultSet existingFieldParam = stmtquery.executeQuery();
                                if (existingFieldParam.next()) {
                                    String fieldComboQuery = "SELECT `value` FROM fieldcombodata WHERE id='" + uniqueFieldCombo + "'";
                                    stmtquery = conn.prepareStatement(fieldComboQuery);
                                    ResultSet fieldComboresult = stmtquery.executeQuery();
                                    if (fieldComboresult.next()) {
                                        String usedValue = fieldComboresult.getString("value");
                                        newFieldParamId = existingFieldParam.getString("id");
                                        String fieldComboParamsQuery = "SELECT fcd.id FROM fieldcombodata fcd INNER JOIN fieldparams fp on fp.id=fcd.fieldid WHERE fp.id='" + newFieldParamId + "' AND fcd.`value`='" + usedValue + "'";
                                        stmtquery = conn.prepareStatement(fieldComboParamsQuery);
                                        ResultSet newFieldParam = stmtquery.executeQuery();
                                        if (newFieldParam.next()) {
                                            newuniqueFieldCombo = newFieldParam.getString("id");
                                        }
                                    }
                                    newColNumber = existingFieldParam.getString("colnum");
                                }

                                String q = "select " + customgolbaltableid + ",col" + newColNumber + " from " + customgolbaltable + " where " + customgolbaltableid + " =?";
                                stmtquery = conn.prepareStatement(q);
                                stmtquery.setString(1, maintableid);
                                ResultSet globalcustomtabledata = stmtquery.executeQuery();
                                if (!globalcustomtabledata.next()) {
                                    // insert data into global custom table

                                    String query5 = "insert into " + customgolbaltable + " (" + customgolbaltableid + ",company,moduleId,col" + newColNumber + ") values(?,?,?,?)";
                                    stmtquery = conn.prepareStatement(query5);
                                    stmtquery.setString(1, maintableid);
                                    stmtquery.setString(2, company);
                                    stmtquery.setInt(3, module);
                                    stmtquery.setString(4, newuniqueFieldCombo);
                                    stmtquery.executeUpdate();
                                    count++;
                                    String q2 = "update " + maintable + " set " + refkey + " =? where " + linetableid + "=?";
                                    stmtquery = conn.prepareStatement(q2);
                                    stmtquery.setString(1, maintableid);
                                    stmtquery.setString(2, maintableid);
                                    stmtquery.executeUpdate();
                                    count++;
                                } else {
                                    String val = globalcustomtabledata.getString("col" + newColNumber);
                                    if (StringUtil.isNullOrEmpty(val)) {
                                        String q2 = "update " + customgolbaltable + " set col" + newColNumber + " =? where " + customgolbaltableid + "=?";
                                        stmtquery = conn.prepareStatement(q2);
                                        stmtquery.setString(1, newuniqueFieldCombo);
                                        stmtquery.setString(2, maintableid);
                                        stmtquery.executeUpdate();
                                        count++;
                                    }
                                }

                            }
                        }

                        message = "<br>Module: " + module + "&nbsp;&nbsp;&nbsp; Assigned value for "+newFieldLabel+" for : " + count + " Records <br>";
                        out.print(message);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    out.print(e.toString());
                    out.print("Error Ocurred For ModuleId : - " + ModuleID);
                }
            isSameLineAndGlobalTableUsed=false;
        }
    }catch (MySQLSyntaxErrorException e) {
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