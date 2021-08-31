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
Wtf.common.WtfAuditTrail = function(config){
    Wtf.common.WtfAuditTrail.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.common.WtfAuditTrail,Wtf.Panel,{
    onRender : function(config){
        Wtf.common.WtfAuditTrail.superclass.onRender.call(this,config);
        
        this.groupingView1 = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: true,
            hideGroupedColumn: true
        });
    
        this.auditRecord = Wtf.data.Record.create([
        { 
            name: 'username',
            type: 'string'
        },{
            name: 'auditid',
            type: 'string'
        },{
            name: 'details',
            type: 'string'
        },{
            name: 'link',
            type: 'string'
        },{
            name: 'timestamp'
        },{
            name: 'ipaddr',
            type: 'string'
        },{
            name: 'actionname',
            type: 'string'
        }
        ]);
    
    
        this.auditReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, this.auditRecord);
    
    this.auditStore = new Wtf.data.Store({
            proxy: new Wtf.data.HttpProxy({
            url: "ACCAudit/getAuditData.do"
        }),
        reader: this.auditReader,
        remoteSort : true
    });
    
        this.cmodel = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer({width:40}),
       /* {
            header: WtfGlobal.getLocaleText("acc.auditTrail.action"),  //"Action",
            tip:WtfGlobal.getLocaleText("acc.auditTrail.action"),  //'Action',
            width: 150,
            hidden:true,
            dataIndex: 'actionname'
        },*/
            {
            header: WtfGlobal.getLocaleText("acc.auditTrail.gridDetails"),  //"Details",
            tip:WtfGlobal.getLocaleText("acc.auditTrail.gridDetails"),  //'Details',
            width: 240,
            renderer : function(val, p , rec) {
                return "<div wtf:qtip=\""+unescape(val)+"\"wtf:qtitle="+ WtfGlobal.getLocaleText("acc.auditTrail.gridDetails") +">"+unescape(val + rec.data.link)+"</div>";
            },
            dataIndex: 'details'
        }, {
            header: WtfGlobal.getLocaleText("acc.auditTrail.gridUser"),  //"User",
            tip:WtfGlobal.getLocaleText("acc.auditTrail.gridUser"),  //'User Name',
            width: 120,
            dataIndex: 'username'
        }, {
            header: WtfGlobal.getLocaleText("acc.auditTrail.gridTimestamp"),  //"Timestamp",
            tip:WtfGlobal.getLocaleText("acc.auditTrail.gridTimestamp"),  //'Time',
            width: 160,
            align:'center',
            dataIndex: 'timestamp'
        }, {
            header: WtfGlobal.getLocaleText("acc.auditTrail.gridIPaddress"),  //"IP Address",
            tip: WtfGlobal.getLocaleText("acc.auditTrail.gridIPaddress"),
            align:'right',
            width: 100,
            dataIndex: 'ipaddr',
            groupable:true
        }]);
            
        this.grid=new Wtf.grid.GridPanel({
            ds: this.auditStore,
            cm: this.cmodel,
            border: false,
            viewConfig:{forceFit:true,emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))},
            view: new Wtf.ux.KWLGridView({forceFit:true}),
            trackMouseOver: true,
            loadMask: {
                msg: WtfGlobal.getLocaleText("acc.msgbox.50")
            }
        });
    
        this.cmodel.defaultSortable = true;
    
        this.comboReader = new Wtf.data.Record.create([
        {
            name: 'id',
            type: 'string'
        },{
            name: 'name',
            type: 'string'
        }
        ]);

        this.groupRecord = Wtf.data.Record.create([
        {
            name: 'groupname',
            type: 'string'
        },{
            name: 'groupid',
            type: 'string'
        }
        ]);

        this.groupReader = new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.groupRecord);

        this.groupStore = new Wtf.data.Store({
                proxy: new Wtf.data.HttpProxy({
                url : "ACCAudit/getAuditGroupData.do"
            }),
            reader: this.groupReader
        });
      
        this.resetBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
                tooltip :WtfGlobal.getLocaleText("acc.auditTrail.resetTip"),  //'Reset Search Result.',
                id: 'btnRec' + this.id,
                scope: this,
                iconCls :getButtonIconCls(Wtf.etype.resetbutton),
                disabled :false
        });
        this.resetBttn.on('click',this.handleResetClick,this);

        this.startDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:Wtf.account.companyAccountPref.fyfrom
        });
        this.endDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            value: Wtf.serverDate	
        });

        this.searchBttn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
            scope: this,
            id:"search"+this.helpmodeid,
            handler: this.searchHandler,
            iconCls:'accountingbase fetch'
        });
        this.fT = new Wtf.form.TextField({
            fieldLabel : WtfGlobal.getLocaleText("acc.auditTrail.con"),  //"Contains",
            emptyText : WtfGlobal.getLocaleText("acc.auditTrail.emp1"),  //"Search by Details/User Name",  // -- Search Text --",
            width : 200
        });
                            
        this.groupCombo=new Wtf.form.ComboBox({
            id:'selectTransaction' + this.helpmodeid,
            emptyText : WtfGlobal.getLocaleText("acc.auditTrail.emp"),  //'Select a transaction',
            store : this.groupStore,
            readOnly : true,
            displayField:'groupname',
            mode: 'local',
            triggerAction: 'all',
            fieldLabel : WtfGlobal.getLocaleText("acc.auditTrail.trans"),  //'Transaction',
            name : 'groupid',
            valueField:'groupid'
        });
        this.reader = new Wtf.data.JsonReader({
            root: 'data',
            fields: [{
                name: 'id'
            }, {
                name: 'name'
            }]
        });
      
        var innerPanel = new Wtf.Panel({
            border : false,
            layout:'fit',
            items:[this.grid],
            tbar: [WtfGlobal.getLocaleText("acc.auditTrail.trans"),  //' Transaction: ',
                    this.groupCombo,'-',
                    this.fT,
                    WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate,'-',
                    this.searchBttn,
                    this.resetBttn,"->",
                    getHelpButton(this,this.helpmodeid)
            ],
            bbar: new Wtf.PagingSearchToolbar({
                id: 'pgTbar' + this.id,
                searchField: this.fT,
                pageSize: 30,
                store: this.auditStore,
                displayInfo: true,
//                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                plugins: this.pP =new Wtf.common.pPageSize({})
            })
        });
        this.auditStore.on('beforeload', this.addDates, this);
        this.auditStore.on('load', function() {
            if(this.auditStore.getCount()<1) {
                this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.grid.getView().refresh();
            }
             WtfGlobal.resetAjaxTimeOut();
//            if(this.fT.getValue() == "")
//            	this.groupCombo.getEl().dom.value = WtfGlobal.getLocaleText("acc.auditTrail.emp");  //"Select a transaction";
        }, this);
        this.auditStore.on('datachanged', function() {
            var p = this.pP.combo.value;
        }, this);
        this.add(innerPanel);
        this.auditStore.baseParams = {
            mode:201,
            groupid:this.groupCombo.getValue(),
            search:this.fT.getValue()
        };
        this.auditStore.load({
            params: {
                start:0,
                limit:30
            }
        });
        this.groupStore.load({
            params: {
                mode:202
            }
        });
    },
    addDates:function(s,opt){
//        if(!opt.params){
//            opt.params={};
//        }
//        if(this.startDate.getValue() && this.endDate.getValue() && this.startDate.getValue() > this.endDate.getValue()) {
//            opt.params.startdate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
//            var ed=this.startDate.getValue();
//        } else {
//            opt.params.startdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
//            ed=this.endDate.getValue();
//        }
//        if(ed) ed = ed.add(Date.DAY, 1);
//        opt.params.enddate=WtfGlobal.convertToGenericDate(ed);

        if(!opt.params){
            opt.params={};
        }
        opt.params.startdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var ed=this.endDate.getValue();
        if(ed) ed = ed.add(Date.DAY, 1);
        opt.params.enddate=WtfGlobal.convertToGenericDate(ed);
         WtfGlobal.setAjaxTimeOut();
        this.auditStore.baseParams.groupid=this.groupCombo.getValue();
        this.auditStore.baseParams.search=this.fT.getValue();
    },
    handleResetClick:function(){
        this.groupCombo.reset();
        this.fT.reset();
        this.startDate.setValue(Wtf.account.companyAccountPref.fyfrom);
        this.endDate.setValue(Wtf.serverDate);
        this.auditStore.baseParams = {
            mode:201,
            groupid:'',
            search:''
        };
        this.auditStore.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
    },
    searchHandler: function() {
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
        if(this.sDate!="" && this.eDate!="" && this.sDate>this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        this.auditStore.removeAll();
        this.auditStore.baseParams = {
            mode:201,
            groupid:this.groupCombo.getValue(),
            search:this.fT.getValue()
        };
        this.auditStore.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
    }

});

