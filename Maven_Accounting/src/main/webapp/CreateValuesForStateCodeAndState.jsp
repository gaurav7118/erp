
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
        String query = "",fieldparamQuery="",fieldComboQuery="", companyid = "",insertFieldComboDataQuery="",baseTableSelectQuery="",updateBaseTableQuery="",duplicateCheckQuery="";
        String data="",openingTable="",openingtableid="",basetable="",basetableid="",customDataRefCol="",dupCheck="";
        String subdomain=""; String country=""; String setUpDone="";
        boolean isError = false;                   
        Class.forName(driver).newInstance();        
        DateFormat df = new SimpleDateFormat("MMMM d, yyyy");
        DateFormat df1 = new SimpleDateFormat("MMMM d, yyyy");
        //Execution Started :
        out.println("<br><br>Execution Started @ " + new java.util.Date() + "<br><br>");

        con = DriverManager.getConnection(connectString, username, password);
        PreparedStatement cmpanyPstn=null,fieldParamsPstn=null,duplicateCheckPstn=null ,insertPstn=null,statePstn=null;
        ResultSet cmpanyRst=null,fieldParamsRst,duplicateCheckRst=null,insertRst=null,stateRst=null;
        ArrayList<String> fieldParamEntityID=new ArrayList<String>();
        ArrayList<String> fieldParamProductTaxClassID=new ArrayList<String>();
        HashMap<String,String> stateValues=new HashMap<String,String>();
        String stateQuery="SELECT statename,statecode FROM defaultstatevalues";
        statePstn=con.prepareStatement(stateQuery);
        stateRst=statePstn.executeQuery();
        while(stateRst.next()){
            String stateName=stateRst.getString("statename");
            String stateCode=stateRst.getString("statecode");
            stateValues.put(stateName,stateCode);
        }

        File dateFile = new File(ConfigReader.getinstance().get("DocStorePath0") + "deletefile.txt");
        dateFile.createNewFile();
        FileOutputStream is = new FileOutputStream(dateFile);
        OutputStreamWriter osw = new OutputStreamWriter(is);
        Writer w = new BufferedWriter(osw);
        String fileContent = "";
//        String stateName[]= "Andaman and Nicobar Islands,Andhra Pradesh,Andhra Pradesh (New),Arunachal Pradesh,Assam,Bihar,Chandigarh,Chattisgarh,Dadra and Nagar Haveli,Daman and Diu,Delhi,Goa,Gujarat,Haryana,Himachal Pradesh,Jammu and Kashmir,Jharkhand,Karnataka,Kerala,Lakshadweep Islands,Madhya Pradesh,Maharashtra,Manipur,Meghalaya,Mizoram,Nagaland,Odisha,Pondicherry,Punjab,Rajasthan,Sikkim,Tamil Nadu,Telangana,Tripura,Uttar Pradesh,Uttarakhand,West Bengal".split(",");        
//        ArrayList<String> statesToInsert=new ArrayList<String>();
        HashMap<String,String> statesToInsert=new HashMap<String,String>();
        String states[]="01,02,03,04,05,06,07,08,09".split(",");
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
            out.print("Execution Started For "+subdomain+"<br/>");
            fieldparamQuery="SELECT id,moduleid,fieldlabel FROM fieldparams WHERE companyid='"+companyid+"' and (fieldlabel='State') ";
            fieldParamsPstn=con.prepareStatement(fieldparamQuery);
            fieldParamsRst=fieldParamsPstn.executeQuery();
            while(fieldParamsRst.next()){
                statesToInsert.clear();               
                for (Map.Entry<String, String> stateFromDB : stateValues.entrySet()) { 
                     statesToInsert.put(stateFromDB.getKey(),stateFromDB.getValue());
                }                               
                String fieldParamID=fieldParamsRst.getString("id"); 
                String moduleId=fieldParamsRst.getString("moduleid"); 
                String fieldlabel=fieldParamsRst.getString("fieldlabel"); 
                duplicateCheckQuery="select id,value from fieldcombodata where fieldid='"+ fieldParamID +"' ";
                duplicateCheckPstn=con.prepareStatement(duplicateCheckQuery);
                duplicateCheckRst=duplicateCheckPstn.executeQuery();
                while(duplicateCheckRst.next()){
                    String existingState=duplicateCheckRst.getString("value");
//                    for(String state:stateName){    
                    for (Map.Entry<String, String> stateFromDB : stateValues.entrySet()) {  
                        String stateToCheckDuplicate=stateFromDB.getKey();
                        if(stateToCheckDuplicate.equalsIgnoreCase(existingState)){                            
                            statesToInsert.remove(stateToCheckDuplicate);                            
                        }
                    }                    
                }
//                for(String state:statesToInsert){   
                for (Map.Entry<String, String> stateFromDB : statesToInsert.entrySet()) {    
//                         dupCheck="select id from fieldcombodata where fieldid='"+ fieldParamID +"' and value";
                         String uuid = UUID.randomUUID().toString();
                         String value=stateFromDB.getKey();
                         String itemDescription=stateFromDB.getValue();
//                         char activatedeactivatedimensionvalue='T';
                         insertFieldComboDataQuery="insert into fieldcombodata (id,value,fieldid,activatedeactivatedimensionvalue,itemdescription) values( '"+uuid+"','"+value+"','"+fieldParamID+"','T','"+itemDescription+"' )";
                         insertPstn = con.prepareStatement(insertFieldComboDataQuery);
                         insertPstn.execute();
                }
                                                                
            }                        
            out.print("Execution Fineshed For "+subdomain+"<br/>");
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

