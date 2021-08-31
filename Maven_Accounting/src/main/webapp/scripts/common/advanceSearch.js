/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
Wtf.advancedSearchComponent = function(config) {
    this.arr = [];
    Wtf.apply(this, config);
    this.createTbar();
    this.advGrid = new Wtf.advancedSearchComponentgrid({
        panel: this
    }, config);

    Wtf.advancedSearchComponent.superclass.constructor.call(this, {
        region: 'north',
        height: this.dimensionBasedComparisionReport ? 100 : 150,
        hidden: true,
        layout: 'fit',
        tbar: this.btnArr,
        items: [this.advGrid]
    });
}
Wtf.extend(Wtf.advancedSearchComponent, Wtf.Panel, {
    createTbar: function() {
        var tranStore = new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data: [["1", "Transaction Level"],
                ["2", "Line Level"]
            ],
            autoLoad: true
        });
        this.searchCaseComboValue = "1";

        this.transationData = "Transaction data On : ";
        this.serachCombo = new Wtf.form.ComboBox({
            selectOnFocus: true,
            triggerAction: 'all',
            mode: 'local',
            store: tranStore,
            useDefault: true,
            displayField: 'name',
            typeAhead: true,
            valueField: 'id',
            anchor: '100%',
            hidden: !this.lineLevelSearch,
            value: this.searchCaseComboValue
        });

        this.Tax = new Wtf.form.Checkbox({
            name: 'Tax',
            id: 'Tax' + this.heplmodeid + this.id,
            checked: false,
            hidden: !this.lineLevelSearch
        });
        this.Discount = new Wtf.form.Checkbox({
            name: 'Discount',
            id: 'Discount' + this.heplmodeid + this.id,
            checked: false,
            hidden: !this.lineLevelSearch
        });
        this.btnArr = [];
        if (this.lineLevelSearch) {
            this.btnArr.push(this.transationData);
            this.btnArr.push(this.serachCombo);
            this.btnArr.push('-', this.Tax);
            this.btnArr.push("Including Tax");
            this.btnArr.push('-', this.Discount);
            this.btnArr.push("Including Discount/Term");
        }
    }
});
/*
 *Added 2 events in advance search component:
 * 1. beforeadd : event will fire before adding record in store.
 * Whatever operation you want perform before adding record you can handle that in this event.
 * If you return false from this event that record will not be added in store.
 * 
 * for example.
 * In Dimension based report I want to add only one record in store.If user add second record I want to show pop up message.
 * I will handle this as below.
 * objSearchComponent.advGrid.on("beforeadd",function(grid,rec){
 *      if(grid.getStore().getRange().length > 0){
 *          Wtf.Msg.alert("Warning","Delete already selected value");
 *          return false;
 *      }
 *      return true;
 * });
 * 
 * 2. afteradd : 
 * Whatever operation you want perform after adding record you can handle that in this event.
 * 
 * for example.
 * objSearchComponent.advGrid.on("afteradd",function(grid,rec){
 *       Wtf.Msg.alert("Alert","Record added.");
 * });
 **/

Wtf.advancedSearchComponentgrid = function(gridConfig,config){
    Wtf.apply(this, config);
    Wtf.apply(this, gridConfig);
    this.checkRecXtype=config.checkRecXtype
    this.events = {
        "filterStore": true,
        "beforeadd": true,
        "afteradd": true,
        "clearStoreFilter": true
    };
    
    this.combovalArr=[];
    this.xtypeArr=[];
    this.dimensionBasedComparisionReport=config.dimensionBasedComparisionReport==undefined?false:config.dimensionBasedComparisionReport;
    this.dimBasedSkipRequestParams=config.dimBasedSkipRequestParams==undefined?false:config.dimBasedSkipRequestParams;
    this.hideRememberSerchConfig=config.hideRememberSerch==undefined?false:config.hideRememberSerch;
    this.hideRememberSerch=false;
    this.hideFilterConjunction=false;
    this.hideSearchBttn = config.hideSearchBttn == undefined ? false : config.hideSearchBttn;
    this.hideCloseBttn = config.hideCloseBttn == undefined ? false : config.hideCloseBttn;
    this.isOnlyGlobalCustomColumn=config.isOnlyGlobalCustomColumn==undefined?false:config.isOnlyGlobalCustomColumn;
    this.isSubLdgerExport=config.isSubLdgerExport==undefined?false:config.isSubLdgerExport;
    this.templateid=config.templateid==undefined?'':config.templateid;//For Custom Layout templates
    this.templatetitle=config.templatetitle==undefined?'':config.templatetitle;//For Custom Layout templates
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
    if(!this.isSubLdgerExport && (this.reportid==Wtf.autoNum.dimensionBasedProfitLoss || this.reportid==Wtf.autoNum.dimensionBasedBalanceSheet|| this.reportid==Wtf.autoNum.dimensionBasedTrialBalance || Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout)){
        this.hideRememberSerch=false;
    }
    this.globallevelfields = config.globallevelfields==undefined ? false : config.globallevelfields;
    this.linelevelfields = config.linelevelfields==undefined ? false : config.linelevelfields;
    this.isSameFieldFlag = true;
    this.isCustomDetailReport=config.isCustomDetailReport==undefined?false:config.isCustomDetailReport;
    this.combostore = new Wtf.data.SimpleStore({
        fields: [
        {
            name: 'header'
        },

        {
            name: 'name'
        },
        {
            name: 'xtype'
        },
        {
            name: 'cname'
        },
        {
            name: 'iscustomcolumn'
        },
        {
            name: 'dbname'
        },
        {
            name:'sheetEditor'
        },
        {
            name:'fieldtype'
        },
        {
            name: 'refdbname'
        },
        {
            name: 'fieldid'
        },{
            name:'iscustomcolumndata'
        },{
            name:'moduleid'
        },{
            name:'modulename'
        },{
            name: 'isdefaultfield'
        },{
            name: 'isfrmpmproduct'
        },{
            name: "isForProductMasterOnly"
        },{
            name:'isRangeSearchField',
            type: 'boolean'
        },{
            name: "iscustomfield",
            type: 'boolean'
        },{
            name: "isMultiEntity",
            type: 'boolean'
        },{
            name:"isForProductMasterSearch"
        }]
    });

    this.columnCombo = new Wtf.form.ExtFnComboBox({
        store : this.combostore,
        editable:false,
        typeAhead: true,
        selectOnFocus:true,
        displayField:'header',
        valueField : 'fieldid',
        triggerAction: 'all',
        emptyText : WtfGlobal.getLocaleText("acc.responsealert.msg.12"),//'Select a Search Field to search',
        mode:'local',
        isAdvanceSearchCombo:true,
        extraComparisionField: "",
        extraFields: "",
        listWidth: 400,
        ctCls : 'widthforCmb'
    });
    this.columnCombo.on('beforeselect', function(combo, record, index) {
        if(record.get('fieldid')=="NA"){
                 return false;
        }
        /**
         * Restricted the user from selecting cross fields in Advance Search. // ERP-29974
         */
        if(this.reportid!=undefined && this.reportid!='' && (this.reportid==Wtf.autoNum.GeneralLedger || this.reportid==Wtf.autoNum.GroupDetailReport || this.reportid==Wtf.TrialBalance_Moduleid)){
            this.isSameFieldFlag = true;
            if(this.searchStore.getCount() > 0){
                this.searchStore.each(function(filterRecord){
                    var recdata = filterRecord.data;
                    if(recdata.isdefaultfield != record.get('isdefaultfield')){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.report.selectsearchfield.alert")],2) ;
                        this.isSameFieldFlag = false;
                        return false;
                    }
                },this);
            }
        }
    }, this);
    this.columnCombo.on("select",this.displayField,this);


    this.cm=new Wtf.grid.ColumnModel([{
        header: WtfGlobal.getLocaleText("acc.customreport.header.column"),//"Column",
        dataIndex:'column',
        renderer : function(val, cell, row, rowIndex, colIndex, ds) {
                if (row.get("modulename") == "") {
                    return val;
                } else {
                    return val + " [" + row.get("modulename") + "]";
                }
        }
    },{
        header: WtfGlobal.getLocaleText("acc.advancesearch.search1txt"),
        dataIndex:'searchText',
        hidden:true

    },
    {
        header: WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt"),//"Search Text",
        dataIndex:'id'
    },{
        header: WtfGlobal.getLocaleText("acc.DELETEBUTTON"),//"Delete",
        dataIndex:'delField',
        renderer : function(val, cell, row, rowIndex, colIndex, ds) {
            return "<div class='pwnd delete-gridrow' > </div>";
        }
    }
    ]);

    this.searchRecord = Wtf.data.Record.create([{
        name: 'column'
    },{
        name: 'searchText'
    },{
        name: 'dbname'
    },
    {
        name: 'id'
    },{
        name: 'xtype'
    },{
        name: 'refdbname'
    },{
        name: 'iscustomcolumn'
    },{
        name:'xfield'
    },{
        name:'fieldtype'
    },{
        name:'iscustomcolumndata'
    },{
        name:'isfrmpmproduct'
    },{ 
        name:'isdefaultfield'
    },{ 
        name:'combosearch'
    },{ 
        name:'moduleid'
    },{
        name:'modulename'
    },{
        name:"isForProductMasterOnly"
    },{
        name:"isinterval"
    },{
        name:"interval"
    },{
        name:"isbefore"
    },{
        name: "iscustomfield",
        type: 'boolean'
    },{
        name: "isMultiEntity",
        type: 'boolean'
    },{
        name: "sdate"
    },{
        name: "edate"
    },{
        name: "isRangeSearchField",
        type: 'boolean'
    },{
        nane:"isForProductMasterSearch"
    }
]);
         
    this.GridJsonReader = new Wtf.data.JsonReader({
        root: "data",
        totalProperty: 'count'
    }, this.searchRecord);

    this.searchStore = new Wtf.data.Store({
        reader: this.GridJsonReader
    });

     this.on("cellclick", this.deleteFilter, this);
     
    this.appendCase = new Wtf.Toolbar.TextItem((this.hideFilterConjunction||this.dimensionBasedComparisionReport)?"":WtfGlobal.getLocaleText("acc.editor.advanceSearch.filter.conjunction"));//'Filter Conjunction');
    var modStore = new Wtf.data.SimpleStore({
        fields:['id','name'],
        data: [["AND","AND"],
        ["OR","OR"]
        ],
        autoLoad: true
    });
    this.appendCaseComboValue = "AND";
    
    this.appendCaseCombo=new Wtf.form.ComboBox({
        selectOnFocus:true,
        triggerAction: 'all',
        mode: 'local',
        store: modStore,
        useDefault:true,
        hidden:this.hideFilterConjunction || this.dimensionBasedComparisionReport,
        displayField: 'name',
        typeAhead: true,
        valueField:'id',
        anchor:'100%',
        width:60,
        ctCls : 'widthforCmb1',
        value:this.appendCaseComboValue
    });
     
    this.appendCaseCombo.on('select',function(a,b,c){
        this.appendCaseComboValue = this.appendCaseCombo.getValue();
    },this); 
    
    this.saveSearchName = new Wtf.ux.TextField({
        anchor: '95%',
        maxLength: 40,
        hidden:this.hideRememberSerch,
        width: 100
    });
   
   this.saveSearch = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.advancesearch.remembersearch"),//'Remember Search',
            tooltip: {text: WtfGlobal.getLocaleText("acc.advancesearch.remembersearch.ttip")},//'Save search state.'},
            handler: this.RememberSearch,
            scope: this,
            disabled:true,
            hidden:this.hideRememberSerch,
            iconCls : 'pwnd add'
    });
    Wtf.advancedSearchComponentgrid.superclass.constructor.call(this, {

//        region :'north',
        height:this.dimensionBasedComparisionReport?100:150,
        layout:'fit',
//        hidden:true,
        store: this.searchStore,
        cm:this.cm,
        stripeRows: true,
        autoScroll : true,
        border:false,
        clicksToEdit:1,
        viewConfig: {
            forceFit:true
        },

        tbar: [this.text1=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText("acc.advancesearch.searchfield")+": "),this.columnCombo,'-',this.text=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt")+": "), this.searchText = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.advancesearch.newmasterrec"),//'New Master Record',
            anchor: '95%',
            maxLength: 100,
            width:125
        }),this.appendCase,this.appendCaseCombo,
        this.add = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.targetlists.creatarr.addBTN"),//'Add',
            tooltip: {text: WtfGlobal.getLocaleText("acc.advancesearch.addtermtosearchtip")},//'Add a term to search.'},
            handler: this.addSearchFilter,
            scope: this,
            iconCls : 'pwnd addfilter'
        }),
        this.search = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.audittrail.searchBTN"),//'Search',
            tooltip: {text: WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")},//'Add terms to search.'},
            handler: this.doSearch,
            hidden: this.hideSearchBttn,
            scope:this,
            disabled:true,
            iconCls : "advanceSearchButton"
        }),
        this.cancel = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.CLOSEBUTTON"),//'Close',
            tooltip: {text: WtfGlobal.getLocaleText("acc.advancesearch.closebtn.ttip")},//'Clear search terms and close advanced search. '},
            handler: this.cancelSearch,
            hidden: this.hideCloseBttn,
            scope:this,
            iconCls:'pwnd clearfilter'
        }),this.saveSearch]
    });

}

