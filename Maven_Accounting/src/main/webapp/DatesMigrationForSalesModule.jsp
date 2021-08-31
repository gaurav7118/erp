
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
        //SCRIPT URL : http://<app-url>/DatesMigrationForSalesModule.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
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
        String tzdifference = "", serverTZQuery = "", serverTZDiff = "", userTZDiff = "", defaultTZ = "+08:00", query1 = "", applyDateQuery = "", companyid = "";
        String createdby="",modifiedby="", userTZQuery = "", prevCreatedBy = "", id = "", JRid="", inid="",compcreator="";
        String ConvertCQDate = "", ConvertShipDate = "", ConvertDueDate = "";
        String convertValidDate = "", soid="", srid="", ConvertSODate="", ConvertMPDate="", ConvertChqDate="";
        String compName = "", ConvertDebtDate = "", ConvertPartyInvDate = "", ctzone="", utzone="";
        
        
        PreparedStatement serverpst = null, pst1 = null, pst2 = null, tzpst = null, modifypst = null,cppst=null;
        ResultSet serverrst = null, rst1 = null, rst2 = null, tzrst = null,cprst=null;
        PreparedStatement pst, pstinner;
        ResultSet rst, rstinner;
        int companycount=0;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE c.subdomain='" + subdomain + "'";
        }
        
        //Execution Started :
        out.println("<br><br>Execution Started @ "+new java.util.Date()+"<br><br>");
        
        Class.forName(driver).newInstance();
        //Get Server's timezone from which we ll convert date to Company Creator's timezone.
        //TimeZone tzone = Calendar.getInstance().getTimeZone();
        //System.out.println("Server Timezone Name : " + tzone.getDisplayName() + "\n");   //O/p E.g.: Indian Standard Time
        //out.println("Server Timezone Name : " + tzone.getDisplayName() + "<br>");
        //String serverTZID = tzone.getID();
        //System.out.println("Server Timezone ID : " + serverTZID + "\n");  //O/p E.g. Asia/Calcutta
        //out.println("Server Timezone ID : " + serverTZID + "<br>");
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        
        conn = DriverManager.getConnection(connectString, username, password);
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
//======================================================= CUSTOMER QUOTATION MODULE - START =========================================================
                System.out.println("Customer Quotation Module Started...\n");
//                out.println("<b>Customer Quotation Module Started...</b><br>");
                prevCreatedBy = "";
                applyDateQuery = "SELECT id, createdby, quotationdate, duedate, shipdate, validdate FROM quotation where company='" + companyid + "' ORDER BY quotationdate";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;                   
                    if(!prevCreatedBy.equalsIgnoreCase(createdby)){
                        prevCreatedBy = createdby;
                        userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='"+createdby+"'";
                        tzpst = conn.prepareStatement(userTZQuery);
                        tzrst = tzpst.executeQuery();
                        while(tzrst.next()){ 
                            userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference"))?tzrst.getString("difference"):defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));  
                        }
                    }//if
                    Date creationdate = rst2.getObject("quotationdate") != null ? (java.util.Date)rst2.getObject("quotationdate") : null;
                    Date shipdate = rst2.getObject("shipdate") != null ? (java.util.Date)rst2.getObject("shipdate") : null;
                    Date duedate = rst2.getObject("duedate") != null ? (java.util.Date)rst2.getObject("duedate") : null;
                    Date validdate = rst2.getObject("validdate") != null ? (java.util.Date)rst2.getObject("validdate") : null;
                    id = rst2.getString("id");

                    //Convert Date from Server's timezone to Admin's timezone
                    if (creationdate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newcreationDate = df.format(creationdate);
                            String updateVQDate = "UPDATE quotation SET quotationdate='" + newcreationDate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateVQDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                               // System.out.println("Updated CQ Date : " + newcreationDate + "\n");
                               // out.println("Updated CQ Date : " + newcreationDate + "<br>");
                            } else {
                                //System.out.println("CQ Date is not updated \n");
                               // out.println("CQ Date is not updated.<br>");
                            }
                        }
                    if (shipdate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newShipDate = df.format(shipdate);
                            String updateShipDate = "UPDATE quotation SET shipdate='" + newShipDate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateShipDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Shipdate Date : " + newShipDate + "\n");
                                //out.println("Updated Shipdate Date : " + newShipDate + "<br>");
                            } else {
                                //System.out.println("Shipdate Date is not updated \n");
                                //out.println("Shipdate Date is not updated.<br>");
                            }
                        }//shipdate
                    if (duedate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newDueDate = df.format(duedate);
                            String updateDueDate = "UPDATE quotation SET duedate='" + newDueDate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateDueDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Duedate Date : " + newDueDate + "\n");
                                //out.println("Updated Duedate Date : " + newDueDate + "<br>");
                            } else {
                                //System.out.println("Duedate Date is not updated \n");
                                //out.println("Duedate Date is not updated.<br>");
                            }
                        }//duedate
                    if (validdate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newValidDate = df.format(validdate);
                            String updateValidDate = "UPDATE quotation SET validdate='" + newValidDate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateValidDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated validdate Date : " + newValidDate + "\n");
                                //out.println("Updated validdate Date : " + newValidDate + "<br>");
                            } else {
                                //System.out.println("validdate is not updated \n");
                                //out.println("validdate is not updated.<br>");
                            }
                        }
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp
                            
                System.out.println("Customer Quotation Module Ended...\n");
//                out.println("<b>Customer Quotation Module Ended...<b><br>");
                
//======================================================= CUSTOMER QUOTATION MODULE - END =========================================================
                

                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
                
//======================================================= SALES ORDER MODULE - START =========================================================
                
                System.out.println("Sales Order Module Started...\n");
