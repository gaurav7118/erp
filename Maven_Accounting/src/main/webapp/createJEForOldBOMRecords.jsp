<%-- 
    Document   : createJEForOldBOMRecords
    Created on : Jul 2, 2015, 12:45:38 PM
    Author     : krawler
--%>

<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="com.krawler.common.util.Constants"%>
<%@page import="com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
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

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(subDomain)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password,subdomain) in url. so please provide all these parameter correctly. ");
        }

        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);

        String query1 = " select companyid,subdomain,creator,currency from company where subdomain = ? ";
        PreparedStatement stmt1 = conn.prepareStatement(query1);
        stmt1.setString(1, subDomain);
        ResultSet rs1 = stmt1.executeQuery();

        while (rs1.next()) {
            String companyId = rs1.getString(1);
            String companyName = rs1.getString(2);
            String creator = rs1.getString(3);
            String currency = rs1.getString(4);

            String query2 = " select pb.id,pb.product,p.name,pb.productcost,pb.entrydate,p.purchaseAccount,pb.quantity,pb.refno from productbuild pb "
                    + " inner join product p on p.id = pb.product "
                    + " where pb.journalentry is null and pb.company = ? ";
            PreparedStatement stmt2 = conn.prepareStatement(query2);
            stmt2.setString(1, companyId);
            ResultSet rs2 = stmt2.executeQuery();

            while (rs2.next()) {
                String productBuildID = rs2.getString(1);
                String productID = rs2.getString(2);
                String productName = rs2.getString(3);
                double productCost = rs2.getDouble(4);
                java.sql.Date entryDate = rs2.getDate(5);
                String purchaseAccount = rs2.getString(6);
                double buildquantity = rs2.getDouble(7);
                String refno = rs2.getString(8);
                String autoNumber = "";
                String jeSeqFormatID = "";
                String nextNumTemp = "";

                synchronized (this) {
                    String query3 = " select id,startfrom,isdatebeforeprefix,prefix,dateformatinprefix,suffix,numberofdigit,showleadingzero from sequenceformat  where deleted  = 'F' and company = ? and modulename = ? and moduleid = ? and isdefaultformat = 'T' ";
                    PreparedStatement stmt3 = conn.prepareStatement(query3);
                    stmt3.setString(1, companyId);
                    stmt3.setString(2, CompanyPreferencesConstants.AUTOJOURNALENTRY);
                    stmt3.setInt(3, Constants.Acc_GENERAL_LEDGER_ModuleId);
                    ResultSet rs3 = stmt3.executeQuery();

                    int startfrom = 1;
                    boolean showleadingzero = false;
                    String prefix = "";
                    String selecteddateformat = "";
                    String suffix = "";
                    int numberofdigit = 1;
                    boolean datebeforePrefix = false;
                    while (rs3.next()) {
                        jeSeqFormatID = rs3.getString(1);
                        startfrom = rs3.getInt(2);
                        datebeforePrefix = rs3.getBoolean(3);
                        prefix = rs3.getString(4);
                        selecteddateformat = rs3.getString(5);
                        suffix = rs3.getString(6);
                        numberofdigit = rs3.getInt(7);
                        showleadingzero = rs3.getBoolean(8);
                    }

                    String query4 = " select max(seqnumber) from journalentry where company =  ? and seqnumber >= 1 and autogen = 'T' and seqformat = ? ";
                    PreparedStatement stmt4 = conn.prepareStatement(query4);
                    stmt4.setString(1, companyId);
                    stmt4.setString(2, jeSeqFormatID);
                    ResultSet rs4 = stmt4.executeQuery();
                    int nextNumber = startfrom;
                    while (rs4.next()) {
                        nextNumber = rs4.getInt(1) + 1;
                    }
                    nextNumTemp = nextNumber + "";
                    if (showleadingzero) {
                        while (nextNumTemp.length() < numberofdigit) {
                            nextNumTemp = "0" + nextNumTemp;
                        }
                    }

                    if (datebeforePrefix) {
                        java.util.Date date = new java.util.Date();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        int year = cal.get(Calendar.YEAR);
                        DecimalFormat mFormat = new DecimalFormat("00");
                        int month = cal.get(Calendar.MONTH) + 1;
                        if (!StringUtil.isNullOrEmpty(selecteddateformat) && selecteddateformat.equalsIgnoreCase("YYYY")) {
                            autoNumber = "" + year + prefix + nextNumTemp + suffix;
                        } else if (!StringUtil.isNullOrEmpty(selecteddateformat) && selecteddateformat.equalsIgnoreCase("YYYYMM")) {
                            autoNumber = "" + year + mFormat.format(month) + prefix + nextNumTemp + suffix;
                        } else { // for YYYYMMDD this will default case
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd"); // ERP-8689
                            String curentDate = sdf.format(date);
                            autoNumber = curentDate + prefix + nextNumTemp + suffix;
                        }
                    } else {
                        autoNumber = prefix + nextNumTemp + suffix;
                    }
                }

                String jeUUID = UUID.randomUUID().toString().replace("-", "");
                String query5 = " insert into journalentry (id,entryno,autogen,seqformat,seqnumber,entrydate,company,memo,createdby,currency,deleteflag,createdon) values (?,?,?,?,?,?,?,?,?,?,?,?) ";
                PreparedStatement stmt5 = conn.prepareStatement(query5);
                stmt5.setString(1, jeUUID);
                stmt5.setString(2, autoNumber);
                stmt5.setString(3, "T");
                stmt5.setString(4, jeSeqFormatID);
                stmt5.setString(5, nextNumTemp);
                stmt5.setDate(6, entryDate);
                stmt5.setString(7, companyId);
                stmt5.setString(8, "Memo: Build Assembly JE for " + productName + "Build Ref No.: " + refno);
                stmt5.setString(9, creator);
                stmt5.setString(10, currency);
                stmt5.setString(11, "F");
                stmt5.setLong(12, entryDate.getTime());
                stmt5.execute();
                
                out.println("JE Number: " + autoNumber + "<br>");

                int jedSrNo = 1;

                String jedDrUUID = UUID.randomUUID().toString().replace("-", "");
                String query6 = " insert into jedetail (id,debit,amount,account,journalEntry,company,srno,isbankcharge) values (?,?,?,?,?,?,?,?) ";
                PreparedStatement stmt6 = conn.prepareStatement(query6);
                stmt6.setString(1, jedDrUUID);
                stmt6.setString(2, "T");
                stmt6.setDouble(3, productCost);
                stmt6.setString(4, purchaseAccount);
                stmt6.setString(5, jeUUID);
                stmt6.setString(6, companyId);
                stmt6.setInt(7, jedSrNo);
                stmt6.setInt(8, 0);
                stmt6.execute();

                String query7 = " select pbd.id,pbd.aquantity,pbd.rate,p.purchaseAccount from pbdetails pbd "
                        + " inner join product p on p.id = pbd.aproduct "
                        + " where pbd.build  = ? ";
                PreparedStatement stmt7 = conn.prepareStatement(query7);
                stmt7.setString(1, productBuildID);
                ResultSet rs7 = stmt7.executeQuery();

                while (rs7.next()) {
                    jedSrNo++;
                    String productBuildDetailID = rs7.getString(1);
                    double aquantity = rs7.getDouble(2);
                    double rate = rs7.getDouble(3);
                    String bomPurchaseAccount = rs7.getString(4);
                    double deductqty = aquantity * buildquantity;

                    String jedCrUUID = UUID.randomUUID().toString().replace("-", "");
                    String query8 = " insert into jedetail (id,debit,amount,account,journalEntry,company,srno,isbankcharge) values (?,?,?,?,?,?,?,?) ";
                    PreparedStatement stmt8 = conn.prepareStatement(query8);
                    stmt8.setString(1, jedCrUUID);
                    stmt8.setString(2, "F");
                    stmt8.setDouble(3, deductqty * rate);
                    stmt8.setString(4, bomPurchaseAccount);
                    stmt8.setString(5, jeUUID);
                    stmt8.setString(6, companyId);
                    stmt8.setInt(7, jedSrNo);
                    stmt8.setInt(8, 0);
                    stmt8.execute();
                }
                
                String query9 = " update productbuild set journalentry = ? where id = ? and company = ? ";
                PreparedStatement stmt9 = conn.prepareStatement(query9);
                stmt9.setString(1, jeUUID);
                stmt9.setString(2, productBuildID);
                stmt9.setString(3, companyId);
                stmt9.executeUpdate();
            }
            out.println("JE's are posted successfully for subdomain " + companyName);
        }
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>