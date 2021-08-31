Ext.define("Ext.form.CheckboxGroup",{extend:"Ext.form.FieldContainer",mixins:{field:"Ext.form.field.Field"},alias:"widget.checkboxgroup",requires:["Ext.layout.container.CheckboxGroup","Ext.form.field.Checkbox","Ext.form.field.Base"],columns:"auto",vertical:false,allowBlank:true,blankText:"You must select at least one item in this group",defaultType:"checkboxfield",defaultBindProperty:"value",groupCls:Ext.baseCSSPrefix+"form-check-group",extraFieldBodyCls:Ext.baseCSSPrefix+"form-checkboxgroup-body",layout:"checkboxgroup",componentCls:Ext.baseCSSPrefix+"form-checkboxgroup",ariaRole:"group",ariaEl:"containerEl",ariaRenderAttributes:{"aria-invalid":false},initComponent:function(){var A=this;A.name=A.name||A.id;A.callParent();A.initField()},initRenderData:function(){var F=this,G,B,D,C,A,E;G=F.callParent();G.inputId=F.id+"-"+F.ariaEl;B=G.ariaAttributes;if(B){D=F.getBoxes();E=[];for(C=0,A=D.length;C<A;C++){E.push(D[C].id+"-inputEl")}B["aria-owns"]=E.join(" ")}return G},initValue:function(){var B=this,A=B.value;B.originalValue=B.lastValue=A||B.getValue();if(A){B.setValue(A)}},onAdd:function(E){var D=this,B,A,C;if(E.isCheckbox){if(!E.name){E.name=D.name}D.mon(E,"change",D.checkChange,D)}else{if(E.isContainer){B=E.items.items;for(C=0,A=B.length;C<A;C++){D.onAdd(B[C])}}}D.callParent(arguments)},onRemove:function(E){var D=this,B,A,C;if(E.isCheckbox){D.mun(E,"change",D.checkChange,D)}else{if(E.isContainer){B=E.items.items;for(C=0,A=B.length;C<A;C++){D.onRemove(B[C])}}}D.callParent(arguments)},isEqual:function(B,A){var C=Ext.Object.toQueryString;return C(B)===C(A)},getErrors:function(){var A=[];if(!this.allowBlank&&Ext.isEmpty(this.getChecked())){A.push(this.blankText)}return A},getBoxes:function(A){return this.query("[isCheckbox]"+(A||""))},eachBox:function(B,A){Ext.Array.forEach(this.getBoxes(),B,A||this)},getChecked:function(){return this.getBoxes("[checked]")},isDirty:function(){var B=this.getBoxes(),A,C=B.length;for(A=0;A<C;A++){if(B[A].isDirty()){return true}}},setReadOnly:function(D){var B=this.getBoxes(),A,C=B.length;for(A=0;A<C;A++){B[A].setReadOnly(D)}this.readOnly=D},reset:function(){var C=this,B=C.hasActiveError(),A=C.preventMark;C.preventMark=true;C.batchChanges(function(){var E=C.getBoxes(),D,F=E.length;for(D=0;D<F;D++){E[D].reset()}});C.preventMark=A;C.unsetActiveError();if(B){C.updateLayout()}},resetOriginalValue:function(){var C=this,B=C.getBoxes(),A,D=B.length;for(A=0;A<D;A++){B[A].resetOriginalValue()}C.originalValue=C.getValue();C.checkDirty()},setValue:function(F){var E=this,C=E.getBoxes(),A,H=C.length,D,B,G;E.batchChanges(function(){Ext.suspendLayouts();for(A=0;A<H;A++){D=C[A];B=D.getName();G=false;if(F){if(Ext.isArray(F[B])){G=Ext.Array.contains(F[B],D.inputValue)}else{G=F[B]}}D.setValue(G)}Ext.resumeLayouts(true)});return E},getValue:function(){var C={},E=this.getBoxes(),B,H=E.length,F,D,A,G;for(B=0;B<H;B++){F=E[B];D=F.getName();A=F.inputValue;if(F.getValue()){if(C.hasOwnProperty(D)){G=C[D];if(!Ext.isArray(G)){G=C[D]=[G]}G.push(A)}else{C[D]=A}}}return C},getSubmitData:function(){return null},getModelData:function(){return null},validate:function(){var A=this,D,C,B;if(A.disabled){C=true}else{D=A.getErrors();C=Ext.isEmpty(D);B=A.wasValid;if(C){A.unsetActiveError()}else{A.setActiveError(D)}}if(C!==B){A.wasValid=C;A.fireEvent("validitychange",A,C);A.updateLayout()}return C}},function(){this.borrow(Ext.form.field.Base,["markInvalid","clearInvalid","setError"])})