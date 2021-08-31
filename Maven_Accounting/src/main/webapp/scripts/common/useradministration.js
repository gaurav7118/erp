Wtf.common.adminpageuser = function(config){
    Wtf.common.adminpageuser.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.common.adminpageuser, Wtf.Panel, {
    onRender: function(config){
        Wtf.common.adminpageuser.superclass.onRender.call(this, config);
        this.userds = new Wtf.data.Store({
            url: '../../admin.jsp',
            baseParams: {
                action: 1
            },
            params:{
              start:0,
              limit:20  
            },
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'total',
                root: "data"
            })
        });
        this.sm = new Wtf.grid.RadioSelectionModel({
            singleSelect: true
        });
        this.createUserbut = new Wtf.menu.Item({
            text: WtfGlobal.getLocaleText("acc.field.CreateUser"),
            disabled: this.manageUserPerm,
            allowDomMove: false,
            scope: this,
            handler: this.createHandler,
            tooltip: {
                title: WtfGlobal.getLocaleText("acc.field.CreateUser"),
                text: WtfGlobal.getLocaleText("acc.field.Clicktocreateauser")
            },
            iconCls: 'pwnd setModerator'
        });
        this.editUserbut = new Wtf.menu.Item({
            text: WtfGlobal.getLocaleText("acc.field.Edituserprofile"),
            allowDomMove: false,
            scope: this,
            disabled: true,
            handler: this.editHandler,
            tooltip: {
                title:WtfGlobal.getLocaleText("acc.field.EditUser"),
                text: WtfGlobal.getLocaleText("acc.field.Clicktoeditauser")
            },
            iconCls: 'pwnd editusericon'
        });
        this.deleteUserbut = new Wtf.menu.Item({
            text: WtfGlobal.getLocaleText("acc.field.Deleteuser"),
            allowDomMove: false,
            scope: this,
            disabled: true,
            handler: this.delHandler,
            tooltip: {
                title: WtfGlobal.getLocaleText("acc.field.Deleteuser"),
                text:WtfGlobal.getLocaleText("acc.field.Clicktodeleteauser")
            },
            iconCls: 'pwnd remModerator'
        });

        this.changePasswordBut = new Wtf.menu.Item({
            text : WtfGlobal.getLocaleText("acc.changePass.tabTitle"),
            allowDomMove : false,
            scope : this,
            disabled : true,
            handler : this.handleChangePassword,
            tooltip: {
                title: WtfGlobal.getLocaleText("acc.changePass.tabTitle"),
                text: WtfGlobal.getLocaleText("acc.field.Clicktochangepassword")
            },
            iconCls : "pwnd changepwdicon"
        });

        this.resetPasswordBut=new Wtf.menu.Item({
            text : WtfGlobal.getLocaleText("acc.field.ResetPassword"),
            allowDomMove : false,
            scope : this,
            disabled : true,
            handler : this.handleResetPassword,
            tooltip: {
                title: WtfGlobal.getLocaleText("acc.field.ResetPassword"),
                text:WtfGlobal.getLocaleText("acc.field.Clicktoresetpassword")
            },
            iconCls : "pwnd resetpwdicon"
        });

//        this.changeCard = new Wtf.menu.Item({
//            text: 'Change Credit Card',
//            tooltip: {
//                title: 'Change credit card',
//                text: "Click to change credit card"
//            },
//            disabled:(Wtf.checkGracePeriod == "makepayment")?true:false,
//            scope: this,
//            handler: this.changeCreditCard
//        });
//
//        this.cancelsubscription = new Wtf.menu.Item({
//            text: 'Cancel Current Subscription',
//            tooltip: {
//                title: 'Cancel Current Subscription',
//                text: "Click to cancel your current subscription"
//            },
//            scope: this,
//            handler: this.cancelSubscription
//        });


        this.manageTeamBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.ManageApplicationAccess"),
            scope: this,
            tooltip: {
                title: WtfGlobal.getLocaleText("acc.field.ManageApplicationAccess"),
                text: WtfGlobal.getLocaleText("acc.field.Clicktomanageuseraccessforapplications")
            },
          //  hidden:(Wtf.checkGracePeriod == "makepayment")?true:false,
            menu: []
        });

//        this.viewInvoice = new Wtf.Toolbar.Button({
//            text: 'View Invoices',
//            tooltip: {
//                title: 'View Invoices',
//                text: "Click to view application subscriptions and invoices/payment receipts"
//            },
//            disabled:(Wtf.checkGracePeriod == "makepayment")?true:false,
//            scope: this,
//            handler: this.showSubscriptionPanel
//        });


//        this.manageSubscription = new Wtf.Toolbar.Button({
//            text: 'Manage Subscriptions',
//            scope: this,
//            tooltip: {
//                title: 'Manage Subscriptions',
//                text: "Click to manage subscriptions"
//            },
//            menu: [this.changeCard,
//                   this.cancelsubscription]
//        });

        var _tb = [];
        _tb.push(WtfGlobal.getLocaleText("acc.field.QuickSearch"));
         this.quickSearchTF = new Wtf.KWLTagSearch({
            id: 'user' + this.id,
            width: 200,
            field: "username",
            emptyText: WtfGlobal.getLocaleText("acc.userAdmin.search")
        });
        this.resetButton = new Wtf.Toolbar.Button({
           text:WtfGlobal.getLocaleText("acc.common.reset"),
           tooltip :WtfGlobal.getLocaleText("acc.field.ResetSearchResults"),
           id: 'btnRec' + this.id,
           scope: this,
           disabled :false
        });
        this.resetButton.on('click',this.handleResetClick,this);
        _tb.push(this.quickSearchTF);
        _tb.push("-");
        _tb.push(this.resetButton);
        _tb.push("-");
        _tb.push(new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.UserManagement"),
            tooltip: {
                title: WtfGlobal.getLocaleText("acc.field.UserManagement"),
                text: WtfGlobal.getLocaleText("acc.field.Clicktocreate/manageauser")
            },
            scope: this,
            disabled:(Wtf.checkGracePeriod == "makepayment")?true:false,           
            
            menu:(Wtf.provisionstatus == true)?[
                //this.createUserbut,
                this.editUserbut,
                //this.deleteUserbut,
                //this.changePasswordBut,
                //this.resetPasswordBut
            ]:[
                this.createUserbut,
                this.editUserbut,
                this.deleteUserbut,
                this.changePasswordBut,
                this.resetPasswordBut
            ]
        }));
        _tb.push("-");
        _tb.push(this.manageTeamBttn);
        _tb.push("-");
        if(Wtf.partnerStatus != 1){
            /*_tb.push(new Wtf.Toolbar.Button({
                text: 'Get More Apps',
                tooltip: {
                    title: 'Get More Apps',
                    text: "Click to manage applications"
                },
                disabled:(Wtf.checkGracePeriod == "makepayment")?true:false,
                scope: this,
                handler: this.onGetMoreApps
            }));*/
            //_tb.push("-");
        }
