<%@page import="com.krawler.spring.common.KwlReturnObject"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<!--%@page contentType="text/html" pageEncoding="UTF-8"%>-->
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>



<%
    Connection conn = null;
    try {

        /*
         NOTE : This script has been written specially for some edno related issue ERP-17669.This script is not generic.So according to issue requirements,
         some values have been put directly.
         */
        String serverip = request.getParameter("serverip");//"192.168.0.208";                            
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"newstaging";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        
        String subdomain = "edno";

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        conn.setAutoCommit(false);

        String query = "select companyid,companyname FROM company where subdomain= ? ";

        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        
        int totalproductUpdationCnt = 0;
        int invStockAddCount = 0;
        int totalOpeningCaseAddCnt = 0;
        int productCnt = 0;
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String companyId = rs.getString("companyid");

            /**
             * *******************************************************************************
             *
             * Stock Related Data Correction
             *
             ********************************************************************************
             */
            
            String productFetch = "SELECT id, name FROM product where company='" + companyId + "' and deleteflag='F'";
            PreparedStatement psProductFetch = null;
            psProductFetch = conn.prepareStatement(productFetch);
            ResultSet productList = psProductFetch.executeQuery();
            while (productList.next()) {
                
                //=============== ERP side==================
                
                String productId = productList.getString("id");
                String productName = productList.getString("name");

                String inventoryFetch = "SELECT id, quantity, baseuomquantity FROM inventory where product='" + productId + "' and newinv='T' and carryin='T' and isopening='T'";
                PreparedStatement psInventoryFetch = conn.prepareStatement(inventoryFetch);
                ResultSet inventoryList = psInventoryFetch.executeQuery();
                String firstRecordId = "";
                double quantity = 0;
                double baseuomquantity = 0;
                boolean firstRecordFlag = false;
                while (inventoryList.next()) {
                    String inventoryId = inventoryList.getString("id");
                    if(!firstRecordFlag) {
                        firstRecordId = inventoryId;
                        firstRecordFlag = true;
                    }
                    
                    quantity += inventoryList.getDouble("quantity");
                    baseuomquantity += inventoryList.getDouble("baseuomquantity");
                }
                
                String updateInventory = "UPDATE inventory SET quantity=" + quantity + ", baseuomquantity=" + baseuomquantity + ", updatedate='2015-10-01 00:00:00' WHERE id='" + firstRecordId + "'";//updatedate set to specified date as mentioned in ticket ERP-17669
                PreparedStatement smUpdate = conn.prepareStatement(updateInventory);
                smUpdate.executeUpdate();
                smUpdate.close();
                out.println("<br>Product <b>"+productName+"</b> inventory updated...<br>");
                
                String deleteInventory = "DELETE from inventory where product='" + productId + "' and id !='" + firstRecordId + "' and newinv='T' and carryin='T' and isopening='T'";
                PreparedStatement smdDel = conn.prepareStatement(deleteInventory);
                smdDel.executeUpdate();
                smdDel.close();
                out.println("Product <b>"+productName+"</b> duplicate inventory deleted...<br>");
                
                String newProductBatchFetch = "SELECT id FROM newproductbatch where company='" + companyId + "' and product='" + productId + "' and quantity=0 and quantitydue=0";
                PreparedStatement psNewProductBatchFetch = conn.prepareStatement(newProductBatchFetch);
                ResultSet newProductBatchList = psNewProductBatchFetch.executeQuery();
                while (newProductBatchList.next()) {
                    String newProductBatchId = newProductBatchList.getString("id");
                    
                    String checkUsageInLocationBatchDocumentMapping = "SELECT count(id) from locationbatchdocumentmapping where batchmapid='" + newProductBatchId + "' and documentid != '" + productId + "'";
                    PreparedStatement psUsageFetch = conn.prepareStatement(checkUsageInLocationBatchDocumentMapping);
                    ResultSet usageList = psUsageFetch.executeQuery();
                    while (usageList.next()) {
                        int count = usageList.getInt("count(id)");
                        
                        if(count==0) {
                            String deleteLocationBatch = "DELETE FROM locationbatchdocumentmapping where batchmapid='" + newProductBatchId + "'";
                            PreparedStatement smDelLocationBatch = conn.prepareStatement(deleteLocationBatch);
                            smDelLocationBatch.executeUpdate();
                            smDelLocationBatch.close();
                            out.println("Product <b>"+productName+"</b> zero quantity entry from 'locationbatchdocumentmapping' deleted...<br>");
                            
                            String deleteNewProductBatch = "DELETE FROM newproductbatch where id='" + newProductBatchId + "'";
                            PreparedStatement smDelNewProductBatch = conn.prepareStatement(deleteNewProductBatch);
                            smDelNewProductBatch.executeUpdate();
                            smDelNewProductBatch.close();
                            out.println("Product <b>"+productName+"</b> zero quantity entry from 'newproductbatch' deleted...<br>");
                        }
                    }
                }
                
                //================= Inventory side==================
                
                String deleteIn_Sm_Detail = " DELETE  FROM in_sm_detail WHERE stockmovement IN (SELECT id from in_stockmovement sm WHERE sm.product=?)";
                String deleteIn_Stockmovement = " DELETE  FROM in_stockmovement WHERE product=?";
                String deleteIn_Stock = " DELETE  FROM in_stock WHERE product=?";

                PreparedStatement smDelete = null;

                smDelete = conn.prepareStatement(deleteIn_Sm_Detail);
                smDelete.setString(1, productId);
                smDelete.executeUpdate();
                smDelete.close();

                smDelete = conn.prepareStatement(deleteIn_Stockmovement);
                smDelete.setString(1, productId);
                smDelete.executeUpdate();
                smDelete.close();

                smDelete = conn.prepareStatement(deleteIn_Stock);
                smDelete.setString(1, productId);
                smDelete.executeUpdate();
                smDelete.close();
                
                String stockInsertQry = "select i.updatedate, SUM(b.quantitydue) as qty, b.product, b.warehouse, b.location"
                        + " FROM newproductbatch b "
                        + " INNER JOIN product  p ON  p.id=b.product "
                        + " LEFT JOIN (SELECT product, MAX(updatedate) as updatedate from inventory WHERE company=? GROUP BY product) as i ON i.product=b.product"
                        + " WHERE p.producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa')  AND  b.company=? AND b.product=? "
                        + " GROUP BY b.product,b.warehouse,b.location ";

                PreparedStatement stmtstk = conn.prepareStatement(stockInsertQry);
                stmtstk.setObject(1, companyId);
                stmtstk.setObject(2, companyId);
                stmtstk.setObject(3, productId);
                ResultSet rsstk = stmtstk.executeQuery();

                String InvWarId = "", InvLocId = "";
                while (rsstk.next()) {

                    double Invbaseuomquantity = rsstk.getDouble("qty");
                    if (!StringUtil.isNullOrEmpty(rsstk.getString("warehouse"))) {
                        InvWarId = rsstk.getString("warehouse");
                    }
                    if (!StringUtil.isNullOrEmpty(rsstk.getString("location"))) {
                        InvLocId = rsstk.getString("location");
                    }

                    //Entry of stock on Inventory Side
                    String query2 = "INSERT INTO in_stock(id, product, store, location, batchname, serialnames, company, quantity, createdon, modifiedon) "
                            + " VALUES (UUID(), ?,?,?,?,?,?,?, ?, NOW()) ";
                    PreparedStatement stmtquery2 = conn.prepareStatement(query2);
                    stmtquery2.setString(1, productId);
                    stmtquery2.setString(2, InvWarId);
                    stmtquery2.setString(3, InvLocId);
                    stmtquery2.setString(4, "");
                    stmtquery2.setString(5, null);
                    stmtquery2.setString(6, companyId);
                    stmtquery2.setDouble(7, Invbaseuomquantity);
                    stmtquery2.setObject(8, rsstk.getDate("updatedate") != null ? rsstk.getDate("updatedate") : new java.util.Date());
                    if (Invbaseuomquantity != 0) {
                        stmtquery2.execute();
                        System.out.println("\n" + query2);
                    }

                    stmtquery2.close();
                    invStockAddCount++;
                }
                stmtstk.close();
                totalproductUpdationCnt++;
                
                String InvId = "", newProductId;
                double Invbaseuomquantity = 0.0, price = 0.0;
                String invquery = " SELECT b.warehouse,b.location,lbm.quantity,i.uom,i.updatedate,p.currency,b.product from newproductbatch b "
                        + " INNER JOIN  locationbatchdocumentmapping lbm ON b.id=lbm.batchmapid "
                        + " INNER JOIN product p ON p.id=b.product"
                        + " LEFT JOIN (SELECT product,uom,Min(updatedate) as updatedate from inventory WHERE company=? GROUP BY product) as i ON i.product=b.product"
                        + " WHERE b.company=? AND lbm.documentid=b.product AND b.product=?";

                PreparedStatement stmtinv = conn.prepareStatement(invquery);
                stmtinv.setObject(1, companyId);
                stmtinv.setObject(2, companyId);
                stmtinv.setObject(3, productId);
                ResultSet rsinv = stmtinv.executeQuery();

                while (rsinv.next()) {
                    newProductId = rsinv.getString("product");
                    Invbaseuomquantity = rsinv.getDouble("quantity");

                    if (Invbaseuomquantity != 0) {
                        double productCurrency = 0;
                        productCurrency = rsinv.getDouble("currency");
                        InvWarId = rsinv.getString("warehouse");
                        InvLocId = rsinv.getString("location");
                        java.util.Date updateDate = rsinv.getDate("updatedate") != null ? rsinv.getDate("updatedate") : new java.util.Date();

                        String getProducInitPurchasePriceQry = " select pl1.price as initpurchaseprice from pricelist pl1 where pl1.affecteduser='-1' "
                                + " and pl1.product=? and pl1.carryin='T' and pl1.currency= ? and"
                                + " (pl1.applydate in (select min(pl2.applydate) from pricelist pl2 "
                                + " where pl2.affecteduser='-1' and pl2.product=pl1.product and pl2.currency=? and pl2.carryin=pl1.carryin group by pl2.product)) ";

                        PreparedStatement ppsmt = conn.prepareStatement(getProducInitPurchasePriceQry);
                        ppsmt.setObject(1, newProductId);
                        ppsmt.setObject(2, productCurrency);
                        ppsmt.setObject(3, productCurrency);
                        ResultSet pprs = ppsmt.executeQuery();
                        if (pprs.next()) {
                            price = pprs.getDouble("initpurchaseprice");
                        }

                        String smId = java.util.UUID.randomUUID().toString();
                        String smDetailId = java.util.UUID.randomUUID().toString();

                        String stockMovementQry = "INSERT INTO in_stockmovement(id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                        stmt1.setString(1, smId);
                        stmt1.setString(2, companyId);
                        stmt1.setString(3, newProductId); //product 
                        stmt1.setString(4, InvWarId);
                        stmt1.setString(5, null); // null becoz as per new code it is present in sm detail 
                        stmt1.setString(6, rsinv.getString("uom"));//stockuom 
                        stmt1.setDouble(7, Invbaseuomquantity); //qty 
                        stmt1.setDouble(8, price); //initial purchase price 
                        stmt1.setObject(9, updateDate); //createddon 
                        stmt1.setString(10, null); // transaction no
                        stmt1.setObject(11, updateDate); // transaction date 
                        stmt1.setInt(12, 0); // opening stock :0 , stock In : 1 .Stock out : 2 
                        stmt1.setInt(13, 10); // transaction module // do- 6,grn-5 ,10 -opening/new product 
                        stmt1.setString(14, newProductId); // modulerefid (parent) 
                        stmt1.setString(15, "Stock added through OPENING");
                        stmt1.setString(16, null); // cost center
                        stmt1.setString(17, null); // vendor 
                        stmt1.setString(18, null);// customer 
                        stmt1.execute();
                        stmt1.close();

                        String smDetailQry = "INSERT INTO in_sm_detail (id,stockmovement,location,batchname,serialnames,quantity) VALUES(?,?,?,?,?,?)";
                        PreparedStatement smd = conn.prepareStatement(smDetailQry);
                        smd.setString(1, smDetailId);
                        smd.setString(2, smId);
                        smd.setString(3, InvLocId);
                        smd.setString(4, "");
                        smd.setString(5, null);
                        smd.setDouble(6, Invbaseuomquantity);
                        smd.execute();
                        smd.close();

                        totalOpeningCaseAddCnt++;
                    }

                    out.println("Count Detail : Opening -->" + totalOpeningCaseAddCnt);
                }
                productCnt++;
            }
            out.println("<b><br><br>Product Count Detail : -->" + productCnt);
            psProductFetch.close();
            conn.commit();
        }
        out.println("<br><br> Time :  " + new java.util.Date().toString());
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
        if (conn != null) {
            conn.rollback();
        }
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>