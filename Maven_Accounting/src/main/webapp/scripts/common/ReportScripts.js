/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.ReportScriptLoadedFlag = {
    agedreceivablereportbasedondimension: false,
    customerrevenuereport: false,
    bankbooksummaryreport: false,
    salespersoncommissionreport: false,
    salespersoncommissionproductreport: false,
    cashflowstatementreport: false,
    costcentersummaryanddetailreport: false,
    creditnoteaccountdetailreport: false,
    customcolumndetailandsummaryreport: false,
    customercreditlimit: false,
    customerandvendorpartledgerreport: false,
    customervendortledgerreport: false,
    customervendorproductexpiryreport: false,
    dimensionbasedbalancesheetandprofitandloss: false,
    dimensionbasedtrialbalance: false,
    dimensionsreport: false,
    drivertrackingreport: false,
    unpaidinvoiceandfinancedetailreport: false,
    foreigncurrencyexposureandforeigngailloss: false,
    inactivecustomerlistreport: false,
    inventorymovementsummaryanddetailreport: false,
    invoicevhtandwhtreport: false,
    linkinginformationeport: false,
    missingautosequencenumberreport: false,
    monthlybalancesheetreport: false,
    monthlyagedpayablereport: false,
    monthlyrevenueandmonthlytradingprogitloss: false,
    yearlytradingprogitloss: false,
    monthlysalesreportandmonthlysalesbyproductreport: false,
    dailysalesreport: false,
    dailybookingreport: false,
    monthlybookingreport: false,
    yearlybookingreport: false,
    showWidget: false,
    excisecomputationreport: false,
    servicetaxcomputationreport: false,
    outstandingorderreport: false,
    productmaintenancereport: false,
    productreplacementreport: false,
    qapendingrejecteditemsandreorderanalysisreport: false,
    revenuerecognitionreport: false,
    salesbyserviceproductdetailreport: false,
    salespersoncommissiondimensionreport: false,
    salesbyitemanddetailreport: false,
    salesbyproductcategoryreport: false,
    dayendcollectionreport: false,
    GSTHistoryInput: false,
    jobworkorderwithoutgrn: false,
    jobWorkInAgedReport: false,
    pendingapprovalreport: false,
    stockageingreport: false,
    stockledger: false,
    customdetailreport: false,
    stockmovementreportadvancesearch: false,
    ChallanWiseReport: false,
    JWProductSummaryWiseReport: false,
    stockstatusreport: false,
    stockvaluationdetailreport: false,
    stockvaluationsummaryreport: false,
    topanddormantcustomervendorandprosuctreport:false,
    tradingandprofitlosswithbudget:false,
    salescommissionpaymentterm:false,
    monthlycommissionofsalespersonreport:false,
    deliveryplannerreport:false,
    accountforecastingreport:false,
    vehicledeliverysummaryreport:false,
    salespurchasehistoryreport:false,
    callCommonReport:false,
    pricevariancereport: false,
    gstFormGenerationHistory:false,
    customerregisteryreport:false,
    vendorregisteryreport:false,
    taxperiodsettings:false,
    accountingperiodsettings:false,
    pricelistbandreport: false,
    producttransactiondetailreport: false,
    productbranddiscountwin: false,
    rule16register: false,
    smtpauthentication: false,
    plaReport: false,
    dailyStockReport: false,
    rg23Part1: false,
    vatregisters:false,
    rg23Part2: false,
    annexure10Report: false,
    testmailwindow: false,
    serviceTaxReports: false,
    TDSChallanControlReport:false,
    costAndSellingPriceOfItemsToCustomer:false,
    salesPurhcaseAnnexureReport:false,
    vatAndCSTCalculationReport:false,
    monthWiseGeneralLedger: false,
    SalesAnalysis: false,
    formDVAT31Report:false,
    TDSMasterRates:false,
    CustomerReceivedReport:false,
    LandingCostItemReport:false,
    salesCommissionReport: false,
    salesprofitReport: false,
    monthlycustomlayout: false,
    checkInCheckOutReport: false,
    checkIncidentCasesReport: false,
    gstsalestaxliabilityreport: false,
    callDiscountDetailsWindow: false,
    gstOutputRuleReport : false,
    gstInputRuleReport : false,
    CostOfManufacturing : false,
    GSTRSummaryReport : false, // GSTR1, GSTR2 and MisMatch summary report flag
    GSTRDetailedReport : false, // GSTR1, GSTR2 and MisMatch details report flag
    GSTComputationReport : false, // GST Computation summary report flag
    GSTR3BSummaryReport : false, // GSTR3B summary report flag
    GSTR3BAndComputationDetailedReport : false, // GSTR3B and GST Computation detailed report flag
    GSTR2MatchAndComparisionReport : false, // GSTR2MatchAndReconcile and callGSTR2AComparison report Flag
    GSTForm5eSubmissionFunction : false,
    phpVATReport : false, // Load Philippines VAT Summary report JS dynamically flag
    indonesiaVATReport : false, // Load Indonesia VAT Summary report JS dynamically flag
    callPhilippiensReliefReports:false
}


function callDimensionsBasedAgeingReport(isSummary, searchStr, filterAppend, rowdata) {
    if (!isProdBuild) {
        loadAgedreportbasedonManagers(isSummary, searchStr, filterAppend, rowdata);
    } else {
        if (Wtf.ReportScriptLoadedFlag.agedreceivablereportbasedondimension) {
            loadAgedreportbasedonManagers(isSummary, searchStr, filterAppend, rowdata);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/AgedReportBasedOnDimension.js'],
                callback: function () {
                    loadAgedreportbasedonManagers(isSummary, searchStr, filterAppend, rowdata);
                    Wtf.ReportScriptLoadedFlag.agedreceivablereportbasedondimension = true
                },
                scope: this
            });
        }
    }
}

function callDimensionsBasedAgedPayableReport(isSummary, searchStr, filterAppend, rowdata) {
    if (!isProdBuild) {
        callDimensionsBasedAgedPayableReportbasedonManagers(isSummary, searchStr, filterAppend, rowdata);
    } else {
        if (Wtf.ReportScriptLoadedFlag.agedreceivablereportbasedondimension) {
            callDimensionsBasedAgedPayableReportbasedonManagers(isSummary, searchStr, filterAppend, rowdata);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/AgedReportBasedOnDimension.js'],
                callback: function () {
                    callDimensionsBasedAgedPayableReportbasedonManagers(isSummary, searchStr, filterAppend, rowdata);
                    Wtf.ReportScriptLoadedFlag.agedreceivablereportbasedondimension = true
                },
                scope: this
            });
        }
    }
}

function getCustRevenueView() {
    if (!isProdBuild) {
        getCustRevenueViewReport();
    } else {
        if (Wtf.ReportScriptLoadedFlag.customerrevenuereport) {
            getCustRevenueViewReport();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CustomerRevenue.js'],
                callback: function () {
                    getCustRevenueViewReport();
                    Wtf.ReportScriptLoadedFlag.customerrevenuereport = true
                },
                scope: this
            });
        }
    }
}

function callBankBookSummaryReport() {
    if (!isProdBuild) {
        callBankBookSummaryPanel();
    } else {
        if (Wtf.ReportScriptLoadedFlag.bankbooksummaryreport) {
            callBankBookSummaryPanel();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/BankBookSummayReport.js'],
                callback: function () {
                    callBankBookSummaryPanel();
                    Wtf.ReportScriptLoadedFlag.bankbooksummaryreport = true
                },
                scope: this
            });
        }
    }
}

function SalesCommissionReport() {
    if (!isProdBuild) {
        SalesCommissionReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.salespersoncommissionreport) {
            SalesCommissionReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SalesComissionReport.js','../../scripts/Reports/salesCommissionDetailReport.js'],
                callback: function () {
                    SalesCommissionReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.salespersoncommissionreport = true
                },
                scope: this
            });
        }
    }
}

function SalesCommissionproductReport() {
    if (!isProdBuild) {
        getSalesCommissionproductDetailTabView();
    } else {
        if (Wtf.ReportScriptLoadedFlag.salespersoncommissionproductreport) {
            getSalesCommissionproductDetailTabView();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/salesCommissionproductDetailReport.js'],
                callback: function () {
                    getSalesCommissionproductDetailTabView();
                    Wtf.ReportScriptLoadedFlag.salespersoncommissionproductreport = true
                },
                scope: this
            });
        }
    }
}

function BrandCommissionReport() {

    if (!isProdBuild) {
        BrandCommissionReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.salespersoncommissionreport) {
            BrandCommissionReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SalesComissionReport.js'],
                callback: function () {
                    BrandCommissionReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.salespersoncommissionreport = true
                },
                scope: this
            });
        }
    }

}

function callCashFlowStatement(consolidateFlag) {
    if (!isProdBuild) {
        callCashFlowStatementDynamicLoad(consolidateFlag);
    } else {
        if (Wtf.ReportScriptLoadedFlag.cashflowstatementreport) {
            callCashFlowStatementDynamicLoad(consolidateFlag);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CashFlowStatement.js'],
                callback: function () {
                    callCashFlowStatementDynamicLoad(consolidateFlag);
                    Wtf.ReportScriptLoadedFlag.cashflowstatementreport = true
                },
                scope: this
            });
        }
    }
}


function callWeeklyCashFlowStatement(consolidateFlag) {
    if (!isProdBuild) {
        callWeeklyCashFlowStatementDynamicLoad(consolidateFlag);
    } else {
        if (Wtf.ReportScriptLoadedFlag.cashflowstatementreport) {
            callWeeklyCashFlowStatementDynamicLoad(consolidateFlag);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CashFlowStatement.js'],
                callback: function () {
                    callWeeklyCashFlowStatementDynamicLoad(consolidateFlag);
                    Wtf.ReportScriptLoadedFlag.cashflowstatementreport = true
                },
                scope: this
            });
        }
    }
}


function callCostCenterReportTransactionDetails(consolidateFlag) {
    if (!isProdBuild) {
        callCostCenterReportTransactionDetailsDynamicLoad(consolidateFlag);
    } else {
        if (Wtf.ReportScriptLoadedFlag.costcentersummaryanddetailreport) {
            callCostCenterReportTransactionDetailsDynamicLoad(consolidateFlag);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CostCenterDetailsReport.js'],
                callback: function () {
                    callCostCenterReportTransactionDetailsDynamicLoad(consolidateFlag);
                    Wtf.ReportScriptLoadedFlag.costcentersummaryanddetailreport = true
                },
                scope: this
            });
        }
    }
}

