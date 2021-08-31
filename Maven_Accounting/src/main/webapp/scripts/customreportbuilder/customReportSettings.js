
Ext.fieldType = {
        textField: 1,
        numberField: 2,
        dateField: 3,
        comboBox: 4,
        multiselect : 7,
        checkbox : 11,
        listBox: 12,
        textArea: 13
    }


Ext.pref= JSON.parse(sessionStorage.getItem("wtfPref"));

Ext.Acc_Invoice_ModuleId = 2;
Ext.Acc_Sales_Order_ModuleId = 20;
Ext.Acc_Customer_Quotation_ModuleId = 22;
Ext.Acc_FixedAssets_DisposalInvoice_ModuleId = 38;
Ext.Acc_ConsignmentRequest_ModuleId=50;
Ext.Acc_ConsignmentInvoice_ModuleId=52;
Ext.Acc_Lease_Quotation=65;
Ext.Acc_Lease_Order=36;
Ext.LEASE_INVOICE_MODULEID =93;
Ext.Acc_Vendor_Invoice_ModuleId = 6;
Ext.Acc_Purchase_Order_ModuleId = 18;
Ext.Acc_Vendor_Quotation_ModuleId = 23;
Ext.Acc_FixedAssets_Vendor_Quotation_ModuleId = 89;
Ext.Acc_FixedAssets_Purchase_Order_ModuleId = 90;
Ext.Acc_FixedAssets_PurchaseInvoice_ModuleId = 39;
Ext.Acc_ConsignmentVendorRequest_ModuleId=63;
Ext.Acc_Consignment_GoodsReceipt_ModuleId=58;
Ext.Acc_Purchase_Requisition_ModuleId = 32;
Ext.Acc_RFQ_ModuleId = 33;

Ext.getFieldType = function(field){
    var fieldType=""
    switch(parseInt(field)){
        case Ext.fieldType.textField :
            fieldType = "Text Field";
            break;
        case Ext.fieldType.numberField :
            fieldType = "Number Field";
            break;
        case Ext.fieldType.dateField :
            fieldType = "Date Field";
            break;
        case Ext.fieldType.comboBox :
            fieldType = "Combo Box";
            break;
        case Ext.fieldType.multiselect :
            fieldType = "Multiselect Combo Box";
            break;
        case Ext.fieldType.checkbox :
            fieldType = "Check Box";
            break;
        case Ext.fieldType.listBox :
            fieldType = "List Box";
            break;
        case Ext.fieldType.textArea :
            fieldType = "Text Area";
            break;
    }
    return fieldType;
}

function getTopHtmlReqField(text, body,img,para){
    if(img===undefined || img=='') {
        img = '../../images/createuser.png';
    }
    
    var altImg='../../images/createuser.png';
    var str =  "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
    +"<div style='float:left;height:100%;width:auto;position:relative;'>"
    +"<img src = "+img+" onerror = 'src="+altImg+"' style = ' height: 51px;margin: 9px 2px 0 13px;'></img>"
    +"</div>"
    +"<div style='float:left;height:100%;width:60%;position:relative;'>"
    +"<div style='font-size:12px;font-style:bold;float:left;margin:15px 0px 0px 10px;width:100%;position:relative;'><b>"+text+"</b></div>"
    +"<div style='font-size:10px;float:left;margin:6px 0px 10px 10px;width:100%;position:relative;'>"+body+"</div>";
    
//    str+="<div style='font-size:10px;margin:60px 0px 20px 245px;width:100%;position:absolute;'>"+para+"</div>";
            
    str+="</div>"+"</div>" ;
    return str;
}

Ext.CustomMsg = function(title,msg,icon){
    Ext.Msg.show({
        title      : title,
        msg        : msg,
        buttons    : Ext.MessageBox.OK,
        icon       : icon
    });
}

