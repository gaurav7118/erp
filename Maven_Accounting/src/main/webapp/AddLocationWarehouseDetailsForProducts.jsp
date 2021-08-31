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
        String list = "";
        while (rs.next()) {
            String companyId = rs.getString("companyid");

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
                stmtInv.execute();
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
                stmtWar.execute();
                stmtWar.close();
            }

            String productid = "", productname = "";
            String queryproduct = "select id,name from product where islocationforproduct='T' and iswarehouseforproduct='T' and company=? ";
//            String queryproduct = "select id,name from product where company=? and id='ff8080814c6a4c1a014c6deb95e20983'";
            PreparedStatement stmtp = conn.prepareStatement(queryproduct);  //select all product from company
            stmtp.setObject(1, companyId);
            ResultSet rsp = stmtp.executeQuery();
            while (rsp.next()) {
                productid = rsp.getString("id");
                productname = rsp.getString("name");
                if (!StringUtil.isNullOrEmpty(productid)) {
                    //set location option for product
                    String queryForupdate = "update product set location=?,warehouse=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, InvLocId);
                    stmtforUpdate.setString(2, InvWarId);
                    stmtforUpdate.setString(3, productid);
                    stmtforUpdate.setString(4, companyId);
                    stmtforUpdate.executeUpdate();
                }

                String InvId = "";
                double Invbaseuomquantity = 0.0, Invquantity = 0.0;
                String invquery = "select id,quantity,baseuomquantity from inventory where company=? and product= ? and newinv='T' ";
                PreparedStatement stmtinv = conn.prepareStatement(invquery);
                stmtinv.setObject(1, companyId);
                stmtinv.setObject(2, productid);
                ResultSet rsinv = stmtinv.executeQuery();
                if (rsinv.next()) {
                    InvId = rsinv.getString("id");
                    Invquantity = rsinv.getDouble("quantity");
                    Invbaseuomquantity = rsinv.getDouble("baseuomquantity");
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
                }

                // In case of GR generated
                String invqueryforgr = "select i.id,i.quantity,i.baseuomquantity from inventory i INNER JOIN grodetails g ON i.id=g.id where i.company=? and i.product= ? and i.newinv='F' and i.carryin='T' ";
                PreparedStatement stmtinvgr = conn.prepareStatement(invqueryforgr);
                stmtinvgr.setObject(1, companyId);
                stmtinvgr.setObject(2, productid);
                ResultSet rsinvgr = stmtinvgr.executeQuery();
                while (rsinvgr.next()) {
                    InvId = rsinvgr.getString("id");
                    Invquantity = rsinvgr.getDouble("quantity");
                    Invbaseuomquantity = rsinvgr.getDouble("baseuomquantity");
//                }
                    if (!StringUtil.isNullOrEmpty(InvId)) { //check if GR is done for this product ifthere then move that quantity in default location and warehouse
                        if (Invbaseuomquantity > 0) {
                            String batchMapId = java.util.UUID.randomUUID().toString();
                            String querybatch = "insert into newproductbatch (id,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company) values (?,?,?,?,?,28,?,'F','T',?)";
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
                            stmtlbmap.setString(3, InvId);
                            stmtlbmap.setString(4, batchMapId);
                            stmtlbmap.execute();
                            stmtlbmap.close();

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
                            totalCompanyUpdationCnt++;
                        }
                    }

                }

//                Inventory stock adjudtment
                String invqueryforstock = "select isa.id,isa.store,isd.location,isa.product,isa.quantity,isa.finalquantity from in_stockadjustment isa inner join in_sa_detail isd on isa.id=isd.stockadjustment where isa.company=? and isa.product=? ";
                PreparedStatement stmtinvstock = conn.prepareStatement(invqueryforstock);
                stmtinvstock.setObject(1, companyId);
                stmtinvstock.setObject(2, productid);
                ResultSet rsinvstock = stmtinvstock.executeQuery();
                while (rsinvstock.next()) {
                    InvId = rsinvstock.getString("id");
                    Invquantity = rsinvstock.getDouble("quantity");
                    Invbaseuomquantity = rsinvstock.getDouble("finalquantity");
                    String invloc = rsinvstock.getString("location");
                    String invstore = rsinvstock.getString("store");
//                }
                    if (!StringUtil.isNullOrEmpty(InvId)) { //check if GR is done for this product ifthere then move that quantity in default location and warehouse
                        if (Invbaseuomquantity > 0) {
                            String batchMapId = java.util.UUID.randomUUID().toString();
                            String querybatch = "insert into newproductbatch (id,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company) values (?,?,?,?,?,28,?,'F','T',?)";
                            PreparedStatement stmtb = conn.prepareStatement(querybatch);
                            stmtb.setString(1, batchMapId);
                            stmtb.setDouble(2, Invbaseuomquantity);
                            stmtb.setDouble(3, Invbaseuomquantity);
                            stmtb.setString(4, invloc); //location
                            stmtb.setString(5, invstore);  //warehouse
                            stmtb.setString(6, productid);
                            stmtb.setString(7, companyId);
                            stmtb.execute();
                            stmtb.close();

                            String query2 = "INSERT INTO in_stock(id, product, store, location, batchname, serialnames, company, quantity, createdon, modifiedon) "
                                    + " VALUES (UUID(), ?,?,?,?,?,?,?, NOW(), NOW()) ";
                            PreparedStatement stmtquery2 = conn.prepareStatement(query2);
                            stmtquery2.setString(1, productid);
                            stmtquery2.setString(2, invstore);
                            stmtquery2.setString(3, invloc);
                            stmtquery2.setString(4, "");
                            stmtquery2.setString(5, null);
                            stmtquery2.setString(6, companyId);
                            stmtquery2.setDouble(7, Invbaseuomquantity);
                            stmtquery2.execute();
                            stmtquery2.close();
                            totalCompanyUpdationCnt++;
                        }
                    }
                }

                list += productname;
                totalproductUpdationCnt++;
                System.out.println("Count is" + totalproductUpdationCnt + "List for product" + list);
            } //iterate for all product
//            totalCompanyUpdationCnt++;
        }
        out.println("<br><br> Total insert records are " + totalCompanyUpdationCnt);
        out.println("<br><br> Total products updated are " + totalproductUpdationCnt);
        out.println("<br><br> Total products updated are " + list);
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>