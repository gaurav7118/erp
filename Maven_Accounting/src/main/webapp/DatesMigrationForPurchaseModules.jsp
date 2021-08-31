
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
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
        //SCRIPT URL : http://<app-url>/DatesMigrationForPurchesModules.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
        // 'subdomain' is a optional field

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        ServletContext sc = getServletContext();
        String uri = request.getRequestURL().toString();
        String driver = "com.mysql.jdbc.Driver";
        String userTZDiff = "", defaultTZ = "+08:00", query1 = "", companyid = "", compCreator = "";
        String prevCreatedBy = "", compName = "", ctzone="", utzone="";
        int totalCount=0, updateCount=0, companycount=0; 
        
        PreparedStatement pst1 = null, pst2 = null, tzpst = null, modifypst=null, cppst=null;
        ResultSet rst1 = null, rst2 = null, tzrst = null, rstinner, cprst=null;
        PreparedStatement pstinner,pstinner2;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE c.subdomain='" + subdomain + "'";
        }

        Class.forName(driver).newInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        DateFormat utcdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        utcdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        //Execution Started :
        out.println("<br><br>Execution Started @ "+new java.util.Date()+"<br><br>");
        
        conn = DriverManager.getConnection(connectString, username, password);
   
                
        query1 = "SELECT c.companyid, c.creator, c.subdomain, c.timezone AS ctzone, u.timezone AS utzone FROM company c INNER JOIN users u "
                + "ON c.creator=u.userid " + subdomainQuery;
            pst1 = conn.prepareStatement(query1);
            rst1 = pst1.executeQuery();
            while (rst1.next()) {
                companycount++;
                companyid = rst1.getString("companyid");
                compName = rst1.getString("subdomain");
                compCreator = rst1.getObject("creator") != null ? rst1.getString("creator") : "";
                if (StringUtil.isNullOrEmpty(compCreator)) {
                    compCreator = "";
                    continue;
                }
                ctzone = rst1.getObject("ctzone") != null ? rst1.getString("ctzone") : "268";   //Default Singapore
                utzone = rst1.getObject("utzone") != null ? rst1.getString("utzone") : ctzone;   
                cppst=null; cprst=null;
                System.out.println(companycount +" : "+compName);
                out.println("<center><b>"+companycount+" : "+compName+"</b></center><br><br>");
                                
                // To Fetch company creator's timezone.
                String defaultTimezone = "SELECT difference from timezone WHERE timezoneid='"+utzone+"'";
                cppst = conn.prepareStatement(defaultTimezone);
                cprst = cppst.executeQuery();
                if (cprst.next()) {
                    defaultTZ = cprst.getString("difference")!=null?cprst.getString("difference"): "+08:00";
                }
//======================================================= VENDOR QUOTATION MODULE - START =========================================================
                System.out.println("Vendor Quotation Module Started...\n");
//                out.println("<b>Vendor Quotation Module Started...</b>");
                prevCreatedBy = "";
                String vqapplyDateQuery = "select DISTINCT (v.id), v.quotationnumber, v.createdby, v.quotationdate, v.duedate, v.shipdate, v.validdate, v.isfixedassetvq, vd.purchaserequisitiondetailsid FROM vendorquotation v  LEFT JOIN vendorquotationdetails vd ON vd.vendorquotation=v.id where v.company='" + companyid + "' ORDER BY v.quotationdate";
                pst2 = conn.prepareStatement(vqapplyDateQuery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    totalCount++;
                    String vqcreatedby = !StringUtil.isNullOrEmpty(rst2.getString("createdby")) ? rst2.getString("createdby") : compCreator;
                    if (!prevCreatedBy.equalsIgnoreCase(vqcreatedby)) {
                        prevCreatedBy = vqcreatedby;
                        String vquserTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + vqcreatedby + "'";
                        tzpst = conn.prepareStatement(vquserTZQuery);
                        tzrst = tzpst.executeQuery();
                        while (tzrst.next()) {
                            String vquserTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + vquserTZDiff));
                        }
                    }//if
                    String vqid = rst2.getString("id");
                    String vqNo = rst2.getObject("quotationnumber") != null ? rst2.getString("quotationnumber") : "Unknown Number";
                    Date vqcreationdate = rst2.getObject("quotationdate") != null ? (java.util.Date) rst2.getObject("quotationdate") : null;
                    Date vqshipdate = rst2.getObject("shipdate") != null ? (java.util.Date) rst2.getObject("shipdate") : null;
                    Date vqduedate = rst2.getObject("duedate") != null ? (java.util.Date) rst2.getObject("duedate") : null;
                    Date vqvaliddate = rst2.getObject("validdate") != null ? (java.util.Date) rst2.getObject("validdate") : null;
                    int vqisFixedAsset = rst2.getObject("isfixedassetvq") != null ? rst2.getInt("isfixedassetvq") : 0;
                    String vqassetDetailid = rst2.getObject("purchaserequisitiondetailsid") != null ? rst2.getString("purchaserequisitiondetailsid") : null;
                    
                    String newvqcreationDate=null, newvqShipDate=null, newvqDueDate=null, newvqValidDate=null, vqsubquery="";
                    pstinner = null;  rstinner = null;
                    
                    if (vqcreationdate != null) {
                        newvqcreationDate = df.format(vqcreationdate);
                        vqsubquery += " quotationdate='" + newvqcreationDate +"' ";
                    }
                    if (vqshipdate != null) {
                        if (!StringUtil.isNullOrEmpty(vqsubquery)) {
                            vqsubquery += ", ";
                        }
                        newvqShipDate = df.format(vqshipdate);
                        vqsubquery += " shipdate='" + newvqShipDate + "' ";
                    }
                    if (vqduedate != null) {
                        if (!StringUtil.isNullOrEmpty(vqsubquery)) {
                            vqsubquery += ", ";
                        }
                        newvqDueDate = df.format(vqduedate);
                        vqsubquery += " duedate='" + newvqDueDate + "' ";
                    }
                    if (vqvaliddate != null) {
                        if (!StringUtil.isNullOrEmpty(vqsubquery)) {
                            vqsubquery += ", ";
                        }
                        newvqValidDate = df.format(vqvaliddate);
                       vqsubquery += " validdate='" + newvqValidDate + "' ";
                    }
                    if (!StringUtil.isNullOrEmpty(vqsubquery)) {
                    pstinner = null;
                    String vqupdate = "UPDATE vendorquotation SET "+vqsubquery+" WHERE id='" + vqid + "'";
                    pstinner = conn.prepareStatement(vqupdate);
                    int updatecount1 = pstinner.executeUpdate();
                    if (updatecount1 > 0) {
                        updateCount++;
                            //Nothing to do.
                        } else {
                            //System.out.println("Vendor Quotation No :"+vqNo+" is not updated \n");
                            //out.println("Vendor Quotation No :"+vqNo+" is not updated.<br>");
                        }
                  }
                    
                  if(vqisFixedAsset==1 && vqassetDetailid!=null){
                        //Code to update installation date of Fixed Asset Vendor Quotation.
                        String vqAssetQuery = "SELECT pr.id, pr.installationdate FROM assetdetailspurchaserequisitiondetailmapping pm LEFT JOIN purchaserequisitionassetdetail pr ON pm.purchaserequisitionassetdetails=pr.id WHERE pm.purchaserequisitiondetailid='" + vqassetDetailid + "' AND pm.company='" + companyid + "'";
                        pstinner = null;
                        rstinner = null;
                        pstinner = conn.prepareStatement(vqAssetQuery);
                        rstinner = pstinner.executeQuery();
                        while (rstinner.next()) {
                            String vqassetid = rstinner.getString("id") != null ? rstinner.getString("id") : null;
                            Date vqinstallDate = rstinner.getObject("installationdate") != null ? (java.util.Date) rstinner.getObject("installationdate") : null;
                            if (vqassetid != null && vqinstallDate != null) {
                                pstinner2 = null;
                                String newVQInstallDate = df.format(vqinstallDate);
                                String updateAssetVQDate = "UPDATE purchaserequisitionassetdetail SET installationdate='" + newVQInstallDate + "' WHERE id='" + vqassetid + "'";
                                pstinner2 = conn.prepareStatement(updateAssetVQDate);
                                int updatecount2 = pstinner2.executeUpdate();
                                if (updatecount2 > 0) {
                                    //Nothing to do.
                                } else {
                                    //System.out.println("Asset Vendor Quotation :"+vqNo+" is not updated \n");
                                    //out.println("Asset Vendor Quotation :"+vqNo+" is not updated.<br>");
                                }
                            }//aseet date
                        }//asset loop
                  }
                }
//                out.println("Total No. of Records :"+totalCount+" <br>");
//                out.println("Updated No. of Records :"+updateCount+" <br>");
                System.out.println("Vendor Quotation Module Ended...\n");
//                out.println("<b>Vendor Quotation Module Ended...<b><br>");

//======================================================= VENDOR QUOTATION MODULE - END =========================================================


                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");

//======================================================= PURCHASE ORDER MODULE - START =========================================================

                System.out.println("Purchase Order Module Started...\n");
