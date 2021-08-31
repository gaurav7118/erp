Wtf.MasterTree = function(config){
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

    Wtf.MasterTree.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.MasterTree, Wtf.tree.TreePanel, {
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
        Wtf.MasterTree.superclass.initComponent.call(this);
        treeObj = this;

        function _openFunction(node){
            switch (node.id) {
                case "5":
                    callMasterConfiguration("sales");
                    break;
                case "411":
                    callCOA();
                    break;
                case "412":
                    callMasterCustomLayoutGrid(0);
                    break;
                case "413":
                    callMasterCustomLayoutGrid(1);
                    break;
                case "414":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case '415':
                    callMasterCustomLayoutGrid(3);
                    break;
                case "611":
                    callCustomerDetails(true);
                    break;
                case "612":
                    callVendorDetails(true);
                    break;
//                
//                case "6111":
//                    callCustomerDetails(null,true,true);
//                    break;
//                case "6112":
//                    callCustomerDetails(true);
//                    break;
//                case "6121":
//                    callVendorDetails(null,true,true);
//                    break;
//                case "6122":
//                    callVendorDetails(true);
//                    break;               
                case "613":
                    callNewCustomerByCategoryReport();
                    break;
                case "614":
                    callNewVendorByCategoryReport();
                    break;
                case "711":
                    callProductDetails();
                    break;
                case "712":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "713":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "714":
                    callNewProductByCategoryReport();
                    break;
                case "715":
                    callMasterPricelistWindow("Customer", false, undefined);
                    break;
                case "716":
                    callMasterPricelistWindow("Vendor", false, undefined);
                    break;
                case "717":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "718":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "719":
                    callMasterPricelistWindow("", false, undefined); // call the function for mass update of price
                    break;
                case "811":
                    callOnlyDimensionConfiguration(true,false,"masterconfigforsalesp");
                    break;
                case "812":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "813":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "911":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "912":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "913":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "914":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "1011":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "1012":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "11":
                    callCostCenter();
                    break;
                case "8":
                    callOnlyDimensionConfiguration(true,false,"masterconfigforsalesp");
                    break;
                case "12":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "13":
                    callCurrencyExchangeWindow();
                    break;
                case "1411":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "1412":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "1511":
                    masterInvoiceTerms();
                    break;
                case "1512":
                    masterPurchaseTerms();
                    break;
                case "1513":
                    callTax();
                    break;
                    //case "16":
                    //     Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    //    break;     
                case "17":
                    callOnlyDimensionConfiguration(false,true,"masterconfigforuserfield");
                    break;
                case "18":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "19":
                    callCustomDesigner();
                    break;
                case "20":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "21":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "22":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "23":
                    callMasterConfiguration();
                    break;
                case "24":
                    callStoreMaster();
                    break;
                case "25":
                    callMachineMaster();
                    break;
                case "26":
                    callProcessMaster();
                    break;
                case "27":
                    callUOM();
                    break;
                case "28":
                    callLocationMaster();
                    break;
                case "30":
                    callInspectionTemplateMaster();
                    break;
//                case "6131":
//                  setApprovelRules();
//                  break;
                case "6132":
                    setDOApprovelRules();
                    break;
                case "6141":
                    inventorySetup();
                    break;
                case "6142":
                    callPackageWindow();
                    break;
                case "6143":
                    callcustomerwarehouses();
                    break;
                case "6144":
                    callPricingBandMaster();
                    break;
                case "6145":
                    callPriceListVolumeDiscount();
                    break;
                case "6146":
                    callBudgetingWin();
                    break;
                case "6147":
                    consignmentApprovalRequest();
                    break;
                case "6148":
                    consignmentQARequest();
                    break;
                case "6149":  // Unit Excise Window 
                    callExciseUnitWin();
                    break;
                case "31":
                    callTaxCurrencyExchangeWindow();
                    break;
                case "32"://TDS Master Rates, only in case of India
                    callTDSMasterRates("tdsmasterrates");
                    break;
                case "266":
                     /**
                     * ERM - 294
                     * Disable this button when Avalara Integration is on
                     */
                    if (Wtf.account.companyAccountPref.avalaraIntegration) {
                        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.integration.invoiceGSTInputRuleReportDisabledForAvalaraMsg"));
                    }
                    else{
                        callGSTInputRuleReport();
                    }
                    break;
                case "277":
                     /**
                     * ERM - 294
                     * Disable this button when Avalara Integration is on
                     */
                    if (Wtf.account.companyAccountPref.avalaraIntegration) {
                        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.integration.invoiceGSTOutputRuleReportDisabledForAvalaraMsg"));
                    }else{
                        callGSTOutputRuleReport();
                    }
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
        var arrayListCOA = new Array();
        var arrayListCustVen = new Array();
        var arrayListProduct = new Array();
        var arrayListDclass = new Array();
        var arrayListinterestRates = new Array();
        var arrayListaccConf = new Array();
        var arrayListclosingRates = new Array();
        var arrayListinvoiceTerms = new Array();
        var arrayListGSTTaxMaster = new Array(); // GST US and INDIA
        var arrayListCust = new Array();
        var arrayListVen = new Array();
        var arrayListApprovalRules = new Array();//  For Approving Rules

        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.view)) {
            arrayListCOA.push(_createNode(WtfGlobal.getLocaleText("acc.master.invoiceterm.glaccount"), '411', false, true, 'images/Masters/General-Ledger.png'));
        }
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view)) {
//            arrayListCOA.push(_createNode(WtfGlobal.getLocaleText("acc.field.Profit&LossLayout"), '412', false, true, 'images/Masters/Profit-&-Loss-layout.png'));
//            arrayListCOA.push(_createNode(WtfGlobal.getLocaleText("acc.field.BalanceSheetLayout"), '413', false, true, 'images/Masters/Balance-Sheet-layout.png'));
//        }
//        arrayListCOA.push(_createNode('Class Flow Layout', '414', false, true, 'images/Masters/Cash-Flow-Layout.png'));
        var COANode=_createNode(WtfGlobal.getLocaleText("acc.coa.tabTitle"), '4', false, false, 'images/Masters/chart-of-accounts.png');
        if(arrayListCOA.length!=0){
            arrayList.push(COANode);
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.Salesmen/Agents"), '5', false, true, 'images/Masters/salesmen-agents.png'));
        }

        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.viewcoa)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.coa.tabTitle"), '411', false, true, 'images/Masters/chart-of-account.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.customer, Wtf.Perm.customer.view)) {
            var custNode=_createNode(WtfGlobal.getLocaleText("acc.nee.42"), '611', false, false, 'images/Masters/Customers.png');
            arrayListCustVen.push(custNode);
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendor, Wtf.Perm.vendor.view)) {
            var venNode=_createNode(WtfGlobal.getLocaleText("acc.nee.41"), '612', false, false, 'images/Masters/Vendors.png');
            arrayListCustVen.push(venNode);
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.customer, Wtf.Perm.customer.view)) {
            arrayListCustVen.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomersGrouping"), '613', false, true, 'images/Masters/Customers-Grouping.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendor, Wtf.Perm.vendor.view)) {
            arrayListCustVen.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorsGrouping"), '614', false, true, 'images/Masters/Vendors-Grouping.png'));
        }
        var custVenNode=_createNode(WtfGlobal.getLocaleText("acc.field.Customers/Vendors"), '6', false, false, 'images/Masters/customers-vendor.png');
        if(arrayListCustVen.length!=0) {
            arrayList.push(custVenNode);
        }

        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.view)) {
            arrayListProduct.push(_createNode(WtfGlobal.getLocaleText("acc.field.Products"), '711', false, true, 'images/Masters/Products.png'));
            arrayListProduct.push(_createNode(WtfGlobal.getLocaleText("acc.field.Grouping"), '714', false, true, 'images/Masters/Grouping.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.view)) {
            arrayListProduct.push(_createNode(WtfGlobal.getLocaleText("General Price Rate"), '719', false, true, 'images/Masters/Products.png'));
        }
//        arrayListProduct.push(_createNode('Product Matrix Wizard', '712', false, true, 'images/Masters/Product-Matrix-Wizard.png'));
//        arrayListProduct.push(_createNode('Product Matrix Order Pad', '713', false, true, 'images/Masters/Product-Matrix-Order-Pad.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.viewpricelist)) {
            arrayListProduct.push(_createNode(WtfGlobal.getLocaleText("acc.field.SpecialRatesCustomers"), '715', false, true, 'images/Masters/Special-Rates-Customers.png'));
            arrayListProduct.push(_createNode(WtfGlobal.getLocaleText("acc.field.SpecialRatesVendors"), '716', false, true, 'images/Masters/Special-Rates-Vendors.png'));
        }
         if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.pricelistband)) {
            arrayListProduct.push(_createNode(WtfGlobal.getLocaleText("acc.field.pricingBands"), '6144', false, true, 'images/Masters/documents-designer.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.pricelistvolumediscount)) {
            arrayListProduct.push(_createNode(WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount"), '6145', false, true, 'images/Masters/documents-designer.png'));
        }
//        arrayListProduct.push(_createNode('Warehouse', '717', false, true, 'images/Masters/Warehouse.png'));
//        arrayListProduct.push(_createNode('UOM - Schema Defination ', '718', false, true, 'images/Masters/UOM-Schema-Definition.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventorymasters, Wtf.Perm.inventorymasters.storemaster)) {
            arrayListProduct.push(_createNode(WtfGlobal.getLocaleText("acc.lp.storemaster"), '24', false, true, 'images/inventory/warehouse-master-icon.png'));
        }
        // arrayListProduct.push(_createNode('Warehouse Master', '24', false, true, 'images/inventory/warehouse-master-icon.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventorymasters, Wtf.Perm.inventorymasters.locationmaster)) {
            arrayListProduct.push(_createNode(WtfGlobal.getLocaleText("acc.lp.locationmaster"), '28', false, true, 'images/inventory/warehouse-master-icon.png'));
        }
        var productNode=_createNode(WtfGlobal.getLocaleText("acc.up.49"), '7', false, false, 'images/Masters/items-products.png');
        if(arrayListProduct.length!=0) {
            arrayList.push(productNode);
        }
        var DclassNode=_createNode(WtfGlobal.getLocaleText("acc.inv.fieldset.title"), '8', false, false, 'images/Masters/Document-Class.png');
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view)) {
            arrayList.push(DclassNode);
        }
        var interestRatesNode=_createNode(WtfGlobal.getLocaleText("acc.field.InterestRates"), '9', false, false, 'images/Masters/interest-rates.png');