function callAllCostCenterSummary(consolidateFlag) {
    if (!isProdBuild) {
        callAllCostCenterSummaryDynamicLoad(consolidateFlag);
    } else {
        if (Wtf.ReportScriptLoadedFlag.costcentersummaryanddetailreport) {
            callAllCostCenterSummaryDynamicLoad(consolidateFlag);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CostCenterDetailsReport.js'],
                callback: function () {
                    callAllCostCenterSummaryDynamicLoad(consolidateFlag);
                    Wtf.ReportScriptLoadedFlag.costcentersummaryanddetailreport = true
                },
                scope: this
            });
        }
    }
}


function callCreditNoteAccountDetailReport(searchStr, filterAppend) {
    if (!isProdBuild) {
        callCreditNoteAccountDetailReportDynamicLoad(searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.creditnoteaccountdetailreport) {
            callCreditNoteAccountDetailReportDynamicLoad(searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CreditNoteWithAccountDetail.js'],
                callback: function () {
                    callCreditNoteAccountDetailReportDynamicLoad(searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.creditnoteaccountdetailreport = true
                },
                scope: this
            });
        }
    }
}


function CustomColumnSummaryReportList() {//Summary Report

    if (!isProdBuild) {
        CustomColumnSummaryReportListDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.customcolumndetailandsummaryreport) {
            CustomColumnSummaryReportListDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CustomDetailSummaryReport.js'],
                callback: function () {
                    CustomColumnSummaryReportListDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.customcolumndetailandsummaryreport = true
                },
                scope: this
            });
        }
    }

}

function CustomColumnDetailReportList() {  //Detail Report

    if (!isProdBuild) {
        CustomColumnDetailReportListDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.customcolumndetailandsummaryreport) {
            CustomColumnDetailReportListDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CustomDetailSummaryReport.js'],
                callback: function () {
                    CustomColumnDetailReportListDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.customcolumndetailandsummaryreport = true
                },
                scope: this
            });
        }
    }

}

function callCostOfManufacturing() {

    if (!isProdBuild) {
        callCostOfManufacturingReport();
    } else {
        if (Wtf.ReportScriptLoadedFlag.CostOfManufacturing) {
            callCostOfManufacturingReport();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CostOfManufacturing.js'],
                callback: function () {
                    callCostOfManufacturingReport();
                    Wtf.ReportScriptLoadedFlag.CostOfManufacturing = true;
                },
                scope: this
            });
        }
    }

}

function callCustomerCreditException() {
    if (!isProdBuild) {
        callCustomerCreditExceptionDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.customercreditlimit) {
            callCustomerCreditExceptionDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CustomerCreditExceptionReport.js'],
                callback: function () {
                    callCustomerCreditExceptionDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.customercreditlimit = true
                },
                scope: this
            });
        }
    }

}

function callCustomerPartyLedgerReport() {
    if (!isProdBuild) {
        callCustomerPartyLedgerReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.customerandvendorpartledgerreport) {
            callCustomerPartyLedgerReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/WtfPartyLedger.js'],
                callback: function () {
                    callCustomerPartyLedgerReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.customerandvendorpartledgerreport = true
                },
                scope: this
            });
        }
    }
}

function callVendorPartyLedgerReport() {
    if (!isProdBuild) {
        callVendorPartyLedgerReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.customerandvendorpartledgerreport) {
            callVendorPartyLedgerReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/WtfPartyLedger.js'],
                callback: function () {
                    callVendorPartyLedgerReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.customerandvendorpartledgerreport = true
                },
                scope: this
            });
        }
    }
}

function callCustomerVendorTLedgerReport() {
    if (!isProdBuild) {
        callCustomerVendorTLedgerReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.customervendortledgerreport) {
            callCustomerVendorTLedgerReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CustomerVendorLedger.js'],
                callback: function () {
                    callCustomerVendorTLedgerReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.customervendortledgerreport = true
                },
                scope: this
            });
        }
    }
}

function getSerialNoTabView() {
    if (!isProdBuild) {
        getSerialNoTabViewDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.customervendorproductexpiryreport) {
            getSerialNoTabViewDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SerialNoDetails.js'],
                callback: function () {
                    getSerialNoTabViewDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.customervendorproductexpiryreport = true
                },
                scope: this
            });
        }
    }
}

function getJobWorkOutWithGRNTabView() {
    if (!isProdBuild) {
        getJobWorkOutWithGRNViewDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.jobworkorderwithoutgrn) {
            getJobWorkOutWithGRNViewDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/JobWorkOutOrderWithoutGRN.js'],
                callback: function () {
                    getJobWorkOutWithGRNViewDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.jobworkorderwithoutgrn = true
                },
                scope: this
            });
        }
    }
}

/*
 * To Open job work in ageing report
 */
function getJobWorkInAgedReport() {
    if (!isProdBuild) {
        jobWorkInAgedReport();
    } else {
        if (Wtf.ReportScriptLoadedFlag.jobWorkInAgedReport) {
            jobWorkInAgedReport();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/JobWorkInAgedReport.js'],
                callback: function () {
                    jobWorkInAgedReport();
                    Wtf.ReportScriptLoadedFlag.jobWorkInAgedReport = true
                },
                scope: this
            });
        }
    }
}

function getProductExpiryTabView() {
    if (!isProdBuild) {
        getProductExpiryTabViewDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.customervendorproductexpiryreport) {
            getProductExpiryTabViewDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SerialNoDetails.js'],
                callback: function () {
                    getProductExpiryTabViewDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.customervendorproductexpiryreport = true
                },
                scope: this
            });
        }
    }
}

function callDimensionBasedProfitLoss(isCustomLayout,templateid, searchStr, filterAppend) {
    if (!isProdBuild) {
        callDimensionBasedProfitLossDynamicLoad(isCustomLayout,templateid, searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.dimensionbasedbalancesheetandprofitandloss) {
            callDimensionBasedProfitLossDynamicLoad(isCustomLayout,templateid, searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/WtfDimensionBasedProfitAndLoss.js'],
                callback: function () {
                    callDimensionBasedProfitLossDynamicLoad(isCustomLayout,templateid, searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.dimensionbasedbalancesheetandprofitandloss = true
                },
                scope: this
            });
        }
    }
}

function callDimensionBasedBalanceSheet(isCustomLayout,templateid, searchStr, filterAppend) {
    if (!isProdBuild) {
        callDimensionBasedBalanceSheetDynamicLoad(isCustomLayout,templateid, searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.dimensionbasedbalancesheetandprofitandloss) {
            callDimensionBasedBalanceSheetDynamicLoad(isCustomLayout,templateid, searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/WtfDimensionBasedProfitAndLoss.js'],
                callback: function () {
                    callDimensionBasedBalanceSheetDynamicLoad(isCustomLayout,templateid, searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.dimensionbasedbalancesheetandprofitandloss = true
                },
                scope: this
            });
        }
    }
}

function callDimensionBasedTrialBalance(searchStr, filterAppend) {
    if (!isProdBuild) {
        callDimensionBasedTrialBalanceDynamicLoad(searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.dimensionbasedtrialbalance) {
            callDimensionBasedTrialBalanceDynamicLoad(searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/WtfDimensionBasedTrialBalance.js'],
                callback: function () {
                    callDimensionBasedTrialBalanceDynamicLoad(searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.dimensionbasedtrialbalance = true
                },
                scope: this
            });
        }
    }
}


function callDimensionsReport() {
    if (!isProdBuild) {
        callDimensionsReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.dimensionsreport) {
            callDimensionsReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/DimensionsReport.js'],
                callback: function () {
                    callDimensionsReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.dimensionsreport = true
                },
                scope: this
            });
        }
    }
}


function getDriversTrackingReportTabView() {
    if (!isProdBuild) {
        getDriversTrackingReportTabViewDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.drivertrackingreport) {
            getDriversTrackingReportTabViewDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/DriversTrackingReport.js'],
                callback: function () {
                    getDriversTrackingReportTabViewDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.drivertrackingreport = true
                },
                scope: this
            });
        }
    }
}

function UnpaidInvoices() {
    if (!isProdBuild) {
        UnpaidInvoicesDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.unpaidinvoiceandfinancedetailreport) {
            UnpaidInvoicesDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/FinanceReport.js'],
                callback: function () {
                    UnpaidInvoicesDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.unpaidinvoiceandfinancedetailreport = true
                },
                scope: this
            });
        }
    }
}

function FinanceDetailsReport() {
    if (!isProdBuild) {
        FinanceDetailsReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.unpaidinvoiceandfinancedetailreport) {
            FinanceDetailsReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/FinanceReport.js'],
                callback: function () {
                    FinanceDetailsReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.unpaidinvoiceandfinancedetailreport = true
                },
                scope: this
            });
        }
    }
}


function callForeignCurrencyExposure(consolidateFlag) {
    if (!isProdBuild) {
        callForeignCurrencyExposureDynamicLoad(consolidateFlag);
    } else {
        if (Wtf.ReportScriptLoadedFlag.foreigncurrencyexposureandforeigngailloss) {
            callForeignCurrencyExposureDynamicLoad(consolidateFlag);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/ForeignCurrencyExposure.js'],
                callback: function () {
                    callForeignCurrencyExposureDynamicLoad(consolidateFlag);
                    Wtf.ReportScriptLoadedFlag.foreigncurrencyexposureandforeigngailloss = true
                },
                scope: this
            });
        }
    }
}
function callForeignCurrencyGainAndLossTabView(consolidateFlag) {
    if (!isProdBuild) {
        callForeignCurrencyGainAndLossTabViewDynamicLoad(consolidateFlag);
    } else {
        if (Wtf.ReportScriptLoadedFlag.foreigncurrencyexposureandforeigngailloss) {
            callForeignCurrencyGainAndLossTabViewDynamicLoad(consolidateFlag);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/ForeignCurrencyExposure.js'],
                callback: function () {
                    callForeignCurrencyGainAndLossTabViewDynamicLoad(consolidateFlag);
                    Wtf.ReportScriptLoadedFlag.foreigncurrencyexposureandforeigngailloss = true
                },
                scope: this
            });
        }
    }
}

function callInactiveCustomerList(personlinkid, openperson, withinventory) {
    if (!isProdBuild) {
        callInactiveCustomerListDynamicLoad(personlinkid, openperson, withinventory);
    } else {
        if (Wtf.ReportScriptLoadedFlag.inactivecustomerlistreport) {
            callInactiveCustomerListDynamicLoad(personlinkid, openperson, withinventory);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/InactiveCustomerList.js'],
                callback: function () {
                    callInactiveCustomerListDynamicLoad(personlinkid, openperson, withinventory);
                    Wtf.ReportScriptLoadedFlag.inactivecustomerlistreport = true
                },
                scope: this
            });
        }
    }
}