//                out.println("<b>Purchase Order Module Started...<b><br>");
                prevCreatedBy = "";
                pst2 = null;
                totalCount=0; updateCount=0; 
                rst2 = null;
                String poApplyDateQuery = "SELECT DISTINCT(po.id), po.ponumber, po.createdby, po.orderdate, po.duedate, po.shipdate, po.isfixedassetpo, pod.id AS podetailID FROM purchaseorder po LEFT JOIN podetails pod ON po.id=pod.purchaseorder where po.company='" + companyid + "' ORDER BY po.orderdate";
                pst2 = conn.prepareStatement(poApplyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null;
                tzrst = null;
                while (rst2.next()) {
                    totalCount++;
                    String pocreatedby = !StringUtil.isNullOrEmpty(rst2.getString("createdby")) ? rst2.getString("createdby") : compCreator;
                    if (!prevCreatedBy.equalsIgnoreCase(pocreatedby)) {
                        prevCreatedBy = pocreatedby;
                        String pouserTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + pocreatedby + "'";
                        tzpst = conn.prepareStatement(pouserTZQuery);
                        tzrst = tzpst.executeQuery();
                        while (tzrst.next()) {
                            String pouserTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + pouserTZDiff));
                        }
                    }//if
                    String poid = rst2.getString("id");
                    String ponumber = rst2.getObject("ponumber") != null ? rst2.getString("ponumber") : "Unknown Number";
                    String podetailID = rst2.getObject("podetailID") != null ? rst2.getString("podetailID") : null;
                    Date podate = rst2.getObject("orderdate") != null ? (java.util.Date) rst2.getObject("orderdate") : null;
                    Date poshipdate = rst2.getObject("shipdate") != null ? (java.util.Date) rst2.getObject("shipdate") : null;
                    Date poduedate = rst2.getObject("duedate") != null ? (java.util.Date) rst2.getObject("duedate") : null;
                    int poisfixedassetpo = rst2.getObject("isfixedassetpo") != null ? rst2.getInt("isfixedassetpo") : null;
                    String newPODate=null, newPOShipDate=null, newPODueDate=null, posubquery="";
                    pstinner = null;
                                       
                    if (podate != null) {
                        newPODate = df.format(podate);
                        posubquery += " orderdate='" + newPODate + "' ";
                    }
                    if (poshipdate != null) {
                        if (!StringUtil.isNullOrEmpty(posubquery)) {
                            posubquery += ", ";
                        }
                        newPOShipDate = df.format(poshipdate);
                        posubquery += " shipdate='" + newPOShipDate + "' ";
                    }
                    if (poduedate != null) {
                        if (!StringUtil.isNullOrEmpty(posubquery)) {
                            posubquery += ", ";
                        }
                        newPODueDate = df.format(poduedate);
                        posubquery += " duedate='" + newPODueDate + "' ";
                  }
                    
                  if (!StringUtil.isNullOrEmpty(posubquery)) {
                  pstinner = null;
                  String updatePODates = "UPDATE purchaseorder SET "+ posubquery + " WHERE id='" + poid + "'";  
                  pstinner = conn.prepareStatement(updatePODates);
                  int updatecount1 = pstinner.executeUpdate();
                  if (updatecount1 > 0) {
                      updateCount++;
                            //Nothing to do.
                        } else {
                            //System.out.println("PO :"+ponumber+" is not updated \n");
                            //out.println("PO :"+ponumber+" is not updated .<br>");
                        }
                  }
                    
                  if(poisfixedassetpo==1 && podetailID!=null){
                        String assetPOQuery = "SELECT pr.id, pr.installationdate FROM assetdetailspurchaserequisitiondetailmapping pm LEFT JOIN purchaserequisitionassetdetail pr ON pm.purchaserequisitionassetdetails=pr.id WHERE pm.purchaserequisitiondetailid='" + podetailID + "' AND pm.company='" + companyid + "'";
                        pstinner = null;
                        rstinner = null;
                        pstinner = conn.prepareStatement(assetPOQuery);
                        rstinner = pstinner.executeQuery();
                        while (rstinner.next()) {
                            String poassetid = rstinner.getString("id") != null ? rstinner.getString("id") : null;
                            Date poinstallDate = rstinner.getObject("installationdate") != null ? (java.util.Date) rstinner.getObject("installationdate") : null;
                            if (poassetid != null && poinstallDate != null) {
                                pstinner2 = null;
                                String newPOInstallDate = df.format(poinstallDate);
                                String updateAssetPODate = "UPDATE purchaserequisitionassetdetail SET installationdate='" + newPOInstallDate + "' WHERE id='" + poassetid + "'";
                                pstinner2 = conn.prepareStatement(updateAssetPODate);
                                int updatecount = pstinner2.executeUpdate();
                                if (updatecount > 0) {
                                    //Nothing to do.
                                } else {
                                    //System.out.println("Asset Purchase Order :"+ponumber+" is not updated. \n");
                                    //out.println("Asset Purchase Order :"+ponumber+" is not updated.<br>");
                                }
                            }//aseet date
                        }//asset loop
                  }
                }
//                out.println("Total No. of Records :"+totalCount+" <br>");
//                out.println("Updated No. of Records :"+updateCount+" <br>");
                System.out.println("Purchase Order Module Ended...\n");
//                out.println("<b>Purchase Order Module Ended...<b><br>");

//======================================================= PURCHASE ORDER MODULE - END =========================================================

                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");

//======================================================= PURCHASE INVOICE / CASH PURCHASE MODULE - START =========================================================

                System.out.println("PPURCHASE INVOICE / CASH PURCHASE Module Started...\n");
