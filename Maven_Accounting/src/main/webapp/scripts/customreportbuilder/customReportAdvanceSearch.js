
Ext.define('SearchFieldsModel', {
    extend: 'Ext.data.Model',
    fields: []
});

Ext.advancedSearchComponent = function(config){
    Ext.apply(this, config);
    this.createToolbar();
    this.checkRecXtype=config.checkRecXtype
    this.events = {
        "filterStore": true,
        "clearStoreFilter": true
    };
        
    this.combovalArr=[];
    this.xtypeArr=[];
    this.dimensionBasedComparisionReport=config.dimensionBasedComparisionReport==undefined?false:config.dimensionBasedComparisionReport;
    this.hideRememberSerchConfig=config.hideRememberSerch==undefined?false:config.hideRememberSerch;
    this.hideRememberSerch=false;
    this.hideFilterConjunction=false;
    this.hideSearchBttn=false;
    this.hideCloseBttn=false;
    this.isOnlyGlobalCustomColumn=config.isOnlyGlobalCustomColumn==undefined?false:config.isOnlyGlobalCustomColumn;
    this.isSubLdgerExport=config.isSubLdgerExport==undefined?false:config.isSubLdgerExport;
    this.templateid=config.templateid==undefined?'':config.templateid;//For Custom Layout templates
    this.isCustomLayout=config.isCustomLayout==undefined?false:config.isCustomLayout;//To identify whether the search was saved from Custom Layout or Report List
    if(this.dimensionBasedComparisionReport || this.hideRememberSerchConfig){
        this.hideRememberSerch=true;
    }
    if(this.isSubLdgerExport){
        this.hideRememberSerch=true;
        this.hideFilterConjunction=true;
        this.hideSearchBttn=true;
        this.hideCloseBttn=true;
    }
    this.isCustomDetailReport=config.isCustomDetailReport==undefined?false:config.isCustomDetailReport;

    this.cm= [{
        header: ExtGlobal.getLocaleText("acc.customreport.header.column"),//"Column",
        dataIndex:'column',
        flex:1,
        renderer : function(val, cell, row, rowIndex, colIndex, ds) {
            if (row.get("modulename") == "") {
                return val;
            } else {
                return val + " [" + row.get("modulename") + "]";
            }
        }
    },
    {
        header: ExtGlobal.getLocaleText("acc.advancesearch.search1txt"),
        dataIndex:'searchText',
        flex:1,
        hidden:true

    },
    {
        header: ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt"),//"Search Text",
        dataIndex:'id',
        flex:1
    },
    {
        header: ExtGlobal.getLocaleText("acc.DELETEBUTTON"),//"Delete",
        dataIndex:'delField',
        flex:1,
        renderer : function(val, cell, row, rowIndex, colIndex, ds) {
            return "<div class='pwnd delete-gridrow' > </div>";
        }
    }];
    this.searchStore = Ext.create('Ext.data.Store', {
        model: 'SearchFieldsModel'
    });
    
    Ext.advancedSearchComponent.superclass.constructor.call(this, {
        height:this.dimensionBasedComparisionReport?100:150,
        layout:'fit',
        store: this.searchStore,
        columns:this.cm,
        stripeRows: true,
        autoScroll : true,
        border:false,
        clicksToEdit:1
    });
    
    this.on("cellclick", this.deleteFilter, this);

}

