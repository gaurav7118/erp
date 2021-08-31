<%@page import="com.krawler.common.util.StringUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
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
        String driver = "com.mysql.jdbc.Driver";
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String url = "jdbc:mysql@" + serverip + ":" + port + "/";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        System.out.print(url + dbName + userName + password);

        int cnt = 0;

        String companyId = "02435724-03b4-4470-a2f3-17bf5e10c6cf";    //companyid for caygroup
        String bbfrom = "2016-07-01";

        String query = "select bbfrom from compaccpreferences where id =?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setObject(1, companyId);
        ResultSet rsb = stmt.executeQuery();
//        if (rsb.next()) {
//            bbfrom = rsb.getString("bbfrom");
//        }
        String storeId = "", locationid = "";
        String query1 = "select id from in_storemaster where company =? and isdefault=1"; //default warehose 
        PreparedStatement stmt1 = conn.prepareStatement(query1);
        stmt1.setObject(1, companyId);
        ResultSet rs = stmt1.executeQuery();
        if (rs.next()) {
            storeId = rs.getString("id");
        }
        String query2 = "select id from in_location where company =? and isdefault=1";  //default warehose 
        PreparedStatement stmt2 = conn.prepareStatement(query2);
        stmt2.setObject(1, companyId);
        ResultSet rs1 = stmt2.executeQuery();
        if (rs1.next()) {
            locationid = rs1.getString("id");
        }
        String productId = "", uomid = "", name = "";
        double currency = 0, price = 0, availablequantity = 0;
        String openingQtyString = "1000";
        double openingQty = Double.parseDouble(openingQtyString);

        //select all product from company   
        String queryproduct = "SELECT * FROM in_stockmovement WHERE transaction_date='2016-07-01' AND company=? AND transaction_type=1 "
                + " AND remark='Stock added through OPENING' GROUP BY product";
        PreparedStatement stmtp = conn.prepareStatement(queryproduct);
        stmtp.setObject(1, companyId);
        ResultSet rsp = stmtp.executeQuery();

        while (rsp.next()) {
            String invId = "";
            String smid = "";
            double qty = 0;
            String location = "";
            String id = rsp.getString("id");
            String product = rsp.getString("product");
            String store = rsp.getString("store");
            String stockuom = rsp.getString("stockuom");
            double quantity = rsp.getDouble("quantity");
            double priceperunit = rsp.getDouble("priceperunit");
            String costcenter = rsp.getString("costcenter");
            String remark = rsp.getString("remark");
            // double priceperunit = rsp.getDouble("priceperunit");

            String selSmDTL = "SELECT * FROM in_sm_detail WHERE stockmovement=?";

            PreparedStatement stmtp1 = conn.prepareStatement(selSmDTL);
            stmtp1.setObject(1, id);
            ResultSet rsp1 = stmtp1.executeQuery();
            while (rsp1.next()) {
                location = rsp1.getString("location");
                qty = rsp1.getDouble("quantity");
                smid = rsp1.getString("id");
            }

            String selInv = "SELECT * FROM inventory WHERE product=? AND updatedate='2016-07-01' and quantity > 500 and newinv='F' and company=?";

            PreparedStatement stmtp2 = conn.prepareStatement(selInv);
            stmtp2.setObject(1, product);
            stmtp2.setObject(2, companyId);
            ResultSet rsp2 = stmtp2.executeQuery();
            while (rsp2.next()) {
                invId = rsp2.getString("id");
            }
            String saId = java.util.UUID.randomUUID().toString();
            String insrtSA = "insert into in_stockadjustment (id,seqno,store,location,product,quantity,finalquantity,amount,bussinessdate,"
                    + " uom,company,adjustment_type,createdon,createdby,inventoryref) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            PreparedStatement stmtInv = conn.prepareStatement(insrtSA);
            stmtInv.setString(1, saId);
            stmtInv.setString(2, "SA0001");
            stmtInv.setString(3, store);
            stmtInv.setString(4, location);
            stmtInv.setString(5, product);
            stmtInv.setDouble(6, quantity);
            stmtInv.setDouble(7, quantity);
            stmtInv.setDouble(8, priceperunit);
            stmtInv.setString(9, "2016-07-01");
            stmtInv.setString(10, stockuom);
            stmtInv.setString(11, companyId);
            stmtInv.setString(12, "Stock IN");
            stmtInv.setString(13, "2016-07-01");
            stmtInv.setString(14, "");
            stmtInv.setString(15, invId);
            stmtInv.execute();
            stmtInv.close();

            String sadtlId = java.util.UUID.randomUUID().toString();
            String insrtSAdtl = "INSERT INTO in_sa_detail (id,stockadjustment,location,quantity,finalquantity) VALUES (?,?,?,?,?)";

            PreparedStatement stmtInv1 = conn.prepareStatement(insrtSAdtl);
            stmtInv1.setString(1, sadtlId);
            stmtInv1.setString(2, saId);
            stmtInv1.setString(3, location);
            stmtInv1.setDouble(4, qty);
            stmtInv1.setDouble(5, qty);

            stmtInv1.execute();
            stmtInv1.close();
            
            cnt++;
        }
        int count = cnt - 1;
        StringBuilder result = new StringBuilder("" + count).append(" Records added successfully.");
        out.println(result);
    } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());

    } finally {
        conn.close();
    }

%>
