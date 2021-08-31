
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
        String query = "",masterItemQuery="",defaultMasterItemQuery="", companyid = "",insertFieldComboDataQuery="",baseTableSelectQuery="",updateBaseTableQuery="",duplicateCheckQuery="";
        String data="",openingTable="",openingtableid="",basetable="",basetableid="",customDataRefCol="",dupCheck="";
        String subdomain=""; String country=""; String setUpDone="";
        boolean isError = false;                   

        out.println("<br><br>Execution Started @ " + new java.util.Date() + "<br><br>");

        con = DriverManager.getConnection(connectString, username, password);
        PreparedStatement cmpanyPstn=null,masterItemPstn=null,duplicateCheckPstn=null ,insertPstn=null,defaultMasterItemPstn=null,deletePstn=null,regTypePstn=null,venRegTypePstn=null,customerPstn=null,updatePstn=null,vendorPstn=null;
        ResultSet cmpanyRst=null,masterItemRst,duplicateCheckRst=null,insertRst=null,defaultMasterItemRst=null,regTypeRst=null,venRegTypeRst=null,customerRst=null,vendorRst=null;
        String newValue="NA";
        ArrayList<String> presentRecords=new ArrayList<String>(); 
        ArrayList<String> affectedSubdomains=new ArrayList<String>(); 
//======================fetching Companies with Duplicate Entries==========================================        
        duplicateCheckQuery="SELECT subdomain,companyid FROM company WHERE companyid in (SELECT company from (select count(*) as count, company FROM masteritem WHERE mastergroup=63 GROUP BY company) as temp where temp.count > 3)";
        duplicateCheckPstn = con.prepareStatement(duplicateCheckQuery);
        duplicateCheckRst = duplicateCheckPstn.executeQuery();
        while(duplicateCheckRst.next()){
            affectedSubdomains.add(duplicateCheckRst.getString("companyid"));
        }
        
                
        HashMap<String,String> custVendTypeRequired=new HashMap<String,String>();
        ArrayList<String> custVendTypeduplicate=new ArrayList<String>();
        HashMap<String,String> gstRegTypeRequired=new HashMap<String,String>();
        ArrayList<String> gstRegTypeDuplicate=new ArrayList<String>();
        
        ArrayList<String> usedIds=new ArrayList<String>();
        HashMap<String,String> allrecords=new HashMap<String,String>();
        for(String companyid1:affectedSubdomains){                      
          String masterItemquery="select id,value,masterGroup from masteritem where company='"+companyid1+"' and (masterGroup=63 or masterGroup=62)";
          duplicateCheckPstn=con.prepareStatement(masterItemquery);
          duplicateCheckRst=duplicateCheckPstn.executeQuery();
            while (duplicateCheckRst.next()) {
                                        
                    if (duplicateCheckRst.getString("masterGroup").equals("63")) {
                        if (!custVendTypeRequired.containsKey(duplicateCheckRst.getString("value"))) {
                            custVendTypeRequired.put(duplicateCheckRst.getString("value"), duplicateCheckRst.getString("id"));
                        } else {
                            custVendTypeduplicate.add(duplicateCheckRst.getString("id"));
                        }
                    }

                    if (duplicateCheckRst.getString("masterGroup").equals("62")) {
                        if (!gstRegTypeRequired.containsKey(duplicateCheckRst.getString("value"))) {
                            gstRegTypeRequired.put(duplicateCheckRst.getString("value"), duplicateCheckRst.getString("id"));
                        } else {
                            gstRegTypeDuplicate.add(duplicateCheckRst.getString("id"));
                        }
                    }
                }
          
          
               String custRegType="SELECT c.id,c.gstregistrationtype,m.value FROM customer c INNER JOIN masteritem m ON c.gstregistrationtype=m.id  WHERE  c.gstregistrationtype is not NULL AND m.company='"+companyid1+"'";
               regTypePstn=con.prepareStatement(custRegType);          
               regTypeRst=regTypePstn.executeQuery();
               while(regTypeRst.next()){
                   String id=regTypeRst.getString("id");
                   String usedregTypeId=regTypeRst.getString("gstregistrationtype");
                   String value=regTypeRst.getString("value");
                   String preservedValue=(String)gstRegTypeRequired.get(value);
                   
                   if(!usedregTypeId.equals(preservedValue)){
                       String updateRegType="update customer set gstregistrationtype='"+preservedValue+"' where id='"+id+"' ";
                       updatePstn=con.prepareStatement(updateRegType);
                       int i=updatePstn.executeUpdate();
                   }                   
               }
               
               String vendorRegTypeQuery="SELECT v.id,v.gstregistrationtype,m.value FROM vendor v INNER JOIN masteritem m ON v.gstregistrationtype=m.id  WHERE  v.gstregistrationtype is not NULL AND m.company='"+companyid1+"' ";
               venRegTypePstn=con.prepareStatement(vendorRegTypeQuery);
               venRegTypeRst=venRegTypePstn.executeQuery();
               while(venRegTypeRst.next()){     
                   String id=venRegTypeRst.getString("id");
                   String usedregTypeId=venRegTypeRst.getString("gstregistrationtype");
                   String value=venRegTypeRst.getString("value");
                   String preservedValue=(String)gstRegTypeRequired.get(value);
                   
                   if(!usedregTypeId.equals(preservedValue)){
                       String updateRegType="update vendor set gstregistrationtype='"+preservedValue+"' where id='"+id+"' ";
                       updatePstn=con.prepareStatement(updateRegType);
                       int i=updatePstn.executeUpdate();
                   }     
               }
               
               String customerTypeQuery="SELECT c.id,c.gstcustomertype,m.value FROM customer c INNER JOIN masteritem m ON c.gstcustomertype=m.id  WHERE  c.gstcustomertype is not NULL AND m.company='"+companyid1+"'";
               customerPstn=con.prepareStatement(customerTypeQuery);
               customerRst=customerPstn.executeQuery();
               while(customerRst.next()){
                   String id=customerRst.getString("id");
                   String usedCustomerTypeId=customerRst.getString("gstcustomertype");
                   String value=customerRst.getString("value");
                   String preservedValue=(String)custVendTypeRequired.get(value);
                   
                   if(!usedCustomerTypeId.equals(preservedValue)){
                       String updateCustomerType="update customer set gstcustomertype='"+preservedValue+"' where id='"+id+"' ";
                       updatePstn=con.prepareStatement(updateCustomerType);
                       int i=updatePstn.executeUpdate();
                   }
               }
               
               String vendorTypeQuery="SELECT v.id,v.gstvendortype,m.value FROM vendor v INNER JOIN masteritem m ON v.gstvendortype=m.id  WHERE  v.gstvendortype is not NULL AND m.company='"+companyid1+"'";
               vendorPstn=con.prepareStatement(vendorTypeQuery);
               vendorRst=vendorPstn.executeQuery();
               while(vendorRst.next()){
                   String id=vendorRst.getString("id");
                   String usedVendorTypeId=vendorRst.getString("gstvendortype");
                   String value=vendorRst.getString("value");
                   String preservedValue=(String)custVendTypeRequired.get(value);  
                   
                   if(!usedVendorTypeId.equals(preservedValue)){                       
                       String updateVendorType="update vendor set gstvendortype='"+preservedValue+"' where id='"+id+"' ";
                       updatePstn=con.prepareStatement(updateVendorType);
                       int i=updatePstn.executeUpdate();
                   }
               }
               
            for(String key:custVendTypeduplicate){                
                try {
                   String deleteQuery="DELETE FROM masteritem WHERE id='"+key+"'";
                   deletePstn=con.prepareStatement(deleteQuery);
                   int i=deletePstn.executeUpdate();
                } catch (Exception e) {
                    out.println(key+" not deleted....<br/>");
                    continue;
                }
            }
            for(String key:gstRegTypeDuplicate){                
                try {
                   String deleteQuery="DELETE FROM masteritem WHERE id='"+key+"'";
                   deletePstn=con.prepareStatement(deleteQuery);
                   int i=deletePstn.executeUpdate();
                } catch (Exception e) {
                    out.println(key+" not deleted....<br/>");
                    continue;
                }
            }
            
            custVendTypeRequired.clear();
            custVendTypeduplicate.clear();
            gstRegTypeRequired.clear();
            gstRegTypeDuplicate.clear();
        }
        
            
