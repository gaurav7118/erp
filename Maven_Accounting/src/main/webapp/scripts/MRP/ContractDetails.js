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

Wtf.account.ContractDetails = function(config) {
    this.id = config.id;
    this.modeName = config.modeName;
    this.isContractDetails = config.isContractDetails ? config.isContractDetails : false;
    this.productOptimizedFlag = Wtf.account.companyAccountPref.productOptimizedFlag;
    this.isEdit = config.isEdit!=undefined ? config.isEdit : false;
    this.record = config.record;
    this.custUPermType=Wtf.UPerm.customer;
    this.custPermType=Wtf.Perm.customer;
    
    Wtf.apply(this, config);

    Wtf.account.ContractDetails.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.account.ContractDetails, Wtf.Panel, {
    autoScroll: true,
    bodyStyle: {background: "#DFE8F6 none repeat scroll 0 0"},
    border: false,
    closable: false,
    autoHeight: true,
    initComponent: function(config) {
        Wtf.account.ContractDetails.superclass.initComponent.call(this, config);

        //Create stores required for fields
        this.createStores();

        //Load stores required for fields
        this.loadStores();

        //Create fields required in panels
        this.createFields();

        this.createCustomFields();


        //Create panels
        this.createPanels();

        //Create Product grid
        this.createProductGrid();
        
    },
    onRender: function(config) {
        
        this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            scope: this,
//            width:"100%",
            autoHeight: true,
            id: 'centerPanel' + this.id,
            items: [
                this.contractDetailsPanel
//                ,this.GridPanel
                        , this.ProductGrid
            ]
        });

        this.add(this.centerPanel);

        Wtf.account.ContractDetails.superclass.onRender.call(this, config);
    },
    createStores: function() {
        this.TypeStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'id'
                }, {
                    name: 'name'
                }],
            data: [['1', 'Yes'], ['2', 'No']]
        });

        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
            {
                name: 'id'
            },
            {
                name: 'value'
            },
            {
                name: 'oldflag'
            }
        ]);
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.sequenceFormatStoreRec),
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: "automrpcontract",
                isEdit: this.isEdit
            }
        });

        this.sellerTypeRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.sellerTypeStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 35
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.sellerTypeRec)
        });
        
        this.contractStatusRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.contractStatusStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 45
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.contractStatusRec)
        });

        this.parentContractIdRec = Wtf.data.Record.create ([
            {name:'billid'},
            {name:'billno'},
            {name:'personname'},
            {name:'personid'},
            {name:'currencyid'},
            {name:'currencysymbol'},
            {name:'contractname'}
        ]);
        
        this.parentContractIdStore = new Wtf.data.Store({
            url:"ACCContractMaster/getMasterContracts.do",
            baseParams:{
                mode: 42,
                closeflag:true,
                nondeleted: true,
                onlyApprovedRecords: true,
                rfqlinkflag: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.parentContractIdRec)
        });
        this.parentContractIdStore.on('load', this.parentContractIdStoreOnLoad, this);
        
        this.contractTermStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'id'
                }, {
                    name: 'name'
                }],
            data: [['1', 'Day'], ['2', 'Week'], ['3', 'Month'], ['4', 'Year']]
        });

    },
    
    loadStores: function() {
        this.sequenceFormatStore.load();
        this.sequenceFormatStore.on('load', this.setNextNumber, this);
        
        this.sellerTypeStore.load();
        this.sellerTypeStore.on('load', this.sellerTypeStoreOnLoad, this);
        
        this.contractStatusStore.load();
        this.contractStatusStore.on('load', this.contractStatusStoreOnLoad, this);
        
        this.parentContractIdStore.load();
    },
    
    setNextNumber: function(config) {
         if(this.sequenceFormatStore.getCount()>0){
            if(this.isEdit){ //only edit case 
                var index=this.sequenceFormatStore.find('id',this.record.data.seqformat);   
                if(index!=-1){
                    this.sequenceFormatCombobox.setValue(this.record.data.seqformat); 
                    this.sequenceFormatCombobox.disable();
                    this.contractID.disable();   
                } else {
                    this.sequenceFormatCombobox.setValue("NA"); 
                    this.sequenceFormatCombobox.disable();
                    this.contractID.enable(); 
                }
            }else if(!this.isEdit){//New Case
                var count=this.sequenceFormatStore.getCount();
                for(var i=0;i<count;i++){
                    var seqRec=this.sequenceFormatStore.getAt(i)
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                        break;
                    }
                }
                if(this.sequenceFormatCombobox.getValue()!=""){
                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
                } else{
                    this.contractID.setValue("");
                    this.contractID.disable();
                }
            }         
        }
        
        
    },
    loadData: function() {
        if (this.record != null && this.isEdit) {
            var data = this.record.data;
//            this.contractDetailsPanel.getForm().loadRecord(this.record);
            this.contractID.setValue(data.contractid);
            this.contractName.setValue(data.contractname);
            this.Customer.setValForRemoteStore(data.customerid, data.customername);
            this.creationDate.setValue(data.creationdate);
            
            this.contractTerm.setValue(data.contacttermid);
            /*Set term values depending upon start date & end date*/
            var date1 = new Date(data.contractstartdate);
            var date2 = new Date(data.contractenddate);
            var timeDiff = Math.abs(date2.getTime() - date1.getTime());
            var diffDays = 0;
            if(data.contacttermid === Wtf.mrpContractName.day) {
                diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));
            } else if (data.contacttermid === Wtf.mrpContractName.week) {
                diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24 * 7));
            } else if (data.contacttermid === Wtf.mrpContractName.month) {
                diffDays = Math.floor(timeDiff / (1000 * 3600 * 24 * 30));
            } else if (data.contacttermid === Wtf.mrpContractName.year) {
                diffDays = Math.floor(timeDiff / (1000 * 3600 * 24 * 365));
            }
            this.termvalue.setValue(diffDays);
            /*Set term values depending upon start date & end date*/
            this.fromDate.setValue(data.contractstartdate);
            this.toDate.setValue(data.contractenddate);
            this.parentContractName.setValue(data.subcontractname);
         
            //Enable form components in edit case
            this.enableComponentsForEdit();
            //Disable form components in edit case
            this.disableComponentsForEdit();
        }
    },
    sellerTypeStoreOnLoad: function(){
        if (this.record != null && this.isEdit) {
            var data = this.record.data;
            this.sellerType.setValue(data.sellertypeid);
        }
    },
    contractStatusStoreOnLoad: function(){
        if (this.record != null && this.isEdit) {
            var data = this.record.data;
            this.contractStatus.setValue(data.contractstatusid);
        }
    },
    parentContractIdStoreOnLoad: function(){
        if (this.record != null && this.isEdit) {
            var data = this.record.data;
            this.parentContractId.setValue(data.subcontractid);
        }
    },
    enableComponentsForEdit:function(){
        this.enbleStartEndDate();
    },
    disableComponentsForEdit:function(){
        this.Customer.disable();
    },
    getNextSequenceNumber: function(a, val) {
        if(!(a.getValue()=="NA")){
            var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
            var oldflag=rec!=null?rec.get('oldflag'):true;
            Wtf.Ajax.requestEx({
                url:"ACCCompanyPref/getNextAutoNumber.do",
                params:{
                    from:Wtf.MRP_MASTER_CONTRACT_MODULE_ID,
                    sequenceformat:a.getValue(),
                    oldflag:oldflag
                }
            }, this,function(resp){
                if(resp.data=="NA"){
                    this.contractID.reset();
                    this.contractID.enable();
                }else {
                    this.contractID.setValue(resp.data);
                    this.contractID.disable();
                }
            
            });
        } else {
            this.contractID.reset();
            this.contractID.enable();
        }
    },
    createFields: function() {
        //Create left panel fields
        this.createLeftContractDetailsFields();

        //Create right panel fields
        this.createRightContractDetailsFields();
    },
    createLeftContractDetailsFields: function() {
        this.sequenceFormatCombobox = new Wtf.form.ComboBox({
            triggerAction: 'all',
            mode: 'local',
            id: 'sequenceformatcombobox' + this.id,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.Sequenceformat.tip") + "'>" + WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat") + "</span>",
            valueField: 'id',
            displayField: 'value',
            store: this.sequenceFormatStore,
            disabled: (this.isEdit ? true : false),
            width:240,
            typeAhead: true,
            forceSelection: true,
            name: 'sequenceformat',
            hiddenName: 'sequenceformat',
            listeners: {
                'select': {
                    fn: this.getNextSequenceNumber,
                    scope: this
                }
            }

        });

        this.contractID = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.field.contractid") + "*",
            name: 'contractid',
            id: "contractid" + this.id,
            hidden: false,
            width: 240,
            maxLength: 50,
            allowBlank: false,
            scope: this
        });

        this.contractName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.field.contractname") + "*",
            name: 'contractname',
            id: "contractname_" + this.id,
            hidden: false,
            width: 240,
            maxLength: 50,
            allowBlank: false,
            scope: this
        });

        this.Customer = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.documentRequired.customerName") + "*",
            id: "customer" + this.id,
            store: Wtf.customerAccRemoteStore,
            valueField: 'accid',
            displayField: 'accname',
            minChars: 1,
            name: 'customer',
            allowBlank: false,
            hirarchical: true,
            emptyText: WtfGlobal.getLocaleText("acc.inv.cus"),
            mode: 'remote',
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode ? 550 : 400,
            extraComparisionField: 'acccode',
            typeAhead: true,
            typeAheadDelay: 30000,
            forceSelection: true,
            selectOnFocus: true,
            width: 240,
            triggerAction: 'all',
            scope: this
        });
        if(!WtfGlobal.EnableDisable(this.custUPermType,this.custPermType.create)){
            this.Customer.addNewFn=this.addPerson.createDelegate(this,[false,null,"Customerwindow",true],true);
        }

        this.creationDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.field.creationdate") + "*",
            name: 'creationdate',
            id: "creationdate" + this.id,
            hidden: false,
            width: 240,
            maxLength: 50,
            scope: this,
            allowBlank: false,
            format: WtfGlobal.getOnlyDateFormat(),
            emptyText: WtfGlobal.getLocaleText("acc.mastercontract.field.creationdate.emptytext")
        });

        this.sellerType = new Wtf.form.ExtFnComboBox({
            triggerAction: 'all',
            mode: 'remote',
            id: 'sellertype' + this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.field.sellertype"),
            valueField: 'id',
            displayField: 'name',
            store: this.sellerTypeStore,
            width: 240,
            typeAhead: true,
            forceSelection: true,
            name: 'sellertype',
            hiddenName: 'sellertype',
            extraFields: [],
            allowBlank: true,
            addNoneRecord: true,
            emptyText: WtfGlobal.getLocaleText("acc.mastercontract.field.sellertype.emptytext")
        });
        this.sellerType.addNewFn = this.addSellerType.createDelegate(this);
    },
    addPerson:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid='contactDetailCustomerTab';
        Wtf.getCmp(tabid).on('update', function(){
           Wtf.customerAccStore.load();
        }, this);
    },
    createRightContractDetailsFields: function() {
        this.fromDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.field.contractstartdate") + "*",
            name: 'contractstartdate',
            id: "contractstartdate" + this.id,
            hidden: false,
            width: 240,
            maxLength: 50,
            scope: this,
            allowBlank: false,
            format: WtfGlobal.getOnlyDateFormat(),
            emptyText: WtfGlobal.getLocaleText("acc.mastercontract.field.contractstartdate.emptytext")
        });
        this.fromDate.on('blur', this.updateEndDate, this);
        this.toDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.field.contractenddate") + "*",
            name: 'contractenddate',
            id: "contractenddate" + this.id,
            hidden: false,
            width: 240,
            maxLength: 50,
            scope: this,
            format: WtfGlobal.getOnlyDateFormat(),
            allowBlank: false,
            fromenddate: true,
            emptyText: WtfGlobal.getLocaleText("acc.mastercontract.field.contractenddate.emptytext")
        });
        this.toDate.on('blur', this.updateEndDate, this);
        this.disableStartEndDate();
        this.termvalue = new Wtf.form.NumberField({
            allowNegative: false,
            defaultValue: 0,
            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.field.contractterm"),
            allowBlank: false,
            maxLength: 10,
            allowDecimals:false,
            width: 90,
//            anchor: '50%',
            hideLabel: true,
            name: 'termvalue'
        });
        this.termvalue.on('blur', this.updateEndDate, this)
        this.termvalue.on('change', this.enbleStartEndDate, this)

        this.contractTerm = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.field.contractterm")+"*",
            name: 'contractterm',
            id: "contractterm" + this.id,
            hidden: false,
            width: 70,
            maxLength: 50,
            scope: this,
            anchor: '95%',
            triggerAction: 'all',
            mode: 'local',
            valueField: 'id',
            displayField: 'name',
            store: this.contractTermStore,
            addNoneRecord: true,
            allowBlank: false,
            emptyText: WtfGlobal.getLocaleText("acc.mastercontract.field.contractterm.emptytext"),
            listeners: {
                'select': {
                    fn: this.updateEndDate,
                    scope: this
                }
            }
        });

        this.contractStatus = new Wtf.form.ExtFnComboBox({
            triggerAction: 'all',
            mode: 'remote',
            id: 'contractstatus' + this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.field.contractstatus"),
            valueField: 'id',
            displayField: 'name',
            store: this.contractStatusStore,
            width: 240,
            typeAhead: true,
            forceSelection: true,
            listWidth: 300,
            name: 'contractstatus',
            hiddenName: 'contractstatus',
            allowBlank: true,
            extraFields: [],
//            addNoneRecord: true,
            emptyText: WtfGlobal.getLocaleText("acc.mastercontract.field.contractstatus.emptytext")
        });

        this.contractStatus.addNewFn = this.addContractStatus.createDelegate(this);

        this.parentContractId = new Wtf.common.Select({
            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.field.parentcontractid"),
            name: "parentcontractid",
            store: this.parentContractIdStore,
            id: 'parentcontractid' + this.id,
            valueField: 'billid',
            displayField: 'billno',
            clearTrigger: true,
            mode: 'local',
            width: 240,
            typeAhead: true,
            selectOnFocus: true,
            allowBlank: true,
            triggerAction: 'all',
            scope: this,
            multiSelect: true,
            forceSelection: true,
            emptyText: WtfGlobal.getLocaleText("acc.mastercontract.field.parentcontractid.emptytext")
        });
         this.parentContractId.on('collapse',function(combo){
          var Contractname ="",ContractnameArr="";
          var index=0;
          while(index<combo.valueArray.length)
          {                
                var idx = this.parentContractIdStore.find("billid", combo.valueArray[index]);
                if(idx == -1)
                    return Contractname;
                var Contractname = this.parentContractIdStore.getAt(idx).data["contractname"];
                ContractnameArr=(index>0)?(ContractnameArr+","+Contractname):Contractname;
                index++;
               
            }
          this.parentContractName.setValue(ContractnameArr);
        },this);
        this.parentContractId.on('clearval', function (combo, rec, index) {
            this.parentContractName.setValue("");
        }, this);

        this.parentContractName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.field.parentcontractname"),
            name: 'parentcontractname',
            id: "parentcontractname" + this.id,
            hidden: false,
            width: 240,
            maxLength: 50,
            allowBlank: true,
            scope: this
        });
    },
    createPanels: function() {
        this.leftContractDetailsPanel = new Wtf.Panel({
            columnWidth: 0.45,
            id: "leftContractDetailsPanel" + this.id,
            layout: "form",
            scope: this,
            labelWidth: 150,
            bodyStyle: {
                margin: "20px"
            },
            border: false,
            items: [
                this.sequenceFormatCombobox,
                this.contractID,
                this.contractName,
                this.Customer,
                this.creationDate,
                this.sellerType
            ]
        });

        this.rightContractDetailsPanel = new Wtf.Panel({
            columnWidth: 0.45,
            id: "rightContractDetailsPanel" + this.id,
            layout: "form",
            labelWidth: 150,
            scope: this,
            border: false,
            bodyStyle: {
                margin: "20px"
            },
            defaults: {
                border: false
            },
            items: [{
                    layout: 'column',
                    width: 600,
                    defaults: {
                        border: false
                    },
                    items: [
                        {
                            layout: 'form',
                            columnWidth: 0.50,
                            items: [this.contractTerm]
                        }, {
                            layout: 'form',
                            columnWidth: 0.30,
                            items: [this.termvalue]
                        }
                    ]},
                this.fromDate,
                this.toDate,
                this.contractStatus,
                this.parentContractId,
                this.parentContractName
            ]
        });

        this.contractDetailsPanel = new Wtf.form.FormPanel({
            region: "center",
            id: "contractDetailsPanel" + this.id,
            border: false,
            autoHeight: true,
            autoScroll: true,
            scope: this,
            bodyStyle: {
                        background: "#f1f1f1 none repeat scroll 0 0",
                        margin: "10px",
                        borderColor: "#99bbe8",
                        border: "1px solid #99bbe8"
                    },
            layout: 'form',
            items: [{
                    layout: 'column',
                    border: false,
                    items: [
                        this.leftContractDetailsPanel,
                        this.rightContractDetailsPanel
                    ]
                }, this.tagsFieldset]
        });


    },
    createProductGrid: function() {
        this.ProductGrid = new Wtf.account.MRPProductDetailsGrid({
            layout: "fit",
            id: this.id + "editproductdetailsgrid",
            bodyBorder: true,
            border: false,
            bodyStyle: 'padding:10px',
            height: 300,
            isInitialQuatiy: false,
            excluseDateFilters: true,
            rendermode: "productform",
            isContractDetails: this.isContractDetails,
            isEdit: this.isEdit,
            record: this.record
        });

    },
    createCustomFields: function () {
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId: "contractDetailsPanel" + this.id,
            autoHeight: true,
            bodyStyle:'margin: 0 10px;',
            moduleid: Wtf.MRP_MASTER_CONTRACT_MODULE_ID,
            isEdit: this.isEdit,
            record: this.record,
            isViewMode: this.readOnly,
            parentcompId: this.id
        });
    },
    addSellerType: function() {
        addMasterItemWindow('35');
    },
    addContractStatus: function() {
        addMasterItemWindow('45');
    },
    updateEndDate: function(a, val) {
        var term = null;
        var termtype = this.contractTerm.getValue();  //['1','Day'],['2','Week'],['3','Month'],['4','Year']
        var initialEndDateEnteredByUser = this.toDate.getValue();
        switch (termtype * 1) {
            case 1://Day
                if (this.fromDate.getValue() != "" && this.termvalue.getValue() != "" && isNaN(this.termvalue.getValue()) == false) {
                    term = new Date(this.fromDate.getValue()).add(Date.DAY, this.termvalue.getValue() + 1);
                }
                else if (this.fromDate.getValue() != "" && isNaN(this.fromDate.getValue()) == false) {
                    term = this.fromDate.getValue();
                }
                break;
            case 2://Week
                if (this.fromDate.getValue() != "" && this.termvalue.getValue() != "" && isNaN(this.termvalue.getValue()) == false) {
                    term = new Date(this.fromDate.getValue()).add(Date.DAY, (this.termvalue.getValue() * 7)+1);
                }
                else if (this.fromDate.getValue() != "" && isNaN(this.fromDate.getValue()) == false) {
                    term = this.fromDate.getValue();
                }
                break;
            case 3://Month
                if (this.fromDate.getValue() != "" && this.termvalue.getValue() != "" && isNaN(this.termvalue.getValue()) == false) {
                    term = new Date((this.fromDate.getValue()).add(Date.MONTH, this.termvalue.getValue()).add(Date.DAY, 1));
                }
                else if (this.fromDate.getValue() != "" && isNaN(this.fromDate.getValue()) == false) {
                    term = this.fromDate.getValue();
                }
                break;
            case 4://Year
                if (this.fromDate.getValue() != "" && this.termvalue.getValue() != "" && isNaN(this.termvalue.getValue()) == false) {
                    term = new Date((this.fromDate.getValue()).add(Date.YEAR, this.termvalue.getValue()).add(Date.DAY, 1));
                }
                else if (this.fromDate.getValue() != "" && isNaN(this.fromDate.getValue()) == false) {
                    term = this.fromDate.getValue();
                }
                break;
        }
        if (term != null) {
            term.setDate(term.getDate() - 1);
            this.toDate.setValue(term);
        }

    },
    enbleStartEndDate: function(txt, newval, oldval) {
        var termValue = this.termvalue.getValue();
        if (termValue != "") {
            this.fromDate.enable();
            this.toDate.enable();
        }
    },
    disableStartEndDate: function() {
        this.fromDate.disable();
        this.toDate.disable();
    }

});