//                out.println("<b>PURCHASE INVOICE / CASH PURCHASE Module Started...<b><br>");
                prevCreatedBy = "";
                totalCount=0; updateCount=0; 
                pst2 = null;
                rst2 = null;
                String piApplyDateQuery = "SELECT DISTINCT(gr.id), gr.grnumber, je.id AS jeid, grd.id AS grdetailID, "
                        + "chk.id AS chequeid, crd.id AS cardid, je.entrydate, gr.createdby, gr.creationdate, gr.duedate, "
                        + "gr.shipdate, gr.debtclaimeddate, gr.partyinvoicedate, gr.isfixedassetinvoice, chk.duedate AS chkduedate, "
                        + "inv.updatedate AS invdate, inv.isupdated, crd.expirydate FROM goodsreceipt gr LEFT JOIN journalentry je "
                        + "ON gr.journalentry=je.id LEFT JOIN grdetails grd ON gr.id=grd.goodsreceipt LEFT JOIN paydetail pyd "
                        + "ON gr.paydetail = pyd.id LEFT JOIN cheque chk ON pyd.cheque=chk.id LEFT JOIN card crd "
                        + "ON pyd.card = crd.id LEFT JOIN inventory inv ON grd.id=inv.id "
                        + "WHERE gr.company='" + companyid + "' ORDER BY gr.creationdate";
                pst2 = conn.prepareStatement(piApplyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null;
                tzrst = null;
                while (rst2.next()) {
                    int isupdated = 1;
                    totalCount++;
                    String picreatedby = !StringUtil.isNullOrEmpty(rst2.getString("createdby")) ? rst2.getString("createdby") : compCreator;
                    if (!prevCreatedBy.equalsIgnoreCase(picreatedby)) {
                        prevCreatedBy = picreatedby;
                        String piuserTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + picreatedby + "'";
                        tzpst = conn.prepareStatement(piuserTZQuery);
                        tzrst = tzpst.executeQuery();
                        while (tzrst.next()) {
                            String piuserTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + piuserTZDiff));
                        }
                    }//if

                    String piid = rst2.getString("id");
                    String grnumber = rst2.getObject("grnumber") != null ? rst2.getString("grnumber") : "Unknown Number";
                    //String pijeid = rst2.getObject("jeid") != null ? rst2.getString("jeid") : null;
                    String grdetailID = rst2.getObject("grdetailID") != null ? rst2.getString("grdetailID") : null;
                    String chequeID = rst2.getObject("chequeid") != null ? rst2.getString("chequeid") : null;
                    String cardID = rst2.getObject("cardid") != null ? rst2.getString("cardid") : null;
                    Date picreationdate = rst2.getObject("creationdate") != null ? (java.util.Date) rst2.getObject("creationdate") : null;
                    Date pishipdate = rst2.getObject("shipdate") != null ? (java.util.Date) rst2.getObject("shipdate") : null;
                    Date piduedate = rst2.getObject("duedate") != null ? (java.util.Date) rst2.getObject("duedate") : null;
                    Date pipartyinvoicedate = rst2.getObject("partyinvoicedate") != null ? (java.util.Date) rst2.getObject("partyinvoicedate") : null;
                    Date pidebtclaimeddate = rst2.getObject("debtclaimeddate") != null ? (java.util.Date) rst2.getObject("debtclaimeddate") : null;
                    //Date pijedate = rst2.getObject("entrydate") != null ? (java.util.Date) rst2.getObject("entrydate") : null;
                    Date piinvdate = rst2.getObject("invdate") != null ? (java.util.Date) rst2.getObject("invdate") : null;
                    Date piChkDate = rst2.getObject("chkduedate") != null ? (java.util.Date) rst2.getObject("chkduedate") : null;
                    Date expirydate = rst2.getObject("expirydate") != null ? (java.util.Date) rst2.getObject("expirydate") : null;
                    
                    int assetPI = rst2.getObject("isfixedassetinvoice") != null ? rst2.getInt("isfixedassetinvoice") : null;
                    isupdated = rst2.getInt("isupdated");
                    String newPIcreationDate=null, newPIShipDate=null, newPIDueDate=null, newPIDebtDate=null, newPIPartyDate=null, pisubquery=""; 
                    pstinner = null;  rstinner = null;
                    
                    if (picreationdate != null) {
                        newPIcreationDate = df.format(picreationdate);
                        pisubquery += " creationdate='" + newPIcreationDate + "' ";
                    }
                    if (pishipdate != null) {
                        if (!StringUtil.isNullOrEmpty(pisubquery)) {
                            pisubquery += ", ";
                        }
                        newPIShipDate = df.format(pishipdate);
                        pisubquery += " shipdate='" + newPIShipDate + "' ";
                    }
                    if (piduedate != null) {
                        if (!StringUtil.isNullOrEmpty(pisubquery)) {
                            pisubquery += ", ";
                        }
                        newPIDueDate = df.format(piduedate);
                        pisubquery += " duedate='" + newPIDueDate + "' ";
                    }
                    if (pidebtclaimeddate != null) {
                        if (!StringUtil.isNullOrEmpty(pisubquery)) {
                            pisubquery += ", ";
                        }
                        newPIDebtDate = df.format(pidebtclaimeddate);
                        pisubquery += " debtclaimeddate='" + newPIDebtDate + "' ";
                    }
                    if (pipartyinvoicedate != null) {
                        if (!StringUtil.isNullOrEmpty(pisubquery)) {
                            pisubquery += ", ";
                        }
                        newPIPartyDate = df.format(pipartyinvoicedate);
                        pisubquery += " partyinvoicedate='" + newPIPartyDate + "' ";
                    }
                    if (!StringUtil.isNullOrEmpty(pisubquery)) {
                    pstinner = null;
                    String updatePIDate = "UPDATE goodsreceipt SET " + pisubquery + " WHERE id='" + piid + "'";
                    pstinner = conn.prepareStatement(updatePIDate);
                        int updatecount1 = pstinner.executeUpdate();
                        if (updatecount1 > 0) {
                            updateCount++;
                            //Nothing to do.
                        } else {
                            //System.out.println("Purchase Invoie :"+grnumber+" is not updated. \n");
                            //out.println("Purchase Invoie :"+grnumber+" is not updated.<br>");
                        }
                   }
                   if(chequeID != null && piChkDate != null){
                    pstinner = null;
                    String updateChequeDate = "UPDATE cheque SET duedate ='" + df.format(piChkDate) + "' WHERE id='" + chequeID + "'";
                    pstinner = conn.prepareStatement(updateChequeDate);
                        int updatecount1 = pstinner.executeUpdate();
                        if (updatecount1 > 0) {
                            updateCount++;
                            //Nothing to do.
                        } else {
                            //System.out.println("Purchase Invoie :"+grnumber+" - Cheque Duedate is not updated. \n");
                            //out.println("Purchase Invoie :"+grnumber+" - Cheque Duedate is not updated.<br>");
                        }
                   }//Updtae Cheque date
                   if(cardID !=null && expirydate != null){
                    pstinner = null;
                    String updateCardDate = "UPDATE card SET expirydate ='" + df.format(expirydate) + "' WHERE id='" + cardID + "'";
                    pstinner = conn.prepareStatement(updateCardDate);
                        int updatecount1 = pstinner.executeUpdate();
                        if (updatecount1 > 0) {
                            updateCount++;
                            //Nothing to do.
                        } else {
                            //System.out.println("Purchase Invoie :"+grnumber+" - Card Expiry Date is not updated. \n");
                            //out.println("Purchase Invoie :"+grnumber+" - Card Expiry Date is not updated.<br>");
                        }
                   }//Updtae Cheque date
                    
                   if(assetPI==1 && grdetailID!=null){
                        String assetPIQuery = "SELECT DISTINCT(asd.id), asd.installationdate FROM assetdetailsinvdetailmapping adm LEFT JOIN assetdetail asd ON adm.assetdetails=asd.id WHERE adm.invoicedetailid='" + grdetailID + "' AND adm.company='" + companyid + "'";
                        pstinner = null;
                        rstinner = null;
                        pstinner = conn.prepareStatement(assetPIQuery);
                        rstinner = pstinner.executeQuery();
                        while (rstinner.next()) {
                            String piassetid = rstinner.getString("id") != null ? rstinner.getString("id") : null;
                            Date piinstallDate = rstinner.getObject("installationdate") != null ? (java.util.Date) rstinner.getObject("installationdate") : null;
                            if (piassetid != null && piinstallDate != null) {
                                pstinner2 = null;
                                String newPIInstallDate = df.format(piinstallDate);
                                String updateAssetPIDate = "UPDATE assetdetail SET installationdate='" + newPIInstallDate + "' WHERE id='" + piassetid + "'";
                                pstinner2 = conn.prepareStatement(updateAssetPIDate);
                                int updatecount = pstinner2.executeUpdate();
                                if (updatecount > 0) {
                                    //Nothing to do.
                                } else {
                                    //System.out.println("Asset Purchase Invoice :"+grnumber+" is not updated. \n");
                                    //out.println("Asset Purchase Invoice :"+grnumber+" is not updated.<br>");
                                }
                            }//aseet date
                        }//asset loop
                   }                    
/*                    if (jedate != null) {
                        pstinner = null;
                        rstinner = null;
                        String jeentrydate = df.format(jedate);
                        String updatePIDate = "UPDATE journalentry SET entrydate='" + jeentrydate + "' WHERE id='" + jeid + "'";
                        pstinner = conn.prepareStatement(updatePIDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            updateCount++;
                            //Nothing to do
                        } else {
                            System.out.println("Purchase Invoie :"+grnumber+" - JE is not updated.\n");
                            out.println("Purchase Invoie :"+grnumber+" - JE is not updated.<br>");
                        }
                    }           */
                    if (piinvdate != null && isupdated==0) {
                        pstinner = null;
                        rstinner = null;
                        String newPIInvDate = df.format(piinvdate);
                        String updatePIInvDate = "UPDATE inventory SET updatedate='" + newPIInvDate + "', isupdated=1 WHERE id='" + grdetailID + "'";
                        pstinner = conn.prepareStatement(updatePIInvDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            //Nothing to do.
                        } else {
                            //System.out.println("Purchase Invoie :"+grnumber+" - Inventory is not updated.\n");
                            //out.println("Purchase Invoie :"+grnumber+" - Inventory is not updated.<br>");
                        }
                    }
                }
//                out.println("Total No. of Records :"+totalCount+" <br>");
//                out.println("Updated No. of Records :"+updateCount+" <br>");
                System.out.println("PPURCHASE INVOICE / CASH PURCHASE Module Ended...\n");
//                out.println("<b>PURCHASE INVOICE / CASH PURCHASE Module Ended...<b><br>");

//======================================================= PURCHASE INVOICE / CASH PURCHASE MODULE - END =========================================================

                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");

//======================================================= MAKE PAYMENT MODULE - START =========================================================

                System.out.println("Make Payment Module Started...\n");
//                out.println("<b>Make Payment Module Started...</b>");
                prevCreatedBy = "";
                totalCount=0; updateCount=0; 
                pst2 = null;
                rst2 = null;
                String mpApplyDateQuery = "SELECT pm.id, pm.paymentnumber, pm.createdby,"
                        + "pm.creationdate, pm.chequedate FROM payment pm "
                        + "WHERE pm.company='" + companyid + "' ORDER BY pm.creationdate";
                pst2 = conn.prepareStatement(mpApplyDateQuery);
                rst2 = pst2.executeQuery();
                tzpst = null;
                tzrst = null;
                while (rst2.next()) {
                    totalCount++;
                    String mpcreatedby = !StringUtil.isNullOrEmpty(rst2.getString("createdby")) ? rst2.getString("createdby") : compCreator;
                    if (!prevCreatedBy.equalsIgnoreCase(mpcreatedby)) {
                        prevCreatedBy = mpcreatedby;
                        String mpuserTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + mpcreatedby + "'";
                        tzpst = conn.prepareStatement(mpuserTZQuery);
                        tzrst = tzpst.executeQuery();
                        while (tzrst.next()) {
                            String mpuserTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + mpuserTZDiff));
                        }
                    }//if
                    String mpid = rst2.getString("id");
                    String paymentnumber = rst2.getObject("paymentnumber")!=null?rst2.getString("paymentnumber"):"Unknown Number";
                    Date mpcreationdate = rst2.getObject("creationdate") != null ? (java.util.Date) rst2.getObject("creationdate") : null;
                    Date mpchequedate = rst2.getObject("chequedate") != null ? (java.util.Date) rst2.getObject("chequedate") : null;
                    
                    String newMPCreationDate=null, newMPChequeDate=null, mpsubquery="";
                    pstinner = null;  rstinner = null;
                    
                    if (mpcreationdate != null) {
                        newMPCreationDate = df.format(mpcreationdate);
                        mpsubquery += " creationdate='" + newMPCreationDate + "' "; 
                    }
                    if (mpchequedate != null) {
                        if (!StringUtil.isNullOrEmpty(mpsubquery)) {
                            mpsubquery += ", ";
                        }
                        newMPChequeDate = df.format(mpchequedate);
                        mpsubquery += " chequedate='" + newMPChequeDate + "' "; 
                    }
                    if (!StringUtil.isNullOrEmpty(mpsubquery)) {
                    String updateMPDates = "UPDATE payment SET " + mpsubquery + " WHERE id='" + mpid + "'";
                    pstinner = conn.prepareStatement(updateMPDates);
                        int updatecount1 = pstinner.executeUpdate();
                        if (updatecount1 > 0) {
                            updateCount++;
                            //Nothing to do
                        } else {
                            //System.out.println("Make Payment :"+paymentnumber+" is not updated. \n");
                            //out.println("Make Payment :"+paymentnumber+" is not updated. <br>");
                        }
                    }                                                 
                }
