Ext.define("Ext.app.bind.BaseBinding",{extend:"Ext.util.Schedulable",calls:0,kind:20,defaultOptions:{},lastValue:undefined,constructor:function(A,E,C,B){var D=this;D.options=B;D.owner=A;D.scope=C;D.callback=E;if(!E){Ext.raise("Callback is required")}D.lateBound=Ext.isString(E);if(B&&B.deep){D.deep=true}D.callParent()},destroy:function(){var B=this,A=B.owner;B.callParent();if(A){A.onBindDestroy(B)}B.scope=B.callback=B.owner=null},isReadOnly:function(){return true},privates:{getScheduler:function(){var A=this.owner;return A&&A.getScheduler()},getSession:function(){var A=this.owner;return A.isSession?A:A.getSession()},notify:function(D){var C=this,A=C.options||C.defaultOptions,B=C.lastValue;if(!C.calls||C.deep||B!==D||Ext.isArray(D)){++C.calls;C.lastValue=D;if(C.lateBound){C.scope[C.callback](D,B,C)}else{C.callback.call(C.scope,D,B,C)}if(A.single){C.destroy()}}}}})