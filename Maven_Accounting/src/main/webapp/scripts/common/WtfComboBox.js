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
Wtf.form.FnComboBox=function(config){
    this.initial="REC";
    config.hideAddButton = config.hideAddButton ? config.hideAddButton : false;
    config.addCreateNewRecord = (config.addCreateNewRecord==undefined || config.addCreateNewRecord==null) ? true:config.addCreateNewRecord;//default value will be true
    this.btnToolTip=(config.btnToolTip==undefined || config.btnToolTip==null)?"":config.btnToolTip;
    Wtf.form.FnComboBox.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.form.FnComboBox,Wtf.form.ComboBox,{
    addNewDisplay:WtfGlobal.getLocaleText("acc.rem.138"),
    addNoneRecord: false,       // Flag to add a "none selection" record.
//    addCreateNewRecord : false, // Flag to add a "Create New" record.
    noneRecordText:WtfGlobal.getLocaleText("acc.rem.111"),     // None record's display text
    noneRecordValue: "",        // None recods's value (id)
    disableToolTip: WtfGlobal.getLocaleText("acc.field.Thisrecordhasbeendeleted."), // Style for disable record
    disableStyle: "color:gray; text-decoration:line-through;", // Style for disable record
    mode: 'local',
    triggerAction: 'all',
    typeAhead: true,
    // private
    initValue: Wtf.form.ComboBox.prototype.initValue.createSequence(function() {
        /**  
         * @cfg displayValue
         * A display value to initialise this {@link Ext.form.ComboBox}
         * (only useful for ComboBoxes with remote Stores, and having valueField != displayField).
         */
        if (this.mode == 'remote' && !!this.valueField && this.valueField != this.displayField && this.displayValue) {
            if (this.forceSelection) {
                this.lastSelectionText = this.displayValue;
            }    
            var Record = this.store.reader.recordType;
            var  obj = {};
            obj[this.valueField] = this.valueID;
            obj[this.displayField] = this.displayValue;
            var rec = new Record(obj);
            this.store.insert(0, [rec]);
//            this.setRawValue(this.displayValue);
            this.setValue(this.valueID);
//            this.setRawValue(this.displayValue);
        }    
    }),
    initComponent:function(config){
        Wtf.form.FnComboBox.superclass.initComponent.call(this, config);
        this.addNewID=this.initial+this.store.id;
        this.addLastEntry(this.store);
        this.store.on('load',this.addLastEntry,this);
        this.on('beforeselect',this.callFunction, this);
//        this.listWidth = Wtf.account.companyAccountPref.accountsWithCode?500:this.listWidth;

        if(this.hirarchical){
            if(this.disableOnField){
                this.tpl=new Wtf.XTemplate('<tpl for="."><div class="x-combo-list-item" style="{[values.'+this.disableOnField+' == true ? "'+this.disableStyle+'" : "" ]}" Wtf:qtip="{[values.'+this.disableOnField+' == true ? "'+this.disableToolTip+'" : values.'+this.displayField+']}">{[this.getDots(values.level)]}{'+this.displayField+'}</div></tpl>',{
                    getDots:function(val){
                        var str="";
                        for(var i=0;i<val;i++)
                            str+="....";
                        return str;
                    }
                })
            } else {
                this.tpl=new Wtf.XTemplate('<tpl for="."><div class="x-combo-list-item" Wtf:qtip="{'+this.displayField+'}">{[this.getDots(values.level)]}{'+this.displayField+'}</div></tpl>',{
                    getDots:function(val){
                        var str="";
                        for(var i=0;i<val;i++)
                            str+="....";
                        return str;
                    }
                })
            }
        } else {
            if(this.disableOnField){
                this.tpl=new Wtf.XTemplate('<tpl for="."><div class="x-combo-list-item" style="{[values.'+this.disableOnField+' == true ? "'+this.disableStyle+'" : "" ]}" Wtf:qtip="{[values.'+this.disableOnField+' == true ? "'+this.disableToolTip+'" : values.'+this.displayField+']}">{'+this.displayField+'}</div></tpl>');
            } else {
                this.tpl=new Wtf.XTemplate('<tpl for="."><div class="x-combo-list-item" Wtf:qtip="{'+this.displayField+'}">{'+this.displayField+'}</div></tpl>');
            }
        }
    },

    setValForRemoteStore : function(valueID, displayValue, hasAccess, record) {
        if (this.mode == 'remote' && !!this.valueField) {
            if (this.forceSelection) {
                this.lastSelectionText = displayValue;
            }    
            var Record = this.store.reader.recordType;
            var  obj = {};
            obj[this.valueField] = valueID;
            obj[this.displayField] = displayValue;
            if(hasAccess!=undefined){
                obj['hasAccess'] = hasAccess;
            }
            if(record && record.data){
                for(var key in record.data){
                    obj[key]=record.data[key];
                } 
            }
            var rec = new Record(obj);
            this.store.insert(0, [rec]);
            this.setValue(valueID);
        }
    },
    setValForChildComboStore: function(valueID, displayValue, hasAccess, record) {
        if (!!this.valueField) {
            if (this.forceSelection) {
                this.lastSelectionText = displayValue;
            }
            this.store.removeAll();
            var Record = this.store.reader.recordType;
            var flag = true;
            for (var index = 0; index < 2; index++) {
                var obj = {};
                if (flag) {
                    obj[this.valueField] = "1234";
                    obj[this.displayField] = "None";
                } else {
                    obj[this.valueField] = valueID;
                    obj[this.displayField] = displayValue;
                }

                if (hasAccess != undefined) {
                    obj['hasAccess'] = hasAccess;
                }
                if (record && record.data) {
                    for (var key in record.data) {
                        obj[key] = record.data[key];
                    }
                }
                var rec = new Record(obj);
                this.store.insert(index, [rec]);
                flag = false;

            }

            this.setValue(valueID);
        }
    },
    onRender : function(ct, position){
        Wtf.form.FnComboBox.superclass.onRender.call(this, ct, position);
        if(this.addNewFn==undefined)return;
        if(this.hideAddButton!=undefined && this.hideAddButton==true){//hide the addNewFn button from editor box 
            if(!this.width){
                this.wrap.setWidth(this.el.getWidth());
            }
        }else{
            this.anButton = this.wrap.createChild({tag: "img", src: Wtf.BLANK_IMAGE_URL, cls:"combo-addnew", title:this.btnToolTip});
            this.initAddNewButton();
            if(!this.width){
                this.wrap.setWidth(this.el.getWidth()+this.anButton.getWidth());
            }
        }
    },

    initAddNewButton:function(){
        this.anButton.on("click", function(){
                if(this.disabled)return;
                if(this.isExpanded())
                    this.collapse();
                this.addNewFn();
            }, this, {preventDefault:true});
    },

    onResize : function(w, h){
        Wtf.form.FnComboBox.superclass.onResize.call(this, w, h);
        if(this.addNewFn==undefined)return;
        if(this.hideAddButton!=undefined && this.hideAddButton==true){
            if(typeof w == 'number'){
                this.el.setWidth(this.adjustWidth('input', w -this.trigger.getWidth()));
            }
            this.wrap.setWidth(this.el.getWidth()+this.trigger.getWidth());
        }else{
            if(typeof w == 'number'){
                this.el.setWidth(this.adjustWidth('input', w -this.trigger.getWidth()- this.anButton.getWidth()));
            }
            this.wrap.setWidth(this.el.getWidth()+this.trigger.getWidth()+this.anButton.getWidth());
        }
    },


    addLastEntry:function(s){
        var comboRec, rec;
        if(this.addNoneRecord){     // For no Selections, Add a record as "None"
            var nrecid=s.find(this.displayField,this.noneRecordText);
            if(nrecid==-1){
                comboRec=Wtf.data.Record.create(s.fields);
                rec=new comboRec({});
                s.insert(0,rec);
                rec.beginEdit();
                rec.set(this.valueField, this.noneRecordValue);
                rec.set(this.displayField, this.noneRecordText);
                rec.endEdit();
            }
        }
        if(this.addNewFn==undefined)return;
        var recid=s.find(this.valueField,this.addNewID);
        if(recid==-1 && this.valueField!='accid' && this.addCreateNewRecord){
            comboRec=Wtf.data.Record.create(s.fields);
            rec=new comboRec({});
            if(this.hideAddButton!=undefined && this.hideAddButton==true){//hide the addNewFn button from editor box 
                var storeCount=s.getCount();
                s.insert(storeCount,rec);//Add New Option Added at the last. 
            }else{
                s.insert(0,rec);
            }
            rec.beginEdit();
            if(this.extraFields!=undefined){
                for(var i=0;i<this.extraFields.length;i++){
                    rec.set(this.extraFields[i], "");
                }
            }
            rec.set(this.valueField, this.addNewID);
            rec.set(this.displayField, this.addNewDisplay);
            rec.endEdit();
        }
    },

    callFunction:function(c,r){
        if(r.data[this.valueField]==this.addNewID){
            this.collapse();
            this.addNewFn();
            return false;
        }
        if(this.disableOnField && r.data[this.disableOnField]==true){ //Don't select Disabled record.
            return false;
        }
    }
});


