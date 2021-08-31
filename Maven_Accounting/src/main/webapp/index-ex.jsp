<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page contentType="text/html; charset=UTF-8" %> 
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <%@page import="com.krawler.spring.storageHandler.storageHandlerImpl"%>    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="viewport" content="height=device-height, initial-scale=1.0"/>
        <title id="acctitle">ERP</title>
        <!--		<script type="text/javascript" src="http://www.google.com/jsapi"></script>-->
        <!--		<script type="text/javascript" src="../../scripts/googleapi.js"></script>-->
        <!--		<script type="text/javascript">
                                                google.load('visualization', '1', {packages: ['charteditor']});
                                        </script>-->
        <script type="text/javascript">
            /*<![CDATA[*/
            isProdBuild = true;
            function _r(url) {
                window.top.location.href = url;
            }
            /*]]>*/
        </script>
        <!-- css -->
        <link rel="stylesheet" type="text/css" href="../../lib/resources/css/wtf-all.css">
            <link rel="stylesheet" type="text/css" href="../../style/accounting.css?v=53_00">
            <link rel="stylesheet" type="text/css" href="../../style/dashboardstyles1.css"/>
            <link rel='stylesheet' type='text/css' href='fullcalendar-2.1.1/fullcalendar.css' />
            <link rel='stylesheet' type='text/css' href='fullcalendar-2.1.1/fullcalendar.print.css' media='print' />
            <link rel="stylesheet" href="../../lib/amcharts/plugins/export/export.css" type="text/css" media="all" />
            <link id="theme" rel="stylesheet" type="text/css" />
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
            <link rel="shortcut icon" href="../../images/favicon.png"/>
    </head>
    <body>
        <div id="loading-mask" style="width:100%;height:100%;background:#c3daf9;position:absolute;z-index:20000;left:0;top:0;">&#160;</div>
        <div id="loading">
            <div class="loading-indicator-init"><img src="../../images/loading.gif" style="width:16px;height:16px; vertical-align:middle" alt="Loading" />&#160;Loading...</div>
        </div>
        <!-- js -->
        <!--		<script type="text/javascript" src="../../scripts/canvg.js"></script>
                        <script type="text/javascript" src="../../scripts/rgbcolor.js"></script>
                        <script type="text/javascript" src="../../scripts/grChartImg.js"></script>-->
        <script type="text/javascript" src="../../lib/adapter/wtf/wtf-base.js"></script>
        <script type="text/javascript" src="../../lib/wtf-all.js"></script>
        <script  src="../../lib/tinymce/tinymce.min.js"></script>      
        <script  src="../../lib/tinymce/tiny_mce_popup.js"></script>  
        <script src="../../lib/amcharts/amcharts.js"></script>
        <script src="../../lib/amcharts/pie.js"></script>
        <script src="../../lib/amcharts/serial.js"></script>
        <script src="../../lib/amcharts/gauge.js"></script>
        <script src="../../lib/amcharts/plugins/export/export.min.js"></script>
        <script src="../../lib/amcharts/themes/light.js"></script>
        <script src="../../lib/amcharts/themes/black.js"></script>
        <script src="../../lib/amcharts/themes/dark.js"></script>
        
        <script type="text/javascript" src="../../scripts/common/WtfAdvHtmlEditor.js"></script>
        <script type="text/javascript" src="../../props/wtf-lang-locale.js"></script>
        <script type="text/javascript" src="../../props/msgs/messages.js"></script>        
        <script type="text/javascript" src="fullcalendar-2.1.1/lib/jquery.min.js"></script>
        <script type="text/javascript" src="fullcalendar-2.1.1/lib/jquery-ui.custom.min.js"></script>
        <script src='lib/moment.min.js'></script>
        <script type='text/javascript' src='fullcalendar-2.1.1/fullcalendar.js'></script>
        <script type="text/javascript" src="../../scripts/index-ex.js?v=53_00"></script>
        <script type="text/javascript">
            /*<![CDATA[*/
            PostProcessLoad = function () {
                setTimeout(function () {
                    Wtf.get('loading').remove();
                    Wtf.get('loading-mask').fadeOut({remove: true});
                }, 250);
                Wtf.EventManager.un(window, "load", PostProcessLoad);
            }
            Wtf.EventManager.on(window, "load", PostProcessLoad);
            /*]]>*/
        </script>
        <script type="text/javascript">
            var is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
            if (is_chrome) {
                document.write('<link rel="stylesheet" href="../../style/webkit.css" type="text/css" />');
            }
            if (navigator.userAgent.indexOf("MSIE 10") > -1) {
                document.write('<link rel="stylesheet" href="../../style/ie10hax.css" type="text/css" />');
            }
            if (navigator.userAgent.indexOf("Safari") > -1) {
                document.write('<link rel="stylesheet" href="../../style/webkit.css" type="text/css" />');
            }
        </script>
        <!-- /js -->
        <!-- html -->
        <img id="dummyCompanyLogo" style="display:none;" src="<%=getServletContext().getInitParameter("platformURLProtocolNeutral")%>b/<%=com.krawler.common.util.URLUtil.getDomainName(request)%>/images/store/?company=true" alt="logo"/>
        <div id="header" style="position: relative;">
