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
Wtf.apply(Wtf.DataView.prototype, {
    deselect: function(node, suppressEvent){
        if (this.isSelected(node)) {
            var node = this.getNode(node);
            this.selected.removeElement(node);
            if (this.last == node.viewIndex) {
                this.last = false;
            }
            Wtf.fly(node).removeClass(this.selectedClass);
            if (!suppressEvent) {
                this.fireEvent('selectionchange', this, this.selected.elements);
            }
        }
    }
});

/**
* @class Wtf.common.Select
* @extends Wtf.form.ComboBox
* A combobox control with support for multiSelect.
* @constructor
* Create a new Select.
* @param {Object} config Configuration options
*/
Wtf.common.Select = function(config){
    if (config.transform && typeof config.multiSelect == 'undefined') {
        var o = Wtf.getDom(config.transform);
        config.multiSelect = (Wtf.isIE ? o.getAttributeNode('multiple').specified : o.hasAttribute('multiple'));
    }
    config.hideTrigger2 = config.hideTrigger2 || config.hideTrigger;
    this.isCustomCombo = (config.isCustomCombo!=null && config.isCustomCombo!=undefined)?config.isCustomCombo:false;
    Wtf.common.Select.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.common.Select, Wtf.form.ComboBox, {
    /**
     * @cfg {Boolean} multiSelect Multiple selection is allowed (defaults to false)
     */
    multiSelect: false,
    /**
     * @cfg {Integer} minLength Minimum number of required items to be selected
     */
    minLength: 0,
    /**
     * @cfg {String} minLengthText Validation message displayed when minLength is not met.
     */
    minLengthText: WtfGlobal.getLocaleText("acc.field.Minimum0itemsrequired"),
    /**
     * @cfg {Integer} maxLength Maximum number of allowed items to be selected
     */
    maxLength: Number.MAX_VALUE,
    /**
     * @cfg {String} maxLengthText Validation message displayed when maxLength is not met.
     */
    maxLengthText: WtfGlobal.getLocaleText("acc.field.Maximum0itemsallowed"),
    /**
     * @cfg {Boolean} clearTrigger Show the clear button (defaults to true)
     */
    clearTrigger: true,
    /**
     * @cfg {Boolean} history Add selected value to the top of the list (defaults to false)
     */
    history: false,
    /**
     * @cfg {Integer} historyMaxLength Number of entered values to remember. 0 means remember all (defaults to 0)
     */
    historyMaxLength: 0,
    /**
     * @cfg {String} separator Separator to use for the values passed to setValue (defaults to comma)
     */
    separator: ',',
    /**
     * @cfg {String} displaySeparator Separator to use for displaying the values (defaults to comma)
     */
    displaySeparator: ',',

    // private
    valueArray: [],

    // private
    rawValueArray: [],
    /*
     *max height of drop down list
     */
//    maxHeight : 100,

    initComponent: function(){
        //from twintrigger
        this.addEvents({
            "beforeclearval": true,
            "unselect": true,
            "clearval" : true
        });

        this.triggerConfig = {
            tag: 'span',
            style: Wtf.isIE ? "margin-left: 5px;" :"",
            cls: 'x-form-twin-triggers',
            cn: [{tag: "img", src: Wtf.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger1Class},
            {tag: "img", src: Wtf.BLANK_IMAGE_URL, cls: "x-form-trigger " + this.trigger2Class}
                ]
        };
        
        if(this.addCreateOpt){
            this.triggerConfig.cn.push({tag: "img", src: Wtf.BLANK_IMAGE_URL, cls: "combo-addnewExt " });
        }else if(this.addCreateOpt!=undefined && !this.addCreateOpt){
            this.triggerConfig.cn.push({tag: "img", src: Wtf.BLANK_IMAGE_URL, cls: "combo-addnewBtn " });
        }
        
        Wtf.common.Select.superclass.initComponent.call(this);
        
        var extrafield='';
        var accountCodeField='';
        var length=0;
        if(this.extraFields){
            length=this.extraFields.length;
        }
        
        if (!this.isCustomCombo) {
            this.listWidth = (length == 3) ? 700 : (length == 2) ? 450 : 300;
        }
        for (var i=0;i<length;i++){
             if(this.extraFields[i] == 'acccode' || this.extraFields[i] == 'code'){
                this.listWidth=this.listWidth+250;
                accountCodeField+='<td align="left" Wtf:qtip="{'+this.extraFields[i]+'}" width="'+100/(length+4)+'%"td>{'+this.extraFields[i]+':ellipsis(15)}</td>';          
            }else if(this.extraFields[i] == 'groupname'){
                this.listWidth=this.listWidth+100;
                extrafield+='<td align="left" width="'+100/(length)+'%"td>{'+this.extraFields[i]+':ellipsis(50)}</td>';          
            }else if(this.isParentCombo!=undefined && this.isParentCombo && this.extraFields[i] == 'pid'){ // used for parent store
                extrafield += '<td align="left" width="' + 100 / (length) + '%"td>{' + this.extraFields[i] + ':ellipsis(25)}</td>';       
            }else if(this.isParentCombo!=undefined && this.isParentCombo && this.extraFields[i] == 'parentname'){
                extrafield += '<td align="left" width="' + 100 / (length) + '%"td>{' + this.extraFields[i] + ':ellipsis(35)}</td>';  
            } else if(this.extraFields[i] == 'pid' ||this.extraFields[i] == 'ccid'){// for productid and product type and costcenter ID  //|| this.extraFields[i] == 'type' ERP-16498
                extrafield+='<td align="left" width="'+100/(length+4)+'%"td>{'+this.extraFields[i]+':ellipsis(15)}</td>';          
            } else if(this.extraFields[i] == 'type'){// width is 100/5=20 so ellipsis are not grater than 20
                extrafield+='<td align="left" width="'+100/(length+2)+'%"td>{'+this.extraFields[i]+':ellipsis(20)}</td>';          
            } else if(this.extraFields[i] == 'productname' ){// width is 100/3=33.33 so ellipsis are not grater than 33 
                extrafield+='<td align="left" width="'+100/(length)+'%"td>{'+this.extraFields[i]+':ellipsis(33)}</td>';          
            } else if(this.extraFields[i] == 'description'){// for product name Cost center Desctiption
                extrafield+='<td align="left" width="'+100/(length+1)+'%"td>{'+this.extraFields[i]+':ellipsis(35)}</td>';          
            } else if(this.extraFields[i] == 'machineid'){// for Machine Code
                extrafield+='<td align="left" width="'+100/(length+1)+'%"td>{'+this.extraFields[i]+':ellipsis(20)}</td>';          
            }else if(this.extraFields[i] == 'empcode'){// for Labour Code
                extrafield+='<td align="left" width="'+100/(length+1)+'%"td>{'+this.extraFields[i]+':ellipsis(20)}</td>';          
            }else if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.extraFields[i] == 'mvatdescription'){
                extrafield+='<td align="left" width="'+100/(length+1)+'%"td>{'+this.extraFields[i]+':ellipsis(90)}</td>';          
            }else if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.extraFields[i] == 'mvatannexurecode'){
                extrafield+='<td align="right" width="'+10+'%"td>{'+this.extraFields[i]+':ellipsis(10)}</td>';
            }else if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.extraFields[i] == 'namepurchasetype'){
                extrafield+='<td align="left" width="'+90+'%"td>{'+this.extraFields[i]+':ellipsis(90)}</td>';          
            }else if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.extraFields[i] == 'codepurchasetype'){
                extrafield+='<td align="right" width="'+10+'%"td>{'+this.extraFields[i]+':ellipsis(90)}</td>';     
            }else if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.extraFields[i] == 'name'){
                extrafield+='<td align="left" width="'+100/(length+1)+'%"td>{'+this.extraFields[i]+':ellipsis(90)}</td>';          
            }else if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.extraFields[i] == 'salespersoncode'){
                extrafield+='<td align="right" width="'+10+'%"td>{'+this.extraFields[i]+':ellipsis(10)}</td>';          
            }else{
                extrafield+='<td align="left" width="'+100/(length+3)+'%"td>{'+this.extraFields[i]+':ellipsis(20)}</td>';            
            }
        }
        if (this.isAdvanceSearchCombo) {
            this.tpl = new Wtf.XTemplate(
                    '<tpl for="."><div Wtf:qtip="{[values.id === "" ? "You cannot select value" : values.' + this.displayField + ']}" class="{[values.id === "" ? "x-combo-list-item disabled-record" : "x-combo-list-item"]}" ><table width="100%"><tr>' + accountCodeField + '<td align="left" width="' + 100 / (length) + '%">{[this.getDots(values.level)]}{' + this.displayField + ':ellipsis(50)}</td>' + extrafield + '</tr></table></div></tpl>', {
                getDots: function(val) {
                    var str = "";
                    for (var i = 0; i < val; i++)
                        str += "....";
                    return str;
                }
            })
        } else  if(this.isVendor || this.isCustomer || this.activated || this.isProductCombo||this.isTerm){
            this.tpl=new Wtf.XTemplate(
            '<tpl for="."><div Wtf:qtip="{[values.hasAccess === false ? ('+this.isVendor+'?"You cannot select deactivated Vendor":'+this.isTerm+'?"You cannot select deactivated Term":'+this.isCustomer+'?"You cannot select deactivated Customer":'+this.activated+'?"You cannot select deactivated salesperson" : '+this.isProductCombo+'?"You cannot select deactivated Products":"You cannot select deactivated Accounts") : values.'+this.displayField+']}" class="{[values.hasAccess === false ? "x-combo-list-item disabled-record" : "x-combo-list-item"]}" ><table width="100%"><tr>'+accountCodeField+'<td align="left" width="'+100/(length)+'%">{[this.getDots(values.level)]}{'+this.displayField+':ellipsis(50)}</td>'+extrafield+'</tr></table></div></tpl>',{
            getDots:function(val){
                    var str="";
                    for(var i=0;i<val;i++)
                        str+="....";
                    return str;
                }
            })
        }else {
            this.tpl = new Wtf.XTemplate(
                    '<tpl for="."><div class="x-combo-list-item" Wtf:qtip="{' + this.displayField + '}"><table width="100%"><tr>' + accountCodeField + '<td align="left" width="' + 100 / (length + 1) + '%">{[this.getDots(values.level)]}{' + this.displayField + ':ellipsis(35)}</td>' + extrafield + '</tr></table></div></tpl>', {
                getDots: function(val) {
                    var str = "";
                    for (var i = 0; i < val; i++)
                        str += "....";
                    return str;
                }
            });
        }
        if (this.multiSelect) {
            this.typeAhead = true;
            /*
             * Flag is used make values Editable/Non Editable in multiselect drop down 
             * 
             */
            if (this.isNotEditable) {
                this.editable = false;
            } else {
                this.editable = true;
            }
            //this.lastQuery = this.allQuery;
            this.triggerAction = 'all';
            this.selectOnFocus = false;
        }
        if (this.history) {
            this.forceSelection = false;
        }
        if (this.value) {
            this.setValue(this.value);
        }
    },

    hideTrigger1: true,

    getTrigger: Wtf.form.TwinTriggerField.prototype.getTrigger,

    initTrigger: Wtf.form.TwinTriggerField.prototype.initTrigger,

    trigger1Class: 'x-form-clear-trigger',
    trigger2Class: 'x-form-arrow-trigger',

    onTrigger2Click: function(){
        if(this.store){
            this.store.clearFilter();
        }
        this.onTriggerClick();
    },

    onTrigger1Click: function(){
        this.clearValue();
    },

    initList: function(){
        if (!this.list) {
            var cls = 'x-combo-list';

            this.list = new Wtf.Layer({
                shadow: this.shadow,
                cls:[cls,this.listClass] .join(' '),
                constrain: false
            });

            var lw = this.listWidth || Math.max(this.wrap.getWidth(), this.minListWidth);
            this.list.setWidth(lw);
            this.list.swallowEvent('mousewheel');
            this.assetHeight = 0;

            if (this.title) {
                this.header = this.list.createChild({
                    cls: cls + '-hd',
                    html: this.title
                });
                this.assetHeight += this.header.getHeight();
            }

            this.innerList = this.list.createChild({
                cls: cls + '-inner'
            });
            this.innerList.on('mouseover', this.onViewOver, this);
            this.innerList.on('mousemove', this.onViewMove, this);
            this.innerList.setWidth(lw - this.list.getFrameWidth('lr'))

            if (this.pageSize) {
                this.footer = this.list.createChild({
                    cls: cls + '-ft'
                });
                this.pageTb = new Wtf.PagingToolbar({
                    store: this.store,
                    pageSize: this.pageSize,
                    renderTo: this.footer
                });
                
                if(this.pageTb.loading) {
                    this.pageTb.loading.on('click', this.onPagingToolbarRefreshBtnClick, this);
                }
                
                this.assetHeight += this.footer.getHeight();
            }

            if (!this.tpl) {
                this.tpl = new Wtf.XTemplate('<tpl for="."><div class="' + cls + '-item" style="color:{status}" wtf:qtip="{tooltip}">{[this.f(values)]}</div></tpl>',{
                    f: function(val){
                        var dVal = val[this.scope.displayField];
                        if(typeof this.scope.renderer == "function")
                            dVal = this.scope.renderer(dVal, val);
                        return dVal;
                    },
                    scope: this
                });
            }

            /**
             * The {@link Wtf.DataView DataView} used to display the ComboBox's options.
             * @type Wtf.DataView
             */
            this.view = new Wtf.DataView({
                applyTo: this.innerList,
                tpl: this.tpl,
                singleSelect: true,
                scope: this,
                multiSelect: this.multiSelect,
                simpleSelect: true,
                overClass: cls + '-cursor',
                selectedClass: this.selectedClass,
                itemSelector: this.itemSelector || '.' + cls + '-item'
            });

            this.view.on('click', this.onViewClick, this);

            this.view.on('beforeClick', this.onViewBeforeClick, this);
            
            this.on('unselect', this.onUnSelect, this);

            this.bindStore(this.store, true);


            if (this.valueArray.length) {
                this.selectByValue(this.valueArray);
            }

            if (this.resizable) {
                this.resizer = new Wtf.Resizable(this.list, {
                    pinned: true,
                    handles: 'se'
                });
                this.resizer.on('resize', function(r, w, h){
                    this.maxHeight = h - this.handleHeight - this.list.getFrameWidth('tb') - this.assetHeight;
                    this.listWidth = w;
                    this.innerList.setWidth(w - this.list.getFrameWidth('lr'));
                    this.restrictHeight();
                }, this);
                this[this.pageSize ? 'footer' : 'innerList'].setStyle('margin-bottom', this.handleHeight + 'px');
            }
        }
    },

    // private
    initEvents: function(){
        Wtf.form.ComboBox.superclass.initEvents.call(this);

        this.keyNav = new Wtf.KeyNav(this.el, {
            "up": function(e){
                this.inKeyMode = true;
                this.hoverPrev();
            },

            "down": function(e){
                if (!this.isExpanded()) {
                    this.onTriggerClick();
                }
                else {
                    this.inKeyMode = true;
                    this.hoverNext();
                }
            },

            "enter": function(e){
                if (this.isExpanded()) {
                    this.inKeyMode = true;
                    var hoveredIndex = this.view.indexOf(this.view.lastItem);
                    this.onViewBeforeClick(this.view, hoveredIndex, this.view.getNode(hoveredIndex), e);
                    this.onViewClick(this.view, hoveredIndex, this.view.getNode(hoveredIndex), e);
                }
                else {
                    this.onSingleBlur();
                }
                return true;
            },

            "esc": function(e){
                this.collapse();
            },

            "tab": function(e){
                this.collapse();
                return true;
            },

            "home": function(e){
                this.hoverFirst();
                return false;
            },

            "end": function(e){
                this.hoverLast();
                return false;
            },

            scope: this,

            doRelay: function(foo, bar, hname){
                if (hname == 'down' || this.scope.isExpanded()) {
                    return Wtf.KeyNav.prototype.doRelay.apply(this, arguments);
                }

                if (hname == 'enter' || this.scope.isExpanded()) {
                    return Wtf.KeyNav.prototype.doRelay.apply(this, arguments);
                }

                return true;
            },

            forceKeyDown: true
        });
        this.queryDelay = Math.max(this.queryDelay || 10, this.mode == 'local' ? 10 : 250);
        this.dqTask = new Wtf.util.DelayedTask(this.initQuery, this);
        if (this.typeAhead) {
            this.taTask = new Wtf.util.DelayedTask(this.onTypeAhead, this);
        }
        if (this.editable !== false) {
            this.el.on("keyup", this.onKeyUp, this);
        }

        if (!this.multiSelect) {
            if (this.forceSelection) {
                this.on('blur', this.doForce, this);
            }
            this.on('focus', this.onSingleFocus, this);
            this.on('blur', this.onSingleBlur, this);
        }
        this.on('change', this.onChange, this);
        
        if (this.addCreateOpt) {
            var ts = this.trigger.select('.combo-addnewExt', true);
            ts.each(function(t, all, index) {
                var Index = index + 1;
                t.on("click", this['onExtTrigger' + Index + 'Click'], this, {preventDefault: true});
            }, this);
        }else if(this.addCreateOpt!=undefined && !this.addCreateOpt){
            var ts = this.trigger.select('.combo-addnewBtn', true);
            ts.each(function(t, all, index) {
                var Index = index + 1;
                t.on("click", this['onExtTrigger' + Index + 'Click'], this, {preventDefault: true});
            }, this);
    }
        
        this.on('blur', function (combo) {
            if (combo && combo.el) {
                combo.el.blur();
            }
        }, this);
        
    },
