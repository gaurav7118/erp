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
Wtf.account.GroupReport = function(config){
    this.createStore();
    this.groupID="";
    this.isAdd=false;
    this.recArr=[];
    this.isEdit=false;
    this.ispropagatetochildcompanyflag=false;
    this.createColumnModel();
    this.createGrid();
    this.store.on('load',this.hideMsg,this)
    Wtf.account.GroupReport.superclass.constructor.call(this, config);
    this.ExportButtonVersion='_v1';
    if (dojoInitCount <= 0) {
        dojo.cometd.init("../../bind");
        dojoInitCount++;
    }
}

Wtf.extend(Wtf.account.GroupReport, Wtf.Panel,{
    onRender: function(config){
        dojo.cometd.subscribe(Wtf.ChannelName.AccountGroupReport, this, "accountGroupGridAutoRefreshPublishHandler");//this channel get publish when Account grop get imported succesfully.
        Wtf.account.GroupReport.superclass.onRender.call(this, config);
        this.add(this.grid);
  },
    hideMsg:function(){
        Wtf.MessageBox.hide();
    },
    createStore:function(){
        this.coaRec = new Wtf.data.Record.create([
            {name: 'groupid'},
            {name: 'groupname'},
            {name: 'nature',type:'int'},
            {name: 'affectgp', type:'boolean'},
            {name: 'isCostOfGoodsSoldGroup', type: 'boolean'},
            {name: 'displayorder'},
            {name: 'level',type:'int'},
            {name: 'leaf',type:'boolean'},
            {name: 'parentid'},
            {name: 'parentname'},
            {name: 'deleted'},
            {name: 'companyid'}
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.coaRec),
            url:"ACCAccount/getGroups.do",
//            url: Wtf.req.account +'CompanyManager.jsp',
            baseParams:{
                mode:1
            },
            remoteSort:true
        });
        this.store.load();
        WtfComMsgBox(29,4,true);
    },

    createColumnModel:function(){
//        this.selectionModel = new Wtf.grid.CheckboxSelectionModel({singleSelect:true});
        this.selectionModel = new Wtf.grid.RadioSelectionModel();
        this.gridcm= new Wtf.grid.ColumnModel([this.selectionModel,{
            header: WtfGlobal.getLocaleText("acc.coa.gridAccountType"),  //"Account Type",
            dataIndex: 'groupname',
            sortable: true,
            renderer:this.formatPredefinedGroup,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.coa.gridNature"),  //"Nature",
            dataIndex: 'nature',
            renderer:this.natureRenderer,
            pdfwidth:200
        },{
            header :WtfGlobal.getLocaleText("acc.coa.gridAffectsGrossProfit"),  //'Affects Gross Profit',
            dataIndex: 'affectgp',
            sortable: true,
            pdfwidth:200,
            renderer:this.boolRendererWithDeleted,
            hidden:true,
            hideable:false
        },{
            header: WtfGlobal.getLocaleText("acc.rem.20"),  //"Parent Name",
            dataIndex: 'parentname',
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.account.group.soldgroup"),  //"is Cost Of Goods Sold Group",
            dataIndex: 'isCostOfGoodsSoldGroup',
            pdfwidth:200
        }]);
    },

    formatPredefinedGroup:function(val, m, r){
        if(!r.data['companyid'])
            val= '<b>'+val+'</b>';
        if(r.data.deleted)
                val='<del>'+val+'</del>';
        return val;
    },
    boolRendererWithDeleted: function(val,m,rec) {
        if(val){
            if(rec.data.deleted){
                return '<del>'+"Yes"+'</del>';
            }else{
                return "Yes";
            }
        }else{
            if(rec.data.deleted){
                return '<del>'+"No"+'</del>';
            }else{
                return "No";
            }
        } 
    },
    createGrid:function(){
        this.localSearch = new Wtf.KWLLocalSearch({
            emptyText:WtfGlobal.getLocaleText("acc.coa.groupTypeSearchText"),  //'Search by Account Type',
            width: 150,
            searchField: "groupname"
        });
        this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            hidden:this.isSummary,
            tooltip :WtfGlobal.getLocaleText("acc.coa.resetTT"),  //'Allows you to add a new search group by clearing existing search groups.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        });
        var btnArr=[];
        btnArr.push(this.localSearch);
        btnArr.push(this.resetBttn);
        this.resetBttn.on('click',this.handleResetClick,this);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.groups, Wtf.Perm.groups.create)){
        btnArr.push(new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.coa.addNewAccountType"),  //'Add New Account type',
                id:'maintainAccountType5', //FixMe: remove hardcoded helpmodeid
                tooltip:WtfGlobal.getLocaleText("acc.coa.addNewAccountType"),  //"Add new account type.",
                iconCls:getButtonIconCls(Wtf.etype.add),
                handler:this.editGroup.createDelegate(this,[false])
            })
        );
        }
            this.editType=new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.coa.editAccountType"),  //'Edit Account type',
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.coa.editAccountType"),  //{text:"Select an account type to edit.",dtext:"Select an account type to edit.", etext:"Edit selected account type."},
                iconCls:getButtonIconCls(Wtf.etype.edit),
                disabled:true,
                handler:this.editGroup.createDelegate(this,[true])
            })
        this.deleteMenu = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"), 
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"), 
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            hidden: this.reportbtnshwFlag, 
            menu:[this.deleteType= new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.coa.deleteAccountType"),  //'Delete Account type',
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.coa.deleteAccountType"),  //{text:"Select an account type to delete.",dtext:"Select an account type to delete.", etext:"Delete selected account type."},
                disabled:true,
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                handler:this.confirmBeforeDeletegroup.createDelegate(this,this.isPermDel=["false"])
            }),this.accountGrpDelPermBttn = new Wtf.Action({
                text:WtfGlobal.getLocaleText("acc.coa.deleteAccountTypePermanently"),
                disabled:true,
                tooltip:WtfGlobal.getLocaleText("acc.coa.deleteAccountTypePermanently"),
                scope:this,
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                handler:this.confirmBeforeDeletegroup.createDelegate(this,this.isPermDel=["true"])
        })]
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.edit))
            btnArr.push(this.editType);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.remove))
            btnArr.push(this.deleteMenu);
        
