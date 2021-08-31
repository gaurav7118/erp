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
WtfGlobal = {
    formatAccountName:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="") {
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        }       
        if(rec.data.accountflag) {
            fmtVal = WtfGlobal.accountLinkRenderer(fmtVal);
        }
        return fmtVal; 
    },
    
    formatMoney:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    },
    
    getCookie: function(c_name){
        if (document.cookie.length > 0) {
            c_start = document.cookie.indexOf(c_name + "=");
            if (c_start != -1) {
                c_start = c_start + c_name.length + 1;
                c_end = document.cookie.indexOf(";", c_start);
                if (c_end == -1)
                    c_end = document.cookie.length;
                return unescape(document.cookie.substring(c_start, c_end));
            }
        }
        return "";
    },

    nameRenderer: function(value){
        var resultval = value.substr(0, 1);
        var patt1 = new RegExp("^[a-zA-Z]");
        if (patt1.test(resultval)) {
            return resultval.toUpperCase();
        }
        else
            return "Others";
    },
    memoRenderer: function (value) {
        value = value.replace(/\'/g, "&#39;");
        value = value.replace(/\"/g, "&#34");
        return "<span class=memo_custom  wtf:qtip='" + value + "'>" + Wtf.util.Format.ellipsis(value, 60) + "</span>"
    },
    qcStatusRenderer: function (value) {
        if (value === 0) { // Failed Flag
            return '<span style="color:red;font-weight:bold">' + "Fail" + '</span>';
        } else { // Pass 
            return '<span style="color:green;font-weight:bold">' + "Pass" + '</span>';
        }
    },
     progressRenderer: function (value) {
         var left=5;
         var color='black';
         if(value > 20){
             left=value-16;
         }
         
         if(value >= 15){
             color='white';
         }
         
          return '<div id="progress">\n\
                     <span  style="position: absolute; left: '+left+'%;color:'+color+'" >'+value+'%</span>\n\
                        <div  style="height: 20px;background-color: green;width:'+value+'%" >\n\
                        </div>\n\
                  </div>';
    },
    fliterNonUsedDeactivatedTerms: function(scope)
    {
            /*
             * If term is deactivated and term amount and  term percentage is zero term is removed from the store
             */
     scope.termStore.filterBy(function(rec){
                if (rec.data.isTermActive||(!rec.data.isTermActive && (rec.data.termamount != 0 || rec.data.termpercentage != 0))){
                    return true;
                } else{
                    return false
                }
            });
    },
    getRowClass:function(record,grid){
                /*
                 * If term is deactivated and background is set to gray color
                 */
                var colorCss = "";
                        switch (record.data["isTermActive"]){
                case false:colorCss = " grey-background"; break;
                }
               return colorCss;
    },

    sizeRenderer: function(value){
        var sizeinKB = value
        if (sizeinKB >= 1 && sizeinKB < 1024) {
            text = "Small";
        }else if (sizeinKB > 1024 && sizeinKB < 102400) {
            text = "Medium";
        } else if (sizeinKB > 102400 && sizeinKB < 1048576) {
            text = "Large";
        }else {
            text = "Gigantic";
        }
        return text;
    },
    sizetypeRenderer: function(value){
        var i = 0;
        var text = "";
        if(value==""){
            return "-";
        }
        var size = value*1024;
        while(size > 1024){
            size/=1024;
            i++;
        }
        switch(i){
            case 0:text=WtfGlobal.conventInDecimal(size, "")+" Bytes";break;
            case 1:text=WtfGlobal.conventInDecimal(size, "")+" KB";break;
            case 2:text=WtfGlobal.conventInDecimal(size, "")+" MB";break;
            case 3:text=WtfGlobal.conventInDecimal(size, "")+" GB";break;
            case 4:text=WtfGlobal.conventInDecimal(size, "")+" TB";break;
        }
        return text;
    },
    dateFieldRenderer: function(value){
        var text = "";
        if (value) {
            var dt = new Date();
            if ((value.getMonth() == dt.getMonth()) && (value.getYear() == dt.getYear())) {
                if (dt.getDate() == value.getDate()) {
                    text = WtfGlobal.getLocaleText("acc.field.Today");
                } else if (value.getDate() == (dt.getDate() - 1))
                    text = WtfGlobal.getLocaleText("acc.field.Yesterday");
                else if (value.getDate() <= (dt.getDate() - 7) && value.getDate() > (dt.getDate() - 14))
                    text = WtfGlobal.getLocaleText("acc.field.LastWeek");
            } else if ((value.getMonth() == (dt.getMonth() - 1)) && (value.getYear() == dt.getYear()))
                text = WtfGlobal.getLocaleText("acc.field.LastMonth");
            else if ((value.getYear() == (dt.getYear() - 1)))
                text = WtfGlobal.getLocaleText("acc.field.LastYear");
            else
                text = WtfGlobal.getLocaleText("acc.field.Older");
        } else
            text = WtfGlobal.getLocaleText("acc.rem.111");
        return text;
    },

    permissionRenderer: function(value, rec){
        var text = value.toLowerCase();
        switch (text) {
            case "everyone":
                text = "Everyone on deskEra";
                break;
            case "connections":
                text = "All Connections";
                break;
            case "none":
                text = "Private";
                break;
            default:
                text = "Selected Connections";
                break;
        }
        return text;
    },

    replaceAll : function(txt, replace, with_this) {
        return txt.replace(new RegExp(replace, 'g'),with_this);
    },

	HTMLStripper: function(val){
        var str = Wtf.util.Format.stripTags(val);
        return str.replace(/"/g, '').trim();
    },

    ScriptStripper: function(str){
        str = Wtf.util.Format.stripScripts(str);
        if (str)
            return str.replace(/"/g, '');
        else
            return str;
    },

    URLDecode: function(str){
        str=str.replace(new RegExp('\\+','g'),' ');
        return unescape(str);
    },

    getDateFormat: function() {
        return Wtf.pref.DateFormat;
    },

    getSeperatorPos: function() {
        return Wtf.pref.seperatorpos;
    },

    getOnlyDateFormat: function() {
        var pos=WtfGlobal.getSeperatorPos();
        var fmt=WtfGlobal.getDateFormat();
        if(pos<=0)
            return "Y-m-d";
        return fmt.substring(0,pos);
    },

//This method ll return Date in ( Thu Jan 31 2015 00:00:00 Z) format. 'Z' indicates Timezone.
    getDayMonthYearDate : function(date){               //ERP-9338
      var day = date.getDate();                         
      var month = date.getMonth()+1;   //Index of Jan=0, Feb=1 ... Dec=11. So for understanding purpose added 1 in month.      
      var year = date.getFullYear();                    
      return new Date(month + "/" + day + "/" + year);  
    },
    
    getOnlyTimeFormat: function() {
        var pos=WtfGlobal.getSeperatorPos();
        var fmt=WtfGlobal.getDateFormat();
        if(pos>=fmt.length)
            return "H:i:s";
        return fmt.substring(pos);
    },

    dateRenderer: function(v) {
        if(!v) return v;
        return '<div class="datecls">'+v.format(WtfGlobal.getDateFormat())+'</div>';
    },
    
    dateTimeRenderer: function(v) {
        if(!v) return v;
        var vd = new Date(v);
        return vd.format(WtfGlobal.getDateFormat());
    },  
    
    onlyTimeRenderer: function(v) {
        if(!v) return v;
        return '<div class="datecls">'+v.format(WtfGlobal.getOnlyTimeFormat())+'</div>';
    },
    onlyMonthRenderer: function(v) {
        var m_names = new Array("January", "February", "March",
        "April", "May", "June", "July", "August", "September",
            "October", "November", "December");

        if(!v) return v;
        var date=new Date(v);
       return '<div class="leftdatecls">'+m_names[date.getMonth()]+"-"+date.getFullYear()+'</div>';
    },
    onlyDateRenderer: function(v) {
        if(v==""){
            return "-";
        }
        if(!v) return v;
        return '<div class="datecls">'+v.format(WtfGlobal.getOnlyDateFormat())+'</div>';
    },
    
    stringValueRenderer: function(v) {
        if (v == "") {
            return "-";
        } else {
            return v;
        }
    },
    
    onlyDateRendererForDeliveryPlanner: function(val) {
        if (!val) {
            return val;
        } else if (val > new Date()) {
            return "<div class='datecls' style='color:#0000FF'>" + val.format(WtfGlobal.getOnlyDateFormat()) + "</div>";
        } else {
            return "<div class='datecls'>" + val.format(WtfGlobal.getOnlyDateFormat()) + "</div>";
        }
    },
    
    onlyDateRendererParsed: function (v) {
        if (!v) {
            return v;
        } else {
            var v=new Date(Date.parse(v));
            return '<div class="datecls">' + v.format(WtfGlobal.getOnlyDateFormat()) + '</div>';
        }
    },
    onlyDateRendererForGridHeader: function(v) {
        if(!v) return v;
        return v.format(WtfGlobal.getOnlyDateFormat());
    },
    itemSequenceRenderer: function(val, cell, row, rowIndex, colIndex, ds) {
        var storecount = ds.getCount();
        var str = "";
        if (row.data.parentid == "") {
            var count = 0;
            var arr = [];
            var arrcount = 0;
            for (var i = 0; i < storecount; i++) {
                if (ds.data.items[i].data.parentid == "") {
                    count++;
                    arr[arrcount++] = i;
                }
            }
            if (count >= 2) {
                if (rowIndex >= arr[0] && rowIndex != arr[arr.length - 1])
                    str += '<div class=\'pwndBar2 shiftrowdownIcon\'></div>';
                if (rowIndex <= arr[arr.length - 1] && rowIndex != arr[0])
                    str += ' <div class=\'pwndBar2 shiftrowupIcon\' ></div>';
            }
        } else {
            var count = 0;
            var arr = [];
            var arrcount = 0;
            for (var i = 0; i < storecount; i++) {
                if (ds.data.items[i].data.parentid == row.data.parentid) {
                    count++;
                    arr[arrcount++] = i;
                }
            }
            if (count >= 2) {
                if (rowIndex >= arr[0] && rowIndex != arr[arr.length - 1])
                    str += '<div class=\'pwndBar2 shiftrowdownIcon\'></div>';
                if (rowIndex <= arr[arr.length - 1] && rowIndex != arr[0])
                    str += ' <div class=\'pwndBar2 shiftrowupIcon\' ></div>';
            }
        }
        return str;
    },
    onlyDateRightRenderer: function(v) {
        if(!v) return v;
        return '<div class="rightdatecls">'+v.format(WtfGlobal.getOnlyDateFormat())+'</div>';
    },
    onlyDateLeftRenderer: function(v) {
        if(!v) return v;
        return '<div class="leftdatecls">'+v.format(WtfGlobal.getOnlyDateFormat())+'</div>';
    },
    convertToGenericDate:function(value){
        if(!value) return value;
        if(typeof value==="string"){
            return value;
        }else{
            return value.format("M d, Y h:i:s A");
        }
    },
       
    convertToGenericStartDate:function(value){ //this method mainly used to format Report Start Date  
        var d = new Date();
        if(!value) return value;       
        var monthDateStr=value.format('M d');
        var startdate=new Date(monthDateStr+', '+value.getFullYear()+' 12:00:00 AM');
        return startdate.format("M d, Y h:i:s A");        
    },
    
    convertToGenericEndDate:function(value){ //this method mainly used to format Report End Date
        var d = new Date();
        if(!value)return value;
        var monthDateStr=value.format('M d');
        var enddate=new Date(monthDateStr+', '+value.getFullYear()+' 11:59:59 PM');
        return enddate.format("M d, Y h:i:s A");         
    },
 
    convertToDateOnly:function(value){
        if(!value) return value;
        return value.format("Y-m-d");
    },

    getTimeZone: function() {
        return Wtf.pref.Timezone;
    },
    onlyDateDeletedRenderer: function(v,m,rec) {
        if(!v) return v;
        if(rec!=undefined&&rec.data.deleted)
            v='<del>'+v.format(WtfGlobal.getOnlyDateFormat())+'</del>';
        else
        	v = v.format(WtfGlobal.getOnlyDateFormat());
        v='<div class="datecls">'+v+'</div>';
         return v;
    },
    deletedRenderer: function(v,m,rec) {
        if(!v) return v;
        if(rec.data.deleted)
             v='<del>'+v+'</del>';
         return v;
    },
    /*  Renderer for balance Quantity of Product*/
    balanceQtyrenderer: function(val, m, rec) {
        val = (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        return'<div style="margin-right: 15px;">' + val + '</div>';
    },
    /*Renderer for block Quantity of Product*/
    blockQtyrenderer: function(val, m, rec, i, j, s) {
        if (rec.data['type'] == "Service") {
            return "N/A";
        }
        var unit = rec.data['uomname'];
        var value = parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        if (isNaN(value))
            return val;
        val = WtfGlobal.conventInDecimalWithLink(value, unit);
        return val;
    },
    customTextFieldRenderer: function(val,m,rec) {
        if(!val) return val;
        var regex = /(<([^>]+)>)/ig;
        val = val.replace(/(<([^>]+)>)/ig,"");
        var tip = val.replace(/"/g,'&rdquo;');
           return "<div style=\'width:600px;\' wtf:qtip=\""
           +"<div style=\'word-wrap:break-word;text-wrap:unrestricted;\'>"+tip+"</div>"
           + "\" wtf:qtitle='"
           +   "'>" + Wtf.util.Format.ellipsis(val,60) + "</div>";
    },
    booleanRenderer: function(v,m,rec) {    //To show "Yes/No" in UI for "true/false" value
        if(!v) return "No";
        if(rec.data.isactivate){
             v='Yes';
        }
         return v;
    },
    excludedRenderer: function(v,m,rec) {
        if(!v) return v;
        if(rec.data.excluded)
             v='<del>'+v+'</del>';
         return v;
    },
        rendererForAccountNameWithTax: function(v,m,rec) {
        if(!v)
            return unescape(v);  //decode first and then removed + characters from string 
        if(rec.data.deleted)
             v='<del>'+v+'</del>';
         v=unescape(v);
         var i=0;
         if(v.indexOf("+")!=-1){
           for(i=0;i<v.indexOf("+");i++) 
                v=v.replace("+"," ");  
         }
         return v;
    },
    getSelectComboRenderer:function(combo, textLength){
        return function(value) {
            var idx;
            var rec;
            var valStr="";
            if (value != undefined && value != "") {
                var valArray = value.split(",");
                for (var i=0;i < valArray.length;i++ ){
                    idx = combo.store.find(combo.valueField, valArray[i]);
                    if(idx != -1){
                        rec = combo.store.getAt(idx);
                        valStr+=rec.get(combo.displayField)+", ";
                    }
                }
                if(valStr != ""){
                    valStr=valStr.substring(0, valStr.length -2);
                    if(textLength){
                        valStr="<div wtf:qtip=\""+valStr+"\">"+Wtf.util.Format.ellipsis(valStr,textLength)+"</div>";    
                    }else{
                        valStr="<div wtf:qtip=\""+valStr+"\">"+Wtf.util.Format.ellipsis(valStr,27)+"</div>";    
                    }
                }
            }
            return valStr;
        }
    },
    currencyDeletedRenderer: function(value,isCheckCenterAlign,rec) {
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign[0]);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
            v= WtfGlobal.conventInDecimal(v,WtfGlobal.getCurrencySymbol());
            if(rec.data.deleted)
                v='<del>'+v+'</del>';
             v=  '<div class="currency">'+v+'</div>';
            if(isCenterAlign)
                  v= '<div>'+v+'</div>';
         return v;
    },
    currencyNoncurrencyRenderer: function (value, isCheckCenterAlign, rec) {
        var isCenterAlign = (isCheckCenterAlign == undefined ? false : isCheckCenterAlign[0]);
        var v = parseFloat(value);
        if (isNaN(v)){
            return value;
        }
        if (rec.data.acccode == "<b>Zero-Moved Products<b>" || rec.data.acccode == "<b>Zero Transaction Sales Persons<b>" || rec.data.acccode == "<b>Total Customers Reported<b>" || rec.data.acccode == "<b>Active Customers Not Reported<b>" || rec.data.acccode == "<b>Zero Transaction Customers<b>" || rec.data.acccode == "<b>Total Customers<b>" || rec.data.acccode == "<b>Total Products Reported<b>" || rec.data.acccode == "<b>Active Products Not Reported<b>" || rec.data.acccode == "<b>Total Products<b>" || rec.data.acccode == "<b>Total Sales Persons Reported<b>" || rec.data.acccode == "<b>Active Sales Persons Not Reported<b>" || rec.data.acccode == "<b>Total Sales Persons<b>") {
            return '<div align="right">' + value + '</div>';
        }
        v = WtfGlobal.conventInDecimal(v, WtfGlobal.getCurrencySymbol());
        if (rec.data.deleted)
            v = '<del>' + v + '</del>';
        v = '<div class="currency">' + v + '</div>';
        if (isCenterAlign)
            v = '<div>' + v + '</div>';
        return v;
    },
    
    currencyDeletedRendererDefaultValue: function(value,isCheckCenterAlign,rec) {
        if(value==undefined || value=='' || value==null){
            value=0;
        }
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign[0]);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
            v= WtfGlobal.conventInDecimal(v,WtfGlobal.getCurrencySymbol());
            if(rec.data.deleted)
                v='<del>'+v+'</del>';
             v=  '<div class="currency">'+v+'</div>';
            if(isCenterAlign)
                  v= '<div>'+v+'</div>';
         return v;
    },
    currencyDeletedRendererDefaultValueforCOA: function(value, isCheckCenterAlign, rec) {
        var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
        if (value == undefined || value == '' || value == null) {
            value = 0;
        }
        var isCenterAlign = (isCheckCenterAlign == undefined ? false : isCheckCenterAlign[0]);
        var v = parseFloat(value);
        if (isNaN(v))
            return value;
        v = WtfGlobal.conventInDecimal(v, symbol);
        if (rec.data.deleted)
            v = '<del>' + v + '</del>';
        v = '<div class="currency">' + v + '</div>';
        if (isCenterAlign)
            v = '<div>' + v + '</div>';
        return v;
    },

    withCurrencyUnitPriceRenderer : function(value,isCheckCenterAlign,rec){
        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign);
        var v=parseFloat(value);
        if(isNaN(v)){
          return value;  
        } 
        v= WtfGlobal.convertInDecimalWithDecimalDigit(v,symbol,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        if(rec.data.deleted){
            v='<del>'+v+'</del>';
        } else if(isCenterAlign){            
            v= '<div>'+v+'</div>';
        } else {
            v=  '<div class="currency">'+v+'</div>';   
        }                                         
        return v;
    },
        
    withVendorCurrencyUnitCostRenderer : function(value,isCheckCenterAlign,rec){
        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['vendorcurrencysymbol']==undefined||rec.data['vendorcurrencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['vendorcurrencysymbol']);
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign);
        var v=parseFloat(value);
        if(isNaN(v)){
          return value;  
        } 
        v= WtfGlobal.convertInDecimalWithDecimalDigit(v,symbol,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        if(rec.data.deleted){
            v='<del>'+v+'</del>';
        } else if(isCenterAlign){            
            v= '<div>'+v+'</div>';
        } else {
            v=  '<div class="currency">'+v+'</div>';   
        }                                         
        return v;
    },
    exchangeRateRenderer: function (value, meta, record) {
        var currencysymbol = WtfGlobal.getCurrencySymbol();
        var currencysymboltransaction = ((record == undefined || record.data.vendorcurrencysymbol == null || record.data['vendorcurrencysymbol'] == undefined || record.data['vendorcurrencysymbol'] == "") ? currencysymbol : record.data['vendorcurrencysymbol']);
        var v = parseFloat(value);
        if (isNaN(v))
            return value;
        return "1 " + currencysymboltransaction + " = " + value + " " + currencysymbol;
    },
    
    withoutCurrencyUnitPriceRenderer : function(value,isCheckCenterAlign,rec){
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign[0]);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
            v= WtfGlobal.convertInDecimalWithDecimalDigit(v,"",Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
             if(rec.data.deleted){
            v='<del>'+v+'</del>';
        } else if(isCenterAlign){            
            v= '<div>'+v+'</div>';
        } else {
            v=  '<div class="currency">'+v+'</div>';   
        }                                         
        return v;
    },
    amountWithoutCurrencyRender: function (value, m, rec) {
        var v = parseFloat(value);
        if (isNaN(v)) {
            return value;
        }
        v = WtfGlobal.conventInDecimal(v, "")
        return '<div class="currency">' + v + '</div>';
    },
    convertInDecimalWithDecimalDigit :function(value,symbol,decimalDigit){
            value= Number(value);	
            value=getRoundofValueWithValues(value,decimalDigit);
            value=parseFloat(value).toFixed(decimalDigit);
            value = String(value);
            var ps = value.split('.');
            var whole = ps[0];
            
            if(decimalDigit==4){
                var sub = ps[1] ? '.'+ ps[1] : '.0000';
            }else if(decimalDigit==3){
                sub = ps[1] ? '.'+ ps[1] : '.000';
            }else if(decimalDigit==2){
                 sub = ps[1] ? '.'+ ps[1] : '.00';
            }else if(decimalDigit==1){
                sub = ps[1] ? '.'+ ps[1] : '.0';
            }else{
                sub = ps[1] ? '.'+ ps[1] : '';
            }  
            var r = /(\d+)(\d{3})/;
            while (r.test(whole)) {
                whole = whole.replace(r, '$1' + ',' + '$2');
            }
            value = whole + sub;
            if(value.charAt(0) == '-') {
              value= "(<label style='color:red'>"+symbol + " " + value.substr(1)+"</label>)";
            } else
                value=symbol + " " + value;
            return value;
    },
    
     withoutRateCurrencyDeletedSymbol: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        if(rec.data.deleted)
            v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
        else
        	v=WtfGlobal.conventInDecimal(v,symbol);
          v=  '<div class="currency">'+v+'</div>';
         return v;
    },
         gstdecimalRenderer: function(value,m,rec) {
        var symbol="";//((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        	v=WtfGlobal.conventInGSTDecimal(v,symbol);
                v = v + "%";
          v=  '<div class="currency">'+v+'</div>';
         return v;
    },         
    gstdecimalRendererWithoutCurrency: function (value, m, rec) {
        var symbol = "";//((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v = parseFloat(value);
        if (isNaN(v))
            return value;
        v = WtfGlobal.conventInGSTDecimal(v, symbol);
        v = v + "%";
        return v;
    },
        conventInGSTDecimal: function(v,symbol) {

	    v= Number(v);	
            v=getRoundedAmountValue(v);
            v=parseFloat(v).toFixed(Wtf.account.companyAccountPref.columnPref.gstamountdigitafterdecimal);
            v = String(v);
            var ps = v.split('.');
            var whole = ps[0];
            
            if(Wtf.account.companyAccountPref.columnPref.gstamountdigitafterdecimal==4){
                var sub = ps[1] ? '.'+ ps[1] : '.0000';
            }else if(Wtf.account.companyAccountPref.columnPref.gstamountdigitafterdecimal==3){
                sub = ps[1] ? '.'+ ps[1] : '.000';
            }else{
                sub = ps[1] ? '.'+ ps[1] : '.00';
            }  
          
                whole = whole.replace(/(\d)(?=(\d\d)+\d$)/g, "$1,");
            v = whole + sub;
            if(v.charAt(0) == '-') {
              v= "(<label style='color:red'>"+symbol + " " + v.substr(1)+"</label>)";
            } else
                v=symbol + " " + v;
            return v;
    },
    conversionFactorRenderer:function(value,meta,record) {
//           var currencysymboltransaction=((record==undefined||record.data.currencysymboltransaction==null||record.data['currencysymboltransaction']==undefined||record.data['currencysymboltransaction']=="")?WtfGlobal.getCurrencySymbol():record.data['currencysymboltransaction']);
           var currencysymboltransaction=WtfGlobal.getCurrencySymbol();
           var currencysymbol=((record==undefined||record.data.currencysymbol==null||record.data['currencysymbol']==undefined||record.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():record.data['currencysymbol']);
                var v=parseFloat(value);
                if(isNaN(v)) return value;
//                    v= WtfGlobal.conventInDecimal(v,currencysymboltransaction)
//                return '<div class="currency">'+v+'</div>';
                return "1 "+ currencysymboltransaction +" = " +value+" "+currencysymbol;
            
    },

     currencyRendererDeletedSymbol: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data['currencysymbol']==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
        var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
        var v;
        if(rate!=0.0)
            v=(parseFloat(value)*parseFloat(rate))/parseFloat(oldcurrencyrate);
        else
            v=(parseFloat(value)/parseFloat(oldcurrencyrate));
        if(isNaN(v)) return value;
        if(rec.data.deleted)
            v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
        else
        	v = WtfGlobal.conventInDecimal(v,symbol);
        v=  '<div class="currency">'+v+'</div>';
         return v;

    },
    onlyMonthDeletedRenderer: function(v,m,rec) {
        var m_names = new Array("January", "February", "March",
        "April", "May", "June", "July", "August", "September",
            "October", "November", "December");

        if(!v) return v;
        var date=new Date(v);
       v='<div class="leftdatecls">'+m_names[date.getMonth()]+"-"+date.getFullYear()+'</div>';
       if(rec.data.deleted)
             v='<del>'+v+'</del>';
         return v;
    },
    linkDeletedRenderer: function(v,m,rec) {
        if(rec.data.deleted){
            v="<span class='deletedlink'><del>"+v+"</del></span>";
        }else if(rec.data.isOpeningBalanceTransaction && !rec.data.isNormalTransaction){
              v="<span class='EntryNumPedding'>"+v+"</span>";
        }else{
            v= "<a class='jumplink' href='#'>"+v+"</a>";
        }

        return v;
    },
        linkRendererForGSTR: function(v,m,rec) {
     
            v= "<a class='jumplink' href='#'>"+v+"</a>";
        return v;
    },
    amountRendererForExport: function(v) {
        var currSymb = WtfGlobal.getCurrencySymbol();
        if (v != "" && v != undefined && currSymb != undefined) {
            return '<div align="right">' + parseFloat(v).toFixed(2) + '</div>';
        } else {
            return '<div align="right">' + parseFloat(0).toFixed(2) + '</div>';
        }
    },
    linkDeletedRendererForPRandRFQ: function(v,m,rec) {
        var returnString = "";
        if(rec.data.deleted){
            returnString="<span class='deletedlink'><del>"+v+"</del></span>";
        }else if(rec.data.isOpeningBalanceTransaction && !rec.data.isNormalTransaction){
              returnString=v;
        }else{
            var moduleid = rec.data.moduleid ;
            var arr = v.split(",");
            var mid = "'" + moduleid + "'";
            if(moduleid== Wtf.Acc_Purchase_Requisition_ModuleId){
                mid = "'" + Wtf.Acc_RFQ_ModuleId + "'";
            } else if(moduleid== Wtf.Acc_RFQ_ModuleId){
                mid = "'" + Wtf.Acc_Purchase_Requisition_ModuleId + "'";
            }
            for(var i = 0; i< arr.length ; i++){
                var number = arr[i];
                var number1 = "'"+number+"'";
                returnString += "<a class='jumplink' href='#' onclick=callViewMode("+encodeURIComponent(number1)+","+mid+") href=#>"+number+"</a> ";
                if( i != arr.length -1){
                    returnString += "</br>";
                }
            }
        }

        return returnString;
    },
    linkRendererForAll: function (v, m, rec) {
        var returnString = "";
        var mid = "'" + rec.data.moduleid + "'";
        var value = "'" + v + "'";
        var cntype = "'" + rec.data.cntype + "'";
        if (rec.data.isOpeningBalanceTransaction) {
            returnString="<span class='EntryNumPedding'>"+v+"</span>";
        } else {
            returnString += "<a class='jumplink' href='#' onclick=WtfGlobal.callViewMode(" + encodeURIComponent(value) + "," + mid + "," + cntype + ") href=#>" + v + "</a> ";
        }
        return returnString;
    },
    onlyDateRendererDateTZ: function(v) {
        if(v!=""){
            v=new Date(v*1);
            if(!v||!(v instanceof Date)) return v;
            //      return new Date(v.getTime()+1*(v.getTimezoneOffset()*60000+Wtf.pref.tzoffset)).format(WtfGlobal.getOnlyDateFormat());
            return new Date(v.getTime()).format(WtfGlobal.getOnlyDateFormat());
        }else{
            return "";
        }
    },
    getMonthQuarterdata: function(year,s,e) {
        var date = Wtf.serverDate.add(Date.MONTH, -1);
//        var date = s.add(Date.MONTH, -1);
        var p1 = date.getFirstDateOfMonth() 
        var prstartdate=p1.dateFormat("y-m-d")
        var p2 = date.getLastDateOfMonth()
        var prenddate=p2.dateFormat("y-m-d")
       return [{'0': {"quarterendDate": year+"-12-31", "quarterstartDate": year+"-01-01"}},
            {'1': {"quarterendDate": year+prenddate.substring(2,9), "quarterstartDate": year+prstartdate.substring(2,9)}},
            {'2': {"quarterendDate": year+"-03-31", "quarterstartDate": year+"-01-01"}},
            {'3': {"quarterendDate": year+"-06-30", "quarterstartDate": year+"-04-01"}},
            {'4': {"quarterendDate": year+"-09-30", "quarterstartDate": year+"-07-01"}},
            {'5': {"quarterendDate": year+"-12-31", "quarterstartDate": year+"-10-01"}}
        ];
    },
     /**
     *this method check the allow zero quantity functionality is activated
     * or nor in system preferences
     **/
    checkAllowZeroQuantityForProduct:function(moduleid){
        var allowZeroQuantity=false;
        if(moduleid!=undefined && (Wtf.account.companyAccountPref.allowZeroQuantityInDO && moduleid==Wtf.Acc_Delivery_Order_ModuleId) || (Wtf.account.companyAccountPref.AllowZeroQuantityInGRO && moduleid==Wtf.Acc_Goods_Receipt_ModuleId)|| (Wtf.account.companyAccountPref.allowZeroQuantityInSR && moduleid==Wtf.Acc_Sales_Return_ModuleId)|| (Wtf.account.companyAccountPref.allowZeroQuantityInPR && moduleid==Wtf.Acc_Purchase_Return_ModuleId)||
            (Wtf.account.companyAccountPref.allowZeroQuantityInCQ && moduleid==Wtf.Acc_Customer_Quotation_ModuleId )|| (Wtf.account.companyAccountPref.AllowZeroQuantityInVQ && moduleid==Wtf.Acc_Vendor_Quotation_ModuleId)||(Wtf.account.companyAccountPref.allowZeroQuantityInSI && moduleid == Wtf.Acc_Invoice_ModuleId )|| (Wtf.account.companyAccountPref.allowZeroQuantityInSO && moduleid==Wtf.Acc_Sales_Order_ModuleId)||
            (Wtf.account.companyAccountPref.allowZeroQuantityInPI && moduleid==Wtf.Acc_Vendor_Invoice_ModuleId)|| (Wtf.account.companyAccountPref.allowZeroQuantityInPO && moduleid==Wtf.Acc_Purchase_Order_ModuleId)){
            
            allowZeroQuantity=true;
        }
        return allowZeroQuantity;
    },
    setDateToolTipAfterselectQuarterYear: function(config) {
        Wtf.QuickTips.register({
            target: config.getEl(),
            text: config.value
        });
    },
    callViewMode: function(number, moduleid, cntype,isDashLink,isDraft) {
        var url = "";
        number = decodeURIComponent(number);
        if (moduleid == Wtf.Acc_Purchase_Requisition_ModuleId || moduleid == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId) {
            url = "ACCPurchaseOrderCMN/getRequisitions.do";
        } else if (moduleid == Wtf.Acc_Vendor_Quotation_ModuleId || moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId) {
            url = "ACCPurchaseOrderCMN/getQuotations.do";
        } else if (moduleid == Wtf.Acc_Purchase_Order_ModuleId || moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) {
            url = "ACCPurchaseOrderCMN/getPurchaseOrdersMerged.do";
        } else if (moduleid == Wtf.Acc_Goods_Receipt_ModuleId) {
            url = "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
        } else if (moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || moduleid == Wtf.Acc_Cash_Purchase_ModuleId) {
            url = "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
        } else if (moduleid == Wtf.Acc_Make_Payment_ModuleId) {
            url = "ACCVendorPaymentCMN/getPayments.do";
        } else if (moduleid == Wtf.Acc_RFQ_ModuleId) {
            url = "ACCPurchaseOrderCMN/getRFQs.do";
        } else if (moduleid == Wtf.Acc_GENERAL_LEDGER_ModuleId) {
            url = "ACCReports/getJournalEntry.do";
        } else if (moduleid == Wtf.Acc_Debit_Note_ModuleId) {
            url = "ACCDebitNote/getDebitNoteMerged.do";
        } else if (moduleid == Wtf.Acc_Customer_Quotation_ModuleId) {
            url = "ACCSalesOrderCMN/getQuotations.do";
        } else if (moduleid == Wtf.Acc_Sales_Order_ModuleId) {
            url = "ACCSalesOrderCMN/getSalesOrdersMerged.do";
        } else if (moduleid == Wtf.Acc_Invoice_ModuleId || moduleid == Wtf.Acc_Cash_Sales_ModuleId) {
            url = "ACCInvoiceCMN/getInvoicesMerged.do";
        } else if (moduleid == Wtf.Acc_Delivery_Order_ModuleId) {
            url = "ACCInvoiceCMN/getDeliveryOrdersMerged.do";
        }else if (moduleid == Wtf.Acc_Receive_Payment_ModuleId) {
            url = "ACCReceiptCMN/getReceipts.do";
        } else if (moduleid == Wtf.Acc_Credit_Note_ModuleId) {
            url = "ACCCreditNote/getCreditNoteMerged.do";
        } else if (moduleid == Wtf.Acc_Purchase_Return_ModuleId) {
            url = "ACCGoodsReceiptCMN/getPurchaseReturn.do";
        } else if (moduleid == Wtf.Acc_Sales_Return_ModuleId) {
            url = "ACCInvoiceCMN/getSalesReturn.do";
        }

        Wtf.Ajax.requestEx({
            url: url,
            params: {
                linknumber: number,
                CashAndInvoice: true,
                cntype: cntype,
                isDraft:isDraft,
                pendingapproval: (isDashLink==true) ? true : false,
                pendingApproval: (isDashLink==true) ? ((moduleid == Wtf.Acc_GENERAL_LEDGER_ModuleId) ? true : false) : false,
                isFixedAsset: (moduleid == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId || moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId || moduleid == Wtf.Acc_FixedAssets_RFQ_ModuleId) ? true : false
            }
        }, this, function(resp) {
            if (resp.data && resp.data[0]) {
                var rec = {};
                rec["data"] = resp.data[0];
                rec["json"] = resp.data[0];
                if (rec.data.date) {
                    var date = rec.data.date;
                    var d = Date.parse(date);
                    var datenew = new Date(d);
                    if (datenew) {
                        rec.data.date = datenew;
                    }
                }
                if (rec.data.linkingdate) {
                    var date = rec.data.linkingdate;
                    var d = Date.parse(date);
                    var datenew = new Date(d);
                    if (datenew) {
                        rec.data.linkingdate = datenew;
                    }
                }
                if (rec.data.billdate) {
                    var date = rec.data.billdate;
                    var d = Date.parse(date);
                    var datenew = new Date(d);
                    if (datenew) {
                        rec.data.billdate = datenew;
                    }
                }
                if (rec.data.entrydate) {
                    var date = rec.data.entrydate;
                    var d = Date.parse(date);
                    var datenew = new Date(d);
                    if (datenew) {
                        rec.data.entrydate = datenew;
                    }
                }
                if (rec.data.validdate) {
                    var date = rec.data.validdate;
                    var d = Date.parse(date);
                    var datenew = new Date(d);
                    if (datenew) {
                        rec.data.validdate = datenew;
                    }
                }
                if (rec.data.shipdate) {
                    var date = rec.data.shipdate;
                    var d = Date.parse(date);
                    var datenew = new Date(d);
                    if (datenew) {
                        rec.data.shipdate = datenew;
                    }
                }
                if (rec.data.duedate) {
                    var date = rec.data.duedate;
                    var d = Date.parse(date);
                    var datenew = new Date(d);
                    if (datenew) {
                        rec.data.duedate = datenew;
                    }
                }
                if (moduleid == Wtf.Acc_Purchase_Requisition_ModuleId) {
                    callViewPurchaseReq(true, rec, rec.data.billid);
                } else if (moduleid == Wtf.Acc_Vendor_Quotation_ModuleId) {
                    callViewVendorQuotation(true, rec.data.billid, rec, false);
                } else if (moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
                    callViewPurchaseOrder(true, rec, rec.data.billid + "Purchase Order", false);
                } else if (moduleid == Wtf.Acc_Goods_Receipt_ModuleId) {
                    callViewGoodsReceiptDelivery(true, rec, rec.data.billid)
                } else if (moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
                    callViewGoodsReceipt(rec, 'ViewGoodsReceipt', rec.data.isexpenseinv);
                } else if (moduleid == Wtf.Acc_Make_Payment_ModuleId) {
                    callViewPaymentNew(rec, 'ViewPaymentMade', false);
                }else if (moduleid == Wtf.Acc_RFQ_ModuleId) {
                    var PR_MEMOS = "1) " + rec.data.billno + " - " + rec.data.memo + "\n";
                    var PR_IDS = rec.data.billid;
                    if (!rec.data.personid) {
                        rec.data.personid = "";
                    }
                    callViewRequestForQuotation(true, PR_IDS, PR_MEMOS, rec);
                } else if (moduleid == Wtf.Acc_GENERAL_LEDGER_ModuleId) {
                    callViewJournalEntryTab(true, rec, rec.data.billid, rec.data.typeValue, null)
                } else if (moduleid == Wtf.Acc_Debit_Note_ModuleId) {
                    callViewDebitNote("ViewDebitNote" + rec.data.noteno, true, false, rec.data.cntype, rec, null, true);
                } else if (moduleid == Wtf.Acc_Customer_Quotation_ModuleId) {
                    callViewQuotation(true, rec.data.billid, rec, true);
                }else if (moduleid == Wtf.Acc_Sales_Order_ModuleId) {
                    callViewSalesOrder(true, rec, rec.data.billid, false);
                } else if (moduleid == Wtf.Acc_Invoice_ModuleId) {
                    callViewInvoice(rec, 'ViewCashReceipt');
                } else if (moduleid == Wtf.Acc_Delivery_Order_ModuleId) {
                    callViewDeliveryOrder(true, rec, rec.data.billid, false, false);
                } else if (moduleid == Wtf.Acc_Receive_Payment_ModuleId) {
                    callViewPaymentNew(rec, 'ViewReceivePayment', true);
                } else if (moduleid == Wtf.Acc_Credit_Note_ModuleId) {
                    callViewCreditNote("ViewcreditNote" + rec.data.noteno, true, true, rec.data.cntype, rec, null);
                } else if (moduleid == Wtf.Acc_Purchase_Return_ModuleId) {
                    callViewPurchaseReturn(true, rec, rec.data.billid, false, rec.data.isNoteAlso);
                } else if (moduleid == Wtf.Acc_Sales_Return_ModuleId) {
                    callViewSalesReturn(true, rec, rec.data.billid, false, rec.data.isNoteAlso);
                } else if (moduleid == Wtf.Acc_Cash_Purchase_ModuleId) {
                    callViewPaymentReceipt(rec, 'ViewPaymentReceipt', rec.data.isexpenseinv);
                } else if (moduleid == Wtf.Acc_Cash_Sales_ModuleId) {
                    callViewCashReceipt(rec, 'ViewCashReceipt');
                } else if (moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) {
                    callViewFixedAssetPurchaseOrder(true, rec, rec.data.billid, false, this, this, true);
                } else if (moduleid == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId) {
                    callViewFixedAssetPurchaseReq(true, rec, rec.data.billid, undefined, true);
                } else if (moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId) {
                    callViewFixedAssetVendorQuotation(false, rec.data.billid, rec, true);
                }
            }
        });
    },
    multipleJELinkDeletedRenderer: function (v, m, rec) {       // handle multiple links for in single row
        var b = "";
        if (rec.data.deleted) {
            b = "<span class='deletedlink'><del>" + v + "</del></span>";
        } else {
            var a = v.split(",");
            var ids = rec.data.journalentryid;
            var id = ids.split(",");
            var jedates = "";
            if(rec.data.journalentrydate) {
                jedates = rec.data.journalentrydate.split(",");
            }
            for (var i = 0; i < a.length; i++) {
                var jedate = '';
                if(jedates.length>0) {
                    jedate = "jedate="+jedates[i];
                }
                b += "<a style='padding-bottom: 2px;' class='jumplink' name=" + id[i] + " "+jedate+" href='#'>" + a[i] + "</a>";
            }
        }
        return b;
    },
    jERendererForGST: function (v, m, rec) {       // handle multiple links for in single row
        m.attr = "Wtf:qtip='" + v + "' Wtf:qtitle='Journal Entry Number' ";
        var b = "";
        var id = rec.data.jeid;
        var jedate = "";
        if(rec.data.jedate) {
            jedate = rec.data.jedate;
        }
        
       b = "<a style='padding-bottom: 2px;' class='jumplink' name=" + id + " "+"jedate="+jedate+" href='#'>" + v + "</a>";
       return b;
    },
    renderDeletedEmailTo: function(value,p,record){
        value = "<div class='mailTo'><a href=mailto:"+value+">"+value+"</a></div>";
        if(record.data.deleted) {
             value='<del>'+value+'</del>';
        }
        return value;
    },
    renderDeletedEmailsTo: function(value,p,record){
        var selectedEmailArray = value.split(',');
        var tempValue="";
        value = value.replace(/\'/g, "&#39;");
        value = value.replace(/\"/g, "&#34");
        value = value.replace(/\,/g, "&#44 ");
        if(selectedEmailArray.length>0){
            for(var i=0;i<selectedEmailArray.length; i++){
                if(i==selectedEmailArray.length-1){
                tempValue+="<a href=mailto:"+selectedEmailArray[i]+"><span class=memo_custom  wtf:qtip='" + value + "'>"+selectedEmailArray[i]+"</span></a>";
                }else{
                tempValue+="<a href=mailto:"+selectedEmailArray[i]+"><span class=memo_custom  wtf:qtip='" + value + "'>"+selectedEmailArray[i]+"</span></a>,";
                }
            }
        }
        return tempValue;
    },
    emailRendererWithoutLink: function(value,p,record){
        var selectedEmailArray = value.split(',');
        var tempValue="";
        value = value.replace(/\'/g, "&#39;");
        value = value.replace(/\"/g, "&#34");
        value = value.replace(/\,/g, "&#44 ");
        if(selectedEmailArray.length>0){
            for(var i=0;i<selectedEmailArray.length; i++){
                if(i==selectedEmailArray.length-1){
                tempValue+="<span class=memo_custom  wtf:qtip='" + value + "'>"+selectedEmailArray[i]+"</span></a>";
                }else{
                tempValue+="<span class=memo_custom  wtf:qtip='" + value + "'>"+selectedEmailArray[i]+"</span></a>,";
                }
            }
        }
        return tempValue;
    },

    renderDeletedContactToSkype: function(value,p,record){
        value = "<div class='mailTo'><a href=skype:"+value+"?call>"+value+"</a></div>";
        if(record.data.deleted) {
             value='<del>'+value+'</del>';
        }
        return value;
    },

    getCurrencyName: function() {
        return Wtf.pref.CurrencyName;
    },
    getCurrencyID: function() {
        return Wtf.pref.Currencyid;
    },

    getCurrencySymbol: function() {
        return Wtf.pref.CurrencySymbol;
    },

    getCurrencySymbolForForm: function() {
        return "<span class='currency-view'>"+WtfGlobal.getCurrencySymbol()+"</span>";
    },
    getFieldLabel:function(text){
        return "<span class='fieldlabel'>"+text+"</span>";
    },
    singaporecountry: function() {
        return Wtf.account.companyAccountPref.countryid==Wtf.Country.SINGAPORE;
    },


    linkRenderer: function(value) {
        return "<a class='jumplink' href='#'>"+value+"</a>";
    },
    accountLinkRenderer: function(value) {
        return "<a href='#'>"+value+"</a>";
    },
     currencyLinkRenderer: function(text,value) {
        return text+"<a class='currencyjumplink' href='#'>"+value+"</a>";
    },
    emptyGridRenderer: function(value) {
        return "<div class='grid-link-text'>"+value+"</div>";
    },

    searchRecord : function(store, ID, idname) {
        var index =  store.findBy(function(record) {
            if(record.get(idname)==ID)
                return true;
            else
                return false;
        });
        if(index == -1)
            return null;

        return store.getAt(index);
    },
    duplicatesearchRecord : function(store, ID, idname) {
        var index =  store.findBy(function(record) {
            if(record.get(idname) === ID)
                return true;
            else
                return false;
        });
        if(index == -1)
            return null;

        return store.getAt(index);
    },
    /**
     * ERP-34294
     * Search GST Location dimension without case sensitive or not
     */
    searchRecordIsWithCase: function (store, ID, idname, isCase) {
        var index = store.findBy(function (record) {
            var _S_Val = isCase ? record.get(idname) : (record.get(idname).trim().toUpperCase()); //Store Vlaue
            var _F_Val = isCase ? ID : (ID.trim().toUpperCase()); // Find Value
            if (_S_Val == _F_Val) {
                return true;
            } else {
                return false;
            }
        });
        if (index == -1)
            return null;
        return store.getAt(index);
    },
//    isDefaultReferralKey : function() {
//        return Wtf.account.companyAccountPref.referralkey!=Wtf.defaultReferralKey;
//    },
    
resetCustomFields : function(tagsFieldset){ // For reset Custom Fields, Check List and Custom Dimension
    this.tagsFieldset=tagsFieldset;
    var customFieldArray = this.tagsFieldset.customFieldArray;  // Reset Custom Fields
    if(customFieldArray!=null && customFieldArray!=undefined && customFieldArray!="" ) {
        for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
            var fieldId = customFieldArray[itemcnt].id
            if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
                Wtf.getCmp(fieldId).reset();
            }
        }
    }
    var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  // Reset Check List
    if(checkListCheckBoxesArray!=null && checkListCheckBoxesArray!=undefined && checkListCheckBoxesArray!="" ) {
        for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
            var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
            if (Wtf.getCmp(checkfieldId) != undefined) {
                Wtf.getCmp(checkfieldId).reset();
            }
        }
    }
    
    var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  // Reset Custom Dimension
    if(customDimensionArray!=null && customDimensionArray!=undefined && customDimensionArray!="" ) {
        for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
            var fieldId1 = customDimensionArray[itemcnt1].id
            if (Wtf.getCmp(fieldId1) != undefined) {
                Wtf.getCmp(fieldId1).reset();
            }
        }
    }
},
    searchRecordIndex : function(store, ID, idname) {  //combo.store,value,combo.valueField
        var index =  store.findBy(function(record) {
            if(record.get(idname)==ID)
                return true;
            else
                return false;
        });
        return index;
    },

    queryBy : function(store,key,value){
        var array=[];
        array = store.queryBy(function(record){
            return (record.get('chequeno') == value);
        },this);
        return array;
    },
    setDefaultWarehouseLocation: function(obj,proRecord,isdquantity,grid,index){
        if(obj){
               var quantity=(isdquantity!= undefined && isdquantity ==true)?obj.record.data.dquantity:obj.record.data.quantity;
        }else if(grid){
               var quantity=(isdquantity!= undefined && isdquantity ==true)?proRecord.data.dquantity:proRecord.data.quantity;
        }
        if(((proRecord.data.isLocationForProduct && proRecord.data.location!=undefined && proRecord.data.location!="") || (proRecord.data.isWarehouseForProduct && proRecord.data.warehouse!=undefined && proRecord.data.warehouse!="")) && !proRecord.data.isSerialForProduct && !proRecord.data.isBatchForProduct){
                var filterJson="";
                filterJson='[';
                filterJson+='{"location":"';
                filterJson+= (proRecord.data.isLocationForProduct && !Wtf.isEmpty(proRecord.data.location)) ? proRecord.data.location : "";
                filterJson+='","warehouse":"';
                filterJson+= (proRecord.data.isWarehouseForProduct && !Wtf.isEmpty(proRecord.data.warehouse)) ? proRecord.data.warehouse : "";
                filterJson+='","productid":"'+proRecord.data.productid+'","documentid":"","purchasebatchid":"","quantity":"'+(obj?quantity*obj.record.get("baseuomrate"):quantity*proRecord.get("baseuomrate"))+'"},';
                filterJson=filterJson.substring(0,filterJson.length-1);
                filterJson+="]";
                if(obj){
                  obj.record.set("batchdetails",filterJson);
                }else if(grid){
                    grid.getStore().getAt(index).set("batchdetails",filterJson);
                }
            }
    },
    /**
     * 
     * @param {type} gridrec
     * @param {type} proRecord
     * @returns 
     * @ Desc : Set dafult warehouse and location for assembly items
     */
    setDefaultWarehouseLocationforAssembly: function(gridrec, proRecord) {
        var quantity = gridrec.data.quantity;
        if (((proRecord.data.isLocationForProduct && proRecord.data.location != undefined && proRecord.data.location != "") || (proRecord.data.isWarehouseForProduct && proRecord.data.warehouse != undefined && proRecord.data.warehouse != "")) && !proRecord.data.isSerialForProduct && !proRecord.data.isBatchForProduct) {
            var filterJson = "";
            filterJson = '[';
            filterJson += '{"location":"';
            filterJson += (proRecord.data.isLocationForProduct && !Wtf.isEmpty(proRecord.data.location)) ? proRecord.data.location : "";
            filterJson += '","warehouse":"';
            filterJson += (proRecord.data.isWarehouseForProduct && !Wtf.isEmpty(proRecord.data.warehouse)) ? proRecord.data.warehouse : "";
            filterJson += '","productid":"' + proRecord.data.productid + '","documentid":"","purchasebatchid":"","quantity":"' + (quantity) + '"},';
            filterJson = filterJson.substring(0, filterJson.length - 1);
            filterJson += "]";
            if (gridrec) {
                gridrec.set("batchdetails", filterJson);
            }
        }
    },
    conventInDecimal: function(v,symbol) {
        //To do - Need to write generic method/code for this to remove hardcoded logic.
//            if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==4){
//                v = (Math.round((v-0)*10000))/10000;
//                v = (v == Math.floor(v)) ? v + ".0000" : (parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
//            }else if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==3){
//                v = (Math.round((v-0)*1000))/1000;
//                v = (v == Math.floor(v)) ? v + ".000" : (parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
//            }else {
//                v = (Math.round((v-0)*100))/100;
//                v = (v == Math.floor(v)) ? v + ".00" : (parseFloat(v).toFixed(2));
//            }
	    v= Number(v);	
            v=getRoundedAmountValue(v);
            v=parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
            v = String(v);
            var ps = v.split('.');
            var whole = ps[0];
            
            if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==4){
                var sub = ps[1] ? '.'+ ps[1] : '.0000';
            }else if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==3){
                sub = ps[1] ? '.'+ ps[1] : '.000';
            }else{
                sub = ps[1] ? '.'+ ps[1] : '.00';
            }  
            if(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA){
                var r = /(\d+)(\d{3})/;
                while (r.test(whole)) {
                    whole = whole.replace(r, '$1' + ',' + '$2');
                }
            }else{
                whole = whole.replace(/(\d)(?=(\d\d)+\d$)/g, "$1,");
            }
            v = whole + sub;
            if(v.charAt(0) == '-') {
//                v= '-'+symbol + " " + v.substr(1);
              v= "(<label style='color:red'>"+symbol + " " + v.substr(1)+"</label>)";
            } else
                v=symbol + " " + v;
            return v;
    },
    convertInDecimalWithoutCurrencySymbol: function(v) {
        
	    v= Number(v);	
            v=getRoundedAmountValue(v);
            v=parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
            v = String(v);
            var ps = v.split('.');
            var whole = ps[0];
            
            if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==4){
                var sub = ps[1] ? '.'+ ps[1] : '.0000';
            }else if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==3){
                sub = ps[1] ? '.'+ ps[1] : '.000';
            }else{
                sub = ps[1] ? '.'+ ps[1] : '.00';
            }  
            if(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA){
                var r = /(\d+)(\d{3})/;
                while (r.test(whole)) {
                    whole = whole.replace(r, '$1' + ',' + '$2');
                }
            }else{
                whole = whole.replace(/(\d)(?=(\d\d)+\d$)/g, "$1,");
            }
            v = whole + sub;

        return v;
    },
    RitchTextBoxSetting:function(grid, rowIndex, columnIndex, e,readOnly){
        var record = grid.getStore().getAt(rowIndex);
        this.isEdit=grid.parentObj?grid.parentObj.isEdit:false;
        var fieldName= grid.getColumnModel().getDataIndex(columnIndex);
        if (e.getTarget(".richtext")) {
            
                value = record.get(fieldName);
                if (value == "" && record.data.productid != "" && (!this.isEdit || (this.isEdit && record.modified.pid != undefined )) && !Wtf.getCmp(record.id + record.data.pid + fieldName)) {
                   
                /*
                 * Here Data will be fetched for Rich Text Area.
                 */
                    Wtf.Ajax.requestEx({
                        url: "ACCProduct/getRichTextArea.do",
                        params: {
                            fieldName: fieldName,
                            productid: record.data.productid
                        }
                    },
                    this,
                    function (response) {
                        if (response.success) {
                            value = response.data;
                            record.set(fieldName,value);                            
                        }
                        
                        this.richText = new Wtf.RichTextArea({
                            rec: record,
                            fieldName: fieldName,
                            val: value ? value : "",
                            readOnly: this.readOnly,
                            id: record.id + record.data.pid + fieldName                            
                        });

                        this.richText.win.on('hide', function () {
                            /*
                             * Here Flag is set so value for This field will not be fetched at back end
                             */
                            record.set("richText"+fieldName, "changed");
                        }, this);
                        
                    },
                    function (response) {
                        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                    });
                } else {
                    /*
                     * If Data for Rich Text Area is already present and ajax call not required then value will be assigned here..
                     */
                    this.richText = new Wtf.RichTextArea({
                        rec: record,
                        fieldName: fieldName,
                        val: value ? value : "",
                        readOnly: this.readOnly,
                        id: record.id + record.data.pid + fieldName                        
                    });

                    this.richText.win.on('hide', function () {
                        record.set("richText"+fieldName,"changed");  
                    }, this);    
                }
            } 
    if(Wtf.account.companyAccountPref.proddiscripritchtextboxflag!=0 && !readOnly){
        if(fieldName == "description" || fieldName == "desc"){
            
            if (Wtf.account.companyAccountPref.proddiscripritchtextboxflag == 1) {
                this.prodDescTextArea = new Wtf.form.TextArea({// Just make new Wtf.form.TextArea to new Wtf.form.HtmlEditor to fix ERP-8675
                    name: 'remark',
                    id: 'descriptionRemarkTextAreaId'
                });
            } else if (Wtf.account.companyAccountPref.proddiscripritchtextboxflag == 2) {
                this.prodDescTextArea = new Wtf.form.HtmlEditor({
                    name: 'remark',
                    id: 'descriptionRemarkTextAreaId'
                });
            }
           
            if(fieldName == "description"){
                var val=record.data.description;
            }else{
                var val=record.data.desc;
            }
            
//            val = val.replace(/(<([^>]+)>)/ig,"");
            this.prodDescTextArea.setValue(val);
            if(record.data.productid !=undefined && record.data.productid !=""){
                var descWindow=Wtf.getCmp(this.id+'DescWindow')
                if(descWindow==null){
                    var win = new Wtf.Window
                    ({
                        width: 560,
                        height:310,
                        title:record.data.productname+" "+WtfGlobal.getLocaleText("acc.gridproduct.discription"),
                        layout: 'fit',
                        id:this.id+'DescWindow',
                        bodyBorder: false,
                        closable:   true,
                        resizable:  false,
                        modal:true,
                        items:[this.prodDescTextArea],
                        bbar:
                        [{
                            text: 'Save',
                            iconCls: 'pwnd save',
                            handler: function()
                            {
                                if(fieldName == "description"){
                                    record.set('description',  Wtf.get('descriptionRemarkTextAreaId').getValue());
                                }else{
                                    record.set('desc',  Wtf.get('descriptionRemarkTextAreaId').getValue());
                                }
                                win.close();   
                            }
                        },{
                            text: 'Cancel',
                            handler: function()
                            {
                                win.close();   
                            }
                        }]
                    });
                }
                win.show(); 
            }
            return false;
        }
    }
            
    },
        conventInDecimalWithLink: function(v,symbol) {
	    v= Number(v);	
            v=getRoundedAmountValue(v);
            v=parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
            v = String(v);
            var ps = v.split('.');
            var whole = ps[0];            
            if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==4){
                var sub = ps[1] ? '.'+ ps[1] : '.0000';
            }else if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==3){
                sub = ps[1] ? '.'+ ps[1] : '.000';
            }else{
                sub = ps[1] ? '.'+ ps[1] : '.00';
            }  
            var r = /(\d+)(\d{3})/;
            while (r.test(whole)) {
                whole = whole.replace(r, '$1' + ',' + '$2');
            }
            v = whole + sub;
            if(v.charAt(0) == '-') {
              v= "(<a style='color:red;margin-right: 15px'href='#' >"+symbol + " " + v.substr(1)+")</a>";
            } else
                v="(<a style='color:black;margin-right: 15px'href='#' >"+symbol + " " + v+")</a>";
            return v;
    },
        conventInDecimalWithLink: function(v,symbol) {
	    v= Number(v);	
            v=getRoundedAmountValue(v);
            v=parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
            v = String(v);
            var ps = v.split('.');
            var whole = ps[0];            
            if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==4){
                var sub = ps[1] ? '.'+ ps[1] : '.0000';
            }else if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==3){
                sub = ps[1] ? '.'+ ps[1] : '.000';
            }else{
                sub = ps[1] ? '.'+ ps[1] : '.00';
            }  
            var r = /(\d+)(\d{3})/;
            while (r.test(whole)) {
                whole = whole.replace(r, '$1' + ',' + '$2');
            }
            v = whole + sub;
            if(v.charAt(0) == '-') {
              v= "<a style='color:red;margin-right: 15px'href='#' >"+ v.substr(1)+" "+symbol+"</a>";
            } else
                v="<a style='color:black;margin-right: 15px'href='#' >"+ v+" "+symbol+"</a>";
            return v;
    },
    conventInDecimalWithoutSymbol: function(v) {
//            if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==4){
//                v = (Math.round((v-0)*10000))/10000;
//                v = (v == Math.floor(v)) ? v + ".0000" : (parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
//            }else if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==3){
//                v = (Math.round((v-0)*1000))/1000;
//                v = (v == Math.floor(v)) ? v + ".000" : (parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
//            }else {
//                v = (Math.round((v-0)*100))/100;
//                v = (v == Math.floor(v)) ? v + ".00" : (parseFloat(v).toFixed(2));
//            }
	    v= Number(v);
            v=getRoundedAmountValue(v);
             v=parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
            v = String(v);
            var ps = v.split('.');
            var whole = ps[0];
            if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==4){
                var sub = ps[1] ? '.'+ ps[1] : '.0000';
            }else if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==3){
                sub = ps[1] ? '.'+ ps[1] : '.000';
            }else{
                sub = ps[1] ? '.'+ ps[1] : '.00';
            }  
            var r = /(\d+)(\d{3})/;
