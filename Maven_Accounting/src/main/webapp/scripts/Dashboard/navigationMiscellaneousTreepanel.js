Wtf.MiscellaneousTree = function(config) {
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

    Wtf.MiscellaneousTree.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.MiscellaneousTree, Wtf.tree.TreePanel, {
    autoWidth: true,
    autoHeight: true,
    rootVisible: false,
    id: 'folderview',
    border: false,
    autoScroll: true,
    animate: Wtf.enableFx,
    enableDD: false,
    hlDrop: Wtf.enableFx,
    initComponent: function() {
        Wtf.MiscellaneousTree.superclass.initComponent.call(this);
        treeObj = this;

        function _openFunction(node) {
            switch (node.id) {
                case "1":
                    PaymentMethod();
                    break;
                case "2":
                    callCreditTerm();
                    break;
                case "3":
                    callUOM();
                    break;
                case "4":
                    callTax();
                    break;
                case "5":
                    addTemplete();
                    break;
                case "6":
                    addPDFFooter();
                    break;
//                case "7":
//                    callSalesCommission();
//                    break;
                    // case "8":
                    //     saveCustomizedAgedDuration();
                    //     break;
                    //  case "9":
                    //     setApprovelRules();
                    //      break;
                case "10":
                    templates();
                    break;
                    //  case "11":
                    //      addDeleteNoteType();
                    //     break;
                case "12":
                    callAccountRevaluationWindow();
                    break;
                case "13":
                    callDemoTransactionsImportWin();
                    break;
                case "14":
                    callIBGEntryReport();
                    break;
                case "15":
                    chequeLayoutSetup();
                    break;
                case "16":
                    saveWIPAndCPAccountSettings();
                    break;
                case "17":
                    callUOMMaterType();
                    break;
                case "18":
                    syncAllFromLMS();
                    break;
                case "19":
                    callSalesCommissionSchemaMaster();
                    break;
                case "22":
                callDiscountMasterSalesWindow();        //calls discount master window
                    break;
                /*case "20":
                    callGSTRuleSetup(undefined,undefined);
                    break;
                case "21":
                    GSTR1Report();
                    break;
                case "23":
                    var gstrreporttype=1;
                    callGSTR1Summary(gstrreporttype);
                    break;
                case "24":
                    var gstrreporttype=2;
                    callGSTR1Summary(gstrreporttype);
                    break;
                case "25":
                    callGSTComputationReport();
                    break;
                case "26":
                    callGSTInputRuleReport();
                    break;
                case "27":
                    callGSTOutputRuleReport();
                    break;
                case "28":
                    var gstrreporttype = 3;
                    callGSTRMisMatchSummary(gstrreporttype);
                    break;
                case "29":
                    callGSTR2MatchAndReconcile();
                    break;
                case "30":
                    callGSTR2AComparison();
                    break;
                case "31":
                    callGSTR3BSummaryReport();
                    break;*/
            }
        }
        function _createNode(nodeText, nodeID, canDrag, isLeaf, nodeIcon) {
            var treeNode = new Wtf.tree.TreeNode({
                text: nodeText,
                id: nodeID,
                cls: 'paddingclass',
                allowDrag: canDrag,
                leaf: isLeaf,
                icon: nodeIcon
            });
            treeNode.on("click", function(node) {
                _openFunction(node);
            }, this);
            return treeNode;
        }

        var root1 = new Wtf.tree.AsyncTreeNode({
            text: '',
            expanded: true
        });
        var arrayList = new Array();
        var arrayJobWorkReciever = new Array();// Job Work Reciever
        
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.view)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.pmList.gridPaymentMethod"), '1', false, true, 'images/accounting_image/Payment-Method.gif'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.creditterm, Wtf.Perm.creditterm.view)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.masterConfig.payTerm"), '2', false, true, 'images/accounting_image/Credit-Term.gif'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.uom, Wtf.Perm.uom.view)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.masterConfig.uom"), '3', false, true, 'images/accounting_image/Unit-of-measure.gif'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.viewuom)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.schema.UOMSchema"), '17', false, true, 'images/Masters/UOM-Schema-Definition.png'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.salescommission)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.sales.salescommission.salesCommissionSchema.master"), '19', false, true, 'images/Masters/GST-Codes.png'));
        }
        
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.view) && (WtfGlobal.getGSTType()==Wtf.GSTType.SINGAPORETYPE || !Wtf.isNewGSTOnly)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.masterConfig.taxes"), '4', false, true, 'images/accounting_image/tax.gif'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.template)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.masterConfig.tempLogo"), '5', false, true, 'images/createuser.png'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.pdftemplate)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.masterConfig.pdffooterheader"), '6', false, true, 'images/createuser.png'));
        }
