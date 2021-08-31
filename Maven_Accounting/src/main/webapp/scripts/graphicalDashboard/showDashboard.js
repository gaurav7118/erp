/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
Wtf.reg('htmleditor', Wtf.form.HtmlEditor);
//getDashboard();
function getDashboard(){
    Wtf.Ajax.requestEx({
        url: "ACCUSDashboard/getDashboard.do",
        params :{
            isActive:true
        }
    }, this, function(resData,response) {
        if(resData.length > 0 ){
            showDashboard(resData);
        }else{
            openDefaultDashboard();
        }
    }, function() {});
}
/*
 * Get configuration json for product view
 */
chkcurrencyload();
function getConfiguredProductView(productId){
    chkProductBrandload();
    chkProductCategoryload();

    Wtf.Ajax.requestEx({
        url: "ACCUSDashboard/getDashboard.do",
        params :{
            isActive:true,
            isProductView : true
        }
    }, this, function(resData,response) {
        // if configuration present then show configured product view else show normal product view.
        if(resData.length > 0 ){
            try{
                showProductView(resData,productId);
            }catch(e){
                // if any exception occured while creating configured product view then show default product view.
                clog(e);
                Wtf.showProductDetails(productId);
            }
        }else{
            // open default product view
            Wtf.showProductDetails(productId);
        }
    }, function() {});
}

function showDashboard(response){
    var panel = Wtf.getCmp("dashboard");
    if(panel==null){
        panel = new Wtf.account.ShowDashboard({
            border: false,
            id :"dashboard",
            autoScroll: true,
            frame: false,
            response : response
        });

        panel.on("resize", function () {
            this.doLayout();
        }); 
        Wtf.getCmp("tabdashboard").on("activate",function(){
            panel.doLayout(); 
        });
    }
    Wtf.getCmp("tabdashboard").add(panel);
    Wtf.getCmp("tabdashboard").doLayout();
}

var productViewIdPrefix = "product-configure-view";
function showProductView(response,productid){
    
//    var title = productRec.get("productname");
//    var productid = productId;
    var panelId = productViewIdPrefix + productid;
    var panel = Wtf.getCmp(panelId);
    if(panel==null){
        panel = new Wtf.account.ShowDashboard({
            border: false,
            id :panelId,
            autoScroll: true,
            title :"Configure Product",
            frame: false,
            iconCls : "pwnd clock",
            productId:productid,
            closable : true,
//            record : productRec, // Used to load values in product forms
            response : response
        });

        panel.on("resize", function () {
            this.doLayout();
        }); 
        panel.on("activate", function () {
            this.doLayout();
        }); 
        Wtf.getCmp("as").add(panel);
    }else{
        // if product is already opened in view mode, then just refresh its values.
        panel.loadProduct(productid);
    }
    Wtf.getCmp("as").setActiveTab(panel);
    Wtf.getCmp("as").doLayout();
}

function refreshDashboard(resData){
    var panel = Wtf.getCmp("dashboard");
    if(panel !=undefined){
        Wtf.getCmp("tabdashboard").remove(panel);
        Wtf.getCmp("tabdashboard").doLayout();
        panel.destroy();
        panel = null;
    }
    
    if(resData[0].isactive){
        showDashboard(resData);
    }else{
        openDefaultDashboard();
    }
}
/*
 *JSON Object for handler functions that will work for links with scope of product view.
 **/
var handlerFunctions ={};