//        _tb.push(this.manageSubscription);
//        _tb.push("-");
//        _tb.push(this.viewInvoice);

        var comboReader = new Wtf.data.Record.create([
            {name: 'id'},{name: 'name',type: 'string'
            }
            ]);
        this.GenderStore = new Wtf.data.Store({
            url:  '../../admin.jsp?action=31',
            reader: new Wtf.data.KwlJsonReader({
                        root: 'data'
                    },comboReader)
        });
        
        this.usergrid = new Wtf.ux.DynamicGridPanel({
            id: 'user-grid',
            ds: this.userds,
            rowNumberer: true,
            radioSelModel: true,
            loadMask: {
                msg: WtfGlobal.getLocaleText("acc.msgbox.50")
            },
            sm: this.sm,
            tbar: _tb,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 20,
                id: "pagingtoolbar" + this.id,
                store: this.userds,
                searchField: this.quickSearchTF,
                displayInfo: true,
                displayMsg: WtfGlobal.getLocaleText("acc.rem.116"),
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                plugins: this.pP = new Wtf.common.pPageSize({
                    id: "pPageSize_" + this.id
                })
            })
        });
        this.sm.addListener('selectionchange', this.rowSelectionHandler, this);
        this.permaflag = false;
        this.userds.on("load", function(){
            mainLoadMask.hide();
            var col = this.userds.reader.jsonData.columns;
			var pMenu = [];
			Wtf.each(this.manageTeamBttn.menu.items.items, function(mi){
				pMenu.push(mi.id.substring(8));
			}, this);
            for (var i = 4; i < col.length; i++) {
                    if (Wtf.getCmp("menu" + col[i].dataIndex)) {
                        Wtf.getCmp("menu" + col[i].dataIndex).destroy();
                    }
                    this.manageTeamBttn.menu.add(new Wtf.menu.Item({
                        text: col[i].header,
                        scope: this,
                        id: "menu" + col[i].dataIndex,
                        icon:'../../images/'+col[i].dataIndex.substr(4)+'.gif',
                        handler: function(btn){
                            this.manageAppTeams(btn.id.substring(8), btn.text);
                        }
                    }));
//                }
				pMenu.remove(col[i].dataIndex.substring(4));
            }            
            if(checkPermalink()>0 && !this.permaflag) {
                this.createHandler();
                this.permaflag = true;
            }
			Wtf.each(pMenu, function(it){
				var o = Wtf.getCmp("menuapp_" + it);
				this.manageTeamBttn.menu.items.items.remove(o);
				o.destroy();
			}, this);
            this.userds.on("datachanged", function(){
                this.quickSearchTF.setPage(this.pP.combo.value);
            }, this);
           
                this.quickSearchTF.StorageChanged(this.userds);
            this.quickSearchTF.on('SearchComplete', function() {
                this.usergrid.getView().refresh();
            }, this);            
        }, this);  
        this.add(this.usergrid);

        this.loadMask1 = new Wtf.LoadMask(this.el.dom, {msg: WtfGlobal.getLocaleText("acc.msgbox.50"), msgCls:"x-mask-loading admin-loadmask"});
        mainLoadMask.show();
    }, 

//    showSubscriptionPanel: function(){
//        var subwindow = new Wtf.subscriptionWindow({
//            height: 300,
//            width: 500
//        });
//        subwindow.show();
//    },

//   changeCreditCard: function(){
//        var _nc = new Wtf.newCreditCard({
//            mode: 2
//        });
//        _nc.show();
//    },

