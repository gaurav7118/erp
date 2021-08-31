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

/*This component used for 
 *1. Aged Receivable Report
 *2. Aged Payable Report
 *3. Aged Report Based on sales Person
 */
Wtf.account.AgedDetail=function(config){
    this.expandButtonClicked = false;
    this.receivable=config.receivable||false;
    this.withinventory=config.withinventory||false;
    this.reportWithoutAging=config.reportWithoutAging||false;
    this.isSummary=config.isSummary||false;
    this.displayFlag=config.displayFlag||false;
    this.totalaged=config.totalaged||false;
    this.summary = new Wtf.ux.grid.GridSummary({});
    this.uPermType=(this.receivable?Wtf.UPerm.agedreceivable:Wtf.UPerm.agedpayable);
    this.permType=(this.receivable?Wtf.Perm.agedreceivable:Wtf.Perm.agedpayable);
    this.exportPermType=(this.receivable?this.permType.exportdataagedreceivable:this.permType.exportdataagedpayable);
    this.printPermType=(this.receivable?this.permType.printagedreceivable:this.permType.printagedpayable);
    this.chartPermType=(this.receivable?this.permType.chartagedreceivable:this.permType.chartagedpayable);
    this.custVendorID=config.custVendorID;
    this.isAgedForSalesPerson=config.isAgedForSalesPerson;
    this.isParentChild=config.isParentChild!=undefined?config.isParentChild:false;
    this.isCustomWidgetReport=config.isCustomWidgetReport||false;
//    this.moduleid = this.receivable?Wtf.Acc_Customer_AccountStatement_ModuleId:Wtf.Acc_Vendor_AccountStatement_ModuleId;
//*********************following fields are only used when grouping is performed  using group by combo*********************    
    this.moduleIDForFetchingGroupingData="";
    this.listOfModuleIDForFetchingGroupingData="";
    this.fieldIDsListForFetchingGroupingData="";
//************************************************************************************************************************
    /*
    * setting moduleid after checking moduleid in config 
    */ 
   this.moduleid = "";
   if(config.moduleId){
       this.moduleid = config.moduleId;
   }else{
       if(this.isSummary){
           if(this.receivable){
               this.moduleid = Wtf.Acc_AgedReceivables_Summary_ModuleId;
           }else{
               this.moduleid = Wtf.Acc_AgedPayables_Summary_ModuleId;
           }
       }else{
           if(this.receivable){
               this.moduleid = Wtf.Acc_AgedReceivables_ReportView_ModuleId;
           }else{
               this.moduleid = Wtf.Acc_AgedPayables_ReportView_ModuleId;
           }
       }
   }
    this.arr = [];
    this.AgedRec = new Wtf.data.Record.create([{
            name:'billid'
        },{
            name:'journalentryid'
        },{
            name:'entryno'
        },{
            name:'supplierinvoiceno'
        },{
            name:'billno'
        },{
            name:'noteid'
        },{
            name:'noteno'
        },{
            name:'date', type:'date'
        },{
            name:'duedate', type:'date'
        },{
            name:'personname'
        },{
            name:'code'
        },{
            name:'aliasname'
        },{
            name:'personemail'
        },{
            name:'personid'
        },{
            name:'salespersonname'
        },{
            name:'salespersonid',mapping:'salesPerson'
        },{
            name: 'currencysymbol'
        },{
            name: 'currencyname'
        },{
            name: 'currencyid'
        },{
            name:'amountdueinbase'
        },{
             name:'amountdue'
        },{
            name:'amountdue1'
        },{
            name:'amountdue2'
        },{
            name:'amountdue3'
        },{
            name:'amountdue4'
        },{
            name:'amountdue5'
        },{
            name:'amountdue6'
        },{
            name:'amountdue7'
        },{
            name:'amountdue8'
        },{
            name:'amountdue9'
        },{
            name:'amountdue10'
        },{
            name:'amountdue11'    
        },{
            name:'total'
        },{
            name:'amountdueinbase1'
        },{
            name:'amountdueinbase2'
        },{
            name:'amountdueinbase3'
        },{
            name:'amountdueinbase4'
        },{
            name:'total'
        },{
            name:'memo'
        },{
            name:'totalinbase'
        },{
            name: 'currencysymbol'
        },{
            name: 'termname' //customer credit term
        },{
            name: 'creditlimit' //customer credit limit      
        },{
            name: 'creditlimitinbase' //customer credit limit in base     
        },{
            name: 'withoutinventory', type:'boolean'
        }, {
            name:'type'
        },{
            name:'isOpeningBalanceTransaction'
        },{
            name:'sequenceformatid'
        },{
            name:'includingGST'
        },{
            name:'salesPersonID'
        },{
            name:'costcenterid'
        },
        {name:'fixedAssetInvoice'},
        {name:'fixedAssetLeaseInvoice'},
        {name:'customerId'},
        {name:'start'},
        {name: 'fCustomerId'},
        {name: 'exchangerate'},
        {name:"dimensionvalue"},
        {name:'cntype'},
        {name:'isConsignment', type:'boolean'},
        {name:'isLeaseFixedAsset', type:'boolean'},
        {name:'personinfo'},
        {name: 'gTaxId'},
        {name: 'includeprotax'},
        {name: 'lasteditedby'},
        {name: 'customercreditterm'},
        {name: 'leaf'},
        {name: 'level'}
    ]);
     this.interval=new Wtf.form.NumberField({
        fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.till"),  //'Till',
        maxLength:2,
        width:30,
        allowDecimal:false,
        allowBlank:true,
        minValue:2,
        name:'duration',
        value:this.receivable ? Wtf.agedReceivableInterval: Wtf.agedPayableInterval
    });
 
    this.noOfIntervalStore= new Wtf.data.SimpleStore({
        fields: [{name : 'value'}],
        data :[[2], [3], [4], [5], [6], [7], [8], [9], [10]]
    });
    
    this.noOfIntervalCombo = new Wtf.form.ComboBox({
        store: this.noOfIntervalStore,
        name:'noOfInterval',
        displayField:'value',
        value:this.receivable ? Wtf.agedReceivableNoOfInterval : Wtf.agedPayableNoOfInterval,
        width:50,
        valueField:'value',
        mode: 'local',
        triggerAction: 'all'
    });

    this.AgedStoreUrl = "";
    this.AgedStoreSummaryUrl = "";
    //(this.receivable?19:12),
    if(this.receivable){
//        //mode:(this.withinventory?12:16),
        this.AgedStoreUrl = "ACCInvoiceCMN/getInvoicesMerged.do";
//        this.AgedStoreUrl = "CommonFunctions/verifyAgedReceivablesChanges.do";
        this.expGet = this.withinventory?24:25; 
//        //(this.isSummary?18:(this.withinventory?12:16))
        if(this.isAgedForSalesPerson){
            this.AgedStoreUrl = "ACCInvoiceCMN/getSalesPersonAgedDetail.do";
            this.AgedStoreSummaryUrl = "ACCInvoiceCMN/getSalesPersonAgedSummary.do";
            this.expSummGet = Wtf.autoNum.agedSummaryBasedOnSalesPerson
            this.expGet=Wtf.autoNum.agedDetailBasedOnSalesPerson;
        } else {
            this.AgedStoreSummaryUrl = this.isSummary?"ACCInvoiceCMN/getCustomerAgedReceivable.do":"ACCInvoiceCMN/getInvoicesMerged.do";
//            this.AgedStoreSummaryUrl = this.isSummary?"ACCInvoiceCMN/getCustomerAgedReceivable.do":"CommonFunctions/verifyAgedReceivablesChanges.do";
            this.expSummGet = this.isSummary?26:this.expGet;
        }                
    }else{
//        //mode:(this.withinventory?12:16),
        this.AgedStoreUrl = "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
        this.expGet = this.withinventory?21:22;
//        //(this.isSummary?18:(this.withinventory?12:16))
        this.AgedStoreSummaryUrl = this.isSummary?"ACCGoodsReceiptCMN/getVendorAgedPayable.do":"ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
        this.expSummGet = this.isSummary?23:this.expGet;
    }
    this.includeExcludeChildStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'name'
                }, {
                    name: 'value',
                    type: 'boolean'
                }],
            data: [[WtfGlobal.getLocaleText("acc.includechildcustomer"), true], [WtfGlobal.getLocaleText("acc.excludechildcustomer"), false]]
        });
        /* 
          initial value of combobox is set to All and disabled is true for disable purpose
         */
        this.includeExcludeChildCmb = new Wtf.form.ComboBox({
            labelSeparator: '',
            labelWidth: 0,
            triggerAction: 'all',
            mode: 'local',
            valueField: 'value',
            displayField: 'name',
            store: this.includeExcludeChildStore,
            value: 'All',
            width: 200,
            hidden:!this.receivable,
            disabledClass: "newtripcmbss",
            name: 'includeExcludeChildCmb',
            hiddenName: 'includeExcludeChildCmb',
            emptyText:'All',
            disabled:true
        }); 
    this.AgedStore =this.isSummary? new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.AgedRec),
        remoteSort:(!(this.isAgedForSalesPerson||this.isParentChild))?true:false,
        groupField:'personname', 
        sortInfo: {field: 'personname',direction: "ASC"},
        url: this.AgedStoreSummaryUrl,
        //url: Wtf.req.account+(this.receivable?'CustomerManager.jsp':'VendorManager.jsp'),
        baseParams:{
            mode:(this.isSummary?18:(this.withinventory?12:16)),
            creditonly:true,
            withinventory:this.withinventory,
            ignorezero:true,
            isdistributive:this.typeEditor != undefined?this.typeEditor.getValue():true,
            datefilter : this.dateFilter != undefined ? this.dateFilter.getValue() : 0,
            nondeleted:true,
            isAged:true,
            isParentChild:this.isParentChild
          }
    }):new Wtf.ux.grid.MultiGroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.AgedRec),
        remoteSort: (!(this.isAgedForSalesPerson||this.isParentChild))?true:false,
        groupField:this.isAgedForSalesPerson?['salespersonname']:['personinfo','type'],
        sortInfo: {field: 'personname',direction: "ASC"},
        url: this.AgedStoreUrl,
//        url: Wtf.req.account+(this.receivable?'CustomerManager.jsp':'VendorManager.jsp'),
        baseParams:{
            mode:(this.withinventory?12:16),
            creditonly:true,
            withinventory:this.withinventory,
            datefilter : this.dateFilter != undefined ? this.dateFilter.getValue() : 0,
            ignorezero:true,
            nondeleted:true,
            isAged:true
        }
    });