Wtf.common.WtfGIROFileGenerationLog = function(config){
    Wtf.common.WtfGIROFileGenerationLog.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.common.WtfGIROFileGenerationLog,Wtf.Panel,{
    onRender : function(config){
        Wtf.common.WtfGIROFileGenerationLog.superclass.onRender.call(this,config);
        
        this.groupingView1 = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: true,
            hideGroupedColumn: true
        });
    
        this.auditRecord = Wtf.data.Record.create([
        { 
            name: 'filename',
            type: 'string'
        },{
            name: 'bank',
            type: 'string'
        },{
            name: 'status',
            type: 'string'
        },{
            name: 'timestamp'
        },{
            name: 'comments',
            type: 'string'
        }
        ]);
    
        this.auditReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, this.auditRecord);
    
        this.auditStore = new Wtf.data.Store({
                proxy: new Wtf.data.HttpProxy({
                url: "GIROFile/getGIROFileGenerationLog.do"
            }),
            reader: this.auditReader
        });
    
        this.cmodel = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),
        {
            header: WtfGlobal.getLocaleText("acc.importLog.fileName"),  //"File Name",
            tip:WtfGlobal.getLocaleText("acc.importLog.fileName"),  //'File Name',
            width: 150,
            dataIndex: 'filename'
        }, {
            header: WtfGlobal.getLocaleText("acc.setupWizard.BankNam"),  //"Bank",
            tip:WtfGlobal.getLocaleText("acc.setupWizard.BankNam"),  //'Bank Name',
            width: 120,
            dataIndex: 'bank'
        }, {
            header: WtfGlobal.getLocaleText("acc.cc.8"),  //"Status",
            tip:WtfGlobal.getLocaleText("acc.cc.8"),
            align:'right',
            width: 100,
            dataIndex: 'status'
        }, {
            header: WtfGlobal.getLocaleText("acc.auditTrail.gridTimestamp"),  //"Timestamp",
            tip:WtfGlobal.getLocaleText("acc.auditTrail.gridTimestamp"),  //'Time',
            width: 160,
            align:'center',
            dataIndex: 'timestamp',
            renderer:WtfGlobal.dateTimeRenderer
        }, {
            header: WtfGlobal.getLocaleText("acc.het.211"),  //"Comments",
            tip:WtfGlobal.getLocaleText("acc.het.211"),  //'Comments',
            width: 160,
            align:'center',
            dataIndex: 'comments'
        }]);
            
        this.grid=new Wtf.grid.GridPanel({
            ds: this.auditStore,
            cm: this.cmodel,
            border: false,
            viewConfig:{forceFit:true,emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))},
            view: new Wtf.ux.KWLGridView({forceFit:true}),
            trackMouseOver: true,
            loadMask: {
                msg:WtfGlobal.getLocaleText("acc.msgbox.50")
            }
        });
    
        this.cmodel.defaultSortable = true;
    
        this.comboReader = new Wtf.data.Record.create([
        {
            name: 'id',
            type: 'string'
        },{
            name: 'name',
            type: 'string'
        }
        ]);

        this.groupRecord = Wtf.data.Record.create([
        {
            name: 'groupname',
            type: 'string'
        },{
            name: 'groupid',
            type: 'string'
        }
        ]);

        this.resetBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
                tooltip :WtfGlobal.getLocaleText("acc.auditTrail.resetTip"),  //'Reset Search Result.',
                id: 'btnRec' + this.id,
                scope: this,
                iconCls :getButtonIconCls(Wtf.etype.resetbutton),
                disabled :false
        });
        this.resetBttn.on('click',this.handleResetClick,this);

        this.startDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:Wtf.account.companyAccountPref.fyfrom
        });
        this.endDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            value: Wtf.serverDate	
        });

        this.searchBttn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
            scope: this,
            id:"search"+this.helpmodeid,
            handler: this.searchHandler,
            iconCls:'accountingbase fetch'
        });
        this.fT = new Wtf.form.TextField({
            fieldLabel : WtfGlobal.getLocaleText("acc.auditTrail.con"),  //"Contains",
            emptyText :WtfGlobal.getLocaleText("acc.field.SearchbyFileName"),  //"Search by Details/User Name",  // -- Search Text --",
            width : 200
        });
                            
        
        this.reader = new Wtf.data.JsonReader({
            root: 'data',
            fields: [{
                name: 'id'
            }, {
                name: 'name'
            }]
        });
      
        var innerPanel = new Wtf.Panel({
            border : false,
            layout:'fit',
            items:[this.grid],
            tbar: [this.fT,
                    WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate,'-',
                    this.searchBttn,
                    this.resetBttn,"->",
                    getHelpButton(this,this.helpmodeid)
            ],
            bbar: new Wtf.PagingSearchToolbar({
                id: 'pgTbar' + this.id,
                searchField: this.fT,
                pageSize: 30,
                store: this.auditStore,
                displayInfo: true,
//                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                plugins: this.pP =new Wtf.common.pPageSize({})
            })
        });
        this.auditStore.on('beforeload', this.addDates, this);
        this.auditStore.on('load', function() {
            if(this.auditStore.getCount()<1) {
                this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.grid.getView().refresh();
            }
            
        }, this);
        this.auditStore.on('datachanged', function() {
            var p = this.pP.combo.value;
        }, this);
        this.add(innerPanel);
        this.auditStore.baseParams = {
            mode:201,
            search:this.fT.getValue()
        };
