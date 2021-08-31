/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


Ext.define('ReportBuilder.view.ValidationWin', {
    extend: 'Ext.window.Window',
    xtype: 'validationwin',
    initComponent: function () {
        var rawData = this.store.getProxy().getReader().rawData;
        if (!this.totalCount) {
            this.totalCount = rawData.validationTotalCount;
        }
        if (!this.validRecordsCount) {
            this.validRecordsCount = rawData.validRecordsCount;
        }
        if (!this.invalidRecordsCount) {
            this.invalidRecordsCount = rawData.invalidRecordsCount;
        }
        
        var keepJSONExportEnabled = rawData.keepJSONExportEnabled;
        
        var dockedContent = [{
                xtype: 'pagingtoolbar',
                store: this.store,
                dock: 'bottom',
                displayInfo: true,
                animateShadow: true,
                items: [],
                plugins: [new Ext.ux.grid.PageSize(), new Ext.ux.ProgressBarPager()]
            }]
        this.reportGrid = Ext.create('Ext.grid.Panel', {
            columns: [{width: 1}],
            bufferedRenderer: false,
//            height: 410,
            dockedItems: dockedContent,
            viewConfig: {
                deferEmptyText: false,
                emptyText: "<div style='text-align:center;font-size:18px;'>" +ExtGlobal.getLocaleText("account.common.nodatadisplay") + "</div>"
            },
            border: false,
            enableLocking: true,
            defaultListenerScope: true,
            store: this.store
        });

        this.columns = rawData.columns;
        this.columns = createColumns(this.columns, this.moduleid);
        if (this.columns) {
            this.columns.push({
                text: "Validation Log",
                align: "left",
                dataIndex: "reason",
                width: 220
            });
        }
        this.reportGrid.reconfigure(this.store, this.columns);

        this.reportGrid.on("rowclick", this.handleRowClick, this);


        this.detailPanel = new Ext.panel.Panel({
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;overflow: fit;',
            border: false,
            scrollable: true,
            html: "<div style='font-size:12px;float:left;margin:6px 0px 10px 10px;width:90%;position:relative;'><br> <b>Total Records : </b>" + this.totalCount + ", <b>Valid Records :</b> " + this.validRecordsCount + ", " +
                    " <font color=red><b>Invalid Records :</b> " + this.invalidRecordsCount + "</font><br><br> <b>Validation Details :</b> <br>Please select a record</div>"
        });

        this.createBtn = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.common.createReport"), //'Save',,
            scope: this,
            handler: function () {
            }
        });

        this.cancelBtn = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler: function () {
                this.destroy();
            }
        });

        this.exportInvalidRecords = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.customreport.Button.ExportInvalidRecords"),
            scope: this,
            handler: function () {
                this.callExport(0);
            }
        });

        this.exportRecordsToJSON = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.customreport.Button.ExportValidRecordsToJSON"),
            disabled:!keepJSONExportEnabled,
            scope: this,
            handler: function () {
                this.callExport(1);
            }
        });

        this.store.on("load", function () {
            this.detailPanel.body.update("<div style='font-size:12px;float:left;margin:6px 0px 10px 10px;width:90%;position:relative;'><br> <b>Total Records :</b> " + this.totalCount + ", <b>Valid Records :</b> " + this.validRecordsCount + ", " +
                    " <font color=red><b>Invalid Records :</b> " + this.invalidRecordsCount + "</font><br><br> <b>Validation Details :</b> <br>Please select a record</div>");
        }, this);
        
        
        if(this.validRecordsCount <= 0){
            this.exportRecordsToJSON.disable();
        }
        
        if(this.invalidRecordsCount <= 0){
            this.exportInvalidRecords.disable();
        }

        Ext.apply(this, {
            title: "E-Way Validation Analysis Report",
            modal: true,
            iconCls: "pwnd favwinIcon",
            width: 1200,
            height: 550,
            resizable: true,
            closable: true,
            bodyStyle: 'overflow:auto',
            constrain: true,
            layout: 'border',
            buttonAlign: 'right',
            items: [{
                    region: 'north',
                    height: 85,
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtmlReqField("E-Way Validation Analysis Report", "<ul style='list-style-type:disc;padding-left:15px;font-size:12px;'>" +
                    "<li>"+"Please provide the valid data going back to the  Document(s). "+"</li>"+
                    "<li>"+"For a document JSON file is exported only when its all line level data  is valid."+"</li>",
            '../../images/accounting_image/account-revaluation-icon.png', 'HTML code and "\\\" character are not allowed')
                }, {
                    region: 'center',
                    border: false,
                    layout: "fit",
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    autoScroll: true,
                    items: [this.reportGrid]
                }, {
                    region: 'south',
                    height: 100,
                    layout: "fit",
                    border: false,
                    autoScroll: true,
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    items: [this.detailPanel]
                }
            ],
            buttons: [this.exportInvalidRecords, this.exportRecordsToJSON, this.cancelBtn]
        });

        this.callParent(arguments);
    },
    handleRowClick: function (scope, rec, tr, rowIndex, e) {
        var reasonText = rec.get("reasonDescription");
        this.detailPanel.body.update("<div style='font-size:12px;float:left;margin:6px 0px 10px 10px;width:90%;position:relative;'><br> <b>Total Records :</b> " + this.totalCount + ", <b>Valid Records :</b> " + this.validRecordsCount + ", " +
                " <font color=red><b>Invalid Records :</b> " + this.invalidRecordsCount + "</font><br><br> <b>Validation Details :</b> <br>" + reasonText + "</div>");
    },
    callExport: function (exportFor) {
        var fromDate = ExtGlobal.convertToGenericDate(this.parentObj.fromDate.getValue());
        var toDate = ExtGlobal.convertToGenericDate(this.parentObj.toDate.getValue());
        var reportID = this.parentObj.reportId;
        var gcurrencyid = Ext.pref.Currencyid;
        var consolidateFlag = false;
        var deleted = false;
        var nondeleted = false;
        var pendingapproval = false;
        var showRowLevelFieldsflag = false;
        var moduleid = this.parentObj.moduleid;
        var isreportloaded = this.parentObj.isreportloaded
        var isChartRequest = false;
        var isEWayReportValidation = true;
        var filter = this.parentObj.filterArray;
        var data = "";
        var rawData = this.store.getProxy().getReader().rawData;
        var columns = rawData.columns;
        var header = [];
        var title = [];
        var width = [];
        var indx = [];
        var align = [];
        var k = 0;
        if (exportFor == 0) {
            for (var itr = 0; itr < columns.length; itr++) {
                var recData = columns[itr];
                align.push('none');
                header.push(recData.dataIndex);
                title.push(recData.defaultHeaderOrig);
                width.push('220');

            }
            align.push('none');
            header.push('reasonDescription');
            title.push('Validation Log');
            width.push('220');
        }
        
        if (exportFor == 0) {
            var paramsStr = "exportEWayInvalidRecords=" + true;
            paramsStr += "&header=" + header + "&title=" + title + "&width=" + width + "&align=" + align + "&filename='EWay_InvalidRecords'" + "&filetype=" + "xlsx" ;
        } else if (exportFor == 1) {
            var paramsStr = "exportValidRecordsToJSON=" + true;
            paramsStr += "&filename='EWay_ValidRecordsJSON'" + "&filetype=" + "json" ;
        }
        paramsStr += "&get=" + 1+ "&isEWayReportValidation=" + isEWayReportValidation + "&fromDate=" + fromDate + "&toDate=" + toDate;
        paramsStr += "&reportID=" + reportID + "&gcurrencyid=" + gcurrencyid + "&consolidated=" + consolidateFlag;
        paramsStr += "&deleted=" + deleted + "&nondeleted=" + nondeleted + "&pendingapproval=" + pendingapproval + "&showRowLevelFieldsflag=" + showRowLevelFieldsflag + "&moduleid=" + moduleid + "&isreportloaded=" + isreportloaded + "&isChartRequest=" + isChartRequest;
        paramsStr += "&ewayFilter="+this.parentObj.statusFilter.getValue()+"&searchJson="+this.parentObj.searchJson+"&companyName="+companyName;
        if (filter) {
            paramsStr += "&filter=" + JSON.stringify(filter);
        }

            postData('ACCCreateCustomReport/executeCustomReport.do?', paramsStr);
            
            this.store.reload();
        if (exportFor == 1) {
            this.exportRecordsToJSON.disable();
        }
    }
});