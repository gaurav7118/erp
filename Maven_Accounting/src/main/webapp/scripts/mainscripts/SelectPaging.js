/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Combo-Box with paging, works exactly like "Wtf.common.Select" combo-box but has paging in drop down records
 * xtype -> 'selectpaging'
 * Additional config required:
 * 1. pageSize -> Number of records to show at a time (e.g. 30); must be set '' in case paging is not required
 * @type @exp;Wtf@call;extend
 */
Wtf.common.SelectPaging = Wtf.extend(Wtf.common.Select, {
    forceSelection: true,
    typeAhead: false,
    minChars: 2,
    queryDelay: 500,
    typeAheadDelay: 500,
    initComponent: function () {
        Wtf.common.SelectPaging.superclass.initComponent.apply(this, arguments);
        this.typeAhead = false;
        this.store.on('beforeload', this.beforeStoreLoad, this);
        this.store.on('load', this.afterStoreLoad, this);
        this.on('clearval', this.loadStore, this);
//        this.on('specialkey', this.loadStore, this);
    },
    loadStore : function(){
        if(this.store.lastOptions && this.store.lastOptions.params){
            this.store.lastOptions.params.query = "";
        }
        this.store.reload();
    },
    beforeStoreLoad: function () {
        var value = this.getValue();
        if (this.combovalue != undefined && this.combovalue != '') {
            this.store.baseParams.combovalue = this.combovalue;
        } else if (value != undefined && value != '') {
            this.store.baseParams.combovalue = value;
        } else {
            this.store.baseParams.combovalue = undefined;
        }
    },
    afterStoreLoad: function () {
        var value = this.getValue();
        var rawValue = this.getRawValue();
        var appendComma = false;
        var appendQuery = false;
        var inputElDom = this.getEl().dom;
        if (rawValue != undefined && rawValue!="") {
            rawValue = rawValue.split(",");
            if(rawValue.length > 0 && rawValue[rawValue.length-1].trim() == ""){
                appendComma = true;
            }else{
                appendQuery=true;
            }
        }
        if (this.combovalue != undefined && this.combovalue != '') {
            this.setValue(this.combovalue);
        } else if (value != undefined && value != '') {
            this.setValue(value);
        }
        if(appendComma){
            inputElDom.value += ",";
        }else if(appendQuery && rawValue.length>1 ){
            inputElDom.value += ","+rawValue[rawValue.length-1].trim();
        }
        this.combovalue = undefined;
    },
    onTrigger2Click: function () {
        if (this.pageSize > 0) {
            this.lastQuery = undefined;
        }
        this.onTriggerClick();
    },
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
//                this.selectNext();
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
    onKeyUp: function (e) {
        var me = this,
        displayField = me.displayField,
        inputElDom = me.getEl().dom,
        valueStore = me.valueStore,
        //  boundList = me.getPicker(),
        record, newValue, len, selStart;

        if (me.mode == "remote") {
            if (this.editable !== false && !e.isSpecialKey()) {
                var filterData = inputElDom.value.split(",");
                var data = filterData[filterData.length - 1];
                var filterDatasubstr = "";
                if (filterData != undefined && filterData != "" && inputElDom.value.length > this.minChars) {
//                    filterDatasubstr = inputElDom.value.substring(inputElDom.value - 2);
                    filterDatasubstr = inputElDom.value.substring(inputElDom.value.length-1);
                }
                if (data != undefined && data != "") {
                    this.lastKey = e.getKey();
                    this.store.baseParams.query = data;
                    this.store.baseParams.combovalue = this.getValue();
                    this.dqTask.delay(this.queryDelay);
                } else if (filterDatasubstr != "" && filterDatasubstr=="," && (this.getValue() !=undefined && this.getValue() !="")) {
                    this.store.baseParams.query = "";
//                    this.store.reload();
//                    this.afterStoreLoad();
                }
            }
        } else {
            if (me.filterPickList) {
                var fn = this.createFilterFn(displayField, inputElDom.value);
                record = me.store.findBy(function (rec) {
                    return ((valueStore.indexOfId(rec.getId()) === -1) && fn(rec));
                });
                record = (record === -1) ? false : me.store.getAt(record);
            } else {
                var filterData = inputElDom.value.split(",");
                var data = filterData[filterData.length - 1];
                data = new RegExp(data, 'i');

                if (this.searchByProductCode != undefined && this.searchByProductCode) {
                    me.store.filterBy(function (rec) {
                        var returnFlag = false;
                        var regval = this.getRawValue();
                        var temp = regval.split(",");

                        if (temp.length != 0) {
                            var len = temp.length - 1;
                            regval = temp[len]
                        }

                        var reg = "";
                        if (regval == "") {
                            me.store.filter(this.displayField, regval, true);
                            this.store.clearFilter();
                        }
                        else {
                            reg = new RegExp(regval + "+", "gi");
                        }
                        var extraComparisionFieldArray = ['pid'];

                        for (var i = 0; i < extraComparisionFieldArray.length; i++) {
                            var arrayValue = extraComparisionFieldArray[i];
                            if (rec.get(arrayValue) != null && rec.get(arrayValue) != undefined && (rec.get(arrayValue).match(reg) || rec.get(this.displayField).match(reg))) {
                                returnFlag = true;
                                break;
                            }
                        }
                        return returnFlag;
                    }, this);
                } else {
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
        }
        
        if (inputElDom.value == "" && (e.getKey() == e.BACKSPACE || e.getKey() == e.DELETE)) {
            this.commonChangeValue('', '', [], []);
            if (this.view) {
                this.view.clearSelections();
            }
            if (this.el && this.el.dom) {
                this.el.dom.value = "";
                this.el.removeClass(this.emptyClass);
            }
        }
    },
    doQuery : function(q, forceAll){
        if (q === undefined || q === null) {
            q = '';
        } else {
            var filterData = q.split(",");
            q = filterData[filterData.length - 1];
        }
        var qe = {
            query: q,
            forceAll: forceAll,
            combo: this,
            cancel:false
        };
        if(this.fireEvent('beforequery', qe)===false || qe.cancel){
            return false;
        }
        q = qe.query;
        forceAll = qe.forceAll;
        if(forceAll === true || (q.length >= this.minChars)){
            if(this.lastQuery !== q){
                this.lastQuery = q;
                if(this.mode == 'local'){
                    this.selectedIndex = -1;
                    if(forceAll){
                        this.store.clearFilter();
                    }else{
                        this.store.filter(this.displayField, q);
                    }
                    this.onLoad();
                }else{
                    this.store.baseParams[this.queryParam] = q;
                    this.store.load({
                        params: this.getParams(q)
                    });
                    this.expand();
                }
            }else{
                this.selectedIndex = -1;
                this.onLoad();   
            }
        }
    }
});
Wtf.reg('selectpaging', Wtf.common.SelectPaging);
