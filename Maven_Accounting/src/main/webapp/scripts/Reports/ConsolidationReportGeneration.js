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

function callConsolidationReportGenerationTab(){
    var panel = Wtf.getCmp("consolidationReportGenerationTab");
    if(!panel){
        panel= new Wtf.account.consolidationReportGenerationTab({
            id:'consolidationReportGenerationTab',
            layout:'fit',
            border: false,
            tabTip : WtfGlobal.getLocaleText("acc.conslodation.consolidationReportGeneration"),
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.conslodation.consolidationReportGeneration"),Wtf.TAB_TITLE_LENGTH),
            closable:true,
            isClosable:true
        })
        Wtf.getCmp('as').add(panel);
        
        panel.on('beforeclose', function (panel) {
            if (panel.isClosable !== true) {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                    msg: WtfGlobal.getLocaleText("acc.msgbox.51"), //this.closeMsg,
                    width: 500,
                    buttons: Wtf.MessageBox.YESNO,
                    icon: Wtf.MessageBox.QUESTION,
                    fn: function (btn) {
                        if (btn == "yes") {
                            Wtf.getCmp('as').remove(panel);
                        }
                    },
                    scope: this
                });
            } else {
                Wtf.getCmp('as').remove(panel);
            }
            return false;
        }, this);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.consolidationReportGenerationTab = function (config){
    Wtf.apply(this, config);
    Wtf.account.consolidationReportGenerationTab.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.consolidationReportGenerationTab,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.consolidationReportGenerationTab.superclass.onRender.call(this, config);
        this.add({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid,
            tbar:this.buttonArray,
            bbar:this.pagingToolbar
        });
    },
    initComponent:function(config){
        Wtf.account.consolidationReportGenerationTab.superclass.initComponent.call(this, config);
        //Create Store
        this.createStore();
        
        //Load Store
        this.loadStore();
        
        //Create Grid
        this.createGrid();
        
        //Create Button
        this.createButton();
        
        this.grid.on('cellclick',this.onCellClick, this);
        this.grid.on('afteredit',function(){
            this.isClosable = false;
        }, this);
    },
    createButton:function(){
        this.buttonArray = new Array();
        
        this.MSComboconfig = {
            hiddenName:'subdomainmulselectcombo',         
            store: this.childSubdomainStore,
            valueField:'companyid',
            hideLabel:false,
            hidden : false,
            displayField:'subdomain',
            emptyText:WtfGlobal.getLocaleText("acc.fxexposure.all"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };          
                
        this.MultiSelectSubdomainCombo = new Wtf.common.Select(Wtf.applyIf({
            id:'mulsubdomaincombo'+this.id,
            multiSelect:true,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectAccounts") ,
            forceSelection:true,  
            extraFields:['companyname'],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            width:125
        },this.MSComboconfig));
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.conslodation.fetchdataonfilter") ,
            id: 'fetchButton' + this.id,
            scope: this,
            iconCls:'accountingbase fetch',
            handler: this.fetchData.createDelegate(this)
        });
        
        this.expButton=new Wtf.exportButton({
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
            id:"exportConsolidationgenerationReport",
            filename: WtfGlobal.getLocaleText("acc.conslodation.consolidationReportGeneration") + "_v1",
            menuItem:{
                csv:true,
                pdf:true,
                rowPdf:false,
                xls:true
            },
            get:Wtf.autoNum.consolidationGenerationReport,
            label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
        });
    
        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            id:"printConsolidationgenerationReport",
            filename: WtfGlobal.getLocaleText("acc.conslodation.consolidationReportGeneration") + "_v1",
            tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
            menuItem:{
                print:true
            },
            get:Wtf.autoNum.consolidationGenerationReport,
            label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
        });
        
        
        this.buttonArray.push(WtfGlobal.getLocaleText("acc.conslodation.selectsubdomain"),this.MultiSelectSubdomainCombo,this.fetchBttn,this.expButton,this.printButton);
        
        var bottombtnArr=[];
        this.save=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  
            scope: this,
            handler: this.saveData.createDelegate(this),
            iconCls: 'pwnd save'
        });
        
        bottombtnArr.push('-', this.save);
        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.store,           
            displayInfo: true,
            searchField: this.MultiSelectSubdomainCombo.getValue(),
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            }),
            items:bottombtnArr
        });
    },
    
    createStore:function(){
        this.record=new Wtf.data.Record.create([
        {
            name:"id"
        },
        {
            name:"subdomainid"
        },
        {
            name:"subdomainname"
        },
        {
            name:"companyname"
        },

        {
            name:"stakeinpercentage"
        },

        {
            name:"currencyname"
        },

        {
            name:"exchangerate"
        },
        {
            name:"applydate",
            type:'date'
        }
        ]);  

        this.store=new Wtf.data.Store({
            reader:new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"  
            },this.record),
            url: "ACCCurrency/getConsolidationReportGenerationData.do"
        }); 
        
        this.childSubdomainRecord = new Wtf.data.Record.create([
        {
            name: 'companyid'
        },

        {
            name: 'companyname'
        },

        {
            name: 'subdomain'
        }
        ]);
        
        this.childSubdomainReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, this.childSubdomainRecord);
        
        this.childSubdomainStore = new Wtf.data.Store({
            reader: this.childSubdomainReader,
            url:"ACCReports/getMappedCompanies.do"
        });
    },
    fetchData:function(){
        this.store.load({
            params:{
                selectedSubdomains:this.MultiSelectSubdomainCombo.getValue(),
                start:0,
                limit:(this.pP.combo!=undefined) ? this.pP.combo.value:30
            }
        })
    },
    loadStore:function(){
        this.store.load({
            start:0,
            limit:30
        });
        this.childSubdomainStore.load();
    },
    saveData:function(){
        var rec=[];
        var arr=[]
        rec.data=this.getUpdatedDetails();
        var invalidSubdomain="";
        for(var i=0;i<this.store.getCount();i++){                           //outer for loop with Grid store
            var record=this.store.getAt(i);
            if(record.dirty){
                if(record.data.applydate=="" || record.data.applydate==undefined || record.data.exchangerate=="" || record.data.exchangerate==undefined){
                    if(invalidSubdomain==""){
                        invalidSubdomain = record.data.subdomainname; 
                    } else{
                        invalidSubdomain +=", "+record.data.subdomainname;
                    }
                } else {
                    record.set('applydate', (new Date(record.data.applydate).clearTime()));
                    arr.push(i);
                }
            }
        }
        
        if(invalidSubdomain!=""){//It means there are some subdomains for which invalid value is given
            var msg = WtfGlobal.getLocaleText("acc.conslodation.applydateisnotavailable") +" "+invalidSubdomain+"."
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),msg], 2);
            return;
        } 
        rec.data=WtfGlobal.getJSONArray(this.grid,true,arr);
        
        if(rec.data=="[]")
        {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.conslodation.nochangetosave")], 2);
            return;
        } else{
            Wtf.Ajax.requestEx({
                url: "ACCCurrency/saveConsolidationExchangeRateDetails.do",
                params: rec
            }, this, function(resp){
                this.isClosable=true;
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.success"),
                    msg: resp.msg,
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.INFO,
                    scope: this,
                    fn: function(btn) {
                        if (btn == "ok") {
                            this.store.load();
                        }
                    }
                });
            }, function(resp){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"),resp.msg], 1);
            });
        }
    },
    getUpdatedDetails:function(){
        var arr=[];
        for(var i=0;i<this.store.getCount();i++){                           //outer for loop with Grid store
            var rec=this.store.getAt(i)
            if(rec.dirty){//rec.dirty becomes true for modified data
                if(rec.data.applydate=="" || rec.data.applydate==undefined || rec.data.exchangerate=="" || rec.data.exchangerate==undefined){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.conslodation.applydateisnotavailable")], 2);
                    break;
                } else{
                    rec.set('applydate', (new Date(rec.data.applydate).clearTime()));
                }
                arr.push(i);
            }
        }
        return WtfGlobal.getJSONArray(this.grid,true,arr);
    },
    createGrid:function(){
        this.gridcm= new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),
        {
            header :'',
            pdfwidth:75,
            dataIndex: 'id',
            hidden:true,            
            renderer:WtfGlobal.deletedRenderer
        },{
            header :WtfGlobal.getLocaleText("acc.conslodation.companyName"), 
            pdfwidth:75,
            dataIndex: 'companyname',
            renderer:WtfGlobal.deletedRenderer
        },{
            header :WtfGlobal.getLocaleText("acc.conslodation.subdomainname"), 
            pdfwidth:75,
            dataIndex: 'subdomainname',
            renderer:WtfGlobal.deletedRenderer
        },{
            header :WtfGlobal.getLocaleText("acc.conslodation.stakeinpercentage"), 
            pdfwidth:75,
            dataIndex: 'stakeinpercentage',
            renderer:WtfGlobal.deletedRenderer
        },{
            header :WtfGlobal.getLocaleText("acc.customerList.gridCurrency"),  //'Currency',
            pdfwidth:75,
            dataIndex: 'currencyname',
            renderer:this.currencylink.createDelegate(this)
        },{
            header :WtfGlobal.getLocaleText("acc.conslodation.exchangerate"),  //'Exchange rate',
            dataIndex:'exchangerate',
            pdfwidth:75,
            editor:this.exchangeRate=new Wtf.form.NumberField({
                allowBlank: false,
                decimalPrecision:16,
                allowNegative: false,
                minValue:0
            })
        },{
            header: WtfGlobal.getLocaleText("acc.currency.lastapp"),
            dataIndex: 'applydate',
            renderer:WtfGlobal.onlyDateRenderer,
            pdfwidth:75,
            minValue:new Date().clearTime(true),
            editor:new Wtf.form.DateField({
                name:'applydate',
                format:WtfGlobal.getOnlyDateFormat()
            })
        }
        ]);
        this.grid = new Wtf.grid.EditorGridPanel({
            cls:'vline-on',
            layout:'fit',
            autoScroll:true,
            height:200,
            id:'consolidationReportgenerationgrid',
            store: this.store,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            bbar : this.save,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
    },
    currencylink:function(val){
        return WtfGlobal.currencyLinkRenderer(val,WtfGlobal.getLocaleText("acc.currency.his"));
    },
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var rec=this.store.getAt(i);
        callViewExchangerateHistoryDetails(rec.data.id);
    }
});