//        if (!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.salescommission)) {
//            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.masterConfig.salesCommission"), '7', false, true, 'images/createuser.png'));
//        }
        // arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.interval.summary.aged"), '8', false, true, 'images/accounting_image/calendar.jpg'));
        //   arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.DocumentApprovalRules"), '9', false, true, 'images/accounting_image/calendar.jpg'));
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.documenttemplate)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.DocumentTemplates"), '10', false, true, 'images/accounting_image/calendar.jpg'));
        }
        // arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.invoice.gridNoteType"), '11', false, true, 'images/accounting_image/Unit-of-measure.gif'));
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.accountreval)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.AccountsRe-evaluation"), '12', false, true, 'images/accounting_image/account-revaluation-icon.png'));
        }
        if (Wtf.show_just_commodity_software_import_link) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.ImportJustCommodityTransactions"), '13', false, true, 'images/accounting_image/import-just-commodity-icon.png'));
        }
//           if((Wtf.account.companyAccountPref.activateIBG)
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.ibg) && Wtf.Countryid != Wtf.Country.INDIA) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.IBG.tabtitel"), '14', false, true, 'images/accounting_image/ibg-icon.png'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.checklayout)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.masterConfig.chequeLayoutSetup"), '15', false, true, 'images/accounting_image/cheque-layout-setup-icon.png'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.setwipandcp)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.wipcpaccount.setting"), '16', false, true, 'images/Masters/custom-fields.png'));
        }
        if (Wtf.syncAllFromLMSFlag && Wtf.UserReporRole.URole.roleid == 1) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.SyncAllFromLMS"), '18', false, true, 'images/Masters/Sync.png'));
        }
        /*if (Wtf.Countryid == Wtf.Country.INDIA ||Wtf.Countryid == Wtf.Country.US ) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("GST Rule Setup"), '20', false, false, 'images/Masters/custom-fields.png'));
            if(Wtf.Countryid == Wtf.Country.INDIA){
               // arrayList.push(_createNode(WtfGlobal.getLocaleText("GSTR1"), '21', false, false, 'images/Masters/custom-fields.png'));
            }
        }
         if(Wtf.Countryid == Wtf.Country.INDIA){
                arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.gstr1summary.setting"), '23', false, false, 'images/Masters/custom-fields.png'));
            }
        if (Wtf.Countryid == Wtf.Country.INDIA) {
            arrayList.push(_createNode("GSTR2A- Match and Reconcile", '29', false, false, 'images/Masters/custom-fields.png'));
        }
        if (Wtf.Countryid == Wtf.Country.INDIA) {
            arrayList.push(_createNode("GSTR2A", '30', false, false, 'images/Masters/custom-fields.png'));
        }
            if(Wtf.Countryid == Wtf.Country.INDIA){
                arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.gstr2summary.setting"), '24', false, false, 'images/Masters/custom-fields.png'));
            }
        if (Wtf.Countryid == Wtf.Country.INDIA) {
            arrayList.push(_createNode("GSTR3B", '31', false, false, 'images/Masters/custom-fields.png'));
        }
        if (Wtf.Countryid == Wtf.Country.INDIA) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.gst.ismatchreport"), '28', false, false, 'images/Masters/custom-fields.png'));
        }
        if (Wtf.Countryid == Wtf.Country.INDIA) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.GSTR3B.Title"), '25', false, false, 'images/Masters/custom-fields.png'));
        }*/
//        if (CompanyPreferenceChecks.discountMaster()) {
            if (!WtfGlobal.EnableDisable(Wtf.UPerm.discountMaster, Wtf.Perm.discountMaster.addDiscountMaster)) {
                arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.masterConfig.discountMasterSales"), '22', false, true, 'images/Masters/documents-designer.png'));
            }
//        }
        /*if ((Wtf.Countryid == Wtf.Country.INDIA || Wtf.Countryid == Wtf.Country.US) && Wtf.isNewGSTOnly && Wtf.UserReporRole.URole.roleid == 1) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.gstrr.input.tabtitle"), '26', false, false, 'images/Masters/custom-fields.png'));
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.gstrr.output.tabtitle"), '27', false, false, 'images/Masters/custom-fields.png'));
        } */   
        
        
        this.setRootNode(root1);
        root1.appendChild(arrayList);
    }
});
