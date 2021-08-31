<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


<%
    Connection conn = null;
    try {

        String serverip = "192.168.0.209";
        String port = "3306";
        String dbName = "newaccountingstaging";
        String SERVICE_ID = "4efb0286-5627-102d-8de6-001cc0794cfa";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";

        String driver = "com.mysql.jdbc.Driver";
        String userName = "krawlersqladmin";
        String password = "krawler";
        String companyId = "04575a0c-b33c-11e3-986d-001e670e1414";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
        FileInputStream invoice = new FileInputStream("/home/krawler/Downloads/Itemlisting1.CSV");
        BufferedReader in=new BufferedReader(new InputStreamReader(invoice));
//        DataInputStream in = new DataInputStream(invoice);
        int cnt = 0;
         String record = "";
        while ((record = in.readLine()) != null){
            if (cnt != 0) {

                //record = record.replace("\\N","\"-1\"");
                String[] recarr = record.split(",");

                String productId = java.util.UUID.randomUUID().toString();
                String cntStr=String.valueOf(cnt + 3);
                String productId1 ="";
                if (cntStr.length() == 1) {
                        productId1 = "PI00000" + (cnt + 3);
                    } else if (cntStr.length() == 2) {
                        productId1 = "PI0000" + (cnt + 3);
                    } else if (cntStr.length() == 3) {
                        productId1 = "PI000" + (cnt + 3);
                    } else if (cntStr.length() == 4) {
                        productId1 = "PI00" + (cnt + 3);
                    } else if (cntStr.length() == 5) {
                        productId1 = "PI0" + (cnt + 3);
                    }    
                String productName = recarr[0].trim();
                productName=productName.replaceAll("\"", "");
                String productDesc = recarr[1].trim();
                productDesc=productDesc.replaceAll("\"", "");
                String productUnitMeasureId = "";

                String productTypeName = recarr[2].trim();
                String productTypeId = "";

                String productInitialPurchaseprice = recarr[3].trim();
                String productSalesprice = recarr[4].trim();

                String productUnitMeasureName = recarr[5].trim();
                int productcycleCntInter = Integer.parseInt(recarr[6].trim());
                String productSalesAcc = recarr[7].trim();
                String productSalesAccId = "";
                String productReturnSalesAcc = recarr[8].trim();
                String productReturnSalesAccId = "";
                String productPurchaseAcc = recarr[9].trim();
                String productPurchaseAccID = "";
                String productReturnPurchaseAcc = recarr[10].trim();
                String productReturnPurchaseAccId = "";



                String query = "select id,name from uom where company=? and name =" + productUnitMeasureName;
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setObject(1, companyId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    productUnitMeasureId = rs.getString("id");
                } else {
                    out.println("Product Measured type not fount for record row " + cnt);
                    continue;
                }

                query = "select id,name from producttype where name =" + productTypeName;
                stmt = conn.prepareStatement(query);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    productTypeId = rs.getString("id");
                } else {
                    out.println("Product Type type not fount for record row " + cnt);
                    continue;
                }


                query = "select id,name from  account ac where ac.company=? and ac.deleteflag=false  and ac.isheaderaccount=false and name=" + productSalesAcc;
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, companyId);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    productSalesAccId = rs.getString("id");
                } else {
                    out.println("Product Sales Account type not fount for record row " + cnt);
                    continue;
                }
                query = "select id,name from  account ac where ac.company=?  and ac.deleteflag=false  and ac.isheaderaccount=false and name=" + productReturnSalesAcc;
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, companyId);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    productReturnSalesAccId = rs.getString("id");
                } else {
                    out.println("Product Sales Return Account type not fount for record row " + cnt);
                    continue;
                }

                query = "select id,name from  account ac where ac.company=?  and ac.deleteflag=false  and ac.isheaderaccount=false and name=" + productPurchaseAcc;
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, companyId);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    productPurchaseAccID = rs.getString("id");
                } else {
                    out.println("Product purchase Account type not fount for record row " + cnt);
                    continue;
                }

                query = "select id,name from  account ac where ac.company=? and ac.deleteflag=false  and ac.isheaderaccount=false and name=" + productReturnPurchaseAcc;
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, companyId);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    productReturnPurchaseAccId = rs.getString("id");
                } else {
                    out.println("Product Purchase Return Account type not fount for record row " + cnt);
                    continue;
                }


                query = "insert into product (id,parent,name,productid,producttype,description,syncable,unitOfMeasure,purchaseAccount,salesAccount,purchaseReturnAccount,salesReturnAccount,leadtimeindays,warrantyperiod,reorderlevel,reorderquantity,company,vendor,deleteflag,isImport,multiuom) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'F',1,?)";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, productId);
                stmt.setString(2, null);
                stmt.setString(3, productName);
                stmt.setString(4, productId1);
                stmt.setString(5, productTypeId);
                stmt.setString(6, productDesc);
                stmt.setString(7, "T");                
                stmt.setString(8, productUnitMeasureId);
                stmt.setString(9, productPurchaseAccID);
                stmt.setString(10, productSalesAccId);
                stmt.setString(11, productReturnPurchaseAccId);
                stmt.setString(12, productReturnSalesAccId);
                stmt.setInt(13, 0);
                stmt.setInt(14, -1);
                stmt.setInt(15, 0);
                stmt.setInt(16, 0);
                stmt.setString(17, companyId);
                stmt.setString(18, null);
                stmt.setString(19, "F");
                stmt.execute();
                if (!productTypeId.equals(SERVICE_ID)) {
                    String productcyclecountId = java.util.UUID.randomUUID().toString();
                    query = "insert into productcyclecount(id,countinterval,tolerance,product,prevdate,nextdate) values(?,?,?,?,?,?)";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, productcyclecountId);
                    stmt.setInt(2, productcycleCntInter);
                    stmt.setInt(3, 0);
                    stmt.setString(4, productId);
                    stmt.setString(5, "1900-01-01 00:00:00");
                    stmt.setString(6, "1900-01-01 00:00:00");
                    stmt.execute();
                }
                if (productInitialPurchaseprice.length() > 0) {
                    String pricelistId = java.util.UUID.randomUUID().toString();
                    query = "insert into pricelist(id,applydate,carryin,price,product,company,affecteduser) values(?,now(),'T',?,?,?,-1)";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, pricelistId);
                    stmt.setDouble(2, Double.parseDouble(productInitialPurchaseprice));
                    stmt.setString(3, productId);
                    stmt.setString(4, companyId);
                    stmt.execute();
                }
                if (productSalesprice.length() > 0) {
                    String pricelistId = java.util.UUID.randomUUID().toString();
                    query = "insert into pricelist(id,applydate,carryin,price,product,company,affecteduser) values(?,now(),'F',?,?,?,-1)";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, pricelistId);
                    stmt.setDouble(2, Double.parseDouble((productSalesprice)));
                    stmt.setString(3, productId);
                    stmt.setString(4, companyId);
                    stmt.execute();
                }

                rs.close();
                stmt.close();
            }
            cnt++;
        }
         out.println(cnt +" Records added successfully.");
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>