//                out.println("Total No. of Records :"+totalCount+" <br>");
//                out.println("Updated No. of Records :"+updateCount+" <br>");
                System.out.println("Make Payment Module Ended...\n");
//                out.println("<b>Make Payment Module Ended...<b><br>");

//======================================================= MAKE PAYMENT MODULE - END =========================================================

                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");

//======================================================= GOODS RECEIPT MODULE - START =========================================================
                System.out.println("Goods Receipt Order Module Started...\n");
//                out.println("<b>Goods Receipt Order Module Started...</b>");
                prevCreatedBy = "";
                totalCount=0; updateCount=0; 
                rst2 = null;
                String grApplyDateQuery = "SELECT gr.id, gr.gronumber, grd.id AS grdetailID, gr.createdby, gr.grorderdate, gr.shipdate, gr.isfixedassetgro, inv.updatedate AS invdate, inv.isupdated FROM grorder gr LEFT JOIN  grodetails grd ON gr.id=grd.grorder LEFT JOIN inventory inv ON grd.id=inv.id WHERE gr.company='" + companyid + "' ORDER BY gr.grorderdate";
                pst2 = conn.prepareStatement(grApplyDateQuery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    int isupdated = 1;
                    totalCount++;
                    String grocreatedby = !StringUtil.isNullOrEmpty(rst2.getString("createdby")) ? rst2.getString("createdby") : compCreator;
                    if (!prevCreatedBy.equalsIgnoreCase(grocreatedby)) {
                        prevCreatedBy = grocreatedby;
                        String grouserTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + grocreatedby + "'";
                        tzpst = conn.prepareStatement(grouserTZQuery);
                        tzrst = tzpst.executeQuery();
                        while (tzrst.next()) {
                            String grouserTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + grouserTZDiff));
                        }
                    }//if
                    String groid = rst2.getString("id");
                    String gronumber = rst2.getObject("gronumber") != null ? rst2.getString("gronumber") : "Unknown Number";
                    String grodetailID = rst2.getObject("grdetailID") != null ? rst2.getString("grdetailID") : null;
                    Date grocreationdate = rst2.getObject("grorderdate") != null ? (java.util.Date) rst2.getObject("grorderdate") : null;
                    Date groshipdate = rst2.getObject("shipdate") != null ? (java.util.Date) rst2.getObject("shipdate") : null;
                    Date groinvupdateDate = rst2.getObject("invdate") != null ? (java.util.Date) rst2.getObject("invdate") : null;
                    int groisAssetGRO = rst2.getObject("isfixedassetgro") != null ? rst2.getInt("isfixedassetgro") : null;
                    isupdated = rst2.getInt("isupdated");
                    String newGROCreationDate = null, newGROShipDate=null, grosubquery="";
                    pstinner = null;  rstinner = null;
                    
                    if (grocreationdate != null) {
                        newGROCreationDate = df.format(grocreationdate);
                        grosubquery += " grorderdate='" + newGROCreationDate + "' ";
                    }
                    if (groshipdate != null) {
                        if (!StringUtil.isNullOrEmpty(grosubquery)) {
                            grosubquery += ", ";
                        }
                        newGROShipDate = df.format(groshipdate);
                        grosubquery += " shipdate='" + newGROShipDate + "' ";
                    }
                  if (!StringUtil.isNullOrEmpty(grosubquery)) {
                        pstinner = null;
                        String updateGRODate = "UPDATE grorder SET " + grosubquery + " WHERE id='" + groid + "'";
                        pstinner = conn.prepareStatement(updateGRODate);
                        int updatecount1 = pstinner.executeUpdate();
                        if (updatecount1 > 0) {
                            updateCount++;
                            //Nothing to do.
                        } else {
                            //System.out.println("Goods Receipt :"+gronumber+" is not updated.\n");
                            //out.println("Goods Receipt :"+gronumber+" is not updated.<br>");
                        }
                  }
                  if(groisAssetGRO==1 && grodetailID!=null){
                        //Code to update installation date of Fixed Asset Purchase GRO.
                        String assetGROQuery = "SELECT DISTINCT(asd.id), asd.installationdate FROM assetdetailsinvdetailmapping adm LEFT JOIN assetdetail asd ON adm.assetdetails=asd.id WHERE adm.invoicedetailid='" + grodetailID + "' AND adm.company='" + companyid + "'";
                        pstinner = null;
                        rstinner = null;
                        pstinner = conn.prepareStatement(assetGROQuery);
                        rstinner = pstinner.executeQuery();
                        while (rstinner.next()) {
                            String groassetid = rstinner.getString("id") != null ? rstinner.getString("id") : null;
                            Date groinstallDate = rstinner.getObject("installationdate") != null ? (java.util.Date) rstinner.getObject("installationdate") : null;
                            if (groassetid != null && groinstallDate != null) {
                                pstinner2 = null;
                                String newGROInstallDate = df.format(groinstallDate);
                                String updateAssetGRODate = "UPDATE assetdetail SET installationdate='" + newGROInstallDate + "' WHERE id='" + groassetid + "'";
                                pstinner2 = conn.prepareStatement(updateAssetGRODate);
                                int updatecount = pstinner2.executeUpdate();
                                if (updatecount > 0) {
                                    //Nothing to do
                                } else {
                                    System.out.println("Asset Goods Receipt :"+gronumber+" is not updated.\n");
                                    out.println("Asset Goods Receipt :"+gronumber+" is not updated.<br>");
                                }
                            }//aseet date
                        }//asset loop
                  }
                    if (groinvupdateDate != null && isupdated==0) {
                        pstinner = null;
                        rstinner = null;
                        String newGROInvUpDate = df.format(groinvupdateDate);
                        String updateGROInvDate = "UPDATE inventory SET updatedate='" + newGROInvUpDate + "', isupdated=1 WHERE id='" + grodetailID + "'";
                        pstinner = conn.prepareStatement(updateGROInvDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            //Nothing to do
                        } else {
                            //System.out.println("Asset Goods Receipt :"+gronumber+" - Inventory is not updated.\n");
                            //out.println("Asset Goods Receipt :"+gronumber+" - Inventory is not updated.<br>");
                        }
                    }//inventory updateDate                    
                }//createdBy
//                out.println("Total No. of Records :"+totalCount+" <br>");
//                out.println("Updated No. of Records :"+updateCount+" <br>");
                System.out.println("Goods Receipt Order Module Ended...\n");
//                out.println("<b>Goods Receipt Order Module Ended...<b><br>");

//======================================================= GOODS RECEIPT MODULE - END =========================================================

                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");

//======================================================= DEBIT NOTE MODULE - START =========================================================

                System.out.println("Debit Note Module Started...\n");
//                out.println("<b>Debit Note Module Started...</b>");
                prevCreatedBy = "";
                totalCount=0; updateCount=0; 
                rst2 = null;
                String dnApplyDateQuery = "SELECT dn.id, dn.dnnumber, je.id as jeid, dn.createdby, dn.creationdate, je.entrydate FROM debitnote dn LEFT JOIN journalentry je ON dn.journalentry=je.id where dn.company='" + companyid + "' ORDER BY dn.creationdate";
                pst2 = conn.prepareStatement(dnApplyDateQuery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    totalCount++;
                    String dncreatedby = !StringUtil.isNullOrEmpty(rst2.getString("createdby")) ? rst2.getString("createdby") : compCreator;
                    if (!prevCreatedBy.equalsIgnoreCase(dncreatedby)) {
                        prevCreatedBy = dncreatedby;
                        String dnuserTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + dncreatedby + "'";
                        tzpst = conn.prepareStatement(dnuserTZQuery);
                        tzrst = tzpst.executeQuery();
                        while (tzrst.next()) {
                            String dnuserTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + dnuserTZDiff));
                        }
                    }//if
                    String dnid = rst2.getString("id");
                    String dnnumber = rst2.getObject("dnnumber") != null ? rst2.getString("dnnumber"):"Unknown Number";
                    Date dncreationdate = rst2.getObject("creationdate") != null ? (java.util.Date) rst2.getObject("creationdate") : null;
                    //String dnjeid = rst2.getObject("jeid") != null ? rst2.getString("jeid"):null;
                    //Date jedate = rst2.getObject("entrydate") != null ? (java.util.Date) rst2.getObject("entrydate") : null;

                    if (dncreationdate != null) {
                        pstinner = null;
                        rstinner = null;
                        String newdncreationDate = df.format(dncreationdate);
                        String updateDNDate = "UPDATE debitnote SET creationdate='" + newdncreationDate + "' WHERE id='" + dnid + "'";
                        pstinner = conn.prepareStatement(updateDNDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            updateCount++;
                            //Nothing to do
                        } else {
                            //System.out.println("Debit Note :"+dnnumber+" is not updated.\n");
                            //out.println("Debit Note :"+dnnumber+" is not updated.<br>");
                        }
                    }
