/*
 * Copyright (C) 2016  Krawler Information Systems Pvt Ltd
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
Wtf.account.TaxCurrencyExchangeWindow = function(config){
    this.currencyhistory=config.currencyhistory||false;
    this.uPermType=Wtf.UPerm.currencyexchange;
    this.permType=Wtf.Perm.currencyexchange;
    var btnArr=[];

    this.moduleName = Wtf.Tax_Currency_Exchange;
    var extraConfig = {};
    extraConfig.url = "ACCCurrency/importCurrencyExchange.do";
    var extraParams = "";
    var importCurrencyExchangeBtnArray = Wtf.importMenuArray(this, this.moduleName, config.store, extraParams, extraConfig);

    this.importCurrencyBtn = new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.common.import"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.import"),
        iconCls: (Wtf.isChrome ? 'pwnd importChrome' : 'pwnd import'),
        menu: importCurrencyExchangeBtnArray,
        typeXLSFile:true
    });

    if (!this.currencyhistory && !config.isRevaluation) {
        btnArr.push(this.importCurrencyBtn);
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
    Wtf.account.TaxCurrencyExchangeWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.TaxCurrencyExchangeWindow, Wtf.Window, {
    defaultCurreny:false,
    draggable:false,
    onRender: function(config){
        Wtf.account.TaxCurrencyExchangeWindow.superclass.onRender.call(this, config);
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
            url:"ACCCurrencyExchange/getTaxCurrencyExchangeList.do",
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
        
        var data=[[0,'Foreign to Base'],[1,'Base to Foreign']];
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
       this.store.proxy.conn.url = "ACCCurrencyExchange/getTaxCurrencyExchangeList.do";
       if(this.currencyhistory){
       this.store.load({
            params:{
                mode:203,
                currencyid:this.currencyid
                }
            });
        }
        else{
        this.store.proxy.conn.url = "ACCCurrencyExchange/getTaxCurrencyExchange.do";
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
            callTaxCurrencyExchangeDetails("list"+rec.data["id"],rec.data["id"], rec.data["fromcurrency"]+' To '+rec.data["tocurrency"],true);
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
                rec.set('todate', (new Date(rec.data.todate).clearTime()));
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
                url: "ACCCurrencyExchange/saveTaxCurrencyExchange.do",
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
                    url:"ACCCurrencyExchange/saveTaxCurrencyExchange.do",
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