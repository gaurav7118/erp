

<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%
    String message = "";
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
        PreparedStatement stmtquery;
        String companyId = "";
        String query = "";
        String insertquery = "";
        ResultSet rs;
        String paymentid = "";
        String memo = "";
        String company = "";

        if (!StringUtil.isNullOrEmpty(subDomain)) {
            query = "SELECT p.id,p.company,p.journalentry,p.receiptnumber,p.externalcurrencyrate,p.bankinterestamount,p.bankinterestaccountid,p.deposittojedetail,p.depositamount,p.journalentryforbankinterest,p.paydetail,"
                        + "c.subdomain FROM receipt p inner join company c on p.company=c.companyid where c.subdomain = ? and p.bankinterestamount>? ";
//                + "c.subdomain FROM receipt p inner join company c on p.company=c.companyid where c.subdomain = ? and p.receiptnumber='RP000025'";
            stmtquery = conn.prepareStatement(query);
            stmtquery.setString(1, subDomain);
            stmtquery.setDouble(2, 0);
            rs = stmtquery.executeQuery();
        } else {
            query = "SELECT p.id,p.company,p.journalentry,p.receiptnumber,p.externalcurrencyrate,p.bankinterestamount,p.bankinterestaccountid,p.deposittojedetail,p.depositamount,p.journalentryforbankinterest,p.paydetail,"
                    + "c.subdomain FROM receipt p inner join company c on p.company=c.companyid where p.bankinterestamount>? order by c.subdomain";
//                + "c.subdomain FROM receipt p inner join company c on p.company=c.companyid where c.subdomain = ? and p.receiptnumber='RP101'";
            stmtquery = conn.prepareStatement(query);
            stmtquery.setDouble(1, 0);
            rs = stmtquery.executeQuery();
        }


        while (rs.next()) {

            String journalentryforbankcharges = rs.getString("journalentryforbankinterest");
            if (StringUtil.isNullOrEmpty(journalentryforbankcharges)) {

                company = rs.getString("company");
                double bankinteresrsamount = rs.getDouble("bankinterestamount");
                String bankinterestaccountid = rs.getString("bankinterestaccountid");
                String mainJE = rs.getString("journalentry");
                String receipt = rs.getString("id");
                String receiptnumber = rs.getString("receiptnumber");
                String pdetail = rs.getString("paydetail");
                double depositamount = rs.getDouble("depositamount");
                String companySubdomain = rs.getString("subdomain");
                query = "select j.entryno,j.entrydate,j.autogen,j.currency,j.externalcurrencyrate,j.createdon,j.isinventory,j.seqnumber,j.seqformat,j.createdby from journalentry j where id=?";
                stmtquery = conn.prepareStatement(query);
                stmtquery.setString(1, mainJE);
                ResultSet resultSet = stmtquery.executeQuery();
                PreparedStatement preparedStatement = null;
                String jeid = "";
                while (resultSet.next()) {
                    jeid = UUID.randomUUID().toString();
                    String entryno = resultSet.getString("entryno");
                    java.sql.Date entrydate = resultSet.getDate("entrydate");
                    String autogen = resultSet.getString("autogen");
                    String currency = resultSet.getString("currency");
                    double externalcurrencyrate = resultSet.getDouble("externalcurrencyrate");
                    long createdon = resultSet.getLong("createdon");
                    String isinventory = resultSet.getString("isinventory");
                    int seqnumber = resultSet.getInt("seqnumber");
                    String seqformat = resultSet.getString("seqformat");
                    String createdby = resultSet.getString("createdby");
                    String sequenceformat = "";
                    int sequencenuber = 0;
                    // to get sequence format
                    String sequenceQuery = "select s.startfrom,s.prefix,s.suffix,s.numberofdigit,s.showleadingzero from sequenceformat s where s.id=? and s.company=?";
                    stmtquery = conn.prepareStatement(sequenceQuery);
                    stmtquery.setString(1, seqformat);
                    stmtquery.setString(2, company);
                    ResultSet resultSet1 = stmtquery.executeQuery();
                    while (resultSet1.next()) {
                        String prefix = resultSet1.getString("prefix");
                        String suffix = resultSet1.getString("suffix");
                        int startfrom = resultSet1.getInt("startfrom");
                        int numberofdigit = resultSet1.getInt("numberofdigit");
                        String showleadingzero=resultSet1.getString("showleadingzero");
                        String sequnberquery = "select max(seqnumber) from journalentry where company = ? and seqformat = ? and autogen = ? and seqnumber >=? ";
                        stmtquery = conn.prepareStatement(sequnberquery);
                        stmtquery.setString(1, company);
                        stmtquery.setString(2, seqformat);
                        stmtquery.setString(3,"T");
                        stmtquery.setInt(4, startfrom);
                        ResultSet resultSet2 = stmtquery.executeQuery();
                       while(resultSet2.next()){
                           sequencenuber = resultSet2.getInt(1) + 1;
                       }
                        
                            String nextNumTemp = (sequencenuber) + "";
                            if (showleadingzero.equalsIgnoreCase("T")) {
                                while (nextNumTemp.length() < numberofdigit) {
                                    nextNumTemp = "0" + nextNumTemp;
                                }
                            }
                        sequenceformat = prefix + nextNumTemp + suffix;
                    }

                    // Insert JE
                    String insertQuery = "INSERT into journalentry (id,entryno,autogen,entrydate,company,currency,deleteflag,externalcurrencyrate,createdon,"
                            + "isinventory,seqnumber,seqformat,createdby )"
                            + "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

                    preparedStatement = conn.prepareStatement(insertQuery);
                    preparedStatement.setString(1, jeid);
                    preparedStatement.setString(2, sequenceformat);
                    preparedStatement.setString(3, autogen);
                    preparedStatement.setDate(4, entrydate);
                    preparedStatement.setString(5, company);
                    preparedStatement.setString(6, currency);
                    preparedStatement.setString(7, "F");
                    preparedStatement.setDouble(8, externalcurrencyrate);
                    preparedStatement.setLong(9, createdon);
                    preparedStatement.setString(10, isinventory);
                    preparedStatement.setInt(11, sequencenuber);
                    preparedStatement.setString(12, seqformat);
                    preparedStatement.setString(13, createdby);
                    preparedStatement.executeUpdate();

                    // insert JE Details for bank interest debit
                    String Insertje1 = "INSERT into jedetail (id,debit,amount,account,journalEntry,company,srno)"
                            + "values(?,?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(Insertje1);
                    String jed1 = UUID.randomUUID().toString();
                    preparedStatement.setString(1, jed1);
                    preparedStatement.setString(2, "T");
                    preparedStatement.setDouble(3, bankinteresrsamount);
                    preparedStatement.setString(4, bankinterestaccountid);
                    preparedStatement.setString(5, jeid);
                    preparedStatement.setString(6, company);
                    preparedStatement.setInt(7, 1);
                    preparedStatement.executeUpdate();

                    //get user account
                    String paydetail = "select paymentMethod from paydetail where id=?";
                    stmtquery = conn.prepareStatement(paydetail);
                    stmtquery.setString(1, pdetail);

                    String pmethod = "";
                    ResultSet resultSet4 = stmtquery.executeQuery();
                    while (resultSet4.next()) {
                        pmethod = resultSet4.getString("paymentMethod");

                    }

                    // get account from payment method
                    String paccount = "select account from paymentmethod where id=?";
                    stmtquery = conn.prepareStatement(paccount);
                    stmtquery.setString(1, pmethod);

                    String useraccount = "";
                    ResultSet resultSet5 = stmtquery.executeQuery();
                    while (resultSet5.next()) {
                        useraccount = resultSet5.getString("account");

                    }

                    // Update credit details form JE
                    String amountquery = "select amount from jedetail where journalEntry=? and account=?";
                    stmtquery = conn.prepareStatement(amountquery);
                    stmtquery.setString(1, mainJE);
                    stmtquery.setString(2, useraccount);
                    ResultSet resultSet2 = stmtquery.executeQuery();

                    double amount = 0;
                    double finalamt = 0;
                    //String useraccount = "";
                    while (resultSet2.next()) {
                        amount = resultSet2.getDouble("amount");
                    }
//                if(debit.equalsIgnoreCase("T")){
//                    
//                }
                    finalamt = amount - bankinteresrsamount;

                    String updateJe = "update jedetail set amount=? where journalEntry=? and  account=?";
                    PreparedStatement statement1 = conn.prepareStatement(updateJe);
                    statement1.setDouble(1, finalamt);
                    statement1.setString(2, mainJE);
                    statement1.setString(3, useraccount);
                    statement1.executeUpdate();

                    // delete Bank interest detail from Main JE
                    String deletequery = "delete from jedetail where journalEntry=? and account=?";
                    PreparedStatement statement = conn.prepareStatement(deletequery);
                    statement.setString(1, mainJE);
                    statement.setString(2, bankinterestaccountid);
                    statement.executeUpdate();

                    // insert JE Details for bank interest credit
                    String Insertje2 = "INSERT into jedetail (id,debit,amount,account,journalEntry,company,srno)"
                            + "values(?,?,?,?,?,?,?)";
                    preparedStatement = conn.prepareStatement(Insertje2);
                    String jed2 = UUID.randomUUID().toString();
                    preparedStatement.setString(1, jed2);
                    preparedStatement.setString(2, "F");
                    preparedStatement.setDouble(3, bankinteresrsamount);
                    preparedStatement.setString(4, useraccount);
                    preparedStatement.setString(5, jeid);
                    preparedStatement.setString(6, company);
                    preparedStatement.setInt(7, 2);
                    preparedStatement.executeUpdate();
                    
                    System.out.println("Subdomain: "+companySubdomain + ", Receipt:"+receiptnumber);
                    message+="\nSubdomain: "+companySubdomain + ", Receipt:"+receiptnumber;
                }

                // Update Payment
                String updatepayment = "update receipt set depositamount=?,journalentryforbankinterest=? where id=? and  company=?";
                PreparedStatement statement1 = conn.prepareStatement(updatepayment);
                depositamount = depositamount - bankinteresrsamount;
                statement1.setDouble(1, depositamount);
                statement1.setString(2, jeid);
                statement1.setString(3, receipt);
                statement1.setString(4, company);
                statement1.executeUpdate();
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
        out.print(message);
    }
%>