//function callReceiptSales(winValue){
//    if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createreceipt)) {
//        var panel = Wtf.getCmp("Receipt");
//        if(panel!=null){
//            Wtf.getCmp('as').remove(panel);
//            panel.destroy();
//            panel=null;
//        }
//        if(panel==null){
//            panel = new Wtf.account.OSDetailPanel({
//                id : 'Receipt',
//                border : false,
//                isReceipt:true,
//                winValue:winValue,
//                isDirectCustomer:true,
//                moduleId:Wtf.Acc_Receive_Payment_ModuleId,
//                cls: 'paymentFormPayMthd',
//                layout: 'border',
//                helpmodeid: 9, //This is help mode id
//                title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoRP"),Wtf.TAB_TITLE_LENGTH),
//                tabTip:WtfGlobal.getLocaleText("acc.accPref.autoRP"),  //'Receive Payments',
//                iconCls:'accountingbase receivepayment',
//                closable: true,
//                modeName:'autoreceipt'
//            });
//            panel.on("activate", function(){
//                panel.doLayout();
//                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
//            }, this);
//            Wtf.getCmp('as').add(panel);
//        }
//        //    panel.on('invoice',callInvoiceList);
//        Wtf.getCmp('as').setActiveTab(panel);
//        Wtf.getCmp('as').doLayout();
//    }
//    else
//        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.accPref.autoRP"));
//}
function callReceiptSalesNew(winValue){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.salesreceivepayment, Wtf.Perm.salesreceivepayment.createreceipt)) {
        var panel = Wtf.getCmp("receiptwindow");
        if(panel!=null){
            Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null;
        }
        if(panel==null){
                panel=new Wtf.account.ReceiptEntry({
                id : 'receiptwindow',
                paymentType: 3,      // Against GL
                border : false,
                isReceipt:true,
                moduleId:Wtf.Acc_Receive_Payment_ModuleId,
                cls: 'paymentFormPayMthd',
                layout: 'border',
                helpmodeid: 9, //This is help mode id
                title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoRP"),Wtf.TAB_TITLE_LENGTH),
                tabTip:WtfGlobal.getLocaleText("acc.accPref.autoRP"),
                iconCls:'accountingbase receivepayment',
                closable: true,
                isCustomer:false,
                modeName:'autoreceipt'
            });
            panel.on("activate", function(){
                panel.doLayout();
                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.accPref.autoRP"));
}

//function callPaymentSales(winValue){
//    if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createpayment)) {
//        var panel = Wtf.getCmp("payment");
//        if(panel!=null){
//            Wtf.getCmp('as').remove(panel);
//            panel.destroy();
//            panel=null;
//        }
//        if(panel==null){
//            panel = new Wtf.account.OSDetailPanel({
//                id : 'payment',
//                border : false,
//                isReceipt:false,
//                winValue:winValue,
//                isDirectCustomer:true,
//                moduleId:Wtf.Acc_Make_Payment_ModuleId,
//                cls: 'paymentFormPayMthd',
//                layout: 'border',
//                helpmodeid: 10, //This is help mode id
//                title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoMP"),Wtf.TAB_TITLE_LENGTH),
//                tabTip:WtfGlobal.getLocaleText("acc.accPref.autoMP"),  //'Make Payment',
//                iconCls:'accountingbase makepayment',
//                closable: true,
//                modeName:'autopayment'
//            });
//            panel.on("activate", function(){
//                panel.doLayout();
//                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
//            }, this);
//            Wtf.getCmp('as').add(panel);
//        }
//        Wtf.getCmp('as').setActiveTab(panel);
//        Wtf.getCmp('as').doLayout();
//    }
//    else
//        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.accPref.autoMP"));
//}

function callPaymentSalesNew(winValue){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.purchasemakepayment, Wtf.Perm.purchasemakepayment.createpayment)) {
        var panel = Wtf.getCmp("paymentwindow");
        if(panel!=null){
            Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null;
        }
        if(panel==null){
            panel=new Wtf.account.PaymentEntry({
                id : 'paymentwindow',
                paymentType: 3,                      // Against GL
                border : false,
                isReceipt:false,
                moduleId:Wtf.Acc_Make_Payment_ModuleId,
                cls: 'paymentFormPayMthd',
                layout: 'border',
                helpmodeid: 10, //This is help mode id
                title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoMP"),Wtf.TAB_TITLE_LENGTH),
                tabTip:WtfGlobal.getLocaleText("acc.accPref.autoMP"),  
                iconCls:'accountingbase receivepayment',
                closable: true,
                isCustomer:false,
                modeName:"autopayment"
            });
            panel.on("activate", function(){
                panel.doLayout();
                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.accPref.autoMP"));
}


   function callCreditNoteSales(isCN,value){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createcn)) {
        var winid=(winid==null?"cnwindow":winid);
        var panel = Wtf.getCmp(winid);
        //        var isCN = true;
        var isCustBill = false;
        if(!panel){
            new Wtf.account.CreditNoteWindow({
                title: isCN?WtfGlobal.getLocaleText("acc.cn.generate"):WtfGlobal.getLocaleText("acc.dn.generate"),  //"Receipt Type",
                id: winid,
                isCustBill:isCustBill,
                closable: false,
                isCN:isCN,
                moduleid:isCN?Wtf.Acc_Credit_Note_ModuleId:Wtf.Acc_Debit_Note_ModuleId,
                cntype:value,
                modal: true,
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                width: 500,
                autoScroll:true,
                height: 450,
                resizable: false,
                layout: 'border',
                buttonAlign: 'right',
                renderTo: document.body,
                modeName:isCN?'autocreditmemo':'autodebitnote'
            }).show();
        }
    }else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.accPref.autoCN"));
}

function openCNWindow(isCN,cntype){// CN Against CI.
    var winid="creditnotepanel";
    var panel = Wtf.getCmp(winid); 
    if(panel!=null){
        Wtf.getCmp('as').remove(panel);
        panel.destroy();
        panel=null; 
    }
    panel= new Wtf.account.NoteAgainsInvoice({
        title: WtfGlobal.getLocaleText("acc.cn.generate"), 
        id: winid,
        isCustBill:this.isCustBill,
        closable: true,
        isCN:isCN,
        moduleid:Wtf.Acc_Credit_Note_ModuleId,
        cntype:cntype,
        iconCls :'accountingbase creditnote',
        autoScroll:true,
        resizable: false,
        layout: 'border',
        modeName:'autocreditmemo'
    });
    Wtf.getCmp('as').add(panel);
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
    
function openDNWindow(isCN,cntype){// CN Against CI.
    var winid="debitnotepanel";
    var panel = Wtf.getCmp(winid);
    if(panel!=null){
        Wtf.getCmp('as').remove(panel);
        panel.destroy();
        panel=null; 
    }
    panel=new Wtf.account.NoteAgainsInvoice({
        title: WtfGlobal.getLocaleText("acc.dn.generate"), 
        id: winid,
        isCustBill:this.isCustBill,
        closable: true,
        isCN:isCN,
        moduleid:Wtf.Acc_Debit_Note_ModuleId,
        cntype:cntype,
        iconCls :'accountingbase debitnote',
        autoScroll:true,
        resizable: false,
        layout: 'border',
        modeName:'autodebitnote'
    });
    Wtf.getCmp('as').add(panel);
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

 function openCNTabSales (isCN,cntype){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createcn)) {
            var panel = Wtf.getCmp("CreditNote");
            if(panel!=null){
                 Wtf.getCmp('as').remove(panel);
                panel.destroy();
                panel=null;
            }
                if(panel==null){
                    panel = new Wtf.account.TrNotePanel({
                        id : 'CreditNote',
                        border : false,
                        layout: 'fit',
                        helpmodeid:13,
                        moduleid:12,
                        isCN:isCN,
                        isCustBill:false,
                        cntype:cntype,
                        title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoCN"),Wtf.TAB_TITLE_LENGTH) ,
                        tabTip:WtfGlobal.getLocaleText("acc.accPref.autoCN"),  //'Credit Note',
                        closable: true,
                        iconCls:'accountingbase creditnote',
                        modeName:'autocreditmemo'
                    });
                    panel.on("activate", function(){
                        panel.doLayout();
                    }, this);
                    Wtf.getCmp('as').add(panel);
                }
                Wtf.getCmp('as').setActiveTab(panel);
                Wtf.getCmp('as').doLayout();
        } else {
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.accPref.autoCN"));
        }
    }
    
     function openDNTabSales(cntype){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createdn)) {
            var panel = Wtf.getCmp("DebitNote");
            if(panel!=null){
                 Wtf.getCmp('as').remove(panel);
                panel.destroy();
                panel=null;
            }
                if(panel==null){
                    panel = new Wtf.account.TrNotePanel({
                        id : 'DebitNote',
                        border : false,
                        layout: 'fit',
                        isCN:false,
                        cntype:cntype,
                        moduleid:Wtf.Acc_Debit_Note_ModuleId,
                        isCustBill:false,
                        title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoDN"),Wtf.TAB_TITLE_LENGTH),
                        tabTip:WtfGlobal.getLocaleText("acc.accPref.autoDN"),  //'Debit Note',
                         helpmodeid:14,
                        iconCls:'accountingbase debitnote',
                        closable: true,
                        modeName:'autodebitnote'
                    });
                    panel.on("activate", function(){
                        panel.doLayout();
                    }, this);
                    Wtf.getCmp('as').add(panel);
                }
                Wtf.getCmp('as').setActiveTab(panel);
                Wtf.getCmp('as').doLayout();
        } else {
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.accPref.autoDN"));
        }
    }


function callNewInvoiceList(id,check,isCash,consolidateFlag,searchStr, filterAppend,titlelabel){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewinvoice)) {
        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
        var panelID = "InvoiceListNavigation";
        panelID = consolidateFlag?panelID+'Merged':panelID+'Entry';    
        var panel = Wtf.getCmp(panelID);
        if(panel==null){
            panel = getInvoiceTab(false, panelID, (titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.invoiceList.tabtitle"), undefined, isCash,consolidateFlag,undefined,searchStr, filterAppend,Wtf.Acc_Invoice_ModuleId);
            panel.moduleId=Wtf.Acc_Invoice_ModuleId;
            panel.closable= true;
            Wtf.getCmp('as').add(panelID);
            panel.on('journalentry',callJournalEntryDetails);
            panel.expandInvoice(id,check);            
        }else{
            panel.expandInvoice(id,check);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel,searchStr, filterAppend);
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.invoiceList.tabtitle"));
}
function callRecInvoiceList(consolidateFlag,titlelabel){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewinvoice)) {
        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
        var repeatInvoicePanelID = "FromNavigationRepeateInvoiceList";//chnaging Panel ID for Recuring Invoice Report which opens from navigation panel List . as it also Opens from Invoice Report in Sub tab.
        repeatInvoicePanelID = consolidateFlag?repeatInvoicePanelID+'Merged':repeatInvoicePanelID; 
        var panel = Wtf.getCmp(repeatInvoicePanelID);
        if(panel==null){
            panel = new Wtf.RepeatedInvoicesReport({
                id : repeatInvoicePanelID,
                consolidateFlag:consolidateFlag,
                title:(titlelabel!=undefined)?titlelabel:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.invoiceList.recInvReport"),Wtf.TAB_TITLE_LENGTH),
                tabTip:(titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.invoiceList.recInvReport"),  //'Recurring Invoices Report',
                border: false,
                closable: true,
                layout: 'fit',
                iconCls:'accountingbase invoicelist',
                isCustBill:false,
                isCustomer: true,
                moduleid:Wtf.Acc_Invoice_ModuleId                               //Passing module id of sales invoice report as from navigation tree panel the edit and delete button where not displaying ERM-384
            })    
            Wtf.getCmp('as').add(repeatInvoicePanelID);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.invoiceList.tabtitle"));
}

//function SalesReceiptReport(consolidateFlag,searchStr, filterAppend,type,winValue,panelID,titlelabel){
//    if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewreceipt)) {
//        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
////        var panelID = "receiptReport";
//        panelID = consolidateFlag?panelID+'Merged':panelID;
//        var panel = Wtf.getCmp(panelID);
//        if(panel==null){
//            panel = new Wtf.account.ReceiptReport({
//                id : panelID,
//                border : false,
//                helpmodeid: 20,
//                winValue:winValue,
//                recordType:type,
//                layout: 'fit',
//                searchJson: searchStr,
//                filterConjuctionCrit:filterAppend,
//                moduleid:Wtf.Acc_Receive_Payment_ModuleId,
//                consolidateFlag:consolidateFlag,
//                title:Wtf.util.Format.ellipsis((titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.prList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
//                tabTip:(titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.prList.tabTitle"),  //'Payment Received Report',
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
//        showAdvanceSearch(panel,searchStr, filterAppend); 
//    }
//    else
//        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.prList.tabTitle"));
//}

function callSalesDebitNoteDetails(consolidateFlag,searchStr, filterAppend,type,isCustomer,panelID,titlelabel,isForAgainstInvoice,inputType){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.debitnote, Wtf.Perm.debitnote.viewdn)) {
        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
//        var panelID = "DebitNoteDetails";
        panelID = consolidateFlag?panelID+'Merged':panelID;
        var panel = Wtf.getCmp(panelID);
        if(panel==null){
             panel = getSalesDNTab(false, panelID,(titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.dnList.tabTitle"), undefined, false,"", "",Wtf.Acc_Debit_Note_ModuleId,isCustomer,type,isForAgainstInvoice,inputType);
            Wtf.getCmp('as').add(panel);
            //panel.on('goodsreceipt', callGoodsReceiptDetails);
            panel.on('journalentry',callJournalEntryDetails);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel,searchStr, filterAppend);
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.dnList.tabTitle"));
}

function getSalesDNTab(isWithOutInventory, tabId, tabTitle, extraFilters, consolidateFlag,searchStr, filterAppend,moduleid,isCustomer,type,isForAgainstInvoice,inputType){
    var reportPanel = new Wtf.account.NoteDetailsPanel({
        id : tabId,
        border : false,
        searchJson: searchStr,
        filterConjuctionCrit:filterAppend,
        moduleId:moduleid,
        isCustRecord:isCustomer,
        cntypeNo:type,
        cmbNo:4,
        inputType:inputType,
        isCNReport:false,
        isCustBill: isWithOutInventory,
        consolidateFlag:consolidateFlag,
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle,
        isForAgainstInvoice:isForAgainstInvoice,  //flag for credit note and debit note for invoice
        extraFilters: extraFilters,
        helpmodeid:22,
        layout: 'fit',
        closable: true,
        iconCls:'accountingbase debitnotereport'
    });
    return reportPanel;
}

function callSalesCreditNoteDetails(consolidateFlag,searchStr, filterAppend,type,isCustomer,panelID,titlelabel,isForAgainstInvoice,inputType){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.creditnote, Wtf.Perm.creditnote.viewcn)) {
        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
//        var panelID = "CreditNoteDetails";
        panelID = consolidateFlag?panelID+'Merged':panelID;
        var panel = Wtf.getCmp(panelID);
        if(panel==null){
            panel = getSalesCNTab(false, panelID,(titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.cnList.tabTitle"), undefined, false,"", "",Wtf.Acc_Credit_Note_ModuleId,isCustomer,type,isForAgainstInvoice,inputType);
            Wtf.getCmp('as').add(panel);
            panel.on('journalentry',callJournalEntryDetails);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel,searchStr, filterAppend);
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.cnList.tabTitle"));
}


function getSalesCNTab(isWithOutInventory, tabId, tabTitle, extraFilters, consolidateFlag,searchStr, filterAppend,moduleid,isCustomer,type,isForAgainstInvoice,inputType){
    var reportPanel = new Wtf.account.NoteDetailsPanel({
        id : tabId,
        border : false,
        layout: 'fit',
        helpmodeid:19,
        searchJson: searchStr,
        filterConjuctionCrit:filterAppend,
        moduleId:moduleid,
        isCNReport:true,
        isCustRecord:isCustomer,
        cntypeNo:type,
        isForAgainstInvoice:isForAgainstInvoice,
        consolidateFlag:consolidateFlag,
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle,
        isCustBill: isWithOutInventory,
        extraFilters: extraFilters,
        closable: true,
        inputType: inputType,
        cmbNo: 4,
        iconCls:'accountingbase creditnotereport'
    });
    return reportPanel;
}

function callSalesJournalEntryDetails(jid,check,consolidateFlag,JETYpe,winType,titlelabel){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.viewje)){
        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
        var panelID = "JournalEntryDetails"+(jid!=undefined?jid:"");
        panelID = consolidateFlag?panelID+'Merged':panelID;
        var panel = Wtf.getCmp(panelID);
        if(panel==null){
            panel = getSalesJETab(panelID,(titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.jeList.tabTitle"), jid, "", consolidateFlag,JETYpe,winType);
            Wtf.getCmp('as').add(panel);
//            panel.expandJournalEntry(jid,check);
//        }else{
//            panel.expandJournalEntry(jid,check);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.jeList.tabTitle"));
}

function getSalesJETab(tabId, tabTitle, jeId, extraFilters, consolidateFlag,JETYpe,winType){
    var reportPanel = new Wtf.account.JournalEntryDetailsPanel({
        id : tabId,
        consolidateFlag:consolidateFlag,
        border : false,
        JETYpe:JETYpe,
        winType:winType,
        helpmodeid: 24,
        layout: 'fit',
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle,
        label : tabTitle,
        extraFilters: extraFilters,
        entryID:jeId,
        closable: true,
        iconCls:'accountingbase journalentryreport'
    });
    //    reportPanel.expandJournalEntry();
    return reportPanel;
}

//function callSalesPaymentReport(consolidateFlag,searchStr, filterAppend,type,winValue,panelID,titlelabel){
//    if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewpayment)) {
//        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
////        var panelID = "paymentReport";
//        panelID = consolidateFlag?panelID+'Merged':panelID;
//        var panel = Wtf.getCmp(panelID);
//        if(panel==null){
//            panel = new Wtf.account.ReceiptReport({
//                id : panelID,
//                border : false,
//                helpmodeid: 23,
//                winValue:winValue,
//                recordType:type,
//                searchJson: searchStr,
//                filterConjuctionCrit:filterAppend,
//                moduleid:Wtf.Acc_Make_Payment_ModuleId,
//                layout: 'fit',
//                consolidateFlag:consolidateFlag,
//                title:Wtf.util.Format.ellipsis((titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.pmList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
//                tabTip:(titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.pmList.tabTitle"),  //'Payment Made Report',
//                closable: true,
//                isReceipt:false,
//                iconCls:'accountingbase makepaymentreport'
//            });
//            Wtf.getCmp('as').add(panel);
//            panel.on('journalentry',callJournalEntryDetails);
//        }
//        Wtf.getCmp('as').setActiveTab(panel);
//        Wtf.getCmp('as').doLayout();
//        showAdvanceSearch(panel,searchStr, filterAppend);
//    }
//    else
//        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.pmList.tabTitle"));
//}

//function openPaymentTypeWin(winValue,isReceipt){ 
//    if(winValue==3 || winValue==6){
//        winValue=(winValue==3)?1:winValue;
//        if(isReceipt)
//            callReceiptSales(winValue);
//        else
//            callPaymentSales(winValue);
//    }else{
//    new Wtf.account.ReceiptTypeWindow({
//        modal: true,
//        iconCls :getButtonIconCls(Wtf.etype.deskera),
//        width: 380,
//        winValue:winValue,
//        isReceipt:isReceipt,
//        autoScroll:true,
//        height: 230,
//        resizable: false,
//        layout: 'border',
//        buttonAlign: 'right',
//        renderTo: document.body
//    }).show();
//    }
//}
//this method get called when CN/DN Report open from side panel 
function OpenCNDNTab(value,isCN,isCustBill){  
    this.value = value;
    this.isCN = isCN;
    this.isCustBill = isCustBill;
    if(this.value == 1 || this.value == 3) {
        if(this.isCN) {
            openCNWindow(isCN,this.value);
        } else {
            openDNWindow(isCN,this.value);
        }
    } else {// CN/DN otherwise
        var winid=this.isCN?"creditnotepanel":"debitnotepanel";
        var panel = Wtf.getCmp(winid);
        if(panel!=null){
            Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null; 
        }
        panel=new Wtf.account.NoteAgainsInvoice({
            title: this.isCN?WtfGlobal.getLocaleText("acc.cn.generate"):WtfGlobal.getLocaleText("acc.dn.generate"),  //"Receipt Type",
            id: winid,
            isCustBill:this.isCustBill,
            closable: true,
            isCN:this.isCN,
            moduleid:this.isCN?Wtf.Acc_Credit_Note_ModuleId:Wtf.Acc_Debit_Note_ModuleId,
            cntype:this.value,
            iconCls :this.isCN?'accountingbase creditnote':'accountingbase debitnote',
            autoScroll:true,
            layout: 'border',
            modeName:this.isCN?'autocreditmemo':'autodebitnote'
        });
        Wtf.getCmp('as').add(panel);
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
} 
    function OpenCNDNTabForInvoice(value,isCN,isCustBill,isCustomer,isCndnAgainstInvoice){  
        this.value = value;
        this.isCN = isCN;
        this.isCustBill = isCustBill;
        this.isCndnAgainstInvoice=this.isCndnAgainstInvoice;
            var winid=(winid==null?((isCustomer)?"cnwindowforvendor":"dnwindoforcustomer"):winid);
            var panel = Wtf.getCmp(winid);            
//            if(!panel){
                 if(panel==null){
                panel = new Wtf.account.CreditNoteDebitNotePanel({
                    id : winid,
//                    isEdit: isEdit,
//                    record: rec,
                    isCustomer:isCustomer,
                    isNoteAlso:false,
                    isCustBill:false,
                    isCN:isCN,
                    isCndnAgainstInvoice:isCndnAgainstInvoice,
                    label:isCN?WtfGlobal.getLocaleText("acc.accPref.creditNoteAgainst.invoice"):WtfGlobal.getLocaleText("acc.accPref.debititNoteAgainst.invoice"),
                    border : false,
                    heplmodeid: 11,
                    moduleid:Wtf.Acc_Sales_Return_ModuleId,
                    //            layout: 'border',
                    title:isCN?WtfGlobal.getLocaleText("acc.accPref.creditNoteAgainst.invoice"):WtfGlobal.getLocaleText("acc.accPref.debititNoteAgainst.invoice"),
                    tabTip:isCN?WtfGlobal.getLocaleText("acc.accPref.creditNoteAgainst.invoice"):WtfGlobal.getLocaleText("acc.accPref.debititNoteAgainst.invoice"),
                    closable: true,
                    iconCls:'accountingbase deliveryorder',
                   modeName:isCN?'autocreditmemo':'autodebitnote'
                });
                panel.on("activate", function(){
                    panel.doLayout();
                }, this);
                Wtf.getCmp('as').add(panel);
            }
             Wtf.getCmp('as').setActiveTab(panel);
            Wtf.getCmp('as').doLayout();

//        }
    }
    
//Wtf.account.ReceiptTypeWindow = function(config){
//    this.value="1",
//    this.butnArr = [];
//    this.butnArr.push({
//        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
//        scope: this,
//        handler: this.saveForm
//    },{
//        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
//        scope: this,
//        handler: function() {
//            this.close();
//        }
//    });
//
//    Wtf.apply(this,{
//        buttons: this.butnArr
//    },config);
//    Wtf.account.ReceiptTypeWindow.superclass.constructor.call(this, config);
//    this.addEvents({
//        'update':true
//    });
//}
//    
//Wtf.extend(Wtf.account.ReceiptTypeWindow, Wtf.Window, {
//
//    onRender: function(config){
//        Wtf.account.ReceiptTypeWindow.superclass.onRender.call(this, config);
//        this.createForm();
//        var title=this.isReceipt?WtfGlobal.getLocaleText("acc.mp.payType"):WtfGlobal.getLocaleText("acc.mp.rtype");
//        var msg=this.isReceipt?WtfGlobal.getLocaleText("acc.field.SelectPaymentType"):WtfGlobal.getLocaleText("acc.field.SelectReceiptType");
//        var isgrid=true;
//        this.add({
//            region: 'north',
//            height: 75,
//            border: false,
//            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
//            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
//        },{
//            region: 'center',
//            border: false,
//            baseCls:'bckgroundcolor',
//            layout: 'fit',
//            items:this.TypeForm
//        });
//    },
//
//    createForm:function(){
//        var fieldArray=[];
//       
//        this.accountType= new Wtf.form.Checkbox({
//            boxLabel:" ",
//            width: 50,
//            inputType:'radio',
//            inputValue:'1',
//            name:'rectype',
//            checked:true,
//            fieldLabel:this.isReceipt?WtfGlobal.getLocaleText("acc.rp.recPay"):WtfGlobal.getLocaleText("acc.mp.msg1")
//        })
//       
//        this.makeReceivePayAgainstNoteType = new Wtf.form.Checkbox({
//            boxLabel: " ",
//            inputType: 'radio',
//            name: 'rectype',
//            hidden:Wtf.account.companyAccountPref.withoutinventory,
//            hideLabel:Wtf.account.companyAccountPref.withoutinventory,
//            inputValue: '7',
//            width: 50,
//            fieldLabel: this.isReceipt? WtfGlobal.getLocaleText("acc.rp.recPayAgainstDebitNote") : WtfGlobal.getLocaleText("acc.field.MakepaymentagainstCreditNote")
//        });
//        
//           this.makeReceivePayType= new Wtf.form.Checkbox({
//            boxLabel: " ",
//            inputType: 'radio',
//            name: 'rectype',
//            inputValue: '6',
//            width: 50,
//            checked:true,
//            fieldLabel: this.isReceipt? WtfGlobal.getLocaleText("acc.field.ReceivepaymentfromVendor") : WtfGlobal.getLocaleText("acc.field.Makepaymenttocustomer")
//        })
//        
//       
//        
//        if(this.winValue==10 || this.winValue==13)
//        {
//            fieldArray.push(this.accountType)
//            fieldArray.push(this.makeReceivePayAgainstNoteType)
//        }else if(this.winValue==11 || this.winValue==12)
//        {
//            fieldArray.push(this.makeReceivePayType)
//            fieldArray.push(this.makeReceivePayAgainstNoteType)
//        }
//        this.TypeForm=new Wtf.form.FormPanel({
//            region:'center',
//            autoScroll:true,
//            border:false,
//            labelWidth:245,
//            bodyStyle: "background: transparent;",
//            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
//            defaultType: 'textfield',
//            items:fieldArray
//        });
// 
//    },
//
//    saveForm:function(){  
//        var rec=this.TypeForm.getForm().getValues();
//        this.value = rec.rectype;
//        if(this.isReceipt){
//            callReceiptSales(this.value);
//        }else{
//            callPaymentSales(this.value);
//        }
//        this.close();
//    }
//});
//


function NewGSTForm5DetailedView(){
    var GSTForm5DetailedViewPanel = Wtf.getCmp('NewGSTForm5DetailedViewID');
    if(GSTForm5DetailedViewPanel==null){
        var detailedView = new Wtf.account.GSTForm5DetailedView({
            id:'NewGSTForm5DetailedViewID',
            border: false,
            layout: 'fit',
            iconCls: 'accountingbase agedpayable',
            closable:true,
            title:Wtf.util.Format.ellipsis((Wtf.account.companyAccountPref.countryid=='137')?WtfGlobal.getLocaleText("acc.field.GSTTapReturnDetailedView"):WtfGlobal.getLocaleText("acc.field.GSTForm5DetailedView"),Wtf.TAB_TITLE_LENGTH) ,
            tabTip:(Wtf.account.companyAccountPref.countryid=='137')?WtfGlobal.getLocaleText("acc.field.GSTTapReturnDetailedView"):WtfGlobal.getLocaleText("acc.field.GSTForm5DetailedView")
        });
          Wtf.getCmp('as').add(detailedView);
          Wtf.getCmp('as').setActiveTab(detailedView);
          detailedView.on('journalentry',callJournalEntryDetails);
    }else{
        Wtf.getCmp('as').setActiveTab(GSTForm5DetailedViewPanel);
    }
    Wtf.getCmp('as').doLayout();
}
function GSTForm5Tab(){
    var GSTForm5Tab = Wtf.getCmp('GSTForm5Tab');
    if(GSTForm5Tab==null){
        var GSTForm5Report = new Wtf.account.GSTForm5HierarchyTab({
            id: "GSTForm5Tab",
            border: false,
            closable:true,
            //             isSales:isSales,
//            layout: 'fit',
            iconCls: 'accountingbase agedpayable',
            title:Wtf.util.Format.ellipsis('GST Form 5',Wtf.TAB_TITLE_LENGTH) ,
            tabTip:WtfGlobal.getLocaleText("acc.field.GSTForm5Report") //'You can view your Purchase and Sales Tax reports here.',
        });
        Wtf.getCmp('as').add(GSTForm5Report);
        Wtf.getCmp('as').setActiveTab(GSTForm5Report);
    }else{
        Wtf.getCmp('as').setActiveTab(GSTForm5Tab);
    }
    Wtf.getCmp('as').doLayout();
}

function exciseFormER_5(){
    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportExciseFormERJasper.do?report=FORM_ER_5&filetype=pdf";
}

function VATForm16(){
    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportDVAT16FormJasper.do?report=DVATFORM16&filetype=pdf";
}

function AnnexureII57F4(type){
    if(type=="1"){
        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportAnnexureII57F4.do?report=AnnexureII57F4I&filetype=pdf";
    }else if(type=="2"){
        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportAnnexureII57F4.do?report=AnnexureII57F4II&filetype=pdf";
    }else if(type=="3"){
        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportAnnexureII57F4.do?report=AnnexureII57F4III&filetype=pdf";
    }
}

function vatRepoAnnexure_2A(is2A){
    this.freqStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'frequencyid'
        },{
            name:'frequencyname'
        }],
        data:[['1','Quaterly'], ['2','Monthly']]
    });
        
    this.periodStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'periodid'
        },{
            name:'periodname'
        }],
        data:[['quater1','1st April '+new Date().getFullYear()+' to 30th June '+new Date().getFullYear()+' (Quarter I)'], 
            ['quater2','1st July '+new Date().getFullYear()+' to 30th September '+new Date().getFullYear()+' (Quarter II)'], 
            ['quater3','1st October '+new Date().getFullYear()+' to 31st December '+new Date().getFullYear()+' (Quarter III)'],
            ['quater4','1st January '+(new Date().getFullYear()+1)+' to 31st Mar '+(new Date().getFullYear()+1)+' (Quarter IV)']]
    });
    
    var yearArr=new Array()
    for(var i=2000;i<2051;i++){
        yearArr.push(i, i)
    }
    this.yearStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'yearid'
        },{
            name:'year'
        }],
        data:[['2000','2000']]
    });

    Wtf.yearRec = new Array();
    for(var i=0; i<50; i++){
        Wtf.yearRec[i] = new Array((i+2001), (i+2001)+" - "+(i+2002));
    }

    Wtf.yearStore = new Wtf.data.SimpleStore({
        id    : 'monStore',
        fields: ['id','name'],
        data: Wtf.yearRec
    });
    
    this.winForAnnexture = new Wtf.Window({
        height:270,
        width:370,
        modal:true,
        title: is2A?WtfGlobal.getLocaleText("acc.report.annexure2A.title"):WtfGlobal.getLocaleText("acc.report.annexure2B.title"),//Annexure 2A" ,     
        border: false,
        items :[
        {
            region: 'north',
            height:70,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: is2A ? getTopHtml(WtfGlobal.getLocaleText('acc.report.annexure2A.ExportAnnexure2A'),WtfGlobal.getLocaleText("acc.report.annexure2A.Selectayearfrequencyandperiod"),"../../images/accounting_image/price-list.gif",'Frequncey'):getTopHtml(WtfGlobal.getLocaleText('acc.report.annexure2A.ExportAnnexure2B'),WtfGlobal.getLocaleText("acc.report.annexure2A.Selectayearfrequencyandperiod"),"../../images/accounting_image/price-list.gif",'Frequncey')
        },{
            region: 'center',
            layout:'form',
            height:100,
            bodyStyle : 'padding:20px 10px 10px 10px',
            labelWidth:95,
            border: false,
            items:[
            this.yearCombo=new Wtf.form.ComboBox({
                fieldLabel:'Financial Year',//WtfGlobal.getLocaleText('acc.report.annexure2A.Year'),
                hiddenName: 'year',
                name: 'year',
                store: Wtf.yearStore,
                valueField: 'id',
                displayField: 'name',
                value:new Date().getFullYear(),
                mode: 'local',
                typeAhead: true,
                allowBlank:false,
                triggerAction: 'all'
            }), 
            this.freqCombo=new Wtf.form.ComboBox({
                fieldLabel:WtfGlobal.getLocaleText('acc.report.annexure2A.Frequency'),
                hiddenName: 'frequency',
                name: 'frequency',
                store: this.freqStore,
                valueField: 'frequencyid',
                displayField: 'frequencyname',
                value:'1',
                mode: 'local',
                typeAhead: true,
                allowBlank:false,
                triggerAction: 'all'
            }), 
            this.periodCombo=new Wtf.form.ComboBox({
                fieldLabel:WtfGlobal.getLocaleText('acc.report.annexure2A.Period'),
                hiddenName: 'frequency',
                name: 'frequency',
                store: this.periodStore,
                valueField: 'periodid',
                displayField: 'periodname',
                mode: 'local',
                typeAhead: true,
                triggerAction: 'all',
                allowBlank:false,
                listWidth:300,
                emptyText:WtfGlobal.getLocaleText('acc.report.annexure2A.Selectaperiod')
            }) 

            ]              
        }],
        buttonAlign: 'center',
        buttons:[{
            text: 'Export in xls',
            scope: this,
            handler: function(combo, record, index){
                if(!Wtf.isEmpty(this.freqCombo.getValue()) && !Wtf.isEmpty(this.periodCombo.getValue()) && !Wtf.isEmpty(this.yearCombo.getValue())){
                    var frequency=this.freqCombo.getValue();
                    var period=this.periodCombo.getValue();
                    var year=this.yearCombo.getValue();
                    var filename= is2A?'Annexure 2A':'Annexure 2B';
                    var filetype='xls';
                    var get=Wtf.autoNum.annexure2AReport;
                    var align='none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none';
                    var width='500,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200';
                    
                    var header2A='srno,yearmmqq,tin,nameofsupplier,impfrmoutind,'
                    +'highseaspur,receivbckf,amountunregisterdealer,otherdealerreceivedf,cgpurc,'
                    +'cpurc,ineterstatepurh,ineterstatepurwithout,inwardstockbranch,inwardstockconsignment,'
                    +'lpurcgramt,lpurcgpuramt,lpurcgiptaxpaid,lpurcgtotalpurintax,typeofpur,'
                    +'vatper,amount,vatamount,amountwithtax,vatdper,prevquater';
                
                    var header2B='srno,yearmmqq,tin,nameofbuyer,interstatebranch,'
                    +'interstateconsignment,expoutind,highseasales,issgoodtrantype,chie1e2jnone,'
                    +'ratoftax,salespricexcst,cst,total,lsaletypeofsale,'
                    +'lratetax,salespricexvat,optax,totalinvat,rattaxdvat,'
                    +'saleomclvl,chargesinwork,chargesinservicework,saleshdelhi,chargeother';
                
                    var title2A='C01,Year and (Month or Quarter) (YYYYMM or YYYYQQ)(C02),'
                    +'Seller’s TIN (C03),Seller’s Name (C04),Import from Outside India (C05),'
                    +'High Seas Purchase (C06),Own Goods Received Back after job work against F-Form(C07),'
                    +'Purchase from Unregistered Dealer/Composition Dealer/Non Creditable Goods/Against Retail Invoices/Tax free Goods/Labour and Services related to work contract/Tax invoices not eligible for ITC/Delhi dealers against Form-H/Capital Goods(Used for manufacturing of non-creditable goods) (C08),'
                    +'Other Dealer Received for job work against F-form (C09),Capital Goods Purchase Against C-Form (C10),'
                    +'Goods (Other than capital goods) purchased against C-Forms (C11),Inter state Purchase against H-Form (other than Delhi dealers)(C12),'
                    +'Inter state Purchase without Forms (C13),Inward Stock Transfer (Branch) against F-Form(C14),'
                    +'Inward Stock Transfer (Consignment) against F-Form(C15),Local Purchase eligible-Capital Goods-Rate of Tax (C16),'
                    +'Local Purchase eligible-Capital Goods-Purchase Amount (C17),Local Purchase eligible-Capital Goods-Input Tax Paid (C18),'
                    +'Local Purchase eligible-Capital Goods-Total Purchase including Tax (C19),Type of Purchase (C20),'
                    +'Local Purchase eligible-Others-Rate of Tax (C21),Local Purchase eligible-Others-Purchase Amount (C22),'
                    +'Local Purchase eligible-Others-Input Tax Paid (23),Local Purchase eligible-Others-Total Purchase including Tax (C24),'
                    +'Rate of Tax on the Item under Delhi Value Added Tax Act 2004(C25),'
                    +'Does this purchase pertain to dispatches made by seller in previous Quarter? (Yes/No)(C26)';
                
                    var title2B='C01,Year and (Month or Quarter) (YYYYMM or YYYYQQ)(C02),'
                    +'Buyer’s TIN (C03),Buyer’s Name (C04),Inter-State Branch Transfer  against F-form (C05),'
                    +'Inter-State Consignment Transfer against F-form (C06),Export Out side of India (C07),'
                    +'High Sea Sal (C08),'
                    +'ISS-Goods Type/ISS-Transaction Type (C09),ISS-Form Type (C10),ISS-Rate of Tax (C11),'
                    +'ISS-Sales Price (Excluding CST) (C12),'
                    +'ISS-Central Sales Tax (C13),ISS-Total (C14),Local Sale-Type of Sale (C15),Local Sale-Rate of Tax (C16),'
                    +'Local Sale-Sales Price (Excluding VAT) (C17),Local Sale-Output Tax (C18),Local Sale-Total (including VAT) (C19),'
                    +'Rate of Tax on the Item under Delhi Value Added Tax Act 2004 (C20),Sale of Petrol/Diesel Suffered Tax on Full Sale Price at OMC level(C21),'
                    +'Charges towards labour services and other like charges in civil works contracts(C22),Charges towards cost of land if any in services civil works contracts(C23),'
                    +'Sales Against H-form to Delhi dealer (C24),Charges towards cost of land if any other than services civil works contracts(C25)'
                
                    var title=is2A ? title2A:title2B;
                    var header=is2A ? header2A:header2B;
                    
                    var parameter='&filename='+filename+'&filetype='+filetype+'&header='+header+'&title='+title
                        +'&align='+align+'&width='+width+'&frequency='+frequency+'&period='+period+'&get='+get+'&year='+year+'&is2A='+is2A;
                    Wtf.get('downloadframe').dom.src = "ACCCombineReports/exportDVATAnnexure2AReport.do?"+parameter;
                    this.winForAnnexture.close();
                }else{
                    this.periodCombo. markInvalid('This field is mandatory');
                }
            }
        },{
            text:'Cancel',
            handler: function(){
                this.winForAnnexture.close();
            },
            scope: this
        }]
    });
        
    this.freqCombo.on('select',
        function(combo,record,index){ 
            this.periodCombo.reset();
            var data=new Array();
            if(combo.getValue()=='1'){ 
                var year=this.yearCombo.getValue();
                data.push(['quater1','1st April '+year+' to 30th June '+year+' (Quarter I)'], 
                    ['quater2','1st July '+year+' to 30th September '+year+' (Quarter II)'], 
                    ['quater3','1st October '+year+' to 31st December '+year+' (Quarter III)'],
                    ['quater4','1st January '+(year+1)+' to 31st Mar '+(year+1)+' (Quarter IV)']);
            }else{
                data.push(['0','January'], ['1','February'], ['2','March'], ['3','April'], ['4','May'], ['5','June'], ['6','July'], ['7','August'], ['8','September'], ['9','October'], ['10','November'], ['11','December']);                
            }
            this.periodStore.loadData(data);                
        },this);
        
    this.yearCombo.on('select',
        function(combo,record,index){ 
            if(this.freqCombo.getValue()=='1'){ 
                this.periodCombo.reset();
                var data=new Array();
                var year=this.yearCombo.getValue();
                data.push(['quater1','1st April '+year+' to 30th June '+year+' (Quarter I)'], 
                    ['quater2','1st July '+year+' to 30th September '+year+' (Quarter II)'], 
                    ['quater3','1st October '+year+' to 31st December '+year+' (Quarter III)'],
                    ['quater4','1st January '+(year+1)+' to 31st Mar '+(year+1)+' (Quarter IV)']);
            }
            this.periodStore.loadData(data);                
        },this);    
    this.winForAnnexture.show();
}

