
<%@page import="com.krawler.utils.json.base.JSONException"%>
<%@page import="java.text.ParseException"%>
<%@page import="com.krawler.esp.utils.ConfigReader"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.utils.json.base.JSONArray"%>
<%@page import="com.krawler.common.util.Constants"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%--<%@page import="java.sql.Date"%>--%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<%!
%>
<%
    Connection con = null;
    try {
        //SCRIPT URL : http://<app-url>/DatesMigrationFromLongToDate.jsp?serverip=?&dbname=?&username=?&password=?    &subdomain=?

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        //String subdomain = "";//request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username)) {//|| StringUtil.isNullOrEmpty(password)
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        String errorOccured="";
        String driver = "com.mysql.jdbc.Driver";
        String query1 = "", companyid = "", subdomain = "";
        boolean isError = false;
        int totalCount = 0, updateCount = 0, companycount = 0;
        final int fieldId = 3;

        PreparedStatement DbCheckPstn = null, RBUpdatePstn = null, ReportBldrPstn = null, pst1 = null, relatedModulePstn = null, relatedModulePstn1 = null, pst2 = null, UsrTZpstn = null, cmpTZpstn = null, pst3 = null, pst4 = null, finalPstn = null, columnPstn = null;
        ResultSet DbCheckRst = null, RBUpdateRst = null, ReportBldrRst = null, rst1 = null, rst2 = null, relatedModulerst = null, relatedModulerst1 = null, rst3 = null, UsrTZrst = null, cmpTZrst = null, cprst = null, columnRst = null;
        PreparedStatement expensePODetailpstn = null, openingBopeningPstn = null;
        Class.forName(driver).newInstance();
        //SimpleDateFormat sdf1 = new SimpleDateFormat("MMMM d, yyyy");
        DateFormat df = new SimpleDateFormat("MMMM d, yyyy");
        DateFormat df1 = new SimpleDateFormat("MMMM d, yyyy");
        //Execution Started :
        out.println("<br><br>Execution Started @ " + new java.util.Date() + "<br><br>");

        con = DriverManager.getConnection(connectString, username, password);
        String dbCheck = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE "
                + "TABLE_SCHEMA = '" + dbname + "' AND TABLE_NAME = 'salesordercustomdata'"
                + " AND COLUMN_NAME = 'col3001'";
        DbCheckPstn = con.prepareStatement(dbCheck);
        DbCheckRst = DbCheckPstn.executeQuery();
        if (!DbCheckRst.next()) {
            throw new Exception("Please Execute DB Changes First  ");
        }

        File dateFile = new File(ConfigReader.getinstance().get("DocStorePath0") + "DateFile.txt");
        dateFile.createNewFile();
        FileOutputStream is = new FileOutputStream(dateFile);
        OutputStreamWriter osw = new OutputStreamWriter(is);
        Writer w = new BufferedWriter(osw);
        String fileContent = "";

        query1 = "SELECT companyid,subdomain FROM company";
        pst1 = con.prepareStatement(query1);
        rst1 = pst1.executeQuery();

        //Boolean a=true;
        while (rst1.next()) {
            //a=false;
            companyid = rst1.getString("companyid");//"12345b0z-b12w-1003-986d-002e670e7712";
            subdomain = rst1.getString("subdomain");
//            if (companyid.equalsIgnoreCase("cb9385f6-ccec-4851-8bed-0dc8c25a9e3a")) {
//                out.println("Caught....<br/>");
//            }
            String comp = "======" + companyid + "===" + subdomain + "======";
            w.write(comp + "\n");
            String timeZone = null;
//            String TimeZN = "Select c.subdomain,c.timezone AS Company_TimezoneID, u.fname AS Creator_Name, "
//                    + " u.timeZone AS User_Timezone, t.name AS User_TZName, t.difference AS User_TZDifference "
//                    + " from users u INNER JOIN company c ON u.userid=c.creator INNER JOIN timezone t "
//                    + " ON t.timezoneid=u.timeZone WHERE c.companyid='" + companyid + "' ";
//            UsrTZpstn = con.prepareStatement(TimeZN);
//            UsrTZrst = UsrTZpstn.executeQuery();
//            if (UsrTZrst.next()) {
//                if (UsrTZrst.getString("User_TZDifference") != null) {
//                    timeZone = UsrTZrst.getString("User_TZDifference");
//                }
//            }
//            if (timeZone == null) {
//
//                String companyTimeZone = "SELECT c.timezone AS companyTZ,t.difference  AS Company_TZDifference FROM "
//                        + " company c INNER JOIN timezone t ON t.timezoneid=c.timeZone "
//                        + " WHERE c.companyid='" + companyid + "' ";
//                cmpTZpstn = con.prepareStatement(companyTimeZone);
//                cmpTZrst = cmpTZpstn.executeQuery();
//                if (cmpTZrst.next()) {
//                    if (cmpTZrst.getString("Company_TZDifference") != null) {
//                        timeZone = cmpTZrst.getString("Company_TZDifference");
//                    } else {
//                        timeZone = "+08:00";
//                    }
//
//                } else {
//                    timeZone = "+08:00";
//                }
//
//            }

            String query2 = "select id,moduleid,relatedmoduleid,colnum,customcolumn,defaultvalue,isessential FROM fieldparams"
                    + " WHERE companyid='" + companyid + "' AND fieldtype='3' ";

            pst2 = con.prepareStatement(query2);
            rst2 = pst2.executeQuery();

            while (rst2.next()) {

                String fieldParamId = rst2.getString("id");
                String columnNum = rst2.getString("colnum");
                //System.out.println(rst2.getString("colnum"));
                String column = "col" + columnNum;
                int moduleId = rst2.getInt("moduleid");
                String customgolbaltable = "";
                String customdetailTable = "";
                String customgolbaltableid = "";
                String DetailTableid = "";
                String openingBalanceTable = "";
                String openingBalanceTableID = "";
                String versionCustomTable = "";
                String versionCustmTableID = "";
                String versionDetailCustomTable = "";
                String versionDetailCustomTableID = "";
                String expensepoDetailCustomTable = "expensepodetailcustomdata";
                String expensepoDetailCustomTableID = "expensepodetailid";

                boolean isCustomcolumn = rst2.getInt("customcolumn") == 1 ? true : false;
                ArrayList<Integer> modules = new ArrayList();
                // String colInCustTable=getNextDateColumn(companyid,moduleId);
                String nextColumnQuery = "SELECT id,fieldlabel,moduleid,MAX(colnum) as colnum FROM fieldparams WHERE companyid='" + companyid + "' "
                        + " AND fieldtype='3' AND moduleid='" + moduleId + "' AND colnum>=3001 AND colnum<=3010";
                columnPstn = con.prepareStatement(nextColumnQuery);
                System.out.println(moduleId);
                columnRst = columnPstn.executeQuery();

                int row = 3001;
                if (columnRst.next()) {
                    if (columnRst.getInt("colnum") != 0) {
                        row = columnRst.getInt("colnum");
                        row = row + 1;
                        if (row == 3011) {

                            try {

                                fileContent = "" + companyid + "  "
                                        + columnRst.getString("id") + "  "
                                        + columnRst.getString("fieldlabel") + "  "
                                        + columnRst.getInt("moduleid") + "\n";

                                w.write(fileContent);

                            } catch (IOException e) {
                                System.err.println("Problem writing to the file statsTest.txt");
                            }
                            continue;

                        }
                        //System.out.println(row);
                    }
                }
                String colInFieldParams = "" + row;
                String colInCustTable = "col" + row;
//                lt.remove();
                switch (moduleId) {
                    case 27:
                    case 41:
                    case 51:
                    case 67:

                        DetailTableid = "dodetailsid";          //id in customdetail table
                        customgolbaltable = "deliveryordercustomdata";
                        customgolbaltableid = "deliveryOrderId";
                        customdetailTable = "dodetailscustomdata";
                        //                    customdetailTableid="dodetailsid";
                        break;
                    case 28:
                    case 40:
                    case 57:
                        DetailTableid = "grodetailsid";
                        customgolbaltable = "grordercustomdata";
                        customgolbaltableid = "goodsreceiptorderid";
                        customdetailTable = "grodetailscustomdata";
                        //                    customdetailTableid="grodetailsid";
                        break;
                    case 18:        //PO
                    case 63:
                    case 90:

                        DetailTableid = "poDetailID";
                        customgolbaltable = "purchaseordercustomdata";
                        customgolbaltableid = "poID";
                        customdetailTable = "purchaseorderdetailcustomdata";
                        //                    customdetailTableid="grodetailsid";

                        break;
                    case 20:            //SO
                    case 36:            //LO
                    case 50:
                    case 1114:

                        DetailTableid = "soDetailID";
                        customgolbaltable = "salesordercustomdata";
                        customgolbaltableid = "soID";
                        customdetailTable = "salesorderdetailcustomdata";
                        //                    customdetailTableid="grodetailsid";
                        break;
                    case 23:        //VQ
                    case 89:

                        customgolbaltable = "vendorquotationcustomdata";
                        customgolbaltableid = "vendorquotationid";

                        customdetailTable = "vendorquotationdetailscustomdata";
                        DetailTableid = "vendorquotationdetailsid";

                        versionCustomTable = "vendorquotationversioncustomdata";
                        versionCustmTableID = "quotationid";

                        versionDetailCustomTable = "vendorquotationversiondetailscustomdata";
                        versionDetailCustomTableID = "quotationdetailsid";
                        //                    customdetailTableid="grodetailsid";

                        break;
                    case 22:        //Cq
                    case 65:

                        DetailTableid = "quotationdetailsid";
                        customgolbaltable = "quotationcustomdata";
                        customgolbaltableid = "quotationid";
                        customdetailTable = "quotationdetailscustomdata";

                        versionCustomTable = "quotationversioncustomdata";
                        versionCustmTableID = "quotationid";

                        versionDetailCustomTable = "quotationversiondetailscustomdata";
                        versionDetailCustomTableID = "quotationdetailsid";

                        break;

                    case 29:        //SR
                    case 53:
                    case 68:
                    case 98:
                        DetailTableid = "srdetailsid";
                        customgolbaltable = "salesreturncustomdata";
                        customgolbaltableid = "salesreturnid";
                        customdetailTable = "srdetailscustomdata";
                        //                    customdetailTableid="grodetailsid";
                        break;

                    case 31:        //PR
                    case 59:
                    case 96:

                        DetailTableid = "prdetailsid";
                        customgolbaltable = "purchasereturncustomdata";
                        customgolbaltableid = "purchasereturnid";
                        customdetailTable = "prdetailscustomdata";
                        //                    customdetailTableid="grodetailsid";

                        break;
                    case 32:        //PRqui
                    case 87:

                        DetailTableid = "purchaserequisitiondetailid";
                        customgolbaltable = "purchaserequisitioncustomdata";
                        customgolbaltableid = "purchaserequisitionid";
                        customdetailTable = "purchaserequisitiondetailcustomdata";
                        //                    customdetailTableid="grodetailsid";

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
                        openingBalanceTable = "openingbalancevendorinvoicecustomdata";
                        openingBalanceTableID = "openingbalancevendorinvoiceid";
                        break;
                    case 2:     //CI
                    case 38:     //CI
                    case 52:
                        customgolbaltable = "accjecustomdata";
                        customgolbaltableid = "journalentryId";
                        customdetailTable = "accjedetailcustomdata";
                        DetailTableid = "jedetailId";
                        openingBalanceTable = "openingbalanceinvoicecustomdata";
                        openingBalanceTableID = "openingbalanceinvoiceid";
                        break;
                    case 14:    //MP
                        customgolbaltable = "accjecustomdata";
                        customgolbaltableid = "journalentryId";
                        customdetailTable = "accjedetailcustomdata";
                        DetailTableid = "jedetailId";
                        openingBalanceTable = "openingbalancemakepaymentcustomdata";
                        openingBalanceTableID = "openingbalancemakepaymentid";
                        break;
                    case 16:        //RP
                        customgolbaltable = "accjecustomdata";
                        customgolbaltableid = "journalentryId";
                        customdetailTable = "accjedetailcustomdata";
                        DetailTableid = "jedetailId";
                        openingBalanceTable = "openingbalancereceiptcustomdata";
                        openingBalanceTableID = "openingbalancereceiptid";

                        break;
                    case 10:        //DN
                        customgolbaltable = "accjecustomdata";
                        customgolbaltableid = "journalentryId";
                        customdetailTable = "accjedetailcustomdata";
                        DetailTableid = "jedetailId";
                        openingBalanceTable = "openingbalancedebitnotecustomdata";
                        openingBalanceTableID = "openingbalancedebitnoteid";
                        break;
                    case 12:        //CN
                    case 93:
                        customgolbaltable = "accjecustomdata";
                        customgolbaltableid = "journalentryId";
                        customdetailTable = "accjedetailcustomdata";
                        DetailTableid = "jedetailId";
                        openingBalanceTable = "openingbalancecreditnotecustomdata";
                        openingBalanceTableID = "openingbalancecreditnoteid";
                        break;

                    case 30:        //product
                    case 42:        //group
                        customgolbaltable = "accproductcustomdata";
                        customgolbaltableid = "productId";           //module with same table.
                        customdetailTable = "accproductcustomdata";
                        DetailTableid = "productId";
                        break;

                    case 34:        //account
                        customgolbaltable = "accountcustomdata";
                        customgolbaltableid = "accountId";
                        customdetailTable = "accountcustomdata";
                        DetailTableid = "accountId";
                        break;

                    case 24:        //JE
                        customgolbaltable = "accjecustomdata";
                        customgolbaltableid = "journalentryId";
                        customdetailTable = "accjedetailcustomdata";
                        DetailTableid = "jedetailId";
                        break;
                    case 35:        //contract
                    case 64:
                        customgolbaltable = "contractcustomdata";
                        customgolbaltableid = "contractid";
                        customdetailTable = "contractdetailcustomdata";
                        DetailTableid = "scDetailID";
                        break;
                    case 25:        //Customer
                        customgolbaltable = "customercustomdata"; //module with same table.
                        customgolbaltableid = "customerId";
                        customdetailTable = "customercustomdata";
                        DetailTableid = "customerId";
                        break;
                    case 26:        //Vendor
                        customgolbaltable = "vendorcustomdata";
                        customgolbaltableid = "vendorId";        //module with same table.
                        customdetailTable = "vendorcustomdata";
                        DetailTableid = "vendorId";
                        break;
                    case 79:        //Serial Windowte = new java.sql.Date(convertedDate1.getTime());

                        customgolbaltable = "serialcustomdata";
                        customgolbaltableid = "serialdocumentmappingid";
                        customdetailTable = "serialcustomdata";     //module with same table.
                        DetailTableid = "serialdocumentmappingid";
                        break;
                    case 121:
                        customgolbaltable = "assetdetailcustomdata";
                        customgolbaltableid = "assetDetailsId";       //module with same table.
                        customdetailTable = "assetdetailcustomdata";
                        DetailTableid = "assetDetailsId";
                        break;
                    // ------------newly added cases---
                    case 95:
                        customgolbaltable = "in_stockadjustment_customdata";
                        customgolbaltableid = "stockadjustmentid";
                        customdetailTable = "in_stockadjustment_customdata";
                        DetailTableid = "stockadjustmentid";
                        break;
                    case 1004:
                        customdetailTable = "cyclecountcustomdata";
                        DetailTableid = "ccid";
                        break;
                    case 1001:
                    case 92:
                        customgolbaltable = "stockcustomdata";
                        customgolbaltableid = "stockId";
                        customdetailTable = "stockcustomdata";
                        DetailTableid = "stockId";
                        break;
                    case 1002:
                    case 1003:
                        customgolbaltable = "in_interstoretransfer_customdata";
                        customgolbaltableid = "istid";
                        customdetailTable = "in_interstoretransfer_customdata";
                        DetailTableid = "istid";
                        break;

                    case 1101:
                        customgolbaltable = "labourcustomdata";
                        customgolbaltableid = "labourId";
                        customdetailTable = "labourcustomdata";
                        DetailTableid = "labourId";
                        break;
                    case 1102:
                        customgolbaltable = "workcentrecustomdata";
                        customgolbaltableid = "workCentreId";
                        customdetailTable = "workcentrecustomdata";
                        DetailTableid = "workCentreId";
                        break;
                    case 1103:
                        customgolbaltable = "machinecustomdata";
                        customgolbaltableid = "machineId";
                        customdetailTable = "machinecustomdata";
                        DetailTableid = "machineId";
                        break;
                    case 1105:
                        customgolbaltable = "workordercustomdata";
                        customgolbaltableid = "workOrderId";
                        customdetailTable = "workordercustomdata";
                        DetailTableid = "workOrderId";
                        break;
                    case 1106:
                        customgolbaltable = "mrpcontractcustomdata";
                        customgolbaltableid = "contractId";
                        customdetailTable = "mrpcontractdetailscustomdata";
                        DetailTableid = "contractDetailsId";
                        break;
                    case 1107:
                        customgolbaltable = "routingtemplatecustomdata";
                        customgolbaltableid = "routingTemplateId";
                        customdetailTable = "routingtemplatecustomdata";
                        DetailTableid = "routingTemplateId";
                        break;
                    case 1104:
                        customgolbaltable = "jobworkcustomdata";
                        customgolbaltableid = "jobworkId";
                        customdetailTable = "jobworkcustomdata";
                        DetailTableid = "jobworkId";
                    case 1116:
                        customgolbaltable = "securitygateentrycustomdata";
                        customgolbaltableid = "sgeid";
                        customdetailTable = "securitygateentrydetailcustomdata";
                        DetailTableid = "sgeDetailID";

                }
      
//================================Migration For FieldParams=============================================================================                     
                int isMandetory = rst2.getInt("isessential");
                String dateInString = "";
                String fieldParamUpdate="";
                Long longDate =null;
                
                if (isMandetory == 1) {

                        if (rst2.getString("defaultvalue") != null && rst2.getString("defaultvalue") != "") {
                            try {
                                String milliseconds = rst2.getString("defaultvalue");
                                longDate = Long.parseLong(milliseconds);
                                dateInString = df.format(new java.util.Date(longDate));
                            } catch (Exception e) {
                                errorOccured = e.toString();
                                fileContent = "error " + errorOccured + " For " + fieldParamId + " fieldparams " + moduleId + " transactions for this field will not be migrated \n";
                                w.write(fileContent);
                                continue;
                            }

                        }
                    }
                try {
                        if (dateInString != "") {
                            fieldParamUpdate = "UPDATE fieldparams set colnum='" + colInFieldParams + "' , "
                                    + "defaultvalue= '" + dateInString + "' where id= '" + fieldParamId + "' ";
                        } else {
                            fieldParamUpdate = "UPDATE fieldparams set colnum='" + colInFieldParams + "' where id= '" + fieldParamId + "' ";
                        }

                        finalPstn = con.prepareStatement(fieldParamUpdate);
                        int i = finalPstn.executeUpdate();
                    } catch (SQLException e) {
                        errorOccured = e.toString();
                        fileContent = "error " + errorOccured + " For " + fieldParamId + " fieldparams " + moduleId + " transactions for this field will not be migrated \n";
                        w.write(fileContent);
                        continue;
                    } catch (Exception e) {
                        errorOccured = e.toString();
                        fileContent = "error " + errorOccured + " For " + fieldParamId + " fieldparams " + moduleId + " transactions for this field will not be migrated \n";
                        w.write(fileContent);
                        continue;
                    }
                   
                
//   -------------------This Code Handles Deatil-Product-Custom-Tables Data Migration------------
                if (moduleId == 30) {
                    String relatedModuleIdList = rst2.getString("relatedmoduleid");
                    String relatedModuleQuery = null;
                    String relatedModuleSelectQuery = null;
                    String tableName = null;
                    String primaryKey1 = null;
                    if (!StringUtil.isNullOrEmpty(relatedModuleIdList)) {

                        String relatedModuleIdArray[] = relatedModuleIdList.split(",");
                        for (String module : relatedModuleIdArray) {

                            int detailTableModuleId = Integer.parseInt(module);
                            String detailProductCustomTableDeatils[] = getDetailProductCustomTable(detailTableModuleId);
                            tableName = detailProductCustomTableDeatils[0];
                            if (StringUtil.isNullOrEmpty(tableName)) {
                                continue;
                            }

                            primaryKey1 = detailProductCustomTableDeatils[1];
                            relatedModuleSelectQuery = "SELECT " + primaryKey1 + "," + column + " FROM " + tableName + " WHERE company='" + companyid + "' "
                                    + " AND moduleId='" + detailTableModuleId + "' ";
                            relatedModulePstn = con.prepareStatement(relatedModuleSelectQuery);
                            relatedModulerst = relatedModulePstn.executeQuery();
                            while (relatedModulerst.next()) {
                                java.sql.Date convertedDate = null;
                                String primaryKeyValue = "";
                                if (!StringUtil.isNullOrEmpty(relatedModulerst.getString(column))) {
                                    try {
                                        primaryKeyValue = relatedModulerst.getString(primaryKey1);
                                        longDate = Long.parseLong(relatedModulerst.getString(column));
                                        //Date convertedDate1 = new Date(longDate);
                                        //df.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
                                        //String dateWithTZ = df.format(new Date(longDate));
                                        //Date convertedDate1 = df1.parse(dateWithTZ);//new Date(longDate);
                                        convertedDate = new java.sql.Date(longDate);
                                        
                                    }catch (Exception e) {
                                        errorOccured=e.toString();
                                        fileContent = "error "+errorOccured+" For " + fieldParamId + "in table  "+tableName +" key:- " + primaryKeyValue + ""+ moduleId +"  \n";
                                        w.write(fileContent);
                                    }
                                    try {
                                            if (longDate != null && convertedDate != null) {
                                                relatedModuleQuery = "UPDATE " + tableName + " SET " + colInCustTable + " = '" + convertedDate + "'," + column + "= '' "
                                                        + " WHERE " + primaryKey1 + "= '" + primaryKeyValue + "'";
                                            } else {
                                                relatedModuleQuery = "UPDATE " + tableName + " SET " + column + "= '' "
                                                        + " WHERE " + primaryKey1 + "= '" + primaryKeyValue + "'";

                                            }

                                            relatedModulePstn1 = con.prepareStatement(relatedModuleQuery);
                                            int i = relatedModulePstn1.executeUpdate();
                                            System.out.println(tableName + " updated");
                                        } catch (Exception e) {
                                            errorOccured = e.toString();
                                            fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + tableName + "  key:- " + primaryKeyValue + "" + moduleId + "  \n";
                                            w.write(fileContent);
                                        }

                                }

                            }

                        }

                    }
                }
//------------------------------------------------------------------------------------------------                

                if (!isCustomcolumn) {
                    if (StringUtil.isNullOrEmpty(customgolbaltable)) {
                        System.out.println(" Handles Deatil-Product-Custom-Tables");
                    }

                    String query3 = "select " + customgolbaltableid + "," + column + "  FROM " + customgolbaltable + " "
                            + " WHERE company='" + companyid + "' and moduleId='" + moduleId + "'  ";
                    pst3 = con.prepareStatement(query3);
                    rst3 = pst3.executeQuery();
                    while (rst3.next()) {
                        java.sql.Date convertedDate = null;
                        String primaryKey="";
                        String updateQuery="";
                        if (!StringUtil.isNullOrEmpty(rst3.getString(column))) {

                                try {
                                    primaryKey = rst3.getString(customgolbaltableid);
                                    longDate = Long.parseLong(rst3.getString(column));
                                    //Date convertedDate1 = new Date(longDate);
                                    // df.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
                                    //String dateWithTZ = df.format(new Date(longDate));
                                    // Date convertedDate1 = df1.parse(dateWithTZ);//new Date(longDate);
                                    convertedDate = new java.sql.Date(longDate);
                                    

                               } catch (Exception e) {
                                    errorOccured = e.toString();
                                    fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + customgolbaltable + " key:- " + primaryKey + " " + moduleId + "  \n";
                                    w.write(fileContent);
                                }
                                try {
                                    if (longDate != null  && convertedDate != null) {
                                        updateQuery = "update " + customgolbaltable + " set " + colInCustTable + "= '" + convertedDate + "', " + column + " = '' "
                                        + " where " + customgolbaltableid + " = '" + primaryKey + "' ";
                                    } else {
                                        updateQuery = "update " + customgolbaltable + " set " + column + " = '' "
                                        + " where " + customgolbaltableid + " = '" + primaryKey + "' ";
                                    }

                                    pst4 = con.prepareStatement(updateQuery);
                                    int i = pst4.executeUpdate();
                                    System.out.println(customgolbaltable + " updated");
                                } catch (Exception e) {
                                    errorOccured = e.toString();
                                    fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + customgolbaltable + " key:- " + primaryKey + " " + moduleId + "  \n";
                                    w.write(fileContent);
                                }

                            }

                    }

                }
//================================================================================================================                   

                if (isCustomcolumn) {

                    if (StringUtil.isNullOrEmpty(customdetailTable)) {
                        System.out.println(" Handles Deatil-Product-Custom-Tables");
                    }
                    String query3 = "select " + DetailTableid + "," + column + " FROM " + customdetailTable + " "
                            + " WHERE company='" + companyid + "' and moduleId='" + moduleId + "' ";
                    pst3 = con.prepareStatement(query3);
                    rst3 = pst3.executeQuery();
                    while (rst3.next()) {

                        java.sql.Date convertedDate = null;
                         String primaryKey="";
                         String updateQuery="";
                        if (!StringUtil.isNullOrEmpty(rst3.getString(column))) {
                                try {
                                    primaryKey = rst3.getString(DetailTableid);
                                    longDate = Long.parseLong(rst3.getString(column));
                                //Date convertedDate1 = new Date(longDate);
                                    // df.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
                                    //String dateWithTZ = df.format(new Date(longDate));
                                    //Date convertedDate1 = df1.parse(dateWithTZ);  //new Date(longDate);
                                    convertedDate = new java.sql.Date(longDate);
                                    
                                    
                                } catch (Exception e) {
                                    errorOccured = e.toString();
                                    fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + customdetailTable + " key:- " + primaryKey + " " + moduleId + "  \n";
                                    w.write(fileContent);
                                }
                                try {
                                    if (longDate != null  && convertedDate != null) {
                                        updateQuery = "update " + customdetailTable + " set " + colInCustTable + "='" + convertedDate + "'," + column + "= '' "
                                        + " where " + DetailTableid + " = '" + primaryKey + "' ";

                                    } else {
                                        updateQuery = "update " + customdetailTable + " set " + column + "= '' "
                                        + " where " + DetailTableid + " = '" + primaryKey + "' ";
                                    }
                                    pst4 = con.prepareStatement(updateQuery);
                                    int i = pst4.executeUpdate();
                                    System.out.println(customdetailTable + " updated");
                                } catch (Exception e) {
                                    errorOccured = e.toString();
                                    fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + customdetailTable + " key:- " + primaryKey + "  " + moduleId + "  \n";
                                    w.write(fileContent);
                                }

                            }
                    }

                }
//======================Expense PO Detail Table==========================================================

                if (moduleId == 18) {

                    if (StringUtil.isNullOrEmpty(expensepoDetailCustomTableID)) {
                        System.out.println(" Handles Deatil-Product-Custom-Tables");
                    }

                    String query3 = "select " + expensepoDetailCustomTableID + "," + column + " FROM " + expensepoDetailCustomTable + " "
                            + " WHERE company='" + companyid + "' and moduleId='" + moduleId + "' ";
                    pst3 = con.prepareStatement(query3);
                    rst3 = pst3.executeQuery();
                    while (rst3.next()) {

                        java.sql.Date convertedDate = null;
                        String primaryKey="";
                        String updateQuery="";
                        if (!StringUtil.isNullOrEmpty(rst3.getString(column))) {
                                try {
                                    primaryKey = rst3.getString(expensepoDetailCustomTableID);
                                    longDate = Long.parseLong(rst3.getString(column));
                                //Date convertedDate1 = new Date(longDate);
                                    //df.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
                                    // String dateWithTZ = df.format(new Date(longDate));
                                    // Date convertedDate1 = df1.parse(dateWithTZ);  //new Date(longDate);
                                    convertedDate = new java.sql.Date(longDate);
                                } catch (Exception e) {
                                    errorOccured = e.toString();
                                    fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + expensepoDetailCustomTable + " key:- "+primaryKey+" " + moduleId + "  \n";
                                    w.write(fileContent);
                                }

                                try {
                                    if (longDate != null && convertedDate != null) {
                                        updateQuery = "update " + expensepoDetailCustomTable + " set " + colInCustTable + "='" + convertedDate + "'," + column + "= '' "
                                        + " where " + expensepoDetailCustomTableID + " = '" + primaryKey + "' ";
                                    } else {
                                        updateQuery = "update " + expensepoDetailCustomTable + " set " + column + "= '' "
                                        + " where " + expensepoDetailCustomTableID + " = '" + primaryKey + "' ";
                                    }

                                    expensePODetailpstn = con.prepareStatement(updateQuery);
                                    int i = expensePODetailpstn.executeUpdate();
                                    System.out.println(expensepoDetailCustomTable + " updated");
                                } catch (Exception e) {
                                    errorOccured = e.toString();
                                    fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + expensepoDetailCustomTable + " key:- "+primaryKey+" " + moduleId + "  \n";
                                    w.write(fileContent);
                                }

                            }
                    }
                }
//=======================Opening Balance Tables=====================================================                    

                if (openingBalanceTable != "") {

                    String query3 = "select " + openingBalanceTableID + "," + column + " FROM " + openingBalanceTable + " "
                            + " WHERE company='" + companyid + "' and moduleId='" + moduleId + "' ";
                    pst3 = con.prepareStatement(query3);
                    rst3 = pst3.executeQuery();
                    while (rst3.next()) {

                        java.sql.Date convertedDate = null;
                        String primaryKey="";
                        String updateQuery="";
                        if (!StringUtil.isNullOrEmpty(rst3.getString(column))) {

                                try {
                                    
                                    primaryKey = rst3.getString(openingBalanceTableID);
                                    longDate = Long.parseLong(rst3.getString(column));
                                    //Date convertedDate1 = new Date(longDate);
                                    //df.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
                                    //String dateWithTZ = df.format(new Date(longDate));
                                    // Date convertedDate1 = df1.parse(dateWithTZ);  //new Date(longDate);
                                    convertedDate = new java.sql.Date(longDate);
                                    
                                    
                                } catch (Exception e) {
                                    errorOccured = e.toString();
                                    fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + openingBalanceTable + " key:- " + primaryKey + "  " + moduleId + "  \n";
                                    w.write(fileContent);
                                }
                                try {
                                    if (longDate != null && convertedDate != null) {
                                        updateQuery = "update " + openingBalanceTable + " set " + colInCustTable + "='" + convertedDate + "'," + column + "= '' "
                                            + " where " + openingBalanceTableID + " = '" + primaryKey + "' ";
                                    } else {
                                        updateQuery = "update " + openingBalanceTable + " set " + column + "= '' "
                                            + " where " + openingBalanceTableID + " = '" + primaryKey + "' ";
                                    }
                                    openingBopeningPstn = con.prepareStatement(updateQuery);
                                    int i = openingBopeningPstn.executeUpdate();
                                    System.out.println(openingBalanceTable + " updated");
                                } catch (Exception e) {
                                    errorOccured = e.toString();
                                    fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + openingBalanceTable + " key:- " + primaryKey + "  " + moduleId + "  \n";
                                    w.write(fileContent);

                                }

                            }
                    }

                }
//==================================Version Custom Table========================================================
                if (versionCustomTable != "") {

                    String query3 = "select " + versionCustmTableID + "," + column + " FROM " + versionCustomTable + " "
                            + " WHERE company='" + companyid + "' and moduleId='" + moduleId + "' ";
                    pst3 = con.prepareStatement(query3);
                    rst3 = pst3.executeQuery();
                    while (rst3.next()) {

                        java.sql.Date convertedDate = null; 
                        String primaryKey="";
                        String updateQuery="";
                        
                        if (!StringUtil.isNullOrEmpty(rst3.getString(column))) {
                                try {
                                     primaryKey = rst3.getString(versionCustmTableID);
                                    longDate = Long.parseLong(rst3.getString(column));
                                //Date convertedDate1 = new Date(longDate);
                                    //df.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
                                    //String dateWithTZ = df.format(new Date(longDate));
                                    //Date convertedDate1 = df1.parse(dateWithTZ);  //new Date(longDate);
                                    convertedDate = new java.sql.Date(longDate);
                                   
                                
                                } catch (Exception e) {
                                    errorOccured = e.toString();
                                    fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + versionCustomTable + " key:- " + primaryKey + " " + moduleId + "  \n";
                                    w.write(fileContent);
                                }
                                try {
                                            if (longDate != null && convertedDate != null) {
                                                updateQuery = "update " + versionCustomTable + " set " + colInCustTable + "='" + convertedDate + "'," + column + "= '' "
                                                        + " where " + versionCustmTableID + " = '" + primaryKey + "' ";
                                            } else {
                                                updateQuery = "update " + versionCustomTable + " set " + column + "= '' "
                                                        + " where " + versionCustmTableID + " = '" + primaryKey + "' ";
                                            }
                                            pst4 = con.prepareStatement(updateQuery);
                                            int i = pst4.executeUpdate();
                                            System.out.println(versionCustomTable + " updated");
                                        } catch (Exception e) {
                                            errorOccured = e.toString();
                                            fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + versionCustomTable + " key:- " + primaryKey + " " + moduleId + "  \n";
                                            w.write(fileContent);
                                        }

                                    }
                    }
                }
//=============================================================================================================
//==================================Version Detail Custom Table================================================
                if (versionDetailCustomTable != "") {

                        String query3 = "SELECT " + versionDetailCustomTableID + "," + column + " FROM " + versionDetailCustomTable + " "
                                + " WHERE company='" + companyid + "' AND moduleId='" + moduleId + "' ";
                        pst3 = con.prepareStatement(query3);
                        rst3 = pst3.executeQuery();
                        while (rst3.next()) {

                            java.sql.Date convertedDate = null;
                            String primaryKey="";
                            String updateQuery="";
                            
                            if (!StringUtil.isNullOrEmpty(rst3.getString(column))) {
                                    try {
                                        primaryKey = rst3.getString(versionDetailCustomTableID);
                                        longDate = Long.parseLong(rst3.getString(column));
                                //Date convertedDate1 = new Date(longDate);
                                        // df.setTimeZone(TimeZone.getTimeZone("GMT" + timeZone));
                                        //String dateWithTZ = df.format(new Date(longDate));
                                        //Date convertedDate1 = df1.parse(dateWithTZ);  //new Date(longDate);
                                        convertedDate = new java.sql.Date(longDate);
                                        

                                    } catch (Exception e) {
                                        errorOccured = e.toString();
                                        fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + versionDetailCustomTable + " id:- " + primaryKey + " " + moduleId + "  \n";
                                        w.write(fileContent);

                                    }
                                    try {
                                        if (longDate != null && convertedDate != null) {
                                            updateQuery = "UPDATE " + versionDetailCustomTable + " SET " + colInCustTable + "='" + convertedDate + "'," + column + "= '' "
                                            + " WHERE " + versionDetailCustomTableID + " = '" + primaryKey + "' ";
                                        } else {
                                            updateQuery = "UPDATE " + versionDetailCustomTable + " SET " + column + "= '' "
                                            + " WHERE " + versionDetailCustomTableID + " = '" + primaryKey + "' ";
                                        }
                                        pst4 = con.prepareStatement(updateQuery);
                                        int i = pst4.executeUpdate();
                                        System.out.println(versionDetailCustomTable + " updated..");
                                    } catch (Exception e) {
                                        errorOccured = e.toString();
                                        fileContent = "error " + errorOccured + " For " + fieldParamId + "in table  " + versionDetailCustomTable + " id:- " + primaryKey + " " + moduleId + "  \n";
                                        w.write(fileContent);

                                    }

                                }
                        }
                    }

//======================================================================================
                //All Data for current company is migrated...Execution for REPORT BUILDER Starts here...
                String rptBldrQuery = "SELECT id,ispivotreport,reportjson FROM reportmaster WHERE companyid= '" + companyid + "' and isdefaultreport='F'";
                //String rptBldrQuery="SELECT id,ispivotreport,reportjson FROM reportmaster WHERE id='a05dcfb8-1da4-46ac-92b6-1d54fef8451e'";
                ReportBldrPstn = con.prepareStatement(rptBldrQuery);
                ReportBldrRst = ReportBldrPstn.executeQuery();
                String reportBuilderfieldId = "";
                String reportBuilderModuleId = "";
                JSONObject reportJobj = null;

                while (ReportBldrRst.next()) {
                        String reportJsonId = ReportBldrRst.getString("id");
                        String isPivot = ReportBldrRst.getString("ispivotreport");
                        JSONArray jsonArray = new JSONArray();
                        try {
                            if (isPivot.equals("T")) {
                                reportJobj = new JSONObject(ReportBldrRst.getString("reportjson").replaceAll("\"T\"", "'T'"));
                                if (reportJobj != null) {
                                    jsonArray = reportJobj.getJSONArray("columnConfig");
                                } else {
                                    continue;
                                }
                            } else {

                                jsonArray = new JSONArray(ReportBldrRst.getString("reportjson").replaceAll("\"T\"", "'T'"));
                                if (jsonArray == null) {
                                    continue;
                                }
                            }
                        } catch (Exception e) {
                            out.println("While Getting Data...");
                            out.println("\nReportID->" + reportJsonId);
                            out.println("\n");
                            e.printStackTrace();
                            fileContent = "error " + errorOccured + " For " + fieldParamId + "in Reportmaster " + moduleId + "  \n";
                            w.write(fileContent);
                            continue;
                        } 
                        JSONArray modiFiedJsonArray = new JSONArray();
                        Boolean isModified = false;
                        //JSONArray editedJsonObjects=new JSONArray();
                        for (int j = 0; j < jsonArray.length(); j++) {

                            JSONObject jObject = jsonArray.getJSONObject(j);
                            reportBuilderfieldId = jObject.getString("id");
                            //reportBuilderModuleId = jObject.getString("moduleName");

                            if (reportBuilderfieldId.equals(fieldParamId)) {

                                try {
                                        jObject.put("reftabledatacolumn", colInCustTable);//"new" column in custom table
                                        isModified = true;
                                        modiFiedJsonArray.put(jObject);
                                    } catch(JSONException jsonException){
                                        errorOccured.toString();
                                        fileContent = "error " + errorOccured + " For " + fieldParamId + "in ReportMaster" + moduleId + "  \n";
                                        w.write(fileContent);
                                    } catch (Exception e) {
                                        errorOccured.toString();
                                        out.println("While Changing Data..." + reportJsonId + "\n");
                                        fileContent = "error " + errorOccured + " For " + fieldParamId + "in ReportMaster" + moduleId + "  \n";
                                        w.write(fileContent);
                                    }
                                //jsonArray.remove(j);

                            } else {

                                modiFiedJsonArray.put(jObject);
                            }

                        }
                        if (isModified) {
                            try {
                                if (isPivot.equals("T")) {
                                    reportJobj.put("columnConfig", modiFiedJsonArray);
                                    String reportJson = reportJobj.toString();
                                    String UpdateRB = "UPDATE reportmaster set reportjson=? WHERE id= ? ";
                                    RBUpdatePstn = con.prepareStatement(UpdateRB);
                                    RBUpdatePstn.setString(1, reportJson);
                                    RBUpdatePstn.setString(2, reportJsonId);
                                int i1 = RBUpdatePstn.executeUpdate();
                                } else {

                                    String reportJson = modiFiedJsonArray.toString();
                                    String UpdateRB = "UPDATE reportmaster set reportjson=? WHERE id= ? ";
                                    RBUpdatePstn = con.prepareStatement(UpdateRB);
                                    RBUpdatePstn.setString(1, reportJson);
                                    RBUpdatePstn.setString(2, reportJsonId);

                                int i1 = RBUpdatePstn.executeUpdate();

                                }
                            } catch (Exception e) {
                                out.println("While Putting Data...");
                                out.println("\nReportID->" + reportJsonId + "\n");
                                e.printStackTrace();
                                errorOccured=e.toString();
                                fileContent = "error " + errorOccured + " For " + fieldParamId + "in Reportmaster while Migrating Data " + moduleId + "  \n";
                                w.write(fileContent);

                                continue;
                            }

                        }

                    }
                // System.out.println("Updated");

                }
                }
        w.close();

        System.out.println("Script Executed Successfully...\n");
        out.println("<b><center>Script Executed Successfully...</center><b><br>");
    } catch (Exception ex) {
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
    } finally {
        if (con != null) {
            try {
                con.close();
                out.println("Connection Closed....<br/>");
                //Execution Ended :
                out.println("<br><br>Execution Ended @ " + new java.util.Date() + "<br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally
%>
<%!    public String[] getDetailProductCustomTable(int moduleId) {

        String detailProductCustomTableDeatils[] = new String[2];
        switch (moduleId) {
            case 18:

                detailProductCustomTableDeatils[0] = "podetailproductcustomdata";
                detailProductCustomTableDeatils[1] = "poDetailID";
                break;

            case 22:
                detailProductCustomTableDeatils[0] = "cqdetailproductcustomdata";
                detailProductCustomTableDeatils[1] = "cqDetailID";
                break;

            case 27:
                detailProductCustomTableDeatils[0] = "dodetailproductcustomdata";
                detailProductCustomTableDeatils[1] = "doDetailID";
                break;

            case 28:
                detailProductCustomTableDeatils[0] = "grodetailproductcustomdata";
                detailProductCustomTableDeatils[1] = "grDetailID";
                break;

            case 31:
                detailProductCustomTableDeatils[0] = "prdetailproductcustomdata";
                detailProductCustomTableDeatils[1] = "prDetailID";
                break;
            case 29:
                detailProductCustomTableDeatils[0] = "srdetailproductcustomdata";
                detailProductCustomTableDeatils[1] = "srDetailID";
                break;
            case 23:
                detailProductCustomTableDeatils[0] = "vqdetailproductcustomdata";
                detailProductCustomTableDeatils[1] = "vqDetailID";
                break;
            case 20:
                detailProductCustomTableDeatils[0] = "sodetailproductcustomdata";
                detailProductCustomTableDeatils[1] = "soDetailID";
                break;
            case 2:
            case 6:
                detailProductCustomTableDeatils[0] = "accjedetailproductcustomdata";
                detailProductCustomTableDeatils[1] = "jedetailId";
                break;

        }

        return detailProductCustomTableDeatils;
    }

%>