Wtf.extend(Wtf.advancedSearchComponentgrid, Wtf.grid.EditorGridPanel, {
    addSearchFilter:function(){
        if(this.columnCombo.getRawValue().trim()==""){
             WtfComMsgBox(107,2);
            return;
        }
        var column =this.columnCombo.getValue();
        var searchText="";
        if(this.searchText.getXType()=="numberfield"){
            if (!this.searchText.getRawValue().trim() == "" && !this.searchTextTo.getRawValue().trim() == "") {
                var num1 = parseFloat(this.searchText.getValue());
                var num2 = parseFloat(this.searchTextTo.getValue());
                if (parseFloat(num1) > parseFloat(num2)) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.advancesearch.valueCheck")], 2);
                    return;
                }
            }
            // 'Blank' as a text added when we are trying to search on blank value.
            searchText = (this.searchText.getRawValue().trim() == "" && this.searchTextTo.getRawValue().trim() == "") ? (this.searchText.isdefaultfield || this.searchText.iscustomcolumndata) ? "" : Wtf.blankSearchKey : this.searchText.getValue() + " To "+ this.searchTextTo.getValue();
        } else if(this.searchText.getXType()=="datefield"){
            if (!this.searchText.getRawValue().trim() == "" && !this.searchTextTo.getRawValue().trim() == "") {
                if (this.searchText.getValue() > this.searchTextTo.getValue()) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
                    return;
                }
            }
            // 'Blank' as a text added when we are trying to search on blank value.
            searchText = (this.searchText.getRawValue().trim() == "" && this.searchTextTo.getRawValue().trim() == "") ? (this.searchText.isdefaultfield || this.searchText.iscustomcolumndata) ? "" : Wtf.blankSearchKey : this.searchText.getRawValue().trim() + " To "+ this.searchTextTo.getRawValue().trim();
        } else if (this.searchText.isRangeSearchField && this.searchTextTo) {
            if (this.searchText.getRawValue().trim() == "" || this.searchTextTo.getRawValue().trim() == "") {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.advancesearch.valueEmptyCheck")], 2); // "From value can not be greater than To value."
                return;
            } else if (!this.searchText.getRawValue().trim() == "" && !this.searchTextTo.getRawValue().trim() == "") {
                if (this.searchText.getValue() > this.searchTextTo.getValue()) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.advancesearch.valueCheck")], 2); // "From value can not be greater than To value."
                    return;
                }
            }
            searchText = (this.searchText.getRawValue().trim() == "" && this.searchTextTo.getRawValue().trim() == "") ? "" : ("between " + "'" + this.searchText.getRawValue().trim() + "'" + " and " + "'" + this.searchTextTo.getRawValue().trim() + "'");
        } else if (this.searchText.getXType() == "textfield") {                 // Text Field , Text Area , Rich Text Area.
            // 'Blank' as a text added when we are trying to search on blank value.
            searchText = this.searchText.getValue().trim();
            if (searchText == "") {
                if (!(this.searchText.isdefaultfield || this.searchText.iscustomcolumndata)) {
                    searchText = Wtf.blankSearchKey;
                }
            }
        } else {
            searchText=this.searchText.getValue().trim();
        }
        if(this.searchText.getXType()=="timefield") {
           searchText =  WtfGlobal.convertToGenericTime(Date.parseDate(searchText,WtfGlobal.getLoginUserTimeFormat()));
        }
        var do1=0;
        if (this.searchText.getRawValue() && this.searchText.getRawValue().indexOf("None") != -1) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.responsealert.msg.15")], 2);
            return;
        }
        var values= this.searchText.getRawValue().split(",");
        if (column != "" &&  searchText !=""){
            if((this.dimensionBasedComparisionReport || this.isSubLdgerExport)&& this.combovalArr.length>0){
                  WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.removethepreviousselectiontogeneratenecomparision")],2) ;
                  return;
            }/*else if(this.dimensionBasedComparisionReport &&values.length >10){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.removethepreviousselectiontogeneratenecomparision")],2) ;
                 return;
            }*/
            this.searchText1=this.searchText.getRawValue();
            this.comboSearch = this.searchText1;
            this.combovalArr.push(this.searchText1);

            if((this.searchText.getXType()=="numberfield" || this.searchText.getXType()=="datefield") && this.searchTextTo){
                if (searchText == Wtf.blankSearchKey) {            // Search on Blank value.  this.searchText1 is Display Value and searchText is ID
                    this.searchText1 = Wtf.blankSearchText;
                } else {
                    this.searchText1 = this.searchText.getRawValue() + " To " + this.searchTextTo.getRawValue();
                }
                
                this.combovalArr.push(this.searchTextTo.getRawValue());
            } else if(this.searchText.isRangeSearchField && this.searchTextTo){
                this.searchText1="between "+ "'"+this.searchText.getRawValue().trim()+"'" + " and "+ "'"+this.searchTextTo.getRawValue().trim()+"'";
                this.combovalArr.push(this.searchTextTo.getRawValue());
            } else if (this.searchText.getXType() == "textfield" && searchText == Wtf.blankSearchKey) {
                this.searchText1 = Wtf.blankSearchText;
            }

            if(this.searchText.getXType()=="datefield"){
                this.xtypeArr.push("datefield");
                this.xtypeArr.push("datefield");
            } else{
                if(this.searchText.getXType()=="numberfield")
                    this.xtypeArr.push(this.searchText.store);
                this.xtypeArr.push(this.searchText.store);
            }

            this.columnText="";
            if(searchText != "") {
                for(var i=0;i<this.combostore.getCount();i++) {
                    if(this.combostore.getAt(i).get("fieldid")== column) {
                        this.columnText=this.combostore.getAt(i).get("header");
                        do1=1;
                    }
                }
                
                this.iscustomfield=false;//SDP-4889
                this.isMultiEntity=false;
                if(this.columnCombo!=null &&this.columnCombo!=undefined && this.columnCombo!="undefined" && this.columnCombo!=""&& this.columnCombo.store!=null&& this.columnCombo.store!=undefined&& this.columnCombo.store!="undefined"){
                    this.columnStore=this.columnCombo.store;
                    for(var i=0;i<this.columnStore.getCount();i++) {
                        if(this.columnStore.getAt(i).get("fieldid")== column) {
                            this.iscustomfield=this.columnStore.data.items[i].data.iscustomfield;
                            this.isMultiEntity=this.columnStore.data.items[i].data.isMultiEntity;
                        }
                    }
                }
                
                if(do1==1) {

                    if(this.isinterval!=true){
                        this.isinterval = false;
                        this.interval = "";
                        this.isbefore = "";
                    }

                    this.search.enable();
                    this.saveSearch.enable();
                    this.search.setTooltip(WtfGlobal.getLocaleText("acc.advancesearch.searchonmulterms"));
                    
//                    var index=this.searchStore.find('column',this.columnText);
                    var index = this.searchStore.findBy(function(rec,id){
                        if(rec.get('column')!=null && rec.get('column')!=undefined && rec.get('column')!=""){
                            if(this.columnText == rec.get('column') && this.searchText.moduleid == rec.get('moduleid')){
                                if (this.searchText.moduleid == Wtf.Acc_Product_Master_ModuleId && this.searchText.isForProductMasterOnly != rec.get("isForProductMasterOnly")) {
                                    return false;
                                } else {
                                    return true;
                                }
                            }                                   
                        }
                    },this);
                       if (index != -1 && (this.searchText.getXType()=='combo')) {
                          var srctxt = this.searchStore.getAt(index).get("searchText").indexOf(searchText);
                        if (srctxt == -1) {
                            searchText = this.searchStore.getAt(index).get("searchText") + "','" + searchText;
                            this.searchText1 = this.searchStore.getAt(index).get("id") + ", '" + this.searchText1 + "'";
                        } else{
                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.FAILURSEARCHETITLE"),WtfGlobal.getLocaleText("acc.advance.search.ErrorMsg")],0)   
                         return false;
                        }
                       }else{
                          this.searchText1="'"+this.searchText1+"'"; 
                       }
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
                    var searchRecord = new this.searchRecord({
                        sdate: this.sdate, //(this.sdate !== undefined? this.sdate:0), //ERP-33751
                        edate: this.edate, //(this.edate !== undefined? this.edate:0),
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
                        isRangeSearchField:this.searchText.isRangeSearchField,
                        isForProductMasterOnly:this.searchText.isForProductMasterOnly,
                        isForProductMasterSearch:this.searchText.isForProductMasterSearch,
                        iscustomfield:(this.iscustomfield==""||this.iscustomfield=="undefined"||this.iscustomfield==undefined)?false:this.iscustomfield,
                        isMultiEntity:(this.isMultiEntity==""||this.isMultiEntity=="undefined"||this.isMultiEntity==undefined)?false:this.isMultiEntity
                    });
                    
                    if(this.fireEvent("beforeadd",this,searchRecord) == false){
                        return;
                    }

                    var rec = this.searchStore.getAt(index );
                  //  var index=this.searchStore.find('column',this.columnText);
                    var showAlertFlag = false;
//                    if(this.isCustomDetailReport || this.reportid==Wtf.autoNum.CustomerRevenueReport){
//                        for(var i=0 ; i<this.searchStore.getCount() ; i++){
//                            if(!(this.searchText.moduleid == this.searchStore.data.items[i].data.moduleid)){
//                                showAlertFlag = true;
//                                break;
//                            }
//                        }
//                    }
                  
                    if (showAlertFlag) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.report.selectdimensioncustomfield.alert")],2) ;
                        return;
                    } else if (index == -1) {
                        this.searchStore.add(searchRecord);
                    } else if (rec.data.moduleid == this.searchText.moduleid) {
                        this.searchStore.remove(this.searchStore.getAt(index));
                        this.searchStore.insert(index, searchRecord);
                    } else {
                        this.searchStore.add(searchRecord);
                    }
                    this.fireEvent("afteradd",this,searchRecord); 
                }
            }
        } else {
            if(column == "") {
                  WtfComMsgBox(107,2);
                } else if(searchText =="") {
                     WtfComMsgBox(108,2);
                }
            }
        this.searchText.setValue("");
        if(this.searchTextTo)
            this.searchTextTo.setValue("");
