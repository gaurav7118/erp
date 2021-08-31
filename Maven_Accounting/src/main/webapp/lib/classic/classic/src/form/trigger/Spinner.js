Ext.define("Ext.form.trigger.Spinner",{extend:"Ext.form.trigger.Trigger",alias:"trigger.spinner",cls:Ext.baseCSSPrefix+"form-trigger-spinner",spinnerCls:Ext.baseCSSPrefix+"form-spinner",spinnerUpCls:Ext.baseCSSPrefix+"form-spinner-up",spinnerDownCls:Ext.baseCSSPrefix+"form-spinner-down",focusCls:Ext.baseCSSPrefix+"form-spinner-focus",overCls:Ext.baseCSSPrefix+"form-spinner-over",clickCls:Ext.baseCSSPrefix+"form-spinner-click",focusFieldOnClick:true,vertical:true,bodyTpl:'<tpl if="vertical"><div class="{spinnerCls} {spinnerCls}-{ui} {spinnerUpCls} {spinnerUpCls}-{ui} {childElCls} {upDisabledCls}"></div></tpl><div class="{spinnerCls} {spinnerCls}-{ui} {spinnerDownCls} {spinnerDownCls}-{ui} {childElCls} {downDisabledCls}"></div><tpl if="!vertical"><div class="{spinnerCls} {spinnerCls}-{ui} {spinnerUpCls} {spinnerUpCls}-{ui} {childElCls} {upDisabledCls}"></div></tpl>',destroy:function(){var A=this;if(A.spinnerEl){A.spinnerEl.destroy();A.spinnerEl=A.upEl=A.downEl=null}A.callParent()},getBodyRenderData:function(){var A=this;return{vertical:A.vertical,upDisabledCls:A.upEnabled?"":(A.spinnerUpCls+"-disabled"),downDisabledCls:A.downEnabled?"":(A.spinnerDownCls+"-disabled"),spinnerCls:A.spinnerCls,spinnerUpCls:A.spinnerUpCls,spinnerDownCls:A.spinnerDownCls}},getStateEl:function(){return this.spinnerEl},onClick:function(){var B=this,A=arguments,D=B.clickRepeater?A[1]:A[0],C=B.field;if(!C.readOnly&&!C.disabled){if(B.upEl.contains(D.target)){Ext.callback(B.upHandler,B.scope,[C,B,D],0,C)}else{if(B.downEl.contains(D.target)){Ext.callback(B.downHandler,B.scope,[C,B,D],0,C)}}}C.inputEl.focus()},onFieldRender:function(){var B=this,A=B.vertical,D,C;B.callParent();D=B.spinnerEl=B.el.select("."+B.spinnerCls,true);C=D.elements;B.upEl=A?C[0]:C[1];B.downEl=A?C[1]:C[0]},setUpEnabled:function(A){this.upEl[A?"removeCls":"addCls"](this.spinnerUpCls+"-disabled")},setDownEnabled:function(A){this.downEl[A?"removeCls":"addCls"](this.spinnerDownCls+"-disabled")}})