/*                    if (jedate != null) {
                        pstinner = null;
                        rstinner = null;
                        String newJEDate = df.format(jedate);
                        String updateShipDate = "UPDATE journalentry SET entrydate='" + newJEDate + "' WHERE id='" + jeid + "'";
                        pstinner = conn.prepareStatement(updateShipDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            updateCount++;
                            //System.out.println("Updated JE Entry Date : " + newJEDate + "\n");
                            //out.println("Updated JE Entry Date : " + newJEDate + "<br>");
                        } else {
                            System.out.println("Debit Note :"+dnnumber+" - JE is not updated.\n");
                            out.println("Debit Note :"+dnnumber+" - JE is not updated.<br>");
                        }
                    }           */
                }//createdBy

//                out.println("Total No. of Records :"+totalCount+" <br>");
//                out.println("Updated No. of Records :"+updateCount+" <br>");
                System.out.println("Debit Note Module Ended...\n");
//                out.println("<b>Debit Note Module Ended...<b><br>");

//======================================================= DEBIT NOTE MODULE - END =========================================================

                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
//======================================================= PURCHASE RETURN MODULE - START =========================================================

                System.out.println("Purchase Return Module Started...\n");
//                out.println("<b>Purchase Return Module Started...</b>");
                prevCreatedBy = "";
                rst2 = null;
                totalCount=0; updateCount=0; 
                String prApplyDateQuery = "SELECT pr.id, pr.prnumber, pr.createdby, pr.orderdate, pr.shipdate, pr.isfixedasset, prd.id AS prdetailID, stock.id AS stockid, cons.id AS consid, consdetail.id AS consdetailid, "
                        +" inv.updatedate AS invdate, stock.transaction_date AS stocktxnDate, stock.createdon AS stockCreateDate, cons.fromdate AS consFromDate, "
                        +" cons.todate AS consTodate, cons.createdon AS consCreateDate, consdetail.modifiedon AS consdetailModifyDate, inv.isupdated FROM purchasereturn pr "
                        +" LEFT JOIN prdetails prd ON pr.id=prd.purchasereturn LEFT JOIN inventory inv ON prd.id=inv.id "
                        +" LEFT JOIN in_stockmovement stock ON pr.id = stock.modulerefid "
                        +" LEFT JOIN in_consignment cons ON cons.modulerefid=pr.id "
                        +" LEFT JOIN in_consignmentdetails consdetail ON consdetail.consignment = cons.id WHERE pr.company='" + companyid + "' ORDER BY pr.orderdate";
                pst2 = conn.prepareStatement(prApplyDateQuery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    int isupdated = 1;
                    totalCount++;
                    String prcreatedby = !StringUtil.isNullOrEmpty(rst2.getString("createdby")) ? rst2.getString("createdby") : compCreator;
                    if (!prevCreatedBy.equalsIgnoreCase(prcreatedby)) {
                        prevCreatedBy = prcreatedby;
                        String pruserTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + prcreatedby + "'";
                        tzpst = conn.prepareStatement(pruserTZQuery);
                        tzrst = tzpst.executeQuery();
                        while (tzrst.next()) {
                            String pruserTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + pruserTZDiff));
                        }
                    }//if
                    String prid = rst2.getString("id");
                    String prnumber = rst2.getObject("prnumber") != null ? rst2.getString("prnumber") : "Unknown Number";
                    String prdetailID = rst2.getObject("prdetailID") != null ? rst2.getString("prdetailID") : null;
                    String stockid = rst2.getObject("stockid") != null ? rst2.getString("stockid") : null;
                    String consid = rst2.getObject("consid") != null ? rst2.getString("consid") : null;
                    String consdetailid = rst2.getObject("consdetailid") != null ? rst2.getString("consdetailid") : null;
                    Date prcreationdate = rst2.getObject("orderdate") != null ? (java.util.Date) rst2.getObject("orderdate") : null;
                    Date prshipdate = rst2.getObject("shipdate") != null ? (java.util.Date) rst2.getObject("shipdate") : null;
                    Date prinvupdateDate = rst2.getObject("invdate") != null ? (java.util.Date) rst2.getObject("invdate") : null;
                    Date stocktxnDate = rst2.getObject("stocktxnDate") != null ? (java.util.Date) rst2.getObject("stocktxnDate") : null;
                    Date stockCreateDate = rst2.getObject("stockCreateDate") != null ? (java.util.Date) rst2.getObject("stockCreateDate") : null;
                    Date consFromDate = rst2.getObject("consFromDate") != null ? (java.util.Date) rst2.getObject("consFromDate") : null;
                    Date consTodate = rst2.getObject("consTodate") != null ? (java.util.Date) rst2.getObject("consTodate") : null;
                    Date consCreateDate = rst2.getObject("consCreateDate") != null ? (java.util.Date) rst2.getObject("consCreateDate") : null;
                    Date consdetailModifyDate = rst2.getObject("consdetailModifyDate") != null ? (java.util.Date) rst2.getObject("consdetailModifyDate") : null;
                    int prisfixedasset = rst2.getObject("isfixedasset") != null ? rst2.getInt("isfixedasset") : null;
                    isupdated = rst2.getInt("isupdated");
                    String newPRcreationDate=null, newPRShipDate=null, prsubquery="";
                    String newstocktxnDate=null, newstockCreateDate=null, newconsFromDate=null, newconsTodate=null, newconsCreateDate=null;
                    String prsubstockquery="", prconsignsubquery="";
                    pstinner = null; rstinner = null; 
                    
                    if (prcreationdate != null) {
                        newPRcreationDate = df.format(prcreationdate);
                        prsubquery += " orderdate='" + newPRcreationDate + "' ";
                    }
                    if (prshipdate != null) {
                        if (!StringUtil.isNullOrEmpty(prsubquery)) {
                            prsubquery += ", ";
                        }
                        newPRShipDate = df.format(prshipdate);
                        prsubquery += " shipdate='" + newPRShipDate + "' ";
                    }
                    
                    if (!StringUtil.isNullOrEmpty(prsubquery)) {
                    pstinner = null;
                    String updatePRDate = "UPDATE purchasereturn SET " + prsubquery + " WHERE id='" + prid + "'";
                    pstinner = conn.prepareStatement(updatePRDate);
                        int updatecount1 = pstinner.executeUpdate();
                        if (updatecount1 > 0) {
                            updateCount++;
                            // Nothing to do
                        } else {
                            //System.out.println("Purchase Return :"+prnumber+" is not updated.\n");
                            //out.println("Purchase Return :"+prnumber+" is not updated.<br>");
                        }    
                  }
                    
                  if(prisfixedasset==1 && prdetailID!=null) {
                      String assetPRQuery = "SELECT DISTINCT(asd.id), asd.installationdate FROM assetdetailsinvdetailmapping adm LEFT JOIN assetdetail asd ON adm.assetdetails=asd.id WHERE adm.invoicedetailid='" + prdetailID + "' AND adm.company='" + companyid + "'";
                        pstinner = null;
                        rstinner = null;
                        pstinner = conn.prepareStatement(assetPRQuery);
                        rstinner = pstinner.executeQuery();
                        while (rstinner.next()) {
                            String prassetid = rstinner.getObject("id") != null ? rstinner.getString("id") : null;
                            Date prinstallDate = rstinner.getObject("installationdate") != null ? (java.util.Date) rstinner.getObject("installationdate") : null;
                            if (prassetid != null && prinstallDate != null) {
                                pstinner2 = null;
                                String newPRInstallDate = df.format(prinstallDate);
                                String updateAssetPRDate = "UPDATE assetdetail SET installationdate='" + newPRInstallDate + "' WHERE id='" + prassetid + "'";
                                pstinner2 = conn.prepareStatement(updateAssetPRDate);
                                int updatecount = pstinner2.executeUpdate();
                                if (updatecount > 0) {
                                    //Nothing to do
                                } else {
                                    //System.out.println("Asset Purchase Return :"+prnumber+" is not updated.\n");
                                    //out.println("Asset Purchase Return :"+prnumber+" is not updated.<br>");
                                }
                            }//aseet date
                        }//asset loop
                  }
                    // Stock Movement Date updates
                    if (stocktxnDate != null) {
                        newstocktxnDate = df.format(stocktxnDate);
                        prsubstockquery += " transaction_date='" + newstocktxnDate + "' ";
                    }
                    if (stockCreateDate != null) {
                        if (!StringUtil.isNullOrEmpty(prsubstockquery)) {
                            prsubstockquery += ", ";
                        }
                        newstockCreateDate = utcdf.format(stockCreateDate);
                        prsubstockquery += " createdon='" + newstockCreateDate + "' ";
                    }
                    if (!StringUtil.isNullOrEmpty(prsubstockquery)) {
                    pstinner = null;
                    String updatePRStockDate = "UPDATE in_stockmovement SET " + prsubstockquery + " WHERE id='" + stockid + "'";
                    pstinner = conn.prepareStatement(updatePRStockDate);
                        int updatecount1 = pstinner.executeUpdate();
                        if (updatecount1 > 0) {
                            updateCount++;
                            // Nothing to do
                        } else {
                            //System.out.println("Purchase Return :"+prnumber+" - Stock is not updated.\n");
                            //out.println("Purchase Return :"+prnumber+" - Stock is not updated.<br>");
                        }    
                    }
                    
                    //Consignment date updates
                    if (consFromDate != null) {
                        newconsFromDate = df.format(consFromDate);
                        prconsignsubquery += " fromdate='" + newconsFromDate + "' ";
                    }
                    if (consTodate != null) {
                        if (!StringUtil.isNullOrEmpty(prconsignsubquery)) {
                            prconsignsubquery += ", ";
                        }
                        newconsTodate = df.format(consTodate);
                        prconsignsubquery += " todate='" + newconsTodate + "' ";
                    }
                    if (consCreateDate != null) {
                        if (!StringUtil.isNullOrEmpty(prconsignsubquery)) {
                            prconsignsubquery += ", ";
                        }
                        newconsCreateDate = utcdf.format(consCreateDate);
                        prconsignsubquery += " createdon='" + newconsCreateDate + "' ";
                    }
                    if (!StringUtil.isNullOrEmpty(prconsignsubquery)) {
                    pstinner = null;
                    String updatePRConsignDate = "UPDATE in_consignment SET " + prconsignsubquery + " WHERE id='" + consid + "'";
                    pstinner = conn.prepareStatement(updatePRConsignDate);
                        int updatecount1 = pstinner.executeUpdate();
                        if (updatecount1 > 0) {
                            updateCount++;
                            // Nothing to do
                        } else {
                            //System.out.println("Purchase Return :"+prnumber+" - Consignment is not updated.\n");
                            //out.println("Purchase Return :"+prnumber+" - Consignment is not updated.<br>");
                        }    
                    }
                    
                  //Consignment Detail Date updates 
                  if (consdetailModifyDate != null) {
                        pstinner = null;
                        String newPRConsDetailDate = utcdf.format(consdetailModifyDate);
                        String updatePRConsDetailDate = "UPDATE in_consignmentdetails SET modifiedon='" + newPRConsDetailDate + "' WHERE id='" + consdetailid + "'";
                        pstinner = conn.prepareStatement(updatePRConsDetailDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            //Nothing to do.
                        } else {
                            //System.out.println("Purchase Return :"+prnumber+" - Consignment Detail is not updated.\n");
                            //out.println("Purchase Return :"+prnumber+" - Consignment Detail is not updated.<br>");
                        }
                  } 
                    
                  if (prinvupdateDate != null && isupdated==0) {
                        pstinner = null;
                        String newPRInvDate = df.format(prinvupdateDate);
                        String updatePRInvDate = "UPDATE inventory SET updatedate='" + newPRInvDate + "', isupdated=1 WHERE id='" + prdetailID + "'";
                        pstinner = conn.prepareStatement(updatePRInvDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            //Nothing to do.
                        } else {
                            //System.out.println("Purchase Return :"+prnumber+" - Inventory is not updated.\n");
                            //out.println("Purchase Return :"+prnumber+" - Inventory is not updated.<br>");
                        }
                    }
                }//createdBy
