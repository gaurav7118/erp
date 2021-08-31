

<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.common.util.Constants"%>

<%
    Connection conn = null;
    try {

        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        String dimension = request.getParameter("dimension");
        String value = request.getParameter("value");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(subDomain) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(dimension)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password,dimension) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery, fieldquery;
        String query = "";
        ResultSet rs;
        int count = 0, failedcount = 0, totalCount = 0;
        PreparedStatement preparedStatement = null;

        JSONObject fieldParamsMap = new JSONObject();

        String fieldParamsQuery = "select moduleid, colnum from fieldparams "
                + "INNER JOIN company on company.companyid = fieldparams.companyid and company.subdomain = ? "
                + "where moduleid in ('" + Constants.Acc_Invoice_ModuleId + "','" + Constants.Acc_Vendor_Invoice_ModuleId + "','" + Constants.Acc_Debit_Note_ModuleId + "','" + Constants.Acc_Credit_Note_ModuleId + "','" + Constants.Acc_Make_Payment_ModuleId + "','" + Constants.Acc_Receive_Payment_ModuleId + "') and fieldlabel = ? and isforknockoff = ?";
        fieldquery = conn.prepareStatement(fieldParamsQuery);
        fieldquery.setString(1, subDomain);
        fieldquery.setString(2, dimension);
        fieldquery.setInt(3, 1);
        rs = fieldquery.executeQuery();

        while (rs.next()) {
            String moduleid = rs.getString("moduleid");
            String colnum = rs.getString("colnum");
            fieldParamsMap.put(moduleid, colnum);
        }
        String siCust = "col" + fieldParamsMap.get(Constants.Acc_Invoice_ModuleId + "");
        String piCust = "col" + fieldParamsMap.get(Constants.Acc_Vendor_Invoice_ModuleId + "");
        String cnCust = "col" + fieldParamsMap.get(Constants.Acc_Credit_Note_ModuleId + "");
        String dnCust = "col" + fieldParamsMap.get(Constants.Acc_Debit_Note_ModuleId + "");
        String paymentCust = "col" + fieldParamsMap.get(Constants.Acc_Make_Payment_ModuleId + "");
        String receiptCust = "col" + fieldParamsMap.get(Constants.Acc_Receive_Payment_ModuleId + "");


        out.println("======================= Updating data for payment ========================<br>");

        fieldParamsQuery = "select id,`value` from fieldcombodata "
                + "where fieldid =(select id from fieldparams INNER JOIN company on company.companyid = fieldparams.companyid and company.subdomain = ? and fieldparams.fieldlabel = ? and fieldparams.moduleid = ? ) "
                + "ORDER BY `value`";

        fieldquery = conn.prepareStatement(fieldParamsQuery);
        fieldquery.setString(1, subDomain);
        fieldquery.setString(2, dimension);
        fieldquery.setInt(3, Constants.Acc_Make_Payment_ModuleId);
        rs = fieldquery.executeQuery();
        JSONObject fieldValueMap = new JSONObject();

        while (rs.next()) {
            String id = rs.getString("id");
            String fieldValue = rs.getString("value");
            fieldValueMap.put(fieldValue, id);
        }
        Iterator<String> keys = fieldValueMap.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (fieldValueMap.has(key) && fieldValueMap.getString(key) != null) {

                query = "update accjedetailcustomdata jedcust set jedcust." + paymentCust + " = ? "
                        + "where  jedcust.moduleId = ?  "
                        + "and jedcust.company = (select companyid from company where subdomain = ?) "
                        + "and jedcust." + paymentCust + " = (select id from fieldcombodata where value =  ? and fieldid = (select id from fieldparams where fieldparams.companyid =  jedcust.company and fieldparams.fieldlabel = ? and fieldparams.moduleid = ? ))";

                preparedStatement = conn.prepareStatement(query);

                preparedStatement.setString(1, fieldValueMap.getString(key));
                preparedStatement.setInt(2, Constants.Acc_Make_Payment_ModuleId);
                preparedStatement.setString(3, subDomain);
                preparedStatement.setString(4, key);
                preparedStatement.setString(5, dimension);
                preparedStatement.setInt(6, Constants.Acc_Vendor_Invoice_ModuleId);

                count = preparedStatement.executeUpdate();

                out.println("Payment jedetails updated for " + key + " : " + count + "<br>");

            }

        }

        count = 0;
        failedcount = 0;
        preparedStatement = null;


        out.println("======================= Updating data for Receipt ========================<br>");

        fieldParamsQuery = "select id,`value` from fieldcombodata "
                + "where fieldid =(select id from fieldparams INNER JOIN company on company.companyid = fieldparams.companyid and company.subdomain = ? and fieldparams.fieldlabel = ? and fieldparams.moduleid = ? ) "
                + "ORDER BY `value`";

        fieldquery = conn.prepareStatement(fieldParamsQuery);
        fieldquery.setString(1, subDomain);
        fieldquery.setString(2, dimension);
        fieldquery.setInt(3, Constants.Acc_Receive_Payment_ModuleId);
        rs = fieldquery.executeQuery();
        fieldValueMap = new JSONObject();

        while (rs.next()) {
            String id = rs.getString("id");
            String fieldValue = rs.getString("value");
            fieldValueMap.put(fieldValue, id);
        }
        keys = fieldValueMap.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (fieldValueMap.has(key) && fieldValueMap.getString(key) != null) {

                query = "update accjedetailcustomdata jedcust set jedcust." + receiptCust + " = ? "
                        + "where  jedcust.moduleId = ?  "
                        + "and jedcust.company = (select companyid from company where subdomain = ?) "
                        + "and jedcust." + receiptCust + " = (select id from fieldcombodata where value =  ? and fieldid = (select id from fieldparams where fieldparams.companyid =  jedcust.company and fieldparams.fieldlabel = ? and fieldparams.moduleid = ? ))";

                preparedStatement = conn.prepareStatement(query);

                preparedStatement.setString(1, fieldValueMap.getString(key));
                preparedStatement.setInt(2, Constants.Acc_Receive_Payment_ModuleId);
                preparedStatement.setString(3, subDomain);
                preparedStatement.setString(4, key);
                preparedStatement.setString(5, dimension);
                preparedStatement.setInt(6, Constants.Acc_Invoice_ModuleId);

                count = preparedStatement.executeUpdate();

                out.println("Receipt jedetails updated for " + key + " : " + count + "<br>");

            }

        }

        count = 0;
        failedcount = 0;
        preparedStatement = null;
        out.println("======================= Updating data for Debit Note ========================<br>");

        fieldParamsQuery = "select id,`value` from fieldcombodata "
                + "where fieldid =(select id from fieldparams INNER JOIN company on company.companyid = fieldparams.companyid and company.subdomain = ? and fieldparams.fieldlabel = ? and fieldparams.moduleid = ? ) "
                + "ORDER BY `value`";

        fieldquery = conn.prepareStatement(fieldParamsQuery);
        fieldquery.setString(1, subDomain);
        fieldquery.setString(2, dimension);
        fieldquery.setInt(3, Constants.Acc_Debit_Note_ModuleId);
        rs = fieldquery.executeQuery();
        fieldValueMap = new JSONObject();

        while (rs.next()) {
            String id = rs.getString("id");
            String fieldValue = rs.getString("value");
            fieldValueMap.put(fieldValue, id);
        }
        keys = fieldValueMap.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (fieldValueMap.has(key) && fieldValueMap.getString(key) != null) {

                query = "update accjecustomdata jedcust set jedcust." + dnCust + " = ? "
                        + "where  jedcust.moduleId = ?  "
                        + "and jedcust.company = (select companyid from company where subdomain = ?) "
                        + "and jedcust." + dnCust + " = (select id from fieldcombodata where value =  ? and fieldid = (select id from fieldparams where fieldparams.companyid =  jedcust.company and fieldparams.fieldlabel = ? and fieldparams.moduleid = ? ))";

                preparedStatement = conn.prepareStatement(query);

                preparedStatement.setString(1, fieldValueMap.getString(key));
                preparedStatement.setInt(2, Constants.Acc_Debit_Note_ModuleId);
                preparedStatement.setString(3, subDomain);
                preparedStatement.setString(4, key);
                preparedStatement.setString(5, dimension);
                preparedStatement.setInt(6, Constants.Acc_Vendor_Invoice_ModuleId);

                count = preparedStatement.executeUpdate();

                out.println("Debit note Journal entries updated for " + key + " : " + count + "<br>");

            }

        }

        count = 0;
        failedcount = 0;
        preparedStatement = null;
        out.println("======================= Updating data for Credit Note ========================<br>");

        fieldParamsQuery = "select id,`value` from fieldcombodata "
                + "where fieldid =(select id from fieldparams INNER JOIN company on company.companyid = fieldparams.companyid and company.subdomain = ? and fieldparams.fieldlabel = ? and fieldparams.moduleid = ? ) "
                + "ORDER BY `value`";

        fieldquery = conn.prepareStatement(fieldParamsQuery);
        fieldquery.setString(1, subDomain);
        fieldquery.setString(2, dimension);
        fieldquery.setInt(3, Constants.Acc_Credit_Note_ModuleId);
        rs = fieldquery.executeQuery();
        fieldValueMap = new JSONObject();

        while (rs.next()) {
            String id = rs.getString("id");
            String fieldValue = rs.getString("value");
            fieldValueMap.put(fieldValue, id);
        }
        keys = fieldValueMap.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (fieldValueMap.has(key) && fieldValueMap.getString(key) != null) {

                query = "update accjecustomdata jedcust set jedcust." + dnCust + " = ? "
                        + "where  jedcust.moduleId = ?  "
                        + "and jedcust.company = (select companyid from company where subdomain = ?) "
                        + "and jedcust." + dnCust + " = (select id from fieldcombodata where value =  ? and fieldid = (select id from fieldparams where fieldparams.companyid =  jedcust.company and fieldparams.fieldlabel = ? and fieldparams.moduleid = ? ))";

                preparedStatement = conn.prepareStatement(query);

                preparedStatement.setString(1, fieldValueMap.getString(key));
                preparedStatement.setInt(2, Constants.Acc_Credit_Note_ModuleId);
                preparedStatement.setString(3, subDomain);
                preparedStatement.setString(4, key);
                preparedStatement.setString(5, dimension);
                preparedStatement.setInt(6, Constants.Acc_Invoice_ModuleId);

                count = preparedStatement.executeUpdate();

                out.println("Debit note Journal entries updated for " + key + " : " + count + "<br>");

            }

        }

        count = 0;
        failedcount = 0;
        preparedStatement = null;

        out.println("======================= Updating accjedetailcustomdataref for Payment/Receipt ========================<br>");

        query = "UPDATE jedetail jed INNER JOIN accjedetailcustomdata jedcust on jedcust.jedetailId = jed.id "
                + "set jed.accjedetailcustomdataref = jedcust.jedetailId "
                + "where (ISNULL(jed.accjedetailcustomdataref) or jed.accjedetailcustomdataref !=jedcust.jedetailId) and jedcust.moduleId in (?,?,?) "
                + "and jedcust.company = (select companyid from company where subdomain = ?)";

        preparedStatement = conn.prepareStatement(query);

        preparedStatement.setInt(1, Constants.Acc_Make_Payment_ModuleId);
        preparedStatement.setInt(2, Constants.Acc_Receive_Payment_ModuleId);
        preparedStatement.setInt(3, Constants.Acc_GENERAL_LEDGER_ModuleId);
        preparedStatement.setString(4, subDomain);

        count = preparedStatement.executeUpdate();

        out.println("Total accjedetailcustomdataref updated : "  + count + "<br>");
        count = 0;
        failedcount = 0;
        preparedStatement = null;
        
        out.println("======================= Updating accjecustomdataref for CNDN ========================<br>");

        query = "update journalentry je INNER JOIN accjecustomdata jecust on jecust.journalEntryId = je.id "
                + "set je.accjecustomdataref = jecust.journalEntryId "
                + "where (ISNULL(je.accjecustomdataref) or je.accjecustomdataref !=jecust.journalEntryId) and jecust.moduleId in (?,?) "
                + "and jecust.company = (select companyid from company where subdomain = ?)";

        preparedStatement = conn.prepareStatement(query);

        preparedStatement.setInt(1, Constants.Acc_Debit_Note_ModuleId);
        preparedStatement.setInt(2, Constants.Acc_Credit_Note_ModuleId);
        preparedStatement.setString(3, subDomain);

        count = preparedStatement.executeUpdate();

        out.println("Total accjecustomdataref updated : "  + count + "<br>");
        out.println("======================= END ========================<br>");
        


        out.println();
    } catch (Exception e) {
        out.print(e.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>