function getInventoryMovementDetailsReport() {
    if (!isProdBuild) {
        getInventoryMovementDetailsReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.inventorymovementsummaryanddetailreport) {
            getInventoryMovementDetailsReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/InventoryMovementReport.js'],
                callback: function () {
                    getInventoryMovementDetailsReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.inventorymovementsummaryanddetailreport = true
                },
                scope: this
            });
        }
    }
}

function getInventoryMovementSummaryReport() {
    if (!isProdBuild) {
        getInventoryMovementDetailsReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.inventorymovementsummaryanddetailreport) {
            getInventoryMovementDetailsReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/InventoryMovementReport.js'],
                callback: function () {
                    getInventoryMovementDetailsReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.inventorymovementsummaryanddetailreport = true
                },
                scope: this
            });
        }
    }
}

function getInvoicesVatReport(record) {
    if (!isProdBuild) {
        getInvoicesVatReportDynamicLoad(record);
    } else {
        if (Wtf.ReportScriptLoadedFlag.invoicevhtandwhtreport) {
            getInvoicesVatReportDynamicLoad(record);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/VATandVHTReport.js'],
                callback: function () {
                    getInvoicesVatReportDynamicLoad(record);
                    Wtf.ReportScriptLoadedFlag.invoicevhtandwhtreport = true
                },
                scope: this
            });
        }
    }
}

function getRule16Register(record) { 
    if (!isProdBuild) {
        getRule16RegisterDynamicLoad(record);
    } else {
        if (Wtf.ReportScriptLoadedFlag.rule16register) {
            getRule16RegisterDynamicLoad(record);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/Rule16Register.js'],
                callback: function () {
                    getRule16RegisterDynamicLoad(record);
                    Wtf.ReportScriptLoadedFlag.rule16register = true
                },
                scope: this
            });
        }
    }
}

function getRG23Part1(record) { 
    if (!isProdBuild) {
        getRG23Part1DynamicLoad(record);
    } else {
        if (Wtf.ReportScriptLoadedFlag.rg23Part1) {
            getRG23Part1DynamicLoad(record);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/RG23Part1.js'],
                callback: function () {
                    getRG23Part1DynamicLoad(record);
                    Wtf.ReportScriptLoadedFlag.rg23Part1 = true
                },
                scope: this
            });
        }
    }
}
function getRG23Part2(record) { 
    if (!isProdBuild) {
        getRG23Part2DynamicLoad(record);
    } else {
        if (Wtf.ReportScriptLoadedFlag.rg23Part2) {
            getRG23Part2DynamicLoad(record);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/RG23Part2.js'],
                callback: function () {
                    getRG23Part2DynamicLoad(record);
                    Wtf.ReportScriptLoadedFlag.rg23Part2 = true
                },
                scope: this
            });
        }
    }
}

function getDailyStockRegister(record) { 
    if (!isProdBuild) {
        getDailyStockRegisterDynamicLoad(record);
    } else {
        if (Wtf.ReportScriptLoadedFlag.dailyStockReport) {
            getDailyStockRegisterDynamicLoad(record);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/dailyStockReport.js'],
                callback: function () {
                    getDailyStockRegisterDynamicLoad(record);
                    Wtf.ReportScriptLoadedFlag.dailyStockReport = true
                },
                scope: this
            });
        }
    }
}

function getVatSalesRegister(isCommodity,isSales) { 
    if (!isProdBuild) {
        getVatReportsDynamicLoad(isCommodity,isSales);
    } else {
        if (Wtf.ReportScriptLoadedFlag.vatregisters) {
            getVatReportsDynamicLoad(isCommodity,isSales);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/vatRegister.js'],
                callback: function () {
                    getVatReportsDynamicLoad(isCommodity,isSales);
                    Wtf.ReportScriptLoadedFlag.vatregisters = true
                },
                scope: this
            });
        }
    }
}

function callPlaReport(isPlaSummary) {
    if (!isProdBuild) {
        getPlaReport(isPlaSummary);
    } else {
        if (Wtf.ReportScriptLoadedFlag.plaReport) {
            getPlaReport(isPlaSummary);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/plaReport.js'],
                callback: function () {
                    getPlaReport(isPlaSummary);
                    Wtf.ReportScriptLoadedFlag.plaReport = true
                },
                scope: this
            });
        }
    }
}
function callServiceTaxReport(type) {
    if (!isProdBuild) {
        getServiceTaxReport(type);
    } else {
        if (Wtf.ReportScriptLoadedFlag.serviceTaxReports) {
            getServiceTaxReport(type);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/serviceTaxReports.js'],
                callback: function () {
                    getServiceTaxReport(type);
                    Wtf.ReportScriptLoadedFlag.serviceTaxReports = true
                },
                scope: this
            });
        }
    }
}
function callCreditAvailedReport() {
    if (!isProdBuild) {
        getCreditAvailedReport();
    } else {
        if (Wtf.ReportScriptLoadedFlag.plaReport) {
            getCreditAvailedReport();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/plaReport.js'],
                callback: function () {
                    getCreditAvailedReport();
                    Wtf.ReportScriptLoadedFlag.plaReport = true
                },
                scope: this
            });
        }
    }
}
function getAnnexure10Report() {
    if (!isProdBuild) {
        getAnnexure10ReportLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.plaReport) {
            getAnnexure10ReportLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/Annexure10Report.js'],
                callback: function () {
                    getAnnexure10ReportLoad();
                    Wtf.ReportScriptLoadedFlag.annexure10Report = true
                },
                scope: this
            });
        }
    }
}
function getInvoicesVhtReport(record) {
    if (!isProdBuild) {
        getInvoicesVhtReportDynamicLoad(record);
    } else {
        if (Wtf.ReportScriptLoadedFlag.invoicevhtandwhtreport) {
            getInvoicesVhtReportDynamicLoad(record);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/VATandVHTReport.js'],
                callback: function () {
                    getInvoicesVhtReportDynamicLoad(record);
                    Wtf.ReportScriptLoadedFlag.invoicevhtandwhtreport = true
                },
                scope: this
            });
        }
    }
}

function linkPurchaseReportTab(module,moduleid,transactionno) {
    if (!isProdBuild) {
        linkPurchaseReportTabDynamicLoad(module,moduleid,transactionno);
    } else {
        if (Wtf.ReportScriptLoadedFlag.linkinginformationeport) {
            linkPurchaseReportTabDynamicLoad(module,moduleid,transactionno);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/AccLinkDataReport.js'],
                callback: function () {
                    linkPurchaseReportTabDynamicLoad(module,moduleid,transactionno);
                    Wtf.ReportScriptLoadedFlag.linkinginformationeport = true
                },
                scope: this
            });
        }
    }
}

function callMissingAutoSequenceNumberWindow() {
    if (!isProdBuild) {
        callMissingAutoSequenceNumberWindowDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.missingautosequencenumberreport) {
            callMissingAutoSequenceNumberWindowDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/MissingAutoSequenceNumber.js'],
                callback: function () {
                    callMissingAutoSequenceNumberWindowDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.missingautosequencenumberreport = true
                },
                scope: this
            });
        }
    }

}

function monthlyBalanceSheet() {
    if (!isProdBuild) {
        monthlyBalanceSheetDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlybalancesheetreport) {
            monthlyBalanceSheetDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/WtfMonthlyBalanceSheet.js'],
                callback: function () {
                    monthlyBalanceSheetDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.monthlybalancesheetreport = true
                },
                scope: this
            });
        }
    }
}


function callMonthlyAgedPayable(withinventory, custVendorID) {
    if (!isProdBuild) {
        callMonthlyAgedPayableDynamicLoad(withinventory, custVendorID);
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlyagedpayablereport) {
            callMonthlyAgedPayableDynamicLoad(withinventory, custVendorID);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/MonthlyAgeingReport.js'],
                callback: function () {
                    callMonthlyAgedPayableDynamicLoad(withinventory, custVendorID);
                    Wtf.ReportScriptLoadedFlag.monthlyagedpayablereport = true
                },
                scope: this
            });
        }
    }
}

function callMonthlyAgedRecievable(withinventory, custVendorID) {
    if (!isProdBuild) {
        callMonthlyAgedRecievableDynamicLoad(withinventory, custVendorID);
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlyagedpayablereport) {
            callMonthlyAgedRecievableDynamicLoad(withinventory, custVendorID);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/MonthlyAgeingReport.js'],
                callback: function () {
                    callMonthlyAgedRecievableDynamicLoad(withinventory, custVendorID);
                    Wtf.ReportScriptLoadedFlag.monthlyagedpayablereport = true
                },
                scope: this
            });
        }
    }
}

function monthlyRevenue() {
    if (!isProdBuild) {
        monthlyRevenueDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlyrevenueandmonthlytradingprogitloss) {
            monthlyRevenueDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/WtfNewMonthlyTrading.js'],
                callback: function () {
                    monthlyRevenueDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.monthlyrevenueandmonthlytradingprogitloss = true
                },
                scope: this
            });
        }
    }
}

function monthlyTradingProfitLoss(params) {
    if(params == undefined){
        params={};
    }
    if (!isProdBuild) {
        monthlyTradingProfitLossDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlyrevenueandmonthlytradingprogitloss) {
            monthlyTradingProfitLossDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/WtfNewMonthlyTrading.js'],
                callback: function () {
                    monthlyTradingProfitLossDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.monthlyrevenueandmonthlytradingprogitloss = true
                },
                scope: this
            });
        }
    }
}

function YearlyTradingProfitLoss(params) {
    if(params == undefined){
        params={};
    }
    if (!isProdBuild) {
        yearlyTradingProfitLossDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.yearlytradingprogitloss) {
            yearlyTradingProfitLossDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/YearlyTradingAndPLReport.js'],
                callback: function () {
                    yearlyTradingProfitLossDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.yearlytradingprogitloss = true
                },
                scope: this
            });
        }
    }
}

function callMonthlySalesByProduct(params) {
    if(params == undefined){
        params={};
    }
    if (!isProdBuild) {
        callMonthlySalesByProductDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlysalesreportandmonthlysalesbyproductreport) {
            callMonthlySalesByProductDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/MonthlySalesReport.js', '../../scripts/Reports/MonthlySalesByProductSubjectToGSTReport.js'],
                callback: function () {
                    callMonthlySalesByProductDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.monthlysalesreportandmonthlysalesbyproductreport = true
                },
                scope: this
            });
        }
    }
}

function monthlySalesReport(params) {
    if(params == undefined){
        params={};
    }
    if (!isProdBuild) {
        monthlySalesReportDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlysalesreportandmonthlysalesbyproductreport) {
            monthlySalesReportDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/MonthlySalesReport.js'],
                callback: function () {
                    monthlySalesReportDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.monthlysalesreportandmonthlysalesbyproductreport = true
                },
                scope: this
            });
        }
    }
}
/*
 *Function to load customer summary report from report list 
 */