//        if(this.intervalCombo)
//            this.intervalCombo.setValue("");
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
    
    getJsonofStore : function(){
       var filterJson=[];
       var i=0;
       this.searchStore.each(function(filterRecord){

            var searchText=filterRecord.data.searchText+"";
            var xtype=filterRecord.data.xtype;

            if (xtype == 'datefield' || xtype =='Date' ){
                 if(filterRecord.data.searchText && filterRecord.data.searchText.format)
                        searchText=WtfGlobal.convertToOnlyDate(filterRecord.data.searchText);
            }
            searchText = WtfGlobal.replaceAll(searchText, "\\\\" , "\\\\");
            if(this.combovalArr[i])
                this.combovalArr[i] = WtfGlobal.replaceAll(this.combovalArr[i], "\\\\" , "\\\\");
           // value =  WtfGlobal.replaceAll(value, "\\\\" , "\\\\");
            //searchText = searchText.replace(/"/g,"");
            var recdata = filterRecord.data;
            filterJson.push({
                column:encodeURIComponent(recdata.column),
                refdbname:recdata.refdbname,
                xfield:recdata.xfield,
                iscustomcolumn:recdata.iscustomcolumn,
                columnheader:encodeURIComponent(recdata.column),
                moduleid:recdata.moduleid,//this.moduleid,
                modulename: this.modulename,
                iscustomcolumndata:recdata.iscustomcolumndata,
                isfrmpmproduct:recdata.isfrmpmproduct,
                fieldtype:recdata.fieldtype,
                searchText:searchText,
                dbname : recdata.dbname,
                id : recdata.id,
                xtype:xtype,
                combosearch:recdata.combosearch,
                isinterval: recdata.isinterval,
                interval: recdata.interval,
                isbefore: recdata.isbefore,
                isForProductMasterOnly:recdata.isForProductMasterOnly,
                isdefaultfield:recdata.isdefaultfield,
                personid:(this.reportid==Wtf.autoNum.vendorRegistryReport || this.reportid==Wtf.autoNum.customerRegistryReport)?this.panel.person:"",
                moduleids:(this.reportid==Wtf.autoNum.vendorRegistryReport || this.reportid==Wtf.autoNum.customerRegistryReport)?this.panel.modules:"",
                sdate: this.sdate, //(this.sdate !== undefined? this.sdate:0),
                edate: this.edate, //(this.edate !== undefined? this.edate:0)
                viewCombo: this.viewCombo
            });
            i++;
        },this);

        filterJson = {
            data:filterJson
        }

        return Wtf.encode(filterJson);
    },
    RememberSearch:function(){
        var module=this.moduleid;
        switch (this.reportid) {
                case Wtf.autoNum.agedDetailsBasedOnDimension:
                    module = Wtf.autoNum.agedDetailsBasedOnDimension;
                    break;
                case Wtf.autoNum.agedDetailsBasedOnDimensionDetailed:
                    module = Wtf.autoNum.agedDetailsBasedOnDimensionDetailed;
                    break;
                case Wtf.autoNum.salesByProductCategoryDetail:
                    module = Wtf.autoNum.salesByProductCategoryDetail;
                    break; 
                case Wtf.autoNum.SalesByServiceProductDetailReport:
                    module = Wtf.autoNum.SalesByServiceProductDetailReport;
                    break;
                 case Wtf.autoNum.agedpayablereportBasedOnDimension:
                    module = Wtf.autoNum.agedpayablereportBasedOnDimension;
                    break;
                 case Wtf.autoNum.agedpayabledetailedreportBasedOnDimension:
                    module = Wtf.autoNum.agedpayabledetailedreportBasedOnDimension;
                    break;
                case Wtf.autoNum.StockLedger:
                    module = Wtf.autoNum.StockLedger;
                    break;
                case Wtf.autoNum.CreditNoteWithAccountDetail:
                    module = Wtf.autoNum.CreditNoteWithAccountDetail;
                    break;
                case Wtf.Acc_Customer_AccountStatement_ModuleId:
                    module = Wtf.Acc_Customer_AccountStatement_ModuleId;
                    break;
                case Wtf.Acc_Vendor_AccountStatement_ModuleId:
                    module = Wtf.Acc_Vendor_AccountStatement_ModuleId;
                    break;
                case Wtf.autoNum.StockReportOnDimension:
                    module = Wtf.autoNum.StockReportOnDimension;
                    break;
                case Wtf.autoNum.SalesPurchaseReport:
                    module = Wtf.autoNum.SalesPurchaseReport;
                    break;
                case Wtf.autoNum.AgedReceivableDetailReport:
                    module = Wtf.autoNum.AgedReceivableDetailReport;
                    break;
                case Wtf.autoNum.dimensionBasedProfitLoss:
                    module = Wtf.autoNum.dimensionBasedProfitLoss;
                    break;
                case Wtf.autoNum.profitAndLossMonthlyCustomLayout:
                    module = Wtf.autoNum.profitAndLossMonthlyCustomLayout;
                    break;
                case Wtf.autoNum.balanceSheetMonthlyCustomLayout:
                    module = Wtf.autoNum.balanceSheetMonthlyCustomLayout;
                    break;
                case Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout:
                    module = Wtf.autoNum.dimensionBasedMonthlyPLCustomLayout;
                    break;
                case Wtf.autoNum.dimensionBasedBalanceSheet:
                    module = Wtf.autoNum.dimensionBasedBalanceSheet;
                    break;
                case Wtf.autoNum.dimensionBasedTrialBalance:
                    module = Wtf.autoNum.dimensionBasedTrialBalance;
                    break;
                case Wtf.autoNum.vendorRegistryReport:
                    module = Wtf.autoNum.vendorRegistryReport;
                    break;
                case Wtf.autoNum.customerRegistryReport:
                    module = Wtf.autoNum.customerRegistryReport;
                    break;
                case Wtf.autoNum.EntityBasedGSTReport:
                    module = Wtf.autoNum.EntityBasedGSTReport;
                    break;
                case Wtf.autoNum.EntityBasedGSTForm03:
                    module = Wtf.autoNum.EntityBasedGSTForm03;
                    break;
                case Wtf.autoNum.EntityBasedGSTAuditFile:
                    module = Wtf.autoNum.EntityBasedGSTAuditFile;
                    break;
                case Wtf.autoNum.EntityBasedGSTTabReturnFile:
                    module = Wtf.autoNum.EntityBasedGSTTabReturnFile;
                    break;
                case Wtf.autoNum.EntityBasedSalesTaxReport:
                    module = Wtf.autoNum.EntityBasedSalesTaxReport;
                    break;
                case Wtf.autoNum.EntityBasedPurchaseTaxReport:
                    module = Wtf.autoNum.EntityBasedPurchaseTaxReport;
                    break;
                case Wtf.autoNum.TradingAndProfitLoss:
                    module = Wtf.autoNum.TradingAndProfitLoss;
                    break;
                case Wtf.autoNum.BalanceSheetReportId:
                    module = Wtf.autoNum.BalanceSheetReportId;
                    break;
                case Wtf.autoNum.CashFlowStatement:
                    module = Wtf.autoNum.CashFlowStatement;
                    break;
                case Wtf.autoNum.TrialBalance:
                    module = Wtf.autoNum.TrialBalance;
                    break;
                case Wtf.autoNum.AssetGroupReport:
                    module = Wtf.autoNum.AssetGroupReport;
                    break;
                case Wtf.autoNum.AssetDepreciationReport:
                    module = Wtf.autoNum.AssetDepreciationReport;
                    break;
                case Wtf.autoNum.AssetSummeryReport:
                    module = Wtf.autoNum.AssetSummeryReport;
                    break;
                case Wtf.autoNum.FixedAssetReport:
                    module = Wtf.autoNum.FixedAssetReport;
                    break;
                case Wtf.autoNum.DisposedAssetReport:
                    module = Wtf.autoNum.DisposedAssetReport;
                    break;
               case Wtf.autoNum.GroupDetailReport:
                    module = Wtf.autoNum.GroupDetailReport;
                    break;
               case Wtf.Acc_AgedReceivables_Summary_ModuleId:
                    module = Wtf.Acc_AgedReceivables_Summary_ModuleId;
                    break;
               case Wtf.Acc_AgedPayables_Summary_ModuleId:
                    module = Wtf.Acc_AgedPayables_Summary_ModuleId;
                    break;
               case Wtf.autoNum.AgedReceivableDetailReport:
                    module = Wtf.autoNum.AgedReceivableDetailReport;
                    break;                    
               case Wtf.Acc_AgedPayables_ReportView_ModuleId:
                    module = Wtf.Acc_AgedPayables_ReportView_ModuleId;
                    break; 
               case Wtf.autoNum.DefaultBalanceSheetReportId:
                    module = Wtf.autoNum.DefaultBalanceSheetReportId;
                    break; 
               case Wtf.autoNum.BalanceSheetPeriodView:
                    module = Wtf.autoNum.BalanceSheetPeriodView;
                    break; 
               case Wtf.TrialBalance_Moduleid:
                    module = Wtf.TrialBalance_Moduleid;
                    break; 
                default:
                    module = this.moduleid;
                    break;
        }
        if ( this.searchStore.getCount() > 0 ){
            this.saveSearchName.setValue( WtfGlobal.HTMLStripper(this.saveSearchName.getValue()));
            if(this.saveSearchName.getValue()!=""){
                var json = this.getJsonofStore();
                var saveSearchName = this.saveSearchName.getValue();
                var appendCase = this.appendCaseCombo.getValue();
                Wtf.Ajax.requestEx({
                    url:'AdvanceSearch/saveSearchQuery.do',
                    params:{
                        searchstate:json,
                        module:module,
                        searchname:saveSearchName,
                        filterAppend:appendCase,
                        templateid:this.templateid,
                        isCustomLayout:this.isCustomLayout,
                        templatetitle:this.templatetitle
                    }
                },
                this,
                function(res)
                {
                    if(res.msg!=undefined){
                        Wtf.MessageBox.show({
                            title:WtfGlobal.getLocaleText("acc.msgbox.CONFIRMTITLE"),
                            msg:res.msg,
                            icon:Wtf.MessageBox.QUESTION,
                            buttons:Wtf.MessageBox.YESNO,
                            scope:this,
                            fn:function(button){
                                if(button=='yes')
                                {
                                    this.SaveRememberSearch(json,module,saveSearchName, appendCase);
                                }
                            }
                        });
                    } else {
                        this.saveSearchName.setValue("");
                        var msg = WtfGlobal.getLocaleText("acc.RememberSearch.msg");
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.advancesearch.remembersearch"), msg], 4);
                    }

                },
                function(res)
                {
                    WtfComMsgBox(104,1);
                }
                )
            }else{
                 WtfComMsgBox(105,2);
            }
        }else{
            WtfComMsgBox(106,2);
        }
    },
    SaveRememberSearch:function(json,module,searchname, appendCase){

            Wtf.Ajax.requestEx({
                url:'AdvanceSearch/saveSearchQuery.do',
                params:{
                    searchstate:json,
                    module:module,
                    searchname:searchname,
                    confirmationFlag:true,
                    filterAppend:appendCase,
                    templateid:this.templateid,
                    isCustomLayout:this.isCustomLayout,
                    templatetitle:this.templatetitle
                }
            },
            this,
            function(res)
            {
                 this.saveSearchName.setValue("");
                 var msg = WtfGlobal.getLocaleText("acc.RememberSearch.msg");
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.advancesearch.remembersearch"), msg], 4);
            },
            function(res)
            {
                 WtfComMsgBox(104,1);
            }
            )


    },
    doSearch:function(formatdate,isExportButtonClicked){
        var filterJson={};
        if ( this.searchStore.getCount() > 0 ){
            filterJson = this.getStoreSearchJson(formatdate,this.dimBasedSkipRequestParams);
            if (isExportButtonClicked === true) {
                 this.fireEvent("filterStore", filterJson, this.appendCaseCombo.getValue(), isExportButtonClicked);
            }else{
                this.fireEvent("filterStore", filterJson, this.appendCaseCombo.getValue());

            }

        }else{
//             WtfComMsgBox(106,2);
            this.fireEvent("filterStore","","",isExportButtonClicked);
        }
    }, 
    getStoreSearchJson : function(formatdate,dimBasedSkipRequestParams){
        var filterJson=[];
        var i=0;

        this.searchStore.each(function(filterRecord){
            ///  for combo case also searchText is combodataid
            var searchText=filterRecord.data.searchText+"";
            var value="";
            var xType = "";
            value=filterRecord.data.searchText+"";
            var xtype=filterRecord.data.xtype;
            if(xtype==undefined)
                xtype = 'combo';
            if (xtype == 'datefield' || xtype =='Date' ){
                    if(formatdate && filterRecord.data.searchText && filterRecord.data.searchText.format)
                        searchText=WtfGlobal.convertToOnlyDate(filterRecord.data.searchText);
            }

            searchText = WtfGlobal.replaceAll(searchText, "\\\\" , "\\\\");
            if(this.combovalArr[i])
                this.combovalArr[i] = WtfGlobal.replaceAll(this.combovalArr[i], "\\\\" , "\\\\");
            value =  WtfGlobal.replaceAll(value, "\\\\" , "\\\\");
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
            var json = {};
                if (dimBasedSkipRequestParams) {
                    json = {
                        column: filterRecord.data.dbname,
//                        searchText: searchText,
                        iscustomcolumn: filterRecord.data.iscustomcolumn,
                        columnheader: encodeURIComponent(filterRecord.data.column),
                        combosearch: encodeURIComponent(filterRecord.data.combosearch),
                        iscustomcolumndata: filterRecord.data.iscustomcolumndata,
                        fieldtype: filterRecord.data.fieldtype,
                        isfrmpmproduct: filterRecord.data.isfrmpmproduct,
                        searchText: searchText,
                        isinterval: filterRecord.data.isinterval,                        
                        interval: filterRecord.data.interval,
                        isRangeSearchField: filterRecord.data.isRangeSearchField,
                        isbefore: filterRecord.data.isbefore,
                        xtype: xtype,
                        xfield: filterRecord.data.xfield,
                        isForProductMasterSearch:filterRecord.data.isForProductMasterSearch
//                        search: value
                    }
                } else {
                    json = {
                        column: filterRecord.data.dbname,
                        refdbname: filterRecord.data.refdbname,
                        xfield: filterRecord.data.xfield,
                        iscustomcolumn: filterRecord.data.iscustomcolumn,
                        iscustomcolumndata: filterRecord.data.iscustomcolumndata,
                        isfrmpmproduct: filterRecord.data.isfrmpmproduct,
                        fieldtype: filterRecord.data.fieldtype,
                        searchText: searchText,
                        columnheader: encodeURIComponent(filterRecord.data.column),
                        search: value,
                        xtype: xtype,
                        combosearch: encodeURIComponent(filterRecord.data.combosearch),
                        isinterval: filterRecord.data.isinterval,
                        interval: filterRecord.data.interval,
                        isbefore: filterRecord.data.isbefore,
                        isdefaultfield: filterRecord.data.isdefaultfield,
                        moduleid: filterRecord.data.moduleid,
                        isForProductMasterOnly: filterRecord.data.isForProductMasterOnly,
                        transactionSearch: this.panel.serachCombo.getValue(),
                        includingTax: this.panel.Tax.getValue(),
                        includingDiscount: this.panel.Discount.getValue(),
                        isRangeSearchField: filterRecord.data.isRangeSearchField,
                        iscustomfield: (filterRecord.data.iscustomfield != undefined && filterRecord.data.iscustomfield != null && filterRecord.data.iscustomfield != "") ? filterRecord.data.iscustomfield : false,
                        isMultiEntity: (filterRecord.data.isMultiEntity != undefined && filterRecord.data.isMultiEntity != null && filterRecord.data.isMultiEntity != "") ? filterRecord.data.isMultiEntity : false,
                        modulename: filterRecord.data.modulename,
                        isForProductMasterSearch:filterRecord.data.isForProductMasterSearch
                    }
            }
            filterJson.push(json);
            i++;
        },this);

        var filterObj= {
            root:filterJson
        }
if(filterJson.length > 0){
            return  Wtf.encode(filterObj);
 }
  return "";
    },
    getSearchJSON:function(formatdate){
        if ( this.searchStore.getCount() > 0 ){
        var filterJson="";
        var i=0;

        this.searchStore.each(function(filterRecord){
            ///  for combo case also searchText is combodataid
            var searchText=filterRecord.data.searchText+"";
            var value="";
            var xType = "";
            value=filterRecord.data.searchText+"";
            var xtype=filterRecord.data.xtype;
            if(xtype==undefined)
                xtype = 'combo';
            if (xtype == 'datefield' || xtype =='Date' ){
                    if(formatdate && filterRecord.data.searchText && filterRecord.data.searchText.format)
                        searchText=WtfGlobal.convertToOnlyDate(filterRecord.data.searchText);
            }

            searchText = WtfGlobal.replaceAll(searchText, "\\\\" , "\\\\");
            if(this.combovalArr[i])
                this.combovalArr[i] = WtfGlobal.replaceAll(this.combovalArr[i], "\\\\" , "\\\\");
            value =  WtfGlobal.replaceAll(value, "\\\\" , "\\\\");
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
            filterJson={
                column:filterRecord.data.dbname,
                refdbname:filterRecord.data.refdbname,
                xfield:filterRecord.data.xfield,
                iscustomcolumn:filterRecord.data.iscustomcolumn,
                iscustomcolumndata:filterRecord.data.iscustomcolumndata,
                isfrmpmproduct:filterRecord.data.isfrmpmproduct,
                fieldtype:filterRecord.data.fieldtype,
                searchText:searchText,
                columnheader:encodeURIComponent(filterRecord.data.column),
                search:value,
                xtype:xtype,
//                combosearch:filterRecord.data.combosearch,
                combosearch:encodeURIComponent(filterRecord.data.combosearch),
                isinterval:filterRecord.data.isinterval,
                interval:filterRecord.data.interval,
                isbefore:filterRecord.data.isbefore,
                isdefaultfield:filterRecord.data.isdefaultfield,
                moduleid:filterRecord.data.moduleid
            };
            i++;
        },this);
            return(filterJson);
        }else{
             WtfComMsgBox(106,2);
            return("");
        }
    },
    cancelSearch:function(){
        this.columnCombo.setValue("");
        var searchXtype = this.searchText.getXType();
        if(searchXtype=='combo')
            this.columnCombo.fireEvent("select",undefined,'');
        (this.toDate != undefined)?this.toDate.setValue(""):null;
        (this.fromDate != undefined)?this.fromDate.setValue(""):null;
        //(this.intervalCombo != undefined)?this.intervalCombo.setValue(""):null;
        this.searchStore.removeAll();
        this.combovalArr=[];
        this.xtypeArr=[];
        this.fireEvent("clearStoreFilter");
    },

    deleteFilter:function(gd, ri, ci, e) {
        var event = e;
        if(event.target.className == "pwnd delete-gridrow") {
            this.searchStore.remove(this.searchStore.getAt(ri));
            if(this.searchStore.getCount()==0) {
//                this.search.disable();
                this.saveSearch.disable();
                this.search.setTooltip(WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip"));
            }
            this.combovalArr.splice(ri,1);
            this.xtypeArr.splice(ri,1);
            if(!this.dimensionBasedComparisionReport){
                this.doSearch();
            }
        }

    },

	displayField:function(combo,record){
        var isRangeSearchField = false;
        if(record == '') {
            var recXtype = "textfield";
        } else {
            isRangeSearchField = record.get('isRangeSearchField');
            recXtype=record.get('xtype');
        }
        if (recXtype == "None"){
            record.set('xtype','textfield');
        }
        if(this.reportid!=undefined && this.reportid!='' && this.reportid==Wtf.autoNum.GeneralLedger && !this.isSameFieldFlag){
            this.columnCombo.reset();
        }
        if (this.text){
            this.text.destroy();
        }
        if (this.textTo){
            this.textTo.destroy();
        }
        this.saveSearchName.destroy();
        this.saveSearch.destroy();
        this.appendCase.destroy();
        this.appendCaseCombo.destroy();
        
        document.querySelectorAll(".widthforCmb1")[0].style.display="none";
        if (this.intervalCombo) {
            this.separator.destroy();
            this.intervalCombo.destroy();
        }
        
        this.searchText.destroy();
        if(this.searchTextTo)
            this.searchTextTo.destroy();
        this.add.destroy();
        this.search.destroy();
        this.cancel.destroy();
        this.doLayout();
        var iscustomcolumn,fieldtype,refdbname,dbname,iscustomcolumndata,isfrmpmproduct,isdefaultfield=false;
        var moduleid='';
        var modulename='';
        var isForProductMasterOnly="";
        var isForProductMasterSearch="";
        if(record!=''){
             iscustomcolumn=record.get('iscustomcolumn');
             iscustomcolumndata=record.get('iscustomcolumndata');
             /**
              * Address fields are not custom fields so making iscustomcolumn flag false
              */
             if(record.get('modulename')=="Address_Fields"){
                iscustomcolumn=false;
             }
             isfrmpmproduct=record.get('moduleid')== Wtf.Acc_Product_Master_ModuleId;
             moduleid=record.get('moduleid');
             modulename=record.get('modulename');
             this.modulename = record.get('modulename');
             if(this.moduleid == Wtf.Acc_Ledger_ModuleId || this.moduleid == 101){
                 isfrmpmproduct=record.get('isfrmpmproduct');
             }
             fieldtype=record.get('fieldtype');
             refdbname = record.get('refdbname');
             dbname=record.get('dbname');
             isdefaultfield=record.get('isdefaultfield');
             isForProductMasterOnly=record.get('isForProductMasterOnly');
             isForProductMasterSearch=record.get('isForProductMasterSearch');
             isRangeSearchField=record.get('isRangeSearchField');
        }

        iscustomcolumn = iscustomcolumn?iscustomcolumn:"";
        if (recXtype == "textfield" || recXtype == 'Text' || recXtype =='textarea' || recXtype == "richtextarea"){
            this.searchText = new Wtf.form.TextField({
                anchor: '95%',
                maxLength: 100,
                width:125,
                fromproduct:isfrmpmproduct,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield,
                moduleid:moduleid,
                modulename:modulename,
                isRangeSearchField:isRangeSearchField,
                isForProductMasterOnly:isForProductMasterOnly,
                isForProductMasterSearch:isForProductMasterSearch
            });
            if (isRangeSearchField) {
                this.searchTextTo = new Wtf.form.TextField({
                    anchor: '95%',
                    maxLength: 100,
                    width: 125,
                    fromproduct: isfrmpmproduct,
                    iscustomcolumn: iscustomcolumn,
                    iscustomcolumndata: iscustomcolumndata,
                    fieldtype: fieldtype,
                    dbname: dbname,
                    refdbname: refdbname,
                    isdefaultfield: isdefaultfield,
                    moduleid: moduleid,
                    modulename: modulename,
                    isForProductMasterOnly: isForProductMasterOnly
                });
            }
        }
        
        if (recXtype == "numberfield" || recXtype == 'Number(Integer)' || recXtype == 'Number(Float)'){
            this.searchText = new Wtf.form.NumberField({
                anchor: '95%',
                maxLength: 100,
                width:125,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield,
                moduleid:moduleid,
                modulename:modulename,
                isForProductMasterOnly:isForProductMasterOnly,
                isForProductMasterSearch:isForProductMasterSearch
            });
            this.searchTextTo = new Wtf.form.NumberField({
                anchor: '95%',
                maxLength: 100,
                width:125,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield,
                moduleid:moduleid,
                modulename:modulename,
                isForProductMasterOnly:isForProductMasterOnly,
                isForProductMasterSearch:isForProductMasterSearch
            });
        }



        if (recXtype == "combo" || recXtype == "Combobox" || recXtype == "select" || recXtype == "fieldset"){
            this.agentRec = new Wtf.data.Record.create([
                {
                    name: 'id'
                },
                {
                    name: 'name'
                }]
            );
            this.agentStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.agentRec),
                url: "ACCMaster/getMasterItems.do",
                baseParams: {
                    mode: 112,
                    groupid: 20
                }
            });
            this.agentStore.load();
            
            this.salesPersonRec = new Wtf.data.Record.create([
                {
                    name: 'id'
                },
                {
                    name: 'name'
                }]
            );
            this.salesPersonStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.salesPersonRec),
                url: "ACCMaster/getMasterItems.do",
                baseParams: {
                    mode: 112,
                    groupid: 15
                }
            });
            this.salesPersonStore.load();

            // Store "Wtf.MPPaidToStore" made local store as "this.MPPaidToStore" to fix ERP-18515
            this.MPPaidToRec = new Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'},
                {name: 'isIbgActivItematedForPaidTo', type: 'boolean'}
            ]);
            this.MPPaidToStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.MPPaidToRec),
                url: "ACCMaster/getMasterItems.do",
                baseParams: {
                    mode: 112,
                    groupid: 17
                }
            });
            
            this.MPPaidToStore.load();
            
            
            
        // If Product Brand is selected as a Search Field.============================
            this.ProductBrandRec = new Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name'}                
            ]);
            this.ProductBrandStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.ProductBrandRec),
                url: "ACCMaster/getMasterItems.do",
                baseParams: {
                    mode: 112,
                    groupid: 53
                }
            });
