
function addborder(ps, po, isGlobalTable) {
    
    var tMargin = $(document)[0].styleSheets[3].cssRules[4].style.marginTop;          // Fetching Top MArgin and bottom margin
    var lMargin = $(document)[0].styleSheets[3].cssRules[4].style.marginLeft;
    var bMargin = $(document)[0].styleSheets[3].cssRules[4].style.marginBottom;
    var rMargin = $(document)[0].styleSheets[3].cssRules[4].style.marginRight;
    var platform =  navigator.platform;                                               // checking platform  
    
    var topMargin = 0;
    var leftMargin = 0;
    var bottomMargin = 0;
    var rightMargin = 0;
    if(tMargin.indexOf("px") > -1 && bMargin.indexOf("px") > -1){
        topMargin = parseInt(tMargin.replace("px",""));
        leftMargin = parseInt(lMargin.replace("px",""));
        bottomMargin = parseInt(bMargin.replace("px",""));
        rightMargin = parseInt(rMargin.replace("px",""));
    }else if(tMargin.indexOf("cm") > -1 && bMargin.indexOf("cm") > -1){             //Convert Margins from cm to pixels.
        topMargin = cm2px(parseFloat(tMargin.replace("cm","")));
        leftMargin = cm2px(parseFloat(lMargin.replace("cm","")));
        bottomMargin = cm2px(parseFloat(bMargin.replace("cm","")));
        rightMargin = cm2px(parseFloat(rMargin.replace("cm","")));
    }else if(tMargin.indexOf("mm") > -1 && bMargin.indexOf("mm") > -1){            //Convert Margins from mm to pixels. 
        topMargin = mm2px(parseFloat(tMargin.replace("mm","")));
        leftMargin = mm2px(parseFloat(lMargin.replace("mm","")));
        bottomMargin = mm2px(parseFloat(bMargin.replace("mm","")));
        rightMargin = mm2px(parseFloat(rMargin.replace("mm","")));
    }else if(tMargin.indexOf("in") > -1 && bMargin.indexOf("in") > -1){            //Convert Margins from inch to pixels.
        topMargin = in2px(parseFloat(tMargin.replace("in","")));
        leftMargin = in2px(parseFloat(lMargin.replace("in","")));
        bottomMargin = in2px(parseFloat(bMargin.replace("in","")));
        rightMargin = in2px(parseFloat(rMargin.replace("in","")));
    }
    
    topMargin = Math.round(topMargin);
    leftMargin = Math.round(leftMargin);
    bottomMargin = Math.round(bottomMargin);
    rightMargin = Math.round(rightMargin);
    var pageHeight = 1120.00;
    pageHeight = getPageHeight(ps, po,platform);                                                // getting page height depending upon page size and orientation.
    pageHeight=Math.round(pageHeight);
    
    /*
     * addWatermarkImageToPage function adds the watermark to page if applied.
     */
    var pageWidth = $(document)[0].styleSheets[3].cssRules[5].cssRules[0].style.width;
    
    if (pageWidth.indexOf("px") != -1) {
        pageWidth = Math.round(parseInt(pageWidth.replace("px", "")));
    } else if (pageWidth.indexOf("cm") != -1) {
        pageWidth = Math.round(cm2px(parseFloat(pageWidth.replace("cm", ""))));
    } else if (pageWidth.indexOf("mm") != -1) {
        pageWidth = Math.round(mm2px(parseFloat(pageWidth.replace("mm", ""))));
    } else if (pageWidth.indexOf("in") != -1) {
        pageWidth = Math.round(in2px(parseFloat(pageWidth.replace("in", ""))));
    }
    
    var pageMargin = {
        top: topMargin,
        left: leftMargin,
        bottom: bottomMargin,
        right: rightMargin
    }
    addWatermarkImageToPage(pageHeight, pageWidth, pageMargin);
    
    var RunningHeight = 0;
    var PageNo = 1;
    var headerHeight = $('#page1>table>thead>tr').length != 0 ? $('#page1>table>thead>tr')[0].offsetHeight : 0;                     // getting various heights
    var footerHeight = $('#page1>table>tfoot>tr').length != 0 ? $('#page1>table>tfoot>tr')[0].offsetHeight : 0;
    
    var lineItemTableArr = $("table.lineitemtablewrap");
    var ind = 0;
    while(ind < lineItemTableArr.length){
        var lineItemTable = lineItemTableArr[ind];
        var lineItemTRval;
        if (isGlobalTable && isGlobalTable === "1") {
            lineItemTable.style.margin= "0px";
            lineItemTable.parentNode.style.padding = "0px";
            lineItemTRval = lineItemTable.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode;
        } else {
            lineItemTRval = lineItemTable.parentNode.parentNode.parentNode;
        }
        lineItemTRval.setAttribute("id", "lineitemtr");
        ind++;
        
        var classname = "";
        if (isGlobalTable && isGlobalTable === "1") {
            classname = "extendedRow";
        } else {
            var borderType = lineItemTable.attributes.getNamedItem("bordertype").value;
            classname = getClass(borderType);
        }
        var lineItemHeaderHeight = lineItemTable.tHead.children[0].offsetHeight;
        var columns =  lineItemTable.tHead.children[0].children; 
        var lineItemRowsTemp = lineItemTable.tBodies[0].children; 
        var lineItemRows = [];
        var refTR ;
        if (isGlobalTable && isGlobalTable === "1" ) {
            for (var rowindex=0 ; rowindex < lineItemRowsTemp.length; rowindex++) {
                var isAfterRepeatRow = "0";
                if (lineItemRowsTemp[rowindex].attributes["isafterrepeatrow"]) {
                    isAfterRepeatRow =  lineItemRowsTemp[rowindex].attributes["isafterrepeatrow"].value;
                } 
                
                if (isAfterRepeatRow === "1") {
                    break;
                } else {
                    lineItemRows.push(lineItemRowsTemp[rowindex]);
                    refTR = lineItemRowsTemp[rowindex];
                }
            } 
        } else {
            lineItemRows = lineItemRowsTemp;
            refTR = lineItemRowsTemp[lineItemRowsTemp.length-1];
        }
        
        
        RunningHeight =  headerHeight + footerHeight + topMargin + bottomMargin;
        var checkHeight = 0;
        var trHeight = 0;
        var lineItemAfterHeight = 0;
        var lineItemBeforeHeight = 0;
        var trs;
        if (isGlobalTable && isGlobalTable == "1") {
            trs = lineItemTable.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.children;
        } else {
            trs = lineItemTable.parentNode.parentNode.parentNode.parentNode.children;
        }
    
        for (var i=0 ; i < trs.length; i++) {
            trHeight = trs[i].offsetHeight;
            RunningHeight = trHeight + RunningHeight;
            checkHeight = trHeight + checkHeight;
            if (trs[i].attributes.getNamedItem("id") && trs[i].attributes.getNamedItem("id").value === 'lineitemtr') {
                break;
            }
        
        }
        var afterCheck = 0;
        for (k=0; k < trs.length; k++) {
        
            if ( afterCheck === 1 ) {
                lineItemAfterHeight += trs[k].offsetHeight;
            }
            if (trs[k].attributes.getNamedItem("id") && trs[k].attributes.getNamedItem("id").value === 'lineitemtr') {
                afterCheck = 1;
            }
        }
        var beforeCheck = 0;
        for (k=0; k < trs.length; k++) {
        
            if (trs[k].attributes.getNamedItem("id") && trs[k].attributes.getNamedItem("id").value === 'lineitemtr') {
                break;
            }		        
            lineItemBeforeHeight += trs[k].offsetHeight;
        
        }
 

        var lineitemendpage = 1;
        //check on which page line item table will end
//        lineitemendpage = Math.ceil(checkHeight/pageHeight);
//        checkHeight = checkHeight + ((lineitemendpage ) * (headerHeight + footerHeight + topMargin + bottomMargin));
//        lineitemendpage = Math.ceil(checkHeight/pageHeight);
        //Calculate correct position of line item ending page
        while(true) {
            var tempCheckHieght = (checkHeight - lineItemHeaderHeight) + ((lineitemendpage ) * (headerHeight + footerHeight + topMargin + bottomMargin + lineItemHeaderHeight));
            var templineitemendpage = Math.ceil(tempCheckHieght/pageHeight);
            if (templineitemendpage === lineitemendpage) {
                break;
            }
            lineitemendpage = templineitemendpage;
        }

        var heighttoadd = 0;
        heighttoadd = (lineitemendpage - 1 ) * (headerHeight + footerHeight + topMargin + bottomMargin + lineItemHeaderHeight);
        RunningHeight = RunningHeight + heighttoadd;
        var extraheight = RunningHeight % pageHeight;
        var page = RunningHeight / pageHeight;
   

    
        var remaining = pageHeight - extraheight;
//        if ( page > 0 ) {
//            remaining = remaining -  lineItemHeaderHeight ;
//        }    
    



        var tempCnter =1;
        var tempHeight1 = 0;
        var tempHeight = 0;
        tempHeight = headerHeight + footerHeight + topMargin + bottomMargin + lineItemHeaderHeight;
    
        tempHeight1 = tempHeight + lineItemBeforeHeight;
        var pageBreakHeight = 0;
        for (var k=0; k < lineItemRows.length; k++ ) {
      
            var rowHeight = lineItemRows[k].offsetHeight; 
            tempHeight1 += rowHeight 
            if (tempHeight1 > pageHeight) {
                pageBreakHeight += rowHeight - (tempHeight1 - pageHeight);
                var pageBreakHeightforrow = rowHeight - (tempHeight1 - pageHeight);
                //           tempCntr++;
                tempHeight1 = tempHeight;
                if (pageBreakHeightforrow > 5) {
                    pageBreakHeightforrow = pageBreakHeightforrow -5;
                //             pageBreakHeightforrow = pageBreakHeightforrow ; 
                }
                k--;
//                var rowHeight1 = lineItemRows[k].offsetHeight; 
                for (var m =0; m< lineItemRows[k].cells.length ; m++) {
                    if(classname != "extendedRowBorder4" && classname != "extendedRowBorder8"){
                        lineItemRows[k].cells[m].style.borderBottom = "1px solid black";
                    }
                    //lineItemRows[k].cells[m].style.height = rowHeight1 + pageBreakHeightforrow + "px" ;
                    lineItemRows[k].style.pageBreakAfter = "always";
                }
           
            }
        }

        remaining = remaining - pageBreakHeight;

    
//        var tr = document.createElement('tr');
//        var j,cnt;
//        var htmltoadd = "";
    }
}


