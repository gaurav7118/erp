/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//function callPaymentReportforVoucher(consolidateFlag,searchStr, filterAppend,winValue,label){
//    if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewpayment)) {
//        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
//        var panelID = "paymentReport";
//        panelID = consolidateFlag?panelID+'Merged':panelID;
//        var panel = Wtf.getCmp(panelID);
//        if(panel==null){
//            panel = new Wtf.account.ReceiptReport({
//                id : panelID,
//                border : false,
//                helpmodeid: 23,
//                isGlcodeValue:9,
//                winValue:winValue,
//                recordType:winValue,
//                searchJson: searchStr,
//                filterConjuctionCrit:filterAppend,
//                moduleid:Wtf.Acc_Make_Payment_ModuleId,
//                layout: 'fit',
//                consolidateFlag:consolidateFlag,
//                title:(label!=undefined)?label:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.pmList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
//                tabTip:(label!=undefined)?label:WtfGlobal.getLocaleText("acc.pmList.tabTitle"),  //'Payment Made Report',
//                closable: true,
//                isReceipt:false,
//                iconCls:'accountingbase makepaymentreport'
//            });
//            Wtf.getCmp('as').add(panel);
//            panel.on('journalentry',callJournalEntryDetails);
//        }
//        Wtf.getCmp('as').setActiveTab(panel);
//        Wtf.getCmp('as').doLayout();
//        showAdvanceSearchforVoucher(panel,searchStr, filterAppend);
//    }
//    else
//        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.pmList.tabTitle"));
//}

function callPaymentReportforVoucherNew(consolidateFlag,searchStr, filterAppend,winValue,label){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.purchasemakepayment, Wtf.Perm.purchasemakepayment.viewpayment)) {
        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
        var panelID = "paymentReportVoucher";
        panelID = consolidateFlag?panelID+'Merged':panelID;
        var panel = Wtf.getCmp(panelID);
        //Payment Report from GL/Ledger/Cash/Bank navigation
        if (panel != null){
            Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel = null;
        }
        if(panel==null){
              panel = new Wtf.account.ReceiptReportNew({
                id : panelID,
                border : false,
                helpmodeid: 23,
                isGlcodeValue:9,
                winValue:winValue,
                recordType:winValue,
                searchJson: searchStr,
                filterConjuctionCrit:filterAppend,
                moduleid:Wtf.Acc_Make_Payment_ModuleId,
                layout: 'fit',
                consolidateFlag:consolidateFlag,
                title:Wtf.util.Format.ellipsis((label!=undefined)?label:WtfGlobal.getLocaleText("acc.pmList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
                tabTip:(label!=undefined)?label:WtfGlobal.getLocaleText("acc.pmList.tabToolTip"),  //'Payment Made Report',
                closable: true,
                isReceipt:false,
                iconCls:'accountingbase makepaymentreport'
            });
            Wtf.getCmp('as').add(panel);
            panel.on('journalentry',callJournalEntryDetails);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel,searchStr, filterAppend);
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.pmList.tabTitle"));
}

//function callReceiptReportforVoucher(consolidateFlag,searchStr, filterAppend,winValue,label){
//    if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewreceipt)) {
//        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
//        var panelID = "receiptReport";
//        panelID = consolidateFlag?panelID+'Merged':panelID;
//        var panel = Wtf.getCmp(panelID);
//        if(panel==null){
//            panel = new Wtf.account.ReceiptReport({
//                id : panelID,
//                border : false,
//                helpmodeid: 20,
//                isGlcodeValue:9,
//                winValue:winValue,
//                recordType:winValue,
//                layout: 'fit',
//                searchJson: searchStr,
//                filterConjuctionCrit:filterAppend,
//                moduleid:Wtf.Acc_Receive_Payment_ModuleId,
//                consolidateFlag:consolidateFlag,
//                title:(label!=undefined)?label:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.prList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
//                tabTip:(label!=undefined)?label:WtfGlobal.getLocaleText("acc.prList.tabTitle"),  //'Payment Received Report',
//                closable: true,
//                isReceipt:true,
//                iconCls:'accountingbase receivepaymentreport'
//            //            activeItem : 0
//            });
//            Wtf.getCmp('as').add(panel);
//            panel.on('journalentry',callJournalEntryDetails);
//        }
//        Wtf.getCmp('as').setActiveTab(panel);
//        Wtf.getCmp('as').doLayout();
//         showAdvanceSearchforVoucher(panel,searchStr, filterAppend); 
//    }
//    else
//        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.prList.tabTitle"));
//}
function callReceiptReportforVoucherNew(consolidateFlag,searchStr, filterAppend,winValue,label){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.salesreceivepayment, Wtf.Perm.salesreceivepayment.viewreceipt)) {
        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
        var panelID = "receiptReportVoucher";
        panelID = consolidateFlag?panelID+'Merged':panelID;
        var panel = Wtf.getCmp(panelID);
        //Receipt Report from GL/Ledger/Cash/Bank navigation
        if (panel != null) {
            Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel = null;
        }
        if(panel==null){
            panel = new Wtf.account.ReceiptReportNew({
                id : panelID,
                border : false,
                helpmodeid: 20,
                layout: 'fit',
                searchJson: searchStr,
                isGlcodeValue:9,
                winValue:winValue,
                recordType:winValue,
                
                filterConjuctionCrit:filterAppend,
                moduleid:Wtf.Acc_Receive_Payment_ModuleId,
                consolidateFlag:consolidateFlag,
                title:Wtf.util.Format.ellipsis((label!=undefined)?label:WtfGlobal.getLocaleText("acc.prList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
                tabTip:(label!=undefined)?label:WtfGlobal.getLocaleText("acc.prList.tabToolTip"),  //'Payment Received Report',
                closable: true,
                isReceipt:true,
                iconCls:'accountingbase receivepaymentreport'
            });
            Wtf.getCmp('as').add(panel);
            panel.on('journalentry',callJournalEntryDetails);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
       showAdvanceSearch(panel,searchStr, filterAppend); 
         showAdvanceSearchforVoucher(panel,searchStr, filterAppend); 
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.prList.tabTitle"));
}
function  showAdvanceSearchforVoucher(panel, c, filterAppend) {
    if (c != undefined) {
        if (c != "") {
            var data = eval("(" + decodeURIComponent(c) + ")");
            if (!panel.objsearchComponent) {
                panel.getAdvanceSearchComponent();
            }
            panel.configurAdvancedSearch();
            panel.objsearchComponent.appendCaseCombo.setValue(filterAppend);
            panel.objsearchComponent.searchStore.loadData(data);
            for (var i = 0; i < data.data.length; i++) {
                panel.objsearchComponent.combovalArr.push(data.data[i].combosearch);
            }
            if(panel.Store!=undefined)
                panel.Store.on("load", panel.storeLoad, panel);
            else
                panel.storeLoad
            panel.objsearchComponent.doSearch(false);
            panel.objsearchComponent.search.enable();
        //            panel.objsearchComponent.saveSearch.enable();
        }
    }
}