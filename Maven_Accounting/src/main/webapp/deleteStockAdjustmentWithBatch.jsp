<%-- 
    Document   : DeleteOpeningStock
    Created on : Oct 3, 2017, 9:18:24 PM
    Author     : krawler
--%>


<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


<%
    Connection conn = null;
    try {

        String serverip = request.getParameter("serverip");//"192.168.0.208";                            
        String port = "3306";
        String dbName = request.getParameter("dbname");//"bwrl2909";
        String userName = request.getParameter("username");//"root";
        String password = request.getParameter("password"); //""
        String subdomain = request.getParameter("subdomain"); //""
        String driver = "com.mysql.jdbc.Driver";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);

        String sa_id = "", warehouse = "", productId = "", companyId = "", type = "", location = "", journalEntry = "", inventoryref = "", batch = "";
        double quantity = 0.0;
        int count = 0;

        String getStockAdjustment = "SELECT sa.id,sa.store,sad.location,sad.batchname,sa.product,sad.finalquantity,sa.adjustment_type,sa.inventoryje,sa.company,sa.inventoryref FROM in_stockadjustment sa INNER JOIN in_sa_detail sad ON sa.id=sad.stockadjustment WHERE sa.company IN (SELECT companyid FROM company WHERE subdomain='" + subdomain + "') AND sa.seqno in ('SA-201803-000003','SA-201803-000004','SA-201803-000005','SA-201803-000006','SA-201803-000007','SA-201803-000008')";
        PreparedStatement ppsmt = conn.prepareStatement(getStockAdjustment);
        ResultSet stockAdjustments = ppsmt.executeQuery();

        while (stockAdjustments.next()) {

            sa_id = stockAdjustments.getString("id");
            warehouse = stockAdjustments.getString("store");
            location = stockAdjustments.getString("location");
            batch = StringUtil.isNullOrEmpty(stockAdjustments.getString("batchname")) ? "" : stockAdjustments.getString("batchname");
            productId = stockAdjustments.getString("product");
            type = stockAdjustments.getString("adjustment_type");
            inventoryref = StringUtil.isNullOrEmpty(stockAdjustments.getString("inventoryref")) ? "" : stockAdjustments.getString("inventoryref");
            quantity = Math.abs(stockAdjustments.getDouble("finalquantity"));
            journalEntry = StringUtil.isNullOrEmpty(stockAdjustments.getString("inventoryje")) ? "" : stockAdjustments.getString("inventoryje");
            companyId = stockAdjustments.getString("company");
            String newproductbatchQuery = " ";
            String inStockQuery = " ";
            ResultSet productBatch = null;
            ResultSet inStock = null;
            if (!StringUtil.isNullOrEmpty(batch)) {
                newproductbatchQuery = "SELECT id,quantity,quantitydue FROM newproductbatch WHERE location=? AND warehouse=? AND batchname =? AND product=?";
                inStockQuery = "SELECT id,quantity FROM in_stock WHERE location=? AND store=? AND batchname=? AND product=?";

                PreparedStatement stmt2 = conn.prepareStatement(newproductbatchQuery);
                stmt2.setString(1, location);
                stmt2.setString(2, warehouse);
                stmt2.setString(3, batch);
                stmt2.setString(4, productId);
                productBatch = stmt2.executeQuery();

                PreparedStatement stmt3 = conn.prepareStatement(inStockQuery);
                stmt3.setString(1, location);
                stmt3.setString(2, warehouse);
                stmt3.setString(3, batch);
                stmt3.setString(4, productId);
                inStock = stmt3.executeQuery();
            } else {
                newproductbatchQuery = "SELECT id,quantity,quantitydue FROM newproductbatch WHERE location=? AND warehouse=? AND product=?";
                inStockQuery = "SELECT id,quantity FROM in_stock WHERE location=? AND store=? AND product=?";

                PreparedStatement stmt2 = conn.prepareStatement(newproductbatchQuery);
                stmt2.setString(1, location);
                stmt2.setString(2, warehouse);
                stmt2.setString(3, productId);
                productBatch = stmt2.executeQuery();

                PreparedStatement stmt3 = conn.prepareStatement(inStockQuery);
                stmt3.setString(1, location);
                stmt3.setString(2, warehouse);
                stmt3.setString(3, productId);
                inStock = stmt3.executeQuery();

            }
            while (productBatch.next()) {
                String batchId = productBatch.getString("id");
                double batchQty = productBatch.getDouble("quantity");
                double batchQtyDue = productBatch.getDouble("quantitydue");

                if (type.equalsIgnoreCase("Stock IN")) {
                    batchQty = batchQty - quantity;
                    batchQtyDue = batchQtyDue - quantity;
                } else if (type.equalsIgnoreCase("Stock Out")) {
                    batchQty = batchQty + quantity;
                    batchQtyDue = batchQtyDue + quantity;
                }

                String updateProductBatch = "update newproductbatch set quantity=?,quantitydue=? where id=? and company=? ";

                PreparedStatement stmtforUpdate = conn.prepareStatement(updateProductBatch);
                stmtforUpdate.setDouble(1, batchQty);
                stmtforUpdate.setDouble(2, batchQtyDue);
                stmtforUpdate.setString(3, batchId);
                stmtforUpdate.setString(4, companyId);
                stmtforUpdate.executeUpdate();
            }

            while (inStock.next()) {
                String stockId = inStock.getString("id");
                double stockQty = inStock.getDouble("quantity");

                if (type.equalsIgnoreCase("Stock IN")) {
                    stockQty = stockQty - quantity;
                } else if (type.equalsIgnoreCase("Stock Out")) {
                    stockQty = stockQty + quantity;
                }

                String updateInStock = "update in_stock set quantity=? where id=? and company=? ";

                PreparedStatement stmtforUpdate = conn.prepareStatement(updateInStock);
                stmtforUpdate.setDouble(1, stockQty);
                stmtforUpdate.setString(2, stockId);
                stmtforUpdate.setString(3, companyId);
                stmtforUpdate.executeUpdate();
            }

            String deletesmdetail = "delete from in_sm_detail where stockmovement in (select id from in_stockmovement where modulerefid=? and company=?)";   //Delete record from stock detail
            PreparedStatement smd = conn.prepareStatement(deletesmdetail);
            smd.setString(1, sa_id);
            smd.setString(2, companyId);
            smd.executeUpdate();

            String deletestockmovement = "delete from in_stockmovement where modulerefid=? ";   //Delete record from stockmovement
            PreparedStatement smdel = conn.prepareStatement(deletestockmovement);
            smdel.setString(1, sa_id);
            smdel.executeUpdate();

            if (!StringUtil.isNullOrEmpty(inventoryref)) {
                String queryinvdel = "delete from inventory where company=? and id= ? ";    //Delete Inventory Details
                PreparedStatement stmt4 = conn.prepareStatement(queryinvdel);
                stmt4.setObject(1, companyId);
                stmt4.setObject(2, inventoryref);
                stmt4.executeUpdate();
            }

            String queryForupdatepaq = "";
            if (type.equalsIgnoreCase("Stock IN")) {
                queryForupdatepaq = "update product set availablequantity=availablequantity-? where id=? and company=? ";
            } else if (type.equalsIgnoreCase("Stock Out")) {
                queryForupdatepaq = "update product set availablequantity=availablequantity+? where id=? and company=? ";
            }

            PreparedStatement stmtforUpdatepaq = conn.prepareStatement(queryForupdatepaq);
            stmtforUpdatepaq.setDouble(1, quantity);
            stmtforUpdatepaq.setString(2, productId);
            stmtforUpdatepaq.setString(3, companyId);
            stmtforUpdatepaq.executeUpdate();

            String deleteStockAdjustDetail = "DELETE from in_sa_detail WHERE stockadjustment=?";
            PreparedStatement sadel = conn.prepareStatement(deleteStockAdjustDetail);
            sadel.setString(1, sa_id);
            sadel.executeUpdate();

            String deleteStockAdjust = "DELETE from in_stockadjustment WHERE id=? and company=? ";
            PreparedStatement sdel = conn.prepareStatement(deleteStockAdjust);
            sdel.setString(1, sa_id);
            sdel.setString(2, companyId);
            sdel.executeUpdate();

            if (!StringUtil.isNullOrEmpty(journalEntry)) {
                String deleteJeDetail = " delete from jedetail where journalentry='" + journalEntry + "'";
                PreparedStatement deleteJeD = conn.prepareStatement(deleteJeDetail);
                deleteJeD.executeUpdate();

                String deletequeryJE = "delete from journalentry where id=?";
                PreparedStatement preparedStmntJE = conn.prepareStatement(deletequeryJE);
                preparedStmntJE.setString(1, journalEntry);
                preparedStmntJE.executeUpdate();

            }

            count++;

        }

        StringBuilder result = new StringBuilder("" + count).append(" Records deleted successfully.");
        out.println(result);
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>