//        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdata)){
            this.exportDepData=new Wtf.exportButton({
                    obj:this,
                    text:WtfGlobal.getLocaleText("acc.common.export"),
                    tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),
                    scope: this,
                    iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
                    get:Wtf.autoNum.GroupExport,
                    menuItem:{csv:true,xls:true},
                    filename:"Account_Groups_v1"//ERP-23845
                });
            btnArr.push(this.exportDepData);
//        }
  
        var extraConfig = {};
        extraConfig.url= "ACCAccount/importGroups.do";
        var extraParams = "{\"Company\":\""+companyid+"\",\"GrpOldId\":\"0\"}";
        this.importBtnArray= Wtf.importMenuArray(this, "Group", this.store, extraParams, extraConfig);
        this.importButton= Wtf.importMenuButtonA(this.importBtnArray, this, "Group");
        btnArr.push(this.importButton);
        
       btnArr.push("->");
       btnArr.push(getHelpButton(this,5));
       this.grid = new Wtf.grid.HirarchicalGridPanel({
            layout:'fit',
            store: this.store,
            cm: this.gridcm,
            sm : this.selectionModel,
            hirarchyColNumber:1,
            autoScroll:true,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });        
        this.grid.on("render", function(grid) {
            WtfGlobal.autoApplyHeaderQtip(grid);
            this.localSearch.applyGrid(grid);
        },this);
        