function getVATRepoForm201(type201){
    var form201ABC="";
    var title="";
    if(type201=="A"){
        form201ABC=WtfGlobal.getLocaleText("acc.common.export")+" "+WtfGlobal.getLocaleText("acc.field.form201A");
        title = "Form 201"+type201;
    }else if(type201=="B"){
        form201ABC=WtfGlobal.getLocaleText("acc.common.export")+" "+WtfGlobal.getLocaleText("acc.field.form201B");
        title = "Form 201"+type201;
    }else if(type201=="C"){
        form201ABC=WtfGlobal.getLocaleText("acc.common.export")+" "+WtfGlobal.getLocaleText("acc.field.form201C");
        title = "Form 201"+type201;
    }else if(type201=="D"){
        form201ABC=WtfGlobal.getLocaleText("acc.common.export")+" "+WtfGlobal.getLocaleText("acc.field.form201A");
        title = "Form 201A";
    }else if(type201=="E"){
        form201ABC=WtfGlobal.getLocaleText("acc.common.export")+" "+WtfGlobal.getLocaleText("acc.field.form201B");
        title = "Form 201B";
    }else if(type201=="F"){
        form201ABC=WtfGlobal.getLocaleText("acc.common.export")+" "+WtfGlobal.getLocaleText("acc.field.form201C");
        title = "Form 201C";
    }
    
    this.freqStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'frequencyid'
        },{
            name:'frequencyname'
        }],
        data:[['0','Annually'],['1','Quaterly'], ['2','Monthly']]
    });
        
    this.periodStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'periodid'
        },{
            name:'periodname'
        }],
        data:[['quater1','1st April '+new Date().getFullYear()+' to 30th June '+new Date().getFullYear()+' (Quarter I)'], 
            ['quater2','1st July '+new Date().getFullYear()+' to 30th September '+new Date().getFullYear()+' (Quarter II)'], 
            ['quater3','1st October '+new Date().getFullYear()+' to 31st December '+new Date().getFullYear()+' (Quarter III)'],
            ['quater4','1st January '+(new Date().getFullYear()+1)+' to 31st Mar '+(new Date().getFullYear()+1)+' (Quarter IV)']]
    });
    
    var yearArr=new Array()
    for(var i=2000;i<2051;i++){
        yearArr.push(i, i)
    }
    this.yearStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'yearid'
        },{
            name:'year'
        }],
        data:[['2000','2000']]
    });

    Wtf.yearRec = new Array();
    for(var i=0; i<50; i++){
        Wtf.yearRec[i] = new Array((i+2001), (i+2001)+" - "+(i+2002));
    }

    Wtf.yearStore = new Wtf.data.SimpleStore({
        id    : 'monStore',
        fields: ['id','name'],
        data: Wtf.yearRec
    });
    
    this.form201ABCWindow = new Wtf.Window({
        height:240,
        width:370,
        modal:true,
        title: title,     
        border: false,
        items :[
        {
            region: 'north',
            height:70,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(form201ABC,"Select From Date and To Date","../../images/accounting_image/price-list.gif",'Frequncey')
        },{
            region: 'center',
            layout:'form',
            height:100,
            bodyStyle : 'padding:20px 10px 10px 10px',
            labelWidth:95,
            border: false,
            items:[
                this.yearCombo=new Wtf.form.ComboBox({
                    fieldLabel:'Financial Year',//WtfGlobal.getLocaleText('acc.report.annexure2A.Year'),
                    hiddenName: 'year',
                    name: 'year',
                    store: Wtf.yearStore,
                    valueField: 'id',
                    displayField: 'name',
                    value:new Date().getFullYear(),
                    mode: 'local',
                    typeAhead: true,
                    allowBlank:false,
                    triggerAction: 'all'
                }), 
                this.freqCombo=new Wtf.form.ComboBox({
                    fieldLabel:WtfGlobal.getLocaleText('acc.report.annexure2A.Frequency'),
                    hiddenName: 'frequency',
                    name: 'frequency',
                    store: this.freqStore,
                    valueField: 'frequencyid',
                    displayField: 'frequencyname',
                    value:'1',
                    mode: 'local',
                    typeAhead: true,
                    allowBlank:false,
                    triggerAction: 'all'
                }), 
                this.periodCombo=new Wtf.form.ComboBox({
                    fieldLabel:WtfGlobal.getLocaleText('acc.report.annexure2A.Period'),
                    hiddenName: 'frequency',
                    name: 'frequency',
                    store: this.periodStore,
                    valueField: 'periodid',
                    displayField: 'periodname',
                    mode: 'local',
                    typeAhead: true,
                    triggerAction: 'all',
                    allowBlank:false,
                    listWidth:300,
                    emptyText:WtfGlobal.getLocaleText('acc.report.annexure2A.Selectaperiod')
                }) 
            ]              
        }],
        buttonAlign: 'center',
        buttons:[{
            text: WtfGlobal.getLocaleText('acc.ra.export'),
            scope: this,
            handler: function(){
                if(!Wtf.isEmpty(this.freqCombo.getValue()) && !Wtf.isEmpty(this.periodCombo.getValue()) && !Wtf.isEmpty(this.yearCombo.getValue())){
                    var frequency=this.freqCombo.getValue();
                    var period=this.periodCombo.getValue();
                    var year=this.yearCombo.getValue();
                    var isForm202 = false;
                    download201ABC(type201, frequency, period, year, isForm202);
                }else{
                    if(this.freqCombo.getValue()==0 && Wtf.isEmpty(this.periodCombo.getValue())){
                    var frequency=this.freqCombo.getValue();
                    var period=this.periodCombo.getValue();
                    var year=this.yearCombo.getValue();
                    var isForm202 = false;
                    download201ABC(type201, frequency, period, year, isForm202);
                    }else{
                        this.periodCombo. markInvalid('This field is mandatory');
                    }
                }
            }
        },{
            text:'Cancel',
            handler: function(){
                this.form201ABCWindow.close();
            },
            scope: this
        }]
    });
    
    this.freqCombo.on('select',
        function(combo,record,index){ 
            this.periodCombo.reset();
            var data=new Array();
            if(combo.getValue()=='1'){ 
                this.periodCombo.setDisabled(false);
                var year=this.yearCombo.getValue();
                data.push(['quater1','1st April '+year+' to 30th June '+year+' (Quarter I)'], 
                    ['quater2','1st July '+year+' to 30th September '+year+' (Quarter II)'], 
                    ['quater3','1st October '+year+' to 31st December '+year+' (Quarter III)'],
                    ['quater4','1st January '+(year+1)+' to 31st Mar '+(year+1)+' (Quarter IV)']);
            }else if(combo.getValue()=='2'){
                this.periodCombo.setDisabled(false);
                data.push(['0','January'], ['1','February'], ['2','March'], ['3','April'], ['4','May'], ['5','June'], ['6','July'], ['7','August'], ['8','September'], ['9','October'], ['10','November'], ['11','December']);                
            }else{
                this.periodCombo.setDisabled(true);
            }
        this.periodStore.loadData(data);                
    },this);
        
    this.yearCombo.on('select',
        function(combo,record,index){ 
            if(this.freqCombo.getValue()=='1'){ 
                this.periodCombo.reset();
                var data=new Array();
                var year=this.yearCombo.getValue();
                data.push(['quater1','1st April '+year+' to 30th June '+year+' (Quarter I)'], 
                    ['quater2','1st July '+year+' to 30th September '+year+' (Quarter II)'], 
                    ['quater3','1st October '+year+' to 31st December '+year+' (Quarter III)'],
                    ['quater4','1st January '+(year+1)+' to 31st Mar '+(year+1)+' (Quarter IV)']);
            }
        this.periodStore.loadData(data);                
    },this);    
        
    this.form201ABCWindow.show();
}