//            while (r.test(whole)) {
//                whole = whole.replace(r, '$1' + ',' + '$2');
//            }
            v = whole + sub;
            
            return parseFloat(v);
    },
    
    conventCurrencyDecimal: function(v,symbol) {
            v = (Math.round((v-0)*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
            v = (v == Math.floor(v)) ? v + ".00" : ((v*10 == Math.floor(v*10)) ? v + "0" : v);
            v = String(v);
            var ps = v.split('.');
            var whole = ps[0];
            var sub = ps[1] ? '.'+ ps[1] : '.00';
            var r = /(\d+)(\d{3})/;
            while (r.test(whole)) {
                whole = whole.replace(r, '$1' + ',' + '$2');
            }
            v = whole + sub;
            if(v.charAt(0) == '-')
                v= '-'+symbol + " " + v.substr(1);
            else
                v=symbol + " " + v;
            return v;
    },

     currencyRenderer: function(value,isCheckCenterAlign) {
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign[0]);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
            v= WtfGlobal.conventInDecimal(v,WtfGlobal.getCurrencySymbol())
            if(isCenterAlign)
                 return '<div>'+v+'</div>';
        return '<div class="currency">'+v+'</div>';
    },
    currencyRendererWithoutCurrencySymbol: function(value,isCheckCenterAlign) {
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign[0]);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
            v= WtfGlobal.conventInDecimal(v,"")
            if(isCenterAlign)
                 return '<div>'+v+'</div>';
        return '<div class="currency">'+v+'</div>';
    },
     currencyRendererWithLink: function(value,isCheckCenterAlign) {
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign[0]);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
            v= WtfGlobal.conventInDecimal(v,WtfGlobal.getCurrencySymbol())
            if(isCenterAlign)
                 return '<div>'+v+'</div>';
        return '<a class="currencyjumplink" href="#" >'+v+'</a>';
    },
    

    withoutRateCurrencySymbolForAccountCurrency: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data.accountcurrencysymbol==null||rec.data['accountcurrencysymbol']==undefined||rec.data['accountcurrencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['accountcurrencysymbol']);
        Wtf.reconcillationAccountCurrencySymbol = symbol;
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        v= WtfGlobal.conventInDecimal(v,symbol)
        return '<div class="currency">'+v+'</div>';
        
    },
    
    withoutRateCurrencySymbolForGSTFM5: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        v= WtfGlobal.conventInDecimal(v,symbol)
        return '<div class="currency">'+v+'</div>';
        
    },
    /*
     * Added a new formatter for appending the country currency symbol
     */
    withoutRateCountryWiseCurrencySymbol: function(value,m,rec) {
    
        var symbol = (Wtf.account.companyAccountPref.countryid==Wtf.Country.SINGAPORE) ? Wtf.CurrencySymbols.SGD : Wtf.CurrencySymbols.MYR;
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        v= WtfGlobal.conventInDecimal(v,symbol)
        return '<div class="currency">'+v+'</div>';
},
    withoutRateCurrencySymbolForGSTFM5withTransactionCurrency:function(value,m,rec) {
        var symbol=((rec==undefined||rec.data.transactioncurrencysymbol==null||rec.data['transactioncurrencysymbol']==undefined||rec.data['transactioncurrencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['transactioncurrencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        v= WtfGlobal.conventInDecimal(v,symbol)
        return '<div class="currency">'+v+'</div>';
        
    },
     currencyRenderer1: function(value,isCheckCenterAlign) {
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign[0]);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
            v= WtfGlobal.conventInDecimal(v,WtfGlobal.getCurrencySymbol())
            if(isCenterAlign)
                 return v;
        return v;
    },

    withoutRateCurrencySymbolForGST: function(value,m,rec) {
   if(value >0){
        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
          v= WtfGlobal.conventInDecimal(v,symbol)
        return '<div class="currency">'+v+'</div>';
   }else{
        return '';
   } 
    },
    withoutRateCurrencySymbol: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
          v= WtfGlobal.conventInDecimal(v,symbol)
        return '<div class="currency">'+v+'</div>';
    },
    /**
     * Below renderer checks value of hideUnitPriceAmount flag to see whether or not a user has permission to view Unit Price and other amounts in transactions
     * If flag hideUnitPriceAmount is passed true then Wtf.UpriceAndAmountDisplayValue is returned instead of actual value
     */
    withoutRateCurrencySymbolWithPermissionCheck: function(value, m, rec, hideUnitPriceAmount) {
        if (hideUnitPriceAmount) {
            return Wtf.UpriceAndAmountDisplayValue;
        } else {
            return WtfGlobal.withoutRateCurrencySymbol(value, m, rec);
        }
    },
    isIndiaCountryAndGSTApplied: function () {
        var returnVal = false;
        if (WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW && Wtf.Countryid == Wtf.Country.INDIA) {
            returnVal = true;
        }
        return returnVal;
    },
    /**
     * Get US country GST applied or not
     * @returns {Boolean}
     */
    isUSCountryAndGSTApplied: function () {
        var returnVal = false;
        if (WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW && Wtf.Countryid == Wtf.Country.US) {
            returnVal = true;
        }
        return returnVal;
    },
    withoutRateCurrencySymbolTransaction: function(value,m,rec) {
        var currencysymbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var symbol=((rec==undefined||rec.data.currencysymboltransaction==null||rec.data['currencysymboltransaction']==undefined||rec.data['currencysymboltransaction']=="")?currencysymbol:rec.data['currencysymboltransaction']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
          v= WtfGlobal.conventInDecimal(v,symbol)
        return '<div class="currency">'+v+'</div>';
    },
    withoutRateCurrencySymbolTransaction: function(value,m,rec) {
        var currencysymbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var symbol=((rec==undefined||rec.data.currencysymboltransaction==null||rec.data['currencysymboltransaction']==undefined||rec.data['currencysymboltransaction']=="")?currencysymbol:rec.data['currencysymboltransaction']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
          v= WtfGlobal.conventInDecimal(v,symbol)
        return '<div class="currency">'+v+'</div>';
    },
    convertQuantityInDecimalWithLink: function(v, symbol) {
//        v = Number(v);
//        v = getRoundedAmountValue(v);
//        v = parseFloat(v).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
//        v = String(v);
//            var ps = v.split('.');
//            var whole = ps[0];
//            if (Wtf.QUANTITY_DIGIT_AFTER_DECIMAL === 4) {
//                var sub = ps[1] ? '.' + ps[1] : '.0000';
//            } else if (Wtf.QUANTITY_DIGIT_AFTER_DECIMAL === 3) {
//                sub = ps[1] ? '.' + ps[1] : '.000';
//        } else {
//                 sub = ps[1] ? '.' + ps[1] : '.00';
//            }
//            var r = /(\d+)(\d{3})/;
//            while (r.test(whole)) {
//                whole = whole.replace(r, '$1' + ',' + '$2');
//            }
//            v = whole + sub;
        if (v.charAt(0) == '-') {
            v = "<a style='color:red;margin-right: 15px'href='#' >" + v.substr(1) + " " + symbol + "</a>";
        } else
            v = "<a style='color:black;margin-right: 15px'href='#' >" + v + " " + symbol + "</a>";
        return v;
    },
    
    withoutRateCurrencySymbolforDebit: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
          v= WtfGlobal.conventInDecimal(v,symbol)
        if(rec.data["type"] == "Cash Sale")
            return '<div class="currency" style="color:blue">'+v+'</div>';
        else
            return '<div class="currency">'+v+'</div>';
    },
    globalCurrencySymbolforDebit: function(value,m,rec) {
        //var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
          v= WtfGlobal.conventInDecimal(v,WtfGlobal.getCurrencySymbol())
        if(rec.data["type"] == "Cash Sale")
            return '<div class="currency" style="color:blue">'+v+'</div>';
        else
            return '<div class="currency">'+v+'</div>';
    },
    withoutRateCurrencySymbolforCredit: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
          v= WtfGlobal.conventInDecimal(v,symbol)
        if(rec.data["type"] == "Cash Purchase")
            return '<div class="currency" style="color:blue">'+v+'</div>';
        else
            return '<div class="currency">'+v+'</div>';
    },
    
    globalCurrencySymbolforCredit: function(value,m,rec) {
//        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
          v= WtfGlobal.conventInDecimal(v,WtfGlobal.getCurrencySymbol())
        if(rec.data["type"] == "Cash Purchase")
            return '<div class="currency" style="color:blue">'+v+'</div>';
        else
            return '<div class="currency">'+v+'</div>';
    },

     currencyRendererSymbol: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data['currencysymbol']==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
        var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
        var v;
        if(rate!=0.0)
            v=(parseFloat(value)*parseFloat(rate))/parseFloat(oldcurrencyrate);
        else
            v=(parseFloat(value)/parseFloat(oldcurrencyrate));
        if(isNaN(v)) return value;
            v= WtfGlobal.conventInDecimal(v,symbol)
        return '<div class="currency">'+v+'</div>';

    },
    currencyRendererWithoutSymbol: function(value,m,rec) {
        var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
        var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
        var v;
        if(rate!=0.0)
            v=(parseFloat(value)*parseFloat(rate));
        else
            v=(parseFloat(value)/parseFloat(oldcurrencyrate));
        if(isNaN(v)) return value;
        return v;
    },
    addCurrencySymbolOnly: function(value,symbol,isCheckCenterAlign) {
        symbol=((symbol==undefined||symbol==null||symbol=="")?WtfGlobal.getCurrencySymbol():symbol);
        symbol=((symbol.data!=undefined&&symbol.data['currencysymbol']!=null&&symbol.data['currencysymbol']!=undefined&&symbol.data['currencysymbol']!="")?symbol.data['currencysymbol']:symbol);
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign[0]);
        symbol=((symbol==undefined || symbol == ""||symbol==null)?WtfGlobal.getCurrencySymbol():symbol);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
           v= WtfGlobal.conventInDecimal(v,symbol)
            if(isCenterAlign)
                 return '<div>'+v+'</div>';
        return '<div class="currency">'+v+'</div>';
    },
   
   quantityRenderer:function(val,m,rec){
            return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
    },
   quantityaRenderer:function(val,m,rec){
            if (rec.data.acccode == "<b>Zero-Moved Products<b>" || rec.data.acccode == "<b>Zero Transaction Sales Persons<b>" || rec.data.acccode == "<b>Gross Sales For the Period<b>" || rec.data.acccode == "<b>Total of Un-Reported Sales<b>" || rec.data.acccode == "<b>Total Customers Reported<b>" || rec.data.acccode == "<b>Active Customers Not Reported<b>" || rec.data.acccode == "<b>Zero Transaction Customers<b>" || rec.data.acccode == "<b>Total Customers<b>" || rec.data.acccode == "<b>Total Products Reported<b>" || rec.data.acccode == "<b>Active Products Not Reported<b>" || rec.data.acccode == "<b>Total Products<b>" || rec.data.acccode == "<b>Total Sales Persons Reported<b>" || rec.data.acccode == "<b>Active Sales Persons Not Reported<b>" || rec.data.acccode == "<b>Total Sales Persons<b>") {
                return '';
            }else{
                return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            }
    },
    numericRenderer:function(v){
        return '<div class="currency">'+v+'</div>';
    },
    currencySummaryRenderer: function(value) {
        return WtfGlobal.summaryRenderer(WtfGlobal.currencyRenderer(value));
    },
    
    accountCurrencySummaryRenderer: function(value){
        return WtfGlobal.summaryRenderer(WtfGlobal.rateCurrencySymbolForAccountCurrency(value));
    },
    
    rateCurrencySymbolForAccountCurrency: function(value,m,rec) {
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        v= WtfGlobal.conventInDecimal(v,Wtf.reconcillationAccountCurrencySymbol)
        return '<div class="currency">'+v+'</div>';
    },
    
    currencySummaryRendererSymbol: function(value,p,r,symbol) {
        return WtfGlobal.summaryRenderer(WtfGlobal.addCurrencySymbolOnly(value,r));
    },
    summaryRenderer: function(value) {
        value = value!=undefined?value:'';  //To avoid 'undefined' static content in Cost Center Detail Report  
        return '<div class="grid-summary-common">'+value+'</div>';
    },

    boolRenderer: function(tval,fval) {
        return function(val){if(val) return tval;return fval};
    },

    validateEmail: function(value){
        return Wtf.ValidateMailPatt.test(value);
    },
    validateMultipleEmail: function(value){
        var emails = value.split(",");
        var validflag=false;
            emails.forEach(function (email) {
            email = email.trim();
            if(Wtf.ValidateMailPatt.test(email)){
                validflag=true;
            }else{
                 validflag=false;
            }
    });
    return validflag;
},
    renderEmailTo: function(value,p,record){
        return "<div class='mailTo'><a href=mailto:"+value+">"+value+"</a></div>";
    },

    validateHTField:function(value){
      return Wtf.validateHeadTitle.test(value.trim());
    },

    renderContactToSkype: function(value,p,record){
        return "<div class='mailTo'><a href=skype:"+value+"?call>"+value+"</a></div>";
    },

    validateUserid: function(value){
        return Wtf.ValidateUserid.test(value);
    },

    validateUserName: function(value){
        return Wtf.ValidateUserName.test(value.trim());
    },

    getInstrMsg: function(msg){
        return "<span style='font-size:10px !important;color:gray !important;'>"+msg+"</span>"
    },
    getOpeningDocumentDate: function(start) {
        if (Wtf.account.companyAccountPref.bbfrom) { // Get date before first financial year.
            var bbDate = Wtf.account.companyAccountPref.bbfrom;
            var monthDateStr = bbDate.format('M d');
            var openingDocDate = new Date(monthDateStr + ', ' + bbDate.getFullYear() + ' 12:00:00 AM');
            return openingDocDate;
        } else {
            return getDates(start);
        }
    },
    EnableDisable: function(userpermcode, permcode){//alert(permcode)
        if(permcode==null){
//            clog("Some Permission are undefined.\n"+userpermcode+"\n"+showCallStack());
            clog("Some Permission are undefined.\n"+userpermcode+"\n");

        }
        if (userpermcode && permcode) {
            if ((userpermcode & permcode) == permcode)
                return false;
        }
        return true;
    },
    isSaledpersonDeactivated: function (salespersoncombo, businessPerson) {
        if (businessPerson == "Customer") {
            var hasAccessFlag = false;
            var Name = "";
            var title = "";
            var personId = salespersoncombo.getValue();
            var personRec = (personId != undefined && personId != '') ? WtfGlobal.searchRecord(salespersoncombo.store, personId, salespersoncombo.valueField) : null;
            if (personRec != null) {
                var hasAccess = personRec.get('hasAccess');
                if (!hasAccess) {
                    Name = personRec.get('name');
                    hasAccessFlag = true;
                }
            }
            if (hasAccessFlag) {
                if (businessPerson == "Customer") {
                    title = ' Salesperson ';
                } else {
                    title = ' Agent ';
                }
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                    msg: title + Name + " " + WtfGlobal.getLocaleText("acc.field.iscurrentlydeactivatedsalesperson.msg"),
                    width: 370,
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this
                });

                return true;
//                this.enableSaveButtons();
//                return;
            } else {
                return false;
            }
        }else {
            return false;
        }
    },
    getIndividualProductPriceInMultiCurrency:function(rec,datewiseprice){
        if(!Wtf.account.companyAccountPref.productPriceinMultipleCurrency){ //If product in Multiple currency is not set in account preferences
            var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
            var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
            var modifiedRate;
            if(rate!=0.0)
                modifiedRate=getRoundofValueWithValues(((parseFloat(datewiseprice)*parseFloat(rate))/parseFloat(oldcurrencyrate)),Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
            else
                modifiedRate=getRoundofValueWithValues((parseFloat(datewiseprice)/parseFloat(oldcurrencyrate)),Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        }else{
                                        
            modifiedRate=datewiseprice;
        }
        return modifiedRate;
    },
    
    isMultipleContractsSelected : function(soIdsArray, postore){
    
        var isMultipleContractsSelected = false;
        //        var soIdsArray = this.PO.getValue().split(",");
        var contractIdArray = [];
            
        for(var i=0;i<soIdsArray.length; i++){
            var doRec = WtfGlobal.searchRecord(postore,soIdsArray[i],'billid');
            var contractId = doRec.get('contract');
                
            // Check whether contract Array contains same id or not if different don't let it save
            if(contractIdArray.length == 0){
                contractIdArray.push(contractId);
            }else{
                for(var c=0;c<contractIdArray.length; c++){
                    if(contractIdArray[c] != contractId){
                        isMultipleContractsSelected = true;
                    }
                }
            }
        }
    
        return isMultipleContractsSelected;
    },
        isAnotherReuquestTypeSelected : function(soIdsArray, postore){
    
        var isAnotherReuquestTypeSelected = false;
        var movementtypeIdArray = [];
            
        for(var i=0;i<soIdsArray.length; i++){
            var doRec = WtfGlobal.searchRecord(postore,soIdsArray[i],'billid');
            var movementtypeId = "";
                if(doRec!=null && doRec!=undefined){
                movementtypeId=doRec.get('movementtype');
            }
                
            // Check whether consignment request Array contains same id or not if different don't let it save
            if(movementtypeIdArray.length == 0){
                movementtypeIdArray.push(movementtypeId);
            }else{
                for(var c=0;c<movementtypeIdArray.length; c++){
                    if(movementtypeIdArray[c] != movementtypeId){
                        isAnotherReuquestTypeSelected = true;
                    }
                }
            }
        }
    
        return isAnotherReuquestTypeSelected;
    },
    isValidInventoryInfo:function(store,field){        
        for(var storeid=0;storeid<store.getCount();storeid++){
            var storeIdVal=store.getAt(storeid).data[field];                    
            if(storeIdVal==undefined||storeIdVal==""){
                if(store.getAt(storeid).data['productid'].length>0){
                    return false;
                } 
            }            
        }
        return true;
    },

//    getLocaleText:function(key, basename, def){
//    	var base=window[basename||"messages"];
//    	if(base){
//    			if(base[key])
//    				return base[key];
//    			else
//    				clog("Locale specific text not found for ["+key+"]");
//    	}else{
//    		clog("Locale specific base ("+basename+") not available");
//    	}
//    	return def||key;
//    },
        getXType: function(fieldType){
        switch(fieldType){

            case 1:
                return "textfield";
                break;
            case 2:
               return "numberfield";
               break;

           case 3:
               return "datefield";
               break;
           case 4:
                return "combo";
               break;

           case 5:
                  return "timefield";
               break;

           case 11:
                  return "checkbox";
              break;
           case 7:
                  return "select";

               break;
           case 8:
                return "combo";
               break;
           case 9:
                return "autono";
               break;

           case 12:
                return "fieldset";
               break;
               
           case 13:
                return "textarea";
               break;
           case 15:
                return "richtextarea";
               break;
           case 16:
                return "htmleditor";
               break;
           case 17:
                return "extfncombobox";
               break;
           case 18:
                return "fncombo";
               break;
           case 19:
                return "striptextfield";
               break;
        }
    },
        
     getXTypeLable: function(fieldType){
        switch(fieldType){

            case 1:
                return "Text Field";
                break;


           case 2:
               return "Number Field";
               break;

           case 3:
               return "Date Field";
               break;


           case 4:
                return "Combo Box";
               break;


           case 5:
                  return "Time Field";
               break;

           case 6:
                  return "Check Box";
              break;
           case 7:
                  return "Select";

               break;
           case 8:
                return "Combo Box";
               break;
           case 9:
                return "Autono";
               break;

           case 11:
                return "Check Box";
                break;
            case 12:
                return "Field Set";
                break; 
            case 13:
                return "Text Area";
                break;
            case 15:
                return "Rich Text Area";
                break;
               
        }
    },
    appendCustomColumn:function(colArr,ccm,isReport,onCellClickView,readOnly,isViewTemplate,moduleid,excludeLineItems,isfromSalesPurchaseMasterReport){
        if(isReport != undefined && isReport != null){
            isReport=true;
        }else{
            isReport=false;
        } 
        if(ccm){
         for(var cnt=0;cnt<ccm.length;cnt++){
             var tempObj=null;
             var colModelObj = ccm[cnt];
             if(isReport && colModelObj.iscustomcolumn && moduleid!=Wtf.Acc_Customer_ModuleId && moduleid!=Wtf.Acc_Product_Master_ModuleId && moduleid!=Wtf.Acc_Vendor_ModuleId && moduleid!=Wtf.Account_Statement_ModuleId && moduleid!=Wtf.Acc_FixedAssets_Details_ModuleId){//FOR Customer and Vendor Management showing line item column as global level
                 continue;
             }
             if(colModelObj.iscustomcolumn && excludeLineItems){ //excludeLineItems : To hide line item fields in Asset Entry Form
                 continue;
             }
             var fieldtype = colModelObj.fieldtype;
             var fieldid = colModelObj.fieldid;
             var editorObj = {
                 xtype:WtfGlobal.getXType(fieldtype),
                 maxLength:colModelObj.maxlength,
                 required:colModelObj.isessential,
                 fieldLabel : colModelObj.fieldlabel,
                 iscustomfield:colModelObj.iscustomfield,
                 commonFlagforDimAndCustomeField:(colModelObj.iscustomfield || !colModelObj.iscustomfield),//this.commonFlagforDimAndCustomeField flge used to activate or deactivate master items for custom field and dimension field
                 store:null,
                 disabled:(onCellClickView || readOnly || isViewTemplate), //Reverting code changes of ERP-5451
                 disabledClass:"newtripcmbss",
                 useDefault:true,
                 hidden : isfromSalesPurchaseMasterReport != undefined ? isfromSalesPurchaseMasterReport : false//ERM-912
             };
             switch(fieldtype){
             case 3:editorObj['format']=WtfGlobal.getOnlyDateFormat();
            	 break;
             case 12:
             case 4:
             case 7:
            	 editorObj.store=WtfGlobal.getCCStore(fieldid,{
                     url:'ACCAccountCMN/getCustomCombodata.do',
                     baseParams : {
                         fieldid : fieldid,
                        mode: 2,
                           flag: 1,
                           isFormPanel: true
                     },
                     reader: new Wtf.data.KwlJsonReader({
                        root: 'data'
                    }, Wtf.ComboReader)
                 });      
//                 editorObj.store.load();
                 editorObj.isCustomCombo = true;
                 
                 /*
                   To make store mode as remote that why putting as a true.
                 */
                 editorObj.searchStoreCombo =true;  
            	 break;
             case 8:
            	 editorObj.store=WtfGlobal.getCCStore(fieldid,{
                     url: 'ACCAccountCMN/getCustomCombodata.do',
                     baseParams:{
                         comboname:colModelObj.comboname,
                         common:'1'
                     },
                     reader: new Wtf.data.KwlJsonReader({
                         root:'data'
                     },  Wtf.ComboReader)
                 },colModelObj.comboid);
            	 break;
             }
                 
             var dbname = fieldtype==7?colModelObj.refcolumn_number:colModelObj.column_number;
             var header = colModelObj.fieldlabel;
//             var denied = this.moduleid!=1&&fieldtype==3;
             tempObj = {
                 header:header,
                 originalheader : header,
                 tip:header,
                 id:'custom_field'+fieldid,
                 fieldid:fieldid,
                 editor: colModelObj.iseditable=="true" ? WtfGlobal.getEditor(editorObj): undefined,
                 dataIndex:colModelObj.fieldname.replace(".",""),
                 pdfwidth:60,
                 lastQuery : '',
                 dbname:dbname,//Custom field column in which value is saved.
                 iscustomcolumn:true,
                 sortable: false, //(colModelObj.iseditable=="true" && isReport)?true:false,      ERP-34747
                 xtype:WtfGlobal.getXType(fieldtype),
                 fieldtype:fieldtype,
                 sendnotification : colModelObj.sendnotification,
                 notificationdays : colModelObj.notificationdays,
                 relatedModuleIsAllowEditid:colModelObj.relatedModuleIsAllowEditid,
                 hidden : isfromSalesPurchaseMasterReport != undefined ? isfromSalesPurchaseMasterReport : false,//ERM-912
                 renderer : function(val){
                           return "<div style=\'width:600px;\' wtf:qtip=\""
                           +"<div style=\'word-wrap:break-word;text-wrap:unrestricted;\'>"+val+"</div>"
                           + "\" wtf:qtitle='"
                           +   "'>" + Wtf.util.Format.ellipsis(val,60) + "</div>";
                          },
                 refdbname : colModelObj.column_number//Custom field column in which sort(single) value for multi select dropdown is saved.
             };
             switch(fieldtype){
            case 4:
                tempObj['renderer'] = isReport ? WtfGlobal.deletedRenderer:Wtf.comboBoxRenderer(tempObj.editor);
                break;
            case 12:
            case 7:
                tempObj['renderer'] = Wtf.MulticomboBoxRenderer(tempObj.editor);
                break;
             
             case 3:tempObj['renderer']=isReport ? WtfGlobal.onlyDateDeletedRenderer:WtfGlobal.onlyDateRendererTZ;
            	 break;
             case 5:tempObj['renderer']=WtfGlobal.getTimeFieldRenderer;
            	 break;
             case 1:
             case 13:tempObj['renderer']=WtfGlobal.customTextFieldRenderer;    
                 break;
             case 15:
                    tempObj['renderer']=  function(val,m,rec){
                        if(val=="" && isReport){
                            return "";
                        }else{
                            return "<div class='editor richtext'></div>";
                        }
                    }
                    break;
//             case 2:
//            	 if(colModelObj.fieldname.toLowerCase().endsWith("currency"))
//            		 tempObj['renderer']=WtfGlobal.currencyRenderer;
//            	 else
//            		 tempObj['renderer']=WtfGlobal.zeroRenderer;
//            	 break;
             }

             colArr.push(tempObj);
         }
       }
       return colArr;
   },
   
   //Used in Wtf.account.CustomerReceivedReport
   appendCustomColumnForReport:function(colArr,ccm,isReport,onCellClickView,readOnly,isViewTemplate,moduleid){
        if(isReport != undefined && isReport != null){
            isReport=true;
        }else{
            isReport=false;
        } 
        if(ccm){
         for(var cnt=0;cnt<ccm.length;cnt++){
             var tempObj=null;
             var colModelObj = ccm[cnt];
             if(isReport && colModelObj.iscustomcolumn && moduleid!=Wtf.Acc_Customer_ModuleId && moduleid!=Wtf.Acc_Vendor_ModuleId && moduleid!=Wtf.Account_Statement_ModuleId){//FOR Customer and Vendor Management showing line item column as global level
                 continue;
             }
             var fieldtype = colModelObj.fieldtype;
             var fieldid = colModelObj.fieldid;
             var editorObj = {
                 xtype:WtfGlobal.getXType(fieldtype),
                 maxLength:colModelObj.maxlength,
                 required:colModelObj.isessential,
                 iscustomfield:colModelObj.iscustomfield,
                 store:null,
                 disabled:(onCellClickView || readOnly || isViewTemplate), //Reverting code changes of ERP-5451
                 disabledClass:"newtripcmbss",
                 useDefault:true
             };
             switch(fieldtype){
                case 3:editorObj['format']=WtfGlobal.getOnlyDateFormat();
            	 break;
             }

             var dbname = fieldtype==7?colModelObj.refcolumn_number:colModelObj.column_number;
             var header = colModelObj.fieldlabel;
             tempObj = {
                 header:header,
                 originalheader : header,
                 tip:header,
                 id:'custom_field'+fieldid,
                 fieldid:fieldid,
//                 editor: colModelObj.iseditable=="true" ? WtfGlobal.getEditor(editorObj): undefined,
                 dataIndex:colModelObj.fieldname.replace(".",""),
                 pdfwidth:60,
                 lastQuery : '',
                 dbname:dbname,//Custom field column in which value is saved.
                 iscustomcolumn:true,
                 sortable:true,
                 fieldtype:fieldtype,
                 sendnotification : colModelObj.sendnotification,
                 notificationdays : colModelObj.notificationdays,
                 renderer : function(val){
                           return "<div style=\'width:600px;\' wtf:qtip=\""
                           +"<div style=\'word-wrap:break-word;text-wrap:unrestricted;\'>"+val+"</div>"
                           + "\" wtf:qtitle='"
                           +   "'>" + Wtf.util.Format.ellipsis(val,60) + "</div>";
                          },
                 refdbname : colModelObj.column_number//Custom field column in which sort(single) value for multi select dropdown is saved.
             };
             switch(fieldtype){
                case 3:tempObj['renderer']=isReport ? WtfGlobal.onlyDateDeletedRenderer:WtfGlobal.onlyDateRendererTZ;
                    break;
                case 5:tempObj['renderer']=WtfGlobal.getTimeFieldRenderer;
                    break;
                case 1:
                case 13:tempObj['renderer']=WtfGlobal.customTextFieldRenderer;    
                    break;
             }
             colArr.push(tempObj);
         }
       }
       return colArr;
   },
   
      getCCStore:function(fieldid, storeObj, comboid){
        var store = new Wtf.data.Store(storeObj);
//        this.cstStore = this.cstStore||[];
//        this.cstStore.push(store);
//        store.on('load', this.loadMainStore,this,{
//            single:true
//        });
        store.load();
//        var store = GlobalComboStore["cstore"+fieldid];
//        if(store==null){
//            switch(comboid){
//		   case Wtf.common.productModuleID:store = Wtf.productStore;break;
//                /*
//            * Below stores are not in Used
//            */
//                //		   case Wtf.common.leadModuleID: store = Wtf.leadStore;break;
//                //		   case Wtf.common.contactModuleID: store = Wtf.contactStore;break;
//                //		   case Wtf.common.caseModuleID: store = Wtf.caseStore;break;
//                //		   case Wtf.common.oppModuleID: store = Wtf.opportunityStore;break;
//                case Wtf.common.userModuleID:
//                    store = Wtf.allUsersStore;Wtf.allUsersStore.load();
//                    break;
//                default:
//                    store = new Wtf.data.Store(storeObj);
//                    this.cstStore = this.cstStore||[];
//                    this.cstStore.push(store);
//                    store.on('load', this.loadMainStore,this,{
//                        single:true
//                    });
//                    store.load();
//            }
//            GlobalComboStore["cstore"+fieldid] = store;
//        }
        return store;
    },
   postData:function(url, params){
        var mapForm = document.createElement("form");
        mapForm.target = "downloadframe";
        mapForm.method = "post";
        mapForm.action = url;
        var params = params;
        var inputs = params.split('&');
        for (var i = 0; i < inputs.length; i++) {
            var KV_pair = inputs[i].split('=');
            var mapInput = document.createElement("input");
            mapInput.type = "text";
            mapInput.name = KV_pair[0];
            mapInput.value = KV_pair[1];
            mapForm.appendChild(mapInput);
        }
        document.body.appendChild(mapForm);
        mapForm.submit();
        //        var myWindow = window.open(url+"?"+params, "mywindow","menubar=1,resizable=1,scrollbars=1");
//        var div = myWindow.document.createElement("div");
//        div.innerHTML = "Loading, Please Wait...";
//        myWindow.document.body.appendChild(div);
        mapForm.remove();
    },
     /**
     * Search in array by it's key.
      */
    arraySearch: function(arr, val) {
        for (var i = 0; i < arr.length; i++)
            if (arr[i].fieldid === val) {
                return arr[i].fieldname;
            }
        return false;
    },
    replaceText: function(find,text,replaceWith){
        var regEx = new RegExp(find, 'g');
        text = text.replace(regEx, replaceWith);    
        return text;
    },
    
    isInvalidProductsSelected:function(multiSelectProductCombo){
        
        var returnFlag = false;
        
        var enteredProducts = multiSelectProductCombo.getRawValue();
        
        var enteredProductsArray = enteredProducts.split(',');
        
        var selectedProducts = multiSelectProductCombo.getValue();
        
        var selectedProductsArray = selectedProducts.split(',');
        
        var enteredProductsArrayLength = 0;
        var selectedProductsArrayLength = 0;
        
        if(selectedProductsArray[0] != ""){
            selectedProductsArrayLength = selectedProductsArray.length;
        }
        
        if(enteredProductsArray[0] != ""){
            enteredProductsArrayLength = enteredProductsArray.length;
        }
            
        if(enteredProductsArrayLength>0 && selectedProductsArrayLength==0){
            returnFlag = true;
        }
        return returnFlag;
    },
    
    
      getEditor : function(eObj){
       var editor = null;
       if(eObj.xtype == "combo") {
           if(eObj.useDefault==true){
        	   Wtf.applyIf(eObj,{
                   selectOnFocus:true,
                   triggerAction:'all',
                   mode:'local',
                   valueField:'id',
                   displayField:'name',
                   typeAhead:false,  //It set None by default that's why set false
                   tpl:Wtf.comboTemplate     		   
        	   });   
           }
           if(eObj.searchStoreCombo && eObj.searchStoreCombo == true) {
                eObj.mode = 'remote';
                if(!eObj.loadOnSelect) {
                    eObj.minChars = 2;
                    eObj.triggerClass ='dttriggerForTeamLead';
                }
                eObj.spreadSheetCombo = true;
                eObj.listeners = {
                    'beforeselect': function(combo, record, index) {
                        combo.originalOldId = record.data.name;
                    },
                    scope: this
                }
           }
           /**
            * ERP-34339
            * added itemdescription extra field for Product Tax Class Dimension (GST) using   Wtf.form.ExtFnComboBox(eObj)
            */
           if(WtfGlobal.GSTApplicableForCompany() != Wtf.GSTStatus.NONE && (Wtf.GSTProdCategory==eObj.fieldLabel || Wtf.GSTProdCategory+'*'==eObj.fieldLabel)){
               eObj.extraFields = ['itemdescription'];
               eObj.extraComparisionField = 'itemdescription';
               editor = new Wtf.form.ExtFnComboBox(eObj);
           }else{
               editor = new Wtf.form.ComboBox(eObj);    
           }
           if(eObj!=null && eObj!=undefined && eObj.commonFlagforDimAndCustomeField){//before select event provided custom and  dimension drop down (this.commonFlagforDimAndCustomeField flge used to activate or deactivate master items for custom field and dimension field) 
            editor.on('beforeselect',validateSelection);
        }
       }else if(eObj.xtype == "select" || eObj.xtype == "fieldset") {
           if(eObj.useDefault==true){
               eObj.selectOnFocus = true;
               eObj.forceSelection = true;
               eObj.multiSelect = true;
               eObj.triggerAction = 'all';
               eObj.mode = 'local';
               eObj.valueField = 'id';
               eObj.displayField = 'name';
               eObj.typeAhead = true;
               eObj.tpl= Wtf.comboTemplate;
               eObj.isCustomCombo = eObj.isCustomCombo==true?true:false;
           }
           eObj.spreadSheetCombo = true;
           editor = new Wtf.common.Select(eObj);
//           editor.on('beforeselect',Wtf.SpreadSheetGrid.prototype.validateSelection,this);
       } else if(eObj.xtype == "textfield") {
           editor = new Wtf.form.TextField(eObj);
       } else if(eObj.xtype == "numberfield") {
           editor = new Wtf.form.NumberField(eObj);
       } else if(eObj.xtype == "datefield") {
           eObj.format = WtfGlobal.getOnlyDateFormat();
           // readonly property: false - For Issue - Once we select the date how can we delete it again.
           eObj.readOnly=false;
//           eObj.offset=Wtf.pref.tzoffset;
           editor = new Wtf.form.DateField(eObj);
       } else if(eObj.xtype == "timefield") {
           if(eObj.useDefault==true) {
               eObj.value = WtfGlobal.setDefaultValueTimefield();
               eObj.format=WtfGlobal.getLoginUserTimeFormat();
           }
           editor = new Wtf.form.TimeField(eObj);
       }else if(eObj.xtype == "textarea") {
           editor = new Wtf.form.TextArea(eObj);
       }else if(eObj.xtype == "checkbox"){
           editor = new Wtf.form.Checkbox(eObj);
       }

       return editor;
   },
   
  getCustomColumnData:function(rData,moduleid){
      var jsondata = ",customfield:[]";
      var GlobalcolumnModel=GlobalColumnModel[moduleid];
      if(GlobalcolumnModel){
          jsondata =',customfield:[{';
          for(var cnt = 0;cnt<GlobalcolumnModel.length;cnt++){
              var fieldname = GlobalcolumnModel[cnt].fieldname;
              var refcolumn_number = GlobalcolumnModel[cnt].refcolumn_number;
              var column_number = GlobalcolumnModel[cnt].column_number;
              var fieldid = GlobalcolumnModel[cnt].fieldid;
              var fieldtype = GlobalcolumnModel[cnt].fieldtype;
//              if(isMassordetails&&(rData[fieldname]=="" || rData[fieldname]==undefined)){
//                  continue;
//              }
              if(cnt > 0){
                  jsondata +="},{";
              }
               var recData = "";
              if(fieldname.indexOf('.')>=0){
                  recData = rData[fieldname.replace(".","")];
              }else{
                  recData = rData[fieldname];
              }
              if(GlobalcolumnModel[cnt].fieldtype=="3" && !Wtf.isEmpty(recData)){
                  var daterec =recData;
                  if(recData!=undefined && recData!="" ){
                	  //daterec =new Date(recData).getTime();
                          daterec=WtfGlobal.convertToGenericDate(recData); 
                  }
                  jsondata +="\"refcolumn_name\": \""+refcolumn_number+"\",\"fieldname\": \""+fieldname+"\",\""+column_number+"\": \""+daterec+"\",\""+fieldname+"\": \""+column_number+"\",\"filedid\":\""+fieldid+"\",\"xtype\":\""+fieldtype+"\"";
              }else if(GlobalcolumnModel[cnt].fieldtype=="5"){  // Time Field
                  if(!Wtf.isEmpty(recData)){
                      recData =  recData;
                  }
                  jsondata +="\"refcolumn_name\": \""+refcolumn_number+"\",\"fieldname\": \""+fieldname+"\",\""+column_number+"\": \""+recData+"\",\""+fieldname+"\": \""+column_number+"\",\"filedid\":\""+fieldid+"\",\"xtype\":\""+fieldtype+"\"";
              }else{  
                  if(!Wtf.isEmpty(recData)){  //ERP-12328 [SJ]
                      recData =  recData;
                  }
                  else{
                      recData='';
                  }
                  jsondata +="\"refcolumn_name\": \""+refcolumn_number+"\",\"fieldname\": \""+fieldname+"\",\""+column_number+"\": "+Wtf.util.JSON.encode(recData)+",\""+fieldname+"\": \""+column_number+"\",\"filedid\":\""+fieldid+"\",\"xtype\":\""+fieldtype+"\"";
              }
          }
          jsondata +='}]';
      }
      return jsondata;
  },
    getCustomColumnDataForProduct:function(rData,moduleid,record){
        var jsondata = ",productcustomfield:[]";
        var GlobalcolumnModel = GlobalColumnModelForProduct[moduleid];
        var richTextFieldChanged = false;
        if (GlobalcolumnModel) {
            jsondata = ',productcustomfield:[{';
            for (var cnt = 0; cnt < GlobalcolumnModel.length; cnt++) {
                var fieldname = GlobalcolumnModel[cnt].fieldname;
                var refcolumn_number = GlobalcolumnModel[cnt].refcolumn_number;
                var column_number = GlobalcolumnModel[cnt].column_number;
                var fieldid = GlobalcolumnModel[cnt].fieldid;
                var fieldtype = GlobalcolumnModel[cnt].fieldtype;
                var richTextFlag = "richText" + fieldname;
                if (GlobalcolumnModel[cnt].fieldtype == 15) {
                    var existingRecordModified = false;
                    if (record !== undefined) {
                        if (rData.isNewRecord === "") {
                            if (record.modified.fieldname != undefined) {
                                existingRecordModified = true;
                            } else if (record.modified.pid == undefined) {
                                existingRecordModified = true;
                            }
                        }
                    }
                    if (rData[fieldname] != "" || (rData.isNewRecord === "1" && rData[richTextFlag] != undefined) || existingRecordModified) {
                        richTextFieldChanged = true;
                    }

//                  var flag=Wtf.getCmp(rData.id+fieldname).modificationFlag;
                }
//              if(isMassordetails&&(rData[fieldname]=="" || rData[fieldname]==undefined)){
//                  continue;
//              }
              if(cnt > 0){
                  jsondata +="},{";
                }
                var recData = "";
              if(fieldname.indexOf('.')>=0){
                  recData = rData[fieldname.replace(".","")];
              }else{
                    recData = rData[fieldname];
                }
              if(GlobalcolumnModel[cnt].fieldtype=="3" && !Wtf.isEmpty(recData)){
                  var daterec =recData;
                  if(!Wtf.isEmpty(recData)){
                        //daterec =new Date(recData).getTime();
                          daterec=WtfGlobal.convertToGenericDate(recData);
                    }
                  jsondata +="\"refcolumn_name\": \""+refcolumn_number+"\",\"fieldname\": \""+fieldname+"\",\""+column_number+"\": \""+daterec+"\",\""+fieldname+"\": \""+column_number+"\",\"filedid\":\""+fieldid+"\",\"xtype\":\""+fieldtype+"\"";
              }else if(GlobalcolumnModel[cnt].fieldtype=="5"){  // Time Field
                  if(!Wtf.isEmpty(recData)){
                      recData =  recData;
                    }
                    jsondata += "\"refcolumn_name\": \"" + refcolumn_number + "\",\"fieldname\": \"" + fieldname + "\",\"" + column_number + "\": \"" + recData + "\",\"" + fieldname + "\": \"" + column_number + "\",\"filedid\":\"" + fieldid + "\",\"xtype\":\"" + fieldtype + "\"";
                } else
                    jsondata += "\"refcolumn_name\": \"" + refcolumn_number + "\",\"fieldname\": \"" + fieldname + "\",\"" + column_number + "\": " + Wtf.util.JSON.encode(recData) + ",\"" + fieldname + "\": \"" + column_number + "\",\"filedid\":\"" + fieldid + "\",\"xtype\":\"" + fieldtype + "\"";
                if (GlobalcolumnModel[cnt].fieldtype == 15) {
                    jsondata += ",\"isRichTextFieldChanged\":\"" + richTextFieldChanged + "\"";
                }
            }
            jsondata += '}]';
        }
        return jsondata;
    },
  checkValidItems:function(moduleid,grid){          // Check Mandatory Line Item Values
    var data="";
    var GlobalcolumnModel=GlobalColumnModel[moduleid];  
          if(GlobalcolumnModel){       
          for(var cnt = 0;cnt<GlobalcolumnModel.length;cnt++){
              var fieldname = GlobalcolumnModel[cnt].fieldname;
              var fieldlabel= GlobalcolumnModel[cnt].fieldlabel;
              var fieldtype = GlobalcolumnModel[cnt].fieldtype;
              var isMendatory = GlobalcolumnModel[cnt].isessential;
                if (isMendatory)
                {                
                   var array = grid.store.data.items;
                    for (var i = 0; i < array.length-1; i++) {
                        var customfield = array[i].get("customfield");
                        for (var j = 0; j < customfield.length; j++) {
                            var name = customfield[j].fieldname;
                            var xtype = customfield[j].xtype;
                            if (fieldname == name) {
                                var value = customfield[j][name];
                                value=customfield[j][value];
                                if (value == "" ||  value==undefined) {
                                    data += " "+fieldlabel+"(row"+(i+1)+"),";
                                }
                            }
                        }

                    }
                }

          }
        
      }
      if(data.length>1){
          data=data.substr(0,data.length-1);
      }     
        return data;
  },
  checkBatchDetail:function(moduleid,grid){          // Check Mandatory Line Item Values
    var data="";
    var totalRec = grid.store.getCount();
    var array = grid.store.data.items;
//    for(var j=0;j<totalRec;j++){
        for (var i = 0; i < array.length - 1; i++) {
            /* 
             * For service type product we don't have batch details so no need to check in that case 
             */
            if (array[i].data.type != "Service") {
                var productname = array[i].get("pid");
                var value = array[i].get("batchdetails");
                if (value == "" || value == undefined || value == "[]") {
                    data += " " + productname + "(row" + (i + 1) + "),";
                }
            }
        }
//    }
    if(data.length>1){
        data=data.substr(0,data.length-1);
    }     
    return data;
},
checkBatchDetailQty:function(moduleid,grid){          // Check Mandatory Line Item Values
    var data="";
    var totalRec = grid.store.getCount();
    var array = grid.store.data.items;
    //    for(var j=0;j<totalRec;j++){
    for (var i = 0; i < array.length-1; i++) {
        var productname = array[i].get("pid");
        var batchDetail = array[i].get("batchdetails");
        var productQty  = (moduleid=="27"||moduleid=="28" || moduleid== Wtf.Acc_Sales_Return_ModuleId || moduleid== Wtf.Acc_Purchase_Return_ModuleId )?array[i].get('dquantity'):array[i].get('quantity');
        var baseUOMRateQty= array[i].get('baseuomrate');
        var type= array[i].get('type');
       
        var jsonBatchDetails= eval(batchDetail);
        var batchQty=0;
        if(jsonBatchDetails){
            for(var batchCnt=0;batchCnt<jsonBatchDetails.length;batchCnt++){
                if(jsonBatchDetails[batchCnt].quantity>0){
                    batchQty=batchQty+ parseFloat(jsonBatchDetails[batchCnt].quantity);
                }
            }
        }
        if(batchQty.toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) != (productQty*baseUOMRateQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)&& type !='Service'){
             data += " "+productname+"(row"+(i+1)+"),";
        }
    }
    //    }
    if(data.length>1){
        data=data.substr(0,data.length-1);
    }     
    return data;
},
  /**
  * E
   * @returns {Number}RP-32829 
  * Check GST applicable status for company
   */
    GSTApplicableForCompany: function() {
        if (Wtf.account.companyAccountPref.isNewGSTOnly) {
            return Wtf.GSTStatus.NEW;
        } else if (!Wtf.account.companyAccountPref.isNewGSTOnly && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
            return Wtf.GSTStatus.OLDNEW;
        } else {
            return Wtf.GSTStatus.NONE;
        }
    },
    getGSTType:function(){
      if(Wtf.Countryid == Wtf.Country.INDIA || Wtf.Countryid == Wtf.Country.US)  {
          return Wtf.GSTType.INDIATYPE;
      }else{
          return Wtf.GSTType.SINGAPORETYPE;
      }
    },
    populateDimensionValueingrid: function (moduleid, rec, grid) {          // Check Mandatory Line Item Values
/**
 *  If Cash Sales then fetch custom fields of Invoice as invoice moduleid is used for storing custom fields of CS.
 *  If Cash Purchase then fetch custom fields of Purchase Invoice as PI moduleid is used for storing custom fields of CP.
 */
        if (moduleid == Wtf.Acc_Cash_Sales_ModuleId) {              
            moduleid = Wtf.Acc_Invoice_ModuleId;
        } else if (moduleid == Wtf.Acc_Cash_Purchase_ModuleId) {
            moduleid = Wtf.Acc_Vendor_Invoice_ModuleId;
        }
        var GlobalcolumnModel = GlobalColumnModel[moduleid];
        if (GlobalcolumnModel) {
            for (var cnt = 0; cnt < GlobalcolumnModel.length; cnt++) {
                var fieldname = GlobalcolumnModel[cnt].fieldname;
                var iscustomfield = GlobalcolumnModel[cnt].iscustomfield;
                var value = rec.lastSelectionText;
                var globalname = rec.name
                var globallebel = rec.fieldLabel
                var modulenameForJE = rec.modulename; //added to check type of journal entry
                
                if (!iscustomfield && fieldname == globalname)
                {
                    var array = grid.store.data.items;
                    var length = array.length - 1;
                    if (modulenameForJE != undefined && moduleid == 24 &&  modulenameForJE == 3) {
                        length = array.length;
                    }
                    if (array.length > 1) {
                        var confirmMsg = WtfGlobal.getLocaleText("acc.common.Linelevelvaluesfor")+" "+ globallebel + " " + WtfGlobal.getLocaleText("acc.common.getrefreshedDoyouwanttocontinue");
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), confirmMsg + '</center>', function (btn) {
                            if (btn == "yes") {
                                for (var i = 0; i < length; i++) {
                                        for (var k = 0; k < grid.colModel.config.length; k++) {
                                            if (grid.colModel.config[k].editor && grid.colModel.config[k].editor.field.store && grid.colModel.config[k].dataIndex == globalname) {
                                                var store = grid.colModel.config[k].editor.field.store;
                                                var gridRecord = grid.store.getAt(i);
                                                var recCustomCombo = WtfGlobal.searchRecord(store, value, "name");
                                                var ComboValueID = recCustomCombo.data.id;
                                                gridRecord.set(globalname, ComboValueID);
                                            }
                                        }
                                    }
                            } else {
                                return;
                            }
                        }, this);
                    }
                }
            }
        }
    },
        /**
     * Populate dimensions custom fields data in buld payment
     */
    populateGlobalDimensionValueInBulkPayment: function(parentObj) {
        if (parentObj.isBulkPayment) {
            var recordArr = eval(parentObj.invObj.getSelectedRecords());
            var fieldArr = parentObj.invObj.Store.fields.items;
            if (recordArr.length == 1) {
                /**
                 * If single record is selected 
                 */
                for (var fieldCnt = 0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                    if (fieldN && fieldN.name.indexOf("Custom_") == 0) {
                        /**
                         * If custom field found 
                         */
                        var fieldname = fieldN.name;
                        var billid = recordArr[0].billid;
                        var record = parentObj.invObj.Store.getAt(parentObj.invObj.Store.find('billid', billid));
                        if (recordArr[0][fieldname] != "" && Wtf.getCmp(fieldname + parentObj.tagsFieldset.id)) {
                            if (Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).xtype == 'fncombo' || Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).getXType() == 'fncombo') {
                                /**
                                 * set single select combo box
                                 */
                                var dimstore = Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).store;
                                var rec = WtfGlobal.searchRecordIsWithCase(dimstore, recordArr[0][fieldname], 'name', false);
                                if (rec != undefined) {
                                    var dimid = rec.data.id;
                                    Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).setValue(dimid);
                                }

                            } else if (Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).getXType() == 'datefield') {
                                /**
                                 * Set date field
                                 */
                                Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).setValue(new Date(record.json[fieldname + "_Value"]));
                            } else {
                                /**
                                 * set Text, Number etc
                                 */
                                Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).setValue(recordArr[0][fieldname]);
                            }
                        }
                        /**
                         * 
                         */
                        var fieldname = fieldN.name.substring(7, fieldN.name.length);
                        var datavalue = record.json["Custom_" + fieldname + "_colValueForMulti"];
                        if (datavalue != "" && Wtf.getCmp(fieldname + parentObj.tagsFieldset.id)) {
                            if (Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).getXType() == 'select' || Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).getXType() == 'select') {
                                /**
                                 * Set multi select combo
                                 */
                                var ComboValue = datavalue;
                                var dimid = "";
                                if (ComboValue !== undefined && ComboValue !== null && ComboValue != "") {
                                    var ComboValueArrya = ComboValue.split(',');
                                    var ComboValueID = "";
                                    for (var i = 0; i < ComboValueArrya.length; i++) {
                                        var dimstore = Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).store;
                                        var rec = WtfGlobal.searchRecordIsWithCase(dimstore, ComboValueArrya[i], 'name', false);
                                        dimid += rec.data.id + ",";
                                    }
                                    if (dimid.length > 1) {
                                        dimid = dimid.substring(0, dimid.length - 1);
                                    }
                                    Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).setValue(dimid);
                                }
                            } else if (Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).getXType() == 'fieldset') {
                                /**
                                 * Set check list
                                 */
                                var ComboValue = datavalue;
                                if (ComboValue !== undefined && ComboValue !== null && ComboValue != "") {
                                    var ComboValueArrya = ComboValue.split(',');
                                    var ComboValueID = "";
                                    var checkListCheckBoxesArray = parentObj.tagsFieldset.checkListCheckBoxesArray;
                                    for (var i = 0; i < ComboValueArrya.length; i++) {
                                        for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                                            if (checkListCheckBoxesArray[checkitemcnt].name.indexOf(ComboValueArrya[i]) != -1)
                                                if (Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id) != undefined) {
                                                    Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id).setValue(true);
                                                }
                                        }
                                    }
                                }
                            } else {
                                Wtf.getCmp(fieldname + parentObj.tagsFieldset.id).setValue(recordArr[0][fieldname]);
                            }
                        }
                    }
                }
            }
        }
    },
    validateKnockOffFieldsData: function (moduleid, tagsFieldset, invoiceRec,isForMultientity) {
        /*
         * This function validate invoices global level custom fields/dimension data with DN/CN global level data for "Knock Off" type Fields
         * Case -1 If same field is created in both modules having same values then retrun true else retun false.
         * Case -2 If CN/DN module contain field is not created then return true else return false.
         * Case -3 IF field is created on in CN/DN then return false.
         * @type Boolean|Boolean|Boolean
         */
        var isValid = true;
        var dimensionFor = isForMultientity || "isForKnockOff";
        var GlobalcolumnModel = GlobalColumnModelForReports[moduleid];
        var dimensionFieldArray = tagsFieldset.dimensionFieldArray;
        var customFieldArray = tagsFieldset.customFieldArray;
        for (var columncount = 0; columncount < GlobalcolumnModel.length; columncount++) {
            if (GlobalcolumnModel[columncount][dimensionFor]) {
                for (var custFieldCount = 0; custFieldCount < dimensionFieldArray.length; custFieldCount++) {
                    if (dimensionFieldArray[custFieldCount].name == GlobalcolumnModel[columncount].fieldname) {
                        var fieldId = dimensionFieldArray[custFieldCount].id;
                        var fieldValue = Wtf.getCmp(fieldId).getRawValue();
                        var isValidNoneValue =WtfGlobal.validateNoneValuedCustomField(fieldId,invoiceRec,custFieldCount,dimensionFieldArray);
                        if (isValidNoneValue !=undefined && !isValidNoneValue){
                            isValid = false;
                            break;
                        }
                    }
                }
                for (var custFieldCount = 0; custFieldCount < customFieldArray.length; custFieldCount++) {
                    if (customFieldArray[custFieldCount].name == GlobalcolumnModel[columncount].fieldname) {
                        var fieldId = customFieldArray[custFieldCount].id;
                        var fieldValue = Wtf.getCmp(fieldId).getRawValue();
                        var isValidNoneValue = WtfGlobal.validateNoneValuedCustomField(fieldId,invoiceRec,custFieldCount,customFieldArray);
                        if (isValidNoneValue !=undefined && !isValidNoneValue) {
                                isValid = false;
                                break;
                            }
                        }
                    }
                }
            }
        return isValid;
    },
