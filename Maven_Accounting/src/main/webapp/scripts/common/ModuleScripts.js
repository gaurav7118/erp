Wtf.ModuleScriptLoadedFlag = {
    consignment:false,
    assetslease:false,
    inventory:false,
    loan:false,
    setupwizard:false,
    companyprefsetting:false,
    calendar: false,
    dashboardUpdates:false,
    mrp:false
}

Wtf.ModuleScriptGr = {
    consignment : 'consignment',
    assetslease : 'assetslease',
    inventory : 'inventory',
    loan : 'loan',
    calendar: 'calendar',
    mrp:'mrp'
}
WtfModuleWiseScripts = {
    getConsignmentScripts : function() {
        if(isProdBuild) {
            return ['../../scripts/minifiedjs/consignment-ex.js?v=53_00']
        } else {
            return [
//                '../../scripts/mainscripts/ConsignmentRequestApproval.js',
//                '../../scripts/mainscripts/ConsignmentRequestPendingApproval.js',
//                '../../scripts/mainscripts/ConsignmentStockInvoice.js',
//                '../../scripts/mainscripts/ConsignmentStockInvoiceGrid.js',
//                '../../scripts/mainscripts/ConsignmentStockDeliveryOrder.js',
//                '../../scripts/mainscripts/ConsignmentStockDeliveryorderGrid.js',
//                '../../scripts/mainscripts/ConsignmentStockSalesReturn.js',
//                '../../scripts/mainscripts/ConsignmentStockSalesReturnGrid.js',
//                '../../scripts/mainscripts/ConsignmentLoan.js'
            ];
        }
    },
    
    getAssetsLeaseManagementScripts : function() {
        if(isProdBuild) {
            return ['../../scripts/minifiedjs/assetlease-ex.js?v=53_00']
        } else {
            return [
//                "../../scripts/mainscripts/FixedAssetSalesReturn.js",
//                "../../scripts/mainscripts/FixedAssetSalesReturnGrid.js",
//                "../../scripts/mainscripts/FixedAssetPurchaseRequisition.js",
//                "../../scripts/mainscripts/FixedAssetPurchaseRequisitionGrid.js",
//                "../../scripts/mainscripts/fixedassetopeningwindow.js",
//                "../../scripts/mainscripts/FixedAssetDetails.js",
//                "../../scripts/mainscripts/FixedAssetReport.js",
//                "../../scripts/mainscripts/FixedAssetGroup.js",
//                "../../scripts/mainscripts/FixedAssetDepreciation.js",
//                "../../scripts/mainscripts/FixedAssetInvoice.js",
//                "../../scripts/mainscripts/FixedAssetInvoiceGrid.js",
//                "../../scripts/mainscripts/FixedAssetDeliveryOrder.js",
//                "../../scripts/mainscripts/FixedAssetDeliveryOrderGrid.js"
            ];
        }
    },
    
    getInventoryScripts : function() {
        if(isProdBuild) {
            return ['../../scripts/minifiedjs/inventory-ex.js?v=53_00']
        } else {
            return [
//                "../../scripts/inventory/process/MachineMaster.js",
//                "../../scripts/inventory/process/ProcessMaster.js",
//                "../../scripts/inventory/cycleCount/WtfCycleCountModule.js",
//                "../../scripts/inventory/cycleCount/WtfCycleCountReport.js",
//                "../../scripts/inventory/cycleCount/automatedCycleCount.js",
//                "../../scripts/inventory/goodstransfer/GoodsTransferTab.js",
//                "../../scripts/inventory/goodstransfer/order.js",
//                "../../scripts/inventory/goodstransfer/interStoreStockTransferTab.js",
//                "../../scripts/inventory/goodstransfer/interStoreTransferRequest.js",
//                "../../scripts/inventory/goodstransfer/interstoreTransfer.js",
//                "../../scripts/inventory/goodstransfer/interLocationTransfer.js",
//                "../../scripts/inventory/goodstransfer/goodIssue.js",
//                "../../scripts/inventory/goodstransfer/GoodsOrderTransfer.js",
//                "../../scripts/inventory/stockadjustment/AddEditMaster.js",
//                "../../scripts/inventory/stockadjustment/makoutallTab.js",
//                "../../scripts/inventory/stockadjustment/markout.js",
//                "../../scripts/inventory/stockadjustment/markoutList.js",
//                "../../scripts/inventory/threshold/ProductThresholdForm.js",
//                "../../scripts/inventory/threshold/ThresholdReport.js",
//                "../../scripts/inventory/StockDetailFormWin.js",
//                "../../scripts/inventory/TransactionBalanceReport.js",
//                "../../scripts/inventory/StockMovementReport.js",
//                "../../scripts/inventory/StockSummaryReport.js",
//                "../../scripts/inventory/inventoryLevel.js",
//                "../../scripts/inventory/InventoryConfiguration.js",
//                "../../scripts/inventory/SequenceFormat.js",
//                "../../scripts/inventory/stockacknowledgement/Receipt.js",
//                "../../scripts/inventory/store/storeMasterGrid.js?v=7",
//                "../../scripts/inventory/store/exchangeRecordGrid.js?v=7",
//                "../../scripts/inventory/InventorySettings.js",
//                "../../scripts/inventory/QAApproval/StockoutApproval.js",
//                "../../scripts/inventory/QAApproval/InterstoreApproval.js",
//                "../../scripts/inventory/QAApproval/InspectionForm.js",
//                "../../scripts/inventory/QAApproval/InspectionTemplate.js",
//                "../../scripts/inventory/LocationMaster.js"
            ];
        }
    },
    getloandisbursementScripts : function() {
        if(isProdBuild) {
            return ['../../scripts/minifiedjs/loan-ex.js?v=53_00']
        } else {
            return [ ];
        }
    },
    getmrpScripts: function() {
        if (isProdBuild) {
            return ['../../scripts/minifiedjs/mrp-ex.js?v=53_00']
        } else {
            return this.getMRPJSList();
        }
    },
    getMRPJSList: function(){
        var list = [
            "../../scripts/MRP/MRPTransactionManager.js",
            "../../scripts/MRP/MasterContract.js",
            "../../scripts/MRP/ContractDetails.js",
            "../../scripts/MRP/ShipmentContract.js",
            "../../scripts/MRP/PackagingContract.js",
            "../../scripts/MRP/ProductGrid.js",
            "../../scripts/MRP/MasterContractList.js",
            "../../scripts/MRP/BillingContract.js",
            "../../scripts/MRP/PaymentTerms.js",
            "../../scripts/MRP/DocumentRequired.js",
            "../../scripts/MRP/LabourList.js",
            "../../scripts/MRP/MachineMasterList.js",
//            "../../scripts/MRP/MachineMaster.js",
            "../../scripts/MRP/MachineBreakDown.js",
//            "../../scripts/MRP/MachineMaintenanceDetail.js",
            "../../scripts/MRP/MachineManRatio.js",
            "../../scripts/MRP/LabourInformation.js",
            "../../scripts/MRP/AssignTaskList.js",
            "../../scripts/MRP/ResourceCost.js",
            "../../scripts/MRP/ResourceAnalysisReport.js",
            "../../scripts/MRP/ResolveConflictLabourMachineReport.js",
            "../../scripts/MRP/WorkCentreMasterReport.js",
            "../../scripts/MRP/WorkcentreMasterForm.js",
            "../../scripts/MRP/JobOrderReport.js",
            "../../scripts/MRP/WorkCentreList.js",
            "../../scripts/MRP/ResolveConflictLabourMachineReport.js",
            "../../scripts/MRP/workorder.js",
            "../../scripts/MRP/workOrderEntryForm.js",
            "../../scripts/MRP/workOrderReport.js",
            "../../scripts/MRP/WorkOrderStockDetailsReport.js",
            "../../scripts/MRP/JobOrderEntryForm.js",
            "../../scripts/MRP/RoutingMasterList.js",
            "../../scripts/MRP/MRPAssemblyProductDetails.js",
//            "../../scripts/MRP/RoutingTemplateMaster.js",
            "../../scripts/MRP/RoutingTemplateMasterGrid.js",
            "../../scripts/MRP/JobWorkOrderGrid.js",
            "../../scripts/MRP/MRPMultiUpload.js",
             "../../scripts/MRP/RejectedItemListMRP.js",
             "../../scripts/MRP/WOShortfallReport.js",
            "../../scripts/MRP/ForecastList.js",
            "../../scripts/MRP/ForecastCriteriaTemplate.js",
            "../../scripts/MRP/ForecastDetailList.js",
             "../../scripts/MRP/TaskProgressReport.js"
        ];
        
        return list
    },
    getCalendarScripts : function() {
        if(isProdBuild) {
            return ['fullcalendar-2.1.1/lib/jquery.min.js','fullcalendar-2.1.1/lib/jquery-ui.custom.min.js','lib/moment.min.js',
            'fullcalendar-2.1.1/fullcalendar.min.js','../../scripts/Calendar.js?v=53_00']
        } else {
            return [];
        }
                
    },
    
    getCalendarCSS : function() {
        if(isProdBuild) {
            return ['fullcalendar-2.1.1/fullcalendar.css','fullcalendar-2.1.1/fullcalendar.print.css?v=53_00']
        } else {
            return [];
        }
    },
    
    loadScripts : function(module) {
        var scriptsArray = [];
        switch(module) {
            case Wtf.ModuleScriptGr.consignment :
                if(!Wtf.ModuleScriptLoadedFlag.consignment) {
                    scriptsArray = WtfModuleWiseScripts.getConsignmentScripts();
                    Wtf.ModuleScriptLoadedFlag.consignment = true;
                }
                break;
            case Wtf.ModuleScriptGr.assetslease :
                if(!Wtf.ModuleScriptLoadedFlag.assetslease) {
                    scriptsArray = WtfModuleWiseScripts.getAssetsLeaseManagementScripts();
                    Wtf.ModuleScriptLoadedFlag.assetslease = true;
                }
                break;
            case Wtf.ModuleScriptGr.inventory :
                if(!Wtf.ModuleScriptLoadedFlag.inventory) {
                    scriptsArray = WtfModuleWiseScripts.getInventoryScripts();
                    Wtf.ModuleScriptLoadedFlag.inventory = true;
                }
                break;
            case Wtf.ModuleScriptGr.loan :
                if(!Wtf.ModuleScriptLoadedFlag.loan) {
                    scriptsArray = WtfModuleWiseScripts.getloandisbursementScripts();
                    Wtf.ModuleScriptLoadedFlag.loan = true;
                }
                break;
            case Wtf.ModuleScriptGr.mrp :
                if (!Wtf.ModuleScriptLoadedFlag.mrp) {
                    scriptsArray = WtfModuleWiseScripts.getmrpScripts();
                    Wtf.ModuleScriptLoadedFlag.mrp = true;
                }
                break;
        }
        
        if(scriptsArray.length>0) {
            ScriptMgr.load({
                scripts : scriptsArray,
                callback : function() {    
                    
                },
                scope : this
           });
        }
    }
}

