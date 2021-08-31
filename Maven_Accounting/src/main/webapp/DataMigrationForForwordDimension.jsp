

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


        out.println("======================= Migrating data for payment ========================<br>");
        out.println("======================= Payment for Invoice ========================<br>");

        query = " select  p.id, p.paymentnumber,gr.grnumber, jecust." + piCust + " as 'globalCustomData',jecust.deleted,jedcust.jedetailId as 'customjedetail', "
                + "jedcust." + paymentCust + " as 'lineCustomData',c.companyid,jed.id as 'jedetail',pd.id as 'recdetailid' from payment p "
                + "inner JOIN paymentdetail pd on pd.payment = p.id "
                + "inner JOIN goodsreceipt gr on pd.goodsReceipt = gr.id "
                + "inner JOIN journalentry je on gr.journalentry = je.id "
                + "inner JOIN jedetail jed on jed.id = pd.totaljedid "
                + "LEFT JOIN accjecustomdata jecust on je.id=jecust.journalentryId "
                + "LEFT JOIN accjedetailcustomdata jedcust on jedcust.recdetailId = pd.id "
                + "INNER JOIN company c on c.companyid = p.company "
                + "where c.subdomain = ? and ISNULL(jedcust." + paymentCust + ") order by p.paymentnumber";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        ResultSet resultSet = stmtquery.executeQuery();
        while (resultSet.next()) {
            preparedStatement = null;
            String invoicenumber = resultSet.getString("grnumber");
            String paymentnumber = resultSet.getString("paymentnumber");
            String customjedetail = resultSet.getString("customjedetail");
            String companyid = resultSet.getString("companyid");
            String invoiceCustomData = resultSet.getString("globalCustomData");
            String paymentCustomData = resultSet.getString("lineCustomData");
            String deleted = resultSet.getString("deleted");
            String jedetail = resultSet.getString("jedetail");
            String recdetailid = resultSet.getString("recdetailid");
            if (StringUtil.isNullOrEmpty(paymentCustomData) && !StringUtil.isNullOrEmpty(invoiceCustomData)) {
                if (StringUtil.isNullOrEmpty(customjedetail)) {
                    String insertQuery = "insert into accjedetailcustomdata (jedetailId," + paymentCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, jedetail);
                    preparedStatement.setString(2, invoiceCustomData);
                    preparedStatement.setString(3, companyid);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_Make_Payment_ModuleId);
                    preparedStatement.setString(6, recdetailid);
                } else if (customjedetail.equals(jedetail)) {
                    String insertQuery = "update accjedetailcustomdata set " + paymentCust + "=? where jedetailId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, invoiceCustomData);
                    preparedStatement.setString(2, jedetail);
                }
                try {
                    if (preparedStatement != null) {
                        preparedStatement.executeUpdate();
                    }
                } catch (Exception e) {
                    failedcount++;
                    count--;
                    e.printStackTrace();
                    System.out.println(" payment Invoice" + e);
                    throw e;
                }
                count++;
            }
        }
        out.println("Records completed : " + count + " <br>");
        out.println("Records failed : " + failedcount + " <br>");
        count = 0;
        failedcount = 0;
        preparedStatement = null;

        out.println("======================= Payment for Advance Payment/Refund ========================<br>");

        query = " select  p.id, p.paymentnumber,jecust." + paymentCust + " as 'globalCustomData',jecust.deleted,jedcust.jedetailId as 'customjedetail',  "
                + "jedcust." + paymentCust + " as 'lineCustomData',c.companyid,jed.id as 'jedetail',pd.id as 'recdetailid' from payment p "
                + "inner JOIN advancedetail pd on pd.payment = p.id "
                + "inner JOIN journalentry je on p.journalentry = je.id "
                + "inner JOIN jedetail jed on jed.id = pd.totaljedid "
                + "LEFT JOIN accjecustomdata jecust on je.id=jecust.journalentryId "
                + "LEFT JOIN accjedetailcustomdata jedcust on jedcust.recdetailId = pd.id "
                + "INNER JOIN company c on c.companyid = p.company "
                + "where c.subdomain = ? and ISNULL(jedcust." + paymentCust + ") order by p.paymentnumber";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        while (resultSet.next()) {
            String paymentnumber = resultSet.getString("paymentnumber");
            String companyid = resultSet.getString("companyid");
            String customjedetail = resultSet.getString("customjedetail");
            String invoiceCustomData = resultSet.getString("globalCustomData");
            String paymentCustomData = resultSet.getString("lineCustomData");
            String deleted = resultSet.getString("deleted");
            String jedetail = resultSet.getString("jedetail");
            String recdetailid = resultSet.getString("recdetailid");
            if (StringUtil.isNullOrEmpty(paymentCustomData) && !StringUtil.isNullOrEmpty(invoiceCustomData)) {
                if (StringUtil.isNullOrEmpty(customjedetail)) {
                    String insertQuery = "insert into accjedetailcustomdata (jedetailId," + paymentCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, jedetail);
                    preparedStatement.setString(2, invoiceCustomData);
                    preparedStatement.setString(3, companyid);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_Make_Payment_ModuleId);
                    preparedStatement.setString(6, recdetailid);
                } else if (customjedetail.equals(jedetail)) {
                    String insertQuery = "update accjedetailcustomdata set " + paymentCust + "=? where jedetailId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, invoiceCustomData);
                    preparedStatement.setString(2, jedetail);
                }
                try {
                    preparedStatement.executeUpdate();
                } catch (Exception e) {
                    failedcount++;
                    count--;
                    e.printStackTrace();
                    System.out.println("Shrinath" + e);
                    throw e;
                }
                count++;
            }
        }
        out.println("Records completed : " + count + " <br>");
        out.println("Records failed : " + failedcount + " <br>");
        count = 0;
        failedcount = 0;
        preparedStatement = null;

        out.println("======================= Payment for GL account ========================<br>");

        query = " select  p.id, p.paymentnumber,jecust." + paymentCust + " as 'globalCustomData',jecust.deleted, jedcust.jedetailId as 'customjedetail',"
                + "jedcust." + paymentCust + " as 'lineCustomData',c.companyid,jed.id as 'jedetail',pd.id as 'recdetailid',ac.`name` as 'accountname' from payment p "
                + "inner JOIN paymentdetailotherwise pd on pd.payment = p.id "
                + "inner JOIN journalentry je on p.journalentry = je.id "
                + "inner JOIN jedetail jed on jed.id = pd.totaljedid "
                + "LEFT JOIN account ac on pd.account = ac.id "
                + "LEFT JOIN accjecustomdata jecust on je.id=jecust.journalentryId "
                + "LEFT JOIN accjedetailcustomdata jedcust on jedcust.recdetailId = pd.id "
                + "INNER JOIN company c on c.companyid = p.company "
                + "where c.subdomain = ? and ISNULL(jedcust." + paymentCust + ") order by p.paymentnumber";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        while (resultSet.next()) {
            String paymentnumber = resultSet.getString("paymentnumber");
            String accountname = resultSet.getString("accountname");
            String companyid = resultSet.getString("companyid");
            String customjedetail = resultSet.getString("customjedetail");
            String invoiceCustomData = resultSet.getString("globalCustomData");
            String paymentCustomData = resultSet.getString("lineCustomData");
            String deleted = resultSet.getString("deleted");
            String jedetail = resultSet.getString("jedetail");
            String recdetailid = resultSet.getString("recdetailid");
            if (StringUtil.isNullOrEmpty(paymentCustomData) && !StringUtil.isNullOrEmpty(invoiceCustomData)) {
                if (StringUtil.isNullOrEmpty(customjedetail)) {
                    String insertQuery = "insert into accjedetailcustomdata (jedetailId," + paymentCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, jedetail);
                    preparedStatement.setString(2, invoiceCustomData);
                    preparedStatement.setString(3, companyid);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_Make_Payment_ModuleId);
                    preparedStatement.setString(6, recdetailid);
                } else if (customjedetail.equals(jedetail)) {
                    String insertQuery = "update accjedetailcustomdata set " + paymentCust + "=? where jedetailId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, invoiceCustomData);
                    preparedStatement.setString(2, jedetail);
                }
                try {
                    preparedStatement.executeUpdate();
                } catch (Exception e) {
                    failedcount++;
                    count--;
                    e.printStackTrace();
                    System.out.println(" payment Invoice" + e);
                }
                count++;
            }
        }
        out.println("Records completed : " + count + " <br>");
        out.println("Records failed : " + failedcount + " <br>");
        count = 0;
        failedcount = 0;
        preparedStatement = null;

        out.println("======================= Payment for Credit Note ========================<br>");
        query = " select  p.id, p.paymentnumber,cn.cnnumber, jecust." + cnCust + " as 'globalCustomData',jecust.deleted,jedcust.jedetailId as 'customjedetail', "
                + "jedcust." + paymentCust + " as 'lineCustomData',c.companyid,jed.id as 'jedetail',pd.id as 'recdetailid' from payment p "
                + "inner JOIN creditnotpayment pd on pd.paymentid = p.id "
                + "inner JOIN creditnote cn on cn.id = pd.cnid "
                + "inner JOIN journalentry je on cn.journalentry = je.id "
                + "inner JOIN jedetail jed on jed.id = pd.totaljedid "
                + "LEFT JOIN accjecustomdata jecust on je.id=jecust.journalentryId "
                + "LEFT JOIN accjedetailcustomdata jedcust on jedcust.recdetailId = pd.id "
                + "INNER JOIN company c on c.companyid = p.company "
                + "where c.subdomain = ? and ISNULL(jedcust." + paymentCust + ") order by p.paymentnumber";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        while (resultSet.next()) {
            String cnnumber = resultSet.getString("cnnumber");
            String paymentnumber = resultSet.getString("paymentnumber");
            String companyid = resultSet.getString("companyid");
            String customjedetail = resultSet.getString("customjedetail");
            String invoiceCustomData = resultSet.getString("globalCustomData");
            String paymentCustomData = resultSet.getString("lineCustomData");
            String deleted = resultSet.getString("deleted");
            String jedetail = resultSet.getString("jedetail");
            String recdetailid = resultSet.getString("recdetailid");
            if (StringUtil.isNullOrEmpty(paymentCustomData) && !StringUtil.isNullOrEmpty(invoiceCustomData)) {
                if (StringUtil.isNullOrEmpty(customjedetail)) {
                    String insertQuery = "insert into accjedetailcustomdata (jedetailId," + paymentCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, jedetail);
                    preparedStatement.setString(2, invoiceCustomData);
                    preparedStatement.setString(3, companyid);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_Make_Payment_ModuleId);
                    preparedStatement.setString(6, recdetailid);
                } else if (customjedetail.equals(jedetail)) {
                    String insertQuery = "update accjedetailcustomdata set " + paymentCust + "=? where jedetailId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, invoiceCustomData);
                    preparedStatement.setString(2, jedetail);
                }
                try {
                    preparedStatement.executeUpdate();
                } catch (Exception e) {
                    failedcount++;
                    count--;
                    e.printStackTrace();
                    System.out.println(" payment Invoice" + e);
                }
                count++;
            }
        }
        out.println("Records completed : " + count + " <br>");
        out.println("Records failed : " + failedcount + " <br>");
        count = 0;
        failedcount = 0;
        preparedStatement = null;

        out.println("====================================================================<br>");


        out.println("======================= Migrating data for Receipt ========================<br>");
        out.println("======================= Receipt for Invoice ========================<br>");

        query = " select DISTINCT  r.id, r.receiptnumber, inv.invoicenumber, jecust." + siCust + " as 'globalCustomData',jecust.deleted, jedcust.jedetailId as 'customjedetail',"
                + "jedcust." + receiptCust + " as 'lineCustomData',c.companyid,jed.id as 'jedetail',rd.id as 'recdetailid' from receipt r "
                + "inner JOIN receiptdetails rd on rd.receipt = r.id "
                + "inner JOIN invoice inv on inv.id = rd.invoice "
                + "inner JOIN journalentry je on inv.journalentry = je.id  "
                + "inner JOIN jedetail jed on jed.id = rd.totaljedid "
                + "LEFT JOIN accjecustomdata jecust on je.id=jecust.journalentryId "
                + "LEFT JOIN accjedetailcustomdata jedcust on jedcust.recdetailId = rd.id "
                + "INNER JOIN company c on c.companyid = r.company "
                + "where c.subdomain = ? and ISNULL(jedcust." + receiptCust + ") order by r.receiptnumber";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        while (resultSet.next()) {
            String invoicenumber = resultSet.getString("invoicenumber");
            String receiptnumber = resultSet.getString("receiptnumber");
            String companyid = resultSet.getString("companyid");
            String customjedetail = resultSet.getString("customjedetail");
            String invoiceCustomData = resultSet.getString("globalCustomData");
            String paymentCustomData = resultSet.getString("lineCustomData");
            String deleted = resultSet.getString("deleted");
            String jedetail = resultSet.getString("jedetail");
            String recdetailid = resultSet.getString("recdetailid");
            if (StringUtil.isNullOrEmpty(paymentCustomData) && !StringUtil.isNullOrEmpty(invoiceCustomData)) {
                if (StringUtil.isNullOrEmpty(customjedetail)) {
                    String insertQuery = "insert into accjedetailcustomdata (jedetailId," + receiptCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, jedetail);
                    preparedStatement.setString(2, invoiceCustomData);
                    preparedStatement.setString(3, companyid);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_Receive_Payment_ModuleId);
                    preparedStatement.setString(6, recdetailid);
                } else if (customjedetail.equals(jedetail)) {
                    String insertQuery = "update accjedetailcustomdata set " + receiptCust + "=? where jedetailId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, invoiceCustomData);
                    preparedStatement.setString(2, jedetail);
                }
                try {
                    preparedStatement.executeUpdate();
                } catch (Exception e) {
                    failedcount++;
                    count--;
                    e.printStackTrace();
                    System.out.println(" payment Invoice" + e);
                    throw e;
                }
                count++;
            }
        }
        out.println("Records completed : " + count + " <br>");
        out.println("Records failed : " + failedcount + " <br>");
        count = 0;
        failedcount = 0;
        preparedStatement = null;

        out.println("======================= Receipt for Advance/Refund ========================<br>");

        query = " select DISTINCT  r.id, r.receiptnumber,jecust." + receiptCust + " as 'globalCustomData',jecust.deleted, jedcust.jedetailId as 'customjedetail',"
                + "jedcust." + receiptCust + " as 'lineCustomData',c.companyid,jed.id as 'jedetail',rd.id as 'recdetailid' from receipt r "
                + "inner JOIN receiptadvancedetail rd on rd.receipt = r.id "
                + "inner JOIN journalentry je on r.journalentry = je.id  "
                + "inner JOIN jedetail jed on jed.id = rd.totaljedid "
                + "LEFT JOIN accjecustomdata jecust on je.id=jecust.journalentryId "
                + "LEFT JOIN accjedetailcustomdata jedcust on jedcust.recdetailId = rd.id "
                + "INNER JOIN company c on c.companyid = r.company "
                + "where c.subdomain = ? and ISNULL(jedcust." + receiptCust + ") order by r.receiptnumber";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        while (resultSet.next()) {
            String receiptnumber = resultSet.getString("receiptnumber");
            String companyid = resultSet.getString("companyid");
            String customjedetail = resultSet.getString("customjedetail");
            String invoiceCustomData = resultSet.getString("globalCustomData");
            String paymentCustomData = resultSet.getString("lineCustomData");
            String deleted = resultSet.getString("deleted");
            String jedetail = resultSet.getString("jedetail");
            String recdetailid = resultSet.getString("recdetailid");
            if (StringUtil.isNullOrEmpty(paymentCustomData) && !StringUtil.isNullOrEmpty(invoiceCustomData)) {
                if (StringUtil.isNullOrEmpty(customjedetail)) {
                    String insertQuery = "insert into accjedetailcustomdata (jedetailId," + receiptCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, jedetail);
                    preparedStatement.setString(2, invoiceCustomData);
                    preparedStatement.setString(3, companyid);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_Receive_Payment_ModuleId);
                    preparedStatement.setString(6, recdetailid);
                } else if (customjedetail.equals(jedetail)) {
                    String insertQuery = "update accjedetailcustomdata set " + receiptCust + "=? where jedetailId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, invoiceCustomData);
                    preparedStatement.setString(2, jedetail);
                }
                try {
                    preparedStatement.executeUpdate();
                } catch (Exception e) {
                    failedcount++;
                    count--;
                    e.printStackTrace();
                    System.out.println(" payment Invoice" + e);
                    throw e;
                }
                count++;
            }
        }
        out.println("Records completed : " + count + " <br>");
        out.println("Records failed : " + failedcount + " <br>");
        count = 0;
        failedcount = 0;
        preparedStatement = null;

        out.println("======================= Receipt for GL account ========================<br>");

        query = " select DISTINCT  r.id, r.receiptnumber,jecust." + receiptCust + " as 'globalCustomData',jecust.deleted,jedcust.jedetailId as 'customjedetail', "
                + "jedcust." + receiptCust + " as 'lineCustomData',c.companyid,jed.id as 'jedetail',rd.id as 'recdetailid' from receipt r "
                + "inner JOIN receiptdetailotherwise rd on rd.receipt = r.id "
                + "inner JOIN journalentry je on r.journalentry = je.id  "
                + "inner JOIN jedetail jed on jed.id = rd.totaljedid "
                + "LEFT JOIN accjecustomdata jecust on je.id=jecust.journalentryId "
                + "LEFT JOIN accjedetailcustomdata jedcust on jedcust.recdetailId = rd.id "
                + "INNER JOIN company c on c.companyid = r.company "
                + "where c.subdomain = ? and ISNULL(jedcust." + receiptCust + ") order by r.receiptnumber";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        while (resultSet.next()) {
            String receiptnumber = resultSet.getString("receiptnumber");
            String companyid = resultSet.getString("companyid");
            String customjedetail = resultSet.getString("customjedetail");
            String invoiceCustomData = resultSet.getString("globalCustomData");
            String paymentCustomData = resultSet.getString("lineCustomData");
            String deleted = resultSet.getString("deleted");
            String jedetail = resultSet.getString("jedetail");
            String recdetailid = resultSet.getString("recdetailid");
            if (StringUtil.isNullOrEmpty(paymentCustomData) && !StringUtil.isNullOrEmpty(invoiceCustomData)) {
                if (StringUtil.isNullOrEmpty(customjedetail)) {
                    String insertQuery = "insert into accjedetailcustomdata (jedetailId," + receiptCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, jedetail);
                    preparedStatement.setString(2, invoiceCustomData);
                    preparedStatement.setString(3, companyid);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_Receive_Payment_ModuleId);
                    preparedStatement.setString(6, recdetailid);
                } else if (customjedetail.equals(jedetail)) {
                    String insertQuery = "update accjedetailcustomdata set " + receiptCust + "=? where jedetailId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, invoiceCustomData);
                    preparedStatement.setString(2, jedetail);
                }
                try {
                    preparedStatement.executeUpdate();
                } catch (Exception e) {
                    failedcount++;
                    count--;
                    e.printStackTrace();
                    System.out.println(" payment Invoice" + e);
                    throw e;
                }
                count++;
            }
        }
        out.println("Records completed : " + count + " <br>");
        out.println("Records failed : " + failedcount + " <br>");
        count = 0;
        failedcount = 0;
        preparedStatement = null;

        out.println("======================= Receipts for Debit Note ========================<br>");

        query = " select DISTINCT  r.id, r.receiptnumber,dn.dnnumber,jecust." + dnCust + " as 'globalCustomData',jecust.deleted,jedcust.jedetailId as 'customjedetail', "
                + "jedcust." + receiptCust + " as 'lineCustomData',c.companyid,jed.id as 'jedetail',rd.id as 'recdetailid' from receipt r "
                + "inner JOIN debitnotepayment rd on rd.receiptid = r.id  "
                + "INNER JOIN debitnote dn on dn.id = rd.dnid "
                + "inner JOIN journalentry je on dn.journalentry = je.id   "
                + "inner JOIN jedetail jed on jed.id = rd.totaljedid "
                + "LEFT JOIN accjecustomdata jecust on je.id=jecust.journalentryId "
                + "LEFT JOIN accjedetailcustomdata jedcust on jedcust.recdetailId = rd.id "
                + "INNER JOIN company c on c.companyid = r.company "
                + "where c.subdomain = ? and ISNULL(jedcust." + receiptCust + ") order by r.receiptnumber";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        while (resultSet.next()) {
            String receiptnumber = resultSet.getString("receiptnumber");
            String dnnumber = resultSet.getString("dnnumber");
            String companyid = resultSet.getString("companyid");
            String customjedetail = resultSet.getString("customjedetail");
            String invoiceCustomData = resultSet.getString("globalCustomData");
            String paymentCustomData = resultSet.getString("lineCustomData");
            String deleted = resultSet.getString("deleted");
            String jedetail = resultSet.getString("jedetail");
            String recdetailid = resultSet.getString("recdetailid");
            if (StringUtil.isNullOrEmpty(paymentCustomData) && !StringUtil.isNullOrEmpty(invoiceCustomData)) {
                if (StringUtil.isNullOrEmpty(customjedetail)) {
                    String insertQuery = "insert into accjedetailcustomdata (jedetailId," + receiptCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, jedetail);
                    preparedStatement.setString(2, invoiceCustomData);
                    preparedStatement.setString(3, companyid);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_Receive_Payment_ModuleId);
                    preparedStatement.setString(6, recdetailid);
                } else if (customjedetail.equals(jedetail)) {
                    String insertQuery = "update accjedetailcustomdata set " + receiptCust + "=? where jedetailId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, invoiceCustomData);
                    preparedStatement.setString(2, jedetail);
                }
                try {
                    preparedStatement.executeUpdate();
                } catch (Exception e) {
                    failedcount++;
                    count--;
                    e.printStackTrace();
                    System.out.println(" payment Invoice" + e);
                }
                count++;
            }
        }
        out.println("Records completed : " + count + " <br>");
        out.println("Records failed : " + failedcount + " <br>");
        count = 0;
        failedcount = 0;
        out.println("====================================================================");

        out.println();
    } catch (Exception e) {
        out.print(e.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>