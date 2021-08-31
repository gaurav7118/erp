
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

Wtf.account.FixedAssetPurchaseRequisitionPanel = function(config) {
    this.quotation = config.quotation;
    this.DefaultVendor = config.DefaultVendor;
    this.isFixedAsset = (config.isFixedAsset!=null && config.isFixedAsset!=undefined)? config.isFixedAsset : false;
    this.id = config.id;
    this.titlel = (config.title != undefined)? config.title : "null";
    this.dataLoaded = false;
    this.isExpenseInv = false;
    this.isEdit = config.isEdit;
    this.isDraft = false;
    this.isRequisition = (config.isRequisition)? config.isRequisition : false;
    this.isRFQ = (config.isRFQ)? config.isRFQ : false;
    this.label = config.label;
    this.copyInv = config.copyInv;
    this.viewGoodReceipt = config.viewGoodReceipt;
    this.readOnly = config.readOnly;
    this.billid = null;
    this.custChange = false;
    this.record = config.record;
    this.pendingapproval = (config.pendingapproval != undefined)? config.pendingapproval : false;
    this.datechange = 0;
    this.oldval = "";
    this.val = "";
    this.pronamearr = [];
    this.changeGridDetails = true;
    this.appendID = true;
    this.mailFlag=false;
    var help = getHelpButton(this, config.heplmodeid);
    this.productOptimizedFlag = Wtf.account.companyAccountPref.productOptimizedFlag;
    var isbchlFields1 = (!config.isCustomer && config.isOrder);
    this.modeName = config.modeName;
    this.heplmodeid = config.heplmodeid;
    this.PR_IDS = config.PR_IDS;
    this.originallyLinkedDocuments = '';    
    /* SDP-13487
     * To identify, from where the call has been received to Business Logic Function when user open the draft in edit mode and then save it again. this.isDraft will be false but this.isFromDraftReport will be true when user save the draft as an transaction.
     */
    this.isDraft = false;
    this.isSaveDraftRecord = (this.record!=null && this.record.data!=undefined) ? this.record.data.isDraft : false;
    this.isAutoSeqForEmptyDraft = false;    //SDP-13927 : To identify Old Record No. and Auto Generated No while saving Draft Record in Edit Mode.
    this.isSequenceFormatChangedInEdit = false; //SDP-13923 : This flag has been used to identify whether user has changed Sequence Format in Edit case (Only for Draft Type of record)
    
    var buttonArray = new Array();
    this.saveBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), // 'Save',
        tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
        id: "submit" + config.heplmodeid,
        hidden: this.viewGoodReceipt,
        scope: this,
        handler:function() {
            this.isDraft = false;
            this.isSaveDraftRecord = (this.record!=null && this.record.data!=undefined) ? this.record.data.isDraft : false;  //SDP-13487 - When user save the draft as a Transaction then to identify this call is for transaction, we have used this flag. At this time, this.isDraft will be false.
            this.save();
        },
        iconCls: 'pwnd save'
    });
    this.savencreateBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveasdraft"), // 'Save as Draft',
        tooltip: WtfGlobal.getLocaleText("acc.rem.222"),
        id: "save" + config.heplmodeid + this.id,
        hidden : this.isRFQ || this.viewGoodReceipt || (this.isEdit && !this.copyInv && !this.record.data.isDraft),// || (this.isRequisition && this.isEdit ),
        scope: this,
        disabled:(this.isEdit && this.record!=null && this.record.data.isDraft!=undefined && !this.record.data.isDraft),    //ERP-38582 : REOPEN-1
        handler: function() {
            this.isDraft = true;
            this.isSaveDraftRecord = false; //SDP-13487 - When user save the draft as a Transaction then to identify this call is for transaction, we have used this flag. At this time, this.isDraft will be false.
            this.save();
        },
        iconCls: 'pwnd save'
    });
    buttonArray.push(this.saveBttn,this.savencreateBttn);
    
    if (isbchlFields1 && this.isEdit && !this.isRFQ) {
        buttonArray.push(this.exportButton = new Wtf.exportButton({
            obj: config.POthisObj,
//            id: "exportReports" + config.helpmodeid + config.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), // 'Export report details',
            disabled: false,
            hidden: this.readOnly,
            menuItem: {csv: false, pdf: false, rowPdf: (config.isSalesCommissionStmt) ? false : true, rowPdfTitle: WtfGlobal.getLocaleText("acc.rem.39")},
            get: config.POnewtranType
        }));
    }
    
    if (!this.readOnly&&!this.isEdit) {
        buttonArray.push('->');
        buttonArray.push(help);
    }

    Wtf.apply(this, config);
    Wtf.apply(this, {
        bbar: buttonArray
    });
    
    Wtf.account.FixedAssetPurchaseRequisitionPanel.superclass.constructor.call(this,config);
    
    this.addEvents({
        'update': true
    });
}