function customerSummaryMonthlySalesReport(consolidateFlag, withinventory) {
    if (!isProdBuild) {
        customerSummaryMonthlySalesDynamicLoad(consolidateFlag, withinventory);
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlysalesreportandmonthlysalesbyproductreport) {
            customerSummaryMonthlySalesDynamicLoad(consolidateFlag, withinventory);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CustomerSummaryMonthlySales.js'],
                callback: function () {
                    customerSummaryMonthlySalesDynamicLoad(consolidateFlag, withinventory);
                    Wtf.ReportScriptLoadedFlag.monthlysalesreportandmonthlysalesbyproductreport = true
                },
                scope: this
            });
        }
    }
}

function callDailyBookingsReport(params) {
    if(params == undefined){
        params={};
    }
    params.isBookingReport = true;
    if (!isProdBuild) {
        dailySalesReportDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.dailybookingreport) {
            dailySalesReportDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/DailySalesReport.js'],
                callback: function () {
                    dailySalesReportDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.dailybookingreport = true
                },
                scope: this
            });
        }
    }
}

function callMonthlyBookingsReport(params) {
    if(params == undefined){
        params={};
    }
    params.isMonthlyBookings = true;
    if (!isProdBuild) {
        monthlyBookingsReportDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlybookingreport) {
            monthlyBookingsReportDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/BookingsReport.js'],
                callback: function () {
                    monthlyBookingsReportDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.monthlybookingreport = true
                },
                scope: this
            });
        }
    }
}

function callYearlyBookingsReport(params) {
    if(params == undefined){
        params={};
    }
    if (!isProdBuild) {
        monthlyBookingsReportDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.yearlybookingreport) {
            monthlyBookingsReportDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/BookingsReport.js'],
                callback: function () {
                    monthlyBookingsReportDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.yearlybookingreport = true
                },
                scope: this
            });
        }
    }
}

function callDailySalesReport(params) {
    if(params == undefined){
        params={};
    }
    if (!isProdBuild) {
        dailySalesReportDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.dailysalesreport) {
            dailySalesReportDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/DailySalesReport.js'],
                callback: function () {
                    dailySalesReportDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.dailysalesreport = true
                },
                scope: this
            });
        }
    }
}
function callExciseComputationReport() {
    if (!isProdBuild) {
        callExciseComputationDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.excisecomputationreport) {
            callExciseComputationDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/ExciseComputationReport.js'],
                callback: function () {
                    callExciseComputationDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.excisecomputationreport = true
                },
                scope: this
            });
        }
    }
}
function callServiceTaxComputationReport() {
    if (!isProdBuild) {
        callServiceTaxComputationDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.servicetaxcomputationreport) {
            callServiceTaxComputationDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/ServiceTaxComputationReport.js'],
                callback: function () {
                    callServiceTaxComputationDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.servicetaxcomputationreport = true
                },
                scope: this
            });
        }
    }
}

function MonthlySalesInvoicesList(jid, consolidateFlag, reportbtnshwFlag, customerid, monthyear) {
    if (!isProdBuild) {
        MonthlySalesInvoicesListDynamicLoad(jid, consolidateFlag, reportbtnshwFlag, customerid, monthyear);
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlysalesreportandmonthlysalesbyproductreport) {
            MonthlySalesInvoicesListDynamicLoad(jid, consolidateFlag, reportbtnshwFlag, customerid, monthyear);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/MonthlySalesReport.js'],
                callback: function () {
                    MonthlySalesInvoicesListDynamicLoad(jid, consolidateFlag, reportbtnshwFlag, customerid, monthyear);
                    Wtf.ReportScriptLoadedFlag.monthlysalesreportandmonthlysalesbyproductreport = true
                },
                scope: this
            });
        }
    }
}

function MonthlySalesInvoicesListByProduct(productid, consolidateFlag, reportbtnshwFlag, customerid, monthyear) {
    if (!isProdBuild) {
        MonthlySalesInvoicesListByProductDynamicLoad(productid, consolidateFlag, reportbtnshwFlag, customerid, monthyear);
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlysalesreportandmonthlysalesbyproductreport) {
            MonthlySalesInvoicesListByProductDynamicLoad(productid, consolidateFlag, reportbtnshwFlag, customerid, monthyear);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/MonthlySalesReport.js'],
                callback: function () {
                    MonthlySalesInvoicesListByProductDynamicLoad(productid, consolidateFlag, reportbtnshwFlag, customerid, monthyear);
                    Wtf.ReportScriptLoadedFlag.monthlysalesreportandmonthlysalesbyproductreport = true
                },
                scope: this
            });
        }
    }
}

function CallOutstandingOrdersReport() {
    if (!isProdBuild) {
        CallOutstandingOrdersReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.outstandingorderreport) {
            CallOutstandingOrdersReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/RecurringInvoiceList.js'],
                callback: function () {
                    CallOutstandingOrdersReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.outstandingorderreport = true
                },
                scope: this
            });
        }
    }
}

function callProductMaintenanceReport(id, isNormalContract, titlelabel) {

    if (!isProdBuild) {
        callProductMaintenanceReportDynamicLoad(id, isNormalContract, titlelabel);
    } else {
        if (Wtf.ReportScriptLoadedFlag.productmaintenancereport) {
            callProductMaintenanceReportDynamicLoad(id, isNormalContract, titlelabel);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/MaintenanceReport.js'],
                callback: function () {
                    callProductMaintenanceReportDynamicLoad(id, isNormalContract, titlelabel);
                    Wtf.ReportScriptLoadedFlag.productmaintenancereport = true
                },
                scope: this
            });
        }
    }
}


function callProductReplacementReport(id, isNormalContract, titlelabel) {

    if (!isProdBuild) {
        callProductReplacementReportDynamicLoad(id, isNormalContract, titlelabel);
    } else {
        if (Wtf.ReportScriptLoadedFlag.productreplacementreport) {
            callProductReplacementReportDynamicLoad(id, isNormalContract, titlelabel);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/ProductReplacementList.js'],
                callback: function () {
                    callProductReplacementReportDynamicLoad(id, isNormalContract, titlelabel);
                    Wtf.ReportScriptLoadedFlag.productreplacementreport = true
                },
                scope: this
            });
        }
    }
}


function callQAApprovalReport() {

    if (!isProdBuild) {
        callQAApprovalReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.qapendingrejecteditemsandreorderanalysisreport) {
            callQAApprovalReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/QAPendingRejectedItems.js'],
                callback: function () {
                    callQAApprovalReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.qapendingrejecteditemsandreorderanalysisreport = true
                },
                scope: this
            });
        }
    }
}

function getReorderAnalysisMainTabView() {
    if (!isProdBuild) {
        getReorderAnalysisMainTabViewDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.qapendingrejecteditemsandreorderanalysisreport) {
            getReorderAnalysisMainTabViewDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/QAPendingRejectedItems.js'],
                callback: function () {
                    getReorderAnalysisMainTabViewDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.qapendingrejecteditemsandreorderanalysisreport = true
                },
                scope: this
            });
        }
    }
}


function callRevenueRecognitionReport() {
    if (!isProdBuild) {
        callRevenueRecognitionReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.revenuerecognitionreport) {
            callRevenueRecognitionReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/RevenueRecognitionReport.js'],
                callback: function () {
                    callRevenueRecognitionReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.revenuerecognitionreport = true
                },
                scope: this
            });
        }
    }

}


//function callSalesByServiceProductDetailReport(searchStr, filterAppend) {
//    if (!isProdBuild) {
//        callSalesByServiceProductDetailReportDynamicLoad(searchStr, filterAppend);
//    } else {
//        if (Wtf.ReportScriptLoadedFlag.salesbyserviceproductdetailreport) {
//            callSalesByServiceProductDetailReportDynamicLoad(searchStr, filterAppend);
//        } else {
//            ScriptMgr.load({
//                scripts: ['../../scripts/Reports/SalesByServiceProductDetailReport.js'],
//                callback: function () {
//                    callSalesByServiceProductDetailReportDynamicLoad(searchStr, filterAppend);
//                    Wtf.ReportScriptLoadedFlag.salesbyserviceproductdetailreport = true
//                },
//                scope: this
//            });
//        }
//    }
//
//}


function callSalesPersonCommissionDimensionReport() {
    if (!isProdBuild) {
        callSalesPersonCommissionDimensionReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.salespersoncommissiondimensionreport) {
            callSalesPersonCommissionDimensionReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/salesPersonCommissionDimensionReport.js'],
                callback: function () {
                    callSalesPersonCommissionDimensionReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.salespersoncommissiondimensionreport = true
                },
                scope: this
            });
        }
    }
}


function callSaleByItem() {
    if (!isProdBuild) {
        callSaleByItemDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.salesbyitemanddetailreport) {
            callSaleByItemDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SalesByItem.js', '../../scripts/Reports/SalesByItemDetail.js'],
                callback: function () {
                    callSaleByItemDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.salesbyitemanddetailreport = true
                },
                scope: this
            });
        }
    }
}


function  salesByProductCategoryDetailReport(searchStr, filterAppend) {
    if (!isProdBuild) {
        salesByProductCategoryDetailReportDynamicLoad(searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.salesbyproductcategoryreport) {
            salesByProductCategoryDetailReportDynamicLoad(searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SalesProductCategoryDetailReport.js'],
                callback: function () {
                    salesByProductCategoryDetailReportDynamicLoad(searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.salesbyproductcategoryreport = true
                },
                scope: this
            });
        }
    }
}
function  callDayEndCollectionReport(searchStr, filterAppend) {
    if (!isProdBuild) {
        callDayEndCollectionReportDynamicLoad(searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.dayendcollectionreport) {
            callDayEndCollectionReportDynamicLoad(searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/DayEndCollectionReport.js'],
                callback: function () {
                    callDayEndCollectionReportDynamicLoad(searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.dayendcollectionreport = true
                },
                scope: this
            });
        }
    }
}
function  callGSTHistoryInput(config) {
    if (!isProdBuild) {
        callGSTHistoryInputDynamicLoad(config);
    } else {
        if (Wtf.ReportScriptLoadedFlag.GSTHistoryInput) {
            callGSTHistoryInputDynamicLoad(config);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/EntityGst/GSTHistoryData.js'],
                callback: function() {
                    callGSTHistoryInputDynamicLoad(config);
                    Wtf.ReportScriptLoadedFlag.GSTHistoryInput = true
                },
                scope: this
            });
        }
    }
}
function  callGSTSalesTaxLiabilityReport(searchStr, filterAppend) {
    if (!isProdBuild) {
        callGSTSalesTaxLiabilityReportDynamicLoad(searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.gstsalestaxliabilityreport) {
            callGSTSalesTaxLiabilityReportDynamicLoad(searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/GSTSalesTaxLiabilityReport.js'],
                callback: function () {
                    callGSTSalesTaxLiabilityReportDynamicLoad(searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.gstsalestaxliabilityreport = true
                },
                scope: this
            });
        }
    }
}

