/*////////////////////////////////////////////////////////////////////////////
//grChartImg.js  - A Wrapper Library to tranform SVG Vector Graphs to image.//
//This library is under MIT License.                                        //
//Author: Risvas Georgios (George)                                          //
//Copyright (c) 2013      Risvas Georgios (George)                          //
*/////////////////////////////////////////////////////////////////////////////



var grChartImg = {
    'supportNativeCanvas': function() {
        var objCanv = document.createElement('canvas');
        var bExist = !!objCanv.getContext;
        objCanv = null;
        return bExist;
    },
    'supportFlashCanvas': function() {
        return (typeof (FlashCanvas) != 'undefined');
    },
    'GetCanvasType': function() {
        return (this.supportNativeCanvas() == true) ? 'native' : (this.supportFlashCanvas() == true) ? 'flash' : 'other';
    },
    'GetChartVectorCode': function(strChartID) {//Retrieve the Chart Code. Specialized in Google charts
        switch (this.GetCanvasType()) {
            case 'native': //Support for Browsers with Native Canvas
                var nVectGraphs = (document.getElementById(strChartID).getElementsByTagName("SVG").length || document.getElementById(strChartID).getElementsByTagName("svg").length);
                var strChartsHTML = " ";
                var obj = document.getElementById(strChartID).getElementsByTagName("svg");
                strChartsHTML = obj[0].parentNode.innerHTML;
                break;

            default:
                var strChartsHTML = "";
                alert(WtfGlobal.getLocaleText("acc.field.Thisoperationnotsupported\nbythisversionofgrChartImglibrary"));
        }
        return strChartsHTML;
    },
    'GetImageData': function(strChartID) {//retrieve binary ImageData. Specialized in Google charts
        try {
            switch (this.GetCanvasType()) {
                case 'native':
                    //create dynamically a canvas element
                    var c = document.createElement("CANVAS");
                    //load a svg snippet code in the canvas element
                    canvg(c, this.GetChartVectorCode(strChartID));
                    //set canvas data as url
                    var imgData = c.toDataURL("image/png");
                    c = null;
                    break;
                default:
                    alert(WtfGlobal.getLocaleText("acc.field.Thisoperationnotsupported\nbythisversionofgrChartImglibrary"));
                    return false;
            }
        }
        catch (e) {
            return false;
        }
        return imgData;
    },
    'DownloadImage': function(strChartID) {//Support in all Major Browsers except IE.
        try {
            var imgData = this.GetImageData(strChartID).replace("image/png", "image/octet-stream");
            window.open(imgData);
        }
        catch (e) {
            return false;
        }
    },
    'DownloadImageDataAsImage': function(imgData) {//Support in all Major Browsers except IE.
        imgData = imgData.replace("image/png", "image/octet-stream");
        window.open(imgData);
    },
    'CopyImageToClip': function(strChartID) {
        if (navigator.userAgent.indexOf("MSIE") != -1) {//Support Only IE Browsers
            var imgData = this.GetImageData(strChartID);
            if (imgData == false) { return; }
            var oImg = document.createElement("IMG");
            var oCol = document.body.createControlRange();

            oImg.src = imgData;
            document.body.appendChild(oImg);
            oCol.addElement(oImg);
            document.body.removeChild(oImg);
            var retValue = oCol.execCommand("copy", false, null);

            //Clean up the controls
            oImg = null;
            oCol = null;
            if (retValue == false) { alert(WtfGlobal.getLocaleText("acc.field.ClipboardAccessDenied")) };
        }
    },
    'CopyImageDataToClip': function(imgData) {
        if (navigator.userAgent.indexOf("MSIE") != -1) {//Support Only IE Browsers
            var oImg = document.createElement("IMG");
            var oCol = document.body.createControlRange();

            oImg.src = imgData;
            document.body.appendChild(oImg);
            oCol.addElement(oImg);
            document.body.removeChild(oImg);
            var retValue = oCol.execCommand("copy", false, null);

            //Clean up the controls
            oImg = null;
            oCol = null;
            if (retValue == false) { alert(WtfGlobal.getLocaleText("acc.field.ClipboardAccessDenied")) };
        }
    },
    //bDialog variable values {true as dialog,false as window}
    'ShowImage': function(strChartID, bDialog) {//Opens a new window/tab or a dialog with chart as image
        if (bDialog == true) {
            var BrowserDimensions = GetDimensionsBrowser();
            var ChartWidth = parseInt(document.getElementById(strChartID).style.width);
            var ChartHeight = parseInt(document.getElementById(strChartID).style.height);
            strFeatures = "left=" + (Math.round((BrowserDimensions.Width - ChartWidth) / 2) + BrowserDimensions.Left) +
                              ",top=" + (Math.round((BrowserDimensions.Height - ChartHeight) / 2) + BrowserDimensions.Top) +
                              ",height=" + ChartHeight + ",width=" + ChartWidth;
        }
        else {
            strFeatures = ""
        }

        var imgData = this.GetImageData(strChartID);
        if (imgData == false) { return; }
        //Supports  All Browsers except IE
        if (navigator.userAgent.indexOf("MSIE") == -1) {
            window.open(imgData, "", strFeatures);
        }
        else {//Supports IE Browsers
            var oWin = window.open("", "_blank", strFeatures);
            strHTML = "<html><head><title>Chart Image</title></head><body style='margin:0'><img src=\'" + imgData + "\'></body></html>";
            oWin.document.write(strHTML);
            oWin.document.close();
        }
    },
    //jsonOptions variable structure --> {height:h,width:w,bDialog:boolean}
    //bDialog option values {true as dialog,false as window}
    'ShowImageDataAsImage': function(imgData, jsonOptions) {//Opens a new window/tab or a dialog with vector Graphs as image
        if (jsonOptions.bDialog == true) {
            var BrowserDimensions = GetDimensionsBrowser();
            var ChartWidth = parseInt(jsonOptions.width);
            var ChartHeight = parseInt(jsonOptions.height);
            strFeatures = "left=" + (Math.round((BrowserDimensions.Width - ChartWidth) / 2) + BrowserDimensions.Left) +
                                  ",top=" + (Math.round((BrowserDimensions.Height - ChartHeight) / 2) + BrowserDimensions.Top) +
                                  ",height=" + ChartHeight + ",width=" + ChartWidth;
        }
        else {
            strFeatures = ""
        }

        //Supports  All Browsers except IE
        if (navigator.userAgent.indexOf("MSIE") == -1) {
            window.open(imgData, "", strFeatures);
        }
        else {//Supports IE Browsers
            var oWin = window.open("", "_blank", strFeatures);
            strHTML = "<html><head><title>"+WtfGlobal.getLocaleText("acc.field.ChartImage")+"</title></head><body style='margin:0'><img src=\'" + imgData + "\'></body></html>";
            oWin.document.write(strHTML);
            oWin.document.close();
        }
    },
    //jsonOptions variable structure --> {height:h,width:w,imageFormat:'jpeg'|'png'}
    'VectorGraphtoImageData': function(strVectorCode, jsonOptions) {//retrieve binary ImageData.Cross browser function
        try {
            switch (this.GetCanvasType()) {
                case 'native':
                    //create dynamically a canvas element
                    var c = document.createElement("CANVAS");

                    if (strVectorCode.indexOf("<svg") != -1) {//SVG Code run in Canvas browsers only
                        //load a svg snippet code in the canvas element
                        canvg(c, strVectorCode);
                        //set canvas data as url
                        var imgData = c.toDataURL("image/png");
                    }

                    c = null;    
                    break;

                default:
                    return false;
            }
        }
        catch (e) {
            return false;
        }
        return imgData;
    }

};


//return jsonDimension {Left: left,Top: top ,Height:height,Width:width}
function GetDimensionsBrowser() {
    var l = (typeof  window.screenLeft!='undefined') ? window.screenLeft : window.screenX;
    var t = (typeof window.screenTop!='undefined')  ? window.screenTop  : window.screenY;
    var w = (typeof window.outerWidth!='undefined') ? window.outerWidth : document.documentElement.offsetWidth;
    var h = (typeof window.outerHeight!='undefined')? window.outerHeight: document.documentElement.offsetHeight;
    return {'Left':l, 'Top':t, 'Height': h, 'Width': w };
}