Wtf.account.ShowDashboard = Wtf.extend( Wtf.Panel,{
    autoScroll :true,
    dashboardJson :{},
    style : 'background-color: #f0f0f0;',
    initComponent:function() {
        
        // Added functions in handlerFunctions object that will work on links with scope of this object.
        handlerFunctions.getSalesHistory = getProductSalesHistory.createDelegate(this,[this.productId]);
        handlerFunctions.getStockMovement = getStockMovementWindow.createDelegate(this,[this.productId]);
        handlerFunctions.openSearchWin = openSearchDialog.createDelegate(this,[this.productId]);
            
        this.createItems();
        this.createTbar();
        this.createBbar();
        this.on("render",function(){
            this.loadMask = new Wtf.LoadMask(this.getEl(),{
                msg:"Loading product"
            }); 
            if(this.productId){
                this.loadProduct(this.productId)
            }
        });
        
        Wtf.account.ShowDashboard.superclass.initComponent.apply(this, arguments);
    },
    onDestroy:function() {
        /*
         * On closing this form destroy all components which are used for support.
         */ 
        if(this.tagsFieldset){
            this.tagsFieldset.destroy();
        }
        var dummyProductForm = Wtf.getCmp("dummyProductReport");
        if(dummyProductForm){
            dummyProductForm.destroy();
        }
        if(this.productEditor){
            this.productEditor.destroy();
        }
        
        Wtf.account.ShowDashboard.superclass.onDestroy.call(this);
    },
    createTbar : function(){
        var tbarArr = [];
        var config ={
            displayField: 'productname',
            extraFields:['pid','type']
        };
        this.productEditor = CommonERPComponent.createProductPagingComboBox(250,150,Wtf.ProductCombopageSize,this,{},false,false,config);
        tbarArr.push("Select product",this.productEditor,"-");
        
        this.fetchBtn = new Wtf.Button({
            text : WtfGlobal.getLocaleText("acc.common.search"),
            iconCls : "advanceSearchButton",
            tooltip : WtfGlobal.getLocaleText("acc.setPriceForPriceListBand.QuickSearchEmptyText"),
            scope :this,
            handler : function(){
                if(this.productEditor.getValue()){
                    this.resetForms();
                    this.loadProduct(this.productEditor.getValue());
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.prod.comboEmptytext")], 2);
                }
            }
        });
        tbarArr.push(this.fetchBtn);
        
        this.tbar = tbarArr;
    },
    createBbar : function(){
        var bbarArr = [];
        
        this.saveBtn = new Wtf.Button({
            text : "Save",
            iconCls : "pwnd save",
            scope :this,
            handler : function(){
                this.validateForms();
//                this.loadProduct(this.productEditor.getValue());
            }
        });
        bbarArr.push(this.saveBtn);
        
        this.bbar = bbarArr;
    },
    loadProduct : function(productId){
        //flags to check if all fields are loaded or not. if loaded then remove load mask.
        this.isProductDetailsLoaded = false;
        this.isTotalPurchasesLoaded = false;
        this.isInvDetailsLoaded = false;
        
        if(this.loadMask){
            this.loadMask.show();
        }
        var pid = productId;
        var stDate = WtfGlobal.getDates(true) || new Date();
        var eDate = WtfGlobal.getDates(false) || new Date();
        var startDate = WtfGlobal.convertToGenericDate(stDate.add(Date.DAY, 0));
        var endDate = WtfGlobal.convertToGenericDate(eDate.add(Date.DAY, 0));
            
        Wtf.Ajax.requestEx({
            url: "ACCProduct/getProducts.do",
            params: {
                includeParent:true, // subproduct didn't gets open in view case
                isProductView:true, 
                mode:22,
                ids:pid                    
            }
        }, this, function(response,request){
            var recc=eval(response.data);   
            var rec;
            rec=eval({
                data:recc[0]
            });
            this.productData = rec.data || {};
            this.productId = this.productData.productid;
            var title = "[" + this.productData.pid+"] "+this.productData.productname ;
            this.title = "<span wtf:qtip ='View details of product "+title+"'>"+title+"- Product Details <span>";
            this.setTitle(this.title);
            var productRec = WtfGlobal.searchRecord(this.productEditor.store,this.productId,this.productEditor.valueField);
            // Add record if not present as productEditor store is remote store. 
            if(productRec == undefined){
                var recConfig = {};
                recConfig[this.productEditor.valueField] = this.productId;
                recConfig[this.productEditor.displayField] = this.productData[this.productEditor.displayField];
                var prorec = new Wtf.data.Record(recConfig);
                this.productEditor.store.add(prorec);
            }
            this.productEditor.setValue(this.productId);
            
            //Handle none condition for product category.
            var productCategory = this.productData.productCategory;
            productCategory = productCategory ? productCategory.split(","):[];
            if(productCategory.length > 0){
                
                for(var index =0;index < productCategory.length;index++){
                    if(!productCategory[index]){
                        productCategory[index] = "None";
                    }
                }
                this.productData.productCategory = productCategory.join(",");
            }else{
                this.productData.productCategory = "None";
            }
            var task = new Wtf.util.DelayedTask(function() {
                this.loadRecords(this.productData);
                this.loadForms(this.productData);
                this.loadImageComponent(this.productData);
                this.isProductDetailsLoaded = true;
                
                // apply validations after loading form. Also calculate some fields like actual cost.
                this.applyFormValidations();
                this.calculateValues();
                
                this.hideLoadMask();
                // Handler functions for Create, Edit and Delete product.
                handlerFunctions.createNewProduct = createEditDeleteProduct.createDelegate(this,[false]);
                handlerFunctions.editProduct = createEditDeleteProduct.createDelegate(this,[true,false,this.productData]);
                handlerFunctions.deleteProduct = createEditDeleteProduct.createDelegate(this,[false,false,this.productData,true]);
            }, this);
            task.delay(100);
        //            
        }); 
            
            
        Wtf.Ajax.requestEx({
            url : "ACCGoodsReceiptCMN/getPurchaseByVendor.do",
            params :{
                start:0,
                limit:15,
                startdate : startDate,
                enddate : endDate,
                prodfiltercustid:"All",
                productid:-1,
                productCategoryid:-1,
                salesPersonid:-1,
                customerCategoryid:-1,
                productids : pid
            }
        }, this, function(response){
            var recArr=eval(response.data);
            var totalPurchases = 0;
            for(var i=0;i<recArr.length ;i++ ){
                if(recArr[i].rowquantity){
                    var quantity = parseFloat(recArr[i].rowquantity)
                    if(!isNaN(quantity - quantity)){
                        totalPurchases += quantity;
                    }
                }
            }
            var totalpurchases = WtfGlobal.quantityRenderer(totalPurchases);
            //Show negative items in red and in bracket.
            totalpurchases =  totalPurchases >= 0 ? totalpurchases : "(<span style='color:red;'>"+totalpurchases+"</span>)";
            
            var task = new Wtf.util.DelayedTask(function() {
                this.loadTotalPurchaseWidget({
                    "totalPurchases":totalpurchases
                });
                this.isTotalPurchasesLoaded = true;
                this.hideLoadMask();
            }, this);
            task.delay(100);

        });
            
        Wtf.Ajax.requestEx({
            url : "ACCUSDashboard/getProductViewInvDetails.do",
            params :{
                start:0,
                limit:15,
                startdate : startDate,
                enddate : endDate,
                prodfiltercustid:"All",
                productCategoryid:-1,
                salesPersonid:-1,
                customerCategoryid:-1,
                productid : pid
            }
        }, this, function(response){
            var recArr=eval(response.data);
            var rec = recArr[0] || {};
            var task = new Wtf.util.DelayedTask(function() {
                this.loadInventoryWidget(rec);
                this.loadForms(rec);
                this.isInvDetailsLoaded = true;
                this.hideLoadMask();
            }, this);
            task.delay(100);

        });
    },
    createItems : function(){
        var panelData = this.response[0];
        var json = JSON.parse(panelData.json)
        var section = json.section
        var items =[];
        //Used to store ids of components so that we can set values to each object easily.
        this.componentIds = {
            imageComponentIds :[],
            widgetComponentIds :[],
            totalPurchasewidgetId :[],
            inventoryWidgetIds :[],
            formComponentIds : []
        }
        
        for(var i=0;i<section.length;i++){
            var row = clone(section[i]);
            var rowHeight = row.height-3;
            var columns = row.items;
            var rowItems =[];
            if(columns.length > 0){
                for(var col = 0 ; col < columns.length ; col++){
                    var columnConfig = clone(columns[col]);
                    columnConfig.cls = "widget";
                    var height = columnConfig.items.height;
                    var columnItems = [];
                
                    var selectedWidget = columnConfig.items.selectedWidget;
                    if(selectedWidget != undefined){
                        selectedWidget.parentId = this.id;
                        var widget = createDashboardWidget(selectedWidget,this.componentIds,this.parentObj);
                        if(widget !=undefined){
                            columnItems.push(widget);
                            columnConfig.items = columnItems;
                        }
                    }
                    columnConfig.height = height;
                    rowItems.push(new Wtf.Panel(columnConfig));
                    if(height > rowHeight){
                        rowHeight = height; 
                    }
                }
                row.height = height+7;
                row.items = rowItems;
                items.push(new Wtf.Panel(row));
            }
        }
        
        this.items = items;
    },
    
    loadForms : function(record){
        var component,tpl;
        var formIds = this.componentIds.formComponentIds || [];
        for(var i =0;i<formIds.length;i++){
            component = Wtf.getCmp(formIds[i]);
            component.getForm().setValues(record);
        }
    },
    getFormsValue : function(record){
        var component;
        // Array to maintain information of custom fields added in view.
        this.customFieldsArr = [];
        var formIds = this.componentIds.formComponentIds || [];
        for(var i =0;i<formIds.length;i++){
            component = Wtf.getCmp(formIds[i]);
            Wtf.apply(this.productData,component.getForm().getValues());
            if(component.customFields){
                this.customFieldsArr = this.customFieldsArr.concat(component.customFields ||[]);
            }
        }
        return this.productData;
    },
    resetForms : function(){
        var component;
        var formIds = this.componentIds.formComponentIds || [];
        for(var i =0;i<formIds.length;i++){
            component = Wtf.getCmp(formIds[i]);
            component.getForm().reset();
        }
    },
    loadRecords : function(record){
        var widgetComponentIds = this.componentIds.widgetComponentIds || [];
        this.loadXtemplates(widgetComponentIds,record);
    },
    loadImageComponent : function (record){ 
        var imagePanelIds = this.componentIds.imageComponentIds || [];
        this.loadXtemplates(imagePanelIds,record);
    },
    loadTotalPurchaseWidget : function(record){
        var widgetComponentIds = this.componentIds.totalPurchasewidgetId || [];
        this.loadXtemplates(widgetComponentIds,record);
    },
    loadInventoryWidget : function(record){
        var widgetComponentIds = this.componentIds.inventoryWidgetIds || [];
        this.loadXtemplates(widgetComponentIds,record);
    },
    loadXtemplates : function(componentIds,record){
        var component;
        var tpl;
        for(var i =0;i<componentIds.length;i++){
            component = Wtf.getCmp(componentIds[i]);
            if(component.tpl){
                tpl = component.tpl;
                tpl.overwrite(component.body, record);
            }
        }
    },
    hideLoadMask : function(){
        /*
         *If all things are loaded then remove load mask.
         */
        if(this.isProductDetailsLoaded && this.isTotalPurchasesLoaded && this.isInvDetailsLoaded){
            this.loadMask.hide();
        }
    },
    calculateValues : function(){
        this.currencyCombo = Wtf.getCmp(this.id +"currencyid");
        var exchangeRate = Wtf.getCmp(this.id +"exchangerate");
        if(exchangeRate && this.currencyCombo && this.currencyCombo.getValue()){
            var rec = WtfGlobal.searchRecord(this.currencyCombo.store,this.currencyCombo.getValue(),"currencyid");
            if(rec){
                exchangeRate.setValue(rec.get("exchangerate"));
            }
        }
        if(this.currencyCombo){
            if(this.productData.producttype == Wtf.producttype.assembly || this.productData.producttype == Wtf.producttype.customerAssembly){
                this.currencyCombo.disable(); 
            }else{
                this.currencyCombo.enable(); 
            }
        }
        
        this.getFormsValue(); // used to get custom fields info as freight costand other cost are custom fields.
        var freightCostObj = this.customFieldsArr.getIemtByParam({
            fieldlabel : "Freight Cost"
        });
        var otherCostObj = this.customFieldsArr.getIemtByParam({
            fieldlabel: "Other Cost"
        });
        
        // Actual cost is sum of purchase cost, other cost and freight cost.
        var actualCost = Wtf.getCmp(this.id +"actualcost");
        var purchaseCost = Wtf.getCmp(this.id +"purchaseprice");
        if(freightCostObj && otherCostObj && actualCost){
            var freightCost = Wtf.getCmp(freightCostObj.id);
            var otherCost = Wtf.getCmp(otherCostObj.id);
            if(freightCost && otherCost && purchaseCost){
                function setActualCost(){
                    var actualCostValue = (freightCost.getValue() || 0) + (otherCost.getValue()|| 0) + (purchaseCost.getValue()|| 0);
                    actualCost.setValue(actualCostValue);
                }
                
                setActualCost();
                purchaseCost.on("change",setActualCost);
                freightCost.on("change",setActualCost);
                otherCost.on("change",setActualCost);
                
            }
        }
    },
    applyFormValidations : function(){
        var productBrand = Wtf.getCmp(this.id +"productBrandName");
        if(productBrand){ //edit case of assembly product
            if((this.productData.isUsedInTransaction && this.productData.producttype == Wtf.producttype.assembly)){
                productBrand.disable(); 
            }else{
                productBrand.enable(); 
            }
        }
        
        if(Wtf.getCmp(this.id +"pid")){
            if(this.productData.sequenceformatid){
                Wtf.getCmp(this.id +"pid").disable();
            }else{
                Wtf.getCmp(this.id +"pid").enable();
            }
        }
        
        this.productName = Wtf.getCmp(this.id +"productname");
        
        this.purchaseCost = Wtf.getCmp(this.id +"purchaseprice");
        if(this.purchaseCost ){ //edit case of assembly product
            if((this.productData.producttype == Wtf.producttype.assembly || this.productData.producttype == Wtf.producttype.customerAssembly || this.productData.isUsedInTransaction)){
                this.purchaseCost.disable(); //It will be always disable either product is used or not 
            }else{
                this.purchaseCost.enable(); //It will be always disable either product is used or not 
            }
        }
        
        this.currencyCombo = Wtf.getCmp(this.id +"currencyid");
        var exchangeRate = Wtf.getCmp(this.id +"exchangerate");
        if( this.currencyCombo && exchangeRate){
             this.currencyCombo.on("change",function(combo,newVal,oldVal){
                var rec = WtfGlobal.searchRecord(combo.store,newVal,"currencyid");
                exchangeRate.setValue(rec.get("exchangerate"));
            });
        }
    },
    validateForms : function(){
        var component;
        var isValidForms = true;
        try{
            var formIds = this.componentIds.formComponentIds || [];
            for(var i =0;i<formIds.length;i++){
                component = Wtf.getCmp(formIds[i]);
                var isValid = component.getForm().isValid();
                if(!isValid && isValidForms){
                    isValidForms = isValid;
                }
            }
        }catch(e){
            clog(e);
            isValidForms = false;
        }
        
        if(!isValidForms){
            // Check whether values are valid or not.
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasefillinthenecessaryinformation.")],2);
            return;
        }
        
        // Check currency is fill up or not.
        if(this.currencyCombo &&  !this.currencyCombo.getRawValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasefillinthenecessaryinformation.")],2);
            try{
                this.currencyCombo.markInvalid();
            }catch(e){
                clog(e);
            }
            return;
        }
        
        
        var format = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]+/;
        // Check whether product name contains any special keywords. Get confirmation from user.
        if(this.productName && format.test(this.productName.getValue())){
            Wtf.MessageBox.confirm("Confirm","Product name having some special characters, do you want to save ?", function(btn){
                if(btn == 'yes') {
                    this.saveForms();
                }
            },this);
        }else{
            this.saveForms();
        }
    },
    saveForms : function(){
        // Get product data to save
        var productData = this.getProductJsonToSave();
        if(this.loadMask){
            this.loadMask.show();
        }
        
        Wtf.Ajax.requestEx({
            url: "ACCProductCMN/saveProduct.do",//edit case call
            method : "POST",
            params: productData
        },this,this.genSuccessResponse,this.genFailureResponse);
    },
    getProductJsonToSave : function(){
        //Get forms values 
        this.productData = this.getFormsValue();
        var data = this.productData || {};
        data.sequenceformat = this.productData.sequenceformatid ? this.productData.sequenceformatid : "NA";
        
        data.isFromProductView = true;
        
        //Get custom data info.
        var custFieldArr=this.getCustomFieldsValues();
        if (custFieldArr.length > 0)
            data.customfield = JSON.stringify(custFieldArr);
        
        return data;
    },
    getCustomFieldsValues : function(){
        //Using existing function to get information about custom fields.
        if(!this.tagsFieldset){
            this.tagsFieldset = new Wtf.account.CreateCustomFields({
                border: false,
                autoHeight: true,
                parentcompId: this.id,
                moduleid: Wtf.Acc_Product_Master_ModuleId,
                isEdit: this.isEdit,
                record: this.record
            });
            this.tagsFieldset.dimensionFieldArray = [];
            this.tagsFieldset.dimensionFieldArrayValues = [];
            this.tagsFieldset.customFieldArray = this.customFieldsArr;
            this.tagsFieldset.customFieldArrayValues = this.customFieldsArr;
        }
        return this.tagsFieldset.createFieldValuesArray();
    },
    genSuccessResponse:function(response){
        if(this.loadMask){
            this.loadMask.hide();
        }
        if(response.success){
            var titleMsg = WtfGlobal.getLocaleText("acc.invReport.prod");
            WtfComMsgBox([titleMsg,response.msg],0,undefined,undefined);
            if(this.productData.currencyid && this.productData.purchaseprice && this.productData.productid && this.productData.asofdate){
                
                var pricerec = {};
                pricerec.carryin = true;
                pricerec.productid = this.productData.productid;
                pricerec.changeprice = true;							// To change existing price			Neeraj
                pricerec.price = this.productData.purchaseprice;
                pricerec.mode = 11;
                //                        pricerec.applydate=WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime(true));
                pricerec.applydate = WtfGlobal.convertToGenericDate(new Date(this.productData.asofdate));
                //While creating product, asOfDate is saved as 'updatedate' in inventory, & also should be in 'applydate' in pricelist.
                pricerec.currencyid = this.productData.currencyid;
                Wtf.Ajax.requestEx({
                    url:"ACCProduct/setNewPrice.do",
                    params: pricerec
                },this,function(){},this.genPriceFailureResponse);
            }
            
        }else if (response.isDuplicateExe) {
            // Check for duplicate product code. If it gets duplicated then take another product code from user.
            this.newnowin = new Wtf.Window({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                closable: true,
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                width: 330,
                autoHeight: true,
                modal: true,
                bodyStyle: "background-color:#f1f1f1;",
                buttonAlign: 'right',
                items: [new Wtf.Panel({
                    border: false,
                    html: (response.msg.length > 60) ? response.msg : "<br>" + response.msg,
                    height: 50,
                    bodyStyle: "background-color:white; padding: 7px; font-size: 11px; border-bottom: 1px solid #bfbfbf;"
                }),
                this.newdoForm = new Wtf.form.FormPanel({
                    labelWidth: 190,
                    border: false,
                    autoHeight: true,
                    bodyStyle: 'padding:10px 5px 3px; ',
                    autoWidth: true,
                    defaultType: 'textfield',
                    items: [this.newdono = new Wtf.form.TextField({
                        fieldLabel: WtfGlobal.getLocaleText("acc.product.newproductid"),
                        allowBlank: false,
                        labelSeparator: '',
                        width: 90,
                        itemCls: 'nextlinetextfield',
                        name: 'newdono',
                        id: 'newdono'
                    })],
                    buttons: [{
                        text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                        handler: function () {
                            if (this.newdono.validate()) {
                                if(Wtf.getCmp(this.id +"pid")){
                                    Wtf.getCmp(this.id +"pid").setValue(this.newdono.getValue());
                                }
                                this.saveForms();
                                this.newnowin.close();
                            }
                        },
                        scope: this
                    }, {
                        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                        scope: this,
                        handler: function () {
                            this.newnowin.close();
                        }
                    }]
                })]
            });
            this.newnowin.show();
        }else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.invReport.prod"),response.msg],2);
        }
  
    },
    genFailureResponse:function(response){
        if(this.loadMask){
            this.loadMask.hide();
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            //        this.fireEvent('closed',this);
    },
    genPriceFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
    
});

