Ext.define("Ext.form.field.Radio",{extend:"Ext.form.field.Checkbox",alias:["widget.radiofield","widget.radio"],alternateClassName:"Ext.form.Radio",requires:["Ext.form.RadioManager"],isRadio:true,inputType:"radio",ariaRole:"radio",tabIndex:0,formId:null,getGroupValue:function(){var A=this.getManager().getChecked(this.name,this.getFormId());return A?A.inputValue:null},onBoxClick:function(){var A=this;if(!A.disabled&&!A.readOnly){this.setValue(true)}},onRemoved:function(){this.callParent(arguments);this.formId=null},setValue:function(B){var A=this,C;if(Ext.isBoolean(B)){A.callParent(arguments)}else{C=A.getManager().getWithValue(A.name,B,A.getFormId()).getAt(0);if(C){C.setValue(true)}}return A},getSubmitValue:function(){return this.checked?this.inputValue:null},getModelData:function(){var A=this.callParent(arguments);if(A){A[this.getName()]=this.getSubmitValue()}return A},onChange:function(C,A){var F=this,E,D,B,G;F.callParent(arguments);if(C){G=F.getManager().getByName(F.name,F.getFormId()).items;D=G.length;for(E=0;E<D;E++){B=G[E];if(B!==F){B.setValue(false)}}}},getManager:function(){return Ext.form.RadioManager}})