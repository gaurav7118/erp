<%-- 
    Ticket Number: ERM-334
    When Permission is given to user by admin.    
    After successful login user will be redirected to startworkorder.jsp page
    Document   : startworkorder
    Created on : May 18, 2018, 11:53:40 AM
    Author     : krawler/Amit K.
--%>

<%@page import="com.krawler.spring.sessionHandler.CompanySessionClass"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    HttpSession sessionObj = request.getSession();
    if (sessionObj != null && sessionObj.getAttribute("initialized") != null && sessionObj.getAttribute("initialized").equals("true")) {
        String cdomain = (String) sessionObj.getAttribute("cdomain");

        CompanySessionClass companySessionObj = (CompanySessionClass) sessionObj.getAttribute(cdomain);
        String val = companySessionObj != null && companySessionObj.getPermissions() != null && companySessionObj.getPermissions().containsKey("barcodescanner") && companySessionObj.getPermissions().get("barcodescanner") != null ? companySessionObj.getPermissions().get("barcodescanner").toString() : "";
        /**
         * If user has work order permission then only user can access this
         * page. If user has permission then barcodescanner = 2
         * If user has work order as well as permission for DO and GRN then user can access this page 
         * If user has permission then barcodescanner = 3
         */
        if (val.equals("2") || val.equals("3")) {
%>
<!DOCTYPE html>
<html>
    <head>
        <title>Start Work Order</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="shortcut icon" href="images/favicon.png"/>
        <link rel="stylesheet" type="text/css" href="style/generateOrder.css"/>
        <script type="text/javascript" src="scripts/startWorkOrder.js"></script>
        <script type="text/javascript" src="lib/adapter/wtf/wtf-base.js"></script>
        <script type="text/javascript" src="lib/wtf-all-debug.js"></script>
        <script type="text/javascript" src="scripts/WtfChannel.js"></script>
    </head>
    <body onload="onFormLoad_WO()">
        <div class="form-style-10">
            <div class="logo">
                <img id="companyLogo" src="<%=getServletContext().getInitParameter("platformURL")%>b/<%=com.krawler.common.util.URLUtil.getDomainName(request)%>/images/store/?company=true" alt="logo"/>
                <input type="button" name="signOutBtn" id="signOutBtn" value="Sign Out" onclick="signOut('signout')" style="float: right;"/>
            </div>
            <h1>Start Work Order<span><br>Please provide order details below</span></h1>
            <form method="post" name="orderForm" id="orderForm">
                <div class="inner-wrap">
                    <label>
                        Work Order Number: <input type="text" name="linkWONumber" id="linkDocNumber_WO" onchange="onLinkedDocNumberChange_WO()" required/>
                    </label>
                    <input type="button" name="validateLinkDocBttn" id="validateLinkDocBttn" value="Validate Work Order" onclick="validateLinkDocNumber_WO()"/>
                </div>

                <div class="inner-wrap" id="productDetails_WO">
                    <label id="productDetailsLabel">Product Details: <table title="Product Details" name="productDetailsTable" id="productDetailsTable_WO">
                            <tr>
                                <th>Product Name</th>
                                <th>Product Code</th>
                                <th>Quantity</th>
                                <th>Product Type</th>
                            </tr>
                        </table>
                    </label>
                </div>

                <div class="inner-wrap">
                    <label>Barcode: <input type="text" name="barcode" id="barcode_WO" onchange="addBarcode_WO()" disabled/></label>
                    <label>Scanned Barcodes: <textarea name="scanned_barcodes" id="scanned_barcodes_WO" rows="5" required disabled></textarea></label>
                    <input type="button" name="removeBarcodeBttn" id="removeBarcodeBttn_WO" value="Remove Last Barcode" onclick="removeLastBarcode_WO()" disabled/>
                </div>

                <div class="button-section">
                    <input type="button" name="createDocBttn" id="createDocBttn_WO" value="Start Work Order" onclick="validateAndSaveDoc_WO()" disabled/>
                    <input type="reset" name="reset" value="Reset" onclick="onFormLoad_WO()"/>
                </div>
            </form>
        </div>
    </body>
</html>
<%
        } else {
            response.sendRedirect("./");
        }
    } else {
        response.sendRedirect("./");
    }
%>