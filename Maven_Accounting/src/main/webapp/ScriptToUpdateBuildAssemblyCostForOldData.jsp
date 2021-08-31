<%-- 
    Document   : OlympusImportMaster
    Created on : 23 Sep, 2015, 11:42:06 AM
    Author     : krawler
--%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.GregorianCalendar"%>
<%@page import="com.krawler.spring.accounting.reports.AccReportsHandler"%>
<%@page import="com.krawler.utils.json.base.JSONArray"%>
<%@page import="java.util.List"%>
<%@page import="com.krawler.spring.authHandler.authHandler"%>
<%@page import="java.util.Map"%>
<%@page import="com.krawler.spring.sessionHandler.sessionHandlerImpl"%>
<%@page import="com.krawler.spring.accounting.product.accProductControllerCMN"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.krawler.spring.accounting.product.accProductDAO"%>
<%@page import="com.krawler.spring.common.KwlReturnObject"%>
<%@page import="com.krawler.common.admin.User"%>
<%@page import="com.krawler.spring.auditTrailModule.auditTrailDAO"%>
<%@page import="com.krawler.common.util.Constants"%>
<%@page import="com.krawler.common.util.URLUtil"%>
<%@page import="java.text.ParseException"%>
<%@page import="com.krawler.common.admin.ImportLog"%>
<%@page import="com.krawler.spring.storageHandler.storageHandlerImpl"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.spring.accounting.handler.OlympusImportDataServiceDAO"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="java.io.FileFilter"%>
<%@page import="org.apache.commons.io.filefilter.AgeFileFilter"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.io.File"%>
<%@page import="com.krawler.spring.accounting.handler.OlympusImportDataController"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
    Connection conn = null;
    PreparedStatement pstmt = null;
    try {
//        String port = "3306";
        String driver = "com.mysql.jdbc.Driver";
        String serverip = request.getParameter("serverip") != null ? request.getParameter("serverip") : "";
        String dbName = request.getParameter("dbname") != null ? request.getParameter("dbname") : "";
        String userName = request.getParameter("username") != null ? request.getParameter("username") : "";
        String password = request.getParameter("password") != null ? request.getParameter("password") : "";
        String port = request.getParameter("port") != null ? request.getParameter("port") : "3306";
//        String subdomain = request.getParameter("subdomain") != null ? request.getParameter("subdomain") : "";

//        String serverip = "localhost";
//        String dbName = "sustenir_12042016";
//        String userName = "root";
//        String password = request.getParameter("password") != null ? request.getParameter("password") : "";
        String subdomain = "sustenir";
        JSONArray DataJArr = new JSONArray();

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(subdomain)|| StringUtil.isNullOrEmpty(password)) {
            out.println("Parameter missing from parameters => [serverip,dbname,username,password,subdomain] ");
        } else {
            String connectDB = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(connectDB, userName, password);

            String companyid = sessionHandlerImpl.getCompanyid(request);
            String queryForCompany = "select companyid,companyname FROM company  where subdomain= ? ";
            PreparedStatement stmt = conn.prepareStatement(queryForCompany);
            stmt.setString(1, subdomain);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String companyId = rs.getString("companyid");
                
                Set productBuildIdSet=new HashSet();
                Set productIdSet=new HashSet();
                String productQry="SELECT * from pbdetails pbd INNER JOIN productbuild pb ON pb.id=pbd.build WHERE percentage <> 100 AND  percentage/100 <> aquantity AND pb.company=? ";
                PreparedStatement stmtprod = conn.prepareStatement(productQry);
                stmtprod.setString(1, companyId);
                ResultSet rsBoMProd = stmtprod.executeQuery();
                
                while(rsBoMProd.next()){
                    String bomProdId=rsBoMProd.getString("aproduct");
                    String assemblyProdId=rsBoMProd.getString("pb.product");
                    String buildId=rsBoMProd.getString("build");
                    String buildDetailId=rsBoMProd.getString("pbd.id");
                    double percentage=rsBoMProd.getDouble("pbd.percentage");
                    double previousRate=rsBoMProd.getDouble("pbd.rate");
                    double buildQty=rsBoMProd.getDouble("pb.quantity");
                    double pbdQty=percentage/100;
                    productBuildIdSet.add(buildId);
                    productIdSet.add(bomProdId);
                    productIdSet.add(assemblyProdId);
                    
                    String updateQry2 = " UPDATE pbdetails pbd SET aquantity=?,actualquantity=?,inventoryquantity=?,rate=? WHERE id=? ";
                    PreparedStatement stmtforUpdate2 = conn.prepareStatement(updateQry2);
                    stmtforUpdate2.setDouble(1,pbdQty );
                    stmtforUpdate2.setDouble(2, pbdQty);
                    stmtforUpdate2.setDouble(3, pbdQty);
                    stmtforUpdate2.setDouble(4, previousRate * pbdQty);
                    stmtforUpdate2.setString(5, buildDetailId);
                    stmtforUpdate2.executeUpdate();
                    
                    
                    String updateQryy = " UPDATE inventory SET quantity=? ,baseuomquantity=? WHERE id=? ";
                    PreparedStatement stmtupdateQryy = conn.prepareStatement(updateQryy);
                    stmtupdateQryy.setDouble(1,pbdQty * buildQty);
                    stmtupdateQryy.setDouble(2,pbdQty * buildQty );
                    stmtupdateQryy.setString(3,buildDetailId );
                    stmtupdateQryy.executeUpdate();
                    
                    
                    String qrytoUpdateLBM=" SELECT id,batchmapid,quantity from locationbatchdocumentmapping WHERE documentid=? ";
                    PreparedStatement stmtlbm = conn.prepareStatement(qrytoUpdateLBM);
                    stmtlbm.setString(1, buildDetailId);
                    ResultSet rsLBM = stmtlbm.executeQuery();
                    while(rsLBM.next()){
                        String lbmId=rsLBM.getString("id");
                        String batchMapId=rsLBM.getString("batchmapid");
                        double lbmOldQty=rsLBM.getDouble("quantity");
                        double lbmNewQty=pbdQty * buildQty;
                        
                        String updateQry3 = " UPDATE locationbatchdocumentmapping SET quantity=? WHERE id=? ";
                        PreparedStatement stmtforUpdate3 = conn.prepareStatement(updateQry3);
                        stmtforUpdate3.setDouble(1,lbmNewQty);
                        stmtforUpdate3.setString(2,lbmId );
                        stmtforUpdate3.executeUpdate();
                        
                        String updateQry4 = " UPDATE newproductbatch SET quantity=quantity-?+?,quantitydue=quantitydue-?+? WHERE id=? ";
                        PreparedStatement stmtforUpdate4 = conn.prepareStatement(updateQry4);
                        stmtforUpdate4.setDouble(1,lbmOldQty);
                        stmtforUpdate4.setDouble(2,lbmNewQty);
                        stmtforUpdate4.setDouble(3,lbmOldQty);
                        stmtforUpdate4.setDouble(4,lbmNewQty);
                        stmtforUpdate4.setString(5,batchMapId);
                        stmtforUpdate4.executeUpdate();
                    }
                }
                
                Iterator itr=productBuildIdSet.iterator();
                while(itr.hasNext()){
                    String productBuildId=(String)itr.next();
                    double unitBuildcost=0;
                    String qrytoSelectRate=" SELECT SUM(rate) as unitbuildcost from pbdetails WHERE build= ? ";
                    PreparedStatement stmtrate = conn.prepareStatement(qrytoSelectRate);
                    stmtrate.setString(1, productBuildId);
                    ResultSet rsRate = stmtrate.executeQuery();
                    while(rsRate.next()){
                        unitBuildcost=rsRate.getDouble("unitbuildcost");
                    }
                    
                    String updateQry4 = " UPDATE productbuild SET productcost=quantity*? WHERE id=? ";
                    PreparedStatement stmtforUpdate4 = conn.prepareStatement(updateQry4);
                    stmtforUpdate4.setDouble(1,unitBuildcost);
                    stmtforUpdate4.setString(2,productBuildId);
                    stmtforUpdate4.executeUpdate();
                }
                
                
                
                itr=productIdSet.iterator();
                while(itr.hasNext()){
                    String productId=(String)itr.next();
                    String updateQry4 = " UPDATE product p1 INNER JOIN ( SELECT p.id, sum(case when carryIn='T' then baseuomquantity else -baseuomquantity end)as quantity from inventory inv inner join product p on p.id=inv.product where inv.deleteflag='F' AND p.id=? group by inv.product ) invtbl SET p1.availablequantity = invtbl.quantity  WHERE p1.id = invtbl.id  ";
                    PreparedStatement stmtforUpdate4 = conn.prepareStatement(updateQry4);
                    stmtforUpdate4.setString(1,productId);
                    stmtforUpdate4.executeUpdate();
                }
                
                out.println("<br/>Data Updated for subdomain : <b>"+subdomain+"</b> <br/>");
                out.println("<br/>Product Build Data updated for following ids : <br/>");
                out.println(productBuildIdSet.toString());
                out.println("<br/>Product Available Quantity updated for following Products : <br/>");
                out.println(productIdSet.toString());
            }

        }
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    } finally {
        if (pstmt != null) {
            pstmt.close();
        }
        if (conn != null) {
            conn.close();
        }
    }
%>