function createDashboardWidget (selectedWidget,componentIds,parentObj){
    var widget;
    var widgetType = selectedWidget.type;
        
    switch (widgetType){
        case "chart" :
            widget = createChart(selectedWidget,componentIds);
            break;
        case "form" :
            widget = createFormWidget(selectedWidget,componentIds);
            componentIds.formComponentIds.push(widget.id);
            break;
        case "image" :
            widget = createImageWidget(selectedWidget,componentIds);
            componentIds.imageComponentIds.push(widget.id);
            break;
        case "reportlist" :
            widget = createReportListForProduct(selectedWidget,componentIds);
            componentIds.inventoryWidgetIds.push(widget.id);
            break;
        case "totalpurchases" :
            widget = createTotalPurchasesWidget(selectedWidget,componentIds);
            componentIds.totalPurchasewidgetId.push(widget.id);
            break;
        case "productfeatures" :
            widget = createProductFeaturesPanel(selectedWidget,componentIds,parentObj);
            componentIds.widgetComponentIds.push(widget.id);
            break;
        case "grid" :
            if(selectedWidget.subtype == "hierachical"){
                widget = createHierachicalGrid(selectedWidget);
            }
            break;
        default :
            widget = createDefaultPanel(selectedWidget,componentIds);
            break;
    }
    return widget;
}