<!--            <div id="headTimezone" class="TimezonePopup" id="wtf-gen442">
                <div class="TimezoneMessage">Please note that your timezone is different from your organization's timezone. Please <a onclick="showPersnProfile()" href="#" style="color:#445566;" class="helplinks">click here</a> to update.</div>
                <div class="TimezoneImage" onclick = "closeTimezonePop()">&nbsp</div>
            </div>
            <img id="companyLogo" src="<%=getServletContext().getInitParameter("platformURL")%>b/<%=com.krawler.common.util.URLUtil.getDomainName(request)%>/images/store/?company=true" alt="logo"/>
            <img src="../../images/Deskera-financials-text.png" alt="accounting" style="float:left;margin-left:4px;margin-top:1px;" />
            <div class="userinfo"> 
                <span id="whoami"></span><br /><a id="signout"; href="#" onclick="signOut('signout');"wtf:qtip=''><script></script></a>&nbsp;&nbsp;<a id="changepass"; href="#" onclick="showPersnProfile1();"wtf:qtip=''><script></script></a>&nbsp;&nbsp;<a id="myacc"; href="#" onclick="showPersnProfile();" wtf:qtip=''><script></script></a>&nbsp;&nbsp;<a id="cal"; href="#" onclick="callCalendar();" wtf:qtip=''><script></script></a>
            </div>
            <div id="serchForIco"></div>
            <div id="searchBar"></div>
            <div id="shortcuts" class="shortcuts">
                <div id="menulinks" style="float:right !important;position: relative;">
                    <div id="shortcutmenu6"style="float:left !important;position: relative;"></div>
                    <div id="dash6"style="float: left ! important; margin-top: 3px;">|</div>
                    <div id="shortcutmenu1"style="float:left !important;position: relative;"></div>
                    <div id="dash1"style="float: left ! important; margin-top: 3px;">|</div>
                    <div id="shortcutmenu2"style="float:left !important;position: relative;"></div>
                    <div id="dash2"style="float: left ! important; margin-top: 3px;">|</div>
                      <div id="shortcutmenu3"style="float:left !important;position: relative;"></div>
                                                                                                    <div id="dash3"style="float: left ! important; margin-top: 3px;">|</div>                
                    <div id="shortcutmenu4"style="float:left !important;position: relative;"></div>
                    <div id="dash4"style="float: left ! important; margin-top: 3px;">|</div>
                    <div id="shortcutmenu5"style="float:left !important;position: relative;"></div>
                </div>
                <div id="signupLink"style="float: right ! important; margin-top: 3px;"></div>
            </div>-->
        </div>
        <div id='centerdiv'></div>
        <div id="fcue-360-mask" class="wtf-el-mask" style="display:none;z-index:1999999;opacity:0.3;">&nbsp;</div>
        <div style="display:none;">
            <iframe id="downloadframe" name="downloadframe"></iframe>
        </div>
        <form id="designpanelpreview" target="downloadframe"
              method="post" action="CustomDesign/showSamplePreview.do">
            <input type="hidden" name="json" value="[]"/>
            <input type="hidden" name="html" value=""/>
            <input type="hidden" name="moduleid" value="1"/>
        </form>
        <!-- /html -->
    </body>
</html>
