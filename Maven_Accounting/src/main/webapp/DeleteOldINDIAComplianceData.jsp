<%-- 
    Document   : DeleteOldINDIAComplianceData
    Created on : Sep 7, 2017, 3:35:51 PM
    Author     : krawler
--%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.Connection"%>

<%
    Connection conn = null;
    String serverip = request.getParameter("serverip");
    String port = "3306";
    String dbName = request.getParameter("dbname");
    String userName = request.getParameter("username");
    String password = request.getParameter("password");
    String subdomain = request.getParameter("subdomain");
    if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
        throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
    }
    String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
    String driver = "com.mysql.jdbc.Driver";
    String fialResultStatus = "Final Result : <br/>";
    try {
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);

        String companyQuery = "select c.companyid, c.subdomain , c.country from company c inner join extracompanypreferences ecf on c.companyid=ecf.id where (isnewgst='T' or isnewgst = 1) and ecf.lineleveltermflag=1  and c.country=105 ";

        if (!StringUtil.isNullOrEmpty(subdomain)) {
            companyQuery += "  and c.subdomain= '" + subdomain + "' ";
        }
        PreparedStatement pstmt = null;
        pstmt = conn.prepareStatement(companyQuery);
        ResultSet rscompany = pstmt.executeQuery();

        List<String> transactionTables = new ArrayList<String>();
        transactionTables.add("vendorquotationdetailstermmap");
        transactionTables.add("purchaseorderdetailstermmap");
        transactionTables.add("salesorderdetailtermmap");
        transactionTables.add("quotationdetailtermmap");
        transactionTables.add("salesreturndetailtermmap");
        transactionTables.add("invoicedetailtermsmap");
        transactionTables.add("deliveryorderdetailtermsmap");
        transactionTables.add("producttermsmap");
        transactionTables.add("receiptorderdetailtermsmap");
        transactionTables.add("receiptdetailtermsmap");
        transactionTables.add("purchasereturndetailtermmap");
        transactionTables.add("dealerexciseterm");

        
        
        List<String> taxUsedtables = new ArrayList<String>();
        taxUsedtables.add("billinggr");
        taxUsedtables.add("billinggrdetails");
        taxUsedtables.add("billinginvoice");
        taxUsedtables.add("billinginvoicedetails");
        taxUsedtables.add("billingpaymentdetailotherwise");
        taxUsedtables.add("billingpodetails");
        taxUsedtables.add("billingpurchaseorder");
        taxUsedtables.add("billingreceiptdetailotherwise");
        taxUsedtables.add("billingsalesorder");
        taxUsedtables.add("billingsodetails");
        taxUsedtables.add("cndetailsgst");
        taxUsedtables.add("contractdetails");
        taxUsedtables.add("creditnote");
        taxUsedtables.add("debitnote");
        taxUsedtables.add("dndetailsgst");
        taxUsedtables.add("expenseggrdetails");
        taxUsedtables.add("expensepodetails");
        taxUsedtables.add("goodsreceipt");
        taxUsedtables.add("grdetails");
        taxUsedtables.add("invoice");
        taxUsedtables.add("invoicedetails");
        taxUsedtables.add("jedetail");
        taxUsedtables.add("paymentdetailotherwise");
        taxUsedtables.add("podetails");
        taxUsedtables.add("prdetails");
        taxUsedtables.add("purchaseorder");
        taxUsedtables.add("purchaserequisition");
        taxUsedtables.add("purchaserequisitiondetail");
        taxUsedtables.add("quotation");
        taxUsedtables.add("quotationdetails");
        taxUsedtables.add("quotationversion");
        taxUsedtables.add("quotationversiondetails");
        taxUsedtables.add("receipt");
        taxUsedtables.add("receiptadvancedetail");
        taxUsedtables.add("receiptdetailotherwise");
        taxUsedtables.add("salesorder");
        taxUsedtables.add("securitygatedetails");
        taxUsedtables.add("securitygateentry");
        taxUsedtables.add("sodetails");
        taxUsedtables.add("srdetails");
        taxUsedtables.add("taxadjustment");
        //taxUsedtables.add("taxlist"); // Need to delete entries
        //taxUsedtables.add("taxtermsmapping"); //Need to delete entries
        taxUsedtables.add("vendorquotation");
        taxUsedtables.add("vendorquotationdetails");
        taxUsedtables.add("vendorquotationversion");
        taxUsedtables.add("vendorquotationversiondetails");
        taxUsedtables.add("vendorquotationversiondetails");

        

        //alter table linelevelterms drop foreign key  linelevelterms_ibfk_4;
        //alter table linelevelterms drop foreign key  linelevelterms_ibfk_7;
        //alter table producttermsmap  drop foreign key  producttermsmap_ibfk_1;