function createChart(selectedWidget){
    var dataUrl = selectedWidget.dataUrl;
    var params = selectedWidget.params;
    
   
    var startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    
    var endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false)
    });
    
    var fetchBtn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),
        tooltip : WtfGlobal.getLocaleText("acc.invReport.fetchTT"), 
        iconCls:'accountingbase fetch'
    });
    var tbar =[];
    tbar.push(WtfGlobal.getLocaleText("acc.common.from"));
    tbar.push(startDate);
    tbar.push("-");
    tbar.push(WtfGlobal.getLocaleText("acc.common.to"));
    tbar.push(endDate);
    tbar.push("-");
    tbar.push(fetchBtn);
    
    var chartConfig = getChartConfig(selectedWidget);
//    chartConfig["export"].fileName = selectedWidget.chartname;
//    chartConfig.titles[0].text = selectedWidget.chartname;
    var subtext = "From "+ startDate.getValue().format(startDate.format) +" To "+endDate.getValue().format(endDate.format);
    
    chartConfig.titles[1]={
        text : subtext,
        bold : false,
        "size": 11
    }
        
    var chart = new Wtf.Chart({
        dataUrl:dataUrl,
        tbar :tbar,
        params:params,
        selectedWidget : selectedWidget,
        border:false,
        chartConfig:chartConfig ,
        height: 390
    });
    return chart;
}

function createDefaultPanel(selectedWidget,componentIds){
    var panelItems;
    
    if(selectedWidget.items && Array.isArray(selectedWidget.items)){
        panelItems = [];
        var widgetItems = selectedWidget.items;
        for(var i=0;i < widgetItems.length ;i++){
            widgetItems[i].parentId = selectedWidget.parentId;
            var comp = createDashboardWidget(widgetItems[i],componentIds);
            panelItems.push(comp);
        }
        selectedWidget.items = panelItems;
    }else{
        delete selectedWidget["items"];
    }
    
    
    var panel = new Wtf.Panel(selectedWidget);
    return panel;
}

