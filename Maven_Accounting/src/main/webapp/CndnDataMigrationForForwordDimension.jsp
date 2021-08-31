

<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.utils.json.base.JSONArray"%>
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
        PreparedStatement preparedStatement = null;

        JSONObject fieldParamsMap = new JSONObject();

        String fieldParamsQuery = "select moduleid, colnum from fieldparams "
                + "INNER JOIN company on company.companyid = fieldparams.companyid and company.subdomain = ? "
                + "where moduleid in ('" + Constants.Acc_Invoice_ModuleId + "','" + Constants.Acc_Vendor_Invoice_ModuleId + "','" + Constants.Acc_Debit_Note_ModuleId + "','" + Constants.Acc_Credit_Note_ModuleId + "') and fieldlabel = ? and isforknockoff = ?";
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
        int count = 0, failedcount = 0;

        String siCust = "col" + fieldParamsMap.get(Constants.Acc_Invoice_ModuleId + "");
        String piCust = "col" + fieldParamsMap.get(Constants.Acc_Vendor_Invoice_ModuleId + "");
        String cnCust = "col" + fieldParamsMap.get(Constants.Acc_Credit_Note_ModuleId + "");
        String dnCust = "col" + fieldParamsMap.get(Constants.Acc_Debit_Note_ModuleId + "");

        out.println("======================= Migrating values for " + dimension + " from Purchase invoice to debit note =============<br>");

        String query = "SELECT dnn.id as 'dnid',dnn.dnnumber,goodsreceipt.grnumber,invcustval.value as 'invCustomVal',dncustval.value as 'dnCustomVal', "
                + "dnn.journalentry as 'dnJournal' ,dncust.journalentryId as 'dnCustomJournal',invcust." + piCust + " as 'invCust',invcust.company , invcust.deleted from debitnote dnn "
                + "INNER JOIN company c on c.companyid = dnn.company and c.subdomain = ? "
                + "INNER JOIN dndetails on dndetails.debitNote = dnn.id "
                + "INNER JOIN goodsreceipt on dndetails.goodsreceipt = goodsreceipt.id "
                + "INNER JOIN accjecustomdata invcust on invcust.journalentryId = goodsreceipt.journalentry "
                + "INNER JOIN fieldcombodata invcustval on invcustval.id = invcust." + piCust + " "
                + "left JOIN accjecustomdata dncust on dncust.journalentryId = dnn.journalentry "
                + "left JOIN fieldcombodata dncustval on dncustval.id = dncust." + dnCust + " "
                + "where ISNULL(dncustval.id ) ORDER BY dnn.dnnumber";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        ResultSet resultSet = stmtquery.executeQuery();
        String previousDn = "";
        String previousInvCust = "";

        JSONObject jobj = new JSONObject();
        JSONObject contentsObj = new JSONObject();
        List<String> rejectedDNArr = new ArrayList<String>();
        String dnnumber = "";
        String dnid = "";
        String grnumber = "";
        String invCustomVal = "";
        String dnCustomVal = "";
        String dnJournal = "";
        String dnCustomJournal = "";
        String invCust = "";
        String company = "";
        String deleted = "";

        while (resultSet.next()) {
            dnnumber = resultSet.getString("dnnumber");
            dnid = resultSet.getString("dnid");
            grnumber = resultSet.getString("grnumber");
            invCustomVal = resultSet.getString("invCustomVal");
            dnCustomVal = resultSet.getString("dnCustomVal");
            dnJournal = resultSet.getString("dnJournal");
            dnCustomJournal = resultSet.getString("dnCustomJournal");
            invCust = resultSet.getString("invCust");
            company = resultSet.getString("company");
            deleted = resultSet.getString("deleted");

            if (!rejectedDNArr.contains(dnnumber)) {
                if (previousDn.equals(dnnumber)) {
                    if (previousInvCust.equals(invCustomVal)) {
                        continue;
                    } else {
                        previousInvCust = invCustomVal;
                        jobj.remove(dnid);
                        rejectedDNArr.add(dnnumber);
                    }
                } else {
                    contentsObj = new JSONObject();
                    contentsObj.put("dnnumber", dnnumber);
                    contentsObj.put("dnid", dnid);
                    contentsObj.put("grnumber", grnumber);
                    contentsObj.put("invCustomVal", invCustomVal);
                    contentsObj.put("dnCustomVal", dnCustomVal);
                    contentsObj.put("dnJournal", dnJournal);
                    contentsObj.put("dnCustomJournal", dnCustomJournal);
                    contentsObj.put("invCust", invCust);
                    contentsObj.put("company", company);
                    contentsObj.put("deleted", deleted);

                    jobj.put(dnid, contentsObj);
                }
                previousInvCust = invCustomVal;
                previousDn = dnnumber;
            }
        }

        Iterator<String> keys = jobj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            contentsObj = jobj.getJSONObject(key);
            dnnumber = contentsObj.getString("dnnumber");
            dnid = contentsObj.getString("dnid");
            grnumber = contentsObj.getString("grnumber");
            invCustomVal = contentsObj.getString("invCustomVal");
            dnCustomVal = contentsObj.optString("dnCustomVal");
            dnJournal = contentsObj.getString("dnJournal");
            dnCustomJournal = contentsObj.optString("dnCustomJournal");
            invCust = contentsObj.getString("invCust");
            company = contentsObj.getString("company");
            deleted = contentsObj.getString("deleted");
            if (StringUtil.isNullOrEmpty(dnCustomVal) && !StringUtil.isNullOrEmpty(invCust)) {
                if (StringUtil.isNullOrEmpty(dnCustomJournal)) {
                    String insertQuery = "insert into accjecustomdata (journalentryId," + dnCust + ",company,deleted,moduleId) VALUES (?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, dnJournal);
                    preparedStatement.setString(2, invCust);
                    preparedStatement.setString(3, company);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_Debit_Note_ModuleId);
                } else if (dnCustomJournal.equals(dnJournal)) {
                    String insertQuery = "update accjecustomdata set " + dnCust + "=? where journalentryId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, invCust);
                    preparedStatement.setString(2, dnJournal);
                }
                try {
                    if (preparedStatement != null) {
                        preparedStatement.executeUpdate();
                    }
                    count++;
                } catch (Exception e) {
                    failedcount++;
                    e.printStackTrace();
                    System.out.println(" payment Invoice" + e);
                    throw e;
                }

            }
        }

        out.println(" Total Debit Notes updated : " + count + "<br>");
        out.println(" Total Debit Notes failed : " + failedcount + "<br>");
        if (rejectedDNArr.size() > 0) {
            out.println(" <b>Not able to update following debit notes as it have multiple dimension values for linked invoice.<b><br>");
            out.println(" <b>" + rejectedDNArr.toString().replace(",", "<br>") + "<b>");
        }
        out.println("====================================================================<br>");

        out.println("======================= Migrating values for " + dimension + " from Sales invoice to Credit note =============<br>");

        query = "SELECT cnn.id as 'cnid',cnn.cnnumber,invoice.invoicenumber,invcustval.value as 'invCustomVal',cncustval.value as 'cncustomVal',"
                + "cnn.journalentry as 'cnJournal' ,cncust.journalentryId as 'cncustomJournal', invcust." + siCust + " as 'invCust',invcust.company , invcust.deleted  from creditnote cnn "
                + "INNER JOIN company c on c.companyid = cnn.company and c.subdomain = ? "
                + "INNER JOIN cndetails on cndetails.creditNote = cnn.id "
                + "INNER JOIN invoice on cndetails.invoice = invoice.id "
                + "INNER JOIN accjecustomdata invcust on invcust.journalentryId = invoice.journalentry "
                + "INNER JOIN fieldcombodata invcustval on invcustval.id = invcust." + siCust + " "
                + "left JOIN accjecustomdata cncust on cncust.journalentryId = cnn.journalentry "
                + "left JOIN fieldcombodata cncustval on cncustval.id = cncust." + cnCust + " "
                + "where ISNULL(cncustval.id ) ORDER BY cnn.cnnumber";

        stmtquery = conn.prepareStatement(query);
        stmtquery.setString(1, subDomain);
        resultSet = stmtquery.executeQuery();
        previousDn = "";
        previousInvCust = "";

        jobj = new JSONObject();
        contentsObj = new JSONObject();
        rejectedDNArr = new ArrayList<String>();
        dnnumber = "";
        dnid = "";
        grnumber = "";
        invCustomVal = "";
        dnCustomVal = "";
        dnJournal = "";
        dnCustomJournal = "";
        invCust = "";
        company = "";
        deleted = "";
        count = 0;
        failedcount = 0;


        while (resultSet.next()) {
            dnid = resultSet.getString("cnid");
            dnnumber = resultSet.getString("cnnumber");
            grnumber = resultSet.getString("invoicenumber");
            invCustomVal = resultSet.getString("invCustomVal");
            dnCustomVal = resultSet.getString("cncustomVal");
            dnJournal = resultSet.getString("cnJournal");
            dnCustomJournal = resultSet.getString("cncustomJournal");
            invCust = resultSet.getString("invCust");
            company = resultSet.getString("company");
            deleted = resultSet.getString("deleted");

            if (!rejectedDNArr.contains(dnnumber)) {
                if (previousDn.equals(dnnumber)) {
                    if (previousInvCust.equals(invCustomVal)) {
                        continue;
                    } else {
                        previousInvCust = invCustomVal;
                        jobj.remove(dnid);
                        rejectedDNArr.add(dnnumber);
                    }
                } else {
                    contentsObj = new JSONObject();
                    contentsObj.put("dnnumber", dnnumber);
                    contentsObj.put("dnid", dnid);
                    contentsObj.put("grnumber", grnumber);
                    contentsObj.put("invCustomVal", invCustomVal);
                    contentsObj.put("dnCustomVal", dnCustomVal);
                    contentsObj.put("dnJournal", dnJournal);
                    contentsObj.put("dnCustomJournal", dnCustomJournal);
                    contentsObj.put("invCust", invCust);
                    contentsObj.put("company", company);
                    contentsObj.put("deleted", deleted);

                    jobj.put(dnid, contentsObj);
                }
                previousInvCust = invCustomVal;
                previousDn = dnnumber;
            }
        }

        keys = jobj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            contentsObj = jobj.getJSONObject(key);
            dnnumber = contentsObj.getString("dnnumber");
            dnid = contentsObj.getString("dnid");
            grnumber = contentsObj.getString("grnumber");
            invCustomVal = contentsObj.getString("invCustomVal");
            dnCustomVal = contentsObj.optString("dnCustomVal");
            dnJournal = contentsObj.getString("dnJournal");
            dnCustomJournal = contentsObj.optString("dnCustomJournal");
            invCust = contentsObj.getString("invCust");
            company = contentsObj.getString("company");
            deleted = contentsObj.getString("deleted");
            if (StringUtil.isNullOrEmpty(dnCustomVal) && !StringUtil.isNullOrEmpty(invCust)) {
                if (StringUtil.isNullOrEmpty(dnCustomJournal)) {
                    String insertQuery = "insert into accjecustomdata (journalentryId," + dnCust + ",company,deleted,moduleId) VALUES (?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, dnJournal);
                    preparedStatement.setString(2, invCust);
                    preparedStatement.setString(3, company);
                    preparedStatement.setString(4, deleted);
                    preparedStatement.setInt(5, Constants.Acc_Credit_Note_ModuleId);
                } else if (dnCustomJournal.equals(dnJournal)) {
                    String insertQuery = "update accjecustomdata set " + dnCust + "=? where journalentryId = ? ";
                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, invCust);
                    preparedStatement.setString(2, dnJournal);
                }
                try {
                    if (preparedStatement != null) {
                        preparedStatement.executeUpdate();
                    }
                    count++;
                } catch (Exception e) {
                    failedcount++;
                    e.printStackTrace();
                    System.out.println(" payment Invoice" + e);
                    throw e;
                }

            }
        }

        out.println(" Total Credit Notes updated : " + count + "<br>");
        out.println(" Total Credit Notes failed : " + failedcount + "<br>");
        if (rejectedDNArr.size() > 0) {
            out.println(" <b>Not able to update following Credit notes as it have multiple dimension values for linked invoice.<b><br>");
            out.println(" <b>" + rejectedDNArr.toString().replace(",", "<br>") + "<b>");
        }
        out.println("====================================================================<br>");

    } catch (Exception e) {
        out.print(e.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>