function  callPendingApprovalsReport(isForForm03) {
    if (!isProdBuild) {
        callPendingApprovalsForAllModules(isForForm03);
    } else {
        if (Wtf.ReportScriptLoadedFlag.pendingapprovalreport) {
            callPendingApprovalsForAllModules(isForForm03);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/WtfPendingApproval.js'],
                callback: function () {
                    callPendingApprovalsForAllModules(isForForm03);
                    Wtf.ReportScriptLoadedFlag.pendingapprovalreport = true
                },
                scope: this
            });
        }
    }
}

function getStockAgeingTabView(params) {
    if(params == undefined ){
        params = {};
    }
    if (!isProdBuild) {
        getStockAgeingTabViewDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.stockageingreport) {
            getStockAgeingTabViewDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/StockAgeing.js'],
                callback: function () {
                    getStockAgeingTabViewDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.stockageingreport = true
                },
                scope: this
            });
        }
    }
}


function getStockLedgerTabView(params) {
    if(params == undefined ){
        params = {};
    }
    if (!isProdBuild) {
        getStockLedgerTabViewDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.stockledger) {
            getStockLedgerTabViewDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/StockLedger.js'],
                callback: function () {
                    getStockLedgerTabViewDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.stockledger = true
                },
                scope: this
            });
        }
    }
}

function getStockLedgerDetailedReport(params) {
    if(params == undefined ){
        params = {};
    }
    params.isStockLedgerDetailedReport =true;
    params.id ="StockLedgerDetailedReport";
    if (!isProdBuild) {
        getStockLedgerTabViewDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.stockledger) {
            getStockLedgerTabViewDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/StockLedger.js'],
                callback: function () {
                    getStockLedgerTabViewDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.stockledger = true
                },
                scope: this
            });
        }
    }
}

function StockReportOnDimension(params) {
    if(params == undefined ){
        params = {};
    }
    if (!isProdBuild) {
        StockReportOnDimensionDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.stockmovementreportadvancesearch) {
            StockReportOnDimensionDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/StockReportOnDimension.js'],
                callback: function () {
                    StockReportOnDimensionDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.stockmovementreportadvancesearch = true
                },
                scope: this
            });
        }
    }
}
function ChallanWiseReport(searchStr, filterAppend) {
    if (!isProdBuild) {
        ChallanReport(searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.ChallanWiseReport) {
            ChallanReport(searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/ChallanWiseReport.js'],
                callback: function () {
                    ChallanReport(searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.ChallanWiseReport = true
                },
                scope: this
            });
        }
    }
}

function JWProductSummaryWiseReport(searchStr, filterAppend) {
    if (!isProdBuild) {
        JWPProductSummaryReport(searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.JWProductSummaryWiseReport) {
            JWPProductSummaryReport(searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/JWProductSummaryReport.js'],
                callback: function () {
                    JWPProductSummaryReport(searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.JWProductSummaryWiseReport = true
                },
                scope: this
            });
        }
    }
}
function showStockStatusReportTab(params) {
    if(params == undefined ){
        params = {};
    }
    if (!isProdBuild) {
        showStockStatusReportTabDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.stockstatusreport) {
            showStockStatusReportTabDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/StockStatus.js'],
                callback: function () {
                    showStockStatusReportTabDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.stockstatusreport = true
                },
                scope: this
            });
        }
    }
}

function getStockValuationDetailReportTabView(record, type) {
    if (!isProdBuild) {
        getStockValuationDetailReportTabViewDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.stockvaluationdetailreport) {
            getStockValuationDetailReportTabViewDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/StockValuationDetailReport.js'],
                callback: function () {
                    getStockValuationDetailReportTabViewDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.stockvaluationdetailreport = true
                },
                scope: this
            });
        }
    }
}

function getStockValuationDetailsReport(params) {
    if(params == undefined ){
        params = {};
    }
    if (!isProdBuild) {
        getStockValuationDetailsReportDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.stockvaluationdetailreport) {
            getStockValuationDetailsReportDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/StockValuationDetailReport.js'],
                callback: function () {
                    getStockValuationDetailsReportDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.stockvaluationdetailreport = true
                },
                scope: this
            });
        }
    }
}


function getLocationSummaryReportTabView(params) {
    if(params == undefined ){
        params = {};
    }
    if (!isProdBuild) {
        getLocationSummaryReportTabViewDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.stockvaluationsummaryreport) {
            getLocationSummaryReportTabViewDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/LocationSummary.js'],
                callback: function () {
                    getLocationSummaryReportTabViewDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.stockvaluationsummaryreport = true
                },
                scope: this
            });
        }
    }
}

function calltopProductsByCustomers() {
    if (!isProdBuild) {
        calltopProductsByCustomersDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.topanddormantcustomervendorandprosuctreport) {
            calltopProductsByCustomersDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/TopAndDormantUsers.js'],
                callback: function () {
                    calltopProductsByCustomersDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.topanddormantcustomervendorandprosuctreport = true
                },
                scope: this
            });
        }
    }
}


function calltopCustomersByProducts() {
    if (!isProdBuild) {
        calltopCustomersByProductsDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.topanddormantcustomervendorandprosuctreport) {
            calltopCustomersByProductsDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/TopAndDormantUsers.js'],
                callback: function () {
                    calltopCustomersByProductsDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.topanddormantcustomervendorandprosuctreport = true
                },
                scope: this
            });
        }
    }
}


function calltopVendorsByProducts() {
    if (!isProdBuild) {
        calltopVendorsByProductsDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.topanddormantcustomervendorandprosuctreport) {
            calltopVendorsByProductsDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/TopAndDormantUsers.js'],
                callback: function () {
                    calltopVendorsByProductsDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.topanddormantcustomervendorandprosuctreport = true
                },
                scope: this
            });
        }
    }
}


function getTradingAndProfitLossWithBudget(){
    if (!isProdBuild) {
        getTradingAndProfitLossWithBudgetDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.tradingandprofitlosswithbudget) {
            getTradingAndProfitLossWithBudgetDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/TradingAndProfitLossWithBudget.js'],
                callback: function () {
                    getTradingAndProfitLossWithBudgetDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.tradingandprofitlosswithbudget = true
                },
                scope: this
            });
        }
    }
}


function PaymentTermSalesCommissionReport(){

    if (!isProdBuild) {
        PaymentTermSalesCommissionReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.salescommissionpaymentterm) {
            PaymentTermSalesCommissionReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SalesCommissionStatementForPaymentTerm.js','../../scripts/Reports/salesCommissionDetailReport.js'],
                callback: function () {
                    PaymentTermSalesCommissionReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.salescommissionpaymentterm = true
                },
                scope: this
            });
        }
    }

}


function callMonthlyCommissionOfSalesPersonReport() {
    if (!isProdBuild) {
        callMonthlyCommissionOfSalesPersonReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlycommissionofsalespersonreport) {
            callMonthlyCommissionOfSalesPersonReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/MonthlyCommissionOfSalesPerson.js'],
                callback: function () {
                    callMonthlyCommissionOfSalesPersonReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.monthlycommissionofsalespersonreport = true
                },
                scope: this
            });
        }
    }
}

function getDeliveryPlannerTabView(moduleid, billid) {
    if (!isProdBuild) {
        getDeliveryPlannerTabViewDynamicLoad(moduleid, billid);
    } else {
        if (Wtf.ReportScriptLoadedFlag.deliveryplannerreport) {
            getDeliveryPlannerTabViewDynamicLoad(moduleid, billid);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/DeliveryPlanner.js'],
                callback: function () {
                    getDeliveryPlannerTabViewDynamicLoad(moduleid, billid);
                    Wtf.ReportScriptLoadedFlag.deliveryplannerreport = true
                },
                scope: this
            });
        }
    }
}

function AccountForecastReportDetails(){
    if (!isProdBuild) {
        AccountForecastReportDetailsDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.accountforecastingreport) {
            AccountForecastReportDetailsDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/WtfAccountForecasting.js','../../scripts/Reports/MonthlySalesReport.js'],
                callback: function () {
                    AccountForecastReportDetailsDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.accountforecastingreport = true
                },
                scope: this
            });
        }
    }
}

function getIndividualDriverDeliveryReportTabView(record) {
    if (!isProdBuild) {
        getIndividualDriverDeliveryReportTabViewDynamicLoad(record);
    } else {
        if (Wtf.ReportScriptLoadedFlag.vehicledeliverysummaryreport) {
            getIndividualDriverDeliveryReportTabViewDynamicLoad(record);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/VehicleDeliverySummaryReport.js'],
                callback: function () {
                    getIndividualDriverDeliveryReportTabViewDynamicLoad(record);
                    Wtf.ReportScriptLoadedFlag.vehicledeliverysummaryreport = true
                },
                scope: this
            });
        }
    }
}
    
function getVehicleDeliverySummaryReportTabView() {
  if (!isProdBuild) {
        getVehicleDeliverySummaryReportTabViewDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.vehicledeliverysummaryreport) {
            getVehicleDeliverySummaryReportTabViewDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/VehicleDeliverySummaryReport.js'],
                callback: function () {
                    getVehicleDeliverySummaryReportTabViewDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.vehicledeliverysummaryreport = true
                },
                scope: this
            });
        }
    }
}

function getIndividualVehicleDeliveryReportTabView(record) {
   if (!isProdBuild) {
        getIndividualVehicleDeliveryReportTabViewDynamicLoad(record);
    } else {
        if (Wtf.ReportScriptLoadedFlag.vehicledeliverysummaryreport) {
            getIndividualVehicleDeliveryReportTabViewDynamicLoad(record);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/VehicleDeliverySummaryReport.js'],
                callback: function () {
                    getIndividualVehicleDeliveryReportTabViewDynamicLoad(record);
                    Wtf.ReportScriptLoadedFlag.vehicledeliverysummaryreport = true
                },
                scope: this
            });
        }
    }
}

