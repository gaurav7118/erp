<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Learn with Google</title>
<!--        <link rel="stylesheet" href="//fonts.googleapis.com/css?family=Open+Sans:300,200,500,500&amp;subset=latin">-->

        <link rel='stylesheet' type='text/css' href='fullcalendar-2.1.1/fullcalendar.css' />
        <script type="text/javascript" src="fullcalendar-2.1.1/lib/jquery.min.js"></script>
        <script type="text/javascript" src="fullcalendar-2.1.1/lib/jquery-ui.custom.min.js"></script>
        <script src='lib/moment.min.js'></script>
        <script type='text/javascript' src='fullcalendar-2.1.1/fullcalendar.js'></script>
        <script type='text/javascript' src='fullcalendar-2.1.1/fullcalendar.min.js'></script>

        <link rel='stylesheet' type='text/css' href='fullcalendar-2.1.1/fullcalendar.print.css' media='print' />
<!--         <script type='text/javascript' src='../../scripts/Calendar.js'></script>-->

<style>
        .custcss {
            list-style: inside square;
            font-size: 20px;
             color: #c5eff7;
        }
        .vencss {
            list-style: inside square;
            font-size: 20px;
             color: #c8f7c5;
        }
    </style>
    </head>
    <body>
        <table><tr>
                <td style="padding:20px;">
                    <ol class="custcss"><li><font color="black">Customer Invoice Due</font></li></ol></td>
                <td>
                    <ol class="vencss"><li><font color="black">Vendor Invoice Due</font></li></ol></td>
        </tr></table>
        <div id='calendar'></div>
    </body>
</html>