//        arrayList.push(interestRatesNode);
        var accConf=_createNode(WtfGlobal.getLocaleText("acc.field.AccountConfirmation"), '10', false, false, 'images/Masters/account-confirmation.png');
//        arrayList.push(accConf);
        if(Wtf.UserReporRole.URole.roleid==1){
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.CostCenters"), '11', false, false, 'images/Masters/cost-centers.png'));
        } else{
            for(var userpermcount=0;userpermcount<Wtf.UserReportPerm.length;userpermcount++)
            {
                if(Wtf.UserReportPerm[userpermcount]==Wtf.ReportListName.CostCenterReport){
                    arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.CostCenters"), '11', false, false, 'images/Masters/cost-centers.png'));
                }
            }
        }

//        arrayList.push(_createNode('Cost Centers Grouping', '12', false, false, 'images/Masters/cost-centers-grouping.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.currencyexchange, Wtf.Perm.currencyexchange.view)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.CurrencyRates"), '13', false, true, 'images/Masters/currency-rates.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.currencyexchange, Wtf.Perm.currencyexchange.view) && Wtf.Countryid == Wtf.Country.INDONESIA) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.CurrencyRatesForTax"), '31', false, true, 'images/Masters/currency-rates.png'));
        }
        //TDS Master Rates, only in case of India
        if( Wtf.Countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.TDSRates"), '32', false, true, 'images/Masters/currency-rates.png'));
        }
        var closingRatesNode=_createNode(WtfGlobal.getLocaleText("acc.field.Closing/ProductionRates"), '14', false, false, 'images/Masters/closing-production-rates.png');