function getIndividualVehicleDOPOReportTabView(record) {
  if (!isProdBuild) {
        getIndividualVehicleDOPOReportTabViewDynamicLoad(record);
    } else {
        if (Wtf.ReportScriptLoadedFlag.vehicledeliverysummaryreport) {
            getIndividualVehicleDOPOReportTabViewDynamicLoad(record);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/VehicleDeliverySummaryReport.js'],
                callback: function () {
                    getIndividualVehicleDOPOReportTabViewDynamicLoad(record);
                    Wtf.ReportScriptLoadedFlag.vehicledeliverysummaryreport = true
                },
                scope: this
            });
        }
    }
}

function getDriverDeliverySummaryReportTabView() {
    if (!isProdBuild) {
        getDriverDeliverySummaryReportTabViewDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.vehicledeliverysummaryreport) {
            getDriverDeliverySummaryReportTabViewDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/VehicleDeliverySummaryReport.js'],
                callback: function () {
                    getDriverDeliverySummaryReportTabViewDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.vehicledeliverysummaryreport = true
                },
                scope: this
            });
        }
    }
}


function getIndividualDriverDOPOReportTabView(record) {
  if (!isProdBuild) {
        getIndividualDriverDOPOReportTabViewDynamicLoad(record);
    } else {
        if (Wtf.ReportScriptLoadedFlag.vehicledeliverysummaryreport) {
            getIndividualDriverDOPOReportTabViewDynamicLoad(record);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/VehicleDeliverySummaryReport.js'],
                callback: function () {
                    getIndividualDriverDOPOReportTabViewDynamicLoad(record);
                    Wtf.ReportScriptLoadedFlag.vehicledeliverysummaryreport = true
                },
                scope: this
            });
        }
    }
}

function getSalesPurchaseTabView(searchStr,filterAppend){
     if (!isProdBuild) {
        getSalesPurchaseTabViewDynamicLoad(searchStr,filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.salespurchasehistoryreport) {
            getSalesPurchaseTabViewDynamicLoad(searchStr,filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SalesPurchaseReport.js'],
                callback: function () {
                    getSalesPurchaseTabViewDynamicLoad(searchStr,filterAppend);
                    Wtf.ReportScriptLoadedFlag.salespurchasehistoryreport = true
                },
                scope: this
            });
        }
    }
}

function callPriceVarianceReport(searchStr, filterAppend) {
    if (!isProdBuild) {
        callPriceVarianceReportDynamicLoad(searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.pricevariancereport) {
            callPriceVarianceReportDynamicLoad(searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/PriceVarianceReport.js'],
                callback: function () {
                    callPriceVarianceReportDynamicLoad(searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.pricevariancereport = true
                },
                scope: this
            });
        }
    }
}

function getVendorProductPriceReportTabView(){
     if (!isProdBuild) {
        callVendorProductPriceReport();
    } else {
        if (Wtf.ReportScriptLoadedFlag.vendorProductPriceReport) {
            callVendorProductPriceReport();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/VendorProductPriceListReport.js'],
                callback: function () {
                    callVendorProductPriceReport();
                    Wtf.ReportScriptLoadedFlag.vendorProductPriceReport = true
                },
                scope: this
            });
        }
    }
}

function  CustomColumnLineDetail(searchStr, filterAppend) {
    if (!isProdBuild) {
        CustomColumnLineDetail(searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.customdetailreport) {
            CustomColumnLineDetail(searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CustomDetailReport.js'],
                callback: function() {
                    CustomColumnLineDetail(searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.customdetailreport = true
                },
                scope: this
            });
        }
    }
}
function callCommonReportView(reportid, reportname) {
    if (!isProdBuild) {
        callCommonReportDynamicLoad(reportid, reportname);
    } else {
        if (Wtf.ReportScriptLoadedFlag.callCommonReport) {
            callCommonReportDynamicLoad(reportid, reportname);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CommonReport.js'],
                callback: function () {
                    callCommonReportDynamicLoad(reportid, reportname);
                    Wtf.ReportScriptLoadedFlag.callCommonReport = true
                },
                scope: this
            });
        }
    }
}


function callGstFormGenerationHistoryReportDynamicLoad() {
    if (!isProdBuild) {
        callGstFormGenerationHistoryReport();
    } else {
        if (Wtf.ReportScriptLoadedFlag.gstFormGenerationHistory) {
            callGstFormGenerationHistoryReport();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/GstFormGenerationHistory.js'],
                callback: function() {
                    callGstFormGenerationHistoryReport();
                    Wtf.ReportScriptLoadedFlag.gstFormGenerationHistory = true
                },
                scope: this
            });
        }
    }
}

function  callCustomerRegistryReport(searchStr, filterAppend) {
    if (!isProdBuild) {
        callCustomerRegistryReportDynamicLoad(searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.customerregisteryreport) {
            callCustomerRegistryReportDynamicLoad(searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/customervendorregistry.js'],
                callback: function () {
                    callCustomerRegistryReportDynamicLoad(searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.customerregisteryreport = true
                },
                scope: this
            });
        }
    }
}

function  callVendorRegistryReport(searchStr, filterAppend) {
    if (!isProdBuild) {
        callVendorRegistryReportDynamicLoad(searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.vendorregisteryreport) {
            callVendorRegistryReportDynamicLoad(searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/customervendorregistry.js'],
                callback: function () {
                    callVendorRegistryReportDynamicLoad(searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.vendorregisteryreport = true
                },
                scope: this
            });
        }
    }
}
    
function getAccountingandtaxperiod() {
    if (!isProdBuild) {
        getTaxPeriod();
    } else {
        if (Wtf.ReportScriptLoadedFlag.taxperiodsettings) {
            getTaxPeriod();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/TaxPeriodTab.js', '../../scripts/Reports/TaxPeriodGrid.js', '../../scripts/Reports/CreateTaxPeriodWindow.js'],
                callback: function () {
                    getTaxPeriod();
                    Wtf.ReportScriptLoadedFlag.taxperiodsettings = true
                },
                scope: this
            });
        }
    }
}

function  callProductTransactionDetail(productId) {
    if (!isProdBuild) {
        callProductTransactionDetailReport(productId);
    } else {
        if (Wtf.ReportScriptLoadedFlag.producttransactiondetailreport) {
            callProductTransactionDetailReport(productId);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/ProductTransactionDetail.js'],
                callback: function() {
                    callProductTransactionDetailReport(productId);
                    Wtf.ReportScriptLoadedFlag.producttransactiondetailreport = true
                },
                scope: this
            });
        }
    }
}
function callPriceListBandReport(searchStr, filterAppend) {
    if (!isProdBuild) {
        callPriceListBandReportDynamicLoad(searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.pricelistbandreport) {
            callPriceListBandReportDynamicLoad(searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/PriceListBandReport.js'],
                callback: function () {
                    callPriceListBandReportDynamicLoad(searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.pricelistbandreport = true
                },
                scope: this
            });
        }
    }
}

function callMonthwiseGeneralLedger() {
    if (!isProdBuild) {
        callMonthwiseGeneralLedgerReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthWiseGeneralLedger) {
            callMonthwiseGeneralLedgerReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/MonthwiseGeneralLedger.js'],
                callback: function () {
                    callMonthwiseGeneralLedgerReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.monthWiseGeneralLedger = true
                },
                scope: this
            });
        }
    }
}

function callSalesAnalysis_TopCustomers() {
    if (!isProdBuild) {
        callSalesAnalysisReportDynamicLoad(true,false,false,WtfGlobal.getLocaleText("acc.header.SAtopcustomers"));
    } else {
        if (Wtf.ReportScriptLoadedFlag.SalesAnalysis) {
            callSalesAnalysisReportDynamicLoad(true,false,false,WtfGlobal.getLocaleText("acc.header.SAtopcustomers"));
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SalesAnalysis.js'],
                callback: function () {
                    callSalesAnalysisReportDynamicLoad(true,false,false,WtfGlobal.getLocaleText("acc.header.SAtopcustomers"));
                    Wtf.ReportScriptLoadedFlag.SalesAnalysis = true
                },
                scope: this
            });
        }
    }
}

function callSalesAnalysis_TopProducts() {
    if (!isProdBuild) {
        callSalesAnalysisReportDynamicLoad(false,true,false,WtfGlobal.getLocaleText("acc.header.SAtopproducts"));
    } else {
        if (Wtf.ReportScriptLoadedFlag.SalesAnalysis) {
            callSalesAnalysisReportDynamicLoad(false,true,false,WtfGlobal.getLocaleText("acc.header.SAtopproducts"));
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SalesAnalysis.js'],
                callback: function () {
                    callSalesAnalysisReportDynamicLoad(false,true,false,WtfGlobal.getLocaleText("acc.header.SAtopproducts"));
                    Wtf.ReportScriptLoadedFlag.SalesAnalysis = true
                },
                scope: this
            });
        }
    }
}

function callSalesAnalysis_TopAgents() {
    if (!isProdBuild) {
        callSalesAnalysisReportDynamicLoad(false,false,true,WtfGlobal.getLocaleText("acc.header.SAtopagents"));
    } else {
        if (Wtf.ReportScriptLoadedFlag.SalesAnalysis) {
            callSalesAnalysisReportDynamicLoad(false,false,true,WtfGlobal.getLocaleText("acc.header.SAtopagents"));
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SalesAnalysis.js'],
                callback: function () {
                    callSalesAnalysisReportDynamicLoad(false,false,true,WtfGlobal.getLocaleText("acc.header.SAtopagents"));
                    Wtf.ReportScriptLoadedFlag.SalesAnalysis = true
                },
                scope: this
            });
        }
    }
}

function getAccountingandtaxperoid() {
    if (!isProdBuild) {
        getAccountingPeriod();
    } else {
        if (Wtf.ReportScriptLoadedFlag.accountingperiodsettings) {
            getAccountingPeriod();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/accountingPeriodTab.js','../../scripts/Reports/AccountingPeriodGrid.js','../../scripts/Reports/CheckColumnComponent.js','../../scripts/Reports/CreateAccountingPeriodWindow.js'],
                callback: function () {
                    getAccountingPeriod();
                    Wtf.ReportScriptLoadedFlag.accountingperiodsettings = true
                },
                scope: this
            });
        }
    }
}

/**
 * For calling Product Brand Discount Window.
 */
function callProductBrandDiscount(record) {
    if (!isProdBuild) {
        callProductBrandDiscountDynamicLoad(record);
    } else {
        if (Wtf.ReportScriptLoadedFlag.productbranddiscountwin) {
            callProductBrandDiscountDynamicLoad(record);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/ProductBrandDiscount.js'],
                callback: function () {
                    callProductBrandDiscountDynamicLoad(record);
                    Wtf.ReportScriptLoadedFlag.productbranddiscountwin = true
                },
                scope: this
            });
        }
    }
}

/**
 * For calling Test Mail Window.
 */
