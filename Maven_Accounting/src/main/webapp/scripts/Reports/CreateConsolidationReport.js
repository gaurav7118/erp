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
function callCreateConsolidationReportTab(){
    var p = Wtf.getCmp("createconsolidationReportTab");
    if(!p){
        p= new Wtf.account.createconsolidationtab({
            id:'createconsolidationReportTab',
            layout:'fit',
            border: false,
            tabTip : WtfGlobal.getLocaleText("acc.conslodation.createConsolidation"),
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.conslodation.createConsolidation"),Wtf.TAB_TITLE_LENGTH),
            closable:true
        })
        Wtf.getCmp('as').add(p);
    }
    Wtf.getCmp('as').setActiveTab(p);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.createconsolidationtab = function(config) {
    Wtf.apply(this, config);
    this.recurringruleid="";
    
    this.createButton();
    this.createStores();
    this.loadStores();
    this.createComponet();
    this.createGrid();

    Wtf.account.createconsolidationtab.superclass.constructor.call(this,{
        autoDestroy:true,
        border: false,
        layout :'border',
        items:[{
            title : '',
            paging : false,
            autoLoad : false,
            region:"north",
            height:200,
            bodyStyle : "background:#f0f0f0;",
            border: false,
            layout:"fit",
            items: [{
                border:false,
                layout:'form',
                bodyStyle:'padding:13px 13px 13px 13px',
                labelWidth:140,
                items: [this.subdomainName,this.stakeinpercentage],
                bbar:[this.submitBttn]
            }]
        },{
            title : '',
            paging : false,
            autoLoad : false,
            region:'center',
            layout:'fit',
            items:this.grid,
            bbar:this.pg = new Wtf.PagingSearchToolbar({
                id: 'pgTbarModule' + this.id,
                pageSize: 15,
                store: this.gridStore,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                plugins: this.pP3 = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                })
            })
        }]
    });     
 

    this.gridStore.on("datachanged",function() {
        var p = this.pP3.combo.value;
    }, this);

    this.gridStore.on('beforeload', function(s, o) {
        if (this.pP3 != undefined && this.pP3.combo!=undefined) {
            if (this.pP3.combo.value == "All") {
                var count = this.gridStore.getTotalCount();
                var rem = count % 5;
                if (rem == 0) {
                    count = count;
                } else {
                    count = count + (5 - rem);
                }
                o.params.limit = count;
            }
        }
    }, this);

    this.gridStore.on("load",function(store){
        var p = this.pP3.combo.value;
    },this);    

    this.on('render',this.handleRender,this);
    this.submitBttn.on('click',this.savedata,this);
}

