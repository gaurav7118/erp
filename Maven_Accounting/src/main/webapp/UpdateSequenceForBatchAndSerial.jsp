
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
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = request.getParameter("subdomain");

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) ) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        int count = 0;
        String companyID = "";
        String compquery = "";
        out.println("<center>");
        out.println("<br><br><br><br><br>");
        out.println("<table cellspacing='0' cellpadding='10' border='1' align='center' >");
        if(!StringUtil.isNullOrEmpty(subdomain)){
            compquery = " select companyid, subdomain from company where subdomain=? ";
        } else {
            compquery = " select companyid , subdomain from company order by companyid ";
        }
        
        PreparedStatement stmt = conn.prepareStatement(compquery);
        if(!StringUtil.isNullOrEmpty(subdomain)){
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();
        int srno = 1;
        while (rs.next()) {
            companyID = rs.getString(1); // Company ID.
            subdomain = rs.getString(2); // Sub Domain
            
            
            out.println("<tr><th>srno</th><th>Subdomain</th><th>DO</th><th>GRN</th><th>SR</th><th>PR</th><th>SO</th><th>AD</th><th>Product</th><th>WO</th><th>PB</th><th>WOD</th><th>PBD</th>  </tr>");
            out.println("<tr>");
            out.println("<td> "+ srno +" </td>");
            out.println("<td> "+ subdomain +" </td>");
            count = UpdateSequnceNOByModule(companyID, conn, "dodetails"); // For Delivery Order
            out.println("<td> "+ count +" </td>");
            count = UpdateSequnceNOByModule(companyID, conn, "grodetails"); // For Goods Receipt Note
            out.println("<td> "+ count +" </td>");
            count = UpdateSequnceNOByModule(companyID, conn, "srdetails"); // For Sales Return
            out.println("<td> "+ count +" </td>");
            count = UpdateSequnceNOByModule(companyID, conn, "prdetails"); // For Purchase Return
            out.println("<td> "+ count +" </td>");
            count = UpdateSequnceNOByModule(companyID, conn, "sodetails"); // For Sales Order
            out.println("<td> "+ count +" </td>");
            count = UpdateSequnceNOByModule(companyID, conn, "assetdetail"); // For Asset Details
            out.println("<td> "+ count +" </td>");
            count = UpdateSequnceNOByModule(companyID, conn, "product"); // For Product
            out.println("<td> "+ count +" </td>");
            count = UpdateSequnceNOByModule(companyID, conn, "workorder"); // For workorder
            out.println("<td> "+ count +" </td>");
            count = UpdateSequnceNOByModule(companyID, conn, "productbuild"); // For Product Build
            out.println("<td> "+ count +" </td>");
//           
            count = UpdateSequnceNOByModuleOther(companyID, conn, "workordercomponentdetail"); // For workordercomponentdetail
            out.println("<td> "+ count +" </td>");
            count = UpdateSequnceNOByModuleOther(companyID, conn, "pbdetails"); // For pbdetails
            out.println("<td> "+ count +" </td>");
            out.println("</tr>");
            srno++;
        }

       
        out.println("</table>");
        out.println("<br><br> Script Executed Sccessfully ");
        out.println("</center>");

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
    
%>
<%!
    public static int UpdateSequnceNOByModule(String companyID, Connection conn, String tableName) {
        
        int count = 0;
        int batchResult = 0;
        int serialResult = 0;
        try {
            
            String detailsID = "select tbl.id from "+ tableName +" tbl inner join locationbatchdocumentmapping  lbdm ON lbdm.documentid = tbl.id "
                    + "where company = ? order by tbl.id ";
            PreparedStatement stmt1 = conn.prepareStatement(detailsID);
            stmt1.setString(1, companyID);
            ResultSet rs1 = stmt1.executeQuery();
           
            while (rs1.next()) {
                String documentID = rs1.getString(1);
                String batchIDQuery = "select lbdm.batchmapid , lbdm.id from locationbatchdocumentmapping  lbdm  where lbdm.documentid = ?";
                PreparedStatement batchStmt = conn.prepareStatement(batchIDQuery);
                
                batchStmt.setString(1, documentID); // Document ID
                ResultSet batchRS = batchStmt.executeQuery();
                int batchSeqno = 1; int serialSeqno = 1;
                while (batchRS.next()){
                    String batchID = batchRS.getString(1);
                    String lbdmID = batchRS.getString(2);
                    String updateBatchSeq = " update  locationbatchdocumentmapping  lbdm SET lbdm.selectedsequence = ?  where lbdm.documentid = ? and lbdm.id = ?";
                    PreparedStatement updateLBDM = conn.prepareStatement(updateBatchSeq);
                    updateLBDM.setInt(1, batchSeqno);// Sequnce Number
                    updateLBDM.setString(2, documentID);// Document ID
                    updateLBDM.setString(3, lbdmID);// LBDM ID
                    batchResult = updateLBDM.executeUpdate();
                    if(batchResult > 0){
                        count++;
                    }
                    batchSeqno++;
                    
                    
                    /**
                     * UPDATE Serial Sequence. 
                     */
                    String serialIDquery = "select nbs.id, sdm.id from serialdocumentmapping sdm inner join newbatchserial nbs  ON sdm.serialid = nbs.id "
                        + "where sdm.documentid=? and nbs.batch=? and nbs.company= ?";
                    PreparedStatement serialStmt = conn.prepareStatement(serialIDquery);
                    serialStmt.setString(1, documentID);// Document ID
                    serialStmt.setString(2, batchID);// Batch ID
                    serialStmt.setString(3, companyID); // Company ID
                    ResultSet serialRS = serialStmt.executeQuery();
                    while(serialRS.next()){
                        String serialID = serialRS.getString(1);
                        String sdmID = serialRS.getString(2);
                        String updateSerialSeq = " update  serialdocumentmapping sdm inner join newbatchserial nbs  ON sdm.serialid = nbs.id SET selectedsequence = ?  "
                                + "where nbs.company= ? and sdm.documentid= ? and sdm.id = ?";
                        PreparedStatement updateSDM = conn.prepareStatement(updateSerialSeq);
                        updateSDM.setInt(1, serialSeqno); // Sequnce Number 
                        //updateSDM.setString(2, batchID); // Batch ID
                        updateSDM.setString(2, companyID); // Company ID
                        updateSDM.setString(3, documentID); // Document ID
                        updateSDM.setString(4, sdmID); // SDM ID
                    
                        serialResult = updateSDM.executeUpdate();
                        if (serialResult > 0) {
                            count++;
                        }
                        serialSeqno++;    
                    }
                
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();

        }
        return count;
    }
    
    public int UpdateSequnceNOByModuleOther(String companyID, Connection conn, String tableName){
        int count = 0;
        int batchResult = 0;
        int serialResult = 0;
        String product = "product";
        try {
            if(tableName.equals("pbdetails")){
                product = "aproduct";
            } 
            
            String detailsID = "select distinct tbl.id from "+ tableName +" tbl inner join locationbatchdocumentmapping  lbdm ON lbdm.documentid = tbl.id "
                    + " inner join product p on tbl."+ product +" = p.id where company = ? order by tbl.id ";
            PreparedStatement stmt1 = conn.prepareStatement(detailsID);
            stmt1.setString(1, companyID);
            ResultSet rs1 = stmt1.executeQuery();
           
            while (rs1.next()) {
                String documentID = rs1.getString(1);
                String batchIDQuery = "select lbdm.batchmapid , lbdm.id from locationbatchdocumentmapping  lbdm inner join " + tableName + " tbl ON lbdm.documentid = tbl.id "
                    + " inner join product p on tbl."+ product +" = p.id where company = ?  and lbdm.documentid = ?";
                PreparedStatement batchStmt = conn.prepareStatement(batchIDQuery);
                batchStmt.setString(1, companyID); // Company ID
                batchStmt.setString(2, documentID); // Document ID
                ResultSet batchRS = batchStmt.executeQuery();
                int batchSeqno = 1; int serialSeqno = 1;
                while (batchRS.next()){
                    String batchID = batchRS.getString(1);
                    String lbdmID = batchRS.getString(2);
                    String updateBatchSeq = " update  locationbatchdocumentmapping  lbdm inner join " + tableName + " tbl ON lbdm.documentid = tbl.id "
                        + " inner join product p on tbl."+ product +" = p.id  SET lbdm.selectedsequence = ?  where company = ? and lbdm.documentid = ? and lbdm.id = ?";
                    PreparedStatement updateLBDM = conn.prepareStatement(updateBatchSeq);
                    updateLBDM.setInt(1, batchSeqno);// Sequnce Number
                    updateLBDM.setString(2, companyID); // Company ID
                    updateLBDM.setString(3, documentID);// Document ID
                    updateLBDM.setString(4, lbdmID);// LBDM ID
                    batchResult = updateLBDM.executeUpdate();
                    if(batchResult > 0){
                        count++;
                    }
                    batchSeqno++;
                    
                    
                    /**
                     * UPDATE Serial Sequence. 
                     */
                    String serialIDquery = "select nbs.id, sdm.id from serialdocumentmapping sdm inner join newbatchserial nbs  ON sdm.serialid = nbs.id "
                        + "where sdm.documentid=? and nbs.batch=? and nbs.company= ?";
                    PreparedStatement serialStmt = conn.prepareStatement(serialIDquery);
                    serialStmt.setString(1, documentID);// Document ID
                    serialStmt.setString(2, batchID);// Batch ID
                    serialStmt.setString(3, companyID); // Company ID
                    ResultSet serialRS = serialStmt.executeQuery();
                    while(serialRS.next()){
                        String serialID = serialRS.getString(1);
                        String sdmID = serialRS.getString(2);
                        String updateSerialSeq = " update  serialdocumentmapping sdm inner join newbatchserial nbs  ON sdm.serialid = nbs.id SET selectedsequence = ?  "
                                + "where nbs.company= ? and sdm.documentid= ? and sdm.id = ?";
                        PreparedStatement updateSDM = conn.prepareStatement(updateSerialSeq);
                        updateSDM.setInt(1, serialSeqno); // Sequnce Number 
                        //updateSDM.setString(2, batchID); // Batch ID
                        updateSDM.setString(2, companyID); // Company ID
                        updateSDM.setString(3, documentID); // Document ID
                        updateSDM.setString(4, sdmID); // SDM ID
                    
                        serialResult = updateSDM.executeUpdate();
                        if (serialResult > 0) {
                            count++;
                        }
                        serialSeqno++;    
                    }
                
                }
            }
            
        } catch (Exception e) {
             e.printStackTrace();

        }
        return count;
    }
    
%>


