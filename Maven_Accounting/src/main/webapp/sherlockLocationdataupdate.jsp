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
        String subdomain = "sherlock";

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

            String InvLocId = "5ff44e20-6d50-4b57-a5b9-34d6f35c39a9"; // Default Location
            String InvWarId = "";

            String batchLocationSet = " UPDATE newproductbatch  SET location=? WHERE company=? AND location IS NULL ";  //Location Update
            PreparedStatement stmtforUpdate2 = conn.prepareStatement(batchLocationSet);
            stmtforUpdate2.setString(1, InvLocId);
            stmtforUpdate2.setString(2, companyId);
            stmtforUpdate2.executeUpdate();

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

            totalCompanyUpdationCnt++;
            out.println("<br><br> Total products Added in Inventory are " + invStockAddCount);
            out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
            out.println("<br><br> Total Opening Entries added in Stock Movement are " + totalOpeningCaseAddCnt);
            out.println("<br><br> Total GRN Entries added in Stock Movement are " + totalGRNAddCnt);
            out.println("<br><br> Total DO Entries added in Stock Movement are " + totalDOAddCnt);
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