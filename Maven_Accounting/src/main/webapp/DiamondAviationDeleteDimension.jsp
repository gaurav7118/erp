

<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%
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
        int module = Integer.parseInt(request.getParameter("module"));
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery;
        String query = "";
        ResultSet rs;
        String company = "";
        String customquery = "";
        ResultSet custrs;
        String fieldId = "";
        String fieldcombodataid = "";
        String invno = "";
        int count = 0;
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
        int fieldType = 0;
        switch (module) {
            case 27:
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
                customgolbaltable = "accjecustomdata";
                customgolbaltableid = "journalentryId";
                customdetailTable = "accjedetailcustomdata";
                DetailTableid = "jedetailId";
                globalTable = "goodsreceipt";
                Entryno = "grnumber";

                break;
            case 2:     //CI
                customgolbaltable = "accjecustomdata";
                customgolbaltableid = "journalentryId";
                customdetailTable = "accjedetailcustomdata";
                DetailTableid = "jedetailId";
                globalTable = "invoice";
                Entryno = "invoicenumber";
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
            case 30:        //product
                customgolbaltable = "accproductcustomdata";
                customgolbaltableid = "productId";
                globalTable = "product";
                Entryno = "name";
                break;

        }

        if (!StringUtil.isNullOrEmpty("" + module)) {
//             get fieldid and column no for respective dimention
            customquery = "select id,colnum,companyid,fieldtype from fieldparams where fieldlabel=? and companyid in (select companyid from company where subdomain=?) and moduleid=?";
            stmtquery = conn.prepareStatement(customquery);
            stmtquery.setString(1, fieldlabel);
            stmtquery.setString(2, subDomain);
            stmtquery.setInt(3, module);
            custrs = stmtquery.executeQuery();

            while (custrs.next()) {
                fieldId = custrs.getString("id");
                column = custrs.getLong("colnum");
                company = custrs.getString("companyid");
                fieldType = custrs.getInt("fieldtype");
            }
        }
        String vaulueid1 = "";
        if (!StringUtil.isNullOrEmpty(fieldId) && !StringUtil.isNullOrEmpty(customgolbaltable)) {
            String tablevalue = "";
            String recvalue = "";
            String colquery = "select " + customgolbaltableid + ",col" + column + " from " + customgolbaltable + " where company=?";
            stmtquery = conn.prepareStatement(colquery);
            stmtquery.setString(1, company);
            ResultSet resultSet2 = stmtquery.executeQuery();
            while (resultSet2.next()) {
                tablevalue = resultSet2.getString("col" + column + "");
                recvalue = resultSet2.getString(customgolbaltableid);
                if (!StringUtil.isNullOrEmpty(tablevalue)) {
                    String insertQuery = "update " + customgolbaltable + " set col" + column + "=NULL where " + customgolbaltableid + "=? and company=? ";
                    PreparedStatement preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, recvalue);
                    preparedStatement.setString(2, company);
                    preparedStatement.executeUpdate();

                    String colquery1 = "select " + Entryno + " from " + globalTable + " where id=?";
                    stmtquery = conn.prepareStatement(colquery1);
                    stmtquery.setString(1, recvalue);
                    ResultSet resultSet3 = stmtquery.executeQuery();
                    while (resultSet3.next()) {
                        String no = resultSet3.getString(Entryno);
                        message1 += no + ",";
                        count++;
                    }
                }
            }
            if (module == 2 && fieldType == 4) {
                String insertQuery2 = "update accjedetailproductcustomdata set col" + column + "=NULL where company=? ";
                PreparedStatement preparedStatement2 = conn.prepareStatement(insertQuery2);
                preparedStatement2.setString(1, company);
                preparedStatement2.executeUpdate();
            }
        }
        //delete fieldCombodata which consist duplication
        if (!StringUtil.isNullOrEmpty(fieldId)) {
            if (fieldType == 4) {
                String deletequery = "delete from fieldcombodata where fieldid=?";
                PreparedStatement statement = conn.prepareStatement(deletequery);
                statement.setString(1, fieldId);
                statement.executeUpdate();
            }
            String deletequery1 = "delete from fieldparams where id=?";
            PreparedStatement statement1 = conn.prepareStatement(deletequery1);
            statement1.setString(1, fieldId);
            statement1.executeUpdate();
        }
        System.out.println("data added for following records");
        message += "\nSubdomain: " + subDomain + ",count=" + count + ", Record No=" + message1;

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