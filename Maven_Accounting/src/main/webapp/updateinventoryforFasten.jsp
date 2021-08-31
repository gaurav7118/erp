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
       /* String serverip = request.getParameter("serverip");//"192.168.0.208";                            
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"newstaging";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = request.getParameter("subdomain"); */
        String serverip = "localhost";                            
        String port = "3306";
        String dbName = "fast123";
        String userName = "krawlersqladmin";
        String password = "Krawler[X]";
        String subdomain = "fasten";

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
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
        int totalproductUpdationCnt = 0;
        while (rs.next()) {
            String companyId = rs.getString("companyid");

          

            String productid = "", productname = "";
            String queryproduct = "select id,name from product where company=? AND producttype IN('e4611696-515c-102d-8de6-001cc0794cfa','d8a50d12-515c-102d-8de6-001cc0794cfa')"; //product type = assembly,inventory part
            PreparedStatement stmtp = conn.prepareStatement(queryproduct);  //select all product from company
            stmtp.setObject(1, companyId);
            ResultSet rsp = stmtp.executeQuery();
            while (rsp.next()) {
                productid = rsp.getString("id");
                  
                String deletequery = "delete from inventory  where newinv='T' and product=? and company=? ";
                PreparedStatement statement = conn.prepareStatement(deletequery);
                statement.setString(1, productid);
                statement.setString(2, companyId);
                statement.executeUpdate();
                      
                String querylocbatchmapid = "select id,batchmapid,quantity from locationbatchdocumentmapping where documentid=? ";
                PreparedStatement stmtgr = conn.prepareStatement(querylocbatchmapid);
                stmtgr.setObject(1, productid);
                ResultSet rsgr = stmtgr.executeQuery();
                while (rsgr.next()) {
                    String locbationdocid = rsgr.getString("id");
                    String batchmapid = rsgr.getString("batchmapid");
                    double quantity = rsgr.getDouble("quantity");

                    if (!StringUtil.isNullOrEmpty(batchmapid)) {
                        String queryForupdate = "update newproductbatch set quantity=quantity-?,quantitydue=quantitydue-? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, quantity);
                        stmtforUpdate.setDouble(2, quantity);
                        stmtforUpdate.setString(3, batchmapid);
                        stmtforUpdate.setString(4, companyId);
                        stmtforUpdate.executeUpdate();
                    }

                    String deletemapquery = "delete from locationbatchdocumentmapping where id=?";
                    PreparedStatement statement1 = conn.prepareStatement(deletemapquery);
                    statement1.setString(1, locbationdocid);
                    statement1.executeUpdate();
                    
                    totalproductUpdationCnt++;
                }
            }
        }
        out.println("<br><br> Total products updated are " + totalproductUpdationCnt);
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }

%>