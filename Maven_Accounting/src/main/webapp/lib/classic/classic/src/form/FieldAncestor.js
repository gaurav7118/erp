Ext.define("Ext.form.FieldAncestor",{extend:"Ext.Mixin",requires:["Ext.container.Monitor"],mixinConfig:{id:"fieldAncestor",after:{initInheritedState:"initFieldInheritedState"},before:{destroy:"onBeforeDestroy"}},initFieldAncestor:function(){var A=this;A.monitor=new Ext.container.Monitor({scope:A,selector:"[isFormField]:not([excludeForm])",addHandler:A.onChildFieldAdd,removeHandler:A.onChildFieldRemove});A.initFieldDefaults()},initMonitor:function(){this.monitor.bind(this)},initFieldInheritedState:function(B){var A=B.fieldDefaults,C=this.fieldDefaults;if(C){if(A){B.fieldDefaults=Ext.apply(Ext.Object.chain(A),C)}else{B.fieldDefaults=C}}},onChildFieldAdd:function(B){var A=this;A.mon(B,"errorchange",A.handleFieldErrorChange,A);A.mon(B,"validitychange",A.handleFieldValidityChange,A)},onChildFieldRemove:function(B){var A=this;A.mun(B,"errorchange",A.handleFieldErrorChange,A);A.mun(B,"validitychange",A.handleFieldValidityChange,A)},initFieldDefaults:function(){if(!this.fieldDefaults){this.fieldDefaults={}}},handleFieldValidityChange:function(C,B){var A=this;if(C!==A){A.fireEvent("fieldvaliditychange",A,C,B);A.onFieldValidityChange(C,B)}},handleFieldErrorChange:function(B,A){var C=this;if(B!==C){C.fireEvent("fielderrorchange",C,B,A);C.onFieldErrorChange(B,A)}},onFieldValidityChange:Ext.emptyFn,onFieldErrorChange:Ext.emptyFn,onBeforeDestroy:function(){this.monitor.unbind()}})