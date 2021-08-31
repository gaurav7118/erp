<%-- 
    Document   : irascallback
    Created on : 17 Jul, 2018, 8:28:37 PM
    Author     : krawler
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String state = request.getParameter("state");
    String code = request.getParameter("code");
    String scope = request.getParameter("scope");
    String ids= request.getParameter("ids");
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="shortcut icon" href="images/favicon.png"/>
        <link rel="stylesheet" type="text/css" href="style/iras_callback.css"/>
        <link rel="stylesheet" type="text/css" href="lib/resources/css/wtf-all.css"/>
        <script type="text/javascript" src="scripts/irasgstform5esubmissioncallback.js"></script>
        <script type="text/javascript" src="lib/adapter/wtf/wtf-base.js"></script>
        <script type="text/javascript" src="lib/wtf-all-debug.js"></script>
        <script type="text/javascript" src="scripts/WtfChannel.js"></script>
        <title>GST Form-5 e-Submission</title>
    </head>
    <body onload="onFormLoad_IRAS_GSTForm5Submission('<%= state%>','<%= code%>','<%= scope%>','<%= ids%>')">
        <div class="form-style-10">
            <div class="logo">
                <center><img id="companyLogo" src="<%=getServletContext().getInitParameter("platformURL")%>b/<%=com.krawler.common.util.URLUtil.getDomainName(request)%>/images/store/?company=true" alt="logo"/></center>
            </div>
            <center><h1>GST Form-5 e-Submission</h1></center>
            <form method="post" name="orderForm" id="irasForm">
                <div class="inner-wrap" id="irasDetails">
                    <label id="irasDetailsLabel">
                        <Center>
                            <p title="User Message" name="irasUserMessage" id="irasUserMessage"></p>
                            <table title="IRAS Details" name="irasDetailsTable" id="irasDetailsTable">
                            </table>
                        </center>
                    </label>
                </div>
                <div class="button-section">
                    <Center><input type="button" name="createDocBttn" id="createDocBttn_IRAS" value="Close" onclick="close_Form5()" disabled/></center>
                </div>
            </form>
        </div>
    </body>
</html>
