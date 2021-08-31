<%@page import="com.krawler.common.util.Constants"%>
<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%!
    int udpateCustomRefId(Connection conn, String linetable, String refkey, String linetableid, String id) {
        int count = 0;
        try {
            String updateRef = "UPDATE " + linetable + " SET " + refkey + "=?  WHERE " + linetableid + "=?";
            PreparedStatement stmtquery = conn.prepareStatement(updateRef);
            stmtquery.setString(1, id);
            stmtquery.setString(2, id);
            count = stmtquery.executeUpdate();
        } catch (Exception ex) {

        }
        return count;
    }
%>

<%

    String message = "";
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        String fieldlabel = request.getParameter("fieldlabel");
        int module = -1;
        if (request.getParameter("module") != null) {
            module = Integer.parseInt(request.getParameter("module"));
        }
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery;
        String company = "";
        String customquery = "";
        ResultSet custrs;
        ResultSet customerCustomRs;
        int count = 0;
        long column = 1;
        HashMap<Integer, Long> moduleMap = new HashMap<Integer, Long>();

        String DetailTableid = "";
        String customgolbaltable = "";
        String customgolbaltableid = "";
        String customdetailTable = "";
        int moduleid = 1;

        // Get customer custom field
        String fieldquery = "SELECT id,colnum,moduleid,companyid from fieldparams where fieldlabel=? and  companyid in (SELECT companyid FROM company WHERE subdomain=?) ";
        stmtquery = conn.prepareStatement(fieldquery);
        stmtquery.setString(1, fieldlabel);
        stmtquery.setString(2, subDomain);
        customerCustomRs = stmtquery.executeQuery();

        while (customerCustomRs.next()) {
            column = customerCustomRs.getLong("colnum");
            moduleid = customerCustomRs.getInt("moduleid");
            company = customerCustomRs.getString("companyid");
            moduleMap.put(moduleid, column);

        }

        // Get Customers
        customquery = "SELECT id,name from customer WHERE company in (SELECT companyid FROM company WHERE subdomain=?)";
        stmtquery = conn.prepareStatement(customquery);
        stmtquery.setString(1, subDomain);
        custrs = stmtquery.executeQuery();

        while (custrs.next()) {

            String customerid = custrs.getString("id");
            String customerName = custrs.getString("name");
            int invCount = 0;
            int doCount = 0;
            int soCount = 0;

            String getCustomerValue = " SELECT col" + moduleMap.get(Constants.Acc_Customer_ModuleId) + " FROM customercustomdata WHERE customerid=? ";
            stmtquery = conn.prepareStatement(getCustomerValue);
            stmtquery.setString(1, customerid);
            ResultSet customerValueRs = stmtquery.executeQuery();

            if (customerValueRs.next()) {

                String customerValue = customerValueRs.getString("col" + moduleMap.get(Constants.Acc_Customer_ModuleId));

                // Get Customer Invoices
                String invoicequery = "SELECT id,journalentry FROM invoice WHERE customer=? and company=? ";
                stmtquery = conn.prepareStatement(invoicequery);
                stmtquery.setString(1, customerid);
                stmtquery.setString(2, company);
                ResultSet invoiceRs = stmtquery.executeQuery();

                while (invoiceRs.next()) {
                    String invoiceid = invoiceRs.getString("id");
                    String jeid = invoiceRs.getString("journalentry");
                    String getInvoiceCustomData = " SELECT journalentryId from accjecustomdata where journalentryId=? ";
                    stmtquery = conn.prepareStatement(getInvoiceCustomData);
                    stmtquery.setString(1, jeid);
                    ResultSet invoiceCustomDataRs = stmtquery.executeQuery();

                    if (invoiceCustomDataRs.next()) {

                        String invQuery = " SELECT col" + moduleMap.get(Constants.Acc_Invoice_ModuleId) + " FROM accjecustomdata where journalentryId=? and col"+  moduleMap.get(Constants.Acc_Invoice_ModuleId) + " <> '' ";
                        stmtquery = conn.prepareStatement(invQuery);
                        stmtquery.setString(1, jeid);
                        ResultSet invQueryRS = stmtquery.executeQuery();
                        if (!invQueryRS.next()) {
                            String updateQuery = " UPDATE accjecustomdata set col" + moduleMap.get(Constants.Acc_Invoice_ModuleId) + "= ? where journalentryId=? ";
                            stmtquery = conn.prepareStatement(updateQuery);
                            stmtquery.setString(1, customerValue);
                            stmtquery.setString(2, jeid);
                            stmtquery.executeUpdate();
                            invCount++;
                        }

                    } else {
                        String insertQuery = " insert into accjecustomdata (journalentryId,company,moduleId,col" + moduleMap.get(Constants.Acc_Invoice_ModuleId) + ") values(?,?,?,?) ";
                        stmtquery = conn.prepareStatement(insertQuery);
                        stmtquery.setString(1, jeid);
                        stmtquery.setString(2, company);
                        stmtquery.setInt(3, Constants.Acc_Invoice_ModuleId);
                        stmtquery.setString(4, customerValue);
                        int invQueryRS = stmtquery.executeUpdate();
//                        udpateCustomRefId(conn, "deliveryorder", "accdeliveryordercustomdataref", "id", doid);        // Update Ref Id in Details table
                        invCount++;
                    }

                }

                // Get Customer DO
                String DOquery = "SELECT id FROM deliveryorder WHERE customer=? and company=? ";
                stmtquery = conn.prepareStatement(DOquery);
                stmtquery.setString(1, customerid);
                stmtquery.setString(2, company);
                ResultSet deliveryOrderRs = stmtquery.executeQuery();

                while (deliveryOrderRs.next()) {
                    String doid = deliveryOrderRs.getString("id");
                    String getDOCustomData = " SELECT deliveryOrderId from deliveryordercustomdata where deliveryOrderId=? ";
                    stmtquery = conn.prepareStatement(getDOCustomData);
                    stmtquery.setString(1, doid);
                    ResultSet doCustomDataRs = stmtquery.executeQuery();

                    if (doCustomDataRs.next()) {

                        String doQuery = " SELECT col" + moduleMap.get(Constants.Acc_Delivery_Order_ModuleId) + " FROM deliveryordercustomdata where deliveryOrderId=? and col"+  moduleMap.get(Constants.Acc_Delivery_Order_ModuleId) + " <> '' ";
                        stmtquery = conn.prepareStatement(doQuery);
                        stmtquery.setString(1, doid);
                        ResultSet doQueryRS = stmtquery.executeQuery();
                        if (!doQueryRS.next()) {
                            String updateQuery = " UPDATE deliveryordercustomdata set col" + moduleMap.get(Constants.Acc_Delivery_Order_ModuleId) + "= ? where deliveryOrderId=? ";
                            stmtquery = conn.prepareStatement(updateQuery);
                            stmtquery.setString(1, customerValue);
                            stmtquery.setString(2, doid);
                            stmtquery.executeUpdate();
                            doCount++;
                        }

                    } else {
                        String insertQuery = " insert into deliveryordercustomdata (deliveryOrderId,company,moduleId,col" + moduleMap.get(Constants.Acc_Delivery_Order_ModuleId) + ") values(?,?,?,?) ";
                        stmtquery = conn.prepareStatement(insertQuery);
                        stmtquery.setString(1, doid);
                        stmtquery.setString(2, company);
                        stmtquery.setInt(3, Constants.Acc_Delivery_Order_ModuleId);
                        stmtquery.setString(4, customerValue);
                        int doQueryRS = stmtquery.executeUpdate();
                        udpateCustomRefId(conn, "deliveryorder", "accdeliveryordercustomdataref", "id", doid);        // Update Ref Id in Details table
                        doCount++;
                    }

                }

                // Get Customer SO
                String SOquery = "SELECT id FROM salesorder WHERE customer=? and company=? ";
                stmtquery = conn.prepareStatement(SOquery);
                stmtquery.setString(1, customerid);
                stmtquery.setString(2, company);
                ResultSet salesOrderRs = stmtquery.executeQuery();

                while (salesOrderRs.next()) {
                    String soid = salesOrderRs.getString("id");
                    String getSOCustomData = " SELECT soID from salesordercustomdata where soID=? ";
                    stmtquery = conn.prepareStatement(getSOCustomData);
                    stmtquery.setString(1, soid);
                    ResultSet soCustomDataRs = stmtquery.executeQuery();

                    if (soCustomDataRs.next()) {

                        String soQuery = " SELECT col" + moduleMap.get(Constants.Acc_Sales_Order_ModuleId) + " FROM salesordercustomdata where soID=? and col"+  moduleMap.get(Constants.Acc_Sales_Order_ModuleId) + " <> '' ";
                        stmtquery = conn.prepareStatement(soQuery);
                        stmtquery.setString(1, soid);
                        ResultSet soQueryRS = stmtquery.executeQuery();
                        if (!soQueryRS.next()) {
                            String updateQuery = " UPDATE salesordercustomdata set col" + moduleMap.get(Constants.Acc_Sales_Order_ModuleId) + "= ? where soID=? ";
                            stmtquery = conn.prepareStatement(updateQuery);
                            stmtquery.setString(1, customerValue);
                            stmtquery.setString(2, soid);
                            stmtquery.executeUpdate();
                            soCount++;
                        }

                    } else {
                        String insertQuery = " insert into salesordercustomdata (soID,company,moduleId,col" + moduleMap.get(Constants.Acc_Sales_Order_ModuleId) + ") values(?,?,?,?) ";
                        stmtquery = conn.prepareStatement(insertQuery);
                        stmtquery.setString(1, soid);
                        stmtquery.setString(2, company);
                        stmtquery.setInt(3, Constants.Acc_Sales_Order_ModuleId);
                        stmtquery.setString(4, customerValue);
                        int soQueryRS = stmtquery.executeUpdate();
                        udpateCustomRefId(conn, "salesorder", "salesordercustomdataref", "id", soid);        // Update Ref Id in Details table
                        soCount++;
                    }

                }

            }

            message += "\nCustomer Name: " + customerName + ", DO Count=" + doCount + ", SO Count=" + soCount + ", Invoice Count=" + invCount;

        }

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
        out.print("Script Updated successfully.");
        out.print(message);
    }
%>