function getDPI(){
    // create an empty element
    var div = document.createElement("div");
    // give it an absolute size of one inch
    div.style.height="1in";
    div.style.width="1in";
    // append it to the body
    var body = document.getElementsByTagName("body")[0];
    body.appendChild(div);
    // read the computed height
    var dpi = document.defaultView.getComputedStyle(div, null).getPropertyValue('height');
    // remove it again
    body.removeChild(div);
    // and return the value
    return parseInt(dpi, 10);
}

function px2cm(px) {
    var d = $("<div/>").css({
        position: 'absolute', 
        top : '-1000cm', 
        left : '-1000cm', 
        height : '1000cm', 
        width : '1000cm'
    }).appendTo('body');
    var px_per_cm = d.height() / 1000;
    d.remove();
    return px / px_per_cm;
}
function cm2px(cm) {
    var d = $("<div/>").css({
        position: 'absolute', 
        top : '-1000cm', 
        left : '-1000cm', 
        height : '1000cm', 
        width : '1000cm'
    }).appendTo('body');
    var px_per_cm = d.height() / 1000;
    d.remove();
    return cm * px_per_cm;
}

function newcm2px(cm){
    var px_per_cm = getDPI()/ 2.54; 
    return cm * px_per_cm; 
}

function newpx2cm(px){
    var px_per_cm = getDPI()/ 2.54; 
    return px / px_per_cm; 
}