//=================================Inserting "Other" customer/vendor type=================================================================================================          
        if (!StringUtil.isNullOrEmpty(reqSubdomain)) {
                query = "select c.companyid,c.country,c.subdomain,cp.setupdone from company c INNER JOIN compaccpreferences cp ON  c.companyid=cp.id  where subdomain='"+reqSubdomain+"'";
        }else{
                query = "select c.companyid,c.country,c.subdomain,cp.setupdone from company c inner join country cr on cr.id=c.country INNER JOIN compaccpreferences cp on c.companyid=cp.id  where cr.id in ('105')";
        }                
        cmpanyPstn = con.prepareStatement(query);
        cmpanyRst = cmpanyPstn.executeQuery();                                                       
        while(cmpanyRst.next()){
            companyid=cmpanyRst.getString("companyid");   
            subdomain=cmpanyRst.getString("subdomain");  
            String setUpDone1=cmpanyRst.getString("setupdone");
            if (setUpDone1.equalsIgnoreCase("T")) {
                    out.print("Execution Started For " + subdomain + "<br/>");
                    masterItemQuery = "SELECT * FROM masteritem WHERE mastergroup=63 and company='" + companyid + "' ";
                    masterItemPstn = con.prepareStatement(masterItemQuery);
                    masterItemRst = masterItemPstn.executeQuery();
                    presentRecords.clear();
                    while (masterItemRst.next()) {
                        presentRecords.add(masterItemRst.getString("value"));
                    }
                    if (presentRecords.contains(newValue)) {
                        continue;
                    } else {
                        String uuid = UUID.randomUUID().toString();
                        String query41 = "INSERT INTO masteritem (id,value,masterGroup,defaultmasteritem,company) VALUES (?,?,?,?,?) ";
                        PreparedStatement stmt4 = con.prepareStatement(query41);
                        stmt4.setString(1, uuid);
                        stmt4.setString(2, newValue);
                        stmt4.setString(3, "63");
                        stmt4.setString(4, "47d48400-6789-11e7-b99d-14dda97927f2"); //hardcoded defaultmasterItem id given to avoid unnecessory data processing.
                        stmt4.setString(5, companyid);
                        stmt4.execute();            
                    }
                    out.print("Execution Fineshed For " + subdomain + "<br/>");
                }            
        }                
 //==============================================================================================================================       
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

