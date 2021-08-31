<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


<%
    Connection conn = null;
    try {

        String serverip = "192.168.0.209";
        String port = "3306";
        String dbName = "newstaging";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";

        String driver = "com.mysql.jdbc.Driver";
        String userName = "krawlersqladmin";
        String password = "krawler";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);
        int cnt = 0;
        String record = "";
        String companyId="";
        String companySubDomain="";
        
        String query = "SELECT companyid, subdomain FROM company";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        
        while(rs.next()) {
            companyId = rs.getString("companyid");
            companySubDomain = rs.getString("subdomain");
            out.println("<br><br><b>Making Entry For Company subdomain : "+companySubDomain+"</b>");
            
            
            // adding vendor in payment
            
            int pmtCnt = 0;
            query = "SELECT id, paymentnumber FROM payment WHERE company=? and vendor is null";
            stmt = conn.prepareStatement(query);
            stmt.setObject(1, companyId);
            ResultSet paymentRS = stmt.executeQuery();
            while(paymentRS.next()){
                String paymentId = paymentRS.getString("id");
                String paymentNumber = paymentRS.getString("paymentnumber");
                
                query = "SELECT gr.vendor as vid FROM paymentdetail pdl Inner Join payment pmt on pmt.id = pdl.payment Inner Join goodsreceipt gr on gr.id = pdl.goodsReceipt WHERE pmt.company=? AND pmt.id=?";
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, companyId);
                stmt.setObject(2, paymentId);
                
                ResultSet vendorIDRS = stmt.executeQuery();
                if(vendorIDRS.next()){
                    String vendorId = vendorIDRS.getString("vid");
                    
                    query = "UPDATE payment set vendor = ? WHERE id=? AND company=?";
                    stmt = conn.prepareStatement(query);
                    stmt.setObject(1, vendorId);
                    stmt.setObject(2, paymentId);
                    stmt.setObject(3, companyId);
                    stmt.executeUpdate();
                    out.println("<br><br>Vendor filled for Payment Number <b>: "+paymentNumber+"</b>");
                    pmtCnt++;
                }
            }
            
            
            // adding customer in receivepayment
            int receiptCnt = 0;
            
            query = "SELECT id, receiptnumber FROM receipt WHERE company=? and customer is null";
            stmt = conn.prepareStatement(query);
            stmt.setObject(1, companyId);
            ResultSet receiptRS = stmt.executeQuery();
            while(receiptRS.next()){
                String receiptId = receiptRS.getString("id");
                String receiptNumber = receiptRS.getString("receiptnumber");
                
                query = "SELECT inv.customer as cid FROM receiptdetails rdl Inner Join receipt rpt on rpt.id = rdl.receipt Inner Join invoice inv on inv.id = rdl.invoice WHERE rpt.company=? AND rpt.id=?";
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, companyId);
                stmt.setObject(2, receiptId);
                
                ResultSet customerIDRS = stmt.executeQuery();
                if(customerIDRS.next()){
                    String customerId = customerIDRS.getString("cid");
                    
                    query = "UPDATE receipt set customer = ? WHERE id=? AND company=?";
                    stmt = conn.prepareStatement(query);
                    stmt.setObject(1, customerId);
                    stmt.setObject(2, receiptId);
                    stmt.setObject(3, companyId);
                    stmt.executeUpdate();
                    out.println("<br><br>Customer filled for Receipt Number <b>: "+receiptNumber+"</b>");
                    receiptCnt++;
                }
            }
            
            out.println("<br><br>"+pmtCnt +" Payments updated successfully.");
            out.println("<br><br>"+receiptCnt +" Receipts updated successfully.");
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>
