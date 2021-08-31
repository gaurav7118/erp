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
Wtf.form.ExtFnComboBox=function(config){

    Wtf.form.ExtFnComboBox.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.form.ExtFnComboBox,Wtf.form.FnComboBox,{

    initComponent:function(config){
        Wtf.form.ExtFnComboBox.superclass.initComponent.call(this, config);
        var extrafield='';
        var accountCodeField='';
        var adjustWidth = 0;   // Width is not proper used to adjust width i product combo SDP-12746.
        var length=this.extraFields.length;
        this.listWidth=(length==3)?700:(length==2)?500:this.mailnotification?this.listWidth:300;
        if (this.isShowFullProductname) {
            this.listWidth = 700;
        } else if (this.searchOnField) {// Call from Stock Ledger Report
            this.listWidth = 400;
        } else if (this.customListWidth) {// Call from customer vendor address Information
            this.listWidth = 343;
        } else if(this.isProductCombo){
            this.listWidth = 600;
            adjustWidth = 0.5;
        }
        for (var i=0;i<length;i++){
            if(this.extraFields[i] == 'acccode' || this.AddQtipOnExtraFields){
                this.listWidth=this.listWidth+250;
                accountCodeField+='<td align="left" Wtf:qtip="{'+this.extraFields[i]+'}" width="'+100/(length+4)+'%"td>{'+this.extraFields[i]+':ellipsis(15)}</td>';          
            }else if(this.extraFields[i] == 'groupname'){
                this.listWidth=this.listWidth+100;
                extrafield+='<td align="left" width="'+100/(length+4)+'%"td>{'+this.extraFields[i]+':ellipsis(50)}</td>';          
            }else if(this.extraFields[i] == 'amountdue'){
                this.listWidth=this.listWidth+100;
                extrafield+='<td align="left" width="'+80/(length+4)+'%"td>{currencysymboltransaction}{'+this.extraFields[i] +'}</td>';          
            }
            else if(this.isParentCombo!=undefined && this.isParentCombo && this.extraFields[i] == 'pid'){ // used for parent store
                extrafield += '<td align="left" width="' + 100 / (length) + '%"td>{' + this.extraFields[i] + ':ellipsis(25)}</td>';       
            }else if(this.isParentCombo!=undefined && this.isParentCombo && this.extraFields[i] == 'parentname'){
                extrafield += '<td align="left" width="' + 100 / (length) + '%"td>{' + this.extraFields[i] + ':ellipsis(35)}</td>';  
            } else if (this.extraFields[i] == 'pid' || this.extraFields[i] == 'wcid') {// for productid and product type   //|| this.extraFields[i] == 'type' ERP-16498
                var width=100 / (length + 4);
                if(this.isShowFullProductname){
                    width=40;
                }
                extrafield += '<td align="left" width="' + width + '%"td>{' + this.extraFields[i] + ':ellipsis(15)}</td>';
            } else if (this.extraFields[i] == 'ccid') {  // costcenter ID
                this.listWidth = this.listWidth + 100;
                extrafield += '<td align="left" width="' + 100 / (length + 4) + '%"td>{' + this.extraFields[i] + ':ellipsis(15)}</td>';
            } else if(this.extraFields[i] == 'type'){// width is 100/5=20 so ellipsis are not grater than 20
                if (this.isProductCombo) {
                    adjustWidth=3;
                }
                extrafield+='<td align="left" width="'+100/(length+adjustWidth)+'%"td>{'+this.extraFields[i]+':ellipsis(20)}</td>';          
            } else if(this.extraFields[i] == 'productname' ){// width is 100/3=33.33 so ellipsis are not grater than 33 
                var elipsisValue=33;
                if(this.isShowFullProductname){
                    elipsisValue=100;
                }
                extrafield+='<td align="left" width="'+100/(length+adjustWidth)+'%"td>{'+this.extraFields[i]+':ellipsis('+elipsisValue+')}</td>';          
            } else if(this.extraFields[i] == 'description'){// for product name Cost center Desctiption
                if (CompanyPreferenceChecks.productComboDisplay() == Wtf.AccountProcutdescription && this.isProductCombo) {
                    extrafield += '<td align="left" width="' + 100 / (length + 1) + '%"td>{' + this.extraFields[i] + ':ellipsis(20)}</td>';
                }
                else {
                    extrafield += '<td align="left" width="' + 100 / (length + 1) + '%"td>{' + this.extraFields[i] + ':ellipsis(35)}</td>';
                }          
            } else if (this.searchOnField) {
                extrafield += '<td align="left" Wtf:qtip="{' + this.extraFields[i] + '}" width="'+100/(length)+'%"td>{'+this.extraFields[i]+':ellipsis(50)}</td>';
            }else if(this.isBOMCombo && this.extraFields[i] == 'name'){
                extrafield+='<td align="left" width="'+100/(length+1)+'%"td>{'+this.extraFields[i]+':ellipsis(90)}</td>';
            } else{
                extrafield+='<td align="left" width="'+100/(length+3)+'%"td>{'+this.extraFields[i]+':ellipsis(20)}</td>';            
            } 
        }
        /*
         * displayred-record = Loan is not clear
         * displayblue-record = Loan is clear
         * x-combo-list-item=Loan is not Apply
         */
        if(this.isProductCombo){
            adjustWidth = 0.5;
        }
        if(this.loanFlag){
            this.tpl=new Wtf.XTemplate(
                '<tpl for="."><div Wtf:qtip="{[values.isLoanClear === false ? ('+this.isVendor+'?"You cannot select deactivated Vendor":'+this.isCustomer+'?"You cannot select deactivated Customer":'+this.activated+'?"You cannot select deactivated salesperson" : '+this.isProductCombo+'?"You cannot select deactivated Products":"You cannot select deactivated Accounts") : values.'+this.displayField+']}" class="{[(values.isLoanClear === false && values.isLoanApply===true) ? "x-combo-list-item displayred-record" : (values.isLoanClear === true && values.isLoanApply===true) ? "x-combo-list-item displayblue-record":"x-combo-list-item"]}" ><table width="100%"><tr>'+accountCodeField+'<td align="left" width="'+100/(length)+'%">{[this.getDots(values.level)]}{'+this.displayField+':ellipsis(50)}</td>'+extrafield+'</tr></table></div></tpl>',{
                    getDots:function(val){
                        var str="";
                        for(var i=0;i<val;i++)
                            str+="....";
                        return str;
                    }
                })
        } else if(this.isAccountCombo || this.isVendor || this.isCustomer || this.activated || this.isProductCombo || this.isTax || this.commonFlagforDimAndCustomeField){//this.commonFlagforDimAndCustomeField flge used to activate or deactivate master items for custom field and dimension field
            var template = '<tpl for="."><div Wtf:qtip="{[values.hasAccess === false ? ('+this.isVendor+'?"You cannot select deactivated Vendor":'+this.isCustomer+'?"You cannot select deactivated Customer":'+this.activated+'?"You cannot select deactivated salesperson" :'+this.commonFlagforDimAndCustomeField+'?"You cannot select deactivated master field" :'+this.isProductCombo+'?"You cannot select deactivated Products":'+this.isTax+'?"You cannot select deactivated Tax.":"You cannot select deactivated Account.") : values.'+this.displayField+']}" class="{[values.hasAccess === false ? "x-combo-list-item disabled-record" : "x-combo-list-item"]}" ><table width="100%"><tr>'+accountCodeField+'<td align="left" width="'+100/(length)+'%">{[this.getDots(values.level)]}{'+this.displayField+':ellipsis(50)}</td>'+extrafield+'</tr></table></div></tpl>';;
            if(this.isProductCombo){
                if (this.isCostCenterCombo) {
                    template = '<tpl for="."><div Wtf:qtip="{[values.hasAccess === false ? (' + this.isVendor + '?"You cannot select deactivated Vendor":' + this.isCustomer + '?"You cannot select deactivated Customer":' + this.activated + '?"You cannot select deactivated salesperson" : ' + this.isProductCombo + '?"You cannot select deactivated Products":"You cannot select deactivated Accounts") : values.' + this.displayField + ']}" class="{[values.hasAccess === false ? "x-combo-list-item disabled-record" : "x-combo-list-item"]}" ><table width="100%"><tr>' + accountCodeField + '<td align="left" width="' + 100 + '%">{[this.getDots(values.level)]}{' + this.displayField + ':ellipsis(50)}</td>' + extrafield + '</tr></table></div></tpl>';
                } else {
                    template = '<tpl for="."><div Wtf:qtip="{[values.hasAccess === false ? (' + this.isVendor + '?"You cannot select deactivated Vendor":' + this.isCustomer + '?"You cannot select deactivated Customer":' + this.activated + '?"You cannot select deactivated salesperson" : ' + this.isProductCombo + '?"You cannot select deactivated Products":"You cannot select deactivated Accounts") : values.' + this.displayField + ']}" class="{[values.hasAccess === false ? "x-combo-list-item disabled-record" : "x-combo-list-item"]}" ><table width="100%"><tr>' + accountCodeField + '<td align="left" width="' + 100 /(length+adjustWidth) + '%">{[this.getDots(values.level)]}{' + this.displayField + ':ellipsis(30)}</td>' + extrafield + '</tr></table></div></tpl>';
                }
                 }
            
            this.tpl=new Wtf.XTemplate(
            template,{
                getDots:function(val){
                    var str="";
                    for(var i=0;i<val;i++)
                        str+="....";
                    return str;
                }
            })
        } else if (this.isAdvanceSearchCombo) {
            this.tpl = new Wtf.XTemplate(
                    '<tpl for="."><div Wtf:qtip="{[values.fieldid === "NA" ? "You cannot select value" : values.' + this.displayField + ']}" class="{[values.fieldid === "NA" ? "x-combo-list-item disabled-record" : "x-combo-list-item"]}" ><table width="100%"><tr>' + accountCodeField + '<td align="left" width="' + 100 / (length) + '%">{[this.getDots(values.level)]}{' + this.displayField + ':ellipsis(50)}</td>' + extrafield + '</tr></table></div></tpl>', {
                getDots: function(val) {
                    var str = "";
                    for (var i = 0; i < val; i++)
                        str += "....";
                    return str;
                }
            })
        } else if (this.WOStatus && !this.isEdit) { // Done to Make In Process status disabled at creation time while enable on edit time
            this.tpl = new Wtf.XTemplate(
                    '<tpl for="."><div Wtf:qtip="{[values.defaultMasterItem === "4c3f913b-1e3a-11e6-8206-14dda97927f2" ? "You cannot select value" : values.' + this.displayField + ']}" class="{[values.defaultMasterItem === "4c3f913b-1e3a-11e6-8206-14dda97927f2" ? "x-combo-list-item disabled-record" : "x-combo-list-item"]}" ><table width="100%"><tr>' + accountCodeField + '<td align="left" width="' + 100 / (length) + '%">{[this.getDots(values.level)]}{' + this.displayField + ':ellipsis(50)}</td>' + extrafield + '</tr></table></div></tpl>', {
                getDots: function(val) {
                    var str = "";
                    for (var i = 0; i < val; i++)
                        str += "....";
                    return str;
                }
            })
        } else if (this.mailnotification) {//For mail notifications 
            this.tpl = new Wtf.XTemplate(
            '<tpl for="."><div Wtf:qtip="{[values.id === "NA" ? "You cannot select value" : values.' + this.displayFi000000000000000eld + ']}" class="{[values.id === "NA" ? "x-combo-list-item disabled-record" :"x-combo-list-item"]} Wtf:qtip="{' + this.displayField + '}"><table width="100%"><tr>' + accountCodeField + '<td align="left" width="' + 100 / (length) + '%">{[this.getDots(values.level)]}{' + this.displayField + ':ellipsis(70)}</td>' + extrafield + '</tr></table></div></tpl>',{
                getDots: function(val) {
                    var str = "";
                    for (var i = 0; i < val; i++)
                        str += "....";
                    return str;
                }
            })
        } else {
            this.tpl = new Wtf.XTemplate(
                    (this.isProductCombo) ? '<tpl for="."><div class="x-combo-list-item" Wtf:qtip="{' + this.displayField + '}"><table width="100%"><tr>' + accountCodeField + extrafield + '</tr></table></div></tpl>' : '<tpl for="."><div class="x-combo-list-item" Wtf:qtip="{' + this.displayField + '}"><table width="100%"><tr>' + accountCodeField + '<td align="left" width="' + 100 / (length) + '%">{[this.getDots(values.level)]}{' + this.displayField + ':ellipsis(50)}</td>' + extrafield + '</tr></table></div></tpl>', {
                getDots: function(val) {
                    var str = "";
                    for (var i = 0; i < val; i++)
                        str += "....";
                    return str;
        }
            })
        }
        },
        doQuery : function(q, forceAll){
            q = Wtf.isEmpty(q) ? '' : q;
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
            if(this.mode == "remote" && q.length==0){
                forceAll = true;   
            }

            if(forceAll === true || (q.length >= this.minChars)){
                if(q.trim() ==""){
                    this.selectedIndex = -1;
                    this.store.clearFilter();
                    this.store.baseParams[this.queryParam] = q; 
                    if(this.mode == 'remote'){             // If store is remote, then only reloaded
                        this.store.load({
                                params: this.getParams(q)
                        });
                    }
                    this.onLoad();
                } else {
                if(this.lastQuery !== q){
                    this.lastQuery = q;
                    if(this.mode == 'local'){
                        this.selectedIndex = -1;
                        if(forceAll){
                            this.store.clearFilter();
                        }else{
                           
                            if(this.extraComparisionFieldArray != null && this.extraComparisionFieldArray != undefined)  {
                               
                                this.store.filterBy(function(rec) {
                                    var returnFlag = false;
                                    var regval = this.getRawValue();
                                    if (regval == "") {
                                        this.store.filter(this.displayField, q, true);
                                    }         
                                    var reg = new RegExp(regval + "+", "gi");//  var reg = new RegExp("^"+this.getRawValue()+"+", "gi"); //changed to Start from beging to any match
                                    if (this.isProductCombo && Wtf.account.companyAccountPref.productsearchingflag == 0) {
                                        reg = new RegExp("^" + this.getRawValue() + "+", "gi"); //changed to Start from beging to any match
                                    }
                                    for (var i = 0; i < this.extraComparisionFieldArray.length; i++) {
                                        var arrayValue = this.extraComparisionFieldArray[i];
                                        if (rec.get(arrayValue) != null && rec.get(arrayValue) != undefined && (rec.get(arrayValue).match(reg) || rec.get(this.displayField).match(reg))) {
                                            returnFlag = true;
                                            break;
                                        }
                                    }
                                   
                                    return returnFlag;
                                }, this);
                            } else if(this.extraComparisionField != null && this.extraComparisionField != undefined ){
                                this.store.filterBy(function(rec){
                                    var returnFlag = false;
                                    var regval=this.getRawValue();
                                     if(regval==""){this.store.filter(this.displayField, q, true);}
                                    //var reg = new RegExp(regval+"+", "gi");//  var reg = new RegExp("^"+this.getRawValue()+"+", "gi"); //changed to Start from beging to any match
                                    var reg = new RegExp(regval + "+", "gi");//  var reg = new RegExp("^"+this.getRawValue()+"+", "gi"); //changed to Start from beging to any match
                                    if (this.isProductCombo && Wtf.account.companyAccountPref.productsearchingflag == 0) {
                                        reg = new RegExp("^" + this.getRawValue() + "+", "gi"); //changed to Start from beging to any match
                                    }
                                    if(rec.get(this.extraComparisionField)!= null && rec.get(this.extraComparisionField) != undefined &&(rec.get(this.extraComparisionField).match(reg) || rec.get(this.displayField).match(reg))){
                                        returnFlag = true;
                                    }
                                    return returnFlag;
                                },this);
                            } else{
                                this.store.filter(this.displayField, q, true); // supply the anyMatch option
                            }
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
    },
      onTypeAhead : function(){
        if(this.store.getCount() > 0){
            var r = this.store.getAt(0);
            var newValue = r.data[this.displayField];
            var len = newValue.length;
            var selStart = this.getRawValue().length;
//            if(selStart != len){
//                this.setRawValue(newValue);
//                this.selectText(selStart, newValue.length);
//            }
        }
    }
});
//Regestering xtype for Wtf.form.ExtFnComboBox
Wtf.reg('extfncombobox', Wtf.form.ExtFnComboBox);