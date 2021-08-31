
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
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subdomain = request.getParameter("subdomain"); //"caygroup";
        String InvLocId; //5b62d8be-0237-40c8-83a7-495c232ad498";
        String InvWarId;   //"c0ea4a9a-fad1-46d6-82e1-69fffa012731";

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(subdomain)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password,subdomain) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname FROM company ";
        //if (!StringUtil.isNullOrEmpty(subdomain)) {
        query += " where subdomain= ?";
        //}
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int totalCompanyUpdationCnt = 0;
        int totalproductUpdationCnt = 0;
        String defWarehouse = "ff80808154aa8f730154b38fcde3081a";
        String defLocation = "4e6f3d4c-9973-479f-9c43-0292482c7d7e";
        while (rs.next()) {
            String companyId = rs.getString("companyid");

            String productid = "", productname = "";
            String queryproduct = "select grdid,productid,qty from tempgrdidforgs ";
            PreparedStatement stproduct = conn.prepareStatement(queryproduct);  //select all product from company
            ResultSet rsp = stproduct.executeQuery();

            while (rsp.next()) {
                productid = rsp.getString("productid");
                String grdid = rsp.getString("grdid");
                double qty = Double.parseDouble(rsp.getString("qty"));
                if (productid.equals("ff80808154aa8f730154b898eafa0a68") || productid.equals("ff80808154aa8f730154b8cbcebd0a8a")) {
                    continue;
                }
                String queryForbatchselect = "select id from newproductbatch where product=? and company=? and warehouse=? and location=?";
                PreparedStatement stmtbat = conn.prepareStatement(queryForbatchselect);
                stmtbat.setObject(1, productid);
                stmtbat.setObject(2, companyId);
                stmtbat.setObject(3, defWarehouse);
                stmtbat.setObject(4, defLocation);

                ResultSet rs9 = stmtbat.executeQuery();

                if (rs9.next()) {
                    String batchId = rs9.getString("id");
                    String queryForbatchselect1 = "select id from locationbatchdocumentmapping where documentid=?";
                    PreparedStatement stmtbat1 = conn.prepareStatement(queryForbatchselect1);
                    stmtbat1.setObject(1, grdid);


                    ResultSet rs10 = stmtbat1.executeQuery();

                    if (!StringUtil.isNullOrEmpty(batchId)) {
                        String queryForupdate = "update newproductbatch set quantity=quantity+?,quantitydue=quantitydue+? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, qty);
                        stmtforUpdate.setDouble(2, qty);
                        stmtforUpdate.setString(3, batchId);
                        stmtforUpdate.setString(4, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                    if (!rs10.next()) {
                        String locBatchdocId = java.util.UUID.randomUUID().toString();
                        String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                        PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                        stmtlbmap.setString(1, locBatchdocId);
                        stmtlbmap.setDouble(2, qty);
                        stmtlbmap.setString(3, grdid);
                        stmtlbmap.setString(4, batchId);
                        stmtlbmap.execute();
                        stmtlbmap.close();
                        out.println("<br><br> for grdetail " + grdid + "and for product " + productid + "qty " + qty + "inserted in to lbdm");
                        totalproductUpdationCnt++;
                    }
                } else {
                    String batchMapId = java.util.UUID.randomUUID().toString();
                    String querybatch = "insert into newproductbatch (id,quantity,quantitydue,location,warehouse,transactiontype,product,isopening,ispurchase,company) values (?,?,?,?,?,28,?,'F','T',?)";
                    PreparedStatement stmtb = conn.prepareStatement(querybatch);
                    stmtb.setString(1, batchMapId);
                    stmtb.setDouble(2, qty);
                    stmtb.setDouble(3, qty);
                    stmtb.setString(4, defLocation); //location
                    stmtb.setString(5, defWarehouse);  //warehouse
                    stmtb.setString(6, productid);
                    stmtb.setString(7, companyId);
                    stmtb.execute();
                    stmtb.close();

                    String locBatchdocId = java.util.UUID.randomUUID().toString();
                    String querylbmap = "insert into locationbatchdocumentmapping(id,quantity,documentid,batchmapid,transactiontype) values(?,?,?,?,28)";
                    PreparedStatement stmtlbmap = conn.prepareStatement(querylbmap);
                    stmtlbmap.setString(1, locBatchdocId);
                    stmtlbmap.setDouble(2, qty);
                    stmtlbmap.setString(3, grdid);
                    stmtlbmap.setString(4, batchMapId);
                    stmtlbmap.execute();
                    stmtlbmap.close();
                    out.println("<br><br> for grdetail " + grdid + "and for product " + productid + "qty " + qty + "inserted in to lbdm");
                    totalproductUpdationCnt++;
                }

                //}

            }
            totalCompanyUpdationCnt++;
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
        out.println("<br><br> Total products updated are " + totalproductUpdationCnt);
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