//   cancelSubscription: function(){
//        Wtf.MessageBox.confirm('Confirm', 'Are you sure you want to cancel subscription?<br>This cancellation will be effective from next billing cycle.', function(btn){
//            if (btn == "yes") {
//                Wtf.Ajax.requestEx({
//                    url: '../../jspfiles/TMS/getlist.jsp',
//                    params : {
//                        action:10
//                    }
//                },
//                this,
//                function(response, req){
//                    var obj = eval('(' + response.trim() + ')');
//                    if(obj.success){
//                        msgBoxShow(["Success", obj.msg]);
//                    }
//                    else {
//                        msgBoxShow(["Failure", obj.msg]);
//                    }
//                },
//                function(result, req){
//                    msgBoxShow(["Failure", "Error connecting to server"]);
//                }
//                );
//            }
//        }, this);
//    },
    handleResetClick:function(){
        this.quickSearchTF.reset();        
        this.userds.load({
            params:{
                start:0,
                limit:this.pagingToolbar.pageSize
            }
        });
    }, 
    rowSelectionHandler: function(sm){
        if (this.sm.getSelections().length > 0) {
            this.deleteUserbut.enable();
        }
        else {
            this.deleteUserbut.disable();
        }
        if (this.sm.getSelections().length == 1) {
            this.editUserbut.enable();
            this.changePasswordBut.enable();
            this.resetPasswordBut.enable();
        }
        else {
            this.editUserbut.disable();
            this.changePasswordBut.disable();
            this.resetPasswordBut.disable();
        }
    }, 

    onGetMoreApps: function(){
        this.msgbox = new Wtf.Window({
            id: 'msgbox' + this.id,
            title: WtfGlobal.getLocaleText("acc.field.GetMoreApps"),
            closable: true,
            border: false,
            modal: true,
            iconCls: 'iconwin',
            height: 310,
            width: 350,
            layout: "fit",
            items: [{
                border: false,
                layout: "border",
                items: [{
                    region: 'north',
                    border: false,
                    height: 75,
                    baseCls : 'northWinClass',
                    html: getTopHtml(WtfGlobal.getLocaleText("acc.field.ManageApplications"), WtfGlobal.getLocaleText("acc.field.Selectapplicationsthatyouwantforyourorganization"), '../../images/section.gif')
                }, {
                    region: "center",
                    baseCls : 'centerWinClass',
                    id: "center" + this.id,
                    border: false,
                    height: 80,

                    layout: "column",
                    items: [{
                        id: "appscont",
                        columnWidth: 1,
                        layout: "form",
                        border: false,
                        labelWidth: 200,
                        bodyStyle: 'margin-top:10px;margin-left:15px;',
                        defaults: {
                            width: 50
                        }
                    }]
                }]
            }],
            buttonAlign: 'center',
            buttons: [{
                text: WtfGlobal.getLocaleText("acc.msgbox.ok"),
                scope: this,
                handler: function(){
                    mainPanel.loadMask.msg = WtfGlobal.getLocaleText("acc.field.onemomentplease...updatingyourapplicationaccess...");
                    mainLoadMask.show();
                    var appids = "";
                    var delids = "";
                    var obj = Wtf.getCmp("appscont").items;
                    for (var ctr = 0; ctr < obj.items.length; ctr++) {
                        if (obj.items[ctr].getValue() == true) {
                            appids += obj.items[ctr].id + ",";
                        } else {
                            delids += obj.items[ctr].id + ",";
                        }

                    }
                    if(appids.length>0)
                        appids = appids.substring(0, appids.length - 1);

                    if(delids.length>0)
                        delids = delids.substring(0, delids.length - 1);

                    //alert(appids);
                    Wtf.Ajax.requestEx({
                        url: '../../admin.jsp',
                        method: 'POST',
                        params: {
                            action: 3,
                            appids: appids,
                            delids : delids
                        }
                    }, this, function(response, e){
                        mainLoadMask.hide();
                        msgBoxShow([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.field.Application'saccessupdatedsuccessfully.")], 0);
                        Wtf.getCmp('msgbox' + this.id).close();
                        this.userds.load();
			Wtf.bRefreshDashboard = true;
                    }, function(resp, req){
                        mainLoadMask.hide();
                    })

                }
            }, {
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                handler: function(){
                    Wtf.getCmp('msgbox' + this.id).close();
                },
                scope: this
            }]
        }).show();
        Wtf.Ajax.requestEx({
            url: '../../admin.jsp',
            method: 'POST',
            params: {
                action: 2
            }
        }, this, function(response, e){
            var obj = Wtf.getCmp("appscont");
            var respobj = eval('(' + response + ')');
            for (var ctr = 0; ctr < respobj.data.length; ctr++) {
                this.check1 = new Wtf.form.Checkbox({
                    fieldLabel: respobj.data[ctr].appname,
                    name: "chck" + this.id,
                    id: respobj.data[ctr].appid,
                    checked: respobj.data[ctr].access
                })
                obj.add(this.check1);
                obj.doLayout();
            }
        }, function(resp, req){
        })
    },

    appGrid: function(){
        this.companyApps = [];
        this.app_Users = {};
        this.GenderStore.load();
        Wtf.Ajax.requestEx({
            url: "../../admin.jsp",
            params: {
                action: 6
            },
            method: 'POST'
        }, this, function(result, req){
            result = eval('(' + result + ')');
            if (result.success) {
                compApp = result.companyapps;
                var plandetail = result.planDetails;
                var items = [];
                for (var cnt = 0; cnt < compApp.length; cnt++) {
                    var comboField = {};
                    comboField['fieldLabel'] = compApp[cnt].appname;
                    comboField['id'] = 'approle' + compApp[cnt].appid;
                    this.companyApps[cnt] = compApp[cnt].appid;
                  //  this.app_Users[compApp[cnt].appid] = compApp[cnt].maxusers+"_"+compApp[cnt].appusers;

                    var obj = new Wtf.data.JsonStore({
                        fields: ["id", "rolename"],
                        data: compApp[cnt].roledata
                    });

                    comboField['store'] = obj;
                    comboField['displayField'] = 'rolename';
                    comboField['valueField'] = 'id';
                    comboField['triggerAction'] = 'all';
                    comboField['editable'] = false;
                    comboField['mode'] = 'local';
                    comboField['value'] = '-1';
                    if(this.userinfo.mode == 1 && this.sm.hasSelection() && this.sm.getSelected().data.iscreator == "1") {
                        comboField['disabled'] = true;
                    }
                    comboField['hiddenName'] = compApp[cnt].appid;
                    var comboObj = new Wtf.form.ComboBox(comboField);
//                    comboObj.on("select",function(obj,rec,index) {
//                         var usersCnt = this.app_Users[obj.hiddenName].split("_");
//                         var max = parseInt(usersCnt[0]);
//                         var appusers = parseInt(usersCnt[1]);
//                         if(max==appusers) {
//                            msgBoxShow(["Error", "Cross max users limit"], 1);
//                            obj.setValue("-1");
//                         }
//                    },this);
                    items.push(comboObj);
                }
                this.appFieldSet = new Wtf.form.FieldSet({
                    id: "appaccessDiv",
                    autoHeight: true,
                    autoWidth: true,
                    width: 400,                    
                    title: WtfGlobal.getLocaleText("acc.field.ApplicationAccess"),
                    items: items
                });
                if (this.userinfo.mode == 1) {
                    var buf = this.sm.getSelected().data;
                    this.userinfo = {
                        'mode': 1,
                        'userid': buf["userid"],
                        'username': buf["username"],
                        'fname': buf["fname"],
                        'lname': buf["lname"],
                        'emailid': buf["emailid"],
                        'address': buf["address"],
                        'contactno': buf["contactno"],
                        'gender':buf["gender"]
                    };
                    var userapps = [];
                    for (var cnt = 0; cnt < this.companyApps.length; cnt++) {
                        var obj = {};
                        obj['appid'] = this.companyApps[cnt];
                        obj['roleid'] = buf["appid_" + this.companyApps[cnt]];
                        userapps[cnt] = obj;
                    }
                    this.userinfo['userapps'] = userapps;
                } else {
                    this.planinfo= {
                        'isuserplan':  plandetail.isuserplan,
                        'planprice': plandetail.usercost,
                        'planid': plandetail.planid,
                        'planname':plandetail.planname,
                        'trialperiod':plandetail.trialperiod, // to check if the trial period is over
                        'subnum':plandetail.subnum,
                        'isPayment':plandetail.isPayment
                    };
                }              
                this.CreateUserWindow(this.userinfo,this.planinfo);             
            }
        }, function(){
        }); 
    },

    createHandler: function(){
        this.userinfo = {
            'mode': 0
        };
        this.appGrid();
    },

    editHandler: function(){
        if (this.sm.hasSelection()) {
            this.userinfo = {
                'mode': 1
            };
            this.appGrid();
        }
    },

    delHandler: function(){
        if (this.sm.hasSelection()) {
            if(this.sm.getSelected().data.iscreator != "1") {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeletetheselecteduser(s)"), function(btn) {
                    if (btn == "yes") {
                        mainLoadMask.show();
                        Wtf.Ajax.requestEx({
                            url: '../../admin.jsp?action=7&mode=2',
                            method: 'POST',
                            params: {
                                userid: this.sm.getSelected().data.userid
                            }
                        }, this, function(response, e){
                            mainLoadMask.hide();
                            var suc = eval('(' + response + ')');
                            if (suc.success) {
                                msgBoxShow([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.field.User(s)deletedsuccessfully")], 0);
                                this.userds.load();
                            }
                        }, function(resp, req){
                            mainLoadMask.hide();
                        });
                    }
                }, this);

            } else {
                msgBoxShow(135, 1);
            }
        }
    },

    handleChangePassword:function(){

        var memname = this.usergrid.selModel.getSelections()[0].get("username");
        this.changePassWin = new Wtf.Window({
            title : WtfGlobal.getLocaleText("acc.changePass.tabTitle"),
            modal : true,
            iconCls : 'iconwin',
            width : 400,
            height: 240,
            resizable :false,
            buttonAlign : 'right',
            buttons :[{
                text : WtfGlobal.getLocaleText("acc.field.Set"),
                scope : this,
                handler : function() {
                    if(this.changePass.form.isValid()){
                        if(this.newpass.getValue() == this.cnewpass.getValue()){
                            this.changePass.form.submit({
                                scope:this,
                                params:{
                                    action:7,
                                    mode:3,
                                    userid:this.usergrid.selModel.getSelected().get("userid")
                                },
                                success:function(result,resp){
                                    var resObj =eval('('+resp.response.responseText+')');
                                    if(resObj.data==""){
                                        msgBoxShow([WtfGlobal.getLocaleText("acc.field.Success"), WtfGlobal.getLocaleText("acc.field.Passwordchangedsuccessfully")]);
                                        this.changePassWin.close();
                                    }
                                    else
                                       msgBoxShow([WtfGlobal.getLocaleText("acc.common.error"), resObj.data], 1);
                                },
                                failure:function(result,resp){
                                    msgBoxShow([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Errorconnectingtoserver")], 1);
                                }
                            });
                        }else
                            msgBoxShow(133, 1);
                    }
                }
            },{
                text : WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope : this,
                handler : function() {
                    this.changePassWin.close();
                }
            }],
            layout : 'border',
            items :[{
                region : 'north',
                height : 75,
                border : false,
                baseCls : 'northWinClass',
                html : getTopHtml(WtfGlobal.getLocaleText("acc.changePass.tabTitle"),WtfGlobal.getLocaleText("acc.field.Enterpassworddetails"),'../../images/createuser.png')
            },{
                region : 'center',
                border : false,
                baseCls : 'centerWinClass',
                bodyStyle : 'padding:20px 20px 20px 20px;',
                layout : 'fit',
                items : [{
                    border : false,
                    bodyStyle : 'background:transparent;',
                    layout : "fit",
                    items : [
                    this.changePass = new Wtf.form.FormPanel({
                        url: "../../admin.jsp",
                        waitMsgTarget: true,
                        method : 'POST',
                        border : false,
                        bodyStyle : 'font-size:10px;',
                        labelWidth : 150,
                        items : [
                            this.newpass = new Wtf.form.TextField({
                                fieldLabel:WtfGlobal.getLocaleText("acc.field.NewPassword*"),
                                inputType:'password',
                                maxLength:32,
                                minLength:4,
                                name:'pass',
                                width:150,
                                allowBlank:false
                            }),
                            this.cnewpass = new Wtf.form.TextField({
                                fieldLabel:WtfGlobal.getLocaleText("acc.field.ConfirmNewPassword*"),
                                inputType:'password',
                                maxLength:32,
                                minLength:4,
                                name:'rpass',
                                width:150,
                                allowBlank:false
                            })
                        ]
                   })]
                }]
             }]
        });
        this.changePassWin.show();
    },

    handleResetPassword:function(){
        var memname = this.usergrid.selModel.getSelections()[0].get("username");
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"),WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoresetthepasswordfor")+memname+'?', function(btn){
            if (btn == "yes") {
                Wtf.Ajax.request({
                    method: 'POST',
                    url: '../../admin.jsp',
                    params: ({
                        userid:this.usergrid.selModel.getSelections()[0].get("userid"),
                        action:7,
                        mode:4
                    }),
                    scope: this,
                    success: function(result, req){
                        msgBoxShow(134, 0);
                    },
                    failure: function(result, req){
                        msgBoxShow(4, 1);
                    }
                });
            }
        }, this);
    },

    CreateUserWindow: function(params,planinfo){
        var isuserplan = false;
        var usercost = 0;
        var text = "Create";
        var winHeight = 0;
        var userplanMessage = "";
        if(planinfo) {
            isuserplan = planinfo.isuserplan;
            usercost = planinfo.usercost;
            if(isuserplan) {
                winHeight = 95;
                userplanMessage = "<br><br><font color='red'>"+WtfGlobal.getLocaleText("acc.field.Peruserfeewillbeaddedfromthenextbillingcycle")+"</font>";
            } else {
                winHeight = 75;
            }
        }
        if (params.mode == 1) {
            text = WtfGlobal.getLocaleText("acc.common.edit");
            winHeight = 70;
        }
        this.companyAppsitems = [{          
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Username*"),
            name: 'username',
            id:'username',
            allowBlank: false,
            regex: /^\w[\w|\.]*$/,
            disabled: params.mode == 0 ? false : true,
           // value: params.mode == 1 ? params.username : null,
            maxLength: 36
        }, {
            fieldLabel: WtfGlobal.getLocaleText("acc.field.EmailAddress*"),
            name: 'emailid',
            id:'emailaddress',
            allowBlank: false,
            maxLength: 100,
            //                validator:WtfGlobal.validateEmail,
            vtype: 'email'
           // value: params.mode == 1 ? params.emailid : null
        }, {
            fieldLabel: WtfGlobal.getLocaleText("acc.profile.fName"),
            name: 'fname',
            id:'firstname',
            allowBlank: false,
            maxLength: 50
           // value: params.mode == 1 ? params.fname : null
        }, {
            fieldLabel:WtfGlobal.getLocaleText("acc.field.LastName*"),
            id:'lastname',
            name: 'lname',
            allowBlank: false,
            //                validator:WtfGlobal.validateUserName,
            maxLength: 50
           /// value: params.mode == 1 ? params.lname : null
        },
        this.GenderCmb =new Wtf.form.ComboBox({
                           xtype:'combo',
			   mode : 'local',
                           fieldLabel: WtfGlobal.getLocaleText("acc.field.Gender*"),
                           editable: false,
                           hiddenName: 'gender',
			   store: this.GenderStore,
			   displayField: 'name',
			   valueField: 'id',
                           allowBlank: false,
                           triggerAction: 'all',
                           value:params.gender
        }),

        {
            fieldLabel: WtfGlobal.getLocaleText("acc.profile.userPic"),
            name: 'image',
            height: 24,
            inputType: 'file'
        }, {
            fieldLabel: WtfGlobal.getLocaleText("acc.profile.contactNo"),
            id: 'contactnumNewUser',
            name: 'contactno',
            validator:validatePhone,
            maxLength: 15,
            value: params.mode == 1 ? params.contactno : ''
        }, {
            xtype: 'textarea',
            id: 'addressNewUser',
            fieldLabel: 'Address',
            height: 80,
            name: 'address',
            maxLength: 255,
            value: params.mode == 1 ? params.address : ''
        }, {
            xtype: 'hidden',
            name: 'userid',
            value: params.mode == 1 ? params.userid : ''
        }];

        this.companyAppsitems.push(this.appFieldSet);

        if (params.mode == 1) {            
            for (var cnt = 0; cnt < params.userapps.length; cnt++) {
                if(params.userapps[cnt].roleid.length>0) {
                    Wtf.getCmp('approle' + params.userapps[cnt].appid).setValue(params.userapps[cnt].roleid);
                }
            }
        }
        if(!Wtf.getCmp("createUserWin")){
            this.createuserWindow = new Wtf.Window({
                title: WtfGlobal.getLocaleText("acc.field.User"),
                closable: true,
                modal: true,
                id: 'createUserWin',
                iconCls: 'iconwin',
                width: 470,
                height: 420,
                 resizable: false,
                buttons: [{
                    text: params.mode == 1?WtfGlobal.getLocaleText("acc.het.108"):WtfGlobal.getLocaleText("acc.createTemplate"),
                    id: "createUserButton",
                    scope: this,
                    handler: function(obj, e){
                        if (obj.text == WtfGlobal.getLocaleText("acc.createTemplate") || obj.text == WtfGlobal.getLocaleText("acc.het.108")){

                            if (this.userinfo.mode == 0 && this.planinfo.trialperiod < 0) {
                                this.ccwindow(planinfo);
                              // createUser(params.mode);
                            } else{
                               createUser(params.mode);
                            }
                        }
                        else{
                            Wtf.getCmp('createUserWin').close();
                        }
                    }
                }, {
                    text: WtfGlobal.getLocaleText("acc.field.Cancel"),
                    id: 'cancelCreateUserButton',
                    scope: this,
                    handler: function(){
                        Wtf.getCmp('createUserWin').close();
                    }
                }],
                layout: 'border',
                items: [{
                    region: 'north',
                    height: winHeight,
                    border: false,
                    bodyStyle:'background:white;border-bottom:1px solid #bfbfbf;',
                  //  baseCls:'northWinClass',
                    html: getTopHtml(text +WtfGlobal.getLocaleText("acc.field.User"), text+WtfGlobal.getLocaleText("acc.field.UserProfile")+userplanMessage)
                }, {
                    region: 'center',
                    border: false,
                    //                height: 360,
                    autoScroll: true,
                    //                height: 360,
                    baseCls:'centerWinClass',
                    //                layout : 'fit',
                    items: [createuserForm = new Wtf.form.FormPanel({
                        url: '../../admin.jsp?action=7&mode=' + params.mode,
                        method: 'GET',
                        fileUpload: true,
                        border: false,
                        labelWidth: 130,
                        bodyStyle: 'margin-top:10px;margin-left:15px;font-size:11px;',
                        defaults: {
                            width: 220
        },
                        defaultType: 'textfield',
                        items: this.companyAppsitems
                    })]
                }]
            }).show();
        }
                
        if(params.mode == 1){
            Wtf.getCmp("username").setValue(params.username);
            Wtf.getCmp("emailaddress").setValue(params.emailid);
            Wtf.getCmp("firstname").setValue(params.fname);
            Wtf.getCmp("lastname").setValue(params.lname);
            this.GenderCmb.setValue(params.gender);
        }
    }, 

    ccwindow: function(planinfo){
        if (!createuserForm.form.isValid()) {
            msgBoxShow([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Pleasefillinthenecessaryinformation.")]);
        }
        else{
            var planprice = 1 * planinfo.planprice;
            for (var cnt = 0; cnt < compApp.length; cnt++) {
                var app = Wtf.getCmp('approle' + compApp[cnt].appid);
                if(app.getValue()!= -1)
                    planprice += (1 * compApp[cnt].appprice);
            }
            if(!this.planinfo.isPayment && (this.planinfo.subnum == "00000" || this.planinfo.subnum.substring(0,9) == "WITHOUTCC")){
               //  Wtf.getCmp('createUserWin').close();
                
                createSubscription(this.planinfo.planid,this.planinfo.planname,planprice,true);
            }else{
                var paramsarray = {};
                paramsarray.planprice = planprice;
                createUser(0,paramsarray)
            }
        }
    },

//    createUser: function(mode,paramsarray){
//        if (this.createuserForm.form.isValid()) {
//            Wtf.getCmp("cancelCreateUserButton").disable();
//            this.createuserForm.form.submit({
//                waitMsg: 'Loading...',
//                scope: this,
//                params: paramsarray,
//                useraction: mode,
//                failure: function(frm, action){
//                    if(Wtf.getCmp("confirmationWindow")){
//                            Wtf.getCmp("confirmationWindow").close();
//                    }
//                    var text = "create";
//                    if (action.options.useraction == 1)
//                        text = "edit";
//                    if (action.failureType == "client")
//                        msgBoxShow(["Error", " Could not" + text + " user. Please enter valid values"], 1);
//                    else {
//                        if (action.response && action.response.responseText !== undefined && action.response.responseText != "") {
//                            var resObj = eval("(" + action.response.responseText + ")")
//                            if(resObj.data){
//                                msgBoxShow(["Error", " Could not " + text + " user. " + resObj.data], 1);
//                            }else if(resObj.errormsg){
//                                msgBoxShow(["Error", " Could not " + text + " user. " + resObj.errormsg], 1);
//
//                            }
//                            Wtf.getCmp("cancelCreateUserButton").enable();
//                            //                            this.createuserWindow.close();
//                        }
//                    }
//                },
//                success: function(frm, action){
//                    if(Wtf.getCmp("confirmationWindow")){
//                            Wtf.getCmp("confirmationWindow").close();
//                    }
//                    var text = "created";
//                    if (action.options.useraction == 1) {
//                        text = " Profile edited";
//                        msgBoxShow(["Success", "User " + text + " successfully. "], 0);
//                        if(Wtf.getCmp('createUserWin'))
//                            Wtf.getCmp('createUserWin').close();
//                    }
//                    if (action.options.useraction == 0) {
//                        msgBoxShow(["Success", "User " + text + " successfully. "], 0);
//                        if(Wtf.getCmp('createUserWin'))
//                            Wtf.getCmp('createUserWin').close();
//                    }
//                    this.userds.load();
//                }
//            });
//        }else {
//            msgBoxShow(["Error", "Please fill in the necessary information."]);
//        }
//        //else { Wtf.getCmp("createUserButton").enable(); Wtf.getCmp("cancelCreateUserButton").enable(); }
//    },

    manageAppTeams: function(appid, appname){
        this.creategrid(appid);
        this.centerdiv = document.createElement("div");
        this.centerdiv.appendChild(this.movetoright);
        this.centerdiv.appendChild(this.movetoleft);
        this.centerdiv.style.padding = "135px 10px 135px 10px";
        this.assignTeamWin = new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.field.Assignusersfor")+" '" + appname + "'",
            closable: true,
            modal: true,
            iconCls: 'iconwin',
            width: 600,
            height: 525,
            resizable: false,
            buttonAlign: 'right',
            buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.update"),
                scope: this,
                handler: function(){
                    this.assignUserSubmit(appid)
                }
            }, {
                text:WtfGlobal.getLocaleText("acc.msgbox.cancel"),
                scope: this,
                handler: function(){
                    this.assignTeamWin.close();
                }
            }],
            layout: 'border',
            items: [{
                region: 'north',
                height: 75,
                border: false,
                baseCls : 'northWinClass',
                html: getTopHtml(WtfGlobal.getLocaleText("acc.field.AssignUsers"), WtfGlobal.getLocaleText("acc.field.Assignusersfor")+" <b>" + appname + "</b>", "../../images/createuser.png")
            }, {
                region: 'center',
                border: false,
                baseCls : 'centerWinClass',
                bodyStyle: 'padding:20px 20px 20px 20px;',
                layout: 'fit',
                items: [{
                    border: false,
                    bodyStyle: 'background:transparent;',
                    layout: 'border',
                    items: [{
                        region: 'west',
                        border: false,
                        width: 250,
                        layout: 'fit',
                        items: [{
                            xtype: 'KWLListPanel',
                            title: WtfGlobal.getLocaleText("acc.field.NewUsers"),
                            border: false,
                            paging: false,
                            layout: 'fit',
                            autoLoad: false,
                            items: this.availablegrid
                        }]
                    }, {
                        region: 'center',
                        border: false,
                        contentEl: this.centerdiv
                    }, {
                        region: 'east',
                        border: false,
                        width: 250,
                        layout: 'fit',
                        items: [{
                            xtype: 'KWLListPanel',
                            title: WtfGlobal.getLocaleText("acc.field.AssignedUsers"),
                            border: false,
                            paging: false,
                            layout: 'fit',
                            autoLoad: false,
                            items: this.selectedgrid
                        }]
                    }]
                }]
            }]
        });
        this.assignTeamWin.show();

    },

    assignUserSubmit: function(appid){
        var addid = "";
        var roleid = "";
        var masterrole = "";
        var delid = "";
            for (var ctr = 0; ctr < this.selectedds.getCount(); ctr++) {
                var recData = this.selectedds.getAt(ctr).data;
                addid += recData.userid;
                roleid += recData.roleid;
                var index = this.getMasterRole(recData.roleid);
                masterrole += this.positionds.getAt(index).data.masterroleid;

                if (ctr < this.selectedds.getCount() - 1) {
                    addid += ',';
                    roleid += ',';
                    masterrole += ',';
                }
            }

            for (var ctr = 0; ctr < this.availableds.getCount(); ctr++) {
                delid += this.availableds.getAt(ctr).data.userid;
                if (ctr < this.availableds.getCount() - 1) {
                    delid += ',';
                }
            }

            Wtf.Ajax.requestEx({
                url: '../../admin.jsp',
                params: {
                    appid: appid,
                    addid: addid,
                    roleid: roleid,
                    masterrole: masterrole,
                    delid: delid,
                    action: 11
                },
                method: 'POST'
            }, this, function(request, response){
                var suc = eval('(' + request + ')');
                if (suc.success == true) {
                    msgBoxShow([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.field.Usersubscriptionmanagedsuccessfully")], 0);
                    this.userds.load();
                }
            }, function(request, response){
            });
            this.assignTeamWin.close();
    }, 

    getMasterRole: function(ID){
        var index = this.positionds.findBy(function(record){
            if (record.get("roleid") == ID)
                return true;
            else
                return false;
        });
        if (index == -1)
            return null;
        return index;
    },

    creategrid: function(appid){
        this.availableds = new Wtf.data.Store({
            url: '../../admin.jsp',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty: 'count'
            }, ['fullname', 'username', 'userid']),
            autoLoad: false,
            baseParams: {
                action: 9,
                appid: appid
            }
        });

        this.positionds = new Wtf.data.Store({
            url: '../../admin.jsp',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty: 'count'
            }, ['rolename', 'roleid', 'masterroleid']),
            autoLoad: false,
            baseParams: {
                action: 10,
                appid: appid
            }
        });
        this.positionds.load();
        this.positionCombo = new Wtf.form.ComboBox({
            name: 'Position',
            store: this.positionds,
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectRole----"),
            displayField: 'rolename',
            valueField: 'roleid',
            typeAhead: true,
            mode: 'local',
            width: 120,
            value: '2',
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus: true
        });
        this.availablesm = new Wtf.grid.CheckboxSelectionModel();

        this.availablecm = new Wtf.grid.ColumnModel([this.availablesm, {
            header: WtfGlobal.getLocaleText("acc.userAdmin.userName"),
            dataIndex: 'fullname',
            autoWidth: true,
            sortable: true,
            groupable: true
        }]);
        this.quickSearchEmp = new Wtf.KWLQuickSearch({
            width: 100,
            field: "fullname"           
        });
        this.availablegrid = new Wtf.grid.GridPanel({
            layout: 'fit',
            store: this.availableds,
            cm: this.availablecm,
            sm: this.availablesm,
            border: false,
            loadMask: {
                msg: WtfGlobal.getLocaleText("acc.msgbox.50")
            },
            viewConfig: {
                forceFit: true,
                autoFill: true
            },
            tbar: [WtfGlobal.getLocaleText("acc.field.QuickSearch"), this.quickSearchEmp]
        });

        this.availableds.load();
        this.availableds.on("load", this.empSearch, this);

        this.selectedds = new Wtf.data.Store({
            url: '../../admin.jsp',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty: 'count'
            }, ['fullname', 'username', 'userid', 'roleid', 'iscreator']),
            autoLoad: false,
            baseParams: {
                action: 8,
                appid: appid
            }
        });
        this.selectedsm = new Wtf.grid.CheckboxSelectionModel();
        this.selectedcm = new Wtf.grid.ColumnModel([this.selectedsm, {
            header: WtfGlobal.getLocaleText("acc.auditTrail.gridUser"),
            dataIndex: 'fullname',
            sortable: true
        }, {
            header: WtfGlobal.getLocaleText("acc.field.Roles"),
            dataIndex: 'roleid',
            editor: this.positionCombo,
            renderer: Wtf.ux.comboBoxRenderer(this.positionCombo)
        }]);
        this.quickSearchAssgEmp = new Wtf.KWLQuickSearch({
            width: 100,
            field: "fullname"
        });
        this.selectedgrid = new Wtf.grid.EditorGridPanel({
            store: this.selectedds,
            cm: this.selectedcm,
            sm: this.selectedsm,
            border: false,
            clicksToEdit: 1,
            view : new Wtf.grid.GridView({
                forceFit:true,
                emptyText:"<div>"+WtfGlobal.getLocaleText("acc.field.Nousersselected")+"</div>",
                getRowClass : function(record, index, rowParams, store) {
                    if(record.data.iscreator == "1") {
                        return "x-item-disabled";
                    }
                }
            }),
            loadMask: {
                msg: WtfGlobal.getLocaleText("acc.msgbox.50")
            },
            tbar: [WtfGlobal.getLocaleText("acc.field.QuickSearch"), this.quickSearchAssgEmp]
        });
        this.selectedgrid.on("beforeedit",function(e){
            if(e.record.data.iscreator == "1"){
                return false;
            }
        },this);
        this.selectedds.load();
        this.selectedds.on("load", this.empAssgSearch, this);

        this.movetoright = document.createElement('img');
        this.movetoright.src = "../../images/arrowright.gif";
        this.movetoright.style.width = "24px";
        this.movetoright.style.height = "24px";
        this.movetoright.style.margin = "5px 0px 5px 0px";
        this.movetoright.onclick = this.movetorightclicked.createDelegate(this, []);
        this.movetoleft = document.createElement('img');
        this.movetoleft.src = "../../images/arrowleft.gif";
        this.movetoleft.style.width = "24px";
        this.movetoleft.style.height = "24px";
        this.movetoleft.style.margin = "5px 0px 5px 0px";
        this.movetoleft.onclick = this.movetoleftclicked.createDelegate(this, []);
        this.selectedsm.on("beforerowselect", function(obj, row, keepExisting, record) {
            if(record.data.iscreator == "1") {
                return false;
            }
        },this);
    },    
    empSearch: function(store, rec, opt){
        this.quickSearchEmp.StorageChanged(store);
    },    
    empAssgSearch: function(store, rec, opt){
        this.quickSearchAssgEmp.StorageChanged(store);
    },

    movetorightclicked: function(){
        var selected = this.availablesm.getSelections();
            var recArr = [];
            for (var ctr = 0; ctr < selected.length; ctr++) {
                recArr[0] = selected[ctr];
                this.selectedds.add(recArr);
                this.selectedds.getAt(this.selectedds.getCount() - 1).set('roleid', this.positionCombo.store.getAt(0).data.roleid);
                this.availableds.remove(selected[ctr]);
            }
    },

    movetoleftclicked: function(){
        var selected = this.selectedsm.getSelections();
        if (selected.length > 0) {
            this.availableds.add(selected);
        }
        for (var ctr = 0; ctr < selected.length; ctr++) {
               this.selectedds.remove(selected[ctr]);
            }
        }
});

