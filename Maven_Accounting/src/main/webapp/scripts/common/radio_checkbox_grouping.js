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
Wtf.form.readOnlyCmp = Wtf.extend(Wtf.form.TextField, {


    blankText : "Text",

    // private
    defaultType : 'textfield'
});

Wtf.reg('readOnlyCmp', Wtf.form.readOnlyCmp);


Wtf.form.CheckboxGroup = Wtf.extend(Wtf.form.Field, {


    columns : 'auto',

    vertical : false,

    allowBlank : true,

    blankText : WtfGlobal.getLocaleText("acc.field.Youmustselectatleastoneiteminthisgroup"),

    // private
    defaultType : 'checkbox',

    // private
    groupCls: 'x-form-check-group',

    // private
    onRender : function(ct, position){
        if(!this.el){
            var panelCfg = {
                cls: this.groupCls,
                layout: 'column',
                border: false,
                renderTo: ct
            };
            var colCfg = {
                defaultType: this.defaultType,
                layout: 'form',
                border: false,
                defaults: {
                    hideLabel: true,
                    anchor: '100%'
                }
            }

            if(this.items[0].items){

                // The container has standard ColumnLayout configs, so pass them in directly

                Wtf.apply(panelCfg, {
                    layoutConfig: {columns: this.items.length},
                    defaults: this.defaults,
                    items: this.items
                })
                for(var i=0, len=this.items.length; i<len; i++){
                    Wtf.applyIf(this.items[i], colCfg);
                };

            }else{

                // The container has field item configs, so we have to generate the column
                // panels first then move the items into the columns as needed.

                var numCols, cols = [];

                if(typeof this.columns == 'string'){ // 'auto' so create a col per item
                    this.columns = this.items.length;
                }
                if(this.columns instanceof Array == false ){
                    var cs = [];
                    for(var i=0; i<this.columns; i++){
                        cs.push((100/this.columns)*.01); // distribute by even %
                    }
                    this.columns = cs;
                }

                numCols = this.columns.length;

                // Generate the column configs with the correct width setting
                for(var i=0; i<numCols; i++){
                    var cc = Wtf.apply({items:[]}, colCfg);
                    cc[this.columns[i] <= 1 ? 'columnWidth' : 'width'] = this.columns[i];
                    if(this.defaults){
                        cc.defaults = Wtf.apply(cc.defaults || {}, this.defaults)
                    }
                    cols.push(cc);
                };

                // Distribute the original items into the columns
                if(this.vertical){
                    var rows = Math.ceil(this.items.length / numCols), ri = 0;
                    for(var i=0, len=this.items.length; i<len; i++){
                        if(i>0 && i%rows==0){
                            ri++;
                        }
                        if(this.items[i].fieldLabel){
                            this.items[i].hideLabel = false;
                        }
                        cols[ri].items.push(this.items[i]);
                    };
                }else{
                    for(var i=0, len=this.items.length; i<len; i++){
                        var ci = i % numCols;
                        if(this.items[i].fieldLabel){
                            this.items[i].hideLabel = false;
                        }
                        cols[ci].items.push(this.items[i]);
                    };
                }

                Wtf.apply(panelCfg, {
                    layoutConfig: {columns: numCols},
                    items: cols
                });
            }

            this.panel = new Wtf.Panel(panelCfg);
            this.el = this.panel.getEl();

            if(this.forId && this.itemCls){
                var l = this.el.up(this.itemCls).child('label', true);
                if(l){
                    l.setAttribute('htmlFor', this.forId);
                }
            }

            var fields = this.panel.findBy(function(c){
                return c.isFormField;
            }, this);

            this.items = new Wtf.util.MixedCollection();
            this.items.addAll(fields);
        }
        Wtf.form.CheckboxGroup.superclass.onRender.call(this, ct, position);
    },

    // private
    validateValue : function(value){
        if(!this.allowBlank){
            var blank = true;
            this.items.each(function(f){
                if(f.checked){
                    return blank = false;
                }
            }, this);
            if(blank){
                this.markInvalid(this.blankText);
                return false;
            }
        }
        return true;
    },

    // private
    onDisable : function(){
        this.items.each(function(item){
            item.disable();
        })
    },

    // private
    onEnable : function(){
        this.items.each(function(item){
            item.enable();
        })
    },

    // private
    onResize : function(w, h){
        this.panel.setSize(w, h);
        this.panel.doLayout();
    },

    // inherit docs from Field
    reset : function(){
        Wtf.form.CheckboxGroup.superclass.reset.call(this);
        this.items.each(function(c){
            if(c.reset){
                c.reset();
            }
        }, this);
    },



    initValue : Wtf.emptyFn,

    getValue : Wtf.emptyFn,

    getRawValue : Wtf.emptyFn,

    setValue : Wtf.emptyFn,

    setRawValue : Wtf.emptyFn

});

Wtf.reg('checkboxgroup', Wtf.form.CheckboxGroup);


Wtf.form.Radio = Wtf.extend(Wtf.form.Checkbox, {
    // private
    inputType: 'radio',
    // private
    baseCls: 'x-form-radio',


    getGroupValue : function(){
        var c = this.getParent().child('input[name='+this.el.dom.name+']:checked', true);
        return c ? c.value : null;
    },

    // private
    getParent : function(){
        return this.el.up('form') || Wtf.getBody();
    },

    // private
    toggleValue : function() {
        if(!this.checked){
            var els = this.getParent().select('input[name='+this.el.dom.name+']');
            els.each(function(el){
                if(el.dom.id == this.id){
                    this.setValue(true);
                }else{
                    Wtf.getCmp(el.dom.id).setValue(false);
                }
            }, this);
        }
    },


    setValue : function(v){
        if(typeof v=='boolean') {
            Wtf.form.Radio.superclass.setValue.call(this, v);
        }else{
            var r = this.getParent().child('input[name='+this.el.dom.name+'][value='+v+']', true);
            if(r && !r.checked){
                Wtf.getCmp(r.id).toggleValue();
            };
        }
    },


    markInvalid : Wtf.emptyFn,

    clearInvalid : Wtf.emptyFn

});
Wtf.reg('radio', Wtf.form.Radio);


Wtf.form.RadioGroup = Wtf.extend(Wtf.form.CheckboxGroup, {

    allowBlank : true,

    blankText : WtfGlobal.getLocaleText("acc.field.Youmustselectoneiteminthisgroup"),

    // private
    defaultType : 'radio',

    // private
    groupCls: 'x-form-radio-group'
});

Wtf.reg('radiogroup', Wtf.form.RadioGroup);
/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


