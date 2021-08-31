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
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"newstaging";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String filepath = request.getParameter("filepath"); //"krawler"
        String driver = "com.mysql.jdbc.Driver";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
        FileInputStream Productinfo = new FileInputStream(filepath);
        BufferedReader in = new BufferedReader(new InputStreamReader(Productinfo));
//        DataInputStream in = new DataInputStream(invoice);
        int cnt = 0;
        String record = "", currencyid = "";
        double qtyToUpdate = 0;

        String companyId = "3ab05e64-027c-43a3-9add-a95d855bd348";    //companyid for monzoneac
        String storeId = "", locationid = "";
        String query1 = "select id from in_storemaster where company =? and isdefault=1"; //default warehose 
        PreparedStatement stmt1 = conn.prepareStatement(query1);
        stmt1.setObject(1, companyId);
        ResultSet rs = stmt1.executeQuery();
        if (rs.next()) {
            storeId = rs.getString("id");
        }
        String query2 = "select id from in_location where company =? and isdefault=1";  //default warehose 
        PreparedStatement stmt2 = conn.prepareStatement(query2);
        stmt2.setObject(1, companyId);
        ResultSet rs1 = stmt2.executeQuery();
        if (rs1.next()) {
            locationid = rs1.getString("id");
        }
        while ((record = in.readLine()) != null) {
            String productId = "", uomid = "";
            double currency = 0, price = 0;
            if (record != "") {
                String[] recarrName = record.split(",");
                String productCode = recarrName[0].trim();
                String openingQtyString = recarrName[1].trim();


                if ("PID".equals(productCode) && "IQ".equals(openingQtyString)) {
                    continue;
                }
                //price =  Double.parseDouble(recarrName[4].trim());   
                double openingQty = Double.parseDouble(openingQtyString);

                //getting First location Id  ENS       
                String queryForProductId = "select id,unitOfMeasure,currency from product where company=? and productid =?";
                PreparedStatement stmt3 = conn.prepareStatement(queryForProductId);
                stmt3.setObject(1, companyId);
                stmt3.setObject(2, productCode);
                ResultSet rs2 = stmt3.executeQuery();
                if (rs2.next()) {
                    productId = rs2.getString("id");
                    //uomid = rs2.getString("unitOfMeasure");
                } else {
                    out.println("<br> Product not fount for record row " + productCode);
                    continue;
                }
                /*
                 * String getProducInitPurchasePriceQry = " select pl1.price as
                 * initpurchaseprice from pricelist pl1 where
                 * pl1.affecteduser='-1' " + " and pl1.product=? and
                 * pl1.carryin='T' and pl1.currency= ? and" + " (pl1.applydate
                 * in (select min(pl2.applydate) from pricelist pl2 " + " where
                 * pl2.affecteduser='-1' and pl2.product=pl1.product and
                 * pl2.currency=? and pl2.carryin=pl1.carryin group by
                 * pl2.product)) ";
                 *
                 * PreparedStatement ppsmt =
                 * conn.prepareStatement(getProducInitPurchasePriceQry);
                 * ppsmt.setObject(1, productId); ppsmt.setObject(2, currency);
                 * ppsmt.setObject(3, currency); ResultSet pprs =
                 * ppsmt.executeQuery(); if (pprs.next()) { price =
                 * pprs.getDouble("initpurchaseprice"); }
                 */
                String InvId = "";
                double Invbaseuomquantity = 0.0, Invquantity = 0.0;
                String queryinvid = "select id,quantity,baseuomquantity from inventory where company=? and product= ? and newinv='T'";
                PreparedStatement stmt4 = conn.prepareStatement(queryinvid);
                stmt4.setObject(1, companyId);
                stmt4.setObject(2, productId);
                ResultSet rs4 = stmt4.executeQuery();
                if (rs4.next()) {
                    InvId = rs4.getString("id");
                    Invquantity = rs4.getDouble("quantity");
                    Invbaseuomquantity = rs4.getDouble("baseuomquantity");
                }
                if (!StringUtil.isNullOrEmpty(InvId)) {

                    if (Invbaseuomquantity == 0) {
                        out.println("<br> Product  oprning quantity is 0 in table and " + openingQty + " in file for product" + productCode);
                        continue;
                    } else if (Invbaseuomquantity > openingQty) {
                        out.println("<br> Product  oprning quantity is greater than given initial quantity for product" + productCode + " and difrrence is" + (Invbaseuomquantity - openingQty));
                        continue;
                    } else if (Invbaseuomquantity == openingQty) {
                        out.println("<br> Product  oprning quantity is same with given initial quantity for product" + productCode + " and difrrence is" + (Invbaseuomquantity - openingQty));
                        continue;
                    } else {
                        out.println("<br> Product  oprning quantity is less than given initial quantity for product this is valid data" + productCode + " and difrrence is" + (Invbaseuomquantity - openingQty));
                    }

                } else {

                    out.println("<br> entry not found in inventory table for product" + productCode);
                    continue;
                }

                qtyToUpdate =  openingQty-Invbaseuomquantity;
                String updatequery = "update inventory set quantity=quantity+?,baseuomquantity=baseuomquantity+?,carryin='T',defective='F',newinv='T',updatedate=now(),deleteflag='F' where id=?";
                PreparedStatement stmtquery = conn.prepareStatement(updatequery);
                stmtquery.setDouble(1, qtyToUpdate);
                stmtquery.setDouble(2, qtyToUpdate);
                stmtquery.setString(3, InvId);
                stmtquery.executeUpdate();

                out.println("<br> Product updated with " + qtyToUpdate + " productcode= " + productCode);

                String querybatchid = "select id from newproductbatch where company=? and product= ?  and warehouse=? and location=? ";
                PreparedStatement stmtbatch = conn.prepareStatement(querybatchid);
                stmtbatch.setObject(1, companyId);
                stmtbatch.setObject(2, productId);
                stmtbatch.setObject(3, storeId);
                stmtbatch.setObject(4, locationid);
                ResultSet rsbatch = stmtbatch.executeQuery();
                String batchId = "";
                if (rsbatch.next()) {
                    batchId = rsbatch.getString("id");
                }
                if (!StringUtil.isNullOrEmpty(batchId)) {
                    String queryForupdate = "update newproductbatch set quantity=quantity+?,quantitydue=quantitydue+? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setDouble(1, qtyToUpdate);
                    stmtforUpdate.setDouble(2, qtyToUpdate);
                    stmtforUpdate.setString(3, batchId);
                    stmtforUpdate.setString(4, companyId);
                    stmtforUpdate.executeUpdate();

                    String queryForlocupdate = "update locationbatchdocumentmapping set quantity=quantity+? where  batchmapid=? and documentid=?";
                    PreparedStatement stmtforlocUpdate = conn.prepareStatement(queryForlocupdate);
                    stmtforlocUpdate.setDouble(1, qtyToUpdate);
                    stmtforlocUpdate.setString(2, batchId);
                    stmtforlocUpdate.setString(3, productId);
                    int updatecount = stmtforlocUpdate.executeUpdate();
                    if (updatecount == 0) {
                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                        PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                        stmtlbmap.setString(1, locBatchdocId);
                        stmtlbmap.setDouble(2, openingQty);
                        stmtlbmap.setString(3, productId);
                        stmtlbmap.setString(4, batchId);
                        stmtlbmap.execute();
                        stmtlbmap.close();
                    }
                } else {

                    String batchMapId = java.util.UUID.randomUUID().toString();
                    String querybatch = "insert into newproductbatch (id,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company) values (?,?,?,?,?,28,?,'T','T',?)";
                    PreparedStatement stmtb = conn.prepareStatement(querybatch);
                    stmtb.setString(1, batchMapId);
                    stmtb.setDouble(2, openingQty);
                    stmtb.setDouble(3, openingQty);
                    stmtb.setString(4, locationid); //location
                    stmtb.setString(5, storeId);  //warehouse
                    stmtb.setString(6, productId);
                    stmtb.setString(7, companyId);
                    stmtb.execute();
                    stmtb.close();

                    String locBatchdocId = java.util.UUID.randomUUID().toString();
                    String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                    PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                    stmtlbmap.setString(1, locBatchdocId);
                    stmtlbmap.setDouble(2, openingQty);
                    stmtlbmap.setString(3, productId);
                    stmtlbmap.setString(4, batchMapId);
                    stmtlbmap.execute();
                    stmtlbmap.close();

                }

                String queryStockid = "select id from in_stock where company=? and product= ?  and store=? and location=? ";
                PreparedStatement stmtstock = conn.prepareStatement(queryStockid);
                stmtstock.setObject(1, companyId);
                stmtstock.setObject(2, productId);
                stmtstock.setObject(3, storeId);
                stmtstock.setObject(4, locationid);
                ResultSet rsstock = stmtstock.executeQuery();
                String stockId = "";
                if (rsstock.next()) {
                    stockId = rsstock.getString("id");
                    String queryForupdate = "update in_stock set quantity=quantity+? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setDouble(1, qtyToUpdate);
                    stmtforUpdate.setString(2, stockId);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }
                String querySMid = "select id from in_stockmovement where company=? and store=? and transaction_type=0 and transaction_module=10 and modulerefid=?";
                PreparedStatement stmtsm = conn.prepareStatement(querySMid);
                stmtsm.setObject(1, companyId);
                stmtsm.setObject(2, storeId);
                stmtsm.setObject(3, productId);

                ResultSet rssm = stmtsm.executeQuery();
                String stmId = "";
                if (rssm.next()) {
                    stmId = rssm.getString("id");
                    String queryForupdate = "update in_stockmovement set quantity=quantity+? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setDouble(1, qtyToUpdate);
                    stmtforUpdate.setString(2, stmId);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                    String queryForSMDupdate = "update in_sm_detail set quantity=quantity+? where stockmovement=?";
                    PreparedStatement stmtforSMDUpdate = conn.prepareStatement(queryForSMDupdate);
                    stmtforSMDUpdate.setDouble(1, qtyToUpdate);
                    stmtforSMDUpdate.setString(2, stmId);
                    stmtforSMDUpdate.executeUpdate();
                }




                cnt++;
            }
        }

        int count = cnt;
        StringBuilder result = new StringBuilder("" + count).append(" Records added successfully.");
        out.println(result);
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>
