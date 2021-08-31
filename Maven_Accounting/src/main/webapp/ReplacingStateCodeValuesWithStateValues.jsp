
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
        //SCRIPT URL :htttp://localhost:8080/Accounting/ReplacingStateCodeValuesWithStateValues.jsp?serverip=192.168.0.108&dbname=in2107&username=krawlersqladmin&subdomain=kabel&password=krawler

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
        String query = "",fieldparamQuery="",multiEntityQuery="",queryForState="",companyQuery="",updateQuery="";
        String data="",companyid="", stateId="",stateColNum="",fieldComboQuery="",stateCodeValue="",termsRateQuery="";
        String subdomain=""; String country=""; String setUpDone="",finalValue="",fieldComboId="",multiEntityId="",updateTermsRateQuery="";
        boolean isError = false;                   
        Class.forName(driver).newInstance();        
      
        //Execution Started :
        out.println("<br><br>Execution Started @ " + new java.util.Date() + "<br><br>");

        con = DriverManager.getConnection(connectString, username, password);
        PreparedStatement fieldParamPstn=null,multiEntityPstn=null,forStatePstn=null,companyPstn=null,fieldComboPstn=null,updatePstn=null,termsRatePstn=null,sqlQueryPstn=null,stateQPstn=null,updateTermsRatePstn=null,deletePstn=null,deletePstn1=null;
        ResultSet fieldParamRst=null,multiEntityRst=null,forStateRst=null,companyRst=null,fieldComboRst=null,updateRst=null,termsRateRst=null,sqlQueryRst=null,stateQRst=null,updateTermsRateRst=null;
        
        //================Updating entitybasedlineleveltermsrate================================     
        
        termsRateQuery="SELECT id,shippedloc1 FROM entitybasedlineleveltermsrate WHERE shippedloc1 IS NOT NULL";
        /*
         * fetching all records from entitybasedlineleveltermsrate where shippedloc1 IS NOT NULL
        */
        termsRatePstn=con.prepareStatement(termsRateQuery);
        termsRateRst=termsRatePstn.executeQuery();
        while(termsRateRst.next()){
        
            String termsRateId=termsRateRst.getString("id");
            String fieldComboId1=termsRateRst.getString("shippedloc1");
            String sqlQuery="select f.fieldlabel,f.companyid,fc.`value` from fieldparams f inner join fieldcombodata fc on f.id=fc.fieldid where fc.id= '"+fieldComboId1+"' ";
            /*
             *  fetching records from fieldcombodata which are use in entitybasedlineleveltermsrate, also fetching their coresponding label from fieldparams. 
             */
            sqlQueryPstn=con.prepareStatement(sqlQuery);
            sqlQueryRst=sqlQueryPstn.executeQuery();
            while(sqlQueryRst.next()){
                        String stateCodeLabel = sqlQueryRst.getString("fieldlabel");
                        String companyid1 = sqlQueryRst.getString("companyid");
                        String value1 = sqlQueryRst.getString("value");
                try {
                        
                        if (!StringUtil.isNullOrEmpty(stateCodeLabel) && stateCodeLabel.equalsIgnoreCase("State Code")) {

                            /*
                             * if used record in entitybasedlineleveltermsrate belongs to "State Code" then fetching record of "State" with same value
                             */
                            System.out.println(termsRateId+" in entitybasedlineleveltermsrate was having 'State Code' value i.e "+fieldComboId1);
                            out.println(termsRateId+" in entitybasedlineleveltermsrate was having 'State Code' value i.e "+fieldComboId1+"<br><br>");
                            
                            String stateQ = "select fc.id from fieldcombodata fc inner join fieldparams f on fc.fieldid=f.id "
                                    + "where fc.value='" + value1 + "' and f.fieldlabel='State' and f.moduleid=1200 and f.companyid='" + companyid1 + "' ";
                            stateQPstn = con.prepareStatement(stateQ);
                            stateQRst = stateQPstn.executeQuery();
                            String targetId = "";
                            while (stateQRst.next()) {
                                /*
                                 * target id is value of 'State' eg. Maharashtra of State, which will be replaced with value of 'State Code' i.e. Maharashtra of State Code 
                                 */
                                targetId = stateQRst.getString("id");
                            }
                            updateTermsRateQuery = "update entitybasedlineleveltermsrate set shippedloc1='" + targetId + "' where id='" + termsRateId + "' ";
                            updateTermsRatePstn = con.prepareStatement(updateTermsRateQuery);
                            int i = updateTermsRatePstn.executeUpdate();
                            if(i>0){
                                System.out.println("Value Updated for "+termsRateId+" updated value is "+targetId+"<br><br>");
                                out.println("Value Updated for "+termsRateId+" updated value is "+targetId+"<br><br>");
                            }
                        }
                    } catch (Exception e) {
                          out.println("Exception occurred for  "+companyid1+"<br/>");
                    }
            }
            
                
        }
        
       //===================== Execution for Entity================================
        if(reqSubdomain == null){
            companyQuery="SELECT companyid,subdomain FROM company WHERE country in (105,244)";
        }else{
            companyQuery="SELECT companyid,subdomain FROM company WHERE subdomain='"+reqSubdomain+"' ";
        }
        
        companyPstn=con.prepareStatement(companyQuery);
        companyRst=companyPstn.executeQuery();
        while (companyRst.next()) {
                
                companyid=companyRst.getString("companyid"); 
                subdomain=companyRst.getString("subdomain"); 
                queryForState="SELECT id,colnum FROM fieldparams WHERE fieldlabel='State' AND moduleid=1200 AND companyid='"+companyid+"' ";
                /*
                 *  get Details of "State" field
                 */
                forStatePstn=con.prepareStatement(queryForState);
                forStateRst=forStatePstn.executeQuery();
                while(forStateRst.next()){
                    stateId=forStateRst.getString("id");
                    stateColNum="col"+forStateRst.getString("colnum");
                }
                fieldparamQuery = "SELECT id,colnum FROM fieldparams WHERE fieldlabel='State Code' AND moduleid=1200 AND companyid='"+companyid+"' ";
                 /*
                 *  get Details of "State Code" field
                 */
                fieldParamPstn = con.prepareStatement(fieldparamQuery);
                fieldParamRst = fieldParamPstn.executeQuery();
                while (fieldParamRst.next()) {
                    String fieldParamId = fieldParamRst.getString("id");
                    String colNum = "col"+fieldParamRst.getString("colnum");
                    multiEntityQuery = "select fcdid,"+colNum+" from multientitydimesioncustomdata where company='" + companyid + "' ";
                    /*
                     * Retriving State Code values from multientitydimesioncustomdata i.e value in colNum *step 2*
                     */
                    multiEntityPstn = con.prepareStatement(multiEntityQuery);
                    multiEntityRst = multiEntityPstn.executeQuery();
                    while (multiEntityRst.next()) {
                        fieldComboId = multiEntityRst.getString(colNum);
                        if (!StringUtil.isNullOrEmpty(fieldComboId)) {
                                multiEntityId = multiEntityRst.getString("fcdid");
                                fieldComboQuery = "select `value` from fieldcombodata where id='" + fieldComboId + "' ";  // fetching record for id retrived in *step 2*  
                                fieldComboPstn = con.prepareStatement(fieldComboQuery);
                                fieldComboRst = fieldComboPstn.executeQuery();
                                while (fieldComboRst.next()) {
                                    stateCodeValue = fieldComboRst.getString("value"); // "State Code" Value used in multientitydimesioncustomdata
                                }

                                fieldComboQuery = "select id from fieldcombodata where `value`='" + stateCodeValue + "' and fieldid='" + stateId + "' ";  // 
                                /*
                                 * Now retriving same value for "State"
                                 *   here stateId is id of fieldParams where fieldlabel='State' AND moduleid=1200 for this company
                                 */
                                fieldComboPstn = con.prepareStatement(fieldComboQuery);
                                fieldComboRst = fieldComboPstn.executeQuery();
                                while (fieldComboRst.next()) {
                                    finalValue = fieldComboRst.getString("id");
                                }
                                updateQuery = "UPDATE multientitydimesioncustomdata SET "+colNum+"= null, "+stateColNum+"='" + finalValue + "' where fcdid='" + multiEntityId + "' ";
                                /*
                                 *  here colNum is for "State Code" and stateColNum is for "State"
                                 */
                                updatePstn = con.prepareStatement(updateQuery);
                                int rows=updatePstn.executeUpdate();
                            }                        
                    }
                }//End of while
                
                //===============Deleting "State Code"=========================
                String deleteQuery="DELETE FROM fieldcombodata WHERE fieldid in (select id from fieldparams where fieldlabel='State Code' and moduleid=1200 and companyid='"+companyid+"' )";
                deletePstn=con.prepareStatement(deleteQuery);
                int fieldComboRows=deletePstn.executeUpdate();
                
                deleteQuery="delete from fieldparams where fieldlabel='State Code' and moduleid=1200 and companyid='"+companyid+"'";
                deletePstn=con.prepareStatement(deleteQuery);
                int fieldParamsRows=deletePstn.executeUpdate();
                out.print(fieldComboRows+" Records from fieldcombodata and "+fieldParamsRows+" Records from fieldparams are deleted from "+subdomain+"<br><br>");
                                           
            }                
        
        
        
        
        
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