Wtf.common.adminPageCompany = function(config){
    Wtf.common.adminPageCompany.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.common.adminPageCompany, Wtf.Panel, {
    onRender: function(config){
        Wtf.common.adminPageCompany.superclass.onRender.call(this, config);

        if (!Wtf.StoreMgr.containsKey("timezone")) {
            Wtf.timezoneStore.load();
            Wtf.timezoneStore.on("load", function(){
                Wtf.StoreMgr.add("timezone", Wtf.timezoneStore);
            })
        }

        if (!Wtf.StoreMgr.containsKey("country")) {
            Wtf.countryStore.load();
            Wtf.countryStore.on("load", function(){
                Wtf.StoreMgr.add("country", Wtf.countryStore);
            });
        }

        if (!Wtf.StoreMgr.containsKey("currency")) {
            Wtf.currencyStore.load();
            Wtf.currencyStore.on("load", function(){
                Wtf.StoreMgr.add("currency", Wtf.currencyStore);
            });
        }

        var seperator = {
            border: false,
            html: '<hr style = "width:75%;margin-left:10px">'
        };

        var defConf = {
            ctCls: 'fieldContainerClass',
            labelStyle: 'font-size:11px; text-align:right;'
        };

        this.companyDetailsPanel = new Wtf.form.FormPanel({
            id: 'companyDetailsForm',
            url: "../../admin.jsp?action=5&cmode=2",
            fileUpload: true,
            cls: 'adminFormPanel',
            autoScroll: true,
            border: false,
            items: [{
                layout: 'column',
                border: false,
                items: [{
                    columnWidth: 0.49,
                    border: false,
                    items: [({
                        id: 'compfieldset',
                        xtype: 'fieldset',
                        disabledClass: 'companyFieldSet-disable',
                        cls: "companyFieldSet",
                        title: WtfGlobal.getLocaleText("acc.pdf.15"),
                        defaultType: 'textfield',
                        autoHeight: true,
                        items: [{
                            labelStyle: 'font-size:11px; text-align:right;',
                            name: 'logo',
                            id: 'logoFileDialog',
                            inputType: "file",
                            ctCls: 'fieldContainerClass',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Logo"),
                            width: 200
                        }, {
                            xtype: 'panel',
                            border: false,
                            id: 'compLogoinfo',
                           html: "Recommended image size 130 X 25, image type JPEG, JPG, GIF, PNG "
                        }, {
                            xtype: 'panel',
                            border: false,
                            html: "<img id='displaycompanylogo' style='margin-left:120px; margin-top:20px;' src = ''>"
                        }]
                    }), seperator, {
                        xtype: 'fieldset',
                        cls: "companyFieldSet",
                        defaults: defConf,
                        title: WtfGlobal.getLocaleText("acc.field.CompanyDetails"),
                        defaultType: 'textfield',
                        autoHeight: true,
                        items: [{
                            id: 'nameField',
                            name: 'companyname',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Name*"),
                            allowBlank: false,
                            maxLength: 100,
                            width: 200
                        }, {
                            id: 'domainField',
                            name: 'domainname',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Subdomain*"),
                         // allowBlank: false,
                         // invalidText: 'Alphabets and numbers only, 1-32 characters',
                            maxLength: 32,
                            minLength: 1,
                            disabled:true,
                         // validator: WtfGlobal.validateUserid,
                            width: 200
                        }, {
                            xtype: 'textarea',
                            id: 'addressField',
                            name: 'address',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Address"),
                            maxLength: 1024,
                            height: 80,
                            width: 200
                        }, {
                            id: 'cityField',
                            name: 'city',
                            maxLength: 50,
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.City"),
                            width: 200
                        }, {
                            id: 'stateField',
                            name: 'state',
                            maxLength: 50,
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.State"),
                            width: 200
                        }, {
                            xtype: 'numberfield',
                            id: 'zipField',
                            name: 'zip',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.ZipCode"),
                            maxLength: 10,
                            width: 200
                        }, {
                            xtype: 'combo',
                            typeAhead: true,
                            forceSelection: true,
                            selectOnFocus: true,
                            emptyText: WtfGlobal.getLocaleText("acc.field.Selectacountry"),
                            autoCreate: {
                                tag: "input",
                                type: "text",
                                size: "24",
                                autocomplete: "on"
                            },
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Country"),
                            hiddenName: 'country',
                            store: Wtf.countryStore,
                            displayField: 'name',
                            valueField: 'id',
                            mode: 'local',
                            triggerAction: 'all',
                            id: 'countryField',
                            allowBlank: false,
                            width: 200
                        }]
                    }, seperator, {
                        xtype: 'fieldset',
                        cls: "companyFieldSet",
                        title: WtfGlobal.getLocaleText("acc.profile.timeZone"),
                        defaults: defConf,
                        autoHeight: true,
                        items: [{
                            xtype: 'combo',
                            allowBlank: false,
                            emptyText: WtfGlobal.getLocaleText("acc.field.Selectatimezone"),
                            typeAhead: true,
                            forceSelection: true,
                            triggerAction: 'all',
                            id: 'timezoneField',
                            store: Wtf.timezoneStore,
                            displayField: "name",
                            valueField: 'id',
                            mode: 'local',
                            hiddenName: 'timezone',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectTime-Zone"),
                            height: 80,
                            width: 200
                        }]
                    }, seperator, {
                        xtype: 'fieldset',
                        cls: "companyFieldSet",
                        title: WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
                        defaults: defConf,
                        autoHeight: true,
                        items: [this.currencyfield = new Wtf.form.ComboBox({
                            allowBlank: false,
                            emptyText: WtfGlobal.getLocaleText("acc.field.Selectcurrency"),
                            typeAhead: true,
                            forceSelection: true,
                            triggerAction: 'all',
                            id: 'currencyField',
                            store: Wtf.currencyStore,
                            displayField: "currencyname",
                            valueField: 'currencyid',
                            mode: 'local',
                           // editable: false,
                            hiddenName: 'currency',
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectCurrency"),
                            height: 80,
                            width: 200
                        })]
                    }]
                }, {
                    columnWidth: 0.49,
                    border: false,
                    items: [{

                        xtype: 'fieldset',
                        cls: "companyFieldSet",
                        title: WtfGlobal.getLocaleText("acc.field.ContactInformation"),
                        defaultType: 'textfield',
                        defaults: defConf,
                        autoHeight: true,
                        items: [{
                            id: 'phoneField',
                            //xtype:'numberfield',
                            name: 'phone',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.PhoneNumber"),
                            validator:validatePhone,
                            invalidText: WtfGlobal.getLocaleText("acc.field.PleaseenteravalidPhoneNumber"),
                            maxLength: 16,
                            width: 200
                        }, {
                            id: 'faxField',
                            name: 'fax',
                            maxLength: 50,
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.FaxNumber"),
                            validator:validatePhone,
                            invalidText: WtfGlobal.getLocaleText("acc.field.PleaseenteravalidFaxNumber"),
                            width: 200
                        }, {
                            id: 'websiteField',
                            vtype: 'url',
                            name: 'website',
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Website"),
                            maxLength: 50,
                            width: 200
                        }, {
                            id: 'emailField',
                            name: 'mail',
                          //  validator: WtfGlobal.validateEmail,
                            vtype:'email',
                            maxLength: 50,
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.EmailAddress"),
                            invalidText: WtfGlobal.getLocaleText("acc.field.PleaseenteravalidEmailAddress"),
                            width: 200
                        }]
                    }]
                }]
            }]
        });

        if (this.companyEdit) {
            Wtf.getCmp("nameField").disable();
//            Wtf.getCmp("domainField").disable();
            Wtf.getCmp("addressField").disable();
            Wtf.getCmp("cityField").disable();
            Wtf.getCmp("stateField").disable();
            Wtf.getCmp("zipField").disable();
            Wtf.getCmp("countryField").disable();
            Wtf.getCmp("timezoneField").disable();
            Wtf.getCmp("currencyField").disable();
            Wtf.getCmp("phoneField").disable();
            Wtf.getCmp("faxField").disable();
            Wtf.getCmp("websiteField").disable();
            Wtf.getCmp("emailField").disable();
        }

        if (this.companyEditLogo) {
            Wtf.getCmp("compfieldset").disable();
        }

        Wtf.getCmp("nameField").on("change", function(){
            Wtf.getCmp("nameField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("nameField").getValue()));
        }, this);

        Wtf.getCmp("addressField").on("change", function(){
            Wtf.getCmp("addressField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("addressField").getValue()));
        }, this);

        Wtf.getCmp("cityField").on("change", function(){
            Wtf.getCmp("cityField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("cityField").getValue()));
        }, this);

        Wtf.getCmp("stateField").on("change", function(){
            Wtf.getCmp("stateField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("stateField").getValue()));
        }, this);

        Wtf.getCmp("websiteField").on("change", function(){
            var validateURL = WtfGlobal.HTMLStripper(Wtf.getCmp("websiteField").getValue());
            if (validateURL != "") {
                if (validateURL.indexOf("http://") != 0 && validateURL.indexOf("https://") != 0 && validateURL.indexOf("ftp://") != 0)
                    validateURL = "http://" + validateURL;
            }
            Wtf.getCmp("websiteField").setValue(validateURL);
        }, this);

        var detailPanel = new Wtf.Panel({
            layout: "border",
            border: false,
            bodyStyle: "background-color:#ffffff;",
            bbar: [{
                text: WtfGlobal.getLocaleText("acc.common.update"),
                scope: this,
                handler: this.updateCompany,
                disabled: Wtf.checkGracePeriod=="makepayment" ? true:false,
                tooltip: {
                    title: WtfGlobal.getLocaleText("acc.common.update"),
                    text: WtfGlobal.getLocaleText("acc.field.Clicktoupdatecompanydetails")
                },
                iconCls: "pwnd updatecompanydetails"
            }],
            items: [{
                border: false,
                region: 'center',
                autoScroll: true,
                items: [this.companyDetailsPanel]
            }]
        });
        this.add(detailPanel);
        mainLoadMask.show();
        Wtf.Ajax.requestEx({
            url: '../../admin.jsp',
            params: {
                action: 5,
                cmode: 1
            }
        }, this, function(request, response){
            mainLoadMask.hide();
            var res = eval('(' + request + ')');
            if (res && res.data) {
                this.doLayout();
                this.fillData(res.data[0]);

            }
            else {
                msgBoxShow(108, 1);
                this.companyDetailsPanel.disable();
            }
        }, function(){
            mainLoadMask.hide();
            msgBoxShow(108, 1);
            this.companyDetailsPanel.disable();
        });
    },   
    fillData: function(resObj){
        Wtf.getCmp("nameField").setValue(resObj.companyname);
        Wtf.getCmp("addressField").setValue(resObj.address);
        Wtf.getCmp("cityField").setValue(resObj.city);
        Wtf.getCmp("stateField").setValue(resObj.state);
        Wtf.getCmp("zipField").setValue(resObj.zip);
        Wtf.getCmp("phoneField").setValue(resObj.phone);
        Wtf.getCmp("faxField").setValue(resObj.fax);
        Wtf.getCmp("websiteField").setValue(resObj.website);
        Wtf.getCmp("emailField").setValue(resObj.emailid);
        Wtf.getCmp("domainField").setValue(resObj.subdomain);
        document.getElementById('displaycompanylogo').src = "images/store/?company=true&" + Math.random();

        if (!Wtf.StoreMgr.containsKey("timezone")) {
            Wtf.timezoneStore.on("load", function(){
                Wtf.getCmp("timezoneField").setValue(resObj.timezone);
            });

        }
        else {
            Wtf.getCmp("timezoneField").setValue(resObj.timezone);
        }

        if (!Wtf.StoreMgr.containsKey("country")) {
            Wtf.countryStore.on("load", function(){
                Wtf.getCmp("countryField").setValue(resObj.country);
            });

        }
        else {
            Wtf.getCmp("countryField").setValue(resObj.country);
        }

        if (!Wtf.StoreMgr.containsKey("currency")) {
            Wtf.currencyStore.on("load", function(){
                Wtf.getCmp("currencyField").setValue(resObj.currency);
            });

        }
        else {
            Wtf.getCmp("currencyField").setValue(resObj.currency);
        }
    },

    updateCompany: function(){

        Wtf.getCmp("nameField").validate();
       // Wtf.getCmp("domainField").validate();
        if (Wtf.getCmp("nameField").isValid() /* && Wtf.getCmp("domainField").isValid() */) {
            this.companyDetailsPanel.form.submit({
                scope: this,
                success: function(result, action){
                    var resultObj = eval('(' + action.response.responseText + ')');
                    document.getElementById('companyLogo').src = "images/store/?company=true&" + Math.random();
                    document.getElementById('displaycompanylogo').src = "images/store/?company=true&" + Math.random();
                    Wtf.CurrencySymbol = Wtf.currencyStore.getAt(Wtf.currencyStore.find('currencyid', this.currencyfield.getValue())).data.htmlcode;
                    if (resultObj.data == null)
                        msgBoxShow(109, 0);
                    else
                        msgBoxShow([WtfGlobal.getLocaleText("acc.field.CompanyAdministration"), resultObj.data], 1);
                },
                failure: function(frm, action){
                    if (action.failureType == "client")
                        msgBoxShow(110, 1);
                    else {
                        var resObj = eval("(" + action.response.responseText + ")");
                        msgBoxShow(111, 1);
                    }
                }
            });
        }
    }
});

Wtf.common.MainAdmin = function(config){
    Wtf.common.MainAdmin.superclass.constructor.call(this, config);
    this.addEvents({
        'adminclicked': true,
        'panelRendered': true,
        "companyclicked": true
    });
    this.on("adminclicked", this.handleadminClick, this);
    this.on("companyclicked", this.handleCompanyClick, this);
    this.actTab = null;
}

Wtf.extend(Wtf.common.MainAdmin, Wtf.Panel, {
    handleadminClick: function(){
        this.actTab = 0;
        this.setActivefunc();
    },

    handleCompanyClick: function(){
        this.actTab = 1;
        this.setActivefunc();
    },
    onRender: function(config){
        Wtf.common.MainAdmin.superclass.onRender.call(this, config);
        this.adminuser = new Wtf.common.adminpageuser({
            title: WtfGlobal.getLocaleText("acc.dashboard.userAdministration"),
            layout: 'fit',
            border: false,
            iconCls: 'pwnd userTabIcon'
        });
        this.adminCompany = new Wtf.common.adminPageCompany({
            id:'companyAdmin',
            title: WtfGlobal.getLocaleText("acc.field.CompanyAdministration"),
            layout: 'fit',
            border: false,
            iconCls: 'pwnd projectTabIcon'
        });
        this.tabpanel = this.add(new Wtf.TabPanel({
            id: 'subtabpanel' + this.id,
            border: false,
            activeItem: 0
        }));
        this.tabpanel.add(this.adminuser);
        if(Wtf.partnerStatus != 1){
            this.tabpanel.add(this.adminCompany);
        }
        if (this.event) {
            this.fireEvent(this.event);
        }
        this.adminuser.on("render", this.setActivefunc, this);
    },

    setActivefunc: function(obj){
        this.tabpanel.setActiveTab(this.actTab);
    }
});

Wtf.common.edituser = function(userid){
    alert(userid);
}

 function createUser(mode,paramsarray,scope){
        //var createuserform = Wtf.getCmp("createuserform");
        if (createuserForm.form.isValid()) {
            Wtf.getCmp("cancelCreateUserButton").disable();
            createuserForm.form.submit({
                waitMsg: 'Loading...',
                scope: this,
                params: paramsarray,
                useraction: mode,
                failure: function(frm, action){
                    if(Wtf.getCmp("confirmationWindow")){
                            Wtf.getCmp("confirmationWindow").close();
                    }
                    var text = "create";
                    if (action.options.useraction == 1)
                        text = "edit";
                    if (action.failureType == "client")
                        msgBoxShow([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Couldnot")  + text + WtfGlobal.getLocaleText("acc.field.user.Pleaseentervalidvalues")], 1);
                    else {
                        if (action.response && action.response.responseText !== undefined && action.response.responseText != "") {
                            var resObj = eval("(" + action.response.responseText + ")")
                            if(resObj.data){
                                msgBoxShow([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Couldnot") + text + WtfGlobal.getLocaleText("acc.field.user.") + resObj.data], 1);
                            }else if(resObj.errormsg){
                                msgBoxShow([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Couldnot")  + text + WtfGlobal.getLocaleText("acc.field.user.") + resObj.errormsg], 1);

                            }

                            Wtf.getCmp("cancelCreateUserButton").enable();
                            //                            this.createuserWindow.close();
                        }
                    }
                },
                success: function(frm, action){
                    var contacts = Wtf.getCmp('contactschattree');
                    contacts.contactStore.load();
                    if(Wtf.getCmp("confirmationWindow")){
                            Wtf.getCmp("confirmationWindow").close();
                    }
                    var text = "created";
                    if (action.options.useraction == 1) {
                        text = WtfGlobal.getLocaleText("acc.field.Profileedited");
                        msgBoxShow([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.field.User")+ text + WtfGlobal.getLocaleText("acc.field.successfully.")], 0);
                        if(Wtf.getCmp('createUserWin'))
                            Wtf.getCmp('createUserWin').close();
                    }
                    if (action.options.useraction == 0) {
                        msgBoxShow([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.field.User")+ text + WtfGlobal.getLocaleText("acc.field.successfully.")], 0);
                        if(Wtf.getCmp('createUserWin'))
                            Wtf.getCmp('createUserWin').close();
                    }
                    Wtf.getCmp("user-grid").getStore().load();
                }
            });
        }else {
            msgBoxShow([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Pleasefillinthenecessaryinformation.")]);
        }
        //else { Wtf.getCmp("createUserButton").enable(); Wtf.getCmp("cancelCreateUserButton").enable(); }
    };

function validatePhone(value){
        var regex = /^([^-])(\(?\+?[0-9]*\)?)?[0-9_\- \(\)]*$/;
        if(value!=""){
            if(value.match(regex)){
                return true;
            }
            else {
                return false;
            }
        }
        else{
            return false;
        }
    };

var profile = new Wtf.common.MainAdmin({
    id: 'mainAdmin',
    layout: 'fit',
    border: false,
    event: Wtf.getCmp("tabcompanyadminpanel").activesubtab
});
Wtf.getCmp("tabcompanyadminpanel").add(profile);
Wtf.getCmp("tabcompanyadminpanel").doLayout();