validateNoneValuedCustomField: function (fieldId,invoiceRec,custFieldCount,customFieldArray) {
    var fieldValue = Wtf.getCmp(fieldId).getRawValue();
    if(customFieldArray==""||customFieldArray==undefined||customFieldArray[custFieldCount]==undefined||invoiceRec.data[customFieldArray[custFieldCount].name]==undefined ){
        return;
    }
    if(invoiceRec.data[customFieldArray[custFieldCount].name]!=undefined &&((fieldValue=="None" && Wtf.getCmp(fieldId).getValue()=='1234')&&invoiceRec.data[customFieldArray[custFieldCount].name]=="")){
        return true;
    }else{
        if(fieldValue == invoiceRec.data[customFieldArray[custFieldCount].name]){
            return true;
        }
        else{
            return false;
        }
    }
},
    populateCustomData:function(billid,POStore,tagsFieldset){
        this.tagsFieldset=tagsFieldset;
        var record = POStore.getAt(POStore.find('billid', billid));

        var fieldArr = POStore.fields.items;
        for (var fieldCnt = 0; fieldCnt < fieldArr.length; fieldCnt++) {
            var fieldN = fieldArr[fieldCnt];
            if(record !=  undefined){
            if (Wtf.getCmp(fieldN.name + this.tagsFieldset.id) && record.data[fieldN.name] != "") {
                if (Wtf.getCmp(fieldN.name + this.tagsFieldset.id).getXType() == 'datefield') {
                    Wtf.getCmp(fieldN.name + this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                } else if (Wtf.getCmp(fieldN.name + this.tagsFieldset.id).getXType() == 'fncombo') {
                    var ComboValue = record.data[fieldN.name];
                    if (ComboValue) {
                        Wtf.getCmp(fieldN.name + this.tagsFieldset.id).setValue(ComboValue);
                        var childid = Wtf.getCmp(fieldN.name + this.tagsFieldset.id).childid;
                        if (childid.length > 0) {
                            var childidArray = childid.split(",");
                            for (var i = 0; i < childidArray.length; i++) {
                                var currentBaseParams = Wtf.getCmp(childidArray[i] + this.tagsFieldset.id).store.baseParams;
                                currentBaseParams.parentid = ComboValue;
                                Wtf.getCmp(childidArray[i] + this.tagsFieldset.id).store.baseParams = currentBaseParams;
                                Wtf.getCmp(childidArray[i] + this.tagsFieldset.id).store.load();
                            }
                        }
                    }
                }else {
                    Wtf.getCmp(fieldN.name + this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                }
            }
            if (fieldN.name.indexOf("Custom_") == 0) {
                var fieldname = fieldN.name.substring(7, fieldN.name.length);
                if (Wtf.getCmp(fieldname + this.tagsFieldset.id) && record.data[fieldN.name] != "") {
                    if (Wtf.getCmp(fieldname + this.tagsFieldset.id).getXType() == 'fieldset') {
                        var ComboValue = record.json[fieldN.name];
                        var ComboValueArrya = ComboValue.split(',');
                        var ComboValueID = "";
                        var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;
                        for (var i = 0; i < ComboValueArrya.length; i++) {
                            for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                                if (checkListCheckBoxesArray[checkitemcnt].id.indexOf(ComboValueArrya[i]) != -1)
                                    if (Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id) != undefined) {
                                        Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id).setValue(true);
                                    }
                            }
                        }
                    } else if (Wtf.getCmp(fieldname + this.tagsFieldset.id).getXType() == 'select') {
                        var ComboValue = record.json[fieldN.name];
                        if (ComboValue != "" && ComboValue != undefined)
                            Wtf.getCmp(fieldname + this.tagsFieldset.id).setValue(ComboValue);
                    }

                }
            }
        }
        }
    }, 
    setCustomFieldValueAfterStoreLoad: function(grid, moduleid, lineRecord) {
        var GlobalcolumnModel = GlobalColumnModel[moduleid];
        if (GlobalcolumnModel) {
            for (var cnt = 0; cnt < GlobalcolumnModel.length; cnt++) {
                var fieldname = GlobalcolumnModel[cnt].fieldname;
                var value = lineRecord.data[fieldname];
                if (value != undefined && value != "") {
                    if (GlobalcolumnModel[cnt].fieldtype == Wtf.CustomFieldType.SingleSelectDropdown || GlobalcolumnModel[cnt].fieldtype == Wtf.CustomFieldType.MultiSelectDropdown) {
                        value = getValueForDimension(fieldname, value);

                        var array = grid.store.data.items;
                        if (array.length > 0) {
                            for (var i = 0; i < array.length - 0; i++) {
                                for (var k = 0; k < grid.colModel.config.length; k++) {
                                    if (grid.colModel.config[k].dataIndex == fieldname) {
                                        var gridRecord = grid.store.getAt(i);
                                        if (grid.colModel.config[k].editor && grid.colModel.config[k].editor.field.store) {
                                            var store = grid.colModel.config[k].editor.field.store;
                                            var valArr = value.split(',');
                                            var ComboValueID = "";
                                            for (var index = 0; index < valArr.length; index++) {
                                                var recCustomCombo = WtfGlobal.searchRecord(store, valArr[index], "name");
                                                if (recCustomCombo)
                                                    ComboValueID += recCustomCombo.data.id + ',';
                                            }
                                            if (ComboValueID.length > 0)
                                                ComboValueID = ComboValueID.substring(0, ComboValueID.length - 1);
                                            gridRecord.set(fieldname, ComboValueID);
                                        } else {
                                            gridRecord.set(fieldname, value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    },
       /*
     * Returns value for drop down custom fields from group grid 
     */
    getValueForDimension: function(fieldName, value, grid) {
        if (grid) {
            var array = grid.store.data.items;
            if (array.length > 1) {
                for (var i = 0; i < array.length - 1; i++) {
                    for (var k = 0; k < grid.colModel.config.length; k++) {
                        if (grid.colModel.config[k].editor && grid.colModel.config[k].editor.field.store && grid.colModel.config[k].dataIndex == fieldName) {
                            var store = grid.colModel.config[k].editor.field.store;
                            var valArr = value.split(',');
                            var returnData = "";
                            for (var index = 0; index < valArr.length; index++) {
                                var recCustomCombo = WtfGlobal.searchRecord(store, valArr[index], "id");
                                if (recCustomCombo)
                                    returnData += recCustomCombo.data.name + ',';
                            }
                            return returnData;
                        }
                    }
                }
            }
        }
    },
    zeroRenderer: function(val){
        if((val=="")&&(val==undefined)){
        	return "";
        }
        else if(val=="0"){
        	return "0";
        } else {
        	return val;
        }
    },
    docCommentEnable:function (obj,detailPanelObj,isMainOwner,enableContactsButton){
   
          obj.document.enable();
          obj.document.setTooltip('Add files to the selected '+obj.customParentModName+'.');

          obj.comment.enable();
          obj.comment.setTooltip('Add comments to the selected '+obj.customParentModName+'.');

    },
    onlyDateRendererTZ: function(v) {
    	if(!v||!(v instanceof Date)) return v;
//      return new Date(v.getTime()+1*(v.getTimezoneOffset()*60000+Wtf.pref.tzoffset)).format(WtfGlobal.getOnlyDateFormat());
    	return new Date(v.getTime()).format(WtfGlobal.getOnlyDateFormat());
    },
    getLocaleText:function(key, basename, def){
        var base=window[basename||"messages"];
        var params=[].concat(key.params||[]);
        key = key.key||key;
        if(base){
            if(base[key]){
                    params.splice(0, 0, base[key]);
                    return String.format.apply(this,params);
            }else
                    clog("Locale specific text not found for ["+key+"]");
        }else{
            clog("Locale specific base ("+basename+") not available");
        }
        return def||key;
    },

    loadScript: function(src, callback, scope){
        var scriptTag = document.createElement("script");
        scriptTag.type = "text/javascript";
        if(typeof callback == "function"){
        	scriptTag.onreadystatechange= function () {
        		      if (this.readyState == 'complete')
        		    	  callback.call(scope || this || window);
        		   }
        	scriptTag.onload= callback.createDelegate(scope || this || window);
        }
        scriptTag.src = src;
        document.getElementsByTagName("head")[0].appendChild(scriptTag);
    },

    loadStyleSheet: function(ref){
        var styleTag = document.createElement("link");
        styleTag.setAttribute("rel", "stylesheet");
        styleTag.setAttribute("type", "text/css");
        styleTag.setAttribute("href", ref);
        document.getElementsByTagName("head")[0].appendChild(styleTag);
    },

    getJSONArray:function(grid, includeLast, idxArr){ // Function is written for getting data in encoded format. It is used for passing data to java side with encoded format
        var indices="";
        if(idxArr)
            indices=":"+idxArr.join(":")+":";
        var store=grid.getStore();
        var arr=[];
        var fields=store.fields;
        var len=store.getCount()-1;
        if(includeLast)len++;
        for(var i=0;i<len;i++){
            if(idxArr&&indices.indexOf(":"+i+":")<0) continue;
            var rec=store.getAt(i);
            var recarr=[];
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name]!=undefined?rec.data[fields.get(j).name]:'';   //ERP-12328 [SJ]
                if(fields.get(j).name=="srno" && fields.get(j).isForSequence) {    //refer ticket ERP-13781 - only for product grid in any form 
                    value = i+1;
                } else if (fields.get(j).name=='discountname'||fields.get(j).name=='discountdescription'||fields.get(j).name=='qtipdiscountstr'||fields.get(j).name=='discountjson'||fields.get(j).name=='customfield' || fields.get(j).name==CUSTOM_FIELD_KEY_PRODUCT || fields.get(j).name=='batchdetails' || fields.get(j).name=='serialDetails'|| fields.get(j).name=='replacebatchdetails' || fields.get(j).name=='assetDetails' || fields.get(j).name=='packingdodetails' || fields.get(j).name=='packingdetails' || fields.get(j).name=='wastageDetails' || fields.get(j).name=='joborderdetails') {
                    value=Wtf.encode(value);
                } else if(fields.get(j).name=="inspectionAreaDetails") {
                  /* instead of encode,encodeURI use encodeURIComponent
                   * This function encodes special characters. In addition, it encodes the following characters: , / ? : @ & = + $ #
                   * This function Return String. 
                   */
                    value = encodeURIComponent(value);
                    value="\""+value+"\"";
                }else {
                    switch(fields.get(j).type){
                        case "auto":
                            if(value!=undefined){
                                value=(value+"").trim();
                            }
                            value = value.replace(/\%/ig,"%25");              //commenting this code because the value is encode below as it is encoding % and + symbol twice
                            value = value.replace(/\+/ig,"%2b");
                            value=encodeURI(value);value="\""+value+"\"";break;
                        case "date":value="'"+WtfGlobal.convertToGenericDate(value)+"'";break;
                        case "string":
                            if(value == ""){
                                value="\""+value+"\"";
                            }
                            break;
                        case "boolean":
                            if(value === "" || value === undefined || value === null){
                                value = false;
                            }
                            break;
                    }
                }
                recarr.push(fields.get(j).name+":"+value);    //for saving the assetId no. Its showing %20 in space
            }
            recarr.push("modified:"+rec.dirty);
            arr.push("{"+recarr.join(",")+"}");
        }
        return "["+arr.join(',')+"]";
    },
    /*
     *return type : JSON array
     **/
    getJSONArrayWithEncoding:function(grid, includeLast, idxArr){ // Function is written for getting data in encoded format. It is used for passing data to java side with encoded format
        var indices="";
        if(idxArr)
            indices=":"+idxArr.join(":")+":";
        var store=grid.getStore();
        var arr=[];
        var fields=store.fields;
        var len=store.getCount()-1;
        if(includeLast)len++;
        for(var i=0;i<len;i++){
            if(idxArr&&indices.indexOf(":"+i+":")<0) continue;
            var rec=store.getAt(i);
            var recObj={};
            for(var j=0;j<fields.items.length;j++){
                var value=rec.data[fields.get(j).name]!=undefined?rec.data[fields.get(j).name]:'';   //ERP-12328 [SJ]
                if(fields.get(j).name=="srno" && fields.get(j).isForSequence) {    //refer ticket ERP-13781 - only for product grid in any form 
                    value = i+1;
                }
                recObj[fields.get(j).name]=value;    //for saving the assetId no. Its showing %20 in space
            }
            recObj["modified"]=rec.dirty;
            arr.push(recObj);
        }
        return arr;
    },
    getJSONArrayWithoutEncoding:function(grid, includeLast, idxArr){  // function is written for getting data without encoding. This is used for showing the data on UI side as it is (i.e. with special characters) without any changes
        var indices="";
        if(idxArr)
            indices=":"+idxArr.join(":")+":";
        var store=grid.getStore();
        var arr=[];
        var fields=store.fields;
        var len=store.getCount()-1;
        if(includeLast)len++;
        for(var i=0;i<len;i++){
            if(idxArr&&indices.indexOf(":"+i+":")<0) continue;
            var rec=store.getAt(i);
            var recarr=[];
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name];
                if(fields.get(j).name=='documentno' || fields.get(j).name=='customfield' || fields.get(j).name==CUSTOM_FIELD_KEY_PRODUCT || fields.get(j).name=='batchdetails' || fields.get(j).name=='assetDetails'||fields.get(j).name=='packingdodetails'||fields.get(j).name=='packingdetails') {
                    value=Wtf.encode(value);
                } else {
                    switch(fields.get(j).type){
                        case "auto":if(value!=undefined){
                                value=(value+"").trim();
                            }
                            value="\""+value+"\"";
                            break;
                        case "date":value="'"+WtfGlobal.convertToGenericDate(value)+"'";break;
                        case "string":value="\'"+value+"\'";break;
                    }
                }
                recarr.push(fields.get(j).name+":"+value);    
            }
            recarr.push("modified:"+rec.dirty);
            arr.push("{"+recarr.join(",")+"}");
        }
        return "["+arr.join(',')+"]";
    },
    getJSONArrayWithoutEncodingNew: function (grid, includeLast, idxArr) {  // function is written for getting data without encoding. This is used for showing the data on UI side as it is (i.e. with special characters) without any changes
        var indices = "";
        if (idxArr)
            indices = ":" + idxArr.join(":") + ":";
        var store = grid.getStore();
        var arr = [];
        var items = store.fields.items;
        var len = store.getCount() - 1;
        if (includeLast)
            len++;
        for (var i = 0; i < len; i++) {
            if (idxArr && indices.indexOf(":" + i + ":") < 0)
                continue;
            var rec = store.getAt(i);
            var recarr = {};
            for (var j = 0; j < items.length; j++) {
                var value = rec.data[items[j].name];
                switch (items[j].type) {
                    case "auto":
                        if (value != undefined) {
                            value = (value + "").trim();
                        }
//                        value = encodeURI(value);       //comment this like due to ticket SDP-9945
                        break;
                    case "date":
                        value = WtfGlobal.convertToGenericDate(value);
                        break;
                }
                recarr[items[j].name] = value;
            }
            recarr["modified"] = rec.dirty;
            arr.push(recarr);
        }
        return JSON.stringify(arr);
    },
    getJSONArrayForExactLength:function(grid, includeLast, idxArr){
        var indices="";
        if(idxArr)
            indices=":"+idxArr.join(":")+":";
        var store=grid.getStore();
        var arr=[];
        var fields=store.fields;
        var len=store.getCount();  //For Store Actual Length
        if(includeLast)len++;
        for(var i=0;i<len;i++){
            if(idxArr&&indices.indexOf(":"+i+":")<0) continue;
            var rec=store.getAt(i);
            var recarr=[];
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name];
                if(fields.get(j).name=='customfield' || fields.get(j).name==CUSTOM_FIELD_KEY_PRODUCT || fields.get(j).name=='batchdetails' || fields.get(j).name=='assetDetails') {
                    value=Wtf.encode(value);
                } else {
                    switch(fields.get(j).type){
                        case "auto":if(value!=undefined){value=(value+"").trim();}value=encodeURI(value);value="\""+value+"\"";break;
                        case "date":value="'"+WtfGlobal.convertToGenericDate(value)+"'";break;
                            }
                    }
                recarr.push(fields.get(j).name+":"+value);
                }
            recarr.push("modified:"+rec.dirty);
            arr.push("{"+recarr.join(",")+"}");
            }
        return "["+arr.join(',')+"]";
    },

    shortString:function(name){
        if(name.length > 20){
            return name.substr(0, 17) + '...';
        }
        return name;
    },
    scrollRec:function(grid,index) {
        var rowEl = grid.getView().getRow(index);
        var el=grid.getView().scroller;
        var a=Wtf.fly(rowEl).getOffsetsTo(el);
        el.scrollTo("top",a[1],true);
    },
    highLightAddRow:function(grid,duration,rec,color) {
        var row=grid.getStore().indexOf(rec);
        var rowEl = grid.getView().getRow(row);
        if(rowEl!=undefined) {
            var el=grid.getView().scroller;
            var a=Wtf.fly(rowEl).getOffsetsTo(el);
            el.scrollTo("top",a[1],true);
            Wtf.fly(rowEl).highlight(
            color,{
                attr: "background-color",
                duration: duration,
                endColor: "ffffff",
                easing: 'easeIn'
            });
        }
    },
    /*  Code for highlighting Row with particular color */
    highLightAddRowPermanently: function(grid, duration, rec, color) {
        var row = grid.getStore().indexOf(rec);
        var rowEl = grid.getView().getRow(row);
        if (rowEl != undefined) {
            var el = grid.getView().scroller;
            var a = Wtf.fly(rowEl).getOffsetsTo(el);
            el.scrollTo("top", a[1], true);
            Wtf.fly(rowEl).highlight(
                    color, {
                        attr: "background-color",
                        duration: duration,
                        endColor: color,
                        easing: 'easeIn'
                    });
        }
    },
    gridReloadDelay:function(){
        return 3000;
    },
    highLightRowColor:function(grid,recArr,addColor,strip,type,isrec){
        var color='FFFFFF';
        var duration = 2001;
        if(strip==1)
            color='FAFAFA';
        if(addColor){
        switch (type) {
            case 0:
                 color='FFFFCC';
                duration = 5;
                WtfGlobal.highLightAddRow(grid,duration,recArr,color);
                break;
            case 1:
                color='4E9258';
                break;
            case 3:
                color='F75D59';
                duration = 2.5;
                WtfGlobal.highLightAddRow(grid,duration,recArr,color);
                break;
            case 4:
                color='A3BAE9';
                duration = 2.5;
                WtfGlobal.highLightAddRow(grid,duration,recArr,color);
                break;
                case 5:
                    color = '#76d7c4';
                    WtfGlobal.highLightAddRowPermanently(grid, duration, recArr, color);
                    break;
                case 6:
                    color = '#ffff99';
                    WtfGlobal.highLightAddRowPermanently(grid, duration, recArr, color);
                    break;
                case 7:
                    color = '#f1948a';
                    WtfGlobal.highLightAddRowPermanently(grid, duration, recArr, color);
                    break;
            default:
                color='F75D59'; //delete
                duration = 2000;
                break;
            }
        }
        WtfGlobal.onlyhighLightRow(grid,duration,recArr,color,isrec)
    },
    onlyhighLightRow:function(grid,duration,recArr,color,isrec) {
         var index=0;
         var rowEl;
         var store;
         var row;
         if(isrec){
             store= grid.getStore();
            row=store.indexOf(recArr);
            rowEl = grid.getView().getRow(row);
            Wtf.fly(rowEl).highlight(
            color,{
                attr: "background-color",
                duration: duration,
                endColor: "ffffff",
                easing: 'easeIn',
                stopFx:true,
                concurrent:true
            });

         }
        else{
             while(index+1<=recArr.length){
                store= grid.getStore();
                row=store.indexOf(recArr[index]);
                rowEl = grid.getView().getRow(row);
                Wtf.fly(rowEl).highlight(
                color,{
                    attr: "background-color",
                    duration: duration,
                    endColor: "ffffff",
                    easing: 'easeIn',
                    stopFx:true,
                    concurrent:true
                });
                index++;
            }
        }
    },
    loadpersonacc:function(isCustomer){
        if(isCustomer!=undefined ||isCustomer!=null)
            isCustomer?Wtf.customerAccStore.load():Wtf.vendorAccStore.load();
    },
    enableDisableBtnArr:function(btnArr,grid,singleSelectArr,multiSelectArr){
        var multi = !grid.getSelectionModel().hasSelection();
        var single = (grid.getSelectionModel().getCount()!=1);
        for(var i=0;i<multiSelectArr.length;i++){
            btnArr[multiSelectArr[i]].setDisabled(multi);
            WtfGlobal.setTip(btnArr[multiSelectArr[i]]);
        }
        for(i=0;i<singleSelectArr.length;i++){
           if(btnArr[singleSelectArr[i]] != undefined){ 
                btnArr[singleSelectArr[i]].setDisabled(single);
                WtfGlobal.setTip(btnArr[singleSelectArr[i]]);
           }
        }
    },

    setTip:function(btn, tipText){
            var tooltip=btn.tooltip;
            var disabled=btn.disabled;
            if(!tooltip&&btn.isAction){
                tooltip=btn.initialConfig.tooltip;
                disabled=btn.initialConfig.disabled;
            }
            if(!tooltip)
                return;

            if(!tooltip.buttonTitle)tooltip.buttonTitle=btn.getText();
            tooltip.text=(tipText?tipText:(disabled?tooltip.dtext:tooltip.etext));
            if(tooltip.text){
                if(btn.setTooltip)
                    btn.setTooltip(tooltip.text);
                else
                    btn.setText("<span wtf:qtip='"+tooltip.text+"'>"+tooltip.buttonTitle+"</span>")
            }
    },
//To Do - Need to check when it is called.
    fetchAutoNumber:function(from, fn, scope){
        Wtf.Ajax.requestEx({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                mode:83,
                from:from
            }
        }, scope,function(resp){
            if(resp.success)
                fn.call(scope,resp)
            else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),resp.msg],resp.success*2+1);
            }
        });
    },

    formatNumber: function(v,format) {
            v = (Math.round((v-0)*100))/100;
            v = (v == Math.floor(v)) ? v + ".00" : ((v*10 == Math.floor(v*10)) ? v + "0" : v);
            v = String(v);
            var ps = v.split('.');
            var whole = ps[0];
            var sub = ps[1] ? '.'+ ps[1] : '.00';
            var r = /\d{1,3}(?=(\d{3})+(?!\d))/g;
            whole = whole.replace(r, '$&,');
            v = whole + sub;
            var temp="";
            if(v.charAt(0) == '-'){
                temp="-";
                v= v.substr(1);
            }
            var pat = /\{v\}/g;
            var val = " "+format;
            val = " "+val.replace(pat, v);
            pat = /\{c\}/g;
            val = " "+val.replace(pat, WtfGlobal.getCurrencySymbol());
            pat = /\{s\}/g;
            val = val.replace(pat, temp);

            return val;
    },

    formatNumberForVariableDigit: function(v,format) {
//            if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==4){
//                v = (Math.round((v-0)*10000))/10000;
//                v = (v == Math.floor(v)) ? v + ".0000" : (parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
//            }else if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==3){
//                v = (Math.round((v-0)*1000))/1000;
//                v = (v == Math.floor(v)) ? v + ".000" : (parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
//            }else {
//                v = (Math.round((v-0)*100))/100;
//                v = (v == Math.floor(v)) ? v + ".00" : (parseFloat(v).toFixed(2));
//            }
            v=getRoundedAmountValue(v);
             v=parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
            v = String(v);
            var ps = v.split('.');
            var whole = ps[0];
            if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==4){
                var sub = ps[1] ? '.'+ ps[1] : '.0000';
            }else if(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL==3){
                sub = ps[1] ? '.'+ ps[1] : '.000';
            }else{
                sub = ps[1] ? '.'+ ps[1] : '.00';
            }  
            var r = /\d{1,3}(?=(\d{3})+(?!\d))/g;
            whole = whole.replace(r, '$&,');
            v = whole + sub;
            var temp="";
            if(v.charAt(0) == '-'){
                temp="-";
                v= v.substr(1);
            }
            var pat = /\{v\}/g;
            var val = " "+format;
            val = " "+val.replace(pat, v);
            pat = /\{c\}/g;
            val = " "+val.replace(pat, WtfGlobal.getCurrencySymbol());
            pat = /\{s\}/g;
            val = val.replace(pat, temp);

            return val;
    },
    showFormElement:function(obj){
        if(obj.container){
        obj.container.up('div.x-form-item').dom.style.display='block';
        }
    },
    showFormLabel:function(obj){
        if(obj.container){
            obj.container.up('div.x-form-item').dom.classList.remove("x-hide-label");
        }
    },
    hideFormElement:function(obj){
        if(obj.container){
        obj.container.up('div.x-form-item').dom.style.display='none';
        }
    },
    updateFormLabel:function(obj,newLabel){
        if(obj.container){
        obj.container.up('div.x-form-item').child('label.x-form-item-label').update(newLabel);
        }
    },
    hideFormLabel:function(obj){
        if(obj.container){
            obj.container.up('div.x-form-item').child('label.x-form-item-label').hide();
        }
    },
    showLabel:function(obj){
        if(obj.container){
            obj.container.up('div.x-form-item').child('label.x-form-item-label').show();
        }
    },
    addLabelHelp:function(HelpText){
        return "<span wtf:qtip=\""+HelpText+"\" class=\"formHelpButton\">&nbsp;&nbsp;&nbsp;&nbsp;</span>";
    },
    autoApplyHeaderQtip : function(grid){
        var m = grid.elMetrics;
        if (!grid.elMetrics) {
            m = Wtf.util.TextMetrics.createInstance(grid.container);
        }
        var cm = grid.getColumnModel();
        var vw = grid.getView();
        for(var i=0; i<cm.getColumnCount(); i++) {
            if(!cm.isHidden(i)) {
                var columnWidth = cm.getColumnWidth(i);
                var columnText = cm.getColumnHeader(i);
                var textwidth = m.getWidth(columnText);
                if(textwidth > columnWidth) {
//                    cm.setColumnTooltip(i, columnText);
                    var headerCell = vw.getHeaderCell(i).lastChild;
                    headerCell.innerHTML = "<span Wtf:qtip=\""+columnText+"\">"+headerCell.innerHTML+"<span>";
                }
            }
        }
    },

    getDates:function(start, SavedSearchDate) {
        if(SavedSearchDate != undefined){            
            return new Date(SavedSearchDate);
        }
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
     getBookBeginningDates:function(start) {
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.depreciationCalculationBasedOn==Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
            if(Wtf.account.companyAccountPref.bbfrom)
                monthDateStr=Wtf.account.companyAccountPref.bbfrom.format('M d');
        }else{
            if(Wtf.account.companyAccountPref.fyfrom)
                monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
            
    getGeneratedOnTimestamp : function(){
        var generateOnDate=new Date();
        return generateOnDate.format('F j, Y g:i:s A');
    },
    
    getBookBeginningYear:function(isfirst, isFromFixedAsset){
        var ffyear;
        if(isfirst){
            var cfYear=new Date(Wtf.account.companyAccountPref.fyfrom)
            if(isFromFixedAsset && Wtf.account.companyAccountPref.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                ffyear=new Date(Wtf.account.companyAccountPref.bbfrom)
                ffyear=new Date(ffyear.getFullYear(),ffyear.getMonth(),ffyear.getDate()).clearTime()
            } else {
                ffyear=new Date(Wtf.account.companyAccountPref.firstfyfrom)
                ffyear=new Date( ffyear.getFullYear(),cfYear.getMonth(),cfYear.getDate()).clearTime()
            }
        }else{
            var fyear=new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear()
            ffyear=new Date( fyear,this.fmonth.getValue(),this.fdays.getValue()).clearTime()
        }

        var data=[];
        var newrec;
        if(ffyear==null||ffyear=="NaN"){
            ffyear=new Date(Wtf.account.companyAccountPref.fyfrom)
        }
        var year=ffyear.getFullYear();
        var temp=new Date();
        var year1=temp.getFullYear();
        var i = 0;
        while (year1 + 5 >= year) {
            data.push([i, year++]);
            i++;
        }
        return data;
    },
    
    exportAllData:function(mode,filename,type,startdate,enddate,productExportCallPost,totalRecords,paramstring){
        var module="";
        var fetchCustomFields=false;
        startdate = startdate!=undefined?startdate:"";
        enddate = enddate!=undefined?enddate:"";
        if(mode==113){//customer
            module=Wtf.ExportMolueName.customer;
        }else if(mode==114){//vendor
            module=Wtf.ExportMolueName.vendor;
        }else if(mode==198){//product
            module=Wtf.ExportMolueName.product;
            fetchCustomFields=true;
        } else if (mode == 112) { // Account
            module = Wtf.ExportMolueName.Account;
            fetchCustomFields = true;
        } else if(mode==Wtf.autoNum.GroupExport){
            module=Wtf.ExportMolueName.Group;
        } else if(mode==Wtf.autoNum.assetGroupExport){
            module=Wtf.ExportMolueName.Fixed_Asset_Group;
        }
        var header = [];
        var title = [];
        var width = [];
        var align=[];
        var exportUrl=getExportUrl(mode);
        this.ccrecord=new Wtf.data.Record.create([
          {name:"id"},
          {name:"columnName"},
          {name:"dataindex"},
          {name:"renderertype"}
        ]);    
        this.ccstore=new Wtf.data.Store({
           reader:new Wtf.data.KwlJsonReader({
             totalProperty:'count',
             root: "data"  
           },this.ccrecord),
        url: "ImportRecords/getColumnConfig.do",
        baseParams:{
            module:module,
            isExport:true,
            fetchCustomFields:fetchCustomFields
        }
      });
      
      this.ccstore.on('load',function(store){
        if(store.getCount()>0){
            for(var cntVal=0;cntVal<store.getCount();cntVal++){
                var rec=store.getAt(cntVal);
                if(rec.data.dataindex!=""){
                    header.push(rec.data.dataindex) ;
                    title.push(encodeURIComponent(rec.data.columnName)) ;
                    align.push(rec.data.renderertype) ;
                    width.push(40) ;   
                }                      
            }  
            
           var nondeleted="true"   
           var url = exportUrl+"?filename="+encodeURIComponent(filename)+"+&filetype="+type+"&nondeleted="+nondeleted+"&header="+header+"&title="+encodeURIComponent(title)+"&width="+width+"&get="+mode+"&align="+align+paramstring+"&startdate="+startdate+"&enddate="+enddate+((mode==198 || mode==1110)?"&isExport=true":"")+(mode==1110?"&isFixedAsset=true":"")+(totalRecords !=undefined? "&totalProducts="+totalRecords:"");
          if (productExportCallPost != undefined && productExportCallPost) { // Currently used for only Product,Sales by item Report
                Wtf.Ajax.requestEx({
                    url: url
                }, this,
                function () {

                    }, function () {

                    });
            } else {
                Wtf.get('downloadframe').dom.src  = url;              
            }
       }        
    },this);
        if (mode == 198) {//If Module is Product then We are not sending request for column config
            var nondeleted = "true"
            var fetchCustomFields="true"
            var otherThanAboveCheckForProductMaster="true"
            var url = exportUrl + "?filename=" + encodeURIComponent(filename) + "+&filetype=" + type + "&nondeleted=" + nondeleted  +"&fetchCustomFields="+fetchCustomFields+ "&otherThanAboveCheckForProductMaster="+otherThanAboveCheckForProductMaster+ "&get=" + mode + paramstring + "&startdate=" + startdate + "&enddate=" + enddate + ((mode == 198 || mode == 1110) ? "&isExport=true" : "") + (mode == 1110 ? "&isFixedAsset=true" : "") + (totalRecords != undefined ? "&totalProducts=" + totalRecords : "");
            if (productExportCallPost != undefined && productExportCallPost) { // Currently used for only Product,Sales by item Report
                Wtf.Ajax.requestEx({
                    url: url
                }, this,
                        function() {

                        }, function() {

                });
            } else {
                Wtf.get('downloadframe').dom.src = url;
            }
        }else{
    this.ccstore.load();   
        }   
    },
    
    dispalyErrorMessageDetails: function(containerID, invalidfieldInfoArray){
        var invalidFields='';
        for(var i=0; i<invalidfieldInfoArray.length;i++){
            var field=invalidfieldInfoArray[i];
            var label=field.fieldLabel.replace('*','');
//            if(invalidFields!=='') {
//                invalidFields+='</br>';
//            }
            invalidFields+='<div style="height:20px"><span class="errorInfoMsg">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>'+'<b>'+label+'</b>'+':'+((field.errorIcon != undefined)?field.errorIcon.dom.qtip:WtfGlobal.getLocaleText("acc.product.msg1"))+'</div>';
        }
        Wtf.getCmp(containerID).body.update(invalidFields);
        Wtf.getCmp(containerID).show();
        document.getElementById(containerID).scrollIntoView();
    },
    exportReportInThread: function(obj){
        var url = obj.url + "?";
        var scope = obj.scope;
        var type = obj.type;
        var threadflag = obj.threadflag;
        var paramsWithoutURL = obj.paramsWithoutURL;
        var paramaters = obj.paramaters;
        url += paramaters;
        if (scope.isProductExport && scope.isProductExportRecordsGreaterThanThousands) {
            Wtf.Ajax.requestEx({
                url: url
            }, this,
                    function () {

                    }, function () {

            });
        } else if (scope.usePostMethod) {
            this.postData(url, obj.paramsWithoutURL);
        } else if (scope.get == Wtf.autoNum.Dimension_Based_TradingAndProfitLoss && threadflag && type == "xls") {

            Wtf.MessageBox.show({
                title: this.getLocaleText("acc.common.info"), //'Warning',
                msg: this.getLocaleText("acc.generalLedger.msg.downloadTakeTime"),
                buttons: Wtf.MessageBox.YESNOCANCEL,
                closable: false,
                fn: function (btn) {
                    if (btn == "yes") {
                        url += "&threadflag=" + true;
                        Wtf.get('downloadframe').dom.src = url;
                    } else if (btn == "no") {
                        url += "&threadflag=" + false;
                        Wtf.get('downloadframe').dom.src = url;
                    }
                },
                scope: this,
                icon: Wtf.MessageBox.QUESTION
            });
        } else if (scope.get == Wtf.autoNum.GroupDetailReport && scope.params.exportThreadFlagLedger && (type == "xls" || type == "detailedXls")) {

            Wtf.MessageBox.show({
                title: this.getLocaleText("acc.common.info"), //'Warning',
                msg: this.getLocaleText("acc.generalLedger.msg.downloadTakeTime"),
                buttons: Wtf.MessageBox.YESNOCANCEL,
                closable: false,
                fn: function (btn) {
                    if (btn == "yes") {
                        url += "&threadflag=" + true;
                        Wtf.get('downloadframe').dom.src = url;
                    } else if (btn == "no") {
                        url += "&threadflag=" + false;
                        Wtf.get('downloadframe').dom.src = url;
                    }
                },
                scope: this,
                icon: Wtf.MessageBox.QUESTION
            });
        } else {
            Wtf.get('downloadframe').dom.src = url;
        }
    },
    
    onFormSumbitGetDisableFieldValues:function (formitems, requestParamObject) {
        formitems.each(function(item){
            if(item.el.dom.disabled) {
                var v = '';
                if(item.value) {
                    v = item.value;
                } else {
                    v = item.el.getValue();
                    if(v === item.emptyText || v === undefined){
                        v = '';
                    }
                }
                if(item.name) {
                    requestParamObject[item.name] = v;
                } else if(item.hiddenName) {
                    requestParamObject[item.hiddenName] = v;
                }
            }
        });
    },
    
    updateStoreConfigForDynamicColModel:function(colModelArray, store) {
        if(colModelArray){
            for(var cnt = 0;cnt < colModelArray.length;cnt++){
                var fieldname = colModelArray[cnt].fieldname;
                var newField = new Wtf.data.ExtField({
                    name:fieldname.replace(".",""),
                    defValue:colModelArray[cnt].fieldtype == 3 ? colModelArray[cnt].fieldData:colModelArray[cnt].fieldData,
                    sortDir:'ASC',
                    type:colModelArray[cnt].fieldtype == 3 ?  'date' : (colModelArray[cnt].fieldtype == 2?'float':'auto'),
                    dateFormat:colModelArray[cnt].fieldtype == 3 ?  'time' : undefined
                });
                store.fields.items.push(newField);
                store.fields.map[fieldname]=newField;
                store.fields.keys.push(fieldname);
            }
            store.reader = new Wtf.data.KwlJsonReader2(store.reader.meta, store.fields.items);
        } 
    },
    
    updateStoreConfig:function(colModelArray, store) {
        if(colModelArray){
            for(var cnt = 0;cnt < colModelArray.length;cnt++){
                var fieldname = colModelArray[cnt].fieldname;
                var newField = new Wtf.data.ExtField({
                    name:fieldname.replace(".",""),
                    defValue:colModelArray[cnt].fieldtype == 3 ? colModelArray[cnt].fieldData:colModelArray[cnt].fieldData,
                    sortDir:'ASC',
                    type:colModelArray[cnt].fieldtype == 3 ?  'date' : (colModelArray[cnt].fieldtype == 2?'float':'auto'),
                    mapping : colModelArray[cnt].mapping,
                    dateFormat:colModelArray[cnt].fieldtype == 3 ?  null : undefined
                });
                store.fields.items.push(newField);
                store.fields.map[fieldname]=newField;
                store.fields.keys.push(fieldname);
//                store.fields.length++;
            }
            store.reader = new Wtf.data.KwlJsonReader(store.reader.meta, store.fields.items);
        } 
    },
    updateStoreConfigStringDate:function(colModelArray, store) {
        if(colModelArray){
            for(var cnt = 0;cnt < colModelArray.length;cnt++){
                 var fieldname = colModelArray[cnt].fieldname;
                var defaultValue = "";
                if (colModelArray[cnt].fieldtype == 3 && colModelArray[cnt].fieldData!=undefined) {
                    defaultValue = new Date(colModelArray[cnt].fieldData * 1).format(WtfGlobal.getOnlyDateFormat());
                }
                var newField = new Wtf.data.ExtField({
                    name:fieldname.replace(".",""),
                    defValue:colModelArray[cnt].fieldtype == 3 ? defaultValue:colModelArray[cnt].fieldData,
                    sortDir:'ASC',
                    type:colModelArray[cnt].fieldtype == 3 ?  'date' : (colModelArray[cnt].fieldtype == 2?'float':'auto'),
                    dateFormat:colModelArray[cnt].fieldtype == 3 ?  null : undefined
                });
                store.fields.items.push(newField);
                store.fields.map[fieldname]=newField;
                store.fields.keys.push(fieldname);
            }
            store.reader = new Wtf.data.KwlJsonReader(store.reader.meta, store.fields.items);
        } 
    },
    
    getColumnIndex: function(grid, dataIndex) {
        var value = grid.getColumnModel().getColumnsBy(function(columnConfig, index){
                return (columnConfig.dataIndex == dataIndex);
        });
        if(value) {
            return value[0].id;
        }
    },
    
    forInAnotherObject: function(originalObject, sourceObject) {
        for (var key in sourceObject) {
            if (sourceObject.hasOwnProperty(key)) {
                originalObject[key] = sourceObject[key]
            }
        }
        return originalObject;
    },
    
    setAjaxTimeOutFor5Minutes : function(){
        Wtf.Ajax.timeout = 300000; // set 5min
    },
    setAjaxTimeOut: function() {
        Wtf.Ajax.timeout = 900000; // set 15mins
    },
    
    setAjaxTimeOutFor30Minutes: function() {
        Wtf.Ajax.timeout = 1800000; // set 30mins
    },
    
    resetAjaxTimeOut: function() {
        Wtf.Ajax.timeout = 30000; // reset to 30secs
    },
    
    getModuleId : function(scope){
        var moduleId = "";
        if(scope.cash){
            if(scope.isCustomer){
                moduleId = Wtf.Acc_Cash_Sales_ModuleId;
            }else{
                moduleId = Wtf.Acc_Cash_Purchase_ModuleId;
            }
        } else if(scope.isOrder && !scope.quotation){
            if (scope.isJobWorkOrderReciever) {
                moduleId = Wtf.MRP_Job_Work_ORDER_REC;
            } else if(scope.isSecurityGateEntry) {
                moduleId = Wtf.Acc_Security_Gate_Entry_ModuleId;
            } else if(scope.isCustomer){
                moduleId = Wtf.Acc_Sales_Order_ModuleId;
            }else{
                moduleId = Wtf.Acc_Purchase_Order_ModuleId;
            }
        } else if(scope.quotation){
            if(scope.isCustomer){
                moduleId = Wtf.Acc_Customer_Quotation_ModuleId;
            }else{
                moduleId = Wtf.Acc_Vendor_Quotation_ModuleId;
            }
        }else{
            if(scope.isCustomer){
                moduleId = Wtf.Acc_Invoice_ModuleId;
            }else{
                moduleId = Wtf.Acc_Vendor_Invoice_ModuleId;
            }
        }
        return moduleId;
    },
    
        /**
         * isTemplate for create template
         * isViewtemplate for view template
         * isEditTemplate for edit template
         */
    openModuleTab : function(scope, isCustomer, isQuotation, isOrder, copyInv, templateId, fromrec,isViewTemplate,isEditTemplate) {
        var incash=fromrec.get("incash");
        var billid=fromrec.get("billid");
        var label="From_Template_"+scope.id;
        if(isOrder && !isQuotation){
            scope.withInvMode = fromrec.get("withoutinventory");
            if(!isCustomer){
                var tranType=isCustomer?(isOrder?Wtf.autoNum.SalesOrder:Wtf.autoNum.Invoice):(isOrder?Wtf.autoNum.PurchaseOrder:Wtf.autoNum.GoodsReceipt);
                callEditPurchaseOrder(true,fromrec,label+billid,false,scope,tranType, copyInv, templateId,isViewTemplate,undefined,undefined,undefined,isEditTemplate);
            //            else{								
            //                var tranType=isCustomer?(isOrder?Wtf.autoNum.BillingSalesOrder:Wtf.autoNum.BillingInvoice):(isOrder?Wtf.autoNum.BillingPurchaseOrder:Wtf.autoNum.BillingGoodsReceipt);
            //                callBillingPurchaseOrder(true,formrec,label+billid,false,this,tranType, copyInv, templateId);
            //            }
            }
            else{
                callSalesOrder(true,fromrec,label+billid, copyInv,templateId,isViewTemplate,undefined,undefined,undefined,undefined,undefined,isEditTemplate);
            //            else{								
            //                    callBillingSalesOrder(true,formrec,label+billid, copyInv,templateId);
            //            }
            }
    
        }else if(isQuotation){
            callQuotation(true, label+billid, fromrec, copyInv, false, templateId, isViewTemplate,undefined,isEditTemplate);
        }else{
            if(isCustomer){
            //                if(Wtf.account.companyAccountPref.withoutinventory){
            //                    if(incash)
            //                        callEditBillingSalesReceipt(formrec, label+'BillingCSInvoice',copyInv, templateId);
            //                    else
            //                        callEditBillingInvoice(formrec, label+'BillingInvoice',copyInv, templateId);
            //                }
            //                else
            {
                if(incash)
                    callEditCashReceipt(fromrec, label+'CashSales',copyInv, templateId,isViewTemplate,undefined,undefined,isEditTemplate);
                else
                    callEditInvoice(fromrec, label+'Invoice',copyInv, templateId,isViewTemplate,undefined,undefined,undefined,undefined,undefined,undefined,isEditTemplate);
            }
            }else{
            //                if(Wtf.account.companyAccountPref.withoutinventory){
            //                    if(incash)
            //                        callEditBillingPurchaseReceipt(formrec, label+'BillingCSInvoice',copyInv,templateId);
            //                    else
            //                        callEditBillingGoodsReceipt(formrec,  label+'BillingInvoice',copyInv, templateId);
            //                }
            //                else
            {
                if(incash)
                    callEdiCashPurchase(fromrec, label+'PaymentReceipt',copyInv,false,templateId,isViewTemplate,undefined,undefined,isEditTemplate);
                else
                    callEditGoodsReceipt(fromrec, label+'GoodsReceipt',copyInv,false, templateId,isViewTemplate,undefined,undefined,undefined,isEditTemplate);
            }
            }
        }
    },
    
    /* Create FnCombobox.
     * Parameters
     * 1. config : Specific config to be applied to FnCombobox
     * 2. store : Specific store for FnCombobox
     **/
    createFnCombobox: function(config, store, valueField, displayField, obj){
        var combo = new Wtf.form.FnComboBox(Wtf.applyIf({
            width: 240,
            store: store,
            valueField: valueField,
            displayField: displayField,
            scope: obj,
            forceSelection: true,
            selectOnFocus: true
        },config));
        
        return combo;
    },
    
    //Create ExtFnCombobox
    createExtFnCombobox: function(config, store, valueField, displayField, obj){
        var combo = new Wtf.form.ExtFnComboBox(Wtf.applyIf({
            width: 240,
            store: store,
            valueField: valueField,
            displayField: displayField,
            scope: obj,
            forceSelection: true,
            selectOnFocus: true,
            typeAhead: true,
            typeAheadDelay:30000,
            triggerAction:'all'
        },config));
        
        return combo;
    },
    
    //Create Combobox
    createCombobox: function(config, store, valueField, displayField, obj){
        var combo = new Wtf.form.ComboBox(Wtf.applyIf({
            triggerAction:'all',
            typeAhead: true,
            store: store,
            valueField: valueField,
            displayField: displayField,
            scope: obj,
            forceSelection: true,
            selectOnFocus: true
        },config));
        
        return combo;
    },
    
    //Create TextField
    createTextfield: function(config, disabled, allowBlank, maxLength, obj){
        var textField = new Wtf.form.TextField(Wtf.applyIf({
            scope: obj,
            width: 240,
            disabled: disabled,
            allowBlank: allowBlank,
            maxLength: maxLength
        },config));
        
        return textField;
    },
        //Create Number Field
    createNumberfield: function(config, disabled, allowBlank, maxLength, obj){
        var numberField = new Wtf.form.NumberField(Wtf.applyIf({
            scope: obj,
            width: 240,
            disabled: disabled,
            allowBlank: allowBlank,
            maxLength: maxLength
        },config));
        
        return numberField;
    },
    createDatefield: function(config, allowBlank, obj){
        var dateField = new Wtf.form.DateField(Wtf.applyIf({
            format: WtfGlobal.getOnlyDateFormat(),
            allowBlank: allowBlank,
            width: 240,
            scope: obj
        },config));
        
        return dateField;
    },
    
    /* Parameters
     * 1. array : Contains all buttons
     * 2. index : Location at which comp is to be added
     * 3. comp : Actual comopnent
     **/
    addComponentAtIndex: function(array, index, comp){
        var newArray = new Array();
        if(array.length>0){
            for(var i=0 ; i<array.length ; i++){
                if(i==index){
                    newArray.push(comp);
                    newArray.push(array[i]);
                }else{
                    newArray.push(array[i]);
                }
            }
        }else{
            newArray.push(comp);
        }
        
        return newArray;
    },
    
    getCompanyAccountPrefObj: function(choice){
        var companyPrefObj = '';
        switch(choice){
            case Wtf.companyAccountPref_custvenloadtype :companyPrefObj = Wtf.account.companyAccountPref.custvenloadtype;
                break;
            case Wtf.companyAccountPref_withinvupdate :companyPrefObj = Wtf.account.companyAccountPref.withinvupdate;
                break;
            case Wtf.companyAccountPref_activateProfitMargin :companyPrefObj = Wtf.account.companyAccountPref.activateProfitMargin;
                break;
            case Wtf.companyAccountPref_memo :companyPrefObj = Wtf.account.companyAccountPref.memo;
                break;
            case Wtf.companyAccountPref_descriptionType :companyPrefObj = Wtf.account.companyAccountPref.descriptionType;
                break;
            case Wtf.companyAccountPref_allowZeroUntiPriceForProduct :companyPrefObj = Wtf.account.companyAccountPref.allowZeroUntiPriceForProduct;
                break;
            case Wtf.companyAccountPref_termsincludegst :companyPrefObj = Wtf.account.companyAccountPref.termsincludegst;
                break;
            case Wtf.companyAccountPref_productOptimizedFlag :companyPrefObj = Wtf.account.companyAccountPref.productOptimizedFlag;
                break;
            case Wtf.companyAccountPref_autoPopulateMappedProduct :companyPrefObj = Wtf.account.companyAccountPref.autoPopulateMappedProduct;
                break;
            case Wtf.companyAccountPref_isDuplicateItems :companyPrefObj = Wtf.account.companyAccountPref.isDuplicateItems;
                break;
            case Wtf.companyAccountPref_countryid :companyPrefObj = Wtf.account.companyAccountPref.countryid;
                break;
            case Wtf.companyAccountPref_noOfDaysforValidTillField :companyPrefObj = Wtf.account.companyAccountPref.noOfDaysforValidTillField;
                break;
            case Wtf.companyAccountPref_autoquotation :companyPrefObj = Wtf.account.companyAccountPref.autoquotation;
                break;
            case Wtf.companyAccountPref_autovenquotation :companyPrefObj = Wtf.account.companyAccountPref.autovenquotation;
                break;
            case Wtf.companyAccountPref_enableGST :companyPrefObj = Wtf.account.companyAccountPref.enableGST;
                break;
            case Wtf.companyAccountPref_retainExchangeRate :companyPrefObj = Wtf.account.companyAccountPref.retainExchangeRate;
                break;
            case Wtf.companyAccountPref_currencyid :companyPrefObj = Wtf.account.companyAccountPref.currencyid;
                break;
            case Wtf.companyAccountPref_fyfrom :companyPrefObj = Wtf.account.companyAccountPref.fyfrom;
                break;
            case Wtf.companyAccountPref_bbfrom :companyPrefObj = Wtf.account.companyAccountPref.bbfrom;
                break;
            case Wtf.companyAccountPref_accountsWithCode :companyPrefObj = Wtf.account.companyAccountPref.accountsWithCode;
                break;
            case Wtf.companyAccountPref_activateToBlockSpotRate :companyPrefObj = Wtf.account.companyAccountPref.activateToBlockSpotRate;
                break;
            case Wtf.companyAccountPref_deliveryPlanner :
                companyPrefObj =Wtf.account.companyAccountPref.deliveryPlanner;
                break;                
            case Wtf.companyAccountPref_isBatchCompulsory :
                companyPrefObj =Wtf.account.companyAccountPref.isBatchCompulsory;
                break;
            case Wtf.companyAccountPref_restrictDuplicateBatch :
                companyPrefObj =Wtf.account.companyAccountPref.restrictDuplicateBatch;
                break;
            case Wtf.companyAccountPref_isSerialCompulsory :
                companyPrefObj =Wtf.account.companyAccountPref.isSerialCompulsory;
                break;
            case Wtf.companyAccountPref_isLocationCompulsory :
                companyPrefObj =Wtf.account.companyAccountPref.isLocationCompulsory;
                break;   
            case Wtf.companyAccountPref_isWarehouseCompulsory :
                companyPrefObj =Wtf.account.companyAccountPref.isWarehouseCompulsory;
                break;    
            case Wtf.companyAccountPref_isRowCompulsory :
                companyPrefObj =Wtf.account.companyAccountPref.isRowCompulsory;
                break;   
            case Wtf.companyAccountPref_isRackCompulsory :
                companyPrefObj =Wtf.account.companyAccountPref.isRackCompulsory;
                break;              
            case Wtf.companyAccountPref_isBinCompulsory :
                companyPrefObj =Wtf.account.companyAccountPref.isBinCompulsory;
                break;       
            case Wtf.companyAccountPref_shipDateConfiguration :
                companyPrefObj =Wtf.account.companyAccountPref.shipDateConfiguration;
                break;         
            case Wtf.companyAccountPref_autogoodsreceipt :
                companyPrefObj = Wtf.account.companyAccountPref.autogoodsreceipt;
                break;
            case Wtf.companyAccountPref_autocashpurchase :
                companyPrefObj = Wtf.account.companyAccountPref.autocashpurchase;
                break;
            case Wtf.companyAccountPref_autoinvoice :
                companyPrefObj = Wtf.account.companyAccountPref.autoinvoice;
                break;
            case Wtf.companyAccountPref_autocashsales :
                companyPrefObj = Wtf.account.companyAccountPref.autocashsales;
                break;
            case Wtf.companyAccountPref_autodo :
                companyPrefObj = Wtf.account.companyAccountPref.autodo;
                break;
            case Wtf.companyAccountPref_autogro :
                companyPrefObj = Wtf.account.companyAccountPref.autogro;
                break;
            case Wtf.companyAccountPref_cashaccount :
                companyPrefObj = Wtf.account.companyAccountPref.cashaccount;
                break;
            case Wtf.companyAccountPref_custcreditlimit :
                companyPrefObj = Wtf.account.companyAccountPref.custcreditlimit;
                break;
            case Wtf.companyAccountPref_negativestock :
                companyPrefObj = Wtf.account.companyAccountPref.negativestock;
                break;
        }
        return companyPrefObj;
    },
    
    getHideFormFieldObj: function(choice){
        var hideFormFieldObj = '';
        switch(choice){
            case Wtf.HideFormFieldProperty_customerQuotation :hideFormFieldObj = Wtf.account.HideFormFieldProperty.customerQuotation;
                break;
            case Wtf.HideFormFieldProperty_vendorQuotation :hideFormFieldObj = Wtf.account.HideFormFieldProperty.vendorQuotation;
                break;
            case Wtf.HideFormFieldProperty_customerInvoice :
                hideFormFieldObj = Wtf.account.HideFormFieldProperty.customerInvoice;
                break;
            case Wtf.HideFormFieldProperty_CS :
                hideFormFieldObj = Wtf.account.HideFormFieldProperty.CS;
                break;
            case Wtf.HideFormFieldProperty_vendorInvoice :
                hideFormFieldObj = Wtf.account.HideFormFieldProperty.vendorInvoice;
                break;
            case Wtf.HideFormFieldProperty_CP :
                hideFormFieldObj = Wtf.account.HideFormFieldProperty.CP;
                break;
        }
        return hideFormFieldObj;
    },
    
    getUPermObj: function(choice){
        var UPermObj = '';
        switch(choice){
            case Wtf.UPerm_customer :UPermObj = Wtf.UPerm.customer;
                break;
            case Wtf.UPerm_vendor :UPermObj = Wtf.UPerm.vendor;
                break;
            case Wtf.UPerm_invoice :UPermObj = Wtf.UPerm.invoice;
                break;
            case Wtf.UPerm_vendorinvoice :UPermObj = Wtf.UPerm.vendorinvoice;
                break;
            case Wtf.UPerm_vendorpr :UPermObj = Wtf.UPerm.vendorpr;
                break;
            case Wtf.UPerm_creditterm :UPermObj = Wtf.UPerm.creditterm;
                break;
            case Wtf.UPerm_tax :UPermObj = Wtf.UPerm.tax;
                break;
        }
        return UPermObj;
    },
    
    getPermObj: function(choice){
        var PermObj = '';
        switch(choice){
            case Wtf.Perm_customer :PermObj = Wtf.Perm.customer;
                break;
            case Wtf.Perm_vendor :PermObj = Wtf.Perm.vendor;
                break;
            case Wtf.Perm_invoice_createso :PermObj = Wtf.Perm.invoice.createso;
                break;
            case Wtf.Perm_vendorpr :PermObj = Wtf.Perm.vendorpr;
                break;
            case Wtf.Perm_vendorinvoice_createpo :PermObj = Wtf.Perm.vendorinvoice.createpo;
                break;
            case Wtf.Perm_invoice :PermObj = Wtf.Perm.invoice;
                break;
            case Wtf.Perm_vendorinvoice :PermObj = Wtf.Perm.vendorinvoice;
                break;
            case Wtf.Perm_invoice_createreceipt :PermObj = Wtf.Perm.invoice.createreceipt;
                break;
            case Wtf.Perm_vendorinvoice_createpayment :PermObj = Wtf.Perm.vendorinvoice.createpayment;
                break;
            case Wtf.Perm_creditterm_edit :PermObj = Wtf.Perm.creditterm.edit;
                break;
            case Wtf.Perm_tax_view :PermObj = Wtf.Perm.tax.view;
                break;
        }
        return PermObj; 
    },
    getAddressRecordsForSave: function(recordsToSave, transactionRecord, linkRecord, userGivenAddrRecord, isCustomer, isSingleLink, isEdit, isCopy, generatePO, generateSO, isQuotationFromPR, moduleid) {
        /*recordsToSave : This is record which is going as request Param on save 
         *transactionRecord : This is record of any transaction in edit or copy case
         *linkRecord : Record of link transaction
         *userGivenAddrRecord : Record of user address which is entered by user manually when "save address" window is open
         *generatePO and generateSO flag will be true when we create click Generate SO and PO button in PO Report and SO Report.
         **/
        
        /*-----Executed for Dropship Type document--------- */
        if (CompanyPreferenceChecks.activateDropShip() && recordsToSave.isdropshipchecked != undefined && recordsToSave.isdropshipchecked && (moduleid == Wtf.Acc_Purchase_Order_ModuleId || moduleid == Wtf.Acc_Vendor_Invoice_ModuleId)) {

            if (userGivenAddrRecord != "" && userGivenAddrRecord != undefined) {
                recordsToSave = WtfGlobal.getAddressRecordsForDropShipDocument(recordsToSave, userGivenAddrRecord);
            } else if (linkRecord != "" && linkRecord != undefined && isSingleLink) {
                recordsToSave = WtfGlobal.getAddressRecordsForDropShipDocument(recordsToSave, linkRecord.data);
                if (moduleid == Wtf.Acc_Purchase_Order_ModuleId)
                    recordsToSave.defaultAdress = true;
            } else if (isEdit && !isCopy && transactionRecord) {
                recordsToSave = WtfGlobal.getAddressRecordsForDropShipDocument(recordsToSave, transactionRecord.data);
            }

        } else {

            if (userGivenAddrRecord != "" && userGivenAddrRecord != undefined) { //This is the case when user manually saved address in "show address" form
                recordsToSave = WtfGlobal.getAddressRecordsFromUser(recordsToSave, userGivenAddrRecord, isCustomer);
            } else if (linkRecord != "" && linkRecord != undefined && isSingleLink) { //This is the case when user Linked some other document while creating document. in this case linked document address will get saved
                recordsToSave = WtfGlobal.getAddressRecordsFromExistingRecord(recordsToSave, linkRecord, isCustomer);
            } else if ((isEdit || isCopy) && transactionRecord && !(generatePO || generateSO || isQuotationFromPR)) { //Edit/copy case of document, except generate so/po case and Recor VQ case
                recordsToSave = WtfGlobal.getAddressRecordsFromExistingRecord(recordsToSave, transactionRecord, isCustomer);
            } else {
                recordsToSave.defaultAdress = true;
            }
        }

        return recordsToSave;
    },
    getAddressRecordsFromUser:function(recordsToSave,userGivenAddress,isCustomer){ //This method used for putting address details in save record, which are manually given by user in Show address component.
        if(userGivenAddress!="" && userGivenAddress!=undefined){
            recordsToSave.billingAddressType=userGivenAddress.billingAddrsCombo;
            recordsToSave.billingAddress=userGivenAddress.billingAddress;
            recordsToSave.billingCounty=userGivenAddress.billingCounty;
            recordsToSave.billingCity=userGivenAddress.billingCity;
            recordsToSave.billingState=userGivenAddress.billingState;
            recordsToSave.billingCountry=userGivenAddress.billingCountry;
            recordsToSave.billingPostal=userGivenAddress.billingPostal;
            recordsToSave.billingPhone=userGivenAddress.billingPhone;
            recordsToSave.billingMobile=userGivenAddress.billingMobile;
            recordsToSave.billingFax=userGivenAddress.billingFax;
            recordsToSave.billingEmail=userGivenAddress.billingEmail;
            recordsToSave.billingRecipientName=userGivenAddress.billingRecipientName;
            recordsToSave.billingContactPerson=userGivenAddress.billingContactPerson;                    
            recordsToSave.billingContactPersonNumber=userGivenAddress.billingContactPersonNumber;                    
            recordsToSave.billingContactPersonDesignation=userGivenAddress.billingContactPersonDesignation;                    
            recordsToSave.billingWebsite=userGivenAddress.billingWebsite;                    
            recordsToSave.shippingAddressType=userGivenAddress.shippingAddrsCombo;
            recordsToSave.shippingAddress=userGivenAddress.shippingAddress;
            recordsToSave.shippingCounty=userGivenAddress.shippingCounty;
            recordsToSave.shippingCity=userGivenAddress.shippingCity;
            recordsToSave.shippingState=userGivenAddress.shippingState;
            recordsToSave.shippingCountry=userGivenAddress.shippingCountry;
            recordsToSave.shippingPostal=userGivenAddress.shippingPostal;
            recordsToSave.shippingPhone=userGivenAddress.shippingPhone;
            recordsToSave.shippingMobile=userGivenAddress.shippingMobile;
            recordsToSave.shippingFax=userGivenAddress.shippingFax;
            recordsToSave.shippingEmail=userGivenAddress.shippingEmail;
            recordsToSave.shippingRecipientName=userGivenAddress.shippingRecipientName;
            recordsToSave.shippingContactPerson=userGivenAddress.shippingContactPerson;
            recordsToSave.shippingContactPersonNumber=userGivenAddress.shippingContactPersonNumber;             
            recordsToSave.shippingContactPersonDesignation=userGivenAddress.shippingContactPersonDesignation;             
            recordsToSave.shippingWebsite=userGivenAddress.shippingWebsite;             
            recordsToSave.shippingRoute=userGivenAddress.shippingRoute;
            if(!isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster){//Only For vendor Documents when addresses are fetched from vendor master only
               recordsToSave.vendcustShippingAddress=userGivenAddress.vendorShippingAddress;  
               recordsToSave.vendcustShippingCountry=userGivenAddress.vendorShippingCountry;  
               recordsToSave.vendcustShippingState=userGivenAddress.vendorShippingState;  
               recordsToSave.vendcustShippingCounty=userGivenAddress.vendorShippingCounty;  
               recordsToSave.vendcustShippingCity=userGivenAddress.vendorShippingCity;  
               recordsToSave.vendcustShippingEmail=userGivenAddress.vendorShippingEmail;  
               recordsToSave.vendcustShippingFax=userGivenAddress.vendorShippingFax;  
               recordsToSave.vendcustShippingMobile=userGivenAddress.vendorShippingMobile;  
               recordsToSave.vendcustShippingPhone=userGivenAddress.vendorShippingPhone;  
               recordsToSave.vendcustShippingPostal=userGivenAddress.vendorShippingPostal;  
               recordsToSave.vendcustShippingContactPersonNumber=userGivenAddress.vendorShippingContactNumber;  
               recordsToSave.vendcustShippingContactPersonDesignation=userGivenAddress.vendorShippingContactDesignation;  
               recordsToSave.vendcustShippingWebsite=userGivenAddress.vendorShippingWebsite;  
               recordsToSave.vendcustShippingContactPerson=userGivenAddress.vendorShippingContactPerson;  
               recordsToSave.vendcustShippingRecipientName=userGivenAddress.vendorShippingRecipientName;  
               recordsToSave.vendcustShippingAddressType=userGivenAddress.vendorShippingAddrsCombo; 
               /**
                 * If Show Vendor Address in Purchase document is off then Vendor billing address shown separate fieldset
                 * get This address value while save document 
                 */
                if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && !isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster) {
                    /*---Vendor Billing Address------ */
                    recordsToSave.vendorbillingAddressTypeForINDIA = userGivenAddress.vendorbillingAddressTypeForINDIA != undefined ? userGivenAddress.vendorbillingAddressTypeForINDIA : userGivenAddress.vendorbillingAddrsComboForINDIA;
                    recordsToSave.vendorbillingAddressForINDIA = userGivenAddress.vendorbillingAddressForINDIA;
                    recordsToSave.vendorbillingCountyForINDIA = userGivenAddress.vendorbillingCountyForINDIA;
                    recordsToSave.vendorbillingCityForINDIA = userGivenAddress.vendorbillingCityForINDIA;
                    recordsToSave.vendorbillingStateForINDIA = userGivenAddress.vendorbillingStateForINDIA;
                    recordsToSave.vendorbillingCountryForINDIA = userGivenAddress.vendorbillingCountryForINDIA;
                    recordsToSave.vendorbillingPostalForINDIA = userGivenAddress.vendorbillingPostalForINDIA;
                    recordsToSave.vendorbillingPhoneForINDIA = userGivenAddress.vendorbillingPhoneForINDIA;
                    recordsToSave.vendorbillingMobileForINDIA = userGivenAddress.vendorbillingMobileForINDIA;
                    recordsToSave.vendorbillingFaxForINDIA = userGivenAddress.vendorbillingFaxForINDIA;
                    recordsToSave.vendorbillingEmailForINDIA = userGivenAddress.vendorbillingEmailForINDIA;
                    recordsToSave.vendorbillingRecipientNameForINDIA = userGivenAddress.vendorbillingRecipientNameForINDIA;
                    recordsToSave.vendorbillingContactPersonForINDIA = userGivenAddress.vendorbillingContactPersonForINDIA;
                    recordsToSave.vendorbillingContactPersonNumberForINDIA = userGivenAddress.vendorbillingContactPersonNumberForINDIA;
                    recordsToSave.vendorbillingContactPersonDesignationForINDIA = userGivenAddress.vendorbillingContactPersonDesignationForINDIA;
                    recordsToSave.vendorbillingWebsiteForINDIA = userGivenAddress.vendorbillingWebsiteForINDIA;
                }
            }
            
            if(recordsToSave!=undefined && recordsToSave!='' && recordsToSave.isPOfromSO!=undefined &&recordsToSave.isPOfromSO){
             recordsToSave.customerShippingAddressType=userGivenAddress.customerShippingAddrsCombo;
             recordsToSave.customerShippingAddress=userGivenAddress.customerShippingAddress;
             recordsToSave.customerShippingCounty=userGivenAddress.customerShippingCounty;
             recordsToSave.customerShippingCity=userGivenAddress.customerShippingCity;
             recordsToSave.customerShippingState=userGivenAddress.customerShippingState;
             recordsToSave.customerShippingCountry=userGivenAddress.customerShippingCountry;
             recordsToSave.customerShippingPostal=userGivenAddress.customerShippingPostal;
             recordsToSave.customerShippingPhone=userGivenAddress.customerShippingPhone;
             recordsToSave.customerShippingMobile=userGivenAddress.customerShippingMobile;
             recordsToSave.customerShippingFax=userGivenAddress.customerShippingFax;
             recordsToSave.customerShippingEmail=userGivenAddress.customerShippingEmail;
             recordsToSave.customerShippingRecipientName=userGivenAddress.customerShippingRecipientName;
             recordsToSave.customerShippingContactPerson=userGivenAddress.customerShippingContactPerson;
             recordsToSave.customerShippingContactPersonNumber=userGivenAddress.customerShippingContactNumber;             
             recordsToSave.customerShippingContactPersonDesignation=userGivenAddress.customerShippingContactDesignation;             
             recordsToSave.customerShippingWebsite=userGivenAddress.customerShippingWebsite;             
             recordsToSave.customerShippingRoute=userGivenAddress.customerShippingRoute;
            }
        }
        return recordsToSave;
    },
    getAddressRecordsFromExistingRecord:function(recordsToSave,existingDocumentAddress,isCustomer){ //This method used for putting address details in save record, which are copied/edited/Linked From other Records.
        if(existingDocumentAddress!="" && existingDocumentAddress!=undefined){
            var documentAddress=existingDocumentAddress.data;
            recordsToSave.billingAddressType=documentAddress.billingAddressType;
            recordsToSave.billingAddress=documentAddress.billingAddress;
            recordsToSave.billingCounty=documentAddress.billingCounty;
            recordsToSave.billingCity=documentAddress.billingCity;
            recordsToSave.billingState=documentAddress.billingState;
            recordsToSave.billingCountry=documentAddress.billingCountry;
            recordsToSave.billingPostal=documentAddress.billingPostal;
            recordsToSave.billingPhone=documentAddress.billingPhone;
            recordsToSave.billingMobile=documentAddress.billingMobile;
            recordsToSave.billingFax=documentAddress.billingFax;
            recordsToSave.billingEmail=documentAddress.billingEmail;
            recordsToSave.billingRecipientName=documentAddress.billingRecipientName;
            recordsToSave.billingContactPerson=documentAddress.billingContactPerson;
            recordsToSave.billingContactPersonNumber=documentAddress.billingContactPersonNumber;
            recordsToSave.billingContactPersonDesignation=documentAddress.billingContactPersonDesignation;
            recordsToSave.billingWebsite=documentAddress.billingWebsite;   
            recordsToSave.shippingAddressType=documentAddress.shippingAddressType;
            recordsToSave.shippingAddress=documentAddress.shippingAddress;
            recordsToSave.shippingCounty=documentAddress.shippingCounty;
            recordsToSave.shippingCity=documentAddress.shippingCity;
            recordsToSave.shippingState=documentAddress.shippingState;
            recordsToSave.shippingCountry=documentAddress.shippingCountry;
            recordsToSave.shippingPostal=documentAddress.shippingPostal;
            recordsToSave.shippingPhone=documentAddress.shippingPhone;
            recordsToSave.shippingMobile=documentAddress.shippingMobile;
            recordsToSave.shippingFax=documentAddress.shippingFax;
            recordsToSave.shippingEmail=documentAddress.shippingEmail;
            recordsToSave.shippingRecipientName=documentAddress.shippingRecipientName;
            recordsToSave.shippingContactPerson=documentAddress.shippingContactPerson;
            recordsToSave.shippingContactPersonNumber=documentAddress.shippingContactPersonNumber;
            recordsToSave.shippingContactPersonDesignation=documentAddress.shippingContactPersonDesignation;
            recordsToSave.shippingWebsite=documentAddress.shippingWebsite;             
            recordsToSave.shippingRoute=documentAddress.shippingRoute; 
            if(!isCustomer){//Only For vendor Documents
                recordsToSave.vendcustShippingAddress=documentAddress.vendcustShippingAddress;  
                recordsToSave.vendcustShippingCountry=documentAddress.vendcustShippingCountry;  
                recordsToSave.vendcustShippingState=documentAddress.vendcustShippingState;  
                recordsToSave.vendcustShippingCounty=documentAddress.vendcustShippingCounty;  
                recordsToSave.vendcustShippingCity=documentAddress.vendcustShippingCity;  
                recordsToSave.vendcustShippingEmail=documentAddress.vendcustShippingEmail;  
                recordsToSave.vendcustShippingFax=documentAddress.vendcustShippingFax;  
                recordsToSave.vendcustShippingMobile=documentAddress.vendcustShippingMobile;  
                recordsToSave.vendcustShippingPhone=documentAddress.vendcustShippingPhone;  
                recordsToSave.vendcustShippingPostal=documentAddress.vendcustShippingPostal;  
                recordsToSave.vendcustShippingContactPersonNumber=documentAddress.vendcustShippingContactPersonNumber;  
                recordsToSave.vendcustShippingContactPersonDesignation=documentAddress.vendcustShippingContactPersonDesignation;  
                recordsToSave.vendcustShippingWebsite=documentAddress.vendcustShippingWebsite;  
                recordsToSave.vendcustShippingContactPerson=documentAddress.vendcustShippingContactPerson;  
                recordsToSave.vendcustShippingRecipientName=documentAddress.vendcustShippingRecipientName;  
                recordsToSave.vendcustShippingAddressType=documentAddress.vendcustShippingAddressType;
                
           if(recordsToSave!=undefined && recordsToSave!='' && recordsToSave.isPOfromSO!=undefined &&recordsToSave.isPOfromSO){
                recordsToSave.customerShippingAddressType=documentAddress.customerShippingAddressType;
                recordsToSave.customerShippingAddress=documentAddress.customerShippingAddress;
                recordsToSave.customerShippingCounty=documentAddress.customerShippingCounty;
                recordsToSave.customerShippingCity=documentAddress.customerShippingCity;
                recordsToSave.customerShippingState=documentAddress.customerShippingState;
                recordsToSave.customerShippingCountry=documentAddress.customerShippingCountry;
                recordsToSave.customerShippingPostal=documentAddress.customerShippingPostal;
                recordsToSave.customerShippingPhone=documentAddress.customerShippingPhone;
                recordsToSave.customerShippingMobile=documentAddress.customerShippingMobile;
                recordsToSave.customerShippingFax=documentAddress.customerShippingFax;
                recordsToSave.customerShippingEmail=documentAddress.customerShippingEmail;
                recordsToSave.customerShippingRecipientName=documentAddress.customerShippingRecipientName;
                recordsToSave.customerShippingContactPerson=documentAddress.customerShippingContactPerson;
                recordsToSave.customerShippingContactPersonNumber=documentAddress.customerShippingContactPersonNumber;             
                recordsToSave.customerShippingContactPersonDesignation=documentAddress.customerShippingContactPersonDesignation;             
                recordsToSave.customerShippingWebsite=documentAddress.customerShippingWebsite;             
                recordsToSave.customerShippingRoute=documentAddress.customerShippingRoute;
            }
         } 
           /**
             * If Show Vendor Address in Purchase document is off then Vendor billing address shown separate fieldset
             * get This address value while save document 
             */
          if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && !isCustomer && !Wtf.account.companyAccountPref.isAddressFromVendorMaster) {
                /*---Vendor Billing Address------ */
                recordsToSave.vendorbillingAddressTypeForINDIA = documentAddress.vendorbillingAddressTypeForINDIA != undefined ? documentAddress.vendorbillingAddressTypeForINDIA : documentAddress.vendorbillingAddrsComboForINDIA;
                recordsToSave.vendorbillingAddressForINDIA = documentAddress.vendorbillingAddressForINDIA;
                recordsToSave.vendorbillingCountyForINDIA = documentAddress.vendorbillingCountyForINDIA;
                recordsToSave.vendorbillingCityForINDIA = documentAddress.vendorbillingCityForINDIA;
                recordsToSave.vendorbillingStateForINDIA = documentAddress.vendorbillingStateForINDIA;
                recordsToSave.vendorbillingCountryForINDIA = documentAddress.vendorbillingCountryForINDIA;
                recordsToSave.vendorbillingPostalForINDIA = documentAddress.vendorbillingPostalForINDIA;
                recordsToSave.vendorbillingPhoneForINDIA = documentAddress.vendorbillingPhoneForINDIA;
                recordsToSave.vendorbillingMobileForINDIA = documentAddress.vendorbillingMobileForINDIA;
                recordsToSave.vendorbillingFaxForINDIA = documentAddress.vendorbillingFaxForINDIA;
                recordsToSave.vendorbillingEmailForINDIA = documentAddress.vendorbillingEmailForINDIA;
                recordsToSave.vendorbillingRecipientNameForINDIA = documentAddress.vendorbillingRecipientNameForINDIA;
                recordsToSave.vendorbillingContactPersonForINDIA = documentAddress.vendorbillingContactPersonForINDIA;
                recordsToSave.vendorbillingContactPersonNumberForINDIA = documentAddress.vendorbillingContactPersonNumberForINDIA;
                recordsToSave.vendorbillingContactPersonDesignationForINDIA = documentAddress.vendorbillingContactPersonDesignationForINDIA;
                recordsToSave.vendorbillingWebsiteForINDIA = documentAddress.vendorbillingWebsiteForINDIA;
            }
        }
        return recordsToSave;
    },
    
    /*--Calculating addresses for sending java side as parameter for the purpose of saving(for dropship type doc)----  */
     getAddressRecordsForDropShipDocument: function(recordsToSave, existingDocumentAddress) { 
        var documentAddress = "";

        if (existingDocumentAddress != "" && existingDocumentAddress != undefined) {
            documentAddress = existingDocumentAddress;

            /*--Company Billing Address---- */
            recordsToSave.billingAddressType = documentAddress.billingAddressType != undefined ? documentAddress.billingAddressType : documentAddress.billingAddrsCombo;
            recordsToSave.billingAddress = documentAddress.billingAddress;
            recordsToSave.billingCounty = documentAddress.billingCounty;
            recordsToSave.billingCity = documentAddress.billingCity;
            recordsToSave.billingState = documentAddress.billingState;
            recordsToSave.billingCountry = documentAddress.billingCountry;
            recordsToSave.billingPostal = documentAddress.billingPostal;
            recordsToSave.billingPhone = documentAddress.billingPhone;
            recordsToSave.billingMobile = documentAddress.billingMobile;
            recordsToSave.billingFax = documentAddress.billingFax;
            recordsToSave.billingEmail = documentAddress.billingEmail;
            recordsToSave.billingRecipientName = documentAddress.billingRecipientName;
            recordsToSave.billingContactPerson = documentAddress.billingContactPerson;
            recordsToSave.billingContactPersonNumber = documentAddress.billingContactPersonNumber;
            recordsToSave.billingContactPersonDesignation = documentAddress.billingContactPersonDesignation;
            recordsToSave.billingWebsite = documentAddress.billingWebsite;

            /*---Vendor Billing Address------ */
            recordsToSave.dropshipbillingAddressType = documentAddress.dropshipbillingAddressType != undefined ? documentAddress.dropshipbillingAddressType : documentAddress.dropshipbillingAddrsCombo;
            recordsToSave.dropshipbillingAddress = documentAddress.dropshipbillingAddress;
            recordsToSave.dropshipbillingCounty = documentAddress.dropshipbillingCounty;
            recordsToSave.dropshipbillingCity = documentAddress.dropshipbillingCity;
            recordsToSave.dropshipbillingState = documentAddress.dropshipbillingState;
            recordsToSave.dropshipbillingCountry = documentAddress.dropshipbillingCountry;
            recordsToSave.dropshipbillingPostal = documentAddress.dropshipbillingPostal;
            recordsToSave.dropshipbillingPhone = documentAddress.dropshipbillingPhone;
            recordsToSave.dropshipbillingMobile = documentAddress.dropshipbillingMobile;
            recordsToSave.dropshipbillingFax = documentAddress.dropshipbillingFax;
            recordsToSave.dropshipbillingEmail = documentAddress.dropshipbillingEmail;
            recordsToSave.dropshipbillingRecipientName = documentAddress.dropshipbillingRecipientName;
            recordsToSave.dropshipbillingContactPerson = documentAddress.dropshipbillingContactPerson;
            recordsToSave.dropshipbillingContactPersonNumber = documentAddress.dropshipbillingContactPersonNumber;
            recordsToSave.dropshipbillingContactPersonDesignation = documentAddress.dropshipbillingContactPersonDesignation;
            recordsToSave.dropshipbillingWebsite = documentAddress.dropshipbillingWebsite;
            /**
             * Vendor Billing address if drop ship activated 
             */
            if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && !Wtf.account.companyAccountPref.isAddressFromVendorMaster) {
                recordsToSave.vendorbillingAddressTypeForINDIA = documentAddress.dropshipbillingAddressType != undefined ? documentAddress.dropshipbillingAddressType : documentAddress.dropshipbillingAddrsCombo;
                recordsToSave.vendorbillingAddressForINDIA = documentAddress.dropshipbillingAddress;
                recordsToSave.vendorbillingCountyForINDIA = documentAddress.dropshipbillingCounty;
                recordsToSave.vendorbillingCityForINDIA = documentAddress.dropshipbillingCity;
                recordsToSave.vendorbillingStateForINDIA = documentAddress.dropshipbillingState;
                recordsToSave.vendorbillingCountryForINDIA = documentAddress.dropshipbillingCountry;
                recordsToSave.vendorbillingPostalForINDIA = documentAddress.dropshipbillingPostal;
                recordsToSave.vendorbillingPhoneForINDIA = documentAddress.dropshipbillingPhone;
                recordsToSave.vendorbillingMobileForINDIA = documentAddress.dropshipbillingMobile;
                recordsToSave.vendorbillingFaxForINDIA = documentAddress.dropshipbillingFax;
                recordsToSave.vendorbillingEmailForINDIA = documentAddress.dropshipbillingEmail;
                recordsToSave.vendorbillingRecipientNameForINDIA = documentAddress.dropshipbillingRecipientName;
                recordsToSave.vendorbillingContactPersonForINDIA = documentAddress.dropshipbillingContactPerson;
                recordsToSave.vendorbillingContactPersonNumberForINDIA = documentAddress.dropshipbillingContactPersonNumber;
                recordsToSave.vendorbillingContactPersonDesignationForINDIA = documentAddress.dropshipbillingContactPersonDesignation;
                recordsToSave.vendorbillingWebsiteForINDIA = documentAddress.dropshipbillingWebsite;
            }


            /*---Customer Shipping address-------  */
            recordsToSave.customerShippingAddressType = documentAddress.customerShippingAddressType != undefined ? documentAddress.customerShippingAddressType : documentAddress.customerShippingAddrsCombo;
            recordsToSave.customerShippingAddress = documentAddress.customerShippingAddress;
            recordsToSave.customerShippingCounty = documentAddress.customerShippingCounty;
            recordsToSave.customerShippingCity = documentAddress.customerShippingCity;
            recordsToSave.customerShippingState = documentAddress.customerShippingState;
            recordsToSave.customerShippingCountry = documentAddress.customerShippingCountry;
            recordsToSave.customerShippingPostal = documentAddress.customerShippingPostal;
            recordsToSave.customerShippingPhone = documentAddress.customerShippingPhone;
            recordsToSave.customerShippingMobile = documentAddress.customerShippingMobile;
            recordsToSave.customerShippingFax = documentAddress.customerShippingFax;
            recordsToSave.customerShippingEmail = documentAddress.customerShippingEmail;
            recordsToSave.customerShippingRecipientName = documentAddress.customerShippingRecipientName;
            recordsToSave.customerShippingContactPerson = documentAddress.customerShippingContactPerson;
            recordsToSave.customerShippingContactPersonNumber = documentAddress.customerShippingContactPersonNumber!=undefined ? documentAddress.customerShippingContactPersonNumber : documentAddress.customerShippingContactNumber;
            recordsToSave.customerShippingContactPersonDesignation = documentAddress.customerShippingContactPersonDesignation!=undefined ? documentAddress.customerShippingContactPersonDesignation : documentAddress.customerShippingContactDesignation;
            recordsToSave.customerShippingWebsite = documentAddress.customerShippingWebsite;
            recordsToSave.customerShippingRoute = documentAddress.customerShippingRoute;
            //Vendor Shipping address
            if ((Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA  || Wtf.account.companyAccountPref.countryid == Wtf.Country.US) && Wtf.account.companyAccountPref.isAddressFromVendorMaster) {//Only For vendor Documents when addresses are fetched from vendor master only
                recordsToSave.shippingAddressType = documentAddress.shippingAddressType;
                recordsToSave.shippingAddress = documentAddress.shippingAddress;
                recordsToSave.shippingCounty = documentAddress.shippingCounty;
                recordsToSave.shippingCity = documentAddress.shippingCity;
                recordsToSave.shippingState = documentAddress.shippingState;
                recordsToSave.shippingCountry = documentAddress.shippingCountry;
                recordsToSave.shippingPostal = documentAddress.shippingPostal;
                recordsToSave.shippingPhone = documentAddress.shippingPhone;
                recordsToSave.shippingMobile = documentAddress.shippingMobile;
                recordsToSave.shippingFax = documentAddress.shippingFax;
                recordsToSave.shippingEmail = documentAddress.shippingEmail;
                recordsToSave.shippingRecipientName = documentAddress.shippingRecipientName;
                recordsToSave.shippingContactPerson = documentAddress.shippingContactPerson;
                recordsToSave.shippingContactPersonNumber = documentAddress.shippingContactPersonNumber;
                recordsToSave.shippingContactPersonDesignation = documentAddress.shippingContactPersonDesignation;
                recordsToSave.shippingWebsite = documentAddress.shippingWebsite;
            }
        }


        return recordsToSave;
    },  
    
    multipleDOLinkRenderer: function (v, m, rec) { // handle multiple links for in single row
        var b = "";
        var a = v.split(",");
        var ids = rec.data.doId;
        var id = ids.split(",");
        for (var i = 0; i < a.length; i++) {
            b += "<a style='padding-bottom: 2px;' class='jumplink' doId=" + id[i] + " href='#'>" + a[i] + "</a>";
        }
        return b;
    },
    getGridConfig: function (grid, moduleid, isdocumentEntryForm, isFireEvent, isInitialLoad) {
        var sysPrefConfig = WtfGlobal.getSysGridPreferences(moduleid);
        Wtf.Ajax.requestEx({
            url: "ACCCommon/getGridConfig.do",
            params:{
                moduleid : moduleid,
                isdocumentEntryForm:isdocumentEntryForm
            }
        }, this, function(res) {
            var data = res.data[0];
            if(data.cid!="" && data.cid!=undefined && data.cid!=null){
                var header = res.Header;
                grid.gridConfigId = data.cid;
                setMyConfig(grid, data, false, true, false, header, sysPrefConfig, isInitialLoad, isdocumentEntryForm);
                /**
                 * To replace previously saved grid config with new one. This should happen only once.
                 * This code will be removed when all old grid config updated with new one.
                 */
//                if (!data.isnewconfigsaved) {
//                    WtfGlobal.saveGridStateHandler(undefined, grid, grid.getState(), moduleid, data.cid, isdocumentEntryForm, true);
//                }
                if(isFireEvent){
                    grid.fireEvent('gridconfigloaded',grid,this);
                }
            } else if(isFireEvent){
                grid.fireEvent('gridconfigloaded',grid,this);    
            }
            
            if (data.cid == "" || data.cid == undefined || data.cid == null) {
                WtfGlobal.applySysPrefConfig(grid, isdocumentEntryForm, sysPrefConfig);
            }
        },
        function(res) {
        });
    },
    
    saveGridStateHandler: function (scope, grid, state, moduleid, configid, isdocumentEntryForm) {
        var sysPrefConfig = WtfGlobal.getSysGridPreferences(moduleid).gridPref;
        for(var count=0;count<state.columns.length;count++){//this loop for repalcing id of each state column with it dataindex
            var columnState=state.columns[count];
            var columnRecord=grid.getColumnModel().getColumnById(columnState.id);
            
            /*there are some column in grid for which we have no dataindex such as cheker,action buttons, Row Number etc. 
             *In such cases what ever id came remain as it is
             */
            if(columnRecord.dataIndex!="" && columnRecord.dataIndex!=undefined){
                 state.columns[count].id=columnRecord.dataIndex;
            }
            
            if (sysPrefConfig) {
                for (var j = 0; j < sysPrefConfig.length; j++) {
                    if (columnState.id == sysPrefConfig[j].fieldId && (sysPrefConfig[j].isReportField || (isdocumentEntryForm && sysPrefConfig[j].isFormField && sysPrefConfig[j].isHidden))) {
                        columnState.hidden = false;
                    }
                }
            }
        }
        
        if(scope && scope.updateGridState) {
            state = scope.updateGridState(state);
        }
        
        Wtf.Ajax.requestEx({
            url: "ACCCommon/saveGridConfig.do",
            params:{
                cid : configid,
                moduleid : moduleid,
                state : Wtf.encode(state),
                isdocumentEntryForm:isdocumentEntryForm
//                isnewconfigsaved: isnewconfigsaved
            }
        },
        this,
        function(res) {
            var data;
            if(res.data!=undefined){
                data = res.data[0];
                if (scope) {
                    scope.gridConfigId = data.cid;
                }
            }
        },
        function(res) {
        });
    },
    applySysPrefConfig: function (grid, isdocumentEntryForm, sysPrefConfig) {
        sysPrefConfig = sysPrefConfig.gridPref;
        if (grid && sysPrefConfig) {
            var cm = grid.colModel;
            for (var j = 0; j < sysPrefConfig.length; j++) {
                var index = cm.findColumnIndex(sysPrefConfig[j].fieldId);

                if (index != -1) {
                    if (sysPrefConfig[j].isReportField || (isdocumentEntryForm && sysPrefConfig[j].isFormField && sysPrefConfig[j].isHidden)) {
                        cm.config[index].hideable = false;
                        cm.setHidden(index, true);
                    }
                    if (isdocumentEntryForm && sysPrefConfig[j].isFormField && sysPrefConfig[j].isReadOnly) {
                        cm.setEditable(index, false);
                    }
                    if (sysPrefConfig[j].isFormField && sysPrefConfig[j].fieldLabelText) {
                        cm.setColumnHeader(index, sysPrefConfig[j].fieldLabelText);
                    }
                } else {
                    var column = cm.getColumnById(sysPrefConfig[j].fieldId);
                    if (column) {
                        if (sysPrefConfig[j].isReportField || (isdocumentEntryForm && sysPrefConfig[j].isFormField && sysPrefConfig[j].isHidden)) {
                            column.hideable = false;
                            column.hidden = true;
                        }
                        if (isdocumentEntryForm && sysPrefConfig[j].isFormField && sysPrefConfig[j].isReadOnly) {
                            column.editable = false;
                        }
                        if (sysPrefConfig[j].isFormField && sysPrefConfig[j].fieldLabelText) {
                            column.header = sysPrefConfig[j].fieldLabelText;
                        }
                    }
                }
            }

            grid.getView().updateAllColumnWidths();
            grid.getView().refresh(true);

        }
    },
    getSysGridPreferences: function (moduleid) {
        if (moduleid != undefined && typeof moduleid == "string") {
            moduleid = parseInt(moduleid.split("_")[0]);
        }
        var gridPref;
        switch (moduleid) {
            case Wtf.Acc_Invoice_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.customerInvoice;
                break;
            case Wtf.Acc_Vendor_Invoice_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.vendorInvoice;
                break;
            case Wtf.Acc_Cash_Sales_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.CS;
                break;
            case Wtf.Acc_Cash_Purchase_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.CP;
                break;
            case Wtf.Acc_Purchase_Order_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.purchaseOrder;
                break;
            case Wtf.Acc_Sales_Order_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.salesOrder;
                break;
            case Wtf.Acc_Vendor_Quotation_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.vendorQuotation;
                break;
            case Wtf.Acc_Customer_Quotation_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.customerQuotation;
                break;
            case Wtf.Acc_Purchase_Return_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.purchaseReturn;
                break;
            case Wtf.Acc_Sales_Return_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.salesReturn;
                break;
            case Wtf.Acc_Goods_Receipt_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.goodsReceipt;
                break;
            case Wtf.Acc_Delivery_Order_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.deliveryOrder;
                break;
            case Wtf.Acc_Product_Master_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.productForm;
                break;
            case Wtf.Acc_Stock_Request_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.stockRequest;
                break;
            case Wtf.Acc_Contract_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.salesContract;
                break;
            case Wtf.Acc_Lease_Contract:
                gridPref = Wtf.account.HideFormFieldProperty.leaseContract;
                break;
            case Wtf.Acc_Make_Payment_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.makePayment;
                break;
            case Wtf.Acc_Receive_Payment_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.receivePayment;
                break;
            case Wtf.Acc_Credit_Note_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.creditNote;
                break;
            case Wtf.Acc_Debit_Note_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.debitNote;
                break;
            case Wtf.Acc_Customer_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.customer;
                break;
            case Wtf.Acc_Vendor_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.vendor;
                break;
            case Wtf.Acc_Purchase_Requisition_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.purchaseRequisition;
                break;
            case Wtf.Acc_RFQ_ModuleId:
                gridPref = Wtf.account.HideFormFieldProperty.requestForQuotation;
                break;
            default:
                gridPref = undefined;
                break;
        }
        
        return new Object({
            gridPref: gridPref,
            moduleid: moduleid
        });
    },
    attachmentRenderer: function(){
        return "<div style='height:16px;width:16px;'><div class='pwndbar1 uploadDoc' style='cursor:pointer' wtf:qtitle='"
            + WtfGlobal.getLocaleText("acc.invoiceList.attachDocuments")
            + "' wtf:qtip='"+ WtfGlobal.getLocaleText("acc.invoiceList.clickToAttachDocuments")
            +"'>&nbsp;</div></div>"; 
    },
    remarkRenderer: function(v) {
        return "<div wtf:qtip='" + v + "'>" + v + "</div>";
    },
    quantityInRenderer: function(v, m, r) {
        if (r.get('type') == "OUT") {
            return "";
        } else {
            return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
    },
    quantityInsummaryRenderer: function(v) {
        return "<b> IN : " + parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "</b>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
    },
    quantityOUTRenderer: function(v, m, r) {
        if (r.get('type') == "OUT") {
            return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        } else {
            return "";
        }
    },
    quantityOUTsummaryRenderer: function(v) {
        return "<b>OUT : " + parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "</b>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
    },
    returnValRenderer: function(v, m, r) {
        return v
    },
    serialNoRenderer: function(val, m, r) {
        val = WtfGlobal.replaceAll(val, ",", ", ");
        var tipval = WtfGlobal.replaceAll(val, ",", "<br>");
        return "<div wtf:qtip='" + tipval + "'>" + val + "</div>"
    },
    returnsummaryRenderer: function(v) {
        return "<b>" + v + "</b>"//WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
    },
    reusableRenderer: function(val, m, r) {
        return "<div wtf:qtip='" + r.get('stockTypeId') + "'>" + val + "</div>"
    },
    ReusableCountRenderer: function(val, m, r) {
        val = WtfGlobal.replaceAll(val, ",", ", ");
        var tipval = WtfGlobal.replaceAll(val, ",", "<br>");
        return "<div wtf:qtip='" + tipval + "'>" + val + "</div>"
    },
    
    percentageRenderer: function(v,m,rec) {
        v = parseFloat(v).toFixed(2);
        v = v + "%";
        return '<div class="currency">' + v + '</div>';
    },
    weightRenderer : function(value){
        if(isNaN(parseFloat(value))){//when value is not a number
            return parseFloat(0).toFixed(3)+" "+WtfGlobal.getLocaleText("acc.field.weighingunit");
        } else {
            return parseFloat(value).toFixed(3)+" "+WtfGlobal.getLocaleText("acc.field.weighingunit");
        }
    },
    volumeRenderer : function(value){
        if(isNaN(parseFloat(value))){//when value is not a number
            return parseFloat(0).toFixed(3)+" Cubic";
        } else {
            return parseFloat(value).toFixed(3)+" Cubic";
        }
    },
    displayUoMRenderer : function (store, valueField, displayField, gridStore) {
        return function (value, meta, record) {
            if (value != "") {
                value = (parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            }
            
            var searchrecord = WtfGlobal.searchRecord(store, record.data.pid, valueField);
            if (searchrecord !== null && searchrecord.data[displayField] !== undefined) {
                return value + " " + searchrecord.data[displayField];
            } else {
                searchrecord = WtfGlobal.searchRecord(gridStore, record.data.productid, 'productid');
                if (searchrecord !== null) {
                    return value + " " + searchrecord.data.displayUoMName;
                } else {
                    return value;
                }
            }
        }
    },
    getReportMenu: function (toolbar, moduleid, modulename) {
        var report;
        var reportArr = [];
        Wtf.Ajax.requestEx({
            url: "CommonFunctions/getReports.do",
            params: {
                moduleid: moduleid
            }
        }, this, function (successResponse) {
    
            var reportsArr = successResponse.data;
            for (var i = 0; i < reportsArr.length; i++) {
                report = reportsArr[i];
                var temp = new Wtf.Action({
                    text: report.name,
                    tooltip: {
                        text: report.description
                    },
                    iconCls: "accountingbase reportsBtnIcon",
                    handler: function (e) {
                        eval(e.initialConfig.methodName);
                    },
                     initialConfig : {
                    methodName : report.methodName
                },
                    scope: this
                });
                reportArr.push(temp);
            }

            if (reportArr.length != 0) {
                var tbar=toolbar;
                var btnpos=toolbar.items.length;
                if (tbar != undefined) {
                    
//                    if (tbar.items.items[tbar.items.length - 1].iconCls == "helpButton")
//                        btnpos = tbar.items.length - 1;
//                    else {
                        tbar.add('->');
                        btnpos++;
//                    }
                    tbar.insertButton(btnpos - 2, new Wtf.Toolbar.Button({
                        iconCls: "accountingbase reportsBtnIcon",
                        tooltip: {
                            text: WtfGlobal.getLocaleText({
                                key: "acc.REPORTSBTN.ttip",
                                params: [modulename]
                            })
                        }, //"Click to view reports related to "+this.moduleName+"."},
                        scope: this,
                        text: WtfGlobal.getLocaleText("acc.REPORTSBTN.title"), //"Reports",

                        menu: reportArr
                    }));
//                    tbar.insertButton(tbar.items.length - 2, new Wtf.Toolbar.Separator());
                }
            }

        }, function (failureResponse) {


        });

    },
    getModuleName:function(moduleid){
        var moduleName='';
        switch(moduleid){
            case Wtf.Acc_Invoice_ModuleId:
                moduleName=' Sales Invoice ';
                break;
            case  Wtf.Acc_Customer_ModuleId:
                moduleName=' Customer ';
                break;
            case Wtf.Acc_Vendor_ModuleId:
                moduleName=' Vendor ';
                break;
            case Wtf.Acc_Product_Master_ModuleId:
                moduleName=' Product ';
                break;
            case Wtf.Account_Statement_ModuleId:
                moduleName=' Accounts ';
                break;
            case Wtf.Acc_Credit_Note_ModuleId:
                moduleName=' Credit Note ';
                break;
            case Wtf.Acc_Debit_Note_ModuleId:
                moduleName=' Debit Note ';
                break;
            case Wtf.Acc_Receive_Payment_ModuleId:
                moduleName=' Receipt ';
                break;   
            case Wtf.Acc_Make_Payment_ModuleId:
                moduleName=' Payment ';
                break;   
            case Wtf.Acc_Sales_Order_ModuleId:
                moduleName=' Sales Order ';
                break;   
            case Wtf.Acc_Purchase_Order_ModuleId:
                moduleName=' Purchase Order ';
                break;   
            case Wtf.Acc_Vendor_Invoice_ModuleId:
                moduleName=' Purchase Invoice ';
                break;   
            case Wtf.Acc_Delivery_Order_ModuleId:
                moduleName=' Delivery Order ';
                break;   
            default:
                moduleName='';
            break;
            
        }
       return moduleName;
    },
    
    //Global function to print multiple template
 callPrintMultipleTemplates:function(tempArray,invParentObj){
    var moduleid= invParentObj.moduleid;
    var filetype='print';
    var selRec=null;
    var mode=Wtf.autoNum.Invoice;
    var templateId='';
    var amount=0;
    var billid="";
    var billno="";
    var fileName="Invoice "+billno+"_v1";
    if(invParentObj.objForPrintTemp!==undefined && invParentObj.objForPrintTemp!=null) {
        amount=invParentObj.objForPrintTemp.amount;  
    }
    if (invParentObj!==undefined && invParentObj!==null) {  
        var url="ACCInvoiceCMN/exportSingleInvoice.do";
        for (var i = 0; i < tempArray.length; i++) {
            var obj =tempArray[i].data;
            if(obj.moduleid==Wtf.Acc_Invoice_ModuleId){
                if(invParentObj.objForPrintTemp!=undefined && invParentObj.objForPrintTemp!=null) {   
                    billno=invParentObj.objForPrintTemp.billno;
                    billid=invParentObj.objForPrintTemp.billid;
                } 
                url= "ACCInvoiceCMN/exportSingleInvoice.do";
                fileName="Invoice "+billno+"_v1";
                moduleid=Wtf.Acc_Invoice_ModuleId;
                mode = Wtf.autoNum.Invoice;
                templateId=obj.templateid;
                 
            }else if(invParentObj.isAutoCreateDO){
                if(invParentObj.objForPrintTemp!=undefined && invParentObj.objForPrintTemp!=null) {   
                    billno=invParentObj.objForPrintTemp.dono;
                    billid=invParentObj.objForPrintTemp.doid;
                }
                url = "ACCInvoiceCMN/exportSingleDeliveryOrder.do";
                fileName="Delivery Order "+billno+"_v1";
                moduleid=Wtf.Acc_Delivery_Order_ModuleId;
                mode=Wtf.autoNum.DeliveryOrder;
                templateId=obj.templateid;
            }
            fileName = encodeURIComponent(fileName);
            selRec = "&amount="+amount+"&bills="+billid+"&recordids=" + billid + "&isConsignment="+false+"&isLeaseFixedAsset="+false+"&isDraft="+invParentObj.isDraft;
            
            var params = "moduleid="+moduleid+"&mode=" + mode + "&rec=" + selRec +"&filename=" + fileName +"&templateid="+templateId+"&contraentryflag=" + false + "&filetype=" + filetype+"&templatesubtype="+obj.templatesubtype;
            WtfGlobal.callToPrintTemplate(url,params);
        }
    }
    
},
 /*
  
 * @param {type} url
 * @param {type} params
 * @returns {undefined}* Below method is used to Open the each template in separate tab
 */
 callToPrintTemplate :function(url, params){
        var mapForm = document.createElement("form");
        mapForm.target = "_blank";
        mapForm.method = "post"; 
        mapForm.action = url;
        var params = params;
        var inputs =params.split('&');
        for(var i=0;i<inputs.length;i++){
            var KV_pair = inputs[i].split('=');
            var mapInput = document.createElement("input");
            mapInput.type = "text";
            mapInput.name = KV_pair[0];
            mapInput.value = KV_pair[1];
            mapForm.appendChild(mapInput); 
        }
        document.body.appendChild(mapForm);
        mapForm.submit();
        mapForm.remove();
    },
    isTaxShouldBeEnable: function(date) {
        var isTaxShouldBeEnable=true;
        if (Wtf.account.companyAccountPref.gstEffectiveDate !== "" && Wtf.account.companyAccountPref.gstDeactivationDate !== "") {
            if ((new Date(Wtf.account.companyAccountPref.gstEffectiveDate).clearTime() <= date) && (date < new Date(Wtf.account.companyAccountPref.gstDeactivationDate).clearTime())) {
                isTaxShouldBeEnable = true;
            } else {
                isTaxShouldBeEnable = false;
            }            
        } else if (Wtf.account.companyAccountPref.gstEffectiveDate === "" && Wtf.account.companyAccountPref.gstDeactivationDate === "") {
            isTaxShouldBeEnable = false;
        } else if (Wtf.account.companyAccountPref.gstEffectiveDate === "" && Wtf.account.companyAccountPref.gstDeactivationDate !== "") {
            isTaxShouldBeEnable = false;
        }
        return isTaxShouldBeEnable;
    },
    
    /**
     * Description: To get next week day for given date
     * @param date Existing date on which next date is required.
     * @param weekDay Week day for which next date is requered. '0' ('Sunday') to '6' ('Saturday').
     * @param isNextDay 'true' - if required next day date of given date otherwise 'false'
     */
    getNextDateForWeekDay: function(date, weekDay, isNextDay) {
        var daysDiff = 0;
        
        if (isNextDay) { // For next day
            daysDiff = 1;
        } else if (weekDay >= 0 && weekDay <= 6) { // For other week days '0' ('Sunday') to '6' ('Saturday')
            var currentDay = date.getDay();
            daysDiff = weekDay - currentDay;
            if (currentDay >= weekDay) {
                daysDiff += 7;
            }
        }

        return date.add(Date.DAY, daysDiff);
    },
    
    getOneCurrencyToOther: function(amount, oldCurrencyExchangeRate, newCurrencyExchangeRate) {
        var currencyAmount = 0;
        if (amount != 0) {
            // For gettting amount in base currency
            if (oldCurrencyExchangeRate != 0) {
                currencyAmount = getRoundedAmountValue(amount / oldCurrencyExchangeRate);
            }

            // For gettting amount in new currency
            if (newCurrencyExchangeRate != 0) {
                currencyAmount = getRoundedAmountValue(currencyAmount * newCurrencyExchangeRate);
            }
        }
        return currencyAmount;
    },
    
    getDownloadFrame : function(downloadURL){
        Wtf.get('downloadframe').dom.src = downloadURL;
    },
    
    getColIndexByDataIndex: function (colModel, dataIndex) {
        var index = -1;
        for (var indexCount = 0; indexCount < colModel.getColumnCount(); indexCount++) {
            var tempDataIndex = colModel.getDataIndex(indexCount);
            if (tempDataIndex === dataIndex) {
                index = indexCount;
                break;
            }
        }
        return index;
    },
    convertStringToBoolean:function(value){
    if(typeof value=="string"){
         if(value=="true")
             return true;
         else
             return false;
     }else{
      return value;
     }
   },
   /**
    * Calculating discount and assigning discount on the basis of Applicable days and discount master.
    * If discount amount is greater than amount due of invoice then returning the invoice nos and displaying it to user.
    * Written global function as it is used in different commponents.
    * ERM-981
    * @param {type} scope
    * @returns {undefined}    
    */
   assignDiscountAfterCalculation: function (scope) {
        if (CompanyPreferenceChecks.discountOnPaymentTerms()) {
            var gridStore = scope.grid != undefined ? scope.grid.getStore() : "";
            var discount = 0.0;
            if (gridStore != undefined && gridStore != "") {
                var length = gridStore.data.items.length;
                for (var cnt = 0; cnt < length; cnt++) {
                    var amount = gridStore.data.items[cnt].data.amount;
                    var exchangeRateForTransaction = gridStore.data.items[cnt].data.exchangeratefortransaction;
                    var amountDueOriginal = gridStore.data.items[cnt].data.amountDueOriginal;
                    if (amount == amountDueOriginal) {
                        discount = scope.grid.calculateDiscount(gridStore.data.items[cnt].data,exchangeRateForTransaction);
                    }
                    
                    var amountDueOriginal = gridStore.data.items[cnt].data.amountDueOriginal;
                    var amountDueOriginalWithExchangeRate = getRoundedAmountValue(amountDueOriginal * exchangeRateForTransaction);
                    if (discount > amountDueOriginalWithExchangeRate) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.receiptpayment.greaterdiscounterrormessage")], 1);
                        gridStore.data.items[cnt].data.discountname = amountDueOriginalWithExchangeRate;
                        gridStore.data.items[cnt].data.amountdueafterdiscount = getRoundedAmountValue((gridStore.data.items[cnt].data.amount * exchangeRateForTransaction) - gridStore.data.items[cnt].data.discountname);
                    } else {
                        gridStore.data.items[cnt].data.discountname = discount;
                        if (discount == 0) {
                            gridStore.data.items[cnt].data.amountdueafterdiscount = getRoundedAmountValue(gridStore.data.items[cnt].data.amountdue);
                        } else {
                            gridStore.data.items[cnt].data.amountdueafterdiscount = getRoundedAmountValue(gridStore.data.items[cnt].data.amountdue - discount);
                        }
                    }
                    gridStore.data.items[cnt].data.enteramount = gridStore.data.items[cnt].data.amountdueafterdiscount;
                }
                scope.onGridDataChanged();   // Purpose of calling this function is only that updating new amount in 'Amount' field,Balance amount and south temp form after reloading grid
            }
            scope.grid.getView().refresh();
        }
    },
    
    hideShowCustomizeLineFields : function(scope, moduleid){
            Wtf.Ajax.requestEx({
                url: "ACCAccountCMN/getCustomizedReportFields.do",
                params: {
                    moduleid:moduleid,
                    reportId:1,
                    isFormField:true,
                    isLineField:true
                }
            }, scope, function(action, response){
                if(action.success && action.data!=undefined){
                    scope.customizeData=action.data;
                    var cm=scope.getColumnModel();
                    for(var i=0;i<action.data.length;i++){
                        for(var j=0;j<cm.config.length;j++){
                            if(cm.config[j].dataIndex==action.data[i].fieldDataIndex ){
                                cm.setHidden(j,action.data[i].hidecol);       
                                cm.setEditable(j,!action.data[i].isreadonlycol);
                                if( action.data[i].fieldlabeltext!=null && action.data[i].fieldlabeltext!=undefined && action.data[i].fieldlabeltext!=""){
                                    cm.setColumnHeader(j,action.data[i].fieldlabeltext);
                                }
                            }
                        }
                    }
                    scope.reconfigure( scope.store, cm);
                }
            });
        },
        checkForFutureDate: function (scope, selectedValuesArr) {
        var laterDateRecords = "";
        if (selectedValuesArr != "") {//If linked document id array is not empty
            var nameCombo = scope.Name.getValue();
            var currencyValue= scope.Currency.getValue();
            if(scope.Term != undefined){
                var termvalue= scope.Term.getValue();
            }
            for (var cnt = 0; cnt < selectedValuesArr.length; cnt++) {
                var record = WtfGlobal.searchRecord(scope.POStore, selectedValuesArr[cnt],scope.PO.valueField);
                //var record = scope.POStore.getAt(scope.POStore.find('billid', selectedValuesArr[cnt]));
                if (record != undefined && new Date(record.data["date"]).clearTime() > scope.billDate.getValue()) {//Compare date of linked document and corrent document
                    laterDateRecords += record.data["billno"] + ", ";//If date of linked document is greater than current document date then add in variable
                }
            }
        }

        if (laterDateRecords != "")//if variable value is not same as initialized value it means future dated documents are present in array
        {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.plnote") + WtfGlobal.getLocaleText("acc.field.documents") + "<b>" + laterDateRecords.substring(0, laterDateRecords.length - 2) + "</b> " + WtfGlobal.getLocaleText("acc.field.areFutureDatedDocuments")], 2);//show alert
            if (scope.isEdit && !scope.copyInv && (scope.fromPO.disabled || scope.moduleid == Wtf.Acc_RFQ_ModuleId)) {//For edit case where link is used, In RFQ it is not disabled
                scope.billDate.setValue(scope.record.data.date);
            } else {
                if (scope.moduleid == Wtf.Acc_RFQ_ModuleId) {//In RFQ store is not loaded yet so loading the store
                    scope.Grid.store.load();
                }
                scope.Grid.getStore().removeAll();//Remove all data from product's store 
                scope.Grid.addBlankRow();//Adding blank row
                scope.NorthForm.getForm().reset();
                scope.Name.setValue(nameCombo);//Set value of Name combo
                scope.Currency.setValue(currencyValue);//Set value of Name combo
                scope.tagsFieldset.resetCustomComponents();//To reset custom fields.
                if (termvalue != undefined) {//As term is not present in all form checking for undefined
                    scope.Term.setValue(termvalue);
                }
                if (!(scope.moduleid == Wtf.Acc_RFQ_ModuleId)) {//In RFQ PO is always enable
                    scope.fromLinkCombo.disable();
                    scope.PO.disable();
                }
                if (scope.copyInv) {//For copy case of transactions.
                    scope.billDate.setValue(scope.record.data.date);//In case of copy as north form is reset date field also reset to blank so have to set it
                }
            }
            return true;
        }
    },
    
    calculateTaxAmountUsingAdaptiveRoundingAlgo: function (scope, isExpenseInv) {
        /*
         * Please algorithm docs - http://support.autocountsoft.com/?p=1233
         */
        var gstPrevUnroundedTotalAmount = 0;//To hold previous unrounded gst amount.
        var gstPrevRoundedTotalAmount = 0;//To hold previous rounded gst amount.
        var count = 0;
        var includingGST = (scope.parentObj.includingGST) ? scope.parentObj.includingGST.getValue() : false;
        var moduleid=scope.parentObj.moduleid;
        scope.getStore().each(function (rec) {
            if (!Wtf.isEmpty(rec.data, false)) {
                var gstUnroundedAmount = 0;//Current unrounded gst amount.
                var gstRoundedAmount = 0;//Current rounded gst amount.

                var rate = 0;
                if (includingGST) {
                    rate = isExpenseInv ? rec.data.rateIncludingGstEx : rec.data.rateIncludingGst;
                } else {
                    rate = rec.data.rate;
                }
                var origionalAmount = rate;
                var quantity = 0;
                if (!isExpenseInv) {
                    /*
                     * Consider quantity & partamount only in inventory case.
                     */
                    quantity = (Wtf.isEmpty(rec.data.quantity, false) && isNaN(rec.data.quantity)) ? 0 : rec.data.quantity;
                    //In DO & GRN quantity should be calculated on the basis of dquantity
                    if(moduleid==Wtf.Acc_Delivery_Order_ModuleId || moduleid==Wtf.Acc_Goods_Receipt_ModuleId || moduleid == Wtf.Acc_Sales_Return_ModuleId || moduleid == Wtf.Acc_Purchase_Return_ModuleId){
                        quantity = (Wtf.isEmpty(rec.data.dquantity, false) && isNaN(rec.data.dquantity)) ? rec.data.quantity : rec.data.dquantity;
                    }
                    origionalAmount = rate * quantity;

                    if (!Wtf.isEmpty(rec.data.partamount, false) && rec.data.partamount != 0) {
                        var partamount = getRoundedAmountValue(rec.data.partamount);
                        origionalAmount = origionalAmount * (partamount / 100);
                    }
                }

                var discount = 0;
                if (rec.data.prdiscount > 0) {
                    var prdiscount = getRoundedAmountValue(rec.data.prdiscount);
                    if (rec.data.discountispercent == 1) {
                        discount = getRoundedAmountValue(origionalAmount * prdiscount / 100);
                    } else {
                        discount = prdiscount;
                    }
                }

                var val = origionalAmount - discount;
                var taxpercent = 0;
                var index = this.taxStore.find('prtaxid', rec.data.prtaxid);
                if (index >= 0) {
                    var taxrec = this.taxStore.getAt(index);
                    taxpercent = getRoundedAmountValue(taxrec.data.percent);
                }

                if (includingGST) {
                    gstUnroundedAmount = (val * taxpercent) / (taxpercent + 100);
                } else if (!Wtf.isEmpty(rec.data.isUserModifiedTaxAmount, false) && rec.data.isUserModifiedTaxAmount) {
                    /*
                     * If user has changed the tax amount manually then manually entered tax amount considered in calculation.
                     */
                    gstUnroundedAmount = rec.data.taxamount;
                } else {
                    gstUnroundedAmount = val * taxpercent / 100;
                }

                gstPrevUnroundedTotalAmount += gstUnroundedAmount;
                if (count == 0) {
                    gstRoundedAmount = getRoundedAmountValue(gstUnroundedAmount);
                } else {
                    gstRoundedAmount = getRoundedAmountValue(gstPrevUnroundedTotalAmount) - gstPrevRoundedTotalAmount;
                }
                gstPrevRoundedTotalAmount += gstRoundedAmount;
                rec.set('taxamount', gstRoundedAmount);
                count++;
            }
        }, scope);
    },

    isNonZeroRatedTaxCodeUsedInTransaction: function (scope) {
        var isNonZeroRatedTaxCodeUsedInTransaction = false;
        if (scope.billDate != undefined && scope.billDate.getValue() != undefined && (new Date(scope.billDate.getValue()) >= new Date(Wtf.ZeroRatedTaxAppliedDateForMalasia))) {
            if (scope.includeProTax && scope.includeProTax.getValue()) {
                scope.Grid.getStore().each(function (rec) {
                    if (!Wtf.isEmpty(rec.data, false) && !Wtf.isEmpty(rec.data.prtaxid, false)) {
                        var taxrec = WtfGlobal.searchRecord(scope.Grid.taxStore, rec.data.prtaxid, 'prtaxid');
                        if (taxrec && taxrec.data && taxrec.data.percent > 0) {
                            isNonZeroRatedTaxCodeUsedInTransaction = true;
                            return;
                        }
                    }
                }, scope);
            } else if (scope.isTaxable && scope.isTaxable.getValue()) {
                var taxrec = WtfGlobal.searchRecord(scope.Grid.taxStore, scope.Tax.getValue(), 'prtaxid');
                if (taxrec && taxrec.data && taxrec.data.percent > 0) {
                    isNonZeroRatedTaxCodeUsedInTransaction = true;
                }
            }
        }
        return isNonZeroRatedTaxCodeUsedInTransaction;
    },
    
    getModuleidForExportBtn: function (obj) {
        var regex = new RegExp('^moduleid$', 'i');
        for (var key in obj) {
            if (key.match(regex)) {
                if (obj[key] != null || obj[key] != undefined || obj[key] != "") {
                    return obj[key];
                }
            }
        }
        return undefined;
    }
};


