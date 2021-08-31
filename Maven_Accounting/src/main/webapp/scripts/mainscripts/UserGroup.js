/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

/**
 * This Component is used for create Users Group for Users Visibility Feature
 * @param {type} config
 * @returns {undefined}
 */
Wtf.UserGroup = function(config) {
    this.isEdit = false;
    Wtf.apply(this, {
        title: WtfGlobal.getLocaleText("acc.user.usergroupbtn"),
        modal: true,
        iconCls: getButtonIconCls(Wtf.etype.deskera),
        width: 600,
        height: 400,
        resizable: false,
        buttonAlign: "right",
        buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.close"),
                scope: this,
                handler: function() {
                    this.close();
                }
            }]
    }, config);

    Wtf.UserGroup.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.UserGroup, Wtf.Window, {
    draggable: false,
    onRender: function(config) {
        Wtf.UserGroup.superclass.onRender.call(this, config);
        this.usergrouprec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'users'},
            {name: 'usersid'},
            {name: 'groupid'}
        ]);
        this.usergrpstore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.usergrouprec),
            url: "ACCMaster/getUsersGroup.do",
            params: {
            },
            scope: this
        });
        this.usergrpstore.load();
        this.usergrpcm = new Wtf.grid.ColumnModel([new Wtf.grid.CheckboxSelectionModel({
                singleSelect: true
            }), {
                header: WtfGlobal.getLocaleText("acc.user.groupname"),
                align: 'left',
                dataIndex: 'name',
                autoWidth: true
            }, {
                header: WtfGlobal.getLocaleText("acc.user.usersname"),
                dataIndex: 'users',
                align: 'center',
                autoWidth: true,
                renderer: function(value) {
                    value = value.replace(/\'/g, "&#39;");
                    value = value.replace(/\"/g, "&#34");
                    return "<span class=memo_custom  wtf:qtip='" + value + "'>" + Wtf.util.Format.ellipsis(value, 60) + "</span>"
                }
            }]);
        this.addgroup = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.user.addgrp"),
            iconCls: getButtonIconCls(Wtf.etype.add),
            tooltip: WtfGlobal.getLocaleText("acc.user.addgrp"), //ERP-25613
            disabled: false
        });
        this.addgroup.on('click', function() {
            this.addUserGroup(false);
        }, this);
        this.editgroup = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.user.editgrp"),
            iconCls: getButtonIconCls(Wtf.etype.edit),
            tooltip: WtfGlobal.getLocaleText("acc.user.editgrp"),
            disabled: true
        });
        this.editgroup.on('click', function() {
            this.addUserGroup(true);
            this.editGroup();
        }, this);
        this.deletegroup = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.user.deletegrp"),
            iconCls: getButtonIconCls(Wtf.etype.deletebutton),
            tooltip: WtfGlobal.getLocaleText("acc.user.deletegrp"), //ERP-25613
            disabled:true
        });
        this.deletegroup.on('click', function() {
            this.handleDelete();
        }, this);
        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 15,
            id: "pagingtoolbar" + this.id,
            store: this.usergrpstore,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
        });

        this.usergrpstore.on('beforeload', function(store, option) {
            var currentBaseParams = this.usergrpstore.baseParams;
            this.usergrpstore.baseParams = currentBaseParams;
        }, this);

        this.usergrpGrid = new Wtf.grid.GridPanel({
            layout: 'fit',
            autoScroll: true,
            height: 330,
            width: 580,
            store: this.usergrpstore,
            tbar: [this.addgroup, this.editgroup, this.deletegroup],
            cm: this.usergrpcm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            bbar: this.pagingToolbar
        });
        this.usergrpGrid.on('rowclick', this.handleRowClick, this);
        this.add(this.usergrpGrid);
    },
    /**
     * Function to enable edit functionality
     */
    handleRowClick: function() {
        var arr=this.usergrpGrid.getSelectionModel().getSelections();
        var rec = this.usergrpGrid.getSelectionModel().getSelected();
        if (arr.length == 1) {
            this.editgroup.enable();
            this.deletegroup.enable();
        } else if (arr.length > 1) {
            this.editgroup.disable();
            this.deletegroup.enable();
        } else {
            this.editgroup.disable();
            this.deletegroup.disable();
        }
    },
    /**
     * Function for Add User Grp
     */
    addUserGroup: function(isEdit) {
        this.isEdit = isEdit;
        this.UserGroupConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.user.groupname"),
            name: 'usergroup',
            hiddenName: 'usergroup',
            id: "usergroup" + this.heplmodeid + this.id,
        };

        this.usergroup = WtfGlobal.createTextfield(this.UserGroupConfig, false, true, 255, this);
        this.usersRec = new Wtf.data.Record.create([
            {name: 'userid'},
            {name: 'username'},
            {name: 'fname'},
            {name: 'lname'},
            {name: 'fullname'},
            {name: 'image'},
            {name: 'emailid'},
            {name: 'lastlogin', type: 'date'},
            {name: 'aboutuser'},
            {name: 'address'},
            {name: 'contactno'},
            {name: 'rolename'},
            {name: 'roleid'}
        ]);
        this.userds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.usersRec),
            url: "ProfileHandler/getAllUserDetails.do",
            baseParams: {
                mode: 11
            }
        });
        this.userds.on('load', function() {
            if (this.isEdit) {
                var rec = this.usergrpGrid.getSelectionModel().getSelected();
                if (rec != null && rec != "" && rec != undefined) {
                    this.userCombo.setValue(rec.data.usersid);
                }
            }
        }, this);
        this.userCombo = new Wtf.common.Select({
            width: 150,
            fieldLabel: WtfGlobal.getLocaleText("acc.user.usersname"),
            name: 'approver',
            store: this.userds,
            hiddenName: 'approver',
            xtype: 'select',
            selectOnFocus: true,
            forceSelection: true,
            multiSelect: true,
            displayField: 'fullname',
            valueField: 'userid',
            mode: 'local',
            allowBlank: false,
            triggerAction: 'all',
            typeAhead: true
        })
        this.userds.load();



        this.usergroupform = new Wtf.form.FormPanel({
//            frame:true,
            url: "ACCMaster/saveUserGroup.do",
            labelWidth: 125,
            border: false,
            autoHeight: true,
            bodyStyle: 'padding:5px 5px 20px',
            autoWidth: true,
            defaults: {anchor: '94%'},
            defaultType: 'textfield',
            items: [this.usergroup, this.userCombo],
            buttons: [{
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                    handler: function() {
                        if (this.usergroupform.getForm().isValid()) {

                            this.usergroupform.getForm().submit({
                                waitMsg: WtfGlobal.getLocaleText("acc.user.Savinggrp..."),
                                params: {
                                    usergroup: this.usergroup.getValue(),
                                    users: this.userCombo.getValue(),
                                    usergroupid: this.groupid
                                },
                                scope: this,
                                success: function(f, a) {
                                    this.usergroupwin.close();
                                    this.usergrpstore.load({
                                        params: {
                                            start: 0,
                                            limit: this.pagingToolbar.pageSize,
                                        }
                                    });
                                    var response = eval('(' + a.response.responseText + ')')
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.data.msg], response.success * 2 + 1);
                                },
                                failure: function(f, a) {
                                    this.usergroupwin.close();
                                    this.genFailureResponse(eval('(' + a.response.responseText + ')'))
                                }
                            });
                        }
                    },
                    scope: this
                }, {
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                    scope: this,
                    handler: function() {
                        this.usergroupwin.close();
                    }
                }]
        });
        this.usergroupform.add({xtype: 'hidden', name: 'featureid'})

        this.usergroupwin = new Wtf.Window({
            title: this.isEdit ? WtfGlobal.getLocaleText("acc.user.editgrp") : WtfGlobal.getLocaleText("acc.user.addgrp"),
            closable: true,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            width: 500,
            autoHeight: true,
            modal: true,
            buttonAlign: 'right',
            items: this.usergroupform
        });
        this.usergroupwin.show();
    },
    editGroup: function() {
        var rec = this.usergrpGrid.getSelectionModel().getSelected();
        if (rec != null && rec != "" && rec != undefined) {
            this.groupid = rec.data.groupid;
            this.usergroup.setValue(rec.data.name);
            this.userCombo.setValue(rec.data.usersid);
        }
    },
    handleDelete: function() {
        if (this.usergrpGrid.getSelectionModel().hasSelection()) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.user.usergrp.confirmDelete"), function(btn) {
                if (btn == "yes") {
                    var arr = [];
                    this.recArr = this.usergrpGrid.getSelectionModel().getSelections();
                    for (i = 0; i < this.recArr.length; i++) {
                        arr.push(this.usergrpstore.indexOf(this.recArr[i]));
                    }
                    var data = WtfGlobal.getJSONArray(this.usergrpGrid, true, arr);
                    Wtf.Ajax.requestEx({
                        url: "ACCMaster/deleteUsersGroup.do",
                        params: {
                            data: data,
                        }
                    }, this, this.genSuccessResponse, this.genFailureResponse);
                } else {
                    return;
                }
            }, this);
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.user.nouser")], 2);
            return;
        }

    },
    genSuccessResponse: function(response) {
        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function() {
            this.usergrpstore.load({
                params: {
                    start: 0,
                    limit: this.pagingToolbar.pageSize,
                }
            });
        }, this);
    },
    genFailureResponse: function(response) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Erroroccurredwhiledeletinggrp")], 2);
    }
});

