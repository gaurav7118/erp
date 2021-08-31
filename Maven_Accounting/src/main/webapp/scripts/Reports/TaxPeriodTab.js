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

function getTaxPeriod() {
    
    var groupView = new Wtf.grid.GroupingView({
        forceFit: true,
        showGroupName: false,
        enableGroupingMenu: true,
        groupTextTpl: '{group} '
    });
    var reportPanel = Wtf.getCmp('taxperiod');
    if (reportPanel == null) {
        reportPanel = new Wtf.accountingTaxPeriod({
            id: 'taxperiod',
            border: false,
            moduleid: 2,
            title: 'Tax Period',
            tabTip: 'Tax Period',
            layout: 'fit',
            iscustreport: true,
            closable: true,
            isCustomer: true,
            label: 'Tax Period', 
            iconCls: 'accountingbase invoicelist',
            viewConfig:{
                forceFit: true,
                view:groupView,
                emptyText: '<div class="emptyGridText">' + WtfGlobal.getLocaleText('account.common.nodatadisplay') + ' <br></div>'
            }
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

Wtf.accountingTaxPeriod = function (config) {
    Wtf.apply(this, config);     
    Wtf.accountingTaxPeriod.superclass.constructor.call(this, config);

}

Wtf.extend(Wtf.accountingTaxPeriod, Wtf.Panel, {
    onRender: function(config){
        Wtf.accountingTaxPeriod.superclass.onRender.call(this, config);
    },
    initComponent: function (config) {
        Wtf.accountingTaxPeriod.superclass.initComponent.call(this, config);
        this.createForm();

    },
    createForm: function () {

        this.btnArr = [];
        this.gridpanel=getLockPeriodInfoGrid();
        this.taxStore=this.gridpanel.LockingPeriodStore;
        
        this.SetUpfullYear = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText('acc.field.setupFullYear'),
            tooltip: WtfGlobal.getLocaleText('acc.field.setupFullYear'),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function () {
                getTaxPeriodSetUpWindow(Wtf.TaxAccountingPeriods.FULLYEAR,this.taxStore);
            }
        });
        
        this.fullYear = new Wtf.Toolbar.Button({
            text:  WtfGlobal.getLocaleText('acc.field.setupNewYear'),
            tooltip:  WtfGlobal.getLocaleText('acc.field.setupNewYear'),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function () {
                getTaxPeriodSetUpWindow(Wtf.TaxAccountingPeriods.YEAR,this.taxStore);
            }
        });

        this.Quarter = new Wtf.Toolbar.Button({
            text:  WtfGlobal.getLocaleText('acc.field.setupQuarter'),
            tooltip: WtfGlobal.getLocaleText('acc.field.setupQuarter'),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function () {
                getTaxPeriodSetUpWindow(Wtf.TaxAccountingPeriods.QUARTER,this.taxStore);
            }
        });
        this.Month = new Wtf.Toolbar.Button({
            text:  WtfGlobal.getLocaleText('acc.field.setupMonth'),
            tooltip: WtfGlobal.getLocaleText('acc.field.setupMonth'),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function () {
                getTaxPeriodSetUpWindow(Wtf.TaxAccountingPeriods.MONTHS,this.taxStore);
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
        
        var htmlDesc = getTopHtml('  Tax Period Settings', ' Tax Period Settings', '../../images/accounitngperiodgrid.jpg', true, '0px 0px 0px 0px');
        this.northPanel = new Wtf.Panel({
            region: "north",
            height: 105,
            border: false,
            bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
            html: htmlDesc
        });
        this.newPanel = new Wtf.Panel({
            autoScroll: true,
            bodyStyle: ' background: none repeat scroll 0 0 #DFE8F6;',
            region: 'center',
            border:false,
            items: [this.northPanel,this.toolbarPanel,this.gridpanel]
        });
        this.add(this.newPanel);
    }
});





