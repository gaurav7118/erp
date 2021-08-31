
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
        String serverip = request.getParameter("serverip");                            
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain"); //"amcoweldnew";
        String InvLocId = "0ad57e8b-cdb1-45ea-a1a1-38d3aa013925"; //5b62d8be-0237-40c8-83a7-495c232ad498";
        String InvWarId =  "4028e4d351eb2b800151ed68de8f1621";   //"c0ea4a9a-fad1-46d6-82e1-69fffa012731";

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(subdomain)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password,subdomain) in url. so please provide all these parameter correctly. ");
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
            
            String productid = "", productname = "";
            String queryproduct="select productid from cskbiotempproductid ";
            PreparedStatement stproduct = conn.prepareStatement(queryproduct);  //select all product from company
            ResultSet rsp = stproduct.executeQuery();
            
            while (rsp.next()) {
            productid = rsp.getString("productid");
            
            String queryForbatchselect = "select id from newproductbatch where product=? and company=? ";
            PreparedStatement stmtbat = conn.prepareStatement(queryForbatchselect);
            stmtbat.setObject(1, productid);
            stmtbat.setObject(2, companyId);
           
            ResultSet rs9 = stmtbat.executeQuery();
            while (rs9.next()) {
                String batchids = rs9.getString("id");
                String mappingquery = "delete from locationbatchdocumentmapping where batchmapid= ? ";
                PreparedStatement stmtmapp = conn.prepareStatement(mappingquery);
                stmtmapp.setObject(1, batchids);
                 stmtmapp.executeUpdate();
                
                String batchquery = "delete from newproductbatch where id= ? ";
                PreparedStatement stmtbatchdelete = conn.prepareStatement(batchquery);
                stmtbatchdelete.setObject(1, batchids);
                stmtbatchdelete.executeUpdate();
                
            } 
            
            
            
              //      + " where inventory.newinv='T'  and p.company = ? group by p.id) as T1 where count> 1)";
            /*    String queryproduct="select p.id,p.name from product p where id in ('4028e4d35217486b01521c0257a33065','4028e4d35217486b01521c04365730fb',"
                + " '4028e4d35217486b01521c069c4d3195','4028e4d35217486b01521c070f5331ad','4028e4d35217486b01521c0cc29f32dd','4028e4d35217486b01521c11635f3397',"
                    + " '4028e4d35217486b01521c12f1f733cf','4028e4d35217486b01521c1461673406','4028e4d35217486b01521c15efe93448','4028e4d35217486b01521cd4125247d8',"
                    + " '4028e4d35217486b01521cd4554d482c','4028e4d35217486b01521cd5e14a49b4','4028e4d35217486b01521cd5f2a149c4','4028e4d35217486b01521cd631b349fc', "
                    + " '4028e4d35217486b01521cd7798e4afb','4028e4d35217486b01521cdb74b34d2c','4028e4d35217486b01521cdb7cd04d30','4028e4d35217486b01521cdbdf604d5c', "
                    + " '4028e4d35217486b01521cdbf17c4d64','4028e4d35217486b01521cdc3ad14d84','4028e4d35217486b01521cdc712b4d9c','4028e4d35217486b01521cdd58fc4e00',"
                    + " '4028e4d35217486b01521cf29c285526','4028e4d35217486b01521cf45ef8559a','4028e4d35217486b01521cfe23f957c6','4028e4d35217486b01521cfe2ebd57d6',"
                    + " '4028e4d35217486b01521cfe420e57f2','4028e4d35217486b01521cfe5f47581a','4028e4d35217486b01521cfe68745826','4028e4d35217486b01521cfeee6858c2',"
                    + " '4028e4d35217486b01521cff38be5912','4028e4d35217486b01521cff3cc45916','4028e4d35217486b01521cff92a9596a','4028e4d35217486b01521cffdf6959b6', "
                    + " '4028e4d35217486b01521d00106959e2','4028e4d35217486b01521d0021aa59f2','4028e4d35217486b01521d003a2a5a06','4028e4d35217486b01521d0050885a1a',"
                    + "'4028e4d35217486b01521d0320c55c21','4028e4d35217486b01521d0403f45ca8','4028e4d35217486b01521d0521e95d53','4028e4d35217486b01521d052a015d57','4028e4d35217486b01521d0e4dc060fa','4028e4d35217486b01521d136890628e','4028e4d35217486b01521d14283962c6','4028e4d35217486b01521d18328b63da','4028e4d35217486b01521d1944a9641a','4028e4d35217486b01521d1966566422','4028e4d35217486b01521d1a69c16462','4028e4d35217486b01521d1cc97664fa') ";
            
            PreparedStatement stmtp = conn.prepareStatement(queryproduct);  //select all product from company
           // stmtp.setObject(1, companyId);
            ResultSet rsp = stmtp.executeQuery();
            while (rsp.next()) {
                boolean anyProductRecordUpdated = false;
                productid = rsp.getString("id");
                productname = rsp.getString("name");
                */ 
                /*
                 * *************************************
                 * For : Opening Case **********************************
                 */
                 
                boolean anyProductRecordUpdated = false;
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
                    }
                }

                /**
                 * *************************************
                 * For : GR Case **********************************
                 */
                String querygr = "select id,baseuomdeliveredquantity from grodetails where company=? and product=?";
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
                String querydo = "select id,baseuomdeliveredquantity from dodetails where company=? and product=?";
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
                        stmtb.setDouble(2, 0);
                        stmtb.setDouble(3, -deliveredQty);
                        stmtb.setString(4, InvLocId); //location
                        stmtb.setString(5, InvWarId);  //warehouse
                        stmtb.setString(6, productid);
                        stmtb.setString(7, companyId);
                        stmtb.execute();
                        stmtb.close();

                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,27)";
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
                String querysr = "select id,baseuomquantity from srdetails where company=? and product=?";
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
                String querypr = "select id,baseuomquantity from prdetails where company=? and product=?";
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
                        stmtb.setDouble(2, srQty);
                        stmtb.setDouble(3, srQty);
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
                /**
                 * *************************************
                 * For : Product Build Case **********************************
                 */
                String queryProductBuild = "select id,quantity,batch from productbuild where company=? and product=?";
                PreparedStatement stmtpb = conn.prepareStatement(queryProductBuild);
                stmtpb.setObject(1, companyId);
                stmtpb.setObject(2, productid);
                ResultSet rspb = stmtpb.executeQuery();
                while (rspb.next()) {
                    String productBuildId = rspb.getString("id");
                    double deliveredQty = rspb.getDouble("quantity");
                    String batchId = rspb.getString("batch");

                    boolean isNewBatch = true;
                    double quantitydue = 0.0,batchQty=0.0;
                    if (!StringUtil.isNullOrEmpty(productid)) {
                        String queryForbatch = "select id,quantitydue,quantity from newproductbatch where product=? and company=? ";
                        PreparedStatement stmtbatch = conn.prepareStatement(queryForbatch);
                        stmtbatch.setObject(1, productid);
                        stmtbatch.setObject(2, companyId);
                        ResultSet rs6 = stmtbatch.executeQuery();
                        if (rs6.next()) {
                            batchId = rs6.getString("id");
                            quantitydue = rs6.getDouble("quantitydue");
                            batchQty = rs6.getDouble("quantity");
                            isNewBatch = false;
                        } else {
                            isNewBatch = true;
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid) && !isNewBatch) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse
                        String queryForupdate = "update newproductbatch set quantity=?, quantitydue=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, batchQty + deliveredQty);
                        stmtforUpdate.setDouble(2, quantitydue + deliveredQty);
                        stmtforUpdate.setString(3, batchId);
                        stmtforUpdate.setString(4, companyId);
                        stmtforUpdate.executeUpdate();

                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                        PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                        stmtlbmap.setString(1, locBatchdocId);
                        stmtlbmap.setDouble(2, deliveredQty);
                        stmtlbmap.setString(3, productBuildId);
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
                        stmtlbmap.setString(3, productid);  ////////////////////////////////////////////////////////////////////////////////////////
                        stmtlbmap.setString(4, batchMapId);
                        stmtlbmap.execute();
                        stmtlbmap.close();
                        anyProductRecordUpdated = true;
                    }

                }
                /**
                 * *************************************
                 * For : Stock Adjustment Case **********************************
                 */
                String querySA = "select finalquantity from in_stockadjustment where company=? and product=?";
                PreparedStatement stmtsa = conn.prepareStatement(querySA);
                stmtsa.setObject(1, companyId);
                stmtsa.setObject(2, productid);
                ResultSet rssa = stmtsa.executeQuery();
                while (rssa.next()) {
                    
                    double saQty = rssa.getDouble("finalquantity");
                    

                    boolean isNewBatch = true;
                    double quantitydue = 0.0,batchQty=0.0;
                    String batchId = "";
                    if (!StringUtil.isNullOrEmpty(productid)) {
                        String queryForbatch = "select id,quantitydue,quantity from newproductbatch where product=? and company=? ";
                        PreparedStatement stmtbatch = conn.prepareStatement(queryForbatch);
                        stmtbatch.setObject(1, productid);
                        stmtbatch.setObject(2, companyId);
                        ResultSet rs6 = stmtbatch.executeQuery();
                        if (rs6.next()) {
                            batchId = rs6.getString("id");
                            quantitydue = rs6.getDouble("quantitydue");
                            batchQty = rs6.getDouble("quantity");
                            isNewBatch = false;
                        } else {
                            isNewBatch = true;
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid) && !isNewBatch) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse
                        String queryForupdate = "update newproductbatch set quantity=?, quantitydue=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, batchQty + saQty);
                        stmtforUpdate.setDouble(2, quantitydue + saQty);
                        stmtforUpdate.setString(3, batchId);
                        stmtforUpdate.setString(4, companyId);
                        stmtforUpdate.executeUpdate();
                      
                        anyProductRecordUpdated = true;

                    } else if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid) && isNewBatch) {

                        String batchMapId = java.util.UUID.randomUUID().toString();
                        String querybatch = "insert into newproductbatch (id,batchname,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company) values (?,'',?,?,?,?,28,?,'T','T',?)";
                        PreparedStatement stmtb = conn.prepareStatement(querybatch);
                        stmtb.setString(1, batchMapId);
                        stmtb.setDouble(2, saQty);
                        stmtb.setDouble(3, saQty);
                        stmtb.setString(4, InvLocId); //location
                        stmtb.setString(5, InvWarId);  //warehouse
                        stmtb.setString(6, productid);
                        stmtb.setString(7, companyId);
                        stmtb.execute();
                        stmtb.close();
                      
                        anyProductRecordUpdated = true;
                    }

                }
                totalproductUpdationCnt++;
            //}

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
