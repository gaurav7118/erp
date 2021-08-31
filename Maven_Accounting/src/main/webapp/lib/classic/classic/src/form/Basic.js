Ext.define("Ext.form.Basic",{extend:"Ext.util.Observable",alternateClassName:"Ext.form.BasicForm",requires:["Ext.util.MixedCollection","Ext.form.action.Load","Ext.form.action.Submit","Ext.window.MessageBox","Ext.data.ErrorCollection","Ext.util.DelayedTask"],taskDelay:10,constructor:function(B,C){var D=this,A;D.owner=B;D.fieldMonitors={validitychange:D.checkValidityDelay,enable:D.checkValidityDelay,disable:D.checkValidityDelay,dirtychange:D.checkDirtyDelay,errorchange:D.checkErrorDelay,scope:D};D.checkValidityTask=new Ext.util.DelayedTask(D.checkValidity,D);D.checkDirtyTask=new Ext.util.DelayedTask(D.checkDirty,D);D.checkErrorTask=new Ext.util.DelayedTask(D.checkError,D);D.monitor=new Ext.container.Monitor({selector:"[isFormField]:not([excludeForm])",scope:D,addHandler:D.onFieldAdd,removeHandler:D.onFieldRemove,invalidateHandler:D.onMonitorInvalidate});D.monitor.bind(B);Ext.apply(D,C);if(Ext.isString(D.paramOrder)){D.paramOrder=D.paramOrder.split(/[\s,|]/)}A=D.reader;if(A&&!A.isReader){if(typeof A==="string"){A={type:A}}D.reader=Ext.createByAlias("reader."+A.type,A)}A=D.errorReader;if(A&&!A.isReader){if(typeof A==="string"){A={type:A}}D.errorReader=Ext.createByAlias("reader."+A.type,A)}D.callParent()},initialize:function(){this.initialized=true;this.onValidityChange(!this.hasInvalidField())},timeout:30,paramsAsHash:false,waitTitle:"Please Wait...",trackResetOnLoad:false,wasDirty:false,destroy:function(){var B=this,A=B.monitor;if(A){A.unbind();B.monitor=null}B.clearListeners();B.checkValidityTask.cancel();B.checkDirtyTask.cancel();B.checkErrorTask.cancel();B.checkValidityTask=B.checkDirtyTask=B.checkErrorTask=null;B.callParent()},onFieldAdd:function(A){A.on(this.fieldMonitors);this.onMonitorInvalidate()},onFieldRemove:function(A){A.un(this.fieldMonitors);this.onMonitorInvalidate()},onMonitorInvalidate:function(){if(this.initialized){this.checkValidityDelay()}},getFields:function(){return this.monitor.getItems()},getBoundItems:function(){var A=this._boundItems;if(!A||A.getCount()===0){A=this._boundItems=new Ext.util.MixedCollection();A.addAll(this.owner.query("[formBind]"))}return A},hasInvalidField:function(){return !!this.getFields().findBy(function(C){var A=C.preventMark,B;C.preventMark=true;B=C.isValid();C.preventMark=A;return !B})},isValid:function(){var A=this,B;Ext.suspendLayouts();B=A.getFields().filterBy(function(C){return !C.validate()});Ext.resumeLayouts(true);return B.length<1},checkValidity:function(){var B=this,A;if(B.destroyed){return }A=!B.hasInvalidField();if(A!==B.wasValid){B.onValidityChange(A);B.fireEvent("validitychange",B,A);B.wasValid=A}},checkValidityDelay:function(){var A=this.taskDelay;if(A){this.checkValidityTask.delay(A)}else{this.checkValidity()}},checkError:function(){this.fireEvent("errorchange",this)},checkErrorDelay:function(){var A=this.taskDelay;if(A){this.checkErrorTask.delay(A)}else{this.checkError()}},onValidityChange:function(F){var D=this.getBoundItems(),B,C,A,E;if(D){B=D.items;A=B.length;for(C=0;C<A;C++){E=B[C];if(E.disabled===F){E.setDisabled(!F)}}}},isDirty:function(){return !!this.getFields().findBy(function(A){return A.isDirty()})},checkDirtyDelay:function(){var A=this.taskDelay;if(A){this.checkDirtyTask.delay(A)}else{this.checkDirty()}},checkDirty:function(){var B=this,A;if(B.destroyed){return }A=this.isDirty();if(A!==this.wasDirty){this.fireEvent("dirtychange",this,A);this.wasDirty=A}},hasUpload:function(){return !!this.getFields().findBy(function(A){return A.isFileUpload()})},doAction:function(B,A){if(Ext.isString(B)){B=Ext.ClassManager.instantiateByAlias("formaction."+B,Ext.apply({},A,{form:this}))}if(this.fireEvent("beforeaction",this,B)!==false){this.beforeAction(B);Ext.defer(B.run,100,B)}return this},submit:function(A){A=A||{};var B=this,C;if(A.standardSubmit||B.standardSubmit){C="standardsubmit"}else{C=B.api?"directsubmit":"submit"}return B.doAction(C,A)},load:function(A){return this.doAction(this.api?"directload":"load",A)},updateRecord:function(C){C=C||this._record;if(!C){Ext.raise("A record is required.");return this}var B=C.self.fields,D=this.getFieldValues(),G={},F=0,A=B.length,E;for(;F<A;++F){E=B[F].name;if(D.hasOwnProperty(E)){G[E]=D[E]}}C.beginEdit();C.set(G);C.endEdit();return this},loadRecord:function(A){this._record=A;return this.setValues(A.getData())},getRecord:function(){return this._record},beforeAction:function(C){var F=this,B=C.waitMsg,I=Ext.baseCSSPrefix+"mask-loading",D=F.getFields().items,E,H=D.length,G,A;for(E=0;E<H;E++){G=D[E];if(G.isFormField&&G.syncValue){G.syncValue()}}if(B){A=F.waitMsgTarget;if(A===true){F.owner.el.mask(B,I)}else{if(A){A=F.waitMsgTarget=Ext.get(A);A.mask(B,I)}else{F.floatingAncestor=F.owner.up("[floating]");if(F.floatingAncestor){F.savePreventFocusOnActivate=F.floatingAncestor.preventFocusOnActivate;F.floatingAncestor.preventFocusOnActivate=true}Ext.MessageBox.wait(B,C.waitTitle||F.waitTitle)}}}},afterAction:function(C,E){var A=this;if(C.waitMsg){var B=Ext.MessageBox,D=A.waitMsgTarget;if(D===true){A.owner.el.unmask()}else{if(D){D.unmask()}else{B.hide()}}}if(A.floatingAncestor){A.floatingAncestor.preventFocusOnActivate=A.savePreventFocusOnActivate}if(E){if(C.reset){A.reset()}Ext.callback(C.success,C.scope||C,[A,C]);A.fireEvent("actioncomplete",A,C)}else{Ext.callback(C.failure,C.scope||C,[A,C]);A.fireEvent("actionfailed",A,C)}},findField:function(A){return this.getFields().findBy(function(B){return B.id===A||B.name===A||B.dataIndex===A})},markInvalid:function(H){var D=this,F,A,B,E,C;function G(I,K){var J=D.findField(I);if(J){J.markInvalid(K)}}if(Ext.isArray(H)){A=H.length;for(F=0;F<A;F++){B=H[F];G(B.id||B.field,B.msg||B.message)}}else{if(H instanceof Ext.data.ErrorCollection){A=H.items.length;for(F=0;F<A;F++){B=H.items[F];G(B.field,B.message)}}else{for(C in H){if(H.hasOwnProperty(C)){E=H[C];G(C,E,H)}}}}return this},setValues:function(B){var D=this,A,C,F;function E(G,I){var H=D.findField(G);if(H){H.setValue(I);if(D.trackResetOnLoad){H.resetOriginalValue()}}}Ext.suspendLayouts();if(Ext.isArray(B)){C=B.length;for(A=0;A<C;A++){F=B[A];E(F.id,F.value)}}else{Ext.iterate(B,E)}Ext.resumeLayouts(true);return this},getValues:function(I,J,N,L,C){var M={},G=this.getFields().items,O=G.length,F=Ext.isArray,K,E,D,B,A,H;for(H=0;H<O;H++){K=G[H];if(!J||K.isDirty()){E=K[L?"getModelData":"getSubmitData"](N,C);if(Ext.isObject(E)){for(A in E){if(E.hasOwnProperty(A)){D=E[A];if(N&&D===""){D=K.emptyText||""}if(!K.isRadio){if(M.hasOwnProperty(A)){B=M[A];if(!F(B)){B=M[A]=[B]}if(F(D)){M[A]=B.concat(D)}else{B.push(D)}}else{M[A]=D}}else{M[A]=M[A]||D}}}}}}if(I){M=Ext.Object.toQueryString(M)}return M},getFieldValues:function(A){return this.getValues(false,A,false,true)},clearInvalid:function(){Ext.suspendLayouts();var B=this,A=B.getFields().items,C,D=A.length;for(C=0;C<D;C++){A[C].clearInvalid()}Ext.resumeLayouts(true);return B},reset:function(B){Ext.suspendLayouts();var C=this,A=C.getFields().items,D,E=A.length;for(D=0;D<E;D++){A[D].reset()}Ext.resumeLayouts(true);if(B===true){delete C._record}return C},applyToFields:function(C){var A=this.getFields().items,B,D=A.length;for(B=0;B<D;B++){Ext.apply(A[B],C)}return this},applyIfToFields:function(C){var A=this.getFields().items,B,D=A.length;for(B=0;B<D;B++){Ext.applyIf(A[B],C)}return this}})