<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>


<%
    Connection conn = null;
    try {

        String serverip = "192.168.0.164";
        String port = "3306";
        String dbName = "atulTest";
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
            out.println("<br><br>Making Entry For Company subdomain <b>: "+companySubDomain+"</b>");
            
            
            // Filling Data into podetails table for rowtaxamount column.
            
            query = "SELECT id,ponumber FROM purchaseorder WHERE company=?";
            stmt = conn.prepareStatement(query);
            stmt.setObject(1, companyId);
            ResultSet poidrs = stmt.executeQuery();
            while(poidrs.next()){
                String poid = poidrs.getString("id");
                String ponumber = poidrs.getString("ponumber");
                int rowUpdationCntForAPO = 0;
                
                out.println("<br><br>Updating Purchase Order Rows For PO <b>: "+ponumber+"</b>");
                
                // Selecting purchase order rows for each PO.
                
                query = "SELECT id,quantity,rate,tax,discount,discountispercent FROM podetails WHERE purchaseorder=? AND company=?";
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, poid);
                stmt.setObject(2, companyId);
                ResultSet podetailsRS = stmt.executeQuery();
                while(podetailsRS.next()){
                    String podetailsRowId = podetailsRS.getString("id");
                    double quantity = podetailsRS.getDouble("quantity");
                    double rate = podetailsRS.getDouble("rate");
                    double discount = podetailsRS.getDouble("discount");
                    boolean discountispercent = podetailsRS.getBoolean("discountispercent");
                    double taxPercent = 0.0;
                    double rowTaxAmount = 0.0;
                    double quantityRate = quantity*rate;
                    if(podetailsRS.getObject("tax")!=null){
                        String taxId = podetailsRS.getString("tax");
                        query = "SELECT percent FROM taxlist WHERE tax=? AND company=?";
                        stmt = conn.prepareStatement(query);
                        stmt.setObject(1, taxId);
                        stmt.setObject(2, companyId);
                        ResultSet taxRS = stmt.executeQuery();
                        if(taxRS.next()){
                            taxPercent = taxRS.getDouble("percent");
                        }
                    }
                    if(discountispercent){
                        rowTaxAmount = (quantityRate-(quantityRate*discount/100))*taxPercent/100;
                    }else{
                        rowTaxAmount = (quantityRate-discount)*taxPercent/100;
                    }
                    query = "UPDATE podetails SET rowtaxamount=? WHERE id=? AND company=?";
                    stmt = conn.prepareStatement(query);
                    stmt.setObject(1, rowTaxAmount);
                    stmt.setObject(2, podetailsRowId);
                    stmt.setObject(3, companyId);
                    int updatedRowCnt = stmt.executeUpdate();
                    if(updatedRowCnt>0){
                        rowUpdationCntForAPO++;
                    }
                }
                out.println("<br><br>Rows Updated For PO <b>: "+ponumber+"</b> are : "+rowUpdationCntForAPO);
            }
            
            
            // Filling Data into Billing podetails table for rowtaxamount column.
            
            query = "SELECT id,ponumber FROM billingpurchaseorder WHERE company=?";
            stmt = conn.prepareStatement(query);
            stmt.setObject(1, companyId);
            ResultSet billingPoidrs = stmt.executeQuery();
            while(billingPoidrs.next()){
                String poid = billingPoidrs.getString("id");
                String ponumber = billingPoidrs.getString("ponumber");
                int rowUpdationCntForAPO = 0;
                
                out.println("<br><br>Updating Billing Purchase Order Rows For PO <b>: "+ponumber+"</b>");
                
                // Selecting purchase order rows for each PO.
                
                query = "SELECT id,quantity,rate,tax,discount,discountispercent FROM billingpodetails WHERE purchaseorder=? AND company=?";
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, poid);
                stmt.setObject(2, companyId);
                ResultSet podetailsRS = stmt.executeQuery();
                while(podetailsRS.next()){
                    String podetailsRowId = podetailsRS.getString("id");
                    double quantity = podetailsRS.getDouble("quantity");
                    double rate = podetailsRS.getDouble("rate");
                    double discount = podetailsRS.getDouble("discount");
                    boolean discountispercent = podetailsRS.getBoolean("discountispercent");
                    double taxPercent = 0.0;
                    double rowTaxAmount = 0.0;
                    double quantityRate = quantity*rate;
                    if(podetailsRS.getObject("tax")!=null){
                        String taxId = podetailsRS.getString("tax");
                        query = "SELECT percent FROM taxlist WHERE tax=? AND company=?";
                        stmt = conn.prepareStatement(query);
                        stmt.setObject(1, taxId);
                        stmt.setObject(2, companyId);
                        ResultSet taxRS = stmt.executeQuery();
                        if(taxRS.next()){
                            taxPercent = taxRS.getDouble("percent");
                        }
                    }
                    if(discountispercent){
                        rowTaxAmount = (quantityRate-(quantityRate*discount/100))*taxPercent/100;
                    }else{
                        rowTaxAmount = (quantityRate-discount)*taxPercent/100;
                    }
                    query = "UPDATE billingpodetails SET rowtaxamount=? WHERE id=? AND company=?";
                    stmt = conn.prepareStatement(query);
                    stmt.setObject(1, rowTaxAmount);
                    stmt.setObject(2, podetailsRowId);
                    stmt.setObject(3, companyId);
                    int updatedRowCnt = stmt.executeUpdate();
                    if(updatedRowCnt>0){
                        rowUpdationCntForAPO++;
                    }
                }
                out.println("<br><br>Rows Updated For Billing PO <b>: "+ponumber+"</b> are : "+rowUpdationCntForAPO);
            }
            
            
            // Filling Data into sodetails table for rowtaxamount column.
            
            query = "SELECT id,sonumber FROM salesorder WHERE company=?";
            stmt = conn.prepareStatement(query);
            stmt.setObject(1, companyId);
            ResultSet soidrs = stmt.executeQuery();
            while(soidrs.next()){
                String poid = soidrs.getString("id");
                String ponumber = soidrs.getString("sonumber");
                int rowUpdationCntForAPO = 0;
                
                out.println("<br><br>Updating Sales Order Rows For SO <b>: "+ponumber+"</b>");
                
                // Selecting purchase order rows for each PO.
                
                query = "SELECT id,quantity,rate,tax,discount,discountispercent FROM sodetails WHERE salesorder=? AND company=?";
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, poid);
                stmt.setObject(2, companyId);
                ResultSet podetailsRS = stmt.executeQuery();
                while(podetailsRS.next()){
                    String podetailsRowId = podetailsRS.getString("id");
                    double quantity = podetailsRS.getDouble("quantity");
                    double rate = podetailsRS.getDouble("rate");
                    double discount = podetailsRS.getDouble("discount");
                    boolean discountispercent = podetailsRS.getBoolean("discountispercent");
                    double taxPercent = 0.0;
                    double rowTaxAmount = 0.0;
                    double quantityRate = quantity*rate;
                    if(podetailsRS.getObject("tax")!=null){
                        String taxId = podetailsRS.getString("tax");
                        query = "SELECT percent FROM taxlist WHERE tax=? AND company=?";
                        stmt = conn.prepareStatement(query);
                        stmt.setObject(1, taxId);
                        stmt.setObject(2, companyId);
                        ResultSet taxRS = stmt.executeQuery();
                        if(taxRS.next()){
                            taxPercent = taxRS.getDouble("percent");
                        }
                    }
                    if(discountispercent){
                        rowTaxAmount = (quantityRate-(quantityRate*discount/100))*taxPercent/100;
                    }else{
                        rowTaxAmount = (quantityRate-discount)*taxPercent/100;
                    }
                    query = "UPDATE sodetails SET rowtaxamount=? WHERE id=? AND company=?";
                    stmt = conn.prepareStatement(query);
                    stmt.setObject(1, rowTaxAmount);
                    stmt.setObject(2, podetailsRowId);
                    stmt.setObject(3, companyId);
                    int updatedRowCnt = stmt.executeUpdate();
                    if(updatedRowCnt>0){
                        rowUpdationCntForAPO++;
                    }
                }
                out.println("<br><br>Rows Updated For Sales Order <b>: "+ponumber+"</b> are : "+rowUpdationCntForAPO);
            }
            
            
            // Filling Data into Billing sodetails table for rowtaxamount column.
            
            query = "SELECT id,sonumber FROM billingsalesorder WHERE company=?";
            stmt = conn.prepareStatement(query);
            stmt.setObject(1, companyId);
            ResultSet BillingSoidrs = stmt.executeQuery();
            while(BillingSoidrs.next()){
                String poid = BillingSoidrs.getString("id");
                String ponumber = BillingSoidrs.getString("sonumber");
                int rowUpdationCntForAPO = 0;
                
                out.println("<br><br>Updating Billing Sales Order Rows For SO <b>: "+ponumber+"</b>");
                
                // Selecting purchase order rows for each PO.
                
                query = "SELECT id,quantity,rate,tax,discount,discountispercent FROM billingsodetails WHERE salesorder=? AND company=?";
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, poid);
                stmt.setObject(2, companyId);
                ResultSet podetailsRS = stmt.executeQuery();
                while(podetailsRS.next()){
                    String podetailsRowId = podetailsRS.getString("id");
                    double quantity = podetailsRS.getDouble("quantity");
                    double rate = podetailsRS.getDouble("rate");
                    double discount = podetailsRS.getDouble("discount");
                    boolean discountispercent = podetailsRS.getBoolean("discountispercent");
                    double taxPercent = 0.0;
                    double rowTaxAmount = 0.0;
                    double quantityRate = quantity*rate;
                    if(podetailsRS.getObject("tax")!=null){
                        String taxId = podetailsRS.getString("tax");
                        query = "SELECT percent FROM taxlist WHERE tax=? AND company=?";
                        stmt = conn.prepareStatement(query);
                        stmt.setObject(1, taxId);
                        stmt.setObject(2, companyId);
                        ResultSet taxRS = stmt.executeQuery();
                        if(taxRS.next()){
                            taxPercent = taxRS.getDouble("percent");
                        }
                    }
                    if(discountispercent){
                        rowTaxAmount = (quantityRate-(quantityRate*discount/100))*taxPercent/100;
                    }else{
                        rowTaxAmount = (quantityRate-discount)*taxPercent/100;
                    }
                    query = "UPDATE billingsodetails SET rowtaxamount=? WHERE id=? AND company=?";
                    stmt = conn.prepareStatement(query);
                    stmt.setObject(1, rowTaxAmount);
                    stmt.setObject(2, podetailsRowId);
                    stmt.setObject(3, companyId);
                    int updatedRowCnt = stmt.executeUpdate();
                    if(updatedRowCnt>0){
                        rowUpdationCntForAPO++;
                    }
                }
                out.println("<br><br>Rows Updated For Billing Sales Order <b>: "+ponumber+"</b> are : "+rowUpdationCntForAPO);
            }
            
            
            // Filling Data into Vendor Quotation Details table for rowtaxamount column.
            
            query = "SELECT id,quotationnumber FROM vendorquotation WHERE company=?";
            stmt = conn.prepareStatement(query);
            stmt.setObject(1, companyId);
            ResultSet vqidrs = stmt.executeQuery();
            while(vqidrs.next()){
                String poid = vqidrs.getString("id");
                String ponumber = vqidrs.getString("quotationnumber");
                int rowUpdationCntForAPO = 0;
                
                out.println("<br><br>Updating Vendor Quotation Rows For VQ <b>: "+ponumber+"</b>");
                
                // Selecting purchase order rows for each PO.
                
                query = "SELECT id,quantity,rate,tax,discount,discountispercent FROM vendorquotationdetails WHERE vendorquotation=? AND company=?";
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, poid);
                stmt.setObject(2, companyId);
                ResultSet podetailsRS = stmt.executeQuery();
                while(podetailsRS.next()){
                    String podetailsRowId = podetailsRS.getString("id");
                    double quantity = podetailsRS.getDouble("quantity");
                    double rate = podetailsRS.getDouble("rate");
                    double discount = podetailsRS.getDouble("discount");
                    boolean discountispercent = podetailsRS.getBoolean("discountispercent");
                    double taxPercent = 0.0;
                    double rowTaxAmount = 0.0;
                    double quantityRate = quantity*rate;
                    if(podetailsRS.getObject("tax")!=null){
                        String taxId = podetailsRS.getString("tax");
                        query = "SELECT percent FROM taxlist WHERE tax=? AND company=?";
                        stmt = conn.prepareStatement(query);
                        stmt.setObject(1, taxId);
                        stmt.setObject(2, companyId);
                        ResultSet taxRS = stmt.executeQuery();
                        if(taxRS.next()){
                            taxPercent = taxRS.getDouble("percent");
                        }
                    }
                    if(discountispercent){
                        rowTaxAmount = (quantityRate-(quantityRate*discount/100))*taxPercent/100;
                    }else{
                        rowTaxAmount = (quantityRate-discount)*taxPercent/100;
                    }
                    query = "UPDATE vendorquotationdetails SET rowtaxamount=? WHERE id=? AND company=?";
                    stmt = conn.prepareStatement(query);
                    stmt.setObject(1, rowTaxAmount);
                    stmt.setObject(2, podetailsRowId);
                    stmt.setObject(3, companyId);
                    int updatedRowCnt = stmt.executeUpdate();
                    if(updatedRowCnt>0){
                        rowUpdationCntForAPO++;
                    }
                }
                out.println("<br><br>Rows Updated For Vendor Quotation <b>: "+ponumber+"</b> are : "+rowUpdationCntForAPO);
            }
            
            
            // Filling Data into Customer Quotation Details table for rowtaxamount column.
            
            query = "SELECT id,quotationnumber FROM quotation WHERE company=?";
            stmt = conn.prepareStatement(query);
            stmt.setObject(1, companyId);
            ResultSet cqidrs = stmt.executeQuery();
            while(cqidrs.next()){
                String poid = cqidrs.getString("id");
                String ponumber = cqidrs.getString("quotationnumber");
                int rowUpdationCntForAPO = 0;
                
                out.println("<br><br>Updating Customer Quotation Rows For CQ <b>: "+ponumber+"</b>");
                
                // Selecting purchase order rows for each PO.
                
                query = "SELECT id,quantity,rate,tax,discount,discountispercent FROM quotationdetails WHERE quotation=? AND company=?";
                stmt = conn.prepareStatement(query);
                stmt.setObject(1, poid);
                stmt.setObject(2, companyId);
                ResultSet podetailsRS = stmt.executeQuery();
                while(podetailsRS.next()){
                    String podetailsRowId = podetailsRS.getString("id");
                    double quantity = podetailsRS.getDouble("quantity");
                    double rate = podetailsRS.getDouble("rate");
                    double discount = podetailsRS.getDouble("discount");
                    boolean discountispercent = podetailsRS.getBoolean("discountispercent");
                    double taxPercent = 0.0;
                    double rowTaxAmount = 0.0;
                    double quantityRate = quantity*rate;
                    if(podetailsRS.getObject("tax")!=null){
                        String taxId = podetailsRS.getString("tax");
                        query = "SELECT percent FROM taxlist WHERE tax=? AND company=?";
                        stmt = conn.prepareStatement(query);
                        stmt.setObject(1, taxId);
                        stmt.setObject(2, companyId);
                        ResultSet taxRS = stmt.executeQuery();
                        if(taxRS.next()){
                            taxPercent = taxRS.getDouble("percent");
                        }
                    }
                    if(discountispercent){
                        rowTaxAmount = (quantityRate-(quantityRate*discount/100))*taxPercent/100;
                    }else{
                        rowTaxAmount = (quantityRate-discount)*taxPercent/100;
                    }
                    query = "UPDATE quotationdetails SET rowtaxamount=? WHERE id=? AND company=?";
                    stmt = conn.prepareStatement(query);
                    stmt.setObject(1, rowTaxAmount);
                    stmt.setObject(2, podetailsRowId);
                    stmt.setObject(3, companyId);
                    int updatedRowCnt = stmt.executeUpdate();
                    if(updatedRowCnt>0){
                        rowUpdationCntForAPO++;
                    }
                }
                out.println("<br><br>Rows Updated For Costomer Quotation <b>: "+ponumber+"</b> are : "+rowUpdationCntForAPO);
            }
        }
        
        
        
        
        
         out.println(cnt +" Records added successfully.");
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        //conn.close();
    }

%>
