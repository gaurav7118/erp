
<%@page import="com.krawler.spring.sessionHandler.CompanySessionClass"%>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>

<%
    HttpSession sessionObj = request.getSession();
    if (sessionObj != null && sessionObj.getAttribute("initialized") != null && sessionObj.getAttribute("initialized").equals("true")) {
        String cdomain = (String) sessionObj.getAttribute("cdomain");

        CompanySessionClass companySessionObj = (CompanySessionClass) sessionObj.getAttribute(cdomain);
        String val = companySessionObj != null && companySessionObj.getPermissions() != null && companySessionObj.getPermissions().containsKey("barcodescanner") && companySessionObj.getPermissions().get("barcodescanner") != null ? companySessionObj.getPermissions().get("barcodescanner").toString() : "";
        /**
         * If user has DO and GRN permission then only user can access this
         * page. If user has permission then barcodescanner=1.
         * If user has work order as well as permission for DO and GRN then user can access this page 
         * If user has permission then barcodescanner = 3
         */
        if (val.equals("1") || val.equals("3")) {
            String docType = request.getParameter("docType") != null ? request.getParameter("docType") : "";
%>

<!DOCTYPE html>
<html>
    <head>
        <title>Generate Order</title>
        <meta charset="UTF-8">
        <link rel="shortcut icon" href="images/favicon.png"/>
        <link rel="stylesheet" type="text/css" href="style/generateOrder.css"/>
        <script type="text/javascript" src="scripts/generateOrder.js"></script>
        <script type="text/javascript" src="lib/adapter/wtf/wtf-base.js"></script>
        <script type="text/javascript" src="lib/wtf-all-debug.js"></script>
        <script type="text/javascript" src="scripts/WtfChannel.js"></script>
    </head>
    <body onload="onFormLoad()">
        <div class="form-style-10">

            <div class="logo">
                <img id="companyLogo" src="<%=getServletContext().getInitParameter("platformURL")%>b/<%=com.krawler.common.util.URLUtil.getDomainName(request)%>/images/store/?company=true" alt="logo"/>   

                <input type="button" name="signOutBtn" id="signOutBtn" value="Sign Out" onclick="signOut('signout')" style="float: right;"/>
            </div>

            <h1>Generate Order<span><br>Please provide order details below</span></h1>
            <form method="post" name="orderForm" id="orderForm">
                <div class="inner-wrap">
                    <label>Document Type: <select size="1" name="docType" id="docType" onchange="onDocTypeChange()" required>
                            <% if (docType.equalsIgnoreCase("DO")) { %>
                                <option selected value ="DO">Delivery Order</option>
                            <%} else if (docType.equalsIgnoreCase("GR")) {%>
                                <option selected value ="GR">Goods Receipt Note</option>
                            <%} else {%>
                                <option value ="DO">Delivery Order</option>
                                <option value ="GR">Goods Receipt Note</option>
                            <%}%>
                        </select>
                    </label>
                    <label>Linked Document Type: <select size="1" name="linkDocType" id="linkDocType" onchange="onLinkedDocTypeChange()" required>
                            <option value ="SO">Sales Order</option>
                            <option value ="SI">Sales Invoice</option>
                        </select>
                    </label>
                    <label>Linked Document Number: <input type="text" name="linkDocNumber" id="linkDocNumber" onchange="onLinkedDocNumberChange()" required/></label>
                    <input type="button" name="validateLinkDocBttn" id="validateLinkDocBttn" value="Validate Linked Document" onclick="validateLinkDocNumber()"/>
                </div>

                <div class="inner-wrap" id="productDetails">
                    <label id="productDetailsLabel">Product Details: <table title="Product Details" name="productDetailsTable" id="productDetailsTable">
                            <tr>
                                <th>Product Name</th>
                                <th>Product Code</th>
                                <th>Quantity</th>
                            </tr>
                        </table>
                    </label>
                </div>

                <div class="inner-wrap">
                    <label>Barcode: <input type="text" name="barcode" id="barcode" onchange="addBarcode()" disabled/></label>
                    <label>Scanned Barcodes: <textarea name="scanned_barcodes" id="scanned_barcodes" rows="5" required disabled></textarea></label>
                    <input type="button" name="removeBarcodeBttn" id="removeBarcodeBttn" value="Remove Last Barcode" onclick="removeLastBarcode()" disabled/>
                </div>

                <div class="button-section">
                    <input type="button" name="createDocBttn" id="createDocBttn" value="Create Document" onclick="validateAndSaveDoc()" disabled/>
                    <input type="reset" name="reset" value="Reset" onclick="onFormLoad()"/>
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