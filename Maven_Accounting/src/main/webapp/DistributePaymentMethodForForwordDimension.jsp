

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
        ResultSet rs;
        int jecount = 0, jefailedcount = 0, dimensionCount = 0, dimensionfailedcount = 0, paymentCount = 0;
        PreparedStatement preparedStatement = null;

        JSONObject fieldParamsMap = new JSONObject();

        String fieldParamsQuery = "select moduleid, colnum from fieldparams "
                + "INNER JOIN company on company.companyid = fieldparams.companyid and company.subdomain = ? "
                + "where moduleid in ('" + Constants.Acc_Make_Payment_ModuleId + "','" + Constants.Acc_Receive_Payment_ModuleId + "') and fieldlabel = ? and isforknockoff = ?";
        fieldquery = conn.prepareStatement(fieldParamsQuery);
        fieldquery.setString(1, subDomain);
        fieldquery.setString(2, dimension);
        fieldquery.setInt(3, 1);
        rs = fieldquery.executeQuery();

        boolean isDimensionExists = false;
        while (rs.next()) {
            isDimensionExists = true;
            String moduleid = rs.getString("moduleid");
            String colnum = rs.getString("colnum");
            fieldParamsMap.put(moduleid, colnum);
        }

        if (!isDimensionExists) {
            throw new Exception(" Please provide valid dimension.");
        }

        String paymentCust = "col" + fieldParamsMap.get(Constants.Acc_Make_Payment_ModuleId + "");
        String receiptCust = "col" + fieldParamsMap.get(Constants.Acc_Receive_Payment_ModuleId + "");

        out.println("======================= Distributing payment method amount for payment =============<br>");

        String query = "select  p.id as 'paymentid' , p.paymentnumber , j.entryno , j.id as 'journalid', jedcust." + paymentCust + " as 'linedimension', company.companyid, "
                + "jed.debit, jed.amount , jed.amountinbase ,jed.isbankcharge,jed.forexgainloss,jed.paymenttype,jed.exchangeratefortransaction , jed.gstapplied ,jed.gstcurrencyrate , jed.customerVendorId,jed.accountpersontype,jed.accjedetailproductcustomdataref,jed.accjedetailcustomdataref,   "
                + "jedcust.deleted, jedcust.recdetailId, pm.methodname , pm.account as 'paymentAccount',UUID() as 'newJedetailId' from payment p "
                + "inner JOIN journalentry j on p.journalentry = j.id "
                + "INNER JOIN jedetail jed on jed.journalEntry = j.id "
                + "INNER JOIN paydetail on p.paydetail = paydetail.id "
                + "INNER JOIN paymentmethod pm on pm.id = paydetail.paymentMethod "
                + "INNER JOIN company on p.company = company.companyid "
                + "INNER JOIN accjedetailcustomdata jedcust on jedcust.jedetailId = jed.id "
                + "where NOT EXISTS ( SELECT seperatedjed.id FROM jedetail AS seperatedjed WHERE j.id = seperatedjed.journalEntry AND seperatedjed.isseparated = 'T')  "
                + "and pm.account!=jed.account and company.subdomain = ?  and !ISNULL(jedcust." + paymentCust + ") ORDER BY  p.paymentnumber";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        ResultSet resultSet = stmtquery.executeQuery();
        String previousPayment = "";
        boolean isFailed = false;

        while (resultSet.next()) {
            isFailed = false;
            String paymentid = resultSet.getString("paymentid");
            String journalid = resultSet.getString("journalid");
            String linedimension = resultSet.getString("linedimension");
            String debit = resultSet.getString("debit");
            double amount = resultSet.getDouble("amount");
            double amountinbase = resultSet.getDouble("amountinbase");
            String deleted = resultSet.getString("deleted");
            String methodname = resultSet.getString("methodname");
            String recdetailId = resultSet.getString("recdetailId");
            String paymentAccount = resultSet.getString("paymentAccount");
            String newJedetailId = resultSet.getString("newJedetailId").replace("-", "");
            String companyid = resultSet.getString("companyid");
            String isbankcharge = resultSet.getString("isbankcharge");
            String forexgainloss = resultSet.getString("forexgainloss");
            String paymenttype = resultSet.getString("paymenttype");
            String exchangeratefortransaction = resultSet.getString("exchangeratefortransaction");
            String gstapplied = resultSet.getString("gstapplied");
            String gstcurrencyrate = resultSet.getString("gstcurrencyrate");
            String customerVendorId = resultSet.getString("customerVendorId");
            String accountpersontype = resultSet.getString("accountpersontype");
            String accjedetailproductcustomdataref = resultSet.getString("accjedetailproductcustomdataref");
            String accjedetailcustomdataref = resultSet.getString("accjedetailcustomdataref");

            if (debit.equals("T")) {
                debit = "F";
            } else if (debit.equals("F")) {
                debit = "T";
            }

            if (!StringUtil.isNullOrEmpty(linedimension)) {
                String insertQuery = "insert into jedetail (id , debit , amount , account , journalEntry , company , amountinbase , "
                        + "isseparated,isbankcharge,forexgainloss,paymenttype,exchangeratefortransaction,gstapplied,gstcurrencyrate,customerVendorId,accountpersontype,accjedetailproductcustomdataref,"
                        + "accjedetailcustomdataref) VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )";
                preparedStatement = conn.prepareStatement(insertQuery);
                preparedStatement.setString(1, newJedetailId);
                preparedStatement.setString(2, debit);
                preparedStatement.setDouble(3, amount);
                preparedStatement.setString(4, paymentAccount);
                preparedStatement.setString(5, journalid);
                preparedStatement.setString(6, companyid);
                preparedStatement.setDouble(7, amountinbase);
                preparedStatement.setString(8, "T");
                preparedStatement.setString(9, isbankcharge);
                preparedStatement.setString(10, forexgainloss);
                preparedStatement.setString(11, paymenttype);
                preparedStatement.setString(12, exchangeratefortransaction);
                preparedStatement.setString(13, gstapplied);
                preparedStatement.setString(14, gstcurrencyrate);
                preparedStatement.setString(15, customerVendorId);
                preparedStatement.setString(16, accountpersontype);
                preparedStatement.setString(17, accjedetailproductcustomdataref);
                preparedStatement.setString(18, accjedetailcustomdataref);

                try {
                    if (preparedStatement != null) {
                        preparedStatement.executeUpdate();
                    }
                    jecount++;
                } catch (Exception e) {
                    isFailed = true;
                    jefailedcount++;
                    e.printStackTrace();
                    System.out.println(" payment Invoice : " + jecount + e);
                    throw e;
                }

                insertQuery = "insert into accjedetailcustomdata (jedetailId," + paymentCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                preparedStatement = conn.prepareStatement(insertQuery);
                preparedStatement.setString(1, newJedetailId);
                preparedStatement.setString(2, linedimension);
                preparedStatement.setString(3, companyid);
                preparedStatement.setString(4, deleted);
                preparedStatement.setInt(5, Constants.Acc_Make_Payment_ModuleId);
                preparedStatement.setString(6, recdetailId);
                try {
                    if (preparedStatement != null) {
                        preparedStatement.executeUpdate();
                    }
                    dimensionCount++;
                } catch (Exception e) {
                    isFailed = true;
                    System.out.println(" payment Invoice : " + dimensionCount + e);
                    dimensionfailedcount++;
                    e.printStackTrace();
                    throw e;
                }

                if (!previousPayment.equals(paymentid) && !isFailed) {
                    paymentCount++;
                }
                previousPayment = paymentid;
            }
        }
        out.println(" Jedetails created : " + jecount + "<br>");
        out.println(" Jedetails failed : " + jefailedcount + "<br>");
        out.println(" Dimensions created : " + dimensionCount + "<br>");
        out.println(" Dimensions failed : " + dimensionfailedcount + "<br>");
        out.println(" Total Payments updated : " + paymentCount + "<br>");
        jecount = 0; 
        jefailedcount = 0; dimensionCount = 0; dimensionfailedcount = 0; paymentCount = 0;

        out.println("====================================================================<br>");
        
        out.println("======================= Distributing payment method amount for Receipt ==============<br>");

        query = "select  r.id as 'receiptid' , r.receiptnumber , j.entryno , j.id as 'journalid', jedcust." + receiptCust+ " as 'linedimension', company.companyid, "
                + "jed.debit, jed.amount , jed.amountinbase ,jed.isbankcharge,jed.forexgainloss,jed.paymenttype,jed.exchangeratefortransaction , jed.gstapplied ,jed.gstcurrencyrate , jed.customerVendorId,jed.accountpersontype,jed.accjedetailproductcustomdataref,jed.accjedetailcustomdataref,   "
                + "jedcust.deleted, jedcust.recdetailId, pm.methodname , pm.account as 'paymentAccount',UUID() as 'newJedetailId' from receipt r "
                + "inner JOIN journalentry j on r.journalentry = j.id "
                + "INNER JOIN jedetail jed on jed.journalEntry = j.id "
                + "INNER JOIN paydetail on r.paydetail = paydetail.id "
                + "INNER JOIN paymentmethod pm on pm.id = paydetail.paymentMethod "
                + "INNER JOIN company on r.company = company.companyid "
                + "INNER JOIN accjedetailcustomdata jedcust on jedcust.jedetailId = jed.id "
                + "where NOT EXISTS ( SELECT seperatedjed.id FROM jedetail AS seperatedjed WHERE j.id = seperatedjed.journalEntry AND seperatedjed.isseparated = 'T')  "
                + "and pm.account!=jed.account and company.subdomain = ?  and !ISNULL(jedcust." + receiptCust + ") ORDER BY r.receiptnumber";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        previousPayment = "";

        while (resultSet.next()) {
            isFailed = false;
            String receiptid = resultSet.getString("receiptid");
            String journalid = resultSet.getString("journalid");
            String linedimension = resultSet.getString("linedimension");
            String debit = resultSet.getString("debit");
            double amount = resultSet.getDouble("amount");
            double amountinbase = resultSet.getDouble("amountinbase");
            String deleted = resultSet.getString("deleted");
            String methodname = resultSet.getString("methodname");
            String recdetailId = resultSet.getString("recdetailId");
            String paymentAccount = resultSet.getString("paymentAccount");
            String newJedetailId = resultSet.getString("newJedetailId").replace("-", "");
            String companyid = resultSet.getString("companyid");
            String isbankcharge = resultSet.getString("isbankcharge");
            String forexgainloss = resultSet.getString("forexgainloss");
            String paymenttype = resultSet.getString("paymenttype");
            String exchangeratefortransaction = resultSet.getString("exchangeratefortransaction");
            String gstapplied = resultSet.getString("gstapplied");
            String gstcurrencyrate = resultSet.getString("gstcurrencyrate");
            String customerVendorId = resultSet.getString("customerVendorId");
            String accountpersontype = resultSet.getString("accountpersontype");
            String accjedetailproductcustomdataref = resultSet.getString("accjedetailproductcustomdataref");
            String accjedetailcustomdataref = resultSet.getString("accjedetailcustomdataref");

            if (debit.equals("T")) {
                debit = "F";
            } else if (debit.equals("F")) {
                debit = "T";
            }

            if (!StringUtil.isNullOrEmpty(linedimension)) {
               String insertQuery = "insert into jedetail (id , debit , amount , account , journalEntry , company , amountinbase , "
                        + "isseparated,isbankcharge,forexgainloss,paymenttype,exchangeratefortransaction,gstapplied,gstcurrencyrate,customerVendorId,accountpersontype,accjedetailproductcustomdataref,"
                        + "accjedetailcustomdataref) VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )";
                preparedStatement = conn.prepareStatement(insertQuery);
                preparedStatement.setString(1, newJedetailId);
                preparedStatement.setString(2, debit);
                preparedStatement.setDouble(3, amount);
                preparedStatement.setString(4, paymentAccount);
                preparedStatement.setString(5, journalid);
                preparedStatement.setString(6, companyid);
                preparedStatement.setDouble(7, amountinbase);
                preparedStatement.setString(8, "T");
                preparedStatement.setString(9, isbankcharge);
                preparedStatement.setString(10, forexgainloss);
                preparedStatement.setString(11, paymenttype);
                preparedStatement.setString(12, exchangeratefortransaction);
                preparedStatement.setString(13, gstapplied);
                preparedStatement.setString(14, gstcurrencyrate);
                preparedStatement.setString(15, customerVendorId);
                preparedStatement.setString(16, accountpersontype);
                preparedStatement.setString(17, accjedetailproductcustomdataref);
                preparedStatement.setString(18, accjedetailcustomdataref);

                try {
                    if (preparedStatement != null) {
                        preparedStatement.executeUpdate();
                    }
                    jecount++;
                } catch (Exception e) {
                    isFailed = true;
                    jefailedcount++;
                    e.printStackTrace();
                    System.out.println(" payment Invoice : " + jecount + e);
                    throw e;
                }

                insertQuery = "insert into accjedetailcustomdata (jedetailId," + paymentCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                preparedStatement = conn.prepareStatement(insertQuery);
                preparedStatement.setString(1, newJedetailId);
                preparedStatement.setString(2, linedimension);
                preparedStatement.setString(3, companyid);
                preparedStatement.setString(4, deleted);
                preparedStatement.setInt(5, Constants.Acc_Receive_Payment_ModuleId);
                preparedStatement.setString(6, recdetailId);
                try {
                    if (preparedStatement != null) {
                        preparedStatement.executeUpdate();
                    }
                    dimensionCount++;
                } catch (Exception e) {
                    isFailed = true;
                    System.out.println(" payment Invoice : " + dimensionCount + e);
                    dimensionfailedcount++;
                    e.printStackTrace();
                    throw e;
                }

                if (!previousPayment.equals(receiptid) && !isFailed) {
                    paymentCount++;
                }
                previousPayment = receiptid;
            }
        }
        out.println(" Jedetails created : " + jecount + "<br>");
        out.println(" Jedetails failed : " + jefailedcount + "<br>");
        out.println(" Dimensions created : " + dimensionCount + "<br>");
        out.println(" Dimensions failed : " + dimensionfailedcount + "<br>");
        out.println(" Total Receipts updated : " + paymentCount + "<br>");

        out.println("====================================================================<br>");

    } catch (Exception e) {
        out.print(e.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>