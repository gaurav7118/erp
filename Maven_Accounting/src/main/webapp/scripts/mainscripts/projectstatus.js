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

Wtf.account.projectStatusGrid = function(config){
    this.createGrid();
    Wtf.apply(this,{
            border:false,
            layout : "fit",
            tbar:this.btnArr,
            items:[this.grid],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.store,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.sales.norec"),  //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                            id : "pPageSize_"+this.id
                    })
            })
    }); 
    Wtf.account.projectStatusGrid.superclass.constructor.call(this,config); 
}

Wtf.extend(Wtf.account.projectStatusGrid,Wtf.Panel,{
    createGrid:function(){
        this.gridRec=Wtf.data.Record.create([
            {name:'projectName'},
            {name:'projectValue'},
            {name:'projectActualCost'},
            {name:'budgetedAmount'},
            {name:'percentCompletion'},
            {name:'totalInvoiceAmount'},
            {name:'totalAmountReceived'},
            {name:'balanceAmount'},
            {name:'salesOrderIds'},
            {name:'customerIds'}
        ]);
        
        this.store = new Wtf.data.Store({
            url:'ACCCombineReports/getProjectStatusReport.do',
            baseParams:{
                stdate : WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                enddate : WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false))
            },
            reader:new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.gridRec)
        });
        
        this.store.on('load', function(store){
//            this.grid.getView().refresh(true);
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
        }, this);
        
        this.store.load();
        
        this.selectionModel = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:true
        });
        
        this.gridcm = new Wtf.grid.ColumnModel([
            this.selectionModel,
            {
                header:WtfGlobal.getLocaleText("acc.field.ProjectName"),
                dataIndex:'projectName',
                pdfwidth:150
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.ProjectValue"),
                dataIndex:'projectValue',
                pdfwidth:150,
                pdfrenderer : "rowcurrency",
                renderer:WtfGlobal.currencyRendererSymbol
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.ProjectActualCost"),
                dataIndex:'projectActualCost',
                pdfwidth:150,
                pdfrenderer : "rowcurrency",
                renderer:WtfGlobal.currencyRendererSymbol
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.ProjectBudgetedAmount"),
                dataIndex:'budgetedAmount',
                pdfwidth:150,
                pdfrenderer : "rowcurrency",
                renderer:WtfGlobal.currencyRendererSymbol
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.Completion"),
                dataIndex:'percentCompletion',
                pdfwidth:150
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.TotalInvoiceAmount"),
                dataIndex:'totalInvoiceAmount',
                pdfwidth:150,
                pdfrenderer : "rowcurrency",
                renderer:WtfGlobal.currencyRendererSymbol
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.TotalAmountReceived"),
                dataIndex:'totalAmountReceived',
                pdfwidth:150,
                pdfrenderer : "rowcurrency",
                renderer:WtfGlobal.currencyRendererSymbol
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.BalanceAmount"),
                dataIndex:'balanceAmount',
                pdfwidth:150,
                pdfrenderer : "rowcurrency",
                renderer:WtfGlobal.currencyRendererSymbol
            }
        ]);
        
        this.grid = new Wtf.grid.GridPanel({
            layout:'fit',
            store:this.store,
            cm:this.gridcm,
            sm:this.selectionModel,
            loadMask:true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.createInvoiceButton = new Wtf.Toolbar.Button({
            text : WtfGlobal.getLocaleText("acc.lp.createinvoice"),
            iconCls: 'accountingbase agedrecievable',
            tooltip : WtfGlobal.getLocaleText("acc.lp.createinvoice"),
            id : 'createinvoice'+this.id,
            scope : this,
            handler : this.createInvoice
        });
        
        this.fetch = new Wtf.Toolbar.Button({
            text : WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',
            iconCls: 'accountingbase fetch',
            tooltip : 'Fetch Records.',
            id : 'fetch'+this.id,
            scope : this,
            handler : this.fetchData
        });
        
        this.exportButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.agedPay.exportTT"),  //'Export report details',
            disabled :true,
            params:{
            },
            menuItem:{csv:true,pdf:true,rowPdf:false},
            get:127
        });
        
        this.printBtn=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.print"),
            tooltip :WtfGlobal.getLocaleText("acc.agedPay.printTT"),  //'Print report details',
            disabled :true,
            params:{
                name: 'Project Status Report'
            },
            lable: WtfGlobal.getLocaleText("acc.projectStatusReport"),
            menuItem:{
                print:true
            },
            get:127
        });
        
        this.startDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate' + this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            value:WtfGlobal.getDates(true)
        });
    
        this.endDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate' + this.id,
            value:WtfGlobal.getDates(false)
        });
            
        this.btnArr = [];
        this.btnArr.push('From ');
        this.btnArr.push(this.startDate);
        this.btnArr.push('To ');
        this.btnArr.push(this.endDate);
        this.btnArr.push('-');
        this.btnArr.push(this.fetch);
        this.btnArr.push('-');
        this.btnArr.push(this.createInvoiceButton);
        this.btnArr.push('-');
        this.btnArr.push(this.exportButton);
        this.btnArr.push('-');
        this.btnArr.push(this.printBtn);
    },
    
    createInvoice:function(){
        var recArr = this.grid.getSelectionModel().getSelections();
        if(recArr != null && recArr != undefined && recArr.length==1){
            var rec = recArr[0];
            var soIds = rec.get('salesOrderIds');
            var soIdArr = soIds.split(',');
            var customerIds = rec.get('customerIds');
            createInvoiceForProjectStatusReport(false,null,null,true,soIdArr,customerIds)
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
        }
    },
    
    fetchData:function(){
        this.store.baseParams.stdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        this.store.baseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        this.store.load();
    }
})