//                out.println("Total No. of Records :"+totalCount+" <br>");
//                out.println("Updated No. of Records :"+updateCount+" <br>");
                System.out.println("Purchase Return Module Ended...\n");
//                out.println("<b>Purchase Return Module Ended...<b><br>");
//======================================================= PURCHASE RETURN MODULE - END =========================================================

                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
//======================================================= PURCHASE REQUISITION MODULE - START =========================================================

                System.out.println("PURCHASE REQUISITION MODULE STARTTED...\n");
//                out.println("<b>PURCHASE REQUISITION MODULE STARTTED...</b>");
                prevCreatedBy = "";
                totalCount=0; updateCount=0; 
                rst2 = null;
                String prqApplyDateQuery = "SELECT pr.id, pr.createdby, pr.prnumber, pr.requisitiondate, pr.duedate, pr.users, pr.isfixedassetpurchaserequisition, prd.id AS detailid FROM purchaserequisition pr LEFT JOIN purchaserequisitiondetail prd ON pr.id=prd.purchaserequisition WHERE pr.company='" + companyid + "' ORDER BY pr.requisitiondate";
                pst2 = conn.prepareStatement(prqApplyDateQuery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    totalCount++;
                    String prqcreatedby = !StringUtil.isNullOrEmpty(rst2.getString("createdby")) ? rst2.getString("createdby") : compCreator;
                    if (!prevCreatedBy.equalsIgnoreCase(prqcreatedby)) {
                        prevCreatedBy = prqcreatedby;
                        String prquserTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + prqcreatedby + "'";
                        tzpst = conn.prepareStatement(prquserTZQuery);
                        tzrst = tzpst.executeQuery();
                        while (tzrst.next()) {
                            String prquserTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + prquserTZDiff));
                        }
                    }//if
                    String prqid = rst2.getString("id");
                    String prqnumber = rst2.getObject("prnumber") != null ? rst2.getString("prnumber") : "Unknown Number";
                    String prqdetailid = rst2.getString("detailid") != null ? rst2.getString("detailid") : null;
                    Date prqcreationdate = rst2.getObject("requisitiondate") != null ? (java.util.Date) rst2.getObject("requisitiondate") : null;
                    Date prqduedate = rst2.getObject("duedate") != null ? (java.util.Date) rst2.getObject("duedate") : null;
                    int prqisAssetRequisition = rst2.getObject("isfixedassetpurchaserequisition") != null ? rst2.getInt("isfixedassetpurchaserequisition") : 0;
                    String newPRQcreationDate=null, newPRQDueDate=null, prqsubquery="";
                    pstinner = null;  rstinner = null;
                    
                        if (prqcreationdate != null) {
                            newPRQcreationDate = df.format(prqcreationdate);
                            prqsubquery += " requisitiondate='" + newPRQcreationDate + "' ";
                        }
                        if (prqduedate != null) {
                            if (!StringUtil.isNullOrEmpty(prqsubquery)) {
                            prqsubquery += ", ";
                            }
                            newPRQDueDate = df.format(prqduedate);
                            prqsubquery += " duedate='" + newPRQDueDate + "' ";
                        }
                    if (!StringUtil.isNullOrEmpty(prqsubquery)) {
                        pstinner = null;
                        String updatePReqDate = "UPDATE purchaserequisition SET " + prqsubquery + " WHERE id='" + prqid + "'";
                            pstinner = conn.prepareStatement(updatePReqDate);
                            int updatecount1 = pstinner.executeUpdate();
                            if (updatecount1 > 0) {
                                updateCount++;
                                //Nothing to do
                            } else {
                                //System.out.println("Purchase Requisition :"+prqnumber+" is not updated.\n");
                                //out.println("Purchase Requisition :"+prqnumber+" is not updated.<br>");
                            }
                    }
                    
                    if(prqisAssetRequisition==1 && prqdetailid!=null){
                        //Code to update installation date of Fixed Asset Purchase Requisition.
                        String assetRequiQuery = "SELECT pr.id, pr.installationdate FROM assetdetailspurchaserequisitiondetailmapping pm LEFT JOIN purchaserequisitionassetdetail pr ON pm.purchaserequisitionassetdetails=pr.id WHERE pm.purchaserequisitiondetailid='" + prqdetailid + "' AND pm.company='" + companyid + "'";
                        pstinner = null;
                        rstinner = null;
                        pstinner = conn.prepareStatement(assetRequiQuery);
                        rstinner = pstinner.executeQuery();
                        while (rstinner.next()) {
                            String prqassetid = rstinner.getString("id") != null ? rstinner.getString("id") : null;
                            Date prqinstallDate = rstinner.getObject("installationdate") != null ? (java.util.Date) rstinner.getObject("installationdate") : null;
                            if (prqassetid != null && prqinstallDate != null) {
                                pstinner2 = null;
                                String newPRQInstallDate = df.format(prqinstallDate);
                                String updateAssetPReqDate = "UPDATE purchaserequisitionassetdetail SET installationdate='" + newPRQInstallDate + "' WHERE id='" + prqassetid + "'";
                                pstinner2 = conn.prepareStatement(updateAssetPReqDate);
                                int updatecount = pstinner2.executeUpdate();
                                if (updatecount > 0) {
                                    //Nothing to do.
                                } else {
                                    //System.out.println("Asset Purchase Requisition :"+prqnumber+" is not updated.\n");
                                    //out.println("Asset Purchase Requisition :"+prqnumber+" is not updated.<br>");
                                }
                            }//aseet date
                        }//asset loop
                    }//Fixed Asset PReq.
                }//createdBy
//                out.println("Total No. of Records :"+totalCount+" <br>");
//                out.println("Updated No. of Records :"+updateCount+" <br>");
                System.out.println("PURCHASE REQUISITION MODULE ENDED...\n");
//                out.println("<b>PURCHASE REQUISITION MODULE ENDED...<b><br>");
//======================================================= PURCHASE REQUISITION MODULE - END =========================================================

                System.out.println("----------------------------------------------------------------------------------------------\n");
//                out.println("<b>---------------------------------------------------------------------------------------------<b><br>");
//======================================================= DELIVERY ORDER MODULE - START ====================================================
                
                System.out.println("DELIVERY ORDER MODULE STARTED...\n");
