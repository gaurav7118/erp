
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
        //SCRIPT URL :htttp://localhost:8080/Accounting/DuplicateEntitiesAndProductTaxClassRemoval.jsp?serverip=192.168.0.65&dbname=cardpaynew&username=krawler&subdomain=vinodtest&password=krawler

        String serverip = request.getParameter("serverip");
        String dbname = request.getParameter("dbname");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String reqSubdomain = request.getParameter("subdomain");
        //String subdomain = "";//request.getParameter("subdomain");
        String connectString = "jdbc:mysql://" + serverip + ":3306/" + dbname;
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(dbname) || StringUtil.isNullOrEmpty(username)) {//|| StringUtil.isNullOrEmpty(password)
            throw new Exception(" You have not provided all parameters (Parameter are: serverip, dbname, username, password) in url. so please provide all these parameters correctly. ");
        }
        String errorOccured="";
        String driver = "com.mysql.jdbc.Driver";
        String query = "",fieldparamQuery="",fieldComboQuery="", companyid = "",updateOpeningQuery="",baseTableSelectQuery="",updateBaseTableQuery="";
        String data="",openingTable="",openingtableid="",basetable="",basetableid="",customDataRefCol="";
        String subdomain=""; String country=""; String setUpDone="";
        boolean isError = false;                   
        Class.forName(driver).newInstance();        
        DateFormat df = new SimpleDateFormat("MMMM d, yyyy");
        DateFormat df1 = new SimpleDateFormat("MMMM d, yyyy");
        //Execution Started :
        out.println("<br><br>Execution Started @ " + new java.util.Date() + "<br><br>");

        con = DriverManager.getConnection(connectString, username, password);
        PreparedStatement cmpanyPstn=null,fieldParamsPstn=null;
        ResultSet cmpanyRst=null,fieldParamsRst;
        ArrayList<String> fieldParamEntityID=new ArrayList<String>();
        ArrayList<String> fieldParamProductTaxClassID=new ArrayList<String>();

        File dateFile = new File(ConfigReader.getinstance().get("DocStorePath0") + "deletefile.txt");
        dateFile.createNewFile();
        FileOutputStream is = new FileOutputStream(dateFile);
        OutputStreamWriter osw = new OutputStreamWriter(is);
        Writer w = new BufferedWriter(osw);
        String fileContent = "";
        if (!StringUtil.isNullOrEmpty(reqSubdomain)) {
                query = "select c.companyid,c.country,c.subdomain,cp.setupdone from company c INNER JOIN compaccpreferences cp ON  c.companyid=cp.id  where subdomain='"+reqSubdomain+"'";
        }else{
                query = "select c.companyid,c.country,c.subdomain,cp.setupdone from company c inner join country cr on cr.id=c.country INNER JOIN compaccpreferences cp on c.companyid=cp.id  where cr.id in ('105','244')";
        }                
        cmpanyPstn = con.prepareStatement(query);
        cmpanyRst = cmpanyPstn.executeQuery();                                                       
        while(cmpanyRst.next()){
            companyid=cmpanyRst.getString("companyid");   
            subdomain=cmpanyRst.getString("subdomain");   
            String modules[]="2,6,10,12,14,16,24,27,41,51,67,28,40,57,18,63,90,20,36,50,1114,23,89,22,65,29,53,68,98,31,59,96,32,87,33,88,39,58,38,52,93,30,42,34,35,64,25,26,79,121,95,1004,1001,92,1002,1003,1101,1102,1103,1105,1106,1107,1104,1116,1200".split(",");
            for(String module:modules){
                int moduleid=Integer.parseInt(module);
                w.write("Execution Started For "+subdomain+"\n");
                out.println("Execution Started For "+subdomain+"\n");
                
                //============for "Entity" =========
                fieldparamQuery="select id from fieldparams where moduleid='"+moduleid+"' and companyid='"+companyid+"' and fieldlabel='Entity' ";
                fieldParamsPstn=con.prepareStatement(fieldparamQuery);
                fieldParamsRst=fieldParamsPstn.executeQuery();
                while(fieldParamsRst.next()){
                    String entityId=fieldParamsRst.getString("id");
                    fieldParamEntityID.add(entityId);                    
                }            
                if (fieldParamEntityID.size() >= 1) {
                        fieldParamEntityID.remove(0);
                    }                                             
                for (String id:fieldParamEntityID) {
                        String deleteQuery = "delete from fieldcombodata where fieldid='" + id + "' ";
                        fieldParamsPstn=con.prepareStatement(deleteQuery);
                        try {
                            fieldParamsPstn.executeUpdate();
                        }catch(Exception e){
                           e.printStackTrace();       
                        }
                    }
                for (String id:fieldParamEntityID) {
                        String deleteQuery = "delete from fieldparams where id='" + id + "' ";
                        fieldParamsPstn=con.prepareStatement(deleteQuery);
                        try {
                            fieldParamsPstn.executeUpdate();
                        }catch(Exception e){
                           e.printStackTrace();       
                        }
                    }
                fieldParamEntityID.clear();
                
                //=============For "Product Tax Class"=========================
                
                fieldparamQuery="select id from fieldparams where moduleid="+moduleid+" and companyid='"+companyid+"' and fieldlabel='Product Tax Class' ";
                fieldParamsPstn=con.prepareStatement(fieldparamQuery);
                fieldParamsRst=fieldParamsPstn.executeQuery();
                while(fieldParamsRst.next()){
                    String entityId=fieldParamsRst.getString("id");
                    fieldParamProductTaxClassID.add(entityId);                    
                }
                if(fieldParamProductTaxClassID.size()>=1){
                    fieldParamProductTaxClassID.remove(0);
                }                                                
                for (String id:fieldParamProductTaxClassID) {
                        String deleteQuery = "delete from fieldcombodata where fieldid='" + id + "' ";
                        fieldParamsPstn=con.prepareStatement(deleteQuery);
                        try{
                           fieldParamsPstn.executeUpdate();
                        }catch(Exception e){
                           e.printStackTrace();       
                        }
                    }
                for (String id:fieldParamProductTaxClassID) {
                        String deleteQuery = "delete from fieldparams where id='" + id + "' ";
                        fieldParamsPstn=con.prepareStatement(deleteQuery);
                        try{
                           fieldParamsPstn.executeUpdate();
                        }catch(Exception e){
                           e.printStackTrace();       
                        }
                    }
                fieldParamProductTaxClassID.clear();
                
                
                
            }
            out.print("Execution Fineshed For "+subdomain+"\n");
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

