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

            if (!StringUtil.isNullOrEmpty(companyId)) {
                //set location,warehouse,Batch option for product
                String queryForupdate = "update compaccpreferences set islocationcompulsory='T',iswarehousecompulsory='T' where id=?  ";
                PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                stmtforUpdate.setString(1, companyId);
                stmtforUpdate.executeUpdate();
                
                String queryFornegupdate = "update extracompanypreferences set isnegativestockforlocwar='T' where id=?  ";
                PreparedStatement stmtfornegUpdate = conn.prepareStatement(queryFornegupdate);
                stmtfornegUpdate.setString(1, companyId);
                stmtfornegUpdate.executeUpdate();
            }

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
           
            String productid = "", productname = "";
            String queryproduct = "select id,name from product where company=? AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa')"; //product type = assembly,inventory part
            PreparedStatement stmtp = conn.prepareStatement(queryproduct);  //select all product from company
            stmtp.setObject(1, companyId);
            ResultSet rsp = stmtp.executeQuery();
            while (rsp.next()) {
                boolean anyProductRecordUpdated = false;
                productid = rsp.getString("id");
                productname = rsp.getString("name");
                if (!StringUtil.isNullOrEmpty(productid)) {
                    //set location option for product
                    String queryForupdate = "update product set islocationforproduct='T',iswarehouseforproduct='T',location=?,warehouse=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, InvLocId);
                    stmtforUpdate.setString(2, InvWarId);
                    stmtforUpdate.setString(3, productid);
                    stmtforUpdate.setString(4, companyId);
                    stmtforUpdate.executeUpdate();
                }

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
                String invquery = "select * from inventory where company=? and product= ? and newinv='T' ";
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

                        String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                        stmt1.setString(1, smId);
                        stmt1.setString(2, companyId);
                        stmt1.setString(3, rsinv.getString("product")); //product
                        stmt1.setString(4, InvWarId);
                        stmt1.setString(5, InvLocId);
                        stmt1.setString(6, rsinv.getString("uom"));//stockuom
                        stmt1.setDouble(7, Invbaseuomquantity); //qty
                        stmt1.setDouble(8, price); //priceperunit
                        stmt1.setDate(9, rsinv.getDate("updatedate")); //createdon
                        stmt1.setString(10, null); // transaction no
                        stmt1.setDate(11, rsinv.getDate("updatedate")); // transaction date
                        stmt1.setInt(12, 1); // stock In : 1 .Stock out : 2
                        stmt1.setInt(13, 10); // transaction module // do- 6,grn-5 ,10 -opening/new product
                        stmt1.setString(14, InvId); // modulerefid (parent) 
                        stmt1.setString(15, "Stock added through OPENING");
                        stmt1.setString(16, null);
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


                    }
                }

                /**
                 * *************************************
                 * For : GR Case **********************************
                 */
                String querygr = "select gro.id,gro.baseuomdeliveredquantity from grodetails gro inner join grorder gr on gr.id=gro.grorder where gro.company=? and gro.product=? and gr.deleteflag='F' ";
                PreparedStatement stmtgr = conn.prepareStatement(querygr);
                stmtgr.setObject(1, companyId);
                stmtgr.setObject(2, productid);
                ResultSet rsgr = stmtgr.executeQuery();
                while (rsgr.next()) {
                    String grdetailid = rsgr.getString("id");
                    double deliveredQty = rsgr.getDouble("baseuomdeliveredquantity");

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
                 * For : DO Case **********************************
                 */
                String querydo = "select dod.id,dod.baseuomdeliveredquantity from dodetails dod inner join deliveryorder do on do.id=dod.deliveryorder where dod.company=? and dod.product=? and do.deleteflag='F' ";
                PreparedStatement stmtdo = conn.prepareStatement(querydo);
                stmtdo.setObject(1, companyId);
                stmtdo.setObject(2, productid);
                ResultSet rsdo = stmtdo.executeQuery();
                while (rsdo.next()) {

                    String dodetailid = rsdo.getString("id");
                    double deliveredQty = rsdo.getDouble("baseuomdeliveredquantity");
                    String batchId = "";
                    boolean isNewBatch = true;
                    double quantitydue = 0.0;
                    if (!StringUtil.isNullOrEmpty(productid)) {
                        String queryForbatch = "select id,quantitydue from newproductbatch where product=? and company=? ";
                        PreparedStatement stmtbatch = conn.prepareStatement(queryForbatch);
                        stmtbatch.setObject(1, productid);
                        stmtbatch.setObject(2, companyId);
                        ResultSet rs6 = stmtbatch.executeQuery();
                        if (rs6.next()) {
                            batchId = rs6.getString("id");
                            quantitydue = rs6.getDouble("quantitydue");
                            isNewBatch = false;
                        } else {
                            isNewBatch = true;
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid) && !isNewBatch) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse
                        String queryForupdate = "update newproductbatch set quantitydue=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, quantitydue - deliveredQty);
                        stmtforUpdate.setString(2, batchId);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();

                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,27)";
                        PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                        stmtlbmap.setString(1, locBatchdocId);
                        stmtlbmap.setDouble(2, deliveredQty);
                        stmtlbmap.setString(3, dodetailid);
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
                        stmtb.setDouble(3, deliveredQty-deliveredQty); //as in deliveryorder order case if bath is not created then create the 0 quantity batch
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
                        stmtlbmap.setString(3, dodetailid);  ////////////////////////////////////////////////////////////////////////////////////////
                        stmtlbmap.setString(4, batchMapId);
                        stmtlbmap.execute();
                        stmtlbmap.close();
                        anyProductRecordUpdated = true;
                    }

                }
                /**
                 * *************************************
                 * For : SR Case **********************************
                 *///modifying script for fasten  as for them negative stock option is on we will have to add update data on one batch onlyy
                String querysr = "select srd.id,srd.baseuomquantity from srdetails srd inner join salesreturn sr on sr.id=srd.salesreturn where srd.company=? and srd.product=? and sr.deleteflag='F' ";
                PreparedStatement stmtsr = conn.prepareStatement(querysr);
                stmtsr.setObject(1, companyId);
                stmtsr.setObject(2, productid);
                ResultSet rssr = stmtsr.executeQuery();
                while (rssr.next()) {

                    String srdetailid = rssr.getString("id");
                    double srQty = rssr.getDouble("baseuomquantity");
                    String batchId = "";
                    boolean isNewBatch = true;
                    double quantitydue = 0.0;
                    if (!StringUtil.isNullOrEmpty(productid)) {
                        String queryForbatch = "select id,quantitydue from newproductbatch where product=? and company=? ";
                        PreparedStatement stmtbatch = conn.prepareStatement(queryForbatch);
                        stmtbatch.setObject(1, productid);
                        stmtbatch.setObject(2, companyId);
                        ResultSet rs6 = stmtbatch.executeQuery();
                        if (rs6.next()) {
                            batchId = rs6.getString("id");
                            quantitydue = rs6.getDouble("quantitydue");
                            isNewBatch = false;
                        } else {
                            isNewBatch = true;
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid) && !isNewBatch) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse
                        String queryForupdate = "update newproductbatch set quantitydue=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, quantitydue + srQty);  //as sales retun case we will add quantity
                        stmtforUpdate.setString(2, batchId);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();

                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,29)";
                        PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                        stmtlbmap.setString(1, locBatchdocId);
                        stmtlbmap.setDouble(2, srQty);
                        stmtlbmap.setString(3, srdetailid);
                        stmtlbmap.setString(4, batchId);
                        stmtlbmap.execute();
                        stmtlbmap.close();
                        anyProductRecordUpdated = true;

                    } else if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid) && isNewBatch) {

                        String batchMapId = java.util.UUID.randomUUID().toString();
                        String querybatch = "insert into newproductbatch (id,batchname,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company) values (?,'',?,?,?,?,28,?,'T','T',?)";
                        PreparedStatement stmtb = conn.prepareStatement(querybatch);
                        stmtb.setString(1, batchMapId);
                        stmtb.setDouble(2, srQty);
                        stmtb.setDouble(3, srQty);
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
                        stmtlbmap.setDouble(2, srQty);
                        stmtlbmap.setString(3, srdetailid);  ////////////////////////////////////////////////////////////////////////////////////////
                        stmtlbmap.setString(4, batchMapId);
                        stmtlbmap.execute();
                        stmtlbmap.close();
                        anyProductRecordUpdated = true;
                    }

                }

                /**
                 * *************************************
                 * For : PR Case **********************************
                 *///modifying script for fasten  as for them negative stock option is on we will have to add update data on one batch onlyy
                 String querypr ="select prd.id,prd.baseuomquantity from prdetails prd inner join purchasereturn pr on pr.id=prd.purchasereturn where prd.company=? and prd.product=? and pr.deleteflag='F' ";
                PreparedStatement stmtpr = conn.prepareStatement(querypr);
                stmtpr.setObject(1, companyId);
                stmtpr.setObject(2, productid);
                ResultSet rspr = stmtpr.executeQuery();
                while (rspr.next()) {

                    String srdetailid = rspr.getString("id");
                    double srQty = rspr.getDouble("baseuomquantity");
                    String batchId = "";
                    boolean isNewBatch = true;
                    double quantitydue = 0.0;
                    if (!StringUtil.isNullOrEmpty(productid)) {
                        String queryForbatch = "select id,quantitydue from newproductbatch where product=? and company=? ";
                        PreparedStatement stmtbatch = conn.prepareStatement(queryForbatch);
                        stmtbatch.setObject(1, productid);
                        stmtbatch.setObject(2, companyId);
                        ResultSet rs6 = stmtbatch.executeQuery();
                        if (rs6.next()) {
                            batchId = rs6.getString("id");
                            quantitydue = rs6.getDouble("quantitydue");
                            isNewBatch = false;
                        } else {
                            isNewBatch = true;
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid) && !isNewBatch) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse
                        String queryForupdate = "update newproductbatch set quantitydue=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, quantitydue - srQty);  //as purchase  retun case we will add quantity
                        stmtforUpdate.setString(2, batchId);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();

                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,31)";
                        PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                        stmtlbmap.setString(1, locBatchdocId);
                        stmtlbmap.setDouble(2, srQty);
                        stmtlbmap.setString(3, srdetailid);
                        stmtlbmap.setString(4, batchId);
                        stmtlbmap.execute();
                        stmtlbmap.close();
                        anyProductRecordUpdated = true;

                    } else if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid) && isNewBatch) {

                        String batchMapId = java.util.UUID.randomUUID().toString();
                        String querybatch = "insert into newproductbatch (id,batchname,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company) values (?,'',?,?,?,?,28,?,'T','T',?)";
                        PreparedStatement stmtb = conn.prepareStatement(querybatch);
                        stmtb.setString(1, batchMapId);
                        stmtb.setDouble(2, -srQty);
                        stmtb.setDouble(3, -srQty);
                        stmtb.setString(4, InvLocId); //location
                        stmtb.setString(5, InvWarId);  //warehouse
                        stmtb.setString(6, productid);
                        stmtb.setString(7, companyId);
                        stmtb.execute();
                        stmtb.close();

                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,31)";
                        PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                        stmtlbmap.setString(1, locBatchdocId);
                        stmtlbmap.setDouble(2, srQty);
                        stmtlbmap.setString(3, srdetailid);  ////////////////////////////////////////////////////////////////////////////////////////
                        stmtlbmap.setString(4, batchMapId);
                        stmtlbmap.execute();
                        stmtlbmap.close();
                        anyProductRecordUpdated = true;
                    }

                }
                if (anyProductRecordUpdated) {
                    totalproductUpdationCnt++;
                }
                System.out.println("Count is" + totalproductUpdationCnt);
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