<%-- 
    Document   : DeleteOpeningStock
    Created on : Oct 3, 2017, 9:18:24 PM
    Author     : krawler
--%>


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
        String dbName = request.getParameter("dbname");//"bwrl2909";
        String userName = request.getParameter("username");//"root";
        String password = request.getParameter("password"); //""
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
        String record = "";

        String companyId = "15853f14-3c27-4f5b-876c-3cb79f0a61be";    //companyid for bwrl         
        
        
//        String storeId = "";
//        String query1 = "select id from in_stockmovement where company =? and transaction_module=10"; //default warehose 
//        PreparedStatement stmt1 = conn.prepareStatement(query1);
//        stmt1.setObject(1, companyId);
//        ResultSet rs = stmt1.executeQuery();
//        if (rs.next()) {
//            storeId = rs.getString("id");
//        }
//        String query2 = "select id from in_location where company =? and isdefault=1";  //default warehose 
//        PreparedStatement stmt2 = conn.prepareStatement(query2);
//        stmt2.setObject(1, companyId);
//        ResultSet rs1 = stmt2.executeQuery();
//        if (rs1.next()) {
//            locationid = rs1.getString("id");
//        }
        String sameProduct="";
        while ((record = in.readLine()) != null) {
            String productId = "", uomid = "";

            if (record != "") {
                String[] recarrName = record.split(",");
                String productCode = recarrName[0].trim();
//                String serialName =  recarrName[3].trim();
                
                
                if("Product ID".equals(productCode)){
                    continue;
                }
              //  price =  Double.parseDouble(recarrName[4].trim());   
                //double openingQty = Double.parseDouble(openingQtyString);

                //getting First location Id  ENS       
                String queryForProductId = "select id from product where company=? and productid =?";
                PreparedStatement stmt3 = conn.prepareStatement(queryForProductId);
                stmt3.setObject(1, companyId);
                stmt3.setObject(2, productCode);
                ResultSet rs2 = stmt3.executeQuery();
              //  if (!productCode.equals(sameProduct)) {
                     cnt++;
                        if (rs2.next()) {
                            productId = rs2.getString("id");
//                            uomid = rs2.getString("unitOfMeasure");
                            sameProduct = productCode;
                            //out.println("</br>Product: " + productCode + " and iswarehouseforproduct: " + rs2.getString("iswarehouseforproduct") + " islocationforproduct: " + rs2.getString("islocationforproduct") + " isSerialForProduct: " + rs2.getString("isSerialForProduct") + " isBatchForProduct: " + rs2.getString("isBatchForProduct"));
                            //continue;
                        } else {
                            out.println("</br>Product not found for record row " + cnt + " and id is " + productCode);
                            continue;
                        }
                       
                //    }
                String getStockMovementbyProduct = "select id from in_stockmovement where product=? and transaction_module!=10";  //For Opening Stock

                PreparedStatement ppsmt = conn.prepareStatement(getStockMovementbyProduct);
                ppsmt.setObject(1, productId);
                ResultSet pprs = ppsmt.executeQuery();
                
//                String smid = "";
//                smid = pprs.getString("id");
                 
////                if(!StringUtil.isNullOrEmpty(smid)) {
//                int module = 0;
//                module= pprs.getInt("transaction_module");
                
                if (pprs.next()) {
                    out.println("</br>Product with id "+ productCode +" is use in transaction");
                    continue;
                } else {
                    String queryinvdel = "delete from inventory where company=? and product= ? and newinv='T'";    //Delete Inventory Details
                    PreparedStatement stmt4 = conn.prepareStatement(queryinvdel);
                    stmt4.setObject(1, companyId);
                    stmt4.setObject(2, productId);
                    stmt4.executeUpdate();

                    String querybatchid = "select id,location,warehouse,batchname from newproductbatch where company=? and product= ?";    // Select Record from Product batch
                    PreparedStatement stmtbatch = conn.prepareStatement(querybatchid);
                    stmtbatch.setObject(1, companyId);
                    stmtbatch.setObject(2, productId);
                    ResultSet rsbatch = stmtbatch.executeQuery();
                    String batchId = "";
                    String location = "";
                    String warehouse = "";
                    String batchname = "";
                    if (rsbatch.next()) {
                        batchId = rsbatch.getString("id");
                        location = rsbatch.getString("location");
                        warehouse = rsbatch.getString("warehouse");
                        batchname = rsbatch.getString("batchname");
                    }

                    if (!StringUtil.isNullOrEmpty(batchId)) {

                        String queryForupdate = "delete from locationbatchdocumentmapping where documentid  = ? and batchmapid= ?";   //Delete from Location Batch Mapping Table
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setObject(1, productId);
                        stmtforUpdate.setString(2, batchId);
                        stmtforUpdate.executeUpdate();

                        String queryForInStockDelete = "delete from in_stock where product=? and location=? and store=? and batchname=?";   //Delete  Stock
                        PreparedStatement stmtforstockdel = conn.prepareStatement(queryForInStockDelete);
                        stmtforstockdel.setString(1, productId);
                        stmtforstockdel.setString(2, location);
                        stmtforstockdel.setString(3, warehouse);
                        stmtforstockdel.setString(4, batchname);
                        stmtforstockdel.executeUpdate();

                        String deletesmdetail = "delete from in_sm_detail where location=? and batchname=? and stockmovement in (select id from in_stockmovement where product=? and company=?)";   //Delete record from stock detail
                        PreparedStatement smd = conn.prepareStatement(deletesmdetail);
//                        smd.setString(1, smid);
                        smd.setString(1, location);
                        smd.setString(2, batchname);
                        smd.setString(3, productId);
                        smd.setString(4, companyId);
                        smd.executeUpdate();

                        String deletestockmovement = "delete from in_stockmovement where product=?";   //Delete record from stockmovement
                        PreparedStatement smdel = conn.prepareStatement(deletestockmovement);
                        smdel.setString(1, productId);
//                        smdel.setString(1, smid);
                        smdel.executeUpdate();
                        
                        String deletenewproductbatch = "delete from newproductbatch where company=? and product= ?";   //Delete record from newproductbatch
                        PreparedStatement delnpb = conn.prepareStatement(deletenewproductbatch);
                        delnpb.setObject(1, companyId);
                        delnpb.setObject(2, productId);
                        delnpb.executeUpdate();
                        
                        String productquantity = "Update product set availablequantity=0 where company=? and id=?";
                        PreparedStatement proq = conn.prepareStatement(productquantity);
                        proq.setObject(1, companyId);
                        proq.setObject(2, productId);
                        proq.executeUpdate();

                    }
    

                }
            }
            
        }
        int count = cnt - 1;
        StringBuilder result = new StringBuilder("" + count).append(" Records deleted successfully.");
        out.println(result);
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>