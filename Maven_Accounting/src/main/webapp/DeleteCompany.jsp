<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page language="java" %>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.text.*"%>
<%@ page import="com.krawler.common.util.URLUtil" %>
<%@ page import="com.krawler.esp.utils.ConfigReader" %>
<%@page import="com.krawler.esp.handlers.SendMailHandler"%>
<%@page import="javax.mail.MessagingException"%>
<%@page import="com.krawler.esp.web.resource.Links"%>
<%@page import="com.krawler.spring.sessionHandler.sessionHandlerImpl" %>
<%
        String fromDB = "accounting_15072014";
        String receiverEmails = "sagar.mahamuni@deskera.com";
        String dbUser = "krawler";
        String dbPass = "krawler";
        String currentSubdomain = "";
        String connectionURL = "jdbc:mysql://localhost:3306/"+fromDB+"?user="+dbUser+"&password="+dbPass;
        Connection conn = null;
        try {
            ResultSet rs = null;

            PreparedStatement pstmt = null;
            CallableStatement cstmt = null;
            Class.forName("com.mysql.jdbc.Driver");
            
            conn = DriverManager.getConnection(connectionURL);

            String fetchRecords = "select subdomain,companyid from company where subdomain !='swt'";
            pstmt = conn.prepareStatement(fetchRecords);
//            pstmt.setInt(1, 0);
            rs = pstmt.executeQuery();
            int cnt = 0;
            while (rs.next()) {
                String subdomain = rs.getString("subdomain");
                String companyid = rs.getString("companyid");
                currentSubdomain = subdomain;
                PreparedStatement companyChkpstmt = conn.prepareStatement("select companyid from company where companyid =?");
                companyChkpstmt.setString(1, companyid);
                ResultSet rscompanyChk = companyChkpstmt.executeQuery();
                
                if(rscompanyChk.next()) {
                    /*
                    *  Please change this code
                    */
                    cstmt = conn.prepareCall("{call deletecompanydata(?,?)}");
                    cstmt.setString(1, fromDB);
                    cstmt.setString(2, subdomain);
                    cstmt.executeUpdate();
                    cstmt.close();

                    /*
                    * END
                    */

//                    PreparedStatement pstmt1 = conn.prepareStatement("update subdomaintodelete set deleted=? where companyid=?");
//                    pstmt1.setInt(1, 1);
//                    pstmt1.setString(2, companyid);
//                    pstmt1.executeUpdate();
//                    pstmt1.close();
//                    try {
//                        SendMailHandler.postMail(new String[] {receiverEmails}, "[CRM] Deleted "+subdomain+" company", "", "", "admin@deskera.com");
//                    } catch(MessagingException e) {
//                        System.out.println(e.getMessage());
//                    }
                    cnt++;
                    if(cnt%10==0) {
                        rs.close();
                        pstmt.close();
                        conn.close();
                        conn = DriverManager.getConnection(connectionURL);
                        pstmt = conn.prepareStatement(fetchRecords);
//                        pstmt.setInt(1, 0);
                        rs = pstmt.executeQuery();
                    }
                    out.println(cnt+") Successfully deleted for company : "+subdomain);
                    System.out.println(cnt+") Successfully deleted for company : "+subdomain);
                } else {
//                    PreparedStatement pstmt1 = conn.prepareStatement("update subdomaintodelete set deleted=? where companyid=?");
//                    pstmt1.setInt(1, 1);
//                    pstmt1.setString(2, companyid);
//                    pstmt1.executeUpdate();
//                    pstmt1.close();
//                    try {
//                        SendMailHandler.postMail(new String[] {receiverEmails}, "[CRM] Company not exist "+subdomain, "", "", "admin@deskera.com");
//                    } catch(MessagingException e) {
//                        System.out.println(e.getMessage());
//                        out.println(e.getMessage());
//                    }
//                    out.println(cnt+") Company already exist : "+subdomain);
                }
                companyChkpstmt.close();
                rscompanyChk.close();
            }
            rs.close();
            pstmt.close();
        } catch (Exception ex) {
            System.out.println("Error for Subdomain : " + currentSubdomain + ex.getMessage());
            out.println("Error for Subdomain : " + currentSubdomain + ex.getMessage());
        } finally{
            if(conn != null) {
                conn.close();
            }
//            try {
//                SendMailHandler.postMail(new String[] {receiverEmails}, "[ERP] Hurreeee Process Completed", "", "", "admin@deskera.com");
//            } catch(MessagingException e) {
//                System.out.println(e.getMessage());
//                out.println(e.getMessage());
//            }
        }
%>
