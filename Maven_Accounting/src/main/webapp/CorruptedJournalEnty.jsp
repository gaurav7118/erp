<%@page import="com.krawler.spring.authHandler.authHandler"%>
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
        String serverip = request.getParameter("serverip");//"192.168.0.21";                            
        String port = "3306";//"3306";
        String dbName = request.getParameter("dbname");//"stagingaccounting";
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
        String query = "select companyid,companyname,subdomain FROM company ";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query += " where subdomain= ?";
        }
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        while (rs.next()) {
            int accGrpUpdateCounter = 0;
            String companyId = rs.getString("companyid");
            String companyname = rs.getString("companyname");
            subdomain = rs.getString("subdomain");
%>
            <div align="center">
                <table border="1" cellpadding="5">
                    <caption><b>List of Corrupted Journal Entry</b></caption>
                    <tr><th>Subdomain</th><th>Journal_Entry</th><th>JE_Date</th><th>JE_Id</th><th>Transaction_Id</th><th>Transaction_Moduleid</th><th>JE_Type</th></tr>
<%
            // Get Accounts with default account
            query = "select id,entryno,entrydate,transactionId,transactionModuleid,typevalue from journalentry where deleteflag='F' and istemplate!=2 and company=?"; 
            PreparedStatement stmt1 = conn.prepareStatement(query);
            stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                String accid = "", accName="", accCode="", accGroup="", accGroupName="";
                String  transactionId = "",transactionModuleid="";
                accid = rs1.getString("id");
                accName = rs1.getString("entryno");
                accCode = rs1.getString("entrydate");
                transactionId = rs1.getString("transactionId");
                transactionModuleid = rs1.getString("transactionModuleid");
                int typevalue = rs1.getInt("typevalue");
                String jetype = "";
                if (!StringUtil.isNullOrEmpty(accid)) { 
                    // Get rules to user mapping
                    query = "select id, debit,amountinbase from jedetail where isseparated='F' and company=? and journalentry=?"; 
                    String query2 = "select count(id) as jedcount from jedetail where isseparated='F' and company=? and journalentry=?"; 
                    String query3 = "select sum(case when debit='T' then amountinbase else -amountinbase end) as totalDrCr from jedetail where isseparated='F' and company=? and journalentry=?"; 
                    /*
                    PreparedStatement stmt3 = conn.prepareStatement(query);
                    stmt3.setString(1, companyId);
                    stmt3.setString(2, accid);
                    ResultSet rs2 = stmt3.executeQuery();
                    
                    PreparedStatement stmt4 = conn.prepareStatement(query2);
                    stmt4.setString(1, companyId);
                    stmt4.setString(2, accid);
                    ResultSet rs3 = stmt4.executeQuery();
                    int count= 0;
                    while (rs3.next()) {
                        count = rs3.getInt("jedcount");
                    }
                    */ 
                    
                    // JE with only debit or credit
                    String newGrpId = "", oldGrpId = "", oldDebit="";
                    int cnt=1;
                    boolean isCorrupted=true;
                    /*
                    if(count==0){
                         isCorrupted=false;
                    }else{
                        while (rs2.next()) {
                            newGrpId = rs2.getString("id");
                            if(cnt==1){
                                oldDebit=rs2.getString("debit");
                            }
                            String newGrpName = rs2.getString("debit");
                            if (cnt>1 && !StringUtil.isNullOrEmpty(newGrpId) && !oldDebit.equalsIgnoreCase(newGrpName)) { 
                                isCorrupted=false;
                                break;
                            }
                            cnt++;
                        }
                    }
                    */
                    // JE with credit and debit mismatch
                    PreparedStatement stmt5 = conn.prepareStatement(query3);
                    stmt5.setString(1, companyId);
                    stmt5.setString(2, accid);
                    ResultSet rs4 = stmt5.executeQuery();
                    double debitMinusCredit = 0;
                    while (rs4.next()) {
                        try{
                            debitMinusCredit = authHandler.round(rs4.getDouble("totalDrCr"),companyId);
                        } catch (Exception ex){
                            debitMinusCredit = rs4.getDouble("totalDrCr");
                        }
                        if(debitMinusCredit > 0.05){
                        //if(debitMinusCredit<=0.05 && debitMinusCredit>0){
                            isCorrupted=true;
                        } else {
                            isCorrupted=false;
                        }
                    }
                    if(typevalue==0){
                        jetype = "Auto JE "+typevalue;
                    }else if(typevalue==1){
                        jetype = "Normal JE "+typevalue;
                    }else if(typevalue==2){
                        jetype = "Party JE "+typevalue;
                    }else if(typevalue==3){
                        jetype = "Funds Transfer JE "+typevalue;
                    }
                    if(isCorrupted){
                        accGrpUpdateCounter++;
%>
                            <tr>
                                <td><% out.println(subdomain);%></td>
                                <td><% out.println(accName);%></td>
                                <td><% out.println(accCode);%></td> 
                                <td><% out.println(accid);%></td> 
                                <td><% out.println(transactionId);%></td> 
                                <td><% out.println(transactionModuleid);%></td> 
                                <td><% out.println(jetype);%></td> 
                            </tr>
<%
                    }
                }
            }
            out.println("<br><br><b>No. of corrupted JE found for " + companyname + " : " + accGrpUpdateCounter +"</b>");
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