function setupWizardScriptLoad() {
    if (Wtf.ModuleScriptLoadedFlag.setupwizard) {
        callSetUpWizardOnScriptLoad();
    } else {
        ScriptMgr.load({
            scripts : ['../../scripts/minifiedjs/setupwizard-ex.js?v=53_00'],
            callback : function() { 
                callSetUpWizardOnScriptLoad();
                Wtf.ModuleScriptLoadedFlag.setupwizard = true;
            },
            scope : this
       });
    }
}

function accountPrefScriptLoad(loadingMask) {

    if (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.showIndiaCompanyPreferencesTab) {// Load India company preferences JS 
        indiaCompanyPrefScriptLoad();   
    }
    if (Wtf.ModuleScriptLoadedFlag.companyprefsetting) {
        callAccountPrefOnScriptLoad(loadingMask);
    } else {
        ScriptMgr.load({
            scripts : ['../../scripts/minifiedjs/accountprefsetting-ex.js?v=53_00'],
            callback : function() { 
                callAccountPrefOnScriptLoad(loadingMask);
                Wtf.ModuleScriptLoadedFlag.companyprefsetting = true;
            },
            scope : this
       });
    }
}

function indiaCompanyPrefScriptLoad() {
    if (!Wtf.ModuleScriptLoadedFlag.IndiaCompanyPreferences) {
        ScriptMgr.load({
            scripts : ['../../scripts/mainscripts/IndiaCompanyPreferences.js?v=53_00'],
            callback : function() { 
                Wtf.ModuleScriptLoadedFlag.IndiaCompanyPreferences = true;
            },
            scope : this
       });
    }
}

function loadCalendarCSS() {
    if (Wtf.ModuleScriptLoadedFlag.calendar) {
        callCalendarOnScriptLoad();
    } else {
        ScriptMgr.loadCss(WtfModuleWiseScripts.getCalendarCSS());
        loadCalendarScripts();
    }
}

function loadCalendarScripts() {
    ScriptMgr.load({
        scripts : WtfModuleWiseScripts.getCalendarScripts(),
        callback : function() { 
            Wtf.ModuleScriptLoadedFlag.calendar = true;
            callCalendarOnScriptLoad();
        },
        scope : this
    });
}
