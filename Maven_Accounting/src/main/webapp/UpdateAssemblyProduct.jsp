<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
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
        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");
        String filepath = request.getParameter("filepath");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(filepath)) {
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password, filepath) in url. so please provide all these parameters correctly. ");
        }
        String driver = "com.mysql.jdbc.Driver";
        PreparedStatement pst1 = null;
        ResultSet rst1 = null;
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, username, password);
        conn.setAutoCommit(false);
        String compqry = "select companyid from company where subdomain='" + subdomain + "'";
        pst1 = conn.prepareStatement(compqry);
        rst1 = pst1.executeQuery();
        while (rst1.next()) {
            String companyId = rst1.getString("companyid");
            FileInputStream path = new FileInputStream(filepath);
            BufferedReader in = new BufferedReader(new InputStreamReader(path));
//        DataInputStream in = new DataInputStream(invoice);
            int cnt = 0;
            String record = "";
            while ((record = in.readLine()) != null) {
                if (cnt == 0 && record != "") {
                    //there is nothing to take this are headers                
                }
                if (cnt != 0 && !StringUtil.isNullOrEmpty(record)) {
                    boolean isSerial = false, isBatch = false;
                    String warehouseid = "", locationid = "";
                    String[] rec = record.split(",");
                    String productId = rec[1].trim();       //Product ID (Product Code)
                    String initialQuantity = rec[4].trim(); //Product Opening Quantity
                    String additionalFreeText = rec[5].trim();  //Additional Free Text From Remark Tab
                    String location = rec[9].trim();    //Default Location From Inventory Tab
                    String warehouse = rec[10].trim();  //Default Warehouse From Inventory Tab
                    Double prodinitialqty = 0.0;
                    if (!StringUtil.isNullOrEmpty(initialQuantity)) {
                        prodinitialqty = Double.parseDouble((initialQuantity));
                    }
                    out.println(productId+"<br><br>");
                    String pid = "", currency = "", unitOfMeasure="";
                    long createdon = 1472659200000L;    //2016-09-01

                    double prodAvlqty = 0;
                    String query3 = "select id, isBatchForProduct, isSerialForProduct,availablequantity, currency, unitOfMeasure from product where productid=? and company=?";
                    PreparedStatement stmt3 = conn.prepareStatement(query3);
                    stmt3.setObject(1, productId);
                    stmt3.setObject(2, companyId);
                    ResultSet rs3 = stmt3.executeQuery();
                    if (rs3.next()) {
                        pid = rs3.getString("id");
                        isBatch = rs3.getBoolean("isBatchForProduct");
                        isSerial = rs3.getBoolean("isSerialForProduct");
                        prodAvlqty = rs3.getDouble("availablequantity");
                        currency = rs3.getString("currency");
                        unitOfMeasure = rs3.getString("unitOfMeasure");
                        //If Batch Or Serial is there then skip the record updation.
                        if (isBatch || isSerial) {
                            out.println("<br><br>" + productId + " cannot update.<br><br>");
                            continue;
                        }

                        //Update the Product
                        String warehouseqry = "select id from inventorywarehouse where company=? and name=? ";
                        PreparedStatement stmt4 = conn.prepareStatement(warehouseqry);
                        stmt4.setObject(1, companyId);
                        stmt4.setObject(2, warehouse);
                        ResultSet rs4 = stmt4.executeQuery();
                        if (rs4.next()) {
                            warehouseid = rs4.getString("id");
                        }
                        String locationqry = "select id from inventorylocation where company=? and name=? ";
                        PreparedStatement stmt5 = conn.prepareStatement(locationqry);
                        stmt5.setObject(1, companyId);
                        stmt5.setObject(2, location);
                        ResultSet rs5 = stmt5.executeQuery();
                        if (rs5.next()) {
                            locationid = rs5.getString("id");
                        }
                    }

                    Date transactionDate = new Date(createdon);
                    double oldInvqty = 0;
                    java.util.Date invdate = null;
                    String invuom = "";
                    if (!StringUtil.isNullOrEmpty(pid)) {


                        String InvId = "", batchId = "";

                        String queryinvid = "select id,baseuomquantity, updatedate, uom from inventory where carryin='T' AND newinv='T' AND isopening='T' AND company=? and product= ?";
                        PreparedStatement stmt4 = conn.prepareStatement(queryinvid);
                        stmt4.setObject(1, companyId);
                        stmt4.setObject(2, pid);
                        ResultSet rs4 = stmt4.executeQuery();
                        if (rs4.next()) {
                            InvId = rs4.getString("id");
                            oldInvqty = rs4.getDouble("baseuomquantity");
                            invdate = rs4.getObject("updatedate") != null ? (java.util.Date) rs4.getObject("updatedate") : new java.util.Date();
                            invuom = rs4.getString("uom");
                        }
                        //updated entry in inventory
                        if (!StringUtil.isNullOrEmpty(InvId)) {
                            String updatequery = "update inventory set quantity=?, baseuomquantity=? where id=?";
                            PreparedStatement stmtquery = conn.prepareStatement(updatequery);
                            stmtquery.setDouble(1, prodinitialqty);
                            stmtquery.setDouble(2, prodinitialqty);
                            stmtquery.setString(3, InvId);
                            stmtquery.executeUpdate();

                            //Update Product Table
                            String produpdateqry = "update product set availablequantity=?, additionalfreetext=?, warehouse=?, location=? where company= ? and id=?";
                            PreparedStatement stmtt = conn.prepareStatement(produpdateqry);
                            stmtt.setDouble(1, (prodAvlqty - oldInvqty) + prodinitialqty);
                            stmtt.setString(2, additionalFreeText);
                            stmtt.setString(3, warehouseid);
                            stmtt.setString(4, locationid);
                            stmtt.setString(5, companyId);
                            stmtt.setString(6, pid);
                            stmtt.executeUpdate();

                        } else {
                            InvId = java.util.UUID.randomUUID().toString();
                            String queryInv = "insert into inventory(id,product,quantity,baseuomquantity,actquantity,baseuomrate,description,carryin,defective,newinv,company,updatedate,deleteflag) values (?,?,?,?,?,?,'Inventory Opened','T','F','T',?,?,'F')";
                            PreparedStatement stmtInv = conn.prepareStatement(queryInv);
                            stmtInv.setString(1, InvId);
                            stmtInv.setString(2, pid);
                            stmtInv.setDouble(3, prodinitialqty);
                            stmtInv.setDouble(4, prodinitialqty);
                            stmtInv.setDouble(5, 0);
                            stmtInv.setDouble(6, 1);
                            stmtInv.setString(7, companyId);
                            stmtInv.setDate(8, transactionDate);
                            stmtInv.execute();
                            stmtInv.close();

                            //Update Product Table
                            String produpdateqry = "update product set availablequantity=?, additionalfreetext=?, warehouse=?, location=? where company= ? and id=?";
                            PreparedStatement stmtt = conn.prepareStatement(produpdateqry);
                            stmtt.setDouble(1, prodAvlqty + prodinitialqty);
                            stmtt.setString(2, additionalFreeText);
                            stmtt.setString(3, warehouseid);
                            stmtt.setString(4, locationid);
                            stmtt.setString(5, companyId);
                            stmtt.setString(6, pid);
                            stmtt.executeUpdate();
                        }

                        double batchQty = 0, batchQtyDue = 0;
                        String querybatchid = "select id,quantity,quantitydue from newproductbatch where company=? and product= ? AND warehouse=? AND location=? ";
                        PreparedStatement stmtbatch = conn.prepareStatement(querybatchid);
                        stmtbatch.setObject(1, companyId);
                        stmtbatch.setObject(2, pid);
                        stmtbatch.setObject(3, warehouseid);
                        stmtbatch.setObject(4, locationid);
                        ResultSet rsbatch = stmtbatch.executeQuery();
                        if (rsbatch.next()) {
                            batchId = rsbatch.getString("id");
                            batchQty = rsbatch.getDouble("quantity");
                            batchQtyDue = rsbatch.getDouble("quantitydue");
                        }
                        //Update New Product Batch 
                        if (!StringUtil.isNullOrEmpty(batchId)) {
                            String instockID = "", instockmovementID = "";
                            String queryForupdate = "update newproductbatch set quantity=?,quantitydue=? where id=? and company=? ";
                            PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                            stmtforUpdate.setDouble(1, (batchQty - oldInvqty) + prodinitialqty);
                            stmtforUpdate.setDouble(2, (batchQtyDue - oldInvqty) + prodinitialqty);
                            stmtforUpdate.setString(3, batchId);
                            stmtforUpdate.setString(4, companyId);
                            stmtforUpdate.executeUpdate();

                            String queryForlocupdate = "update locationbatchdocumentmapping set quantity=? where  batchmapid=? and documentid=? ";
                            PreparedStatement stmtforlocUpdate = conn.prepareStatement(queryForlocupdate);
                            stmtforlocUpdate.setDouble(1, prodinitialqty);
                            stmtforlocUpdate.setString(2, batchId);
                            stmtforlocUpdate.setString(3, pid);
                            stmtforlocUpdate.executeUpdate();

                            double stockqty = 0;
                            String in_stockQry = "select id, quantity from in_stock where company=? AND product= ? AND store=? AND location=? ";
                            PreparedStatement stmtinstock = conn.prepareStatement(in_stockQry);
                            stmtinstock.setObject(1, companyId);
                            stmtinstock.setObject(2, pid);
                            stmtinstock.setObject(3, warehouseid);
                            stmtinstock.setObject(4, locationid);
                            ResultSet rsinstock = stmtinstock.executeQuery();
                            if (rsinstock.next()) {
                                instockID = rsinstock.getString("id");
                                stockqty = rsinstock.getDouble("quantity");
                            }
                            //Update in_Stock Table
                            if (!StringUtil.isNullOrEmpty(instockID)) {
                                String in_stockUpdate = "update in_stock set quantity=? where  id=? and company=? ";
                                PreparedStatement stmtin_stockUpdate = conn.prepareStatement(in_stockUpdate);
                                stmtin_stockUpdate.setDouble(1, (stockqty - oldInvqty) + prodinitialqty);
                                stmtin_stockUpdate.setString(2, instockID);
                                stmtin_stockUpdate.setString(3, companyId);
                                stmtin_stockUpdate.executeUpdate();


                                String in_sm_detailqry = "select id from in_stockmovement where company=? AND modulerefid= ? AND store=? AND location=? ";
                                PreparedStatement stmtsmdetail = conn.prepareStatement(in_sm_detailqry);
                                stmtsmdetail.setObject(1, companyId);
                                stmtsmdetail.setObject(2, pid);
                                stmtsmdetail.setObject(3, warehouseid);
                                stmtsmdetail.setObject(4, locationid);
                                ResultSet rssmdetail = stmtsmdetail.executeQuery();
                                if (rssmdetail.next()) {
                                    instockmovementID = rssmdetail.getString("id");
                                }
                                //Update in_stockmovement
                                if (!StringUtil.isNullOrEmpty(instockmovementID)) {
                                    String in_stockmovementqry = "update in_stockmovement set quantity=? where id=? and company=?";
                                    PreparedStatement stmtinstockmovement = conn.prepareStatement(in_stockmovementqry);
                                    stmtinstockmovement.setDouble(1, prodinitialqty);
                                    stmtinstockmovement.setString(2, instockmovementID);
                                    stmtinstockmovement.setString(3, companyId);
                                    stmtinstockmovement.executeUpdate();

                                    //Update Stock_MovementDetail
                                    String smDetailQry = "UPDATE in_sm_detail SET quantity= ? WHERE  stockmovement=?";
                                    PreparedStatement smd = conn.prepareStatement(smDetailQry);
                                    smd.setDouble(1, prodinitialqty);
                                    smd.setString(2, instockmovementID);
                                    smd.executeUpdate();
                                }

                            } else {
                                //Entry of stock on Inventory Side ----
                                String query2 = "INSERT INTO in_stock(id, product, store, location, batchname, serialnames, company, quantity, createdon, modifiedon) "
                                        + " VALUES (UUID(), ?,?,?,?,?,?,?, NOW(), NOW()) ";
                                PreparedStatement stmtquery2 = conn.prepareStatement(query2);
                                stmtquery2.setString(1, pid);
                                stmtquery2.setString(2, warehouseid);
                                stmtquery2.setString(3, locationid);
                                stmtquery2.setString(4, "");
                                stmtquery2.setString(5, null);
                                stmtquery2.setString(6, companyId);
                                stmtquery2.setDouble(7, (stockqty - oldInvqty) + prodinitialqty);   
                                stmtquery2.execute();

                                //Update Stock_Movement
                                double price = 0;
                                String smId = java.util.UUID.randomUUID().toString();
                                String smDetailId = java.util.UUID.randomUUID().toString();

                                String getProducInitPurchasePriceQry = " select pl1.price as initpurchaseprice from pricelist pl1 where pl1.affecteduser='-1' "
                                        + " and pl1.product=? and pl1.carryin='T' and pl1.currency= ? and"
                                        + " (pl1.applydate in (select min(pl2.applydate) from pricelist pl2 "
                                        + " where pl2.affecteduser='-1' and pl2.product=pl1.product and pl2.currency=? and pl2.carryin=pl1.carryin group by pl2.product)) ";

                                PreparedStatement ppsmt = conn.prepareStatement(getProducInitPurchasePriceQry);
                                ppsmt.setObject(1, pid);
                                ppsmt.setObject(2, currency);
                                ppsmt.setObject(3, currency);
                                ResultSet pprs = ppsmt.executeQuery();
                                if (pprs.next()) {
                                    price = pprs.getDouble("initpurchaseprice");
                                }
                                String stockMovementQry = "INSERT INTO in_stockmovement(id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,"
                                        + "transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer,modulerefdetailid) "
                                        + "values(?,?,?,?,?,?,?,?,NOW(),?,NOW(),?,?,?,?,?,?,?,?)";
                                PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                                stmt1.setString(1, smId);
                                stmt1.setString(2, companyId);
                                stmt1.setString(3, pid); //product 
                                stmt1.setString(4, warehouseid);
                                stmt1.setString(5, null); // null becoz as per new code it is present in sm detail 
                                stmt1.setString(6, !StringUtil.isNullOrEmpty(invuom)?invuom:unitOfMeasure);//stockuom 
                                stmt1.setDouble(7, prodinitialqty); //qty 
                                stmt1.setDouble(8, price); //initial purchase price 
                                //stmt1.setObject(9, invdate); //createddon 
                                stmt1.setString(9, null); // transaction no
                                //stmt1.setObject(11, invdate); // transaction date 
                                stmt1.setInt(10, 0); // opening stock :0 , stock In : 1 .Stock out : 2 
                                stmt1.setInt(11, 10); // transaction module // do- 6,grn-5 ,10 -opening/new product 
                                stmt1.setString(12, pid); // modulerefid (parent) 
                                stmt1.setString(13, "Stock added through OPENING");
                                stmt1.setString(14, null); // cost center
                                stmt1.setString(15, null); // vendor 
                                stmt1.setString(16, null);// customer 
                                stmt1.setString(17, pid);// modulerefid (child) 
                                stmt1.execute();
                                stmt1.close();

                                //Update Stock_MovementDetail
                                String smDetailQry = "INSERT INTO in_sm_detail (id,stockmovement,location,batchname,serialnames,quantity) VALUES(?,?,?,?,?,?)";
                                PreparedStatement smd = conn.prepareStatement(smDetailQry);
                                smd.setString(1, smDetailId);
                                smd.setString(2, smId);
                                smd.setString(3, locationid);
                                smd.setString(4, "");
                                smd.setString(5, null);
                                smd.setDouble(6, prodinitialqty);
                                smd.execute();
                                smd.close();
                            }
                        } else {
                            //new entry in newproductbatch and lbdm

                            String batchMapId = java.util.UUID.randomUUID().toString();
                            String querybatch = "insert into newproductbatch (id,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company) "
                                    + "values (?,?,?,?,?,28,?,'T','T',?)";
                            PreparedStatement stmtb = conn.prepareStatement(querybatch);
                            stmtb.setString(1, batchMapId);
                            stmtb.setDouble(2, prodinitialqty);
                            stmtb.setDouble(3, prodinitialqty);
                            stmtb.setString(4, locationid); //location
                            stmtb.setString(5, warehouseid);  //warehouse
                            stmtb.setString(6, pid);
                            stmtb.setString(7, companyId);
                            stmtb.execute();
                            stmtb.close();

                            String locBatchdocId = java.util.UUID.randomUUID().toString();
                            String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                            PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                            stmtlbmap.setString(1, locBatchdocId);
                            stmtlbmap.setDouble(2, prodinitialqty);
                            stmtlbmap.setString(3, pid);
                            stmtlbmap.setString(4, batchMapId);
                            stmtlbmap.execute();
                            stmtlbmap.close();

                            //Entry of stock on Inventory Side ----
                            String query2 = "INSERT INTO in_stock(id, product, store, location, batchname, serialnames, company, quantity, createdon, modifiedon) "
                                    + " VALUES (UUID(), ?,?,?,?,?,?,?, NOW(), NOW()) ";
                            PreparedStatement stmtquery2 = conn.prepareStatement(query2);
                            stmtquery2.setString(1, pid);
                            stmtquery2.setString(2, warehouseid);
                            stmtquery2.setString(3, locationid);
                            stmtquery2.setString(4, "");
                            stmtquery2.setString(5, null);
                            stmtquery2.setString(6, companyId);
                            stmtquery2.setDouble(7, prodinitialqty);   //CHECK IT
                            stmtquery2.execute();

                            //Update Stock_Movement
                            double price = 0;
                            String smId = java.util.UUID.randomUUID().toString();
                            String smDetailId = java.util.UUID.randomUUID().toString();

                            String getProducInitPurchasePriceQry = " select pl1.price as initpurchaseprice from pricelist pl1 where pl1.affecteduser='-1' "
                                    + " and pl1.product=? and pl1.carryin='T' and pl1.currency= ? and"
                                    + " (pl1.applydate in (select min(pl2.applydate) from pricelist pl2 "
                                    + " where pl2.affecteduser='-1' and pl2.product=pl1.product and pl2.currency=? and pl2.carryin=pl1.carryin group by pl2.product)) ";

                            PreparedStatement ppsmt = conn.prepareStatement(getProducInitPurchasePriceQry);
                            ppsmt.setObject(1, pid);
                            ppsmt.setObject(2, currency);
                            ppsmt.setObject(3, currency);
                            ResultSet pprs = ppsmt.executeQuery();
                            if (pprs.next()) {
                                price = pprs.getDouble("initpurchaseprice");
                            }
                            String stockMovementQry = "INSERT INTO in_stockmovement(id,company,product,store,location,stockuom,quantity,priceperunit,createdon,transactionno,"
                                    + "transaction_date,transaction_type,transaction_module,modulerefid,remark,costcenter,vendor,customer,modulerefdetailid) "
                                    + "values(?,?,?,?,?,?,?,?,NOW(),?,NOW(),?,?,?,?,?,?,?,?)";
                            PreparedStatement stmt1 = conn.prepareStatement(stockMovementQry);
                            stmt1.setString(1, smId);
                            stmt1.setString(2, companyId);
                            stmt1.setString(3, pid); //product 
                            stmt1.setString(4, warehouseid);
                            stmt1.setString(5, null); // null becoz as per new code it is present in sm detail 
                            stmt1.setString(6, !StringUtil.isNullOrEmpty(invuom)?invuom:unitOfMeasure);//stockuom 
                            stmt1.setDouble(7, prodinitialqty); //qty 
                            stmt1.setDouble(8, price); //initial purchase price 
                            //stmt1.setObject(9, invdate); //createddon 
                            stmt1.setString(9, null); // transaction no
                            //stmt1.setObject(11, invdate); // transaction date 
                            stmt1.setInt(10, 0); // opening stock :0 , stock In : 1 .Stock out : 2 
                            stmt1.setInt(11, 10); // transaction module // do- 6,grn-5 ,10 -opening/new product 
                            stmt1.setString(12, pid); // modulerefid (parent) 
                            stmt1.setString(13, "Stock added through OPENING.");
                            stmt1.setString(14, null); // cost center
                            stmt1.setString(15, null); // vendor 
                            stmt1.setString(16, null);// customer 
                            stmt1.setString(17, pid);// modulerefid (child) 
                            stmt1.execute();
                            stmt1.close();

                            //Update Stock_MovementDetail
                            String smDetailQry = "INSERT INTO in_sm_detail (id,stockmovement,location,batchname,serialnames,quantity) VALUES(?,?,?,?,?,?)";
                            PreparedStatement smd = conn.prepareStatement(smDetailQry);
                            smd.setString(1, smDetailId);
                            smd.setString(2, smId);
                            smd.setString(3, locationid);
                            smd.setString(4, "");
                            smd.setString(5, null);
                            smd.setDouble(6, prodinitialqty);
                            smd.execute();
                            smd.close();
                        }
                        // stmt.close();
                    } else {
                        System.out.println("Product Not Found");
                        continue;
                    }

                }
                cnt++;
            }
            int count = cnt - 1;
            StringBuilder result = new StringBuilder("" + count).append(" Records updated successfully.");
            out.println(result);
        }
        conn.commit();
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    } finally {
        if (conn != null) {
                conn.close();
            }
    }

%>
