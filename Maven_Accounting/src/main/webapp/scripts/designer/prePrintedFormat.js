/* 
 * JS for PrePrinted Template
 */
function loadPrePrintedPage(){
    
    //Summary table start
    var isSummaryTable = true;
    var summaryTable = $("#summaryTable");                                                      // get summary table element
    var summaryTableHeightType = "leaveOnPage"; //summary table height adjustment type
    var summaryTableTrHTML = "";
    var summaryTableContainerID = "";
    var summaryTableContainer = "";
    var summaryTableMainContainerHTML = "";
    var summaryTableContainerHeight = 0;
    $("#idLineitemTabel").removeClass("newborder1"); // remove border type 1 css
//    var platform =  navigator.platform;
//    var pageHeight = getPageHeight("a4", "portrait",platform);
    $("#page1")[0].outerHTML = $("#page1")[0].outerHTML.replace("pagenumberspan",""); //replace page number field id from html
    var pageHtmlBeforeSummaryTableHeight = $("#page1")[0].outerHTML;// get page html(before (line item + summary table height) change)
    if(summaryTable.length == 0){
        isSummaryTable = false;
    } else{
        summaryTableHeightType = summaryTable[0].getAttribute("tableHeightType");
//        summaryTableTrHTML = $("#summaryTable tbody").html();                                    // get all tr of summary table
        summaryTableContainerID = $("#summaryTable").parent().parent()[0].id;
        summaryTableContainerID = summaryTableContainerID.replace("-innerCt", "");               // get summary table parent container ID
        summaryTableContainer = $("#"+summaryTableContainerID);                                  // get summary table container element
        summaryTableContainerHeight = summaryTableContainer.height();                            // get summary table container height
        summaryTableMainContainerHTML = summaryTableContainer[0].offsetParent.offsetParent.outerHTML; // get summary table row html
        var currentLineTableContainerHeight = $("#idLineitemTabel").parent().height();           // get height of line item table container
        
        // for CM
        summaryTableContainerHeight = parseFloat((summaryTableContainerHeight * 0.026458).toFixed(1));
        currentLineTableContainerHeight = parseFloat((currentLineTableContainerHeight * 0.026458).toFixed(1));
        if(summaryTableHeightType == "leaveOnPage"){
            //No need to change height
        } else{
            $("#idLineitemTabel").parent()[0].style.setProperty("height", (currentLineTableContainerHeight + summaryTableContainerHeight) +"cm","important"); // add height of summary table row into line item row
        }
        // for CM end
        
//        $("#idLineitemTabel").parent()[0].style.setProperty("height",currentLineTableContainerHeight + summaryTableContainerHeight,"important"); // add height of summary table row into line item row
    }
    //Summary table end
    var pageHtmlAfterSummaryTableHeight = $("#page1")[0].outerHTML;                              // get page html(after (line item + summary table height) change)
//    $("#idLineitemTabel")[0].style.setProperty("table-layout","fixed","");
    var lineTableHTML = $("#idLineitemTabel").parent()[0].innerHTML;                             // get line item table html
    var pageHTMLwithoutLineItemTable_old = pageHtmlAfterSummaryTableHeight.replace(lineTableHTML,"##");                     // get Page html without lineitem table
    var pageHTMLwithoutLineItemTable_ForLastPage = pageHtmlBeforeSummaryTableHeight.replace(lineTableHTML,"##");                     // get Page html without lineitem table
//    var pageHTMLwithoutLineItemTable = pageHTML.replace(lineTableHTML,"##");                     // get Page html without lineitem table
    var containerHeight = $("#idLineitemTabel").parent()[0].offsetHeight;                        // get height of line item table container
        
    var lineItemTableWithoutTRs = lineTableHTML.replace($("#idLineitemTabel tbody").html(),"##");// get line item table without any tr
    var newLineItemTableBeginBody = lineItemTableWithoutTRs.split("##")[0];                      // get portion of line item table before tr (upto starting of table and tbody)
    var newLineItemTableEndBody = lineItemTableWithoutTRs.split("##")[1];                        // get portion of line item table after tr (ending of tbody and table)
//    var newLineItemTableEndBodyForLastPage = "";
//    if(isSummaryTable){
//        newLineItemTableEndBodyForLastPage = summaryTableTrHTML + newLineItemTableEndBody;       // if summary table present then append summary table before line item table ends
//        newLineItemTableEndBodyForLastPage = newLineItemTableEndBody;       // if summary table present then append summary table before line item table ends
//    }
    var tableHeader = $("#idLineitemTabel thead tr");                                            // get table header
    var newTableHeader = "";
    var tableHeaderTH = $("#idLineitemTabel thead th");
    for(var cnt = 0; cnt < tableHeaderTH.length; cnt ++){
        var thWidth = tableHeaderTH[cnt].width;
//        tableHeaderTH[cnt].style.setProperty("border","none","");
//        tableHeaderTH[cnt].innerHTML="";
//        newTableHeader += tableHeaderTH[cnt].outerHTML;
        newTableHeader += "<th style='width:"+ thWidth +"'></th>";
    }
    var tableTotalHeight = 0;
    var tableHeaderHeight = 0;
    tableHeaderHeight = $("#idLineitemTabel thead tr")[0].offsetHeight;                          // get table header height 
    tableTotalHeight = tableHeaderHeight;                                                        // add table header height to table total height
    newLineItemTableBeginBody = newLineItemTableBeginBody.replace(tableHeader[0].outerHTML, "<tr style='height: "+ tableHeaderHeight+"px;'>"+ newTableHeader +"</tr>"); // Replace thead tr with empty tr with header height
    
    var newLineItemTable = newLineItemTableBeginBody;                                            // assign line item table starting html
    var mainHTML = "";
    var fitOnWholeHeight = false;
    var trArray = $("#idLineitemTabel > tbody > tr");                                                // get line item table all trs array
    var pageCount = 1;
    
    for(var ind = 0; ind < trArray.length; ind++){
        var pageHTMLwithoutLineItemTable = pageHTMLwithoutLineItemTable_old;
        var trHeight = trArray[ind].offsetHeight;                                                // get row height
        
        tableTotalHeight += trHeight;                                                            // add row height into table height
        var trHTMLWithoutBorder = trArray[ind];
        for(var cnt = 0; cnt < trArray[ind].getElementsByTagName("td").length; cnt++){ // loop for removing border of line table
            trHTMLWithoutBorder.getElementsByTagName("td")[cnt].style.setProperty("border","none","");
        }
        if(tableTotalHeight < containerHeight){                 // If table height is less than container height then copy tr into table
            newLineItemTable += trHTMLWithoutBorder.outerHTML;  // copy tr into table
        } else if(tableTotalHeight == containerHeight){         // If table height equale to container height then copy tr into table and create new line item table
            newLineItemTable += trHTMLWithoutBorder.outerHTML;  // copy tr into table
            newLineItemTable += newLineItemTableEndBody;        // end line item table
            mainHTML += pageHTMLwithoutLineItemTable.replace("##", newLineItemTable); // put completed line item table into container
            //replace page number on template page
            mainHTML = mainHTML.replace("#Page Number#", pageCount);
            pageCount++; //increase page number counter
            
            newLineItemTable = newLineItemTableBeginBody;       // start new line item table for next page
            tableTotalHeight = $("#idLineitemTabel thead tr")[0].offsetHeight; // get and add thead height
            if(ind == trArray.length-1){                        // If last row exactly fits in table then raise flag
                fitOnWholeHeight = true;
            }
        } else if(tableTotalHeight > containerHeight){          // If table height greater than container height then create ne line item table and copy tr into table
            if(isSummaryTable){
                newLineItemTable += newLineItemTableEndBody;    // end line item table
                if(summaryTableHeightType == "leaveOnPage"){
                    pageHTMLwithoutLineItemTable = pageHTMLwithoutLineItemTable.replace("<tr><td>"+summaryTableMainContainerHTML+"</td></tr>", "<tr><td><div style='height:"+summaryTableContainerHeight+"cm !important;'></div></td></tr>");//remove summary table from page but leave height of section as it is
                } else{
                    pageHTMLwithoutLineItemTable = pageHTMLwithoutLineItemTable.replace("<tr><td>"+summaryTableMainContainerHTML+"</td></tr>", "");//remove summary table from page
                }
            } else{
                newLineItemTable += newLineItemTableEndBody;    // end line item table
            }
            mainHTML += pageHTMLwithoutLineItemTable.replace("##", newLineItemTable); // put completed line item table into container
            //replace page number on template page
            mainHTML = mainHTML.replace("#Page Number#", pageCount);
            pageCount++; //increase page number counter
            
            newLineItemTable = newLineItemTableBeginBody;       // start new line item table for next page
            tableTotalHeight = $("#idLineitemTabel thead tr")[0].offsetHeight; // get and add thead height
            tableTotalHeight += trHeight;
            newLineItemTable += trHTMLWithoutBorder.outerHTML;  // copy tr into table
        }
    }
    
    if(!fitOnWholeHeight){                                      // if line item last row exactly fited in conatiner then already line item table is ended above otherwise ending here
//        if(isSummaryTable){
//            newLineItemTable += newLineItemTableEndBodyForLastPage; // add line item table end with summary table
//            pageHTMLwithoutLineItemTable = pageHTMLwithoutLineItemTable.replace("<tr><td>"+summaryTableMainContainerHTML+"</td></tr>", "");//remove summary table from page
//        } else{
            newLineItemTable += newLineItemTableEndBody;        // end line item table
//        }
        mainHTML += pageHTMLwithoutLineItemTable_ForLastPage.replace("##", newLineItemTable); // put completed line item table into container
        //replace page number on template page
        mainHTML = mainHTML.replace("#Page Number#", pageCount);
        pageCount++; //increase page number counter
    }
    document.body.innerHTML = mainHTML;                         // set html of all pages to document body
    for(ind = 0; ind < $(".lineitemtablewrap").length; ind++){
        $(".lineitemtablewrap")[ind].style.setProperty("table-layout","fixed","");
    }
}
//
//
///* Function to get DPI */
//function getDPI(){
//    // create an empty element
//    var div = document.createElement("div");
//    // give it an absolute size of one inch
//    div.style.height="1in";
//    div.style.width="1in";
//    // append it to the body
//    var body = document.getElementsByTagName("body")[0];
//    body.appendChild(div);
//    // read the computed height
//    var dpi = document.defaultView.getComputedStyle(div, null).getPropertyValue('height');
//    // remove it again
//    body.removeChild(div);
//    // and return the value
//    return parseInt(dpi, 10);
//}
///* Function to calculate page height */
//function getPageHeight(ps,po,ptf){
//    var ph = 1120.00;
//    if ( ps === "a4" ) {
//        if ( po === 'portrait' ) {
//            ph = 11.49 * getDPI();
//        } else {
//            ph = 8.07 * getDPI();
//        }
//    } else if ( ps === "letter" ) {
//        if ( po === 'portrait' ) {
//            ph = 10.8 * getDPI();
//        } else {
//            ph = 8.3 * getDPI();
//        }
//    } else if ( ps === "a3" ) {
//        if ( po === 'portrait' ) {
//            ph = 16.34 * getDPI();
//        } else {
//            ph = 11.49 * getDPI();
//        }
//    }
//    return ph;
//}
//
//
///* Function to convert cm to px */
//function cm2px(cm) {
////    1 cm = 37.795276 px;
//    var d = $("<div/>").css({
//        position: 'absolute', 
//        top : '-1000cm', 
//        left : '-1000cm', 
//        height : '1000cm', 
//        width : '1000cm'
//    }).appendTo('body');
//    var px_per_cm = d.height() / 1000;
//    d.remove();
//    return cm * px_per_cm;
////    return parseFloat((cm * 37.80).toFixed(1));
//}
//
//function px2cm(px) {
////    1 px = 0.026458 cm
//    var d = $("<div/>").css({
//        position: 'absolute', 
//        top : '-1000cm', 
//        left : '-1000cm', 
//        height : '1000cm', 
//        width : '1000cm'
//    }).appendTo('body');
//    var px_per_cm = d.height() / 1000;
//    d.remove();
//    return px / px_per_cm;
////    return parseFloat((px * 0.026458).toFixed(1));
//}
