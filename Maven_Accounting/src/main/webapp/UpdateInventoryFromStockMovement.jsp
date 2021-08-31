<%@page import="com.krawler.inventory.model.stockmovement.StockMovement"%>
<%@page import="com.krawler.inventory.model.stockmovement.TransactionModule"%>
<%@page import="java.text.Format"%>
<%@page import="com.ibm.icu.text.SimpleDateFormat"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<!--%@page contentType="text/html" pageEncoding="UTF-8"%>-->
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Date"%>




<%
    Connection conn = null;
        try {

//            String serverip = "localhost";
//            String port = "3306";
//            String dbName = "accounting";
//            String userName = "root";//"krawlersqladmin";
//            String password = ""; //"krawler"
//            String subdomain = "erp29";
            String serverip = request.getParameter("serverip");//"192.168.0.208";                            
            String port = request.getParameter("port");//"3306";
            String dbName = request.getParameter("dbname");//"newstaging";
            String userName = request.getParameter("username");//"krawlersqladmin";
            String password = request.getParameter("password"); //"krawler"
            String subdomain = request.getParameter("subdomain");

            if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName)) {
                throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
            }
            String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            String driver = "com.mysql.jdbc.Driver";

            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectString, userName, password);
            String query = "select companyid,companyname FROM company ";
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                query += " where subdomain= ?";
            }
            PreparedStatement stmt = conn.prepareStatement(query);
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                stmt.setString(1, subdomain);
            }
            ResultSet rs = stmt.executeQuery();
            int totalCompanyUpdationCnt = 0;
            int totalInventoryUpdationCnt = 0;
            int totalSM = 0;
            
            while (rs.next()) {

                String companyId = rs.getString("companyid");
                totalCompanyUpdationCnt++;
                out.println(companyId);
                String selStock = "SELECT SUM(quantity)as quantity,product,transactionno,"
                        + "transaction_type,transaction_module,modulerefdetailid,modulerefid,remark,transaction_date "
                        + "FROM  in_stockmovement WHERE company=? GROUP BY product,transactionno,transaction_module,modulerefdetailid";
                
                PreparedStatement stmtp = conn.prepareStatement(selStock);
                stmtp.setObject(1, companyId);
                ResultSet rsp = stmtp.executeQuery();
                while (rsp.next()) {
                    
                    totalSM++;
                    double quantity = rsp.getDouble("quantity");
                    int transactionType = rsp.getInt("transaction_type");
		    int transactionModule = rsp.getInt("transaction_module");
		    String transactionno = rsp.getString("transactionno");
		    String modulerefdetailid = rsp.getString("modulerefdetailid");
		    String modulerefid = rsp.getString("modulerefid");
		    String strDate = rsp.getString("transaction_date");
		    String product = rsp.getString("product");
		    String remark = rsp.getString("remark");
                    String transactiondate="";
                    
                    try{
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
                        Date date = format.parse(strDate);
                        transactiondate= format.format(date);
                    }catch(Exception e){
                        out.println("Date Format Exception.");
                        e.printStackTrace();
                    }
                    
                    if(transactionType == 0 && transactionModule == 10 ){
                      
                        
                            String insertInventoryQuery="insert into inventory "
                                    + "(id,quantity,carryin,defective,product,company,newinv,actquantity,invrecord,isopening,"
                                    + "description,baseuomquantity,baseuomrate,deleteflag,updatedate) "
                                    + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                            PreparedStatement insertInventory = conn.prepareStatement(insertInventoryQuery);
                            String inventory_UUID = modulerefdetailid;
                            insertInventory.setString(1,inventory_UUID);
                            insertInventory.setDouble(2,quantity);
                            insertInventory.setString(3,"T");
                            insertInventory.setString(4,"F");
                            insertInventory.setString(5,product);
                            insertInventory.setString(6,companyId);
                            insertInventory.setString(7,"T");
                            insertInventory.setDouble(8,quantity);
                            insertInventory.setString(9,"T");
                            insertInventory.setString(10,"T");
                            insertInventory.setString(11,remark);
                            insertInventory.setDouble(12,quantity);
                            insertInventory.setDouble(13,1);
                            insertInventory.setString(14,"F");
                            insertInventory.setString(15,transactiondate);
                            insertInventory.execute();
                            
                            out.print("<br>-> Opening Created"); 
                            
                            totalInventoryUpdationCnt++;                        
                            out.println("<br>Srno:<b> "+ totalSM +"</b>  -> Product : <b>"+ product +"</b> -> DetailId:<b> "+modulerefdetailid+" </b> Module :<b>"+modulerefid+"</b> -> TNo :<b> "+transactionno+" </b>-> TransactionType :<b>"+transactionType+"</b> transaction module = " +transactionModule+"-> Remark:<b><u>"+remark+"</u></b>");
                            out.print("<br>-><b>"+insertInventory.toString()+"</b><br>");
                            insertInventory.close();
                            
		    }else{
                        
                        String insertInventoryQuery="insert into inventory "
                                + "(id,quantity,carryin,defective,product,company,newinv,actquantity,invrecord,isopening,"
                                + "description,baseuomquantity,baseuomrate,deleteflag,updatedate) "
                                + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
                        PreparedStatement insertInventory = conn.prepareStatement(insertInventoryQuery);
                        String inventory_UUID = modulerefdetailid;
                        insertInventory.setString(1, inventory_UUID);
                        insertInventory.setDouble(2, quantity);
                        insertInventory.setString(3, transactionType==2?"F":"T");
                        insertInventory.setString(4, "F");
                        insertInventory.setString(5, product);
                        insertInventory.setString(6, companyId);
                        insertInventory.setString(7, "F");
                        insertInventory.setDouble(8, quantity);
                        insertInventory.setString(9, "T");
                        insertInventory.setString(10,transactionType==0?"T":"F");
                        insertInventory.setString(11,remark);
                        insertInventory.setDouble(12,quantity);
                        insertInventory.setDouble(13,1);
                        insertInventory.setString(14,"F");
                        insertInventory.setString(15,transactiondate);
                        insertInventory.execute();
                        if(transactionType==2){out.print("<br>-> Out Created");}
                        else if(transactionType==1){out.print("<br>-> In Created");}
                        else {out.print("-> Opening Created");}
                        
                        totalInventoryUpdationCnt++;
                        out.println("<br>Srno:<b> "+ totalSM +"</b>  -> Product : <b>"+ product +"</b> -> DetailId:<b> "+modulerefdetailid+" </b> Module :<b>"+modulerefid+"</b> -> TNo :<b> "+transactionno+" </b>-> TransactionType :<b>"+transactionType+"</b> transaction module = " +transactionModule+"-> Remark:<b><u>"+remark+"</u></b>");
                        out.print("<br>-><b>"+insertInventory.toString()+"</b><br>");
                        insertInventory.close();

		    }

                }
                
                out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
                out.println("<br><br> Total Inventories updated are " + totalInventoryUpdationCnt);
                out.println("<br><br> Total SM are " + totalSM);
                out.println("<br><br> Time :  " + new java.util.Date().toString());
            }
        } catch (Exception e) {
            if (conn != null) {
//                 conn.rollback();
            }
            e.printStackTrace();
            out.print(e.toString());
        } finally {
            if (conn != null) {
                conn.close();
            }
        }

%>
