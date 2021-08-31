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
function copyDefaultRates(id){
    Wtf.getCmp(id).getStore().removeAll();
    Wtf.getCmp(id).getColumnModel().setRenderer(1, function(val){
        return "<span class='defaultcurrency'>"+val+ "</span>"
        });
    Wtf.getCmp(id).getStore().proxy.conn.url = "ACCCurrency/getDefaultCurrencyExchange.do";
//    Wtf.getCmp(id).getStore().proxy.conn.url = Wtf.req.account+'CompanyManager.jsp';
    Wtf.getCmp(id).getStore().load({
        params:{
            mode:204
        }
        });   
}

Wtf.account.CurrencyExchangeWindow = function(config){
    this.currencyhistory=config.currencyhistory||false;
    this.uPermType=Wtf.UPerm.currencyexchange;
    this.permType=Wtf.Perm.currencyexchange;
    var btnArr=[];

    this.moduleName = Wtf.Currency_Exchange;
    var extraConfig = {};
    extraConfig.url = "ACCCurrency/importCurrencyExchange.do";
    var extraParams = "";
    var importCurrencyExchangeBtnArray = Wtf.importMenuArray(this, this.moduleName, config.store, extraParams, extraConfig);

    this.importUOMBtn = new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.common.import"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.import"),
        iconCls: (Wtf.isChrome ? 'pwnd importChrome' : 'pwnd import'),
        menu: importCurrencyExchangeBtnArray,
        typeXLSFile:true
    });

    if (!this.currencyhistory && !config.isRevaluation) {
        btnArr.push(this.importUOMBtn);
    }
      if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)) {
        btnArr.push(this.save=new Wtf.Toolbar.Button({
            text: (config.isRevaluation)?WtfGlobal.getLocaleText("acc.setupWizard.next"):WtfGlobal.getLocaleText("acc.currency.sav"),  //'Save and Close',
            scope: this,
            hidden:this.currencyhistory,
            handler: this.saveData.createDelegate(this)
        }))
      }
      btnArr.push(this.cancel=new Wtf.Toolbar.Button({
            text: this.currencyhistory?WtfGlobal.getLocaleText("acc.common.backBtn"):WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }))
    Wtf.apply(this,{
        title:(config.isRevaluation)?WtfGlobal.getLocaleText("acc.currency.revalue.title"):WtfGlobal.getLocaleText("acc.currency.title1"),  //"Currency Exchange Table",
        buttons: btnArr
    },config);
    Wtf.account.CurrencyExchangeWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.CurrencyExchangeWindow, Wtf.Window, {
    defaultCurreny:false,
    draggable:false,
    onRender: function(config){
        Wtf.account.CurrencyExchangeWindow.superclass.onRender.call(this, config);
        this.createStore();
        this.createGrid();

        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml((this.isRevaluation)?WtfGlobal.getLocaleText("acc.currency.revalue.title"):WtfGlobal.getLocaleText("acc.currency.title1"),(this.isRevaluation)?WtfGlobal.getLocaleText("acc.currency.revalue.desc"):WtfGlobal.getLocaleText("acc.currency.title"),"../../images/accounting_image/currency-exchange.jpg",true)
        },this.headerCalTemp=new Wtf.Panel({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            hidden:this.isRevaluation,
            //html: "<a class='tbar-link-text' href='#' onClick='javascript: copyDefaultRates(\""+this.grid.getId()+"\")'wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.currency.down")+"</a>",
            bodyStyle: 'border-bottom:1px solid #bfbfbf;padding:10px'
        }),{
            region:(this.isRevaluation)?'center':'south',
            border: false,
            height:(this.currencyhistory?260:150),
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid
        });
    if(!this.currencyhistory)
        this.grid.on('cellclick',this.onCellClick, this);
        this.grid.on('afteredit',this.updateRow,this);
    this.grid.on('validateedit',this.checkZeroValue, this);
    },
    createStore:function(){
        this.gridRec = new Wtf.data.Record.create([{
            name: 'id'
        },{
            name: 'applydate',
            type:'date'
        },{
            name: 'fromcurrency'
        },{
            name: 'tocurrency'
        },{
            name: 'exchangerate',
            type:'float'
        },{
            name: 'newexchangerate',
            type:'float'
        },{
            name: 'tocurrencyid'
        },{
            name: 'fromcurrencyid'
        },{
            name: 'companyid'        
        },{
            name: 'currencycode'
        },{
            name: 'exchangeratetype' , defValue:0
        },{
            name: 'todate',
            type:'date'
        },{
            name: 'foreigntobaseexchangerate',
            type:'float'
        }
    ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.gridRec),
            url:Wtf.req.account+'CompanyManager.jsp'
        });
        
        this.currencyExchangeRateStore = new Wtf.data.Store({
            url:"ACCCurrency/getCurrencyExchangeList.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.gridRec)
        });
    },
    filterStore:function(store){
         this.store.filterBy(function(rec){
            if(rec.data.tocurrencyid==rec.data.fromcurrencyid)
                return false
            else
                return true
        },this)
    },

    applyTemplate:function(store){       
        this.filterStore();
        var index=store.getCount();
        if(index>0) {
            this.headerTplSummary.overwrite(this.headerCalTemp.body,{
                foreigncurrency:store.getAt(0).data['tocurrency'],
                exchangerate:store.getAt(0).data['exchangerate'],
                newexchangerate:store.getAt(0).data['newexchangerate'],
                basecurrency:store.getAt(0).data['fromcurrency']
                });
            if(this.isRevaluation){
               this.store.each(function(rec){
                    rec.set("exchangeratetype",0);
                },this);
            }    
        }else{
            this.headerTplSummary.overwrite(this.headerCalTemp.body,{
                foreigncurrency:"foreign currency",
                exchangerate:"x",
                newexchangerate:"x",
                basecurrency:WtfGlobal.getCurrencyName()
            });
        }
    },
    checkZeroValue:function(obj){
        if(obj.field=="exchangerate"){
            if(obj.value==0)
                obj.cancel=true;
        }
        if(obj.field=="newexchangerate"){
            if(obj.value==0)
                obj.cancel=true;
        }
        if(obj.field=="foreigntobaseexchangerate"){
            if(obj.value==0){
                obj.cancel=true;
            }
        }
    },
    createGrid:function(){
        this.editorFlag = (this.currencyhistory?true:false);
        
        var data=[[0,'Base to Foreign'],[1,'Foreign to Base']];
        this.typeStore= new Wtf.data.SimpleStore({
                fields: [{name:'id',type:'int'}, 'ratetypeid'],
                data :data
        });
        this.rateTypecombo = new Wtf.form.ComboBox({
                store: this.typeStore,
                name:'exchangeratetype',
                displayField:'ratetypeid',
                allowBlank: false,
                valueField:'id',
                forceSelection:true,
                mode: 'local',
                triggerAction: 'all',
                selectOnFocus:true
        });
        
//        var recordSelected = this.typeStore.getAt(0);                     
//        this.selectedYear.setValue(recordSelected.get("id"));
//               
                
        this.gridcm= new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),{
            header:WtfGlobal.getLocaleText("acc.currency.cur"),  //"Currency",
            dataIndex:'tocurrency',
            hidden:this.currencyhistory,
            renderer:this.currencylink.createDelegate(this),//function(){WtfGlobal.currencyLinkRenderer(val,),
            autoWidth : true
     
        },{
            header:WtfGlobal.getLocaleText("acc.currency.basetoforeignexRate"),  //"Base to Foreign Exchange Rate",
            dataIndex:'exchangerate',
            renderer:this.setRateRenderer.createDelegate(this),
            hidden:this.isRevaluation,
            editor:this.editorFlag?"":this.exchangeRate=new Wtf.form.NumberField({
                allowBlank: false,
                decimalPrecision:16,
                readOnly:this.isRevaluation,
                allowNegative: false,
                minValue:0
            })
        },{
            header:WtfGlobal.getLocaleText("acc.currency.foreigntobaseexRate"),  //"Foreign to Base Exchange Rate",
            dataIndex:'foreigntobaseexchangerate',
            renderer:this.setRateRenderer.createDelegate(this),
            hidden:this.isRevaluation,
            editor:this.editorFlag?"":this.foreignToBaseExchangeRate=new Wtf.form.NumberField({
                allowBlank: false,
                decimalPrecision:16,
                allowNegative: false,
                minValue:0
            })
        },{
            header: WtfGlobal.getLocaleText("acc.currencyExchange.type"),
            width:200,
            dataIndex:'exchangeratetype',
            hidden:!this.isRevaluation,
            renderer:Wtf.comboBoxRenderer(this.rateTypecombo),
            editor:this.rateTypecombo
        },{
            header:WtfGlobal.getLocaleText("acc.currency.revalueexRate"),  //"Exchange Rate1",
            dataIndex:'newexchangerate',
            renderer:this.setRateRenderer.createDelegate(this),
            hidden:!this.isRevaluation,
            editor:this.editorFlag?"":this.newexchangeRate=new Wtf.form.NumberField({
                allowBlank: false,
                decimalPrecision:9,
                allowNegative: false,
                minValue:0
            })
        },{
            header: Wtf.account.companyAccountPref.activateToDateforExchangeRates? WtfGlobal.getLocaleText("acc.currency.FromDate"):(this.currencyhistory?WtfGlobal.getLocaleText("acc.currency.app"):WtfGlobal.getLocaleText("acc.currency.lastapp")),  //"From Date" :"Applied Date":"Last Applied Date",
            dataIndex: 'applydate',
            renderer:WtfGlobal.onlyDateRenderer,
            minValue:new Date().clearTime(true),
            hidden:this.isRevaluation,
            editor:this.editorFlag?"":new Wtf.form.DateField({
                name:'applydate',
//                maxValue : (Wtf.account.companyAccountPref.fyfrom).clearTime(),    // Disable Dates ahead of the Financial year date 
                format:WtfGlobal.getOnlyDateFormat()
            })
        },{
            header: WtfGlobal.getLocaleText("acc.currency.ToDate"),  //"To Date",
            dataIndex: 'todate',
            renderer:WtfGlobal.onlyDateRenderer,
            minValue:new Date().clearTime(true),
            hidden:!Wtf.account.companyAccountPref.activateToDateforExchangeRates,
            editor:this.editorFlag?"":new Wtf.form.DateField({
                name:'todate',
                format:WtfGlobal.getOnlyDateFormat()
            })
        }
        ]);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit))
        this.grid = new Wtf.grid.EditorGridPanel({
            cls:'vline-on',
            layout:'fit',
            autoScroll:true,
            height:200,
            id:(this.currencyhistory?'currencyhistory':'defaultCurrencygrid'),
            store: this.store,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        else
         this.grid = new Wtf.grid.GridPanel({
            cls:'vline-on',
            layout:'fit',
            autoScroll:true,
            height:200,
            id:(this.currencyhistory?'currencyhistory':'defaultCurrencygrid'),
            store: this.store,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
         var downloadstr="";
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit))
        //downloadstr="<a class='tbar-link-text' href='#' onClick='javascript: copyDefaultRates(\""+this.grid.getId()+"\")'wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.currency.down")+"</a>"
        this.headerTplSummary=new Wtf.XTemplate(
            "<div><b>"+WtfGlobal.getLocaleText("acc.currency.homCur")+"</b> {basecurrency} </div>",
            "<div><b>"+WtfGlobal.getLocaleText("acc.currency.exrate")+"</b> "+WtfGlobal.getLocaleText("acc.currency.msg2")+"</div>",
            "<div><b>"+WtfGlobal.getLocaleText("acc.currency.example")+"</b> {exchangerate} {foreigncurrency} "+WtfGlobal.getLocaleText("acc.currency.for")+" = 1 {basecurrency} "+WtfGlobal.getLocaleText("acc.currency.hom")+" </div>",
            "<br>",
            downloadstr
        );
        this.loadStore()                
    },

    loadStore:function(){
       this.store.proxy.conn.url = "ACCCurrency/getCurrencyExchangeList.do";
       if(this.currencyhistory){
       this.store.load({
            params:{
                mode:203,
                currencyid:this.currencyid
                }
            });
        }
        else{
        this.store.proxy.conn.url = "ACCCurrency/getCurrencyExchange.do";
             this.store.load({
                params:{
                    mode:201,
                    tocurrencyid:this.revalueCurrenyId,
                    transactiondate:WtfGlobal.convertToGenericDate(new Date()),
                    iscurrencyexchangewindow:true
                }
            });
        this.store.on('load',this.applyTemplate,this)
        this.gridcm.setRenderer(1, this.currencylink.createDelegate(this));
        }
        if(Wtf.account.companyAccountPref.activateToDateforExchangeRates){
            this.currencyExchangeRateStore.load();
        }
      
    }, 
    currencylink:function(val){
        return WtfGlobal.currencyLinkRenderer(val,WtfGlobal.getLocaleText("acc.currency.his"));
    },
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        var rec=this.store.getAt(i);
        if(header=="fromcurrency"||header=="tocurrency")
            callCurrencyExchangeDetails("list"+rec.data["id"],rec.data["id"], rec.data["fromcurrency"]+' To '+rec.data["tocurrency"],true);
        Wtf.getCmp("list"+rec.data["id"]).on('update',function(){
            this.store.reload()
            },this);    
    },
    updateRow:function(obj){
        if(obj!=null){
            var rec=obj.record;
            if(obj.field=="exchangeratetype" && this.isRevaluation){
                if(obj.value != obj.originalValue){
                    obj.record.set("newexchangerate", (1/rec.get('newexchangerate')));
                }
            }
            if(obj.field=="exchangerate"){
                if(obj.value != obj.originalValue){
                    var revExchangeRate = 1/((rec.get('exchangerate')*1)-0);
                    revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
                    obj.record.set("foreigntobaseexchangerate", revExchangeRate);
        }
            }
            if(obj.field=="foreigntobaseexchangerate"){
                if(obj.value != obj.originalValue){
                    var revExchangeRate = 1/((rec.get('foreigntobaseexchangerate')*1)-0);
                    revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
                    obj.record.set("exchangerate", revExchangeRate);
                }
            }
        }
    },    
    closeWin:function(){
        this.fireEvent('cancel',this)
        this.close();
    },
    getUpdatedDetails:function(){
        var arr=[];
        this.store.clearFilter();
        for(var i=0;i<this.store.getCount();i++){                           //outer for loop with Grid store
            var rec=this.store.getAt(i)
            if((rec.dirty||rec.data.companyid=="")){
                var currExchangeRateStore = [];
                this.currencyExchangeRateStore.each(function(record){
                    if(record.get("tocurrencyid")==rec.data.tocurrencyid)
                        currExchangeRateStore.push(record);
                });        
                for(var j=0;j<currExchangeRateStore.length;j++){            //inner for loop with history of exchange rates store
                    var excrec = currExchangeRateStore[j];
                    var recApplyDate = new Date(rec.data.applydate);
                    recApplyDate = recApplyDate.clearTime();
                    var recToDate = new Date(rec.data.todate);
                    recToDate = recToDate.clearTime();
                    var excRecApplyDate = new Date(excrec.data.applydate);
                    excRecApplyDate = excRecApplyDate.clearTime();
                    var excRecToDate = new Date(excrec.data.todate);
                    excRecToDate = excRecToDate.clearTime();
                    if (!(+recApplyDate == +excRecApplyDate && +recToDate == +excRecToDate)) {
                        if(rec.data.applydate >= excrec.data.applydate && rec.data.applydate <= excrec.data.todate){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.PeriodisOverlappingwithexistingExchangeRatePleaseEnterperiodcorrectly")],2);
                            return WtfGlobal.getJSONArray(this.grid,true,[]);
                        }else if(rec.data.todate >= excrec.data.applydate && rec.data.todate <= excrec.data.todate){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.PeriodisOverlappingwithexistingExchangeRatePleaseEnterperiodcorrectly")],2);
                            return WtfGlobal.getJSONArray(this.grid,true,[]);
                        }else if(rec.data.applydate > rec.data.todate){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                            return WtfGlobal.getJSONArray(this.grid,true,[]);
                        }else if(rec.data.todate < rec.data.applydate){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                            return WtfGlobal.getJSONArray(this.grid,true,[]);
                        }
                    }
                }
                rec.set('applydate', (new Date(rec.data.applydate).clearTime()));
                if(Wtf.account.companyAccountPref.activateToDateforExchangeRates){
                    rec.set('todate', (new Date(rec.data.todate).clearTime()));
                }else{
                    rec.set('todate', (rec.data.todate!==null && rec.data.todate!=="" && rec.data.todate !== undefined) ? (new Date(rec.data.todate).clearTime()): (new Date(rec.data.applydate).clearTime()));
                }                 
                arr.push(i);
            }
        }        

        return WtfGlobal.getJSONArray(this.grid,true,arr);
    }, 
    getUpdatedDetailsForRevalueate: function() {
        var arr = [];
        this.store.clearFilter();
        for (var i = 0; i < this.store.getCount(); i++) {
            arr.push(i);
        }
        return WtfGlobal.getJSONArray(this.grid, true, arr);
    }, 

    setRateRenderer:function(val){
       return  WtfGlobal.conventCurrencyDecimal(val,"")
    },

    saveData:function(){
        var rec=[];
        rec.mode=202;
        if (this.isRevaluation)
            rec.data = this.getUpdatedDetailsForRevalueate();
        else
            rec.data = this.getUpdatedDetails();
       
        if(rec.data=="[]")
        {
//            this.store.filterBy(function(rec){
//                if(rec.data.tocurrencyid==rec.data.fromcurrencyid)
//                    return false
//                else
//                    return true
//                },this)
            this.close();
            this.filterStore();
            return;
        }
        if (this.isRevaluation) {
            var currencyCode=Wtf.pref.CurrencySymbol;
            var storeRec=WtfGlobal.searchRecord(this.store,this.revalueCurrenyId,"tocurrencyid");
            if(storeRec)
               currencyCode= storeRec.data.currencycode;                    
           new Wtf.account.SetDimensionTypeWin({
                 closable: false,
                modal: true,
                id: 'dimenstionselectionwin',
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                width: 600,
                height: 400,
                reevalueData:rec.data,
                currencodeforReval:currencyCode,
                revalueCurrenyId: this.revalueCurrenyId,
                accTypeId: this.accTypeId,
                startDate: this.startDate,
                endDate: this.endDate,
                resizable: false,
                layout: 'border',
                buttonAlign: 'right'
            }).show();
            this.fireEvent('cancel',this)
            this.close();
            
        } else {
            Wtf.Ajax.requestEx({
//            url:Wtf.req.account+'CompanyManager.jsp',
                url: "ACCCurrency/saveCurrencyExchange.do",
                params: rec
            }, this, this.genSuccessResponse, this.genFailureResponse);
        }
        
    },
  
    genSuccessResponse:function(response){
        if(response.dateexist){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.currency.update"),WtfGlobal.getLocaleText("acc.currency.msg1"),function(btn){
                if(btn!="yes") {this.filterStore(); return; }
                var rec=[];
                rec.mode=202;
                rec.changerate=true;
                rec.data=this.getUpdatedDetails();
                if(rec.data=="[]"){
                    return;
                    this.filterStore();
                }
                Wtf.Ajax.requestEx({
                    //            url:Wtf.req.account+'CompanyManager.jsp',
                    url:"ACCCurrency/saveCurrencyExchange.do",
                    params: rec
                },this,this.genUpdateSuccessResponse,this.genFailureResponse);


            },this);
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
            if(response.success) this.fireEvent('update');
            this.loadStore();
            Wtf.currencyStore.load();
            this.close();
        }
    },
    genUpdateSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
        if(response.success) this.fireEvent('update');
       this.loadStore();
        Wtf.currencyStore.load();
        this.close();
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
});