//                out.println("<b>Sales Order Module Started...<b><br>");
                prevCreatedBy = ""; pst2 = null; rst2 = null;
                applyDateQuery = "SELECT id, createdby, orderdate, duedate, shipdate FROM salesorder where company='" + companyid + "' ORDER BY orderdate";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null; tzrst = null;
                while (rst2.next()) {
                    createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;                   
                    if(!prevCreatedBy.equalsIgnoreCase(createdby)){
                        prevCreatedBy = createdby;
                        userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='"+createdby+"'";
                        tzpst = conn.prepareStatement(userTZQuery);
                        tzrst = tzpst.executeQuery();
                        while(tzrst.next()){ 
                            userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference"))?tzrst.getString("difference"):defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                        }
                    }//if
                    soid = rst2.getString("id");
                    Date sodate = rst2.getObject("orderdate") != null ? (java.util.Date)rst2.getObject("orderdate") : null;
                    Date shipdate = rst2.getObject("shipdate") != null ? (java.util.Date)rst2.getObject("shipdate") : null;
                    Date duedate = rst2.getObject("duedate") != null ? (java.util.Date)rst2.getObject("duedate") : null;
                    //Convert Date from Server's timezone to Admin's timezone
                    if (sodate != null) {
                            pstinner = null;
                            String newSODate = df.format(sodate);
                            String updateSODate = "UPDATE salesorder SET orderdate='" + newSODate + "' WHERE id='" + soid + "'";
                            pstinner = conn.prepareStatement(updateSODate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                               // System.out.println("Updated SO Date : " + newSODate + "\n");
                                //out.println("Updated SO Date : " + newSODate + "<br>");
                            } else {
                                //System.out.println("SO Date is not updated \n");
                                //out.println("SO Date is not updated.<br>");
                            }
                        }
                    if (shipdate != null) {
                            pstinner = null;
                            String newShipDate = df.format(shipdate);
                            String updateShipDate = "UPDATE salesorder SET shipdate='" + newShipDate + "' WHERE id='" + soid + "'";
                            pstinner = conn.prepareStatement(updateShipDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Shipdate Date : " + newShipDate + "\n");
                                //out.println("Updated Shipdate Date : " + newShipDate + "<br>");
                            } else {
                                //System.out.println("Shipdate Date is not updated \n");
                                //out.println("Shipdate Date is not updated.<br>");
                            }
                        }//shipdate
                    if (duedate != null) {
                            pstinner = null;
                            String newDueDate = df.format(duedate);
                            String updateDueDate = "UPDATE salesorder SET duedate='" + newDueDate + "' WHERE id='" + soid + "'";
                            pstinner = conn.prepareStatement(updateDueDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                               // System.out.println("Updated Duedate Date : " + newDueDate + "\n");
                               // out.println("Updated Duedate Date : " + newDueDate + "<br>");
                            } else {
                                //System.out.println("Duedate Date is not updated \n");
                                //out.println("Duedate Date is not updated.<br>");
                            }
                        }//duedate
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp
                            
                System.out.println("Sales Order Module Ended...\n");
//                out.println("<b>Sales Order Module Ended...<b><br>");
                
//======================================================= SALES ORDER MODULE - END =============================================================
                
                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
                
//======================================================= SALES INVOICE MODULE - START =========================================================
                
                System.out.println("SALES INVOICE Module Started...\n");
//                out.println("<b>SALES INVOICE Module Started...<b><br>");
                prevCreatedBy = ""; pst2 = null; rst2 = null;
                applyDateQuery = "SELECT invoice.id, journalentry.entrydate, journalentry.id as jeid, inventory.id as inid, inventory.updatedate,inventory.isupdated, "
                                +"invoice.createdby, invoice.creationdate, invoice.duedate, invoice.shipdate, invoice.debtclaimeddate, invoice.lastmodifieddate, "
                                +"invoice.porefdate,card.id AS cardid,card.expirydate,cheque.id AS chkid,cheque.duedate AS chkduedate FROM invoice "
                                +"LEFT JOIN invoicedetails ON invoice.id=invoicedetails.invoice "
                                +"LEFT JOIN inventory ON invoicedetails .id=inventory.id "
                                +"LEFT JOIN journalentry ON journalentry.id=invoice.journalentry "
                                +"LEFT JOIN paydetail ON paydetail.id=invoice.paydetail "
                                +"LEFT JOIN card ON card.id=paydetail.card "
                                +"LEFT JOIN cheque ON cheque.id=paydetail.cheque "
                                +"where invoice.company='" + companyid + "' ORDER BY invoice.creationdate";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null; tzrst = null;
                while (rst2.next()) {
                    int isupdated = 1;
                    createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;                   
                    if(!prevCreatedBy.equalsIgnoreCase(createdby)){
                        prevCreatedBy = createdby;
                        userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='"+createdby+"'";
                        tzpst = conn.prepareStatement(userTZQuery);
                        tzrst = tzpst.executeQuery();
                        while(tzrst.next()){ 
                            userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference"))?tzrst.getString("difference"):defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                        }
                    }//if
                    id = rst2.getString("id");
                    JRid = rst2.getString("jeid");
                    inid = rst2.getString("inid");
                    String cardid = rst2.getString("cardid");
                    String chkid = rst2.getString("chkid");
                    Date updatedate = rst2.getObject("updatedate") != null ? (java.util.Date)rst2.getObject("updatedate"):null;
                    Date creationdate = rst2.getObject("creationdate") != null ? (java.util.Date)rst2.getObject("creationdate"):null;
                    Date shipdate = rst2.getObject("shipdate") != null ? (java.util.Date)rst2.getObject("shipdate"):null;
                    Date duedate = rst2.getObject("duedate") != null ? (java.util.Date)rst2.getObject("duedate"):null;
                    Date lastmodifieddate = rst2.getObject("lastmodifieddate") != null ? (java.util.Date)rst2.getObject("lastmodifieddate"):null;
                    Date porefdate=rst2.getObject("porefdate") != null ? (java.util.Date)rst2.getObject("porefdate"):null;
                    Date debtclaimeddate = rst2.getObject("debtclaimeddate") != null ? (java.util.Date)rst2.getObject("debtclaimeddate"):null;
                    Date JRentrydate = rst2.getObject("entrydate") != null ? (java.util.Date)rst2.getObject("entrydate"):null;
                    Date expirydate = rst2.getObject("expirydate") != null ? (java.util.Date)rst2.getObject("expirydate"):null;
                    Date chkduedate = rst2.getObject("chkduedate") != null ? (java.util.Date)rst2.getObject("chkduedate"):null;
                    isupdated = rst2.getInt("isupdated");
                    //Convert Date from Server's timezone to Admin's timezone
                    if (chkduedate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newchkduedate = df.format(chkduedate);
                            String updateSIDate = "UPDATE cheque SET duedate='" + newchkduedate + "' WHERE id='" + chkid + "'";
                            pstinner = conn.prepareStatement(updateSIDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated cheque duedate : " + newchkduedate + "\n");
                               // out.println("Updated cheque expirydate : " + newchkduedate + "<br>");
                            } else {
                               // System.out.println("cheque duedate is not updated \n");
                                //out.println("cheque duedate is not updated.<br>");
                            }
                        }
                    if (expirydate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newexpirydate = df.format(expirydate);
                            String updateSIDate = "UPDATE card SET expirydate='" + newexpirydate + "' WHERE id='" + cardid + "'";
                            pstinner = conn.prepareStatement(updateSIDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated card expirydate : " + newexpirydate + "\n");
                                //out.println("Updated card expirydate : " + newexpirydate + "<br>");
                            } else {
                                //System.out.println("card expirydate is not updated \n");
                                //out.println("card expirydate is not updated.<br>");
                            }
                        }
                    if (updatedate != null && isupdated==0) {
                            pstinner = null;
                            rstinner = null;
                            String newupdatedate = df.format(updatedate);
                            String updateSIDate = "UPDATE inventory SET updatedate='" + newupdatedate + "',isupdated=1 WHERE id='" + inid + "'";
                            pstinner = conn.prepareStatement(updateSIDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated inventory updatedate : " + newupdatedate + "\n");
                                //out.println("Updated inventory updatedate : " + newupdatedate + "<br>");
                            } else {
                                //System.out.println("inventory updatedate is not updated \n");
                                //out.println("inventory updatedate is not updated.<br>");
                            }
                        }
                    if (creationdate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newcreationDate = df.format(creationdate);
                            String updateSIDate = "UPDATE invoice SET creationdate='" + newcreationDate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateSIDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated SI Date : " + newcreationDate + "\n");
                               // out.println("Updated SI Date : " + newcreationDate + "<br>");
                            } else {
                                //System.out.println("SI Date is not updated \n");
                               // out.println("SI Date is not updated.<br>");
                            }
                        }
                    if (shipdate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newShipDate = df.format(shipdate);
                            String updateShipDate = "UPDATE invoice SET shipdate='" + newShipDate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateShipDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Shipdate Date : " + newShipDate + "\n");
                                //out.println("Updated Shipdate Date : " + newShipDate + "<br>");
                            } else {
                                //System.out.println("Shipdate Date is not updated \n");
                               // out.println("Shipdate Date is not updated.<br>");
                            }
                        }//shipdate
                    if (duedate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newDueDate = df.format(duedate);
                            String updateDueDate = "UPDATE invoice SET duedate='" + newDueDate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateDueDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Duedate Date : " + newDueDate + "\n");
                                //out.println("Updated Duedate Date : " + newDueDate + "<br>");
                            } else {
                               // System.out.println("Duedate Date is not updated \n");
                                //out.println("Duedate Date is not updated.<br>");
                            }
                        }//duedate
                    if (debtclaimeddate != null) {
                            pstinner = null;
                            String newDebtDate = df.format(debtclaimeddate);
                            String updateDebtDate = "UPDATE invoice SET debtclaimeddate='" + newDebtDate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateDebtDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated debtclaimeddate Date : " + newDebtDate + "\n");
                               // out.println("Updated debtclaimeddate Date : " + newDebtDate + "<br>");
                            } else {
                                //System.out.println("debtclaimeddate Date is not updated \n");
                                //out.println("debtclaimeddate Date is not updated.<br>");
                            }
                        }
                    if (lastmodifieddate != null) {
                            pstinner = null;
                            String newlastmodifieddate = df.format(lastmodifieddate);
                            String updatelastmodDate = "UPDATE invoice SET lastmodifieddate='" + newlastmodifieddate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updatelastmodDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                               // System.out.println("Updated lastmodifieddate Date : " + newlastmodifieddate + "\n");
                                //out.println("Updated lastmodifieddate Date : " + newlastmodifieddate + "<br>");
                            } else {
                                //System.out.println("lastmodifieddate Date is not updated \n");
                                //out.println("lastmodifieddate Date is not updated.<br>");
                            }
                        }
                    if (porefdate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newporefdate = df.format(porefdate);
                            String updatePartInvDate = "UPDATE invoice SET porefdate='" + newporefdate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updatePartInvDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated porefdate Date : " + newporefdate + "\n");
                                //out.println("Updated porefdate Date : " + newporefdate + "<br>");
                            } else {
                                //System.out.println("porefdate Date is not updated \n");
                                //out.println("porefdate Date is not updated.<br>");
                            }
                        }
                    /*if (JRentrydate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newJRentrydate = df.format(JRentrydate);
                            String updateJEDate = "UPDATE journalentry SET entrydate='" + newJRentrydate + "' WHERE id='" + JRid + "'";
                            pstinner = conn.prepareStatement(updateJEDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                System.out.println("Updated JRentrydate Date : " + newJRentrydate + "\n");
                                out.println("Updated JRentrydate Date : " + newJRentrydate + "<br>");
                            } else {
                                System.out.println("JRentrydate Date is not updated \n");
                                out.println("JRentrydate Date is not updated.<br>");
                            }
                    }*/
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp
                            
                System.out.println("SALES INVOICE Module Ended...\n");
