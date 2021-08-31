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
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname FROM company ";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query += " where subdomain= ?";
        }
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        int totalproductUpdationCnt = 0;
        int totaldoUpdationCnt = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");
        
            if (!StringUtil.isNullOrEmpty(companyId)) {
                    //set location,warehouse,Batch option for product
                String queryForupdate = "update compaccpreferences set islocationcompulsory='T',iswarehousecompulsory='T',isBatchCompulsory='T' where id=?  ";
                 PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                 stmtforUpdate.setString(1, companyId);
                 stmtforUpdate.executeUpdate();
                }
            String InvLocId = "";
            String queryinvid = "select id from inventorylocation where company=? and isdefault=1 ";  //is default location availble
            PreparedStatement stmt4 = conn.prepareStatement(queryinvid);
            stmt4.setObject(1, companyId);
            ResultSet rs4 = stmt4.executeQuery();
            if (rs4.next()) {
                InvLocId = rs4.getString("id");
            }
            if (StringUtil.isNullOrEmpty(InvLocId)) { //if not availble create new  default location
                InvLocId = java.util.UUID.randomUUID().toString();
                String queryInv = "insert into inventorylocation(id,name,isdefault,parentid,parent,company) values (?,'Default Location',1,NULL,NULL,?)";
                PreparedStatement stmtInv = conn.prepareStatement(queryInv);
                stmtInv.setString(1, InvLocId);
                stmtInv.setString(2, companyId);
                stmtInv.executeUpdate();
                stmtInv.close();
            }


            String InvWarId = "";
            String queryWarid = "select id from inventorywarehouse where company=? and isdefault=1 "; //is default warehouse availble
            PreparedStatement stmt5 = conn.prepareStatement(queryWarid);
            stmt5.setObject(1, companyId);
            ResultSet rs5 = stmt5.executeQuery();
            if (rs5.next()) {
                InvWarId = rs5.getString("id");
            }
            if (StringUtil.isNullOrEmpty(InvWarId)) { //if not availble create new  default warehouse
                InvWarId = java.util.UUID.randomUUID().toString();
                String queryWar = "insert into inventorywarehouse(id,name,isdefault,parentid,parent,isForCustomer,company) values (?,'Default warehouse',1,NULL,NULL,'F',?)";
                PreparedStatement stmtWar = conn.prepareStatement(queryWar);
                stmtWar.setString(1, InvWarId);
                stmtWar.setString(2, companyId);
                stmtWar.executeUpdate();
                stmtWar.close();
            }

            String productid = "", productname = "";
            String queryproduct = "select id,name from product where company=? ";
            PreparedStatement stmtp = conn.prepareStatement(queryproduct);  //select all product from company
            stmtp.setObject(1, companyId);
            ResultSet rsp = stmtp.executeQuery();
            while (rsp.next()) {
                productid = rsp.getString("id");
                productname = rsp.getString("name");
                if (!StringUtil.isNullOrEmpty(productid)) {
                    //set location,warehouse,Batch option for product
                    String queryForupdate = "update product set islocationforproduct='T',iswarehouseforproduct='T',isBatchForProduct='T',location=?,warehouse=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, InvLocId);
                    stmtforUpdate.setString(2, InvWarId);
                    stmtforUpdate.setString(3, productid);
                    stmtforUpdate.setString(4, companyId);
                    stmtforUpdate.executeUpdate();
                }


                if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid)) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse
                    String batchMapId = java.util.UUID.randomUUID().toString();
                    String querybatch = "insert into newproductbatch (id,quantity,quantitydue,batchname,location,warehouse,transactiontype,product,isopening,ispurchase,company) values (?,?,?,'Default Batch',?,?,28,?,'T','T',?)";
                    PreparedStatement stmtb = conn.prepareStatement(querybatch);
                    stmtb.setString(1, batchMapId);
                    stmtb.setDouble(2, 1000);
                    stmtb.setDouble(3, 1000);
                    stmtb.setString(4, InvLocId); //location
                    stmtb.setString(5, InvWarId);  //warehouse
                    stmtb.setString(6, productid);
                    stmtb.setString(7, companyId);
                    stmtb.executeUpdate();
                    stmtb.close();

                    String locBatchdocId = java.util.UUID.randomUUID().toString();
                    String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                    PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                    stmtlbmap.setString(1, locBatchdocId);
                    stmtlbmap.setDouble(2, 1000);
                    stmtlbmap.setString(3, productid);
                    stmtlbmap.setString(4, batchMapId);
                    stmtlbmap.executeUpdate();
                    stmtlbmap.close();

                }
                totalproductUpdationCnt++;
                System.out.println("Count is" + totalproductUpdationCnt);
            } //iterate for all product

            String dodetailId = "";
            String doproduct = "";
            double deliveredQty = 0.0;
            String querydo = "select id,baseuomdeliveredquantity,product from dodetails where company=? ";
            PreparedStatement stmtdo = conn.prepareStatement(querydo);  //select all product from company
            stmtdo.setObject(1, companyId);
            ResultSet rsdo = stmtdo.executeQuery();
            while (rsdo.next()) {
                dodetailId = rsdo.getString("id");
                deliveredQty = rsdo.getDouble("baseuomdeliveredquantity");
                doproduct = rsdo.getString("product");
                String batchId = "";
                double quantitydue=0.0;
                if (!StringUtil.isNullOrEmpty(doproduct)) {
                    String queryForbatch = "select id,quantitydue from newproductbatch where product=? and company=? ";
                    PreparedStatement stmtbatch = conn.prepareStatement(queryForbatch);
                    stmtbatch.setObject(1, doproduct);
                    stmtbatch.setObject(2, companyId);
                    ResultSet rs6 = stmtbatch.executeQuery();
                    if (rs6.next()) {
                        batchId = rs6.getString("id");
                        quantitydue = rs6.getDouble("quantitydue");
                    }
                }


                if (!StringUtil.isNullOrEmpty(InvLocId) && !StringUtil.isNullOrEmpty(InvWarId) && !StringUtil.isNullOrEmpty(productid)) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse
                    String queryForupdate = "update newproductbatch set quantitydue=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setDouble(1,quantitydue-deliveredQty);
                    stmtforUpdate.setString(2, batchId);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();

                    String locBatchdocId = java.util.UUID.randomUUID().toString();
                    String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,27)";
                    PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                    stmtlbmap.setString(1, locBatchdocId);
                    stmtlbmap.setDouble(2, deliveredQty);
                    stmtlbmap.setString(3, dodetailId);
                    stmtlbmap.setString(4, batchId);
                    stmtlbmap.execute();
                    stmtlbmap.close();

                }
                totaldoUpdationCnt++;
                System.out.println("Count is" + totaldoUpdationCnt);
            } //iterate for al
            totalCompanyUpdationCnt++;
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
        out.println("<br><br> Total deliveryorder details updated are " + totaldoUpdationCnt);
        out.println("<br><br> Total products updated are " + totalproductUpdationCnt);
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>