Wtf.extend(Wtf.account.FixedAssetPurchaseRequisitionPanel, Wtf.account.ClosablePanel, {
    autoScroll: true,
    bodyStyle: {background:"#DFE8F6 none repeat scroll 0 0"},
    border: 'false',
    externalcurrencyrate: 0,
    isCurrencyLoad: false,
    currencyid: null,
    custdatechange: false,
    closable : true,
    cash: false,
    layout: 'border',
    isCustomer: false,
    cls: 'southcollapse',
    isCustBill: false,
    isOrder: false,
    fromOrder: false,
    
    loadRecord: function() {
        if (this.record!=null && !this.dataLoaded) {
            var data = this.record.data;
            this.NorthForm.getForm().loadRecord(this.record);
            
            if ((!this.copyInv && this.isRequisition) || this.viewGoodReceipt|| (this.isRFQ && this.isEdit)) {
                if(data.isDraft){
                    if(data.billno!=""){
                        this.Number.setValue(data.billno);  //SDP-13487 : Do not set empty entry no. in edit case of of Draft                                         
                    }
                } else {
                    this.Number.setValue(data.billno);
                }
            }
//            if(this.isRFQ && this.isEdit){
//                this.Number.disable();
//                this.sequenceFormatCombobox.disable();
//            }
            
            this.Currency.setValue(data.currencyid);
            
            var store = (this.isCustomer)? Wtf.customerAccStore : Wtf.vendorAccStore;
            var index = store.findBy(function(rec) {
                var parentname = rec.data['accid'];
                if (parentname == data.personid) {
                    return true;
                } else {
                    return false;
                }
            })
            if (index >= 0) {
                store.load();
            }
            if (data.personid != undefined) {
                this.Name.setValue(data.personid);
            } else {
                this.Name.setValue("");
            }
            this.Memo.setValue(data.memo);
            this.DueDate.setValue(data.duedate);
            this.billDate.setValue(data.date);
            this.dataLoaded = true;
            
            if (this.copyInv) {
            	//this.billDate.setValue(Wtf.serverDate);
            	this.updateDueDate();
            }
            
            if (this.isCustomer && this.record.data.partialinv) {
                var id = this.Grid.getId();
                var rowindex = this.Grid.getColumnModel().getIndexById(id + "partdisc");
                this.Grid.getColumnModel().setHidden(rowindex, false) ;
            }
            
            var gridID = this.Grid.getId();
            var taxColumnIndex = this.Grid.getColumnModel().getIndexById(gridID + "prtaxid");
            var taxAmtColumnIndex = this.Grid.getColumnModel().getIndexById(gridID + "taxamount");
            
            if (this.record.data.includeprotax) {
                this.Grid.getColumnModel().setHidden(taxColumnIndex,false) ;
                this.Grid.getColumnModel().setHidden(taxAmtColumnIndex,false) ;
            } else {
                this.Grid.getColumnModel().setHidden( taxColumnIndex,true) ;
                this.Grid.getColumnModel().setHidden( taxAmtColumnIndex,true) ;
            }
        }
    },
    
    afterRender: function(config) {
        Wtf.account.FixedAssetPurchaseRequisitionPanel.superclass.afterRender.call(this, config);
    },
    
    onRender: function(config) {
        var centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll: true
        });
        
        if (this.isCustomer || this.isCustBill || this.isOrder || this.isEdit || this.copyInv) {
            centerPanel.add(this.NorthForm,this.Grid,this.southPanel);
        } else {
            centerPanel.add(this.NorthForm,this.GridPanel,this.southPanel);
        }
        
        this.add(centerPanel);
        Wtf.account.FixedAssetPurchaseRequisitionPanel.superclass.onRender.call(this, config);

        this.initForClose();
    },
    
    initComponent: function(config) {
        Wtf.account.FixedAssetPurchaseRequisitionPanel.superclass.initComponent.call(this,config);
        this.businessPerson = (this.isCustomer? 'Customer' : 'Vendor');
        this.loadCurrFlag = true;
        
        if (!this.isCustBill) {
            this.isCustBill = false;
        }

        this.tplSummary = new Wtf.XTemplate(
            '<div class="currency-view">',
            '<hr class="templineview">',
            '<table width="100%">',
            '<tr><td ><b>' + WtfGlobal.getLocaleText("acc.invoice.totalAmt") + ' </b></td><td align=right>{totalamount}</td></tr>',
            '</table>',
            '<table width="100%">',
            '<tr><td ><b>' + WtfGlobal.getLocaleText("acc.invoice.totalAmtInBase") + ' </b></td><td align=right>{totalAmtInBase}</td></tr>',
            '</table>',
            '<hr class="templineview">',
            '<hr class="templineview">',
            '</div>'
        );
            
        this.GridRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'number'}
        ]);

        this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid', mapping: 'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname', mapping: 'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'}
         ]);
         
         this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.currencyRec),
            url: "ACCCurrency/getCurrencyExchange.do"
         });
         
         this.currencyStoreCMB = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
            url: "ACCCurrency/getCurrencyExchange.do"
         });

         this.currencyStoreCMB.load();
         
         this.Currency = new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.currency.cur") + "*", // 'Currency',
            hiddenName: 'currencyid',
            hidden: this.isRFQ,
            hideLabel: this.isRFQ,
            id: this.isEdit || this.copyInv? "currency" + this.heplmodeid + this.id : "currency" + this.heplmodeid,
            anchor: '94%',
            store: this.currencyStore,
            valueField: 'currencyid',
            allowBlank: this.isRFQ? true : false,
            forceSelection: true,
            displayField: 'currencyname',
            scope: this,
            disabled: this.readOnly,
            selectOnFocus: true
        });

        this.Currency.on('select', function() {
            //            this.currencychanged = true;
            this.externalcurrencyrate=0; 
            if (this.currencyStore.getCount() < 1) {
                callCurrencyExchangeWindow();
                
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+"<b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.Currency.setValue("");
            } else {
                this.updateFormCurrency();
            }
        }, this);
         
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'value'},
            {name: 'oldflag'}
        ]);
         
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.sequenceFormatStoreRec),
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: this.modeName
            }
        });
        
        this.sequenceFormatStore.on('load', function() {
            if (this.sequenceFormatStore.getCount() > 0) {
                if (this.isEdit||this.copyInv) {
                    var index = this.sequenceFormatStore.find('id',this.record.data.sequenceformatid);   
                    if (index != -1) {
                        this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid);
                        this.sequenceFormatCombobox.disable();
                        this.Number.disable();  
                        /*
                         * SDP-13487 : Below piece of code has written to get the next auto sequence no.when draft open in edit mode.
                         */
			this.draftNo = this.record.data.billno;
                        this.isDraft = (this.record != null && this.record.data != undefined) ? this.record.data.isDraft : false;
                        if (this.isDraft && !this.copyInv && (this.draftNo==null || this.draftNo==undefined || this.draftNo=="")) {
                            this.Number.disable();
                            WtfGlobal.hideFormElement(this.Number);
                            this.getNextSequenceNumber(this.sequenceFormatCombobox);
			    this.isAutoSeqForEmptyDraft = true;
                        } else if (this.copyInv) {//for copy NA enable disable number field
                            this.sequenceFormatCombobox.enable();
                            this.getNextSequenceNumber(this.sequenceFormatCombobox);
                        }
                    } else {
                        this.sequenceFormatCombobox.setValue("NA");
                        if((this.isDraft || this.record.data.isDraft) && this.isEdit){
                            this.sequenceFormatCombobox.enable()    //SDP-13923 : In edit case, if sequence format is NA for draft record then keep Sequence Format Combox enable
                            this.Number.enable();  
                        } else if (this.viewGoodReceipt || this.isEdit) {
                            this.sequenceFormatCombobox.disable();
                            if (this.viewGoodReceipt) {
                                this.Number.disable();
                            }else{
                                this.Number.enable();
                            }
                        } else {
                            this.sequenceFormatCombobox.enable();
                            this.Number.enable();
                        }
                    } 
                } else {
                    var count = this.sequenceFormatStore.getCount();
                    for (var i=0; i<count; i++) {
                        var seqRec = this.sequenceFormatStore.getAt(i);
                        if (seqRec.json.isdefaultformat == "Yes") {
                            this.sequenceFormatCombobox.setValue(seqRec.data.id);
                            break;
                        }
                    }
                    if(this.sequenceFormatCombobox.getValue()!=""){
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
                    } else{
                        this.Number.setValue(""); 
                        WtfGlobal.hideFormElement(this.Number);
                    }
                }
            }
        }, this);
        this.sequenceFormatStore.load();
       
        var transdate = (this.isEdit || this.copyInv? WtfGlobal.convertToGenericDate(this.record.data.date) : WtfGlobal.convertToGenericDate(new Date()));
        
        this.sequenceFormatCombobox = new Wtf.form.ComboBox({
            triggerAction: 'all',
            mode: 'local',
            fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
            valueField: 'id',
            displayField: 'value',
            store: this.sequenceFormatStore,
            disabled: (this.isEdit && !this.copyInv && !this.isSOfromPO? true : false),  
            anchor: '50%',
            typeAhead: true,
            forceSelection: true,
            name: 'sequenceformat',
            hiddenName: 'sequenceformat',
            allowBlank: false,
            listeners: {
                'select': {
                    fn: this.getNextSequenceNumber,
                    scope: this
                }
            }
        });
        this.sequenceFormatCombobox.on('change',this.sequenceFormatChanged,this);   //SDP-13923 : Call the 'sequenceFormatChanged' function on Sequence Format change.    
        
           this.PORec = Wtf.data.Record.create ([
            {name:'billid'},
            {name:'journalentryid'},
            {name:'entryno'},
            {name:'billto'},
            {name:'discount'},
            {name:'shipto'},
            {name:'mode'},
            {name:'billno'},
            {name:'date', type:'date'},
            {name:'duedate', type:'date'},
            {name:'shipdate', type:'date'},
            {name:'personname'},
            {name:'creditoraccount'},
            {name:'personid'},
            {name:'shipping'},
            {name:'othercharges'},
            {name:'taxid'},
            {name:'currencyid'},
            {name:'amount'},
            {name:'amountdue'},
            {name:'costcenterid'},
            {name:'costcenterName'},
            {name:'memo'},
            {name:'othervendoremails'},
            {name:'includeprotax',type:'boolean'},
        ]);
        this.POStoreUrl = "ACCPurchaseOrderCMN/getRequisitions.do";
        this.POStore = new Wtf.data.Store({
            url: this.POStoreUrl,
            baseParams: {
                mode: (this.isCustBill ? 52 : 42)
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            }, this.PORec)
        });
        var colModelArray = GlobalColumnModelForReports[this.moduleid];
        if (colModelArray) {
            for (var cnt = 0; cnt < colModelArray.length; cnt++) {
                var fieldname = colModelArray[cnt].fieldname;
                var newField = new Wtf.data.Field({
                    name: fieldname.replace(".", ""),
//                   sortDir:'ASC',
                    type: colModelArray[cnt].fieldtype == 3 ? 'date' : (colModelArray[cnt].fieldtype == 2 ? 'float' : 'auto'),
                    format: colModelArray[cnt].fieldtype == 3 ? 'y-m-d' : undefined
                });
                this.POStore.fields.items.push(newField);
                this.POStore.fields.map[fieldname] = newField;
                this.POStore.fields.keys.push(fieldname);
            }
            this.POStore.reader = new Wtf.data.KwlJsonReader(this.POStore.reader.meta, this.POStore.fields.items);
        }