//            this.ProductBrandStore.load();
        // If Item Reusability is selected as a Search Field.   =====================                         
            this.reusabilityStore = new Wtf.data.SimpleStore({
                fields : ['id', 'name'],
                data : [
                        ['0', 'Reusable'],
                        ['1', 'Consumable']
                       ]
            });
            
        // If License Type is selected as a Search Field.   =======================    
            this.licenseTypeStore= new Wtf.data.SimpleStore({
                fields: ['id', 'name'],
                data : [['0','None'],['1','Local'],['2','Overseas']]
            });
            
        // If Valuation Method is selected as a Search Field.       
            this.valuationMethodStore= new Wtf.data.SimpleStore({
                fields: ['id', 'name'],
                data : [['0','LIFO'],['1','FIFO'],['2','Moving Average']]
            });
         
        // If Unit Of Measure is selected as a Search Field.  =====================         
            this.uomRec = Wtf.data.Record.create([
                {name: 'id', mapping:'uomid'},
                {name: 'name', mapping:'uomname'},              
            ]);
            this.uomStore = new Wtf.data.Store({
                
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.uomRec),
                url: "ACCUoM/getUnitOfMeasure.do",
                baseParams: {
                    mode: 31,
                    common: '1'
                }
                
            });
//            this.uomStore.load();
            
        // If Default Warehouse is selected as a Search Field.  ====================     
            this.wareHouseRec = new Wtf.data.Record.create([
                {
                    name:'id', mapping: 'store_id'
                },
                {
                    name:'name', mapping: 'fullname'
                }
            ]);
            this.wareHouseStore = new Wtf.data.Store({                
                url: 'INVStore/getStoreList.do',
                baseParams: {
                    storeTypes: "0,2",
                    isActive: true
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.wareHouseRec)
            });