/*  WtfHTMLEditor: Start    */
Wtf.newHTMLEditor = function(config){
    Wtf.apply(this, config);
    this.createLinkText = 'Please enter the URL for the link:';
    this.defaultLinkValue = 'http:/'+'/';
    this.smileyel = null;
    this.SmileyArray = [" ", ":)", ":(", ";)", ":D", ";;)", ">:D<", ":-/", ":x", ":>>", ":P", ":-*", "=((", ":-O", "X(", ":>", "B-)", ":-S", "#:-S", ">:)", ":((", ":))", ":|", "/:)", "=))", "O:-)", ":-B", "=;", ":-c", ":)]", "~X("];
    this.tpl = new Wtf.Template('<div id="{curid}smiley{count}" style="float:left; height:20px; width:20px; background: #ffffff;padding-left:4px;padding-top:4px;"  ><img id="{curid}smiley{count}" src="{url}" style="height:16px; width:16px"></img></div>');
    this.tbutton = new Wtf.Toolbar.Button({
        minWidth: 30,
        disabled:true,
        enableToggle: true,
        iconCls: 'smiley'
    });
    this.eventSetFlag=false;
    this.tbutton.on("click", this.handleSmiley, this);
    this.smileyWindow = new Wtf.Window({
        width: 185,
        height: 116,
        minWidth: 200,
        plain: true,
        cls: 'replyWind',
        shadow: false,
        buttonAlign: 'center',
        draggable: false,
        header: false,
        closable  : true,
        closeAction : 'hide',
        resizable: false
    });
    this.smileyWindow.on("deactivate", this.closeSmileyWindow, this);
    Wtf.newHTMLEditor.superclass.constructor.call(this, {});
    this.on("render", this.addSmiley, this);
    this.on("activate", this.enableSmiley, this);
    this.on("hide", this.hideSmiley, this);
}

