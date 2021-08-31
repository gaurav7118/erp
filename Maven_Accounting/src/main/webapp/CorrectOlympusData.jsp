
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
       // String serverip = "localhost";
       // String port = "3306";
       // String dbName = "invacc";
       // String userName = "krawlersqladmin";//"krawlersqladmin";
       // String password = "Krawler[X]"; //"krawler"
       // String subdomain = "olympus3";
        
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);

        String companyId = "";
        int totalCompanyUpdationCnt = 0;

        String query1 = " select companyid,currency from company where subdomain=? ";
        PreparedStatement stmt1 = conn.prepareStatement(query1);
        stmt1.setString(1, subdomain);
        ResultSet rs1 = stmt1.executeQuery();
        while (rs1.next()) {
            companyId = rs1.getString("companyid");
        }

        String query2 = "update newproductbatch set lockquantity=0 where lockquantity<0 and company=? ";
        PreparedStatement stmt2 = conn.prepareStatement(query2);
        stmt2.setString(1, companyId);
        stmt2.executeUpdate();

        String query3 = "update newproductbatch set quantitydue=0 where quantitydue<0 and company=? ";
        PreparedStatement stmt3 = conn.prepareStatement(query3);
        stmt3.setString(1, companyId);
        stmt3.executeUpdate();

        String query4 = " SELECT tb1.nbpquantity,tb1.location,tb1.productid,tb1.store,tb2.stkquantity,tb2.serialnames,tb1.pid,tb1.batchid,tb1.locationid,tb1.warehouse FROM "
                + " (SELECT SUM(quantitydue) AS nbpquantity,lcn.`name` AS location,wh.`name` AS store,p.productid,p.id AS pid,nbp.id AS batchid,nbp.location AS locationid,nbp.warehouse FROM newproductbatch nbp "
                + " LEFT JOIN in_location lcn  ON  lcn.id=nbp.location "
                + " LEFT JOIN inventorywarehouse wh ON wh.id=nbp.warehouse"
                + " LEFT JOIN product p ON p.id=nbp.product"
                + " WHERE nbp.company='" + companyId + "' AND nbp.location IS NOT NULL GROUP BY nbp.warehouse,nbp.location,nbp.product ORDER BY product) AS tb1"
                + " INNER JOIN ("
                + " SELECT SUM(stk.quantity) AS stkquantity,lcn.`name` AS location,wh.`name` AS store,p.productid,stk.serialnames FROM in_stock stk "
                + " LEFT JOIN in_location lcn  ON  lcn.id=stk.location "
                + " LEFT JOIN inventorywarehouse wh ON wh.id=stk.store"
                + " LEFT JOIN product p ON p.id=stk.product"
                + " WHERE stk.company='" + companyId + "' GROUP BY stk.store,stk.location,stk.product ORDER BY stk.product) AS tb2 ON tb1.location=tb2.location  "
                + " WHERE tb1.location=tb2.location AND tb1.productid=tb2.productid "
                + " AND tb1.store=tb2.store AND tb1.nbpquantity<>tb2.stkquantity ORDER BY tb1.productid ";

        PreparedStatement stmt4 = conn.prepareStatement(query4);
        ResultSet rs4 = stmt4.executeQuery();
        while (rs4.next()) {
            double stkQuantity = rs4.getDouble("stkquantity");
            double batchQuantity = rs4.getDouble("nbpquantity");
            String location = rs4.getString("locationid");
            String wareHouse = rs4.getString("warehouse");
            String product = rs4.getString("pid");
            String serialNames = rs4.getString("serialnames");
            String batchId = rs4.getString("batchid");
            String productid1 = rs4.getString("productid");
            out.println("<br><br> Product : " + productid1);
            if (stkQuantity == 0) {
                String query5 = "update newproductbatch set lockquantity=0,quantitydue=0 where location=? and warehouse=? and product=? and company=? ";
                PreparedStatement stmt5 = conn.prepareStatement(query5);
                stmt5.setString(1, location);
                stmt5.setString(2, wareHouse);
                stmt5.setString(3, product);
                stmt5.setString(4, companyId);
                stmt5.executeUpdate();

                String query6 = "update newbatchserial set lockquantity=0,quantitydue=0 where  batch in (select id from newproductbatch where location='" + location + "' "
                        + " and warehouse='" + wareHouse + "' and product='" + product + "' and company='" + companyId + "') ";
                PreparedStatement stmt6 = conn.prepareStatement(query6);
                stmt6.executeUpdate();

            } else if (!StringUtil.isNullOrEmpty(serialNames) && batchQuantity < stkQuantity) {
                double qty = 0;

                String selQuery = "SELECT id,batchname,serialnames,quantity FROM in_stock WHERE product=? AND store=? AND location=? AND company=? ";
                PreparedStatement stmt = conn.prepareStatement(selQuery);
                stmt.setString(1, product);
                stmt.setString(2, wareHouse);
                stmt.setString(3, location);
                stmt.setString(4, companyId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    serialNames = rs.getString("serialnames");
                    String[] serArr = serialNames.split(",");
                    for (int i = 0; i < serArr.length; i++) {
                        String srName = serArr[i].toString();
                        String query7 = " UPDATE newbatchserial nsr INNER JOIN newproductbatch nbp ON nbp.id=nsr.batch "
                                + " SET nsr.quantitydue=1 AND nsr.lockquantity=0 "
                                + " WHERE nbp.id=?  AND nsr.product=? AND serialname=?;";
                        PreparedStatement stmt7 = conn.prepareStatement(query7);
                        stmt7.setString(1, batchId);
                        stmt7.setString(2, product);
                        stmt7.setString(3, srName);
                        stmt7.executeUpdate();
                        qty++;
                    }
                }
                String query7 = "update newproductbatch set quantitydue=? where id=?  ";
                PreparedStatement stmt7 = conn.prepareStatement(query7);
                stmt7.setDouble(1, qty);
                stmt7.setString(2, batchId);
                stmt7.executeUpdate();
            } else if (StringUtil.isNullOrEmpty(serialNames) || stkQuantity < batchQuantity) {
                String query8 = "update newproductbatch set quantitydue=? where id=?  ";
                PreparedStatement stmt8 = conn.prepareStatement(query8);
                stmt8.setDouble(1, stkQuantity);
                stmt8.setString(2, batchId);
                stmt8.executeUpdate();
            }
        }


        //  ----------------------for null batches in newserial table-------------------------------------

        String query9 = " SELECT sr.*,p.isSerialForProduct,p.productid from newbatchserial sr INNER JOIN (SELECT npd.serid FROM deliveryorder dlo "
                + " INNER JOIN dodetails dodtl ON dlo.id=dodtl.deliveryorder "
                + " INNER JOIN locationbatchdocumentmapping lcmp  ON lcmp.documentid=dodtl.id"
                + " INNER JOIN (SELECT nw.id AS batchid,ns.id AS serid FROM newproductbatch nw "
                + " INNER JOIN  newbatchserial ns ON nw.product=ns.product AND ns.company=? "
                + " WHERE nw.location IS NULL AND ns.batch is NULL ) npd ON npd.batchid=lcmp.batchmapid "
                + " WHERE dlo.company=?) nw1 ON nw1.serid=sr.id "
                + " LEFT JOIN product p on p.id=sr.product and p.company='" + companyId + "'"
                + " WHERE sr.company=? GROUP BY id ";
        PreparedStatement stmt9 = conn.prepareStatement(query9);
        stmt9.setString(1, companyId);
        stmt9.setString(2, companyId);
        stmt9.setString(3, companyId);
        ResultSet rs9 = stmt9.executeQuery();
        while (rs9.next()) {
            totalCompanyUpdationCnt++;
            String serId = rs9.getString("id");
            String isSerialForProduct = rs9.getString("isSerialForProduct");
            String prodId = rs9.getString("product");
            String productid2 = rs9.getString("productid");
            String query10 = "select * from newproductbatch where product=? and location IS NULL and transactiontype=28 and company=?";
            PreparedStatement stmt10 = conn.prepareStatement(query10);
            stmt10.setString(1, prodId);
            stmt10.setString(2, companyId);

            ResultSet rs10 = stmt10.executeQuery();
            double numOfrows = 0;
            String batchId1 = "";
            while (rs10.next()) {
                numOfrows++;
                batchId1 = rs10.getString("id");
            }
            if (numOfrows == 1 && "T".equals(isSerialForProduct)) {
                String query11 = "update newbatchserial set batch=? where id=?";
                PreparedStatement stmt11 = conn.prepareStatement(query11);
                stmt11.setString(1, batchId1);
                stmt11.setString(2, serId);
                stmt11.executeUpdate();
                out.println("<br><br> Product : " + productid2);
            }
        }

        //-----------------------------------------Sales return------------------------------------------

        String query13 = "SELECT stk.modulerefid,stk.transactionno,stk.store,smdtl.location,smdtl.serialnames,smdtl.batchname,p.isBatchForProduct,"
                + " p.isSerialForProduct,smdtl.id AS stockdtlid,p.productid,p.id AS pid,smdtl.stockmovement "
                + " FROM in_stockmovement stk "
                + " INNER JOIN in_sm_detail smdtl ON smdtl.stockmovement=stk.id "
                + " LEFT JOIN product p ON p.id=stk.product"
                + " WHERE stk.company=? AND stk.transaction_module=9 AND transaction_type=1 AND stk.store NOT IN('402880504ee81348014ee92cb1c16455','402880504ee81348014ee92d56616456')";
        PreparedStatement stmt13 = conn.prepareStatement(query13);
        stmt13.setString(1, companyId);
        ResultSet rs13 = stmt13.executeQuery();
        int r = 0;
        while (rs13.next()) {
            r++;
            String moduleId = rs13.getString("modulerefid");
            String srtransactioNo = rs13.getString("transactionno");
            String srstore = rs13.getString("store");
            String srlocation = rs13.getString("location");
            String srbatchName = rs13.getString("batchname");
            String srserialNames = rs13.getString("serialnames");
            String isBatchforProduct = rs13.getString("isBatchForProduct");
            String isSerial = rs13.getString("isSerialForProduct");
            String stockdtlid = rs13.getString("stockdtlid");
            String productid = rs13.getString("productid");
            String pid = rs13.getString("pid");
            String stockmovement = rs13.getString("stockmovement");

            String condition = "";
            if ("T".equals(isSerial) && "F".equals(isBatchforProduct)) {
                condition += " AND serialnames='" + srserialNames + "'";
            } else if ("T".equals(isBatchforProduct) && "F".equals(isSerial)) {
                condition += " AND batchname='" + srbatchName + "'";
            } else if ("T".equals(isBatchforProduct) && "T".equals(isSerial)) {
                condition += " AND batchname='" + srbatchName + "' AND serialnames='" + srserialNames + "'";
            }

            String query12 = " SELECT DISTINCT smdtl.id AS smdtlid,sm.transactionno,sm.store,smdtl.location,smdtl.batchname,smdtl.serialnames FROM  salesreturn srt "
                    + " INNER JOIN srdetails srdtl ON srdtl.salesreturn=srt.id "
                    + " INNER JOIN dodetails dtl ON dtl.id=srdtl.dodetails"
                    + " INNER JOIN in_stockmovement sm ON sm.modulerefid=dtl.deliveryorder"
                    + " INNER JOIN in_sm_detail smdtl ON smdtl.stockmovement=sm.id"
                    + " WHERE srt.company=? AND sm.transaction_type=2 AND transaction_module=6 AND srt.id=? AND "
                    + " sm.store NOT IN ('402880504ee81348014ee92cb1c16455','402880504ee81348014ee92d56616456') AND sm.product='" + pid + "'" + condition;
            PreparedStatement stmt12 = conn.prepareStatement(query12);
            stmt12.setString(1, companyId);
            stmt12.setString(2, moduleId);
            ResultSet rs12 = stmt12.executeQuery();
            while (rs12.next()) {
                String smdId = rs12.getString("smdtlid");
                String doLocation = rs12.getString("location");
                String doStore = rs12.getString("store");
                String doBatchName = rs12.getString("batchname");
                String doSerialName = rs12.getString("serialnames");
                String srnumber = rs12.getString("transactionno");

                if ((!doLocation.equals(srlocation) || !doStore.equals(srstore)) && ("T".equals(isSerial) || "T".equals(isBatchforProduct))) {
                    out.println("<br><br>No :" + r + " DO-NUMBER : " + srnumber + "  Return Number " + srtransactioNo + " Product : " + productid);
                }

                if ((!doLocation.equals(srlocation) || !doStore.equals(srstore)) && ("T".equals(isSerial) || "T".equals(isBatchforProduct))) {
                    String query14 = "update in_sm_detail set location=? where id=?";//update stock movement
                    PreparedStatement stmt14 = conn.prepareStatement(query14);
                    stmt14.setString(1, doLocation);
                    stmt14.setString(2, stockdtlid);
                    //stmt14.executeUpdate();

                    String query20 = "update in_stockmovement set store=? where id=?";//update stock movement
                    PreparedStatement stmt20 = conn.prepareStatement(query20);
                    stmt20.setString(1, doStore);
                    stmt20.setString(2, stockmovement);
                    //stmt20.executeUpdate();


                    String cond = "";
                    if ("T".equals(isBatchforProduct)) {
                        cond += " AND batch='" + doBatchName + "' ";
                    }
                    String query15 = "select * from in_stock where location=? and store=? and product=? and company=? " + cond;
                    PreparedStatement stmt15 = conn.prepareStatement(query15);
                    stmt15.setString(1, doLocation);
                    stmt15.setString(2, doStore);
                    stmt15.setString(3, pid);
                    stmt15.setString(4, companyId);
                    ResultSet rs15 = stmt15.executeQuery();

                    boolean updated = false;
                    while (rs15.next()) {
                        updated = true;
                        if ("T".equals(isSerial) && !"F".equals(isBatchforProduct)) {
                            String id = rs15.getString("id");
                            String srnm = rs15.getString("serialnames") + "," + srserialNames;
                            String query16 = "update in_stock set quantity=quantity+?,serialnames='" + srnm + "' where id=?";
                            PreparedStatement stmt16 = conn.prepareStatement(query16);
                            stmt16.setDouble(1, 1);
                            stmt16.setString(2, id);
                            // stmt16.executeUpdate();

                            srnm = rs15.getString("serialnames").replace(srserialNames, "");

                            String query17 = "update in_stock set quantity=quantity-1,serialnames=replace(serialnames , '('" + srnm + "')','') "
                                    + " where store=? and location=? and product=? and serialnames like '%'" + srserialNames + "'%'";// update stock
                            PreparedStatement stmt17 = conn.prepareStatement(query17);
                            stmt17.setString(1, srstore);
                            stmt17.setString(2, srlocation);
                            stmt17.setString(3, pid);
                            // stmt17.executeUpdate();


                            String query18 = "UPDATE newbatchserial nsr INNER JOIN newproductbatch npb ON npb.id=nsr.batch SET nsr.quantitydue=1,nsr.lockquantity=0 "
                                    + " WHERE npb.product=? AND npb.location=? AND npb.warehouse=? AND nsr.serialname=?";
                            PreparedStatement stmt18 = conn.prepareStatement(query18);
                            stmt18.setString(1, pid);
                            stmt18.setString(2, doLocation);
                            stmt18.setString(3, doStore);
                            stmt18.setString(4, srserialNames);
                            //  stmt18.executeUpdate();

                            String query19 = "UPDATE newbatchserial nsr INNER JOIN newproductbatch npb ON npb.id=nsr.batch SET nsr.quantitydue=0,nsr.lockquantity=0 "
                                    + " WHERE npb.product=? AND npb.location=? AND npb.warehouse=? AND nsr.serialname=?";
                            PreparedStatement stmt19 = conn.prepareStatement(query19);
                            stmt19.setString(1, pid);
                            stmt19.setString(2, srlocation);
                            stmt19.setString(3, srstore);
                            stmt19.setString(4, srserialNames);
                            //  stmt19.executeUpdate();
                        } else {
                            String id = rs15.getString("id");
                            String bnm = rs15.getString("batchname");
                            String query16 = "update in_stock set quantity=quantity+? where id=?";
                            PreparedStatement stmt16 = conn.prepareStatement(query16);
                            stmt16.setDouble(1, 1);
                            stmt16.setString(2, id);
                            // stmt16.executeUpdate();


                            String query17 = "update in_stock set quantity=quantity-1 "
                                    + " where store=? and location=? and product=? and batchname like '" + srbatchName + "'";// update stock
                            PreparedStatement stmt17 = conn.prepareStatement(query17);
                            stmt17.setString(1, srstore);
                            stmt17.setString(2, srlocation);
                            stmt17.setString(3, pid);
                            // stmt17.executeUpdate();


                            String query18 = "UPDATE  newproductbatch  SET quantitydue=quantitydue+1 "
                                    + " WHERE product=? AND location=? AND warehouse=? AND batchname=?";
                            PreparedStatement stmt18 = conn.prepareStatement(query18);
                            stmt18.setString(1, pid);
                            stmt18.setString(2, doLocation);
                            stmt18.setString(3, doStore);
                            stmt18.setString(4, doBatchName);
                            // stmt18.executeUpdate();

                            String query19 = "UPDATE  newproductbatch  SET quantitydue=quantitydue-1 "
                                    + " WHERE product=? AND location=? AND warehouse=? AND batchname=?";
                            PreparedStatement stmt19 = conn.prepareStatement(query19);
                            stmt19.setString(1, pid);
                            stmt19.setString(2, srlocation);
                            stmt19.setString(3, srstore);
                            stmt19.setString(4, srbatchName);
                            //stmt19.executeUpdate();
                        }
                        break;
                    }


                } else if ((!doLocation.equals(srlocation) && !doStore.equals(srstore)) && ("T".equals(isSerial) || "T".equals(isBatchforProduct))) {
                }
            }
        }





        if (totalCompanyUpdationCnt == 0) {
            //  out.println("<br><br> Script Already Executed for Database " + dbName);
        } else {
            //out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
            out.println("<br><br> Script Executed Sccessfully ");
        }

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>