//    if(!this.displayFlag){
//        this.AgedStore.sort('personname', 'DESC');
//    }
//    if(this.displayFlag){
//        this.AgedStore.sort('personname', 'DESC');
//    }
    
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:"boolean"}, 'name'],
        data :[[true,WtfGlobal.getLocaleText("acc.rem.127")],[false,WtfGlobal.getLocaleText("acc.rem.128")]]
    });
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name:'isdistributive',
        displayField:'name',
        value:true,
        anchor:"30%",
        width:100,
        valueField:'typeid',
        mode: 'local',
        triggerAction: 'all'
    });
    
    //************************************Below code is used only when group by dimensions functionality is used*********************
     this.combostore = new Wtf.data.SimpleStore({
        fields: [
        {
            name: 'header'
        },
        {
            name: 'fieldid'
        },
        {
            name: 'moduleid'
        },
        {
            name: 'modulename' //this field is used to hold comma seperated module Names when same dimension present in multiple modules(Invoice,Credit Note,Debit note,Receipt)
        },
        {
            name: 'fieldidlist' //this field is used to hold comma seperated field ids when same dimension present in multiple modules(Invoice,Credit Note,Debit note,Receipt)
        },
        {
            name: 'moduleidlist' //this field is used to hold comma seperated module ids when same dimension present in multiple modules(Invoice,Credit Note,Debit note,Receipt)
        }]
    });
    
    var mainArray = [];
    var firsRrec = [];
    
    firsRrec.push(Wtf.ExportMolueName.customer);
    firsRrec.push(Wtf.ExportMolueName.customer);
    firsRrec.push(Wtf.ExportMolueName.customer);
    firsRrec.push("NA");
    firsRrec.push("NA");//4
    firsRrec.push("NA");//5
    mainArray.push(firsRrec)
    
    var ModuleArr =[];
    var header;
    if(this.receivable && this.isSummary){
       ModuleArr= [Wtf.Acc_Customer_ModuleId, Wtf.Acc_Invoice_ModuleId,Wtf.Acc_Credit_Note_ModuleId,Wtf.Acc_Debit_Note_ModuleId,Wtf.Acc_Receive_Payment_ModuleId];
        var tmpArray = [];
            header = getModuleNameByModuleNumber(Wtf.Acc_Customer_ModuleId);
            tmpArray.push(header);
            tmpArray.push("NA");
            tmpArray.push("NA");
            tmpArray.push("NA");
            tmpArray.push("NA");
            tmpArray.push("NA");
            mainArray.push(tmpArray)
        
    }else if(this.receivable && !this.isSummary){
         ModuleArr= [ Wtf.Acc_Invoice_ModuleId,Wtf.Acc_Credit_Note_ModuleId,Wtf.Acc_Debit_Note_ModuleId,Wtf.Acc_Receive_Payment_ModuleId];
        
         var tmpArray = [];
                header = '--------[Transaction Level Dimension(s)]---------';
                tmpArray.push(header);
                tmpArray.push("NA");
                tmpArray.push("NA");
                tmpArray.push("NA");
                tmpArray.push("NA");
                tmpArray.push("NA");
                mainArray.push(tmpArray)
    }
            
    for (var i = 0; i < ModuleArr.length; i++) {
        var customfieldArr = GlobalDimensionCustomFieldModel[ModuleArr[i]];
        if (customfieldArr != undefined) {
            var tmpArray = [];
            
            if(this.receivable && this.isSummary && i==1) {
                /*i==1 -> to skip Customer master dimension because they are alredy added in summary view
                 * This if block is used in case of summary view view to add transaction level dimensions label
                 */
                
                header = '--------[Transaction Level Dimension(s)]---------';
                tmpArray.push(header);
                tmpArray.push("NA");
                tmpArray.push("NA");
                tmpArray.push("NA");
                tmpArray.push("NA");
                mainArray.push(tmpArray)
            }

            for (var j = 0; j < customfieldArr.length; j++) {
                tmpArray = [];
                var mainArrayIndex;
                var customfielddata = customfieldArr[j];
                
                var fieldname=customfielddata.fieldname;
                var indexOfChar = fieldname.indexOf("_");
                var header=fieldname.substring(indexOfChar+1,(fieldname.length)); 
                var repeated=false;
                if (!customfielddata.iscustomcolumn && !customfielddata.iscustomfield) {//check to include only dimension in group by combo
                    
                    for(var k=0; k<mainArray.length;k++){
                    /*
                     * Here in this for loop i am checking that this dimension is already present or not in mainArray.
                     */
                    
                           if(header == mainArray[k][0] && mainArray[k][2] != Wtf.Acc_Customer_ModuleId){
                                repeated=true;
                                mainArrayIndex=k;
                               break;
                           }
                        
                    }
                    if (!repeated) {
                        /*
                         * make fresh entry in mainArray if given dimension is not available in mainarray
                         */
                        tmpArray.push(header);
                        tmpArray.push(customfielddata.fieldid);
                        tmpArray.push(customfielddata.moduleid);
                        tmpArray.push(WtfGlobal.getModuleName(ModuleArr[i]));
                        
                        tmpArray.push(customfielddata.fieldid);//4- this is usred for comma seperated fieldids
                        tmpArray.push(customfielddata.moduleid);//5 - this is usred for comma seperated fieldids
                        mainArray.push(tmpArray)
                    }else{
                        /*
                         * If Dimension is already present in mainArray then update its modulename,moduleids,fieldids fields which is used later
                         */
                        
                        mainArray[mainArrayIndex][3]=mainArray[mainArrayIndex][3]+' ; '+WtfGlobal.getModuleName(ModuleArr[i]);// comma seperated modulenames module name
                        mainArray[mainArrayIndex][4]=mainArray[mainArrayIndex][4]+' , '+customfielddata.fieldid; // comma seperated fieldids 
                        mainArray[mainArrayIndex][5]=mainArray[mainArrayIndex][5]+' , '+customfielddata.moduleid; // comma seperated moduleids
                        
                    }
                }
            }
        }
    }
    
    /*
     * mainArray[k][0] =Dimension  Header
     * mainArray[k][1] =Dimension  fieldid
     * mainArray[k][2] =Dimension  moduleid
     * mainArray[k][3] =Dimension  present in how many modules string(modulenames string)
     */
    
    
    
    for (var k = 0; k < mainArray.length; k++) {
        if (mainArray[k][3] != "NA" && mainArray[k][2] != Wtf.Acc_Customer_ModuleId ) {
            /*
             * Check whether givent element is not label or customer
             * Here i am appending Dimension with its availability in other module like manager(invoice,creditnote etc).
             */
            
            mainArray[k][0] = mainArray[k][0] + '(' + mainArray[k][3] + ')';
        }
    }
    
    //************************************above code is used only when group by dimensions functionality is used*********************
    this.myData = mainArray;
    this.combostore.loadData(this.myData);
     this.columnCombo = new Wtf.form.ExtFnComboBox({
        store: this.combostore,
        editable: false,
        typeAhead: true,
        selectOnFocus: true,
        displayField: 'header',
        valueField: 'fieldid',
        triggerAction: 'all',
        hidden:this.receivable ? false : true,
        emptyText: WtfGlobal.getLocaleText("acc.combo.groupby.dimensions.emptytext"), //'Select a Search Field to search',
        mode: 'local',
        extraComparisionField: "",
        extraFields: "",
        listWidth: 400,
        hidden:(!this.isSummary && !this.receivable) || this.isAgedForSalesPerson,
        isAdvanceSearchCombo:true
    });
    this.columnCombo.on('change',this.groupByOnChange,this);
    this.columnCombo.setValue(Wtf.ExportMolueName.customer);
    this.columnCombo.on('beforeselect', function (combo, record, index) {
        if (record.get('fieldid') == "NA") {
            return false;
        }
    }, this);
    
    this.dateFilterStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name','tooltip'],
        data :[[0,WtfGlobal.getLocaleText("acc.agedPay.dueDate1-30"),WtfGlobal.getLocaleText("acc.agedPay.dueDate1-30tt")],[2,WtfGlobal.getLocaleText("acc.agedPay.dueDate0-30"),WtfGlobal.getLocaleText("acc.agedPay.dueDate0-30tt")],[1,WtfGlobal.getLocaleText("acc.agedPay.invoiceDate1-30"),WtfGlobal.getLocaleText("acc.agedPay.invoiceDate1-30tt")],[3,WtfGlobal.getLocaleText("acc.agedPay.invoiceDate0-30"),WtfGlobal.getLocaleText("acc.agedPay.invoiceDate0-30tt")]]
    });
    this.dateFilter = new Wtf.form.ComboBox({
        store: this.dateFilterStore,
        name:'datefilter',
        displayField:'name',
        value: this.receivable ? Wtf.agedReceivableDateFilter : Wtf.agedPayableDateFilter,
        anchor:"30%",
        width:150,
        valueField:'id',
        mode: 'local',
        triggerAction: 'all',
        tpl : new Wtf.XTemplate('<tpl for="."><div class="x-combo-list-item" Wtf:qtip="{tooltip}">{name}</div></tpl>')
    });
    /*Group By Fileter ( 1. Group by transaction type (which will be set as default), 2. Sort by Date)*/

     this.groupComboStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data:[[0,"Group by Transaction Type"],[1,"Sort by Date"],[2,"Base Currency"],[3,"Other than Base Currency"]]
    });
    this.groupCombo = new Wtf.form.ComboBox({
        store: this.groupComboStore,
        name:'datefilter',
        displayField:'name',
        value:0,
        anchor:"30%",
        width:200,
        valueField:'id',
        mode: 'local',
        triggerAction: 'all'
    });
    this.groupCombo.on('select',this.groupComboChange,this);
       this.expandRec = new Wtf.data.Record.create([{
        name:'billid'
    },{
        name:'journalentryid'
    },{
        name:'entryno'
    },{
        name:'billno'
    },{
        name:'noteid'
    },{
        name:'noteno'
    },{
        name:'date', 
        type:'date'
    },{
        name:'duedate', 
        type:'date'
    },{
        name:'personname'
    },{
        name:'aliasname'
    },{
        name:'personemail'
    },{
        name:'personid'
    },{
        name:'salespersonname'
    },{
        name:'salespersonid',mapping:'salesPerson'
    },{
        name: 'currencysymbol'
    },{
        name: 'currencyname'
    },{
        name: 'currencyid'
    },{
        name:'amountdueinbase'
    },{
        name:'amountdue'
    },{
        name:'amountdue1'
    },{
        name:'amountdue2'
    },{
        name:'amountdue3'
    },{
        name:'amountdue4'
    },{
        name:'amountdue5'
    },{
        name:'amountdue6'
    },{
        name:'amountdue7'
    },{
        name:'amountdue8'
    },{
        name:'amountdue9'
    },{
        name:'amountdue10'
    },{
        name:'amountdue11'
    },{
        name:'total'
    },{
        name:'memo'
    },{
        name:'totalinbase'
    },{
        name: 'currencysymbol'
    },{
        name: 'termname'
    },{
        name: 'withoutinventory', 
        type:'boolean'
    }, {
        name:'type'
    },
    {
        name:'fixedAssetInvoice'
    },
    {
        name:'fixedAssetLeaseInvoice'
    },
    {
        name:'personinfo'
    }
    ]);
    
    if(this.receivable){
        this.expandStoreUrl = this.isSummary?"ACCInvoiceCMN/getCustomerAgedReceivable.do":"ACCInvoiceCMN/getInvoicesMerged.do";
//        this.expandStoreUrl = this.isSummary?"ACCInvoiceCMN/getCustomerAgedReceivable.do":"CommonFunctions/verifyAgedReceivablesChanges.do";
    }else{
        this.expandStoreUrl = this.isSummary?"ACCGoodsReceiptCMN/getVendorAgedPayable.do":"ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
    }
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
        baseParams:{
            mode:(this.isSummary?18:(this.withinventory?12:16)),
            creditonly:true,
            withinventory:this.withinventory,
            ignorezero:true,
            nondeleted:true,
            isAged:true,
            isParentChild:this.isParentChild
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    
    this.expandStore.on("beforeload", function(store){
        if(this.receivable){
            this.expandStoreUrl = this.isSummary?"ACCInvoiceCMN/getCustomerAgedReceivable.do":"ACCInvoiceCMN/getInvoicesMerged.do";
//            this.expandStoreUrl = this.isSummary?"ACCInvoiceCMN/getCustomerAgedReceivable.do":"CommonFunctions/verifyAgedReceivablesChanges.do";
        }else{
            this.expandStoreUrl = this.isSummary?"ACCGoodsReceiptCMN/getVendorAgedPayable.do":"ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
        }
        store.proxy.conn.url = this.expandStoreUrl;
    }, this);

    this.expander = new Wtf.grid.RowExpander({});
    this.rowNo=new Wtf.KWLRowNumberer();
    this.chkselModel = new Wtf.grid.CheckboxSelectionModel({
        singleSelect : false
    });
    this.rowselModel = new Wtf.grid.RowSelectionModel();
    this.sm = this.isSummary?this.chkselModel:this.chkselModel;
    this.sm.on('selectionchange', this.handleRowSelect, this);
//    this.summary = new Wtf.ux.grid.GridSummary();
    //this.summary = new Wtf.grid.GroupSummary({});
//    this.summary = this.isSummary? new Wtf.ux.grid.GridSummary() : new Wtf.grid.GroupSummary({});

        
     this.colModelData = [];
     this.colModelData.push(this.sm, this.rowNo,(this.isSummary&&!this.isAgedForSalesPerson)?this.expander:{
            header: WtfGlobal.getLocaleText("acc.field.DocumentNumber"),//(this.receivable?WtfGlobal.getLocaleText("acc.agedPay.inv"): WtfGlobal.getLocaleText("acc.agedPay.venInv"))+" "+WtfGlobal.getLocaleText("acc.agedPay.number"),
            hidden:this.isSummary,
            dataIndex:'billno',
            width:150,
            pdfwidth:75,
//            renderer:config.isOrder?"":WtfGlobal.linkDeletedRenderer
            renderer:WtfGlobal.linkDeletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.inv.fieldset.title"),//Dimensions - this column is used when grouping is performed on dimensions
            hidden:false,
            hidden:true,
            dataIndex:'dimensionvalue',
            width:150,
            pdfwidth:75
        })
        if(!this.isSummary){
            this.colModelData.push({
            header:WtfGlobal.getLocaleText("acc.agedPay.gridJEno"),  //"Journal Entry Number",
            dataIndex:'entryno',
            hidden:(this.isSummary || this.isAgedForSalesPerson),
            width:150,
            pdfwidth:100,
            sortable: (this.isAgedForSalesPerson||this.isParentChild)?true:false,
            groupable: true,
            groupRenderer: function(v){return v},
            renderer:WtfGlobal.linkRenderer
            });
        }        
    if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && !this.isSummary && !this.receivable){
        this.colModelData.push({
            header:WtfGlobal.getLocaleText("acc.invoice.SupplierInvoiceNumber"),  //"Journal Entry Number",
            dataIndex:'supplierinvoiceno',
            width:150,
            pdfwidth:100
        });
    }        
        this.colModelData.push({
            header:WtfGlobal.getLocaleText("acc.agedPay.gridDate"),  //"Bill Date",
            dataIndex:'date',
            width:150,
             pdfwidth:100,
            align:'center',
            groupRenderer:this.groupDateRender.createDelegate(this),
            hidden:(this.isSummary || this.isAgedForSalesPerson),
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.agedPay.gridDueDate"),  //"Due Date",
            dataIndex:'duedate',
            sortable: (this.isAgedForSalesPerson||this.isParentChild)?true:false,
            width:150,
            pdfwidth:100,
            groupable: true,
            hidden:(this.isSummary || this.isAgedForSalesPerson),
            align:'center',
            groupRenderer: this.groupDateRender.createDelegate(this),
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:(this.moduleid === Wtf.Acc_AgedReceivables_Summary_ModuleId || this.moduleid === Wtf.Acc_AgedReceivables_ReportView_ModuleId)? WtfGlobal.getLocaleText("acc.invoiceList.salesPersonName"):WtfGlobal.getLocaleText("acc.invoiceList.agentName"),
            dataIndex:'salespersonname',
            hidden:!this.isAgedForSalesPerson,
            width:150,
            pdfwidth:150,
            sortable: (this.isAgedForSalesPerson||this.isParentChild)?true:false,
            groupable: true,
            summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
        });
        if (!(this.isAgedForSalesPerson && this.isSummary)){
        this.colModelData.push({
            header:this.isAgedForSalesPerson?WtfGlobal.getLocaleText("acc.cust.name"):(this.receivable?WtfGlobal.getLocaleText("acc.agedPay.cus"):WtfGlobal.getLocaleText("acc.agedPay.ven"))+"/"+ WtfGlobal.getLocaleText("acc.agedPay.accName"),
            dataIndex:'personname',
            width:150,
            pdfwidth:150,
            //hidden:(this.isAgedForSalesPerson && this.isSummary),
            sortable: true,
            groupable: true,
            summaryRenderer:this.isAgedForSalesPerson ? "" : function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
        },{
            header:this.receivable ? WtfGlobal.getLocaleText("acc.agedPay.customerCreditLimit"):WtfGlobal.getLocaleText("acc.agedPay.VendorCreditLimit"),
            dataIndex:'creditlimitinbase',
            sortable: (this.isAgedForSalesPerson||this.isParentChild)?true:false,
            pdfwidth:100,
            groupable: true,
        //    hidden: !(this.isAgedForSalesPerson && !this.isSummary), //show only in detail view of sales pereson aged report
            align:'center',
            renderer:WtfGlobal.currencyRenderer
        },{        
            header:this.receivable ? WtfGlobal.getLocaleText("acc.agedPay.customercreditterm") : WtfGlobal.getLocaleText("acc.agedPay.vendordebitterm"),
            dataIndex:'customercreditterm',
            sortable: (this.isAgedForSalesPerson||this.isParentChild)?true:false,
            pdfwidth:100,
            groupable: true,
           // hidden: !(this.receivable && this.isSummary && !this.isAgedForSalesPerson ), // Customer credit term SDP-6157
            align:'center'
        },{
            header:this.receivable?WtfGlobal.getLocaleText("acc.cust.aliasname"):WtfGlobal.getLocaleText("acc.ven.aliasname"),
            dataIndex:'aliasname',
            width:150,
           // hidden:this.isAgedForSalesPerson,
            pdfwidth:150,
            sortable: (this.isAgedForSalesPerson||this.isParentChild)?true:false,
            pdfwidth:120
        });
     }
     if(!(this.isAgedForSalesPerson||this.isParentChild)){
        this.colModelData.push({
            header: (this.receivable ? WtfGlobal.getLocaleText("acc.agedPay.cus") : WtfGlobal.getLocaleText("acc.agedPay.ven")) + WtfGlobal.getLocaleText(" Code"),
            dataIndex: 'code',
            align: 'left',
            width: 150,
            pdfwidth: 150,
            sortable: true,
        });
     }
     this.colModelData.push({
            header:WtfGlobal.getLocaleText("acc.agedReceiveandPayablePersonInfo"),
            dataIndex:'personinfo',
            width:150,
            pdfwidth:150,
            hidden:true,
            sortable: (this.isAgedForSalesPerson||this.isParentChild)?true:false,
            groupable: true,
            summaryRenderer:this.isAgedForSalesPerson ? "" : function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
        },
//        {
//            header:this.receivable?WtfGlobal.getLocaleText("acc.cust.aliasname"):WtfGlobal.getLocaleText("acc.ven.aliasname"),
//            dataIndex:'aliasname',
//            width:150,
//            hidden:this.isAgedForSalesPerson,
//            pdfwidth:150,
//            sortable: true,
//            pdfwidth:120
//        },
//        },{        // Remove Customer Term as Term Column is already present For Showing Document Term.
//            header:WtfGlobal.getLocaleText("acc.agedPay.customerTerm"),
//            dataIndex:'termname',
//            sortable: true,
//            pdfwidth:100,
//            groupable: true,
//            hidden: !(this.isAgedForSalesPerson && !this.isSummary), //show only in detail view of sales pereson aged report
//            align:'center'
  //      },
//        {        
//            header:this.receivable ? WtfGlobal.getLocaleText("acc.agedPay.customercreditterm") : WtfGlobal.getLocaleText("acc.agedPay.vendordebitterm"),
//            dataIndex:'customercreditterm',
//            sortable: true,
//            pdfwidth:100,
//            groupable: true,
//            hidden: !(this.receivable && this.isSummary && !this.isAgedForSalesPerson ), // Customer credit term SDP-6157
//            align:'center'
//        },
//        {
//            header:this.receivable ? WtfGlobal.getLocaleText("acc.agedPay.customerCreditLimit"):WtfGlobal.getLocaleText("acc.agedPay.VendorCreditLimit"),
//            dataIndex:'creditlimitinbase',
//            sortable: true,
//            pdfwidth:100,
//            groupable: true,
//            hidden: !(this.isAgedForSalesPerson && !this.isSummary), //show only in detail view of sales pereson aged report
//            align:'center',
//            renderer:WtfGlobal.currencyRenderer
//        },
        {
            header:WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"),  //"Currency Symbol",
            dataIndex:'currencyname',
            align:'center',
            width:150,
            pdfwidth:120
            //renderer:WtfGlobal.deletedRenderer
         },{
            header:WtfGlobal.getLocaleText("acc.setupWizard.curEx"),  //"Exchange Rate",
            dataIndex:'exchangerate',
            align:'center',
            hidden:(this.isSummary ||this.isAgedForSalesPerson),
            width:150,
            pdfwidth:120
         },{
            header:WtfGlobal.getLocaleText("acc.field.TransactionType"),
            dataIndex:'type',
            align:'center',
            pdfwidth:120,
            groupable: true,
            hidden:true
         },{
            header:(this.isSummary?WtfGlobal.getLocaleText("acc.agedPay.gridCurrent"):WtfGlobal.getLocaleText("acc.agedPay.gridAmtDue")),  //"Current":"Amount Due",
            dataIndex:this.isSummary?'amountdue1':'amountdue',
            align:'right',
            width:150,
            pdfwidth:120,
            hidden:this.reportWithoutAging,
//            summaryType:this.isSummary?'sum':"",
//            summaryRenderer:this.isSummary?WtfGlobal.withoutRateCurrencySymbol:"",
            renderer:WtfGlobal.withoutRateCurrencySymbol//,
//            summaryType:'sum',
//            summaryRenderer: function(value,m,rec){
//                var retVal = WtfGlobal.withoutRateCurrencySymbol(value,m,rec)
//                return '<b>'+retVal+'</b>';
//            }
         });
        if (!this.isSummary) {
            this.colModelData.push({
            header: WtfGlobal.getLocaleText("acc.agedPay.gridCurrent"), //"Current",
            dataIndex: 'amountdue1',
            hidden: this.isSummary,
            align: 'right',
            width: 150,
            pdfwidth: 120,
            //  hidden:this.typeEditor.getValue(),
//            summaryType:this.isSummary?'sum':"",
//            summaryRenderer:this.isSummary?WtfGlobal.withoutRateCurrencySymbol:"",
            renderer:WtfGlobal.withoutRateCurrencySymbol//,
//            summaryType:'sum',
//            summaryRenderer: function(value,m,rec){
//                var retVal = WtfGlobal.withoutRateCurrencySymbol(value,m,rec)
//                return '<b>'+retVal+'</b>';
//            }
            });
        }
        
    this.colModelData.push({
        header: (!this.typeEditor.getValue() ? "" : (this.dateFilter.getValue() != Wtf.agedDueDate0to30Filter && this.dateFilter.getValue() != Wtf.agedInvoiceDate0to30Filter) ? "1-" : "0-") + this.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + (!this.typeEditor.getValue() ? " " + WtfGlobal.getLocaleText("acc.agedPay.before") + " " : ""),
        dataIndex: 'amountdue2',
        hidden: this.reportWithoutAging,
        width: 150,
        pdfwidth: 120,
        //summaryRenderer:this.isSummary?WtfGlobal.withoutRateCurrencySymbol:"",
        align: 'right',
        renderer: WtfGlobal.withoutRateCurrencySymbol//,
    });
    
    this.totalNoOfColumns = 9  // Use For FOR Loop Iteration.  keeping Value as 9 because  amountdue1 and amountdue2 is already push in ColModelData.
    this.calculateDataIndex = 2 // we have already added 2 columns in column model current and 1-30 so keeping this value as 2
    
    for (var i = 1 ; i <= this.totalNoOfColumns ; i++) { // Pushing Columns From amountdue3 to amountdue11 i.e 31-60 to >270.
        this.colModelData.push({
            header: ((!this.typeEditor.getValue() ? "" : ((this.interval.getValue() * i) + 1) + "-") + (this.interval.getValue() * (i + 1)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + (!this.typeEditor.getValue() ? " " + WtfGlobal.getLocaleText("acc.agedPay.before") + " " : "")),
            dataIndex: 'amountdue' + (i + this.calculateDataIndex), // 
            hidden: this.reportWithoutAging,
            width: 150,
            pdfwidth: 120,
            align: 'right',
            renderer: (!this.typeEditor.getValue() ? this.totalRender.createDelegate(this) : WtfGlobal.withoutRateCurrencySymbol)
        });
    }
//        this.colModelData.push({
//            header:WtfGlobal.getLocaleText("acc.agedPay.accruedbalance"),
//            hidden: !this.typeEditor.getValue()||this.reportWithoutAging,
//            width:150,
//            pdfwidth:120,
//            dataIndex:'accruedbalance',
//            align:'right',
//            renderer:(!this.typeEditor.getValue()?this.totalRender.createDelegate(this):WtfGlobal.withoutRateCurrencySymbol)
//        });
        /**
         * Hide Total column from AP and AR report.SDP-13193
         */
        if (!(this.moduleid == Wtf.Acc_AgedReceivables_Summary_ModuleId || this.moduleid == Wtf.Acc_AgedPayables_Summary_ModuleId)) {
        this.colModelData.push({
            header: this.isSummary ? '<b>' + WtfGlobal.getLocaleText("acc.common.total") + '</b>' : Wtf.account.companyAccountPref.descriptionType, //"Memo",
            align: 'right',
            width: 150,
            pdfwidth: 150,
            hidden: this.isAgedForSalesPerson,
//            summaryType:this.isSummary?'sum':"",
//            summaryRenderer:this.isSummary?this.totalRender:"",
            dataIndex: this.isSummary ? "total" : 'memo',
            pdfrenderer: "rowcurrency",
            renderer: this.isSummary ? this.totalRender.createDelegate(this) : WtfGlobal.memoRenderer   // ERP-21192 tooltip for 'memo' column
        });
    }
    if(!this.isSummary)
    {
        this.colModelData.push({
            header:WtfGlobal.getLocaleText("acc.agedPay.gridAmtDueHomeCurrency")+ " ("+WtfGlobal.getCurrencyName()+")",  //"Amount Due (In Home Currency)",
            dataIndex:'amountdueinbase',
            align:'right',
            pdfwidth:100,
       //     hidden:this.isSummary,
            summaryType:'sum',
            width:150,
            width:140,
            hidecurrency : true,
            summaryRenderer: function(value,m,rec){
                var retVal = WtfGlobal.withoutRateCurrencySymbol(value)
                return '<b>'+retVal+'</b>';
            },
            renderer:WtfGlobal.currencyRenderer
        });
        
    }else{
        this.colModelData.push({
            header:WtfGlobal.getLocaleText("acc.common.total") +" "+ WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur")+ " ("+WtfGlobal.getCurrencyName()+")",  //"Memo",
            //hidden : !this.isSummary,
            align:'right',
            width:150,
            pdfwidth:150,
            dataIndex:"totalinbase",
            pdfrenderer:"rowcurrency",
            summaryType:'sum',
            hidecurrency : true,
            summaryRenderer: function(value,m,rec){
                var retVal = WtfGlobal.withoutRateCurrencySymbol(value)
                return '<b>'+retVal+'</b>';
            },
            renderer:WtfGlobal.currencyRenderer
            });
        }
            this.colModelData.push({
                header:WtfGlobal.getLocaleText("acc.field.Term"),
                dataIndex:'termname',
                sortable: (this.isAgedForSalesPerson||this.isParentChild)?true:false,
                pdfwidth:100,
                groupable: true,
                hidden:(this.isSummary || this.isAgedForSalesPerson), 
                align:'center'
    //            groupRenderer: this.groupDateRender.createDelegate(this)
            });
    this.cm = new Wtf.grid.ColumnModel(this.colModelData);
    this.initialColumnCnt = this.cm.getColumnCount();
     this.groupView = new Wtf.ux.grid.MultiGroupingView({
        forceFit: false,
        showGroupName: false,
        enableNoGroups: false,
        isGrandTotal: false,
        isGroupTotal: false,
        hideGroupedColumn:false,
     //   emptyText: '<div class="emptyGridText">' + WtfGlobal.getLocaleText('account.common.nodatadisplay') + ' <br></div>',
        emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec') + "<br>" +WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn')) ,
        groupTextTpl: '{group} '    
    });
    this.GrandTotalSummary=new Wtf.XTemplate(// to display the grand total Ref ERP-8925
        '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
            
        '<div>',
        '<table width="100%">'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.common.pagedtotalinbase")+':</b></td><td style="width:55%;"><span><b>'+Wtf.util.Format.ellipsis('{pagedTotal}',60)+'</b></span></td>'+                   
        '</tr>'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.common.grandtotalinbase")+':</b></td><td style="width:55%;"><span><b>'+Wtf.util.Format.ellipsis('{grandTotal}',60)+'</b></span></td>'+                   
        '</tr>'+
        '</table>'+
        '</div>',            
                                 
        '</div>'
        );
            
    this.GrandTotalSummaryTPL=new Wtf.Panel({
        id:this.isSummary?'GrandTotalSummaryTPL'+this.id:'GrandTotalReportTPL'+this.id,
        border:false,
        width:'95%',
        baseCls:'tempbackgroundview',
        html:this.GrandTotalSummary.apply({
            pagedTotal:WtfGlobal.currencyRenderer(0), 
            grandTotal:WtfGlobal.currencyRenderer(0)                    
        })
    }); 
    this.GrandTotalReport=new Wtf.XTemplate(// to display the grand total Ref ERP-8925
        '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
            
        '<div>',
        '<table width="100%">'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.common.pagedtotalinbase")+':</b></td><td style="width:55%;"><span><b>'+Wtf.util.Format.ellipsis('{pagedTotal}',60)+'</b></span></td>'+                   
        '</tr>'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.common.grandtotalinbase")+':</b></td><td style="width:55%;"><span><b>'+Wtf.util.Format.ellipsis('{grandTotal}',60)+'</b></span></td>'+                   
        '</tr>'+
        '</table>'+
        '</div>',            
                                 
        '</div>'
        );
            
    this.GrandTotalReportTPL=new Wtf.Panel({
        id:this.isSummary?'GrandTotalSummaryTPL'+this.id:'GrandTotalReportTPL'+this.id,
        border:false,
        width:'95%',
        baseCls:'tempbackgroundview',
        html:this.GrandTotalReport.apply({
            pagedTotal:WtfGlobal.currencyRenderer(0),
            grandTotal:WtfGlobal.currencyRenderer(0)                    
        })
    }); 
            
    this.tbar3 = new Array();
    this.bbar = new Array();// to display the grand total Ref ERP-8925
    this.grid = this.isSummary? !this.isParentChild ? new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.AgedStore,
        cm:this.cm,
        sm: this.sm,
        loadMask : true,
        ctCls : 'agedview' ,
        border:false,
        layout:'fit',
        plugins: [this.expander],
        tbar:this.tbar3,
        view:new Wtf.grid.GridView({
            emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec') + "<br>" +WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn')) ,
            forceFit:false            
        }),
        bbar:this.bbar
    }):new Wtf.grid.HirarchicalGridPanel({
        stripeRows :true,
        store:this.AgedStore,
        loadMask : true,
        cm:this.cm,
        sm: this.sm,
        ctCls : 'agedview' ,
        border:false,
        hirarchyColNumber:8,
        layout:'fit',
        plugins: [this.expander],
        tbar:this.tbar3,
        view:new Wtf.grid.GridView({
            emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec') + "<br>" +WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn')) ,
            forceFit:false            
        }),
        bbar:this.bbar
    }):new Wtf.ux.grid.MultiGroupingGrid({
        stripeRows :true,
        store:this.AgedStore,
        loadMask : true,
        border:false,
        ctCls:'agedsummary',
        view: this.groupView,
        plugins: [this.summary],
        cm:this.cm,
        sm: this.sm,
        tbar:this.tbar3,
        bbar:this.bbar
    });
    
    this.grid.on("render",function(){
        this.grid.getView().applyEmptyText();
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    },this);
    
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        hidden:this.isSummary,
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,       
        hidden: this.isCustomWidgetReport,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    this.customReportViewBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
        scope: this,
        hidden: this.isSummary||this.isCustomWidgetReport,
        // hidden:!(this.isOrder && this.isCustomer),
        tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
        handler: this.customizeView,
        iconCls: 'accountingbase fetch'
    });
    
    var moduleArr = ""+Wtf.Acc_Vendor_Invoice_ModuleId+","+Wtf.Acc_Make_Payment_ModuleId+","+Wtf.Acc_Credit_Note_ModuleId+","+Wtf.Acc_Debit_Note_ModuleId+"";
    if(this.isAgedForSalesPerson){// In aged report based on sales person on sales invoices are coming hence module array contains invoice module id
        moduleArr = ""+Wtf.Acc_Invoice_ModuleId;
    } else if(this.receivable){
        moduleArr=""+Wtf.Acc_Invoice_ModuleId+","+Wtf.Acc_Receive_Payment_ModuleId+","+Wtf.Acc_Credit_Note_ModuleId+","+Wtf.Acc_Debit_Note_ModuleId+"";
    }
                                  
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        advSearch: false,
        moduleidarray: moduleArr.split(','),
        customerCustomFieldFlag:(this.isAgedForSalesPerson || !this.receivable)?false:true,
        vendorCustomFieldFlag :this.receivable?false:true, 
        isAvoidRedundent:true,
        ignoreDefaultFields:true,
        reportid: (this.receivable?(this.isSummary?Wtf.Acc_AgedReceivables_Summary_ModuleId:Wtf.autoNum.AgedReceivableDetailReport):(this.isSummary?Wtf.Acc_AgedPayables_Summary_ModuleId:Wtf.Acc_AgedPayables_ReportView_ModuleId))
    });
    
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
    this.email=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.email"),
        tooltip :WtfGlobal.getLocaleText("acc.MailWin.sendMail"),
        hidden:this.isCustomWidgetReport,
