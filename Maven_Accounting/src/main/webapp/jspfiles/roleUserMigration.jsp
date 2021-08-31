<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page language="java" %>
<%@ page import="com.krawler.common.util.StringUtil" %>
<%@ page import="com.krawler.esp.database.*" %>
<%@ page import="com.krawler.utils.json.base.JSONObject" %>
<%@ page import="com.krawler.utils.json.base.JSONArray" %>
<%@ page import="com.krawler.esp.handlers.AuthHandler"%>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.DriverManager" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.util.*"%>
<%@ page import=" java.io.*"%>

<%!
    public static String fun1(ResultSet rs, String fieldName) {
        String str = null;
        try {
            str = (rs.getObject(fieldName) != null ? "'" + rs.getObject(fieldName) + "'" : rs.getObject(fieldName)).toString();
        } catch (Exception e) {
            str = null;
            return str;
        }
        return str;
    }
%>

<%
        String resultstr = "";
        Connection conn = null;
        try {
            ResultSet rs = null;
            ResultSet rs2 = null;
            PreparedStatement pstmt = null;
            String query = "";
            String roleid = "";
            String app = request.getParameter("app");
            String frmDb = request.getParameter("fromdb");
            String toDb = request.getParameter("todb")==null?frmDb:request.getParameter("todb");
            String ip = request.getParameter("ip");
            String user = request.getParameter("user");
            String password = request.getParameter("password");
            String mode = request.getParameter("mode"); //1:role_user_map, 2:rolelist, 3:UserPermission, 4:audit_group, 5:audit_action, 6:audit_trail

            //path : jspfiles/roleUserMigration.jsp?app=accounting&mode=123456&fromdb=springaccounting8410&ip=192.168.0.40&user=krawler&password=krawler
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://"+ip+":3306/" + toDb + "?user="+user+"&password="+password);

            File file = new File("/home/krawler/roleusermap.txt");
            Writer output = null;
            output = new BufferedWriter(new FileWriter(file));

            pstmt = conn.prepareStatement("set foreign_key_checks = 0");
            int a1 = pstmt.executeUpdate();


            
            

    if(mode.contains("1")) { //role_user_map
        //Flush all
            String deletequery = "delete from role_user_mapping";
            pstmt = conn.prepareStatement(deletequery);
            int numrows = pstmt.executeUpdate();
        //Migrate all
            pstmt = conn.prepareStatement("select userid,roleid from " + toDb + ".users");
            rs = pstmt.executeQuery();

            numrows = 0;
            while (rs.next()) {
                try {
                    String uuid = java.util.UUID.randomUUID().toString();
                    query = "insert into " + toDb + ".role_user_mapping (id,userId,roleId)values(" +
                            "'" + uuid + "'" + "," +
                            fun1(rs, "userId") + "," +
                            fun1(rs, "roleId");
                    query = query + ");\n";
                    pstmt = conn.prepareStatement(query);
                    numrows += pstmt.executeUpdate();
                } catch (Exception e) {
                    output.write(query);
                    resultstr += "FAILED: "+e.getMessage()+", QUERY: "+query+ "\n";
                }
            }
            resultstr += "role_user_mapping : "+numrows+ "\n";
    }

if (app.equals("accounting")) {
    if(mode.contains("2")) { //roleList
        //Flush all
            String deletequery = "delete from rolelist";
            pstmt = conn.prepareStatement(deletequery);
            int numrows = pstmt.executeUpdate();

        //Migrate role
            pstmt = conn.prepareStatement("select id,name from " + frmDb + ".role");
            rs = pstmt.executeQuery();

            numrows = 0;
            while (rs.next()) {
                try {
                    query = " insert into " + toDb + ".rolelist(roleid,rolename,displayrolename) values(" +
                            fun1(rs, "id") + "," +
                            fun1(rs, "name") + "," +
                            fun1(rs, "name");
                    query = query + ");\n";
                    pstmt = conn.prepareStatement(query);
                    numrows += pstmt.executeUpdate();
                } catch (Exception e) {
                    output.write(query);
                    resultstr += "FAILED: "+e.getMessage()+", QUERY: "+query+ "\n";
                }
            }
            resultstr += "rolelist : "+numrows+ "\n";
   }

    if(mode.contains("3")) {
        //Update UserPermission
            pstmt = conn.prepareStatement("select feature, role, permissioncode from " + frmDb + ".userpermission");
            rs = pstmt.executeQuery();

            int numrows = 0;
            while (rs.next()) {
                roleid = rs.getObject("role").toString();
                pstmt = conn.prepareStatement("select id from " + frmDb + ".role_user_mapping where roleId='" + roleid + "'");
                rs2 = pstmt.executeQuery();
                while (rs2.next()) {
                    String rolemapid = rs2.getObject("id").toString();
                    try {
                        query = " insert into " + toDb + ".userpermission(feature,roleId,permissioncode,role) values(" +
                                fun1(rs, "feature") + "," +
                                "'" + rolemapid + "'" + "," +
                                fun1(rs, "permissioncode") + "," +
                                "'" + roleid + "'";
                        query = query + ");\n";
                        pstmt = conn.prepareStatement(query);
                        numrows += pstmt.executeUpdate();
                    } catch (Exception e) {
                        output.write(query);
                        resultstr += "FAILED: "+e.getMessage()+", QUERY: "+query+ "\n";
                    }
                }
            }
            resultstr += "userpermission : "+numrows+ "\n";
    }
}

    if(mode.contains("4")) { //audit_group
        //Flush all audit_group
            String deletequery = "delete from audit_group";
            pstmt = conn.prepareStatement(deletequery);
            int numrows = pstmt.executeUpdate();

        //Migrate audit_group
            try {
                query = "insert into audit_group(select * from auditgroup)";
                pstmt = conn.prepareStatement(query);
                numrows = pstmt.executeUpdate();
            } catch (Exception e) {
                output.write(query);
                resultstr += "FAILED: " + e.getMessage() + ", QUERY: " + query + "\n";
            }
        resultstr += "audit_group : " + numrows + "\n";
    }

    if(mode.contains("5")) { //audit_action
        //Flush all audit_action
            String deletequery = "delete from audit_action";
            pstmt = conn.prepareStatement(deletequery);
            int numrows = pstmt.executeUpdate();

        //Migrate audit_action
            try {
                query = "insert into audit_action(select * from auditaction)";
                pstmt = conn.prepareStatement(query);
                numrows = pstmt.executeUpdate();
            } catch (Exception e) {
                output.write(query);
                resultstr += "FAILED: " + e.getMessage() + ", QUERY: " + query + "\n";
            }
        resultstr += "audit_action : " + numrows + "\n";
    }

    if(mode.contains("6")) { //audit_trail
        //Flush all
            String deletequery = "delete from audit_trail";
            pstmt = conn.prepareStatement(deletequery);
            int numrows = pstmt.executeUpdate();

        //Migrate audit_trail
            pstmt = conn.prepareStatement("select id,details,ipaddr,audittime,user,action from " + frmDb + ".audittrail");
            rs = pstmt.executeQuery();

            numrows = 0;
            while (rs.next()) {
                try {
                    query = " insert into " + toDb + ".audit_trail(id,details,ipaddr,audittime,user,action,recid,extraid) values(" +
                            fun1(rs, "id") + "," +
                            fun1(rs, "details") + "," +
                            fun1(rs, "ipaddr") + "," +
                            fun1(rs, "audittime") + "," +
                            fun1(rs, "user") + "," +
                            fun1(rs, "action") + ",0,0";
                    query = query + ");\n";
                    pstmt = conn.prepareStatement(query);
                    numrows += pstmt.executeUpdate();
                } catch (Exception e) {
                    output.write(query);
                    resultstr += "FAILED: "+e.getMessage()+", QUERY: "+query+ "\n";
                }
            }
            resultstr += "audit_trail : "+numrows+ "\n";
    }

    if(mode.contains("7")) { //Revert user permissions

        //Migrate user permissions
            pstmt = conn.prepareStatement("select feature, role, permissioncode from " + frmDb + ".userperms");
            rs = pstmt.executeQuery();

            int numrows = 0;
            while (rs.next()) {
                try {
                    query = " insert into " + toDb + ".userpermission(feature, role, permissioncode) values(" +
                            fun1(rs, "feature") + "," +
                            fun1(rs, "role") + "," +
                            fun1(rs, "permissioncode") ;
                    query = query + ");\n";
                    pstmt = conn.prepareStatement(query);
                    numrows += pstmt.executeUpdate();
                } catch (Exception e) {
                    output.write(query);
                    resultstr += "FAILED: "+e.getMessage()+", QUERY: "+query+ "\n";
                }
            }
            resultstr += "user permissions : "+numrows+ "\n";
    }

            if (app.equals("crm")) {
                pstmt = conn.prepareStatement("select feature,userlogin,permissioncode from " + frmDb + ".userpermission");
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    try {
                        String userid = rs.getObject("userlogin").toString();
                        pstmt = conn.prepareStatement("select id from " + toDb + ".role_user_mapping where userId='" + userid + "'");
                        rs2 = pstmt.executeQuery();
                        if (rs2.next()) {
                            roleid = rs2.getObject("id").toString();
                        }

                        query = " insert into " + toDb + ".userpermission(feature,roleId,permissioncode) values(" +
                                fun1(rs, "feature") + "," +
                                "'" + roleid + "'" + "," +
                                fun1(rs, "permissioncode");
                        query = query + ");\n";
                        pstmt = conn.prepareStatement(query);
                        int a = pstmt.executeUpdate();
                    } catch (Exception e) {
                        output.write(query);
                    }
                }
            }


            if(output!=null) {
                output.close();
            }
            if(rs!=null) {
                rs.close();
            }
            if(rs2!=null) {
                rs2.close();
            }
            
        } catch (Exception ex) {
            resultstr += "FAILED: "+ex.getMessage()+"\n";
        } finally {
            if(conn!=null) {
                conn.close();
            }
            out.println(resultstr);
        }
%>
