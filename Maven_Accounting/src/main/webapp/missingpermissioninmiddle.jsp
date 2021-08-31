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
        String subdomain = request.getParameter("subdomain") != null ? request.getParameter("subdomain") : "";
        

        if (StringUtil.isNullOrEmpty(serverip) || StringUtil.isNullOrEmpty(port) || StringUtil.isNullOrEmpty(dbName) || StringUtil.isNullOrEmpty(userName) || StringUtil.isNullOrEmpty(password)) {//
            throw new Exception(" You have not privided all parameters (parameter are: serverip,port,dbname,username,password) in url. so please provide all these parameter correctly. ");
        }

        String connectString = "jdbc:mysql://" + serverip + ":" + port + "/" + dbName;
        String driver = "com.mysql.jdbc.Driver";
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(connectString, userName, password);
        
        

        // For deleting permissions of Unlink button
//        String deleteQuery = "delete from activitylist where activityid = 'f2ad0406628e11e5b942eca86bfcd415'";
//        PreparedStatement delStmt = conn.prepareStatement(deleteQuery);
//        delStmt.executeUpdate();
//
//        deleteQuery = "delete from activitylist where activityid = '2972908e629c11e5b942eca86bfcd415'";
//        delStmt = conn.prepareStatement(deleteQuery);
//        delStmt.executeUpdate();
//
//        deleteQuery = "delete from activitylist where activityid = '2ecb0fee62b911e5b942eca86bfcd415'";
//        delStmt = conn.prepareStatement(deleteQuery);
//        delStmt.executeUpdate();
//
//        deleteQuery = "delete from activitylist where activityid = '6c384af2634c11e5a11beca86bfcd415'";
//        delStmt = conn.prepareStatement(deleteQuery);
//        delStmt.executeUpdate();
//
//        deleteQuery = "delete from activitylist where activityid = '6ec56a94635511e5a11beca86bfcd415'";
//        delStmt = conn.prepareStatement(deleteQuery);
//        delStmt.executeUpdate();
//
//        deleteQuery = "delete from activitylist where activityid = 'a48af3a2636d11e5a11beca86bfcd415'";
//        delStmt = conn.prepareStatement(deleteQuery);
//        delStmt.executeUpdate();
//
//        deleteQuery = "delete from activitylist where activityid = '2b13fc82637f11e5a11beca86bfcd415'";
//        delStmt = conn.prepareStatement(deleteQuery);
//        delStmt.executeUpdate();
//
//        deleteQuery = "delete from activitylist where activityid = '834a22c0640e11e58e15eca86bfcd415'";
//        delStmt = conn.prepareStatement(deleteQuery);
//        delStmt.executeUpdate();
//
//        deleteQuery = "delete from activitylist where activityid = '06a49cfc641111e58e15eca86bfcd415'";
//        delStmt = conn.prepareStatement(deleteQuery);
//        delStmt.executeUpdate();
//
//        deleteQuery = "delete from activitylist where activityid = '640fc0ee641411e58e15eca86bfcd415'";
//        delStmt = conn.prepareStatement(deleteQuery);
//        delStmt.executeUpdate();

        // main logic for updating the missing permission
        String query = "select companyid,companyname FROM company ";
        String [] subdomainArray=null;
        String condition="";
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            subdomainArray=subdomain.split(",");
            for(int i=0;i<subdomainArray.length;i++){
              condition +=" "+"'"+subdomainArray[i]+"'"+" ,";  
            }
            condition=condition.substring(0,condition.length()-2);
            query += "where subdomain in ("+ condition +")";
        }
        

        PreparedStatement stmt = conn.prepareStatement(query);
        /*if (!StringUtil.isNullOrEmpty(subdomain)) {
            stmt.setString(1, subdomain);
        }*/
        ResultSet rs = stmt.executeQuery();

        int totalCompanyUpdationCnt = 0;
        int prRuleAddCounter = 0;
        String queryactivityfind = "select activityname from activitylist where feature='ff80808122f3cb640122f44c9db10012' and activityname in('unlinkcqdoc','unlinksidoc','unlinksodoc')"; //
        PreparedStatement stmtactvity = conn.prepareStatement(queryactivityfind);
        ResultSet rsactivity= stmtactvity.executeQuery();
        int count=0;
        while(rsactivity.next()){
            count++;
        }
        if(count<1){
            
        String create_table = "create table userpermission_backup like userpermission";
        PreparedStatement createStmt_backup = conn.prepareStatement(create_table);
        createStmt_backup.executeUpdate();
       
        String insert_table = "insert into userpermission_backup (select * from userpermission)";
        PreparedStatement insertStmt_backup = conn.prepareStatement(insert_table);
        insertStmt_backup.executeUpdate();
        while (rs.next()) {
            String companyId = rs.getString("companyid");
            String companyname = rs.getString("companyname");

            query = "select featureid,featurename from featurelist"; //
            PreparedStatement stmt1 = conn.prepareStatement(query);
            //stmt1.setString(1, companyId);
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                String featureid = "", featureName = "";
                int permissioncode = 0;

                featureid = rs1.getString("featureid");
                featureName = rs1.getString("featurename");
                // out.println(featureName);
                if (!StringUtil.isNullOrEmpty(featureName)) {
                    //int permissioncount=(int)Math.pow(2,count)-1;
                    if (featureName.equals("invoice")) {
                        String Query = "select permissioncode,roleUserMapping from userpermission where feature='ff80808122f3cb640122f44c9db10012' and role!='1' and roleUserMapping in(select id from role_user_mapping where userid in(select userid from users where roleid!='1' and company = ?))";//select userid from userlogin
                        PreparedStatement stmt6 = conn.prepareStatement(Query);
                        stmt6.setString(1, companyId);
                        ResultSet rs6 = stmt6.executeQuery();
                        while (rs6.next()) {

                            long permsioncode = Long.parseLong(rs6.getString("permissioncode"));

                            long temppermissioncode = permsioncode;
                            int j = 0;
                            int actidcount = 25;            
                            long activitylist = 0;

                            long exitpemissioncodeforinvoice = 0;

                            while (actidcount >= 0) {
                                activitylist = (long) Math.pow(2, j);

                                if ((permsioncode & activitylist) == activitylist) {

                                    if (j <= 6) {
                                        exitpemissioncodeforinvoice = exitpemissioncodeforinvoice + (long) Math.pow(2, j);

                                    } else if (j >= 7 && j <= 13) {
                                        exitpemissioncodeforinvoice = exitpemissioncodeforinvoice + (long) Math.pow(2, j + 1);
                                    } else if (j >= 14 && j <= 24) {
                                        exitpemissioncodeforinvoice = exitpemissioncodeforinvoice + (long) Math.pow(2, j + 2);
                                    }
                                }
                                j++;
                                actidcount--;
                            }
                            try {
                                query = "update userpermission set permissioncode=? where feature='ff80808122f3cb640122f44c9db10012' and role!='1' and roleUserMapping=?";
                                PreparedStatement stmt4 = conn.prepareStatement(query);
                                //long finalpermissionforexitingtable=temppermissioncode-exitpemissioncode;
                                stmt4.setLong(1, exitpemissioncodeforinvoice);
                                stmt4.setString(2, rs6.getString("roleUserMapping"));
                                int count1 = stmt4.executeUpdate();
                                out.println("<br>set permissioncode for exiting Set for sale management" + "=" + exitpemissioncodeforinvoice);
                            } catch (Exception e) {
                                e.printStackTrace();
                                out.println(e.getMessage());
                            } 

                        }
                    } else if (featureName.equals("vendorinvoice")) {

                        String Query = "select permissioncode,roleUserMapping from userpermission where feature='ff80808122f9dba90122fa4888cf0054' and role!='1' and roleUserMapping in(select id from role_user_mapping where userid in(select userid from users where roleid!='1' and company=?))";
                        PreparedStatement stmt6 = conn.prepareStatement(Query);
                        stmt6.setString(1, companyId);
                        ResultSet rs6 = stmt6.executeQuery();
                        while (rs6.next()) {
                            long permsioncode = Long.parseLong(rs6.getString("permissioncode"));

                            int j = 0;
                            int actidcount = 25;       
                            long temppermissioncode = permsioncode;
                            long activitylist = 0;
                            long exitpemissioncode = 0;
                            long exitpemissioncodeforvendorinvoice = 0;
                            long exitpermissionforvendorpayment = 0;
                            String acid = "";
                            while (actidcount >= 0) {
                                activitylist = (long) Math.pow(2, j);

                                if ((permsioncode & activitylist) == activitylist) {

                                    if (j <= 6) {
                                        exitpemissioncodeforvendorinvoice = exitpemissioncodeforvendorinvoice + (long) Math.pow(2, j);

                                    } else if (j >= 7 && j <= 13) {
                                        exitpemissioncodeforvendorinvoice = exitpemissioncodeforvendorinvoice + (long) Math.pow(2, j + 1);
                                    } else if (j >= 14 && j <= 24) {
                                        exitpemissioncodeforvendorinvoice = exitpemissioncodeforvendorinvoice + (long) Math.pow(2, j + 2);
                                    }
                                }
                                j++;
                                actidcount--;
                            }

                            query = "update userpermission set permissioncode=? where feature='ff80808122f9dba90122fa4888cf0054' and role!='1' and roleUserMapping=?";
                            PreparedStatement stmt4 = conn.prepareStatement(query);

                            stmt4.setLong(1, exitpemissioncodeforvendorinvoice);
                            stmt4.setString(2, rs6.getString("roleUserMapping"));
                            //out.println("<br>Setting permissioncode for user = " + rs6.getString("roleUserMapping"));
                            int count1 = stmt4.executeUpdate();
                            out.println("<br>set permissioncode for exiting Set for purchase management" + "=" + exitpemissioncodeforvendorinvoice);

                        }

                    }

                }

            }
            
            //out.println("<br><br> permissioncode successfully <b>Updated</b> ");

        }

        // For inserting permissions of Unlink button
        String insertQuery = "insert into activitylist(activityid,activityname,displayactivityname,orderno,parent,feature) VALUES ('f2ad0406628e11e5b942eca86bfcd415', 'unlinksidoc', 'Unlink', '209', 'ff80808122f3cb640122f457435a0027', 'ff80808122f3cb640122f44c9db10012')";
        PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.executeUpdate();

        insertQuery = "insert into activitylist(activityid,activityname,displayactivityname,orderno,parent,feature) VALUES ('2972908e629c11e5b942eca86bfcd415', 'unlinkpidoc', 'Unlink', '211', 'ff80808122f9dba90122fa4888cf00101', 'ff80808122f9dba90122fa4888cf0054')";
        insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.executeUpdate();

        insertQuery = "insert into activitylist(activityid,activityname,displayactivityname,orderno,parent,feature) VALUES ('2ecb0fee62b911e5b942eca86bfcd415', 'unlinkvqdoc', 'Unlink', '8', '906b2334a2ce11e4adaceca86bff8e7d', 'ff80808122f9dba90122fa4888cf0054')";
        insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.executeUpdate();

        insertQuery = "insert into activitylist(activityid,activityname,displayactivityname,orderno,parent,feature) VALUES ('6c384af2634c11e5a11beca86bfcd415', 'unlinkcqdoc', 'Unlink', '18', 'ff80808122f9dba90122fa4888cf1000', 'ff80808122f3cb640122f44c9db10012')";
        insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.executeUpdate();

        insertQuery = "insert into activitylist(activityid,activityname,displayactivityname,orderno,parent,feature) VALUES ('6ec56a94635511e5a11beca86bfcd415', 'unlinkpodoc', 'Unlink', '107', 'ff80808122f9dba90122fa4888cf00106', 'ff80808122f9dba90122fa4888cf0054')";
        insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.executeUpdate();

        insertQuery = "insert into activitylist(activityid,activityname,displayactivityname,orderno,parent,feature) VALUES ('a48af3a2636d11e5a11beca86bfcd415', 'unlinksodoc', 'Unlink', '107', 'ff80808122f9dba90122fa4888cf00105', 'ff80808122f3cb640122f44c9db10012')";
        insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.executeUpdate();

        insertQuery = "insert into activitylist(activityid,activityname,displayactivityname,orderno,parent,feature) VALUES ('2b13fc82637f11e5a11beca86bfcd415', 'unlinkgrdoc', 'Unlink', '8', '408a4e1eb35811e3abe3001cc066e9f0', '4d5cb414b35411e3abe3001cc066e9f0')";
        insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.executeUpdate();

        insertQuery = "insert into activitylist(activityid,activityname,displayactivityname,orderno,parent,feature) VALUES ('834a22c0640e11e58e15eca86bfcd415', 'unlinkdodoc', 'Unlink', '8', '7a6d0d5cb35c11e39a00001cc066e9f0', 'f487fea4b35b11e39a00001cc066e9f0')";
        insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.executeUpdate();

        insertQuery = "insert into activitylist(activityid,activityname,displayactivityname,orderno,parent,feature) VALUES ('06a49cfc641111e58e15eca86bfcd415', 'unlinkprdoc', 'Unlink', '8', '151e5ab89bd811e4bbfeeca86bff8e7d', 'a528df089bd711e4bbfeeca86bff8e7d')";
        insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.executeUpdate();

        insertQuery = "insert into activitylist(activityid,activityname,displayactivityname,orderno,parent,feature) VALUES ('640fc0ee641411e58e15eca86bfcd415', 'unlinksrdoc', 'Unlink', '8', '4a888d049bd811e4bbfeeca86bff8e7d', 'e092295a9bd711e4bbfeeca86bff8e7d')";
        insertStmt = conn.prepareStatement(insertQuery);
        insertStmt.executeUpdate();
        } else{
            out.println("<br><b>Script already executed</b>");
        }
        } catch (Exception e) {
        e.printStackTrace();
        out.println(e.getMessage());
    } finally {
        conn.close();
          out.println("<br><b>Done</b>");
    }
%>