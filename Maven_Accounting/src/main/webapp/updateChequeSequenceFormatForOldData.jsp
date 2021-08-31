<%-- 
    Document   : updateCustomerFields
    Created on : Nov 7, 2014, 11:27:11 AM
    Author     : krawler
--%>

<%@page import="org.krysalis.barcode4j.tools.Length"%>
<%@page import="java.util.UUID"%>
<%@page import="java.math.BigInteger"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


<%
    Connection conn = null;
    try {

        String port = "3306";
        String driver = "com.mysql.jdbc.Driver";

        String serverip = request.getParameter("serverip") != null ? request.getParameter("serverip") : "";
        String dbName = request.getParameter("dbname") != null ? request.getParameter("dbname") : "";
        String userName = request.getParameter("username") != null ? request.getParameter("username") : "";
        String password = request.getParameter("password") != null ? request.getParameter("password") : "";
        String subdomain = request.getParameter("subdomain") != null ? request.getParameter("subdomain") : "";

        if (StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            out.println("Parameter missing from parameters=> [serverip,dbname,username,password] ");
        } else {
            String connectDB = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectDB, userName, password);

            String query = "";
            PreparedStatement compstmt = null;
            if (StringUtil.isNullOrEmpty(subdomain)) {
                query = "select companyname,subdomain,companyid from company";
                compstmt = conn.prepareStatement(query);
            } else {
                query = "select companyname,subdomain,companyid from company where subdomain=?";
                compstmt = conn.prepareStatement(query);
                compstmt.setString(1, subdomain);
            }
            ResultSet comprs = compstmt.executeQuery();
            while (comprs.next()) {
                String companyid = comprs.getString("companyid");
                String companyname = comprs.getString("companyname");
                String compSubDomain = comprs.getString("subdomain");
                try {
                    if (!StringUtil.isNullOrEmpty(companyid)) {
                        String selectChequeQuery = "select max(cheque.chequeno) as maxchequeno,cheque.bankaccount from cheque "
                                + "inner join account on account.id=cheque.bankaccount "//this inner join applied so that only uncorrupted account come in result otherwise it causing exception
                                + "where cheque.deleteflag=0 and cheque.company=? and cheque.bankaccount is not null and cheque.chequeno REGEXP '^[0-9]+$' "
                                + "group by cheque.bankaccount";
                        PreparedStatement chequestmt = conn.prepareStatement(selectChequeQuery);
                        chequestmt.setString(1, companyid);
                        ResultSet chequers = chequestmt.executeQuery();
                        while (chequers.next()) {
                            String bankAccountID = chequers.getString("bankaccount");
                            String chequeNo = chequers.getString("maxchequeno");
                            long startFrom = Long.parseLong(chequeNo) + 1;
                            int numberOfSDigit = String.valueOf(startFrom).length();
                            if(chequeNo.length()>String.valueOf(startFrom).length()){//This is the case like check no 000001
                                numberOfSDigit=chequeNo.length();
                            }
                            

                            //Check Similar Sequence Format is available or not if present then no need to add
                            String selectExistingSeqFormatQuery = "select startfrom,isshowleadingzero from chequesequenceformat where bankaccount=? and company=? and numberofdigits=?";
                            PreparedStatement existingChequeStmt = conn.prepareStatement(selectExistingSeqFormatQuery);
                            existingChequeStmt.setString(1, bankAccountID);
                            existingChequeStmt.setString(2, companyid);
                            existingChequeStmt.setInt(3, numberOfSDigit);
                            ResultSet existingChequeRS = existingChequeStmt.executeQuery();
                            if (!existingChequeRS.next()) {//If not vaialble then add new sequence format
                                long chequeEndNumber = 0;
                                String name="";
                                String endnumber = "9";
                                for (int i = 0; i < numberOfSDigit; i++) {
                                    endnumber += "9";
                                    name +="0";
                                }
                                
                                chequeEndNumber = Long.parseLong(endnumber);
                                String uuid = UUID.randomUUID().toString().replace("-", "");
                                String addChequeSequenceFormatQuery = "insert into chequesequenceformat "
                                        + "(id,name,numberofdigits,startfrom,isshowleadingzero,company,bankaccount,chequeEndNumber) "
                                        + "values (?,?,?,?,?,?,?,?) ";
                                PreparedStatement insertstmt = conn.prepareStatement(addChequeSequenceFormatQuery);
                                insertstmt.setString(1, uuid);
                                insertstmt.setString(2, name);
                                insertstmt.setInt(3, numberOfSDigit);
                                insertstmt.setLong(4, startFrom);
                                insertstmt.setInt(5, 1);
                                insertstmt.setString(6, companyid);
                                insertstmt.setString(7, bankAccountID);
                                insertstmt.setLong(8, chequeEndNumber);
                                insertstmt.execute();
                            }
                        }
                        out.println("</br>Data Successfully updated for <b>Company:-</b> " + companyname + ", <b>Subdomain:-</b>" + compSubDomain);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    out.println("</br>Failed to update data for <b>Company:-</b> " + companyname + ", <b>Subdomain:-</b>" + compSubDomain);
                }
            }
        }
        if (conn != null) {
            out.println("</br>Script Execution Completed Sucessfully.Thanks!!!");
            conn.close();//finally release connection   
        }
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>