//        id: 'chartRec'+config.helpmodeid,// + this.id,
        scope: this,
        handler:this.sendMail,
        iconCls: "accountingbase financialreport"
    });
    this.SOAButton = new Wtf.Toolbar.Button({
        text : WtfGlobal.getLocaleText("acc.field.SOAExport"),
        iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
        tooltip : WtfGlobal.getLocaleText("acc.field.ExportSOAreportinPDFformat"),
        scope : this,
        disabled:false,
        hidden : !this.receivable||this.reportWithoutAging||this.isCustomWidgetReport,
        handler : this.SOAReport
    });
    this.customizedAgedSummary = new Wtf.Toolbar.Button({
        text : WtfGlobal.getLocaleText("acc.field.CustomizeSummary"),
        iconCls: 'accountingbase agedrecievable',
        tooltip : (this.receivable?WtfGlobal.getLocaleText("acc.summary.aged.Receivables.customized.tooltip"):WtfGlobal.getLocaleText("acc.summary.aged.payables.customized.tooltip")),
        id : 'customizedsummarybutt'+this.id,
        scope : this,
        hidden : !this.isSummary||this.reportWithoutAging||this.isCustomWidgetReport,
        handler : this.getCustomizedSummary
    });
    /*Expand collapse functionality for aged details
    the expandall functionality will expand all rows with new request ,
    and collapseall functionality will collapse all rows.
     */
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function() {
            /*if buttontext is equals to collapse the collapse all rows and vice-versa
             */
            if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
                this.expandButtonClicked = true;
            }
            this.expandCollapseBttnHandler(this.expandCollpseButton.getText());
        }
    });
    
    var sdateSavedSearch;
    var edateSavedSearch;
    if(config.searchJson != undefined && config.searchJson != ""){
        sdateSavedSearch = JSON.parse(config.searchJson).data[0].sdate;
        edateSavedSearch = JSON.parse(config.searchJson).data[0].edate;
    }
    
//    this.startDate=new Wtf.ExDateFieldQtip({
//        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
//        name:'startdate',
//        id: 'stdate'+config.id+config.helpmodeid,
//        format:WtfGlobal.getOnlyDateFormat(),
//      //  readOnly:true,
//        value:this.getDates(true, sdateSavedSearch)
//    });
    
    this.asOfDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.asOf"),  //'As of',
        name:'asofdate',
        id: 'asofdate'+config.id+config.helpmodeid,
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true, //ERP-8882[SJ] 
        value:new Date(Wtf.serverDate.format('M d, Y')+" 12:00:00 AM")
    });
    
    this.curDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"), //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        id: 'dueDate'+config.helpmodeid,
        value:((edateSavedSearch == undefined)?new Date(Wtf.serverDate.format('M d, Y')+" 12:00:00 AM"):new Date(edateSavedSearch)) 
    });
//    this.startDate.on("change",this.checkDates,this);
    this.asOfDate.on("change",this.checkDates,this);
    this.curDate.on("change",this.checkDates,this);

    var report_header=(this.isParentChild? WtfGlobal.getLocaleText("acc.wtfTrans.agedprparentchildreport")+"_v1":WtfGlobal.getLocaleText("acc.wtfTrans.agedpr")+"_v1");
    this.expButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.agedPay.exportTT"),  //'Export report details',
        hidden:this.isCustomWidgetReport,
        disabled :true,        
        //filename: this.isAgedForSalesPerson? WtfGlobal.getLocaleText("acc.agedbysalesperson.title")+"_v1" : (this.receivable? WtfGlobal.getLocaleText("acc.wtfTrans.agedrr")+"_v1" : WtfGlobal.getLocaleText("acc.wtfTrans.agedpr")+"_v1"),        
        filename: this.isAgedForSalesPerson? WtfGlobal.getLocaleText("acc.agedbysalesperson.title")+"_v1" : (this.receivable? WtfGlobal.getLocaleText("acc.wtfTrans.agedrr")+"_v1" :report_header),
        params:{
//            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            duration:this.interval.getValue(),
            noOfInterval:this.noOfIntervalCombo.getValue(),
            enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
            accountid:this.accountID||config.accountID,
            curdate: WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
            asofdate : WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()), 
            isdistributive:this.typeEditor.getValue(),
            agedDetailsFlag :this.isAgedForSalesPerson?false:true,
	    isExportReport : true,		//SDP-7909
            datefilter : this.dateFilter.getValue(),
                 isAged : true,
                 checkforex:true,
                 fieldid:this.columnCombo.getValue(),
                 moduleIDForFetchingGroupingData:this.moduleIDForFetchingGroupingData,
                 fieldIDsListForFetchingGroupingData:this.fieldIDsListForFetchingGroupingData,
                 listOfModuleIDForFetchingGroupingData:this.listOfModuleIDForFetchingGroupingData
        },
        menuItem:{
            csv:true,
            pdf:false,
            rowPdf:false,
            CRLetter:true,
            xls:true,
            subMenu:true,
            detailedXls: ((this.isSummary && !this.isAgedForSalesPerson)?true:false)
            },
        get:this.isSummary?this.expSummGet:this.expGet,
        isProductExport:false
    });
    
    this.expButton.on("click", function () {
        this.expButton.extra ="";
    }, this);
    
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.agedPay.printTT"),  //'Print report details',
        hidden:this.isCustomWidgetReport,
        disabled :true,
        filename:this.isAgedForSalesPerson?WtfGlobal.getLocaleText("acc.agedbysalesperson.title"):(this.receivable?WtfGlobal.getLocaleText("acc.wtfTrans.agedrr"): WtfGlobal.getLocaleText("acc.wtfTrans.agedpr")), 
        params:{
//            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                 duration:this.interval.getValue(),
                 noOfInterval:this.noOfIntervalCombo.getValue(),
                 enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                 accountid:this.accountID||config.accountID,
                 curdate: WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                 asofdate : WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()), 
                 isdistributive:this.typeEditor.getValue(),
                 datefilter : this.dateFilter.getValue(),
                 name: this.receivable?WtfGlobal.getLocaleText("acc.wtfTrans.agedr"):WtfGlobal.getLocaleText("acc.wtfTrans.agedp"),
                 isAged : true,
                 checkforex:true,
                 fieldid:this.columnCombo.getValue(),
                 moduleIDForFetchingGroupingData:this.moduleIDForFetchingGroupingData,
                 fieldIDsListForFetchingGroupingData:this.fieldIDsListForFetchingGroupingData,
                 listOfModuleIDForFetchingGroupingData:this.listOfModuleIDForFetchingGroupingData
        },
        lable: this.receivable?WtfGlobal.getLocaleText("acc.wtfTrans.agedr"): WtfGlobal.getLocaleText("acc.wtfTrans.agedp"),
        menuItem:{print:true},
        get:this.isSummary?this.expSummGet:this.expGet
    })
   
    this.personRec="";
    if(this.isAgedForSalesPerson){
        this.personRec = new Wtf.data.Record.create([
        {
            name: 'accid',
            mapping:'id'
        }, {
            name: 'accname',
            mapping:'name'
        }, {
            name: 'acccode'
        }
        ]);
    } else {
        this.personRec = new Wtf.data.Record.create([
        {
            name: 'accid'
        }, {
            name: 'accname'
        }, {
            name: 'acccode'
        }
        ]);    
    }
   
    var nameUrl="ACCVendor/getVendorsForCombo.do";
    var baseParamArray={
            deleted:false,
            nondeleted:true,
            combineData:-1  //Send For Seprate Request
        };
        
    if(this.isAgedForSalesPerson){
        nameUrl="ACCMaster/getMasterItems.do";
        baseParamArray={
           mode:112,
           groupid:15
        };
    } else if(this.receivable){
        nameUrl="ACCCustomer/getCustomersForCombo.do";
    } 
    this.customerAccStore =  new Wtf.data.Store({   //Customer/vendor multi selection Combo
        url:nameUrl,
        baseParams:baseParamArray,
        reader: new  Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "totalCount"
            //autoLoad:false
        },this.personRec)
    });

    this.customerAccStore.on("load", function(store){
        var storeNewRecord=new this.personRec({
            accname:'All',
            accid:'All',
            acccode:''
        });
        /**
         * removed all condition from aging report.
         */
//        this.Name.store.insert( 0,storeNewRecord);
//        if ((this.Name.getValue() == undefined || this.Name.getValue() == "")) {
//            this.Name.setValue("All");
//        }
//        else{
//            this.Name.setValue(this.custVendorID); 
//        }
        WtfGlobal.resetAjaxTimeOut();
    },this);
    this.customerAccStore.on("beforeload", function(store){
        WtfGlobal.setAjaxTimeOut();    // Function which set time out for 900000 milliseconds i.e. 15 minutes
    },this);
    if (!(this.moduleid == Wtf.Acc_AgedReceivables_Summary_ModuleId || this.moduleid == Wtf.Acc_AgedPayables_Summary_ModuleId || this.moduleid == Wtf.Acc_AgedReceivables_ReportView_ModuleId || this.moduleid == Wtf.Acc_AgedPayables_ReportView_ModuleId)) {
           this.customerAccStore.load();
    }
   
    this.CustomerComboconfig = {
        //hiddenName:this.businessPerson.toLowerCase(),
        store: this.customerAccStore,
        valueField:'accid',
        hideLabel:true,
        //hidden : iscustomer,
        displayField:'accname',
//        emptyText:this.isAgedForSalesPerson?WtfGlobal.getLocaleText("acc.agedPay.customerSelect"):this.receivable?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") ,
        mode: 'remote',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };
    if (this.isAgedForSalesPerson) {
        this.Name = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.agedPay.searchcus") + '*',
            forceSelection: true,
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 250,
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            width: 240,
        }, this.CustomerComboconfig));
    } else {
        this.Name = new Wtf.common.SelectPaging(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.agedPay.searchcus") + '*',
            forceSelection: true,
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 250,
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            width: 240,
            pageSize: Wtf.ProductCombopageSize,
        }, this.CustomerComboconfig));
    }
    var labelforcustvencombo = this.isAgedForSalesPerson?WtfGlobal.getLocaleText("acc.agedPay.customerSelect"):this.receivable?WtfGlobal.getLocaleText("acc.field.SelectCustomer"):WtfGlobal.getLocaleText("acc.field.SelectVendor");
    this.Name.on('select',function(combo,personRec){
        /*on selection of customer/vendor enabling includeExcludeChildCmb combobox*/
        this.includeExcludeChildCmb.enable();
        this.includeExcludeChildCmb.clearValue();
        this.includeExcludeChildCmb.setValue(false);
        if(personRec.get('accid')=='All'){
            combo.clearValue();
            combo.setValue('All');
        }else if(combo.getValue().indexOf('All')>=0){
            combo.clearValue();
            combo.setValue(personRec.get('accid'));
        }
    } , this);   
    var btnArr=[];
    var bottombtnArr=[];
    this.tbar3.push(                //ERP-10240
       this.quickPanelSearch = new Wtf.KWLTagSearch({
//            emptyText:this.receivable?WtfGlobal.getLocaleText("acc.agedPay.searchcus"):WtfGlobal.getLocaleText("acc.agedPay.searchven"),  //'Search by Person Name',
            emptyText:WtfGlobal.getLocaleText("acc.agedPay.search"), 
            id:"quickSearch"+config.helpmodeid,
            width: 180,
            hidden:this.isSummary,
            field: 'personname'
        }),
        this.resetBttn); 
        btnArr.push(labelforcustvencombo,this.Name,"-");
        /*
         includeExcludeChildCmb combobox after Customer/Vendor combobox
         */
//        if(this.receivable){
           btnArr.push(this.includeExcludeChildCmb,"-"); //ERP-10240
//        }
        btnArr.push(
//            WtfGlobal.getLocaleText("acc.common.from"),this.startDate,"-",
            WtfGlobal.getLocaleText("acc.common.asOf"),this.asOfDate,"-",
            WtfGlobal.getLocaleText("acc.common.to"),this.curDate,
            WtfGlobal.getLocaleText("acc.field.AgedOn"),this.dateFilter);
            btnArr.push(this.AdvanceSearchBtn);
                   
        this.tbar3.push('-',WtfGlobal.getLocaleText("acc.agedPay.interval"), this.interval, '-', WtfGlobal.getLocaleText("acc.aged.NoOfIntervals"), this.noOfIntervalCombo,'-');          
        if (this.isSummary) {
            this.tbar3.push(this.typeEditor);
        } 
        if (this.receivable && !this.isAgedForSalesPerson) {
                if(!this.isCustomWidgetReport){ 
                this.tbar3.push(WtfGlobal.getLocaleText("acc.common.Groupby"), this.columnCombo);
                }
        }
   
    if(this.isSummary||this.isCustomWidgetReport){                             // ERP-10240
        this.tbar3.push(
            {
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',
                iconCls:'accountingbase fetch',
                scope:this,
                tooltip:this.receivable?(this.isAgedForSalesPerson?WtfGlobal.getLocaleText("acc.agedbysalesperson.title"):WtfGlobal.getLocaleText("acc.agedReceive.view")):WtfGlobal.getLocaleText("acc.agedPay.view"),  //"Select a date to view Aged Receivable.":"Select a date to view Aged Payable.",
                handler:this.fetchAgedData
            });          
            /*code to push a button on tbar3
             */
            if (!this.isAgedForSalesPerson) {
                this.tbar3.push(this.expandCollpseButton);
            }
            }
        else{
            btnArr.push(
            {
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',
                iconCls:'accountingbase fetch',
                scope:this,
                tooltip:this.receivable?(this.isAgedForSalesPerson?WtfGlobal.getLocaleText("acc.agedbysalesperson.title"):WtfGlobal.getLocaleText("acc.agedReceive.view")):WtfGlobal.getLocaleText("acc.agedPay.view"),  //"Select a date to view Aged Receivable.":"Select a date to view Aged Payable.",
                handler:this.fetchAgedData
            });
        /*
         * Pushed ExpandCollapse Button for ReportView. 
         */
        btnArr.push(this.expandCollpseButton );
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
    }
         
        this.ReportWithoutAging = new Wtf.Toolbar.Button({
        text : this.receivable?WtfGlobal.getLocaleText("acc.agedPay.totalagedReceivable"):WtfGlobal.getLocaleText("acc.agedPay.totalagedPayable"),
        iconCls: 'accountingbase agedrecievable',
        tooltip : (this.receivable?WtfGlobal.getLocaleText("acc.agedPay.totalagedReceivable.tooltip"):WtfGlobal.getLocaleText("acc.agedPay.totalagedPayable.tooltip")),
        id : 'agingreportbutt'+this.id,
        scope : this,
        hidden : !this.isSummary ||this.reportWithoutAging||this.isCustomWidgetReport,
        handler : this.getReportWithoutAging
    });
            this.singleRowPrint=new Wtf.exportButton({
            obj:this,
            id:"printSingleRecord"+config.helpmodeid+config.id,
            iconCls: 'pwnd printButtonIcon',
            disabled: true,
            hidden:this.isCustomWidgetReport, 
            text: WtfGlobal.getLocaleText("acc.rem.236"),
            params:{
//                stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                customerIds:this.Name.getValue(),
                interval:this.interval.getValue(),
                noOfInterval:this.noOfIntervalCombo.getValue(),
                statementOfAccountsFlag:true,
                isCustomerSales:true,
                name: "Customer_Account_Statement",
                custVendorID:this.Name.getValue(),
                datefilter:this.dateFilter.getValue(),
                ignorezero:true,
                isdistributive:true,
                isAged:true,
                creditonly:true,
                mode:18,
                nondeleted:true,
                withinventory:true,
                reportWithoutAging:false,
//                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                curdate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                moduleid:this.receivable?Wtf.Acc_Customer_AccountStatement_ModuleId:Wtf.Acc_Vendor_AccountStatement_ModuleId,
                asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
                invoiceAmountDueFilter:false, // for outstanding Invoices only
                duration:this.interval.getValue()
            },
            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
            //hidden:this.isRequisition || this.isSalesCommissionStmt,
            menuItem:{
                rowPrint:true
                },
            //         get:tranType,
            moduleid:this.receivable?Wtf.Acc_Customer_AccountStatement_ModuleId:Wtf.Acc_Vendor_AccountStatement_ModuleId
        });
        
        this.chartButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.Chart"),
            iconCls: "accountingbase chart",
            scope: this,
            hidden: this.isCustomWidgetReport,
            menu: [
                {
                    xtype:"button",
                    text: (this.receivable) ? WtfGlobal.getLocaleText("acc.aged.ChartButton.MonthwiseAgedReceivable") : WtfGlobal.getLocaleText("acc.aged.ChartButton.MonthwiseAgedPayable"),
                    menu: [
                            {
                                xtype: "button",
                                text: WtfGlobal.getLocaleText("acc.common.BarChart"),
                                iconCls: "x-tool-barchartwizard",
                                scope: this,
                                handler: function () {
                                    var params = {}
                                    params.chartType = Wtf.chartType.bar;
                                    params.showType = "month";
                                    this.showChart(params);
                                }
                            },
                            {
                                xtype: "button",
                                text: WtfGlobal.getLocaleText("acc.common.PieChart"),
                                iconCls: "x-tool-piechartwizard",
                                scope: this,
                                handler: function () {
                                    var params = {}
                                    params.chartType = Wtf.chartType.pie;
                                    params.showType = "month";
                                    this.showChart(params)
                                }
                            }
                        ]
                },
                {
                    xtype:"button",
                    text: (this.receivable) ? WtfGlobal.getLocaleText("acc.aged.ChartButton.PeriodwiseAgedReceivable") : WtfGlobal.getLocaleText("acc.aged.ChartButton.PeriodwiseAgedPayable"),
                    menu: [
                            {
                                xtype: "button",
                                text: WtfGlobal.getLocaleText("acc.common.BarChart"),
                                iconCls: "x-tool-barchartwizard",
                                scope: this,
                                handler: function () {
                                    var params = {}
                                    params.chartType = Wtf.chartType.bar;
                                    params.showType = "period";
                                    this.showChart(params);
                                }
                            },
                            {
                                xtype: "button",
                                text: WtfGlobal.getLocaleText("acc.common.PieChart"),
                                iconCls: "x-tool-piechartwizard",
                                scope: this,
                                handler: function () {
                                    var params = {}
                                    params.chartType = Wtf.chartType.pie;
                                    params.showType = "period";
                                    this.showChart(params)
                                }
                            }
                        ]
                }
            ]
        });
        if(!(this.isSummary ||this.isAgedForSalesPerson)) {
            this.tbar3.push(this.email);  
        }

        if(this.isSummary){// to display the grand total Ref ERP-8925
            this.bbar.push("->",this.GrandTotalSummaryTPL);  
        }else{
            this.bbar.push("->",this.GrandTotalReportTPL);  
        }   