//                out.println("<b>SALES INVOICE Module Ended...<b><br>");
                
//======================================================= SALES INVOICE MODULE - END =========================================================
                
                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
                
//======================================================= RECEIVE PAYMENT MODULE - START =========================================================
                
                System.out.println("Receive Payment Module Started...\n");
//                out.println("<b>Receive Payment Module Started...</b><br>");
                prevCreatedBy = ""; pst2 = null; rst2 = null;
                applyDateQuery = "SELECT receipt.id, journalentry.id as jeid, journalentry.entrydate, receipt.createdby, receipt.creationdate, receipt.chequedate FROM receipt "
                                +"LEFT JOIN journalentry ON journalentry.id=receipt.journalentry "
                                +"where receipt.company='" + companyid + "' ORDER BY creationdate";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null; tzrst = null;
                while (rst2.next()) {
                    createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;                   
                    if(!prevCreatedBy.equalsIgnoreCase(createdby)){
                        prevCreatedBy = createdby;
                        userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='"+createdby+"'";
                        tzpst = conn.prepareStatement(userTZQuery);
                        tzrst = tzpst.executeQuery();
                        while(tzrst.next()){ 
                            userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference"))?tzrst.getString("difference"):defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                        }
                    }//if
                    id = rst2.getString("id");
                    JRid = rst2.getString("jeid");
                    Date JEentrydate = rst2.getObject("entrydate") != null ? (java.util.Date)rst2.getObject("entrydate"):null;
                    Date creationdate = rst2.getObject("creationdate") != null ? (java.util.Date)rst2.getObject("creationdate") : null;
                    Date chequedate = rst2.getObject("chequedate") != null ? (java.util.Date)rst2.getObject("chequedate") : null;

                    //Convert Date from Server's timezone to Admin's timezone
                    if (creationdate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newcreationDate = df.format(creationdate);
                            String updateMPDate = "UPDATE receipt SET creationdate='" + newcreationDate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateMPDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated RP Creation Date : " + newcreationDate + "\n");
                                //out.println("Updated RP Creation Date : " + newcreationDate + "<br>");
                            } else {
                                //System.out.println("RP Creation Date is not updated \n");
                                //out.println("RP Creation Date is not updated.<br>");
                            }
                    }
                    if (chequedate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newChequeDate = df.format(chequedate);
                            String updateChqDate = "UPDATE receipt SET chequedate='" + newChequeDate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateChqDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated chequedate : " + newChequeDate + "\n");
                                //out.println("Updated chequedate : " + newChequeDate + "<br>");
                            } else {
                                //System.out.println("Chequedate is not updated \n");
                                //out.println("Chequedate is not updated.<br>");
                            }
                    }//shipdate
/*                    if (JEentrydate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newJEentrydate = df.format(JEentrydate);
                            String updateChqDate = "UPDATE journalentry SET entrydate='" + newJEentrydate + "' WHERE id='" + JRid + "'";
                            pstinner = conn.prepareStatement(updateChqDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                System.out.println("Updated JEdate : " + newJEentrydate + "\n");
                                out.println("Updated JEdate : " + newJEentrydate + "<br>");
                            } else {
                                System.out.println("JEdate is not updated \n");
                                out.println("JEdate is not updated.<br>");
                            }
                    }               */
                    
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp
                            
                System.out.println("Receive Payment Module Ended...\n");
//                out.println("<b>Receive Payment Module Ended...<b><br>");
                
//======================================================= RECEIVE PAYMENT MODULE - END =========================================================
                
                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
                
//======================================================= SALES RETURN MODULE - START ===========================================================
                System.out.println("Sales Return Module Started...\n");