//        this.POStore.load({params: {isFixedAssetRFQ: true,isFixedAsset:true}});
        this.Number = new Wtf.form.TextField({
            fieldLabel:  WtfGlobal.getLocaleText("acc.common.Asset") +this.label + " " + WtfGlobal.getLocaleText("acc.common.number"),  // this.label + ' Number*',
            name: 'number',
            disabled: ((this.isEdit && !this.copyInv)? true : false) || this.viewGoodReceipt,
//            id: this.isEdit? "invoiceNo" + this.heplmodeid + this.id : "invoiceNo" + this.heplmodeid,//ERP-40015
            anchor: '50%',
            maxLength: 50,
            scope: this,
            allowBlank: this.checkin
        });
        
        this.Memo = new Wtf.form.TextArea({
            fieldLabel: Wtf.account.companyAccountPref.descriptionType,  // 'Memo/Note',
            name: 'memo',
            id: this.isEdit || this.copyInv? "memo"+this.heplmodeid+this.id : "memo"+this.heplmodeid, // +this.id,
            height: 40,
            readOnly: this.readOnly,
            anchor: '94%',
            maxLength: this.isRFQ? 8000 : 2048 ,
            qtip: (this.record==undefined)? ' ' : this.record.data.memo,
            listeners: {
                render: function(c) {
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                    });
                }
            }
        });
            
        this.DueDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.dueDate"), // 'Due Date*',
            name: 'duedate',
            format: WtfGlobal.getOnlyDateFormat(),
            allowBlank: false,
            disabled: this.viewGoodReceipt,
            anchor: '94%'
        });

        this.billDate = new Wtf.form.DateField({
            fieldLabel:  WtfGlobal.getLocaleText("acc.common.Asset") +this.label + ' ' + WtfGlobal.getLocaleText("acc.invoice.date"),
            id: this.isEdit || this.copyInv? "invoiceDate" + this.heplmodeid + this.id : "invoiceDate" + this.heplmodeid,
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
            disabled: this.viewGoodReceipt,
            anchor: '50%',
            listeners: {
                'change': {
                    fn: this.updateDueDate,
                    scope: this
                }
            },
            allowBlank: false
        });
        
        this.Name = new Wtf.common.Select({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.ven"),
            hiddenName: this.businessPerson.toLowerCase(),
            id: "customer" + this.heplmodeid + this.id,
            store: this.isCustomer? Wtf.customerAccStore : Wtf.vendorAccStore,
            valueField: 'accid',
            displayField: 'accname',
            allowBlank: true,
            hideLabel: this.isRequisition || (this.viewGoodReceipt && !this.isRFQ),
            hidden: this.isRequisition || (this.viewGoodReceipt && !this.isRFQ),
            emptyText: WtfGlobal.getLocaleText("acc.inv.ven") , //'Select a '+this.businessPerson+'...',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus: true,
            disabled: (this.viewGoodReceipt && this.isRFQ),
            anchor: "50%",
            multiSelect: true,
            xtype: 'select',
            clearTrigger:!(this.readOnly),
            triggerAction: 'all'
        });
        
        if (!(this.DefaultVendor == null || this.DefaultVendor == undefined) && !this.isCustomer) {
            this.Name.value = this.DefaultVendor;
            this.updateData();
        }
        
        this.VendorEmail = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.VendorEmailAddress"),
            name: 'othervendoremails',
            hidden: this.isRequisition || (this.viewGoodReceipt && !this.isRFQ),
            hideLabel: this.isRequisition || (this.viewGoodReceipt && !this.isRFQ),
            value: '',
            anchor: '50%',
            maxLength: 512,
            disabled: (this.viewGoodReceipt && this.isRFQ),
            scope: this,
            allowBlank: true
        });
        
        this.EmailMessage = new Wtf.Panel({
            border: false,
            hidden: this.isRequisition || this. viewGoodReceipt,
            xtype: 'panel',
            bodyStyle: 'padding:0px 0px 10px 140px;',
            html: '<font color="#555555">' + WtfGlobal.getLocaleText("acc.MailWin.msg16") + '</font>'
        });
        
        var itemArr = {};
        
        itemArr = [this.sequenceFormatCombobox, this.Number, this.billDate, this.Name, this.VendorEmail, this.EmailMessage];
        
        var ht = 150;
        if (this.isCustBill) {
            ht += 25;
        }
        if (!(this.quotation || !this.isCustomer || this.isOrder)) {
            ht += 130;
        }
               
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            record: this.record,
            compId:"northForm" + this.id,
            autoHeight: true,
            parentcompId:this.id,
            isViewMode:this.isViewTemplate,
            moduleid: this.isRFQ?Wtf.Acc_FixedAssets_RFQ_ModuleId:Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId,
            isEdit: this.isEdit || this.copyInv