//           this.bbar.push("->",this.GrandTotalSummaryTPL);  
       
        if(!this.reportWithoutAging && !this.isAgedForSalesPerson){
           if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
            this.tbar3.push(this.SOAButton);
           }
//            this.tbar3.push(this.customizedAgedSummary);
        }
        //Button singleRowPrint is  only needs in summary view of AP,AR. It is not needed in Total Aged and Aged Report basd on sales person. So the below check
        if(!this.reportWithoutAging && !this.isAgedForSalesPerson && this.isSummary){
            this.tbar3.push(this.singleRowPrint);
        }
        if (this.isSummary) {
            this.tbar3.push(this.chartButton);
        }
        if(this.isSummary && !this.reportWithoutAging && !this.isAgedForSalesPerson){
            bottombtnArr.push('-', this.ReportWithoutAging);      
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
            bottombtnArr.push('-', this.expButton);
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
            bottombtnArr.push('-', this.printButton);
        }
        this.tbar3.push(this.customReportViewBtn);
        if (!this.isAgedForSalesPerson && !this.isSummary) {
           this.tbar3.push('-',this.groupCombo);
        }
        if(this.displayFlag && !this.totalaged){
        bottombtnArr.push('-');
        bottombtnArr.push(
        {
            xtype:'button',
            text:WtfGlobal.getLocaleText("acc.common.FetchAllRecords"),  //'Fetch',
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.common.FetchAllRecords"),
            handler:this.fetchAllAgedData
        });
    }
    this.resetBttn.on('click',this.handleResetClick,this);
    if(config.helpmodeid!=null){
        btnArr.push("->");
        btnArr.push(getHelpButton(this,config.helpmodeid));
    }
    
    if(this.displayFlag){
        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.AgedStore,
            searchField: this.quickPanelSearch,
            afterPageText:'',
            //displayInfo: true,
            //            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.agedPay.norec"),  //"No results to display",
            plugins: this.pP = new Wtf.pPageSizeForAllOption({
                id : "pPageSize_"+this.id
            }),
            items:bottombtnArr
        })
    }else{
        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.AgedStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            //            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.agedPay.norec"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            }),
            items:bottombtnArr
        })
    }
    
  
    this.leadpan = new Wtf.Panel({
        border:false,
        layout : "border",
        items:[this.objsearchComponent
        , {
            region: 'center',
            layout: 'fit',
            border: false,
            tbar:btnArr,
            items: [this.grid],
            bbar: this.pagingToolbar
                    }]
    });
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });
    
    Wtf.account.AgedDetail.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });
    this.loadMask = new Wtf.LoadMask(document.body, {
        msg: WtfGlobal.getLocaleText("acc.msgbox.50")
    });
    this.AgedStore.on("beforeload", function(s,o) {
        WtfGlobal.setAjaxTimeOut();    // Function which set time out for 900000 milliseconds i.e. 15 minutes
        this.getStoreBaseParams();
//******************following block is used only when you perform grouping on dimnesions through group by combo************************
//        o.params.custVendorID=this.Name.getValue();
        if(this.displayFlag){
            if(s.getCount()>0){
                var firstRec = s.getAt(0);
                var lastRec = s.getAt(s.getCount()-1);
                o.params.fCustomerId=firstRec.data.fCustomerId;
                o.params.lastcustomerid=lastRec.data.customerId;
                o.params.custVendorID=this.Name.getValue();
            }else{
                o.params.firstcustomerid="";
                o.params.lastcustomerid="";
            }
            if(s.getCount()>0){
                var pStart = s.getAt(s.getCount()-1);
                this.prStart=pStart.data.start;
                o.params.previousStart=pStart.data.start;
            }else{
                o.params.previousStart=0;
            }
        }
        s.baseParams.isdistributive=this.typeEditor.getValue();
        s.baseParams.includeExcludeChildCmb=this.includeExcludeChildCmb.getValue();
        s.baseParams.datefilter=this.dateFilter.getValue();
        s.baseParams.custVendorID=(this.Name.getValue()!="")?this.Name.getValue():this.custVendorID;
        if(this.pP.combo!=undefined){
            if(this.pP.combo.value=="All" && this.Name.lastSelectionText==""){
                var count = this.AgedStore.getTotalCount();
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                o.params.limit = count;
            }
        }
        if (!this.isAgedForSalesPerson && !this.isSummary) {
           s.baseParams.groupcombo=this.groupCombo.getValue();
        }
    },this);
    this.AgedStore.on('load',this.storeloaded,this);
    
//    WtfGlobal.setAjaxTimeOutFor30Minutes();
//    
//    this.loadMask.show();
//    this.AgedStore.load({
//        params:{
//            start:0,
//            duration:this.interval.getValue(),
//            isdistributive:this.typeEditor.getValue(),
//            datefilter : this.dateFilter.getValue(),
//            //custVendorID:(this.custVendorID==undefined || this.custVendorID=="")?this.Name.getValue():this.custVendorID,
//            startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),            
//            asofdate : WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()), 
//            enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
//            limit:30,
//            creditonly:true,
//            isAged:true
//        }
//    });
    this.getMyConfig(); 
    this.grid.on('gridconfigloaded', function (grid) {
        var salespersonname = this.grid.getColumnModel().findColumnIndex("salespersonname");
        if (salespersonname != -1 && this.isAgedForSalesPerson && this.isSummary) {
            this.grid.getColumnModel().setHidden(salespersonname, false);
        }
    }, this);
    
    this.AgedStore.on('datachanged', function() {
        var p = 30;
        this.quickPanelSearch.setPage(p);
    }, this);
    
    this.on('activate',function(){
         if(this.Name!=undefined){
             this.doLayout();
             this.Name.syncSize();
             this.Name.setWidth(240);
         }
    },this);
    this.grid.on('cellclick',this.onCellClick, this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on('expand',this.onRowexpand,this);
       }
       
Wtf.extend( Wtf.account.AgedDetail,Wtf.Panel,{ 
    onRender: function(config) {
        if (!this.isSummary ) {
            if(this.isAgedForSalesPerson){
                this.appendGridColumn(Wtf.autoNum.agedDetailBasedOnSalesPerson);
            } else {
                this.appendGridColumn(Wtf.autoNum.AgedReceivableDetailReport);
            }
        }
        Wtf.account.AgedDetail.superclass.onRender.call(this, config);
    },
    customizeView: function() {
        var modules="";
        if(this.isAgedForSalesPerson){
            modules='' + Wtf.Acc_Invoice_ModuleId;
        } else {
            modules='' + Wtf.Acc_Invoice_ModuleId + ',' + Wtf.Acc_Credit_Note_ModuleId + ',' + Wtf.Acc_Receive_Payment_ModuleId + ',' + Wtf.Acc_Debit_Note_ModuleId;
        }
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: this.isAgedForSalesPerson?Wtf.autoNum.agedDetailBasedOnSalesPerson:Wtf.autoNum.AgedReceivableDetailReport,
            modules: modules
        });
        this.customizeViewWin.show();
        var arr = this.arr;
    },
    appendGridColumn: function(reportId) {
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getAgedCustomFieldsToShow.do",
            params: {
                reportId: reportId
            }
        }, this, function(request, response) {
            var customProductField = request.data;
            this.updateStoreConfig(customProductField);
            this.cm = new Wtf.grid.ColumnModel(this.colModelData);
            var config = this.cm.config.slice(0, this.initialColumnCnt);
            if (customProductField && customProductField.length > 0) {
                for (var ccnt = 0; ccnt < customProductField.length; ccnt++) {
                    config.push({
                        header: customProductField[ccnt].columnname,
                        dataIndex: customProductField[ccnt].dataindex,
                        width: 100,
                        pdfwidth: 50,
                        align: 'center'
                    })
                }
            }
            /**
             * Initializing new reader causes unintialized jsonData from response while opening from advanced Search. ERP-33745
             */
            //this.AgedStore.reader = new Wtf.data.KwlJsonReader(this.AgedStore.reader.meta, this.AgedStore.fields.items);
            this.grid.getColumnModel().setConfig(config);
            var newcm = this.grid.getColumnModel();
            this.grid.reconfigure(this.AgedStore, newcm);
//            this.AgedStore.load({
//                params: {
//                    start: 0,
//                    duration: this.interval.getValue(),
//                    isdistributive: this.typeEditor.getValue(),
//                    datefilter: this.dateFilter.getValue(),
//                    startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
//                    asofdate: WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
//                    enddate: WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
//                    limit: 30,
//                    creditonly: true,
//                    isAged: true
//                }
//            });
            this.grid.getView().refresh(true);

        });
    },
    updateStoreConfig: function(customProductField) {
        for (var cnt = 0; cnt < customProductField.length; cnt++) {
            var fieldname = customProductField[cnt].dataindex;
            var newField = new Wtf.data.Field({
                name: fieldname
            });
            this.AgedStore.fields.items.push(newField);
            this.AgedStore.fields.map[fieldname] = newField;
            this.AgedStore.fields.keys.push(fieldname);
            /**
             * Customized view requires updating field's length to reflect response data.
             */
            this.AgedStore.fields.length++;
        }
        /**
         * Initializing new reader causes unintialized jsonData from response while opening from advanced Search. ERP-33745.
         */
        //this.AgedStore.reader = new Wtf.data.KwlJsonReader(this.AgedStore.reader.meta, this.AgedStore.fields.items);
    },
    sumBaseAmount:function(dataindex,v,m,rec){       
        if(!this.isSummary){
            v=rec.data[dataindex];
            return WtfGlobal.withoutRateCurrencySymbol(v,m,rec)
        }
        return "";
    }, 
    groupByOnChange: function (combo,newval,oldval) {
  
        if (this.receivable && !this.isSummary) {//detailed view 

            if (this.columnCombo.getValue() == Wtf.ExportMolueName.customer) {
                this.grid.getStore().groupBy(['personinfo', 'type'],false);
                this.grid.getColumnModel().setHidden(3, true);//Dimension
                 this.grid.getColumnModel().setHidden(4, false)//showing journal entry column when grouping is performed on customer in detail view

                this.GrandTotalReportTPL.show();//show grant total in detailed report view when group is performed on dimensions
            } else {
                this.grid.getStore().groupBy(['dimensionvalue', 'type'],false);//dynamically change grouping field
                var fieldId = this.columnCombo.getValue();
                if (fieldId != undefined && fieldId != '') {
                    var rec = WtfGlobal.searchRecord(this.columnCombo.store, fieldId, 'fieldid');
                    if (rec != undefined) {
                        if (rec.data.moduleid != Wtf.ExportMolueName.customer) {
                            this.grid.getColumnModel().setColumnHeader(3, rec.data.header);
                            this.grid.getColumnModel().setHidden(3, false);//Dimension
                            this.grid.getColumnModel().setHidden(4, true); //for the time being hiding journal entry column wheen grouping is performd on dimension
                        }
                    }
                }
                this.GrandTotalReportTPL.hide();//hide grant total in detailed report view when group is performed on dimensions
            }
            if (this.groupCombo.getValue() == 1) {                                         //IF ALREADY SORTED BY DATE AND GROUP BY DIMENSION
                this.grid.getStore().groupBy(['dimensionvalue'],false);
            }
        } else {//summary view 

            if (this.columnCombo.getValue() == Wtf.ExportMolueName.customer) {
                this.grid.getColumnModel().setHidden(3, true);//Dimension

                this.grid.getColumnModel().setHidden(2, false);//Expander
                this.grid.getColumnModel().setHidden(8, false);//customer /Acccount name
                this.grid.getColumnModel().setHidden(9, false);// Alias name

                this.GrandTotalSummaryTPL.show();//hide grant total in summary view when group is performed on dimensions
            } else {
                var fieldId = this.columnCombo.getValue();
                if (fieldId != undefined && fieldId != '') {
                    var rec = WtfGlobal.searchRecord(this.columnCombo.store, fieldId, 'fieldid');
                    if (rec != undefined) {
                        if (rec.data.moduleid != Wtf.ExportMolueName.customer) {
                            this.grid.getColumnModel().setColumnHeader(3, rec.data.header);
                            this.grid.getColumnModel().setHidden(3, false);//Dimension

                            this.grid.getColumnModel().setHidden(2, true);//Expander
                            this.grid.getColumnModel().setHidden(8, true);//customer /Acccount name
                            this.grid.getColumnModel().setHidden(9, true);// Alias name
//                        this.grid.getColumnModel().setHidden(12, true);
                        }
                    }
                }
                this.GrandTotalSummaryTPL.hide();
            }
        }
        this.fetchAgedData();

    },
    groupDateRender:function(v){
       return v.format(WtfGlobal.getOnlyDateFormat())
    },
    totalRender:function(v,m,rec){
        var val=WtfGlobal.withoutRateCurrencySymbol(v,m,rec);
       return "<b>"+val+"</b>"
    },
    
    SOAReport: function() {
//        if(this.grid.getSelectionModel().hasSelection()==false){
//            WtfComMsgBox(15,2);
//            return;
//        }
        var storeData = this.grid.getStore();  //SDP-170
        var personDetails=new Array();
        storeData.each(function(rec) {
            if(personDetails.indexOf(rec.data.personid)==-1){
                    personDetails.push(rec.data.personid);
                }
            }, this);
//        Wtf.get('downloadframe').dom.src  = "ACCInvoiceCMN/exportSOA.do?accid="+personDetails+"&startdate="+WtfGlobal.convertToGenericStartDate(this.startDate.getValue())
        Wtf.get('downloadframe').dom.src  = "ACCInvoiceCMN/exportSOA.do?accid="+personDetails
        +"&duration="+this.interval.getValue()
        +"&enddate="+WtfGlobal.convertToGenericEndDate(this.curDate.getValue())
        +"&curdate="+WtfGlobal.convertToGenericEndDate(this.curDate.getValue())
        +"&asofdate="+WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue())
        +"&isdistributive="+this.typeEditor.getValue()
        +"&datefilter="+this.dateFilter.getValue()
        +"&isAged="+true
//        +"&withinventory="+this.withinventory
        +"&ignorezero="+true
        +"&nondeleted="+true