Ext.extend(Ext.advancedSearchComponent, Ext.grid.Panel, {
    onRender: function(conf) {
        Ext.advancedSearchComponent.superclass.onRender.call(this, conf);
    },
    createToolbar:function(){
        var columncombostore = Ext.create('Ext.data.Store', {
            model: 'SearchFieldsModel'
        });
        this.columnCombo = Ext.create('Ext.form.field.ComboBox', {
            store : columncombostore,
            width:290,
            fieldLabel:ExtGlobal.getLocaleText("acc.advancesearch.searchfield"),
            typeAhead: true,
            labelWidth:80,
            selectOnFocus:true,
            displayField:'header',
            valueField : 'fieldid',
            triggerAction: 'all',
            emptyText : ExtGlobal.getLocaleText("acc.responsealert.msg.12"),//'Select a Search Field to search',
            queryMode: 'local',
            ctCls : 'widthforCmb',
            listConfig :{
                minWidth:300,
                maxHeight :400
            },
            tpl: Ext.create('Ext.XTemplate',

                '<tpl for=".">',

                '<div data-qtip = "{[values.fieldid === "NA" ? "'+ExtGlobal.getLocaleText("acc.common.cannotselectvalue")+'" : values.header]}" class="{[values.fieldid === "NA" ? "x-boundlist-item disabled-record" : "x-boundlist-item"]}">',

                '<div>{header}</div>',

                '</div>',

                '</tpl>'
                )
        });
        
        this.columnCombo.on('beforeselect', function(combo, record, index) {
            if(record.get('fieldid')=="NA"){
                return false;
            }
        }, this);
        
        this.columnCombo.on("select",this.createSearchFields,this);
        
        this.searchText = Ext.create('Ext.form.TextField',{
            fieldLabel: ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt"),//Search Text,
            emptyText : ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt"),//Search Text,
            labelWidth:70,
            width:200
        })
        
        var modStore = Ext.create('Ext.data.ArrayStore',{
            fields:['id','name'],
            data: [["AND","AND"],
            ["OR","OR"]
            ],
            autoLoad: true
        });
        this.appendCaseComboValue = "AND";
    
        this.appendCaseCombo = Ext.create('Ext.form.field.ComboBox',{
            fieldLabel : ExtGlobal.getLocaleText("acc.editor.advanceSearch.filter.conjunction"),
            selectOnFocus:true,
            triggerAction: 'all',
            queryMode: 'local',
            store: modStore,
            displayField: 'name',
            typeAhead: true,
            valueField:'id',
            width:160,
            ctCls : 'widthforCmb1',
            value:this.appendCaseComboValue
        });
       
        this.addSearch = Ext.create('Ext.Button',{
            text: ExtGlobal.getLocaleText("acc.targetlists.creatarr.addBTN"),//'Add',
            tooltip: {
                text: ExtGlobal.getLocaleText("acc.advancesearch.addtermtosearchtip")
            },//'Add a term to search.'},
            handler: this.addSearchFilter,
            scope: this,
            iconCls : 'pwnd addfilter'
        })
       
        this.searchBtn = Ext.create('Ext.Button',{
            text: ExtGlobal.getLocaleText("acc.audittrail.searchBTN"),//'Search',
            tooltip: {
                text: ExtGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
            },//'Add terms to search.'},
            handler: this.doSearch,
            scope:this,
            disabled:true,
            iconCls : "advanceSearchButton"
        });
        
        this.closeBtn = Ext.create('Ext.Button',{
            text: ExtGlobal.getLocaleText("acc.CLOSEBUTTON"),//'Close',
            tooltip: {
                text: ExtGlobal.getLocaleText("acc.advancesearch.closebtn.ttip")
            },//'Clear search terms and close advanced search. '},
            handler: this.cancelSearch,
            scope:this,
            iconCls:'pwnd remove-filter'
        });

        this.tbar=[this.columnCombo," ","-",this.searchText," ","-",this.appendCaseCombo," ","-",this.addSearch," ","-",this.searchBtn," ","-",this.closeBtn];
    
    },
    addSearchFilter:function(){
        var clearFields = true;
        var fieldValues = {};
        if(this.columnCombo.getRawValue().trim()==""){
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.msg.ALERTTITLE"),ExtGlobal.getLocaleText("acc.responsealert.msg.12"),Ext.Msg.WARNING);
            return;
        }
        var column =this.columnCombo.getValue();
        var searchText="";
        if(this.searchText.getXType()=="numberfield"){
            if (!this.searchText.getRawValue().trim() == "" && !this.searchTextTo.getRawValue().trim() == "") {
                var num1 = parseFloat(this.searchText.getValue());
                var num2 = parseFloat(this.searchTextTo.getValue());
                if (parseFloat(num1) > parseFloat(num2)) {
                    Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.advancesearch.valueCheck"), Ext.Msg.INFO);
                    return;
                }
            }

            searchText = (this.searchText.getRawValue().trim() == "" && this.searchTextTo.getRawValue().trim() == "") ? "" : this.searchText.getValue() + " To "+ this.searchTextTo.getValue();
        } else if(this.searchText.getXType()=="datefield"){
            if (!this.searchText.getRawValue().trim() == "" && !this.searchTextTo.getRawValue().trim() == "") {
                if (this.searchText.getValue() > this.searchTextTo.getValue()) {
                    Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.information"), ExtGlobal.getLocaleText("acc.fxexposure.datechk"), Ext.Msg.INFO); // "From Date can not be greater than To Date."
                    return;
                }else {
                    fieldValues.fromDate = this.searchText.getValue();
                    fieldValues.toDate = this.searchTextTo.getValue();
                }
            }
            searchText = (this.searchText.getRawValue().trim() == "" && this.searchTextTo.getRawValue().trim() == "") ? "" : this.searchText.getRawValue().trim() + " To "+ this.searchTextTo.getRawValue().trim();
        } else {
            searchText=this.searchText.getValue();
        }
        var do1=0;
        if (this.searchText.getRawValue() && this.searchText.getRawValue().indexOf("None") != -1) {
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.responsealert.msg.15"), Ext.Msg.INFO);
            return;
        }
        var values= this.searchText.getRawValue().split(",");
        if (column != "" &&  searchText !=""){
            if((this.dimensionBasedComparisionReport || this.isSubLdgerExport)&& this.combovalArr.length>0){
                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"),ExtGlobal.getLocaleText("erp.removethepreviousselectiontogeneratenecomparision"),Ext.Msg.WARNING) ;
                return;
            }
            this.searchText1=this.searchText.getRawValue();
            this.comboSearch = this.searchText1;
            this.combovalArr.push(this.searchText1);

            if((this.searchText.getXType()=="numberfield" || this.searchText.getXType()=="datefield") && this.searchTextTo){
                this.searchText1=this.searchText.getRawValue() + " To "+ this.searchTextTo.getRawValue();
                this.combovalArr.push(this.searchTextTo.getRawValue());
            }

            if(this.searchText.getXType()=="datefield"){
                this.xtypeArr.push("datefield");
                this.xtypeArr.push("datefield");
            } else{
                this.xtypeArr.push(this.searchText.store);
            }

            this.columnText="";
            if(searchText != "") {
                for(var i=0;i<this.columnCombo.getStore().getCount();i++) {
                    if(this.columnCombo.getStore().getAt(i).get("fieldid")== column) {
                        this.columnText=this.columnCombo.getStore().getAt(i).get("header");
                        do1=1;
                    }
                }
                if(do1==1) {

                    this.searchBtn.enable();
                    
                    var index = this.getStore().findBy(function(rec,id){
                        if(rec.get('column')!=null && rec.get('column')!=undefined && rec.get('column')!=""){
                            if(this.columnText == rec.get('column') && this.searchText.moduleid == rec.get('moduleid')){
                                if (this.searchText.moduleid == 30 /*Wtf.Acc_Product_Master_ModuleId*/ && this.searchText.isForProductMasterOnly != rec.get("isForProductMasterOnly")) {
                                    return false;
                                } else {
                                    return true;
                                }
                            }                                   
                        }
                    },this);
//                    if (index != -1 && (this.searchText.getXType()=='combo')) {
//                        var srctxt = this.getStore().getAt(index).get("searchText").indexOf(searchText);
//                        if (srctxt == -1) {
//                            searchText = this.getStore().getAt(index).get("searchText") + "','" + searchText;
//                            this.searchText1 = this.getStore().getAt(index).get("id") + ", '" + this.searchText1 + "'";
//                        } else{
//                            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.msg.FAILURSEARCHETITLE"),ExtGlobal.getLocaleText("acc.advance.search.ErrorMsg"),Ext.Msg.INFO);   
//                            return false;
//                        }
//                    }else{
                        this.searchText1="'"+this.searchText1+"'"; 
//                    }
                    if(this.searchText.fieldtype == 4){
                        var selectedRecordId = searchText;
                        this.searchText.store. findBy(function(rec,id){
                            if(rec.get('parentid')!=null && rec.get('parentid')!=undefined && rec.get('parentid')!=""){
                                if(selectedRecordId == rec.get('parentid')){
                                    searchText+=(","+rec.get('id'))
                                    var childIds = this.getChildIds(rec.get('id'));
                                    searchText+=childIds;
                                }
                                   
                            }
                        },this)
                    }
                    var searchRecord = SearchFieldsModel.create({
                        column: this.columnText,
                        searchText: searchText,
                        dbname:column,
                        id:this.searchText1,
                        xtype:this.searchText.getXType(),
                        iscustomcolumn:this.searchText.iscustomcolumn,
                        iscustomcolumndata:this.searchText.iscustomcolumndata,
                        isfrmpmproduct:this.searchText.fromproduct,
                        xfield:this.searchText.dbname,
                        fieldtype:this.searchText.fieldtype,
                        refdbname:this.searchText.refdbname,
                        isinterval: this.isinterval,
                        interval: this.interval,
                        isbefore: this.isbefore,
                        isdefaultfield:this.searchText.isdefaultfield,
                        combosearch:this.comboSearch,
                        moduleid:this.searchText.moduleid,
                        modulename:this.searchText.modulename,
                        isForProductMasterOnly:this.searchText.isForProductMasterOnly,
                        fieldValues : fieldValues
                    });
                    
                    if(this.searchTextTo !=undefined && this.searchTextTo.getValue()==undefined){
                        Ext.CustomMsg(ExtGlobal.getLocaleText("acc.msg.ALERTTITLE"),ExtGlobal.getLocaleText("acc.responsealert.msg.13"),Ext.Msg.WARNING);
                        clearFields=false;
                    }else{
                        var rec = this.getStore().getAt(index );
                        if (index == -1) {
                            this.getStore().add(searchRecord);
                        } else if (rec.data.moduleid == this.searchText.moduleid) {
                            this.getStore().remove(this.getStore().getAt(index));
                            this.getStore().insert(index, searchRecord);
                        } else {
                            this.getStore().add(searchRecord);
                        }
                    }
                }
            }
        } else {
            if(column == "") {
                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.msg.ALERTTITLE"),ExtGlobal.getLocaleText("acc.responsealert.msg.12"),Ext.Msg.WARNING);
                clearFields=false;
            } else if(searchText =="") {
                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.msg.ALERTTITLE"),ExtGlobal.getLocaleText("acc.responsealert.msg.13"),Ext.Msg.WARNING);
            }
        }
        if(clearFields){
            this.searchText.setValue("");
            if(this.searchTextTo)
                this.searchTextTo.setValue("");
        }
    },
    
    getChildIds:function(parentId){
        var childIds = "";
        this.searchText.store. findBy(function(rec,id){
            if(rec.get('parentid')!=null && rec.get('parentid')!=undefined && rec.get('parentid')!=""){
                if(parentId == rec.get('parentid')){
                    childIds+=(","+rec.get('id'))
                    var cIds = this.getChildIds(rec.get('id'));
                    childIds+=cIds;
                }
            }
        },this);
        
        return childIds;
    },
    doSearch:function(formatdate){
        if ( this.getStore().getCount() > 0 ){
            var filterJson=[];
            var i=0;

            this.getStore().each(function(filterRecord){
                ///  for combo case also searchText is combodataid
                var searchText=filterRecord.data.searchText+"";
                var fieldValues = filterRecord.data.fieldValues;
                var value="";
                var xType = "";
                value=filterRecord.data.searchText+"";
                var xtype=filterRecord.data.xtype;
                if(xtype==undefined)
                    xtype = 'combo';
                
                if(xtype == "datefield"){ // Converting date from user date format to format like "Jun 07, 2016" which is used at java side
                    if(fieldValues!=undefined && fieldValues.fromDate!=undefined && fieldValues.toDate!=undefined)
                    {
                        var fromDate = fieldValues.fromDate;
                        var toDate = fieldValues.toDate;
                    
                        var fromDateString = fromDate.toLocaleFormat('%b %d, %Y');  // Converting date to format like "Jun 07, 2016"
                        var toDateString = toDate.toLocaleFormat('%b %d, %Y');
                    
                        searchText = fromDateString+" To "+toDateString;
                        value = searchText;
                    }
                }
                
                searchText = ExtGlobal.replaceAll(searchText, "\\\\" , "\\\\");
                if(this.combovalArr[i])
                    this.combovalArr[i] = ExtGlobal.replaceAll(this.combovalArr[i], "\\\\" , "\\\\");
                value =  ExtGlobal.replaceAll(value, "\\\\" , "\\\\");
                // object is push in xtypeArr for Combo field else it is datefield. No value is push for numberfield and textfield.
                if(this.xtypeArr[i]!=undefined){
                    if(this.xtypeArr[i]=="datefield"){
                        xType ='datefield'
                    }else if(typeof(this.xtypeArr[i])=="object"){
                        xType ='Combo'
                    }
                }else {
                    xType ="";
                }
                filterJson.push({
                    column : filterRecord.data.dbname  != undefined ? filterRecord.data.dbname :"",
                    refdbname : filterRecord.data.refdbname  != undefined ? filterRecord.data.refdbname :"",
                    xfield : filterRecord.data.xfield != undefined ? filterRecord.data.xfield : "",
                    iscustomcolumn : filterRecord.data.iscustomcolumn  != undefined ? filterRecord.data.iscustomcolumn :false,
                    iscustomcolumndata : filterRecord.data.iscustomcolumndata != undefined ? filterRecord.data.iscustomcolumndata :false,
                    isfrmpmproduct : filterRecord.data.isfrmpmproduct != undefined ? filterRecord.data.isfrmpmproduct :false,
                    fieldtype : filterRecord.data.fieldtype  != undefined ? filterRecord.data.fieldtype : "",
                    searchText : searchText != undefined ? searchText : "",
                    columnheader : encodeURIComponent(filterRecord.data.column),
                    search : value != undefined ? value : "",
                    xtype : xtype != undefined ? xtype : "",
                    combosearch : encodeURIComponent(filterRecord.data.combosearch),
                    isinterval : filterRecord.data.isinterval != undefined ? filterRecord.data.isinterval :false,
                    interval : filterRecord.data.interval,
                    isbefore : filterRecord.data.isbefore != undefined ? filterRecord.data.isbefore :false,
                    isdefaultfield : filterRecord.data.isdefaultfield != undefined ? filterRecord.data.isdefaultfield :false,
                    moduleid : filterRecord.data.moduleid,
                    isForProductMasterOnly : filterRecord.data.isForProductMasterOnly != undefined ? filterRecord.data.isForProductMasterOnly :false
                });
                i++;
            },this);

            filterJson = {
                root:filterJson
            }
            this.fireEvent("filterStore",Ext.encode(filterJson),this.appendCaseCombo.getValue());

        }else{
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.msg.ALERTTITLE"),ExtGlobal.getLocaleText("acc.responsealert.msg.14"),Ext.Msg.WARNING);
            this.fireEvent("filterStore","");
        }
    },
    cancelSearch:function(){
        this.columnCombo.setValue("");
        var searchXtype = this.searchText.getXType();
        if(searchXtype=='combo')
            this.columnCombo.fireEvent("select",undefined,'');
        (this.toDate != undefined)?this.toDate.setValue(""):null;
        (this.fromDate != undefined)?this.fromDate.setValue(""):null;
        this.searchStore.removeAll();
        this.combovalArr=[];
        this.xtypeArr=[];
        this.fireEvent("clearStoreFilter");
    },
    deleteFilter:function(view, cell, cellIndex, record, row, rowIndex, event) {
        if(event.target.className == "pwnd delete-gridrow") {
            this.searchStore.remove(this.searchStore.getAt(rowIndex));
            if(this.searchStore.getCount()==0) {
                this.searchBtn.disable();
            }
            this.combovalArr.splice(rowIndex,1);
            this.xtypeArr.splice(rowIndex,1);
            if(!this.dimensionBasedComparisionReport){
                this.doSearch();
            }
        }

    },
    createSearchFields:function(combo,record){
        var recXtype = record == '' ?  "textfield" : record.get('xtype');
        if (recXtype == "None"){
            record.set('xtype','textfield');
        }
        
        if (this.searchText){
            this.searchText.destroy();
            this.searchText = undefined;
        }
        if (this.searchTextTo){
            this.searchTextTo.destroy();
            this.searchTextTo = undefined;
        }
        var iscustomcolumn,fieldtype,refdbname,dbname,iscustomcolumndata,isfrmpmproduct,isdefaultfield=false;
        var moduleid='';
        var modulename='';
        var isForProductMasterOnly="";
        if(record!=''){
            iscustomcolumn=record.get('iscustomcolumn');
            iscustomcolumndata=record.get('iscustomcolumndata');
            isfrmpmproduct=record.get('moduleid')== 30//Wtf.Acc_Product_Master_ModuleId;
            moduleid=record.get('moduleid');
            modulename=record.get('modulename');
            this.modulename = record.get('modulename');
            if(this.moduleid == 102/*Wtf.Acc_Ledger_ModuleId*/){
                isfrmpmproduct=record.get('isfrmpmproduct');
            }
            fieldtype=record.get('fieldtype');
            refdbname = record.get('refdbname');
            dbname=record.get('dbname');
            isdefaultfield=record.get('isdefaultfield');
            isForProductMasterOnly=record.get('isForProductMasterOnly');
        }

        iscustomcolumn = iscustomcolumn?iscustomcolumn:"";
        
        if (recXtype == "textfield" || recXtype == 'Text' || recXtype =='textarea'){
            this.searchText = Ext.create('Ext.form.TextField',{
                fieldLabel: ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt"),//Search Text,
                emptyText : ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt"),//Search Text,
                labelWidth:70,
                width:200,
                fromproduct:isfrmpmproduct,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield,
                moduleid:moduleid,
                modulename:modulename,
                isForProductMasterOnly:isForProductMasterOnly
            });
        }

        else if (recXtype == "numberfield" || recXtype == 'Number(Integer)' || recXtype == 'Number(Float)'){
            this.searchText = Ext.create('Ext.form.field.Number',{
                fieldLabel : ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.from"),
                emptyText : ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.from"),
                maxLength: 100,
                labelWidth:70,
                width:180,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield,
                moduleid:moduleid,
                modulename:modulename,
                isForProductMasterOnly:isForProductMasterOnly
            });
            this.searchTextTo = Ext.create('Ext.form.field.Number',{
                fieldLabel : ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.to"),
                emptyText : ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.to"),
                maxLength: 100,
                labelWidth:55,
                width:180,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield,
                moduleid:moduleid,
                modulename:modulename,
                isForProductMasterOnly:isForProductMasterOnly
            });
        }

        else if (recXtype == "combo" || recXtype == "Combobox" || recXtype == "select" || recXtype == "fieldset"){
            
            var searchTextStore = getComboFieldStore(record.data.fieldid , record.get('header') , moduleid , isdefaultfield);
          
            searchTextStore.load();
            
            this.searchText = Ext.create('Ext.form.field.ComboBox',{
                id:'mulaccountcombo'+this.id,
                multiSelect:true,
                fieldLabel:ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt") ,
                emptyText :ExtGlobal.getLocaleText("acc.advancesearch.searchanoption"),
                valueField: 'id',
                displayField: "name",
                store: searchTextStore,
                forceSelection :true,
                queryMode: 'local',
                triggerAction: 'all',
                labelWidth:70,
                width:300,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield,
                moduleid:moduleid,
                modulename:modulename,
                isForProductMasterOnly:isForProductMasterOnly
            }); 
        }

        else if (recXtype == "datefield" || recXtype == 'Date' ){
            this.searchText = Ext.create('Ext.form.DateField',{
                fieldLabel : ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate"),
                emptyText : ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate"),
                width:180,
                labelWidth:70,
                format: ExtGlobal.getOnlyDateFormat(),
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield,
                moduleid:moduleid,
                modulename:modulename,
                isForProductMasterOnly:isForProductMasterOnly
            });
            this.searchTextTo = Ext.create('Ext.form.DateField',{
                fieldLabel : ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.todate"),
                emptyText : ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.todate"),
                width:180,
                labelWidth:55,
                format: ExtGlobal.getOnlyDateFormat(),
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                moduleid:moduleid,
                modulename:modulename,
                isForProductMasterOnly:isForProductMasterOnly
            });
        }
        
        else if (recXtype == "checkbox" ){
            var checkboxStore = Ext.create('Ext.data.ArrayStore',{
                fields:['id','name'],
                data: [["TRUE","TRUE"],
                ["FALSE","FALSE"]
                ],
                autoLoad: true
            });
            this.searchText = Ext.create('Ext.form.field.ComboBox',{
                fieldLabel : ExtGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt"),
                emptyText : ExtGlobal.getLocaleText("acc.advancesearch.searchanoption"),
                multiSelect:true,
                valueField: 'id',
                displayField: 'name',
                store: checkboxStore,
                forceSelection :true,
                queryMode: 'local',
                triggerAction: 'all',
                labelWidth:70,
                width:200,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield,
                moduleid:moduleid,
                modulename:modulename,
                isForProductMasterOnly:isForProductMasterOnly
            }); 
        }
        
        this.getDockedItems('toolbar[dock="top"]')[0].insert(3,this.searchText);
        if(this.searchTextTo){
            this.getDockedItems('toolbar[dock="top"]')[0].insert(6,this.searchTextTo);
        }
    },
    getComboData: function() {
        if (!this.myData) {
            var mainArray = [];
            var params;
            if (this.isOnlyGlobalCustomColumn) {
                params = {
                    moduleid: this.moduleid,
                    isActivated: 1
                }
            } else {
                params = {
                    customcolumn: (this.reportid == 819) ? 1 : 0,
                    moduleid: this.moduleid,
                    jeId: (this.record) ? this.record.data.journalentryid : "",
                    isAdvanceSearch:true,
                    iscustomdimension:this.dimensionBasedComparisionReport,
                    isActivated:1,
                    isCustomDetailReport:(this.moduleid==102 && this.isCustomDetailReport==true)?true:false,//send for custom details report
                    excludeModule: "",
                    customerCustomFieldFlag:this.customerCustomFieldFlag,
                    vendorCustomFieldFlag:this.vendorCustomFieldFlag,
                    reportid:this.reportid,
                    moduleidarray:this.moduleidarray,
                    isAvoidRedundent:this.isAvoidRedundent,
                    ignoreDefaultFields:this.ignoreDefaultFields
                }
            }
            Ext.Ajax.request({
                url: "ACCAccountCMN/getFieldParams.do",
                scope:this,
                params: params,
                success:function(res,req){
                    var responseObj=eval("("+res.responseText+")");
                    var responseData = responseObj.data.data;
                    this.sdate = "0";
                    this.edate = "0";
                    if (responseData != '' && responseData != null) {
                        for (var i = 0; i < responseData.length; i++) {
                            var tmpJson = {};
                            var header = headerCheck(ExtGlobal.HTMLStripper(responseData[i].fieldlabel));
                            header = header.replace("*", "");
                            header = header.trim();
                            tmpJson.header = header;
                            tmpJson.name = responseData[i].column_number;
                            tmpJson.xtype = ExtGlobal.getXType(responseData[i].fieldtype);
                            tmpJson.cname = responseData[i].column_number;
                            tmpJson.iscustomcolumn = true;
                            tmpJson.dbname = responseData[i].column_number;
                            tmpJson.sheetEditor = responseData[i].xfield;
                            tmpJson.fieldtype = responseData[i].fieldtype;
                            tmpJson.refdbname = responseData[i].column_number;
                            tmpJson.fieldid = responseData[i].fieldid;
                            tmpJson.iscustomcolumndata = responseData[i].iscustomcolumn;
                            tmpJson.moduleid = responseData[i].moduleid;
                            tmpJson.modulename = responseData[i].modulename;
                            tmpJson.isdefaultfield = responseData[i].isdefaultfield;
                            tmpJson.isfrmpmproduct = responseData[i].isfrmpmproduct;
                            tmpJson.isForProductMasterOnly = responseData[i].isForProductMasterOnly;
                            mainArray.push(tmpJson)
                        }
                        this.myData = mainArray;
                        if (this.advSearch)
                            this.columnCombo.getStore().loadData(this.myData);
                    }
                }
            });
        }
    }
});