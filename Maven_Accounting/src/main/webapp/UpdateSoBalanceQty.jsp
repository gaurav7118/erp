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
          String serverip = request.getParameter("serverip");//"192.168.0.208";                            
              String port = request.getParameter("port");//"3306";
              String dbName = request.getParameter("dbname");//"newstaging";
              String userName = request.getParameter("username");//"krawlersqladmin";
              String password = request.getParameter("password"); //"krawler"
              String subdomain = request.getParameter("subdomain");
        
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

            String sodetailsId = "", salesorderId = "";
            double poquantity = 0.0;
            String queryforsodetails = "select sod.id,sod.salesorder,sod.quantity from sodetails sod inner join salesorder so on sod.salesorder=so.id where so.company=? AND so.deleteflag='F'";
            PreparedStatement stmtp = conn.prepareStatement(queryforsodetails);  
            stmtp.setObject(1, companyId);
            ResultSet rsp = stmtp.executeQuery();
            while (rsp.next()) {
                     sodetailsId = rsp.getString("id");
                     salesorderId = rsp.getString("salesorder");
                     poquantity = rsp.getDouble("quantity");
                     
                if (!StringUtil.isNullOrEmpty(sodetailsId)) {
                    //set balance quantity of po details
                    String queryForupdate = "update sodetails set balanceqty=? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setDouble(1, poquantity);
                    stmtforUpdate.setString(2, sodetailsId);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }
                double totalDoQty = 0.0, totalsrQty = 0.0;
          
                    String queryForGRO = "select id,deliveredquantity from dodetails  where  sodetails=?  and company=? ";
                    PreparedStatement stmtgro = conn.prepareStatement(queryForGRO);
                    stmtgro.setObject(1, sodetailsId);
                    stmtgro.setObject(2, companyId);
                    ResultSet rsgro = stmtgro.executeQuery();
                    if (rsgro.next()) {
                        String dodetailsId = rsgro.getString("id");
                        totalDoQty += rsgro.getDouble("deliveredquantity");
                    if (!("olympus3".equals(subdomain))) {
                                String queryForprdetails = "select id,returnquantity from srdetails  where  dodetails=?  and company=? ";
                                PreparedStatement stmtpr = conn.prepareStatement(queryForprdetails);
                                stmtpr.setObject(1, dodetailsId);
                                stmtpr.setObject(2, companyId);
                                ResultSet rspr = stmtpr.executeQuery();
                                if (rspr.next()) {
                                    totalsrQty += rspr.getDouble("returnquantity");
                                }
                     }
                }
                    stmtgro.close();
                   if (!StringUtil.isNullOrEmpty(sodetailsId)) {
                    //set balance quantity of po details
                    String queryForupdate = "update sodetails set balanceqty=balanceqty-? where id=? and company=? ";
                    PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                    stmtforUpdate.setDouble(1, (totalDoQty - totalsrQty));
                    stmtforUpdate.setString(2, sodetailsId);
                    stmtforUpdate.setString(3, companyId);
                    stmtforUpdate.executeUpdate();
                }
                  if (!StringUtil.isNullOrEmpty(salesorderId)) {
                    //set balance quantity of po details
                    String queryForupdate = "update salesorder set isopen=?,linkflag=? where id=? and company=? ";
                    PreparedStatement stmtforPoUpdate = conn.prepareStatement(queryForupdate);
                    if ((poquantity - (totalDoQty - totalsrQty)) > 0) {
                            stmtforPoUpdate.setString(1, "T");
                     } else {
                            stmtforPoUpdate.setString(1, "F");
                      }
                    if ((poquantity - (totalDoQty - totalsrQty)) == poquantity) {
                            stmtforPoUpdate.setInt(2, 0);
                      } else {
                            stmtforPoUpdate.setInt(2, 2);
                      }
                            stmtforPoUpdate.setString(3, salesorderId);
                            stmtforPoUpdate.setString(4, companyId);
                            stmtforPoUpdate.executeUpdate();
                        } 
                    totalproductUpdationCnt++;
            }
            totalCompanyUpdationCnt++;
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
        out.println("<br><br> Total Salesorder rows updated are " + totalproductUpdationCnt);
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
