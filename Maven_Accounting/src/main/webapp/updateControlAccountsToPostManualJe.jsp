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
        String serverip = request.getParameter("serverip");//"192.168.0.208";                            
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"newstaging";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
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
        int totalCompanyUpdationCnt = 0;
        int totalproductUpdationCnt = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");

            // Update Accounts from Company Preferences

            String discountGiven = "", discountReceived = "", shippingCharges = "", /*otherCharges = "",*/ cashAccount = "", foreignexchange = "", unrealisedgainloss = "", depereciationAccount = "", expenseAccount = "", customerdefaultaccount = "", vendordefaultaccount = "", roundingDifferenceAccount = "", liabilityAccount = "";
            //String querycp = "select discountGiven,discountReceived,shippingCharges,otherCharges,cashAccount,foreignexchange,unrealisedgainloss,depereciationAccount,expenseAccount,customerdefaultaccount,vendordefaultaccount,roundingDifferenceAccount,liabilityAccount from compaccpreferences where id=? ";
            String querycp = "select discountGiven,discountReceived,shippingCharges,cashAccount,foreignexchange,unrealisedgainloss,depereciationAccount,expenseAccount,customerdefaultaccount,vendordefaultaccount,roundingDifferenceAccount,liabilityAccount from compaccpreferences where id=? ";
            PreparedStatement stmtcp = conn.prepareStatement(querycp);  //select all Customer company
            stmtcp.setObject(1, companyId);
            ResultSet rscp = stmtcp.executeQuery();
            while (rscp.next()) {
                discountGiven = rscp.getString("discountGiven");
                discountReceived = rscp.getString("discountReceived");
                shippingCharges = rscp.getString("shippingCharges");
                //otherCharges = rscp.getString("otherCharges");
                cashAccount = rscp.getString("cashAccount");
                foreignexchange = rscp.getString("foreignexchange");
                unrealisedgainloss = rscp.getString("unrealisedgainloss");
                depereciationAccount = rscp.getString("depereciationAccount");
                expenseAccount = rscp.getString("expenseAccount");
                customerdefaultaccount = rscp.getString("customerdefaultaccount");
                vendordefaultaccount = rscp.getString("vendordefaultaccount");
                roundingDifferenceAccount = rscp.getString("roundingDifferenceAccount");
                liabilityAccount = rscp.getString("liabilityAccount");

                if (!StringUtil.isNullOrEmpty(discountGiven)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, discountGiven);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Discount Given") == -1) {
                            usedin += ", Discount Given";
                        }
                    } else {
                        usedin = "Discount Given";
                    }

                    //update Account table for Discount Given
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, discountGiven);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }

                if (!StringUtil.isNullOrEmpty(discountReceived)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, discountReceived);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Discount Received") == -1) {
                            usedin += ", Discount Received";
                        }
                    } else {
                        usedin = "Discount Received";
                    }

                    //update Account table for Discount Received
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, discountReceived);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }

                if (!StringUtil.isNullOrEmpty(shippingCharges)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, shippingCharges);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Shipping Charges") == -1) {
                            usedin += ", Shipping Charges";
                        }
                    } else {
                        usedin = "Shipping Charges";
                    }

                    //update Account table for Shipping Charges
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, shippingCharges);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }

                /*if (!StringUtil.isNullOrEmpty(otherCharges)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, otherCharges);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Other Charges") == -1) {
                            usedin += ", Other Charges";
                        }
                    } else {
                        usedin = "Other Charges";
                    }

                    //update Account table for Other Charges
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, otherCharges);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }*/

                if (!StringUtil.isNullOrEmpty(cashAccount)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, cashAccount);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Cash Account") == -1) {
                            usedin += ", Cash Account";
                        }
                    } else {
                        usedin = "Cash Account";
                    }

                    //update Account table for Cash Account
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, cashAccount);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }

                if (!StringUtil.isNullOrEmpty(foreignexchange)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, foreignexchange);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Foreign Exchange") == -1) {
                            usedin += ", Foreign Exchange";
                        }
                    } else {
                        usedin = "Foreign Exchange";
                    }

                    //update Account table for Foreign Exchange
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, foreignexchange);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }

                if (!StringUtil.isNullOrEmpty(unrealisedgainloss)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, unrealisedgainloss);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Unrealised Gain Loss") == -1) {
                            usedin += ", Unrealised Gain Loss";
                        }
                    } else {
                        usedin = "Unrealised Gain Loss";
                    }

                    //update Account table for Unrealised Gain Loss
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, unrealisedgainloss);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }

                if (!StringUtil.isNullOrEmpty(depereciationAccount)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, depereciationAccount);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Depreciation Account") == -1) {
                            usedin += ", Depreciation Account";
                        }
                    } else {
                        usedin = "Depreciation Account";
                    }

                    //update Account table for Depreciation Account
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, depereciationAccount);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }

                if (!StringUtil.isNullOrEmpty(expenseAccount)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, expenseAccount);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Salary Expense Account") == -1) {
                            usedin += ", Salary Expense Account";
                        }
                    } else {
                        usedin = "Salary Expense Account";
                    }

                    //update Account table for Salary Expense Account
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, expenseAccount);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }

                if (!StringUtil.isNullOrEmpty(customerdefaultaccount)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, customerdefaultaccount);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Customer Default Account") == -1) {
                            usedin += ", Customer Default Account";
                        }
                    } else {
                        usedin = "Customer Default Account";
                    }

                    //update Account table for Customer Default Account
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, customerdefaultaccount);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }

                if (!StringUtil.isNullOrEmpty(vendordefaultaccount)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, vendordefaultaccount);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Vendor Default Account") == -1) {
                            usedin += ", Vendor Default Account";
                        }
                    } else {
                        usedin = "Vendor Default Account";
                    }

                    //update Account table for Vendor Default Account
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, vendordefaultaccount);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }

                if (!StringUtil.isNullOrEmpty(roundingDifferenceAccount)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, roundingDifferenceAccount);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Rounding Off Difference Account") == -1) {
                            usedin += ", Rounding Off Difference Account";
                        }
                    } else {
                        usedin = "Rounding Off Difference Account";
                    }

                    //update Account table for Rounding Off Difference
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, roundingDifferenceAccount);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }

                if (!StringUtil.isNullOrEmpty(liabilityAccount)) {
                    String usedin = "";
                    String queryused = "select usedin from account where id=? ";
                    PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                    stmtused.setObject(1, liabilityAccount);
                    ResultSet rsused = stmtused.executeQuery();
                    while (rsused.next()) {
                        usedin = rsused.getString("usedin");
                    }

                    if (!StringUtil.isNullOrEmpty(usedin)) {
                        if (usedin.indexOf("Salary Payable Account") == -1) {
                            usedin += ", Salary Payable Account";
                        }
                    } else {
                        usedin = "Salary Payable Account";
                    }

                    //update Account table for Salary Payable
                    String queryForupdate = "update account set usedin=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setString(1, usedin);
                    stmtforUpdate.setString(2, liabilityAccount);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }

                totalproductUpdationCnt++;
                System.out.println("Customer Count is" + totalproductUpdationCnt);
            } //iterate for Company

            // Update Accounts From Customer Table
            String customerid = "", customeraccount = "";
            String queryc = "select id,account from customer where company=? ";
            PreparedStatement stmtc = conn.prepareStatement(queryc);  //select all Customer company
            stmtc.setObject(1, companyId);
            ResultSet rsc = stmtc.executeQuery();
            while (rsc.next()) {
                customerid = rsc.getString("id");
                customeraccount = rsc.getString("account");

                if (!StringUtil.isNullOrEmpty(customerid)) {
                    if (!StringUtil.isNullOrEmpty(customeraccount)) {
                        String usedin = "";
                        String queryused = "select usedin from account where id=? ";
                        PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                        stmtused.setObject(1, customeraccount);
                        ResultSet rsused = stmtused.executeQuery();
                        while (rsused.next()) {
                            usedin = rsused.getString("usedin");
                        }

                        if (!StringUtil.isNullOrEmpty(usedin)) {
                            if (usedin.indexOf("Customer Default Account") == -1) {
                                usedin += ", Customer Default Account";
                            }
                        } else {
                            usedin = "Customer Default Account";
                        }

                        //update Account table for Customer Default Account
                        String queryForupdate = "update account set usedin=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setString(1, usedin);
                        stmtforUpdate.setString(2, customeraccount);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                }
                totalproductUpdationCnt++;
                System.out.println("Customer Count is" + totalproductUpdationCnt);
            } //iterate for all Customer

            // Update Accounts From Vendor Table
            String vendorid = "", vendoraccount = "";
            String queryv = "select id,account from vendor where company=? ";
            PreparedStatement stmtv = conn.prepareStatement(queryv);  //select all Vendor from company
            stmtv.setObject(1, companyId);
            ResultSet rsv = stmtv.executeQuery();
            while (rsv.next()) {
                vendorid = rsv.getString("id");
                vendoraccount = rsv.getString("account");

                if (!StringUtil.isNullOrEmpty(vendorid)) {
                    if (!StringUtil.isNullOrEmpty(vendoraccount)) {
                        String usedin = "";
                        String queryused = "select usedin from account where id=? ";
                        PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                        stmtused.setObject(1, vendoraccount);
                        ResultSet rsused = stmtused.executeQuery();
                        while (rsused.next()) {
                            usedin = rsused.getString("usedin");
                        }

                        if (!StringUtil.isNullOrEmpty(usedin)) {
                            if (usedin.indexOf("Vendor Default Account") == -1) {
                                usedin += ", Vendor Default Account";
                            }
                        } else {
                            usedin = "Vendor Default Account";
                        }
                        
                        //update Account table for Vendor Default Account
                        String queryForupdate = "update account set usedin=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setString(1, usedin);
                        stmtforUpdate.setString(2, vendoraccount);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                }
                totalproductUpdationCnt++;
                System.out.println("Vendor Count is" + totalproductUpdationCnt);
            } //iterate for all Vendor

            // Update Accounts From Payment Method Table
            String pmid = "", methodName = "", pmaccount = "";
            String querypm = "select id,methodname,account from paymentmethod where company=? ";
            PreparedStatement stmtpm = conn.prepareStatement(querypm);  //select all Payment Method from company
            stmtpm.setObject(1, companyId);
            ResultSet rspm = stmtpm.executeQuery();
            while (rspm.next()) {
                pmid = rspm.getString("id");
                methodName = rspm.getString("methodname");
                pmaccount = rspm.getString("account");

                if (!StringUtil.isNullOrEmpty(pmid)) {
                    if (!StringUtil.isNullOrEmpty(pmaccount)) {
                        String usedin = "";
                        String queryused = "select usedin from account where id=? ";
                        PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                        stmtused.setObject(1, pmaccount);
                        ResultSet rsused = stmtused.executeQuery();
                        while (rsused.next()) {
                            usedin = rsused.getString("usedin");
                        }

                        if (!StringUtil.isNullOrEmpty(usedin)) {
                            if (usedin.indexOf("Payment Method") == -1) {
                                usedin += ", Payment Method";
                            }
                        } else {
                            usedin = "Payment Method";
                        }
                        
                        //update Account table for Accounts taged to Payment Method
                        String queryForupdate = "update account set usedin=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setString(1, usedin);
                        stmtforUpdate.setString(2, pmaccount);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                }
                totalproductUpdationCnt++;
                System.out.println("Payment Method Count is" + totalproductUpdationCnt);
            } //iterate for all Payment Method

            // Update Accounts From Tax Table
            String taxid = "", taxname = "", taxaccount = "";
            String querytax = "select id,name,account from tax where company=? ";
            PreparedStatement stmtt = conn.prepareStatement(querytax);  //select all taxes from company
            stmtt.setObject(1, companyId);
            ResultSet rst = stmtt.executeQuery();
            while (rst.next()) {
                taxid = rst.getString("id");
                taxname = rst.getString("name");
                taxaccount = rst.getString("account");

                if (!StringUtil.isNullOrEmpty(taxid)) {
                    if (!StringUtil.isNullOrEmpty(taxaccount)) {
                        String usedin = "";
                        String queryused = "select usedin from account where id=? ";
                        PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                        stmtused.setObject(1, taxaccount);
                        ResultSet rsused = stmtused.executeQuery();
                        while (rsused.next()) {
                            usedin = rsused.getString("usedin");
                        }

                        if (!StringUtil.isNullOrEmpty(usedin)) {
                            if (usedin.indexOf("Tax") == -1) {
                                usedin += ", Tax";
                            }
                        } else {
                            usedin = "Tax";
                        }
                        
                        //update Account table for Accounts Taged to Taxes
                        String queryForupdate = "update account set usedin=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setString(1, usedin);
                        stmtforUpdate.setString(2, taxaccount);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                }
                totalproductUpdationCnt++;
                System.out.println("Tax Count is" + totalproductUpdationCnt);
            } //iterate for all Taxes

            // Update Accounts From Product Table
            String productid = "", purchaseAccount = "", salesAccount = "", purchaseReturnAccount = "", salesReturnAccount = "", salesRevenueRecognitionAccount = "", depreciationGLAccount = "", depreciationProvisionGLAccount = "", sellAssetGLAccount = "";
            String queryproduct = "select id,purchaseAccount,salesAccount,purchaseReturnAccount,salesReturnAccount,salesRevenueRecognitionAccount,depreciationGLAccount,depreciationProvisionGLAccount,sellAssetGLAccount from product where company=? ";
            PreparedStatement stmtp = conn.prepareStatement(queryproduct);  //select all product from company
            stmtp.setObject(1, companyId);
            ResultSet rsp = stmtp.executeQuery();
            while (rsp.next()) {
                productid = rsp.getString("id");
                purchaseAccount = rsp.getString("purchaseAccount");
                salesAccount = rsp.getString("salesAccount");
                purchaseReturnAccount = rsp.getString("purchaseReturnAccount");
                salesReturnAccount = rsp.getString("salesReturnAccount");
                salesRevenueRecognitionAccount = rsp.getString("salesRevenueRecognitionAccount");
                depreciationGLAccount = rsp.getString("depreciationGLAccount");
                depreciationProvisionGLAccount = rsp.getString("depreciationProvisionGLAccount");
                sellAssetGLAccount = rsp.getString("sellAssetGLAccount");

                if (!StringUtil.isNullOrEmpty(productid)) {
                    if (!StringUtil.isNullOrEmpty(purchaseAccount)) {
                        String usedin = "";
                        String queryused = "select usedin from account where id=? ";
                        PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                        stmtused.setObject(1, purchaseAccount);
                        ResultSet rsused = stmtused.executeQuery();
                        while (rsused.next()) {
                            usedin = rsused.getString("usedin");
                        }

                        if (!StringUtil.isNullOrEmpty(usedin)) {
                            if (usedin.indexOf("Product Purchase Account") == -1) {
                                usedin += ", Product Purchase Account";
                            }
                        } else {
                            usedin = "Product Purchase Account";
                        }
                        
                        //update Account table for Purchase Account
                        String queryForupdate = "update account set usedin=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setString(1, usedin);
                        stmtforUpdate.setString(2, purchaseAccount);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                    if (!StringUtil.isNullOrEmpty(salesAccount)) {
                        String usedin = "";
                        String queryused = "select usedin from account where id=? ";
                        PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                        stmtused.setObject(1, salesAccount);
                        ResultSet rsused = stmtused.executeQuery();
                        while (rsused.next()) {
                            usedin = rsused.getString("usedin");
                        }

                        if (!StringUtil.isNullOrEmpty(usedin)) {
                            if (usedin.indexOf("Product Sales Account") == -1) {
                                usedin += ", Product Sales Account";
                            }
                        } else {
                            usedin = "Product Sales Account";
                        }
                        
                        //update Account table for Sales Account
                        String queryForupdate = "update account set usedin=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setString(1, usedin);
                        stmtforUpdate.setString(2, salesAccount);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                    if (!StringUtil.isNullOrEmpty(purchaseReturnAccount)) {
                        String usedin = "";
                        String queryused = "select usedin from account where id=? ";
                        PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                        stmtused.setObject(1, purchaseReturnAccount);
                        ResultSet rsused = stmtused.executeQuery();
                        while (rsused.next()) {
                            usedin = rsused.getString("usedin");
                        }

                        if (!StringUtil.isNullOrEmpty(usedin)) {
                            if (usedin.indexOf("Product Purchase Return Account") == -1) {
                                usedin += ", Product Purchase Return Account";
                            }
                        } else {
                            usedin = "Product Purchase Return Account";
                        }
                        
                        //update Account table for Purchase Return Account
                        String queryForupdate = "update account set usedin=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setString(1, usedin);
                        stmtforUpdate.setString(2, purchaseReturnAccount);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                    if (!StringUtil.isNullOrEmpty(salesReturnAccount)) {
                        String usedin = "";
                        String queryused = "select usedin from account where id=? ";
                        PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                        stmtused.setObject(1, salesReturnAccount);
                        ResultSet rsused = stmtused.executeQuery();
                        while (rsused.next()) {
                            usedin = rsused.getString("usedin");
                        }

                        if (!StringUtil.isNullOrEmpty(usedin)) {
                            if (usedin.indexOf("Product Sales Return Account") == -1) {
                                usedin += ", Product Sales Return Account";
                            }
                        } else {
                            usedin = "Product Sales Return Account";
                        }
                        
                        //update Account table for Sales Return Account
                        String queryForupdate = "update account set usedin=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setString(1, usedin);
                        stmtforUpdate.setString(2, salesReturnAccount);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                    if (!StringUtil.isNullOrEmpty(salesRevenueRecognitionAccount)) {
                        String usedin = "";
                        String queryused = "select usedin from account where id=? ";
                        PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                        stmtused.setObject(1, salesRevenueRecognitionAccount);
                        ResultSet rsused = stmtused.executeQuery();
                        while (rsused.next()) {
                            usedin = rsused.getString("usedin");
                        }

                        if (!StringUtil.isNullOrEmpty(usedin)) {
                            if (usedin.indexOf("Product Sales Revenue Recognition Account") == -1) {
                                usedin += ", Product Sales Revenue Recognition Account";
                            }
                        } else {
                            usedin = "Product Sales Revenue Recognition Account";
                        }
                        
                        //update Account table for Sales Revenue Recognition Account
                        String queryForupdate = "update account set usedin=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setString(1, usedin);
                        stmtforUpdate.setString(2, salesRevenueRecognitionAccount);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                    if (!StringUtil.isNullOrEmpty(depreciationGLAccount)) {
                        String usedin = "";
                        String queryused = "select usedin from account where id=? ";
                        PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                        stmtused.setObject(1, depreciationGLAccount);
                        ResultSet rsused = stmtused.executeQuery();
                        while (rsused.next()) {
                            usedin = rsused.getString("usedin");
                        }

                        if (!StringUtil.isNullOrEmpty(usedin)) {
                            if (usedin.indexOf("Fixed Asset Depreciation GL Account") == -1) {
                                usedin += ", Fixed Asset Depreciation GL Account";
                            }
                        } else {
                            usedin = "Fixed Asset Depreciation GL Account";
                        }
                        
                        //update Account table for Depreciation GL Account
                        String queryForupdate = "update account set usedin=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setString(1, usedin);
                        stmtforUpdate.setString(2, depreciationGLAccount);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                    if (!StringUtil.isNullOrEmpty(depreciationProvisionGLAccount)) {
                        String usedin = "";
                        String queryused = "select usedin from account where id=? ";
                        PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                        stmtused.setObject(1, depreciationProvisionGLAccount);
                        ResultSet rsused = stmtused.executeQuery();
                        while (rsused.next()) {
                            usedin = rsused.getString("usedin");
                        }

                        if (!StringUtil.isNullOrEmpty(usedin)) {
                            if (usedin.indexOf("Fixed Asset Depreciation Provision GL Account") == -1) {
                                usedin += ", Fixed Asset Depreciation Provision GL Account";
                            }
                        } else {
                            usedin = "Fixed Asset Depreciation Provision GL Account";
                        }
                        
                        //update Account table for Depreciation Provision GL Account
                        String queryForupdate = "update account set usedin=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setString(1, usedin);
                        stmtforUpdate.setString(2, depreciationProvisionGLAccount);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                    if (!StringUtil.isNullOrEmpty(sellAssetGLAccount)) {
                        String usedin = "";
                        String queryused = "select usedin from account where id=? ";
                        PreparedStatement stmtused = conn.prepareStatement(queryused);  //select all Customer company
                        stmtused.setObject(1, sellAssetGLAccount);
                        ResultSet rsused = stmtused.executeQuery();
                        while (rsused.next()) {
                            usedin = rsused.getString("usedin");
                        }

                        if (!StringUtil.isNullOrEmpty(usedin)) {
                            if (usedin.indexOf("Fixed Asset Sales Account") == -1) {
                                usedin += ", Fixed Asset Sales Account";
                            }
                        } else {
                            usedin = "Fixed Asset Sales Account";
                        }
                        
                        //update Account table for Fixed Asser Sales Account
                        String queryForupdate = "update account set usedin=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setString(1, usedin);
                        stmtforUpdate.setString(2, sellAssetGLAccount);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                }
                totalproductUpdationCnt++;
                System.out.println("Product Count is" + totalproductUpdationCnt);
            } //iterate for all product

            totalCompanyUpdationCnt++;
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
        out.println("<br><br> Total products updated are " + totalproductUpdationCnt);
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>