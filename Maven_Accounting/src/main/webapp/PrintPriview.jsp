<%-- 
    Document   : PrintPriview
    Created on : Mar 20, 2015, 5:42:25 PM
    Author     : krawler
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        
        
        <script type="text/javascript" src="lib/ext-4/ext-all-debug.js"></script>
        <link rel="stylesheet" type="text/css" href="lib/ext-4/ext-theme-classic-all.css"/ >
        <script type="text/javascript" src="scripts/designer/ExtLabelFormatter2.js"></script>
        <link rel="stylesheet" type="text/css" href="style/custom-template-designer.css"/>
        
        <title>Print Priview</title>
    </head>

    <style>
        body {
          background: #cccccc;
        }

        page[size="A4"] {
          background: white;
          width: 21cm;
          height: 29.7cm;
          display: block;
          margin: 0 auto;
          margin-bottom: 0.5cm;
          box-shadow: 0 0 0.5cm rgba(0, 0, 0, 0.5);
        }

        @media print {
          body, page[size="A4"] {
            margin: 0;
            box-shadow: 0;
          }
        }

        .x-panel-body-default{
            background: none ;
            border-color: '';
            border-style: none;
            border-width: '';
            color:'';
            font-size: '';
        }
        
        .sectionclass_element {
            padding : 0px;
            min-height: 25px;
            overflow: hidden;
            display: block;
            height : auto !important;
        }
        
        .sectionclass_parent {
            margin: 5px;
        }
        
        .sectionclass_field_container {
            margin: 5px;
            padding: 5px;
        }
        
        .sectionclass_table_container {
            margin: 0 5px 5px;
            /*padding: 0 5px 5px;*/
        }
        
        .sectionclass_field {
            -moz-box-sizing: border-box;
            border : none;
            padding : 0px;
        }
        
        .classTextAligment_left {
            margin-top: 0px;
            text-align: left;
        }
        
    </style>
    <link rel="shortcut icon" href="../../images/favicon.png"/>
    <script src="js/prefixfree.min.js"></script>

</head>

<body>
    <!-- <body id="idPrintPriviewBody">
         </body>
    -->
     <page id="page1" size="A4"></page>
     
     <script src='http://codepen.io/assets/libs/fullpage/jquery.js'></script>

</html>