//            copyInv: this.copyInv
        });

        this.NorthForm = new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,
            border: false,
            disabledClass: "newtripcmbss",
            id: "northForm" + this.id,
            items: [
                {
                    layout: 'form',
                    baseCls: 'northFormFormat',
                    labelWidth: 155,
                    cls: "visibleDisabled",
                    items:[
                        {
                            layout: 'column',
                            border: false,
                            defaults: {border: false},
                            items:[
                                {
                                    layout: 'form',
                                    columnWidth: 0.65,
                                    border: false,
                                    items: itemArr
                                },{
                                    layout: 'form',
                                    columnWidth: 0.35,
                                    border: false,
                                    items: [this.DueDate, this.Memo, this.Currency]
                                }
                            ]
                        },this.tagsFieldset
                    ]
                }
            ]
        });
        
        var blockSpotRateLink_first = "";
        var blockSpotRateLink_second = "";
        if(!Wtf.account.companyAccountPref.activateToBlockSpotRate){ // If activateToBlockSpotRate is set then block the Spot Rate Links
            blockSpotRateLink_first = WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div><div style='padding-left:30px;padding-top:5px;padding-bottom:10px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{foreigncurrency}\",\"{basecurrency}\",\"{revexchangerate}\",\"foreigntobase\")'wtf:qtip=''>{foreigncurrency} to {basecurrency}</a>";
            blockSpotRateLink_second = WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div> <div style='padding-left:30px;padding-top:5px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{basecurrency}\",\"{foreigncurrency}\",\"{exchangerate}\",\"basetoforeign\")'wtf:qtip=''>{basecurrency} to {foreigncurrency}</a></div>";
        }
       this.southCenterTplSummary=new Wtf.XTemplate(
    "<div> &nbsp;</div>",  //Currency:
             '<tpl if="editable==true">',
         "<b>"+WtfGlobal.getLocaleText("acc.invoice.msg8")+"</b>",  //Applied Exchange Rate for the current transaction:
           "<div style='line-height:18px;padding-left:30px;'>1 {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+" = {revexchangerate} {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+". "+
         blockSpotRateLink_first,
         "</div><div style='line-height:18px;padding-left:30px;'>1 {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+" = {exchangerate} {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+". "+    
         blockSpotRateLink_second,
             '</tpl>'
        );
        
        this.southCenterTpl = new Wtf.Panel({
            border: false,
            html: this.southCenterTplSummary.apply({basecurrency:WtfGlobal.getCurrencyName(),exchangerate:'x',foreigncurrency:"Foreign Currency", editable:false})
        });
        
        this.southCalTemp = new Wtf.Panel({
            border: false,
            hidden: this.isRFQ,
            baseCls: 'tempbackgroundview',
            html: this.tplSummary.apply({totalamount:WtfGlobal.currencyRenderer(0),tax:WtfGlobal.currencyRenderer(0),totalAmtInBase:WtfGlobal.currencyRenderer(0)})
        });
        
        this.helpMessage = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.dashboard.help"), // 'Help',
            handler: this.helpmessage,
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.click"), // 'Click for help',
            iconCls: 'help'
        });
        
        this.addGrid();
        
        this.southPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            style: 'padding:0px 10px 10px 10px',
            layout: 'column',
            height: 200,
            items: [
                {
                    columnWidth: .45,
                    border: false,
                    items: this.southCenterTpl
                },{
                    id: this.id + 'southEastPanel',
                    columnWidth: .55,
                    border: false,
                    layout: 'column',
                    items: [
                        {
                            layout: 'form',
                            width: 170,
                            labelWidth: 100,
                            border: false
                        },{
                            layout: 'form',
                            columnWidth: 0.4,
                            labelWidth: 30,
                            border: false
                        },{
                            columnWidth: 0.6,
                            layout: 'form',
                            cls: 'bckgroundcolor',
                            bodyStyle: 'padding:10px',
                            labelWidth: 70,
                            hidden: this.isRFQ? true : false,
                            items: this.southCalTemp
                        }
                    ]
                }
            ]
        });
        
        var lastTransPanelId = "";
        
        if (this.quotation) {
            lastTransPanelId = "quotation";
        } else if (this.isOrder) {
            lastTransPanelId = this.isCustomer? "salesorder" : "purchaseorder";
        } else if (this.cash) {
            lastTransPanelId = this.isCustomer? "cashsales": "cashpurchase";
        } else {
            lastTransPanelId = this.isCustomer? "CInvoiceList": "VInvoiceList";
        }
        
        this.lastTransPanel = this.isCustomer? getCustInvoiceTabView(false, lastTransPanelId, '', undefined, true) : getVendorInvoiceTabView(false, lastTransPanelId, '', undefined, true);
        
        this.NorthForm.doLayout();
        this.southPanel.doLayout();
        
        this.DueDate.on('blur',this.dueDateCheck,this);
        this.billDate.on('change',this.onDateChange,this);
        
        this.setTransactionNumber();
        
        WtfComMsgBox(29,4,true);
        this.isCustomer? chkcustaccload() : chkvenaccload();
        
        this.ajxUrl = "CommonFunctions/getInvoiceCreationJson.do";
        var params = {
            transactiondate: transdate,
            loadtaxstore: true,
            loadcurrencystore: true,
            loadtermstore: true
        }
        Wtf.Ajax.requestEx({url:this.ajxUrl,params:params}, this, this.successCallback, this.failureCallback);
        
        this.currencyStore.on('load',this.changeTemplateSymbol,this);
        
        if (!this.isCustBill && !this.isCustomer && !this.isOrder && !this.isEdit && !this.copyInv) {
            this.ProductGrid.on('pricestoreload', function(arr) {
                if (!this.isExpenseInv) {
                    this.datechange = 1;
                    this.changeCurrencyStore(arr);
                }
            }, this);
       } else if (!this.isCustBill && !this.isExpenseInv) {
           this.Grid.on('pricestoreload', function(arr) {
               this.datechange = 1;
               this.changeCurrencyStore(arr);
           }.createDelegate(this),this);
       }
    },
    
    onDateChange: function(a,val,oldval) {
        this.val = val;
        this.oldval = oldval;
        this.externalcurrencyrate = 0;
        this.custdatechange = true;
        this.datechange=1;
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
        if (!(this.isCustBill || this.isExpenseInv)) {
            var affecteduser = this.Name.getValue();
        } else {
            this.changeCurrencyStore();
            this.updateSubtotal();
            this.applyCurrencySymbol();
            
            var totalamount = WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol)
            var totalAmtInBase = WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol());
            this.tplSummary.overwrite(this.southCalTemp.body,{totalamount:totalamount,totalAmtInBase:totalAmtInBase});
        }
    },
    
    successCallback: function(response) {
        if (response.success) {
            this.currencyStore.loadData(response.currencydata);
            
            if (this.currencyStore.getCount() < 1) {
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2);
            } else {
                this.isCurrencyLoad = true;
                var index = this.getCurrencySymbol();
                this.applyTemplate(this.currencyStore,index);
            }
            
            if (this.isEdit || this.copyInv) {
                this.loadRecord();
            }
            
            if (this.productOptimizedFlag == Wtf.Show_all_Products) {
                this.Grid.productComboStore.on("load",function() {
                    this.loadDetailsGrid();
                }, this);
            }
            
            if (this.isRFQ && !this.isEdit && !this.copyInv) {
                 this.POStore.load({params: {isFixedAssetRFQ: true,isFixedAsset:true},callback:this.populateData,scope:this});
            }
            
            this.hideLoading();           
            this.loadDetailsGrid();
        }
    },
    populateData: function() {
        WtfGlobal.populateCustomData(this.PR_IDS,this.POStore,this.tagsFieldset);
    },

    failureCallback: function(response) {
         this.hideLoading();
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg2")+response.msg], 2);
    },
    
    loadDetailsGrid: function() {
        if ((this.isRequisition && (this.isEdit || this.copyInv)) || this.viewGoodReceipt) {
            this.loadProductGridForRequisition();
        }
        
        if (this.isRFQ) {
            this.loadProductGridForRFQ();
        }
    },
    
    hideLoading: function() {
        Wtf.MessageBox.hide();
    },
    
    applyTemplate: function(store,index) {
        var editable = this.Currency.getValue() != WtfGlobal.getCurrencyID() && this.Currency.getValue()!=""
        var exchangeRate = store.getAt(index).data['exchangerate'];
        
        if (this.externalcurrencyrate > 0) {
            exchangeRate = this.externalcurrencyrate;
        } else if (this.isEdit|| this.copyInv && this.record.data.externalcurrencyrate && !this.custdatechange) {
            var externalCurrencyRate = this.record.data.externalcurrencyrate - 0;
            if (externalCurrencyRate > 0) {
                exchangeRate = externalCurrencyRate;
            }
        }
        this.externalcurrencyrate = exchangeRate;
        var revExchangeRate = 1 / (exchangeRate - 0);
        revExchangeRate = (Math.round(revExchangeRate * Wtf.Round_Off_Number)) / Wtf.Round_Off_Number;
        
        this.southCenterTplSummary.overwrite(this.southCenterTpl.body,{foreigncurrency:store.getAt(index).data['currencyname'],exchangerate:exchangeRate,basecurrency:WtfGlobal.getCurrencyName(),editable:editable,revexchangerate:revExchangeRate});
    },

    changeCurrencyStore: function(pronamearr) {
        this.pronamearr = pronamearr;
        var currency = this.Currency.getValue();
        if(this.val == "") this.val = this.billDate.getValue();
        if (currency != "" || this.custChange) {
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val),tocurrencyid:this.Currency.getValue()}});
        } else {
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val)}});
        }
    },
    
    changeTemplateSymbol: function() {
        if (this.loadCurrFlag && Wtf.account.companyAccountPref.currencyid) {
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            this.loadCurrFlag = false;
        }
        
        /* if date of without inventory changes. price store will not be loaded in this case.[PS]*/
        if (this.isCustBill || this.isExpenseInv) {
            if (this.currencyStore.getCount() == 0) {
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.billDate.setValue("");
            } else {
                this.updateFormCurrency();
            }
        }
        
        /*if date of withinventory changes. After price store load. [PS]   */
//        alert(this.datechange+"---"+this.pronamearr.length)

        if (this.datechange == 1) {
            var str = "";
            if (this.pronamearr !=undefined && this.pronamearr.length > 0) {
                str += this.pronamearr[0];
                for (var i=1;i<this.pronamearr.length;i++) {
                    str += "</b>, <b>" + this.pronamearr[i];
                }
            }
            var recResult=WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid");
            if(this.Currency.getValue() !="" && recResult == null){
//            if(this.currencyStore.getCount()==0){
                this.Currency.setValue("");
                callCurrencyExchangeWindow();
                str =WtfGlobal.getLocaleText("acc.field.andpriceof")+ " <b>" + str + "</b>";
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleasesetthecurrencyrate")+" "+str+WtfGlobal.getLocaleText("acc.field.fortheselecteddate")+"<b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.billDate.setValue("");
            } else {
                this.updateFormCurrency();
                if (this.pronamearr != undefined && this.pronamearr.length > 0) {
                    str = WtfGlobal.getLocaleText("acc.field.priceof")+"<b>"+str+"</b>";
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleasesetthe")+" "+str+WtfGlobal.getLocaleText("acc.field.fortheselecteddate")+"<b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                }
                this.Grid.pronamearr = [];
                this.updateFormCurrency();
            }
            
            this.datechange = 0;
            this.updateSubtotal();
            this.applyCurrencySymbol();
            var totalamount = WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol);
            var totalAmtInBase = WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol());
            this.tplSummary.overwrite(this.southCalTemp.body,{totalamount:totalamount,totalAmtInBase:totalAmtInBase});
        }
        
