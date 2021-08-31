

<%@page import="com.krawler.common.admin.FieldComboData"%>
<%@page import="com.krawler.common.util.Constants"%>
<%@page import="com.mysql.jdbc.exceptions.MySQLSyntaxErrorException"%>
<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>


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
        String moduleName = "";

        customQuery = "SELECT id,colnum,moduleid,companyid FROM fieldparams WHERE fieldlabel='" + Constants.GSTProdCategory + "' AND companyid IN (SELECT companyid FROM company WHERE subdomain=?);";
        stmtquery = conn.prepareStatement(customQuery);
        stmtquery.setString(1, subDomain);
        customrs = stmtquery.executeQuery();

        while (customrs.next()) {
            String msg = "";
            long column = 1;

            String moduleid = customrs.getString("moduleid");
            int module = Integer.parseInt(moduleid);
            String fieldid = customrs.getString("id");
            String companyid = customrs.getString("companyid");
            column = customrs.getLong("colnum");

            String[] masterItemArray={"Product @ 0%","Product @ 5%","Product @ 12%","Product @ 18%","Product @ 28%"};

            for (int len=0;len < masterItemArray.length;len++) {
                if (masterItemArray[len] != "") {
                    
                    String value = "", fieldcomboId = "";
                    
                    value = masterItemArray[len];
                    String hql = "select id from fieldcombodata where fieldid=? and value= ?";
                    stmtquery = conn.prepareStatement(hql);
                    stmtquery.setString(1, fieldid);
                    stmtquery.setString(2, value);
                    ResultSet fieldcombors = stmtquery.executeQuery();

                    if (fieldcombors.next()) {
                        fieldcomboId = fieldcombors.getString("id");
                    } else {
                        String uuid = UUID.randomUUID().toString();
                        String insertquery = "insert into fieldcombodata (id,value,fieldid,valuetype) values(?,?,?,?)";
                        stmtquery = conn.prepareStatement(insertquery);
                        stmtquery.setString(1, uuid);
                        stmtquery.setString(2, value);
                        stmtquery.setString(3, fieldid);
                        if (value.equalsIgnoreCase(FieldComboData.TaxClass_Exempted)) {
                            stmtquery.setInt(4, FieldComboData.ValueTypeMap.get(FieldComboData.TaxClass_Exempted));
                        } else if (value.equalsIgnoreCase(FieldComboData.TaxClass_Non_GST_Product)) {
                            stmtquery.setInt(4, FieldComboData.ValueTypeMap.get(FieldComboData.TaxClass_Non_GST_Product));
                        } else if (value.equalsIgnoreCase(FieldComboData.TaxClass_ZeroPercenatge)) {
                            stmtquery.setInt(4, FieldComboData.ValueTypeMap.get(FieldComboData.TaxClass_Percenatge));
                        }else{
                            stmtquery.setInt(4, 0);
                        }
                        int insertcount = stmtquery.executeUpdate();
                        if (insertcount > 0) {
                            fieldcomboId = uuid;
                        }
                    }


                }
            }
            moduleName += module +",";

        }
        message = "<br>Master items added for modules: &nbsp;&nbsp;" + moduleName + "<br>";
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