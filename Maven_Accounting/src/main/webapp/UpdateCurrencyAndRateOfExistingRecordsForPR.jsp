<%-- 
    Document   : UpdateCurrencyAndRateOfExistingRecordsForPR
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

            String prDetailsID = "";
            String grDetails = "";
            String groCurrency = "";
            double grodRate;
            String viDetails = "";
            String viCurrency = "";
            double vidRate;
            Date transactionDate;
            String query2 = " select prd.id,prd.grdetails,gro.currency,grod.rate,prd.videtails,vi.currency,vid.rate,pr.orderdate from prdetails prd "
                    + " left join purchasereturn pr on pr.id = prd.purchasereturn "
                    + " left join grodetails grod on grod.id = prd.grdetails "
                    + " left join grorder gro on gro.id = grod.grorder "
                    + " left join grdetails vid on vid.id = prd.videtails "
                    + " left join goodsreceipt vi on vi.id = vid.goodsreceipt "
                    + " where prd.company = ? ";
            PreparedStatement stmt2 = conn.prepareStatement(query2);
            stmt2.setString(1, companyId);
            ResultSet rs2 = stmt2.executeQuery();
            while (rs2.next()) {
                prDetailsID = rs2.getString(1);
                grDetails = rs2.getString(2);
                groCurrency = rs2.getString(3);
                grodRate = rs2.getDouble(4);
                viDetails = rs2.getString(5);
                viCurrency = rs2.getString(6);
                vidRate = rs2.getDouble(7);
                transactionDate = rs2.getDate(8);

                if (!StringUtil.isNullOrEmpty(grDetails)) {
                    String query3 = " update prdetails "
                            + " set rate = ? "
                            + " where id = ? and company = ? ";
                    PreparedStatement stmt3 = conn.prepareStatement(query3);
                    stmt3.setDouble(1, grodRate);
                    stmt3.setString(2, prDetailsID);
                    stmt3.setString(3, companyId);
                    stmt3.executeUpdate();

                    String query4 = " update purchasereturn pr "
                            + " inner join prdetails prd on prd.purchasereturn = pr.id "
                            + " set pr.currency = ? "
                            + " where prd.id = ? and pr.company = ? ";
                    PreparedStatement stmt4 = conn.prepareStatement(query4);
                    stmt4.setString(1, groCurrency);
                    stmt4.setString(2, prDetailsID);
                    stmt4.setString(3, companyId);
                    stmt4.executeUpdate();
                } else if (!StringUtil.isNullOrEmpty(viDetails)) {
                    String query5 = " update prdetails "
                            + " set rate = ? "
                            + " where id = ? and company = ? ";
                    PreparedStatement stmt5 = conn.prepareStatement(query5);
                    stmt5.setDouble(1, vidRate);
                    stmt5.setString(2, prDetailsID);
                    stmt5.setString(3, companyId);
                    stmt5.executeUpdate();

                    String query6 = " update purchasereturn pr "
                            + " inner join prdetails prd on prd.purchasereturn = pr.id "
                            + " set pr.currency = ? "
                            + " where prd.id = ? and pr.company = ? ";
                    PreparedStatement stmt6 = conn.prepareStatement(query6);
                    stmt6.setString(1, viCurrency);
                    stmt6.setString(2, prDetailsID);
                    stmt6.setString(3, companyId);
                    stmt6.executeUpdate();
                } else {
                    double price;
                    String productID = "";
                    String query7 = " select pl.price,p.id from prdetails prd "
                            + " inner join product p on p.id = prd.product "
                            + " left join pricelist pl on pl.product = p.id and pl.carryin = 'F' and pl.applyDate <= ? and pl.affecteduser ='-1' "
                            + " where prd.company = ? and prd.id = ? ";
                    PreparedStatement stmt7 = conn.prepareStatement(query7);
                    stmt7.setDate(1, transactionDate);
                    stmt7.setString(2, companyId);
                    stmt7.setString(3, prDetailsID);
                    ResultSet rs7 = stmt7.executeQuery();
                    while (rs7.next()) {
                        price = (rs7.getObject("price") != null)? rs7.getDouble("price") : 0;
                        productID = rs7.getString("id");
                        String query8 = " update prdetails "
                                + " set rate = ? "
                                + " where  id = ? and product = ? and company = ? ";
                        PreparedStatement stmt8 = conn.prepareStatement(query8);
                        stmt8.setDouble(1, price);
                        stmt8.setString(2, prDetailsID);
                        stmt8.setString(3, productID);
                        stmt8.setString(4, companyId);
                        stmt8.executeUpdate();

                        String query9 = " update purchasereturn pr "
                                + " inner join prdetails prd on prd.purchasereturn = pr.id "
                                + " set pr.currency = ? "
                                + " where prd.id = ? and prd.product = ? and pr.company = ? ";
                        PreparedStatement stmt9 = conn.prepareStatement(query9);
                        stmt9.setString(1, companyCurrency);
                        stmt9.setString(2, prDetailsID);
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
