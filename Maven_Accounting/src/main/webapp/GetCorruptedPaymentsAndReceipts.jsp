<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="com.krawler.common.util.Constants" %>
<%@page import="java.io.*"%>

<%
    Connection conn = null;
    try {

        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);


        String companyId = "";
        String subdomain = "";
        String queryCompany = "";
        String queryPayments = "";
        String queryReceipts = "";

        ResultSet rs = null;
        ResultSet RsPmt = null;
        ResultSet RsTransactions = null;
        PreparedStatement ps = null;
        // Get companies list
        if (!StringUtil.isNullOrEmpty(subDomain)) {
            queryCompany = "select companyid, subdomain from company where subdomain=?";
            ps = conn.prepareStatement(queryCompany);
            ps.setString(1, subDomain);
            rs = ps.executeQuery();
        } else {
            queryCompany = "select companyid, subdomain from company";
            ps = conn.prepareStatement(queryCompany);
            rs = ps.executeQuery();
        }
        out.println("****************************************************************START******************************************************************<br>");
        while (rs.next()) {
            companyId = rs.getString("companyid");
            subdomain = rs.getString("subdomain");
            System.out.println(subdomain);
            if(subdomain.equals("wordspace")){
                subdomain = subdomain;
            }
            String queryForRoundOffDigit = "select amountdigitafterdecimal from compaccpreferences where id = ?";
            ps = conn.prepareStatement(queryForRoundOffDigit);
            ps.setString(1, companyId);
            ResultSet rsRoundOffDigit = ps.executeQuery();
            rsRoundOffDigit.next();
            int roundOffDigit = rsRoundOffDigit.getInt("amountdigitafterdecimal");
            
            String corruptedPayment = "";
            int countPmtNoBlank = 0;
            int countRctNoBlank = 0;
            String corruptedReceipt = "";
            // Get all payments for company
            queryPayments = "select id,paymentnumber,paymentwindowtype,paydetail,journalentry,journalentryforbankinterest,journalentryforbankcharges,depositamount,bankchargesamount,bankinterestamount from payment where isopeningbalencepayment= 0 and deleteflag = 'F' and company = ?";
            ps = conn.prepareStatement(queryPayments);
            ps.setString(1, companyId);
            RsPmt = ps.executeQuery();
            while (RsPmt.next()) {
                String paymentId = RsPmt.getString("id");
                String paymentNumber = RsPmt.getString("paymentnumber");
                String journalentry = RsPmt.getString("journalentry");
                String journalentryforbankinterest = RsPmt.getString("journalentryforbankinterest");
                String journalentryforbankcharges = RsPmt.getString("journalentryforbankcharges");
                String paydetail = RsPmt.getString("paydetail");
                double depositamount = RsPmt.getDouble("depositamount");
                double bankchargesamount = RsPmt.getDouble("bankchargesamount");
                double bankinterestamount = RsPmt.getDouble("bankinterestamount");
                if(bankchargesamount>0 && StringUtil.isNullOrEmpty(journalentryforbankcharges)){
                    depositamount-=bankchargesamount;
                }
                if(bankinterestamount>0 && StringUtil.isNullOrEmpty(journalentryforbankinterest)){
                    depositamount-=bankinterestamount;
                }
                //Check if JE is null
                if (StringUtil.isNullOrEmpty(journalentry)) {
                    corruptedPayment += paymentNumber + ": (Journal Entry Is Null)<br>";
                    continue;
                }
                //Check if payment number is blank
                if (StringUtil.isNullOrEmpty(paymentNumber)) {
                    countPmtNoBlank++;
                    continue;
                }
                // Check if paydetails and payment method is null for payment
                if (StringUtil.isNullOrEmpty(paydetail)) {
                    corruptedPayment += paymentNumber + ": (paydetail Is Null)<br>";
                    continue;
                }

                // Check if debit and credit amounts are same for payment JE
                String jedetailDebitQuery = "";
                String jedetailCreditQuery = "";
                double debitAmt = 0;
                double creditAmt = 0;
                jedetailDebitQuery = "select Round(sum(amount),?) as amount from jedetail where journalEntry = ? and company = ? and debit = 'T'";
                ps = conn.prepareStatement(jedetailDebitQuery);
                ps.setInt(1, roundOffDigit);
                ps.setString(2, journalentry);
                ps.setString(3, companyId);
                RsTransactions = ps.executeQuery();
                RsTransactions.next();
                debitAmt = RsTransactions.getDouble("amount");

                jedetailCreditQuery = "select Round(sum(amount),?) as amount from jedetail where journalEntry = ? and company = ? and debit = 'F'";
                ps = conn.prepareStatement(jedetailCreditQuery);
                ps.setInt(1, roundOffDigit);
                ps.setString(2, journalentry);
                ps.setString(3, companyId);
                RsTransactions = ps.executeQuery();
                RsTransactions.next();
                creditAmt = RsTransactions.getDouble("amount");


                if (Math.abs(debitAmt-creditAmt) >= 0.000001) {
                    corruptedPayment += paymentNumber + ": (JE debit and credit amount does not match. Debit amount ="+debitAmt+" , Creadit amount ="+creditAmt+" )<br>";
                    continue;
                }

                //Check if bank interest JE (if applicable) has debit and credit amounts are same
                if (!StringUtil.isNullOrEmpty(journalentryforbankinterest)) {
                    jedetailDebitQuery = "select Round(sum(amount),?) as amount from jedetail where journalEntry = ? and company = ? and debit = 'T'";
                    ps = conn.prepareStatement(jedetailDebitQuery);
                    ps.setInt(1, roundOffDigit);
                    ps.setString(2, journalentryforbankinterest);
                    ps.setString(3, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    debitAmt = RsTransactions.getInt("amount");

                    jedetailCreditQuery = "select Round(sum(amount),?) as amount from jedetail where journalEntry = ? and company = ? and debit = 'F'";
                    ps = conn.prepareStatement(jedetailCreditQuery);
                    ps.setInt(1, roundOffDigit);
                    ps.setString(2, journalentryforbankinterest);
                    ps.setString(3, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    creditAmt = RsTransactions.getInt("amount");

                    if (Math.abs(debitAmt-creditAmt) >= 0.000001) {
                        corruptedPayment += paymentNumber + ": (Bank Interest JE debit and credit amount does not match. Debit amount ="+debitAmt+" , Creadit amount ="+creditAmt+" )<br>";
                        continue;
                    }

                }

                //Check if bank charges JE (if applicable) has debit and credit amounts are same
                if (!StringUtil.isNullOrEmpty(journalentryforbankcharges)) {
                    jedetailDebitQuery = "select Round(sum(amount),?) as amount from jedetail where journalEntry = ? and company = ? and debit = 'T'";
                    ps = conn.prepareStatement(jedetailDebitQuery);
                    ps.setInt(1, roundOffDigit);
                    ps.setString(2, journalentryforbankcharges);
                    ps.setString(3, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    debitAmt = RsTransactions.getInt("amount");

                    jedetailCreditQuery = "select Round(sum(amount),?) as amount from jedetail where journalEntry = ? and company = ? and debit = 'F'";
                    ps = conn.prepareStatement(jedetailCreditQuery);
                    ps.setInt(1, roundOffDigit);
                    ps.setString(2, journalentryforbankcharges);
                    ps.setString(3, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    creditAmt = RsTransactions.getInt("amount");

                    if (Math.abs(debitAmt-creditAmt) >= 0.000001) {
                        corruptedPayment += paymentNumber + ": (Bank Charges JE debit and credit amount does not match. Debit amount ="+debitAmt+" , Creadit amount ="+creditAmt+" )<br>";
                        continue;
                    }

                }

                /*
                 * Check if payment has entry in atleast in one table of -
                 * payment details,adavncedetails,paymentdetailsotherwise and
                 * creditnotpayment If payment has no entry in any of this, it
                 * is corrupted
                 */

                String invoiceQuery = "";
                double invoicesTotal=0.0;
                String cnQuery = "";
                double cnTotal=0.0;
                String glQuery = "";
                double glTotal=0.0;
                String advanceQuery = "";
                double advanceTotal=0.0;
                double taxamountTotal=0.0;
                int paymentWindowType = RsPmt.getInt("paymentwindowtype");
                int tempCount = 0;
                int count = 0;

                if (paymentWindowType == 1) {
                    invoiceQuery = "select count(id) as count, SUM(amount) as invoicesTotal from paymentdetail where payment = ? and company= ?";
                    ps = conn.prepareStatement(invoiceQuery);
                    ps.setString(1, paymentId);
                    ps.setString(2, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    invoicesTotal = RsTransactions.getDouble("invoicesTotal");
                    if (count > 0) {
                        tempCount += count;
                    }

                    cnQuery = "select count(id) as count, SUM(amountpaid) as cnTotal from creditnotpayment where paymentid = ?";
                    ps = conn.prepareStatement(cnQuery);
                    ps.setString(1, paymentId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    cnTotal = RsTransactions.getDouble("cnTotal");
                    if (count > 0) {
                        tempCount += count;
                    }

                    glQuery = "select count(id) as count, SUM(amount) as glTotal, SUM(taxamount) as taxamountTotal from paymentdetailotherwise where payment = ?";
                    ps = conn.prepareStatement(glQuery);
                    ps.setString(1, paymentId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    glTotal = RsTransactions.getDouble("glTotal");
                    taxamountTotal = RsTransactions.getDouble("taxamountTotal");
                    if (count > 0) {
                        tempCount += count;
                    }

                    advanceQuery = "select count(id) as count, SUM(amount) as advanceTotal from advancedetail where payment = ? and company = ?";
                    ps = conn.prepareStatement(advanceQuery);
                    ps.setString(1, paymentId);
                    ps.setString(2, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    advanceTotal = RsTransactions.getDouble("advanceTotal");
                    if (count > 0) {
                        tempCount += count;
                    }
                    if (tempCount == 0) {
                        corruptedPayment += paymentNumber + ": (Line level details are corrupted)<br>";
                    }
                } else if (paymentWindowType == 2) {
                    cnQuery = "select count(id) as count, SUM(amountpaid) as cnTotal from creditnotpayment where paymentid = ?";
                    ps = conn.prepareStatement(cnQuery);
                    ps.setString(1, paymentId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    cnTotal = RsTransactions.getDouble("cnTotal");
                    if (count > 0) {
                        tempCount += count;
                    }
                    advanceQuery = "select count(id) as count, SUM(amount) as advanceTotal from advancedetail where payment = ? and company = ?";
                    ps = conn.prepareStatement(advanceQuery);
                    ps.setString(1, paymentId);
                    ps.setString(2, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    advanceTotal = RsTransactions.getDouble("advanceTotal");
                    if (count > 0) {
                        tempCount += count;
                    }
                    if (tempCount == 0) {
                        corruptedPayment += paymentNumber + ": (Line level details are corrupted)<br>";
                    }
                } else if (paymentWindowType == 3) {
                    glQuery = "select count(id) as count, SUM(amount) as glTotal, SUM(taxamount) as taxamountTotal from paymentdetailotherwise where payment = ?";
                    ps = conn.prepareStatement(glQuery);
                    ps.setString(1, paymentId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    glTotal = RsTransactions.getDouble("glTotal");
                    taxamountTotal = RsTransactions.getDouble("taxamountTotal");
                    if (count > 0) {
                        tempCount += count;
                    }
                    if (tempCount == 0) {
                        corruptedPayment += paymentNumber + ": (Line level details are corrupted)<br>";
                    }
                } else {
                    corruptedPayment += paymentNumber + "<br>";
                }
                double totalAmountPaid = invoicesTotal+cnTotal+advanceTotal+glTotal+taxamountTotal;
                if(totalAmountPaid != depositamount){
                    corruptedPayment += paymentNumber + ": (Deposit amount does not match with total line level amount)<br>";
                }
            }

            // Get all receipts for company
            queryReceipts = "select id,receiptnumber,paymentwindowtype,paydetail,journalentry,journalentryforbankinterest,journalentryforbankcharges,depositamount from receipt where isopeningbalencereceipt = 0 and deleteflag = 'F' and company = ?";
            ps = conn.prepareStatement(queryReceipts);
            ps.setString(1, companyId);
            RsPmt = ps.executeQuery();

            while (RsPmt.next()) {
                String receiptId = RsPmt.getString("id");
                String receiptNumber = RsPmt.getString("receiptnumber");
                String journalentry = RsPmt.getString("journalentry");
                String journalentryforbankinterest = RsPmt.getString("journalentryforbankinterest");
                String journalentryforbankcharges = RsPmt.getString("journalentryforbankcharges");
                String paydetail = RsPmt.getString("paydetail");
                double depositamount = RsPmt.getDouble("depositamount");
                //Check if JE is null                
                if (StringUtil.isNullOrEmpty(journalentry)) {
                    corruptedPayment += receiptNumber + ": (Journal Entry Is Null)<br>";
                    continue;
                }
                //Check if receipt number is blank
                if (StringUtil.isNullOrEmpty(receiptNumber)) {
                    countRctNoBlank++;
                    continue;
                }
                // Check if paydetails and payment method is null for receipt
                if (StringUtil.isNullOrEmpty(paydetail)) {
                    corruptedReceipt += receiptNumber + ": (paydetail Is Null)<br>";
                    continue;
                }

                // Check if debit and credit amounts are same for receipt JE
                String jedetailDebitQuery = "";
                String jedetailCreditQuery = "";
                double debitAmt = 0;
                double creditAmt = 0;
                jedetailDebitQuery = "select Round(sum(amount),?) as amount from jedetail where journalEntry = ? and company = ? and debit = 'T'";
                ps = conn.prepareStatement(jedetailDebitQuery);
                ps.setInt(1, roundOffDigit);
                ps.setString(2, journalentry);
                ps.setString(3, companyId);
                RsTransactions = ps.executeQuery();
                RsTransactions.next();
                debitAmt = RsTransactions.getDouble("amount");
                
                jedetailCreditQuery = "select Round(sum(amount),?) as amount from jedetail where journalEntry = ? and company = ? and debit = 'F'";
                ps = conn.prepareStatement(jedetailCreditQuery);
                ps.setInt(1, roundOffDigit);
                ps.setString(2, journalentry);
                ps.setString(3, companyId);
                RsTransactions = ps.executeQuery();
                RsTransactions.next();
                creditAmt = RsTransactions.getDouble("amount");

                if (Math.abs(debitAmt-creditAmt) >= 0.000001) {
                    corruptedReceipt += receiptNumber + ": (JE debit and credit amount does not match. Debit amount ="+debitAmt+" , Creadit amount ="+creditAmt+" )<br>";
                    continue;
                }

                //Check if bank interest JE (if applicable) has debit and credit amounts are same
                if (!StringUtil.isNullOrEmpty(journalentryforbankinterest)) {
                    jedetailDebitQuery = "select Round(sum(amount),?) as amount from jedetail where journalEntry = ? and company = ? and debit = 'T'";
                    ps = conn.prepareStatement(jedetailDebitQuery);
                    ps.setInt(1, roundOffDigit);
                    ps.setString(2, journalentryforbankinterest);
                    ps.setString(3, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    debitAmt = RsTransactions.getInt("amount");

                    jedetailCreditQuery = "select Round(sum(amount),?) as amount from jedetail where journalEntry = ? and company = ? and debit = 'F'";
                    ps = conn.prepareStatement(jedetailCreditQuery);
                    ps.setInt(1, roundOffDigit);
                    ps.setString(2, journalentryforbankinterest);
                    ps.setString(3, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    creditAmt = RsTransactions.getInt("amount");

                    if (Math.abs(debitAmt-creditAmt) >= 0.000001) {
                        corruptedReceipt += receiptNumber + ": (Bank Interest JE debit and credit amount does not match. Debit amount ="+debitAmt+" , Creadit amount ="+creditAmt+" )<br>";
                        continue;
                    }

                }

                //Check if bank charges JE (if applicable) has debit and credit amounts are same
                if (!StringUtil.isNullOrEmpty(journalentryforbankcharges)) {
                    jedetailDebitQuery = "select Round(sum(amount),?) as amount from jedetail where journalEntry = ? and company = ? and debit = 'T'";
                    ps = conn.prepareStatement(jedetailDebitQuery);
                    ps.setInt(1, roundOffDigit);
                    ps.setString(2, journalentryforbankcharges);
                    ps.setString(3, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    debitAmt = RsTransactions.getInt("amount");

                    jedetailCreditQuery = "select Round(sum(amount),?) as amount from jedetail where journalEntry = ? and company = ? and debit = 'F'";
                    ps = conn.prepareStatement(jedetailCreditQuery);
                    ps.setInt(1, roundOffDigit);
                    ps.setString(2, journalentryforbankcharges);
                    ps.setString(3, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    creditAmt = RsTransactions.getInt("amount");

                    if (Math.abs(debitAmt-creditAmt) >= 0.000001) {
                        corruptedReceipt += receiptNumber + ": (Bank Charges JE debit and credit amount does not match. Debit amount ="+debitAmt+" , Creadit amount ="+creditAmt+" )<br>";
                        continue;
                    }

                }

                /*
                 * Check if payment has entry in atleast in one table of -
                 * payment details,adavncedetails,paymentdetailsotherwise and
                 * creditnotpayment If payment has no entry in any of this, it
                 * is corrupted
                 */
                String invoiceQuery = "";
                double invoicesTotal=0.0;
                String dnQuery = "";
                double dnTotal=0.0;
                String glQuery = "";
                double glTotal=0.0;
                String advanceQuery = "";
                double advanceTotal=0.0;
                double taxamountTotal=0.0;
                int paymentWindowType = RsPmt.getInt("paymentwindowtype");
                int tempCount = 0;
                int count = 0;
                if (paymentWindowType == 1) {
                    invoiceQuery = "select count(id) as count, SUM(amount) as invoicesTotal from receiptdetails where receipt = ? and company = ?";
                    ps = conn.prepareStatement(invoiceQuery);
                    ps.setString(1, receiptId);
                    ps.setString(2, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    invoicesTotal = RsTransactions.getDouble("invoicesTotal");
                    if (count > 0) {
                        tempCount += count;
                    }

                    dnQuery = "select count(id) as count, SUM(amountpaid) as dnTotal from debitnotepayment where receiptid = ?";
                    ps = conn.prepareStatement(dnQuery);
                    ps.setString(1, receiptId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    dnTotal = RsTransactions.getDouble("dnTotal");
                    if (count > 0) {
                        tempCount += count;
                    }

                    glQuery = "select count(id) as count, SUM(amount) as glTotal, SUM(taxamount) as taxamountTotal from receiptdetailotherwise where receipt = ?";
                    ps = conn.prepareStatement(glQuery);
                    ps.setString(1, receiptId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    glTotal = RsTransactions.getDouble("glTotal");
                    taxamountTotal = RsTransactions.getDouble("taxamountTotal");
                    if (count > 0) {
                        tempCount += count;
                    }

                    advanceQuery = "select count(id) as count, SUM(amount) as advanceTotal from receiptadvancedetail where receipt = ? and company = ?";
                    ps = conn.prepareStatement(advanceQuery);
                    ps.setString(1, receiptId);
                    ps.setString(2, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    advanceTotal = RsTransactions.getDouble("advanceTotal");
                    if (count > 0) {
                        tempCount += count;
                    }
                    if (tempCount == 0) {
                        corruptedReceipt += receiptNumber + ": (Line level details are corrupted)<br>";
                    }
                } else if (paymentWindowType == 2) {
                    dnQuery = "select count(id) as count, SUM(amountpaid) as dnTotal from debitnotepayment where receiptid = ?";
                    ps = conn.prepareStatement(dnQuery);
                    ps.setString(1, receiptId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    dnTotal = RsTransactions.getDouble("dnTotal");
                    if (count > 0) {
                        tempCount += count;
                    }
                    advanceQuery = "select count(id) as count, SUM(amount) as advanceTotal from receiptadvancedetail where receipt = ? and company = ?";
                    ps = conn.prepareStatement(advanceQuery);
                    ps.setString(1, receiptId);
                    ps.setString(2, companyId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    advanceTotal = RsTransactions.getDouble("advanceTotal");
                    if (count > 0) {
                        tempCount += count;
                    }
                    if (tempCount == 0) {
                        corruptedReceipt += receiptNumber + ": (Line level details are corrupted)<br>";
                    }
                } else if (paymentWindowType == 3) {
                    glQuery = "select count(id) as count, SUM(amount) as glTotal, SUM(taxamount) as taxamountTotal from receiptdetailotherwise where receipt = ?";
                    ps = conn.prepareStatement(glQuery);
                    ps.setString(1, receiptId);
                    RsTransactions = ps.executeQuery();
                    RsTransactions.next();
                    count = RsTransactions.getInt("count");
                    glTotal = RsTransactions.getDouble("glTotal");
                    taxamountTotal = RsTransactions.getDouble("taxamountTotal");
                    if (count > 0) {
                        tempCount += count;
                    }
                    if (tempCount == 0) {
                        corruptedReceipt += receiptNumber + ": (Line level details are corrupted)<br>";
                    }
                } else {
                    corruptedReceipt += receiptNumber + "<br>";
                }
                double totalAmountReceived = invoicesTotal+dnTotal+advanceTotal+glTotal+taxamountTotal;
                if(totalAmountReceived != depositamount){
                    corruptedReceipt += receiptNumber + ": (Deposit amount does not match with total line level amount)<br>";
                }
            }
            if (!corruptedPayment.equals("") || !corruptedReceipt.equals("") || (countPmtNoBlank != 0) || (countRctNoBlank != 0)) {
                out.println("================================SUBDOMAIN : " + subdomain + "===========================================" + "<br>");
            }
            if (corruptedPayment != "") {
                out.println("<b>Corrupted payments found :</b>" + "<br>" + corruptedPayment + "<br>");
            }
            if (corruptedReceipt != "") {
                out.println("<b>Corrupted receipts found : </b>" + "<br>" + corruptedReceipt + "<br>");
            }
            if (countPmtNoBlank != 0) {
                out.println("<b>Payments found with payment number blank : </b>" + countPmtNoBlank + "<br>");
            }
            if (countRctNoBlank != 0) {
                out.println("<b>Receipts found with payment number blank : </b>" + countRctNoBlank + "<br>");
            }
            if (!corruptedPayment.equals("") || !corruptedReceipt.equals("")) {
                out.println("=============================COMPLETED FOR SUBDOMAIN : " + subdomain + "======================================" + "<br><br>");
            }


        }
        out.println("************************************************************FINISH*******************************************************************************");
        ps.close();
        if (RsTransactions != null) {
            RsTransactions.close();
        }
        if (RsPmt != null) {
            RsPmt.close();
        }
        if (rs != null) {
            rs.close();
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