//                out.println("<b>Sales Return Module Started...<b><br>");
                prevCreatedBy = ""; pst2 = null; rst2 = null;
                applyDateQuery = "SELECT salesreturn.id as id, createdby, orderdate, shipdate, inventory.id as inid, inventory.updatedate,inventory.isupdated,in_consignment.id AS conid,"
                                +"in_consignment.fromdate,in_consignment.todate,in_consignment.createdon,in_consignmentdetails.id AS condid,in_consignmentdetails.modifiedon,"
                                +"in_stockmovement.id AS stkid,in_stockmovement.createdon AS stkcreatedon,in_stockmovement.transaction_date FROM salesreturn "
                                +"LEFT JOIN srdetails ON salesreturn.id=srdetails.salesreturn "
                                +"LEFT JOIN inventory ON srdetails.id=inventory.id "
                                +"LEFT JOIN in_consignment ON in_consignment.modulerefid=salesreturn.id "
                                +"LEFT JOIN in_consignmentdetails ON in_consignment.id=in_consignmentdetails.consignment "
                                +"LEFT JOIN in_stockmovement ON in_consignment.modulerefid=in_stockmovement.modulerefid "
                                +"where salesreturn.company='" + companyid + "' ORDER BY orderdate";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null; tzrst = null;
                while (rst2.next()) {
                    int isupdated = 1;
                    createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;                   
                    if(!prevCreatedBy.equalsIgnoreCase(createdby)){
                        prevCreatedBy = createdby;
                        userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='"+createdby+"'";
                        tzpst = conn.prepareStatement(userTZQuery);
                        tzrst = tzpst.executeQuery();
                        while(tzrst.next()){ 
                            userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference"))?tzrst.getString("difference"):defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                        }
                    }//if
                    srid = rst2.getString("id");
                    inid = rst2.getString("inid");
                    String conid = rst2.getString("conid");
                    String condid = rst2.getString("condid");
                    String stkid = rst2.getString("stkid");
                    Date srdate = rst2.getObject("orderdate") != null ? (java.util.Date)rst2.getObject("orderdate") : null;
                    Date shipdate = rst2.getObject("shipdate") != null ? (java.util.Date)rst2.getObject("shipdate") : null;
                    Date updatedate = rst2.getObject("updatedate") != null ? (java.util.Date)rst2.getObject("updatedate") : null;
                    Date fromdate = rst2.getObject("fromdate") != null ? (java.util.Date)rst2.getObject("fromdate") : null;
                    Date todate = rst2.getObject("todate") != null ? (java.util.Date)rst2.getObject("todate") : null;
                    Date createdon = rst2.getObject("createdon") != null ? (java.util.Date)rst2.getObject("createdon") : null;
                    Date modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date)rst2.getObject("modifiedon") : null;
                    Date stkcreatedon = rst2.getObject("stkcreatedon") != null ? (java.util.Date)rst2.getObject("stkcreatedon") : null;
                    Date transaction_date = rst2.getObject("transaction_date") != null ? (java.util.Date)rst2.getObject("transaction_date") : null;
                    isupdated = rst2.getInt("isupdated");
                    //Convert Date from Server's timezone to Admin's timezone
                    
                    if (stkcreatedon != null) {
                            pstinner = null;
                            rstinner = null;
                            String newstkcreatedon = df.format(stkcreatedon);
                            String updateSRDate = "UPDATE in_stockmovement SET createdon='" + newstkcreatedon + "' WHERE id='" + stkid + "'";
                            pstinner = conn.prepareStatement(updateSRDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated in_stockmovement createdon : " + newstkcreatedon + "\n");
                                //out.println("Updated in_stockmovement createdon : " + newstkcreatedon + "<br>");
                            } else {
                                //System.out.println("in_stockmovement createdon is not updated \n");
                                //out.println("in_stockmovement createdon is not updated.<br>");
                            }
                        }
                    if (transaction_date != null) {
                            pstinner = null;
                            rstinner = null;
                            String newtransaction_date = df.format(transaction_date);
                            String updateSRDate = "UPDATE in_stockmovement SET transaction_date='" + newtransaction_date + "' WHERE id='" + stkid + "'";
                            pstinner = conn.prepareStatement(updateSRDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated in_stockmovement transaction_date : " + newtransaction_date + "\n");
                                //out.println("Updated in_stockmovement transaction_date : " + newtransaction_date + "<br>");
                            } else {
                                //System.out.println("in_stockmovement transaction_date is not updated \n");
                                //out.println("in_stockmovement transaction_date is not updated.<br>");
                            }
                        }
                    if (fromdate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newfromdate = df.format(fromdate);
                            String updateSRDate = "UPDATE in_consignment SET fromdate='" + newfromdate + "' WHERE id='" + conid + "'";
                            pstinner = conn.prepareStatement(updateSRDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated in_consignment fromdate : " + newfromdate + "\n");
                                //out.println("Updated in_consignment fromdate : " + newfromdate + "<br>");
                            } else {
                                //System.out.println("in_consignment fromdate is not updated \n");
                                //out.println("in_consignment fromdate is not updated.<br>");
                            }
                        }
                    if (todate != null) {
                            pstinner = null;
                            rstinner = null;
                            String newtodate = df.format(todate);
                            String updateSRDate = "UPDATE in_consignment SET todate='" + newtodate + "' WHERE id='" + conid + "'";
                            pstinner = conn.prepareStatement(updateSRDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated in_consignment todate : " + newtodate + "\n");
                                //out.println("Updated in_consignment todate : " + newtodate + "<br>");
                            } else {
                                //System.out.println("in_consignment todate is not updated \n");
                                //out.println("in_consignment todate is not updated.<br>");
                            }
                        }
                    if (createdon != null) {
                            pstinner = null;
                            rstinner = null;
                            String newcreatedon = df.format(createdon);
                            String updateSRDate = "UPDATE in_consignment SET createdon='" + newcreatedon + "' WHERE id='" + conid + "'";
                            pstinner = conn.prepareStatement(updateSRDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated in_consignment createdon : " + newcreatedon + "\n");
                                //out.println("Updated in_consignment createdon : " + newcreatedon + "<br>");
                            } else {
                                //System.out.println("in_consignment createdon is not updated \n");
                                //out.println("in_consignment createdon is not updated.<br>");
                            }
                        }
                    if (modifiedon != null) {
                            pstinner = null;
                            rstinner = null;
                            String newmodifiedon = df.format(modifiedon);
                            String updateSRDate = "UPDATE in_consignmentdetails SET modifiedon='" + newmodifiedon + "' WHERE id='" + condid + "'";
                            pstinner = conn.prepareStatement(updateSRDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated in_consignmentdetails modifiedon : " + newmodifiedon + "\n");
                                //out.println("Updated in_consignmentdetails modifiedon : " + newmodifiedon + "<br>");
                            } else {
                                //System.out.println("in_consignmentdetails modifiedon is not updated \n");
                                //out.println("in_consignmentdetails modifiedon is not updated.<br>");
                            }
                        }
                    if (updatedate != null && isupdated==0) {
                            pstinner = null;
                            rstinner = null;
                            String newupdatedate = df.format(updatedate);
                            String updateSRDate = "UPDATE inventory SET updatedate='" + newupdatedate + "',isupdated=1 WHERE id='" + inid + "'";
                            pstinner = conn.prepareStatement(updateSRDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated inventory updatedate : " + newupdatedate + "\n");
                                //out.println("Updated inventory updatedate : " + newupdatedate + "<br>");
                            } else {
                                //System.out.println("inventory updatedate is not updated \n");
                                //out.println("inventory updatedate is not updated.<br>");
                            }
                        }
                    if (srdate != null) {
                            pstinner = null;
                            String newSRDate = df.format(srdate);
                            String updateSRDate = "UPDATE salesreturn SET orderdate='" + newSRDate + "' WHERE id='" + srid + "'";
                            pstinner = conn.prepareStatement(updateSRDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                               // System.out.println("Updated SR Date : " + newSRDate + "\n");
                                //out.println("Updated SR Date : " + newSRDate + "<br>");
                            } else {
                               // System.out.println("SR Date is not updated \n");
                                //out.println("SR Date is not updated.<br>");
                            }
                        }
                    if (shipdate != null) {
                            pstinner = null;
                            String newShipDate = df.format(shipdate);
                            String updateShipDate = "UPDATE salesreturn SET shipdate='" + newShipDate + "' WHERE id='" + srid + "'";
                            pstinner = conn.prepareStatement(updateShipDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Shipdate Date : " + newShipDate + "\n");
                                //out.println("Updated Shipdate Date : " + newShipDate + "<br>");
                            } else {
                                //System.out.println("Shipdate Date is not updated \n");
                                //out.println("Shipdate Date is not updated.<br>");
                            }
                        }//shipdate
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp

                System.out.println("Sales Return Module Ended...\n");
//                out.println("<b>Sales Return Module Ended...<b><br>");
//======================================================= SALES RETURN MODULE - END =============================================================   
                
                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
                
//======================================================= CREDIT NOTE MODULE - START ===========================================================
                System.out.println("Credit Note Module Started...\n");
//                out.println("<b>Credit Note Module Started...</b><br>");
                prevCreatedBy = ""; pst2 = null; rst2 = null;
                applyDateQuery = "SELECT creditnote.id, journalentry.id as jeid, journalentry.entrydate, creditnote.createdby, creditnote.creationdate FROM creditnote "
                                +"LEFT JOIN journalentry ON journalentry.id=creditnote.journalentry "
                                +"where creditnote.company='" + companyid + "' ORDER BY creationdate";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null; tzrst = null;
                while (rst2.next()) {
                    createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;                   
                    if(!prevCreatedBy.equalsIgnoreCase(createdby)){
                        prevCreatedBy = createdby;
                        userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='"+createdby+"'";
                        tzpst = conn.prepareStatement(userTZQuery);
                        tzrst = tzpst.executeQuery();
                        while(tzrst.next()){ 
                            userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference"))?tzrst.getString("difference"):defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                        }
                    }//if
                    id = rst2.getString("id");
                    JRid = rst2.getString("jeid");
                    Date JEentrydate = rst2.getObject("entrydate") != null ? (java.util.Date)rst2.getObject("entrydate"):null;
                    Date creationdate = rst2.getObject("creationdate") != null ? (java.util.Date)rst2.getObject("creationdate") : null;

                    //Convert Date from Server's timezone to Admin's timezone
                    if (creationdate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newcreationDate = df.format(creationdate);
                            String updateCNDate = "UPDATE creditnote SET creationdate='" + newcreationDate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateCNDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated CN Creation Date : " + newcreationDate + "\n");
                                //out.println("Updated CN Creation Date : " + newcreationDate + "<br>");
                            } else {
                                //System.out.println("CN Creation Date is not updated \n");
                                //out.println("CN Creation Date is not updated.<br>");
                            }
                    }
/*                    if (JEentrydate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newJEentrydate = df.format(JEentrydate);
                            String updateChqDate = "UPDATE journalentry SET entrydate='" + newJEentrydate + "' WHERE id='" + JRid + "'";
                            pstinner = conn.prepareStatement(updateChqDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                System.out.println("Updated JEdate : " + newJEentrydate + "\n");
                                out.println("Updated JEdate : " + newJEentrydate + "<br>");
                            } else {
                                System.out.println("JEdate is not updated \n");
                                out.println("JEdate is not updated.<br>");
                            }
                        }                   */
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp
                            
                System.out.println("Credit Note Module Ended...\n");
//                out.println("<b>Credit Note Module Ended...<b><br>");
//======================================================= CREDIT NOTE MODULE - END =============================================================
                
                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
                
//======================================================= LEASE CONTRACT MODULE - START ===========================================================
                System.out.println("Lease Contract Module Started...\n");
//                out.println("<b>Lease Contract Module Started...</b><br>");
                prevCreatedBy = ""; pst2 = null; rst2 = null;
                applyDateQuery = "SELECT contract.id,contractdates.id AS cdid,servicedetails.id AS serid,createdby,orderdate,originalenddate,fromdate,todate,contract.enddate,signdate,movedate,moveoutdate,servicedetails.servicedate,contractdates.startdate,contractdates.enddate AS cenddate FROM contract "
                                +"LEFT JOIN servicedetails ON servicedetails.id=contract.parentContract "
                                +"LEFT JOIN contractdates ON contractdates.contract=contract.id "
                                +"where contract.company='" + companyid + "' ORDER BY orderdate";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null; tzrst = null;
                while (rst2.next()) {
                    createdby = rst2.getObject("createdby") != null ? rst2.getObject("createdby").toString() : compcreator;                   
                    if(!prevCreatedBy.equalsIgnoreCase(createdby)){
                        prevCreatedBy = createdby;
                        userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='"+createdby+"'";
                        tzpst = conn.prepareStatement(userTZQuery);
                        tzrst = tzpst.executeQuery();
                        while(tzrst.next()){ 
                            userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference"))?tzrst.getString("difference"):defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                        }
                    }//if
                    id = rst2.getString("id");
                    String serid = rst2.getString("serid");
                    String cdid = rst2.getString("cdid");
                    Date orderdate = rst2.getObject("orderdate") != null ? (java.util.Date)rst2.getObject("orderdate") : null;
                    Date originalenddate = rst2.getObject("originalenddate") != null ? (java.util.Date)rst2.getObject("originalenddate") : null;
                    Date fromdate = rst2.getObject("fromdate") != null ? (java.util.Date)rst2.getObject("fromdate") : null;
                    Date todate = rst2.getObject("todate") != null ? (java.util.Date)rst2.getObject("todate") : null;
                    Date enddate = rst2.getObject("enddate") != null ? (java.util.Date)rst2.getObject("enddate") : null;
                    Date signdate = rst2.getObject("signdate") != null ? (java.util.Date)rst2.getObject("signdate") : null;
                    Date movedate = rst2.getObject("movedate") != null ? (java.util.Date)rst2.getObject("movedate") : null;
                    Date moveoutdate = rst2.getObject("moveoutdate") != null ? (java.util.Date)rst2.getObject("moveoutdate") : null;
                    Date servicedate = rst2.getObject("servicedate") != null ? (java.util.Date)rst2.getObject("servicedate") : null;
                    Date startdate = rst2.getObject("startdate") != null ? (java.util.Date)rst2.getObject("startdate") : null;
                    Date cenddate = rst2.getObject("cenddate") != null ? (java.util.Date)rst2.getObject("cenddate") : null;

                    //Convert Date from Server's timezone to Admin's timezone
                    if (startdate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newstartdate = df.format(startdate);
                            String updateCONDate = "UPDATE contractdates SET startdate='" + newstartdate + "' WHERE id='" + cdid + "'";
                            pstinner = conn.prepareStatement(updateCONDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated contractdates startdate Date : " + newstartdate + "\n");
                                //out.println("Updated contractdates startdate Date : " + newstartdate + "<br>");
                            } else {
                                //System.out.println("contractdates startdate Date is not updated \n");
                                //out.println("contractdates startdate Date is not updated.<br>");
                            }
                    }if (cenddate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newcenddate = df.format(cenddate);
                            String updateCONDate = "UPDATE contractdates SET enddate='" + newcenddate + "' WHERE id='" + cdid + "'";
                            pstinner = conn.prepareStatement(updateCONDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated contractdates enddate Date : " + newcenddate + "\n");
                                //out.println("Updated contractdates enddate Date : " + newcenddate + "<br>");
                            } else {
                                //System.out.println("contractdates enddate Date is not updated \n");
                                //out.println("contractdates enddate Date is not updated.<br>");
                            }
                    }
                    if (orderdate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String neworderdate = df.format(orderdate);
                            String updateCONDate = "UPDATE contract SET orderdate='" + neworderdate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateCONDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Contract orderdate Date : " + neworderdate + "\n");
                                //out.println("Updated Contract orderdate Date : " + neworderdate + "<br>");
                            } else {
                                //System.out.println("Contract orderdate Date is not updated \n");
                                //out.println("Contract orderdate Date is not updated.<br>");
                            }
                    }
                    if (originalenddate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String neworiginalenddate = df.format(originalenddate);
                            String updateCONDate = "UPDATE contract SET originalenddate='" + neworiginalenddate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateCONDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Contract originalenddate Date : " + neworiginalenddate + "\n");
                                //out.println("Updated Contract originalenddate Date : " + neworiginalenddate + "<br>");
                            } else {
                                //System.out.println("Contract originalenddate Date is not updated \n");
                                //out.println("Contract originalenddate Date is not updated.<br>");
                            }
                    }
                    if (fromdate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newfromdate = df.format(fromdate);
                            String updateCONDate = "UPDATE contract SET fromdate='" + newfromdate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateCONDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Contract fromdate Date : " + newfromdate + "\n");
                                //out.println("Updated Contract fromdate Date : " + newfromdate + "<br>");
                            } else {
                                //System.out.println("Contract fromdate Date is not updated \n");
                                //out.println("Contract fromdate Date is not updated.<br>");
                            }
                    }
                    if (todate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newtodate = df.format(todate);
                            String updateCONDate = "UPDATE contract SET todate='" + newtodate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateCONDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Contract todate Date : " + newtodate + "\n");
                                //out.println("Updated Contract todate Date : " + newtodate + "<br>");
                            } else {
                                //System.out.println("Contract todate Date is not updated \n");
                                //out.println("Contract todate Date is not updated.<br>");
                            }
                    }
                    if (enddate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newenddate = df.format(enddate);
                            String updateCONDate = "UPDATE contract SET enddate='" + newenddate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateCONDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Contract enddate Date : " + newenddate + "\n");
                                //out.println("Updated Contract enddate Date : " + newenddate + "<br>");
                            } else {
                                //System.out.println("Contract enddate Date is not updated \n");
                                //out.println("Contract enddate Date is not updated.<br>");
                            }
                    }
                    if (signdate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newsigndate = df.format(signdate);
                            String updateCONDate = "UPDATE contract SET signdate='" + newsigndate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateCONDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Contract signdate Date : " + newsigndate + "\n");
                                //out.println("Updated Contract signdate Date : " + newsigndate + "<br>");
                            } else {
                                //System.out.println("Contract signdate Date is not updated \n");
                                //out.println("Contract signdate Date is not updated.<br>");
                            }
                    }
                    if (movedate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newmovedate = df.format(movedate);
                            String updateCONDate = "UPDATE contract SET movedate='" + newmovedate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateCONDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Contract movedate Date : " + newmovedate + "\n");
                                //out.println("Updated Contract movedate Date : " + newmovedate + "<br>");
                            } else {
                                //System.out.println("Contract movedate Date is not updated \n");
                                //out.println("Contract movedate Date is not updated.<br>");
                            }
                    }
                    if (moveoutdate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newmoveoutdate = df.format(moveoutdate);
                            String updateCONDate = "UPDATE contract SET moveoutdate='" + newmoveoutdate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateCONDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Contract moveoutdate Date : " + newmoveoutdate + "\n");
                                //out.println("Updated Contract moveoutdate Date : " + newmoveoutdate + "<br>");
                            } else {
                                //System.out.println("Contract moveoutdate Date is not updated \n");
                                //out.println("Contract moveoutdate Date is not updated.<br>");
                            }
                    }
                    if (servicedate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newservicedate = df.format(servicedate);
                            String updateSERDate = "UPDATE servicedetails SET servicedate='" + newservicedate + "' WHERE id='" + serid + "'";
                            pstinner = conn.prepareStatement(updateSERDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated Contract servicedate Date : " + newservicedate + "\n");
                                //out.println("Updated Contract servicedate Date : " + newservicedate + "<br>");
                            } else {
                                //System.out.println("Contract servicedate Date is not updated \n");
                                //out.println("Contract servicedate Date is not updated.<br>");
                            }
                    }
                    
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp
                            
                System.out.println("Lease Contract Module Ended...\n");
