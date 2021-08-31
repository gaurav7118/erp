Wtf.reportPrivileges = function(config) {
    /*
     variable to categorize between favourite and non favourite reports
     */
    this.isfavourite=false;  
    this.deleted=false;
    this.nondeleted=true;
    this.index = "";
    Wtf.apply(this, config);
    this.msgLmt = 30;
    this.topUsersRec = new Wtf.data.Record.create([
    {
        name:'isfavourite', type:'boolean'
    },{           
        name:'id'
    },{           
        name:'name'
    },{
        name:'description'
    },{
        name:'methodName'
    },{
        name:'groupedunder' 
    },{
        name:'addtowidget' 
    },{
        name:'iswidgetready', type: 'boolean'
    }]);
    this.topUsersStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.topUsersRec),
        //        url: Wtf.req.account+"reporthandler.jsp",
        url : 'ACCReports/getReports.do',
        baseParams:{
          
            roleid:Wtf.UserReporRole.URole.roleid,
            userid:loginid
        }
    });
    /*
     *data to be displayed in combo box to fetch All or Favourite reports
     */
    var dataArr = new Array();
    dataArr.push([0,WtfGlobal.getLocaleText("acc.rem.105")],[1,WtfGlobal.getLocaleText("acc.field.FavouriteRecords")]);
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :dataArr
    });
    /*
     *Combobox having options to fetch all or favourite reports
     */
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.helpmodeid,
        valueField:'typeid',
        mode: 'local',
        value:0,
        width:160,
        listWidth:160,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    /*
     *tbar1 having various controls on it
        
     */
    this.tbar1=new Array();
    
    /*Quick Search*/
    this.tbar1.push(WtfGlobal.getLocaleText("acc.field.QuickSearch"), this.quickPanelSearch1 = new Wtf.KWLTagSearch({
                width: 200,
                field: "name",
            store: this.topUsersStore
            })
    );
    this.tbar1.push(this.setPrivilege = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.field.SetPrivileges"),
                tooltip: WtfGlobal.getLocaleText("acc.field.Setreportprivileges"),
                disabled: false,
            //    iconCls :'pwnd userTabIcon',
                scope: this,
                handler: function() {
                    this.groupRec= new Wtf.data.Record.create([{           
                        name:'roleid'
                    },{           
                        name:'rolename'
                    }]);
                    this.groupStore = new Wtf.data.Store({
                        reader: new Wtf.data.KwlJsonReader({
                            root: "data",
                            totalProperty:"count"
                        },this.groupRec),
                        url : 'PermissionHandler/getRoleList.do'
                    });
                    
                    this.groupStore.on('load',function(){
                        var index =this.groupStore.find('roleid','1');
                        if(index != -1){
                            var storerec=this.groupStore.getAt(index);                        
                            this.groupStore.remove(storerec);
                        }
                    } ,this);
                    
                    this.groupCombo = new Wtf.form.ComboBox({
                        fieldLabel: WtfGlobal.getLocaleText("acc.field.Rolename"),   // change of name from Group to Roll
                        store: this.groupStore,
                        mode: 'remote',
                        triggerAction: 'all',
                        editable: false,
                        emptyText: WtfGlobal.getLocaleText("acc.field.SelectRole..."),
                        allowBlank: false,
                        width: 200,
                        valueField: 'roleid',
                        displayField: 'rolename'
                    });
                    this.groupCombo.on('select', this.onGroupComboSelect, this);
                    this.roleRec= new Wtf.data.Record.create([{           
                        name:'userID'
                    },{           
                        name:'UserName'
                    }]);
                    this.roleStore = new Wtf.data.Store({
                        reader: new Wtf.data.KwlJsonReader({
                            root: "data",
                            totalProperty:"count"
                        },this.roleRec),
                        url : 'ACCReports/getUserForCombo.do',
                        baseParams:{
                            reportid:this.sm.getSelected().data.id
                        }
                    });
                    this.roleComboConfig={
                        fieldLabel: WtfGlobal.getLocaleText("acc.auditTrail.gridUser"),
                        store: this.roleStore,
                        mode: 'local',
                        triggerAction: 'all',
                        editable: false,
                        emptyText: WtfGlobal.getLocaleText("acc.field.SelectUser..."),
                        allowBlank: false,
                        width: 200,
                        valueField: 'userID',
                        displayField: 'UserName'
                    };                            
           this.roleCombo = new Wtf.common.Select(Wtf.applyIf({
                        multiSelect: true
                    },this.roleComboConfig));
            this.roleStore.on("load", function(combo) {      //for selecting all users while setting privelles
                        var record = new this.roleRec({
                        userID: "All",
                        UserName: "All"
                        });
                  this.roleStore.insert(0,record);
                  this.roleCombo.setValue("All");
                      }, this);
                  this.roleStore.load();     
                   this.roleCombo.on('select',function(combo,roleRec,index){
                      if(this.roleCombo.getValue()=='All'){
                           this.roleCombo.clearValue();
                         this.roleCombo.setValue("All");
                      }else if(this.roleCombo.getValue().indexOf('All')>=0){  // case of all after record
                      this.roleCombo.clearValue();
                       this.roleCombo.setValue(roleRec.get('userID'));
                       }
                  },this);
              
                    this.privWin = new Wtf.Window({
                        title: WtfGlobal.getLocaleText("acc.field.Setreportprivileges"),
                        resizable: false,
                        width: 467,
                        height: 250,
                        modal: true,
                        layout: 'border',
                        scope: this,
                        buttons: [{
                            text: WtfGlobal.getLocaleText("acc.msgbox.ok"),
                            scope: this,
                            handler: function(obj) {
                                this.savePrivileges(obj);
                            }
                        },{
                            text: WtfGlobal.getLocaleText("acc.msgbox.cancel"),
                            scope: this,
                            handler: function() {
                                this.privWin.close();
                            }
                        }],
                        items: [{
                            region: 'north',
                            height: 75,
                            border: false,
                            baseCls : 'northWinClass',
                            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.AssignReportPermissions"), WtfGlobal.getLocaleText("acc.field.Assign")+ this.sm.getSelected().data.name+' '+ WtfGlobal.getLocaleText("acc.field.Permissionforuser")+"<b></b>", "../../images/createuser.png")
                        },{
                            region: 'center',
                            layout: 'form',
                            border: false,
                            labelWidth: 70,
                            bodyStyle: 'background:#f1f1f1;padding:15px',
                            items: [
                            this.groupCombo,
                            this.roleCombo
                            ]
                        }]
                    });
                    this.groupStore.load();
                    this.privWin.show();
                }
            }));
    
      
    this.tbar1.push("->");
    /*
     *tbar 1 will contain the combobox having options 
     *to display all or favourite reports
     */
    this.tbar1.push("&nbsp;View", this.typeEditor);
    this.typeEditor.on('select',this.loadTypeStore,this);
    this.topUsersStore.on('beforeload', function(s, o) {
        if (!o.params) {
            o.params = {};
        }
        o.params.typeid = this.typeEditor.getValue();
    }, this);
    this.topUsersStore.on('load', function(store) {
        var record = this.topUsersStore.queryBy(function(record){
            return (record.get('id') == 'Export_GST_Form_5');
        }, this).items[0];
        if( Wtf.account.companyAccountPref.countryid!='203' && record!=undefined){ 
            this.topUsersStore.remove(record);
        }
        this.quickPanelSearch1.StorageChanged(store);
    }, this);
    this.topUsersStore.on('datachanged', function() {
        this.quickPanelSearch1.setPage(this.pP.combo.value);
    }, this);
    
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect: true
    });
     
    this.repCm = new Wtf.grid.ColumnModel([
        this.sm,
         /*
         *sets an image icon according to favourite flag
         */
        {
            header: '',
            dataIndex:'isfavourite',
            width:20,
            renderer : function(val, meta, record, rowIndex){                
                var value = "";
                if(record.data.isfavourite){
                    value = '<img id="starValiFlag" class="favourite" style="margin-left: 3px;cursor:pointer" wtf:qtip="'+WtfGlobal.getLocaleText('acc.invoiceList.favourite')+'" src="../../images/star-valid.png">';
                }else{
                    value = '<img id="starInvalidFlag" class="favourite" style="margin-left: 3px;cursor:pointer" wtf:qtip="'+WtfGlobal.getLocaleText('acc.invoiceList.notfavourite')+'" src="../../images/star-invalid.png">';
                }
                return value;          
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.field.ReportName"),
            width: 70,
            dataIndex: 'name',
            sortable: true
        }, {
            header: WtfGlobal.getLocaleText("acc.product.description"),
            width: 200,
            dataIndex: 'description',
            sortable: true
        },
        {
            header:WtfGlobal.getLocaleText("acc.report.GroupedUnder"),
            width: 200,
            dataIndex: 'groupedunder',
            sortable: true
        },
//        {
//            header:"Add to Widget",
//            dataIndex: 'addtowidget',
//            renderer:Wtf.comboBoxRenderer(cmbAddToWidget),
//            width : 45,
//            editor:cmbAddToWidget
//        },
        this.CheckBoxColumn = new Wtf.grid.CheckColumnCustomized({
            header: WtfGlobal.getLocaleText("acc.reportlist.AddtoWidget"),
            align:'center',
            dataIndex: 'addtowidget',
            width: 40,
            renderer : function(v, p, record){
                if (record.data.iswidgetready) {
                    p.css += ' x-grid3-check-col-td';
                    return '<div class="x-grid3-check-col'+(v?'-on':'')+' x-grid3-cc-'+this.id+'">&#160;</div>';
                } else {
                    return '';
//                    return '<div class="x-grid3-check-col'+(v?'-disabled':'')+' x-grid3-cc-'+this.id+'">&#160;</div>';
                }
                
            }
        }),
        {
            header : WtfGlobal.getLocaleText("acc.wtfTrans.vvi"),
            dataIndex: 'status',
            width:30,
            renderer:function(){
                return "<img id='AcceptImg' class='ViewR'  style='height:18px; width:18px;' src='images/report.gif' title="+WtfGlobal.getLocaleText("acc.field.ViewReport")+"></img>";
            }
        }
        ]);
    this.repCm.defaultSortable = false;

    this.reportGrid = new Wtf.grid.EditorGridPanel({
        plugins:this.CheckBoxColumn,
        store:  this.topUsersStore,
        cm: this.repCm,
        selModel: this.sm,
        viewConfig: {
            forceFit: true
        },
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: this.msgLmt,
            id: "pagingtoolbar" + this.id,
            store: this.topUsersStore,
            searchField: this.quickPanelSearch1,
            displayInfo: true,
//            displayMsg: "Displaying records {0} - {1} of {2}",
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
        })
        })
    });
    this.reportGrid.on('cellclick',this.onCellClick, this);
    this.reportGrid.on('afteredit',this.processRow,this);
    
    this.sm.on('selectionchange', function(sm) {
        if(Wtf.UserReporRole.URole.roleid == 1){
            if(sm.getSelected()) {
                this.privStore.load({
                    params: {
                        reportid: sm.getSelected().data.id
                    }
                });
                this.setPrivilege.enable();
            } else {
                this.privStore.removeAll();
                this.setPrivilege.disable();
            }
        }
    }, this);
    
    this.privRec = new Wtf.data.Record.create([{           
        name:'GroupName'
    },{
        name : 'UseFirst'
    },
    {
        name : 'UseLast'
    },{           
        name:'RoleID'
    },{
        name:'ReportID'
    },{
        name:'userID'
    },{
        name:'ReportName'
    }
    ]);
    this.privStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.privRec),
        url : 'ACCReports/getReportPerm.do',
        baseParams:{
            start:0,
            limit:30
          
        }
    });
    
    this.sm1 = new Wtf.grid.CheckboxSelectionModel({
        singleSelect: true
    });
    this.privCm = new Wtf.grid.ColumnModel([
        this.sm1,
        {
            header: WtfGlobal.getLocaleText("acc.field.GroupName"),
            width: 70,
            dataIndex: 'GroupName'
        }, 
        {
            header: WtfGlobal.getLocaleText("acc.auditTrail.gridUser"),
            width: 110,
            dataIndex: 'UseFirst' 
        },
        //        {
        //            header: "Last Name",
        //            width: 70,
        //            dataIndex: 'UseLast' 
        //        },  
        {
            header: WtfGlobal.getLocaleText("acc.field.ReportName"),
            width: 120,
            dataIndex: 'ReportName'
        }, 
          {
            header: WtfGlobal.getLocaleText("acc.product.gridAction"),
            width: 20,
            dataIndex: 'delete',
            scope: this,
            sortable: false,
            renderer: function(val, mdata, rec, ri, ci) {
                return "<div class='pwnd delete-gridrow'></div>"//"<img id='delete' class='deleteR'  style='height:18px; width:18px;' src='images/cancel_16.png' title='Action '></img>";
            }
        }]);
    this.privCm.defaultSortable = true;

    this.privilegeGrid = new Wtf.grid.GridPanel({
        store: this.privStore,
        cm: this.privCm,
        selModel: this.sm1,
        viewConfig: {
            forceFit: true
        },
        bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 5,
            id: "pagingtoolbar_b" + this.id,
            store: this.privStore,
            //searchField: this.quickPanelSearch1,
            displayInfo: true,
            //displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.1099.noresult"),
            plugins: this.pP1 = new Wtf.common.pPageSize({
                id : "pPageSize_b"
            })
        })
    });
    //    this.privStore.on('load', function(store) {
    //        this.quickPanelSearch1.StorageChanged(store);
    //    }, this);
    //    this.privStore.on('datachanged', function() {
    //        this.quickPanelSearch1.setPage(this.pP1.combo.value);
    //    }, this);
    Wtf.reportPrivileges.superclass.constructor.call(this, {
        border: false,
        layout: 'border',
        items:[{
            id:'panelPer',
            margins: '0 5 5 5',
            region: 'center',
            layout: 'fit',
            title: WtfGlobal.getLocaleText("acc.field.ListofReports"),
            split: true,
            border: false,
            items: this.reportGrid,
            tbar: this.tbar1,
            bbar: []
        },{
            region: 'south',
            layout: 'fit',
            title: WtfGlobal.getLocaleText("acc.field.PrivilegedUsers"),
            id: "centerregion_",
            height:250,
            split: true,
            border: false,
            items: this.privilegeGrid,
            bbar: []
        }]
    });
}