function mm2px(mm){
    var px_per_mm = getDPI()/ 25.4;
    return mm * px_per_mm; 
}

function px2mm(px){
    var px_per_mm = getDPI()/ 25.4; 
    return px / px_per_mm; 
}
function in2px(inch){
    var px_per_in = getDPI(); 
    return inch * px_per_in; 
}

function px2in(px){
    var px_per_in = getDPI(); 
    return px / px_per_in; 
}

function getPageHeight(ps,po,ptf){
    var ph = 1120.00;
    if ( ps === "a4" ) {
        if ( po === 'portrait' ) {
            ph = 11.49 * getDPI();
        } else {
            ph = 8.07 * getDPI();
        }
    } else if ( ps === "letter" ) {
        if ( po === 'portrait' ) {
            ph = 10.8 * getDPI();
        } else {
            ph = 8.3 * getDPI();
        }
    } else if ( ps === "a3" ) {
        if ( po === 'portrait' ) {
            ph = 16.34 * getDPI();
        } else {
            ph = 11.49 * getDPI();
        }
    }
    return ph;
}

function getClass(bt) {
    var className="";
    if( bt === "border10" ) {
        className = "extendedRow";
    } else if (bt === "border1") {
        className = "extendedRowBorder1";
    } else if (bt === "border2") {
        className = "extendedRowBorder2";
    } else if (bt === "border3") {
        className = "extendedRowBorder3";
    } else if (bt === "border4") {
        className = "extendedRowBorder4";
    } else if (bt === "border5") {
        className = "extendedRowBorder5";
    } else if (bt === "border6") {
        className = "extendedRowBorder6";
    } else if (bt === "border7") {
        className = "extendedRowBorder7";
    } else if (bt === "border8") {
        className = "extendedRowBorder8";
    } else if (bt === "border9") {
        className = "extendedRowBorder9";
    }
    return className;
}