//                out.println("<b>Lease Contract Module Ended...<b><br>");
//======================================================= LEASE CONTRACT MODULE - END =============================================================
                
                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
                
//======================================================= ASSET MAINTENANCE SCHEDULE MODULE - START ===========================================================
                System.out.println("Asset Maintenance Schedule Module Started...\n");
//                out.println("<b>Asset Maintenance Schedule Module Started...</b><br>");
                prevCreatedBy = ""; pst2 = null; rst2 = null;
                applyDateQuery = "SELECT assetdetail.id AS assetdid,assetmaintenanceschedulerobject.id AS assetmid,startdate,enddate,installationdate From assetdetail "
                                +"LEFT JOIN assetmaintenanceschedulerobject ON assetmaintenanceschedulerobject.assetdetails=assetdetail.id "
                                +"where assetdetail.company='" + companyid + "' ORDER BY installationdate";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null; tzrst = null;
                while (rst2.next()) {
                    createdby = compcreator;
                    if(!prevCreatedBy.equalsIgnoreCase(createdby)){
                        prevCreatedBy = createdby;
                        userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='"+createdby+"'";
                        tzpst = conn.prepareStatement(userTZQuery);
                        tzrst = tzpst.executeQuery();
                        while(tzrst.next()){ 
                            userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference"))?tzrst.getString("difference"):defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                        }
                    }//if
                    String assetdid = rst2.getString("assetdid");
                    String assetmid = rst2.getString("assetmid");
                    Date installationdate = rst2.getObject("installationdate") != null ? (java.util.Date)rst2.getObject("installationdate") : null;
                    Date startdate = rst2.getObject("startdate") != null ? (java.util.Date)rst2.getObject("startdate") : null;
                    Date enddate = rst2.getObject("enddate") != null ? (java.util.Date)rst2.getObject("enddate") : null;

                    //Convert Date from Server's timezone to Admin's timezone
                    if (installationdate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newinstallationdate = df.format(installationdate);
                            String updateADEDate = "UPDATE assetdetail SET installationdate='" + newinstallationdate + "' WHERE id='" + assetdid + "'";
                            pstinner = conn.prepareStatement(updateADEDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated assetdetail installationdate Date : " + newinstallationdate + "\n");
                                //out.println("Updated assetdetail installationdate Date : " + newinstallationdate + "<br>");
                            } else {
                                //System.out.println("assetdetail installationdate Date is not updated \n");
                                //out.println("assetdetail installationdate Date is not updated.<br>");
                            }
                    }
                    if (startdate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newstartdate = df.format(startdate);
                            String updateAMDate = "UPDATE assetmaintenanceschedulerobject SET startdate='" + newstartdate + "' WHERE id='" + assetmid + "'";
                            pstinner = conn.prepareStatement(updateAMDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated assetmaintenanceschedulerobject startdate Date : " + newstartdate + "\n");
                                //out.println("Updated assetmaintenanceschedulerobject startdate Date : " + newstartdate + "<br>");
                            } else {
                                //System.out.println("assetmaintenanceschedulerobject startdate Date is not updated \n");
                                //out.println("assetmaintenanceschedulerobject startdate Date is not updated.<br>");
                            }
                    }
                    if (enddate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newenddate = df.format(enddate);
                            String updateAMDate = "UPDATE assetmaintenanceschedulerobject SET enddate='" + newenddate + "' WHERE id='" + assetmid + "'";
                            pstinner = conn.prepareStatement(updateAMDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated assetmaintenanceschedulerobject enddate Date : " + newenddate + "\n");
                                //out.println("Updated assetmaintenanceschedulerobject enddate Date : " + newenddate + "<br>");
                            } else {
                                //System.out.println("assetmaintenanceschedulerobject enddate Date is not updated \n");
                                //out.println("assetmaintenanceschedulerobject enddate Date is not updated.<br>");
                            }
                    }
                    
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp
                            
                System.out.println("Asset Maintenance Schedule Module Ended...\n");
//                out.println("<b>Asset Maintenance Schedule Module Ended...<b><br>");
//======================================================= ASSET MAINTENANCE SCHEDULE MODULE - END =============================================================
                
                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
                
//======================================================= INVENTORY LOCATION MASTER MODULE WITH COVERT_TZ- START ===========================================================
                System.out.println("Inventory Location Master Module Started...\n");
//                out.println("<b>Inventory Location Master Module Started...</b><br>");
                prevCreatedBy = ""; pst2 = null; rst2 = null;
                applyDateQuery = "SELECT id, createdby, createdon, modifiedon FROM in_location "
                                +"WHERE in_location.company ='" + companyid + "' ORDER BY createdon";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null; tzrst = null;
                while (rst2.next()) {
                    
                    df.setTimeZone(TimeZone.getTimeZone("GMT"));
                    
                    id = rst2.getString("id");
                    Date createdon = rst2.getObject("createdon") != null ? (java.util.Date)rst2.getObject("createdon") : null;
                    Date modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date)rst2.getObject("modifiedon") : null;

                    //Convert Date from Server's timezone to Admin's timezone
                    if (createdon!=null) {
                        pstinner = null;
                        rstinner = null;
                        String newcreatedon = df.format(createdon);
                            String updateLMDate = "UPDATE in_location SET createdon='" + newcreatedon + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateLMDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated in_location createdon Date : " + newcreatedon + "\n");
                                //out.println("Updated in_location createdon Date : " + newcreatedon + "<br>");
                            } else {
                                //System.out.println("in_location createdon Date is not updated \n");
                                //out.println("in_location createdon Date is not updated.<br>");
                            }
                    }
                    if (modifiedon!=null) {
                        pstinner = null;
                        rstinner = null;
                        String newmodifiedon = df.format(modifiedon);
                            String updateLMDate = "UPDATE in_location SET modifiedon='" + newmodifiedon + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateLMDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated in_location modifiedon Date : " + newmodifiedon + "\n");
                                //out.println("Updated in_location modifiedon Date : " + newmodifiedon + "<br>");
                            } else {
                                //System.out.println("in_location modifiedon Date is not updated \n");
                                //out.println("in_location modifiedon Date is not updated.<br>");
                            }
                    }
                    
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp
                            
                System.out.println("Inventory Location Master Module Ended...\n");