Wtf.extend(Wtf.newHTMLEditor, Wtf.form.HtmlEditor, {
    enableSmiley:function(){
        this.tbutton.enable();
    },
    hideSmiley: function(){
        if(this.smileyWindow !== undefined && this.smileyWindow.el !== undefined)
            this.smileyWindow.hide();
    },
    addSmiley: function(editorObj){
        editorObj.getToolbar().addSeparator();
        editorObj.getToolbar().addButton(this.tbutton);

    },
    createLink : function(){
        var url = prompt(this.createLinkText, this.defaultLinkValue);
        if(url && url != 'http:/'+'/'){
            var tmpStr = url.substring(0,7);
            if(tmpStr!='http:/'+'/')
                url = 'http:/'+'/'+url;
            this.win.focus();
            var selTxt = this.doc.getSelection().trim();
            selTxt = selTxt =="" ? url : selTxt;
            if(this.SmileyArray.join().indexOf(selTxt)==-1) {
                this.insertAtCursor("<a href = '"+url+"' target='_blank'>"+selTxt+" </a>");
                this.deferFocus();
            } else {
                msgBoxShow(170,1);
            }
        }
    },
    //  FIXME: ravi: When certain smilies are used in a pattern, the resultant from this function does not conform to regex used to decode smilies in messenger.js.

    writeSmiley: function(e){
        var obj=e;
        this.insertAtCursor(this.SmileyArray[obj.target.id.substring(this.id.length + 6)]+" ");
        this.smileyWindow.hide();
        this.tbutton.toggle(false);
    },

    handleSmiley: function(buttonObj, e){
        if(this.tbutton.pressed) {
            this.smileyWindow.setPosition(e.getPageX(), e.getPageY());
            this.smileyWindow.show();
            if(!this.eventSetFlag){
                for (var i = 1; i < 29; i++) {
                    var divObj = {
                        url: '../../images/smiley' + i + '.gif',
                        count: i,
                        curid: this.id
                    };
                    this.tpl.append(this.smileyWindow.body, divObj);
                    this.smileyel = Wtf.get(this.id + "smiley" + i);
                    this.smileyel.on("click", this.writeSmiley, this);
                    this.eventSetFlag=true;
                }
            }
        } else {
            this.smileyWindow.hide();
            this.tbutton.toggle(false);
        }
    },

    closeSmileyWindow: function(smileyWindow){
        this.smileyWindow.hide();
        this.tbutton.toggle(false);
    }
});