Wtf.account.mapAccountsWindow = function(config){
    var btnArr=[];
        btnArr.push(this.save=new Wtf.Toolbar.Button({
            text: (this.isRevaluation)?WtfGlobal.getLocaleText("acc.currency.calculate"):WtfGlobal.getLocaleText("acc.currency.sav"),  //'Save and Close',
            scope: this,
            handler: this.saveData.createDelegate(this)
        }))

      btnArr.push(this.cancel=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }))
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.MapAccounts"),//WtfGlobal.getLocaleText("acc.currency.curTab")
        buttons: btnArr
    },config);
    Wtf.account.mapAccountsWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.mapAccountsWindow, Wtf.Window, {    
    onRender: function(config){
        Wtf.account.mapAccountsWindow.superclass.onRender.call(this, config);
        this.createFormItems();
        this.createGrid();

        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(WtfGlobal.getLocaleText("acc.MapAccounts"),WtfGlobal.getLocaleText("acc.MapAccountsforMultiCompany"),"../../images/accounting_image/currency-exchange.jpg",true)
            //html:getTopHtml(WtfGlobal.getLocaleText("acc.currency.title1"),WtfGlobal.getLocaleText("acc.currency.title"),"../../images/accounting_image/currency-exchange.jpg",true)
        },this.headerCalTemp=new Wtf.FormPanel({
            autoScroll: true,
            border: false,
//            width:'100%',
            method :'POST',
            hidden:this.isRevaluation,
            scope: this,
            labelWidth: 100,
            region: 'center',
            bodyStyle : 'background:#F1F1F1;padding-left:10px;padding-top:10px;padding-bottom:10px;',
            items:[this.autoMapChk,this.cmbCompany]
        }),{
            region: 'south',
            border: false,
            height:370,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid
        });