//            this.wareHouseStore.load();        
            
            var searchField=record.get('header')
            
            if (searchField == "Product Brand") {  
                this.ProductBrandStore.load();
            }else if (searchField == "Default Warehouse") {  
                this.wareHouseStore.load();  
            }else if (searchField == "Stock UOM") {  
                this.uomStore.load();  
            }
            
            // Store "Wtf.RPReceivedFromStore" made local store as "this.RPReceivedFromStore" to fix ERP-18560
            this.RPReceivedFromRec = new Wtf.data.Record.create([
                {
                    name: 'id'
                },
                {
                    name: 'name'
                }]
                    );
            this.RPReceivedFromStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, Wtf.MPPaidToRec),
                url: "ACCMaster/getMasterItems.do",
                baseParams: {
                    mode: 112,
                    groupid: 18
                }
            });  
            
            this.RPReceivedFromStore.load();
            
            var editor = record.data.sheetEditor;
                 var comboReader = new Wtf.data.Record.create([
                {
                    name: 'id',
                    type: 'string'
                },
                {
                    name: 'name',
                    type: 'string'
                }, {name: 'level', type: 'int'},
            {name: 'leaf', type: 'boolean'},
            {name: 'parentid'},
            {name: 'parentname'},
            {name: 'companyid'}   
                ]);
                var comboboxname=record.get('header')
                
                var url = "ACCAccountCMN/getCustomCombodata.do";
                baseParams = {
                    mode: 2,
                    flag: 1,
                    fieldid: record.data.fieldid
                }
                
            if (isdefaultfield) {
                var Params;
                if (comboboxname == "Shipping Route") {
                    chkShippingRouteload();
                    this.comboStore = Wtf.ShippingRouteStore;
                } else if (comboboxname == "Customer" || comboboxname == "Customer Name") {
                    chkcustaccload();
                    this.comboStore = Wtf.customerAccStore;
                } else if (comboboxname == "Vendor") {
                    chkvenaccload();
                    this.comboStore = Wtf.vendorAccStore;
                } else if (comboboxname == "Currency") {
                    chkcurrencyload();
                    this.comboStore = Wtf.currencyStore;
                } else if (comboboxname == "Sales Person") {
                    chkSalesPersonload();
                    this.comboStore = this.salesPersonStore;
                } else if (comboboxname == "Agent") {
                    chkAgentload();
                    this.comboStore = this.agentStore;
                } else if (comboboxname == "Tax Name") {
                    Params = {params: {moduleid: this.moduleid}};
                    chktaxload(Params, this.moduleid);
                    this.comboStore = Wtf.taxStore;
                } else if (comboboxname == "Created By") {
                    chkUsersload();
                    this.comboStore = Wtf.userds;
                } else if (comboboxname == "Last Edited By") {
                    chkUsersload();
                    this.comboStore = Wtf.userds;
                } else if (comboboxname == "Payment Method") {
                    chkPaymentMethodload();
                    this.comboStore = Wtf.pmtStore;
                } else if (comboboxname == "Paid To") {
                    chkPaidToload();
                    this.comboStore = this.MPPaidToStore;
                } else if (comboboxname == "Received From") {
                    chkReceivedFromload();
                    this.comboStore = this.RPReceivedFromStore;
                } else if (comboboxname == "Product Brand") {                   
                    this.comboStore = this.ProductBrandStore;
                } else if (comboboxname == "Item Reusability") {                    
                    this.comboStore = this.reusabilityStore;
                } else if (comboboxname == "License Type") {                    
                    this.comboStore = this.licenseTypeStore;
                } else if (comboboxname == "Valuation Method") {                 
                    this.comboStore = this.valuationMethodStore;
                } else if (comboboxname == "Default Warehouse") {             //Stock UOM   //Default Warehouse      
                    this.comboStore = this.wareHouseStore;
                } else if (comboboxname == "Stock UOM") {             //Stock UOM   //Default Warehouse   //Default Location   
                    this.comboStore = this.uomStore;
                } else if (comboboxname == "SO No.") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    chkSONoload(Params, this.moduleid);
                    this.comboStore = Wtf.SONoStore;
                } else if (comboboxname == "DO No.") {
                    Params = {params: {requestModuleid: this.moduleid, linkFlag: true}};
                    chkDONoload(Params, this.moduleid);
                    this.comboStore = Wtf.DONoStore;
                } else if (comboboxname == "CQ No.") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    chkCQNoload(Params, this.moduleid);
                    this.comboStore = Wtf.CQNoStore;
                } else if (comboboxname == "SI No.") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    chkSINoload(Params, this.moduleid);
                    this.comboStore = Wtf.SINoStore;
                } else if (comboboxname == "VQ No.") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    chkVQNoload(Params, this.moduleid);
                    this.comboStore = Wtf.VQNoStore;
                } else if (comboboxname == "PO No.") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    chkPONoload(Params, this.moduleid);
                    this.comboStore = Wtf.PONoStore;
                } else if (comboboxname == "GR No.") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    chkGRNoload(Params, this.moduleid);
                    this.comboStore = Wtf.GRNoStore;
                } else if (comboboxname == "PI No.") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    chkPINoload(Params, this.moduleid);
                    this.comboStore = Wtf.PINoStore;
                } else if (comboboxname == "Store") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    chkStoresload(Params, this.moduleid);
                    this.comboStore = Wtf.Stores;
                } else if (comboboxname == "PR No.") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    chkPRNoload(Params, this.moduleid);
                    this.comboStore = Wtf.PRNoStore;
                } else if (comboboxname == "Debit Note") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    chkDNload(Params, this.moduleid);
                    this.comboStore = Wtf.DNStore;
                } else if (comboboxname == "Credit Note") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    chkCNload(Params, this.moduleid);
                    this.comboStore = Wtf.CNStore;
                } else if (comboboxname == "RFQ No.") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    chkRFQNoload(Params, this.moduleid);
                    this.comboStore = Wtf.RFQNoStore;
                } else if (comboboxname == "Account" || comboboxname == "Sales Account" || comboboxname == "Purchase Account"
                        || comboboxname == "Sales Return Account" || comboboxname == "Purchase Return Account" || comboboxname == "Cost of Goods Sold Account"
                        || comboboxname == "Stock Adjustment Account" || comboboxname == "Inventory Account") {
                    chkAccountload();
                    this.comboStore = Wtf.accountStore;
                } else if (comboboxname == "Debit Term" || comboboxname == "Credit Term") {
                    chktermload();
                    this.comboStore = Wtf.termds;
                } else if (comboboxname == "Cost Center") {
                    chkCostCenterload();
                    this.comboStore = Wtf.CostCenterStore;
                } else if (comboboxname == "Group") {
                    chkaccgroupload();
                    this.comboStore = Wtf.accGrpoupStore;
                } else if (comboboxname == "Product Type") {
                    chkProductTypeload();
                    this.comboStore = Wtf.productTypeStore;
                } else if (comboboxname == "Invoice No") {
                    Params = {params: {requestModuleid: this.moduleid}};
                    if (this.moduleid == Wtf.Acc_Make_Payment_ModuleId) {             //Make Payment
                        chkPINoload(Params, this.moduleid);
                        this.comboStore = Wtf.PINoStore;
                    } else if (this.moduleid == Wtf.Acc_Receive_Payment_ModuleId) {   //Receive Payment
                        chkSINoload(Params, this.moduleid);
                        this.comboStore = Wtf.SINoStore;
                    }
                }
                else if (comboboxname == "Department") {
                    chklabourdeptload();
                    this.comboStore = Wtf.departmentStore;
                }
                else if (comboboxname == "Work Center" || comboboxname == "Work Centre") {
                    chkworkCentreload();
                    this.comboStore = Wtf.workCentreStore;
                }
                else if (comboboxname == "Key Skills") {
                    chkkeySkillload();
                    this.comboStore = Wtf.keySkillStore;
                }
                else if (comboboxname == "Process") {
                    chkProcessload();
                    this.comboStore = Wtf.processStore;
                }
                else if (comboboxname == "Machine Vendor") {
                    chkvendorload();
                    this.comboStore = Wtf.vendorAccRemoteStore;
                }
                else if (comboboxname == "Purchase Account") {
                    chkPurchaseAccload();
                    this.comboStore = Wtf.purchaseAccStore;
                }
                else if (comboboxname == "Work Centre Type") {
                    chkwcTypeload();
                    this.comboStore = Wtf.workCenterTypeStore;
                }
                else if (comboboxname == "Product") {
                    chkwcproductload();
                    this.comboStore = Wtf.wcproductStore;
                }
                else if (comboboxname == "Material" || comboboxname == "BOM" || comboboxname == "BOM Code") {
                    chkMaterialload();
                    this.comboStore = Wtf.materialStore;
                }
                else if (comboboxname == "Labour" || comboboxname == "Labour ID") {
                    chkLabourload();
                    this.comboStore = Wtf.labourStore;
                }
                else if (comboboxname == "Machine" || comboboxname == "Machine ID") {
                    chkMachineload();
                    this.comboStore = Wtf.machineStore;
                }
                else if (comboboxname == "Work Centre Location") {
                    chkwcLocationload();
                    this.comboStore = Wtf.workCenterLocationStore;
                }
                else if (comboboxname == "Warehouse") {
                    chkWarehouseload();
                    this.comboStore = Wtf.inventoryStore;
                }
                else if (comboboxname == "Work Type") {
                    chkworkTypeload();
                    this.comboStore = Wtf.workTypeStore;
                }
                else if (comboboxname == "Work Centre Manager") {
                    chkwcManagerload();
                    this.comboStore = Wtf.workCenterManagerStore;
                }
                else if (comboboxname == "Cost Centre") {
                    chkCostCenterload();
                    this.comboStore = Wtf.CostCenterStore;
                }
                else if (comboboxname == "Routing Template") {
                    chkroutingload();
                    this.comboStore = Wtf.routingStore;
                }
                else if (comboboxname == "Work Order Status") {
                    chkWOStatusload();
                    this.comboStore = Wtf.WOStatusStore;
                }
                else if (comboboxname == "Work Order Type") {
                    chkWOTypeload();
                    this.comboStore = Wtf.workOrderTypeStore;
                }
                else if (comboboxname == "Job Work Location") {
                    chklocationload();
                    this.comboStore = Wtf.locationStore;
                }
                else if (comboboxname == "Work Order") {
                    chkWorkOrderload();
                    this.comboStore = Wtf.workOrderStore;
                }
                else if (comboboxname == "Seller Type") {
                    chkSellerTypeload();
                    this.comboStore = Wtf.sellerTypeStore;
                }
                else if (comboboxname == "Contract Status") {
                    chkContractStatusload();
                    this.comboStore = Wtf.contractStatusStore;
                }
                else if (comboboxname == "Parent Contract ID") {
                    chkParentContractload();
                    this.comboStore = Wtf.parentContractIdStore;
                } else if (comboboxname == "Status") {
                    chkStatusload();
                    this.comboStore = Wtf.dostatusStore;
                } else if (comboboxname == "Transaction Type") {
                    this.comboStore = Wtf.transactionTypeStore;
                }
            } else {
                    this.comboStore = new Wtf.data.Store({
                       url: url,
                       id:'isCustomStore',
                       baseParams: baseParams,
                       reader: new Wtf.data.KwlJsonReader({
                         root: 'data'
                       }, Wtf.ComboReader),
                      autoLoad:true
                });
              }
            this.displayField=combo.getValue();
            this.comboStore.on('load',function(store,b,c){
                var record = new Wtf.data.Record({
                 id: "-9999",
                 name: "Blank"
            });
            // Insert 'Blank' in combo box only in case of Global custom field. NA for Line level fields and Default fields.
            if (!(isdefaultfield || this.searchText.iscustomcolumndata)) {
                this.comboStore.insert(0, record);
            }
            },this);
            this.MSComboconfig = {
                valueField: 'id',
                // displayField: this.displayField,
                displayField: 'name',
                store: this.comboStore,
                typeAhead:true,
                forceSelection :true,
                anchor: '95%',
                multiSelect:true,
                hirarchical:true,
                mode: editor.mode ? (editor.storemanagerkey ? (Wtf.StoreMgr.containsKey(editor.storemanagerkey) ? "local":editor.mode):editor.mode) : "local",
                minChars : 2,
                triggerClass : (editor.searchStoreCombo && !editor.loadOnSelect) ? "dttriggerForTeamLead" : "",
                triggerAction: 'all',
                selectOnFocus: true,
                extraFields: this.showBOMWithProduct && (comboboxname == "Material" || comboboxname == "BOM" || comboboxname == "BOM Code")?['pid', 'productname']:[],
                emptyText: editor.searchStoreCombo && !editor.loadOnSelect ? WtfGlobal.getLocaleText("acc.advancesearch.searchanoption"):WtfGlobal.getLocaleText("acc.advancesearch.searchcombo.seloptmtytxt"),
                width:125,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield,
                moduleid:moduleid,
                modulename:modulename,
                isAdvanceSearchCombo:true,
                isForProductMasterOnly:isForProductMasterOnly,
                isForProductMasterSearch:isForProductMasterSearch,
                lazyInit: false
            }; 
            this.searchText = new Wtf.common.Select(Wtf.applyIf({
                id:'mulaccountcombo'+this.id,
                multiSelect:true,
                fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectAccounts") ,
                forceSelection:true,
                anchor: '95%',
                width:240
            },this.MSComboconfig));
            this.searchText.on('beforeselect', function(combo, record, index) {
                if (record.get('id') == "") {
                    this.searchText.clearValue();
                    return false;
                }
            }, this);
            if (recXtype == "combo" || recXtype == "Combobox"){
                this.searchText.on('focus', function (combo, record, index) {
                    combo.doQuery(combo.allQuery, true);
                }, this);
            }
        }

        if (recXtype == "checkbox") {
            var modStore = new Wtf.data.SimpleStore({
                fields: ['id', 'name'],
                data: [["true", "true"],
                    ["false", "false"]],
                autoLoad: true
            });
            
            this.Comboconfig={
                selectOnFocus: true,
                triggerAction: 'all',
                mode: 'local',
                store: modStore,
                useDefault: true,
                displayField: 'name',
                multiSelect:false,
                typeAhead: true,
                valueField: 'id',
                anchor: '95%',
                width: 125,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield,
                moduleid:moduleid,
                modulename:modulename
            }
            this.searchText = new Wtf.common.Select(Wtf.applyIf({
                id:'accountcombo'+this.id,
                multiSelect:false,
                fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectAccounts") ,
                forceSelection:true,
                anchor: '95%',
                width:125
            },this.Comboconfig));
            
            this.searchText.on('beforeselect', function (combo, record, index) {
                if (record.get('id') == "") {
                    this.searchText.clearValue();
                    return false;
                }
            }, this);
        }

        if (recXtype == "datefield" || recXtype == 'Date' ){
            this.searchText=new Wtf.form.DateField({
                width:100,
                format:"M d, Y ",
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield,
                moduleid:moduleid,
                modulename:modulename,
                isForProductMasterOnly:isForProductMasterOnly,
                isForProductMasterSearch:isForProductMasterSearch
            });
            this.searchTextTo=new Wtf.form.DateField({
                width:100,
                format:"M d, Y ",
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                moduleid:moduleid,
                modulename:modulename,
                isForProductMasterOnly:isForProductMasterOnly,
                isForProductMasterSearch:isForProductMasterSearch
            });

            this.searchText.on('focus', function(){
            	this.intervalCombo.setValue('');
            	this.isinterval = false;
            	this.interval = "";
                this.isbefore = "";
            }, this);

            this.searchTextTo.on('focus', function(){
            	this.intervalCombo.setValue('');
            	this.isinterval = false;
            	this.interval = "";
                this.isbefore = "";
            }, this);

            this.intervalStore = new Wtf.data.SimpleStore({
                fields:['id', 'isbefore', 'values', 'name'],
                data:[
//                      ['0', true, 1, WtfGlobal.getLocaleText("crm.editor.advanceSearch.before")+" 1 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.day")],
//                      ['1', true, 7, WtfGlobal.getLocaleText("crm.editor.advanceSearch.before")+" 7 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['2', true, 10, WtfGlobal.getLocaleText("crm.editor.advanceSearch.before")+" 10 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['3', true, 15, WtfGlobal.getLocaleText("crm.editor.advanceSearch.before")+" 15 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['4', true, 30, WtfGlobal.getLocaleText("crm.editor.advanceSearch.before")+" 30 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['5', true, 60, WtfGlobal.getLocaleText("crm.editor.advanceSearch.before")+" 60 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['6', true, 90, WtfGlobal.getLocaleText("crm.editor.advanceSearch.before")+" 90 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['7', true, 120, WtfGlobal.getLocaleText("crm.editor.advanceSearch.before")+" 120 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['8', true, 180, WtfGlobal.getLocaleText("crm.editor.advanceSearch.before")+" 180 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['9', true,365, WtfGlobal.getLocaleText("crm.editor.advanceSearch.before")+" 365 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['10', false, 1, WtfGlobal.getLocaleText("crm.editor.advanceSearch.after")+" 1 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.day")],
//                      ['11', false, 7, WtfGlobal.getLocaleText("crm.editor.advanceSearch.after")+" 7 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['12', false, 10, WtfGlobal.getLocaleText("crm.editor.advanceSearch.after")+" 10 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['13', false, 15, WtfGlobal.getLocaleText("crm.editor.advanceSearch.after")+" 15 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['14', false, 30, WtfGlobal.getLocaleText("crm.editor.advanceSearch.after")+" 30 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['15', false, 60, WtfGlobal.getLocaleText("crm.editor.advanceSearch.after")+" 60 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['16', false, 90, WtfGlobal.getLocaleText("crm.editor.advanceSearch.after")+" 90 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['17', false, 120, WtfGlobal.getLocaleText("crm.editor.advanceSearch.after")+" 120 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['18', false, 180, WtfGlobal.getLocaleText("crm.editor.advanceSearch.after")+" 180 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")],
//                      ['19', false,365, WtfGlobal.getLocaleText("crm.editor.advanceSearch.after")+" 365 "+WtfGlobal.getLocaleText("crm.editor.advanceSearch.days")]
                   ]
        	});

            this.intervalCombo = new Wtf.form.ComboBox({
                name:"id",
                store:this.intervalStore,
                displayField:"name",
                valueField:'id',
                hiddenName:'id',
                mode:'local',
                typeAhead:true,
                triggerAction:'all',
                width:120,
                forceSelection:true,
                emptyText:WtfGlobal.getLocaleText("crm.editor.advanceSearch.select.interval")
            });

            this.intervalCombo.on('select', function(combo, record, index){

            	
                    var today = new Date();

                    if(record.data.isbefore){
                        this.searchText.setValue(today.add(Date.DAY, -record.data.values));
                        this.searchTextTo.setValue(today);
                    }else{
                        this.searchText.setValue(today);
                        this.searchTextTo.setValue(today.add(Date.DAY, record.data.values));
                    }

                    this.isinterval = true;
                    this.interval = record.data.values;
                    this.isbefore = record.data.isbefore;
            }, this);
            
        }
        if (recXtype == "timefield"  ){
            this.searchText=new Wtf.form.TimeField({
                width:125,
                value:WtfGlobal.setDefaultValueTimefield(),
                format:WtfGlobal.getLoginUserTimeFormat(),
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                moduleid:moduleid,
                modulename:modulename,
                isForProductMasterOnly:isForProductMasterOnly,
                isForProductMasterSearch:isForProductMasterSearch
            });
        }

        if (recXtype == "datefield" || recXtype == 'Date' || recXtype == "numberfield" || recXtype == 'Number(Integer)' || recXtype == 'Number(Float)' || record.get('isRangeSearchField')){
            if(recXtype == "datefield" || recXtype == 'Date'){
                this.text=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate")+" : ");
                this.textTo=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.todate")+" : ");
            }
            if(recXtype == "numberfield" || recXtype == 'Number(Integer)' || recXtype == 'Number(Float)' || record.get('isRangeSearchField')){
                this.text=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.from")+" : ");
                this.textTo=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.to")+" : ");
            }
            
            this.getTopToolbar().add(this.text);
            this.getTopToolbar().add(this.searchText);
            this.getTopToolbar().add(this.textTo);
            this.getTopToolbar().add(this.searchTextTo);
            if (recXtype == "datefield" || recXtype == 'Date'){
                this.separator = new Wtf.Toolbar.Separator();
                this.getTopToolbar().add(this.separator);
//                this.getTopToolbar().add(this.intervalCombo);
            }

        }else{
            this.text=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt")+" : ");
            this.getTopToolbar().add(this.text);
            this.getTopToolbar().add(this.searchText);
        }
        this.appendCase = new Wtf.Toolbar.TextItem(this.dimensionBasedComparisionReport || this.hideFilterConjunction ? "" : WtfGlobal.getLocaleText("acc.editor.advanceSearch.filter.conjunction"));//'Filter Conjunction');
        var modStore = new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data: [["AND", "AND"],
                ["OR", "OR"]
            ],
            autoLoad: true
        });
        this.appendCaseCombo = new Wtf.form.ComboBox({
            selectOnFocus: true,
            triggerAction: 'all',
            mode: 'local',
            store: modStore,
            useDefault: true,
            hidden: this.dimensionBasedComparisionReport || this.hideFilterConjunction,
            displayField: 'name',
            typeAhead: true,
            valueField: 'id',
            anchor: '100%',
            width: 60,
            value: this.appendCaseComboValue
        });
        this.getTopToolbar().add(this.appendCase);
        this.getTopToolbar().add(this.appendCaseCombo);
        this.getTopToolbar().addButton([this.add = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.targetlists.creatarr.addBTN"),//'Add',
            tooltip: {text: WtfGlobal.getLocaleText("acc.advancesearch.addtermtosearchtip")},//'Add a term to search.'},
            handler: this.addSearchFilter,
            scope: this,
            iconCls : 'pwnd addfilter'
        }),
        this.search = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.audittrail.searchBTN"),//'Search',
            tooltip: {text: WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")},//'Add terms to search.'},
            handler: this.doSearch,
            hidden: this.hideSearchBttn,
            disabled:true,
            scope:this,
            iconCls : "advanceSearchButton"
        }),
        this.cancel = new Wtf.Toolbar.Button({
            text:  WtfGlobal.getLocaleText("acc.CLOSEBUTTON"),//'Close',
            tooltip: {text: WtfGlobal.getLocaleText("acc.advancesearch.closebtn.ttip")},//'Clear search terms and close advanced search.'},
            handler: this.cancelSearch,
            hidden: this.hideCloseBttn,
            scope:this,
            iconCls:'pwnd clearfilter'
        })]);
        this.saveSearchName = new Wtf.ux.TextField({
            anchor: '95%',
            maxLength: 40,
            hidden: this.hideRememberSerch,
            width:100
        });
        this.saveSearch = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.advancesearch.remembersearch"),//'Remember Search',
                tooltip: {text: WtfGlobal.getLocaleText("acc.advancesearch.remembersearch.ttip")},//'Save search state.'},
                handler: this.RememberSearch,
                scope: this,
                hidden:this.hideRememberSerch,
                disabled:true,
                iconCls : 'pwnd add'
        });

        this.getTopToolbar().add(this.saveSearch);
        this.getTopToolbar().add(this.saveSearchName);

        if ((recXtype == "numberfield" || recXtype == "datefield")) {
            this.add.setTooltip(WtfGlobal.getLocaleText("acc.advancesearch.numberanddatesearch"));
        }
        this.appendCaseCombo.on('select',function(a,b,c){
            this.appendCaseComboValue = this.appendCaseCombo.getValue();
        },this); 
        
        
        this.add.getEl().dom.style.paddingLeft="4px";
        this.doLayout();
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
                    customcolumn: (this.reportid == Wtf.autoNum.salesPersonCommissionDimensionReport) ? 1 : 0,
                    moduleid: this.moduleid,
                    jeId: (this.record) ? this.record.data.journalentryid : "",
                    isAdvanceSearch:true,
                    iscustomdimension:this.dimensionBasedComparisionReport || this.dimensionOnly,
                    isActivated:1,
                    isCustomDetailReport:(this.moduleid==102 && this.isCustomDetailReport==true)?true:false,//send for custom details report
                    excludeModule: (this.moduleid==100 || this.moduleid==101 || this.moduleid==102) ? Wtf.Account_Statement_ModuleId : "",
                    splitOpeningBalance:Wtf.account.companyAccountPref.splitOpeningBalanceAmount,
                    customerCustomFieldFlag:this.customerCustomFieldFlag,
                    vendorCustomFieldFlag:this.vendorCustomFieldFlag,
                    reportid:this.reportid,
                    moduleidarray:this.moduleidarray,
                    isAvoidRedundent:this.isAvoidRedundent,
                    ignoreDefaultFields:this.ignoreDefaultFields,
                    isLinedetailReport: this.isLinedetailReport,
                    isMultiEntity:this.isMultiEntity,
                    globallevelfields:this.globallevelfields,
                    linelevelfields:this.linelevelfields,
                    isAddressFieldSearch:this.isAddressFieldSearch  //Flag used to enable advance search on address fields
                }
            }
            Wtf.Ajax.requestEx({
                url: "ACCAccountCMN/getFieldParams.do",
                params: params
            }, this,
                    function(responseObj) {
                        this.sdate = "0";
                        this.edate = "0";
                          // HELP
            //
            //column : tablename.columnname.
            //refdbname : table name in database
            //xfield : Name of column in database
            //iscustomcolumn : true for custom colummn and false for default column
            //fieldType : 1(textfield) , 2 (Numberfield) , 3 (datefield) , 4 (Combofield) ,5( Timefield)
            //searchText : search text ( id for combo ie value field )
            //columnheader : header of column in column model ( decoded)
            //search : same as searchText
            //xtype : xtype of field like textfield, numberfield ,combo etc
            //combosearch  : displayfield
            //pojoheadername  : pojoheader for custom column
            
            

                        if (responseObj.data != '' && responseObj.data != null) {
                            for (var i = 0; i < responseObj.data.length; i++) {
                                var tmpArray = [];
                                responseObj.data[i].fieldData
                                var header = headerCheck(WtfGlobal.HTMLStripper(responseObj.data[i].fieldlabel));
                                header = header.replace("*", "");
                                header = header.trim();
                                tmpArray.push(header);
                                responseObj.data[i].column_number
                                tmpArray.push(responseObj.data[i].column_number);
                                tmpArray.push(WtfGlobal.getXType(responseObj.data[i].fieldtype));
                                tmpArray.push(responseObj.data[i].column_number);
                                tmpArray.push(true);
                                tmpArray.push(responseObj.data[i].column_number);
                                tmpArray.push(responseObj.data[i].xfield);
                                tmpArray.push(responseObj.data[i].fieldtype);
                                tmpArray.push(responseObj.data[i].column_number);
                                tmpArray.push(responseObj.data[i].fieldid);
                                tmpArray.push(responseObj.data[i].iscustomcolumn);
                                tmpArray.push(responseObj.data[i].moduleid);
                                tmpArray.push(responseObj.data[i].modulename);
                                tmpArray.push(responseObj.data[i].isdefaultfield);
                                tmpArray.push(responseObj.data[i].isfrmpmproduct);
                                tmpArray.push(responseObj.data[i].isForProductMasterOnly);
                                tmpArray.push(responseObj.data[i].isRangeSearchField);
                                tmpArray.push(responseObj.data[i].iscustomfield);
                                tmpArray.push(responseObj.data[i].isMultiEntity);
                                tmpArray.push(responseObj.data[i].isForProductMasterSearch); // If check is true Advance Search will be applied on all modules even if field is activated only for product master.
                                var recXtype=WtfGlobal.getXType(responseObj.data[i].fieldtype);
                                if(this.checkRecXtype!=undefined && this.checkRecXtype==true && (recXtype == "combo" || recXtype == "Combobox" || recXtype == "select" )){
                                    mainArray.push(tmpArray)
                                }else if(this.checkRecXtype==undefined){
                                    mainArray.push(tmpArray)
                                }
                                
                            }
                            this.myData = mainArray;
                            if (this.advSearch)
                                this.combostore.loadData(this.myData);

                        }

                    },
                    function() {
                    }
            );
        }
    }

});