function createFormWidget(selectedWidget){
    var formItems = [];
    var customFieldArr = [];
    var layoutOfForm;
    var noOfColumns;
    var layoutConfig;
    layoutOfForm = selectedWidget.layout;
    if(selectedWidget.layoutConfig){
        layoutConfig = selectedWidget.layoutConfig || {};
        noOfColumns = selectedWidget.layoutConfig.columns;
    }
    
    if(selectedWidget.items && selectedWidget.items.length > 0){
        var widgetItems = selectedWidget.items;
        if(noOfColumns && noOfColumns > 0 && layoutOfForm == "column"){
            var columnWidth = 1/noOfColumns;
            var columnsArray = [];
            for (var colCnt = 0;colCnt <noOfColumns;colCnt++){
                columnsArray.push({
                    columnWidth : columnWidth,
                    layout : "form",
                    border : false,
                    style :"margin : 2px;",
                    items :[]
                })
            }
        }
        
        var dimensionArr = GlobalColumnModelForReports[Wtf.Acc_Product_Master_ModuleId];
        for(var i = 0;i < widgetItems.length;i++ ){
            var item = widgetItems[i];
            if (item.xtype){
                var xtype ="textfield";
                xtype = WtfGlobal.getXType(item.xtype);
                item.xtype = xtype;
            }
            if(item.store && typeof item.store == "string"){
                item.store = eval(item.store);
            }
            if(item.maskRe && typeof item.maskRe == "string"){
                item.maskRe = eval(item.maskRe);
            }
            if(item.customField && dimensionArr){
                var dimensionObj = dimensionArr.getIemtByParam({
                    fieldlabel:item.customField
                });
                if(dimensionObj){
                    dimensionObj = JSON.clone(dimensionObj);
                    xtype = WtfGlobal.getXType(dimensionObj.fieldtype);
                    item.xtype = xtype;
                    item.name = dimensionObj.fieldname || item.name;
                    item.fieldLabel = dimensionObj.fieldlabel || item.fieldLabel;
                    
                    if(xtype == "combo"){
                        // create store for drop down type custom fields. 
                        var url = "ACCAccountCMN/getCustomCombodata.do";
                        var baseParams = {
                            mode: 2,
                            flag: 1,
                            fieldid: dimensionObj.fieldid
                        }
                        var comboStore = new Wtf.data.Store({
                            url: url,
                            baseParams: baseParams,
                            reader: new Wtf.data.KwlJsonReader({
                                root: 'data'
                            }, Wtf.ComboReader),
                            autoLoad:true
                        });
                        comboStore.on("load",function(){
                            var noneRecord = new Wtf.data.Record({
                                id :"1234",
                                name :"None"
                            });
                            this.insert(0,noneRecord);
                        });
                        item.name = dimensionObj.fieldname + "_value";
                        item.store = comboStore;
                        item.mode = "local";
                        item.triggerAction = "all";
                        item.valueField= 'id';
                        item.displayField= 'name';
                    }
                    item.id = (selectedWidget.parentId || "") + dimensionObj.fieldid;
                    dimensionObj.id = (selectedWidget.parentId || "") + dimensionObj.fieldid;
                    customFieldArr.push(dimensionObj);
                }
            }else if(item.name && selectedWidget.parentId){
                item.id = (selectedWidget.parentId || "") + (item.name);
            }
            item.border = false;
//            item.readOnly = true;
            
            if(columnsArray && columnsArray.length == noOfColumns && layoutOfForm == "column"){
                columnsArray[(i%noOfColumns)].items.push(item);
            }else{
                formItems.push(item)
            }
            
        }
    }
    var comp = Wtf.Panel;
    if(formItems.length > 0){
        comp = Wtf.form.FormPanel;
    }
    if(columnsArray && columnsArray.length == noOfColumns){
        formItems = columnsArray;
    }
    
    var formConfig = {
        autoScroll : true,
        border:false,
        anchorSize : 100,
        height: selectedWidget.height || 390,
        customFields : customFieldArr,
        items : formItems.length > 0 ?  formItems : undefined 
    };
    
    
    Wtf.apply(formConfig,selectedWidget);
    
        
    var form = new comp(formConfig);
    return form;
}


function createImageWidget(selectedWidget){
        
    var imagePanel = new Wtf.Panel({
        bodyStyle : "padding:20px;overflow:auto;",
        autoScroll : true,
        border:false,
        tpl :new Wtf.XTemplate('<tpl>',
            '<div style="height:200px;width:260px;">',
            '<img src="productimage?loadtime='+new Date().getTime()+'&fname={productid}.png" style="width: 100%;height: auto;" alt="No Image has uploaded" />',
            '</div>',            
            '</tpl>'),
        height: selectedWidget.height || 390
    });
    return imagePanel;
}
function createReportListForProduct(selectedWidget){
    
    // Stock on hand
    var contentHtml = '<tpl>'
    contentHtml+='<table style = "border-spacing: 15px 21px;"> <tbody><tr>'
       
    contentHtml += '<td valign="top" style="width : 33%;">';
    contentHtml += '<table><tbody><tr><td valign="top"><img src="images/productView/stock_on_hand.png"></td><td valign="top"><div>Stock On Hand </div> <div style="font-size: 16px;color: #1567B9;">  {stockOnHand} </div></td></tr></tbody></table>';
    contentHtml += '</td>';
    //Last Out standing
    contentHtml += '<td valign="top" style="width : 33%;">';
    contentHtml += '<table><tbody><tr><td valign="top"><img src="images/productView/last_outstanding.png"></td><td valign="top"><div>Outstanding Purchase Orders </div> <div style="font-size: 18px;color: #1567B9;">{outStandingPOs}</div></td></tr></tbody></table>';
    contentHtml += '</td>';
    // Last Stock in
    contentHtml += '<td valign="top" style="width : 33%;">';
    contentHtml += '<table><tbody><tr><td valign="top"><img src="images/productView/last_outstanding.png"></td><td valign="top"><div>Last Stock-In Date & Quantity  </div> <div style="font-size: 16px;color: #1567B9;">{lastStockInDate}</div></td></tr></tbody></table>'
    contentHtml += '</td>';
    
    contentHtml   += '</tr><tr>';
    // Stock Location
    contentHtml += '<td valign="top" style="width : 33%;">';
    contentHtml += '<table><tbody><tr><td valign="top"><img src="images/productView/stock_location.png"></td><td valign="middle"><div style="font-size: 16px;color: #1567B9;">Stock Location </div> <div>{locationDetails}</div></td></tr></tbody></table>';
    contentHtml += '</td>';
    // Stock Movement
    contentHtml += '<td valign="top" style="width : 33%;">';
    contentHtml += '<table><tbody><tr><td><img src="images/productView/stock_movement.png"></td><td valign="middle"><a style="font-size: 16px;color: #1567B9;" onMouseOver="this.style.color=\'#551A8B\'"onMouseOut="this.style.color=\'#1567B9\'" onClick="handlerFunctions.getStockMovement()" href ="#">Stock Movement</a> </td></tr></tbody></table>';
    contentHtml += '</td>';
    //Total Quantity Ordered
    contentHtml += '<td valign="top" style="width : 33%;">';
    contentHtml += '<table><tbody><tr><td><img src="images/productView/ordered_quantity.png"></td><td valign="top"><div>Total Quantity Ordered</div> <div style="font-size: 16px;color: #1567B9;">{totalOrderedQuantity}</div></td></tr></tbody></table>'
    contentHtml += '</td>';
    
    contentHtml  += '</tr></tbody></table>';
    contentHtml  += '</tpl>';
    
    var config = {
        bodyStyle : "padding:21px 6px 6px 25px;overflow:auto;",
        tpl : new Wtf.XTemplate(contentHtml),
        autoScroll : true,
        border:false,
        height: selectedWidget.height || 390
    };
    
    
    Wtf.apply(config,selectedWidget);
    
    var reportList = new Wtf.Panel(config);
    
    return reportList;
}