Ext.getModuleCategory = function(moduleCat){
    var moduleCategory=""
    switch(moduleCat){
        case Ext.moduleCatogery.sales :
            moduleCategory="Sales";
            break;
        case Ext.moduleCatogery.purchase :
            moduleCategory="Purchase";
            break;
        case Ext.moduleCatogery.asset :
            moduleCategory="Asset";
            break;
        case Ext.moduleCatogery.consignment :
            moduleCategory="Consignment";
            break;
        case Ext.moduleCatogery.lease :
            moduleCategory="Lease";
            break;
    }
    return moduleCategory;
}

Ext.getModule = function(moduleid){
    var moduleName=""
    switch(moduleid){
        case Ext.module.salesorder :
            moduleName="Sales Order";
            break;
    }
    return moduleName;
}

Ext.getFilterType = function(column){
    var xtype = parseInt(column.xtype);
    var filterType="";
    var isCustom = column.customfield != undefined ? column.customfield : column.custom;
    if( typeof isCustom === 'string'){
        isCustom = (isCustom  === "true");
    }
    
    if(xtype==Ext.fieldType.numberField){
        filterType = {type: "number"};
    }else if(xtype==Ext.fieldType.dateField){
        filterType = {type: "date",dateFormat:'Y-m-d'};
    }else if(xtype==Ext.fieldType.checkbox){
        filterType = {type: 'boolean',yesText: 'True',noText: 'False'}
    }else if(xtype== Ext.fieldType.comboBox){
       var filterStore = getComboFieldStore(column.id , column.defaultHeader, "20", !isCustom);
        filterType =  {
            type: 'customlist',
            store: filterStore,
            idField:'id',
            labelField:'name'
        }
    }else if(xtype != Ext.fieldType.multiselect && xtype != Ext.fieldType.listBox){
        filterType = {type: "string"};
    }
    return filterType;
}

Ext.getColumnRenderer = function(column){
    var renderer = "";
    var xtype =parseInt(column.xtype);
    if(xtype == Ext.fieldType.numberField){
        var rendererProperty = column.properties.source.renderer;
         if(column.defaultHeader=="Discount"){
            renderer = function(val, metaData, record){
                val = Ext.util.Format.number(val, Ext.getColumnRendererFormat(column));
                if(record.data.discountispercent=="1"||record.data.discountispercent=="T"){//For Discount is in percentage
                    if(val !="" && val!=undefined)
                        return val+ "%";
                }else if(rendererProperty== "Transaction Currency"){
                    var currency = record.data.currencysymbol;
                    if(val !="" && val!=undefined)
                        return currency+" "+val;
                }else if(rendererProperty== "Base Currency"){
                    if(val !="" && val!=undefined)
                        return Ext.pref.CurrencySymbol+" "+val;
                }else{
                    return val;
                }
            };
        }else if(rendererProperty== "Transaction Currency"){     // Transaction level currency renderer
            renderer = function(val, metaData, record){
                var currency = record.data.currencysymbol;
                val = Ext.util.Format.number(val, Ext.getColumnRendererFormat(column));
                if(val !="" && val!=undefined)
                return currency+" "+val;
            };
        }else if(rendererProperty== "Base Currency"){     // Base currency renderer
            renderer = function(val, metaData, record){
                val = Ext.util.Format.number(val, Ext.getColumnRendererFormat(column));
                if(val !="" && val!=undefined)
                return Ext.pref.CurrencySymbol+" "+val;
            };
        }else{
            renderer = function(val, metaData, record){
                val = Ext.util.Format.number(val, Ext.getColumnRendererFormat(column));
                return val;
            };
        }
    }else{
        renderer = function(val, metaData, record){
            return val;
        }; 
    }
    return renderer;
}