Wtf.reg('fncombo', Wtf.form.FnComboBox);

//Added date select event for date field

Wtf.form.FnDateField=function(config){
    
    Wtf.form.FnDateField.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.form.FnDateField,Wtf.form.DateField,{
    
    initComponent:function(config){
        Wtf.form.FnDateField.superclass.initComponent.call(this, config);
        this.addEvents({
            'dateselect':true
        });
    },
    
      menuListeners : {
        select: function(m, d){
            this.setValue(d);
            this.fireEvent("dateselect");
        },
        show : function(){
            this.onFocus();
        },
        hide : function(){
            this.focus.defer(10, this);
            var ml = this.menuListeners;
            this.menu.un("select", ml.select,  this);
            this.menu.un("show", ml.show,  this);
            this.menu.un("hide", ml.hide,  this);
        }
    }
});


Wtf.form.FnRefreshBtn=function(config){
    
    Wtf.form.FnRefreshBtn.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.form.FnRefreshBtn,Wtf.form.ComboBox,{
    initComponent:function(config){
        Wtf.form.FnRefreshBtn.superclass.initComponent.call(this, config);
    },
        
      onRender : function(ct, position){
        Wtf.form.FnRefreshBtn.superclass.onRender.call(this, ct, position);
        if(this.addNewFn==undefined)return;
        this.anButton = this.wrap.createChild({tag: "img", src: Wtf.BLANK_IMAGE_URL, cls:"accountingbase combo-loading"});
        this.initAddNewButton();
        if(!this.width){
            this.wrap.setWidth(this.el.getWidth()+this.anButton.getWidth());
        }    
    },

    initAddNewButton:function(){
        this.anButton.on("click", function(){
                if(this.disabled)return;
                this.addNewFn();
            }, this);

    }        

});