Wtf.linkPanel = function(conf){
    Wtf.apply(this, conf);
    Wtf.linkPanel.superclass.constructor.call(this, conf);
}

Wtf.extend(Wtf.linkPanel, Wtf.Panel, {
    border: false,
    baseCls: 'linkPanelBorder',
    initComponent: function(config){
        Wtf.linkPanel.superclass.initComponent.call(this, config);
        this.addEvents({
            "linkClicked": true
        });
    },
    onRender: function(conf){
        Wtf.linkPanel.superclass.onRender.call(this, conf);
        if(this.nameForDisplay) {
            this.filename = document.createElement("span");
            this.filename.style = "margin-left:4px !important;display:inline-block !important;";
            this.filename.style.textAlign = "float:left !important;";
            this.filename.id = "filename" + this.id;
            this.filename.innerHTML = this.nameForDisplay;
            this.add(this.filename);
        }
        
        this.link = document.createElement("a");
        this.link.id = "panelLink" + this.id;
        this.link.onclick = this.linkClick.createDelegate(this, [this.link]);
        this.link.style = "display:inline-block !important;"
        this.link.innerHTML = this.text;
        this.add(this.link);
    },

    afterRender: function() {
        this.doLayout();
    },

    linkClick: function(){
        this.fireEvent("linkClicked", this.link, this);
    },
    setLinkText: function(text){
        this.link.innerHTML = text;
    },
    hideLink: function(){
        this.link.style.display = 'none';
    },
    showLink: function(){
        this.link.style.display = 'block';
    }
});

