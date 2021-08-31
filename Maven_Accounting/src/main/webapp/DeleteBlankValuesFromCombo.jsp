<%@page import="org.hibernate.hql.ast.tree.DeleteStatement"%>
<%@page import="java.sql.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%@page import="com.krawler.common.util.StringUtil"%>

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
        int module = -1;
        if(request.getParameter("module")!=null) {
            module = Integer.parseInt(request.getParameter("module"));
        }
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not provided all parameters (parameter are: serverip,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        PreparedStatement stmtquery;
        String company = "";
        String customquery = "";
        String jedetailId = "";
        String journalEntryId = "";
        ResultSet custrs;
        ResultSet fieldcombors;
        String fieldId = "";
        int count = 0;
        long column = 1;

        String DetailTableid = "";
        String customgolbaltable = "";
        String customgolbaltableid = "";
        String customdetailTable = "";

        switch (module) {
            case 2:     //CI
                customgolbaltable = "accjecustomdata";
                customgolbaltableid = "journalentryId";
                customdetailTable = "accjedetailcustomdata";
                DetailTableid = "jedetailId";
                break;
            case 16:    //RP
                customgolbaltable = "accjecustomdata";
                customgolbaltableid = "journalentryId";
                customdetailTable = "accjedetailcustomdata";
                DetailTableid = "jedetailId";
                break;
            case 24:        //JE
                customgolbaltable = "accjecustomdata";
                customgolbaltableid = "journalentryId";
                customdetailTable = "accjedetailcustomdata";
                DetailTableid = "jedetailId";
                break;

        }
        if (module!=-1) {
//             get fieldid and column no for respective dimension
            customquery = "select id,colnum,companyid from fieldparams where fieldlabel=? and companyid in (select companyid from company where subdomain=?) and moduleid=?";
            stmtquery = conn.prepareStatement(customquery);
            stmtquery.setString(1, fieldlabel);
            stmtquery.setString(2, subDomain);
            stmtquery.setInt(3, module);
            custrs = stmtquery.executeQuery();

            while (custrs.next()) {
                fieldId = custrs.getString("id");
                column = custrs.getLong("colnum");
                company = custrs.getString("companyid");

                if (!StringUtil.isNullOrEmpty(fieldId)) {
                    customquery = "select id from fieldcombodata where fieldid=? AND value=''";    //get all record for module
                    stmtquery = conn.prepareStatement(customquery);
                    stmtquery.setString(1, fieldId);
                    fieldcombors = stmtquery.executeQuery();
                    while (fieldcombors.next()) {
                        String valueid = fieldcombors.getString("id");
                        count++;
                        if (!StringUtil.isNullOrEmpty(valueid)) {
                            //for line level dimension......Check if entry present in accjedetailcustomdata
                            String lineLevelCustomquery = "SELECT " + DetailTableid + " FROM " + customdetailTable + " WHERE col" + column + "=? AND company=?";
                            stmtquery = conn.prepareStatement(lineLevelCustomquery);
                            stmtquery.setString(1, valueid);
                            stmtquery.setString(2, company);
                            ResultSet lineLevelrs = stmtquery.executeQuery();
                            while (lineLevelrs.next()) {
                                jedetailId = lineLevelrs.getString(DetailTableid);
                                String insertQuery = "update " + customdetailTable + " set col" + column + "=NULL WHERE jedetailId=?";
                                PreparedStatement preparedStatement = conn.prepareStatement(insertQuery);
                                preparedStatement.setString(1, jedetailId);
                                preparedStatement.executeUpdate();
                            }

                            //For Global level dimension........Check if entry present in accjecustomdata
                            String globalLevelCustomquery = "SELECT " + customgolbaltableid + " FROM " + customgolbaltable + " WHERE col" + column + "=? AND company=?";
                            stmtquery = conn.prepareStatement(globalLevelCustomquery);
                            stmtquery.setString(1, valueid);
                            stmtquery.setString(2, company);
                            ResultSet globalLevelrs = stmtquery.executeQuery();
                            while (globalLevelrs.next()) {
                                journalEntryId = globalLevelrs.getString(customgolbaltableid);
                                String updateQuery = "update " + customgolbaltable + " set col" + column + "=NULL WHERE journalentryId=?";
                                PreparedStatement preparedStatement = conn.prepareStatement(updateQuery);
                                preparedStatement.setString(1, journalEntryId);
                                preparedStatement.executeUpdate();
                            }
                        }

                        //delete blank value from fieldcombodata
                        String deletequery = "DELETE FROM fieldcombodata where id=?";
                        PreparedStatement statement = conn.prepareStatement(deletequery);
                        statement.setString(1, valueid);
                        statement.executeUpdate();
                    }
                }
            }
        }

        System.out.println("Duplicate data for following records");
        message += "\nSubdomain: " + subDomain + ", Blank entry count=" + count + ", Field=" + fieldlabel + ", ModuleId=" + module;

    } catch (Exception e) {
        e.printStackTrace();
        out.print(e.toString());
    } finally {
        if (conn != null) {
            conn.close();
        }
        out.print(message);
    }
%>