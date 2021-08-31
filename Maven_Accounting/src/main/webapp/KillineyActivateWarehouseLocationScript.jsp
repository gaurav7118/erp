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
//        String dbName = "sg";
//        String userName = "root";
//        String password = "krawler";
//        String subdomain = "killiney";
        
        String serverip = request.getParameter("serverip");//"192.168.0.208";                            
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"newstaging";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = "killiney";

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname FROM company ";
        //if (!StringUtil.isNullOrEmpty(subdomain)) {
        query += " where subdomain= ?";
        //}
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        int totalproductUpdationCnt = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");

            String InvLocId = "d060b8ad-f69d-4eae-916a-9f2c26756fcb";  //Default Location
            String InvWarId = "ff808081533c925501533f69781e0578";  //MWH

            String queryForWLupdate = "update product set islocationforproduct='T',iswarehouseforproduct='T',location=?,warehouse=? where company=? ";
            PreparedStatement stmtforwlUpdate = conn.prepareStatement(queryForWLupdate);
            stmtforwlUpdate.setString(1, InvLocId);
            stmtforwlUpdate.setString(2, InvWarId);
            stmtforwlUpdate.setString(3, companyId);
            stmtforwlUpdate.executeUpdate();

            String delInvZero = " DELETE from inventory WHERE company=	?  AND newinv='T' AND isopening='T' AND carryin='T'  AND baseuomquantity=0 AND quantity=0";
            PreparedStatement smdDel = conn.prepareStatement(delInvZero);
            smdDel.setString(1, companyId);

            String productid = "", productname = "";
            String queryproduct = "select id,name from product where company=? AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa')"; //product type = assembly,inventory part
            PreparedStatement stmtp = conn.prepareStatement(queryproduct);  //select all product from company
            stmtp.setObject(1, companyId);
            ResultSet rsp = stmtp.executeQuery();
            while (rsp.next()) {
                boolean anyProductRecordUpdated = false;
                productid = rsp.getString("id");
                productname = rsp.getString("name");

                String stockInsertQry = "select sum((case when carryin='T' then baseuomquantity else -baseuomquantity end)) as qty from inventory  LEFT JOIN product  p ON  p.id=inventory.product where inventory.deleteflag='F' and p.producttype <>'4efb0286-5627-102d-8de6-001cc0794cfa'  AND  inventory.company=? and product=? group by product";
                PreparedStatement stmtstk = conn.prepareStatement(stockInsertQry);
                stmtstk.setObject(1, companyId);
                stmtstk.setObject(2, productid);
                ResultSet rsstk = stmtstk.executeQuery();
                if (rsstk.next()) {

                    double Invbaseuomquantity = rsstk.getDouble("qty");

                    String query2 = "INSERT INTO in_stock(id, product, store, location, batchname, serialnames, company, quantity, createdon, modifiedon) "
                            + " VALUES (UUID(), ?,?,?,?,?,?,?, NOW(), NOW()) ";
                    PreparedStatement stmtquery2 = conn.prepareStatement(query2);
                    stmtquery2.setString(1, productid);
                    stmtquery2.setString(2, InvWarId);
                    stmtquery2.setString(3, InvLocId);
                    stmtquery2.setString(4, "");
                    stmtquery2.setString(5, null);
                    stmtquery2.setString(6, companyId);
                    stmtquery2.setDouble(7, Invbaseuomquantity);
                    stmtquery2.execute();
                    stmtquery2.close();
                }
                stmtstk.close();


                /*
                 * *************************************
                 * For : Opening Case **********************************
                 */
                String InvId = "";
                double Invbaseuomquantity = 0.0, Invquantity = 0.0, price = 0.0;
                String invquery = "select * from inventory where company=? and product= ? and newinv='T' AND isopening='T' AND carryin='T' ";
                PreparedStatement stmtinv = conn.prepareStatement(invquery);
                stmtinv.setObject(1, companyId);
                stmtinv.setObject(2, productid);
                ResultSet rsinv = stmtinv.executeQuery();
                if (rsinv.next()) {
                    InvId = rsinv.getString("id");
                    Invquantity = rsinv.getDouble("quantity");
                    Invbaseuomquantity = rsinv.getDouble("baseuomquantity");
                    price = rsinv.getDouble("baseuomrate") * Invbaseuomquantity;
                }
                if (!StringUtil.isNullOrEmpty(InvId)) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse
                    if (Invbaseuomquantity > 0) {
                        String batchMapId = java.util.UUID.randomUUID().toString();
                        String querybatch = "insert into newproductbatch (id,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company) values (?,?,?,?,?,28,?,'T','T',?)";
                        PreparedStatement stmtb = conn.prepareStatement(querybatch);
                        stmtb.setString(1, batchMapId);
                        stmtb.setDouble(2, Invbaseuomquantity);
                        stmtb.setDouble(3, Invbaseuomquantity);
                        stmtb.setString(4, InvLocId); //location
                        stmtb.setString(5, InvWarId);  //warehouse
                        stmtb.setString(6, productid);
                        stmtb.setString(7, companyId);
                        stmtb.execute();
                        stmtb.close();

                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                        PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                        stmtlbmap.setString(1, locBatchdocId);
                        stmtlbmap.setDouble(2, Invbaseuomquantity);
                        stmtlbmap.setString(3, productid);
                        stmtlbmap.setString(4, batchMapId);
                        stmtlbmap.execute();
                        stmtlbmap.close();
                        anyProductRecordUpdated = true;

                        /**
                         * **************************************
                         * Stock Movement Entry
                         * **********************************
                         */
                        String smId = java.util.UUID.randomUUID().toString();
                        String smDetailId = java.util.UUID.randomUUID().toString();

                        String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,modulerefdetailid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                        stmt1.setString(1, smId);
                        stmt1.setString(2, companyId);
                        stmt1.setString(3, rsinv.getString("product")); //product
                        stmt1.setString(4, InvWarId);
                        stmt1.setString(5, null);
                        stmt1.setString(6, rsinv.getString("uom"));//stockuom
                        stmt1.setDouble(7, Invbaseuomquantity); //qty
                        stmt1.setDouble(8, price); //priceperunit
                        stmt1.setDate(9, rsinv.getDate("updatedate")); //createdon
                        stmt1.setString(10, null); // transaction no
                        stmt1.setDate(11, rsinv.getDate("updatedate")); // transaction date
                        stmt1.setInt(12, 1); // stock In : 1 .Stock out : 2
                        stmt1.setInt(13, 10); // transaction module // do- 6,grn-5 ,10 -opening/new product
                        stmt1.setString(14, productid); // modulerefid (parent) 
                        stmt1.setString(15, productid); // modulerefdetailid (row) 
                        stmt1.setString(16, "Stock added through OPENING");
                        stmt1.setString(17, null);
                        stmt1.setString(18, null);  // vendor
                        stmt1.setString(19, null); // customer
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

                    }
                }

                /**
                 * *************************************
                 * For : GR Case **********************************
                 */
                String querygr = "select id,baseuomdeliveredquantity,grorder from grodetails where company=? and product=?";
                PreparedStatement stmtgr = conn.prepareStatement(querygr);
                stmtgr.setObject(1, companyId);
                stmtgr.setObject(2, productid);
                ResultSet rsgr = stmtgr.executeQuery();
                while (rsgr.next()) {
                    String grdetailid = rsgr.getString("id");
                    double deliveredQty = rsgr.getDouble("baseuomdeliveredquantity");
                    String grOrderId = rsgr.getString("grorder");

                    String batchId = "";
                    boolean isNewBatch = true;
                    double batchQty = 0;
                    double quantitydue = 0.0;
                    if (!StringUtil.isNullOrEmpty(productid)) {
                        String queryForbatch = "select id,quantity,quantitydue from newproductbatch where product=? and company=? ";
                        PreparedStatement stmtbatch = conn.prepareStatement(queryForbatch);
                        stmtbatch.setObject(1, productid);
                        stmtbatch.setObject(2, companyId);
                        ResultSet rs6 = stmtbatch.executeQuery();
                        if (rs6.next()) {
                            batchId = rs6.getString("id");
                            batchQty = rs6.getDouble("quantity");
                            quantitydue = rs6.getDouble("quantitydue");
                            isNewBatch = false;
                        } else {
                            isNewBatch = true;
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid) && !isNewBatch) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse
                        String queryForupdate = "update newproductbatch set quantitydue=?,quantity=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, quantitydue + deliveredQty);
                        stmtforUpdate.setDouble(2, batchQty + deliveredQty);
                        stmtforUpdate.setString(3, batchId);
                        stmtforUpdate.setString(4, companyId);
                        stmtforUpdate.executeUpdate();

                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                        PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                        stmtlbmap.setString(1, locBatchdocId);
                        stmtlbmap.setDouble(2, deliveredQty);
                        stmtlbmap.setString(3, grdetailid);
                        stmtlbmap.setString(4, batchId);
                        stmtlbmap.execute();
                        stmtlbmap.close();

                        anyProductRecordUpdated = true;

                    } else if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid) && isNewBatch) {

                        String batchMapId = java.util.UUID.randomUUID().toString();
                        String querybatch = "insert into newproductbatch (id,batchname,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company) values (?,'',?,?,?,?,28,?,'T','T',?)";
                        PreparedStatement stmtb = conn.prepareStatement(querybatch);
                        stmtb.setString(1, batchMapId);
                        stmtb.setDouble(2, deliveredQty);
                        stmtb.setDouble(3, deliveredQty);
                        stmtb.setString(4, InvLocId); //location
                        stmtb.setString(5, InvWarId);  //warehouse
                        stmtb.setString(6, productid);
                        stmtb.setString(7, companyId);
                        stmtb.execute();
                        stmtb.close();

                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                        PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                        stmtlbmap.setString(1, locBatchdocId);
                        stmtlbmap.setDouble(2, deliveredQty);
                        stmtlbmap.setString(3, grdetailid);  ////////////////////////////////////////////////////////////////////////////////////////
                        stmtlbmap.setString(4, batchMapId);
                        stmtlbmap.execute();
                        stmtlbmap.close();

                        anyProductRecordUpdated = true;
                    }

                }

                /**
                 * *************************************
                 * For : GRN Stock Movement Entry
                 * **********************************
                 */
                String grnQry = "SELECT gr.*,grd.*,FROM_UNIXTIME(gr.createdon/1000) as createdondate,(baseuomdeliveredquantity*baseuomrate) as price from grorder gr INNER JOIN grodetails grd ON gr.id=grd.grorder  WHERE  gr.company=? and gr.isfixedassetgro=0  "; // only for non asset type gro
                PreparedStatement stmtgrn = conn.prepareStatement(grnQry);
                stmtgrn.setObject(1, companyId);
                ResultSet rs1 = stmtgrn.executeQuery();
                while (rs1.next()) {
                    String smId = java.util.UUID.randomUUID().toString();
                    String smDetailId = java.util.UUID.randomUUID().toString();

                    String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,modulerefdetailid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                    stmt1.setString(1, smId);
                    stmt1.setString(2, companyId);
                    stmt1.setString(3, rs1.getString("product")); //product
                    stmt1.setString(4, InvWarId);
                    stmt1.setString(5, InvLocId);
                    stmt1.setString(6, rs1.getString("uom"));//stockuom
                    stmt1.setDouble(7, rs1.getDouble("baseuomdeliveredquantity")); //qty
                    stmt1.setDouble(8, rs1.getDouble("rate")); //priceperunit
                    stmt1.setDate(9, rs1.getDate("createdondate")); //createdon
                    stmt1.setString(10, rs1.getString("gronumber")); // transaction no
                    stmt1.setDate(11, rs1.getDate("grorderdate")); // transaction date
                    stmt1.setInt(12, 1); // stock In : 1 .Stock out : 2
                    stmt1.setInt(13, 5); // transaction module // do- 6,grn-5
                    stmt1.setString(14, rs1.getString("id")); // modulerefid (parent) 
                    stmt1.setString(15, rs1.getString("grorder")); // modulerefdetailid (row) 
                    stmt1.setString(16, "Stock added through GR");
                    stmt1.setString(17, rs1.getString("costcenter"));
                    stmt1.setString(18, rs1.getString("vendor"));  // vendor
                    stmt1.setString(19, null); // customer
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
                }
            }
            totalCompanyUpdationCnt++;
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
        out.println("<br><br> Total products updated are " + totalproductUpdationCnt);
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