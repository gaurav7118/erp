Ext.define("Ext.device.Analytics",{alternateClassName:"Ext.ux.device.Analytics",singleton:true,requires:["Ext.device.Communicator","Ext.device.analytics.*"],constructor:function(){var A=Ext.browser.is;if(A.WebView&&A.Cordova){return Ext.create("Ext.device.analytics.Cordova")}else{return Ext.create("Ext.device.analytics.Abstract")}}})