//        arrayList.push(closingRatesNode);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view)) {
            arrayListinvoiceTerms.push(_createNode(WtfGlobal.getLocaleText("acc.field.Sales"), '1511', false, true, 'images/Masters/Sales.png'));
            arrayListinvoiceTerms.push(_createNode(WtfGlobal.getLocaleText("acc.field.Purchase"), '1512', false, true, 'images/Masters/Purchase.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.view) && Wtf.Countryid != Wtf.Country.INDIA && Wtf.Countryid != Wtf.Country.US) {
            arrayListinvoiceTerms.push(_createNode(WtfGlobal.getLocaleText("acc.field.GSTCodes") , '1513', false, true, 'images/Masters/GST-Codes.png'));
        }
        var invoiceTermsNode=_createNode(WtfGlobal.getLocaleText("acc.field.InvoiceTerms"), '15', false, false, 'images/Masters/invoice-terms.png');
        if(arrayListinvoiceTerms.length!=0) {
            arrayList.push(invoiceTermsNode);
        }
        var GSTTaxMasterNode=_createNode(WtfGlobal.getLocaleText("acc.GSTtax.master"), 'GSTtaxMaster', false, false, 'images/Masters/GST-Codes.png');
        if((Wtf.Countryid==Wtf.Country.INDIA || Wtf.Countryid==Wtf.Country.US )&& Wtf.isNewGSTOnly){
            arrayList.push(GSTTaxMasterNode);
            arrayListGSTTaxMaster.push(_createNode(WtfGlobal.getLocaleText("acc.gstrr.input.tabtitle"), '266', false, false, 'images/Masters/custom-fields.png'));
            arrayListGSTTaxMaster.push(_createNode(WtfGlobal.getLocaleText("acc.gstrr.output.tabtitle"), '277', false, false, 'images/Masters/custom-fields.png'));
        }
//        arrayList.push(_createNode('Opening Balances', '16', false, true, 'images/Masters/opening-balances.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.UserDefinedFields"), '17', false, true, 'images/Masters/custom-fields.png'));
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.DocumentDesigner"), '19', false, true, 'images/Masters/documents-designer.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.MasterConfiguration"), '23', false, true, 'images/Masters/custom-fields.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventorymasters, Wtf.Perm.inventorymasters.inspectiontemplate)){  
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.lp.inspectiontemplate"), '30', false, true, 'images/Masters/custom-fields.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.Profit&LossLayout"), '412', false, true, 'images/Masters/Profit-&-Loss-layout.png'));
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.BalanceSheetLayout"), '413', false, true, 'images/Masters/Balance-Sheet-layout.png'));
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.CashFlowStatementLayout"), '415', false, true, 'images/Masters/Cash-Flow-Layout.png'));
        }