//                out.println("<b>Inventory Location Master Module Ended...<b><br>");
//======================================================= INVENTORY LOCATION MASTER MODULE WITH COVERT_TZ- END =================================================
                
                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
                
//======================================================= INVENTORY STORE MASTER MODULE WITH COVERT_TZ- START ===============================================================
                System.out.println("Inventory Store Master Module Started...\n");
//                out.println("<b>Inventory Store Master Module Started...</b><br>");
                prevCreatedBy = ""; pst2 = null; rst2 = null;
                applyDateQuery = "SELECT id, createdby, createdon, modifiedon FROM in_storemaster "
                                +"WHERE in_storemaster.company ='" + companyid + "' ORDER BY createdon";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null; tzrst = null;
                while (rst2.next()) {
                    
                    df.setTimeZone(TimeZone.getTimeZone("GMT"));
                    
                    id = rst2.getString("id");
                    Date createdon = rst2.getObject("createdon") != null ? (java.util.Date)rst2.getObject("createdon") : null;
                    Date modifiedon = rst2.getObject("modifiedon") != null ? (java.util.Date)rst2.getObject("modifiedon") : null;

                    //Convert Date from Server's timezone to Admin's timezone
                    if (createdon!=null) {
                        pstinner = null;
                        rstinner = null;
                        String newcreatedon = df.format(createdon);
                            String updateSMDate = "UPDATE in_storemaster SET createdon='" + newcreatedon + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateSMDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated in_storemaster createdon Date : " + newcreatedon + "\n");
                                //out.println("Updated in_storemaster createdon Date : " + newcreatedon + "<br>");
                            } else {
                                //System.out.println("in_storemaster createdon Date is not updated \n");
                                //out.println("in_storemaster createdon Date is not updated.<br>");
                            }
                    }
                    if (modifiedon!=null) {
                        pstinner = null;
                        rstinner = null;
                        String newmodifiedon = df.format(modifiedon);
                            String updateSMDate = "UPDATE in_storemaster SET modifiedon='" + newmodifiedon + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateSMDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated in_storemaster modifiedon Date : " + newmodifiedon + "\n");
                                //out.println("Updated in_storemaster modifiedon Date : " + newmodifiedon + "<br>");
                            } else {
                                //System.out.println("in_storemaster modifiedon Date is not updated \n");
                                //out.println("in_storemaster modifiedon Date is not updated.<br>");
                            }
                    }
                    
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp
                            
                System.out.println("Inventory Store Master Module Ended...\n");
