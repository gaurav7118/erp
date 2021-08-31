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
         NOTE : This script has been written specially for some fasten related issues.This script is not generic.So according to issue requirements,
         some values have been put directly.
    
         */
        String serverip = request.getParameter("serverip");//"192.168.0.208";                            
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"newstaging";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
//        String serverip = "localhost";
//        String port = "3306";
//        String dbName = "fastentest2";
//        String userName = "root";
//        String password = "krawler";
        String subdomain = "fasten";

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
//        conn.setAutoCommit(true);

        String query = "select companyid,companyname FROM company where subdomain= ? ";

        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        int totalproductUpdationCnt = 0;
        int prodQtyUpdateCount = 0;
        int invStockAddCount = 0;
        int smCount = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");

            /**
             * *******************************************************************************
             *
             * Stock Related Data Correction
             *
             ********************************************************************************
             */
            if (!StringUtil.isNullOrEmpty(companyId)) {
                //set location,warehouse option for product
                String queryForupdate = "update compaccpreferences set islocationcompulsory='T',iswarehousecompulsory='T' where id=?  ";
                PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                stmtforUpdate.setString(1, companyId);
                stmtforUpdate.executeUpdate();
            }

            String del4 = " DELETE  FROM in_sm_detail WHERE stockmovement IN(SELECT id from in_stockmovement sm WHERE sm.company=? ) ";
            String del5 = " DELETE  FROM in_stockmovement WHERE company=? ";
            String del3 = " DELETE  FROM in_stock WHERE company=? ";

            PreparedStatement smdDel = null;

            smdDel = conn.prepareStatement(del4);
            smdDel.setString(1, companyId);
            smdDel.executeUpdate();
            smdDel.close();

            smdDel = conn.prepareStatement(del5);
            smdDel.setString(1, companyId);
            smdDel.executeUpdate();
            smdDel.close();

            smdDel = conn.prepareStatement(del3);
            smdDel.setString(1, companyId);
            smdDel.executeUpdate();
            smdDel.close();

            String delInvZero = " DELETE from inventory WHERE  company=?  AND baseuomquantity=0";
            smdDel = conn.prepareStatement(delInvZero);
            smdDel.setString(1, companyId);
//            int d2 = smdDel.executeUpdate();
//            out.println("Total deleted zero qty records from inventory is : " + d2 + "<br/><br/>");
            smdDel.close();

            String delBatchZero = " DELETE from newproductbatch WHERE  company=?  AND quantity=0 AND quantitydue=0  ";
            smdDel = conn.prepareStatement(delBatchZero);
            smdDel.setString(1, companyId);
            int d2 = smdDel.executeUpdate();
            out.println("Total deleted zero qty records from newproductbatch is : " + d2 + "<br/><br/>");
            smdDel.close();

            String updateQry1 = " UPDATE product  SET islocationforproduct = 'T', iswarehouseforproduct = 'T' WHERE company = ? ";
            PreparedStatement stmtforUpdate1 = conn.prepareStatement(updateQry1);
            stmtforUpdate1.setString(1, companyId);
            stmtforUpdate1.executeUpdate();

            String InvLocId = "4028818a4e05e809014e1a0d60020105"; // Default Location
            String InvWarId = "4028818a4e05e809014e1a0d5ff80104"; // DS -Default Store

            String updateQry2 = " UPDATE product  SET warehouse=?,location=?  WHERE  company=? AND warehouse IS NULL AND location IS NULL  ";
            PreparedStatement stmtforUpdate2 = conn.prepareStatement(updateQry2);
            stmtforUpdate2.setString(1, InvWarId);
            stmtforUpdate2.setString(2, InvLocId);
            stmtforUpdate2.setString(3, companyId);
            stmtforUpdate2.executeUpdate();

            String batchLocationSet = " UPDATE newproductbatch  SET location=? WHERE company=? AND location IS NULL ";
            stmtforUpdate2 = conn.prepareStatement(batchLocationSet);
            stmtforUpdate2.setString(1, InvLocId);
            stmtforUpdate2.setString(2, companyId);