function download201ABC(type201, frequency, period, year, isForm202){
    if(type201=="A"){
        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportForm21AInvoiceJasper.do?moduleid=2&templateflag="+Wtf.templateflag+"&isform202="+isForm202+"&frequency=" + frequency+"&period=" + period+"&year=" +year;
        this.form201ABCWindow.close();
    }else if(type201=="B"){
        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportForm21AInvoiceJasper.do?moduleid=6&templateflag="+Wtf.templateflag+"&isform202="+isForm202+"&frequency=" + frequency+"&period=" + period+"&year=" +year;
        this.form201ABCWindow.close();
    }else if(type201=="C"){
        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportForm201CJasper.do?report=FORM_703&filetype=pdf"+"&frequency=" + frequency+"&period=" + period+"&year=" +year;
        this.form201ABCWindow.close();
    }else if(type201=="D"){
        var align='none,none,none,none,none,none,none,none,none,none,none,none';
       
        var width='500,500,500,500,500,500,500,500,500,500,500,500';
        var header='taxIncomeNo~@date~@name~@rcNo~@goodsWithHSN~@valueOfGoods~@tax~@additionaltax~@total';
        var title='Tax Invoice No~@Date~@Name with RC No. of the register dealer from whom goods Sold~@Turnover of Sales of taxable goods';
        var subtitle='Name~@RC No.~@Goods with HSN~@Value of goods~@Tax~@Additional Tax~@Total';
        var url="ACCCombineReports/getVatReportsFomr201AExcelExport.do";

        url+="?isExport="+true+"&filetype=xls&filename=Form 201A"+'&header='+header+'&title='+title+'&subtitle='+subtitle+'&align='+align+'&width='+width+'&isForm201ExcelReport='+true+'&get=60'+"&isform202="+isForm202+"&frequency=" + frequency+"&period=" + period+"&year=" +year;

        Wtf.get('downloadframe').dom.src=url;
        this.form201ABCWindow.close();
    }else if(type201=="E"){
        var align='none,none,none,none,none,none,none,none,none,none,none,none';
       
        var width='500,500,500,500,500,500,500,500,500,500,500,500';
        var header='taxIncomeNo~@date~@name~@rcNo~@goodsWithHSN~@valueOfGoods~@tax~@additionaltax~@total';
        var title='Tax Invoice No~@Date~@Name with RC No. of the register dealer from whom goods purchased~@Turnover of purchase of taxable goods';
        var subtitle='Name~@RC No.~@Goods with HSN~@Value of goods~@Tax~@Additional Tax~@Total';
        var url="ACCCombineReports/getVatReportsFomr201AExcelExport.do";

        url+="?isExport="+true+"&filetype=xls&filename=Form 201B"+'&header='+header+'&title='+title+'&subtitle='+subtitle+'&align='+align+'&width='+width+'&isForm201ExcelReport='+true+'&get=60'+"&isform202="+isForm202+"&frequency=" + frequency+"&period=" + period+"&year=" +year+"&isform201B="+true;

        Wtf.get('downloadframe').dom.src=url;
        this.form201ABCWindow.close();
    }else if(type201=="F"){
        var align='none,none,none,none,none,none,none,none,none,none,none,none';
       
        var width='500,500,500,500,500,500,500,500,500,500,500,500';
        var header='godown,comoName,HSNCode,openingBal,incomingINTax,outgoingINTax,closingBal,valueOfClosingBal';
        var title='GODOWN,Name of commodity,HSN Code,Opening Balance Qty,Incoming during the tax period,outgoing during the tax period,closing balance,approximate value of closing balance';
        var url="ACCCombineReports/getVatReportsFomr201AExcelExport.do";

        url+="?isExport="+true+"&filetype=xls&filename=Form 201C"+'&header='+header+'&title='+title+'&subtitle='+subtitle+'&align='+align+'&width='+width+'&isForm201CExcelReport='+true+'&get=60'+"&isform202="+isForm202+"&frequency=" + frequency+"&period=" + period+"&year=" +year+"&isform201C="+true+"&border="+true;

        Wtf.get('downloadframe').dom.src=url;
        this.form201ABCWindow.close();
    }
}

