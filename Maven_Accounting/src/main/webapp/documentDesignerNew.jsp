<%--
    Document   : DragDrop4
    Created on : Mar 12, 2015, 5:02:07 PM
    Author     : krawler
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script type="text/javascript" src="lib/ext-4/ext-all-debug.js"></script>
        <link rel="stylesheet" type="text/css" href="lib/ext-4/ext-theme-classic-all.css"/>
        <script type="text/javascript" src="scripts/designer/ExtColorPicker.js"></script>
        <script type="text/javascript" src="scripts/designer/CommonDesignerFunctionsNew.js"></script>
        <script type="text/javascript" src="scripts/designer/ExtWatermark.js"></script>
        <script type="text/javascript" src="scripts/designer/documentDesignerNew.js"></script>
        <script type="text/javascript" src="scripts/designer/ExtLabelFormatterNew.js"></script>
        <script type="text/javascript" src="scripts/designer/HeaderPropertyNew.js"></script>
        <script type="text/javascript" src="scripts/designer/PageLayoutProp.js"></script>
        <script type="text/javascript" src="scripts/designer/imageUploadNew.js"></script>
        <script type="text/javascript" src="scripts/designer/ageingDetailsTable.js"></script>
        <script type="text/javascript" src="scripts/designer/groupingSummaryTable.js"></script>
        <script type="text/javascript" src="scripts/designer/FormulaBuilderDocumentDesigner.js"></script>
        <script type="text/javascript" src="scripts/designer/FieldsModelDocumentDesigner.js"></script>
        <!--JS for Details Table component-->
        <script type="text/javascript" src="scripts/designer/DetailsTable.js"></script>
        <link rel="stylesheet" type="text/css" href="style/ExtColorPicker.css"/>
        <link rel="stylesheet" type="text/css" href="style/custom-template-designer.css"/>
        <link rel="shortcut icon" href="../../images/favicon.png"/>
        
       <script type="text/javascript">
             var companyid = '<%=request.getParameter("_companyid")%>';
             var defaultFieldGlobalStore;
             var pagelayoutproperty = eval('[{},{}]');// First Json Object = Page Layout property; Second Json Object = Line level table style
             var documentLineColumns = [];
             var documentLineColumnsArray = [];
             var detailsTableColumnsObj = {};//object for storing Details Table columns data
             var detailsTableCount = {};//object or storing count with subtype of Details Table
             var ageingColumns = [];
             var ageingColumnsArray = [];
             var groupingSummaryColumns = [];
             var groupingSummaryColumnsArray = [];
             var componentCollection = [];
             var templateName='<%=request.getParameter("_tname")%>'
             document.title=templateName;
             var _CustomDesign_moduleId = '<%=request.getParameter("_m")%>'
             var _countryid = '<%=request.getParameter("_countryid")%>'
             var _CustomDesign_templateId = '<%=request.getParameter("_t")%>'
             var _isdefaulttemplate = '<%=request.getParameter("_isdft")%>';
             var _CustomDesign_templateSubtype = '<%=request.getParameter("_s")%>'
             var _amountDecimalPrecision = '<%=request.getParameter("_amountdecimal")%>'
             var _unitpriceDecimalPrecision = '<%=request.getParameter("_unitpricedecimal")%>'
             var _quantityDecimalPrecision = '<%=request.getParameter("_quantitydecimal")%>'
             Ext.tip.QuickTipManager.init();
	</script>

        <style>
            .green-box {
                background-color: #fec;
                border: 2px solid #5a7;
                border-radius: 5px;
                box-shadow: 2px 2px 2px #bbb;
                padding: 5px;
            }

            .classSaveDocument {
                    background: none repeat scroll 0 0 #fffea1;
                    border: 1px solid #fc0;
                    font-family: Arial;
                    padding: 10px;
                    position: fixed;
                    left:50%;
                    display:none;
                    

                }
        </style>


    </head>

    <body>
        <div id="idDivSaveDoc" class="classSaveDocument">Document saved successfully.</div>
        <div id="idDivDefaultSaveDoc" class="classSaveDocument">You cannot change default template.</div>
    </body>
    
</html>