//            stmtforUpdate2.executeUpdate();

            String batchwarehouseSet = " UPDATE newproductbatch  SET warehouse=? WHERE company=? AND warehouse is NULL";
            stmtforUpdate2 = conn.prepareStatement(batchwarehouseSet);
            stmtforUpdate2.setString(1, InvWarId);
            stmtforUpdate2.setString(2, companyId);
//            stmtforUpdate2.executeUpdate();

            String fetchErrorProdQry = " SELECT id from product WHERE productid IN "
                    + "('CAST-16100-ZP','locware','MAS-552438W5-RS','MAS-DW-6X1-ZC','MAS-NA-05033','MAS-SA-08040A2','MAS-SA-65025A2',"
                    + " 'MAS-SET-12100-YC','MAS-SET-16120-YC','MAS-WA-12100-ZP','MCA-TS-16190-5.8','NEL-SS-13075-5950','NEL-SS-13100-5950', "
                    + " 'NEL-SS-13115-5950','NEL-SS-13150-5950','NEL-SS-16075-5950','NEL-SS-16100-5950','NEL-SS-16125-5400','NEL-SS-16155-5400',"
                    + " 'NEL-SS-16200-5400','NEL-SS-19075-5400','NEL-SS-19100-5400','NEL-SS-19120-5400','NEL-SS-19125-5400','NEL-SS-19150-5400',"
                    + " 'NEL-SS-19200-5400','NEL-SS-22100-5400','NEL-SS-22170-5400','NEL-SS-22200-5400','NEL-SS-25100-5400','NEL-SS-25150-5950',"
                    + " 'NEL-SS-25200-5400','NEL-STD-FR-25','NS','PIA-4025C-ZC','PIA-5019P-SS410PAS') AND id IS NOT NULL ";

//            String fetchErrorProdQry = " SELECT id from product WHERE productid IN "
//                    + "('MAS-552438W5-RS','MAS-DW-6X1-ZC','MAS-NA-05033','MAS-SA-08040A2','MAS-SA-65025A2',"
//                    + " 'MAS-SET-12100-YC','MAS-SET-16120-YC','MAS-WA-12100-ZP','MCA-TS-16190-5.8','NEL-SS-13075-5950','NEL-SS-13100-5950', "
//                    + " 'NEL-SS-13115-5950','NEL-SS-13150-5950','NEL-SS-16075-5950','NEL-SS-16100-5950','NEL-SS-16125-5400','NEL-SS-16155-5400',"
//                    + " 'NEL-SS-16200-5400','NEL-SS-19075-5400','NEL-SS-19100-5400','NEL-SS-19120-5400','NEL-SS-19125-5400','NEL-SS-19150-5400',"
//                    + " 'NEL-SS-19200-5400','NEL-SS-22100-5400','NEL-SS-22170-5400','NEL-SS-22200-5400','NEL-SS-25100-5400','NEL-SS-25150-5950',"
//                    + " 'NEL-SS-25200-5400','NEL-STD-FR-25','NS','PIA-4025C-ZC','PIA-5019P-SS410PAS') AND id IS NOT NULL AND company=? ";
            PreparedStatement errpst = conn.prepareStatement(fetchErrorProdQry);