function createTotalPurchasesWidget(selectedWidget){
    
    var contentHtml = '<tpl>'
    contentHtml += '<table><tbody><tr><td><img src="images/productView/total_purchases.png"></td><td valign="middle"><div style="font-size: 16px;color: #1567B9;">{totalPurchases} </div> <div>Total Purchases</div></td></tr></tbody></table>';
    contentHtml  += '</tpl>';
    
    var config = {
        bodyStyle : "margin: 5px 0px 0px 12px;border-bottom: 1px solid #D3D3D3;",
        tpl : new Wtf.XTemplate(contentHtml),
        autoScroll : true,
        type : "total",
        border:false,
        height: selectedWidget.height || 50
    };
    
    
    Wtf.apply(config,selectedWidget);
    
    var reportList = new Wtf.Panel(config);
    
    return reportList;
}
function createProductFeaturesPanel(selectedWidget,componentIds,parentObj){
    
    var contentHtml = '<tpl>'
    contentHtml += '<table style = "border-spacing: 15px 6px;"> <tbody><tr>';
    
    
    // Product Sales History
    contentHtml += '<td>';
    contentHtml += '<table><tbody><tr><td><img src="images/productView/product_sales_history.png"></td><td valign="middle"><a style="font-size: 16px;color: #1567B9;" onMouseOver="this.style.color=\'#551A8B\'"onMouseOut="this.style.color=\'#1567B9\'" onClick="handlerFunctions.getSalesHistory()" href ="#">Product Sales History</a> </td></tr></tbody></table>';
    contentHtml += '</td>';
    //Re-Stock level
    contentHtml += '<td>';
    contentHtml += '<table><tbody><tr><td><img src="images/productView/restock_level.png"></td><td valign="top"><div>Re-Stock level</div> <div style="font-size: 18px;color: #1567B9;">{[values.reorderlevel ? values.reorderlevel :"  ----"]}</div></td></tr></tbody></table>'
    contentHtml += '</td>';
    
    contentHtml   += '</tr><tr>';

    // Search Products
    contentHtml += '<td>';
//    contentHtml += '<table><tbody><tr><td><img src="images/productView/search_products.png"></td><td valign="middle"><a style="font-size: 16px;color: #1567B9;" onMouseOver="this.style.color=\'#551A8B\'"onMouseOut="this.style.color=\'#1567B9\'" href ="#" onClick="handlerFunctions.openSearchWin()">Search Products</a> </td></tr></tbody></table>';
    contentHtml += '</td>';
    // Create New Product
    contentHtml += '<td>';
    contentHtml += '<table><tbody><tr><td><img src="images/productView/create_new_product.png"></td><td valign="middle"><a style="font-size: 16px;color: #1567B9;" onMouseOver="this.style.color=\'#551A8B\'"onMouseOut="this.style.color=\'#1567B9\'" href ="#" onClick="handlerFunctions.createNewProduct()">Create New Product</a> </td></tr></tbody></table>'
    contentHtml += '</td>';
    
    contentHtml += '</tr><tr>';
    // Edit Product
    contentHtml += '<td>';
    contentHtml += '<table><tbody><tr><td><img src="images/productView/edit_product.png"></td><td valign="middle"><a style="font-size: 16px;color: #1567B9;" onMouseOver="this.style.color=\'#551A8B\'"onMouseOut="this.style.color=\'#1567B9\'" href ="#"  onClick="handlerFunctions.editProduct()">Edit Product</a> </td></tr></tbody></table>';
    contentHtml += '</td>';
    //Total Quantity Ordered
    contentHtml += '<td>';
    contentHtml += '<table><tbody><tr><td><img src="images/productView/delete_product.png"></td><td valign="middle"><a style="font-size: 16px;color: #1567B9;" onMouseOver="this.style.color=\'#551A8B\'"onMouseOut="this.style.color=\'#1567B9\'" href ="#"  onClick="handlerFunctions.deleteProduct()">Delete Product</a> </td></tr></tbody></table>'
    contentHtml += '</td>';
    
    contentHtml  += '</tr></tbody></table>';
    contentHtml += '</tpl>';
    
    var reportList = new Wtf.Panel({
        bodyStyle : "padding:21px 6px 6px 25px;overflow:auto;",
        tpl : new Wtf.XTemplate(contentHtml),
        autoScroll : true,
        border:false,
        height: selectedWidget.height || 390
    });
    
    return reportList;
}

function getChartConfig(selectedWidget){
    var chartConfig;
    var chartType = selectedWidget.subtype;
    var chartName = selectedWidget.chartname;
    if(selectedWidget.properties !=undefined ){
        chartType = selectedWidget.properties.source.Type.toLowerCase();
        if(selectedWidget.properties.source["Title"]){
            chartName = selectedWidget.properties.source["Title"];
        }
    }
    switch(chartType){
        case "pie" :
            chartConfig={
                "type": "pie",
                "titleField": "name",
                "valueField": "value",
                "colorField": "color",
                "labelsEnabled": false,
                "innerRadius": "40%",
                "theme": 'light',
                "defs": {
                    "filter": [{
                        "id": "shadow",
                        "width": "200%",
                        "height": "200%",
                        "feOffset": {
                            "result": "offOut",
                            "in": "SourceAlpha",
                            "dx": 0,
                            "dy": 0
                        },
                        "feGaussianBlur": {
                            "result": "blurOut",
                            "in": "offOut",
                            "stdDeviation": 2
                        },
                        "feBlend": {
                            "in": "SourceGraphic",
                            "in2": "blurOut",
                            "mode": "normal"
                        }
                    }]
                },
                "legend": {
                    "position": "bottom",
                    "marginRight": 20,
                    "autoMargins": false,
                    unit: "$",
                    unitPosition: "left"
                },
                "export": {
                    "enabled": true,
                    "fileName": chartName
                },
                "titles": [
                {
                    "text": chartName,
                    "size": 15
                }
                ]
            }
            break;
        case "line" :
            chartConfig ={
                "type": "serial",
                "theme": "light",
                "graphs": [{
                    "valueField": "value",
                    "balloonText": "[[category]] : <b>[[value]]</b>",
                    "lineThickness": 5,
                    "bullet": "round",
                    "bulletSize": 40,
                    "labelText": "[[value]]",
                    "labelPosition": "middle",
                    "color": "#fff"
                }],
                "categoryField": "name",
                "export": {
                    "enabled": true,
                    "fileName": chartName
                },
                "titles": [
                {
                    "text": chartName,
                    "size": 15
                }
                ]
            }
            
            if(selectedWidget.properties.source["Show Legends"]){
                chartConfig["legend"]= {
                    "align": "center",
                    "equalWidths": false,
//                    "periodValueText": "total: [[value.sum]]",
                    "valueAlign": "left",
                    "valueText": "[[value]] ([[percents]]%)",
                    "valueWidth": 100
                }
            }
            break;        
        case "clustered_bar":
            chartConfig = {
                "theme": "light",
                "type": "serial",
                "startDuration": 0,
                "valueAxes": [{
                    "gridThickness": 0,
                    "unit": "$",
                    "unitPosition": "left"

                }],        
     
                "graphs": [{
                    "balloonText": "Amount Received in [[category]]: <b>[[value]]</b>",
                    "fillAlphas": 0.9,
                    "lineAlpha": 0.2,
                    "type": "column",
                    "valueField": "amountreceived",
                    "fillColorsField": "colorreceived"
                
                }, {
                    "balloonText": "Amount Due in [[category]]: <b>[[value]]</b>",
                    "fillAlphas": 0.9,
                    "lineAlpha": 0.2,
                    "type": "column",
                    "fillColorsField": "colordue",
                    "valueField": "amountdue"
                }],
                "categoryField": "monthname",
                "categoryAxis": {
                    "gridPosition": "start",
                    "gridThickness": 0
                },
                "export": {
                    "enabled": true,
                    "fileName": chartName
                },
                "titles": [
                {
                    "text": chartName,
                    "size": 15
                }
                ]

            }
            break;
        case "clustered_line" :
            chartConfig ={
                dataDateFormat: "YYYY-MM-dd",
                "theme": "",
                "type": "serial",
                "marginRight": 80,
                "autoMarginOffset": 20,
                "marginTop": 20,
                "valueAxes": [{
                    gridThickness: 0,
                    unit: "$",
                    unitPosition: "left"
                }],
                "graphs": [{
                    title:'Sale',
                    "balloonText": "[[category]]<br><b>Sale Value: [[value]]</b>",
                    "bullet": "round",
                    "bulletBorderAlpha": 2,
                    "hideBulletsCount": 50,
                    "lineThickness": 2,
                    "valueField": "salevalue"
                },{
                    title:'Purchase',
                    "balloonText": "[[category]]<br><b>Purchase Value: [[value]]</b>",
                    "bullet": "round",
                    "bulletBorderAlpha": 5,
                    "hideBulletsCount": 50,
                    "lineThickness": 2,
                    "valueField": "purchasevalue"
                }],
                "categoryField": "date",
                "categoryAxis": {
                    "parseDates": true,
                    gridThickness: 0
                },
                "export": {
                    "enabled": true,
                    "fileName": chartName
                },
                "legend": {
                    labelText:"[[title]]",
                    "useGraphSettings": true
                },
                "titles": [
                {
                    "text": chartName,
                    "size": 15
                }]
            }
            break;
        case "vertical bar" :
        case "horizontal bar":
            
            chartConfig = {
                "type": "serial",
                "theme": "light",
                "valueAxes": [ {
                    "gridColor": "#FFFFFF",
                    "gridAlpha": 0.2,
                    unitPosition: "left",
                    "unit" : "SGD ",
                    "dashLength": 0
                } ],
                "gridAboveGraphs": true,
                "startDuration": 1,
                "graphs": [ {
                    "balloonText": "[[category]]: <b>[[value]]</b>",
                    "fillAlphas": 0.8,
                    "lineAlpha": 0.2,
                    "type": "column",
                    "valueField": "value"
//                    "fillColorsField": "color"
                } ],
                "chartCursor": {
                    "categoryBalloonEnabled": false,
                    "cursorAlpha": 0,
                    "zoomable": false
                },
                "categoryField": "name",
                "categoryAxis": {
                    "gridPosition": "start",
                    "gridAlpha": 0,
                    "tickPosition": "start",
                    "tickLength": 20
                },
                "export": {
                    "enabled": true
                },
                "titles": [
                {
                    "text": chartName,
                    "size": 15
                }]

            }
            
            if(selectedWidget.properties.source["Show Legends"]){
                chartConfig.legend = {
                    labelText:"[[title]]",
                    enabled:true,
                    "useGraphSettings": true
                }
            }
            
            if(chartType == "horizontal bar"){
//                chartConfig.categoryAxis.inside= true;
                chartConfig.rotate=true;
            }
            
            
            break;
    }
    return chartConfig;
}

