
<%@page import="com.krawler.hql.accounting.Producttype"%>
<%@page import="com.krawler.inventory.model.stockmovement.TransactionModule"%>
<%@page import="com.krawler.inventory.model.stockmovement.TransactionType"%>
<%@page import="com.krawler.spring.authHandler.authHandler"%>
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
        //SCRIPT URL : http://<app-url>/DateMigrationForProductsModule.jsp?serverip=?&dbname=?&username=?&password=?&subdomain=?
        // 'subdomain' is a mandatory field

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
        String serverTZQuery = "", serverTZDiff = "", userTZDiff = "", query1 = "", applyDateQuery = "", companyid = "";
        String productId = "", productBatchId = "",compCreator = "", ctzone="", utzone="",defaultTZ="+08:00";

        Date creationdate = null, duedate = null;

        PreparedStatement serverpst = null, pst1 = null, pst2 = null, pst3 = null, pst4 = null,cppst=null;
        ResultSet serverrst = null, rst1 = null, rst2 = null, rst3 = null, rst4 = null, innerRst3 = null, innerMostRst3 = null,cprst=null;
        PreparedStatement pst, pstinner, tzpst, modifypst, innerPst3, innerMostPst3;
        ResultSet tzrst;
        int companycount=0;

        String subdomainQuery = "";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainQuery = " WHERE subdomain='" + subdomain + "'";
        }
        
        //Execution Started :
        out.println("<br><br>Execution Started @ "+new java.util.Date()+"<br><br>");

        Class.forName(driver).newInstance();
        //Get Server's timezone from which we ll convert date to Company Creator's timezone.
        TimeZone tzone = Calendar.getInstance().getTimeZone();
        System.out.println("Server Timezone Name : " + tzone.getDisplayName() + "\n");   //O/p E.g.: Indian Standard Time
        out.println("Server Timezone Name : " + tzone.getDisplayName() + "<br/>");
//        String serverTZID = tzone.getID();
//        System.out.println("Server Timezone ID : " + serverTZID + "\n");  //O/p E.g. Asia/Calcutta
//        out.println("Server Timezone ID : " + serverTZID + "<br/>");
        
        //Define Timezone object
        DateFormat df = null;
        DateFormat constantDf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        constantDf.setTimeZone(TimeZone.getTimeZone("GMT"));
        DateFormat serverTzDf = null;
        
        conn = DriverManager.getConnection(connectString, username, password);
        conn.setAutoCommit(false);
//        serverTZQuery = "SELECT difference FROM timezone WHERE tzid like '%" + serverTZID + "%' OR name like '%" + tzone.getDisplayName() + "%'";
//        serverpst = conn.prepareStatement(serverTZQuery);
//        serverrst = serverpst.executeQuery();
//        while (serverrst.next()) {
//            serverTZDiff = serverrst.getString("difference");
            serverTzDf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            serverTzDf.setTimeZone(TimeZone.getTimeZone("GMT"));