//                out.println("<b>Inventory Store Master Module Ended...<b><br>");
//======================================================= INVENTORY STORE MASTER MODULE WITH COVERT_TZ - END =====================================
                
                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
                
//======================================================= INVOICE TERMS MODULE - START ===========================================================
                System.out.println("Invoice Terms Module Started...\n");
//                out.println("<b>Invoice Terms Module Started...</b><br>");
                prevCreatedBy = ""; pst2 = null; rst2 = null;
                applyDateQuery = "SELECT id,applydate FROM taxlist "
                                +"where company='" + companyid + "' ORDER BY applydate";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null; tzrst = null;
                while (rst2.next()) {
                    createdby = compcreator;
                    if(!prevCreatedBy.equalsIgnoreCase(createdby)){
                        prevCreatedBy = createdby;
                        userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='"+createdby+"'";
                        tzpst = conn.prepareStatement(userTZQuery);
                        tzrst = tzpst.executeQuery();
                        while(tzrst.next()){ 
                            userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference"))?tzrst.getString("difference"):defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                        }
                    }//if
                    id = rst2.getString("id");
                    Date applydate = rst2.getObject("applydate") != null ? (java.util.Date)rst2.getObject("applydate") : null;

                    //Convert Date from Server's timezone to Admin's timezone
                    if (applydate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newapplydate = df.format(applydate);
                            String updateITDate = "UPDATE taxlist SET applydate='" + newapplydate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateITDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated taxlist applydate Date : " + newapplydate + "\n");
                               // out.println("Updated taxlist applydate Date : " + newapplydate + "<br>");
                            } else {
                                //System.out.println("taxlist applydate is not updated \n");
                                //out.println("taxlist applydate is not updated.<br>");
                            }
                    }
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp
                            
                System.out.println("Invoice Terms Module Ended...\n");
//                out.println("<b>Invoice Terms Module Ended...<b><br>");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
//======================================================= INVOICE TERMS MODULE - END ==============================================================                
             
                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
                
//======================================================= PACKING DELIVER ORDER MODULE - START ====================================================
                System.out.println("Packing Delivery Order Module Started...\n");
//                out.println("<b>Packing Delivery Order Module Started...</b><br>");
                prevCreatedBy = ""; pst2 = null; rst2 = null;
                applyDateQuery = "SELECT id,packingdate,dateoflc FROM packingdolist "
                                +"where company='" + companyid + "' ORDER BY packingdate";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null; tzrst = null;
                while (rst2.next()) {
                    createdby = compcreator;
                    if(!prevCreatedBy.equalsIgnoreCase(createdby)){
                        prevCreatedBy = createdby;
                        userTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='"+createdby+"'";
                        tzpst = conn.prepareStatement(userTZQuery);
                        tzrst = tzpst.executeQuery();
                        while(tzrst.next()){ 
                            userTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference"))?tzrst.getString("difference"):defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + userTZDiff));
                        }
                    }//if
                    id = rst2.getString("id");
                    Date packingdate = rst2.getObject("packingdate") != null ? (java.util.Date)rst2.getObject("packingdate") : null;
                    Date dateoflc = rst2.getObject("dateoflc") != null ? (java.util.Date)rst2.getObject("dateoflc") : null;

                    //Convert Date from Server's timezone to Admin's timezone
                    if (packingdate!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newpackingdate = df.format(packingdate);
                            String updatePDDate = "UPDATE packingdolist SET packingdate='" + newpackingdate + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updatePDDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                System.out.println("Updated packingdolist packingdate Date : " + newpackingdate + "\n");
//                                out.println("Updated packingdolist packingdate Date : " + newpackingdate + "<br>");
                            } else {
                                System.out.println("packingdolist packingdate is not updated \n");
//                                out.println("packingdolist packingdate is not updated.<br>");
                            }
                    }
                    if (dateoflc!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newdateoflc = df.format(dateoflc);
                            String updatePDDate = "UPDATE packingdolist SET dateoflc='" + newdateoflc + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updatePDDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated packingdolist dateoflc Date : " + newdateoflc + "\n");
                                //out.println("Updated packingdolist dateoflc Date : " + newdateoflc + "<br>");
                            } else {
                                //System.out.println("packingdolist dateoflc is not updated \n");
                                //out.println("packingdolist dateoflc is not updated.<br>");
                            }
                    }
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp
                            
                System.out.println("Packing Delivery Order Module Ended...\n");
