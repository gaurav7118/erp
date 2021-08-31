<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%
    Connection conn = null;
    try {

        String serverip = "192.168.0.116";
        String port = "3306";
        String dbName = "smsdb";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";

        String driver = "com.mysql.jdbc.Driver";
        String userName = "krawlersqladmin";
        String password = "krawler";
        String companyId = "03848f44-ed14-45d7-a1c8-7e7997ad8657";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
        FileInputStream invoice = new FileInputStream("/home/krawler/Desktop/products.csv");
        BufferedReader in = new BufferedReader(new InputStreamReader(invoice));
//        DataInputStream in = new DataInputStream(invoice);
        int cnt = 0;
        String record = "";
        while ((record = in.readLine()) != null) {
            if (cnt == 0 && record != "") {
                //there is nothing to take this are headers                
            }
            if (cnt != 0 && !StringUtil.isNullOrEmpty(record)) {

                String[] rec = record.split(",");
                String productId = rec[0].trim();
                String initialQuantity = rec[2].trim();
                String rate = rec[3].trim();
                Double prodinitialqty = 0.0;
                Double initialpurchaseprice =0.0;
                   if (!StringUtil.isNullOrEmpty(initialQuantity)) {
                        prodinitialqty = Double.parseDouble((initialQuantity));
                    }
                if (!StringUtil.isNullOrEmpty(rate)) {
                       initialpurchaseprice=Double.parseDouble((rate));
                }
                String cntStr = String.valueOf(cnt + 3);
                String pid = "", currencyid = "", uomid = "";
                long createdon =1429036200000L;
                
            
                String query3 = "select id,unitOfMeasure,createdon from product where productid=? and company=?";
                PreparedStatement stmt3 = conn.prepareStatement(query3);
                stmt3.setObject(1, productId);
                stmt3.setObject(2, companyId);
                ResultSet rs3 = stmt3.executeQuery();
                if (rs3.next()) {
                    pid = rs3.getString("id");
                    uomid = rs3.getString("unitOfMeasure");
                    //createdon = rs3.getLong("createdon");
                }
                 Date transactionDate = new Date(createdon);
      
                String queryforCurrency = "select currency from company where companyid=? ";
                PreparedStatement stmtcurrency = conn.prepareStatement(queryforCurrency);
                stmtcurrency.setObject(1, companyId);
                ResultSet rsForcurrency = stmtcurrency.executeQuery();
                if (rsForcurrency.next()) {
                    currencyid = rsForcurrency.getString("currency");
                }
                if (!StringUtil.isNullOrEmpty(pid)) {
                    String InvId = "", batchId = "",InvLocId="",InvWarId="";
                    double Invbaseuomquantity = 0.0, Invquantity = 0.0;
                    String queryinvid = "select id,quantity,baseuomquantity from inventory where company=? and product= ? and newinv='T'";
                    PreparedStatement stmt4 = conn.prepareStatement(queryinvid);
                    stmt4.setObject(1, companyId);
                    stmt4.setObject(2, pid);
                    ResultSet rs4 = stmt4.executeQuery();
                    if (rs4.next()) {
                        InvId = rs4.getString("id");
                        Invquantity = rs4.getDouble("quantity");
                        Invbaseuomquantity = rs4.getDouble("baseuomquantity");
                    }
                    //updated entry in inventory
                    if (!StringUtil.isNullOrEmpty(InvId)) {
                        PreparedStatement stmtquery = conn.prepareStatement(queryinvid);
                        String updatequery = "update inventory set quantity=?,baseuomquantity=?,carryin='T',defective='F',newinv='T',updatedate=now(),deleteflag='F' where id=?";
                        stmtquery = conn.prepareStatement(updatequery);
                        stmtquery.setDouble(1, prodinitialqty);
                        stmtquery.setDouble(2, prodinitialqty);
                        stmtquery.setString(3, InvId);
                        stmtquery.executeUpdate();

                    } else {
                        InvId = java.util.UUID.randomUUID().toString();
                        String queryInv = "insert into inventory(id,product,quantity,baseuomquantity,actquantity,baseuomrate,uom,description,carryin,defective,newinv,company,updatedate,deleteflag) values (?,?,?,?,?,?,?,'Inventory Opened','T','F','T',?,?,'F')";
                        PreparedStatement stmtInv = conn.prepareStatement(queryInv);
                        stmtInv.setString(1, InvId);
                        stmtInv.setString(2, pid);
                        stmtInv.setDouble(3, prodinitialqty);
                        stmtInv.setDouble(4, prodinitialqty);
                        stmtInv.setDouble(5, 0);
                        stmtInv.setDouble(6, 1);
                        stmtInv.setString(7, uomid);
                        stmtInv.setString(8, companyId);
                        stmtInv.setDate(9, transactionDate);
                        stmtInv.execute();
                        stmtInv.close();
                    }
                    String querybatchid = "select id,location,warehouse from newproductbatch where company=? and product= ? and ispurchase='T' and isopening='T' and transactiontype=28";
                    PreparedStatement stmtbatch = conn.prepareStatement(querybatchid);
                    stmtbatch.setObject(1, companyId);
                    stmtbatch.setObject(2, pid);
                    ResultSet rsbatch = stmtbatch.executeQuery();
                    if (rsbatch.next()) {
                        batchId = rsbatch.getString("id");
                    }
                   if (!StringUtil.isNullOrEmpty(batchId)) {
                           String queryForupdate = "update newproductbatch set quantity=?,quantitydue=? where id=? and company=? ";
                           PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                           stmtforUpdate.setDouble(1, prodinitialqty);
                           stmtforUpdate.setDouble(2, prodinitialqty);
                           stmtforUpdate.setString(3, batchId);
                           stmtforUpdate.setString(4, companyId);
                           stmtforUpdate.executeUpdate();

                           String queryForlocupdate = "update locationbatchdocumentmapping set quantity=? where  batchmapid=? and documentid=? and transactiontype=28 ";
                           PreparedStatement stmtforlocUpdate = conn.prepareStatement(queryForlocupdate);
                           stmtforlocUpdate.setDouble(1, prodinitialqty);
                           stmtforlocUpdate.setString(2, batchId);
                           stmtforlocUpdate.setString(3, pid);
                           stmtforlocUpdate.executeUpdate();
                           
                        String query2 = "INSERT INTO in_stock(id, product, store, location, batchname, serialnames, company, quantity, createdon, modifiedon) "
                                        + " VALUES (UUID(), ?,?,?,?,?,?,?, NOW(), NOW()) ";
                            PreparedStatement stmtquery2 = conn.prepareStatement(query2);
                            stmtquery2.setString(1, pid);
                            stmtquery2.setString(2, InvWarId);
                            stmtquery2.setString(3, InvLocId);
                            stmtquery2.setString(4, "");
                            stmtquery2.setString(5, null);
                            stmtquery2.setString(6, companyId);
                            stmtquery2.setDouble(7, prodinitialqty);
                            stmtquery2.execute();
                            stmtquery2.close();
                    }

                    //updated initial price of the product
                   if (!StringUtil.isNullOrEmpty(pid) && !StringUtil.isNullOrEmpty(currencyid)) {
                    String priceId = java.util.UUID.randomUUID().toString();
                    String queryInv = "insert into pricelist(id,product,carryin,price,applydate,affecteduser,currency,company) values (?,?,'T',?,now(),'-1',?,?)";
                    PreparedStatement stmtprice = conn.prepareStatement(queryInv);
                    stmtprice.setString(1, priceId);
                    stmtprice.setString(2, pid);
                    stmtprice.setDouble(3, initialpurchaseprice);
                    stmtprice.setString(4, currencyid);
                    stmtprice.setString(5, companyId);
                    stmtprice.execute();
                    stmtprice.close();
                    }

                    // stmt.close();
                } else {
                    System.out.println("Product Not Found");
                    continue;
                }

            }
            cnt++;
        }
        
        FileInputStream file = new FileInputStream("/home/krawler/Desktop/products.csv");
        BufferedReader infile = new BufferedReader(new InputStreamReader(file));
        String recordfile = "";
         int cntp = 0;
        while ((recordfile = infile.readLine()) != null) {
            if (cntp == 0 && recordfile != "") {
                //there is nothing to take this are headers                
            }
            if (cntp != 0 && !StringUtil.isNullOrEmpty(recordfile)) {

                String[] rec = recordfile.split(",");
                String productid = rec[0].trim();
                String pid = "";
               String query3 = "select id,unitOfMeasure from product where productid=? and company=?";
              PreparedStatement stmt3 = conn.prepareStatement(query3);
              stmt3.setObject(1, productid);
              stmt3.setObject(2, companyId);
              ResultSet rs3 = stmt3.executeQuery();
              if (rs3.next()) {
                  pid = rs3.getString("id");

              }
            if (!StringUtil.isNullOrEmpty(pid)) {
                String batchId = "";
                double deliveredQty = 0.0;
                double BatchQty = 0.0;
              
               
                String querydo = "select sum(baseuomdeliveredquantity) from dodetails where  product=? and company=? ";
                PreparedStatement stmtdo = conn.prepareStatement(querydo);  //select all product from company
                stmtdo.setObject(1, pid);
                stmtdo.setObject(2, companyId);
                ResultSet rsdo = stmtdo.executeQuery();
                while (rsdo.next()) {
                    deliveredQty = rsdo.getDouble(1);
                }
                if (deliveredQty > 0) {
                        String querybatchid = "select id,quantity from newproductbatch where company=? and product= ? and ispurchase='T' and isopening='T' and transactiontype=28";
                        PreparedStatement stmtbatch = conn.prepareStatement(querybatchid);
                        stmtbatch.setObject(1, companyId);
                        stmtbatch.setObject(2, pid);
                        ResultSet rsbatch = stmtbatch.executeQuery();
                        if (rsbatch.next()) {
                            batchId = rsbatch.getString("id");
                            BatchQty= rsbatch.getDouble("quantity");
                        }

                        String queryForupdate = "update newproductbatch set quantitydue=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, (BatchQty-deliveredQty));
                        stmtforUpdate.setString(2, batchId);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
              } else {
                    System.out.println("Product Not Found");
                    continue;
               }
            } //iterate for all product
         cntp++;
         }
          int count = cnt - 1;
            StringBuilder result = new StringBuilder("" + count).append(" Records updated successfully.");
            out.println(result);
        }  catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>
