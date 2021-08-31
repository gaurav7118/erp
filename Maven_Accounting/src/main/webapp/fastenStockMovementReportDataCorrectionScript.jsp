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
        String subdomain = "fastenhardware";

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname FROM company where subdomain= ?";

        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        int totalproductUpdationCnt = 0;
        int totalOpeningCaseAddCnt = 0;
        int totalDOAddCnt = 0;
        int totalGRNAddCnt = 0;
        int totalSADJAddCnt = 0;
        int totalPRAddCnt = 0;
        int totalSRAddCnt = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");

            String InvLocId = "";
            String queryinvid = "select id from in_location where company=? and isdefault=1 ";  //is default location availble
            PreparedStatement stmt4 = conn.prepareStatement(queryinvid);
            stmt4.setObject(1, companyId);
            ResultSet rs4 = stmt4.executeQuery();
            if (rs4.next()) {
                InvLocId = rs4.getString("id");
            }

            String InvWarId = "";
            String queryWarid = "select id from in_storemaster where company=? and isdefault=1 "; //is default warehouse availble
            PreparedStatement stmt5 = conn.prepareStatement(queryWarid);
            stmt5.setObject(1, companyId);
            ResultSet rs5 = stmt5.executeQuery();
            if (rs5.next()) {
                InvWarId = rs5.getString("id");
            }

            String del1 = "DELETE FROM in_sm_detail WHERE stockmovement IN(SELECT id FROM in_stockmovement WHERE product IN(SELECT  product FROM inventory where newinv='T' AND  isopening='T' AND company=?  AND  baseuomquantity=0)  AND transaction_module=10)";
            String del2 = "DELETE  FROM in_stockmovement WHERE product IN(SELECT  product FROM inventory WHERE newinv='T' AND  isopening='T' AND company=?  AND  baseuomquantity=0)  AND transaction_module=10";
//            String del3 = "DELETE FROM in_sm_detail WHERE stockmovement IN(SELECT  id FROM in_stockmovement WHERE company=? AND remark IN ('Stock added through OPENING','Stock added through GR','Stock out through DO','Stock added from Product Build','Stock out through Product Build','Stock added through Sales Return','Stock out through Purchase Return'))";
            String del3 = "DELETE  from in_sm_detail where stockmovement IN(SELECT id from in_stockmovement WHERE company=? and transaction_module != 3)";
