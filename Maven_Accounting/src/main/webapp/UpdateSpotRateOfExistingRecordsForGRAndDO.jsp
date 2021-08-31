<%-- 
    Document   : SpotRate
    Created on : Sep 8, 2015, 4:52:42 PM
    Author     : krawler
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.io.*"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<!DOCTYPE html>
<%
    Connection conn = null;
    try {

        String port = "3306";
        String serverip = request.getParameter("serverip") != null ? request.getParameter("serverip") : "";
        String dbName = request.getParameter("dbname") != null ? request.getParameter("dbname") : "";
        String userName = request.getParameter("username") != null ? request.getParameter("username") : "";
        String password = request.getParameter("password") != null ? request.getParameter("password") : "";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);

        if (StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(password)) {
            out.println("Parameter missing from parameters=> [serverip,dbname,username,password] ");
        } else {
            String connectDB = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectDB, userName, password);

            String companyId = "";
            String fromCurrency = "";
            String toCurrency = "";
            String mainQuery = "";
            String OrderDate = "";
            String orderid = "";
            String fromDate = "";
            
            //For GR
            String query1 = "select gr.id,gr.company,gr.grorderdate,c.currency,gr.currency from grorder gr "
                    + "inner join company c  on gr.company = c.companyid where gr.currency <> c.currency";

            PreparedStatement stmt1 = conn.prepareStatement(query1);
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                orderid = rs1.getString(1);
                companyId = rs1.getString(2);
                OrderDate = rs1.getString(3);
                fromCurrency = rs1.getString(4);
                toCurrency = rs1.getString(5);
                fromDate = rs1.getString(5);
                String query2 = "select count(distinct goodsreceipt) from grdetails where id in (select videtails from grodetails "
                        + "where grorder = ? and company = ? )";
                PreparedStatement stmt2 = conn.prepareStatement(query2);
                PreparedStatement stmt3 = conn.prepareStatement(query2);
                stmt2.setString(1, orderid);
                stmt2.setString(2, companyId);
                ResultSet rs2 = stmt2.executeQuery();

                if (rs2.next()) {
                    if (StringUtil.getInteger(rs2.getString(1)) == 1) { //Single Invoice Linking case, setting external currency rate of invoice.
                        mainQuery = "update grorder gro set gro.externalcurrencyrate = (select je.externalcurrencyrate  from journalentry je "
                                + "inner join goodsreceipt gr on  je.id = gr.journalentry inner join grdetails grd on grd.goodsreceipt = gr.id "
                                + " inner join grodetails grod on grod.videtails = grd.id where gr.company = ? and grod.grorder=? LIMIT 1)"
                                + " where gro.company= ? and gro.id = ? ";
                        stmt3 = conn.prepareStatement(mainQuery);
                        stmt3.setString(1, companyId);
                        stmt3.setString(2, orderid);
                        stmt3.setString(3, companyId);
                        stmt3.setString(4, orderid);
                        stmt3.executeUpdate();
                    } else { //Multiple linking or No linking case, setting external currency rate near to GRN transaction date
                        mainQuery = "update grorder gro set gro.externalcurrencyrate = (select erd.exchangerate from exchangeratedetails erd "
                                + "inner join exchangerate er on er.id = erd.exchangeratelink where erd.company= ? and er.fromcurrency= ? and "
                                + "er.tocurrency=? ORDER BY ABS(DATEDIFF( ?, erd.applydate)) LIMIT 1) where gro.company = ? and gro.id = ? ";
                        stmt3 = conn.prepareStatement(mainQuery);
                        stmt3.setString(1, companyId);
                        stmt3.setString(2, fromCurrency);
                        stmt3.setString(3, toCurrency);
                        stmt3.setString(4, OrderDate);//GRN transaction date
                        stmt3.setString(5, companyId);
                        stmt3.setString(6, orderid);
                        stmt3.executeUpdate();

                    }
                }
            }


            //For DO
            String query7 = "select do.id,do.company,do.orderdate,c.currency,do.currency from deliveryorder do "
                    + "inner join company c  on do.company = c.companyid where do.currency <> c.currency";

            PreparedStatement stmt7 = conn.prepareStatement(query7);
            ResultSet rs7 = stmt7.executeQuery();
            while (rs7.next()) {
                orderid = rs7.getString(1);
                companyId = rs7.getString(2);
                OrderDate = rs7.getString(3);
                fromCurrency = rs7.getString(4);
                toCurrency = rs7.getString(5);
                fromDate = rs7.getString(5);
                String query8 = "select count(distinct invoice) from invoicedetails where id in (select cidetails from dodetails "
                        + "where deliveryorder = ? and company = ? )";
                PreparedStatement stmt8 = conn.prepareStatement(query8);
                PreparedStatement stmt9 = conn.prepareStatement(query8);
                stmt8.setString(1, orderid);
                stmt8.setString(2, companyId);
                ResultSet rs2 = stmt8.executeQuery();

                if (rs2.next()) { //Single Invoice Linking case,setting external currency rate of invoice.
                    if (StringUtil.getInteger(rs2.getString(1)) == 1) {
                        mainQuery = "update deliveryorder do set do.externalcurrencyrate = (select je.externalcurrencyrate  from journalentry je "
                                + "inner join invoice inv on  je.id = inv.journalentry inner join invoicedetails invd on invd.invoice = inv.id "
                                + " inner join dodetails dod on dod.cidetails = invd.id where do.company = ? and dod.deliveryorder=? LIMIT 1)"
                                + " where do.company= ? and do.id = ? ";
                        stmt9 = conn.prepareStatement(mainQuery);
                        stmt9.setString(1, companyId);
                        stmt9.setString(2, orderid);
                        stmt9.setString(3, companyId);
                        stmt9.setString(4, orderid);
                        stmt9.executeUpdate();
                    } else { //Multiple linking or No linking case, setting external currency rate near to DO transaction date
                        mainQuery = "update deliveryorder do set do.externalcurrencyrate = (select erd.exchangerate from exchangeratedetails erd "
                                + "inner join exchangerate er on er.id = erd.exchangeratelink where erd.company= ? and er.fromcurrency= ? and "
                                + "er.tocurrency=? ORDER BY ABS(DATEDIFF( ?, erd.applydate)) LIMIT 1) where do.company = ? and do.id = ? ";
                        stmt9 = conn.prepareStatement(mainQuery);
                        stmt9.setString(1, companyId);
                        stmt9.setString(2, fromCurrency);
                        stmt9.setString(3, toCurrency);
                        stmt9.setString(4, OrderDate);//DO transaction date
                        stmt9.setString(5, companyId);
                        stmt9.setString(6, orderid);
                        stmt9.executeUpdate();

                    }
                }
            }


            //For PR
            String query_PR1 = "select pr.id,pr.company,pr.orderdate,c.currency,pr.currency from purchasereturn pr "
                    + "inner join company c  on pr.company = c.companyid where pr.currency <> c.currency";

            boolean invoice_linked = false;
            PreparedStatement stmt_PR1 = conn.prepareStatement(query_PR1);
            ResultSet rs_PR1 = stmt_PR1.executeQuery();
            while (rs_PR1.next()) {
                orderid = rs_PR1.getString(1);
                companyId = rs_PR1.getString(2);
                OrderDate = rs_PR1.getString(3);
                fromCurrency = rs_PR1.getString(4);
                toCurrency = rs_PR1.getString(5);
                fromDate = rs_PR1.getString(5);

                String query_PR2 = "select count(distinct goodsreceipt) from grdetails where id in (select videtails from prdetails "
                        + "where purchasereturn = ? and company = ? )";
                PreparedStatement stmt_PR2 = conn.prepareStatement(query_PR2);
                PreparedStatement stmt_PR3 = conn.prepareStatement(query_PR2);
                stmt_PR2.setString(1, orderid);
                stmt_PR2.setString(2, companyId);
                ResultSet rs2 = stmt_PR2.executeQuery();
                if (rs2.next()) {
                    if (StringUtil.getInteger(rs2.getString(1)) == 1) { //Single Invoice Linking case,setting external currency rate of invoice.
                        mainQuery = "update purchasereturn pr set pr.externalcurrencyrate = (select je.externalcurrencyrate  from journalentry je "
                                + "inner join goodsreceipt gr on  je.id = gr.journalentry inner join grdetails grd on grd.goodsreceipt = gr.id "
                                + " inner join prdetails prd on prd.videtails = grd.id where gr.company = ? and prd.purchasereturn=? LIMIT 1)"
                                + " where pr.company= ? and pr.id = ? ";
                        stmt_PR3 = conn.prepareStatement(mainQuery);
                        stmt_PR3.setString(1, companyId);
                        stmt_PR3.setString(2, orderid);
                        stmt_PR3.setString(3, companyId);
                        stmt_PR3.setString(4, orderid);
                        stmt_PR3.executeUpdate();
                        invoice_linked = true;
                    } else if (StringUtil.getInteger(rs2.getString(1)) > 1) { //Multiple Invoice Linking case, setting external currency rate near to Invoice transaction date
                        mainQuery = "update purchasereturn pr set pr.externalcurrencyrate = (select erd.exchangerate from exchangeratedetails erd "
                                + "inner join exchangerate er on er.id = erd.exchangeratelink where erd.company= ? and er.fromcurrency= ? and "
                                + "er.tocurrency=? ORDER BY ABS(DATEDIFF( ?, erd.applydate)) LIMIT 1) where pr.company = ? and pr.id = ? ";
                        stmt_PR3 = conn.prepareStatement(mainQuery);
                        stmt_PR3.setString(1, companyId);
                        stmt_PR3.setString(2, fromCurrency);
                        stmt_PR3.setString(3, toCurrency);
                        stmt_PR3.setString(4, OrderDate);//PR transaction date
                        stmt_PR3.setString(5, companyId);
                        stmt_PR3.setString(6, orderid);
                        stmt_PR3.executeUpdate();
                        invoice_linked = true;
                    }
                }

                query_PR2 = "select count(distinct grorder) from grodetails where id in (select grdetails from prdetails "
                        + "where purchasereturn = ? and company = ? )";
                stmt_PR2 = conn.prepareStatement(query_PR2);
                stmt_PR2.setString(1, orderid);
                stmt_PR2.setString(2, companyId);
                rs2 = stmt_PR2.executeQuery();
                if (rs2.next() && !invoice_linked) { // only when invoice is not linked.
                    if (StringUtil.getInteger(rs2.getString(1)) == 1) { //Single GRN linking case,setting external currency rate of GRN.
                        mainQuery = "update purchasereturn pr set pr.externalcurrencyrate = (select je.externalcurrencyrate  from journalentry je "
                                + "inner join grorder gr on  je.id = gr.journalentry inner join grodetails grd on grd.grorder = gr.id "
                                + " inner join prdetails prd on prd.grdetails = grd.id where gr.company = ? and prd.purchasereturn=? LIMIT 1)"
                                + " where pr.company= ? and pr.id = ? ";
                        stmt_PR3 = conn.prepareStatement(mainQuery);
                        stmt_PR3.setString(1, companyId);
                        stmt_PR3.setString(2, orderid);
                        stmt_PR3.setString(3, companyId);
                        stmt_PR3.setString(4, orderid);
                        stmt_PR3.executeUpdate();
                    } else {  //Multiple GRN or No linking case, setting external currency rate near to GRN transaction date
                        mainQuery = "update purchasereturn pr set pr.externalcurrencyrate = (select erd.exchangerate from exchangeratedetails erd "
                                + "inner join exchangerate er on er.id = erd.exchangeratelink where erd.company= ? and er.fromcurrency= ? and "
                                + "er.tocurrency=? ORDER BY ABS(DATEDIFF( ?, erd.applydate)) LIMIT 1) where pr.company = ? and pr.id = ? ";
                        stmt_PR3 = conn.prepareStatement(mainQuery);
                        stmt_PR3.setString(1, companyId);
                        stmt_PR3.setString(2, fromCurrency);
                        stmt_PR3.setString(3, toCurrency);
                        stmt_PR3.setString(4, OrderDate);//PR transaction date
                        stmt_PR3.setString(5, companyId);
                        stmt_PR3.setString(6, orderid);
                        stmt_PR3.executeUpdate();

                    }
                }

                invoice_linked = false;
            }


            //For SR
            String query_SR1 = "select sr.id,sr.company,sr.orderdate,c.currency,sr.currency from salesreturn sr "
                    + "inner join company c  on sr.company = c.companyid where sr.currency <> c.currency";

            PreparedStatement stmt_SR1 = conn.prepareStatement(query_SR1);
            ResultSet rs_SR1 = stmt_SR1.executeQuery();
            while (rs_PR1.next()) {
                orderid = rs_PR1.getString(1);
                companyId = rs_PR1.getString(2);
                OrderDate = rs_PR1.getString(3);
                fromCurrency = rs_PR1.getString(4);
                toCurrency = rs_PR1.getString(5);
                fromDate = rs_PR1.getString(5);
                String query_PR2 = "select count(distinct invoice) from invoicedetails where id in (select cidetails from srdetails "
                        + "where salesreturn = ? and company = ? )";
                PreparedStatement stmt_PR2 = conn.prepareStatement(query_PR2);
                PreparedStatement stmt_PR3 = conn.prepareStatement(query_PR2);
                stmt_PR2.setString(1, orderid);
                stmt_PR2.setString(2, companyId);
                ResultSet rs2 = stmt_PR2.executeQuery();
                if (rs2.next()) {
                    if (StringUtil.getInteger(rs2.getString(1)) == 1) { //Single invoice linking case,setting external currency rate of invoice.
                        mainQuery = "update salesreturn sr set sr.externalcurrencyrate = (select je.externalcurrencyrate  from journalentry je "
                                + "inner join invoice inv on  je.id = inv.journalentry inner join invoicedetails invd on invd.invoice = inv.id "
                                + " inner join srdetails srd on srd.cidetails = invd.id where inv.company = ? and srd.salesreturn=? LIMIT 1)"
                                + " where sr.company= ? and sr.id = ? ";
                        stmt_PR3 = conn.prepareStatement(mainQuery);
                        stmt_PR3.setString(1, companyId);
                        stmt_PR3.setString(2, orderid);
                        stmt_PR3.setString(3, companyId);
                        stmt_PR3.setString(4, orderid);
                        stmt_PR3.executeUpdate();
                        invoice_linked = true;
                    } else if (StringUtil.getInteger(rs2.getString(1)) > 1) { //Multiple Invoice Linking case, setting external currency rate near to Invoice transaction date
                        mainQuery = "update salesreturn sr set sr.externalcurrencyrate = (select erd.exchangerate from exchangeratedetails erd "
                                + "inner join exchangerate er on er.id = erd.exchangeratelink where erd.company= ? and er.fromcurrency= ? and "
                                + "er.tocurrency=? ORDER BY ABS(DATEDIFF( ?, erd.applydate)) LIMIT 1) where sr.company = ? and sr.id = ? ";
                        stmt_PR3 = conn.prepareStatement(mainQuery);
                        stmt_PR3.setString(1, companyId);
                        stmt_PR3.setString(2, fromCurrency);
                        stmt_PR3.setString(3, toCurrency);
                        stmt_PR3.setString(4, OrderDate);//SR transaction date
                        stmt_PR3.setString(5, companyId);
                        stmt_PR3.setString(6, orderid);
                        stmt_PR3.executeUpdate();
                        invoice_linked = true;
                    }
                }

                query_PR2 = "select count(distinct deliveryorder) from dodetails where id in (select dodetails from srdetails "
                        + "where salesreturn = ? and company = ? )";
                stmt_PR2 = conn.prepareStatement(query_PR2);
                stmt_PR2.setString(1, orderid);
                stmt_PR2.setString(2, companyId);
                rs2 = stmt_PR2.executeQuery();
                if (rs2.next() && !invoice_linked) {// only when invoice is not linked.
                    if (StringUtil.getInteger(rs2.getString(1)) == 1) {//Single DO linking case,setting external currency rate of DO.
                        mainQuery = "update salesreturn sr set sr.externalcurrencyrate = (select je.externalcurrencyrate  from journalentry je "
                                + "inner join deliveryorder do on  je.id = do.journalentry inner join dodetails dod on dod.deliveryorder = do.id "
                                + " inner join srdetails srd on srd.dodetails = dod.id where do.company = ? and srd.salesreturn=? LIMIT 1)"
                                + " where sr.company= ? and sr.id = ? ";
                        stmt_PR3 = conn.prepareStatement(mainQuery);
                        stmt_PR3.setString(1, companyId);
                        stmt_PR3.setString(2, orderid);
                        stmt_PR3.setString(3, companyId);
                        stmt_PR3.setString(4, orderid);
                        stmt_PR3.executeUpdate();
                    } else { //Multiple DO or No linking case, setting external currency rate near to DO transaction date
                        mainQuery = "update salesreturn sr set sr.externalcurrencyrate = (select erd.exchangerate from exchangeratedetails erd "
                                + "inner join exchangerate er on er.id = erd.exchangeratelink where erd.company= ? and er.fromcurrency= ? and "
                                + "er.tocurrency=? ORDER BY ABS(DATEDIFF( ?, erd.applydate)) LIMIT 1) where sr.company = ? and sr.id = ? ";
                        stmt_PR3 = conn.prepareStatement(mainQuery);
                        stmt_PR3.setString(1, companyId);
                        stmt_PR3.setString(2, fromCurrency);
                        stmt_PR3.setString(3, toCurrency);
                        stmt_PR3.setString(4, OrderDate);//SR transaction date
                        stmt_PR3.setString(5, companyId);
                        stmt_PR3.setString(6, orderid);
                        stmt_PR3.executeUpdate();

                    }
                }
                invoice_linked = false;
            }
            out.println(" Records updated successfully.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    }
%>
