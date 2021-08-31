

<%@page import="com.mysql.jdbc.exceptions.MySQLSyntaxErrorException"%>
<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%!
    int udpateCustomRefId(Connection conn, String linetable, String refkey, String linetableid, String id) {
        int count = 0;
        try {
            String updateRef = "UPDATE " + linetable + " SET " + refkey + "=?  WHERE " + linetableid + "=?";
            PreparedStatement stmtquery = conn.prepareStatement(updateRef);
            stmtquery.setString(1, id);
            stmtquery.setString(2, id);
            count = stmtquery.executeUpdate();
        } catch (Exception ex) {

        }
        return count;
    }
%>

<%

    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        String fieldLabel = request.getParameter("fieldlabel");
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery;
        PreparedStatement pstmt;
        String customQuery = "";
        ResultSet customrs = null;
        String columnData = "";
        String linetableData = "";

        customQuery = "SELECT relatedmoduleid,colnum,companyid FROM fieldparams WHERE fieldlabel=? AND moduleid=30 AND companyid IN (SELECT companyid FROM company WHERE subdomain=?);";
        stmtquery = conn.prepareStatement(customQuery);
        stmtquery.setString(1, fieldLabel);
        stmtquery.setString(2, subDomain);
        customrs = stmtquery.executeQuery();
        while (customrs.next()) {
            String message = "";
            String linetableid = "";
            String linetable = "";
            String refkey = "";
            String productMasterTable = "";
            String DetailTable = "";
            String DetailTableid = "";
            String refid = "";
            String customdetailTable = "";
            int count = 0;
            String relatedModuleIds = customrs.getString("relatedmoduleid");
            String[] moduleIdStr = relatedModuleIds.split(",");

            for (int cnt = 0; cnt < moduleIdStr.length; cnt++) {
                int module = Integer.parseInt(moduleIdStr[cnt]);
                String fromColumn = customrs.getString("colnum");
                String toColumn = fromColumn;
                String company = customrs.getString("companyid");
                
                
                String columnNoQuery = "SELECT colnum FROM fieldparams WHERE fieldlabel=? AND moduleid=? AND companyid=?";
                stmtquery = conn.prepareStatement(columnNoQuery);
                stmtquery.setString(1, fieldLabel);
                stmtquery.setInt(2, module);
                stmtquery.setString(3, company);
                ResultSet colres = stmtquery.executeQuery();
                if(colres.next()){
                    toColumn = colres.getString("colnum");
                }
                
                switch (module) {

                    case 27:
                        customdetailTable = "dodetailscustomdata";
                        DetailTableid = "dodetailsid";
                        productMasterTable = "dodetailproductcustomdata";
                        refid = "doDetailID";
                        linetable = "dodetails";
                        linetableid = "id";
                        refkey = "accdodetailscustomdataref";
                        break;
                    case 18:        //PO
                        customdetailTable = "purchaseorderdetailcustomdata";
                        DetailTableid = "poDetailID";
                        productMasterTable = "podetailproductcustomdata";
                        refid = "poDetailID";
                        linetable = "podetails";
                        linetableid = "id";
                        refkey = "purchaseorderdetailcustomdataref";
                        break;
                    case 20:            //SO
                        customdetailTable = "salesorderdetailcustomdata";
                        DetailTableid = "soDetailID";
                        productMasterTable = "sodetailproductcustomdata";
                        refid = "soDetailID";
                        linetable = "sodetails";
                        linetableid = "id";
                        refkey = "salesorderdetailcustomdataref";
                        break;
                    case 2:     //CI
                    case 6:     //VI
                        customdetailTable = "accjedetailcustomdata";
                        DetailTableid = "jedetailId";
                        productMasterTable = "accjedetailproductcustomdata";
                        refid = "jedetailId";
                        linetable = "jedetail";
                        linetableid = "id";
                        refkey = "accjedetailcustomdataref";
                        break;
                }

                if (!customdetailTable.isEmpty()) {
                    columnData = "SELECT " + refid + ",recdetailId,col" + fromColumn + " from " + productMasterTable + " WHERE col" + fromColumn + " is not null AND moduleId=? AND company=?";
                    stmtquery = conn.prepareStatement(columnData);
                    stmtquery.setInt(1, module);
                    stmtquery.setString(2, company);
                    ResultSet globaltablers = stmtquery.executeQuery();

                    while (globaltablers.next()) {
                        String id = globaltablers.getString(refid);
                        String value = globaltablers.getString("col" + fromColumn);
                        String recdetailId = globaltablers.getString("recdetailId");
                        if (!StringUtil.isNullOrEmpty(value)) {

                            String query3 = "SELECT 1 FROM " + customdetailTable + " WHERE " + DetailTableid + "=?";
                            stmtquery = conn.prepareStatement(query3);
                            stmtquery.setString(1, id);
                            ResultSet rs = stmtquery.executeQuery();
                            if (rs.next()) {
                                //Update
                                String query4 = "update " + customdetailTable + " set col" + toColumn + " = ? where " + DetailTableid + "=? AND company=?";
                                stmtquery = conn.prepareStatement(query4);
                                stmtquery.setString(1, value);
                                stmtquery.setString(2, id);
                                stmtquery.setString(3, company);
                                stmtquery.executeUpdate();
                                count++;
                            } else {
                                // Insert 
                                if (module == 2 || module == 6) {
                                    String query5 = "insert into " + customdetailTable + " (jedetailId,company,moduleId,recdetailId,col" + toColumn + ") values(?,?,?,?,?)";
                                    stmtquery = conn.prepareStatement(query5);
                                    stmtquery.setString(1, id);
                                    stmtquery.setString(2, company);
                                    stmtquery.setInt(3, module);
                                    stmtquery.setString(4, recdetailId);
                                    stmtquery.setString(5, value);
                                    stmtquery.executeUpdate();

                                    udpateCustomRefId(conn, linetable, refkey, linetableid, id);            // Update Ref Id in Details table
                                    count++;
                                } else {
                                    String query5 = "insert into " + customdetailTable + " (" + DetailTableid + ",company,moduleId,col" + toColumn + ") values(?,?,?,?)";
                                    stmtquery = conn.prepareStatement(query5);
                                    stmtquery.setString(1, id);
                                    stmtquery.setString(2, company);
                                    stmtquery.setInt(3, module);
                                    stmtquery.setString(4, value);
                                    stmtquery.executeUpdate();

                                    udpateCustomRefId(conn, linetable, refkey, linetableid, id);
                                    count++;
                                }

                            }

                            String query2 = "UPDATE " + productMasterTable + " SET col" + fromColumn + "=null where " + refid + "=? AND company=?";
                            stmtquery = conn.prepareStatement(query2);
                            stmtquery.setString(1, id);
                            stmtquery.setString(2, company);
                            stmtquery.executeUpdate();
                        }
                    }
                }

                message = "<br>Module: " + module + "&nbsp;&nbsp;&nbsp; Records moved from Product level to Line level: " + count + "<br>";
                out.print(message);
            }
        }

        String deletefromProduct = "DELETE FROM fieldparams WHERE fieldlabel=? AND moduleid=30 AND companyid IN (SELECT companyid FROM company WHERE subdomain=?)";
        pstmt = conn.prepareStatement(deletefromProduct);
        pstmt.setString(1, fieldLabel);
        pstmt.setString(2, subDomain);
        int num = pstmt.executeUpdate();

    } catch (MySQLSyntaxErrorException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>