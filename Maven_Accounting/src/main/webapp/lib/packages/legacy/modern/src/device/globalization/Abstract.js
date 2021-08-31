Ext.define("Ext.device.globalization.Abstract",{mixins:["Ext.mixin.Observable"],config:{formatLength:"full",selector:"date and time",dateType:"wide",items:"months",numberType:"decimal",currencyCode:"USD"},getPreferredLanguage:function(A){if(!A.success){Ext.Logger.warn("You need to specify a `success` function for #getPreferredLanguage")}return A},getLocaleName:function(A){if(!A.success){Ext.Logger.warn("You need to specify a `success` function for #getLocaleName")}return A},dateToString:function(B){var A=Ext.device.globalization.Abstract.prototype.config;B=Ext.applyIf(B,{date:new Date(),formatLength:A.formatLength,selector:A.selector});if(!B.success){Ext.Logger.warn("You need to specify a `success` function for #dateToString")}return B},stringToDate:function(B){var A=Ext.device.globalization.Abstract.prototype.config;B=Ext.applyIf(B,{dateString:Ext.util.Format.date(new Date(),"m/d/Y"),formatLength:A.formatLength,selector:A.selector});if(!B.success){Ext.Logger.warn("You need to specify a `success` function for #stringToDate")}return B},getDatePattern:function(B){var A=Ext.device.globalization.Abstract.prototype.config;B=Ext.applyIf(B,{formatLength:A.formatLength,selector:A.selector});if(!B.success){Ext.Logger.warn("You need to specify a `success` function for #getDatePattern")}return B},getDateNames:function(B){var A=Ext.device.globalization.Abstract.prototype.config;B=Ext.applyIf(B,{type:A.dateType,items:A.items});if(!B.success){Ext.Logger.warn("You need to specify a `success` function for #getDateNames")}return B},isDayLightSavingsTime:function(A){A=Ext.applyIf(A,{date:new Date()});if(!A.success){Ext.Logger.warn("You need to specify a `success` function for #isDayLightSavingsTime")}return A},getFirstDayOfWeek:function(A){if(!A.success){Ext.Logger.warn("You need to specify a `success` function for #getFirstDayOfWeek")}return A},numberToString:function(B){var A=Ext.device.globalization.Abstract.prototype.config;B=Ext.applyIf(B,{number:A.number,type:A.numberType});if(!B.number){Ext.Logger.warn("You need to specify a `number` for #numberToString")}if(!B.success){Ext.Logger.warn("You need to specify a `success` function for #numberToString")}return B},stringToNumber:function(B){var A=Ext.device.globalization.Abstract.prototype.config;B=Ext.applyIf(B,{type:A.numberType});if(!B.number){Ext.Logger.warn("You need to specify a `string` for #stringToNumber")}if(!B.success){Ext.Logger.warn("You need to specify a `success` function for #stringToNumber")}return B},getNumberPattern:function(B){var A=Ext.device.globalization.Abstract.prototype.config;B=Ext.applyIf(B,{type:A.numberType});if(!B.success){Ext.Logger.warn("You need to specify a `success` function for #getNumberPattern")}return B},getCurrencyPattern:function(B){var A=Ext.device.globalization.Abstract.prototype.config;B=Ext.applyIf(B,{currencyCode:A.currencyCode});if(!B.success){Ext.Logger.warn("You need to specify a `success` function for #getCurrency")}return B}})