//                out.println("<b>DELIVERY ORDER MODULE STARTED...</b>");
                prevCreatedBy = "";
                totalCount=0; updateCount=0;
                rst2 = null;
                String doApplyDateQuery = "SELECT do.id, do.donumber, dod.id as dodetailID, do.createdby, do.orderdate, do.shipdate, je.id AS jeid, je.entrydate, do.isfixedassetdo, inv.updatedate AS invdate, inv.isupdated FROM deliveryorder do LEFT JOIN dodetails dod ON do.id=dod.deliveryorder LEFT JOIN inventory inv ON dod.id=inv.id LEFT JOIN productbuild pb ON do.id=pb.deliveryorder LEFT JOIN journalentry je ON pb.journalentry=je.id WHERE do.company='" + companyid + "' ORDER BY do.orderdate";
                pst2 = conn.prepareStatement(doApplyDateQuery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    int isupdated = 1;
                    totalCount++;
                    String docreatedby = !StringUtil.isNullOrEmpty(rst2.getString("createdby")) ? rst2.getString("createdby") : compCreator;
                    if (!prevCreatedBy.equalsIgnoreCase(docreatedby)) {
                        prevCreatedBy = docreatedby;
                        String userDOTZQuery = "SELECT tz.difference from timezone tz INNER JOIN users ur ON ur.timezone=tz.timezoneid WHERE ur.userid='" + docreatedby + "'";
                        tzpst = conn.prepareStatement(userDOTZQuery);
                        tzrst = tzpst.executeQuery();
                        while (tzrst.next()) {
                            String douserTZDiff = !StringUtil.isNullOrEmpty(tzrst.getString("difference")) ? tzrst.getString("difference") : defaultTZ;
                            df.setTimeZone(TimeZone.getTimeZone("GMT" + douserTZDiff));
                        }
                    }//if
                    String doid = rst2.getString("id");
                    String donumber = rst2.getObject("donumber") != null ? rst2.getString("donumber") : "Unknown Number";
                    String dodetailID = rst2.getObject("dodetailID") != null ? rst2.getString("dodetailID") : null;
                    Date docreationdate = rst2.getObject("orderdate") != null ? (java.util.Date) rst2.getObject("orderdate") : null;
                    Date doshipdate = rst2.getObject("shipdate") != null ? (java.util.Date) rst2.getObject("shipdate") : null;
                    Date doinvdate = rst2.getObject("invdate") != null ? (java.util.Date) rst2.getObject("invdate") : null;
                    int doisfixedassetdo = rst2.getObject("isfixedassetdo") != null ? rst2.getInt("isfixedassetdo") : null;
                    isupdated = rst2.getInt("isupdated");
                    //String dojeid = rst2.getString("jeid")!=null?rst2.getString("jeid"):null;
                    //Date dojedate = rst2.getObject("entrydate") != null ? (java.util.Date) rst2.getObject("entrydate") : null;
                                        
                    String newDOcreationDate=null, newDOShipDate=null, newDOInvdate=null, dosubquery="";
                    
                    
                    if (docreationdate != null) {
                        newDOcreationDate = df.format(docreationdate);
                        dosubquery += " orderdate='" + newDOcreationDate + "' ";
                    }
                    if (doshipdate != null) {
                        if (!StringUtil.isNullOrEmpty(dosubquery)) {
                            dosubquery += ", ";
                        }
                        newDOShipDate = df.format(doshipdate);
                        dosubquery += " shipdate='" + newDOShipDate + "' ";
                    }
                 if (!StringUtil.isNullOrEmpty(dosubquery)) {
                    pstinner = null;
                    String updateDODate = "UPDATE deliveryorder SET " + dosubquery + " WHERE id='" + doid + "'";
                    pstinner = conn.prepareStatement(updateDODate);
                            int updatecount1 = pstinner.executeUpdate();
                            if (updatecount1 > 0) {
                                updateCount++;
                                //Nothing to do
                            } else {
                                //System.out.println("Delivery Order :"+donumber+" is not updated.\n");
                                //out.println("Delivery Order :"+donumber+" is not updated.<br>");
                            }
                 }
                 if(doisfixedassetdo==1 && dodetailID!=null){
                        String assetDOQuery = "SELECT DISTINCT(asd.id), asd.installationdate FROM assetdetailsinvdetailmapping adm LEFT JOIN assetdetail asd ON adm.assetdetails=asd.id WHERE adm.invoicedetailid='" + dodetailID + "' AND adm.company='" + companyid + "'";
                        pstinner = null;
                        rstinner = null;
                        pstinner = conn.prepareStatement(assetDOQuery);
                        rstinner = pstinner.executeQuery();
                        while (rstinner.next()) {
                            String doassetid = rstinner.getString("id") != null ? rstinner.getString("id") : null;
                            Date doinstallDate = rstinner.getObject("installationdate") != null ? (java.util.Date) rstinner.getObject("installationdate") : null;
                            if (doassetid != null && doinstallDate != null) {
                                pstinner2 = null;
                                String newDOInstallDate = df.format(doinstallDate);
                                String updateAssetDODate = "UPDATE assetdetail SET installationdate='" + newDOInstallDate + "' WHERE id='" + doassetid + "'";
                                pstinner2 = conn.prepareStatement(updateAssetDODate);
                                int updatecount = pstinner2.executeUpdate();
                                if (updatecount > 0) {
                                    //nothing to do
                                } else {
                                    //System.out.println("Asset Delivery Order Date is not updated \n");
                                    //out.println("Asset Delivery Order Date is not updated.<br>");
                                }
                            }//aseet date
                        }//asset loop
                 }
/*                    if (jedate != null) {
                        pstinner = null;
                        rstinner = null;
                        String newJeDate = df.format(jedate);
                        String updateJEDate = "UPDATE journalentry SET entyrdate='" + newJeDate + "' WHERE id='" + jeid + "'";
                        pstinner = conn.prepareStatement(updateJEDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            updateCount++;
                            //System.out.println("Updated Journal Entry Date : " + newJeDate + "\n");
                            //out.println("Updated Journal Entry Date : " + newJeDate + "<br>");
                        } else {
                            System.out.println("Journal Entry Date is not updated \n");
                            out.println("Journal Entry Date is not updated.<br>");
                        }
                    }           */
                    if (doinvdate != null && isupdated==0) {
                        pstinner = null;
                        rstinner = null;
                        String newDOInvDate = df.format(doinvdate);
                        String updateDOInvDate = "UPDATE inventory SET updatedate='" + newDOInvDate + "', isupdated=1 WHERE id='" + dodetailID + "'";
                        pstinner = conn.prepareStatement(updateDOInvDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
//                            System.out.println("Updated Inventory Date : " + newDOInvDate + "\n");
//                            out.println("Updated Inventory Date : " + newDOInvDate + "<br>");
                        } else {
//                            System.out.println("Inventory Date is not updated \n");
//                            out.println("Inventory Date is not updated.<br>");
                        }
                    }
                }//createdBy
//                out.println("Total No. of Records :"+totalCount+" <br>");
//                out.println("Updated No. of Records :"+updateCount+" <br>");
                System.out.println("DELIVERY ORDER MODULE ENDED...\n");
//                out.println("<b>DELIVERY ORDER MODULE ENDED...<b><br>");
                
//======================================================= DELIVERY MODULE - END ==================================================  
//                out.println("<center><b>=================================================================================</b></center><br><br>");
            }//companyid
       // }//Server query while