//                out.println("<b>Packing Delivery Order Module Ended...<b><br>");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
//======================================================= PACKING DELIVER ORDER MODULE - END ======================================================   
               
                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
                
//======================================================= AUDIT TRAIL MODULE - START ========================================================
                System.out.println("Audit Trail Module Started...\n");
//                out.println("<b>Audit Trail Module Started...</b><br>");
                prevCreatedBy = ""; pst2 = null; rst2 = null;
                applyDateQuery = "SELECT id,audittime FROM audit_trail "
                                +"LEFT JOIN users ON users.userid=audit_trail.user "
                                +"LEFT JOIN company ON company.companyid=users.company "
                                +"WHERE company.companyid='" + companyid + "' ORDER BY audittime";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null; tzrst = null;
                while (rst2.next()) {
                    df.setTimeZone(TimeZone.getTimeZone("GMT"));
                    
                    id = rst2.getString("id");
                    Date audittime = rst2.getObject("audittime") != null ? (java.util.Date)rst2.getObject("audittime") : null;

                    //Convert Date from Server's timezone to Admin's timezone
                    if (audittime!=null) {
                        pstinner = null;
                        rstinner = null;
                            String newaudittime = df.format(audittime);
                            String updateDPDate = "UPDATE audit_trail SET audittime='" + newaudittime + "' WHERE id='" + id + "'";
                            pstinner = conn.prepareStatement(updateDPDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Updated audit_trail audittime Date : " + newaudittime + "\n");
                                //out.println("Updated audit_trail audittime Date : " + newaudittime + "<br>");
                            } else {
                                //System.out.println("audit_trail audittime is not updated \n");
                                //out.println("audit_trail audittime is not updated.<br>");
                            }
                    }
                    
                }
                        //Modify data type of applydate & todate column as Date instead of timestamp
                            
                System.out.println("Audit Trail Module Ended...\n");
//                out.println("<b>Audit Trail Module Ended...<b><br>");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
//======================================================= AUDIT TRAIL MODULE - END ======================================================                
//                out.println("<center><b>=================================================================================</b></center><br><br>");
            }//companyid
        //}//Server query while                                                 
    } catch (Exception ex) {
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
        out.print("Dates are not updated.");
    } finally {
        if (conn != null) {
            try {
                conn.close();
                out.println("Connection Closed....<br>");
                //Execution Ended :
                out.println("<br><br>Execution Ended @ "+new java.util.Date()+"<br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally
%>