// Call stack code
function showCallStack(){
var f=showCallStack,result="Call stack:\n";

while((f=f.caller)!==null){
var sFunctionName = f.toString().match(/^function (\w+)\(/)
sFunctionName = (sFunctionName) ? sFunctionName[1] : 'anonymous function';
result += sFunctionName;
result += getArguments(f.toString(), f.arguments);
result += "\n";

}
return result;
}


function getArguments(sFunction, a) {
var i = sFunction.indexOf(' ');
var ii = sFunction.indexOf('(');
var iii = sFunction.indexOf(')');
var aArgs = sFunction.substr(ii+1, iii-ii-1).split(',')
var sArgs = '';
for(var i=0; i<a.length; i++) {
var q = ('string' == typeof a[i]) ? '"' : '';
sArgs+=((i>0) ? ', ' : '')+(typeof a[i])+' '+aArgs[i]+':'+q+a[i]+q+'';
}
return '('+sArgs+')';
}


Wtf.taskDetail = Wtf.extend(Wtf.Component, {

	tplMarkup: ['<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                        '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" style="display:none;" id="helptipsID" onclick="goToNextCue();" href="javascript:;"><img src="../../images/help-tip.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" style="display:none;" id="closeID" onclick="closeHelp();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a>'+
                        '</div>'+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '</div>',
        // left - top
  		'<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                  '<a onkeypress="" onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p></p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><strong class="i"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1"  style="display:none;" id="closeID" onclick="closeHelp();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a></div>'+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '<div class="fcue-pnt fcue-pnt-lf-t">'+
            '</div>'+
        '</div>',
        // left - bottom
        '<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                 '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p></p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><strong class="i"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1"  style="display:none;" id="closeID" onclick="closeHelp();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a></div>'+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '<div class="fcue-pnt fcue-pnt-lf-b">'+
            '</div>'+
        '</div>',

        // 3 : bottom - left
        '<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                  '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p></p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><strong class="i"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" style="display:none;" id="closeID" onclick="closeHelp();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a></div>'+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '<div id="pointerdiv" class="fcue-pnt fcue-pnt-bm-l">'+
            '</div>'+
        '</div>',
        // bottom - right
        '<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                  '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p></p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><strong class="i"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" style="display:none;" id="closeID" onclick="closeHelp();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a></div>'+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '<div id="pointerdiv" class="fcue-pnt fcue-pnt-bm-r">'+
            '</div>'+
        '</div>',
        // top - left
        '<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                  '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p></p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" style="display:none;" id="closeID" onclick="closeHelp();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a></div>'+
                        ''+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '<div class="fcue-pnt fcue-pnt-t-l">'+
            '</div>'+
        '</div>',
        // top - right
        '<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                  '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p></p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1"  style="display:none;" id="closeID" onclick="closeHelp();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1"  id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a></div>'+
                        ''+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '<div class="fcue-pnt fcue-pnt-t-r">'+
            '</div>'+
        '</div>'],

	startingMarkup: 'Please select a module to see details',
        id : 'helpdialog',
        helpIndex : 0,
	initComponent: function(config) {
		Wtf.taskDetail.superclass.initComponent.call(this, config);
	},

	welcomeHelp: function(flag) {
        if(document.getElementById('fcue-360-mask'))document.getElementById('fcue-360-mask').style.display="block";
        var data = _helpContent[this.helpIndex];
        var compid = data.compid;
        if(flag==undefined)
            flag=1;
        if(compid=="") {
            var len=_helpContent.length;
            this.tpl = new Wtf.Template(this.tplMarkup[0]);
            var ht = this.tpl.append(document.body,{});
            document.getElementById('titlehelp').innerHTML = data.title;
            document.getElementById('titledesc').innerHTML = data.desc;
            Wtf.get('fcue-360').setXY([500,250]);
            document.getElementById('fcue-360').style.visibility ="visible";
            if(this.helpIndex == len-2){
                document.getElementById("nextID").style.display ="none";
                document.getElementById("previousID").style.display="none";
                document.getElementById("helptipsID").style.display="inline";
                document.getElementById("closeID").style.display="inline";
            } else if(this.helpIndex==0) {
                document.getElementById("nextID").style.visibility ="visible";
                document.getElementById("previousID").style.visibility ="hidden";
            } else if(this.helpIndex == len-1){
                document.getElementById("nextID").style.display ="none";
                document.getElementById("previousID").style.display="none";
                document.getElementById("helptipsID").style.display="none";
                document.getElementById("closeID").style.display="inline";
            } else {
                document.getElementById("nextID").style.visibility ="visible";
                document.getElementById("previousID").style.visibility ="visible";
            }
        } else
                this.nextPrevious(flag);
    },

    updateToNextDetail: function() {
        this.helpIndex = this.helpIndex+1;
        this.welcomeHelp(1);
    },

    updateToPrevDetail: function() {
        this.helpIndex = this.helpIndex-1;
        this.welcomeHelp(2);
	},

    blankDetail : function() {
        this.bltpl.overwrite(this.body,"");
	},

    getTemplateIndex : function(comppos) {
        var index = 0;
        var xPos = comppos[0];
        var yPos = comppos[1];
        var flag = 0;
        var myWidth = 0, myHeight = 0;
        if( typeof( window.innerWidth ) == 'number' ) {
            //Non-IE
            myWidth = window.innerWidth;
            myHeight = window.innerHeight;
        } else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
            //IE 6+ in 'standards compliant mode'
            myWidth = document.documentElement.clientWidth;
            myHeight = document.documentElement.clientHeight;
        } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
            //IE 4 compatible
            myWidth = document.body.clientWidth;
            myHeight = document.body.clientHeight;
        }

        if(xPos<20) { // extreme left
            flag = 1;
        } else if(xPos>(myWidth-370)) { // extreme right
            flag = 2;
        }
        if(yPos<100) {
            if(flag == 1) {
                index = 1; // left top corner
            } else if(flag == 2){
                index = 6; // top right corner
            } else {
                index = 5;
            }
        } else if(yPos>(myHeight-150)) {
            if(flag == 1) {// bottom left corner
                index = 3;
            } else if(flag == 2) {// bottom right corner
                index = 4;
            } else
                index = 3; // bottom left corner
        } else if(yPos<(myHeight/2)) {
            if(flag==1)
                index = 1;
            else if(flag==2)
                index = 6;
            else index = 5;
        } else if(yPos>(myHeight/2)) {
            if(flag==1)
                index = 2;
            else
                index = 3;
        }
        return index;
    },
    
    nextPrevious:function(flag){
        var len=_helpContent.length;
        var data = _helpContent[this.helpIndex];

        if(Wtf.get(data['compid'])==null) {
            if(flag==1)
                this.updateToNextDetail();
            else
                this.updateToPrevDetail();
            return;
        } else if(Wtf.get(data['compid']).getXY()[0]==0 && Wtf.get(data['compid']).getXY()[1]==0){
            if(flag==1) {
                if(this.helpIndex==0)
                    this.firstCollapse=1;
                this.updateToNextDetail();
            } else
                this.updateToPrevDetail();
            return;
        } else {
            if(flag==1 && this.helpIndex == len-1) { // For the last help in the page.
                this.firstCollapse=0;
            }
        }
        var comppos = Wtf.get(data['compid']).getXY();
        if(data['modeid'] == "1") {
            var dash = Wtf.getCmp('tabdashboard').body;
            dash.dom.scrollTop = comppos[1] - 70;
        } else if(mainPanel.activeTab.EditorGrid!=null && data['compid'].indexOf('addnew')!=-1) {  // crm modules
            var store = mainPanel.activeTab.EditorStore;
            var storeLen = store.getCount();
            var rowEl = mainPanel.activeTab.EditorGrid.getView().getRow(storeLen-1);
            var gBody = mainPanel.activeTab.EditorGrid.getView().scroller;
            var a = (Wtf.fly(rowEl).getOffsetsTo(gBody)[1]) + gBody.dom.scrollTop;
            gBody.dom.scrollTop = a;
        }
        comppos = Wtf.get(data['compid']).getXY();
        var index = this.getTemplateIndex(comppos);

        this.tpl = new Wtf.Template(this.tplMarkup[index]);
        var ht = this.tpl.append(document.body,{});
        document.getElementById('titlehelp').innerHTML = data.title;
        document.getElementById('titledesc').innerHTML = data.desc;
        var helpDiv = Wtf.get('fcue-360');
        var helpSize = helpDiv.getSize();
        if(data['compid']!=undefined && data['compid']=="gridmsg17PurchaseOrderListEntry"){ //ERP-10602 PO-Help Message
            comppos[0]=comppos[0]-3;
            comppos[1]=comppos[1]+17;
        }
        var pos = comppos;

        switch(index) {
            case 1: //left-top
                pos[1] -= 35;
                pos[0] += 60;
                break;
            case 2: //left-bottom
                break;
            case 3: //bottom-left
                pos[1] -= (helpSize.height);
                pos[0] -= 30
                if(pos[0]<0){
                    pos[0] = 12;
                    document.getElementById('pointerdiv').style.left = '-15px';
                }
                var topPos = helpSize.height-20;
                if(Wtf.isSafari) {
                    if(window.innerHeight-pos[1]<=180) {
                        topPos = helpSize.height-9;
                    }
                }
                document.getElementById('pointerdiv').style.top = (topPos+'px');// 22px - bottom div height
                break;
            case 4: //bottom-right
                pos[1] -= (helpSize.height);
                pos[0] -= (helpSize.width-22-32); // 22px - left div width and 32px - pointer position at inner side
                document.getElementById('pointerdiv').style.top = ((helpSize.height-20)+'px');// 22px - bottom div height
                break;
            case 5: //top - left
                pos[1] += 38;
                break;
            case 6: //top - right
                pos[1] += 35;
                pos[0] -= 310;
                break;
        }
        
        helpDiv.setXY(pos);
        document.getElementById('fcue-360').style.visibility ="visible";

        if(this.helpIndex == len-1) {
            if(this.firstCollapse == 0 && this.helpIndex != 0) {
                document.getElementById("nextID").style.display ="none";
                document.getElementById("previousID").style.cssFloat="left";
                document.getElementById("closeID").style.display="inline";
            } else {
                document.getElementById("nextID").style.display ="none";
                document.getElementById("previousID").style.display="none";
                document.getElementById("closeID").style.display="inline";
            }
        } else if(this.helpIndex == 0 || this.firstCollapse == 1 || this.prevCollapse == this.helpIndex) {
            document.getElementById("nextID").style.visibility ="visible";
            document.getElementById("previousID").style.visibility ="hidden";
            this.prevCollapse = this.helpIndex;
            this.firstCollapse=0;
        } else{
            document.getElementById("nextID").style.visibility ="visible";
            document.getElementById("previousID").style.visibility ="visible";
        }
    }

});

function isNumber(n) {
  return typeof o === 'number' && isFinite(o);
}

/*
 *  FinanceVumberField is a new component written for displaying amount in decimal format on UI side. 
 *  Wtf.form.NumberField does not support decimal formatting , so this component is written by extending the Wtf.Form.NumberField  component
 */
Wtf.form.FinanceNumberField = Wtf.extend(Wtf.form.NumberField, {
    setValue : function(v){
        var dp = Wtf.AMOUNT_DIGIT_AFTER_DECIMAL;
        if (dp < 0 || !this.allowDecimals) {
            dp = 0;
        }
        v = this.fixPrecision(v);
        v = isNumber(v) ? v : parseFloat(String(v).replace(this.decimalSeparator, "."));
        v = isNaN(v) ? '' : String(v.toFixed(dp)).replace(".", this.decimalSeparator);
        return Wtf.form.NumberField.superclass.setValue.call(this, v);
    },
    fixPrecision : function(value){
        var nan = isNaN(value);
        if(!this.allowDecimals || this.decimalPrecision == -1 || nan || !value) {
        return nan ? '' : value;
        }
        var val = parseFloat(value).toFixed(this.decimalPrecision);
        return val;
    },
    getValue: function() {
        return parseFloat(this.fixPrecision(this.parseValue(Wtf.form.NumberField.superclass.getValue.call(this))))
    }
});

function closeCue () {
    Wtf.get('fcue-360').remove();
    if(document.getElementById('fcue-360-mask'))document.getElementById('fcue-360-mask').style.display="none";
}

function closeHelp () {
    Wtf.get('fcue-360').remove();
   if(Wtf.get('dashhelp')!=undefined||Wtf.get('dashhelp')!=null){
           Wtf.get('dashhelp').remove();
       }
    if(document.getElementById('fcue-360-mask'))document.getElementById('fcue-360-mask').style.display="none";
}

function goToNextCue() {
    closeCue();
    Wtf.getCmp('helpdialog').updateToNextDetail();
}

function goToPrevCue() {
    closeCue();
    Wtf.getCmp('helpdialog').updateToPrevDetail();
}

function viewTransactionTemplate(type, formrec){
    var withoutInventory = Wtf.account.companyAccountPref.withoutinventory;
     var billid=formrec.data['billid'];
if(type == "Sales Invoice" ||type == "Cash Sale" ||type == "Cash Purchase" ||type == "Purchase Invoice"||type=="Customer Invoice" || type=="Vendor Invoice" )
 {
    this.GridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'companyid'},
        {name:'companyname'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'supplierinvoiceno'},//SDP-13085
        {name:'lasteditedby'},//SDP-13085
        {name:'orderamount'},
        {name:'isexpenseinv'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'date',type:'date'},
        {name:'duedate',type:'date'},
        {name:'shipdate',type:'date'},
        {name:'personname'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'partialinv',type:'boolean'},
        {name:'includeprotax',type:'boolean'},
        {name:'isapplytaxtoterms',type:'boolean'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'termdays'},
        {name:'termid'},
        {name:'termname'},
        {name:'incash'},
        {name:'taxamount'},
        {name:'taxid'},
        {name:'orderamountwithTax'},
        {name:'taxincluded',type:'boolean'},
        {name:'taxname'},
        {name:'deleted'},
        {name:'amountinbase'},
        {name:'memo'},
        {name:'createdby'},
        {name:'createdbyid'},
        {name:'externalcurrencyrate'},
        {name:'ispercentdiscount'},
        {name:'discountval'},
        {name:'crdraccid'},
        {name:'creditDays'},
        {name:'isRepeated'},
        {name:'porefno'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'NoOfpost'}, 
        {name:'NoOfRemainpost'},  
        {name:'templateid'},
        {name:'templatename'},
        {name:'startDate',type:'date'},
        {name:'nextDate',type:'date'},
        {name:'expireDate',type:'date'},
        {name:'repeateid'},
        {name:'status'},
        {name:'amountwithouttax'},
        {name:'amountwithouttaxinbase'},
        {name:'commission'},
        {name:'commissioninbase'},
        {name:'amountDueStatus'},
        {name:'salesPerson'},
        {name:'agent'},
        {name:'shipvia'},
        {name:'fob'},
        {name:'approvalstatus'},
        {name:'approvalstatusint', type:'int', defaultValue:-1},
        {name:'archieve', type:'int'},
        {name:'withoutinventory',type:'boolean'},
        {name:'isfavourite'},
        {name:'othervendoremails'},
        {name:'termdetails'},
        {name:'approvestatuslevel'},// for requisition
        {name:'posttext'},
        {name:'isOpeningBalanceTransaction'},
        {name:'isNormalTransaction'},
        {name:'isreval'},
        {name:'islockQuantityflag'},
        {name:'isprinted'},
        {name:'validdate',type:'date'},
        {name:'cashtransaction',type:'boolean'},
        {name:'landedInvoiceNumber'},
        {name:'termdays'},
        {name:'billingAddress'},
        {name:'billingCountry'},
        {name:'billingState'},
        {name:'billingPostal'},
        {name:'billingEmail'},
        {name:'billingFax'},
        {name:'billingMobile'},
        {name:'billingPhone'},
        {name:'billingContactPerson'},
        {name:'billingContactPersonNumber'},
        {name:'billingContactPersonDesignation'},
        {name:'billingCounty'},
        {name:'billingCity'},
        {name:'billingAddressType'},
        {name:'shippingAddress'},
        {name:'shippingCountry'},
        {name:'shippingState'},
        {name:'shippingCounty'},
        {name:'shippingCity'},
        {name:'shippingEmail'},
        {name:'shippingFax'},
        {name:'shippingMobile'},
        {name:'shippingPhone'},
        {name:'shippingPostal'},
        {name:'shippingContactPersonNumber'},
        {name:'shippingContactPersonDesignation'},
        {name:'shippingContactPerson'},
        {name:'shippingAddressType'},
        {name:'sequenceformatid'},
        {name:'isFixedAsset'},
        {name:'fixedAssetLeaseInvoice'},
        {name:'fixedAssetInvoice'},
        {name:'fixedAssetLeaseInvoice'},
        {name:'isConsignment',type:'boolean'},
        {name:'isLeaseFixedAsset',type:'boolean'},
        {name:'tdsrate'},
        {name:'tdsamount'},
        {name:'totalAmountWithTDS'},
        {name:'TotalAdvanceTDSAdjustmentAmt'}
    ]);
       
       
    this.StoreUrl = "";
    this.RemoteSort= false;
    if(type == "Sales Invoice" ||type == "Cash Sale" || type=="Customer Invoice"){
        this.StoreUrl = "ACCInvoiceCMN/getInvoicesMerged.do";
       this.RemoteSort = true;
    }else if(type == "Purchase Invoice" ||type == "Cash Purchase" || type=="Vendor Invoice"){
        this.StoreUrl = "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
      this.RemoteSort = true;
    }
        
    this.Store = new Wtf.data.Store({
        url:this.StoreUrl,
        baseParams:{
                billid: billid,
                costCenterId: this.costCenterId,
                deleted:false,
                nondeleted:false,
                cashonly:false,
                creditonly:false,
                CashAndInvoice:true,
                isConsignment : formrec!=undefined?formrec.data.isConsignment:false,  //consignment flag
                isFixedAsset:this.isFixedAsset,
//                isLeaseFixedAsset:this.isLeaseFixedAsset,
                isLeaseFixedAsset:formrec!=undefined?formrec.data.isLeaseFixedAsset:false,
                consolidateFlag:true,                
                isfavourite:false,
                isprinted:false
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });
    
    this.Store.load();
    var formrec1="";
    this.Store.on('load',function(){
        if(this.Store.getCount()>0){
            formrec1=this.Store.getAt(0);   
//                if(type == "Cash Sale" && withoutInventory) {
//                    callViewBillingCashReceipt(formrec,null, 'ViewBillingCSInvoice',true);
//                } else 
                if(type == "Cash Sale" && !withoutInventory) {
                    callViewCashReceipt(formrec1, 'ViewInvoice');
                } 
//                else if((type == "Sales Invoice" || type=="Customer Invoice") && withoutInventory) {
//                    callViewBillingInvoice(formrec,null, 'ViewBillingInvoice',false);
//                } 
                else if((type == "Sales Invoice" || type=="Customer Invoice") && !withoutInventory) {
                    if(formrec1.data.fixedAssetInvoice ||formrec1.data.fixedAssetLeaseInvoice){
                        callViewFixedAssetInvoice(formrec1, billid+'Invoice',false,undefined,false,formrec1.data.fixedAssetInvoice,formrec1.data.fixedAssetLeaseInvoice);
                    } else if(formrec1.data.isConsignment){
                        callViewConsignmentInvoice(true,formrec1,formrec1.data.billid+'ConsignmentInvoice',false,false,true);
                    } else{
                        callViewInvoice(formrec1, 'ViewCashReceipt');
                    }
                } 
//                else if(type == "Cash Purchase" && withoutInventory) {
//                    callViewBillingPaymentReceipt(formrec,null, 'ViewBillingCSInvoice',true);
//                } 
                else if(type == "Cash Purchase" && !withoutInventory) {
                    callViewPaymentReceipt(formrec1, 'ViewPaymentReceipt',formrec1.get("isexpenseinv"));
                } 
//                else if((type == "Purchase Invoice" || type =="Venor Invoice") && withoutInventory) {
//                    callViewBillingGoodsReceipt(formrec,null, 'ViewBillingInvoice',false);
//                } 
                else if((type == "Purchase Invoice" || type =="Vendor Invoice") && !withoutInventory){
                    if(formrec1.data.fixedAssetInvoice){
                        callViewFixedAssetGoodsReceipt(formrec1, billid+'GoodsReceipt',false,formrec1.data.isExpensiveInv,undefined,false,formrec1.data.fixedAssetInvoice);
                    } else if(formrec1.data.isConsignment){
                         callViewConsignmentGoodsReceipt(true,formrec1,formrec1.data.billid+'ConsignmentInvoice',false,false,true);
                    } else{
                        callViewGoodsReceipt(formrec1, 'ViewGoodsReceipt',formrec1.get("isexpenseinv"));
                    }
                }
    }
        
    },this);
}
else if(type == "Debit Note" ||type == "Credit Note" )
{    
        this.GridRec = Wtf.data.Record.create ([
        {name:"noteid"},
        {name:"noteno"},
        {name:"currencyid"},
        {name:'companyid'},
        {name:'companyname'},
        {name:'journalentryid'},        
        {name:'currencysymbol'},
        {name:'entryno'},
        {name:"personid"},
        {name:"personname"},
        {name:'amount'},
        {name:'amountdue'},
        {name:'costcenterid'},
        {name:'deleted'},
        {name:'costcenterName'},
        {name:"date",type:'date'},
        {name:'memo'},
        {name:'notetax'},
        {name:'noteSubTotal'},
        {name:'withoutinventory'},
        {name:'otherwise'},
        {name:'openflag'},
        {name:'isOpeningBalanceTransaction'},
        {name:'isNormalTransaction'},
        {name:'isOldRecord'},
        {name:'cntype'},
        {name:'isprinted'},
        {name:'partlyJeEntryWithCnDn'},
        {name:'sequenceformatid'},
        {name:'linkInvoices'},		
        {name: 'salesPersonID'},
        {name: 'externalcurrencyrate'},
        {name: 'billid'},
        {name: 'gTaxId'},
        {name: 'salesPerson'},
        {name: 'agent'},
        {name: 'agentid'},
        {name: 'includeprotax'},
        {name: 'lasteditedby'},
        {name: 'prtaxid'},
        {name: 'linkingdate'},
        {name:'externalcurrencyrate'}
    ]);
    this.StoreUrl = "";
   
    if(type == "Credit Note"){
        this.StoreUrl = "ACCCreditNote/getCreditNoteMerged.do";
    }else {
        this.StoreUrl = "ACCDebitNote/getDebitNoteMerged.do";
    }
   
        this.Store = new Wtf.data.GroupingStore({
            url:this.StoreUrl,
    //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            baseParams:{
                billid: billid,
                noteid: formrec.data['noteid'],
                mode:(type == "Credit Note")?(withoutInventory?62:27):(withoutInventory?62:28),
                costCenterId: this.costCenterId,
                deleted:"false",
                nondeleted:"false",
                viewMode:true
            },
            sortInfo : {
                field : 'companyname',
                direction : 'ASC'
            },
            groupField : 'companyname',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.GridRec)
        });
               
    this.Store.load();
    var formrec1="";
    this.Store.on('load',function(){
        if(this.Store.getCount()>0){
            formrec1=this.Store.getAt(0);   
              // check whether note is linked with payment or not        
            Wtf.Ajax.requestEx({
                url: (type == "Credit Note")?"ACCVendorPaymentCMN/getPaymentsLinkedWithNCreditNote.do":"ACCReceiptCMN/getPaymentsLinkedWithDebitNote.do",
                params:{
                    noteId:formrec1.get('noteid')
                }
            },this,function(response, request){               
                var noteType = formrec1.get('cntype');
                var isNoteLinkedWithPayment = response.isNoteLinkedWithPayment;
                
//                if (!isNoteLinkedWithPayment) { // remove restriction for CN DN whose payment made
//                    if (type == "Credit Note" && withoutInventory) {
//                        callViewBillingCreditNote(formrec, 'ViewcreditNote')
//                    }
                    if (type == "Credit Note") {
                        if (formrec1 != undefined && formrec1.data.cntype == '5') {
                            callViewCreditNoteGst(true, formrec1, false, false, true);
                        } else if (formrec1 != undefined && formrec1.data.cntype == Wtf.NoteForOvercharge) {
                            var winid = this.isCNReport ? 'creditnoteForOverchargeView' + formrec1.get("noteno") : 'debitnoteForOverchargeView' + formrec1.get("noteno");
                            callEditNoteForOvercharge(winid, formrec1, true, true, true, true);//cntype=6 - CN/DN for Overcharge
                        } else {
                            callViewCreditNote("ViewcreditNote" + formrec1.get("noteno"), true, true, formrec1.get('cntype'), formrec1, null);
                        }
                    } else if (type == "Debit Note") {
                        if (formrec1 != undefined && formrec1.data.cntype == '5') {
                            callViewCreditNoteGst(true, formrec1, false, false, false);
                        } else if (formrec1 != undefined && formrec1.data.cntype == Wtf.NoteForOvercharge) {
                            var winid = this.isCNReport ? 'creditnoteForOverchargeView' + formrec1.get("noteno") : 'debitnoteForOverchargeView' + formrec1.get("noteno");
                            callEditNoteForOvercharge(winid, formrec1, true, true, false, true);//cntype=6 - CN/DN for Overcharge
                        } else {
                            callViewDebitNote("ViewDebitNote" + formrec1.get("noteno"), true, false, formrec1.get('cntype'), formrec1, null);
                        }
                    }
        }
          ,function(response, request){                             
            }); 
    }
    },this);   
   }else if(type=="Payment Received"||type == "Payment Made"||type=="Dishonoured Receive Payment" || type=="Dishonoured Make Payment"){
        this.usersRec = new Wtf.data.Record.create([
            {name: 'billid'},
            {name:'companyid'},
            {name:'companyname'},
            {name: 'refid'},
            {name: 'personid'},
            {name: 'billno'},
            {name: 'refno'},
            {name: 'refname'},
            {name: 'refdetail'},
            {name:'personemail'},
            {name: 'detailtype'},
            {name: 'expirydate',type:'date'},
            {name: 'journalentryid'},
            {name: 'entryno'},
            {name: 'currencysymbol'},
            {name: 'externalcurrencyrate'},
            {name: 'personname'},
    //        {name: 'customervendorname'},
            {name: 'address'},
            {name: 'deleted'},
            {name: 'billdate',type:'date'},
            {name: 'paymentmethod'},
            {name: 'chequenumber'},
            {name: 'memo'},
            {name: 'amount'},
            {name: 'methodid'},
            {name: 'receiptamount'},
            {name: 'currencyid'},
            {name: 'detailsjarr'},
            {name: 'clearanceDate',type:'date'},
            {name: 'paymentStatus'},
            {name: 'otherwise'},
            {name: 'isOpeningBalanceTransaction'},
            {name: 'isNormalTransaction'},
            {name: 'receipttype'},
            {name: 'isadvancepayment'},
            {name: 'isadvancefromvendor'},
            {name: 'advanceUsed'},
            {name: 'advanceid'},
            {name: 'advanceamount'},
            {name: 'withoutinventory'},
            {name: 'refcardno'},
            {name: 'refexpdate'},
            {name: 'ismanydbcr'},
            {name:'dramount'},
            {name:'bankCharges'},
            {name:'bankChargesCmb'},
            {name:'bankInterest'},
            {name:'bankInterestCmb'},
            {name:'paidToCmb'},
            {name:'paidto'},
            {name:'isprinted'},
            {name:'sequenceformatid'},
            {name:'paymentwindowtype'},
            {name: 'discountname', defValue: 0},
            {name: 'amountdueafterdiscount', defValue: 0}
            
        ]);

        this.userdsUrl = "";
        if(type == "Payment Received" || type=="Dishonoured Receive Payment"){
            this.userdsUrl = "ACCReceipt/getReceipts.do";
        }else{
            this.userdsUrl = "ACCVendorPayment/getPayments.do";
        }
            this.userds = new Wtf.data.GroupingStore({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data",
                    totalProperty:"count"
                },this.usersRec),
                url : this.userdsUrl,
                sortInfo : {
                    field : 'companyname',
                    direction : 'ASC'
                },
                groupField : 'companyname',
        //        url:Wtf.req.account+(this.isReceipt?'CustomerManager.jsp':'VendorManager.jsp'),
                baseParams:{
                    billid: billid,
                    mode:(this.isCustBill?35:32),           
                    isprinted:false
                }
            });  

            this.userds.load();
            var formrec1="";
            this.userds.on('load',function(){
            if(this.userds.getCount()>0){
                formrec1=this.userds.getAt(0);   

//            if(type == "Payment Received" && withoutInventory) {
//                callViewBillPayment(formrec, 'ViewBillingReceivePayment',true)
//            } 
//            else 
            if(type == "Payment Received" || type=="Dishonoured Receive Payment") {
                if(Wtf.isNewPaymentStructure) {
                    callViewPaymentNew(formrec1, 'ViewReceivePayment',true,this.grid);
                } 
            } 
//            else if(type == "Payment Made" && withoutInventory) {
//                callViewBillPayment(formrec, 'ViewBillingPaymentMade',false)
//            } 
            else if(type == "Payment Made" || type=="Dishonoured Make Payment") {
                if(Wtf.isNewPaymentStructure) {
                    callViewPaymentNew(formrec1, 'ViewPaymentMade',false,this.grid);
                }
            } 
        }
        },this);
    }else{
      var jetype=formrec.data['typeValue'];
      callViewJournalEntryTab(true, formrec,billid ,jetype,null)
    }
}