function createHierachicalGrid(selectedWidget){
    var store = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: "totalCount",
            root: "data"
        }),
        url: selectedWidget.dataUrl
    });

    var columnArr = [];
    columnArr.push({
        header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
        dataIndex:'accountname',
        renderer:this.formatAccountName,
        width:250,
        pdfwidth: 80,
        summaryRenderer:function(){
            return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));
        }.createDelegate(this)
    });

    for(var i=0; i<18; i++){
        columnArr.push({
            hidden: false,
            dataIndex: 'amount_'+i,      
            renderer:this.formatData,  
            width: 110,
            align:'right',
            style: 'text-align:right'
        });
    };
        
    var grid = new Wtf.grid.HirarchicalGridPanel({
        store: store,
        title : selectedWidget.name,
        selectedWidget : selectedWidget,
        loadMask:true,
        hirarchyColNumber:0,
        height : 400,
        border: false,
        columns: columnArr,
        layout: 'fit',
        autoScroll:true,
        viewConfig: {
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }
    });
    grid.on("render", function (){
        this.loadingMask = new Wtf.LoadMask(this.body, {
            msg : WtfGlobal.getLocaleText("acc.msgbox.50"),
            msgCls :"loading-mask"
        });
        this.loadingMask.show();
        WtfGlobal.autoApplyHeaderQtip(this);
    });
    store.load({
        start : 0,
        limit :30
    });
    grid.getStore().on("load", function(){
           
        var store = grid.getStore();
        var monthArray = store.data.items[store.data.length-1].json["months"];

        for(var i=0; i<monthArray.length; i++){            
            grid.getColumnModel().setColumnHeader((i+1), '<div align=center><b>'+monthArray[i]["monthname"]+'</b></div>') ;            
            var column = grid.getColumnModel().getColumnById((i+1));
            column.align= 'right';
            column.style= 'text-align:right';
        }

        var columnCount =  grid.getColumnModel().getColumnCount();
        var monthCount = monthArray.length;

        for(var i=1; i<(1+monthCount); i++){
            grid.getColumnModel().setHidden(i, false) ;
        }        
            
        // show those months with data
        for(var i=(monthCount+1); i<columnCount; i++){
            grid.getColumnModel().setHidden(i,true) ;
        } 

        var lcm = grid.getColumnModel();
        for(var i=1; i<(1+monthCount); i++){
            lcm.setRenderer(i, formatMoney) ;
        }
        var store1 = grid.getStore();
        grid.reconfigure(store1,lcm);
            
        for(var i=0; i< grid.getStore().data.length; i++){
            grid.collapseRow(grid.getView().getRow(i));
        }
        grid.loadingMask.hide();
    });
        
    return grid;
}

function formatMoney(val,m,rec,i,j,s){
    var fmtVal=WtfGlobal.currencyRenderer(val);
    if(rec.data['fmt']){
        fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
    }
    else if(rec.data["level"]==0&&rec.data["accountname"]!="")
        fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
    return fmtVal;
}


function getProductSalesHistory(productId){
//    alert(productId);
    var win = new Wtf.ProductSalesHistory({
        title : "Product Sales History",
        layout : "fit",
        height : 400,
        width :800,
        modal : true,
        productId :productId
    });
    win.show();
}


/*
 *Component used to show product sales history.
 *Opened Sales by customer report in this window and changed column model as per requirement.
 */
