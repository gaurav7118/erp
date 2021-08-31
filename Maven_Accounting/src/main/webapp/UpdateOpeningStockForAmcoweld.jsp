<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
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
        String filepath = request.getParameter("filepath"); //"krawler"
        String driver = "com.mysql.jdbc.Driver";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
        FileInputStream Productinfo = new FileInputStream(filepath);
        BufferedReader in = new BufferedReader(new InputStreamReader(Productinfo));
//        DataInputStream in = new DataInputStream(invoice);
        int cnt = 0;
        String record = "", currencyid = "";
        double qtyToUpdate = 0;

        String companyId = "1621b1fd-2e83-4ade-abf2-9e01aa4b706f";    //companyid for amcoweldnew
        String bbfrom = "";
        String queryforCurrency = "select currency from company where companyid=? ";
        PreparedStatement stmtcurrency = conn.prepareStatement(queryforCurrency);
        stmtcurrency.setObject(1, companyId);
        ResultSet rsForcurrency = stmtcurrency.executeQuery();
        if (rsForcurrency.next()) {
            currencyid = rsForcurrency.getString("currency");
        }
        String query = "select bbfrom from compaccpreferences where id =?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setObject(1, companyId);
        ResultSet rsb = stmt.executeQuery();
        if (rsb.next()) {
            bbfrom = rsb.getString("bbfrom");
        }



        String storeId = "4028e4d35217486b01521aa93b84066f";    //id for E1 store
        String locationid = "a2918c6d-a40f-4d18-9a9d-dbaf1aa2540d";    //id for default location
        
        while ((record = in.readLine()) != null) {
            String productId = "", uomid = "", uomName = "";
            double currency = 0, price = 0;
            if (record != "") {
                String[] recarrName = record.split(",");
                String productCode = recarrName[0].trim();
                String openingQtyString = "";
                if (recarrName.length > 1 && !StringUtil.isNullOrEmpty(recarrName[1].trim())) {
                    openingQtyString = recarrName[1].trim();
                } else {
                    out.println("<br> Product Opening Quantity is null for product " + productCode);
                    continue;
                }

                String filePrice = "";
                if (recarrName.length > 3 && !StringUtil.isNullOrEmpty(recarrName[3].trim())) {
                    filePrice = recarrName[3].trim();
                } else {
                    filePrice = "0";
                }
                if ("PID".equals(productCode) && "IQ".equals(openingQtyString)) {
                    continue;
                }
                price = Double.parseDouble(filePrice);
                double openingQty = Double.parseDouble(openingQtyString);
                String fileUom = "";
                if (recarrName.length > 2 && !StringUtil.isNullOrEmpty(recarrName[2].trim())) {
                    fileUom = recarrName[2].trim();
                } else {
                    fileUom = "";
                }

                //getting First location Id  ENS       
                String queryForProductId = "select p.id,p.unitOfMeasure,u.name from product p inner join uom u on (u.id=p.unitOfMeasure) where p.company=? and p.productid =?";
                PreparedStatement stmt3 = conn.prepareStatement(queryForProductId);
                stmt3.setObject(1, companyId);
                stmt3.setObject(2, productCode);
                ResultSet rs2 = stmt3.executeQuery();
                if (rs2.next()) {
                    productId = rs2.getString("id");

                    uomid = rs2.getString("unitOfMeasure");
                    uomName = rs2.getString("name");

                } else {
                    out.println("<br> Product not fount for record row " + productCode);
                    continue;
                }
                /*
                 * if (!StringUtil.isNullOrEmpty(fileUom) &&
                 * fileUom.equalsIgnoreCase(uomName)) { out.println("<br> valid
                 * Stock UOM for Product UOM " + uomName + " same as file UOM "
                 * + fileUom + " for product" + productCode); }
                 *
                 */
                if (StringUtil.isNullOrEmpty(fileUom)) {
                    out.println("<br> Product  UOM " + uomName + " Not Given in file So We Assume IQ in Stock UOM");
                    
                } else if (!fileUom.equalsIgnoreCase(uomName)) {
                    out.println("<br> Product  UOM " + uomName + " Not same as  file UOM " + fileUom + " for product" + productCode);
                    continue;
                }


                if (price == 0) {          //IP price not given in file then take from minmum applydate price from system
                    String getProducInitPurchasePriceQry = " select pl1.price as initpurchaseprice from pricelist pl1 where pl1.affecteduser='-1' "
                            + " and pl1.product=? and pl1.carryin='T' and pl1.currency= ? and"
                            + " (pl1.applydate in (select min(pl2.applydate) from pricelist pl2 "
                            + " where pl2.affecteduser='-1' and pl2.product=pl1.product and pl2.currency=? and pl2.carryin=pl1.carryin group by pl2.product)) ";

                    PreparedStatement ppsmt = conn.prepareStatement(getProducInitPurchasePriceQry);
                    ppsmt.setObject(1, productId);
                    ppsmt.setObject(2, currency);
                    ppsmt.setObject(3, currency);
                    ResultSet pprs = ppsmt.executeQuery();
                    if (pprs.next()) {
                        price = pprs.getDouble("initpurchaseprice");
                    }
                }
                String InvId = "";
                double Invbaseuomquantity = 0.0, Invquantity = 0.0;
                String queryinvid = "select id,quantity,baseuomquantity from inventory where company=? and product= ? and newinv='T'";
                PreparedStatement stmt4 = conn.prepareStatement(queryinvid);
                stmt4.setObject(1, companyId);
                stmt4.setObject(2, productId);
                ResultSet rs4 = stmt4.executeQuery();
                if (rs4.next()) {
                    InvId = rs4.getString("id");
                    Invquantity = rs4.getDouble("quantity");
                    Invbaseuomquantity = rs4.getDouble("baseuomquantity");
                }
                
                 /*if (!StringUtil.isNullOrEmpty(InvId)) {

                    if (Invbaseuomquantity == 0) {
                        out.println("<br> Product  oprning quantity is 0 in table and " + openingQty + " in file for product" + productCode);
                        continue;
                    } else if (Invbaseuomquantity > openingQty) {
                        out.println("<br> Product  oprning quantity is greater than given initial quantity for product" + productCode + " and difrrence is" + (Invbaseuomquantity - openingQty));
                        continue;
                    } else if (Invbaseuomquantity == openingQty) {
                        out.println("<br> Product  oprning quantity is same with given initial quantity for product" + productCode + " and difrrence is" + (Invbaseuomquantity - openingQty));
                        continue;
                    } else {
                        out.println("<br> Product  oprning quantity is less than given initial quantity for product this is valid data" + productCode + " and difrrence is" + (Invbaseuomquantity - openingQty));
                    }

                } else {

                    out.println("<br> entry not found in inventory table for product" + productCode);
                    continue;
                }*/
                /*
                 * if (!StringUtil.isNullOrEmpty(InvId)) {
                 *
                 * if (Invbaseuomquantity == 0) { out.println("<br> Product
                 * oprning quantity is 0 in table and " + openingQty + " in file
                 * for product" + productCode); continue; } else if
                 * (Invbaseuomquantity > openingQty) { out.println("<br> Product
                 * oprning quantity is greater than given initial quantity for
                 * product" + productCode + " and difrrence is" +
                 * (Invbaseuomquantity - openingQty)); continue; } else if
                 * (Invbaseuomquantity == openingQty) { out.println("<br>
                 * Product oprning quantity is same with given initial quantity
                 * for product" + productCode + " and difrrence is" +
                 * (Invbaseuomquantity - openingQty)); continue; } else {
                 * out.println("<br> Product oprning quantity is less than given
                 * initial quantity for product this is valid data" +
                 * productCode + " and difrrence is" + (Invbaseuomquantity -
                 * openingQty)); continue; }
                 *
                 * } else {
                 *
                 * out.println("<br> entry not found in inventory table for
                 * product" + productCode); continue; }
                 */

                qtyToUpdate = openingQty - Invbaseuomquantity;
                
                if (!StringUtil.isNullOrEmpty(InvId)) {
                    String updatequery = "update inventory set quantity=?,baseuomquantity=?,updatedate=? where id=?";
                    PreparedStatement stmtquery = conn.prepareStatement(updatequery);
                    stmtquery.setDouble(1, openingQty);
                    stmtquery.setDouble(2, openingQty);
                    stmtquery.setString(3, bbfrom);
                    stmtquery.setString(4, InvId);
                    stmtquery.executeUpdate();
                    out.println("<br> Product updated with " + openingQty + " productcode= " + productCode);
                } else {
                    InvId = java.util.UUID.randomUUID().toString();
                    String queryInv = "insert into inventory(id,product,quantity,baseuomquantity,actquantity,baseuomrate,uom,description,carryin,defective,newinv,company,updatedate,deleteflag) values (?,?,?,?,?,?,?,'Inventory Opened','T','F','T',?,?,'F')";
                    PreparedStatement stmtInv = conn.prepareStatement(queryInv);
                    stmtInv.setString(1, InvId);
                    stmtInv.setString(2, productId);
                    stmtInv.setDouble(3, openingQty);
                    stmtInv.setDouble(4, openingQty);
                    stmtInv.setDouble(5, openingQty);
                    stmtInv.setDouble(6, 1);
                    stmtInv.setString(7, uomid);
                    stmtInv.setString(8, companyId);
                    stmtInv.setString(9, bbfrom);
                    stmtInv.execute();
                    stmtInv.close();
                }

                String querybatchid = "select id from newproductbatch where company=? and product= ?  and warehouse=? and location=? ";
                PreparedStatement stmtbatch = conn.prepareStatement(querybatchid);
                stmtbatch.setObject(1, companyId);
                stmtbatch.setObject(2, productId);
                stmtbatch.setObject(3, storeId);
                stmtbatch.setObject(4, locationid);
                ResultSet rsbatch = stmtbatch.executeQuery();
                String batchId = "";
                if (rsbatch.next()) {
                    batchId = rsbatch.getString("id");
                }
                if (!StringUtil.isNullOrEmpty(batchId)) {
                    String queryForupdate = "update newproductbatch set quantity=quantity+?,quantitydue=quantitydue+? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setDouble(1, qtyToUpdate);
                    stmtforUpdate.setDouble(2, qtyToUpdate);
                    stmtforUpdate.setString(3, batchId);
                    stmtforUpdate.setString(4, companyId);
                    stmtforUpdate.executeUpdate();

                    String queryForlocupdate = "update locationbatchdocumentmapping set quantity=quantity+? where  batchmapid=? and documentid=?";
                    PreparedStatement stmtforlocUpdate = conn.prepareStatement(queryForlocupdate);
                    stmtforlocUpdate.setDouble(1, qtyToUpdate);
                    stmtforlocUpdate.setString(2, batchId);
                    stmtforlocUpdate.setString(3, productId);
                    int updatecount = stmtforlocUpdate.executeUpdate();
                    if (updatecount == 0) {
                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                        PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                        stmtlbmap.setString(1, locBatchdocId);
                        stmtlbmap.setDouble(2, openingQty);
                        stmtlbmap.setString(3, productId);
                        stmtlbmap.setString(4, batchId);
                        stmtlbmap.execute();
                        stmtlbmap.close();
                    }
                } else {

                    String batchMapId = java.util.UUID.randomUUID().toString();
                    String querybatch = "insert into newproductbatch (id,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company) values (?,?,?,?,?,28,?,'T','T',?)";
                    PreparedStatement stmtb = conn.prepareStatement(querybatch);
                    stmtb.setString(1, batchMapId);
                    stmtb.setDouble(2, openingQty);
                    stmtb.setDouble(3, openingQty);
                    stmtb.setString(4, locationid); //location
                    stmtb.setString(5, storeId);  //warehouse
                    stmtb.setString(6, productId);
                    stmtb.setString(7, companyId);
                    stmtb.execute();
                    stmtb.close();

                    String locBatchdocId = java.util.UUID.randomUUID().toString();
                    String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                    PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                    stmtlbmap.setString(1, locBatchdocId);
                    stmtlbmap.setDouble(2, openingQty);
                    stmtlbmap.setString(3, productId);
                    stmtlbmap.setString(4, batchMapId);
                    stmtlbmap.execute();
                    stmtlbmap.close();

                }

                String queryStockid = "select id from in_stock where company=? and product= ?  and store=? and location=? ";
                PreparedStatement stmtstock = conn.prepareStatement(queryStockid);
                stmtstock.setObject(1, companyId);
                stmtstock.setObject(2, productId);
                stmtstock.setObject(3, storeId);
                stmtstock.setObject(4, locationid);
                ResultSet rsstock = stmtstock.executeQuery();
                String stockId = "";
                if (rsstock.next()) {
                    stockId = rsstock.getString("id");
                    String queryForupdate = "update in_stock set quantity=quantity+? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setDouble(1, qtyToUpdate);
                    stmtforUpdate.setString(2, stockId);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                } else {
                    String queryforInsrt = "INSERT INTO in_stock(id, product, store, location, batchname, serialnames, company, quantity, createdon, modifiedon) "
                            + " VALUES (UUID(), ?,?,?,?,?,?,?,?,?) ";
                    PreparedStatement stmtquery2 = conn.prepareStatement(queryforInsrt);
                    stmtquery2.setString(1, productId);
                    stmtquery2.setString(2, storeId);
                    stmtquery2.setString(3, locationid);
                    stmtquery2.setString(4, "");
                    stmtquery2.setString(5, null);
                    stmtquery2.setString(6, companyId);
                    stmtquery2.setDouble(7, openingQty);
                    stmtquery2.setString(8, bbfrom);
                    stmtquery2.setString(9, bbfrom);
                    stmtquery2.execute();
                    stmtquery2.close();
                }
                String querySMid = "select id from in_stockmovement where company=? and store=? and transaction_type=0 and transaction_module=10 and modulerefid=?";
                PreparedStatement stmtsm = conn.prepareStatement(querySMid);
                stmtsm.setObject(1, companyId);
                stmtsm.setObject(2, storeId);
                stmtsm.setObject(3, productId);

                ResultSet rssm = stmtsm.executeQuery();
                String stmId = "";
                if (rssm.next()) {
                    stmId = rssm.getString("id");
                    String queryForupdate = "update in_stockmovement set quantity=quantity+?,priceperunit=?,createdon=?,transaction_date=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setDouble(1, qtyToUpdate);
                    stmtforUpdate.setDouble(2, price);
                    stmtforUpdate.setString(3, bbfrom);
                    stmtforUpdate.setString(4, bbfrom);
                    stmtforUpdate.setString(5, stmId);
                    stmtforUpdate.setString(6, companyId);
                    stmtforUpdate.executeUpdate();
                    String queryForSMDupdate = "update in_sm_detail set quantity=quantity+? where stockmovement=?";
                    PreparedStatement stmtforSMDUpdate = conn.prepareStatement(queryForSMDupdate);
                    stmtforSMDUpdate.setDouble(1, qtyToUpdate);
                    stmtforSMDUpdate.setString(2, stmId);
                    stmtforSMDUpdate.executeUpdate();
                } else {
                    String smId = java.util.UUID.randomUUID().toString();
                    String smDetailId = java.util.UUID.randomUUID().toString();

                    String stockMovementQry = "INSERT INTO in_stockmovement(id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    PreparedStatement stmt5 = conn.prepareStatement(stockMovementQry);
                    stmt5.setString(1, smId);
                    stmt5.setString(2, companyId);
                    stmt5.setString(3, productId); //product 
                    stmt5.setString(4, storeId);
                    stmt5.setString(5, null); // null becoz as per new code it is present in sm detail 
                    stmt5.setString(6, uomid);//stockuom 
                    stmt5.setDouble(7, openingQty); //qty 
                    stmt5.setDouble(8, price); //initial purchase price 
                    stmt5.setObject(9, bbfrom); //createddon 
                    stmt5.setString(10, productCode); // transaction no
                    stmt5.setObject(11, bbfrom); // transaction date 
                    stmt5.setInt(12, 0); // opening stock :0 , stock In : 1 .Stock out : 2 
                    stmt5.setInt(13, 10); // transaction module // do- 6,grn-5 ,10 -opening/new product 
                    stmt5.setString(14, productId); // modulerefid (parent) 
                    stmt5.setString(15, "Stock added through OPENING");
                    stmt5.execute();
                    stmt5.close();

                    String smDetailQry = "INSERT INTO in_sm_detail (id,stockmovement,location,batchname,serialnames,quantity) VALUES(?,?,?,?,?,?)";
                    PreparedStatement smd = conn.prepareStatement(smDetailQry);
                    smd.setString(1, smDetailId);
                    smd.setString(2, smId);
                    smd.setString(3, locationid);
                    smd.setString(4, "");
                    smd.setString(5, null);
                    smd.setDouble(6, openingQty);
                    smd.execute();
                    smd.close();
                }
                if (price != 0) {
                    String queryIPP = "select id from pricelist where company=? and product= ?  and applydate=? and carryin='T' and affecteduser=-1 and currency=?";
                    PreparedStatement stmtip = conn.prepareStatement(queryIPP);
                    stmtip.setObject(1, companyId);
                    stmtip.setObject(2, productId);
                    stmtip.setObject(3, bbfrom);
                    stmtip.setObject(4, currencyid);

                    ResultSet rsip = stmtip.executeQuery();
                    if (rsip.next()) {
                        String ipid = rsstock.getString("id");
                        String queryForupdate = "update pricelist set price=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, price);
                        stmtforUpdate.setString(2, ipid);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    } else {
                        if (!StringUtil.isNullOrEmpty(productId) && !StringUtil.isNullOrEmpty(currencyid)) {
                            String priceId = java.util.UUID.randomUUID().toString();
                            String queryInv = "insert into pricelist(id,product,carryin,price,applydate,affecteduser,currency,company) values (UUID(),?,'T',?,?,'-1',?,?)";
                            PreparedStatement stmtprice = conn.prepareStatement(queryInv);
                            //stmtprice.setString(1, priceId);
                            stmtprice.setString(1, productId);
                            stmtprice.setDouble(2, price);
                            stmtprice.setString(3, bbfrom);
                            stmtprice.setString(4, currencyid);
                            stmtprice.setString(5, companyId);
                            stmtprice.execute();
                            stmtprice.close();
                        }
                    }
                }


                cnt++;
            }
        }

        int count = cnt;
        StringBuilder result = new StringBuilder("" + count).append(" Records added successfully.");
        out.println(result);
            
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>
