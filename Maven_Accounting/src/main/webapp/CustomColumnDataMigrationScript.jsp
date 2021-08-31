

<%@page import="com.mysql.jdbc.exceptions.MySQLSyntaxErrorException"%>
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
//        String subDomain = request.getParameter("subdomain");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery;

        String customQuery = "";
        ResultSet customrs = null;
        String columnData = "";

        String globalTable = "";
        String DetailTable = "";
        String DetailTableid = "";
        String Entryno = "";
        String customgolbaltable = "";
        String customgolbaltableid = "";
        String customdetailTable = "";
        String refid = "";
        int checkBoxFieldType = 11;

        customQuery = "SELECT moduleid,colnum,companyid from fieldparams WHERE fieldtype=? ";
        stmtquery = conn.prepareStatement(customQuery);
        stmtquery.setInt(1, checkBoxFieldType);
        customrs = stmtquery.executeQuery();
        while (customrs.next()) {
            int module = Integer.parseInt(customrs.getString("moduleid"));
            String columnNumber = customrs.getString("colnum");
            int colnum = Integer.parseInt(columnNumber);
            int finalColNumber = colnum + 965;
            String company = customrs.getString("companyid");
            switch (module) {

                case 27:
                case 41:
                case 51:
                case 67:
                    DetailTableid = "dodetailsid";          //id in customdetail table
                    Entryno = "donumber";
                    customgolbaltable = "deliveryordercustomdata";
                    customgolbaltableid = "deliveryOrderId";
                    customdetailTable = "dodetailscustomdata";
//                    customdetailTableid="dodetailsid";
                    break;
                case 28:
                case 40:
                case 57:
                    DetailTableid = "grodetailsid";
                    Entryno = "gronumber";
                    customgolbaltable = "grordercustomdata";
                    customgolbaltableid = "goodsreceiptorderid";
                    customdetailTable = "grodetailscustomdata";
//                    customdetailTableid="grodetailsid";
                    break;
                case 18:        //PO
                case 63:
                case 90:
                    DetailTableid = "poDetailID";
                    Entryno = "ponumber";
                    customgolbaltable = "purchaseordercustomdata";
                    customgolbaltableid = "poID";
                    customdetailTable = "purchaseorderdetailcustomdata";
//                    customdetailTableid="grodetailsid";
                    break;
                case 20:            //SO
                case 36:            //LO
                case 50:                //consignment
                    DetailTableid = "soDetailID";
                    Entryno = "sonumber";
                    customgolbaltable = "salesordercustomdata";
                    customgolbaltableid = "soID";
                    customdetailTable = "salesorderdetailcustomdata";
//                    customdetailTableid="grodetailsid";
                    break;
                case 23:        //VQ
                case 89:
                    DetailTableid = "vendorquotationdetailsid";
                    Entryno = "quotationnumber";
                    customgolbaltable = "vendorquotationcustomdata";
                    customgolbaltableid = "vendorquotationid";
                    customdetailTable = "vendorquotationdetailscustomdata";
//                    customdetailTableid="grodetailsid";
                    break;
                case 24:        //CI
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    break;
                case 22:        //Cq
                case 65:
                    globalTable = "quotationversioncustomdata";
                    refid = "quotationid";
                    DetailTableid = "quotationdetailsid";
                    Entryno = "quotationnumber";
                    customgolbaltable = "quotationcustomdata";
                    customgolbaltableid = "quotationid";
                    customdetailTable = "quotationdetailscustomdata";

                    break;
                case 29:        //SR
                case 53:
                case 68:
                case 98:
                    DetailTableid = "srdetailsid";
                    Entryno = "srnumber";
                    customgolbaltable = "salesreturncustomdata";
                    customgolbaltableid = "salesreturnid";
                    customdetailTable = "srdetailscustomdata";
                    break;
                case 31:        //PR
                case 59:
                case 96:
                    DetailTableid = "prdetailsid";
                    Entryno = "prnumber";
                    customgolbaltable = "purchasereturncustomdata";
                    customgolbaltableid = "purchasereturnid";
                    customdetailTable = "prdetailscustomdata";
                    break;
                case 32:        //PRqui
                case 87:
                    DetailTableid = "purchaserequisitiondetailid";
                    Entryno = "prnumber";
                    customgolbaltable = "purchaserequisitioncustomdata";
                    customgolbaltableid = "purchaserequisitionid";
                    customdetailTable = "purchaserequisitiondetailcustomdata";
                    break;
                case 33:
                case 88:        //RFQ
                    customgolbaltable = "rfqcustomdata";
                    customgolbaltableid = "rfqid";
                    break;
                case 34:
                    customgolbaltable = "accountcustomdata";
                    customgolbaltableid = "accountId";
                    break;
                case 6:     //VI
                case 39:   //FA -PI 
                case 58:
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    globalTable = "openingbalancevendorinvoicecustomdata";
                    refid = "openingbalancevendorinvoiceid";
                    break;
                case 2:     //CI
                case 38:
                case 52:
                case 93:
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    globalTable = "openingbalanceinvoicecustomdata";
                    refid = "openingbalanceinvoiceid";
                    break;
                case 14:    //MP
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    globalTable = "openingbalancemakepaymentcustomdata";
                    refid = "openingbalancemakepaymentid";
                    break;
                case 16:        //RP
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    globalTable = "openingbalancereceiptcustomdata";
                    refid = "openingbalancereceiptid";
                    break;
                case 10:        //DN
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    globalTable = "openingbalancedebitnotecustomdata";
                    refid = "openingbalancedebitnoteid";
                    break;
                case 12:        //CN
                    customgolbaltable = "accjecustomdata";
                    customgolbaltableid = "journalentryId";
                    customdetailTable = "accjedetailcustomdata";
                    DetailTableid = "jedetailId";
                    globalTable = "openingbalancecreditnotecustomdata";
                    refid = "openingbalancecreditnoteid";
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
                case 64:
                    customgolbaltable = "contractcustomdata";
                    customgolbaltableid = "contractid";
                    break;
                case 79:        //Serial Window
                    customgolbaltable = "serialcustomdata";
                    customgolbaltableid = "serialdocumentmappingid";
                    break;
                case 92:        //Inventory
                    customgolbaltable = "stockcustomdata";
                    customgolbaltableid = "stockId";
                    break;

            }

            if (!customgolbaltable.isEmpty()) {
                if (colnum >= 1036 && colnum <= 1045) {
                    columnData = "SELECT " + customgolbaltableid + ",col" + columnNumber + " from " + customgolbaltable + " WHERE col" + columnNumber + " is not null AND company=?";
                    stmtquery = conn.prepareStatement(columnData);
                    stmtquery.setString(1, company);
                    ResultSet globaltablers = stmtquery.executeQuery();

                    while (globaltablers.next()) {
                        String columnId = globaltablers.getString(customgolbaltableid);
                        String value = globaltablers.getString("col" + columnNumber);

                        //col number of fieldtype 11 starts from 1036 
                        String query1 = "update " + customgolbaltable + " set col" + finalColNumber + " = ? where " + customgolbaltableid + "=? AND company=?";
                        stmtquery = conn.prepareStatement(query1);
                        stmtquery.setString(1, value);
                        stmtquery.setString(2, columnId);
                        stmtquery.setString(3, company);
                        stmtquery.executeUpdate();

                        String query2 = "update " + customgolbaltable + " set col" + columnNumber + "=null where " + customgolbaltableid + "=? AND company=?";
                        stmtquery = conn.prepareStatement(query2);
                        stmtquery.setString(1, columnId);
                        stmtquery.setString(2, company);
                        stmtquery.executeUpdate();
                    }
                }
            }
            if (!customdetailTable.isEmpty()) {
                if (colnum >= 1036 && colnum <= 1045) {
                    columnData = "SELECT " + DetailTableid + ",col" + columnNumber + " from " + customdetailTable + " WHERE col" + columnNumber + " is not null AND company=?";
                    stmtquery = conn.prepareStatement(columnData);
                    stmtquery.setString(1, company);
                    ResultSet detailtablers = stmtquery.executeQuery();

                    while (detailtablers.next()) {
                        String columnId = detailtablers.getString(DetailTableid);
                        String value = detailtablers.getString("col" + columnNumber);

                        //col number of fieldtype 11 starts from 1036 
                        String query1 = "update " + customdetailTable + " set col" + finalColNumber + " = ? where " + DetailTableid + "=? AND company=?";
                        stmtquery = conn.prepareStatement(query1);
                        stmtquery.setString(1, value);
                        stmtquery.setString(2, columnId);
                        stmtquery.setString(3, company);
                        stmtquery.executeUpdate();

                        String query2 = "update " + customdetailTable + " set col" + columnNumber + "=null where " + DetailTableid + "=? AND company=?";
                        stmtquery = conn.prepareStatement(query2);
                        stmtquery.setString(1, columnId);
                        stmtquery.setString(2, company);
                        stmtquery.executeUpdate();
                    }
                }
            }
            if (!globalTable.isEmpty()) {
                if (colnum >= 1036 && colnum <= 1045) {
                    columnData = "SELECT " + refid + ",col" + columnNumber + " from " + globalTable + " WHERE col" + columnNumber + " is not null AND company=?";
                    stmtquery = conn.prepareStatement(columnData);
                    stmtquery.setString(1, company);
                    ResultSet globaltablers = stmtquery.executeQuery();

                    while (globaltablers.next()) {
                        String columnId = globaltablers.getString(refid);
                        String value = globaltablers.getString("col" + columnNumber);

                        String query1 = "update " + globalTable + " set col" + finalColNumber + " = ? where " + refid + "=? AND company=?";
                        stmtquery = conn.prepareStatement(query1);
                        stmtquery.setString(1, value);
                        stmtquery.setString(2, columnId);
                        stmtquery.setString(3, company);
                        stmtquery.executeUpdate();

                        String query2 = "update " + globalTable + " set col" + columnNumber + "=null where " + refid + "=? AND company=?";
                        stmtquery = conn.prepareStatement(query2);
                        stmtquery.setString(1, columnId);
                        stmtquery.setString(2, company);
                        stmtquery.executeUpdate();
                    }
                }
            }
            if (colnum >= 1036 && colnum <= 1045) {
                String query3 = "update fieldparams set colnum = ? where moduleid = ? AND fieldtype=? AND companyid=? ";
                stmtquery = conn.prepareStatement(query3);
                stmtquery.setInt(1, finalColNumber);
                stmtquery.setInt(2, module);
                stmtquery.setInt(3, checkBoxFieldType);
                stmtquery.setString(4, company);
                stmtquery.executeUpdate();
                out.print("Module: " + module + " ColNumber: " + columnNumber + " Replaced with: " + finalColNumber + " CompanyId: "+company+"<br>");
            }
        }

    } catch (MySQLSyntaxErrorException e) {
        e.printStackTrace();
        out.print("<br>" + e.toString() + "<br><br> Please execute proper DB changes first");
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>