function VATRepoForm402(){
    
    this.invoiceRec = new Wtf.data.Record.create([
        {name: 'billid',mapping:'billid'},
        {name: 'billno',mapping:'billno'}
    ]);
    this.invoiceStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.invoiceRec),
            url:"ACCInvoiceCMN/getInvoices.do"
         });
    this.invoiceStore.load({
        params:{
            isForm402:true
        }
    });
    this.form402Window = new Wtf.Window({
        height:240,
        width:370,
        modal:true,
        title: WtfGlobal.getLocaleText("acc.field.form402"),     
        border: false,
        items :[
        {
            region: 'north',
            height:70,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.form402"),"Select the invoice.","../../images/accounting_image/price-list.gif",'Frequncey')
        },{
            region: 'center',
            layout:'form',
            height:100,
            bodyStyle : 'padding:20px 10px 10px 10px',
            labelWidth:95,
            border: false,
            items:[
                this.invoiceCombo=new Wtf.form.ComboBox({
                    fieldLabel:WtfGlobal.getLocaleText("acc.selectinvoice.text"),
                    hiddenName: 'invoice',
                    name: 'invoice',
                    store: this.invoiceStore,
                    valueField: 'billid',
                    displayField: 'billno',
                    mode: 'local',
                    typeAhead: true,
                    allowBlank:false,
                    triggerAction: 'all'
                })
            ]              
        }],
        buttonAlign: 'center',
        buttons:[{
            text: WtfGlobal.getLocaleText('acc.ra.export'),
            scope: this,
            handler: function(){
                if(!Wtf.isEmpty(this.invoiceCombo.getValue())){
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportExciseFormERJasper.do?report=Form402&filetype=pdf&invoiceId="+this.invoiceCombo.getValue();
                    this.form402Window.close();
                }else{
                    this.invoiceCombo. markInvalid('This field is mandatory');
                }
            }
        },{
            text:'Cancel',
            handler: function(){
                this.form402Window.close();
            },
            scope: this
        }]
    });
            
    this.form402Window.show();
}