//        this.auditStore.load({
//            params: {
//                start:0,
//                limit:30
//            }
//        });
//        this.groupStore.load({
//            params: {
//                mode:202
//            }
//        });
    },
    addDates:function(s,opt){
//        if(!opt.params){
//            opt.params={};
//        }
//        if(this.startDate.getValue() && this.endDate.getValue() && this.startDate.getValue() > this.endDate.getValue()) {
//            opt.params.startdate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
//            var ed=this.startDate.getValue();
//        } else {
//            opt.params.startdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
//            ed=this.endDate.getValue();
//        }
//        if(ed) ed = ed.add(Date.DAY, 1);
//        opt.params.enddate=WtfGlobal.convertToGenericDate(ed);

        if(!opt.params){
            opt.params={};
        }
        opt.params.startdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var ed=this.endDate.getValue();
        if(ed) ed = ed.add(Date.DAY, 1);
        opt.params.enddate=WtfGlobal.convertToGenericDate(ed);

        this.auditStore.baseParams.search=this.fT.getValue();
    },
    handleResetClick:function(){
        this.fT.reset();
        this.startDate.setValue(Wtf.account.companyAccountPref.fyfrom);
        this.endDate.setValue(Wtf.serverDate);
        this.auditStore.baseParams = {
            mode:201,
            groupid:'',
            search:''
        };
        this.auditStore.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
    },
    searchHandler: function() {
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
        if(this.sDate!="" && this.eDate!="" && this.sDate>this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        this.auditStore.removeAll();
        this.auditStore.baseParams = {
            mode:201,
            search:this.fT.getValue()
        };
        this.auditStore.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
    }

});

