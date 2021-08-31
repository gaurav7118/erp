Ext.define("Ext.tip.QuickTipManager",{requires:["Ext.tip.QuickTip"],singleton:true,alternateClassName:"Ext.QuickTips",disabled:false,init:function(E,B){var D=this;if(!D.tip){if(!Ext.isReady){Ext.onInternalReady(function(){Ext.tip.QuickTipManager.init(E,B)});return false}var A=Ext.apply({sticky:true,disabled:D.disabled,id:"ext-quicktips-tip"},B),C=A.className,F=A.xtype;if(C){delete A.className}else{if(F){C="widget."+F;delete A.xtype}}if(E!==false){A.renderTo=document.body;if(A.renderTo.tagName.toUpperCase()!=="BODY"){Ext.raise({sourceClass:"Ext.tip.QuickTipManager",sourceMethod:"init",msg:"Cannot init QuickTipManager: no document body"})}}D.tip=Ext.create(C||"Ext.tip.QuickTip",A);Ext.quickTipsActive=true}},destroy:function(){Ext.destroy(this.tip);this.tip=undefined},ddDisable:function(){var A=this,B=A.tip;if(B&&!A.disabled){B.disable()}},ddEnable:function(){var A=this,B=A.tip;if(B&&!A.disabled){B.enable()}},enable:function(){var A=this,B=A.tip;if(B){B.enable()}A.disabled=false},disable:function(){var A=this,B=A.tip;if(B){B.disable()}A.disabled=true},isEnabled:function(){var A=this.tip;return A!==undefined&&!A.disabled},getQuickTip:function(){return this.tip},register:function(){var A=this.tip;A.register.apply(A,arguments)},unregister:function(){var A=this.tip;A.unregister.apply(A,arguments)},tips:function(){var A=this.tip;A.register.apply(A,arguments)}})