//            
//            
//            System.out.println("Server Timezone Difference : " + serverTZDiff + "\n");
            query1 = "SELECT c.companyid, c.creator, c.subdomain, c.timezone AS ctzone, u.timezone AS utzone FROM company c INNER JOIN users u "
                + "ON c.creator=u.userid " + subdomainQuery;
            pst1 = conn.prepareStatement(query1);
            rst1 = pst1.executeQuery();
            while (rst1.next()) {
                companycount++;
                companyid = rst1.getString("companyid");
                String cmpSubdomain = rst1.getString("subdomain");
                compCreator = rst1.getString("creator")!=null?rst1.getString("creator"):null;
                if (StringUtil.isNullOrEmpty(compCreator)) {
                        compCreator = "";
                        continue;
                }
                ctzone = rst1.getObject("ctzone") != null ? rst1.getString("ctzone") : "268";   //Default Singapore
                utzone = rst1.getObject("utzone") != null ? rst1.getString("utzone") : ctzone;   
                
                // To Fetch company creator's timezone.
                String defaultTimezone = "SELECT difference from timezone WHERE timezoneid='"+utzone+"'";
                cppst = conn.prepareStatement(defaultTimezone);
                cprst = cppst.executeQuery();
                if (cprst.next()) {
                    defaultTZ = cprst.getString("difference")!=null?cprst.getString("difference"): "+08:00";
                }
                df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("GMT" + defaultTZ));
                System.out.println(companycount +" : "+cmpSubdomain);
                out.println("<center><b>"+companycount+" : "+cmpSubdomain+"</b></center><br><br>");                
                System.out.println("Products Module Started...\n");
                out.println("<b>Subdomain '" +cmpSubdomain+ "' Products Module Started...</b><br>");
                applyDateQuery = "SELECT p.id, p.name, p.islocationforproduct, p.iswarehouseforproduct, p.isBatchForProduct, p.isSerialForProduct FROM product p WHERE p.company='" + companyid + "' ORDER BY p.createdon";
                pst2 = conn.prepareStatement(applyDateQuery);
                rst2 = pst2.executeQuery();
                while (rst2.next()) {
                    productId = rst2.getObject("id")!=null ? rst2.getString("id") : "";
                    String productName = rst2.getObject("name")!=null ? rst2.getString("name") : "";
                    out.println("<br><b>Product "+productName+" details are updating : </b><br>");
                    boolean islocationforproduct = rst2.getObject("islocationforproduct")!=null ? rst2.getBoolean("islocationforproduct") : false;
                    boolean iswarehouseforproduct = rst2.getObject("iswarehouseforproduct")!=null ? rst2.getBoolean("iswarehouseforproduct") : false;
                    boolean isBatchForProduct = rst2.getObject("isBatchForProduct")!=null ? rst2.getBoolean("isBatchForProduct") : false;
                    boolean isSerialForProduct = rst2.getObject("isSerialForProduct")!=null ? rst2.getBoolean("isSerialForProduct") : false;
                    productBatchId = "";
                    
                    //Update Stock Movement Dates
                    if (iswarehouseforproduct && islocationforproduct) {
                        String queryToGetStockMovements = "SELECT id, transaction_date, createdon FROM in_stockmovement WHERE product = '"+ productId + "' and transaction_type = " + 0 + " and transaction_module = " + 10;
                        pst3 = conn.prepareStatement(queryToGetStockMovements);
                        rst3 = pst3.executeQuery();
                        while (rst3.next()) {
                            String stockMovementId = rst3.getString("id") != null ? rst3.getString("id") : null;
                            creationdate = rst3.getObject("createdon") != null ? (java.util.Date) rst3.getObject("createdon") : null;
                            duedate = rst3.getObject("transaction_date") != null ? (java.util.Date) rst3.getObject("transaction_date") : null;

                            //Convert Date from Server's timezone to Admin's timezone
                            String newcreationDate = null, newDueDate = null;
                            if (creationdate != null) {
                                String convert1 = serverTzDf.format(creationdate);
                                newcreationDate = "'" + df.format(constantDf.parse(convert1)) + "'";
                            }//creationdate
                            if (duedate != null) {
                                String convert1 = serverTzDf.format(duedate);
                                newDueDate = "'" + df.format(constantDf.parse(convert1)) + "'";
                            }//duedate
                            
                            String updatePReqDate = "UPDATE in_stockmovement SET createdon=" + newcreationDate + ", transaction_date=" + newDueDate + " WHERE id='" + stockMovementId + "'";
                            pstinner = null;
                            pstinner = conn.prepareStatement(updatePReqDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Stock Movement : Updated Creation Date : " + newcreationDate + " & Transaction Date : " +newDueDate+ "\n");
                                //out.println("Stock Movement : Updated Creation Date : " + newcreationDate + " & Transaction Date : " + newDueDate + "<br>");
                            } else {
                                //System.out.println("Stock Movement : Creation Date & Transaction Date is not updated \n");
                                //out.println("Stock Movement : Creation Date & Transaction Date is not updated.<br>");
                            }
                        }//stockmovement loop end
                    }//end if of warehouse & location applied for product
                    
                    //Update New Product Batch & Location Batch Document Mapping Dates
                    if(islocationforproduct || iswarehouseforproduct || isBatchForProduct) {
                        
                        //************************New Product Batch****************************
                        String queryToGetStockMovements = "SELECT id, mfgdate, expdate FROM newproductbatch WHERE product = '"+ productId + "' and isopening = 'T' and transactiontype = 28 and ispurchase = 'T'";
                        pst3 = conn.prepareStatement(queryToGetStockMovements);
                        rst3 = pst3.executeQuery();
                        while (rst3.next()) {
                            productBatchId = rst3.getString("id") != null ? rst3.getString("id") : null;
                            creationdate = rst3.getObject("mfgdate") != null ? (java.util.Date) rst3.getObject("mfgdate") : null;
                            duedate = rst3.getObject("expdate") != null ? (java.util.Date) rst3.getObject("expdate") : null;

                            //Convert Date from Server's timezone to Admin's timezone
                            String newcreationDate = null, newDueDate = null;
                            if (creationdate != null) {
                                newcreationDate = "'" + df.format(creationdate) + "'";
                            }//creationdate
                            if (duedate != null) {
                                newDueDate = "'" + df.format(duedate) + "'";
                            }//duedate
                            
                            String updatePReqDate = "UPDATE newproductbatch SET mfgdate=" + newcreationDate + ", expdate=" + newDueDate + " WHERE id='" + productBatchId + "'";
                            pstinner = null;
                            pstinner = conn.prepareStatement(updatePReqDate);
                            int updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("New Product Batch : Updated Mfg Date : " + newcreationDate + " & Exp Date : " +newDueDate+ "\n");
                                //out.println("New Product Batch : Updated Mfg Date : " + newcreationDate + " & Exp Date : " + newDueDate + "<br>");
                            } else {
                                //System.out.println("New Product Batch : Mfg Date & Exp Date is not updated \n");
                                //out.println("New Product Batch : Mfg Date & Exp Date is not updated.<br>");
                            }
                            //********************************************************************
                        
                            //************************Location Batch Document Mapping****************************
                            queryToGetStockMovements = "SELECT id, mfgdate, expdate FROM locationbatchdocumentmapping WHERE batchmapid = '"+ productBatchId + "' and documentid = '" + productId + "' and transactiontype = 28";
                            innerPst3 = conn.prepareStatement(queryToGetStockMovements);
                            innerRst3 = innerPst3.executeQuery();
                            while (innerRst3.next()) {
                                String locationBatchDMId = innerRst3.getString("id") != null ? innerRst3.getString("id") : null;
                                creationdate = innerRst3.getObject("mfgdate") != null ? (java.util.Date) innerRst3.getObject("mfgdate") : null;
                                duedate = innerRst3.getObject("expdate") != null ? (java.util.Date) innerRst3.getObject("expdate") : null;

                                //Convert Date from Server's timezone to Admin's timezone
                                if (creationdate != null) {
                                    newcreationDate = "'" + df.format(creationdate)+ "'";
                                }//creationdate
                                if (duedate != null) {
                                    newDueDate = "'" + df.format(duedate) + "'";
                                }//duedate

                                updatePReqDate = "UPDATE locationbatchdocumentmapping SET mfgdate=" + newcreationDate + ", expdate=" + newDueDate + " WHERE id='" + locationBatchDMId + "'";
                                pstinner = null;
                                pstinner = conn.prepareStatement(updatePReqDate);
                                updatecount = pstinner.executeUpdate();
                                if (updatecount > 0) {
                                   //System.out.println("Location Batch Document Mapping : Updated Mfg Date : " + newcreationDate + " & Exp Date : " +newDueDate+ "\n");
                                    //out.println("Location Batch Document Mapping : Updated Mfg Date : " + newcreationDate + " & Exp Date : " + newDueDate + "<br>");
                                } else {
                                    //System.out.println("Location Batch Document Mapping : Mfg Date & Exp Date is not updated \n");
                                    //out.println("Location Batch Document Mapping : Mfg Date & Exp Date is not updated.<br>");
                                }
                            }//Location Batch Document Mapping loop end
                            //********************************************************************
                        }//New Product Batch loop end
                        
                        //Update New Batch Serial & Serial Document Mapping Dates
                        if (isSerialForProduct) {

                            //**********************New Batch Serial******************************
                            queryToGetStockMovements = "SELECT id, exptodate, expfromdate FROM newbatchserial WHERE ";
                            if(!StringUtil.isNullOrEmpty(productBatchId)) {
                                queryToGetStockMovements += "batch = '" + productBatchId + "' and ";
                            }
                            
                            queryToGetStockMovements += "product = '"+ productId + "' and isopening = 'T' and transactiontype = 28 and ispurchase = 'T'";
                            innerPst3 = conn.prepareStatement(queryToGetStockMovements);
                            innerRst3 = innerPst3.executeQuery();
                            while (innerRst3.next()) {
                                String serialId = innerRst3.getString("id") != null ? innerRst3.getString("id") : null;
                                creationdate = innerRst3.getObject("expfromdate") != null ? (java.util.Date) innerRst3.getObject("expfromdate") : null;
                                duedate = innerRst3.getObject("exptodate") != null ? (java.util.Date) innerRst3.getObject("exptodate") : null;

                                //Convert Date from Server's timezone to Admin's timezone
                                String newcreationDate = null, newDueDate = null;
                                if (creationdate != null) {
                                    newcreationDate = "'" + df.format(creationdate) + "'";
                                }//creationdate
                                if (duedate != null) {
                                    newDueDate = "'" + df.format(duedate) + "'";
                                }//duedate

                                String updatePReqDate = "UPDATE newbatchserial SET expfromdate=" + newcreationDate + ", exptodate=" + newDueDate + " WHERE id='" + serialId + "'";
                                pstinner = null;
                                pstinner = conn.prepareStatement(updatePReqDate);
                                int updatecount = pstinner.executeUpdate();
                                if (updatecount > 0) {
                                    //System.out.println("Updated ExpFrom Date : " + newcreationDate + " & ExpTo Date : " +newDueDate+ "\n");
                                    //out.println("Updated ExpFrom Date : " + newcreationDate + " & ExpTo Date : " + newDueDate + "<br>");
                                } else {
                                    //System.out.println("ExpFrom Date & ExpTo Date is not updated \n");
                                    //out.println("ExpFrom Date & ExpTo Date is not updated.<br>");
                                }

                                //**********************Serial Document Mapping***********************
                                queryToGetStockMovements = "SELECT id, exptodate, expfromdate FROM serialdocumentmapping WHERE serialid = '"+ serialId + "' and documentid = '"+ productId + "' and transactiontype = 28";
                                innerMostPst3 = conn.prepareStatement(queryToGetStockMovements);
                                innerMostRst3 = innerMostPst3.executeQuery();
                                while (rst3.next()) {
                                    String serialDocumentId = innerMostRst3.getString("id") != null ? innerMostRst3.getString("id") : null;
                                    creationdate = innerMostRst3.getObject("expfromdate") != null ? (java.util.Date) innerMostRst3.getObject("expfromdate") : null;
                                    duedate = innerMostRst3.getObject("exptodate") != null ? (java.util.Date) innerMostRst3.getObject("exptodate") : null;

                                    //Convert Date from Server's timezone to Admin's timezone
                                    if (creationdate != null) {
                                        newcreationDate = "'" + df.format(creationdate) + "'";
                                    }//creationdate
                                    if (duedate != null) {
                                        newDueDate = "'" + df.format(duedate) + "'";
                                    }//duedate

                                    updatePReqDate = "UPDATE serialdocumentmapping SET expfromdate=" + newcreationDate + ", exptodate=" + newDueDate + " WHERE id='" + serialDocumentId + "'";
                                    pstinner = null;
                                    pstinner = conn.prepareStatement(updatePReqDate);
                                    updatecount = pstinner.executeUpdate();
                                    if (updatecount > 0) {
                                        //System.out.println("Serial Document Mapping : Updated ExpFrom Date : " + newcreationDate + " & ExpTo Date : " +newDueDate+ "\n");
                                        //out.println("Serial Document Mapping : Updated ExpFrom Date : " + newcreationDate + " & ExpTo Date : " + newDueDate + "<br>");
                                    } else {
                                        //System.out.println("Serial Document Mapping : ExpFrom Date & ExpTo Date is not updated \n");
                                        //out.println("Serial Document Mapping : ExpFrom Date & ExpTo Date is not updated.<br>");
                                    }
                                }//Serial Document Mapping loop end
                                //********************************************************************
                            }//New Batch Serial loop end
                            //********************************************************************
                        }//end if serial for product
                    }//end if of warehouse or location or batchforproduct
                   
                    //Update Inventory Dates
                    String queryToGetStockMovements = "SELECT id, updatedate FROM inventory WHERE product = '"+ productId + "' and newinv = 'T' and carryin = 'T' and defective = 'F' and isupdated=0";
                    innerPst3 = conn.prepareStatement(queryToGetStockMovements);
                    innerRst3 = innerPst3.executeQuery();
                    while (innerRst3.next()) {
                        String stockMovementId = innerRst3.getString("id") != null ? innerRst3.getString("id") : null;
                        creationdate = innerRst3.getObject("updatedate") != null ? (java.util.Date) innerRst3.getObject("updatedate") : null;

                        //Convert Date from Server's timezone to Admin's timezone
                        String newcreationDate = null;
                        if (creationdate != null) {
//                            String convert1 = serverTzDf.format(creationdate);                            
//                            newcreationDate = "'" + df.format(constantDf.parse(convert1)) + "'";
                            newcreationDate = "'" + df.format(creationdate) + "'";
                        }//creationdate

                        String updatePReqDate = "UPDATE inventory SET updatedate=" + newcreationDate + ", isupdated=1 WHERE id='" + stockMovementId + "'";
                        pstinner = null;
                        pstinner = conn.prepareStatement(updatePReqDate);
                        int updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            //System.out.println("Inventory : Updated UpdatedDate : " + newcreationDate + "\n");
                            //out.println("Inventory : Updated UpdatedDate : " + newcreationDate + "<br>");
                        } else {
                            //System.out.println("Inventory : UpdatedDate is not updated \n");
                            //out.println("Inventory : UpdatedDate is not updated.<br>");
                        }
                    }//Inventory loop end
                    out.println("<br>**********************</b><br>");
                }//products loop end
                
                //****************************** Product Assembly **********************************
                //Update Assembly Inventory Dates
                System.out.println("Products Build Assembly Module Started...\n");
                out.println("<b>Subdomain '" +cmpSubdomain+ "' Products Build Assembly Module Started...</b><br>");
                String queryToGetStockMovements = "SELECT id, entrydate, createdon FROM productbuild WHERE company = '"+ companyid + "'";
                pst3 = conn.prepareStatement(queryToGetStockMovements);
                rst3 = pst3.executeQuery();
                Date productBuildDate = null;
                while (rst3.next()) {
                    String productBuildId = rst3.getString("id") != null ? rst3.getString("id") : null;
                    creationdate = rst3.getObject("entrydate") != null ? (java.util.Date) rst3.getObject("entrydate") : null;
                    productBuildDate = creationdate;
                    duedate = rst3.getObject("createdon") != null ? (java.util.Date) rst3.getObject("createdon") : null;

                    //Convert Date from Server's timezone to Admin's timezone
                    String newcreationDate = null, newDueDate = null;
                    if (creationdate != null) {
                        newcreationDate = "'" + df.format(creationdate) + "'";
                    }//creationdate
                    if (duedate != null) {
                        String convert1 = serverTzDf.format(duedate);
                        newDueDate = "'" + df.format(constantDf.parse(convert1)) + "'";
                    }//duedate

                    String updatePReqDate = "UPDATE productbuild SET entrydate=" + newcreationDate + ", createdon = " + newDueDate + " WHERE id='" + productBuildId + "'";
                    pstinner = null;
                    pstinner = conn.prepareStatement(updatePReqDate);
                    int updatecount = pstinner.executeUpdate();
                    if (updatecount > 0) {
                        //System.out.println("Build Assembly : Updated Entry Date : " + newcreationDate + " & Created On : " + newDueDate + "\n");
                        //out.println("Build Assembly : Updated Entry Date : " + newcreationDate + " & Created On : " + newDueDate + "<br>");
                    } else {
                        //System.out.println("Build Assembly : Entry Date & CreatedOn Date is not updated \n");
                        //out.println("Build Assembly : Entry Date & CreatedOn Date is not updated.<br>");
                    }
                    
                    //Update Inventory Dates
                    String queryToGetBuildInventory = "SELECT id, updatedate FROM inventory WHERE product = '"+ productBuildId + "' and newinv = 'F' and carryin = 'T' and defective = 'F' and isupdated=0";
                    pst4 = conn.prepareStatement(queryToGetBuildInventory);
                    rst4 = pst4.executeQuery();
                    while (rst4.next()) {
                        String inventoryId = rst4.getString("id") != null ? rst4.getString("id") : null;
                        creationdate = rst4.getObject("updatedate") != null ? (java.util.Date) rst4.getObject("updatedate") : null;

                        //Convert Date from Server's timezone to Admin's timezone
                        if (creationdate != null) {
                            String convert1 = serverTzDf.format(creationdate);
                            newcreationDate = "'" + df.format(constantDf.parse(convert1)) + "'";
                        }//creationdate

                        updatePReqDate = "UPDATE inventory SET updatedate=" + newcreationDate + ", isupdated=1 WHERE id='" + inventoryId + "'";
                        pstinner = null;
                        pstinner = conn.prepareStatement(updatePReqDate);
                        updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            //System.out.println("Inventory : Updated UpdatedDate : " + newcreationDate + "\n");
                            //out.println("Inventory : Updated UpdatedDate : " + newcreationDate + "<br>");
                        } else {
                            //System.out.println("Inventory : UpdatedDate is not updated \n");
                            //out.println("Inventory : UpdatedDate is not updated.<br>");
                        }
                    }//Assembly Inventory loop end

                    //Update Assembly Stock Movement Dates
                    queryToGetStockMovements = "SELECT id, transaction_date, createdon FROM in_stockmovement WHERE product = '"+ productBuildId + "' and modulerefid = '" + productBuildId + "' and transaction_type = " + 1 + " and transaction_module = " + 10;
                    innerPst3 = conn.prepareStatement(queryToGetStockMovements);
                    innerRst3 = innerPst3.executeQuery();
                    while (innerRst3.next()) {
                        String stockMovementId = innerRst3.getString("id") != null ? innerRst3.getString("id") : null;
                        creationdate = innerRst3.getObject("createdon") != null ? (java.util.Date) innerRst3.getObject("createdon") : null;
                        duedate = innerRst3.getObject("transaction_date") != null ? (java.util.Date) innerRst3.getObject("transaction_date") : null;

                        //Convert Date from Server's timezone to Admin's timezone
                        if (creationdate != null) {
                            String convert1 = serverTzDf.format(creationdate);
                            newcreationDate = "'" + df.format(constantDf.parse(convert1)) + "'";
                        }//creationdate
                        if (duedate != null) {
                            String convert1 = serverTzDf.format(duedate);
                            newDueDate = "'" + df.format(constantDf.parse(convert1)) + "'";
                        }//duedate

                        updatePReqDate = "UPDATE in_stockmovement SET createdon=" + newcreationDate + ", transaction_date=" + newDueDate + " WHERE id='" + stockMovementId + "'";
                        pstinner = null;
                        pstinner = conn.prepareStatement(updatePReqDate);
                        updatecount = pstinner.executeUpdate();
                        if (updatecount > 0) {
                            //System.out.println("Stock Movement : Updated Creation Date : " + newcreationDate + " & Transaction Date : " +newDueDate+ "\n");
                            //out.println("Stock Movement : Updated Creation Date : " + newcreationDate + " & Transaction Date : " + newDueDate + "<br>");
                        } else {
                            //System.out.println("Stock Movement : Creation Date & Transaction Date is not updated \n");
                            //out.println("Stock Movement : Creation Date & Transaction Date is not updated.<br>");
                        }
                    }//Assembly Stock Movement loop end
                    
                    //Update Assembly Sub-Products Inventory Dates
                    queryToGetBuildInventory = "SELECT aproduct FROM pbdetails WHERE build = '"+ productBuildId + "'";
                    pst4 = conn.prepareStatement(queryToGetBuildInventory);
                    rst4 = pst4.executeQuery();
                    while (rst4.next()) {
                        String aProductId = rst4.getString("aproduct") != null ? rst4.getString("aproduct") : null;
                        
                        //Update Inventory Dates
                        queryToGetBuildInventory = "SELECT id, updatedate FROM inventory WHERE product = '"+ aProductId + "' and updatedate = '" + productBuildDate + "' and carryin='F' and defective='F' and newinv='F' and isupdated=0";
                        innerMostPst3 = conn.prepareStatement(queryToGetBuildInventory);
                        innerMostRst3 = innerMostPst3.executeQuery();
                        while (innerMostRst3.next()) {
                            String inventoryId = innerMostRst3.getString("id") != null ? innerMostRst3.getString("id") : null;
                            creationdate = innerMostRst3.getObject("updatedate") != null ? (java.util.Date) innerMostRst3.getObject("updatedate") : null;

                            //Convert Date from Server's timezone to Admin's timezone
                            if (creationdate != null) {
                                String convert1 = serverTzDf.format(creationdate);
                                newcreationDate = "'" + df.format(constantDf.parse(convert1)) + "'";
                            }//creationdate

                            updatePReqDate = "UPDATE inventory SET updatedate=" + newcreationDate + ", isupdated=1 WHERE id='" + inventoryId + "'";
                            pstinner = null;
                            pstinner = conn.prepareStatement(updatePReqDate);
                            updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Inventory : Updated UpdatedDate : " + newcreationDate + "\n");
                                //out.println("Inventory : Updated UpdatedDate : " + newcreationDate + "<br>");
                            } else {
                                //System.out.println("Inventory : UpdatedDate is not updated \n");
                                //out.println("Inventory : UpdatedDate is not updated.<br>");
                            }
                        }//Assembly Sub-Products Inventory loop end
                        
                        //Update Assembly Sub-Product Stock Movement Dates
                        queryToGetStockMovements = "SELECT id, transaction_date, createdon FROM in_stockmovement WHERE modulerefid = '" + productBuildId + "' and product = '"+ aProductId + "' and transaction_type = " + 2 + " and transaction_module = " + 10;
                        innerPst3 = conn.prepareStatement(queryToGetStockMovements);
                        innerRst3 = innerPst3.executeQuery();
                        while (innerRst3.next()) {
                            String stockMovementId = innerRst3.getString("id") != null ? innerRst3.getString("id") : null;
                            creationdate = innerRst3.getObject("createdon") != null ? (java.util.Date) innerRst3.getObject("createdon") : null;
                            duedate = innerRst3.getObject("transaction_date") != null ? (java.util.Date) innerRst3.getObject("transaction_date") : null;

                            //Convert Date from Server's timezone to Admin's timezone
                            if (creationdate != null) {
                                String convert1 = serverTzDf.format(creationdate);
                                newcreationDate = "'" + df.format(constantDf.parse(convert1)) + "'";
                            }//creationdate
                            if (duedate != null) {
                                String convert1 = serverTzDf.format(duedate);
                                newDueDate = "'" + df.format(constantDf.parse(convert1)) + "'";
                            }//duedate

                            updatePReqDate = "UPDATE in_stockmovement SET createdon=" + newcreationDate + ", transaction_date=" + newDueDate + " WHERE id='" + stockMovementId + "'";
                            pstinner = null;
                            pstinner = conn.prepareStatement(updatePReqDate);
                            updatecount = pstinner.executeUpdate();
                            if (updatecount > 0) {
                                //System.out.println("Stock Movement : Updated Creation Date : " + newcreationDate + " & Transaction Date : " +newDueDate+ "\n");
                                //out.println("Stock Movement : Updated Creation Date : " + newcreationDate + " & Transaction Date : " + newDueDate + "<br>");
                            } else {
                                //System.out.println("Stock Movement : Creation Date & Transaction Date is not updated \n");
                                //out.println("Stock Movement : Creation Date & Transaction Date is not updated.<br>");
                            }
                        }//Assembly Stock Movement loop end
                    }//Assembly Sub-Products loop end
                }//Product Assembly loop end
                //******************************************************************************************************
                out.println("<center><b>=================================================================================</b></center><br><br>");
                df = null;
            }//company id loop end
