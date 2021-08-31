
<%@page import="java.text.*"%>
<%@page import="com.krawler.common.util.*"%>
<!--%@page contentType="text/html" pageEncoding="UTF-8"%>-->
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.*"%>



<%
        Connection conn = null;
        try {
            String serverip = request.getParameter("serverip");
            String port = "3306";
            String dbName = request.getParameter("dbname");
            String userName = request.getParameter("username");//"krawlersqladmin";
            String password = request.getParameter("password"); //"krawler"
            String subdomain = request.getParameter("subdomain");

            if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName)) {
                throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
            }
            String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            String driver = "com.mysql.jdbc.Driver";

            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectString, userName, password);
            String query = "select companyid,companyname,subdomain FROM company ";
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                query += " where subdomain= ?";
            }
            PreparedStatement stmt = conn.prepareStatement(query);
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                stmt.setString(1, subdomain);
            }
            //int a=0;
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String companyId = rs.getString("companyid");
                String companyname = rs.getString("companyname");
                subdomain = rs.getString("subdomain");
                int totalDocumentUpdationCnt = 0;

                List<Integer> moduleList = new ArrayList();
                moduleList.add(Constants.Acc_GENERAL_LEDGER_ModuleId);
                moduleList.add(Constants.Acc_Sales_Order_ModuleId);
                moduleList.add(Constants.Acc_Invoice_ModuleId);
                moduleList.add(Constants.Acc_Cash_Sales_ModuleId);
                moduleList.add(Constants.Acc_Credit_Note_ModuleId);
                moduleList.add(Constants.Acc_Receive_Payment_ModuleId);
                moduleList.add(Constants.Acc_Purchase_Order_ModuleId);
                moduleList.add(Constants.Acc_Vendor_Invoice_ModuleId);
                moduleList.add(Constants.Acc_Cash_Purchase_ModuleId);
                moduleList.add(Constants.Acc_Debit_Note_ModuleId);
                moduleList.add(Constants.Acc_Make_Payment_ModuleId);
                moduleList.add(Constants.Acc_Contract_Order_ModuleId);
                moduleList.add(Constants.Acc_Customer_Quotation_ModuleId);
                moduleList.add(Constants.Acc_Vendor_Quotation_ModuleId);
                for (int moduleid: moduleList) {
                  // int moduleid = moduleID;
                  //  int moduleid = 2;
                    String querytogetsequenceformat = "select id,prefix,suffix,name,numberofdigit,startfrom,isdatebeforeprefix,dateformatinprefix,showdateformataftersuffix,dateformataftersuffix,isdateafterprefix,dateformatafterprefix from sequenceformat where  company= ? and  deleted='F' and moduleid=?";
                    PreparedStatement stmt1 = conn.prepareStatement(querytogetsequenceformat);
                    stmt1.setString(1, companyId);
                    stmt1.setInt(2, moduleid);
                    ResultSet sequenceFormatResult = stmt1.executeQuery();
                    while (sequenceFormatResult.next()) {
                        String sequnceformatid = sequenceFormatResult.getString("id");
                        String prefix = sequenceFormatResult.getString("prefix");
                        prefix = prefix.toLowerCase();
                        String suffix = sequenceFormatResult.getString("suffix");
                        suffix = suffix.toLowerCase();
                        String sequenceformatname = sequenceFormatResult.getString("name");
                        int numberofdigit = sequenceFormatResult.getInt("numberofdigit");
                        String startfrom = sequenceFormatResult.getString("startfrom");
                        boolean isDateBeforePrefix = sequenceFormatResult.getBoolean("isdatebeforeprefix");
                        boolean isDateAfterSuffix = sequenceFormatResult.getBoolean("showdateformataftersuffix");
                        boolean isDateAfterPrefix = sequenceFormatResult.getBoolean("isdateafterprefix");
                        String selecteddateformatBeforePrefix = sequenceFormatResult.getString("dateformatinprefix") == null ? "" : sequenceFormatResult.getString("dateformatinprefix");
                        String dateformataftersuffix = sequenceFormatResult.getString("dateformataftersuffix") == null ? "" : sequenceFormatResult.getString("dateformataftersuffix");
                        String dateFormatAfterPrefix = sequenceFormatResult.getString("dateformatafterprefix") == null ? "" : sequenceFormatResult.getString("dateformatafterprefix");
                        int maxSequenceNumber = 1;
                        
                        String format = "";
                        for (int f = 0; f < numberofdigit; f++) {
                            format += "0";
                        }

                        String extremeLastNumber = "";

                        for (int i = 0; i < numberofdigit; i++) {
                            extremeLastNumber += "9";
                        }

                        String sequenceformatpattern = selecteddateformatBeforePrefix + prefix + format + dateFormatAfterPrefix + suffix + dateformataftersuffix;
                        //String sequenceformatpattern = prefix + format + suffix;
                        String likecondition = "%" + prefix + "%" + suffix;
                        String sqltable = "";
                        String sqlfield = "";
                        switch (moduleid) {
                            case Constants.Acc_GENERAL_LEDGER_ModuleId:
                                sqltable = "journalentry";
                                sqlfield = "entryno";
                                break;
                            case Constants.Acc_Sales_Order_ModuleId:
                                sqltable = "salesorder";
                                sqlfield = "sonumber";
                                break;
                            case Constants.Acc_Invoice_ModuleId:
                                sqltable = "invoice";
                                sqlfield = "invoicenumber";
                                break;
                            case Constants.Acc_Cash_Sales_ModuleId:
                                sqltable = "invoice";
                                sqlfield = "invoicenumber";
                                break;
                            case Constants.Acc_Credit_Note_ModuleId:
                                sqltable = "creditnote";
                                sqlfield = "cnnumber";
                                break;
                            case Constants.Acc_Receive_Payment_ModuleId:
                                sqltable = "receipt";
                                sqlfield = "receiptnumber";
                                break;
                            case Constants.Acc_Purchase_Order_ModuleId:
                                sqltable = "purchaseorder";
                                sqlfield = "ponumber";
                                break;
                            case Constants.Acc_Vendor_Invoice_ModuleId:
                                sqltable = "goodsreceipt";
                                sqlfield = "grnumber";
                                break;
                            case Constants.Acc_Cash_Purchase_ModuleId:
                                sqltable = "goodsreceipt";
                                sqlfield = "grnumber";
                                break;
                            case Constants.Acc_Debit_Note_ModuleId:
                                sqltable = "debitnote";
                                sqlfield = "dnnumber";
                                break;
                            case Constants.Acc_Make_Payment_ModuleId:
                                sqltable = "payment";
                                sqlfield = "paymentnumber";
                                break;
                            case Constants.Acc_Contract_Order_ModuleId:
                                sqltable = "contract";
                                sqlfield = "contractnumber";
                                break;
                            case Constants.Acc_Customer_Quotation_ModuleId:
                                sqltable = "quotation";
                                sqlfield = "quotationnumber";
                                break;
                            case Constants.Acc_Vendor_Quotation_ModuleId:
                                sqltable = "vendorquotation";
                                sqlfield = "quotationnumber";
                                break;
                            case Constants.Acc_Purchase_Requisition_ModuleId:
                                sqltable = "purchaserequisition";
                                sqlfield = "prnumber";
                                break;
                            case Constants.Acc_RFQ_ModuleId:
                                sqltable = "requestforquotation";
                                sqlfield = "rfqnumber";
                                break;
                            case Constants.Acc_Product_Master_ModuleId:
                                sqltable = "product";
                                sqlfield = "productid";
                                break;
                            case Constants.Acc_Delivery_Order_ModuleId:
                                sqltable = "deliveryorder";
                                sqlfield = "donumber";
                                break;
                            case Constants.Acc_Goods_Receipt_ModuleId:
                                sqltable = "grorder";
                                sqlfield = "gronumber";
                                break;
                            case Constants.Acc_Sales_Return_ModuleId:
                                sqltable = "salesreturn";
                                sqlfield = "srnumber";
                                break;
                            case Constants.Acc_Purchase_Return_ModuleId:
                                sqltable = "purchasereturn";
                                sqlfield = "prnumber";
                                break;
                            case Constants.Acc_Customer_ModuleId:
                                sqltable = "customer";
                                sqlfield = "acccode";
                                break;
                            case Constants.Acc_Vendor_ModuleId:
                                sqltable = "vendor";
                                sqlfield = "acccode";
                                break;
                            case Constants.SALES_BAD_DEBT_CLAIM_ModuleId:
                                sqltable = "baddebtinvoicemapping";
                                sqlfield = "invoice";
                                break;
                            case Constants.SALES_BAD_DEBT_RECOVER_ModuleId:
                                sqltable = "baddebtinvoicemapping";
                                sqlfield = "invoice";
                                break;
                            case Constants.PURCHASE_BAD_DEBT_CLAIM_ModuleId:
                                sqltable = "baddebtpurchaseinvoicemapping";
                                sqlfield = "goodsreceipt";
                                break;
                            case Constants.PURCHASE_BAD_DEBT_RECOVER_ModuleId:
                                sqltable = "baddebtpurchaseinvoicemapping";
                                sqlfield = "goodsreceipt";
                                break;
                            case Constants.Acc_Build_Assembly_Product_ModuleId:
                                sqltable = "productbuild";
                                sqlfield = "refno";
                                break;
                            case Constants.Acc_Loan_Management_ModuleId:
                                sqltable = "disbursement";
                                sqlfield = "loanrefnumber";
                                break;
                        }
                        String querytogetmatchedtransaction = "select id, " + sqlfield + " ,createdon from " + sqltable + " where LENGTH(" + sqlfield + ")=" + sequenceformatpattern.length() + " and " + sqlfield + " like '" + likecondition + "' and seqformat is null and company=?";
                        PreparedStatement stmt2 = conn.prepareStatement(querytogetmatchedtransaction);
                        stmt2.setString(1, companyId);
                        ResultSet transactionResults = stmt2.executeQuery();
                        while (transactionResults.next()) {

                            String trasnactionid = transactionResults.getString(1);
                            String trasnactionNumber = transactionResults.getString(2);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            java.sql.Date transactioncreationDate = null;
                            Long londdate = transactionResults.getLong(3);
                            transactioncreationDate = new java.sql.Date(londdate);




                            String lowerTransactionNumber = trasnactionNumber.toLowerCase();
                            int intPartValue = 0;
                            int intStartFromValue = 0;
                            boolean isSeqnum = false;
                            String afterprefixDateValue = "";
                            String prefixDateValue = "";
                            String suffixDateValue = "";
                            //If any one add more date formats in UI of sequence format needs to add here as well
                            Map<String, String> dataFormatMap = new HashMap<String, String>();
                            dataFormatMap.put("YYYY", "yyyy");
                            dataFormatMap.put("YYYYMM", "yyyyMM");
                            dataFormatMap.put("YYYYMMDD", "yyyyMMdd");
                            dataFormatMap.put("YY", "yy");
                            dataFormatMap.put("YYMM", "yyMM");
                            dataFormatMap.put("YYMMDD", "yyMMdd");
                            if ((isDateBeforePrefix || isDateAfterPrefix || isDateAfterSuffix)) {//if sequnece format have date
                                if (trasnactionNumber.length() == (selecteddateformatBeforePrefix.length() + sequenceformatname.length() + dateformataftersuffix.length())) { //when lenght of number as well as lenght of format with date matches
                                    if (isDateBeforePrefix) {
                                        String datePrefix = lowerTransactionNumber.substring(0, selecteddateformatBeforePrefix.length());

                                        if (dataFormatMap.containsKey(selecteddateformatBeforePrefix.toUpperCase())) {
                                            sdf = new SimpleDateFormat(dataFormatMap.get(selecteddateformatBeforePrefix.toUpperCase()));
                                        }
                                        try {
                                            sdf.setLenient(false);//make date validation more strictly.
                                            sdf.parse(datePrefix);//If datePrefix is sucessfully parsed it means it is datevalue otherwise this number will not generate from this sequence format so continue
                                            prefixDateValue = sdf.format(transactioncreationDate);
                                        } catch (Exception ex) {
                                            continue;
                                        }
                                    }
                                    if (isDateAfterPrefix) {
                                        String dateAfterPrefix = lowerTransactionNumber.substring((selecteddateformatBeforePrefix.length() + prefix.length()), (selecteddateformatBeforePrefix.length() + prefix.length() + dateFormatAfterPrefix.length()));
                                        sdf = new SimpleDateFormat("yyyyMMdd");
                                        if (dataFormatMap.containsKey(dateFormatAfterPrefix.toUpperCase())) {
                                            sdf = new SimpleDateFormat(dataFormatMap.get(dateFormatAfterPrefix.toUpperCase()));
                                        }
                                        try {
                                            sdf.setLenient(false);//make date validation more strictly.
                                            sdf.parse(dateAfterPrefix);//If datePrefix is sucessfully parsed it means it is datevalue otherwise this number will not generate from this sequence format so continue
                                            afterprefixDateValue = sdf.format(transactioncreationDate);
                                        } catch (Exception ex) {
                                            continue;
                                        }
                                    }
                                    if (isDateAfterSuffix) {
                                        String dateSuffix = lowerTransactionNumber.substring((selecteddateformatBeforePrefix.length() + sequenceformatname.length()), lowerTransactionNumber.length());
                                        sdf = new SimpleDateFormat("yyyyMMdd");
                                        if (dataFormatMap.containsKey(dateformataftersuffix.toUpperCase())) {
                                            sdf = new SimpleDateFormat(dataFormatMap.get(dateformataftersuffix.toUpperCase()));
                                        }
                                        try {
                                            sdf.setLenient(false);//make date validation more strictly.
                                            sdf.parse(dateSuffix);//If dateSuffix is sucessfully parsed it means it is datevalue otherwise entrynumber will not generate from this sequence format so continue
                                            suffixDateValue = sdf.format(transactioncreationDate);
                                        } catch (Exception ex) {
                                            continue;
                                        }
                                    }
                                    String lowerEntryNumberWithoutDate = lowerTransactionNumber.substring(selecteddateformatBeforePrefix.length(), (lowerTransactionNumber.length() - dateformataftersuffix.length()));//removed prefix and suffix date
                                    if (lowerEntryNumberWithoutDate.length() == sequenceformatname.length() && lowerEntryNumberWithoutDate.startsWith(prefix) && lowerEntryNumberWithoutDate.endsWith(suffix)) {
                                        String intPart = lowerEntryNumberWithoutDate.substring((prefix.length() + dateFormatAfterPrefix.length()), (lowerEntryNumberWithoutDate.length() - suffix.length()));
                                        try {
                                            intPartValue = Integer.parseInt(intPart);
                                        } catch (Exception ex) {
                                            continue;
                                        }
                                        intStartFromValue = Integer.parseInt(startfrom);
                                        // isSeqnum = true;
                                    }
                                }
                            } else {
                                if (lowerTransactionNumber.length() == sequenceformatname.length() && lowerTransactionNumber.startsWith(prefix) && lowerTransactionNumber.endsWith(suffix)) {
                                    String intPart = trasnactionNumber.substring(prefix.length(), (trasnactionNumber.length() - suffix.length()));
                                    try {
                                        intPartValue = Integer.parseInt(intPart);
                                    } catch (Exception ex) {
                                        continue;
                                    }
                                    intStartFromValue = Integer.parseInt(startfrom);
                                    //isSeqnum = true;
                                }

                            }
                            if (intStartFromValue >= Integer.parseInt(format) && intStartFromValue <= Integer.parseInt(extremeLastNumber)) {
                                isSeqnum = true;
                               // System.out.println("*************************************************");
                              //  System.out.println("updated: " + trasnactionNumber);
                              //  System.out.println("updated: " + sequenceformatname);
                            }else{
                                System.out.println("************************************************");
                                System.out.println("Not updated: " + trasnactionNumber);
                                System.out.println("Not updated: " + sequenceformatname);
                            }

                            if (isSeqnum) {
                                String querytogetmaxsequence = "select max(seqnumber) from " + sqltable + " where seqformat =? and company = ?";
                                PreparedStatement stmt3 = conn.prepareStatement(querytogetmaxsequence);
                                stmt3.setString(1, sequnceformatid);
                                stmt3.setString(2, companyId);
                                ResultSet getmaxsequenceNumberResult = stmt3.executeQuery();
                                
                                if (getmaxsequenceNumberResult.next()) {
                                    maxSequenceNumber = getmaxsequenceNumberResult.getInt(1);
                                    //break;
                                }
                                String updatetrasanctionQuery = "update " + sqltable + " set seqformat=? , seqnumber=? , datepreffixvalue=? , datesuffixvalue=? ,dateafterpreffixvalue=? where id=? order by "+sqlfield+"";
                                PreparedStatement updatestmt = conn.prepareStatement(updatetrasanctionQuery);
                                updatestmt.setString(1, sequnceformatid);
                                updatestmt.setInt(2, maxSequenceNumber + 1);
                                updatestmt.setString(3, prefixDateValue);
                                updatestmt.setString(4, suffixDateValue);
                                updatestmt.setString(5, afterprefixDateValue);
                                updatestmt.setString(6, trasnactionid);
                                updatestmt.execute();
                                totalDocumentUpdationCnt++;

                            }
                        }
                    }
                   // a+=totalDocumentUpdationCnt;
                }
                   out.println("<br><br> **********************"+subdomain+"***********************************************");
                   out.println("<br><br> Total Record updated: " + totalDocumentUpdationCnt+"\n"); 
            //}

        }
        //out.println("<br><br> Total total Record updated: " + a+"\n"); 
       
    } catch (Exception e) {
            e.printStackTrace();
            out.print(e.toString());
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

%>



