

<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

<%!
    String checkParentRecordExist(Connection conn, String refParentId, String refField, String curentField) {
        String returnParentId = "";
        try {
            String checkValueQuery = "SELECT value, parent from fieldcombodata  where id = ?";
            PreparedStatement stmtquery = conn.prepareStatement(checkValueQuery);
            stmtquery.setString(1, refParentId);
            ResultSet checkvaluers = stmtquery.executeQuery();
            if (checkvaluers.next()) {
                String checkValueInCurrentModule = "SELECT id from fieldcombodata  where fieldid = ? and value=?";
                stmtquery = conn.prepareStatement(checkValueInCurrentModule);
                stmtquery.setString(1, curentField);
                stmtquery.setString(2, checkvaluers.getString("value"));
                ResultSet checkParent = stmtquery.executeQuery();
                if (checkParent.next()) {
                    returnParentId = checkParent.getString("id");
//                        if (checkParent.getInt("cnt") <= 0) {
                    // Make parent Id entry in current module and check parent's parent value 

//                        } else {
//                            
//                        }
                } else {
                    String hql = "INSERT into fieldcombodata (id, value, fieldid) value(?,?,?)";

                    String id = UUID.randomUUID().toString();
                    stmtquery = conn.prepareStatement(hql);
                    stmtquery.setString(1, id);
                    stmtquery.setString(2, checkvaluers.getString("value"));
                    stmtquery.setString(3, curentField);

                    int insertrs = stmtquery.executeUpdate();

                    if (checkvaluers.getObject("parent") != null) {
                        String isParentIDGenerated = checkParentRecordExist(conn, checkvaluers.getString("parent"), refField, curentField);

                        if (!StringUtil.isNullOrEmpty(isParentIDGenerated)) {
                            String query1 = "update fieldcombodata set parent=? where id=? ";
                            stmtquery = conn.prepareStatement(query1);
                            stmtquery.setString(1, isParentIDGenerated);
                            stmtquery.setString(2, id);
                            stmtquery.executeUpdate();
                        }
                    }
                    returnParentId = id;
                }
            }
        } catch (Exception ex) {

        }

        return returnParentId;
    }
%>

<%
    String message = "";
    Connection conn = null;
    try {
        String serverip = request.getParameter("serverip");
        String port = "3306";
        String dbName = request.getParameter("dbname");
        String userName = request.getParameter("username");
        String password = request.getParameter("password");
        String subDomain = request.getParameter("subdomain");
        String fieldlabel = request.getParameter("fieldlabel");
        String moduleArray = request.getParameter("module");
//        int module = Integer.parseInt(request.getParameter("module"));
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery;
        int i = 0;
        int count = 0;

        String[] moduleIdArray = moduleArray.split(",");
        while (moduleIdArray.length > count) {
            
            int module = Integer.parseInt(moduleIdArray[count]);
            String customquery = "";
            String comboDataQuery = "";
            String checkValueQuery = "";
            String referenceModuleId = "";
            ResultSet resultset = null;
            ResultSet checkvaluers = null;
            ResultSet moduleIdrs = null;
            ResultSet insertrs = null;
            ResultSet datars = null;
            String dataQuery = "";

            customquery = "SELECT id,moduleid FROM fieldparams WHERE fieldlabel=? and companyid IN(SELECT companyid from company WHERE subdomain=?)";
            stmtquery = conn.prepareStatement(customquery);
            stmtquery.setString(1, fieldlabel);
            stmtquery.setString(2, subDomain);
            resultset = stmtquery.executeQuery();

            comboDataQuery = "SELECT id FROM fieldparams WHERE fieldlabel=? and moduleid=? and companyid IN(SELECT companyid from company WHERE subdomain=?)";
            stmtquery = conn.prepareStatement(comboDataQuery);
            stmtquery.setString(1, fieldlabel);
            stmtquery.setInt(2, module);
            stmtquery.setString(3, subDomain);
            moduleIdrs = stmtquery.executeQuery();
            if (moduleIdrs.next()) {
                referenceModuleId = moduleIdrs.getString("id");
            }

            while (resultset.next()) {

                if (resultset.getInt("moduleid") != module) {
                    String missingValue = "";

                    dataQuery = "SELECT value, parent from fieldcombodata  where fieldid = ?";
                    stmtquery = conn.prepareStatement(dataQuery);
                    stmtquery.setString(1, referenceModuleId);
                    datars = stmtquery.executeQuery();

                    while (datars.next()) {
                        String value = datars.getString("value");
                        String parentId = datars.getString("parent");
                        checkValueQuery = "SELECT count(id) as cnt from fieldcombodata  where fieldid = ? and value=?";
                        stmtquery = conn.prepareStatement(checkValueQuery);
                        stmtquery.setString(1, resultset.getString("id"));
                        stmtquery.setString(2, value);
                        checkvaluers = stmtquery.executeQuery();
                        if (checkvaluers.next()) {
                            if (checkvaluers.getInt("cnt") <= 0) {
                                missingValue += "<br>" + value + ",";

                                String hql = "INSERT into fieldcombodata (id, value, fieldid) value(?,?,?)";

                                String id = UUID.randomUUID().toString();
                                stmtquery = conn.prepareStatement(hql);
                                stmtquery.setString(1, id);
                                stmtquery.setString(2, value);
                                stmtquery.setString(3, resultset.getString("id"));

                                stmtquery.executeUpdate();

//                            String parentQuery = "SELECT parent from fieldcombodata  where fieldid = ? and value=?";
//                            stmtquery = conn.prepareStatement(parentQuery);
//                            stmtquery.setString(1, referenceModuleId);
//                            stmtquery.setString(2, value);
//                            ResultSet parentrs = stmtquery.executeQuery();
                                if (parentId != null) {
                                    String createdParentId = checkParentRecordExist(conn, parentId, referenceModuleId, resultset.getString("id"));

                                    String query1 = "update fieldcombodata set parent=? where id=? ";
                                    stmtquery = conn.prepareStatement(query1);
                                    stmtquery.setString(1, createdParentId);
                                    stmtquery.setString(2, id);
                                    stmtquery.executeUpdate();

                                }

                            }
                        }
                    }
                    message = "<br>Subdomain: " + subDomain + ", Reference Module: " + module + ", Values Missing In Module: " + resultset.getInt("moduleid") + ", Missing Values: " + missingValue + "<br>";
                    out.print(message);

                }

            }
            count++;
        }

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
    }
%>