//            String del4 = "DELETE FROM in_stockmovement WHERE company=? AND remark IN ('Stock added through OPENING','Stock added through GR','Stock out through DO','Stock added from Product Build','Stock out through Product Build','Stock added through Sales Return','Stock out through Purchase Return')";
            String del4 = "DELETE from in_stockmovement WHERE company=? and transaction_module != 3";

            PreparedStatement smdDel = conn.prepareStatement(del1);
            smdDel.setString(1, companyId);
            smdDel.executeUpdate();
            smdDel.close();

            smdDel = conn.prepareStatement(del2);
            smdDel.setString(1, companyId);
            smdDel.executeUpdate();
            smdDel.close();

            smdDel = conn.prepareStatement(del3);
            smdDel.setString(1, companyId);
            smdDel.executeUpdate();
            smdDel.close();

            smdDel = conn.prepareStatement(del4);
            smdDel.setString(1, companyId);
            smdDel.executeUpdate();
            smdDel.close();

            /**
             * *************************************
             * Opening Case *********************************
             */
            String InvId = "";
            double Invbaseuomquantity = 0.0, Invquantity = 0.0, price = 0.0;
            String invquery = "select * from inventory where company=? and product IN(select id from product where company=? AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa')) and newinv='T' and isopening='T' and baseuomquantity>0 and id NOT IN (select modulerefid from in_stockmovement where company=? )";
            PreparedStatement stmtinv = conn.prepareStatement(invquery);
            stmtinv.setObject(1, companyId);
            stmtinv.setObject(2, companyId);
            stmtinv.setObject(3, companyId);
            ResultSet rsinv = stmtinv.executeQuery();
            while (rsinv.next()) {
                InvId = rsinv.getString("id");
                Invquantity = rsinv.getDouble("quantity");
                Invbaseuomquantity = rsinv.getDouble("baseuomquantity");
                price = rsinv.getDouble("baseuomrate") * Invbaseuomquantity;

                if (!StringUtil.isNullOrEmpty(InvId)) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse

                    String smId = java.util.UUID.randomUUID().toString();
                    String smDetailId = java.util.UUID.randomUUID().toString();

                    String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                    stmt1.setString(1, smId);
                    stmt1.setString(2, companyId);
                    stmt1.setString(3, rsinv.getString("product")); //product
                    stmt1.setString(4, InvWarId);
                    stmt1.setString(5, null); // null becoz as per new code it is present in sm detail
                    stmt1.setString(6, rsinv.getString("uom"));//stockuom
                    stmt1.setDouble(7, Invbaseuomquantity); //qty
                    stmt1.setDouble(8, price); //priceperunit
                    stmt1.setDate(9, rsinv.getDate("updatedate")); //createdon
                    stmt1.setString(10, null); // transaction no
                    stmt1.setDate(11, rsinv.getDate("updatedate")); // transaction date
                    stmt1.setInt(12, 0); // opening stock :0 , stock In : 1 .Stock out : 2
                    stmt1.setInt(13, 10); // transaction module // do- 6,grn-5 ,10 -opening/new product
                    stmt1.setString(14, InvId); // modulerefid (parent) 
                    stmt1.setString(15, "Stock added through OPENING");
                    stmt1.setString(16, null);  // cost center
                    stmt1.setString(17, null);  // vendor
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
                    smd.setDouble(6, Invbaseuomquantity);
                    smd.execute();
                    smd.close();

                    totalOpeningCaseAddCnt++;

                    System.out.println("Count Detail  :  Opening -->" + totalOpeningCaseAddCnt + "    GRN -->  " + totalGRNAddCnt + "  DO -->  " + totalDOAddCnt + "   PR -->  " + totalPRAddCnt + "  SR --> " + totalSRAddCnt);

                }
            }

            /**
             * *************************************
             * For : GRN Stock Movement Entry **********************************
             */
            String grnQry = "SELECT gr.*,grd.*,FROM_UNIXTIME(gr.createdon/1000) as createdondate,(baseuomdeliveredquantity*baseuomrate) as price from grorder gr INNER JOIN grodetails grd ON gr.id=grd.grorder  WHERE  gr.company=? and gr.isfixedassetgro=0 AND grd.product IN(select id from product where company=? AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa')) AND gr.id  NOT IN (SELECT modulerefid from in_stockmovement WHERE company=?)"; // only for non asset type gro
            PreparedStatement stmtgrn = conn.prepareStatement(grnQry);
            stmtgrn.setObject(1, companyId);
            stmtgrn.setObject(2, companyId);
            stmtgrn.setObject(3, companyId);
            ResultSet rs1 = stmtgrn.executeQuery();
            while (rs1.next()) {
                String smId = java.util.UUID.randomUUID().toString();
                String smDetailId = java.util.UUID.randomUUID().toString();

                String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                stmt1.setString(1, smId);
                stmt1.setString(2, companyId);
                stmt1.setString(3, rs1.getString("product")); //product
                stmt1.setString(4, InvWarId);
                stmt1.setString(5, null);
                stmt1.setString(6, rs1.getString("uom"));//stockuom
                stmt1.setDouble(7, rs1.getDouble("baseuomdeliveredquantity")); //qty
                stmt1.setDouble(8, rs1.getDouble("rate")); //priceperunit
                stmt1.setDate(9, rs1.getDate("createdondate")); //createdon
                stmt1.setString(10, rs1.getString("gronumber")); // transaction no
                stmt1.setDate(11, rs1.getDate("grorderdate")); // transaction date
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
                smd.setDouble(6, rs1.getDouble("baseuomdeliveredquantity"));
                smd.execute();
                smd.close();

                totalGRNAddCnt++;

                System.out.println("Count Detail  :  Opening -->" + totalOpeningCaseAddCnt + "    GRN -->  " + totalGRNAddCnt + "  DO -->  " + totalDOAddCnt + "   PR -->  " + totalPRAddCnt + "  SR --> " + totalSRAddCnt);
            }

            /**
             * *************************************
             * For : DO Stock Movement Entry **********************************
             */
            String doQry = "SELECT dor.*,dodtl.*,FROM_UNIXTIME(dor.createdon/1000) as createdondate,(baseuomdeliveredquantity*baseuomrate) as price FROM deliveryorder dor INNER JOIN dodetails dodtl ON dor.id=dodtl.deliveryorder WHERE dor.company=?  AND dor.isfixedassetdo=0 AND dodtl.product IN(select id from product where company=? AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa')) AND dor.id NOT IN (SELECT modulerefid from in_stockmovement WHERE company=?)"; // only for non asset type do
            PreparedStatement stmtdo = conn.prepareStatement(doQry);
            stmtdo.setObject(1, companyId);
            stmtdo.setObject(2, companyId);
            stmtdo.setObject(3, companyId);
            ResultSet rs2 = stmtdo.executeQuery();
            while (rs2.next()) {
                String smId = java.util.UUID.randomUUID().toString();
                String smDetailId = java.util.UUID.randomUUID().toString();

                String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                stmt1.setString(1, smId);
                stmt1.setString(2, companyId);
                stmt1.setString(3, rs2.getString("product")); //product
                stmt1.setString(4, InvWarId);
                stmt1.setString(5, null);
                stmt1.setString(6, rs2.getString("uom"));//stockuom
                stmt1.setDouble(7, rs2.getDouble("baseuomdeliveredquantity")); //qty
                stmt1.setDouble(8, rs2.getDouble("rate")); //priceperunit
                stmt1.setDate(9, rs2.getDate("createdondate")); //createdon
                stmt1.setString(10, rs2.getString("donumber")); // transaction no
                stmt1.setDate(11, rs2.getDate("orderdate")); // transaction date
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
                smd.setDouble(6, rs2.getDouble("baseuomdeliveredquantity"));
                smd.execute();
                smd.close();

                totalDOAddCnt++;

                System.out.println("Count Detail  :  Opening -->" + totalOpeningCaseAddCnt + "    GRN -->  " + totalGRNAddCnt + "  DO -->  " + totalDOAddCnt + "   PR -->  " + totalPRAddCnt + "  SR --> " + totalSRAddCnt);
            }

            /**
             * *************************************
             * For : Purchase Return Stock Movement Entry
             * **********************************
             */
            String prQry = "select pr.*,prd.*,FROM_UNIXTIME(pr.createdon/1000) as createdondate,(baseuomquantity*baseuomrate) as price  from purchasereturn pr INNER JOIN prdetails prd ON pr.id=prd.purchasereturn WHERE prd.company=?  AND pr.isfixedasset=0 AND prd.product IN(select id from product where company=? AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa'))  AND pr.id NOT IN(SELECT modulerefid from in_stockmovement WHERE company=? )"; // only for non asset type do
            PreparedStatement stmtpr = conn.prepareStatement(prQry);
            stmtpr.setObject(1, companyId);
            stmtpr.setObject(2, companyId);
            stmtpr.setObject(3, companyId);
            ResultSet rspr = stmtpr.executeQuery();
            while (rspr.next()) {
                String smId = java.util.UUID.randomUUID().toString();
                String smDetailId = java.util.UUID.randomUUID().toString();

                String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                stmt1.setString(1, smId);
                stmt1.setString(2, companyId);
                stmt1.setString(3, rspr.getString("product")); //product
                stmt1.setString(4, InvWarId);
                stmt1.setString(5, null);
                stmt1.setString(6, rspr.getString("uom"));//stockuom
                stmt1.setDouble(7, rspr.getDouble("baseuomquantity")); //qty
                stmt1.setDouble(8, rspr.getDouble("rate")); //priceperunit
                stmt1.setDate(9, rspr.getDate("createdondate")); //createdon
                stmt1.setString(10, rspr.getString("prnumber")); // transaction no
                stmt1.setDate(11, rspr.getDate("orderdate")); // transaction date
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
                smd.setDouble(6, rspr.getDouble("baseuomquantity"));
                smd.execute();
                smd.close();

                totalPRAddCnt++;

                System.out.println("Count Detail  :  Opening -->" + totalOpeningCaseAddCnt + "    GRN -->  " + totalGRNAddCnt + "  DO -->  " + totalDOAddCnt + "   PR -->  " + totalPRAddCnt + "  SR --> " + totalSRAddCnt);
            }

            /**
             * *************************************
             * For : Sales Return Stock Movement Entry
             * **********************************
             */
            String srQry = "SELECT slr.*,slrd.*,FROM_UNIXTIME(slr.createdon/1000) as createdondate,(baseuomquantity*baseuomrate) as price from salesreturn slr INNER JOIN srdetails slrd ON slr.id=slrd.salesreturn WHERE slrd.company=?  AND slr.isfixedasset=0 AND slrd.product IN(select id from product where company=? AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa'))  AND slr.id NOT IN(SELECT modulerefid from in_stockmovement WHERE company=? )"; // only for non asset type gro
            PreparedStatement stmtsr = conn.prepareStatement(srQry);
            stmtsr.setObject(1, companyId);
            stmtsr.setObject(2, companyId);
            stmtsr.setObject(3, companyId);
            ResultSet rssr = stmtsr.executeQuery();
            while (rssr.next()) {
                String smId = java.util.UUID.randomUUID().toString();
                String smDetailId = java.util.UUID.randomUUID().toString();

                String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                stmt1.setString(1, smId);
                stmt1.setString(2, companyId);
                stmt1.setString(3, rssr.getString("product")); //product
                stmt1.setString(4, InvWarId);
                stmt1.setString(5, null);
                stmt1.setString(6, rssr.getString("uom"));//stockuom
                stmt1.setDouble(7, rssr.getDouble("baseuomquantity")); //qty
                stmt1.setDouble(8, rssr.getDouble("rate")); //priceperunit
                stmt1.setDate(9, rssr.getDate("createdondate")); //createdon
                stmt1.setString(10, rssr.getString("srnumber")); // transaction no
                stmt1.setDate(11, rssr.getDate("orderdate")); // transaction date
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
                smd.setDouble(6, rssr.getDouble("baseuomquantity"));
                smd.execute();
                smd.close();

                totalSRAddCnt++;
                System.out.println("Count Detail  :  Opening -->" + totalOpeningCaseAddCnt + "    GRN -->  " + totalGRNAddCnt + "  DO -->  " + totalDOAddCnt + "   PR -->  " + totalPRAddCnt + "  SR --> " + totalSRAddCnt);
            }

            // -----------------------------------------------------------------------------------------------------------------------------------------------------------------
            /*  Note :  1) Stock Adjustment entries are not added in stockmovement because those data are correct there.If there is needed to add in future following code with 
                        some modification will ve useful .
                        2)Product build entries also are not added as fasten has no trnsactions currently for this.if in future it is needed to add following code will be useful.
            */
            // ----------------------------------------------------------------------------------------------------------------------------------------------------------------- 
            
            
            /**
             * *************************************
             * For : Stock Adjustment Stock Movement Entry
             * **********************************
             */
            /*          String sAdjQry = "SELECT * from in_stockadjustment sadj INNER JOIN in_sa_detail sadjd ON sadj.id=sadjd.stockadjustment WHERE sadj.company=? AND sadj.product IN(select id from product where company=? AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa'))  AND sadj.id NOT IN(SELECT modulerefid from in_stockmovement WHERE company=? )"; // only for non asset type gro
             PreparedStatement stmtsadj = conn.prepareStatement(sAdjQry);
             stmtsadj.setObject(1, companyId);
             stmtsadj.setObject(2, companyId);
             stmtsadj.setObject(3, companyId);
             ResultSet rssa = stmtsadj.executeQuery();
             while (rssa.next()) {
             String smId = java.util.UUID.randomUUID().toString();
             String smDetailId = java.util.UUID.randomUUID().toString();
                    
             String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
             PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
             stmt1.setString(1, smId);
             stmt1.setString(2, companyId);
             stmt1.setString(3, rssa.getString("product")); //product
             stmt1.setString(4, InvWarId);
             stmt1.setString(5, null);
             stmt1.setString(6, rssa.getString("uom"));//stockuom
             stmt1.setDouble(7, rssa.getDouble("baseuomquantity")); //qty
             stmt1.setDouble(8, rssa.getDouble("rate")); //priceperunit
             stmt1.setDate(9, rssa.getDate("createdondate")); //createdon
             stmt1.setString(10, rssa.getString("seqno")); // transaction no
             stmt1.setDate(11, rssa.getDate("grorderdate")); // transaction date
             stmt1.setInt(12, 1); // stock In : 1 .Stock out : 2
             stmt1.setInt(13, 3); // transaction module // do- 6,grn-5 ,salesreturn ,stock adjustment : 3
             stmt1.setString(14, rssa.getString("id")); // modulerefid (parent) 
             stmt1.setString(15, "Stock added through Sales Return");
             stmt1.setString(16, rssa.getString("costcenter"));
             stmt1.setString(17, rssa.getString("vendor"));  // vendor
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
             smd.setDouble(6, rssa.getDouble("baseuomquantity"));
             smd.execute();
             smd.close();
             }

             /*
                
                
             /**
             * *************************************
             * For : Product Build Case Stock Movement Entry
             * **********************************
             */
            /*             String productBuildQry = "SELECT pb.*,product.unitOfMeasure from productbuild pb INNER JOIN product ON product.id=pb.product where pb.company=? AND pb.product=?";
             PreparedStatement stmtpb = conn.prepareStatement(productBuildQry);
             stmtpb.setObject(1, companyId);
             stmtpb.setObject(2, productid);
             ResultSet rs3 = stmtpb.executeQuery();
             while (rs3.next()) {
             String smId = java.util.UUID.randomUUID().toString();
             String smDetailId = java.util.UUID.randomUUID().toString();
             String productBuildId = rs3.getString("id");
             double parentQty = rs3.getDouble("quantity");
             String parentProductId = rs3.getString("product");

             String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
             PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
             stmt1.setString(1, smId);
             stmt1.setString(2, companyId);
             stmt1.setString(3, parentProductId); //product
             stmt1.setString(4, InvWarId);
             stmt1.setString(5, null);
             stmt1.setString(6, rs3.getString("unitOfMeasure"));//stockuom
             stmt1.setDouble(7, parentQty); //qty
             stmt1.setDouble(8, rs3.getDouble("productcost")); //priceperunit
             stmt1.setDate(9, rs3.getDate("entrydate")); //createdon
             stmt1.setString(10, rs3.getString("refno")); // transaction no
             stmt1.setDate(11, rs3.getDate("entrydate")); // transaction date
             stmt1.setInt(12, 1); // stock In : 1 .Stock out : 2
             stmt1.setInt(13, 10); // transaction module // ERP_PRODUCT -10 , do- 6,grn-5
             stmt1.setString(14, productBuildId); // modulerefid (parent) 
             stmt1.setString(15, "Stock added from Product Build");
             stmt1.setString(16, null); //costcenter
             stmt1.setString(17, null);  // vendor
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
             smd.setDouble(6, parentQty);
             smd.execute();
             smd.close();

             String productBuildDetailQry = "SELECT pbd.*,p.unitOfMeasure,pb.entrydate,pb.refno from pbdetails pbd  INNER JOIN productbuild pb ON pb.id = pbd.build INNER JOIN product p ON p.id=pbd.aproduct where pbd.build= ?";
             PreparedStatement stmtpbdtl = conn.prepareStatement(productBuildDetailQry);
             stmtpbdtl.setObject(1, productBuildId);
             ResultSet rsdtl = stmtpbdtl.executeQuery();
             String stockMovementQryForPBD = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer,assembled_product) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
             while (rsdtl.next()) {

             String smIdChild = java.util.UUID.randomUUID().toString();
             String smDetailIdChild = java.util.UUID.randomUUID().toString();
             String productBuildIdChild = rsdtl.getString("id");
             double qty = rsdtl.getDouble("inventoryquantity");

             PreparedStatement stmt1dtl = conn.prepareStatement(stockMovementQryForPBD);
             stmt1dtl.setString(1, smIdChild);
             stmt1dtl.setString(2, companyId);
             stmt1dtl.setString(3, rsdtl.getString("aproduct")); //product
             stmt1dtl.setString(4, InvWarId);
             stmt1dtl.setString(5, null);
             stmt1dtl.setString(6, rsdtl.getString("unitOfMeasure"));//stockuom
             stmt1dtl.setDouble(7, parentQty * qty); //qty
             stmt1dtl.setDouble(8, rsdtl.getDouble("rate")); //priceperunit   -------->>>>>>>>>>>Check
             stmt1dtl.setDate(9, rsdtl.getDate("entrydate")); //createdon
             stmt1dtl.setString(10, rsdtl.getString("refno")); // transaction no
             stmt1dtl.setDate(11, rsdtl.getDate("entrydate")); // transaction date
             stmt1dtl.setInt(12, 2); // stock In : 1 .Stock out : 2
             stmt1dtl.setInt(13, 10); // transaction module // ERP_PRODUCT-10, do- 6,grn-5
             stmt1dtl.setString(14, productBuildIdChild); // modulerefid (parent) 
             stmt1dtl.setString(15, "Stock out through Product Build");
             stmt1dtl.setString(16, null); //costcenter
             stmt1dtl.setString(17, null);  // vendor
             stmt1dtl.setString(18, null); // customer
             stmt1dtl.setString(19, parentProductId); // parent product id
             stmt1dtl.execute();
             stmt1dtl.close();

             PreparedStatement smdchild = conn.prepareStatement(smDetailQry);
             smdchild.setString(1, smDetailIdChild);
             smdchild.setString(2, smIdChild);
             smdchild.setString(3, InvLocId);
             smdchild.setString(4, "");
             smdchild.setString(5, null);
             smdchild.setDouble(6, parentQty * qty);
             smdchild.execute();
             smdchild.close();

             }  */
            System.out.println("Count Detail  :  Opening -->" + totalOpeningCaseAddCnt + "    GRN -->  " + totalGRNAddCnt + "  DO -->  " + totalDOAddCnt + "   PR -->  " + totalPRAddCnt + "  SR --> " + totalSRAddCnt);
        }

        totalCompanyUpdationCnt++;
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
        out.println("<br><br> Total Opening Entries added in Stock Movement are " + totalOpeningCaseAddCnt);
        out.println("<br><br> Total GRN Entries added in Stock Movement are " + totalGRNAddCnt);
        out.println("<br><br> Total DO Entries added in Stock Movement are " + totalDOAddCnt);
        out.println("<br><br> Total PR Entries added in Stock Movement are " + totalPRAddCnt);
        out.println("<br><br> Total SR Entries added in Stock Movement are " + totalSRAddCnt);
        out.println("<br><br> Time :  " + new java.util.Date().toString());

    } catch (Exception e) {
        if (conn != null) {
            // conn.rollback();
        }
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>