Wtf.form.TextFieldAddNewBtn=function(config){
    
    Wtf.form.TextFieldAddNewBtn.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.form.TextFieldAddNewBtn,Wtf.form.TextField,{
    initComponent:function(config){
        Wtf.form.TextFieldAddNewBtn.superclass.initComponent.call(this, config);
    },
    
    listeners: {
        render : function(c) {
            Wtf.QuickTips.register({
                target: c.getEl(),
                text: "<font style='word-break: break-all;word-wrap:break-word;'>"+this.toolTip+"</font>"
            });
        }
    },
      onRender : function(ct, position){
        Wtf.form.TextFieldAddNewBtn.superclass.onRender.call(this, ct, position);
        if(this.addNewFn==undefined)return;
        this.wrap = this.el.wrap({cls: "x-form-field-wrap"});
        this.anButton = this.wrap.createChild({tag: "img", src: Wtf.BLANK_IMAGE_URL, cls:"combo-addnew"});
        this.initAddNewButton();
        if(!this.width){
            this.wrap.setWidth(this.el.getWidth()+this.anButton.getWidth());
        }


//        this.anButton = ct.createChild({tag: "img", src: Wtf.BLANK_IMAGE_URL, cls:"combo-addnew"});
//        this.initAddNewButton();
//        if(!this.width){
//            ct.setWidth(this.el.getWidth()+this.anButton.getWidth());
//        }    
    },

    initAddNewButton:function(){
        this.anButton.on("click", function(){
                if(this.disabled)return;
            this.addNewFn();
        }, this);
    },
    setTooltip: function(tooltip) {
        Wtf.QuickTips.unregister({
            target: this.getEl()
        });
        Wtf.QuickTips.register({
            target: this.getEl(),
            text: "<font style='word-break: break-all;word-wrap:break-word;'>" + tooltip + "</font>"
        });
    }

});



Wtf.form.NumberFieldAddNewBtn=function(config){
    
    Wtf.form.NumberFieldAddNewBtn.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.form.NumberFieldAddNewBtn,Wtf.form.NumberField,{
    initComponent:function(config){
        Wtf.form.NumberFieldAddNewBtn.superclass.initComponent.call(this, config);
    },
        
    onRender : function(ct, position){
        Wtf.form.NumberFieldAddNewBtn.superclass.onRender.call(this, ct, position);
        if(this.addNewFn==undefined)return;
        this.anButton = ct.createChild({
            tag: "img", 
            src: Wtf.BLANK_IMAGE_URL, 
            cls:"combo-addnew"
        });
        this.initAddNewButton();
        if(!this.width){
            ct.setWidth(this.el.getWidth()+this.anButton.getWidth());
        }    
    },

    initAddNewButton:function(){
        this.anButton.on("click", function(){
            if(this.disabled)return;
            this.addNewFn();
        }, this);
    },        
    
    onResize : function(w, h){
        Wtf.form.NumberFieldAddNewBtn.superclass.onResize.call(this, w, h);
        if(this.addNewFn==undefined)return;
        if(typeof w == 'number'){
            this.el.setWidth(this.adjustWidth('input', w -24 - this.anButton.getWidth()));
        }
//        this.wrap.setWidth(this.el.getWidth()+24+this.anButton.getWidth());
    }

});

Wtf.ExDateFieldQtip=function(config){
    config.qtip = WtfGlobal.dateRenderer(config.value),
    config.listeners = {
        render: function(c){
            Wtf.QuickTips.register({
                target: c.getEl(),
                text: c.qtip
            });
        },
       change: function(c){
        Wtf.QuickTips.register({
            target: c.getEl(),
            text: c.el.dom.value
        });
    }
    }
    Wtf.ExDateFieldQtip.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.ExDateFieldQtip,Wtf.form.DateField,{
});

// register xtype 
Wtf.reg('exdatefieldqtip', Wtf.ExDateFieldQtip);