//        +"&creditonly="+true
    },
    
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.AgedStore.load({
                params: {
                    start:0,
                    duration:this.interval.getValue(),
                    noOfInterval:this.noOfIntervalCombo.getValue(),
                    isdistributive:this.typeEditor.getValue(),
                    datefilter : this.dateFilter.getValue(),
                    limit:this.pP.combo.value,
                    aged:true,
                    creditonly:true,
                    isAged:true,
//                    startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                    enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                    asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue())
                }
            });
        }
    },

    showChart : function(param) {
        var chartPanelID;
        if (param.showType == "month") {
            var title;
            var tabTipParams = [];
            if (this.receivable) {
                chartPanelID = (param.chartType == Wtf.chartType.bar) ? "MonthwiseReceivableBarChart" : "MonthwiseReceivablePieChart";
                title = (param.chartType == Wtf.chartType.bar) ? WtfGlobal.getLocaleText("acc.common.AgedReceivable.BarChart") : WtfGlobal.getLocaleText("acc.common.AgedReceivable.PieChart");
                tabTipParams.push(WtfGlobal.getLocaleText("acc.common.TT.AgedReceivable"));
            } else {
                chartPanelID = (param.chartType == Wtf.chartType.bar) ? "MonthwisePayableBarChart" : "MonthwisePayablePieChart";
                title = (param.chartType == Wtf.chartType.bar) ? WtfGlobal.getLocaleText("acc.common.AgedPayable.BarChart") : WtfGlobal.getLocaleText("acc.common.AgedPayable.PieChart");
                tabTipParams.push(WtfGlobal.getLocaleText("acc.common.TT.AgedPayable"));
            }
            var chartParams = (param.chartType == Wtf.chartType.bar) ? this.getBarChartParams(param) : this.getPieChartParams(param);
            chartParams.id = chartPanelID;
            chartParams.title = title;
            chartParams.url = (param.chartType == Wtf.chartType.bar) ? this.getBarChartUrl(param) : this.getPieChartUrl(param);
            chartParams.chartConfig = (param.chartType == Wtf.chartType.bar) ? this.getBarChartConfig(chartParams, param) : this.getPieChartConfig(chartParams);
            chartParams.tabTipParams = tabTipParams;
        } else if(param.showType == "period") {
            var title;
            var tabTipParams = [];
            if (this.receivable) {
                chartPanelID = (param.chartType == Wtf.chartType.bar) ? "PeriodwiseReceivableBarChart" : "PeriodwiseReceivablePieChart";
                title = (param.chartType == Wtf.chartType.bar) ? WtfGlobal.getLocaleText("acc.common.AgedReceivable.BarChart") : WtfGlobal.getLocaleText("acc.common.AgedReceivable.PieChart");
                tabTipParams.push(WtfGlobal.getLocaleText("acc.common.TT.AgedReceivable"));
            } else {
                chartPanelID = (param.chartType == Wtf.chartType.bar) ? "PeriodwisePayableBarChart" : "PeriodwisePayablePieChart";
                title = (param.chartType == Wtf.chartType.bar) ? WtfGlobal.getLocaleText("acc.common.AgedPayable.BarChart") : WtfGlobal.getLocaleText("acc.common.AgedPayable.PieChart");
                tabTipParams.push(WtfGlobal.getLocaleText("acc.common.TT.AgedPayable"));
            }
            var chartParams = (param.chartType == Wtf.chartType.bar) ? this.getBarChartParams(param) : this.getPieChartParams(param);
            chartParams.id = chartPanelID;
            chartParams.title = title;
            chartParams.url = (param.chartType == Wtf.chartType.bar) ? this.getBarChartUrl(param) : this.getPieChartUrl(param);
            chartParams.chartConfig = (param.chartType == Wtf.chartType.bar) ? this.getBarChartConfig(chartParams, param) : this.getPieChartConfig(chartParams);
            chartParams.tabTipParams = tabTipParams;
        }
        
        var chart = Wtf.getCmp(chartPanelID);
        if (chart) {
            (this.receivable) ? Wtf.getCmp('mainAgedRecievable').remove(chart, true) : Wtf.getCmp('mainAgedPayable').remove(chart, true);
        }
        chart = getReportChartPanel(chartParams);
        (this.receivable) ? Wtf.getCmp('mainAgedRecievable').add(chart) : Wtf.getCmp('mainAgedPayable').add(chart);
        (this.receivable) ? Wtf.getCmp('mainAgedRecievable').setActiveTab(chart) : Wtf.getCmp('mainAgedPayable').setActiveTab(chart);
        (this.receivable) ? Wtf.getCmp('mainAgedRecievable').doLayout() : Wtf.getCmp('mainAgedPayable').doLayout();
    },
    
    getStoreBaseParams: function (param, s) {
        var currentBaseParams = this.AgedStore.baseParams;
        
        currentBaseParams.curdate = WtfGlobal.convertToGenericEndDate(this.curDate.getValue());
        currentBaseParams.duration = this.interval.getValue();
        currentBaseParams.noOfInterval=this.noOfIntervalCombo.getValue();
//        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.asofdate = WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.curDate.getValue());
        currentBaseParams.isParentChild = this.isParentChild;
        currentBaseParams.chartType = "";
        
        //******************following block is used only when you perform grouping on dimnesions through group by combo************************
        if (this.columnCombo != undefined) {
            currentBaseParams.fieldid = this.columnCombo.getValue();
            var fieldId = this.columnCombo.getValue();
            if (fieldId != undefined && fieldId != '') {
                var rec = WtfGlobal.searchRecord(this.columnCombo.store, fieldId, 'fieldid');
                if (rec != undefined) {
                    if (rec.data.moduleid == Wtf.ExportMolueName.customer) {
                        this.moduleIDForFetchingGroupingData = 0;//for by default grouping on customers passing module as '0'
                    } else {
                        this.moduleIDForFetchingGroupingData = rec.data.moduleid;//for performing grouping on dimension of any module.

                        this.listOfModuleIDForFetchingGroupingData = rec.data.moduleidlist;
                        this.fieldIDsListForFetchingGroupingData = rec.data.fieldidlist;
                    }
                    currentBaseParams.moduleIDForFetchingGroupingData = this.moduleIDForFetchingGroupingData;

                    currentBaseParams.listOfModuleIDForFetchingGroupingData = this.listOfModuleIDForFetchingGroupingData; //comma seperated moduleids
                    currentBaseParams.fieldIDsListForFetchingGroupingData = this.fieldIDsListForFetchingGroupingData; // comma seperated fieldids

                }
            }
        }
        return currentBaseParams;
    },
    getBarChartParams: function (param) {
        var chartParams = {
            params: {}
        }
        
        chartParams.params = this.getStoreBaseParams(param);
        chartParams.params.persongroup = false;
        chartParams.params.personlimit = 1;
        chartParams.params.chartType = Wtf.chartType.bar;
        
        if (param.showType == "period") {
            chartParams.params.columnHeaders = this.getColumnHeaderforChart();
        }
        return chartParams;
    },
    getBarChartUrl: function (param) {
        var url;
        if(param.showType == "period") {
            url = (this.receivable) ? "ACCInvoiceCMN/getCustomerAgedReceivable.do" : "ACCGoodsReceiptCMN/getVendorAgedPayable.do";
        } else {
            url = (this.receivable) ? "ACCInvoiceCMN/getAgedReceivableBarChart.do":"ACCGoodsReceiptCMN/getAccountPayableBarChart.do";
        }
        return url;
    },
    
    getPieChartParams: function (param) {
        var chartParams = {
            params: {}
        }
        chartParams.params = this.getStoreBaseParams(param);
        chartParams.params.persongroup = false;
        chartParams.params.personlimit = 1;
        chartParams.params.chartType = Wtf.chartType.pie;
        return chartParams;
    },
    getPieChartUrl: function (param) {
        var url;
        if(param.showType == "period") {
            url = (this.receivable) ? "ACCInvoiceCMN/getCustomerAgedReceivable.do" : "ACCGoodsReceiptCMN/getVendorAgedPayable.do";
        } else {
            url = (this.receivable) ? "ACCInvoiceCMN/getAgedReceivablePieChart.do":"ACCGoodsReceiptCMN/getAgedPayablePie.do";
        }
        return url;
    },
    
    getBarChartConfig :function(chartParams, param){
        var chartConfig;
        var params = {};
        if(param.showType == "month") {
            var valueFieldArr = (this.receivable) ? ["amountreceived", "amountdue"] : ["amountreceived", "amountdue"];
            var legendTitleArr = (this.receivable) ? ["Received", "Due"] : ["Paid", "Due"];
            var chartColorArr = (this.receivable) ? ["#00ADB5", "#3A4750"] : ["#2D4059", "#D85C6F"];
            params.titleField = "monthname";
            params.valueField = valueFieldArr;
            params.chartColor = chartColorArr;
            params.legendTitle = legendTitleArr;
//           params.textColor = (this.receivable) ? "#009987" : "#CC0000";
            params.valueTitle = "Amount in (" + Wtf.pref.CurrencySymbol + ")";
            params.title = (this.receivable) ? "Aged Receivable Report" : "Aged Payable Report";
            params.categoryAxisTitle = "Months";
            params.unit = Wtf.pref.CurrencySymbol;
            params.height = 520;

//            if (chartParams.params.startdate) {
//                params.subTitle = "For Year : " + this.startDate.getValue().format("Y");
//            }
            chartConfig = getClusteredBarChartConfig(params);
            
        } else if(param.showType == "period") {
            var chartColorArr = (this.receivable) ? ["#3A4750"] : ["#D85C6F"];
            params.titleField = "period";
            params.valueField = "total";
            params.chartColor = chartColorArr;
//           params.textColor = (this.receivable) ? "#009987" : "#CC0000";
            params.valueTitle = (this.receivable) ? "Amount Received in (" + Wtf.pref.CurrencySymbol + ")" : "Amount Paid in (" + Wtf.pref.CurrencySymbol + ")";
            params.title = (this.receivable) ? "Aged Receivable Report" : "Aged Payable Report";
            params.categoryAxisTitle = "Months";
            params.unit = Wtf.pref.CurrencySymbol;
            params.height = 520;

//            if (chartParams.params.startdate) {
//                params.subTitle = "For Year : " + this.startDate.getValue().format("Y");
//            }
            chartConfig = getBarChartConfig(params);
        }
        return chartConfig;
    },
    getPieChartConfig :function(chartParams){
        
        var params = {};
        params.titleField = "customername";
        params.valueField = "total";
        params.chartColor = (this.receivable) ? "#00FF00" : "#50EBEC";
//        params.textColor = (this.receivable) ? "#00ADB5" : "#00ADB5";
        params.title = (this.receivable) ? "Aged Receivable Report Against Customer" : "Aged Payable Report Against Customer";
        params.legendPosition = "right";
        params.unit = Wtf.pref.CurrencySymbol;
        params.height = 520;
        
//        if(chartParams.params.startdate){
//            params.subTitle = "For Year : " + this.startDate.getValue().format("Y");
//        }
        
        var chartConfig = getPieChartConfig(params);
        return chartConfig;
    },
    getColumnHeaderforChart: function () {
        //getting column header as per set interval for chart
        var columnHeaders = [];
        if (this.isSummary && !this.reportWithoutAging) {
            if (this.cm) {
                this.cm.getColumnsBy(function (colConfig, index) {
                    if (colConfig.dataIndex && colConfig.dataIndex.indexOf("amountdue") != -1 && !colConfig.hidden) {
                        columnHeaders.push(colConfig.header);
                    }
                }, this);
            }
        }
        return columnHeaders.join(",");
    },
    storeloaded:function(store){
        Wtf.MessageBox.hide();
        if(this.displayFlag){
            this.pagingToolbar.first.hide();
            this.pagingToolbar.last.hide();
            var pluginsCount=store.getCount()%10;
            var storeCount=store.getCount();
            if(pluginsCount==9 ||pluginsCount==4){
                storeCount=storeCount+1;
            }else if(pluginsCount==8||pluginsCount==3){
                storeCount=storeCount+2;
            }else if(pluginsCount==7||pluginsCount==2){
                storeCount=storeCount+3;
            }else if(pluginsCount==6||pluginsCount==1){
                storeCount=storeCount+4;
            }
            if(this.allPageData){
                this.allPageData=false;
                if(this.pagingToolbar.plugins.combo.store.find('pageSize',storeCount)==-1){
                    var count = this.pagingToolbar.plugins.combo.store.getCount()+1;
                    var defaultdata = {
                        pageSize:storeCount
                    }
                    var record = new this.pagingToolbar.plugins.combo.store.recordType(defaultdata,count);
                    this.pagingToolbar.plugins.combo.store.add(record);
                    this.pagingToolbar.plugins.combo.store.sort('pageSize', 'ASC');
                }
                this.pagingToolbar.plugins.combo.setValue(storeCount);
                this.pagingToolbar.next.disable();
            }
            if(store.getCount()==0){
                this.pagingToolbar.next.disable();
                if(!this.fetch)
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.field.NoMorerecordstodisplay")], 0);
            }
        }
        if (!this.displayFlag && (this.fetch || this.allPageData)) {
            if (store.getCount() == 0) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"), WtfGlobal.getLocaleText("acc.field.NoMorerecordstodisplay")], 0);
            }
        }
        //this.msg=WtfComMsgBox(27,4,true);
        WtfGlobal.resetAjaxTimeOut(); // Function which set time out for 30000 milliseconds i.e. 30 seconds
        if(store.getCount()==0){
            if(this.expButton)this.expButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        this.quickPanelSearch.StorageChanged(store);
        var i = 0;       
        var grandTotalinbase = 0;             // to display the grand total Ref ERP-8925
        var pagedTotalinbase = 0;
        //        for(i=0;i <= store.data.length-1;i++)           //THE LOOP IS REMOVED AS LOADING TIME FOR  JS INCREASES REF SDP-1767
        //        {
        //            grandTotalinbase+=this.isSummary?store.getAt(i).get('totalinbase'):store.getAt(i).get('amountdueinbase');
        //        }
        if(store.reader.jsonData && store.reader.jsonData.totalAmountJSON){
        
            var totalAmountJSONObject = store.reader.jsonData.totalAmountJSON;
            if((this.isSummary && totalAmountJSONObject.grandTotalInBase!=undefined) || (!this.isSummary && totalAmountJSONObject.grandAmountdueinbase!=undefined)){
                if(this.isSummary){
                    grandTotalinbase = totalAmountJSONObject.grandTotalInBase; //APPENDED TO RESOPNSE's LAST RECORD(SDP-1767)
                    pagedTotalinbase = totalAmountJSONObject.pagedTotalInBase; //APPENDED TO RESOPNSE's LAST RECORD(SDP-1767)
                    this.GrandTotalSummary.overwrite(this.GrandTotalSummaryTPL.body,{
                        pagedTotal:WtfGlobal.withoutRateCurrencySymbol(pagedTotalinbase),
                        grandTotal:WtfGlobal.withoutRateCurrencySymbol(grandTotalinbase)
                    });
                }else{
                    grandTotalinbase = totalAmountJSONObject.grandAmountdueinbase; // APPENDED TO RESOPNSE's LAST RECORD(SDP-1767)
                    pagedTotalinbase = totalAmountJSONObject.pagedAmountdueinbase; // APPENDED TO RESOPNSE's LAST RECORD(SDP-1767)
                    this.GrandTotalReport.overwrite(this.GrandTotalReportTPL.body,{
                        pagedTotal:WtfGlobal.withoutRateCurrencySymbol(pagedTotalinbase),
                        grandTotal:WtfGlobal.withoutRateCurrencySymbol(grandTotalinbase)
                    });
                }
                //TODO: check this function
                this.pagingToolbar.updateInfo(); 
            }
        }
          this.grid.getView().refresh();
          if(this.grid.loadMask){
            this.grid.loadMask.hide();   
          }          
        this.loadMask.hide();
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.AgedStore.getAt(i).data['journalentryid'];
            var startDateValue=this.AgedStore.getAt(i).data['date'];    //date value for 'From' field in JE tab 
            var endDateValue=this.AgedStore.getAt(i).data['date'];  // date value for 'To' field in JE tab
            this.fireEvent('journalentry',accid,true,undefined,undefined,undefined,undefined,startDateValue,endDateValue); //journalentry tab opens with 'startDateValue' as 'From' value and 'endDateValue' as 'To' value
        }
        if(header=="billno"){
            //            this.viewTransection(g,i,e)
            var formrec = this.AgedStore.getAt(i);
            var type=formrec.data['type'];
            var withoutinventoryFlag = formrec.data.withoutinventory;
//            if(type=="Credit Note" && withoutinventoryFlag){
//                callViewBillingCreditNote(formrec, 'ViewcreditNote')         
//            }
//            if(type=="Credit Note" && !withoutinventoryFlag){
//                if(this.receivable){
//                    callViewCreditNote("ViewcreditNote" + formrec.get("noteno"), true,true,formrec.get("cntype"),formrec, null);
//                } else{
//                    if(formrec.data.cntype=='5'){
//                        callViewCreditNoteGst(true,formrec,false,false,true);
//                    }else{
//                        callViewCreditNote("ViewcreditNote" + formrec.get("noteno"), true,true,formrec.get("cntype"),formrec, null);
//                    }
//                }     
//            }
////            else if(type=="Debit Note" && withoutinventoryFlag){
////                callViewBillingDebitNote(formrec, 'ViewDebitNote')
////            }
//            else if(type=="Debit Note" && !withoutinventoryFlag){
//                if(this.receivable){
//                    if(formrec.data.cntype=='5'){
//                        callViewCreditNoteGst(true,formrec,false,false,false);
//                    }else{
//                        callViewDebitNote("ViewDebitNote" + formrec.get("noteno"), true,false,formrec.get("cntype"),formrec, null);
//                    }
//                }else{
//                    if(formrec.data.dntype=='5'){
//                        callViewCreditNoteGst(true,formrec,false,false,false);
//                    }else{
//                        callViewDebitNote("ViewDebitNote" + formrec.get("noteno"), true,false,formrec.get("cntype"),formrec, null);
//                    }
//                }
//            }else{
                viewTransactionTemplate(type, formrec);   
//            }
        }
    },
    
    checkDates : function(dateObj,newVal,oldVal){
        if(this.asOfDate.getValue() < Wtf.account.companyAccountPref.bbfrom) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"), WtfGlobal.getLocaleText("acc.agedPay.AsofdateshouldbebeforeBookBeginningDate")], 2);  //"As of date should not be less than From Date."
            dateObj.setValue(oldVal);
        }
        if (this.curDate.getValue() < Wtf.account.companyAccountPref.bbfrom) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"), WtfGlobal.getLocaleText("acc.agedPay.TodateshouldbebeforeBookBeginningDate")], 2);  //"As of date should not be less than From Date."
            dateObj.setValue(oldVal);
        }           
                
                