Wtf.extend(Wtf.reportPrivileges, Wtf.Panel, {
      
    /*
     *function to be called on selecting a report by user
     *which is to be made favourite or un favourite
     */
      onCellClick : function(grid, rowIndex, columnIndex,e){
        var event=e;
        if(event.getTarget('img[class="favourite"]')) {    
            var formrec = grid.getStore().getAt(rowIndex);
            var isfavourite = formrec.get('isfavourite');
            if(!formrec.data.deleted){
                    /*
                     *calls addRemoveFavouriteReport function
                     */
                    this.addRemoveFavouriteReport(formrec);
            }
        }
    },
    /*
     *function to call a controller method to make reports 
     *favourite or un favourite
     */
      addRemoveFavouriteReport : function(formrec){
        var url ="ACCReports/addRemoveFavouriteReport.do";
        var isfavourite = formrec.get('isfavourite');
        var favourite = (isfavourite==true?false:true);
        Wtf.Ajax.requestEx({
            url:url,
            params:{
                date: WtfGlobal.convertToGenericDate(formrec.data.date),
                id:formrec.get('id'),
                isfavourite:favourite,
                userid:loginid
            }
        },this,
        function(){
            formrec.set('isfavourite', favourite);
        },function(){
                
            });
    },
    
    /*
     *function to set favourite flag according to user choice 
     *from typeEditior comboBox
     */
    loadTypeStore:function(a,rec){
        this.deleted=false;
        this.nondeleted=false;
        this.index=rec.data.typeid;
        this.isfavourite=false;
        if(this.index==1){
            this.isfavourite=true;
            
        }
        this.topUsersStore.on('load',this.storeloaded,this);
        this.topUsersStore.load({
           params : {
               start : 0,
               limit : this.pP.combo.value,
               ss : this.quickPanelSearch1.getValue(),
               typeid:rec.data.typeid
           }
       });
       WtfComMsgBox(29,4,true); 

    },
     storeloaded:function(store){
        this.hideLoading();
        this.quickPanelSearch1.StorageChanged(store);
    },
      hideLoading: function() {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        },
    
    onRender: function(config) {
        this.reportGrid.on('cellclick',this.onCellClick, this);
        this.privilegeGrid.on("cellclick",this.callDeletePerm, this);
        this.reportGrid.on("cellclick",this.callUserReports, this);
        this.setPrivilege.disable();
        if(Wtf.UserReporRole.URole.roleid != 1)
        {
            Wtf.getCmp("centerregion_").hide();
               
            this.setPrivilege.hide();
        }
        Wtf.reportPrivileges.superclass.onRender.call(this, config);
        this.topUsersStore.load({
            params: {               
                 ss: this.quickPanelSearch1.getValue(),         
                 start: 0,
                 limit: 30
            }
        });
    },
    
    onGroupComboSelect: function() {
        this.roleStore.load({
            params: {
                groupid: this.groupCombo.getValue()
            }
        });
    },
    
    savePrivileges: function(obj) {
        if(this.groupCombo.isValid() && this.roleCombo.isValid()) {
            obj.disable();
            Wtf.Ajax.request({
                method: 'POST',
                url:'ACCReports/AssignUserPerm.do',
                scope: this,
                params: {
                    type: 'savereportrolemap',
                    mode: 'insert',
                    reportid: this.sm.getSelected().data.id,
                    roleid: this.groupCombo.getValue(),
                    groupid: this.groupCombo.getValue(),   
                    userid:this.roleCombo.getValue()
                },
                success: function() {
                    this.privWin.close();
                    this.privStore.reload();
                },
                failure: function () {
                    obj.enable();
                }
            });
        }
    },
    callDeletePerm :function(obj,row,col,e) {
        var event=e;
        var i=0;
        if(e.target.className == 'pwnd delete-gridrow') {
            Wtf.Ajax.request({
                method: 'POST',
                url: 'ACCReports/DeleteUserPerm.do',
                scope: this,
                params: {
                    type: 'savereportrolemap',
                    mode: 'delete',
                    reportid: this.sm1.getSelected().data.ReportID,
                    userid: this.sm1.getSelected().data.userID,
                    roleid: this.sm1.getSelected().data.RoleID
                },
                success: function() {
                    this.privStore.reload();
                }
            });
        }
    },
    callUserReports: function(obj,row,col,e) {
        var event=e;
        var i=0;
        var flag=0;
        if(event.getTarget("img[class='ViewR']")) {
            if(Wtf.UserReporRole.URole.roleid == 1)
            {
                eval(obj.getStore().getAt(row).data.methodName);
            }
            else
            {
                for(i=0;i<Wtf.UserReportPerm.length;i++)
                {
                    if(Wtf.UserReportPerm[i] == obj.getStore().getAt(row).data.id)
                    {
                        eval(obj.getStore().getAt(row).data.methodName);
                        flag=1;
                    }
                }
                if(flag==0)
                {   
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Youdonothavepermissionsforaccessingthisreport")], 2);
                }
            }
        }
    },
    
    processRow:function(obj){        
        if(obj!=null){
            var rec=obj.record;
            if(obj.field=="addtowidget"){
                var url = "";
                if(rec.data.addtowidget==true){
                    url = "ACCCompanyPref/addReportToWidgetView.do";
                }else{
                    url = "ACCCompanyPref/removeReportFromWidgetView.do";
                    
                }
                
                Wtf.Ajax.requestEx({
                    url: url,
                    params: {
                        reportid: obj.record.data.id
                    }
                }, this, function(response) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],response.success*2+1);
                } , function(response){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],response.success*2+1);
                });
            }
            this.fireEvent('datachanged',this);
        }
    }
});

var rportPriv = new Wtf.reportPrivileges({
    id: 'reportPriv',
    layout: 'fit',
    border: false,
    event: Wtf.getCmp("tabreportperm").activesubtab
}); 
Wtf.getCmp("tabreportperm").add(rportPriv);
Wtf.getCmp("tabreportperm").doLayout();

