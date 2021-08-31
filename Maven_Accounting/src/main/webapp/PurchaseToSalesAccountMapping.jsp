
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>

<%! 
%>
<%
    Connection conn = null;
    try {
        //SCRIPT URL : http://<app-url>/PurchaseToSalesAccountMapping.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
        // 'subdomain' is a mandatory field

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password) || StringUtil.isNullOrEmpty(subdomain)) {
            throw new Exception(" You have not privided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        subdomain = "fastenhardware"; //Applicable only 'FastenHardware
        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String journalentry = "", accountName= "", companyid="", salesAccount="", purchaseAccount="";
        String compqry = "", jeQry = "", accqry="", salesaccqry="", purchaseaccqry="";
        PreparedStatement jestmt = null;
        ResultSet jeresult = null;
        PreparedStatement compst = null;
        ResultSet comprs = null;
        PreparedStatement salesaccst = null;
        ResultSet salesaccrs = null;
        PreparedStatement purchaseaccst = null;
        ResultSet purchaseaccrs = null;
        PreparedStatement updatest = null;
        
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, username, password);
            //Get Company ID
            compqry = "SELECT companyid FROM company WHERE subdomain='"+subdomain+"'";
            compst = conn.prepareStatement(compqry);
            comprs = compst.executeQuery();
            while (comprs.next()) {
                companyid = comprs.getString("companyid");
                
            //Get Sales Account ID
            salesaccqry = "SELECT id FROM account WHERE company='"+companyid+"' AND name IN ('Sales')";
            salesaccst = conn.prepareStatement(salesaccqry);
            salesaccrs = salesaccst.executeQuery();
            while (salesaccrs.next()) {
                salesAccount = salesaccrs.getString("id");
            
            purchaseaccqry = "SELECT id FROM account WHERE company='"+companyid+"' AND name IN ('Purchases')";
            purchaseaccst = conn.prepareStatement(purchaseaccqry);
            purchaseaccrs = purchaseaccst.executeQuery();
            while (purchaseaccrs.next()) {
                purchaseAccount = purchaseaccrs.getString("id");
            //Get JournalEntry
            //jeQryforAll = "SELECT inv.journalentry from invoice inv INNER JOIN jedetail jed ON inv.journalentry=jed.journalentry where inv.id IN (SELECT invd.invoice from invoicedetails invd INNER JOIN invoice inv ON invd.invoice = inv.id INNER JOIN inventory ivtr ON ivtr.id =  invd.id where invd.company='"+companyid+"' AND ivtr.product IN (select id from product where salesAccount NOT IN (select id from account WHERE name IN ('Sales', 'Cash Sales'))) AND jed.account IN (SELECT id from account WHERE name IN ('Purchases')))";
            jeQry = "SELECT inv.journalentry from invoice inv INNER JOIN jedetail jed ON inv.journalentry=jed.journalentry where inv.id IN (SELECT invd.invoice from invoicedetails invd INNER JOIN invoice inv ON invd.invoice = inv.id INNER JOIN inventory ivtr ON ivtr.id =  invd.id where invd.company='"+companyid+"' AND ivtr.product IN ('4028818a4e05e809014e4c346b32641f') AND jed.account IN (SELECT id from account WHERE name IN ('Purchases')))";
            
            jestmt = conn.prepareStatement(jeQry);
            jeresult = jestmt.executeQuery();
            while (jeresult.next()) {
                journalentry = jeresult.getString("journalentry");
                accqry = "UPDATE jedetail SET account='"+salesAccount+"' WHERE account='"+purchaseAccount+"' AND journalentry='"+journalentry+"' AND company='"+companyid+"'";
                updatest = conn.prepareStatement(accqry);
                int count = 0;
                count = updatest.executeUpdate();
                if(count > 0){
                    out.print("Record is updated Successfully \n");
                } else {
                    out.print("Record is not updated \n");
                }
            }//je while  
          } //purchaseAccount while
        } //salesAccount while
    }//company while   
    } catch(Exception ex){
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
    }
%>