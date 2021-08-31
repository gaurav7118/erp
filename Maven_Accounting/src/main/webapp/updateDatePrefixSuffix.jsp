
<%@page import="com.krawler.common.util.Constants"%>
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
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname FROM company ";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query += " where subdomain= ?";
        }
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalDocumentUpdationCnt = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");
            String companyname = rs.getString("companyname");

            query = "select id,name,dateformatinprefix,dateformataftersuffix,moduleid from sequenceformat where (isdatebeforeprefix='T' OR showdateformataftersuffix='T') and  deleted='F' and company=? "; //Build Assembly moduleid is 120
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                String sequnceformatid = rs1.getString("id");
                String sequnceformatname = rs1.getString("name");
                String dateformatinprefix = rs1.getString("dateformatinprefix")==null?"":rs1.getString("dateformatinprefix");
                String dateformataftersuffix = rs1.getString("dateformataftersuffix")==null?"":rs1.getString("dateformataftersuffix");
                int moduleID = rs1.getInt("moduleid");
                String sqltable = "";
                String sqlfield = "";
                switch (moduleID) {
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
                if (!StringUtil.isNullOrEmpty(sqltable)) {
                    String selectTransactionQuery = "select id," + sqlfield + " from " + sqltable + " where  datepreffixvalue ='' and datesuffixvalue='' and seqformat = ? and company=? ";
                    PreparedStatement stmt2 = conn.prepareStatement(selectTransactionQuery);
                    stmt2.setString(1, sequnceformatid);
                    stmt2.setString(2, companyId);
                    ResultSet rs2 = stmt2.executeQuery();
                    while (rs2.next()) {
                        String documentID = rs2.getString("id");
                        String entryNumber = rs2.getString(sqlfield);
                        if (entryNumber.length()>0 && entryNumber.length() == (dateformatinprefix.length() + sequnceformatname.length() + dateformataftersuffix.length())) {
                            String prefixDateValue=entryNumber.substring(0,dateformatinprefix.length());
                            String suffixDateValue=entryNumber.substring((dateformatinprefix.length()+sequnceformatname.length()),entryNumber.length());
                            if(!StringUtil.isNullOrEmpty(prefixDateValue) || !StringUtil.isNullOrEmpty(suffixDateValue)){//if both are empty then no need to update
                                String updateQuery="update "+sqltable+" set datepreffixvalue=?,datesuffixvalue=? where id=?";
                                PreparedStatement updatestmt = conn.prepareStatement(updateQuery);
                                updatestmt.setString(1, prefixDateValue);
                                updatestmt.setString(2, suffixDateValue);
                                updatestmt.setString(3, documentID);
                                updatestmt.execute(); 
                                totalDocumentUpdationCnt++;
                            }
                        }
                    }
                }
            }
        }
        if (totalDocumentUpdationCnt == 0) {
            out.println("<br><br> Total Record updated are " + totalDocumentUpdationCnt);
            out.println("<br><br> Either Script Already Executed or No such records to update.");
        } else {
            out.println("<br><br> Total Record updated are " + totalDocumentUpdationCnt);
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



