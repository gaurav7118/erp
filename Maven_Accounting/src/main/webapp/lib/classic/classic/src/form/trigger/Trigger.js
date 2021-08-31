Ext.define("Ext.form.trigger.Trigger",{alias:"trigger.trigger",requires:["Ext.util.ClickRepeater"],mixins:["Ext.mixin.Factoryable"],factoryConfig:{defaultType:"trigger"},repeatClick:false,hidden:false,hideOnReadOnly:undefined,weight:0,preventMouseDown:true,baseCls:Ext.baseCSSPrefix+"form-trigger",focusCls:Ext.baseCSSPrefix+"form-trigger-focus",overCls:Ext.baseCSSPrefix+"form-trigger-over",clickCls:Ext.baseCSSPrefix+"form-trigger-click",validIdRe:Ext.validIdRe,renderTpl:['<div id="{triggerId}" class="{baseCls} {baseCls}-{ui} {cls} {cls}-{ui} {extraCls} ','{childElCls}"<tpl if="triggerStyle"> style="{triggerStyle}"</tpl>>',"{[values.$trigger.renderBody(values)]}","</div>"],statics:{weightComparator:function(A,B){return A.weight-B.weight}},constructor:function(B){var C=this,A;Ext.apply(C,B);if(C.compat4Mode){A=C.cls;C.focusCls=[C.focusCls,A+"-focus"];C.overCls=[C.overCls,A+"-over"];C.clickCls=[C.clickCls,A+"-click"]}if(!C.validIdRe.test(C.id)){Ext.raise('Invalid trigger "id": "'+C.id+'"')}},afterFieldRender:function(){this.initEvents()},destroy:function(){var A=this;A.clickRepeater=A.el=Ext.destroy(A.clickRepeater,A.el);A.callParent()},getBodyRenderData:Ext.emptyFn,getEl:function(){return this.el||null},getStateEl:function(){return this.el},hide:function(){var B=this,A=B.el;B.hidden=true;if(A){A.hide()}},initEvents:function(){var D=this,A=D.isFieldEnabled,C=D.getStateEl(),B=D.el;C.addClsOnOver(D.overCls,A,D);C.addClsOnClick(D.clickCls,A,D);if(D.repeatClick){D.clickRepeater=new Ext.util.ClickRepeater(B,{preventDefault:true,handler:D.onClick,listeners:{mousedown:D.onClickRepeaterMouseDown,scope:D},scope:D})}else{D.field.mon(B,{click:D.onClick,mousedown:D.onMouseDown,scope:D})}},isFieldEnabled:function(){return !this.field.disabled},isVisible:function(){var A=this,C=A.field,B=false;if(A.hidden||!C||!A.rendered||A.destroyed){B=true}return !B},onClick:function(){var C=this,A=arguments,E=C.clickRepeater?A[1]:A[0],B=C.handler,D=C.field;if(B&&!D.readOnly&&C.isFieldEnabled()){Ext.callback(C.handler,C.scope,[D,C,E],0,D)}},resolveListenerScope:function(A){return this.field.resolveSatelliteListenerScope(this,A)},onMouseDown:function(A){if(A.pointerType!=="touch"&&!this.field.owns(Ext.Element.getActiveElement())){this.field.inputEl.focus()}if(this.preventMouseDown){A.preventDefault()}},onClickRepeaterMouseDown:function(B,A){if(!A.parentEvent||A.parentEvent.type==="mousedown"){this.field.inputEl.focus()}A.preventDefault()},onFieldBlur:function(){this.getStateEl().removeCls(this.focusCls)},onFieldFocus:function(){this.getStateEl().addCls(this.focusCls)},onFieldRender:function(){var B=this,A=B.el=B.field.triggerWrap.selectNode("#"+B.domId,false);A.setVisibilityMode(Ext.Element.DISPLAY);B.rendered=true},renderBody:function(B){var A=this,C=A.bodyTpl;Ext.apply(B,A.getBodyRenderData());return C?Ext.XTemplate.getTpl(A,"bodyTpl").apply(B):""},renderTrigger:function(A){var C=this,B=C.width,D=C.hidden?"display:none;":"";if(B){D+="width:"+B}return Ext.XTemplate.getTpl(C,"renderTpl").apply({$trigger:C,fieldData:A,ui:A.ui,childElCls:A.childElCls,triggerId:C.domId=C.field.id+"-trigger-"+C.id,cls:C.cls,triggerStyle:D,extraCls:C.extraCls,baseCls:C.baseCls})},setHidden:function(A){if(A!==this.hidden){this[A?"hide":"show"]()}},setVisible:function(A){this.setHidden(!A)},show:function(){var B=this,A=B.el;B.hidden=false;if(A){A.show()}}})