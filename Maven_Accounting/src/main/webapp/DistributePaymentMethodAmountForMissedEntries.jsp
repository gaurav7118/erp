

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
        PreparedStatement stmtquery, fieldquery, deleteStmt;
        ResultSet rs;
        int jecount = 0, jefailedcount = 0, dimensionCount = 0, dimensionfailedcount = 0, paymentCount = 0;
        PreparedStatement preparedStatement = null;
        PreparedStatement pstmt = null;

        JSONObject fieldParamsMap = new JSONObject();

        String fieldParamsQuery = "select moduleid, colnum from fieldparams "
                + "INNER JOIN company on company.companyid = fieldparams.companyid and company.subdomain = ? "
                + "where moduleid in ('" + Constants.Acc_Make_Payment_ModuleId + "','" + Constants.Acc_Receive_Payment_ModuleId + "','" + Constants.Acc_GENERAL_LEDGER_ModuleId + "') and fieldlabel = ? and isforknockoff = ?";
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
        String jeCust = "col" + fieldParamsMap.get(Constants.Acc_GENERAL_LEDGER_ModuleId + "");


        out.println("======================= Distributing payment method amount for Missing entries ==============<br>");

        String query = "select  j.entryno ,j.entrydate,j.id as 'journalid', j.company,pm.account as 'pmaccount',jed.account as 'jedaccount',jed.id as 'jedid' , "
                + "jed.debit,jed.amount,jed.amountinbase,jed.isseparated,jedcust.col2 as 'linedimension',jed.isbankcharge,jed.forexgainloss,jed.paymenttype,jed.exchangeratefortransaction , jed.gstapplied ,"
                + "jed.gstcurrencyrate , jed.customerVendorId,jed.accountpersontype,jed.accjedetailproductcustomdataref,jed.accjedetailcustomdataref,jedcust.deleted, jedcust.recdetailId,UUID() as 'newJedetailId' from receipt r "
                + "inner JOIN journalentry j on r.journalentry = j.id  "
                + "INNER JOIN jedetail jed on jed.journalEntry = j.id "
                + "INNER JOIN paydetail on r.paydetail = paydetail.id "
                + "INNER JOIN paymentmethod pm on pm.id = paydetail.paymentMethod "
                + "INNER JOIN company on r.company = company.companyid "
                + "LEFT JOIN accjedetailcustomdata jedcust on jedcust.jedetailId = jed.id "
                + " where EXISTS ( SELECT * FROM jedetail AS seperatedjed left JOIN accjedetailcustomdata on accjedetailcustomdata.jedetailId = seperatedjed.id WHERE j.id = seperatedjed.journalEntry AND seperatedjed.isseparated = 'T' and ISNULL(accjedetailcustomdata.col2)) "
                + " and company.subdomain = ? ORDER BY j.entryno";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        //stmtquery.setString(2, "JE009900");
        ResultSet resultSet = stmtquery.executeQuery();
        String previousPayment = "";
        String deleteQuery = "";
        boolean isFailed;

        while (resultSet.next()) {
            isFailed = false;
            String entryno = resultSet.getString("entryno");
            String entrydate = resultSet.getString("entrydate");
            String journalid = resultSet.getString("journalid");
            String company = resultSet.getString("company");
            String pmaccount = resultSet.getString("pmaccount");
            String jedaccount = resultSet.getString("jedaccount");
            String jedid = resultSet.getString("jedid");
            String debit = resultSet.getString("debit");
            double amount = resultSet.getDouble("amount");
            double amountinbase = resultSet.getDouble("amountinbase");
            String isseparated = resultSet.getString("isseparated");
            String linedimension = resultSet.getString("linedimension");
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
            String deleted = resultSet.getString("deleted");
            String recdetailId = resultSet.getString("recdetailId");
            String newJedetailId = resultSet.getString("newJedetailId");

            if (StringUtil.isNullOrEmpty(linedimension)) {
                if (isseparated.equals("T") && jedaccount.equals(pmaccount)) {
                    deleteQuery = " delete from jedetail where id = ? ";
                    deleteStmt = conn.prepareStatement(deleteQuery);
                    deleteStmt.setString(1, jedid);
                    deleteStmt.executeUpdate();
                }
            } else if (!StringUtil.isNullOrEmpty(linedimension)) {

                if (debit.equals("T")) {
                    debit = "F";
                } else if (debit.equals("F")) {
                    debit = "T";
                }
                String insertQuery = "insert into jedetail (id , debit , amount , account , journalEntry , company , amountinbase , "
                        + "isseparated,isbankcharge,forexgainloss,paymenttype,exchangeratefortransaction,gstapplied,gstcurrencyrate,customerVendorId,accountpersontype,accjedetailproductcustomdataref,"
                        + "accjedetailcustomdataref) VALUES ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )";
                preparedStatement = conn.prepareStatement(insertQuery);
                preparedStatement.setString(1, newJedetailId);
                preparedStatement.setString(2, debit);
                preparedStatement.setDouble(3, amount);
                preparedStatement.setString(4, pmaccount);
                preparedStatement.setString(5, journalid);
                preparedStatement.setString(6, company);
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
                pstmt = conn.prepareStatement(insertQuery);
                pstmt.setString(1, newJedetailId);
                pstmt.setString(2, linedimension);
                pstmt.setString(3, company);
                pstmt.setString(4, deleted);
                pstmt.setInt(5, Constants.Acc_Receive_Payment_ModuleId);
                pstmt.setString(6, recdetailId);
                try {
                    if (pstmt != null) {
                        pstmt.executeUpdate();
                    }
                    dimensionCount++;
                } catch (Exception e) {
                    isFailed = true;
                    System.out.println(" payment Invoice : " + dimensionCount + e);
                    dimensionfailedcount++;
                    e.printStackTrace();
                    throw e;
                }

                if (!previousPayment.equals(entryno) && !isFailed) {
                    paymentCount++;
                }
                previousPayment = entryno;
            }
        }
        out.println(" Jedetails created : " + jecount + "<br>");
        out.println(" Jedetails failed : " + jefailedcount + "<br>");
        out.println(" Dimensions created : " + dimensionCount + "<br>");
        out.println(" Dimensions failed : " + dimensionfailedcount + "<br>");
        out.println(" Total Receipts updated : " + paymentCount + "<br>");

        out.println("====================================================================<br>");

        out.println("==== Tag Custom field to tax for payment against GL =====<br>");

        int count = 0;
        int failedcount = 0;

        query = "SELECT pdo.gstjedid, paymentcust." + paymentCust + " as 'paymentDim', paymentcust.company , paymentcust.deleted , paymentcust.moduleId , paymentcust.recdetailId, "
                + "taxcust.jedetailId as 'custjedetailId',taxcust." + paymentCust + " as 'taxDim' from payment p "
                + "inner join paymentdetailotherwise pdo on pdo.payment = p.id "
                + "INNER JOIN jedetail pdojed on pdojed.id = pdo.totaljedid "
                + "INNER JOIN jedetail jed on pdo.gstjedid = jed.id "
                + "INNER JOIN accjedetailcustomdata paymentcust on paymentcust.jedetailId = pdojed.id "
                + "LEFT JOIN accjedetailcustomdata taxcust on taxcust.jedetailId = jed.id "
                + "where p.company =(select companyid from company where subdomain = ?)";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        while (resultSet.next()) {
            preparedStatement = null;
            String gstjedid = resultSet.getString("gstjedid");
            String paymentDim = resultSet.getString("paymentDim");
            String company = resultSet.getString("company");
            String deleted = resultSet.getString("deleted");
            String moduleId = resultSet.getString("moduleId");
            String recdetailId = resultSet.getString("recdetailId");
            String custjedetailId = resultSet.getString("custjedetailId");
            String taxDim = resultSet.getString("taxDim");


            if (StringUtil.isNullOrEmpty(taxDim) && !StringUtil.isNullOrEmpty(paymentDim)) {
                if (StringUtil.isNullOrEmpty(custjedetailId)) {
                    String insertQuery = "insert into accjedetailcustomdata (jedetailId," + paymentCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, gstjedid);
                    preparedStatement.setString(2, paymentDim);
                    preparedStatement.setString(3, company);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_Make_Payment_ModuleId);
                    preparedStatement.setString(6, recdetailId);
                } else if (custjedetailId.equals(gstjedid)) {
                    String insertQuery = "update accjedetailcustomdata set " + paymentCust + "=? where jedetailId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, paymentDim);
                    preparedStatement.setString(2, gstjedid);
                }
                try {
                    if (preparedStatement != null) {
                        count += preparedStatement.executeUpdate();
                    }
                    //count++;
                } catch (Exception e) {
                    failedcount++;
                    e.printStackTrace();
                    System.out.println(" payment Invoice" + e);
                    throw e;
                }
            }
        }
        out.println(" Total tax details updated : " + count + "<br>");
        out.println(" Total tax details failed : " + failedcount + "<br>");
        out.println("====================================================================<br>");

        out.println("==== Tag Custom field to tax for receipt against GL =====<br>");

        count = 0;
        failedcount = 0;

        query = "SELECT pdo.gstjedid, paymentcust." + receiptCust + " as 'paymentDim', paymentcust.company , paymentcust.deleted , paymentcust.moduleId , paymentcust.recdetailId, "
                + "taxcust.jedetailId as 'custjedetailId',taxcust." + receiptCust + " as 'taxDim' from receipt p "
                + "inner join receiptdetailotherwise pdo on pdo.receipt = p.id "
                + "INNER JOIN jedetail pdojed on pdojed.id = pdo.totaljedid "
                + "INNER JOIN jedetail jed on pdo.gstjedid = jed.id "
                + "INNER JOIN accjedetailcustomdata paymentcust on paymentcust.jedetailId = pdojed.id "
                + "LEFT JOIN accjedetailcustomdata taxcust on taxcust.jedetailId = jed.id "
                + "where p.company =(select companyid from company where subdomain = ?)";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        while (resultSet.next()) {
            preparedStatement = null;
            String gstjedid = resultSet.getString("gstjedid");
            String paymentDim = resultSet.getString("paymentDim");
            String company = resultSet.getString("company");
            String deleted = resultSet.getString("deleted");
            String moduleId = resultSet.getString("moduleId");
            String recdetailId = resultSet.getString("recdetailId");
            String custjedetailId = resultSet.getString("custjedetailId");
            String taxDim = resultSet.getString("taxDim");


            if (StringUtil.isNullOrEmpty(taxDim) && !StringUtil.isNullOrEmpty(paymentDim)) {
                if (StringUtil.isNullOrEmpty(custjedetailId)) {
                    String insertQuery = "insert into accjedetailcustomdata (jedetailId," + paymentCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, gstjedid);
                    preparedStatement.setString(2, paymentDim);
                    preparedStatement.setString(3, company);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_Make_Payment_ModuleId);
                    preparedStatement.setString(6, recdetailId);
                } else if (custjedetailId.equals(gstjedid)) {
                    String insertQuery = "update accjedetailcustomdata set " + paymentCust + "=? where jedetailId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, paymentDim);
                    preparedStatement.setString(2, gstjedid);
                }
                try {
                    if (preparedStatement != null) {
                        count += preparedStatement.executeUpdate();
                    }
                    //count++;
                } catch (Exception e) {
                    failedcount++;
                    e.printStackTrace();
                    System.out.println(" payment Invoice" + e);
                    throw e;
                }
            }
        }
        out.println(" Total tax details updated : " + count + "<br>");
        out.println(" Total tax details failed : " + failedcount + "<br>");
        out.println("====================================================================<br>");

        out.println("========= Distributing payment method amount against tax for GL payment ======<br>");
        jecount = 0;
        jefailedcount = 0;
        dimensionCount = 0;
        dimensionfailedcount = 0;
        paymentCount = 0;
        query = "SELECT p.paymentnumber,jecust." + paymentCust + " as 'globalCustom' ,jecust.deleted,jecust.company, accjedetailcustomdata.col2 as 'taxCustom', pdo.id as 'recdetailId',p.journalentry as 'journalid', "
                + "jed.debit, jed.amount , jed.amountinbase ,jed.isbankcharge,jed.forexgainloss,jed.paymenttype,jed.exchangeratefortransaction , jed.gstapplied ,jed.gstcurrencyrate , jed.customerVendorId,jed.accountpersontype, jed.accjedetailproductcustomdataref , jed.accjedetailcustomdataref, "
                + "pm.account as 'paymentAccount',UUID() as 'newJedetailId' from payment p "
                + "inner join paymentdetailotherwise pdo on pdo.payment = p.id  "
                + "INNER JOIN paydetail on p.paydetail = paydetail.id "
                + "INNER JOIN paymentmethod pm on pm.id = paydetail.paymentMethod "
                + "INNER JOIN accjecustomdata jecust on jecust.journalentryId = p.journalentry "
                + "INNER JOIN jedetail jed on pdo.gstjedid = jed.id "
                + "LEFT JOIN accjedetailcustomdata on accjedetailcustomdata.jedetailId = jed.id "
                + "where p.company in(select companyid from company where subdomain = ?) ";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        previousPayment = "";
        isFailed = false;

        while (resultSet.next()) {
            isFailed = false;
            String paymentnumber = resultSet.getString("paymentnumber");
            String globalCustom = resultSet.getString("globalCustom");
            String taxCustom = resultSet.getString("taxCustom");
            String debit = resultSet.getString("debit");
            double amount = resultSet.getDouble("amount");
            double amountinbase = resultSet.getDouble("amountinbase");
            String journalid = resultSet.getString("journalid");
            String recdetailId = resultSet.getString("recdetailId");
            String paymentAccount = resultSet.getString("paymentAccount");
            String newJedetailId = resultSet.getString("newJedetailId").replace("-", "");
            String companyid = resultSet.getString("company");
            String deleted = resultSet.getString("deleted");
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

            if (StringUtil.isNullOrEmpty(taxCustom) && !StringUtil.isNullOrEmpty(globalCustom)) {
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
                preparedStatement.setString(2, globalCustom);
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

            }
        }
        out.println(" Jedetails created : " + jecount + "<br>");
        out.println(" Jedetails failed : " + jefailedcount + "<br>");
        out.println(" Dimensions created : " + dimensionCount + "<br>");
        out.println(" Dimensions failed : " + dimensionfailedcount + "<br>");
        jecount = 0;
        jefailedcount = 0;
        dimensionCount = 0;
        dimensionfailedcount = 0;
        paymentCount = 0;

        out.println("====================================================================<br>");

        out.println("======================= Distributing payment method amount against tax for GL receipt ==============<br>");

        query = "SELECT p.receiptnumber,jecust." + receiptCust + " as 'globalCustom' ,jecust.deleted,jecust.company, accjedetailcustomdata." + receiptCust + " as 'taxCustom', pdo.id = 'recdetailId',p.journalentry as 'journalid' ,"
                + "jed.debit, jed.amount , jed.amountinbase ,jed.isbankcharge,jed.forexgainloss,jed.paymenttype,jed.exchangeratefortransaction , jed.gstapplied ,jed.gstcurrencyrate , jed.customerVendorId,jed.accountpersontype, jed.accjedetailproductcustomdataref , jed.accjedetailcustomdataref,  "
                + "pm.account as 'paymentAccount',UUID() as 'newJedetailId' from receipt p "
                + "inner join receiptdetailotherwise pdo on pdo.receipt = p.id  "
                + "INNER JOIN paydetail on p.paydetail = paydetail.id "
                + "INNER JOIN paymentmethod pm on pm.id = paydetail.paymentMethod "
                + "INNER JOIN accjecustomdata jecust on jecust.journalentryId = p.journalentry "
                + "INNER JOIN jedetail jed on pdo.gstjedid = jed.id "
                + "LEFT JOIN accjedetailcustomdata on accjedetailcustomdata.jedetailId = jed.id "
                + "where p.company in(select companyid from company where subdomain = ?) ";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        previousPayment = "";

        while (resultSet.next()) {
            isFailed = false;
            String receiptnumber = resultSet.getString("receiptnumber");
            String globalCustom = resultSet.getString("globalCustom");
            String taxCustom = resultSet.getString("taxCustom");
            String debit = resultSet.getString("debit");
            double amount = resultSet.getDouble("amount");
            double amountinbase = resultSet.getDouble("amountinbase");
            String deleted = resultSet.getString("deleted");
            String journalid = resultSet.getString("journalid");
            String recdetailId = resultSet.getString("recdetailId");
            String paymentAccount = resultSet.getString("paymentAccount");
            String newJedetailId = resultSet.getString("newJedetailId").replace("-", "");
            String companyid = resultSet.getString("company");
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

            if (!StringUtil.isNullOrEmpty(globalCustom)) {
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

                insertQuery = "insert into accjedetailcustomdata (jedetailId," + receiptCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                preparedStatement = conn.prepareStatement(insertQuery);
                preparedStatement.setString(1, newJedetailId);
                preparedStatement.setString(2, globalCustom);
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

            }
        }
        out.println(" Jedetails created : " + jecount + "<br>");
        out.println(" Jedetails failed : " + jefailedcount + "<br>");
        out.println(" Dimensions created : " + dimensionCount + "<br>");
        out.println(" Dimensions failed : " + dimensionfailedcount + "<br>");

        out.println("====================================================================<br>");

        out.println("============= Migrate Normal Journal Entry Dimension data =========<br>");
        count = 0;
        failedcount = 0;
        query = "select jedetail.id as 'jedetailid' , accjecustomdata." + jeCust + " as 'jecust' , journalentry.company, accjecustomdata.deleted, accjecustomdata.moduleId, accjedetailcustomdata.jedetailId as 'custjedetail' from journalentry "
                + "INNER JOIN company on company.companyid = journalentry.company and company.subdomain = ? "
                + "INNER JOIN jedetail on jedetail.journalEntry = journalentry.id "
                + "INNER JOIN accjecustomdata on journalentry.id=accjecustomdata.journalentryId "
                + "LEFT JOIN accjedetailcustomdata on accjedetailcustomdata.jedetailId = jedetail.id "
                + "where journalentry.typevalue = 1 AND !ISNULL(accjecustomdata." + jeCust + ") and ISNULL(accjedetailcustomdata." + jeCust + ")";
        
        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        while (resultSet.next()) {
            String jedetailid = resultSet.getString("jedetailid");
            String jecust = resultSet.getString("jecust");
            String company = resultSet.getString("company");
            String deleted = resultSet.getString("deleted");
            String moduleId = resultSet.getString("moduleId");
            String custjedetail = resultSet.getString("custjedetail");
            
            if (!StringUtil.isNullOrEmpty(jecust)) {
                if (StringUtil.isNullOrEmpty(custjedetail)) {
                    String insertQuery = "insert into accjedetailcustomdata (jedetailId," + jeCust + ",company,deleted,moduleId,recdetailId) VALUES (?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, jedetailid);
                    preparedStatement.setString(2, jecust);
                    preparedStatement.setString(3, company);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    preparedStatement.setString(6, jedetailid);
                } else if (custjedetail.equals(jedetailid)) {
                    String insertQuery = "update accjedetailcustomdata set " + jeCust + "=? where jedetailId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, jecust);
                    preparedStatement.setString(2, jedetailid);
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

    } catch (Exception e) {
        out.print(e.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>