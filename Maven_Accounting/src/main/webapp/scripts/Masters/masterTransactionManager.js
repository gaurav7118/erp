function callMasterCustomLayoutGrid(filterTemplate) {
    var reportList = Wtf.getCmp("mastercustomLayoutGrid"+filterTemplate);
    if (reportList == null) {
        reportList = new Wtf.customLayoutList({
            id: "mastercustomLayoutGrid"+filterTemplate,
            closable: true,
            filterTemplate:filterTemplate,
            iconCls :getButtonIconCls(Wtf.etype.reportList),
            modal: true
        });

        Wtf.getCmp('as').add(reportList);
    }
    Wtf.getCmp('as').setActiveTab(reportList);
    Wtf.getCmp('as').doLayout();

}

function callMasterPricelistWindow(pricePersonType, isEdit, priceRec) {
//    if ((!Wtf.account.companyAccountPref.productPricingOnBands && pricePersonType == "Vendor") || ((!Wtf.account.companyAccountPref.productPricingOnBandsForSales || Wtf.account.companyAccountPref.bandsWithSpecialRateForSales) && pricePersonType == "Customer") || ((!Wtf.account.companyAccountPref.productPricingOnBandsForSales || !Wtf.account.companyAccountPref.productPricingOnBands) && pricePersonType == "") || isEdit) {
    /**
     * Special Rates are not applicable as Product Pricing List on Bands For Sales/purchase option is activated in System Controls.
     */
    if (((!Wtf.account.companyAccountPref.productPricingOnBands || Wtf.account.companyAccountPref.columnPref.bandsWithSpecialRateForPurchase ) && pricePersonType == "Vendor") || ((!Wtf.account.companyAccountPref.productPricingOnBandsForSales || Wtf.account.companyAccountPref.bandsWithSpecialRateForSales) && pricePersonType == "Customer") || (!isEdit && pricePersonType == "") || isEdit) {
        var winid = "pricewindow";
        var panel = Wtf.getCmp(winid);
        if (!panel) {
            new Wtf.account.MasterPricelistWindow({
                title: isEdit ? WtfGlobal.getLocaleText("acc.field.editPrice") : WtfGlobal.getLocaleText("acc.field.Setprice"),
                id: winid,
                closable: true,
                modal: true,
                pricePersonType: pricePersonType,
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                width: 450,
                autoScroll: true,
                height: 325,
                resizable: false,
                layout: 'border',
                buttonAlign: 'right',
                renderTo: document.body,
                isEdit: isEdit,
                priceRec: priceRec
            }).show();
        }
    } else {
        if (pricePersonType == "Vendor") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.product.specialRates.access")], 3);
        } else if (pricePersonType == "Customer") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.product.specialRates.accessForSales")], 3);
        }
    }
}

function callOnlyDimensionConfiguration(dimension,customColumn,tabid){
    var panel = Wtf.getCmp(tabid);
    if(panel==null){
        panel = new Wtf.account.MasterConfigurator({
            layout : "fit",
            showonlyDimension:dimension,
            isShowCustomFieldonly:customColumn,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.masterConfig.tabTitleTT"),  //'You can Add Master Items for various Master Groups from here.',
            helpmodeid:31,
            onlyMaster:true,
            border : false,
             id : tabid,
            //id : "masterconfiguration",
            iconCls:'accountingbase masterconfiguration',
            closable: true
        });
    }
    Wtf.getCmp('as').add(panel);
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();    
    return panel ;
}

function callPersonGrouping(isCustomerGrouping, ByCategoryStore, isPricingBandGrouping, isBySalesPersonOrAgent){
    var p = Wtf.getCmp("personsgroupinglinkforaccounting");
    var winTitle = "";
    if(isBySalesPersonOrAgent){
        if(isCustomerGrouping){
            winTitle=WtfGlobal.getLocaleText("acc.cust.editSalesPerson");
        }else{
            winTitle=WtfGlobal.getLocaleText("acc.vend.editAgent");
        }
    }else{
        if(isPricingBandGrouping){
            winTitle=(WtfGlobal.getLocaleText("acc.common.edit") + " " + WtfGlobal.getLocaleText("acc.field.pricingBandMaster"));
        }else{
            if(isCustomerGrouping){
                winTitle=WtfGlobal.getLocaleText("acc.cust.editCategoty");
            }else{
                winTitle=WtfGlobal.getLocaleText("acc.vend.editCategory");
            }
        }
    }
    if(!p){
        new Wtf.persongroupingwin({
            title:winTitle,
            isCustomerGrouping:isCustomerGrouping,
            ByCategoryStore:ByCategoryStore,
            isPricingBandGrouping: isPricingBandGrouping,
            isBySalesPersonOrAgent : isBySalesPersonOrAgent
        }).show();
    }
}

function callNewVendorByCategoryReport(){
    var Reportpanel= Wtf.getCmp("NewVendorByCategoryDetails");
    if(Reportpanel==null){
        Reportpanel= new Wtf.account.BusinessPersonListByCategory({
            id: 'NewVendorByCategoryDetails',
            border: false,
            layout: 'fit',
            isCustomer: false,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.vendorList.tabTitleCategory"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.vendorList.tabTitleCategory"), //"Vendor List By Category",
            iconCls:'accountingbase vendor',
            closable: true
        });
        Wtf.getCmp('as').add(Reportpanel);
    }
    Wtf.getCmp('as').setActiveTab(Reportpanel);
    Wtf.getCmp('as').doLayout();
}

function callNewCustomerByCategoryReport(){
    var panel= Wtf.getCmp("NewCustomerByCategoryDetails");
    if(panel==null){
        panel= new Wtf.account.BusinessPersonListByCategory({
            id: 'NewCustomerByCategoryDetails',
            border: false,
            layout: 'fit',
            isCustomer: true,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.customerList.tabTitleCategory"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.customerList.tabTitleCategory"),  //"Customer List By Category",
            iconCls: getButtonIconCls(Wtf.etype.customer),
            closable: true
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callNewProductByCategoryReport(){
    var Reportpanel= Wtf.getCmp("NewProductByCategoryDetails");
    if(Reportpanel==null){
        Reportpanel= new Wtf.account.ProductListByCategory({
            id: 'NewProductByCategoryDetails',
            border: false,
            layout: 'fit',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.prod.tabTitleCategory"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.prod.tabTitleCategory"), //"Product List By Category",
            iconCls:getButtonIconCls(Wtf.etype.product),
            closable: true
        });
        Wtf.getCmp('as').add(Reportpanel);
    }
    Wtf.getCmp('as').setActiveTab(Reportpanel);
    Wtf.getCmp('as').doLayout();
}
