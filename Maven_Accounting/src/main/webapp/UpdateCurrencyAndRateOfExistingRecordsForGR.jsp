<%-- 
    Document   : UpdateCurrencyAndRateOfExistingRecordsForGR
    Created on : May 19, 2014, 7:07:15 PM
    Author     : krawler
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@page import="java.sql.*"%>
<%@page import="java.io.*"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%
    Connection conn = null;
    try {
        String serverip = "192.168.0.164";
        String port = "3306";
        String dbName = "accountingsms_06062014_original";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "krawlersqladmin";
        String password = "krawler";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
        
        String companyId = "";
        String companyCurrency = "";
        String query1 = " select companyid,currency from company ";
        PreparedStatement stmt1 = conn.prepareStatement(query1);
        ResultSet rs1 = stmt1.executeQuery();
        while (rs1.next()) {
            companyId = rs1.getString("companyid");
            companyCurrency = rs1.getString("currency");

            String groDetailsID = "";
            String poDetails = "";
            String poCurrency = "";
            double podRate;
            String viDetails = "";
            String viCurrency = "";
            double vidRate;
            Date transactionDate;
            String query2 = " select grod.id,grod.podetails,po.currency,pod.rate,grod.videtails,vi.currency,vid.rate,gro.grorderdate from grodetails grod "
                    + " left join grorder gro on gro.id = grod.grorder "
                    + " left join podetails pod on pod.id = grod.podetails "
                    + " left join purchaseorder po on po.id = pod.purchaseorder "
                    + " left join grdetails vid on vid.id = grod.videtails "
                    + " left join goodsreceipt vi on vi.id = vid.goodsreceipt "
                    + " where grod.company = ? ";
            PreparedStatement stmt2 = conn.prepareStatement(query2);
            stmt2.setString(1, companyId);
            ResultSet rs2 = stmt2.executeQuery();
            while (rs2.next()) {
                groDetailsID = rs2.getString(1);
                poDetails = rs2.getString(2);
                poCurrency = rs2.getString(3);
                podRate = rs2.getDouble(4);
                viDetails = rs2.getString(5);
                viCurrency = rs2.getString(6);
                vidRate = rs2.getDouble(7);
                transactionDate = rs2.getDate(8);

                if (!StringUtil.isNullOrEmpty(poDetails)) {
                    String query3 = " update grodetails "
                            + " set rate = ? "
                            + " where id = ? and company = ? ";
                    PreparedStatement stmt3 = conn.prepareStatement(query3);
                    stmt3.setDouble(1, podRate);
                    stmt3.setString(2, groDetailsID);
                    stmt3.setString(3, companyId);
                    stmt3.executeUpdate();

                    String query4 = " update grorder gro "
                            + " inner join grodetails grod on grod.grorder = gro.id "
                            + " set gro.currency = ? "
                            + " where grod.id = ? and gro.company = ? ";
                    PreparedStatement stmt4 = conn.prepareStatement(query4);
                    stmt4.setString(1, poCurrency);
                    stmt4.setString(2, groDetailsID);
                    stmt4.setString(3, companyId);
                    stmt4.executeUpdate();
                } else if (!StringUtil.isNullOrEmpty(viDetails)) {
                    String query5 = " update grodetails "
                            + " set rate = ? "
                            + " where id = ? and company = ? ";
                    PreparedStatement stmt5 = conn.prepareStatement(query5);
                    stmt5.setDouble(1, vidRate);
                    stmt5.setString(2, groDetailsID);
                    stmt5.setString(3, companyId);
                    stmt5.executeUpdate();

                    String query6 = " update grorder gro "
                            + " inner join grodetails grod on grod.grorder = gro.id "
                            + " set gro.currency = ? "
                            + " where grod.id = ? and gro.company = ? ";
                    PreparedStatement stmt6 = conn.prepareStatement(query6);
                    stmt6.setString(1, viCurrency);
                    stmt6.setString(2, groDetailsID);
                    stmt6.setString(3, companyId);
                    stmt6.executeUpdate();
                } else {
                    double price;
                    String productID = "";
                    String query7 = " select pl.price,p.id from grodetails grod "
                            + " inner join product p on p.id = grod.product "
                            + " left join pricelist pl on pl.product = p.id and pl.carryin = 'F' and pl.applyDate <= ? and pl.affecteduser ='-1' "
                            + " where grod.company = ? and grod.id = ? ";
                    PreparedStatement stmt7 = conn.prepareStatement(query7);
                    stmt7.setDate(1, transactionDate);
                    stmt7.setString(2, companyId);
                    stmt7.setString(3, groDetailsID);
                    ResultSet rs7 = stmt7.executeQuery();
                    while (rs7.next()) {
                        price = (rs7.getObject("price") != null)? rs7.getDouble("price") : 0;
                        productID = rs7.getString("id");
                        String query8 = " update grodetails "
                                + " set rate = ? "
                                + " where  id = ? and product = ? and company = ? ";
                        PreparedStatement stmt8 = conn.prepareStatement(query8);
                        stmt8.setDouble(1, price);
                        stmt8.setString(2, groDetailsID);
                        stmt8.setString(3, productID);
                        stmt8.setString(4, companyId);
                        stmt8.executeUpdate();

                        String query9 = " update grorder gro "
                                + " inner join grodetails grod on grod.grorder = gro.id "
                                + " set gro.currency = ? "
                                + " where grod.id = ? and grod.product = ? and gro.company = ? ";
                        PreparedStatement stmt9 = conn.prepareStatement(query9);
                        stmt9.setString(1, companyCurrency);
                        stmt9.setString(2, groDetailsID);
                        stmt9.setString(3, productID);
                        stmt9.setString(4, companyId);
                        stmt9.executeUpdate();
                    }
                }
            }
        }

        out.println(" Records updated successfully.");
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    }
%>
