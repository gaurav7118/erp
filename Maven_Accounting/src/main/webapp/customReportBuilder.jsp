<%-- 
    Document   : customReportBuilder
    Created on : 3 Feb, 2016, 2:46:22 PM
    Author     : krawler
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
        <title></title>

        <link rel="stylesheet" type="text/css" href="lib/ext-6.0.1/theme-classic-all.css"/>
        <link rel="stylesheet" type="text/css" href="lib/ext-6.0.1/theme-classic-all_1.css"/>
        <link rel="stylesheet" type="text/css" href="lib/ext-6.0.1/theme-classic-all_2.css"/>
        <link rel="stylesheet" type="text/css" href="lib/packages/charts/classic/classic/resources/charts-all.css"/>
        <link rel="stylesheet" type="text/css" href="style/customreportbuilder/custom-report-builder.css"/>
        <link rel="stylesheet" href="../../lib/amcharts/plugins/export/export.css" type="text/css" media="all" />
        <link rel="shortcut icon" href="../../images/favicon.png"/>
    </head>
    <body>
        <script type="text/javascript">
            var companyName = '<%=request.getParameter("companyName")%>'
            if(companyName.indexOf('and') >  -1){
                companyName = companyName.replace('and', '&');
            }
            document.title=companyName;
            var amountDecimalPrecision = '<%=request.getParameter("_amountdecimal")%>'
            var unitpriceDecimalPrecision = '<%=request.getParameter("_unitpricedecimal")%>'
            var quantityDecimalPrecision = '<%=request.getParameter("_quantitydecimal")%>'
            var reportPreviewPageSize = 10;
            var financialYearFromDate;
            var financialYearToDate;
            var reportScrollMinColumn = 6;
        </script>   

        <script type="text/javascript" src="../../lib/ext-6.0.1/ext-all.js"></script>  
        
        <!-- Amcharts lib-->
        <script src="../../lib/amcharts/amcharts.js"></script>
        <script src="../../lib/amcharts/pie.js"></script>
        <script src="../../lib/amcharts/serial.js"></script>
        <script src="../../lib/amcharts/gauge.js"></script>
        <script src="../../lib/amcharts/plugins/export/export.min.js"></script>
        <script src="../../lib/amcharts/themes/light.js"></script>
        <script src="../../lib/amcharts/themes/black.js"></script>
        <script src="../../lib/amcharts/themes/dark.js"></script>
        <script src="../../lib/amcharts/themes/patterns.js"></script>
        <script src="../../lib/amcharts/themes/chalk.js"></script>
        
        <script type="text/javascript" src="scripts/customreportbuilder/ExtMultiSelectCombo.js"></script>
        <script type="text/javascript" src="../../props/msgs/messages.js"></script>
        <script type="text/javascript" src="../../lib/packages/ux/classic/src/ExportReportInterface.js"></script>  
        <script type="text/javascript" src="../../lib/packages/ux/classic/src/ProgressBarPager.js"></script>        
        <script type="text/javascript" src="../../lib/packages/ux/classic/src/GridPageSize.js"></script>        
       <script type="text/javascript" src="../../lib/packages/exporter/src/exporter/File.js"></script>        
        <script type="text/javascript" src="../../lib/packages/charts/src/draw/Color.js"></script>        
        <!--        <script type="text/javascript" src="../../lib/packages/exporter/src/exporter/file/Base.js"></script>        -->
        <script type="text/javascript" src="../../lib/packages/charts/src/chart/theme/Base.js"></script>
        <!--<script type="text/javascript" src="../../lib/packages/core/src/data/request/Base.js"></script>-->        
        <script type="text/javascript" src="../../lib/packages/core/src/class/Base.js"></script>        
        <!--<script type="text/javascript" src="../../lib/packages/pivot/src/pivot/filter/Base.js"></script>-->        
        <!--<script type="text/javascript" src="../../lib/packages/pivot/src/pivot/result/Base.js"></script>-->        
        <!--<script type="text/javascript" src="../../lib/packages/pivot/src/pivot/axis/Base.js"></script>-->        
        <!--<script type="text/javascript" src="../../lib/packages/pivot/src/pivot/matrix/Base.js"></script>-->        
        <script type="text/javascript" src="../../lib/packages/exporter/src/exporter/Base.js"></script>        
        <script type="text/javascript" src="../../lib/packages/exporter/src/exporter/file/Base.js"></script>        


        <script type="text/javascript" src="../../lib/packages/exporter/src/exporter/file/excel/Worksheet.js"></script>        
        <script type="text/javascript" src="../../lib/packages/exporter/src/exporter/file/excel/Table.js"></script>        
        <script type="text/javascript" src="../../lib/packages/exporter/src/exporter/file/excel/Style.js"></script>        
        <script type="text/javascript" src="../../lib/packages/exporter/src/exporter/file/excel/Row.js"></script>        
        <script type="text/javascript" src="../../lib/packages/exporter/src/exporter/file/excel/Cell.js"></script>        
        <script type="text/javascript" src="../../lib/packages/exporter/src/exporter/file/excel/Column.js"></script>        
        <script type="text/javascript" src="../../lib/packages/ux/src/ajax/Simlet.js"></script>        
        <script type="text/javascript" src="../../lib/packages/exporter/src/exporter/file/excel/Workbook.js"></script>        
        <script type="text/javascript" src="../../lib/packages/ux/src/ajax/DataSimlet.js"></script>        
        <script type="text/javascript" src="../../lib/packages/ux/classic/src/SlidingPager.js"></script>        
        <script type="text/javascript" src="../../lib/packages/ux/classic/src/ProgressBarPager.js"></script>        
        <script type="text/javascript" src="../../lib/packages/exporter/src/exporter/Excel.js"></script>   
        <script type="text/javascript" src="../../lib/amcharts/plugins/export/libs/jszip/jszip.js"></script>
        <script type="text/javascript" src="../../lib/amcharts/plugins/export/libs/FileSaver.js/FileSaver.js"></script>

        <!--<script type="text/javascript" src="../../lib/packages/exporter/build/exporter-debug.js"></script>-->      


        <script type="text/javascript" src="../../lib/packages/ux/src/ajax/JsonSimlet.js"></script>        
        <script type="text/javascript" src="../../lib/packages/pivot/build/pivot.js"></script>        
        <script type="text/javascript" src="../../lib/packages/pivot/build/PivotGrid.js"></script>        
        <script type="text/javascript" src="../../lib/packages/charts/charts.js"></script>        
        <!-- <script type="text/javascript" src="scripts/customreportbuilder/customReportSettings.js"></script> -->
        <script type="text/javascript" src="../../lib/packages/charts/src/chart/CartesianChart.js"></script>     
        
        <script type="text/javascript" src="scripts/customreportbuilder/ExtGlobal.js"></script>
        <script type="text/javascript" src="scripts/customreportbuilder/vtypes.js"></script>
        <script type="text/javascript" src="scripts/customreportbuilder/customReportSettingsNew.js"></script>
        <script type="text/javascript" src="scripts/customreportbuilder/customReportLauncherNew.js"></script>
        
        <div id="header" style="height: 40px;padding-left:5px;vertical-align:middle;">
            <img id="companyLogo" src="<%=getServletContext().getInitParameter("platformURL")%>b/<%=com.krawler.common.util.URLUtil.getDomainName(request)%>/images/store/?company=true" alt="logo"/>	
            <img src="../../images/CustomReportBuilder/report-builder-logo3.png" alt="accounting" style="margin-left:4px;margin-top:7px;" />
        </div>
        <div style="display:none;">
            <iframe id="downloadframe" name="downloadframe"></iframe>
        </div>
    </body>
</html>