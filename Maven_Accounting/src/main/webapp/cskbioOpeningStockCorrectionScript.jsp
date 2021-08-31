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
        String serverip = request.getParameter("serverip");                          
        String port = request.getParameter("port");
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
//        String serverip = "192.168.0.52";
//        String port = "3306";
//        String dbName = "DC_18012016";
//        String userName = "root";
//        String password = "krawler";
        String subdomain = "cskbio";

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname FROM company where subdomain= ?";

        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        int totalproductUpdationCnt = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");

            if (!StringUtil.isNullOrEmpty(companyId)) {

                String InvLocId = "";
                String queryinvid = "select id from in_location where company=? and isdefault=1 ";  //is default location availble
                PreparedStatement stmt4 = conn.prepareStatement(queryinvid);
                stmt4.setObject(1, companyId);
                ResultSet rs4 = stmt4.executeQuery();
                if (rs4.next()) {
                    InvLocId = rs4.getString("id");
                }
                

                String InvWarId = "";
                String queryWarid = "select id from in_storemaster where company=? and isdefault=1 "; //is default warehouse availble
                PreparedStatement stmt5 = conn.prepareStatement(queryWarid);
                stmt5.setObject(1, companyId);
                ResultSet rs5 = stmt5.executeQuery();
                if (rs5.next()) {
                    InvWarId = rs5.getString("id");
                }
                
                String productid = "", productname = "";
                String queryproduct = "select id,name from product where company=? AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa')"; //product type = assembly,inventory part
                PreparedStatement stmtp = conn.prepareStatement(queryproduct);  //select all product from company
                stmtp.setObject(1, companyId);
                ResultSet rsp = stmtp.executeQuery();
                while (rsp.next()) {
                    boolean anyProductRecordUpdated = false;
                    productid = rsp.getString("id");
                    productname = rsp.getString("name");
                    


                    /*
                     * **********************************
                     * For : Opening Case 
                       **********************************
                     */
                    String InvId = "";
                    double Invbaseuomquantity = 0.0, Invquantity = 0.0;
                    String invquery = "select * from inventory where company=? and product= ?  and carryin='T' and newinv='T' and isopening ='T' ";
                    PreparedStatement stmtinv = conn.prepareStatement(invquery);
                    stmtinv.setObject(1, companyId);
                    stmtinv.setObject(2, productid);
                    ResultSet rsinv = stmtinv.executeQuery();
                    if (rsinv.next()) {
                        InvId = rsinv.getString("id");
                        Invquantity = rsinv.getDouble("quantity");
                        Invbaseuomquantity = rsinv.getDouble("baseuomquantity");
                    }
                    if (!StringUtil.isNullOrEmpty(InvId)) { //check if opening is done for this product ifthere then move that quantity in default location and warehouse
                        if (Invbaseuomquantity > 0) {
                            String batchMapId = java.util.UUID.randomUUID().toString();
                            String querybatch = "insert into newproductbatch (id,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company) values (?,?,?,?,?,28,?,'T','T',?)";
                            PreparedStatement stmtb = conn.prepareStatement(querybatch);
                            stmtb.setString(1, batchMapId);
                            stmtb.setDouble(2, Invbaseuomquantity);
                            stmtb.setDouble(3, Invbaseuomquantity);
                            stmtb.setString(4, InvLocId); //location
                            stmtb.setString(5, InvWarId);  //warehouse
                            stmtb.setString(6, productid);
                            stmtb.setString(7, companyId);
                            stmtb.execute();
                            stmtb.close();

                            String locBatchdocId = java.util.UUID.randomUUID().toString();
                            String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                            PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                            stmtlbmap.setString(1, locBatchdocId);
                            stmtlbmap.setDouble(2, Invbaseuomquantity);
                            stmtlbmap.setString(3, productid);
                            stmtlbmap.setString(4, batchMapId);
                            stmtlbmap.execute();
                            stmtlbmap.close();
                            anyProductRecordUpdated = true;
                                           
                            totalproductUpdationCnt++;
                        }
                    }
                }

                totalCompanyUpdationCnt++;
            }
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
        out.println("<br><br> Total products opening mapping added count is " + totalproductUpdationCnt);
        out.println("<br><br> Time :  " + new java.util.Date().toString());
    } catch (Exception e) {
        if (conn != null) {
            // conn.rollback();
        }
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>