//    this.grid.on('validateedit',this.checkZeroValue, this);
    },
    createFormItems:function(){
        this.autoMapChk= new Wtf.form.Checkbox({
            name:'automap',
            fieldLabel:WtfGlobal.getLocaleText("acc.AutoMaponAccountCodes"),  //'Copy Address',
            checked:false,
            cls : 'custcheckbox',
            width: 10
        });
        
        this.companyRec=new Wtf.data.Record.create([
            {name: 'companyid'},
            {name: 'companyname'}
        ]);
        this.companyStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.companyRec),
            url : "ACCReports/getMappedCompanies.do"
        });
//        this.companyStore.on('load', function(){
//            if(this.companyStore.getCount() > 0){
//                this.cmbCompany.setValue(this.companyStore.getAt(0).get('companyid'));
//            }
//        }, this);

        this.cmbCompany= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.SourceCompany"),
            name:'companyid',
            hiddenName:'companyid',
            width:'100px',
            store:this.companyStore,
            valueField:'companyid',
            displayField:'companyname',
            mode: 'local',
            emptyText:WtfGlobal.getLocaleText("acc.field.Selectsourcecompany"),
            disableKeyFilter:true,
            allowBlank:false,
            triggerAction:'all',
            forceSelection:true,
            typeAhead: true,
            hirarchical:true
        });
        this.companyStore.load();        
        
        this.accRec=new Wtf.data.Record.create([
            {name: 'accountid',mapping:'accid'},
            {name: 'accnamecode'},
            {name:'groupname'},
            {name:'groupid'},
            {name:'level'}
        ]);
        this.accStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec),
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                headerAdded:true,
                consolidateAccMapFlag:true,
                levelFlag:true,
                ignorecustomers:true,  
                ignorevendors:true,
