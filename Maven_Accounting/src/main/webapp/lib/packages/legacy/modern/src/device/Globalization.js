Ext.define("Ext.device.Globalization",{singleton:true,requires:["Ext.device.globalization.Cordova","Ext.device.globalization.Simulator"],constructor:function(){var A=Ext.browser.is;if(A.WebView){if(A.Cordova){return Ext.create("Ext.device.globalization.Cordova")}}return Ext.create("Ext.device.globalization.Simulator")}})