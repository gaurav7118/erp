Wtf.Statutory = function(config){
    this.nodeHash = {};
    var tree;
    var outbox;
    var drafts;
    var deleteditems;
    var starreditems;
    var temptreenode;
    var folders;
    var nodeid;
    var treeObj;
    var composeMail;
    
    Wtf.Statutory.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.Statutory, Wtf.tree.TreePanel, {
    autoWidth: true,
    autoHeight: true,
    rootVisible: false,
//    id: 'folderview',
    border:false,
    autoScroll: true,
    animate: Wtf.enableFx,
    enableDD: false,
    hlDrop: Wtf.enableFx,
    
        
    initComponent: function(){
        Wtf.Statutory.superclass.initComponent.call(this);
        treeObj = this;
        this.reportBuildObj = {};
          function _openFunction(node){
            switch (node.id) {
                case "912":
                    callIAFfileWindow();
                    break;
                case "IRASAuditeSubmission":
                    callIAFfileWindowforeSubmission();
                    break;
                case "IRASAuditeSubmissionHistory":
                    callIAFfileWindowforeSubmissionHistory();
                    break;
                case "9111":
                    GSTForm5Tab();
                    break;              
                case "GSTForm5eSubmissionDetails":
                    callGSTForm5eSubmissionHistory();
                    break;                            
                case "9112":
                    GSTReportTab();
                    break;              
                case "9113":
                    NewGSTForm5DetailedView();
                    break;
                case "bad1":
                    BadDebtInvoices();
                    break;
                case "bad2":
                    BadDebtPurchaseInvoices();
                    break;
                case "tapfile":
                    callMalasianGSTWindow('tapfile');
                    break;
                case "auditfile":
                    callMalasianGSTWindow('auditfile');
                    break;
                case "tapreturnfile":
                    callMalasianGSTWindow('tapreturnfile');
                    break;
                case "gstrep":
                    GSTReportTab();
                    break;
                case "doadjs":
                    getTaxableDeliveryOrdersPanel();
                    break;
                case "inputadj":
                    callTaxAdjustmentWindow(false)
                    break;
                case "outputadj":
                    callTaxAdjustmentWindow(true)
                    break;
                case "badsaleinvoiceid":
                    badDebtReport(true, true);
                    break;
                case "badrecoverinvoiceid":
                    badDebtReport(false,true);
                    break;
                case "badpurchaseinvoiceid":
                    badDebtReport(true,false);
                    break;
                case "badpurchaserecoverinvoiceid":
                    badDebtReport(false,false);
                    break;
                case "gsttapdetailedview":
                    NewGSTForm5DetailedView();
                    break;
                case "taxreport":
                    /**
                     * ERM - 294
                     * Disable this button when Avalara Integration is on
                     */
                    if (Wtf.account.companyAccountPref.avalaraIntegration) {
                        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.integration.invoiceTaxReportDisabledForAvalaraMsg"));
                    }
                    else
                    {
                        callTaxReport();
                    }
                    break;
                case "exiceRepoER5":
                    exciseFormER_5();
                    break;
                case "VATRepoDVAT16":
                    VATForm16();
                    break;
                case "vatRepoAnnexure2A":
                    vatRepoAnnexure_2A(true);
                    break;
                case "vatRepoAnnexure2B":
                    vatRepoAnnexure_2A(false);
                    break;
                case "VATcomputation":
                    callCommonReportView(11,WtfGlobal.getLocaleText("acc.reports.india.VATcomputation"));
                    break;
                case "VATPurchaseRegister":
                    getVatSalesRegister(false, false);
                    break;
                case "VATSalesRegister":
                    getVatSalesRegister(false, true);
                    break;
                case "VATRepoForm201A":
                    getVATRepoForm201("A");
                    break;
                case "VATRepoForm201B":
                    getVATRepoForm201("B");
                    break;
                case "VATRepoForm201C":
                    getVATRepoForm201("C");
                    break;
                case "MVATSalesAnnexure":
                    MVATSalesPurchaseAnnexure("Sales");
                    break;
                case "MVATPurchaseAnnexure":
                    MVATSalesPurchaseAnnexure("Purchase");
                    break;
                case "AnnexureII57F4I":
                    AnnexureII57F4("1");
                    break;
                case "AnnexureII57F4II":
                    AnnexureII57F4("2");
                    break;
                case "AnnexureII57F4III":
                    AnnexureII57F4("3");
                    break;
                case "CreateConsolidation":
                    callCreateConsolidationReportTab();
                    break;
                case "ConsolidationRepGen":
                    callConsolidationReportGenerationTab();
                    break;
                case "ConsolidationReport":
                    callConsolidationReportTab();
                    break;
                case "VATRepoForm402":
                    VATRepoForm402();
                    break;
                case "tdschallanreport":
                    getTDSChallanControlReport();
                    break;
                case "tdsnotapplicablereport":
                    callCommonReportView(6,WtfGlobal.getLocaleText("acc.reports.india.TDS.notds.report.title"));
                    break;
                case "tdsunknowndeducteereport":
                    callCommonReportView(7,WtfGlobal.getLocaleText("acc.reports.india.TDS.Deductee.report.title"));
                    break;
                case "tdspanreport":
                    callCommonReportView(8,WtfGlobal.getLocaleText("acc.reports.india.TDS.pan.report.title"));
                    break;
                case "tdspaymentreport":
                    callCommonReportView(9,WtfGlobal.getLocaleText("acc.reports.india.TDS.payment.report.title"));
                    break;
                case "servicetaxcreditregisterreport":
                    downloadServiceTaxCreditRegister();
                    break;
                case "CstReportForm6":
                    CSTForm6();
                    break;
                case "philippinesVATSummaryReport":
                    loadPhpVATSummaryReport();
                    break;
                case "indonesiaVATOutReport":
                    loadIndonesiaVATSummaryReport();
                    break;
                case "VATRepoForm201A_Excel":
                    getVATRepoForm201("D");
                    break;
                case "VATRepoForm201B_Excel":
                    getVATRepoForm201("E");
                    break;
                case "VATRepoForm201C_Excel":
                    getVATRepoForm201("F");
                    break;
                case "DVATForm31":
                    getFormDVAT31Report();
                    break;
                case "PHPPurchasesReliefSummaryReport":
                    loadPHPPurchasesReliefSummaryReport();
                    break;
                case "PHPSalesReliefSummaryReport":
                    loadPHPSalesReliefSummaryReport(); // function call for philippines sales releif report summary 
                    break;
                case "ConsolidationProfitAndLossReport":
                    callConsolidationProfitAndLossReportTab();
                    break;
                case "ConsolidationBalanceSheetReport":
                    callConsolidationBalanceSheetReportTab();
                    break;
                case "ConsolidationStockReport":
                    callConsolidationStockReportTab();
                    break;
                case "21":
                    GSTR1Report();
                    break;
                case "23":
                    var gstrreporttype=1;
                    callGSTRSummaryReport(gstrreporttype);
                    break;
                case "24":
                    var gstrreporttype=2;
                    callGSTRSummaryReport(gstrreporttype);
                    break;
                case "25":
                    callGSTComputationReportDynamic();
                    break;
                case "28":
                    var gstrreporttype = 3;
                    callGSTRSummaryReport(gstrreporttype);
                    break;
                case "29":
                    var gstrreporttype = 1; // call GSTR2 Match And Reconcile report
                    callGSTR2MatchAndComparisionReport(gstrreporttype);
                    break;
                case "30":
                    var gstrreporttype = 2; // call GSTR2A Comparison report
                    callGSTR2MatchAndComparisionReport(gstrreporttype);
                    break;
                case "31":
                    callGSTR3BSummaryReportDynamic();
                    break;
            }
          } 
      function _createNode(nodeText, nodeID, canDrag, isLeaf, nodeIcon){
            var treeNode=new Wtf.tree.TreeNode({
                text: nodeText,
                id: nodeID,
                cls:'paddingclass',
                allowDrag: canDrag,
                leaf: isLeaf,
                icon: nodeIcon
            });
            treeNode.on("click",function(node){
                _openFunction(node);
            },this);
            return treeNode;
        }

        var root1 = new Wtf.tree.AsyncTreeNode({
            text: '',
            expanded: true
        });           
        var arrayList = new Array();
        var arrayListEntry = new Array();
        var arrayGSTListEntry = new Array();
        var arrayConsolidationListEntry = new Array();
        var arrayExciseListEntry = new Array(); // For Indian Company Excise duty tax
        var arrayVATListEntry = new Array(); // For Indian Company VAT/CST Tax
        var arrayVATCSTListEntry = new Array(); // For Indian Company VAT/CST Tax
        var arrayTDSListEntry = new Array(); // For Indian Company VAT/CST Tax
        var GSTRListEntry = new Array(); // For Indian Company GSTR Reports
        var PHPRListEntry=new Array();// Created Array For Philippines Country Relief Reports child nodes.
        var arrayServiceTaxListEntry = new Array(); // For Indian Company Service Tax
        var nodeAnnexuresList = new Array(); // For Indian Company Annexures Form 201A 201B 201C
        var reportsNode=_createNode(WtfGlobal.getLocaleText("acc.dash.rep"), '91', false, false, 'images/Account_Payable/Reports.png');
        arrayList.push(reportsNode);
        
        var salesBadDebtReleifNode=_createNode('Sales Bad Debt Relief Adjustment', 'bad1', false, false, 'images/Account_Payable/Reports.png');
        arrayList.push(salesBadDebtReleifNode);
        
        var purchaseBadDebtReleifNode=_createNode('Purchase Bad Debt Relief Adjustment', 'bad2', false, false, 'images/Account_Payable/Reports.png');
        arrayList.push(purchaseBadDebtReleifNode);
        
        var DeliveryOrderAdjustment=_createNode('Delivery Order Adjustment', 'doadjs', false, false, 'images/Account_Payable/Reports.png');
        arrayList.push(DeliveryOrderAdjustment);
        
        var InputTaxAdjustment=_createNode('Input Tax Adjustment', 'inputadj', false, false, 'images/Account_Payable/Reports.png');
        arrayList.push(InputTaxAdjustment);
        
        var OutputTaxAdjustment=_createNode('Output Tax Adjustment', 'outputadj', false, false, 'images/Account_Payable/Reports.png');
        arrayList.push(OutputTaxAdjustment);
        
        var gstNode=_createNode(WtfGlobal.getLocaleText("coa.masterType.GST"), '911', false, false, 'images/Statutory/gst.png');
        
        // Created node for Relief Reports
        var reliefReportNode=_createNode(WtfGlobal.getLocaleText("acc.php.releifreport"), 'reliefreports', false, false, 'images/Account_Payable/Reports.png');
        
        if (Wtf.UserReporRole.URole.roleid == Wtf.ADMIN_ROLE_ID) {
            arrayListEntry.push(_createNode('GST Form 03', 'tapfile', false, true, 'images/Statutory/iras-audit-file.png'));
            arrayListEntry.push(_createNode('GST Audit File', 'auditfile', false, true, 'images/Statutory/iras-audit-file.png'));
            arrayListEntry.push(_createNode('GST Report', 'gstrep', false, true, 'images/Statutory/iras-audit-file.png'));
            arrayListEntry.push(_createNode('GST Tap Return File', 'tapreturnfile', false, true, 'images/Statutory/iras-audit-file.png'));
            arrayListEntry.push(_createNode('Bad Debt Claimed Sales Invoices', 'badsaleinvoiceid', false, true, 'images/Account_Payable/Receipts-Register.png'));
            arrayListEntry.push(_createNode('Bad Debt Recovered Sales Invoices', 'badrecoverinvoiceid', false, true, 'images/Account_Payable/Vendor-Receipt-Payment.png'));
            arrayListEntry.push(_createNode('Bad Debt Claimed Purchase Invoices', 'badpurchaseinvoiceid', false, true, 'images/General_Ledger/Listing-of-Masters.png'));
            arrayListEntry.push(_createNode('Bad Debt Recovered Purchase Invoices', 'badpurchaserecoverinvoiceid', false, true, 'images/General_Ledger/Voucher-Printing.png'));
        }else{
            for(var userpermcount=0;userpermcount<Wtf.UserReportPerm.length;userpermcount++)
                {
                    if(Wtf.UserReportPerm[userpermcount]==Wtf.ReportListName.Gstreport)
                    {
                    arrayListEntry.push(_createNode('GST Form 03', 'tapfile', false, true, 'images/Statutory/iras-audit-file.png'));
                    arrayListEntry.push(_createNode('GST Audit File', 'auditfile', false, true, 'images/Statutory/iras-audit-file.png'));
                    arrayListEntry.push(_createNode('GST Report', 'gstrep', false, true, 'images/Statutory/iras-audit-file.png'));
                    arrayListEntry.push(_createNode('GST Tap Return File', 'tapreturnfile', false, true, 'images/Statutory/iras-audit-file.png'));  
                    arrayListEntry.push(_createNode('Bad Debt Claimed Sales Invoices', 'badsaleinvoiceid', false, true, 'images/Account_Payable/Receipts-Register.png'));
                    arrayListEntry.push(_createNode('Bad Debt Recovered Sales Invoices', 'badrecoverinvoiceid', false, true, 'images/Account_Payable/Vendor-Receipt-Payment.png'));
                    arrayListEntry.push(_createNode('Bad Debt Claimed Purchase Invoices', 'badpurchaseinvoiceid', false, true, 'images/General_Ledger/Listing-of-Masters.png'));
                    arrayListEntry.push(_createNode('Bad Debt Recovered Purchase Invoices', 'badpurchaserecoverinvoiceid', false, true, 'images/General_Ledger/Voucher-Printing.png'));
                    break;
                    } 
                }
        }
        
        
         if(Wtf.UserReporRole.URole.roleid==1 && Wtf.Countryid!=Wtf.Country.INDIA){
            arrayGSTListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.GSTForm5"), '9111', false, true, 'images/Statutory/gst-form-5.png'));
            arrayGSTListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.GSTInputTaxRegister"), '9112', false, true, 'images/Statutory/gst-form-5.png'));
            arrayGSTListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.BreakupOfGSTBoxes19"), '9113', false, true, 'images/Statutory/breakup-of-gst-boxes.png'));
            if(Wtf.Countryid!=Wtf.Country.US){
                arrayListEntry.push(gstNode);
                arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.IRASAuditFile"), '912', false, true, 'images/Statutory/iras-audit-file.png'));
            }

            // IRAS Integration
            if (Wtf.Countryid == Wtf.Country.SINGAPORE) {
                arrayGSTListEntry.push(_createNode("GST Form 5 e-Submission History", 'GSTForm5eSubmissionDetails', false, true, 'images/Statutory/breakup-of-gst-boxes.png'));
                arrayListEntry.push(_createNode("GST Transaction Listing Submission", 'IRASAuditeSubmission', false, true, 'images/Statutory/iras-audit-file.png'));
                arrayListEntry.push(_createNode("GST Transaction Listing Submission History", 'IRASAuditeSubmissionHistory', false, true, 'images/Statutory/iras-audit-file.png'));
            }
          
          arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.taxReport"), 'taxreport', false, true, 'images/Statutory/gst-form-5.png'));
        }
        else if(Wtf.Countryid!=Wtf.Country.INDIA){ //GST Not applicable for India.
            for(var userpermcount=0;userpermcount<Wtf.UserReportPerm.length;userpermcount++)
                {
                    if(Wtf.UserReportPerm[userpermcount]==Wtf.ReportListName.Gstreport)
                    {
                        arrayGSTListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.GSTForm5"), '9111', false, true, 'images/Statutory/gst-form-5.png'));
                        arrayGSTListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.GSTInputTaxRegister"), '9112', false, true, 'images/Statutory/gst-form-5.png'));
                        arrayGSTListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.BreakupOfGSTBoxes19"), '9113', false, true, 'images/Statutory/breakup-of-gst-boxes.png'));
                        arrayListEntry.push(gstNode);              
                          
                    }
                }
            for(var userpermcount=0;userpermcount<Wtf.UserReportPerm.length;userpermcount++){
                if(Wtf.UserReportPerm[userpermcount]==Wtf.ReportListName.IrasAudifile){
                    arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.IRASAuditFile"), '912', false, true, 'images/Statutory/iras-audit-file.png'));
                }
            }
            
            for(var x=0;x<Wtf.UserReportPerm.length;x++){
                if(Wtf.UserReportPerm[x]==Wtf.ReportListName.TaxReport){
                    arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.taxReport"), 'taxreport', false, true, 'images/Statutory/gst-form-5.png'));
                }
            }
        }
        
        /******************************Consolidation********************************/
            var consolidation_Node=_createNode(WtfGlobal.getLocaleText("acc.conslodation.consolidation"), 'consolidation', false, false, 'images/Statutory/consolidation-icon.png');
            arrayConsolidationListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.conslodation.createConsolidation"), 'CreateConsolidation', false, true, 'images/Statutory/create-consolidation-icon.png'));
            arrayConsolidationListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.conslodation.consolidationReportGeneration"), 'ConsolidationRepGen', false, true, 'images/Statutory/consolidation-report-generation-icon.png'));
            arrayConsolidationListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.conslodation.ConsolidationReport"), 'ConsolidationReport', false, true, 'images/Statutory/consolidation-report-icon.png'));
            arrayConsolidationListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.consolidation.consolidationprofitandloss"), 'ConsolidationProfitAndLossReport', false, true, 'images/Statutory/consolidation-profit-and-loss-icon.png'));
            arrayConsolidationListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.consolidation.consolidationbalancesheet"), 'ConsolidationBalanceSheetReport', false, true, 'images/Statutory/consolidation-balance-sheet-icon.png'));
            arrayConsolidationListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.consolidation.consolidationstockreport"), 'ConsolidationStockReport', false, true, 'images/Statutory/stock-consolidated-report-icon.png'));
            consolidation_Node.appendChild(arrayConsolidationListEntry);
            arrayListEntry.push(consolidation_Node);
        
        ////////////////////////////////////// Statutory Reports for INDIAN Company ////////////////////////////////////////////////////////////
//        var exciseNode=_createNode(WtfGlobal.getLocaleText("acc.field.india.exciseReports"), 'exiceRepo', false, false, 'images/Statutory/gst.png');
//        if(Wtf.UserReporRole.URole.roleid==1 && Wtf.Countryid==Wtf.Country.INDIA){
////                arrayExciseListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.india.exciseReportsER4"), 'exiceRepoER4', false, true, 'images/Statutory/gst-form-5.png'));
//                arrayExciseListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.india.exciseReportsER5"), 'exiceRepoER5', false, true, 'images/Statutory/gst-form-5.png'));
//                arrayListEntry.push(exciseNode);
//        }
        var VATCSTNode=_createNode(WtfGlobal.getLocaleText("acc.reports.india.VAT"), 'VATCSTRepo', false, false, 'images/Statutory/gst.png');
        var TDSNode=_createNode(WtfGlobal.getLocaleText("acc.reports.india.TDS"), 'TDSCSTRepo', false, false, 'images/Statutory/gst.png');
        var GSTReportsNode=_createNode(WtfGlobal.getLocaleText("acc.india.GSTR.Report"), 'GSTRReports', false, false, 'images/Statutory/gst.png');
        var ServiceTaxNode=_createNode(WtfGlobal.getLocaleText("acc.reports.india.serviceTaxReports"), 'ServiceTaxRepo', false, false, 'images/Statutory/gst.png');
        if (Wtf.Countryid == Wtf.Country.INDIA) {
            if (Wtf.UserReporRole.URole.roleid == 1) {
                arrayTDSListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.reports.india.TDS.challan.report"), 'tdschallanreport', false, true, 'images/Statutory/gst-form-5.png'));
                arrayTDSListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.reports.india.TDS.payment.report"), 'tdspaymentreport', false, true, 'images/Statutory/gst-form-5.png'));
                arrayTDSListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.reports.india.TDS.Deductee.report"), 'tdsunknowndeducteereport', false, true, 'images/Statutory/gst-form-5.png'));
                arrayTDSListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.reports.india.TDS.pan.report"), 'tdspanreport', false, true, 'images/Statutory/gst-form-5.png'));
                arrayTDSListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.reports.india.TDS.notds.report"), 'tdsnotapplicablereport', false, true, 'images/Statutory/gst-form-5.png'));
                arrayServiceTaxListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.reports.india.serviceTaxReports.serviceTaxCreditRegister"), 'servicetaxcreditregisterreport', false, true, 'images/Statutory/gst-form-5.png'));
//            arrayListEntry.push(VATCSTNode);
                arrayListEntry.push(TDSNode);
                arrayListEntry.push(ServiceTaxNode);
            } else {
                /*
                 * This will be executed when logged in with other users i.e other than company admin
                 * if privileges are set then only reports will be shown to user 
                 */
                var i = 0;
                for (i = 0; i < Wtf.UserReportPerm.length; i++) {
                    if (Wtf.UserReportPerm[i] == 'TDSChallanControl') {
                        arrayTDSListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.reports.india.TDS.challan.report"), 'tdschallanreport', false, true, 'images/Statutory/gst-form-5.png'));
                        continue;
                    }
                    if (Wtf.UserReportPerm[i] == 'Nature_of_payment_wise_Report') {
                        arrayTDSListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.reports.india.TDS.payment.report"), 'tdspaymentreport', false, true, 'images/Statutory/gst-form-5.png'));
                        continue;
                    }
                    if (Wtf.UserReportPerm[i] == 'Unknown_Deductee_Type_Report') {
                        arrayTDSListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.reports.india.TDS.Deductee.report"), 'tdsunknowndeducteereport', false, true, 'images/Statutory/gst-form-5.png'));
                        continue;
                    }
                    if (Wtf.UserReportPerm[i] == 'PAN_not_available_report') {
                        arrayTDSListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.reports.india.TDS.pan.report"), 'tdspanreport', false, true, 'images/Statutory/gst-form-5.png'));
                        continue;
                    }
                    if (Wtf.UserReportPerm[i] == 'TDS_Not_Deducted_Report') {
                        arrayTDSListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.reports.india.TDS.notds.report"), 'tdsnotapplicablereport', false, true, 'images/Statutory/gst-form-5.png'));
                        continue;
                    }
                }
                if (arrayTDSListEntry.length > 0) {
                    arrayListEntry.push(TDSNode);
                }
            }
        }
        if(Wtf.Countryid==Wtf.Country.INDIA){
            // GSTR Reports node
            arrayListEntry.push(GSTReportsNode);
            // GSTR Reports List
            GSTRListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.gstr1summary.setting"), '23', false, false, 'images/Masters/custom-fields.png'));
            GSTRListEntry.push(_createNode("GSTR2-Match and Reconcile", '29', false, false, 'images/Masters/custom-fields.png'));
            GSTRListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.gstr2summary.setting"), '24', false, false, 'images/Masters/custom-fields.png'));
            GSTRListEntry.push(_createNode("GSTR2A", '30', false, false, 'images/Masters/custom-fields.png'));
            GSTRListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.GSTR3B.Title"), '25', false, false, 'images/Masters/custom-fields.png'));
            GSTRListEntry.push(_createNode("GSTR3B", '31', false, false, 'images/Masters/custom-fields.png'));
            GSTRListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.gst.ismatchreport"), '28', false, false, 'images/Masters/custom-fields.png'));
        }
        ////////////////////////////////////// Statutory Reports for INDIAN Company ////////////////////////////////////////////////////////////
        if (Wtf.UserReporRole.URole.roleid == Wtf.ADMIN_ROLE_ID) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.gsttapdetailed"), 'gsttapdetailedview', false, true, 'images/Statutory/breakup-of-gst-boxes.png'));
        }
        else {
            for (var userpermcount = 0; userpermcount < Wtf.UserReportPerm.length; userpermcount++)
            {
                if (Wtf.UserReportPerm[userpermcount] == Wtf.ReportListName.Gstreport)
                {
                    arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.gsttapdetailed"), 'gsttapdetailedview', false, true, 'images/Statutory/breakup-of-gst-boxes.png'));
                    break;
                }
            }
        }
        /**
         * VAT Summary Report for Philippines Country
         * ERP-41508
         */
        if(Wtf.Countryid == Wtf.Country.PHILIPPINES && false){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.statutoryPanel.philippines.vat.summary.report"), 'philippinesVATSummaryReport', false, true, 'images/Statutory/consolidation-report-icon.png'));
            arrayListEntry.push(reliefReportNode);
            //Created Child node purchase relief report and push into PHPRListEntry
            PHPRListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.php.reliefreport.purchase"), 'PHPPurchasesReliefSummaryReport', false, true, 'images/Account_Payable/Reports.png'));
            arrayTDSListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.reports.india.TDS.payment.report"), 'tdspaymentreport', false, true, 'images/Statutory/gst-form-5.png'));
            arrayListEntry.push(TDSNode);
            PHPRListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.php.reliefreport.sales"), 'PHPSalesReliefSummaryReport', false, true, 'images/Account_Payable/Reports.png'))
        }
        /**
         * Output VAT Report for INDONESIA
         */
        if (Wtf.Countryid == Wtf.Country.INDONESIA) {
            if (Wtf.UserReporRole.URole.roleid == Wtf.ADMIN_ROLE_ID) {
                arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.statutoryPanel.indonesia.vat.out.report"), 'indonesiaVATOutReport', false, true, 'images/Statutory/consolidation-report-icon.png'));
            } else {
                for (var userpermcount = 0; userpermcount < Wtf.UserReportPerm.length; userpermcount++) {
                    if (Wtf.UserReportPerm[userpermcount] == Wtf.ReportListName.VATOutReportIndonesia) {
                        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.statutoryPanel.indonesia.vat.out.report"), 'indonesiaVATOutReport', false, true, 'images/Statutory/consolidation-report-icon.png'));
                        break;
                    }
                }
            }
        }
        
        
        this.setRootNode(root1);
        reportsNode.appendChild(arrayListEntry);
        gstNode.appendChild(arrayGSTListEntry);
//        exciseNode.appendChild(arrayExciseListEntry);
//        VATCSTNode.appendChild(arrayVATCSTListEntry);
        TDSNode.appendChild(arrayTDSListEntry);
        GSTReportsNode.appendChild(GSTRListEntry); // INDIA GSTR Reports
        ServiceTaxNode.appendChild(arrayServiceTaxListEntry);
        reliefReportNode.appendChild(PHPRListEntry);//append all child into parent node i.e Relief Report
        root1.appendChild(arrayList);
        /**
         * Append Custom Build report in Statutory panel for INDONESIA country
         */
        if (Wtf.Countryid == Wtf.Country.INDONESIA) {
            this.loadReportBuilderList();
        }
    },
    /**
     * Load Custom Builder report list
     * @returns {undefined}
     */
    loadReportBuilderList: function () {
        try {
            Wtf.Ajax.request({
                url: 'ACCCreateCustomReport/getCustomReportList.do',
                params: {},
                success: function (res, req) {
                    var result = eval('(' + res.responseText + ')');
                    this.reportBuildObj = result;
                    this.createReportBuilderList();
                },
                failure: function (res, req) {
                    this.reportBuildObj = {};
                },
                scope: this
            });
        } catch (e) {
            clog(e);
        }
    },
    /**
     * Create Custom Report Node's under "Custom Reports" main node 
     * @returns {undefined}
     */
    createReportBuilderList: function () {
        try {
            var customReportBuilderList = new Array();
            if (this.reportBuildObj && this.reportBuildObj.data) {
                var reportData = this.reportBuildObj.data;
                for (var reportCount = 0; reportCount < reportData.length; reportCount++) {
                    var reportDataObject = reportData[reportCount];
                    if (reportDataObject && reportDataObject.isShowasQuickLinks == "T") {
                        var InputTaxAdjustment = this._createCustomBuilderReportNode(reportDataObject.name, reportDataObject.id, false, true, 'images/Account_Payable/Reports.png', reportDataObject);
                        customReportBuilderList.push(InputTaxAdjustment);
                    }
                }
                if (customReportBuilderList && customReportBuilderList.length > 0) {
                    this.root.lastChild.appendChild(customReportBuilderList);
                }
            }
        } catch (e) {
            clog(e);
        }
    },
    /**
     * Create Node function added for Custom Report Builder
     * @param {type} nodeText
     * @param {type} nodeID
     * @param {type} canDrag
     * @param {type} isLeaf
     * @param {type} nodeIcon
     * @param {type} reportData
     * @returns {Wtf.tree.TreeNode}
     */
    _createCustomBuilderReportNode: function (nodeText, nodeID, canDrag, isLeaf, nodeIcon, reportData) {
        /**
         * Create Tree Node
         */
        var treeNode = new Wtf.tree.TreeNode({
            text: nodeText,
            id: nodeID,
            cls: 'paddingclass',
            allowDrag: canDrag,
            leaf: isLeaf,
            icon: nodeIcon,
            reportData: reportData
        });
        treeNode.on("click", function (node) {
            if (node && node.attributes && node.attributes && node.attributes.reportData) {
                /**
                 * Load Custom Builder and then open report after list load successfully
                 */
                loadCreateCustomReportPerm(node.id);
            } else { // If Root Node then don't show messages for other report no id found then show alert
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"),
                    msg: "Report Details Not Present",
                    buttons: Wtf.MessageBox.OK,
                    animEl: 'mb9',
                    icon: Wtf.MessageBox.INFO
                });
            }
        }, this);
        return treeNode;
    }
});
