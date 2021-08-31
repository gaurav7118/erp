<%-- 
    Document   : UpdateCurrencyAndRateOfExistingRecordsForSR
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

            String srDetailsID = "";
            String doDetails = "";
            String doCurrency = "";
            double dodRate;
            String ciDetails = "";
            String ciCurrency = "";
            double cidRate;
            Date transactionDate;
            String query2 = " select srd.id,srd.dodetails,do.currency,dod.rate,srd.cidetails,ci.currency,cid.rate,sr.orderdate from srdetails srd "
                    + " left join salesreturn sr on sr.id = srd.salesreturn "
                    + " left join dodetails dod on dod.id = srd.dodetails "
                    + " left join deliveryorder do on do.id = dod.deliveryorder "
                    + " left join invoicedetails cid on cid.id = srd.cidetails "
                    + " left join invoice ci on ci.id = cid.invoice "
                    + " where srd.company = ? ";
            PreparedStatement stmt2 = conn.prepareStatement(query2);
            stmt2.setString(1, companyId);
            ResultSet rs2 = stmt2.executeQuery();
            while (rs2.next()) {
                srDetailsID = rs2.getString(1);
                doDetails = rs2.getString(2);
                doCurrency = rs2.getString(3);
                dodRate = rs2.getDouble(4);
                ciDetails = rs2.getString(5);
                ciCurrency = rs2.getString(6);
                cidRate = rs2.getDouble(7);
                transactionDate = rs2.getDate(8);

                if (!StringUtil.isNullOrEmpty(doDetails)) {
                    String query3 = " update srdetails "
                            + " set rate = ? "
                            + " where id = ? and company = ? ";
                    PreparedStatement stmt3 = conn.prepareStatement(query3);
                    stmt3.setDouble(1, dodRate);
                    stmt3.setString(2, srDetailsID);
                    stmt3.setString(3, companyId);
                    stmt3.executeUpdate();

                    String query4 = " update salesreturn sr "
                            + " inner join srdetails srd on srd.salesreturn = sr.id "
                            + " set sr.currency = ? "
                            + " where srd.id = ? and sr.company = ? ";
                    PreparedStatement stmt4 = conn.prepareStatement(query4);
                    stmt4.setString(1, doCurrency);
                    stmt4.setString(2, srDetailsID);
                    stmt4.setString(3, companyId);
                    stmt4.executeUpdate();
                } else if (!StringUtil.isNullOrEmpty(ciDetails)) {
                    String query5 = " update srdetails "
                            + " set rate = ? "
                            + " where id = ? and company = ? ";
                    PreparedStatement stmt5 = conn.prepareStatement(query5);
                    stmt5.setDouble(1, cidRate);
                    stmt5.setString(2, srDetailsID);
                    stmt5.setString(3, companyId);
                    stmt5.executeUpdate();

                    String query6 = " update salesreturn sr "
                            + " inner join srdetails srd on srd.salesreturn = sr.id "
                            + " set sr.currency = ? "
                            + " where srd.id = ? and sr.company = ? ";
                    PreparedStatement stmt6 = conn.prepareStatement(query6);
                    stmt6.setString(1, ciCurrency);
                    stmt6.setString(2, srDetailsID);
                    stmt6.setString(3, companyId);
                    stmt6.executeUpdate();
                } else {
                    double price;
                    String productID = "";
                    String query7 = " select pl.price,p.id from srdetails srd "
                            + " inner join product p on p.id = srd.product "
                            + " left join pricelist pl on pl.product = p.id and pl.carryin = 'F' and pl.applyDate <= ? and pl.affecteduser ='-1' "
                            + " where srd.company = ? and srd.id = ? ";
                    PreparedStatement stmt7 = conn.prepareStatement(query7);
                    stmt7.setDate(1, transactionDate);
                    stmt7.setString(2, companyId);
                    stmt7.setString(3, srDetailsID);
                    ResultSet rs7 = stmt7.executeQuery();
                    while (rs7.next()) {
                        price = (rs7.getObject("price") != null)? rs7.getDouble("price") : 0;
                        productID = rs7.getString("id");
                        String query8 = " update srdetails "
                                + " set rate = ? "
                                + " where  id = ? and product = ? and company = ? ";
                        PreparedStatement stmt8 = conn.prepareStatement(query8);
                        stmt8.setDouble(1, price);
                        stmt8.setString(2, srDetailsID);
                        stmt8.setString(3, productID);
                        stmt8.setString(4, companyId);
                        stmt8.executeUpdate();

                        String query9 = " update salesreturn sr "
                                + " inner join srdetails srd on srd.salesreturn = sr.id "
                                + " set sr.currency = ? "
                                + " where srd.id = ? and srd.product = ? and sr.company = ? ";
                        PreparedStatement stmt9 = conn.prepareStatement(query9);
                        stmt9.setString(1, companyCurrency);
                        stmt9.setString(2, srDetailsID);
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
