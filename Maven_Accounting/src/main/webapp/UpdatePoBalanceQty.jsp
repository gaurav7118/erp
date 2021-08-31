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
        String port = "3306";
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

            String queryforPOs = "select DISTINCT po.id from purchaseorder po where po.company=? AND po.deleteflag='F' ";
            PreparedStatement pstmt = conn.prepareStatement(queryforPOs);
            pstmt.setString(1, companyId);
            ResultSet poresult = pstmt.executeQuery();
            while (poresult.next()) {

                String purchaseorderId = poresult.getString("id");
                boolean isOpen = false;
                boolean isLinked = false;
                String queryforPoDetails = "select pod.id,pod.quantity from podetails pod  where pod.purchaseorder=?";
                PreparedStatement stmtp = conn.prepareStatement(queryforPoDetails);
                stmtp.setObject(1, purchaseorderId);
                ResultSet rsp = stmtp.executeQuery();
                while (rsp.next()) {
                    String podetailsId = "";
                    double poquantity = 0.0;
                    podetailsId = rsp.getString("id");
                    poquantity = rsp.getDouble("quantity");

                    if (!StringUtil.isNullOrEmpty(podetailsId)) {
                        //set balance quantity of po details
                        String queryForupdate = "update podetails set balanceqty=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, poquantity);
                        stmtforUpdate.setString(2, podetailsId);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }
                    double totalgrQty = 0.0, totalprQty = 0.0;

                    String queryForGRO = "select id,deliveredquantity from grodetails  where  podetails=?  and company=? ";
                    PreparedStatement stmtgro = conn.prepareStatement(queryForGRO);
                    stmtgro.setObject(1, podetailsId);
                    stmtgro.setObject(2, companyId);
                    ResultSet rsgro = stmtgro.executeQuery();
                    while (rsgro.next()) {
                        String grodetailsId = rsgro.getString("id");
                        totalgrQty += rsgro.getDouble("deliveredquantity");

                        String queryForprdetails = "select id,returnquantity from prdetails  where  grdetails=?  and company=? ";
                        PreparedStatement stmtpr = conn.prepareStatement(queryForprdetails);
                        stmtpr.setObject(1, grodetailsId);
                        stmtpr.setObject(2, companyId);
                        ResultSet rspr = stmtpr.executeQuery();
                        while (rspr.next()) {
                            totalprQty += rspr.getDouble("returnquantity");
                        }
                    }
                    stmtgro.close();
                    if (!StringUtil.isNullOrEmpty(podetailsId)) {
                        //set balance quantity of po details
                        String queryForupdate = "update podetails set balanceqty=balanceqty-? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, (totalgrQty - totalprQty));
                        stmtforUpdate.setString(2, podetailsId);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }

                   if ((poquantity - (totalgrQty - totalprQty)) > 0 && !isOpen) {
                            isOpen = true;
                        }
                   
                   if (((poquantity - (totalgrQty - totalprQty)) != poquantity) && !isLinked) {
                            isLinked = true;
                        } 
                   totalproductUpdationCnt++;

                }
                
                  if (!StringUtil.isNullOrEmpty(purchaseorderId)) {
                        //set balance quantity of po details
                        String queryForupdate = "update purchaseorder set isopen=?,linkflag=? where id=? and company=? ";
                        PreparedStatement stmtforPoUpdate = conn.prepareStatement(queryForupdate);
                        if (isOpen) {
                            stmtforPoUpdate.setString(1, "T");
                        } else {
                            stmtforPoUpdate.setString(1, "F");
                        }
                        if (isLinked) {
                            stmtforPoUpdate.setInt(2, 2);
                        } else {
                            stmtforPoUpdate.setInt(2, 0);
                        }
                        stmtforPoUpdate.setString(3, purchaseorderId);
                        stmtforPoUpdate.setString(4, companyId);
                        stmtforPoUpdate.executeUpdate();
                    }

            }
            
            totalCompanyUpdationCnt++;
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
        out.println("<br><br> Total Purchase order rows updated are " + totalproductUpdationCnt);
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

