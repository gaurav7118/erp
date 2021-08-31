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

function getAccountingPeriod() {
    var reportPanel = Wtf.getCmp('accountingperiod');
    if (reportPanel == null) {
        reportPanel = new Wtf.accountingPeriod({
            id: 'accountingperiod',
            border: false,
            moduleid: 2,
            title: WtfGlobal.getLocaleText('acc.accountingperiodtab.northpanel.accountingperiod.title'),
            tabTip: WtfGlobal.getLocaleText('acc.accountingperiodtab.northpanel.accountingperiod.title'),
            layout: 'fit',
            iscustreport: true,
            closable: true,
            isCustomer: true,
            label: WtfGlobal.getLocaleText('acc.accountingperiodtab.northpanel.accountingperiod.title'), //"Invoice",
            iconCls: 'accountingbase invoicelist'
        });
        Wtf.getCmp('as').add(reportPanel);
    }

    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

Wtf.accountingPeriod = function (config) {
    Wtf.apply(this, config);
    Wtf.accountingPeriod.superclass.constructor.call(this, config);

}

Wtf.extend(Wtf.accountingPeriod, Wtf.Panel, {
    onRender: function(config){
        Wtf.accountingPeriod.superclass.onRender.call(this, config);
    },
    
    initComponent: function (config) {

        Wtf.accountingPeriod.superclass.initComponent.call(this, config);
        this.ACCOUNTING_PERIOD_TYPES={
            YEAR:1,
            QUARTER:2,
            MONTH:3,
            FULL_YEAR_SETUP:4
        };
        this.createForm();

    },
    createForm: function () {
        
        this.btnArr = [];
        this.gridpanel=getAccountingLockPeriodInfoGrid();
       var accountingPeriodsStore=this.gridpanel.LockingPeriodStore;
        
          this.SetUpfullYear = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('acc.field.setupFullYear'),
            tooltip: WtfGlobal.getLocaleText('acc.field.setupFullYear'),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function () {
                getAccountingPeriodSetUpWindow(Wtf.TaxAccountingPeriods.FULLYEAR,accountingPeriodsStore);
            }
        });
        
        this.fullYear = new Wtf.Toolbar.Button({
            text:  WtfGlobal.getLocaleText('acc.field.setupNewYear'),
            tooltip:  WtfGlobal.getLocaleText('acc.field.setupNewYear'),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function () {
                getAccountingPeriodSetUpWindow(Wtf.TaxAccountingPeriods.YEAR,accountingPeriodsStore);
            }
        });

        this.Quarter = new Wtf.Toolbar.Button({
            text:  WtfGlobal.getLocaleText('acc.field.setupQuarter'),
            tooltip:  WtfGlobal.getLocaleText('acc.field.setupQuarter'),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function () {
                getAccountingPeriodSetUpWindow(Wtf.TaxAccountingPeriods.QUARTER,accountingPeriodsStore);
            }
        });
        
         this.Month= new Wtf.Toolbar.Button({
            text:  WtfGlobal.getLocaleText('acc.field.setupMonth'),
            tooltip:  WtfGlobal.getLocaleText('acc.field.setupMonth'),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function () {
                getAccountingPeriodSetUpWindow(Wtf.TaxAccountingPeriods.MONTHS,accountingPeriodsStore);
            }
        });

        this.btnArr.push(this.SetUpfullYear);
        this.btnArr.push(this.fullYear);
        this.btnArr.push(this.Quarter);
        this.btnArr.push(this.Month);

         
        var firsttbar = new Wtf.Toolbar(this.btnArr);

        this.toolbarPanel = new Wtf.Panel({
            border: false,
            items: [firsttbar]
        });
        
        var htmlDesc = getTopHtml(WtfGlobal.getLocaleText("acc.accountingperiodtab.northpanel.maintabview.title"), WtfGlobal.getLocaleText("acc.accountingperiodtab.northpanel.maintabview.title"), '../../images/accounitngperiodgrid.jpg', true, '0px 0px 0px 0px');
        this.northPanel = new Wtf.Panel({
            region: "north",
            height: 75,
            border: false,
            bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
            html: htmlDesc
        });
        
        
         this.saveBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            scope: this,
        iconCls :getButtonIconCls(Wtf.etype.save),
        handler:this.save.createDelegate(this)
        });
        
        this.newPanel = new Wtf.Panel({
            autoScroll: true,
            bodyStyle: ' background: none repeat scroll 0 0 #DFE8F6;',
            region: 'center',
            border:false,
            items: [this.northPanel,this.toolbarPanel,this.gridpanel],
            bbar:[ this.saveBttn]
        });

        this.add(this.newPanel);
    },
    save: function () {
        var gridDataJson = this.gridpanel.saveGridData();

        var params = {
            gridDataJson: gridDataJson
        }
        Wtf.Ajax.requestEx({
            url: "accPeriodSettings/saveLockUnlockInformationofAccountingPeriod.do",
            params: params
        }, this,
                function (resp) {
                    if (resp.success == true) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resp.msg], 0);
                        this.accountingPeriodsStore.reload();
                        this.close();
                    } else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), resp.msg], 1);
                    }

                }, function () {

        });
    }

});






