/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


Ext.define('ReportBuilder.view.CreateNewReportWin', {
    extend:'Ext.window.Window',
    xtype:'createnewreportwin',
    initComponent: function() {
        var itemsArr = [];
        this.reportNameField = new Ext.form.field.Text({
            fieldLabel: ExtGlobal.getLocaleText("acc.common.ReportName")+"*",
            xtype: 'textfield',
            name: 'reportName',
            labelWidth: 165,
            width: 385,
            maxLength: 50,
            allowBlank: false,
            validateBlank: true,
            vtype: 'reportnamevtype'
        });
        itemsArr.push(this.reportNameField);
        this.reportDescriptionField = new Ext.form.field.TextArea({
            fieldLabel:ExtGlobal.getLocaleText("acc.common.ReportDescription"),
                name: 'reportDesc',
                xtype: 'textarea',
                labelWidth: 165,
                width: 385,
                maxLength: 255
        });
        itemsArr.push(this.reportDescriptionField);
        if(showPivot) {            
            this.reportType = new Ext.form.field.Checkbox({
                fieldLabel: ExtGlobal.getLocaleText("acc.common.createPivotReport") + ":",
                defaultType: 'checkboxfield',
                align: 'center',
                labelWidth : 167,
                name: 'reportType'
            });
            if(!this.isCopy){
             itemsArr.push(this.reportType);   
            }
        }
        
        if(countryid == 105){
            this.EWayReport = new Ext.form.field.Checkbox({
                fieldLabel: ExtGlobal.getLocaleText("acc.common.createEWayReport") + ":",
                defaultType: 'checkboxfield',
                align: 'center',
                labelWidth: 167,
                name: 'reportType'
            });
//            itemsArr.push(this.EWayReport);
        }
        
        this.form = new Ext.form.FormPanel({
            padding: '15 15 15 15',
            bodyStyle: "background: transparent;",
            border: false,
            style: "background: transparent;padding:20px;",
            labelWidth: 150,
            items: itemsArr
        });
        
        this.createBtn = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.common.createReport"), //'Save',,
            itemId: 'create',
            scope: this,
            handler: function() {
                this.setReportDetails();                   
            }
        });
        this.copyBtn = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.lp.copy"),
            scope: this,
            handler: function() {
                this.setReportDetails();                   
            }
        });
        
        this.createBtn.on('click',function(){
            this.mask(ExtGlobal.getLocaleText("acc.common.load"));
        },this);
        
        this.cancelBtn=Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.common.cancelBtn"),
            itemId: 'cancel',
            scope: this,
            handler: function() {
                this.hide();
            }
        });
        var reportBtnArr = [];
        if(this.isCopy){
            reportBtnArr.push(this.copyBtn,this.cancelBtn);
        }else{
            reportBtnArr.push(this.createBtn,this.cancelBtn);
        }
        
        Ext.apply(this, {
            title:this.isCopy? ExtGlobal.getLocaleText("acc.CustomReport.copyReport"):ExtGlobal.getLocaleText("acc.common.createnewCustomReport"),
            modal: true,
            iconCls: "pwnd favwinIcon",
            width: 500,
            height: 300,
            resizable: false,
            closable: false,
            constrain: true,
            layout: 'border',
            buttonAlign: 'right',
            items: [{
                region: 'north',
                height: 75,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html:getTopHtmlReqField(this.isCopy?ExtGlobal.getLocaleText("acc.CustomReport.copyReport"):  ExtGlobal.getLocaleText("acc.common.createnewCustomReport"),this.isCopy? ExtGlobal.getLocaleText("acc.CustomReport.copyCustomReport") : ExtGlobal.getLocaleText("acc.common.newCustomReport"), '../../images/save.png', 'HTML code and "\\\" character are not allowed')
            }, {
                region: 'center',
                border: false,
                bodyStyle: 'background:#f1f1f1;font-size:10px;border-top:1px solid #bfbfbf;',
                autoScroll: true,
                items: [this.form]
            }],
            buttons: reportBtnArr
        });
        
        this.callParent(arguments);
    },
    
    setReportDetails: function() {
        if(this.form.isValid()) {
            this.reportName = this.reportNameField.getValue();
            this.reportDescription = this.reportDescriptionField.getValue();
            
            if(this.reportType != undefined && this.reportType.getValue() != undefined && this.reportType.getValue() == true) {
                this.isPivot = true;
            } else {
                this.isPivot = false;
            }
            if(this.EWayReport != undefined && this.EWayReport.getValue() != undefined && this.EWayReport.getValue() == true) {
                this.isEWayReport = true;
            } else {
                this.isEWayReport = false;
            }
            
            this.checkReportNameExistance();
        } else {
            Ext.CustomMsg('Invalid Data', ExtGlobal.getLocaleText("acc.common.providevalidData"), Ext.Msg.INFO);
            this.unmask();
        }
    },
    
    checkReportNameExistance: function() {
        checkReportNameExistance(this.reportName,function(isReportNameExists){
            if (isReportNameExists) {
                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.CustomReport.dupReportNameEdit"), Ext.Msg.INFO);
                this.unmask();
            } else {
                if (Ext.getCmp('mainTabPanel').getChildByElement("idnewcustomreporttab" + this.reportName.replace(/\s/g, '_')) !== undefined && Ext.getCmp('mainTabPanel').getChildByElement("idnewcustomreporttab" + this.reportName.replace(/\s/g, '_'))) {
                    Ext.getCmp('mainTabPanel').setActiveTab("idnewcustomreporttab" + this.reportName.replace(/\s/g, '_'));
                    this.unmask();
                    this.close();
                } else {
                    this.unmask();
                    if (this.newreportpanelscope) {
                        this.newreportpanelscope.destroy();
                        
                    }
                    var params = {
                        isEWayReport: this.isEWayReport
                    };
                    if (!this.isCopy) {
                        createNewReportTab("idnewcustomreporttab", undefined, undefined, undefined, undefined, this.isPivot, undefined, this.reportName, this.reportDescription, undefined, undefined, undefined, false, undefined, params);
                    } else {
                        CopyReport(this.reportId,this.reportName,this.reportDescription,this.isEdit);
                    }
                    this.close();
                }
            }
        },this);
    }
});