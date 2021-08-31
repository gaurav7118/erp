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
Wtf.account.BankReconciliationReport=function(config){
this.accid=config.accid;
this.sm=new Wtf.grid.CheckboxSelectionModel();
    this.GridRec = Wtf.data.Record.create ([
        {name:'id'},
        {name:'startdate',type:'date'},
        {name:'enddate',type:'date'},
        {name:'clearanceDate',type:'date'},
        {name:'clearingbalance'},
        {name:'endingbalance'},
        {name:'difference'},
        {name:'accountname'},
        {name:'accountid'},
        {name:'transactionID'},
        {name:'type'},
        {name:'d_entryno'},
        {name:'jeid'},
        {name:'billid'},
        {name:'withoutinventory'}
    ]);
    this.Store = new Wtf.data.Store({
        url: "ACCReports/getBankReconciliation.do",
        baseParams:{
            mode:57
        },
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.GridRec)
    });
    this.deleteBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.rem.7"),  //'Delete',
        tooltip:WtfGlobal.getLocaleText("acc.rem.117"),
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.deletebutton)
    });
    this.deleteBttn.on('click',this.deleteRows,this);
    this.btnArr=new Wtf.Toolbar({
            items:[this.deleteBttn]});
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        border:false,
        viewConfig:{forceFit:true} ,
        forceFit:true,
        loadMask : true,
        sm:this.sm,
        columns:[this.sm,{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.startDate"),  //"Start Date",
            dataIndex:'startdate',
            width:30,
            align:'center',
            pdfwidth:150,
            sortable:true,
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.endDate"),  //"End Date",
            dataIndex:'enddate',
            width:30,
            align:'center',
            pdfwidth:150,
            sortable:true,
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.field.TransactionID"),
            dataIndex:'transactionID',
            width:30,
            pdfwidth:50,
            sortable:true,
            renderer:function(value,meta,rec){

                meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Transaction ID' ";
                if(!value) return value;

                value = WtfGlobal.linkRenderer(value,meta,rec)

                return value;
            }
        },{
            header: WtfGlobal.getLocaleText("acc.bankReconcile.gridJournalFolio"),  //"Journal Folio (J/F)",
            dataIndex: 'd_entryno',
            width:50,
            renderer:WtfGlobal.linkRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.clrdBal"),  //"Cleared Balance",
            dataIndex:'clearingbalance',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:150,
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.clearanceDate"),
            dataIndex:'clearanceDate',
            width:30,
            align:'center',
            pdfwidth:150,
            sortable:true,
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.endBal"),  //"Ending Balance",
            dataIndex:'endingbalance',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:150,
            sortable:true
         },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.diff"),  //"Difference",
            dataIndex:'difference',
            renderer:this.diffRenderer.createDelegate(this),
            pdfwidth:150,
            sortable:true
        }]
    });
    Wtf.apply(this,{
        items:[{
            region:'center',
            layout:'fit',
            border:false,
            items:this.grid
        }],
        tbar:[this.deleteBttn,{
                    xtype:'button',
                    text:WtfGlobal.getLocaleText("acc.ra.fetch"),
                    tooltip:WtfGlobal.getLocaleText("acc.bankReconcile.refreshReportTT"),
                    iconCls:getButtonIconCls(Wtf.etype.resetbutton),
                    scope:this,
                    handler:this.loadParmStore
                }]
    },config);
    Wtf.account.BankReconciliationReport.superclass.constructor.call(this,config);
    this.addEvents({
       'journalentry': true
    });
    this.grid.on('render',this.loadParmStore,this)
    this.grid.on('cellclick',this.onCellClick, this);
}
 Wtf.extend(Wtf.account.BankReconciliationReport,Wtf.Panel,{
    diffRenderer:function(val){
        return "<span font-color='red'>"+WtfGlobal.currencyRenderer(val)+"</span>";
    },
    loadParmStore:function(){
        if(this.entryID==null)
            this.Store.load({params:{accid:this.accid}});
    },
    loadStore:function(accid){
        if(this.entryID==null)
            this.Store.load({params:{accid:accid}});
    },
    handleResetClick:function(){
       if(this.quickPanelSearch.getValue()){
           this.quickPanelSearch.reset();
           this.Store.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
           });
        }
    },
    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
    },
    dataChanged:function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    },
    deleteRows: function(){
       var arr=[];
       var data=[];
       this.recArr = this.grid.getSelectionModel().getSelections();
       if(!this.grid.getSelectionModel().hasSelection()){
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.bankReconcile.msg3")],1);
           return;
       }
       this.grid.getSelectionModel().clearSelections();
       WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
       Wtf.MessageBox.show({
       title: WtfGlobal.getLocaleText("acc.common.warning"), //"Warning",
       msg: WtfGlobal.getLocaleText("acc.bankReconcile.msg4")+"<div><b>"+WtfGlobal.getLocaleText("acc.bankReconcile.msg5")+"</b></div>",
       width: 500,
       buttons: Wtf.MessageBox.OKCANCEL,
       animEl: 'upbtn',
       icon: Wtf.MessageBox.QUESTION,
       scope:this,
       fn:function(btn){
            if(btn!="ok"){
                for(var i=0;i<this.recArr.length;i++){
                    var ind=this.Store.indexOf(this.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                }
                return;
            }
            for(i=0;i<this.recArr.length;i++){
                arr.push(this.Store.indexOf(this.recArr[i]));
        }
        data= WtfGlobal.getJSONArray(this.grid,true,arr);
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url: "ACCReconciliation/deleteBankReconciliation.do",
                params:{
                   data:data,
                    mode:58
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
       }});
    },

    genSuccessResponse:function(response){
       WtfComMsgBox([WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"),response.msg],response.success*2+1);
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.Store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        if(response.success){
            (function(){
            this.Store.reload();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }
    },

    genFailureResponse:function(response){
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.Store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");
        if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var dataindex=g.getColumnModel().getDataIndex(j);
        if(dataindex == "transactionID"){
            var formrec = this.Store.getAt(i);
            var type=formrec.data['type'];
            var withoutinventory=formrec.data['withoutinventory'];            
            viewTransactionTemplate1(type, formrec,withoutinventory);
        } else if(dataindex == "d_entryno") {
            var formrec = this.Store.getAt(i);
            var jid = formrec.data['jeid'];
            this.fireEvent('journalentry', jid, true);
        }
    }
});