//                group:[2,9,18],
                nondeleted:true
            }
        });        
        this.accStore.load();
        

        this.cmbAccount= new Wtf.form.Select({
            fieldLabel:WtfGlobal.getLocaleText("acc.je.acc"),
            name:'accountid',
            hiddenName:'accountid',
            store:this.accStore,
            valueField:'accountid',
            displayField:'accnamecode',
            mode: 'local',
            lastQuery : '',
            disableKeyFilter:true,
            allowBlank:false,
            triggerAction:'all',
            forceSelection:true,
            typeAhead: true,
            hirarchical:true
        });
        this.gridRec = new Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'id'},
            {name: 'accname'},
            {name: 'acccode'},
            {name: 'accnamecode'},
            {name: 'groupid'},
            {name: 'groupname'},
            {name: 'level'},
            {name: 'parentaccid'},
            {name: 'parentaccname'},
            {name: 'parentacccode'},
            {name: 'parentaccnamecode'},
            {name: 'parentgroupid'},
            {name: 'parentgroupname'},
            {name: 'mappedFlag'}
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.gridRec),
            url : "ACCReports/getAccounts.do",
            baseParams:{
                acctypes:2,
                deleted:false,
                group:12,
                ignore:true,
                mode:2,
                nondeleted:true,
                accMapFlag:true
            }
        });
        
    },
    filterStore:function(store){
         this.store.filterBy(function(rec){
            if(rec.data.tocurrencyid==rec.data.fromcurrencyid)
                return false
            else
                return true
        },this)
    },
    checkZeroValue:function(obj){
        if(obj.field=="exchangerate"){
            if(obj.value==0)
                obj.cancel=true;
        }
    },
    createGrid:function(){      
        this.gridcm= new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),{
            header: WtfGlobal.getLocaleText("acc.field.SourceAccountName"),//WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridAccountName"),  //"Account Name",
            dataIndex: 'accnamecode'
        },{
            header:WtfGlobal.getLocaleText("acc.multiCompany.srcAccType"),//"Source Account Type",
            dataIndex: 'groupname'
        },{
             header:WtfGlobal.getLocaleText("acc.multiCompany.grpAccName"),//"Group Account Name",
             width:200,
             dataIndex:'parentaccid',
             renderer:Wtf.comboBoxRenderer(this.cmbAccount),
             editor:this.cmbAccount
        },{
            header: WtfGlobal.getLocaleText("acc.multiCompany.grpAccType"),//WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridAccountName"),  //"Account Name",
            dataIndex: 'parentgroupname'
        },{
            header: WtfGlobal.getLocaleText("acc.field.Mapped"),//WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridAccountName"),  //"Account Name",
            dataIndex: 'mappedFlag',
            width:50,
            renderer:function(val, meta, rec){
                if(val=="1") {
                    return "Yes";
                } else {
                    return "No";
                }
            }
        },{
            width:50,
            header:WtfGlobal.getLocaleText("acc.masterConfig.costCenter.action"),  //'Action',
            renderer:this.deleteRenderer.createDelegate(this)
        }]);        
        this.grid = new Wtf.grid.EditorGridPanel({
            cls:'vline-on',
            layout:'fit',
            autoScroll:true,
            height:200,
//            id:(this.currencyhistory?'currencyhistory':'defaultCurrencygrid'),
            store: this.store,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.cmbCompany.on("select", function(e) {
           this.store.baseParams.childcompanyid=this.cmbCompany.getValue();
           this.store.baseParams.autoMap=this.autoMapChk.getValue()?true:false;
           this.loadStore();
        },this);
        this.grid.on('rowclick',this.processRow,this);
        this.grid.on('afteredit',this.updateRow,this);
        this.grid.on('beforeedit',this.filterAccStore,this);        
    },
    
    deleteRenderer:function(v,m,rec){
        var flag=false;
        var value=rec.data["parentaccid"];
        if(value && value != "") {
            flag = true;
        }
        if(flag){
              var deletegriclass=getButtonIconCls(Wtf.etype.deletegridrow);
            return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
        }
        return "";
    }, 

    loadStore:function(){
       this.store.load();
    },
    processRow: function(grid, rowindex, e) {
        if (e.getTarget(".delete-gridrow")) {
            var rec = grid.getStore().getAt(rowindex);
            var id = 0;
            if (rec) {
                id = rec.data.id;
            }
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn) {
                if (btn != "yes"){
                    return;
                } else {                        
                    if (id == 0) {
                        rec.set('parentaccid', "");
                        rec.set('parentgroupname', "");
                        return;
                    }
                    Wtf.Ajax.requestEx({
                        url: "ACCAccount/deleteAccountMapping.do",
                        params: {
                            mappingid: id
                        }
                    }, this, function(response) {
                        rec.set('parentaccid', "");
                        rec.set('parentgroupname', "");
                        WtfComMsgBox([this.title,response.msg],0);
                    },
                    function(response) {
                    });
                }
            }, this);
        }
    },
    filterAccStore:function(obj){
        if(obj!=null){
            var rec;
            if(obj.field=="parentaccid"){
                this.accStore.clearFilter();
                var childgroupid = obj.record.get("groupid");
                var level = obj.record.get("level");
                this.accStore.filterBy(function(rec){
                    if(rec.data.groupid==childgroupid && rec.data.level==level)
                        return true;
                    else
                        return false;
                },this);
            }
        }
    },
    updateRow:function(obj){
        if(obj!=null){
            var rec;
            if(obj.field=="parentaccid"){                
                var parentaccid = this.cmbAccount.getValue();
                var recIndex = this.accStore.find('accountid',parentaccid);
                if(recIndex >=0) {
                    rec=this.accStore.getAt(recIndex);
                    var parentgroupid = rec.data["groupid"];
                    var parentgroupname = rec.data["groupname"];
                }
                if(obj.record.get("groupid") != parentgroupid) {
                    obj.record.set("parentgroupname", "");
                    obj.record.set("parentaccid", "");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleaseselectaccountwithsameaccounttype")], 2);
                    return;
                } else {
                    obj.record.set("parentgroupname", parentgroupname);
                }
            }
        }
    },
    closeWin:function(){
        this.fireEvent('cancel',this);
        this.close();
    },
    saveData: function() {
        var rec;
        var recData = "";
        var finalarray = [];
        if (this.store.getCount() > 0) {
            for (var arrCount = 0; arrCount < this.store.getCount(); arrCount++)
            {
                var fields = this.store.fields;
                var recarr = [];
                var record = null;
                record = this.store.getAt(arrCount);
                if (record != undefined)
                {
                    for (var j = 0; j < fields.length; j++) {
                        var value = record.data[fields.get(j).name];
                        if ((fields.get(j).name == 'parentaccid' || fields.get(j).name == 'parentgroupname') && value == "")
                        {
                            recarr = [];
                            break;
                        }
                        else
                        {
                            switch (fields.get(j).type) {
                                case "auto":
                                    if (value != undefined) {
                                        value = (value + "").trim();
                                    }
                                    value = encodeURI(value);
                                    value = "\"" + value + "\"";
                                    break;
                                case "date":
                                    value = "'" + WtfGlobal.convertToGenericDate(value) + "'";
                                    break;
                            }
                            recarr.push(fields.get(j).name + ":" + value);
                        }
                    }
                }
                if (recarr.length > 0)
                {
                    recarr.push("modified:" + record.dirty);
                    finalarray.push("{" + recarr.join(",") + "}");
                }
            }
            recData = "[" + finalarray.join(',') + "]";


            rec = {
                jsondata: recData
            };


            Wtf.Ajax.requestEx({
                url: "ACCAccount/saveUpdateAccountMapping.do",
                params: rec
            }, this, this.genSuccessResponse, this.genFailureResponse);

        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Norecordsavailabetoupdate")], 2);
        }
    },
    genSuccessResponse:function(response){
        WtfComMsgBox([this.title,response.msg],0);
        if(response.success){               
            this.close();
        }
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
});