Wtf.advancedSearchComponentForCustomReport = function(config){
    Wtf.apply(this, config);

    this.events = {
        "customReportFilterStore": true,
        "customReportClearStoreFilter": true
    };

    this.combovalArr=[];
    this.xtypeArr=[];

    this.combostore = new Wtf.data.SimpleStore({
        fields: [
        {
            name: 'name'
        },
        {
            name: 'dbcolumnname'
        },
        {
            name: 'displayname'
        },
        {
            name: 'modulename'
        },
        {
            name: 'tablename'
        },{
            name: "defaultname"
        },
        {
            name:'type'
        },{
            name :'iscustomcolumn'
        },{
            name :'pojoname'
        },{
            name :'dataindex'
        },{
            name :'configid'
        },{
            name :'iscustomfield',
            type: 'boolean'
        }
        ]
    });

    this.columnCombo = new Wtf.form.ComboBox({
        store : this.combostore,
        editable: false,
        selectOnFocus:true,
        displayField:'displayname',
        valueField : 'dataindex',
        triggerAction: 'all',
        emptyText : WtfGlobal.getLocaleText("crm.responsealert.msg.12"),//'Select a Search Field to search',
        mode:'local'
    })

    this.columnCombo.on("select",this.displayField,this);


    this.cm=new Wtf.grid.ColumnModel([{
        header: WtfGlobal.getLocaleText("crm.customreport.header.column"),//"Column",
        dataIndex:'column'
    },{
        header: WtfGlobal.getLocaleText("crm.advancesearch.search1txt"),//"Search1 Text",
        dataIndex:'searchText',
        hidden:true

    },
    {
        header: WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt"),//"Search Text",
        dataIndex:'id'
    },{
        header:WtfGlobal.getLocaleText("acc.DELETEBUTTON"),// "Delete",
        dataIndex:'delField',
        renderer : function(val, cell, row, rowIndex, colIndex, ds) {
            return "<div class='pwnd delete-gridrow' > </div>";
        }
    }
    ]);

    this.searchRecord = Wtf.data.Record.create([{
        name: 'column'
    },{
        name: 'searchText'
    },{
        name: 'dbname'
    },
    {
        name: 'id'
    },{
        name: 'xtype'
    },{
        name: 'refdbname'
    },{
        name: 'iscustomcolumn'
    },{
        name:'xfield'
    },{
        name:'fieldtype'
    },{
        name:'dataindex'
    },{
        name :'configid'
    }]);

    this.GridJsonReader = new Wtf.data.JsonReader({
        root: "data",
        totalProperty: 'count'
    }, this.searchRecord);

    this.searchStore = new Wtf.data.Store({
        reader: this.GridJsonReader
    });

     this.on("cellclick", this.deleteFilter, this);

    Wtf.advancedSearchComponentForCustomReport.superclass.constructor.call(this, {

        region :'north',
        height:150,
        hidden:true,
        store: this.searchStore,
        cm:this.cm,
        stripeRows: true,
        autoScroll : true,
        border:false,
        clicksToEdit:1,
        viewConfig: {
            forceFit:true
        },

        tbar: [this.text1=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText("crm.advancesearch.searchfield")+": "),this.columnCombo,'-',this.text=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText("crm.mydocuments.quicksearch.mtytxt")+": "), this.searchText = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("crm.advancesearch.newmasterrec"),//'New Master Record',
            anchor: '95%',
            maxLength: 100,
            width:125
        }),this.add = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("crm.targetlists.creatarr.addBTN"),//
            tooltip: {text: WtfGlobal.getLocaleText("crm.advancesearch.addtermtosearchtip")},//'Add a term to search.'},
            handler: this.addSearchFilter,
            scope: this,
            iconCls : 'pwnd addfilter'
        }),
        this.search = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("crm.advancesearch.savefilterbtn"),//'Save Filter',
            tooltip: {text:  WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")},//'Click to save filter for this report.'},
            handler: this.doSearch,
            scope:this,
            disabled:true,
            iconCls : "advanceSearchButton"
        }),
        this.cancel = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.CLOSEBUTTON"),// 'Close',
            tooltip: {text:this.call_from_custom_report? WtfGlobal.getLocaleText("crm.advancesearch.clobtntip"):WtfGlobal.getLocaleText("crm.advancesearch.closebtn.ttip")},//'Clear search terms and close the search. ' :'Clear search terms and close advanced search. '},
            handler: this.confirmationToClose,
            scope:this,
            iconCls:'pwnd clearfilter'
        })]
    });

}

