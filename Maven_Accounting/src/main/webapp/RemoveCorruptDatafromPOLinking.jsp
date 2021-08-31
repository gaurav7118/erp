<%-- --%>
<%@page import="com.krawler.common.util.Constants"%>
<%@page import="com.krawler.utils.json.JSON"%>
<%@page import="com.krawler.utils.json.JSONObject"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%

    Connection conn = null;
    try {
        String port = request.getParameter("port") != null ? request.getParameter("port") : "";
        String serverip = request.getParameter("serverip") != null ? request.getParameter("serverip") : "";
        String dbName = request.getParameter("dbname") != null ? request.getParameter("dbname") : "";
        String userName = request.getParameter("username") != null ? request.getParameter("username") : "";
        String password = request.getParameter("password") != null ? request.getParameter("password") : "";
        String subdomain = request.getParameter("subdomain") != null ? request.getParameter("subdomain") : "";
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName)) { //when password is empty
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username) in url. so please provide all these parameter correctly. ");
        }

        //Execution Started :
        out.println("<br><br><center>Execution Started @ " + new java.util.Date() + "</center><br><br>");
        out.println("<br><br><center>Please wait.....</center><br><br>");

        int count = 0;
        int delCount = 0;
        String id = "";
        String polinkingid = ""; // for polinking id
        String docid = ""; // for Purchase Order ID
        int moduleid = 0;
        String linkeddocid = ""; // for linking Doc ID either PI or GR
        String linkeddocno = "";
        int sourceflag = 0;

        String polinkingQuery = "";
        String poLinkWithPIQuery = "";
        String poLinkWithGRQuery = "";
        String delQuery = "";
        String insertQuery = "";

        PreparedStatement polinkingstmt = null;
        PreparedStatement poLinkWithPIstmt = null;
        PreparedStatement poLinkWithGRstmt = null;
        Statement delstmt = null;
        PreparedStatement insertstmt = null;

        ResultSet polinkingrs = null;
        ResultSet poLinkWithPIrs = null;
        ResultSet poLinkWithGRrs = null;

        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        /**
         * Moduleid : Purchase Invoice - 6,Goods Receipt Order - 28.
         * SourceFlag : Parent - 0, Child - 1.
         */
        if (StringUtil.isNullOrEmpty(subdomain)) {
            polinkingQuery = "select * from polinking where moduleid in (" + Constants.Acc_Vendor_Invoice_ModuleId + "," + Constants.Acc_Goods_Receipt_ModuleId + ") and sourceflag = 0";
        } else {
            polinkingQuery = "select pl.* from polinking pl, purchaseorder po, company c "
                    + "where pl.docid = po.id and po.company = c.companyid and moduleid in (" + Constants.Acc_Vendor_Invoice_ModuleId + "," + Constants.Acc_Goods_Receipt_ModuleId + ") and sourceflag = 0 and c.subdomain = '" + subdomain + "'";
        }

        polinkingstmt = conn.prepareStatement(polinkingQuery);
        polinkingrs = polinkingstmt.executeQuery();

        while (polinkingrs.next()) {

            polinkingid = polinkingrs.getString("id");
            docid = polinkingrs.getString("docid");
            moduleid = polinkingrs.getInt("moduleid");
            linkeddocid = polinkingrs.getString("linkeddocid");
            linkeddocno = polinkingrs.getString("linkeddocno");
            sourceflag = polinkingrs.getInt("sourceflag");

            /**
             * Checking PO is link with PI or not. if(count > 0) then PO is Link
             * with PI
             */
            if (moduleid == Constants.Acc_Vendor_Invoice_ModuleId) {

                poLinkWithPIQuery = " SELECT COUNT(*) FROM grdetails grd , podetails pod WHERE pod.id = grd.purchaseorderdetail "
                        + " and pod.purchaseorder = '" + docid + "'  and grd.goodsreceipt = '" + linkeddocid + "'";

                poLinkWithPIstmt = conn.prepareStatement(poLinkWithPIQuery);
                poLinkWithPIrs = poLinkWithPIstmt.executeQuery();

                while (poLinkWithPIrs.next()) {

                    count = poLinkWithPIrs.getInt(1);

                    if (count == 0) {
                        /**
                         * PO is not Link with either PI then we need to delete
                         * entry from goodsreceiptlinking table.
                         */
                        delQuery = " DELETE FROM goodsreceiptlinking WHERE docid = '" + linkeddocid + "' AND linkeddocid = '" + docid + "'";
                        delstmt = conn.createStatement();
                        delstmt.executeUpdate(delQuery);
                        delstmt.close();

                        /**
                         * PO is not Link with either PI or GR then we need to
                         * delete entry from polinking table.
                         */
                        delQuery = " DELETE FROM polinking WHERE docid = '" + docid + "' AND linkeddocid = '" + linkeddocid + "'";
                        delstmt = conn.createStatement();
                        delCount += delstmt.executeUpdate(delQuery);
                        delstmt.close();

                    }
                }
            } else if (moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                /**
                 * Checking PO is link with GR or not. if(count > 0) then PO is
                 * Link with GR
                 */
                poLinkWithGRQuery = " SELECT  COUNT(*) FROM grodetails gro , podetails pod WHERE pod.id = gro.podetails"
                        + " and pod.purchaseorder = '" + docid + "' AND gro.grorder = '" + linkeddocid + "'";

                poLinkWithGRstmt = conn.prepareStatement(poLinkWithGRQuery);
                poLinkWithGRrs = poLinkWithGRstmt.executeQuery();

                while (poLinkWithGRrs.next()) {

                    count = poLinkWithGRrs.getInt(1);

                    if (count == 0) {
                        /**
                         * PO is not Link with either GR then we need to delete
                         * entry from goodsreceiptorderlinking table.
                         */
                        delQuery = " DELETE FROM goodsreceiptorderlinking WHERE docid = '" + linkeddocid + "' AND linkeddocid = '" + docid + "'";
                        delstmt = conn.createStatement();
                        delstmt.executeUpdate(delQuery);
                        delstmt.close();

                        /**
                         * PO is not Link with either PI or GR then we need to
                         * delete entry from polinking table.
                         */
                        delQuery = " DELETE FROM polinking WHERE docid = '" + docid + "' AND linkeddocid = '" + linkeddocid + "'";
                        delstmt = conn.createStatement();
                        delCount += delstmt.executeUpdate(delQuery);
                        delstmt.close();

                    }
                }
            }

        }
        out.println("<br><br><center>Data Deleted Count : " + delCount + " </center><br><br>");

    } catch (SQLException e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            try {
                conn.close();
                out.println("<center>Connection Closed</center><br/>");
                //Execution Ended :
                out.println("<br><br><center>Execution Ended @ " + new java.util.Date() + "</center><br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


%>