//        arrayList.push(_createNode('Copy Codes from another Company', '18', false, true, 'images/Masters/copy-code.png'));
//        arrayList.push(_createNode('Report Writer', '20', false, true, 'images/Masters/report-writer.png'));
//        arrayList.push(_createNode('Report Scheduler', '21', false, true, 'images/Masters/report-scheduler.png'));
//        arrayList.push(_createNode('Alert Scheduler', '22', false, true, 'images/Masters/alert-scheduler.png'));

        arrayListDclass.push(_createNode(WtfGlobal.getLocaleText("acc.field.Codes"), '811', false, true, 'images/Masters/code.png'));
        arrayListDclass.push(_createNode(WtfGlobal.getLocaleText("acc.field.Picture"), '812', false, true, 'images/Masters/Picture.png'));
        arrayListDclass.push(_createNode(WtfGlobal.getLocaleText("acc.field.Filters"), '813', false, true, 'images/Masters/Filter.png'));

        arrayListinterestRates.push(_createNode(WtfGlobal.getLocaleText("acc.field.GeneralLedgerInterestRates"), '911', false, true, 'images/Masters/General-Ledger-Interest-Rat.png'));
        arrayListinterestRates.push(_createNode(WtfGlobal.getLocaleText("acc.field.SubLedgerInterestRates"), '912', false, true, 'images/Masters/Sub-Ledger-Interest-Rates.png'));
        arrayListinterestRates.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomerLedgerInterestRates"), '913', false, true, 'images/Masters/Customer-Interest-Rates.png'));
        arrayListinterestRates.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorInterestRates"), '914', false, true, 'images/Masters/Vendor-Interest-Rates.png'));

        arrayListaccConf.push(_createNode(WtfGlobal.getLocaleText("acc.field.GeneralLedgerAccountConfirmation"), '1011', false, true, 'images/Masters/General-Ledger-Account-Conf.png'));
        arrayListaccConf.push(_createNode(WtfGlobal.getLocaleText("acc.field.SubLedgerAccountConfirmation"), '1012', false, true, 'images/Masters/Sub-Ledger-Account-Confirma.png'));

        arrayListclosingRates.push(_createNode(WtfGlobal.getLocaleText("acc.field.Closing"), '1411', false, true, 'images/Masters/Closing.png'));
        arrayListclosingRates.push(_createNode(WtfGlobal.getLocaleText("acc.field.Production"), '1412', false, true, 'images/Masters/Producation.png'));

        arrayListCust.push(_createNode(WtfGlobal.getLocaleText("acc.common.add"), '6111', false, true, 'images/Masters/Entry.png'));
        arrayListCust.push(_createNode(WtfGlobal.getLocaleText("acc.common.view"), '6112', false, true, 'images/Masters/report-writer.png'));

        arrayListVen.push(_createNode(WtfGlobal.getLocaleText("acc.common.add"), '6121', false, true, 'images/Masters/Entry.png'));
        arrayListVen.push(_createNode(WtfGlobal.getLocaleText("acc.common.view"), '6122', false, true, 'images/Masters/report-writer.png'));