Wtf.account.ExchangeRateswindow = function(config) {
    this.uPermType = Wtf.UPerm.currencyexchange;
    this.permType = Wtf.Perm.currencyexchange;
    var btnArr = [];

    if (!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)) {
        btnArr.push(this.save = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            scope: this,
            handler: this.saveData.createDelegate(this)
        }))
    }
    btnArr.push(this.cancel = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
        scope: this,
        handler: this.closeWin.createDelegate(this)
    }))
    Wtf.apply(this, {
        buttons: btnArr
    }, config);
    Wtf.account.ExchangeRateswindow.superclass.constructor.call(this, config);

}
Wtf.extend(Wtf.account.ExchangeRateswindow, Wtf.Window, {
    onRender: function(config) {
        Wtf.account.ExchangeRateswindow.superclass.onRender.call(this, config);
        this.createStore();
        this.createGrid();
        this.loadStore();
        this.store.on('load', this.filterStore, this)
        this.setBttn.on('click', function() {
            this.grid.getView().refresh();
            this.loadStore();
        }, this)
        this.add({
            region: 'north',
            height: 70,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getImportTopHtml(WtfGlobal.getLocaleText("acc.common.downloadexchangerates"), "<ul style='list-style-type:disc;padding-left:15px;'><li><b>Note</b>: " + WtfGlobal.getLocaleText("acc.currency.downloadcurrencyraterate") + "</li></ul>", "../../images/import.png", true, "5px 0px 0px 0px", "7px 0px 0px 10px")
        },
        this.centerPanel = new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan' + this.id,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px;padding-bottom: 2px',
            baseCls: 'bckgroundcolor',
            layout: 'fit',
            items: [this.TypeForm, this.grid]
        })

                )
    },
    createGrid: function() {
        this.setBttn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.set"),
            tooltip: WtfGlobal.getLocaleText("acc.common.set"),
            id: 'submitRec' + this.id,
            scope: this,
            disabled: false

        });

        this.applyDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.currency.app"),
            name: 'stdate' + this.id,
            id: 'applydate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: new Date().clearTime(true)
        });
        this.toDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.currency.ToDate"),
            name: 'todate' + this.id,
            id: 'todate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: new Date().clearTime(true)
        });
        this.TypeForm = new Wtf.form.FormPanel({
            region: 'center',
            autoScroll: true,
            border: false,
            autoHeight: true,
            layout: 'form',
            bodyStyle: 'background:transparent;border-bottom:1px solid #bfbfbf;padding-left: 28px;',
            items: [
                {layout: 'column',
                    border: false,
                    items: [{
                            layout: 'form',
                            columnWidth: 0.32,
                            border: false,
                            items: this.applyDate
                        }, Wtf.account.companyAccountPref.activateToDateforExchangeRates ? {
                            layout: 'form',
                            columnWidth: 0.32,
                            border: false,
                            items: this.toDate
                        } : {border: false}, {
                            columnWidth: 0.36,
                            layout: 'form',
                            border: false,
                            items: this.setBttn
                        },
                    ]


                }, ]
        });
        this.gridcm = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(), {
                header: WtfGlobal.getLocaleText("acc.currency.cur"), //"Currency",
                dataIndex: 'tocurrency',
                autoWidth: true

            },
            {header: WtfGlobal.getLocaleText("acc.currency.exRate"), //"Exchange Rate1",
                dataIndex: 'exchangerate',
                renderer: this.setRateRenderer.createDelegate(this),
                editor: this.editorFlag ? "" : this.exchangeRate = new Wtf.form.NumberField({
                    allowBlank: false,
                    decimalPrecision: 9,
                    allowNegative: false,
                    minValue: 0
                })
            }, {
                header: Wtf.account.companyAccountPref.activateToDateforExchangeRates ? WtfGlobal.getLocaleText("acc.currency.FromDate") : (this.currencyhistory ? WtfGlobal.getLocaleText("acc.currency.app") : WtfGlobal.getLocaleText("acc.currency.lastapp")), //"From Date" :"Applied Date":"Last Applied Date",
                dataIndex: 'applydate',
                minValue: new Date().clearTime(true),
                disabled: true,
                renderer: function(v) {
                    v = "";
                    v = Wtf.getCmp('applydate').getValue().format(WtfGlobal.getOnlyDateFormat());
                    return v;
                }

            }, {
                header: WtfGlobal.getLocaleText("acc.currency.ToDate"), //"To Date",
                dataIndex: 'todate',
                renderer: WtfGlobal.onlyDateRenderer,
                minValue: new Date().clearTime(true),
                hidden: !Wtf.account.companyAccountPref.activateToDateforExchangeRates,
                disabled: true,
                renderer:function(v) {
                    v = "";
                    v = Wtf.getCmp('todate').getValue().format(WtfGlobal.getOnlyDateFormat());
                    return v;
                }
            }
        ]);
        if (!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit))
            this.grid = new Wtf.grid.EditorGridPanel({
                cls: 'vline-on',
                layout: 'fit',
                autoScroll: false,
                height: 370,
                id: 'defaultCurrencygrid',
                store: this.store,
                cm: this.gridcm,
                border: false,
                loadMask: true,
                style: "width: 768px; height: 392px;",
                viewConfig: {
                    forceFit: true,
                    emptyText: WtfGlobal.getLocaleText("acc.common.norec")
                }
            });




    },
    createStore: function() {
        this.gridRec = new Wtf.data.Record.create([{
                name: 'id'
            }, {
                name: 'applydate',
                type: 'date'
            }, {
                name: 'fromcurrency'
            }, {
                name: 'tocurrency'
            }, {
                name: 'exchangerate',
                type: 'float'
            }, {
                name: 'newexchangerate',
                type: 'float'
            }, {
                name: 'tocurrencyid'
            }, {
                name: 'fromcurrencyid'
            }, {
                name: 'companyid'
            }, {
                name: 'currencycode'
            }, {
                name: 'exchangeratetype', defValue: 0
            }, {
                name: 'todate',
                type: 'date'
            }]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.gridRec),
            url: "ACCCurrency/getDefaultCurrencyExchange.do"// getCurrencyExchange
        });
        this.currencyExchangeRateStore = new Wtf.data.Store({
            url: "ACCCurrency/getCurrencyExchangeList.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.gridRec)
        });
    },
    loadStore: function() {
        var recApplyDate = new Date(Wtf.getCmp('applydate').getValue()).clearTime();
        this.store.load({
            params: {
                mode: 201,
                transactiondate: WtfGlobal.convertToGenericDate(recApplyDate),
                downloadexchangerate: true
            }});
    },
    filterStore: function(store) {
        store.filterBy(function(rec) {
            if (rec.data.tocurrencyid == rec.data.fromcurrencyid) {
                return false
            }
            else {
                return true
            }
        }, this);
    },
    closeWin: function() {
        this.close();
    },
    setRateRenderer: function(val) {
        return  WtfGlobal.conventCurrencyDecimal(val, "")
    },
    getUpdatedDetails: function() {
        var arr = [];
        this.store.clearFilter();
        for (var i = 0; i < this.store.getCount(); i++) {                           //outer for loop with Grid store
            var rec = this.store.getAt(i)
            var recApplyDate = new Date(Wtf.getCmp('applydate').getValue()).clearTime();
            if (Wtf.account.companyAccountPref.activateToDateforExchangeRates) {
                var recToDate = new Date(Wtf.getCmp('todate').getValue()).clearTime();
                if (recApplyDate > recToDate) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                    return WtfGlobal.getJSONArray(this.grid, true, []);
                } else if (recToDate < recApplyDate) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                    return WtfGlobal.getJSONArray(this.grid, true, []);
                }
                rec.set('todate', recToDate);
            }
            rec.set('applydate', recApplyDate);
            arr.push(i);
        }
        return WtfGlobal.getJSONArray(this.grid, true, arr);
    },
    saveData: function() {
        var rec = [];
        rec = this.savedatadetails();
        Wtf.Ajax.requestEx({
            url: "ACCCurrency/saveCurrencyExchange.do",
            params: rec
        }, this, this.genSuccessResponse, this.genFailureResponse);
    },
    genSuccessResponse: function(response) {
        if (response.dateexist) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.currency.update"), WtfGlobal.getLocaleText("acc.currency.msg1"), function(btn) {
                if (btn != "yes") {
                    this.filterStore(this.store);
                    return;
                }
                var rec = [];
                rec = this.savedatadetails();
                rec.changerate = true;
                Wtf.Ajax.requestEx({
                    url: "ACCCurrency/saveCurrencyExchange.do",
                    params: rec
                }, this, this.genUpdateSuccessResponse, this.genFailureResponse);


            }, this);
        } else {
            this.genUpdateSuccessResponse(response);
        }
    },
    genUpdateSuccessResponse: function(response) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), response.msg], response.success * 2 + 1);
        this.loadStore();
        Wtf.currencyStore.load();
        this.close();
    },
    genFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    savedatadetails: function() {
        var rec = [];
        rec.mode = 202;
        rec.data = this.getUpdatedDetails();
        this.filterStore(this.store);
        if (rec.data == "[]") {
            this.close();
            return;
        }
        return rec;
    }
})

