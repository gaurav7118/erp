
<%@page import="com.krawler.utils.json.base.JSONException"%>
<%@page import="java.text.ParseException"%>
<%@page import="com.krawler.esp.utils.ConfigReader"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.utils.json.base.JSONArray"%>
<%@page import="com.krawler.common.util.Constants"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%--<%@page import="java.sql.Date"%>--%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%
    Connection con = null;
    try {
        //SCRIPT URL :htttp://localhost:8080/Accounting/UpdateDefaultValueOfEntityToOpeningTransactions.jsp?serverip=192.168.0.135&dbname=custom&username=root&subdomain=vinodtest&password=

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String reqSubdomain = request.getParameter("subdomain");
        //String subdomain = "";//request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username)) {//|| StringUtil.isNullOrEmpty(password)
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        String errorOccured="";
        String driver = "com.mysql.jdbc.Driver";
        String query = "",fieldparamQuery="",fieldComboQuery="", companyid = "",updateOpeningQuery="",baseTableSelectQuery="",updateBaseTableQuery="",newQuery="";
        String data="",openingTable="",openingtableid="",basetable="",basetableid="",customDataRefCol="";
        String subdomain=""; String country=""; String setUpDone="";
        boolean isError = false;                   
        Class.forName(driver).newInstance();        
        DateFormat df = new SimpleDateFormat("MMMM d, yyyy");
        DateFormat df1 = new SimpleDateFormat("MMMM d, yyyy");
        //Execution Started :
        out.println("<br><br>Execution Started @ " + new java.util.Date() + "<br><br>");

        con = DriverManager.getConnection(connectString, username, password);
        PreparedStatement cmpanyPstn=null,fieldprmPstn=null,fieldComboPstn=null,updateOpeningTablePstn=null,baseTablePstn=null ,openinInsertPstn=null,updateBaseTablePstn=null,newQueryPstn;
        ResultSet cmpanyRst=null,fieldprmRslt=null,fieldComboRst=null,baseTableRst=null , openinRst=null,newQueryRst=null;

        File dateFile = new File(ConfigReader.getinstance().get("DocStorePath0") + "DateFile.txt");
        dateFile.createNewFile();
        FileOutputStream is = new FileOutputStream(dateFile);
        OutputStreamWriter osw = new OutputStreamWriter(is);
        Writer w = new BufferedWriter(osw);
        String fileContent = "";
        if (!StringUtil.isNullOrEmpty(reqSubdomain)) {
                query = "select c.companyid,c.country,c.subdomain,cp.setupdone from company c INNER JOIN compaccpreferences cp ON  c.companyid=cp.id  where subdomain='"+reqSubdomain+"'";
        }else{
                query = "select c.companyid,c.country,c.subdomain,cp.setupdone from company c inner join country cr on cr.id=c.country INNER JOIN compaccpreferences cp on c.companyid=cp.id  where cr.id in ('105','244')";
        }
        cmpanyPstn = con.prepareStatement(query);
        cmpanyRst = cmpanyPstn.executeQuery();  
        
                while (cmpanyRst.next()) {
                        try {
                            setUpDone = cmpanyRst.getString("setupdone");
                            subdomain = cmpanyRst.getString("subdomain");
                            country = cmpanyRst.getString("country");
                            companyid =cmpanyRst.getString("companyid");//04575a0c-b33c-11e3-986d-001e670e1128
                            out.println("===========Execution Started For "+subdomain+"==========="+ "<br/>");
                            if (setUpDone.equalsIgnoreCase("T")) {

                                    fieldparamQuery = "select id,moduleid,colnum from fieldparams where companyid='" + companyid + "' and fieldlabel='Entity' ";
                                    fieldprmPstn = con.prepareStatement(fieldparamQuery);
                                    fieldprmRslt = fieldprmPstn.executeQuery();
                                    while (fieldprmRslt.next()) {
                                        String moduleid = fieldprmRslt.getString("moduleid");
                                        String colnum = "col" + fieldprmRslt.getString("colnum");
                                        String fieldParamId = fieldprmRslt.getString("id");

                                        fieldComboQuery = "select id from fieldcombodata where fieldid='" + fieldParamId + "' ";
                                        fieldComboPstn = con.prepareStatement(fieldComboQuery);
                                        fieldComboRst = fieldComboPstn.executeQuery();
                                        while (fieldComboRst.next()) {
                                            data = fieldComboRst.getString("id");
                                        }
                                        HashMap<String,String> tablesInfo=new HashMap<String,String>();
                                        tablesInfo = getTableName(Integer.parseInt(moduleid));
                                        if ((!tablesInfo.isEmpty()) && !StringUtil.isNullOrEmpty(data)) {

                                            openingTable = String.valueOf(tablesInfo.get("openingtable"));
                                            openingtableid = String.valueOf(tablesInfo.get("openingtableid"));
                                            basetable = String.valueOf(tablesInfo.get("basetable"));
                                            basetableid = String.valueOf(tablesInfo.get("basetableid"));
                                            customDataRefCol = String.valueOf(tablesInfo.get("customrefcol"));

//                                            //========Existing Records in opening table are Upadated==========                                            
//                                            updateOpeningQuery = "update " + openingTable + " set " + colnum + "= '" + data + "' where company='" + companyid + "' and moduleid='" + moduleid + "'";
//                                            updateOpeningTablePstn = con.prepareStatement(updateOpeningQuery);
//                                            int rows = 0;
//                                            rows=updateOpeningTablePstn.executeUpdate();
//                                            
//                                            //=================================================================
//                                            baseTableSelectQuery="select "+basetableid+",company from "+basetable+" where "+customDataRefCol+" is null and "+basetableid+" in ("+query1+") and company ='"+companyid+"'";
                                            baseTableSelectQuery = "select " + basetableid + ",company from " + basetable + " where company ='" + companyid + "'";
                                            baseTablePstn = con.prepareStatement(baseTableSelectQuery);
                                            baseTableRst = baseTablePstn.executeQuery();
                                            while (baseTableRst.next()) {
                                                try {
                                                    //===========inserting entries in opening custom tables...
                                                    String key = baseTableRst.getString(basetableid);
                                                    newQuery = "select " + openingtableid + " from " + openingTable + " where " + openingtableid + " = '" + key + "' ";
                                                    newQueryPstn = con.prepareStatement(newQuery);
                                                    newQueryRst = newQueryPstn.executeQuery();
                                                    if (!newQueryRst.next()) {

                                                        String insertQuery = "insert into " + openingTable + " (" + openingtableid + ",company,moduleId," + colnum + ") values ('" + key + "','" + companyid + "'," + moduleid + ",'" + data + "')";
                                                        openinInsertPstn = con.prepareStatement(insertQuery);
                                                        openinInsertPstn.execute();
                                                        //=========Updating customdatareferences in base tables================
                                                        updateBaseTableQuery = "update " + basetable + " set " + customDataRefCol + "=" + basetableid + " where " + customDataRefCol + " is null and " + basetableid + "='" + key + "'";
                                                        updateBaseTablePstn = con.prepareStatement(updateBaseTableQuery);
                                                        int row2 = 0;
                                                        row2 = updateBaseTablePstn.executeUpdate();
                                                    } else {
                                                        do {
                                                            updateOpeningQuery = "update " + openingTable + " set " + colnum + "= '" + data + "' where " + openingtableid + " = '" + key + "'";
                                                            updateOpeningTablePstn = con.prepareStatement(updateOpeningQuery);
                                                            int rows = 0;
                                                            rows = updateOpeningTablePstn.executeUpdate();
                                                        } while (newQueryRst.next());
                                                    }
                                                } catch (Exception e) {
                                                    w.write(" Error Ocurred For " + openingTable + " " + e.toString());
                                                    out.println(" Error Ocurred For " + openingTable + " " + e.toString());
                                                    continue;
                                                }
                                            }
                                        }
                                       
                                    }
                                }else{
                                    w.write("No Set up done for " + subdomain+ " performed no action."+ "<br/>");
                                    out.println("No Set up done for " + subdomain+ " so performed no action."+ "<br/>");
                            }
                            out.println("===========Execution Finished For "+subdomain+"==========="+"<br/>");
                        } catch (Exception e) {
                            w.write("Error occured for "+subdomain+" Error : "+e.toString());
                            out.print("Error occured for "+subdomain+" Error : "+e.toString());
                            
                        }
                    }
        
        w.close();

        System.out.println("Script Executed Successfully...\n");
        out.println("<b><center>Script Executed Successfully...</center><b><br>");
    } catch (Exception ex) {
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
    } finally {
        if (con != null) {
            try {
                con.close();
                out.println("Connection Closed....<br/>");
                //Execution Ended :
                out.println("<br><br>Execution Ended @ " + new java.util.Date() + "<br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally        
%>
<%!    
       public HashMap<String,String> getTableName(int moduleId) {    //(2,6,10,12,14,16)

        String openingTableName = "";
//        ArrayList<String> tablesInfo=new ArrayList<String>();
        HashMap<String,String> tablesInfo=new HashMap<String,String>();
        switch (moduleId) {   
                    case 6:     //VI  //Vendor Invoice
                    case 39:     //VI  //Asset Purchase Invoice
                    case 58:   //Consignment Purchase Invoice
                        tablesInfo.put("openingtable","openingbalancevendorinvoicecustomdata");
                        tablesInfo.put("openingtableid","openingbalancevendorinvoiceid");
                        tablesInfo.put("basetable","goodsreceipt");
                        tablesInfo.put("basetableid","id");
                        tablesInfo.put("customrefcol","accopeningbalancevendorinvoicecustomdataref");
                        break;
                    case 2:     //CI  //Customer Invoices
                    case 38:     //CI  //Asset Disposal Invoice
                    case 52:     //Consignment Stock Sales Invoice                        
                        tablesInfo.put("openingtable","openingbalanceinvoicecustomdata");
                        tablesInfo.put("openingtableid","openingbalanceinvoiceid");
                        tablesInfo.put("basetable","invoice");
                        tablesInfo.put("basetableid","id");
                        tablesInfo.put("customrefcol","accopeningbalanceinvoicecustomdataref");
                        break;
                    case 14:    //MP   //Payment                        
                        tablesInfo.put("openingtable","openingbalancemakepaymentcustomdata");
                        tablesInfo.put("openingtableid","openingbalancemakepaymentid");
                        tablesInfo.put("basetable","payment");
                        tablesInfo.put("basetableid","id");
                        tablesInfo.put("customrefcol","accopeningbalancemakepaymentcustomdataref");                        
                        break;
                    case 16:        //RP  //Receipt                        
                        tablesInfo.put("openingtable","openingbalancereceiptcustomdata");
                        tablesInfo.put("openingtableid","openingbalancereceiptid");
                        tablesInfo.put("basetable","receipt");
                        tablesInfo.put("basetableid","id");
                        tablesInfo.put("customrefcol","accopeningbalancereceiptcustomdataref");                        
                        break;
                    case 10:        //Debit Note                                                
                        tablesInfo.put("openingtable","openingbalancedebitnotecustomdata");
                        tablesInfo.put("openingtableid","openingbalancedebitnoteid");
                        tablesInfo.put("basetable","debitnote");
                        tablesInfo.put("basetableid","id");
                        tablesInfo.put("customrefcol","accopeningbalancedebitnotecustomdataref");                        
                        break;
                    case 12:        //Credit Note
                    case 93:  //Lease Invoice                        
                        tablesInfo.put("openingtable","openingbalancecreditnotecustomdata");
                        tablesInfo.put("openingtableid","openingbalancecreditnoteid");
                        tablesInfo.put("basetable","creditnote");
                        tablesInfo.put("basetableid","id");
                        tablesInfo.put("customrefcol","accopeningbalancecreditnotecustomdataref");                        
                        break;
                    case 30:   //Product //Asset Group
                        tablesInfo.put("openingtable","accproductcustomdata");
                        tablesInfo.put("openingtableid","productId");
                        tablesInfo.put("basetable","product");
                        tablesInfo.put("basetableid","id");
                        tablesInfo.put("customrefcol","accproductcustomdataref");                        
                        break;
                    case 26:    // Vendor
                        tablesInfo.put("openingtable","vendorcustomdata");
                        tablesInfo.put("openingtableid","vendorId");
                        tablesInfo.put("basetable","vendor");
                        tablesInfo.put("basetableid","id");
                        tablesInfo.put("customrefcol","accvendorcustomdataref");                        
                        break;
                    case 25:  //Accounts
                        tablesInfo.put("openingtable","customercustomdata");
                        tablesInfo.put("openingtableid","customerId");
                        tablesInfo.put("basetable","customer");
                        tablesInfo.put("basetableid","id");
                        tablesInfo.put("customrefcol","acccustomercustomdataref");                        
                        break;
                    case 34:  //Chart of Account
                        tablesInfo.put("openingtable","accountcustomdata");
                        tablesInfo.put("openingtableid","accountId");
                        tablesInfo.put("basetable","account");
                        tablesInfo.put("basetableid","id");
                        tablesInfo.put("customrefcol","accaccountcustomdataref");                        
                        break;                                                                                                                                
                                        
        }

        return tablesInfo;
    }

%>