Wtf.extend(Wtf.account.createconsolidationtab, Wtf.Panel,{
    createButton:function(){
        this.submitBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.submit"),
            iconCls :getButtonIconCls(Wtf.etype.save),
            tooltip :WtfGlobal.getLocaleText("acc.conslodation.saveyouchanges"),
            id: 'BtnSubNew' + this.id,
            scope: this
        });
    },
    createStores:function(){
        this.childSubdomainRecord = new Wtf.data.Record.create([
            {name: 'companyid'},
            {name: 'companyname'},
            {name: 'subdomain'}
        ]);
        
        this.childSubdomainReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, this.childSubdomainRecord);
        
        this.childSubdomainStore = new Wtf.data.Store({
            reader: this.childSubdomainReader,
            url:"ACCReports/getMappedCompanies.do",
            baseParams:{
                includeParentCompany:true
            }
        });
    },
    loadStores:function(){
        this.childSubdomainStore.load();
    },
    createComponet:function(){
        this.subdomainName= new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.conslodation.companyName")+ '*',
            labelStyle:'width:120px;margin-left: 5px;',
            name:'subdomainname',
            hiddenName:'subdomainname',
            width:250,                         
            store:this.childSubdomainStore,
            valueField:'companyid',
            displayField:'companyname',
            extraFields:['subdomain'],
            mode: 'local',
            disableKeyFilter:true,
            allowBlank:false,
            triggerAction:'all',
            forceSelection:true,
            typeAhead: true,
            emptyText:WtfGlobal.getLocaleText("acc.conslodation.selectacompany")
        });
        
        this.stakeinpercentage = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.conslodation.stakeinpercentage") + '*', 
            name:'stakeinpercentage',                
            width:250,                         
            allowNegative:false,
            labelStyle:'width:120px;margin-left: 5px;',
            allowBlank:false,
            minValue : 0,
            maxValue : 100
        });
    },
    createGrid:function(){
        this.rowNo=new Wtf.KWLRowNumberer();
        this.chkselModel = new Wtf.grid.CheckboxSelectionModel({
            singleSelect : false
        });
//        this.sm = new Wtf.grid.CheckboxSelectionModel();
        this.cm = new Wtf.grid.ColumnModel([this.rowNo,
        {
            header: "",
            dataIndex: 'id',
            hidden:true
        },{
            header: WtfGlobal.getLocaleText("acc.conslodation.companyName"),
            anchor:'20%',
            dataIndex: 'companyname'
        },{
            header: WtfGlobal.getLocaleText("acc.conslodation.subdomainname"),
            anchor:'20%',
            dataIndex: 'subdomainname'
        },{
            header: WtfGlobal.getLocaleText("acc.conslodation.stakeinpercentage"),
            anchor:'20%',
            dataIndex: 'stakeinpercentage'
        },{
            header: "",
            anchor:'60%',
            dataIndex:""
        }]);

    
        this.gridRecord = new Wtf.data.Record.create([{
            name: 'id' 
        },{
            name: 'subdomainid'
        },{
            name: 'companyname'
        },{
            name: 'subdomainname'
        },{
            name: 'stakeinpercentage'
        }
        ]);
       
        this.gridReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, this.gridRecord);
   
        this.gridStore = new Wtf.data.Store({
            proxy: new Wtf.data.HttpProxy({
                url:"ACCCurrency/getConsolidation.do" 
            }),
            reader: this.gridReader,
            baseParams: {
                companyid:companyid
            }
        });

        this.grid=new Wtf.grid.GridPanel({
            id:'createconsolidationgrid'+this.id,
            store: this.gridStore,
            cm: this.cm,
            border: false,
            view: new Wtf.grid.GridView({
                forceFit:true
            }),
            sm: this.chkselModel,
            trackMouseOver: true,
            loadMask: {
                msg: WtfGlobal.getLocaleText("acc.msgbox.50")
            }
        });
    },
    
    savedata:function(){
        if(this.subdomainName.isValid() && this.stakeinpercentage.isValid()){//when both fields are valid 
            var record=WtfGlobal.searchRecord(this.gridStore, this.subdomainName.getValue(), "subdomainid")
            var consolidationid="";
            if(record!=null){
                consolidationid=record.data.id;
            }
            Wtf.Ajax.requestEx({
                url:"ACCCurrency/saveConsolidation.do",
                params:{
                   childcompanyid:this.subdomainName.getValue(),
                   stakeinpercentage:this.stakeinpercentage.getValue(),
                   consolidationid:consolidationid// sending reocrd id so that if exist then we can update
                }
            },this, function(response){
                if(response.success == true) {    
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.success"),
                        msg: response.msg,
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.INFO,
                        scope: this,
                        fn: function(btn) {
                            if (btn == "ok") {
                                this.gridStore.reload();
                            }
                        }
                    });
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), response.msg], 1);
                }  
            },function(resp){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), resp.msg], 1);
            });          
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.2")], 2);
            return;  
        }
        this.NewRule();
    },
    handleRender:function(panelObj) {
        this.gridStore.load({
            params:{
                start:0,
                limit:15
            }
        });
    },
        NewRule:function(){
        this.grid.getSelectionModel().clearSelections();
        this.subdomainName.setValue("");
        this.subdomainName.clearInvalid();
        this.stakeinpercentage.setValue("");
        this.stakeinpercentage.clearInvalid("");
    }
});