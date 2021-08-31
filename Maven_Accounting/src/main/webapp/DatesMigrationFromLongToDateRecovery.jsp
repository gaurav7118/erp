
<%@page import="com.krawler.utils.json.base.JSONException"%>
<%@page import="java.text.ParseException"%>
<%@page import="com.krawler.esp.utils.ConfigReader"%>
<%@page import="com.krawler.utils.json.base.JSONObject"%>
<%@page import="com.krawler.utils.json.base.JSONArray"%>
<%@page import="com.krawler.common.util.Constants"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="com.krawler.common.util.StringUtil"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.*"%>
<%--<%@page import="java.sql.Date"%>--%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.logging.Level"%>
<%@page import="java.util.logging.Logger"%>
<%
    Connection con = null;
    try {
        //SCRIPT URL : http://<app-url>/DatesMigrationFromLongToDate.jsp?serverip=?&dbname=?&username=?&password=?    &subdomain=?

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        //String subdomain = "";//request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username)) {//|| StringUtil.isNullOrEmpty(password)
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        String errorOccured = "";
        String driver = "com.mysql.jdbc.Driver";
        String query1 = "", companyid = "", subdomain = "";
        String fieldParamQuery = "";
        boolean isError = false;
        int totalCount = 0, updateCount = 0, companycount = 0;
        final int fieldId = 3;

        PreparedStatement DbCheckPstn = null, RBUpdatePstn = null, ReportBldrPstn = null, pst1 = null, relatedModulePstn = null, relatedModulePstn1 = null, pst2 = null, UsrTZpstn = null, cmpTZpstn = null, pst3 = null, pst4 = null, finalPstn = null, columnPstn = null;
        ResultSet DbCheckRst = null, RBUpdateRst = null, ReportBldrRst = null, rst1 = null, rst2 = null, relatedModulerst = null, relatedModulerst1 = null, rst3 = null, UsrTZrst = null, cmpTZrst = null, cprst = null, columnRst = null;
        PreparedStatement expensePODetailpstn = null, openingBopeningPstn = null;
        Class.forName(driver).newInstance();
        out.println("<br><br>Execution Started @ " + new java.util.Date() + "<br><br>");

        con = DriverManager.getConnection(connectString, username, password);
        String dbCheck = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE "
                + "TABLE_SCHEMA = '" + dbname + "' AND TABLE_NAME = 'salesordercustomdata'"
                + " AND COLUMN_NAME = 'col3001'";
        DbCheckPstn = con.prepareStatement(dbCheck);
        DbCheckRst = DbCheckPstn.executeQuery();
        if (!DbCheckRst.next()) {
            throw new Exception("Please Execute DB Changes First  ");
        }

        File dateFile = new File(ConfigReader.getinstance().get("DocStorePath0") + "DateFile.txt");
        dateFile.createNewFile();
        FileOutputStream is = new FileOutputStream(dateFile);
        OutputStreamWriter osw = new OutputStreamWriter(is);
        Writer w = new BufferedWriter(osw);
        String fileContent = "";

        String rptBldrQuery = "SELECT id,ispivotreport,reportjson,name FROM reportmaster WHERE isdefaultreport='F'";
        ReportBldrPstn = con.prepareStatement(rptBldrQuery);
        ReportBldrRst = ReportBldrPstn.executeQuery();
        JSONObject reportJobj = null;
        //JSONArray modifiedJsonArray=new JSONArray();
        while (ReportBldrRst.next()) {

            String reportJsonId = ReportBldrRst.getString("id");
            //String isPivot = ReportBldrRst.getString("ispivotreport");
            JSONArray jsonArray = new JSONArray();
            if (!StringUtil.isNullOrEmpty(ReportBldrRst.getString("reportjson"))) {

                try {
                    reportJobj = new JSONObject(ReportBldrRst.getString("reportjson").replaceAll("\"T\"", "'T'"));
                    if (reportJobj != null) {
                        jsonArray = reportJobj.getJSONArray("columnConfig");
                    } else {
                        continue;
                    }
                } catch (Exception e) {
                    fileContent = e.toString();
                    fileContent = fileContent+"\n Problem in report(id) :-  " + reportJsonId + "";
                    w.write(fileContent);
                    continue;
                }
                boolean modified = false;
                for (int j = 0; j < jsonArray.length(); j++) {

                    JSONObject jObject = jsonArray.getJSONObject(j);
                    if (jObject.optBoolean("customfield", false) && (jObject.getInt("xtype") == 3)) {
                        String reportBuilderfieldId = jObject.getString("id");
                        fieldParamQuery = "select * from fieldparams where id='" + reportBuilderfieldId + "'";
                        pst2 = con.prepareStatement(fieldParamQuery);                                               
                        rst2 = pst2.executeQuery();
                        while (rst2.next()) {
                            //out.print(rst2.getString("fieldlabel")+" used in :- "+ReportBldrRst.getString("name")+"<br>");
                            String columnNum = "col" + rst2.getString("colnum");
                            jsonArray.getJSONObject(j).put("reftabledatacolumn", columnNum);
                            modified = true;
                        }
                    }

                }
                if (modified) {
                    reportJobj.put("columnConfig", jsonArray);
                    String reportJson = reportJobj.toString();
                    String UpdateRB = "UPDATE reportmaster set reportjson=? WHERE id= ? ";
                    RBUpdatePstn = con.prepareStatement(UpdateRB);
                    RBUpdatePstn.setString(1, reportJson);
                    RBUpdatePstn.setString(2, reportJsonId);
                    int i1 = RBUpdatePstn.executeUpdate();
                }
            }
        }
        w.close();

        System.out.println("Script Executed Successfully...\n");
        out.println("<b><center>Script Executed Successfully...</center><b><br>");
    } catch (Exception ex) {
        ex.printStackTrace();
        out.print("Exception occured : ");
        out.print(ex.toString());
    } finally {
        if (con != null) {
            try {
                con.close();
                out.println("Connection Closed....<br/>");
                //Execution Ended :
                out.println("<br><br>Execution Ended @ " + new java.util.Date() + "<br><br>");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//finally
%>