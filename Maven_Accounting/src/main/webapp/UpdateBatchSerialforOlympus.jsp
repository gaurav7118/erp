
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
        //String serverip = "localhost";
        //String port = "3306";
        //String dbName = "staging";
        //String userName = "krawlersqladmin";//"krawlersqladmin";
        //String password = "Krawler[X]"; //"krawler"
        //String subdomain = "olympus3";

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

        String query3 = " SELECT p.id as pid,p.productid,sk.store,sk.location,SUM(quantity) as quantity,sk.batchname,sk.serialnames,"
                + " p.isBatchForProduct,p.isSerialForProduct FROM in_stock sk "
                + " INNER JOIN product p ON p.id=sk.product "
                + " WHERE quantity>0 AND sk.company=? AND p.id NOT IN (SELECT p.id FROM (SELECT * FROM in_stock WHERE company='" + companyId + "' "
                + " GROUP BY store,location,product HAVING(COUNT(*))>1 ORDER BY product ) as tb1 INNER JOIN product p ON p.id=tb1.product "
                + " WHERE p.isBatchForProduct='F' AND p.isSerialForProduct='F') GROUP BY store,location,product,batchname order by p.productid ";

        PreparedStatement stmt3 = conn.prepareStatement(query3);
        stmt3.setString(1, companyId);
        ResultSet rs3 = stmt3.executeQuery();

        while (rs3.next()) {
            String productId = rs3.getString("productid");
            // out.println("<br><br> Product : " + productId);
            if ("WB50402W".equals(productId)) {
                System.out.println("");
            }
            String pid = rs3.getString("pid");
            String store = rs3.getString("store");
            String location = rs3.getString("location");
            String batchName = rs3.getString("batchname");
            String serialnames = rs3.getString("serialnames");
            String isBatchForProduct = rs3.getString("isBatchForProduct");
            String isSerialForProduct = rs3.getString("isSerialForProduct");
            double quantity = rs3.getDouble("quantity");
            if ("402880504ee81348014ee92cb1c16455".equals(store) || "402880504ee81348014ee92d56616456".equals(store)) {
                continue;
            }

            if ("F".equals(isSerialForProduct) && "F".equals(isBatchForProduct)) {

                String qry1 = "SELECT SUM(quantitydue) AS quantity,id FROM newproductbatch WHERE warehouse=? AND location=?  AND product=? AND company=? ";
                PreparedStatement stmt4 = conn.prepareStatement(qry1);
                stmt4.setString(1, store);
                stmt4.setString(2, location);
                stmt4.setString(3, pid);
                stmt4.setString(4, companyId);
                ResultSet rs4 = stmt4.executeQuery();

                double qty = 0;
                while (rs4.next()) {
                    qty = rs4.getDouble("quantity");
                    if (quantity != qty) {
                        String upquery1 = "UPDATE newproductbatch SET quantitydue=? WHERE id=?";
                        PreparedStatement stmt = conn.prepareStatement(upquery1);
                        stmt.setDouble(1, quantity);
                        stmt.setString(2, rs4.getString("id"));
                        stmt.executeUpdate();
                        out.println("<br><br> Product : " + productId);
                    }
                }


            } else if ("F".equals(isSerialForProduct) && "T".equals(isBatchForProduct)) {
                String qry2 = "SELECT SUM(quantitydue) AS quantity,id FROM newproductbatch WHERE warehouse=? AND location=?  AND product=? AND company=? AND batchname=? ";
                PreparedStatement stmt5 = conn.prepareStatement(qry2);
                stmt5.setString(1, store);
                stmt5.setString(2, location);
                stmt5.setString(3, pid);
                stmt5.setString(4, companyId);
                stmt5.setString(5, batchName);
                ResultSet rs5 = stmt5.executeQuery();

                double qty = 0;
                String id = "";
                while (rs5.next()) {
                    qty = rs5.getDouble("quantity");
                    id = rs5.getString("id");
                }
                if (quantity != qty) {

                    String upquery1 = "UPDATE newproductbatch SET quantitydue=? WHERE id=?";
                    PreparedStatement stmt6 = conn.prepareStatement(upquery1);
                    stmt6.setDouble(1, quantity);
                    stmt6.setString(2, id);
                    stmt6.executeUpdate();
                    out.println("<br><br> Product : " + productId + " Batch : " + batchName);
                }
            } else if ("T".equals(isSerialForProduct) && "F".equals(isBatchForProduct)) {
                if (!StringUtil.isNullOrEmpty(serialnames)) {
                    String[] srNm = serialnames.split(",");
                    if (srNm.length > 0) {
                        for (int i = 0; i < srNm.length; i++) {
                            String sirialnm = srNm[i];

                            String qry3 = "SELECT nsr.id AS id,SUM(nsr.quantitydue) AS quantity FROM newproductbatch nb "
                                    + " INNER JOIN newbatchserial nsr ON nb.id=nsr.batch "
                                    + " WHERE nb.warehouse=? AND nb.location=? AND nb.product=? AND nb.company=? AND nsr.serialname=?";
                            PreparedStatement stmt7 = conn.prepareStatement(qry3);
                            stmt7.setString(1, store);
                            stmt7.setString(2, location);
                            stmt7.setString(3, pid);
                            stmt7.setString(4, companyId);
                            stmt7.setString(5, sirialnm);
                            ResultSet rs7 = stmt7.executeQuery();
                            while (rs7.next()) {
                                if (rs7.getDouble("quantity") <= 0) {
                                    String upquery2 = "UPDATE newbatchserial SET quantitydue=1,lockquantity=0 WHERE id=?";
                                    PreparedStatement stmt8 = conn.prepareStatement(upquery2);
                                    stmt8.setString(1, rs7.getString("id"));
                                    stmt8.executeUpdate();
                                    out.println("<br><br> Product : " + productId + " Serial : " + sirialnm);
                                }
                            }
                        }
                    }
                }
            } else if ("T".equals(isSerialForProduct) && "T".equals(isBatchForProduct)) {
                if (!StringUtil.isNullOrEmpty(serialnames)) {
                    String[] srNm1 = serialnames.split(",");
                    if (srNm1.length > 0) {
                        for (int i = 0; i < srNm1.length; i++) {
                            String sirialnm1 = srNm1[i];

                            String qry4 = "SELECT nsr.id AS id,SUM(nsr.quantitydue) AS quantity FROM newproductbatch nb "
                                    + " INNER JOIN newbatchserial nsr ON nb.id=nsr.batch "
                                    + " WHERE nb.warehouse=? AND nb.location=? AND nb.product=? AND nb.company=? AND nsr.serialname=? AND nb.batchname=? ";
                            PreparedStatement stmt9 = conn.prepareStatement(qry4);
                            stmt9.setString(1, store);
                            stmt9.setString(2, location);
                            stmt9.setString(3, pid);
                            stmt9.setString(4, companyId);
                            stmt9.setString(5, sirialnm1);
                            stmt9.setString(6, batchName);
                            ResultSet rs9 = stmt9.executeQuery();
                            while (rs9.next()) {
                                if (rs9.getDouble("quantity") == 0) {
                                    String upquery3 = "UPDATE newbatchserial SET quantitydue=1,lockquantity=0 WHERE id=?";
                                    PreparedStatement stmt10 = conn.prepareStatement(upquery3);
                                    stmt10.setString(1, rs9.getString("id"));
                                    stmt10.executeUpdate();
                                    out.println("<br><br> Product : " + productId + " Batch : " + batchName + " Serial : " + sirialnm1);
                                }
                            }
                        }
                    }
                }
            }
        }