//        arrayListApprovalRules.push(_createNode(WtfGlobal.getLocaleText("acc.field.DocumentApprovalRules"), '6131', false, true, 'images/Masters/document-icon.png'));
        arrayListApprovalRules.push(_createNode(WtfGlobal.getLocaleText("acc.masterconfig.MultiLevelApprovalRules"), '6132', false, true, 'images/Masters/do-gr-ge-icon.png'));
        arrayListApprovalRules.push(_createNode(WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApproval"), '6147', false, true, 'images/Masters/do-gr-ge-icon.png'));
        arrayListApprovalRules.push(_createNode(WtfGlobal.getLocaleText("acc.field.QAApproval"), '6148', false, true, 'images/Masters/do-gr-ge-icon.png'));
        var approvalNode=_createNode(WtfGlobal.getLocaleText("acc.field.ApprovalRules"), '16', false, false, 'images/Masters/approval-rules-icon.png');
        if(arrayListApprovalRules.length!=0) {
           if(!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.viewapprovalrule)) {  
                arrayList.push(approvalNode);
            }
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventorymasters, Wtf.Perm.inventorymasters.inventrysetup)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.masterConfig.inventorysetup"), '6141', false, true, 'images/Masters/inventory-setup-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventorymasters, Wtf.Perm.inventorymasters.packagemaster)) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("erp.packagemaster"), '6142', false, true, 'images/Masters/inventory-setup-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventorymasters, Wtf.Perm.inventorymasters.customerwarehousemaster) && !Wtf.defaultReferralKeyflag) {
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.WarehousesMaster"), '6143', false, true, 'images/Masters/documents-designer.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventorymasters, Wtf.Perm.inventorymasters.budgeting) && !Wtf.defaultReferralKeyflag){  
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.budgeting"), '6146', false, true, 'images/Masters/budgeting.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view) && Wtf.Countryid==Wtf.Country.INDIA && Wtf.isExciseApplicable){  // Unit Excise Window 
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.paymentEntry.ExciseUnit"), '6149', false, true, 'images/Masters/documents-designer.png'));
        }

//          arrayList.push(_createNode('Warehouse Master', '24', false, true, 'images/inventory/warehouse-master-icon.png'));
        //          arrayList.push(_createNode('Location Master', '28', false, true, 'images/inventory/warehouse-master-icon.png'));
//        arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.machinemaster.title"), '25', false, true, 'images/inventory/machine-master-icon.png'));
//        arrayList.push(_createNode('Proccess Master', '26', false, true, 'images/inventory/process-master-icon.png'));
//        arrayList.push(_createNode('UoM Master', '27', false, true, 'images/accounting_image/Unit-of-measure.gif'));

        //    venNode.appendChild(arrayListVen);
        //  custNode.appendChild(arrayListCust);
        approvalNode.appendChild(arrayListApprovalRules);
        invoiceTermsNode.appendChild(arrayListinvoiceTerms);
        GSTTaxMasterNode.appendChild(arrayListGSTTaxMaster);
        closingRatesNode.appendChild(arrayListclosingRates);
        accConf.appendChild(arrayListaccConf);
        interestRatesNode.appendChild(arrayListinterestRates);
//        DclassNode.appendChild(arrayListDclass);
        productNode.appendChild(arrayListProduct);
        custVenNode.appendChild(arrayListCustVen);
        COANode.appendChild(arrayListCOA);
        this.setRootNode(root1);
        root1.appendChild(arrayList);
    }
});