function callTestMailWindow() {
    if (!isProdBuild) {
        callTestMailWindowDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.productbranddiscountwin) {
            callTestMailWindowDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/TestMailWindow.js'],
                callback: function () {
                    callTestMailWindowDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.testmailwindow = true
                },
                scope: this
            });
        }
    }
}


function getCostAndSellingPriceOfItemsToCustomer() {
    if (!isProdBuild) {
        getCostAndSellingPriceOfItemsToCustomerDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.costAndSellingPriceOfItemsToCustomer) {
            getCostAndSellingPriceOfItemsToCustomerDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CostAndSellingPriceOfItemsToCustomer.js'],
                callback: function() {
                    getCostAndSellingPriceOfItemsToCustomerDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.costAndSellingPriceOfItemsToCustomer = true
                },
                scope: this
            });
        }
    }
}
//Wtf.account.SalesPurhcaseAnnexureReport
function callSalesPurhcaseAnnexureReport() {
    if (!isProdBuild) {
        callSalesPurhcaseAnnexureLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.salesPurhcaseAnnexureReport) {
            callSalesPurhcaseAnnexureLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SalesPurhcaseAnnexureReport.js'],
                callback: function () {
                    callSalesPurhcaseAnnexureLoad();
                    Wtf.ReportScriptLoadedFlag.salesPurhcaseAnnexureReport = true
                },
                scope: this
            });
        }
    }
}

function getTDSChallanControlReport() {
    if (!isProdBuild) {
        getTDSChallanControlReport();
    } else {
        if (Wtf.ReportScriptLoadedFlag.plaReport) {
            getTDSChallanControlReport();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/TDSChallanControlReport.js'],
                callback: function () {
                    getTDSChallanControlReport();
                    Wtf.ReportScriptLoadedFlag.TDSChallanControlReport = true
                },
                scope: this
            });
        }
    }
}
// -- callVATAndCSTCalculationReport

function callVATAndCSTCalculationReport() {
    if (!isProdBuild) {
        callOutPutVATAndCSTCalculationReportLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.vatAndCSTCalculationReport) {
            callOutPutVATAndCSTCalculationReportLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/VATAndCSTCalculationReport.js'],
                callback: function () {
                    callOutPutVATAndCSTCalculationReportLoad();
                    Wtf.ReportScriptLoadedFlag.vatAndCSTCalculationReport = true
                },
                scope: this
            });
        }
    }
}
/* Indian Company - DVAT Form 31*/
function getFormDVAT31Report(record) { 
    if (!isProdBuild) {
        getFormDVAT31ReportDynamicLoad(record);
    } else {
        if (Wtf.ReportScriptLoadedFlag.formDVAT31Report) {
            getFormDVAT31ReportDynamicLoad(record);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/DVATForm31Report.js'],
                callback: function () {
                    getFormDVAT31ReportDynamicLoad(record);
                    Wtf.ReportScriptLoadedFlag.formDVAT31Report = true
                },
                scope: this
            });
        }
    }
}
function callTDSMasterRates(tabid) { 
    if (!isProdBuild) {
        callTDSMasterRatesDynamicLoad(tabid);
    } else {
        if (Wtf.ReportScriptLoadedFlag.TDSMasterRates) {
            callTDSMasterRatesDynamicLoad(tabid);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/TDSMasterRates.js'],
                callback: function () {
                    callTDSMasterRatesDynamicLoad(tabid);
                    Wtf.ReportScriptLoadedFlag.TDSMasterRates = true;
                },
                scope: this
            });
        }
    }
}
function  callSalesCommissionSchemaReport(params) {
    if(params == undefined){
        params={};
    }
    params.isprofitreport = false;
    if (!isProdBuild) {
        SalesCommissionDimensionReportLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.salesCommissionReport) {
            SalesCommissionDimensionReportLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SalesCommissionOnDimension.js'],
                callback: function () {
                    SalesCommissionDimensionReportLoad(params);
                    Wtf.ReportScriptLoadedFlag.salesCommissionReport = true
                },
                scope: this
            });
        }
    }
}

function  callJobProfitabilityReport(params) {
    if(params == undefined){
        params={};
    }
    params.isprofitreport = true;
    if (!isProdBuild) {
        SalesCommissionDimensionReportLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.salesprofitReport) {
            SalesCommissionDimensionReportLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/SalesCommissionOnDimension.js'],
                callback: function () {
                    SalesCommissionDimensionReportLoad(params);
                    Wtf.ReportScriptLoadedFlag.salesprofitReport = true
                },
                scope: this
            });
        }
    }
}

function callCheckInandCheckOutReport(paramsObj) {
    if(paramsObj == undefined ||paramsObj == null){
        paramsObj={};
    }
    if (!isProdBuild) {
        checkInCheckOutReportLoad(paramsObj);
    } else {
        if (Wtf.ReportScriptLoadedFlag.checkInCheckOutReport) {
            checkInCheckOutReportLoad(paramsObj);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CheckInCheckOutReport.js'],
                callback: function () {
                    checkInCheckOutReportLoad(paramsObj);
                    Wtf.ReportScriptLoadedFlag.checkInCheckOutReport = true
                },
                scope: this
            });
        }
    }
}
/*
 * Below function is used to call Incidense Cases report details
 */
function callIncidentCasesReport(paramsObj) {
    if(paramsObj == undefined ||paramsObj == null){
        paramsObj={};
    }
    if (!isProdBuild) {
        checkIncidentCasesReportLoad(paramsObj);
    } else {
        if (Wtf.ReportScriptLoadedFlag.checkIncidentCasesReport) {
            checkIncidentCasesReportLoad(paramsObj);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/CheckInCheckOutReport.js'],
                callback: function () {
                    checkIncidentCasesReportLoad(paramsObj);
                    Wtf.ReportScriptLoadedFlag.checkIncidentCasesReport = true
                },
                scope: this
            });
        }
    }
}
function callCustomerReceivedReport() {
    if (!isProdBuild) {
        callCustomerReceivedReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.CustomerReceivedReport) {
            callCustomerReceivedReportDynamicLoad(false,true,false,WtfGlobal.getLocaleText("acc.header.SAtopproducts"));
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/mainscripts/CustomerReceivedReport.js'],
                callback: function () {
                    callCustomerReceivedReportDynamicLoad(false,true,false,WtfGlobal.getLocaleText("acc.header.SAtopproducts"));
                    Wtf.ReportScriptLoadedFlag.CustomerReceivedReport = true
                },
                scope: this
            });
        }
    }
}
function callLandingCostItemReport() {
    if (!isProdBuild) {
        callLandingCostItemReportDynamicLoad();
    } else {
        if (Wtf.ReportScriptLoadedFlag.LandingCostItemReport) {
            callLandingCostItemReportDynamicLoad();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/mainscripts/LandingCostItemReport.js'],
                callback: function () {
                    callLandingCostItemReportDynamicLoad();
                    Wtf.ReportScriptLoadedFlag.LandingCostItemReport = true
                },
                scope: this
            });
        }
    }
}

function callmonthlyCustomLayout(templateid, templatetitle, statementType, reportid, searchStr, filterAppend) {
    if (!isProdBuild) {
        callMonthlyCustomLayoutDynamicLoad(templateid, templatetitle, statementType, reportid, searchStr, filterAppend);
    } else {
        if (Wtf.ReportScriptLoadedFlag.monthlycustomlayout) {
            callMonthlyCustomLayoutDynamicLoad(templateid, templatetitle, statementType, reportid, searchStr, filterAppend);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/Reports/WtfMonthlyCustomLayout.js'],
                callback: function () {
                    callMonthlyCustomLayoutDynamicLoad(templateid, templatetitle, statementType, reportid, searchStr, filterAppend);
                    Wtf.ReportScriptLoadedFlag.monthlycustomlayout = true
                },
                scope: this
            });
        }
    }
}
/*
 * Below function is used to call disocunt details Window when user clicks the icon at transaction level
 */
function callDiscountDetailsDynamic(paramsObj) {
    if (!isProdBuild) {
        callDiscountDetails(paramsObj);
    } else {
        if (Wtf.ReportScriptLoadedFlag.callDiscountDetailsWindow) {
            callDiscountDetails(paramsObj);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/mainscripts/WtfDiscountDetailsWindow.js'],
                callback: function () {
                    callDiscountDetails(paramsObj);
                    Wtf.ReportScriptLoadedFlag.callDiscountDetailsWindow = true
                },
                scope: this
            });
        }
    }
}

function  callGSTOutputRuleReport(params) {
    if (params == undefined) {
        var params = {};
    }
    params.id = "GSTOutputRuleReport";
    params.tabtitle = "acc.gstrr.output.tabtitle";
    if (!isProdBuild) {
        callGSTRuleReportDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.gstOutputRuleReport) {
            callGSTRuleReportDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/EntityGst/EntityGSTReports/callGSTRuleReport.js'],
                callback: function () {
                    callGSTRuleReportDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.gstOutputRuleReport = true
                },
                scope: this
            });
        }
    }
}

function  callGSTInputRuleReport(params) {
    if (params == undefined) {
        var params = {};
    }
    params.id = "GSTInputRuleReport";
    params.tabtitle = "acc.gstrr.input.tabtitle";
    if (!isProdBuild) {
        callGSTRuleReportDynamicLoad(params);
    } else {
        if (Wtf.ReportScriptLoadedFlag.gstOutputRuleReport) {
            callGSTRuleReportDynamicLoad(params);
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/EntityGst/EntityGSTReports/callGSTRuleReport.js'],
                callback: function () {
                    callGSTRuleReportDynamicLoad(params);
                    Wtf.ReportScriptLoadedFlag.gstOutputRuleReport = true
                },
                scope: this
            });
        }
    }
}
/**
 * Load GSTR1,GSTR2 and MisMatch Summary report JS dynamically 
 * @param {type} gstrreporttype
 * @returns {undefined}
 */
function  callGSTRSummaryReport(gstrreporttype) {
    if (!isProdBuild) {
        if (gstrreporttype == 3) { // For MisMatch Report 
            callGSTRMisMatchSummary(gstrreporttype);
        } else {
            callGSTR1Summary(gstrreporttype); // For GSTR1 and GSTR2 report
        }
    } else {
        if (Wtf.ReportScriptLoadedFlag.GSTRSummaryReport) {
            if (gstrreporttype == 3) { // For MisMatch Report 
                callGSTRMisMatchSummary(gstrreporttype);
            } else {
                callGSTR1Summary(gstrreporttype); // For GSTR1 and GSTR2 report
            }
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/EntityGst/EntityGSTReports/GSTSummary.js'],
                callback: function () {
                    if (gstrreporttype == 3) { // For MisMatch Report 
                        callGSTRMisMatchSummary(gstrreporttype);
                    } else {
                        callGSTR1Summary(gstrreporttype); // For GSTR1 and GSTR2 report
                    }
                    Wtf.ReportScriptLoadedFlag.GSTRSummaryReport = true
                },
                scope: this
            });
        }
    }
}
/**
 * Load GSTR1,GSTR2 and MisMatch details report JS dynamically 
 * @param {type} config
 * @returns {undefined}
 */
