

<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%
    
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
        PreparedStatement stmtquery;
        String ModuleIdQuery = "";
        ResultSet rs;
        String company = "";
        String customquery = "";
        ResultSet custrs;
        ResultSet moduleidrs;
        String fieldId = "";
        String fieldcombodataid = "";
        String invno = "";
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

        ModuleIdQuery = "SELECT moduleid FROM fieldparams WHERE fieldlabel=? and companyid IN(SELECT companyid from company WHERE subdomain=?)";
        stmtquery = conn.prepareStatement(ModuleIdQuery);
        stmtquery.setString(1, fieldlabel);
        stmtquery.setString(2, subDomain);
        moduleidrs = stmtquery.executeQuery();
        while (moduleidrs.next()) {

            int module = Integer.parseInt(moduleidrs.getString("moduleid"));
            int count = 0;
            String message = "";
            String message1 = "";
            switch (module) {
                case 27:
                case 41:
                case 51:
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
                case 57:
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
                case 50:                //consignment
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
                case 24:        //CI
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;
                case 22:        //Cq
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

                case 6:     //VI
                case 39:   //FA -PI 
                case 58:
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;
                case 2:     //CI
                case 38:
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
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;
                case 25:        //Customer
                    customgolbaltable = "customercustomdata";
                    customgolbaltableid = "customerId";
                    break;
                case 26:        //Vendor
                    customgolbaltable = "vendorcustomdata";
                    customgolbaltableid = "vendorId";
                    break;
                case 30:        //product
                case 42:        //group
                    customgolbaltable = "accproductcustomdata";
                    customgolbaltableid = "productId";

                    break;
                case 35:        //contract
                    customgolbaltable = "contractcustomdata";
                    customgolbaltableid = "contractid";
                    break;
                case 79:        //Serial Window
                    customgolbaltable = "serialcustomdata";
                    customgolbaltableid = "serialdocumentmappingid";
                    break;
            }

            if (!StringUtil.isNullOrEmpty("" + module)) {
//             get fieldid and column no for respective dimention
                customquery = "select id,colnum,companyid from fieldparams where fieldlabel=? and companyid in (select companyid from company where subdomain=?) and moduleid=?";
                stmtquery = conn.prepareStatement(customquery);
                stmtquery.setString(1, fieldlabel);
                stmtquery.setString(2, subDomain);
                stmtquery.setInt(3, module);
                custrs = stmtquery.executeQuery();

                while (custrs.next()) {
                    fieldId = custrs.getString("id");
                    column = custrs.getLong("colnum");
                    company = custrs.getString("companyid");
                }
            }
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

                    String q1 = "select id from fieldcombodata where fieldid=? and value=?";        // get records for same value and field i.e. same doule
                    stmtquery = conn.prepareStatement(q1);
                    stmtquery.setString(1, fieldId);
                    stmtquery.setString(2, value);
                    ResultSet r1 = stmtquery.executeQuery();
                    HashMap map = new HashMap();
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
                        if (!customgolbaltable.isEmpty()) {
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
                    //detail table

                        if (!customdetailTable.isEmpty()) {
                            String colquery2 = "select " + DetailTableid + ",col" + column + " from " + customdetailTable + " where company=?";
                            stmtquery = conn.prepareStatement(colquery2);
                            stmtquery.setString(1, company);
                            ResultSet resultSet3 = stmtquery.executeQuery();
                            while (resultSet3.next()) {
                                tablevalue = resultSet3.getString("col" + column + "");
                                recvalue = resultSet3.getString(DetailTableid);
                                if (map.size() > 1 && !StringUtil.isNullOrEmpty(tablevalue)) {
                                    if (map.containsValue(tablevalue)) {
//                                if (tablevalue.equalsIgnoreCase(map.get("" + 2).toString()) || tablevalue.equalsIgnoreCase(map.get("" + 3).toString()) || tablevalue.equalsIgnoreCase(map.get("" + 4).toString())) {
                                        String insertQuery = "update " + customdetailTable + " set col" + column + "=? where " + DetailTableid + "=? ";
                                        PreparedStatement preparedStatement = conn.prepareStatement(insertQuery);
                                        preparedStatement.setString(1, map.get("" + 1).toString());                // value fro global level data
                                        preparedStatement.setString(2, recvalue);
                                        preparedStatement.executeUpdate();

                                    }
                                }
                            }
                        }
                    }

                    //delete fieldCombodata which consist duplication
                    if (map.size() > 1) {
                        message1 += value+", ";
                        count++;
                        String deletequery = "delete from fieldcombodata where fieldid=? and id=?";
                        PreparedStatement statement = conn.prepareStatement(deletequery);
                        statement.setString(1, fieldId);
                        statement.setString(2, map.get("" + 2).toString());
                        statement.executeUpdate();
                        if (map.size() > 2) {
                            statement.setString(2, map.get("" + 3).toString());
                            statement.executeUpdate();
                        }
                        if (map.size() > 3) {
                            statement.setString(2, map.get("" + 4).toString());
                            statement.executeUpdate();
                        }

                    }

                }
            }

            System.out.println("data added for following records");
            message = "<br>Subdomain: " + subDomain + ", ModuleId= " + module + ", count=" + count + ", Fields=" + message1 + "<br>";
            out.print(message);
        }

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>