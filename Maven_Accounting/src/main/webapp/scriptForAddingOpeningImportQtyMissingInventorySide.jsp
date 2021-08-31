<%@page import="com.krawler.hql.accounting.Producttype"%>
<%@page import="com.krawler.spring.common.KwlReturnObject"%>
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

        /*
         NOTE : This script has been written  for opening quantity related issues.
                This script empties stock and stockmovement table and make new entries according to ERP stock.
                Here no transaction is made so we can empty table.But,if product is used in any Inventory side transactions then make code for checking that too.
                This script is tried to make generic.But according to requirements,make necessary changes.
        */
        
        String serverip = request.getParameter("serverip");//"192.168.0.208";                            
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"newstaging";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
//        String subdomain = "fasten";
        String[] subdomains = new String[]{"cftp", "edno", "cfdn", "fved"};

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        conn.setAutoCommit(false);
        
        int totalCompanyUpdationCnt = 0;
        int totalproductUpdationCnt = 0;

        for (String subdomain : subdomains) {

            String query = "select companyid,companyname FROM company where subdomain= ? ";

            PreparedStatement stmt = conn.prepareStatement(query);
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                stmt.setString(1, subdomain);
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String companyId = rs.getString("companyid");
                String InvWarId = null; // Default Store id
                String InvLocId = null; // Default Location id

                if (!StringUtil.isNullOrEmpty(companyId)) {
                    //set location,warehouse option for product
                    String queryForupdate = "update compaccpreferences set islocationcompulsory='T',iswarehousecompulsory='T' where id=?  ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, companyId);
                    stmtforUpdate.executeUpdate();
                }

                String del1 = " DELETE  FROM inventorywarehouse WHERE ID NOT IN(select id from in_storemaster WHERE company=?) AND company=? ";
                String del2 = " DELETE  FROM inventorylocation WHERE ID NOT IN(select id from in_location WHERE company=?) AND company=? ";

                String del4 = " DELETE  FROM in_sm_detail WHERE stockmovement IN(SELECT id from in_stockmovement sm WHERE sm.company=? ) ";
                String del5 = " DELETE  FROM in_stockmovement WHERE company=? ";
                String del3 = " DELETE  FROM in_stock WHERE company=? ";

                PreparedStatement smdDel = conn.prepareStatement(del1);
                smdDel.setString(1, companyId);
                smdDel.setString(2, companyId);
//                smdDel.executeUpdate();
                smdDel.close();

                smdDel = conn.prepareStatement(del2);
                smdDel.setString(1, companyId);
                smdDel.setString(2, companyId);
//                smdDel.executeUpdate();
                smdDel.close();

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

                // fetch default warehouse
                String qryForDefaultStore = "SELECT id from in_storemaster WHERE company =? AND isdefault=1 AND isactive=1 ";
                PreparedStatement psst = conn.prepareStatement(qryForDefaultStore);  //select all product from company
                psst.setObject(1, companyId);
                ResultSet prsp = psst.executeQuery();
                if (prsp.next()) {
                    InvWarId = prsp.getString("id");
                }

                // fetch default location
                String qryForDefaultLocation = "SELECT id from in_location WHERE company =? AND isdefault=1 AND isactive=1";
                psst = conn.prepareStatement(qryForDefaultLocation);  //select all product from company
                psst.setObject(1, companyId);
                prsp = psst.executeQuery();
                if (prsp.next()) {
                    InvLocId = prsp.getString("id");
                }
//                out.println(InvWarId+" "+ InvLocId+" "+subdomain);
                if(StringUtil.isNullOrEmpty(InvWarId) || StringUtil.isNullOrEmpty(InvLocId)){
                    out.println("<br/>Defalut warehouse or Default Location is not found for subdomain - <b>"+subdomain+"</b> .So Stock is not added on Inventory side for this company.");
                    continue;
                }
                
                String queryproduct = "select id,name,unitOfMeasure,currency from product where company=? AND producttype IN(?,?)"; //product type = assembly,inventory part
                PreparedStatement stmtp = conn.prepareStatement(queryproduct);  //select all product from company
                stmtp.setObject(1, companyId);
                stmtp.setObject(2, Producttype.INVENTORY_PART);
                stmtp.setObject(3, Producttype.ASSEMBLY);
                ResultSet rsp = stmtp.executeQuery();
                while (rsp.next()) {
                    boolean anyProductRecordUpdated = false;
                    String productid = "", productname = "", productUoM = "";
                    double productCurrency = 0;
                    double productInitPurchasePrice = 0;

                    productid = rsp.getString("id");
                    productname = rsp.getString("name");
                    productUoM = rsp.getString("unitOfMeasure");
                    productCurrency = rsp.getDouble("currency");

                    String getProducInitPurchasePriceQry = " select pl1.price as initpurchaseprice from pricelist pl1 where pl1.affecteduser='-1' "
                            + " and pl1.product=? and pl1.carryin='T' and pl1.currency= ? and"
                            + " (pl1.applydate in (select min(pl2.applydate) from pricelist pl2 "
                            + " where pl2.affecteduser='-1' and pl2.product=pl1.product and pl2.currency=? and pl2.carryin=pl1.carryin group by pl2.product)) ";

                    PreparedStatement ppsmt = conn.prepareStatement(getProducInitPurchasePriceQry);
                    ppsmt.setObject(1, productid);
                    ppsmt.setObject(2, productCurrency);
                    ppsmt.setObject(3, productCurrency);
                    ResultSet pprs = ppsmt.executeQuery();
                    if (pprs.next()) {
                        productInitPurchasePrice = pprs.getDouble("initpurchaseprice");
                    }

                    // initial purchase price get 
                    //KwlReturnObject initPurchasePriceObj = accProductObj.getInitialPrice(product.getID(), true);
                    String stockInsertQry = "select (case when carryin='T' then baseuomquantity else -baseuomquantity end) as qty,updatedate,b.warehouse,b.location "
                            + " FROM inventory  i LEFT JOIN product  p ON  p.id=i.product  "
                            + " LEFT JOIN  newproductbatch b  ON (i.product=b.product  AND i.baseuomquantity=b.quantitydue) "
                            + " where i.deleteflag='F' and p.producttype <>'4efb0286-5627-102d-8de6-001cc0794cfa'  AND  i.company=? and i.product=? AND baseuomquantity >0 ";

                    PreparedStatement stmtstk = conn.prepareStatement(stockInsertQry);
                    stmtstk.setObject(1, companyId);
                    stmtstk.setObject(2, productid);
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
                                + " VALUES (UUID(), ?,?,?,?,?,?,?,?, NOW()) ";
                        PreparedStatement stmtquery2 = conn.prepareStatement(query2);
                        stmtquery2.setString(1, productid);
                        stmtquery2.setString(2, InvWarId);
                        stmtquery2.setString(3, InvLocId);
                        stmtquery2.setString(4, "");
                        stmtquery2.setString(5, null);
                        stmtquery2.setString(6, companyId);
                        stmtquery2.setDouble(7, Invbaseuomquantity);
                        stmtquery2.setDate(8,  rsstk.getDate("updatedate"));
                        stmtquery2.execute();
                        stmtquery2.close();

                        //Entry of Stock movement and Stock movement Detail on Inventory side if Qty > 0
                        if (Invbaseuomquantity > 0) {

                            String smId = java.util.UUID.randomUUID().toString();
                            String smDetailId = java.util.UUID.randomUUID().toString();

                            String stockMovementQry = "INSERT INTO in_stockmovement (id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                            PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                            stmt1.setString(1, smId);
                            stmt1.setString(2, companyId);
                            stmt1.setString(3, productid); //product
                            stmt1.setString(4, InvWarId);
                            stmt1.setString(5, InvLocId);
                            stmt1.setString(6, productUoM);//stockuom
                            stmt1.setDouble(7, Invbaseuomquantity); //qty
                            stmt1.setDouble(8, productInitPurchasePrice); //priceperunit  
                            stmt1.setDate(9, rsstk.getDate("updatedate")); //createdon
                            stmt1.setString(10, null); // transaction no
                            stmt1.setDate(11, rsstk.getDate("updatedate")); // transaction date
                            stmt1.setInt(12, 1); // stock In : 1 .Stock out : 2
                            stmt1.setInt(13, 10); // transaction module // do- 6,grn-5 ,10 -opening/new product
                            stmt1.setString(14, productid); // modulerefid (parent) 
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
                            
                            totalproductUpdationCnt++;
                        }
                        
                    }
                    stmtstk.close();

                }

                totalCompanyUpdationCnt++;
            }
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
        out.println("<br><br> Total products updated are " + totalproductUpdationCnt);
        out.println("<br><br> Time :  " + new java.util.Date().toString());
        conn.commit();
    } catch (Exception e) {
        if (conn != null) {
             conn.rollback();
        }
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

%>