function viewTransactionTemplate1(type, formrec, withoutInventory,billid){
    //var withoutInventory = Wtf.account.companyAccountPref.withoutinventory;
 if (type=="RFQ"||type=="Purchase Requisition"||type=="Customer Quotation" || type=="Vendor Quotation"||type == "Sales Invoice" ||type == "Cash Sale" ||type == "Cash Purchase" ||type == "Purchase Invoice" || type=="Vendor Invoice" || type == Wtf.ERP_CONSIGNMENT_PURCHASE_INVOICE || type =="Customer Invoice" || type == "Delivery Order" || type == "Sales Order" || type == "Purchase Order" || type == "Goods Receipt Order") {
    this.GridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'companyid'},
        {name:'companyname'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'orderamount'},
        {name:'isexpenseinv'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name: 'isRoundingAdjustmentApplied'},
        {name:'date',type:'date'},
        {name:'duedate',type:'date'},
        {name:'shipdate',type:'date'},
        {name:'personname'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'partialinv',type:'boolean'},
        {name:'includeprotax',type:'boolean'},
        {name:'isapplytaxtoterms',type:'boolean'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'termdays'},
        {name:'termid'},
        {name:'termname'},
        {name:'incash'},
        {name:'taxamount'},
        {name:'tdsrate'},
        {name:'tdsamount'},
        {name:'totalAmountWithTDS'},
        {name:'TotalAdvanceTDSAdjustmentAmt'},
        {name:'taxid'},
        {name:'orderamountwithTax'},
        {name:'taxincluded',type:'boolean'},
        {name:'taxname'},
        {name:'deleted'},
        {name:'amountinbase'},
        {name:'memo'},
        {name:'createdby'},
        {name:'createdbyid'},
        {name:'externalcurrencyrate'},
        {name:'ispercentdiscount'},
        {name:'discountval'},
        {name:'crdraccid'},
        {name:'creditDays'},
        {name:'isRepeated'},
        {name:'porefno'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'NoOfpost'}, 
        {name:'NoOfRemainpost'},  
        {name:'templateid'},
        {name:'templatename'},
        {name:'startDate',type:'date'},
        {name:'nextDate',type:'date'},
        {name:'expireDate',type:'date'},
        {name:'repeateid'},
        {name:'status'},
        {name:'amountwithouttax'},
        {name:'amountwithouttaxinbase'},
        {name:'commission'},
        {name:'commissioninbase'},
        {name:'amountDueStatus'},
        {name:'salesPerson'},
        {name:'agent'},
        {name:'shipvia'},
        {name:'fob'},
        {name:'approvalstatus'},
        {name:'approvalstatusint', type:'int', defaultValue:-1},
        {name:'archieve', type:'int'},
        {name:'withoutinventory',type:'boolean'},
        {name:'isfavourite'},
        {name:'othervendoremails'},
        {name:'termdetails'},
        {name:'termsincludegst'},
        {name:'approvestatuslevel'},// for requisition
        {name:'posttext'},
        {name:'isOpeningBalanceTransaction'},
        {name:'isNormalTransaction'},
        {name:'isreval'},
        {name:'islockQuantityflag'},
        {name:'isprinted'},
        {name:'validdate',type:'date'},
        {name:'cashtransaction',type:'boolean'},
        {name:'landedInvoiceNumber'},
        {name:'termdays'},
        {name:'billingAddress'},
        {name:'billingCountry'},
        {name:'billingState'},
        {name:'billingPostal'},
        {name:'billingEmail'},
        {name:'billingFax'},
        {name:'billingMobile'},
        {name:'billingPhone'},
        {name:'billingContactPerson'},
        {name:'billingContactPersonNumber'},
        {name:'billingContactPersonDesignation'},
        {name:'billingCounty'},
        {name:'billingCity'},
        {name:'billingAddressType'},
        {name:'shippingAddress'},
        {name:'shippingCountry'},
        {name:'shippingState'},
        {name:'shippingCounty'},
        {name:'shippingCity'},
        {name:'shippingEmail'},
        {name:'shippingFax'},
        {name:'shippingMobile'},
        {name:'shippingPhone'},
        {name:'shippingPostal'},
        {name:'shippingContactPersonNumber'},
        {name:'shippingContactPersonDesignation'},
        {name:'shippingContactPerson'},
        {name:'shippingAddressType'},
        {name:'sequenceformatid'},
        {name:'fixedAssetInvoice'},
        {name:'fixedAssetLeaseInvoice'},
        {name:'lasteditedby'},
        {name:'statusID'},
        {name:'isCapitalGoodsAcquired'},
        {name:'isRetailPurchase'},
        {name:'importService'},
        {name:'getFullShippingAddress'},
              /*Below Fields Only Used for tThe cash Purcahse and Sales*/
        {name:'methodid'},
        {name:'paymentname'},
        {name:'detailtype'},
        {name:'cardno'},
        {name:'nameoncard'},
        {name:'cardexpirydate', type:'date'},
        {name:'cardtype'},
        {name:'cardrefno'},
        {name:'chequeno'},
        {name:'bankname'},
        {name:'chequedate', type:'date'},
        {name:'chequedescription'},
        {name:'paymentStatus'},
        {name:'clearanceDate', type:'date'},
        {name:'isConsignment'},
        {name:'isLeaseFixedAsset'},
        {name:'leaseOrMaintenanceSo'},
        {name:'custWarehouse'},
        {name:'requestWarehouse'},
        {name:'isExciseInvoice'},
        {name:'gstIncluded'},
        {name: 'gtaapplicable'},
        {name: 'isMerchantExporter'},
        {name: 'additionalMemo'}, // Additional memo column for INDONESIA country in Sales Invoice
        {name: 'additionalMemoName'}, // Additional memo column for INDONESIA country in Sales Invoice
        {name:'tdsrate'},
        {name:'tdsamount'},
        {name:'isFreeGift'},
        {name:'supplierinvoiceno'}
    ]);       
       
    this.StoreUrl = "";
    this.RemoteSort= false;
    if(type == "Customer Invoice" ||type == "Sales Invoice" ||type == "Cash Sale"){
        this.StoreUrl = "ACCInvoiceCMN/getInvoicesMerged.do";
       this.RemoteSort = true;
    }else if(type == "Vendor Invoice" ||type == "Purchase Invoice" ||type == "Cash Purchase" || type == Wtf.ERP_CONSIGNMENT_PURCHASE_INVOICE){
        this.StoreUrl = "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
      this.RemoteSort = true;
    } else if (type == "Delivery Order") {
        this.StoreUrl = "ACCInvoiceCMN/getDeliveryOrdersMerged.do";
        this.RemoteSort = true;
    } else if (type == "Sales Order") {
        this.StoreUrl = "ACCSalesOrderCMN/getSalesOrdersMerged.do";
        this.RemoteSort = true;
    } else if (type == "Purchase Order") {
        this.StoreUrl = "ACCPurchaseOrderCMN/getPurchaseOrdersMerged.do";
        this.RemoteSort = true;
    } else if (type == "Goods Receipt Order") {
        this.StoreUrl = "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
        this.RemoteSort = true;
    }
    else if (type == "Customer Quotation") {
        this.StoreUrl = "ACCSalesOrderCMN/getQuotations.do";
        this.RemoteSort = true;
    }
    else if (type == "Vendor Quotation") {
        this.StoreUrl = "ACCPurchaseOrderCMN/getQuotations.do";
        this.RemoteSort = true;
    }
        else if (type == "Purchase Requisition") {
            this.StoreUrl = "ACCPurchaseOrderCMN/getRequisitions.do";
            this.RemoteSort = true;
        }
        else if (type == "RFQ") {
            this.StoreUrl = "ACCPurchaseOrderCMN/getRFQs.do";
            this.RemoteSort = true;
        }
        
    this.Store = new Wtf.data.Store({
        url:this.StoreUrl,
        baseParams:{
                billid: billid,
                costCenterId: this.costCenterId,
                deleted:false,
                nondeleted:false,
                cashonly:false,
                creditonly:false,
                CashAndInvoice:true,
//              isFixedAsset:this.isFixedAsset,
                isLeaseFixedAsset:formrec!=undefined?formrec.data.isLeaseFixedAsset:false, //Lease Flag
                isConsignment:formrec!=undefined?formrec.data.isConsignment:false,  //consignment flag
                consolidateFlag:true,                
                isfavourite:false,
                isprinted:false
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });
    
    this.Store.load();
    var formrec1="";
    this.Store.on('load',function(){
        if(this.Store.getCount()>0){
            formrec1=this.Store.getAt(0);   
            
//                if(type == "Cash Sale" && withoutInventory) {
//                    callViewBillingCashReceipt(formrec,null, 'ViewBillingCSInvoice',true);
//                } else 
                if(type == "Cash Sale" && !withoutInventory) {
                    callViewCashReceipt(formrec1, 'ViewInvoice');
                } 
//                else if((type == "Sales Invoice" || type == "Customer Invoice") && withoutInventory) {
//                    callViewBillingInvoice(formrec,null, 'ViewBillingInvoice',false);
//                } 
                else if((type == "Sales Invoice" || type == "Customer Invoice") && !withoutInventory) {
                    if(formrec1.data.fixedAssetInvoice||formrec1.data.fixedAssetLeaseInvoice){
                        callViewFixedAssetInvoice(formrec1, billid+'Invoice',false,undefined,false,formrec1.data.fixedAssetInvoice,formrec1.data.fixedAssetLeaseInvoice);
                    } else if(formrec1.data.isConsignment){
                        callViewConsignmentInvoice(true,formrec1,formrec1.data.billid+'ConsignmentInvoice',false,false,true);
                    }else{
                        callViewInvoice(formrec1, 'ViewCashReceipt');
                    }
                } 
//                else if(type == "Cash Purchase" && withoutInventory) {
//                    callViewBillingPaymentReceipt(formrec,null, 'ViewBillingCSInvoice',true);
//                } 
                else if(type == "Cash Purchase" && !withoutInventory) {
                    callViewPaymentReceipt(formrec1, 'ViewPaymentReceipt',formrec1.get("isexpenseinv"));
                } 
//                else if((type == "Purchase Invoice" || type == "Vendor Invoice")&& withoutInventory) {
//                    callViewBillingGoodsReceipt(formrec,null, 'ViewBillingInvoice',false);
//                } 
                else if((type == "Purchase Invoice" || type == "Vendor Invoice" || type == Wtf.ERP_CONSIGNMENT_PURCHASE_INVOICE) && !withoutInventory){
                    if(formrec1.data.fixedAssetInvoice){
                        callViewFixedAssetGoodsReceipt(formrec1, billid+'GoodsReceipt',false,formrec1.get("isexpenseinv"),undefined,false,formrec1.data.fixedAssetInvoice);
                    } else if(formrec1.data.isConsignment){
                         callViewConsignmentGoodsReceipt(true,formrec1,formrec1.data.billid+'ConsignmentInvoice',false,false,true);
                    }else{
                        callViewGoodsReceipt(formrec1, 'ViewGoodsReceipt',formrec1.get("isexpenseinv"));
                    }
                } else if (type == "Delivery Order" && !withoutInventory) {
                    if(formrec1.data.isConsignment){
                        callViewConsignmentDeliveryOrder(true,formrec1,billid,formrec1.data.isFixedAsset,formrec1.data.isLeaseFixedAsset,formrec1.data.isConsignment);
                    } else if(formrec1.data.isFixedAsset ||formrec1.data.isLeaseFixedAsset){         
                        callViewFixedAssetDeliveryOrder(true,formrec1,billid,formrec1.data.isFixedAsset,formrec1.data.isLeaseFixedAsset);
                    } else{
                        callViewDeliveryOrder(true,formrec1,formrec1.data.billid,false,false)
                    }
                } else if (type == "Sales Order" && !withoutInventory) {
                    if(formrec1.data.isLeaseFixedAsset || formrec1.data.leaseOrMaintenanceSo==1){
                        callViewFixedAssetLeaseSalesOrder(true,formrec1,formrec1.data.billid, false,null,false,false,false,true);
                    } else  if(formrec1.data.isConsignment){
                        callViewConsignmentRequest(true,formrec1,formrec1.data.billid, false,null,true,false,false,true,true);
                    } else{
                        callViewSalesOrder(true,formrec1,formrec1.data.billid, false);
                    }
                } else if (type == "Purchase Order" && !withoutInventory) {
                    if(formrec1.data.isConsignment){
                        callViewConsignmentRequest(true,formrec1,billid, false,null,false,false,false,true,false);
                    }else{
                        callViewPurchaseOrder(true,formrec1,formrec1.data.billid,false,this,Wtf.autoNum.PurchaseOrder,true);
                    }
                } else if (type == "Goods Receipt Order" && !withoutInventory) {
                     if(formrec1.data.isConsignment){
                        callViewConsignmentGoodsReceiptDelivery(true,formrec1,billid,false,false,formrec1.data.isConsignment)
                    }else{
                       callViewGoodsReceiptDelivery(true,formrec1,formrec1.data.billid,false,false)
                    }
                   
                }
                else if(type == "Customer Quotation"){
                     callViewQuotation(true, formrec1.data.billid, formrec1, true);
            } 
                else if(type == "Vendor Quotation"){
                    callViewVendorQuotation(true, formrec1.data.billid, formrec1, false);
                }
                else if (type == "Purchase Requisition") {
                    callViewPurchaseReq(true, formrec1, formrec1.data.billid);
                }
                else if (type == "RFQ") {
                    var PR_MEMOS = "1) " + formrec1.data.billno + " - " + formrec1.data.memo + "\n";
                    var PR_IDS = formrec1.data.billid;
                    if (!formrec1.data.personid) {
                        formrec1.data.personid = "";
                    }
                    callViewRequestForQuotation(true, PR_IDS, PR_MEMOS, formrec1);
                }
            } 
        
    },this);
}
else if(type == "Debit Note" ||type == "Credit Note" ){    
        this.GridRec = Wtf.data.Record.create ([
        {name:"noteid"},
        {name:"noteno"},
        {name:"currencyid"},
        {name:'companyid'},
        {name:'companyname'},
        {name:'journalentryid'},        
        {name:'currencysymbol'},
        {name:'entryno'},
        {name:"personid"},
        {name:"personname"},
        {name:'amount'},
        {name:'amountdue'},
        {name:'costcenterid'},
        {name:'deleted'},
        {name:'costcenterName'},
        {name:"date",type:'date'},
        {name:'memo'},
        {name:'notetax'},
        {name:'noteSubTotal'},
        {name:'withoutinventory'},
        {name:'otherwise'},
        {name:'openflag'},
        {name:'isOpeningBalanceTransaction'},
        {name:'isNormalTransaction'},
        {name:'isOldRecord'},
        {name:'cntype'},
        {name:'isprinted'},
        {name:'partlyJeEntryWithCnDn'},
        {name:'sequenceformatid'},
        {name:'salesPersonID'},
        {name:'externalcurrencyrate'},
        {name:'billid'},
        {name:'gTaxId'},
        {name:'salesPerson'},
        {name:'agent'},
        {name:'agentid'},
        {name:'includeprotax'},
        {name:'lasteditedby'},
        {name:'prtaxid'},
        {name:'linkingdate'}
    ]);
    this.StoreUrl = "";
   
    if(type == "Credit Note"){
        this.StoreUrl = "ACCCreditNote/getCreditNoteMerged.do";
    }else {
        this.StoreUrl = "ACCDebitNote/getDebitNoteMerged.do";
    }
   
        this.Store = new Wtf.data.GroupingStore({
            url:this.StoreUrl,
    //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            baseParams:{
                noteid: billid,
                mode:(type == "Credit Note")?(withoutInventory?62:27):(withoutInventory?62:28),
                costCenterId: this.costCenterId,
                deleted:"false",
                nondeleted:"false",
                viewMode:true
            },
            sortInfo : {
                field : 'companyname',
                direction : 'ASC'
            },
            groupField : 'companyname',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.GridRec)
        });
               
    this.Store.load();
    var formrec1="";
    this.Store.on('load',function(){
        if(this.Store.getCount()>0){
            formrec1=this.Store.getAt(0);   
              // check whether note is linked with payment or not        
            Wtf.Ajax.requestEx({
                url: (type == "Credit Note")?"ACCVendorPaymentCMN/getPaymentsLinkedWithNCreditNote.do":"ACCReceiptCMN/getPaymentsLinkedWithDebitNote.do",
                params:{
                    noteId:formrec1.get('noteid')
                }
            },this,function(response, request){
                    var noteType = formrec1.get('cntype');
                    var isNoteLinkedWithPayment = response.isNoteLinkedWithPayment;

//                if(!isNoteLinkedWithPayment){             // remove restriction for CN DN whose payment made
//                if (type == "Credit Note" && withoutInventory) {
//                    callViewBillingCreditNote(formrec, 'ViewcreditNote')
//                } 
                    if (type == "Credit Note") {
                        /*
                         *For company is malaysian 
                         *while opening Debit note report.
                         */
                        if (formrec1 != undefined && formrec1.data.cntype == '5') {
                            callViewCreditNoteGst(true, formrec1, false, false, true);
                        } else if (formrec1 != undefined && formrec1.data.cntype == Wtf.NoteForOvercharge) {
                            var winid = this.isCNReport ? 'creditnoteForOverchargeView' + formrec1.get("noteno") : 'debitnoteForOverchargeView' + formrec1.get("noteno");
                            callEditNoteForOvercharge(winid, formrec1, true, true, true, true);//cntype=6 - CN/DN for Overcharge
                        } else {
                            callViewCreditNote("ViewcreditNote" + formrec1.get("noteno"), true, true, formrec1.get('cntype'), formrec1, null);
                        }
                    }
//                else if (type == "Debit Note" && withoutInventory) {
//                    callViewBillingDebitNote(formrec, 'ViewDebitNote')
//                } 
                    else if (type == "Debit Note") {
                        /*
                         *For company is malaysian 
                         *while opening Debit note report.
                         */
                        if (formrec1 != undefined && formrec1.data.cntype == '5') {
                            callViewCreditNoteGst(true, formrec1, false, false, false);
                        } else if (formrec1 != undefined && formrec1.data.cntype == Wtf.NoteForOvercharge) {
                            var winid = this.isCNReport ? 'creditnoteForOverchargeView' + formrec1.get("noteno") : 'debitnoteForOverchargeView' + formrec1.get("noteno");
                            callEditNoteForOvercharge(winid, formrec1, true, true, false, true);//cntype=6 - CN/DN for Overcharge
                        } else {
                            callViewDebitNote("ViewDebitNote" + formrec1.get("noteno"), true, false, formrec1.get('cntype'), formrec1, null);
                        }
                    }
//                }
                }
          ,function(response, request){                
             
            }); 
    }
    },this);
      
}else if(type=="Payment Received"||type == "Payment Made"){
//    if(!Wtf.isNewPaymentStructure){
//           this.usersRec = new Wtf.data.Record.create([
//            {name: 'billid'},
//            {name:'companyid'},
//            {name:'companyname'},
//            {name: 'refid'},
//            {name: 'personid'},
//            {name: 'billno'},
//            {name: 'refno'},
//            {name: 'refname'},
//            {name: 'refdetail'},
//            {name:'personemail'},
//            {name: 'detailtype'},
//            {name: 'expirydate',type:'date'},
//            {name: 'journalentryid'},
//            {name: 'entryno'},
//            {name: 'currencysymbol'},
//            {name: 'externalcurrencyrate'},
//            {name: 'personname'},
//    //        {name: 'customervendorname'},
//            {name: 'address'},
//            {name: 'deleted'},
//            {name: 'billdate',type:'date'},
//            {name: 'paymentmethod'},
//            {name: 'chequenumber'},
//            {name: 'memo'},
//            {name: 'amount'},
//            {name: 'methodid'},
//            {name: 'receiptamount'},
//            {name: 'currencyid'},
//            {name: 'detailsjarr'},
//            {name: 'clearanceDate',type:'date'},
//            {name: 'paymentStatus'},
//            {name: 'otherwise'},
//            {name: 'isOpeningBalanceTransaction'},
//            {name: 'isNormalTransaction'},
//            {name: 'receipttype'},
//            {name: 'isadvancepayment'},
//            {name: 'isadvancefromvendor'},
//            {name: 'advanceUsed'},
//            {name: 'advanceid'},
//            {name: 'advanceamount'},
//            {name: 'withoutinventory'},
//            {name: 'refcardno'},
//            {name: 'refexpdate'},
//            {name: 'ismanydbcr'},
//            {name:'dramount'},
//            {name:'bankCharges'},
//            {name:'bankChargesCmb'},
//            {name:'bankInterest'},
//            {name:'bankInterestCmb'},
//            {name:'paidToCmb'},
//            {name:'paidto'},
//            {name:'isprinted'},
//            {name:'sequenceformatid'}
//        ]);     
//    }
//else
{
   this.usersRec = new Wtf.data.Record.create([
        {name: 'billid'},
        {name:'companyid'},
        {name:'companyname'},
        {name: 'refid'},
        {name: 'personid'},
        {name: 'billno'},
        {name: 'refno'},
        {name: 'refname'},
        {name: 'refdetail'},
        {name:'personemail'},
        {name: 'detailtype'},
        {name: 'expirydate',type:'date'},
        {name: 'journalentryid'},
        {name: 'entryno'},
        {name: 'currencysymbol'},
        {name: 'externalcurrencyrate'},
        {name: 'personname'},
//        {name: 'customervendorname'},
        {name: 'address'},
        {name: 'deleted'},
        {name: 'billdate',type:'date'},
        {name: 'chequedate',type:'date'},
        {name: 'chequedateforprint',type:'date'},
        {name: 'paymentmethod'},
        {name: 'chequenumber'},
        {name: 'chequedescription'},
        {name: 'memo'},
        {name: 'amount'},
        {name: 'amountinbase'},
        {name: 'methodid'},
        {name: 'receiptamount'},
        {name: 'currencyid'},
        {name: 'detailsjarr'},
        {name: 'clearanceDate',type:'date'},
        {name: 'paymentStatus'},
        {name: 'otherwise'},
        {name: 'isOpeningBalanceTransaction'},
        {name: 'isNormalTransaction'},
        {name: 'receipttype'},
        {name: 'paymentwindowtype'},
        {name: 'isadvancepayment'},
        {name: 'isadvancefromvendor'},
        {name: 'advanceUsed'},
        {name: 'advanceid'},
        {name: 'advanceamount'},
        {name: 'withoutinventory'},
        {name: 'refcardno'},
        {name: 'refexpdate'},
        {name: 'ismanydbcr'},
        {name:'dramount'},
        {name:'bankCharges'},
        {name:'bankChargesCmb'},
        {name:'bankInterest'},
        {name:'bankInterestCmb'},
        {name:'paidToCmb'},
        {name:'paidto'},
        {name:'isprinted'},
        {name:'sequenceformatid'},
        {name:'totaltaxamount'},
        {name: 'isIBGTypeTransaction',type:'boolean'},
        {name: 'ibgDetailsID'},
        {name: 'ibgCode'},
        {name:'paymentamountdue'},
        {name:'isLinked'},
        {name:'linkedadvanceMsgFlag'},
        {name:'disableOtherwiseLinking'},        
        {name:'cndnid'},
        {name:'invoiceadvcndntype'},
        {name:'cndnAndInvoiceId'},
        {name:'ischequeprinted', type:'boolean'},
        {name: 'discountname', type: 'float', defValue: 0},
        {name: 'amountdueafterdiscount', tyep: 'float', defValue: 0}

    ]); 
}

        this.userdsUrl = "";
        if (Wtf.isNewPaymentStructure) {
            if (type == "Payment Received") {
                this.userdsUrl = "ACCReceiptCMN/getReceipts.do";
            } else {
                this.userdsUrl = "ACCVendorPaymentCMN/getPayments.do";
            }
        } 
//        else {
//            if (type == "Payment Received") {
//                this.userdsUrl = "ACCReceipt/getReceipts.do";
//            } else {
//                this.userdsUrl = "ACCVendorPayment/getPayments.do";
//            }
//        }
            this.userds = new Wtf.data.GroupingStore({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data",
                    totalProperty:"count"
                },this.usersRec),
                url : this.userdsUrl,
                sortInfo : {
                    field : 'companyname',
                    direction : 'ASC'
                },
                groupField : 'companyname',
        //        url:Wtf.req.account+(this.isReceipt?'CustomerManager.jsp':'VendorManager.jsp'),
                baseParams:{
                    mode:(this.isCustBill?35:32),           
                    isprinted:false,
                    billid: billid
                }
            });  

            this.userds.load();
            var formrec1="";
            this.userds.on('load',function(){
            if (this.userds.getCount() > 0) {
                formrec1 = this.userds.getAt(0);
//                if (type == "Payment Received" && withoutInventory) {
//                    callViewBillPayment(formrec, 'ViewBillingReceivePayment', true)
//                } else 
                if (type == "Payment Received") {
                    if (Wtf.isNewPaymentStructure) {
                        callViewPaymentNew(formrec1, 'ViewReceivePayment', true, this.grid);
                    }
                } 
//                else if (type == "Payment Made" && withoutInventory) {
//                    callViewBillPayment(formrec, 'ViewBillingPaymentMade', false)
//                } 
                else if (type == "Payment Made") {
                    if (Wtf.isNewPaymentStructure) {
                        callViewPaymentNew(formrec1, 'ViewPaymentMade', false, this.grid);
                    }
                }
            }
        },this);
    }else if(type=="Product Assembly"){
        this.auditRecord = Wtf.data.Record.create([
        {
            name:'productid'
        },
        {
            name:'productname'
        },
        {
            name:'quantity'
        },
        {
            name:'productrefno'
        },
        {
            name:'memo'
        },
        {
            name:'description'
        },
        {
            name:'mainproductid'
        },
        {
            name:'sequenceformatid'
        },{
            name:'bomdetailid'
        }, {
            name:'bomCode'
        },
        {
            name:'journalentryid'
        }

        ]);

        
        this.auditReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, this.auditRecord);
    
        this.store = new Wtf.data.Store({
            proxy: new Wtf.data.HttpProxy({
                url:"ACCProduct/getAssemblyProducts.do"
            }),
            baseParams:{
//                    productid: billid,
                    billid: billid
            },
            reader: this.auditReader
        });
        
        this.store.load();
        this.store.on('load',function(){
            if (this.store.getCount() > 0) {
                var formrec1 = WtfGlobal.searchRecord(this.store, formrec.data.transactionID, 'productrefno'); //get the exact match with transactionID record;
                callBuildAssemblyForm(billid, formrec1, true, false);
            }
        },this);
    }else if(type=="Product Disassembly"){  //ERP-25361 (This code piece has written to get the unbuild assembly details in view mode of Unbuild Assembly Form.)
        this.auditRecord = Wtf.data.Record.create([
        {
            name:'productid'
        },
        {
            name:'productname'
        },
        {
            name:'quantity'
        },
        {
            name:'productrefno'
        },
        {
            name:'memo'
        },
        {
            name:'description'
        },
        {
            name:'mainproductid'
        },
        {
            name:'sequenceformatid'
        },{
            name:'bomdetailid'
        }, {
            name:'bomCode'
        },
        {
            name:'journalentryid'
        }

        ]);

        
        this.auditReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, this.auditRecord);
    
        this.store = new Wtf.data.Store({
            proxy: new Wtf.data.HttpProxy({
                url:"ACCProduct/getAssemblyProducts.do"
            }),
            baseParams:{
//                    productid: billid,
                    billid: billid,
                    isUnbuildAssembly : true
            },
            reader: this.auditReader
        });
        
        this.store.load();
        this.store.on('load',function(){
            if (this.store.getCount() > 0) {
                var formrec1 = WtfGlobal.searchRecord(this.store, formrec.data.transactionID, 'productrefno'); //get the exact match with transactionID record;
                callBuildAssemblyForm(billid, formrec1, true, true);
            }
        },this);
    } else if (type == "Stock Adjustment") {
        markoutList(billid);
    } else if (type == "Loan Disbursement") {
        this.disburseStore = new Wtf.data.Store({  
            url:"ACCLoanCMN/getLoanDisbursements.do",
            baseParams:{
                billid:billid  
            },
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalCount",
                root: "data"
            }) 
        });
   
       this.disburseStore.on('load',function(){
            if (this.disburseStore.getCount() > 0) {
                formrec = this.disburseStore.getAt(0);
                callViewLoanDisbursement(true, formrec, "");
            }
        },this);
        this.disburseStore.load();
    } else if (type == "Sales Return" || type == "Purchase Return") {

        this.GridRec = Wtf.data.Record.create([
            {name: 'billid'},
            {name: 'companyid'},
            {name: 'companyname'},
            {name: 'journalentryid'},
            {name: 'entryno'},
            {name: 'billto'},
            {name: 'orderamount'},
            {name: 'shipto'},
            {name: 'mode'},
            {name: 'billno'},
            {name: 'date', type: 'date'},
            {name: 'shipdate', type: 'date'},
            {name: 'personname'},
            {name: 'aliasname'},
            {name: 'personemail'},
            {name: 'billingEmail'},
            {name: 'personid'},
            {name: 'shipping'},
            {name: 'deleted'},
            {name: 'externalcurrencyrate'},
            {name: 'memo'},
            {name: 'costcenterid'},
            {name: 'costcenterName'},
            {name: 'statusID'},
            {name: 'shipvia'},
            {name: 'fob'},
            {name: 'status'},
            {name: 'withoutinventory', type: 'boolean'},
            {name: 'isfavourite'},
            {name: 'currencyid'},
            {name: 'currencysymbol'},
            {name: 'amount'},
            {name: 'amountinbase'},
            {name: 'discountamountinbase'},
            {name: 'sequenceformatid'},
            {name: 'lasteditedby'},
            {name: 'isConsignment'},
            {name: 'isNoteAlso'},
            {name: 'isAssignSRNumberntocn'},
            {name: 'isdeletable'},
            {name: 'cndnsequenceformatid'},
            {name: 'cndnnumber'},
            {name: 'movementtype'},
            {name: 'custWarehouse'},
            {name: 'movementtype'},
            {name: 'movementtypename'},
            {name: 'includeprotax'},
            {name: 'attachdoc'}, //SJ[ERP-16331]
            {name: 'attachment'}, //SJ[ERP-16331]
            {name: 'taxid'},
            {name: 'totaltaxamount'},
            {name: 'amountwithouttax'},
            {name: 'hasAccess', type: 'boolean'},
            {name: 'currencycode'},
            {name: 'mapSalesPersonName'},
            {name: 'salesPerson'},
        ]);
        this.StoreUrl = "";
        this.RemoteSort = false;
        if (type == "Purchase Return") {
            this.StoreUrl = "ACCGoodsReceiptCMN/getPurchaseReturn.do";
        } else if (type == "Sales Return") {
            this.StoreUrl = "ACCInvoiceCMN/getSalesReturn.do";
        }
        this.Store = new Wtf.data.Store({
            url: this.StoreUrl,
            baseParams: {
                billid: billid,
                deleted: false
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            }, this.GridRec)
        });

        this.Store.load();
        var formrec1 = "";
        this.Store.on('load', function() {
            if (this.Store.getCount() > 0) {
                formrec1 = this.Store.getAt(0);
                if (type == "Sales Return") {
                    callViewSalesReturn(true, formrec1, formrec1.data.billid, false, formrec1.data.isNoteAlso);
                } else if (type == "Purchase Return") {
                    callViewPurchaseReturn(true, formrec1, formrec1.data.billid, false, formrec1.data.isNoteAlso);
                }
            }
        }, this);

    } else {
        var jetype = formrec.data['typeValue'];
        callViewJournalEntryTab(true, formrec, billid, jetype, null)
    }
}
function validateSelection(combo,record,index){
    if(record!=null && record!=undefined)
        return record.get('hasAccess');
    return true;
}

/*
* If entered value in combo is random or without selected then 
* restrict it from to do so
*/
function validateSelectionOfLinkingCombo(combo){
    if(combo!=undefined && combo.getValue()!=undefined && combo.getValue()=="" && Wtf.account.companyAccountPref.isPRmandatory){
        combo.setValue("");
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.global.pocombo")],2);
        return; 
    }else{
        return true;  
    }
}
function checkForAccountActivate(store,recid,recKey){
    var rec=WtfGlobal.searchRecord(store, recid, recKey);
    if(rec!=null && !rec.get('hasAccess')){
        return false;
    }
    return true;
}

function isTaxActivate(store, recid, recKey) {
    var rec = WtfGlobal.searchRecord(store, recid, recKey);
    if (rec != null && !rec.get('hasAccess')) {
        return false;
    }
    return true;
}

function hideAllChildDimensions(childIdArray, compId) {
    var childArray = childIdArray.split(',');
    for (var i = 0; i < childArray.length; i++) {
        var childId = childArray[i];
        var childComponent = Wtf.getCmp(childId + compId);
        if (childId != "" && childId != undefined) {
            childComponent.setValue("1234");
            if (childComponent.ownerCt && Wtf.account.companyAccountPref.hierarchicalDimensions) {
                Wtf.getCmp(childComponent.ownerCt.id).hide();
            }
            childComponent.store.removeAll();
            childId = childComponent.childid;
            if (childId != "" && childId != undefined) {
                hideAllChildDimensions(childId, compId);
            }
        }
    }
}
// Set child dimension values recursively if there is only one value of child for selected parent value. i.e one to one mapping
     function setRecursiveChildDimensionsValues(compid, childids, value) {
         if(value && childids && childids.length>0){
            var childidArray=childids.split(",");
            for(var i=0;i<childidArray.length;i++){
                var childComponent = Wtf.getCmp(childidArray[i]+compid);
                var currentBaseParams = childComponent.store.baseParams;
                currentBaseParams.parentid=value;
                childComponent.setValue("1234");
                childComponent.store.baseParams=currentBaseParams;
                childComponent.store.myid=childidArray[i]+compid;
                if(childComponent.ownerCt){
                    if(value=="1234"){
                        // Hide and Reset all hierarchical childs Dimensions if parent value is "None"
                        var childId = childidArray[i];
                        hideAllChildDimensions(childId,compid);
                    }else{
                        // Show and reset only nearest Child then hide and reset other sub childs 
                        if(Wtf.account.companyAccountPref.hierarchicalDimensions){
                            Wtf.getCmp(childComponent.ownerCt.id).show();
                        }
                        var childId = childComponent.childid;
                        hideAllChildDimensions(childId,compid);
                    }
                }
                
                childComponent.store.on('load', function(s, rec) {
                    var hasPermission = (Wtf.getCmp(s.myid).isAddnew != "" && Wtf.getCmp(s.myid).isAddnew != undefined) ? Wtf.getCmp(s.myid).isAddnew : false;
                    if ((hasPermission && s.getCount() == 3 ) || (!hasPermission && s.getCount() == 2)) {            // [0]:None [1]:mapped value.
                        var record = rec[1];
                        /**
                        *if the master item is activated then value will set to combo.
                        */
                        if(record !=undefined && record.data.hasAccess!=undefined && record.data.hasAccess){
                            Wtf.getCmp(s.myid).setValue(record.data.id);
                        }
                        setRecursiveChildDimensionsValues(compid, Wtf.getCmp(s.myid).childid, record.data.id);
                    } else if (s.getCount() == 1){
                        if (Wtf.getCmp(s.myid).ownerCt && Wtf.account.companyAccountPref.hierarchicalDimensions) {
                            Wtf.getCmp(Wtf.getCmp(s.myid).ownerCt.id).hide();
                        }
                    }
                }, this);
                childComponent.store.load();
            }
        }
        Wtf.getCmp(compid).doLayout();
    }

function expandCollapseGrid(btntext, expandstore, expanderObj, scopeObject) {
    var arr = "";
    var store = scopeObject.grid.getStore();
    if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
        for (var i = 0; i < store.data.length; i++) {
            expanderObj.collapseRow(i)
        }
        scopeObject.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
    } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
        store.each(function(rec) {
            if (rec.data.billid != "" && rec.data.billid != undefined)
                arr += rec.data.billid + ",";
        }, scopeObject);
        if (arr.length != 0) {
            var colModelArray = [];
            colModelArray = GlobalColumnModel[scopeObject.moduleid];
            WtfGlobal.updateStoreConfig(colModelArray, expandstore);
            colModelArray = [];
            colModelArray = GlobalColumnModelForProduct[scopeObject.moduleid];
            WtfGlobal.updateStoreConfig(colModelArray, expandstore);
            arr = arr.substring(0, arr.length - 1);
            scopeObject.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
        expandstore.load({params: {bills: arr, isexpenseinv: "", isConsignment: scopeObject.isConsignment, moduleid:scopeObject.moduleid,isFixedAsset: scopeObject.isFixedAsset, isLeaseFixedAsset: scopeObject.isLeaseFixedAsset,isForReport:true}});//isForReport - to get field type 4,7's value on row expand
    }
}

//Global function to check whether some specific date falls under active date range or not.
function isFromActiveDateRange(date){
    if(Wtf.account.companyAccountPref.activeDateRangeFromDate != '' && Wtf.account.companyAccountPref.activeDateRangeToDate !='' && Wtf.account.companyAccountPref.activeDateRangeFromDate != undefined && Wtf.account.companyAccountPref.activeDateRangeToDate !=undefined && Wtf.account.companyAccountPref.activeDateRangeFromDate != null && Wtf.account.companyAccountPref.activeDateRangeToDate !=null ){
        var startDate = new Date(Wtf.account.companyAccountPref.activeDateRangeFromDate);
        var endDate = new Date(Wtf.account.companyAccountPref.activeDateRangeToDate);
        if(date.getTime() >= startDate.getTime() && date.getTime()<= endDate.getTime()){
            return true
        } else {
            return false;
        }
    }
    return true;
}
// Checkes the exchange rate present is nearest exchange rate or not. If Yes then add new exchange rate
function checkForNearestExchangeRate(obj,record,transactionDate){
    if(record!=null && record!=undefined && record!=""){
        if(record.data.ismaxnearestexchangerate!=null && record.data.ismaxnearestexchangerate!=undefined && record.data.ismaxnearestexchangerate!="" && record.data.ismaxnearestexchangerate){	//SDP-12282
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.currencyexchange, Wtf.Perm.currencyexchange.view) && !WtfGlobal.EnableDisable(Wtf.UPerm.currencyexchange, Wtf.Perm.currencyexchange.edit)) {
            Wtf.MessageBox.confirm("Warning",WtfGlobal.getLocaleText("acc.addExchangeRate.novalidexchangerateisavailable")+"<br>"+WtfGlobal.getLocaleText("acc.addExchangeRate.Lastvalidexchangeratewillbeconsidered")+"<br><b>"+WtfGlobal.getLocaleText("acc.addExchangeRate.addnewexchangerateforselectedcurrency"), function(btn){
                if(btn == 'yes') {  
                    var superthis=obj;
                    addExchangeRate(record,transactionDate,superthis);
                }
            },this);
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.addExchangeRate.novalidexchangerateisavailable")+"<br>"+WtfGlobal.getLocaleText("acc.addExchangeRate.Lastvalidexchangeratewillbeconsidered")+"<br><br><b>"+WtfGlobal.getLocaleText({key: "acc.addExchangeRate.contactpersoninchangreofrate"})], 2);
        }
      } 
    }
}
//Global function to open attachments window for upload and download files
function openAttachmentsWindow(obj){
    //Window for upload and download attachment files
    obj.imageUploadURL=(obj.savedFilesMappingId==undefined || obj.savedFilesMappingId==null) ?'ACCLoanCMN/attachDocuments.do?type=doc' :'ACCLoanCMN/attachDocuments.do?type=doc&savedFilesMappingId='+obj.savedFilesMappingId;
    var scopeObj = obj;
    var comp = Wtf.getCmp('attachmentFileUploadWindow');
    if(!comp){
        if(obj.uploadForm != null && obj.uploadForm != undefined){
            if(obj.uploadForm.fileStr != null && obj.uploadForm.fileStr != undefined){
                /*
                 *Updated fileStr in WtfMain-ex.js file assigning here in attachFileStr.
                 *When we are deleting document in copy case it is not updated.
                 *Because of that reason this code is added.
                 */
                scopeObj.attachedFilesStr = obj.uploadForm.fileStr;
            }
        }
        obj.uploadForm = new Wtf.MultiFlieUploadPanel({
            methodType : 'upload',
            id:'attachmentFileUploadWindow',
            layout : 'fit',
            closable:false,
            border : false,
            url : obj.imageUploadURL,
            savedFilesMappingId:obj.savedFilesMappingId,
            isDisbursement : false,
            isFromOtherForm : true,
            readOnly : obj.readOnly,
            docid : obj.record ? obj.record.data.billid : "",
            copyInv :obj.copyInv != undefined ? obj.copyInv : "",
            fileStr:obj.isFromSaveAndCreateNewButton ? undefined : scopeObj.attachedFilesStr,
            bbar : [new Wtf.FileBrowseButton({
                text : WtfGlobal.getLocaleText("acc.activitydetailpanel.addfilesBTN"),//'Add Files',
                tooltip : WtfGlobal.getLocaleText("acc.template.addfiles.ttip"),//'Click here to browse and add your files to upload',
                hidden : obj.readOnly,
                handler : function(btn) {
                    this.uploadForm.addFiles(btn);
                },
                scope : obj
            }),{
                text : WtfGlobal.getLocaleText("acc.uploadbtn"),//'Upload',
                tooltip : WtfGlobal.getLocaleText("acc.template.uploadbtn.ttip"),//'Click here to start uploading your files which are listed above',
                hidden : obj.readOnly,
                handler : function() {
                    this.uploadForm.startUpload();
                },
                scope : obj
            },{
                text : WtfGlobal.getLocaleText("acc.template.stop"),//'Stop',
                tooltip : WtfGlobal.getLocaleText("acc.template.canceluploading.ttip"),//'Cancel uploading of files which are not uploaded yet',
                hidden : obj.readOnly,
                handler : function() {
                    this.uploadForm.cancelUpload();
                },
                scope : obj
            },{
                text : WtfGlobal.getLocaleText("acc.template.clear"),//'Clear',
                tooltip : WtfGlobal.getLocaleText("acc.template.removefilesttip"),//'remove all files which are listed above',
                hidden : obj.readOnly,
                handler : function() {
                    this.uploadForm.clearAll();
                },
                scope : obj
            },'->',{
                text : WtfGlobal.getLocaleText("acc.msgbox.ok"),//'Clear',
                //            tooltip : WtfGlobal.getLocaleText("acc.template.removefilesttip"),//'remove all files which are listed above',
                handler : function() {
                    this.attachWindow.close();
                },
                scope : obj
            }]
        });
        obj.uploadForm.on('uploadComplete',function(){
            this.savedFilesMappingId = this.uploadForm.savedFilesMappingId;
            this.attachedFilesStr = this.uploadForm.savedFilesId;
            this.uploadForm.fileStr=this.uploadForm.savedFilesId;
        },obj);
        obj.attachWindow=new Wtf.Window({
            width:600,
            height: 300,
            modal :true,
            title : WtfGlobal.getLocaleText("acc.contract.uploadnewfile"),//'Upload File',
            layout : 'fit',
            items:[obj.uploadForm]
        });
        obj.attachWindow.show();
    }
}

function alertForOpeningTransactions(){
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.openingTransactions.clickedOnLink")], 2);
    return;
}
Wtf.data.KwlJsonReader2 = function(meta, recordType){
    meta = meta || {};
    Wtf.data.KwlJsonReader2.superclass.constructor.call(this, meta, recordType);
    this.events = {
        aftereval : true
    };
    this.on("aftereval", this.jsonErrorResponseHandler);
};  

Wtf.extend(Wtf.data.KwlJsonReader2, Wtf.data.KwlDataReader, {
    read : function(response){
        var json = response;
        var o =response.data; // eval(" (" +json +") ");
        if (o && o.valid==false) {
            signOut("timeout");
        }
                
        //                if(!o) {
        //                    throw {message: "JsonReader.read: Json object not found"};
        //                }
        if(o.metaData){
            delete this.ef;
            this.meta = o.metaData;
            this.recordType = Wtf.data.Record.create(o.metaData.fields);
            this.onMetaChange(this.meta, this.recordType, o);
        }              
               
        //this.fireEvent("aftereval", o, this, response);
        return this.readRecords(o);
    },
      
    onMetaChange : function(meta, recordType, o){

    },

    jsonErrorResponseHandler:function (json, reader, response) {
        if (json && !json.valid) {
            signOut("timeout");
        }
    },

    simpleAccess: function(obj, subsc) {
        return obj[subsc];
    },

    getJsonAccessor: function(){
        var re = /[\[\.]/;
        return function(expr) {
            try {
                return(re.test(expr))
                ? new Function("obj", "return obj." + expr)
                : function(obj){
                    return obj[expr];
                };
            } catch(e){}
            return Wtf.emptyFn;
        };
    }(),

   
    readRecords : function(o){
        this.jsonData = o;
        var s = this.meta, Record = this.recordType,
        f = Record.prototype.fields, fi = f.items, fl = f.length;

        if (!this.ef) {
            if(s.totalProperty) {
                this.getTotal = this.getJsonAccessor(s.totalProperty);
            }
            if(s.successProperty) {
                this.getSuccess = this.getJsonAccessor(s.successProperty);
            }
            this.getRoot = s.root ? this.getJsonAccessor(s.root) : function(p){
                return p;
            };
            if (s.id) {
                var g = this.getJsonAccessor(s.id);
                this.getId = function(rec) {
                    var r = g(rec);
                    return (r === undefined || r === "") ? null : r;
                };
            } else {
                this.getId = function(){
                    return null;
                };
            }
            this.ef = [];
            for(var i = 0; i < fl; i++){
                f = fi[i];
                var map = (f.mapping !== undefined && f.mapping !== null) ? f.mapping : f.name;
                this.ef[i] = this.getJsonAccessor(map);
            }
        }

        var root = this.getRoot(o), c = root.length, totalRecords = c, success = true;
        if(s.totalProperty){
            var v = parseInt(this.getTotal(o), 10);
            if(!isNaN(v)){
                totalRecords = v;
            }
        }
        if(s.successProperty){
            var v = this.getSuccess(o);
            if(v === false || v === 'false'){
                success = false;
            }
        }
        var records = [];
        for(var i = 0; i < c; i++){
            var n = root[i];
            var values = {};
            var id = this.getId(n);
            for(var j = 0; j < fl; j++){
                f = fi[j];
                var v = this.ef[j](n);
                values[f.name] = f.convert((v !== undefined) ? v : f.defaultValue);
            }
            var record = new Record(values, id);
            record.json = n;
            records[i] = record;
        }
        return {
            success : success,
            records : records,
            totalRecords : totalRecords
        };
    },
    
    assetIdRenderer:function(v,m,rec){
        if(rec.data.issummaryvalue){
            return '<div style="height:10px;margin-top:2px;"><font size=2><b>'+v+'</b></font></div>';
        }else{
            return v;
        }                    
    }
});