//    doQuery : function(q, forceAll){      //This function affecting the Advance Search Functionality. This used at single place only. Dipak Dorkar handles this case separatly.
//            q = Wtf.isEmpty(q) ? '' : q;
//            var qe = {
//                query: q,
//                forceAll: forceAll,
//                combo: this,
//                cancel:false
//            };
//            if(this.fireEvent('beforequery', qe)===false || qe.cancel){
//                return false;
//            }
//            q = qe.query;
//            forceAll = qe.forceAll;
//            if (forceAll === true || (q.length >= this.minChars)) {
//            if (q.trim() == "") {
//                this.selectedIndex = -1;
//                this.store.clearFilter();
//                this.store.baseParams[this.queryParam] = q;
//                if (this.mode == 'remote') {             // If store is remote, then only reloaded
//                    this.store.load({
//                        params: this.getParams(q)
//                    });
//                    }
//                this.onLoad();
//            } else {
//                if (this.lastQuery !== q) {
//                    this.lastQuery = q;
//                    if (this.mode == 'local') {
//                        this.selectedIndex = -1;
//                        if (forceAll) {
//                            this.store.clearFilter();
//                        }
//                        this.onLoad();
//                    } else {
//                        this.store.baseParams[this.queryParam] = q;
//                        this.store.load({
//                            params: this.getParams(q)
//                        });
//                        this.expand();
//                    }
//                } else {
//                    this.selectedIndex = -1;
//                    this.onLoad();
//                }
//            }
//            }
//    },
    setValForRemoteStore : function(valueID, displayValue) {
        if (this.mode == 'remote' && !!this.valueField) {
            if (this.forceSelection) {
                this.lastSelectionText = displayValue;
            }    
            var valueArr=[];
            valueArr=valueID.split(',');
            var displayValueArr=[];
            displayValueArr=displayValue.split(',');
            var Record = this.store.reader.recordType;
            for(var i=0;i<valueArr.length;i++){
                var  obj = {};
                obj[this.valueField] = valueArr[i];
                obj[this.displayField] = displayValueArr[i];
                var rec = new Record(obj);
                this.store.insert(i, [rec]);
            }
            this.setValue(valueID);
        }    
    },
    onExtTrigger1Click: function() {
        if (this.disabled)
            return;
        if (this.isExpanded())
            this.collapse();
        this.addNewFn();
    },
    
    onKeyUp: function() {
        var me = this,
        displayField = me.displayField,
        inputElDom = me.getEl().dom,
        valueStore = me.valueStore,
      //  boundList = me.getPicker(),
        record, newValue, len, selStart;

        if (me.filterPickList) {
            var fn = this.createFilterFn(displayField, inputElDom.value);
            record = me.store.findBy(function(rec) {
                return ((valueStore.indexOfId(rec.getId()) === -1) && fn(rec));
            });
            record = (record === -1) ? false : me.store.getAt(record);
        } else {
            var filterData = inputElDom.value.split(",");
            var data = filterData[filterData.length-1];
            data = new RegExp(data, 'i');
            if(this.extraComparisionField != null && this.extraComparisionField != undefined && this.extraComparisionField != ''){
                record = me.store.filterBy(function(rec){
                    var returnFlag = false;
                    var reg = data;//  var reg = new RegExp("^"+this.getRawValue()+"+", "gi"); //changed to Start from beging to any match
                    if (this.isProductCombo && Wtf.account.companyAccountPref.productsearchingflag == 0) {
                        reg = new RegExp("^" + this.getRawValue() + "+", "gi"); //changed to Start from beging to any match
                    }//var reg = new RegExp("^"+this.getRawValue()+"+", "gi");
                    if(rec.get(this.extraComparisionField)!= null && rec.get(this.extraComparisionField) != undefined &&(rec.get(this.extraComparisionField).match(reg) || rec.get(this.displayField).match(reg))){
                        returnFlag = true;
                    }
                    return returnFlag;
                },this);
            }else{
                record = me.store.filter(displayField, data);
            }
        }

        if (record) {
            newValue = record.get(displayField);
            len = newValue.length;
            selStart = inputElDom.value.length;
           
            if (selStart !== 0 && selStart !== len) {
                inputElDom.value = newValue;
                me.selectText(selStart, newValue.length);
            }
        }
    },
    
    // ability to delete value with keyboard
    doForce: function(){
        if (this.el.dom.value.length > 0) {
            if (this.el.dom.value == this.emptyText) {
                this.clearValue();
            }
            else
            if (!this.multiSelect) {
                this.el.dom.value = this.lastSelectionText === undefined ? '' : this.lastSelectionText;
                this.applyEmptyText();
            }
        }
    },


    /* listeners */
    // private
    onLoad: function(){
        if (!this.hasFocus) {
            return;
        }
        if (this.store.getCount() > 0) {
            this.expand();
            this.restrictHeight();
            if (this.lastQuery == this.allQuery) {
                if (this.editable) {
                    this.el.dom.select();
                }

                this.selectByValue(this.value, true);
            /*if(!this.selectByValue(this.value, true)){
                 this.select(0, true);
                 }*/
            }
            else {
                this.selectNext();
                if (this.typeAhead && this.lastKey != Wtf.EventObject.BACKSPACE && this.lastKey != Wtf.EventObject.DELETE) {
                    this.taTask.delay(this.typeAheadDelay);
                }
            }
        }
        else {
            this.onEmptyResults();
        }
    //this.el.focus();
    },

    // private
    onSelect: function(record, index){
        if (this.fireEvent('beforeselect', this, record, index) !== false) {
            this.addValue(record.data[this.valueField || this.displayField]);
            this.fireEvent('select', this, record, index);
            if (!this.multiSelect) {
                this.collapse();
            }
        }
    },

    // private
    onSingleFocus: function(){
        this.oldValue = this.getRawValue();
    },

    // private
    onSingleBlur: function(){
        var r = this.findRecord(this.displayField, this.getRawValue());
        if (r) {
            this.select(this.store.indexOf(r));
            return;
        }
        if (String(this.oldValue) != String(this.getRawValue())) {
            this.setValue(this.getRawValue());
            this.fireEvent('change', this, this.oldValue, this.getRawValue());
        }
        this.oldValue = String(this.getRawValue());
    },

    // private
    onChange: function(){
        if (!this.clearTrigger) {
            return;
        }
        var inputTagWidth;
        /*
         *Calculate current width of input tag of combo box
         */
        if(this.el){
            inputTagWidth = this.el.dom.style.width;
            try{
                inputTagWidth = parseInt(inputTagWidth.substring(0,inputTagWidth.indexOf("px")));
            }catch(e){
                inputTagWidth = this.el.dom.clientWidth;
            }
        }
        /*
         *If combo have some value then show remove all trigger and adjust width of input tag.
         */
        if (this.getValue() != '' ) {
            if(!this.triggers[0].triggerShown){
                this.triggers[0].show();
                inputTagWidth -= 17;
                this.triggers[0].triggerShown = true;
            }
        }
        else if(this.triggers[0].triggerShown){
            /*
             *If combo have some value then hide remove all trigger and adjust width of input tag.
             */
            this.triggers[0].hide();
            inputTagWidth += 17;
            this.triggers[0].triggerShown = false;
        }
        /*
         *If combo box is not visible (that is it will be in tab which is not active) 
         *then adjust width of input tag of select combo.
         */
        if(this.el.dom.clientWidth == undefined || this.el.dom.clientWidth == 0){
            this.el.dom.style.width = inputTagWidth + "px";
        }
    },

    /* list/view functions AND listeners */
    collapse: function(){
        this.hoverOut();
        Wtf.common.Select.superclass.collapse.call(this);
    },

    expand: function(){
        Wtf.common.Select.superclass.expand.call(this);
        this.hoverFirst();
    },

    // private
    onViewOver: function(e, t){
        if (this.inKeyMode) { // prevent key nav and mouse over conflicts
            return;
        }

    /*var item = this.view.findItemFromChild(t);
         if(item){
         var index = this.view.indexOf(item);
         this.select(index, false);
         }*/
    },

    // private
    onViewBeforeClick: function(vw, index, node, e){
        this.preClickSelections = this.view.getSelectedIndexes();
    },

    // private
    onViewClick: function(vw, index, node, e){
        if (typeof index != 'undefined') {
            var arrayIndex = this.preClickSelections.indexOf(index);
            if (arrayIndex != -1 && this.multiSelect) {
                this.removeValue(this.store.getAt(index).data[this.valueField || this.displayField]);
                if (this.inKeyMode) {
                    this.view.deselect(index, true);
                }
                this.fireEvent("unselect", this, this.view.store.getAt(index),index);
                this.hover(index, true);
            }
            else {
                var r = this.store.getAt(index);
                if (r) {
                    if (this.inKeyMode) {
                        this.view.select(index, true);
                    }
                    this.onSelect(r, index);
                    this.hover(index, true);
                }
            }
        }

        // from the old doFocus argument; don't really know its use
        if (vw !== false) {
            this.el.focus();
        }
    },

    /* value functions */
    /**
     * Add a value if this is a multi select
     * @param {String} value The value to match
     */
    addValue: function(v){
        if (!this.multiSelect) {
            this.setValue(v);
            return;
        }
        if (v instanceof Array) {
            v = v[0];
        }
        v = String(v);
        if (this.valueArray.indexOf(v) == -1) {
            var text = v;
            var r = this.findRecord(this.valueField || displayField, v);
            if (r) {
                text = r.data[this.displayField];
                if (this.view) {
                    this.select(this.store.indexOf(r));
                }
            }
            else
            if (this.forceSelection) {
                return;
            }
            var result = Wtf.apply([], this.valueArray);
            result.push(v);
            var resultRaw = Wtf.apply([], this.rawValueArray);
            resultRaw.push(text);
            v = result.join(this.separator || ',');
            text = resultRaw.join(this.displaySeparator || this.separator || ',');
            this.commonChangeValue(v, text, result, resultRaw);
        }
    },

    /**
     * Remove a value
     * @param {String} value The value to match
     */
    removeValue: function(v){
        if (v instanceof Array) {
            v = v[0];
        }
        v = String(v);
        if (this.valueArray.indexOf(v) != -1) {
            var text = v;
            var r = this.findRecord(this.valueField || displayField, v);
            if (r) {
                text = r.data[this.displayField];
                if (this.view) {
                    this.deselect(this.store.indexOf(r));
                }
            }
            else
            if (this.forceSelection) {
                return;
            }
            var result = Wtf.apply([], this.valueArray);
            result.remove(v);
            var resultRaw = Wtf.apply([], this.rawValueArray);
            resultRaw.remove(text);
            v = result.join(this.separator || ',');
            text = resultRaw.join(this.displaySeparator || this.separator || ',');
            this.commonChangeValue(v, text, result, resultRaw);
        }
    },

    /**
     * Sets the specified value for the field. The value can be an Array or a String (optionally with separating commas)
     * If the value finds a match, the corresponding record text will be displayed in the field.
     * @param {Mixed} value The value to match
     */
    setValue: function(v){
        var result = [], resultRaw = [];
        if (!(v instanceof Array)) {
            if (this.separator && this.separator !== true) {
                v = v.split(String(this.separator));
            }
            else {
                v = [v];
            }
        }
        else
        if (!this.multiSelect) {
            v = v.slice(0, 1);
        }
        for (var i = 0, len = v.length; i < len; i++) {
            var value = v[i];
            var text = value;
            if (this.valueField) {
                var r = this.findRecord(this.valueField || this.displayField, value);
                if (r) {
                    text = r.data[this.displayField];
                }
                else
                if (this.forceSelection) {
                    continue;
                }
            }
            result.push(value);
            resultRaw.push(text);
        }
        v = result.join(this.separator || ',');
        text = resultRaw.join(this.displaySeparator || this.separator || ',');

        this.commonChangeValue(v, text, result, resultRaw);

        if (this.history && !this.multiSelect && this.mode == 'local') {
            this.addHistory(this.valueField ? this.getValue() : this.getRawValue());
        }
        if (this.view) {
            this.view.clearSelections();
            this.selectByValue(this.valueArray);
        }
    },

    // private
    commonChangeValue: function(v, text, result, resultRaw){
        this.lastSelectionText = text;
        this.valueArray = result;
        this.rawValueArray = resultRaw;
        if (this.hiddenField) {
            this.hiddenField.value = v;
        }
        Wtf.form.ComboBox.superclass.setValue.call(this, text);
        this.value = v;

        if (this.oldValueArray != this.valueArray) {
            this.fireEvent('change', this, this.oldValueArray, this.valueArray);
        }
        this.oldValueArray = Wtf.apply([], this.valueArray);
    },

    validateValue: function(value){
        if (!Wtf.common.Select.superclass.validateValue.call(this, value)) {
            return false;
        }
        if (this.valueArray.length < this.minLength) {
            this.markInvalid(String.format(this.minLengthText, this.minLength));
            return false;
        }
        if (this.valueArray.length > this.maxLength) {
            this.markInvalid(String.format(this.maxLengthText, this.maxLength));
            return false;
        }
        return true;
    },

    clearValue: function(){
        if(this.fireEvent("beforeclearval", this) !== false){
            this.commonChangeValue('', '', [], []);
            if (this.view) {
                this.view.clearSelections();
            }
            Wtf.common.Select.superclass.clearValue.call(this);
            this.fireEvent("clearval",this);
            
            if (this.isExpanded() && this.el && this.el.dom) {
                this.el.dom.value = "";
                this.el.removeClass(this.emptyClass);
            }
        }
    },

    reset: function(){
        if (this.view) {
            this.view.clearSelections();
        }
        Wtf.common.Select.superclass.reset.call(this);
    },

    getValue: function(asArray){
        if (asArray) {
            return typeof this.valueArray != 'undefined' ? this.valueArray : [];
        }
        return Wtf.common.Select.superclass.getValue.call(this);
    },

    getRawValue: function(asArray){
        if (asArray) {
            return typeof this.rawValueArray != 'undefined' ? this.rawValueArray : [];
        }
        return Wtf.common.Select.superclass.getRawValue.call(this);
    },



    /* selection functions */
    select: function(index, scrollIntoView){
        this.selectedIndex = index;
        if (!this.view) {
            return;
        }
        this.view.select(index, this.multiSelect);
        if (scrollIntoView !== false) {
            var el = this.view.getNode(index);
            if (el) {
                this.innerList.scrollChildIntoView(el, false);
            }
        }
    },

    deselect: function(index, scrollIntoView){
        this.selectedIndex = index;
        this.view.deselect(index, this.multiSelect);
        if (scrollIntoView !== false) {
            var el = this.view.getNode(index);
            if (el) {
                this.innerList.scrollChildIntoView(el, false);
            }
        }
    },

    selectByValue: function(v, scrollIntoView){
        this.hoverOut();
        if (v !== undefined && v !== null) {
            if (!(v instanceof Array)) {
                v = [v];
            }
            var result = [];
            for (var i = 0, len = v.length; i < len; i++) {
                var value = v[i];
                var r = this.findRecord(this.valueField || this.displayField, value);
                if (r) {
                    this.select(this.store.indexOf(r), scrollIntoView);
                    result.push(value);
                }
            }
            return result.join(',');
        }
        return false;
    },

    // private
    selectFirst: function(){
        var ct = this.store.getCount();
        if (ct > 0) {
            this.select(0);
        }
    },

    // private
    selectLast: function(){
        var ct = this.store.getCount();
        if (ct > 0) {
            this.select(ct);
        }
    },



    /* hover functions */
    /**
     * Hover an item in the dropdown list by its numeric index in the list.
     * @param {Number} index The zero-based index of the list item to select
     * @param {Boolean} scrollIntoView False to prevent the dropdown list from autoscrolling to display the
     * hovered item if it is not currently in view (defaults to true)
     */
    hover: function(index, scrollIntoView){
        if (!this.view) {
            return;
        }
        this.hoverOut();
        var node = this.view.getNode(index);
        this.view.lastItem = node;
        Wtf.fly(node).addClass(this.view.overClass);
        if (scrollIntoView !== false) {
            var el = this.view.getNode(index);
            if (el) {
                this.innerList.scrollChildIntoView(el, false);
            }
        }
    },

    hoverOut: function(){
        if (!this.view) {
            return;
        }
        if (this.view.lastItem) {
            Wtf.fly(this.view.lastItem).removeClass(this.view.overClass);
            delete this.view.lastItem;
        }
    },

    // private
    hoverNext: function(){
        if (!this.view) {
            return;
        }
        var ct = this.store.getCount();
        if (ct > 0) {
            if (!this.view.lastItem) {
                this.hover(0);
            }
            else {
                var hoveredIndex = this.view.indexOf(this.view.lastItem);
                if (hoveredIndex < ct - 1) {
                    this.hover(hoveredIndex + 1);
                }
            }
        }
    },

    // private
    hoverPrev: function(){
        if (!this.view) {
            return;
        }
        var ct = this.store.getCount();
        if (ct > 0) {
            if (!this.view.lastItem) {
                this.hover(0);
            }
            else {
                var hoveredIndex = this.view.indexOf(this.view.lastItem);
                if (hoveredIndex != 0) {
                    this.hover(hoveredIndex - 1);
                }
            }
        }
    },

    // private
    hoverFirst: function(){
        var ct = this.store.getCount();
        if (ct > 0) {
            this.hover(0);
        }
    },

    // private
    hoverLast: function(){
        var ct = this.store.getCount();
        if (ct > 0) {
            this.hover(ct);
        }
    },



    /* history functions */

    addHistory: function(value){
        if (!value.length) {
            return;
        }
        var r = this.findRecord(this.valueField || this.displayField, value);
        if (r) {
            this.store.remove(r);
        }
        else {
            //var o = this.store.reader.readRecords([[value]]);
            //r = o.records[0];
            var o = {};
            if (this.valueField) {
                o[this.valueField] = value;
            }
            o[this.displayField] = value;
            r = new this.store.reader.recordType(o);
        }
        this.store.clearFilter();
        this.store.insert(0, r);
        this.pruneHistory();
    },

    // private
    pruneHistory: function(){
        if (this.historyMaxLength == 0) {
            return;
        }
        if (this.store.getCount() > this.historyMaxLength) {
            var overflow = this.store.getRange(this.historyMaxLength, this.store.getCount());
            for (var i = 0, len = overflow.length; i < len; i++) {
                this.store.remove(overflow[i]);
            }
        }
    },
    
    onUnSelect: function (combo, record, index) {
        if (this.getValue() == "" || this.getValue() == undefined) {
            this.el.dom.value = "";
            this.el.removeClass(this.emptyClass);
        }
    },
    
    onPagingToolbarRefreshBtnClick: function (pagingTb, e) {
        if (this.store && this.store.baseParams) {
            this.store.baseParams.query = this.getRawValue();
        }
    }
});
Wtf.reg('select', Wtf.common.Select);