//        if(this.asOfDate.getValue()<this.startDate.getValue()){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.agedPay.AsofdateshouldbeinrangeofFromDateandToDate")], 2);  //"As of date should not be less than From Date."
//            dateObj.setValue(oldVal);
//        }           
//        if(this.curDate.getValue()<this.startDate.getValue()){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.agedPay.FromdateshouldnotbegreaterthanToDate")], 2);  //"From date should not be greater than To Date."
//            dateObj.setValue(oldVal);
//        }
    },
        
    viewTransection:function(){						// Function for viewing the invoice details from the invoice list  Neeraj
        var formrec=null;
//        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
//                WtfComMsgBox(15,2);
//                return;
//        }
        formrec = this.grid.getSelectionModel().getSelected();
        var type=formrec.data['type'];
        var withoutinventoryFlag = formrec.data.withoutinventory;
        var isExpensiveInv = formrec.get("isexpenseinv");
            
//            if(type=="Customer Invoice" && withoutinventoryFlag) {
//                callViewBillingInvoice(formrec,null, 'ViewBillingInvoice',false);
//            } else 
            if(type=="Customer Invoice" && !withoutinventoryFlag){
                if(formrec.data.fixedAssetInvoice||formrec.data.fixedAssetLeaseInvoice){
                    callViewFixedAssetInvoice(formrec, formrec.data.billid+'Invoice',false,undefined,false,formrec.data.fixedAssetInvoice,formrec.data.fixedAssetLeaseInvoice);
                } else if(formrec.data.isConsignment){
                    callViewConsignmentInvoice(true,formrec,formrec.data.billid+'ConsignmentInvoice',false,false,true);
                }else{
                    callViewInvoice(formrec, 'ViewCashReceipt');
                }
            } 
//            else if(type=="Vendor Invoice" && withoutinventoryFlag) {
//                callViewBillingGoodsReceipt(formrec,null, 'ViewBillingInvoice',false);
//            } 
            else if(type=="Vendor Invoice" && !withoutinventoryFlag){
                if(formrec.data.fixedAssetInvoice){
                    callViewFixedAssetGoodsReceipt(formrec, formrec.data.billid+'GoodsReceipt',false,formrec.data.isExpensiveInv,undefined,false,formrec.data.fixedAssetInvoice);
                } else{
                    callViewGoodsReceipt(formrec, 'ViewGoodsReceipt',formrec.get("isexpenseinv"));
                }
            } 
//            else if(type == "Payment Received" && withoutinventoryFlag) {
//                callViewBillPayment(formrec, 'ViewBillingReceivePayment',true)
//            } 
            else if(type == "Payment Received"&& !withoutinventoryFlag) {
                callViewPayment(formrec, 'ViewReceivePayment',true);
            } 
//            else if(type == "Payment Made" && withoutinventoryFlag) {
//                callViewBillPayment(formrec, 'ViewBillingPaymentMade',false)
//            } 
            else if(type == "Payment Made" && !withoutinventoryFlag) {
                if(Wtf.isNewPaymentStructure) {
                    callViewPaymentNew(formrec, 'ViewPaymentMade',false);
                }
            } 
//            else if(type == "Credit Note" && withoutInventory) {
//                callViewBillingCreditNote(formrec, 'ViewcreditNote')
//            } 
            else if(type == "Credit Note" && !withoutInventory) {
                callViewCreditNote("ViewcreditNote" + formrec.get("noteno"), true,true,formrec.get('cntype'),formrec, null);
            }
//            else if(type == "Debit Note" && withoutInventory) {
//                callViewBillingDebitNote(formrec, 'ViewDebitNote')
//            } 
            else if(type == "Debit Note" && !withoutInventory) {
                callViewDebitNote("ViewDebitNote" + formrec.get("noteno"), true,false,formrec.get('cntype'),formrec, null);
            } 
        
    },
    fetchAllAgedData:function(){
        this.allPageData=true;
        this.AgedStore.load({
            params:{
//                startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
                duration:this.interval.getValue(),
                isdistributive:this.typeEditor.getValue(),
                datefilter : this.dateFilter.getValue(),
                custVendorID:this.Name.getValue(),
                reportWithoutAging:this.reportWithoutAging,
                start:0,
                limit:this.pP.combo.value,
                creditonly:true,
                isAged:true,
                checkforex:true
            }
        });
        if(this.receivable){
            Wtf.agedReceivableDateFilter = this.dateFilter.value;
        } else {
            Wtf.agedPayableDateFilter = this.dateFilter.value;
        }
    },
    fetchAgedData:function(){
        this.fetch=true;
        if(this.interval.getValue()==""||this.interval.getValue()<=1 || this.noOfIntervalCombo.getValue() == ""){
              WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.agedPay.msg1")], 2);   //"Alert","Please enter interval greater than one."], 2);
              return;
        }
   
        this.AgedStore.load({
            params:{
//                startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
                duration:this.interval.getValue(),
                noOfInterval:this.noOfIntervalCombo.getValue(),
                isdistributive:this.typeEditor.getValue(),
                datefilter : this.dateFilter.getValue(),
                groupcombo: this.groupCombo.getValue(),
                custVendorID:this.Name.getValue(),
                reportWithoutAging:this.reportWithoutAging,
                start:0,
                limit: (this.pP != undefined && this.pP.combo != undefined) ? this.pP.combo.value : 30,
                creditonly:true,
                isAged:true,
                fieldid:this.columnCombo.getValue(),
                moduleIDForFetchingGroupingData:this.moduleIDForFetchingGroupingData,
                fieldIDsListForFetchingGroupingData:this.fieldIDsListForFetchingGroupingData,
                listOfModuleIDForFetchingGroupingData:this.listOfModuleIDForFetchingGroupingData
            }
        });
        
        if(this.receivable){ // Customer
            Wtf.agedReceivableInterval = this.interval.getValue();
            Wtf.agedReceivableNoOfInterval = this.noOfIntervalCombo.getValue();
            Wtf.agedReceivableDateFilter = this.dateFilter.value;
        } else { // Vendor
            Wtf.agedPayableInterval = this.interval.getValue();
            Wtf.agedPayableNoOfInterval = this.noOfIntervalCombo.getValue();
            Wtf.agedPayableDateFilter = this.dateFilter.value;
        }
        if(!this.reportWithoutAging){ 
            //when interval and comulative/distributive changes we need to change Header name as well. 
            //eg if interval is 30 then header will be 1-30, when interval is 40 then header will be 1-40

            var dataIndex = this.grid.getColumnModel().findColumnIndex("amountdue1");
            this.cm.setColumnHeader(dataIndex, WtfGlobal.getLocaleText("acc.agedPay.gridCurrent")); //"Current")

            dataIndex = this.grid.getColumnModel().findColumnIndex("amountdue2");

            if (this.typeEditor.getValue()) {
                if (this.dateFilter.value == Wtf.agedDueDate0to30Filter || this.dateFilter.value == Wtf.agedInvoiceDate0to30Filter) {
                    this.cm.setColumnHeader(dataIndex, ("0-" + this.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days"))) //0-30 days
                } else {
                    this.cm.setColumnHeader(dataIndex, ("1-" + this.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days"))) //1-30 days
                }
            } else {
                this.cm.setColumnHeader(dataIndex, (this.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + WtfGlobal.getLocaleText("acc.agedPay.before"))); //30 days before 
            }
            for (var noOfInterval = 1; noOfInterval <= this.totalNoOfColumns; noOfInterval++) {  // loop will run for (this.noOfIntervalCombo -1) Times as 1-30 is already added
                dataIndex = this.grid.getColumnModel().findColumnIndex("amountdue" + (noOfInterval + this.calculateDataIndex));
                if (noOfInterval < this.noOfIntervalCombo.getValue() && this.typeEditor.getValue()) { // Distributive
                    this.cm.setHidden(dataIndex, false);
                    this.cm.config[dataIndex].hideable = true;

                    if (noOfInterval == (this.noOfIntervalCombo.getValue() - 1)) { // To append ">" For Last Column
                        /* For Ex if NoOfInterval is 2 then Header will be >30  */
                        this.cm.setColumnHeader(dataIndex, ">" + (this.interval.getValue() * (noOfInterval)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + " ");
                    } else { // For Column like 31-60 , 61-90 ...
                        this.cm.setColumnHeader(dataIndex, ((this.interval.getValue() * noOfInterval) + 1) + "-" + (this.interval.getValue() * (noOfInterval + 1)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + " ");
                    }
                } else {
                    this.cm.setHidden(dataIndex, true);
                    this.cm.config[dataIndex].hideable = true;
                    if (!this.typeEditor.getValue()) {
                        dataIndex = this.grid.getColumnModel().findColumnIndex("amountdue3");
                        this.cm.setHidden(dataIndex, false);
                        this.cm.config[dataIndex].hideable = true;
                        this.cm.setColumnHeader(dataIndex, ((this.interval.getValue() * 1 + 1) + "-" + (this.interval.getValue() * 2)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days")); //31-60 days

                        var dataIndex = this.grid.getColumnModel().findColumnIndex("amountdue4");
                        this.cm.setHidden(dataIndex, false);
                        this.cm.config[dataIndex].hideable = true;
                        this.cm.setColumnHeader(dataIndex, (">" + (this.interval.getValue() * 2) + " " + WtfGlobal.getLocaleText("acc.agedPay.days"))); //>60 days
                    }
                }
            }
        }
        if (!this.isSummary && this.grid) {
            var index = this.grid.getColumnModel().findColumnIndex('amountdue2');
            if (this.dateFilter.value == Wtf.agedDueDate0to30Filter || this.dateFilter.value == Wtf.agedInvoiceDate0to30Filter) {
                this.cm.setColumnHeader(index, ("0-" + this.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days"))) //0-30 days
            } else {
                this.cm.setColumnHeader(index, ("1-" + this.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days"))) //1-30 days
            }
            this.grid.reconfigure(this.grid.getStore(), this.grid.getColumnModel());
        }
        
        
        this.expButton.setParams({
             duration:this.interval.getValue(),
             noOfInterval:this.noOfIntervalCombo.getValue(),
             accountid:this.accountID,
             enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
             curdate: WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
             isdistributive:this.typeEditor.getValue(),
             datefilter : this.dateFilter.getValue(),
             isAged : true,
             fieldid:this.columnCombo.getValue(),
             moduleIDForFetchingGroupingData:this.moduleIDForFetchingGroupingData,
             fieldIDsListForFetchingGroupingData:this.fieldIDsListForFetchingGroupingData,
             listOfModuleIDForFetchingGroupingData:this.listOfModuleIDForFetchingGroupingData,
             custVendorID:this.Name.getValue()
             
        });
//        if (!this.startDate.getValue() == "") 
//        {
//            this.expButton.setParams({
//                stdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
//                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
//            });
//        } else {
//            this.expButton.setParams({
//                stdate: "",
//                startdate: "",
//            });
//        }
        this.printButton.setParams({
//             stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
//             startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
             duration:this.interval.getValue(),
             noOfInterval:this.noOfIntervalCombo.getValue(),
             accountid:this.accountID,
             enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
             curdate: WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
             isdistributive:this.typeEditor.getValue(),
             datefilter : this.dateFilter.getValue(),
             name: this.receivable?"Aged Receivable":"Aged Payable",
             isAged : true,
             fieldid:this.columnCombo.getValue(),
             moduleIDForFetchingGroupingData:this.moduleIDForFetchingGroupingData,
             fieldIDsListForFetchingGroupingData:this.fieldIDsListForFetchingGroupingData,
             listOfModuleIDForFetchingGroupingData:this.listOfModuleIDForFetchingGroupingData
        });
        this.singleRowPrint.setParams({
//                    stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
//                    startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                    enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                    curdate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                    asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),//this is used for Fetching aged data based on asofdate
                    datefilter:this.dateFilter.getValue(),//this is used for Fetching aged data based on invoice date
                    customerIds:this.Name.getValue(),
                    custVendorID:this.Name.getValue(),
                    reportid: this.receivable?Wtf.Acc_AgedReceivable_CustomizeSummary:Wtf.Acc_AgedPayable_CustomizeSummary,
                    invoiceType:true,
                    name:this.receivable?"Customer Account Statement":"Vendor Account Statement",
                    isPostDatedCheque:false,
                    invoiceAmountDueFilter:false, // for outstanding Invoices only
                    withoutinventory: Wtf.account.companyAccountPref.withoutinventory
                });
    }, 
    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.AgedStore.getAt(i).data['accountid'];
        this.fireEvent('account',accid);
    },
    
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
        
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.AgedStore.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.receivable?2:6,
            filterConjuctionCriteria: this.filterConjuctionCrit,
            isAged:true
        }
        this.AgedStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
    this.objsearchComponent.hide();
    this.AdvanceSearchBtn.enable();
    this.doLayout();
},
    
    filterStore: function(json, filterConjuctionCriteria) {
        /**
         * ERP-33751 - Start Date and End date Required for Saved Search.
         */
//        this.objsearchComponent.advGrid.sdate = this.startDate.getValue();
        this.objsearchComponent.advGrid.edate = this.curDate.getValue();
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.AgedStore.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.receivable?2:6,
            filterConjuctionCriteria: filterConjuctionCriteria,
            creditonly:true,
            withinventory:this.withinventory,
            ignorezero:true,
            nondeleted:true,
            isAged:true
        }
        this.AgedStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
    },
    
    handleRowSelect : function(sm){
        var AccountIds = "";
        var selectedRecArr = this.grid.getSelectionModel().getSelections();
//        if(selectedRecArr.length >= 1) {//SDP-170     //As per SDP-170, user should able to export all. Here this condition is wrong. Vaibhav P.
//                this.SOAButton.enable();
//            } else {
//                this.SOAButton.disable();
//            }
//        var distictVendorsExists = false;
//        if(this.isSummary) {
//            if(selectedRecArr.length == 1) {
//                this.SOAButton.enable();
//            } else {
//                this.SOAButton.disable();
//            }
//        } else {
        var isAllNormalInvoices = true;
        for(var i = 0; i<selectedRecArr.length ; i++){
            var recData= selectedRecArr[i].data;
            if(i==0){
                AccountIds = recData.personid;
            }else{
                AccountIds =AccountIds+","+ recData.personid;
            }
            var type = recData.type;
            var entryNo = recData.entryno;
            if(this.receivable){
                if(type!="Sales Invoice" || (type=="Sales Invoice" && (entryNo=="" || entryNo==undefined) )){
                    isAllNormalInvoices =false;
                }
            } else{
                if(type!="Purchase Invoice" || (type=="Purchase Invoice" && (entryNo=="" || entryNo==undefined) )){
                    isAllNormalInvoices =false;
                }
            }
        }
        if(this.email){
            if(isAllNormalInvoices){
                this.email.enable();
            } else{
                this.email.disable();
            }
        }
//            if(distictVendorsExists){
//                this.email.disable();
//                this.SOAButton.disable();
//            }else{
//                this.email.enable();
//                this.SOAButton.enable();
//            }
//        }
        if (selectedRecArr.length >= 1) {
            this.singleRowPrint.enable();
        } else {
            this.singleRowPrint.disable();
        }
        this.singleRowPrint.setParams({
            customerIds:AccountIds,
            custVendorID:AccountIds
        });
    },
    
    sendMail : function(){
        var selectedRec = null;
        if(this.grid.getSelectionModel().hasSelection()==false){
                WtfComMsgBox(15,2);
                return;
        }
        selectedRec = this.grid.getSelectionModel().getSelections();
//        var mode = Wtf.urlEncode(this.grid.getStore().baseParams);
        var mode = 0;
        var configstr = this.getTemplateConfig();
        var fileType = 'pdf';
        var get = 24;
        var pdfStore = this.filPdfStore();
        var gridConfig = this.genJsonForPdf(pdfStore);
        var deleted = false;
        var nondeleted = true;
        var exportUrl = "ACCInvoiceCMN/exportInvoices.do";
        var gridParams = this.grid.getStore().baseParams;
        var isCustomer = (this.receivable)?true:false;
        var label = WtfGlobal.getLocaleText("acc.field.Aged")+((this.receivable)?WtfGlobal.getLocaleText("acc.field.Receivables")+" ":WtfGlobal.getLocaleText("acc.field.Payables"));
        var fileName = label;
        var filterParams="";
        callEmailForMultipleRecords('newwinid', selectedRec, label, mode, isCustomer, configstr, fileName, fileType, get, gridConfig,deleted,nondeleted,filterParams);
//        var url = exportUrl+"?"+mode+"&config="+configstr+"&filename="+fileName+"&filetype="+ fileType
//                     +"&get="+get+"&gridconfig="+(gridConfig)+"&deleted="+deleted+"&nondeleted="+nondeleted;
//        Wtf.get('downloadframe').dom.src = url;
    },
    
    filPdfStore:function()
    {
        this.pdfStore = new Wtf.data.Store({});
        var column = this.grid.getColumnModel();
        var k=1;
        for(var i=1 ; i<column.getColumnCount() ; i++) { // skip row numberer
//        if(column.isHidden(i)==true||column.getColumnHeader(i)==""||column.getDataIndex(i)=="" || column.getDataIndex(i)=='amountdue1' || column.getDataIndex(i)=='amountdue2' || column.getDataIndex(i)=='amountdue3' || column.getDataIndex(i)=='amountdue4'|| column.getDataIndex(i)=='amountdue5' || column.getDataIndex(i)=='amountdue6' || column.getDataIndex(i)=='amountdue7' || column.getDataIndex(i)=='amountdue8' || column.getDataIndex(i)=='accruedbalance'){
        if(column.isHidden(i)==true||column.getColumnHeader(i)==""||column.getDataIndex(i)=="" || column.getDataIndex(i)=='amountdue1' || column.getDataIndex(i)=='amountdue2' || column.getDataIndex(i)=='amountdue3' || column.getDataIndex(i)=='amountdue4'|| column.getDataIndex(i)=='amountdue5' || column.getDataIndex(i)=='amountdue6' || column.getDataIndex(i)=='amountdue7' || column.getDataIndex(i)=='amountdue8'){
                    continue;
        }
        else{
            if( column.config[i].pdfwidth!=undefined) {
                var format="";
                var title;
                if(column.getRenderer(i)==WtfGlobal.currencyRenderer || column.getRenderer(i)==WtfGlobal.currencyDeletedRenderer ||column.getRenderer(i)==WtfGlobal.currencySummaryRenderer || column.getRenderer(i)==WtfGlobal.currencyRendererDeletedSymbol || column.getRenderer(i)==WtfGlobal.globalCurrencySymbolforDebit || column.getRenderer(i)==WtfGlobal.globalCurrencySymbolforCredit) {
                    format= (column.config[i].hidecurrency) ? "withoutcurrency" :  'currency';
                } else if(column.getRenderer(i)==WtfGlobal.withoutRateCurrencySymbol || column.getRenderer(i)==WtfGlobal.withoutRateCurrencyDeletedSymbol || column.getRenderer(i)==WtfGlobal.withoutRateCurrencySymbolforDebit || column.getRenderer(i)==WtfGlobal.withoutRateCurrencySymbolforCredit) {
                    format= (column.config[i].hidecurrency) ? "withoutrowcurrency" : 'rowcurrency';
                } else if(column.getRenderer(i)==WtfGlobal.onlyDateRenderer || column.getRenderer(i)==WtfGlobal.onlyDateDeletedRenderer) {
                    format='date';
                } else {
                    if(column.config[i].pdfrenderer!=undefined) {
                        format= (column.config[i].hidecurrency) ? "" : column.config[i].pdfrenderer;
                    }
                }

                if(column.config[i].title==undefined)
                    title=column.config[i].dataIndex;
                else
                    title=column.config[i].title;
                this.newPdfRec = new Wtf.data.Record({
                    header : title,
                    title : column.config[i].header,
                    width : column.config[i].pdfwidth,
                    align : format,
                    index : k
                });
                this.pdfStore.insert(this.pdfStore.getCount(), this.newPdfRec);
                k++;
            }
        }
        }
    return this.pdfStore;
    },
    
    getTemplateConfig : function(){
        var title = WtfGlobal.getLocaleText("acc.field.Aged")+((this.receivable)?WtfGlobal.getLocaleText("acc.field.Receivables"):WtfGlobal.getLocaleText("acc.field.Payables"));
        var config = '{"landscape":"true","pageBorder":"true","gridBorder":"true","title":'+title+',"subtitles":"","headNote":"Aged Report","showLogo":"true","headDate":"true","footDate":"false","footPager":"false","headPager":"true","footNote":"","textColor":"000000","bgColor":"FFFFFF"}';
        
        return config;
    },
    
    genJsonForPdf:function (pdfStore)
    {
        var jsondata = [];
        for(var i=0;i<pdfStore.getCount();i++) {
            var recData = pdfStore.getAt(i).data;
            if(recData.align=="right" && recData.title.indexOf("(")!=-1) {
                recData.title=recData.title.substring(0,recData.title.indexOf("(")-1);
            }
            var temp = {
                            header:recData.header,
                            title:encodeURIComponent(recData.title),
                            width:recData.width,
                            align:recData.align
            };
            jsondata.push(temp)
        }

        return Wtf.encode({data:jsondata});
    },
    
    getCustomizedSummary : function(){
        
        var mainid=this.receivable?"mainAgedRecievable":"mainAgedPayable";
        var customizedsummarypanelID=this.receivable?"receivablecustomizedpanelID":"payablecustomizedpanelID";
        var customizedSummaryPanel = Wtf.getCmp(customizedsummarypanelID);
        
        
        if(customizedSummaryPanel == null){
            var loadingMask = new Wtf.LoadMask(document.body,{
                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
            });
            customizedSummaryPanel = new Wtf.account.CustomizedSummaryPanel({
                id : customizedsummarypanelID,
                border : false,
                title : WtfGlobal.getLocaleText("acc.field.CustomizedSummary"),
                layout: 'fit',
                iconCls: 'accountingbase agedrecievable',
                tabTip:(this.receivable?WtfGlobal.getLocaleText("acc.summary.customized.customer.tabtip"):WtfGlobal.getLocaleText("acc.summary.customized.vendor.tabtip")),
                url : this.AgedStoreSummaryUrl,
                receivable : this.receivable,
                moduleid : this.receivable?Wtf.Acc_AgedReceivable_CustomizeSummary :Wtf.Acc_AgedPayable_CustomizeSummary,
                uPermType : this.uPermType,
                exportPermType : this.exportPermType,
                printPermType : this.printPermType,
                withinventory : this.withinventory,
                closable: true,
                loadMask : loadingMask
            });
            Wtf.getCmp(mainid).add(customizedSummaryPanel);
        }
        
        Wtf.getCmp(mainid).setActiveTab(customizedSummaryPanel);
        Wtf.getCmp(mainid).doLayout();
        
    },
    getReportWithoutAging : function(){
       
        var mainid=this.receivable?"mainAgedRecievable":this.isParentChild?"agedreportbasedparentchild":"mainAgedPayable";
        var agingreportpanelID=this.receivable?"receivableagedpanelID":this.isParentChild?"parentpayableagedpanelID":"payableagedpanelID";
        var AgedRecievablepanel = Wtf.getCmp(agingreportpanelID);
            if(AgedRecievablepanel==null){
                AgedRecievablepanel = new Wtf.account.AgedDetail({
                    id: agingreportpanelID,
                    border: false,
                    isSummary:true,
                    withinventory:this.withinventory,
                    reportWithoutAging:true,
                    layout: 'fit',
                    closable: true,
                    totalaged:true,
                    iconCls: 'accountingbase agedrecievable',
                    title: this.receivable?WtfGlobal.getLocaleText("acc.agedPay.totalagedReceivable"):WtfGlobal.getLocaleText("acc.agedPay.totalagedPayable"),  //'Summary View',
                    tabTip:this.receivable?WtfGlobal.getLocaleText("acc.summary.aged.Receivables.totalaged.tooltip"):WtfGlobal.getLocaleText("acc.summary.aged.Payables.totalaged.tooltip"),
                    receivable:this.receivable,
                    displayFlag:true
                });
                Wtf.getCmp(mainid).add(AgedRecievablepanel);
            }
            Wtf.getCmp(mainid).setActiveTab(AgedRecievablepanel);
            Wtf.getCmp(mainid).doLayout();
    },
    getDates:function(start, savedSearchDate){
        if(savedSearchDate != undefined){
            return new Date(savedSearchDate);
        }
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.isexpenseinv=!this.isCustomer&&record.data.isexpenseinv;
        this.withInvMode = record.data.withoutinventory;
        this.expandStore.load({
            params:{
                curdate: WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                duration:this.interval.getValue(),
                noOfInterval:this.noOfIntervalCombo.getValue(),
//                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                isdistributive:this.typeEditor != undefined?this.typeEditor.getValue():true,
                datefilter : this.dateFilter != undefined ? this.dateFilter.getValue() : 0,
                custVendorID:record.data.personid,
                reportWithoutAging:this.reportWithoutAging,
                start:0,
                limit:this.pP.combo.value,
                isCurrencyDetails:true
    }
});
    },
    /*function for SummaryView expandCollpseButton  
     */
    expandCollapseGrid:function(btntext, expandstore, expanderObj, scopeObject) {
    var arr = "";
    var storex = scopeObject.grid.getStore();
    if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
        for (var i = 0; i < storex.data.length; i++) {
            expanderObj.collapseRow(i)
        }
        scopeObject.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
    } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
        storex.each(function(rec) {
            if (rec.data.personid != "" && rec.data.personid != undefined)
                arr += rec.data.personid + ",";
        }, scopeObject);
        if (arr.length != 0) {
            var colModelArray = [];
            colModelArray = GlobalColumnModel[scopeObject.moduleid];
            WtfGlobal.updateStoreConfig(colModelArray, expandstore);
            colModelArray = [];
            colModelArray = GlobalColumnModelForProduct[scopeObject.moduleid];
            WtfGlobal.updateStoreConfig(colModelArray, expandstore);
            arr = arr.substring(0, arr.length - 1);
            scopeObject.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
         this.expandStore.load({
            params:{
                curdate: WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                duration:this.interval.getValue(),
                noOfInterval:this.noOfIntervalCombo.getValue(),
//                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                isdistributive:this.typeEditor != undefined?this.typeEditor.getValue():true,
                datefilter : this.dateFilter != undefined ? this.dateFilter.getValue() : 0,
                custVendorID:storex.data.personid,
                reportWithoutAging:this.reportWithoutAging,
                start:0,
                limit:this.pP.combo.value,
                isCurrencyDetails:true
    }
});
       
    }
},
    /*function to create body for all expanded rows
     */

    fillExpanderBody:function(){
        var p=this.expandStore.getCount();
        if(this.expandStore.getCount()>0){        
            //            for (var j = 0; j < this.expandStore.getCount(); j++) {
            //removing for loop as calculating header for one time.
            var header = "";
            var disHtml = "";
            var arr=[];
            //                var rec1 = this.expandStore.getAt(j);
            if(!this.reportWithoutAging){
                if(!this.typeEditor.getValue()){
                    if (this.dateFilter.value == Wtf.agedDueDate0to30Filter || this.dateFilter.value == Wtf.agedInvoiceDate0to30Filter) {
                        arr = [WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"),
                        WtfGlobal.getLocaleText("acc.agedPay.gridCurrent"),
                        (!this.typeEditor.getValue() ? "" : "0-") + this.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + (!this.typeEditor.getValue() ? " " + WtfGlobal.getLocaleText("acc.agedPay.before") + " " : ""),
                        (this.typeEditor.getValue() ? (">" + (this.interval.getValue() * 6)) : " ") + " " + WtfGlobal.getLocaleText("acc.agedPay.days"),
                        WtfGlobal.getLocaleText("acc.common.total"), WtfGlobal.getLocaleText("acc.common.total") + " " + WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur") + " (" + WtfGlobal.getCurrencyName() + ")"];
                    } else {
                        arr = [WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"),
                        WtfGlobal.getLocaleText("acc.agedPay.gridCurrent"),
                        (!this.typeEditor.getValue() ? "" : "1-") + this.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + (!this.typeEditor.getValue() ? " " + WtfGlobal.getLocaleText("acc.agedPay.before") + " " : ""),
                        (this.typeEditor.getValue() ? (">" + (this.interval.getValue() * 6)) : " ") + " " + WtfGlobal.getLocaleText("acc.agedPay.days"),
                        WtfGlobal.getLocaleText("acc.common.total"), WtfGlobal.getLocaleText("acc.common.total") + " " + WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur") + " (" + WtfGlobal.getCurrencyName() + ")"];
                    }
                }else{
                
                    if (this.dateFilter.value == Wtf.agedDueDate0to30Filter || this.dateFilter.value == Wtf.agedInvoiceDate0to30Filter) {
                        arr = [WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"),
                        WtfGlobal.getLocaleText("acc.agedPay.gridCurrent"),
                        (!this.typeEditor.getValue() ? "" : "0-") + this.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + (!this.typeEditor.getValue() ? " " + WtfGlobal.getLocaleText("acc.agedPay.before") + " " : "")]
                    } else {
                        arr = [WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"),
                        WtfGlobal.getLocaleText("acc.agedPay.gridCurrent"),
                        (!this.typeEditor.getValue() ? "" : "1-") + this.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + (!this.typeEditor.getValue() ? " " + WtfGlobal.getLocaleText("acc.agedPay.before") + " " : "")]
                    }
                     
                    for (var noOfInterval = 1 ; noOfInterval < this.noOfIntervalCombo.getValue() ; noOfInterval++){  // loop will run for (this.noOfIntervalCombo -1) Times as 1-30 is already added
                        if (noOfInterval == (this.noOfIntervalCombo.getValue()-1)) { // To append ">" For Last Column
                            arr.push(">" + (this.interval.getValue() * (noOfInterval)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + " ");
                        } else { // For Column like 31-60 , 61-90 ...
                            arr.push(((this.interval.getValue() * noOfInterval) + 1) + "-" + (this.interval.getValue() * (noOfInterval + 1)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + " ");
                        }
                    }
                     /**
                     * Hidded Total column from AP and AR SDP-13193.
                     */
                   arr.push(!(this.moduleid == Wtf.Acc_AgedReceivables_Summary_ModuleId || this.moduleid == Wtf.Acc_AgedPayables_Summary_ModuleId) ? WtfGlobal.getLocaleText("acc.common.total") : "",
                    WtfGlobal.getLocaleText("acc.common.total") + " " + WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur")
                    );
                }
            }else{
                // Removed header 'Total' from expander body    ERP-40412
                arr=[WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"), 
                //WtfGlobal.getLocaleText("acc.common.total"),
                "<span class = 'currency'>"+ WtfGlobal.getLocaleText("acc.common.total") +" "+ WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur") +"</span>"];
            }
            var gridHeaderText = WtfGlobal.getLocaleText("acc.aged.amountwithcurrency");
            header = "<span class='gridHeader'>"+gridHeaderText+"</span>";   //Product List
            var count=0;
            for(var i=0;i<arr.length;i++){
                if(arr[i] != ""){
                    count++;
                }
            }
            count++; // from grid no
   
            var expanderWidth = (count * 140) + 250;   
            if(this.grid.el && (this.grid.el.dom.clientWidth || this.grid.el.dom.offsetWidth)){
                // calculating expander width from grid width.
                expanderWidth =  this.grid.el.dom.clientWidth || this.grid.el.dom.offsetWidth;
            }
            var widthInPercent = (100 / count);
            var minWidth = count*100;
            header += "<div style='display:table !important;width: "+expanderWidth+"px'>";
      
            for(var arrI=0;arrI<arr.length;arrI++){
                if(arr[arrI]!=undefined) 
                    header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[arrI] + "</span>";
            }
            header += "</div><div style='width: "+expanderWidth+"px'><span class='gridLine'></span></div>";  
            // getting expander data grouped by personid.
            var personExpanderData = this.getGroupedExpanderData(this.expandStore);
            var personStoreData = [];
            
            if(!this.expandButtonClicked){
                // if viewing expander for single person then getting that personid from last params.
                var id = this.expandStore.lastOptions ? this.expandStore.lastOptions.params.custVendorID : "";
                personStoreData.push({"data" :{"personid" : id}});
            }else{
                // getting data from aged store
                personStoreData = this.grid.getStore().getRange();
            }
            var titleHeader = header;
                
            for (var storeCount = 0; storeCount < personStoreData.length ; storeCount++) {
                disHtml = "";
                header = titleHeader;
                var personId = personStoreData[storeCount].data.personid;
                if(personId != "" && !personExpanderData.hasOwnProperty(personId)){
                    // if grouped data don't have personId
                    continue;
                }
                //getting currensy records for person.
                var records = personExpanderData[personId];
                   
                for(var j = 0;j < records.length;j++){
                    var rec = records[j];
                    header += "<div style='display:table !important;height: 18px;width: "+expanderWidth+"px;'>"; 
                    if(!this.reportWithoutAging){
                        if(!this.typeEditor.getValue()){
                            //Column : Currency
                            if(rec.data['currencyname']!=undefined ){
                                header += "<span class='gridRow' wtf:qtip='"+rec.data['currencyname']+"' style='width: "+widthInPercent+"% ! important;'>"+rec.data['currencyname']+"</span>";
                            }else{
                                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
                            }
            
                            //Column : Current
                            if(rec.data['amountdue1']!=undefined){
                                header += "<span class='gridRow' wtf:qtip='"+rec.data['amountdue1']+"' style='width: "+widthInPercent+"% ! important;'>"+this.withoutRateCurrencySymbol(rec.data['amountdue1'],undefined,rec)+"</span>";
                            }else{
                                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
                            }
            
                            //Column : 1-30 days
                            if(rec.data['amountdue2']!=undefined ){
                                header += "<span class='gridRow' wtf:qtip='"+rec.data['amountdue2']+"' style='width: "+widthInPercent+"% ! important;'>"+this.withoutRateCurrencySymbol(rec.data['amountdue2'],undefined,rec)+"</span>";
                            }else{
                                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
                            }

                            //Column : days
                            if(rec.data['amountdue3']!=undefined){
                                header += "<span class='gridRow' wtf:qtip='"+rec.data['amountdue3']+"' style='width: "+widthInPercent+"% ! important;'>"+this.withoutRateCurrencySymbol(rec.data['amountdue3'],undefined,rec)+"</span>";
                            }else{
                                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
                            }
             
                            //Column : Total
                            if(rec.data['total']!=undefined){
                                header += "<span class='gridRow' wtf:qtip='"+rec.data['total']+"' style='width: "+widthInPercent+"% ! important;'>"+this.withoutRateCurrencySymbol(rec.data['total'],undefined,rec)+"</span>";
                            }else{
                                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
                            }
                        }else{
                            //Column : Currency
                            if(rec.data['currencyname']!=undefined ){
                                header += "<span class='gridRow' wtf:qtip='"+rec.data['currencyname']+"' style='width: "+widthInPercent+"% ! important;'>"+rec.data['currencyname']+"</span>";
                            }else{
                                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
                            }

                            for (var noOfInterval = 1; noOfInterval <= this.noOfIntervalCombo.getValue() + 1; noOfInterval++) {

                                    //Column : current,1-30 days,31-60 days...
                                    if (rec.data['amountdue' + noOfInterval] != undefined) {
                                        header += "<span class='gridRow' wtf:qtip='" + rec.data['amountdue' + noOfInterval] + "' style='width: " + widthInPercent + "% ! important;'>" + this.withoutRateCurrencySymbol(rec.data['amountdue' + noOfInterval], undefined, rec) + "</span>";
                                    } else {
                                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                                    }
//                                }
                            }

                            if (rec.data['total'] != undefined) {
                                header += "<span class='gridRow' wtf:qtip='" + rec.data['total'] + "' style='width: " + widthInPercent + "% ! important;'>" + this.withoutRateCurrencySymbol(rec.data['total'], undefined, rec) + "</span>";
                            } else {
                                header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                            }

                            if (rec.data['totalinbase'] != undefined) {
                                header += "<span class='gridRow' wtf:qtip='" + rec.data['totalinbase'] + "' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.withoutRateCurrencySymbol(rec.data['totalinbase']) + "</span>";
                            } else {
                                header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                            }

                        }
                    }else{
                        //Column : Currency
                        if(rec.data['currencyname']!=undefined ){
                            header += "<span class='gridRow' wtf:qtip='"+rec.data['currencyname']+"' style='width: "+widthInPercent+"% ! important;'>"+rec.data['currencyname']+"</span>";
                        }else{
                            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
                        }
            
                        //Column : Total        Hide column 'Total' from expander body  ERP-40412
                        //if(rec.data['total']!=undefined){
                        //    header += "<span class='gridRow' wtf:qtip='"+rec.data['total']+"' style='width: "+widthInPercent+"% ! important;'>"+this.withoutRateCurrencySymbol(rec.data['total'],undefined,rec)+"</span>";
                        //}else{
                        //    header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
                        //}
                        //Column : Total in Base Currency
                        if(rec.data['totalinbase']!=undefined){
                            header += "<span class='gridRow' wtf:qtip='"+rec.data['totalinbase']+"' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.withoutRateCurrencySymbol(rec.data['totalinbase'])+"</span>";
                        }else{
                            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
                        }
                    }
                    header +="</div>";
                }
                header += "</div>";
                disHtml += "<div class='expanderContainer' style='width:auto'>" + header + "</div>";
                
            if (this.expandButtonClicked) {
                // In case of expand all getting expander body and showing html in it.
                var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(storeCount));
                body.innerHTML = disHtml;
                this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
                this.expander.expandRow(storeCount);                // After data set to Grid Row, expand row forcefully.
            }else if(this.expanderBody != undefined){
                this.expanderBody.innerHTML = disHtml;
            }
            }
        }else{
            header = "<span class='gridHeader'>"+gridHeaderText+"</span>";
            header += "<span class='headerRow'>"+WtfGlobal.getLocaleText("acc.field.Nodatatodisplay")+"</span>";
            header += "</div>";
            disHtml += "<div class='expanderContainerAgedDetail' style='width:auto'>" + header + "</div>";
            this.expanderBody.innerHTML = disHtml;
        }
    },
    
    getGroupedExpanderData : function(store){
        /* 
         Json Object to group data for person.
        key : personId
        value : array of currency record objects for that personId
        */
        var dataByPersonId ={}; 
        var storeCount = store.getCount();
        for(var i=0 ; i < storeCount ; i++){
            var rec = store.getAt(i);
            var personid = rec.get('personid');
            if(!dataByPersonId.hasOwnProperty(personid)){
                dataByPersonId[personid] = [];
            }
            dataByPersonId[personid].push(rec);
        }
        return dataByPersonId;
    },
    
    withoutRateCurrencySymbol: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        v= WtfGlobal.conventInDecimal(v,symbol)
        return v;
    },
     groupComboChange: function (combo,newval,oldval) {
            if (combo.getValue() == 1) {                                         //SORT BY DATE 
            this.grid.getStore().groupBy(['personinfo'],false);
        }else{
            if (this.receivable && this.columnCombo.getValue() != Wtf.ExportMolueName.customer) {
                this.grid.getStore().groupBy(['dimensionvalue', 'type'],false);//GROUP BY DIMENTION FILED
            } else {
                this.grid.getStore().groupBy(['personinfo', 'type'],false);     //GROUP BY TRANSCATION
            }
        }
            
            /*this.columnCombo.getValue() == Wtf.ExportMolueName.customer
             **/
            this.fetchAgedData();
    },
    /*
     * ExpandCollapse button function for ReportView
     * To expand or collapse all row details
     * If grid rows are already in expand mode then collapse rows and vise versa
     */
    expandCollpseGrid: function (btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            /*If button text is collapse then collapse all rows*/
            this.grid.getView().collapseAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            /*If button text is expand then expand all rows*/
            this.grid.getView().expandAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },
    /*
     * ExpandCollapse button handler to check which report tab is opened SummaryView or ReportView  
     */
    expandCollapseBttnHandler: function (btntext) {
       if(this.isSummary){ 
          this.expandCollapseGrid(btntext, this.expandStore, this.grid.plugins[0], this);
       } else {
          this.expandCollpseGrid(btntext);
       }
    },
    getMyConfig : function(){
        WtfGlobal.getGridConfig (this.grid, this.moduleid, false, true);
        
        var statusForCrossLinkage = this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage");
        if (statusForCrossLinkage != -1) {
            this.grid.getColumnModel().setHidden(statusForCrossLinkage, true);
        }
    },
    saveMyStateHandler: function(grid,state){
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid, grid.gridConfigId, false);
    }
});

Wtf.account.CustomizedSummaryPanel = function(config){
    
    var url = config.url;
    this.isCustomoizeAgedReport=config.isCustomoizeAgedReport;
    this.summary = new Wtf.ux.grid.GridSummary({});
    this.searchStr=config.searchStr;
    this.moduleid=config.moduleid
    this.frmDuration = [];
    this.toDur = [];
    this.receivable = config.receivable;
    this.uPermType = config.uPermType;
    this.exportPermType = config.exportPermType;
    this.printPermType = config.printPermType;
    this.columnLength=0;   // For No Of Columns
    
    this.durComboRec = new Wtf.data.Record.create([
        {name : 'id'},
        {name : 'val'},
        {name : 'duration'},
        {name: 'dataindex'}
    ]);
    
    this.previousViewedDurComboStore = new Wtf.data.Store({
        url : 'ACCInvoiceCMN/getCustomizedAgedDuration.do',
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.durComboRec)
    });
    
    

    this.DurationComboconfig = {
        store: this.previousViewedDurComboStore,
        valueField:'val',
        hideLabel:true,
        name:'previousViewedDurationCombo',
        displayField:'duration',
        emptyText:WtfGlobal.getLocaleText("acc.duration.interval.select"),
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };
   this.CustomizedDuration = new Wtf.common.Select(Wtf.applyIf({
         multiSelect:true,
         //fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.searchcus") + '*' ,
         forceSelection:true, 
         //extraFields:Wtf.account.companyAccountPref.accountsWithCode?['id']:[],
         listWidth:250,  //Wtf.account.companyAccountPref.accountsWithCode?350:
         //extraComparisionField:'id',// type ahead search on acccode as well.
         width:240
    },this.DurationComboconfig));
    
    this.previousViewedDurComboStore.on('beforeload', function(){
        config.loadMask.show();
    }, this);
    
    this.previousViewedDurComboStore.on('loadexception', function(){
        config.loadMask.hide();
    }, this);
    
    this.previousViewedDurComboStore.on('load', function(store, record){
        config.loadMask.hide();
        if(store.getTotalCount() != 0){
            if(this.isCustomoizeAgedReport){//this flag is true when we select customize report from report 
                var intrval="";
                if(this.searchStr!=""){
                    intrval= eval('(' + this.searchStr + ')')[0].customizeDuration;
                }
                this.CustomizedDuration.setValue(intrval);
            }else{
                this.CustomizedDuration.setValue(store.getAt(0).get('val'));
            }            
            this.customizedSummaryDataColumnAdd();
            if(this.exportButton){
                this.exportButton.enable();
            }
            if(this.printBtn){
                this.printBtn.enable();
            }
        }else{
            this.previousViewedDurationCombo.clearValue();
            this.customizedGridStore.removeAll();
            this.grid.getView().refresh();
            this.grid.getColumnModel().setColumnHeader(2, ((this.receivable?'Receivables':'Payables')+' Between From days - To days'));
            if(this.exportButton){
                this.exportButton.disable();
            }
            if(this.printBtn){
                this.printBtn.disable();
            }
        }
    }, this);
    
    this.previousViewedDurComboStore.load();
    this.dateFilterStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data :[[0,WtfGlobal.getLocaleText("acc.agedPay.dueDate")],[1,WtfGlobal.getLocaleText("acc.agedPay.invoiceDate")]]
    });
    this.CustomizedDateFilter = new Wtf.form.ComboBox({
        store: this.dateFilterStore,
        name:'datefilter',
        displayField:'name',
        value:0,
        anchor:"50%",
        valueField:'id',
        mode: 'local',
        triggerAction: 'all'
    });

    
      this.personRec = new Wtf.data.Record.create([
        {
            name: 'accid'
        }, {
            name: 'accname'
        }, {
            name: 'acccode'
        }
    ]);
    
    this.CustomizedCustomerAccStore =  new Wtf.data.Store({   //Customer/vendor multi selection Combo
        url:this.receivable?"ACCCustomer/getCustomersForCombo.do":"ACCVendor/getVendorsForCombo.do",
        baseParams:{
            //mode:2,
            //group:10,
            deleted:false,
            nondeleted:true,
            combineData:this.receivable?1:-1  //Send For Seprate Request
        },
        reader: new  Wtf.data.KwlJsonReader({
            root: "data"
            //autoLoad:false
        },this.personRec)
    });

    this.CustomizedCustomerAccStore.on("load", function(store){
        var storeNewRecord=new this.personRec({
            accname:'All',
            accid:'All'
        });
        this.CustomizedName.store.insert( 0,storeNewRecord);
        this.CustomizedName.setValue("All"); 
    },this);
    this.CustomizedCustomerAccStore.load();
    
    this.CustomerComboconfig = {
        //hiddenName:this.businessPerson.toLowerCase(),         
        store: this.CustomizedCustomerAccStore,
        valueField:'accid',
        hideLabel:true,
        //hidden : iscustomer,
        displayField:'accname',
        emptyText:this.receivable?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") ,
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };
   this.CustomizedName = new Wtf.common.Select(Wtf.applyIf({
         multiSelect:true,
         fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.searchcus") + '*' ,
         forceSelection:true, 
         extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
         listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
         extraComparisionField:'acccode',// type ahead search on acccode as well.
         width:240
    },this.CustomerComboconfig));
    
     this.CustomizedName.on('select',function(combo,personRec){
        if(personRec.get('accid')=='All'){
            combo.clearValue();
            combo.setValue('All');
        }else if(combo.getValue().indexOf('All')>=0){
            combo.clearValue();
            combo.setValue(personRec.get('accid'));
        }
    } , this); 
     this.CustomizedCurDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        //id: 'dueDate',
        value:new Date(Wtf.serverDate.format('M d, Y')+" 12:00:00 AM")
    });
    
//    this.startDate=new Wtf.ExDateFieldQtip({
//        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
//        name:'startdate',
//        id: 'startdate'+config.helpmodeid,
//        format:WtfGlobal.getOnlyDateFormat(),
//        readOnly:true,
//        value:this.getDates(true)
//    });
    
    this.saveSearchName = new Wtf.form.TextField({            
            anchor: '95%',
            maxLength: 100,
            width:150
    });
    
    var tbar1= new Array();
    var tbar2= new Array();    
    tbar1.push(this.CustomizedName,WtfGlobal.getLocaleText("acc.duration.interval.select")," ",this.CustomizedDuration)
    tbar1.push(" ",WtfGlobal.getLocaleText("acc.field.searchName"),this.saveSearchName); 
    tbar1.push(" ",
    {
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.advancesearch.remembersearch"),
        iconCls:'advanceSearchButton',
        tooltip:WtfGlobal.getLocaleText("acc.advancesearch.remembersearch.ttip"),
        scope : this,
        handler:this.saveSearch
    });  
    
    this.showCustVendorWithZeroAmounts=new Wtf.form.Checkbox({
        text:this.receivable?WtfGlobal.getLocaleText("acc.aged.ShowCustomerswithzeroamounts"):WtfGlobal.getLocaleText("acc.aged.ShowVendorswithzeroamounts"),
        tooltip:(this.receivable?WtfGlobal.getLocaleText("acc.aged.ShowCustomerswithzeroamountsTooltip"):WtfGlobal.getLocaleText("acc.aged.ShowVendorswithzeroamountsTooltip")),
        name:'showCustVendorWithZeroAmounts',
        cls:'checkboxtopPosition',
        autoWidth:true,
        checked:false,
        scope : this
    })
    
    tbar2.push(WtfGlobal.getLocaleText("acc.common.to"),this.CustomizedCurDate,WtfGlobal.getLocaleText("acc.field.On"),this.CustomizedDateFilter,
//    tbar2.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.CustomizedCurDate,WtfGlobal.getLocaleText("acc.field.On"),this.CustomizedDateFilter,
    this.showCustVendorWithZeroAmounts, this.receivable?WtfGlobal.getLocaleText("acc.aged.ShowCustomerswithzeroamounts"):WtfGlobal.getLocaleText("acc.aged.ShowVendorswithzeroamounts"),
    {
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',
        iconCls:'accountingbase fetch',
        scope : this,
        tooltip:this.receivable?WtfGlobal.getLocaleText("acc.receivables.fetch"):WtfGlobal.getLocaleText("acc.payables.fetch"),
        handler:this.customizedSummaryDataColumnAdd
    });
    
    tbar2.push('-',
    {
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.interval.duration.add"),
        iconCls:'pwnd add',
        tooltip:(this.receivable?WtfGlobal.getLocaleText("acc.interval.duration.add.receivables.tooltip"):WtfGlobal.getLocaleText("acc.interval.duration.add.payables.tooltip")),
        scope : this,
        handler:function(){
            saveCustomizedAgedDuration('', this.previousViewedDurComboStore);
        }
    });
    
    
    var customizedRec = new Wtf.data.Record.create([{
            name:'billid'
        },{
            name:'journalentryid'
        },{
            name:'entryno'
        },{
            name:'billno'
        },{
            name:'date', type:'date'
        },{
            name:'duedate', type:'date'
        },{
            name:'personname'
        },{
            name:'amountdueinbase'
        },{
             name:'amountdue'
        },{
            name:'amountdue1'
        },{
            name:'amountdue2'
        },{
            name:'amountdue3'
        },{
            name:'amountdue4'
         },{
            name:'amountdue5'
        },{
            name:'amountdue6'
        },{
            name:'amountdue7'
        },{
            name:'amountdue8'
//        },{
//            name:'accruedbalance'
        },{
            name:'total'
        },{
            name:'amountdueinbase1'
        },{
            name:'amountdueinbase2'
        },{
            name:'amountdueinbase3'
        },{
            name:'amountdueinbase4'
        },{
            name:'total'
        },{
            name:'memo'
        },{
            name: 'currencysymbol'
        },{
            name: 'withoutinventory', type:'boolean'
        },{
            name: 'gTaxId'
        }
    ]);
    
    this.customizedGridStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },customizedRec),
        url: url,
        sortInfo: {
            field: 'personname',
            direction: "DESC"
        },
        baseParams:{
            mode:18,
            creditonly:true,
            withinventory:this.withinventory,
            ignorezero:true,
            isdistributive:this.typeEditor != undefined?this.typeEditor.getValue():true,
            //datefilter : this.CustomizedDateFilter != undefined ? this.CustomizedDateFilter.getValue() : 0,
            nondeleted:true,
            customizedSummaryReportFlag : true
        }
    });
    
   
    this.customizedDurationHeader = ((this.receivable?WtfGlobal.getLocaleText("acc.field.Receivables"):WtfGlobal.getLocaleText("acc.field.Payables"))+' '+WtfGlobal.getLocaleText("acc.field.BetweenFromdaysTodays"));
   
    var custRowNo=new Wtf.KWLRowNumberer();
    var columnArr =[];
    columnArr.push(custRowNo,
    {
        header : (this.receivable?WtfGlobal.getLocaleText("acc.agedPay.cus"):WtfGlobal.getLocaleText("acc.agedPay.ven"))+"/"+ WtfGlobal.getLocaleText("acc.agedPay.accName"),
        dataIndex : 'personname',
        sortable : true,
        pdfwidth:100,
        width:100,
        summaryRenderer: function () {
            return '<div class="grid-summary-common">' + WtfGlobal.getLocaleText("acc.common.total") + '</div>'
        }
    },{
        header : this.customizedDurationHeader,
        dataIndex : 'amountdue1',
        sortable : true,
        align : 'right',
        renderer:WtfGlobal.withoutRateCurrencySymbol,
        pdfwidth:100,
        width:100
    });