function MVATSalesPurchaseAnnexure(SalesOrPurchase){
    
    var monthsArray = [
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December'
    ];
    
    var returnTypeData = [["ORIGINAL"], ["REVISED u/s 20(4)(a)"],["REVISED u/s 20(4)(b)"],["REVISED u/s 20(4)(c)"]];
    this.returnTypeStore = new Wtf.data.SimpleStore({
        fields: ['name'],
        data: returnTypeData 
    });
    
    var returnFormFiledData = [['231'],['232'],['233'],['234'],['235'],['CST'],['231_CST'],['233_CST'],['234_CST'],['235_CST'],['231_234'],['233_234'],['233_235'],['231_234_CST'],['233_234_CST'],['233_235_CST']];
    this.returnFormFiledStore = new Wtf.data.SimpleStore({
        fields: ['name'],
        data: returnFormFiledData 
    });
    
    var financialYearData = [['2015-2016'], ['2016-2017'],['2017-2018'],['2018-2019']];
    this.financialYearStore = new Wtf.data.SimpleStore({
        fields: ['name'],
        data: financialYearData 
    });
    
    var YesNoData = [["Yes"], ['No']];
    this.YesNoStore = new Wtf.data.SimpleStore({
        fields: ['name'],
        data: YesNoData 
    });
    
    var returnPeriodData = [['2016 April'],['2016 May'],['2016 June'],['2016 July'],['2016 August'],['2016 September'],['2016 October'],['2016 November'],['2016 December'],['2017 January'],['2017 February'],['2017 March'],['2016 1st Quarter'],['2016 2nd Quarter'],['2016 3rd Quarter'],['2016 4th Quarter']];
    this.returnPeriodStore = new Wtf.data.SimpleStore({
        fields: ['name'],
        data: returnPeriodData
    });
    
    this.MVATSalesPurchaseAnnexureWindow = new Wtf.Window({
        height:500,
        width:500,
        modal:true,
        autoScroll : true,
        title: "MVAT " + SalesOrPurchase + " Annexure",     
        border: false,
        resizable : false,
        items :[
        {
            region: 'north',
            height:70,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml("MVAT " + SalesOrPurchase + " Annexure","","../../images/accounting_image/price-list.gif",'Frequncey')
        },{
            region: 'center',
            layout:'form',
            bodyStyle : 'padding:20px 10px 10px 10px; background: #f1f1f1 none repeat scroll 0 0;',
            border: false,
            items:[this.MVATSalesPurchaseAnnexureForm = new Wtf.form.FormPanel({
                border : false,
                labelWidth:150,
                items :[
                this.VATTINNo= new Wtf.form.TextField({
                    fieldLabel: 'M.V.A.T. R.C. No.', //M.V.A.T. R.C. No.
                    value: Wtf.CompanyVATNumber,
                    width:220,
                    readOnly:true,
                    maxLength:11,
                    invalidText :'Alphabets and numbers only',
                    vtype : "alphanum"
                }),
                this.CSTTINNo= new Wtf.form.TextField({
                    fieldLabel: 'CST. R.C. No.',//CST. R.C. No.
                    value: Wtf.CompanyCSTNumber,
                    width:220,
                    readOnly:true,
                    maxLength:11,
                    invalidText :'Alphabets and numbers only',
                    vtype : "alphanum"
                }),
                this.NameofDealer= new Wtf.form.TextField({
                    fieldLabel: 'Name of Dealer',//Name of Dealer
                    value: Wtf.companyfullname,
                    width:220,
                    readOnly:true,
                    maxLength:50
                }),
                this.returnTypeCombo = new Wtf.form.ComboBox({
                    fieldLabel: "Return  Type", //Return  Type
                    forceSelection: true,
                    width :220,
                    hiddenName: 'h_returntype',
                    name: 'returntype',
                    store: this.returnTypeStore,
                    valueField: 'name',
                    displayField: 'name',
                    value : 'ORIGINAL',
                    selectOnFocus: true,
                    mode: 'local',
                    triggerAction: 'all',
                    typeAhead: true
                }),
                this.returnFormsFiledCombo = new Wtf.form.ComboBox({
                    fieldLabel: "Return Forms to be filed", //Return Forms to be filed
                    forceSelection: true,
                    width :220,
                    hiddenName: 'h_returnformfiled',
                    name: 'returnformfiled',
                    store: this.returnFormFiledStore,
                    value : '231',
                    valueField: 'name',
                    displayField: 'name',
                    selectOnFocus: true,
                    mode: 'local',
                    triggerAction: 'all',
                    typeAhead: true
                }),
                this.financialYearCombo = new Wtf.form.ComboBox({
                    fieldLabel: "Financial Year", //Financial Year
                    forceSelection: true,
                    width :220,
                    hiddenName: 'h_financialyear',
                    name: 'financialyear',
                    store: this.financialYearStore,
                    valueField: 'name',
                    displayField: 'name',
                    selectOnFocus: true,
                    value : '2016-2017',
                    mode: 'local',
                    triggerAction: 'all',
                    typeAhead: true,
                    listeners :{
                        scope : this,
                        select : function(v,m, rec){
                            this.returnPeriodCombo.reset();
                            this.fromDate.reset();
                            this.toDate.reset();
                            this.returnPeriodStore.each(function(rec){
                                var dataArray = rec.data.name.split(" ");
                                if(dataArray.length>=3){
                                    rec.set('name', this.financialYearCombo.getValue().split("-")[0] + " "+ rec.data.name.split(" ")[1] + " " + rec.data.name.split(" ")[2]);
                                }else{
                                    rec.set('name', this.financialYearCombo.getValue().split("-")[0] + " "+ rec.data.name.split(" ")[1]);
                                }
                            },this)
                        }
                    }
                }),
                this.returnPeriodCombo = new Wtf.form.ComboBox({
                    fieldLabel: "Return Period", //Return Period
                    forceSelection: true,
                    width :220,
                    hiddenName: 'h_returnperiod',
                    name: 'returnperiod',
                    store: this.returnPeriodStore,
                    valueField: 'name',
                    displayField: 'name',
                    selectOnFocus: true,
                    mode: 'local',
                    triggerAction: 'all',
                    typeAhead: true,
                    listeners :{
                        scope : this,
                        select : function(v,m, rec){
                            var dataArray = this.returnPeriodCombo.getValue().split(" ");
                            var firstDayMonth = new Date();
                            var lastDayMonth = new Date();
                            
                            if(dataArray.length>=3){
                                var year = dataArray[0];
                                if(dataArray[1] == "1st"){
                                    firstDayMonth = new Date(year, 3, 1);
                                    lastDayMonth = new Date(year, 5 + 1, 0);
                                }else if(dataArray[1] == "2nd"){
                                    firstDayMonth = new Date(year, 6, 1);
                                    lastDayMonth = new Date(year, 8 + 1, 0);
                                }else if(dataArray[1] == "3rd"){
                                    firstDayMonth = new Date(year, 9, 1);
                                    lastDayMonth = new Date(year, 11 + 1, 0);
                                }else  if(dataArray[1] == "4th"){
                                    firstDayMonth = new Date(parseInt(year) + 1, 0, 1);
                                    lastDayMonth = new Date(parseInt(year) + 1, 2 + 1, 0);
                                }
                            }else{
                                var year = dataArray[0];
                                var month = (monthsArray.indexOf(dataArray[1])); 
                                firstDayMonth = new Date(year, month, 1);
                                lastDayMonth = new Date(year, month + 1, 0);
                            }
                            this.fromDate.setValue(firstDayMonth);
                            this.toDate.setValue(lastDayMonth);
                        }
                    }
                }), this.fromDate = new Wtf.form.DateField({
                    fieldLabel : WtfGlobal.getLocaleText("acc.nee.FromDate"),
                    format : WtfGlobal.getOnlyDateFormat(),
                    name : 'fromdate',
                    hiddenName : 'h_fromdate',
                    readOnly:true,
                    disabled:true,
                    width : 220
                }), 
                this.toDate = new Wtf.form.DateField({
                    fieldLabel : WtfGlobal.getLocaleText("acc.nee.ToDate"),
                    format : WtfGlobal.getOnlyDateFormat(),
                    name : 'todate',
                    hiddenName : 'h_todate',
                    readOnly:true,
                    disabled:true,
                    width : 220
                }),
                this.whetherFirstReturnCombo = new Wtf.form.ComboBox({
                    fieldLabel: "Whether First Return", //Whether First Return
                    forceSelection: true,
                    width :220,
                    hiddenName: 'h_whetherfirstreturn',
                    name: 'whetherfirstreturn',
                    store: this.YesNoStore,
                    valueField: 'name',
                    displayField: 'name',
                    value : 'Yes',
                    selectOnFocus: true,
                    mode: 'local',
                    triggerAction: 'all',
                    typeAhead: true
                }),
                this.whetherLastReturnCombo = new Wtf.form.ComboBox({
                    fieldLabel: "Whether Last Return", //Whether Last Return
                    forceSelection: true,
                    width :220,
                    hiddenName: 'h_whetherlastreturn',
                    name: 'whetherlastreturn',
                    store: this.YesNoStore,
                    valueField: 'name',
                    displayField: 'name',
                    value : 'No',
                    selectOnFocus: true,
                    mode: 'local',
                    triggerAction: 'all',
                    typeAhead: true
                }),
               
                this.NameofAuthPerson= new Wtf.form.TextField({
                    fieldLabel: 'Name of Authorised Person*',//Name of Authorised Person
                    width:220,
                    name : 'auth_person',
                    allowBlank: false,
                    hiddenName : 'h_auth_person',
                    value:Wtf.account.companyAccountPref.authorizedperson,
                    maxLength:50
                }),
                this.designation= new Wtf.form.TextField({
                    fieldLabel: 'Designation*',//Designation
                    width:220,
                    name : 'designation',
                    allowBlank: false,
                    value:Wtf.account.companyAccountPref.statusordesignation,
                    hiddenName : 'h_designation',
                    maxLength:50
                }),
                this.mobileNumber= new Wtf.form.NumberField({
                    fieldLabel: "Mobile No.*", //Mobile Number
                    name: 'mobile',
                    hiddenName : 'h_mobile',
                    width:220,
                    maxLength:15,
                    allowBlank: false,
                    allowDecimals:false,
                    allowNegative:false            
                }),
                this.EmMailId=new Wtf.form.TextField({
                    fieldLabel : "Email Id*", //Email Id
                    name:'emialid',
                    hiddenName : 'h_emialid',
                    maxLength:100,
                    scope:this,
                    allowBlank: false,
                    width :220,
                    validator:WtfGlobal.validateMultipleEmail
                })
                ]
            })
            ]              
        }],
        buttonAlign: 'right',
        buttons:[{
            text: WtfGlobal.getLocaleText('acc.ra.export'),
            scope: this,
            handler: function(){
                if(this.MVATSalesPurchaseAnnexureForm.getForm().isValid()){
                    if(this.fromDate.getValue()!=undefined && this.fromDate.getValue()!="" && this.toDate.getValue()!=undefined && this.toDate.getValue()!=""){
                        if(this.fromDate.getValue()>this.toDate.getValue()){
                            this.fromDate.marInvalid();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.1")], 2);    
                            return;
                        }
                    }
                //More Code write here
               var startDate= WtfGlobal.convertToGenericDate(this.fromDate.getValue());
               var endDate = WtfGlobal.convertToGenericDate(this.toDate.getValue());
               var code=this.returnFormsFiledCombo.getValue();
                var align='none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none';
                var width='500,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200,200';
                    
                var header='srno,invoicenos,dosalesinvoice,tinofPurchase,assessableamount,'
                +'taxamount,inclusivetax,valuecompo42,taxfree,exempted41,'
                +'labourcharges,othercharges,grosstotal,action,returnformno,'
                +'transactioncode,transcationdesc';
            
                var titleSales='Sr no.,Invoice No.,Date of Sale Invoice,TIN of Purchaser (If Any ),Net Rs.,TAX (If any) Rs.,Value of Inclusive of Tax Rs.,Value of Composition u/s 42 (1) and (2),Tax Free Sales,Exempted Sales u/s 41 and 8 ,Labour Charges,Other Charges,Gross Total (Rs.),Action,Return Form Number,Transaction Code,Description of Transaction type';
                var titlepurchase='Sr no.,Sales Invoice No.,Date of Sale Invoice,TIN of Seller (If Any ),Net Rs.,TAX (If any) Rs.,Value of Inclusive of Tax Rs.,Value of Composition u/s 42 (1) and (2),TAX Free Purchases,Exempted Purchase u/s 41 and 8 ,Labour Charges,Other Charges,Gross Total (Rs.),Action,Return Form Number,Transaction Code,Description of Transaction type';
                
                var title=SalesOrPurchase==="Sales"?titleSales:titlepurchase; 
                
                var url="ACCCombineReports/getSalesPurhcaseAnnexureExport.do";
                if(SalesOrPurchase==="Sales"){                    
                    url+="?startdate=" + startDate+"&enddate=" + endDate+"&isSalesAnnax="+true+"&isExport="+true+"&filetype=xls&filename="+"MVAT " + SalesOrPurchase + " Annexure"+'&header='+header+'&title='+title+'&align='+align+'&width='+width+"&code="+code;
                    
                }else if(SalesOrPurchase==="Purchase"){
                  url+="?startdate=" + startDate+"&enddate=" + endDate+"&isSalesAnnax="+false+"&isExport="+true+"&filetype=xls&filename="+"MVAT " + SalesOrPurchase + " Annexure"+'&header='+header+'&title='+title+'&align='+align+'&width='+width+"&code="+code;
                }
                
                Wtf.get('downloadframe').dom.src=url;
                this.MVATSalesPurchaseAnnexureWindow.close();
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.tar.required")], 2);    
                }
            }
        },{
            text:'Cancel',
            handler: function(){
                this.MVATSalesPurchaseAnnexureWindow.close();
            },
            scope: this
        }]
    });
    this.MVATSalesPurchaseAnnexureWindow.show();
}

