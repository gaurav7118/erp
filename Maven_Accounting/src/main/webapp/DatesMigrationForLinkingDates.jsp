
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
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
        //SCRIPT URL : http://<app-url>/DatesMigrationForLinkingDates.jsp?serverip=?&dbname=?&username=?&password=?
        // 'subdomain' is a optional field

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username)) {
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String userTZDiff = "", defaultTZ = "+08:00", query1 = "", companyid = "";
        String createdby="", userTZQuery = "", prevCreatedBy = "", compcreator="", compName = "", ctzone="", utzone="";
        
        
        PreparedStatement serverpst = null, pst1 = null, pst2 = null, tzpst = null, modifypst = null,cppst=null;
        ResultSet serverrst = null, rst1 = null, rst2 = null, tzrst = null,cprst=null;
        PreparedStatement pst, pstinner;
        ResultSet rst, rstinner;
        int companycount=0;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            //subdomainQuery = " WHERE c.subdomain='" + subdomain + "'";
        }
        
        //Execution Started :
        out.println("<br><br><center>Execution Started @ "+new java.util.Date()+" For Subdomain : "+ subdomain +"</center><br><br>");
        
        Class.forName(driver).newInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        
        conn = DriverManager.getConnection(connectString, username, password);
        conn.setAutoCommit(false);
        
        //Add dummy column to hold original data.
        String addCopyColQry1 = "ALTER TABLE cndetails ADD COLUMN invoicelinkdatecopy BIGINT DEFAULT 0";
        serverpst = conn.prepareStatement(addCopyColQry1);
        serverpst.execute();  
        
        serverpst = null;
        String addCopyColQry2 = "ALTER TABLE dndetails ADD COLUMN grlinkdatecopy BIGINT DEFAULT 0";
        serverpst = conn.prepareStatement(addCopyColQry2);
        serverpst.execute();  
        
        serverpst = null;
        String addCopyColQry3 = "ALTER TABLE linkdetailreceipt ADD COLUMN receiptlinkdatecopy BIGINT DEFAULT 0";
        serverpst = conn.prepareStatement(addCopyColQry3);
        serverpst.execute(); 
        
        serverpst = null;
        String addCopyColQry4 = "ALTER TABLE linkdetailreceipttodebitnote ADD COLUMN receiptlinkdatecopy BIGINT DEFAULT 0";
        serverpst = conn.prepareStatement(addCopyColQry4);
        serverpst.execute();  
        
        serverpst = null;
        String addCopyColQry5 = "ALTER TABLE linkdetailreceipttoadvancepayment ADD COLUMN receiptlinkdatecopy BIGINT DEFAULT 0";
        serverpst = conn.prepareStatement(addCopyColQry5);
        serverpst.execute();  
        
        serverpst = null;
        String addCopyColQry6 = "ALTER TABLE linkdetailpayment ADD COLUMN paymentlinkdatecopy BIGINT DEFAULT 0";
        serverpst = conn.prepareStatement(addCopyColQry6);
        serverpst.execute(); 
        
        serverpst = null;
        String addCopyColQry7 = "ALTER TABLE linkdetailpaymenttocreditnote ADD COLUMN paymentlinkdatecopy BIGINT DEFAULT 0";
        serverpst = conn.prepareStatement(addCopyColQry7);
        serverpst.execute();  
        
        serverpst = null;
        String addCopyColQry8 = "ALTER TABLE linkdetailpaymenttoadvancepayment ADD COLUMN paymentlinkdatecopy BIGINT DEFAULT 0";
        serverpst = conn.prepareStatement(addCopyColQry8);
        serverpst.execute();  
        
        /* --------------------------------------------------------------------------------------------------------------- */
        
        //Copy the data from orginal column to dummy column.
        serverpst = null;
        String copydataqry1 = "UPDATE cndetails SET invoicelinkdatecopy=invoicelinkdate";
        serverpst = conn.prepareStatement(copydataqry1);
        serverpst.execute();
        
        serverpst = null;
        String copydataqry2 = "UPDATE dndetails SET grlinkdatecopy=grlinkdate";
        serverpst = conn.prepareStatement(copydataqry2);
        serverpst.execute();
        
        serverpst = null;
        String copydataqry3 = "UPDATE linkdetailreceipt SET receiptlinkdatecopy=receiptlinkdate";
        serverpst = conn.prepareStatement(copydataqry3);
        serverpst.execute();
        
        serverpst = null;
        String copydataqry4 = "UPDATE linkdetailreceipttodebitnote SET receiptlinkdatecopy=receiptlinkdate";
        serverpst = conn.prepareStatement(copydataqry4);
        serverpst.execute();
        
        serverpst = null;
        String copydataqry5 = "UPDATE linkdetailreceipttoadvancepayment SET receiptlinkdatecopy=receiptlinkdate";
        serverpst = conn.prepareStatement(copydataqry5);
        serverpst.execute();
        
        serverpst = null;
        String copydataqry6 = "UPDATE linkdetailpayment SET paymentlinkdatecopy=paymentlinkdate";
        serverpst = conn.prepareStatement(copydataqry6);
        serverpst.execute();
        
        serverpst = null;
        String copydataqry7 = "UPDATE linkdetailpaymenttocreditnote SET paymentlinkdatecopy=paymentlinkdate";
        serverpst = conn.prepareStatement(copydataqry7);
        serverpst.execute();
        
        serverpst = null;
        String copydataqry8 = "UPDATE linkdetailpaymenttoadvancepayment SET paymentlinkdatecopy=paymentlinkdate";
        serverpst = conn.prepareStatement(copydataqry8);
        serverpst.execute();
        
        /* --------------------------------------------------------------------------------------------------------------- */
        
        //Delete the original column.
        serverpst = null;        
        String delcolqry1 = "ALTER TABLE cndetails DROP COLUMN invoicelinkdate";
        serverpst = conn.prepareStatement(delcolqry1);
        serverpst.execute();
        
        serverpst = null;        
        String delcolqry2 = "ALTER TABLE dndetails DROP COLUMN grlinkdate";
        serverpst = conn.prepareStatement(delcolqry2);
        serverpst.execute();
        
        serverpst = null;        
        String delcolqry3 = "ALTER TABLE linkdetailreceipt DROP COLUMN receiptlinkdate";
        serverpst = conn.prepareStatement(delcolqry3);
        serverpst.execute();
        
        serverpst = null;        
        String delcolqry4 = "ALTER TABLE linkdetailreceipttodebitnote DROP COLUMN receiptlinkdate";
        serverpst = conn.prepareStatement(delcolqry4);
        serverpst.execute();
        
        serverpst = null;        
        String delcolqry5 = "ALTER TABLE linkdetailreceipttoadvancepayment DROP COLUMN receiptlinkdate";
        serverpst = conn.prepareStatement(delcolqry5);
        serverpst.execute();
        
        serverpst = null;        
        String delcolqry6 = "ALTER TABLE linkdetailpayment DROP COLUMN paymentlinkdate";
        serverpst = conn.prepareStatement(delcolqry6);
        serverpst.execute();
        
        serverpst = null;        
        String delcolqry7 = "ALTER TABLE linkdetailpaymenttocreditnote DROP COLUMN paymentlinkdate";
        serverpst = conn.prepareStatement(delcolqry7);
        serverpst.execute();
        
        serverpst = null;        
        String delcolqry8 = "ALTER TABLE linkdetailpaymenttoadvancepayment DROP COLUMN paymentlinkdate";
        serverpst = conn.prepareStatement(delcolqry8);
        serverpst.execute();
        
        /* --------------------------------------------------------------------------------------------------------------- */
        
        //Add the column with DATE Data-Type.
        serverpst = null;      
        String addColQry1 = "ALTER TABLE cndetails ADD COLUMN invoicelinkdate DATE DEFAULT '1970-01-01' AFTER goodsReceiptRow";
        serverpst = conn.prepareStatement(addColQry1);
        serverpst.execute();
        
        serverpst = null;      
        String addColQry2 = "ALTER TABLE dndetails ADD COLUMN grlinkdate DATE DEFAULT '1970-01-01' AFTER invoiceRow";
        serverpst = conn.prepareStatement(addColQry2);
        serverpst.execute();
        
        serverpst = null;      
        String addColQry3 = "ALTER TABLE linkdetailreceipt ADD COLUMN receiptlinkdate DATE DEFAULT '1970-01-01' AFTER linkedgainlossje";
        serverpst = conn.prepareStatement(addColQry3);
        serverpst.execute();
        
        serverpst = null;      
        String addColQry4 = "ALTER TABLE linkdetailreceipttodebitnote ADD COLUMN receiptlinkdate DATE DEFAULT '1970-01-01' AFTER linkedgainlossje";
        serverpst = conn.prepareStatement(addColQry4);
        serverpst.execute();
        
        serverpst = null;      
        String addColQry5 = "ALTER TABLE linkdetailreceipttoadvancepayment ADD COLUMN receiptlinkdate DATE DEFAULT '1970-01-01' AFTER linkedgainlossje";
        serverpst = conn.prepareStatement(addColQry5);
        serverpst.execute();
        
        serverpst = null;      
        String addColQry6 = "ALTER TABLE linkdetailpayment ADD COLUMN paymentlinkdate DATE DEFAULT '1970-01-01' AFTER linkedgainlossje";
        serverpst = conn.prepareStatement(addColQry6);
        serverpst.execute();
        
        serverpst = null;      
        String addColQry7 = "ALTER TABLE linkdetailpaymenttocreditnote ADD COLUMN paymentlinkdate DATE DEFAULT '1970-01-01' AFTER linkedgainlossje";
        serverpst = conn.prepareStatement(addColQry7);
        serverpst.execute();
        
        serverpst = null;      
        String addColQry8 = "ALTER TABLE linkdetailpaymenttoadvancepayment ADD COLUMN paymentlinkdate DATE DEFAULT '1970-01-01' AFTER linkedgainlossje";
        serverpst = conn.prepareStatement(addColQry8);
        serverpst.execute();
        
        /* --------------------------------------------------------------------------------------------------------------- */
        
        query1 = "SELECT c.companyid, c.creator, c.subdomain, c.timezone AS ctzone, u.timezone AS utzone FROM company c INNER JOIN users u "
                + "ON c.creator=u.userid " + subdomainQuery;
        pst1 = conn.prepareStatement(query1);
        rst1 = pst1.executeQuery();
        while (rst1.next()) {
                companycount++;
                companyid = rst1.getString("companyid");
                compName = rst1.getString("subdomain");
                compcreator = rst1.getObject("creator") != null ? rst1.getString("creator") : "";
                if (StringUtil.isNullOrEmpty(compcreator)) {
                    compcreator = "";
                    continue;
                }
                ctzone = rst1.getObject("ctzone") != null ? rst1.getString("ctzone") : "268";   //Default Singapore
                utzone = rst1.getObject("utzone") != null ? rst1.getString("utzone") : ctzone;  
                cppst=null; cprst=null;
                System.out.println(companycount +" : "+compName);
                out.println("<center><b>"+companycount+" : "+compName+"</b></center><br><br>");
                
                String defaultTimezone = "SELECT difference from timezone WHERE timezoneid='"+utzone+"'";
                cppst = conn.prepareStatement(defaultTimezone);
                cprst = cppst.executeQuery();
                if (cprst.next()) {
                    defaultTZ = cprst.getString("difference")!=null?cprst.getString("difference"): "+08:00";
                }
                
    //======================================================= CREDIT NOTE MODULE - START ===========================================================
                    System.out.println("Credit Note Module Started...\n");
                    out.println("<center>Table Name : CreditNoteDetail (cndetails)</center><br><br>");                    
                    prevCreatedBy = "";
                    pst2 = null;
                    rst2 = null;
                    String cnlinkdetail = "SELECT cnd.id AS linkid, cnd.creditNote, cn.cnnumber, cnd.invoicelinkdatecopy, cn.createdby FROM cndetails cnd INNER JOIN creditnote cn ON cnd.creditNote=cn.id WHERE cnd.company='"+companyid+"' ORDER BY cn.cnnumber";                   
                    pst2 = conn.prepareStatement(cnlinkdetail);
                    rst2 = pst2.executeQuery();
                    tzpst = null;
                    tzrst = null; 
                    int updatecount = 0, totalrec = 0;
                    while (rst2.next()) {
                        boolean iszerodate = false;
                        createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;
                        if (!prevCreatedBy.equalsIgnoreCase(createdby)) {
                            prevCreatedBy = createdby;
                            userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                            tzpst = conn.prepareStatement(userTZQuery);
                            tzrst = tzpst.executeQuery();
                            while (tzrst.next()) {
                                userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                                //df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                            }
                        }//if
                        if(rst2.getLong("invoicelinkdatecopy")==0){
                            iszerodate = true;
                        }
                        String linkid = rst2.getString("linkid");
                        //String creditNoteid = rst2.getString("creditNote");                        
                        Date linkDate = new Date(rst2.getLong("invoicelinkdatecopy"));  //Convert long value into Date Object                        
                        
                        if (linkDate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newlinkDate = "";
                            if(iszerodate){
                                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                newlinkDate = sdf.format(linkDate);
                            } else {
                                newlinkDate = df.format(linkDate);
                            }                            
                            String updateCNDate = "UPDATE cndetails SET invoicelinkdate='" + newlinkDate + "' WHERE id='" + linkid + "'";
                            pstinner = conn.prepareStatement(updateCNDate);
                            int count = pstinner.executeUpdate();
                            if (count > 0) {
                                updatecount++;
                            } else {
                                //TO DO HERE
                            }
                        }
                        totalrec++;
                    }
                    
                    System.out.println("Total Record Count = "+totalrec+"\n");
                    System.out.println("Updated Record Count = "+updatecount+"\n");
                    
                    out.println("<center>Total Record Count = "+totalrec+"</center><br>");
                    out.println("<center>Updated Record Count = "+updatecount+"</center><br><br>");

                    System.out.println("Credit Note Module Ended...\n");
                    
    //======================================================= CREDIT NOTE MODULE - END =============================================================
                    
    //======================================================= DEBIT NOTE MODULE - START ===========================================================
                    System.out.println("Debit Note Module Started...\n");
                    out.println("<center>Table Name : DebitNoteDetail (dndetails)</center><br><br>");                    
                    prevCreatedBy = "";
                    pst2 = null;
                    rst2 = null;
                    String dnlinkdetail = "SELECT dnd.id AS linkid, dnd.debitNote, dn.dnnumber, dnd.grlinkdatecopy, dn.createdby FROM dndetails dnd INNER JOIN debitnote dn ON dnd.debitNote=dn.id WHERE dnd.company='"+companyid+"' ORDER BY dn.dnnumber";                   
                    pst2 = conn.prepareStatement(dnlinkdetail);
                    rst2 = pst2.executeQuery();
                    tzpst = null;
                    tzrst = null; 
                    int updatecount1 = 0, totalrec1 = 0;
                    while (rst2.next()) {
                        boolean iszerodate = false;
                        createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;
                        if (!prevCreatedBy.equalsIgnoreCase(createdby)) {
                            prevCreatedBy = createdby;
                            userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                            tzpst = conn.prepareStatement(userTZQuery);
                            tzrst = tzpst.executeQuery();
                            while (tzrst.next()) {
                                userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                                //df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                            }
                        }//if
                        if(rst2.getLong("grlinkdatecopy")==0){
                            iszerodate = true;
                        }
                        String linkid = rst2.getString("linkid");
                        //String debitNoteid = rst2.getString("debitNote");
                        Date linkDate = new Date(rst2.getLong("grlinkdatecopy"));  //Convert long value into Date Object
                        
                        if (linkDate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newlinkDate = "";
                            if(iszerodate){
                                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                newlinkDate = sdf.format(linkDate);
                            } else {
                                newlinkDate = df.format(linkDate);
                            }
                            String updateCNDate = "UPDATE dndetails SET grlinkdate='" + newlinkDate + "' WHERE id='" + linkid + "'";
                            pstinner = conn.prepareStatement(updateCNDate);
                            int count = pstinner.executeUpdate();
                            if (count > 0) {
                                updatecount1++;
                            } else {
                                //TO DO HERE
                            }
                        }
                        totalrec1++;
                    }
                    
                    System.out.println("Total Record Count = "+totalrec1+"\n");
                    System.out.println("Updated Record Count = "+updatecount1+"\n");
                    
                    out.println("<center>Total Record Count = "+totalrec1+"</center><br>");
                    out.println("<center>Updated Record Count = "+updatecount1+"</center><br><br>");
                    System.out.println("Debit Note Module Ended...\n");
                    
    //======================================================= DEBIT NOTE MODULE - END =============================================================
                    
    //======================================================= RECEIVE PAYMENT MODULE - START ===========================================================
                    System.out.println("Receive Payment Module (LinkDetailReceipt) Started...\n");
                    out.println("<center>Table Name : LinkDetailReceipt (linkdetailreceipt)</center><br><br>");                    
                    prevCreatedBy = "";
                    pst2 = null;
                    rst2 = null;
                    String receiptlinkdetail = "SELECT ldr.id AS linkid, ldr.receipt, rp.receiptnumber, ldr.receiptlinkdatecopy, rp.createdby FROM linkdetailreceipt ldr INNER JOIN receipt rp ON ldr.receipt=rp.id WHERE ldr.company='"+companyid+"' ORDER BY rp.receiptnumber";                   
                    pst2 = conn.prepareStatement(receiptlinkdetail);
                    rst2 = pst2.executeQuery();
                    tzpst = null;
                    tzrst = null; 
                    int updatecount2 = 0, totalrec2 = 0;
                    while (rst2.next()) {
                        boolean iszerodate = false;
                        createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;
                        if (!prevCreatedBy.equalsIgnoreCase(createdby)) {
                            prevCreatedBy = createdby;
                            userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                            tzpst = conn.prepareStatement(userTZQuery);
                            tzrst = tzpst.executeQuery();
                            while (tzrst.next()) {
                                userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                                //df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                            }
                        }//if
                        if(rst2.getLong("receiptlinkdatecopy")==0){
                            iszerodate = true;
                        }
                        String linkid = rst2.getString("linkid");
                        //String receiptid = rst2.getString("receipt");
                        Date linkDate = new Date(rst2.getLong("receiptlinkdatecopy"));  //Convert long value into Date Object
                        
                        if (linkDate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newlinkDate = "";
                            if(iszerodate){
                                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                newlinkDate = sdf.format(linkDate);
                            } else {
                                newlinkDate = df.format(linkDate);
                            }
                            String updateCNDate = "UPDATE linkdetailreceipt SET receiptlinkdate='" + newlinkDate + "' WHERE id='" + linkid + "'";
                            pstinner = conn.prepareStatement(updateCNDate);
                            int count = pstinner.executeUpdate();
                            if (count > 0) {
                                updatecount2++;
                            } else {
                                //TO DO HERE
                            }
                        }
                        totalrec2++;
                    }
                    
                    System.out.println("Total Record Count = "+totalrec2+"\n");
                    System.out.println("Updated Record Count = "+updatecount2+"\n");
                    
                    out.println("<center>Total Record Count = "+totalrec2+"</center><br>");
                    out.println("<center>Updated Record Count = "+updatecount2+"</center><br><br>");

                    System.out.println("Receive Payment Module (LinkDetailReceipt) Ended...\n");
                    
                    /*-----------------------------------------------------------------------------------------------------------------------*/
                    
                    System.out.println("Receive Payment Module (LinkDetailReceiptToDebitNote) Started...\n");
                    out.println("<center>Table Name : LinkDetailReceiptToDebitNote (linkdetailreceipttodebitnote)</center><br><br>");                    
                    prevCreatedBy = "";
                    pst2 = null;
                    rst2 = null;
                    String dnreceiptlinkdetail = "SELECT ldr.id AS linkid, ldr.receipt, rp.receiptnumber, ldr.receiptlinkdatecopy, rp.createdby FROM linkdetailreceipttodebitnote ldr INNER JOIN receipt rp ON ldr.receipt=rp.id WHERE ldr.company='"+companyid+"' ORDER BY rp.receiptnumber";                   
                    pst2 = conn.prepareStatement(dnreceiptlinkdetail);
                    rst2 = pst2.executeQuery();
                    tzpst = null;
                    tzrst = null; 
                    int updatecount3 = 0, totalrec3 = 0;
                    while (rst2.next()) {
                        boolean iszerodate = false;
                        createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;
                        if (!prevCreatedBy.equalsIgnoreCase(createdby)) {
                            prevCreatedBy = createdby;
                            userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                            tzpst = conn.prepareStatement(userTZQuery);
                            tzrst = tzpst.executeQuery();
                            while (tzrst.next()) {
                                userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                                //df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                            }
                        }//if
                        if(rst2.getLong("receiptlinkdatecopy")==0){
                            iszerodate = true;
                        }
                        String linkid = rst2.getString("linkid");
                        //String receiptid = rst2.getString("receipt");
                        Date linkDate = new Date(rst2.getLong("receiptlinkdatecopy"));  //Convert long value into Date Object
                        
                        if (linkDate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newlinkDate = "";
                            if(iszerodate){
                                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                newlinkDate = sdf.format(linkDate);
                            } else {
                                newlinkDate = df.format(linkDate);
                            }
                            String updateCNDate = "UPDATE linkdetailreceipttodebitnote SET receiptlinkdate='" + newlinkDate + "' WHERE id='" + linkid + "'";
                            pstinner = conn.prepareStatement(updateCNDate);
                            int count = pstinner.executeUpdate();
                            if (count > 0) {
                                updatecount3++;
                            } else {
                                //TO DO HERE
                            }
                        }
                        totalrec3++;
                    }
                    
                    System.out.println("Total Record Count = "+totalrec3+"\n");
                    System.out.println("Updated Record Count = "+updatecount3+"\n");
                    
                    out.println("<center>Total Record Count = "+totalrec3+"</center><br>");
                    out.println("<center>Updated Record Count = "+updatecount3+"</center><br><br>");

                    out.println("Receive Payment Module (LinkDetailReceiptToDebitNote) Ended...\n");
                                       
                    /*-----------------------------------------------------------------------------------------------------------------------*/
                    
                    System.out.println("Receive Payment Module (LinkDetailReceiptToAdvancePayment) Started...\n");
                    out.println("<center>Table Name : LinkDetailReceiptToAdvancePayment (linkdetailreceipttoadvancepayment)</center><br><br>");                    
                    prevCreatedBy = "";
                    pst2 = null;
                    rst2 = null;
                    String pyreceiptlinkdetail = "SELECT ldr.id AS linkid, ldr.receipt, rp.receiptnumber, ldr.receiptlinkdatecopy, rp.createdby FROM linkdetailreceipttoadvancepayment ldr INNER JOIN receipt rp ON ldr.receipt=rp.id WHERE ldr.company='"+companyid+"' ORDER BY rp.receiptnumber";                   
                    pst2 = conn.prepareStatement(pyreceiptlinkdetail);
                    rst2 = pst2.executeQuery();
                    tzpst = null;
                    tzrst = null; 
                    int updatecount4= 0, totalrec4 = 0;
                    while (rst2.next()) {
                        boolean iszerodate = false;
                        createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;
                        if (!prevCreatedBy.equalsIgnoreCase(createdby)) {
                            prevCreatedBy = createdby;
                            userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                            tzpst = conn.prepareStatement(userTZQuery);
                            tzrst = tzpst.executeQuery();
                            while (tzrst.next()) {
                                userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                                //df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                            }
                        }//if
                        if(rst2.getLong("receiptlinkdatecopy")==0){
                            iszerodate = true;
                        }
                        String linkid = rst2.getString("linkid");
                        //String receiptid = rst2.getString("receipt");
                        Date linkDate = new Date(rst2.getLong("receiptlinkdatecopy"));  //Convert long value into Date Object
                        
                        if (linkDate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newlinkDate = "";
                            if(iszerodate){
                                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                newlinkDate = sdf.format(linkDate);
                            } else {
                                newlinkDate = df.format(linkDate);
                            }
                            String updateCNDate = "UPDATE linkdetailreceipttoadvancepayment SET receiptlinkdate='" + newlinkDate + "' WHERE id='" + linkid + "'";
                            pstinner = conn.prepareStatement(updateCNDate);
                            int count = pstinner.executeUpdate();
                            if (count > 0) {
                                updatecount4++;
                            } else {
                                //TO DO HERE
                            }
                        }
                        totalrec4++;
                    }                    
                    System.out.println("Total Record Count = "+totalrec4+"\n");
                    System.out.println("Updated Record Count = "+updatecount4+"\n");
                    
                    out.println("<center>Total Record Count = "+totalrec4+"</center><br>");
                    out.println("<center>Updated Record Count = "+updatecount4+"</center><br><br>");

                    out.println("Receive Payment Module (LinkDetailReceiptToAdvancePayment) Ended...\n");                    
                    
    //======================================================= RECEIVE PAYMENT MODULE - END =============================================================         
                    
    //======================================================= MAKE PAYMENT MODULE - START ===========================================================
                    System.out.println("Make Payment Module (linkdetailpayment) Started...\n");
                    out.println("<center>Table Name : LinkDetailPayment (linkdetailpayment)</center><br><br>");                    
                    prevCreatedBy = "";
                    pst2 = null;
                    rst2 = null;
                    String paymentlinkdetail = "SELECT ldr.id AS linkid, ldr.payment, rp.paymentnumber, ldr.paymentlinkdatecopy, rp.createdby FROM linkdetailpayment ldr INNER JOIN payment rp ON ldr.payment=rp.id WHERE ldr.company='"+companyid+"' ORDER BY rp.paymentnumber";                   
                    pst2 = conn.prepareStatement(paymentlinkdetail);
                    rst2 = pst2.executeQuery();
                    tzpst = null;
                    tzrst = null; 
                    int updatecount5 = 0, totalrec5 = 0;
                    while (rst2.next()) {
                        boolean iszerodate = false;
                        createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;
                        if (!prevCreatedBy.equalsIgnoreCase(createdby)) {
                            prevCreatedBy = createdby;
                            userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                            tzpst = conn.prepareStatement(userTZQuery);
                            tzrst = tzpst.executeQuery();
                            while (tzrst.next()) {
                                userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                                //df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                            }
                        }//if
                        if(rst2.getLong("paymentlinkdatecopy")==0){
                            iszerodate = true;
                        }
                        String linkid = rst2.getString("linkid");
                        //String payment = rst2.getString("payment");
                        Date linkDate = new Date(rst2.getLong("paymentlinkdatecopy"));  //Convert long value into Date Object
                        
                        if (linkDate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newlinkDate = "";
                            if(iszerodate){
                                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                newlinkDate = sdf.format(linkDate);
                            } else {
                                newlinkDate = df.format(linkDate);
                            }
                            String updateCNDate = "UPDATE linkdetailpayment SET paymentlinkdate='" + newlinkDate + "' WHERE id='" + linkid + "'";
                            pstinner = conn.prepareStatement(updateCNDate);
                            int count = pstinner.executeUpdate();
                            if (count > 0) {
                                updatecount5++;
                            } else {
                                //TO DO HERE
                            }
                        }
                        totalrec5++;
                    }
                    
                    System.out.println("Total Record Count = "+totalrec5+"\n");
                    System.out.println("Updated Record Count = "+updatecount5+"\n");
                    
                    out.println("<center>Total Record Count = "+totalrec5+"</center><br>");
                    out.println("<center>Updated Record Count = "+updatecount5+"</center><br><br>");

                    System.out.println("Make Payment Module (LinkDetailReceipt) Ended...\n");
                    
    /* ---------------------------------------------------------------------------------------------------------------------------------------- */                
                    
                    System.out.println("Make Payment Module (linkdetailpaymenttocreditnote) Started...\n");
                    out.println("<center>Table Name : LinkDetailPaymentToCreditNote (linkdetailpaymenttocreditnote)</center><br><br>");                    
                    prevCreatedBy = "";
                    pst2 = null;
                    rst2 = null;
                    String cnpaymentlinkdetail = "SELECT ldr.id AS linkid, ldr.payment, rp.paymentnumber, ldr.paymentlinkdatecopy, rp.createdby FROM linkdetailpaymenttocreditnote ldr INNER JOIN payment rp ON ldr.payment=rp.id WHERE ldr.company='"+companyid+"' ORDER BY rp.paymentnumber";                   
                    pst2 = conn.prepareStatement(cnpaymentlinkdetail);
                    rst2 = pst2.executeQuery();
                    tzpst = null;
                    tzrst = null; 
                    int updatecount6 = 0, totalrec6 = 0;
                    while (rst2.next()) {
                        boolean iszerodate = false;
                        createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;
                        if (!prevCreatedBy.equalsIgnoreCase(createdby)) {
                            prevCreatedBy = createdby;
                            userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                            tzpst = conn.prepareStatement(userTZQuery);
                            tzrst = tzpst.executeQuery();
                            while (tzrst.next()) {
                                userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                                //df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                            }
                        }//if
                        if(rst2.getLong("paymentlinkdatecopy")==0){
                            iszerodate = true;
                        }
                        String linkid = rst2.getString("linkid");
                        //String payment = rst2.getString("payment");
                        Date linkDate = new Date(rst2.getLong("paymentlinkdatecopy"));  //Convert long value into Date Object
                        
                        if (linkDate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newlinkDate = "";
                            if(iszerodate){
                                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                newlinkDate = sdf.format(linkDate);
                            } else {
                                newlinkDate = df.format(linkDate);
                            }
                            String updateCNDate = "UPDATE linkdetailpaymenttocreditnote SET paymentlinkdate='" + newlinkDate + "' WHERE id='" + linkid + "'";
                            pstinner = conn.prepareStatement(updateCNDate);
                            int count = pstinner.executeUpdate();
                            if (count > 0) {
                                updatecount6++;
                            } else {
                                //TO DO HERE
                            }
                        }
                        totalrec6++;
                    }
                    
                    System.out.println("Total Record Count = "+totalrec6+"\n");
                    System.out.println("Updated Record Count = "+updatecount6+"\n");
                    
                    out.println("<center>Total Record Count = "+totalrec6+"</center><br>");
                    out.println("<center>Updated Record Count = "+updatecount6+"</center><br><br>");

                    System.out.println("Make Payment Module (LinkDetailPaymentToCreditNote) Ended...\n");    
                    
    /* ---------------------------------------------------------------------------------------------------------------------------------------- */                
                    
                    System.out.println("Make Payment Module (LinkDetailPaymentToAdvancePayment) Started...\n");
                    out.println("<center>Table Name : LinkDetailPaymentToAdvancePayment (linkdetailpaymenttoadvancepayment)</center><br><br>");                    
                    prevCreatedBy = "";
                    pst2 = null;
                    rst2 = null;
                    String advpaymentlinkdetail = "SELECT ldr.id AS linkid, ldr.payment, rp.paymentnumber, ldr.paymentlinkdatecopy, rp.createdby FROM linkdetailpaymenttoadvancepayment ldr INNER JOIN payment rp ON ldr.payment=rp.id WHERE ldr.company='"+companyid+"' ORDER BY rp.paymentnumber";                   
                    pst2 = conn.prepareStatement(advpaymentlinkdetail);
                    rst2 = pst2.executeQuery();
                    tzpst = null;
                    tzrst = null; 
                    int updatecount7 = 0, totalrec7 = 0;
                    while (rst2.next()) {
                        boolean iszerodate = false;
                        createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;
                        if (!prevCreatedBy.equalsIgnoreCase(createdby)) {
                            prevCreatedBy = createdby;
                            userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + createdby + "'";
                            tzpst = conn.prepareStatement(userTZQuery);
                            tzrst = tzpst.executeQuery();
                            while (tzrst.next()) {
                                userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                                //df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                            }
                        }//if
                        if(rst2.getLong("paymentlinkdatecopy")==0){
                            iszerodate = true;
                        }
                        String linkid = rst2.getString("linkid");
                        //String payment = rst2.getString("payment");
                        Date linkDate = new Date(rst2.getLong("paymentlinkdatecopy"));  //Convert long value into Date Object
                        
                        if (linkDate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newlinkDate = "";
                            if(iszerodate){
                                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                                newlinkDate = sdf.format(linkDate);
                            } else {
                                newlinkDate = df.format(linkDate);
                            }
                            String updateCNDate = "UPDATE linkdetailpaymenttoadvancepayment SET paymentlinkdate='" + newlinkDate + "' WHERE id='" + linkid + "'";
                            pstinner = conn.prepareStatement(updateCNDate);
                            int count = pstinner.executeUpdate();
                            if (count > 0) {
                                updatecount7++;
                            } else {
                                //TO DO HERE
                            }
                        }
                        totalrec7++;
                    }
                    
                    System.out.println("Total Record Count = "+totalrec7+"\n");
                    System.out.println("Updated Record Count = "+updatecount7+"\n");
                    
                    out.println("<center>Total Record Count = "+totalrec7+"</center><br>");
                    out.println("<center>Updated Record Count = "+updatecount7+"</center><br><br>");

                    System.out.println("Make Payment Module (LinkDetailPaymentToAdvancePayment) Ended...\n");                   
                    
    //======================================================= MAKE PAYMENT MODULE - START ===========================================================                        
                    
            }//companyid
        
        //Delete the dummy column.
    /*    serverpst = null;        
        String deldummyColqry1 = "ALTER TABLE cndetails DROP COLUMN invoicelinkdatecopy";
        serverpst = conn.prepareStatement(deldummyColqry1);
        serverpst.execute();
        
        serverpst = null;
        String deldummyColqry2 = "ALTER TABLE dndetails DROP COLUMN grlinkdatecopy";
        serverpst = conn.prepareStatement(deldummyColqry2);
        serverpst.execute();
        
        serverpst = null;
        String deldummyColqry3 = "ALTER TABLE linkdetailreceipt DROP COLUMN receiptlinkdatecopy";
        serverpst = conn.prepareStatement(deldummyColqry3);
        serverpst.execute();
        
        serverpst = null;      
        String deldummyColqry4 = "ALTER TABLE linkdetailreceipttodebitnote DROP COLUMN receiptlinkdatecopy";
        serverpst = conn.prepareStatement(deldummyColqry4);
        serverpst.execute();
        
        serverpst = null;      
        String deldummyColqry5 = "ALTER TABLE linkdetailreceipttoadvancepayment DROP COLUMN receiptlinkdatecopy";
        serverpst = conn.prepareStatement(deldummyColqry5);
        serverpst.execute();
        
        serverpst = null;
        String deldummyColqry6 = "ALTER TABLE linkdetailpayment DROP COLUMN paymentlinkdatecopy";
        serverpst = conn.prepareStatement(deldummyColqry6);
        serverpst.execute();
        
        serverpst = null;      
        String deldummyColqry7 = "ALTER TABLE linkdetailpaymenttocreditnote DROP COLUMN paymentlinkdatecopy";
        serverpst = conn.prepareStatement(deldummyColqry7);
        serverpst.execute();
        
        serverpst = null;      
        String deldummyColqry8 = "ALTER TABLE linkdetailpaymenttoadvancepayment DROP COLUMN paymentlinkdatecopy";
        serverpst = conn.prepareStatement(deldummyColqry8);
        serverpst.execute();        
        */  
        conn.commit();
    } catch (Exception ex) {
        conn.rollback();
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
    } finally {

            try {
                if (conn != null) {
                    conn.close();
                    out.println("Connection Closed....<br>");
                    out.println("<br><br><center>Execution Ended @ " + new java.util.Date() + "</center><br><br>");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }//finally
%>