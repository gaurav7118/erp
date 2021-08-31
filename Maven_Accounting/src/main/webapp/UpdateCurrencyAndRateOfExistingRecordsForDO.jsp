<%-- 
    Document   : UpdateCurrencyAndRateOfExistingRecordsForDO
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

            String doDetailsID = "";
            String soDetails = "";
            String soCurrency = "";
            double sodRate;
            String ciDetails = "";
            String ciCurrency = "";
            double cidRate;
            Date transactionDate;
            String query2 = " select dod.id,dod.sodetails,so.currency,sod.rate,dod.cidetails,ci.currency,cid.rate,do.orderdate from dodetails dod "
                    + " left join deliveryorder do on do.id = dod.deliveryorder "
                    + " left join sodetails sod on sod.id = dod.sodetails "
                    + " left join salesorder so on so.id = sod.salesorder "
                    + " left join invoicedetails cid on cid.id = dod.cidetails "
                    + " left join invoice ci on ci.id = cid.invoice "
                    + " where dod.company = ? ";
            PreparedStatement stmt2 = conn.prepareStatement(query2);
            stmt2.setString(1, companyId);
            ResultSet rs2 = stmt2.executeQuery();
            while (rs2.next()) {
                doDetailsID = rs2.getString(1);
                soDetails = rs2.getString(2);
                soCurrency = rs2.getString(3);
                sodRate = rs2.getDouble(4);
                ciDetails = rs2.getString(5);
                ciCurrency = rs2.getString(6);
                cidRate = rs2.getDouble(7);
                transactionDate = rs2.getDate(8);

                if (!StringUtil.isNullOrEmpty(soDetails)) {
                    String query3 = " update dodetails "
                            + " set rate = ? "
                            + " where id = ? and company = ? ";
                    PreparedStatement stmt3 = conn.prepareStatement(query3);
                    stmt3.setDouble(1, sodRate);
                    stmt3.setString(2, doDetailsID);
                    stmt3.setString(3, companyId);
                    stmt3.executeUpdate();

                    String query4 = " update deliveryorder do "
                            + " inner join dodetails dod on dod.deliveryorder = do.id "
                            + " set do.currency = ? "
                            + " where dod.id = ? and do.company = ? ";
                    PreparedStatement stmt4 = conn.prepareStatement(query4);
                    stmt4.setString(1, soCurrency);
                    stmt4.setString(2, doDetailsID);
                    stmt4.setString(3, companyId);
                    stmt4.executeUpdate();
                } else if (!StringUtil.isNullOrEmpty(ciDetails)) {
                    String query5 = " update dodetails "
                            + " set rate = ? "
                            + " where id = ? and company = ? ";
                    PreparedStatement stmt5 = conn.prepareStatement(query5);
                    stmt5.setDouble(1, cidRate);
                    stmt5.setString(2, doDetailsID);
                    stmt5.setString(3, companyId);
                    stmt5.executeUpdate();

                    String query6 = " update deliveryorder do "
                            + " inner join dodetails dod on dod.deliveryorder = do.id "
                            + " set do.currency = ? "
                            + " where dod.id = ? and do.company = ? ";
                    PreparedStatement stmt6 = conn.prepareStatement(query6);
                    stmt6.setString(1, ciCurrency);
                    stmt6.setString(2, doDetailsID);
                    stmt6.setString(3, companyId);
                    stmt6.executeUpdate();
                } else {
                    double price;
                    String productID = "";
                    String query7 = " select pl.price,p.id from dodetails dod "
                            + " inner join product p on p.id = dod.product "
                            + " left join pricelist pl on pl.product = p.id and pl.carryin = 'F' and pl.applyDate <= ? and pl.affecteduser ='-1' "
                            + " where dod.company = ? and dod.id = ? ";
                    PreparedStatement stmt7 = conn.prepareStatement(query7);
                    stmt7.setDate(1, transactionDate);
                    stmt7.setString(2, companyId);
                    stmt7.setString(3, doDetailsID);
                    ResultSet rs7 = stmt7.executeQuery();
                    while (rs7.next()) {
                        price = (rs7.getObject("price") != null)? rs7.getDouble("price") : 0;
                        productID = rs7.getString("id");
                        String query8 = " update dodetails "
                                + " set rate = ? "
                                + " where  id = ? and product = ? and company = ? ";
                        PreparedStatement stmt8 = conn.prepareStatement(query8);
                        stmt8.setDouble(1, price);
                        stmt8.setString(2, doDetailsID);
                        stmt8.setString(3, productID);
                        stmt8.setString(4, companyId);
                        stmt8.executeUpdate();

                        String query9 = " update deliveryorder do "
                                + " inner join dodetails dod on dod.deliveryorder = do.id "
                                + " set do.currency = ? "
                                + " where dod.id = ? and dod.product = ? and do.company = ? ";
                        PreparedStatement stmt9 = conn.prepareStatement(query9);
                        stmt9.setString(1, companyCurrency);
                        stmt9.setString(2, doDetailsID);
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
