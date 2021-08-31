/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.DbsAccountsMappingWindow = function (config) {
    this.isReadOnly = config.isReadOnly;
    this.bttnArr = [];
    this.itemsArr = [];
    this.saveBttn = new Wtf.Toolbar.Button({//Save button
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
        tooltip: WtfGlobal.getLocaleText("acc.common.savdat"),
        scope: this,
        handler: this.saveBttnHandler
    });

    if (!this.isReadOnly) {
        this.bttnArr.push(this.saveBttn);
    }

    this.cancelBttn = new Wtf.Toolbar.Button({//Cancel button
        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
        tooltip: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
        scope: this,
        handler: function () {
            this.close();//close the window
        }
    });
    this.bttnArr.push(this.cancelBttn);

    this.creteBankDetailsGrid();
     this.Store.on('beforeload', this.setParametersBeforeLoadingStore, this);
    this.Store.on('load', this.handleStoreOnLoad, this);

    Wtf.apply(this, {
        items: this.itemsArr,
        buttons: this.bttnArr
    }, config);
    Wtf.account.DbsAccountsMappingWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.DbsAccountsMappingWindow, Wtf.Window, {
    initComponent: function () {
        Wtf.account.DbsAccountsMappingWindow.superclass.initComponent.call(this);
    },
    onRender: function (config) {
        this.Store.load();
        Wtf.account.DbsAccountsMappingWindow.superclass.onRender.call(this, config);
    },
    saveBttnHandler: function () {
        var recData = "";
        var arr = [];

        //check is there duplicate Bank Account Mapped in window
        var isDuplicate = false;
        var duplicateval = ", ";
        var  bankID = "";
        var count = this.Store.data.items.length;
        for (var i = 0; i < count; i++) {
                arr.push(i);
            var outerAssetID = this.Store.getAt(i).data['deskeraaccount'];
            bankID = this.Store.getAt(i).data[Wtf.integration.bankid];
            for (var j = i + 1; j < count; j++) {
                var innerAssetID = this.Store.getAt(j).data['deskeraaccount'];
                if (outerAssetID == innerAssetID) {
                    isDuplicate = true;
                    if (duplicateval.indexOf(", " + this.Store.getAt(j).data['deskeraaccountname'] + ",") == -1) {
                        duplicateval += this.Store.getAt(j).data['deskeraaccountname'] + ", ";//Add duplicate Deskera Account ID
                    }
                }
            }
        }
        if (isDuplicate) {
            duplicateval = duplicateval.substring(2, (duplicateval.length - 2));
            WtfComMsgBox(['Information', 'Duplicate Deskera Account(s)[<b>' + duplicateval + '</b>] are Mapped.'], 0);
            return;
        }
        this.ajxUrl = "ACCAccount/saveBankAccountMappingDetails.do";
        recData = this.getJSONArray(arr);
        Wtf.Ajax.requestEx({
            url: this.ajxUrl,
            params: {
                data: recData,
                bankid:bankID
            }
        }, this, this.genSuccessResponse, this.genFailureResponse);

    },
    genSuccessResponse: function (response) {
        WtfComMsgBox([this.title, response.msg], 0);
        if (response.success) {
            this.close();
        }
    },
    genFailureResponse: function (response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    creteBankDetailsGrid: function () {

        var baseparam = {
            mode: 2,
            deleted: false,
            nondeleted: true,
            ignoreAssets: true,
            isForJE: true,
            ignoreCashAccounts: true,
            ignoreGSTAccounts: true,
            ignoreGLAccounts: true,
            ignorecustomers: true,
            ignorevendors: true
        };

        var extraFields = Wtf.account.companyAccountPref.accountsWithCode ? ['acccode', 'groupname'] : ['groupname'];
        var listWidth = Wtf.account.companyAccountPref.accountsWithCode ? 300 : 200;
        var ajaxURL = "ACCAccountCMN/getAccountsForCombo.do";
        this.deskeraAccountComboEditor = CommonERPComponent.createAccountPagingComboBox(this, 100, listWidth, 300, 30, extraFields, Object.assign(new Object(), baseparam), {}, ajaxURL);
        this.deskeraAccountComboEditor.addNewFn = this.openCOAWindow.createDelegate(this);
        this.deskeraAccountComboEditor.on('render', function () {
            if (this.deskeraAccountComboEditor.trigger) {
                this.deskeraAccountComboEditor.trigger.on('click', function () {
                    this.deskeraAccountComboEditor.footer.setVisible(true);
                }, this);
            }
        }, this);

        this.deskeraAccountComboEditor.on('beforeselect', function (combo, record, index) {
            return validateSelection(combo, record, index);
        }, this);
        
        this.deskeraAccountComboEditor.store.load({
            scope: this,
            callback: function () {
                if (this.grid.getView()) {
                    this.grid.getView().refresh();
                }
            }
        });
        this.Store = new Wtf.data.Store({
            url: "ACCAccount/getBankAccountMappingDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            })
        });

        this.grid = new Wtf.grid.EditorGridPanel({
            clicksToEdit: 1,
            width: '100%',
            store: this.Store,
            columns: [],
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            },
            loadMask: true
        });

        var northPanel = new Wtf.Panel({
            region: "north",
            height: 80,
            border: true,
            bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
            html: this.isReadOnly ? this.getTopHtml("View Deskera Bank Account Mapping Details", "", '../../images/deskera-logo.png', false, '0px 0px 0px 0px') : this.getTopHtml("Connect Bank Accounts", "Select Accounts from Deskera to Map to Bank Accounts.", '../../images/deskera-logo.png', false, '0px 0px 0px 0px')
        });
        this.itemsArr.push(northPanel);

        var centerPanelItemsArr = [];
        centerPanelItemsArr.push(this.grid);
        var centerPanel = new Wtf.Panel({
            region: "center",
            layout: "fit",
            border: true,
            items: centerPanelItemsArr
        });
        this.itemsArr.push(centerPanel);
    },
    cmbAccountComboRenderer: function (value, metadata, record, rowIndex, colIdex, store) {
        if (this.deskeraAccountComboEditor && this.deskeraAccountComboEditor.store) {
            var rec = WtfGlobal.searchRecord(this.deskeraAccountComboEditor.store, value, this.deskeraAccountComboEditor.valueField);
            if (rec == undefined || rec == null) {
                rec = WtfGlobal.searchRecord(this.deskeraAccountComboEditor.store, value, this.deskeraAccountComboEditor.displayField);
            }
            var accountcode = rec != undefined ? (rec.data['acccode'] ? "[" + rec.data['acccode'] + "] " : "") : "";
            return rec != undefined ? accountcode + rec.data[this.deskeraAccountComboEditor.displayField] : "";
        }
        return "";
    },
    openCOAWindow: function () {
        this.grid.stopEditing();
        callCOAWindow(false, null, "coaWin");
        Wtf.getCmp("coaWin").on("update", function () {
            this.accountStore.reload()
        }, this);
    },
    setParametersBeforeLoadingStore: function (s, o) {
        if (!o.params)
            o.params = {};
        o.params.isReadOnly = this.isReadOnly; 
    },
    handleStoreOnLoad: function (store) {
        var columns = [];
        Wtf.each(this.Store.reader.jsonData.columns, function (column) {

            if (column.dataIndex == "deskeraaccount") {
                if (!this.isReadOnly) {
                    column.editor = this.deskeraAccountComboEditor;
                    column.renderer = Wtf.account.companyAccountPref.accountsWithCode ? this.cmbAccountComboRenderer.createDelegate(this) : Wtf.comboBoxRenderer(this.deskeraAccountComboEditor);
                }
            }
            if (column.dataIndex == Wtf.integration.bankid) {
                column.renderer =this.logoRenderer.createDelegate(this);
            }
            columns.push(column);
        }, this);
        this.grid.getColumnModel().setConfig(columns);
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
        }
        this.grid.getView().refresh();
    },
    getJSONArray: function (arr) {
        return WtfGlobal.getJSONArray(this.grid, true, arr);
    },
    logoRenderer: function (value, metadata, record, rowIndex, colIdex, store) {
        var logo_path = "";
        if (this.Store) {
            var rec = WtfGlobal.searchRecord(this.Store, value, Wtf.integration.bankid);
            if (rec !== undefined || rec !== null) {
                var bankID = rec.data[Wtf.integration.bankid];
                logo_path = Wtf.bankIdLogoMapping[bankID];
            }
        }
        return logo_path;
    },
    
    getTopHtml: function (text, body, img, isgrid, margin) {
        if (isgrid === undefined)
            isgrid = false;
        if (margin === undefined)
            margin = '15px 0px 10px 10px';
        if (img === undefined || img == null) {
            img = '../../images/createuser.png';
        }
        var str = "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
                + "<div style='float:left;height:100%;width:auto;position:relative;'>"
                + "<img src = " + img + "  style = 'margin-top: 20px;margin-left: 10px;'></img>"
                + "</div>"
                + "<div style='float:left;height:100%;width:80%;position:relative;'>"
                + "<div style='font-size:12px;font-style:bold;float:left;margin:29px 4px 0px 10px;width:100%;position:relative;'><b>" + text + "</b></div>"
                + "<div style='font-size:10px;float:left;margin:2mm 0px 3mm 3mm;width:100%;position:relative;'>" + body + "</div>"
                + "</div>"
                + "</div>";
        return str;
    }

});
