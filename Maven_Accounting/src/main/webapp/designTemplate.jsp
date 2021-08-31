<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page contentType="text/html; charset=UTF-8" %> 
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <title id="acctitle">Deskera Accounting - Designer</title>
        <!-- css -->
        <link rel="stylesheet" type="text/css" href="style/template-editor.css"/>
        <link rel="stylesheet" type="text/css" href="lib/ext-4/ext-theme-classic-all.css"/>
        <!--<script type="text/javascript" src="scripts/designer/ExtLabelFormatter.js?v=3"></script>-->
        <!--link rel="stylesheet" type="text/css" href="../../style/taxform.css?v=3"/-->
        <!--[if lte IE 7]>
                    <link rel="stylesheet" type="text/css" href="../../style/ielte6hax.css" />
            <![endif]-->
        <!--[if IE 7]>
                     <link rel="stylesheet" type="text/css" href="../../style/ie7hax.css" />
             <![endif]-->
        <!--[if IE 8]>
                     <link rel="stylesheet" type="text/css" href="../../style/ie8hax.css" />
             <![endif]-->
        <!--[if gte IE 8]>
                     <link rel="stylesheet" type="text/css" href="../../style/ie8hax.css" />
             <![endif]-->
        <!-- /css -->
        <link rel="shortcut icon" href="images/deskera.png"/>
        <style>
            #_designerPanel-body {
            background: url("../../images/grid.png") repeat scroll center top #FFFFFF;
                    margin: 0 auto;
                    overflow: hidden;
                    position: relative;
            }
        </style>
    </head>
   <body>
        <!-- js -->
        <script type="text/javascript">
            var defaultFieldGlobalStore;
            var pagelayoutproperty = eval('[{},{}]');// First Json Object = Page Layout property; Second Json Object = Line level table style
            var tableproperties;
            var labels = new Array();
            var textboxes = new Array();
            var images = new Array();
            var documentLineColumns = [];
            var componentCollection = [];
            var _CustomDesign_moduleId = '<%=request.getParameter("_m")%>'
            var _CustomDesign_templateId = '<%=request.getParameter("_t")%>'
            var _CustomDesign_templateSubtype = '<%=request.getParameter("_s")%>'
            var lineobj;
            var isXMatched = false;
            var isYMatched = false;
            var isrightMatched = false;
            var isbottomMatched = false;
            var sectionWindow_Top = 0;
            var sectionWindow_Left = 0;
            var defaultdecimalvalue=2;
        </script>
        <script type="text/javascript" src="lib/ext-4/ext-all-debug.js"></script>
        <script type="text/javascript" src="scripts/designer/WtfGraphics.js"></script>
        <script type="text/javascript" src="scripts/designer/CommonDesignerFunctions.js"></script>
        <script type="text/javascript" src="scripts/designer/imageUpload.js"></script>
        <script type="text/javascript" src="scripts/designer/ExtLabelFormatter.js"></script>
        <script type="text/javascript" src="scripts/designer/SectionWindow.js"></script>
        <script type="text/javascript" src="scripts/designer/DocumentDesigner1.js"></script>
        <script type="text/javascript" src="scripts/designer/PropertyWin.js"></script>
        <script type="text/javascript" src="scripts/designer/HeaderProperty.js"></script>
        <script type="text/javascript" src="scripts/designer/PropertiesBox.js"></script>
        <script type="text/javascript" src="scripts/designer/PageLayoutProp.js"></script>
        <div style="display:none;">
            <iframe id="downloadframe" name="downloadframe"></iframe>
        </div>
        <form id="designpanelpreview" target="downloadframe"
              method="post" action="CustomDesign/showSamplePreview.do">
            <input type="hidden" name="json" value="[]"/>
            <input type="hidden" name="html" value=""/>
            <input type="hidden" name="moduleid" value="1"/>
        </form>
    </body>
</html>
