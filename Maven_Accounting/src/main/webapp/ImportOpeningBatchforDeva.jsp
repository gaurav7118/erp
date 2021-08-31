<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.util.Date"%>
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

        String companyId = "e53aa586-789b-4e85-af41-2480bbea5650";    //companyid for Deva
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

        String sameProduct = "";
        while ((record = in.readLine()) != null) {
            String productId = "", uomid = "";
            double currency = 0, price = 0;
            if (record != "") {
                String[] recarrName = record.split(",");
                String productCode = recarrName[0].trim();
                String serialName = recarrName[1].trim();


                if ("PID".equals(productCode) && "Series".equals(serialName)) {
                    continue;
                }
                String Exp = recarrName[3].trim();
                
                DateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
                java.util.Date eDate = (java.util.Date)formatter.parse(Exp);
                java.sql.Date expDate = new java.sql.Date(eDate.getTime());
     
                //String Expdate = formatter.format(da);
                double qty = Double.parseDouble(recarrName[4].trim());
                String warehouse = recarrName[6].trim();
                String location = recarrName[5].trim();
                //  price =  Double.parseDouble(recarrName[4].trim());   
                //double openingQty = Double.parseDouble(openingQtyString);

                //getting First location Id  ENS   
                String storeId = "", locationid = "";
                String query1 = "select id from in_storemaster where company =? and abbrev=?"; //default warehose 
                PreparedStatement stmt1 = conn.prepareStatement(query1);
                stmt1.setObject(1, companyId);
                stmt1.setObject(2, warehouse);
                ResultSet rs = stmt1.executeQuery();
                if (rs.next()) {
                    storeId = rs.getString("id");
                }else{
                    out.println("</br>invalid warehouse " + warehouse);
                    continue;
                }
                String query2 = "select id from in_location where company =? and name=?";  //default warehose 
                PreparedStatement stmt2 = conn.prepareStatement(query2);
                stmt2.setObject(1, companyId);
                stmt2.setObject(2, location);
                ResultSet rs1 = stmt2.executeQuery();
                if (rs1.next()) {
                    locationid = rs1.getString("id");
                }else{
                    out.println("</br>invalid Location " + location);
                    continue;
                }
                String queryForProductId = "select id,unitOfMeasure,currency from product where company=? and name =?";
                PreparedStatement stmt3 = conn.prepareStatement(queryForProductId);
                stmt3.setObject(1, companyId);
                stmt3.setObject(2, productCode);
                ResultSet rs2 = stmt3.executeQuery();
                //  if (!productCode.equals(sameProduct)) {
                cnt++;
                if (rs2.next()) {
                    productId = rs2.getString("id");
                    uomid = rs2.getString("unitOfMeasure");
                    sameProduct = productCode;
                    //out.println("</br>Product: " + productCode + " and iswarehouseforproduct: " + rs2.getString("iswarehouseforproduct") + " islocationforproduct: " + rs2.getString("islocationforproduct") + " isSerialForProduct: " + rs2.getString("isSerialForProduct") + " isBatchForProduct: " + rs2.getString("isBatchForProduct"));
                    //continue;
                } else {
                    out.println("</br>Product not fount for record row " + cnt + " and id is " + productCode);
                    continue;
                }

                //    }

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
                if (!StringUtil.isNullOrEmpty(InvId)) {
                    String updatequery = "update inventory set quantity=?,baseuomquantity=?,carryin='T',defective='F',newinv='T',updatedate=now(),deleteflag='F' where id=?";
                    PreparedStatement stmtquery = conn.prepareStatement(updatequery);
                    stmtquery.setDouble(1, Invquantity + qty);
                    stmtquery.setDouble(2, Invbaseuomquantity + qty);
                    stmtquery.setString(3, InvId);
                    stmtquery.executeUpdate();

                } else {
                    InvId = java.util.UUID.randomUUID().toString();
                    String queryInv = "insert into inventory(id,product,quantity,baseuomquantity,actquantity,baseuomrate,uom,description,carryin,defective,newinv,company,updatedate,deleteflag) values (?,?,?,?,?,?,?,'Inventory Opened','T','F','T',?,?,'F')";
                    PreparedStatement stmtInv = conn.prepareStatement(queryInv);
                    stmtInv.setString(1, InvId);
                    stmtInv.setString(2, productId);
                    stmtInv.setDouble(3, qty);
                    stmtInv.setDouble(4, qty);
                    stmtInv.setDouble(5, qty);
                    stmtInv.setDouble(6, 1);
                    stmtInv.setString(7, uomid);
                    stmtInv.setString(8, companyId);
                    stmtInv.setString(9, bbfrom);
                    stmtInv.execute();
                    stmtInv.close();
                }

                String querybatchid = "select id from newproductbatch where company=? and product= ?  and warehouse=? and location=? and batchname=?";
                PreparedStatement stmtbatch = conn.prepareStatement(querybatchid);
                stmtbatch.setObject(1, companyId);
                stmtbatch.setObject(2, productId);
                stmtbatch.setObject(3, storeId);
                stmtbatch.setObject(4, locationid);
                stmtbatch.setObject(5, serialName);
                ResultSet rsbatch = stmtbatch.executeQuery();
                String batchId = "";
                if (rsbatch.next()) {
                    batchId = rsbatch.getString("id");
                }
                if (!StringUtil.isNullOrEmpty(batchId)) {
                    String queryForupdate = "update newproductbatch set quantity=quantity+?,quantitydue=quantitydue+? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setDouble(1, qty);
                    stmtforUpdate.setDouble(2, qty);
                    stmtforUpdate.setString(3, batchId);
                    stmtforUpdate.setString(4, companyId);
                    stmtforUpdate.executeUpdate();

                    String queryForlocupdate = "update locationbatchdocumentmapping set quantity=quantity+? where  batchmapid=? and documentid=?";
                    PreparedStatement stmtforlocUpdate = conn.prepareStatement(queryForlocupdate);
                    stmtforlocUpdate.setDouble(1, qty);
                    stmtforlocUpdate.setString(2, batchId);
                    stmtforlocUpdate.setString(3, productId);
                    int updatecount = stmtforlocUpdate.executeUpdate();
                    if (updatecount == 0) {
                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                        PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                        stmtlbmap.setString(1, locBatchdocId);
                        stmtlbmap.setDouble(2, qty);
                        stmtlbmap.setString(3, productId);
                        stmtlbmap.setString(4, batchId);
                        stmtlbmap.execute();
                        stmtlbmap.close();
                    }
                } else {

                    batchId = java.util.UUID.randomUUID().toString();
                    String querybatch = "insert into newproductbatch (id,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company,expdate,batchname) values (?,?,?,?,?,28,?,'T','T',?,?,?)";
                    PreparedStatement stmtb = conn.prepareStatement(querybatch);
                    stmtb.setString(1, batchId);
                    stmtb.setDouble(2, qty);
                    stmtb.setDouble(3, qty);
                    stmtb.setString(4, locationid); //location
                    stmtb.setString(5, storeId);  //warehouse
                    stmtb.setString(6, productId);
                    stmtb.setString(7, companyId);
                    stmtb.setDate(8, expDate);
                    stmtb.setString(9, serialName);
                    stmtb.execute();
                    stmtb.close();

                    String locBatchdocId = java.util.UUID.randomUUID().toString();
                    String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                    PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                    stmtlbmap.setString(1, locBatchdocId);
                    stmtlbmap.setDouble(2, qty);
                    stmtlbmap.setString(3, productId);
                    stmtlbmap.setString(4, batchId);
                    stmtlbmap.execute();
                    stmtlbmap.close();

                }

                

                /*
                 * String queryStockid = "select id from in_stock where
                 * company=? and product= ? and store=? and location=? ";
                 * PreparedStatement stmtstock =
                 * conn.prepareStatement(queryStockid); stmtstock.setObject(1,
                 * companyId); stmtstock.setObject(2, productId);
                 * stmtstock.setObject(3, storeId); stmtstock.setObject(4,
                 * locationid); ResultSet rsstock = stmtstock.executeQuery();
                 * String stockId = ""; if (rsstock.next()) { stockId =
                 * rsstock.getString("id"); String queryForupdate = "update
                 * in_stock set quantity=quantity+? where id=? and company=? ";
                 * PreparedStatement stmtforUpdate =
                 * conn.prepareStatement(queryForupdate);
                 * stmtforUpdate.setDouble(1, openingQty);
                 * stmtforUpdate.setString(2, stockId);
                 * stmtforUpdate.setString(3, companyId);
                 * stmtforUpdate.executeUpdate(); } else { String queryforInsrt
                 * = "INSERT INTO in_stock(id, product, store, location,
                 * batchname, serialnames, company, quantity, createdon,
                 * modifiedon) " + " VALUES (UUID(), ?,?,?,?,?,?,?,?,?) ";
                 * PreparedStatement stmtquery2 =
                 * conn.prepareStatement(queryforInsrt); stmtquery2.setString(1,
                 * productId); stmtquery2.setString(2, storeId);
                 * stmtquery2.setString(3, locationid); stmtquery2.setString(4,
                 * ""); stmtquery2.setString(5, null); stmtquery2.setString(6,
                 * companyId); stmtquery2.setDouble(7, openingQty);
                 * stmtquery2.setString(8, bbfrom); stmtquery2.setString(9,
                 * bbfrom); stmtquery2.execute(); stmtquery2.close(); }
                 *
                 * String smId = java.util.UUID.randomUUID().toString(); String
                 * smDetailId = java.util.UUID.randomUUID().toString();
                 *
                 * String stockMovementQry = "INSERT INTO
                 * in_stockmovement(id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer)
                 * values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                 * PreparedStatement stmt5 =
                 * conn.prepareStatement(stockMovementQry); stmt5.setString(1,
                 * smId); stmt5.setString(2, companyId); stmt5.setString(3,
                 * productId); //product stmt5.setString(4, storeId);
                 * stmt5.setString(5, null); // null becoz as per new code it is
                 * present in sm detail stmt5.setString(6, uomid);//stockuom
                 * stmt5.setDouble(7, openingQty); //qty stmt5.setDouble(8,
                 * price); //initial purchase price stmt5.setObject(9, bbfrom);
                 * //createddon stmt5.setString(10, productCode); // transaction
                 * no stmt5.setObject(11, bbfrom); // transaction date
                 * stmt5.setInt(12, 0); // opening stock :0 , stock In : 1
                 * .Stock out : 2 stmt5.setInt(13, 10); // transaction module //
                 * do- 6,grn-5 ,10 -opening/new product stmt5.setString(14,
                 * productId); // modulerefid (parent) stmt5.setString(15,
                 * "Stock added through OPENING"); stmt5.setString(16, null); //
                 * cost center stmt5.setString(17, null); // vendor
                 * stmt5.setString(18, null);// customer stmt5.execute();
                 * stmt5.close();
                 *
                 * String smDetailQry = "INSERT INTO in_sm_detail
                 * (id,stockmovement,location,batchname,serialnames,quantity)
                 * VALUES(?,?,?,?,?,?)"; PreparedStatement smd =
                 * conn.prepareStatement(smDetailQry); smd.setString(1,
                 * smDetailId); smd.setString(2, smId); smd.setString(3,
                 * locationid); smd.setString(4, ""); smd.setString(5, null);
                 * smd.setDouble(6, openingQty); smd.execute(); smd.close();
                 *
                 * //updated initial price of the product if
                 * (!StringUtil.isNullOrEmpty(productId) &&
                 * !StringUtil.isNullOrEmpty(currencyid)) { String priceId =
                 * java.util.UUID.randomUUID().toString(); String queryInv =
                 * "insert into
                 * pricelist(id,product,carryin,price,applydate,affecteduser,currency,company)
                 * values (UUID(),?,'T',?,?,'-1',?,?)"; PreparedStatement
                 * stmtprice = conn.prepareStatement(queryInv);
                 * //stmtprice.setString(1, priceId); stmtprice.setString(1,
                 * productId); stmtprice.setDouble(2, price);
                 * stmtprice.setString(3, bbfrom); stmtprice.setString(4,
                 * currencyid); stmtprice.setString(5, companyId);
                 * stmtprice.execute(); stmtprice.close(); }
                 *
                 * }
                 *
                 *
                 * cnt++;
                 */
              

            }

        }
        int count = cnt - 1;
        StringBuilder result = new StringBuilder("" + count).append(" Records added successfully.");
        out.println(result);
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>