Wtf.ProductSalesHistory = Wtf.extend(Wtf.Window, {
    iconCls: "pwnd deskeralogoposition",
    width : 800,
    modal : true,
    constrain: true,
    bodyStyle:"background-color:#f1f1f1;",
    height : 400,
    layout : "fit",
    initComponent:function() {
        this.createSalesHistoryGrid();
        Wtf.apply(this, {
            buttons : [{
                text :"Cancel",
                scope : this,
                handler:function(){
                    this.close();
                }
            }]
        }); 
        this.on("render",function(){
            this.reConfigureSalesGrid();
            this.add(this.reportPanel);
        },this);
        Wtf.ProductSalesHistory.superclass.initComponent.apply(this, arguments);
        
    },
    createSalesHistoryGrid : function(){
        /*
         *Create sales by customer report
         **/
        this.reportPanel = new Wtf.account.TransactionListPanelViewSales({
            border : false,
            isProductView : true,
            layout: 'fit',
            iscustreport : true,
            isAddressFieldSearch : true, //Flag used to enable advance search on address fields
            businessPerson:'Customer',
            moduleid : Wtf.Acc_Invoice_ModuleId,
            isCustomer:true,
            label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //"Invoice",
            iconCls:'accountingbase invoicelist',
            getMyConfig : Wtf.emptyFn,
            saveGridStateHandler: Wtf.emptyFn //Overriden this function with empty function as no need to save grid config.
        });
        
        var store = this.reportPanel.getGrid().store;
        
        store.on('beforeload', function(store,options){
             var currentBaseParams = store.baseParams || {};
             currentBaseParams.productid = this.productId;
        },this);
    },
    reConfigureSalesGrid : function(){
        /*
         * Reconfigure columns as per requirement.
         */
        var columns = [new Wtf.grid.RowNumberer(),{
            header:WtfGlobal.getLocaleText("acc.cust.name"),
            pdfwidth:80,
            dataIndex:'customername',
            summaryRenderer:function(){
                return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }
        },{
            header:"Invoice Number",
            pdfwidth:80,
            dataIndex:'billno',
            renderer:WtfGlobal.linkDeletedRenderer
        },{
            header:"Invoice "+WtfGlobal.getLocaleText("acc.inventoryList.date"),
            dataIndex:'date',
            align:'center',
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridQty"),
            dataIndex:'rowquantity',
            width : 50,
            pdfwidth:50,
            align:'center',
            renderer:function(value){
                return parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            }
        },{
            header:WtfGlobal.getLocaleText("acc.rem.188.Mixed"),
            dataIndex:'rowrate',
            width : 100,
            align:'right',
            pdfwidth:100,
            pdfrenderer : "unitpricecurrency",
            //            hidden: this.isSalesByProductReport,
            renderer:WtfGlobal.withCurrencyUnitPriceRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.rem.193.Mixed"),
            dataIndex:'amount',
            align:'right',
            pdfwidth:100,
            pdfrenderer : "rowcurrency",
            renderer : WtfGlobal.currencyRendererSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.field.AmountWithoutTaxinBaseCurrency")+ " ("+WtfGlobal.getCurrencyName()+")",
            dataIndex:'amountinbase',
            align:'right',
            summaryType:'sum',
            pdfwidth:100,
            hidecurrency : true,
            renderer : WtfGlobal.currencyDeletedRenderer,
            summaryRenderer:WtfGlobal.currencySummaryRenderer
        }];
    
        var grid =this.reportPanel.getGrid();
        if(grid){
            grid.getColumnModel().setConfig(columns);
        }
    }
});

function getStockMovementWindow(productId){
//    alert(productId);
    var win = new Wtf.StockMovementWindow({
        title : "Stock Movement",
        layout : "fit",
        height : 400,
        width :900,
        modal : true,
        productId :productId
    });
    win.show();
}


/*
 *Component to show Stock movement report in product view.
 */
Wtf.StockMovementWindow = Wtf.extend(Wtf.Window, {
    iconCls: "pwnd deskeralogoposition",
    width : 1000,
    modal : true,
    constrain: true,
    bodyStyle:"background-color:#f1f1f1;",
    height : 400,
    layout : "fit",
    initComponent:function() {
        this.createGrid();
        Wtf.apply(this, {
            buttons : [{
                text :"Cancel",
                scope : this,
                handler:function(){
                    this.close();
                }
            }]
        }); 
        this.on("render",function(){
            this.add(this.reportPanel);
        },this);
        Wtf.StockMovementWindow.superclass.initComponent.apply(this, arguments);
        
    },
    createGrid : function(){
        this.reportPanel = new Wtf.StockMovementReport({
            layout:"fit",
            border:false,
            isProductView : true,
            productId : this.productId,
            iconCls:getButtonIconCls(Wtf.etype.inventorysmr)
        });
    }
});

function openSearchDialog(productId){
    var win = new Wtf.productSearchDialog({
        title : "Search Products",
        productId : productId
    });
    win.show()
}
/*
 *Component to search product in product view.
 **/
Wtf.productSearchDialog = Wtf.extend(Wtf.Window,{
    iconCls: "pwnd deskeralogoposition",
    width : 400,
    modal : true,
    constrain: true,
    //            resizable : false,
    bodyStyle:"background-color:#f1f1f1;padding: 10px;",
    height : 150,
    layout : "fit",
    initComponent:function() {
        this.createProductCombo();
        Wtf.apply(this, {
            items :[this.form],
            buttons : [{
                text :"View product",
                scope : this,
                tooltip:"View product in current tab",
                handler: this.loadProductInSameView
            },{
                text :"View product in new tab",
                scope : this,
                tooltip:"View product in new tab",
                handler: this.loadProductView
            },{
                text :"Cancel",
                scope : this,
                handler:function(){
                    this.close();
                }
            }]
        }); 
        this.on("render",function(){
            var width = Math.max(document.documentElement.clientWidth, document.body.clientWidth, window.innerWidth ||  0);
            var height = Math.max(document.documentElement.clientHeight, document.body.clientHeight, window.innerHeight ||  0);
            var left = width* (40/100);
            var right = height * (20/100);
            this.setPosition(left,right);
        });
        Wtf.productSearchDialog.superclass.initComponent.apply(this, arguments);
        
    },
    createProductCombo : function(){
//        this.productEditor = CommonERPComponent.createProductPagingComboBox(250,150,Wtf.ProductCombopageSize,this,{},false);
        //        this.productEditor.fieldLabel = "Select Product";
        
        var productRec= Wtf.data.Record.create([
        {
            name: 'productid'
        },

        {
            name: 'productname'
        },

        {
            name: 'pid'
        },

        {
            name: 'type'
        },

        {
            name: 'hasAccess'
        }
        ]);

        var productStore = new Wtf.data.Store({
            url:"ACCProductCMN/getProductsIdNameforCombo.do",
            baseParams:{},
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            }, productRec)
        });  
        this.productEditor = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectProduct"),
            hiddenName: 'productid',
            name: 'productid',
            //                hidden: scope.isCustBill,
            store: productStore,
            valueField: 'productid',
            displayField: 'productname',
            mode: 'remote',
            pageSize:Wtf.ProductCombopageSize,
            typeAhead: true,
            //            listClass : 'search_in_product_view',
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
            width:200,
            listWidth:150,
            extraComparisionField:'productname',
            extraComparisionFieldArray:['productname','pid'], //search on both pid and name
            isProductCombo: true,
            selectOnFocus:true,
            extraFields:[],
            minChars:1,
            scope:this,
            hirarchical:true,
            handleHeight : 1,
            forceSelection:true
        });
        
        this.form = new Wtf.form.FormPanel({
            border:false,
            items : [this.productEditor]
        })
    },
    loadProductInSameView :  function(){
        var productId = this.productEditor.getValue();
        if(!productId){
            this.productEditor.markInvalid();
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.prod.comboEmptytext"));
            return;
        }
        var oldView = Wtf.getCmp(productViewIdPrefix + this.productId);
        if(oldView){
            Wtf.getCmp("as").remove(oldView);
            oldView.destroy();
        }
        this.loadProductView();
    },
//    loadProductInSameView :  function(){
//        var productId = this.productEditor.getValue();
//        var oldView = Wtf.getCmp(productViewIdPrefix + this.productId);
//        if(oldView){
//            if(oldView.loadMask){
//                oldView.loadMask.show();
//            }
//            oldView.resetForms();
//            oldView.loadProduct(productId);
//            oldView.id = productViewIdPrefix+productId;
//        }
//        this.close();
//    },
    loadProductView : function(){
        
        var productId = this.productEditor.getValue();
        if(!productId){
            this.productEditor.markInvalid();
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.prod.comboEmptytext"));
            return;
        }
        getConfiguredProductView(productId);
        this.close();
    }
});