/*
        out.println("<br><br><br><br> Alter Queries For VENDOR QUOTATION<br><br><br><br>");
        String modifyVQDate = "ALTER TABLE vendorquotation MODIFY quotationdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyVQDate);
            boolean PODates = modifypst.execute();
            System.out.println("Data type of quotationdate column is altered to 'Date'.\n");
            out.println("Data type of quotationdate column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("quotationdate column data type not modified.<br>");
        }

        modifypst = null;
        String modifyVQShipDate = "ALTER TABLE vendorquotation MODIFY shipdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyVQShipDate);
            boolean shipDates = modifypst.execute();
            System.out.println("Data type of Ship Date column is altered to 'Date'.\n");
            out.println("Data type of Ship Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Ship Date column data type not modified.<br>");
        }

        modifypst = null;
        String modifyVQDueDate = "ALTER TABLE vendorquotation MODIFY duedate DATE";
        try {
            modifypst = conn.prepareStatement(modifyVQDueDate);
            modifypst.execute();
            System.out.println("Data type of Due Date column is altered to 'Date'.\n");
            out.println("Data type of Due Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Due Date column data type not modified.<br>");
        }

        modifypst = null;
        String modifyVQValiddate = "ALTER TABLE vendorquotation MODIFY validdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyVQValiddate);
            modifypst.execute();
            System.out.println("Data type of validdate column is altered to 'Date'.\n");
            out.println("Data type of validdate column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("validdate column data type not modified.<br>");
        }
//----------------------------------------------------------------------------------------------------------------------------------------------                
        out.println("<br><br><br><br> Alter Queries For PURCHASE ORDER <br><br><br><br>");
        String modifyPODate = "ALTER TABLE purchaseorder MODIFY orderdate DATE";
        try {
            modifypst = null;
            modifypst = conn.prepareStatement(modifyPODate);
            modifypst.execute();
            System.out.println("Data type of Order Date column is altered to 'Date'.\n");
            out.println("Data type of Order Date column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Order Date column data type not modified.<br/>");
        }

        modifypst = null;
        String modifyPOShipDate = "ALTER TABLE purchaseorder MODIFY shipdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyPOShipDate);
            modifypst.execute();
            System.out.println("Data type of Ship Date column is altered to 'Date'.\n");
            out.println("Data type of Ship Date column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Ship Date column data type not modified.<br/>");
        }

        modifypst = null;
        String modifyPODueDate = "ALTER TABLE purchaseorder MODIFY duedate DATE";
        try {
            modifypst = conn.prepareStatement(modifyPODueDate);
            modifypst.execute();
            System.out.println("Data type of Due Date column is altered to 'Date'.\n");
            out.println("Data type of Due Date column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Due Date column data type not modified.<br/>");
        }
//----------------------------------------------------------------------------------------------------------------------------------------------                
        out.println("<br><br><br><br> Alter Queries For PURCHASE INVOICE <br><br><br><br>");
        String modifyPIDate = "ALTER TABLE goodsreceipt MODIFY creationdate DATE";
        try {
            modifypst = null;
            modifypst = conn.prepareStatement(modifyPIDate);
            modifypst.execute();
            System.out.println("Data type of creationdate column is altered to 'Date'.\n");
            out.println("Data type of creationdate column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Creation Date column data type not modified.<br/>");
        }

        modifypst = null;
        String modifyPIShipDate = "ALTER TABLE goodsreceipt MODIFY shipdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyPIShipDate);
            modifypst.execute();
            System.out.println("Data type of Ship Date column is altered to 'Date'.\n");
            out.println("Data type of Ship Date column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("shipdate column data type not modified.<br/>");
        }

        modifypst = null;
        String modifyPIDueDate = "ALTER TABLE goodsreceipt MODIFY duedate DATE";
        try {
            modifypst = conn.prepareStatement(modifyPIDueDate);
            modifypst.execute();
            System.out.println("Data type of Due Date column is altered to 'Date'.\n");
            out.println("Data type of Due Date column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Duedate column data type not modified.<br/>");
        }

        modifypst = null;
        String modifyPIDebtDate = "ALTER TABLE goodsreceipt MODIFY debtclaimeddate DATE";
        try {
            modifypst = conn.prepareStatement(modifyPIDebtDate);
            modifypst.execute();
            System.out.println("Data type of debtclaimeddate Date column is altered to 'Date'.\n");
            out.println("Data type of debtclaimeddate Date column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Debtclaimeddate Date column data type not modified.<br/>");
        }

        modifypst = null;
        String modifyPIPartyInvDate = "ALTER TABLE goodsreceipt MODIFY partyinvoicedate DATE";
        try {
            modifypst = conn.prepareStatement(modifyPIPartyInvDate);
            modifypst.execute();
            System.out.println("Data type of partyinvoicedate Date column is altered to 'Date'.\n");
            out.println("Data type of partyinvoicedate Date column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Partyinvoicedate Date column data type not modified.<br/>");
        }
//----------------------------------------------------------------------------------------------------------------------------------------------
        out.println("<br><br><br><br> Alter Queries For MAKE PAYMENT <br><br><br><br>");
        String modifyMPDate = "ALTER TABLE payment MODIFY creationdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyMPDate);
            modifypst.execute();
            System.out.println("Data type of Creation column is altered to 'Date'.\n");
            out.println("Data type of Creation column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("CreationDate column data type not modified.<br>");
        }

        modifypst = null;
        String modifyMPChqDate = "ALTER TABLE payment MODIFY chequedate DATE";
        try {
            modifypst = conn.prepareStatement(modifyMPChqDate);
            modifypst.execute();
            System.out.println("Data type of Cheque Date column is altered to 'Date'.\n");
            out.println("Data type of Cheque Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Cheque Date column data type not modified.<br>");
        }
//-----------------------------------------------------------------------------------------------------------------------------------------------
        out.println("<br><br><br><br> Alter Queries For GOODS RECEIPT <br><br><br><br>");
        modifypst = null;
        String modifyGRDate = "ALTER TABLE grorder MODIFY grorderdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyGRDate);
            boolean GRDate = modifypst.execute();
            System.out.println("Data type of grorderdate column is altered to 'Date'.\n");
            out.println("Data type of grorderdate column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("grorderdate column data type not modified.<br>");
        }

        modifypst = null;
        String modifyGRShipDate = "ALTER TABLE grorder MODIFY shipdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyGRShipDate);
            boolean grshipDates = modifypst.execute();
            System.out.println("Data type of Ship Date column is altered to 'Date'.\n");
            out.println("Data type of Ship Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Ship Date column data type not modified.<br>");
        }

/*        modifypst = null;
        String modifyInvUpdateDate = "ALTER TABLE inventory MODIFY updatedate DATE";
        try {
            modifypst = conn.prepareStatement(modifyInvUpdateDate);
            modifypst.execute();
            System.out.println("Data type of update Date column is altered to 'Date'.\n");
            out.println("Data type of update Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Update Date column data type not modified.<br>");
        }    
*/
//-----------------------------------------------------------------------------------------------------------------------------------------------

/*        
               out.println("<br><br><br><br> Alter Queries For DEBIT NOTE <br><br><br><br>");
        modifypst = null;
        String modifyDNDate = "ALTER TABLE debitnote MODIFY creationdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyDNDate);
            boolean DNDate = modifypst.execute();
            System.out.println("Data type of DN Date column is altered to 'Date'.\n");
            out.println("Data type of DN Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("DN Date column data type not modified.<br>");
        }
//-----------------------------------------------------------------------------------------------------------------------------------------------
        out.println("<br><br><br><br> Alter Queries For PURCHASE RETURN <br><br><br><br>");
        modifypst = null;
        String modifyPRDate = "ALTER TABLE purchasereturn MODIFY orderdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyPRDate);
            boolean PRDate = modifypst.execute();
            System.out.println("Data type of Order Date column is altered to 'Date'.\n");
            out.println("Data type of Order Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Order Date column data type not modified.<br>");
        }

        modifypst = null;
        String modifyPRShipDate = "ALTER TABLE purchasereturn MODIFY shipdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyPRShipDate);
            boolean PRshipDates = modifypst.execute();
            System.out.println("Data type of Ship Date column is altered to 'Date'.\n");
            out.println("Data type of Ship Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Ship Date column data type not modified.<br>");
        }
//-----------------------------------------------------------------------------------------------------------------------------------------------
        out.println("<br><br><br><br> Alter Queries For PURCHASE REQUISITION <br><br><br><br>");
        modifypst = null;
        String modifyPReqDate = "ALTER TABLE purchaserequisition MODIFY requisitiondate DATE";
        try {
            modifypst = conn.prepareStatement(modifyPReqDate);
            boolean PReqDate = modifypst.execute();
            System.out.println("Data type of Requisition Date column is altered to 'Date'.\n");
            out.println("Data type of Requisition Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Requisition Date column data type not modified.<br>");
        }

        modifypst = null;
        String modifyPReqDueDate = "ALTER TABLE purchaserequisition MODIFY duedate DATE";
        try {
            modifypst = conn.prepareStatement(modifyPReqDueDate);
            boolean PReqDueDates = modifypst.execute();
            System.out.println("Data type of Due Date column is altered to 'Date'.\n");
            out.println("Data type of Due Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Due Date column data type not modified.<br>");
        }

        modifypst = null;
        String modifyAssetPReqDate = "ALTER TABLE purchaserequisitionassetdetail MODIFY installationdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyAssetPReqDate);
            boolean AssetPReqDate = modifypst.execute();
            System.out.println("Data type of Installation Date column is altered to 'Date'.\n");
            out.println("Data type of Installation Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Installation Date column data type not modified.<br>");
        }
//-----------------------------------------------------------------------------------------------------------------------------------------------
        out.println("<br><br><br><br> Alter Queries For DELIVERY ORDER <br><br><br><br>");
        modifypst = null;
        String modifyDODate = "ALTER TABLE deliveryorder MODIFY orderdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyDODate);
            boolean DODate = modifypst.execute();
            System.out.println("Data type of Order Date column is altered to 'Date'.\n");
            out.println("Data type of Order Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Order Date column data type not modified.<br>");
        }

        modifypst = null;
        String modifyDOShipDate = "ALTER TABLE purchasereturn MODIFY shipdate DATE";
        try {
            modifypst = conn.prepareStatement(modifyDOShipDate);
            boolean DOshipDate = modifypst.execute();
            System.out.println("Data type of Ship Date column is altered to 'Date'.\n");
            out.println("Data type of Ship Date column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Ship Date column data type not modified.<br>");
        } 
 
        modifypst = null;
        String modifyInvDate = "ALTER TABLE inventory MODIFY updatedate DATE";
        try {
            modifypst = conn.prepareStatement(modifyInvDate);
            modifypst.execute();
            System.out.println("Data type of Inventory update column is altered to 'Date'.\n");
            out.println("Data type of Inventory update column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Inventory update column data type not modified.<br>");
        }   
//-----------------------------------------------------------------------------------------------------------------------------------------------
        out.println("<br><br><br><br> Alter Queries For INVENTORY <br><br><br><br>");
        modifypst = null;
        String modifyInvDate = "ALTER TABLE inventory MODIFY updatedate DATE";
        try {
            modifypst = conn.prepareStatement(modifyInvDate);
            modifypst.execute();
            System.out.println("Data type of Inventory - UpdateDate column is altered to 'Date'.\n");
            out.println("Data type of Inventory - UpdateDate column is altered to 'Date'.<br>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Inventory - UpdateDate column data type not modified.<br>");
        }
//-----------------------------------------------------------------------------------------------------------------------------------------------        
*/
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