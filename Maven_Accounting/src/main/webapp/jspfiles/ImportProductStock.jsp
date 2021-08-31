<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


<%
    Connection conn = null;
    try {

        String serverip = "localhost";
        String port = "3306";
        String dbName = "accountingdiamondaviation";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";

        String driver = "com.mysql.jdbc.Driver";
        String userName = "krawlersqladmin";
        String password = "krawler";
        String companyId = "7e40b2e0-85b0-11e4-a6d6-001e670e1459";
        String firstLocname="",secondLocName="";
        String firstLocId="",secondLocId="";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
        FileInputStream invoice = new FileInputStream("/home/krawler/Products.CSV");
         BufferedReader in=new BufferedReader(new InputStreamReader(invoice));
//        DataInputStream in = new DataInputStream(invoice);
        int cnt = 0;
         String record = "";
        while ((record = in.readLine()) != null){
            if (cnt == 0 && record!="") {
                    String[] recarrName = record.split(",");
                     firstLocname +="'"+recarrName[2].trim()+"'";
                     secondLocName+="'"+recarrName[3].trim()+"'";
                       
                       //getting First location Id  ENS       
                String query1 = "select id from inventorylocation where company=? and name =" + firstLocname;
                    PreparedStatement stmt1 = conn.prepareStatement(query1);
                    stmt1.setObject(1, companyId);
                    ResultSet rs = stmt1.executeQuery();
                    if (rs.next()) {
                        firstLocId = rs.getString("id");
                    } else {
                        out.println("Location not fount for record row " + cnt);
                        continue;
                    }
                    //getting second location Id JYR
                String query2 = "select id from inventorylocation where company=? and name =" + secondLocName;
                    PreparedStatement stmt2 = conn.prepareStatement(query2);
                    stmt2.setObject(1, companyId);
                    ResultSet rs2 = stmt2.executeQuery();
                    if (rs2.next()) {
                        secondLocId = rs2.getString("id");
                    } else {
                        out.println("Location not fount for record row " + cnt);
                        continue;
                    }
                
                }
              if (cnt != 0  && !StringUtil.isNullOrEmpty(record)) {
                   
                String[] rec = record.split(",");
                String productId =rec[0].trim();
                String firstLocQuantity=rec[2].trim();
                String secondLocQuantity=rec[3].trim();
                
                String cntStr=String.valueOf(cnt + 3);
                String pid="",uomid="";
                String query3 = "select id,unitOfMeasure from product where productid=? and company=?";
                    PreparedStatement stmt3 = conn.prepareStatement(query3);
                    stmt3.setObject(1,productId);
                    stmt3.setObject(2,companyId);
                    ResultSet rs3 = stmt3.executeQuery();
                    if (rs3.next()) {
                        pid = rs3.getString("id");
                        uomid=rs3.getString("unitOfMeasure");
                    } 
                    
                    
                if(!StringUtil.isNullOrEmpty(pid)){
                    //set location option for product
                String query = "update product set islocationforproduct='T' where id=? and company=?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1,pid);
                stmt.setString(2,companyId );
                stmt.execute();
             //   generate entry for first location batch quantity
                 double firstLocqty = 0.0;
                    firstLocqty= Double.parseDouble((firstLocQuantity));
                     if (firstLocqty >= 0) {
                         String batchMapId = java.util.UUID.randomUUID().toString();
                         String querybatch = "insert into newproductbatch (id,quantity,quantitydue,location,transactiontype,product,isopening,ispurchase,company) values (?,?,?,?,28,?,'T','T',?)";
                         PreparedStatement stmtb = conn.prepareStatement(querybatch);
                         stmtb.setString(1, batchMapId);
                         stmtb.setDouble(2, firstLocqty);
                         stmtb.setDouble(3, firstLocqty);
                         stmtb.setString(4, firstLocId);
                         stmtb.setString(5, pid);
                         stmtb.setString(6, companyId);
                         stmtb.execute();
                         stmtb.close();

                         String locBatchdocId = java.util.UUID.randomUUID().toString();
                         String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                         PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                         stmtlbmap.setString(1, locBatchdocId);
                         stmtlbmap.setDouble(2, firstLocqty);
                         stmtlbmap.setString(3, pid);
                         stmtlbmap.setString(4, batchMapId);
                         stmtlbmap.execute();
                     }
                    double secondLocqty = 0.0;
                    secondLocqty = Double.parseDouble((secondLocQuantity));
                     if (secondLocqty >= 0) {
                         String batchMapId = java.util.UUID.randomUUID().toString();
                         String querybatch = "insert into newproductbatch (id,quantity,quantitydue,location,transactiontype,product,isopening,ispurchase,company) values (?,?,?,?,28,?,'T','T',?)";
                         PreparedStatement stmtb = conn.prepareStatement(querybatch);
                         stmtb.setString(1, batchMapId);
                         stmtb.setDouble(2, secondLocqty);
                         stmtb.setDouble(3, secondLocqty);
                         stmtb.setString(4, secondLocId);
                         stmtb.setString(5, pid);
                         stmtb.setString(6, companyId);
                         stmtb.execute();
                         stmtb.close();

                         String locBatchdocId = java.util.UUID.randomUUID().toString();
                         String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                         PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                         stmtlbmap.setString(1, locBatchdocId);
                         stmtlbmap.setDouble(2, secondLocqty);
                         stmtlbmap.setString(3, pid);
                         stmtlbmap.setString(4, batchMapId);
                         stmtlbmap.execute();
                         stmtlbmap.close();

                     }
                     String InvId = "";
                     double Invbaseuomquantity=0.0,Invquantity=0.0;
                     String queryinvid = "select id,quantity,baseuomquantity from inventory where company=? and product= ?";
                         PreparedStatement stmt4 = conn.prepareStatement(queryinvid);
                          stmt4.setObject(1, companyId);
                          stmt4.setObject(2, pid);
                         ResultSet rs4 = stmt4.executeQuery();
                         if (rs4.next()) {
                             InvId = rs4.getString("id");
                             Invquantity = rs4.getDouble("quantity");
                             Invbaseuomquantity = rs4.getDouble("baseuomquantity");
                             }
                         if(!StringUtil.isNullOrEmpty(InvId)){
                             PreparedStatement stmtquery = conn.prepareStatement(queryinvid);
                              String updatequery = "update inventory set quantity=?,baseuomquantity=?,carryin='T',defective='F',newinv='T',updatedate=now(),deleteflag='F' where id=?";
                                  stmtquery = conn.prepareStatement(updatequery);
                                  stmtquery.setDouble(1, Invquantity + (firstLocqty + secondLocqty));
                                  stmtquery.setDouble(2, Invbaseuomquantity + (firstLocqty + secondLocqty));
                                  stmtquery.setString(3, InvId);
                                  stmtquery.executeUpdate();
                                  
                         } else {
                             InvId = java.util.UUID.randomUUID().toString();
                             String queryInv = "insert into inventory(id,product,quantity,baseuomquantity,actquantity,baseuomrate,uom,description,carryin,defective,newinv,company,updatedate,deleteflag) values (?,?,?,?,?,?,?,'Inventory Opened','T','F','T',?,now(),'F')";
                             PreparedStatement stmtInv = conn.prepareStatement(queryInv);
                             stmtInv.setString(1, InvId);
                             stmtInv.setString(2, pid);
                             stmtInv.setDouble(3, firstLocqty + secondLocqty);
                             stmtInv.setDouble(4, firstLocqty + secondLocqty);
                             stmtInv.setDouble(5, 0);
                             stmtInv.setDouble(6, 1);
                             stmtInv.setString(7, uomid);
                             stmtInv.setString(8, companyId);
                             stmtInv.execute();
                             stmtInv.close();
                         }

                   stmt.close();
                } else {
                        System.out.println("Product Not Found");
                    }
                 
            }
            cnt++;
        }
         int count=cnt-1;
         StringBuilder result= new StringBuilder(""+count).append(" Records added successfully.");
         out.println(result);
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>