ExtGlobal={
    getLocaleText:function(key, basename, def){
        var base=window[basename||"messages"];
        var params=[].concat(key.params||[]);
        key = key.key||key;
        if(base){
            if(base[key]){
                params.splice(0, 0, base[key]);
                //                return String.format.apply(this,params);
                return params.toString();
            //                return this,params[0];
            }else
                console.log("Locale specific text not found for ["+key+"]");
        }else{
            console.log("Locale specific base ("+basename+") not available");
        }
        return def||key;
    },
    convertToGenericDate:function(value){
        if(!value) return value;
        return Ext.Date.format(value,"Y-m-d");
    },
    HTMLStripper: function(val){
        var str = Ext.util.Format.stripTags(val);
        return str.replace(/"/g, '').trim();
    },
    getXType: function(fieldType){
        switch(fieldType){

            case 1:
                return "textfield";
                break;
            case 2:
                return "numberfield";
                break;
            case 3:
                return "datefield";
                break;
            case 4:
                return "combo";
                break;
            case 5:
                return "timefield";
                break;
            case 11:
                return "checkbox";
                break;
            case 7:
                return "select";
                break;
            case 8:
                return "combo";
                break;
            case 9:
                return "autono";
                break;
            case 12:
                return "fieldset";
                break;
            case 13:
                return "textarea";
                break;
        }
    },
    replaceAll : function(txt, replace, with_this) {
        return txt.replace(new RegExp(replace, 'g'),with_this);
    },
    reportNameTest : /^(\w+ ?)*$/,
    
    //To use this function you need to specify pluginColumns in grid config and all plugin columns should be initially unlocked.
    lockPluginColumns:function(grid, column, eOpts){     
        var me = grid,
        pluginColumns = grid.pluginColumns,
        normalGrid = me.normalGrid,
        lockedGrid = me.lockedGrid,
        normalView = normalGrid.view,
        lockedView = lockedGrid.view,
        normalHCt = normalGrid.headerCt,
        lockedHCt = lockedGrid.headerCt,
        newIndex = 0,
        refreshFlags, ownerCt,activeHd;
            
        normalView.blockRefresh = lockedView.blockRefresh = true;
        if(lockedHCt.getColumnCount() == 2){  // by defualt one plugin column is get locked when we loacked any column.
            for(var i=0 ; i < pluginColumns - 1 ; i++){   // hence lock all remaining plugin columns
                activeHd = normalHCt.items.items[0];
                ownerCt = activeHd.ownerCt;
                
                // We decide which views to refresh. Do not let the grids do it in response to column changes
                // Keep the column in the hierarchy during the move.
                activeHd.ownerCmp = activeHd.ownerCt;
                ownerCt.remove(activeHd, false);
                activeHd.locked = true;
                // Flag to the locked column add listener to do nothing
                if(i==0 && pluginColumns > 2){
                    newIndex = i;
                }else{
                    newIndex = i+1 ;
                }
                lockedHCt.insert(newIndex, activeHd);
                activeHd.ownerCmp = null;
            }
            normalView.blockRefresh = lockedView.blockRefresh = false;
            refreshFlags = me.syncLockedWidth();
            if (refreshFlags[0]) {
                lockedGrid.getView().refreshView();
            }
            if (refreshFlags[1]) {
                normalGrid.getView().refreshView();
            }
        }
    },
    
    //To use this function you need to specify pluginColumns in grid config and all plugin columns should be initially unlocked.
    unlockPluginColumns : function(grid, column, eOpts){
        var me = grid,
        pluginColumns=grid.pluginColumns,
        normalGrid = me.normalGrid,
        lockedGrid = me.lockedGrid,
        normalView = normalGrid.view,
        lockedView = lockedGrid.view,
        normalHCt = normalGrid.headerCt,
        lockedHCt = lockedGrid.headerCt,
        newIndex = 0,
        refreshFlags, activeHd;
            
        normalView.blockRefresh = lockedView.blockRefresh = true;
        if(lockedHCt.getColumnCount() == pluginColumns){
            for (var i=0 ; i < pluginColumns ; i++){
                activeHd = lockedHCt.items.items[0];
                // We decide which views to refresh. Do not let the grids do it in response to column changes
                // Keep the column in the hierarchy during the move.
                // So that grid.isAncestor(column) still returns true, and SpreadsheetModel does not deselect
                activeHd.ownerCmp = activeHd.ownerCt;
                activeHd.ownerCt.remove(activeHd, false);
                activeHd.locked = false;
                
                normalHCt.insert(i, activeHd);
                activeHd.ownerCmp = null;
            }
            
            normalView.blockRefresh = lockedView.blockRefresh = false;
            // syncLockedWidth returns visible column counts for both grids.
            // only refresh what needs refreshing
            refreshFlags = me.syncLockedWidth();
            if (refreshFlags[0]) {
                lockedGrid.getView().refreshView();
            }
            if (refreshFlags[1]) {
                normalGrid.getView().refreshView();
            }
        }
    },
    
    getSeperatorPos: function() {
        return Ext.pref.seperatorpos;
    },
    
    getDateFormat : function(){
        return Ext.pref.DateFormat ; 
    },
    
    getOnlyDateFormat: function() {
        var pos = ExtGlobal.getSeperatorPos();
        var fmt = ExtGlobal.getDateFormat();
        if(pos<=0)
            return "Y-m-d";
        return fmt.substring(0,pos);
    }
}

Ext.getColumnRendererFormat = function(column){
    var format="";
    if(column.xtype == Ext.fieldType.numberField){
        var precision=column.properties.source.precision;
        format= '0,000'
        for(var i=0;i<precision;i++){
            if(i==0){
                format +='.'
            }
            format += '0'
        }
    }
    return format;
}

Array.prototype.getIemtByParam = function(paramPair) {
    var key = Object.keys(paramPair)[0];
    return this.find(function(item){
        return ((item[key] == paramPair[key]) ? true: false)
        });
}

//Override addExpander method in row expander plugin because,In last line which is commented It is overriding position of checkbox from checkbox selection model. 
Ext.grid.plugin.RowExpander.prototype.addExpander = function(expanderGrid) {
    var me = this;
    me.grid = expanderGrid;
    me.expanderColumn = expanderGrid.headerCt.insert(0, me.getHeaderConfig());
//    expanderGrid.getSelectionModel().injectCheckbox = 1;
    if(expanderGrid.getSelectionModel().injectCheckbox == 0) expanderGrid.getSelectionModel().injectCheckbox = 1;
    return me;
};

// Overiding summary type methods to converting input values from string type to number type
Ext.util.Collection.prototype._aggregators= {
        average: function(items, begin, end, property, root) {
            var n = end - begin;
            return n && this._aggregators.sum.call(this, items, begin, end, property, root) / n;
        },
        bounds: function(items, begin, end, property, root) {
            for (var value, max, min,
                i = Number(begin); i < end; ++i) {                       //here is the change converting "begin" from string type to number type
                value = items[i];
                value = Number((root ? value[root] : value)[property]);  //here is the change converting "value" from string type to number type
                // First pass max and min are undefined and since nothing is less than
                // or greater than undefined we always evaluate these "if" statements as
                // true to pick up the first value as both max and min.
                if (!(value < max)) {
                    // jshint ignore:line
                    max = value;
                }
                if (!(value > min)) {
                    // jshint ignore:line
                    min = value;
                }
            }
            return [
                min,
                max
            ];
        },
        count: function(items) {
            return items.length;
        },
        extremes: function(items, begin, end, property, root) {
            var most = null,
                least = null,
                i, item, max, min, value;
            for (i = begin; i < end; ++i) {
                item = items[i];
                value = Number((root ? item[root] : item)[property]);        //here is the change converting "value" from string type to number type
                // Same trick as "bounds"
                if (!(value < max)) {
                    // jshint ignore:line
                    max = value;
                    most = item;
                }
                if (!(value > min)) {
                    // jshint ignore:line
                    min = value;
                    least = item;
                }
            }
            return [
                least,
                most
            ];
        },
        max: function(items, begin, end, property, root) {
            var b = this._aggregators.bounds.call(this, items, begin, end, property, root);
            return b[1];
        },
        maxItem: function(items, begin, end, property, root) {
            var b = this._aggregators.extremes.call(this, items, begin, end, property, root);
            return b[1];
        },
        min: function(items, begin, end, property, root) {
            var b = this._aggregators.bounds.call(this, items, begin, end, property, root);
            return b[0];
        },
        minItem: function(items, begin, end, property, root) {
            var b = this._aggregators.extremes.call(this, items, begin, end, property, root);
            return b[0];
        },
        sum: function(items, begin, end, property, root) {
            for (var value,
                sum = 0,
                i = begin; i < end; ++i) {
                value = items[i];
                value = Number((root ? value[root] : value)[property]);     //here is the change converting "value" from string type to number type
                sum += value;
            }
            return sum;
        }
    }

Ext.define("My.extension.ListFilter", {
    extend : "Ext.grid.filters.filter.List",
    emptyText : ExtGlobal.getLocaleText("acc.field.Nodatatodisplay"),
    alias: 'grid.filter.customlist',
    type : 'customlist',
    createMenuItems : function (store) {
        var me = this,
        menu = me.menu,
        len = store.getCount(),
        contains = Ext.Array.contains,
        listeners, itemDefaults, record, gid, idValue, idField, labelValue, labelField, i, item, processed;

        // B/c we're listening to datachanged event, we need to make sure there's a menu.
        if (len && menu) {
            listeners = {
                checkchange: me.onCheckChange,
                scope: me
            };

            itemDefaults = me.getItemDefaults();
            menu.suspendLayouts();
            menu.removeAll(true);
            gid = me.single ? Ext.id() : null;
            idField = me.idField;
            labelField = me.labelField;

            processed = [];

            for (i = 0; i < len; i++) {
                record = store.getAt(i);
                idValue = record.get(idField);
                labelValue = record.get(labelField);

                // Only allow unique values.
                if (labelValue == null || contains(processed, idValue)) {
                    continue;
                }

                processed.push(labelValue);

                // Note that the menu items will be set checked in filter#activate() if the value of the menu
                // item is in the cfg.value array.
                item = menu.add(Ext.apply({
                    text: labelValue,
                    group: gid,
                    value: idValue,
                    listeners: listeners
                }, itemDefaults));
            }

            menu.resumeLayouts(true);
        }else if(len === 0 && menu){
            menu.removeAll(true);     // If no data present in store then apply empty text.
            item = menu.add(Ext.apply({
                text : this.emptyText,
                iconCls : "nodata"
            }));
        }
    }
});

Ext.define("My.extension.LockedCheckBoxSelModel", {
    extend: "Ext.selection.CheckboxModel",
    lock:false,
    onReconfigure: function(grid, store, columns) {
        if (columns) {
            var view;
            if(this.views[1]!=undefined){
                view = this.lock ? this.views[0]:this.views[1]; 
            }else{
                view = this.views[0];
            }
            this.addCheckbox(view);
        }
    }, 
    toggleUiHeader: function(isChecked) {
        var view = this.views[0],
            headerCt = view.headerCt,
            checkHd = headerCt.child('gridcolumn[isCheckerHd]'),
            cls = this.checkerOnCls;
            
        if(checkHd == null && this.views[1] != undefined){
            checkHd = this.views[1].headerCt.child('gridcolumn[isCheckerHd]');
        }
        if (checkHd) {
            if (isChecked) {
                checkHd.addCls(cls);
            } else {
                checkHd.removeCls(cls);
            }
        }
    }
});


Ext.autoNum={
    SalesOrder:1
};

function removeDuplicateParameters(parameters){
    var resultStr = "";
    var result= new Array();
    var keyValuesForParameters;
    /* 
     * For Check first parameter is blank in url.
     * checkBlankParameter=false (If first parameter in url is blank) (ERP-12576 for this issue first parameter is blank)
     * checkBlankParameter=true  (If first parameter in url is not blank)
     */
    var checkBlankParameter=true;  
    keyValuesForParameters=parameters.split('&');
    var len=keyValuesForParameters.length;
    for(var i=0;i<len;i++){
        var parameter;
        parameter=keyValuesForParameters[i];
        var len1=result.length;
        var isPresent=false;
        for(var j=0;j<len1;j++){   //checking for duplicate key value pairs for parameters
            if(result[j]==parameter){
                isPresent=true;
                break; 
            }
        }
        if(!isPresent){
            if(keyValuesForParameters[0]==""){
                checkBlankParameter=false;
            }
            if(i!=0 && !checkBlankParameter){  //not inserting first value because it is blank
                result[i-1]=parameter;
                resultStr+="&"+parameter; 
            }
            /*
             *if First Parameter is blank in url
             */
            if(checkBlankParameter){
                result[i]=parameter;
                resultStr+="&"+parameter; 
            }
                
        }
    }
    return resultStr;
}

function getTopHtml(text, body,img,isgrid,margin){
    if(isgrid===undefined)isgrid=false;
    if(margin===undefined)margin='15px 0px 10px 10px';
     if(img===undefined||img==null) {
        img = '../../images/createuser.png';
    }
     var str =  "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
                    +"<div style='float:left;height:100%;width:auto;position:relative;'>"
                    +"<img src = "+img+"  class = 'adminWinImg'></img>"
                    +"</div>"
                    +"<div style='float:left;height:100%;width:80%;position:relative;'>"
                    +"<div style='font-size:12px;font-style:bold;float:left;margin:15px 0px 0px 10px;width:100%;position:relative;'><b>"+text+"</b></div>"
                    +"<div style='font-size:10px;float:left;margin:15px 0px 10px 10px;width:100%;position:relative;'>"+body+"</div>"
                        +(isgrid?"":"<div class='medatory-msg'>"+ExtGlobal.getLocaleText("acc.changePass.reqFields")+"</div>")
                        +"</div>"
                    +"</div>" ;
     return str;
}

//Export Url on the basis of Moduelid
function getExportUrlForCustomReportBuilder(moduleId) {
    var exportUrl = "../../export.jsp";
    switch(moduleId) {
        case Ext.Acc_Sales_Order_ModuleId:
//            exportUrl = "ACCSalesOrderCMN/exportSalesOrder.do";
            exportUrl = "ACCSalesOrderCMN/exportSalesOrderforCustomReportBuilder.do";
            break;
    }
    return exportUrl;
}

Ext.apply(Ext.form.field.VTypes, {
    //  vtype validation function
    reportnamevtype: function(val, field) {
        return ExtGlobal.reportNameTest.test(val);
    },
   // vtype Text property: The error text to display when the validation function returns false
    reportnamevtypeText: ExtGlobal.getLocaleText("acc.CustomReport.invalidReportName"),
    
    daterange : function(val, field) {
		var date = field.parseDate(val);

		if(!date){
			return;
		}
		if (field.startDateField && (!this.dateRangeMax || (date.getTime() != this.dateRangeMax.getTime()))) {
			var start = Ext.getCmp(field.startDateField);
			start.setMaxValue(date);
                        this.dateRangeMax = date;
			start.validate();
			
		} 
		else if (field.endDateField && (!this.dateRangeMin || (date.getTime() != this.dateRangeMin.getTime()))) {
			var end = Ext.getCmp(field.endDateField);
			end.setMinValue(date);
                        this.dateRangeMin = date;
			end.validate();
			
		}
		/*
		 * Always return true since we're only using this vtype to set the
		 * min/max allowed values (these are tested for after the vtype test)
		 */
		return true;
	}
});

function getStoreActionMethods() {
    return {
            create: "POST",
            read: "POST",
            update: "POST",
            destroy: "POST"
        };
}
function headerCheck(header) {
    var indx=header.indexOf('(');
    if(indx!=-1) {
        indx=header.indexOf("&#");
        if(indx!=-1)
            header=header.substring(0,header.indexOf('('));
    }
    return header;
}

function getComboFieldStore(fieldid,comboboxname,moduleid,isdefaultfield){
    var url =  "ACCAccountCMN/getCustomCombodata.do";
    var extraParams = {
        mode: 2,
        flag: 1,
        fieldid: fieldid
    }
    var fields=[{name: 'id'},{name: 'name'}];
            
    if(isdefaultfield){
        if(comboboxname=="Shipping Route"){
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode:112,   
                groupid:28,
                common:'1'
            }
        }else if (comboboxname == "Customer") {
            url = "ACCCustomer/getCustomersForCombo.do";
            extraParams={
                mode:112,   
                groupid:28,
                common:'1'
            }
            fields[0].mapping="accid";
            fields[1].mapping="accname";
        }else if (comboboxname == "Vendor") {
            url = "ACCVendor/getVendorsForCombo.do";
            extraParams={
                mode:2,
                group:13,
                deleted:false,
                nondeleted:true,
                common:'1'
            }
            fields[0].mapping="accid";
            fields[1].mapping="accname";
        }else if (comboboxname == "Currency") {
            url = "ACCCurrency/getCurrencyExchange.do";
            extraParams={
                mode:201,
                common:'1'
            }
            fields[0].mapping="tocurrencyid";
            fields[1].mapping="tocurrency";
        }else if (comboboxname == "Sales Person") {
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode: 112,
                groupid: 15
            }
        }else if (comboboxname == "Agent") {
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode: 112,
                groupid: 20
            }
        }else if (comboboxname == "Tax Name") {
            url = "ACCTax/getTax.do";
            extraParams={
                mode:33,
                common:'1',
                moduleid:moduleid
            }
            fields[0].mapping="taxid";
            fields[1].mapping="taxname";
        }else if (comboboxname == "Created By" || comboboxname == "Last Edited By") {
            url = "ProfileHandler/getAllUserDetails.do";
            extraParams={
                mode:11
            }
            fields[0].mapping="userid";
            fields[1].mapping="fullname";
        }else if (comboboxname == "Payment Method") {
            url = "ACCPaymentMethods/getPaymentMethods.do";
            extraParams= "";
            fields[0].mapping="methodid";
            fields[1].mapping="methodname";
        }else if (comboboxname == "Paid To") {
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode: 112,
                groupid: 17
            }
        }else if (comboboxname == "Received From") {
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode: 112,
                groupid: 18
            }
        }else if (comboboxname == "SO No.") {
            url = "ACCLinkData/getLinkedSONo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "DO No.") {
            url = "ACCLinkData/getLinkedDONo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid,
                linkFlag:true
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "CQ No.") {
            url = "ACCLinkData/getLinkedCQNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "SI No.") {
            url = "ACCLinkData/getLinkedSINo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "VQ No.") {
            url = "ACCLinkData/getLinkedVQNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "PO No.") {
            url = "ACCLinkData/getLinkedPONo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "GR No.") {
            url = "ACCLinkData/getLinkedGRNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "PI No.") {
            url = "ACCLinkData/getLinkedPINo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "Store") {
            url = "INVStore/getStoreList.do";
            extraParams={
                isAdvanceSearch:true,
                isActive : true,
                storeTypes : "1",
                requestModuleid:moduleid
            }
            fields[0].mapping="store_id";
            fields[1].mapping="fullname";
        }else if (comboboxname == "PR No.") {
            url = "ACCLinkData/getLinkedPRNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "Debit Note") {
            url = "ACCLinkData/getLinkedDebitNoteNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "Credit Note") {
            url = "ACCLinkData/getLinkedCreditNoteNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "RFQ No.") {
            url = "ACCLinkData/getLinkedRFQNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "Invoice No") {
            if(moduleid == 14/*Wtf.Acc_Make_Payment_ModuleId*/){             //Make Payment
                url = "ACCLinkData/getLinkedPINo.do";
            } else if(moduleid == 16/*Wtf.Acc_Receive_Payment_ModuleId*/){   //Receive Payment
                url = "ACCLinkData/getLinkedSINo.do"
            }
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }    
    }
     
    var store = Ext.create('Ext.data.Store', {
        autoLoad: false,
        fields: fields,
        proxy: {
            type: 'ajax',
            url: url,
            actionMethods : getStoreActionMethods(),
            reader: {
                type: 'json',
                rootProperty: 'data["data"]',
                keepRawData: true
            },
            extraParams: extraParams
        }
    });
    return store;
}