//        btnArr.push(new Wtf.exportButton({
//             obj:this,
//             menuItem:{csv:true,pdf:true,rowPdf:false},
//             get:112
//        }));
        this.tbar=btnArr;
        this.selectionModel.on("selectionchange",this.enableDisableButtons.createDelegate(this,[btnArr]),this);
    },
    exportGroups:function(){
        var module='Group';
        var mode=Wtf.autoNum.GroupExport;
        var type="csv";
        var filename="Groups";
        WtfGlobal.exportAllData(mode,filename,type,module);
    },
    
    enableDisableButtons:function(btnArr){
        var rec=this.grid.getSelectionModel().getSelected();
        if(rec&&!rec.data['companyid']){
            WtfGlobal.enableDisableBtnArr(btnArr, this.grid, [], []);            
        }else{
            WtfGlobal.enableDisableBtnArr(btnArr, this.grid, [3,4], []);
        }
        var selection=this.grid.getSelectionModel().getCount();
        if(selection>0){
            this.deleteType.setDisabled(false);
            this.accountGrpDelPermBttn.setDisabled(false); 
        }else{
            this.deleteType.setDisabled(true);
            this.accountGrpDelPermBttn.setDisabled(true); 
        }
    },
        handleResetClick:function(){
        if(this.localSearch.getValue()){
            this.localSearch.reset();
            this.store.load();
        }
    },
    editGroup:function(isEdit){
        this.recArr =[] ;
        this.isEdit=isEdit;
        if(isEdit){
            this.recArr = this.grid.getSelectionModel().getSelections();
            this.grid.getSelectionModel().clearSelections();
            WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,1);
        }
        var  rec=isEdit?this.recArr[0]:null;
        if(rec&&!rec.data['companyid']){
            WtfComMsgBox(31, 2);
            var num= (this.store.indexOf(this.recArr[0]))%2;WtfGlobal.highLightRowColor(this.grid,this.recArr,false,num,true);
            return;
        }
        callGroupWindow(isEdit,rec,"groupWindow");
        Wtf.getCmp("groupWindow").on('update',this.updateGrid,this);
        Wtf.getCmp("groupWindow").on('cancel',function(){ 
            var num= (this.store.indexOf(this.recArr[0]))%2;WtfGlobal.highLightRowColor(this.grid,this.recArr,false,num,true);
        },this);
    },
    accountGroupGridAutoRefreshPublishHandler:function(){
        this.store.reload();
    },
    updateGrid: function(obj,groupID){
        this.groupID=groupID;
        this.store.reload();
        this.isAdd=true;
        this.store.on('load',this.colorRow,this)
    },
    colorRow: function(store){
       if(this.store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
       }
       if(this.isAdd && (!this.isEdit)){
          this.recArr=[];
          this.recArr.push(store.getAt(store.find('groupid',this.groupID)));
          WtfGlobal.highLightRowColor(this.grid,this.recArr[0],true,0,0);
          this.isAdd=false;
       }
    },
     confirmBeforeDeletegroup: function (isPermDel) {
        if (Wtf.account.companyAccountPref.propagateToChildCompanies) {
            Wtf.MessageBox.show({ 
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText({key: "acc.customervendormaster.propagated.delete.confirm", params: [" Account Group "]}),
                buttons: Wtf.MessageBox.YESNO,
                icon: Wtf.MessageBox.QUESTION,
                width: 300,
                scope: {
                    scopeObject: this
                },
                fn: function (btn) {
                    if (btn == "yes") {
                        this.scopeObject.ispropagatetochildcompanyflag = true;
                    }
                    this.scopeObject.deleteGroup(isPermDel);
                }
            }, this);
        } else {
            this.deleteGroup(isPermDel);
        }
    },
   deleteGroup:function(isPermDel){
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.show({
        title: WtfGlobal.getLocaleText("acc.common.warning"),  //"Warning",
        msg: isPermDel?WtfGlobal.getLocaleText("acc.coa.deleteAccountType.confirmMessage"):WtfGlobal.getLocaleText("acc.rem.12"),  //"Are you sure you want to delete the selected group and all associated sub group(s)?<div><b>Note: This data cannot be retrieved later.</b></div>",
        width: 560,
        buttons: Wtf.MessageBox.OKCANCEL,
        animEl: 'upbtn',
        icon: Wtf.MessageBox.QUESTION,
        scope:this,
        fn:function(btn){
            if(btn!="ok"){
               var num= (this.store.indexOf(this.recArr[0]))%2;
               WtfGlobal.highLightRowColor(this.grid,this.recArr,false,num,2);
                 return;
            }
            Wtf.Ajax.requestEx({
                url:"ACCAccount/deleteGroup.do",
//                url: Wtf.req.account+'CompanyManager.jsp',
                params:{
                    groupid:this.recArr[0].data['groupid'],
                    acccode:this.recArr[0].data['groupname'],
                    mode:9,
                    isPermDel:isPermDel,
                    ispropagatetochildcompanyflag:this.ispropagatetochildcompanyflag
                    
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
             this.ispropagatetochildcompanyflag=false; //after deleting reocrd setting this flag to 'false'.
            }
        });
    }, 

    genSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.coa.tabTitle"),response.msg],response.deleted*2+1);
        if(response.success){
            //WtfGlobal.highLightRowColor(this.grid,this.recArr,false,0);
            WtfGlobal.highLightRowColor(this.grid,this.recArr[0],true,0,3);
            (function(){
            this.store.reload();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    natureRenderer:function(val,m,rec){
        switch(val){
            case Wtf.account.nature.Asset:
                var v="Asset";
                if(rec.data.deleted)
                    v='<del>'+v+'</del>';
                return v;
            case Wtf.account.nature.Liability:
                var v1="Liability";
                if(rec.data.deleted)
                    v1='<del>'+v1+'</del>';
                return v1;
            case Wtf.account.nature.Expences:
                var v2="Expenses";
                if(rec.data.deleted)
                    v2='<del>'+v2+'</del>';
                return v2;
            case Wtf.account.nature.Income:
                 var v3="Income";
                if(rec.data.deleted)
                    v3='<del>'+v3+'</del>';
                return v3;
        }
    }
});
