/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
function callDiscountDetails(paramObj) {
    this.discountDetailsWin= new Wtf.account.DiscountDetailsWindow({
            renderTo: document.body,
            width: 950,
            readOnly: paramObj.readOnly,
            isLinkedTransaction: paramObj.isLinkedTransaction,
            height: 400,
            parentObj: paramObj.parentObj,
            resizable: false,
            modal: true,
            parentCmpScope : paramObj.parentCmpScope,
            parentRecord : paramObj.record,
            discountDetailsJson:paramObj.record.data.discountjson
      });
      this.discountDetailsWin.show();
}
Wtf.account.DiscountDetailsWindow = function(config) {
    this.butnArr = [];
    this.discountDetailsJson = config.discountDetailsJson;

    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.delete"), //'Delete',
        scope: this,
        hidden:(config.readOnly || config.isLinkedTransaction),
        handler: function() {
            this.isDeleteBtnClicked = true;
            this.deleteSelectedRecords();
        }
    }, {
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
        scope: this,
        handler: function() {
            this.close();
        }
    });
    Wtf.apply(this, {
        buttons: this.butnArr
    }, config);
    Wtf.account.DiscountDetailsWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.DiscountDetailsWindow, Wtf.Window, {
    height: 485,
    width: 800,
    modal: true,
    iconCls : 'pwnd deskeralogoposition',
    title:WtfGlobal.getLocaleText("acc.discountdetails.title"),
    onRender: function(config) {
        Wtf.account.DiscountDetailsWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        var msg = "";
        msg=msg+'<div style="font-size:14px; text-align:left; font-weight:bold; margin-top:1%;">'+WtfGlobal.getLocaleText("acc.field.Note")+': </div><div style="font-size:12px; text-align:left; margin-top:1%;">'+ WtfGlobal.getLocaleText("acc.discount.discoundetailsmsg")+'</div>';
        var isgrid = true;
        this.add({
            region: 'north',
            height: 115,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml('', msg, "../../images/accounting_image/price-list.gif", isgrid)
        }, this.centerPanel = new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan' + this.id,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls: 'bckgroundcolor',
            layout: 'fit',
            items: [this.grid],
            bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.store,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                    })
            })
        }))
    },
    createDisplayGrid: function() {
        this.accRec = Wtf.data.Record.create ([
            {
                name:'accountname',
                mapping:'accname'
            },
            {
                name:'accountid',
                mapping:'accid'
            }
            ,
            {
                name:'acccode',
                mapping:'acccode'
            },
            {
                name:'groupid',
                mapping:'groupid'
            },
            {
                name:'accounttype'
            }
        ]);

        this.dgStore = new Wtf.data.Store({
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                ignore:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                        root: "data"
                    }, this.accRec)
        });
        this.discountAccountCmb = new Wtf.form.ExtFnComboBox({
            name:'discountgiven',
            store:this.dgStore,
            hiddenName:'discountgiven',
            displayField:'accountname',
            valueField:'accountid',
            extraComparisionField:'acccode', // type ahead search on acccode as well.
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode']:[],
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 550 : 400,
            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText"),
            allowBlank:false
        });
        this.dgStore.load();
        this.discountTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid', type:'int'}, 'name'],
            data :[[1, 'Percentage'], [0, 'Flat']]
        });
        
        this.discountTypeCmb = new Wtf.form.ComboBox({
        store: this.discountTypeStore,
               name:'discounttypeid',
               displayField:'name',
               valueField:'typeid',
               mode: 'local',
               triggerAction: 'all',
               selectOnFocus:true,
               allowBlank:false
        });
        this.gridRec = Wtf.data.Record.create([
            {name: 'discountid'},
            {name: 'discountname'},
            {name: 'discountaccount'},
            {name: 'discountdescription'},
            {name: 'discounttype'},
            {name: 'discountvalue'},
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.gridRec),
            baseParams: {}
        });
        var discountDetailsJsonObj = {};
        if (this.discountDetailsJson != undefined && this.discountDetailsJson != "") {
            discountDetailsJsonObj = JSON.parse(this.discountDetailsJson)
        }
        this.dgStore.on('load', function () {
            if (discountDetailsJsonObj && discountDetailsJsonObj != undefined && discountDetailsJsonObj != "") {
                this.store.loadData(discountDetailsJsonObj);
            } 
        }, this);
        
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: false
        });
        
        this.colModel = new Wtf.grid.ColumnModel([this.sm, {
            header: WtfGlobal.getLocaleText("acc.invset.header.1"),
            dataIndex: 'discountname'
        }, {
            header: WtfGlobal.getLocaleText("acc.gridproduct.discription"),
            dataIndex: 'discountdescription'
        }, {
            header: WtfGlobal.getLocaleText("acc.field.DiscountType"),
            dataIndex: 'discounttype',
            renderer:Wtf.comboBoxRenderer(this.discountTypeCmb)
        }, {
            header: WtfGlobal.getLocaleText("acc.ra.value"),
            dataIndex: 'discountvalue'
        }, {
            header: WtfGlobal.getLocaleText("acc.je.acc"),
            dataIndex: 'discountaccount',
            renderer:Wtf.comboBoxRenderer(this.discountAccountCmb)
        }]);
        
        this.grid = new Wtf.grid.GridPanel({
            cm: this.colModel,
            store: this.store,
            sm: this.sm,
            height: 235,
            autoScroll: true,
            border: false,
            loadMask: true,
            viewConfig: {
                emptyText: WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec') + "<br>"),
                forceFit: true
            }
        });
        this.grid.on("render", function () {
            this.grid.getView().applyEmptyText();
        }, this);
    },
    submitSelectedRecords: function() {
        var selections = this.grid.getSelectionModel().getSelections();
        if (selections.length == 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.15")], 2);
            return;
        }
        this.close();
    },
    getSelectedRecords: function() {
        var arr = [];
        var selectionArray=this.grid.getSelectionModel().getSelections();
        for( var i=0;i<selectionArray.length;i++){
            arr.push(this.store.indexOf(selectionArray[i]));
        }
        var jarray = WtfGlobal.getJSONArrayWithoutEncodingNew(this.grid, true, arr);
        return jarray;
    },
    loadStore:function(){
        this.store.reload();
    },
    deleteSelectedRecords: function () {
        if (this.grid.getSelectionModel().hasSelection() == false) {
            WtfComMsgBox(34, 2);
            return;
        }
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.masterConfig.msg1"), function (btn) {
            if (btn == "yes") {
                var record = this.grid.getSelectionModel().getSelections();
                for (var cnt = 0; cnt < record.length; cnt++) {
                    this.store.remove(record[cnt]);
                }
                var recArr = [];
                this.store.each(function (rec) {
                    recArr.push(rec.data);
                }, this);
                this.parentCmpScope.afterDeletingDiscount({discountData: recArr}, this.parentRecord);
            }else{
                return;
            }
        }, this);
    },
    
});