//    var customizedGridColModel = new Wtf.grid.ColumnModel([custRowNo,
//    {
//        header : (this.receivable?WtfGlobal.getLocaleText("acc.agedPay.cus"):WtfGlobal.getLocaleText("acc.agedPay.ven"))+"/"+ WtfGlobal.getLocaleText("acc.agedPay.accName"),
//        dataIndex : 'personname',
//        sortable : true,
//        pdfwidth:300
//    },{
//        header : this.customizedDurationHeader,
//        dataIndex : 'amountdue1',
//        sortable : true,
//        align : 'right',
//        renderer:WtfGlobal.withoutRateCurrencySymbol,
//        pdfwidth:300
//    }
//    ]);
     for(var j=2;j<=8;j++)
     {
         columnArr.push({
            header : this.customizedDurationHeader,
            dataIndex : 'amountdue'+j,
            sortable : true,
            align : 'right',
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            pdfwidth:100,
            hidden:true,
            width:100
        });
    } 
    /*ERP-18214  "Total" column in customise summary view for aged receivables*/
    columnArr.push({
            header: '<b>'+WtfGlobal.getLocaleText("acc.common.total")+'</b>',  //Total Amount due
            align:'right',
            width:150,
            pdfwidth:150,
            summaryType: 'sum',
            dataIndex:"total",
            summaryRenderer: function(value,m,rec){
                var retVal = WtfGlobal.withoutRateCurrencySymbol(value)
                return '<b>'+retVal+'</b>';
            },
            pdfrenderer:"rowcurrency",
            hidecurrency : true,
            renderer:WtfGlobal.withoutRateCurrencySymbol
            });
        
    this.grid = new Wtf.grid.GridPanel({
        store : this.customizedGridStore,
        cm : new Wtf.grid.ColumnModel(columnArr),
        sm: new Wtf.grid.RowSelectionModel(),
        border : false,
        plugins: [this.summary],
//        autoHeight:true,
        tbar:tbar2,
        stripeRows : true,
        view:new Wtf.grid.GridView({
            emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec')),
            forceFit:true
        }),
        
        viewConfig : {
            forceFit : true,
            emptyText:'<div style="font-size:15px; text-align:center; color:#CCCCCC; font-weight:bold; margin-top:8%;">'+WtfGlobal.getLocaleText("acc.summary.aged.grid.empty.text")+'<br></div>'
        }
    });
    
      
    this.customizedGridStore.on('beforeload', function(){
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        config.loadMask.show();
    }, this);
    
    this.customizedGridStore.on('loadexception', function(){
        WtfGlobal.resetAjaxTimeOut();
        config.loadMask.hide();
    }, this);
    
    this.customizedGridStore.on('load', function(store){
        config.loadMask.hide();
        WtfGlobal.resetAjaxTimeOut();
        this.grid.getView().refresh(true);
        if(store.getCount()==0){
            if(this.exportButton){
                this.exportButton.disable();
            }
            if(this.printBtn){
                this.printBtn.disable();
            }
        }else{
            if(this.exportButton){
                this.exportButton.enable();
            }
            if(this.printBtn){
                this.printBtn.enable();
            }
        }
        this.pagingToolbar.first.hide();
        this.pagingToolbar.last.hide();
        var pluginsCount=store.getCount()%10;
        var storeCount=store.getCount();
        if(pluginsCount==9 ||pluginsCount==4){
            storeCount=storeCount+1;
        }else if(pluginsCount==8||pluginsCount==3){
            storeCount=storeCount+2;
        }else if(pluginsCount==7||pluginsCount==2){
            storeCount = storeCount + 3;
        } else if (pluginsCount == 6 || pluginsCount == 1) {
            storeCount = storeCount + 4;
        }
        if (this.allPageData) {
            this.allPageData = false;
            if (this.pagingToolbar.plugins.combo.store.find('pageSize', storeCount) == -1) {
                var count = this.pagingToolbar.plugins.combo.store.getCount() + 1;
                var defaultdata = {
                    pageSize: storeCount
                }
                var record = new this.pagingToolbar.plugins.combo.store.recordType(defaultdata, count);
                this.pagingToolbar.plugins.combo.store.add(record);
                this.pagingToolbar.plugins.combo.store.sort('pageSize', 'ASC');
            }
            this.pagingToolbar.plugins.combo.setValue(storeCount);
            this.pagingToolbar.next.disable();
        }else{
            this.pagingToolbar.next.enable();
        }
        if (store.getCount() == 0) {
            this.pagingToolbar.next.disable();
            if (!this.fetch) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"), WtfGlobal.getLocaleText("acc.field.NoMorerecordstodisplay")], 0);
            }
            config.loadMask.hide();
        }
        if (this.pagingToolbar.store.getAt(this.pagingToolbar.store.getCount() - 1).data.personname == "") {
            this.pagingToolbar.store.remove(this.pagingToolbar.store.getAt(this.pagingToolbar.store.getCount() - 1));// REMOVE LAST RECORD AFTER THE GRAND TOTAL IS SET (SDP-1767)
            this.pagingToolbar.updateInfo();
        }
        this.grid.getView().refresh(true);
    }, this);
    
    this.customizedGridStore.on('beforeload', function(store){
        var showCustVendorWithZeroAmounts = this.showCustVendorWithZeroAmounts.getValue();
        store.baseParams.fromDuration = Wtf.util.JSON.encode(this.frmDuration),
        store.baseParams.toDuration = Wtf.util.JSON.encode(this.toDur),
        store.baseParams.datefilter= this.CustomizedDateFilter != undefined ? this.CustomizedDateFilter.getValue() : 0
//        store.baseParams.startdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
        store.baseParams.enddate=WtfGlobal.convertToGenericEndDate(this.CustomizedCurDate.getValue()),    
        store.baseParams.curdate=WtfGlobal.convertToGenericEndDate(this.CustomizedCurDate.getValue()),    
        store.baseParams.showCustVendorWithZeroAmounts= showCustVendorWithZeroAmounts,
        store.baseParams.custVendorID=this.CustomizedName.getValue()
//        store.baseParams.limit=this.pP.combo.value
        if(this.allPageData){
            this.pagingToolbar.next.setDisabled=true;
        }else{
            this.pagingToolbar.next.enable();
        }
    }, this);
    
    this.exportButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.agedPay.exportTT"),  //'Export report details',
        disabled :true,
        filename:this.receivable?WtfGlobal.getLocaleText("acc.wtfTrans.agedrr"): WtfGlobal.getLocaleText("acc.wtfTrans.agedpr"),
        params:{
        },
        menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
        get:(this.receivable?55:56)
    });
    
    
     this.printBtn=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.agedPay.printTT"),  //'Print report details',
        disabled :true,
        filename:this.receivable?WtfGlobal.getLocaleText("acc.wtfTrans.agedrr"): WtfGlobal.getLocaleText("acc.wtfTrans.agedpr"),
        params:{
            name: this.receivable?WtfGlobal.getLocaleText("acc.wtfTrans.agedr"):WtfGlobal.getLocaleText("acc.wtfTrans.agedp")
        },
        lable: this.receivable?WtfGlobal.getLocaleText("acc.wtfTrans.agedr"):WtfGlobal.getLocaleText("acc.wtfTrans.agedp"),
        menuItem:{
            print:true
        },
        get:(this.receivable?55:56)
    });
    
    var buttomBar =new Array();
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))
    buttomBar.push('-',this.exportButton);
    
    
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType))
    buttomBar.push('-',this.printBtn,{
            xtype:'button',
            text:"Fetch All Records",  //'Fetch',
            scope:this,
            tooltip:"Fetch All Records",
            handler:this.fetchAllCustomizedSummaryData
        }
    );
    
    
    Wtf.apply(this,{
        border:false,
        scope : this,
        layout : "fit",
        tbar : tbar1,
        items:[this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.customizedGridStore,
            searchField: this.quickPanelSearch,
            afterPageText:'',
            //displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.agedPay.norec"),  //"No results to display",
            plugins: this.pP = new Wtf.pPageSizeForAllOption({
                id : "pPageSize_"+this.id
            }),
            items:buttomBar
        })
    });
    
    Wtf.account.CustomizedSummaryPanel.superclass.constructor.call(this,config);
}