Wtf.extend(Wtf.advancedSearchComponentForCustomReport, Wtf.grid.EditorGridPanel, {
    addSearchFilter:function(){
        if(this.columnCombo.getRawValue().trim()==""){
              WtfComMsgBox(107,2);
            return;
        }
//        var column =this.columnCombo.getValue();
        var column =this.columnCombo.getRawValue();
        var searchText="";
        if(this.searchText.getXType()=="numberfield" || this.searchText.getXType()=="datefield"){
            searchText=this.searchText.getValue();
        } else {
            searchText=this.searchText.getValue().trim();
        }
        if(this.searchText.getXType()=="timefield") {
           searchText =  WtfGlobal.convertToGenericTime(Date.parseDate(searchText,WtfGlobal.getLoginUserTimeFormat()));
        }
        var do1=0;
        var values =this.searchText.getRawValue().split(",");
        var refDBname="";
        if (column != "" &&  searchText !=""){
            if(this.dimensionBasedComparisionReport && this.combovalArr.length>0){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.removethepreviousselectiontogeneratenecomparision")],2) ;
                 return;
            }/*else if(this.dimensionBasedComparisionReport && values.length>10){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.removethepreviousselectiontogeneratenecomparision")],2) ;
                 return;
            }*/
            this.searchText1=this.searchText.getRawValue();
            this.combovalArr.push(this.searchText1);
            if(this.searchText.getXType()=="datefield"){
                this.xtypeArr.push("datefield");
            } else{
                this.xtypeArr.push(this.searchText.store);
            }
            this.columnText="";
            this.dataInd="";
            this.configID="";

            if(searchText != "") {
                for(var i=0;i<this.combostore.getCount();i++) {
//                    if(this.combostore.getAt(i).get("name")== column) {
//                        this.columnText=this.combostore.getAt(i).get("name");
                    if(this.combostore.getAt(i).get("displayname")== column) {
                        this.columnText=this.combostore.getAt(i).get("displayname");
                        this.dataInd=this.combostore.getAt(i).get("dataindex");
                        this.configID=this.combostore.getAt(i).get("configid");
                        do1=1;
                    }
                }
                if(do1==1) {
                    this.search.enable();
                   // this.saveSearch.enable();
                    this.search.setTooltip(WtfGlobal.getLocaleText("crm.advancesearch.searchonmulterms"));//'Search on multiple terms');
                    if(this.searchText.fieldtype=="7"){
                    	if(!this.searchText.iscustomcolumn){
                    		if(this.searchText.refdbname=="crm_opportunity")
                    			refDBname="crm_oppurtunityProducts";
                    		else
                    			refDBname = this.searchText.refdbname+"Products";
                    	}
                    }else{
                    	refDBname=this.searchText.refdbname;
                    }
                    var searchRecord = new this.searchRecord({
                        column: this.columnText,
                        searchText: searchText,
                        dbname:column,
                        id:this.searchText1,
                        xtype:this.searchText.getXType(),
                        iscustomcolumn:this.searchText.iscustomcolumn,
                        xfield:this.searchText.dbname,
                        fieldtype:this.searchText.fieldtype,
                        refdbname:refDBname,
                        dataindex:this.dataInd,
                        configid:this.configID
                    });

                    var index=this.searchStore.find('column',this.columnText);
                    if (index == -1 ) {
                        this.searchStore.add(searchRecord);
                    } else {
                        this.searchStore.remove(this.searchStore.getAt(index ) );
                        this.searchStore.insert(index,searchRecord);
                    }
                }
            }
        } else {
            if(column == "") {
                  WtfComMsgBox(107,2);
                } else if(searchText =="") {
                     WtfComMsgBox(108,2);
                }
            }
        this.searchText.setValue("");
    },

    doSearch:function(formatdate){
        if ( this.searchStore.getCount() > 0 ){
        var filterJson='{"root":[';
        var i=0;

        this.searchStore.each(function(filterRecord){
            ///  for combo case also searchText is combodataid
            var searchText=filterRecord.data.searchText+"";
            var value="";
            var xType = "";
            value=filterRecord.data.searchText+"";
            var xtype=filterRecord.data.xtype;
            if (xtype == 'datefield' || xtype =='Date' ){
                    if(formatdate && filterRecord.data.searchText && filterRecord.data.searchText.format)
                        searchText=WtfGlobal.convertToOnlyDate(filterRecord.data.searchText);
            }
            searchText = searchText.replace(/"/g,"");
            if(this.combovalArr[i])
                this.combovalArr[i] = this.combovalArr[i].replace(/"/g,"");
            value =  value.replace(/"/g,"");
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

            // HELP
            //
            //column : tablename.columnname.
            //refdbname : table name in database
            //xfield : Name of column in database
            //iscustomcolumn : true for custom colummn and false for default column
            //fieldType : 1(textfield) , 2 (Numberfield) , 3 (datefield) , 4 (Combofield) ,5( Timefield)
            //searchText : search text ( id for combo ie value field )
            //columnheader : header of column in column model ( decoded)
            //search : same as searchText
            //xtype : xtype of field like textfield, numberfield ,combo etc
            //combosearch  : displayfield
            //pojoheadername  : pojoheader for custom column


            filterJson+='{ "column":"'+filterRecord.data.dataindex+'","refdbname":"'+filterRecord.data.refdbname+'","xfield":"'+filterRecord.data.xfield+'","iscustomcolumn":"'+filterRecord.data.iscustomcolumn+'","fieldtype":"'+filterRecord.data.fieldtype+'","searchText":"'+searchText+'","columnheader":"'+encodeURIComponent(filterRecord.data.column)+'","search":"'+value+'","xtype":"'+xtype+'","pojoname":"'+filterRecord.data.pojoname+'","combosearch":"'+this.combovalArr[i]+'"},';
            i++;
        },this);

        filterJson=filterJson.substring(0,filterJson.length-1);
        filterJson+="]}";
            this.fireEvent("customReportFilterStore",filterJson);

        }else{
             WtfComMsgBox(106,2);
            this.fireEvent("customReportFilterStore","");
        }
    },
    cancelSearch:function(){
        this.columnCombo.setValue("");
        var searchXtype = this.searchText.getXType();
        (this.toDate != undefined)?this.toDate.setValue(""):null;
        (this.fromDate != undefined)?this.fromDate.setValue(""):null;
        this.searchStore.removeAll();
        this.combovalArr=[];
        this.xtypeArr=[];
        this.fireEvent("customReportClearStoreFilter");
    },
    confirmationToClose:function(){
       if(this.searchStore.data.items.length>0){
             Wtf.MessageBox.show({
                title:WtfGlobal.getLocaleText("crm.msg.ALERTTITLE"),//"Alert",
                msg:WtfGlobal.getLocaleText("crm.advancesearch.filternotsavedmsg"),//"Filters applied, are not saved. Do you want to exit without saving it?",
                buttons:Wtf.MessageBox.YESNO,
                animEl:'mb9',
                fn:function(btn){
                    if(btn=="yes"){
                        this.cancelSearch();
                    }
                },
                scope:this,
                icon: Wtf.MessageBox.QUESTION
            });
       }else {
           this.cancelSearch();
       }
    },

    deleteFilter:function(gd, ri, ci, e) {
        var event = e;
        if(event.target.className == "pwnd delete-gridrow") {
            this.searchStore.remove(this.searchStore.getAt(ri));
            if(this.searchStore.getCount()==0) {
                this.search.disable();
                this.search.setTooltip(WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip"));
            }
            this.combovalArr.splice(ri,1);
            this.xtypeArr.splice(ri,1);
        }
    },

	displayField:function(combo,record){

        if(record == '')
            var recXtype = "textfield";
        else
            recXtype=record.get('type');
        if (recXtype == "None"){
            record.set('type','textfield');
        }

        if (this.text){
            this.text.destroy();
        }
        if(this.searchText)
            this.searchText.destroy();
        this.add.destroy();
        this.search.destroy();
        this.cancel.destroy();
        this.doLayout();
        var name,dbcolumnname,displayname,modulename,tablename,type,iscustomcolumn;
        if(record!=''){
             modulename=record.get('modulename');
             type=record.get('type');
             tablename = record.get('tablename');
             dbcolumnname=record.get('dbcolumnname');
             iscustomcolumn=record.get('iscustomcolumn');

        }
         var a = record!=''?record.get('type'):""
         var xtype ="";
         switch(a){
            case "1":
                xtype = "textfield";
                break;
            case "2":
                xtype = "numberfield";
                break;
            case "3":
                xtype = "Datefield";
                break;
            case "4":
                xtype = "Combo";
                break;
            case "5":
                xtype = "Timefield";
                break;
            case "6":
                xtype = "Checkbox";
                break;
            case "7":
                xtype = "Multiselect Combo";
                break;
            case "8":
                xtype = "Ref. Combo";
                break;
            case "12":
                xtype = "Field Set";
                break; 
            case "13":
                xtype = "textarea";
                break; 

        }
        recXtype = xtype;
        if (recXtype == "textfield" || recXtype == "textarea"){
            this.searchText = new Wtf.form.TextField({
                anchor: '95%',
                maxLength: 100,
                width:125,
                iscustomcolumn:iscustomcolumn,
                fieldtype:type,
                dbname:dbcolumnname,
                refdbname:tablename
            });
        }

        if (recXtype == "numberfield" ){
            this.searchText = new Wtf.form.NumberField({
                anchor: '95%',
                maxLength: 100,
                width:125,
                iscustomcolumn:iscustomcolumn,
                fieldtype:type,
                dbname:dbcolumnname,
                refdbname:tablename
            });
        }

        if (recXtype == "Combo" || recXtype == "Multiselect Combo" || recXtype == "Ref. Combo" ||  recXtype == "Field Set"){
                var editor = record.data.sheetEditor;
                var comboReader = new Wtf.data.Record.create([
                {
                    name: 'id',
                    type: 'string'
                },
                {
                    name: 'name',
                    type: 'string'
                }
                ]);
                var comboboxname=record.get('defaultname')

                if(!iscustomcolumn){ // for Default Column
                    if(modulename=="Campaign"){

                        if(comboboxname=='Campaign Type' || comboboxname=='Type'){
                            chkviewStoreTypeload()
                            this.comboStore = Wtf.viewStoreType;
                        } else if(comboboxname=='Campaign Status' || comboboxname=='Status'){
                            chkviewStoreStatusload()
                            this.comboStore = Wtf.viewStoreStatus;
                        }

                    } else if(modulename=="Lead"){

                        if(comboboxname=="Type"){
                            this.comboStore = Wtf.LeadTypeStore;
                        } else if(comboboxname=='Industry'){
                            chkindustryload()
                            this.comboStore = Wtf.industryStore;
                        } else if(comboboxname=='Lead Status'){
                            chklstatusStoreload()
                            this.comboStore = Wtf.lstatusStore;
                        } else if(comboboxname=='Lead Rating' || comboboxname=='Rating'){
                            chklratingStoreload()
                            this.comboStore = Wtf.lratingStore;
                        } else if(comboboxname=='Lead Source'){
                            chkleadsourceload()
                            this.comboStore = Wtf.lsourceStore;
                        } else if(comboboxname=='Product'|| comboboxname=='Product Name'){
                            chkproductStoreload()
                            this.comboStore = Wtf.productStore
                        }
                        else if(comboboxname=='owner' || comboboxname=='Owner'){
                            this.comboStore = new Wtf.data.Store({
                                reader: new Wtf.data.KwlJsonReader({
                                    root:'data'
                                }, comboReader),
                                url: "Common/User/getOwner.do?module="+this.module
                            });
                            this.comboStore.load({params:{common:'1'}});
                        }

                    } else if(modulename=="Account"){

                        if(comboboxname=='Account'|| comboboxname=='Account Name') {
                            this.comboStore = Wtf.parentaccountstoreSearch
                        }else if(comboboxname=='Account Type' || comboboxname=='Type'){
                            chkaccounttypeload()
                            this.comboStore = Wtf.accountTypeStore;
                        }else if(comboboxname=='Industry'){
                            chkindustryload()
                            this.comboStore = Wtf.industryStore;
                        }else if(comboboxname=='Product'){
                            chkproductStoreload()
                            this.comboStore = Wtf.productStore
                        }else if(comboboxname=='Lead Source'){
                            chkleadsourceload()
                            this.comboStore = Wtf.lsourceStore;
                        }else if(comboboxname=='Lead Status'){
                            chklstatusStoreload()
                            this.comboStore = Wtf.lstatusStore;
                        }
                        else if(comboboxname=='Account Owner' || comboboxname=='Account Owner'){
                            this.comboStore = new Wtf.data.Store({
                                reader: new Wtf.data.KwlJsonReader({
                                    root:'data'
                                }, comboReader),
                                url: "Common/User/getOwner.do?module="+this.module
                            });
                            this.comboStore.load({params:{common:'1'}});
                        }

                    } else if(modulename=="Contact"){

                        if(comboboxname=='Contact'){
                            this.comboStore = Wtf.contactStoreSearch;
                        }else if(comboboxname=='Title'){
                            chktitleload()
                            this.comboStore = Wtf.titleStore;
                        }else if(comboboxname=='Industry'){
                            chkindustryload()
                            this.comboStore = Wtf.industryStore;
                        }else if(comboboxname=='Lead Source'){
                            chkleadsourceload()
                            this.comboStore = Wtf.lsourceStore;
                        }else if(comboboxname=='Account' || comboboxname=='Account name'|| comboboxname=='Account Name') {
                            this.comboStore = Wtf.relatedToNameStoreSearch
                        }
                        else if(comboboxname=='owner' || comboboxname=='Owner'){
                            this.comboStore = new Wtf.data.Store({
                                reader: new Wtf.data.KwlJsonReader({
                                    root:'data'
                                }, comboReader),
                                url: "Common/User/getOwner.do?module="+this.module
                            });
                            this.comboStore.load({params:{common:'1'}});
                        }

                    } else if(modulename=="Opportunity"){

                        if(comboboxname=='Opportunity Type' || comboboxname=='Type'){
                            chkopptypeStoreload()
                            this.comboStore = Wtf.opptypeStore;
                        } else if(comboboxname=='Region'){
                            chkregionStoreload()
                            this.comboStore = Wtf.regionStore;
                        } else if(comboboxname=='Opportunity Stage' || comboboxname=='Stage'){
                            chkoppstageload()
                            this.comboStore = Wtf.oppstageStore;
                        } else if(comboboxname=='Account' || comboboxname=='Account name'|| comboboxname=='Account Name') {
                            this.comboStore = Wtf.relatedToNameStoreSearch
                        } else if(comboboxname=='Lead Source'){
                            chkleadsourceload()
                            this.comboStore = Wtf.lsourceStore;
                        } else if(comboboxname=='Product'|| comboboxname=='Product Name'){
                            chkproductStoreload()
                            this.comboStore = Wtf.productStore
                        }
                        else if(comboboxname=='owner' || comboboxname=='Owner'){
                            this.comboStore = new Wtf.data.Store({
                                reader: new Wtf.data.KwlJsonReader({
                                    root:'data'
                                }, comboReader),
                                url: "Common/User/getOwner.do?module="+this.module
                            });
                            this.comboStore.load({params:{common:'2'}});
                        }

                    } else if(modulename=="Case"){

                        if(comboboxname=='Case Type' || comboboxname=='Type'){
                            chkcaseoriginStoreload()
                            this.comboStore = Wtf.caseoriginStore;
                        } else if(comboboxname=='Priority'){
                            chkpriorityload()
                            this.comboStore = Wtf.cpriorityStore;
                        } else if(comboboxname=='Case Status' || comboboxname=='Status'){
                            chkstatusload()
                            this.comboStore = Wtf.caseStatusStore;
                        } else if(comboboxname=='owner' || comboboxname=='Owner'){
                            this.comboStore = new Wtf.data.Store({
                                reader: new Wtf.data.KwlJsonReader({
                                    root:'data'
                                }, comboReader),
                                url: "Common/User/getOwner.do?module="+this.module
                            });
                            this.comboStore.load({params:{common:'1'}});
                        } else if(comboboxname=='assignedto'){
                            this.comboStore = Wtf.caseAssignedUserStore
                        } else if(comboboxname=='Account' || comboboxname=='Account name'|| comboboxname=='Account Name') {
                            Wtf.relatedToNameStoreSearch.load();
                            this.comboStore = Wtf.relatedToNameStoreSearch;
                        }else if(comboboxname=='Contact' || comboboxname=='Contact Name'){
                            Wtf.contactStoreSearch.load();
                            this.comboStore = Wtf.contactStoreSearch;
                        }else if(comboboxname=='Product'|| comboboxname=='Product Name'){
                            chkproductStoreload()
                            this.comboStore = Wtf.productStore
                        }

                    } else if(modulename=="Product"){

                        if(comboboxname=='Product'){
                            chkproductStoreload()
                            this.comboStore = Wtf.productStore
                        }else if(comboboxname=='Product Category' || comboboxname=='Category'){
                            chkproductcategorystoreload()
                            this.comboStore = Wtf.productcategorystore;
                        }
                        else if(comboboxname=='Owner'){
                            this.comboStore = new Wtf.data.Store({
                                reader: new Wtf.data.KwlJsonReader({
                                    root:'data'
                                }, comboReader),
                                url: "Common/User/getOwner.do?module="+this.module
                            });
                            this.comboStore.load({params:{common:'1'}});
                        }

                    } else {

                        if(comboboxname=='Task Type'){
                            chktasktypeload()
                            this.comboStore = Wtf.typeStore;
                        } else if(comboboxname=='Task Status'){
                            chktaskstatusload()
                            this.comboStore = Wtf.statusStore;
                        } else if(comboboxname=='owner'){
                            this.comboStore = new Wtf.data.Store({
                                reader: new Wtf.data.KwlJsonReader({
                                    root:'data'
                                }, comboReader),
                                url: "Common/User/getOwner.do?module="+this.module
                            });
                            this.comboStore.load({params:{common:'1'}});
                        }
                    }

                } else {  // for custom column
                        if(type=='8'){ // for reference custom combo
                                var cnfgID = record.data.configid
                                if(cnfgID=="users"){

                                    this.comboStore = new Wtf.data.Store({
                                        reader: new Wtf.data.KwlJsonReader({
                                            root:'data'
                                        }, comboReader),
                                        url: "Common/User/getOwner.do?module="+this.module
                                    });
                                    this.comboStore.load({params:{common:'1',allUsers:true}});

                                } else {
                                    this.comboStore = new Wtf.data.Store({
                                        url: 'Common/CRMManager/getRefComboData.do',
                                        baseParams:{
                                            common:'1',
                                            customflag:1,
                                            configid :cnfgID
                                        },
                                        reader: new Wtf.data.KwlJsonReader({
                                            root:'data'
                                        }, comboReader)
                                    });

                                    this.comboStore.load();
                               }


                        } else {
                                this.comboStore = new Wtf.data.Store({
                                    url: 'Common/CRMManager/getComboData.do',
                                    baseParams:{
                                        comboname:comboboxname,
                                        common:'1',
                                        customflag:1,
                                        configid :record.data.pojoname
                                    },
                                    reader: new Wtf.data.KwlJsonReader({
                                        root:'data'
                                    }, comboReader)
                                });

                                this.comboStore.load();
                        }
                }

            this.displayField=combo.getValue();

            this.searchText = new Wtf.form.ComboBox({
                valueField: 'id',
                displayField: 'name',
                store: this.comboStore,
                typeAhead:true,
                forceSelection :true,
                anchor: '95%',
                mode: 'local',
                triggerAction: 'all',
                selectOnFocus: true,
                emptyText: WtfGlobal.getLocaleText("crm.advancesearch.searchcombo.seloptmtytxt"),//'Select an option',
                width:125,
                iscustomcolumn:iscustomcolumn,
                fieldtype:type,
                dbname:dbcolumnname,
                refdbname:tablename
            });
        }

        if (recXtype == "Datefield"){
            this.searchText=new Wtf.form.DateField({
                width:125,
                format:"M d, Y ",
                iscustomcolumn:iscustomcolumn,
                fieldtype:type,
                dbname:dbcolumnname,
                refdbname:tablename
            });
        }
        if (recXtype == "Timefield"  ){
            this.searchText=new Wtf.form.TimeField({
                width:125,
                value:WtfGlobal.setDefaultValueTimefield(),
                format:WtfGlobal.getLoginUserTimeFormat(),
                iscustomcolumn:iscustomcolumn,
                fieldtype:type,
                dbname:dbcolumnname,
                refdbname:tablename
            });
        }
            this.text=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt")+": ");
            this.getTopToolbar().add(this.text);


            this.getTopToolbar().add(this.searchText);
        this.getTopToolbar().addButton([this.add = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("crm.targetlists.creatarr.addBTN"),//'Add',
            tooltip: {text: WtfGlobal.getLocaleText("crm.advancesearch.addtermtosearchtip")},//'Add a term to search.'},
            handler: this.addSearchFilter,
            scope: this,
            iconCls : 'pwnd addfilter'
        }),
        this.search = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("crm.advancesearch.savefilterbtn"),//WtfGlobal.getLocaleText("crm.advancesearch.savefilterbtn"),//
            tooltip: {text: WtfGlobal.getLocaleText("crm.advancesearch.savefilterbtn.ttip")},//'Click to save filter for this report.'},
            handler: this.doSearch,
            disabled:true,
            scope:this,
            iconCls :"advanceSearchButton"
        }),
        this.cancel = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.CLOSEBUTTON"),//'Close',
            tooltip: {text: WtfGlobal.getLocaleText("crm.advancesearch.closebtn.ttip")},//'Clear search terms and close advanced search.'},
            handler: this.confirmationToClose,
            scope:this,
            iconCls:'pwnd clearfilter'
        })]);

        this.add.getEl().dom.style.paddingLeft="4px";
        this.doLayout();
    },

    getComboData: function(grid,reconfigurestore){
        this.storeItmes = grid.getStore().data.items
        if(!this.myData || reconfigurestore==true){
            var mainArray=[];
            for (var i=0;i<this.storeItmes.length;i++) {
                var tmpArray=[];
                if((this.storeItmes[i].data.name!=undefined || this.storeItmes[i].data.name!="")) {
                	if(this.storeItmes[i].data.iscustomcolumn && this.storeItmes[i].data.type == "7" ){
                		continue;
                	}
                    var header=headerCheck(WtfGlobal.HTMLStripper(this.storeItmes[i].data.name));
                    header=header.replace("*","");
                    header=header.trim();

                    var modulename = this.storeItmes[i].data.modulename!=undefined?this.storeItmes[i].data.modulename:"";
                    var displayname = this.storeItmes[i].data.displayname!=undefined?this.storeItmes[i].data.displayname:"";
                    if(modulename != "") {
                        displayname += " [" + modulename + "]";
                    }
                    tmpArray.push(header);
                    tmpArray.push(this.storeItmes[i].data.dbcolumnname!=undefined?this.storeItmes[i].data.dbcolumnname:"");
                    tmpArray.push(displayname);
                    tmpArray.push(modulename);
                    tmpArray.push(this.storeItmes[i].data.tablename!=undefined?this.storeItmes[i].data.tablename:"");
                    tmpArray.push(this.storeItmes[i].data.defaultname!=undefined?this.storeItmes[i].data.defaultname:"");
                    tmpArray.push(this.storeItmes[i].data.type!=undefined?this.storeItmes[i].data.type:"");
                    tmpArray.push(this.storeItmes[i].data.iscustomcolumn!=undefined?this.storeItmes[i].data.iscustomcolumn:"");
                    tmpArray.push(this.storeItmes[i].data.pojoname!=undefined?this.storeItmes[i].data.pojoname:"");
                    tmpArray.push(this.storeItmes[i].data.dataindex!=undefined?this.storeItmes[i].data.dataindex:"");
                    tmpArray.push(this.storeItmes[i].data.configid!=undefined?this.storeItmes[i].data.configid:"");

                    mainArray.push(tmpArray)
                }
            }
            this.myData = mainArray;
            if(this.advSearch)
                this.combostore.loadData(this.myData);
        }
    }

});

