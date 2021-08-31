Ext.define("Ext.form.field.Base",{extend:"Ext.Component",mixins:["Ext.form.Labelable","Ext.form.field.Field"],xtype:"field",alternateClassName:["Ext.form.Field","Ext.form.BaseField"],requires:["Ext.util.DelayedTask","Ext.XTemplate"],focusable:true,shrinkWrap:true,fieldSubTpl:['<input id="{id}" data-ref="inputEl" type="{type}" {inputAttrTpl}',' size="1"','<tpl if="name"> name="{name}"</tpl>','<tpl if="value"> value="{[Ext.util.Format.htmlEncode(values.value)]}"</tpl>','<tpl if="placeholder"> placeholder="{placeholder}"</tpl>','{%if (values.maxLength !== undefined){%} maxlength="{maxLength}"{%}%}','<tpl if="readOnly"> readonly="readonly"</tpl>','<tpl if="disabled"> disabled="disabled"</tpl>','<tpl if="tabIdx != null"> tabindex="{tabIdx}"</tpl>','<tpl if="fieldStyle"> style="{fieldStyle}"</tpl>','<tpl foreach="inputElAriaAttributes"> {$}="{.}"</tpl>',' class="{fieldCls} {typeCls} {typeCls}-{ui} {editableCls} {inputCls}" autocomplete="off"/>',{disableFormats:true}],defaultBindProperty:"value",autoEl:{role:"presentation"},subTplInsertions:["inputAttrTpl"],childEls:["inputEl"],inputType:"text",isTextInput:true,invalidText:"The value in this field is invalid",fieldCls:Ext.baseCSSPrefix+"form-field",focusCls:"form-focus",dirtyCls:Ext.baseCSSPrefix+"form-dirty",checkChangeEvents:Ext.isIE&&(!document.documentMode||document.documentMode<=9)?["change","propertychange","keyup"]:["change","input","textInput","keyup","dragdrop"],ignoreChangeRe:/data\-errorqtip|style\.|className/,checkChangeBuffer:50,liquidLayout:true,readOnly:false,readOnlyCls:Ext.baseCSSPrefix+"form-readonly",validateOnBlur:true,hasFocus:false,baseCls:Ext.baseCSSPrefix+"field",fieldBodyCls:Ext.baseCSSPrefix+"field-body",maskOnDisable:false,stretchInputElFixed:true,ariaEl:"inputEl",initComponent:function(){var A=this;A.callParent();A.subTplData=A.subTplData||{};A.initLabelable();A.initField();A.initDefaultName();if(A.readOnly){A.addCls(A.readOnlyCls)}A.addCls(Ext.baseCSSPrefix+"form-type-"+A.inputType)},initDefaultName:function(){var A=this;if(!A.name){A.name=A.getInputId()}},getInputId:function(){return this.inputId||(this.inputId=this.id+"-inputEl")},getSubTplData:function(C){var E=this,D=E.inputType,A=E.getInputId(),F,B;F=Ext.apply({ui:E.ui,id:A,cmpId:E.id,name:E.name||A,disabled:E.disabled,readOnly:E.readOnly,value:E.getRawValue(),type:D,fieldCls:E.fieldCls,fieldStyle:E.getFieldStyle(),childElCls:C.childElCls,tabIdx:E.tabIndex,inputCls:E.inputCls,typeCls:Ext.baseCSSPrefix+"form-"+(E.isTextInput?"text":D)},E.subTplData);if(E.ariaRole){B={role:E.ariaRole,"aria-hidden":!!E.hidden,"aria-disabled":!!E.disabled,"aria-readonly":!!E.readOnly,"aria-invalid":false};if(E.ariaLabel){B["aria-label"]=E.ariaLabel}if(E.format&&E.formatText&&!F.title){B.title=Ext.String.formatEncode(E.formatText,E.format)}F.inputElAriaAttributes=Ext.apply(B,E.getAriaAttributes())}E.getInsertionRenderData(F,E.subTplInsertions);return F},getSubTplMarkup:function(B){var C=this,D=C.getSubTplData(B),E=C.getTpl("preSubTpl"),F=C.getTpl("postSubTpl"),A="";if(E){A+=E.apply(D)}A+=C.getTpl("fieldSubTpl").apply(D);if(F){A+=F.apply(D)}return A},initRenderData:function(){return Ext.applyIf(this.callParent(),this.getLabelableRenderData())},setFieldStyle:function(A){var B=this,C=B.inputEl;if(C){C.applyStyles(A)}B.fieldStyle=A},getFieldStyle:function(){var A=this.fieldStyle;return Ext.isObject(A)?Ext.DomHelper.generateStyles(A,null,true):A||""},onRender:function(){this.callParent(arguments);this.mixins.labelable.self.initTip();this.renderActiveError()},onFocusLeave:function(A){this.callParent([A]);this.completeEdit()},completeEdit:Ext.emptyFn,isFileUpload:function(){return this.inputType==="file"},getSubmitData:function(){var A=this,B=null,C;if(!A.disabled&&A.submitValue){C=A.getSubmitValue();if(C!==null){B={};B[A.getName()]=C}}return B},getSubmitValue:function(){return this.processRawValue(this.getRawValue())},getRawValue:function(){var B=this,A=(B.inputEl?B.inputEl.getValue():Ext.valueFrom(B.rawValue,""));B.rawValue=A;return A},setRawValue:function(C){var A=this,B=A.rawValue;if(!A.transformRawValue.$nullFn){C=A.transformRawValue(C)}C=Ext.valueFrom(C,"");if(B===undefined||B!==C||A.valueContainsPlaceholder){A.rawValue=C;if(A.inputEl){A.bindChangeEvents(false);A.inputEl.dom.value=C;A.bindChangeEvents(true)}if(A.rendered&&A.reference){A.publishState("rawValue",C)}}return C},transformRawValue:Ext.identityFn,valueToRaw:function(A){return""+Ext.valueFrom(A,"")},rawToValue:Ext.identityFn,processRawValue:Ext.identityFn,getValue:function(){var A=this,B=A.rawToValue(A.processRawValue(A.getRawValue()));A.value=B;return B},setValue:function(B){var A=this;A.setRawValue(A.valueToRaw(B));return A.mixins.field.setValue.call(A,B)},onBoxReady:function(){var A=this;A.callParent(arguments);if(A.setReadOnlyOnBoxReady){A.setReadOnly(A.readOnly)}},onDisable:function(){var A=this,B=A.inputEl;A.callParent();if(B){B.dom.disabled=true;if(A.hasActiveError()){A.clearInvalid();A.hadErrorOnDisable=true}}if(A.wasValid===false){A.checkValidityChange(true)}},onEnable:function(){var B=this,C=B.inputEl,D=B.preventMark,A;B.callParent();if(C){C.dom.disabled=false}if(B.wasValid!==undefined){B.forceValidation=true;B.preventMark=!B.hadErrorOnDisable;A=B.isValid();B.forceValidation=false;B.preventMark=D;B.checkValidityChange(A)}delete B.hadErrorOnDisable},setReadOnly:function(D){var B=this,C=B.inputEl,A=B.readOnly;D=!!D;B[D?"addCls":"removeCls"](B.readOnlyCls);B.readOnly=D;if(C){C.dom.readOnly=D;B.ariaEl.dom.setAttribute("aria-readonly",D)}else{if(B.rendering){B.setReadOnlyOnBoxReady=true}}if(D!==A){B.fireEvent("writeablechange",B,D)}},fireKey:function(A){if(A.isSpecialKey()){this.fireEvent("specialkey",this,A)}},initEvents:function(){var E=this,G=E.inputEl,F=E.onFieldMutation,C=E.checkChangeEvents,A=C.length,B,D;if(G){E.mon(G,Ext.supports.SpecialKeyDownRepeat?"keydown":"keypress",E.fireKey,E);for(B=0;B<A;++B){D=C[B];if(D==="propertychange"){E.usesPropertychange=true}if(D==="textInput"){E.usesTextInput=true}E.mon(G,D,F,E)}}E.callParent()},onFieldMutation:function(A){if(!this.readOnly&&!(A.type==="propertychange"&&this.ignoreChangeRe.test(A.browserEvent.propertyName))){this.startCheckChangeTask()}},startCheckChangeTask:function(){var B=this,A=B.checkChangeTask;if(!A){B.checkChangeTask=A=new Ext.util.DelayedTask(B.doCheckChangeTask,B)}if(!B.bindNotifyListener){B.bindNotifyListener=Ext.on("beforebindnotify",B.onBeforeNotify,B,{destroyable:true})}A.delay(B.checkChangeBuffer)},doCheckChangeTask:function(){var A=this.bindNotifyListener;if(A){A.destroy();this.bindNotifyListener=null}this.checkChange()},publishValue:function(){var A=this;if(A.rendered&&!A.getErrors().length){A.publishState("value",A.getValue())}},onDirtyChange:function(B){var A=this;A[B?"addCls":"removeCls"](A.dirtyCls);if(A.rendered&&A.reference){A.publishState("dirty",B)}},isValid:function(){var B=this,A=B.disabled,C=B.forceValidation||!A;return C?B.validateValue(B.processRawValue(B.getRawValue())):A},validateValue:function(B){var A=this,D=A.getErrors(B),C=Ext.isEmpty(D);if(!A.preventMark){if(C){A.clearInvalid()}else{A.markInvalid(D)}}return C},markInvalid:function(E){var C=this,A=C.ariaEl.dom,B=C.getActiveError(),D;C.setActiveErrors(Ext.Array.from(E));D=C.getActiveError();if(B!==D){C.setError(D);if(!C.ariaStaticRoles[C.ariaRole]&&A){A.setAttribute("aria-invalid",true)}}},clearInvalid:function(){var C=this,A=C.ariaEl.dom,B=C.hasActiveError();delete C.hadErrorOnDisable;C.unsetActiveError();if(B){C.setError("");if(!C.ariaStaticRoles[C.ariaRole]&&A){A.setAttribute("aria-invalid",false)}}},setError:function(A){var C=this,B=C.msgTarget,D;if(C.rendered){if(B==="title"||B==="qtip"){D=B==="qtip"?"data-errorqtip":"title";C.getActionEl().dom.setAttribute(D,A||"")}else{C.updateLayout()}}},renderActiveError:function(){var C=this,B=C.hasActiveError(),A=C.invalidCls+"-field";if(C.inputEl){C.inputEl[B?"addCls":"removeCls"]([A,A+"-"+C.ui])}C.mixins.labelable.renderActiveError.call(C)},beforeDestroy:function(){var B=this,A=B.checkChangeTask;if(A){A.cancel()}B.checkChangeTask=B.bindNotifyListener=Ext.destroy(B.bindNotifyListener);B.callParent()},privates:{applyBind:function(F,C){var B=this,A=C&&C.value,E,D;E=B.callParent([F,C]);if(E){D=E.value;B.hasBindingValue=!!D;if(D!==A&&B.getInherited().modelValidation){B.updateValueBinding(E)}}return E},applyRenderSelectors:function(){var A=this;A.callParent();if(!A.inputEl){A.inputEl=A.el.getById(A.getInputId())}},bindChangeEvents:function(B){var C=B?"resumeEvent":"suspendEvent",A=this.inputEl;if(this.usesPropertychange){A[C]("propertychange")}if(this.usesTextInput){A[C]("textInput")}},getActionEl:function(){return this.inputEl||this.el},getFocusEl:function(){return this.inputEl},initRenderTpl:function(){var A=this;if(!A.hasOwnProperty("renderTpl")){A.renderTpl=A.getTpl("labelableRenderTpl")}return A.callParent()},onBeforeNotify:function(){this.checkChangeTask.cancel();this.checkChange()},updateValueBinding:function(D){var C=this,B=D.value,A=D.$fieldBinding;if(A){A.destroy();D.$fieldBinding=null}if(B&&B.bindValidationField){C.fieldBinding=B.bindValidationField("setValidationField",C)}}},deprecated:{"5":{methods:{doComponentLayout:function(){this.bindChangeEvents(false);this.callParent(arguments);this.bindChangeEvents(true)}}}}})