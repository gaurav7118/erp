%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<!--Following steps need to be done on DB before executing this script
First execute missingsql.jsp then only execute sqlformulltermid.jsp-->


<%
    Connection conn = null;
    try {
        String serverip = "192.168.0.209";
        String port = "3306";
        String dbName = "stagingaccountingsms";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";

        String driver = "com.mysql.jdbc.Driver";
        String userName = "krawlersqladmin";
        String password = "krawler";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
        int cnt = 0;
        String record = "";
        String companyId = "";
        String termid = "";
        String termdays = "";
        String cashAccount = "";
        String querycash = "select id,cashAccount from compaccpreferences";
        PreparedStatement stmtcash = conn.prepareStatement(querycash);
        ResultSet rscash = stmtcash.executeQuery();
        while (rscash.next()) {
            companyId = rscash.getString("id");
            cashAccount = rscash.getString("cashAccount");
            String querycashNet = "update invoice inner join jedetail on jedetail.id = invoice.centry   set invoice.termid=(select creditterm.termid from creditterm where creditterm.company='"+companyId +"' and creditterm.termdays=-1)   where invoice.company = '"+ companyId +"' and jedetail.account = '"+cashAccount+"' ";
            PreparedStatement stmtcashNet = conn.prepareStatement(querycashNet);
            cnt = stmtcashNet.executeUpdate();
            querycashNet = "update goodsreceipt inner join jedetail on jedetail.id = goodsreceipt.centry   set goodsreceipt.termid=(select creditterm.termid from creditterm where creditterm.company='"+companyId +"' and creditterm.termdays=-1)   where goodsreceipt.company = '"+ companyId +"' and jedetail.account = '"+cashAccount+"'" ;
            stmtcashNet = conn.prepareStatement(querycashNet);
            cnt = stmtcashNet.executeUpdate();
        }

        String query = "SELECT companyid FROM company";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            //rs.next();
            companyId = rs.getString("companyid");
            String querytermid = "SELECT termid,termdays FROM creditterm where company ='" + companyId + "'";
            PreparedStatement termidstm = conn.prepareStatement(querytermid);
            ResultSet rstermid = termidstm.executeQuery();
            while (rstermid.next()) {
                termid = rstermid.getString("termid");
                termdays = rstermid.getString("termdays");
                String sql = "update invoice inner join journalentry on invoice.journalentry=journalentry.id set termid='" + termid + "' where datediff(invoice.duedate,journalentry.entrydate)='" + termdays + "' and invoice.termid is NULL and invoice.company='" + companyId + "'";
                PreparedStatement stmt1 = conn.prepareStatement(sql);
                cnt = stmt1.executeUpdate();
                String sql1 = "update goodsreceipt inner join journalentry on goodsreceipt.journalentry=journalentry.id  set termid='" + termid + "'  where datediff(goodsreceipt.duedate,journalentry.entrydate)='" + termdays + "' and goodsreceipt.termid is NULL and goodsreceipt.company='" + companyId + "'";
                PreparedStatement stmt12 = conn.prepareStatement(sql1);
                cnt = stmt12.executeUpdate();
            }
        }


    } catch (Exception e) {
        e.printStackTrace();
    }

%>