Wtf.account.CustomizeCurrencywindow = function(config) {
    var btnArr = [];
    btnArr.push(this.cancel = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
        scope: this,
        handler: this.closeWin.createDelegate(this)
    }))
    Wtf.apply(this, {
        buttons: btnArr
    }, config);
    Wtf.account.CustomizeCurrencywindow.superclass.constructor.call(this, config);

}

Wtf.extend(Wtf.account.CustomizeCurrencywindow, Wtf.Window, {
    onRender: function(config) {
        Wtf.account.CustomizeCurrencywindow.superclass.onRender.call(this, config);
        this.createStore();
        this.createGrid();
        this.loadStore();
        this.store.on('load', this.filterStore, this)
        this.add({
            region: 'north',
            height: 70,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getImportTopHtml(WtfGlobal.getLocaleText("acc.common.customizecurrencysymbolcode"),"<ul style='list-style-type:disc;padding-left:15px;'><li><b>Note</b>: " + WtfGlobal.getLocaleText("acc.currency.note") + "</li></ul>", "../../images/CustomCurrencySymbol.png", true, "5px 0px 0px 0px", "7px 0px 0px 10px")
    },
        this.centerPanel = new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan' + this.id,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px;padding-bottom: 2px',
            baseCls: 'bckgroundcolor',
            layout: 'fit',
            items: [this.TypeForm,this.grid]
        })

        )
    },
    
    createGrid: function() {
        this.currencyRec = new Wtf.data.Record.create([
        {
            name: 'currencyid',
            mapping: 'tocurrencyid'
        },

        {
            name: 'symbol'
        },

        {
            name: 'currencyname',
            mapping: 'tocurrency'
        },

        {
            name: 'htmlcode'
        },

        {
            name: 'erdid',
            mapping:'id'
        },

        {
            name: 'companyid'
        },

        {
            name: 'currencycode'
        }
        ]);
        this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.currencyRec),
            url: "ACCCurrency/getCurrencyExchange.do"
        });
        this.currencyStore.load({
            params:{
                mode:201,
                transactiondate:WtfGlobal.convertToGenericDate(new Date())
            }
        });
        this.Currency = new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mp.cur"), //'Currency*',
            hiddenName: 'currencyid',
            labelStyle:'width:100px;margin-left: 5px;',
            name:'currencyid',
            id: "currency",
            anchor: '80%',
            allowBlank: false,
            store: this.currencyStore,
            disabled:this.isEdit,
            valueField: 'currencyid',
            forceSelection: true,
            displayField: 'currencyname',
            scope: this,
            selectOnFocus: true
        });
        this.Currency.on("select",this.onCurrencySelect, this);
        this.customCode = new Wtf.form.TextField({
            fieldLabel : WtfGlobal.getLocaleText("acc.currency.customcode"),
            labelStyle:'width:100px;margin-left: 5px;',
            allowBlank: true,
            maxLength:250
            
        });
        this.customSymbol = new Wtf.form.TextField({
            fieldLabel : WtfGlobal.getLocaleText("acc.currency.customsymbol"),
            labelStyle:'width:100px;margin-left: 5px;',
            allowBlank: true,
            maxLength:250
            
        });

        this.systemCode = new Wtf.Panel({
            xtype: 'panel', 
            border: false,
            style:"width:120px;margin-left: 5px;",
            width:800,
            id:"swapnilanand1",
            html: "<div style='font-size:12px;'>"+WtfGlobal.getLocaleText('acc.currency.systemcode')+':'+"</div>"
        });
   
        this.systemSymbol = new Wtf.Panel({
            xtype: 'panel', 
            border: false,
            style:"width:120px;margin-left: 5px;",
            width:800,
            id:"swapnilanand",
            html: "<div style='font-size:12px;'>"+WtfGlobal.getLocaleText('acc.currency.systemsymbol')+':'+"</div>"
        });
     
        this.submitBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.submit"),
            iconCls :getButtonIconCls(Wtf.etype.save),
            id: 'BtnSubNew' + this.id,
            scope: this
        });
    
        this.submitBttn.on('click',this.submit,this);
        this.TypeForm = new Wtf.form.FormPanel({
            region: 'center',
            autoScroll: true,
            border: false,
            autoHeight: true,
            layout: 'form',
            bodyStyle: 'background:transparent;border-bottom:1px solid #bfbfbf;padding-left: 2px;',
            items: [
            {
                layout: 'column',
                border: false,
                items: [{
                    layout:'form',
                    columnWidth:0.50,
                    border:false,
                    labelWidth:150,
                    defaults : {
                        anchor:'90%'
                    },
                    items:[this.Currency,this.customCode,this.customSymbol]
                },
                {
                    layout:'form',
                    columnWidth:0.50,
                    border:false,
                    labelWidth:150,
                    bodyStyle:'padding:30px 13px 13px 13px',
                    defaults : {
                        anchor:'90%'
                    },
                    items:[this.systemCode,this.systemSymbol]
                }],
                bbar:[this.submitBttn]


            }, ]
        });
        this.currencygridcm = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(), {
            header: WtfGlobal.getLocaleText("acc.currency.cur"), //Currency
            dataIndex: 'name',
            autoWidth: true
        },
        //        {
        //            header: WtfGlobal.getLocaleText("acc.currency.systemsymbol"), //System Symbol
        //            dataIndex: 'systemcurrencysymbol',
        //            autoWidth: true
        //        },
        //        {
        //            header: WtfGlobal.getLocaleText("acc.currency.systemcode"), //System Code
        //            dataIndex: 'systemcurrencycode',
        //            autoWidth: true
        //                
        //        },
        {
            header: WtfGlobal.getLocaleText("acc.currency.customcode"), //Custom Code
            dataIndex: 'customcurrencycode',
            autoWidth: true,
            flex: 1,
            editor: new Wtf.form.TextField({
                allowBlank: false,
                maxLength:50
            })
        },
        {
            header: WtfGlobal.getLocaleText("acc.currency.customsymbol"), //Custom Symbol
            dataIndex: 'customcurrencysymbol',
            autoWidth: true,
            flex: 1,
            editor:  new Wtf.form.TextField({
                allowBlank: false,
                maxLength:50
            })
        },
        {
            header : WtfGlobal.getLocaleText("acc.product.gridAction"),//Action
            width: 100,
            renderer: function (value, css, record, row, column, store) {
                return "<div class='delete pwnd delete-gridrow'></div>";
            }
        }
        ]);
        this.grid = new Wtf.grid.EditorGridPanel({
            cls: 'vline-on',
            layout: 'fit',
            autoScroll: false,
            height: 285,
            id: 'customCurrencygrid',
            store: this.store,
            cm: this.currencygridcm,
            sm: new Wtf.grid.RowSelectionModel({
                singleSelect:true
            }),
            border: false,
            style: "width: 765px; height: 290px;",
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        })
           
            this.grid.on("rowclick", this.rowClickHandle, this);
    },
    createStore: function() {
        this.gridRec = new Wtf.data.Record.create([{
            name: 'id'
        },{
            name: 'currencyid'
        }, {
            name: 'name'
        },
        {
            name: 'systemcurrencysymbol'
        },{
            name: 'systemcurrencycode'
        },
        {
            name: 'customcurrencycode'
        },{
            name: 'customcurrencysymbol'
        }]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.gridRec),
            url: "ACCCurrency/getCustomCurrency.do"
        });
    },
    loadStore: function() {
        this.store.load();
    },
    closeWin: function() {
        this.close();
    },
    onCurrencySelect : function(index,rec){
        var customRec = WtfGlobal.searchRecord(this.store, this.Currency.getValue(), 'currencyid');
        this.customCode.reset();
        this.customSymbol.reset();
        if(customRec!=null && customRec.data!=null){
            this.customCode.setValue(customRec.data.customcurrencycode);
            this.customSymbol.setValue(customRec.data.customcurrencysymbol);
        }
        this.systemCode.el.dom.innerHTML=this.systemCode.el.dom.innerHTML="<div style='font-size:12px;'>"+WtfGlobal.getLocaleText('acc.currency.systemcode')+":&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+rec.data.currencycode+"</div>";
        this.systemSymbol.el.dom.innerHTML=this.systemSymbol.el.dom.innerHTML="<div style='font-size:12px;'>"+WtfGlobal.getLocaleText('acc.currency.systemsymbol')+":&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+rec.data.symbol+"</div>";
    },
   
    rowClickHandle : function(grid,rowindex,e){
        var recData = grid.getSelectionModel().getSelected().data;  
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.show({
                title: "Warning",
                msg: WtfGlobal.getLocaleText("acc.currency.customsymbol.wanttodeletecurrency"),
                width: 370,
                scope:this,
                buttons: Wtf.MessageBox.YESNO,
                fn: function(btn) {
                    if ( btn === "yes") {
                        Wtf.Ajax.requestEx({
                            url: "ACCCurrency/deleteCustomCurrency.do",
                            params: {
                                currencyid:recData.currencyid
                            }
                        }, this, function(response) {
                            this.store.load();
                            var msg=WtfGlobal.getLocaleText("acc.currency.customsymbol.deletedsuccessfully");
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.customizecurrencysymbolcode"), msg], response.success * 2 + 1);
                        },
                        function(response) {
                            });
                    } 
                },
                icon: Wtf.MessageBox.QUESTION
            });
        }else{
            this.Currency.setValue(recData.currencyid)
            this.customCode.setValue(recData.customcurrencycode);
            this.customSymbol.setValue(recData.customcurrencysymbol);
            this.systemCode.el.dom.innerHTML=this.systemCode.el.dom.innerHTML="<div style='font-size:12px;'>"+WtfGlobal.getLocaleText('acc.currency.systemcode')+":&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+recData.systemcurrencycode+"</div>";
            this.systemSymbol.el.dom.innerHTML=this.systemSymbol.el.dom.innerHTML="<div style='font-size:12px;'>"+WtfGlobal.getLocaleText('acc.currency.systemsymbol')+":&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+recData.systemcurrencysymbol+"</div>";
        }
    },
    submit: function() {
        this.store.clearFilter();
        var rec = WtfGlobal.searchRecord(this.Currency.store, this.Currency.getValue(), 'currencyid');
        Wtf.Ajax.requestEx({
            url: "ACCCurrency/saveCustomCurrency.do",
            params: {
                currencyid:this.Currency.getValue(),
                name:rec.data.currencyname,
                customcurrencycode: encodeURIComponent(this.customCode.getValue().trim()),
                customcurrencysymbol: encodeURIComponent(this.customSymbol.getValue().trim()),
                systemcode:rec.data.currencycode,
                systemsymbol:rec.data.symbol
                
            }
        }, this, this.genUpdateSuccessResponse, this.genFailureResponse);
    },
       
    genUpdateSuccessResponse: function(response) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.customizecurrencysymbolcode"), response.msg], response.success * 2 + 1);
        this.store.load();
        this.Currency.reset();
        this.customCode.reset();
        this.customSymbol.reset();
        this.systemCode.reset();
        this.systemSymbol.reset();
    },
    genFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    }
})