/*****************************************Below Code is for showing History window **********************************************/

function callViewExchangerateHistoryDetails(consolidationid){
var panel = Wtf.getCmp("consolidationCurrencyExchangeDetailswin");
    if(!panel){
        new Wtf.account.ConsolidationCurrencyExchangeWindow({
            id:"consolidationCurrencyExchangeDetailswin",
            currencyhistory:true,
            consolidationid:consolidationid,
            closable: true,
            title:WtfGlobal.getLocaleText("acc.conslodation.currencyExchangeDetails"),
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 600,
            height: 400,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
    }
}


Wtf.account.ConsolidationCurrencyExchangeWindow = function(config){
    this.uPermType=Wtf.UPerm.currencyexchange;
    this.permType=Wtf.Perm.currencyexchange;
    this.consolidationid=config.consolidationid;
    var btnArr=[];
    
    btnArr.push(this.cancel=new Wtf.Toolbar.Button({
        text: this.currencyhistory?WtfGlobal.getLocaleText("acc.common.backBtn"):WtfGlobal.getLocaleText("acc.common.cancelBtn"),
        scope: this,
        handler:this.closeWin.createDelegate(this)
    }))
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.currency.title1"), 
        buttons: btnArr
    },config);
    Wtf.account.ConsolidationCurrencyExchangeWindow.superclass.constructor.call(this, config);
}
Wtf.extend( Wtf.account.ConsolidationCurrencyExchangeWindow, Wtf.Window, {
    defaultCurreny:false,
    draggable:false,
    onRender: function(config){
        Wtf.account.ConsolidationCurrencyExchangeWindow.superclass.onRender.call(this, config);
        this.createStore();
        this.createGrid();

        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(WtfGlobal.getLocaleText("acc.currency.title1"),WtfGlobal.getLocaleText("acc.currency.title"),"../../images/accounting_image/currency-exchange.jpg",true)
        },{
            region:'center',
            border: false,
            height:260,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid
        });
    },
    createStore:function(){
        this.gridRec = new Wtf.data.Record.create([{
            name: 'id'
        },{
            name: 'applydate',
            type:'date'
        },{
            name: 'exchangerate',
            type:'float'
        }
        ]);
        this.store = new Wtf.data.Store({
            url:"ACCCurrency/getConsolidationExchangeHistory.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.gridRec)
        });
        
        this.store.load({
            params:{
                consolidationid:this.consolidationid
            }
        });
    },

    createGrid:function(){
        this.gridcm= new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),
        {
            header :'',
            pdfwidth:75,
            dataIndex: 'id',
            hidden:true
        },{
            header:WtfGlobal.getLocaleText("acc.conslodation.exchangerate"),
            dataIndex:'exchangerate',
            pdfwidth:75,
            align:'center'
        },{
            header: WtfGlobal.getLocaleText("acc.conslodation.applydate"),
            dataIndex: 'applydate',
            renderer:WtfGlobal.onlyDateRenderer,
            minValue:new Date().clearTime(true)
        }
        ]);
        this.grid = new Wtf.grid.GridPanel({
            cls:'vline-on',
            layout:'fit',
            autoScroll:true,
            height:200,
            id:'consolidationcurrencyhistory'+this.id,
            store: this.store,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            bbar : this.save,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
    },
    closeWin:function(){
        this.fireEvent('cancel',this)
        this.close();
    }
});