//        }//server timezone loop end
        System.out.println("Complete Products Module Ended...\n");
        out.println("<b>Complete Products Module Ended...<b><br>");
        
        /*//Modify data type of transaction_date columns as Date instead of timestamp
        String modifyDate = "ALTER TABLE in_stockmovement MODIFY COLUMN transaction_date DATE DEFAULT NULL";
        try {
            modifypst = null;
            modifypst = conn.prepareStatement(modifyDate);
            modifypst.execute();
            System.out.println("Data type of transaction_date columns is altered to 'Date'.\n");
            out.println("Data type of transaction_date column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Transaction_date columns data type not modified.<br/>");
        }
        
        //Modify data type of mfgdate, expdate columns as Date instead of timestamp
        modifyDate = "ALTER TABLE newproductbatch MODIFY COLUMN mfgdate DATE DEFAULT NULL, MODIFY COLUMN expdate DATE DEFAULT NULL";
        try {
            modifypst = null;
            modifypst = conn.prepareStatement(modifyDate);
            modifypst.execute();
            System.out.println("Data type of mfgdate, expdate columns is altered to 'Date'.\n");
            out.println("Data type of mfgdate, expdate column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Mfgdate, Expdate columns data type not modified.<br/>");
        }
        
        //Modify data type of mfgdate, expdate columns as Date instead of timestamp
        modifyDate = "ALTER TABLE locationbatchdocumentmapping MODIFY COLUMN mfgdate DATE DEFAULT NULL, MODIFY COLUMN expdate DATE DEFAULT NULL";
        try {
            modifypst = null;
            modifypst = conn.prepareStatement(modifyDate);
            modifypst.execute();
            System.out.println("Data type of mfgdate, expdate columns is altered to 'Date'.\n");
            out.println("Data type of mfgdate, expdate column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Mfgdate, Expdate columns data type not modified.<br/>");
        }
        
        //Modify data type of expfromdate, exptodate columns as Date instead of timestamp
        modifyDate = "ALTER TABLE newbatchserial MODIFY COLUMN expfromdate DATE DEFAULT NULL, MODIFY COLUMN exptodate DATE DEFAULT NULL";
        try {
            modifypst = null;
            modifypst = conn.prepareStatement(modifyDate);
            modifypst.execute();
            System.out.println("Data type of expfromdate, exptodate columns is altered to 'Date'.\n");
            out.println("Data type of expfromdate, exptodate column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Expfromdate, Exptodate columns data type not modified.<br/>");
        }
        
        //Modify data type of expfromdate, exptodate columns as Date instead of timestamp
        modifyDate = "ALTER TABLE serialdocumentmapping MODIFY COLUMN expfromdate DATE DEFAULT NULL, MODIFY COLUMN exptodate DATE DEFAULT NULL";
        try {
            modifypst = null;
            modifypst = conn.prepareStatement(modifyDate);
            modifypst.execute();
            System.out.println("Data type of expfromdate, exptodate columns is altered to 'Date'.\n");
            out.println("Data type of expfromdate, exptodate column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Expfromdate, Exptodate columns data type not modified.<br/>");
        }
        
        //Modify data type of updatedate columns as Date instead of timestamp
        modifyDate = "ALTER TABLE inventory MODIFY COLUMN updatedate DATE DEFAULT NULL";
        try {
            modifypst = null;
            modifypst = conn.prepareStatement(modifyDate);
            modifypst.execute();
            System.out.println("Data type of updatedate columns is altered to 'Date'.\n");
            out.println("Data type of updatedate column is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Updatedate columns data type not modified.<br/>");
        }
        
        //Modify data type of entrydate, createdon columns as Date instead of timestamp
        modifyDate = "ALTER TABLE productbuild MODIFY COLUMN createdon DATE DEFAULT NULL, MODIFY COLUMN entrydate DATE DEFAULT NULL";
        try {
            modifypst = null;
            modifypst = conn.prepareStatement(modifyDate);
            modifypst.execute();
            System.out.println("Data type of entrydate, createdon columns is altered to 'Date'.\n");
            out.println("Data type of entrydate, createdon columns is altered to 'Date'.<br/>");
        } catch (SQLException se) {
            System.out.println(se.getMessage());
            out.println("Entrydate, Createdon columns data type not modified.<br/>");
        }*/
    } catch (Exception ex) {
        conn.rollback();
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
        out.print("Dates are not updated.");
    } finally {
        if (conn != null) {
            try {
                conn.commit();
                conn.close();
                out.println("Connection Closed....<br/>");
                //Execution Ended :
                out.println("<br><br>Execution Ended @ "+new java.util.Date()+"<br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally
%>