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

            String queryforPOs = "select DISTINCT pr.id from purchaserequisition pr where pr.company=? AND pr.deleteflag='F' ";
            PreparedStatement pstmt = conn.prepareStatement(queryforPOs);
            pstmt.setString(1, companyId);
            ResultSet prresult = pstmt.executeQuery();

            /*----------- Iterating on Purchase Requisition------------*/
            while (prresult.next()) {

                String purchaseRequisitionId = prresult.getString("id");

                String queryforPRDetails = "select prd.id,prd.quantity from purchaserequisitiondetail prd  where prd.purchaserequisition=?";
                PreparedStatement stmtp = conn.prepareStatement(queryforPRDetails);
                stmtp.setObject(1, purchaseRequisitionId);
                ResultSet rsp = stmtp.executeQuery();
               
            /*----------- Iterating on Purchase Requisition Details------------*/
                while (rsp.next()) {

                    String prdetailsId = rsp.getString("id");
                    double prquantity = rsp.getDouble("quantity");

                    if (!StringUtil.isNullOrEmpty(prdetailsId)) {
                        //Updating balance quantity of pr details
                        String queryForupdate = "update purchaserequisitiondetail set balanceqty=? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, prquantity);
                        stmtforUpdate.setString(2, prdetailsId);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }

                    double totalPPOQty = 0.0;

                    String queryForPO = "select id,quantity from podetails  where  purchaserequisitiondetailid=?  and company=? ";
                    PreparedStatement stmtpo = conn.prepareStatement(queryForPO);
                    stmtpo.setObject(1, prdetailsId);
                    stmtpo.setObject(2, companyId);
                    ResultSet rspo = stmtpo.executeQuery();
               
                    /*-----Calculating Total Quantity of PRdetail Used IN PO Detail----- */
                    while (rspo.next()) {
                        totalPPOQty += rspo.getDouble("quantity");
                    }
                    stmtpo.close();

                    if (!StringUtil.isNullOrEmpty(prdetailsId)) {
                        //Updating balance quantity of pr details
                        String queryForupdate = "update purchaserequisitiondetail set balanceqty=balanceqty-? where id=? and company=? ";
                        PreparedStatement stmtforUpdate = conn.prepareStatement(queryForupdate);
                        stmtforUpdate.setDouble(1, (totalPPOQty));
                        stmtforUpdate.setString(2, prdetailsId);
                        stmtforUpdate.setString(3, companyId);
                        stmtforUpdate.executeUpdate();
                    }

                }
  
                totalproductUpdationCnt++;
            }
            totalCompanyUpdationCnt++;
        }
        out.println("<br><br> Total companies updated are " + totalCompanyUpdationCnt);
        out.println("<br><br> Total Purchase Requisition rows updated are " + totalproductUpdationCnt);
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