///////////////////////////////////////// release locked quantity /////////////////////////////////////////
        out.println("<br><br> release locked quantity ");

        String query4 = "SELECT nsr.id,nsr.serialname,p.productid FROM newbatchserial nsr "
                + " INNER JOIN newproductbatch nb ON nb.id=nsr.batch AND nb.product=nsr.product "
                + " INNER JOIN serialdocumentmapping srmp ON srmp.serialid=nsr.id "
                + " INNER JOIN product p ON p.id=nsr.product "
                + " WHERE nsr.lockquantity>0 AND nb.company='" + companyId + "' AND nb.location IS NOT NULL AND nsr.id NOT IN (SELECT lmp.serialid  FROM salesorder sor "
                + " INNER JOIN sodetails sodtl ON sodtl.salesorder=sor.id "
                + " INNER JOIN serialdocumentmapping lmp ON lmp.documentid=sodtl.id "
                + " WHERE sor.company=? AND sor.deleteflag='F' AND sor.isopen='T' AND sor.isconsignment='T' ) "
                + " GROUP BY nsr.id ";

        PreparedStatement stmt11 = conn.prepareStatement(query4);
        stmt11.setString(1, companyId);
        ResultSet rs11 = stmt11.executeQuery();
        while (rs11.next()) {
            String srId = rs11.getString("id");
            String serialname = rs11.getString("serialname");
            String productid = rs11.getString("productid");

            String upquery4 = "UPDATE newbatchserial SET lockquantity=0 WHERE id=?";
            PreparedStatement stmt12 = conn.prepareStatement(upquery4);
            stmt12.setString(1, srId);
            stmt12.executeUpdate();
            out.println("<br><br> Product : " + productid + " Serial : " + serialname);
        }

        String query5 = " SELECT nb.id,nb.batchname,p.productid,nb.lockquantity FROM newproductbatch nb "
                + " INNER JOIN locationbatchdocumentmapping lmp ON lmp.batchmapid=nb.id "
                + " INNER JOIN product p ON p.id=nb.product "
                + " WHERE nb.lockquantity>0 AND nb.company='" + companyId + "' AND nb.location IS NOT NULL AND nb.id NOT IN (SELECT lmp1.batchmapid  FROM salesorder sor  "
                + " INNER JOIN sodetails sodtl ON sodtl.salesorder=sor.id "
                + " INNER JOIN locationbatchdocumentmapping lmp1 ON lmp1.documentid=sodtl.id "
                + " WHERE sor.company=? AND sor.deleteflag='F' AND sor.isopen='T' AND sor.isconsignment='T') "
                + " GROUP BY nb.id ";

        PreparedStatement stmt13 = conn.prepareStatement(query5);
        stmt13.setString(1, companyId);
        ResultSet rs13 = stmt13.executeQuery();
        while (rs13.next()) {
            String batchid = rs13.getString("id");
            String batchname = rs13.getString("batchname");
            String productid = rs13.getString("productid");

            String upquery5 = "UPDATE newproductbatch SET lockquantity=0 WHERE id=?";
            PreparedStatement stmt14 = conn.prepareStatement(upquery5);
            stmt14.setString(1, batchid);
            stmt14.executeUpdate();
            out.println("<br><br> Product : " + productid + " batchname : " + batchname);
        }

        ///////////////////////  update batch which are wrong batch selected in DO ///////////////////////////////

        String query6 = "SELECT lcmp.documentid,lcmp.batchmapid,nb.batchname,dol.donumber,p.productid,p.id AS pid FROM locationbatchdocumentmapping lcmp  "
                + " INNER JOIN dodetails dtl ON dtl.id=lcmp.documentid "
                + " INNER JOIN deliveryorder dol ON dol.id=dtl.deliveryorder "
                + " INNER JOIN newproductbatch nb ON nb.id=lcmp.batchmapid "
                + " INNER JOIN product p ON p.id=nb.product "
                + " WHERE nb.company=? AND lcmp.transactiontype=27 AND dol.isdoclosed='F' AND dol.isconsignment='T' AND dtl.id NOT IN ((SELECT  dtl.id FROM deliveryorder dlo "
                + " INNER JOIN  dodetails dtl ON dtl.deliveryorder=dlo.id "
                + " INNER JOIN srdetails srdl ON srdl.dodetails=dtl.id "
                + " WHERE dlo.company=? GROUP BY donumber)) AND p.isBatchForProduct='T' ORDER BY lcmp.documentid ";


        PreparedStatement stmt15 = conn.prepareStatement(query6);
        stmt15.setString(1, companyId);
        stmt15.setString(2, companyId);
        ResultSet rs15 = stmt15.executeQuery();
        while (rs15.next()) {
            String documnetId = rs15.getString("documentid");
            String batchmapId = rs15.getString("batchmapid");
            String batchName = rs15.getString("batchname");
            String pId = rs15.getString("pid");
            String productid = rs15.getString("productid");
            String donumber = rs15.getString("donumber");

            String query7 = " SELECT lcmp.documentid,lcmp.batchmapid,nb.batchname FROM locationbatchdocumentmapping lcmp  "
                    + " INNER JOIN newproductbatch nb ON nb.id=lcmp.batchmapid  "
                    + " WHERE nb.company=? AND lcmp.transactiontype=28 AND lcmp.documentid=? AND nb.batchname=? AND nb.product=? ";

            boolean isExist = false;
            PreparedStatement stmt16 = conn.prepareStatement(query7);
            stmt16.setString(1, companyId);
            stmt16.setString(2, documnetId);
            stmt16.setString(3, batchName);
            stmt16.setString(4, pId);

            ResultSet rs16 = stmt16.executeQuery();
            while (rs16.next()) {
                isExist = true;
                break;
            }
            if (isExist == false && !StringUtil.isNullOrEmpty(documnetId) && !StringUtil.isNullOrEmpty(batchName)) {
                String query8 = " SELECT count(*) AS cnt  FROM locationbatchdocumentmapping  "
                        + " WHERE transactiontype=28 AND documentid=?  ";

                PreparedStatement stmt18 = conn.prepareStatement(query8);
                stmt18.setString(1, documnetId);
                ResultSet rs18 = stmt18.executeQuery();

                while (rs18.next()) {
                    int cnt = rs18.getInt("cnt");
                    if (cnt == 1) {
                        String updatequry = " UPDATE locationbatchdocumentmapping SET batchmapid=(SELECT DISTINCT (id) FROM newproductbatch "
                                + " WHERE batchname=? AND product=? AND location IS NULL AND warehouse='297b3f5e-5915-4b79-b70b-61a8ee4d7fb7') "
                                + " WHERE documentid=?  AND transactiontype=28 ";

                        PreparedStatement stmt17 = conn.prepareStatement(updatequry);
                        stmt17.setString(1, batchName);
                        stmt17.setString(2, pId);
                        stmt17.setString(3, documnetId);
                        stmt17.executeUpdate();

                        out.println("<br><br> DONUMBER : " + donumber + ", Product : " + productid + ", batchname : " + batchName);
                    } else {
                        String query9 = " SELECT documentid,batchmapid,nb.batchname  FROM locationbatchdocumentmapping  lm "
                                + "  INNER JOIN newproductbatch nb ON nb.id=lm.batchmapid "
                                + "  WHERE lm.transactiontype=28 AND lm.documentid=? ";

                        PreparedStatement stmt19 = conn.prepareStatement(query9);
                        stmt19.setString(1, documnetId);
                        ResultSet rs19 = stmt19.executeQuery();
                        while (rs19.next()) {
                            String dID = rs19.getString("documentid");
                            String bID = rs19.getString("batchmapid");
                            String bNm = rs19.getString("batchname");

                            String query10 = " SELECT COUNT(*) AS cnt1  FROM locationbatchdocumentmapping  lm "
                                    + "  INNER JOIN newproductbatch nb ON nb.id=lm.batchmapid "
                                    + "  WHERE lm.transactiontype=27 AND lm.documentid=? AND nb.batchname=?";

                            PreparedStatement stmt20 = conn.prepareStatement(query10);
                            stmt20.setString(1, dID);
                            stmt20.setString(2, bNm);
                            ResultSet rs20 = stmt20.executeQuery();
                            int cnt1 = 0;
                            while (rs20.next()) {
                                cnt1 = rs20.getInt("cnt1");
                            }
                            if (cnt1 == 0) {
                                String updatequry2 = " UPDATE locationbatchdocumentmapping SET batchmapid=(SELECT DISTINCT (id) FROM newproductbatch "
                                        + " WHERE batchname=? AND product=? AND location IS NULL AND warehouse='297b3f5e-5915-4b79-b70b-61a8ee4d7fb7') "
                                        + " WHERE documentid=?  AND transactiontype=28 AND batchmapid=? ";

                                PreparedStatement stmt21 = conn.prepareStatement(updatequry2);
                                stmt21.setString(1, batchName);
                                stmt21.setString(2, pId);
                                stmt21.setString(3, documnetId);
                                stmt21.setString(4, bID);
                                stmt21.executeUpdate();

                                out.println("<br><br> DONUMBER : " + donumber + ", Product : " + productid + ", batchname : " + batchName);
                            }
                        }
                    }
                }
            }
        }

        out.println("<br><br> Script Executed Sccessfully ");

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>



