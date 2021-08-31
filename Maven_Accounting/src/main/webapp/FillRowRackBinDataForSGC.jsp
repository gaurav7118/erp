<%-- 
    Document   : FillEmtptyRowRackBinData
    Created on : Dec 30, 2015, 2:50:45 PM
    Author     : krawler
--%>


<%@page import="com.krawler.hql.accounting.Producttype"%>
<%@page import="java.util.UUID"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%
            Connection conn = null;
            try {
                String serverip = request.getParameter("serverip");//"192.168.0.208";                            
                String port = request.getParameter("port");//"3306";
                String dbName = request.getParameter("dbname");//"newstaging";
                String userName = request.getParameter("username");//"krawlersqladmin";
                String password = request.getParameter("password"); //"krawler"
                String subdomain = request.getParameter("subdomain");

                if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
                    throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
                }
                String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
                String driver = "com.mysql.jdbc.Driver";

                Class.forName(driver).newInstance();
                conn = DriverManager.getConnection(connectString, userName, password);

                String companyId = "c5b7532b-e73f-4003-8d89-515382ba4230"; // SGC companyid
                //for SGC Only
                String updatesmmoduleRefid = " UPDATE  in_stockmovement SET modulerefid = '4028e4d351a5e2650151a96ccebf0afc' WHERE id = '4028e4d35047c35c01504ba65f0d0969' ";
                PreparedStatement uptsmrefstmt = conn.prepareStatement(updatesmmoduleRefid);
                uptsmrefstmt.execute();
                uptsmrefstmt.close();
                updatesmmoduleRefid = " UPDATE  in_stockmovement SET modulerefid = '4028e4d350aef69f0150b2660e1324e6' WHERE id = '4028e4d3508ccff90150a39360f43a83'";
                uptsmrefstmt = conn.prepareStatement(updatesmmoduleRefid);
                uptsmrefstmt.execute();
                uptsmrefstmt.close();
                

                String sql = "SELECT tbl.id,  tbl2.product, npb.warehouse, npb.location, npb.`row`, npb.rack, npb.bin, npb.batchname, 5 AS tm FROM grorder tbl "
                        + " INNER JOIN grodetails tbl2 ON tbl.id=tbl2.grorder "
                        + " INNER JOIN product p ON p.id = tbl2.product "
                        + " INNER JOIN locationbatchdocumentmapping lbm ON lbm.documentid = tbl2.id "
                        + " INNER JOIN newproductbatch npb ON lbm.batchmapid = npb.id"
                        + " WHERE  tbl.company = ? "
                        + " UNION ALL "
                        + " SELECT tbl.id,  tbl2.product, npb.warehouse, npb.location, npb.`row`, npb.rack, npb.bin, npb.batchname, 6 AS tm FROM deliveryorder tbl "
                        + " INNER JOIN dodetails tbl2 ON tbl.id=tbl2.deliveryorder"
                        + " INNER JOIN product p ON p.id = tbl2.product"
                        + " INNER JOIN locationbatchdocumentmapping lbm ON lbm.documentid = tbl2.id"
                        + " INNER JOIN newproductbatch npb ON lbm.batchmapid = npb.id"
                        + " WHERE  tbl.company = ? "
                        + " UNION ALL"
                        + " SELECT tbl.id,  tbl2.product, npb.warehouse, npb.location, npb.`row`, npb.rack, npb.bin, npb.batchname,8 AS tm FROM purchasereturn tbl "
                        + " INNER JOIN prdetails tbl2 ON tbl.id=tbl2.purchasereturn"
                        + " INNER JOIN product p ON p.id = tbl2.product"
                        + " INNER JOIN locationbatchdocumentmapping lbm ON lbm.documentid = tbl2.id"
                        + " INNER JOIN newproductbatch npb ON lbm.batchmapid = npb.id"
                        + " WHERE  tbl.company =  ? "
                        + " UNION ALL"
                        + " SELECT tbl.id,  tbl2.product, npb.warehouse, npb.location, npb.`row`, npb.rack, npb.bin, npb.batchname,9 AS tm FROM salesreturn tbl "
                        + " INNER JOIN srdetails tbl2 ON tbl.id=tbl2.salesreturn"
                        + " INNER JOIN product p ON p.id = tbl2.product"
                        + " INNER JOIN locationbatchdocumentmapping lbm ON lbm.documentid = tbl2.id"
                        + " INNER JOIN newproductbatch npb ON lbm.batchmapid = npb.id"
                        + " WHERE  tbl.company =  ? ";

                String updateQuery = "UPDATE in_sm_detail smd INNER JOIN in_stockmovement  sm SET smd.`row` = ?, smd.rack = ? , smd.bin = ? "
                        + " WHERE  smd.stockmovement = sm.id AND smd.`row` IS NULL AND smd.rack IS NULL AND smd.bin IS NULL "
                        + " AND sm.modulerefid = ? AND sm.transaction_module = ? AND sm.product = ? AND sm.store = ? AND smd.location = ? AND smd.batchname = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setObject(1, companyId);
                stmt.setObject(2, companyId);
                stmt.setObject(3, companyId);
                stmt.setObject(4, companyId);
                ResultSet rspr = stmt.executeQuery();
                int count = 0;
                while (rspr.next()) {
                    PreparedStatement uptsmstmt = conn.prepareStatement(updateQuery);
                    uptsmstmt.setObject(1, rspr.getObject("row"));
                    uptsmstmt.setObject(2, rspr.getObject("rack"));
                    uptsmstmt.setObject(3, rspr.getObject("bin"));
                    uptsmstmt.setObject(4, rspr.getObject("id"));
                    uptsmstmt.setObject(5, rspr.getObject("tm"));
                    uptsmstmt.setObject(6, rspr.getObject("product"));
                    uptsmstmt.setObject(7, rspr.getObject("warehouse"));
                    uptsmstmt.setObject(8, rspr.getObject("location"));
                    uptsmstmt.setObject(9, rspr.getObject("batchname"));
                    boolean u = uptsmstmt.execute();
                    uptsmstmt.close();
                    if(u){
                        count++;
                    }
                }
                stmt.close();
                if(count == 0){
                    out.println("<br>No records for updating Row/Rack/Bin in Stock Movement ");
                }else{
                    out.println("<br><br>Updated record for Row/Rack/Bin in Stock Movement " + count);
                }
                
                count = 0;

                sql = "SELECT id, product, batchname, warehouse, location,`row`, rack, bin, SUM(quantity) AS qty, SUM(quantitydue) AS qtydue FROM newproductbatch s "
                        + " WHERE  s.company = ? GROUP BY product, batchname, warehouse, location,`row`, rack, bin HAVING COUNT(*) >1";

                stmt = conn.prepareStatement(sql);
                stmt.setObject(1, companyId);
                ResultSet duplicateRec = stmt.executeQuery();
                while (duplicateRec.next()) {
                    Object batchId = duplicateRec.getObject("id");
                    Object product = duplicateRec.getObject("product");
                    Object batchName = duplicateRec.getObject("batchname");
                    Object warehouse = duplicateRec.getObject("warehouse");
                    Object location = duplicateRec.getObject("location");
                    Object row = duplicateRec.getObject("row");
                    Object rack = duplicateRec.getObject("rack");
                    Object bin = duplicateRec.getObject("bin");
                    Object qty = duplicateRec.getObject("qty");
                    Object qtydue = duplicateRec.getObject("qtydue");


                    String updateBatchMapping = "UPDATE locationbatchdocumentmapping SET batchmapid = ?  WHERE batchmapid  IN (SELECT id FROM newproductbatch WHERE id <> ? AND product = ? AND  batchname=? AND warehouse = ? AND location = ?  AND `row` IS null AND rack = ? AND bin  IS null) ";
                    PreparedStatement uptsmstmt = conn.prepareStatement(updateBatchMapping);
                    uptsmstmt.setObject(1, batchId);
                    uptsmstmt.setObject(2, batchId);
                    uptsmstmt.setObject(3, product);
                    uptsmstmt.setObject(4, batchName);
                    uptsmstmt.setObject(5, warehouse);
                    uptsmstmt.setObject(6, location);
                    uptsmstmt.setObject(7, rack);
                    uptsmstmt.execute();
                    uptsmstmt.close();

                    String updateSerialBatch = "UPDATE newbatchserial SET batch = ? WHERE batch IN (SELECT id FROM newproductbatch WHERE id <> ? AND product = ? AND  batchname=? AND warehouse = ? AND location = ?  AND `row` IS null AND rack = ? AND bin  IS null) ";
                    uptsmstmt = conn.prepareStatement(updateSerialBatch);
                    uptsmstmt.setObject(1, batchId);
                    uptsmstmt.setObject(2, batchId);
                    uptsmstmt.setObject(3, product);
                    uptsmstmt.setObject(4, batchName);
                    uptsmstmt.setObject(5, warehouse);
                    uptsmstmt.setObject(6, location);
                    uptsmstmt.setObject(7, rack);
                    uptsmstmt.execute();
                    uptsmstmt.close();

                    String removeDuplicateBatch = "DELETE FROM newproductbatch WHERE id <> ? AND product = ? AND  batchname=? AND warehouse = ? AND location = ?  AND `row` IS null AND rack = ? AND bin  IS null";

                    uptsmstmt = conn.prepareStatement(removeDuplicateBatch);
                    uptsmstmt.setObject(1, batchId);
                    uptsmstmt.setObject(2, product);
                    uptsmstmt.setObject(3, batchName);
                    uptsmstmt.setObject(4, warehouse);
                    uptsmstmt.setObject(5, location);
                    uptsmstmt.setObject(6, rack);
                    uptsmstmt.execute();
                    uptsmstmt.close();

                    String updateBatchQty = "UPDATE newproductbatch SET quantity = ?, quantitydue = ? WHERE id = ? ";
                    uptsmstmt = conn.prepareStatement(updateBatchQty);
                    uptsmstmt.setObject(1, qty);
                    uptsmstmt.setObject(2, qtydue);
                    uptsmstmt.setObject(3, batchId);
                    uptsmstmt.execute();
                    uptsmstmt.close();
                    
                    count ++;
                }
                stmt.close();
                if(count == 0){
                    out.println("<br><br>No duplicate records to merge ");
                }else{
                    out.println("<br><br>Merged duplicate reords for stock " + count);
                }
                count = 0;
                sql = "DELETE FROM in_stock WHERE company = ? ";
                stmt = conn.prepareStatement(sql);
                stmt.setObject(1, companyId);
                stmt.execute();
                stmt.close();
                out.println("<br><br>Deleted empty rows for stock.");
                
                sql = "SELECT npb.product, npb.batchname, npb.warehouse, npb.location,npb.`row`, npb.rack, npb.bin, SUM(npb.quantitydue) AS qty, GROUP_CONCAT(nbs.serialname) AS serialnames  FROM newproductbatch npb "
                    +" INNER JOIN product p ON npb.product = p.id"
                    +" LEFT JOIN newbatchserial nbs ON nbs.batch = npb.id AND nbs.quantitydue = 1 "
                    +" WHERE  npb.company = ? AND p.producttype IN(?,?) AND npb.warehouse IS NOT NULL AND npb.location IS NOT NULL GROUP BY product, batchname, warehouse, location,`row`, rack, bin ";
                stmt = conn.prepareStatement(sql);
                stmt.setObject(1, companyId);
                stmt.setObject(2, Producttype.INVENTORY_PART);
                stmt.setObject(3, Producttype.ASSEMBLY);
                ResultSet stockRec = stmt.executeQuery();
                
                while (stockRec.next()) {
                    String insertStock = "INSERT INTO in_stock(id, product, store, location, `row`, rack, bin, batchname, serialnames, quantity, company , createdon, modifiedon) "
                            +" VALUES(UUID(), ?,?,?,?,?,?,?,?,?,?, NOW(), NOW())";
                    PreparedStatement pstmt = conn.prepareStatement(insertStock);
                    pstmt.setObject(1, stockRec.getObject("product"));
                    pstmt.setObject(2, stockRec.getObject("warehouse"));
                    pstmt.setObject(3, stockRec.getObject("location"));
                    pstmt.setObject(4, stockRec.getObject("row"));
                    pstmt.setObject(5, stockRec.getObject("rack"));
                    pstmt.setObject(6, stockRec.getObject("bin"));
                    pstmt.setObject(7, stockRec.getObject("batchname"));
                    pstmt.setObject(8, stockRec.getObject("serialnames"));
                    pstmt.setObject(9, stockRec.getObject("qty"));
                    pstmt.setObject(10, companyId);
                    pstmt.execute();
                    pstmt.close();
                    
                    count++;
                }
                stmt.close();
                
                out.println("<br><br>Stock updated successfully");

            } catch (Exception e) {
                e.printStackTrace();
                out.print(e.toString());
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }

        %>
    </body>
</html>
