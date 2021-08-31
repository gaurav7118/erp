<%-- 
    Document   : insertInvoices
    Created on : 8 Oct, 2015, 2:27:09 PM
    Author     : krawler
    Purpose    :writen for inserting bulk invoices. 
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


<%
    Connection conn = null;
    try {

        String serverip = "localhost";
        String port = "3306";
        String dbName = "fasten07102015";
        String SERVICE_ID = "4efb0286-5627-102d-8de6-001cc0794cfa";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";

        String driver = "com.mysql.jdbc.Driver";
        String userName = "krawlersqladmin";
        String password = "krawler";
        String companyId = "04575a0c-b33c-11e3-986d-001e670e1414";
        String subdomain="fastentest";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
       
        int count=0;
        int seqNum=37000;
         String query1 = "select companyid from company where subdomain= ? ";
         PreparedStatement stmt1 = conn.prepareStatement(query1);
         stmt1.setString(1, subdomain);
        ResultSet rs1 = stmt1.executeQuery();
        java.sql.Timestamp  sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
        Calendar c1 = Calendar.getInstance();
        c1.setTime(sqlDate); // Now use today date.
        c1.add(Calendar.DATE, 19); // Adding 5 days 17 oct
        //java.sql.Timestamp  dueDate = new java.sql.Timestamp(c.getTime());
        sqlDate=new java.sql.Timestamp(c1.getTimeInMillis());//duedate
        int recordcount=0;
        int day=4;
        while (rs1.next()) {
           
            companyId = rs1.getString("companyid");

            String query2 = "select id,invoicenumber,autogen,duedate ,deleteflag ,journalentry ,company,centry ,tax ,taxentry ,currency ,exchangeratedetail, externalcurrencyrate , customer , parentinvoice , repeateinvoice , partialinv , templateid , pendingapproval , favouriteflag , inventoryorderid , approvallevel , approver , istemplate , mastersalesperson , seqnumber , seqformat, termid , account, createdby  , modifiedby , createdon , updatedon , isopeningbalenceinvoice , originalopeningbalanceamount , openingbalanceamountdue , porefdate , lastmodifieddate , creationdate , isnormalinvoice , exchangerateforopeningtransaction , cashtransaction , billingshippingaddresses , printedflag , isfixedassetinvoice , isfixedassetleaseinvoice , shiplength , invoicetype , gstincluded , isconversionratefromcurrencytobase , invoiceamountdue , originalopeningbalancebaseamount , openingbalancebaseamountdue , custWarehouse , isconsignment , deliverytime , baddebttype , claimedperiod , debtclaimeddate , paydetail , gstcurrencyrate , islinkedwithtaxapplieddo , termsincludegst , movementtype , approvestatuslevel , isdraft , isfromPOS , isopensr , isopendo  from invoice where company= ? and isnormalinvoice=1 and deleteflag='F' and creationdate >='2015-09-01 12:14:12' limit 2000 ";
            PreparedStatement stmt2 = conn.prepareStatement(query2);
            stmt2.setObject(1, companyId);
           // stmt2.setObject(2, "ff8080815046ba70015046cf87f20008");
            ResultSet rs2 = stmt2.executeQuery();
            while (rs2.next()) {
                count++;
                
                String newCentryID="";
                String newtaxentryID="";
                
                String invoiceId = java.util.UUID.randomUUID().toString();
                String journalEntryId = java.util.UUID.randomUUID().toString();
                String	id	=	rs2.getString("id");
                String invoicenumber = rs2.getString("invoicenumber");
                String autogen = rs2.getString("autogen");
                String duedate = rs2.getString("duedate");
                String deleteflag = rs2.getString("deleteflag");
                String journalentry = rs2.getString("journalentry");
                String company = rs2.getString("company");
                String centry = rs2.getString("centry");
                String tax = rs2.getString("tax");
                String taxentry = rs2.getString("taxentry");
                String currency = rs2.getString("currency");
                String exchangeratedetail = rs2.getString("exchangeratedetail");
                String externalcurrencyrate = rs2.getString("externalcurrencyrate");
                String customer = rs2.getString("customer");
                String parentinvoice = rs2.getString("parentinvoice");
                String repeateinvoice = rs2.getString("repeateinvoice");
                String partialinv = rs2.getString("partialinv");
                String templateid = rs2.getString("templateid");
                String pendingapproval = rs2.getString("pendingapproval");
                String favouriteflag = rs2.getString("favouriteflag");
                String inventoryorderid = rs2.getString("inventoryorderid");
                String approvallevel = rs2.getString("approvallevel");
                String approver = rs2.getString("approver");
                String istemplate = rs2.getString("istemplate");
                String mastersalesperson = rs2.getString("mastersalesperson");
                String seqnumber = rs2.getString("seqnumber");
                String seqformat = rs2.getString("seqformat");
                String termid = rs2.getString("termid");
                String account = rs2.getString("account");
                String createdby = rs2.getString("createdby");
                String modifiedby = rs2.getString("modifiedby");
                String createdon = rs2.getString("createdon");
                String updatedon = rs2.getString("updatedon");
                String isopeningbalenceinvoice = rs2.getString("isopeningbalenceinvoice");
                String originalopeningbalanceamount = rs2.getString("originalopeningbalanceamount");
                String openingbalanceamountdue = rs2.getString("openingbalanceamountdue");
                String porefdate = rs2.getString("porefdate");
                String lastmodifieddate = rs2.getString("lastmodifieddate");
                String creationdate = rs2.getString("creationdate");
                String isnormalinvoice = rs2.getString("isnormalinvoice");
                String exchangerateforopeningtransaction = rs2.getString("exchangerateforopeningtransaction");
                String cashtransaction = rs2.getString("cashtransaction");
                String billingshippingaddresses = rs2.getString("billingshippingaddresses");
                String printedflag = rs2.getString("printedflag");
                String isfixedassetinvoice = rs2.getString("isfixedassetinvoice");
                String isfixedassetleaseinvoice = rs2.getString("isfixedassetleaseinvoice");
                String shiplength = rs2.getString("shiplength");
                String invoicetype = rs2.getString("invoicetype");
                String gstincluded = rs2.getString("gstincluded");
                String isconversionratefromcurrencytobase = rs2.getString("isconversionratefromcurrencytobase");
                String invoiceamountdue = rs2.getString("invoiceamountdue");
                String originalopeningbalancebaseamount = rs2.getString("originalopeningbalancebaseamount");
                String openingbalancebaseamountdue = rs2.getString("openingbalancebaseamountdue");
                String custWarehouse = rs2.getString("custWarehouse");
                String isconsignment = rs2.getString("isconsignment");
                String deliverytime = rs2.getString("deliverytime");
                String baddebttype = rs2.getString("baddebttype");
                String claimedperiod = rs2.getString("claimedperiod");
                String debtclaimeddate = rs2.getString("debtclaimeddate");
                String paydetail = rs2.getString("paydetail");
                String gstcurrencyrate = rs2.getString("gstcurrencyrate");
                String islinkedwithtaxapplieddo = rs2.getString("islinkedwithtaxapplieddo");
                String termsincludegst = rs2.getString("termsincludegst");
                String movementtype = rs2.getString("movementtype");
                String approvestatuslevel = rs2.getString("approvestatuslevel");
                String isdraft = rs2.getString("isdraft");
                String isfromPOS = rs2.getString("isfromPOS");
                String isopensr = rs2.getString("isopensr");
                String isopendo = rs2.getString("isopendo");
                

                String query5 = " select  id , entryno, autogen ,entrydate , company , currency , deleteflag , externalcurrencyrate , costcenter , createdon, isinventory , reversejournalentry , isreverseje , eliminateflag , pendingapproval , accjecustomdataref , optimizedflag , templatepermcode , intercompanyflag , isReval , istemplate , typevalue , seqnumber , seqformat , revalinvoiceid , partlyjeentrywithcndn , parentje , repeateje , cheque , createdby, approvestatuslevel , chequeprinted , paymentmethod , paidto , isbaddebtje , gstcurrencyrate , istaxadjustmentje , isexchangegainslossje , baddebtseqnumber , paymentcurrencytopaymentmethodcurrencyrate , ismulticurrencypaymentje , customer , isOneTimeReverse , isdraft , isdishonouredcheque , transactionId , transactionModuleid from journalentry where id= ? ";
                PreparedStatement stmt5 = conn.prepareStatement(query5);
                stmt5.setObject(1, journalentry);
                ResultSet rs5 = stmt5.executeQuery();
                while (rs5.next()) {
                    
                    String id5 = rs5.getString("id");
                    String entryno = rs5.getString("entryno");
                    String autogen5 = rs5.getString("autogen");
                    String entrydate = rs5.getString("entrydate");
                    String company5 = rs5.getString("company");
                    String currency5 = rs5.getString("currency");
                    String deleteflag5 = rs5.getString("deleteflag");
                    String externalcurrencyrate5 = rs5.getString("externalcurrencyrate");
                    String costcenter = rs5.getString("costcenter");
                    String createdon5 = rs5.getString("createdon");
                    String isinventory = rs5.getString("isinventory");
                    String reversejournalentry = rs5.getString("reversejournalentry");
                    String isreverseje = rs5.getString("isreverseje");
                    String eliminateflag = rs5.getString("eliminateflag");
                    String pendingapproval5 = rs5.getString("pendingapproval");
                    String accjecustomdataref = rs5.getString("accjecustomdataref");
                    String optimizedflag = rs5.getString("optimizedflag");
                    String templatepermcode = rs5.getString("templatepermcode");
                    String intercompanyflag = rs5.getString("intercompanyflag");
                    String isReval = rs5.getString("isReval");
                    String istemplate5 = rs5.getString("istemplate");
                    String typevalue = rs5.getString("typevalue");
                    String seqnumber5 = rs5.getString("seqnumber");
                    String seqformat5 = rs5.getString("seqformat");
                    String revalinvoiceid = rs5.getString("revalinvoiceid");
                    String partlyjeentrywithcndn = rs5.getString("partlyjeentrywithcndn");
                    String parentje = rs5.getString("parentje");
                    String repeateje = rs5.getString("repeateje");
                    String cheque = rs5.getString("cheque");
                    String createdby5 = rs5.getString("createdby");
                    String approvestatuslevel5 = rs5.getString("approvestatuslevel");
                    String chequeprinted = rs5.getString("chequeprinted");
                    String paymentmethod = rs5.getString("paymentmethod");
                    String paidto = rs5.getString("paidto");
                    String isbaddebtje = rs5.getString("isbaddebtje");
                    String gstcurrencyrate5 = rs5.getString("gstcurrencyrate");
                    String istaxadjustmentje = rs5.getString("istaxadjustmentje");
                    String isexchangegainslossje = rs5.getString("isexchangegainslossje");
                    String baddebtseqnumber = rs5.getString("baddebtseqnumber");
                    String paymentcurrencytopaymentmethodcurrencyrate = rs5.getString("paymentcurrencytopaymentmethodcurrencyrate");
                    String ismulticurrencypaymentje = rs5.getString("ismulticurrencypaymentje");
                    String customer5 = rs5.getString("customer");
                    String isOneTimeReverse = rs5.getString("isOneTimeReverse");
                    String isdraft5 = rs5.getString("isdraft");
                    String isdishonouredcheque = rs5.getString("isdishonouredcheque");
                    String transactionId = rs5.getString("transactionId");
                    String transactionModuleid = rs5.getString("transactionModuleid");

                    String query10 = "insert into journalentry (id , entryno, autogen ,entrydate , company , currency , deleteflag , "
                            + "externalcurrencyrate , costcenter , createdon, isinventory , reversejournalentry , isreverseje , eliminateflag ,"
                            + " pendingapproval , accjecustomdataref , optimizedflag , templatepermcode , isReval , istemplate ,"
                            + " typevalue , seqnumber , seqformat , revalinvoiceid , partlyjeentrywithcndn , parentje , repeateje , cheque , "
                            + "createdby, approvestatuslevel , chequeprinted , paymentmethod , paidto , isbaddebtje , gstcurrencyrate , "
                            + "istaxadjustmentje , isexchangegainslossje , baddebtseqnumber , paymentcurrencytopaymentmethodcurrencyrate , "
                            + "ismulticurrencypaymentje,customer ,isOneTimeReverse ,isdraft ,isdishonouredcheque ,transactionId ,transactionModuleid, intercompanyflag) "
                            + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
                    PreparedStatement stmt10 = conn.prepareStatement(query10);
                    stmt10.setString(1, journalEntryId);
                    stmt10.setString(2, "JE00"+seqNum);
                    stmt10.setString(3, autogen5);
                    stmt10.setTimestamp(4,sqlDate );//entrydate
                    stmt10.setString(5, company5);
                    stmt10.setString(6, currency5);
                    stmt10.setString(7, deleteflag5);
                    stmt10.setDouble(8, Double.parseDouble(externalcurrencyrate5));
                    stmt10.setString(9, costcenter);
                    stmt10.setLong(10, Long.parseLong(createdon));
                    stmt10.setString(11, isinventory);
                    stmt10.setString(12, reversejournalentry);
                    stmt10.setString(13, isreverseje);
                    stmt10.setString(14, eliminateflag);
                    stmt10.setInt(15, Integer.parseInt(pendingapproval5));
                    stmt10.setString(16, accjecustomdataref);
                    stmt10.setString(17, optimizedflag);
                    stmt10.setString(18, templatepermcode);
                    stmt10.setInt(19, Integer.parseInt(isReval));
                    stmt10.setInt(20, Integer.parseInt(istemplate5));
                    stmt10.setInt(21, Integer.parseInt(typevalue));
                    stmt10.setInt(22, seqNum);
                    stmt10.setString(23, "4028818a4e05e809014e05f951e30027");//seqformat
                    stmt10.setString(24, revalinvoiceid);
                    stmt10.setString(25, partlyjeentrywithcndn);
                    stmt10.setString(26, parentje);
                    stmt10.setString(27, repeateje);
                    stmt10.setString(28, cheque);
                    stmt10.setString(29, createdby5);
                    stmt10.setInt(30, Integer.parseInt(approvestatuslevel5));
                    stmt10.setBoolean(31, Boolean.parseBoolean(chequeprinted));
                    stmt10.setString(32, paymentmethod);
                    stmt10.setString(33, paidto);
                    stmt10.setString(34, isbaddebtje);
                    stmt10.setString(35, gstcurrencyrate);
                    stmt10.setString(36, istaxadjustmentje);
                    stmt10.setString(37, isexchangegainslossje);
                    stmt10.setString(38, baddebtseqnumber);
                    stmt10.setDouble(39,Double.parseDouble(paymentcurrencytopaymentmethodcurrencyrate));
                    stmt10.setString(40, ismulticurrencypaymentje);
                    stmt10.setString(41, customer5);
                    stmt10.setString(42, isOneTimeReverse);
                    stmt10.setString(43, isdraft5);
                    stmt10.setString(44, isdishonouredcheque);
                    stmt10.setString(45, transactionId);
                    stmt10.setInt(46, Integer.parseInt(transactionModuleid));
                    stmt10.setString(47, intercompanyflag);
                    stmt10.execute();
                                
                                
                    String query6 = " select id , debit , amount , account , journalEntry , company, description , srno , accjedetailcustomdataref , accjedetailproductcustomdataref , accountpersontype , gstcurrencyrate , forexgainloss , paymenttype , isbankcharge from jedetail where journalentry=?";
                    PreparedStatement stmt6 = conn.prepareStatement(query6);
                    stmt6.setObject(1, id5);
                    ResultSet rs6 = stmt6.executeQuery();
                    while (rs6.next()) {
                        String id6 = rs6.getString("id");
                        String jeDetailId = java.util.UUID.randomUUID().toString();
                        String debit = rs6.getString("debit");
                        String amount = rs6.getString("amount");
                        String account6 = rs6.getString("account");
                        String journalEntry = rs6.getString("journalEntry");
                        String company6 = rs6.getString("company");
                        String description = rs6.getString("description");
                        String srno = rs6.getString("srno");
                        String accjedetailcustomdataref = rs6.getString("accjedetailcustomdataref");
                        String accjedetailproductcustomdataref = rs6.getString("accountpersontype");
                        String accountpersontype = rs6.getString("accountpersontype");
                        String gstcurrencyrate6 = rs6.getString("gstcurrencyrate");
                        String forexgainloss = rs6.getString("forexgainloss");
                        String paymenttype = rs6.getString("paymenttype");
                        String isbankcharge = rs6.getString("isbankcharge");
                        
                        if(id6.equalsIgnoreCase(centry)){
                            newCentryID=jeDetailId;
                            
                        }else if(id6.equalsIgnoreCase(taxentry)){
                            newtaxentryID=jeDetailId;
                        }
                        String query11 = "insert into jedetail (id , debit , amount , account , journalEntry , company, description , srno , "
                                + "accjedetailcustomdataref , accjedetailproductcustomdataref , accountpersontype , gstcurrencyrate , "
                                + "forexgainloss , paymenttype , isbankcharge) "
                                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
                        PreparedStatement stmt11 = conn.prepareStatement(query11);
                        stmt11.setString(1, jeDetailId);
                        stmt11.setString(2, debit);
                        stmt11.setDouble(3, Double.parseDouble(amount));
                        stmt11.setString(4, account6);
                        stmt11.setString(5, journalEntryId);
                        stmt11.setString(6, company6);
                        stmt11.setString(7, description);
                        stmt11.setInt(8, Integer.parseInt(srno));
                        stmt11.setString(9, accjedetailcustomdataref);
                        stmt11.setString(10, accjedetailproductcustomdataref);
                        stmt11.setInt(11, Integer.parseInt(accountpersontype));
                        stmt11.setDouble(12, Double.parseDouble(gstcurrencyrate6));
                        stmt11.setDouble(13, Double.parseDouble(forexgainloss));
                        stmt11.setInt(14, Integer.parseInt(paymenttype));
                        stmt11.setBoolean(15, Boolean.parseBoolean(isbankcharge));
                        stmt11.execute();

                    }
                    

                    }
                
               
                
                
                 String query7 = "insert into invoice (id,invoicenumber,autogen,duedate ,deleteflag ,journalentry ,company,centry ,tax ,taxentry ,"
                            + "currency ,exchangeratedetail, externalcurrencyrate , customer , parentinvoice , repeateinvoice , partialinv , "
                            + "templateid , pendingapproval , favouriteflag , inventoryorderid , approvallevel , approver , istemplate , "
                            + "mastersalesperson , seqnumber , seqformat, termid , account, createdby  , modifiedby , createdon , updatedon , "
                            + "isopeningbalenceinvoice , originalopeningbalanceamount , openingbalanceamountdue , porefdate , lastmodifieddate , "
                            + "creationdate , isnormalinvoice , exchangerateforopeningtransaction , cashtransaction , billingshippingaddresses , "
                            + "printedflag , isfixedassetinvoice , isfixedassetleaseinvoice , shiplength , invoicetype , gstincluded , "
                            + "isconversionratefromcurrencytobase , invoiceamountdue , originalopeningbalancebaseamount , openingbalancebaseamountdue , "
                            + "custWarehouse , isconsignment , deliverytime , baddebttype , claimedperiod , debtclaimeddate , paydetail , "
                            + "gstcurrencyrate , islinkedwithtaxapplieddo , termsincludegst , movementtype , approvestatuslevel , isdraft , "
                            + "isfromPOS , isopensr , isopendo) "
                            + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
                    PreparedStatement stmt7 = conn.prepareStatement(query7);
                    stmt7.setString(1, invoiceId);
                    stmt7.setString(2, "TX0000"+ seqNum);
                    stmt7.setString(3, autogen);
                    Calendar c = Calendar.getInstance();
                    c.setTime(sqlDate); // Now use today date.
                    c.add(Calendar.DATE, 15); // Adding 5 days
                    //java.sql.Timestamp  dueDate = new java.sql.Timestamp(c.getTime());
                    stmt7.setTimestamp(4, new java.sql.Timestamp(c.getTimeInMillis()));//duedate
                    stmt7.setString(5, deleteflag);
                    stmt7.setString(6, journalEntryId);
                    stmt7.setString(7, company);
                    stmt7.setString(8, newCentryID);
                    stmt7.setString(9, tax);
                    stmt7.setString(10, newtaxentryID);
                    stmt7.setString(11, currency);
                    stmt7.setString(12, exchangeratedetail);
                    stmt7.setDouble(13, Double.parseDouble(externalcurrencyrate));
                    stmt7.setString(14, customer);
                    stmt7.setString(15, parentinvoice);
                    stmt7.setString(16, repeateinvoice);
                    stmt7.setString(17, partialinv);
                    stmt7.setString(18, templateid);
                    stmt7.setString(19, pendingapproval);
                    stmt7.setString(20, favouriteflag);
                    stmt7.setString(21, inventoryorderid);
                    stmt7.setInt(22, Integer.parseInt(approvallevel));
                    stmt7.setString(23, approver);
                    stmt7.setInt(24, Integer.parseInt(istemplate));
                    stmt7.setString(25, mastersalesperson);
                    stmt7.setInt(26, seqNum);
                    stmt7.setString(27, "4028818a4ff4b87c01503703da0e50c8");//seqformat
                    stmt7.setString(28, termid);
                    stmt7.setString(29, account);
                    stmt7.setString(30, createdby);
                    stmt7.setString(31, modifiedby);
                    stmt7.setLong(32, Long.parseLong(createdon));
                    stmt7.setLong(33, Long.parseLong(updatedon));
                    stmt7.setInt(34, Integer.parseInt(isopeningbalenceinvoice));
                    stmt7.setDouble(35, Double.parseDouble(originalopeningbalanceamount));
                    stmt7.setDouble(36, Double.parseDouble(openingbalanceamountdue));
                    stmt7.setTimestamp(37, sqlDate);//porefdate
                    stmt7.setTimestamp(38, sqlDate);//lastmodifieddate
                    stmt7.setTimestamp(39, sqlDate); //creationdate
                    stmt7.setInt(40, Integer.parseInt(isnormalinvoice));
                    stmt7.setDouble(41, Double.parseDouble(exchangerateforopeningtransaction));
                    stmt7.setBoolean(42, Boolean.parseBoolean(cashtransaction));
                    stmt7.setString(43, billingshippingaddresses);
                    stmt7.setBoolean(44, Boolean.parseBoolean(printedflag));
                    stmt7.setBoolean(45, Boolean.parseBoolean(isfixedassetinvoice));
                    stmt7.setBoolean(46, Boolean.parseBoolean(isfixedassetleaseinvoice));
                    stmt7.setDouble(47, Double.parseDouble(shiplength));
                    stmt7.setString(48, invoicetype);
                    stmt7.setString(49, gstincluded);
                    stmt7.setString(50, isconversionratefromcurrencytobase);
                    stmt7.setDouble(51, Double.parseDouble(invoiceamountdue));
                    stmt7.setDouble(52, Double.parseDouble(originalopeningbalancebaseamount));
                    stmt7.setDouble(53, Double.parseDouble(openingbalancebaseamountdue));
                    stmt7.setString(54, custWarehouse);
                    stmt7.setString(55, isconsignment);
                    stmt7.setString(56, deliverytime);
                    stmt7.setInt(57, Integer.parseInt(baddebttype));
                    stmt7.setInt(58, Integer.parseInt(claimedperiod));
                    stmt7.setTimestamp(59, null);
                    stmt7.setString(60, paydetail);
                    stmt7.setString(61, gstcurrencyrate);
                    stmt7.setBoolean(62, Boolean.parseBoolean(islinkedwithtaxapplieddo));
                    stmt7.setString(63, termsincludegst);
                    stmt7.setString(64, movementtype);
                    stmt7.setInt(65, Integer.parseInt(approvestatuslevel));
                    stmt7.setInt(66, Integer.parseInt(isdraft));
                    stmt7.setInt(67, Integer.parseInt(isfromPOS));
                    stmt7.setString(68, isopensr);
                    stmt7.setString(69, isopendo);
                    stmt7.execute();
                    
                    
                    
                String query3 = "select  id ,rate , invoice , company , srno , partamount , description , rowtaxamount , wasrowtaxfieldeditable , deferredjedetailid, gstcurrencyrate , rateincludegst , salesjedid from invoicedetails where invoice= ? ";
                PreparedStatement stmt3 = conn.prepareStatement(query3);
                stmt3.setObject(1, id);
                ResultSet rs3 = stmt3.executeQuery();
                while (rs3.next()) {
                        String id1 = rs3.getString("id");
                        String invoicedetailID = java.util.UUID.randomUUID().toString();
                        String rate = rs3.getString("rate");
                        String invoice = rs3.getString("invoice");
                        String company1 = rs3.getString("company");
                        String srno = rs3.getString("srno");
                        String partamount = rs3.getString("partamount");
                        String description = rs3.getString("description");
                        String rowtaxamount = rs3.getString("rowtaxamount");
                        String wasrowtaxfieldeditable = rs3.getString("wasrowtaxfieldeditable");
                        String deferredjedetailid = rs3.getString("deferredjedetailid");
                        String gstcurrencyrate1 = rs3.getString("gstcurrencyrate");
                        String rateincludegst = rs3.getString("rateincludegst");
                        String salesjedid = rs3.getString("salesjedid");
                        
                        
                        
                        
                        
                        
                        String query4 = " select id , description , quantity , uom, baseuomquantity , baseuomrate , carryin , defective , product , company, newinv , deleteflag , updatedate , actquantity , invrecord , isopening , isopeninginv , leaseflag , consignuomquantity , isconsignment, venconsignuomquantity from inventory where id=?";
                        PreparedStatement stmt4 = conn.prepareStatement(query4);
                        stmt4.setObject(1, id1);
                        ResultSet rs4 = stmt4.executeQuery();
                        while (rs4.next()) {
                                 String	id4	=	rs4.getString("id");
                                String description4 = rs4.getString("description");
                                String quantity = rs4.getString("quantity");
                                String uom = rs4.getString("uom");
                                String baseuomquantity = rs4.getString("baseuomquantity");
                                String baseuomrate = rs4.getString("baseuomrate");
                                String carryin = rs4.getString("carryin");
                                String defective = rs4.getString("defective");
                                String product = rs4.getString("product");
                                String company4 = rs4.getString("company");
                                String newinv = rs4.getString("newinv");
                                String deleteflag4 = rs4.getString("deleteflag");
                                String updatedate = rs4.getString("updatedate");
                                String actquantity = rs4.getString("actquantity");
                                String invrecord = rs4.getString("invrecord");
                                String isopening = rs4.getString("isopening");
                                String isopeninginv = rs4.getString("isopeninginv");
                                String leaseflag = rs4.getString("leaseflag");
                                String consignuomquantity = rs4.getString("consignuomquantity");
                                String isconsignment4 = rs4.getString("isconsignment");
                                String venconsignuomquantity = rs4.getString("venconsignuomquantity");
                                
                                
                                String query9 = "insert into inventory (id , description , quantity , uom, baseuomquantity , baseuomrate , "
                                        + "carryin , defective , product , company, newinv , deleteflag , updatedate , actquantity , invrecord , "
                                        + "isopening , isopeninginv , leaseflag , consignuomquantity , isconsignment, venconsignuomquantity )  "
                                        + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
                                PreparedStatement stmt9 = conn.prepareStatement(query9);
                                stmt9.setString(1, invoicedetailID);
                                stmt9.setString(2, description4);
                                stmt9.setDouble(3, Double.parseDouble(quantity));
                                stmt9.setString(4, uom);
                                stmt9.setDouble(5, Double.parseDouble(baseuomquantity));
                                stmt9.setDouble(6, Double.parseDouble(baseuomrate));
                                stmt9.setString(7, carryin);
                                stmt9.setString(8, defective);
                                stmt9.setString(9, product);
                                stmt9.setString(10, company4);
                                stmt9.setString(11, newinv);
                                stmt9.setString(12, deleteflag4);
                                stmt9.setTimestamp(13, sqlDate);//updatedate
                                stmt9.setDouble(14, Double.parseDouble(actquantity));
                                stmt9.setString(15, invrecord);
                                stmt9.setString(16, isopening);
                                stmt9.setString(17, isopeninginv);
                                stmt9.setBoolean(18, Boolean.parseBoolean(leaseflag));
                                stmt9.setDouble(19, Double.parseDouble(consignuomquantity));
                                stmt9.setString(20, isconsignment4);
                                stmt9.setDouble(21, Double.parseDouble(venconsignuomquantity));
                                stmt9.execute();

                        }
                        
                        String query8 = "insert into invoicedetails (id ,rate , invoice , company , srno , partamount , description , rowtaxamount,"
                                + " wasrowtaxfieldeditable , deferredjedetailid, gstcurrencyrate , rateincludegst , salesjedid)  "
                                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?) ";
                        PreparedStatement stmt8 = conn.prepareStatement(query8);
                        stmt8.setString(1, invoicedetailID);
                        stmt8.setDouble(2, Double.parseDouble(rate));
                        stmt8.setString(3, invoiceId);
                        stmt8.setString(4, company1);
                        stmt8.setInt(5, Integer.parseInt(srno));
                        stmt8.setDouble(6, Double.parseDouble(partamount));
                        stmt8.setString(7, description);
                        stmt8.setDouble(8, Double.parseDouble(rowtaxamount));
                        stmt8.setInt(9, Integer.parseInt(wasrowtaxfieldeditable));
                        stmt8.setString(10, deferredjedetailid);
                        stmt8.setDouble(11, Double.parseDouble(gstcurrencyrate1));
                        stmt8.setDouble(12, Double.parseDouble(rateincludegst));
                        stmt8.setString(13, salesjedid);
                        stmt8.execute();
                        
                        
                        
                        

                }
                
                
                
                   
                
                    seqNum++;
            }
            
        }
        

         out.println(count +" Records inserted successfully.");
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>