//        if (this.currencychanged) {
//            if (this.currencyStore.getCount() < 1) {
//                callCurrencyExchangeWindow();
//                
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+"<b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
//                this.Currency.setValue("");
//            } else {
//                this.updateFormCurrency();
//            }
//            this.currencychanged = false;
//        }
        
        /*when customer/vendor name changes [PS]*/
        if (this.custChange) {
            if (this.currencyStore.getCount() == 0) {
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.Name.setValue("");
            } else {
                this.Currency.setValue(this.currencyid)
                this.updateFormCurrency();
            }
            this.custChange = false;
        }
        this.Grid.pronamearr = [];
    },
    
    updateFormCurrency: function() {
        this.applyCurrencySymbol();
        this.tplSummary.overwrite(this.southCalTemp.body,{totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol())});
    },

    getCurrencySymbol: function() {
        var index = null;
//        this.currencyStore.clearFilter(true); //ERP-9962
        var FIND = this.Currency.getValue();
        if (FIND == "" || FIND == undefined || FIND == null) {
            FIND = WtfGlobal.getCurrencyID();
        }
        index = this.currencyStore.findBy(function(rec) {
            var parentname = rec.data['currencyid'];
            if (parentname == FIND) {
                return true;
            } else {
                return false
            }
        });
        
        this.currencyid = this.Currency.getValue();
        return index;
    },

    applyCurrencySymbol: function() {
        var index = this.getCurrencySymbol();
        var rate = this.externalcurrencyrate;
        if (index >= 0) {
            rate = (rate==""? this.currencyStore.getAt(index).data.exchangerate : rate);
            this.symbol = this.currencyStore.getAt(index).data.symbol;
            this.Grid.setCurrencyid(this.currencyid,rate,this.symbol,index);
            this.applyTemplate(this.currencyStore,index);
        }
        return this.symbol;
    },
    
    loadProductGridForRFQ : function() {
        if (this.record) {
            this.billid = this.record.data.billid;   
            this.Grid.getStore().proxy.conn.url = "ACCPurchaseOrderCMN/getRFQRows.do";
        } else {
            this.billid = this.PR_IDS;
            this.Grid.getStore().proxy.conn.url = "ACCPurchaseOrderCMN/getRequisitionRows.do";   
        }
        this.Grid.getStore().load({params:{bills:this.billid,isFixedAsset:this.isFixedAsset,copyInv:this.copyInv,isFixedAssetRFQ:true}});     // at 'edit'/'view' of form
    },

    loadProductGridForRequisition: function() {
        this.billid = this.record.data.billid;
        this.Grid.getStore().proxy.conn.url = "ACCPurchaseOrderCMN/getRequisitionRows.do";
        this.Grid.getStore().load({params:{bills:this.billid,isFixedAsset:this.isFixedAsset,copyInv:this.copyInv}});     // at 'edit'/'view' of form
    },
    
    addGrid: function() {
//        if (Optimized_CompanyIds.indexOf(companyid) != -1) {
//            this.ProductGrid = new Wtf.account.ProductDetailsGridOptimized()({
//                height: 600,
//                layout: 'fit',
//                title: WtfGlobal.getLocaleText("acc.invoice.inventory"), // 'Inventory',
//                border: true,
//                isRequisition: this.isRequisition,
//                isRFQ: this.isRFQ,
//                isQuotation: false,
//                helpedit: this.heplmodeid,
//                id: this.id + "editproductdetailsgrid",
//                viewConfig: {forceFit: true},
//                isCustomer: this.isCustomer,
//                currencyid: this.currencyid,
//                fromOrder: true,
//                isOrder: this.isOrder,
//                forceFit: true,
//                loadMask: true,
//                parentObj: this
//            });
//        } else 
        {
            this.ProductGrid = new Wtf.account.FixedAssetPurchaseRequisitionGrid({
                height: 600,
                layout: 'fit',
                title: WtfGlobal.getLocaleText("acc.invoice.inventory"), // 'Inventory',
                border: true,
                isRequisition: this.isRequisition,
                isRFQ: this.isRFQ,
                isQuotation: false,
                moduleid: this.moduleid,
                helpedit: this.heplmodeid,
                id: this.id + "editproductdetailsgrid",
                viewConfig: {forceFit: true},
                isCustomer: this.isCustomer,
                currencyid: this.currencyid,
                fromOrder: true,
                isOrder: this.isOrder,
                forceFit: true,
                loadMask: true,
                parentObj: this,
                isFixedAsset: this.isFixedAsset
            });
        }
        
        this.ProductGrid.on("productselect", this.loadTransStore, this);
        this.ProductGrid.on("productdeleted", this.removeTransStore, this);

//        if (Optimized_CompanyIds.indexOf(companyid) != -1) {
//            this.Grid = new Wtf.account.ProductDetailsGridOptimized({
//                height: 330,
//                cls: 'gridFormat',
//                layout: 'fit',
//                parentCmpID: this.id,
//                id: this.id + "editproductdetailsgrid",
//                viewConfig: {forceFit: true},
//                record: this.record,
//                isRequisition: this.isRequisition,
//                isRFQ: this.isRFQ,
//                isQuotation: false,
//                isCustomer: this.isCustomer,
//                currencyid: this.currencyid,
//                fromPO: this.isOrder,
//                fromOrder: true,
//                isOrder: this.isOrder,
//                forceFit: true,
//                editTransaction: this.isEdit,
//                loadMask: true,
//                parentObj: this
//            });
//        } else 
        {
            this.Grid = new Wtf.account.FixedAssetPurchaseRequisitionGrid({
                height: 330,
                cls: 'gridFormat',
                layout: 'fit',
                parentCmpID: this.id,
                id: this.id + "editproductdetailsgrid",
                viewConfig: {forceFit: false},
                record: this.record,
                moduleid: this.moduleid,
                isRequisition: this.isRequisition,
                isRFQ: this.isRFQ,
                isQuotation: false,
                isCustomer: this.isCustomer,
                currencyid: this.currencyid,
//                fromPO: this.isOrder,
                fromOrder: true,
                isOrder: this.isOrder,
                forceFit: false,
                editTransaction: this.isEdit,
                copyInv: this.copyInv,
                loadMask: true,
                parentObj:this,
                readOnly: this.readOnly,
                isViewTemplate : this.isViewTemplate,
                isFixedAsset: this.isFixedAsset
            });
        }
        this.Grid.on("productselect", this.loadTransStore, this);
        this.Grid.on("productdeleted", this.removeTransStore, this);
       
        this.NorthForm.on('render',this.setDate,this);
       
        if (this.readOnly) {
            this.disabledbutton(); // disabled button in view case
        }
       
        this.Grid.on('datachanged',this.updateSubtotal,this);
        this.Grid.getStore().on('load',function(store) {
            this.updateSubtotal();
            this.Grid.addBlank(store);
            if (this.isEdit|| this.copyInv) {
                if (this.record.data.externalcurrencyrate != undefined) {
                    this.externalcurrencyrate = this.record.data.externalcurrencyrate;
                    this.updateFormCurrency();
                }
            }
        }.createDelegate(this), this);
    },
    
    disabledbutton: function() {
        this.sequenceFormatCombobox.setDisabled(true);  
        this.Number.setDisabled(true);  
        this.billDate.setDisabled(true);
        this.Name.setDisabled(true);
        this.DueDate.setDisabled(true); 
        this.Currency.setDisabled(true);
        this.Memo.setDisabled(true);
    },
    
    disableComponent:function(){
        this.disabledbutton();
        if(this.saveBttn){
            this.saveBttn.disable();
        }
        if(this.savencreateBttn){
            this.savencreateBttn.disable();
        }
        if(Wtf.getCmp("posttext" + this.id)){
            Wtf.getCmp("posttext" + this.id).disable();
        }

        if(Wtf.getCmp("showaddress" + this.id)){
            Wtf.getCmp("showaddress" + this.id).disable(); 
        } 

        if(this.Grid){
            var GridStore = this.Grid.getStore();
            var count2 = GridStore.getCount();
            var lastRec2 = GridStore.getAt(count2-1);
            GridStore.remove(lastRec2);
        }
        if(this.GridPanel){
            if(this.modeName=="autocashpurchase" || this.modeName=="autogoodsreceipt"){
                this.ProductGrid.purgeListeners();
            }else{
                this.GridPanel.disable();   
            }

        }else{
            this.Grid.purgeListeners();
        }

        if(this.NorthForm){
            this.NorthForm.disable();
        }

        if(this.SouthForm){
            this.SouthForm.disable(); 
        }
    },
    
    updateSubtotal: function(a,val) {
        this.isClosable = false; // Set Closable flag after updating grid data
        this.applyCurrencySymbol();
        this.tplSummary.overwrite(this.southCalTemp.body,{totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol())});
    },
    
    calTotalAmount: function() {
        var subtotal = this.Grid.calSubtotal();
        return subtotal;
    },
    
    calTotalAmountInBase: function() {
        var subtotal = this.Grid.calSubtotal();
        var returnValInOriginalCurr = subtotal;
        returnValInOriginalCurr = returnValInOriginalCurr * this.getExchangeRate();
        return returnValInOriginalCurr; 
    },
    
    getExchangeRate: function() {
        var index = this.getCurrencySymbol();
        var revExchangeRate = 0;
        if (index >= 0) {
            var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
            if (this.externalcurrencyrate > 0) {
                exchangeRate = this.externalcurrencyrate;
            }
            revExchangeRate = 1 / (exchangeRate);
            revExchangeRate = (Math.round(revExchangeRate * Wtf.Round_Off_Number)) / Wtf.Round_Off_Number;
        }
        return revExchangeRate;
    },
    
    save: function() {
        var incash = false;
        this.Number.setValue(this.Number.getValue().trim());
        
        if (this.NorthForm.getForm().isValid()) {
            
            var custFieldArr=this.tagsFieldset.createFieldValuesArray();
            var dimencustomfield="";
            if (custFieldArr.length > 0)
                dimencustomfield = JSON.stringify(custFieldArr);
            // Checking for deactivated products
            var inValidProducts=this.checkForDeActivatedProductsAdded();
            if(inValidProducts!=''){
                inValidProducts = inValidProducts.substring(0, inValidProducts.length-2);
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), 
                    msg: WtfGlobal.getLocaleText("acc.common.followingProductsAreDeactivated")+'</br>'+'<b>'+inValidProducts+'<b>',
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this,
                    scopeObj :this,
                    fn: function(btn){
                        if(btn=="ok"){
                            return;
                        }
                    }
                });
                return;
            }
            for (var i=0; i<this.Grid.getStore().getCount()-1; i++) { // excluding last row
                var quantity = this.Grid.getStore().getAt(i).data['quantity'];
                if (quantity=="" || quantity==undefined || quantity<=0) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.QuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                    return;
                }
                var rate = this.Grid.getStore().getAt(i).data['rate'];
                if ((rate==="" || rate==undefined || rate<0) && !this.isRFQ) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.RateforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.cannotbeempty")], 2);
                    return;
                }
            }
            
            var count = this.Grid.getStore().getCount();
            if (count <= 1) {
                WtfComMsgBox(33, 2);
                return;
            }
            
            // In Case of Fixed Asset OR Lease Fixed Asset Check external and internal quantities are equal or not
            if (this.isFixedAsset) {
                for (var i=0; i<this.Grid.getStore().getCount()-1; i++) { // exclude last row
                    var quantity = this.Grid.getStore().getAt(i).data['quantity'];
                    var productId = this.Grid.getStore().getAt(i).data['productid'];
                    var proRecord = WtfGlobal.searchRecord(this.Grid.productComboStore,productId,'productid');
                    
//                    if (proRecord.get('isAsset')) {
                        var assetDetails = this.Grid.getStore().getAt(i).data['assetDetails'];
                        
                        if (assetDetails == "" || assetDetails == undefined) {
                            WtfComMsgBox(['Information',WtfGlobal.getLocaleText("acc.field.PleaseProvideAssetDetailsforAssetGroup")+" "+"<b>"+this.Grid.getStore().getAt(i).data['productname']+"</b>"+"."],0);
                            return;
                        }
                        
                        var assetDetailArray = eval('(' + assetDetails + ')');
                    
                        if (assetDetailArray == null || assetDetailArray == undefined) {
                            WtfComMsgBox(['Information',WtfGlobal.getLocaleText("acc.field.PleaseProvideAssetDetailsforAssetGroup")+" "+this.Grid.getStore().getAt(i).data['productname']+"</b>"+"."],0);
                            return;
                        }
                        
                        if (quantity != assetDetailArray.length) {
                            WtfComMsgBox(['Information','Entered quantity does not match with the Asset Rows entered. Please give complete Asset Details for Asset Group '+this.Grid.getStore().getAt(i).data['productname']+'.'],0);
                            return;
                        }
                        
                        var rate = this.Grid.getStore().getAt(i).data['rate'];
                        
                        if (this.isCustomer) {
                            var assetDetailTotalSellingAmount = 0;
                            
                            for (var j=0;j<assetDetailArray.length;j++) {
                                assetDetailTotalSellingAmount+=parseFloat(assetDetailArray[j].sellAmount);
                                }
                            
                            var sellMsg = "Sell Amount";
                            if (this.isLeaseFixedAsset) {
                                sellMsg = "Leasing Amount";
                            }

                            var rateIntoQuantityVal = rate*quantity - assetDetailTotalSellingAmount;
                            rateIntoQuantityVal = (rateIntoQuantityVal<0)?(-1)*rateIntoQuantityVal:rateIntoQuantityVal;

                            if ((rateIntoQuantityVal > Wtf.decimalLimiterValue) && !this.isRFQ) { // due to java script rounding off problem
                                WtfComMsgBox(['Information','Rate entered is not equal to Asset Details total Sell Amount value  for Asset Group <b>'+this.Grid.getStore().getAt(i).data['productname']+'</b>'],0);
                                return;
                            }
                        } else {
                            var assetDetailTotalCost = 0;
                            for (var j = 0; j < assetDetailArray.length; j++) {
                                if (assetDetailArray[j].costInForeignCurrency !== "") {
                                    assetDetailTotalCost += parseFloat(assetDetailArray[j].costInForeignCurrency);
                                }
                            }

                            var rateQuantityVal = rate*quantity - assetDetailTotalCost;
                            rateQuantityVal = (rateQuantityVal<0)?(-1)*rateQuantityVal:rateQuantityVal;

                            if ((rateQuantityVal > Wtf.decimalLimiterValue) && !this.isRFQ) { // due to java script rounding off problem
                                WtfComMsgBox(['Information','Rate entered is not equal to Asset Details total Cost value  for Asset Group <b>'+this.Grid.getStore().getAt(i).data['productname']+'</b>'],0);
                                return;
                            }
                        }
//                    }
                }
            }
            
            incash = this.cash;
            var rec = this.NorthForm.getForm().getValues();
            rec.billdate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
            rec.customfield=dimencustomfield;
            
            this.ajxurl = "";
            if (this.isRequisition) {
                this.ajxurl = "ACCPurchaseOrder/saveRequisition.do";
            } else {
                this.ajxurl = "ACCPurchaseOrder/saveRFQ.do";
            }
            
            var detail = this.Grid.getProductDetails();
            var validLineItem = this.Grid.checkDetails(this.Grid);
            if (validLineItem != "" && validLineItem != undefined) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (WtfGlobal.getLocaleText("acc.msgbox.lineitem") + validLineItem)], 2);             
                return;
            }
            
            if (detail == undefined || detail == "[]") {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);  // "Product(s) details are not valid."
                return;
            }
            
            if (this.productOptimizedFlag == Wtf.Products_on_Submit ) {
                this.checklastproduct(rec,detail,incash,count);
            } else {
                this.showConfirmAndSave(rec,detail,incash);
            }
        } else {
            WtfComMsgBox(2, 2);
        }
    },
    
    checklastproduct: function(rec,detail,incash,count) {
        if (this.Grid.getStore().getAt(count-1).data['pid'] != "" && this.Grid.getStore().getAt(count-1).data['productid'] == "") {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.common.productWithSpecifiedId")+" "+this.Grid.getStore().getAt(count-1).data['pid']+" "+WtfGlobal.getLocaleText("acc.common.productDoesNotExistsOrInDormantState")+". "+WtfGlobal.getLocaleText("acc.accPref.productnotFoundonSave")+'</center>' ,function(btn) {
                if (btn == "yes") {
                    this.showConfirmAndSave(rec,detail,incash);
                } else {
                    return;
                }
            },this);
        } else {
            this.showConfirmAndSave(rec,detail,incash);
        }
    },
    
    checkLimit: function(rec,detail,incash) {
        if (!this.quotation && !this.isOrder && !this.cash) {
            if (rec!=null && rec!=undefined && this.calTotalAmount()!=null) {
                rec.totalSUM = this.calTotalAmount();
                Wtf.Ajax.requestEx({
                    url: "ACC" + this.businessPerson+"CMN/get" + this.businessPerson + "Exceeding" + (this.businessPerson == "Vendor"? "Debit" : "Credit") + "Limit.do",
                    params: rec                                                                                                                                            
                }, this, function(response) {
                    if (response.data && response.data.length > 0) {
                        var msg = (this.businessPerson=="Vendor"? "<center>"+WtfGlobal.getLocaleText("acc.cust.debitLimit") : "<center>"+WtfGlobal.getLocaleText("acc.cust.creditLimit")) + " " + WtfGlobal.getLocaleText("acc.field.forthis") + this.businessPerson + " " + WtfGlobal.getLocaleText("acc.field.hasreached") + "</center>" + "<br><br>";
                        var limitMsg = "";
                        for (var i=0; i< response.data.length; i++) {
                            var recTemp = response.data[i];
                            limitMsg = (recTemp.name == ""? "" : "<b>"+this.businessPerson+": </b>" + recTemp.name + ", ") + "<b>" + WtfGlobal.getLocaleText("acc.field.AmountDue1") + " </b>" + recTemp.amountDue + ", <b>" + (this.businessPerson=="Vendor"? WtfGlobal.getLocaleText("acc.cust.debitLimit") : WtfGlobal.getLocaleText("acc.cust.creditLimit")) + ": </b>" + recTemp.limit;
                            msg += limitMsg + "<br>";
                        }
                        msg += "<br>" + WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed") + "</center>";
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),msg, function(btn) {
                            if (btn != "yes") {
                                return;
                            }
                            this.showConfirmAndSave(rec,detail,incash);
                        },this);
                    } else {
                        this.showConfirmAndSave(rec,detail,incash);
                    }
                },function(response) {
                    this.showConfirmAndSave(rec,detail,incash);
                });                
            } else {
                this.showConfirmAndSave(rec,detail,incash);
            }   
        } else {
            this.showConfirmAndSave(rec,detail,incash);
        }
    },
    
    showConfirmAndSave: function(rec,detail,incash) {
//        var isRuleExist = false;
//        Wtf.Ajax.requestEx({
//            url: "ACCPurchaseOrderCMN/isRuleExistsForRequisition.do",
//            params: {
//                totalAmount: this.calTotalAmount()
//            }
//        }, this, function(response) {
//            if (response.success) {
//                isRuleExist = response.isRuleExist;
//                if (isRuleExist) {
                    this.finallySave(rec,detail,incash);
//                } else {
//                    this.checkForBudgetLimitActivated(rec,detail,incash);
//                }
//            }
//        }, this.genFailureResponse);
    },

    getDates: function(start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom) {
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        
        var fd = new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if (d < fd) {
            fd = new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        }
            
        if (start) {
            return fd;
        }

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    
    loadTransStore : function(productid) {
        if (this.Name.getValue() != "") {
            var customer = (this.businessPerson=="Vendor")? "" : this.Name.getValue();
            var vendor = (this.businessPerson=="Vendor")? this.Name.getValue() : "" ;
            
            if ((productid == undefined || productid == "") && this.Grid.getStore().getCount() > 0) {
                productid = this.Grid.getStore().getAt(0).get("productid");
            }
            
            if (productid) {
                this.lastTransPanel.productid = productid;
                this.lastTransPanel.Store.load({
                    params: {
                        start: 0,
                        limit: 5, 
                        prodfiltercustid: customer,
                        prodfilterventid: vendor,
                        productid: productid
                    }
                });
            }
        }
    },
    
    removeTransStore: function() {
        this.lastTransPanel.Store.removeAll();
    },
    
    updateData: function() {
        if (this.Grid) {
            this.Grid.affecteduser = this.Name.getValue();
        }
        
        this.loadTransStore();
        
        if (!(this.isCustBill || this.isExpenseInv || this.isEdit|| this.copyInv)) {
            if (!(this.fromPO && this.fromPO.getValue())) {
                var val = this.billDate.getValue();
            }
        }
    },
    
    updateDueDate: function(a,val) {
         this.NorthForm.getForm().setValues({duedate:this.billDate.getValue()});
        if (this.Grid) {
            this.Grid.billDate = this.billDate.getValue()
        }
    },
  
    genSuccessResponse: function(response) {
        
        if (response.success) {
           this.mailFlag=true;//This flag is used in Wtf.account.ClosablePanel component .mailFlag shows that at the time of creation of PR if we press sav button then whole component will be disabled and at close action of that tab no msg will be displayed.
           if(!(this.isEdit || this.copyInv)){
                this.lastTransPanel.Store.removeAll();
                this.symbol = WtfGlobal.getCurrencySymbol();
                this.currencyid = WtfGlobal.getCurrencyID();
                this.loadStore();
                this.currencyStore.load(); 
                this.externalcurrencyrate=0; // Reset external exchange rate for new Transaction.
                this.isClosable= true; // Reset Closable flag to avoid unsaved Message.
                Wtf.dirtyStore.product = true;
                WtfGlobal.resetCustomFields(this.tagsFieldset); //reset custom fields

            }else{
                this.disableComponent();
            }
            this.fireEvent('update',this);
            if(this.isRFQ){
                var title=this.titlel;
                if(Wtf.getCmp("assetRequestForQuotation")!=null && Wtf.getCmp("assetRequestForQuotation")!=undefined){//to refresh the grid 
                    Wtf.getCmp("assetRequestForQuotation").Store.on('load',function(){
                        WtfComMsgBox([title,response.msg],response.success*2+1); 
                    }, Wtf.getCmp("assetRequestForQuotation").Store, {
                        single : true
                    });
                    Wtf.getCmp("assetRequestForQuotation").Store.reload();//for submit
                }
            }
        if (Wtf.getCmp("FixedAssetPurchaseRequisitionListEntry")!=null && Wtf.getCmp("FixedAssetPurchaseRequisitionListEntry")!=undefined) { // to refresh the grid of PRReport
                var title=this.titlel;   //scope not available for title in on load function of store
            Wtf.getCmp("FixedAssetPurchaseRequisitionListEntry").Store.on('load', function(){
                WtfComMsgBox([title,response.msg],response.success*2+1);
            },Wtf.getCmp("FixedAssetPurchaseRequisitionListEntry").Store, {   //ERP-17243
                single : true
            }); 
//            Wtf.getCmp("FixedAssetPurchaseRequisitionListEntry").Store.reload(); // for save as draft

               if (Wtf.getCmp("FixedAssetPurchaseRequisitionListEntry") != undefined) {
                   Wtf.getCmp("FixedAssetPurchaseRequisitionListEntry").Store.load({
                       params: {
                           start: 0,
                           limit: Wtf.getCmp("FixedAssetPurchaseRequisitionListEntry").pP.combo != undefined ? Wtf.getCmp("FixedAssetPurchaseRequisitionListEntry").pP.combo.value : 30
                       }
                   });
               }
            Wtf.getCmp("assetPurchaseRequisitionListPending").Store.reload(); // for submit
        }
        /*To refresh the grid of Drafted report*/
        if(Wtf.getCmp("draftedFixedAssetPurchaseRequisitionList") != undefined && Wtf.isAutoRefershReportonDocumentSave){
            Wtf.getCmp("draftedFixedAssetPurchaseRequisitionList").Store.load({
                params: {
                    start: 0,
                    limit: Wtf.getCmp("draftedFixedAssetPurchaseRequisitionList").pP.combo!= undefined ? Wtf.getCmp("draftedFixedAssetPurchaseRequisitionList").pP.combo.value : 30
                }
            });
        }
                    
            if (this.productOptimizedFlag == Wtf.Show_all_Products && !this.isCustBill) { // Store load only in case of Show all Products
                Wtf.productStoreSales.reload();
                Wtf.productStore.reload(); // Reload all product information to reflect new quantity, price etc
            }
        }else if (response.isDuplicateExe) {
            Wtf.MessageBox.hide();
            this.newnowin = new Wtf.Window({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                closable: true,
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                width: 330,
                autoHeight: true,
                modal: true,
                bodyStyle: "background-color:#f1f1f1;",
                closable:false,
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
                                fieldLabel: this.isRFQ?WtfGlobal.getLocaleText("acc.RFQ.newrfq"):WtfGlobal.getLocaleText("acc.requisition.newrequisitionno"),
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
                                        this.Number.setValue(this.newdono.getValue());
                                        this.save();
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
        } else if ((this.moduleid == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId) && response.isAccountingExe) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
        }
        else {
            if (response.msg && response.msg != "") {
                WtfComMsgBox([this.titlel, response.msg], response.success * 2 + 1);
            }
        }
    },

    genFailureResponse: function(response) {
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    loadStore: function() {
        var formatid = this.sequenceFormatCombobox.getValue();    
        this.Grid.getStore().removeAll();
        this.setTransactionNumber();
        this.NorthForm.getForm().reset();
        this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
        
        this.Grid.getStore().removeAll();
        this.Grid.symbol = undefined; // To reset currency symbol. BUG Fixed #16202
        this.Grid.updateRow(null);
        this.resetForm = true;
        this.sequenceFormatStore.reload();
        this.sequenceFormatCombobox.setValue(formatid);
        this.getNextSequenceNumber(this.sequenceFormatCombobox);
        this.updateSubtotal();
        
        this.currencyStore.load({params:{tocurrencyid:WtfGlobal.getCurrencyID(),mode:201,transactiondate:WtfGlobal.convertToGenericDate(new Date())}});
        this.currencyStore.on("load",function(store) {
            if (this.resetForm) {
                if (this.currencyStore.getCount() < 1) {
                    callCurrencyExchangeWindow();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2); //"Please set Currency Exchange Rates"
                } else {
                    this.isCurrencyLoad=true;
                    this.applyCurrencySymbol();
                    this.isClosable= true;
                    this.resetForm = false;
                }
            }
        },this);      
    },

    setDate: function() {
        if (!this.isEdit) {
            this.billDate.setValue(new Date());
//            this.billDate.setValue(Wtf.serverDate);
            this.DueDate.setValue(Wtf.serverDate);
        }
    },
    
    getNextSequenceNumber: function(a,val) {
        if (!(a.getValue() == "NA")) {
            WtfGlobal.hideFormElement(this.Number);
            var rec = WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
            var oldflag = rec!=null? rec.get('oldflag') : true;
            this.setTransactionNumber(true);
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/getNextAutoNumber.do",
                params: {
                    from:(this.fromnumber!=undefined) ? this.fromnumber : 1,
                    sequenceformat: a.getValue(),
                    oldflag: oldflag
                }
            }, this,function(resp) {
                if (resp.data == "NA") {
                    WtfGlobal.showFormElement(this.Number);
                    this.Number.reset();
                    this.Number.enable();
                } else {
                    this.Number.setValue(resp.data); 
                    this.Number.disable();
                    WtfGlobal.hideFormElement(this.Number);
                }
            });
        } else {
            WtfGlobal.showFormElement(this.Number);
            this.Number.reset();
            this.Number.enable();
        }
    },
    
    setTransactionNumber: function(isSelectNoFromCombo) {
        var format = "", temp2 = '';
        
        if (this.isRequisition) {
            format = Wtf.account.companyAccountPref.autorequisition;
            temp2 = Wtf.autoNum.Requisition;
        } else if (this.isRFQ) {
            format = Wtf.account.companyAccountPref.autorequestforquotation;
            temp2 = Wtf.autoNum.RFQ;
        }
        
        if (isSelectNoFromCombo) {
            this.fromnumber = temp2;
        } else if (format && format.length > 0) {
            WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit)this.Number.setValue(resp.data)}, this);
        }
    },
    
    dueDateCheck: function() {
        if (this.DueDate.getValue().getTime() < this.billDate.getValue().getTime()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.msg11")], 2); // "The Due Date should be greater than the Order Date."
            this.DueDate.setValue(this.billDate.getValue());
        }
    },

    initForClose: function() {
        this.cascade(function(comp) {
            if (comp.isXType('field')) {
                comp.on('change', function(){this.isClosable=false;},this);
            }
        },this);
    },
    
    finallySave: function(rec,detail,incash) {
        var promptmessage = "";
        if (this.isSaveDraftRecord && this.sequenceFormatCombobox.getValue() == "NA" && (this.moduleid==Wtf.Acc_Purchase_Requisition_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId)) {
            promptmessage = WtfGlobal.getLocaleText("acc.draft.saveTheRecordWith.NA.sequenceFormat");
        } else if(this.isDraft){
            promptmessage = WtfGlobal.getLocaleText("acc.invoice.msg14");
        } else {
            promptmessage = WtfGlobal.getLocaleText("acc.invoice.msg7");
        }
        Wtf.MessageBox.confirm(this.isDraft ? WtfGlobal.getLocaleText("acc.common.saveasdraft") : WtfGlobal.getLocaleText("acc.common.savdat"),promptmessage, function(btn) {
            if (btn != "yes") {
                return;
            }
            if (this.isExpenseInv) {
                rec.expensedetail = detail;
                rec.isExpenseInv = this.isExpenseInv;
            } else {
                rec.detail = detail;
            }
            
            this.msg = WtfComMsgBox(27,4,true);
            rec.subTotal = this.Grid.calSubtotal();
            this.applyCurrencySymbol();
            rec.currencyid = this.Currency.getValue();
            rec.externalcurrencyrate = this.externalcurrencyrate;
            rec.number = this.Number.getValue();
            rec.duedate = WtfGlobal.convertToGenericDate(this.DueDate.getValue());
            rec.billdate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
            rec.shipdate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
            rec.invoiceid = this.copyInv? "" : this.billid;
            rec.mode = (this.isOrder? (this.isCustBill? 51:41) : (this.isCustBill? 13:11));
            rec.incash = incash;
            rec.isdraft = this.isDraft;
            this.sequenceFormatStore.clearFilter(true);
            var seqFormatRec = WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
            rec.seqformat_oldflag = seqFormatRec!=null? seqFormatRec.get('oldflag') : false;
            rec.sequenceformat = this.sequenceFormatCombobox.getValue();
            rec.isEdit = this.isEdit;
            rec.copyInv = this.copyInv;
            rec.isFixedAsset = this.isFixedAsset;
            rec.isSaveDraftRecord = this.isSaveDraftRecord; //SDP-13487
	    rec.isAutoSeqForEmptyDraft = this.isAutoSeqForEmptyDraft;   //SDP-13927 : To identify Old Record No. and Auto Generated No while saving Draft Record in Edit Mode.
            rec.isSequenceFormatChangedInEdit = this.isSequenceFormatChangedInEdit;
            Wtf.Ajax.requestEx({
                url: this.ajxurl,
                params: rec
            }, this, this.genSuccessResponse, this.genFailureResponse);
        }, this);
    },
    
    checkForBudgetLimitActivated: function(rec,detail,incash) {
        this.BudgetSetForDepartment = 0;
        this.BudgetSetForDepartmentAndProduct = 1;
        this.BudgetSetForDepartmentAndProductCategory = 2;
        
        if (Wtf.account.companyAccountPref.activatebudgetingforPR) { // Approval for budgeting amount is activated
            var budgetType = Wtf.account.companyAccountPref.budgetType;
            if (budgetType == this.BudgetSetForDepartment) { // If budgeting is applied upon Department
                this.checkIfBugetLimitExceedingForDepartment(rec,detail,incash);
            } else if (budgetType == this.BudgetSetForDepartmentAndProduct) { // If budgeting is applied upon Department and specific product
                this.checkIfBugetLimitExceedingForDepartmentAndProduct(rec,detail,incash);
            } else { // If budgeting is applied upon Department and specific category of product
                this.finallySave(rec,detail,incash);
            }
        } else { // Approval flow not activated for budgeting amount
            this.finallySave(rec,detail,incash);
        }
    },
    
    getAllProductAndRate: function() {
        var storeLength = this.Grid.getStore().getCount() - 1;
        var rec;
        var productDetails = [];
        for (var i=0; i<storeLength; i++) {
            var rowObject = new Object();
            rec = this.Grid.getStore().getAt(i);
            rowObject['productId'] = rec.data.productid;
            rowObject['amount'] = rec.data.amount;
            productDetails.push(rowObject);
        }
        return productDetails;
    },
    
    checkIfBugetLimitExceedingForDepartmentAndProduct: function(rec,detail,incash) {
        var productDetails = this.getAllProductAndRate();
                 
        Wtf.Ajax.requestEx({
            url: "ACCPurchaseOrderCMN/checkIfBugetLimitExceeding.do",
            params: {
                productDetails: JSON.stringify(productDetails),
                budgetingType: this.BudgetSetForDepartmentAndProduct,
                requisitionDate: WtfGlobal.convertToGenericDate(this.billDate.getValue()),
                currencyID: this.Currency.getValue()
            }
        }, this, function(response) {
            if (response.success) {
                var isBudgetExceeding = response.isBudgetExceeding;

                if (isBudgetExceeding) {
                    if (Wtf.account.companyAccountPref.budgetwarnblock == 0) { // Ignore case
                        this.finallySave(rec,detail,incash);
                    } else if (Wtf.account.companyAccountPref.budgetwarnblock == 1) { // Warn case

                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.budgetIsExceedingDoYouWantToContinue"), function(btn) {
                            if (btn == "yes") {
                                this.finallySave(rec,detail,incash);
                            } else {
                                return;
                            }
                        }, this);

                    } else if (Wtf.account.companyAccountPref.budgetwarnblock == 2) { // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),  WtfGlobal.getLocaleText("acc.field.budgetIsExceedingSoYouCannotProceed")], 2);
                        return;
                    }
                } else {
                    this.finallySave(rec,detail,incash);
                }
            }
        }, this.genFailureResponse);
    },
    
    checkIfBugetLimitExceedingForDepartment: function(rec,detail,incash) {
                 
        Wtf.Ajax.requestEx({
            url: "ACCPurchaseOrderCMN/checkIfBugetLimitExceeding.do",
            params: {
                requisitionTotalAmount: this.calTotalAmount(),
                budgetingType: this.BudgetSetForDepartment,
                requisitionDate: WtfGlobal.convertToGenericDate(this.billDate.getValue()),
                currencyID: this.Currency.getValue()
            }
        }, this, function(response) {
            if (response.success) {
                var isBudgetExceeding = response.isBudgetExceeding;

                if (isBudgetExceeding) {
                    if (Wtf.account.companyAccountPref.budgetwarnblock == 0) { // Ignore case
                        this.finallySave(rec,detail,incash);
                    } else if (Wtf.account.companyAccountPref.budgetwarnblock == 1) { // Warn case

                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.budgetIsExceedingDoYouWantToContinue"), function(btn) {
                            if (btn == "yes") {
                                this.finallySave(rec,detail,incash);
                            } else {
                                return;
                            }
                        }, this);

                    } else if (Wtf.account.companyAccountPref.budgetwarnblock == 2) { // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),  WtfGlobal.getLocaleText("acc.field.budgetIsExceedingSoYouCannotProceed")], 2);
                        return;
                    }
                } else {
                    this.finallySave(rec,detail,incash);
                }
            }
        }, this.genFailureResponse);
    },
    checkForDeActivatedProductsAdded:function(){
        var invalidProducts='';
        if(!this.isEdit){ // Create New
            invalidProducts = this.checkDeactivatedProductsInGrid();
        }
        return invalidProducts;
    },
    checkDeactivatedProductsInGrid :function(){
        var inValidProducts=''
        var rec = null;
        var productId = null;
        var productRec = null;
        for(var count=0;count<this.Grid.store.getCount();count++){
            rec = this.Grid.store.getAt(count);
            productId = rec.data.productid;
            if(productId!= undefined && productId != null && productId != ''){
                    productRec = WtfGlobal.searchRecord(this.Grid.productComboStore, productId, "productid");
                if(productRec && (productRec.data.hasAccess === false)){
                    inValidProducts+=productRec.data.productname+', ';
                }
            }    
        }
        return inValidProducts; // List of deactivated products
    },
     /*
     * SDP-13923
     * This function has been used to check whether user has changed sequence format in Edit case of draft.
     * If user changes the sequence format from "NA" to Auto-Sequence Format then 'this.isSequenceFormatChangedInEdit' flag will be true and this flag has used on java side.
     */
    sequenceFormatChanged : function(combo, newval, oldval) {
        if (this.isEdit && (this.isDraft||this.record.data.isDraft) && (this.moduleid===Wtf.Acc_Purchase_Requisition_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId)){
            if (oldval != newval && newval != "NA") {
                this.isSequenceFormatChangedInEdit = true;
                this.isAutoSeqForEmptyDraft = true;
                this.getNextSequenceNumber(combo);
            } else {
                this.isSequenceFormatChangedInEdit = false;
            }
        }        
    } 
});
