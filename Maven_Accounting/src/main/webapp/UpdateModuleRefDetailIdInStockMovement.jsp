<%@page import="org.apache.commons.collections.CollectionUtils"%>
<%@page import="org.apache.commons.lang.ArrayUtils"%>
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
//        String serverip = "localhost";
//        String port = "3306";
//        String dbName = "21db";
//        String userName = "root";
//        String password = "";
//        String tempsubdomain = "srk01";
        
        String serverip = request.getParameter("serverip");//"192.168.0.208";                            
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"newstaging";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String tempsubdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = " SELECT DISTINCT companyid,subdomain FROM in_stockmovement sm INNER JOIN company  c ON sm.company=c.companyid ";
        if (!StringUtil.isNullOrEmpty(tempsubdomain)) {
            query += " AND c.subdomain='" + tempsubdomain + "' ";
        }

        PreparedStatement stmt = conn.prepareStatement(query);

        ResultSet companyrs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;

        while (companyrs.next()) {
            String companyId = companyrs.getString("companyid");
            String subdomain = companyrs.getString("subdomain");

            System.out.println("Update process start for company : " + subdomain + " at " + new java.util.Date());
            String qryy = " UPDATE in_stockmovement set modulerefdetailid = NULL WHERE company=? ";
            PreparedStatement stmt1 = conn.prepareStatement(qryy);
            stmt1.setString(1, companyId);
            stmt1.executeUpdate();

            // transaction_module  : 0,1,2,3,4,10,11,12 
            String qrry1 = " UPDATE  in_stockmovement sm SET sm.modulerefdetailid=sm.modulerefid WHERE sm.modulerefdetailid IS NULL AND sm.transaction_module IN(0,1,2,3,4,10,11,12) AND sm.company = ?";
            PreparedStatement stmtUpdate = conn.prepareStatement(qrry1);
            stmtUpdate.setString(1, companyId);
            stmtUpdate.executeUpdate();

            // transaction_module  : 5 (GRN)
            qrry1 = "UPDATE  in_stockmovement sm"
                    + " INNER JOIN ( "
                    + " SELECT COUNT(*) , sm.product, sm.modulerefid as modulerefid,grd.id as modulerefdetailid FROM  in_stockmovement sm"
                    + " INNER JOIN grodetails grd ON sm.product = grd.product AND sm.modulerefid = grd.grorder"
                    + " WHERE sm.modulerefdetailid IS NULL "
                    + " GROUP BY sm.product, sm.modulerefid HAVING COUNT(*) = 1"
                    + " ) tbl1 ON  sm.product = tbl1.product AND sm.modulerefid = tbl1.modulerefid"
                    + " SET sm.modulerefdetailid=tbl1.modulerefdetailid "
                    + " WHERE sm.modulerefdetailid IS NULL AND sm.company=?";
            stmtUpdate = conn.prepareStatement(qrry1);
            stmtUpdate.setString(1, companyId);
            stmtUpdate.executeUpdate();

            //  transaction_module  : 6,7 (DO,consignment DO)
            qrry1 = " UPDATE  in_stockmovement sm"
                    + " INNER JOIN ( "
                    + " SELECT COUNT(*) , sm.product, sm.modulerefid as modulerefid,dd.id as modulerefdetailid FROM  in_stockmovement sm "
                    + " INNER JOIN dodetails dd ON sm.product = dd.product AND sm.modulerefid = dd.deliveryorder"
                    + " WHERE sm.modulerefdetailid IS NULL "
                    + " GROUP BY sm.product, sm.modulerefid HAVING COUNT(*) = 1 "
                    + ") tbl1 ON  sm.product = tbl1.product AND sm.modulerefid = tbl1.modulerefid"
                    + " SET sm.modulerefdetailid=tbl1.modulerefdetailid"
                    + " WHERE sm.modulerefdetailid IS NULL AND sm.company=?";
            stmtUpdate = conn.prepareStatement(qrry1);
            stmtUpdate.setString(1, companyId);
            stmtUpdate.executeUpdate();

            //  transaction_module  :  transaction_module  : 8 (purchase return)
            qrry1 = "UPDATE  in_stockmovement sm"
                    + " INNER JOIN ( "
                    + " SELECT COUNT(*) , sm.product, sm.modulerefid as modulerefid,prd.id as modulerefdetailid FROM  in_stockmovement sm"
                    + " INNER JOIN prdetails prd ON sm.product = prd.product AND sm.modulerefid = prd.purchasereturn"
                    + " WHERE sm.modulerefdetailid IS NULL"
                    + " GROUP BY sm.product, sm.modulerefid HAVING COUNT(*) = 1"
                    + " ) tbl1 ON  sm.product = tbl1.product AND sm.modulerefid = tbl1.modulerefid"
                    + " SET sm.modulerefdetailid=tbl1.modulerefdetailid"
                    + " WHERE sm.modulerefdetailid IS NULL AND sm.company=?";
            stmtUpdate = conn.prepareStatement(qrry1);
            stmtUpdate.setString(1, companyId);
            stmtUpdate.executeUpdate();

            //  transaction_module  :  transaction_module  : 9 (sales return)
            qrry1 = "UPDATE  in_stockmovement sm"
                    + " INNER JOIN ( "
                    + " SELECT COUNT(*) , sm.product, sm.modulerefid as modulerefid,srd.id as modulerefdetailid FROM  in_stockmovement sm"
                    + " INNER JOIN srdetails srd ON sm.product = srd.product AND sm.modulerefid = srd.salesreturn"
                    + " WHERE sm.modulerefdetailid IS NULL"
                    + " GROUP BY sm.product, sm.modulerefid HAVING COUNT(*) = 1"
                    + " ) tbl1 ON  sm.product = tbl1.product AND sm.modulerefid = tbl1.modulerefid"
                    + " SET sm.modulerefdetailid=tbl1.modulerefdetailid"
                    + " WHERE sm.modulerefdetailid IS NULL AND sm.company=?";
            stmtUpdate = conn.prepareStatement(qrry1);
            stmtUpdate.setString(1, companyId);
            stmtUpdate.executeUpdate();

            PreparedStatement smdDel = null;
            String delQry1 = " DELETE FROM in_stockmovement WHERE modulerefid NOT IN (SELECT id FROM grorder) AND transaction_module = 5 ";
            String delQry2 = " DELETE FROM in_stockmovement WHERE modulerefid NOT IN (SELECT id FROM deliveryorder) AND transaction_module IN(6,7)  ";
            String delQry3 = " DELETE FROM in_stockmovement WHERE modulerefid NOT IN (SELECT id FROM purchasereturn) AND transaction_module= 8 ";
            String delQry4 = " DELETE FROM in_stockmovement WHERE modulerefid NOT IN (SELECT id FROM salesreturn) AND transaction_module= 9 ";
            smdDel = conn.prepareStatement(delQry1);
            smdDel.executeUpdate();
            smdDel = conn.prepareStatement(delQry2);
            smdDel.executeUpdate();
            smdDel = conn.prepareStatement(delQry3);
            smdDel.executeUpdate();
            smdDel = conn.prepareStatement(delQry4);
            smdDel.executeUpdate();

            /**
             * ********************************
             * For : DO Case *********************************
             */
            int DOCount = 0;
            String querydo = " SELECT sm.id as smid,dd.id as modulerefdetailid,p.isSerialForProduct,p.isBatchForProduct,sm.product  FROM in_stockmovement sm "
                    + " INNER JOIN dodetails dd ON dd.deliveryorder=sm.modulerefid "
                    + " INNER JOIN product p ON p.id=sm.product"
                    + " WHERE sm.company=? AND sm.transaction_module IN(6,7)  AND  sm.product=dd.product  AND sm.modulerefdetailid IS NULL GROUP BY dd.id,smid,sm.product";
            PreparedStatement stmtstkmvt = conn.prepareStatement(querydo);
            stmtstkmvt.setObject(1, companyId);
            ResultSet stkmvtrs = stmtstkmvt.executeQuery();
            Set DOSMSet = new HashSet<String>();
            Set DODetailSet = new HashSet<String>();
//            System.out.println("<br/><b>DO CASE </b><br/>");
            out.println("<br/><b>DO CASE </b><br/>");
            while (stkmvtrs.next()) {
                String moduleRefDetailId = stkmvtrs.getString("modulerefdetailid");
                String stockMovementID = stkmvtrs.getString("smid");
                String productId = stkmvtrs.getString("product");
                boolean isSerialForProduct = "F".equalsIgnoreCase(stkmvtrs.getString("isSerialForProduct")) ? false : true;
                boolean isBatchForProduct = "F".equalsIgnoreCase(stkmvtrs.getString("isBatchForProduct")) ? false : true;

                if (DODetailSet.contains(moduleRefDetailId) || DOSMSet.contains(stockMovementID)) {
                    continue;
                }

                if (isSerialForProduct) {

                    String qryToSelect1 = " SELECT * from "
                            + " (SELECT b.id,b.product,b.warehouse,b.location,b.batchname,b.row,b.rack,b.bin, SUM(s.quantity) as quantity, GROUP_CONCAT(s.serialname) as smserials from newproductbatch b INNER JOIN newbatchserial s ON b.id=s.batch  "
                            + " INNER JOIN serialdocumentmapping sdm ON sdm.serialid=s.id   "
                            + " WHERE sdm.documentid =? AND sdm.transactiontype=27 AND b.product=?  "
                            + " GROUP BY b.id)  AS smdetailtbl    "
                            + " INNER JOIN  "
                            + " (SELECT sm.store as warehouse,smd.location as location,smd.batchname,smd.row,smd.rack,smd.bin,SUM(smd.quantity) as quantity ,GROUP_CONCAT(smd.serialnames)  as serials from in_sm_detail smd   "
                            + " INNER JOIN in_stockmovement sm ON sm.id=smd.stockmovement   "
                            + " INNER JOIN dodetails dod ON  (dod.deliveryorder=sm.modulerefid AND dod.product=sm.product)  "
                            + " WHERE dod.id=? AND sm.product=? AND sm.id=?  "
                            + " GROUP BY smd.batchname)  AS dodetailtbl    "
                            + " ON (smdetailtbl.warehouse=dodetailtbl.warehouse  AND smdetailtbl.location=dodetailtbl.location  AND smdetailtbl.batchname=dodetailtbl.batchname  AND smdetailtbl.quantity=dodetailtbl.quantity    "
                            + " AND  IF( dodetailtbl.row IS NULL,dodetailtbl.row IS NULL, dodetailtbl.row=smdetailtbl.row ) AND  IF( dodetailtbl.rack IS NULL,dodetailtbl.rack IS NULL, dodetailtbl.rack=smdetailtbl.rack ) AND  IF( dodetailtbl.bin IS NULL,dodetailtbl.bin IS NULL, dodetailtbl.bin=smdetailtbl.bin ) )  ";

                    PreparedStatement stmtslct1 = conn.prepareStatement(qryToSelect1);
                    stmtslct1.setObject(1, moduleRefDetailId);
                    stmtslct1.setObject(2, productId);
                    stmtslct1.setObject(3, moduleRefDetailId);
                    stmtslct1.setObject(4, productId);
                    stmtslct1.setObject(5, stockMovementID);
                    ResultSet rsdt1 = stmtslct1.executeQuery();

                    while (rsdt1.next()) {
                        List<String> smSerialList = (!StringUtil.isNullOrEmpty(rsdt1.getString("smserials")) ? Arrays.asList(rsdt1.getString("smserials").split(",")) : null);
                        List<String> serialList = (!StringUtil.isNullOrEmpty(rsdt1.getString("serials")) ? Arrays.asList(rsdt1.getString("serials").split(",")) : null);
                        Collections.sort(smSerialList);
                        Collections.sort(serialList);

                        if (!smSerialList.isEmpty() && !serialList.isEmpty() && smSerialList.equals(serialList)) {
                            if (DODetailSet.contains(moduleRefDetailId) || DOSMSet.contains(stockMovementID)) {
                                continue;
                            }
                            String qryToUpdate = "UPDATE in_stockmovement SET modulerefdetailid=? WHERE id=?";
                            PreparedStatement stmtforUpdate = conn.prepareStatement(qryToUpdate);
                            stmtforUpdate.setString(1, moduleRefDetailId);
                            stmtforUpdate.setString(2, stockMovementID);
                            stmtforUpdate.executeUpdate();

                            DOSMSet.add(stockMovementID);
                            DODetailSet.add(moduleRefDetailId);
                            DOCount++;
                            String printStr = DOCount + ")  FROM IF " + " UPDATE in_stockmovement SET modulerefdetailid='" + moduleRefDetailId + "' WHERE id='" + stockMovementID + "'";
//                            System.out.println(printStr);
                            out.println("<br/>" + printStr);
                        }

                    }

                } else {
                    String qryToSelect = " SELECT * from "
                            + "  (SELECT sm.store as warehouse,smd.location as location,smd.batchname,smd.row,smd.rack,smd.bin,smd.quantity from in_sm_detail smd  INNER JOIN in_stockmovement sm ON sm.id=smd.stockmovement WHERE smd.stockmovement=?)  AS smdetailtbl  INNER JOIN "
                            + "  (SELECT b.warehouse,b.location,b.batchname,b.row,b.rack,b.bin,lbm.quantity from dodetails dod INNER JOIN locationbatchdocumentmapping lbm  ON lbm.documentid=dod.id INNER JOIN newproductbatch b ON b.id=lbm.batchmapid WHERE lbm.documentid=? AND dod.id=?"
                            + "  AND lbm.transactiontype=27)  AS dodetailtbl  "
                            + "  ON (smdetailtbl.warehouse=dodetailtbl.warehouse  AND smdetailtbl.location=dodetailtbl.location  AND smdetailtbl.batchname=dodetailtbl.batchname  AND smdetailtbl.quantity=dodetailtbl.quantity  AND  IF( dodetailtbl.row IS NULL,dodetailtbl.row IS NULL, "
                            + "  dodetailtbl.row=smdetailtbl.row ) AND  IF( dodetailtbl.rack IS NULL,dodetailtbl.rack IS NULL, dodetailtbl.rack=smdetailtbl.rack ) AND  IF( dodetailtbl.bin IS NULL,dodetailtbl.bin IS NULL, dodetailtbl.bin=smdetailtbl.bin ) ) ";

                    PreparedStatement stmtslct = conn.prepareStatement(qryToSelect);
                    stmtslct.setObject(1, stockMovementID);
                    stmtslct.setObject(2, moduleRefDetailId);
                    stmtslct.setObject(3, moduleRefDetailId);
                    ResultSet rsdt = stmtslct.executeQuery();
                    if (rsdt.next()) {
                        if (DODetailSet.contains(moduleRefDetailId) || DOSMSet.contains(stockMovementID)) {
                            continue;
                        }
                        String qryToUpdate = "UPDATE in_stockmovement SET modulerefdetailid=? WHERE id=?";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(qryToUpdate);
                        stmtforUpdate.setString(1, moduleRefDetailId);
                        stmtforUpdate.setString(2, stockMovementID);
                        stmtforUpdate.executeUpdate();

                        DOSMSet.add(stockMovementID);
                        DODetailSet.add(moduleRefDetailId);
                        DOCount++;
                        String printStr = DOCount + ")  FROM ELSE " + " UPDATE in_stockmovement SET modulerefdetailid='" + moduleRefDetailId + "' WHERE id='" + stockMovementID + "'";
//                        System.out.println(printStr);
                        out.println("<br/>" + printStr);

                    }
                }

            }

            /**
             * ********************************
             * For : GRN Case ********************************
             */
            int GRNCount = 0;
            String queryGRN = " SELECT sm.id as smid,gd.id as modulerefdetailid,p.isSerialForProduct,p.isBatchForProduct,gd.product  FROM in_stockmovement sm "
                    + " INNER JOIN grodetails gd ON gd.grorder=sm.modulerefid  "
                    + " INNER JOIN product p ON p.id=sm.product "
                    + " WHERE sm.company=? AND  "
                    + " sm.transaction_module IN(5)  AND  sm.product=gd.product  AND sm.modulerefdetailid IS NULL GROUP BY gd.id,smid,sm.product ";
            stmtstkmvt = conn.prepareStatement(queryGRN);
            stmtstkmvt.setObject(1, companyId);
            stkmvtrs = stmtstkmvt.executeQuery();
            Set GRNSMSet = new HashSet<String>();
            Set GRNDetailSet = new HashSet<String>();
//            System.out.println("<br/><br/><b>GRN CASE </b><br/>");
            out.println("<br/><br/><b>GRN CASE </b><br/>");
            while (stkmvtrs.next()) {
                String moduleRefDetailId = stkmvtrs.getString("modulerefdetailid");
                String stockMovementID = stkmvtrs.getString("smid");
                String productId = stkmvtrs.getString("product");
                boolean isSerialForProduct = "F".equalsIgnoreCase(stkmvtrs.getString("isSerialForProduct")) ? false : true;
                boolean isBatchForProduct = "F".equalsIgnoreCase(stkmvtrs.getString("isBatchForProduct")) ? false : true;

                if (GRNDetailSet.contains(moduleRefDetailId) || GRNSMSet.contains(stockMovementID)) {
                    continue;
                }

                if (isSerialForProduct) {

                    String qryToSelect1 =   " SELECT * from "
                        + " (SELECT b.id,b.product,b.warehouse,b.location,b.batchname,b.row,b.rack,b.bin, SUM(s.quantity) as quantity, GROUP_CONCAT(s.serialname) as smserials from newproductbatch b INNER JOIN newbatchserial s ON b.id=s.batch  "
                        + " INNER JOIN serialdocumentmapping sdm ON sdm.serialid=s.id   "
                        + " WHERE sdm.documentid =? AND sdm.transactiontype=28 AND b.product=?  "
                        + " GROUP BY b.id)  AS smdetailtbl    "
                        + " INNER JOIN  "
                        + " (SELECT sm.store as warehouse,smd.location as location,smd.batchname,smd.row,smd.rack,smd.bin,SUM(smd.quantity) as quantity ,GROUP_CONCAT(smd.serialnames)  as serials from in_sm_detail smd   " 
                        + " INNER JOIN in_stockmovement sm ON sm.id=smd.stockmovement   "
                        + " INNER JOIN grodetails grod ON  (grod.grorder=sm.modulerefid AND grod.product=sm.product)  "
                        + " WHERE grod.id=?  AND sm.product=? AND sm.id=?  "
                        + " GROUP BY smd.batchname)  AS grodetailtbl    "
                        + " ON (smdetailtbl.warehouse=grodetailtbl.warehouse  AND smdetailtbl.location=grodetailtbl.location  AND smdetailtbl.batchname=grodetailtbl.batchname  AND smdetailtbl.quantity=grodetailtbl.quantity  AND  IF  "
                        + " ( grodetailtbl.row IS NULL,grodetailtbl.row IS NULL, grodetailtbl.row=smdetailtbl.row ) AND  IF( grodetailtbl.rack IS NULL,grodetailtbl.rack IS NULL, grodetailtbl.rack=smdetailtbl.rack ) AND  IF  "
                        + " ( grodetailtbl.bin IS NULL,grodetailtbl.bin IS NULL, grodetailtbl.bin=smdetailtbl.bin ) )  ";


                    PreparedStatement stmtslct1 = conn.prepareStatement(qryToSelect1);
                    stmtslct1.setObject(1, moduleRefDetailId);
                    stmtslct1.setObject(2, productId);
                    stmtslct1.setObject(3, moduleRefDetailId);
                    stmtslct1.setObject(4, productId);
                    stmtslct1.setObject(5, stockMovementID);
                    ResultSet rsdt1 = stmtslct1.executeQuery();

                    while (rsdt1.next()) {
                        List<String> smSerialList = (!StringUtil.isNullOrEmpty(rsdt1.getString("smserials")) ? Arrays.asList(rsdt1.getString("smserials").split(",")) : null);
                        List<String> serialList = (!StringUtil.isNullOrEmpty(rsdt1.getString("serials")) ? Arrays.asList(rsdt1.getString("serials").split(",")) : null);
                        Collections.sort(smSerialList);
                        Collections.sort(serialList);

                        if (!smSerialList.isEmpty() && !serialList.isEmpty() && smSerialList.equals(serialList)) {
                            if (GRNDetailSet.contains(moduleRefDetailId) || GRNSMSet.contains(stockMovementID)) {
                                continue;
                            }
                            String qryToUpdate = "UPDATE in_stockmovement SET modulerefdetailid=? WHERE id=?";
                            PreparedStatement stmtforUpdate = conn.prepareStatement(qryToUpdate);
                            stmtforUpdate.setString(1, moduleRefDetailId);
                            stmtforUpdate.setString(2, stockMovementID);
                            stmtforUpdate.executeUpdate();

                            GRNSMSet.add(stockMovementID);
                            GRNDetailSet.add(moduleRefDetailId);
                            GRNCount++;
                            String printStr = GRNCount + ")  FROM IF " + " UPDATE in_stockmovement SET modulerefdetailid='" + moduleRefDetailId + "' WHERE id='" + stockMovementID + "'";
//                            System.out.println(printStr);
                            out.println("<br/>" + printStr);
                        }

                    }

                } else {
                    String qryToSelect =  " SELECT * from "
                        +" (SELECT sm.store as warehouse,smd.location as location,smd.batchname,smd.row,smd.rack,smd.bin,smd.quantity from in_sm_detail smd  INNER JOIN in_stockmovement sm ON sm.id=smd.stockmovement WHERE  "
                        +" smd.stockmovement=?)  AS smdetailtbl  INNER JOIN "
                        +"  (SELECT b.warehouse,b.location,b.batchname,b.row,b.rack,b.bin,lbm.quantity from grodetails grod INNER JOIN locationbatchdocumentmapping lbm  ON lbm.documentid=grod.id INNER JOIN newproductbatch b ON  "
                        +" b.id=lbm.batchmapid WHERE lbm.documentid=? AND grod.id= ? "
                        +"  AND lbm.transactiontype=28 )  AS grndetailtbl  "
                        +"  ON (smdetailtbl.warehouse=grndetailtbl.warehouse  AND smdetailtbl.location=grndetailtbl.location  AND smdetailtbl.batchname=grndetailtbl.batchname  AND smdetailtbl.quantity=grndetailtbl.quantity AND IF" +" ( grndetailtbl.row IS NULL,grndetailtbl.row IS NULL,  "
                        +" grndetailtbl.row=smdetailtbl.row ) AND  IF( grndetailtbl.rack IS NULL,grndetailtbl.rack IS NULL, grndetailtbl.rack=smdetailtbl.rack ) AND  IF( grndetailtbl.bin IS NULL,grndetailtbl.bin IS NULL,  "
                        +" grndetailtbl.bin=smdetailtbl.bin)) " ;
                    
                    PreparedStatement stmtslct = conn.prepareStatement(qryToSelect);
                    stmtslct.setObject(1, stockMovementID);
                    stmtslct.setObject(2, moduleRefDetailId);
                    stmtslct.setObject(3, moduleRefDetailId);
                    ResultSet rsdt = stmtslct.executeQuery();
                    if (rsdt.next()) {
                        if (GRNDetailSet.contains(moduleRefDetailId) || GRNSMSet.contains(stockMovementID)) {
                            continue;
                        }
                        String qryToUpdate = "UPDATE in_stockmovement SET modulerefdetailid=? WHERE id=?";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(qryToUpdate);
                        stmtforUpdate.setString(1, moduleRefDetailId);
                        stmtforUpdate.setString(2, stockMovementID);
                        stmtforUpdate.executeUpdate();

                        GRNSMSet.add(stockMovementID);
                        GRNDetailSet.add(moduleRefDetailId);
                        GRNCount++;
                        String printStr = GRNCount + ")  FROM ELSE " + " UPDATE in_stockmovement SET modulerefdetailid='" + moduleRefDetailId + "' WHERE id='" + stockMovementID + "'";
//                        System.out.println(printStr);
                        out.println("<br/>" + printStr);

                    }
                }

            }
            
             /**
             * ********************************
             * For : PR Case ********************************
             */
            int PRCount = 0;
            String queryPR = " SELECT sm.id as smid,pd.id as modulerefdetailid,p.isSerialForProduct,p.isBatchForProduct,pd.product  FROM in_stockmovement sm "
                    + " INNER JOIN prdetails pd ON pd.purchasereturn=sm.modulerefid  "
                    + " INNER JOIN product p ON p.id=sm.product "
                    + " WHERE sm.company=? AND  "
                    + " sm.transaction_module IN(8)  AND  sm.product=pd.product  AND sm.modulerefdetailid IS NULL GROUP BY pd.id,smid,sm.product ";
            stmtstkmvt = conn.prepareStatement(queryPR);
            stmtstkmvt.setObject(1, companyId);
            stkmvtrs = stmtstkmvt.executeQuery();
            Set PRSMSet = new HashSet<String>();
            Set PRDetailSet = new HashSet<String>();
//            System.out.println("<br/><br/><b>PR CASE </b><br/>");
            out.println("<br/><br/><b>PR CASE </b><br/>");
            while (stkmvtrs.next()) {
                String moduleRefDetailId = stkmvtrs.getString("modulerefdetailid");
                String stockMovementID = stkmvtrs.getString("smid");
                String productId = stkmvtrs.getString("product");
                boolean isSerialForProduct = "F".equalsIgnoreCase(stkmvtrs.getString("isSerialForProduct")) ? false : true;
                boolean isBatchForProduct = "F".equalsIgnoreCase(stkmvtrs.getString("isBatchForProduct")) ? false : true;

                if (PRDetailSet.contains(moduleRefDetailId) || PRSMSet.contains(stockMovementID)) {
                    continue;
                }

                if (isSerialForProduct) {

                    String qryToSelect1 =    " SELECT * from "
                            +" (SELECT b.id,b.product,b.warehouse,b.location,b.batchname,b.row,b.rack,b.bin, SUM(s.quantity) as quantity, GROUP_CONCAT(s.serialname) as smserials from newproductbatch b INNER JOIN newbatchserial s ON" +" +" +" b.id=s.batch  "
                            +" INNER JOIN serialdocumentmapping sdm ON sdm.serialid=s.id "
                            +" WHERE sdm.documentid =? AND sdm.transactiontype=31 AND b.product=?"
                            +" GROUP BY b.id)  AS smdetailtbl  "
                            +" INNER JOIN"
                            +" (SELECT sm.store as warehouse,smd.location as location,smd.batchname,smd.row,smd.rack,smd.bin,SUM(smd.quantity) as quantity ,GROUP_CONCAT(smd.serialnames)  as serials from in_sm_detail smd"  
                            +" INNER JOIN in_stockmovement sm ON sm.id=smd.stockmovement "
                            +" INNER JOIN prdetails prd ON  (prd.purchasereturn=sm.modulerefid AND prd.product=sm.product)"
                            +" WHERE prd.id=?  AND sm.product=? AND sm.id=?"
                            +" GROUP BY smd.batchname)  AS prdetailtbl  "
                            +" ON (smdetailtbl.warehouse=prdetailtbl.warehouse  AND smdetailtbl.location=prdetailtbl.location  AND smdetailtbl.batchname=prdetailtbl.batchname  AND smdetailtbl.quantity=prdetailtbl.quantity "
                            +" AND IF( prdetailtbl.row IS NULL,prdetailtbl.row IS NULL, prdetailtbl.row=smdetailtbl.row ) AND  IF( prdetailtbl.rack IS NULL,prdetailtbl.rack IS NULL, prdetailtbl.rack=smdetailtbl.rack ) "
                            +" AND  IF( prdetailtbl.bin IS NULL,prdetailtbl.bin IS NULL, prdetailtbl.bin=smdetailtbl.bin ) )";



                    PreparedStatement stmtslct1 = conn.prepareStatement(qryToSelect1);
                    stmtslct1.setObject(1, moduleRefDetailId);
                    stmtslct1.setObject(2, productId);
                    stmtslct1.setObject(3, moduleRefDetailId);
                    stmtslct1.setObject(4, productId);
                    stmtslct1.setObject(5, stockMovementID);
                    ResultSet rsdt1 = stmtslct1.executeQuery();

                    while (rsdt1.next()) {
                        List<String> smSerialList = (!StringUtil.isNullOrEmpty(rsdt1.getString("smserials")) ? Arrays.asList(rsdt1.getString("smserials").split(",")) : null);
                        List<String> serialList = (!StringUtil.isNullOrEmpty(rsdt1.getString("serials")) ? Arrays.asList(rsdt1.getString("serials").split(",")) : null);
                        Collections.sort(smSerialList);
                        Collections.sort(serialList);

                        if (!smSerialList.isEmpty() && !serialList.isEmpty() && smSerialList.equals(serialList)) {
                            if (PRDetailSet.contains(moduleRefDetailId) || PRSMSet.contains(stockMovementID)) {
                                continue;
                            }
                            String qryToUpdate = "UPDATE in_stockmovement SET modulerefdetailid=? WHERE id=?";
                            PreparedStatement stmtforUpdate = conn.prepareStatement(qryToUpdate);
                            stmtforUpdate.setString(1, moduleRefDetailId);
                            stmtforUpdate.setString(2, stockMovementID);
                            stmtforUpdate.executeUpdate();

                            PRSMSet.add(stockMovementID);
                            PRDetailSet.add(moduleRefDetailId);
                            PRCount++;
                            String printStr = PRCount + ")  FROM IF " + " UPDATE in_stockmovement SET modulerefdetailid='" + moduleRefDetailId + "' WHERE id='" + stockMovementID + "'";
//                            System.out.println(printStr);
                            out.println("<br/>" + printStr);
                        }

                    }

                } else {
                    String qryToSelect =   " SELECT * from "
                        +" (SELECT sm.store as warehouse,smd.location as location,smd.batchname,smd.row,smd.rack,smd.bin,smd.quantity from in_sm_detail smd  INNER JOIN in_stockmovement sm ON sm.id=smd.stockmovement WHERE "
                        +" smd.stockmovement=? )  AS smdetailtbl  INNER JOIN "
                        +" (SELECT b.warehouse,b.location,b.batchname,b.row,b.rack,b.bin,lbm.quantity from prdetails prd INNER JOIN locationbatchdocumentmapping lbm  ON lbm.documentid=prd.id INNER JOIN newproductbatch b ON"
                        +" b.id=lbm.batchmapid WHERE lbm.documentid=?  AND prd.id=? "
                        +" AND lbm.transactiontype=31 )  AS prdetailtbl "
                        +" ON (smdetailtbl.warehouse=prdetailtbl.warehouse  AND smdetailtbl.location=prdetailtbl.location  AND smdetailtbl.batchname=prdetailtbl.batchname  AND smdetailtbl.quantity=prdetailtbl.quantity  AND  IF( prdetailtbl.row IS NULL,prdetailtbl.row IS NULL, "
                        +" prdetailtbl.row=smdetailtbl.row ) AND  IF( prdetailtbl.rack IS NULL,prdetailtbl.rack IS NULL, prdetailtbl.rack=smdetailtbl.rack ) AND  IF( prdetailtbl.bin IS NULL,prdetailtbl.bin IS NULL," +" prdetailtbl.bin=smdetailtbl.bin ) )" ;
                    
                    PreparedStatement stmtslct = conn.prepareStatement(qryToSelect);
                    stmtslct.setObject(1, stockMovementID);
                    stmtslct.setObject(2, moduleRefDetailId);
                    stmtslct.setObject(3, moduleRefDetailId);
                    ResultSet rsdt = stmtslct.executeQuery();
                    if (rsdt.next()) {
                        if (PRDetailSet.contains(moduleRefDetailId) || PRSMSet.contains(stockMovementID)) {
                            continue;
                        }
                        String qryToUpdate = "UPDATE in_stockmovement SET modulerefdetailid=? WHERE id=?";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(qryToUpdate);
                        stmtforUpdate.setString(1, moduleRefDetailId);
                        stmtforUpdate.setString(2, stockMovementID);
                        stmtforUpdate.executeUpdate();

                        PRSMSet.add(stockMovementID);
                        PRDetailSet.add(moduleRefDetailId);
                        PRCount++;
                        String printStr = PRCount + ")  FROM ELSE " + " UPDATE in_stockmovement SET modulerefdetailid='" + moduleRefDetailId + "' WHERE id='" + stockMovementID + "'";
//                        System.out.println(printStr);
                        out.println("<br/>" + printStr);

                    }
                }
            }
            
            
             /**
             * ********************************
             * For : SR Case ********************************
             */
            int SRCount = 0;
            String querySR = " SELECT sm.id as smid,pd.id as modulerefdetailid,p.isSerialForProduct,p.isBatchForProduct,pd.product  FROM in_stockmovement sm "
                    + " INNER JOIN srdetails pd ON pd.salesreturn=sm.modulerefid  "
                    + " INNER JOIN product p ON p.id=sm.product "
                    + " WHERE sm.company=? AND  "
                    + " sm.transaction_module IN(9)  AND  sm.product=pd.product  AND sm.modulerefdetailid IS NULL GROUP BY pd.id,smid,sm.product ";
            stmtstkmvt = conn.prepareStatement(querySR);
            stmtstkmvt.setObject(1, companyId);
            stkmvtrs = stmtstkmvt.executeQuery();
            Set SRSMSet = new HashSet<String>();
            Set SRDetailSet = new HashSet<String>();
//            System.out.println("<br/><br/><b>SR Case </b><br/>");
            out.println("<br/><br/><b>SR Case </b><br/>");
            while (stkmvtrs.next()) {
                String moduleRefDetailId = stkmvtrs.getString("modulerefdetailid");
                String stockMovementID = stkmvtrs.getString("smid");
                String productId = stkmvtrs.getString("product");
                boolean isSerialForProduct = "F".equalsIgnoreCase(stkmvtrs.getString("isSerialForProduct")) ? false : true;
                boolean isBatchForProduct = "F".equalsIgnoreCase(stkmvtrs.getString("isBatchForProduct")) ? false : true;

                if (SRDetailSet.contains(moduleRefDetailId) || SRSMSet.contains(stockMovementID)) {
                    continue;
                }

                if (isSerialForProduct) {

                    String qryToSelect1 =    " SELECT * from "
                            +" (SELECT b.id,b.product,b.warehouse,b.location,b.batchname,b.row,b.rack,b.bin, SUM(s.quantity) as quantity, GROUP_CONCAT(s.serialname) as smserials from newproductbatch b INNER JOIN newbatchserial s ON" +" +" +" b.id=s.batch  "
                            +" INNER JOIN serialdocumentmapping sdm ON sdm.serialid=s.id "
                            +" WHERE sdm.documentid =? AND sdm.transactiontype=29 AND b.product=?"
                            +" GROUP BY b.id)  AS smdetailtbl  "
                            +" INNER JOIN"
                            +" (SELECT sm.store as warehouse,smd.location as location,smd.batchname,smd.row,smd.rack,smd.bin,SUM(smd.quantity) as quantity ,GROUP_CONCAT(smd.serialnames)  as serials from in_sm_detail smd"  
                            +" INNER JOIN in_stockmovement sm ON sm.id=smd.stockmovement "
                            +" INNER JOIN srdetails prd ON  (prd.salesreturn=sm.modulerefid AND prd.product=sm.product)"
                            +" WHERE prd.id=?  AND sm.product=? AND sm.id=?"
                            +" GROUP BY smd.batchname)  AS srdetailtbl  "
                            +" ON (smdetailtbl.warehouse=srdetailtbl.warehouse  AND smdetailtbl.location=srdetailtbl.location  AND smdetailtbl.batchname=srdetailtbl.batchname  AND smdetailtbl.quantity=srdetailtbl.quantity "
                            +" AND IF( srdetailtbl.row IS NULL,srdetailtbl.row IS NULL, srdetailtbl.row=smdetailtbl.row ) AND  IF( srdetailtbl.rack IS NULL,srdetailtbl.rack IS NULL, srdetailtbl.rack=smdetailtbl.rack ) "
                            +" AND  IF( srdetailtbl.bin IS NULL,srdetailtbl.bin IS NULL, srdetailtbl.bin=smdetailtbl.bin ) )";



                    PreparedStatement stmtslct1 = conn.prepareStatement(qryToSelect1);
                    stmtslct1.setObject(1, moduleRefDetailId);
                    stmtslct1.setObject(2, productId);
                    stmtslct1.setObject(3, moduleRefDetailId);
                    stmtslct1.setObject(4, productId);
                    stmtslct1.setObject(5, stockMovementID);
                    ResultSet rsdt1 = stmtslct1.executeQuery();

                    while (rsdt1.next()) {
                        List<String> smSerialList = (!StringUtil.isNullOrEmpty(rsdt1.getString("smserials")) ? Arrays.asList(rsdt1.getString("smserials").split(",")) : null);
                        List<String> serialList = (!StringUtil.isNullOrEmpty(rsdt1.getString("serials")) ? Arrays.asList(rsdt1.getString("serials").split(",")) : null);
                        Collections.sort(smSerialList);
                        Collections.sort(serialList);

                        if (!smSerialList.isEmpty() && !serialList.isEmpty() && smSerialList.equals(serialList)) {
                            if (SRDetailSet.contains(moduleRefDetailId) || SRSMSet.contains(stockMovementID)) {
                                continue;
                            }
                            String qryToUpdate = "UPDATE in_stockmovement SET modulerefdetailid=? WHERE id=?";
                            PreparedStatement stmtforUpdate = conn.prepareStatement(qryToUpdate);
                            stmtforUpdate.setString(1, moduleRefDetailId);
                            stmtforUpdate.setString(2, stockMovementID);
                            stmtforUpdate.executeUpdate();

                            SRSMSet.add(stockMovementID);
                            SRDetailSet.add(moduleRefDetailId);
                            SRCount++;
                            String printStr = SRCount + ")  FROM IF " + " UPDATE in_stockmovement SET modulerefdetailid='" + moduleRefDetailId + "' WHERE id='" + stockMovementID + "'";
//                            System.out.println(printStr);
                            out.println("<br/>" + printStr);
                        }

                    }

                } else {
                    String qryToSelect =   " SELECT * from "
                        +" (SELECT sm.store as warehouse,smd.location as location,smd.batchname,smd.row,smd.rack,smd.bin,smd.quantity from in_sm_detail smd  INNER JOIN in_stockmovement sm ON sm.id=smd.stockmovement WHERE "
                        +" smd.stockmovement=? )  AS smdetailtbl  INNER JOIN "
                        +" (SELECT b.warehouse,b.location,b.batchname,b.row,b.rack,b.bin,lbm.quantity from srdetails prd INNER JOIN locationbatchdocumentmapping lbm  ON lbm.documentid=prd.id INNER JOIN newproductbatch b ON"
                        +" b.id=lbm.batchmapid WHERE lbm.documentid=?  AND prd.id=? "
                        +" AND lbm.transactiontype=29 )  AS srdetailtbl "
                        +" ON (smdetailtbl.warehouse=srdetailtbl.warehouse  AND smdetailtbl.location=srdetailtbl.location  AND smdetailtbl.batchname=srdetailtbl.batchname  AND smdetailtbl.quantity=srdetailtbl.quantity  AND  IF( srdetailtbl.row IS NULL,srdetailtbl.row IS NULL, "
                        +" srdetailtbl.row=smdetailtbl.row ) AND  IF( srdetailtbl.rack IS NULL,srdetailtbl.rack IS NULL, srdetailtbl.rack=smdetailtbl.rack ) AND  IF( srdetailtbl.bin IS NULL,srdetailtbl.bin IS NULL," +" srdetailtbl.bin=smdetailtbl.bin ) )" ;
                    
                    PreparedStatement stmtslct = conn.prepareStatement(qryToSelect);
                    stmtslct.setObject(1, stockMovementID);
                    stmtslct.setObject(2, moduleRefDetailId);
                    stmtslct.setObject(3, moduleRefDetailId);
                    ResultSet rsdt = stmtslct.executeQuery();
                    if (rsdt.next()) {
                        if (SRDetailSet.contains(moduleRefDetailId) || SRSMSet.contains(stockMovementID)) {
                            continue;
                        }
                        String qryToUpdate = "UPDATE in_stockmovement SET modulerefdetailid=? WHERE id=?";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(qryToUpdate);
                        stmtforUpdate.setString(1, moduleRefDetailId);
                        stmtforUpdate.setString(2, stockMovementID);
                        stmtforUpdate.executeUpdate();

                        SRSMSet.add(stockMovementID);
                        SRDetailSet.add(moduleRefDetailId);
                        SRCount++;
                        String printStr = SRCount + ")  FROM ELSE " + " UPDATE in_stockmovement SET modulerefdetailid='" + moduleRefDetailId + "' WHERE id='" + stockMovementID + "'";
//                        System.out.println(printStr);
                        out.println("<br/>" + printStr);

                    }
                }
            }
            
            qrry1 = "UPDATE in_stockmovement SET modulerefdetailid=modulerefid WHERE modulerefdetailid IS NULL AND company =?";
            stmtUpdate = conn.prepareStatement(qrry1);
            stmtUpdate.setString(1, companyId);
            stmtUpdate.executeUpdate();


            totalCompanyUpdationCnt++;
        }

        out.println("<br><br><br/><br/> Total companies updated are " + totalCompanyUpdationCnt);
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