//            errpst.setString(1, companyId);
            ResultSet errrs = errpst.executeQuery();
            while (errrs.next()) {
                String productId = errrs.getString("id");

                String setZeroNPB = " UPDATE newproductbatch  SET quantity=0,quantitydue=0 WHERE company=? and product=? ";
                stmtforUpdate2 = conn.prepareStatement(setZeroNPB);
                stmtforUpdate2.setString(1, companyId);
                stmtforUpdate2.setString(2, productId);
                stmtforUpdate2.executeUpdate();
                stmtforUpdate2.close();

                String selectBatchId = "SELECT id,batchmapid,quantity from locationbatchdocumentmapping WHERE  documentid=? AND batchmapid IS NOT NULL";
                PreparedStatement pstbatchUpdate = conn.prepareStatement(selectBatchId);
                pstbatchUpdate.setString(1, errrs.getString("id"));
                ResultSet rss1 = pstbatchUpdate.executeQuery();
                while (rss1.next()) {
                    String updateOpeningBatch = " UPDATE newproductbatch  SET quantity=quantity+?,quantitydue=quantitydue+? WHERE id=? ";
                    PreparedStatement btupdt = conn.prepareStatement(updateOpeningBatch);
                    btupdt.setDouble(1, rss1.getDouble("quantity"));
                    btupdt.setDouble(2, rss1.getDouble("quantity"));
                    btupdt.setString(3, rss1.getString("batchmapid"));
                    btupdt.executeUpdate();
                }

                selectBatchId = " SELECT batchmapid,lbm.quantity from locationbatchdocumentmapping lbm INNER JOIN grodetails grd ON grd.id=lbm.documentid "
                        + " WHERE grd.company=? AND grd.product=?";
                PreparedStatement sel1 = conn.prepareStatement(selectBatchId);
                sel1.setString(1, companyId);
                sel1.setString(2, productId);
                rss1 = sel1.executeQuery();
                while (rss1.next()) {
                    String updateOpeningBatch = " UPDATE newproductbatch  SET quantity=quantity+?,quantitydue=quantitydue+? WHERE id=? ";
                    PreparedStatement btupdt = conn.prepareStatement(updateOpeningBatch);
                    btupdt.setDouble(1, rss1.getDouble("quantity"));
                    btupdt.setDouble(2, rss1.getDouble("quantity"));
                    btupdt.setString(3, rss1.getString("batchmapid"));
                    btupdt.executeUpdate();
                }

                selectBatchId = " SELECT batchmapid,lbm.quantity from locationbatchdocumentmapping lbm INNER JOIN dodetails dod ON dod.id=lbm.documentid "
                        + " WHERE dod.company=? AND dod.product=?";
                sel1 = conn.prepareStatement(selectBatchId);
                sel1.setString(1, companyId);
                sel1.setString(2, productId);
                rss1 = sel1.executeQuery();
                while (rss1.next()) {
                    String updateOpeningBatch = " UPDATE newproductbatch  SET quantitydue=quantitydue-? WHERE id=? ";
                    PreparedStatement btupdt = conn.prepareStatement(updateOpeningBatch);
                    btupdt.setDouble(1, rss1.getDouble("quantity"));
                    btupdt.setString(2, rss1.getString("batchmapid"));
                    btupdt.executeUpdate();
                }

            }

            String queryproduct = "select id from product where company=? AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa')"; //product type = assembly,inventory part
            PreparedStatement stmtp = conn.prepareStatement(queryproduct);  //select all product from company
            stmtp.setObject(1, companyId);
            ResultSet rsp = stmtp.executeQuery();
            rsp.last();
            out.println(rsp.getRow() + "  Total Products ");
            rsp.beforeFirst();

            while (rsp.next()) {
                String productid = "";
                productid = rsp.getString("id");

                String stockInsertQry = " select i.updatedate,SUM(b.quantitydue) as qty ,b.product,b.warehouse,b.location"
                        + " FROM newproductbatch b "
                        + " INNER JOIN product  p ON  p.id=b.product "
                        + " LEFT JOIN (SELECT product,MAX(updatedate) as updatedate from inventory WHERE company=? GROUP BY product) as i ON i.product=b.product"
                        + " WHERE p.producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa')  AND  b.company=? AND b.product=? "
                        + " GROUP BY b.product,b.warehouse,b.location ";

                PreparedStatement stmtstk = conn.prepareStatement(stockInsertQry);
                stmtstk.setObject(1, companyId);
                stmtstk.setObject(2, companyId);
                stmtstk.setObject(3, productid);
                ResultSet rsstk = stmtstk.executeQuery();

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
                    stmtquery2.setString(1, productid);
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
            }

            /**
             * *******************************************************************************
             *
             * Stock Movement Related Data Entry according to transactions
             *
             ********************************************************************************
             */
            int totalOpeningCaseAddCnt = 0;
            int totalDOAddCnt = 0;
            int totalGRNAddCnt = 0;
            int totalSADJAddCnt = 0;
            int totalPRAddCnt = 0;
            int totalSRAddCnt = 0;

            /**
             * *************************************
             * Opening Case ********************************
             */
            String InvId = "", productId;
            double Invbaseuomquantity = 0.0, price = 0.0;
            String invquery = " SELECT b.warehouse,b.location,lbm.quantity,i.uom,i.updatedate,p.currency,b.product from newproductbatch b "
                    + " INNER JOIN  locationbatchdocumentmapping lbm ON b.id=lbm.batchmapid "
                    + " INNER JOIN product p ON p.id=b.product"
                    + " LEFT JOIN (SELECT product,uom,Min(updatedate) as updatedate from inventory WHERE company=? GROUP BY product) as i ON i.product=b.product"
                    + " WHERE b.company=? AND lbm.documentid=b.product";

            PreparedStatement stmtinv = conn.prepareStatement(invquery);
            stmtinv.setObject(1, companyId);
            stmtinv.setObject(2, companyId);
            ResultSet rsinv = stmtinv.executeQuery();

            while (rsinv.next()) {
                productId = rsinv.getString("product");
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
                    ppsmt.setObject(1, productId);
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
                    stmt1.setString(3, productId); //product 
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
                    stmt1.setString(14, productId); // modulerefid (parent) 
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
                
                System.out.println("Count Detail : Opening -->" + totalOpeningCaseAddCnt + " GRN --> " + totalGRNAddCnt + " DO -->" + totalDOAddCnt + " PR --> " + totalPRAddCnt + " SR --> " + totalSRAddCnt);

            }

            /**
             * *************************************
             * For : GRN Stock Movement Entry **********************************
             */
            String grnQry = "SELECT gr.id,gr.gronumber,grd.product,grd.uom,grd.baseuomdeliveredquantity,grd.baseuomrate,gr.grorderdate,gr.costcenter,gr.vendor, b.warehouse,b.location,FROM_UNIXTIME(gr.createdon/1000) as createdondate,(baseuomdeliveredquantity*baseuomrate) as price "
                    + " from grorder gr "
                    + " INNER JOIN grodetails grd ON gr.id=grd.grorder "
                    + " INNER JOIN locationbatchdocumentmapping  lbm ON  lbm.documentid=grd.id"
                    + " INNER JOIN newproductbatch b ON b.id=lbm.batchmapid"
                    + " WHERE  gr.company=?  and gr.isfixedassetgro=0 "
                    + "  AND grd.product IN(select id from product where company=?  AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa')) ";

            PreparedStatement stmtgrn = conn.prepareStatement(grnQry);
            stmtgrn.setObject(1, companyId);
            stmtgrn.setObject(2, companyId);
            ResultSet rs1 = stmtgrn.executeQuery();
            while (rs1.next()) {
                String smId = java.util.UUID.randomUUID().toString();
                String smDetailId = java.util.UUID.randomUUID().toString();
                InvWarId = rs1.getString("warehouse");
                InvLocId = rs1.getString("location");
                double qtty = rs1.getDouble("baseuomdeliveredquantity");

                if (qtty != 0) {
                    String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                    stmt1.setString(1, smId);
                    stmt1.setString(2, companyId);
                    stmt1.setString(3, rs1.getString("product")); //product
                    stmt1.setString(4, InvWarId);
                    stmt1.setString(5, null);
                    stmt1.setString(6, rs1.getString("uom"));//stockuom
                    stmt1.setDouble(7, qtty); //qty
                    stmt1.setDouble(8, rs1.getDouble("baseuomrate")); //priceperunit
                    stmt1.setObject(9, rs1.getDate("createdondate") != null ? rs1.getDate("createdondate") : new java.util.Date()); //createdon
                    stmt1.setString(10, rs1.getString("gronumber")); // transaction no
                    stmt1.setObject(11, rs1.getDate("grorderdate") != null ? rs1.getDate("grorderdate") : new java.util.Date()); // transaction date
                    stmt1.setInt(12, 1); // stock In : 1 .Stock out : 2
                    stmt1.setInt(13, 5); // transaction module // do- 6,grn-5
                    stmt1.setString(14, rs1.getString("id")); // modulerefid (parent) 
                    stmt1.setString(15, "Stock added through GR");
                    stmt1.setString(16, rs1.getString("costcenter"));
                    stmt1.setString(17, rs1.getString("vendor"));  // vendor
                    stmt1.setString(18, null); // customer
                    stmt1.execute();
                    stmt1.close();

                    String smDetailQry = "INSERT INTO in_sm_detail (id,stockmovement,location,batchname,serialnames,quantity) VALUES(?,?,?,?,?,?)";
                    PreparedStatement smd = conn.prepareStatement(smDetailQry);
                    smd.setString(1, smDetailId);
                    smd.setString(2, smId);
                    smd.setString(3, InvLocId);
                    smd.setString(4, "");
                    smd.setString(5, null);
                    smd.setDouble(6, qtty);
                    smd.execute();
                    smd.close();

                    totalGRNAddCnt++;
                }
                System.out.println("Count Detail  :  Opening -->" + totalOpeningCaseAddCnt + "    GRN -->  " + totalGRNAddCnt + "  DO -->  " + totalDOAddCnt + "   PR -->  " + totalPRAddCnt + "  SR --> " + totalSRAddCnt);
            }

            /**
             * *************************************
             * For : DO Stock Movement Entry **********************************
             */
            String doQry = " SELECT dor.id,dor.donumber,dodtl.product,dodtl.uom,dodtl.baseuomdeliveredquantity,dodtl.baseuomrate,dor.orderdate,dor.costcenter,dor.customer, b.warehouse,b.location,FROM_UNIXTIME(dor.createdon/1000) as createdondate,(baseuomdeliveredquantity*baseuomrate) as price  "
                    + " FROM deliveryorder dor INNER JOIN dodetails dodtl ON dor.id=dodtl.deliveryorder "
                    + " INNER JOIN locationbatchdocumentmapping  lbm ON  lbm.documentid=dodtl.id"
                    + " INNER JOIN newproductbatch b ON b.id=lbm.batchmapid"
                    + " WHERE dor.company=?   AND dor.isfixedassetdo=0 "
                    + " AND dodtl.product IN(select id from product where company=?   AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa'))";
            PreparedStatement stmtdo = conn.prepareStatement(doQry);
            stmtdo.setObject(1, companyId);
            stmtdo.setObject(2, companyId);
            ResultSet rs2 = stmtdo.executeQuery();
            while (rs2.next()) {
                String smId = java.util.UUID.randomUUID().toString();
                String smDetailId = java.util.UUID.randomUUID().toString();
                double qtty = rs2.getDouble("baseuomdeliveredquantity");

                if (qtty != 0) {
                    String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                    stmt1.setString(1, smId);
                    stmt1.setString(2, companyId);
                    stmt1.setString(3, rs2.getString("product")); //product
                    stmt1.setString(4, InvWarId);
                    stmt1.setString(5, null);
                    stmt1.setString(6, rs2.getString("uom"));//stockuom
                    stmt1.setDouble(7, qtty); //qty
                    stmt1.setDouble(8, rs2.getDouble("baseuomrate")); //priceperunit
                    stmt1.setObject(9, rs2.getDate("createdondate") != null ? rs2.getDate("createdondate") : new java.util.Date()); //createdon
                    stmt1.setString(10, rs2.getString("donumber")); // transaction no
                    stmt1.setObject(11, rs2.getDate("orderdate") != null ? rs2.getDate("orderdate") : new java.util.Date()); // transaction date
                    stmt1.setInt(12, 2); // stock In : 1 .Stock out : 2
                    stmt1.setInt(13, 6); // transaction module // do- 6,grn-5
                    stmt1.setString(14, rs2.getString("id")); // modulerefid (parent) 
                    stmt1.setString(15, "Stock out through DO");
                    stmt1.setString(16, rs2.getString("costcenter"));
                    stmt1.setString(17, null);  // vendor
                    stmt1.setString(18, rs2.getString("customer")); // customer
                    stmt1.execute();
                    stmt1.close();

                    String smDetailQry = "INSERT INTO in_sm_detail (id,stockmovement,location,batchname,serialnames,quantity) VALUES(?,?,?,?,?,?)";
                    PreparedStatement smd = conn.prepareStatement(smDetailQry);
                    smd.setString(1, smDetailId);
                    smd.setString(2, smId);
                    smd.setString(3, InvLocId);
                    smd.setString(4, "");
                    smd.setString(5, null);
                    smd.setDouble(6, qtty);
                    smd.execute();
                    smd.close();

                    totalDOAddCnt++;
                }

                System.out.println("Count Detail  :  Opening -->" + totalOpeningCaseAddCnt + "    GRN -->  " + totalGRNAddCnt + "  DO -->  " + totalDOAddCnt + "   PR -->  " + totalPRAddCnt + "  SR --> " + totalSRAddCnt);
            }

            /**
             * *************************************
             * For : Purchase Return Stock Movement Entry
             * **********************************
             */
            String prQry = " select pr.id,pr.prnumber,prd.product,prd.uom,prd.baseuomquantity,prd.baseuomrate,pr.orderdate,pr.costcenter,pr.vendor, b.warehouse,b.location,FROM_UNIXTIME(pr.createdon/1000) as createdondate,(baseuomquantity*baseuomrate) as price "
                    + "  from purchasereturn pr INNER JOIN prdetails prd ON pr.id=prd.purchasereturn "
                    + " INNER JOIN locationbatchdocumentmapping  lbm ON  lbm.documentid=prd.id"
                    + " INNER JOIN newproductbatch b ON b.id=lbm.batchmapid"
                    + " WHERE prd.company=?  AND pr.isfixedasset=0 "
                    + " AND prd.product IN(select id from product where company=?  AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa')) ";

            PreparedStatement stmtpr = conn.prepareStatement(prQry);
            stmtpr.setObject(1, companyId);
            stmtpr.setObject(2, companyId);
            ResultSet rspr = stmtpr.executeQuery();
            while (rspr.next()) {
                String smId = java.util.UUID.randomUUID().toString();
                String smDetailId = java.util.UUID.randomUUID().toString();
                double qtty = rspr.getDouble("baseuomquantity");

                if (qtty != 0) {
                    String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                    stmt1.setString(1, smId);
                    stmt1.setString(2, companyId);
                    stmt1.setString(3, rspr.getString("product")); //product
                    stmt1.setString(4, InvWarId);
                    stmt1.setString(5, null);
                    stmt1.setString(6, rspr.getString("uom"));//stockuom
                    stmt1.setDouble(7, qtty); //qty
                    stmt1.setDouble(8, rspr.getDouble("baseuomrate")); //priceperunit
                    stmt1.setObject(9, rspr.getDate("createdondate") != null ? rspr.getDate("createdondate") : new java.util.Date()); //createdon
                    stmt1.setString(10, rspr.getString("prnumber")); // transaction no
                    stmt1.setObject(11, rspr.getDate("orderdate") != null ? rspr.getDate("orderdate") : new java.util.Date()); // transaction date
                    stmt1.setInt(12, 2); // stock In : 1 .Stock out : 2
                    stmt1.setInt(13, 8); // transaction module // do- 6,grn-5
                    stmt1.setString(14, rspr.getString("id")); // modulerefid (parent) 
                    stmt1.setString(15, "Stock out through Purchase Return");
                    stmt1.setString(16, rspr.getString("costcenter"));
                    stmt1.setString(17, rspr.getString("vendor"));  // vendor
                    stmt1.setString(18, null); // customer
                    stmt1.execute();
                    stmt1.close();

                    String smDetailQry = "INSERT INTO in_sm_detail (id,stockmovement,location,batchname,serialnames,quantity) VALUES(?,?,?,?,?,?)";
                    PreparedStatement smd = conn.prepareStatement(smDetailQry);
                    smd.setString(1, smDetailId);
                    smd.setString(2, smId);
                    smd.setString(3, InvLocId);
                    smd.setString(4, "");
                    smd.setString(5, null);
                    smd.setDouble(6, qtty);
                    smd.execute();
                    smd.close();

                    totalPRAddCnt++;
                }
                System.out.println("Count Detail  :  Opening -->" + totalOpeningCaseAddCnt + "    GRN -->  " + totalGRNAddCnt + "  DO -->  " + totalDOAddCnt + "   PR -->  " + totalPRAddCnt + "  SR --> " + totalSRAddCnt);
            }

            /**
             * *************************************
             * For : Sales Return Stock Movement Entry
             * **********************************
             */
            String srQry = " SELECT slr.id,slr.srnumber,slrd.product,slrd.uom,slrd.baseuomquantity,slrd.baseuomrate,slr.orderdate,slr.costcenter,slr.customer, b.warehouse,b.location,FROM_UNIXTIME(slr.createdon/1000) as createdondate,(baseuomquantity*baseuomrate) as price "
                    + " FROM salesreturn slr INNER JOIN srdetails slrd ON slr.id=slrd.salesreturn "
                    + " INNER JOIN locationbatchdocumentmapping  lbm ON  lbm.documentid=slrd.id"
                    + " INNER JOIN newproductbatch b ON b.id=lbm.batchmapid"
                    + " WHERE slr.company=?   AND slr.isfixedasset=0 "
                    + " AND slrd.product IN(select id from product where company=?   AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa'))";
            PreparedStatement stmtsr = conn.prepareStatement(srQry);
            stmtsr.setObject(1, companyId);
            stmtsr.setObject(2, companyId);
            ResultSet rssr = stmtsr.executeQuery();
            while (rssr.next()) {
                String smId = java.util.UUID.randomUUID().toString();
                String smDetailId = java.util.UUID.randomUUID().toString();
                double qtty = rssr.getDouble("baseuomquantity");

                if (qtty != 0) {
                    String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                    stmt1.setString(1, smId);
                    stmt1.setString(2, companyId);
                    stmt1.setString(3, rssr.getString("product")); //product
                    stmt1.setString(4, InvWarId);
                    stmt1.setString(5, null);
                    stmt1.setString(6, rssr.getString("uom"));//stockuom
                    stmt1.setDouble(7, qtty); //qty
                    stmt1.setDouble(8, rssr.getDouble("baseuomrate")); //priceperunit
                    stmt1.setObject(9, rssr.getDate("createdondate") != null ? rssr.getDate("createdondate") : new java.util.Date()); //createdon
                    stmt1.setString(10, rssr.getString("srnumber")); // transaction no
                    stmt1.setObject(11, rssr.getDate("orderdate") != null ? rssr.getDate("orderdate") : new java.util.Date()); // transaction date
                    stmt1.setInt(12, 1); // stock In : 1 .Stock out : 2
                    stmt1.setInt(13, 9); // transaction module // do- 6,grn-5 ,salesreturn
                    stmt1.setString(14, rssr.getString("id")); // modulerefid (parent) 
                    stmt1.setString(15, "Stock added through Sales Return");
                    stmt1.setString(16, rssr.getString("costcenter"));
                    stmt1.setString(17, null);  // vendor
                    stmt1.setString(18, rssr.getString("customer")); // customer
                    stmt1.execute();
                    stmt1.close();

                    String smDetailQry = "INSERT INTO in_sm_detail (id,stockmovement,location,batchname,serialnames,quantity) VALUES(?,?,?,?,?,?)";
                    PreparedStatement smd = conn.prepareStatement(smDetailQry);
                    smd.setString(1, smDetailId);
                    smd.setString(2, smId);
                    smd.setString(3, InvLocId);
                    smd.setString(4, "");
                    smd.setString(5, null);
                    smd.setDouble(6, qtty);
                    smd.execute();
                    smd.close();

                    totalSRAddCnt++;
                }
                System.out.println("Count Detail  :  Opening -->" + totalOpeningCaseAddCnt + "    GRN -->  " + totalGRNAddCnt + "  DO -->  " + totalDOAddCnt + "   PR -->  " + totalPRAddCnt + "  SR --> " + totalSRAddCnt);
            }
            totalCompanyUpdationCnt++;
            out.println("<br><br> Total products Added in Inventory are " + invStockAddCount);
            out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
            out.println("<br><br> Total Opening Entries added in Stock Movement are " + totalOpeningCaseAddCnt);
            out.println("<br><br> Total GRN Entries added in Stock Movement are " + totalGRNAddCnt);
            out.println("<br><br> Total DO Entries added in Stock Movement are " + totalDOAddCnt);
            out.println("<br><br> Total PR Entries added in Stock Movement are " + totalPRAddCnt);
            out.println("<br><br> Total SR Entries added in Stock Movement are " + totalSRAddCnt);
        }
        out.println("<br><br> Time :  " + new java.util.Date().toString());
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
        if (conn != null) {
//            conn.rollback();
        }
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>