function  callGSTRDetailedReport(config) {
    if (!isProdBuild) {
        callGSTR1SummaryDetails(config); // For GST MisMatch, GSTR1 and GSTR2 report
    } else {
        if (Wtf.ReportScriptLoadedFlag.GSTRDetailedReport) {
            callGSTR1SummaryDetails(config); // For GST MisMatch, GSTR1 and GSTR2 report
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/EntityGst/EntityGSTReports/GSTSummaryDetails.js'],
                callback: function () {
                    callGSTR1SummaryDetails(config); // For GST MisMatch, GSTR1 and GSTR2 report
                    Wtf.ReportScriptLoadedFlag.GSTRDetailedReport = true
                },
                scope: this
            });
        }
    }
}
/**
 * Load GST Computation report JS dynamically 
 * @param {type} config
 * @returns {undefined}
 */
function  callGSTComputationReportDynamic() {
    if (!isProdBuild) {
        callGSTComputationReport();
    } else {
        if (Wtf.ReportScriptLoadedFlag.GSTComputationReport) {
            callGSTComputationReport();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/EntityGst/EntityGSTReports/GSTComputationReport.js'],
                callback: function () {
                    callGSTComputationReport();
                    Wtf.ReportScriptLoadedFlag.GSTComputationReport = true
                },
                scope: this
            });
        }
    }
}
/**
 * Load GSTR3B summary report JS dynamically 
 * @param {type} config
 * @returns {undefined}
 */
function  callGSTR3BSummaryReportDynamic() {
    if (!isProdBuild) {
        callGSTR3BSummaryReport();
    } else {
        if (Wtf.ReportScriptLoadedFlag.GSTR3BSummaryReport) {
            callGSTR3BSummaryReport();
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/EntityGst/EntityGSTReports/GSTR3BSummaryReport.js'],
                callback: function () {
                    callGSTR3BSummaryReport();
                    Wtf.ReportScriptLoadedFlag.GSTR3BSummaryReport = true
                },
                scope: this
            });
        }
    }
}
/**
 * Load GSTR3B summary report JS dynamically 
 * @param {type} config
 * @returns {undefined}
 */
function  callGSTR3BAndComputationDetailedReport(config, gstrreporttype) {
    if (!isProdBuild) {
        if (gstrreporttype == 1) { // GST Computation Report
            callGSTComputationDetails(config);
        } else if (gstrreporttype == 2) { // GSTR3B Report
            callGSTR3BDetails(config);
        }
    } else {
        if (Wtf.ReportScriptLoadedFlag.GSTR3BAndComputationDetailedReport) {
            if (gstrreporttype == 1) { // GST Computation Report
                callGSTComputationDetails(config);
            } else if (gstrreporttype == 2) { //GSTR3B report
                callGSTR3BDetails(config);
            }
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/EntityGst/EntityGSTReports/GSTR3BDetailReport.js'],
                callback: function () {
                    if (gstrreporttype == 1) { // GST Computation Report
                        callGSTComputationDetails(config);
                    } else if (gstrreporttype == 2) { // GSTR3B Report
                        callGSTR3BDetails(config);
                    }
                    Wtf.ReportScriptLoadedFlag.GSTR3BAndComputationDetailedReport = true
                },
                scope: this
            });
        }
    }
}
/**
 * Load GSTR2MatchAndReconcile and callGSTR2AComparison report JS dynamically 
 * @param {type} config
 * @returns {undefined}
 */
function  callGSTR2MatchAndComparisionReport(gstrreporttype) {
    if (!isProdBuild) {
        if (gstrreporttype == 1) { // Match and Reconcile
            callGSTR2MatchAndReconcile();
        } else if (gstrreporttype == 2) { // GSTR2 Comparision
            callGSTR2AComparison();
        }
    } else {
        if (Wtf.ReportScriptLoadedFlag.GSTR2MatchAndComparisionReport) {
            if (gstrreporttype == 1) { // Match and Reconcile
                callGSTR2MatchAndReconcile();
            } else if (gstrreporttype == 2) { // GSTR2 Comparision
                callGSTR2AComparison();
            }
        } else {
            ScriptMgr.load({
                scripts: ['../../scripts/EntityGst/EntityGSTReports/GSTR2MatchAndReconcile.js'],
                callback: function () {
                    if (gstrreporttype == 1) { // Match and Reconcile
                        callGSTR2MatchAndReconcile();
                    } else if (gstrreporttype == 2) { // GSTR2 Comparision
                        callGSTR2AComparison();
                    }
                    Wtf.ReportScriptLoadedFlag.GSTR2MatchAndComparisionReport = true
                },
                scope: this
            });
        }
    }
}

function callGSTForm5eSubmissionDetailsForm(params) {
    if (Wtf.ReportScriptLoadedFlag.GSTForm5eSubmissionFunction || !isProdBuild) {
        gstForm5ComponentCall(params);
    } else {
        ScriptMgr.load({
            scripts: ['../../scripts/Reports/GSTeSubmissionDetails.js'],
            callback: function () {
                gstForm5ComponentCall(params);
                Wtf.ReportScriptLoadedFlag.GSTForm5eSubmissionFunction = true;
            },
            scope: this
        });
    }
}
/**
 * Load Philippines VAT Summary report JS dynamically 
 * @param {type} config
 * @returns {undefined}
 */
function  loadPhpVATSummaryReport() {
    var params = {};
    params.title = WtfGlobal.getLocaleText("acc.statutoryPanel.philippines.vat.summary.report");
    params.titleQtip = WtfGlobal.getLocaleText("acc.statutoryPanel.philippines.vat.summary.report");
    params.reportID = "phpVATSummaryReport";
    params.reportStoreURL = "ACCPhilippinesCompliance/getVATSummaryReportData.do";
    params.reportType = 1; // 1 for Summary Type
    if (Wtf.ReportScriptLoadedFlag.phpVATReport || !isProdBuild) {
        phpVATReport(params);
    } else {
        ScriptMgr.load({
            scripts: ['../../scripts/PhilippinesCompliance/phpVATReport.js'],
            callback: function () {
                phpVATReport(params);
                Wtf.ReportScriptLoadedFlag.phpVATReport = true
            },
            scope: this
        });
    }
}
/**
 * Load Indonesia VAT Summary report JS dynamically 
 * @param {type} config
 * @returns {undefined}
 */
function  loadIndonesiaVATSummaryReport() {
    var params = {};
    params.title = WtfGlobal.getLocaleText("acc.statutoryPanel.indonesia.vat.out.report");
    params.titleQtip = WtfGlobal.getLocaleText("acc.statutoryPanel.indonesia.vat.out.report");
    params.reportID = "IndonesiaVATSummaryReport";
    params.reportStoreURL = "ACCIndonesiaCompliance/exportVATOutReportData.do";
    if (Wtf.ReportScriptLoadedFlag.IndonesiaVATReport || !isProdBuild) {
        indonesiaVATReport(params);
    } else {
        ScriptMgr.load({
            scripts: ['../../scripts/IndonesiaCompliance/indonesiaVATReport.js'],
            callback: function () {
                indonesiaVATReport(params);
                Wtf.ReportScriptLoadedFlag.IndonesiaVATReport = true
            },
            scope: this
        });
    }
}
function callGSTForm5eSubmissionHistory() {
    if (Wtf.ReportScriptLoadedFlag.GSTForm5eSubmissionFunction || !isProdBuild) {
        GSTForm5eSubmissionDetails();
    } else {
        ScriptMgr.load({
            scripts: ['../../scripts/Reports/GSTeSubmissionDetails.js'],
            callback: function () {
                GSTForm5eSubmissionDetails();
                Wtf.ReportScriptLoadedFlag.GSTForm5eSubmissionFunction = true;
            },
            scope: this
        });
    }
}    

function callIAFfileWindowforeSubmissionHistory() {
    if (Wtf.ReportScriptLoadedFlag.GSTForm5eSubmissionFunction || !isProdBuild) {
        GSTTransactionListingSubmissionDetails();
    } else {
        ScriptMgr.load({
            scripts: ['../../scripts/Reports/GSTeSubmissionDetails.js'],
            callback: function () {
                GSTTransactionListingSubmissionDetails();
                Wtf.ReportScriptLoadedFlag.GSTForm5eSubmissionFunction = true;
            },
            scope: this
        });
    }
}
function loadPHPPurchasesReliefSummaryReport() {
    var params = {};
    params.title = WtfGlobal.getLocaleText("acc.php.reliefreport.purchase");
    params.titleQtip = WtfGlobal.getLocaleText("acc.php.reliefreport.purchase");
    params.reportID = Wtf.PHPReportType.PurchaseRelief + "Report";
    params.reportStoreURL = "ACCPhilippinesCompliance/getPurchaseReliefReport.do";
    params.reportType = Wtf.PHPReportType.PurchaseRelief; // PurchaseReliefSummary
    if (!isProdBuild || Wtf.ReportScriptLoadedFlag.callPhilippiensReliefReports) {
        callPhilippiensReliefReports(params);
    } else {
        ScriptMgr.load({
            scripts: ['../../scripts/PhilippinesCompliance/PHPReliefReports.js'],
            callback: function () {
                callPhilippiensReliefReports(params);
                Wtf.ReportScriptLoadedFlag.callPhilippiensReliefReports = true;
            },
            scope: this
        });
    }
}
function loadPHPSalesReliefSummaryReport() {
    var params = {};
    params.title = WtfGlobal.getLocaleText("acc.php.reliefreport.sale");
    params.titleQtip = WtfGlobal.getLocaleText("acc.php.reliefreport.sale");
    params.reportID = Wtf.PHPReportType.SalesRelief + "Report";
    params.reportStoreURL = "ACCPhilippinesCompliance/getSalesReliefReport.do";
    params.reportType = Wtf.PHPReportType.SalesRelief; // SalesReliefSummary
    if (!isProdBuild || Wtf.ReportScriptLoadedFlag.callPhilippiensReliefReports) {
        callPhilippiensReliefReports(params);
    } else {
        ScriptMgr.load({
            scripts: ['../../scripts/PhilippinesCompliance/PHPReliefReports.js'],
            callback: function () {
                callPhilippiensReliefReports(params);
                Wtf.ReportScriptLoadedFlag.callPhilippiensReliefReports = true;
            },
            scope: this
        });
    }
}