//        List<String> deleteTablesConstraints = new ArrayList<String>();
//        deleteTablesConstraints.add("alter table linelevelterms drop foreign key  linelevelterms_ibfk_4;"); // Delete defaultterms Constraints
//        deleteTablesConstraints.add("alter table linelevelterms drop foreign key  linelevelterms_ibfk_7;"); // Delete defaultterms Constraints
//        deleteTablesConstraints.add("alter table producttermsmap  drop foreign key  producttermsmap_ibfk_1;"); // Delete linelevelterms Constraints - producttermsmap not used table
//        for (String query : deleteTablesConstraints) {
//            try {
//                pstmt = conn.prepareStatement(query);
//                pstmt.executeUpdate();
//            } catch (Exception e) {
//                // If constarints not exists
//            }
//        }
        // Default terms table Constraints in linelevelterms
        try{
            String defaulttermConstraints = " SELECT concat('ALTER TABLE linelevelterms DROP FOREIGN KEY ', CONSTRAINT_NAME, ';') as query  FROM information_schema.key_column_usage  WHERE CONSTRAINT_SCHEMA = '"+ dbName + "'   AND TABLE_NAME='linelevelterms'  AND REFERENCED_TABLE_NAME='defaultterms';";
            pstmt = conn.prepareStatement(defaulttermConstraints);
            ResultSet constraintsRS = pstmt.executeQuery();
            while(constraintsRS.next()){
                String constraintsQuery  = constraintsRS.getString("query");
                try {
                    pstmt = conn.prepareStatement(constraintsQuery);
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    // If constarints not exists
                }
            }
        }catch(Exception ex){
        }
        // linelevelterms Constrains in producttermsmap  table
        try{
            String producttermsmapConstraints = "SELECT concat('ALTER TABLE producttermsmap DROP FOREIGN KEY ', CONSTRAINT_NAME, ';') as query  FROM information_schema.key_column_usage  WHERE CONSTRAINT_SCHEMA = '"+ dbName + "'   AND TABLE_NAME='producttermsmap'  AND REFERENCED_TABLE_NAME='linelevelterms';";
            pstmt = conn.prepareStatement(producttermsmapConstraints);
            ResultSet constraintsRS = pstmt.executeQuery();
            while(constraintsRS.next()){
                String constraintsQuery  = constraintsRS.getString("query");
                try {
                    pstmt = conn.prepareStatement(constraintsQuery);
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    // If constarints not exists
                }
            }
        }catch(Exception ex){
        }
        while (rscompany.next()) {
            String companyid = rscompany.getString("companyid");
            String companyDomain = rscompany.getString("Subdomain");
            String country = rscompany.getString("country");
            /**
             * Delete data From Transaction tables
             */
            fialResultStatus += "<br/>===============Started Execution for : " + companyDomain + "=====================<br/>";
            String termQuery = " where term in (select id from linelevelterms where termtype !=7 and company='" + companyid + "'); ";
            int deletedRecords = 0;

            for (String tableName : transactionTables) {
                String tableQuery = " delete from " + tableName + termQuery;
                deletedRecords = 0;
                try {
                    pstmt = conn.prepareStatement(tableQuery);
                    deletedRecords = pstmt.executeUpdate();

                    fialResultStatus += " Execution Done for  : " + tableName + " ( " + deletedRecords + ") <br/>";
                } catch (Exception e) {
                    fialResultStatus += " Falied Execution for  : " + tableName + " ( " + e.getMessage() + ") <br/>";
                }
            }

            // delete Linelevel terms table Data
            deletedRecords = 0;
            try {
                pstmt = conn.prepareStatement(" delete from linelevelterms where termtype!=7 and company= '" + companyid + "' ;");
                deletedRecords = pstmt.executeUpdate();

                fialResultStatus += " Execution Done for  : linelevelterms ( " + deletedRecords + ") <br/>";
            } catch (Exception e) {
                fialResultStatus += " Falied Execution for  : linelevelterms ( " + e.getMessage() + ") <br/>";
            }

            // delete taxList table Data
            deletedRecords = 0;
            try {
                pstmt = conn.prepareStatement(" delete from taxlist where company= '" + companyid + "' ;");
                deletedRecords = pstmt.executeUpdate();

                fialResultStatus += " Execution Done for  : taxlist ( " + deletedRecords + ") <br/>";
            } catch (Exception e) {
                fialResultStatus += " Falied Execution for  : taxlist ( " + e.getMessage() + ") <br/>";
            }

            //******************************************Update Tax column **************************************
            String taxQuery = " where tax in (select id from tax where company= '" + companyid + "' );";
            String jedetailQuery = " where gstapplied in (select id from tax where company= '" + companyid + "' );";
            deletedRecords = 0;
            // update tax table Data
            for (String tableName : taxUsedtables) {
                String tableQuery = " update " + tableName +  (tableName.equalsIgnoreCase("jedetail") ?  " set gstapplied=null " + jedetailQuery : " set tax=null " + taxQuery);
                deletedRecords = 0;
                try {
                    pstmt = conn.prepareStatement(tableQuery);
                    deletedRecords = pstmt.executeUpdate();
                    fialResultStatus += " Execution Done for  : " + tableName + " ( " + deletedRecords + ") <br/>";
                } catch (Exception e) {
                    fialResultStatus += " Falied Execution for  : " + tableName + " ( " + e.getMessage() + ") <br/>";
                }
            }
            //******************************************Update Tax column **************************************
            // update paymentdetailotherwise table Data
                String tableQuery = " update paymentdetailotherwise  set gstapplied=null where gstapplied in (select id from tax where company= '" + companyid + "' );";
                deletedRecords = 0;
                try {
                    pstmt = conn.prepareStatement(tableQuery);
                    deletedRecords = pstmt.executeUpdate();
                    fialResultStatus += " Execution Done for  : paymentdetailotherwise ( " + deletedRecords + ") <br/>";
                } catch (Exception e) {
                    fialResultStatus += " Falied Execution for  : paymentdetailotherwise ( " + e.getMessage() + ") <br/>";
                }
            //****************************************** End Update Tax column **************************************
            
            // set taxid for vendor table Data
            deletedRecords = 0;
            try {
                pstmt = conn.prepareStatement(" update vendor set taxid=null where  company= '" + companyid + "' ;");
                deletedRecords = pstmt.executeUpdate();
                fialResultStatus += " Execution Done for  : vendor ( " + deletedRecords + ") <br/>";
            } catch (Exception e) {
                fialResultStatus += " Falied Execution for  : vendor ( " + e.getMessage() + ") <br/>";
            }
            
            // set taxid for vendor table Data
            deletedRecords = 0;
            try {
                pstmt = conn.prepareStatement(" update customer set taxid=null where  company= '" + companyid + "' ;");
                deletedRecords = pstmt.executeUpdate();
                fialResultStatus += " Execution Done for  : customer ( " + deletedRecords + ") <br/>";
            } catch (Exception e) {
                fialResultStatus += " Falied Execution for  : customer ( " + e.getMessage() + ") <br/>";
            }
            
            // delete taxtermsmapping table Data
            deletedRecords = 0;
            try {
                pstmt = conn.prepareStatement(" delete from taxtermsmapping where tax in (select id from tax where company= '" + companyid + "' );");
                deletedRecords = pstmt.executeUpdate();

                fialResultStatus += " Execution Done for  : taxtermsmapping ( " + deletedRecords + ") <br/>";
            } catch (Exception e) {
                fialResultStatus += " Falied Execution for  : taxtermsmapping ( " + e.getMessage() + ") <br/>";
            }

            // delete tax table Data
            deletedRecords = 0;
            try {
                pstmt = conn.prepareStatement(" delete from tax where company= '" + companyid + "' ;");
                deletedRecords = pstmt.executeUpdate();

                fialResultStatus += " Execution Done for  : tax ( " + deletedRecords + ") <br/>";
            } catch (Exception e) {
                fialResultStatus += " Falied Execution for  : tax ( " + e.getMessage() + ") <br/>";
            }

            fialResultStatus += "<br/>===============End Execution for : " + companyDomain + "========================<br/>";
            //----------------------------------------
        }
        // delete defaultterms table Data
        int deletedRecords = 0;

        try {
            pstmt = conn.prepareStatement("delete from defaultterms where country=105  and termtype!=7 ;");
            deletedRecords = pstmt.executeUpdate();

            fialResultStatus += " Execution Done for  : defaultterms ( " + deletedRecords + ") <br/>";
        } catch (Exception e) {
            fialResultStatus += " Falied Execution for  : defaultterms ( " + e.getMessage() + ") <br/>";
        }
        // delete defaultgst table Data
        deletedRecords = 0;
        try {
            pstmt = conn.prepareStatement(" delete from defaultgst where country=105  ; ");
            deletedRecords = pstmt.executeUpdate();

            fialResultStatus += " Execution Done for  : defaultgst ( " + deletedRecords + ") <br/>";
        } catch (Exception e) {
            fialResultStatus += " Falied Execution for  : defaultgst ( " + e.getMessage() + ") <br/>";
        }
    } catch (Exception ex) {
        fialResultStatus += ex.getMessage();
    } finally {
        out.println(fialResultStatus);
        conn.close();
    }
%>