function CSTForm6(){
    var align='none,none,none,none,none,none,none,none,none,none,none,none,none,none';
    var width='500,200,200,200,200,200,200,200,200,200,200,200,200,200,200';

    var header='date,name,state,isbranch,regno1,regno2,desc,quantity,ship,challan,invoice,remarks';

    var titleSales='Date of receipt of goods,Name and full address of the transferor of goods ,Name of State from which goods transferred ,State here whether the transfer is to your HeadOffice/Branch/Agent/ Principal,Under Central Act,Under General Sales Tax/ Value Added Tax law of relevant State,Description of goods,Quantity of goods received on transfer ,Name of the carrier (i.e. rail/transport company/air company) and RR/GR No. etc. or other relevant particulars ,Particulars of challan or other documents covering the goods received,SI. No. of declaration in Form ‘F’ issued ,Remarks ';

    var title=titleSales; 

    var url="ACCCombineReports/getCSTForm6Export.do";
    url+="?isExport="+true+"&filetype=xls&filename=CST_form6"+'&header='+header+'&title='+title+'&align='+align+'&width='+width+'&isFromCSTReport='+true+'&get=60';

    Wtf.get('downloadframe').dom.src=url;
}

function downloadServiceTaxCreditRegister(){
    this.freqStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'frequencyid'
        },{
            name:'frequencyname'
        }],
        data:[['0','Annually'],['1','Quaterly'], ['2','Monthly']]
    });
        
    this.periodStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'periodid'
        },{
            name:'periodname'
        }],
        data:[['quater1','1st April '+new Date().getFullYear()+' to 30th June '+new Date().getFullYear()+' (Quarter I)'], 
            ['quater2','1st July '+new Date().getFullYear()+' to 30th September '+new Date().getFullYear()+' (Quarter II)'], 
            ['quater3','1st October '+new Date().getFullYear()+' to 31st December '+new Date().getFullYear()+' (Quarter III)'],
            ['quater4','1st January '+(new Date().getFullYear()+1)+' to 31st Mar '+(new Date().getFullYear()+1)+' (Quarter IV)']]
    });
    
    var yearArr=new Array()
    for(var i=2000;i<2051;i++){
        yearArr.push(i, i)
    }
    this.yearStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'yearid'
        },{
            name:'year'
        }],
        data:[['2000','2000']]
    });

    Wtf.yearRec = new Array();
    for(var i=0; i<50; i++){
        Wtf.yearRec[i] = new Array((i+2001), (i+2001)+" - "+(i+2002));
    }

    Wtf.yearStore = new Wtf.data.SimpleStore({
        id    : 'monStore',
        fields: ['id','name'],
        data: Wtf.yearRec
    });
    
    this.serviceTaxCreditRegisterWindow = new Wtf.Window({
        height:240,
        width:370,
        modal:true,
        title: WtfGlobal.getLocaleText("acc.reports.india.serviceTaxReports.serviceTaxCreditRegister"),
        border: false,
        items :[
        {
            region: 'north',
            height:70,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml("Export "+WtfGlobal.getLocaleText("acc.reports.india.serviceTaxReports.serviceTaxCreditRegister"),"Select From Date and To Date","../../images/accounting_image/price-list.gif",'Frequncey')
        },{
            region: 'center',
            layout:'form',
            height:100,
            bodyStyle : 'padding:20px 10px 10px 10px',
            labelWidth:95,
            border: false,
            items:[
                this.yearCombo=new Wtf.form.ComboBox({
                    fieldLabel:'Financial Year',//WtfGlobal.getLocaleText('acc.report.annexure2A.Year'),
                    hiddenName: 'year',
                    name: 'year',
                    store: Wtf.yearStore,
                    valueField: 'id',
                    displayField: 'name',
                    value:new Date().getFullYear(),
                    mode: 'local',
                    typeAhead: true,
                    allowBlank:false,
                    triggerAction: 'all'
                }), 
                this.freqCombo=new Wtf.form.ComboBox({
                    fieldLabel:WtfGlobal.getLocaleText('acc.report.annexure2A.Frequency'),
                    hiddenName: 'frequency',
                    name: 'frequency',
                    store: this.freqStore,
                    valueField: 'frequencyid',
                    displayField: 'frequencyname',
                    value:'1',
                    mode: 'local',
                    typeAhead: true,
                    allowBlank:false,
                    triggerAction: 'all'
                }), 
                this.periodCombo=new Wtf.form.ComboBox({
                    fieldLabel:WtfGlobal.getLocaleText('acc.report.annexure2A.Period'),
                    hiddenName: 'frequency',
                    name: 'frequency',
                    store: this.periodStore,
                    valueField: 'periodid',
                    displayField: 'periodname',
                    mode: 'local',
                    typeAhead: true,
                    triggerAction: 'all',
                    allowBlank:false,
                    listWidth:300,
                    emptyText:WtfGlobal.getLocaleText('acc.report.annexure2A.Selectaperiod')
                }) 
            ]              
        }],
        buttonAlign: 'center',
        buttons:[{
            text: WtfGlobal.getLocaleText('acc.ra.export'),
            scope: this,
            handler: function(){
                if(!Wtf.isEmpty(this.yearCombo.getValue()) && !Wtf.isEmpty(this.freqCombo.getValue()) && (!Wtf.isEmpty(this.periodCombo.getValue()) || (this.freqCombo.getValue()==0 && Wtf.isEmpty(this.periodCombo.getValue())))){
                    var frequency=this.freqCombo.getValue();
                    var period=this.periodCombo.getValue();
                    var year=this.yearCombo.getValue();
                    var get=Wtf.autoNum.ServiceTaxCreditRegister;

                    var header = "entryno,date,invoiceno,invoicedate,vendorname,servicetaxregno,descriptionofservice,typeofservice,cenvatcreditoninputservice"
                            +",invoiceamount,servicetax,sbc,kkc,paymentno,paymentdate,openingbalanceservicetax,openingbalancesbc,openingbalancekkc"
                            +",credittakenservicetax,credittakensbc,credittakenkkc,creditutilizedservicetax,creditutilizedsbc,creditutilizedkkc"
                            +",balanceservicetax,balancesbc,balancekkc,remarks";
                    var title = "Entry No,Date,No,Date,Name,Service Tax Regn. No,Description of Input Service,Classification of Input Service"
                            +",Cenvat Credit On Input Services and RCM,Value of Taxable Service Paid,Service Tax,SBC,KKC,Payment No.,Date,Service Tax,SBC,KKC"
                            +",Service Tax,SBC,KKC,Service Tax,SBC,KKC,Service Tax,SBC,KKC,Remarks";
                    var align = "none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none";
                    var width = "300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300,300";

                    var url = "ACCCombineReports/exportServiceTaxCreditRegister.do?ServiceTaxCreditRegister=true&get="+ get +"&frequency=" + frequency+"&period=" + period+"&year=" +year;
                    url +="&filetype=xls&filename=Service Tax Credit Register"+'&header='+header+'&title='+title+'&align='+align+'&width='+width;

                    Wtf.get('downloadframe').dom.src = url;
                    this.serviceTaxCreditRegisterWindow.close();
                }else{
                    this.periodCombo. markInvalid('This field is mandatory');
                }
            }
        },{
            text:'Cancel',
            handler: function(){
                this.serviceTaxCreditRegisterWindow.close();
            },
            scope: this
        }]
    });
    
    this.freqCombo.on('select',
        function(combo,record,index){ 
            this.periodCombo.reset();
            var data=new Array();
            if(combo.getValue()=='1'){ 
                this.periodCombo.setDisabled(false);
                var year=this.yearCombo.getValue();
                data.push(['quater1','1st April '+year+' to 30th June '+year+' (Quarter I)'], 
                    ['quater2','1st July '+year+' to 30th September '+year+' (Quarter II)'], 
                    ['quater3','1st October '+year+' to 31st December '+year+' (Quarter III)'],
                    ['quater4','1st January '+(year+1)+' to 31st Mar '+(year+1)+' (Quarter IV)']);
            }else if(combo.getValue()=='2'){
                this.periodCombo.setDisabled(false);
                data.push(['0','January'], ['1','February'], ['2','March'], ['3','April'], ['4','May'], ['5','June'], ['6','July'], ['7','August'], ['8','September'], ['9','October'], ['10','November'], ['11','December']);                
            }else{
                this.periodCombo.setDisabled(true);
            }
        this.periodStore.loadData(data);                
    },this);
        
    this.yearCombo.on('select',
        function(combo,record,index){ 
            if(this.freqCombo.getValue()=='1'){ 
                this.periodCombo.reset();
                var data=new Array();
                var year=this.yearCombo.getValue();
                data.push(['quater1','1st April '+year+' to 30th June '+year+' (Quarter I)'], 
                    ['quater2','1st July '+year+' to 30th September '+year+' (Quarter II)'], 
                    ['quater3','1st October '+year+' to 31st December '+year+' (Quarter III)'],
                    ['quater4','1st January '+(year+1)+' to 31st Mar '+(year+1)+' (Quarter IV)']);
            }
        this.periodStore.loadData(data);                
    },this);    
        
    this.serviceTaxCreditRegisterWindow.show();
}