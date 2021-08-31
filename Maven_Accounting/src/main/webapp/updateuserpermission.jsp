<%@page import="org.hibernate.Transaction"%>

<%@ page pageEncoding="UTF-8"%>
<%@ page language="java" %>
<%@ page import="com.krawler.common.util.StringUtil" %>

<%@ page import="com.krawler.utils.json.base.JSONObject" %>
<%@ page import="com.krawler.utils.json.base.JSONArray" %>

<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.DriverManager" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.util.*"%>
<%@ page import=" java.io.*"%>



<%
    Connection conn = null;
    //Session hSession = null;
    Transaction tx = null;
    try {
        //JSONObject jbj = new JSONObject();
        String serverip = request.getParameter("serverip");//"192.168.0.225";                            
        String port = request.getParameter("port");//"3306";
        String dbName = request.getParameter("dbname");//"staginginvaccnew";
        String userName = request.getParameter("username");//"krawlersqladmin";
        String password = request.getParameter("password"); //"krawler"
        String subdomain = request.getParameter("subdomain") != null ? request.getParameter("subdomain") : "";//"swt";
      
       
        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }
        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";

        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        String query = "select companyid,companyname FROM company ";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            query += " where subdomain= ?";
        }
       
        PreparedStatement stmt = conn.prepareStatement(query);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }
        ResultSet rs = stmt.executeQuery();

        int totalCompanyUpdationCnt = 0;
        int prRuleAddCounter = 0;

        while (rs.next()) {
            String companyId = rs.getString("companyid");
            String companyname = rs.getString("companyname");

            // Get Tax
            query = "select featureid,featurename from featurelist"; //
            PreparedStatement stmt1 = conn.prepareStatement(query);
            //stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                String featureid = "", featureName = "";int permissioncode =0;
                
                featureid = rs1.getString("featureid");
                featureName = rs1.getString("featurename");
               // out.println(featureName);
                if (!StringUtil.isNullOrEmpty(featureName)) {
                    //int permissioncount=(int)Math.pow(2,count)-1;
                    if (featureName.equals("invoice")) {
                        String Query= "select permissioncode,roleUserMapping from userpermission where feature='ff80808122f3cb640122f44c9db10012' and role=2 and roleUserMapping in(select id from role_user_mapping where userid in(select userid from userlogin))";
                        PreparedStatement stmt6 = conn.prepareStatement(Query);
                        
                        ResultSet rs6 = stmt6.executeQuery(); 
                        while(rs6.next()){
                       
                        long permsioncode=Long.parseLong(rs6.getString("permissioncode"));   
                        
                        long temppermissioncode=permsioncode;
                        int j=0; 
                        int actidcount= 33; //     24+9;           
                        long activitylist=0;
                       
                        long exitpemissioncodeforinvoice=0;
                        long exitpermissionforreceivepayment=0;
                        
                       
                        while (actidcount >= 0) {
                               activitylist = (long) Math.pow(2, j);
                              
                                if ((permsioncode & activitylist)==activitylist) {

                                    if (j <= 24) {
                                        exitpemissioncodeforinvoice = exitpemissioncodeforinvoice + (long) Math.pow(2, j);

                                    } else {
                                        exitpermissionforreceivepayment = exitpermissionforreceivepayment + (long) Math.pow(2,j-25);
                                    }
                                }
                                j++;
                                actidcount--;
                            }
                          
                           query = "update userpermission set permissioncode=? where feature='ff80808122f3cb640122f44c9db10012' and role=2 and roleUserMapping=?";
                           PreparedStatement stmt4 = conn.prepareStatement(query);
                           //long finalpermissionforexitingtable=temppermissioncode-exitpemissioncode;
                           stmt4.setLong(1,exitpemissioncodeforinvoice);
                           stmt4.setString(2, rs6.getString("roleUserMapping"));
                           int count1 = stmt4.executeUpdate();
                           out.println("<br>set permissioncode for exiting Set for sale management" + "=" + exitpemissioncodeforinvoice);
                           
                           query ="update userpermission set permissioncode=? where feature='4f52f9615e344a50a7e1c8c6a51def99' and role=2 and roleUserMapping=?";
                           PreparedStatement stmt5 = conn.prepareStatement(query);
                           stmt5.setLong(1,exitpermissionforreceivepayment);
                           stmt5.setString(2, rs6.getString("roleUserMapping"));
                           int count2 = stmt5.executeUpdate();
                           out.println("<br>set permissioncode for new Set for Receive payment" + " " + "=" + exitpermissionforreceivepayment);
                           
                       }         
                    } else if (featureName.equals("vendorinvoice")) {
                          
                        String Query= "select permissioncode,roleUserMapping from userpermission where feature='ff80808122f9dba90122fa4888cf0054' and role=2 and roleUserMapping in(select id from role_user_mapping where userid in(select userid from userlogin))";
                        PreparedStatement stmt6 = conn.prepareStatement(Query);
                        ResultSet rs6 = stmt6.executeQuery(); 
                       while(rs6.next()){
                        long permsioncode=Long.parseLong(rs6.getString("permissioncode"));  
                        
                        int j=0;            
                        int actidcount=32;   //23+9      
                        long temppermissioncode=permsioncode;                        
                        long activitylist=0;
                        long exitpemissioncode=0;
                        long exitpemissioncodeforvendorinvoice=0;
                        long exitpermissionforvendorpayment=0;
                        String acid="";
                        while (actidcount>=0) {
                                activitylist = (long) Math.pow(2, j);
                              
                                if((permsioncode & activitylist)==activitylist) {
                                  
                                    if (j<=23) {
                                            exitpemissioncodeforvendorinvoice = exitpemissioncodeforvendorinvoice + (long) Math.pow(2,j);

                                        } else {
                                            exitpermissionforvendorpayment = exitpermissionforvendorpayment + (long) Math.pow(2,j-24);
                                        }
                                }
                                    j++;
                                   actidcount--; 
                              }  
                          
                           query = "update userpermission set permissioncode=? where feature='ff80808122f9dba90122fa4888cf0054' and role=2 and roleUserMapping=?";
                           PreparedStatement stmt4 = conn.prepareStatement(query);
                          
                           stmt4.setLong(1,exitpemissioncodeforvendorinvoice);
                           stmt4.setString(2, rs6.getString("roleUserMapping"));
                           
                           int count1 = stmt4.executeUpdate();
                           out.println("<br>set permissioncode for exiting Set for purchase management" +  "=" + exitpemissioncodeforvendorinvoice);
                           
                           query = "update userpermission set permissioncode=? where feature='4f52f9615e344a50a7e1c8c6a51deg99' and role=2 and roleUserMapping=?";
                           PreparedStatement stmt5 = conn.prepareStatement(query);
                           stmt5.setLong(1,exitpermissionforvendorpayment);
                           stmt5.setString(2,rs6.getString("roleUserMapping"));
                           int count2 = stmt5.executeUpdate();
                           out.println("<br>set permissioncode for new Set for make payment" + " " + "=" + exitpermissionforvendorpayment);
                           
                    } 
                 
                                    }
               
            }

            
        }
        
           tx.commit();
                out.println("<br><br> permissioncode successfully <b>Updated</b> for " + companyname);
        
    } 
    }catch (Exception e) {
        e.printStackTrace();
       // out.print(e.toString());
    } finally {
        //if (conn != null) {
            conn.close();
       // }

    }

%>

