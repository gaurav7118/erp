

<%@page import="com.krawler.common.util.Constants"%>
<%@page import="com.mysql.jdbc.exceptions.MySQLSyntaxErrorException"%>
<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%--<%!
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
%>--%>

<%

    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
//        String fieldLabel = request.getParameter("fieldlabel");
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
        int count = 0;
        ResultSet customrs = null;
        String message = "";

        customQuery = "SELECT id,colnum,moduleid,companyid FROM fieldparams WHERE fieldlabel='" + Constants.GSTProdCategory + "' AND companyid IN (SELECT companyid FROM company WHERE subdomain=?);";
        stmtquery = conn.prepareStatement(customQuery);
        stmtquery.setString(1, subDomain);
        customrs = stmtquery.executeQuery();

        while (customrs.next()) {
            String linetableid = "";
            String customgolbaltable = "";
            String maintable = "";
            String customgolbaltableid = "";
            String record = "";
            long column = 1;
            String refkey = "";
            String hsncode = "", saccode = "", productid = "";

            String moduleid = customrs.getString("moduleid");
            int module = Integer.parseInt(moduleid);
            String fieldid = customrs.getString("id");
            String companyid = customrs.getString("companyid");
            column = customrs.getLong("colnum");

            FileInputStream gstcustomdata = new FileInputStream("/home/krawler/Products.xls");
            BufferedReader in = new BufferedReader(new InputStreamReader(gstcustomdata));

            switch (module) {

                case 30:        //product
                case 42:        //group
                    customgolbaltable = "accproductcustomdata";
                    customgolbaltableid = "productId";
                    linetableid = "id";
                    maintable = "product";
                    refkey = "accproductcustomdataref";
                    break;
                case 1200:        //Entity
                    customgolbaltable = "";
                    customgolbaltableid = "";
                    linetableid = "";
                    maintable = "";
                    refkey = "";
                    break;

            }

            while ((record = in.readLine()) != null) {
                if (record != "") {
                    String[] recarrName = record.split(",");
                    hsncode = recarrName[0].trim();
                    saccode = recarrName[1].trim();
                    productid = recarrName[2].trim();
                    String value = "", fieldcomboId = "";
                    if (!StringUtil.isNullOrEmpty(hsncode)) {
                        value = hsncode;
                    } else if (!StringUtil.isNullOrEmpty(saccode)) {
                        value = saccode;
                    }
                    String hql = "select id from fieldcombodata where fieldid=? and value= ?";
                    stmtquery = conn.prepareStatement(hql);
                    stmtquery.setString(1, fieldid);
                    stmtquery.setString(2, value);
                    ResultSet fieldcombors = stmtquery.executeQuery();

                    if (fieldcombors.next()) {
                        fieldcomboId = fieldcombors.getString("id");
                    } else {
                        String uuid = UUID.randomUUID().toString();
                        String insertquery = "insert into fieldcombodata (id,value,fieldid) values(?,?,?)";
                        stmtquery = conn.prepareStatement(insertquery);
                        stmtquery.setString(1, uuid);
                        stmtquery.setString(2, value);
                        stmtquery.setString(3, fieldid);
                        int insertcount = stmtquery.executeUpdate();
                        if (insertcount > 0) {
                            fieldcomboId = uuid;
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(customgolbaltable)) {
                        String checkRecord = "select " + linetableid + " from " + maintable + " where productid=? and company=? limit 1";
                        stmtquery = conn.prepareStatement(checkRecord);
                        stmtquery.setString(1, productid);
                        stmtquery.setString(2, companyid);
                        ResultSet globaltabledata = stmtquery.executeQuery();
                        while (globaltabledata.next()) {
                            String pid = globaltabledata.getString(linetableid);
                            // check data in global custom table 
                            String q = "select * from " + customgolbaltable + " where " + customgolbaltableid + " =?";
                            stmtquery = conn.prepareStatement(q);
                            stmtquery.setString(1, pid);
                            ResultSet customtabledata = stmtquery.executeQuery();

                            if (customtabledata.next()) {
//                                String val = customtabledata.getString("col" + column);
                                if (!StringUtil.isNullOrEmpty(fieldcomboId)) {
                                    String q2 = "update " + customgolbaltable + " set col" + column + " =? where " + customgolbaltableid + "=?";
                                    stmtquery = conn.prepareStatement(q2);
                                    stmtquery.setString(1, fieldcomboId);
                                    stmtquery.setString(2, pid);
                                    stmtquery.executeUpdate();
                                    count++;
                                }

                            } else {
                                // insert data into global custom table
                                String query5 = "insert into " + customgolbaltable + " (" + customgolbaltableid + ",company,moduleId,col" + column + ") values(?,?,?,?)";
                                stmtquery = conn.prepareStatement(query5);
                                stmtquery.setString(1, pid);
                                stmtquery.setString(2, companyid);
                                stmtquery.setInt(3, module);
                                stmtquery.setString(4, fieldcomboId);
                                stmtquery.executeUpdate();
                                count++;
                            }

                        }

                    }

                }
            }

        }
        message = "<br>updated: " + count + "&nbsp;&nbsp;&nbsp; Products.<br>";
        out.print(message);

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