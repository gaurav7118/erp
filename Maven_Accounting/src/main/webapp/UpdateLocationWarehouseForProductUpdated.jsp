
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
        String InvLocId = request.getParameter("location");//"e5d1688c-dbee-4c72-a4a7-868a27517031";
        String InvWarId =  request.getParameter("warehouse");//"4028e4d35af53b72015af58e510f039a";//   

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(subdomain)) {
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
            String companyId = "62ab6cfe-7d03-468b-b370-273aa7e5fc90";//rs.getString("companyid");

            String productid = "", productname = "";
            String queryproduct = "select id from product where company=? "; //where productid='402881e54bb59cd0014bba0579a50495'
            PreparedStatement stproduct = conn.prepareStatement(queryproduct);  //select all product from company
            stproduct.setObject(1, companyId);
            ResultSet rsp = stproduct.executeQuery();

            while (rsp.next()) {
                productid = rsp.getString("id");

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

                }             /*
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

                        /**
                         * In Opening case deleting location batch document
                         * mapping
                         */
                        String mappingquery = "delete from locationbatchdocumentmapping where documentid= ? ";
                        PreparedStatement stmtlocmap = conn.prepareStatement(mappingquery);
                        stmtlocmap.setObject(1, productid);
                        stmtlocmap.executeUpdate();

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
                String querygr = "select grd.id,baseuomdeliveredquantity,gr.deleteflag from grodetails grd "
                        + " inner join grorder gr on gr.id=grd.grorder where gr.company=? and product=?   ";
                PreparedStatement stmtgr = conn.prepareStatement(querygr);
                stmtgr.setObject(1, companyId);
                stmtgr.setObject(2, productid);
                ResultSet rsgr = stmtgr.executeQuery();
                while (rsgr.next()) {
                    String grdetailid = rsgr.getString("id");
                    double deliveredQty = rsgr.getDouble("baseuomdeliveredquantity");
                    boolean deleteflag = rsgr.getBoolean("deleteflag");

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

                        /**
                         * deleting location batch document mapping before
                         * inserting new entery
                         */
                        if (!StringUtil.isNullOrEmpty(grdetailid)) {
                            String mappingquery = "delete from locationbatchdocumentmapping where documentid= ? ";
                            PreparedStatement stmtlocmap = conn.prepareStatement(mappingquery);
                            stmtlocmap.setObject(1, grdetailid);
                            stmtlocmap.executeUpdate();
                        }


                    }

                    if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid) && !isNewBatch) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse
                        if (!deleteflag) {
                            String queryForupdate = "update newproductbatch set quantitydue=?,quantity=? where id=? and company=? ";
                            PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                            stmtforUpdate.setDouble(1, quantitydue + deliveredQty);
                            stmtforUpdate.setDouble(2, batchQty + deliveredQty);
                            stmtforUpdate.setString(3, batchId);
                            stmtforUpdate.setString(4, companyId);
                            stmtforUpdate.executeUpdate();
                        }
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
                        stmtb.setDouble(2, (deleteflag) ? 0 : deliveredQty);
                        stmtb.setDouble(3, (deleteflag) ? 0 : deliveredQty);
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
                String querydo = "select dt.id,baseuomdeliveredquantity,d.deleteflag from dodetails dt "
                        + " inner join deliveryorder d on d.id=dt.deliveryorder where dt.company=? and product=?";
                PreparedStatement stmtdo = conn.prepareStatement(querydo);
                stmtdo.setObject(1, companyId);
                stmtdo.setObject(2, productid);
                ResultSet rsdo = stmtdo.executeQuery();
                while (rsdo.next()) {

                    String dodetailid = rsdo.getString("id");
                    double deliveredQty = rsdo.getDouble("baseuomdeliveredquantity");
                    boolean deleteflag = rsdo.getBoolean("deleteflag");
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

                        /**
                         * deleting location batch document mapping before
                         * inserting new entery
                         */
                        if (!StringUtil.isNullOrEmpty(dodetailid)) {
                            String mappingquery = "delete from locationbatchdocumentmapping where documentid= ? ";
                            PreparedStatement stmtlocmap = conn.prepareStatement(mappingquery);
                            stmtlocmap.setObject(1, dodetailid);
                            stmtlocmap.executeUpdate();
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid) && !isNewBatch) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse
                        if (!deleteflag) {
                            String queryForupdate = "update newproductbatch set quantitydue=? where id=? and company=? ";
                            PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                            stmtforUpdate.setDouble(1, quantitydue - deliveredQty);
                            stmtforUpdate.setString(2, batchId);
                            stmtforUpdate.setString(3, companyId);
                            stmtforUpdate.executeUpdate();
                        }
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
                        stmtb.setDouble(3, (deleteflag)?0:-deliveredQty);
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

                        /**
                         * deleting location batch document mapping before
                         * inserting new entery
                         */
                        if (!StringUtil.isNullOrEmpty(srdetailid)) {
                            String mappingquery = "delete from locationbatchdocumentmapping where documentid= ? ";
                            PreparedStatement stmtlocmap = conn.prepareStatement(mappingquery);
                            stmtlocmap.setObject(1, srdetailid);
                            stmtlocmap.executeUpdate();
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

                        /**
                         * deleting location batch document mapping before
                         * inserting new entery
                         */
                        if (!StringUtil.isNullOrEmpty(srdetailid)) {
                            String mappingquery = "delete from locationbatchdocumentmapping where documentid= ? ";
                            PreparedStatement stmtlocmap = conn.prepareStatement(mappingquery);
                            stmtlocmap.setObject(1, srdetailid);
                            stmtlocmap.executeUpdate();
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
                    double quantitydue = 0.0, batchQty = 0.0;
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
                        /**
                         * deleting location batch document mapping before
                         * inserting new entery
                         */
                        if (!StringUtil.isNullOrEmpty(productBuildId)) {
                            String mappingquery = "delete from locationbatchdocumentmapping where documentid= ? ";
                            PreparedStatement stmtlocmap = conn.prepareStatement(mappingquery);
                            stmtlocmap.setObject(1, productBuildId);
                            stmtlocmap.executeUpdate();
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
                 * For : Stock Adjustment Case
                 * **********************************
                 */
                String querySA = "select finalquantity from in_stockadjustment where company=? and product=?";
                PreparedStatement stmtsa = conn.prepareStatement(querySA);
                stmtsa.setObject(1, companyId);
                stmtsa.setObject(2, productid);
                ResultSet rssa = stmtsa.executeQuery();
                while (rssa.next()) {

                    double saQty = rssa.getDouble("finalquantity");


                    boolean isNewBatch = true;
                    double quantitydue = 0.0, batchQty = 0.0;
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