Wtf.extend( Wtf.account.CustomizedSummaryPanel,Wtf.Panel,{
    customizedSummaryDataColumnAdd : function(){
        var customizedHeader = this.CustomizedDuration.getValue();
        var frmDurationArr=[];
        var toDurArr=[]
        if(customizedHeader !== ''){
            var customizedHeaderArr=customizedHeader.split(',');
            this.columnLength=customizedHeaderArr.length;
       
                var counter=0;
            for(var i=0;i<this.columnLength;i++){
                var selectedDurVal=customizedHeaderArr[i];
            var durationArray = selectedDurVal.split('-');
            this.frmDuration = durationArray[0];
            this.toDur = durationArray[1];
                    for(var k=0;k<this.previousViewedDurComboStore.getCount();k++){
                        if(selectedDurVal==this.previousViewedDurComboStore.data.items[k].data.val){
                            counter++;
                            frmDurationArr.push("{'id':'"+durationArray[0]+"','amountdueindex':'amountdue"+counter+"','amountdue':'"+this.previousViewedDurComboStore.data.items[k].data.dataindex+"'}"); //
                            toDurArr.push("{'id':'"+durationArray[1]+"'}");
                            break;
            }
                    }
          }
            
          this.frmDuration=frmDurationArr;
          this.toDur=toDurArr;
          //this.grid.getColumnModel().setColumnHeader(k, this.customizedDurationHeader);
            this.customizedGridStore.load({
                params : {
//                          startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                          enddate:WtfGlobal.convertToGenericEndDate(this.CustomizedCurDate.getValue()),                  
                          curdate:WtfGlobal.convertToGenericEndDate(this.CustomizedCurDate.getValue()),
//                          datefilter : this.CustomizedDateFilter.getValue(),
                          custVendorID:this.CustomizedName.getValue(),
                          start:0,
                          limit:this.pP.combo.value
                          //creditonly:true
                    
                }
            });
         
        }
//        for(i=1;i<cm.config.length;i++){
//            cm.setHidden(j,true);
//        }
         var cm=this.grid.getColumnModel();
         var selectedId=this.CustomizedDuration.getValue();
         var seleArray=selectedId.split(",");
         var data =this.CustomizedDuration.store.data;
         for(i=0;i<this.previousViewedDurComboStore.getCount();i++){
            for(var j=0;j<cm.config.length;j++){
                var cmbRec=this.previousViewedDurComboStore.getAt(i);
                var cmbdataIndec=cmbRec.data.dataindex;
                var cmbValue=cmbRec.data.val;
                var cmmodeldataIndec=cm.config[j].dataIndex;
                if(cmbdataIndec==cmmodeldataIndec){
                    if(selectedId.indexOf(cmbValue) !=-1)
                    {
                        cm.setHidden(j,false);
                        cm.setColumnHeader(j,(this.receivable?WtfGlobal.getLocaleText("acc.field.ReceivablesBetween"):WtfGlobal.getLocaleText("acc.field.PayablesBetween"))+cmbValue+' '+WtfGlobal.getLocaleText("acc.agedPay.days") );
                    }else{
                        cm.setHidden(j,true);
                    }
                }
            }
             
        }
     
//         cm.reconfigure();
    },
    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    saveSearch:function(){        
        if(this.CustomizedDuration.getValue()=="" || this.CustomizedDuration.getValue()==undefined){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.fxexposure.warning"),WtfGlobal.getLocaleText("acc.wtfcomalert.msg.105")], 2); 
            return;
        }
        if(this.saveSearchName.getValue()=="" || this.saveSearchName.getValue()==undefined){          
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.fxexposure.warning"),WtfGlobal.getLocaleText("acc.wtfcomalert.msg.105")], 2); 
            return;
        }
        var jsonArray=[];
        jsonArray.push({
            customizeDuration:this.CustomizedDuration.getValue()
        });
        var json = Wtf.encode(jsonArray);
        var saveSearchName = this.saveSearchName.getValue();
        var appendCase = "";
        Wtf.Ajax.requestEx({
            url:'AdvanceSearch/saveSearchQuery.do',
            params:{
                searchstate:json,
                module:this.moduleid,
                searchname:saveSearchName,
                filterAppend:appendCase
            }
        },this,this.genSuccessFunction,this.genFailureFunction);
    },
    fetchAllCustomizedSummaryData:function(){
//        this.allPageData=true;
        this.customizedGridStore.load({
            params : {
//                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.CustomizedCurDate.getValue()),                  
                curdate:WtfGlobal.convertToGenericEndDate(this.CustomizedCurDate.getValue()),
                custVendorID:this.CustomizedName.getValue(),
                showCustVendorWithZeroAmounts: this.showCustVendorWithZeroAmounts.getValue(),
                start:0,
                limit:this.pP.combo.value,
                checkforex:true
            }
        });
        this.grid.getView().refresh(true);
    },
    genSuccessFunction:function(res){
        if(res.msg!=undefined){
            Wtf.MessageBox.show({
                title:WtfGlobal.getLocaleText("acc.msgbox.CONFIRMTITLE"),
                msg:WtfGlobal.getLocaleText("acc.field.serachnamealreadyexist"),
                icon:Wtf.MessageBox.QUESTION,
                buttons:Wtf.MessageBox.YESNO,
                scope:this,
                fn:function(button){
                    if(button=='yes'){
                        var jsonArray=[];
                        jsonArray.push({
                            customizeDuration:this.CustomizedDuration.getValue()
                        });
                        var json = Wtf.encode(jsonArray);
                        var saveSearchName = this.saveSearchName.getValue();
                        var appendCase = "";
                        Wtf.Ajax.requestEx({
                            url:'AdvanceSearch/saveSearchQuery.do',
                            params:{
                                searchstate:json,
                                module:this.moduleid,
                                searchname:saveSearchName,
                                confirmationFlag:true,
                                filterAppend:appendCase
                            }
                        },this,function(res){
                            this.saveSearchName.setValue("");
                            WtfComMsgBox(103,0);
                        },function(res){
                            WtfComMsgBox(104,1);
                        })
                    }
                }
            });
        } else {
            this.saveSearchName.setValue("");
            WtfComMsgBox(103,0);
        }
    },
    genFailureFunction:function(resp){
        WtfComMsgBox(104,1);
    }
});

Wtf.pPageSizeForAllOption = function(config){
    Wtf.apply(this, config);
};

Wtf.extend(Wtf.pPageSizeForAllOption, Wtf.util.Observable, {
    /**
     * @cfg {String} beforeText
     * Text to display before the comboBox
     */
    beforeText: WtfGlobal.getLocaleText("acc.rem.151"), //'Show',
    
    /**
     * @cfg {String} afterText
     * Text to display after the comboBox
     */
    afterText: WtfGlobal.getLocaleText("acc.rem.152"), //'items',
    
    /**
     * @cfg {Mixed} addBefore
     * Toolbar item(s) to add before the PageSizer
     */
    addBefore: '-',
    
    /**
     * @cfg {Mixed} addAfter
     * Toolbar item(s) to be added after the PageSizer
     */
    addAfter: null,
    
    /**
     * @cfg {Array} variations
     * Variations used for determining pageSize options
     */
    variations: [5, 10, 20, 50, 100],
    
    init: function(pagingToolbar){
        this.pagingToolbar = pagingToolbar;
        this.pagingToolbar.on('render', this.onRender, this);
    },
    
    //private
    addToStore: function(value){
        if (value > 0) {
            this.sizes.push([value]);
        }
    },
    
    //private
    updateStore: function(){
        var middleValue = this.pagingToolbar.pageSize, start;
        middleValue = (middleValue > 0) ? middleValue : 1;
        this.sizes = [];
        var v = this.variations;
        for (var i = 0, len = v.length; i < len; i++) {
            this.addToStore(middleValue - v[v.length - 1 - i]);
        }
        this.addToStore(middleValue);
        for (var i = 0, len = v.length; i < len; i++) {
            this.addToStore(middleValue + v[i]);
        }

        this.combo.store.loadData(this.sizes);
        this.combo.setValue(this.pagingToolbar.pageSize);
    },

    changePageSize: function(value){
        var pt = this.pagingToolbar;
        value = parseInt(value) || parseInt(this.combo.getValue());
        value = (value > 0) ? value : 1;
        if (value < pt.pageSize) {
            pt.pageSize = value;
            var ap = Math.round(pt.cursor / value) + 1;
            var cursor = (ap - 1) * value;
            var store = pt.store;
            store.suspendEvents();
            for (var i = 0, len = cursor - pt.cursor; i < len; i++) {
                store.remove(store.getAt(0));
            }
            while (store.getCount() > value) {
                store.remove(store.getAt(store.getCount() - 1));
            }
            store.resumeEvents();
            store.fireEvent('datachanged', store);
            pt.cursor = cursor;
            var d = pt.getPageData();
            pt.afterTextEl.el.innerHTML = String.format(pt.afterPageText, d.pages);
            pt.field.dom.value = ap;
            pt.first.setDisabled(ap == 1);
            pt.prev.setDisabled(ap == 1);
            pt.next.setDisabled(ap == d.pages);
            pt.last.setDisabled(ap == d.pages);
            pt.updateInfo();
        }
        else {
            this.pagingToolbar.pageSize = value;
            this.pagingToolbar.doLoad(Math.floor(this.pagingToolbar.cursor / this.pagingToolbar.pageSize) * this.pagingToolbar.pageSize);
        }
        this.updateStore();
        this.combo.collapse();
    },
    //private
    onRender: function(){
        var component = Wtf.form.ComboBox;
        this.combo = new component({
            store: new Wtf.data.SimpleStore({
                fields: ['pageSize'],
                data: []
            }),
            clearTrigger: false,
            displayField: 'pageSize',
            valueField: 'pageSize',
            editable: false,
            mode: 'local',
            triggerAction: 'all',
            width: 50
        });
        this.combo.on('select', this.changePageSize, this);
        this.updateStore();
        
        if (this.addBefore) {
            this.pagingToolbar.add(this.addBefore);
        }
        if (this.beforeText) {
            this.pagingToolbar.add(this.beforeText);
        }
        this.pagingToolbar.add(this.combo);
        if (this.afterText) {
            this.pagingToolbar.add(this.afterText);
        }
        if (this.addAfter) {
            this.pagingToolbar.add(this.addAfter);
        }
    }
})